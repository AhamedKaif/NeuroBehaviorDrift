package com.neurobehavior.drift.automation.pages;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

public class LoginPage extends BasePage {
    private final By usernameInput = AppiumBy.accessibilityId("Username Input");
    private final By passwordInput = AppiumBy.accessibilityId("Password Input");
    private final By loginBtn = AppiumBy.accessibilityId("Login Button");
    private final By navigateToRegisterBtn = AppiumBy.accessibilityId("Navigate to Register");
    private final By forgotPasswordBtn = AppiumBy.accessibilityId("Forgot Password Link");
    private final By errorMessageText = AppiumBy.accessibilityId("Error Message");

    public LoginPage(AndroidDriver driver) {
        super(driver);
    }

    public void enterUsername(String username) {
        waitAndSendKeys(usernameInput, username);
    }

    public void enterPassword(String password) {
        waitAndSendKeys(passwordInput, password);
    }

    public void clickLogin() {
        waitAndClick(loginBtn);
    }

    public void clickForgotPassword() {
        waitAndClick(forgotPasswordBtn);
    }

    public void navigateToRegister() {
        waitAndClick(navigateToRegisterBtn);
    }

    public void login(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        clickLogin();
    }

    public boolean isErrorMessageDisplayed() {
        return isDisplayed(errorMessageText);
    }

    public String getErrorMessage() {
        return waitAndGetText(errorMessageText);
    }
}
