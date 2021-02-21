package xyz.troublor.crawljax.experiments;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.*;
import com.crawljax.core.state.Identification;
import com.crawljax.forms.FormInput;
import org.kristen.crawljax.plugins.grpc.GRPCClientPlugin;
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
import java.util.concurrent.atomic.AtomicInteger;

public class AuctionExperiment extends Experiment {

    private static final long WAIT_TIME_AFTER_EVENT = 500;
    private static final long WAIT_TIME_AFTER_RELOAD = 500;
    private static final String DAPP_URL = "http://localhost:8080";
    private static final String DAPP_NAME = "Auction";
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

        Form createForm = new Form();
        AtomicInteger count = new AtomicInteger(0);
        createForm.inputField(FormInput.InputType.CUSTOMIZE, new Identification(Identification.How.id, "title"))
                .setInputFiller((driver, webElement, nodeElement) -> {
                    webElement.sendKeys("Test Auction " + count.getAndIncrement());
                    driver.findElement(By.id("startPrice")).sendKeys(String.valueOf(new Random().nextInt(1) + 1));
                    switchMetamaskAccount(driver, "Default0");
                    WebElement btn = driver.findElement(By.xpath("/html/body/div/div[3]/div/button"));
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
                });
        inputSpec.setValuesInForm(createForm).beforeClickElement("BUTTON").underXPath("/html/body/div/div[3]/div/button");

        Form bidForm = new Form();

        bidForm.inputField(FormInput.InputType.CUSTOMIZE, new Identification(Identification.How.id, "__BVID__5"))
                .setInputFiller((driver, webElement, nodeElement) -> {
                    WebElement finalizeBtn;
                    try {
                        finalizeBtn = driver.findElement(By.xpath("//BUTTON[contains(text(),'finalized')]"));
                    } catch (NoSuchElementException ignored) {
                        switchMetamaskAccount(driver, "Account 1");
                        webElement.sendKeys("2");
                        WebElement btn = driver.findElement(By.xpath("//BUTTON[text()='place BID']"));
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
                    } catch (Exception ignored) {
                    }
                });
        inputSpec.setValuesInForm(bidForm).beforeClickElement("BUTTON").withText("place BID");

        Form finalizeForm = new Form();
        finalizeForm.inputField(FormInput.InputType.CUSTOMIZE, new Identification(Identification.How.xpath, "/HTML[1]/BODY[1]/DIV[1]/DIV[4]/DIV[1]/ARTICLE[1]/DIV[1]/BUTTON[1]"))
                .setInputFiller((driver, webElement, nodeElement) -> {
                    if (!webElement.getText().contains("finalized")) {
                        switchMetamaskAccount(driver, "Default0");
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", webElement);
                    }
                });
        inputSpec.setValuesInForm(finalizeForm).beforeClickElement("BUTTON").underXPath("/HTML[1]/BODY[1]/DIV[1]/DIV[4]/DIV[1]/ARTICLE[1]/DIV[1]/BUTTON[1]");

        /* Don't repeat finalize auction*/
        builder.crawlRules().dontClick("BUTTON").underXPath("//BUTTON[contains(text(),'finalized')]");

        builder.crawlRules().setInputSpec(inputSpec);
        builder.setBrowserConfig(
                new BrowserConfiguration(EmbeddedBrowser.BrowserType.CHROME_EXISTING, chromeDebuggerAddress));
        builder.addPlugin(new GRPCClientPlugin(DAPP_NAME, instanceId, METAMASK_POPUP_URL, DAPP_URL, METAMASK_PASSWORD));


        return new CrawljaxRunner(builder.build());
    }

    private void switchMetamaskAccount(WebDriver driver, String accountName) {
        String originalTab = driver.getWindowHandle();
        driver.switchTo().newWindow(WindowType.TAB);
        driver.get(METAMASK_POPUP_URL);

        // reject txs if any
        new WebDriverWait(driver, Duration.ofHours(1)).until(d -> {
            try {
                WebElement main = d.findElement(By.className("main-container"));
                if (main != null) {
                    return true;
                }
            } catch (Exception ignored) {
            }
            try {
                WebElement cancelBtn = driver.findElement(By.xpath("//BUTTON[@data-testid='page-container-footer-cancel']"));
                if (cancelBtn != null) {
                    cancelBtn.click();
                }
            } catch (Exception ignored) {
            }
            return false;
        });

        WebElement element = driver.findElement(By.className("account-menu__icon"));
        element.click();
        WebElement dropDown = new WebDriverWait(driver, Duration.ofMinutes(1))
                .until(ExpectedConditions.presenceOfElementLocated(By.className("account-menu")));
        List<WebElement> accountItems = dropDown.findElements(By.className("account-menu__account"));
        for (WebElement account : accountItems) {
            try {
                WebElement name = account.findElement(By.className("account-menu__name"));
                String text = name.getText();
                if (!text.contains(accountName)) {
                    continue;
                }
                account.click();
                new WebDriverWait(driver, Duration.ofMinutes(1))
                        .until(d -> {
                            WebElement elem = d.findElement(By.className("selected-account__name"));
                            return elem.getText().contains(accountName);
                        });
                break;
            } catch (Exception ignored) {
            }
        }

        driver.close();
        driver.switchTo().window(originalTab);
    }

    public static void main(String[] args) throws IOException {
        new AuctionExperiment().start("scripts/coverage", "scripts" + File.separator + "status.log", "localhost:9222");
    }
}
