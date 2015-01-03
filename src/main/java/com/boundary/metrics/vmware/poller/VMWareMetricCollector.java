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

import java.io.File;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.ws.soap.SOAPFaultException;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.boundary.metrics.vmware.client.client.meter.manager.MeterManagerClient;
import com.boundary.metrics.vmware.client.metrics.Measurement;
import com.boundary.metrics.vmware.client.metrics.Metric;
import com.boundary.metrics.vmware.client.metrics.MetricClient;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricSet;
import com.codahale.metrics.Timer;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.RuntimeFaultFaultMsg;

/**
 * Responsible for collecting information from the vSphere endpoint. Drop in replacement
 * for {@link VMwarePerfPoller}
 *
 */
public class VMWareMetricCollector implements Runnable, MetricSet {
	
    private static final Logger LOG = LoggerFactory.getLogger(VMWareMetricCollector.class);
        
    private DateTime lastPoll;
    private Duration skew;
    
    private final AtomicBoolean lock = new AtomicBoolean(false);
    
    private final Timer pollTimer = new Timer();
    private final Meter overrunMeter = new Meter();

    private MetricCollectionJob job;
    

	private VMwareClient vmwClient;
	private MetricClient metricClient;
	private MeterManagerClient meterClient;
	private MonitoredEntity configuration;
    
    public VMWareMetricCollector(VMwareClient vmwClient,
    		MetricClient metricClient,
    		MeterManagerClient meterClient,
    		MonitoredEntity configuration) {
    	
    	this.job = null;
    	this.vmwClient = vmwClient;
    	this.metricClient = metricClient;
    	this.meterClient = meterClient;
    	this.configuration = configuration;
    }
	
    /**
     * Extracts performance metrics from Managed Objects on the monitored entity
     * 
     * @throws MalformedURLException Bad URL
     * @throws RemoteException Endpoint exception
     * @throws InvalidPropertyFaultMsg Bad Property
     * @throws RuntimeFaultFaultMsg Runtime error
     * @throws SOAPFaultException WebServer error
     */
	public void collectMetrics()
			throws MalformedURLException, RemoteException,
			InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, SOAPFaultException {

		// 'now' according to the server

		DateTime now = vmwClient.getTimeAtEndPoint();

//		Duration serverSkew = new Duration(now, new DateTime());
//		if (serverSkew.isLongerThan(Duration.standardSeconds(1))
//				&& (skew == null || skew.getStandardSeconds() != serverSkew
//						.getStandardSeconds())) {
//			LOG.warn("Server {} and local time skewed by {} seconds",
//					job.getHost(), serverSkew.getStandardSeconds());
//			skew = serverSkew;
//		}
		if (lastPoll == null) {
			lastPoll = now.minusSeconds(20);
		}

		// Our catalog consists of managed object types along with their
		// associated performance counters and boundary metric identifiers
		MORCatalog catalog = job.getManagedObjectCatalog();

		for (MORCatalogEntry entry : catalog.getCatalog()) {
			
			// Need a list of the metrics to collect
			List<PerformanceCounterEntry> counters = entry.getCounters();

			Map<String, ManagedObjectReference> entities = vmwClient.getManagedObjects(entry.getType());

			for (Map.Entry<String, ManagedObjectReference> entity : entities.entrySet()) {
				ManagedObjectReference mor = entity.getValue();
				String entityName = entity.getKey();

				// Prefix the VM name with the name from the monitored entity
				// configuration, we can form unique names that way
				String meterName = vmwClient.getName() + "-" + entityName;
				int obsDomainId = meterClient.createOrGetMeterMetadata(meterName).getObservationDomainId();
				
				List<Measurement> measurements = vmwClient.getMeasurements(mor,entityName,obsDomainId,20,lastPoll,now,job.getMetadata());

				// Send metrics
				if (!measurements.isEmpty()) {
					metricClient.addMeasurements(measurements);
				} else {
					LOG.warn("No measurements collected in last poll for {}",vmwClient.getName());
				}
			}
			// Reset lastPoll time
			lastPoll = now;
		}
	}


	@Override
	public Map<String, com.codahale.metrics.Metric> getMetrics() {
        return ImmutableMap.of(
                MetricRegistry.name(getClass(), "poll-timer", vmwClient.getHost()), (com.codahale.metrics.Metric)pollTimer,
                MetricRegistry.name(getClass(), "overrun-meter", vmwClient.getHost()), overrunMeter
                );
	}

    /**
     * Test to see if we need to update our metrics to be collected.
     * 
     * This currently just looks for maps on the instance but in the future
     * the need to update will come from an API call to the integration that indicates
     * which metrics to collect
     * 
     * @return {@link boolean} true, update metric map, false no update needed
     */
	private boolean isMetadataUpdated() {
		boolean update = false;

		/**
		 * If our {@link MetricCollectionJob} instance is null then we need to load our configuration}
		 */
		if (this.job == null) {
			update = true;
		}

		return update;
	}

	@Override
	public void run() {
    	// The lock is used in case the sampling of the metrics takes longer than the poll interval.
    	// If during collection cycle with thread A is in progress and another thread B tries to collect metrics
    	// then thread B fails to get the lock and skips collecting metrics
        if (lock.compareAndSet(false, true)) {
            final Timer.Context timer = pollTimer.time();
            try {
            	// We can call connect() and it handles its connection state by connecting if needed
            	vmwClient.connect();
                
                // Check to see if we need to update which meterics we need
                // to collect from the end point
                if (isMetadataUpdated()) {
                	this.updateMetadata();
                }
               
                // Collect the metrics
                collectMetrics();

            }
            catch (SOAPFaultException s) {
            	s.printStackTrace();
            }
            catch (InvalidPropertyFaultMsg f) {
            	f.printStackTrace();
            }
            catch (RuntimeFaultFaultMsg r) {
            	r.printStackTrace();
            }
            catch (Throwable e) {
                LOG.error("Encountered unexpected error while polling for performance data", e);
                vmwClient.disconnect();
                //TODO: Set the number of retries
            } finally {
            	// Release the lock and stop our timer
                lock.set(false);
                timer.stop();
            }
        } else {
            LOG.warn("Poll of {} already in progress, skipping",vmwClient.getName());
            overrunMeter.mark();
        }
	}

	/**
	 * Fetches all of the required metdata before collection begins.
	 * 
	 * @throws RuntimeFaultFaultMsg vSphere runtime error {@link RuntimeFaultFaultMsg}
	 * @throws InvalidPropertyFaultMsg Incorrect property error {@link InvalidPropertyFaultMsg}
	 */
	private void updateMetadata() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
		MORCatalog catalog = MORCatalogFactory.create(new File(configuration.getCatalog()));
		
		PerformanceCounterCollector collector = new PerformanceCounterCollector(vmwClient);
		PerformanceCounterMetadata perfCounterMetadata = collector.fetchPerformanceCounters();
		Map<String, Map<String, MetricDefinition>> metrics = catalog.getMetrics();
    	VMWareMetadata metadata = new VMWareMetadata(perfCounterMetadata,metrics);
		
		this.job = new MetricCollectionJob(metadata,vmwClient,metricClient,meterClient,catalog);
	}
}
