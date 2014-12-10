package com.boundary.metrics.vmware;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.boundary.metrics.vmware.poller.VMwareClient;
import com.vmware.connection.Connection;

public class VMwareClientTest {
	
	
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
		clientProperties.load(VMwareClientTest.class.getResourceAsStream("vmware-client.properties"));
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
		Connection client = new VMwareClient(new URI(url),user,password);
		
		client.connect();
	}

}
