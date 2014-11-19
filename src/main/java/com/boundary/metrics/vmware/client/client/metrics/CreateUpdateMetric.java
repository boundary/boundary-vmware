package com.boundary.metrics.vmware.client.client.metrics;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import static com.google.common.base.Preconditions.checkNotNull;

public class CreateUpdateMetric {

    @JsonProperty
    @NotEmpty
    private String name;

    @JsonProperty
    @NotEmpty
    private String unit;

    @JsonProperty
    @NotEmpty
    private String defaultAggregate = "avg";

    @JsonProperty("isDisabled")
    private boolean disabled = false;

    @JsonProperty
    public String getName() {
        return MetricUtils.normalizeMetricName(name);
    }

    @JsonProperty
    public String getDescription() {
        return name;
    }

    @JsonProperty
    public String getDisplayName() {
        return name;
    }

    @JsonProperty
    public String getDisplayNameShort() {
        return name;
    }

    public CreateUpdateMetric(String name, String unit) {
        this.name = checkNotNull(name);
        this.unit = checkNotNull(unit);
    }
}
