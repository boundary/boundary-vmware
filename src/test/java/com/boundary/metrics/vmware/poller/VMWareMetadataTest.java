package com.boundary.metrics.vmware.poller;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.boundary.metrics.vmware.VMWareTestUtils;
import com.boundary.metrics.vmware.client.metrics.Metric;
import com.vmware.vim25.ElementDescription;
import com.vmware.vim25.PerfCounterInfo;
import com.vmware.vim25.PerfStatsType;
import com.vmware.vim25.PerfSummaryType;

public class VMWareMetadataTest {
	
	private Map<String,Integer> performanceCounterMap;
	private Map<Integer,PerfCounterInfo> performanceCounterInfoMap;
	private Map<String,Metric> metrics;
	private String orgId;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		performanceCounterMap = new HashMap<String,Integer>();
		performanceCounterInfoMap = new HashMap<Integer,PerfCounterInfo>();
		metrics = new HashMap<String,Metric>();
		orgId = "";
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSetPerformanceCounterMap() {
		Map<String,Integer> map = new HashMap<String,Integer>();
		map.put("RED", 1);
		map.put("GREEN", 2);
		map.put("BLUE", 3);
		
		VMWareMetadata data = new VMWareMetadata(map,performanceCounterInfoMap,metrics,orgId);
		
		Map<String,Integer> m = data.getPerformanceCounterMap();
		assertEquals("Check RED item",m.get("RED"),new Integer(1));
		assertEquals("Check GREEN item",m.get("GREEN"),new Integer(2));
		assertEquals("Check BLUE item",m.get("BLUE"),new Integer(3));
	}

	
	@Test
	public void testSetPerformanceCounterInfoMap() {
		PerfCounterInfo one = VMWareTestUtils.buildPerfCounterInfo("cpu",100,new Integer(4),"usage",PerfSummaryType.AVERAGE,PerfStatsType.ABSOLUTE);
		PerfCounterInfo two = VMWareTestUtils.buildPerfCounterInfo("mem",101,new Integer(4),"swapused",PerfSummaryType.MAXIMUM,PerfStatsType.ABSOLUTE);
		PerfCounterInfo three = VMWareTestUtils.buildPerfCounterInfo("disk",102,new Integer(1),"write",PerfSummaryType.AVERAGE,PerfStatsType.RATE);
		Map<Integer,PerfCounterInfo> infoMap = new HashMap<Integer,PerfCounterInfo>();
		infoMap.put(one.getKey(),one);
		infoMap.put(two.getKey(),two);
		infoMap.put(three.getKey(),three);
		
		VMWareMetadata data = new VMWareMetadata(performanceCounterMap,infoMap,metrics,orgId);
		Map<Integer,PerfCounterInfo> newInfoMap = data.getPerformanceCounterInfoMap();
		assertNotNull("check for Null",newInfoMap);
		assertEquals("check one",one,newInfoMap.get(100));
		assertEquals("check two",two,newInfoMap.get(101));
		assertEquals("check three",three,newInfoMap.get(102));
	}

	@Test
	public void testSetOrgId() {
		String myOrgId = "foobar";
		VMWareMetadata data = new VMWareMetadata(performanceCounterMap,performanceCounterInfoMap,metrics,myOrgId);
		assertEquals("check orgId",myOrgId,data.getOrgId());
	}

	@Test
	public void testGetMetrics() {
		Map<String,Metric> m = new HashMap<String,Metric>();
		Metric one = new Metric("VMWARE_DISK_READ_AVERAGE","Disk Read Average");
		Metric two = new Metric("VMWARE_DISK_WRITE_AVERAGE","Disk Write Average");
		Metric three = new Metric("VMWARE_CPU_USAGE_MINIMUM","CPU Usage Minimum");
		m.put("disk.read.AVERAGE",one);
		m.put("disk.write.AVERAGE",two);
		m.put("cpu.usage.MINIMUM",three);
		VMWareMetadata data = new VMWareMetadata(performanceCounterMap,performanceCounterInfoMap,m,orgId);
		Map<String,Metric> newMetrics = data.getMetrics();
		assertEquals("check one",one,newMetrics.get("disk.read.AVERAGE"));
		assertEquals("check two",two,newMetrics.get("disk.write.AVERAGE"));
		assertEquals("check three",three,newMetrics.get("cpu.usage.MINIMUM"));
	}

}
