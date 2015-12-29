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

package kr.co.bitnine.octopus.engine.calcite;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.calcite.linq4j.tree.Expression;
import org.apache.calcite.materialize.Lattice;
import org.apache.calcite.schema.Function;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.TableMacro;
import org.apache.calcite.schema.impl.MaterializedViewTable;
import org.apache.calcite.schema.impl.StarTable;
import org.apache.calcite.util.Compatible;

/**
 * Schema.
 * <p/>
 * <p>Wrapper around user-defined schema used internally.</p>
 */
public abstract class CalciteSchema {
    /**
     * Comparator that compares all strings differently, but if two strings are
     * equal in case-insensitive match they are right next to each other. In a
     * collection sorted on this comparator, we can find case-insensitive matches
     * for a given string using a range scan between the upper-case string and
     * the lower-case string.
     */
    protected static final Comparator<String> COMPARATOR =
            new Comparator<String>() {
                public int compare(String o1, String o2) {
                    int c = o1.compareToIgnoreCase(o2);
                    if (c == 0) {
                        c = o1.compareTo(o2);
                    }
                    return c;
                }
            };

    private final CalciteSchema parent;
    private final Schema schema;
    private final String name;
    /**
     * Tables explicitly defined in this schema. Does not include tables in
     * {@link #schema}.
     */
    private final NavigableMap<String, TableEntry> tableMap =
            new TreeMap<>(COMPARATOR);
    private final Multimap<String, FunctionEntry> functionMap =
            LinkedListMultimap.create();
    private final NavigableMap<String, LatticeEntry> latticeMap =
            new TreeMap<>(COMPARATOR);
    private final NavigableSet<String> functionNames =
            new TreeSet<>(COMPARATOR);
    private final NavigableMap<String, FunctionEntry> nullaryFunctionMap =
            new TreeMap<>(COMPARATOR);
    private final NavigableMap<String, CalciteSchema> subSchemaMap =
            new TreeMap<>(COMPARATOR);
    private ImmutableList<ImmutableList<String>> path;

    CalciteSchema(CalciteSchema parent, Schema schema, String name) {
        this.parent = parent;
        this.schema = schema;
        this.name = name;
    }

    /**
     * Returns a sub-schema with a given name that is defined implicitly
     * (that is, by the underlying {@link Schema} object, not explicitly
     * by a call to {@link #add(String, Schema)}), or null.
     */
    protected abstract CalciteSchema getImplicitSubSchema(String schemaName,
                                                          boolean caseSensitive);

    /**
     * Returns a table with a given name that is defined implicitly
     * (that is, by the underlying {@link Schema} object, not explicitly
     * by a call to {@link #add(String, Table)}), or null.
     */
    protected abstract TableEntry getImplicitTable(String tableName,
                                                   boolean caseSensitive);

    /**
     * Returns table function with a given name and zero arguments that is
     * defined implicitly (that is, by the underlying {@link Schema} object,
     * not explicitly by a call to {@link #add(String, Function)}), or null.
     */
    protected abstract TableEntry getImplicitTableBasedOnNullaryFunction(String tableName,
                                                                         boolean caseSensitive);

    /**
     * Adds implicit sub-schemas to a builder.
     */
    protected abstract void addImplicitSubSchemaToBuilder(
            ImmutableSortedMap.Builder<String, CalciteSchema> builder);

    /**
     * Adds implicit tables to a builder.
     */
    protected abstract void addImplicitTableToBuilder(
            ImmutableSortedSet.Builder<String> builder);

    /**
     * Adds implicit functions to a builder.
     */
    protected abstract void addImplicitFunctionToBuilder(ImmutableList.Builder<Function> builder);

    /**
     * Adds implicit function names to a builder.
     */
    protected abstract void addImplicitFuncNamesToBuilder(
            ImmutableSortedSet.Builder<String> builder);

