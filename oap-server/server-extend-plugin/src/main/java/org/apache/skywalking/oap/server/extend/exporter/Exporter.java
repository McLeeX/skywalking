package org.apache.skywalking.oap.server.extend.exporter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import io.prometheus.client.Collector;
import io.prometheus.client.GaugeMetricFamily;
import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.oap.server.core.analysis.Downsampling;
import org.apache.skywalking.oap.server.core.analysis.TimeBucket;
import org.apache.skywalking.oap.server.core.query.entity.Order;
import org.apache.skywalking.oap.server.core.query.entity.TopNEntity;
import org.apache.skywalking.oap.server.core.storage.StorageModule;
import org.apache.skywalking.oap.server.core.storage.annotation.ValueColumnMetadata;
import org.apache.skywalking.oap.server.core.storage.query.IAggregationQueryDAO;
import org.apache.skywalking.oap.server.library.module.ModuleManager;
import org.apache.skywalking.oap.server.library.module.Service;

@Slf4j
public abstract class Exporter extends Collector {

    private final ModuleManager moduleManager;

    private IAggregationQueryDAO aggregationQueryDAO;

    protected abstract String indName();

    protected abstract GaugeMetricFamily registryMetric();

    protected abstract List<String> labelValues(String entityId);

    //上1分钟的数据
    protected int getTimeInterval() {
        return 0;
    }

    protected <SERVICE extends Service> SERVICE findService(String moduleName, Class<SERVICE> serviceType) {
        return moduleManager.find(moduleName).provider().getService(serviceType);
    }

    private IAggregationQueryDAO getAggregationQueryDAO() {
        if (aggregationQueryDAO == null) {
            aggregationQueryDAO = findService(StorageModule.NAME, IAggregationQueryDAO.class);
        }
        return aggregationQueryDAO;
    }

    @Override
    public List<MetricFamilySamples> collect() {
        String indName = indName();
        long lastMinute = System.currentTimeMillis() - 60 * 1000;
        long endTB = TimeBucket.getMinuteTimeBucket(lastMinute);
        long startTB = TimeBucket.getMinuteTimeBucket(lastMinute - getTimeInterval());
        GaugeMetricFamily metric = this.registryMetric();
        try {
            List<TopNEntity> res = getAggregationQueryDAO().getServiceTopN(indName, ValueColumnMetadata.INSTANCE.getValueCName(indName), Integer.MAX_VALUE, Downsampling.Minute, startTB, endTB, Order.ASC);
            res.forEach(entity -> metric.addMetric(labelValues(entity.getId()), entity.getValue()));
        } catch (IOException e) {
            log.error("统计查询错误。", e);
        }
        return Collections.singletonList(metric);
    }

    public Exporter(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }

}
