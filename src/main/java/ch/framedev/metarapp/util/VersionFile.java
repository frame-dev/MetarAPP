package ch.framedev.metarapp.util;



/*
 * ch.framedev.metarapp.util
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 18.09.2024 19:10
 */

import ch.framedev.metarapp.main.Main;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import static ch.framedev.metarapp.main.Main.*;

@SuppressWarnings("unused")
public class VersionFile {

    public void uploadVersions() {
        try {
            Files.copy(utils.getFromResourceFile("versions.json", Main.class).toPath(),
                    new File(Main.getFilePath() + "files/versions.json").toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String localFile = new File(Main.getFilePath() + "files/versions.json").toPath().toString();
        String remoteDir = "/var/www/html/files/metarapp/";
        String host = Main.connectionTokenHandler.getProperty("ssh-host");
        String user = Main.connectionTokenHandler.getProperty("ssh-user");
        String password = Main.connectionTokenHandler.getProperty("ssh-password");

        JSch jsch = new JSch();
        Session session = null;
        Channel channel = null;
        ChannelSftp channelSftp = null;

        try {
            // Establishing a session
            session = jsch.getSession(user, host, 22);
            session.setPassword(password);

            // Avoid asking for key confirmation
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);

            session.connect();

            // Open SFTP channel
            channel = session.openChannel("sftp");
            channel.connect();
            channelSftp = (ChannelSftp) channel;

            // Sending file
            File file = new File(localFile);
            channelSftp.put(file.getAbsolutePath(), remoteDir + "/" + file.getName());

            System.out.println("File sent successfully!");

        } catch (Exception e) {
            getLogger().error(e.getMessage(), e);
        } finally {
            // Close connections
            if (channelSftp != null) {
                channelSftp.exit();
            }
            if (channel != null) {
                channel.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
    }

    public void downloadVersions() {
        utils.download("https://framedev.ch/files/metarapp/versions.json", getFilePath() + "files", "versions.json");
    }
}
