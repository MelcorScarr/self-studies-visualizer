package pluginEvents;

import java.time.LocalDateTime;

public class Event {
    private LocalDateTime timestamp;
    private EventType eventType;
    private String message;

    Event() {
        this.timestamp = LocalDateTime.now();
        this.eventType = EventType.BASE;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
