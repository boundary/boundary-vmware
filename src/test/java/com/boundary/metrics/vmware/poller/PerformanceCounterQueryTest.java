package com.boundary.metrics.vmware.poller;

import static com.boundary.metrics.vmware.VMWareClientFactory.DEFAULT_PROPERTY_FILE;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
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
		File file = new File("src/test/resources/" + DEFAULT_PROPERTY_FILE);
		assumeTrue(file.exists());
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	private MeterManagerClient meterClient;
	private VMwareClient vmClient;

	@Before
	public void setUp() throws Exception {
		vmClient = VMWareTestUtils.getVMWareConnection(VMWARE_CLIENT_CONFIG_FILE);
		vmClient.connect();
		meterClient = VMWareTestUtils.getMeterClient();
		
	}

	@After
	public void tearDown() throws Exception {
		vmClient.disconnect();
		vmClient = null;
		meterClient = null;
	}
	
	@Test
	public void testGetVMByVMName() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
		GetMOREF search = new GetMOREF(vmClient);
		ManagedObjectReference mor = search.vmByVMname("RHEL-TestVM01",
				vmClient.getServiceContent().getPropertyCollector());
		
		assertNotNull("check ManagedObjectReference",mor);
		System.out.println(mor.getValue());
		assertEquals("check type","VirtualMachine",mor.getType());
	}
	
	

	@Test
	public void testQuery() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
		// Lookup a virtual machine that we want to collect measurements from
		Map<String, ManagedObjectReference> vms = vmClient.getManagedObjects("VirtualMachine");
		ManagedObjectReference mor = null;
		
		for (String key : vms.keySet()) {
			mor = vms.get(key);
			break;
		}
		
		assertNotNull("check MOR",mor);
		
		// Define performance counters we want to retrieve
		Map<String,MetricDefinition> defs = new HashMap<String,MetricDefinition>();
		MetricDefinitionBuilder builder = new MetricDefinitionBuilder();
		builder.setMetric("SYSTEM_CPU_USAGE_AVERAGE").setDisplayName("CPU Average Utilization");
		defs.put("cpu.usage.AVERAGE",builder.build());
		Map<String, Map<String, MetricDefinition>> metrics = new HashMap<String,Map<String,MetricDefinition>>();
		metrics.put("VirtualMachine",defs);
		
		PerformanceCounterCollector counterCollector = new PerformanceCounterCollector(vmClient);
		PerformanceCounterMetadata perfCounterMetadata = counterCollector.fetchPerformanceCounters();
		VMWareMetadata metadata = new VMWareMetadata(perfCounterMetadata,metrics);
		PerformanceCounterQuery query = new PerformanceCounterQuery(vmClient,meterClient,metadata);
		
        DateTime end = vmClient.getTimeAtEndPoint();
        DateTime start = end.minusSeconds(20);
        
		String vmName = "RHEL-TestVM01";
		GetMOREF search = new GetMOREF(vmClient);
		mor = search.vmByVMname(vmName,vmClient.getPropertyCollector());
        
		List<Measurement> measurements = query.queryCounters(mor,start,end);
		assertNotNull("Check measurements",measurements);
		System.out.println("measurements: " + measurements.size());
		assertTrue("Check size of measurements",measurements.size() > 0);
		
		for (Measurement m : measurements) {
			System.out.println(m);
		}
	}
}
