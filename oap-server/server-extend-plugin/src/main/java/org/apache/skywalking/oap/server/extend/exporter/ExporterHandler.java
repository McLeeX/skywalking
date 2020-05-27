package org.apache.skywalking.oap.server.extend.exporter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.MetricsServlet;
import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.oap.server.core.UnexpectedException;
import org.apache.skywalking.oap.server.core.annotation.AnnotationListener;
import org.apache.skywalking.oap.server.core.annotation.AnnotationScan;
import org.apache.skywalking.oap.server.library.module.ModuleManager;
import org.apache.skywalking.oap.server.library.server.jetty.JettyHandler;

@Slf4j
public class ExporterHandler extends JettyHandler implements AnnotationListener {

    private final ModuleManager moduleManager;

    private final MetricsServletWrapper metricsServletWrapper;

    private final CollectorRegistry registry;

    public ExporterHandler(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
        registry = new CollectorRegistry();
        metricsServletWrapper = new MetricsServletWrapper(registry);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        metricsServletWrapper.doGet(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        metricsServletWrapper.doPost(req, resp);
    }

    @Override
    public String pathSpec() {
        return "/exporter/metric";
    }

    @Override
    public Class<? extends Annotation> annotation() {
        return PrometheusExporter.class;
    }

    public void start() throws IOException {
        AnnotationScan annotationScan = new AnnotationScan();
        annotationScan.registerListener(this);
        annotationScan.scan();
    }

    @Override
    public void notify(Class aClass) {
        try {
            if (Exporter.class.isAssignableFrom(aClass)) {
                Exporter exporter = (Exporter) aClass.getConstructor(ModuleManager.class).newInstance(this.moduleManager);
                exporter.register(this.registry);
            }
        } catch (Exception e) {
            throw new UnexpectedException("初始化 prometheus exporter 失败。", e);
        }
    }

    private static class MetricsServletWrapper extends MetricsServlet {

        public MetricsServletWrapper(CollectorRegistry registry) {
            super(registry);
        }

        @Override
        public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            super.doGet(req, resp);
        }

        @Override
        public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            super.doPost(req, resp);
        }
    }
}
