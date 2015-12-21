
SQL
===
이 장에서는 Octopus에서 지원하는 SQL에 대해 기술한다.

Data Type
----------
현재 Octopus에서 지원하는 데이터 타입은 아래와 같다.

.. _making-a-table:

===========  ============== ===================================================================================================
SQL type        크기(bytes)         범위
===========  ============== ===================================================================================================
INTEGER         4                    -231 (-2,147,483,648) ~ 231 - 1 (2,147,483,647)
BIGINT          8                    -263 (-9,223,372,036,854,775,808) ~ 263 - 1 (9,223,372,036,854,775,807)
FLOAT           4                    -3.4E+38 ~ 3.4E+38
REAL            4                    -3.4E+38 ~ 3.4E+38
DOUBLE          8                    -1.7E–308 ~ 1.7E+308
NUMERIC         -                    -
VARCHAR         -                    -
DATE            -                    YYYY-mm-dd 형식
TIMESTAMP       -                    YYYY-mm-dd HH:MM:SS[.NNNNNNNNN] 형식
===========  ============== ===================================================================================================

Data Definition Language
---------------------------
ALTER SYSTEM ADD DATASOURCE
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
Octopus에서 관리할 데이터소스를 추가한다. JDBC 연결을 지원하는 데이터소스만 추가할 수 있다.

.. code-block:: bash

    ALTER SYSTEM ADD DATASOURCE "<dataSourceName>"
      CONNECT TO '<jdbcConnectionString>'
      USING '<jdbcDriverName>' ;

**dataSourceName**

추가할 데이터소스의 이름을 지정.

**jdbcConnectionString**

데이터소스의 JDBC 연결 주소.

**jdbcDriverName**

데이터소스 연결에 사용할 JDBC 드라이버 이름.

- 예제

.. code-block:: bash

    ALTER SYSTEM ADD DATASOURCE "spark"
      CONNECT TO 'jdbc:hive2://localhost:10001'
      USING 'org.apache.hive.JDBC' ;

ALTER SYSTEM UPDATE DATASOURCE
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
Octopus에서 관리중인 데이터소스의 스키마 정보가 변경되었을 경우, 이를 Octopus의 메타스토어에 반영한다. 데이터소스 전체, 특정 스키마만, 특정 테이블만 갱신할 수 있도록 다양한 문법을 지원한다.

.. code-block:: bash

    ALTER SYSTEM UPDATE DATASOURCE "<dataSourceName>" ;
    ALTER SYSTEM UPDATE SCHEMA "<dataSourceName>" . 'schemaPattern' ;
    ALTER SYSTEM UPDATE TABLE "<dataSourceName>" . 'schemaPattern' . 'tablePattern' ;

**dataSourceName**

갱신할 데이터소스의 이름을 지정.

**schemaPattern, tablePattern**

갱신할 스키마, 테이블 패턴을 지정. 패턴은 SQL의 LIKE 패턴과 동일.

- 예제

.. code-block:: bash

    ALTER SYSTEM UPDATE DATASOURCE "spark";
    ALTER SYSTEM UPDATE SCHEMA "spark".'div%';
    ALTER SYSTEM UPDATE TABLE "spark".'%'.'%stat%';

ALTER SYSTEM DROP DATASOURCE
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
Octopus의 메타스토어에서 데이터소스를 삭제한다. 삭제된 데이터소스에 대한 스키마 정보 조회와 질의 처리를 더 이상 할 수 없다.

.. code-block:: bash

    ALTER SYSTEM DROP DATASOURCE "<dataSourceName>" ;

**dataSourceName**

삭제할 데이터소스의 이름을 지정.

- 예제

.. code-block:: bash

    ALTER SYSTEM DROP DATASOURCE "spark";

Data Manipulation Language
-----------------------------
Octopus는 여러 데이터소스에 대한 SELECT 질의만 지원하며 INSERT, UPDATE, DELETE는 지원하지 않는다. Octopus는 아래와 같은 SELECT 문을 지원한다. 단, by-pass 질의의 경우 Octopus의 특성상 데이터 소스가 지원하지 않는 문법은 처리할 수 없다.

