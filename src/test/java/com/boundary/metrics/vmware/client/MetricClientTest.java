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

package com.boundary.metrics.vmware.client;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Random;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.boundary.metrics.vmware.client.metrics.MetricClient;
import com.google.common.io.Resources;
import com.sun.jersey.api.client.Client;

public class MetricClientTest {

	private static final String CLIENT_PROPERTY_FILE = "metric-client.properties";
	private MetricClient metricClient;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// If our configuration file is missing that do not run
		// tests. The configuration file has credentials so we
		// are not able to include in our repository.
		
		File propertiesFile = new File(Resources.getResource(CLIENT_PROPERTY_FILE).toURI());
		assumeTrue(propertiesFile.exists());
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		metricClient = MetricClientFactory.createClient(CLIENT_PROPERTY_FILE);
	}

	@After
	public void tearDown() throws Exception {
		metricClient = null;
	}

	@Test
	public void testMetricClient() throws URISyntaxException, IOException {
		MetricClient client = MetricClientFactory.createClient();
		assertNotNull("check client",client);

	}

	@Ignore
	@Test
	public void testCreateMetric() {
		Random number = new Random();
		
		metricClient.createMetric("BOUNDARY_" + Integer.toString(number.nextInt()),"number");
	}

	@Ignore
	@Test
	public void testAddMeasurementsIntMapOfStringNumberOptionalOfDateTime() {
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testAddMeasurementsListOfMeasurement() {
		fail("Not yet implemented");
	}

}
