package com.neurobehavior.drift.automation.driver;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import com.neurobehavior.drift.automation.config.ConfigManager;

import java.io.File;
import java.net.URL;
import java.time.Duration;

public class DriverFactory {
    private static ThreadLocal<AndroidDriver> driver = new ThreadLocal<>();

    public static AndroidDriver getDriver() {
        if (driver.get() == null) {
            initializeDriver();
        }
        return driver.get();
    }

    private static synchronized void initializeDriver() {
        try {
            UiAutomator2Options options = new UiAutomator2Options();
            options.setPlatformName(ConfigManager.getPlatformName());
            options.setPlatformVersion(ConfigManager.getPlatformVersion());
            options.setDeviceName(ConfigManager.getDeviceName());
            options.setAutomationName("UiAutomator2");
            options.setAutoGrantPermissions(true);
            options.setNoReset(false);
            
            // Set app path if it exists, otherwise fall back to package/activity
            String appPath = ConfigManager.getAppPath();
            File appFile = new File(appPath);
            if (appFile.exists()) {
                options.setApp(appFile.getAbsolutePath());
            } else {
                options.setAppPackage(ConfigManager.getAppPackage());
                options.setAppActivity(ConfigManager.getAppActivity());
            }

            URL url = new URL(ConfigManager.getAppiumServerUrl());
            AndroidDriver androidDriver = new AndroidDriver(url, options);
            androidDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(ConfigManager.getImplicitWait()));
            driver.set(androidDriver);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize Appium Driver: " + e.getMessage());
        }
    }

    public static void quitDriver() {
        if (driver.get() != null) {
            try {
                driver.get().quit();
            } catch (Exception e) {
                // Ignore session already closed exceptions
            }
            driver.remove();
        }
    }
}
