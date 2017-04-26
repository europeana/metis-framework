echo "If something breaks then the stack trace can be copied to file log.txt by running convert.bat > log.txt"
set ANNOCULTOR_HOME=.
mvn exec:exec -Dexec.workingDir="%ANNOCULTOR_HOME%" -Dexec.args="-cp %%classpath %MAVEN_OPTS% eu.annocultor.xconverter.impl.Converter profile\\profile-definitions.xml tmp/work"
