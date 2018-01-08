package com.webpageanalyser.webpagemetadataextractor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


@SpringBootApplication
public class WebPageMetadataExtractorApp {
	public static void main(String[] args) {
		SpringApplication.run(WebPageMetadataExtractorApp.class, args);
	}



}
