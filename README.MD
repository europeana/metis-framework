[![CI](https://github.com/europeana/metis-framework/actions/workflows/ci.yml/badge.svg)](https://github.com/europeana/metis-framework/actions/workflows/ci.yml) [![Coverage](https://sonarcloud.io/api/project_badges/measure?project=europeana_metis-framework&metric=coverage)](https://sonarcloud.io/summary/new_code?id=europeana_metis-framework)

[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=europeana_metis-framework&metric=bugs)](https://sonarcloud.io/summary/new_code?id=europeana_metis-framework) [![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=europeana_metis-framework&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=europeana_metis-framework)  
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=europeana_metis-framework&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=europeana_metis-framework) [![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=europeana_metis-framework&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=europeana_metis-framework)  
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=europeana_metis-framework&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=europeana_metis-framework) [![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=europeana_metis-framework&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=europeana_metis-framework)  
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=europeana_metis-framework&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=europeana_metis-framework)

## Metis framework services repository

This repository contains the code developed as part of Europeana Metis.  
The software is developed on Java and uses Maven as its dependency management system.

## Spring boot

Some applications in this repository have been migrated to spring boot and dockerized.  
These application support both environment variables and spring boot .properties files.

### Spring configuration .properties

ATTENTION:   
When running the spring boot application we should, ideally, NOT create `application.properties` inside the project so
that the project can be easily re-cloned and to also avoid accidental submission of configuration into version control.

The suggested alternative is to apply the configuration with an environment variable pointing to the appropriate
.properties file
`SPRING_CONFIG_LOCATION=file:///data/metis-configuration/metis-framework/metis-authentication/metis-authentication-rest/k8s/overlays/local/resources/application.properties`
It can be applied in Intellij from the:
> Edit configurations -> Modify Options -> Environment Variables -> Add the SPRING_CONFIG_LOCATION variable

If the spring application is to be run command line and we don't want to setup the environment variable beforehand,
then an argument can be set up during execution of the command e.g. `mvn spring-boot:run -Dspring.config.location=`.
Spring variables can be overwritten using environment variable, for example if local docker dbs are used we can
overwrite the
hosts(That can happen if we use Spring boot through the IDE instead of deploying in docker/minikube):
> HIBERNATE_CONNECTION_URL: jdbc:postgresql://localhost:5432/metis-authentication_local

### Environment variables

Alternatively an environment variables file can be created.  
Spring boot accepts environment variables that can be created using the same names as in the `.properties` file,
with their names upper-cased and punctuation characters replaced with "_".  
For example a property named:  
`hibernate.c3p0.min_size`  
Would then become an environment variable named:  
`HIBERNATE_C3P0_MIN_SIZE`

#### Environment variables file with Intellij

- Install the env file plugin in intellij:  
  [envfile plugin](https://plugins.jetbrains.com/plugin/7861-envfile)  
  This is to assist updating the intellij run configurations to add the env file, instead of manually(or with a script)
  creating the environment variables inside the configuration.  
  As before the environment variables file should exist externally of the project.

It can be applied in Intellij from the:
> Edit configurations -> Enable envFile checkbox -> Add a new(Enabled) entry below with the path to the environment
> variable

## Using Docker

The projects contain a simple `Dockerfile` to run the application though docker.  
They also contain a `docker-compose.yml` with the supported local containers.

### Intellij Docker

Intellij also supports Docker through its
[docker plugin](https://plugins.jetbrains.com/plugin/7724-docker?_ga=2.175319586.1865269423.1669020836-1878326318.1658382865&_gl=1%2A19i8cor%2A_ga%2AMTg3ODMyNjMxOC4xNjU4MzgyODY1%2A_ga_9J976DJZ68%2AMTY2OTAyMDgzNS4yNS4wLjE2NjkwMjA4MzUuMC4wLjA)  
To apply the files that are required for the application to be deployed we need to do the following:
> Edit configuration -> Select or create a `Dockerfile` configuration
> - Fill in the Dockerfile
> - Fill in the Image tag
> - Fill in the Container name
> - Fill in the Run options:  
    `-p 8080:8080`    
    `-v /data/metis-configuration/metis-framework/metis-authentication/metis-authentication-rest/k8s/overlays/local/resources/custom-truststore.jks:/data/certificates/custom-truststore.jks`      
    `-v /data/metis-configuration/metis-framework/metis-authentication/metis-authentication-rest/k8s/overlays/local/resources/application.properties:/application.properties`      
    `-v /data/metis-configuration/metis-framework/metis-authentication/metis-authentication-rest/k8s/overlays/local/resources/log4j2.xml:/data/logging/log4j2.xml`  
    They should point to the custom certificate file, the application properties and the log4j2.xml configuration file.

### Intellij Docker Compose

Intellij supports Docker Compose as well.  
To apply that we need to do the following:
> Edit configuration -> Select or create a `Docker-compose` configuration
> - Fill in the Compose files
> - At this point the volumes that need to be applied are inserted inside the `docker-compose.yml` file,  
    since they do not work similarly to the docker configuration with Run options.    
    In the `docker-compose.yml` file there is also an option(commented out) to apply an `env_file`
    if environment variables file is required instead.

### General docker commands

Building the image from the current directory:
> docker build -t <imageName> .

Verifying the image creation:
> docker images <imageName>

Running a container:
> docker run --name=<containerName> -p 8080:8080 <imageName>

Checking running container:
> docker container ls

Entering the container:
> docker exec -ti <containerName> /bin/sh

#### <u>Cleaning:</u>

Removing all exited containers:
> docker rm $(docker ps -a -f status=exited -q)

Removing all images:
> docker rmi $(docker images -a -q)

#### <u>Inspection(used for validating docker image contents):</u>

Create container without starting it:
> docker create --name suspect-container suspect-image:latest

Create a `.tar` from the filesystem of the container.
> docker export suspect-container > suspect-container.tar

Now the tarball can be inspected.
