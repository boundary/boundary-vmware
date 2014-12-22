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
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.xml.ws.soap.SOAPFaultException;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.boundary.metrics.vmware.client.metrics.Measurement;
import com.boundary.metrics.vmware.client.metrics.MetricsClient;
import com.boundary.metrics.vmware.util.TimeUtils;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.vmware.connection.Connection;
import com.vmware.connection.helpers.GetMOREF;
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

/**
 * Responsible for collecting information from the vSphere endpoint
 *
 */
public class VMWareMetricCollector {
	
    private static final Logger LOG = LoggerFactory.getLogger(VMWareMetricCollector.class);
    
    private static final Function<PerfMetricId, String> toStringFunction = new Function<PerfMetricId, String>() {
        @Nullable
        @Override
        public String apply(@Nullable PerfMetricId input) {
            return input == null ? null : String.format("CounterID: %s, InstanceId: %s", input.getCounterId(), input.getInstance());
        }
    };
    
    private static final Function<PerfCounterInfo, String> toFullName = new Function<PerfCounterInfo, String>() {
        @Nullable
        @Override
        public String apply(@Nullable PerfCounterInfo input) {
            return input == null ? null : String.format("%s.%s.%s", input.getGroupInfo().getKey(),
                    input.getNameInfo().getKey(), input.getRollupType().toString().toUpperCase());
        }
    };
    
    private static String toFullName(PerfCounterInfo perfCounterInfo) {
        return toFullName.apply(perfCounterInfo);
    }
    
    private DateTime lastPoll;
    private Duration skew;

