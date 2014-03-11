#!/bin/bash
set -e -x -u

MY_PATH="`dirname \"$0\"`"              # relative
MY_PATH="`( cd \"$MY_PATH\" && pwd )`"  # absolutized and normalized

source "$MY_PATH/detect_qmake.sh"

# Prints number of cores to stdout
GetCPUCores() {
  case "$OSTYPE" in
    # it's GitBash under Windows
    msys)      echo $NUMBER_OF_PROCESSORS
               ;;
    linux-gnu) grep -c ^processor /proc/cpuinfo 2>/dev/null
               ;;
    darwin*)   sysctl -n hw.ncpu
               ;;
    *)         echo "Unsupported platform in $0"
               exit 1
               ;;
  esac
  return 0
}

# 1st param: shadow directory path
# 2nd param: mkspec
# 3rd param: additional qmake parameters
BuildQt() {
  (
    # set qmake path
    QMAKE="$(PrintQmakePath)" || ( echo "ERROR: qmake was not found, please add it to your PATH or into the tools/autobuild/detect_qmake.sh"; exit 1 )
    SHADOW_DIR="$1"
    MKSPEC="$2"
    QMAKE_PARAMS="$3"

    mkdir -p "$SHADOW_DIR"
    cd "$SHADOW_DIR"
    "$QMAKE" CONFIG-=sdk -r "$QMAKE_PARAMS" -spec "$MKSPEC" "$MY_PATH/../../omim.pro"
#    make clean > /dev/null || true
    make -j $(GetCPUCores)
  )
}
