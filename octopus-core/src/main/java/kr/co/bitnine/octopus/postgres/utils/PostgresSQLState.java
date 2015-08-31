/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kr.co.bitnine.octopus.postgres.utils;

import org.omg.PortableInterceptor.SUCCESSFUL;

public enum PostgresSQLState
{
    SUCCESSFUL_COMPLETION   ("00000"),
    WARNING                 ("01000"),
    PROTOCOL_VIOLATION      ("08P01"),
    FEATURE_NOT_SUPPORTED   ("0A000"),
    UNDEFINED_PSTATEMENT    ("26000"),
    INVALID_PASSWORD        ("28P01"),
    UNDEFINED_CURSOR        ("34000"),
    DUPLICATE_CURSOR        ("42P03"),
    DUPLICATE_PSTATEMENT    ("42P05"),
    TOO_MANY_CONNECTIONS    ("53300"),
    INTERNAL_ERROR          ("XX000");

    private final String state;

    PostgresSQLState(String state)
    {
        this.state = state;
    }

    public String getState()
    {
        return this.state;
    }

    public static PostgresSQLState defaultOf(PostgresSeverity severity)
    {
        switch (severity) {
            case PANIC:
            case FATAL:
            case ERROR:
                return INTERNAL_ERROR;
            case WARNING:
                return WARNING;
            case NOTICE:
            case INFO:
            case LOG:
            case DEBUG:
                return SUCCESSFUL_COMPLETION;
            default:
                throw new RuntimeException("invalid severity");
        }
    }
}