.. code-block:: bash

    query:
      [ WITH withItem { , withItem }* query ]
    | { select
      | query UNION [ ALL ] query
      | query EXCEPT query
      | query INTERSECT query
      }
      [ ORDER BY orderItem { , orderItem }* ]
      [ LIMIT { <count> | ALL } ]
      [ OFFSET <start> { ROW | ROWS } ]
      [ FETCH { FIRST | NEXT } [ <count> ] { ROW | ROWS } ]
    
    withItem: <withName> [ ( <withColumn> { , <withColumn> }* ) ] AS ( query )
    
    orderItem: <expression> [ ASC | DESC ] [ NULLS FIRST | NULLS LAST ]
    
    select:
      SELECT [ STREAM ] [ ALL | DISTINCT ]
      { '*' | projectItem { , projectItem }* }
      FROM tableExpression
      [ WHERE <booleanExpression> ]
      [ GROUP BY groupItem { , groupItem }* ]
      [ HAVING <booleanExpression> ]
    
    projectItem:
      <expression> [ [ AS ] <columnAlias> ]
    | <tableAlias> . '*'
    
    tableExpression:
      tableReference { , tableReference }*
    | tableExpression [ NATURAL ] [ LEFT | RIGHT | FULL ] JOIN 
      tableExpression [ joinCondition ]
    
    joinCondition: ON <booleanExpression> | USING ( <column> { , <column> }* )
    
    tableReference:
      [ LATERAL ] tablePrimary
      [ [ AS ] <tableAlias> [ ( <columnAlias> { , <columnAlias> }* ) ] ]
    
    tablePrimary:
      [ TABLE ] [ [ <dataSourceName> . ] <schemaName> . ] <tableName>
      | ( query )
      | values
      | UNNEST ( <expression> )
      | TABLE ( [ SPECIFIC ] functionName ( <expression> { , <expression> }* ) )
    
    values: VALUES <expression> { , <expression> }*
    
    groupItem:
      <expression>
    | ( )
    | ( <expression. { , expression }* )
    | CUBE ( <expression> { , expression }* )
    | ROLLUP ( expression { , expression }* )
    | GROUPING SETS ( groupItem { , groupItem }* )

**count, start**

몇 개의 결과를 가져올지, 몇 번째 결과부터 가져올지 지정 (0 이상의 정수)

**withName, withColumn**

WITH 절의 이름과 결과 칼럼 이름을 지정 (식별자 형식)

**columnAlias, tableAlias**

칼럼, 테이블의 별칭을 지정 (식별자 형식)

**dataSourceName, schemaName, tableName**

데이터소스, 스키마, 테이블의 이름을 지정 (식별자 형식)

- expression

산술 표현식, 비교 표현식, 혹은 논리 표현식이 올 수 있다. 표현식에서 사용하는 값은 특정 칼럼의 값을 해당 칼럼의 식별자로 얻어오거나, 상수를 직접 입력할 수 있다. Octopus에서는 아래와 같은 산술 연산자, 비교 연산자들을 지원하며, 각 표현식에서 사용할 수 있다.

.. _making-b-table:

========================= ========================================= ===================
산술 연산자                     설명                                     결과
========================= ========================================= ===================
numeric1 + numeric2           numeric1과 numeric2의 합               numeric
numeric1 - numeric2           numeric1에서 numeric2를 뺌               numeric
numeric1 * numeric2           numeric1과 numeric2의 곱               numeric
numeric1 / numeric2           numeric1을 numeric2로 나눈 몫         numeric
========================= ========================================= ===================

.. _making-c-table:

============================ ==========================================   =================================
비교 연산자                     설명                                        결과
============================ ==========================================   =================================
value1 = value2               같음                                        boolean
value1 <> value2              같지 않음                                     boolean
value1 > value2               큼                                             boolean
value1 >= value2              크거나 같음                                     boolean
value1 < value2               작음                                            boolean
value1 <= value2              작거나 같음                                     boolean
value IS NULL                 value가 NULL                                   boolean
value IS NOT NULL             value가 NULL이 아님                            boolean
string1 LIKE string2          string1이 패턴 string2와 맞음                boolean
string1 NOT LIKE string2      string1이 패턴 string2와 맞지 않음          boolean
============================ ==========================================   =================================

- booleanExpression (논리 표현식)

논리 연산자들의 AND, OR 조합으로 이루어진 표현식이다. Octopus에서는 아래와 같은 논리 연산자들을 지원한다.

.. _making-d-table:

