package com.neurobehavior.drift.automation.utils;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.screenrecording.CanRecordScreen;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

public class VideoRecordUtility {
    
    public static void startRecording(AndroidDriver driver) {
        if (driver instanceof CanRecordScreen) {
            try {
                ((CanRecordScreen) driver).startRecordingScreen();
            } catch (Exception e) {
                System.out.println("Could not start screen recording: " + e.getMessage());
            }
        }
    }
    
    public static String stopRecording(AndroidDriver driver, String testName) {
        if (driver instanceof CanRecordScreen) {
            try {
                String rawVideo = ((CanRecordScreen) driver).stopRecordingScreen();
                if (rawVideo == null || rawVideo.isEmpty()) {
                    return "N/A";
                }
                
                byte[] decodedVideo = Base64.getDecoder().decode(rawVideo);
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String destPath = "screenrecordings/" + testName + "_" + timestamp + ".mp4";
                File destFile = new File(destPath);
                
                File parentDir = destFile.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs();
                }
                
                FileUtils.writeByteArrayToFile(destFile, decodedVideo);
                return destFile.getAbsolutePath();
            } catch (Exception e) {
                System.out.println("Could not stop screen recording: " + e.getMessage());
            }
        }
        return "N/A";
    }
}
