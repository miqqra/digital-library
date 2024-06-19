package ru.nsu.digitallibrary;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class DigitalLibraryApplication {

    public static void main(String[] args) {
        SpringApplication.run(DigitalLibraryApplication.class, args);
    }

}
