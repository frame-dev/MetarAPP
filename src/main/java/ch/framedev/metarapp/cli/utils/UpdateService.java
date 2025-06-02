package ch.framedev.metarapp.cli.utils;

import ch.framedev.metarapp.cli.Main;
import ch.framedev.simplejavautils.SystemUtils;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static ch.framedev.metarapp.cli.Main.*;

public class UpdateService {

    public static String getLatestVersion() {
        try {
            URLConnection con = new URL("https://framedev.ch/files/metarapp/cli/cli-version.txt").openConnection();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            return bufferedReader.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean hasUpdate() {
        return !getLatestVersion().equalsIgnoreCase(VERSION);
    }

    public static boolean downloadLatestVersion() {
        String latestVersion = getLatestVersion();
        if (!latestVersion.equalsIgnoreCase(VERSION)) {
            try {
                InputStream inputStream = new URL("https://framedev.ch/files/metarapp/cli/MetarAPP-CLI-" + latestVersion + ".zip").openStream();
                if(!Main.TESTING) {
                    Files.copy(inputStream, new File(new File(getFilePath()).getParent() + "/MetarAPP-CLI-" + latestVersion + ".zip").toPath(), StandardCopyOption.REPLACE_EXISTING);
                    if (new File(new File(getFilePath()).getParent() + "/MetarAPP-CLI-" + latestVersion + ".zip").exists()) {
                        unzip(new File(getFilePath()).getParent() + "/MetarAPP-CLI-" + latestVersion + ".zip", new File(getFilePath()).getParent());
                        Files.delete(new File(new File(getFilePath()).getParent() + "/MetarAPP-CLI-" + latestVersion + ".zip").toPath());
                        return true;
                    }
                } else {
                    Files.copy(inputStream, new File(new File(getFilePath()).getParent() + "/MetarAPP-CLI-" + latestVersion + ".zip").toPath(), StandardCopyOption.REPLACE_EXISTING);
                    if (new File(new File(getFilePath()).getParent() + "/MetarAPP-CLI-" + latestVersion + ".zip").exists()) {
                        unzip(new File(getFilePath()).getParent() + "/MetarAPP-CLI-" + latestVersion + ".zip", new File(getFilePath()).getParent());
                        File oldFile = new File(new File(getFilePath()).getParent(), "MetarAPP-CLI");
                        File newFile = new File(new File(getFilePath()).getParent(), "testing");
                        if(newFile.exists()) {
                            deleteDirectory(newFile);
                        }
                        try {
                            if(!oldFile.renameTo(newFile))
                                utils.createEmptyLogger("MetarAPP-CLI", false).log(Level.SEVERE, "Could not create testing folder!");
                        } catch (Exception ex) {
                            utils.createEmptyLogger("MetarAPP-CLI", false).log(Level.SEVERE, "Could not create testing folder!");
                            utils.createEmptyLogger("MetarAPP-CLI", false).log(Level.SEVERE, "Could not create testing", ex);
                        }
                        Files.delete(new File(new File(getFilePath()).getParent() + "/MetarAPP-CLI-" + latestVersion + ".zip").toPath());
                        return true;
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    public static void unzip(String zipFilePath, String destinationDir) throws IOException {
        File dir = new File(destinationDir);
        // create output directory if it doesn't exist
        if (!dir.exists()) dir.mkdirs();

        FileInputStream fis = new FileInputStream(zipFilePath);
        ZipInputStream zis = new ZipInputStream(fis);
        ZipEntry entry;

        while ((entry = zis.getNextEntry()) != null) {
            String filePath = destinationDir + File.separator + entry.getName();

            if (entry.isDirectory()) {
                // If the entry is a directory, create the directory
                new File(filePath).mkdirs();
            } else {
                // If the entry is a file, write the file
                extractFile(zis, filePath);
            }
            zis.closeEntry();
        }

        zis.close();
        fis.close();
    }

    private static void extractFile(ZipInputStream zis, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(Paths.get(filePath)));
        byte[] bytesIn = new byte[4096];
        int read;
        while ((read = zis.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }

    public static void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file); // Recursive call for subdirectories
                    } else {
                        file.delete(); // Delete files
                    }
                }
            }
            directory.delete(); // Delete the directory itself
        }
    }

    public static void downloadLatestVersionAndStart() {
        deleteOldVersionAndStartNew();
    }

    public static void deleteOldVersions() {
        File[] files = new File(getFilePath()).listFiles();
        for (File file : files != null ? files : new File[0]) {
            // Check if the file is an executable or a JAR file and starts with "MetarAPP"
            if (file.getName().endsWith(".exe") || file.getName().endsWith(".jar")) {
                if (file.getName().startsWith("MetarAPP-CLI")) {
                    // Check if the file is not the current version and delete it if it's not a directory
                    if (!file.getName().startsWith("MetarAPP-CLI-" + VERSION)) {
                        if (!file.isDirectory()) {
                            if (!file.delete()) {
                                System.out.println("error deleting");
                            }
                        }
                    }
                }
            }
        }
    }

    public static void deleteOldVersionAndStartNew() {
        // Your existing code to delete old versions
        // Get the list of files in the directory
        File[] files = new File(getFilePath()).listFiles();
        for (File file : files != null ? files : new File[0]) {
            // Check if the file is an executable or a JAR file and starts with "MetarAPP"
            if (file.getName().endsWith(".exe") || file.getName().endsWith(".jar")) {
                if (file.getName().startsWith("MetarAPP-CLI")) {
                    // Check if the file is not the current version and delete it if it's not a directory
                    if (!file.getName().startsWith("MetarAPP-CLI-" + VERSION)) {
                        if (!file.isDirectory()) {
                            if (!file.delete()) {
                                System.out.println("error deleting");
                            }
                        }
                    }
                }
            }
        }

        downloadLatestVersion();
        String newFilePath = getFilePath() + "MetarAPP-CLI-" + getLatestVersion();

        // Check if the new version is an EXE or JAR file and append the appropriate extension
        if (new File(newFilePath + ".jar").exists()) {
            newFilePath += ".jar";
        } else {
            // Handle the case if neither EXE nor JAR file exists
            // You may want to display an error message or take appropriate action
            System.out.println("New version file not found!");
        }


        // Now start the new version of your application
        File newVersionFile = new File(newFilePath); // Adjust this to get the path of your new version file
        if (newVersionFile.exists()) {
            try {
                // Start the new JAR file in the same console
                ProcessBuilder pb = new ProcessBuilder("java", "-jar", newVersionFile.getAbsolutePath());
                pb.inheritIO();  // Redirects standard output and error to the current process's standard output and error
                Process process = pb.start();

                // Wait for the process to complete
                int exitValue = process.waitFor();

                if (exitValue == 0) {
                    // Exit the current application
                    System.exit(0);
                } else {
                    throw new RuntimeException("Failed to start the new JAR file. Exit value: " + exitValue);
                }
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException("Error starting the new JAR file", e);
            }
        }

    }

    public static boolean isOs(SystemUtils.OSType osType) {
        return new SystemUtils().getOSType() == osType;
    }
}
