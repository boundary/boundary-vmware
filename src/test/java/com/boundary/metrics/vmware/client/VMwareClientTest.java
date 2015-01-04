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

package com.boundary.metrics.vmware.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.boundary.metrics.vmware.VMWareClientFactory;
import com.boundary.metrics.vmware.client.metrics.Measurement;
import com.boundary.metrics.vmware.client.metrics.Metric;
import com.boundary.metrics.vmware.poller.MetricDefinition;
import com.boundary.metrics.vmware.poller.MetricDefinitionBuilder;
import com.boundary.metrics.vmware.poller.PerformanceCounterCollector;
import com.boundary.metrics.vmware.poller.PerformanceCounterMetadata;
import com.boundary.metrics.vmware.poller.PerformanceCounterQuery;
import com.boundary.metrics.vmware.poller.VMWareMetadata;
import com.boundary.metrics.vmware.poller.VMwareClient;
import com.boundary.metrics.vmware.util.TimeUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import com.vmware.connection.Connection;
import com.vmware.connection.helpers.GetMOREF;
import com.vmware.vim25.ArrayOfPerfCounterInfo;
import com.vmware.vim25.DynamicProperty;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.ObjectContent;
import com.vmware.vim25.ObjectSpec;
import com.vmware.vim25.PerfCounterInfo;
import com.vmware.vim25.PerfEntityMetric;
import com.vmware.vim25.PerfEntityMetricBase;
import com.vmware.vim25.PerfMetricId;
import com.vmware.vim25.PerfMetricIntSeries;
import com.vmware.vim25.PerfMetricSeries;
import com.vmware.vim25.PerfSampleInfo;
import com.vmware.vim25.PropertyFilterSpec;
import com.vmware.vim25.PropertySpec;
import com.vmware.vim25.RetrieveOptions;
import com.vmware.vim25.RetrieveResult;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.ServiceContent;
import com.vmware.vim25.VimPortType;
import com.vmware.vim25.VimService;

public class VMwareClientTest {

	private VMwareClient vmClient;
	
	private final static String CLIENT_PROPERTY_FILE = "vmware-client.properties";
	private final static String MANAGED_OBJECTS_FILE = "managed-objects.json";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// If our configuration file is missing that do not run
		// tests. The configuration file has credentials so we
		// are not able to include in our repository.
		
		File propertiesFile = new File(Resources.getResource(CLIENT_PROPERTY_FILE).toURI());
		assumeTrue(propertiesFile.exists());
		
