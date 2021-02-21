package xyz.troublor.crawljax.experiments;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawlRules;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.InputSpecification;
import com.crawljax.core.plugin.OnUrlLoadPlugin;
import org.kristen.crawljax.plugins.grpc.GRPCClientPlugin;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class EtherollExperiment extends Experiment {

    private static final long WAIT_TIME_AFTER_EVENT = 500;
    private static final long WAIT_TIME_AFTER_RELOAD = 500;
    private static final String DAPP_URL = "http://localhost:3000/#/";
    private static final String DAPP_NAME = "Etheroll";
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

        /* Don't click nav links */
        builder.crawlRules().dontClick("A").underXPath("//*[@id=\"root\"]/DIV/HEADER/NAV");
        builder.crawlRules().dontClick("BUTTON").underXPath("//*[@id=\"root\"]/DIV/HEADER/NAV");

        builder.crawlRules().click("BUTTON").withText("Roll");

        builder.crawlRules().setInputSpec(inputSpec);
        builder.setBrowserConfig(
                new BrowserConfiguration(EmbeddedBrowser.BrowserType.CHROME_EXISTING, chromeDebuggerAddress));
        builder.addPlugin(new GRPCClientPlugin(DAPP_NAME, instanceId, METAMASK_POPUP_URL, DAPP_URL, METAMASK_PASSWORD));

        // plugin to wait for loading
        builder.addPlugin((OnUrlLoadPlugin) context -> {
            WebDriver driver = context.getBrowser().getWebDriver();
            System.out.print("Wait for Etheroll DApp reloading...");
            new WebDriverWait(driver, Duration.ofSeconds(10)).until(d -> {
                try {
                    WebElement sample = driver.findElement(By.xpath("//*[@id=\"root\"]/DIV/DIV/DIV[1]/DIV[4]/A"));
                    return sample.getText().equals("0x0DA18EBf3C1bA9c201B97693af91757040840664");
                } catch (NoSuchElementException ignored) {
                    return false;
                }
            });
            System.out.println("done");
        });


        return new CrawljaxRunner(builder.build());
    }

    public static void main(String[] args) throws IOException {
        new EtherollExperiment().start("scripts/coverage", "scripts" + File.separator + "status.log", "localhost:9222");
    }
}
