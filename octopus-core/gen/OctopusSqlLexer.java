// Generated from /home/kisung/octopus/octopus-core/src/main/antlr4/kr/co/bitnine/octopus/sql/OctopusSql.g4 by ANTLR 4.5.1

    import org.apache.commons.lang3.StringUtils;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class OctopusSqlLexer extends Lexer {
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
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"T__0", "K_ADD", "K_ALTER", "K_BY", "K_CONNECT", "K_CREATE", "K_DATASOURCE", 
		"K_SCHEMA", "K_TABLE", "K_COLUMN", "K_DROP", "K_IDENTIFIED", "K_REPLACE", 
		"K_ROLE", "K_SYSTEM", "K_USER", "K_SHOW", "K_DATASOURCES", "K_SCHEMAS", 
		"K_TABLES", "K_COLUMNS", "K_PRIVILEGES", "K_USERS", "K_COMMENT", "K_ON", 
		"K_IS", "IDENTIFIER", "STRING_LITERAL", "WHITESPACES", "UNEXPECTED_CHAR", 
		"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", 
		"O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "LETTER", 
		"DIGIT"
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


	public OctopusSqlLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "OctopusSql.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	@Override
	public void action(RuleContext _localctx, int ruleIndex, int actionIndex) {
		switch (ruleIndex) {
		case 26:
			IDENTIFIER_action((RuleContext)_localctx, actionIndex);
			break;
		case 27:
			STRING_LITERAL_action((RuleContext)_localctx, actionIndex);
			break;
		}
	}
	private void IDENTIFIER_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 0:

			            setText(StringUtils.strip(getText(), "\"").replace("\"\"", "\""));
			        
			break;
		case 1:

			            setText(StringUtils.strip(getText(), "`").replace("``", "`"));
			        
			break;
		}
	}
	private void STRING_LITERAL_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 2:

			            setText(StringUtils.strip(getText(), "'").replace("''", "'"));
			        
			break;
		}
	}

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2 \u0199\b\1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"+
		",\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\4\64\t"+
		"\64\4\65\t\65\4\66\t\66\4\67\t\67\48\t8\49\t9\4:\t:\4;\t;\3\2\3\2\3\3"+
		"\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3\4\3\4\3\5\3\5\3\5\3\6\3\6\3\6\3\6\3\6\3"+
		"\6\3\6\3\6\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b"+
		"\3\b\3\b\3\b\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\n\3\n\3\n\3\n\3\n\3\n\3\13"+
		"\3\13\3\13\3\13\3\13\3\13\3\13\3\f\3\f\3\f\3\f\3\f\3\r\3\r\3\r\3\r\3\r"+
		"\3\r\3\r\3\r\3\r\3\r\3\r\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\17"+
		"\3\17\3\17\3\17\3\17\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\21\3\21\3\21"+
		"\3\21\3\21\3\22\3\22\3\22\3\22\3\22\3\23\3\23\3\23\3\23\3\23\3\23\3\23"+
		"\3\23\3\23\3\23\3\23\3\23\3\24\3\24\3\24\3\24\3\24\3\24\3\24\3\24\3\25"+
		"\3\25\3\25\3\25\3\25\3\25\3\25\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26"+
		"\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\30\3\30\3\30"+
		"\3\30\3\30\3\30\3\31\3\31\3\31\3\31\3\31\3\31\3\31\3\31\3\32\3\32\3\32"+
		"\3\33\3\33\3\33\3\34\3\34\3\34\3\34\7\34\u0129\n\34\f\34\16\34\u012c\13"+
		"\34\3\34\3\34\3\34\3\34\3\34\3\34\7\34\u0134\n\34\f\34\16\34\u0137\13"+
		"\34\3\34\3\34\3\34\3\34\7\34\u013d\n\34\f\34\16\34\u0140\13\34\3\34\3"+
		"\34\3\34\3\34\7\34\u0146\n\34\f\34\16\34\u0149\13\34\5\34\u014b\n\34\3"+
		"\35\3\35\3\35\3\35\7\35\u0151\n\35\f\35\16\35\u0154\13\35\3\35\3\35\3"+
		"\35\3\36\6\36\u015a\n\36\r\36\16\36\u015b\3\36\3\36\3\37\3\37\3 \3 \3"+
		"!\3!\3\"\3\"\3#\3#\3$\3$\3%\3%\3&\3&\3\'\3\'\3(\3(\3)\3)\3*\3*\3+\3+\3"+
		",\3,\3-\3-\3.\3.\3/\3/\3\60\3\60\3\61\3\61\3\62\3\62\3\63\3\63\3\64\3"+
		"\64\3\65\3\65\3\66\3\66\3\67\3\67\38\38\39\39\3:\3:\3;\3;\2\2<\3\3\5\4"+
		"\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35\20\37\21!\22"+
		"#\23%\24\'\25)\26+\27-\30/\31\61\32\63\33\65\34\67\359\36;\37= ?\2A\2"+
		"C\2E\2G\2I\2K\2M\2O\2Q\2S\2U\2W\2Y\2[\2]\2_\2a\2c\2e\2g\2i\2k\2m\2o\2"+
		"q\2s\2u\2\3\2#\5\2\f\f\17\17$$\5\2\f\f\17\17bb\5\2\f\f\17\17^_\5\2\f\f"+
		"\17\17))\5\2\13\f\17\17\"\"\4\2CCcc\4\2DDdd\4\2EEee\4\2FFff\4\2GGgg\4"+
		"\2HHhh\4\2IIii\4\2JJjj\4\2KKkk\4\2LLll\4\2MMmm\4\2NNnn\4\2OOoo\4\2PPp"+
		"p\4\2QQqq\4\2RRrr\4\2SSss\4\2TTtt\4\2UUuu\4\2VVvv\4\2WWww\4\2XXxx\4\2"+
		"YYyy\4\2ZZzz\4\2[[{{\4\2\\\\||\5\2C\\aac|\3\2\62;\u0189\2\3\3\2\2\2\2"+
		"\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2"+
		"\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2"+
		"\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2"+
		"\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2\2/\3\2\2\2\2\61\3\2\2\2"+
		"\2\63\3\2\2\2\2\65\3\2\2\2\2\67\3\2\2\2\29\3\2\2\2\2;\3\2\2\2\2=\3\2\2"+
		"\2\3w\3\2\2\2\5y\3\2\2\2\7}\3\2\2\2\t\u0083\3\2\2\2\13\u0086\3\2\2\2\r"+
		"\u008e\3\2\2\2\17\u0095\3\2\2\2\21\u00a0\3\2\2\2\23\u00a7\3\2\2\2\25\u00ad"+
		"\3\2\2\2\27\u00b4\3\2\2\2\31\u00b9\3\2\2\2\33\u00c4\3\2\2\2\35\u00cc\3"+
		"\2\2\2\37\u00d1\3\2\2\2!\u00d8\3\2\2\2#\u00dd\3\2\2\2%\u00e2\3\2\2\2\'"+
		"\u00ee\3\2\2\2)\u00f6\3\2\2\2+\u00fd\3\2\2\2-\u0105\3\2\2\2/\u0110\3\2"+
		"\2\2\61\u0116\3\2\2\2\63\u011e\3\2\2\2\65\u0121\3\2\2\2\67\u014a\3\2\2"+
		"\29\u014c\3\2\2\2;\u0159\3\2\2\2=\u015f\3\2\2\2?\u0161\3\2\2\2A\u0163"+
		"\3\2\2\2C\u0165\3\2\2\2E\u0167\3\2\2\2G\u0169\3\2\2\2I\u016b\3\2\2\2K"+
		"\u016d\3\2\2\2M\u016f\3\2\2\2O\u0171\3\2\2\2Q\u0173\3\2\2\2S\u0175\3\2"+
		"\2\2U\u0177\3\2\2\2W\u0179\3\2\2\2Y\u017b\3\2\2\2[\u017d\3\2\2\2]\u017f"+
		"\3\2\2\2_\u0181\3\2\2\2a\u0183\3\2\2\2c\u0185\3\2\2\2e\u0187\3\2\2\2g"+
		"\u0189\3\2\2\2i\u018b\3\2\2\2k\u018d\3\2\2\2m\u018f\3\2\2\2o\u0191\3\2"+
		"\2\2q\u0193\3\2\2\2s\u0195\3\2\2\2u\u0197\3\2\2\2wx\7=\2\2x\4\3\2\2\2"+
		"yz\5? \2z{\5E#\2{|\5E#\2|\6\3\2\2\2}~\5? \2~\177\5U+\2\177\u0080\5e\63"+
		"\2\u0080\u0081\5G$\2\u0081\u0082\5a\61\2\u0082\b\3\2\2\2\u0083\u0084\5"+
		"A!\2\u0084\u0085\5o8\2\u0085\n\3\2\2\2\u0086\u0087\5C\"\2\u0087\u0088"+
		"\5[.\2\u0088\u0089\5Y-\2\u0089\u008a\5Y-\2\u008a\u008b\5G$\2\u008b\u008c"+
		"\5C\"\2\u008c\u008d\5e\63\2\u008d\f\3\2\2\2\u008e\u008f\5C\"\2\u008f\u0090"+
		"\5a\61\2\u0090\u0091\5G$\2\u0091\u0092\5? \2\u0092\u0093\5e\63\2\u0093"+
		"\u0094\5G$\2\u0094\16\3\2\2\2\u0095\u0096\5E#\2\u0096\u0097\5? \2\u0097"+
		"\u0098\5e\63\2\u0098\u0099\5? \2\u0099\u009a\5c\62\2\u009a\u009b\5[.\2"+
		"\u009b\u009c\5g\64\2\u009c\u009d\5a\61\2\u009d\u009e\5C\"\2\u009e\u009f"+
		"\5G$\2\u009f\20\3\2\2\2\u00a0\u00a1\5c\62\2\u00a1\u00a2\5C\"\2\u00a2\u00a3"+
		"\5M\'\2\u00a3\u00a4\5G$\2\u00a4\u00a5\5W,\2\u00a5\u00a6\5? \2\u00a6\22"+
		"\3\2\2\2\u00a7\u00a8\5e\63\2\u00a8\u00a9\5? \2\u00a9\u00aa\5A!\2\u00aa"+
		"\u00ab\5U+\2\u00ab\u00ac\5G$\2\u00ac\24\3\2\2\2\u00ad\u00ae\5C\"\2\u00ae"+
		"\u00af\5[.\2\u00af\u00b0\5U+\2\u00b0\u00b1\5g\64\2\u00b1\u00b2\5W,\2\u00b2"+
		"\u00b3\5Y-\2\u00b3\26\3\2\2\2\u00b4\u00b5\5E#\2\u00b5\u00b6\5a\61\2\u00b6"+
		"\u00b7\5[.\2\u00b7\u00b8\5]/\2\u00b8\30\3\2\2\2\u00b9\u00ba\5O(\2\u00ba"+
		"\u00bb\5E#\2\u00bb\u00bc\5G$\2\u00bc\u00bd\5Y-\2\u00bd\u00be\5e\63\2\u00be"+
		"\u00bf\5O(\2\u00bf\u00c0\5I%\2\u00c0\u00c1\5O(\2\u00c1\u00c2\5G$\2\u00c2"+
		"\u00c3\5E#\2\u00c3\32\3\2\2\2\u00c4\u00c5\5a\61\2\u00c5\u00c6\5G$\2\u00c6"+
		"\u00c7\5]/\2\u00c7\u00c8\5U+\2\u00c8\u00c9\5? \2\u00c9\u00ca\5C\"\2\u00ca"+
		"\u00cb\5G$\2\u00cb\34\3\2\2\2\u00cc\u00cd\5a\61\2\u00cd\u00ce\5[.\2\u00ce"+
		"\u00cf\5U+\2\u00cf\u00d0\5G$\2\u00d0\36\3\2\2\2\u00d1\u00d2\5c\62\2\u00d2"+
		"\u00d3\5o8\2\u00d3\u00d4\5c\62\2\u00d4\u00d5\5e\63\2\u00d5\u00d6\5G$\2"+
		"\u00d6\u00d7\5W,\2\u00d7 \3\2\2\2\u00d8\u00d9\5g\64\2\u00d9\u00da\5c\62"+
		"\2\u00da\u00db\5G$\2\u00db\u00dc\5a\61\2\u00dc\"\3\2\2\2\u00dd\u00de\5"+
		"c\62\2\u00de\u00df\5M\'\2\u00df\u00e0\5[.\2\u00e0\u00e1\5k\66\2\u00e1"+
		"$\3\2\2\2\u00e2\u00e3\5E#\2\u00e3\u00e4\5? \2\u00e4\u00e5\5e\63\2\u00e5"+
		"\u00e6\5? \2\u00e6\u00e7\5c\62\2\u00e7\u00e8\5[.\2\u00e8\u00e9\5g\64\2"+
		"\u00e9\u00ea\5a\61\2\u00ea\u00eb\5C\"\2\u00eb\u00ec\5G$\2\u00ec\u00ed"+
		"\5c\62\2\u00ed&\3\2\2\2\u00ee\u00ef\5c\62\2\u00ef\u00f0\5C\"\2\u00f0\u00f1"+
		"\5M\'\2\u00f1\u00f2\5G$\2\u00f2\u00f3\5W,\2\u00f3\u00f4\5? \2\u00f4\u00f5"+
		"\5c\62\2\u00f5(\3\2\2\2\u00f6\u00f7\5e\63\2\u00f7\u00f8\5? \2\u00f8\u00f9"+
		"\5A!\2\u00f9\u00fa\5U+\2\u00fa\u00fb\5G$\2\u00fb\u00fc\5c\62\2\u00fc*"+
		"\3\2\2\2\u00fd\u00fe\5C\"\2\u00fe\u00ff\5[.\2\u00ff\u0100\5U+\2\u0100"+
		"\u0101\5g\64\2\u0101\u0102\5W,\2\u0102\u0103\5Y-\2\u0103\u0104\5c\62\2"+
		"\u0104,\3\2\2\2\u0105\u0106\5]/\2\u0106\u0107\5a\61\2\u0107\u0108\5O("+
		"\2\u0108\u0109\5i\65\2\u0109\u010a\5O(\2\u010a\u010b\5U+\2\u010b\u010c"+
		"\5G$\2\u010c\u010d\5K&\2\u010d\u010e\5G$\2\u010e\u010f\5c\62\2\u010f."+
		"\3\2\2\2\u0110\u0111\5g\64\2\u0111\u0112\5c\62\2\u0112\u0113\5G$\2\u0113"+
		"\u0114\5a\61\2\u0114\u0115\5c\62\2\u0115\60\3\2\2\2\u0116\u0117\5C\"\2"+
		"\u0117\u0118\5[.\2\u0118\u0119\5W,\2\u0119\u011a\5W,\2\u011a\u011b\5G"+
		"$\2\u011b\u011c\5Y-\2\u011c\u011d\5e\63\2\u011d\62\3\2\2\2\u011e\u011f"+
		"\5[.\2\u011f\u0120\5Y-\2\u0120\64\3\2\2\2\u0121\u0122\5O(\2\u0122\u0123"+
		"\5c\62\2\u0123\66\3\2\2\2\u0124\u012a\7$\2\2\u0125\u0129\n\2\2\2\u0126"+
		"\u0127\7$\2\2\u0127\u0129\7$\2\2\u0128\u0125\3\2\2\2\u0128\u0126\3\2\2"+
		"\2\u0129\u012c\3\2\2\2\u012a\u0128\3\2\2\2\u012a\u012b\3\2\2\2\u012b\u012d"+
		"\3\2\2\2\u012c\u012a\3\2\2\2\u012d\u012e\7$\2\2\u012e\u014b\b\34\2\2\u012f"+
		"\u0135\7b\2\2\u0130\u0134\n\3\2\2\u0131\u0132\7b\2\2\u0132\u0134\7b\2"+
		"\2\u0133\u0130\3\2\2\2\u0133\u0131\3\2\2\2\u0134\u0137\3\2\2\2\u0135\u0133"+
		"\3\2\2\2\u0135\u0136\3\2\2\2\u0136\u0138\3\2\2\2\u0137\u0135\3\2\2\2\u0138"+
		"\u0139\7b\2\2\u0139\u014b\b\34\3\2\u013a\u013e\7]\2\2\u013b\u013d\n\4"+
		"\2\2\u013c\u013b\3\2\2\2\u013d\u0140\3\2\2\2\u013e\u013c\3\2\2\2\u013e"+
		"\u013f\3\2\2\2\u013f\u0141\3\2\2\2\u0140\u013e\3\2\2\2\u0141\u014b\7_"+
		"\2\2\u0142\u0147\5s:\2\u0143\u0146\5s:\2\u0144\u0146\5u;\2\u0145\u0143"+
		"\3\2\2\2\u0145\u0144\3\2\2\2\u0146\u0149\3\2\2\2\u0147\u0145\3\2\2\2\u0147"+
		"\u0148\3\2\2\2\u0148\u014b\3\2\2\2\u0149\u0147\3\2\2\2\u014a\u0124\3\2"+
		"\2\2\u014a\u012f\3\2\2\2\u014a\u013a\3\2\2\2\u014a\u0142\3\2\2\2\u014b"+
		"8\3\2\2\2\u014c\u0152\7)\2\2\u014d\u0151\n\5\2\2\u014e\u014f\7)\2\2\u014f"+
		"\u0151\7)\2\2\u0150\u014d\3\2\2\2\u0150\u014e\3\2\2\2\u0151\u0154\3\2"+
		"\2\2\u0152\u0150\3\2\2\2\u0152\u0153\3\2\2\2\u0153\u0155\3\2\2\2\u0154"+
		"\u0152\3\2\2\2\u0155\u0156\7)\2\2\u0156\u0157\b\35\4\2\u0157:\3\2\2\2"+
		"\u0158\u015a\t\6\2\2\u0159\u0158\3\2\2\2\u015a\u015b\3\2\2\2\u015b\u0159"+
		"\3\2\2\2\u015b\u015c\3\2\2\2\u015c\u015d\3\2\2\2\u015d\u015e\b\36\5\2"+
		"\u015e<\3\2\2\2\u015f\u0160\13\2\2\2\u0160>\3\2\2\2\u0161\u0162\t\7\2"+
		"\2\u0162@\3\2\2\2\u0163\u0164\t\b\2\2\u0164B\3\2\2\2\u0165\u0166\t\t\2"+
		"\2\u0166D\3\2\2\2\u0167\u0168\t\n\2\2\u0168F\3\2\2\2\u0169\u016a\t\13"+
		"\2\2\u016aH\3\2\2\2\u016b\u016c\t\f\2\2\u016cJ\3\2\2\2\u016d\u016e\t\r"+
		"\2\2\u016eL\3\2\2\2\u016f\u0170\t\16\2\2\u0170N\3\2\2\2\u0171\u0172\t"+
		"\17\2\2\u0172P\3\2\2\2\u0173\u0174\t\20\2\2\u0174R\3\2\2\2\u0175\u0176"+
		"\t\21\2\2\u0176T\3\2\2\2\u0177\u0178\t\22\2\2\u0178V\3\2\2\2\u0179\u017a"+
		"\t\23\2\2\u017aX\3\2\2\2\u017b\u017c\t\24\2\2\u017cZ\3\2\2\2\u017d\u017e"+
		"\t\25\2\2\u017e\\\3\2\2\2\u017f\u0180\t\26\2\2\u0180^\3\2\2\2\u0181\u0182"+
		"\t\27\2\2\u0182`\3\2\2\2\u0183\u0184\t\30\2\2\u0184b\3\2\2\2\u0185\u0186"+
		"\t\31\2\2\u0186d\3\2\2\2\u0187\u0188\t\32\2\2\u0188f\3\2\2\2\u0189\u018a"+
		"\t\33\2\2\u018ah\3\2\2\2\u018b\u018c\t\34\2\2\u018cj\3\2\2\2\u018d\u018e"+
		"\t\35\2\2\u018el\3\2\2\2\u018f\u0190\t\36\2\2\u0190n\3\2\2\2\u0191\u0192"+
		"\t\37\2\2\u0192p\3\2\2\2\u0193\u0194\t \2\2\u0194r\3\2\2\2\u0195\u0196"+
		"\t!\2\2\u0196t\3\2\2\2\u0197\u0198\t\"\2\2\u0198v\3\2\2\2\16\2\u0128\u012a"+
		"\u0133\u0135\u013e\u0145\u0147\u014a\u0150\u0152\u015b\6\3\34\2\3\34\3"+
		"\3\35\4\2\3\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}