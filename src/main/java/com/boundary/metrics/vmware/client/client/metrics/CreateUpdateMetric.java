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
