package com.stocklms.stock_lms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class StockLmsApplication extends SpringBootServletInitializer {

        public static void main(String[] args) {
                SpringApplication.run(StockLmsApplication.class, args);
        }

}