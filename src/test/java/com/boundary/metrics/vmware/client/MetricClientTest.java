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

import static com.boundary.metrics.vmware.poller.MetricAggregates.AVG;
import static com.boundary.metrics.vmware.poller.MetricUnit.NUMBER;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.boundary.metrics.vmware.client.metrics.MetricClient;
import com.boundary.metrics.vmware.poller.MetricDefinition;
import com.boundary.metrics.vmware.poller.MetricDefinitionBuilder;

public class MetricClientTest {

	private static final String CLIENT_PROPERTY_FILE = "metric-client.properties";
	private static File propertiesFile;
	private MetricClient metricClient;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// If our configuration file is missing that do not run
		// tests. The configuration file has credentials so we
		// are not able to include in our repository.
		
		propertiesFile = new File("src/test/resources/" + CLIENT_PROPERTY_FILE);
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
	
	@Test
	public void testMetricCreateUpdate() {
		MetricDefinitionBuilder builder = new MetricDefinitionBuilder();
		builder.setMetric("SYSTEM_FOO")
		       .setDisplayName("System Foo")
		       .setDisplayNameShort("Foo")
		       .setDescription("Foo")
		       .setUnit(NUMBER)
		       .setDefaultResolutionMS(20000)
		       .setDefaultAggregate(AVG);
		MetricDefinition definition = builder.build();
		metricClient.createUpdateMetric(definition);
	}
	
	@Test
	public void testMetricCreateUpdateFromList() {
		MetricDefinitionBuilder builder = new MetricDefinitionBuilder();
		List<MetricDefinition> metrics = new ArrayList<MetricDefinition>();
		builder.setMetric("JDG_ONE")
		       .setDisplayName("JDG One")
		       .setDisplayNameShort("JDG 1")
		       .setDescription("JDG one metric")
		       .setUnit(NUMBER)
		       .setDefaultResolutionMS(20000)
		       .setDefaultAggregate(AVG);
		metrics.add(builder.build());
		builder.setMetric("JDG_TWO")
	       .setDisplayName("JDG Two")
	       .setDisplayNameShort("JDG 2")
	       .setDescription("JDG two metric")
	       .setUnit(NUMBER)
	       .setDefaultResolutionMS(20000)
	       .setDefaultAggregate(AVG);
		metrics.add(builder.build());
		builder.setMetric("JDG_THREE")
	       .setDisplayName("JDG Three")
	       .setDisplayNameShort("JDG 3")
	       .setDescription("JDG three metric")
	       .setUnit(NUMBER)
	       .setDefaultResolutionMS(20000)
	       .setDefaultAggregate(AVG);
		metrics.add(builder.build());
		
		metricClient.createUpdateMetrics(metrics);
	}
}
