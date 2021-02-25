package com.darcher.crawljax.experiments;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.*;
import com.crawljax.core.state.Identification;
import com.crawljax.forms.FormInput;
import com.crawljax.forms.InputValue;
import com.darcher.crawljax.grpc.GRPCClientPlugin;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class EthereumVotingDAppExperiment extends Experiment {

    private static final long WAIT_TIME_AFTER_EVENT = 500;
    private static final long WAIT_TIME_AFTER_RELOAD = 500;
    private static final String DAPP_URL = "http://localhost:8080";
    private static final String DAPP_NAME = "ethereum-voting-dapp";
    private static int instanceId = 1;
    private static final String METAMASK_POPUP_URL = System.getenv("METAMASK_URL");
    private static final String METAMASK_PASSWORD = System.getenv("METAMASK_PASSWORD");

    private static final String mainAccount = "Default0";

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

        GRPCClientPlugin grpcClientPlugin = new GRPCClientPlugin(DAPP_NAME, instanceId, METAMASK_POPUP_URL, DAPP_URL,
                METAMASK_PASSWORD);

        String[] candidates = new String[]{"Alice", "Bob", "Carol"};
        String[] voteData = new String[3]; // [candidate, voter address, signature]
        Form voteForm = new Form();
        voteForm.inputField(FormInput.InputType.DYNAMIC,
                new Identification(Identification.How.id, "candidate-name"))
                .setInputGenerator((driver, webElement, nodeElement) -> {
                    changeMetamaskAccount(driver, null);
                    // generate VoteData
                    String target = candidates[new Random().nextInt(candidates.length)];
                    WebElement input = driver.findElement(By.id("candidate"));
                    input.clear();
                    input.sendKeys(target);
                    WebElement voteBtn = new WebDriverWait(driver, Duration.ofSeconds(1))
                            .until(ExpectedConditions.elementToBeClickable(By.xpath("//BUTTON[@onclick='voteForCandidate()']")));
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click()", voteBtn);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // open metamask and sign
                    String originalTab = driver.getWindowHandle();
                    driver.switchTo().newWindow(WindowType.TAB);
                    driver.get(METAMASK_POPUP_URL);

                    try {
                        WebElement signBtn = new WebDriverWait(driver, Duration.ofSeconds(3))
                                .until(ExpectedConditions.elementToBeClickable(
                                        By.xpath("//BUTTON[@data-testid='request-signature__sign']")));
                        signBtn.click();
                        new WebDriverWait(driver, Duration.ofHours(1)).until(d -> {
                            try {
                                WebElement loading = driver.findElement(By.className("loading-overlay"));
                                return loading == null;
                            } catch (NoSuchElementException ignored) {
                                return true;
                            }
                        });
                    } catch (Exception e) {
                        return new InputValue("");
                    }

                    driver.close();
                    driver.switchTo().window(originalTab);

                    // get the signed data
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    voteData[0] = driver.findElement(By.id("vote-for")).getText().trim().substring(11);
                    voteData[1] = driver.findElement(By.id("addr")).getText().trim().substring(9);
                    voteData[2] = driver.findElement(By.id("signature")).getText().trim().substring(11);

                    changeMetamaskAccount(driver, mainAccount);

                    return new InputValue(voteData[0]);
                });
        voteForm.inputField(FormInput.InputType.DYNAMIC,
                new Identification(Identification.How.id, "voter-address"))
                .setInputGenerator((driver, webElement, nodeElement) -> new InputValue(voteData[1]));
        voteForm.inputField(FormInput.InputType.DYNAMIC,
                new Identification(Identification.How.id, "voter-signature"))
                .setInputGenerator((driver, webElement, nodeElement) -> new InputValue(voteData[2]));
        inputSpec.setValuesInForm(voteForm).beforeClickElement("BUTTON").withText("Submit Vote");

        builder.crawlRules().dontClick("BUTTON").withText("Vote");

        builder.crawlRules().setInputSpec(inputSpec);
        builder.setBrowserConfig(
                new BrowserConfiguration(EmbeddedBrowser.BrowserType.CHROME_EXISTING, chromeDebuggerAddress));
        builder.addPlugin(grpcClientPlugin);

        return new CrawljaxRunner(builder.build());
    }

    private void changeMetamaskAccount(WebDriver driver, String accountName) {
        String originalTab = driver.getWindowHandle();
        driver.switchTo().newWindow(WindowType.TAB);
        driver.get(METAMASK_POPUP_URL);

        WebElement element = new WebDriverWait(driver, Duration.ofSeconds(3))
                .until(ExpectedConditions.elementToBeClickable(By.className("account-menu__icon")));
        element.click();
        WebElement dropDown = new WebDriverWait(driver, Duration.ofSeconds(1))
                .until(ExpectedConditions.presenceOfElementLocated(By.className("account-menu")));
        List<WebElement> accountItems = dropDown.findElements(By.className("account-menu__account"));
        if (accountName == null) {
            WebElement account = accountItems.get(new Random().nextInt(accountItems.size()));
            try {
                WebElement name = account.findElement(By.className("account-menu__name"));
                accountName = name.getText();
                account.click();
                new WebDriverWait(driver, Duration.ofSeconds(1))
                        .until(ExpectedConditions.textToBePresentInElementLocated(
                                By.className("selected-account__name"), accountName));
            } catch (Exception ignored) {
            }
        } else {
            for (WebElement account : accountItems) {
                try {
                    WebElement name = account.findElement(By.className("account-menu__name"));
                    String text = name.getText();
                    if (!text.contains(accountName)) {
                        continue;
                    }
                    account.click();
                    new WebDriverWait(driver, Duration.ofSeconds(1))
                            .until(ExpectedConditions.textToBePresentInElementLocated(
                                    By.className("selected-account__name"), accountName));
                    break;
                } catch (Exception ignored) {
                }
            }
        }

        driver.close();
        driver.switchTo().window(originalTab);
    }

    public static void main(String[] args) throws IOException {
        new EthereumVotingDAppExperiment().start("scripts/coverage", "scripts" + File.separator + "status.log", "localhost:9222");
    }
}
