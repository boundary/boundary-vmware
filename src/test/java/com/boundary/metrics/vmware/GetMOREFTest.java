package com.boundary.metrics.vmware;

import static com.boundary.metrics.vmware.VMWareTestUtils.DEFAULT_VMWARE_CLIENT_CONFIGURATION;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.vmware.connection.Connection;
import com.vmware.connection.helpers.GetMOREF;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.RuntimeFaultFaultMsg;

/**
 * Regression tests for the {@GetMOREF} class
 * @author davidg
 *
 */
public class GetMOREFTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	private GetMOREF getMOREF;
	private Connection client;
	

	@Before
	public void setUp() throws Exception {
		this.client = VMWareTestUtils.getVMWareConnection(DEFAULT_VMWARE_CLIENT_CONFIGURATION);
		assertNotNull(this.client);
		this.client.connect();
		this.getMOREF = new GetMOREF(client);
		assertNotNull(this.getMOREF);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCreateGetMOREF() {
		Connection client = VMWareTestUtils.getVMWareConnection(DEFAULT_VMWARE_CLIENT_CONFIGURATION);
		assertNotNull(client);
		client.connect();
		GetMOREF getMOREF = new GetMOREF(client);
		assertNotNull(getMOREF);
	}

	@Test
	public void testContainerViewByTypeManagedObjectReferenceStringRetrieveOptions() {
		//fail("Not yet implemented");
	}

	@Test
	public void testContainerViewByTypeManagedObjectReferenceStringRetrieveOptionsStringArray() {
		fail("Not yet implemented");
	}

	@Test
	public void testPropertyFilterSpecs() {
		fail("Not yet implemented");
	}

	@Test
	public void testContainerViewByTypeManagedObjectReferenceStringStringArrayRetrieveOptionsPropertyFilterSpecArray() {
		fail("Not yet implemented");
	}

	@Test
	public void testInFolderByTypeVirtualMachines() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        ManagedObjectReference root = client.getServiceContent().getRootFolder();
		Map<String, ManagedObjectReference> entities = getMOREF.inFolderByType(root,"VirtualMachine");
		assertNotNull(entities);
	}
	
	@Test
	public void testInFolderByTypeVirtualMachines1() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        ManagedObjectReference root = client.getServiceContent().getRootFolder();
		Map<String, ManagedObjectReference> entities = getMOREF.inFolderByType(root,"VirtualMachine");
	}

	@Test
	public void testInContainerByTypeManagedObjectReferenceStringStringArrayRetrieveOptions() {
		fail("Not yet implemented");
	}

	@Test
	public void testInContainerByTypeManagedObjectReferenceStringRetrieveOptions() {
		fail("Not yet implemented");
	}

	@Test
	public void testToMap() {
		fail("Not yet implemented");
	}

	@Test
	public void testPopulateRetrieveResultMapOfStringManagedObjectReference() {
		fail("Not yet implemented");
	}

	@Test
	public void testPopulateRetrieveResultListOfObjectContent() {
		fail("Not yet implemented");
	}

	@Test
	public void testVmByVMname() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetVMTraversalSpec() {
		fail("Not yet implemented");
	}

	@Test
	public void testEntityPropsManagedObjectReferenceStringArray() {
		fail("Not yet implemented");
	}

	@Test
	public void testEntityPropsListOfManagedObjectReferenceStringArray() {
		fail("Not yet implemented");
	}

	@Test
	public void testInContainerByTypeManagedObjectReferenceString() {
		fail("Not yet implemented");
	}

	@Test
	public void testInFolderByTypeManagedObjectReferenceString() {
		fail("Not yet implemented");
	}

	@Test
	public void testInContainerByTypeManagedObjectReferenceStringStringArray() {
		fail("Not yet implemented");
	}

}
