package com.boundary.metrics.vmware;

import com.boundary.metrics.vmware.client.client.meter.manager.MeterManagerClient;
import com.boundary.metrics.vmware.client.client.metrics.MetricsClient;
import com.boundary.metrics.vmware.poller.MonitoredEntity;
import com.boundary.metrics.vmware.poller.VMwareClient;
import com.boundary.metrics.vmware.poller.VMwarePerfPoller;
import com.boundary.metrics.vmware.resource.VMWarePerfPollerMonitor;
import com.sun.jersey.api.client.Client;
import com.vmware.connection.Connection;
import io.dropwizard.Application;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class VMwarePerfAdapter extends Application<VMwarePerfAdapterConfiguration> {

    public static void main(String[] args) throws Exception {
        new VMwarePerfAdapter().run(args);
    }

    @Override
    public String getName() {
        return "VMware-Poller";
    }

    @Override
    public void initialize(Bootstrap<VMwarePerfAdapterConfiguration> bootstrap) {

    }

    @Override
    public void run(VMwarePerfAdapterConfiguration configuration, Environment environment) throws Exception {
        final ScheduledExecutorService scheduler = environment.lifecycle().scheduledExecutorService("vmware-poller-%d")
                .threads(Runtime.getRuntime().availableProcessors())
                .build();
        final Client httpClient = new JerseyClientBuilder(environment)
                .using(configuration.getClient())
                .build("http-client");
        final MeterManagerClient meterManagerClient = configuration.getMeterManagerClient().build(httpClient);
        final MetricsClient metricsClient = configuration.getMetricsClient().build(httpClient);
        environment.jersey().register(new VMWarePerfPollerMonitor());

        for (MonitoredEntity entity : configuration.getMonitoredEntities()) {
            Connection connection = new VMwareClient(entity.getUri(), entity.getUsername(), entity.getPassword());
            VMwarePerfPoller poller = new VMwarePerfPoller(connection, entity.getMetrics(), configuration.getOrgId(), metricsClient, meterManagerClient);
            scheduler.scheduleAtFixedRate(poller, 0, 20, TimeUnit.SECONDS);
            environment.metrics().registerAll(poller);
        }
    }
}
