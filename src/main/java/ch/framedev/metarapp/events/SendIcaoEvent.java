package ch.framedev.metarapp.events;


/**
 * This event fires when an ICAO code is sent to the application.
 * The Icao code can be changed
 */
public class SendIcaoEvent {
    private String icao;

    // This class is used to send an ICAO code to the application.
    // It can be used to notify other components that an ICAO code has been sent.
    
    public SendIcaoEvent(String icao) {
        this.icao = icao;
    }

    public String getIcao() {
        return icao;
    }

    public void setIcao(String icao) {
        this.icao = icao;
    }
}
