package org.apache.skywalking.oap.server.core.register.service;

import org.apache.skywalking.oap.server.library.module.Service;

public interface IDBInstanceInventoryRegister extends Service {

    int getOrCreate(int serviceId, String dbInstance, int dbType);

    int get(int serviceId, String dbInstance, int dbType);

    void heartbeat(int dbInstanceId, long heartBeatTime);
}
