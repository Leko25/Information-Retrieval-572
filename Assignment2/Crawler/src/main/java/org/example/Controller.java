package org.example;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class Controller {
    public static void main(String[] args) throws Exception {
        String crawlStorageFolder = "/Users/chukuemekaogudu/Documents/csci572/Assignment2/Crawler/src/data/crawler";

        //config parameters
        int numberOfCrawlers = 16;
        int numberOfPages = 20000;
        int maxDepth = 16;
        int politenessDelay = 203;
        int socketTimeOut = 30000;

        CrawlConfig config = new CrawlConfig();

        config.setCrawlStorageFolder(crawlStorageFolder);
        config.setMaxPagesToFetch(numberOfPages);
        config.setMaxDepthOfCrawling(maxDepth);
        config.setPolitenessDelay(politenessDelay);
        config.setSocketTimeout(socketTimeOut);
        config.setUserAgentString("csci572");
        config.setIncludeBinaryContentInCrawling(true);


        /*
         * Instantiate the controller for this crawl.
         */
        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
        /*
         * For each crawl, you need to add some seed urls. These are the first * URLs that are fetched and then the crawler starts following links
         * which are found in these pages
         */
        controller.addSeed("https://www.nytimes.com");
        /*
         * Start the crawl. This is a blocking operation, meaning that your code * will reach the line after this only when crawling is finished.
         */
        controller.start(MyCrawler.class, numberOfCrawlers);
    }
}
