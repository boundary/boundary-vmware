package com.boundary.metrics.vmware.client.client.metrics;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;
import org.joda.time.DateTime;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public class Measurement {

    @Min(0)
    @JsonProperty("source")
    private final Integer sourceId;

    @JsonProperty
    @NotEmpty
    private final String metric;

    @JsonProperty("measure")
    @NotNull
    private final Number measurement;

    @JsonProperty
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private final DateTime timestamp;

    public Measurement(Integer sourceId, String metric, Number measurement, DateTime timestamp) {
        this.sourceId = checkNotNull(sourceId);
        this.metric = checkNotNull(metric);
        this.measurement = checkNotNull(measurement);
        this.timestamp = checkNotNull(timestamp);
    }

    public int getSourceId() {
        return sourceId;
    }

    public String getMetric() {
        return metric;
    }

    public Number getMeasurement() {
        return measurement;
    }

    public DateTime getTimestamp() {
        return timestamp;
    }

    public static MeasurementBuilder builder() {
        return new MeasurementBuilder();
    }
}
