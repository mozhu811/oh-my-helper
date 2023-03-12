package io.cruii;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Hello world!
 */
@SpringBootApplication
@MapperScan("io.cruii.push.mapper")
public class PushApplication {
    public static void main(String[] args) {
        SpringApplication.run(PushApplication.class, args);
    }
}
