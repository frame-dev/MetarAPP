package ch.framedev.metarapp.events;

/**
 * Event gives the data to display a METAR in the GUI.
 * It contains the ICAO code and the METAR data.
 */
public class DisplayMetarEvent {
    
    private String icao;
    private String data;

    public DisplayMetarEvent(String icao, String data) {
        if (icao == null || icao.isEmpty()) {
            throw new IllegalArgumentException("ICAO cannot be null or empty");
        }
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Data cannot be null or empty");
        }
        this.icao = icao;
        this.data = data;
    }

    public String getIcao() {
        return icao;
    }

    public String getData() {
        return data;
    }
}
