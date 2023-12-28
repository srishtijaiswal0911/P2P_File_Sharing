import java.util.concurrent.Delayed;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

public class PieceIndex implements Delayed { // This class represents a Delayed object for managing piece indexes.
    private final int index;
    private final LocalDateTime insertTime; // Time when piece index was inserted
    public PieceIndex(int index) {
        this.index = index;
        this.insertTime = LocalDateTime.now().plusSeconds(30); // Set the expiration time to 30 seconds from the current time
    }
    public int getIndex() {return index;} // Get the piece index.

    @Override
    public long getDelay(TimeUnit unit) { //calculate the remaining delay until expiration
        LocalDateTime now = LocalDateTime.now();
        long diff = now.until(insertTime, ChronoUnit.MILLIS);
        return unit.convert(diff, TimeUnit.MILLISECONDS);
    }
    @Override
    public int compareTo(Delayed o) {   // method for comparing delay times
        long result = this.getDelay(TimeUnit.NANOSECONDS) - o.getDelay(TimeUnit.NANOSECONDS);
        if (result < 0) {
            return -1;
        } else if (result > 0) {
            return 1;
        }return 0;
    }
}