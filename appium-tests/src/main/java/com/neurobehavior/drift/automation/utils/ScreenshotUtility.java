package com.neurobehavior.drift.automation.utils;

import io.appium.java_client.android.AndroidDriver;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ScreenshotUtility {
    
    public static String captureScreenshot(AndroidDriver driver, String testName) {
        if (driver == null) return "N/A";
        
        File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String destPath = "screenshots/" + testName + "_" + timestamp + ".png";
        File destFile = new File(destPath);
        
        try {
            FileUtils.copyFile(srcFile, destFile);
            return destFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to save screenshot: " + e.getMessage();
        }
    }
}
