// Copyright 2014 Boundary, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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

import static org.junit.Assume.*;

import com.boundary.metrics.vmware.poller.VMwareClient;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.common.base.Joiner;
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
		File configFile = new File(Resources.getResource(resource).toURI());

		Validator validator = Validation.buildDefaultValidatorFactory()
				.getValidator();
		ConfigurationFactory<VMwarePerfAdapterConfiguration> factory = new ConfigurationFactory<VMwarePerfAdapterConfiguration>(
				VMwarePerfAdapterConfiguration.class, validator,
				Jackson.newObjectMapper(), "dw");

		try {
			configuration = factory.build(configFile);
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
		// If our configuration file is missing that do not run
		// tests. The configuration file has credentials so we
		// are not able to include in our repository.
		Joiner join = Joiner.on("//");
		String path = join.join("src/test/resources",resource);
		assumeTrue(new File(path).exists());
		
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
