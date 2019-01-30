package pluginEvents;

public class IDEEvent extends Event {
    public static final String MESSAGE_OPENED = "opened";
    public static final String MESSAGE_CLOSED = "closed";

    public IDEEvent(String message) {
        super();
        this.setEventType(EventType.IDE);
        this.setMessage(message);
    }
}
