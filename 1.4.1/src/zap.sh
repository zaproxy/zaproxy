#!/usr/bin/env sh

#Dereference from link to the real directory
SCRIPTNAME=$0

#While name of this script is symbolic link
while [ -L "$SCRIPTNAME" ] ; do 
        #Dereference the link to the name of the link target 
        SCRIPTNAME=`ls -l "$SCRIPTNAME" | awk '{ print $NF; }'`
done

#Base directory where ZAP is installed
BASEDIR=`dirname $SCRIPTNAME`

#Switch to the directory where ZAP is installed
cd "$BASEDIR"

#Work out best memory options
MEM=`free -m | grep Mem | awk '{ print $2;}'`

if [ -z $MEM ]
then
	echo "Failed to obtain current memory, using jmv default memory settings"
else
	echo "Available memory: " $MEM"Mb"
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
java ${JMEM} -XX:PermSize=256M -jar ${BASEDIR}/zap.jar org.zaproxy.zap.ZAP $*