    /**
     * Adds implicit table functions to a builder.
     */
    protected abstract void addImplicitTablesBasedOnNullaryFunctionsToBuilder(
            ImmutableSortedMap.Builder<String, Table> builder);

    protected abstract boolean isCacheEnabled();

    public abstract void setCache(boolean cache);

    /**
     * Creates a TableEntryImpl with no SQLs.
     */
    public final TableEntryImpl tableEntry(String tableName, Table table) {
        return new TableEntryImpl(this, tableName, table, ImmutableList.<String>of());
    }

    /**
     * Defines a table within this schema.
     */
    public final TableEntry add(String tableName, Table table) {
        return add(tableName, table, ImmutableList.<String>of());
    }

    /**
     * Defines a table within this schema.
     */
    public final TableEntry add(String tableName, Table table,
                         ImmutableList<String> sqls) {
        final TableEntryImpl entry =
                new TableEntryImpl(this, tableName, table, sqls);
        tableMap.put(tableName, entry);
        return entry;
    }

    private FunctionEntry add(String funcName, Function function) {
        final FunctionEntryImpl entry =
                new FunctionEntryImpl(this, funcName, function);
        functionMap.put(funcName, entry);
        functionNames.add(funcName);
        if (function.getParameters().isEmpty()) {
            nullaryFunctionMap.put(funcName, entry);
        }
        return entry;
    }

    private LatticeEntry add(String latticeName, Lattice lattice) {
        if (latticeMap.containsKey(latticeName)) {
            throw new RuntimeException("Duplicate lattice '" + latticeName + "'");
        }
        final LatticeEntryImpl entry = new LatticeEntryImpl(this, latticeName, lattice);

        latticeMap.put(latticeName, entry);
        return entry;
    }

    /**
     * Adds a child schema of this schema.
     */
    public abstract CalciteSchema add(String schemaName, Schema childSchema);

    public final CalciteSchema root() {
        CalciteSchema s = this;
        while (s.parent != null) {
            s = s.parent;
        }
        return s;
    }

    /**
     * Returns whether this is a root schema.
     */
    public final boolean isRoot() {
        return parent == null;
    }

    /**
     * Returns the path of an object in this schema.
     */
    public final List<String> path(String objName) {
        final List<String> list = new ArrayList<>();
        if (objName != null) {
            list.add(objName);
        }
        for (CalciteSchema s = this; s != null; s = s.parent) {
            if (s.parent != null || !s.name.equals("")) {
                // Omit the root schema's name from the path if it's the empty string,
                // which it usually is.
                list.add(s.name);
            }
        }
        return ImmutableList.copyOf(Lists.reverse(list));
    }

    public final CalciteSchema getSubSchema(String schemaName,
                                            boolean caseSensitive) {
        if (caseSensitive) {
            // Check explicit schemas, case-sensitive.
            final CalciteSchema entry = subSchemaMap.get(schemaName);
            if (entry != null) {
                return entry;
            }
        } else {
            // Check explicit schemas, case-insensitive.
            //noinspection LoopStatementThatDoesntLoop
            for (Map.Entry<String, CalciteSchema> entry
                    : find(subSchemaMap, schemaName).entrySet()) {
                return entry.getValue();
            }
        }
        return getImplicitSubSchema(schemaName, caseSensitive);
    }

    /**
     * Returns a table that materializes the given SQL statement.
     */
    public final TableEntry getTableBySql(String sql) {
        for (TableEntry tableEntry : tableMap.values()) {
            if (tableEntry.sqls.contains(sql)) {
                return tableEntry;
            }
        }
        return null;
    }

    /**
     * Returns a table with the given name. Does not look for views.
     */
    public final TableEntry getTable(String tableName, boolean caseSensitive) {
        if (caseSensitive) {
            // Check explicit tables, case-sensitive.
            final TableEntry entry = tableMap.get(tableName);
            if (entry != null) {
                return entry;
            }
        } else {
            // Check explicit tables, case-insensitive.
            //noinspection LoopStatementThatDoesntLoop
            for (Map.Entry<String, TableEntry> entry
                    : find(tableMap, tableName).entrySet()) {
                return entry.getValue();
            }
        }

        return getImplicitTable(tableName, caseSensitive);
    }

