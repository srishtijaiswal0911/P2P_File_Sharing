import java.io.OutputStream;

// The MessageSender class is responsible for sending messages over an OutputStream.
public class MessageSender implements Runnable {
    // The OutputStream to which the message will be written.
    private final OutputStream streamOut;
    
    // The actual message to be sent, in byte array format.
    private final byte[] msg;

    // Constructor to initialize the OutputStream and the message.
    public MessageSender(OutputStream streamOut, byte[] msg) {
        this.streamOut = streamOut;
        this.msg = msg;
    }

    // The run method of the Runnable interface, which will be executed when the thread is started.
    @Override
    public void run() {
        try {
            // Check if the current thread has been interrupted. If so, exit early.
            if (Thread.currentThread().isInterrupted())
                return;
            
            // Synchronize on the OutputStream to ensure thread safety when writing to the stream.
            synchronized(streamOut) {
                // Write the message to the OutputStream.
                streamOut.write(this.msg);
            }
        } catch (Exception e) {
            // Print any exceptions that occur during the write operation.
            e.printStackTrace();
        }
    }
}
