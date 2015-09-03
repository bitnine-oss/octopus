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
    | grant
    | revoke
    | show
    | commentOn
    | setDataCategoryOn
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

grant
    : K_GRANT grantSystemPrivileges
    | K_GRANT grantObjectPrivileges
    ;

grantSystemPrivileges
    : systemPrivileges K_TO grantees
    ;

grantObjectPrivileges
    : objectPrivileges K_ON object K_TO grantees
    ;

revoke
    : K_REVOKE revokeSystemPrivileges
    | K_REVOKE revokeObjectPrivileges
    ;

revokeSystemPrivileges
    : systemPrivileges K_FROM grantees
    ;

revokeObjectPrivileges
    : objectPrivileges K_ON object K_FROM grantees
    ;

systemPrivileges
    : systemPrivilege ( ',' systemPrivilege )*
    ;

systemPrivilege
    : K_ALTER K_SYSTEM                      # SysPrivAlterSystem
    | K_SELECT K_ANY K_TABLE                # SysPrivSelectAnyTable
    | K_CREATE K_USER                       # SysPrivCreateUser
    | K_ALTER K_USER                        # SysPrivAlterUser
    | K_DROP K_USER                         # SysPrivDropUser
    | K_COMMENT K_ANY                       # SysPrivCommentAny
    | K_GRANT K_ANY K_OBJECT K_PRIVILEGE    # SysPrivGrantAnyObjPriv
    | K_GRANT K_ANY K_PRIVILEGE             # SysPrivGrantAnyPriv
    | K_ALL K_PRIVILEGES                    # SysPrivAllPrivs
    | role                                  # SysPrivRole
    ;

objectPrivileges
    : objectPrivilege ( ',' objectPrivilege )*
    ;

objectPrivilege
    : K_SELECT                  # ObjPrivSelect
    | K_COMMENT                 # ObjPrivComment
    | K_ALL ( K_PRIVILEGES )?   # ObjPrivAllPrivs
    ;

grantees
    : grantee ( ',' grantee )*
    ;

object
    : schemaName
    ;

// user or role
grantee
    : IDENTIFIER
    ;

show
    : K_SHOW showTargets
    ;

showTargets
    : K_DATASOURCES                                                                                                                             # ShowDataSources
    | K_SCHEMAS ( K_DATASOURCE dataSourceName )? ( K_SCHEMA schemaPattern )?                                                                    # ShowSchemas
    | K_TABLES ( K_DATASOURCE dataSourceName )? ( K_SCHEMA schemaPattern )? ( K_TABLE tablePattern )?                                           # ShowTables
    | K_COLUMNS ( K_DATASOURCE dataSourceName )? ( K_SCHEMA schemaPattern )? ( K_TABLE tablePattern )? ( K_COLUMN columnPattern )?              # ShowColumns
    | K_TABLE K_PRIVILEGES ( K_DATASOURCE dataSourceName )? ( K_SCHEMA schemaPattern )? ( K_TABLE tablePattern )?                               # ShowTablePrivileges
    | K_COLUMN K_PRIVILEGES ( K_DATASOURCE dataSourceName )? ( K_SCHEMA schemaPattern )? ( K_TABLE tablePattern )? ( K_COLUMN columnPattern )?  # ShowColumnPrivileges
    | K_USERS                                                                                                                                   # ShowUsers
    ;

schemaPattern
    : STRING_LITERAL
    ;

tablePattern
    : STRING_LITERAL
    ;

columnPattern
    : STRING_LITERAL
    ;

commentOn
    : K_COMMENT K_ON commentOnTarget K_IS comment
    ;

comment
    : STRING_LITERAL
    ;

commentOnTarget
    : K_DATASOURCE dataSourceName                                           # CommentDataSource
    | K_SCHEMA dataSourceName '.' schemaName                                # CommentSchema
    | K_TABLE dataSourceName '.' schemaName '.' tableName                   # CommentTable
    | K_COLUMN dataSourceName '.' schemaName '.' tableName '.' columnName   # CommentColumn
    | K_USER user                                                           # CommentUser
    ;

schemaName
    : IDENTIFIER
    ;

tableName
    : IDENTIFIER
    ;

columnName
    : IDENTIFIER
    ;

setDataCategoryOn
    : K_SET K_DATACATEGORY K_ON K_COLUMN dataSourceName '.' schemaName '.' tableName '.' columnName K_IS category
    ;

category
    : STRING_LITERAL
    ;

error
    : UNEXPECTED_CHAR
        {
            throw new RuntimeException("UNEXPECTED_CHAR=" + $UNEXPECTED_CHAR.text);
        }
    ;

K_ADD : A D D ;
K_ALL : A L L ;
K_ALTER : A L T E R ;
K_ANY : A N Y ;
K_BY : B Y ;
K_COLUMN : C O L U M N ;
K_COLUMNS : C O L U M N S ;
K_COMMENT : C O M M E N T ;
K_CONNECT : C O N N E C T ;
K_CREATE : C R E A T E ;
K_DATACATEGORY : D A T A C A T E G O R Y ;
K_DATASOURCE : D A T A S O U R C E ;
K_DATASOURCES : D A T A S O U R C E S ;
K_DROP : D R O P ;
K_FROM : F R O M ;
K_GRANT : G R A N T ;
K_IDENTIFIED : I D E N T I F I E D ;
K_IS : I S ;
K_OBJECT : O B J E C T ;
K_ON : O N ;
K_PRIVILEGE : P R I V I L E G E ;
K_PRIVILEGES : P R I V I L E G E S ;
K_REPLACE : R E P L A C E ;
K_REVOKE : R E V O K E ;
K_ROLE : R O L E ;
K_SCHEMA : S C H E M A ;
K_SCHEMAS : S C H E M A S ;
K_SELECT : S E L E C T ;
K_SET : S E T ;
K_SHOW : S H O W ;
K_SYSTEM : S Y S T E M ;
K_TABLE : T A B L E ;
K_TABLES : T A B L E S ;
K_TO : T O ;
K_USER : U S E R ;
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