package org.cyberrealm.tech.muvio.config;

import org.cyberrealm.tech.muvio.service.AwardService;
import org.cyberrealm.tech.muvio.service.TmDbService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;

@TestConfiguration
public class SpringSecurityWebAuxTestConfig {
    @MockBean
    private TmDbService tmDbService;
    @MockBean
    private AwardService awardService;
}
