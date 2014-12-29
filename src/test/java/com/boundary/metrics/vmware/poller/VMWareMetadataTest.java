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

		metrics = new HashMap<String,Metric>();
		orgId = "";
	}

	@After
	public void tearDown() throws Exception {
	}
	
	

	@Test
	public void testSetPerformanceCounterMap() {
		PerformanceCounterMetadata metadata = new PerformanceCounterMetadata(metrics);
		PerfCounterInfo one = VMWareTestUtils.buildPerfCounterInfo("cpu",100,new Integer(4),"usage",PerfSummaryType.AVERAGE,PerfStatsType.ABSOLUTE);
		PerfCounterInfo two = VMWareTestUtils.buildPerfCounterInfo("mem",101,new Integer(4),"swapused",PerfSummaryType.MAXIMUM,PerfStatsType.ABSOLUTE);
		PerfCounterInfo three = VMWareTestUtils.buildPerfCounterInfo("disk",102,new Integer(1),"write",PerfSummaryType.AVERAGE,PerfStatsType.RATE);

		metadata.put(one);
		metadata.put(two);
		metadata.put(three);
		
		Map<String,Integer> m = metadata.getNameMap();
		assertEquals("Check one item",m.get("cpu.usage.AVERAGE"),new Integer(100));
		assertEquals("Check two item",m.get("mem.swapused.MAXIMUM"),new Integer(101));
		assertEquals("Check three item",m.get("disk.write.AVERAGE"),new Integer(102));
	}

	
	@Test
	public void testSetPerformanceCounterInfoMap() {
		PerformanceCounterMetadata metadata = new PerformanceCounterMetadata(metrics);
		
		PerfCounterInfo one = VMWareTestUtils.buildPerfCounterInfo("cpu",100,new Integer(4),"usage",PerfSummaryType.AVERAGE,PerfStatsType.ABSOLUTE);
		PerfCounterInfo two = VMWareTestUtils.buildPerfCounterInfo("mem",101,new Integer(4),"swapused",PerfSummaryType.MAXIMUM,PerfStatsType.ABSOLUTE);
		PerfCounterInfo three = VMWareTestUtils.buildPerfCounterInfo("disk",102,new Integer(1),"write",PerfSummaryType.AVERAGE,PerfStatsType.RATE);

		metadata.put(one);
		metadata.put(two);
		metadata.put(three);
		
		
		Map<Integer,PerfCounterInfo> infoMap = metadata.getInfoMap();
		assertNotNull("check for Null",metadata);
		assertEquals("check one",one,infoMap.get(100));
		assertEquals("check two",two,infoMap.get(101));
		assertEquals("check three",three,infoMap.get(102));
	}

	@Test
	public void testSetOrgId() {
		String myOrgId = "foobar";
		PerformanceCounterMetadata metadata = new PerformanceCounterMetadata(metrics);
		VMWareMetadata data = new VMWareMetadata(metadata,myOrgId);
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
		PerformanceCounterMetadata metadata = new PerformanceCounterMetadata(m);
		Map<String,Metric> newMetrics = metadata.getMetrics();
		assertEquals("check one",one,newMetrics.get("disk.read.AVERAGE"));
		assertEquals("check two",two,newMetrics.get("disk.write.AVERAGE"));
		assertEquals("check three",three,newMetrics.get("cpu.usage.MINIMUM"));
	}

}
