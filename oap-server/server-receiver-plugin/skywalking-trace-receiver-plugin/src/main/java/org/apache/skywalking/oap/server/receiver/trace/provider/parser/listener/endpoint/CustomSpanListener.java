package org.apache.skywalking.oap.server.receiver.trace.provider.parser.listener.endpoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import org.apache.skywalking.apm.network.common.KeyStringValuePair;
import org.apache.skywalking.apm.network.language.agent.UniqueId;
import org.apache.skywalking.oap.server.core.Const;
import org.apache.skywalking.oap.server.core.CoreModule;
import org.apache.skywalking.oap.server.core.cache.EndpointInventoryCache;
import org.apache.skywalking.oap.server.core.cache.ServiceInstanceInventoryCache;
import org.apache.skywalking.oap.server.core.cache.ServiceInventoryCache;
import org.apache.skywalking.oap.server.core.extend.ExtendModule;
import org.apache.skywalking.oap.server.core.register.DBInstanceInventory;
import org.apache.skywalking.oap.server.core.register.DBInstanceStatementInventory;
import org.apache.skywalking.oap.server.core.register.service.IDBInstanceInventoryRegister;
import org.apache.skywalking.oap.server.core.register.service.IDBInstanceStatementInventoryRegister;
import org.apache.skywalking.oap.server.core.source.DetectPoint;
import org.apache.skywalking.oap.server.core.source.RequestType;
import org.apache.skywalking.oap.server.core.source.SourceReceiver;
import org.apache.skywalking.oap.server.library.module.ModuleManager;
import org.apache.skywalking.oap.server.receiver.trace.provider.TraceServiceModuleConfig;
import org.apache.skywalking.oap.server.receiver.trace.provider.parser.SpanTags;
import org.apache.skywalking.oap.server.receiver.trace.provider.parser.decorator.SegmentCoreInfo;
import org.apache.skywalking.oap.server.receiver.trace.provider.parser.decorator.SpanDecorator;
import org.apache.skywalking.oap.server.receiver.trace.provider.parser.listener.EntrySpanListener;
import org.apache.skywalking.oap.server.receiver.trace.provider.parser.listener.ExitSpanListener;
import org.apache.skywalking.oap.server.receiver.trace.provider.parser.listener.GlobalTraceIdsListener;
import org.apache.skywalking.oap.server.receiver.trace.provider.parser.listener.SpanListener;
import org.apache.skywalking.oap.server.receiver.trace.provider.parser.listener.SpanListenerFactory;

public class CustomSpanListener implements EntrySpanListener, ExitSpanListener, GlobalTraceIdsListener {

    private final SourceReceiver sourceReceiver;
    private final ServiceInstanceInventoryCache instanceInventoryCache;
    private final ServiceInventoryCache serviceInventoryCache;
    private final EndpointInventoryCache endpointInventoryCache;

    private final IDBInstanceInventoryRegister dbInstanceInventoryRegister;
    private final IDBInstanceStatementInventoryRegister dbInstanceStatementInventoryRegister;

    private SpanDecorator entrySpanDecorator;
    private long minuteTimeBucket;
    private String traceId;

    private final List<DBSourceBuilder> dbSourceBuilders;

    private CustomSpanListener(ModuleManager moduleManager) {
        this.sourceReceiver = moduleManager.find(CoreModule.NAME).provider().getService(SourceReceiver.class);

        this.dbSourceBuilders = new ArrayList<>();

        this.instanceInventoryCache = moduleManager.find(CoreModule.NAME)
                                                   .provider()
                                                   .getService(ServiceInstanceInventoryCache.class);
        this.serviceInventoryCache = moduleManager.find(CoreModule.NAME)
                                                  .provider()
                                                  .getService(ServiceInventoryCache.class);
        this.endpointInventoryCache = moduleManager.find(CoreModule.NAME)
                                                   .provider()
                                                   .getService(EndpointInventoryCache.class);

        this.dbInstanceInventoryRegister = moduleManager.find(ExtendModule.NAME)
                                                        .provider()
                                                        .getService(IDBInstanceInventoryRegister.class);
        this.dbInstanceStatementInventoryRegister = moduleManager.find(ExtendModule.NAME)
                                                                 .provider()
                                                                 .getService(IDBInstanceStatementInventoryRegister.class);

        this.traceId = null;
    }

