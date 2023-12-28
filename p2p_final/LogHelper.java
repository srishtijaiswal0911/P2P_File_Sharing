import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LogHelper {

    private Logger logger;

    public LogHelper(int peerName) {
        // Create a Logger instance for the peer
        logger = Logger.getLogger(Integer.toString(peerName));
        logger.setUseParentHandlers(false); // Do not use the parent handler to avoid console output

        try {
            // Define the file name for the logger
            String dirName = "peer_" + Integer.toString(peerName);
            File directory = new File(dirName);
            if (!directory.exists()) {
                directory.mkdir(); // mkdirs() can be used if you need to create multiple nested directories
            }

            FileHandler fileHandler = new FileHandler(dirName + "/" + peerName + ".log", true);
            //FileHandler fileHandler = new FileHandler(peerName + ".log", true);
            fileHandler.setFormatter(new CustomFormatter());

            // Add the FileHandler to the Logger
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            System.err.println("Could not setup logger for peer: " + peerName);
            e.printStackTrace();
        }
    }

    public void logInfo(String msg) {
        logger.info(msg);
    }

    public void logWarning(String msg) {
        logger.warning(msg);
    }

    public void logSevere(String msg) {
        logger.severe(msg);
    }
}
