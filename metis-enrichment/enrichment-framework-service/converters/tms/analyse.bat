set ANNOCULTOR_HOME=%CD%
set ANNOCULTOR_ARGS=%1 %2 %3 %4 %5 %6 %7 %8
set ANNOCULTOR_TMP=../tmp
set MAVEN_OPTS=-Xmx1024m
mvn exec:exec -Dexec.workingDir="%ANNOCULTOR_HOME%" -Dexec.args="-cp %%classpath %MAVEN_OPTS% eu.annocultor.xconverter.impl.Analyser"

