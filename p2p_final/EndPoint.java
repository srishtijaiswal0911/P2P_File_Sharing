import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.BitSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

public class EndPoint implements Runnable {
    // Peer and connection related variables
    int peer1ID; // ID of the first peer
    Peer peer1; // Reference to the first peer object
    int peer2ID; // ID of the second peer
    Socket socket; // Socket for communication between peers
    ExecutorService executorService; // Executor service for handling tasks in parallel
    ScheduledExecutorService scheduler; // Scheduler for scheduling tasks at fixed rates or delays
    InputStream inputStream; // Input stream for reading data from the socket
    OutputStream streamOut; // Output stream for writing data to the socket
    Bitfield bitfield; // Bitfield representing which pieces of the file the peer has
    BitSet peerBitField; // Bitfield representing which pieces of the file the other peer has
    FilePieces filePieces; // Object representing the file pieces
    boolean handshakeInitiated = false; // Flag to check if handshake has been initiated
    boolean choke = true; // Flag to check if the peer is choked or not
    LogHelper peerLogger;

    // Constructor for initializing the endpoint with two peer IDs
    public EndPoint(int peer1ID, Peer peer1, int peer2ID, Socket socket, ExecutorService executorService, ScheduledExecutorService scheduler, Bitfield bitfield, FilePieces filePieces) throws IOException {
        this.peer1ID = peer1ID;
        this.peer1 = peer1;
        this.peer2ID = peer2ID;
        this.socket = socket;
        this.executorService = executorService;
        this.inputStream = this.socket.getInputStream();
        this.streamOut = this.socket.getOutputStream();
        this.scheduler = scheduler;
        this.bitfield = bitfield;
        this.filePieces = filePieces;
        peerLogger = new LogHelper(peer1ID);
    }
    // Constructor for initializing the endpoint with a single peer ID
    public EndPoint(int peer1ID, Peer peer1, Socket socket, ExecutorService executorService, ScheduledExecutorService scheduler, Bitfield bitfield, FilePieces filePieces) throws IOException {
        this.peer1ID = peer1ID;
        this.peer1 = peer1;
        this.peer2ID = peer1ID;
        this.socket = socket;
        this.executorService = executorService;
        this.scheduler = scheduler;
        this.bitfield = bitfield;
        this.filePieces = filePieces;
        this.inputStream = this.socket.getInputStream();
        this.streamOut = this.socket.getOutputStream();
        peerLogger = new LogHelper(peer1ID);
    }
    // Getter method for the socket
    public Socket getSocket() {
        return this.socket;
    }
    // Method to send a handshake message
    private void sendHandshake() throws Exception {
        this.executorService.execute(new MessageSender(this.streamOut, Helper.getHandshakeMessage(this.peer1ID)));
    }
    // Method to receive a handshake message and validate it
    private void receiveHandshake() throws Exception {
        byte[] response = inputStream.readNBytes(Constants.HM_LENGTH);
        String responseHeader = new String(Arrays.copyOfRange(response, Constants.HM_HEADER_START, Constants.HM_HEADER_START + Constants.HM_HEADER_FIELD), StandardCharsets.UTF_8);
        int peer2ID = Helper.byteArrayToInt(Arrays.copyOfRange(response, Constants.HM_PEER_ID_START, Constants.HM_PEER_ID_START + Constants.HM_PEER_ID_FIELD));
        if (!responseHeader.equals(Constants.HM_HEADER)) {
            throw new IllegalArgumentException(String.format("Peer %d received invalid handshake msg header (%s) from %d", responseHeader, peer2ID));
        }
        if (this.handshakeInitiated) {
            if (peer2ID != this.peer2ID) {
                throw new IllegalArgumentException(String.format("Peer %d received invalid peer id (%d) in handshake response", peer1ID, peer2ID));
            }
            System.out.printf("\n%s: Peer %d makes connection to Peer %d", Helper.getCurrentTime(), this.peer1ID, this.peer2ID);     
            peerLogger.logInfo(Helper.getCurrentTime().toString() + ": Peer " + Integer.toString(this.peer1ID) + " makes a connection to Peer " + Integer.toString(this.peer2ID));
        } else {
            this.peer2ID = peer2ID;
            sendHandshake();
            System.out.printf("\n%s: Peer %d is connected from Peer %d", Helper.getCurrentTime(), this.peer1ID, this.peer2ID);   
            peerLogger.logInfo(Helper.getCurrentTime().toString() + ": Peer " + Integer.toString(this.peer1ID) + " is connected from Peer " + Integer.toString(this.peer2ID));  
        }
    }

