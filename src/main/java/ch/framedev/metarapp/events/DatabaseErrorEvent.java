package ch.framedev.metarapp.events;

public class DatabaseErrorEvent {
    
    private String databaseName;
    private String errorMessage;
    private Exception error;
    private Throwable throwable;

    public DatabaseErrorEvent(String databaseName, String errorMessage) {
        if (databaseName == null || databaseName.isEmpty()) {
            throw new IllegalArgumentException("Database name cannot be null or empty");
        }
        if (errorMessage == null || errorMessage.isEmpty()) {
            throw new IllegalArgumentException("Error message cannot be null or empty");
        }
        this.databaseName = databaseName;
        this.errorMessage = errorMessage;
    }

    public DatabaseErrorEvent(String databaseName, Exception error) {
        if (databaseName == null || databaseName.isEmpty()) {
            throw new IllegalArgumentException("Database name cannot be null or empty");
        }
        if (error == null) {
            throw new IllegalArgumentException("Error cannot be null");
        }
        this.databaseName = databaseName;
        this.errorMessage = error.getMessage();
        this.error = error;
    }

    public DatabaseErrorEvent(String databaseName, String errorMessage, Throwable throwable) {
        if (databaseName == null || databaseName.isEmpty()) {
            throw new IllegalArgumentException("Database name cannot be null or empty");
        }
        if (errorMessage == null || errorMessage.isEmpty()) {
            throw new IllegalArgumentException("Error message cannot be null or empty");
        }
        this.databaseName = databaseName;
        this.errorMessage = errorMessage;
        this.throwable = throwable;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public Exception getError() {
        return error;
    }

}
