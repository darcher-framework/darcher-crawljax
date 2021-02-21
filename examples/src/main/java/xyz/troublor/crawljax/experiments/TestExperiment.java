package xyz.troublor.crawljax.experiments;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.*;
import com.crawljax.plugins.crawloverview.CrawlOverview;
import org.kristen.crawljax.plugins.grpc.GRPCClientPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public class TestExperiment extends Experiment {
    private static final long WAIT_TIME_AFTER_EVENT = 500;
    private static final long WAIT_TIME_AFTER_RELOAD = 500;
    private static final String DAPP_URL = "file:///home/troublor/Desktop/testhtml/index.html";
    private static final String DAPP_NAME = "Test DApp";
    private static int instanceId = 1;
    private static final String METAMASK_POPUP_URL = "chrome-extension://jbppcachblnkaogkgacckpgohjbpcekf/home.html";
    private static final String METAMASK_PASSWORD = "12345678";
    private static final String BROWSER_PROFILE_PATH = "/Users/troublor/workspace/darcher_mics/browsers/Chrome/UserData";


    /**
     * Run this method to start the crawl.
     */
    public CrawljaxRunner initialize(Path coverageDir, String chromeDebuggerAddress) {
        CrawljaxConfiguration.CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(DAPP_URL);

        builder.crawlRules().setFormFillMode(CrawlRules.FormFillMode.NORMAL);
        builder.crawlRules().clickOnce(false);
        builder.crawlRules().setCrawlPriorityMode(CrawlRules.CrawlPriorityMode.RANDOM);
        // click these elements
        builder.crawlRules().click("A");
        builder.crawlRules().click("BUTTON");

        builder.crawlRules().crawlHiddenAnchors(true);
        builder.crawlRules().crawlFrames(false);
        builder.setUnlimitedCrawlDepth();
        builder.setUnlimitedRuntime();
        builder.setUnlimitedStates();

        // 1 hour timeout
        builder.setMaximumRunTime(1, TimeUnit.MINUTES);

        builder.crawlRules().clickElementsInRandomOrder(false);

        // Set timeouts
        builder.crawlRules().waitAfterReloadUrl(WAIT_TIME_AFTER_RELOAD, TimeUnit.MILLISECONDS);
        builder.crawlRules().waitAfterEvent(WAIT_TIME_AFTER_EVENT, TimeUnit.MILLISECONDS);

//        builder.setBrowserConfig(
//                new BrowserConfiguration(EmbeddedBrowser.BrowserType.CHROME, 1,
//                        new BrowserOptions(BROWSER_PROFILE_PATH)));
        builder.setBrowserConfig(
                new BrowserConfiguration(EmbeddedBrowser.BrowserType.CHROME_EXISTING, chromeDebuggerAddress));

        // CrawlOverview
        builder.addPlugin(new CrawlOverview());
//        builder.addPlugin(new MetaMaskSupportPlugin(METAMASK_POPUP_URL, METAMASK_PASSWORD));
        builder.addPlugin(new GRPCClientPlugin(DAPP_NAME, instanceId, METAMASK_POPUP_URL, DAPP_URL, METAMASK_PASSWORD));

        return new CrawljaxRunner(builder.build());
    }

    public static void main(String[] args) throws IOException {
        new TestExperiment().start("scripts/coverage", "scripts" + File.separator + "status.log", "localhost:9222");
    }
}
