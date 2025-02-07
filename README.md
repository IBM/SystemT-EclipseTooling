# AQL Developer Tooling

This repository contains the AQL Developer Tooling as maintained and enhanced in Watson, starting 2018.

SystemT and AQL Training Materials: [SystemT wiki](https://w3-connections.ibm.com/wikis/home?lang=en-us#!/wiki/SystemT%20Group%20Public%20Wiki/page/SystemT%20Quick%20Start)

For support and reporting issues on AQL Developer Tooling: 
- Post a message in IBM Watson Slack channel: [#systemt-user](https://ibm-watson.slack.com/messages/C2GSHCA58)
- Open an issue in this repository

## Build

``cd EclipseTooling``

``mvn clean install``

## Installing the AQL Developer Tooling

Installation instructions for the latest release: [link](https://w3-connections.ibm.com/wikis/home?lang=en-us#!/wiki/SystemT%20Group%20Public%20Wiki/page/AQL%20Developer%20Tooling%20(Eclipse-based)%20Download)

## Testing the AQL Developer Tooling prior to release

1. Prerequisite: Eclipse Oxygen (older Eclipse versions are also OK)
2. In Eclipse, Help > Install New Software > Add > Archive > point to EclipseTooling/com.ibm.biginsights.updateSite/target/com.ibm.biginsights.updateSite-<version>.zip and follow the installation steps
3. Follow the steps outlined in [EclipseTooling/test-resources/README.md](https://github.ibm.com/SystemT-Research/eclipseTooling/tree/master/EclipseTooling/test-resources)
