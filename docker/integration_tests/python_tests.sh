#!/bin/bash
# Script for testing python library dependencies

RES=0

echo "Check aws cli - should output version"
aws --version
RES=$?

exit $RES
