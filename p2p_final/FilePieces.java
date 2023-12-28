import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
public class FilePieces {
    // Class variables
    int id;  // ID of the peer
    CommonCfg commonConf;  // Common configuration object
    String fileName;  // Name of the file to be shared
    int fileSize;  // Size of the file
    int pieceSize;  // Size of each piece of the file
    int numPieces;  // Number of pieces the file is divided into
    String piecesDirectoryPath;  // Directory path to store the file pieces
    String pathForFile;  // Path to the original file

    // Constructor
    public FilePieces(int id, CommonCfg commonConfigurationOfVariables) {
        // Initializing class variables
        this.id = id;
        this.commonConf = commonConfigurationOfVariables;
        this.fileName = this.commonConf.getFileName();
        this.fileSize = this.commonConf.getFileSize();
        this.pieceSize = this.commonConf.getPieceSize();
        this.numPieces = this.commonConf.getNumberOfPieces();      
        this.piecesDirectoryPath = Paths.get(Constants.WORKING_DIR, String.format("peer_%d", this.id), "temp").toString();

        // Creating directory to store file pieces
        try {
            Files.createDirectories(Paths.get(this.piecesDirectoryPath));
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.pathForFile = Paths.get(Constants.WORKING_DIR, String.format("peer_%d", this.id), this.commonConf.getFileName()).toString();
    }

    // Method to split the file into pieces
    public void splitFileintoPieces() {
		try {
            FileInputStream fileInputStream = new FileInputStream(this.pathForFile);
            int pieceStart = 0;
            for (int pieceIdx = 0; pieceIdx < this.numPieces; pieceIdx++) {
                int newPieceStart = pieceStart + this.pieceSize;
                int pieceLength = this.pieceSize;
                if (this.fileSize < newPieceStart) {
                    newPieceStart = this.fileSize;
                    pieceLength = this.fileSize - pieceStart;
                }
                byte[] pieceByteArray = new byte[pieceLength];
                fileInputStream.read(pieceByteArray);
                String piecePath = Paths.get(this.piecesDirectoryPath, String.format("%s_%d", this.fileName, pieceIdx)).toString();
                FileOutputStream fileOutputStream = new FileOutputStream(piecePath);
                fileOutputStream.write(pieceByteArray);
                fileOutputStream.close();
                pieceStart = newPieceStart;
            }
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to get a specific piece of the file
    public byte[] getFilePiece(int pieceIdx) throws IOException {
        String piecePath = Paths.get(this.piecesDirectoryPath, String.format("%s_%d", this.fileName, pieceIdx)).toString();
        FileInputStream fileInputStream = new FileInputStream(piecePath);
        int pieceLength = (int) fileInputStream.getChannel().size();
        byte[] pieceByteArray = new byte[pieceLength];
        fileInputStream.read(pieceByteArray);
        fileInputStream.close();
        return pieceByteArray;
    }

    // Method to save a specific piece of the file
    public void saveFilePiece(int pieceIdx, byte[] pieceByteArray) throws IOException {
        String piecePath = Paths.get(this.piecesDirectoryPath, String.format("%s_%d", this.fileName, pieceIdx)).toString();
        FileOutputStream fileOutputStream = new FileOutputStream(piecePath);
        fileOutputStream.write(pieceByteArray);
        fileOutputStream.close();
    }

    // Method to combine all the pieces back into the original file
    public void combinePiecesToFile() throws IOException {
		FileOutputStream fileOutputStream = new FileOutputStream(this.pathForFile);
		File[] splitFiles = new File[numPieces];
		for(int pieceIdx = 0; pieceIdx < numPieces; pieceIdx++) {
            String piecePath = Paths.get(this.piecesDirectoryPath, String.format("%s_%d", this.fileName, pieceIdx)).toString();
			splitFiles[pieceIdx] = new File(piecePath);
		}
		for(int pieceIdx = 0; pieceIdx < numPieces; pieceIdx++) {
			FileInputStream fileInputStream = new FileInputStream(splitFiles[pieceIdx]);
			int chunkFileLength = (int)splitFiles[pieceIdx].length();
			byte[] readChunkFile = new byte[chunkFileLength];
			fileInputStream.read(readChunkFile);
			fileOutputStream.write(readChunkFile);
			fileInputStream.close();
		}
		fileOutputStream.close();
	}

    // Method to delete the directory containing all the file pieces
    public void deletePiecesDirectory() throws IOException {
        try {
            if (Files.exists(Paths.get(piecesDirectoryPath))) {
                Helper.deleteDirectory(piecesDirectoryPath);
                Files.delete(Paths.get(piecesDirectoryPath));
            }
        } catch (Exception e) {
        }}}