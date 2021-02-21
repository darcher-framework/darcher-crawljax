package com.crawljax.core.plugin;

import com.crawljax.core.CrawlerContext;

public interface PreResetPlugin extends Plugin {
    void preReset(CrawlerContext context);
}
