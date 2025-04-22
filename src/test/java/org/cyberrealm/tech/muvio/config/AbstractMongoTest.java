package org.cyberrealm.tech.muvio.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class AbstractMongoTest {
    private static final String MONGO_7 = "mongo:7.0";
    private static final int TEST_PORT = 27017;
    private static final String SPRING_DATA_MONGODB_URI = "spring.data.mongodb.uri";

    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer(MONGO_7)
            .withExposedPorts(TEST_PORT);

    @DynamicPropertySource
    static void containersProperties(DynamicPropertyRegistry registry) {
        registry.add(SPRING_DATA_MONGODB_URI, mongoDBContainer::getReplicaSetUrl);
    }
}
