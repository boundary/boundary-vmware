boundary-vmware
===============

Boundary Enterprise Integration with VMWare. The integration connects to one or more
ESXi servers or VCenter to collect metrics from the physical and virtual servers running on the VMWare infrastructure.


[![Build Status](https://travis-ci.org/boundary/boundary-vmware.svg)](https://travis-ci.org/boundary/boundary-vmware)

Prerequisites
----------
- Git version 2.2.1 or later
- Java JDK 1.7 or later
- Maven 3.2.1 or later

Building
--------
1. Clone the Github repository:
    ```bash
    $ git clone https://github.com/boundary/boundary-vmware.git
     ```
     
2. Run the following maven command:
     ```bash
    $ mvn install
     ```
     
3. After the build runs successfully you are able to run the integration.

Configuration
-------------
Configuration consists of:
- A [YAML](http://en.wikipedia.org/wiki/YAML) configuration file which is used for the general configuration of the application. (You can use the example.yml file in the distribution as a starting point, which is located in the sub-directory `src/main/resources/example.yml`.
- A [JSON](http://en.wikipedia.org/wiki/JSON) configuration file which contains information on which metrics to collect from which kind of managed objects. A default coniguration is located in `src/main/resources/collection-catalog.json`.

### General Configuration

```yaml
# Boundary Integration to VMWare configuration
server:
  minThreads: 1
  applicationConnectors:
    - type: http
      acceptorThreads: 1
      selectorThreads: 1
client:
  minThreads: 4
  maxThreads: 4
  timeout: 10s
  connectionTimeout: 10s
  retries: 3
  gzipEnabledForRequests: false
metricsClient:
  baseUri: <Boundary API host e.g. for premimum-api.boundary.com>
  apiKey: <Email and API token e.g. someuser@mydomain.com:api.a6df22c660-2105>
# Logging settings.
logging:
  # The default level of all loggers. Can be OFF, ERROR, WARN, INFO, DEBUG, TRACE, or ALL.
  level: INFO
  # Logger-specific levels.
  loggers:
    # Overrides the level of com.example.dw.<package name or class> 
    # and sets it to DEBUG.
    #"org.apache.http.wire": DEBUG
    com.boundary: TRACE
monitoredEntities:
    # The following can configuration can be repeated for other instances
    - uri: https://<esx host or vcenter host>/sdk/vimService
      username: <user>
      password: <password>
      name: <name>
      catalog: <path to catalog file>
```

### Collection Configuration
The configuration file consists of two sections:
- `definitions` - This section contains the Boundary metric definitions
- `catalog` - This section consists of the vSphere managed object types and the performance counters to collect. The Boundary metric name is paired with the performance counter to be collected. Every boundary metric name must have a corresponding entry in the `definitions` for the performance metric to be displayed in a dashboard.

An excerpt of the default configuration file is shown below.

```json
{
	"definitions":
	[
		{
			"defaultAggregate": "AVG",
			"defaultResolutionMS": 20000,
			"description": "CPU Average Utilization",
			"displayName": "CPU Average Utilization",
			"displayNameShort": "CPU Avg Util",
			"metric": "VMWARE_SYSTEM_CPU_USAGE_AVERAGE",
			"unit": "PERCENT"
		}
	],

	"catalog":
	[
		{
			"type": "VirtualMachine",
			"counters":
			[
				{
					"name": "cpu.usage.average",
					"metric": "VMWARE_SYSTEM_CPU_USAGE_AVERAGE"
				},

				{
					"name": "cpu.usage.MAXIMUM",
					"metric": "VMWARE_SYSTEM_CPU_USAGE_MAXIMUM"
				}
			]
		},

		{
			"type": "HostSystem",
			"counters":
			[
				{
					"name": "cpu.usage.AVERAGE",
					"metric": "VMWARE_SYSTEM_CPU_USAGE_AVERAGE"
				}
			]
		},

		{
			"type": "Datastore",
			"counters":
			[
				{
					"name": "disk.capacity.SUM",
					"metric": "VMWARE_SYSTEM_DISK_CAPACITY_SUM"
				}
			]
		}
	]
}

```

Starting
--------

1. To start the integration run the following:
```
$ java -jar target/vmware-metrics-collector-1.0.0.jar server src/main/resources/example.yml
```

Stopping
--------

1. To stop the integration run control-c in the shell where the integration was started.


Reference
---------
[Technical Note On Performance Counters](http://www.vmware.com/files/pdf/technote_PerformanceCounters.pdf)

