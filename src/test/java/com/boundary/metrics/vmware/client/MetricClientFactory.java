// Copyright 2014 Boundary, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.boundary.metrics.vmware.client;

import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import com.boundary.metrics.vmware.client.metrics.MetricClient;
import com.google.common.io.Resources;
import com.sun.jersey.api.client.Client;

public class MetricClientFactory {
	
	private final static String DEFAULT_PROPERTY_FILE = "metric-client.properties";
	
	public static MetricClient createClient() throws URISyntaxException, IOException{
		return createClient(DEFAULT_PROPERTY_FILE);
	}
	
	public static MetricClient createClient(String propertyFile) throws URISyntaxException, IOException {
		final String URL = "com.boundary.metrics.metric.client.url";
		final String AUTH = "com.boundary.metrics.metric.client.auth";

		File propertiesFile = new File(Resources.getResource(propertyFile).toURI());
		assumeTrue(propertiesFile.exists());
		Reader reader = new FileReader(propertiesFile);
		Properties clientProperties = new Properties();
		clientProperties.load(reader);
		String url = clientProperties.getProperty(URL);
		String auth = clientProperties.getProperty(AUTH);

		Client client = new Client();
		
		return new MetricClient(client,new URI(url),auth);
	}
}

