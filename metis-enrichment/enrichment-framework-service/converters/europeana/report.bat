set ANNOCULTOR_HOME=.
set MAVEN_OPTS=-Xmx1324m
set ANNOCULTOR_ARGS="%2 %3 %4"
set ANNOCULTOR_TOOLS_JAR=%JAVA_HOME%/jre/lib/tools.jar
mvn exec:exec -Dexec.workingDir="%ANNOCULTOR_HOME%" -Dexec.args="-cp %%classpath %MAVEN_OPTS% eu.annocultor.reports.ReportPresenter"
