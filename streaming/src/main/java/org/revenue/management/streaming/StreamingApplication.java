package org.revenue.management.streaming;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"org.revenue.management.common",
        "org.revenue.management.member", "org.revenue.management.gatewayservice"})
public class StreamingApplication {

    public static void main(String[] args) {
        SpringApplication.run(StreamingApplication.class, args);
    }

}
