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

import java.io.CharArrayReader;
import jsyntaxpane.*;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Segment;

/**
 * This is a default, and abstract implemenatation of a Lexer using JFLex 
 * with some utility methods that Lexers can implement.
 *
 * @author Ayman Al-Sairafi
 */
public abstract class DefaultJFlexLexer implements Lexer {

    protected int tokenStart;
    protected int tokenLength;
    protected int offset;

    /**
     * Helper method to create and return a new Token from of TokenType
     * tokenStart and tokenLength will be modified to the newStart and
     * newLength params
     * @param type
     * @param tStart
     * @param tLength
     * @param newStart
     * @param newLength
     * @return
     */
    protected Token token(TokenType type, int tStart, int tLength,
            int newStart, int newLength) {
        tokenStart = newStart;
        tokenLength = newLength;
        return new Token(type, tStart + offset, tLength);
    }

    /**
     * Create and return a Token of given type from start with length
     * offset is added to start
     * @param type
     * @param start
     * @param length
     * @return
     */
    protected Token token(TokenType type, int start, int length) {
        return new Token(type, start + offset, length);
    }

    /**
     * Create and return a Token of given type.  start is obtained from {@link yychar()}
     * and length from {@link yylength()}
     * offset is added to start

     * @param type
     * @return
     */
    protected Token token(TokenType type) {
        return new Token(type, yychar() + offset, yylength());
    }

    /**
     * Create and return a Token of given type and pairValue.
     * start is obtained from {@link yychar()}
     * and length from {@link yylength()}
     * offset is added to start
     *
     * @param type
     * @param pairValue
     * @return
     */
    protected Token token(TokenType type, int pairValue) {
        return new Token(type, yychar() + offset, yylength(), (byte) pairValue);
    }

    /**
     * The DefaultJFlexLexer simply calls the yylex method of a JFLex compatible
     * Lexer and adds the tokens obtained to an ArrayList.
     */
    @Override
    public void parse(Segment segment, int ofst, List<Token> tokens) {
        try {
            CharArrayReader reader = new CharArrayReader(segment.array, segment.offset, segment.count);
            yyreset(reader);
            this.offset = ofst;
            for (Token t = yylex(); t != null; t = yylex()) {
                tokens.add(t);
            }
        } catch (IOException ex) {
            Logger.getLogger(DefaultJFlexLexer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This will be called to reset the the lexer.
     * This is created automatically by JFlex.
     * @param reader
     */
    public abstract void yyreset(Reader reader);

    /**
     * This is called to return the next Token from the Input Reader
     * @return next token, or null if no more tokens.
     * @throws java.io.IOException
     */
    public abstract Token yylex() throws java.io.IOException;

    /**
     * Returns the character at position <tt>pos</tt> from the
     * matched text.
     *
     * It is equivalent to yytext().charAt(pos), but faster
     *
     * @param pos the position of the character to fetch.
     *            A value from 0 to yylength()-1.
     *
     * @return the character at position pos
     */
    public abstract char yycharat(int pos);

    /**
     * Returns the length of the matched text region.
     * This method is automatically implemented by JFlex lexers
     * @return
     */
    public abstract int yylength();

    /**
     * Returns the text matched by the current regular expression.
     * This method is automatically implemented by JFlex lexers
     * @return
     */
    public abstract String yytext();

    /**
     * Return the char number from beginning of input stream.
     * This is NOT implemented by JFLex, so the code must be
     * added to create this and return the private yychar field
     * @return 
     */
    public abstract int yychar();
}
