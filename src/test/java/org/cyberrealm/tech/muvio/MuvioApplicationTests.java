package org.cyberrealm.tech.muvio;

import org.cyberrealm.tech.muvio.config.SpringSecurityWebAuxTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(SpringSecurityWebAuxTestConfig.class)
class MuvioApplicationTests {

    @Test
    void contextLoads() {
    }

}
