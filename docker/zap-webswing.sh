#!/bin/sh
#
# Startup script for the Webswing
#
# Customised for ZAP running in Docker - just call with no parameters for webswing to start and leave
# the docker container running
#
# Set environment.
export HOME=/zap/webswing
export OPTS="-h 0.0.0.0 -j $HOME/jetty.properties -c $HOME/webswing.config"
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
        cd $HOME
        
        # Set up the ZAP runtime options
        ZAP_OPTS="-host 0.0.0.0 -port 8090"
        ZAP_PUBLIC="/zap/wrk/owasp_zap_root_ca.cer"
        ZAP_PRIVATE="/zap/wrk/owasp_zap_root_ca.key"

        if [ ! -z "${ZAP_WEBSWING_OPTS}" ]; then
          # Replace them with those set in the env var
          ZAP_OPTS="${ZAP_WEBSWING_OPTS}"
        elif [ -f ${ZAP_PRIVATE} ]; then
          # Private cert is available, use that
          ZAP_OPTS="${ZAP_OPTS} -certload ${ZAP_PRIVATE}"
        elif [ -w /zap/wrk ]; then
          # wrk directory is writable, output public and private certs
          ZAP_OPTS="${ZAP_OPTS} -certpubdump  ${ZAP_PUBLIC} -certfulldump  ${ZAP_PRIVATE}"
        fi
        
        echo "Using ZAP command line options: ${ZAP_OPTS}"
        # Use ; for sed separators so we can use the directory slashes
        sed -i "s;ZAP_OPTS;${ZAP_OPTS};" webswing.config
        
        $JAVA_HOME/bin/java $JAVA_OPTS -jar webswing-server.war $OPTS 2>> $LOG >> $LOG
        ;;
    *)
        xvfb-run $SCRIPTPATH$0 run
esac

exit 0
