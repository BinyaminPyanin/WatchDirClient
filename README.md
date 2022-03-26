# Watch Directory Client

## Description

Java (client) program that monitors a directory. When a new Java properties file appears in the monitored directory, it
should process it as follows:

1) Read the file into a Map
2) Apply a regular expression pattern filter for the keys (i.e., remove key/value mappings where keys do not match a
   configurable regular expression pattern).
3) Relay the filtered mappings to a server program
4) Delete the file

### Tech stack/Best practices implemented

- [SpringBoot ```2.6.4```](https://spring.io/projects/spring-boot)
- Java 1.8
- [Maven wrapper](https://github.com/takari/maven-wrapper)
- JUnit ```5.8.1```

## Running locally

The project includes a Maven wrapper ```mvnw```. So no build tool needs to be installed.

Make sure ```WatchDirServer``` is up and running

To build run:

```./mvnw clean install```

To run JUnit:

``` ./mvnw test```

To start client(s) run:

```java -cp target/WatchDirClient-1.0.0.jar com.aw.client.DirMonitorClient <client_config_properties_file_location_path>```

For example:

Client 1:

```client-config1.properties``` located in ```./src/main/resources/client1```

```java -cp target/WatchDirClient-1.0.0.jar com.aw.client.DirMonitorClient ./src/main/resources/client1```

In new terminal run Client 2:

```client-config2.properties``` located in ```./src/main/resources/client2```

```java -cp target/WatchDirClient-1.0.0.jar com.aw.client.DirMonitorClient ./src/main/resources/client2```

Let's assume ```/tmp/watchdir1``` directory watched by Client1 

and

```/tmp/watchdir2``` directory watched by Client2

and

```/tmp/watchdir/output``` directory is an output directory for the Server

Copy ```resources/test/test1.properties``` to ```/tmp/watchdir1```

Copy ```resources/test/test2.properties``` to ```/tmp/watchdir2```

Expected result :

```/tmp/watchdir/output``` contains both filtered 

```test1.properties``` and ```test2.properties```

and ```/tmp/watchdir1``` and ```/tmp/watchdir2``` are empty


To stop client(s):

```CTRL + c```

## Update maven dependencies to the latest version

- To update maven dependencies , run:

```./mvnw versions:display-dependency-updates```

## Have fun :-)

