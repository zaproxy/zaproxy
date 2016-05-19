#!/bin/sh
export DISPLAY=:1.0
Xvfb :1 -screen 0 1024x768x16 -ac &
/zap/zap.sh $@
