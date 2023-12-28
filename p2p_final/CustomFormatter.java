import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.Date;
import java.text.SimpleDateFormat;

public class CustomFormatter extends Formatter {

    private static final String FORMAT = "dd/MM/yyyy HH:mm:ss";
    private final SimpleDateFormat dateFormat;

    public CustomFormatter() {
        this.dateFormat = new SimpleDateFormat(FORMAT);
    }

    @Override
    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();

        // Append the log level
        sb.append(record.getLevel()).append(": ");

        // Append the date and time
        Date recordDate = new Date(record.getMillis());
        sb.append(dateFormat.format(recordDate)).append(": ");

        // Append the log message
        sb.append(formatMessage(record)).append("\n");

        return sb.toString();
    }
}
