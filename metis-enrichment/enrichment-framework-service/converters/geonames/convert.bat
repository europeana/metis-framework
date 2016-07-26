set ANNOCULTOR_HOME=.
set ANNOCULTOR_TOOLS_JAR=%JAVA_HOME%/jre/lib/tools.jar
mvn exec:exec -Dexec.workingdir="%ANNOCULTOR_HOME%" -Dexec.args="-cp %%classpath %MAVEN_OPTS% eu.annocultor.xconverter.impl.Converter profile\\profile-geonames.xml tmp/work"
mvn exec:exec -Dexec.workingDir="%ANNOCULTOR_HOME%" -Dexec.args="-cp %%classpath %MAVEN_OPTS% eu.annocultor.reports.ReportPresenter"
