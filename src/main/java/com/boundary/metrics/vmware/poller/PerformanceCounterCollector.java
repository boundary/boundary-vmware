package com.boundary.metrics.vmware.poller;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.vmware.connection.Connection;
import com.vmware.vim25.ArrayOfPerfCounterInfo;
import com.vmware.vim25.DynamicProperty;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.ObjectContent;
import com.vmware.vim25.ObjectSpec;
import com.vmware.vim25.PerfCounterInfo;
import com.vmware.vim25.PropertyFilterSpec;
import com.vmware.vim25.PropertySpec;
import com.vmware.vim25.RetrieveOptions;
import com.vmware.vim25.RetrieveResult;
import com.vmware.vim25.RuntimeFaultFaultMsg;

public class PerformanceCounterCollector {

	Connection vmClient;

	public PerformanceCounterCollector(Connection vmClient) {
		this.vmClient = vmClient;
	}

	public PerformanceCounterMetadata fetchPerformanceCounters() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
		PerformanceCounterMetadata metadata = new PerformanceCounterMetadata();
		
		// Get the PerformanceManager object which is used to get metrics from
		// counters
		ManagedObjectReference pm = vmClient.getServiceContent().getPerfManager();

		ObjectSpec objectSpec = new ObjectSpec();
		objectSpec.setObj(pm);

		PropertySpec propertySpec = new PropertySpec();
		propertySpec.setType("PerformanceManager");
		propertySpec.getPathSet().add("perfCounter");

		PropertyFilterSpec filterSpec = new PropertyFilterSpec();
		filterSpec.getObjectSet().add(objectSpec);
		filterSpec.getPropSet().add(propertySpec);

		RetrieveOptions retrieveOptions = new RetrieveOptions();
		RetrieveResult retrieveResult = vmClient.getVimPort().retrievePropertiesEx(
				vmClient.getServiceContent().getPropertyCollector(),
						ImmutableList.of(filterSpec), retrieveOptions);

		for (ObjectContent oc : retrieveResult.getObjects()) {
			if (oc.getPropSet() != null) {
				for (DynamicProperty dp : oc.getPropSet()) {
					List<PerfCounterInfo> perfCounters = ((ArrayOfPerfCounterInfo) dp.getVal()).getPerfCounterInfo();
					if (perfCounters != null) {
						for (PerfCounterInfo performanceCounterInfo : perfCounters) {
							metadata.put(performanceCounterInfo);
						}
					}
				}
			}
		}
		
		return metadata;
	}
}
