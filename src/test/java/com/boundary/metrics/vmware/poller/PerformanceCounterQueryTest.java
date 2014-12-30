package com.boundary.metrics.vmware.poller;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.boundary.metrics.vmware.VMWareTestUtils;
import com.boundary.metrics.vmware.client.client.meter.manager.MeterManagerClient;
import com.boundary.metrics.vmware.client.metrics.Measurement;
import com.boundary.metrics.vmware.client.metrics.Metric;
import com.vmware.connection.helpers.GetMOREF;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.connection.helpers.GetMOREF;

public class PerformanceCounterQueryTest {
	
	private final static String VMWARE_CLIENT_CONFIG_FILE=VMWareTestUtils.DEFAULT_VMWARE_CLIENT_CONFIGURATION;

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
		vmwareClient = VMWareTestUtils.getVMWareConnection(VMWARE_CLIENT_CONFIG_FILE);
		vmwareClient.connect();
		meterClient = VMWareTestUtils.getMeterClient();
		
	}

	@After
	public void tearDown() throws Exception {
		vmwareClient.disconnect();
		vmwareClient = null;
		meterClient = null;
	}
	
	@Test
	public void testGetVMByVMName() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
		GetMOREF search = new GetMOREF(vmwareClient);
		ManagedObjectReference mor = search.vmByVMname("RHEL-TestVM01",
				vmwareClient.getServiceContent().getPropertyCollector());
		
		assertNotNull("check ManagedObjectReference",mor);
		System.out.println(mor.getValue());
		System.out.println(mor.getType());
	}

	@Test
	public void testQuery() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
		Map<String,Metric> metrics = new HashMap<String,Metric>();
		String vmName = "RHEL-TestVM01";
		
		metrics.put("cpu.usage.AVERAGE",
        		new Metric("SYSTEM_CPU_USAGE_AVERAGE","CPU Average Utilization"));
		
		GetMOREF search = new GetMOREF(vmwareClient);
		
		ManagedObjectReference mor = search.vmByVMname(vmName,vmwareClient.getServiceContent().getPropertyCollector());
		System.out.println(mor.getValue());

		PerformanceCounterCollector counterCollector = new PerformanceCounterCollector(vmwareClient);
		PerformanceCounterMetadata perfCounterMetadata = counterCollector.fetchPerformanceCounters();
		VMWareMetadata metadata = new VMWareMetadata(perfCounterMetadata,metrics);
		PerformanceCounterQuery query = new PerformanceCounterQuery(vmwareClient,meterClient,metadata);
		
		List<Measurement> measurements = query.queryCounters(mor);
		System.out.println("measurements: " + measurements.size());
		
		for (Measurement m : measurements) {
			System.out.println(m);
		}
	}

}
