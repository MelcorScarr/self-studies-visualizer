package pluginEvents;

public class CodeEvent extends Event {

    private String name;
    private String filename;

    public CodeEvent(String name, String message, String filename) {
        super();
        this.setEventType(EventType.CODE);
        this.setMessage(message);
        this.name = name;
        this.filename = filename;
        System.out.println("Code Event created");
    }

    public String getName() {
        return name;
    }

    public String getFilename() {
        return filename;
    }
}

