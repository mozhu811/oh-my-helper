package io.cruii.execution;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author cruii
 * Created on 2022/4/2
 */
@SpringBootApplication(scanBasePackages = {"io.cruii"}, exclude = DataSourceAutoConfiguration.class)
@EnableFeignClients
public class ExecutionApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExecutionApplication.class, args);
    }
}
