# Predictive

A project to evaluate Predictive analysis on Bonita BPM process execution.

## Run the project
gradlew bootRun

## Switch Database configuration
There are 2 configuration available:
 * an embedded in-memory H2 database with a test dataset loaded at runtime
 * an Oracle server datasource (requires access to the server)
To choose which configuration to use, edit the Application.java and change the ConfigurationProperties declaration.
Set @ConfigurationProperties("h2") to use H2 in-memory database (and its test dataset).
Set @ConfigurationProperties("oracle") to use the datasource to an existing Oracle server available on the network (requires access to the server).