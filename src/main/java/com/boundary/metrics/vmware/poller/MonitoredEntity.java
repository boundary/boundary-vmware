package com.boundary.metrics.vmware.poller;

import com.boundary.metrics.vmware.client.client.metrics.Metric;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;

import javax.annotation.concurrent.Immutable;
import java.net.URI;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public class MonitoredEntity {

    private final URI uri;
    private final String username;
    private final String password;
    /**
     * Map of VMware performance counter full names to Boundary metric descriptions
     */
    private final Map<String, Metric> metrics;

    public MonitoredEntity(@JsonProperty("uri") URI uri,
                           @JsonProperty("username") String username,
                           @JsonProperty("password") String password) {
        this.username = checkNotNull(username);
        this.password = checkNotNull(password);
        this.uri = checkNotNull(uri);

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

    public URI getUri() {
        return uri;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Map<String, Metric> getMetrics() {
        return metrics;
    }

}
