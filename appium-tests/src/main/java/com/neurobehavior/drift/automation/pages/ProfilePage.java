package com.neurobehavior.drift.automation.pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

public class ProfilePage extends BasePage {
    private final By profileHeader = By.xpath("//h1[contains(., 'Profile Management')]");
    private final By editProfileBtn = By.xpath("//button[contains(., 'Edit Profile')]");
    private final By saveProfileBtn = By.xpath("//button[contains(., 'Save Changes')]");
    private final By viewFullName = By.xpath("//h2[contains(@class, 'text-xl')]");
    private final By editFullName = By.name("full_name");
    private final By viewAge = By.xpath("//label[text()='Age']/following-sibling::p");
    private final By editAge = By.name("age");
    private final By viewOccupation = By.xpath("//label[text()='Occupation']/following-sibling::p");
    private final By editOccupation = By.name("occupation");
    private final By successToast = By.xpath("//span[contains(@class, 'text-green-400')]");

    public ProfilePage(AndroidDriver driver) {
        super(driver);
    }

    public boolean isProfilePageDisplayed() {
        switchToWebViewContext();
        return isDisplayed(profileHeader);
    }

    public void clickEditProfile() {
        switchToWebViewContext();
        waitAndClick(editProfileBtn);
    }

    public void clickSaveProfile() {
        switchToWebViewContext();
        waitAndClick(saveProfileBtn);
    }

    public String getFullName() {
        switchToWebViewContext();
        if (isDisplayed(editFullName)) {
            return waitAndFind(editFullName).getAttribute("value");
        }
        return waitAndGetText(viewFullName);
    }

    public String getAge() {
        switchToWebViewContext();
        if (isDisplayed(editAge)) {
            return waitAndFind(editAge).getAttribute("value");
        }
        return waitAndGetText(viewAge);
    }

    public String getOccupation() {
        switchToWebViewContext();
        if (isDisplayed(editOccupation)) {
            return waitAndFind(editOccupation).getAttribute("value");
        }
        return waitAndGetText(viewOccupation);
    }

    public boolean isSaveSuccessDisplayed() {
        switchToWebViewContext();
        try {
            Thread.sleep(500); // Allow react state saveStatus transition
        } catch (Exception ignored) {}
        return isDisplayed(successToast);
    }
}
