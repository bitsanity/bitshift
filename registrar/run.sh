#!/bin/bash

source env.sh

echo "ghost\n" | java $JLIB -cp $JARS:.:./java \
 bitshift.registrar.WebServer $REGISTRARPORT $SCA $TOK \
 > log.out 2>&1
