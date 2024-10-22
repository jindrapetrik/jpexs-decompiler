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
package jsyntaxpane;

import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.event.DocumentEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;
import javax.swing.text.Segment;

/**
 * A document that supports being highlighted. The document maintains an
 * internal List of all the Tokens. The Tokens are updated using a Lexer, passed
 * to it during construction.
 *
 * @author Ayman Al-Sairafi
 */
public class SyntaxDocument extends PlainDocument {

    public static final String CAN_UNDO = "can-undo";
    public static final String CAN_REDO = "can-redo";

    Lexer lexer;
    List<Token> tokens;
    CompoundUndoMan undo;

    boolean ignoreUpdate;
    private final PropertyChangeSupport propSupport;
    private boolean canUndoState = false;
    private boolean canRedoState = false;

    public SyntaxDocument(Lexer lexer) {
        super();
        putProperty(PlainDocument.tabSizeAttribute, 4);
        this.lexer = lexer;
        // Listen for undo and redo events
        undo = new CompoundUndoMan(this);
        propSupport = new PropertyChangeSupport(this);
    }

    /**
     * Parse the entire document and return list of tokens that do not already
     * exist in the tokens list. There may be overlaps, and replacements, which
     * we will cleanup later.
     *
     * @return list of tokens that do not exist in the tokens field
     */
    private void parse() {
        // if we have no lexer, then we must have no tokens...
        if (lexer == null) {
            tokens = null;
            return;
        }
        int len = getLength();
        List<Token> toks = new ArrayList<Token>(len / 10);
        long ts = System.nanoTime();
        try {
            Segment seg = new Segment();
            getText(0, len, seg);
            lexer.parse(seg, 0, toks);
        } catch (BadLocationException ex) {
            log.log(Level.SEVERE, null, ex);
        } finally {
            if (log.isLoggable(Level.FINEST)) {
                log.finest(String.format("Parsed %d in %d ms, giving %d tokens\n",
                        len, (System.nanoTime() - ts) / 1000000, toks.size()));
            }
            tokens = toks;
        }
    }

    @Override
    protected void fireChangedUpdate(DocumentEvent e) {
        parse();
        super.fireChangedUpdate(e);
    }

    @Override
    protected void fireInsertUpdate(DocumentEvent e) {
        if (ignoreUpdate) {
            return;
        }
        parse();
        super.fireInsertUpdate(e);
    }

    @Override
    protected void fireRemoveUpdate(DocumentEvent e) {
        parse();
        super.fireRemoveUpdate(e);
    }

    public void setIgnoreUpdate(boolean value) {
        ignoreUpdate = value;
        if (!ignoreUpdate) {
            parse();
        }
    }

    /**
     * Replace the token with the replacement string
     *
     * @param token
     * @param replacement
     */
    public void replaceToken(Token token, String replacement) {
        try {
            replace(token.start, token.length, replacement, null);
        } catch (BadLocationException ex) {
            log.log(Level.WARNING, "unable to replace token: " + token, ex);
        }
    }

    /**
     * This class is used to iterate over tokens between two positions
     *
     */
    class TokenIterator implements ListIterator<Token> {

        int start;
        int end;
        int ndx = 0;

        @SuppressWarnings("unchecked")
        private TokenIterator(int start, int end) {
            this.start = start;
            this.end = end;
            if (tokens != null && !tokens.isEmpty()) {
                Token token = new Token(TokenType.COMMENT, start, end - start);
                ndx = Collections.binarySearch((List) tokens, token);
                // we will probably not find the exact token...
                if (ndx < 0) {
                    // so, start from one before the token where we should be...
                    // -1 to get the location, and another -1 to go back..
                    ndx = (-ndx - 1 - 1 < 0) ? 0 : (-ndx - 1 - 1);
                    Token t = tokens.get(ndx);
                    // if the prev token does not overlap, then advance one
                    if (t.end() <= start) {
                        ndx++;
                    }

                }
            }
        }

