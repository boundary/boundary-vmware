package com.boundary.metrics.vmware.poller;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.boundary.metrics.vmware.client.VMWareClientFactory;
import com.vmware.connection.Connection;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.PerfCounterInfo;
import com.vmware.vim25.RuntimeFaultFaultMsg;

public class PerformanceCounterCollectorTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testPerformanceCounterCollector() throws URISyntaxException, IOException, InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
		Connection client = VMWareClientFactory.createClient();
		client.connect();
		PerformanceCounterCollector collector = new PerformanceCounterCollector(client);
		
		PerformanceCounterMetadata metadata = collector.fetchPerformanceCounters();
		
		Map<String,Integer> nameMap = metadata.getNameMap();
		Map<Integer,PerfCounterInfo> infoMap = metadata.getInfoMap();
		
		for (PerfCounterInfo info : infoMap.values()) {
			System.out.printf("%s.%s.%s\n",info.getGroupInfo().getKey(),info.getNameInfo().getKey(),info.getRollupType().toString());
		}
	}
}
