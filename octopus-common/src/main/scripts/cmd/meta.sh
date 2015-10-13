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

_this_cmd=meta
COMMAND_LIST="${COMMAND_LIST}${_this_cmd} "

meta() {
    if [ $# -lt 1 ]; then
        cat <<EOF
Usage: octopus meta COMMAND ...
where COMMAND is one of:
  -superuser    create a superuser with given <username> and <password>
EOF
        exit 1
    fi

    OCTOPUS_ROOT_LOGGER_APPENDER=${OCTOPUS_ROOT_LOGGER_APPENDER:-null}

    OCTOPUS_OPTS="$OCTOPUS_OPTS -Doctopus.rootLogger.appender=$OCTOPUS_ROOT_LOGGER_APPENDER"
    CLASS=kr.co.bitnine.octopus.meta.MetaShell
    exec "$JAVA" $OCTOPUS_OPTS $CLASS "$@"
}
