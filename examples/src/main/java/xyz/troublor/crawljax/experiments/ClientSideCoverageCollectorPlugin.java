package xyz.troublor.crawljax.experiments;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawlerContext;
import com.crawljax.core.ExitNotifier;
import com.crawljax.core.plugin.OnFireEventSucceededPlugin;
import com.crawljax.core.plugin.PostCrawlingPlugin;
import com.crawljax.core.plugin.PreResetPlugin;
import com.crawljax.core.state.Eventable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ClientSideCoverageCollectorPlugin implements PreResetPlugin, OnFireEventSucceededPlugin, PostCrawlingPlugin {
    Path covSaveDir;
    String lastCovJson = null;
    Timer periodicallySaveCoverageTimer;

    public ClientSideCoverageCollectorPlugin(Path covSaveDir) {
        this.covSaveDir = covSaveDir;

    }

    private String fetchCovJson(EmbeddedBrowser browser) {
        return (String) browser.executeJavaScript("return JSON.stringify(window.__coverage__)");
    }

    private void saveCovJson(String covJson, String filename) {
        if (!Files.exists(this.covSaveDir)) {
            try {
                Files.createDirectories(this.covSaveDir);
            } catch (IOException e) {
                System.err.println("Create coverage save dir failed: " + e.getMessage());
            }
        }
        try {
            Path file;
            if (filename != null) {
                file = Paths.get(filename);
            } else {
                file = Files.createTempFile(this.covSaveDir, "cov-", ".json");
            }
            FileWriter myWriter = new FileWriter(file.toString());
            myWriter.write(covJson);
            myWriter.close();
        } catch (IOException e) {
            System.err.println("Save coverage failed: " + e.getMessage());
        }
    }

    @Override
    public void preReset(CrawlerContext context) {
        String covJson = fetchCovJson(context.getBrowser());
        saveCovJson(covJson, null);


        // save coverage every 1 minute
        if (this.periodicallySaveCoverageTimer == null) {
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    String json = fetchCovJson(context.getBrowser());
                    DateFormat dateFormat = new SimpleDateFormat("HH-mm-ss");
                    Date now = new Date();
                    saveCovJson(json, covSaveDir + File.separator + "cov-" + dateFormat.format(now) + ".json");
                }
            };
            this.periodicallySaveCoverageTimer = new Timer(true);
            this.periodicallySaveCoverageTimer.schedule(task, 60 * 1000, 60 * 1000);
        }
    }


    @Override
    public void onFireEventSucceeded(CrawlerContext context, Eventable eventable, List<Eventable> pathToFailure) {
        lastCovJson = fetchCovJson(context.getBrowser());
    }

    @Override
    public void postCrawling(CrawlSession session, ExitNotifier.ExitStatus exitReason) {
        saveCovJson(lastCovJson, null);
        if (this.periodicallySaveCoverageTimer != null) {
            this.periodicallySaveCoverageTimer.cancel();
        }
    }
}
