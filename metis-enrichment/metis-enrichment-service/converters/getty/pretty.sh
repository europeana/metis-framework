export ANNOCULTOR_HOME=`pwd`
export MAVEN_OPTS=-Xmx1024m
export ANNOCULTOR_ARGS="$1 $2 $3 $4"
echo $ANNOCULTOR_ARGS
mvn exec:exec -Dexec.workingDir="$ANNOCULTOR_HOME" -Dexec.args="-cp %classpath $MAVEN_OPTS eu.annocultor.xconverter.impl.PrettyPrinter"
