package com.boundary.metrics.vmware.poller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;

public class MORCatalogFactory {
	
	private final static String DEFAULT_CATALOG_FILE = "collection-catalog.json";
	
	public static MORCatalog create() {
		return create(DEFAULT_CATALOG_FILE);
	}
	public static MORCatalog create(String resource) {
		ObjectMapper mapper = new ObjectMapper();
		MORCatalog catalog = null;

		try {
			File catalogFile = new File(Resources.getResource(resource)
					.toURI());
			catalog = mapper.readValue(catalogFile, MORCatalog.class);
		} catch (URISyntaxException e) {

			e.printStackTrace();
		} catch (JsonParseException e) {

			e.printStackTrace();
		} catch (JsonMappingException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}
	
		return catalog;
	}

}
