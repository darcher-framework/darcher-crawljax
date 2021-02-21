package xyz.troublor.crawljax.experiments;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.*;
import com.crawljax.core.state.Identification;
import com.crawljax.forms.FormInput;
import com.crawljax.forms.InputValue;
import com.crawljax.plugins.crawloverview.CrawlOverview;
import org.kristen.crawljax.plugins.grpc.GRPCClientPlugin;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.w3c.dom.Node;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class GivethForeignExperiment extends Experiment {
    private static final long WAIT_TIME_AFTER_EVENT = 500;
    private static final long WAIT_TIME_AFTER_RELOAD = 500;
    private static final String DAPP_URL = "http://localhost:3010/";
    private static final String DAPP_NAME = "Giveth Foreign";
    private static int instanceId = 1;
    private static final String METAMASK_POPUP_URL = "chrome-extension://jbppcachblnkaogkgacckpgohjbpcekf/home.html";
    private static final String METAMASK_PASSWORD = "12345678";
    private static final String BROWSER_PROFILE_PATH = "/Users/troublor/workspace/darcher_mics/browsers/Chrome/UserData";

    // DApp Gloabal Variables
    private static final String ETHEREUM_ADDRESS = "0x90F8bf6A479f320ead074411a4B0e7944Ea8c9C1";
    private static final String OTHER_ADDRESS = "0xFFcf8FDEE72ac11b5c542428B35EEF5769C409f0";


    /**
     * Run this method to start the crawl.
     *
     * @throws IOException when the output folder cannot be created or emptied.
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

        /* don't click unrelated buttons */
        builder.crawlRules().dontClick("A").withText(" Join Giveth");
        builder.crawlRules().dontClick("BUTTON").withText("Draft Saved");
        builder.crawlRules().dontClick("A").withText("Tech Support");
        builder.crawlRules().dontClick("A").withAttribute("id", "navbarDropdownYou");

        // form input specifications
        InputSpecification inputSpec = new InputSpecification();

        inputSpec.inputField(FormInput.InputType.TEXT, new Identification(Identification.How.name, "picture"))
                .inputValues("/Users/troublor/workspace/darcher/packages/darcher-examples/giveth/misc/picture.png");

        /* don't click editor tool bar */
        builder.crawlRules().dontClick("BUTTON").underXPath("//*[@id=\"quill-formsy\"]/DIV[2]/DIV[3]");
        builder.crawlRules().dontClick("A").underXPath("//*[@id=\"quill-formsy\"]/DIV[2]/DIV[3]");
        // don't click hidden things
        builder.crawlRules().dontClick("BUTTON").underXPath("//*[@id=\"quill-formsy\"]/DIV[1]");
        builder.crawlRules().dontClick("A").underXPath("//*[@id=\"quill-formsy\"]/DIV[1]");

        /* Create DAC form */
        Form createDACForm = new Form();
        AtomicInteger dacCount = new AtomicInteger(0);
        // campaign name
        createDACForm.inputField(FormInput.InputType.DYNAMIC, new Identification(Identification.How.id, "title-input"))
                .setInputGenerator((driver, webElement, nodeElement) -> new InputValue("DAC" + dacCount.getAndIncrement()));
        // campaign description
        createDACForm.inputField(FormInput.InputType.CUSTOMIZE, new Identification(Identification.How.xpath, "//*[@id=\"quill-formsy\"]/DIV[2]/DIV[1]"))
                .setInputFiller((driver, webElement, nodeElement) -> webElement.sendKeys("Several descriptions here..."));
        // campaign picture
        createDACForm.inputField(FormInput.InputType.TEXT, new Identification(Identification.How.name, "picture"))
                .inputValues("/Users/troublor/workspace/darcher/packages/darcher-examples/giveth/misc/picture.png");
        // attach create campaign form at BUTTON[@text='Create DAC']
        inputSpec.setValuesInForm(createDACForm).beforeClickElement("BUTTON").withText("Create DAC");

        /* new create Campaign form */
        Form createCampaignForm = new Form();
        AtomicInteger campaignCount = new AtomicInteger(0);
        // campaign name
        createCampaignForm.inputField(FormInput.InputType.DYNAMIC, new Identification(Identification.How.id, "title-input"))
                .setInputGenerator((driver, webElement, nodeElement) -> new InputValue("Campaign" + campaignCount.getAndIncrement()));
        // campaign description
        createCampaignForm.inputField(FormInput.InputType.CUSTOMIZE, new Identification(Identification.How.xpath, "//*[@id=\"quill-formsy\"]/DIV[2]/DIV[1]"))
                .setInputFiller((driver, webElement, nodeElement) -> webElement.sendKeys("Several descriptions here..."));
        // campaign picture
        createCampaignForm.inputField(FormInput.InputType.TEXT, new Identification(Identification.How.name, "picture"))
                .inputValues("/Users/troublor/workspace/darcher/packages/darcher-examples/giveth/misc/picture.png");
        // select reviewer
        createCampaignForm.inputField(FormInput.InputType.SELECT, new Identification(Identification.How.name, "reviewerAddress"))
                .inputValues(ETHEREUM_ADDRESS); // select Giveth0 account as reviewer always
        // attach create campaign form at BUTTON[@text='Create Campaign']
        inputSpec.setValuesInForm(createCampaignForm).beforeClickElement("BUTTON").withText("Create Campaign");

        /* Create Milestone form */
        Form createMileStoneForm = new Form();
        AtomicInteger milestoneCount = new AtomicInteger(0);
        // campaign name
        createMileStoneForm.inputField(FormInput.InputType.DYNAMIC, new Identification(Identification.How.id, "title-input"))
                .setInputGenerator((driver, webElement, nodeElement) -> new InputValue("Milestone" + milestoneCount.getAndIncrement()));
        // campaign description
        createMileStoneForm.inputField(FormInput.InputType.CUSTOMIZE, new Identification(Identification.How.xpath, "//*[@id=\"quill-formsy\"]/DIV[2]/DIV[1]"))
                .setInputFiller((driver, webElement, nodeElement) -> {
                    webElement.sendKeys("Several descriptions here...");
                });
        // campaign picture
        createMileStoneForm.inputField(FormInput.InputType.TEXT, new Identification(Identification.How.name, "picture"))
                .inputValues("/Users/troublor/workspace/darcher/packages/darcher-examples/giveth/misc/picture.png");
        // select reviewer
        createMileStoneForm.inputField(FormInput.InputType.SELECT, new Identification(Identification.How.name, "reviewerAddress"))
                .inputValues(ETHEREUM_ADDRESS); // select Giveth0 account as reviewer always
        // select money destination after completion
//        createMileStoneForm.inputField(FormInput.InputType.TEXT, new Identification(Identification.How.name, "recipientAddress"))
//                .inputValues(ETHEREUM_ADDRESS);
        createMileStoneForm.inputField(FormInput.InputType.CUSTOMIZE, new Identification(Identification.How.name,
                "recipientAddress"))
                .setInputFiller((driver, webElement, nodeElement) -> {
                    webElement.sendKeys(ETHEREUM_ADDRESS);
                });
        // no need to click "Use My Address" anymore
        builder.crawlRules().dontClick("BUTTON").withText("Use My Address");
        // set maximum amount
        createMileStoneForm.inputField(FormInput.InputType.NUMBER, new Identification(Identification.How.name, "fiatAmount"))
                .inputValues("1");
        // attach create campaign form at BUTTON[@text='Create Milestone']
        inputSpec.setValuesInForm(createMileStoneForm).beforeClickElement("BUTTON").withText("Create Milestone");

        /* cancel campaign confirmation input */
        inputSpec.inputField(FormInput.InputType.DYNAMIC, new Identification(Identification.How.xpath, "/HTML/BODY/DIV/DIV/DIV[4]/DIV/INPUT"))
                .setInputGenerator((driver, webElement, nodeElement) -> {
                    // fill the confirmation input by copy the answer from previous sibling node
                    Node node = nodeElement;
                    while (!node.getNodeName().toUpperCase().equals("B")) {
                        node = node.getPreviousSibling();
                    }
                    String answer;
                    try {
                        int end = Math.min(node.getTextContent().trim().length(), 5);
                        answer = node.getTextContent().trim().substring(0, end);
                    } catch (Exception e) {
                        answer = "Campa";
                    }
                    return new InputValue(answer);
                });

        /* Change Milestone Recipient */
        inputSpec.inputField(FormInput.InputType.DYNAMIC,
                new Identification(Identification.How.xpath, "//INPUT[@class='swal-content__input']"))
                .setInputGenerator((driver, webElement, nodeElement) -> {
                    if (nodeElement.getAttribute("placeholder").contains("recipient address")) {
                        return new InputValue(OTHER_ADDRESS);
                    } else {
                        return null;
                    }
                });

        /* Delegate Donation Form */
        Form delegateDonationForm = new Form();
        // delegate 1 ETH
        delegateDonationForm.inputField(FormInput.InputType.DYNAMIC,
                new Identification(Identification.How.name, "amount"))
                .setInputGenerator((driver, webElement, nodeElement) -> {
                    // click delegate destination
                    WebElement element = webElement;
                    while (!element.getTagName().toLowerCase().equals("form")) {
                        element = element.findElement(By.xpath("./.."));
                    }
                    // randomly click one type of delegate destination
                    List<WebElement> tokenInputs = element.findElements(By.xpath("//div[@class='ReactTokenInput']"));
                    if (tokenInputs.size() > 0) {
                        WebElement reactTokenInput = tokenInputs.get(new Random().nextInt(tokenInputs.size()));
                        reactTokenInput.click();
                        try {
                            new WebDriverWait(driver, Duration.ofSeconds(1))
                                    .until(ExpectedConditions.elementToBeClickable(
                                            reactTokenInput.findElement(By.xpath("//div[@class='ReactTokenInput__option']"))));
                        } catch (TimeoutException ignored) {
                        }

                        // randomly click one destination
                        List<WebElement> options = reactTokenInput.findElements(By.className("ReactTokenInput__option"));
                        if (options.size() > 0) {
                            options.get(new Random().nextInt(options.size())).click();
                        }
                    }

                    // decide delegate value
                    WebElement rangeSlide = element.findElement(By.className("rangeslider"));
                    double verySmallValue = 0.001;
                    if (rangeSlide == null) {
                        return new InputValue(String.valueOf(verySmallValue));
                    }
                    // get max value
                    double maxValue;
                    try {
                        maxValue = Double.parseDouble(rangeSlide.getAttribute("aria-valuemax"));
                    } catch (NumberFormatException e) {
                        maxValue = verySmallValue;
                    }
                    return new InputValue(String.valueOf(maxValue > 1 ? 1 : maxValue));
                });
        // click source
        builder.crawlRules().click("DIV").withAttribute("class", "ReactTokenInput");
        builder.crawlRules().click("DIV").withAttribute("class", "ReactTokenInput__option");
        inputSpec.setValuesInForm(delegateDonationForm).beforeClickElement("BUTTON").withText("Delegate here");

        /* Cancel Milestone Form */
        Form cancelMilestoneForm = new Form();
        cancelMilestoneForm.inputField(FormInput.InputType.CUSTOMIZE,
                new Identification(Identification.How.xpath, "//*[@id=\"quill-formsy\"]/DIV[2]/DIV[1]"))
                .setInputFiller((driver, webElement, nodeElement) -> webElement.sendKeys("Cancel milestone"));
        inputSpec.setValuesInForm(cancelMilestoneForm).beforeClickElement("BUTTON").withText("Cancel Milestone");

        /* Change ownership form */
        Form changeOwnershipForm = new Form();
        changeOwnershipForm.inputField(FormInput.InputType.SELECT,
                new Identification(Identification.How.name, "ownerAddress"))
                .inputValues(OTHER_ADDRESS);
        inputSpec.setValuesInForm(changeOwnershipForm).beforeClickElement("BUTTON").withText("Change ownership");

        /* Don't download CSV */
        builder.crawlRules().dontClick("BUTTON").withText("Download CSV");

        /* Click DIV button */
        builder.crawlRules().click("DIV").withAttribute("role", "button");

        /* don't click share buttons */
        builder.crawlRules().dontClick("DIV").withAttribute("class", "SocialMediaShareButton SocialMediaShareButton--facebook");
        builder.crawlRules().dontClick("DIV").withAttribute("class", "SocialMediaShareButton SocialMediaShareButton--twitter");
        builder.crawlRules().dontClick("DIV").withAttribute("class", "SocialMediaShareButton SocialMediaShareButton--telegram");
        builder.crawlRules().dontClick("DIV").withAttribute("class", "SocialMediaShareButton SocialMediaShareButton--linkedin");

        /* Don't click Donate button, which are not usable in Foreign network */
        builder.crawlRules().dontClick("BUTTON").withText("Donate");

        /* No need to edit (no tx involved) */
        builder.crawlRules().dontClick("BUTTON").underXPath("//BUTTON[contains(text(), 'Edit')]");
        builder.crawlRules().dontClick("A").underXPath("//BUTTON[contains(text(), 'Edit')]");

        /* Profile view is excluded to reduce search space */
        builder.crawlRules().dontClick("BUTTON").underXPath("//*[@id=\"profile-view\"]");
        builder.crawlRules().dontClick("A").underXPath("//*[@id=\"profile-view\"]");

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
        new GivethForeignExperiment().start("scripts/coverage", "scripts" + File.separator + "status.log", "localhost:9222");
    }
}
