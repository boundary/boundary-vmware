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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;

public class InventoryCatalogTest {

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
	public void test() {
		ObjectMapper mapper = new ObjectMapper();
		InventoryCatalog inventory = null;
		String configFile = "collection-catalog.json";

		try {
			File catalogFile = new File(Resources.getResource(configFile)
					.toURI());
			inventory = mapper.readValue(catalogFile, InventoryCatalog.class);
		} catch (URISyntaxException e) {

			e.printStackTrace();
		} catch (JsonParseException e) {

			e.printStackTrace();
		} catch (JsonMappingException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}
		
		assertNotNull("check InventoryCatalog",inventory);
		List<CatalogEntry> catalog = inventory.getCatalog();
		assertNotNull("check Inventory.getCatalog()",inventory.getCatalog());
		assertEquals("check Catalog Size",3,catalog.size());
		CatalogEntry catalogEntry1 = catalog.get(0);
		assertNotNull("check CatalogEntry",catalogEntry1);
		List<PerformanceCounterEntry> performanceCounters1 = catalogEntry1.getPerformanceCounters();
		assertNotNull("check CatalogEntry.getPerformanceCounters",performanceCounters1);
		assertEquals("check PerformanceCounters size()",8,performanceCounters1.size());
		PerformanceCounterEntry performanceCounterEntry = performanceCounters1.get(0);
		assertEquals("check PerformanceCounterEntry.get","cpu.usage.average",performanceCounterEntry.getCounterName());
	}
}