		File managedObjectsFile = new File(Resources.getResource(MANAGED_OBJECTS_FILE).toURI());
		assumeTrue(managedObjectsFile.exists());
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		vmClient = VMWareClientFactory.createClient(CLIENT_PROPERTY_FILE);
		vmClient.connect();
	}

	@After
	public void tearDown() throws Exception {
		vmClient.disconnect();
	}

	@Test
	public void testClientConnection() throws URISyntaxException, IOException {
		Connection client =  VMWareClientFactory.createClient(CLIENT_PROPERTY_FILE);

		client.connect();
		client.disconnect();
	}
	
	@Test
	public void testMultipleConnection() throws URISyntaxException, IOException {
		Connection client =  VMWareClientFactory.createClient(CLIENT_PROPERTY_FILE);
		client.connect();
		client.connect();
		client.disconnect();
	}
	
	@Test
	public void testMultipleDisconnect() throws URISyntaxException, IOException {
		Connection client =  VMWareClientFactory.createClient(CLIENT_PROPERTY_FILE);
		client.connect();
		client.disconnect();
		client.disconnect();
	}

	@Test
	public void testGetServiceContent() {
		ServiceContent content = vmClient.getServiceContent();
		assertNotNull(content);
	}

	@Test
	public void testGetPerformanceManager() {
		ServiceContent content = vmClient.getServiceContent();
		ManagedObjectReference performanceManager = content.getPerfManager();
		assertNotNull(performanceManager);
	}

	@Test
	public void testGetVimPort() {
		VimPortType vimPortType = vmClient.getVimPort();
		assertNotNull(vimPortType);
	}

	@Test
	public void testGetVimService() {
		VimService vimService = vmClient.getVimService();
		assertNotNull(vimService);
	}
	
	@Test
	public void testGetManagedObjects() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
		
		Map<String, ManagedObjectReference> managedObjects = vmClient.getManagedObjects("VirtualMachine");
		
		for (Map.Entry<String,ManagedObjectReference> mor : managedObjects.entrySet()) {
			ManagedObjectReference ref = mor.getValue();
			System.out.println(ref.getValue());
			assertEquals("Check mor type","VirtualMachine",ref.getType());
		}
	}
	
	@Test
	public void testGetVMByName() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
		ManagedObjectReference mor = vmClient.getVMByName("RHEL-TestVM01");
		assertNotNull("Check for null MOR",mor);
	}

	@Test
	public void testGetPerformanceManagerDescription() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
		ServiceContent content = vmClient.getServiceContent();
		ManagedObjectReference perfManager = content.getPerfManager();
		ObjectSpec oSpec = new ObjectSpec();
		oSpec.setObj(perfManager);

		PropertySpec pSpec = new PropertySpec();
		pSpec.setType("PerformanceManager");
		pSpec.getPathSet().add("perfCounter");

		PropertyFilterSpec propertyFilter = new PropertyFilterSpec();
		propertyFilter.getObjectSet().add(oSpec);
		propertyFilter.getPropSet().add(pSpec);
		
        RetrieveOptions retriveOptions = new RetrieveOptions();
        RetrieveResult retrieveResult = vmClient.getVimPort().retrievePropertiesEx(
        		vmClient.getServiceContent().getPropertyCollector(),
        		ImmutableList.of(propertyFilter), retriveOptions);

        for (ObjectContent oc : retrieveResult.getObjects()) {
            if (oc.getPropSet() != null) {
                for (DynamicProperty dp : oc.getPropSet()) {
                    List<PerfCounterInfo> perfCounters = ((ArrayOfPerfCounterInfo)dp.getVal()).getPerfCounterInfo();
                    if (perfCounters != null) {
                        for (PerfCounterInfo perfCounter : perfCounters) {
                        	System.out.println("id: " + Integer.toString(perfCounter.getKey()));
                        	System.out.println("groupInfo: " + perfCounter.getGroupInfo().getKey());
                        	System.out.println("nameInfo: " + perfCounter.getNameInfo().getKey());
                        	System.out.println("RollupType: " + perfCounter.getRollupType().toString().toUpperCase());
                        }
                    }
                }
            }
        }        

	}
	
	@Test
	public void testGetStats() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
		Map<String,MetricDefinition> metrics = new HashMap<String,MetricDefinition>();
		MetricDefinitionBuilder m = new MetricDefinitionBuilder();
		m.setMetric("SYSTEM_CPU_USAGE_AVERAGE")
		 .setDisplayName("CPU Average Utilization");
		metrics.put("cpu.usage.AVERAGE",m.build());
		String vmName = "RHEL-TestVM01";
		ManagedObjectReference mor = vmClient.getVMByName(vmName);
		DateTime end = vmClient.getTimeAtEndPoint();
		DateTime start = end.minusSeconds(20);
		
		PerformanceCounterCollector counterCollector = new PerformanceCounterCollector(vmClient);
		PerformanceCounterMetadata perfCounterMetadata = counterCollector.fetchPerformanceCounters();
		List<PerfMetricId> perfMetricIds = perfCounterMetadata.getPerformanceMetricIds(metrics);
		List<PerfEntityMetricBase> entities = vmClient.getStats(mor,new Integer(20),start,end,perfMetricIds);
		assertNotNull("Check entities",entities);
		assertTrue("Check entities size", entities.size() > 0);
		
		for (PerfEntityMetricBase p :entities) {
			if (p instanceof PerfEntityMetric) {
				PerfEntityMetric entity = (PerfEntityMetric)p;
				List<PerfSampleInfo> info = entity.getSampleInfo();
				List<PerfMetricSeries> metricValues = entity.getValue();
				
				for (PerfSampleInfo i : info) {
					System.out.println(TimeUtils.toDateTime(i.getTimestamp()));
				}

				for (int x = 0; x < metricValues.size(); x++) {
					PerfMetricIntSeries metricReading = (PerfMetricIntSeries) metricValues.get(x);
					System.out.println(metricReading.getValue().size());
					for (Long v : metricReading.getValue()) {
						System.out.println(v);
					}
				}
			}
			else {
				fail("Not instance of PerfEntityMetric");
			}
		}
	}
	
	@Test
	public void testGetMeasurements() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
		Map<String,MetricDefinition> metrics = new HashMap<String,MetricDefinition>();
		MetricDefinitionBuilder m = new MetricDefinitionBuilder();
		m.setMetric("SYSTEM_CPU_USAGE_AVERAGE")
		 .setDisplayName("CPU Average Utilization");
		metrics.put("cpu.usage.AVERAGE",m.build());
		Map<String,Map<String,MetricDefinition>> lMetrics = new HashMap<String,Map<String,MetricDefinition>>();
		lMetrics.put("VirtualMachine", metrics);
		
		String vmName = "RHEL-TestVM01";
		GetMOREF search = new GetMOREF(vmClient);
		ManagedObjectReference mor = search.vmByVMname(vmName,vmClient.getPropertyCollector());
		DateTime end = vmClient.getTimeAtEndPoint();
		DateTime start = end.minusSeconds(20);
		
		PerformanceCounterCollector counterCollector = new PerformanceCounterCollector(vmClient);
		PerformanceCounterMetadata perfCounterMetadata = counterCollector.fetchPerformanceCounters();
		VMWareMetadata metadata = new VMWareMetadata(perfCounterMetadata,lMetrics);
		
		System.out.println(mor.getValue());
		List<Measurement> measurements = vmClient.getMeasurements(mor,mor.getValue(),1,new Integer(20),start,end,metadata);
		assertNotNull("Check entities",measurements);
		assertTrue("Check entities size", measurements.size() > 0);
		
		for (Measurement measure : measurements) {
			System.out.println("MEASURE: " + measure);
		}
	}
	
	@Test
	public void testGetMultipleMeasurements() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
		Map<String,MetricDefinition> metrics = new HashMap<String,MetricDefinition>();
		MetricDefinitionBuilder m = new MetricDefinitionBuilder();
		m.setMetric("SYSTEM_CPU_USAGE_AVERAGE")
		 .setDisplayName("CPU Average Utilization");
		metrics.put("cpu.usage.AVERAGE",m.build());
		Map<String,Map<String,MetricDefinition>> lMetrics = new HashMap<String,Map<String,MetricDefinition>>();
		lMetrics.put("VirtualMachine", metrics);
		
		PerformanceCounterCollector counterCollector = new PerformanceCounterCollector(vmClient);
		PerformanceCounterMetadata perfCounterMetadata = counterCollector.fetchPerformanceCounters();
		VMWareMetadata metadata = new VMWareMetadata(perfCounterMetadata,lMetrics);
		
		Map<String, ManagedObjectReference> mors = vmClient.getManagedObjects("VirtualMachine");
		
		for (Map.Entry<String, ManagedObjectReference> entity : mors.entrySet()) {
			ManagedObjectReference mor = entity.getValue();
			DateTime end = vmClient.getTimeAtEndPoint();
			DateTime start = end.minusSeconds(20);

			List<Measurement> measurements = vmClient.getMeasurements(mor,mor.getValue(), 1, new Integer(20), start, end, metadata);
			assertNotNull("Check measurements", measurements);
			//assertTrue("Check measurements size", measurements.size() > 0);

			for (Measurement measure : measurements) {
				System.out.println(measure);
			}
		}
	}
}
