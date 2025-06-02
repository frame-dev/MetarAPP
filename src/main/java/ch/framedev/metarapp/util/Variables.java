package ch.framedev.metarapp.util;

import ch.framedev.metarapp.main.Main;
import org.apache.log4j.Level;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;

import static ch.framedev.metarapp.main.Main.branch;
import static ch.framedev.metarapp.main.Main.utils;

public class Variables {

    public static String FILES_DIRECTORY;
    public static String DOWNLOADS_DIRECTORY;
    public static String DOCUMENTS_DIRECTORY;
    public static String MYSQL_HOST;
    public static int MYSQL_PORT;
    public static String MYSQL_USER;
    public static String MYSQL_PASSWORD;
    public static String MYSQL_DATABASE;
    public static String BRANCH;

    public boolean initialize() {
        FILES_DIRECTORY = Main.getFilePath() + "files";
        DOWNLOADS_DIRECTORY = Main.getFilePath() + Main.settings.getString("download-folder");
        DOCUMENTS_DIRECTORY = Main.getFilePath() + "documents";
        BRANCH = Main.settings.getString(Setting.BRANCH.key).equalsIgnoreCase(branch) ? Main.settings.getString(Setting.BRANCH.key) : branch;
        try { 
            MYSQL_HOST = EncryptionUtil.decrypt(Main.connectionTokenHandler.getProperty("mysql-host"));
            MYSQL_PORT = Integer.parseInt(EncryptionUtil.decrypt(Main.connectionTokenHandler.getProperty("mysql-port")));
            MYSQL_USER = EncryptionUtil.decrypt(Main.connectionTokenHandler.getProperty("mysql-user"));
            MYSQL_PASSWORD = EncryptionUtil.decrypt(Main.connectionTokenHandler.getProperty("mysql-password"));
            MYSQL_DATABASE = EncryptionUtil.decrypt(Main.connectionTokenHandler.getProperty("mysql-database"));
            return true;
        } catch (Exception e) {
            Main.getLogger().error(e.getMessage(), e);
            Main.loggerUtils.addLog(e.getMessage());
            return false;
        }
    }

    public static Image getLogoImage() {
        URL url;
        try {
            url = utils.getFromResourceFile("images/logo.png", Main.class).toURI().toURL();
        } catch (MalformedURLException e) {
            Main.getLogger().log(Level.ERROR, "Could not get Logo from Resources : " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return new ImageIcon(url).getImage().getScaledInstance(250, 250, Image.SCALE_SMOOTH);
    }

    public static Image getWeatherImage() {
        URL url;
        try {
            url = utils.getFromResourceFile("images/weather.png", Main.class).toURI().toURL();
        } catch (MalformedURLException e) {
            Main.getLogger().log(Level.ERROR, "Could not get Logo from Resources : " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return new ImageIcon(url).getImage();
    }

    public static ImageIcon getWeatherImageIcon() {
        URL url;
        try {
            url = utils.getFromResourceFile("images/weather.png", Main.class).toURI().toURL();
        } catch (MalformedURLException e) {
            Main.getLogger().log(Level.ERROR, "Could not get Logo from Resources : " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return new ImageIcon(url);
    }

    public static BufferedImage rotate(BufferedImage img, int radius) {
        int width = img.getWidth();
        int height = img.getHeight();
        BufferedImage newImage = new BufferedImage(width, height, img.getType());

        Graphics2D g2 = newImage.createGraphics();
        g2.rotate(Math.toRadians(radius), width / 2.0, height / 2.0);
        g2.drawImage(img, 0, 0, null);
        g2.dispose();  // Proper resource management

        return newImage;
    }

    public static BufferedImage toBufferedImage(Image image) {
        if (image instanceof BufferedImage) {
            return (BufferedImage) image;
        } else {
            // Create a buffered image with transparency
            int width = image.getWidth(null);
            int height = image.getHeight(null);
            if (width == -1 || height == -1) {
                throw new IllegalArgumentException("Image dimensions are invalid.");
            }
            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D bGr = bufferedImage.createGraphics();
            bGr.drawImage(image, 0, 0, null);
            bGr.dispose();
            return bufferedImage;
        }
    }

    public static BufferedImage resize(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resizedImage.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g2.dispose();

        return resizedImage;
    }

    public static int calculateDegrees(int wind, int runway) {
        return wind - runway;
    }

}
