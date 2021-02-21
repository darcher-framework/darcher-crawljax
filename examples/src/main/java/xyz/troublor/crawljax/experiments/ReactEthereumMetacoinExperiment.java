package xyz.troublor.crawljax.experiments;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.*;
import com.crawljax.core.state.Identification;
import com.crawljax.forms.FormInput;
import org.kristen.crawljax.plugins.grpc.GRPCClientPlugin;
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

public class ReactEthereumMetacoinExperiment extends Experiment {

    private static final long WAIT_TIME_AFTER_EVENT = 1000;
    private static final long WAIT_TIME_AFTER_RELOAD = 500;
    private static final String DAPP_URL = "http://localhost:3000";
    private static final String DAPP_NAME = "publicvotes";
    private static int instanceId = 1;
    private static final String METAMASK_POPUP_URL = "chrome-extension://kdaoeelmbdcinklhldlcmmgmndjcmjpp/home.html";
    private static final String METAMASK_PASSWORD = "12345678";

    private static final String mainAddress = "0x6463F93D65391A8B7c98f0fc8439eFD5d38339d9";
    private static final String otherAddress = "0xBA394B1eAFCBBcE84939103E2f443B80111be596";

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

        Form transferForm = new Form();
        transferForm.inputField(FormInput.InputType.CUSTOMIZE,
                new Identification(Identification.How.xpath, "//INPUT[@type='checkbox']"))
                .setInputFiller((driver, webElement, nodeElement) -> {
                    if (webElement.isSelected()) {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click()", webElement);
                    }
                    WebElement from = new WebDriverWait(driver, Duration.ofSeconds(1))
                            .until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//INPUT[@placeholder='From account hash']")));
                    from.clear();
                    from.sendKeys(mainAddress);
                });
        transferForm.inputField(FormInput.InputType.TEXT,
                new Identification(Identification.How.xpath, "//INPUT[@placeholder='To Account hash']"))
                .inputValues(otherAddress);
        transferForm.inputField(FormInput.InputType.TEXT,
                new Identification(Identification.How.xpath, "//INPUT[@placeholder='Amount of token to transfer']"))
                .inputValues("1");
        inputSpec.setValuesInForm(transferForm).beforeClickElement("BUTTON").withText("Send");



        builder.crawlRules().setInputSpec(inputSpec);
        builder.setBrowserConfig(
                new BrowserConfiguration(EmbeddedBrowser.BrowserType.CHROME_EXISTING, chromeDebuggerAddress));
        builder.addPlugin(new GRPCClientPlugin(DAPP_NAME, instanceId, METAMASK_POPUP_URL, DAPP_URL, METAMASK_PASSWORD));


        return new CrawljaxRunner(builder.build());
    }

    public static void main(String[] args) throws IOException {
        new ReactEthereumMetacoinExperiment().start("scripts/coverage", "scripts" + File.separator + "status.log", "localhost:9222");
    }
}
