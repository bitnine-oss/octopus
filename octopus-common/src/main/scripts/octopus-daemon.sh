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

# Runs a Octopus command as a daemon.

usage="Usage: octopus-daemon.sh [--config <conf-dir>] (start|stop) <octopus-command"

if [ $# -lt 2 ]; then
    echo $usage
    exit 1
fi

sbin=$(dirname -- "${BASH_SOURCE-$0}")
sbin=$(cd -- "$sbin"; pwd)

DEFAULT_LIBEXEC_DIR=$sbin/../libexec
OCTOPUS_LIBEXEC_DIR=${OCTOPUS_LIBEXEC_DIR:-$DEFAULT_LIBEXEC_DIR}
. "$OCTOPUS_LIBEXEC_DIR/octopus-config.sh"

export OCTOPUS_ROOT_LOGGER_LEVEL=${OCTOPUS_ROOT_LOGGER_LEVEL:-INFO}
export OCTOPUS_ROOT_LOGGER_APPENDER=${OCTOPUS_ROOT_LOGGER_APPENDER:-rolling}

start_stop=$1
shift
COMMAND=$1
shift

id=octopus-$OCTOPUS_IDENT_STR-$COMMAND
log=$OCTOPUS_LOG_DIR/$id-${HOSTNAME}.log
pid=$OCTOPUS_PID_DIR/${id}.pid

case $start_stop in
    start)
        [ -w "$OCTOPUS_PID_DIR" ] || mkdir -p "$OCTOPUS_PID_DIR"
        if [ -f "$pid" ]; then
            target_pid=$(cat "$pid")
            if kill -0 $target_pid > /dev/null 2>&1; then
                echo "$COMMAND running as process ${target_pid}. Stop it first."
                exit 1
            fi
        fi

        echo "Starting $COMMAND, logging to $log"
        cd "$OCTOPUS_PREFIX"
        "$OCTOPUS_PREFIX/bin/octopus" --config "$OCTOPUS_CONF_DIR" $COMMAND "$@" > "$log" 2>&1 < /dev/null &
        echo $! > "$pid"
        sleep 1
        head "$log"

        echo "ulimit -a for user $USER" >> "$log"
        ulimit -a >> "$log" 2>&1
        sleep 3

        if ! ps -p $! > /dev/null; then
            exit 1
        fi
        ;;

    stop)
        if [ -f "$pid" ]; then
            target_pid=$(cat "$pid")
            if kill -0 $target_pid > /dev/null 2>&1; then
                echo "Stopping $COMMAND"
                kill $target_pid
                sleep $OCTOPUS_STOP_TIMEOUT
                if kill -0 $target_pid > /dev/null 2>&1; then
                    echo "$COMMAND did not stop gracefully after $OCTOPUS_STOP_TIMEOUT seconds: killing with kill -9"
                    kill -9 $target_pid
                fi
            else
                echo "No $COMMAND to stop"
            fi
            rm -f "$pid"
        else
            echo "No $COMMAND to stop"
        fi
        ;;

    *)
        echo $usage
        exit 1
        ;;
esac
