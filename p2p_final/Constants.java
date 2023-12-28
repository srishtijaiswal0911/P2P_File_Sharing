public class Constants {
    // Configuration file names
    public static final String COMMON_CFG_FILENAME = "Common.cfg";  // Filename for common configurations
    public static final String PEER_INFO_CFG_FILENAME = "PeerInfo.cfg";  // Filename for peer information configurations

    // Directory information
    public static final String WORKING_DIR = System.getProperty("user.dir");  // Current working directory of the Java application

    // Message protocol constants defining positions and lengths for various fields
    public static final Integer AM_MESSAGE_LENGTH_START = 0;  // Start position for message length
    public static final Integer AM_MESSAGE_LENGTH_FIELD = 4;  // Length of the message length field
    public static final Integer AM_MESSAGE_TYPE_START = 4;  // Start position for message type
    public static final Integer AM_MESSAGE_TYPE_FIELD = 1;  // Length of the message type field
    public static final Integer PM_PIECE_IDX_START = 0;  // Start position for piece index in the message
    public static final Integer PM_PIECE_IDX_FIELD = 4;  // Length of the piece index field

    // Handshake message protocol constants
    public static final Integer HM_LENGTH = 32;  // Total length of the handshake message
    public static final String HM_HEADER = "P2PFILESHARINGPROJ";  // Header value for the handshake message
    public static final Integer HM_HEADER_START = 0;  // Start position for handshake message header
    public static final Integer HM_HEADER_FIELD = 18;  // Length of the handshake message header field
    public static final Integer HM_ZERO_BITS_START = 18;  // Start position for zero bits in handshake message
    public static final Integer HM_ZERO_BITS_FIELD = 10;  // Length of the zero bits field in handshake message
    public static final Integer HM_PEER_ID_START = 28;   // Start position for peer ID in handshake message
    public static final Integer HM_PEER_ID_FIELD = 4;  // Length of the peer ID field in handshake message

    // Enumeration for different types of messages in the application
    public static enum MessageType {CHOKE(0), UNCHOKE(1), INTERESTED(2), NOT_INTERESTED(3),  HAVE(4), BITFIELD(5), REQUEST(6), PIECE(7), COMPLETED(8);
        private final int value;

        // Constructor for the MessageType enumeration
		private MessageType(int value) {
			this.value = value;
		}
        // Method to get the integer value of a message type
		public int getValue() {
			return this.value;
		}
        // Method to get a message type by its integer value
        public static MessageType getByValue(int value) {
            for (MessageType messageType: MessageType.values()) {
                if (messageType.getValue() == value) {
                    return messageType;
                }}
            return null;
        }}
}