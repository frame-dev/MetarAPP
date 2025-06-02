package ch.framedev.metarapp.util;

import javax.swing.*;

import ch.framedev.metarapp.events.DownloadedFileEvent;
import ch.framedev.metarapp.events.EventBus;

import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class FileDownloader extends JFrame {

    private JProgressBar progressBar;
    private JLabel statusLabel;
    private String fileUrl, location, fileNameWithExtension;

    private File file;

    public FileDownloader(String fileUrl, String location, String fileNameWithExtensions) {
        setTitle("File Downloader");
        setSize(400, 150);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        this.fileUrl = fileUrl;
        this.location = location;
        this.fileNameWithExtension = fileNameWithExtensions;

        statusLabel = new JLabel("Preparing download...", SwingConstants.CENTER);
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);

        add(statusLabel, BorderLayout.NORTH);
        add(progressBar, BorderLayout.CENTER);
    }

    public CompletableFuture<Boolean> downloadFile() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        SwingWorker<Void, Integer> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                if (location != null) {
                    file = new File(location, fileNameWithExtension);
                    if (file.getParentFile() != null && !file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }
                } else {
                    file = new File(fileNameWithExtension);
                }

                try {
                    URL url = new URL(fileUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();

                    int fileSize = connection.getContentLength();
                    if (fileSize < 0) {
                        System.out.println("Could not determine file size.");
                        fileSize = 1;  // Avoid division by zero
                    }

                    try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
                         FileOutputStream fout = new FileOutputStream(file)) {

                        byte[] data = new byte[4096];
                        int count;
                        int downloaded = 0;

                        while ((count = in.read(data, 0, 4096)) != -1) {
                            fout.write(data, 0, count);
                            downloaded += count;
                            int progress = (int) (((double) downloaded / fileSize) * 100);
                            publish(progress);
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    future.completeExceptionally(e); // Proper error handling
                    return null;
                }
                return null;
            }

            @Override
            protected void process(java.util.List<Integer> chunks) {
                int progress = chunks.get(chunks.size() - 1);
                progressBar.setValue(progress);
                statusLabel.setText("Downloading... " + progress + "%");
            }

            @Override
            protected void done() {
                if (!future.isCompletedExceptionally()) {
                    statusLabel.setText("Download Complete!");
                    progressBar.setValue(100);
                    future.complete(true);
                }
            }
        };
        worker.execute();
        return future;
    }

    public CompletableFuture<Boolean> startDownload() {
        setVisible(true);
        EventBus.dispatchDownloadedFileEvent(new DownloadedFileEvent(file.getAbsolutePath()));
        return downloadFile().exceptionally(e -> {
            JOptionPane.showMessageDialog(this, "Download failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        });
    }
}