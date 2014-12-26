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

import org.joda.time.DateTime;

import com.boundary.metrics.vmware.client.client.meter.manager.MeterManagerClient;
import com.boundary.metrics.vmware.client.metrics.Metric;
import com.boundary.metrics.vmware.client.metrics.MetricsClient;
import com.boundary.metrics.vmware.util.TimeUtils;
import com.vmware.connection.Connection;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.PerfCounterInfo;
import com.vmware.vim25.RuntimeFaultFaultMsg;

/**
 * Encapsulates all the necessary resources to collect metrics from
 * vSphere endpoint by the {@link VMWareMetricCollector}
 *
 */
public class MetricCollectionJob {
	
	private final VMWareMetadata metadata;
	private final VMwareClient vmwareClient;
	private final MetricsClient metricsClient;
	private final MeterManagerClient meterClient;
	
	/**
	 * Consteructor
	 * @param metadata {@link VMWareMetadata} all data required to collect metrics
	 * @param vmwareClient {@link VMwareClient} Handles connection to vSphere end point
	 * @param metricsClient {@link MetricsClient} Handles metrics API connection to Boundary
	 * @param meterManagerClient {@link MeterManagerClient} Handles meter API connection to Boundary
	 */
	public MetricCollectionJob(VMWareMetadata metadata,VMwareClient vmwareClient,
			MetricsClient metricsClient,MeterManagerClient meterClient) {
		this.metadata = metadata;
		this.vmwareClient = vmwareClient;
		this.metricsClient = metricsClient;
		this.meterClient = meterClient;
	}

	public ManagedObjectReference getRootMOR() {
		return vmwareClient.getServiceContent().getRootFolder();
	}

	public String getHost() {
		return vmwareClient.getHost();
	}

	public Map<String, Integer> getPerformanceCounterMap() {
		return metadata.getPerformanceCounterMap();
	}

	public Map<String,Metric> getMetrics() {
		return metadata.getMetrics();
	}

	public VMwareClient getVMWareClient() {
		return this.vmwareClient;
	}

	public Map<Integer, PerfCounterInfo> getPerformanceCounterInfoMap() {
		return metadata.getPerformanceCounterInfoMap();
	}

	public MeterManagerClient getMeterClient() {
		return this.meterClient;
	}

	public String getOrgId() {
		return metadata.getOrgId();
	}

	public MetricsClient getMetricsClient() {
		return this.metricsClient;
	}
}
