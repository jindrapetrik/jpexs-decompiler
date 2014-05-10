/*
 * Copyright 2008 Ayman Al-Sairafi ayman.alsairafi@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License
 *       at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jsyntaxpane.lexers;


import jsyntaxpane.Token;
import jsyntaxpane.TokenType;

%%

%public
%class SqlLexer
%extends DefaultJFlexLexer
%final
%unicode
%char
%type Token
%caseless


%{
    /**
     * Default constructor is needed as we will always call the yyreset
     */
    public SqlLexer() {
        super();
    }

    @Override
    public int yychar() {
        return yychar;
    }

%}

/* main character classes */
LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]

WhiteSpace = {LineTerminator} | [ \t\f]

/* comments */
Comment = {TraditionalComment} | {EndOfLineComment} | {DocumentationComment}

TraditionalComment = "/*" [^*] ~"*/" | "/*" "*"+ "/"
DocumentationComment = "/**" {CommentContent} "*"+ "/"
CommentContent       = ( [^*] | \*+ [^/*] )*
EndOfLineComment = "--" {InputCharacter}* {LineTerminator}?

/* identifiers */
Identifier = [:jletter:][:jletterdigit:]*

/* integer literals */
DecIntegerLiteral = 0 | [1-9][0-9]*

/* floating point literals */
FloatLiteral  = ({FLit1}|{FLit2}|{FLit3}) {Exponent}? [fF]

FLit1    = [0-9]+ \. [0-9]*
FLit2    = \. [0-9]+
FLit3    = [0-9]+
Exponent = [eE] [+-]? [0-9]+

/* string and character literals */
StringCharacter = [^\r\n\"\\]
SingleCharacter = [^\r\n\'\\]

// Create states for Double Quoted and Single Quoted Strings
%state DQ_STRING, SQ_STRING

Reserved =
   "ADD"                 |
   "ALL"                 |
   "ALLOW REVERSE SCANS" |
   "ALTER"               |
   "ANALYZE"             |
   "AND"                 |
   "AS"                  |
   "ASC"                 |
   "AUTOMATIC"           |
   "BEGIN"		 |
   "BEFORE"              |
   "BETWEEN"             |
   "BIGINT"              |
   "BINARY"              |
   "BLOB"                |
   "BOTH"                |
   "BUFFERPOOL"		 |
   "BY"                  |
   "CACHE"		 |
   "CALL"                |
   "CASCADE"             |
   "CASE"                |
   "CHANGE"              |
   "CHAR"                |
   "CHARACTER"           |
   "CHECK"               |
   "COLLATE"             |
   "COLUMN"              |
   "COMMIT"		 |
   "CONDITION"           |
   "CONSTANT"		 |
   "CONSTRAINT"          |
   "CONTINUE"            |
   "CONVERT"             |
   "CREATE"              |
   "CROSS"               |
   "CURSOR"              |
   "DATE"		 |
   "DATABASE"            |
   "DATABASES"           |
   "DEC"                 |
   "DECIMAL"             |
   "DECODE"		 |
   "DECLARE"             |
   "DEFAULT"             |
   "DELAYED"             |
   "DELETE"              |
   "DESC"                |
   "DESCRIBE"            |
   "DETERMINISTIC"       |
   "DISTINCT"            |
   "DISTINCTROW"         |
   "DIV"                 |
   "DOUBLE"              |
   "DROP"                |
   "DUAL"                |
   "EACH"                |
   "ELSE"                |
   "ELSEIF"              |
   "ENCLOSED"            |
   "END"		 |
   "ESCAPED"             |
   "EXCEPTION" 		 |
   "EXISTS"              |
   "EXIT"                |
   "EXPLAIN"             |
   "FALSE"               |
   "FETCH"               |
   "FLOAT"               |
   "FLOAT4"              |
   "FLOAT8"              |
   "FOR"                 |
   "FORCE"               |
   "FOREIGN"             |
   "FROM"                |
   "FUNCTION"		 |
   "FULLTEXT"            |
   "GLOBAL TEMPORARY"	 |
   "GRANT"               |
   "GROUP"               |
   "HAVING"              |
   "IF"                  |
   "IGNORE"              |
   "IN"                  |
   "INDEX"               |
   "INFILE"              |
   "INNER"               |
   "INOUT"               |
   "INSENSITIVE"         |
   "INSERT"              |
   "INT"                 |
   "INTEGER"             |
   "INTERVAL"            |
   "INTO"                |
   "IS"                  |
   "IS REF CURSOR"	 |
   "ITERATE"             |
   "JOIN"                |
   "KEY"                 |
   "KEYS"                |
   "KILL"                |
   "LEADING"             |
   "LEAVE"               |
   "LEFT"                |
   "LIKE"                |
   "LIMIT"               |
   "LINES"               |
   "LOAD"                |
   "LOCK"                |
   "LONG"                |
   "LOOP"                |
   "MATCH"               |
   "MERGE"               |
   "MINVALUE"		 |
   "MAXVALUE"		 |
   "MOD"                 |
   "MODIFIES"            |
   "NATURAL"             |
   "NOCYCLE"		 |
   "NOORDER"		 |
   "NOT"                 |
   "NULL"                |
   "NUMERIC"             |
   "NUMBER"              |
   "ON"                  |
   "OPEN"		 |
   "OPTIMIZE"            |
   "OPTION"              |
   "OPTIONALLY"          |
   "OR"                  |
   "ORDER"               |
   "OTHERS"		 |
   "OUT"                 |
   "OUTER"               |
   "OUTFILE"             |
   "PACKAGE"		 |
   "PACKAGE BODY"	 |
   "PAGESIZE"		 |
   "PLS_INTEGER"	 |
   "PRAGMA"		 |
   "PRECISION"           |
   "PRIMARY"             |
   "PROCEDURE"           |
   "PURGE"               |
   "RAISE"		 |
   "READ"                |
   "READS"               |
   "REAL"                |
   "REFERENCES"          |
   "REGEXP"              |
   "RELEASE"             |
   "RENAME"              |
   "REPEAT"              |
   "REPLACE"             |
   "REQUIRE"             |
   "RESTRICT"            |
   "RETURN"              |
   "REVOKE"              |
   "RIGHT"               |
   "RLIKE"               |
   "ROLLBACK"		 |
   "ROWCOUNT"		 |
   "ROWTYPE"		 |
   "SIZE"		 |
   "SCHEMA"              |
   "SCHEMAS"             |
   "SELECT"              |
   "SENSITIVE"           |
   "SEPARATOR"           |
   "SEQUENCE"		 |
   "SET"                 |
   "SHOW"                |
   "SMALLINT"            |
   "SONAME"              |
   "SPATIAL"             |
   "SPECIFIC"            |
   "SQL"                 |
   "SQLEXCEPTION"        |
   "SQLSTATE"            |
   "SQLWARNING"          |
   "STARTING"            |
   "SYSDATE"		 |
   "TABLE"               |
   "TABLESPACE"		 |
   "TERMINATED"          |
   "THEN"                |
   "TO"                  |
   "TO_CHAR"		 |
   "TO_DATE"		 |
   "TRAILING"            |
   "TRIGGER"             |
   "TRUE"                |
   "TRUNCATE"            |
   "TYPE"		 |
   "UNDO"                |
   "UNION"               |
   "UNIQUE"              |
   "UNLOCK"              |
   "UNSIGNED"            |
   "UPDATE"              |
   "USAGE"               |
   "USE"                 |
   "USER"		 |
   "USING"               |
   "VALUES"              |
   "VARBINARY"           |
   "VARCHAR"             |
   "VARCHAR2"            |
   "VARCHARACTER"        |
   "VARYING"             |
   "WHEN"                |
   "WHERE"               |
   "WHILE"               |
   "WITH"                |
   "WRITE"               |
   "XOR"                 |
   "ZEROFILL"
%%

<YYINITIAL> {

  /* keywords */
  {Reserved}                     { return token(TokenType.KEYWORD); }

  /* operators */

  "("                            |
  ")"                            |
  "{"                            |
  "}"                            |
  "["                            |
  "]"                            |
  ";"                            |
  ","                            |
  "."                            |
  "@"                            |
  "="                            |
  ">"                            |
  "<"                            |
  "!"                            |
  "~"                            |
  "?"                            |
  ":"                            { return token(TokenType.OPERATOR); }

  /* string literal */
  \"                             {
                                    yybegin(DQ_STRING);
                                    tokenStart = yychar;
                                    tokenLength = 1;
                                 }
  \'                             {
                                    yybegin(SQ_STRING);
                                    tokenStart = yychar;
                                    tokenLength = 1;
                                 }

  /* numeric literals */

  {DecIntegerLiteral}            |

  {FloatLiteral}                 { return token(TokenType.NUMBER); }

  /* comments */
  {Comment}                      { return token(TokenType.COMMENT); }

  /* whitespace */
  {WhiteSpace}+                  { /* skip */ }

  /* identifiers */
  {Identifier}                   { return token(TokenType.IDENTIFIER); }

}

<DQ_STRING> {
  {StringCharacter}+             { tokenLength += yylength(); }
  \"\"                           { tokenLength += 2; }
  \\.                            { tokenLength += 2; }
  {LineTerminator}               { yybegin(YYINITIAL);  }
  \"                             {
                                     yybegin(YYINITIAL);
                                     // length also includes the trailing quote
                                     return token(TokenType.STRING, tokenStart, tokenLength + 1);
                                 }
}

<SQ_STRING> {
  {SingleCharacter}+             { tokenLength += yylength(); }
  \'\'                           { tokenLength += 2; }
  \\.                            { tokenLength += 2; }
  {LineTerminator}               { yybegin(YYINITIAL);  }
  \'                             {
                                     yybegin(YYINITIAL);
                                     // length also includes the trailing quote
                                     return token(TokenType.STRING, tokenStart, tokenLength + 1);
                                 }
}

/* error fallback */
.|\n                             {  }
<<EOF>>                          { return null; }

