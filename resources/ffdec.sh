#!/usr/bin/env bash

MEMORY=1024M

REQ_JAVA_VERSION_MAJOR=1
REQ_JAVA_VERSION_MINOR=8
REQ_JAVA_VERSION_PATCH=0

check_java () {
    local JAVA_PATH="$1"
    local REQ_MAJOR=$2
    local REQ_MINOR=$3
    local REQ_PATCH=$4

    local JAVA_VERSION_RAW="$($JAVA_PATH --version | grep -E 'openjdk [0-9][0-9]?\.[0-9][0-9]?\.[0-9][0-9]?' | sed 's/[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]//g' | sed 's/openjdk//g' | sed 's/ //g')"

    local JAVA_VERSION_MAJOR_STR="$(echo "$JAVA_VERSION_RAW" | sed -r 's/([0-9][0-9]?).[0-9][0-9]?.[0-9][0-9]?/\1/g')"
    local JAVA_VERSION_MINOR_STR="$(echo "$JAVA_VERSION_RAW" | sed -r 's/[0-9][0-9]?.([0-9][0-9]?).[0-9][0-9]?/\1/g')"
    local JAVA_VERSION_PATCH_STR="$(echo "$JAVA_VERSION_RAW" | sed -r 's/[0-9][0-9]?.[0-9][0-9]?.([0-9][0-9]?)/\1/g')"

    local JAVA_VERSION_MAJOR=$(expr $JAVA_VERSION_MAJOR_STR + 0)
    local JAVA_VERSION_MINOR=$(expr $JAVA_VERSION_MINOR_STR + 0)
    local JAVA_VERSION_PATCH=$(expr $JAVA_VERSION_PATCH_STR + 0)

    if [[ $JAVA_VERSION_MAJOR -lt $REQ_MAJOR ]]; then return 1; fi
    if [[ $JAVA_VERSION_MINOR -lt $REQ_MINOR ]]; then return 1; fi
    if [[ $JAVA_VERSION_PATCH -lt $REQ_PATCH ]]; then return 1; fi

    return 0
}

get_java_version () {
    local JAVA_PATH="$1"
    JAVA_VERSION="$($JAVA_PATH --version | grep -E 'openjdk [0-9][0-9]?\.[0-9][0-9]?\.[0-9][0-9]?' | sed 's/[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]//g' | sed 's/openjdk//g' | sed 's/ //g')"
}

find_jar () {
    local JAR_FILE_CANDIDATES="./ffdec.jar ../dist/ffdec.jar /usr/share/java/ffdec.jar /usr/share/java/ffdec/ffdec.jar /usr/share/java/jpexs-decompiler/ffdec.jar"
    
    for JAR in ${JAR_FILE_CANDIDATES[@]}; do
        if [[ -f "$JAR" ]]; then
            JAR_PATH="$(realpath "$JAR")"
            return 0
        fi
    done

    return 1
}

DEFAULT_JAVA="$(dirname "$(dirname "$(realpath "$(which java)")")")"
LOOKUPS="$DEFAULT_JAVA /usr/lib/jvm/* /opt/java* /opt/jre*"
JAVA=""

for JRE in ${LOOKUPS[@]}; do
    JAVA_PATH="$JRE/bin/java"
    IS_OK=$(check_java $JAVA_PATH $REQ_JAVA_VERSION_MAJOR $REQ_JAVA_VERSION_MINOR $REQ_JAVA_VERSION_PATCH)

    if [[ $IS_OK -eq 0 ]]; then
        JAVA="$JAVA_PATH"
        break
    fi
done

if [[ "$JAVA" == "" ]]; then
    echo "Unable to find a suitable Java version!"
    exit 1
fi

get_java_version "$JAVA"

echo "Using Java version: $JAVA_VERSION"

find_jar
FOUND_JAR_FILE="$(find_jar)"

if [[ $FOUND_JAR_FILE -eq 1 ]]; then
    echo "Unable to find ffdec.jar!"
    exit 1
fi

PROGRAM_ARGS=(-Djava.net.preferIPv4Stack=true -Xmx$MEMORY)

if [ "$(uname)" = "Darwin" ]; then
	PROGRAM_ARGS=(-Xdock:name=FFDec -Xdock:icon=icon.png "${args[@]}")
fi

echo "Starting /.../$(basename "$JAVA") ${PROGRAM_ARGS[@]} -jar /.../$(basename "$JAR_PATH") $@"

exec "$JAVA" ${PROGRAM_ARGS[@]} -jar $JAR_PATH $@
