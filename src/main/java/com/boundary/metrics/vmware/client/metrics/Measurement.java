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

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data structure that is used to communicate a time series measurement
 * of a metric definition
 */
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
    

    @Override
	public String toString() {
		return "Measurement [sourceId=" + sourceId + ", metric=" + metric
				+ ", measurement=" + measurement + ", timestamp=" + timestamp
				+ "]";
	}

	public static MeasurementBuilder builder() {
        return new MeasurementBuilder();
    }
}
