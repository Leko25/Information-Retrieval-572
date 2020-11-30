package org.example;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.*;

public class MyCrawler extends WebCrawler {
    //Filter Document (HTML, doc, pdf) and Image file types
    private final static Pattern DOC_FILTERS = Pattern.compile(".*(\\.(html?|doc|docx|pdf))(\\?|$)");
    private final static Pattern IMAGE_FILTERS = Pattern.compile(".*(\\.(bmp|gif|jpe?g|png|tiff?))(\\?|$)");
    private final static Pattern NO_EXTENSION = Pattern.compile(".*(\\.[a-zA-Z]*)(\\?|$)");

    //PATHS
    private final static String BASE_URL = "https://www.nytimes.com";
    private final static Path BASE_DIR = Paths.get("src/data/crawler");
    private final static Path FETCH_WSJ_PATH = Paths.get(BASE_DIR.toString(), "fetch_nytimes.csv");
    private final static Path VISIT_WSJ_PATH = Paths.get(BASE_DIR.toString(), "visit_nytimes.csv");
    private final static Path URLS_WSJ_PATH = Paths.get(BASE_DIR.toString(), "urls_nytimes.csv");

    //CSV File Writers
    private static CSVWriter fetchWSJWriter;
    private static CSVWriter visitWSJWriter;
    private static CSVWriter urlsWSJWriter;

    //Statistics
    private static int fetchesAttempted = 0;
    private static int fetchesSucceeded = 0;
    private static int fetchesFailed = 0;
    private static int totalURLS = 0;
    private static int urlsWithin = 0;
    private static int urlsOutside = 0;

    public MyCrawler() throws Exception{
        fetchWSJWriter = new CSVWriter(new FileWriter(FETCH_WSJ_PATH.toString()));
        visitWSJWriter = new CSVWriter(new FileWriter(VISIT_WSJ_PATH.toString()));
        urlsWSJWriter = new CSVWriter(new FileWriter(URLS_WSJ_PATH.toString()));

        // Write CSV Headers
        fetchWSJWriter.writeNext(new String[] {"URL", "Status"});
        visitWSJWriter.writeNext(new String[] {"URL", "File Size", "OutLinks", "Content Type"});
        urlsWSJWriter.writeNext(new String[] {"URL", "Inside/Outside"});
    }

    @Override
    public boolean shouldVisit(Page page, WebURL url) {
        String href = url.getURL().toLowerCase();

        if (!href.startsWith(BASE_URL) || href.startsWith("http://www.nytimes.com")) {
            urlsOutside++;
            urlsWSJWriter.writeNext(new String[] {href, "N_OK"});
            return false;
        }

        urlsWSJWriter.writeNext(new String[] {href, "OK"});
        urlsWithin++;

        if (DOC_FILTERS.matcher(href).matches() || IMAGE_FILTERS.matcher(href).matches()) {
            return true;
        }

        // Do not visit xml files
        if (href.contains("/xml/")) {
            return false;
        }

        if (!NO_EXTENSION.matcher(href).lookingAt()) {
            return true;
        }

        return false;
    }

    @Override
    protected void handlePageStatusCode(WebURL webUrl, int statusCode, String statusDescription) {
        int statusPrefix = (int) statusCode / 100;

        fetchesAttempted++;

        if (statusPrefix == 2)
            fetchesSucceeded++;
        else
            fetchesFailed++;

        String url = webUrl.getURL();
        url = url.replace(',', '-');
        fetchWSJWriter.writeNext(new String[] {url, Integer.toString(statusCode)});
    }

    @Override
    public void visit(Page page) {
       String url = page.getWebURL().getURL();

       String contentType = page.getContentType();

       if (contentType.indexOf(";") > 0)
           contentType = contentType.substring(0, contentType.indexOf(";"));

       int fileSize = page.getContentData().length;

       //Determine the number of outgoing links
        int outLinks = 0;

        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            Set<WebURL> links = htmlParseData.getOutgoingUrls();
            outLinks = links.size();
        }

        totalURLS += outLinks;

        // Fetch total links meta statistic here
        //<!-- Total Links Here-->
        visitWSJWriter.writeNext(new String[] {url, fileSize + " Bytes", Integer.toString(outLinks), contentType});
    }

    @Override
    public void onBeforeExit() {
        super.onBeforeExit();
        System.out.println("URL Statistics:");

        try {
            System.out.println("Fetches Attempted: " + fetchesAttempted);
            System.out.println("Fetches Succeeded: " + fetchesSucceeded);
            System.out.println("Fetches Failed: " + fetchesFailed);
            System.out.println("Total Urls: " + totalURLS);
            System.out.println("Unique Urls Within: " + urlsWithin);
            System.out.println("Unique Urls Outside: " + urlsOutside);
            System.out.println("Unique Urls Extracted: " + (urlsOutside + urlsWithin));

            fetchWSJWriter.close();
            visitWSJWriter.close();
            urlsWSJWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
