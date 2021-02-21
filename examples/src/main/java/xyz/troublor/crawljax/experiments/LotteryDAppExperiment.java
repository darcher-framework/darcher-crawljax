package xyz.troublor.crawljax.experiments;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.*;
import com.crawljax.core.state.Identification;
import com.crawljax.forms.FormInput;
import org.kristen.crawljax.plugins.grpc.GRPCClientPlugin;
import org.openqa.selenium.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class LotteryDAppExperiment extends Experiment {

    private static final long WAIT_TIME_AFTER_EVENT = 500;
    private static final long WAIT_TIME_AFTER_RELOAD = 500;
    private static final String DAPP_URL = "http://localhost:8080";
    private static final String DAPP_NAME = "LotteryDApp";
    private static int instanceId = 1;
    private static final String METAMASK_POPUP_URL = "chrome-extension://kdaoeelmbdcinklhldlcmmgmndjcmjpp/home.html";
    private static final String METAMASK_PASSWORD = "12345678";


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

        Form buyForm = new Form();
        buyForm.inputField(FormInput.InputType.CUSTOMIZE, new Identification(Identification.How.id, "token-amount"))
                .setInputFiller((driver, webElement, nodeElement) -> {
                    webElement.clear();
                    webElement.sendKeys("1");
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    WebElement btn = driver.findElement(By.id("token-amount-btn"));
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
                });
        inputSpec.setValuesInForm(buyForm).beforeClickElement("BUTTON").withAttribute("id", "token-amount-btn");

        Form guessForm = new Form();
        guessForm.inputField(FormInput.InputType.CUSTOMIZE, new Identification(Identification.How.id, "user-guess"))
                .setInputFiller((driver, webElement, nodeElement) -> {
                    int number = new Random().nextInt(10);
                    number = number - 5 + 50;
                    webElement.clear();
                    webElement.sendKeys(String.valueOf(number));
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    WebElement btn = driver.findElement(By.id("user-guess-btn"));
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
                });
        inputSpec.setValuesInForm(guessForm).beforeClickElement("BUTTON").withAttribute("id", "user-guess-btn");

        builder.crawlRules().setInputSpec(inputSpec);
        builder.setBrowserConfig(
                new BrowserConfiguration(EmbeddedBrowser.BrowserType.CHROME_EXISTING, chromeDebuggerAddress));
        builder.addPlugin(new GRPCClientPlugin(DAPP_NAME, instanceId, METAMASK_POPUP_URL, DAPP_URL, METAMASK_PASSWORD));


        return new CrawljaxRunner(builder.build());
    }

    public static void main(String[] args) throws IOException {
        new LotteryDAppExperiment().start("scripts/coverage", "scripts" + File.separator + "status.log", "localhost:9222");
    }
}
