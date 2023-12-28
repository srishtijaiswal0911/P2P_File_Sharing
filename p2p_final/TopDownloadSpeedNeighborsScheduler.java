import java.util.Map;

public class TopDownloadSpeedNeighborsScheduler implements Runnable { // This class is for selecting preferred neighbors in a p2p network.
    int id;
    private Peer peer;
    LogHelper peerLogger;

    public TopDownloadSpeedNeighborsScheduler(int id, Peer peer) {
        this.id = id;
        this.peer = peer;
        this.peerLogger = new LogHelper(id);
    }
    @Override
    public void run(){
        if (Thread.currentThread().isInterrupted()) // If the thread is interrupted, exit the run method.
            return;
        this.peer.reselectNeighbours();  // Reselect preferred neighbors based on criteria.
        System.out.printf("\n%s: Peer %d has the preferred neighbors %d", Helper.getCurrentTime(), this.id, this.peer.getPreferredNeighbors());
        //LOGGER.info("{}: Peer {} has the preferred neighbors {}", Helper.getCurrentTime(), this.id, this.peer.getPreferredNeighbors());
        peerLogger.logInfo(Helper.getCurrentTime().toString() + ": Peer" + Integer.toString(this.id) + "has the preferred neighbors" + (this.peer.getPreferredNeighbors().toString()));
        this.peer.getPreferredNeighbors();  // Get the preferred neighbors and their endpoints.
        for (Map.Entry<Integer, EndPoint> entry : peer.getPeerEndPoints().entrySet()) { // Iterate through the endpoints of connected peers and send messages accordingly.
            Integer peerId = entry.getKey();
            EndPoint endPoint = entry.getValue();
            if (peer.getPreferredNeighbors().contains(peerId)) {
                endPoint.sendMessage(Constants.MessageType.UNCHOKE);  // Send "UNCHOKE" message to preferred neighbors.
            } else if (peer.getOptimisticNeighbor().get() == peerId) {  // Continue if the peer is an optimistic neighbor.
                continue;
            } else {endPoint.sendMessage(Constants.MessageType.CHOKE);} // Send "CHOKE" message to other peers.
        }}
}