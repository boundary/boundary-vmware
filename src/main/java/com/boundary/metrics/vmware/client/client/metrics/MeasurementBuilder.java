package com.boundary.metrics.vmware.client.client.metrics;

import org.joda.time.DateTime;

public class MeasurementBuilder {

    MeasurementBuilder() { }

    private int sourceId;
    private String metric;
    private Number measurement;
    private DateTime timestamp;

    public MeasurementBuilder setSourceId(int sourceId) {
        this.sourceId = sourceId;
        return this;
    }

    public MeasurementBuilder setMetric(String metric) {
        this.metric = MetricUtils.normalizeMetricName(metric);
        return this;
    }

    public MeasurementBuilder setMeasurement(Number measurement) {
        this.measurement = measurement;
        return this;
    }

    public MeasurementBuilder setTimestamp(DateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public Measurement build() {
        return new Measurement(sourceId, metric, measurement, timestamp);
    }
}
