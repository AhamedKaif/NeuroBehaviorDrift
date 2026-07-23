package com.neurobehavior.drift.automation.listeners;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import io.appium.java_client.android.AndroidDriver;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import com.neurobehavior.drift.automation.driver.DriverFactory;
import com.neurobehavior.drift.automation.utils.ExcelUtility;
import com.neurobehavior.drift.automation.utils.ExtentReportManager;
import com.neurobehavior.drift.automation.utils.LoggerHelper;
import com.neurobehavior.drift.automation.utils.ScreenshotUtility;
import com.neurobehavior.drift.automation.utils.VideoRecordUtility;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TestListener implements ITestListener {
    private static final Logger log = LoggerHelper.getLogger(TestListener.class);
    private static Map<String, Integer> retryMap = new ConcurrentHashMap<>();

    @Override
    public void onStart(ITestContext context) {
        log.info("Starting Suite: " + context.getName());
    }

    @Override
    public void onFinish(ITestContext context) {
        log.info("Finishing Suite. Generating Extent Report...");
        ExtentReportManager.getReporter().flush();
    }

    @Override
    public void onTestStart(ITestResult result) {
        String methodName = result.getMethod().getMethodName();
        log.info("--- Starting Test: " + methodName + " ---");
        
        ExtentTest extentTest = ExtentReportManager.getReporter().createTest(methodName, result.getMethod().getDescription());
        ExtentReportManager.setTest(extentTest);
        
        // Start screen recording for the test
        AndroidDriver driver = DriverFactory.getDriverOrNull();
        if (driver != null) {
            VideoRecordUtility.startRecording(driver);
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        String methodName = result.getMethod().getMethodName();
        log.info("Test PASSED: " + methodName);
        
        ExtentTest t = ExtentReportManager.getTest();
        if (t != null) {
            t.log(Status.PASS, "Test passed successfully.");
        }
        
        logToExcel(result, "PASS", "N/A", "N/A", "N/A");
        
        // Clean up video recording
        AndroidDriver driver = DriverFactory.getDriverOrNull();
        if (driver != null) {
            VideoRecordUtility.stopRecording(driver, methodName); // Just stop, no need to keep success videos
        }
        ExtentReportManager.removeTest();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String methodName = result.getMethod().getMethodName();
        log.error("Test FAILED: " + methodName, result.getThrowable());
        
        AndroidDriver driver = DriverFactory.getDriverOrNull();
        String screenshotPath = "N/A";
        String videoPath = "N/A";
        if (driver != null) {
            screenshotPath = ScreenshotUtility.captureScreenshot(driver, methodName);
            videoPath = VideoRecordUtility.stopRecording(driver, methodName);
        }
        
        String failureReason = result.getThrowable() != null ? result.getThrowable().getMessage() : "Unknown exception";
        
        ExtentTest t = ExtentReportManager.getTest();
        if (t != null) {
            t.log(Status.FAIL, "Test failed: " + failureReason);
            if (!"N/A".equals(screenshotPath)) {
                t.addScreenCaptureFromPath(screenshotPath, "Failure Screenshot");
            }
        }
        
        // Increment retry map
        int currentRetry = retryMap.getOrDefault(methodName, 0);
        
        logToExcel(result, "FAIL", screenshotPath, videoPath, failureReason);
        
        // Register the retry count for the next run if it gets retried
        retryMap.put(methodName, currentRetry + 1);
        
        ExtentReportManager.removeTest();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        String methodName = result.getMethod().getMethodName();
        log.warn("Test SKIPPED: " + methodName);
        if (result.getThrowable() != null) {
            log.warn("Skip cause: ", result.getThrowable());
        }
        
        AndroidDriver driver = DriverFactory.getDriverOrNull();
        String screenshotPath = "N/A";
        if (driver != null) {
            screenshotPath = ScreenshotUtility.captureScreenshot(driver, methodName + "_skip");
        }
        
        ExtentTest t = ExtentReportManager.getTest();
        if (t != null) {
            t.log(Status.SKIP, "Test skipped.");
            if (!"N/A".equals(screenshotPath)) {
                t.addScreenCaptureFromPath(screenshotPath, "Skip Screenshot");
            }
        }
        
        logToExcel(result, "SKIPPED", screenshotPath, "N/A", "Was retried or skipped");
        ExtentReportManager.removeTest();
    }

    private void logToExcel(ITestResult result, String status, String screenshotPath, String videoPath, String failureReason) {
        String methodName = result.getMethod().getMethodName();
        String testId = "N/A";
        String feature = "N/A";
        String scenario = result.getMethod().getDescription();
        if (scenario == null || scenario.isEmpty()) {
            scenario = methodName;
        }

        if (methodName.contains("_")) {
            String[] parts = methodName.split("_", 3);
            testId = parts[0];
            if (parts.length > 1) {
                feature = parts[1];
            }
        }

        String module = result.getTestClass().getRealClass().getSimpleName().replace("Tests", "");
        
        long durationMs = result.getEndMillis() - result.getStartMillis();
        String execTime = String.format("%.2f s", durationMs / 1000.0);
        
        AndroidDriver driver = DriverFactory.getDriverOrNull();
        String deviceName = "N/A";
        String osVersion = "N/A";
        if (driver != null) {
            try {
                deviceName = driver.getCapabilities().getCapability("deviceName").toString();
                osVersion = driver.getCapabilities().getCapability("platformVersion").toString();
            } catch (Exception e) {
                // Ignore capability read errors
            }
        }
        
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        int retryCount = retryMap.getOrDefault(methodName, 0);
        
        ExcelUtility.logTestResult(
            testId, module, feature, scenario,
            "Expect functionality to succeed", status.equals("PASS") ? "Succeeded" : "Failed: " + failureReason,
            status, retryCount, execTime, deviceName, osVersion,
            screenshotPath, videoPath, failureReason, date
        );
    }
}
