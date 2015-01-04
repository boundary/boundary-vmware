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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.boundary.metrics.vmware.client.metrics.Metric;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.vmware.vim25.ManagedObjectReference;

/**
 * Represents the catalog of items to collect metrics from
 * a vSphere end point
 *
 */
public class MORCatalog {
	
	private static final Logger LOG = LoggerFactory.getLogger(MORCatalog.class);

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
	 * Performs check to ensure that metrics reference in the catalog are defined
	 * 
	 * @return {@link boolean}
	 */
	public boolean isValid() {
		boolean valid = true;
		
		Map<String,MetricDefinition> definitionMap = new HashMap<String,MetricDefinition>();
		
		for (MetricDefinition def : definitions) {
			definitionMap.put(def.getMetric(),def);
		}
		
		for (MORCatalogEntry entry : catalog) {
			for (PerformanceCounterEntry counter : entry.getCounters()) {
				MetricDefinition definition = definitionMap.get(counter.getMetric());
				if (definition == null) {
					LOG.debug("No metric definition for {}",counter.getMetric());
					valid = false;
					break;
				}
			}
		}

		return valid;
	}
	
	/**
	 * Helper function that returns a MOR type {@link Map} of a {@link Map} of performance
	 * counter names (e.g. <em>cpu.usage.average</em> to {@link Metric}
	 * 
	 * @return {@link Map}
	 */
	public Map<String,Map<String, MetricDefinition>> getMetrics() {
		Builder<String,Map<String,MetricDefinition>> metrics = ImmutableMap.builder();
		Map<String,MetricDefinition> definitionMap = new HashMap<String,MetricDefinition>();
		
		for (MetricDefinition def : definitions) {
			definitionMap.put(def.getMetric(), def);
		}
	
		for (MORCatalogEntry entry : catalog) {
			String type = entry.getType();
			HashMap<String,MetricDefinition> counterMap = new HashMap<String,MetricDefinition>();
			for (PerformanceCounterEntry counter : entry.getCounters()) {
				MetricDefinition def = definitionMap.get(counter.getMetric());
				checkNotNull(def);
				counterMap.put(counter.getName(),def);
			}
			metrics.put(type,counterMap);
		}
		
		return metrics.build();
	}
}
