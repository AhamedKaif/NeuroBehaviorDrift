package com.neurobehavior.drift.automation.tests;

import io.appium.java_client.android.AndroidDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import com.neurobehavior.drift.automation.driver.DriverFactory;
import com.neurobehavior.drift.automation.pages.*;

public class BaseTest {
    protected AndroidDriver driver;
    protected LoginPage loginPage;
    protected RegisterPage registerPage;
    protected DashboardPage dashboardPage;
    protected ProfilePage profilePage;
    protected SettingsPage settingsPage;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        driver = DriverFactory.getDriver();
        loginPage = new LoginPage(driver);
        registerPage = new RegisterPage(driver);
        dashboardPage = new DashboardPage(driver);
        profilePage = new ProfilePage(driver);
        settingsPage = new SettingsPage(driver);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        DriverFactory.quitDriver();
    }
}
