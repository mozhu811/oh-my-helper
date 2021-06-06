package io.cruii.bilibili;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication
@MapperScan("io.cruii.bilibili.mapper")
public class BilibiliHelperHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(BilibiliHelperHubApplication.class, args);
    }

}
