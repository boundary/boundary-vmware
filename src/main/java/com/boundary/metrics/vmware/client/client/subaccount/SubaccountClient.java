package com.boundary.metrics.vmware.client.client.subaccount;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.sun.jersey.api.client.*;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.core.util.Base64;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import java.net.URI;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class SubaccountClient {

    private final WebResource baseResource;
    private static final Joiner PATH_JOINER = Joiner.on('/');

    public SubaccountClient(Client client, URI baseUrl, String apiKey) {
        checkNotNull(client);
        checkNotNull(baseUrl);
        checkArgument(!Strings.isNullOrEmpty(apiKey));
        final String authorization = "Basic " + new String(Base64.encode(apiKey + ":"), Charsets.US_ASCII);
        client.addFilter(new ClientFilter() {
            @Override
            public ClientResponse handle(ClientRequest cr) throws ClientHandlerException {
                final MultivaluedMap<String, Object> headers = cr.getHeaders();
                if (!headers.containsKey(HttpHeaders.AUTHORIZATION)) {
                    headers.add(HttpHeaders.AUTHORIZATION, authorization);
                }
                return getNext().handle(cr);
            }
        });
        this.baseResource = client.resource(baseUrl);
    }

    public String getMetricCredentials(String orgId) {
        SubaccountInfo subaccountInfo = baseResource
                .path(PATH_JOINER.join(orgId, "subaccount"))
                .get(SubaccountInfo.class);
        return subaccountInfo.getCredentials();
        // TODO error handling
    }
}
