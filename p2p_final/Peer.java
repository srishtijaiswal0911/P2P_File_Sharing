import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Set;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

// The Peer class represents a peer in the P2P network.
public class Peer {
    // Various attributes of the peer
    private final int id;
    private final CommonCfg commonConfigurationOfVariables;
    private final PeerInformationConfiguration peerInformationConfiguration;
    private final ExecutorService executorService;
    private final ScheduledExecutorService scheduler;
    private final FilePieces filePieces;
    private final Bitfield bitfield;
    private final Map<Integer, BitSet> peerBitfields = new ConcurrentHashMap<>();
    private final PeerServer peerServer;
    private final PeerClient peerClient;
    private final Map<Integer, EndPoint> peerEndPoints = new ConcurrentHashMap<>();
    private final Map<Integer, Integer> drm = new ConcurrentHashMap<>();
    private final Set<Integer> interestedPeers = ConcurrentHashMap.newKeySet();
    private final Set<Integer> preferredNeighbors = ConcurrentHashMap.newKeySet();
    private final AtomicInteger optimisticNeighbor = new AtomicInteger(-1);
    private final Set<Integer> completedPeers = new HashSet<>();

    // Constructor to initialize the Peer object.
    public Peer(int id, CommonCfg commoncfg, PeerInformationConfiguration peerInformationConfiguration, ExecutorService executorService,
            ScheduledExecutorService scheduler) {
        this.id = id;
        this.commonConfigurationOfVariables = commoncfg;
        this.peerInformationConfiguration = peerInformationConfiguration;
        this.executorService = executorService;
        this.scheduler = scheduler;
        this.filePieces = new FilePieces(this.id, this.commonConfigurationOfVariables);
        BitSet bitfield = new BitSet(this.commonConfigurationOfVariables.getNumberOfPieces());
        if (this.peerInformationConfiguration.getPeer(this.id).getHasFile()) {
            bitfield.set(0, commoncfg.getNumberOfPieces());
            this.filePieces.splitFileintoPieces();
        }
        this.bitfield = new Bitfield(bitfield, commoncfg);
        this.peerServer = new PeerServer();
        this.executorService.execute(this.peerServer);
        this.peerClient = new PeerClient();
        this.executorService.execute(this.peerClient);
        for (PeerInformationConfiguration.PeerInfo peerInfo : this.peerInformationConfiguration.getPeers().values()) {
            if(peerInfo.getHasFile()) {
                this.completedPeers.add(peerInfo.getId());
            }}
    }

    // Various getter and setter methods for the Peer class attributes.
    // Returns the Bitfield
    public Bitfield getFieldForBit() {
        return this.bitfield;
    }

    // Returns the Map of peer bitfields
    public Map<Integer, BitSet> getPeerBitfields() {
        return this.peerBitfields;
    }

    // Returns the PeerServer object
    public Peer.PeerServer getPeerServer() {
        return this.peerServer;
    }

    // Returns the Map of peer EndPoints
    public Map<Integer, EndPoint> getPeerEndPoints() {
        return this.peerEndPoints;
    }

    // Returns the EndPoint for a specific peerId
    public EndPoint getPeerEndPoint(int peerId) {
        return peerEndPoints.get(peerId);
    }

    // Adds a peer EndPoint along with download rate initialization
    public void addPeerEndPoint(int peerId, EndPoint endPoint) {
        this.peerEndPoints.put(peerId, endPoint);
        this.drm.put(peerId, 0);
    }

    // Adds or updates the bitfield for a peer
    public void addOrUpdateBitfield(int peerId, BitSet bitfield) {
        this.peerBitfields.put(peerId, bitfield);
    }

    // Returns the Set of interested peers
    public Set<Integer> getInterestedPeers() {
        return this.interestedPeers;
    }

    // Adds a peer to the set of interested peers
    public void addInterestedPeer(int peerId) {
        this.interestedPeers.add(peerId);
    }

    // Removes a peer from the set of interested peers
    public void removeInterestedPeer(int peerId) {
        this.interestedPeers.remove(peerId);
    }

    // Increments the download rate for a peer
    public void incrementDownloadRate(int peerId) {
        this.drm.put(peerId, this.drm.get(peerId) + 1);
    }

    // Returns the set of preferred neighbors
    public Set<Integer> getPreferredNeighbors() {
        return this.preferredNeighbors;
    }
    // Returns the optimistic neighbor as an AtomicInteger
    public AtomicInteger getOptimisticNeighbor() {
        return this.optimisticNeighbor;
    }

