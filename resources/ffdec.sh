#!/bin/bash
# Based on Freerapid Downloader startup script - created by Petris 2009

# FFDec requires Oracle Java 8
# Look for java in these directories
LOOKUP_JRE_DIRS="/usr/lib/jvm/* /opt/java* /opt/jre*"
# Required version
REQ_JVER1=1
REQ_JVER2=8
REQ_JVER3=0
REQ_JVER4=0
MEMORY=1024m

search_jar_file() {
    JAR_FILE_CANDIDATES='./ffdec.jar ../dist/ffdec.jar /usr/share/java/ffdec.jar /usr/share/java/ffdec/ffdec.jar /usr/share/java/jpexs-decompiler/ffdec.jar'
    for f in $JAR_FILE_CANDIDATES ; do
        [ -r "$f" ] && JAR_FILE="$f" && return 0
    done
    echo Unable to find ffdec.jar in the following locations:
    echo "${JAR_FILE_CANDIDATES// /$'\n'}"
    return 1
}

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
pushd "`dirname \"$PROGRAM\"`" > /dev/null

search_jar_file || exit 1

if [ ${JAR_FILE:0:1} != '/' ] ; then
    JAR_FILE=`pwd`/$JAR_FILE
fi

popd > /dev/null

args=(-Djava.net.preferIPv4Stack=true -Xmx$MEMORY -jar $JAR_FILE "$@")

if [ "`uname`" = "Darwin" ]; then
	args=(-Xdock:name=FFDec -Xdock:icon=icon.png "${args[@]}")
fi

# Check default java
if [ -x "`which java`" ]; then
	JAVA_VERSION_OUTPUT=`java -version 2>&1`
	JAVA_VERSION_OUTPUT=`echo $JAVA_VERSION_OUTPUT | sed 's/openjdk version/java version/'`
	check_java_version && exec java "${args[@]}"
fi

# Test other possible Java locations
for JRE_PATH in $LOOKUP_JRE_DIRS; do
	if [ -x "$JRE_PATH/bin/java" ]; then
		JAVA_VERSION_OUTPUT=`"$JRE_PATH/bin/java" -version 2>&1`
		JAVA_VERSION_OUTPUT=`echo $JAVA_VERSION_OUTPUT | sed 's/openjdk version/java version/'`
		check_java_version && {
			export JRE_PATH
			exec $JRE_PATH/bin/java "${args[@]}"
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
