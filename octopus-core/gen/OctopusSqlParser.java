// Generated from /home/kisung/octopus/octopus-core/src/main/antlr4/kr/co/bitnine/octopus/sql/OctopusSql.g4 by ANTLR 4.5.1

    import org.apache.commons.lang3.StringUtils;

import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class OctopusSqlParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.5.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, K_ADD=2, K_ALTER=3, K_BY=4, K_CONNECT=5, K_CREATE=6, K_DATASOURCE=7, 
		K_SCHEMA=8, K_TABLE=9, K_COLUMN=10, K_DROP=11, K_IDENTIFIED=12, K_REPLACE=13, 
		K_ROLE=14, K_SYSTEM=15, K_USER=16, K_SHOW=17, K_DATASOURCES=18, K_SCHEMAS=19, 
		K_TABLES=20, K_COLUMNS=21, K_PRIVILEGES=22, K_USERS=23, K_COMMENT=24, 
		K_ON=25, K_IS=26, IDENTIFIER=27, STRING_LITERAL=28, WHITESPACES=29, UNEXPECTED_CHAR=30;
	public static final int
		RULE_ddl = 0, RULE_ddlStmts = 1, RULE_ddlStmt = 2, RULE_alterSystem = 3, 
		RULE_dataSourceClause = 4, RULE_dataSourceName = 5, RULE_jdbcConnectionString = 6, 
		RULE_createUser = 7, RULE_alterUser = 8, RULE_dropUser = 9, RULE_user = 10, 
		RULE_password = 11, RULE_oldPassword = 12, RULE_createRole = 13, RULE_dropRole = 14, 
		RULE_role = 15, RULE_showDataSources = 16, RULE_showSchemas = 17, RULE_showTables = 18, 
		RULE_showColumns = 19, RULE_showTablePrivileges = 20, RULE_showColumnPrivileges = 21, 
		RULE_showUsers = 22, RULE_commentOn = 23, RULE_commentOnTarget = 24, RULE_dataSource = 25, 
		RULE_schemaPattern = 26, RULE_tablePattern = 27, RULE_columnPattern = 28, 
		RULE_schemaName = 29, RULE_tableName = 30, RULE_columnName = 31, RULE_comment = 32, 
		RULE_error = 33;
	public static final String[] ruleNames = {
		"ddl", "ddlStmts", "ddlStmt", "alterSystem", "dataSourceClause", "dataSourceName", 
		"jdbcConnectionString", "createUser", "alterUser", "dropUser", "user", 
		"password", "oldPassword", "createRole", "dropRole", "role", "showDataSources", 
		"showSchemas", "showTables", "showColumns", "showTablePrivileges", "showColumnPrivileges", 
		"showUsers", "commentOn", "commentOnTarget", "dataSource", "schemaPattern", 
		"tablePattern", "columnPattern", "schemaName", "tableName", "columnName", 
		"comment", "error"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "';'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, "K_ADD", "K_ALTER", "K_BY", "K_CONNECT", "K_CREATE", "K_DATASOURCE", 
		"K_SCHEMA", "K_TABLE", "K_COLUMN", "K_DROP", "K_IDENTIFIED", "K_REPLACE", 
		"K_ROLE", "K_SYSTEM", "K_USER", "K_SHOW", "K_DATASOURCES", "K_SCHEMAS", 
		"K_TABLES", "K_COLUMNS", "K_PRIVILEGES", "K_USERS", "K_COMMENT", "K_ON", 
		"K_IS", "IDENTIFIER", "STRING_LITERAL", "WHITESPACES", "UNEXPECTED_CHAR"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "OctopusSql.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public OctopusSqlParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class DdlContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(OctopusSqlParser.EOF, 0); }
		public DdlStmtsContext ddlStmts() {
			return getRuleContext(DdlStmtsContext.class,0);
		}
		public ErrorContext error() {
			return getRuleContext(ErrorContext.class,0);
		}
		public DdlContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ddl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).enterDdl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).exitDdl(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OctopusSqlVisitor ) return ((OctopusSqlVisitor<? extends T>)visitor).visitDdl(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DdlContext ddl() throws RecognitionException {
		DdlContext _localctx = new DdlContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_ddl);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(70);
			switch (_input.LA(1)) {
			case T__0:
			case K_ALTER:
			case K_CREATE:
			case K_DROP:
			case K_SHOW:
			case K_COMMENT:
				{
				setState(68);
				ddlStmts();
				}
				break;
			case UNEXPECTED_CHAR:
				{
				setState(69);
				error();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(72);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DdlStmtsContext extends ParserRuleContext {
		public List<DdlStmtContext> ddlStmt() {
			return getRuleContexts(DdlStmtContext.class);
		}
		public DdlStmtContext ddlStmt(int i) {
			return getRuleContext(DdlStmtContext.class,i);
		}
		public DdlStmtsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ddlStmts; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).enterDdlStmts(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).exitDdlStmts(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OctopusSqlVisitor ) return ((OctopusSqlVisitor<? extends T>)visitor).visitDdlStmts(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DdlStmtsContext ddlStmts() throws RecognitionException {
		DdlStmtsContext _localctx = new DdlStmtsContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_ddlStmts);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(77);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__0) {
				{
				{
				setState(74);
				match(T__0);
				}
				}
				setState(79);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(80);
			ddlStmt();
			setState(89);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(82); 
					_errHandler.sync(this);
					_la = _input.LA(1);
					do {
						{
						{
						setState(81);
						match(T__0);
						}
						}
						setState(84); 
						_errHandler.sync(this);
						_la = _input.LA(1);
					} while ( _la==T__0 );
					setState(86);
					ddlStmt();
					}
					} 
				}
				setState(91);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
			}
			setState(95);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__0) {
				{
				{
				setState(92);
				match(T__0);
				}
				}
				setState(97);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DdlStmtContext extends ParserRuleContext {
		public AlterSystemContext alterSystem() {
			return getRuleContext(AlterSystemContext.class,0);
		}
		public CreateUserContext createUser() {
			return getRuleContext(CreateUserContext.class,0);
		}
		public AlterUserContext alterUser() {
			return getRuleContext(AlterUserContext.class,0);
		}
		public DropUserContext dropUser() {
			return getRuleContext(DropUserContext.class,0);
		}
		public CreateRoleContext createRole() {
			return getRuleContext(CreateRoleContext.class,0);
		}
		public DropRoleContext dropRole() {
			return getRuleContext(DropRoleContext.class,0);
		}
		public ShowTablesContext showTables() {
			return getRuleContext(ShowTablesContext.class,0);
		}
		public CommentOnContext commentOn() {
			return getRuleContext(CommentOnContext.class,0);
		}
		public DdlStmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ddlStmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).enterDdlStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).exitDdlStmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OctopusSqlVisitor ) return ((OctopusSqlVisitor<? extends T>)visitor).visitDdlStmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DdlStmtContext ddlStmt() throws RecognitionException {
		DdlStmtContext _localctx = new DdlStmtContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_ddlStmt);
		try {
			setState(106);
			switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(98);
				alterSystem();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(99);
				createUser();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(100);
				alterUser();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(101);
				dropUser();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(102);
				createRole();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(103);
				dropRole();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(104);
				showTables();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(105);
				commentOn();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AlterSystemContext extends ParserRuleContext {
		public TerminalNode K_ALTER() { return getToken(OctopusSqlParser.K_ALTER, 0); }
		public TerminalNode K_SYSTEM() { return getToken(OctopusSqlParser.K_SYSTEM, 0); }
		public DataSourceClauseContext dataSourceClause() {
			return getRuleContext(DataSourceClauseContext.class,0);
		}
		public AlterSystemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_alterSystem; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).enterAlterSystem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).exitAlterSystem(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OctopusSqlVisitor ) return ((OctopusSqlVisitor<? extends T>)visitor).visitAlterSystem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AlterSystemContext alterSystem() throws RecognitionException {
		AlterSystemContext _localctx = new AlterSystemContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_alterSystem);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(108);
			match(K_ALTER);
			setState(109);
			match(K_SYSTEM);
			setState(110);
			dataSourceClause();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DataSourceClauseContext extends ParserRuleContext {
		public TerminalNode K_ADD() { return getToken(OctopusSqlParser.K_ADD, 0); }
		public TerminalNode K_DATASOURCE() { return getToken(OctopusSqlParser.K_DATASOURCE, 0); }
		public DataSourceNameContext dataSourceName() {
			return getRuleContext(DataSourceNameContext.class,0);
		}
		public TerminalNode K_CONNECT() { return getToken(OctopusSqlParser.K_CONNECT, 0); }
		public TerminalNode K_BY() { return getToken(OctopusSqlParser.K_BY, 0); }
		public JdbcConnectionStringContext jdbcConnectionString() {
			return getRuleContext(JdbcConnectionStringContext.class,0);
		}
		public DataSourceClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dataSourceClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).enterDataSourceClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).exitDataSourceClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OctopusSqlVisitor ) return ((OctopusSqlVisitor<? extends T>)visitor).visitDataSourceClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DataSourceClauseContext dataSourceClause() throws RecognitionException {
		DataSourceClauseContext _localctx = new DataSourceClauseContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_dataSourceClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(112);
			match(K_ADD);
			setState(113);
			match(K_DATASOURCE);
			setState(114);
			dataSourceName();
			setState(115);
			match(K_CONNECT);
			setState(116);
			match(K_BY);
			setState(117);
			jdbcConnectionString();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DataSourceNameContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER() { return getToken(OctopusSqlParser.IDENTIFIER, 0); }
		public DataSourceNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dataSourceName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).enterDataSourceName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).exitDataSourceName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OctopusSqlVisitor ) return ((OctopusSqlVisitor<? extends T>)visitor).visitDataSourceName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DataSourceNameContext dataSourceName() throws RecognitionException {
		DataSourceNameContext _localctx = new DataSourceNameContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_dataSourceName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(119);
			match(IDENTIFIER);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class JdbcConnectionStringContext extends ParserRuleContext {
		public TerminalNode STRING_LITERAL() { return getToken(OctopusSqlParser.STRING_LITERAL, 0); }
		public JdbcConnectionStringContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_jdbcConnectionString; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).enterJdbcConnectionString(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).exitJdbcConnectionString(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OctopusSqlVisitor ) return ((OctopusSqlVisitor<? extends T>)visitor).visitJdbcConnectionString(this);
			else return visitor.visitChildren(this);
		}
	}

	public final JdbcConnectionStringContext jdbcConnectionString() throws RecognitionException {
		JdbcConnectionStringContext _localctx = new JdbcConnectionStringContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_jdbcConnectionString);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(121);
			match(STRING_LITERAL);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CreateUserContext extends ParserRuleContext {
		public TerminalNode K_CREATE() { return getToken(OctopusSqlParser.K_CREATE, 0); }
		public TerminalNode K_USER() { return getToken(OctopusSqlParser.K_USER, 0); }
		public UserContext user() {
			return getRuleContext(UserContext.class,0);
		}
		public TerminalNode K_IDENTIFIED() { return getToken(OctopusSqlParser.K_IDENTIFIED, 0); }
		public TerminalNode K_BY() { return getToken(OctopusSqlParser.K_BY, 0); }
		public PasswordContext password() {
			return getRuleContext(PasswordContext.class,0);
		}
		public CreateUserContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_createUser; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).enterCreateUser(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).exitCreateUser(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OctopusSqlVisitor ) return ((OctopusSqlVisitor<? extends T>)visitor).visitCreateUser(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CreateUserContext createUser() throws RecognitionException {
		CreateUserContext _localctx = new CreateUserContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_createUser);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(123);
			match(K_CREATE);
			setState(124);
			match(K_USER);
			setState(125);
			user();
			setState(126);
			match(K_IDENTIFIED);
			setState(127);
			match(K_BY);
			setState(128);
			password();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AlterUserContext extends ParserRuleContext {
		public TerminalNode K_ALTER() { return getToken(OctopusSqlParser.K_ALTER, 0); }
		public TerminalNode K_USER() { return getToken(OctopusSqlParser.K_USER, 0); }
		public UserContext user() {
			return getRuleContext(UserContext.class,0);
		}
		public TerminalNode K_IDENTIFIED() { return getToken(OctopusSqlParser.K_IDENTIFIED, 0); }
		public TerminalNode K_BY() { return getToken(OctopusSqlParser.K_BY, 0); }
		public PasswordContext password() {
			return getRuleContext(PasswordContext.class,0);
		}
		public TerminalNode K_REPLACE() { return getToken(OctopusSqlParser.K_REPLACE, 0); }
		public OldPasswordContext oldPassword() {
			return getRuleContext(OldPasswordContext.class,0);
		}
		public AlterUserContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_alterUser; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).enterAlterUser(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).exitAlterUser(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OctopusSqlVisitor ) return ((OctopusSqlVisitor<? extends T>)visitor).visitAlterUser(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AlterUserContext alterUser() throws RecognitionException {
		AlterUserContext _localctx = new AlterUserContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_alterUser);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(130);
			match(K_ALTER);
			setState(131);
			match(K_USER);
			setState(132);
			user();
			setState(133);
			match(K_IDENTIFIED);
			setState(134);
			match(K_BY);
			setState(135);
			password();
			setState(138);
			_la = _input.LA(1);
			if (_la==K_REPLACE) {
				{
				setState(136);
				match(K_REPLACE);
				setState(137);
				oldPassword();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DropUserContext extends ParserRuleContext {
		public TerminalNode K_DROP() { return getToken(OctopusSqlParser.K_DROP, 0); }
		public TerminalNode K_USER() { return getToken(OctopusSqlParser.K_USER, 0); }
		public UserContext user() {
			return getRuleContext(UserContext.class,0);
		}
		public DropUserContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dropUser; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).enterDropUser(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).exitDropUser(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OctopusSqlVisitor ) return ((OctopusSqlVisitor<? extends T>)visitor).visitDropUser(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DropUserContext dropUser() throws RecognitionException {
		DropUserContext _localctx = new DropUserContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_dropUser);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(140);
			match(K_DROP);
			setState(141);
			match(K_USER);
			setState(142);
			user();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class UserContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER() { return getToken(OctopusSqlParser.IDENTIFIER, 0); }
		public UserContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_user; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).enterUser(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).exitUser(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OctopusSqlVisitor ) return ((OctopusSqlVisitor<? extends T>)visitor).visitUser(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UserContext user() throws RecognitionException {
		UserContext _localctx = new UserContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_user);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(144);
			match(IDENTIFIER);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PasswordContext extends ParserRuleContext {
		public TerminalNode STRING_LITERAL() { return getToken(OctopusSqlParser.STRING_LITERAL, 0); }
		public PasswordContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_password; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).enterPassword(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).exitPassword(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OctopusSqlVisitor ) return ((OctopusSqlVisitor<? extends T>)visitor).visitPassword(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PasswordContext password() throws RecognitionException {
		PasswordContext _localctx = new PasswordContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_password);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(146);
			match(STRING_LITERAL);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class OldPasswordContext extends ParserRuleContext {
		public TerminalNode STRING_LITERAL() { return getToken(OctopusSqlParser.STRING_LITERAL, 0); }
		public OldPasswordContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_oldPassword; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).enterOldPassword(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).exitOldPassword(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OctopusSqlVisitor ) return ((OctopusSqlVisitor<? extends T>)visitor).visitOldPassword(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OldPasswordContext oldPassword() throws RecognitionException {
		OldPasswordContext _localctx = new OldPasswordContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_oldPassword);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(148);
			match(STRING_LITERAL);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CreateRoleContext extends ParserRuleContext {
		public TerminalNode K_CREATE() { return getToken(OctopusSqlParser.K_CREATE, 0); }
		public TerminalNode K_ROLE() { return getToken(OctopusSqlParser.K_ROLE, 0); }
		public RoleContext role() {
			return getRuleContext(RoleContext.class,0);
		}
		public CreateRoleContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_createRole; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).enterCreateRole(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).exitCreateRole(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OctopusSqlVisitor ) return ((OctopusSqlVisitor<? extends T>)visitor).visitCreateRole(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CreateRoleContext createRole() throws RecognitionException {
		CreateRoleContext _localctx = new CreateRoleContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_createRole);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(150);
			match(K_CREATE);
			setState(151);
			match(K_ROLE);
			setState(152);
			role();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DropRoleContext extends ParserRuleContext {
		public TerminalNode K_DROP() { return getToken(OctopusSqlParser.K_DROP, 0); }
		public TerminalNode K_ROLE() { return getToken(OctopusSqlParser.K_ROLE, 0); }
		public RoleContext role() {
			return getRuleContext(RoleContext.class,0);
		}
		public DropRoleContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dropRole; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).enterDropRole(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).exitDropRole(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OctopusSqlVisitor ) return ((OctopusSqlVisitor<? extends T>)visitor).visitDropRole(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DropRoleContext dropRole() throws RecognitionException {
		DropRoleContext _localctx = new DropRoleContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_dropRole);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(154);
			match(K_DROP);
			setState(155);
			match(K_ROLE);
			setState(156);
			role();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RoleContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER() { return getToken(OctopusSqlParser.IDENTIFIER, 0); }
		public RoleContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_role; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).enterRole(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).exitRole(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OctopusSqlVisitor ) return ((OctopusSqlVisitor<? extends T>)visitor).visitRole(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RoleContext role() throws RecognitionException {
		RoleContext _localctx = new RoleContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_role);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(158);
			match(IDENTIFIER);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ShowDataSourcesContext extends ParserRuleContext {
		public TerminalNode K_SHOW() { return getToken(OctopusSqlParser.K_SHOW, 0); }
		public TerminalNode K_DATASOURCES() { return getToken(OctopusSqlParser.K_DATASOURCES, 0); }
		public ShowDataSourcesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_showDataSources; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).enterShowDataSources(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).exitShowDataSources(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OctopusSqlVisitor ) return ((OctopusSqlVisitor<? extends T>)visitor).visitShowDataSources(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ShowDataSourcesContext showDataSources() throws RecognitionException {
		ShowDataSourcesContext _localctx = new ShowDataSourcesContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_showDataSources);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(160);
			match(K_SHOW);
			setState(161);
			match(K_DATASOURCES);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ShowSchemasContext extends ParserRuleContext {
		public TerminalNode K_SHOW() { return getToken(OctopusSqlParser.K_SHOW, 0); }
		public TerminalNode K_SCHEMAS() { return getToken(OctopusSqlParser.K_SCHEMAS, 0); }
		public TerminalNode K_DATASOURCE() { return getToken(OctopusSqlParser.K_DATASOURCE, 0); }
		public DataSourceContext dataSource() {
			return getRuleContext(DataSourceContext.class,0);
		}
		public TerminalNode K_SCHEMA() { return getToken(OctopusSqlParser.K_SCHEMA, 0); }
		public SchemaPatternContext schemaPattern() {
			return getRuleContext(SchemaPatternContext.class,0);
		}
		public ShowSchemasContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_showSchemas; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).enterShowSchemas(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).exitShowSchemas(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OctopusSqlVisitor ) return ((OctopusSqlVisitor<? extends T>)visitor).visitShowSchemas(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ShowSchemasContext showSchemas() throws RecognitionException {
		ShowSchemasContext _localctx = new ShowSchemasContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_showSchemas);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(163);
			match(K_SHOW);
			setState(164);
			match(K_SCHEMAS);
			setState(167);
			_la = _input.LA(1);
			if (_la==K_DATASOURCE) {
				{
				setState(165);
				match(K_DATASOURCE);
				setState(166);
				dataSource();
				}
			}

			setState(171);
			_la = _input.LA(1);
			if (_la==K_SCHEMA) {
				{
				setState(169);
				match(K_SCHEMA);
				setState(170);
				schemaPattern();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ShowTablesContext extends ParserRuleContext {
		public TerminalNode K_SHOW() { return getToken(OctopusSqlParser.K_SHOW, 0); }
		public TerminalNode K_TABLES() { return getToken(OctopusSqlParser.K_TABLES, 0); }
		public TerminalNode K_DATASOURCE() { return getToken(OctopusSqlParser.K_DATASOURCE, 0); }
		public DataSourceContext dataSource() {
			return getRuleContext(DataSourceContext.class,0);
		}
		public TerminalNode K_SCHEMA() { return getToken(OctopusSqlParser.K_SCHEMA, 0); }
		public SchemaPatternContext schemaPattern() {
			return getRuleContext(SchemaPatternContext.class,0);
		}
		public TerminalNode K_TABLE() { return getToken(OctopusSqlParser.K_TABLE, 0); }
		public TablePatternContext tablePattern() {
			return getRuleContext(TablePatternContext.class,0);
		}
		public ShowTablesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_showTables; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).enterShowTables(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).exitShowTables(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OctopusSqlVisitor ) return ((OctopusSqlVisitor<? extends T>)visitor).visitShowTables(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ShowTablesContext showTables() throws RecognitionException {
		ShowTablesContext _localctx = new ShowTablesContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_showTables);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(173);
			match(K_SHOW);
			setState(174);
			match(K_TABLES);
			setState(177);
			_la = _input.LA(1);
			if (_la==K_DATASOURCE) {
				{
				setState(175);
				match(K_DATASOURCE);
				setState(176);
				dataSource();
				}
			}

			setState(181);
			_la = _input.LA(1);
			if (_la==K_SCHEMA) {
				{
				setState(179);
				match(K_SCHEMA);
				setState(180);
				schemaPattern();
				}
			}

			setState(185);
			_la = _input.LA(1);
			if (_la==K_TABLE) {
				{
				setState(183);
				match(K_TABLE);
				setState(184);
				tablePattern();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ShowColumnsContext extends ParserRuleContext {
		public TerminalNode K_SHOW() { return getToken(OctopusSqlParser.K_SHOW, 0); }
		public TerminalNode K_COLUMNS() { return getToken(OctopusSqlParser.K_COLUMNS, 0); }
		public TerminalNode K_DATASOURCE() { return getToken(OctopusSqlParser.K_DATASOURCE, 0); }
		public DataSourceContext dataSource() {
			return getRuleContext(DataSourceContext.class,0);
		}
		public TerminalNode K_SCHEMA() { return getToken(OctopusSqlParser.K_SCHEMA, 0); }
		public SchemaPatternContext schemaPattern() {
			return getRuleContext(SchemaPatternContext.class,0);
		}
		public TerminalNode K_TABLE() { return getToken(OctopusSqlParser.K_TABLE, 0); }
		public TablePatternContext tablePattern() {
			return getRuleContext(TablePatternContext.class,0);
		}
		public TerminalNode K_COLUMN() { return getToken(OctopusSqlParser.K_COLUMN, 0); }
		public ColumnPatternContext columnPattern() {
			return getRuleContext(ColumnPatternContext.class,0);
		}
		public ShowColumnsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_showColumns; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).enterShowColumns(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).exitShowColumns(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OctopusSqlVisitor ) return ((OctopusSqlVisitor<? extends T>)visitor).visitShowColumns(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ShowColumnsContext showColumns() throws RecognitionException {
		ShowColumnsContext _localctx = new ShowColumnsContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_showColumns);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(187);
			match(K_SHOW);
			setState(188);
			match(K_COLUMNS);
			setState(191);
			_la = _input.LA(1);
			if (_la==K_DATASOURCE) {
				{
				setState(189);
				match(K_DATASOURCE);
				setState(190);
				dataSource();
				}
			}

			setState(195);
			_la = _input.LA(1);
			if (_la==K_SCHEMA) {
				{
				setState(193);
				match(K_SCHEMA);
				setState(194);
				schemaPattern();
				}
			}

			setState(199);
			_la = _input.LA(1);
			if (_la==K_TABLE) {
				{
				setState(197);
				match(K_TABLE);
				setState(198);
				tablePattern();
				}
			}

			setState(203);
			_la = _input.LA(1);
			if (_la==K_COLUMN) {
				{
				setState(201);
				match(K_COLUMN);
				setState(202);
				columnPattern();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ShowTablePrivilegesContext extends ParserRuleContext {
		public TerminalNode K_SHOW() { return getToken(OctopusSqlParser.K_SHOW, 0); }
		public List<TerminalNode> K_TABLE() { return getTokens(OctopusSqlParser.K_TABLE); }
		public TerminalNode K_TABLE(int i) {
			return getToken(OctopusSqlParser.K_TABLE, i);
		}
		public TerminalNode K_PRIVILEGES() { return getToken(OctopusSqlParser.K_PRIVILEGES, 0); }
		public TerminalNode K_DATASOURCE() { return getToken(OctopusSqlParser.K_DATASOURCE, 0); }
		public DataSourceContext dataSource() {
			return getRuleContext(DataSourceContext.class,0);
		}
		public TerminalNode K_SCHEMA() { return getToken(OctopusSqlParser.K_SCHEMA, 0); }
		public SchemaPatternContext schemaPattern() {
			return getRuleContext(SchemaPatternContext.class,0);
		}
		public TablePatternContext tablePattern() {
			return getRuleContext(TablePatternContext.class,0);
		}
		public ShowTablePrivilegesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_showTablePrivileges; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).enterShowTablePrivileges(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).exitShowTablePrivileges(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OctopusSqlVisitor ) return ((OctopusSqlVisitor<? extends T>)visitor).visitShowTablePrivileges(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ShowTablePrivilegesContext showTablePrivileges() throws RecognitionException {
		ShowTablePrivilegesContext _localctx = new ShowTablePrivilegesContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_showTablePrivileges);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(205);
			match(K_SHOW);
			setState(206);
			match(K_TABLE);
			setState(207);
			match(K_PRIVILEGES);
			setState(210);
			_la = _input.LA(1);
			if (_la==K_DATASOURCE) {
				{
				setState(208);
				match(K_DATASOURCE);
				setState(209);
				dataSource();
				}
			}

			setState(214);
			_la = _input.LA(1);
			if (_la==K_SCHEMA) {
				{
				setState(212);
				match(K_SCHEMA);
				setState(213);
				schemaPattern();
				}
			}

			setState(218);
			_la = _input.LA(1);
			if (_la==K_TABLE) {
				{
				setState(216);
				match(K_TABLE);
				setState(217);
				tablePattern();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ShowColumnPrivilegesContext extends ParserRuleContext {
		public TerminalNode K_SHOW() { return getToken(OctopusSqlParser.K_SHOW, 0); }
		public List<TerminalNode> K_COLUMN() { return getTokens(OctopusSqlParser.K_COLUMN); }
		public TerminalNode K_COLUMN(int i) {
			return getToken(OctopusSqlParser.K_COLUMN, i);
		}
		public TerminalNode K_PRIVILEGES() { return getToken(OctopusSqlParser.K_PRIVILEGES, 0); }
		public TerminalNode K_DATASOURCE() { return getToken(OctopusSqlParser.K_DATASOURCE, 0); }
		public DataSourceContext dataSource() {
			return getRuleContext(DataSourceContext.class,0);
		}
		public TerminalNode K_SCHEMA() { return getToken(OctopusSqlParser.K_SCHEMA, 0); }
		public SchemaPatternContext schemaPattern() {
			return getRuleContext(SchemaPatternContext.class,0);
		}
		public TerminalNode K_TABLE() { return getToken(OctopusSqlParser.K_TABLE, 0); }
		public TablePatternContext tablePattern() {
			return getRuleContext(TablePatternContext.class,0);
		}
		public ColumnPatternContext columnPattern() {
			return getRuleContext(ColumnPatternContext.class,0);
		}
		public ShowColumnPrivilegesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_showColumnPrivileges; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).enterShowColumnPrivileges(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).exitShowColumnPrivileges(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OctopusSqlVisitor ) return ((OctopusSqlVisitor<? extends T>)visitor).visitShowColumnPrivileges(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ShowColumnPrivilegesContext showColumnPrivileges() throws RecognitionException {
		ShowColumnPrivilegesContext _localctx = new ShowColumnPrivilegesContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_showColumnPrivileges);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(220);
			match(K_SHOW);
			setState(221);
			match(K_COLUMN);
			setState(222);
			match(K_PRIVILEGES);
			setState(225);
			_la = _input.LA(1);
			if (_la==K_DATASOURCE) {
				{
				setState(223);
				match(K_DATASOURCE);
				setState(224);
				dataSource();
				}
			}

			setState(229);
			_la = _input.LA(1);
			if (_la==K_SCHEMA) {
				{
				setState(227);
				match(K_SCHEMA);
				setState(228);
				schemaPattern();
				}
			}

			setState(233);
			_la = _input.LA(1);
			if (_la==K_TABLE) {
				{
				setState(231);
				match(K_TABLE);
				setState(232);
				tablePattern();
				}
			}

			setState(237);
			_la = _input.LA(1);
			if (_la==K_COLUMN) {
				{
				setState(235);
				match(K_COLUMN);
				setState(236);
				columnPattern();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ShowUsersContext extends ParserRuleContext {
		public TerminalNode K_SHOW() { return getToken(OctopusSqlParser.K_SHOW, 0); }
		public TerminalNode K_USERS() { return getToken(OctopusSqlParser.K_USERS, 0); }
		public ShowUsersContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_showUsers; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).enterShowUsers(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).exitShowUsers(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OctopusSqlVisitor ) return ((OctopusSqlVisitor<? extends T>)visitor).visitShowUsers(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ShowUsersContext showUsers() throws RecognitionException {
		ShowUsersContext _localctx = new ShowUsersContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_showUsers);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(239);
			match(K_SHOW);
			setState(240);
			match(K_USERS);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CommentOnContext extends ParserRuleContext {
		public TerminalNode K_COMMENT() { return getToken(OctopusSqlParser.K_COMMENT, 0); }
		public TerminalNode K_ON() { return getToken(OctopusSqlParser.K_ON, 0); }
		public CommentOnTargetContext commentOnTarget() {
			return getRuleContext(CommentOnTargetContext.class,0);
		}
		public TerminalNode K_IS() { return getToken(OctopusSqlParser.K_IS, 0); }
		public CommentContext comment() {
			return getRuleContext(CommentContext.class,0);
		}
		public CommentOnContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_commentOn; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).enterCommentOn(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).exitCommentOn(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OctopusSqlVisitor ) return ((OctopusSqlVisitor<? extends T>)visitor).visitCommentOn(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CommentOnContext commentOn() throws RecognitionException {
		CommentOnContext _localctx = new CommentOnContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_commentOn);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(242);
			match(K_COMMENT);
			setState(243);
			match(K_ON);
			setState(244);
			commentOnTarget();
			setState(245);
			match(K_IS);
			setState(246);
			comment();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CommentOnTargetContext extends ParserRuleContext {
		public TerminalNode K_DATASOURCE() { return getToken(OctopusSqlParser.K_DATASOURCE, 0); }
		public DataSourceContext dataSource() {
			return getRuleContext(DataSourceContext.class,0);
		}
		public TerminalNode K_SCHEMA() { return getToken(OctopusSqlParser.K_SCHEMA, 0); }
		public SchemaNameContext schemaName() {
			return getRuleContext(SchemaNameContext.class,0);
		}
		public TerminalNode K_TABLE() { return getToken(OctopusSqlParser.K_TABLE, 0); }
		public TableNameContext tableName() {
			return getRuleContext(TableNameContext.class,0);
		}
		public TerminalNode K_COLUMN() { return getToken(OctopusSqlParser.K_COLUMN, 0); }
		public ColumnNameContext columnName() {
			return getRuleContext(ColumnNameContext.class,0);
		}
		public TerminalNode K_USER() { return getToken(OctopusSqlParser.K_USER, 0); }
		public UserContext user() {
			return getRuleContext(UserContext.class,0);
		}
		public CommentOnTargetContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_commentOnTarget; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).enterCommentOnTarget(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).exitCommentOnTarget(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OctopusSqlVisitor ) return ((OctopusSqlVisitor<? extends T>)visitor).visitCommentOnTarget(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CommentOnTargetContext commentOnTarget() throws RecognitionException {
		CommentOnTargetContext _localctx = new CommentOnTargetContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_commentOnTarget);
		try {
			setState(265);
			switch (_input.LA(1)) {
			case K_DATASOURCE:
				enterOuterAlt(_localctx, 1);
				{
				setState(248);
				match(K_DATASOURCE);
				setState(249);
				dataSource();
				}
				break;
			case K_SCHEMA:
				enterOuterAlt(_localctx, 2);
				{
				setState(250);
				match(K_SCHEMA);
				setState(251);
				schemaName();
				}
				break;
			case K_TABLE:
				enterOuterAlt(_localctx, 3);
				{
				setState(252);
				match(K_TABLE);
				setState(253);
				tableName();
				}
				break;
			case K_COLUMN:
				enterOuterAlt(_localctx, 4);
				{
				setState(254);
				match(K_COLUMN);
				setState(255);
				dataSource();
				setState(256);
				matchWildcard();
				setState(257);
				schemaName();
				setState(258);
				matchWildcard();
				setState(259);
				tableName();
				setState(260);
				matchWildcard();
				setState(261);
				columnName();
				}
				break;
			case K_USER:
				enterOuterAlt(_localctx, 5);
				{
				setState(263);
				match(K_USER);
				setState(264);
				user();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DataSourceContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER() { return getToken(OctopusSqlParser.IDENTIFIER, 0); }
		public DataSourceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dataSource; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).enterDataSource(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).exitDataSource(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OctopusSqlVisitor ) return ((OctopusSqlVisitor<? extends T>)visitor).visitDataSource(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DataSourceContext dataSource() throws RecognitionException {
		DataSourceContext _localctx = new DataSourceContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_dataSource);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(267);
			match(IDENTIFIER);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SchemaPatternContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER() { return getToken(OctopusSqlParser.IDENTIFIER, 0); }
		public SchemaPatternContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_schemaPattern; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).enterSchemaPattern(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).exitSchemaPattern(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OctopusSqlVisitor ) return ((OctopusSqlVisitor<? extends T>)visitor).visitSchemaPattern(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SchemaPatternContext schemaPattern() throws RecognitionException {
		SchemaPatternContext _localctx = new SchemaPatternContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_schemaPattern);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(269);
			match(IDENTIFIER);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TablePatternContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER() { return getToken(OctopusSqlParser.IDENTIFIER, 0); }
		public TablePatternContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tablePattern; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).enterTablePattern(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).exitTablePattern(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OctopusSqlVisitor ) return ((OctopusSqlVisitor<? extends T>)visitor).visitTablePattern(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TablePatternContext tablePattern() throws RecognitionException {
		TablePatternContext _localctx = new TablePatternContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_tablePattern);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(271);
			match(IDENTIFIER);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ColumnPatternContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER() { return getToken(OctopusSqlParser.IDENTIFIER, 0); }
		public ColumnPatternContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_columnPattern; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).enterColumnPattern(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).exitColumnPattern(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OctopusSqlVisitor ) return ((OctopusSqlVisitor<? extends T>)visitor).visitColumnPattern(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ColumnPatternContext columnPattern() throws RecognitionException {
		ColumnPatternContext _localctx = new ColumnPatternContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_columnPattern);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(273);
			match(IDENTIFIER);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SchemaNameContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER() { return getToken(OctopusSqlParser.IDENTIFIER, 0); }
		public SchemaNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_schemaName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).enterSchemaName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).exitSchemaName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OctopusSqlVisitor ) return ((OctopusSqlVisitor<? extends T>)visitor).visitSchemaName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SchemaNameContext schemaName() throws RecognitionException {
		SchemaNameContext _localctx = new SchemaNameContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_schemaName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(275);
			match(IDENTIFIER);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TableNameContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER() { return getToken(OctopusSqlParser.IDENTIFIER, 0); }
		public TableNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tableName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).enterTableName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).exitTableName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OctopusSqlVisitor ) return ((OctopusSqlVisitor<? extends T>)visitor).visitTableName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TableNameContext tableName() throws RecognitionException {
		TableNameContext _localctx = new TableNameContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_tableName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(277);
			match(IDENTIFIER);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ColumnNameContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER() { return getToken(OctopusSqlParser.IDENTIFIER, 0); }
		public ColumnNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_columnName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).enterColumnName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).exitColumnName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OctopusSqlVisitor ) return ((OctopusSqlVisitor<? extends T>)visitor).visitColumnName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ColumnNameContext columnName() throws RecognitionException {
		ColumnNameContext _localctx = new ColumnNameContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_columnName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(279);
			match(IDENTIFIER);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CommentContext extends ParserRuleContext {
		public TerminalNode STRING_LITERAL() { return getToken(OctopusSqlParser.STRING_LITERAL, 0); }
		public CommentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_comment; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).enterComment(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).exitComment(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OctopusSqlVisitor ) return ((OctopusSqlVisitor<? extends T>)visitor).visitComment(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CommentContext comment() throws RecognitionException {
		CommentContext _localctx = new CommentContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_comment);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(281);
			match(STRING_LITERAL);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ErrorContext extends ParserRuleContext {
		public Token UNEXPECTED_CHAR;
		public TerminalNode UNEXPECTED_CHAR() { return getToken(OctopusSqlParser.UNEXPECTED_CHAR, 0); }
		public ErrorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_error; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).enterError(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof OctopusSqlListener ) ((OctopusSqlListener)listener).exitError(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OctopusSqlVisitor ) return ((OctopusSqlVisitor<? extends T>)visitor).visitError(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ErrorContext error() throws RecognitionException {
		ErrorContext _localctx = new ErrorContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_error);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(283);
			((ErrorContext)_localctx).UNEXPECTED_CHAR = match(UNEXPECTED_CHAR);

			            throw new RuntimeException("UNEXPECTED_CHAR=" + (((ErrorContext)_localctx).UNEXPECTED_CHAR!=null?((ErrorContext)_localctx).UNEXPECTED_CHAR.getText():null));
			        
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3 \u0121\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\3\2\3\2\5\2I\n\2\3\2\3\2\3\3\7\3N\n\3\f\3\16\3Q\13\3"+
		"\3\3\3\3\6\3U\n\3\r\3\16\3V\3\3\7\3Z\n\3\f\3\16\3]\13\3\3\3\7\3`\n\3\f"+
		"\3\16\3c\13\3\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\5\4m\n\4\3\5\3\5\3\5\3\5"+
		"\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\7\3\7\3\b\3\b\3\t\3\t\3\t\3\t\3\t\3\t\3"+
		"\t\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\5\n\u008d\n\n\3\13\3\13\3\13\3\13\3"+
		"\f\3\f\3\r\3\r\3\16\3\16\3\17\3\17\3\17\3\17\3\20\3\20\3\20\3\20\3\21"+
		"\3\21\3\22\3\22\3\22\3\23\3\23\3\23\3\23\5\23\u00aa\n\23\3\23\3\23\5\23"+
		"\u00ae\n\23\3\24\3\24\3\24\3\24\5\24\u00b4\n\24\3\24\3\24\5\24\u00b8\n"+
		"\24\3\24\3\24\5\24\u00bc\n\24\3\25\3\25\3\25\3\25\5\25\u00c2\n\25\3\25"+
		"\3\25\5\25\u00c6\n\25\3\25\3\25\5\25\u00ca\n\25\3\25\3\25\5\25\u00ce\n"+
		"\25\3\26\3\26\3\26\3\26\3\26\5\26\u00d5\n\26\3\26\3\26\5\26\u00d9\n\26"+
		"\3\26\3\26\5\26\u00dd\n\26\3\27\3\27\3\27\3\27\3\27\5\27\u00e4\n\27\3"+
		"\27\3\27\5\27\u00e8\n\27\3\27\3\27\5\27\u00ec\n\27\3\27\3\27\5\27\u00f0"+
		"\n\27\3\30\3\30\3\30\3\31\3\31\3\31\3\31\3\31\3\31\3\32\3\32\3\32\3\32"+
		"\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\5\32"+
		"\u010c\n\32\3\33\3\33\3\34\3\34\3\35\3\35\3\36\3\36\3\37\3\37\3 \3 \3"+
		"!\3!\3\"\3\"\3#\3#\3#\3#\2\2$\2\4\6\b\n\f\16\20\22\24\26\30\32\34\36 "+
		"\"$&(*,.\60\62\64\668:<>@BD\2\2\u011f\2H\3\2\2\2\4O\3\2\2\2\6l\3\2\2\2"+
		"\bn\3\2\2\2\nr\3\2\2\2\fy\3\2\2\2\16{\3\2\2\2\20}\3\2\2\2\22\u0084\3\2"+
		"\2\2\24\u008e\3\2\2\2\26\u0092\3\2\2\2\30\u0094\3\2\2\2\32\u0096\3\2\2"+
		"\2\34\u0098\3\2\2\2\36\u009c\3\2\2\2 \u00a0\3\2\2\2\"\u00a2\3\2\2\2$\u00a5"+
		"\3\2\2\2&\u00af\3\2\2\2(\u00bd\3\2\2\2*\u00cf\3\2\2\2,\u00de\3\2\2\2."+
		"\u00f1\3\2\2\2\60\u00f4\3\2\2\2\62\u010b\3\2\2\2\64\u010d\3\2\2\2\66\u010f"+
		"\3\2\2\28\u0111\3\2\2\2:\u0113\3\2\2\2<\u0115\3\2\2\2>\u0117\3\2\2\2@"+
		"\u0119\3\2\2\2B\u011b\3\2\2\2D\u011d\3\2\2\2FI\5\4\3\2GI\5D#\2HF\3\2\2"+
		"\2HG\3\2\2\2IJ\3\2\2\2JK\7\2\2\3K\3\3\2\2\2LN\7\3\2\2ML\3\2\2\2NQ\3\2"+
		"\2\2OM\3\2\2\2OP\3\2\2\2PR\3\2\2\2QO\3\2\2\2R[\5\6\4\2SU\7\3\2\2TS\3\2"+
		"\2\2UV\3\2\2\2VT\3\2\2\2VW\3\2\2\2WX\3\2\2\2XZ\5\6\4\2YT\3\2\2\2Z]\3\2"+
		"\2\2[Y\3\2\2\2[\\\3\2\2\2\\a\3\2\2\2][\3\2\2\2^`\7\3\2\2_^\3\2\2\2`c\3"+
		"\2\2\2a_\3\2\2\2ab\3\2\2\2b\5\3\2\2\2ca\3\2\2\2dm\5\b\5\2em\5\20\t\2f"+
		"m\5\22\n\2gm\5\24\13\2hm\5\34\17\2im\5\36\20\2jm\5&\24\2km\5\60\31\2l"+
		"d\3\2\2\2le\3\2\2\2lf\3\2\2\2lg\3\2\2\2lh\3\2\2\2li\3\2\2\2lj\3\2\2\2"+
		"lk\3\2\2\2m\7\3\2\2\2no\7\5\2\2op\7\21\2\2pq\5\n\6\2q\t\3\2\2\2rs\7\4"+
		"\2\2st\7\t\2\2tu\5\f\7\2uv\7\7\2\2vw\7\6\2\2wx\5\16\b\2x\13\3\2\2\2yz"+
		"\7\35\2\2z\r\3\2\2\2{|\7\36\2\2|\17\3\2\2\2}~\7\b\2\2~\177\7\22\2\2\177"+
		"\u0080\5\26\f\2\u0080\u0081\7\16\2\2\u0081\u0082\7\6\2\2\u0082\u0083\5"+
		"\30\r\2\u0083\21\3\2\2\2\u0084\u0085\7\5\2\2\u0085\u0086\7\22\2\2\u0086"+
		"\u0087\5\26\f\2\u0087\u0088\7\16\2\2\u0088\u0089\7\6\2\2\u0089\u008c\5"+
		"\30\r\2\u008a\u008b\7\17\2\2\u008b\u008d\5\32\16\2\u008c\u008a\3\2\2\2"+
		"\u008c\u008d\3\2\2\2\u008d\23\3\2\2\2\u008e\u008f\7\r\2\2\u008f\u0090"+
		"\7\22\2\2\u0090\u0091\5\26\f\2\u0091\25\3\2\2\2\u0092\u0093\7\35\2\2\u0093"+
		"\27\3\2\2\2\u0094\u0095\7\36\2\2\u0095\31\3\2\2\2\u0096\u0097\7\36\2\2"+
		"\u0097\33\3\2\2\2\u0098\u0099\7\b\2\2\u0099\u009a\7\20\2\2\u009a\u009b"+
		"\5 \21\2\u009b\35\3\2\2\2\u009c\u009d\7\r\2\2\u009d\u009e\7\20\2\2\u009e"+
		"\u009f\5 \21\2\u009f\37\3\2\2\2\u00a0\u00a1\7\35\2\2\u00a1!\3\2\2\2\u00a2"+
		"\u00a3\7\23\2\2\u00a3\u00a4\7\24\2\2\u00a4#\3\2\2\2\u00a5\u00a6\7\23\2"+
		"\2\u00a6\u00a9\7\25\2\2\u00a7\u00a8\7\t\2\2\u00a8\u00aa\5\64\33\2\u00a9"+
		"\u00a7\3\2\2\2\u00a9\u00aa\3\2\2\2\u00aa\u00ad\3\2\2\2\u00ab\u00ac\7\n"+
		"\2\2\u00ac\u00ae\5\66\34\2\u00ad\u00ab\3\2\2\2\u00ad\u00ae\3\2\2\2\u00ae"+
		"%\3\2\2\2\u00af\u00b0\7\23\2\2\u00b0\u00b3\7\26\2\2\u00b1\u00b2\7\t\2"+
		"\2\u00b2\u00b4\5\64\33\2\u00b3\u00b1\3\2\2\2\u00b3\u00b4\3\2\2\2\u00b4"+
		"\u00b7\3\2\2\2\u00b5\u00b6\7\n\2\2\u00b6\u00b8\5\66\34\2\u00b7\u00b5\3"+
		"\2\2\2\u00b7\u00b8\3\2\2\2\u00b8\u00bb\3\2\2\2\u00b9\u00ba\7\13\2\2\u00ba"+
		"\u00bc\58\35\2\u00bb\u00b9\3\2\2\2\u00bb\u00bc\3\2\2\2\u00bc\'\3\2\2\2"+
		"\u00bd\u00be\7\23\2\2\u00be\u00c1\7\27\2\2\u00bf\u00c0\7\t\2\2\u00c0\u00c2"+
		"\5\64\33\2\u00c1\u00bf\3\2\2\2\u00c1\u00c2\3\2\2\2\u00c2\u00c5\3\2\2\2"+
		"\u00c3\u00c4\7\n\2\2\u00c4\u00c6\5\66\34\2\u00c5\u00c3\3\2\2\2\u00c5\u00c6"+
		"\3\2\2\2\u00c6\u00c9\3\2\2\2\u00c7\u00c8\7\13\2\2\u00c8\u00ca\58\35\2"+
		"\u00c9\u00c7\3\2\2\2\u00c9\u00ca\3\2\2\2\u00ca\u00cd\3\2\2\2\u00cb\u00cc"+
		"\7\f\2\2\u00cc\u00ce\5:\36\2\u00cd\u00cb\3\2\2\2\u00cd\u00ce\3\2\2\2\u00ce"+
		")\3\2\2\2\u00cf\u00d0\7\23\2\2\u00d0\u00d1\7\13\2\2\u00d1\u00d4\7\30\2"+
		"\2\u00d2\u00d3\7\t\2\2\u00d3\u00d5\5\64\33\2\u00d4\u00d2\3\2\2\2\u00d4"+
		"\u00d5\3\2\2\2\u00d5\u00d8\3\2\2\2\u00d6\u00d7\7\n\2\2\u00d7\u00d9\5\66"+
		"\34\2\u00d8\u00d6\3\2\2\2\u00d8\u00d9\3\2\2\2\u00d9\u00dc\3\2\2\2\u00da"+
		"\u00db\7\13\2\2\u00db\u00dd\58\35\2\u00dc\u00da\3\2\2\2\u00dc\u00dd\3"+
		"\2\2\2\u00dd+\3\2\2\2\u00de\u00df\7\23\2\2\u00df\u00e0\7\f\2\2\u00e0\u00e3"+
		"\7\30\2\2\u00e1\u00e2\7\t\2\2\u00e2\u00e4\5\64\33\2\u00e3\u00e1\3\2\2"+
		"\2\u00e3\u00e4\3\2\2\2\u00e4\u00e7\3\2\2\2\u00e5\u00e6\7\n\2\2\u00e6\u00e8"+
		"\5\66\34\2\u00e7\u00e5\3\2\2\2\u00e7\u00e8\3\2\2\2\u00e8\u00eb\3\2\2\2"+
		"\u00e9\u00ea\7\13\2\2\u00ea\u00ec\58\35\2\u00eb\u00e9\3\2\2\2\u00eb\u00ec"+
		"\3\2\2\2\u00ec\u00ef\3\2\2\2\u00ed\u00ee\7\f\2\2\u00ee\u00f0\5:\36\2\u00ef"+
		"\u00ed\3\2\2\2\u00ef\u00f0\3\2\2\2\u00f0-\3\2\2\2\u00f1\u00f2\7\23\2\2"+
		"\u00f2\u00f3\7\31\2\2\u00f3/\3\2\2\2\u00f4\u00f5\7\32\2\2\u00f5\u00f6"+
		"\7\33\2\2\u00f6\u00f7\5\62\32\2\u00f7\u00f8\7\34\2\2\u00f8\u00f9\5B\""+
		"\2\u00f9\61\3\2\2\2\u00fa\u00fb\7\t\2\2\u00fb\u010c\5\64\33\2\u00fc\u00fd"+
		"\7\n\2\2\u00fd\u010c\5<\37\2\u00fe\u00ff\7\13\2\2\u00ff\u010c\5> \2\u0100"+
		"\u0101\7\f\2\2\u0101\u0102\5\64\33\2\u0102\u0103\13\2\2\2\u0103\u0104"+
		"\5<\37\2\u0104\u0105\13\2\2\2\u0105\u0106\5> \2\u0106\u0107\13\2\2\2\u0107"+
		"\u0108\5@!\2\u0108\u010c\3\2\2\2\u0109\u010a\7\22\2\2\u010a\u010c\5\26"+
		"\f\2\u010b\u00fa\3\2\2\2\u010b\u00fc\3\2\2\2\u010b\u00fe\3\2\2\2\u010b"+
		"\u0100\3\2\2\2\u010b\u0109\3\2\2\2\u010c\63\3\2\2\2\u010d\u010e\7\35\2"+
		"\2\u010e\65\3\2\2\2\u010f\u0110\7\35\2\2\u0110\67\3\2\2\2\u0111\u0112"+
		"\7\35\2\2\u01129\3\2\2\2\u0113\u0114\7\35\2\2\u0114;\3\2\2\2\u0115\u0116"+
		"\7\35\2\2\u0116=\3\2\2\2\u0117\u0118\7\35\2\2\u0118?\3\2\2\2\u0119\u011a"+
		"\7\35\2\2\u011aA\3\2\2\2\u011b\u011c\7\36\2\2\u011cC\3\2\2\2\u011d\u011e"+
		"\7 \2\2\u011e\u011f\b#\1\2\u011fE\3\2\2\2\32HOV[al\u008c\u00a9\u00ad\u00b3"+
		"\u00b7\u00bb\u00c1\u00c5\u00c9\u00cd\u00d4\u00d8\u00dc\u00e3\u00e7\u00eb"+
		"\u00ef\u010b";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}