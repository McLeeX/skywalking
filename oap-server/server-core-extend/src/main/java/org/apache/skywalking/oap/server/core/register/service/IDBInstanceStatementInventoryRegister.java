package org.apache.skywalking.oap.server.core.register.service;

import org.apache.skywalking.oap.server.library.module.Service;

public interface IDBInstanceStatementInventoryRegister extends Service {

    int getOrCreate(int dbInstanceId, String dbStatement);

    int get(int dbInstanceId, String dbStatement);

    void heartbeat(int dbInstanceStatementId, long heartBeatTime);
}
