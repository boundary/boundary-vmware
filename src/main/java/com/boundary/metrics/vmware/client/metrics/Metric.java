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
import static com.google.common.base.Strings.emptyToNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Metadata for a single HLM metric.
 */
public class Metric {

    @JsonProperty
    @NotEmpty
    private final String name;

    @JsonProperty
    @NotEmpty
    private final String displayName;

    /**
     * Constructor
     * @param name Immutable metric identifier
     * @param displayName Metric display name
     */
    public Metric(@JsonProperty("name") String name,
    		      @JsonProperty("displayName") String displayName) {
        this.name = MetricUtils.normalizeMetricName(checkNotNull(emptyToNull(name)));
        this.displayName = checkNotNull(emptyToNull(displayName));
    }

    /**
     * Creates an instance from the display name alone.
     * 
     * @param displayName metric display name
     */
    public Metric(String displayName) {
        this(displayName, displayName);
    }

    /**
     * Returns the metric identifier
     * @return {@link String}
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the display name
     * @return {@link String}
     */
    public String getDisplayName() {
        return displayName;
    }
}