========================== ============================================================= ===========================
연산자                        설명                                                             결과
========================== ============================================================= ===========================
boolean1 OR boolean2          boolean1 혹은 boolean2가 참                                boolean
boolean1 AND boolean2         boolean2 그리고 booelan2가 참                                boolean
NOT boolean                   boolean이 참이 아님; boolean이 UNKNOWN이면 UNKNOWN              boolean
boolean IS FALSE              boolean이 거짓; boolean이 UNKNOWN이면 거짓                     boolean
boolean IS NOT FALSE          boolean이 거짓이 아님; boolean이 UNKNOWN이면 참                        boolean
boolean IS TRUE               boolean이 참; boolean이 UNKNOWN이면 거짓                       boolean
boolean IS NOT TRUE           boolean이 참이 아님; boolean이 UNKNOWN이면 참                      boolean
boolean IS UNKNOWN            boolean이 UNKNOWN                                           boolean
boolean IS NOT UNKNOWN        boolean이 UNKNOWN이 아님                                        boolean
========================== ============================================================= ===========================

Octopus Administration Statements
---------------------------------
Octopus에는 사용자 계정 개념이 있다. 또한 사용자 계정이 특정 스키마에 대한 접근 권한이 있는지 확인하고, 접근 권한을 부여할 수 있도록 하는 접근 제어 기능을 제공한다. 그리고 특정 스키마 정보에 주석을 추가할 수 있다. 마지막으로 각종 정보들을 조회할 수 있는 기능을 제공한다. 이 절에서는 이와 같은 Octopus 관리에 필요한 문장들에 대해 살펴본다.

사용자 계정
^^^^^^^^^^^

- 사용자 계정 생성 및 수정

아래 두 문장은 각각 새로운 Octopus 사용자 계정을 생성 및 수정한다. 생성된 계정은 어떠한 권한도 가지고 있지 않는다.

.. code-block:: bash

    CREATE USER "<user>" IDENTIFIED BY '<password>' ;
    
    ALTER USER "<user>" IDENTIFIED BY '<password>' ;

**user**

생성/수정할 계정 이름을 지정.

**password**

새로운 계정의 암호 혹은 수정할 암호 지정.

    - 예제

.. code-block:: bash

    CREATE USER "octopus" IDENTIFIED BY 'bitnine';
    
    ALTER USER "octopus" IDENTIFIED BY 'squid';

- 사용자 계정 삭제

.. code-block:: bash

    DROP USER "<user>" ;

**user**

삭제할 계정을 지정.

    - 예제

.. code-block:: bash
    
    DROP USER "octopus";

권한
^^^^
먼저 시스템 권한을 부여하고 제거하는 문장에 대해 알아본다.

.. code-block:: bash

    GRANT systemPrivileges TO grantees ;
    
    REVOKE systemPrivileges FROM grantees ;
    
    systemPrivileges: systemPrivilege { , systemPrivilege }*
    grantees: grantee { , grantee }*
    grantee: '<user>'

**user**

권한을 부여 받을 대상 계정.

**systemPrivilege**

아래 표에 있는 시스템 권한들을 지원한다.

.. _making-e-table:

=============================== ====================================================
권한                               설명
=============================== ====================================================
ALTER SYSTEM                      ALTER SYSTEM 문장 실행 권한
SELECT ANY TABLE                  모든 테이블에 대한 SELECT 질의 권한
CREATE USER                       사용자 계정 생성 권한
ALTER USER                        사용자 계정 수정 권한
DROP USER                         사용자 계정 삭제 권한
COMMENT ANY                       모든 대상에 대한 주석 및 칼럼 분류 추가 권한
GRANT ANY OBJECT PRIVILEGE        객체 권한 부여 권한
GRANT ANY PRIVILEGE               시스템 권한 부여 권한
ALL PRIVILEGES                    위의 모든 시스템 권한
=============================== ====================================================

    - 예제

.. code-block:: bash


    GRANT ALTER SYSTEM, CREATE USER, DROP USER TO "octopus", "admin";
    
    REVOKE ALTER SYSTEM FROM "octopus";

다음으로 객체 권한을 부여하고 제거하는 문장에 대해 알아본다.

.. code-block:: bash
    
    GRANT objectPrivileges ON object TO grantees ;
    
    REVOKE objectPrivileges ON object FROM grantees ;
    
    objectPrivileges: objectPrivilege { , objectPrivilege }*
    object: "<dataSourceName>" . "<schemaName>"

**dataSourceName, schemaName**

부여할 권한을 지정한 스키마에 국한.

**objectPrivilege**

아래 표에 있는 객체 권한들을 지원한다.

.. _making-f-table:

===================== =====================================================================
권한                    설명
===================== =====================================================================
SELECT                   특정 스키마에 속한 테이블들에 대한 SELECT 질의 권한
COMMENT                  특정 스키마에 속한 테이블과 칼럼에 대한 주석 및 칼럼 분류 추가 권한
ALL [ PRIVILEGES ]       위의 모든 객체 권한
===================== =====================================================================

    - 예제

