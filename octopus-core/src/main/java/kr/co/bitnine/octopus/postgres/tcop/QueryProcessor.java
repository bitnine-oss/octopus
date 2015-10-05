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
import kr.co.bitnine.octopus.postgres.utils.PostgresException;
import kr.co.bitnine.octopus.postgres.utils.adt.FormatCode;
import kr.co.bitnine.octopus.postgres.utils.cache.CachedQuery;
import kr.co.bitnine.octopus.postgres.utils.cache.Portal;

public interface QueryProcessor {
    Portal query(String queryString) throws PostgresException;

    CachedQuery parse(String queryString, String stmtName, PostgresType[] paramTypes) throws PostgresException;

    Portal bind(String stmtName, String portalName, FormatCode[] paramFormats, byte[][] paramValues, FormatCode[] resultFormats) throws PostgresException;

    CachedQuery getCachedQuery(String stmtName) throws PostgresException;

    Portal getPortal(String portalName) throws PostgresException;

    void closeCachedQuery(String stmtName) throws PostgresException;

    void closePortal(String portalName) throws PostgresException;
}
