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

import java.util.List;

/**
 * Entry that contains the type of a {@link ManagedObjectReference} and the
 * list of associated performance counters that are to be collected.
 *
 */
public class MORCatalogEntry {

	private String type;
	private List<PerformanceCounterEntry> counters;
	
	public MORCatalogEntry() {
		
	}

	/**
	 * Returns the type of {@link ManagedObjectReference}
	 * 
	 * @return {@link String}
	 */
	public String getType() {
		return type;
	}

	/**
	 * Returns the performance counter names for the type of
	 * {@link ManagedObjectReference}
	 * 
	 * @return
	 */
	public List<PerformanceCounterEntry> getCounters() {
		return counters;
	}

}
