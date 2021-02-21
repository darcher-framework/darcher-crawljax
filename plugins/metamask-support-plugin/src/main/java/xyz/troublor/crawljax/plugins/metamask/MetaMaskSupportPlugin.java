package xyz.troublor.crawljax.plugins.metamask;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.*;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.plugin.*;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.Identification;
import com.crawljax.core.state.StateVertex;
import com.google.common.collect.ImmutableList;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * This plugin makes it possible to crawl web applications using MetaMask extension.
 * MetaMaskSupportPlugin mainly does two things.
 * Before crawling, it login to the MetaMask extension and starts a new thread monitoring new popup windows of MetaMask
 * Once a popup window of MetaMask is identified, it will automatically click the confirm button (sign tx or agree to
 * connect dapp) to facilitate web testing.
 * After crawling, it will stop the monitor thread.
 */
public class MetaMaskSupportPlugin implements OnBrowserCreatedPlugin, OnFireEventSucceededPlugin, OnUrlFirstLoadPlugin {
    private String METAMASK_POPUP_URL = "chrome-extension://nkbihfbeogaeaoehlefnkodbefgpgknn/popup.html";
    private String METAMASK_PASSWORD = "";

    // how many milliseconds to wait after interacting with the MetaMask popup windows
    private static final int METAMASK_INTERACTION_LATENCY = 1000;

    // TODO: Specific scenario
    // Specific data for DApps
    private static final String AUGUR_URL = "http://localhost:8080";

    public MetaMaskSupportPlugin(String url, String password) {
        this.METAMASK_POPUP_URL = url;
        this.METAMASK_PASSWORD = password;
    }

    /**
     * when browser is created, log in to MetaMask and start a thread to monitor new popup windows of MetaMask
     *
     * @param newBrowser the new created browser object
     */
    @Override
    public void onBrowserCreated(EmbeddedBrowser newBrowser) {
        try {
            newBrowser.goToUrl(new URI(METAMASK_POPUP_URL));
        } catch (URISyntaxException e) {
            System.out.println("ERROR: invalid MetaMask popup url, " + METAMASK_POPUP_URL);
        }
        if (isLogInPage(newBrowser) && !logIn(newBrowser, METAMASK_PASSWORD)) {
            System.out.println("ERROR: MetaMask login failed");
        }

        // TODO: handle other scenarios (specific)
//         Sign up for Augur
        try {
            newBrowser.goToUrl(new URI(AUGUR_URL));

            // Sign up for Augur
            WebDriver driver = newBrowser.getWebDriver();
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
            driver.findElement(By.cssSelector(".buttons-styles_SecondaryButton")).click();
            driver.findElement(By.cssSelector(".buttons-styles_SecondarySignInButton:nth-child(7) > div > div > div:nth-child(1)")).click();
        } catch (URISyntaxException e) {
            System.out.println("ERROR: invalid Augur url, " + METAMASK_POPUP_URL);
        }

    }

    /**
     * Execute `ethereum.enable()` to connect dapp with MetaMask
     *
     * @param context the current crawler context.
     */
    @Override
    public void onUrlFirstLoad(CrawlerContext context) {
        EmbeddedBrowser browser = context.getBrowser();
        WebDriver driver = browser.getWebDriver();
        if (driver instanceof JavascriptExecutor) {
            ((JavascriptExecutor)driver).executeScript("ethereum.enable()");
        } else {
            throw new IllegalStateException("This driver does not support JavaScript!");
        }
    }

    @Override
    public void onFireEventSucceeded(CrawlerContext context, Eventable eventable, List<Eventable> pathToFailure) {
        System.out.println("succeed");
        EmbeddedBrowser browser = context.getBrowser();
        processMetaMaskPopup(browser);
    }

    /**
     * this function will automatically click the primary button in the MetaMask popup window
     *
     * @param browser the browser instance
     */
    private void processMetaMaskPopup(EmbeddedBrowser browser) {

        WebDriver driver = browser.getWebDriver();
        String currentHandle = driver.getWindowHandle();
        ((JavascriptExecutor) driver).executeScript("window.open()");
        ArrayList<String> tabs = new ArrayList<>(driver.getWindowHandles());
        driver.switchTo().window(tabs.get(tabs.indexOf(currentHandle) + 1));
        driver.get(METAMASK_POPUP_URL);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (!isMetaMaskMainPage(browser)) {
            Identification primaryBtnId = new Identification(Identification.How.xpath,
                    "//button[contains(@class, 'btn-primary')]");
            Identification secondaryBtnId = new Identification(Identification.How.xpath,
                    "//button[contains(@class, 'btn-secondary')]");
            Identification defaultBtnId = new Identification(Identification.How.xpath,
                    "//button[contains(@class, 'btn-default')]");
            if (browser.elementExists(primaryBtnId)) {
                WebElement primaryBtn = browser.getWebElement(primaryBtnId);
                primaryBtn.click();
            } else if (browser.elementExists(secondaryBtnId)) {
                WebElement secondaryBtn = browser.getWebElement(secondaryBtnId);
                secondaryBtn.click();
            } else if (browser.elementExists(defaultBtnId)) {
                WebElement defaultBtn = browser.getWebElement(defaultBtnId);
                defaultBtn.click();
            }
        }
        driver.close();
        driver.switchTo().window(currentHandle);
    }

    private boolean isMetaMaskMainPage(EmbeddedBrowser browser) {
        Identification mainId = new Identification(Identification.How.xpath, "//div[@class='main-container']");
        return browser.elementExists(mainId);
    }

    private boolean isMetaMaskPopup(EmbeddedBrowser browser, String handle) {
        WebDriver driver = browser.getWebDriver();
        String currentHandle = driver.getWindowHandle();
        driver.switchTo().window(handle);
        String title = driver.getTitle();
        driver.switchTo().window(currentHandle);
        return title.contains("MetaMask");
    }

    private boolean isLogInPage(EmbeddedBrowser browser) {
        Identification loginPage = new Identification(Identification.How.xpath, "//div[@class='unlock-page']");
        return browser.elementExists(loginPage);
    }

    /**
     * login MetaMask with password
     *
     * @param password the password of MetaMask
     * @return whether the login is successful or not
     */
    private boolean logIn(EmbeddedBrowser browser, String password) {
        try {
            Identification passwordInput = new Identification(Identification.How.id, "password");
            browser.input(passwordInput, password);
            Identification loginBtnId = new Identification(Identification.How.xpath, "//button[@type='submit']");
            if (!browser.elementExists(loginBtnId)) {
                return false;
            }
            WebElement loginBtn = browser.getWebElement(loginBtnId);
            loginBtn.click();
        } catch (CrawljaxException e) {
            return false;
        }
        return true;
    }
}