    // Sets the optimistic neighbor
    public void setOptimisticNeighbor(int optimisticNeighbor) {
        this.optimisticNeighbor.set(optimisticNeighbor);
    }

    // Returns a list of peers sorted by download rate
    public List<Integer> getPeersSortedByDownloadRate() {
        List<Map.Entry<Integer, Integer>> sortedDownloadRateMap = new ArrayList<>(drm.entrySet());
        sortedDownloadRateMap.sort(Map.Entry.comparingByValue());
        List<Integer> sortedPeers = new ArrayList<>();
        for(Map.Entry<Integer, Integer> entry : sortedDownloadRateMap) {
            sortedPeers.add(entry.getKey());
        }return sortedPeers;
    }

    // Reselects preferred neighbors based on download rate
    public void reselectNeighbours() {
        List<Integer> peersAccordingToDownloadRate = getPeersSortedByDownloadRate();
        this.preferredNeighbors.clear();
        for (int peerId : drm.keySet()){
            this.drm.put(peerId, 0);
        }
        int count = 0;
        int i = 0;
        while (count < commonConfigurationOfVariables.getNumberOfPreferredNeighbors() && i < this.interestedPeers.size()) {
            int currentPeer = peersAccordingToDownloadRate.get(i);
            if (this.interestedPeers.contains(currentPeer)) {
                this.preferredNeighbors.add(currentPeer);
                count++;}
            i++;}
    }

    // Checks if a peer is unchoked
    public boolean isUnchoked(int peerId) {
        return preferredNeighbors.contains(peerId) || optimisticNeighbor.get() == peerId;
    }

    // Returns the set of completed peers
    public Set<Integer> getCompletedPeers() {
        return this.completedPeers;
    }

    // Adds a completed peer
    public void addCompletedPeer(int peerId) {
        this.completedPeers.add(peerId);
    }

    // Checks if all peers are complete
    public boolean allPeersComplete() {
        Set<Integer> peerIdentifiers = this.peerInformationConfiguration.getPeers().keySet();
        if (bitfield.receivedAllPieces()) {
            peerIdentifiers.remove(id);
        }
        peerIdentifiers.removeAll(completedPeers);
        return peerIdentifiers.size() == 0;
    }

    // Closes the socket for a peer
    public void closeSocket(int peerId) throws IOException {
        peerEndPoints.get(peerId).getSocket().close();
    }

    // The PeerServer class represents the server-side functionality of a peer.
    public class PeerServer implements Runnable {
        ServerSocket serverSocket;
        // Constructor to initialize the PeerServer object.
        public PeerServer() {
            try {
                PeerInformationConfiguration.PeerInfo currentPeerInfo = Peer.this.peerInformationConfiguration.getPeer(id);
                String hostName = currentPeerInfo.getHostName();
                int port = currentPeerInfo.getPort();
                this.serverSocket = new ServerSocket(port, 50, InetAddress.getByName(hostName));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // Getter method for the server socket.
        public ServerSocket getServerSocket() {
            return this.serverSocket;
        }
        @Override
        public void run() {
            try {
                while (true) {
                    if (Thread.currentThread().isInterrupted()) {
                        return;
                    }
                    Socket socket = serverSocket.accept();
                    Peer.this.executorService.execute(new EndPoint(id, Peer.this, socket, Peer.this.executorService, Peer.this.scheduler, Peer.this.bitfield, Peer.this.filePieces));
                }
            } catch (Exception e) {e.printStackTrace();}}
    }

    // The PeerClient class represents the client-side functionality of a peer.
    public class PeerClient implements Runnable {
        @Override
        public void run() {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            for (PeerInformationConfiguration.PeerInfo peerInfo : Peer.this.peerInformationConfiguration.getPeers().values()) {
                if (peerInfo.getId() == id) {
                    break;
                }
                try {
                    Socket socket = null;
                    while (socket == null) {
                        socket = new Socket(peerInfo.getHostName(), peerInfo.getPort());
                    }
                    Peer.this.executorService.execute(new EndPoint(id, Peer.this, peerInfo.getId(), socket, Peer.this.executorService, Peer.this.scheduler, Peer.this.bitfield, Peer.this.filePieces));
                } catch (Exception e) {e.printStackTrace();}} }
    }
}