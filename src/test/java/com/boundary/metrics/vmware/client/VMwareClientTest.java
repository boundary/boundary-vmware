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

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.boundary.metrics.vmware.poller.VMwareClient;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import com.vmware.connection.Connection;
import com.vmware.vim25.ArrayOfPerfCounterInfo;
import com.vmware.vim25.DynamicProperty;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.ObjectContent;
import com.vmware.vim25.ObjectSpec;
import com.vmware.vim25.PerfCounterInfo;
import com.vmware.vim25.PropertyFilterSpec;
import com.vmware.vim25.PropertySpec;
import com.vmware.vim25.RetrieveOptions;
import com.vmware.vim25.RetrieveResult;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.ServiceContent;
import com.vmware.vim25.VimPortType;
import com.vmware.vim25.VimService;

public class VMwareClientTest {

	private Connection vmClient;

	private static Properties clientProperties;

	private final static String URL = "com.boundary.metrics.vmware.client.url";
	private final static String USER = "com.boundary.metrics.vmware.client.user";
	private final static String PASSWORD = "com.boundary.metrics.vmware.client.password";

	private static String url;
	private static String user;
	private static String password;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// If our configuration file is missing that do not run
		// tests. The configuration file has credentials so we
		// are not able to include in our repository.
		String propertyFile = "vmware-client.properties";
		Joiner propertyFileJoin = Joiner.on("//");
		String propertyFilePath = propertyFileJoin.join("src/test/resources",propertyFile);
		assumeTrue(new File(propertyFilePath).exists());
		
		File propertiesFile = new File(Resources.getResource(propertyFile).toURI());
		assumeTrue(propertiesFile.exists());
		Reader reader = new FileReader(propertiesFile);
		clientProperties = new Properties();
		clientProperties.load(reader);
		url = clientProperties.getProperty(URL);
		user = clientProperties.getProperty(USER);
		password = clientProperties.getProperty(PASSWORD);

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		clientProperties = null;
	}

	@Before
	public void setUp() throws Exception {
		vmClient = new VMwareClient(new URI(url), user, password,this.getClass().toString());
		vmClient.connect();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testPropertiesLoad() {
		assertNotNull(clientProperties);
	}

	@Test
	public void testClientConnection() throws URISyntaxException {
		Connection client = new VMwareClient(new URI(url),user,password,this.getClass().toString());

		client.connect();
	}

	@Test
	public void testGetServiceContent() {
		ServiceContent content = vmClient.getServiceContent();
		assertNotNull(content);
	}

	@Test
	public void testGetPerformanceManager() {
		ServiceContent content = vmClient.getServiceContent();
		ManagedObjectReference performanceManager = content.getPerfManager();
		assertNotNull(performanceManager);

		System.out.println(performanceManager.getClass().toString());
	}

	@Test
	public void testGetVimPort() {
		VimPortType vimPortType = vmClient.getVimPort();
		assertNotNull(vimPortType);
	}

	@Test
	public void testGetVimService() {
		VimService vimService = vmClient.getVimService();
		assertNotNull(vimService);
	}

	@Test
	public void testGetPerformanceManagerDescription() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
		ServiceContent content = vmClient.getServiceContent();
		ManagedObjectReference perfManager = content.getPerfManager();
		ObjectSpec oSpec = new ObjectSpec();
		oSpec.setObj(perfManager);

		PropertySpec pSpec = new PropertySpec();
		pSpec.setType("PerformanceManager");
		pSpec.getPathSet().add("perfCounter");

		PropertyFilterSpec propertyFilter = new PropertyFilterSpec();
		propertyFilter.getObjectSet().add(oSpec);
		propertyFilter.getPropSet().add(pSpec);
		
        RetrieveOptions retriveOptions = new RetrieveOptions();
        RetrieveResult retrieveResult = vmClient.getVimPort().retrievePropertiesEx(
        		vmClient.getServiceContent().getPropertyCollector(),
        		ImmutableList.of(propertyFilter), retriveOptions);

        for (ObjectContent oc : retrieveResult.getObjects()) {
            if (oc.getPropSet() != null) {
                for (DynamicProperty dp : oc.getPropSet()) {
                    List<PerfCounterInfo> perfCounters = ((ArrayOfPerfCounterInfo)dp.getVal()).getPerfCounterInfo();
                    if (perfCounters != null) {
                        for (PerfCounterInfo perfCounter : perfCounters) {
                        	System.out.println("id: " + Integer.toString(perfCounter.getKey()));
                        	System.out.println("groupInfo: " + perfCounter.getGroupInfo().getKey());
                        	System.out.println("nameInfo: " + perfCounter.getNameInfo().getKey());
                        	System.out.println("RollupType: " + perfCounter.getRollupType().toString().toUpperCase());
                        }
                    }
                }
            }
        }        

	}
}
