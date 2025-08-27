package ch.framedev.metarapp.util;

import ch.framedev.metarapp.main.Main;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.apache.log4j.Level;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SuppressWarnings("unused")
public class LoggerUtils {

    private final List<String> logs;
    private final File loggerFile;

    /**
     * Constructs a new LoggerUtils instance.
     * This constructor initializes the logs list and creates a new log file
     * with a timestamp in its name.
     * <p>
     * The log file is created in the "logs" directory within the application's
     * file path, and its name follows the format "log-dd.MM.yyyy_HH-mm-ss.txt".
     */
    public LoggerUtils() {
        logs = new ArrayList<>();
        loggerFile = new File(Main.getFilePath() + "logs", "log-" + new SimpleDateFormat("dd.MM.yyyy_HH-mm-ss").format(new Date()) + ".txt");
    }

    /**
     * Retrieves the log file associated with this LoggerUtils instance.
     *
     * @return The File object representing the log file.
     * The log file is created in the "logs" directory within the application's file path,
     * and its name follows the format "log-dd.MM.yyyy_HH-mm-ss.txt".
     */
    public File getLoggerFile() {
        return loggerFile;
    }

    /**
     * Retrieves the list of log messages.
     *
     * @return A list of log messages. Each log message is a string containing the timestamp,
     * a pipe character ('|'), and the log message itself. The timestamp is formatted as
     * "dd.MM.yyyy/HH:mm:ss".
     */
    public List<String> getLogs() {
        return logs;
    }

    /**
     * Moves the log file from its current location to a designated directory within the application's file path.
     * The log file is renamed to include a ".log" extension instead of ".txt".
     * If the renaming operation fails, an error message is printed to the console.
     */
    public void moveToLogFile() {
        if (!loggerFile.renameTo(new File(Main.getFilePath() + "logs", loggerFile.getName().replace(".txt", ".log")))) {
            System.out.println("could not rename log");
        }
    }

    /**
     * Adds a new log entry to the logs list and writes the logs to a file.
     * It also sends the logs to a remote server using SFTP.
     *
     * @param log The log message to be added.
     */
    public void addLog(String log) {
        logs.add(new SimpleDateFormat("dd.MM.yyyy/HH:mm:ss").format(new Date()) + " | " + log);
        writeLogs();
    }

    /**
     * Writes the logs to a file.
     * If the log file does not exist, it creates the file and its parent directory if necessary.
     * If an error occurs during file creation or writing, it logs the error and prints it to the console.
     *
     */
    public void writeLogs() {
        if (!loggerFile.exists()) {
            try {
                if (!loggerFile.getParentFile().exists() && !loggerFile.getParentFile().mkdirs()) {
                    System.err.println("Could not create log directory");
                }
                if (!loggerFile.createNewFile()) {
                    System.err.println("Could not create Log File");
                }
            } catch (Exception e) {
                Main.getLogger().log(Level.ERROR, "Error while creating log file: " + e.getMessage(), e);
            }
        }
        try (FileWriter writer = new FileWriter(loggerFile)) {
            for (String line : logs) {
                writer.write(line + System.lineSeparator());
            }
            writer.flush();
        } catch (IOException e) {
            Main.getLogger().log(Level.ERROR, "Error while writing logs to file: " + e.getMessage(), e);
        }
    }

    /**
     * Sends the log file from the local machine to a remote server using SFTP.
     * The log file is encrypted using the {@link EncryptionUtil} before being sent.
     * The function retrieves the local IP address, the path of the log file, and the remote directory
     * from the application's properties.
     *
     * @throws RuntimeException     If an error occurs during the SFTP communication or file transfer.
     */
    public void sendLogsFromIP() {
        if(!loggerFile.exists()) return;
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            String ipAddress = inetAddress.getHostAddress();

            String remoteDir = "/var/www/html/files/metarapp/logs/";
            String host = EncryptionUtil.decrypt(Main.connectionTokenHandler.getProperty("ssh-host"));
            String user = EncryptionUtil.decrypt(Main.connectionTokenHandler.getProperty("ssh-user"));
            String password = EncryptionUtil.decrypt(Main.connectionTokenHandler.getProperty("ssh-password"));

            JSch jsch = new JSch();
            Session session = null;
            ChannelSftp channelSftp = null;

            try {
                // Establishing a session
                session = jsch.getSession(user, host, 22);
                session.setPassword(password);

                // Avoid asking for key confirmation
                java.util.Properties config = new java.util.Properties();
                config.put("StrictHostKeyChecking", "no");
                session.setConfig(config);

                // Open SFTP channel
                session.connect(10000); // 10 sec timeout
                channelSftp = (ChannelSftp) session.openChannel("sftp");
                channelSftp.connect();

                String safeIp = ipAddress.replaceAll("[^\\d.]", "_");
                String remoteFileName = remoteDir + safeIp + "_|_" + loggerFile.getName();
                channelSftp.put(loggerFile.getAbsolutePath(), remoteFileName);

                Main.getLogger().log(Level.INFO, "File sent successfully to: " + remoteFileName);

            } catch (Exception e) {
                Main.getLogger().log(Level.ERROR, "Failed to send file to Remote", e);
            } finally {
                // Close connections
                if (channelSftp != null) {
                    channelSftp.exit();
                }
                if (session != null) {
                    session.disconnect();
                }
            }
        } catch (UnknownHostException e) {
            Main.getLogger().log(Level.ERROR, "Unknown Host", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
