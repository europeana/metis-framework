echo "Usage: sh convert.sh"
export ANNOCULTOR_HOME=`pwd`
# /Users/borys/europeana/vocabularies
# export PROFILEDIR=`pwd`
export MAVEN_OPTS=-Xmx4024m
export ANNOCULTOR_ARGS="$2 $3 $4"
echo "$ANNOCULTOR_HOME"
mvn clean install
mvn exec:exec -Dexec.workingdir="$ANNOCULTOR_HOME" -Dexec.args="-cp %classpath $MAVEN_OPTS eu.annocultor.xconverter.impl.Converter profile/profile-time.xml tmp/work"
mvn exec:exec -Dexec.workingdir="$ANNOCULTOR_HOME" -Dexec.args="-cp %classpath $MAVEN_OPTS eu.annocultor.reports.ReportPresenter"
