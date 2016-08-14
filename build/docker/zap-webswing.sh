#!/bin/sh
#
# Startup script for the Webswing
# 
# Customised for ZAP running in Docker - just call with no parameters for webswing to start and leave
# the docker container running
#
# Set environment.
export HOME=/zap/webswing-2.3/
export OPTS="-h 0.0.0.0 -j $HOME/jetty.properties -u $HOME/user.properties -c $HOME/webswing.config"
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/
export JAVA_OPTS="-Xmx128M"
export LOG=$HOME/webswing.out
export PID_PATH_NAME=$HOME/webswing.pid

if [ -z `command -v $0` ]; then 
    CURRENTDIR=`pwd`
    cd `dirname $0` > /dev/null
    SCRIPTPATH=`pwd`/
    cd $CURRENTDIR
else
    SCRIPTPATH="" 
fi

if [ ! -f $HOME/webswing-server.war ]; then
    echo "Webswing executable not found in $HOME folder" 
    exit 1
fi

if [ ! -f $JAVA_HOME/bin/java ]; then
    echo "Java installation not found in $JAVA_HOME folder" 
    exit 1
fi
if [ -z `command -v xvfb-run` ]; then
    echo "Unable to locate xvfb-run command. Please install Xvfb before starting Webswing." 
    exit 1
fi
if [ ! -z `command -v ldconfig` ]; then
    if [ `ldconfig -p | grep -i libXext | wc -l` -lt 1 ]; then 
        echo "Missing dependent library libXext."
        exit 1
    fi
    if [ `ldconfig -p | grep -i libxi | wc -l` -lt 1 ]; then
        echo "Missing dependent library libXi."
        exit 1
    fi
    if [ `ldconfig -p | grep -i libxtst | wc -l` -lt 1 ]; then
        echo "Missing dependent library libXtst"
        exit 1
    fi
    if [ `ldconfig -p | grep -i libxrender | wc -l` -lt 1 ]; then
        echo "Missing dependent library libXrender."
        exit 1
    fi
fi

# See how we were called - customised for ZAP running in Docker
case "$1" in
    run)
        # Run Webswing server- expects X Server to be running
        # dont put into the background otherwise docker will exit
        $JAVA_HOME/bin/java $JAVA_OPTS -jar $HOME/webswing-server.war $OPTS 2>> $LOG >> $LOG
        ;;
    *)
        xvfb-run $SCRIPTPATH$0 run
esac

exit 0