#!/bin/bash

source env.sh

java $JLIB -cp $JARS:.:./java bitshift.sweeper.Sweeper $1
