import java.util.Objects;
import java.util.concurrent.DelayQueue;
import java.util.BitSet;
import java.util.Map;;

public class RequestedPiecesScheduler implements Runnable{   // Class for Managing requested pieces in the P2P network
    private Peer peer; // Associated peer
    private final Bitfield bitfield; //Bitfield representing all available pieces
    public RequestedPiecesScheduler(Peer peer) {
        this.peer = peer; 
        this.bitfield = peer.getFieldForBit(); // Get bitfield from peer
    }
    @Override
    public void run() {
        try{
            if (Thread.currentThread().isInterrupted()) //return if the current thread is interrupted
                return;
            DelayQueue<PieceIndex> piecesRequested = bitfield.getDelayQueue();  //Get queue of requested pieces from bitfield
            PieceIndex expiredPieceIndex = piecesRequested.poll(); //Retrieve and process expired piece indexes from the queue
            while(Objects.nonNull(expiredPieceIndex)){
                bitfield.removeTimedOutPieceIndex(expiredPieceIndex.getIndex()); //remove timed out pieces
                for (Map.Entry<Integer, BitSet> entry : peer.getPeerBitfields().entrySet()){
                    BitSet bitset = entry.getValue();
                    if (bitset.get(expiredPieceIndex.getIndex())){    // checking if a peer has the requested piece
                        EndPoint ep = peer.getPeerEndPoint(entry.getKey());
                        ep.sendMessage(Constants.MessageType.INTERESTED);  // send 'interested' message to the peer
                    }
                }expiredPieceIndex = piecesRequested.poll();  //Get next expired piece from the queue
            }
        } catch (Exception e){e.printStackTrace();}
    }
}