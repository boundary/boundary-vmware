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

import java.net.URI;
import java.util.Map;

import javax.annotation.concurrent.Immutable;

import org.hibernate.validator.constraints.NotEmpty;

import com.boundary.metrics.vmware.client.metrics.Metric;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;

/**
 * Represents the information needed to get metrics from a vCenter or ESXi server
 *
 */
@Immutable
public class MonitoredEntity {

    private final URI uri;
    private final String username;
    private final String password;
    private final String name;
    
    /**
     * Map of VMware performance counter full names to Boundary metric descriptions
     */
    private final Map<String, Metric> vmMetrics;

    /**
     * Constructor
     * 
     * @param uri URI to vCenter or ESXi server
     * @param username name used to authenticate
     * @param password password used to authenticate
     * @param name Identifier of the monitored entity
     */
    public MonitoredEntity(@JsonProperty("uri") URI uri,
                           @JsonProperty("username") String username,
                           @JsonProperty("password") String password,
                           @JsonProperty("name") String name) {
    	
    	// Ensure that the user/password/uri are not null since they
    	// are required to connect to the monitored entity
        this.username = checkNotNull(username);
        this.password = checkNotNull(password);
        this.uri = checkNotNull(uri);
        this.name = checkNotNull(name);

        // Created a map of the permformance counters we require to collect from
        // the monitored entity
        // TODO: Externalize the configuration of these counters to allow collection to be
        // changed dynamically.
        ImmutableMap.Builder<String,Metric> virtualMachineMetrics = ImmutableMap.builder();
        virtualMachineMetrics.put("cpu.usage.AVERAGE",
        		new Metric("SYSTEM_CPU_USAGE_AVERAGE","CPU Average Utilization"));
        virtualMachineMetrics.put("cpu.usage.MINIMUM",
        		new Metric("SYSTEM_CPU_USAGE_MINIMUM","CPU Minimum Utilization"));
        virtualMachineMetrics.put("cpu.idle.SUMMATION",
        		new Metric("SYSTEM_CPU_IDLE_TOTAL","CPU Total Idle"));
        virtualMachineMetrics.put("mem.active.MAXIMUM",
        		new Metric("SYSTEM_MEMORY_ACTIVE_MAXIMUM","Memory Maximum Active"));
        virtualMachineMetrics.put("mem.consumed.AVERAGE",
        		new Metric("SYSTEM_MEMORY_CONSUMED_AVERAGE","Memory Average Consumed"));
        virtualMachineMetrics.put("mem.swapused.MAXIMUM",
        		new Metric("SYSTEM_MEMORY_SWAP_USED_MAXIMUM", "Memory Swap Used Maximum"));
        virtualMachineMetrics.put("disk.read.AVERAGE",
        		new Metric("SYSTEM_DISK_READ_AVERAGE", "Disk Read Average"));
        virtualMachineMetrics.put("disk.write.AVERAGE",
        		new Metric("SYSTEM_DISK_WRITE_AVERAGE", "Disk Write Average"));
        
        vmMetrics = virtualMachineMetrics.build();
    }

    /**
     * Returns the URI endpoint associated with this monitored entity
     * 
     * @return {@link URI}
     */
    public URI getUri() {
        return uri;
    }

    /**
     * Returns the user name associated with the monitored entity
     * @return {@link String}
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the password used to authenticate to the monitored entity
     * 
     * @return {@link String}
     */
    public String getPassword() {
        return password;
    }

    /**
     * Returns the metrics associatd with this monitored entity
     * @return {@link Map}
     */
    public Map<String, Metric> getMetrics() {
        return vmMetrics;
    }
    
    /**
     * Returns the name of the metric client
     * 
     * @return {@link String}
     */
	public String getName() {
		return name;
	}
}
