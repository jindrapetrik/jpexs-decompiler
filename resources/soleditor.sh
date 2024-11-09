#!/usr/bin/env bash

export FFDEC_JARFILENAME=ffdec.jar
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
exec $SCRIPT_DIR/ffdec.sh -soleditor
