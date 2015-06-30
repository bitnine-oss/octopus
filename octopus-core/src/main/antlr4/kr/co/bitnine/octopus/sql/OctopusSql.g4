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
    ;

alterSystem
    : K_ALTER K_SYSTEM datasourceClause
    ;

datasourceClause
    : K_ADD K_DATASOURCE datasourceName K_CONNECT K_BY jdbcConnectionString
    ;

createUser
    : K_CREATE K_USER user K_IDENTIFIED K_BY password
    ;

datasourceName
    : IDENTIFIER
    ;

jdbcConnectionString
    : STRING_LITERAL
    ;

user
    : IDENTIFIER
    ;

password
    : STRING_LITERAL
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
K_IDENTIFIED : I D E N T I F I E D ;
K_SYSTEM : S Y S T E M ;
K_USER : U S E R ;

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