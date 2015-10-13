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

package kr.co.bitnine.octopus.sql;

import kr.co.bitnine.octopus.postgres.access.common.TupleDesc;
import kr.co.bitnine.octopus.postgres.executor.Tuple;
import kr.co.bitnine.octopus.postgres.executor.TupleSet;
import kr.co.bitnine.octopus.postgres.utils.PostgresException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class TupleSetSql implements TupleSet {
    private TupleDesc tupleDesc;
    private final List<Tuple> tuples;
    private Iterator<Tuple> iter;

    public TupleSetSql() {
        tuples = new ArrayList<>();
    }

    @Override
    public TupleDesc getTupleDesc() {
        return tupleDesc;
    }

    public void setTupleDesc(TupleDesc tupleDesc) {
        this.tupleDesc = tupleDesc;
    }

    @Override
    public Tuple next() throws PostgresException {
        if (iter == null)
            iter = tuples.iterator();

        return iter.hasNext() ? iter.next() : null;
    }

    @Override
    public void close() throws PostgresException { }

    public void addTuple(Tuple t) {
        tuples.add(t);
    }

    public void addTuples(List<Tuple> ts) {
        tuples.addAll(ts);
    }
}
