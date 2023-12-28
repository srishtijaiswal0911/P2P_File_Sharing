import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.time.LocalDateTime;

public class Helper {
    // Convert an integer to a byte array
    public static byte[] intToByteArray(int num) {
        return ByteBuffer.allocate(4).putInt(num).array();
    }

    // Convert a byte array to an integer
    public static int byteArrayToInt(byte[] byteArr) {
        return ByteBuffer.wrap(byteArr).getInt();
    }

    // Get the current date and time in the format "dd/MM/yyyy HH:mm:ss"
    public static String getCurrentTime() {
        return DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(LocalDateTime.now());
    }

    // Merge two byte arrays into one, starting from a specific index
    public static int mergeArray1AndArray2(byte[] array1, byte[] array2, int startIdx) {
        for (byte byteData : array2) {
            array1[startIdx++] = byteData;
        }return startIdx;
    }

    // Generate a handshake message based on a given peer ID
    public static byte[] getHandshakeMessage(int id) {
        byte[] msg = new byte[Constants.HM_HEADER_FIELD + Constants.HM_ZERO_BITS_FIELD + Constants.HM_PEER_ID_FIELD];
        int counter = mergeArray1AndArray2(msg, Constants.HM_HEADER.getBytes(), 0);
        for (int i=0; i<10; i++) {
            msg[counter++] = 0;
        }
        counter = mergeArray1AndArray2(msg, intToByteArray(id), counter);
        return msg;
    }

    // Generate a message of a specific type with an optional payload
    public static byte[] getMessage(Constants.MessageType messageType, byte[] messagePayload) {
        int messageLength = messagePayload != null ? messagePayload.length : 0;
        byte[] msg = new byte[Constants.AM_MESSAGE_LENGTH_FIELD + Constants.AM_MESSAGE_TYPE_FIELD + messageLength];
        int counter = mergeArray1AndArray2(msg, intToByteArray(messageLength), 0);
        msg[counter++] = (byte) messageType.getValue();
        if (messageLength > 0) {
            mergeArray1AndArray2(msg, messagePayload, counter);
        }
        return msg;
    }

    // Generate the payload for a piece message
    private static byte[] getPieceMessagePayload(int pieceIdx, byte[] pieceByteDataArray) {
        byte[] pieceMessagePayload = new byte[Constants.PM_PIECE_IDX_FIELD + pieceByteDataArray.length];
        int counter = mergeArray1AndArray2(pieceMessagePayload, intToByteArray(pieceIdx), 0);
        mergeArray1AndArray2(pieceMessagePayload, pieceByteDataArray, counter);
        return pieceMessagePayload;
    }

    // Generate a piece message based on a given piece index and byte data array
    public static byte[] getPieceMessage(int pieceIdx, byte[] pieceByteDataArray) {
        byte[] pieceMessagePayload = getPieceMessagePayload(pieceIdx, pieceByteDataArray);
        byte[] pieceMessage = getMessage(Constants.MessageType.PIECE, pieceMessagePayload);
        return pieceMessage;
    }
    
    // Delete a directory and its contents
    public static void deleteDirectory(String path) throws IOException
    {
        Files
        .walk(Paths.get(path))
        .sorted(Comparator.reverseOrder())
        .map(Path::toFile)
        .forEach(File::delete);
    }
}