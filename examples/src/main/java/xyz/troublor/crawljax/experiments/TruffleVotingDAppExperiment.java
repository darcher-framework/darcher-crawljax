package xyz.troublor.crawljax.experiments;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.*;
import com.crawljax.core.plugin.OnUrlLoadPlugin;
import com.crawljax.core.state.Identification;
import com.crawljax.forms.FormInput;
import com.crawljax.forms.InputValue;
import org.kristen.crawljax.plugins.grpc.GRPCClientPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TruffleVotingDAppExperiment extends Experiment {

    private static final long WAIT_TIME_AFTER_EVENT = 500;
    private static final long WAIT_TIME_AFTER_RELOAD = 5000;
    private static final String DAPP_URL = "http://localhost:3000";
    private static final String DAPP_NAME = "truffle-voting-dapp";
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

        Form proposalForm = new Form();
        AtomicInteger count = new AtomicInteger(0);
        proposalForm.inputField(FormInput.InputType.DYNAMIC, new Identification(Identification.How.name, "input"))
                .setInputGenerator((driver, webElement, nodeElement) -> new InputValue("Proposal" + count.getAndIncrement()));
        inputSpec.setValuesInForm(proposalForm).beforeClickElement("BUTTON").withText("Save");

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
        new TruffleVotingDAppExperiment().start("scripts/coverage", "scripts" + File.separator + "status.log", "localhost:9222");
    }
}
