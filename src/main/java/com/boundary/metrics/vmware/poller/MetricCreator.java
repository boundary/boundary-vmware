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

package com.boundary.metrics.vmware.poller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.boundary.metrics.vmware.client.metrics.Metric;
import com.boundary.metrics.vmware.client.metrics.MetricClient;
import com.vmware.vim25.PerfCounterInfo;
/**
 * Uses the list of metrics in the configuration file, performance counter metadata, and a metrics client
 * to create the needed metric definitions
 *
 */
public class MetricCreator {
	
	private static final Logger LOG = LoggerFactory.getLogger(MetricCreator.class);
	
	private MetricClient client;
	private PerformanceCounterMetadata metadata;
	private Map<String, Metric> metrics;
	
	/**
	 * Constructor
	 * @param client {@link MetricClient}
	 * @param metadata {@link PerformanceCounterMetadata}
	 * @param metrics {@link Map}
	 */
	public MetricCreator(MetricClient client,PerformanceCounterMetadata metadata,Map<String,Metric> metrics) {
		this.client = client;
		this.metadata = metadata;
		this.metrics = metrics;
	}

	/**
	 * Creates or updates metric based on configured metrics
	 */
	public void createMetricDefinitions(){
		Map<Integer,PerfCounterInfo> infoMap = metadata.getInfoMap();
		Map<String,Integer> nameMap = metadata.getNameMap();
		
        /**
         * Get the units for the metrics to be created
         */
        for (String counterName : metrics.keySet()) {
            if (infoMap.containsKey(counterName)) {
                // Ensure metric is created in HLM
                String vmwareUnit = infoMap.get(nameMap.get(counterName)).getUnitInfo().getKey();
                String metricUnit = "number";
                if (vmwareUnit.equalsIgnoreCase("kiloBytes")) {
                	metricUnit = "bytecount";
                } else if (vmwareUnit.equalsIgnoreCase("percent")) {
                	metricUnit = "percent";
                }
                client.createMetric(metrics.get(counterName).getName(),metricUnit);
            } else {
                LOG.warn("Server does not have {} metric, skipping", counterName);
            }
        }
	}
}
