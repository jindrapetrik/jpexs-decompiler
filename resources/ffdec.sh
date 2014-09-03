#!/bin/sh
# FFDec requires Oracle Java 7
# Look for java in these directories
LOOKUP_JRE_DIRS="/usr/lib/jvm/* /opt/java* /opt/jre*"
#Created by Petris 2009 -> Many thanks!
# Required version
REQ_JVER1=1
REQ_JVER2=7
REQ_JVER3=0
REQ_JVER4=0
JAR_FILE=ffdec.jar
MEMORY=1024m

check_java_version () {
	JVER1=`echo $JAVA_VERSION_OUTPUT | sed 's/java version "\([0-9]*\)\.[0-9]*\.[0-9]*_[0-9]*".*/\1/'`
	JVER2=`echo $JAVA_VERSION_OUTPUT | sed 's/java version "[0-9]*\.\([0-9]*\)\.[0-9]*_[0-9]*".*/\1/'`
	JVER3=`echo $JAVA_VERSION_OUTPUT | sed 's/java version "[0-9]*\.[0-9]*\.\([0-9]*\)_[0-9]*".*/\1/'`
	JVER4=`echo $JAVA_VERSION_OUTPUT | sed 's/java version "[0-9]*\.[0-9]*\.[0-9]*_\([0-9]*\)".*/\1/'`

	if [ $JVER1 -gt $REQ_JVER1 ]; then
		return 0
	elif [ $JVER1 -lt $REQ_JVER1 ]; then
		return 1
	fi

	if [ $JVER2 -gt $REQ_JVER2 ]; then
		return 0
	elif [ $JVER2 -lt $REQ_JVER2 ]; then
		return 1
	fi

	if [ $JVER3 -gt $REQ_JVER3 ]; then
		return 0
	elif [ $JVER3 -lt $REQ_JVER3 ]; then
		return 1
	fi

	if [ $JVER4 -lt $REQ_JVER4 ]; then
		return 1
	fi

	return 0	
}

# Handle symlinks
PROGRAM="$0"
while [ -L "$PROGRAM" ]; do
	PROGRAM=`readlink -f "$PROGRAM"`
done
cd "`dirname \"$PROGRAM\"`"

# Check default java
if [ -x "`which java`" ]; then
	JAVA_VERSION_OUTPUT=`java -version 2>&1`
	check_java_version && exec java -Djava.net.preferIPv4Stack=true -Xmx$MEMORY -jar $JAR_FILE $@
fi

# Test other possible Java locations
for JRE_PATH in $LOOKUP_JRE_DIRS; do
	if [ -x "$JRE_PATH/bin/java" ]; then
		JAVA_VERSION_OUTPUT=`"$JRE_PATH/bin/java" -version 2>&1`
		check_java_version && {
			export JRE_PATH
			exec $JRE_PATH/bin/java -Djava.net.preferIPv4Stack=true -Xmx$MEMORY -jar $JAR_FILE $@
		}
	fi
done

# Failed
if [ -x "`which xmessage`" ]; then
	xmessage -nearmouse -file - <<EOF 
Failed to find a suitable java version.
Required: $REQ_JVER1.$REQ_JVER2.$REQ_JVER3_$REQ_JVER4 or newer.
EOF
else
	echo Failed to find a suitable java version.
	echo Required: $REQ_JVER1.$REQ_JVER2.$REQ_JVER3_$REQ_JVER4 or newer.
fi

exit 1
