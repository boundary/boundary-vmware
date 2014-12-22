package com.boundary.metrics.vmware.client;

import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import com.boundary.metrics.vmware.poller.VMwareClient;
import com.google.common.base.Joiner;
import com.google.common.io.Resources;
import com.vmware.connection.Connection;

public class VMWareTestClient {
	
	public static Connection createClient(String propertyFile) throws URISyntaxException, IOException {
		final String URL = "com.boundary.metrics.vmware.client.url";
		final String USER = "com.boundary.metrics.vmware.client.user";
		final String PASSWORD = "com.boundary.metrics.vmware.client.password";

		File propertiesFile = new File(Resources.getResource(propertyFile).toURI());
		assumeTrue(propertiesFile.exists());
		Reader reader = new FileReader(propertiesFile);
		Properties clientProperties = new Properties();
		clientProperties.load(reader);
		String url = clientProperties.getProperty(URL);
		String user = clientProperties.getProperty(USER);
		String password = clientProperties.getProperty(PASSWORD);
		
		return new VMwareClient(new URI(url), user, password,VMWareTestClient.class.toString());
	}
}
