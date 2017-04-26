echo "Convert original rdf dump of Geonames to separate RDF files"
export MAVEN_OPTS=-Xmx1024m
mvn exec:exec -Dexec.args="-cp %classpath $MAVEN_OPTS eu.annocultor.converters.geonames.GeonamesDumpToRdf"
