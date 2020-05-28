package org.apache.skywalking.oap.server.extend.storage;

import java.util.HashMap;
import java.util.Map;

import org.apache.skywalking.oap.server.library.client.jdbc.hikaricp.JDBCHikariCPClient;
import org.apache.skywalking.oap.server.library.module.ModuleProvider;
import org.apache.skywalking.oap.server.library.module.ModuleStartException;
import org.apache.skywalking.oap.server.storage.plugin.jdbc.mysql.MySQLStorageProvider;

public abstract class DAOProxy<T> {

    private final Map<Class<? extends ModuleProvider>, Class<? extends T>> map;
    private T cacheDAO;

    public DAOProxy() {
        map = new HashMap<>();
        this.implClassMap(map);
    }

    protected abstract Class<? extends T> mysqlImplClassType();

    protected void implClassMap(Map<Class<? extends ModuleProvider>, Class<? extends T>> map) {
        map.put(MySQLStorageProvider.class, mysqlImplClassType());
    }

    protected T getDAOImpl() {
        return cacheDAO;
    }

    public void init(ModuleProvider storageProvider) throws ModuleStartException {
        if (storageProvider instanceof MySQLStorageProvider) {
            JDBCHikariCPClient mysqlClient = ((MySQLStorageProvider) storageProvider).getMysqlClient();
            Class<? extends T> daoClass = map.get(MySQLStorageProvider.class);
            try {
                cacheDAO = daoClass.getConstructor(JDBCHikariCPClient.class).newInstance(mysqlClient);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            throw new ModuleStartException("EXTEND_MODULE " + storageProvider.name() + " 不支持此类型的后端存储。");
        }
    }

}
