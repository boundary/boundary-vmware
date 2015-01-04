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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.boundary.metrics.vmware.client.metrics.Metric;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.vmware.vim25.PerfCounterInfo;
import com.vmware.vim25.PerfMetricId;

/**
 * Contains all of the Performance Counter Metadata
 */
public class PerformanceCounterMetadata {
	

	
	private ImmutableMap.Builder<Integer,PerfCounterInfo> infoMap;
	private ImmutableMap.Builder<String,Integer> nameMap;

	private List<PerfMetricId> performanceMetricIds;
	
	/**
	 * Constructore
	 * @param infoMap Mapping of performance counter id and {@link PerfCounterInfo}
	 * @param nameMap Mapping
	 */
	public PerformanceCounterMetadata() {
		this.infoMap = ImmutableMap.builder();
		this.nameMap = ImmutableMap.builder();
	}
	
	/**
	 * Add a {@link PerfCounterInfo} instance to the collection.
	 * 
	 * @param counterInfo {@link PerfCounterInfo}
	 */
	public void put(PerfCounterInfo counterInfo) {
		infoMap.put(counterInfo.getKey(),counterInfo);
		nameMap.put(toFullName(counterInfo), counterInfo.getKey());
	}

	public Map<String,Integer> getNameMap() {
		return nameMap.build();
	}
	
	/**
	 * Returns a {@link Map} of the collection that maps performance
	 * counter id to {@link PerfCounterInfo}
	 * 
	 * @return Contents of the collection 
	 */
	public Map<Integer,PerfCounterInfo> getInfoMap() {
		return infoMap.build();
	}
	
	/**
	 * Returns a list of {@link PerfMetricId}s which are used to identify metrics to collect.
	 * 
	 * @param metrics {@link Map} Mapping of performance counter name to {@link MetricDefinition}
	 * @return {@link List} of {@link PerfMetricId} instances
	 */
	public List<PerfMetricId> getPerformanceMetricIds(Map<String, MetricDefinition> metrics) {
		Map<String,Integer> nameMap = this.getNameMap();
		this.performanceMetricIds = new ArrayList<PerfMetricId>();
		
		for (String counterName : metrics.keySet()) {
			if (nameMap.containsKey(counterName)) {
				PerfMetricId metricId = new PerfMetricId();
				// Get the ID for this counter.
				metricId.setCounterId(nameMap.get(counterName));
				metricId.setInstance("*");
				performanceMetricIds.add(metricId);
			}
		}
		return this.performanceMetricIds;
	}
	
	 /**
     * Method to ratio the sampled value based on units
     * 
     * @param metricInfo {@link PerfCounterInfo} Contains information about
     * @param value Sampled Value
     * @return {@link Number} New value
     */
    public static Number computeValue(PerfCounterInfo metricInfo,Number value) {
    	String key = metricInfo.getUnitInfo().getKey();
    	if (key.equalsIgnoreCase("kiloBytes")) {
    		// Convert kilobytes to bytes
			value = (long) value * 1024; 
														
		} else if (key.equalsIgnoreCase("percent")) {
			// Convert hundredth of a percent to a decimal percent
			value = new Long((long) value).doubleValue() / 10000.0;
		}
    	return value;
    }
	
	/**
	 * Helper method to generate a string use for the key for the {@link Map}
	 * @param perfCounterInfo {@link PerfCounterInfo}
	 * @return String full name of the counter.
	 */
	public static String toFullName(PerfCounterInfo perfCounterInfo) {
        return toFullName.apply(perfCounterInfo);
    }

	/**
	 * Static function name
	 */
    private static final Function<PerfCounterInfo, String> toFullName = new Function<PerfCounterInfo, String>() {
        @Nullable
        @Override
        public String apply(@Nullable PerfCounterInfo input) {
            return input == null ? null : String.format("%s.%s.%s", input.getGroupInfo().getKey(),
                    input.getNameInfo().getKey(), input.getRollupType().toString().toUpperCase());
        }
    };
    
    public static String toString(PerfMetricId input) {
    	return toStringFunction.apply(input);
    }
    
    public static final Function<PerfMetricId, String> toStringFunction = new Function<PerfMetricId, String>() {
      @Nullable
      @Override
      public String apply(@Nullable PerfMetricId input) {
          return input == null ? null : String.format("CounterID: %s, InstanceId: %s", input.getCounterId(), input.getInstance());
      }
  };
}
