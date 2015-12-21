
Connector
=========
다른 프로그램에서 Octopus에 대해 질의를 수행하거나 스키마 정보 등을 가져오기 위해 Octopus JDBC 드라이버를 이용해야 한다. 그래서 Octopus JDBC 드라이버를 얻는 방법과 이 방법으로 얻은 Octopus JDBC 드라이버를 애플리케이션에서 어떻게 사용하는지에 대해 예를 들어 설명한다.

Octopus JDBC 드라이버 얻기
--------------------------
Octopus의 JDBC 드라이버는 Octopus 설치 디렉터리 내에 있는 share/octopus 디렉터리에서 얻을 수 있다. 해당 디렉터리에 있는 octopus-jdbc-x.y.z-SNAPSHOP.jar 파일을 CLASSPATH에 추가하여 Octopus JDBC 드라이버를 사용할 수 있다.

Octopus JDBC 드라이버 사용
--------------------------
Octopus JDBC 드라이버를 사용할 경우 JDBC 드라이버 클래스와 연결 URL이 필요하며, 이에 대한 정보는 다음과 같다.

- JDBC 드라이버 클래스 정보

    - kr.co.bitnine.octopus.Driver

- 연결 URL 정보

    - jdbc:octopus://<octopus-hostname>[:<octopus-port>]

      - 예) jdbc:octopus://127.0.0.1:58001

.. _making-a-table:

=================== =========================================== =====================
인자                         설명                                        기본값
=================== =========================================== =====================
octopus-hostname        접속할 Octopus 인스턴스의 hostname          없음 (필수)
octopus-port            접속할 Octopus 인스턴스의 port 번호           58000
=================== =========================================== =====================


다음은 Octopus JDBC 드라이버 사용 예제이다.

.. code-block:: bash

    public class OctopusJdbcTest {
        public static void main(String[] args) throws Exception {
            Class.forName("kr.co.bitnine.octopus.Driver");
            String connectionString = "jdbc:octopus://127.0.0.1";
    String username = "octopus";
    String password = "bitnine";
            Connection conn = DriverManager.getConnection(connectionString,
    username, password);
    Statement stmt = conn.createStatement();
    	ResultSet rs = stmt.executeQuery("SHOW ALL USERS");
            while (rs.next()) {
    String name = rs.getString(1);
                System.out.println(name);
            }
            rs.close();
    stmt.close();
            conn.close();
        }
    }

Octopus JDBC API
----------------
Octopus는 JDBC 4.2를 지원하고 있다. 그러나 Octopus 특성상 commit/rollback 관련 동작을 지원하지 않고 있으며, 구현상의 제약으로 DatabaseMetaData의 경우 몇 가지 함수만 지원하고 있다.

아래는 Octopus의 ''DatabaseMetaData''에서 지원하고 있는 함수 목록이다.

.. _making-b-table:

=============== ============================
함수                설명
=============== ============================
getCatalogs       모든 데이터소스 조회
getColumns        특정 칼럼 조회
getConnection     현재 연결 객체 반환
getSchemas        특정 스키마 정보 조회
getTables         특정 테이블 정보 조회
=============== ============================

위에 나열된 조회 함수에 대한 결과 칼럼은 4.4.4를 참고하기 바란다. 그 밖에 자세한 내용은 Java의 ''java.sql'' 패키지 문서를 참고하기 바란다.

