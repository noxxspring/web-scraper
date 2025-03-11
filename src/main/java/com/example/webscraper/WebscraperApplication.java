package com.example.webscraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class WebscraperApplication {

	private static final int MAX_RETRIES = 3;

	public static void main(String[] args) {
		SpringApplication.run(WebscraperApplication.class, args);

//		String url = "https://books.toscrape.com/";
//
//		try {
//			Document document = Jsoup.connect(url).get();
//			// creating arrays to store books
//			Elements books = document.select(".product_pod");
//
//			System.out.println("===========================================");
//			System.out.println("Book Web Scraper");
//			System.out.println("===========================================");
//
//			// looping through the books
//			for (Element bk: books){
//				// get the title of book
//				String title = bk.select("h3 > a").text();
//				// get price of book
//				String price = bk.select(".price_color").text();
//
//				System.out.println(title + " - "+ price);
//			}
//			System.out.println("========================================");
//
//		} catch (IOException e) {
//            throw new RuntimeException(e);
//        }


   }


}
