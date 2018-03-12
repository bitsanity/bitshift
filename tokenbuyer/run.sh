#!/bin/bash

source ../env.src

node tokenbuyer $TOKBUYERPORT >> log.out 2>&1 &
