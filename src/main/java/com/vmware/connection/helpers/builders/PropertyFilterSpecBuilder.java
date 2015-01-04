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

package com.vmware.connection.helpers.builders;

import com.vmware.vim25.ObjectSpec;
import com.vmware.vim25.PropertyFilterSpec;
import com.vmware.vim25.PropertySpec;

import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 */
public class PropertyFilterSpecBuilder extends PropertyFilterSpec {
    private void init() {
        if (propSet == null) {
            propSet = new ArrayList<>();
        }
        if(objectSet == null) {
            objectSet = new ArrayList<>();
        }
    }

    public PropertyFilterSpecBuilder reportMissingObjectsInResults(final Boolean value) {
        this.setReportMissingObjectsInResults(value);
        return this;
    }

    public PropertyFilterSpecBuilder propSet(final PropertySpec... propertySpecs) {
        init();
        this.propSet.addAll(Arrays.asList(propertySpecs));
        return this;
    }

    public PropertyFilterSpecBuilder objectSet(final ObjectSpec... objectSpecs) {
        init();
        this.objectSet.addAll(Arrays.asList(objectSpecs));
        return this;
    }
}
