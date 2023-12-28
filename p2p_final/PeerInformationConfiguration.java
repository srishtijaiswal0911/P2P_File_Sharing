import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

public class PeerInformationConfiguration {  //responsible for parsing and managing peerInfo.cfg
	public class PeerInfo { //represents individual peer information
		private int id;
		private String hostName;
		private int port;
		private boolean containsFile;
		public PeerInfo(int id, String hostName, int port, boolean containsFile) {
			this.id = id;
			this.hostName = hostName;
			this.port = port;
			this.containsFile = containsFile;
		}
		//Getters for peerInfo config details
		public int getId() {
			return id;
		}
		public String getHostName() {
			return hostName;
		}
		public int getPort() {
			return port;
		}
		public boolean getHasFile() {
			return containsFile;
		}
		@Override
		public String toString() {  // Convert the PeerInfo object to a human-readable string
			return "id: " + id + "\n" +
				"hostname: " + hostName + "\n" +
				"port: " + port + "\n" +
				"containsFile: " + containsFile + "\n";
		}
	}
    private Map<Integer, PeerInfo> peersInfo = new LinkedHashMap<>();
    public void parse(List<String> lines) { // Parse the configuration lines to populate peer information
		for(String line : lines) {
			String[] lineSplit = line.split(" ");
			int id = Integer.parseInt(lineSplit[0]);
			String hostName = lineSplit[1];
			int port = Integer.parseInt(lineSplit[2]);
			boolean containsFile = Integer.parseInt(lineSplit[3]) == 1;
			this.peersInfo.put(id, new PeerInfo(id, hostName, port, containsFile));
		}
	}
    public Map<Integer, PeerInfo> getPeers() { // Get the map of all peer information
        return this.peersInfo;
    }
	public PeerInfo getPeer(int id) { // Get the peer information for a specific peer by its ID
        return this.peersInfo.get(id);
    }
	@Override
    public String toString() {
		StringBuilder str = new StringBuilder();  // Convert the PeerInformationConfiguration object to a string by joining individual PeerInfo objects
		for(PeerInfo peerInfo: this.peersInfo.values()) {
            str.append(peerInfo);
        }
		return str.toString();
	}
}