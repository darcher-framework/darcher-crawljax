package xyz.troublor.crawljax.experiments;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.*;
import com.crawljax.core.state.Identification;
import com.crawljax.forms.FormInput;
import org.kristen.crawljax.plugins.grpc.GRPCClientPlugin;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MetaMaskExperiment extends Experiment {
    private static final long WAIT_TIME_AFTER_EVENT = 500;
    private static final long WAIT_TIME_AFTER_RELOAD = 500;
    private static final String DAPP_URL = "chrome-extension://kdaoeelmbdcinklhldlcmmgmndjcmjpp/home.html";
    private static final String DAPP_NAME = "Metamask";
    private static int instanceId = 1;
    private static final String METAMASK_POPUP_URL = "chrome-extension://kdaoeelmbdcinklhldlcmmgmndjcmjpp/home.html";
    private static final String METAMASK_PASSWORD = "12345678";


    /**
     * Run this method to start the crawl.
     */
    public CrawljaxRunner initialize(Path coverageDir, String chromeDebuggerAddress) {
        CrawljaxConfiguration.CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(DAPP_URL);

//        builder.crawlRules().setFormFillMode(CrawlRules.FormFillMode.RANDOM);
//        builder.crawlRules().click("div").withAttribute("")
        // we use normal mode to avoid randomly fill forms and only allow predefined form inputs
        builder.crawlRules().setFormFillMode(CrawlRules.FormFillMode.NORMAL);
        builder.crawlRules().clickOnce(true);
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

        /* Don't change network */
        builder.crawlRules().dontClick("DIV").withAttribute("class", "network-component pointer");

        /* Don't change account */
        builder.crawlRules().dontClick("DIV").withAttribute("class", "account-menu__icon");

        /* Don't click account options */
        builder.crawlRules().dontClick("BUTTON").withAttribute("title", "Account Options");

        /* Don't click BUY ETH */
        builder.crawlRules().dontClick("BUTTON").withText("Buy");

        /* Go to Assets Tab */
        builder.crawlRules().click("LI").withAttribute("data-testid", "home__asset-tab");

        /* Click Send recipient */
        builder.crawlRules().click("DIV").withAttribute("class", "send__select-recipient-wrapper__group-item");

        /* Send Asset form */
        Form sendForm = new Form();
        sendForm.inputField(FormInput.InputType.CUSTOMIZE,
                new Identification(Identification.How.xpath, "//INPUT[@class='unit-input__input']"))
                .setInputFiller((driver, webElement, nodeElement) -> {
                    // set value = 1
                    webElement.clear();
                    webElement.sendKeys("1");

                    // randomly select one asset
                    WebElement assetDropdown = driver.findElement(By.className("send-v2__asset-dropdown__asset"));
                    assetDropdown.click();
                    List<WebElement> assets = new WebDriverWait(driver, Duration.ofMillis(500))
                            .until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.className("send-v2__asset-dropdown__asset")));
                    WebElement asset = assets.get(new Random().nextInt(assets.size() - 1) + 1);
                    asset.click();
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
        inputSpec.setValuesInForm(sendForm).beforeClickElement("BUTTON").withText("Next");

        builder.crawlRules().setInputSpec(inputSpec);
        builder.setBrowserConfig(
                new BrowserConfiguration(EmbeddedBrowser.BrowserType.CHROME_EXISTING, chromeDebuggerAddress));
        builder.addPlugin(new GRPCClientPlugin(DAPP_NAME, instanceId, METAMASK_POPUP_URL, DAPP_URL, METAMASK_PASSWORD));

        return new CrawljaxRunner(builder.build());
    }

    public static void main(String[] args) throws IOException {
        new MetaMaskExperiment().start("scripts/coverage", "scripts" + File.separator + "status.log", "localhost:9222");
    }
}
