package xyz.troublor.crawljax.experiments;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.*;
import com.crawljax.core.state.Identification;
import com.crawljax.forms.FormInput;
import com.crawljax.forms.InputValue;
import org.kristen.crawljax.plugins.grpc.GRPCClientPlugin;
import org.openqa.selenium.*;
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

public class GivethExperiment extends Experiment {

    private static final long WAIT_TIME_AFTER_EVENT = 500;
    private static final long WAIT_TIME_AFTER_RELOAD = 500;
    private static final String DAPP_URL = "http://localhost:3010";
    private static final String DAPP_NAME = "Giveth";
    private static int instanceId = 1;
    private static final String METAMASK_POPUP_URL = "chrome-extension://jbppcachblnkaogkgacckpgohjbpcekf/home.html";
    private static final String METAMASK_PASSWORD = "12345678";

    private static final String ETHEREUM_ADDRESS = "0x90F8bf6A479f320ead074411a4B0e7944Ea8c9C1";
    private static final String OTHER_ADDRESS = "0xFFcf8FDEE72ac11b5c542428B35EEF5769C409f0";
    private static final String PICTURE_PATH = "/Users/troublor/workspace/darcher/packages/darcher-examples/giveth/misc/picture.png";

    private static String currentNetwork = "foreign";

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
        builder.setMaximumRunTime(30, TimeUnit.MINUTES);

//        builder.setMaximumStates(0); // unlimited
        builder.setMaximumDepth(0); // unlimited
        builder.crawlRules().clickElementsInRandomOrder(true);

        /* Set timeouts */
        builder.crawlRules().waitAfterReloadUrl(WAIT_TIME_AFTER_RELOAD, TimeUnit.MILLISECONDS);
        builder.crawlRules().waitAfterEvent(WAIT_TIME_AFTER_EVENT, TimeUnit.MILLISECONDS);

        InputSpecification inputSpec = new InputSpecification();

        /* don't click unrelated buttons */
        builder.crawlRules().dontClick("A").withText(" Join Giveth");
        builder.crawlRules().dontClick("BUTTON").withText("Draft Saved");
        builder.crawlRules().dontClick("A").withText("Tech Support");
        builder.crawlRules().dontClick("A").withAttribute("id", "navbarDropdownYou");

        inputSpec.inputField(FormInput.InputType.TEXT, new Identification(Identification.How.name, "picture"))
                .inputValues(PICTURE_PATH);

        /* don't click editor tool bar */
        builder.crawlRules().dontClick("BUTTON").underXPath("//*[@id=\"quill-formsy\"]/DIV[2]/DIV[3]");
        builder.crawlRules().dontClick("A").underXPath("//*[@id=\"quill-formsy\"]/DIV[2]/DIV[3]");
        // don't click hidden things
        builder.crawlRules().dontClick("BUTTON").underXPath("//*[@id=\"quill-formsy\"]/DIV[1]");
        builder.crawlRules().dontClick("A").underXPath("//*[@id=\"quill-formsy\"]/DIV[1]");

        builder.crawlRules().click("BUTTON").withText("Create a Community").when(browser -> {
            try {
                List<WebElement> cards =
                        browser.getWebDriver().findElements(By.id("campaigns-view")).get(0)
                                .findElements(By.cssSelector(".card"));
                return cards.size() < 2;
            } catch (Exception ignored) {
                return false;
            }
        });
        builder.crawlRules().click("BUTTON").withText("Start a Campaign").when(browser -> {
            try {
                List<WebElement> cards =
                        browser.getWebDriver().findElements(By.id("campaigns-view")).get(1)
                                .findElements(By.cssSelector(".card"));
                return cards.size() < 2;
            } catch (Exception ignored) {
                return false;
            }
        });

        /* Create Profile Form */
        Form createProfileForm = new Form();
        createProfileForm.inputField(FormInput.InputType.TEXT, new Identification(Identification.How.id, "name-input"))
                .inputValues("Giveth0");
        createProfileForm.inputField(FormInput.InputType.TEXT, new Identification(Identification.How.name, "picture"))
                .inputValues(PICTURE_PATH);
        inputSpec.setValuesInForm(createProfileForm).beforeClickElement("BUTTON").withText("Save profile");

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
                .inputValues(PICTURE_PATH);
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
                .inputValues(PICTURE_PATH);
        // select reviewer
        createCampaignForm.inputField(FormInput.InputType.SELECT, new Identification(Identification.How.name, "reviewerAddress"))
                .inputValues(ETHEREUM_ADDRESS); // select Giveth0 account as reviewer always
        // attach create campaign form at BUTTON[@text='Create Campaign']
        inputSpec.setValuesInForm(createCampaignForm).beforeClickElement("BUTTON").withText("Create Campaign");

