package org.ram.url.shortner.url;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class UrlServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UrlServiceApplication.class, args);
    }
}
