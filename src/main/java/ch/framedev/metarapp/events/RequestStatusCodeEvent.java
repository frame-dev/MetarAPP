package ch.framedev.metarapp.events;

public class RequestStatusCodeEvent {

    private String requestType;
    private String code;

    public RequestStatusCodeEvent(String requestType, String code) {
        this.requestType = requestType;
        this.code = code;
    }

    public String getRequestType() {
        return requestType;
    }

    public String getCode() {
        return code;
    }
    
}
