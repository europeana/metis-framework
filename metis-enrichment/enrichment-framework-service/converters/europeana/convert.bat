set ANNOCULTOR_HOME=.
set MAVEN_OPTS=-Xmx5324m
call mvn install
mvn exec:exec -Dexec.workingdir="%ANNOCULTOR_HOME%" -Dexec.args="-cp %%classpath %MAVEN_OPTS% eu.annocultor.xconverter.impl.Converter profile\\profile-%1.xml tmp/work"