        /* Create Milestone form */
        Form createMileStoneForm = new Form();
        AtomicInteger milestoneCount = new AtomicInteger(0);
        // Milestone name
        createMileStoneForm.inputField(FormInput.InputType.DYNAMIC, new Identification(Identification.How.id, "title-input"))
                .setInputGenerator((driver, webElement, nodeElement) -> new InputValue("Milestone" + milestoneCount.getAndIncrement()));
        // Milestone description
        createMileStoneForm.inputField(FormInput.InputType.CUSTOMIZE, new Identification(Identification.How.xpath, "//*[@id=\"quill-formsy\"]/DIV[2]/DIV[1]"))
                .setInputFiller((driver, webElement, nodeElement) -> {
                    webElement.sendKeys("Several descriptions here...");
                });
        // Milestone picture
        createMileStoneForm.inputField(FormInput.InputType.TEXT, new Identification(Identification.How.name, "picture"))
                .inputValues(PICTURE_PATH);
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
        createMileStoneForm.inputField(FormInput.InputType.CUSTOMIZE, new Identification(Identification.How.name,
                "fiatAmount"))
                .setInputFiller((driver, webElement, nodeElement) -> {
                    webElement.sendKeys("1");
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
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

        /* No need to edit (no tx involved) */
        builder.crawlRules().dontClick("BUTTON").underXPath("//BUTTON[contains(text(), 'Edit')]");
        builder.crawlRules().dontClick("A").underXPath("//BUTTON[contains(text(), 'Edit')]");

        /* Profile view is excluded to reduce search space */
        builder.crawlRules().dontClick("BUTTON").underXPath("//*[@id=\"profile-view\"]");
        builder.crawlRules().dontClick("A").underXPath("//*[@id=\"profile-view\"]");
        builder.crawlRules().dontClick("A").withText("Profile");

        /* donate */
        Form donateForm = new Form();
        donateForm.inputField(FormInput.InputType.NUMBER, new Identification(Identification.How.id, "amount-input"))
                .inputValues("1");
        inputSpec.setValuesInForm(donateForm).beforeClickElement("BUTTON").underXPath("//BUTTON[.='Donate' and @type='submit']");


        builder.crawlRules().setInputSpec(inputSpec);
        builder.setBrowserConfig(
                new BrowserConfiguration(EmbeddedBrowser.BrowserType.CHROME_EXISTING, chromeDebuggerAddress));
        builder.addPlugin(new GRPCClientPlugin(DAPP_NAME, instanceId, METAMASK_POPUP_URL, DAPP_URL, METAMASK_PASSWORD));

//        builder.addPlugin(new OnUrlLoadPlugin() {
//            @Override
//            public void onUrlLoad(CrawlerContext context) {
//                // change network to foreign with 0.9 possibility
//                double v = new Random().nextDouble();
//                if (v > 1) {
//                    if (!currentNetwork.equals("home")) {
//                        changeNetwork(context.getBrowser().getWebDriver(), "Localhost 8545");
////                        changeMetamaskAccount(context.getBrowser().getWebDriver(), "Giveth1");
//                    }
//
//                    currentNetwork = "home";
//                } else {
//                    if (!currentNetwork.equals("foreign")) {
//                        changeNetwork(context.getBrowser().getWebDriver(), "Localhost 8546");
////                        changeMetamaskAccount(context.getBrowser().getWebDriver(), "Giveth0");
//                    }
//                    currentNetwork = "foreign";
//                }
//            }
//
//            private void changeNetwork(WebDriver driver, String networkName) {
//                String originalTab = driver.getWindowHandle();
//                driver.switchTo().newWindow(WindowType.TAB);
//                driver.get(METAMASK_POPUP_URL);
//
//                WebElement element = new WebDriverWait(driver, Duration.ofSeconds(3))
//                        .until(ExpectedConditions.elementToBeClickable(By.className("network-component")));
//                element.click();
//                WebElement dropDown = new WebDriverWait(driver, Duration.ofSeconds(1))
//                        .until(ExpectedConditions.presenceOfElementLocated(By.className("network-droppo")));
//                List<WebElement> networkItems = dropDown.findElements(By.tagName("li"));
//
//                for (WebElement network : networkItems) {
//                    try {
//                        String text = network.getText();
//                        if (!text.contains(networkName)) {
//                            continue;
//                        }
//                        network.click();
//                        new WebDriverWait(driver, Duration.ofSeconds(1))
//                                .until(ExpectedConditions.textToBePresentInElementLocated(
//                                        By.className("network-name"), networkName));
//                        break;
//                    } catch (Exception ignored) {
//                    }
//                }
//
//                driver.close();
//                driver.switchTo().window(originalTab);
//            }
//
//            private void changeMetamaskAccount(WebDriver driver, String accountName) {
//                String originalTab = driver.getWindowHandle();
//                driver.switchTo().newWindow(WindowType.TAB);
//                driver.get(METAMASK_POPUP_URL);
//
//                WebElement element = new WebDriverWait(driver, Duration.ofSeconds(3))
//                        .until(ExpectedConditions.elementToBeClickable(By.className("account-menu__icon")));
//                element.click();
//                WebElement dropDown = new WebDriverWait(driver, Duration.ofSeconds(1))
//                        .until(ExpectedConditions.presenceOfElementLocated(By.className("account-menu")));
//                List<WebElement> accountItems = dropDown.findElements(By.className("account-menu__account"));
//                if (accountName == null) {
//                    WebElement account = accountItems.get(new Random().nextInt(accountItems.size()));
//                    try {
//                        WebElement name = account.findElement(By.className("account-menu__name"));
//                        accountName = name.getText();
//                        account.click();
//                        new WebDriverWait(driver, Duration.ofSeconds(1))
//                                .until(ExpectedConditions.textToBePresentInElementLocated(
//                                        By.className("selected-account__name"), accountName));
//                    } catch (Exception ignored) {
//                    }
//                } else {
//                    for (WebElement account : accountItems) {
//                        try {
//                            WebElement name = account.findElement(By.className("account-menu__name"));
//                            String text = name.getText();
//                            if (!text.contains(accountName)) {
//                                continue;
//                            }
//                            account.click();
//                            new WebDriverWait(driver, Duration.ofSeconds(1))
//                                    .until(ExpectedConditions.textToBePresentInElementLocated(
//                                            By.className("selected-account__name"), accountName));
//                            break;
//                        } catch (Exception ignored) {
//                        }
//                    }
//                }
//
//                driver.close();
//                driver.switchTo().window(originalTab);
//            }
//        });

        return new CrawljaxRunner(builder.build());
    }

    public static void main(String[] args) throws IOException {
        new GivethExperiment().start("scripts/coverage", "scripts" + File.separator + "status.log", "localhost:9222");
    }
}
