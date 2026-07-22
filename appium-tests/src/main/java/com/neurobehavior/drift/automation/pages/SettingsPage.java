package com.neurobehavior.drift.automation.pages;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

public class SettingsPage extends BasePage {
    private final By settingsHeader = AppiumBy.accessibilityId("Settings Header");
    private final By notificationToggle = AppiumBy.accessibilityId("Notification Toggle Switch");
    private final By darkModeToggle = AppiumBy.accessibilityId("Dark Mode Toggle Switch");
    private final By clearCacheBtn = AppiumBy.accessibilityId("Clear Cache Button");
    private final By logoutBtn = AppiumBy.accessibilityId("Logout Button");
    private final By permissionRequestBtn = AppiumBy.accessibilityId("Request Permissions Button");

    public SettingsPage(AndroidDriver driver) {
        super(driver);
    }

    public boolean isSettingsPageDisplayed() {
        return isDisplayed(settingsHeader);
    }

    public void toggleNotifications() {
        waitAndClick(notificationToggle);
    }

    public void toggleDarkMode() {
        waitAndClick(darkModeToggle);
    }

    public void clickClearCache() {
        waitAndClick(clearCacheBtn);
    }

    public void clickLogout() {
        waitAndClick(logoutBtn);
    }

    public void clickRequestPermissions() {
        waitAndClick(permissionRequestBtn);
    }
}
