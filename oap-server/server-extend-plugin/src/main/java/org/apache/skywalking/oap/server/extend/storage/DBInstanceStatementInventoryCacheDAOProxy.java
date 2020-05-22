package org.apache.skywalking.oap.server.extend.storage;

import java.util.Map;

import org.apache.skywalking.oap.server.core.register.DBInstanceStatementInventory;
import org.apache.skywalking.oap.server.core.storage.cache.IDBInstanceStatementInventoryCacheDAO;
import org.apache.skywalking.oap.server.extend.storage.h2.dao.H2DBInstanceStatementInventoryCacheDAO;
import org.apache.skywalking.oap.server.library.module.ModuleProvider;
import org.apache.skywalking.oap.server.storage.plugin.jdbc.mysql.MySQLStorageProvider;

public class DBInstanceStatementInventoryCacheDAOProxy extends DAOProxy<IDBInstanceStatementInventoryCacheDAO> implements IDBInstanceStatementInventoryCacheDAO {

    @Override
    public int getDBInstanceStatementId(int dbInstanceId, String dbStatement) {
        return getDAOImpl().getDBInstanceStatementId(dbInstanceId, dbStatement);
    }

    @Override
    public DBInstanceStatementInventory get(int dbInstanceStatementId) {
        return getDAOImpl().get(dbInstanceStatementId);
    }

    @Override
    protected void implClassMap(Map<Class<? extends ModuleProvider>, Class<? extends IDBInstanceStatementInventoryCacheDAO>> map) {
        map.put(MySQLStorageProvider.class, H2DBInstanceStatementInventoryCacheDAO.class);
    }
}
