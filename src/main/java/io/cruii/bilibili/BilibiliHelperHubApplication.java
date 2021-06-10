package io.cruii.bilibili;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class BilibiliHelperHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(BilibiliHelperHubApplication.class, args);
    }

}
