// Copyright 2014 Boundary, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.boundary.metrics.vmware.client.metrics;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.validation.constraints.Max;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data structure for creating or updating a Boundary metric definition.
 */
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

    /**
     * Returns the metric identifier of the metric definition
     * @return {@link String}
     */
    @JsonProperty
    public String getName() {
        return MetricUtils.normalizeMetricName(name);
    }

    /**
     * Returns the metric description of the metric definition
     * @return {@link String}
     */
    @JsonProperty
    public String getDescription() {
        return name;
    }

    /**
     * Returns the display name of the metric definition
     * @return {@link String}
     */

    @Max(100)
    @JsonProperty
    public String getDisplayName() {
        return name;
    }

    /**
     * Returns the short display name of the metric definition
     * @return {@link String}
     */
    @Max(20)
    @JsonProperty
    public String getDisplayNameShort() {
        return name;
    }

    /**
     * Defines a metric with an identifier and the unit
     * @param name metric identifier
     * @param unit metric unit
     */
    public CreateUpdateMetric(String name, String unit) {
        this.name = checkNotNull(name);
        this.unit = checkNotNull(unit);
    }
}
