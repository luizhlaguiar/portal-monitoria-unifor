package com.unifor.monitoria;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class MonitoriaApplication {

    public static void main(String[] args) {
        SpringApplication.run(MonitoriaApplication.class, args);
    }
}
