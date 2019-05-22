#!/usr/bin/env bash

# cd into the Java directory of the .app file
cd "$( dirname "${BASH_SOURCE[0]}" )"
cd ../Java

# execute zap.sh, passing through the command line arguments
exec ./zap.sh "$@"
