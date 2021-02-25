package com.darcher.crawljax.experiments;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.*;
import com.crawljax.core.state.Identification;
import com.crawljax.forms.FormInput;
import com.darcher.crawljax.grpc.GRPCClientPlugin;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class PublicVotesExperiment extends Experiment {

    private static final long WAIT_TIME_AFTER_EVENT = 1000;
    private static final long WAIT_TIME_AFTER_RELOAD = 500;
    private static final String DAPP_URL = "http://localhost:3000";
    private static final String DAPP_NAME = "publicvotes";
    private static int instanceId = 1;
    private static final String METAMASK_POPUP_URL = System.getenv("METAMASK_URL");
    private static final String METAMASK_PASSWORD = System.getenv("METAMASK_PASSWORD");

    private static final String mainAddress = "0x6463F93D65391A8B7c98f0fc8439eFD5d38339d9";

    /**
     * Run this method to start the crawl.
     */
    protected CrawljaxRunner initialize(Path coverageDir, String chromeDebuggerAddress) {
        CrawljaxConfiguration.CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(DAPP_URL);

//        builder.crawlRules().setFormFillMode(CrawlRules.FormFillMode.RANDOM);
//        builder.crawlRules().click("div").withAttribute("")
        // we use normal mode to avoid randomly fill forms and only allow predefined form inputs
        builder.crawlRules().setFormFillMode(CrawlRules.FormFillMode.NORMAL);
        builder.crawlRules().clickOnce(false);
        // click these elements
        builder.crawlRules().click("A");
        builder.crawlRules().click("BUTTON");

        builder.crawlRules().crawlHiddenAnchors(true);
        builder.crawlRules().crawlFrames(false);
        builder.setUnlimitedCrawlDepth();
        builder.setUnlimitedRuntime();
        builder.setUnlimitedStates();

        // 1 hour timeout
        builder.setMaximumRunTime(1, TimeUnit.HOURS);

//        builder.setMaximumStates(0); // unlimited
        builder.setMaximumDepth(0); // unlimited
        builder.crawlRules().clickElementsInRandomOrder(true);

        /* Set timeouts */
        builder.crawlRules().waitAfterReloadUrl(WAIT_TIME_AFTER_RELOAD, TimeUnit.MILLISECONDS);
        builder.crawlRules().waitAfterEvent(WAIT_TIME_AFTER_EVENT, TimeUnit.MILLISECONDS);

        InputSpecification inputSpec = new InputSpecification();

        Form createForm = new Form();
        AtomicInteger count = new AtomicInteger(0);
        createForm.inputField(FormInput.InputType.CUSTOMIZE, new Identification(Identification.How.id, "name_poll"))
                .setInputFiller((driver, webElement, nodeElement) -> {
                    webElement.sendKeys("Test Pool " + count.getAndIncrement());
                    WebElement next = new WebDriverWait(driver, Duration.ofSeconds(3))
                            .until(ExpectedConditions.elementToBeClickable(By.id("nxt")));
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click()", next);
                    new WebDriverWait(driver, Duration.ofSeconds(3))
                            .until(ExpectedConditions.visibilityOfElementLocated(By.id("description")));
                    next = new WebDriverWait(driver, Duration.ofSeconds(3))
                            .until(ExpectedConditions.elementToBeClickable(By.id("nxt")));
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click()", next);
                    new WebDriverWait(driver, Duration.ofSeconds(3))
                            .until(ExpectedConditions.visibilityOfElementLocated(By.id("options")));
                    driver.findElement(By.id("option-1")).sendKeys("A");
                    driver.findElement(By.id("option-2")).sendKeys("B");
                    driver.findElement(By.id("vote_limit")).sendKeys("1");
                    new WebDriverWait(driver, Duration.ofSeconds(3))
                            .until(ExpectedConditions.elementToBeClickable(By.id("submit")));
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
        inputSpec.setValuesInForm(createForm).beforeClickElement("BUTTON").withAttribute("id", "submit");

        /* get pool address */
        inputSpec.inputField(FormInput.InputType.CUSTOMIZE, new Identification(Identification.How.xpath, "//DIV[@id" +
                "='eth_address']/BUTTON[1]"))
                .setInputFiller((driver, webElement, nodeElement) -> {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click()", webElement);
                    WebElement startBtn = new WebDriverWait(driver, Duration.ofSeconds(3))
                            .until(ExpectedConditions.elementToBeClickable(By.xpath("//BUTTON[text()='Start Poll']")));
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click()", startBtn);
                });

        builder.crawlRules().click("DIV").underXPath("//DIV[contains(@class, 'option_click')]");

        builder.crawlRules().setInputSpec(inputSpec);
        builder.setBrowserConfig(
                new BrowserConfiguration(EmbeddedBrowser.BrowserType.CHROME_EXISTING, chromeDebuggerAddress));
        builder.addPlugin(new GRPCClientPlugin(DAPP_NAME, instanceId, METAMASK_POPUP_URL, DAPP_URL, METAMASK_PASSWORD));


        return new CrawljaxRunner(builder.build());
    }

    public static void main(String[] args) throws IOException {
        new PublicVotesExperiment().start("scripts/coverage", "scripts" + File.separator + "status.log", "localhost:9222");
    }
}
