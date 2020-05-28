package org.apache.skywalking.oap.server.extend.storage;

import org.apache.skywalking.oap.server.core.register.DBInstanceStatementInventory;
import org.apache.skywalking.oap.server.core.storage.cache.IDBInstanceStatementInventoryCacheDAO;
import org.apache.skywalking.oap.server.extend.storage.h2.dao.H2DBInstanceStatementInventoryCacheDAO;

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
    protected Class<? extends IDBInstanceStatementInventoryCacheDAO> mysqlImplClassType() {
        return H2DBInstanceStatementInventoryCacheDAO.class;
    }
}
