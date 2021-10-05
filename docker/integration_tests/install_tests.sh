#!/bin/bash
# Script for testing installing add-ons
# Attempts to install all ZAP add-ons and should then exit
# Will fail if this takes longer than the specified number of minutes

timeout 10m /zap/zap.sh -addoninstallall -cmd

exit $?
