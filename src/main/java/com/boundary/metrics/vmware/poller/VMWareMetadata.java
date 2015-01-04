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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.boundary.metrics.vmware.client.metrics.Metric;
import com.vmware.vim25.PerfCounterInfo;
import com.vmware.vim25.PerfMetricId;

/**
 * Manages all of the metadata required to collect metrics from vSphere
 */
public class VMWareMetadata {
	
    private final PerformanceCounterMetadata metadata;
	private final Map<String, Map<String, MetricDefinition>> metrics;
	private Map<String,MetricDefinition> counterToNameMap;
    
    public VMWareMetadata(PerformanceCounterMetadata metadata,Map<String, Map<String, MetricDefinition>> metrics) {
    	this.metadata = metadata;
    	this.metrics = metrics;
    	this.counterToNameMap = new HashMap<String,MetricDefinition>();
    	
    	// Build a map of performance counter names to metric identifiers
    	for (String morType : metrics.keySet()) {
    		for (String counter : metrics.get(morType).keySet()) {
    			if (!counterToNameMap.containsKey(counter)) {
    				counterToNameMap.put(counter, metrics.get(morType).get(counter));
    			}
    		}
    	}
    }
    
	public Map<String,Integer> getNameMap() {
		return metadata.getNameMap();
	}

	public Map<Integer,PerfCounterInfo> getInfoMap() {
		return metadata.getInfoMap();
	}
	
	public Map<String, MetricDefinition> getMetrics(String type) {
		return metrics.get(type);
	}

	public List<PerfMetricId> getPerfMetrics(String type) {
		return metadata.getPerformanceMetricIds(getMetrics(type));
	}
	
	public String getMetricName(String metricFullName) {
		return counterToNameMap.get(metricFullName).getMetric();
	}
}
