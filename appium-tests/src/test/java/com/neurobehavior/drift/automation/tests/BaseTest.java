package com.neurobehavior.drift.automation.tests;

import io.appium.java_client.android.AndroidDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import com.neurobehavior.drift.automation.driver.DriverFactory;
import com.neurobehavior.drift.automation.pages.*;

public class BaseTest {
    protected AndroidDriver driver;
    protected LoginPage loginPage;
    protected RegisterPage registerPage;
    protected DashboardPage dashboardPage;
    protected ProfilePage profilePage;
    protected SettingsPage settingsPage;

    @BeforeClass(alwaysRun = true)
    public void setUpClass() {
        registerDefaultTestUser();
    }

    private void registerDefaultTestUser() {
        try {
            java.net.URL url = new java.net.URL("http://localhost:5000/api/auth/register");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            
            String payload = "{"
                + "\"full_name\":\"Valid Test User\","
                + "\"email\":\"valid_user@example.com\","
                + "\"username\":\"valid_user\","
                + "\"password\":\"ValidPassword123!\","
                + "\"age\":28,"
                + "\"occupation\":\"Engineer\""
                + "}";
                
            try (java.io.OutputStream os = conn.getOutputStream()) {
                byte[] input = payload.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            
            int code = conn.getResponseCode();
            System.out.println("Default test user registration status: " + code);
        } catch (Exception e) {
            System.err.println("Failed to register default test user: " + e.getMessage());
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void setUpMethod() {
        driver = DriverFactory.getDriver();
        dismissCompatibilityPopup();

        loginPage = new LoginPage(driver);
        registerPage = new RegisterPage(driver);
        dashboardPage = new DashboardPage(driver);
        profilePage = new ProfilePage(driver);
        settingsPage = new SettingsPage(driver);

        // Allow some time for WebView to load the web interface
        try {
            Thread.sleep(5000);
        } catch (Exception ignored) {}
    }

    private void dismissCompatibilityPopup() {
        try {
            org.openqa.selenium.By okBtn = io.appium.java_client.AppiumBy.xpath("//android.widget.Button[@text='OK']");
            org.openqa.selenium.By dontShowAgainBtn = io.appium.java_client.AppiumBy.xpath("//android.widget.Button[@text=\"Don't Show Again\"]");
            
            Thread.sleep(1000);
            if (!driver.findElements(dontShowAgainBtn).isEmpty()) {
                driver.findElement(dontShowAgainBtn).click();
                Thread.sleep(500);
            } else if (!driver.findElements(okBtn).isEmpty()) {
                driver.findElement(okBtn).click();
                Thread.sleep(500);
            }
        } catch (Exception e) {
            // Ignore if popup is not displayed
        }
    }

    @AfterMethod(alwaysRun = true)
    public void tearDownMethod() {
        DriverFactory.quitDriver();
    }

    @AfterClass(alwaysRun = true)
    public void tearDownClass() {
        // Class-level clean up
    }
}
