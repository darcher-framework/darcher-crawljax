package com.darcher.crawljax.grpc;

import com.crawljax.browser.EmbeddedBrowser;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.ArrayList;

public class MetamaskTxConfirmThread implements Runnable{
    private EmbeddedBrowser browser;
    private WebDriver driver;
    private String METAMASK_POPUP_URL = "chrome-extension://pblaiiacglodkdimplphhfffmpblfgmh/home.html";

    public MetamaskTxConfirmThread(String metamaskUrl) {
        this.METAMASK_POPUP_URL = metamaskUrl;
    }

//    public void setBrowser(EmbeddedBrowser browser) {
//        this.browser = browser;
//    }

    public void setDriver(WebDriver driver) {
        this.driver = driver;
    }

    @Override
    public void run() {
        while (true) {
//            driver.navigate().refresh();
            try {
                Thread.sleep(2500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (driver != null) {
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

                if (isElementExists(By.className("btn-primary"))) {
                    driver.findElement(By.className("btn-primary")).click();
                } else if (isElementExists(By.className("request-signature__footer__sign-button"))) {
                    driver.findElement(By.className("request-signature__footer__sign-button")).click();
                } else if (isElementExists(By.className("btn-secondary"))) {
                    driver.findElement(By.className("btn-secondary")).click();
                } else if (isElementExists(By.className("btn-default"))) {
                    driver.findElement(By.className("btn-default")).click();
                }

                driver.close();
                driver.switchTo().window(currentHandle);
            }
        }
    }

    private boolean isElementExists(By by) {
        try {
            WebDriver driver = browser.getWebDriver();
            driver.findElement(by);
            return true;
        } catch (Exception e) {
            System.out.printf("Element [{}] not exist.\n", by);
            return false;
        }
    }


}
