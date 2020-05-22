package org.apache.skywalking.oap.server.extend.storage;

import java.util.Map;

import org.apache.skywalking.oap.server.core.register.DBInstanceInventory;
import org.apache.skywalking.oap.server.core.storage.cache.IDBInstanceInventoryCacheDAO;
import org.apache.skywalking.oap.server.extend.storage.h2.dao.H2DBInstanceInventoryCacheDAO;
import org.apache.skywalking.oap.server.library.module.ModuleProvider;
import org.apache.skywalking.oap.server.storage.plugin.jdbc.mysql.MySQLStorageProvider;

public class DBInstanceInventoryCacheDAOProxy extends DAOProxy<IDBInstanceInventoryCacheDAO> implements IDBInstanceInventoryCacheDAO {

    @Override
    public int getDBInstanceId(int serviceId, String dbInstance) {
        return getDAOImpl().getDBInstanceId(serviceId, dbInstance);
    }

    @Override
    public DBInstanceInventory get(int dbInstanceId) {
        return getDAOImpl().get(dbInstanceId);
    }

    @Override
    protected void implClassMap(Map<Class<? extends ModuleProvider>, Class<? extends IDBInstanceInventoryCacheDAO>> map) {
        map.put(MySQLStorageProvider.class, H2DBInstanceInventoryCacheDAO.class);
    }
}
