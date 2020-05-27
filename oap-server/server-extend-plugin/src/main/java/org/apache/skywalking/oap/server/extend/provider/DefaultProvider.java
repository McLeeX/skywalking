package org.apache.skywalking.oap.server.extend.provider;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.skywalking.oap.server.core.CoreModule;
import org.apache.skywalking.oap.server.core.cache.IDBInstanceInventoryCache;
import org.apache.skywalking.oap.server.core.cache.IDBInstanceStatementInventoryCache;
import org.apache.skywalking.oap.server.core.extend.ExtendModule;
import org.apache.skywalking.oap.server.core.register.service.IDBInstanceInventoryRegister;
import org.apache.skywalking.oap.server.core.register.service.IDBInstanceStatementInventoryRegister;
import org.apache.skywalking.oap.server.core.server.JettyHandlerRegister;
import org.apache.skywalking.oap.server.core.storage.StorageModule;
import org.apache.skywalking.oap.server.core.storage.cache.IDBInstanceInventoryCacheDAO;
import org.apache.skywalking.oap.server.core.storage.cache.IDBInstanceStatementInventoryCacheDAO;
import org.apache.skywalking.oap.server.extend.cache.DBInstanceInventoryCache;
import org.apache.skywalking.oap.server.extend.cache.DBInstanceStatementInventoryCache;
import org.apache.skywalking.oap.server.extend.config.ExtendModuleConfig;
import org.apache.skywalking.oap.server.extend.exporter.ExporterHandler;
import org.apache.skywalking.oap.server.extend.register.DBInstanceInventoryRegister;
import org.apache.skywalking.oap.server.extend.register.DBInstanceStatementInventoryRegister;
import org.apache.skywalking.oap.server.extend.storage.DAOProxy;
import org.apache.skywalking.oap.server.extend.storage.DBInstanceInventoryCacheDAOProxy;
import org.apache.skywalking.oap.server.extend.storage.DBInstanceStatementInventoryCacheDAOProxy;
import org.apache.skywalking.oap.server.library.module.ModuleConfig;
import org.apache.skywalking.oap.server.library.module.ModuleDefine;
import org.apache.skywalking.oap.server.library.module.ModuleProvider;
import org.apache.skywalking.oap.server.library.module.ModuleStartException;
import org.apache.skywalking.oap.server.library.module.ServiceNotProvidedException;

public class DefaultProvider extends ModuleProvider {

    private final ExtendModuleConfig config;
    private final List<DAOProxy<?>> daoProxyList = new LinkedList<>();
    private ExporterHandler exporterHandler;

    public DefaultProvider() {
        config = new ExtendModuleConfig();
    }

    @Override
    public String name() {
        return "default";
    }

    @Override
    public Class<? extends ModuleDefine> module() {
        return ExtendModule.class;
    }

    @Override
    public ModuleConfig createConfigBeanIfAbsent() {
        return config;
    }

    @Override
    public void prepare() throws ServiceNotProvidedException {
        DBInstanceInventoryCacheDAOProxy dbInstanceInventoryCacheDAOProxy = new DBInstanceInventoryCacheDAOProxy();
        DBInstanceStatementInventoryCacheDAOProxy dbInstanceStatementInventoryCacheDAOProxy = new DBInstanceStatementInventoryCacheDAOProxy();
        daoProxyList.add(dbInstanceInventoryCacheDAOProxy);
        daoProxyList.add(dbInstanceStatementInventoryCacheDAOProxy);
        this.registerServiceImplementation(IDBInstanceInventoryCacheDAO.class, dbInstanceInventoryCacheDAOProxy);
        this.registerServiceImplementation(IDBInstanceStatementInventoryCacheDAO.class, dbInstanceStatementInventoryCacheDAOProxy);
        this.registerServiceImplementation(IDBInstanceInventoryCache.class, new DBInstanceInventoryCache(getManager()));
        this.registerServiceImplementation(IDBInstanceStatementInventoryCache.class, new DBInstanceStatementInventoryCache(getManager()));
        this.registerServiceImplementation(IDBInstanceInventoryRegister.class, new DBInstanceInventoryRegister(getManager()));
        this.registerServiceImplementation(IDBInstanceStatementInventoryRegister.class, new DBInstanceStatementInventoryRegister(getManager()));

        exporterHandler = new ExporterHandler(getManager());
    }

    @Override
    public void start() throws ServiceNotProvidedException, ModuleStartException {
        ModuleProvider storageProvider = (ModuleProvider) getManager().find(StorageModule.NAME).provider();
        for (DAOProxy<?> daoProxy : daoProxyList) {
            daoProxy.init(storageProvider);
        }

        try {
            exporterHandler.start();
            JettyHandlerRegister jettyHandlerRegister = getManager().find(CoreModule.NAME).provider().getService(JettyHandlerRegister.class);
            jettyHandlerRegister.addHandler(exporterHandler);
        } catch (IOException e) {
            throw new ModuleStartException("启动 exporter 失败。", e);
        }
    }

    @Override
    public void notifyAfterCompleted() throws ServiceNotProvidedException {

    }

    @Override
    public String[] requiredModules() {
        return new String[]{CoreModule.NAME, StorageModule.NAME};
    }
}
