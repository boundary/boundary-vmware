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

/**
 * Extracts the performance counters from the end point. Performance counters are then associated with end point
 * instances so that metrics can be collected.
 *
 */
public class PerformanceCounterCollector {

	Connection vmClient;

	/**
	 * Constructs a {@link PerformanceCounter} instance. It is assummed that the {@link Connection} that
	 * is passed is already been connected to the end point.
	 * 
	 * @param vmClient
	 */
	public PerformanceCounterCollector(Connection vmClient) {
		this.vmClient = vmClient;
	}

	/**
	 * Queries the end point to get all the known performance counters in vSphere
	 * 
	 * TODO: As written now the caller is responsible for trapping exceptions, not sure if this is the best
	 * approach at this point.
	 * 
	 * @return {@link PerformanceCounterMetadata} Container to store results
	 * @throws InvalidPropertyFaultMsg {@link InvalidPropertyFaultMsg}
	 * @throws RuntimeFaultFaultMsg {@link RuntimeFaultFaultMsg}
	 */
	public PerformanceCounterMetadata fetchPerformanceCounters() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
		PerformanceCounterMetadata metadata = new PerformanceCounterMetadata(new HashMap<String,Metric>());
		
		// Get the PerformanceManager object which is used
		// to get metrics from counters
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

		// Loop over our results to extract the performance counter information
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
