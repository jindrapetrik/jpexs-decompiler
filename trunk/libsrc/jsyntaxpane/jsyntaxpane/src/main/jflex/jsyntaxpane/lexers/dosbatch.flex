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
%class DOSBatchLexer
%extends DefaultJFlexLexer
%final
%unicode
%char
%type Token
%ignorecase
%state ECHO_TEXT

%{
    /**
     * Create an empty lexer, yyrset will be called later to reset and assign
     * the reader
     */
    public DOSBatchLexer() {
        super();
    }

    @Override
    public int yychar() {
        return yychar;
    }
%}

StartComment = "rem"
LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]

Comment = {StartComment} {InputCharacter}* {LineTerminator}?

%%

<YYINITIAL> {
  /* DOS keywords */
  "@"                           |
  "goto"                        |
  "call"                        |
  "exit"                        |
  "if"                          |
  "else"                        |
  "for"                         |
  "copy"                        |
  "set"                         |
  "dir"                         |
  "cd"                          |
  "set"                         |
  "errorlevel"                  { return token(TokenType.KEYWORD); }

  "%" [:jletter:] [:jletterdigit:]* "%"           {  return token(TokenType.STRING2); }

  "%" [:digit:]+                {  return token(TokenType.KEYWORD2); }

  "echo"       {
                 yybegin(ECHO_TEXT);
                 return token(TokenType.KEYWORD);
               }

  /* DOS commands */
  "append"     |
  "assoc"      |
  "at"         |
  "attrib"     |
  "break"      |
  "cacls"      |
  "cd"         |
  "chcp"       |
  "chdir"      |
  "chkdsk"     |
  "chkntfs"    |
  "cls"        |
  "cmd"        |
  "color"      |
  "comp"       |
  "compact"    |
  "convert"    |
  "copy"       |
  "date"       |
  "del"        |
  "dir"        |
  "diskcomp"   |
  "diskcopy"   |
  "doskey"     |
  "exist"      |
  "endlocal"   |
  "erase"      |
  "fc"         |
  "find"       |
  "findstr"    |
  "format"     |
  "ftype"      |
  "graftabl"   |
  "help"       |
  "keyb"       |
  "label"      |
  "md"         |
  "mkdir"      |
  "mode"       |
  "more"       |
  "move"       |
  "path"       |
  "pause"      |
  "popd"       |
  "print"      |
  "prompt"     |
  "pushd"      |
  "rd"         |
  "recover"    |
  "rem"        |
  "ren"        |
  "rename"     |
  "replace"    |
  "restore"    |
  "rmdir"      |
  "set"        |
  "setlocal"   |
  "shift"      |
  "sort"       |
  "start"      |
  "subst"      |
  "time"       |
  "title"      |
  "tree"       |
  "type"       |
  "ver"        |
  "verify"     |
  "vol"        |
  "xcopy"      { return token(TokenType.KEYWORD); }

  [:jletterdigit:]+ { return token(TokenType.IDENTIFIER);  }

  /* labels */
  ":" [a-zA-Z][a-zA-Z0-9_]*     { return token(TokenType.TYPE3); }

  /* comments */
  {Comment}                      { return token(TokenType.COMMENT); }
  . | {LineTerminator}           { /* skip */ }
}

<ECHO_TEXT> {
  "%" [:jletter:] [:jletterdigit:]* "%"           {  return token(TokenType.STRING2); }

  "%" [:digit:]+                {  return token(TokenType.KEYWORD2); }

  . *                    { return token(TokenType.STRING); }
  {LineTerminator}       { yybegin(YYINITIAL) ; }
}
<<EOF>>                          { return null; }