plugins {
    java
}

group = "com.neurobehavior.drift.automation"
version = "1.0-SNAPSHOT"

dependencies {
    // Appium Java Client for mobile interaction
    implementation("io.appium:java-client:9.2.2")
    
    // TestNG for execution and test management
    implementation("org.testng:testng:7.10.2")
    
    // Apache POI for Excel report generation
    implementation("org.apache.poi:poi:5.2.5")
    implementation("org.apache.poi:poi-ooxml:5.2.5")
    
    // Extent Reports for clean, styled HTML reports
    implementation("com.aventstack:extentreports:5.1.1")
    
    // Allure Reports integration
    implementation("io.qameta.allure:allure-testng:2.27.0")
    
    // Logging framework
    implementation("org.apache.logging.log4j:log4j-api:2.23.1")
    implementation("org.apache.logging.log4j:log4j-core:2.23.1")
    
    // File utility helpers
    implementation("commons-io:commons-io:2.16.1")
}

tasks.test {
    useTestNG {
        suites("testng.xml")
    }
    testLogging {
        showStandardStreams = true
    }
}