    public final Schema getSchema() {
        return schema;
    }

    public final String getName() {
        return name;
    }

    public final SchemaPlus plus() {
        return new SchemaPlusImpl();
    }

    public static CalciteSchema from(SchemaPlus plus) {
        return ((SchemaPlusImpl) plus).calciteSchema();
    }

    /**
     * Returns the default path resolving functions from this schema.
     * <p/>
     * <p>The path consists is a list of lists of strings.
     * Each list of strings represents the path of a schema from the root schema.
     * For example, [[], [foo], [foo, bar, baz]] represents three schemas: the
     * root schema "/" (level 0), "/foo" (level 1) and "/foo/bar/baz" (level 3).
     *
     * @return Path of this schema; never null, may be empty
     */
    public final List<? extends List<String>> getPath() {
        if (path != null) {
            return path;
        }
        // Return a path consisting of just this schema.
        return ImmutableList.of(path(null));
    }

    /**
     * Returns a collection of sub-schemas, both explicit (defined using
     * {@link #add(String, org.apache.calcite.schema.Schema)}) and implicit
     * (defined using {@link org.apache.calcite.schema.Schema#getSubSchemaNames()}
     * and {@link Schema#getSubSchema(String)}).
     */
    public final NavigableMap<String, CalciteSchema> getSubSchemaMap() {
        // Build a map of implicit sub-schemas first, then explicit sub-schemas.
        // If there are implicit and explicit with the same name, explicit wins.
        final ImmutableSortedMap.Builder<String, CalciteSchema> builder =
                new ImmutableSortedMap.Builder<>(COMPARATOR);
        builder.putAll(subSchemaMap);
        addImplicitSubSchemaToBuilder(builder);
        return Compatible.INSTANCE.navigableMap(builder.build());
    }

    /**
     * Returns a collection of lattices.
     * <p/>
     * <p>All are explicit (defined using {@link #add(String, Lattice)}).
     */
    public final NavigableMap<String, LatticeEntry> getLatticeMap() {
        return Compatible.INSTANCE.immutableNavigableMap(latticeMap);
    }

    /**
     * Returns the set of all table names. Includes implicit and explicit tables
     * and functions with zero parameters.
     */
    public final NavigableSet<String> getTableNames() {
        final ImmutableSortedSet.Builder<String> builder =
                new ImmutableSortedSet.Builder<>(COMPARATOR);
        // Add explicit tables, case-sensitive.
        builder.addAll(tableMap.keySet());
        // Add implicit tables, case-sensitive.
        addImplicitTableToBuilder(builder);
        return Compatible.INSTANCE.navigableSet(builder.build());
    }

    /**
     * Returns a collection of all functions, explicit and implicit, with a given
     * name. Never null.
     */
    public final Collection<Function> getFunctions(String funcName, boolean caseSensitive) {
        final ImmutableList.Builder<Function> builder = ImmutableList.builder();

        if (caseSensitive) {
            // Add explicit functions, case-sensitive.
            final Collection<FunctionEntry> functionEntries = functionMap.get(funcName);
            if (functionEntries != null) {
                for (FunctionEntry functionEntry : functionEntries) {
                    builder.add(functionEntry.getFunction());
                }
            }
            // Add implicit functions, case-sensitive.
            final Collection<Function> functions = schema.getFunctions(funcName);
            if (functions != null) {
                builder.addAll(functions);
            }
        } else {
            // Add explicit functions, case-insensitive.
            for (String name2 : find(functionNames, funcName)) {
                final Collection<FunctionEntry> functionEntries =
                        functionMap.get(name2);
                if (functionEntries != null) {
                    for (FunctionEntry functionEntry : functionEntries) {
                        builder.add(functionEntry.getFunction());
                    }
                }
            }
            // Add implicit functions, case-insensitive.
            addImplicitFunctionToBuilder(builder);
        }
        return builder.build();
    }

