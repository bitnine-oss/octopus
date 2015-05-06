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

package kr.co.bitnine.octopus.pgproto;

public class ErrorData
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

    public static String SUCCESSFUL_COMPLETION = "00000";   // default when severity <= NOTICE
    public static String WARNING = "01000";                 // default when severity == WARNING
    public static String INTERNAL_ERROR = "XX000";          // default when severity >= ERROR

    private Severity severity;
    private String sqlState;
    private String message;
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

    public ErrorData(Severity severity)
    {
        this(severity, null);

        switch (severity) {
        case DEBUG:
        case LOG:
        case INFO:
        case NOTICE:
            setSQLState(SUCCESSFUL_COMPLETION);
            break;
        case WARNING:
            setSQLState(WARNING);
            break;
        case ERROR:
        case FATAL:
        case PANIC:
            setSQLState(INTERNAL_ERROR);
        }
    }

    public ErrorData(Severity severity, String sqlState)
    {
        this(severity, sqlState, "missing error text");
    }

    public ErrorData(Severity severity, String sqlState, String message)
    {
        this.severity = severity;
        this.sqlState = sqlState;
        this.message = message;
    }

    public Severity getSeverity()
    {
        return severity;
    }

    public void setSeverity(Severity severity)
    {
        this.severity = severity;
    }

    public String getSQLState()
    {
        return sqlState;
    }

    public void setSQLState(String sqlstate)
    {
        this.sqlState = sqlstate;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }
}
