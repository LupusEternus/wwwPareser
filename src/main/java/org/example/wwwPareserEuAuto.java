package org.example;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class wwwPareserEuAuto {

    static final int MAX_ATTEMPTS = 5;
    private static final Logger logger = LoggerFactory.getLogger(wwwPareserEuAuto.class);


    public static void main(String[] args) throws IOException, InterruptedException {

        int numberOfPages;
        String url = "http://www.euautomation.com/us/manufacturers/pacific-scientific/";
        String filePath = "EUAUTO_pacific-scientific.csv";
        Connection connection = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Firefox/97.0")
                .referrer("http://www.euautomation.com/")
                .timeout(6000)
                .ignoreContentType(true);
        Document manufacturersMain = connection.url(url).get();
        numberOfPages = getNumberOfPages(manufacturersMain);
        System.out.println(numberOfPages);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            for (int i = 0; i <= numberOfPages; i++) {
                String page = url + i + "/8";
                System.out.println(page);
                if (i % 100 == 0) {
                    Thread.sleep(3000);
                }
                int attempts = 0;
                boolean success = false;
                while (!success && attempts < MAX_ATTEMPTS) {
                    try {
                        Document document = connection.url(page).get();
                        success = true;
                        Elements elements = document.select("a.rounded-lg");
                        for (Element element : elements) {
                            String manufacturer = element.select("h3").text().trim();
                            String model = element.select("h4").text().trim();
                            if (!manufacturer.isEmpty()) {
                                bw.write(manufacturer);
                                bw.append(" ");
                                bw.append(model);
                                bw.append(',');
                                bw.newLine();
                            }
                        }
                    } catch (HttpStatusException e) {
                        if (e.getStatusCode() == 404 || e.getStatusCode() == 500) {
                            attempts++; // Increment attempts
                            System.out.println("Retrying... Attempt " + attempts);
                            Thread.sleep(3000); // Delay before retrying
                        }
                    } catch (IOException e) {
                        System.err.println("Failed to parse: " + page);
                        try (BufferedWriter bwe = new BufferedWriter(new FileWriter("errors.csv",true))) {
                            bwe.append("Failed to parse :").append(page);
                            bwe.newLine();
                        }
                        logger.error("An error occurred",e);
                    }
                }
            }
        }
    }

    private static int getNumberOfPages(Document manufacturersMain) {
        int numberOfPages;
        Element elementInMain = manufacturersMain.selectFirst("span.text-sm");
        if(elementInMain == null){
            return 0;
        }
        String[] temporary = elementInMain.text().split(" ");
        int itemNumber = Integer.parseInt(temporary[1].replace(",",""));
        numberOfPages = itemNumber / 8;
        if(itemNumber % 8 != 0){
            numberOfPages = (int)Math.ceil((double) itemNumber/8);

        }
        if(numberOfPages > 9999){
            numberOfPages = 9999;
        }
        return numberOfPages;
    }
}





