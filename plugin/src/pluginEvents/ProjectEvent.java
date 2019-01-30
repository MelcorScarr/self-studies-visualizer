package pluginEvents;

public class ProjectEvent extends Event {
    public static final String MESSAGE_OPENED = "opened";
    public static final String MESSAGE_CLOSED = "closed";

    private String name;

    public ProjectEvent(String name, String message) {
        super();
        this.setEventType(EventType.PROJECT);
        this.setMessage(message);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
