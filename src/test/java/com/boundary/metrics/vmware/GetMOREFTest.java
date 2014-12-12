package com.boundary.metrics.vmware;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import static com.boundary.metrics.vmware.VMWareTestUtils.*;

import com.vmware.connection.Connection;
import com.vmware.connection.helpers.GetMOREF;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.TraversalSpec;

/**
 * Regression tests for the {@GetMOREF} class
 * @author davidg
 *
 */
public class GetMOREFTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		client = VMWareTestUtils.getVMWareConnection(DEFAULT_VMWARE_CLIENT_CONFIGURATION);
		assertNotNull(client);
		client.connect();
		getMOREF = new GetMOREF(client);
		assertNotNull(getMOREF);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	private static GetMOREF getMOREF;
	private static Connection client;
	

	@Before
	public void setUp() throws Exception {

	}

	@After
	public void tearDown() throws Exception {
	}
	
	public Map<String, ManagedObjectReference> getManagedObjects(ManagedObjectReference folder, String type) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        ManagedObjectReference root = client.getServiceContent().getRootFolder();
        return getManagedObjects(root,type);
	}
	
	public Map<String, ManagedObjectReference> getManagedObjects(String type) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        ManagedObjectReference root = client.getServiceContent().getRootFolder();
		Map<String, ManagedObjectReference> entities = getMOREF.inFolderByType(root,type);
		assertNotNull(entities);
		System.out.println("# " + type + ": " + entities.size());
		
		for (Map.Entry<String, ManagedObjectReference> entity : entities.entrySet()) {
            ManagedObjectReference mor = entity.getValue();
            String entityName = entity.getKey();
            System.out.println(type + ": " + entityName);
		}
		return entities;
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
	public void testInFolderByTypeVirtualMachines() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
		getManagedObjects("VirtualMachine");
	}
	
	@Test
	public void testInFolderByTypeDatacenter() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
		getManagedObjects("Datacenter");
	}
	
	@Ignore("Broken Test")
	@Test
	public void testInFolderByTypeHostSystem() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        ManagedObjectReference root = client.getServiceContent().getRootFolder();
		Map<String, ManagedObjectReference> entities = getMOREF.inFolderByType(root,"Folder");
		assertNotNull(entities);
		System.out.println("# Folder: " + entities.size());
		
		ManagedObjectReference hosts = entities.get("host");
		
		getManagedObjects(hosts,"ComputeResource");
		
		entities = getMOREF.inFolderByType(hosts,"ComputeResource");
		System.out.println("# ComputeResource: " + entities.size());
		
		for (Map.Entry<String, ManagedObjectReference> entity : entities.entrySet()) {
            ManagedObjectReference mor = entity.getValue();
            String entityName = entity.getKey();
            System.out.println("ComputeResource: " + entity.getKey());
		}
		
		entities = getMOREF.inFolderByType(hosts,"Folder");
		System.out.println("# Folder: " + entities.size());
		
		for (Map.Entry<String, ManagedObjectReference> entity : entities.entrySet()) {
            ManagedObjectReference mor = entity.getValue();
            String entityName = entity.getKey();
            System.out.println("Folder: " + entity.getKey());
		}
	}
	
	@Ignore("None of these managed objects on target")
	@Test
	public void testInFolderByTypeComputeResource() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
		getManagedObjects("ComputeResources");
	}
	
	@Test
	public void testInFolderByTypeFolder() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
		getManagedObjects("Folder");
	}
	
	@Test
	public void testInFolderByTypeNetwork() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
		getManagedObjects("Network");
	}
	
	@Test
	public void testInFolderByTypeDatastore() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
		getManagedObjects("Datastore");
	}
	
	@Test
	public void testVMTraversalSpec() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
		TraversalSpec ts = getMOREF.getVMTraversalSpec();
		assertNotNull(ts);
		
	}
}
