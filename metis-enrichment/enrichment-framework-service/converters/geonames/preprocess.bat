set MAVEN_OPTS=-Xmx1324m
mvn exec:exec -Dexec.args="-cp %%classpath %MAVEN_OPTS% eu.annocultor.converters.geonames.GeonamesDumpToRdf"
