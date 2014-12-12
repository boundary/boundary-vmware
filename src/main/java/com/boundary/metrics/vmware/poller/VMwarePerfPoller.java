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

import com.boundary.metrics.vmware.client.client.meter.manager.MeterManagerClient;
import com.boundary.metrics.vmware.client.client.metrics.*;
import com.boundary.metrics.vmware.util.TimeUtils;
import com.codahale.metrics.*;
import com.codahale.metrics.Metric;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.vmware.connection.Connection;
import com.vmware.connection.helpers.GetMOREF;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.ObjectContent;
import com.vmware.vim25.ObjectSpec;
import com.vmware.vim25.PerfCounterInfo;
import com.vmware.vim25.PerfEntityMetric;
import com.vmware.vim25.PerfEntityMetricBase;
import com.vmware.vim25.PerfMetricId;
import com.vmware.vim25.PerfMetricIntSeries;
import com.vmware.vim25.PerfMetricSeries;
import com.vmware.vim25.PerfQuerySpec;
import com.vmware.vim25.PerfSampleInfo;
import com.vmware.vim25.PropertyFilterSpec;
import com.vmware.vim25.PropertySpec;
import com.vmware.vim25.RetrieveOptions;
import com.vmware.vim25.RetrieveResult;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.DynamicProperty;
import com.vmware.vim25.ArrayOfPerfCounterInfo;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.xml.ws.soap.SOAPFaultException;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.base.Preconditions.checkNotNull;
/**
 * <h1>Background Information</h1>
 * <p>
 * Useful information about on VI SDK performance counters.
 * </p>
 * <p>
 * A performance counter is represented by:
 * </p>
 * <p>
 * <code>[group].[counter].[rollUpType]</code>
 * </p>
 * <p>
 * example of which would be:
 * </p>
 * <p>
 * <code>disk.usage.average</code>
 * </p>
 * <p>
 * Performance counters have seven predefined groups:
 * </p>
 * <ul>
 * <li>CPU</li>
 * <li>ResCPU</li>
 * <li>Memory</li>
 * <li>Network</li>
 * <li>Disk</li>
 * <li>System</li>
 * <li>Cluster Services</li>
 * </ul>
 * <p>
 * Along with the groups there are six rollup types:
 * </p>
 * <ul>
 * <li>average</li>
 * <li>latest</li>
 * <li>maximum</li>
 * <li>minimum</li>
 * <li>none</li>
 * <li>summation</li>
 * </ul>
 * 
 * <h1>Managed Objects and Metrics collected by {@link VMwarePerfPoller}</h1>
 * The list of managed objects and their associated metrics follow.
 * 
 * <h2>ESXi host</h2>
 * VMWare's <code>HostSystem</code> managed object represents the physical compute
 * resources with the metrics to be collected as follows:
 * <p>
 * <ul>
 * <li><code>cpu.usage.average</code></li>
 * <li><code>cpu.usage.minimum</code></li>
 * <li><code>cpu.idle.?</code></li>
 * <li><code>memory.active.?</code></li>
 * <li><code>memory.consumed.?</code></li>
 * <li><code>memory.swapused.?</code></li>
 * </ul>
 * </p>
 * <h2>Virtual Machine</h2>
 * VMWare's <code>Virtual Machine</code> managed object is to have
 * the metrics collect from each instance as follows:
 * <ul>
 * <li><code>cpu.usage.average</code></li>
 * <li><code>cpu.usage.maximum</code></li>
 * <li><code>memory.active.?</code></li>
 * <li><code>memory.consumed.?</code></li>
 * <li><code>disk.read.average.?</code></li>
 * <li><code>disk.write.average.?</code></li>
 * </ul>
 *
 * <h2>Data Store</h2>
 * VMWare's <code>Datastore</code> managed object is to have
 * the metrics collect from each instance as follows:
 * <p>
 * <ul>
 * <li><code>disk.capacity.?</code></li>
 * <li><code>disk.provisioned.?</code></li>
 * <li><code>disk.used.?</code></li>
 * </ul>
 * </p>
 *
 */

public class VMwarePerfPoller implements Runnable, MetricSet {

    private static final Logger LOG = LoggerFactory.getLogger(VMwarePerfPoller.class);
    private static final Joiner COMMA_JOINER = Joiner.on(", ").skipNulls();

    private final Connection client;
    private final Map<String, com.boundary.metrics.vmware.client.client.metrics.Metric> metrics;
    private final AtomicBoolean lock = new AtomicBoolean(false);
    private final String orgId;
    private final MetricsClient metricsClient;
    private final MeterManagerClient meterManagerClient;