.. code-block:: bash

    GRANT ALL ON "spark"."default" TO "anon";
    
    REVOKE COMMENT ON "spark"."default" FROM "anon";

주석 및 칼럼 분류
^^^^^^^^^^^^^^^^^
Octopus는 수많은 데이터소스, 스키마, 테이블, 칼럼, 사용자 계정에 대한 관리 용이를 위해 각 대상에 주석을 추가할 수 있는 기능을 제공하고 있다. 또한 칼럼이 어떤 내용을 담고 있는지에 대한 분류를 추가할 수 있는 기능도 제공하고 있다. 그리고 이렇게 추가한 주석과 칼럼 분류를 대상의 이름으로 조회할 수 있다. 먼저 주석과 칼럼 분류를 추가하는 문장을 알아보고, 조회하는 문장은 4.4.4의 SHOW 명령에서 알아보도록 한다.

- 주석 추가

.. code-block:: bash

    COMMENT ON target IS '<comment>' ;
    Target: DATASOURCE "<dataSourceName>"
           | SCHEMA "<dataSourceName>" . "<schemaName>"
           | TABLE "<dataSourceName>" . "<schemaName>" . "<tableName>"
           | COLUMN "<dataSourceName>" . "<schemaName>" . "<tableName>" .
                      "<columnName>"
           | USER "<user>"

**dataSourceName, schemaName, tableName, columnName, user**

주석을 추가할 데이터소스, 스키마, 테이블, 칼럼, 사용자 계정을 지정.

**comment**

대상에 추가할 주석 문자열.

    - 예제

.. code-block:: bash

    COMMENT ON USER "octopus" IS 'super user';
    COMMENT ON TABLE "spark"."default"."stat" IS 'basic statistics';

- 칼럼 분류 추가

.. code-block:: bash

    SET DATACATEGORY
      ON COLUMN "<dataSourceName>" . "<schemaName>" . "<tableName>" .
                  "<columnName>"
      IS '<category>' ;

**dataSourceName, schemaName, tableName, columnName**

칼럼 분류를 추가할 칼럼을 지정.

**category**

칼럼 분류 문자열.

    - 예제

.. code-block:: bash

    SET DATACATEGORY ON COLUMN "spark"."account"."vip"."ssn" IS 'private';

SHOW 명령
^^^^^^^^^
이 절에서는 사용자 계정 정보, 스키마 정보, 권한 정보, 주석, 칼럼 분류를 조회할 수 있는 기능을 제공한다.

- 사용자 계정 조회

.. code-block:: bash

    SHOW ALL USERS ;

결과 칼럼은 아래와 같다.

.. _making-g-table:

============= ============== =================================
이름             타입              설명
============= ============== =================================
USER_NAME      VARCHAR        사용자 계정 이름
REMARKS        VARCHAR        사용자 계정에 추가한 주석
============= ============== =================================

- 데이터소스, 스키마, 테이블, 칼럼 조회

아래 문장은 차례로 데이터소스, 스키마, 테이블, 칼럼 정보를 조회한다. 

.. code-block:: bash

    SHOW DATASOURCES ;
    
    SHOW SCHEMAS [ DATASOURCE "<dataSourceName>" ] [ SCHEMA <schemaPattern> ] ;
    
    SHOW TABLES [ DATASOURCE "<dataSourceName>" ] [ SCHEMA '<schemaPattern>' ]
                  [ TABLE '<tablePattern>' ] ;
    
    SHOW COLUMNS [ DATASOURCE "<dataSourceName>" ] [ SCHEMA '<schemaPattern>' ]
                   [ TABLE '<tablePattern>' ] [ COLUMN '<columnPattern>' ] ;

**dataSourceName**

정보를 조회할 데이터소스 이름

**schemaPattern, tablePattern, columnPattern**

정보를 조회할 특정 스키마, 테이블, 칼럼의 패턴을 지정. 패턴은 SQL의 LIKE 패턴과 동일하며 생략할 경우 모든 대상을 조회한다.

    - 예제

.. code-block:: bash

    SHOW TABLES DATASOURCE "spark" TABLE '%stat%';

데이터소스에 대한 결과 칼럼은 아래와 같다.

.. _making-h-table:

=========== ============= ===============================
이름             타입          설명
=========== ============= ===============================
TABLE_CAT      VARCHAR     데이터소스 이름
REMARKS        VARCHAR     데이터소스에 추가한 주석
=========== ============= ===============================

스키마에 대한 결과 칼럼은 아래와 같다.

.. _making-j-table:

