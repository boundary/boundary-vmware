package com.boundary.metrics.vmware.poller;

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;
import java.util.Map;

import javax.annotation.concurrent.Immutable;

import org.hibernate.validator.constraints.NotEmpty;

import com.boundary.metrics.vmware.client.client.metrics.Metric;
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
    private final Map<String, Metric> metrics;

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
        ImmutableMap.Builder<String, Metric> mb = ImmutableMap.builder();
        mb.put("disk.provisioned.NONE", new Metric("SYSTEM_FS_FREE_TOTAL", "Filesystem Free"));
        mb.put("mem.consumed.NONE", new Metric("SYSTEM_MEM_USED", "Memory Used"));
        mb.put("power.power.NONE", new Metric("Power Consumption"));
        mb.put("cpu.usage.NONE", new Metric("SYSTEM_CPU_TOTAL", "CPU Utilization"));
        mb.put("mem.granted.NONE", new Metric("SYSTEM_MEM_TOTAL", "Memory Available to OS"));
//        mb.put("net.droppedRx.SUMMATION", new Metric("SYSTEM_NET_ERRORS_RX_DROPPED_TOTAL", "Network Receive Packets Dropped"));
//        mb.put("net.droppedTx.SUMMATION", new Metric("SYSTEM_NET_ERRORS_TX_DROPPED_TOTAL", "Network Transmit Packets Dropped"));
//        mb.put("net.received.AVERAGE", new Metric("NETRB", "Network Inbound"));
//        mb.put("net.transmitted.AVERAGE", new Metric("NETWB", "Network Outbound"));
        mb.put("disk.read.AVERAGE", new Metric("SYSTEM_DISK_READ_BYTES_TOTAL", "Disk Bytes Read"));
        mb.put("disk.write.AVERAGE", new Metric("SYSTEM_DISK_WRITE_BYTES_TOTAL", "Disk Bytes Written"));

        metrics = mb.build();
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
        return metrics;
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
