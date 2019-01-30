package pluginEvents;

public class EditorEvent extends Event {
    public static final String EDITOR_FILE_OPENED = "opened";
    public static final String EDITOR_FILE_CLOSED = "closed";
    public static final String EDITOR_FILE_SELECTED = "selected";
    public static final String EDITOR_FILE_UNSELECTED = "unselected";

    private String name;
    private String filename;

    public EditorEvent(String name, String message, String filename) {
        super();
        this.setEventType(EventType.EDITOR);
        this.setMessage(message);
        this.name = name;
        this.filename = filename;
    }

    public String getName() {
        return name;
    }

    public String getFileName() {
        return filename;
    }
}
