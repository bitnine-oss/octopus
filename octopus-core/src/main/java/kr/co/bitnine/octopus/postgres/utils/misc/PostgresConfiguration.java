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

package kr.co.bitnine.octopus.postgres.utils.misc;

import kr.co.bitnine.octopus.postgres.libpq.ProtocolConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public final class PostgresConfiguration extends TreeMap<String, String> {
    private static final Log LOG = LogFactory.getLog(PostgresConfiguration.class);

    public static final String PARAM_INTEGER_DATETIMES = "integer_datetimes";
    public static final String PARAM_EXTRA_FLOAT_DIGITS = "extra_float_digits";
    public static final String PARAM_DATESTYLE = "DateStyle";
    public static final String PARAM_CLIENT_ENCODING = "client_encoding";
    public static final String PARAM_SERVER_ENCODING = "server_encoding";
    public static final String PARAM_SERVER_VERSION = "server_version";
    public static final String PARAM_TIMEZONE = "TimeZone";

    public static final String PARAM_DATABASE = "database";
    public static final String PARAM_USER = "user";

    private static final Set<String> PARAMS = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

    static {
        PARAMS.add(PARAM_INTEGER_DATETIMES);
        PARAMS.add(PARAM_EXTRA_FLOAT_DIGITS);
        PARAMS.add(PARAM_DATESTYLE);
        PARAMS.add(PARAM_CLIENT_ENCODING);
        PARAMS.add(PARAM_SERVER_ENCODING);
        PARAMS.add(PARAM_SERVER_VERSION);
        PARAMS.add(PARAM_TIMEZONE);

        PARAMS.add(PARAM_DATABASE);
        PARAMS.add(PARAM_USER);
    }

    public PostgresConfiguration() {
        super(String.CASE_INSENSITIVE_ORDER);

        put(PARAM_INTEGER_DATETIMES, "on");
        put(PARAM_EXTRA_FLOAT_DIGITS, "0");
        put(PARAM_DATESTYLE, "ISO");
        put(PARAM_CLIENT_ENCODING, "UTF8");
        put(PARAM_SERVER_ENCODING, "UTF8");
        put(PARAM_SERVER_VERSION, ProtocolConstants.POSTGRES_VERSION);
        put(PARAM_TIMEZONE, "GMT");
    }

    private static boolean isValidParam(String key) {
        return key != null && PARAMS.contains(key);
    }

    @Override
    public String put(String key, String value) {
        if (!isValidParam(key)) {
            LOG.warn("configuration parameter '" + key + "' is not supported, ignored (value='" + value + "')");
            return null;
        }

        LOG.debug("configuration parameter '" + key + "' is set to '" + value + "'");
        return super.put(key, value);
    }
}
