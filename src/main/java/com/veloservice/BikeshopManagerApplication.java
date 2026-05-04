package com.veloservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application entry point for BikeShop Manager.
 */
@SpringBootApplication
public class BikeshopManagerApplication {
    /**
     * Bootstraps the Spring application.
     *
     * @param args application arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(BikeshopManagerApplication.class, args);
    }
}