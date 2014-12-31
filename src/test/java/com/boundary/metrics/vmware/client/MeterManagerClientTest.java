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
import static org.junit.Assume.*;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Environment;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.boundary.metrics.vmware.VMWareTestUtils;
import com.boundary.metrics.vmware.VMwarePerfAdapterConfiguration;
import com.boundary.metrics.vmware.client.client.meter.manager.MeterManagerClient;
import com.boundary.metrics.vmware.client.client.meter.manager.MeterMetadata;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.io.Resources;
import com.sun.jersey.api.client.Client;

public class MeterManagerClientTest {
	
	private static MeterManagerClient client;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		client = VMWareTestUtils.getMeterClient();
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
	public void testCreateMeter() {
		String meterName = "my-test-meter";
		client.createOrGetMeterMetadata(meterName);
		Optional<MeterMetadata> result = client.getMeterMetadataByName(meterName);
		MeterMetadata meter = result.get();
		assertNotNull("Check for Not Null meter: getMeterMetadataByName()",meter);
		assertEquals("Check getName()",meterName,meter.getName());
		//assertEquals("Check getOrgId()",configuration.getOrgId(),meter.getOrgId());
		String meterId = meter.getId();
		System.out.println(meterId);
		
		result = client.getMeterMetadataById(meterId);
		assertNotNull("Check for Not Null meter: getMeterMetadataById()",meter);
		assertEquals("Check getName()",meterName,meter.getName());
		//assertEquals("Check getOrgId()",configuration.getOrgId(),meter.getOrgId());
	}
	
	
	public static boolean isInteger(String str)  
	{  
	  try  
	  {  
	    int i = Integer.parseInt(str);  
	  }  
	  catch(NumberFormatException nfe)  
	  {  
	    return false;  
	  }  
	  return true;  
	}
	
	public static boolean isRegExInteger(String str)
	{
	  return str.matches("\\d+");  //match a number with optional '-' and decimal.
	}
	
	@Test
	public void testGetObsId() {
		String meterName = String.format("%s-%s","JDG","my-meter");
		int obsDomainId = client.createOrGetMeterMetadata(meterName).getObservationDomainId();
		System.out.printf("obsDomainId: %X",obsDomainId);
		assertTrue("Check if number via RegEx",isRegExInteger(Integer.toString(obsDomainId)));
		assertTrue("Check if number via NumberFormatException",isInteger(Integer.toString(obsDomainId)));
	}
}
