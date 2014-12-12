package com.boundary.metrics.vmware;

import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.jackson.Jackson;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import javax.validation.Validation;
import javax.validation.Validator;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import com.boundary.metrics.vmware.poller.VMwareClient;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.common.io.Resources;
import com.vmware.connection.Connection;

public class VMWareTestUtils {
	
	private final static String URL = "com.boundary.metrics.vmware.client.url";
	private final static String USER = "com.boundary.metrics.vmware.client.user";
	private final static String PASSWORD = "com.boundary.metrics.vmware.client.password";
	
	public final static String DEFAULT_VMWARE_CLIENT_CONFIGURATION = "vmware-client.properties";

	private static Properties clientProperties;

	public static VMwarePerfAdapterConfiguration getConfiguration(String resource) throws Exception {

		VMwarePerfAdapterConfiguration configuration = null;
		File validFile = new File(Resources.getResource(resource).toURI());
		System.out.println(resource);

		Validator validator = Validation.buildDefaultValidatorFactory()
				.getValidator();
		ConfigurationFactory<VMwarePerfAdapterConfiguration> factory = new ConfigurationFactory<VMwarePerfAdapterConfiguration>(
				VMwarePerfAdapterConfiguration.class, validator,
				Jackson.newObjectMapper(), "dw");

		try {
			configuration = factory.build(validFile);
		} catch (JsonParseException e) {

			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return configuration;
	}
	
	/**
	 * Help function that creates a VMware Connection from the provide resource (from class path)
	 * 
	 * @param resource Properties file in the classpath
	 * @return {@link Connection} VMware connection instance
	 */
	public static Connection getVMWareConnection(String resource) {
		VMwareClient client = null;
		try {
			File propertiesFile = new File(Resources.getResource(resource).toURI());
			Reader reader = new FileReader(propertiesFile);
			clientProperties = new Properties();
			clientProperties.load(reader);
			final String url = clientProperties.getProperty(URL);
			final String user = clientProperties.getProperty(USER);
			final String password = clientProperties.getProperty(PASSWORD);

			client = new VMwareClient(new URI(url), user, password,VMWareTestUtils.class.toString());

		} catch (Exception e) {

			e.printStackTrace();
		}

		return client;
	}
}
