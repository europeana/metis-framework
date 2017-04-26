set ANNOCULTOR_HOME=.
set MAVEN_OPTS=-Xmx7324m
call mvn clean compile

echo ==== 2: Construct list of terms, (indirect) children of the top terms, that we need to convert ====
mvn exec:exec -Dexec.workingdir="%ANNOCULTOR_HOME%" -Dexec.args="-cp %%classpath %MAVEN_OPTS% eu.annocultor.converters.time.OntologyToHtmlGenerator top-terms.txt time.historical.rdf time.periods.rdf time.years.rdf time.parent.rdf
