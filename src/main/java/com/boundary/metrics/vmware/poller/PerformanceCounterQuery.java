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

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.boundary.metrics.vmware.client.metrics.Measurement;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.RuntimeFaultFaultMsg;

public class PerformanceCounterQuery {
	
    private static final Logger LOG = LoggerFactory.getLogger(PerformanceCounterQuery.class);
	
	private VMwareClient vmwareClient;
	private VMWareMetadata metadata;

	PerformanceCounterQuery(VMwareClient vmwareClient, VMWareMetadata metadata) {
        this.vmwareClient = checkNotNull(vmwareClient);
        this.metadata = metadata;
	}
	
	List<Measurement> queryCounters(ManagedObjectReference mor,DateTime start,DateTime end) throws RuntimeFaultFaultMsg {
		// Holder for all our newly found measurements
        List<Measurement> measurements = null;
        String entityName = mor.getValue();

        // Prefix the VM name with the name from the monitored entity
		// configuration, we can form unique names that way
		String source = vmwareClient.getName() + "-" + entityName;
		LOG.info("Get measurements for \"{}\"",source);
        measurements = vmwareClient.getMeasurements(mor,entityName,source,new Integer(20),start,end,metadata);
        
        return measurements;
	}

}
