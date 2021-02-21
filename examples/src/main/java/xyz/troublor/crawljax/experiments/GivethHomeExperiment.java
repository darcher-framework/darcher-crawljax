package xyz.troublor.crawljax.experiments;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.*;
import com.crawljax.core.state.Identification;
import com.crawljax.forms.FormInput;
import com.crawljax.plugins.crawloverview.CrawlOverview;
import org.kristen.crawljax.plugins.grpc.GRPCClientPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public class GivethHomeExperiment extends Experiment {
    private static final long WAIT_TIME_AFTER_EVENT = 500;
    private static final long WAIT_TIME_AFTER_RELOAD = 500;
    private static final String DAPP_URL = "http://localhost:3010/";
    private static final String DAPP_NAME = "Giveth Home";
    private static int instanceId = 1;
    private static final String METAMASK_POPUP_URL = "chrome-extension://jbppcachblnkaogkgacckpgohjbpcekf/home.html";
    private static final String METAMASK_PASSWORD = "12345678";
    private static final String BROWSER_PROFILE_PATH = "/Users/troublor/workspace/darcher_mics/browsers/Chrome/UserData";

    // DApp Gloabal Variables
    private static final String ETHEREUM_ADDRESS = "0x90F8bf6A479f320ead074411a4B0e7944Ea8c9C1";
    private static final String OTHER_ADDRESS = "0xFFcf8FDEE72ac11b5c542428B35EEF5769C409f0";


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

        /* don't click share buttons */
        builder.crawlRules().dontClick("DIV").withAttribute("class", "SocialMediaShareButton SocialMediaShareButton--facebook");
        builder.crawlRules().dontClick("DIV").withAttribute("class", "SocialMediaShareButton SocialMediaShareButton--twitter");
        builder.crawlRules().dontClick("DIV").withAttribute("class", "SocialMediaShareButton SocialMediaShareButton--telegram");
        builder.crawlRules().dontClick("DIV").withAttribute("class", "SocialMediaShareButton SocialMediaShareButton--linkedin");

        /* Don't click Foreign Network Buttons */
        builder.crawlRules().dontClick("A").underXPath("//*[@id=\"join-giveth-community\"]/div/center/a");
        builder.crawlRules().dontClick("BUTTON").withText("Crate a Community");
        builder.crawlRules().dontClick("BUTTON").withText("Start a Campaign");
        builder.crawlRules().dontClick("BUTTON").withText("Delegate funds here");
        builder.crawlRules().dontClick("BUTTON").withText("Change ownership");
        builder.crawlRules().dontClick("BUTTON").withText("Download CSV");
        builder.crawlRules().dontClick("A").withText("Add Milestone");
        builder.crawlRules().dontClick("A").withText("Tech Support");
        builder.crawlRules().dontClick("A").withAttribute("id", "navbarDropdownYou");

        /* No need to edit (no tx involved) */
        builder.crawlRules().dontClick("BUTTON").underXPath("//BUTTON[contains(text(), 'Edit')]");
        builder.crawlRules().dontClick("A").underXPath("//BUTTON[contains(text(), 'Edit')]");

        /* donate */
        Form donateForm = new Form();
        donateForm.inputField(FormInput.InputType.NUMBER, new Identification(Identification.How.id, "amount-input"))
                .inputValues("1");
        inputSpec.setValuesInForm(donateForm).beforeClickElement("BUTTON").underXPath("//BUTTON[.='Donate' and @type='submit']");

        builder.crawlRules().setInputSpec(inputSpec);
        builder.setBrowserConfig(
                new BrowserConfiguration(EmbeddedBrowser.BrowserType.CHROME_EXISTING, chromeDebuggerAddress));

        // CrawlOverview
        builder.addPlugin(new CrawlOverview());
//        builder.addPlugin(new MetaMaskSupportPlugin(METAMASK_POPUP_URL, METAMASK_PASSWORD));
        builder.addPlugin(new GRPCClientPlugin(DAPP_NAME, instanceId, METAMASK_POPUP_URL, DAPP_URL, METAMASK_PASSWORD));

        // test zone
//        builder.crawlRules().click("BUTTON").withText("Delegate funds here");

        return new CrawljaxRunner(builder.build());
    }

    public static void main(String[] args) throws IOException {
        new GivethHomeExperiment().start("scripts/coverage", "scripts" + File.separator + "status.log", "localhost:9222");
    }
}
