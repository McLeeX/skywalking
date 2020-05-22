package org.apache.skywalking.oap.server.extend.storage.h2.dao;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.oap.server.core.register.DBInstanceStatementInventory;
import org.apache.skywalking.oap.server.core.storage.cache.IDBInstanceStatementInventoryCacheDAO;
import org.apache.skywalking.oap.server.library.client.jdbc.hikaricp.JDBCHikariCPClient;
import org.apache.skywalking.oap.server.storage.plugin.jdbc.h2.dao.H2SQLExecutor;

@Slf4j
public class H2DBInstanceStatementInventoryCacheDAO extends H2SQLExecutor implements IDBInstanceStatementInventoryCacheDAO {

    private final JDBCHikariCPClient h2Client;

    public H2DBInstanceStatementInventoryCacheDAO(JDBCHikariCPClient h2Client) {
        this.h2Client = h2Client;
    }

    @Override
    public int getDBInstanceStatementId(int dbInstanceId, String dbStatement) {
        String id = DBInstanceStatementInventory.buildId(dbInstanceId, dbStatement);
        return getEntityIDByID(h2Client, DBInstanceStatementInventory.SEQUENCE, DBInstanceStatementInventory.INDEX_NAME, id);
    }

    @Override
    public DBInstanceStatementInventory get(int dbInstanceStatementId) {
        try {
            return (DBInstanceStatementInventory) getByColumn(h2Client, DBInstanceStatementInventory.INDEX_NAME, DBInstanceStatementInventory.SEQUENCE, dbInstanceStatementId, new DBInstanceStatementInventory.Builder());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }
}
