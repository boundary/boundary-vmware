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

/**
 * Contains a performance counter(<em>name</em>) and
 * metric identifier (<em>metric</em>). Used to map vSphere's
 * performance counter to a Boundary metric.
 *
 */
public class PerformanceCounterEntry {
	
	private String name;
	private String metric;

	/**
	 * Return the performance counter name.
	 * 
	 * @return {@link String}
	 */
	public String getName() {
		return name;
	}

	/**
	 * Return the metric identifier
	 * @return {@link String}
	 */
	public String getMetric() {
		return metric;
	}
}
