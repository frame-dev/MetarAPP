package ch.framedev.metarapp.util;



/*
 * ch.framedev.metarapp.util
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 28.11.2024 18:50
 */

import ch.framedev.metarapp.data.AirportData;
import ch.framedev.metarapp.data.MetarData;
import ch.framedev.metarapp.events.ErrorEvent;
import ch.framedev.metarapp.events.EventBus;
import ch.framedev.metarapp.main.Main;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import static ch.framedev.metarapp.main.Main.*;

public class DownloadUtils {

    public File downloadMetarAirportDataToZip(MetarData metarData, AirportData airportData) {
        String date = new SimpleDateFormat("dd_MM_yyyy-HH_mm_ss_SSS").format(new Date());
        File metarDataTempFile = new File(Variables.DOWNLOADS_DIRECTORY, "metarData.json");
        File airportDataTempFile = new File(Variables.DOWNLOADS_DIRECTORY, "airportData.json");
        if (!metarDataTempFile.exists() && !airportDataTempFile.exists()) {
            try {
                if(!metarDataTempFile.getParentFile().mkdirs()) {
                    String errorMessage = ErrorMessages.getErrorCreateFileOrDirectory(ErrorCode.ERROR_CREATE_FILE_OR_DIRECTORY.getError());
                    loggerUtils.addLog(errorMessage);
                    getLogger().error(errorMessage);
                    return null;
                }
                if(!metarDataTempFile.createNewFile()) {
                    String errorMessage = ErrorMessages.getErrorCreateFileOrDirectory(ErrorCode.ERROR_CREATE_FILE_OR_DIRECTORY.getError());
                    loggerUtils.addLog(errorMessage);
                    getLogger().error(errorMessage);
                    return null;
                }
                if(!airportDataTempFile.createNewFile()) {
                    String errorMessage = ErrorMessages.getErrorCreateFileOrDirectory(ErrorCode.ERROR_CREATE_FILE_OR_DIRECTORY.getError());
                    loggerUtils.addLog(errorMessage);
                    getLogger().error(errorMessage);
                    return null;
                }
            } catch (IOException e) {
                String errorMessage = ErrorMessages.getErrorCreateFileOrDirectory(ErrorCode.ERROR_CREATE_FILE_OR_DIRECTORY.getError());
                loggerUtils.addLog(errorMessage);
                getLogger().error(errorMessage, e);
                return null;
            }
        }
        try {
            FileWriter metarDataFileWriter = new FileWriter(metarDataTempFile);
            metarDataFileWriter.write(new GsonBuilder().setPrettyPrinting().create().toJson(metarData));
            metarDataFileWriter.flush();
            metarDataFileWriter.close();
            FileWriter airportDataFileWrite = new FileWriter(airportDataTempFile);
            airportDataFileWrite.write(new GsonBuilder().setPrettyPrinting().create().toJson(airportData));
            airportDataFileWrite.flush();
            airportDataFileWrite.close();
        } catch (Exception ex) {
            getLogger().error(ex.getMessage(), ex);
        }
        File zipFile = new File(Variables.DOWNLOADS_DIRECTORY, metarData.getData().get(0).getIcao() + "-metar-airport-data_" + date + ".zip");
        if (!zipFile.exists()) {
            try {
                if(!zipFile.createNewFile()) {
                    String errorMessage = ErrorMessages.getErrorCreateFileOrDirectory(ErrorCode.ERROR_CREATE_FILE_OR_DIRECTORY.getError());
                    loggerUtils.addLog(errorMessage);
                    getLogger().error(errorMessage);
                    return null;
                }
            } catch (IOException e) {
                String errorMessage = ErrorMessages.getErrorCreateFileOrDirectory(ErrorCode.ERROR_CREATE_FILE_OR_DIRECTORY.getError());
                EventBus.dispatchErrorEvent(new ErrorEvent(ErrorCode.ERROR_CREATE_FILE_OR_DIRECTORY, errorMessage));
                loggerUtils.addLog(errorMessage);
                getLogger().error(errorMessage, e);
                return null;
            }
        }
        try {
            utils.zipFiles(zipFile, metarDataTempFile, airportDataTempFile);
        } catch (IOException e) {
            loggerUtils.addLog(ErrorMessages.getErrorZipFile());
            getLogger().error(ErrorMessages.getErrorZipFile(), e);
            return null;
        }
        if(!metarDataTempFile.delete() &&  !airportDataTempFile.delete()) {
            throw new RuntimeException("Could not delete temp files");
        }

        return zipFile;
    }

    public void zipLogFiles() {
        String date = new SimpleDateFormat("dd_MM_yyyy-HH_mm_ss_SSS").format(new Date());
        File logDirectory = Main.loggerUtils.getLoggerFile().getParentFile();
        File logFiles = new File(Main.getFilePath(), "logs");
        if (logDirectory.exists() && logDirectory.isDirectory()) {
            File zipFile = new File(Variables.DOWNLOADS_DIRECTORY, "metarapp_logs-" + date + ".zip");
            try {
                utils.zipDirectory(logDirectory, zipFile);
                for (File logFile : Objects.requireNonNull(logFiles.listFiles())) {
                    if(!logFile.delete()) {
                        throw new RuntimeException("Could not delete log file: " + logFile.getName());
                    }
                }
                if(!logDirectory.delete()) {
                    throw new RuntimeException("Could not delete log directory");
                }
            } catch (IOException e) {
                loggerUtils.addLog(ErrorMessages.getErrorZipFile());
                getLogger().error(ErrorMessages.getErrorZipFile(), e);
            }
        }
    }
}