        @Override
        public boolean hasNext() {
            if (tokens == null) {
                return false;
            }
            if (ndx >= tokens.size()) {
                return false;
            }
            Token t = tokens.get(ndx);
            if (t.start >= end) {
                return false;
            }
            return true;
        }

        @Override
        public Token next() {
            return tokens.get(ndx++);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasPrevious() {
            if (tokens == null) {
                return false;
            }
            if (ndx <= 0) {
                return false;
            }
            Token t = tokens.get(ndx);
            if (t.end() <= start) {
                return false;
            }
            return true;
        }

        @Override
        public Token previous() {
            return tokens.get(ndx--);
        }

        @Override
        public int nextIndex() {
            return ndx + 1;
        }

        @Override
        public int previousIndex() {
            return ndx - 1;
        }

        @Override
        public void set(Token e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(Token e) {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Return an iterator of tokens between p0 and p1.
     *
     * @param start start position for getting tokens
     * @param end position for last token
     * @return Iterator for tokens that overal with range from start to end
     */
    public Iterator<Token> getTokens(int start, int end) {
        return new TokenIterator(start, end);
    }

    /**
     * Find the token at a given position. May return null if no token is found
     * (whitespace skipped) or if the position is out of range:
     *
     * @param pos
     * @return
     */
    public Token getTokenAt(int pos) {
        if (tokens == null || tokens.isEmpty() || pos > getLength()) {
            return null;
        }
        Token tok = null;
        Token tKey = new Token(TokenType.DEFAULT, pos, 1);
        @SuppressWarnings("unchecked")
        int ndx = Collections.binarySearch((List) tokens, tKey);
        if (ndx < 0) {
            // so, start from one before the token where we should be...
            // -1 to get the location, and another -1 to go back..
            ndx = (-ndx - 1 - 1 < 0) ? 0 : (-ndx - 1 - 1);
            Token t = tokens.get(ndx);
            if ((t.start <= pos) && (pos <= t.end())) {
                tok = t;
            }
        } else {
            tok = tokens.get(ndx);
        }
        return tok;
    }

    public Token getWordAt(int offs, Pattern p) {
        Token word = null;
        try {
            Element line = getParagraphElement(offs);
            if (line == null) {
                return word;
            }
            int lineStart = line.getStartOffset();
            int lineEnd = Math.min(line.getEndOffset(), getLength());
            Segment seg = new Segment();
            getText(lineStart, lineEnd - lineStart, seg);
            if (seg.count > 0) {
                // we need to get the word using the words pattern p
                Matcher m = p.matcher(seg);
                int o = offs - lineStart;
                while (m.find()) {
                    if (m.start() <= o && o <= m.end()) {
                        word = new Token(TokenType.DEFAULT, m.start() + lineStart, m.end() - m.start());
                        break;
                    }
                }
            }
        } catch (BadLocationException ex) {
            Logger.getLogger(SyntaxDocument.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return word;
        }
    }

    /**
     * Return the token following the current token, or null
     * <b>This is an expensive operation, so do not use it to update the gui</b>
     *
     * @param tok
     * @return
     */
    public Token getNextToken(Token tok) {
        int n = tokens.indexOf(tok);
        if ((n >= 0) && (n < (tokens.size() - 1))) {
            return tokens.get(n + 1);
        } else {
            return null;
        }
    }

    /**
     * Return the token prior to the given token, or null
     * <b>This is an expensive operation, so do not use it to update the gui</b>
     *
     * @param tok
     * @return
     */
    public Token getPrevToken(Token tok) {
        int n = tokens.indexOf(tok);
        if ((n > 0) && (!tokens.isEmpty())) {
            return tokens.get(n - 1);
        } else {
            return null;
        }
    }

    /**
     * This is used to return the other part of a paired token in the document.
     * A paired part has token.pairValue <> 0, and the paired token will have
     * the negative of t.pairValue. This method properly handles nestings of
     * same pairValues, but overlaps are not checked. if The document does not
     * contain a paired token, then null is returned.
     *
     * @param t
     * @return the other pair's token, or null if nothing is found.
     */
    public Token getPairFor(Token t) {
        if (t == null || t.pairValue == 0) {
            return null;
        }
        Token p = null;
        int ndx = tokens.indexOf(t);
        // w will be similar to a stack. The openers weght is added to it
        // and the closers are subtracted from it (closers are already negative)
        int w = t.pairValue;
        int direction = (t.pairValue > 0) ? 1 : -1;
        boolean done = false;
        int v = Math.abs(t.pairValue);
        while (!done) {
            ndx += direction;
            if (ndx < 0 || ndx >= tokens.size()) {
                break;
            }
            Token current = tokens.get(ndx);
            if (Math.abs(current.pairValue) == v) {
                w += current.pairValue;
                if (w == 0) {
                    p = current;
                    done = true;
                }
            }
        }

        return p;
    }

    /**
     * Perform an undo action, if possible
     */
    public void doUndo() {
        if (undo.canUndo()) {
            undo.undo();
            parse();
        }
    }

    /**
     * Perform a redo action, if possible.
     */
    public void doRedo() {
        if (undo.canRedo()) {
            undo.redo();
            parse();
        }
    }

    /**
     * Return a matcher that matches the given pattern on the entire document
     *
     * @param pattern
     * @return matcher object
     */
    public Matcher getMatcher(Pattern pattern) {
        return getMatcher(pattern, 0, getLength());
    }

    /**
     * Return a matcher that matches the given pattern in the part of the
     * document starting at offset start. Note that the matcher will have offset
     * starting from <code>start</code>
     *
     * @param pattern
     * @param start
     * @return matcher that <b>MUST</b> be offset by start to get the proper
     * location within the document
     */
    public Matcher getMatcher(Pattern pattern, int start) {
        return getMatcher(pattern, start, getLength() - start);
    }

    /**
     * Return a matcher that matches the given pattern in the part of the
     * document starting at offset start and ending at start + length. Note that
     * the matcher will have offset starting from <code>start</code>
     *
     * @param pattern
     * @param start
     * @param length
     * @return matcher that <b>MUST</b> be offset by start to get the proper
     * location within the document
     */
    public Matcher getMatcher(Pattern pattern, int start, int length) {
        Matcher matcher = null;
        if (getLength() == 0) {
            return null;
        }
        if (start >= getLength()) {
            return null;
        }
        try {
            if (start < 0) {
                start = 0;
            }
            if (start + length > getLength()) {
                length = getLength() - start;
            }
            Segment seg = new Segment();
            getText(start, length, seg);
            matcher = pattern.matcher(seg);
        } catch (BadLocationException ex) {
            log.log(Level.SEVERE, "Requested offset: " + ex.offsetRequested(), ex);
        }
        return matcher;
    }

    /**
     * This will discard all undoable edits
     */
    public void clearUndos() {
        undo.discardAllEdits();
    }

    /**
     * Gets the line at given position. The line returned will NOT include the
     * line terminator '\n'
     *
     * @param pos Position (usually from text.getCaretPosition()
     * @return the STring of text at given position
     * @throws BadLocationException
     */
    public String getLineAt(int pos) throws BadLocationException {
        Element e = getParagraphElement(pos);
        Segment seg = new Segment();
        getText(e.getStartOffset(), e.getEndOffset() - e.getStartOffset(), seg);
        char last = seg.last();
        if (last == '\n' || last == '\r') {
            seg.count--;
        }
        return seg.toString();
    }

    /**
     * Deletes the line at given position
     *
     * @param pos
     * @throws javax.swing.text.BadLocationException
     */
    public void removeLineAt(int pos)
            throws BadLocationException {
        Element e = getParagraphElement(pos);
        remove(e.getStartOffset(), getElementLength(e));
    }

    /**
     * Replace the line at given position with the given string, which can span
     * multiple lines
     *
     * @param pos
     * @param newLines
     * @throws javax.swing.text.BadLocationException
     */
    public void replaceLineAt(int pos, String newLines)
            throws BadLocationException {
        Element e = getParagraphElement(pos);
        replace(e.getStartOffset(), getElementLength(e), newLines, null);
    }

    /**
     * Helper method to get the length of an element and avoid getting a too
     * long element at the end of the document
     *
     * @param e
     * @return
     */
    private int getElementLength(Element e) {
        int end = e.getEndOffset();
        if (end >= (getLength() - 1)) {
            end--;
        }
        return end - e.getStartOffset();
    }

    /**
     * Gets the text without the comments. For example for the string
     * <code>{ // it's a comment</code> this method will return "{ ".
     *
     * @param aStart start of the text.
     * @param anEnd end of the text.
     * @return String for the line without comments (if exists).
     */
    public synchronized String getUncommentedText(int aStart, int anEnd) {
        readLock();
        StringBuilder result = new StringBuilder();
        Iterator<Token> iter = getTokens(aStart, anEnd);
        while (iter.hasNext()) {
            Token t = iter.next();
            if (!TokenType.isComment(t)) {
                result.append(t.getText(this));
            }
        }
        readUnlock();
        return result.toString();
    }

    /**
     * Returns the starting position of the line at pos
     *
     * @param pos
     * @return starting position of the line
     */
    public int getLineStartOffset(int pos) {
        return getParagraphElement(pos).getStartOffset();
    }

    /**
     * Returns the end position of the line at pos. Does a bounds check to
     * ensure the returned value does not exceed document length
     *
     * @param pos
     * @return
     */
    public int getLineEndOffset(int pos) {
        int end = getParagraphElement(pos).getEndOffset();
        if (end >= getLength()) {
            end = getLength();
        }
        return end;
    }

    /**
     * Return the number of lines in this document
     *
     * @return
     */
    public int getLineCount() {
        Element e = getDefaultRootElement();
        int cnt = e.getElementCount();
        return cnt;
    }

    /**
     * Return the line number at given position. The line numbers are zero based
     *
     * @param pos
     * @return
     */
    public int getLineNumberAt(int pos) {
        int lineNr = getDefaultRootElement().getElementIndex(pos);
        return lineNr;
    }

    @Override
    public String toString() {
        return "SyntaxDocument(" + lexer + ", " + ((tokens == null) ? 0 : tokens.size()) + " tokens)@"
                + hashCode();
    }

    /**
     * We override this here so that the replace is treated as one operation by
     * the undomanager
     *
     * @param offset
     * @param length
     * @param text
     * @param attrs
     * @throws BadLocationException
     */
    @Override
    public void replace(int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        remove(offset, length);
        undo.startCombine();
        insertString(offset, text, attrs);
    }

    /**
     * Append the given string to the text of this document.
     *
     * @param str
     * @return this document
     */
    public SyntaxDocument append(String str) {
        try {
            insertString(getLength(), str, null);
        } catch (BadLocationException ex) {
            log.log(Level.WARNING, "Error appending str", ex);
        }
        return this;
    }

    public void setCanUndo(boolean value) {
        if (canUndoState != value) {
            // System.out.println("canUndo = " + value);
            canUndoState = value;
            propSupport.firePropertyChange(CAN_UNDO, !value, value);
        }
    }

    public void setCanRedo(boolean value) {
        if (canRedoState != value) {
            // System.out.println("canRedo = " + value);
            canRedoState = value;
            propSupport.firePropertyChange(CAN_REDO, !value, value);
        }
    }

// our logger instance...
    private static final Logger log = Logger.getLogger(SyntaxDocument.class.getName());
}
