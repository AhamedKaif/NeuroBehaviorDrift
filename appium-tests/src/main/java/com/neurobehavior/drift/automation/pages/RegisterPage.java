package com.neurobehavior.drift.automation.pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class RegisterPage extends BasePage {
    private final By fullNameInput = By.name("full_name");
    private final By emailInput = By.name("email");
    private final By usernameInput = By.name("username");
    private final By passwordInput = By.xpath("//input[@name='password' and @placeholder='Minimum 8 characters']");
    private final By confirmPasswordInput = By.name("confirmPassword");
    private final By ageInput = By.name("age");
    private final By occupationInput = By.name("occupation");
    private final By registerBtn = By.xpath("//button[text()='Complete Registration']");
    private final By navigateToLoginBtn = By.xpath("//button[text()='Log in']");
    private final By registrationErrorText = By.xpath("//div[contains(@class, 'bg-red-500/10')]");
    private final By privacyConsentCheckbox = By.name("privacy_consent");

    public RegisterPage(AndroidDriver driver) {
        super(driver);
    }

    public void fillRegistrationForm(String fullName, String email, String username, String password, String age, String occupation) {
        switchToWebViewContext();
        waitAndSendKeys(fullNameInput, fullName);
        waitAndSendKeys(emailInput, email);
        waitAndSendKeys(usernameInput, username);
        waitAndSendKeys(passwordInput, password);
        waitAndSendKeys(confirmPasswordInput, password); // Always enter matching confirm password
        waitAndSendKeys(ageInput, age);

        // Map occupation dropdown selection
        if (occupation != null && !occupation.isEmpty()) {
            try {
                WebElement selectEl = waitAndFind(occupationInput);
                selectEl.click();
                // Select matching option
                By optionSelector = By.xpath("//option[text()='" + occupation + "']");
                waitAndClick(optionSelector);
            } catch (Exception ignored) {}
        }

        // Accept privacy policy if not checked
        try {
            WebElement consent = waitAndFind(privacyConsentCheckbox);
            if (!consent.isSelected()) {
                consent.click();
            }
        } catch (Exception ignored) {}
    }

    public void clickRegister() {
        switchToWebViewContext();
        waitAndClick(registerBtn);
    }

    public void clickNavigateToLogin() {
        switchToWebViewContext();
        waitAndClick(navigateToLoginBtn);
    }

    public boolean isRegistrationErrorDisplayed() {
        switchToWebViewContext();
        return isDisplayed(registrationErrorText);
    }

    public String getRegistrationError() {
        switchToWebViewContext();
        return waitAndGetText(registrationErrorText);
    }
}
