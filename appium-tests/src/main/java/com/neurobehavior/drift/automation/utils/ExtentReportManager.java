package com.neurobehavior.drift.automation.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import java.io.File;

public class ExtentReportManager {
    private static ExtentReports extent;
    private static ThreadLocal<ExtentTest> test = new ThreadLocal<>();

    public static synchronized ExtentReports getReporter() {
        if (extent == null) {
            String reportPath = "reports/ExtentReport/index.html";
            File file = new File(reportPath);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            ExtentSparkReporter htmlReporter = new ExtentSparkReporter(reportPath);
            htmlReporter.config().setTheme(Theme.DARK);
            htmlReporter.config().setDocumentTitle("NeuroBehaviorDrift Automation Report");
            htmlReporter.config().setReportName("Appium Mobile Test Summary");

            extent = new ExtentReports();
            extent.attachReporter(htmlReporter);
            extent.setSystemInfo("Host Name", "Antigravity CI");
            extent.setSystemInfo("Platform", "Android");
            extent.setSystemInfo("Framework", "Appium Java-Client");
        }
        return extent;
    }

    public static synchronized ExtentTest getTest() {
        return test.get();
    }

    public static synchronized void setTest(ExtentTest extentTest) {
        test.set(extentTest);
    }
    
    public static synchronized void removeTest() {
        test.remove();
    }
}
