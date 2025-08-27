package ch.framedev.metarapp.util;



/*
 * ch.framedev.metarapp.util
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 27.11.2024 20:46
 */

import java.util.List;

/**
 * Represents a changelog for a software release.
 * This class contains information about version, release date, performance fixes, bug fixes, and new features.
 */
@SuppressWarnings("unused")
public class Changelog {

    private String version;
    private String date;
    private List<String> performanceFixes;
    private List<String> bugFixes;
    private List<String> features;
    private boolean release = true;

    /**
     * Constructs a new Changelog instance with default values.
     */
    public Changelog() {
    }

    /**
     * Gets the version of the release.
     *
     * @return the version string
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the version of the release.
     *
     * @param version the version string to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Gets the release date.
     *
     * @return the release date as a string
     */
    public String getDate() {
        return date;
    }

    /**
     * Sets the release date.
     *
     * @param date the release date to set
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * Gets the list of performance fixes.
     *
     * @return a list of performance fixes
     */
    public List<String> getPerformanceFixes() {
        return performanceFixes;
    }

    /**
     * Sets the list of performance fixes.
     *
     * @param performanceFixes the list of performance fixes to set
     */
    public void setPerformanceFixes(List<String> performanceFixes) {
        this.performanceFixes = performanceFixes;
    }

    /**
     * Gets the list of bug fixes.
     *
     * @return a list of bug fixes
     */
    public List<String> getBugFixes() {
        return bugFixes;
    }

    /**
     * Sets the list of bug fixes.
     *
     * @param bugFixes the list of bug fixes to set
     */
    public void setBugFixes(List<String> bugFixes) {
        this.bugFixes = bugFixes;
    }

    /**
     * Gets the list of new features.
     *
     * @return a list of new features
     */
    public List<String> getFeatures() {
        return features;
    }

    /**
     * Sets the list of new features.
     *
     * @param features the list of new features to set
     */
    public void setFeatures(List<String> features) {
        this.features = features;
    }

    /**
     * Sets whether this is a release version.
     *
     * @param release true if this is a release version, false otherwise
     */
    public void setRelease(boolean release) {
        this.release = release;
    }

    /**
     * Checks if this is a release version.
     *
     * @return true if this is a release version, false otherwise
     */
    public boolean isRelease() {
        return release;
    }
}
