package com.boundary.metrics.vmware.client.client.metrics;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.emptyToNull;

public class Metric {

    @JsonProperty
    @NotEmpty
    private final String name;

    @JsonProperty
    @NotEmpty
    private final String displayName;

    public Metric(@JsonProperty("name") String name, @JsonProperty("displayName") String displayName) {
        this.name = MetricUtils.normalizeMetricName(checkNotNull(emptyToNull(name)));
        this.displayName = checkNotNull(emptyToNull(displayName));
    }

    public Metric(String displayName) {
        this(displayName, displayName);
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }
}
