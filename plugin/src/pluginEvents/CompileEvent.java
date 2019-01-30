package pluginEvents;

public class CompileEvent extends Event {
    public static final String COMPILE_SUCCESS = "successful";
    public static final String COMPILE_FAIL = "failed";

    private String name;

    public CompileEvent(String name, String message) {
        super();
        this.setEventType(EventType.COMPILE);
        this.setMessage(message);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
