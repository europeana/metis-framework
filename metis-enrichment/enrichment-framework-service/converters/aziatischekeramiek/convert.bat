set ANNOCULTOR_HOME=%CD%
set MAVEN_OPTS=-Xmx1024m
rem Uncomment this if class com.sun.tools.javac.Main is not found
rem set ANNOCULTOR_TOOLS_JAR=%JAVA_HOME%/jre/lib/tools.jar
mvn exec:exec -Dexec.workingDir="%ANNOCULTOR_HOME%" -Dexec.args="-cp %%classpath %MAVEN_OPTS% eu.annocultor.xconverter.impl.Converter profile/profile.xml work"
mvn exec:exec -Dexec.workingDir="%ANNOCULTOR_HOME%" -Dexec.args="-cp %%classpath %MAVEN_OPTS% eu.annocultor.reports.ReportPresenter"
