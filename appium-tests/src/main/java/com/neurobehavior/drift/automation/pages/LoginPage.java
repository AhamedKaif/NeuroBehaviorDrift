package com.neurobehavior.drift.automation.pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

public class LoginPage extends BasePage {
    private final By usernameInput = By.cssSelector("input[placeholder='Enter username']");
    private final By passwordInput = By.cssSelector("input[placeholder='Enter password']");
    private final By loginBtn = By.xpath("//button[text()='Sign In']");
    private final By navigateToRegisterBtn = By.xpath("//button[text()='Register now']");
    private final By errorMessageText = By.xpath("//div[contains(@class, 'bg-red-500/10')]");

    public LoginPage(AndroidDriver driver) {
        super(driver);
    }

    public void enterUsername(String username) {
        switchToWebViewContext();
        waitAndSendKeys(usernameInput, username);
    }

    public void enterPassword(String password) {
        switchToWebViewContext();
        waitAndSendKeys(passwordInput, password);
    }

    public void clickLogin() {
        switchToWebViewContext();
        waitAndClick(loginBtn);
    }

    public void clickForgotPassword() {
        // No forgot password option exists on the React Web Portal
        // No-op to preserve TestNG automation pipeline integrity
    }

    public void navigateToRegister() {
        switchToWebViewContext();
        waitAndClick(navigateToRegisterBtn);
    }

    public void login(String username, String password) {
        switchToWebViewContext();
        boolean hasUsername = username != null && !username.isEmpty();
        boolean hasPassword = password != null && !password.isEmpty();

        if (hasUsername) {
            enterUsername(username);
        }
        if (hasPassword) {
            enterPassword(password);
        }
        clickLogin();
    }

    public boolean isErrorMessageDisplayed() {
        switchToWebViewContext();
        return isDisplayed(errorMessageText);
    }

    public String getErrorMessage() {
        switchToWebViewContext();
        return waitAndGetText(errorMessageText);
    }
}
