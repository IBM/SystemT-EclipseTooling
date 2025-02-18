# AQL Developer Tooling

This repository contains the AQL Developer Tooling as maintained and enhanced in Watson, starting 2018.

SystemT and AQL Training Materials: [SystemT wiki](https://github.com/IBM/SystemT/blob/main/docs/index.md)

For support and reporting issues on AQL Developer Tooling: 
- Open an issue in this repository

## Build

``cd EclipseTooling``

``mvn clean install -f EclipseTooling/pom.xml -s build/maven-settings.xml``

## Installing the AQL Developer Tooling

## Testing the AQL Developer Tooling prior to release

1. Prerequisite: Eclipse Oxygen (older Eclipse versions are also OK)
2. In Eclipse, Help > Install New Software > Add > Archive > point to EclipseTooling/com.ibm.biginsights.updateSite/target/com.ibm.biginsights.updateSite-<version>.zip and follow the installation steps
3. Follow the steps outlined in [EclipseTooling/test-resources/README.md](https://github.com/IBM/SystemT-EclipseTooling/tree/main/EclipseTooling/test-resources)
