package com.boundary.metrics.vmware.client;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.boundary.metrics.vmware.client.client.meter.manager.MeterMetadata;

public class MeterMetadataTest {

	MeterMetadata meterMetadata;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		meterMetadata = new MeterMetadata();
	}

	@After
	public void tearDown() throws Exception {
		meterMetadata = null;
	}

	@Test
	public void testNull() {
		assertNotNull(meterMetadata);
	}

	@Test
	public void testGetId() {
		assertNull(meterMetadata.getId());
	}

	@Test
	public void testGetName() {
		assertNull(meterMetadata.getName());
	}

	@Test
	public void testGetObservationDomainId() {
		assertNull(meterMetadata.getName());
	}

	@Test
	public void testGetOrgId() {
		assertNull(meterMetadata.getOrgId());
	}
}