	public VMWareMetricCollector() {
		
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
    public void collectMetrics(VMWareCollectionJob job) throws MalformedURLException, RemoteException,
            InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, SOAPFaultException {
    	
        ManagedObjectReference root = job.getRootMOR();
        
        // 'now' according to the server
        
        DateTime now = job.getTimeAtEndPoint();

        Duration serverSkew = new Duration(now, new DateTime());
        if (serverSkew.isLongerThan(Duration.standardSeconds(1)) &&
                (skew == null || skew.getStandardSeconds() != serverSkew.getStandardSeconds())) {
            LOG.warn("Server {} and local time skewed by {} seconds", job.getHost(), serverSkew.getStandardSeconds());
            skew = serverSkew;
        }
        if (lastPoll == null) {
            lastPoll = now.minusSeconds(20);
        }

        // Holder for all our newly found measurements
        // TODO set an upper size limit on measurements list
        List<Measurement> measurements = Lists.newArrayList();

        /*
        * A {@link PerfMetricId} consistents of the performance counter and
        * the instance it applies to.
        * 
        * In our particulary case we are requesting for all of the instances
        * associated with the performance counter.
        * 
        * Will this work when we have a mix of VirtualMachine, HostSystem, and DataSource
        * managed objects.
        * 
        */
        List<PerfMetricId> perfMetricIds = Lists.newArrayList();
        Map<String,Integer> performanceCounterMap = job.getPerformanceCounterMap();
        Map<Integer,PerfCounterInfo> performanceCounterInfoMap = job.getPerformanceCounterInfoMap();
        
        for (String counterName : job.getMetrics().keySet()) {
            if (performanceCounterMap.containsKey(counterName)) {
                PerfMetricId metricId = new PerfMetricId();
                /* Get the ID for this counter. */
                metricId.setCounterId(performanceCounterMap.get(counterName));
                metricId.setInstance("*");
                perfMetricIds.add(metricId);
            }
        }

        GetMOREF getMOREFs = new GetMOREF(job.getVMWareClient());
        Map<String, ManagedObjectReference> entities = getMOREFs.inFolderByType(root, "VirtualMachine");

        for (Map.Entry<String, ManagedObjectReference> entity : entities.entrySet()) {
            ManagedObjectReference mor = entity.getValue();
            String entityName = entity.getKey();

            /*
            * Create the query specification for queryPerf().
            * Specify 5 minute rollup interval and CSV output format.
            */
            PerfQuerySpec querySpec = new PerfQuerySpec();
            querySpec.setEntity(mor);
            querySpec.setIntervalId(20);
            querySpec.setFormat("normal");
            querySpec.setStartTime(TimeUtils.toXMLGregorianCalendar(lastPoll));
            querySpec.setEndTime(TimeUtils.toXMLGregorianCalendar(now));
            querySpec.getMetricId().addAll(perfMetricIds);

            LOG.info("Entity: {}, MOR: {}-{}, Interval: {}, Format: {}, MetricIds: {}, Start: {}, End: {}", entityName,
                    mor.getType(), mor.getValue(), querySpec.getIntervalId(), querySpec.getFormat(),
                    FluentIterable.from(perfMetricIds).transform(toStringFunction), lastPoll, now);

            List<PerfEntityMetricBase> retrievedStats = job.getVMWareClient().getVimPort().queryPerf(
            		job.getVMWareClient().getServiceContent().getPerfManager(), ImmutableList.of(querySpec));

            /*
            * Cycle through the PerfEntityMetricBase objects. Each object contains
            * a set of statistics for a single ManagedEntity.
            */
            for(PerfEntityMetricBase singleEntityPerfStats : retrievedStats) {
                if (singleEntityPerfStats instanceof PerfEntityMetric) {
                    PerfEntityMetric entityStats = (PerfEntityMetric) singleEntityPerfStats;
                    List<PerfMetricSeries> metricValues = entityStats.getValue();
                    List<PerfSampleInfo> sampleInfos = entityStats.getSampleInfo();

                    for (int x = 0; x < metricValues.size(); x++) {
                        PerfMetricIntSeries metricReading = (PerfMetricIntSeries) metricValues.get(x);
                        PerfCounterInfo metricInfo = performanceCounterInfoMap.get(metricReading.getId().getCounterId());
                        String metricFullName = toFullName.apply(metricInfo);
                        if (!sampleInfos.isEmpty()) {
                            PerfSampleInfo sampleInfo = sampleInfos.get(0);
                            DateTime sampleTime = TimeUtils.toDateTime(sampleInfo.getTimestamp());
                            Number sampleValue = metricReading.getValue().iterator().next();

                            if (skew != null) {
                                sampleTime = sampleTime.plusSeconds((int)skew.getStandardSeconds());
                            }

                            if (metricReading.getValue().size() > 1) {
                                LOG.warn("Metric {} has more than one value, only using the first", metricFullName);
                            }

                            // Prefix the VM name with the name from the monitored entity configuration, we can form unique names that way
                            //int obsDomainId = meterManagerClient.createOrGetMeterMetadata(orgId, client.getName() + "-" + entityName).getObservationDomainId();
                            int obsDomainId = job.getMeterClient().createOrGetMeterMetadata(
                            		job.getOrgId(), job.getVMWareClient().getName() + "-" + entityName).getObservationDomainId();

                            if (metricInfo.getUnitInfo().getKey().equalsIgnoreCase("kiloBytes")) {
                                sampleValue = (long)sampleValue * 1024; // Convert KB to Bytes
                            } else if (metricInfo.getUnitInfo().getKey().equalsIgnoreCase("percent")) {
                                // Convert hundredth of a percent to a decimal percent
                                sampleValue = new Long((long)sampleValue).doubleValue() / 10000.0;
                            }
                            String name = job.getMetrics().get(metricFullName).getName();
                            if (name != null) {
                            Measurement measurement = Measurement.builder()
                                    .setMetric(name)
                                    .setSourceId(obsDomainId)
                                    .setTimestamp(sampleTime)
                                    .setMeasurement(sampleValue)
                                    .build();

                            Measurement dummyMeasurement = Measurement.builder()
                                    .setMetric(name)
                                    .setSourceId(obsDomainId)
                                    .setTimestamp(sampleTime.minusSeconds(10))
                                    .setMeasurement(sampleValue)
                                    .build();

                            measurements.add(measurement);
                            measurements.add(dummyMeasurement); // Fill in enough data so HLM graph can stream

                            LOG.info("{} @ {} = {} {}", metricFullName, sampleTime,
                                    sampleValue, metricInfo.getUnitInfo().getKey());
                            }
                            else {
                            	LOG.warn("Skipping collection of metric: {}",metricFullName);
                            }
                        } else {
                            LOG.warn("Didn't receive any samples when polling for {} on {} between {} and {}",
                                    metricFullName, job.getVMWareClient().getHost(), lastPoll, now);
                        }
                    }
                } else {
                    LOG.error("Unrecognized performance entry type received: {}, ignoring",
                            singleEntityPerfStats.getClass().getName());
                }
            }
        }

        // Send metrics
        if (!measurements.isEmpty()) {
            job.getMetricsClient().addMeasurements(measurements);
        } else {
            LOG.warn("No measurements collected in last poll for {}", job.getVMWareClient().getName());
        }

        // Reset lastPoll time
        lastPoll = now;
    }
}
