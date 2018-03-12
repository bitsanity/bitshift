#!/bin/bash

commd=$1

source env.sh

#
# ./build.sh
#
if [ -z $commd ]
then
  echo compiling Java ...
  javac -g -Xlint:deprecation -classpath $JARS:./java:. bitshift/sweeper/*.java
fi

#
# ./build.sh clean
#
if [ "$commd" = "clean" ]
then
  echo cleaning...
  rm bitshift/sweeper/*.class
fi
