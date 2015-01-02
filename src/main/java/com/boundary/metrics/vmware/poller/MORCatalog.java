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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.boundary.metrics.vmware.client.metrics.Metric;
import com.google.common.collect.ImmutableMap;

/**
 * Represents the catalog of items to collect metrics from
 * a vSphere end point
 *
 */
public class MORCatalog {

	private List<MetricDefinition> definitions;
	private List<MORCatalogEntry> catalog;
	
	public MORCatalog() {
	}

	/**
	 * Returns as list of {@link MetricDefinition}s which are
	 * Boundary metric definitions
	 * 
	 * @return {@link List}
	 */
	public List<MetricDefinition> getDefinitions() {
		return definitions;
	}
	
	/**
	 * Returns the list of {@link MORCatalogEntry} which contains a
	 * {@link ManagedObjectReference} type and performance counters
	 * to collect.
	 * @return {@link List}
	 */
	public List<MORCatalogEntry> getCatalog() {
		return catalog;
	}
	
	/**
	 * Helper function that returns a {@link Map} of the performance
	 * counter names (e.g. <em>cpu.usage.average</em> to {@link Metric}
	 * 
	 * @return {@link Map}
	 */
	public Map<String, Metric> getMetrics() {
		ImmutableMap.Builder<String,Metric> metrics = ImmutableMap.builder();
       
		
		for (MORCatalogEntry entry : catalog) {
			for (PerformanceCounterEntry counter : entry.getCounters()) {
			metrics.put(counter.getName(),
	        		new Metric(counter.getMetric(),"CPU Average Utilization"));
			 metrics.put("cpu.usage.AVERAGE",
		        		new Metric("SYSTEM_CPU_USAGE_AVERAGE","CPU Average Utilization"));
			}
		}
		
		return metrics.build();
	}
}
