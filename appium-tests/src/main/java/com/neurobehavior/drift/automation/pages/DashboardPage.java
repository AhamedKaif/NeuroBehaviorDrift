package com.neurobehavior.drift.automation.pages;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

public class DashboardPage extends BasePage {
    private final By dashboardTitle = AppiumBy.accessibilityId("Dashboard Header");
    private final By drawerOpenBtn = AppiumBy.accessibilityId("Open Navigation Drawer");
    private final By homeNavBtn = AppiumBy.accessibilityId("Bottom Nav Home");
    private final By analyticsNavBtn = AppiumBy.accessibilityId("Bottom Nav Analytics");
    private final By driftNavBtn = AppiumBy.accessibilityId("Bottom Nav Drift");
    private final By settingsNavBtn = AppiumBy.accessibilityId("Bottom Nav Settings");
    
    private final By ingestMetricBtn = AppiumBy.accessibilityId("Ingest Metric Button");
    private final By stressValueText = AppiumBy.accessibilityId("Stress Level Text");
    private final By driftIndicatorText = AppiumBy.accessibilityId("Drift Alert Message");

    public DashboardPage(AndroidDriver driver) {
        super(driver);
    }

    public boolean isDashboardDisplayed() {
        return isDisplayed(dashboardTitle);
    }

    public void openDrawer() {
        waitAndClick(drawerOpenBtn);
    }

    public void clickHome() {
        waitAndClick(homeNavBtn);
    }

    public void clickAnalytics() {
        waitAndClick(analyticsNavBtn);
    }

    public void clickDrift() {
        waitAndClick(driftNavBtn);
    }

    public void clickSettings() {
        waitAndClick(settingsNavBtn);
    }

    public void clickIngestMetric() {
        waitAndClick(ingestMetricBtn);
    }

    public String getStressLevel() {
        return waitAndGetText(stressValueText);
    }

    public boolean isDriftIndicatorDisplayed() {
        return isDisplayed(driftIndicatorText);
    }
}
