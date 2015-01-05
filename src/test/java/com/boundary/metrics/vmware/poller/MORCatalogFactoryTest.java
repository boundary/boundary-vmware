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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MORCatalogFactoryTest {
	
	
	private final String TEST_CATALOG_FILE = "test-catalog.json";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
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
	public void testCounters() {
		MORCatalog inventory = MORCatalogFactory.create(TEST_CATALOG_FILE);
		
		assertNotNull("check InventoryCatalog",inventory);
		List<MORCatalogEntry> catalog = inventory.getCatalog();
		assertNotNull("check Inventory.getCatalog()",inventory.getCatalog());
		assertEquals("check Catalog Size",3,catalog.size());
		MORCatalogEntry catalogEntry1 = catalog.get(0);
		assertNotNull("check CatalogEntry",catalogEntry1);
		List<PerformanceCounterEntry> performanceCounters1 = catalogEntry1.getCounters();
		assertNotNull("check CatalogEntry.getPerformanceCounters",performanceCounters1);
		assertEquals("check PerformanceCounters size()",3,performanceCounters1.size());
		PerformanceCounterEntry performanceCounterEntry = performanceCounters1.get(0);
		assertEquals("check PerformanceCounterEntry.get","cpu.usage.AVERAGE",performanceCounterEntry.getName());
	}
	
	@Test
	public void testDefinitions() {
		MORCatalog inventory = MORCatalogFactory.create(TEST_CATALOG_FILE);
		
		assertEquals("check definitions",7,inventory.getDefinitions().size());
		
		List<MetricDefinition> definitions = inventory.getDefinitions();
		
		for (MetricDefinition definition : definitions) {
			
			switch(definition.getMetric()) {
			case "SYSTEM_CPU_USAGE_AVERAGE":
				assertEquals("Check unit",MetricUnit.percent,definition.getUnit());
				break;
			}
			
		}
	}
	
	@Test
	public void testObjectTypeToMetricMap() {
		MORCatalog inventory = MORCatalogFactory.create(TEST_CATALOG_FILE);

		Map<String, Map<String, MetricDefinition>> metrics = inventory.getMetrics();
		System.out.println(metrics);

		Map<String, MetricDefinition> virtualMachine = metrics.get("VirtualMachine");
		assertNotNull("check VirtualMachine for cpu.usage.AVERAGE",virtualMachine.get("cpu.usage.AVERAGE"));
		assertEquals("check VirtualMachine definition for cpu.usage.AVERAGE","SYSTEM_CPU_USAGE_AVERAGE",virtualMachine.get("cpu.usage.AVERAGE").getMetric());
		assertNotNull("check VirtualMachine for cpu.usage.MINIMUM",virtualMachine.get("cpu.usage.MINIMUM"));
		assertEquals("check VirtualMachine definition for cpu.usage.MINIMUM","SYSTEM_CPU_USAGE_MINIMUM",virtualMachine.get("cpu.usage.MINIMUM").getMetric());
		assertNotNull("check VirtualMachine for cpu.usage.MAXIMUM",virtualMachine.get("cpu.usage.MAXIMUM"));
		assertEquals("check VirtualMachine definition for cpu.usage.MAXIMUM","SYSTEM_CPU_USAGE_MAXIMUM",virtualMachine.get("cpu.usage.MAXIMUM").getMetric());

		Map<String, MetricDefinition> hostSystem = metrics.get("HostSystem");
		assertNotNull("check HostSystem for cpu.usage.AVERAGE",hostSystem.get("cpu.usage.AVERAGE"));
		assertNotNull("check HostSystem for cpu.usage.MINIMUM",hostSystem.get("cpu.usage.MINIMUM"));
		assertNotNull("check HostSystem for cpu.usage.MAXIMUM",hostSystem.get("cpu.usage.MAXIMUM"));

		Map<String, MetricDefinition> datastore = metrics.get("Datastore");
		assertNotNull("check Datastore for disk.capacity.SUM",datastore.get("disk.capacity.SUM"));
		assertNotNull("check Datastore for disk.provisioned.SUM",datastore.get("disk.provisioned.SUM"));
		assertNotNull("check Datastore for disk.used.SUM",datastore.get("disk.used.SUM"));

	}
	
	@Test
	public void testValidate() {
		MORCatalog inventory = MORCatalogFactory.create(TEST_CATALOG_FILE);
		
		assertTrue("Check validate",inventory.isValid(true));
	}
}
