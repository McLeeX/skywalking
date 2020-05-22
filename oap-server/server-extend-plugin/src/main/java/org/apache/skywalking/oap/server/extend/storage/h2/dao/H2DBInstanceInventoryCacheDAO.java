package org.apache.skywalking.oap.server.extend.storage.h2.dao;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.oap.server.core.register.DBInstanceInventory;
import org.apache.skywalking.oap.server.core.storage.cache.IDBInstanceInventoryCacheDAO;
import org.apache.skywalking.oap.server.library.client.jdbc.hikaricp.JDBCHikariCPClient;
import org.apache.skywalking.oap.server.storage.plugin.jdbc.h2.dao.H2SQLExecutor;

@Slf4j
public class H2DBInstanceInventoryCacheDAO extends H2SQLExecutor implements IDBInstanceInventoryCacheDAO {

    private final JDBCHikariCPClient h2Client;

    public H2DBInstanceInventoryCacheDAO(JDBCHikariCPClient h2Client) {
        this.h2Client = h2Client;
    }

    @Override
    public int getDBInstanceId(int serviceId, String dbInstance) {
        String id = DBInstanceInventory.buildId(serviceId, dbInstance);
        return getEntityIDByID(h2Client, DBInstanceInventory.SEQUENCE, DBInstanceInventory.INDEX_NAME, id);
    }

    @Override
    public DBInstanceInventory get(int dbInstanceId) {
        try {
            return (DBInstanceInventory) getByColumn(h2Client, DBInstanceInventory.INDEX_NAME, DBInstanceInventory.SEQUENCE, dbInstanceId, new DBInstanceInventory.Builder());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }
}
