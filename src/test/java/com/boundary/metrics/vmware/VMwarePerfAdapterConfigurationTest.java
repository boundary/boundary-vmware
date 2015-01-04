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

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.jackson.Jackson;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.validation.Validation;
import javax.validation.Validator;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.boundary.metrics.vmware.VMwarePerfAdapterConfiguration.MeterManagerConfiguration;
import com.boundary.metrics.vmware.VMwarePerfAdapterConfiguration.MetricClientConfiguration;
import com.boundary.metrics.vmware.poller.MonitoredEntity;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.common.base.Joiner;
import com.google.common.io.Resources;

/**
 * Test cases for the VMware Configuration
 */
public class VMwarePerfAdapterConfigurationTest {

	private static VMwarePerfAdapterConfiguration configuration;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// If our configuration file is missing that do not run
		// tests. The configuration file has credentials so we
		// are not able to include in our repository.
		String configFile = "vm-adapter-configuration.yml";
		Joiner join = Joiner.on("//");
		String path = join.join("src/test/resources",configFile);
		assumeTrue(new File(path).exists());
		
		File validFile = new File(Resources.getResource(configFile).toURI());
		Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
		ConfigurationFactory<VMwarePerfAdapterConfiguration> factory =
	            new ConfigurationFactory<VMwarePerfAdapterConfiguration>(VMwarePerfAdapterConfiguration.class, validator, Jackson.newObjectMapper(), "dw");

		try {
			configuration = factory.build(validFile);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
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
	public void testGetMonitoredEntities() {
		List<MonitoredEntity> entities = configuration.getMonitoredEntities();
		assertNotNull("Check for Null: getMonitoredEntities()",entities);
		assertEquals("Check size: getMonitoriedEntities()",1,entities.size());
		MonitoredEntity entity = entities.get(0);
		assertEquals("check getName()","foobar",entity.getName());
		assertEquals("check getCatalog()","catalog.json",entity.getCatalog());
	}
	
	

	@Test
	public void testGetClient() {
		MetricClientConfiguration client = configuration.getMetricsClient();
		assertNotNull("Check for not null: getMetricClient()",client);
		assertEquals("Check getApiKey()",
				"gvadl8iv3apedefswh1fczuy4ca@metrics.somewhere.com:api.16732a19324-11",client.getApiKey());
		assertEquals("Check getBaseUri()","https://metrics-api.somewhere.com/",client.getBaseUri().toString());
	}
	
	

	@Test
	public void testGetMeterManagerClient() {
		MeterManagerConfiguration client = configuration.getMeterManagerClient();
		assertNotNull("Check for not null: getMeterManagerClient()",client);
		assertEquals("Check getApiKey()","6OThKB6oaF4TkCXPFIWxoHuLzX1",client.getApiKey());
		assertEquals("Check getBaseUri()","https://api.somewhere.com/",client.getBaseUri().toString());
	}

	@Test
	public void testGetMetricsClient() {
		JerseyClientConfiguration client = configuration.getClient();
		assertNotNull("Check for not null: getClient()",client);
		assertEquals("Check getMinThreads()",4,client.getMinThreads());
		assertEquals("Check getMaxThreads()",4,client.getMaxThreads());
		assertEquals("Check getTimeout()","10 seconds",client.getTimeout().toString());
		assertEquals("Check getConnectionTimeout()","10 seconds",client.getConnectionTimeout().toString());
		assertFalse("Check isGzipEnabledForRequests()",client.isGzipEnabledForRequests());
	}

	@Test
	public void testGetOrgId() {
		assertEquals("Check getOrgId()","GvADl8Iv3ApEDeTswh1fCzUy4cA",configuration.getOrgId());
	}
}
