import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReadFile { //Class responsible for reading contents of a file
    public ReadFile() {}
    public List<String> read(String pathForFile) //reads the file and returns as a list of strings
	{
		List<String> contentForFile = new ArrayList<>(); 
		if(pathForFile != null && pathForFile.length() > 0)
		{
            String pathFile = Constants.WORKING_DIR + File.separator + pathForFile;
			try
			{
				FileReader fileReader = new FileReader(pathFile);
				BufferedReader bufferReader = new BufferedReader(fileReader);
				String currentLine = "";
				while((currentLine = bufferReader.readLine()) != null) {
					contentForFile.add(currentLine);	//Add each line to contentForFile
				}bufferReader.close();
			}catch(IOException e){e.printStackTrace();}}	
		return contentForFile;   // Return the contents of the file
	}
}