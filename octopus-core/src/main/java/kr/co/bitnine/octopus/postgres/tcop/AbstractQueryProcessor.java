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

package kr.co.bitnine.octopus.postgres.tcop;

import kr.co.bitnine.octopus.postgres.catalog.PostgresType;
import kr.co.bitnine.octopus.postgres.utils.PostgresErrorData;
import kr.co.bitnine.octopus.postgres.utils.PostgresException;
import kr.co.bitnine.octopus.postgres.utils.PostgresSQLState;
import kr.co.bitnine.octopus.postgres.utils.PostgresSeverity;
import kr.co.bitnine.octopus.postgres.utils.adt.FormatCode;
import kr.co.bitnine.octopus.postgres.utils.cache.CachedQuery;
import kr.co.bitnine.octopus.postgres.utils.cache.Portal;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractQueryProcessor implements QueryProcessor {
    private static final Log LOG = LogFactory.getLog(AbstractQueryProcessor.class);

    private final Map<String, CachedQuery> cachedQueries = new HashMap<>();
    private final Map<String, Portal> portals = new HashMap<>();

    @Override
    public final Portal query(String queryString) throws PostgresException {
        parse(queryString, "", new PostgresType[0]);
        bind("", "", new FormatCode[0], new byte[0][], new FormatCode[0]);
        return getPortal("");
    }

    @Override
    public final CachedQuery parse(String queryString, String stmtName, PostgresType[] paramTypes) throws PostgresException {
        CachedQuery cq = processParse(queryString, paramTypes);

        if (cachedQueries.containsKey(stmtName)) {
            if (stmtName.isEmpty()) {
                cachedQueries.remove(stmtName).close();
            } else {
                PostgresErrorData edata = new PostgresErrorData(
                        PostgresSeverity.ERROR,
                        PostgresSQLState.DUPLICATE_PSTATEMENT,
                        "prepared statement \"" + stmtName + "\" already exists");
                throw new PostgresException(edata);
            }
        }

        if (!stmtName.isEmpty())
            LOG.info("cache query '" + stmtName + "'");
        cachedQueries.put(stmtName, cq);
        return cq;
    }

    @Override
    public final Portal bind(String stmtName, String portalName, FormatCode[] paramFormats, byte[][] paramValues, FormatCode[] resultFormats) throws PostgresException {
        CachedQuery cq = getCachedQuery(stmtName);

        if (paramFormats.length > 1 && paramFormats.length != paramValues.length) {
            PostgresErrorData edata = new PostgresErrorData(
                    PostgresSeverity.ERROR,
                    PostgresSQLState.PROTOCOL_VIOLATION,
                    "bind message has " + paramFormats.length + " parameter formats but " + paramValues.length + " parameters");
            throw new PostgresException(edata);
        }
        int numParams = cq.getParamTypes().length;
        if (paramValues.length != numParams) {
            PostgresErrorData edata = new PostgresErrorData(
                    PostgresSeverity.ERROR,
                    PostgresSQLState.PROTOCOL_VIOLATION,
                    "bind message supplies " + paramValues.length + " parameters, but prepared statement \"" + stmtName + "\" requires " + numParams);
            throw new PostgresException(edata);
        }

        if (portals.containsKey(portalName)) {
            if (portalName.isEmpty()) {
                portals.remove(portalName).close();
            } else {
                PostgresErrorData edata = new PostgresErrorData(
                        PostgresSeverity.ERROR,
                        PostgresSQLState.DUPLICATE_CURSOR,
                        "cursor \"" + portalName + "\" already exists");
                throw new PostgresException(edata);
            }
        }

        Portal p = processBind(cq, portalName, paramFormats, paramValues,
                resultFormats);
        if (!portalName.isEmpty())
            LOG.info("cache portal '" + portalName + "' for query '" + stmtName + "'");
        portals.put(portalName, p);
        return p;
    }

    @Override
    public final CachedQuery getCachedQuery(String stmtName) throws PostgresException {
        CachedQuery cq = cachedQueries.get(stmtName);
        if (cq == null) {
            String msg;
            if (stmtName.isEmpty())
                msg = "unnamed prepared statement does not exist";
            else
                msg = "prepared statement \"" + stmtName + "\" does not exist";
            PostgresErrorData edata = new PostgresErrorData(
                    PostgresSeverity.ERROR,
                    PostgresSQLState.UNDEFINED_PSTATEMENT,
                    msg);
            throw new PostgresException(edata);
        }

        return cq;
    }

    @Override
    public final Portal getPortal(String portalName) throws PostgresException {
        Portal p = portals.get(portalName);
        if (p == null) {
            PostgresErrorData edata = new PostgresErrorData(
                    PostgresSeverity.ERROR,
                    PostgresSQLState.UNDEFINED_CURSOR,
                    "portal \"" + portalName + "\" does not exist");
            throw new PostgresException(edata);
        }

        return p;
    }

    @Override
    public final void closeCachedQuery(String stmtName) throws PostgresException {
        CachedQuery cq = getCachedQuery(stmtName);
        if (!stmtName.isEmpty())
            LOG.info("remove query '" + stmtName + "'");
        cachedQueries.remove(stmtName);
        cq.close();
    }

    @Override
    public final void closePortal(String portalName) throws PostgresException {
        Portal p = getPortal(portalName);
        if (!portalName.isEmpty())
            LOG.info("remove portal '" + portalName + "'");
        portals.remove(portalName);
        p.close();
    }

    protected abstract CachedQuery processParse(String queryString, PostgresType[] paramTypes) throws PostgresException;

    protected abstract Portal processBind(CachedQuery cachedQuery,
                                          String portalName,
                                          FormatCode[] paramFormats,
                                          byte[][] paramValues,
                                          FormatCode[] resultFormats)
            throws PostgresException;
}
