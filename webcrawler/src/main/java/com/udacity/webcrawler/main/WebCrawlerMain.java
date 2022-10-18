package com.udacity.webcrawler.main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.udacity.webcrawler.WebCrawler;
import com.udacity.webcrawler.WebCrawlerModule;
import com.udacity.webcrawler.json.ConfigurationLoader;
import com.udacity.webcrawler.json.CrawlResult;
import com.udacity.webcrawler.json.CrawlResultWriter;
import com.udacity.webcrawler.json.CrawlerConfiguration;
import com.udacity.webcrawler.profiler.Profiler;
import com.udacity.webcrawler.profiler.ProfilerModule;

import javax.inject.Inject;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Objects;

import static com.fasterxml.jackson.core.JsonGenerator.Feature.AUTO_CLOSE_TARGET;
import static com.fasterxml.jackson.core.JsonParser.Feature.AUTO_CLOSE_SOURCE;

public final class WebCrawlerMain {

    private final CrawlerConfiguration config;

    private WebCrawlerMain(CrawlerConfiguration config) {
        this.config = Objects.requireNonNull(config);
    }

    @Inject
    private WebCrawler crawler;

    @Inject
    private Profiler profiler;
    public static final ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper.disable(AUTO_CLOSE_SOURCE);
        mapper.disable(AUTO_CLOSE_TARGET);
    }

    private void run() throws Exception {
        Guice.createInjector(new WebCrawlerModule(config), new ProfilerModule()).injectMembers(this);
        CrawlResult result = crawler.crawl(config.getStartPages());
        CrawlResultWriter resultWriter = new CrawlResultWriter(result);

        if (config.getResultPath().isBlank()) {
            resultWriter.write(new PrintWriter(System.out));
        } else {
            resultWriter.write(Path.of(config.getResultPath()));
        }

        if (config.getProfileOutputPath().isBlank()) {
            try (Writer outputWriter = new PrintWriter(System.out)) {
                profiler.writeData(outputWriter);
            }
        } else {
            profiler.writeData(Path.of(config.getProfileOutputPath()));
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage: WebCrawlerMain [starting-url]");
            return;
        }

        CrawlerConfiguration config = new ConfigurationLoader(Path.of(args[0])).load();

        new WebCrawlerMain(config).run();
    }
}