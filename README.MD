[![Build Status](https://travis-ci.org/europeana/metis-framework.svg?branch=develop)](https://travis-ci.org/europeana/metis-framework)
[![Coverage Status](https://coveralls.io/repos/github/europeana/metis-framework/badge.svg?branch=develop)](https://coveralls.io/github/europeana/metis-framework?branch=develop)

**Metis Services repository**
This repository contains the code developed as part of Europeana Metis and Pandora.
The software is developed on OpenJDK8 and uses Maven3 as its dependency management system.
Each module is usually organized in the following manner:

 * *-client: Implements a Java REST client that implements the REST API for each given service
 * *-common: Includes the model of the module along with module-wide utils
 * *-service: Includes persistence connections (databases/caches) and the main functionality of each module
 * *-rest: Includes a REST API for the services. _This is bound to change during the integration with Europeana Cloud_

The REST APIs have been tested to work on Apache Tomcat 8.
