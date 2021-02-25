package com.darcher.crawljax.experiments;

import com.crawljax.core.CrawlTaskConsumer;
import com.crawljax.core.CrawljaxRunner;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class Experiment {
    protected abstract CrawljaxRunner initialize(Path coverageDir, String chromeDebuggerAddress);

//    public void start(String statusLogPath, String chromeDebuggerAddress) throws IOException {
//        CrawljaxRunner crawljaxRunner = this.initialize(Paths.get(Paths.get(statusLogPath).getParent() + File.separator + "cov"),
//                chromeDebuggerAddress);
//        CrawlTaskConsumer consumer = crawljaxRunner.callRtnConsumer();
//        consumer.crawler.close();
//        try (FileWriter writer = new FileWriter(new File(statusLogPath))) {
//            writer.write(crawljaxRunner.getReason().toString());
//        }
//
//        System.out.println("Crawl Complete: " + crawljaxRunner.getReason());
//    }

    public void start(String coverageDir, String statusLogPath, String chromeDebuggerAddress) throws IOException {
        CrawljaxRunner crawljaxRunner =
                this.initialize(Paths.get(coverageDir),
                chromeDebuggerAddress);
        CrawlTaskConsumer consumer = crawljaxRunner.callRtnConsumer();
        consumer.crawler.close();
        try (FileWriter writer = new FileWriter(statusLogPath)) {
            writer.write(crawljaxRunner.getReason().toString());
        }

        System.out.println("Crawl Complete: " + crawljaxRunner.getReason());
    }
}
