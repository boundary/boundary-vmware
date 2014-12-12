package com.boundary.metrics.vmware.client;

import static org.junit.Assert.*;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Environment;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.boundary.metrics.vmware.VMWareTestUtils;
import com.boundary.metrics.vmware.VMwarePerfAdapterConfiguration;
import com.boundary.metrics.vmware.client.client.meter.manager.MeterManagerClient;
import com.boundary.metrics.vmware.client.client.meter.manager.MeterMetadata;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.io.Resources;
import com.sun.jersey.api.client.Client;

public class MeterManagerClientTest {

	private static Properties clientProperties;
	
	private final static String BASE_URL = "com.boundary.metrics.meter.client.baseuri";
	private final static String API_KEY = "com.boundary.metrics.meter.client.apikey";

	private static String baseUri;
	private static String apiKey;
	private static Environment environment;
	private static VMwarePerfAdapterConfiguration configuration;

	private static MeterManagerClient client;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		MetricRegistry registry = new MetricRegistry();
		environment = new Environment("test", mapper, null, registry, ClassLoader.getSystemClassLoader());
		configuration = VMWareTestUtils.getConfiguration("vmware-adapter-test.yml");
		
		File propertiesFile = new File(Resources.getResource("meter-manager-client.properties").toURI());
		Reader reader = new FileReader(propertiesFile);
		clientProperties = new Properties();
		clientProperties.load(reader);
		System.out.println("clientProperties: " + clientProperties);
		baseUri = clientProperties.getProperty(BASE_URL);
		apiKey = clientProperties.getProperty(API_KEY);
		
		Client httpClient = new JerseyClientBuilder(environment)
        .using(configuration.getClient())
        .build("http-client");
		try {
			System.out.println(baseUri);
			URI uri = new URI(baseUri);
			client = new MeterManagerClient(httpClient,uri,apiKey);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	public void testCreateMeter() {
		String meterName = "my-test-meter";
		client.createOrGetMeterMetadata(configuration.getOrgId(),meterName);
		Optional<MeterMetadata> result = client.getMeterMetadataByName(configuration.getOrgId(), meterName);
		MeterMetadata meter = result.get();
		assertNotNull("Check for Not Null meter: getMeterMetadataByName()",meter);
		assertEquals("Check getName()",meterName,meter.getName());
		//assertEquals("Check getOrgId()",configuration.getOrgId(),meter.getOrgId());
		String meterId = meter.getId();
		System.out.println(meterId);
		
		result = client.getMeterMetadataById(configuration.getOrgId(),meterId);
		assertNotNull("Check for Not Null meter: getMeterMetadataById()",meter);
		assertEquals("Check getName()",meterName,meter.getName());
		//assertEquals("Check getOrgId()",configuration.getOrgId(),meter.getOrgId());
	}
}
