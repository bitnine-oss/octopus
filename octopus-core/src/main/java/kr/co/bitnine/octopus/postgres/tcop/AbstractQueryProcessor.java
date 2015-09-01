package kr.co.bitnine.octopus.postgres.tcop;

import kr.co.bitnine.octopus.postgres.catalog.PostgresType;
import kr.co.bitnine.octopus.postgres.utils.PostgresErrorData;
import kr.co.bitnine.octopus.postgres.utils.PostgresException;
import kr.co.bitnine.octopus.postgres.utils.PostgresSQLState;
import kr.co.bitnine.octopus.postgres.utils.PostgresSeverity;
import kr.co.bitnine.octopus.postgres.utils.adt.FormatCode;
import kr.co.bitnine.octopus.postgres.utils.cache.CachedQuery;
import kr.co.bitnine.octopus.postgres.utils.cache.Portal;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractQueryProcessor implements QueryProcessor
{
    private final Map<String, CachedQuery> cachedQueries = new HashMap<>();
    private final Map<String, Portal> portals = new HashMap<>();

    @Override
    public Portal query(String queryString) throws PostgresException
    {
        parse(queryString, "", new PostgresType[0]);
        bind("", "", new FormatCode[0], new byte[0][], new FormatCode[0]);
        return getPortal("");
    }

    @Override
    public CachedQuery parse(String queryString, String stmtName, PostgresType[] paramTypes) throws PostgresException
    {
        CachedQuery cq = processParse(queryString, paramTypes);

        if (cachedQueries.containsKey(stmtName)) {
            if (stmtName.isEmpty()) {
                cachedQueries.remove(stmtName);
            } else {
                PostgresErrorData edata = new PostgresErrorData(
                        PostgresSeverity.ERROR,
                        PostgresSQLState.DUPLICATE_PSTATEMENT,
                        "prepared statement \"" + stmtName + "\" already exists");
                throw new PostgresException(edata);
            }
        }

        cachedQueries.put(stmtName, cq);
        return cq;
    }

    @Override
    public Portal bind(String stmtName, String portalName, FormatCode[] paramFormats, byte[][] paramValues, FormatCode[] resultFormats) throws PostgresException
    {
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
                portals.remove(stmtName);
            } else {
                PostgresErrorData edata = new PostgresErrorData(
                        PostgresSeverity.ERROR,
                        PostgresSQLState.DUPLICATE_CURSOR,
                        "cursor \"" + portalName + "\" already exists");
                throw new PostgresException(edata);
            }
        }

        Portal p = processBind(cq, paramFormats, paramValues, resultFormats);
        portals.put(portalName, p);
        return p;
    }

    @Override
    public CachedQuery getCachedQuery(String stmtName) throws PostgresException
    {
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
    public Portal getPortal(String portalName) throws PostgresException
    {
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
    public void closeCachedQuery(String stmtName) throws PostgresException
    {
        CachedQuery cq = getCachedQuery(stmtName);
        cachedQueries.remove(stmtName);
        cq.close();
    }

    @Override
    public void closePortal(String portalName) throws PostgresException
    {
        Portal p = getPortal(portalName);
        portals.remove(portalName);
        p.close();
    }

    protected abstract CachedQuery processParse(String queryString, PostgresType[] paramTypes) throws PostgresException;
    protected abstract Portal processBind(CachedQuery cachedQuery, FormatCode[] paramFormats, byte[][] paramValues, FormatCode[] resultFormats) throws PostgresException;
}
