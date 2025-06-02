package ch.framedev.metarapp.events;

/**
 * This Event fires when location has been changed from login to main as example.
 * It contains the 'from' and 'to' parameters to indicate the source and destination of the refresh.
 */
public class CallRefreshEvent {

    private String from, to;
    // This class is used to trigger a refresh event in the application.
    // It can be used to notify other components that a refresh is needed.
    
    // No fields or methods are required for this event class.
    // It serves as a marker for the event system to recognize the refresh request.
    public CallRefreshEvent(String from, String to) {
        this.from = from;
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }
}
