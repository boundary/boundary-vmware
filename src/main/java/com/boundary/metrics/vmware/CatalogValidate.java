package com.boundary.metrics.vmware;

import java.io.File;

import com.boundary.metrics.vmware.poller.MORCatalog;
import com.boundary.metrics.vmware.poller.MORCatalogFactory;

public class CatalogValidate {
	
	
	CatalogValidate() {
		
	}
	
	public boolean validate(File catalogFile) {
		boolean valid = true;
		MORCatalog catalog = MORCatalogFactory.create(catalogFile);
		
		valid = catalog.isValid(false);
		
		return valid;
	}
	
	public static void main(String [] args) {
		
		if (args.length != 1) {
			System.err.println("usage: " + CatalogValidate.class.getCanonicalName() + " <path to catalog>");
			System.exit(1);
		}
		CatalogValidate validator = new CatalogValidate();
		boolean valid = validator.validate(new File(args[0]));
		System.err.printf("catalog %s is %s\n",args[0], valid ? "valid" : "not valid");
	}

}
