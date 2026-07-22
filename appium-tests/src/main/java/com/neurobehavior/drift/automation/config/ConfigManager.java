package com.neurobehavior.drift.automation.config;

import java.io.InputStream;
import java.util.Properties;

public class ConfigManager {
    private static Properties properties = new Properties();

    static {
        try (InputStream input = ConfigManager.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find config.properties");
            } else {
                properties.load(input);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static String getAppiumServerUrl() {
        return getProperty("appium.server.url");
    }

    public static String getPlatformName() {
        return getProperty("platform.name");
    }

    public static String getPlatformVersion() {
        return getProperty("platform.version");
    }

    public static String getDeviceName() {
        return getProperty("device.name");
    }

    public static String getAppPath() {
        return getProperty("app.path");
    }

    public static String getAppPackage() {
        return getProperty("app.package");
    }

    public static String getAppActivity() {
        return getProperty("app.activity");
    }

    public static int getExplicitWait() {
        return Integer.parseInt(getProperty("explicit.wait"));
    }

    public static int getImplicitWait() {
        return Integer.parseInt(getProperty("implicit.wait"));
    }
}
