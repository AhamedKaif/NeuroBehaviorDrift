package com.neurobehavior.drift.automation.pages;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

public class ProfilePage extends BasePage {
    private final By profileHeader = AppiumBy.accessibilityId("Profile Header");
    private final By editProfileBtn = AppiumBy.accessibilityId("Edit Profile Button");
    private final By saveProfileBtn = AppiumBy.accessibilityId("Save Profile Button");
    private final By fullNameDisplay = AppiumBy.accessibilityId("Profile Full Name");
    private final By ageDisplay = AppiumBy.accessibilityId("Profile Age");
    private final By occupationDisplay = AppiumBy.accessibilityId("Profile Occupation");
    private final By successToast = AppiumBy.accessibilityId("Profile Save Success Message");

    public ProfilePage(AndroidDriver driver) {
        super(driver);
    }

    public boolean isProfilePageDisplayed() {
        return isDisplayed(profileHeader);
    }

    public void clickEditProfile() {
        waitAndClick(editProfileBtn);
    }

    public void clickSaveProfile() {
        waitAndClick(saveProfileBtn);
    }

    public String getFullName() {
        return waitAndGetText(fullNameDisplay);
    }

    public String getAge() {
        return waitAndGetText(ageDisplay);
    }

    public String getOccupation() {
        return waitAndGetText(occupationDisplay);
    }

    public boolean isSaveSuccessDisplayed() {
        return isDisplayed(successToast);
    }
}
