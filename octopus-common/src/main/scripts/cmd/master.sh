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

_this_cmd=master
COMMAND_LIST="${COMMAND_LIST}${_this_cmd} "

master() {
    OCTOPUS_ROOT_LOGGER_LEVEL=${OCTOPUS_ROOT_LOGGER_LEVEL:-INFO}
    OCTOPUS_ROOT_LOGGER_APPENDER=${OCTOPUS_ROOT_LOGGER_APPENDER:-console}

    OCTOPUS_OPTS="$OCTOPUS_OPTS -Doctopus.rootLogger.level=$OCTOPUS_ROOT_LOGGER_LEVEL"
    OCTOPUS_OPTS="$OCTOPUS_OPTS -Doctopus.rootLogger.appender=$OCTOPUS_ROOT_LOGGER_APPENDER"
    CLASS=kr.co.bitnine.octopus.master.OctopusMaster
    exec "$JAVA" $JAVA_HEAP_MAX $OCTOPUS_OPTS $CLASS "$@"
}
