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

import kr.co.bitnine.octopus.postgres.libpq.Message;

public final class PostgresErrorData {
    private final PostgresSeverity severity;
    private final PostgresSQLState sqlState;
    private final String message;

    private String detail;
    private String hint;
    private int position;
    private int internalPosition;
    private String internalQuery;
    private String where;    // context
    private String schemaName;
    private String tableName;
    private String columnName;
    private String datatypeName;
    private String constraintName;
    private String file;
    private int line;
    private String routine;

    public PostgresErrorData(PostgresSeverity severity, PostgresSQLState sqlState) {
        this(severity, sqlState, "missing error text");
    }

    public PostgresErrorData(PostgresSeverity severity, String message) {
        this(severity, PostgresSQLState.defaultOf(severity), message);
    }

    public PostgresErrorData(PostgresSeverity severity, PostgresSQLState sqlState, String message) {
        this.severity = severity;
        this.sqlState = sqlState;
        this.message = message;
    }

    public PostgresSeverity getSeverity() {
        return severity;
    }

    public String getMessage() {
        return message;
    }

    public boolean isError() {
        switch (severity) {
        case PANIC:
        case FATAL:
        case ERROR:
            return true;
        default:
            return false;
        }
    }

    public Message toMessage() {
        // 'E' for ErrorResponse, 'N' for NoticeResponse
        char type = isError() ? 'E' : 'N';

        Message.Builder builder = Message.builder(type)
                .putChar('S')
                .putCString(severity.name())
                .putChar('C')
                .putCString(sqlState.getState())
                .putChar('M')
                .putCString(message);

        if (detail != null) {
            builder.putChar('D');
            builder.putCString(detail);
        }
        if (hint != null) {
            builder.putChar('H');
            builder.putCString(hint);
        }
        if (position > 0) {
            builder.putChar('P');
            builder.putCString(String.valueOf(position));
        }
        if (internalPosition > 0) {
            builder.putChar('p');
            builder.putCString(String.valueOf(internalPosition));
        }
        if (internalQuery != null) {
            builder.putChar('q');
            builder.putCString(internalQuery);
        }
        if (where != null) {
            builder.putChar('W');
            builder.putCString(where);
        }
        if (schemaName != null) {
            builder.putChar('s');
            builder.putCString(schemaName);
        }
        if (tableName != null) {
            builder.putChar('t');
            builder.putCString(tableName);
        }
        if (columnName != null) {
            builder.putChar('c');
            builder.putCString(columnName);
        }
        if (datatypeName != null) {
            builder.putChar('d');
            builder.putCString(datatypeName);
        }
        if (constraintName != null) {
            builder.putChar('n');
            builder.putCString(constraintName);
        }
        if (file != null) {
            builder.putChar('F');
            builder.putCString(file);
        }
        if (line > 0) {
            builder.putChar('L');
            builder.putCString(String.valueOf(line));
        }
        if (routine != null) {
            builder.putChar('R');
            builder.putCString(routine);
        }

        builder.putChar('\0');

        return builder.build();
    }
}
