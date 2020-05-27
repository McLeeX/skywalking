package org.apache.skywalking.oap.server.extend.exporter.service;

import java.util.Arrays;
import java.util.List;

import io.prometheus.client.GaugeMetricFamily;
import org.apache.skywalking.oap.server.core.CoreModule;
import org.apache.skywalking.oap.server.core.cache.ServiceInventoryCache;
import org.apache.skywalking.oap.server.core.register.ServiceInventory;
import org.apache.skywalking.oap.server.extend.exporter.Exporter;
import org.apache.skywalking.oap.server.extend.exporter.PrometheusExporter;
import org.apache.skywalking.oap.server.library.module.ModuleManager;

@PrometheusExporter
public class ServiceCpmExporter extends Exporter {

    private ServiceInventoryCache serviceInventoryCache;

    public ServiceCpmExporter(ModuleManager moduleManager) {
        super(moduleManager);
    }

    private ServiceInventoryCache getServiceInventoryCache() {
        if (serviceInventoryCache == null) {
            serviceInventoryCache = this.findService(CoreModule.NAME, ServiceInventoryCache.class);
        }
        return serviceInventoryCache;
    }

    @Override
    protected String indName() {
        return "service_cpm";
    }

    @Override
    protected GaugeMetricFamily registryMetric() {
        return new GaugeMetricFamily("service_call_counter", "调用次数统计", Arrays.asList("serviceId", "serviceName"));
    }

    @Override
    protected List<String> labelValues(String entityId) {
        ServiceInventory service = getServiceInventoryCache().get(Integer.parseInt(entityId));
        return Arrays.asList(entityId, service.getName());
    }
}
