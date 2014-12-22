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

import com.boundary.metrics.vmware.client.metrics.Metric;
import com.vmware.vim25.PerfCounterInfo;

/**
 * Manages all of the metadata required to collect metrics from vSphere
 */
public class VMWareMetadata {
	
    private Map<String,Integer> performanceCounterMap;
    private Map<Integer,PerfCounterInfo> performanceCounterInfoMap;
    private Map<String,Metric> metrics;
    private String orgId;
    
    public VMWareMetadata(Map<String,Integer> performanceCounterMap,
    		Map<Integer,PerfCounterInfo> performanceCounterInfoMap,
    		Map<String,Metric> metrics,String orgId) {
    	this.performanceCounterMap = performanceCounterMap;
    	this.performanceCounterInfoMap = performanceCounterInfoMap;
    	this.metrics = metrics;
    	this.orgId = orgId;
    }
    
	public Map<String, Integer> getPerformanceCounterMap() {
		return performanceCounterMap;
	}

	public Map<Integer, PerfCounterInfo> getPerformanceCounterInfoMap() {
		return performanceCounterInfoMap;
	}

	public String getOrgId() {
		return this.orgId;
	}
	
	public Map<String,Metric> getMetrics() {
		return metrics;
	}
}
