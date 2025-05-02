package edu.cit.AssessMate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"edu.cit.AssessMate"}) // Ensure all sub-packages are scanned
public class AssessMateApplication {
    public static void main(String[] args) {
        SpringApplication.run(AssessMateApplication.class, args);
        System.out.println("AssessMate Application has started successfully!");
    }
}