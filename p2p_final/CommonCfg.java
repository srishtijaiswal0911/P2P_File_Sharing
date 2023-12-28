import java.util.List;

public class CommonCfg
{
	int numPreferredNeighbors; // Number of preferred neighbors
	int unchokingInterval;  // Interval for regular unchoking
    int optimisticUnchokingInterval; // Interval for regular unchoking
    String fileName; // Name of the file being shared
    int fileSize; // Size of the file in bytes
    int pieceSize; // Size of each piece/chunk in bytes
	int numPieces; // Total number of pieces/chunks

    // Default constructor
    public CommonCfg() {}

    // Parse method to initialize configuration variables from a list of strings
    public void parse(List<String> lines) {
		if(lines != null && lines.size() == 6) {
			this.numPreferredNeighbors = Integer.parseInt(lines.get(0).split(" ")[1]);
			this.unchokingInterval = Integer.parseInt(lines.get(1).split(" ")[1]);
			this.optimisticUnchokingInterval = Integer.parseInt(lines.get(2).split(" ")[1]);
			this.fileName = lines.get(3).split(" ")[1];
			this.fileSize = Integer.parseInt(lines.get(4).split(" ")[1]);
			this.pieceSize = Integer.parseInt(lines.get(5).split(" ")[1]);
            
            // Calculating the total number of pieces/chunks
			double fileSizeInDouble = (double) fileSize;
			double pieceSizeInDouble = (double) pieceSize;
			this.numPieces = (int) Math.ceil(fileSizeInDouble / pieceSizeInDouble);
		}
	}

    // Getter methods for configuration variables
	public int getNumberOfPreferredNeighbors() {
        return this.numPreferredNeighbors;
    }
    public int getUnchokingInterval() {
        return this.unchokingInterval;
    }
    public int getOptimisticUnchokingInterval() {
        return this.optimisticUnchokingInterval;
    }
    public String getFileName() {
        return this.fileName;
    }
    public int getFileSize() {
        return this.fileSize;
    }
    public int getPieceSize() {
        return this.pieceSize;
    }
	public int getNumberOfPieces() {
		return this.numPieces;
	}

    // Overriding the toString method to provide a string representation of the configuration
    @Override
    public String toString()
    {
        return "number of PreferredNeighbors : " + this.numPreferredNeighbors + "\n" + 
            "unchokingInterval : " + this.unchokingInterval + "\n" + 
            "optimisticUnchokingInterval : " + this.optimisticUnchokingInterval + "\n" + 
            "fileName : " + this.fileName + "\n" + 
            "fileSize : " + this.fileSize + "\n" + 
            "pieceSize : " + this.pieceSize + "\n" + 
            "numberOfChunks : " + this.numPieces + "\n" ;
    }
}