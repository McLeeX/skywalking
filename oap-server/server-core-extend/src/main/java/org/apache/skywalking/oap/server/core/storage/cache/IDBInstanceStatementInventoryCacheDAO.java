package org.apache.skywalking.oap.server.core.storage.cache;

import org.apache.skywalking.oap.server.core.register.DBInstanceStatementInventory;
import org.apache.skywalking.oap.server.core.storage.DAO;

public interface IDBInstanceStatementInventoryCacheDAO extends DAO {

    int getDBInstanceStatementId(int dbInstanceId, String dbStatement);

    DBInstanceStatementInventory get(int dbInstanceStatementId);
}
