package com.boundary.metrics.vmware.poller;

import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.boundary.metrics.vmware.VMWareTestUtils;
import com.boundary.metrics.vmware.client.client.meter.manager.MeterManagerClient;

public class PerformanceCounterQueryTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	private MeterManagerClient meterClient;
	private VMwareClient vmwareClient;

	@Before
	public void setUp() throws Exception {
		vmwareClient = VMWareTestUtils.getVMWareConnection(this.getClass().toString());
		meterClient = VMWareTestUtils.getMeterClient(); 
	}

	@After
	public void tearDown() throws Exception {
	}

	@Ignore
	@Test
	public void testQuery() {
		PerformanceCounterQuery query = new PerformanceCounterQuery(vmwareClient, meterClient,null);
	}

}
