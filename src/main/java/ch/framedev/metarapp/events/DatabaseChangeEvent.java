package ch.framedev.metarapp.events;

/**
 * Fired when database type has been changed.
 */
public class DatabaseChangeEvent {
    
    private String databaseType;

    public DatabaseChangeEvent(String databaseType) {
        this.databaseType = databaseType;
    }

    public String getDatabaseType() {
        return databaseType;
    }

    public void setDatabaseType(String databaseType) {
        this.databaseType = databaseType;
    }
}