    @Override
    public void parseEntry(SpanDecorator spanDecorator, SegmentCoreInfo segmentCoreInfo) {
        this.minuteTimeBucket = segmentCoreInfo.getMinuteTimeBucket();
        this.entrySpanDecorator = spanDecorator;
    }

    @Override
    public void parseExit(SpanDecorator spanDecorator, SegmentCoreInfo segmentCoreInfo) {
        if (this.minuteTimeBucket == 0) {
            this.minuteTimeBucket = segmentCoreInfo.getMinuteTimeBucket();
        }

        SourceBuilder sourceBuilder = new SourceBuilder();

        int peerId = spanDecorator.getPeerId();
        if (peerId == 0) {
            return;
        }
        int destServiceId = serviceInventoryCache.getServiceId(peerId);
        int mappingServiceId = serviceInventoryCache.get(destServiceId).getMappingServiceId();
        int destInstanceId = instanceInventoryCache.getServiceInstanceId(destServiceId, peerId);
        int mappingServiceInstanceId = instanceInventoryCache.get(destInstanceId).getMappingServiceInstanceId();

        sourceBuilder.setSourceServiceInstanceId(segmentCoreInfo.getServiceInstanceId());
        sourceBuilder.setSourceServiceId(segmentCoreInfo.getServiceId());
        if (Const.NONE == mappingServiceId) {
            sourceBuilder.setDestServiceId(destServiceId);
        } else {
            sourceBuilder.setDestServiceId(mappingServiceId);
        }
        if (Const.NONE == mappingServiceInstanceId) {
            sourceBuilder.setDestServiceInstanceId(destInstanceId);
        } else {
            sourceBuilder.setDestServiceInstanceId(mappingServiceInstanceId);
        }
        sourceBuilder.setDetectPoint(DetectPoint.CLIENT);
        sourceBuilder.setComponentId(spanDecorator.getComponentId());
        setPublicAttrs(sourceBuilder, spanDecorator);

        if (sourceBuilder.getType().equals(RequestType.DATABASE)) {
            DBSourceBuilder dbSourceBuilder = new DBSourceBuilder(sourceBuilder);
            for (KeyStringValuePair tag : spanDecorator.getAllTags()) {
                if (SpanTags.DB_INSTANCE.equals(tag.getKey())) {
                    dbSourceBuilder.setDbInstance(tag.getValue());
                } else if (SpanTags.DB_STATEMENT.equals(tag.getKey())) {
                    dbSourceBuilder.setDbStatement(Optional.ofNullable(tag.getValue()).map(String::trim).orElse(null));
                } else if (SpanTags.DB_BIND_VARIABLES.equals(tag.getKey())) {
                    dbSourceBuilder.setDbBindVariables(tag.getValue());
                } else if (SpanTags.DB_TYPE.equals(tag.getKey())) {
                    dbSourceBuilder.setDbType(tag.getValue());
                }
            }
            dbSourceBuilders.add(dbSourceBuilder);
        }
    }

