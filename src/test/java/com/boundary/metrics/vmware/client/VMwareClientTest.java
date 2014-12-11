package com.boundary.metrics.vmware;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.boundary.metrics.vmware.poller.VMwareClient;
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
import com.vmware.vim25.ServiceContent;
import com.vmware.vim25.VimPortType;
import com.vmware.vim25.VimService;

public class VMwareClientTest {

	private Connection vmClient;

	private static Properties clientProperties;

	private final static String URL = "com.boundary.metrics.vmware.client.url";
	private final static String USER = "com.boundary.metrics.vmware.client.user";
	private final static String PASSWORD = "com.boundary.metrics.vmware.client.password";

	private static String url;
	private static String user;
	private static String password;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		clientProperties = new Properties();
		clientProperties.load(VMwareClientTest.class
				.getResourceAsStream("vmware-client.properties"));
		url = clientProperties.getProperty(URL);
		user = clientProperties.getProperty(USER);
		password = clientProperties.getProperty(PASSWORD);

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		clientProperties = null;
	}

	@Before
	public void setUp() throws Exception {
		vmClient = new VMwareClient(new URI(url), user, password);
		vmClient.connect();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testPropertiesLoad() {
		assertNotNull(clientProperties);
	}

	@Test
	public void testClientConnection() throws URISyntaxException {
		Connection client = new VMwareClient(new URI(url), user, password);

		client.connect();
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

		System.out.println(performanceManager.getClass().toString());
	}

	@Test
	public void testGetVimPort() {
		ServiceContent content = vmClient.getServiceContent();
		VimPortType vimPortType = vmClient.getVimPort();
		assertNotNull(vimPortType);
	}

	@Test
	public void testGetVimService() {
		ServiceContent content = vmClient.getServiceContent();
		VimService vimService = vmClient.getVimService();
		assertNotNull(vimService);
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

		PropertyFilterSpec fSpec = new PropertyFilterSpec();
		fSpec.getObjectSet().add(oSpec);
		fSpec.getPropSet().add(pSpec);
		
        RetrieveOptions ro = new RetrieveOptions();
        RetrieveResult retrieveResult = vmClient.getVimPort().retrievePropertiesEx(vmClient.getServiceContent().getPropertyCollector(), ImmutableList.of(fSpec), ro);

        for (ObjectContent oc : retrieveResult.getObjects()) {
            if (oc.getPropSet() != null) {
                for (DynamicProperty dp : oc.getPropSet()) {
                    List<PerfCounterInfo> perfCounters = ((ArrayOfPerfCounterInfo)dp.getVal()).getPerfCounterInfo();
                    if (perfCounters != null) {
                        for (PerfCounterInfo perfCounter : perfCounters) {
                        	System.out.println("id: " + Integer.toString(perfCounter.getKey()));
                        	System.out.println("groupInfo: " + perfCounter.getGroupInfo().getKey());
                        	System.out.println("nameInfo: " + perfCounter.getNameInfo().getKey());
                        	System.out.println("nameInfo: " + perfCounter.getRollupType().toString().toUpperCase());
                        }
                    }
                }
            }
        }

	}
}

