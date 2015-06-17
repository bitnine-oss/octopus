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

package kr.co.bitnine.octopus.libpg;

public class PostgresException extends Exception
{
    public enum Severity
    {
        DEBUG,      // debugging messages
        LOG,        // server operational messages
        INFO,       // messages specifically requested by user
        NOTICE,     // helpful messages to users about query operation
        WARNING,    // unexpected messages
        ERROR,      // abort transaction
        FATAL,      // abort session
        PANIC       // abort system
    }

    public static abstract class SQLSTATE {
        public static String SUCCESSFUL_COMPLETION = "00000";   // default when severity <= NOTICE
        public static String WARNING = "01000";                 // default when severity == WARNING
        public static String PROTOCOL_VIOLATION = "08P01";
        public static String FEATURE_NOT_SUPPORTED = "0A000";
        public static String INVALID_PASSWORD = "28P01";
        public static String TOO_MANY_CONNECTIONS = "53300";
        public static String INTERNAL_ERROR = "XX000";          // default when severity >= ERROR
    }

    private Severity severity;
    private String sqlState;
/*
    private String detail;
    private String hint;
    private String position;
    private String where;
    private String schema_name;
    private String table_name;
    private String column_name;
    private String datatype_name;
    private String constraint_name;
    private String file;
    private String line;
    private String routine;
 */

    public PostgresException(Severity severity, String sqlState, String message)
    {
        super(message);

        this.severity = severity;
        this.sqlState = sqlState;
    }

    public PostgresException(Severity severity, String sqlState, Throwable cause)
    {
        super(cause);

        this.severity = severity;
        this.sqlState = sqlState;
    }

    public Severity getSeverity()
    {
        return severity;
    }

    public String getSQLState()
    {
        return sqlState;
    }

    public boolean isError()
    {
        switch (severity) {
            case PANIC:
            case FATAL:
            case ERROR:
                return true;
            default:
                return false;
        }
    }

    public Message toMessage()
    {
        // 'E' for ErrorResponse, 'N' for NoticeResponse
        char type = isError() ? 'E' : 'N';

        Message.Builder builder = Message.builder(type)
                .putChar('S')
                .putCString(severity.name())
                .putChar('C')
                .putCString(sqlState)
                .putChar('M')
                .putCString(getMessage());

        // TODO: add optional fields

        builder.putChar('\0');

        return builder.build();
    }
}
