#!/bin/bash

commd=$1

source env.sh

#
# ./build.sh
#
if [ -z $commd ]
then
  echo compiling Java ...
  javac -g -classpath $JARS:./java:. bitshift/registrar/*.java
fi

#
# ./build.sh clean
#
if [ "$commd" = "clean" ]
then
  echo cleaning...
  rm bitshift/registrar/*.class
fi