    /**
     * Returns the list of function names in this schema, both implicit and
     * explicit, never null.
     */
    public final NavigableSet<String> getFunctionNames() {
        final ImmutableSortedSet.Builder<String> builder =
                new ImmutableSortedSet.Builder<>(COMPARATOR);
        // Add explicit functions, case-sensitive.
        builder.addAll(functionMap.keySet());
        // Add implicit functions, case-sensitive.
        addImplicitFuncNamesToBuilder(builder);
        return Compatible.INSTANCE.navigableSet(builder.build());
    }

    /**
     * Returns tables derived from explicit and implicit functions
     * that take zero parameters.
     */
    public final NavigableMap<String, Table> getTablesBasedOnNullaryFunctions() {
        ImmutableSortedMap.Builder<String, Table> builder =
                new ImmutableSortedMap.Builder<>(COMPARATOR);
        for (Map.Entry<String, FunctionEntry> s : nullaryFunctionMap.entrySet()) {
            final Function function = s.getValue().getFunction();
            if (function instanceof TableMacro) {
                assert function.getParameters().isEmpty();
                final Table table = ((TableMacro) function).apply(ImmutableList.of());
                builder.put(s.getKey(), table);
            }
        }
        // add tables derived from implicit functions
        addImplicitTablesBasedOnNullaryFunctionsToBuilder(builder);
        return Compatible.INSTANCE.navigableMap(builder.build());
    }

    /**
     * Returns a tables derived from explicit and implicit functions
     * that take zero parameters.
     */
    public final TableEntry getTableBasedOnNullaryFunction(String tableName,
                                                           boolean caseSensitive) {
        if (caseSensitive) {
            final FunctionEntry functionEntry = nullaryFunctionMap.get(tableName);
            if (functionEntry != null) {
                final Function function = functionEntry.getFunction();
                if (function instanceof TableMacro) {
                    assert function.getParameters().isEmpty();
                    final Table table = ((TableMacro) function).apply(ImmutableList.of());
                    return tableEntry(tableName, table);
                }
            }
            for (Function function : schema.getFunctions(tableName)) {
                if (function instanceof TableMacro
                        && function.getParameters().isEmpty()) {
                    final Table table = ((TableMacro) function).apply(ImmutableList.of());
                    return tableEntry(tableName, table);
                }
            }
        } else {
            for (Map.Entry<String, FunctionEntry> entry
                    : find(nullaryFunctionMap, tableName).entrySet()) {
                final Function function = entry.getValue().getFunction();
                if (function instanceof TableMacro) {
                    assert function.getParameters().isEmpty();
                    final Table table = ((TableMacro) function).apply(ImmutableList.of());
                    return tableEntry(tableName, table);
                }
            }
            TableEntry tableEntry =
                    getImplicitTableBasedOnNullaryFunction(tableName, false);
        }
        return null;
    }

    /**
     * Returns a subset of a map whose keys match the given string
     * case-insensitively.
     */
    protected static <V> NavigableMap<String, V> find(NavigableMap<String, V> map,
                                                      String s) {
        assert map.comparator() == COMPARATOR;
        return map.subMap(s.toUpperCase(), true, s.toLowerCase(), true);
    }

    /**
     * Returns a subset of a set whose values match the given string
     * case-insensitively.
     */
    protected static Iterable<String> find(NavigableSet<String> set, String name) {
        assert set.comparator() == COMPARATOR;
        return set.subSet(name.toUpperCase(), true, name.toLowerCase(), true);
    }

