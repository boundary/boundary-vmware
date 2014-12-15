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

import org.joda.time.DateTime;

/**
 * Uses builder pattern to create {@link Measurement} instances
 */
public class MeasurementBuilder {

	/**
	 * Constructor
	 */
    MeasurementBuilder() { 
    	
    }

    private int sourceId;
    private String metric;
    private Number measurement;
    private DateTime timestamp;

    /**
     * 
     * @param sourceId
     * @return {@link MeasurementBuilder}
     */
    public MeasurementBuilder setSourceId(int sourceId) {
        this.sourceId = sourceId;
        return this;
    }

    /**
     * Sets the metric identifier
     * 
     * @param metric metric identifier
     * @return {@link MeasurementBuilder}
     */
    public MeasurementBuilder setMetric(String metric) {
        this.metric = MetricUtils.normalizeMetricName(metric);
        return this;
    }

    /**
     * Sets the value of the measurement
     * @param measurement {@link Number} value
     * @return {@link MeasurementBuilder}
     */
    public MeasurementBuilder setMeasurement(Number measurement) {
        this.measurement = measurement;
        return this;
    }

    /**
     * Sets the timestamp of the measurement
     * 
     * @param timestamp Time of the measurement
     * @return {@link MeasurementBuilder}
     */
    public MeasurementBuilder setTimestamp(DateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    /**
     * Returns a {@link Measurement} instance
     * @return {@link Measurement}
     */
    public Measurement build() {
        return new Measurement(sourceId, metric, measurement, timestamp);
    }
}
