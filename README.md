boundary-vmware
===============

Boundary Enterprise Integration with VMWare. The integration connects to one or more
ESXi servers or VCenter to collect metrics from the physical and virtual servers running on the VMWare infrastructure.

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
The integration requires a single [YAML](http://en.wikipedia.org/wiki/YAML) configuration file. You can use the example.yml file in the distribution as a starting point, which is located in the sub-directory `src/main/resources/example.yml`.

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
meterManagerClient:
  apiKey: <boundary enterprise api key>
  baseUri: https://api.boundary.com/
metricsClient:
  baseUri: https://metrics-api.boundary.com/
  apiKey: <boundary enterprise metrics api key>
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
orgId:
monitoredEntities:
    # The following can configuration can be repeated for other instances
    - uri: https://<esx host or vcenter host>/sdk/vimService
      username: <user>
      password: <password>
```

Starting
--------

1. To star the integration run the following:
```
$ java -jar target/vmware-metrics-collector-1.0.0.jar server src/main/resources/example.yml
```

Stopping
--------

1. To stop the integration run control-c in the shell where the integration was started.
