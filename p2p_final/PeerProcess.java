import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutorService;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class PeerProcess {  //main entry point for the p2p application
    //Method to create the directory structure for a peer and copy the shared file if required
    private static void createRequiredDirStructure(PeerInformationConfiguration.PeerInfo peerInfo, String workingDir, String inputFileName) throws Exception { 
        try {
            String peerDirPath = Paths.get(Constants.WORKING_DIR, String.format("peer_%d", peerInfo.getId())).toString();
            Files.createDirectories(Paths.get(peerDirPath));
        } catch (Exception e) {e.printStackTrace();}
        
        if(peerInfo.getHasFile()) {
            try {
                Path source = Paths.get(Constants.WORKING_DIR, inputFileName);
                Path target = Paths.get(Constants.WORKING_DIR, String.format("peer_%d", peerInfo.getId()), inputFileName);
                Files.copy(source, target);
            } catch (Exception e) {e.printStackTrace();}
        }
    }
    public static void main(String[] args) { //main method for the PeerProcess application.
        int id = Integer.parseInt(args[0]);  
        ReadFile readFileObject = new ReadFile();  // Read and parse the common configuration file
        List<String> commonCfgLines = readFileObject.read(Constants.COMMON_CFG_FILENAME);
        System.out.println(commonCfgLines);

        System.out.flush();
        CommonCfg commonConfigurationOfVariables = new CommonCfg();
        commonConfigurationOfVariables.parse(commonCfgLines);
        System.out.println(commonConfigurationOfVariables);
        System.out.flush();  
        List<String> peerInfoLines = readFileObject.read(Constants.PEER_INFO_CFG_FILENAME); // Read and parse the peer information configuration file
        System.out.println(peerInfoLines);
        System.out.flush();
        PeerInformationConfiguration peerInformationConfiguration = new PeerInformationConfiguration();
        peerInformationConfiguration.parse(peerInfoLines);
        System.out.println(peerInformationConfiguration);
        System.out.flush();
        try {  // Create the required directory structure for the peer, including copying the shared file
            createRequiredDirStructure(peerInformationConfiguration.getPeer(id), Constants.WORKING_DIR, commonConfigurationOfVariables.getFileName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Create thread pools for execution and scheduling
        ExecutorService executorService = Executors.newFixedThreadPool(8);  
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(8);
        // Create a Peer object to represent the current peer and initialize the schedulers
        Peer peer = new Peer(id, commonConfigurationOfVariables, peerInformationConfiguration, executorService, scheduler);
        scheduler.scheduleAtFixedRate(new TopDownloadSpeedNeighborsScheduler(id, peer), 0L, commonConfigurationOfVariables.getUnchokingInterval(), TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(new RandomUnchokeNeighborSchedule(id, peer, peerInformationConfiguration), 0L, commonConfigurationOfVariables.getOptimisticUnchokingInterval(), TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(new RequestedPiecesScheduler(peer), 0L, 30, TimeUnit.SECONDS);
    }
}