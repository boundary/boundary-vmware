package com.boundary.metrics.vmware;

import com.boundary.metrics.vmware.client.client.meter.manager.MeterManagerClient;
import com.boundary.metrics.vmware.client.client.metrics.MetricsClient;
import com.boundary.metrics.vmware.poller.MonitoredEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.jersey.api.client.Client;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.List;

public class VMwarePerfAdapterConfiguration extends Configuration {

    static class MeterManagerConfiguration {
        @NotNull
        @JsonProperty
        private URI baseUri = URI.create("https://api.boundary.com");

        @NotEmpty
        @JsonProperty
        private String apiKey;

        public URI getBaseUri() { return baseUri; }

        public String getApiKey() {
            return apiKey;
        }

        public MeterManagerClient build(Client httpClient) {
            return new MeterManagerClient(httpClient, getBaseUri(), getApiKey());
        }
    }

    static class MetricClientConfiguration {
        @NotNull
        @JsonProperty
        private URI baseUri = URI.create("https://metrics-api.boundary.com");

        public URI getBaseUri() { return baseUri; }

        @JsonProperty
        @NotEmpty
        private String apiKey;

        public String getApiKey() {
            return apiKey;
        }

        public MetricsClient build(Client httpClient) {
            return new MetricsClient(httpClient, getBaseUri(), apiKey);
        }
    }

    @JsonProperty
    @Valid
    @NotNull
    private List<MonitoredEntity> monitoredEntities;

    public List<MonitoredEntity> getMonitoredEntities() {
        return monitoredEntities;
    }

    @Valid
    @NotNull
    @JsonProperty
    private JerseyClientConfiguration client = new JerseyClientConfiguration();

    public JerseyClientConfiguration getClient() { return client; }

    @JsonProperty
    @Valid
    @NotNull
    private MeterManagerConfiguration meterManagerClient = new MeterManagerConfiguration();

    public MeterManagerConfiguration getMeterManagerClient() {
        return meterManagerClient;
    }

    @JsonProperty
    @Valid
    @NotNull
    private MetricClientConfiguration metricsClient = new MetricClientConfiguration();

    public MetricClientConfiguration getMetricsClient() {
        return metricsClient;
    }

    @JsonProperty
    @NotEmpty
    private String orgId;

    public String getOrgId() {
        return orgId;
    }
}
