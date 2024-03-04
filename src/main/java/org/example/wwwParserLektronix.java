package org.example;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public class wwwParserLektronix {

    public static void main(String[] args) {

        int numberOfPages = 2198;
        String url = "https://www.lektronix.pl/drives/2/";
        String filePath = "output.csv";
        Connection connection = Jsoup.connect(url).maxBodySize(0).timeout(60000).ignoreContentType(true);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            for (int i = 1; i <= numberOfPages; i++) {
                String page = url + i;
                System.out.println(page);
                try {
                    Document document = connection.url(page).get();
                    Elements elements = document.select("a.list");
                    for (Element element : elements) {
                        String manufacturer = element.select("dd#productManufacturer").text().trim();
                        String model = element.select("dd#productModelNumber").text().trim();
                        if (!manufacturer.isEmpty()) {
                            bw.write(manufacturer);
                            bw.append(" ");
                            bw.append(model);
                            bw.append(',');
                            bw.newLine();
                        }
                    }
                }catch (IOException e){
                    System.err.println("Failed to parse: " + page);
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
    }
}

