echo "Usage convert vocabulary parameters, e.g. convert AAT"
set ANNOCULTOR_HOME=.

rem set ANNOCULTOR_HOME=%USERPROFILE%/europeana/vocabularies
rem current dir, Java misinterprets backslash returned by %CD%

set ANNOCULTOR_ARGS="%2 %3 %4"
set ANNOCULTOR_TOOLS_JAR=%JAVA_HOME%/jre/lib/tools.jar
mvn exec:exec -Dexec.workingdir="%ANNOCULTOR_HOME%" -Dexec.args="-cp %%classpath %MAVEN_OPTS% eu.annocultor.xconverter.impl.Converter profile\\profile-%1.xml tmp/work"
