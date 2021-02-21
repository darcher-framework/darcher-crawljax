/**
 * Plugin for GRPC client. Handles the communication with GRPC server.
 */

package org.kristen.crawljax.plugins.grpc;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.*;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.plugin.*;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.Identification;
import com.crawljax.core.state.*;

import com.google.inject.ProvisionException;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.openqa.selenium.*;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import org.kristen.rpc.darcher.*;

public class GRPCClientPlugin implements
        PreCrawlingPlugin,
        PostCrawlingPlugin,
        OnBrowserCreatedPlugin,
        OnUrlFirstLoadPlugin,
        OnFireEventSucceededPlugin,
        OnFireEventFailedPlugin {
    private String METAMASK_PASSWORD;
    private String METAMASK_POPUP_URL;
    private String DAPP_URL;

    private final String INIT_CONTROL_MSG_ID = "0";
    private int WAIT_TIME_FOR_METAMASK_PLUGIN = 1000;
    private static String SERVER_HOST = "localhost";
    private static int SERVER_PORT = 1234;

    private static String txCrawlPathDir = "crawl_paths";

    final Logger logger = LoggerFactory.getLogger(getClass());

    public static ManagedChannel channel = ManagedChannelBuilder.forAddress(SERVER_HOST, SERVER_PORT)
            .usePlaintext()
            .build();
    public final DAppTestDriverServiceGrpc.DAppTestDriverServiceBlockingStub blockingStub;
    public final DAppTestDriverServiceGrpc.DAppTestDriverServiceStub asyncStub;

    public String dappName;
    public int instanceId;
    private String fromAddress = "";
    private String toAddress = "";
    private String txHash = "";
    private String events;
    private String states;
    public ControlMsgHandlerThread controlMsgHandlerThread;
    public HandleBrowserConsoleErrorThread handleBrowserConsoleErrorThread;
    public EmbeddedBrowser dappBrowser;

    // the websocket server which receives message from metamask
    private final MetaMaskNotificationServer metaMaskNotificationServer;
    private final BlockingQueue<Message> metamaskMessageQueue;

    public GRPCClientPlugin(String dappName, int instanceId, String metamaskUrl, String dappUrl, String metamaskPassword) {
        logger.info("Init GRPC client plugin, dappName={}, instanceId={}, dapUrl={}", dappName, instanceId, dappUrl);
        blockingStub = DAppTestDriverServiceGrpc.newBlockingStub(channel);
        asyncStub = DAppTestDriverServiceGrpc.newStub(channel);

        this.dappName = dappName;
        this.instanceId = instanceId;
        this.METAMASK_POPUP_URL = metamaskUrl;
        this.DAPP_URL = dappUrl;
        this.METAMASK_PASSWORD = metamaskPassword;

        this.controlMsgHandlerThread = new ControlMsgHandlerThread(this.dappName, this.instanceId);
        Thread controlThread = new Thread(this.controlMsgHandlerThread);
        controlThread.start();
        logger.info("Start the control msg handler thread, dappName={}, instanceId={}", dappName, instanceId);

        this.metamaskMessageQueue = new ArrayBlockingQueue<>(100);
        this.metaMaskNotificationServer = new MetaMaskNotificationServer(new InetSocketAddress(1237));
        this.metaMaskNotificationServer.setUnapprovedTxListener(this.metamaskMessageQueue::add);
        this.metaMaskNotificationServer.setUnconfirmedMessageListener(this.metamaskMessageQueue::add);
        this.metaMaskNotificationServer.start();
        // listen to SIGINT to shutdown MetaMask Notifier server
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                this.metaMaskNotificationServer.stop();
            } catch (IOException | InterruptedException e) {
                logger.error("MetaMask Notifier server not stopped");
                e.printStackTrace();
            }
        }));

    }

    /**
     * Handles the control message sent by server and performs correct behavior.
     *
     * @param dAppDriverControlMsg the message sent by server
     */
    public void handleControlMsg(DappTestService.DAppDriverControlMsg dAppDriverControlMsg) {
        DappTestService.DAppDriverControlType controlType = dAppDriverControlMsg.getControlType();
        logger.debug("Enter the msg handler, begin to handle {} msg", controlType);
        switch (controlType) {
            case Refresh:
                WebDriver driver = dappBrowser.getWebDriver();
                driver.navigate().refresh();
                logger.info("Finish refreshing the page");
                break;
            case NilType:
                logger.warn("Received nil type control message");
                break;
            case UNRECOGNIZED:
                logger.warn("Received unrecognized control type.");
                break;
            default:
                break;
        }
    }

    /**
     * Gets the necessary information of the latest transaction.
     *
     * @param browser the browser instance
     */
    private void getTxInfo(EmbeddedBrowser browser) {
        logger.debug("Begin to get the transaction information, from address, to address and txhash");

        // Xpath identification for the elements which contain necessary information.
        Identification activityPaneId = new Identification(Identification.How.xpath,
                "/html/body/div[1]/div/div[4]/div/div/div/div[3]/ul/li[2]");
        Identification txStatusId = new Identification(Identification.How.xpath,
                "/html/body/div[1]/div/div[4]/div/div/div/div[3]/div/div/div/div[1]/div[2]/div[3]/h3/div");
        Identification txBoxId = new Identification(Identification.How.xpath,
                "/html/body/div[1]/div/div[4]/div/div/div/div[3]/div/div/div/div/div[1]");
        Identification alternativeTxBoxId = new Identification(Identification.How.xpath,
                "/html/body/div[1]/div/div[4]/div/div/div/div[3]/div/div/div/div[1]/div[2]");
        Identification copyFromId = new Identification(Identification.How.xpath,
                "/html/body/div[2]/div/div/section/div/div/div[2]/div[1]/div/div[1]/div/div/div");
        Identification copyToId = new Identification(Identification.How.xpath,
                "/html/body/div[2]/div/div/section/div/div/div[2]/div[1]/div/div[3]/div/div/div");
        Identification copyTxHashId = new Identification(Identification.How.xpath,
                "/html/body/div[2]/div/div/section/div/div/div[1]/div[2]/div[1]/div/button");

        this.fromAddress = null;
        this.toAddress = null;
        this.txHash = null;

        try {
            // Click the elements step by step to get pop-up window for tx information.
            new WebDriverWait(browser.getWebDriver(), Duration.ofHours(1)).until(d -> browser.elementExists(activityPaneId));
            WebElement activityPane = browser.getWebElement(activityPaneId);
            activityPane.click();

            new WebDriverWait(browser.getWebDriver(), Duration.ofMinutes(1)).until(d -> browser.elementExists(activityPaneId) || browser.elementExists(txBoxId));
            if (browser.elementExists(txStatusId)) {
                WebElement status = browser.getWebElement(txStatusId);
                if (status.getText().contains("Pending")) {
                    WebElement txBox = browser.getWebElement(alternativeTxBoxId);
                    txBox.click();
                } else {
                    WebElement txBox = browser.getWebElement(txBoxId);
                    txBox.click();
                }
            } else {
                WebElement txBox = browser.getWebElement(txBoxId);
                txBox.click();
            }

            // Click to copy from address, to address and hash for the tx, get the information from clipboard.
            new WebDriverWait(browser.getWebDriver(), Duration.ofMinutes(1)).until(d -> browser.elementExists(copyFromId));
            WebElement copyFromElement = browser.getWebElement(copyFromId);
            copyFromElement.click();
            this.fromAddress = getTextFromClipboard();

            new WebDriverWait(browser.getWebDriver(), Duration.ofMinutes(1)).until(d -> browser.elementExists(copyToId));
            WebElement copyToElement = browser.getWebElement(copyToId);
            copyToElement.click();
            this.toAddress = getTextFromClipboard();

            new WebDriverWait(browser.getWebDriver(), Duration.ofMinutes(1)).until(d -> browser.elementExists(copyTxHashId));
            WebElement copyTxHashElement = browser.getWebElement(copyTxHashId);
            copyTxHashElement.click();
            this.txHash = getTextFromClipboard();

        } catch (RuntimeException ignored) {
        }

        logger.debug("Finish getting transaction information, fromAddress={}, toAddress={}, txHash={}", fromAddress, toAddress, txHash);
    }

    /**
     * Gets the text from system clipboard.
     *
     * @return the text in clipboard
     */
    private String getTextFromClipboard() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable trans = clipboard.getContents(null);
        if (trans != null) {
            if (trans.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                try {
                    String text = (String) trans.getTransferData(DataFlavor.stringFlavor);
                    return text;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * Judges whether current page is Metamask processing page (not the main page)
     *
     * @param browser the browser instance
     * @return if the current page is Metamask processing page
     */
    private boolean isMetaMaskProcessPage(EmbeddedBrowser browser) {
        Identification mainId = new Identification(Identification.How.xpath, "//div[@class='main-container']");
        logger.debug("Current page is Metamask process page --> [{}]", !browser.elementExists(mainId));
        return !browser.elementExists(mainId);
    }

    @Override
    public void preCrawling(CrawljaxConfiguration config) throws RuntimeException {
        logger.info("Begin crawling, send testStartMsg to the server, dappName={}, instanceId={}", dappName, instanceId);

        DappTestService.TestStartMsg testStartMsg = DappTestService.TestStartMsg
                .newBuilder()
                .setDappName(this.dappName)
                .setInstanceId(Integer.toString(this.instanceId))
                .build();
        blockingStub.notifyTestStart(testStartMsg);
    }

    @Override
    public void postCrawling(CrawlSession session, ExitNotifier.ExitStatus exitReason) {
        logger.info("End crawling, send testEndMsg to the server, dappName={}, instanceId={}", dappName, instanceId);

        DappTestService.TestEndMsg testEndMsg = DappTestService.TestEndMsg
                .newBuilder()
                .setDappName(this.dappName)
                .setInstanceId(Integer.toString(this.instanceId))
                .build();
        blockingStub.notifyTestEnd(testEndMsg);

        // stop MetaMask Notification WebSocket server
        try {
            this.metaMaskNotificationServer.stop();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        // stop console error listener
        this.handleBrowserConsoleErrorThread.stop();
    }

    @Override
    public void onBrowserCreated(EmbeddedBrowser newBrowser) {
        logger.info("Finish creating the browser");
        this.dappBrowser = newBrowser;

        this.handleBrowserConsoleErrorThread = new HandleBrowserConsoleErrorThread(this.dappName, this.instanceId);
        Thread consoleErrorThread = new Thread(this.handleBrowserConsoleErrorThread);
        consoleErrorThread.start();
        logger.info("Start the browser console error handler thread, dappName={}, instanceId={}", dappName, instanceId);

        try {
            newBrowser.goToUrl(new URI(METAMASK_POPUP_URL));
        } catch (URISyntaxException e) {
            System.out.println("ERROR: invalid MetaMask popup url, " + METAMASK_POPUP_URL);
        }
        if (isLogInPage(newBrowser) && !logIn(newBrowser, METAMASK_PASSWORD)) {
            System.out.println("ERROR: MetaMask login failed");
        }

        // TODO: just for test, need to be removed
//        if (isMetaMaskProcessPage(newBrowser)) {
//            processMetamaskPopup(newBrowser);
//        }

        // TODO: handle other scenarios (specific)
//        try {
//            newBrowser.goToUrl(new URI(DAPP_URL));
//
////            // Sign up for Augur
////            WebDriver driver = newBrowser.getWebDriver();
////            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
////            driver.findElement(By.cssSelector(".buttons-styles_SecondaryButton")).click();
////            driver.findElement(By.cssSelector(".buttons-styles_SecondarySignInButton:nth-child(7) > div > div > div:nth-child(1)")).click();
//        } catch (URISyntaxException e) {
//            System.out.println("ERROR: invalid DAPP url, " + DAPP_URL);
//        }
    }

    /**
     * Executes `ethereum.enable()` to connect dapp with MetaMask
     *
     * @param context the current crawler context.
     */
    @Override
    public void onUrlFirstLoad(CrawlerContext context) {
        logger.info("The url is loaded for the first time");
        EmbeddedBrowser browser = context.getBrowser();
        WebDriver driver = browser.getWebDriver();

        // TODO: Metamask is different from other dapps, br careful
        if (driver instanceof JavascriptExecutor) {
//            ((JavascriptExecutor)driver).executeScript("ethereum.enable()");
        } else {
            throw new IllegalStateException("This driver does not support JavaScript!");
        }
    }

    /**
     * Clicks the primary button in the MetaMask pop-up window.
     *
     * @param context the crawler context
     */
    private void processMetamaskPopup(CrawlerContext context, CrawlPath crawlPath) {
        logger.debug("Enter processMetamaskPopup function, begin to process");

        // Open a new tab in the browser and visit Metamask pop-up page
        EmbeddedBrowser browser = context.getBrowser();
        WebDriver driver = browser.getWebDriver();
        String originalTab = driver.getWindowHandle();
        driver.switchTo().newWindow(WindowType.TAB);
        String metamaskTab = driver.getWindowHandle();
//        ((JavascriptExecutor) driver).executeScript("window.open()");
//        ArrayList<String> tabs = new ArrayList<>(driver.getWindowHandles());
//        driver.switchTo().window(tabs.get(tabs.indexOf(currentHandle) + 1));
        driver.get(METAMASK_POPUP_URL);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // TODO: Sign
        // Process the page, click buttons
        if (isMetaMaskProcessPage(browser)) {
            Identification primaryBtnId = new Identification(Identification.How.xpath,
                    "//button[contains(@class, 'btn-primary')]");
            Identification secondaryBtnId = new Identification(Identification.How.xpath,
                    "//button[contains(@class, 'btn-secondary')]");
            Identification defaultBtnId = new Identification(Identification.How.xpath,
                    "//button[contains(@class, 'btn-default')]");
//            Identification signBtnId = new Identification(Identification.How.xpath, "//button[contains(@class, 'request-signature__footer__sign-button')]");
            boolean isSendTx = false;
            if (browser.elementExists(primaryBtnId)) {
                WebElement primaryBtn = browser.getWebElement(primaryBtnId);
                if (primaryBtn.getText().contains("Confirm")) {
                    isSendTx = true;
                }
                primaryBtn.click();

            } else if (browser.elementExists(secondaryBtnId)) {
                WebElement secondaryBtn = browser.getWebElement(secondaryBtnId);
                secondaryBtn.click();
            } else if (browser.elementExists(defaultBtnId)) {
                WebElement defaultBtn = browser.getWebElement(defaultBtnId);
                defaultBtn.click();
            } else {
                return;
            }
//            if (browser.elementExists(signBtnId)) {
//                WebElement signBtn = browser.getWebElement(signBtnId);
//                signBtn.click();
//            } else {
//                driver.close();
//                driver.switchTo().window(currentHandle);
//                System.out.println("Returned");
//                return;
//            }

            if (isSendTx) {
//                try {
//                    Thread.sleep(2500);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                // wait for transaction reaches pending pool
                new WebDriverWait(driver, Duration.ofHours(1)).until(d -> {
                    try {
                        WebElement loading = driver.findElement(By.className("loading-overlay"));
                        return loading == null;
                    } catch (NoSuchElementException ignored) {
                        return true;
                    }
                });
//                getTxInfo(browser);

                // Get reproduction
//            System.out.println("########################################################################");
                events = "";
                states = "";
//        for (StackTraceElement s: context.getCrawlPath().asStackTrace()) {
//            System.out.println(s);
//        }
                try {
                    for (List<Eventable> l : context.getSession().getCrawlPaths()) {
                        for (Eventable e : l) {
                            events += e.toString() + "\n";
//                    System.out.println(e);
                        }
                    }
//            System.out.println("########################################################################");

//            System.out.println("************************************************************************");
                    for (StateVertex s : context.getSession().getStateFlowGraph().getAllStates()) {
                        states += s.toString() + "\n";
//                System.out.println(s.getName() + "         " + s.getUrl());
                    }
//            System.out.println("************************************************************************");

                } catch (ProvisionException e) {
                    if (!(e.getCause() instanceof CrawlSessionNotSetupYetException)) {
                        throw e;
                    }
                }

                logger.info("Send out a transaction, send txMsg to the server, dappName={}, instanceId={}, txHash={}, fromAddress={}, toAddress={}", dappName, instanceId, txHash, fromAddress, toAddress);
                DappTestService.TxMsg txMsg = DappTestService.TxMsg.newBuilder()
                        .setDappName(this.dappName)
                        .setInstanceId(Integer.toString(this.instanceId))
                        .setHash(this.txHash)
                        .setFrom(this.fromAddress)
                        .setTo(this.toAddress)
                        .setStates(this.states)
                        .setEvents(this.events)
                        .build();
                logger.debug("Begin to send txMsg and wait for tx being processed, dappName={}, instanceId={}, txHash={}, fromAddress={}, toAddress={}", dappName, instanceId, txHash, fromAddress, toAddress);

                // TODO workaround: since it is painful to change gRPC, we simply save the crawlPath along with txHash in
                //  files
                try {
                    File crawlPathDir = new File(txCrawlPathDir);
                    crawlPathDir.mkdirs();
                    File txCrawlPath = new File(txCrawlPathDir + File.separator + this.txHash + ".log");
                    txCrawlPath.createNewFile();
                    FileWriter fileWriter = new FileWriter(txCrawlPath);
                    fileWriter.write(this.txHash + "\n");
                    for (Eventable eventable :
                            crawlPath) {
                        fileWriter.write(eventable.toString() + "\n");
                    }
                    fileWriter.close();
                    logger.info("Crawl path for tx " + this.txHash + " has been saved at " + txCrawlPath.getAbsolutePath());
                } catch (IOException e) {
                    logger.error("Failed to save crawl path for tx " + this.txHash + ": " + e.getMessage());
                }

                // return to previous tab in case REFRESH_PATH requests from DArcher
                driver.switchTo().window(originalTab);

                try {
                    blockingStub.waitForTxProcess(txMsg);
                    logger.debug("Finish sending txMsg and processing tx, dappName={}, instanceId={}, txHash={}, fromAddress={}, toAddress={}", dappName, instanceId, txHash, fromAddress, toAddress);
                    System.out.println();
                } catch (StatusRuntimeException e) {
                    if (e.getStatus().getCode() != Status.Code.UNAVAILABLE) {
                        throw e;
                    }
                }


                // switch to metamask tab
                driver.switchTo().window(metamaskTab);
            } else {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        // Close current tab, return to the original testing tab
        driver.close();
        driver.switchTo().window(originalTab);

        // check if there is more tx coming
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.checkMetamaskMessage(context, crawlPath);

        logger.debug("Exit processMetamaskPopup function, end processing");
    }

    private boolean isLogInPage(EmbeddedBrowser browser) {
        Identification loginPage = new Identification(Identification.How.xpath, "//div[@class='unlock-page']");
        return browser.elementExists(loginPage);
    }


    /**
     * Login MetaMask with password
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

    @Override
    public void onFireEventSucceeded(CrawlerContext context, Eventable eventable, List<Eventable> pathToHere) {
        logger.info("One event is fired successfully");
//        processMetamaskPopup(context);
        // there is unapproved tx to process
        List<Eventable> path = new ArrayList<>(pathToHere);
        path.add(eventable);
        this.checkMetamaskMessage(context, path);
    }

    @Override
    public void onFireEventFailed(CrawlerContext context, Eventable eventable, List<Eventable> pathToHere) {
        logger.info("One event fails to be fired");
//        processMetamaskPopup(context);
        // there is unapproved tx to process
        List<Eventable> path = new ArrayList<>(pathToHere);
        path.add(eventable);
        this.checkMetamaskMessage(context, path);
    }

    public void checkMetamaskMessage(CrawlerContext context, List<Eventable> path) {
        // check unapproved tx queue
        Message unapprovedTxMessage = null;
        try {
            unapprovedTxMessage = this.metamaskMessageQueue.poll(50, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignored) {
        }
        if (unapprovedTxMessage == null) {
            // no unapproved tx
            return;
        }
        processMetamaskPopup(context, new CrawlPath(path));
    }

    /**
     * The class to handle the control messages sent by the server
     */
    public class ControlMsgHandlerThread implements Runnable {
        private String dappName;
        private int instanceId;
        public DappTestService.DAppDriverControlMsg dAppDriverCtlMsg;

        public ControlMsgHandlerThread(String dappName, int instanceId) {
            logger.debug("Init the ControlMsgHandlerThread");
            this.dappName = dappName;
            this.instanceId = instanceId;
        }

        StreamObserver<DappTestService.DAppDriverControlMsg> requestObserver;

        StreamObserver<DappTestService.DAppDriverControlMsg> responseObserver =
                DAppTestDriverServiceGrpc.newStub(channel).dappDriverControl(new StreamObserver<DappTestService.DAppDriverControlMsg>() {
                    @Override
                    public void onNext(DappTestService.DAppDriverControlMsg dAppDriverControlMsg) {
                        logger.info("Receive some information from the stream: " + dAppDriverControlMsg.getAllFields());
                        if (dappBrowser == null) {
                            logger.error("The browser is null");
                            return;
                        }
                        handleControlMsg(dAppDriverControlMsg);
                        dAppDriverCtlMsg = DappTestService.DAppDriverControlMsg
                                .newBuilder()
                                .setRole(Common.Role.DAPP)
                                .setId(dAppDriverControlMsg.getId())
                                .setDappName(dAppDriverControlMsg.getDappName())
                                .setInstanceId(dAppDriverControlMsg.getInstanceId())
                                .setControlType(DappTestService.DAppDriverControlType.NilType)
                                .build();
//                        logger.info("Send feedback DAppDriverControlMsg to the server, tell the server the operation is finished");
                        responseObserver.onNext(dAppDriverControlMsg);
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        System.out.printf("Error from stream %s", t.toString());
                        t.printStackTrace();
                    }

                    @Override
                    public void onCompleted() {
                        System.out.println("Stream completed");
                    }
                });

        @Override
        public void run() {
            requestObserver = asyncStub.dappDriverControl(responseObserver);

            DappTestService.DAppDriverControlMsg dAppDriverControlMsg = DappTestService.DAppDriverControlMsg
                    .newBuilder()
                    .setRole(Common.Role.DAPP)
                    .setId(INIT_CONTROL_MSG_ID)
                    .setDappName(this.dappName)
                    .setInstanceId(Integer.toString(this.instanceId))
                    .setControlType(DappTestService.DAppDriverControlType.NilType)
                    .build();
            responseObserver.onNext(dAppDriverControlMsg);
//            requestObserver.onCompleted();


//            try {
//                Thread.sleep(5000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
        }
    }


    public class HandleBrowserConsoleErrorThread implements Runnable {
        private String dappName;
        private int instanceId;
        private String errorString;
        public DappTestService.ConsoleErrorMsg consoleErrorMsg;

        private AtomicBoolean stopped = new AtomicBoolean(false);

        public HandleBrowserConsoleErrorThread(String dappName, int instanceId) {
            logger.debug("Init the HandleBrowserConsoleErrorThread");
            this.dappName = dappName;
            this.instanceId = instanceId;
        }

        public void stop() {
            stopped.set(true);
        }

        @Override
        public void run() {
            while (true) {
                if (stopped.get()) {
                    System.out.println("console error exit");
                    return;
                }
                LogEntries logEntries = dappBrowser.getWebDriver().manage().logs().get(LogType.BROWSER);
                if (!logEntries.getAll().isEmpty()) {
                    for (LogEntry entry : logEntries) {
                        if (entry.getLevel().equals(Level.SEVERE)) {
                            consoleErrorMsg = DappTestService.ConsoleErrorMsg
                                    .newBuilder()
                                    .setDappName(dappName)
                                    .setInstanceId(Integer.toString(instanceId))
                                    .setErrorString(entry.toString())
                                    .build();
                            try {
                                blockingStub.notifyConsoleError(consoleErrorMsg);
                            } catch (StatusRuntimeException e) {
                                logger.error("Failed to notify console error: " + e.getMessage());
                            }
                        }
                    }

                }
            }
        }
    }

}