==================== ============ ============================
이름                   타입             설명
==================== ============ ============================
TABLE_SCHEM          VARCHAR         스키마 이름
TABLE_CATALOG        VARCHAR         데이터소스 이름
REMARKS              VARCHAR         스키마에 추가한 주석
TABLE_CAT_REMARKS    VARCHAR         데이터소스에 추가한 주석
==================== ============ ============================

테이블에 대한 결과 칼럼은 아래와 같다.

.. _making-k-table:

====================== =========== =====================================
이름                      타입                설명
====================== =========== =====================================
TABLE_CAT                VARCHAR        데이터소스 이름
TABLE_SCHEM              VARCHAR        스키마 이름
TABLE_NAME               VARCHAR        테이블 이름
TABLE_TYPE               VARCHAR        테이블 타입 (TABLE, VIEW)
REMARKS                  VARCHAR        테이블에 추가한 주석
TABLE_CAT_REMARKS        VARCHAR        데이터소스에 추가한 주석
TABLE_SCHEM_REMARKS      VARCHAR        스키마에 추가한 주석
====================== =========== =====================================

칼럼에 대한 결과 칼럼은 아래와 같다.

.. _making-l-table:


======================= =============== =====================================
이름                            타입                설명
======================= =============== =====================================
TABLE_CAT                  VARCHAR           데이터소스 이름
TABLE_SCHEM                VARCHAR           스키마 이름
TABLE_NAME                 VARCHAR           테이블 이름
COLUMN_NAME                VARCHAR           칼럼 이름
DATA_TYPE                  VARCHAR           칼럼의 SQL 타입 (정수)
TYPE_NAME                  VARCHAR           타입 이름
REMARKS                    VARCHAR           칼럼에 추가한 주석
DATA_CATEGORY              VARCHAR           칼럼 분류
TABLE_CAT_REMARKS          VARCHAR           데이터소스에 추가한 주석
TABLE_SCHEM_REMARKS        VARCHAR           스키마에 추가한 주석
TABLE_NAME_REMARKS         VARCHAR           테이블에 추가한 주석
======================= =============== =====================================

- 사용자 계정에 부여된 스키마에 대한 권한 조회

.. code-block:: bash

    SHOW OBJECT PRIVILEGES FOR '<user>' ;

**user**

권한을 조회할 대상 계정.

    - 예제

.. code-block:: bash

    SHOW OBJECT PRIVILEGES FOR "octopus";

결과 칼럼은 아래와 같다.

.. _making-m-table:


============== ============== =======================================================================
이름                타입                   설명
============== ============== =======================================================================
TABLE_CAT         VARCHAR           데이터소스 이름 
TABLE_SCHEM       VARCHAR           스키마 이름
PRIVILEGE         VARCHAR           사용자 계정에 부여된 스키마에 대한 권한 (쉼표 구분 리스트)
============== ============== =======================================================================

- 주석 조회

.. code-block:: bash

    SHOW COMMENTS [ '<commentPattern>' }
      [ DATASOURCE '<dataSourcePattern>' ] [ SCHEMA '<schemaPattern>' ]
      [ TABLE '<tablePattern>' ] [ COLUMN '<columnPattern>' ] ;

**commentPattern**

조회할 주석의 패턴을 지정한다.

**dataSourcePattern, schemaPattern, tablePattern, columnPattern**

특정 데이터소스, 스키마, 테이블, 칼럼의 패턴을 지정. 패턴은 SQL의 LIKE 패턴과 동일하며 생략할 경우 모든 대상의 주석을 조회한다.

    - 예제

.. code-block:: bash

    SHOW COMMENTS '%average%' DATASOURCE 'spark' TABLE '%stat%';

결과 칼럼은 아래와 같다.

.. _making-n-table:

======================= ============ ===============================================================
이름                         타입             설명
======================= ============ ===============================================================
OBJECT_TYPE             VARCHAR           대상에 따라 CATALOG, SCHEMA, TABLE, COLUMN
TABLE_CAT               VARCHAR           데이터소스 이름
TABLE_SCHEM             VARCHAR           스키마 이름
TABLE_NAME              VARCHAR           테이블 이름
COLUMN_NAME             VARCHAR           칼럼 이름
TABLE_CAT_REMARKS       VARCHAR           데이터소스에 추가한 주석
TABLE_SCHEM_REMARKS     VARCHAR           스키마에 추가한 주석
TABLE_NAME_REMARKS      VARCHAR           테이블에 추가한 주석
COLUMN_NAME_REMARKS     VARCHAR        칼럼에 추가한 주석
======================= ============ ===============================================================