    private void setPublicAttrs(SourceBuilder sourceBuilder, SpanDecorator spanDecorator) {
        long latency = spanDecorator.getEndTime() - spanDecorator.getStartTime();
        sourceBuilder.setLatency((int) latency);
        sourceBuilder.setResponseCode(Const.NONE);
        sourceBuilder.setStatus(!spanDecorator.getIsError());

        switch (spanDecorator.getSpanLayer()) {
            case Http:
                sourceBuilder.setType(RequestType.HTTP);
                break;
            case Database:
                sourceBuilder.setType(RequestType.DATABASE);
                break;
            default:
                sourceBuilder.setType(RequestType.RPC);
                break;
        }

        sourceBuilder.setSourceServiceName(serviceInventoryCache.get(sourceBuilder.getSourceServiceId()).getName());
        sourceBuilder.setSourceServiceInstanceName(
                instanceInventoryCache.get(sourceBuilder.getSourceServiceInstanceId())
                                      .getName());
        if (sourceBuilder.getSourceEndpointId() != Const.NONE) {
            sourceBuilder.setSourceEndpointName(endpointInventoryCache.get(sourceBuilder.getSourceEndpointId())
                                                                      .getName());
        }
        sourceBuilder.setDestServiceName(serviceInventoryCache.get(sourceBuilder.getDestServiceId()).getName());
        sourceBuilder.setDestServiceInstanceName(instanceInventoryCache.get(sourceBuilder.getDestServiceInstanceId())
                                                                       .getName());
        if (sourceBuilder.getDestEndpointId() != Const.NONE) {
            sourceBuilder.setDestEndpointName(endpointInventoryCache.get(sourceBuilder.getDestEndpointId()).getName());
        }
    }

    @Override
    public void parseGlobalTraceId(UniqueId uniqueId, SegmentCoreInfo segmentCoreInfo) {
        if (traceId == null) {
            traceId = uniqueId.getIdPartsList().stream().map(String::valueOf).collect(Collectors.joining("."));
        }
    }

    @Override
    public void build() {

        dbSourceBuilders.forEach(dbSourceBuilder -> {
            SourceBuilder exitSourceBuilder = dbSourceBuilder.getSourceBuilder();
            if (nonNull(entrySpanDecorator)) {
                exitSourceBuilder.setSourceEndpointId(entrySpanDecorator.getOperationNameId());
            } else {
                exitSourceBuilder.setSourceEndpointId(Const.USER_ENDPOINT_ID);
            }
            exitSourceBuilder.setSourceEndpointName(endpointInventoryCache.get(exitSourceBuilder.getSourceEndpointId())
                                                                          .getName());

            exitSourceBuilder.setTimeBucket(minuteTimeBucket);
            int dbInstanceId = Const.NONE;
            int serviceId = dbSourceBuilder.getSourceBuilder().getDestServiceId();
            String dbInstance = dbSourceBuilder.getDbInstance();
            int dbType = dbSourceBuilder.getSourceBuilder().getComponentId();
            while (dbInstanceId == Const.NONE) {
                dbInstanceId = dbInstanceInventoryRegister.getOrCreate(serviceId, dbInstance, dbType);
            }
            int dbInstanceStatementId = Const.NONE;
            String dbStatement = dbSourceBuilder.getDbStatement();
            while (dbInstanceStatementId == Const.NONE) {
                dbInstanceStatementId = dbInstanceStatementInventoryRegister.getOrCreate(dbInstanceId, dbStatement);
            }
            dbSourceBuilder.setDbInstanceId(dbInstanceId);
            dbSourceBuilder.setDbInstanceName(DBInstanceInventory.buildId(serviceId, dbInstance));
            dbSourceBuilder.setDbInstanceStatementId(dbInstanceStatementId);
            dbSourceBuilder.setDbInstanceStatementName(DBInstanceStatementInventory.buildId(dbInstanceId, dbStatement));
            sourceReceiver.receive(dbSourceBuilder.toDBInstance());
            sourceReceiver.receive(dbSourceBuilder.toDBInstanceStatement());
        });
    }

    @Override
    public boolean containsPoint(Point point) {
        return Point.Entry.equals(point) || Point.Exit.equals(point) || Point.TraceIds.equals(point);
    }

    public static class Factory implements SpanListenerFactory {

        @Override
        public SpanListener create(ModuleManager moduleManager, TraceServiceModuleConfig config) {
            return new CustomSpanListener(moduleManager);
        }
    }
}
