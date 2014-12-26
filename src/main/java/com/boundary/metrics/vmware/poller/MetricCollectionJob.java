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
public class VMWareCollectionJob {
	
	private final VMWareMetadata metadata;
	private final Connection vmwareClient;
	private final MetricsClient metricsClient;
	private final MeterManagerClient meterClient;
	
	/**
	 * Consteructor
	 * @param metadata {@link VMWareMetadata} all data required to collect metrics
	 * @param vmwareClient Handles connection to vSphere end point
	 * @param metricsClient Handles metrics API connection to Boundary
	 * @param meterManagerClient Handles meter API connection to Boundary
	 */
	public VMWareCollectionJob(VMWareMetadata metadata,Connection vmwareClient,
			MetricsClient metricsClient,MeterManagerClient meterClient) {
		this.metadata = metadata;
		this.vmwareClient = vmwareClient;
		this.metricsClient = metricsClient;
		this.meterClient = meterClient;
	}

	public ManagedObjectReference getRootMOR() {
		return vmwareClient.getServiceContent().getRootFolder();
	}

	public DateTime getTimeAtEndPoint() throws RuntimeFaultFaultMsg {
		return TimeUtils.toDateTime(vmwareClient.getVimPort().currentTime(vmwareClient.getServiceInstanceReference()));
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

	public Connection getVMWareClient() {
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
