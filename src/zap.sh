#!/usr/bin/env bash

# Extract and check the Java version
JAVA_OUTPUT=$(java -version 2>&1)

# Catch warning: Unable to find a $JAVA_HOME at "/usr", continuing with system-provided Java
if [ "`echo ${JAVA_OUTPUT} | grep "continuing with system-provided Java"`" ] ; then
	echo "WARNING, \$JAVA_HOME could be set incorrectly, Java's error is:"
	echo "    " $JAVA_OUTPUT
	echo "Unsetting JAVA_HOME and continuing with ZAP start-up"
	unset JAVA_HOME
	JAVA_OUTPUT=$(java -version 2>&1)
fi

JAVA_VERSION=$(echo ${JAVA_OUTPUT} | awk -F\" 'NR == 1 { print $2 }')

JAVA_MAJOR_VERSION=${JAVA_VERSION%%.*}
JAVA_MINOR_VERSION=$(echo $JAVA_VERSION | awk -F\. '{ print $2 }')

if [ $JAVA_MAJOR_VERSION -ge 1 ] && [ $JAVA_MINOR_VERSION -ge 7 ]; then
	echo "Found Java version $JAVA_VERSION"
else
	echo "Exiting: ZAP requires a minimum of Java 7 to run, found $JAVA_VERSION"
	exit 1
fi

#Dereference from link to the real directory
SCRIPTNAME="$0"

#While name of this script is symbolic link
while [ -L "${SCRIPTNAME}" ] ; do
	cd "`dirname "${SCRIPTNAME}"`" > /dev/null
	SCRIPTNAME="$(readlink "`basename "${SCRIPTNAME}"`")"
done
cd "`dirname "${SCRIPTNAME}"`" > /dev/null

#Base directory where ZAP is installed
BASEDIR="`pwd -P`"

#Switch to the directory where ZAP is installed
cd "$BASEDIR"

#Get Operating System
OS=$(uname -s)

#Work out best memory options
if [ "$OS" = "Linux" ]; then
	MEM=$(expr $(sed -n 's/MemTotal:[ ]\{1,\}\([0-9]\{1,\}\) kB/\1/p' /proc/meminfo) / 1024)
elif [ "$OS" = "Darwin" ]; then
	MEM=$(system_profiler SPMemoryDataType | sed -n -e 's/.*Size: \([0-9]\{1,\}\) GB/\1/p' | awk '{s+=$0} END {print s*1024}')
fi

if [ -z $MEM ]
then
	echo "Failed to obtain current memory, using jmv default memory settings"
else
	echo "Available memory: " $MEM "MB"
	if [ $MEM -gt 1500 ]
	then
		JMEM="-Xmx512m"
	else
		if [ $MEM -gt 900 ]
		then
			JMEM="-Xmx256m"
		else
			if [ $MEM -gt 512 ]
			then
				JMEM="-Xmx128m"
			fi
		fi
	fi
fi

if [ -n "$JMEM" ]
then
	echo "Setting jvm heap size: $JMEM"
fi

#Start ZAP

exec java ${JMEM} -XX:PermSize=256M -jar "${BASEDIR}/zap.jar" $*

