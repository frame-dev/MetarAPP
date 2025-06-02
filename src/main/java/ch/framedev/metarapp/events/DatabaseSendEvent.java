package ch.framedev.metarapp.events;

public class DatabaseSendEvent {

    private String databaseName;
    private String query;

    public DatabaseSendEvent(String databaseName, String query) {
        if (databaseName == null || databaseName.isEmpty()) {
            throw new IllegalArgumentException("Database name cannot be null or empty");
        }
        if (query == null || query.isEmpty()) {
            throw new IllegalArgumentException("Query cannot be null or empty");
        }
        this.databaseName = databaseName;
        this.query = query;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getQuery() {
        return query;
    }
    
}
