export ANNOCULTOR_HOME=`pwd`
export MAVEN_OPTS=-Xmx3024m
/usr/local/java/bin/java -Dfile.encoding=UTF8 -cp /usr/local/tomcat/.m2/repository/annocultor/annocultor/2.3.4/annocultor-2.3.4.jar $MAVEN_OPTS eu.annocultor.converters.europeana.EuropeanaSolrDocumentTagger http://dashboard.europeana.sara.nl/solr/ingestion/ http://dashboard.europeana.sara.nl/solr/enrichment/ *:* $1

