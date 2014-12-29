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

package com.boundary.metrics.vmware.client.client.meter.manager;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.sun.jersey.api.client.*;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.core.util.Base64;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.net.URI;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class MeterManagerClient {

    private final WebResource baseResource;
    private final String orgId;
    private static final Joiner PATH_JOINER = Joiner.on('/');

    // TODO change into guava cache
    private final Map<String, MeterMetadata> hostnameCache = Maps.newConcurrentMap();

    public MeterManagerClient(Client client, URI baseUrl,String orgId,String apiKey) {
        checkNotNull(client);
        checkNotNull(baseUrl);
        checkArgument(!Strings.isNullOrEmpty(apiKey));
        checkArgument(!Strings.isNullOrEmpty(orgId));
        this.orgId = orgId;
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

    /**
     * Create a meter with given hostname for org
     * @param hostname hostname of meter, has to be unique
     * @return meter id
     * @throws MeterNameConflictException if name already in use
     */
    public String createMeter(String hostname) throws MeterNameConflictException {
        ImmutableMap<String,String> params = ImmutableMap.of("name", hostname);
        final ClientResponse response = baseResource.path(PATH_JOINER.join(this.orgId, "meters"))
                .entity(params, MediaType.APPLICATION_JSON_TYPE)
                .post(ClientResponse.class);
        if (ClientResponse.Status.CONFLICT.equals(response.getStatusInfo())) {
            throw new MeterNameConflictException(hostname);
        }
        final String location = response.getLocation().getPath();
        response.close();
        // The meter id is the last component of the path
        final int slashIndex = location.lastIndexOf('/');
        if (slashIndex < 0) {
            throw new IllegalStateException("Invalid response");
        }
        return location.substring(slashIndex + 1);
    }

    public Optional<MeterMetadata> getMeterMetadataById(String meterId) {
        return Optional.fromNullable(baseResource.path(PATH_JOINER.join(this.orgId, "meters", meterId)).get(MeterMetadata.class));
    }

    public Optional<MeterMetadata> getMeterMetadataByName(String meterName) {
        MeterMetadata[] meters = baseResource.path(PATH_JOINER.join(this.orgId, "meters")).queryParam("name", meterName).get(MeterMetadata[].class);
        if (meters.length == 1) {
            return Optional.of(meters[0]);
        } else {
            return Optional.absent(); // TODO handle > 1 meter returned
        }
    }

    public MeterMetadata createOrGetMeterMetadata(String meterName) {
        MeterMetadata meterMetadata = hostnameCache.get(orgId + "-" + meterName);
        if (meterMetadata != null) {
            return meterMetadata;
        }
        try {
            String meterId = createMeter(meterName);
            meterMetadata = getMeterMetadataById(meterId).get();
        } catch (MeterNameConflictException e) {
            meterMetadata = getMeterMetadataByName(meterName).get();
        }

        hostnameCache.put(orgId + "-" + meterName, meterMetadata);
        return meterMetadata;
    }

}
