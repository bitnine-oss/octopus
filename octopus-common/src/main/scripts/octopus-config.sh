#!/usr/bin/env bash

# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# included in all the octopus scripts with source command
# should not be executed directly
# also should not be passed any arguments, since we need original $*

# convert a relative path to an absolute path
# N.B.: The -P option requires bash built-ins or POSIX:2001 compliant pwd
libexec=$(dirname -- "${BASH_SOURCE-$0}")
libexec=$(cd -- "$libexec"; pwd -P)

# the root of the Octopus installation
OCTOPUS_DEFAULT_PREFIX=$(cd -- "$libexec/.."; pwd -P)
OCTOPUS_PREFIX=${OCTOPUS_PREFIX:-$OCTOPUS_DEFAULT_PREFIX}
export OCTOPUS_PREFIX

# check to see if the conf dir is given as an optional argument
if [ $# -gt 1 ]; then
    if [ "--config" = "$1" ]; then
        shift
        confdir=$1
        if [ ! -d "$confdir" ]; then
            echo "Error: Cannot find configuration directory: $confdir" >&2
            exit 1
        fi
        shift
        OCTOPUS_CONF_DIR=$confdir
    fi
fi

OCTOPUS_CONF_DIR=${OCTOPUS_CONF_DIR:-$OCTOPUS_PREFIX/etc/octopus}
export OCTOPUS_CONF_DIR

if [ -f "$OCTOPUS_CONF_DIR/octopus-env.sh" ]; then
    . "$OCTOPUS_CONF_DIR/octopus-env.sh"
fi

if [ -z "$JAVA_HOME" ]; then
    echo "Error: JAVA_HOME is not set" >&2
    exit 1
fi

JAVA=$JAVA_HOME/bin/java
JAVA_HEAP_MAX=-Xmx1000m

# check envvars which might override default args
if [ -n "$OCTOPUS_HEAPSIZE" ]; then
    JAVA_HEAP_MAX=-Xmx${OCTOPUS_HEAPSIZE}m
fi

# check for hadoop in the PATH
HADOOP=$(which hadoop 2> /dev/null)
# HADOOP_PREFIX overrides hadoop in the PATH
if [ -n "$HADOOP_PREFIX" ]; then
    HADOOP=$HADOOP_PREFIX/bin/hadoop
fi
if [ ! -f "$HADOOP" ]; then
    echo "Error: Cannot find Hadoop installation: HADOOP_PREFIX must be set or hadoop must be in the PATH" >&2
    exit 1
fi

CLASSPATH=$("$HADOOP" classpath 2> /dev/null)

# OCTOPUS_BASE_CLASSPATH initially contains OCTOPUS_CONF_DIR
OCTOPUS_BASE_CLASSPATH=$OCTOPUS_CONF_DIR

OCTOPUS_LIB_JARS_DIR=${OCTOPUS_LIB_JARS_DIR:-share/octopus/lib}
OCTOPUS_BASE_CLASSPATH=$OCTOPUS_BASE_CLASSPATH:$OCTOPUS_PREFIX/$OCTOPUS_LIB_JARS_DIR/*

OCTOPUS_EXT_JARS_DIR=${OCTOPUS_EXT_JARS_DIR:-share/octopus/ext}
OCTOPUS_BASE_CLASSPATH=$OCTOPUS_BASE_CLASSPATH:$OCTOPUS_PREFIX/$OCTOPUS_EXT_JARS_DIR/*

OCTOPUS_JARS_DIR=${OCTOPUS_JARS_DIR:-share/octopus}
OCTOPUS_BASE_CLASSPATH=$OCTOPUS_BASE_CLASSPATH:$OCTOPUS_PREFIX/$OCTOPUS_JARS_DIR/*

CLASSPATH=$OCTOPUS_BASE_CLASSPATH:$CLASSPATH

OCTOPUS_IDENT_STR=${OCTOPUS_IDENT_STR:-$USER}
OCTOPUS_PID_DIR=${OCTOPUS_PID_DIR:-/tmp}

# default settings for logging
OCTOPUS_LOG_DIR=${OCTOPUS_LOG_DIR:-$OCTOPUS_PREFIX/logs}
if [ ! -w "$OCTOPUS_LOG_DIR" ]; then
    mkdir -p "$OCTOPUS_LOG_DIR"
    chown "$OCTOPUS_IDENT_STR" "$OCTOPUS_LOG_DIR"
fi
OCTOPUS_LOG_FILE=${OCTOPUS_LOG_FILE:-octopus.log}
OCTOPUS_ROOT_LOGGER_LEVEL=${OCTOPUS_LOG_LEVEL:-INFO}

OCTOPUS_STOP_TIMEOUT=${OCTOPUS_STOP_TIMEOUT:-5}

log4jprops=octopus-log4j.properties
if [ -f "$OCTOPUS_CONF_DIR/$log4jprops" ]; then
    log4jprops=file://$OCTOPUS_CONF_DIR/$log4jprops
fi
OCTOPUS_OPTS="$OCTOPUS_OPTS -Dlog4j.configuration=$log4jprops"
OCTOPUS_OPTS="$OCTOPUS_OPTS -Doctopus.log.dir=$OCTOPUS_LOG_DIR"
OCTOPUS_OPTS="$OCTOPUS_OPTS -Doctopus.log.file=$OCTOPUS_LOG_FILE"
OCTOPUS_OPTS="$OCTOPUS_OPTS -Dmeta.update.log.dir=$OCTOPUS_LOG_DIR"
