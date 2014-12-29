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

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.setup.Environment;

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

import static com.google.common.base.Preconditions.checkArgument;
import static org.junit.Assume.*;

import com.boundary.metrics.vmware.client.client.meter.manager.MeterManagerClient;
import com.boundary.metrics.vmware.poller.VMwareClient;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.io.Resources;
import com.sun.jersey.api.client.Client;
import com.vmware.connection.Connection;
import com.vmware.vim25.ElementDescription;
import com.vmware.vim25.PerfCounterInfo;
import com.vmware.vim25.PerfStatsType;
import com.vmware.vim25.PerfSummaryType;

public class VMWareTestUtils {
	
	private final static String URL = "com.boundary.metrics.vmware.client.url";
	private final static String USER = "com.boundary.metrics.vmware.client.user";
	private final static String PASSWORD = "com.boundary.metrics.vmware.client.password";
	
	public final static String DEFAULT_VMWARE_CLIENT_CONFIGURATION = "vmware-client.properties";
	public final static String DEFAULT_METER_CLIENT_CONFIGURATION = "meter-manager-client.properties";
	
	private final static String BASE_URL = "com.boundary.metrics.meter.client.baseuri";
	private final static String ORG_ID = "com.boundary.metrics.meter.client.orgid";
	private final static String API_KEY = "com.boundary.metrics.meter.client.apikey";


	private static Properties clientProperties;
	private static Environment environment;
	private static VMwarePerfAdapterConfiguration configuration;

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
	 * @return {@link VMwareClient} VMware connection instance
	 */
	public static VMwareClient getVMWareConnection(String resource) {
		// If our configuration file is missing then do not run
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
	
	public static MeterManagerClient getMeterClient() throws Exception {
		MeterManagerClient client = null;
		
		

		File propertiesFile = new File(Resources.getResource(DEFAULT_METER_CLIENT_CONFIGURATION).toURI());
		Reader reader = new FileReader(propertiesFile);
		clientProperties = new Properties();
		clientProperties.load(reader);
		System.out.println("clientProperties: " + clientProperties);
		String baseUri = clientProperties.getProperty(BASE_URL);
		String orgId = clientProperties.getProperty(ORG_ID);
		String apiKey = clientProperties.getProperty(API_KEY);
		
		checkArgument(!Strings.isNullOrEmpty(baseUri));
        checkArgument(!Strings.isNullOrEmpty(apiKey));
        checkArgument(!Strings.isNullOrEmpty(orgId));
        
		ObjectMapper mapper = new ObjectMapper();
		MetricRegistry registry = new MetricRegistry();
		environment = new Environment("test", mapper, null, registry, ClassLoader.getSystemClassLoader());
		String configFile = "vmware-adapter-test.yml";
		configuration = VMWareTestUtils.getConfiguration(configFile);
		Client httpClient = new JerseyClientBuilder(environment)
        .using(configuration.getClient())
        .build("http-client");
		try {
			System.out.println(baseUri);
			URI uri = new URI(baseUri);
			client = new MeterManagerClient(httpClient,uri,orgId,apiKey);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return client;
	}
	
	/**
	 * Helper method to generate {@link PerfCounterInfo} instances
	 * 
	 * @param group group name of the performance counter
	 * @param key unique key of the performance counter
	 * @param level level of the performance counter
	 * @param name name of the performance counter
	 * @param rollupType counter's rollup type (NONE, AVERAGE, etc)
	 * @param statsType counter type (ABSOLUTE, RATE,etc)
	 * @return {@link PerfCounterInfo}
	 */
	static public PerfCounterInfo buildPerfCounterInfo(String group,int key,Integer level,String name,PerfSummaryType rollupType,PerfStatsType statsType) {
		PerfCounterInfo info = new PerfCounterInfo();
		ElementDescription edGroup = new ElementDescription();
		edGroup.setKey(group);
		info.setGroupInfo(edGroup);
		info.setKey(key);
		info.setLevel(level);
		ElementDescription edName = new ElementDescription();
		edName.setKey(name);
		info.setNameInfo(edName);
		info.setRollupType(rollupType);
		info.setStatsType(statsType);
		return info; 
	}
}
