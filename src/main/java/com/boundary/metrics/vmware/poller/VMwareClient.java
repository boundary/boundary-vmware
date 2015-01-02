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

package com.boundary.metrics.vmware.poller;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.boundary.metrics.vmware.client.metrics.Measurement;
import com.boundary.metrics.vmware.util.TimeUtils;
import com.google.common.base.Throwables;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.vmware.connection.Connection;
import com.vmware.connection.helpers.GetMOREF;
import com.vmware.vim25.AboutInfo;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.PerfCounterInfo;
import com.vmware.vim25.PerfEntityMetric;
import com.vmware.vim25.PerfEntityMetricBase;
import com.vmware.vim25.PerfMetricId;
import com.vmware.vim25.PerfMetricIntSeries;
import com.vmware.vim25.PerfMetricSeries;
import com.vmware.vim25.PerfQuerySpec;
import com.vmware.vim25.PerfSampleInfo;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.ServiceContent;
import com.vmware.vim25.UserSession;
import com.vmware.vim25.VimPortType;
import com.vmware.vim25.VimService;

/**
 * VMware Connection with no SSL validation
 */
public class VMwareClient implements Connection {

    private static final Logger LOG = LoggerFactory.getLogger(VMwareClient.class);

    private URI uri;
    private String username;
    private String password;
	private String name;
    private VimService vimService;
    private VimPortType vimPort;
    private UserSession userSession;
    private ServiceContent serviceContent;
    private ManagedObjectReference SERVICE_INSTANCE_REFERENCE;
    @SuppressWarnings("rawtypes")
    private Map headers;


    /**
     * Creates a vSphere client connection provided the end point
     * and authentication tokens (username and password).
     * 
     * @param url {@link URI} Web service end point.
     * @param username {@link String} user to authenticate with
     * @param password {@link String} password to use for authentication
     * @param name {@link String} Name of end point for logging and tagging
     */
    public VMwareClient(URI uri, String username, String password,String name) {
        this.uri = uri;
        this.username = username;
        this.password = password;
        this.name = name;
    }

    @Override
    public void setUrl(String url) {
        this.uri = URI.create(url);
    }

    @Override
    public String getUrl() {
        return uri.toASCIIString();
    }

    @Override
    public String getHost() {
        return uri.getHost();
    }

    @Override
    public Integer getPort() {
        return uri.getPort();
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getPassword() {
        return password;
    }
    
    @Override
    public String getName() {
    	return this.name;
    }

    @Override
    public VimService getVimService() {
        return vimService;
    }

    @Override
    public VimPortType getVimPort() {
        return vimPort;
    }

    @Override
    public ServiceContent getServiceContent() {
        return serviceContent;
    }

    @Override
    public UserSession getUserSession() {
        return userSession;
    }

    @Override
    public String getServiceInstanceName() {
        return "ServiceInstance";
    }

    @Override
    public Map getHeaders() {
        return headers;
    }

    /**
     * Returns a reference to the "ServiceInstance" managed object
     * of the connected end point.
     * 
     * @return {@link ManagedObjectReference}
     */
    @Override
    public ManagedObjectReference getServiceInstanceReference() {
        return SERVICE_INSTANCE_REFERENCE;
    }

    /**
     * Invokes a connection to the Web Service end point using the provide
     * credentials.
     * 
     */
    @Override
    public Connection connect() {
    	LOG.debug("Monitored entity {} is connecting.",getName());
        if (!isConnected()) {
            try {
                // Variables of the following types for access to the API methods
                // and to the vSphere inventory.
                // -- ManagedObjectReference for the ServiceInstance on the Server
                // -- VimService for access to the vSphere Web service
                // -- VimPortType for access to methods
                // -- ServiceContent for access to managed object services
            	SERVICE_INSTANCE_REFERENCE = new ManagedObjectReference();

                // Declare a host name verifier that will automatically enable
                // the connection. The host name verifier is invoked during
                // the SSL handshake.
                HostnameVerifier hv = new HostnameVerifier() {
                    public boolean verify(String urlHostName, SSLSession session) {
                        return true;
                    }
                };

                // Create the trust manager.
                TrustManager[] trustAllCerts = new TrustManager[]{new TrustAllTrustManager()};

                // Create the SSL context
                SSLContext sc = SSLContext.getInstance("SSL");

                // Create the session context
                SSLSessionContext sslsc = sc.getServerSessionContext();

                // Initialize the contexts; the session context takes the trust manager.
                sslsc.setSessionTimeout(0);
                sc.init(null, trustAllCerts, null);

                // Use the default socket factory to create the socket for the secure connection
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                // Set the default host name verifier to enable the connection.
                HttpsURLConnection.setDefaultHostnameVerifier(hv);

                // Set up the manufactured managed object reference for the ServiceInstance
                SERVICE_INSTANCE_REFERENCE.setType("ServiceInstance");
                SERVICE_INSTANCE_REFERENCE.setValue("ServiceInstance");

                // Create a VimService object to obtain a VimPort binding provider.
                // The BindingProvider provides access to the protocol fields
                // in request/response messages. Retrieve the request context
                // which will be used for processing message requests.
                vimService = new VimService();
                vimPort = vimService.getVimPort();
                Map<String, Object> ctxt = ((BindingProvider) vimPort).getRequestContext();

                // Store the Server URL in the request context and specify true
                // to maintain the connection between the client and server.
                // The client API will include the Server's HTTP cookie in its
                // requests to maintain the session. If you do not set this to true,
                // the Server will start a new session with each request.
                ctxt.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, uri.toASCIIString());
                ctxt.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);

                // Retrieve the ServiceContent object and login
                serviceContent = vimPort.retrieveServiceContent(SERVICE_INSTANCE_REFERENCE);
                headers = (Map) ((BindingProvider) vimPort).getResponseContext().get(
                                MessageContext.HTTP_RESPONSE_HEADERS);
                userSession = vimPort.login(serviceContent.getSessionManager(),
                        username,
                        password,
                        null);

                // Display summary information about the product name, server type, and product version
                AboutInfo about = serviceContent.getAbout();
                LOG.info("Successfully connected to {} ({}) using API version {} (of type {})",
                        getName(), about.getFullName(), about.getApiVersion(), about.getApiType());
            } catch (Exception e) {
                LOG.error("Unable to connect to " + getHost(), e);
            }
        }
        
