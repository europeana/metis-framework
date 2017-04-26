set ANNOCULTOR_HOME=%CD%
set ANNOCULTOR_ARGS=%1 %2 %3 %4 %5
set MAVEN_OPTS=-Xmx1024m
mvn -X exec:exec -Dexec.workingDir="%ANNOCULTOR_HOME%" -Dexec.args="-cp %%classpath %MAVEN_OPTS% eu.annocultor.xconverter.impl.PrettyPrinter"

