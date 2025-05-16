package org.cyberrealm.tech.muvio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class MuvioApplication {

    public static void main(String[] args) {
        SpringApplication.run(MuvioApplication.class, args);
    }

}
