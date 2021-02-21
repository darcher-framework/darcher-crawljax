package xyz.troublor.crawljax.experiments;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.*;
import org.kristen.crawljax.plugins.grpc.GRPCClientPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public class AgroChainExperiment extends Experiment {

    private static final long WAIT_TIME_AFTER_EVENT = 500;
    private static final long WAIT_TIME_AFTER_RELOAD = 500;
    private static final String DAPP_URL = "http://localhost:3000";
    private static final String DAPP_NAME = "AgroChain";
    private static int instanceId = 1;
    private static final String METAMASK_POPUP_URL = "chrome-extension://kdaoeelmbdcinklhldlcmmgmndjcmjpp/home.html";
    private static final String METAMASK_PASSWORD = "12345678";

    private static final String mainAccountAddress = "0x6463F93D65391A8B7c98f0fc8439eFD5d38339d9";

    /**
     * Run this method to start the crawl.
     */
    protected CrawljaxRunner initialize(Path coverageDir, String chromeDebuggerAddress) {
        CrawljaxConfiguration.CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(DAPP_URL);

//        builder.crawlRules().setFormFillMode(CrawlRules.FormFillMode.RANDOM);
//        builder.crawlRules().click("div").withAttribute("")
        // we use normal mode to avoid randomly fill forms and only allow predefined form inputs
        builder.crawlRules().setFormFillMode(CrawlRules.FormFillMode.RANDOM);
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

        builder.crawlRules().setInputSpec(inputSpec);
        builder.setBrowserConfig(
                new BrowserConfiguration(EmbeddedBrowser.BrowserType.CHROME_EXISTING, chromeDebuggerAddress));
        builder.addPlugin(new GRPCClientPlugin(DAPP_NAME, instanceId, METAMASK_POPUP_URL, DAPP_URL, METAMASK_PASSWORD));


        return new CrawljaxRunner(builder.build());
    }

    public static void main(String[] args) throws IOException {
        new AgroChainExperiment().start("scripts/coverage", "scripts" + File.separator + "status.log", "localhost:9222");
    }
}
