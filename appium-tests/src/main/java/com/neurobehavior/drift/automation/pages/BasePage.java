package com.neurobehavior.drift.automation.pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import com.neurobehavior.drift.automation.config.ConfigManager;

import java.time.Duration;
import java.util.Collections;

public class BasePage {
    protected AndroidDriver driver;
    protected WebDriverWait wait;

    public BasePage(AndroidDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigManager.getExplicitWait()));
    }

    protected WebElement waitAndFind(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected void waitAndClick(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    protected void waitAndSendKeys(By locator, String text) {
        WebElement el = waitAndFind(locator);
        el.clear();
        el.sendKeys(text);
    }

    protected String waitAndGetText(By locator) {
        return waitAndFind(locator).getText();
    }

    protected boolean isDisplayed(By locator) {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void scrollDown() {
        int width = driver.manage().window().getSize().width;
        int height = driver.manage().window().getSize().height;

        int startX = width / 2;
        int startY = (int) (height * 0.8);
        int endY = (int) (height * 0.2);

        swipe(startX, startY, startX, endY);
    }

    public void swipeLeft() {
        int width = driver.manage().window().getSize().width;
        int height = driver.manage().window().getSize().height;

        int startX = (int) (width * 0.9);
        int endX = (int) (width * 0.1);
        int y = height / 2;

        swipe(startX, y, endX, y);
    }

    public void swipeRight() {
        int width = driver.manage().window().getSize().width;
        int height = driver.manage().window().getSize().height;

        int startX = (int) (width * 0.1);
        int endX = (int) (width * 0.9);
        int y = height / 2;

        swipe(startX, y, endX, y);
    }

    private void swipe(int startX, int startY, int endX, int endY) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 1);

        swipe.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX, startY));
        swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        swipe.addAction(finger.createPointerMove(Duration.ofMillis(600), PointerInput.Origin.viewport(), endX, endY));
        swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        driver.perform(Collections.singletonList(swipe));
    }
}
