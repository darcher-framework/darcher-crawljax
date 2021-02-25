package com.darcher.crawljax.experiments;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.*;
import com.crawljax.core.state.Identification;
import com.crawljax.forms.FormInput;
import com.crawljax.forms.InputValue;
import com.darcher.crawljax.grpc.GRPCClientPlugin;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

public class HeiswapExperiment extends Experiment {

    private static final long WAIT_TIME_AFTER_EVENT = 500;
    private static final long WAIT_TIME_AFTER_RELOAD = 500;
    private static final String DAPP_URL = "http://localhost:3000";
    private static final String DAPP_NAME = "Lordsofthesnails";
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

        /* Send form */
        Form sendForm = new Form();
        sendForm.inputField(FormInput.InputType.TEXT,
                new Identification(Identification.How.xpath, "//INPUT[@type='text']"))
                .inputValues(mainAddress);
        inputSpec.setValuesInForm(sendForm).beforeClickElement("BUTTON").withText("Deposit ETH");

        /* Copy token */
        Stack<String> previousTokens = new Stack<>();
        Form copyTokenForm = new Form();
        inputSpec.inputField(FormInput.InputType.CUSTOMIZE,
                new Identification(Identification.How.xpath, "//BUTTON[.='Copy']"))
                .setInputFiller((driver, webElement, nodeElement) -> {
                    webElement.click();
                    WebElement checkBox = driver.findElement(By.xpath("//INPUT[@type='checkbox']"));
                    if (!checkBox.isSelected()) {
                        checkBox.click();
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    Transferable trans = clipboard.getContents(null);
                    if (trans != null) {
                        if (trans.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                            try {
                                String token = (String) trans.getTransferData(DataFlavor.stringFlavor);
                                previousTokens.push(token);
                            } catch (UnsupportedFlavorException | IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
        inputSpec.setValuesInForm(copyTokenForm).beforeClickElement("BUTTON").withText("Confirm you've copied the token");

        /* Withdraw Form */
        Form withdrawForm = new Form();
        withdrawForm.inputField(FormInput.InputType.DYNAMIC,
                new Identification(Identification.How.xpath, "//INPUT[@type='text']"))
                .setInputGenerator((driver, webElement, nodeElement) -> {
                    // uncheck relayer
                    WebElement checkBox = driver.findElement(By.xpath("//INPUT[@type='checkbox']"));
                    if (checkBox.isSelected()) {
                        checkBox.click();
                    }

                    if (previousTokens.empty()) {
                        return new InputValue("");
                    }
                    return new InputValue(previousTokens.pop());
                });
        inputSpec.setValuesInForm(withdrawForm).beforeClickElement("BUTTON").withText("Withdraw ETH");

        builder.crawlRules().setInputSpec(inputSpec);
        builder.setBrowserConfig(
                new BrowserConfiguration(EmbeddedBrowser.BrowserType.CHROME_EXISTING, chromeDebuggerAddress));
        builder.addPlugin(new GRPCClientPlugin(DAPP_NAME, instanceId, METAMASK_POPUP_URL, DAPP_URL, METAMASK_PASSWORD));


        return new CrawljaxRunner(builder.build());
    }

    public static void main(String[] args) throws IOException {
        new HeiswapExperiment().start("scripts/coverage", "scripts" + File.separator + "status.log", "localhost:9222");
    }
}
