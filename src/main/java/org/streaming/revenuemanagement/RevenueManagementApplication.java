package org.streaming.revenuemanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"org.revenue.management.batch", "org.revenue.management.common",
        "org.revenue.management.member", "org.revenue.management.streaming"})
public class RevenueManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(RevenueManagementApplication.class, args);
    }

}
