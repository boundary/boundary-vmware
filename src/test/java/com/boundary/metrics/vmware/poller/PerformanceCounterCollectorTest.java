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

import static com.boundary.metrics.vmware.VMWareClientFactory.DEFAULT_PROPERTY_FILE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.boundary.metrics.vmware.VMWareClientFactory;
import com.google.common.io.Resources;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.PerfCounterInfo;
import com.vmware.vim25.PerfMetricId;
import com.vmware.vim25.RuntimeFaultFaultMsg;

public class PerformanceCounterCollectorTest {
	
	private final static String PERFORMANCE_COUNTER_PROPERTIES="vmware-performance-counters.properties";
	private Properties performanceCounters = null;

	@BeforeClass
	public static void setUpBeforeClass() {
		File file = new File("src/test/resources/" + DEFAULT_PROPERTY_FILE);
		assumeTrue(file.exists());
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {

		File propertiesFile = new File(Resources.getResource(PERFORMANCE_COUNTER_PROPERTIES).toURI());
		Reader reader = new FileReader(propertiesFile);
		performanceCounters = new Properties();
		performanceCounters.load(reader);
	}

	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Tests collecting of Performance Counters against the vSphere endpoint. Compare the returned values against our
	 * known list. Test will fail if we can not find the performance counter in our property list.
	 * 
	 * @throws URISyntaxException {@link URISyntaxException}
	 * @throws IOException {@link IOException}
	 * @throws InvalidPropertyFaultMsg {@link InvalidPropertyFaultMsg}
	 * @throws RuntimeFaultFaultMsg {@link RuntimeFaultFaultMsg}
	 */
	@Test
	public void testPerformanceCounterCollector() throws URISyntaxException, IOException, InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
		VMwareClient client = VMWareClientFactory.createClient();
		client.connect();

		
		Map<String,MetricDefinition> metrics = new HashMap<String,MetricDefinition>();
		MetricDefinitionBuilder builder = new MetricDefinitionBuilder();
		
		builder.setMetric("VMWARE_DISK_READ_AVERAGE").setDisplayName("Disk Read Average");
		metrics.put("disk.read.AVERAGE",builder.build());
		builder.setMetric("VMWARE_DISK_WRITE_AVERAGE").setDisplayName("Disk Write Average");
		metrics.put("disk.write.AVERAGE",builder.build());
		builder.setMetric("VMWARE_CPU_USAGE_MINIMUM").setDisplayName("CPU Usage Minimum");
		metrics.put("cpu.usage.MINIMUM",builder.build());

		PerformanceCounterCollector collector = new PerformanceCounterCollector(client);
		PerformanceCounterMetadata metadata = collector.fetchPerformanceCounters();
		
		Map<String,Integer> nameMap = metadata.getNameMap();
		Map<Integer,PerfCounterInfo> infoMap = metadata.getInfoMap();
		
		for (PerfCounterInfo info : infoMap.values()) {
			String key = String.format("%s.%s.%s",info.getGroupInfo().getKey(),info.getNameInfo().getKey(),info.getRollupType().toString());
			boolean counter = Boolean.valueOf(performanceCounters.getProperty(key)).booleanValue();
			assertEquals("check for performance counter info: " + key,true,counter);
		}
		
		for (String name : nameMap.keySet()) {
			boolean counter = Boolean.valueOf(performanceCounters.getProperty(name)).booleanValue();
			assertEquals("check for performance counter name: " + name,true,counter);
		}
		
		List<PerfMetricId> perfMetricIds = metadata.getPerformanceMetricIds(metrics);
		System.out.printf("%s: %d\n","perfMetricIds size: ",perfMetricIds.size());
		for(PerfMetricId id: perfMetricIds) {
			assertEquals("Check class",com.vmware.vim25.PerfMetricId.class,id.getClass());
			assertNull("Check dynamic type",id.getDynamicType());
			assertEquals("Check instance","*",id.getInstance());
		}
	}
	

}
