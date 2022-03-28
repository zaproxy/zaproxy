#!/bin/bash
# Script for testing python library dependencies

RES=0

echo "Check aws cli - should output version"
aws --version
RES=$?

echo "Check zap-cli - should output help"
zap-cli --help
if [ "$RES" -eq 0 ]
then
  RES=$?
fi

exit $RES
