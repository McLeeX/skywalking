package org.apache.skywalking.oap.server.core.cache;

import org.apache.skywalking.oap.server.core.register.DBInstanceInventory;
import org.apache.skywalking.oap.server.library.module.Service;

public interface IDBInstanceInventoryCache extends Service {

    int getDbInstanceInventoryId(int serviceId, String dbInstance);

    DBInstanceInventory get(int dbInstanceId);
}
