package org.apache.skywalking.oap.server.core.analysis.metrics;

import lombok.Getter;
import lombok.Setter;
import org.apache.skywalking.oap.server.core.analysis.metrics.annotation.Arg;
import org.apache.skywalking.oap.server.core.analysis.metrics.annotation.ConstOne;
import org.apache.skywalking.oap.server.core.analysis.metrics.annotation.Entrance;
import org.apache.skywalking.oap.server.core.analysis.metrics.annotation.MetricsFunction;
import org.apache.skywalking.oap.server.core.storage.annotation.Column;

@MetricsFunction(functionName = "statistics")
public abstract class StatisticsMetrics extends Metrics {

    protected static final String COUNT = "count";
    protected static final String CPM = "cpm";
    protected static final String ERROR_COUNT = "error_count";
    protected static final String ERROR_RATE = "error_rate";
    protected static final String SUCCESS_RATE = "success_rate";
    protected static final String LATENCY_TOTAL = "latency_total";
    protected static final String LATENCY_AVG = "latency_avg";
    protected static final String LATEST_ERROR_TIME_BUCKET = "latest_error_time_bucket";

    @Getter
    @Setter
    @Column(columnName = COUNT)
    private long count;
    @Getter
    @Setter
    @Column(columnName = ERROR_COUNT)
    private long errorCount;
    @Getter
    @Setter
    @Column(columnName = LATENCY_TOTAL)
    private long latencyTotal;
    @Getter
    @Setter
    @Column(columnName = LATEST_ERROR_TIME_BUCKET)
    private long latestErrorTimeBucket;


    @Getter
    @Setter
    @Column(columnName = CPM)
    private long cpm;
    @Getter
    @Setter
    @Column(columnName = ERROR_RATE)
    private int errorRate;
    @Getter
    @Setter
    @Column(columnName = SUCCESS_RATE)
    private int successRate;
    @Getter
    @Setter
    @Column(columnName = LATENCY_AVG)
    private long latencyAvg;

    @Entrance
    public final void combine(@ConstOne long count, @ConstOne long error, @Arg boolean status, @Arg long latency, @Arg long timeBucket) {
        this.count += count;
        if (!status) {
            errorCount += error;
            if (timeBucket > latestErrorTimeBucket) {
                this.latestErrorTimeBucket = timeBucket;
            }
        }
        this.latencyTotal += latency;
    }

    @Override
    public final void combine(Metrics metrics) {
        StatisticsMetrics statisticsMetrics = (StatisticsMetrics) metrics;
        combine(statisticsMetrics.count, statisticsMetrics.errorCount, false, statisticsMetrics.latencyTotal, statisticsMetrics.latestErrorTimeBucket);
    }

    @Override
    public void calculate() {
        this.cpm = count / getDurationInMinute();
        this.errorRate = (int) (errorCount * 10000 / count);
        this.successRate = 10000 - errorRate;
        this.latencyAvg = latencyTotal / count;
    }

}
