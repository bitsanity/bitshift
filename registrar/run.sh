#!/bin/bash

source env.sh

echo "<passphrase>" | java $JLIB -cp $JARS:.:./java \
 bitshift.registrar.WebServer $REGISTRARPORT $SCA $TOK \
 > log.out 2>&1 &
