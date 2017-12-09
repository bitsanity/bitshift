#!/bin/bash

source env.sh

java $JLIB -cp $JARS:.:./java bitshift.registrar.WebServer 8000