    // Methods for sending, receiving, and processing different types of messages
    public void sendMessage(Constants.MessageType messageType) {
        try {
            this.executorService.execute(
                new MessageSender(this.socket.getOutputStream(), Helper.getMessage(messageType, new byte[0])));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void sendBitfield() {
        try {
            this.bitfield.readLock();
            byte[] bitfield = this.bitfield.getFieldForBit().toByteArray();
            this.executorService.execute(new MessageSender(this.streamOut, Helper.getMessage(Constants.MessageType.BITFIELD, bitfield)));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.bitfield.readUnlock();
        }
    }

    private void receiveBitfield(int messageLength) throws IOException {
        BitSet peerBitField = BitSet.valueOf(inputStream.readNBytes(messageLength));
        this.peerBitField = peerBitField;
        this.peer1.addOrUpdateBitfield(this.peer2ID, this.peerBitField);
        // Send interested msg
        if (this.bitfield.isInterested(this.peerBitField)) {
            sendInterested();
        }
    }
    private void sendInterested() {
        this.executorService.execute(new MessageSender(this.streamOut, Helper.getMessage(Constants.MessageType.INTERESTED, null)));
    }
    private void receiveInterested() {
        System.out.printf("\n%s: Peer %d received 'interested' msg from %d", Helper.getCurrentTime(), this.peer1ID, this.peer2ID);
        peerLogger.logInfo(Helper.getCurrentTime().toString() + ": Peer " + Integer.toString(this.peer1ID) + " received 'interested' msg from " + Integer.toString(this.peer2ID));
        this.peer1.addInterestedPeer(peer2ID);
    }
    private void sendNotInterested() {
        this.executorService.execute(new MessageSender(this.streamOut, Helper.getMessage(Constants.MessageType.NOT_INTERESTED, null)));
    }
    private void receiveNotInterested() {
        System.out.printf("\n%s: Peer %d received 'not interested' msg from %d", Helper.getCurrentTime(), this.peer1ID, this.peer2ID);
        peerLogger.logInfo(Helper.getCurrentTime().toString() + ": Peer " + Integer.toString(this.peer1ID) + " received 'not interested' msg from " + Integer.toString(this.peer2ID));
        this.peer1.removeInterestedPeer(peer2ID);
    }
    private void receiveChoke() {
        System.out.printf("\n%s: Peer %d choked by %d", Helper.getCurrentTime(), this.peer1ID, this.peer2ID);
        peerLogger.logInfo(Helper.getCurrentTime().toString() + ": Peer " + Integer.toString(this.peer1ID) + " choked by " + Integer.toString(this.peer2ID));
        this.choke = true;      
    }
    private void receiveUnchoke() {
        System.out.printf("\n%s: Peer %d unchoked by %d", Helper.getCurrentTime(), this.peer1ID, this.peer2ID);        
        peerLogger.logInfo(Helper.getCurrentTime().toString() + ": Peer " + Integer.toString(this.peer1ID) + " unchoked by " + Integer.toString(this.peer2ID));
        this.choke = false;
        sendRequest();
    }
    private void broadcastHave(int pieceIndex) throws IOException {
        for (EndPoint endPoint : this.peer1.getPeerEndPoints().values()) {
            this.executorService.execute(new MessageSender(endPoint.streamOut, Helper.getMessage(Constants.MessageType.HAVE, Helper.intToByteArray(pieceIndex))));
        }
    }
    private void receiveHave(int messageLength) throws IOException {
        int pieceIndex = Helper.byteArrayToInt(inputStream.readNBytes(messageLength));
        System.out.printf("\n%s: Peer %d received 'have' msg from %d", Helper.getCurrentTime(), this.peer1ID, this.peer2ID);     
        peerLogger.logInfo(Helper.getCurrentTime().toString() + ": Peer " + Integer.toString(this.peer1ID) + " received 'have' msg from " + Integer.toString(this.peer2ID));
        this.peerBitField.set(pieceIndex);
        if(this.bitfield.isInterested(this.peerBitField)) {
            this.executorService.execute(new MessageSender(this.streamOut, Helper.getMessage(Constants.MessageType.INTERESTED, null)));
        }
    }
    private void broadcastCompleted() throws IOException {
        for (EndPoint endPoint : this.peer1.getPeerEndPoints().values()) {
            executorService.execute(new MessageSender(endPoint.streamOut, Helper.getMessage(Constants.MessageType.COMPLETED, null)));
        }if (peer1.allPeersComplete()) {
            this.filePieces.deletePiecesDirectory();
            this.executorService.shutdownNow();
            this.scheduler.shutdown();
            peer1.getPeerServer().getServerSocket().close();
            peer1.closeSocket(peer2ID);
        }
    }
    private void sendRequest() {
    if (!this.choke) {
        int nextInterestedPieceIndex = bitfield.getNxtInterestedPieceIdx(peerBitField);
        if (nextInterestedPieceIndex != -1) {
            bitfield.addToReqPieces(nextInterestedPieceIndex);
            executorService.execute(new MessageSender(this.streamOut, Helper.getMessage(Constants.MessageType.REQUEST, Helper.intToByteArray(nextInterestedPieceIndex))));
        } else {
            sendNotInterested();
        }}
    }
    private void receiveRequest(int messageLength) throws IOException {
        if (this.peer1.isUnchoked(this.peer2ID)) {
            int pieceIndex = Helper.byteArrayToInt(inputStream.readNBytes(messageLength));
            byte[] pieceByteDataArray = this.filePieces.getFilePiece(pieceIndex);
            byte[] pieceMessage = Helper.getPieceMessage(pieceIndex, pieceByteDataArray);
            executorService.execute(new MessageSender(this.streamOut, pieceMessage));
        }
    }
    private void receivePiece(int messageLength) throws IOException {
        int pieceIndex = Helper.byteArrayToInt(inputStream.readNBytes(Constants.PM_PIECE_IDX_FIELD));
        byte[] pieceByteArray = inputStream.readNBytes(messageLength - Constants.PM_PIECE_IDX_FIELD);
        System.out.printf("\n%s: Peer %d downloaded the piece %d from %d", Helper.getCurrentTime(), this.peer1ID, pieceIndex, this.peer2ID);       
        peerLogger.logInfo(Helper.getCurrentTime().toString() + ": Peer " + Integer.toString(this.peer1ID) + " downloaded the piece " + pieceIndex + " from " + Integer.toString(this.peer2ID));
        this.filePieces.saveFilePiece(pieceIndex, pieceByteArray);
        bitfield.setReceivedPieceIdx(pieceIndex);
        peer1.incrementDownloadRate(this.peer2ID);
        broadcastHave(pieceIndex);
        if (!bitfield.isInterested(peerBitField)) {
            sendNotInterested();
        }if (bitfield.receivedAllPieces()) {
            this.filePieces.combinePiecesToFile();
            System.out.printf("\n%s: Peer %d downloaded the complete file", Helper.getCurrentTime(), this.peer1ID); 
            peerLogger.logInfo(Helper.getCurrentTime().toString() + ": Peer " + Integer.toString(this.peer1ID) + " downloaded the complete file");
        broadcastCompleted();
        } else {
            sendRequest();
        }
    }
    private void receiveCompleted() throws IOException {
    this.peer1.addCompletedPeer(peer2ID);
    if (peer1.allPeersComplete()) {
        this.filePieces.deletePiecesDirectory();
        this.executorService.shutdownNow();
        this.scheduler.shutdown();
        peer1.getPeerServer().getServerSocket().close();
        peer1.closeSocket(peer2ID);
    }
    }

    // The main run method for the Runnable interface
    @Override
    public void run() {
        if (this.peer1ID != this.peer2ID) {
            try {
                sendHandshake();
                this.handshakeInitiated = true;
            } catch (Exception e) {
                e.printStackTrace();
            }}
        try {
            receiveHandshake();
        } catch (Exception e) {
            e.printStackTrace();
        }
        sendBitfield();
        peer1.addPeerEndPoint(peer2ID, this);
        try {
            while (true) {      
                byte[] messageHeaders = inputStream.readNBytes(5);
                if (messageHeaders.length > 0) {
                    int messageLength = Helper.byteArrayToInt(Arrays.copyOfRange(messageHeaders, Constants.AM_MESSAGE_LENGTH_START, Constants.AM_MESSAGE_LENGTH_START + Constants.AM_MESSAGE_LENGTH_FIELD));
                    Constants.MessageType messageType = Constants.MessageType.getByValue((int) messageHeaders[Constants.AM_MESSAGE_TYPE_START]);
                    if (messageType != null) {
                        switch (messageType) {
                            case CHOKE:receiveChoke(); break;
                            case UNCHOKE:receiveUnchoke(); break;
                            case INTERESTED: receiveInterested(); break;
                            case NOT_INTERESTED: receiveNotInterested(); break;
                            case HAVE: receiveHave(messageLength); break;
                            case BITFIELD: receiveBitfield(messageLength); break;
                            case REQUEST:receiveRequest(messageLength); break;
                            case PIECE: receivePiece(messageLength); break;
                            case COMPLETED: receiveCompleted(); break;
                        }}}}} catch (Exception e) {
    }}}