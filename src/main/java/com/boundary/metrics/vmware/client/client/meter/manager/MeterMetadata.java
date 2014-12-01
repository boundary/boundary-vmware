package com.boundary.metrics.vmware.client.client.meter.manager;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MeterMetadata {

    @JsonProperty
    @NotEmpty
    private String id;

    @JsonProperty
    @NotEmpty
    private String name;

    @JsonProperty("obs_domain_id")
    @NotNull
    private Integer observationDomainId;

    @JsonProperty
    @NotEmpty
    private String orgId;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getObservationDomainId() {
        return observationDomainId;
    }

    public String getOrgId() {
        return orgId;
    }
}
