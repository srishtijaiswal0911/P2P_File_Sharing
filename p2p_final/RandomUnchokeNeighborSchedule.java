import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomUnchokeNeighborSchedule implements Runnable{ // Class for selecting optimistically unchocked neighbor
    int id;
    Peer peer;
    PeerInformationConfiguration peerInformationConfiguration;
    Random random;
    LogHelper peerLogger;

    public RandomUnchokeNeighborSchedule(int id, Peer peer, PeerInformationConfiguration peerInformationConfiguration) { //Constructor
        this.id = id;
        this.peer = peer;
        this.peerInformationConfiguration = peerInformationConfiguration;
        this.peerLogger = new LogHelper(this.id);
        random = new Random();
    }
    @Override
    public void run(){
        if (Thread.currentThread().isInterrupted()) { // If the thread is interrupted, exit the run method
            return;
        }
        List<Integer> chokedPeers = new ArrayList<>();
        for (int id : this.peerInformationConfiguration.getPeers().keySet()){ //Iterate through every peer to find choked and interested neighbors
            if ((!peer.getPreferredNeighbors().contains(id)) && (peer.getInterestedPeers().contains(id)))
                chokedPeers.add(id); // Add choked and interested peers to the list.
        }
        try {
            if(chokedPeers.size() > 0) {
                int optimisticNeighbor = chokedPeers.get(random.nextInt(chokedPeers.size()));
                peer.setOptimisticNeighbor(optimisticNeighbor);  // Set the selected peer as the optimistic neighbor for the current peer.
                //LOGGER.info("{}: Peer {} has the optimistically unchoked neighbor {}", Helper.getCurrentTime(), this.id, optimisticNeighbor);
                peerLogger.logInfo(Helper.getCurrentTime().toString() + ": Peer" + Integer.toString(this.id) + "has the randomly unchoked neighbor" + Integer.toString(optimisticNeighbor));       
                System.out.printf("\n%s: Peer %d has the randomly unchoked neighbor %d", Helper.getCurrentTime(), this.id, optimisticNeighbor);
                EndPoint endPoint = peer.getPeerEndPoint(optimisticNeighbor);
                endPoint.sendMessage(Constants.MessageType.UNCHOKE); // Get the endpoint for the optimistic neighbor and send an "UNCHOKE" message.
            }
        } catch (Exception e) {e.printStackTrace();}} 
}