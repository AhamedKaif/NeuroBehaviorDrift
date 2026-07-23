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

    public void switchToWebViewContext() {
        try {
            // Already in WebView?
            if (driver.getContext().contains("WEBVIEW")) {
                return;
            }
            for (int i = 0; i < 20; i++) {
                for (String context : driver.getContextHandles()) {
                    if (context.contains("WEBVIEW")) {
                        driver.context(context);
                        System.out.println("Successfully switched to WebView context: " + context);
                        return;
                    }
                }
                System.out.println("WebView context not available yet. Retrying in 2 seconds...");
                Thread.sleep(2000);
            }
            System.err.println("CRITICAL: WebView context not found. Available contexts: " + driver.getContextHandles());
        } catch (Exception e) {
            System.err.println("Error switching to WebView context: " + e.getMessage());
        }
    }

    public void switchToNativeContext() {
        try {
            driver.context("NATIVE_APP");
        } catch (Exception e) {
            // Ignore
        }
    }

    protected WebElement waitAndFind(By locator) {
        try {
            if (driver.getContext().contains("WEBVIEW")) {
                return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
            }
        } catch (Exception ignored) {}
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected void waitAndClick(By locator) {
        WebElement el = waitAndFind(locator);
        try {
            if (driver.getContext().contains("WEBVIEW")) {
                driver.executeScript("arguments[0].click();", el);
                return;
            }
        } catch (Exception e) {
            System.err.println("Failed to click via JS, falling back to Selenium click: " + e.getMessage());
        }
        wait.until(ExpectedConditions.elementToBeClickable(el)).click();
    }

    private void setWebInputValue(WebElement el, String text) {
        String js = "const el = arguments[0];"
                  + "const val = arguments[1];"
                  + "const valueSetter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;"
                  + "valueSetter.call(el, val);"
                  + "el.dispatchEvent(new Event('input', { bubbles: true }));";
        driver.executeScript(js, el, text);
    }

    protected void waitAndSendKeys(By locator, String text) {
        // Skip entirely if text is empty — avoids TimeoutException when testing empty-field validation
        if (text == null || text.isEmpty()) {
            return;
        }
        WebElement el = waitAndFind(locator);
        try {
            if (driver.getContext().contains("WEBVIEW")) {
                setWebInputValue(el, text);
                return;
            }
        } catch (Exception e) {
            System.err.println("Failed to set web input value via JS, falling back to standard sendKeys: " + e.getMessage());
        }
        el.click(); // Focus the text field
        try {
            el.clear();
        } catch (Exception e) {
            // Ignore clear errors on Compose/hybrid elements that do not support native clear actions
        }
        // If text contains HTML tags or shell-sensitive characters, bypass mobile:type to avoid adb hangs
        if (text.contains("<") || text.contains(">") || text.contains("&") || text.contains(";")) {
            el.sendKeys(text);
            return;
        }
        try {
            driver.executeScript("mobile: type", java.util.Map.of("text", text));
        } catch (Exception e) {
            // Fallback to standard sendKeys if mobile: type is not supported
            el.sendKeys(text);
        }
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

    public void scrollUp() {
        int width = driver.manage().window().getSize().width;
        int height = driver.manage().window().getSize().height;

        int startX = width / 2;
        int startY = (int) (height * 0.2);
        int endY = (int) (height * 0.8);

        swipe(startX, startY, startX, endY);
    }
}
