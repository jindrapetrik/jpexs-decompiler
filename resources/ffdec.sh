#!/usr/bin/env bash

# This is a comment, it starts with "#".

# Uncomment following and set it to higher value if you want more memory
# You need 64 bit OS and 64 bit java to set it to higher values
# FFDEC_MEMORY=1024m

# Uncomment following when you encounter StackOverFlowErrors. 
# If the app then terminates with OutOfMemory you can experiment with lower value.
# FFDEC_STACK_SIZE=32m


# Hide VLC error output
export VLC_VERBOSE=-1

# FFDec requires Oracle Java 8
# Look for java in these directories
LOOKUP_JRE_DIRS="/usr/lib/jvm/* /opt/java* /opt/jre*"
# Required version
REQ_JVER1=1
REQ_JVER2=8
REQ_JVER3=0
REQ_JVER4=0


if [ -z ${FFDEC_JARFILENAME+x} ]; then
    FFDEC_JARFILENAME=ffdec.jar
fi

search_jar_file() {
    JAR_FILE_CANDIDATES="./${FFDEC_JARFILENAME} ../dist/${FFDEC_JARFILENAME} /usr/share/java/${FFDEC_JARFILENAME} /usr/share/java/ffdec/${FFDEC_JARFILENAME} /usr/share/java/jpexs-decompiler/${FFDEC_JARFILENAME}"
    for f in $JAR_FILE_CANDIDATES ; do
        [ -r "$f" ] && JAR_FILE="$f" && return 0
    done
    echo Unable to find ${FFDEC_JARFILE} in the following locations:
    echo "${JAR_FILE_CANDIDATES// /$'\n'}"
    return 1
}

check_java_version () {
    JVER1=$(echo $JAVA_VERSION_OUTPUT | sed -E 's/java version "([0-9]*)\.[0-9]*\.[0-9]*(_[0-9]*)?".*/\1/')
    JVER2=$(echo $JAVA_VERSION_OUTPUT | sed -E 's/java version "[0-9]*\.([0-9]*)\.[0-9]*(_[0-9]*)?".*/\1/')
    JVER3=$(echo $JAVA_VERSION_OUTPUT | sed -E 's/java version "[0-9]*\.[0-9]*\.([0-9]*)(_[0-9]*)?".*/\1/')
    JVER4=$(echo $JAVA_VERSION_OUTPUT | sed -E 's/java version "[0-9]*\.[0-9]*\.[0-9]*(_([0-9]*))?".*/\2/' | sed 's/^$/0/')

    if [ "$JVER1" -gt $REQ_JVER1 ]; then
        return 0
    elif [ "$JVER1" -lt $REQ_JVER1 ]; then
        return 1
    fi

    if [ "$JVER2" -gt $REQ_JVER2 ]; then
        return 0
    elif [ "$JVER2" -lt $REQ_JVER2 ]; then
        return 1
    fi

    if [ "$JVER3" -gt $REQ_JVER3 ]; then
        return 0
    elif [ "$JVER3" -lt $REQ_JVER3 ]; then
        return 1
    fi

    if [ "$JVER4" -lt $REQ_JVER4 ]; then
        return 1
    fi

    return 0
}

# Handle symlinks
PROGRAM="$0"
while [ -L "$PROGRAM" ]; do
    PROGRAM=$(readlink -f "$PROGRAM")
done
pushd "$(dirname "$PROGRAM")" > /dev/null

search_jar_file || exit 1

if [ "${JAR_FILE:0:1}" != '/' ] ; then
    JAR_FILE=$(pwd)/$JAR_FILE
fi

popd > /dev/null

if [ -z ${FFDEC_MEMORY+x} ]; then
    FFDEC_MEMORY=1024m
fi

STACK_SIZE_PARAM=""
MEMORY_PARAM=""
if [ -n "$FFDEC_STACK_SIZE" ]; then
    STACK_SIZE_PARAM=" -Xss$FFDEC_STACK_SIZE"
fi
if [ -n "$FFDEC_MEMORY" ]; then
    MEMORY_PARAM=" -Xmx$FFDEC_MEMORY"
fi

args=(-Djava.net.preferIPv4Stack=true -Djna.nosys=true -Djava.util.Arrays.useLegacyMergeSort=true${MEMORY_PARAM}${STACK_SIZE_PARAM} -jar "$JAR_FILE" "$@")

if [ "$(uname)" = "Darwin" ]; then
    args=(-Xdock:name=FFDec -Xdock:icon=icon.png "${args[@]}")
fi

# Check default java
if [ -x "$(which java)" ]; then
    JAVA_VERSION_OUTPUT=$(java -version 2>&1)
    JAVA_VERSION_OUTPUT=$(echo $JAVA_VERSION_OUTPUT | sed -E 's/.*(openjdk|java) version/java version/')
    check_java_version && exec java "${args[@]}"
fi

# Test other possible Java locations
for JRE_PATH in $LOOKUP_JRE_DIRS; do
    if [ -x "$JRE_PATH/bin/java" ]; then
        JAVA_VERSION_OUTPUT=$("$JRE_PATH/bin/java" -version 2>&1)
        JAVA_VERSION_OUTPUT=`echo $JAVA_VERSION_OUTPUT | sed -E 's/.*(openjdk|java) version/java version/'`
        check_java_version && {
            export JRE_PATH
            exec "$JRE_PATH/bin/java" "${args[@]}"
        }
    fi
done

# Failed
if [ -x "$(which xmessage)" ]; then
    xmessage -nearmouse -file - <<EOF
Failed to find a suitable java version.
Required: $REQ_JVER1.$REQ_JVER2.$REQ_JVER3.$REQ_JVER4 or newer.
EOF
else
    echo Failed to find a suitable java version.
    echo Required: $REQ_JVER1.$REQ_JVER2.$REQ_JVER3.$REQ_JVER4 or newer.
fi

exit 1
