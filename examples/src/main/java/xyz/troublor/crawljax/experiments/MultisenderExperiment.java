package xyz.troublor.crawljax.experiments;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.*;
import com.crawljax.core.plugin.OnUrlLoadPlugin;
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
import java.util.concurrent.TimeUnit;

public class MultisenderExperiment extends Experiment {

    private static final long WAIT_TIME_AFTER_EVENT = 500;
    private static final long WAIT_TIME_AFTER_RELOAD = 500;
    private static final String DAPP_URL = "http://localhost:3000";
    private static final String DAPP_NAME = "multisender";
    private static int instanceId = 1;
    private static final String METAMASK_POPUP_URL = "chrome-extension://jbppcachblnkaogkgacckpgohjbpcekf/home.html";
    private static final String METAMASK_PASSWORD = "12345678";

    private static final String tokenAddress = "0xa5492f0f46914ed52224Eac463e8C02F1DEc89b1";
    private static final String recipient1 = "0x6463F93D65391A8B7c98f0fc8439eFD5d38339d9";
    private static final String recipient2 = "0xa5492f0f46914ed52224Eac463e8C02F1DEc89b1";


    /**
     * Run this method to start the crawl.
     */
    protected CrawljaxRunner initialize(Path coverageDir, String chromeDebuggerAddress) {
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

        Form sendForm = new Form();
        sendForm.inputField(FormInput.InputType.CUSTOMIZE, new Identification(Identification.How.id, "token-address"))
                .setInputFiller((driver, webElement, nodeElement) -> {
                    webElement.sendKeys(tokenAddress);
                    WebElement option = new WebDriverWait(driver, Duration.ofMillis(200))
                            .until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id='react-select-2--list']//DIV[1]")));
                    option.click();
                });
        sendForm.inputField(FormInput.InputType.CUSTOMIZE, new Identification(Identification.How.name, "format"))
                .setInputFiller((driver, webElement, nodeElement) -> {
                    WebElement csvBtn = driver.findElement(By.xpath("//INPUT[@value='csv']"));
                    csvBtn.click();
                    WebElement confirmBtn = new WebDriverWait(driver, Duration.ofMillis(200))
                            .until(ExpectedConditions.elementToBeClickable(By.cssSelector(".swal-button--confirm")));
                    confirmBtn.click();
                });
        sendForm.inputField(FormInput.InputType.TEXTAREA, new Identification(Identification.How.id, "addresses-with-balances"))
                .inputValues(recipient1 + ",1\n" +
                        recipient2 + ",1\n");
        inputSpec.setValuesInForm(sendForm).beforeClickElement("BUTTON").withText("Next");

        builder.crawlRules().setInputSpec(inputSpec);
        builder.setBrowserConfig(
                new BrowserConfiguration(EmbeddedBrowser.BrowserType.CHROME_EXISTING, chromeDebuggerAddress));
        builder.addPlugin(new GRPCClientPlugin(DAPP_NAME, instanceId, METAMASK_POPUP_URL, DAPP_URL, METAMASK_PASSWORD));

        // plugin to wait for loading
        builder.addPlugin((OnUrlLoadPlugin) context -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        return new CrawljaxRunner(builder.build());
    }

    public static void main(String[] args) throws IOException {
        new MultisenderExperiment().start("scripts/coverage", "scripts" + File.separator + "status.log", "localhost:9222");
    }
}
