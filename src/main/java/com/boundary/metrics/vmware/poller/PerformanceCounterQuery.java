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
	private VMWareMetadata metadata;

	PerformanceCounterQuery(VMwareClient vmwareClient,
			MeterManagerClient meterClient,
			VMWareMetadata metadata) {
        this.vmwareClient = checkNotNull(vmwareClient);
        this.meterClient = checkNotNull(meterClient);
        this.metadata = metadata;
	}
	
	List<Measurement> queryCounters(ManagedObjectReference mor,DateTime start,DateTime end) throws RuntimeFaultFaultMsg {
		// Holder for all our newly found measurements
        List<Measurement> measurements = null;
        String entityName = mor.getValue();

        // Prefix the VM name with the name from the monitored entity
		// configuration, we can form unique names that way
		String meterName = vmwareClient.getName() + "-" + entityName;
		int obsDomainId = meterClient.createOrGetMeterMetadata(meterName).getObservationDomainId();
		
        measurements = vmwareClient.getMeasurements(mor,entityName,obsDomainId,new Integer(20),start,end,metadata);
        
        return measurements;
	}

}