    private final Timer pollTimer = new Timer();
    private final Meter overrunMeter = new Meter();

    private Map<String, Integer> countersIdMap;
    private Map<Integer, PerfCounterInfo> countersInfoMap;
    private DateTime lastPoll;
    private Duration skew;

    public VMwarePerfPoller(Connection client, Map<String, com.boundary.metrics.vmware.client.client.metrics.Metric> metrics, String orgId,
                            MetricsClient metricsClient, MeterManagerClient meterManagerClient) {
        this.client = checkNotNull(client);
        this.metrics = checkNotNull(metrics);
        this.meterManagerClient = meterManagerClient;
        this.orgId = orgId;
        this.metricsClient = metricsClient;
    }

    /**
     * This is the main processing function that handles fetching the performance counters
     * from the end point. This function is called
     */
    public void run() {
    	// The lock is used in case the sampling of the metrics takes longer than the poll interval.
    	// If during collection cycle with thread A is in progress and another thread B tries to collect metrics
    	// then thread B fails to get the lock and skips collecting metrics
        if (lock.compareAndSet(false, true)) {
            final Timer.Context timer = pollTimer.time();
            try {
                if (!client.isConnected()) {
                    client.connect();
                }
                if (countersIdMap == null || countersInfoMap == null) {
                    fetchAvailableMetrics();
                }
                fetchPerfData();
            } catch (Throwable e) {
                LOG.error("Encountered unexpected error whilst polling for performance data", e);
            } finally {
                lock.set(false);
                timer.stop();
            }
        } else {
            LOG.warn("Poll of {} already in progress, skipping", client.getHost());
            overrunMeter.mark();
        }
    }

    /**
     * Performance counters are likely to differ between versions of VMware products but shouldn't change for the host
     * during the lifetime of polling, so we can safely cache them.
     * 
     * @throws InvalidPropertyFaultMsg thrown if an kind of property error
     * @throws RuntimeFaultFaultMsg thrown if any kind of runtime error
     */
    public void fetchAvailableMetrics() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        ImmutableMap.Builder<String, Integer> countersIdMap = ImmutableMap.builder();
        ImmutableMap.Builder<Integer, PerfCounterInfo> countersInfoMap = ImmutableMap.builder();

        // Get the PerformanceManager object which is used to get metrics from counters
        ManagedObjectReference pm = client.getServiceContent().getPerfManager();

        ObjectSpec oSpec = new ObjectSpec();
        oSpec.setObj(pm);

        PropertySpec pSpec = new PropertySpec();
        pSpec.setType("PerformanceManager");
        pSpec.getPathSet().add("perfCounter");

        PropertyFilterSpec fSpec = new PropertyFilterSpec();
        fSpec.getObjectSet().add(oSpec);
        fSpec.getPropSet().add(pSpec);

        RetrieveOptions ro = new RetrieveOptions();
        RetrieveResult retrieveResult = client.getVimPort().retrievePropertiesEx(client.getServiceContent().getPropertyCollector(), ImmutableList.of(fSpec), ro);

        for (ObjectContent oc : retrieveResult.getObjects()) {
            if (oc.getPropSet() != null) {
                for (DynamicProperty dp : oc.getPropSet()) {
                    List<PerfCounterInfo> perfCounters = ((ArrayOfPerfCounterInfo)dp.getVal()).getPerfCounterInfo();
                    if (perfCounters != null) {
                        for (PerfCounterInfo perfCounter : perfCounters) {
                            int counterId = perfCounter.getKey();
                            countersIdMap.put(toFullName(perfCounter), counterId);
                            countersInfoMap.put(counterId, perfCounter);
                        }
                    }
                }
            }
        }

        this.countersIdMap = countersIdMap.build();
        this.countersInfoMap = countersInfoMap.build();

