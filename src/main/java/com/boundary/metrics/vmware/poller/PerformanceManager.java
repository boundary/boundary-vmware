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
package com.boundary.metrics.vmware.poller;

import com.vmware.connection.Connection;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.PerformanceDescription;

/**
 * Wrapper interface to vSphare Performance Manager managed object
 * @author davidg
 *
 */
public class PerformanceManager {
	

	private Connection client;
	
	ManagedObjectReference performanceManager = null;

	PerformanceManager(Connection client) {
		this.client = client;
		performanceManager = client.getServiceContent().getPerfManager();
	}
	
	public PerformanceDescription getDescription() {
		PerformanceDescription desc = new PerformanceDescription();
		
		return desc;
	}

}
