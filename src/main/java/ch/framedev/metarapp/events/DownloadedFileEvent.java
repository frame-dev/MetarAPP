package ch.framedev.metarapp.events;

/**
 * This Event fires when a file has been downloaded.
 * It contains the file path of the downloaded file.
 * This can be used to trigger further actions, such as displaying a notification or updating the UI.
 */
public class DownloadedFileEvent {

    private final String filePath;

    public DownloadedFileEvent(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }
    
}
