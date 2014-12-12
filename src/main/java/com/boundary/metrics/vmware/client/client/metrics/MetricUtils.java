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

package com.boundary.metrics.vmware.client.client.metrics;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.List;

public class MetricUtils {

    private static final Logger LOG = LoggerFactory.getLogger(MetricUtils.class);

    private MetricUtils() { /* static class */ }

    public static String normalizeMetricName(String name) {
        return name.replace(' ', '_').replace('.', '_').toUpperCase();
        // TODO add more normalization based on only 0-9, A-Z, and _ which cannot lead
    }

    public static List<Object> toBulkEntry(@Nullable Measurement m) {
        return measurementToBulkEntry.apply(m);
    }

    public static Function<Measurement, List<Object>> toBulkEntry() {
        return measurementToBulkEntry;
    }

    private static final Function<Measurement, List<Object>> measurementToBulkEntry = new Function<Measurement, List<Object>>() {
        @Nullable
        @Override
        public List<Object> apply(@Nullable Measurement input) {
            if (input == null) {
                LOG.warn("Tried to convert null measurement to bulk entry");
                return null;
            } else {
                List<Object> bulkEntry = ImmutableList.<Object>of(String.valueOf(input.getSourceId()), input.getMetric(),
                        input.getMeasurement(), input.getTimestamp().getMillis());
                if (bulkEntry.size() != 4) {
                    LOG.error("Bulk entry invalid: {}", bulkEntry);
                }
                return bulkEntry;
            }
        }
    };

}
