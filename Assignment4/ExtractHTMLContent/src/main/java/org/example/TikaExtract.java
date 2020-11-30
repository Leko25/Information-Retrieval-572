package org.example;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;


import java.io.*;

public class TikaExtract {
    public static void main(String[] args) throws IOException, TikaException, SAXException {
        String htmlDir = "/Users/chukuemekaogudu/Documents/solr-7.7.2/CrawlData/foxnews";
        String bigTextPath= "/Users/chukuemekaogudu/Documents/csci572/Assignment4/big.txt";

        File file = new File(htmlDir);
        File[] files = file.listFiles();

        PrintWriter printWriter = new PrintWriter(bigTextPath);


        if (files != null) {
            for (File f: files) {
                BodyContentHandler handler = new BodyContentHandler(-1);
                HtmlParser htmlParser = new HtmlParser();
                Metadata metadata = new Metadata();
                ParseContext pcontext = new ParseContext();
                FileInputStream inputStream = new FileInputStream(f);
                htmlParser.parse(inputStream, handler, metadata, pcontext);
                String content = handler.toString();
                content = content.replaceAll("\\s+", " ");
                printWriter.println(content);
            }
        }
        printWriter.flush();
        printWriter.close();
    }
}
