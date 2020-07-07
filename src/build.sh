#!/bin/sh
# This should be executable.
echo
echo "xml-net Build System"
echo "----------------"
echo
#JAVA_HOME=/usr/java/latest

if [ "$JAVA_HOME" = "" ] ; then
  echo "ERROR: JAVA_HOME not found in your environment."
  echo
  echo "Please, set the JAVA_HOME variable in your environment to match the"
  echo "location of the Java Virtual Machine you want to use."
 export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/jre 
fi

LOCALCLASSPATH=$JAVA_HOME/lib/tools.jar:$JAVA_HOME/lib/classes.zip:./lib/comm.jar:./lib/Serialio.jar:./lib/xerces.jar:./lib/ant.jar:./lib/w3c.jar:./lib
ANT_HOME=./lib

echo Building with classpath $LOCALCLASSPATH:$CLASSPATH
echo

echo Starting Ant...
echo

#$JAVA_HOME/bin/java -Dant.home=$ANT_HOME -classpath "$LOCALCLASSPATH:$CLASSPATH" org.apache.tools.ant.Main $*
/usr/bin/ant $*