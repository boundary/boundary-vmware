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

package com.boundary.metrics.vmware;

import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import com.boundary.metrics.vmware.poller.VMwareClient;
import com.google.common.base.Joiner;
import com.google.common.io.Resources;
import com.vmware.connection.Connection;

/*
 * Factory class to create {@link VMwareClient} instances
 */
public class VMWareClientFactory {
	
	private final static String DEFAULT_PROPERTY_FILE = "vmware-client.properties";
	
	public static VMwareClient createClient() throws URISyntaxException, IOException {
		return createClient(DEFAULT_PROPERTY_FILE);
	}
	
	public static VMwareClient createClient(String propertyFile) throws URISyntaxException, IOException {
		final String URL = "com.boundary.metrics.vmware.client.url";
		final String USER = "com.boundary.metrics.vmware.client.user";
		final String PASSWORD = "com.boundary.metrics.vmware.client.password";

		File propertiesFile = new File(Resources.getResource(propertyFile).toURI());
		assumeTrue(propertiesFile.exists());
		Reader reader = new FileReader(propertiesFile);
		Properties clientProperties = new Properties();
		clientProperties.load(reader);
		String url = clientProperties.getProperty(URL);
		String user = clientProperties.getProperty(USER);
		String password = clientProperties.getProperty(PASSWORD);
		
		return new VMwareClient(new URI(url), user, password,VMWareClientFactory.class.toString());
	}
}
