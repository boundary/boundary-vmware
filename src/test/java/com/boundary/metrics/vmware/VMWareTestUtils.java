package com.boundary.metrics.vmware;

import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.jackson.Jackson;

import java.io.File;
import java.io.IOException;

import javax.validation.Validation;
import javax.validation.Validator;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.common.io.Resources;

public class VMWareTestUtils {

	public static VMwarePerfAdapterConfiguration getConfiguration(String resource) throws Exception {

		VMwarePerfAdapterConfiguration configuration = null;
		File validFile = new File(Resources.getResource(resource).toURI());
		System.out.println(resource);

		Validator validator = Validation.buildDefaultValidatorFactory()
				.getValidator();
		ConfigurationFactory<VMwarePerfAdapterConfiguration> factory = new ConfigurationFactory<VMwarePerfAdapterConfiguration>(
				VMwarePerfAdapterConfiguration.class, validator,
				Jackson.newObjectMapper(), "dw");

		try {
			configuration = factory.build(validFile);
		} catch (JsonParseException e) {

			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return configuration;
	}
}
