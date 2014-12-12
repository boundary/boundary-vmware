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

/**
 * Main class that drives the VMWare integration.
 */
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
        
        // The meter manager client is responsible for interacting with the meter API to create new nodes so that
        // metrics can be be displayed.
        final MeterManagerClient meterManagerClient = configuration.getMeterManagerClient().build(httpClient);
        
        // The metrics client is responsible interacting with the HLM (Host Level Metrics) API
        final MetricsClient metricsClient = configuration.getMetricsClient().build(httpClient);
        environment.jersey().register(new VMWarePerfPollerMonitor());

        // Each of the MonitoredEntity's represent and end point where we can collect metrics from since the VMWare Infrastructure SDK/API
        // is symetric with respect connection to vCenter or ESXi server.
        for (MonitoredEntity entity : configuration.getMonitoredEntities()) {
        	// For each monitored entity we create a client and poller, and then pass to our scheduler
        	// to be processed by individual threads at the polling interval
            Connection connection = new VMwareClient(entity.getUri(), entity.getUsername(), entity.getPassword());
            VMwarePerfPoller poller = new VMwarePerfPoller(connection, entity.getMetrics(), configuration.getOrgId(), metricsClient, meterManagerClient);
            scheduler.scheduleAtFixedRate(poller, 0, 20, TimeUnit.SECONDS);
            environment.metrics().registerAll(poller);
        }
    }
}
