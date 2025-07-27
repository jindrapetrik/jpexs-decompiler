/*
 *  Copyright (C) 2010-2021 JPEXS, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
/* Flash assembler language lexer specification */
package com.jpexs.csv;

import java.util.List;
import java.util.ArrayList;
import java.io.StringReader;
%%

%public
%class CsvLexer
%final
%unicode
%ignorecase
%char
%line
%column
%type CsvRow

%{

    StringBuilder string = new StringBuilder();
        
    List<String> currentRow = new ArrayList<>();

    public CsvLexer(String source){
        this(new StringReader(source));
    }     

%}

LineTerminator = \r|\n|\r\n

InputCharacter = [^\r\n]
Separator = ";"

%state STRING

%%

<YYINITIAL> {  
  {Separator} "\""      {
                            currentRow.add(string.toString());
                            string.setLength(0);
                            yybegin(STRING);
                        }
  {Separator}           {
                            currentRow.add(string.toString());
                            string.setLength(0);
                        }
  {LineTerminator}      {
                            currentRow.add(string.toString());
                            string.setLength(0);
                            CsvRow ret = new CsvRow(currentRow);
                            currentRow = new ArrayList<>();
                            return ret;
                        }
  .                     {string.append(yytext());}
}

<STRING> {
  \"\"                           {
                                     string.append("\"");
                                 }
  \"                             {
                                    yybegin(YYINITIAL);
                                 }
  .                     {string.append(yytext());}  
}

/* error fallback */
[^]                              { }
<<EOF>>                          { 
                            if (string == null) {
                                return null;
                            }
                            if (currentRow.isEmpty() && string.length() == 0) {
                                string = null;
                                return null;
                            }
                            currentRow.add(string.toString());
                            string.setLength(0);
                            CsvRow ret = new CsvRow(currentRow);
                            currentRow = new ArrayList<>();
                            string = null;
                            return ret;
}
