set ANNOCULTOR_HOME=.
set MAVEN_OPTS=-Xmx1324m
set EDM_CLASS=Concept
set EDM_COLLECTION=Gemet
copy profile\files-concepts.xml profile\copy-of-files.xml
call mvn install
mvn exec:exec -Dexec.workingdir="%ANNOCULTOR_HOME%" -Dexec.args="-cp %%classpath %MAVEN_OPTS% eu.annocultor.xconverter.impl.Converter profile\\profile-edm.xml tmp/work"