    /**
     * Creates a root schema.
     * <p/>
     * <p>When <code>addMetadataSchema</code> argument is true adds a "metadata"
     * schema containing definitions of tables, columns etc. to root schema.
     * By default, creates a {@link CachingCalciteSchema}.
     */
    public static CalciteSchema createRootSchema(boolean addMetadataSchema) {
        return createRootSchema(addMetadataSchema, true);
    }

    /**
     * Creates a root schema.
     *
     * @param addMetadataSchema Whether to add a "metadata" schema containing
     *                          definitions of tables, columns etc.
     * @param cache             If true create {@link CachingCalciteSchema};
     *                          if false create {@link SimpleCalciteSchema}
     */
    public static CalciteSchema createRootSchema(boolean addMetadataSchema,
                                                 boolean cache) {
        CalciteSchema rootSchema;
        final Schema schema = new CalciteConnectionImpl.RootSchema();
        if (cache) {
            rootSchema = new CachingCalciteSchema(null, schema, "");
        } else {
            rootSchema = new SimpleCalciteSchema(null, schema, "");
        }
        if (addMetadataSchema) {
            rootSchema.add("metadata", MetadataSchema.INSTANCE);
        }
        return rootSchema;
    }

    /**
     * Entry in a schema, such as a table or sub-schema.
     * <p/>
     * <p>Each object's name is a property of its membership in a schema;
     * therefore in principle it could belong to several schemas, or
     * even the same schema several times, with different names. In this
     * respect, it is like an inode in a Unix file system.</p>
     * <p/>
     * <p>The members of a schema must have unique names.
     */
    public abstract static class Entry {
        private final CalciteSchema schema;
        private final String name;

        public Entry(CalciteSchema schema, String name) {
            this.schema = Preconditions.checkNotNull(schema);
            this.name = Preconditions.checkNotNull(name);
        }

        /**
         * Returns this object's path. For example ["hr", "emps"].
         */
        public final List<String> path() {
            return schema.path(name);
        }
    }

    /**
     * Membership of a table in a schema.
     */
    public abstract static class TableEntry extends Entry {
        private final List<String> sqls;

        public TableEntry(CalciteSchema schema, String name,
                          ImmutableList<String> sqls) {
            super(schema, name);
            this.sqls = Preconditions.checkNotNull(sqls);
        }

        public abstract Table getTable();
    }

    /**
     * Membership of a function in a schema.
     */
    public abstract static class FunctionEntry extends Entry {
        public FunctionEntry(CalciteSchema schema, String name) {
            super(schema, name);
        }

        public abstract Function getFunction();

        /**
         * Whether this represents a materialized view. (At a given point in time,
         * it may or may not be materialized as a table.)
         */
        public abstract boolean isMaterialization();
    }

    /**
     * Membership of a lattice in a schema.
     */
    public abstract static class LatticeEntry extends Entry {
        public LatticeEntry(CalciteSchema schema, String name) {
            super(schema, name);
        }

        public abstract Lattice getLattice();

        public abstract TableEntry getStarTable();
    }

    /**
     * Implementation of {@link SchemaPlus} based on a
     * {@link CalciteSchema}.
     */
    private class SchemaPlusImpl implements SchemaPlus {
        CalciteSchema calciteSchema() {
            return CalciteSchema.this;
        }

        public SchemaPlus getParentSchema() {
            return parent == null ? null : parent.plus();
        }

        public String getName() {
            return CalciteSchema.this.getName();
        }

        public boolean isMutable() {
            return schema.isMutable();
        }

        public void setCacheEnabled(boolean cache) {
            CalciteSchema.this.setCache(cache);
        }

        public boolean isCacheEnabled() {
            return CalciteSchema.this.isCacheEnabled();
        }

        public boolean contentsHaveChangedSince(long lastCheck, long now) {
            return schema.contentsHaveChangedSince(lastCheck, now);
        }

        public Expression getExpression(SchemaPlus parentSchema, String exprName) {
            return schema.getExpression(parentSchema, exprName);
        }

