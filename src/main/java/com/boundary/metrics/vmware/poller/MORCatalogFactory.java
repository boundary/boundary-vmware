package com.boundary.metrics.vmware.poller;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

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
		File file = null;
		try {
			file = new File(Resources.getResource(resource).toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return create(file);
	}
	
	public static MORCatalog create(File file) {
		ObjectMapper mapper = new ObjectMapper();
		MORCatalog catalog = null;

		try {
			catalog = mapper.readValue(file, MORCatalog.class);

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
