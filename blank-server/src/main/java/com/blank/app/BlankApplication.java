package com.blank.app;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.blank.app.mapper")
public class BlankApplication {
    public static void main(String[] args) {
        SpringApplication.run(BlankApplication.class, args);
    }
}