        public Table getTable(String tableName) {
            final TableEntry entry = CalciteSchema.this.getTable(tableName, true);
            return entry == null ? null : entry.getTable();
        }

        public NavigableSet<String> getTableNames() {
            return CalciteSchema.this.getTableNames();
        }

        public Collection<Function> getFunctions(String funcName) {
            return CalciteSchema.this.getFunctions(funcName, true);
        }

        public NavigableSet<String> getFunctionNames() {
            return CalciteSchema.this.getFunctionNames();
        }

        public SchemaPlus getSubSchema(String schemaName) {
            final CalciteSchema subSchema =
                    CalciteSchema.this.getSubSchema(schemaName, true);
            return subSchema == null ? null : subSchema.plus();
        }

        public Set<String> getSubSchemaNames() {
            return CalciteSchema.this.getSubSchemaMap().keySet();
        }

        public SchemaPlus add(String schemaName, Schema childSchema) {
            final CalciteSchema calciteSchema = CalciteSchema.this.add(schemaName, childSchema);
            return calciteSchema.plus();
        }

        public void add(String tableName, Table table) {
            CalciteSchema.this.add(tableName, table);
        }

        public void add(String funcName, Function function) {
            CalciteSchema.this.add(funcName, function);
        }

        public void add(String latticeName, Lattice lattice) {
            CalciteSchema.this.add(latticeName, lattice);
        }

        public <T> T unwrap(Class<T> clazz) {
            if (clazz.isInstance(this)) {
                return clazz.cast(this);
            }
            if (clazz.isInstance(CalciteSchema.this)) {
                return clazz.cast(CalciteSchema.this);
            }
            if (clazz.isInstance(CalciteSchema.this.schema)) {
                return clazz.cast(CalciteSchema.this.schema);
            }
            throw new ClassCastException("not a " + clazz);
        }

        public void setPath(ImmutableList<ImmutableList<String>> path) {
            CalciteSchema.this.path = path;
        }
    }

    /**
     * Implementation of {@link CalciteSchema.TableEntry}
     * where all properties are held in fields.
     */
    public static final class TableEntryImpl extends TableEntry {
        private final Table table;

        /**
         * Creates a TableEntryImpl.
         */
        public TableEntryImpl(CalciteSchema schema, String name, Table table,
                              ImmutableList<String> sqls) {
            super(schema, name, sqls);
            assert table != null;
            this.table = Preconditions.checkNotNull(table);
        }

        public Table getTable() {
            return table;
        }
    }

    /**
     * Implementation of {@link FunctionEntry}
     * where all properties are held in fields.
     */
    public static final class FunctionEntryImpl extends FunctionEntry {
        private final Function function;

        /**
         * Creates a FunctionEntryImpl.
         */
        public FunctionEntryImpl(CalciteSchema schema, String name,
                                 Function function) {
            super(schema, name);
            this.function = function;
        }

        public Function getFunction() {
            return function;
        }

        public boolean isMaterialization() {
            return function
                    instanceof MaterializedViewTable.MaterializedViewTableMacro;
        }
    }

    /**
     * Implementation of {@link LatticeEntry}
     * where all properties are held in fields.
     */
    public static final class LatticeEntryImpl extends LatticeEntry {
        private final Lattice lattice;
        private final CalciteSchema.TableEntry starTableEntry;

        /**
         * Creates a LatticeEntryImpl.
         */
        public LatticeEntryImpl(CalciteSchema schema, String name,
                                Lattice lattice) {
            super(schema, name);
            this.lattice = lattice;

            // Star table has same name as lattice and is in same schema.
            final StarTable starTable = lattice.createStarTable();
            starTableEntry = schema.add(name, starTable);
        }

        public Lattice getLattice() {
            return lattice;
        }

        public TableEntry getStarTable() {
            return starTableEntry;
        }
    }
}
