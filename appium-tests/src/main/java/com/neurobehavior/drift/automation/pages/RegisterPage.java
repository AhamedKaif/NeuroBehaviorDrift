package com.neurobehavior.drift.automation.pages;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

public class RegisterPage extends BasePage {
    private final By fullNameInput = AppiumBy.accessibilityId("Full Name Input");
    private final By emailInput = AppiumBy.accessibilityId("Email Input");
    private final By usernameInput = AppiumBy.accessibilityId("Username Input");
    private final By passwordInput = AppiumBy.accessibilityId("Password Input");
    private final By ageInput = AppiumBy.accessibilityId("Age Input");
    private final By occupationInput = AppiumBy.accessibilityId("Occupation Input");
    private final By registerBtn = AppiumBy.accessibilityId("Submit Register");
    private final By navigateToLoginBtn = AppiumBy.accessibilityId("Navigate to Login");
    private final By registrationErrorText = AppiumBy.accessibilityId("Registration Error");

    public RegisterPage(AndroidDriver driver) {
        super(driver);
    }

    public void fillRegistrationForm(String fullName, String email, String username, String password, String age, String occupation) {
        waitAndSendKeys(fullNameInput, fullName);
        waitAndSendKeys(emailInput, email);
        waitAndSendKeys(usernameInput, username);
        waitAndSendKeys(passwordInput, password);
        waitAndSendKeys(ageInput, age);
        waitAndSendKeys(occupationInput, occupation);
    }

    public void clickRegister() {
        waitAndClick(registerBtn);
    }

    public void clickNavigateToLogin() {
        waitAndClick(navigateToLoginBtn);
    }

    public boolean isRegistrationErrorDisplayed() {
        return isDisplayed(registrationErrorText);
    }

    public String getRegistrationError() {
        return waitAndGetText(registrationErrorText);
    }
}
