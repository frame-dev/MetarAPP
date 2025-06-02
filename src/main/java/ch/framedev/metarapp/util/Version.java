package ch.framedev.metarapp.util;



/*
 * ch.framedev.metarapp.util
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 01.12.2024 18:52
 */

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * This class represents a version information object.
 * It contains details about the latest version, build numbers, and version components.
 */
public class Version {

    private String latest;
    @SerializedName("latest-buildNumber")
    private String latestBuildNumber;
    private List<String> buildNumbers;
    private List<String> version;
    @SerializedName("pre-release")
    private List<String> preRelease;
    @SerializedName("latest-pre-release")
    private String latestPreRelease;

    /**
     * Returns the latest version number.
     *
     * @return the latest version number
     */
    public String getLatest() {
        return latest;
    }

    /**
     * Returns a list of all available build numbers.
     *
     * @return a list of build numbers
     */
    public List<String> getBuildNumbers() {
        return buildNumbers;
    }

    /**
     * Returns the version components.
     *
     * @return a list of version components
     */
    public List<String> getVersion() {
        return version;
    }
}
