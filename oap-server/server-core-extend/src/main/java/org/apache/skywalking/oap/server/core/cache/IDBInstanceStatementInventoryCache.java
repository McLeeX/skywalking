package org.apache.skywalking.oap.server.core.cache;

import org.apache.skywalking.oap.server.core.register.DBInstanceStatementInventory;
import org.apache.skywalking.oap.server.library.module.Service;

public interface IDBInstanceStatementInventoryCache extends Service {
    int getDBInstanceStatementId(int dbInstanceId, String dbStatement);

    DBInstanceStatementInventory get(int dbInstanceStatementId);
}
