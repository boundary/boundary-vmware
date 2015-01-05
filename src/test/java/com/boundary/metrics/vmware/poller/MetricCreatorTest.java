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

package com.boundary.metrics.vmware.poller;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

import java.io.File;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.boundary.metrics.vmware.VMWareTestUtils;
import com.boundary.metrics.vmware.client.metrics.MetricClient;

public class MetricCreatorTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		File propertiesFile = new File("src/test/resources/" + VMWareTestUtils.DEFAULT_METRIC_CLIENT_CONFIGURATION);
		assumeTrue(propertiesFile.exists());
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
	public void testCreateMetricDefinitions() throws Exception {
		MetricClient client = VMWareTestUtils.getMetricClient();
		assertNotNull("Check metric client",client);
	}

}
