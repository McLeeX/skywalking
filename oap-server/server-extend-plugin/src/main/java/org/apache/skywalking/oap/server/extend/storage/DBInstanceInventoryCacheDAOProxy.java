package org.apache.skywalking.oap.server.extend.storage;

import org.apache.skywalking.oap.server.core.register.DBInstanceInventory;
import org.apache.skywalking.oap.server.core.storage.cache.IDBInstanceInventoryCacheDAO;
import org.apache.skywalking.oap.server.extend.storage.h2.dao.H2DBInstanceInventoryCacheDAO;

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
    protected Class<? extends IDBInstanceInventoryCacheDAO> mysqlImplClassType() {
        return H2DBInstanceInventoryCacheDAO.class;
    }
}
