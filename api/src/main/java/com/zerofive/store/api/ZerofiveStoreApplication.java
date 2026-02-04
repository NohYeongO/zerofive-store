package com.zerofive.store.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.zerofive.store")
@EntityScan(basePackages = "com.zerofive.store")
@EnableJpaRepositories(basePackages = "com.zerofive.store")
public class ZerofiveStoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZerofiveStoreApplication.class, args);
    }
}
