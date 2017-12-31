#!/bin/bash

# ===============================
#
# Expected Usage:
#  $ sudo -H -u bitshift run.sh
#
# ===============================

source ./env.src

geth --light --rinkeby --rpc --rpcapi='db,eth,net,web3,personal' --verbosity 2 >geth.out 2>&1 &

pushd tokenbuyer
./run.sh &
popd

pushd registrar
./run.sh &
popd
