package org.apache.skywalking.oap.server.core.storage.cache;

import org.apache.skywalking.oap.server.core.register.DBInstanceInventory;
import org.apache.skywalking.oap.server.core.storage.DAO;

public interface IDBInstanceInventoryCacheDAO extends DAO {

    int getDBInstanceId(int serviceId, String dbInstance);

    DBInstanceInventory get(int dbInstanceId);
}
