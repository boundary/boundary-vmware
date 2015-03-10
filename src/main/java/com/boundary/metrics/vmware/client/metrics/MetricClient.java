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

package com.boundary.metrics.vmware.client.metrics;

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.boundary.metrics.vmware.poller.MetricDefinition;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.sun.jersey.api.client.AsyncWebResource;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.async.TypeListener;
import com.sun.jersey.core.util.Base64;

public class MetricClient {

    private final WebResource baseResource;
    private final AsyncWebResource asyncWebResource;
    private final String authentication;

    private static final Joiner PATH_JOINER = Joiner.on('/');
    private static final Logger LOG = LoggerFactory.getLogger(MetricClient.class);

    /**
     * Constructor
     * @param client Jersey client
     * @param baseUrl URI to make REST call
     * @param authentication authentication string
     */
    public MetricClient(Client client, URI baseUrl, String authentication) {
        checkNotNull(client);
        checkNotNull(baseUrl);
        this.baseResource = client.resource(baseUrl);
        this.asyncWebResource = client.asyncResource(baseUrl);
        this.authentication = checkNotNull(authentication);
    }

    /**
     * Creates or updates a metric if the metric already exists
     * 
     * @param metricName Boundary metric identifier
     * @param unit Boundary metric unit
     */
    public void createMetric(String metricName, String unit) {
        CreateUpdateMetric metricRequest = new CreateUpdateMetric(metricName, unit);
        ClientResponse response = baseResource.path(PATH_JOINER.join("v1", "metrics", metricRequest.getName()))
                .header(HttpHeaders.AUTHORIZATION, "Basic " + new String(Base64.encode(authentication), Charsets.US_ASCII))
                .entity(metricRequest, MediaType.APPLICATION_JSON_TYPE)
                .put(ClientResponse.class);
        response.close();
    }
    
    /**
     * Calls the Boundary APIs to create a metric definition from {@link MetricDefinition}
     * 
     * @param definition Instance of {@MetricDefinition} which describes a metric to be created or updated
     */
    public void createUpdateMetric(MetricDefinition definition) {
    	String path = PATH_JOINER.join("v1", "metrics", definition.getMetric());
    	WebResource base = baseResource.path(path);
    	ClientResponse response = base
                .header(HttpHeaders.AUTHORIZATION, "Basic " + new String(Base64.encode(authentication), Charsets.US_ASCII))
                .entity(definition, MediaType.APPLICATION_JSON_TYPE)
                .put(ClientResponse.class);
    	LOG.info("Create or updating metric: {}, HTTP status code {}",base.getURI(),response.getStatus());
        response.close();
    }
    
    /**
     * Calls the Boundary APIs to create/update a list of metric definitions {@link MetricDefinition}
     * @param metricDefinitions
     */
    public void createUpdateMetrics(List<MetricDefinition> metricDefinitions) {
    	
    	for (MetricDefinition definition : metricDefinitions) {
    		createUpdateMetric(definition);
    	}
    }

    /**
     * Adds metric measurements and sends to Boundary APIs
     * 
     * @param sourceId meter source id
     * @param measurements measurements to associate with the source
     * @param optionalTimestamp time of the metrics
     */
    public void addMeasurements(int sourceId, Map<String, Number> measurements, Optional<DateTime> optionalTimestamp) {
        List<List<Object>> payload = Lists.newArrayList();
        final long timestamp = optionalTimestamp.or(new DateTime()).getMillis();
        for (Map.Entry<String,Number> m : measurements.entrySet()) {
        	LOG.debug("Measurement: {}, {}, {}, {}",String.valueOf(sourceId),m.getKey(),m.getValue(),timestamp);
            payload.add(ImmutableList.<Object>of(String.valueOf(sourceId),m.getKey(),m.getValue(),timestamp));
        }
        sendMeasurements(payload);
    }

    /**
     * 
     * @param measurements Metrics measurements
     */
    public void addMeasurements(List<Measurement> measurements) {
    	if (LOG.isDebugEnabled()) {
    		for (Measurement m : measurements) {
    			LOG.debug("{} ",m.toString());
    		}
    	}
        sendMeasurements(FluentIterable.from(measurements)
                .transform(MetricUtils.toBulkEntry())
                .toList());
    }

    /**
     * Sends measurements to the Boundary metric API
     * @param payload List of measurements
     */
    private void sendMeasurements(final List<List<Object>> payload) {
        asyncWebResource.path(PATH_JOINER.join("v1", "measurements"))
                .header(HttpHeaders.AUTHORIZATION, "Basic " + new String(Base64.encode(authentication), Charsets.US_ASCII))
                .entity(payload, MediaType.APPLICATION_JSON_TYPE)
                .post(new TypeListener<ClientResponse>(ClientResponse.class) {
                    @Override
                    public void onComplete(Future<ClientResponse> f) throws InterruptedException {
                        try {
                            ClientResponse response = f.get();
                            String entity = response.getEntity(String.class);
                            response.close();
                            LOG.debug("HTTPS Result: {}",response.getStatus());
                            if (Response.Status.OK.getStatusCode() != response.getStatus()) {
                                LOG.error("Unexpected response adding measurements: {} - {}", response.getStatusInfo(), entity);
                                throw new RuntimeException(entity);
                            }
                            response.close();
                        } catch (ExecutionException e) {
                            LOG.error("Interrupted trying to add measurement");
                        }
                    }
                });
    }
}
