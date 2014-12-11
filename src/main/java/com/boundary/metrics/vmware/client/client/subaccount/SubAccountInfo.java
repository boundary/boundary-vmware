package com.boundary.metrics.vmware.client.client.subaccount;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SubaccountInfo {

    @JsonProperty
    @NotEmpty
    private String email;

    @JsonProperty
    @NotEmpty
    private String apiToken;

    public String getCredentials() {
        return email + ":" + apiToken;
    }
}
