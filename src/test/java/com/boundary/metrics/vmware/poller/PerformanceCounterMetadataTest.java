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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.boundary.metrics.vmware.VMWareTestUtils;
import com.boundary.metrics.vmware.client.metrics.Metric;
import com.vmware.vim25.PerfCounterInfo;
import com.vmware.vim25.PerfMetricId;
import com.vmware.vim25.PerfStatsType;
import com.vmware.vim25.PerfSummaryType;

public class PerformanceCounterMetadataTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	private PerformanceCounterMetadata metadata;
	private PerfCounterInfo one;
	private PerfCounterInfo two;
	private PerfCounterInfo three;

	@Before
	public void setUp() throws Exception {
		metadata = new PerformanceCounterMetadata();
		one = VMWareTestUtils.buildPerfCounterInfo("cpu",100,new Integer(4),"usage",PerfSummaryType.AVERAGE,PerfStatsType.ABSOLUTE);
		two = VMWareTestUtils.buildPerfCounterInfo("mem",101,new Integer(4),"swapused",PerfSummaryType.MAXIMUM,PerfStatsType.ABSOLUTE);
		three = VMWareTestUtils.buildPerfCounterInfo("disk",102,new Integer(1),"write",PerfSummaryType.AVERAGE,PerfStatsType.RATE);

		metadata.put(one);
		metadata.put(two);
		metadata.put(three);

	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testInfoMap() {
		
	}

	@Test
	public void testPut() {
		
		Map<String,Integer> nameMap = metadata.getNameMap();
		Map<Integer,PerfCounterInfo> infoMap = metadata.getInfoMap();
		
		assertEquals("check one name",new Integer(100).intValue(),nameMap.get("cpu.usage.AVERAGE").intValue());
		assertEquals("check one info",one.toString(),infoMap.get(100).toString());
		
		assertEquals("check two name",new Integer(101).intValue(),nameMap.get("mem.swapused.MAXIMUM").intValue());
		assertEquals("check two info",two.toString(),infoMap.get(101).toString());
		
		assertEquals("check three name",new Integer(102).intValue(),nameMap.get("disk.write.AVERAGE").intValue());
		assertEquals("check three info",three.toString(),infoMap.get(102).toString());

	}
	
	@Test
	public void testGetPerformanceMetrics() {
		Map<String,Metric> metrics = new HashMap<String,Metric>();
		Metric metric1 = new Metric(one.getNameInfo().getKey(),"ONE");
		Metric metric2 = new Metric(two.getNameInfo().getKey(),"TWO");
		Metric metric3 = new Metric(three.getNameInfo().getKey(),"THREE");
		metrics.put(metric1.getName(),metric1);
		metrics.put(metric2.getName(),metric2);
		metrics.put(metric3.getName(),metric3);
		
		List<PerfMetricId> performanceMetrics = metadata.getPerformanceMetricIds(metrics);
		for (PerfMetricId perfMetric : performanceMetrics) {
			
			assertEquals("check instance","*",perfMetric.getInstance());
			switch (perfMetric.getCounterId()) {
			case 100:
				break;
			case 101:
				break;
			case 102:
				break;
			default:
				assertTrue("check default",false);
			}
			
		}
		
	}

}
