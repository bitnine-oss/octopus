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

grammar OctopusSql;

@header {
    import org.apache.commons.lang3.StringUtils;
}

ddl
    : ( ddlStmts | error ) EOF
    ;

ddlStmts
    : ';'* ddlStmt ( ';'+ ddlStmt )* ';'*
    ;

ddlStmt
    : alterSystem
    | createUser
    | alterUser
    | dropUser
    | createRole
    | dropRole
    | showTables
    ;

alterSystem
    : K_ALTER K_SYSTEM dataSourceClause
    ;

dataSourceClause
    : K_ADD K_DATASOURCE dataSourceName K_CONNECT K_BY jdbcConnectionString
    ;

dataSourceName
    : IDENTIFIER
    ;

jdbcConnectionString
    : STRING_LITERAL
    ;

createUser
    : K_CREATE K_USER user K_IDENTIFIED K_BY password
    ;

alterUser
    : K_ALTER K_USER user K_IDENTIFIED K_BY password ( K_REPLACE oldPassword )?
    ;

dropUser
    : K_DROP K_USER user
    ;

user
    : IDENTIFIER
    ;

password
    : STRING_LITERAL
    ;

oldPassword
    : STRING_LITERAL
    ;

createRole
    : K_CREATE K_ROLE role
    ;

dropRole
    : K_DROP K_ROLE role
    ;

role
    : IDENTIFIER
    ;

showDataSources
    : K_SHOW K_DATASOURCES
    ;

showSchemas
    : K_SHOW K_SCHEMAS (K_DATASOURCE datasource)? (K_SCHEMA schemapattern)?
    ;

showTables
    : K_SHOW K_TABLES (K_DATASOURCE datasource)? (K_SCHEMA schemapattern)? (K_TABLE tablepattern)?
    ;

showColumns
    : K_SHOW K_COLUMNS (K_DATASOURCE datasource)? (K_SCHEMA schemapattern)? (K_TABLE tablepattern)? (K_COLUMN columnpattern)?
    ;

showTablePrivileges
    : K_SHOW K_TABLE K_PRIVILEGES (K_DATASOURCE datasource)? (K_SCHEMA schemapattern)? (K_TABLE tablepattern)?
    ;

showColumnPrivileges
    : K_SHOW K_COLUMN K_PRIVILEGES (K_DATASOURCE datasource)? (K_SCHEMA schemapattern)? (K_TABLE tablepattern)? (K_COLUMN columnpattern)?
    ;

showUsers
    : K_SHOW K_USERS
    ;

datasource
    : IDENTIFIER
    ;

schemapattern
    : IDENTIFIER
    ;

tablepattern
    : IDENTIFIER
    ;

columnpattern
    : IDENTIFIER
    ;

error
    : UNEXPECTED_CHAR
        {
            throw new RuntimeException("UNEXPECTED_CHAR=" + $UNEXPECTED_CHAR.text);
        }
    ;

K_ADD : A D D ;
K_ALTER : A L T E R ;
K_BY : B Y ;
K_CONNECT : C O N N E C T ;
K_CREATE : C R E A T E ;
K_DATASOURCE : D A T A S O U R C E ;
K_SCHEMA : S C H E M A ;
K_TABLE : T A B L E ;
K_COLUMN : C O L U M N ;
K_DROP : D R O P ;
K_IDENTIFIED : I D E N T I F I E D ;
K_REPLACE : R E P L A C E ;
K_ROLE : R O L E ;
K_SYSTEM : S Y S T E M ;
K_USER : U S E R ;
K_SHOW : S H O W ;
K_DATASOURCES : D A T A S O U R C E S ;
K_SCHEMAS : S C H E M A S ;
K_TABLES : T A B L E S ;
K_COLUMNS : C O L U M N S ;
K_PRIVILEGES : P R I V I L E G E S ;
K_USERS : U S E R S ;

IDENTIFIER
    : '"' ( ~["\r\n] | '""' )* '"'
        {
            setText(StringUtils.strip(getText(), "\"").replace("\"\"", "\""));
        }
    | '`' ( ~[`\r\n] | '``' )* '`'
        {
            setText(StringUtils.strip(getText(), "`").replace("``", "`"));
        }
    | '[' ~[\]\r\n]* ']'
    | LETTER ( LETTER | DIGIT )*
    ;

STRING_LITERAL
    : '\'' ( ~['\r\n] | '\'\'' )* '\''
        {
            setText(StringUtils.strip(getText(), "'").replace("''", "'"));
        }
    ;

WHITESPACES : [ \t\r\n]+ -> channel(HIDDEN) ;

UNEXPECTED_CHAR : . ;

fragment A : [aA] ;
fragment B : [bB] ;
fragment C : [cC] ;
fragment D : [dD] ;
fragment E : [eE] ;
fragment F : [fF] ;
fragment G : [gG] ;
fragment H : [hH] ;
fragment I : [iI] ;
fragment J : [jJ] ;
fragment K : [kK] ;
fragment L : [lL] ;
fragment M : [mM] ;
fragment N : [nN] ;
fragment O : [oO] ;
fragment P : [pP] ;
fragment Q : [qQ] ;
fragment R : [rR] ;
fragment S : [sS] ;
fragment T : [tT] ;
fragment U : [uU] ;
fragment V : [vV] ;
fragment W : [wW] ;
fragment X : [xX] ;
fragment Y : [yY] ;
fragment Z : [zZ] ;

fragment LETTER : [a-zA-Z_] ;
fragment DIGIT : [0-9] ;