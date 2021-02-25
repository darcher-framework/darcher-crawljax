package com.darcher.crawljax.grpc;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNot.not;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Alert;
import org.openqa.selenium.Keys;
import java.util.*;
import java.net.MalformedURLException;
import java.net.URL;

public class GRPCClientPluginTest {
    private WebDriver driver;
    private Map<String, Object> vars;
    JavascriptExecutor js;
    @Before
    public void setUp() {
        driver = new ChromeDriver();
        js = (JavascriptExecutor) driver;
        vars = new HashMap<String, Object>();
    }
    @After
    public void tearDown() {
        driver.quit();
    }
    @Test
    public void test1() {
        driver.get("http://localhost:8080/");
        driver.manage().window().setSize(new Dimension(1407, 1336));
        driver.findElement(By.linkText("Will the Dow Jones Industrial Average close on or above 15000 on August 12, 2020?")).click();
        driver.findElement(By.id("quantity")).click();
        driver.findElement(By.id("quantity")).sendKeys("10");
        driver.findElement(By.id("limit-price")).click();
        {
            WebElement element = driver.findElement(By.cssSelector(".buttons-styles_CancelTextButton:nth-child(5)"));
            Actions builder = new Actions(driver);
            builder.moveToElement(element).perform();
        }
        driver.findElement(By.id("limit-price")).sendKeys("0.1");
        driver.findElement(By.cssSelector(".form-styles_TradingForm > ul")).click();
        driver.findElement(By.id("limit-price")).click();
        driver.findElement(By.id("limit-price")).sendKeys("0.01");
        driver.findElement(By.cssSelector(".connect-account-styles_AccountInfo div:nth-child(1)")).click();
        driver.findElement(By.cssSelector(".connect-account-styles_ConnectDropdown .connect-dropdown-styles_AddFunds > .buttons-styles_PrimaryButton")).click();
        driver.findElement(By.cssSelector(".modal-styles_ShowSelected .form-styles_RadioTwoLineBar:nth-child(2) > p")).click();
        driver.findElement(By.id("copy_address")).click();
        driver.findElement(By.cssSelector(".modal-styles_ShowSelected .form-styles_RadioTwoLineBar:nth-child(3)")).click();
        driver.findElement(By.cssSelector(".swap-row-styles_SwapRow:nth-child(1) > div > div:nth-child(2) > div")).click();
        driver.findElement(By.cssSelector(".modal-styles_ShowSelected > div:nth-child(2) .buttons-styles_CloseButton > svg")).click();
        driver.findElement(By.cssSelector(".connect-account-styles_AccountInfo > div > div:nth-child(2)")).click();
        driver.findElement(By.cssSelector(".connect-account-styles_AccountInfo > div > div:nth-child(2)")).click();
        driver.findElement(By.cssSelector(".connect-account-styles_ConnectDropdown .connect-dropdown-styles_AddFunds > .buttons-styles_PrimaryButton")).click();
        {
            WebElement element = driver.findElement(By.cssSelector(".connect-account-styles_ConnectDropdown .connect-dropdown-styles_AddFunds > .buttons-styles_PrimaryButton"));
            Actions builder = new Actions(driver);
            builder.moveToElement(element).perform();
        }
        {
            WebElement element = driver.findElement(By.tagName("body"));
            Actions builder = new Actions(driver);
            builder.moveToElement(element, 0, 0).perform();
        }
        driver.findElement(By.cssSelector(".modal-styles_ShowSelected .form-styles_RadioTwoLineBar:nth-child(3)")).click();
        driver.findElement(By.cssSelector(".swap-row-styles_SwapRow:nth-child(1) > div > div:nth-child(2) > div")).click();
        driver.findElement(By.cssSelector(".swap-row-styles_SwapRow:nth-child(1) > div > div:nth-child(2) > div")).click();
        driver.findElement(By.cssSelector(".modal-styles_ShowSelected > div:nth-child(2) > div:nth-child(2)")).click();
        driver.findElement(By.cssSelector(".modal-styles_ShowSelected > div:nth-child(2) .buttons-styles_CloseButton path:nth-child(1)")).click();
        driver.findElement(By.cssSelector(".connect-account-styles_AccountInfo div:nth-child(1)")).click();
        {
            WebElement element = driver.findElement(By.cssSelector(".connect-account-styles_ConnectDropdown .connect-dropdown-styles_AddFunds > .buttons-styles_PrimaryButton"));
            Actions builder = new Actions(driver);
            builder.moveToElement(element).perform();
        }
        {
            WebElement element = driver.findElement(By.tagName("body"));
            Actions builder = new Actions(driver);
            builder.moveToElement(element, 0, 0).perform();
        }
        driver.findElement(By.cssSelector(".connect-account-styles_ConnectDropdown .connect-dropdown-styles_AddFunds > .buttons-styles_PrimaryButton")).click();
        {
            WebElement element = driver.findElement(By.cssSelector(".connect-account-styles_ConnectDropdown .connect-dropdown-styles_AddFunds > .buttons-styles_PrimaryButton"));
            Actions builder = new Actions(driver);
            builder.moveToElement(element).perform();
        }
        {
            WebElement element = driver.findElement(By.tagName("body"));
            Actions builder = new Actions(driver);
            builder.moveToElement(element, 0, 0).perform();
        }
        driver.findElement(By.cssSelector(".modal-styles_ShowSelected .form-styles_RadioTwoLineBar:nth-child(3) > h5")).click();
        driver.findElement(By.cssSelector(".swap-row-styles_SwapRow:nth-child(1) > div > div:nth-child(2) > div")).click();
        driver.findElement(By.cssSelector(".swap-row-styles_SwapRow input")).click();
        driver.findElement(By.cssSelector(".swap-row-styles_SwapRow input")).sendKeys("1");
        driver.findElement(By.cssSelector("div:nth-child(5) > .buttons-styles_PrimaryButton")).click();
        driver.findElement(By.cssSelector(".buttons-styles_PrimaryButton > svg")).click();
        driver.findElement(By.cssSelector("div:nth-child(5) > .buttons-styles_PrimaryButton")).click();
        driver.findElement(By.cssSelector(".buttons-styles_Failed")).click();
        driver.findElement(By.cssSelector(".modal-styles_ShowSelected > div:nth-child(2) .buttons-styles_CloseButton > svg")).click();
        driver.findElement(By.cssSelector(".connect-account-styles_ConnectDropdown .connect-dropdown-styles_AddFunds > .buttons-styles_PrimaryButton")).click();
        {
            WebElement element = driver.findElement(By.cssSelector(".connect-account-styles_ConnectDropdown .connect-dropdown-styles_AddFunds > .buttons-styles_PrimaryButton"));
            Actions builder = new Actions(driver);
            builder.moveToElement(element).perform();
        }
        {
            WebElement element = driver.findElement(By.tagName("body"));
            Actions builder = new Actions(driver);
            builder.moveToElement(element, 0, 0).perform();
        }
        driver.findElement(By.cssSelector(".modal-styles_ShowSelected .form-styles_RadioTwoLineBar:nth-child(3) > h5")).click();
        driver.findElement(By.cssSelector(".swap-row-styles_SwapRow:nth-child(1) > div > div:nth-child(2) > div")).click();
        driver.findElement(By.cssSelector(".swap-row-styles_SwapRow input")).click();
        driver.findElement(By.cssSelector(".swap-row-styles_SwapRow input")).sendKeys("1");
        driver.findElement(By.cssSelector("div:nth-child(5) > .buttons-styles_PrimaryButton")).click();
        driver.findElement(By.cssSelector(".buttons-styles_PrimaryButton path:nth-child(2)")).click();
        driver.findElement(By.cssSelector(".swap-row-styles_SwapRow:nth-child(1) > div > div:nth-child(2) > div")).click();
        driver.findElement(By.cssSelector(".swap-row-styles_SwapRow:nth-child(1) > div > div:nth-child(2) > div")).click();
        driver.findElement(By.cssSelector(".swap-row-styles_SwapRow input")).click();
        driver.findElement(By.cssSelector(".swap-row-styles_SwapRow input")).sendKeys("0.9");
        driver.findElement(By.cssSelector("div:nth-child(5) > .buttons-styles_PrimaryButton")).click();
        driver.findElement(By.cssSelector(".swap-row-styles_SwapRow:nth-child(1) > div > div:nth-child(2) > div")).click();
        driver.findElement(By.cssSelector(".swap-row-styles_SwapRow:nth-child(1) > div > div:nth-child(2) > div")).click();
        driver.findElement(By.cssSelector(".swap-row-styles_SwapRow input")).click();
        driver.findElement(By.cssSelector(".swap-row-styles_SwapRow input")).sendKeys("1");
        driver.findElement(By.cssSelector(".index-styles_Swap")).click();
        driver.findElement(By.cssSelector(".modal-styles_ShowSelected > div:nth-child(2) > div:nth-child(2)")).click();
        driver.findElement(By.cssSelector(".modal-styles_ShowSelected > div:nth-child(2)")).click();
        driver.findElement(By.cssSelector(".modal-styles_ShowSelected > div:nth-child(2) .buttons-styles_CloseButton > svg")).click();
        driver.findElement(By.cssSelector(".connect-account-styles_AccountInfo div:nth-child(1)")).click();
        driver.findElement(By.cssSelector(".connect-account-styles_AccountInfo div:nth-child(1)")).click();
        driver.findElement(By.cssSelector(".connect-account-styles_ConnectDropdown .connect-dropdown-styles_AddFunds > .buttons-styles_PrimaryButton")).click();
        driver.findElement(By.cssSelector(".modal-styles_ShowSelected .form-styles_RadioTwoLineBar:nth-child(2) > p")).click();
        driver.findElement(By.cssSelector(".modal-styles_ShowSelected .form-styles_RadioTwoLineBar:nth-child(3) > p")).click();
        driver.findElement(By.cssSelector(".swap-row-styles_SwapRow:nth-child(1) > div > div:nth-child(2) > div")).click();
        driver.findElement(By.cssSelector(".swap-row-styles_SwapRow:nth-child(1) > div > div:nth-child(2) > div")).click();
        driver.findElement(By.cssSelector(".swap-row-styles_SwapRow:nth-child(1) > div > div:nth-child(2) > div")).click();
        driver.findElement(By.cssSelector(".swap-row-styles_SwapRow input")).click();
        driver.findElement(By.cssSelector(".swap-row-styles_SwapRow input")).sendKeys("1");
        driver.findElement(By.cssSelector(".index-styles_Swap")).click();
        driver.findElement(By.cssSelector(".buttons-styles_PrimaryButton > svg")).click();
        driver.findElement(By.cssSelector("div:nth-child(5) > .buttons-styles_PrimaryButton")).click();
        {
            WebElement element = driver.findElement(By.cssSelector("div:nth-child(5) > .buttons-styles_PrimaryButton"));
            Actions builder = new Actions(driver);
            builder.moveToElement(element).perform();
        }
        {
            WebElement element = driver.findElement(By.tagName("body"));
            Actions builder = new Actions(driver);
            builder.moveToElement(element, 0, 0).perform();
        }
        driver.findElement(By.cssSelector(".buttons-styles_PrimaryButton > svg")).click();
        driver.findElement(By.cssSelector(".swap-row-styles_SwapRow input")).click();
        driver.findElement(By.cssSelector(".swap-row-styles_SwapRow input")).sendKeys("1.0");
        driver.findElement(By.cssSelector("div:nth-child(5) > .buttons-styles_PrimaryButton")).click();
        driver.findElement(By.cssSelector(".buttons-styles_Failed")).click();
        driver.findElement(By.cssSelector(".modal-styles_ShowSelected > div:nth-child(2) .buttons-styles_CloseButton path:nth-child(2)")).click();
        driver.findElement(By.cssSelector(".connect-account-styles_AccountInfo div:nth-child(1)")).click();
        driver.findElement(By.id("quantity")).click();
        driver.findElement(By.id("quantity")).sendKeys("10");
        driver.findElement(By.id("limit-price")).click();
        driver.findElement(By.id("limit-price")).sendKeys("0.01");
        driver.findElement(By.cssSelector(".buttons-styles_BuyOrderButton")).click();
        driver.findElement(By.cssSelector("g:nth-child(1) > path:nth-child(4)")).click();
        driver.findElement(By.cssSelector(".highcharts-background")).click();
        driver.findElement(By.cssSelector(".wrapper-styles_Buy > li:nth-child(2) > button")).click();
        driver.findElement(By.id("quantity")).click();
        driver.close();
    }
}
