package org.cyberrealm.tech.muvio.service;

import org.springframework.context.SmartLifecycle;

public interface SyncSchedulerService extends SmartLifecycle {
    void worker();
}
