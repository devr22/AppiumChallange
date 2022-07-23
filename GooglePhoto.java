package com.dev.appiumautomation;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.ScreenOrientation;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;

import io.appium.java_client.android.AndroidDriver;

public class GooglePhoto {
    private static String ANDROID_PHOTO_PATH = "/mnt/sdcard/Pictures";

    private static String LOCAL_IMAGE_NAME = "Screenshot.png";

    private static By infoBtn = By.id("com.google.android.apps.photos:id/{INFO_BUTTON_ID}");
    private static By locationText = By.id("com.google.android.apps.photos:id/{LOCATION_TEXT}");

    private static By backupSwitch = By.id("com.google.android.apps.photos:id/auto_backup_switch");
    private static By touchOutside = By.id("com.google.android.apps.photos:id/touch_outside");
    private static By keepOff = By.xpath("//*[@text='KEEP OFF']");
    private static By photo = By.xpath("//android.view.ViewGroup[contains(@content-desc, 'Photo taken')]");
    private static By trash = By.id("com.google.android.apps.photos:id/trash");
    private static By moveToTrash = By.xpath("//*[@text='MOVE TO TRASH']");

    @Test
    public void testSeedPhotoPicker() throws IOException {
        DesiredCapabilities capabilities = new DesiredCapabilities();

        File classpathRoot = new File(System.getProperty("user.dir"));

        capabilities.setCapability("platformName", "Android");
        capabilities.setCapability("deviceName", "Automation");
        capabilities.setCapability("automationName", "UiAutomator2");
        capabilities.setCapability("appPackage", "com.google.android.apps.photos");
        capabilities.setCapability("appActivity", ".home.HomeActivity");

        // Open the app.
        AndroidDriver driver = new AndroidDriver(new URL("http://0.0.0.0:4723/wd/hub"), capabilities);

        try {
            // there's some screens we need to navigate through and ensure there are no existing photos
            setupAppState(driver);

            // set up the file we want to push to the phone's library
            File assetDir = new File(classpathRoot, "../assets");
            File img = new File(assetDir.getCanonicalPath(), LOCAL_IMAGE_NAME);

            // actually push the file
            driver.pushFile(ANDROID_PHOTO_PATH + "/" + img.getName(), img);

            // wait for the system to acknowledge the new photo, and use the WebDriverWait to verify
            // that the new photo is there
//            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
//            ExpectedCondition condition = ExpectedConditions.numberOfElementsToBe(photo, 1);
//            wait.until(condition);
            testUseCase(driver);
        } finally {
            driver.quit();
        }
    }

    /**
     * testUseCase function is for testing if the image file name in the full path shown in the Information
     * Panel is the same as the local image file name
     */

    public boolean testUseCase(AndroidDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.presenceOfElementLocated(photo)).click();
        driver.rotate(ScreenOrientation.LANDSCAPE);
        wait.until(ExpectedConditions.presenceOfElementLocated(infoBtn)).click();

        return driver.findElement(locationText).getText().contains(LOCAL_IMAGE_NAME);
    }

    public void setupAppState(AndroidDriver driver) {
        // navigate through the google junk to get to the app
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(3));
        wait.until(ExpectedConditions.presenceOfElementLocated(backupSwitch)).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(touchOutside)).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(keepOff)).click();

        // delete any existing pictures using an infinite loop broken when we can't find any
        // more pictures
        try {
            while (true) {
                shortWait.until(ExpectedConditions.presenceOfElementLocated(photo)).click();
                shortWait.until(ExpectedConditions.presenceOfElementLocated(trash)).click();
                shortWait.until(ExpectedConditions.presenceOfElementLocated(moveToTrash)).click();
            }
        } catch (TimeoutException ignore) {
        }
    }

}