        for (String counterName : metrics.keySet()) {
            if (this.countersIdMap.containsKey(counterName)) {
                // Ensure metric is created in HLM
                String vUnit = this.countersInfoMap.get(this.countersIdMap.get(counterName)).getUnitInfo().getKey();
                String hlmUnit = "number";
                if (vUnit.equalsIgnoreCase("kiloBytes")) {
                    hlmUnit = "bytecount";
                } else if (vUnit.equalsIgnoreCase("percent")) {
                    hlmUnit = "percent";
                }
                metricsClient.createMetric(metrics.get(counterName).getName(), hlmUnit);
            } else {
                LOG.warn("Server does not have {} metric, skipping", counterName);
            }
        }
        LOG.info("Found {} metrics on VMware host {}: {}",
        		this.countersIdMap.size(), client.getHost(),client.getName());
        for (String counter: this.countersIdMap.keySet()) {
        	LOG.info("counter: {}", counter);
        }
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
    public void fetchPerfData() throws MalformedURLException, RemoteException,
            InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, SOAPFaultException {
    	
        ManagedObjectReference root = client.getServiceContent().getRootFolder();

        // 'now' according to the server
        DateTime now = TimeUtils.toDateTime(client.getVimPort().currentTime(client.getServiceInstanceReference()));
        Duration serverSkew = new Duration(now, new DateTime());
        if (serverSkew.isLongerThan(Duration.standardSeconds(1)) &&
                (skew == null || skew.getStandardSeconds() != serverSkew.getStandardSeconds())) {
            LOG.warn("Server {} and local time skewed by {} seconds", client.getHost(), serverSkew.getStandardSeconds());
            skew = serverSkew;
        }
        if (lastPoll == null) {
            lastPoll = now.minusSeconds(20);
        }

        // Holder for all our newly founded measurements
        // TODO set an upper size limit on measurements list
        List<Measurement> measurements = Lists.newArrayList();

        /*
        * Create the list of PerfMetricIds, one for each counter.
        */
        List<PerfMetricId> perfMetricIds = Lists.newArrayList();
        for (String counterName : metrics.keySet()) {
            if (countersIdMap.containsKey(counterName)) {
                PerfMetricId metricId = new PerfMetricId();
                /* Get the ID for this counter. */
                metricId.setCounterId(countersIdMap.get(counterName));
                metricId.setInstance("*");
                perfMetricIds.add(metricId);
            }
        }

        GetMOREF getMOREFs = new GetMOREF(client);
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
            querySpec.setFormat("normal"); // normal or csv
            querySpec.setStartTime(TimeUtils.toXMLGregorianCalendar(lastPoll));
            querySpec.setEndTime(TimeUtils.toXMLGregorianCalendar(now));
            querySpec.getMetricId().addAll(perfMetricIds);

            LOG.info("Entity: {}, MOR: {}-{}, Interval: {}, Format: {}, MetricIds: {}, Start: {}, End: {}", entityName,
                    mor.getType(), mor.getValue(), querySpec.getIntervalId(), querySpec.getFormat(),
                    FluentIterable.from(perfMetricIds).transform(toStringFunction), lastPoll, now);

            List<PerfEntityMetricBase> retrievedStats = client.getVimPort().queryPerf(client.getServiceContent().getPerfManager(), ImmutableList.of(querySpec));

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
                        PerfCounterInfo metricInfo = countersInfoMap.get(metricReading.getId().getCounterId());
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
                            int obsDomainId = meterManagerClient.createOrGetMeterMetadata(orgId, client.getName() + "-" + entityName).getObservationDomainId();

                            if (metricInfo.getUnitInfo().getKey().equalsIgnoreCase("kiloBytes")) {
                                sampleValue = (long)sampleValue * 1024; // Convert KB to Bytes
                            } else if (metricInfo.getUnitInfo().getKey().equalsIgnoreCase("percent")) {
                                // Convert hundredth of a percent to a decimal percent
                                sampleValue = new Long((long)sampleValue).doubleValue() / 10000.0;
                            }
                            String name = metrics.get(metricFullName).getName();
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
                                    metricFullName, client.getHost(), lastPoll, now);
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
            metricsClient.addMeasurements(measurements);
        } else {
            LOG.warn("No measurements collected in last poll for {}", client.getHost());
        }

        // Reset lastPoll time
        lastPoll = now;
    }


    private static final Function<PerfMetricId, String> toStringFunction = new Function<PerfMetricId, String>() {
        @Nullable
        @Override
        public String apply(@Nullable PerfMetricId input) {
            return input == null ? null : String.format("CounterID: %s, InstanceId: %s", input.getCounterId(), input.getInstance());
        }
    };

    private static String toFullName(PerfCounterInfo perfCounterInfo) {
        return toFullName.apply(perfCounterInfo);
    }

    private static final Function<PerfCounterInfo, String> toFullName = new Function<PerfCounterInfo, String>() {
        @Nullable
        @Override
        public String apply(@Nullable PerfCounterInfo input) {
            return input == null ? null : String.format("%s.%s.%s", input.getGroupInfo().getKey(),
                    input.getNameInfo().getKey(), input.getRollupType().toString().toUpperCase());
        }
    };

    @Override
    public Map<String, Metric> getMetrics() {
        return ImmutableMap.of(
                MetricRegistry.name(getClass(), "poll-timer", client.getHost()), (Metric)pollTimer,
                MetricRegistry.name(getClass(), "overrun-meter", client.getHost()), overrunMeter
                );
    }
}
