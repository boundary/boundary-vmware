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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.boundary.metrics.vmware.client.client.meter.manager.MeterManagerClient;
import com.boundary.metrics.vmware.client.metrics.Measurement;
import com.boundary.metrics.vmware.client.metrics.Metric;
import com.boundary.metrics.vmware.util.TimeUtils;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.PerfCounterInfo;
import com.vmware.vim25.PerfEntityMetric;
import com.vmware.vim25.PerfEntityMetricBase;
import com.vmware.vim25.PerfMetricIntSeries;
import com.vmware.vim25.PerfMetricSeries;
import com.vmware.vim25.PerfQuerySpec;
import com.vmware.vim25.PerfSampleInfo;
import com.vmware.vim25.RuntimeFaultFaultMsg;

public class PerformanceCounterQuery {
	
    private static final Logger LOG = LoggerFactory.getLogger(PerformanceCounterQuery.class);
	
	private VMwareClient vmwareClient;
	private MeterManagerClient meterClient;
	private PerformanceCounterMetadata metadata;
	
	private DateTime lastPoll;



	PerformanceCounterQuery(VMwareClient vmwareClient,
			MeterManagerClient meterClient,
			PerformanceCounterMetadata metadata) {
        this.vmwareClient = checkNotNull(vmwareClient);
        this.meterClient = checkNotNull(meterClient);
        this.metadata = metadata;
	}
	
	List<Measurement> queryCounters(ManagedObjectReference mor) throws RuntimeFaultFaultMsg {
		// Holder for all our newly found measurements
        List<Measurement> measurements = Lists.newArrayList();
        String entityName = mor.getValue();
        
        DateTime now = vmwareClient.getTimeAtEndPoint();

        if (lastPoll == null) {
            lastPoll = now.minusSeconds(20);
        }
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
         querySpec.getMetricId().addAll(metadata.getPerformanceMetricIds());

         LOG.info("Entity: {}, MOR: {}-{}, Interval: {}, Format: {}, MetricIds: {}, Start: {}, End: {}", mor,
                 mor.getType(), mor.getValue(), querySpec.getIntervalId(), querySpec.getFormat(),
                 FluentIterable.from(metadata.getPerformanceMetricIds()).transform(PerformanceCounterMetadata.toStringFunction), lastPoll, now);

         List<PerfEntityMetricBase> retrievedStats = vmwareClient.getVimPort().queryPerf(vmwareClient.getServiceContent().getPerfManager(), ImmutableList.of(querySpec));
	
         Map<Integer, PerfCounterInfo> infoMap = metadata.getInfoMap();
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
                      PerfCounterInfo metricInfo = infoMap.get(metricReading.getId().getCounterId());
                      String metricFullName = PerformanceCounterMetadata.toFullName(metricInfo);
                      if (!sampleInfos.isEmpty()) {
                          PerfSampleInfo sampleInfo = sampleInfos.get(0);
                          DateTime sampleTime = TimeUtils.toDateTime(sampleInfo.getTimestamp());
                          Number sampleValue = metricReading.getValue().iterator().next();

//                          if (skew != null) {
//                              sampleTime = sampleTime.plusSeconds((int)skew.getStandardSeconds());
//                          }

                          if (metricReading.getValue().size() > 1) {
                              LOG.warn("Metric {} has more than one value, only using the first", metricFullName);
                          }

                          // Prefix the VM name with the name from the monitored entity configuration, we can form unique names that way
                          int obsDomainId = meterClient.createOrGetMeterMetadata(vmwareClient.getName() + "-" + entityName).getObservationDomainId();

                          if (metricInfo.getUnitInfo().getKey().equalsIgnoreCase("kiloBytes")) {
                              sampleValue = (long)sampleValue * 1024; // Convert KB to Bytes
                          } else if (metricInfo.getUnitInfo().getKey().equalsIgnoreCase("percent")) {
                              // Convert hundredth of a percent to a decimal percent
                              sampleValue = new Long((long)sampleValue).doubleValue() / 10000.0;
                          }
                          String name = metadata.getMetrics().get(metricFullName).getName();
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
                                  metricFullName, vmwareClient.getName(), lastPoll, now);
                      }
                  }
              } else {
                  LOG.error("Unrecognized performance entry type received: {}, ignoring",
                          singleEntityPerfStats.getClass().getName());
              }
          }
         
         return measurements;
	}

}
