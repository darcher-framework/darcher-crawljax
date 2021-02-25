package com.darcher.crawljax.experiments;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.*;
import com.crawljax.core.plugin.OnUrlLoadPlugin;
import com.crawljax.core.state.Identification;
import com.crawljax.forms.FormInput;
import com.darcher.crawljax.grpc.GRPCClientPlugin;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class AugurExperiment extends Experiment {
    private static final long WAIT_TIME_AFTER_EVENT = 500;
    private static final long WAIT_TIME_AFTER_RELOAD = 500;
    private static final String DAPP_URL = "http://localhost:8080/";
    private static final String DAPP_NAME = "Augur";
    private static int instanceId = 1;
    private static final String METAMASK_POPUP_URL = System.getenv("METAMASK_URL");
    private static final String METAMASK_PASSWORD = System.getenv("METAMASK_PASSWORD");

    // DApp Gloabal Variables
    private static final String ETHEREUM_ADDRESS = "0x913dA4198E6bE1D5f5E4a40D0667f70C0B5430Eb";
    private static final String OTHER_ADDRESS = "0x9D4C6d4B84cd046381923C9bc136D6ff1FE292D9";


    /**
     * Run this method to start the crawl.
     **/
    public CrawljaxRunner initialize(Path coverageDir, String chromeDebuggerAddress) {
        CrawljaxConfiguration.CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(DAPP_URL);

//        builder.crawlRules().setFormFillMode(CrawlRules.FormFillMode.RANDOM);
//        builder.crawlRules().click("div").withAttribute("")
        // we use normal mode to avoid randomly fill forms and only allow predefined form inputs
        builder.crawlRules().setFormFillMode(CrawlRules.FormFillMode.NORMAL);
        builder.crawlRules().clickOnce(true);
        // click these elements
//        builder.crawlRules().click("A");
//        builder.crawlRules().click("BUTTON");

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

        /* Go into markets */
//        builder.crawlRules().click("A").underXPath("//H3[@class='market-common-styles_MarketTemplateTitle']");
//        builder.crawlRules().click("A").underXPath("//H3[@class='common-styles_OutcomeGroup']");
        builder.crawlRules().click("A").underXPath("//ARTICLE[@data-testid='markets']");

        /* creating markets (create customized market is clicked)*/
        inputSpec.inputField(FormInput.InputType.CUSTOMIZE,
                new Identification(Identification.How.xpath, "//BUTTON[@class='SingleDatePickerInput_calendarIcon SingleDatePickerInput_calendarIcon_1']"))
                .setInputFiller((driver, webElement, nodeElement) -> {
                    // change market type randomly
                    List<WebElement> typesBtn = driver.findElements(By.cssSelector(".form-styles_RadioCard"));
                    WebDriverTools.click(driver, typesBtn.get(new Random().nextInt(typesBtn.size())));

                    Date now = new Date();
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(now);
                    int today = cal.get(Calendar.DAY_OF_MONTH);
                    int monthMaxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

                    // click date picker
                    WebDriverTools.click(driver, webElement);
                    WebElement picker = new WebDriverWait(driver, Duration.ofMillis(100))
                            .until(d -> d.findElement(
                                    By.xpath("//DIV[@class='SingleDatePicker_picker SingleDatePicker_picker_1 SingleDatePicker_picker__directionLeft SingleDatePicker_picker__directionLeft_2']")));
                    // find tomorrow button
                    if (today > monthMaxDays - 15) {
                        // next month
                        WebElement nextMonthBtn = picker.findElement(By.cssSelector(
                                ".DayPickerNavigation_rightButton__horizontal"));
                        WebDriverTools.click(driver, nextMonthBtn);
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    cal.add(Calendar.DAY_OF_YEAR, 15);
                    int tomorrow = cal.get(Calendar.DAY_OF_MONTH);
                    String monthStr = new SimpleDateFormat("MMMM").format(cal.getTime());
                    List<WebElement> elements =
                            picker.findElements(By.xpath("//BUTTON[text()='" + tomorrow + "']"));
                    for (WebElement elem :
                            elements) {
                        if (elem.getAttribute("aria-label").contains(monthStr)) {
                            ((JavascriptExecutor) driver).executeScript("arguments[0].click()", elem);
                            break;
                        }
                    }

                    // time picker
                    WebElement timePicker = driver.findElement(By.cssSelector(".form-styles_TimeSelector")).findElement(By.tagName("BUTTON"));
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click()", timePicker);

                    // Market question
                    List<WebElement> textAreas = driver.findElements(By.tagName("TEXTAREA"));
                    for (WebElement textArea :
                            textAreas) {
                        String placeHolder = textArea.getAttribute("placeholder");
                        if (placeHolder.startsWith("Example")) {
                            textArea.sendKeys("Is DArcher useful to test DApps?");
                            break;
                        }
                    }

                    // Market Category
                    WebElement category = driver.findElement(By.xpath("//UL[@class='form-styles_CategoryMultiSelect" +
                            "']/LI[1]/*/DIV[@role" +
                            "='button']"));
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click()", category);
                    WebElement entertainment = new WebDriverWait(driver, Duration.ofMillis(100))
                            .until(d -> d.findElement(By.xpath("//UL[@class='form-styles_CategoryMultiSelect']/LI[1]//BUTTON[@value='Entertainment']")));
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click()", entertainment);
                    WebElement secondaryCategoryBtn = new WebDriverWait(driver, Duration.ofMillis(100))
                            .until(d -> d.findElement(By.xpath("//UL[@class='form-styles_CategoryMultiSelect']/LI[2]/*/DIV[@role='button']")));
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click()", secondaryCategoryBtn);
                    WebElement awards = new WebDriverWait(driver, Duration.ofMillis(100))
                            .until(d -> d.findElement(By.xpath("//UL[@class='form-styles_CategoryMultiSelect']/LI[2]//BUTTON[@value='Awards']")));
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click()", awards);
                    WebElement subCategoryBtn = new WebDriverWait(driver, Duration.ofMillis(100))
                            .until(d -> d.findElement(By.xpath("//UL[@class='form-styles_CategoryMultiSelect']/LI[3]/*/DIV[@role='button']")));
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click()", subCategoryBtn);
                    WebElement academicAwards = new WebDriverWait(driver, Duration.ofMillis(100))
                            .until(d -> d.findElement(By.xpath("//UL[@class='form-styles_CategoryMultiSelect']/LI[3]//BUTTON[@value='Academy Awards']")));
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click()", academicAwards);

                    // for Multiple-Choice Types of market
                    try {
                        List<WebElement> outcomeInputs =
                                driver.findElements(By.xpath("//INPUT[@placeholder='Enter outcome']"));
                        String[] outcomes = new String[]{"Yes", "No"};
                        for (int i = 0; i < outcomeInputs.size(); i++) {
                            outcomeInputs.get(i).sendKeys(outcomes[i % 2]);
                        }
                    } catch (NoSuchElementException ignored) {
                    }

                    // for scalar markets
                    try {
                        WebElement denominationInput =
                                driver.findElement(By.xpath("//INPUT[@placeholder='Denomination']"));
                        denominationInput.sendKeys("1");
                    } catch (NoSuchElementException ignored) {
                    }
                });
        builder.crawlRules().click("BUTTON").withText("Create Market");
        builder.crawlRules().click("BUTTON").withText("Create a custom market");
        builder.crawlRules().click("BUTTON").withAttribute("title", "Next");
        builder.crawlRules().click("BUTTON").withAttribute("title", "Create");
        builder.crawlRules().click("BUTTON").withAttribute("title", "Confirm");

        /* Don't click view txs button in account summary */
        builder.crawlRules().dontClick("BUTTON").withAttribute("title", "View Transactions");

        /* Don't click save draft */
        builder.crawlRules().dontClick("BUTTON").withAttribute("title", "Save draft");

        /* Don't click Back */
        builder.crawlRules().dontClick("BUTTON").withAttribute("title", "Back");

        /* Don't preview market */
        builder.crawlRules().dontClick("BUTTON").withAttribute("title", "Preview your market");

        /* Click Add Funds and click Convert */
        builder.crawlRules().click("BUTTON").withAttribute("title", "Deposit");
        builder.crawlRules().click("DIV").underXPath("//DIV[@role='button' and contains(., 'Convert')]");

        /* Convert REP to DAI Form */
        Form convertForm = new Form();
        convertForm.inputField(FormInput.InputType.TEXT,
                new Identification(Identification.How.xpath, "//INPUT[@placeholder='0.0000']"))
                .inputValues("1");
        inputSpec.setValuesInForm(convertForm).beforeClickElement("BUTTON").withAttribute("title", "Trade");

        /* Don't click Learn More */
        builder.crawlRules().dontClick("BUTTON").withText("Learn more");

        /* Don't click depth chart */
        builder.crawlRules().dontClick("BUTTON").underXPath("//DIV[@class='depth-styles_MarketOutcomeDepth__container']");

        /* Transfer funds form (same with Withdraw form) */
        Form transferForm = new Form();
        transferForm.inputField(FormInput.InputType.TEXT,
                new Identification(Identification.How.xpath, "//INPUT[@placeholder='0x...']"))
                .inputValues(OTHER_ADDRESS); // transfer to Augur1
        transferForm.inputField(FormInput.InputType.TEXT,
                new Identification(Identification.How.xpath, "//INPUT[@placeholder='0.00']"))
                .inputValues("10");
        inputSpec.setValuesInForm(transferForm).beforeClickElement("BUTTON").withAttribute("title", "Send");
        builder.crawlRules().click("BUTTON").withAttribute("title", "Withdraw");

        /* Faucet button */
        builder.crawlRules().click("BUTTON").withAttribute("title", "REP Faucet");
        builder.crawlRules().click("BUTTON").withAttribute("title", "DAI Faucet");
        builder.crawlRules().click("BUTTON").withAttribute("title", "Legacy REP Faucet");

        /* don't cancel */
        builder.crawlRules().dontClick("BUTTON").withAttribute("title", "Cancel");
        builder.crawlRules().dontClick("BUTTON").withText("cancel");
        builder.crawlRules().dontClick("BUTTON").withText("Cancel");
        builder.crawlRules().dontClick("BUTTON").withText("");

        /* Click Get REP/DAI button */
        builder.crawlRules().click("BUTTON").withAttribute("title", "Get REP");
        builder.crawlRules().click("BUTTON").withAttribute("title", "Get DAI");

        /* Don't click Disputing Guide button */
        builder.crawlRules().dontClick("BUTTON").withAttribute("class", "common-styles_ReportingModalButton");

        builder.crawlRules().click("A").withText("Account Summary");
        builder.crawlRules().click("A").withText("Markets");
        builder.crawlRules().click("A").withText("Portfolio");
        builder.crawlRules().click("A").withText("Disputing");
        builder.crawlRules().click("A").withText("Reporting");
        builder.crawlRules().click("BUTTON").underXPath("//DIV[@class='notification-styles_Message']");

        /* Migrate V1 REP to V2 form */
        builder.crawlRules().click("BUTTON").withAttribute("title", "Migrate V1 to V2 REP");
        builder.crawlRules().click("BUTTON").withAttribute("title", "Migrate");

        /* But Tokens form */
        Form buyTokensForm = new Form();
        buyTokensForm.inputField(FormInput.InputType.TEXT,
                new Identification(Identification.How.xpath, "//INPUT[@placeholder='0.0000']"))
                .inputValues("1");
        inputSpec.setValuesInForm(buyTokensForm).beforeClickElement("BUTTON").withText("buy");
        builder.crawlRules().click("BUTTON").withAttribute("title", "Get Participation Tokens");

        /* Don't click alert button */
        builder.crawlRules().dontClick("BUTTON").withAttribute("class", "top-bar-styles_alerts");

        /* Don't click category filter button */
        builder.crawlRules().dontClick("BUTTON").withAttribute("data-testid", "Category-0");
        builder.crawlRules().dontClick("BUTTON").withAttribute("data-testid", "Category-1");
        builder.crawlRules().dontClick("BUTTON").withAttribute("data-testid", "Category-2");
        builder.crawlRules().dontClick("BUTTON").withAttribute("data-testid", "label-type-0");
        builder.crawlRules().dontClick("BUTTON").withAttribute("data-testid", "label-type-1");
        builder.crawlRules().dontClick("BUTTON").withAttribute("data-testid", "label-type-2");

        /* Don't click share buttons */
        builder.crawlRules().dontClick("BUTTON").withAttribute("id", "facebookButton");
        builder.crawlRules().dontClick("BUTTON").withAttribute("id", "twitterButton");
        builder.crawlRules().dontClick("BUTTON").withAttribute("title", "Toggle Favorite");

        /*Don't click show details button*/
        builder.crawlRules().dontClick("BUTTON").underXPath("//DIV[@class='market-header-styles_MarketDetails']");

        /* Don't click buttons in open order lists */
        builder.crawlRules().dontClick("BUTTON")
                .underXPath("//DIV[@class='market-view-styles_OrdersPane']//DIV[@class='module-tabs-style_Headers']");

        /* Click existing orders */
        builder.crawlRules().click("DIV").underXPath("//SECTION[@class='order-book-styles_OrderBook']/DIV/DIV");

        /* Don't click buttons in trading form */
        builder.crawlRules().dontClick("BUTTON")
                .underXPath("//SECTION[@class='trading-form-styles_TradingForm']/SECTION[1]/DIV[1]");

        /* Don't click expand toggle button*/
        builder.crawlRules().dontClick("BUTTON").withAttribute("class", "buttons-styles_ToggleExtendButton");

        /* Don't click stats buttons*/
        builder.crawlRules().dontClick("BUTTON").underXPath("//DIV[@class='stats-styles_Stats']");

        /* place order */
        Form placeBuyOrderForm = new Form();
        placeBuyOrderForm.inputField(FormInput.InputType.CUSTOMIZE,
                new Identification(Identification.How.id, "quantity"))
                .setInputFiller((driver, webElement, nodeElement) -> {
                    if (webElement.getAttribute("value").equals("")) {
                        webElement.sendKeys("10");
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
        placeBuyOrderForm.inputField(FormInput.InputType.CUSTOMIZE,
                new Identification(Identification.How.id, "limit-price"))
                .setInputFiller((driver, webElement, nodeElement) -> {
                    if (webElement.getAttribute("value").equals("")) {
                        webElement.sendKeys("0.5");
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
        inputSpec.setValuesInForm(placeBuyOrderForm).beforeClickElement("BUTTON").withAttribute("class", "buttons-styles_SellOrderButton");
        inputSpec.setValuesInForm(placeBuyOrderForm).beforeClickElement("BUTTON").withAttribute("class", "buttons-styles_BuyOrderButton");
        builder.crawlRules().click("BUTTON").withAttribute("class", "buttons-styles_BuyOrderButton");
        builder.crawlRules().click("BUTTON").withAttribute("class", "buttons-styles_SellOrderButton");

        builder.crawlRules().setInputSpec(inputSpec);
        builder.setBrowserConfig(
                new BrowserConfiguration(EmbeddedBrowser.BrowserType.CHROME_EXISTING, chromeDebuggerAddress));

        // CrawlOverview
//        builder.addPlugin(new CrawlOverview());
//        builder.addPlugin(new MetaMaskSupportPlugin(METAMASK_POPUP_URL, METAMASK_PASSWORD));
        GRPCClientPlugin grpcClientPlugin = new GRPCClientPlugin(DAPP_NAME, instanceId, METAMASK_POPUP_URL, DAPP_URL,
                METAMASK_PASSWORD);
        builder.addPlugin(grpcClientPlugin);
        builder.addPlugin(new ClientSideCoverageCollectorPlugin(coverageDir));

        // some Augur-specific plugins
        builder.addPlugin((OnUrlLoadPlugin) context -> {
            WebDriver driver = context.getBrowser().getWebDriver();
            System.out.print("Wait for Augur DApp reloading...");
            new WebDriverWait(driver, Duration.ofSeconds(30)).until(d -> {
                try {
                    WebElement sample = driver.findElement(By.xpath("//*[@id=\"app\"]/main/div/section/section[3]/aside/ul/div[1]/div/div[1]/div/span[2]"));
                    try {
                        Integer.parseInt(sample.getText());
                    } catch (NumberFormatException ignored) {
                        return false;
                    }
                    return true;
                } catch (NoSuchElementException e) {
                    try {
                        driver.findElement(By.xpath("//DIV[@class='price-history-styles_PriceHistory']/DIV"));
                        return true;
                    } catch (NoSuchElementException ignored) {
                        return false;
                    }
                }
            });

            // do some initial faucet
            WebElement accountSummaryTab = driver.findElement(By.xpath("//A[.='Account Summary']"));
            WebDriverTools.click(driver, accountSummaryTab);

            WebElement totalAccountValue = new WebDriverWait(driver, Duration.ofSeconds(1))
                    .until(ExpectedConditions.presenceOfElementLocated(
                            By.xpath("//*[@id=\"tabs_undefined\"]/div[2]/div/div/div[2]/section/div[1]")));
            String value = totalAccountValue.getText().substring(1);
            if (Double.parseDouble(value) <= 100) {
                // faucet
                WebElement repFaucetBtn = new WebDriverWait(driver, Duration.ofSeconds(1))
                        .until(ExpectedConditions.elementToBeClickable(By.xpath("//BUTTON[@title='REP Faucet']")));
                WebDriverTools.click(driver, repFaucetBtn);
                WebElement confirmBtn = new WebDriverWait(driver, Duration.ofSeconds(1))
                        .until(ExpectedConditions.elementToBeClickable(By.xpath("//BUTTON[@title='Get REP']")));
                WebDriverTools.click(driver, confirmBtn);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                grpcClientPlugin.checkMetamaskMessage(context, new ArrayList<>());

                accountSummaryTab = driver.findElement(By.xpath("//A[.='Account Summary']"));
                WebDriverTools.click(driver, accountSummaryTab);
                WebElement daiFaucetBtn = new WebDriverWait(driver, Duration.ofSeconds(1))
                        .until(ExpectedConditions.elementToBeClickable(By.xpath("//BUTTON[@title='DAI Faucet']")));
                WebDriverTools.click(driver, daiFaucetBtn);
                confirmBtn = new WebDriverWait(driver, Duration.ofSeconds(1))
                        .until(ExpectedConditions.elementToBeClickable(By.xpath("//BUTTON[@title='Get DAI']")));
                WebDriverTools.click(driver, confirmBtn);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                grpcClientPlugin.checkMetamaskMessage(context, new ArrayList<>());

                accountSummaryTab = driver.findElement(By.xpath("//A[.='Account Summary']"));
                WebDriverTools.click(driver, accountSummaryTab);
                WebElement legacyRepFaucetBtn = new WebDriverWait(driver, Duration.ofSeconds(1))
                        .until(ExpectedConditions.elementToBeClickable(By.xpath("//BUTTON[@title='Legacy REP Faucet']")));
                WebDriverTools.click(driver, legacyRepFaucetBtn);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                grpcClientPlugin.checkMetamaskMessage(context, new ArrayList<>());
            }

            WebElement marketTab = driver.findElement(By.xpath("//A[.='Markets']"));
            WebDriverTools.click(driver, marketTab);

            try {
                Thread.sleep(2500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            System.out.println("done");
        });

        // test zone

        return new CrawljaxRunner(builder.build());
    }

    public static void main(String[] args) throws IOException {
        new AugurExperiment().start("scripts/coverage", "scripts" + File.separator + "status.log", "localhost:9222");
    }
}
