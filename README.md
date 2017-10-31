# Predictive

A project to evaluate Predictive analysis on Bonita BPM process execution.

## Run the project
gradlew bootRun

## Switch Database configuration
There are 2 configurations available:
 * an embedded in-memory H2 database with a test dataset loaded at runtime  
 * an Oracle server datasource (requires access to the server)  
To choose which configuration to use, edit the application.properties file and change the spring.profiles.active property value.  
Set spring.profiles.active=h2 to use H2 in-memory database (and its test dataset).  
Set spring.profiles.active=oracle to use the datasource to an existing Oracle server available on the network (requires access to the server).  