        return this;
    }

    /**
     * State of the connection to the end point which is either: true (connected) or false (note connected)
     * 
     * @return {@link boolean} connection state
     */
    @Override
    public boolean isConnected() {
    	boolean  result = false;
    	
    	// If our userSession is null it indicates we never had successfull connection
    	// so we can reliably return false
        if (userSession == null) {
            LOG.info("UserSession is null, disconnected");
            result = false;
        }
        else {
        	DateTime startTime = TimeUtils.toDateTime(userSession.getLastActiveTime());
        	DateTime currentTime = new DateTime();
        	DateTime endTime = startTime.plusMinutes(30);
        	result = endTime.isBeforeNow();
        	LOG.info("Testing for a stale connection: startTime: {}, endTime: {}, currentTime: {}, stale: {}",
        			startTime,endTime,currentTime,result);
        }
        
        return result;
    }

    /**
     * Disconnect from the end point.
     * 
     */
    @Override
    public Connection disconnect() {
    	LOG.debug("Monitored entity {} is disconnectiong",getName());
        try {
        	if (vimPort != null) {
        		vimPort.logout(serviceContent.getSessionManager());
        	}
        } catch (RuntimeFaultFaultMsg e) {
            throw Throwables.propagate(e);
        } finally {
            // A connection is very memory intensive, I'm helping the garbage collector here
            userSession = null;
            serviceContent = null;
            vimPort = null;
            vimService = null;
        }
        return this;
    }

    /**
     * Returns the web service end point {@link URL}
     * associated with this client.
     * 
     * @return {@link URL}
     */
    @Override
    public URL getURL() {
        try {
            return uri.toURL();
        } catch (MalformedURLException e) {
            throw Throwables.propagate(e);
        }
    }
    
    /**
     * Returns the current time at the vSphere end point
     * 
     * @return {@link DateTime}
     * @throws RuntimeFaultFaultMsg
     */
    public DateTime getTimeAtEndPoint() throws RuntimeFaultFaultMsg {
		return TimeUtils.toDateTime(getVimPort().currentTime(getServiceInstanceReference()));
	}
    
    /**
     * Convenience function to get a reference to the property collector.
     * 
     * @return {@link ManagedObjectReference}
     */
    public ManagedObjectReference getPropertyCollector() {
    	return this.getServiceContent().getPropertyCollector();
    }
    
	private List<Measurement> extractMeasurements(String entityName,
			int obsDomainId, PerfEntityMetricBase perfStats,
			VMWareMetadata metadata) {
		List<Measurement> measurements = new ArrayList<Measurement>();
		PerfEntityMetric entityStats = (PerfEntityMetric) perfStats;
		List<PerfMetricSeries> metricValues = entityStats.getValue();
		List<PerfSampleInfo> sampleInfos = entityStats.getSampleInfo();

		for (int x = 0; x < metricValues.size(); x++) {
			PerfMetricIntSeries metricReading = (PerfMetricIntSeries) metricValues.get(x);
			PerfCounterInfo metricInfo = metadata.getInfoMap().get(metricReading.getId().getCounterId());
			String metricFullName = PerformanceCounterMetadata.toFullName(metricInfo);
			if (!sampleInfos.isEmpty()) {
				PerfSampleInfo sampleInfo = sampleInfos.get(0);
				DateTime sampleTime = TimeUtils.toDateTime(sampleInfo.getTimestamp());
				Number sampleValue = metricReading.getValue().iterator().next();

				// if (skew != null) {
				// sampleTime =
				// sampleTime.plusSeconds((int)skew.getStandardSeconds());
				// }

				if (metricReading.getValue().size() > 1) {
					LOG.warn("Metric {} has more than one value, only using the first",metricFullName);
				}
				// Scale data based on the metric
				sampleValue = PerformanceCounterMetadata.computeValue(metricInfo,sampleValue);
				String name = metadata.getMetricName(metricFullName);
				if (name != null) {
					Measurement measurement = Measurement.builder()
							.setMetric(name)
							.setSourceId(obsDomainId)
							.setTimestamp(sampleTime)
							.setMeasurement(sampleValue).build();
					measurements.add(measurement);

					LOG.info("{} @ {} = {} {}", metricFullName, sampleTime,
							sampleValue,metricInfo.getUnitInfo().getKey());
				} else {
					LOG.warn("Skipping collection of metric: {}",metricFullName);
			    }
			} else {
				LOG.warn("Didn't receive any samples when polling for {} on {}",metricFullName,this.getName());
			}
		}

		return measurements;
	}
    /**
     * Query vSphere for values of performance metrics
     * 
     * @param querySpec
     * @return {@link List} List of {@link PerfEntityMetricBase}
     * @throws RuntimeFaultFaultMsg Any runtime issue
     */
    public List<PerfEntityMetricBase> getStats(ManagedObjectReference mor,
    		Integer intervalId,DateTime start,DateTime end,
    		List<PerfMetricId> perfMetricIds) throws RuntimeFaultFaultMsg {
    	
    	LOG.debug("interval: {}, start: {}, end: {}",intervalId,start,end);
    	
		/*
		 * Create the query specification for queryPerf().
		 */
		PerfQuerySpec querySpec = new PerfQuerySpec();
		querySpec.setEntity(mor);
		querySpec.setIntervalId(intervalId);
		querySpec.setFormat("normal");
		querySpec.setStartTime(TimeUtils.toXMLGregorianCalendar(start));
		querySpec.setEndTime(TimeUtils.toXMLGregorianCalendar(end));
		querySpec.getMetricId().addAll(perfMetricIds);

		LOG.info("MOR: {}-{}, Interval: {}, Format: {}, MetricIds: {}, Start: {}, End: {}",
				mor.getType(),
				mor.getValue(),
				querySpec.getIntervalId(),
				querySpec.getFormat(),
				FluentIterable.from(perfMetricIds).transform(PerformanceCounterMetadata.toStringFunction),
				start, end);
    	return this.getVimPort().queryPerf(this.getServiceContent().getPerfManager(),ImmutableList.of(querySpec));
    }

	public List<Measurement> getMeasurements(ManagedObjectReference mor,
			String entityName, int obsDomainId, Integer intervalId, DateTime start,
			DateTime end, VMWareMetadata metadata) throws RuntimeFaultFaultMsg {

		List<Measurement> measurements = null;

		List<PerfEntityMetricBase> retrievedStats = getStats(mor,intervalId,start,end,metadata.getPerfMetrics());
		LOG.debug("stats count: {}",retrievedStats.size());

		/*
		 * Cycle through the PerfEntityMetricBase objects. Each object contains
		 * a set of statistics for a single ManagedEntity.
		 */
		for (PerfEntityMetricBase perfStat : retrievedStats) {
			if (perfStat instanceof PerfEntityMetric) {
				measurements = extractMeasurements(entityName,obsDomainId,perfStat, metadata);
			} else {
				LOG.error("Unrecognized performance entry type received: {}, ignoring",
						perfStat.getClass().getName());
			}
		}
		return measurements;
	}
    
    /**
     * Query vSphere to get list of managed objects by their type
     *  
     * @param managedObjectType type of the managed object to look up
     * @return {@link Map}
     * @throws RuntimeFaultFaultMsg Runtime error occurred
     * @throws InvalidPropertyFaultMsg Invalid property
     */
    public Map<String,ManagedObjectReference> getManagedObjects(String managedObjectType) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        GetMOREF getMOREFs = new GetMOREF(this);
        ManagedObjectReference root = this.getServiceContent().getRootFolder();
        Map<String,ManagedObjectReference> entities = getMOREFs.inFolderByType(root,managedObjectType);
        return entities;
	}
    
    public ManagedObjectReference getVMByName(String vmName) throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
    	ManagedObjectReference mor = null;
		GetMOREF search = new GetMOREF(this);
		
		mor = search.vmByVMname(vmName,this.getPropertyCollector());
		return mor;
    }
    /**
	 * Authentication is handled by using a TrustManager and supplying
     * a host name verifier method. (The host name verifier is declared
     * in the main function.)
     */
    private static class TrustAllTrustManager implements TrustManager, X509TrustManager {

        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        @Override
        public void checkServerTrusted(java.security.cert.X509Certificate[] certs,
                                       String authType) throws CertificateException {
        }

        @Override
        public void checkClientTrusted(java.security.cert.X509Certificate[] certs,
                                       String authType) throws CertificateException {
        }
    }
}