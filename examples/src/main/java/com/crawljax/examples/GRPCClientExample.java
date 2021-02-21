package com.crawljax.examples;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawlTaskConsumer;
import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.*;
import com.crawljax.core.state.Identification;
import com.crawljax.forms.FormInput;
import com.crawljax.plugins.crawloverview.CrawlOverview;
import xyz.troublor.crawljax.plugins.metamask.MetaMaskSupportPlugin;
import org.kristen.crawljax.plugins.grpc.GRPCClientPlugin;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GRPCClientExample {
    private static final long WAIT_TIME_AFTER_EVENT = 500;
    private static final long WAIT_TIME_AFTER_RELOAD = 500;
    private static final String DAPP_URL = "chrome-extension://nkbihfbeogaeaoehlefnkodbefgpgknn/home.html#send";
    private static final String DAPP_NAME = "Metamask";
    private static int instanceId = 1;
    private static final String METAMASK_POPUP_URL = "chrome-extension://nkbihfbeogaeaoehlefnkodbefgpgknn/home.html";
    private static final String METAMASK_PASSWORD = "gRP'b~jz|zz;DA7~[[P9";
    private static final String BROWSER_PROFILE_PATH = "/Users/shuqing/Documents/application";


    /**
     * Run this method to start the crawl.
     *
     * @throws IOException when the output folder cannot be created or emptied.
     */
    public static void main(String[] args) throws IOException {
        CrawljaxConfiguration.CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(DAPP_URL);

//        builder.crawlRules().setFormFillMode(CrawlRules.FormFillMode.RANDOM);
//        builder.crawlRules().click("div").withAttribute("")
        // we use normal mode to avoid randomly fill forms and only allow predefined form inputs
        builder.crawlRules().setFormFillMode(CrawlRules.FormFillMode.NORMAL);
        builder.crawlRules().clickOnce(false);
        builder.crawlRules().setCrawlPriorityMode(CrawlRules.CrawlPriorityMode.RANDOM);
        // click these elements
//        builder.crawlRules().clickDefaultElements();
//        CrawljaxConfiguration crawler = new CrawljaxConfiguration();
//        crawler.click("a");
//        crawler.click("div").withAttribute("class", "clickable");
//        crawler.dontClick("a").withText("id", "logout");
//        crawler.dontClick("a").underXpath("//DIV[@id='header']");
        builder.crawlRules().click("A");
        builder.crawlRules().click("BUTTON");
//        builder.crawlRules().click("div");
//        builder.crawlRules().click("div").underXPath("//*[@onclick]");

        builder.crawlRules().crawlHiddenAnchors(true);
        builder.crawlRules().crawlFrames(false);
        builder.setUnlimitedCrawlDepth();
        builder.setUnlimitedRuntime();
        builder.setUnlimitedStates();

        // 1 hour timeout
        builder.setMaximumRunTime(1, TimeUnit.HOURS);

        //builder.setMaximumStates(10);
        //builder.setMaximumDepth(3);
        builder.crawlRules().clickElementsInRandomOrder(false);

        // Set timeouts
        builder.crawlRules().waitAfterReloadUrl(WAIT_TIME_AFTER_RELOAD, TimeUnit.MILLISECONDS);
        builder.crawlRules().waitAfterEvent(WAIT_TIME_AFTER_EVENT, TimeUnit.MILLISECONDS);

        // click "Transfer between my accounts"
//        builder.crawlRules().click("A").withText("Transfer between my accounts");
        // click the transfer recipient accounts from the list of "My Accounts"
        builder.crawlRules().click("DIV").withAttribute("class", "send__select-recipient-wrapper__group-item");
        // click to change the asset when transferring, make it possible to transfer ERC20 token
        builder.crawlRules().click("DIV").withAttribute("class", "send-v2__asset-dropdown");
        builder.crawlRules().click("DIV").withAttribute("class", "send-v2__asset-dropdown__input-wrapper");
        builder.crawlRules().click("DIV").withAttribute("class", "send-v2__asset-dropdown__asset");
        // click home page asset tab
        builder.crawlRules().click("LI").withAttribute("data-testid", "home__asset-tab");
        builder.crawlRules().click("LI").withAttribute("data-testid", "home__activity-tab");

        builder.crawlRules().click("DIV").withAttribute("class", "send-v2__asset-dropdown__asset");
//        //TODO
//        builder.crawlRules().click("BUTTON").withAttribute("data-testid", "page-container-footer-next");
        // prevent removing an account
        builder.crawlRules().dontClick("BUTTON").withAttribute("data-testid", "account-options-menu__remove-account");
        // don't bother buy ether
        builder.crawlRules().dontClick("BUTTON").withText("Buy");
        // don't change network
        builder.crawlRules().dontClick("DIV").withAttribute("class", "network-component pointer");
//        // don't select ETH again when sending assets
//        builder.crawlRules().dontClick("DIV").withText("ETH");
        // don't change account
        builder.crawlRules().dontClick("DIV").withAttribute("class", "account-menu__icon");
//        // don't send to my self
//        builder.crawlRules().dontClick("DIV").underXPath("//DIV[@class='send__select-recipient-wrapper__group']/DIV[2" +
//                "]");
//        builder.crawlRules().dontClick("BUTTON").withText("Send");
        // don't leave send transaction page
//        builder.crawlRules().dontClick("BUTTON").withAttribute("data-testid", "page-container-footer-cancel");
        builder.crawlRules().dontClick("A").withText("Cancel");
        // form input specifications
        InputSpecification input = new InputSpecification();
        // fill recipient address when sending ether
//        Identification recipientInputId = new Identification(Identification.How.xpath, "//INPUT[@class='ens" +
//                "-input__wrapper__input']");
//        input.inputField(FormInput.InputType.TEXT, recipientInputId).inputValues(
//                "0x2ecB718297080fF730269176E42C8278aA193434");
//         fill the amount of ether to transfer
        Identification amountId = new Identification(Identification.How.xpath, "//INPUT[@class='unit" +
                "-input__input']");
        input.inputField(FormInput.InputType.NUMBER, amountId).inputValues("1");

        // fill search tokens
        Identification searchTokenId = new Identification(Identification.How.id, "search-tokens");
        input.inputField(FormInput.InputType.TEXT, searchTokenId).inputValues("0x1AADAa0620e0306156d8aF95E56b92e48eF1e6b8");

        builder.crawlRules().setInputSpec(input);

        builder.setBrowserConfig(
                new BrowserConfiguration(EmbeddedBrowser.BrowserType.CHROME, 1,
                        new BrowserOptions(BROWSER_PROFILE_PATH)));

        // CrawlOverview
        builder.addPlugin(new CrawlOverview());
//        builder.addPlugin(new MetaMaskSupportPlugin(METAMASK_POPUP_URL, METAMASK_PASSWORD));
        builder.addPlugin(new GRPCClientPlugin(DAPP_NAME, instanceId, METAMASK_POPUP_URL, DAPP_URL, METAMASK_PASSWORD));

        CrawljaxRunner crawljax = new CrawljaxRunner(builder.build());
        CrawlTaskConsumer consumer = crawljax.callRtnConsumer();
        System.out.println("Crawl Complete: " + crawljax.getReason());
        consumer.crawler.close();
    }
}
