export MAVEN_OPTS=-Xmx2024m
rm -f alternateNames.zip
rm -f allCountries.zip
wget http://download.geonames.org/export/dump/alternateNames.zip
wget http://download.geonames.org/export/dump/allCountries.zip
unzip alternateNames.zip
unzip allCountries.zip
mv -f allCountries.txt input_source/
mv -f alternateNames.txt input_source/ 
mv -f iso-languagecodes.txt input_source/
mvn clean install
mvn exec:exec -Dexec.args="-cp %classpath $MAVEN_OPTS eu.annocultor.converters.geonames.GeonamesCsvToRdf"