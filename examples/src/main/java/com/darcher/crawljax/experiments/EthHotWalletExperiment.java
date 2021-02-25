package com.darcher.crawljax.experiments;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.*;
import com.crawljax.core.plugin.OnFireEventSucceededPlugin;
import com.crawljax.core.plugin.OnUrlLoadPlugin;
import com.crawljax.core.state.Identification;
import com.crawljax.forms.FormInput;
import com.darcher.crawljax.grpc.GRPCClientPlugin;
import com.darcher.crawljax.rpc.DappTestService;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class EthHotWalletExperiment extends Experiment {

    private static final long WAIT_TIME_AFTER_EVENT = 500;
    private static final long WAIT_TIME_AFTER_RELOAD = 500;
    private static final String DAPP_URL = "http://localhost:3001";
    private static final String DAPP_NAME = "eth-hot-wallet";
    private static int instanceId = 1;
    private static final String METAMASK_POPUP_URL = System.getenv("METAMASK_URL");
    private static final String METAMASK_PASSWORD = System.getenv("METAMASK_PASSWORD");

    private static final String targetAddress = "0x6463F93D65391A8B7c98f0fc8439eFD5d38339d9";
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

        GRPCClientPlugin grpcClientPlugin = new GRPCClientPlugin(DAPP_NAME, instanceId, METAMASK_POPUP_URL, DAPP_URL,
                METAMASK_PASSWORD);

        /* Don't lock or close*/
        builder.crawlRules().dontClick("BUTTON").underXPath("//*[@id=\"app\"]/div/div/div[1]/div[1]");
        builder.crawlRules().dontClick("BUTTON").underXPath("//*[@id=\"app\"]/div/div/div[1]/header/div/div[2]/div/button");
        builder.crawlRules().dontClick("A").underXPath("/html/body/div[3]");

        /* Don't add address */
        builder.crawlRules().dontClick("BUTTON").underXPath("//*[@id=\"app\"]/div/div/div[1]/div[2]/div/div/div[2]/button[1]");

        Form sendForm = new Form();
        sendForm.inputField(FormInput.InputType.NUMBER,
                new Identification(Identification.How.xpath, "//INPUT[@role='spinbutton']"))
                .inputValues("1");
        sendForm.inputField(FormInput.InputType.TEXT,
                new Identification(Identification.How.xpath, "//INPUT[@placeholder='Send to address']"))
                .inputValues(targetAddress);
        inputSpec.setValuesInForm(sendForm).beforeClickElement("BUTTON").withText("Create transaction");

        builder.crawlRules().setInputSpec(inputSpec);
        builder.setBrowserConfig(
                new BrowserConfiguration(EmbeddedBrowser.BrowserType.CHROME_EXISTING, chromeDebuggerAddress));
        builder.addPlugin(grpcClientPlugin);

        builder.addPlugin((OnFireEventSucceededPlugin) (context, eventable, pathToFailure) -> {
            boolean isTxBtn = eventable.getElement().getText().contains("Send ETH");
            if (isTxBtn) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                DappTestService.TxMsg txMsg = DappTestService.TxMsg.newBuilder()
                        .setDappName(DAPP_NAME)
                        .setInstanceId(Integer.toString(instanceId))
                        .setHash("")
                        .setFrom("")
                        .setTo("")
                        .setStates("")
                        .setEvents("")
                        .build();
                grpcClientPlugin.blockingStub.waitForTxProcess(txMsg);
            }
        });

        // change network to local rpc
        builder.addPlugin((OnUrlLoadPlugin) context -> {
            WebDriver driver = context.getBrowser().getWebDriver();
            WebElement networkBtn = new WebDriverWait(driver, Duration.ofSeconds(1))
                    .until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"app\"]/div/div/div[1]/header/div/div[2]/div/button")));
            networkBtn.click();
            WebElement localRPC = new WebDriverWait(driver, Duration.ofSeconds(1))
                    .until(ExpectedConditions.elementToBeClickable(By.xpath("//A[text()='Local RPC']")));
            localRPC.click();
            new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(d->{
                        WebElement loading = d.findElement(By.xpath("//*[@id=\"app\"]/div/div/div[1]/header/div/div[2]/div/span"));
                        try{
                            loading.findElement(By.tagName("I"));
                            return false;
                        }catch (NoSuchElementException ignored){
                            return true;
                        }
                    });
            WebElement unlockBtn = driver.findElement(By.xpath("//*[@id=\"app\"]/div/div/div[1]/div[1]/button[1]"));
            if (unlockBtn.getText().contains("Unlock")) {
                unlockBtn.click();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                String current = driver.getWindowHandle();
                Alert alert = driver.switchTo().alert();
                alert.sendKeys(METAMASK_PASSWORD);
                alert.accept();
                driver.switchTo().window(current);
            }
            new WebDriverWait(driver, Duration.ofSeconds(1))
                    .until(d ->{
                        WebElement lockBtn = driver.findElement(By.xpath("//*[@id=\"app\"]/div/div/div[1]/div[1" +
                                "]/button[1]"));
                        return !lockBtn.getText().contains("Unlock");
                    });
        });

        return new CrawljaxRunner(builder.build());
    }

    public static void main(String[] args) throws IOException {
        new EthHotWalletExperiment().start("scripts/coverage", "scripts" + File.separator + "status.log", "localhost:9222");
    }
}
