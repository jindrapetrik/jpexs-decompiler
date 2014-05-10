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
package jsyntaxpane.components;

import java.beans.PropertyChangeEvent;
import jsyntaxpane.actions.*;
import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import jsyntaxpane.SyntaxDocument;
import jsyntaxpane.Token;
import jsyntaxpane.TokenType;
import jsyntaxpane.util.Configuration;

/**
 * This class highlights Tokens within a document whenever the caret is moved
 * to a TokenType provided in the config file.
 * 
 * @author Ayman Al-Sairafi
 */
public class TokenMarker implements SyntaxComponent, CaretListener, PropertyChangeListener {

    public static final String DEFAULT_TOKENTYPES = "IDENTIFIER, TYPE, TYPE2, TYPE3";
    public static final String PROPERTY_COLOR = "TokenMarker.Color";
    public static final String PROPERTY_TOKENTYPES = "TokenMarker.TokenTypes";
    private static final Color DEFAULT_COLOR = new Color(0xFFEE66);
    private JEditorPane pane;
    private Set<TokenType> tokenTypes = new HashSet<TokenType>();
    private Markers.SimpleMarker marker;
    private Status status;

    /**
     * Constructs a new Token highlighter
     */
    public TokenMarker() {
    }

    @Override
    public void caretUpdate(CaretEvent e) {
        markTokenAt(e.getDot());
    }

    public void markTokenAt(int pos) {
        SyntaxDocument doc = ActionUtils.getSyntaxDocument(pane);
        if (doc != null) {
            Token token = doc.getTokenAt(pos);
            removeMarkers();
            if (token != null && tokenTypes.contains(token.type)) {
                addMarkers(token);
            }
        }
    }

    /**
     * removes all markers from the pane.
     */
    public void removeMarkers() {
        Markers.removeMarkers(pane, marker);
    }

    /**
     * add highlights for the given pattern
     * @param pattern
     */
    void addMarkers(Token tok) {
        SyntaxDocument sDoc = (SyntaxDocument) pane.getDocument();
        sDoc.readLock();
        // we need to create a STring, because the CharSequence does not have an
        // equals method and Object.equals is called.  It will not match
        String text = tok.getText(sDoc).toString();
        Iterator<Token> it = sDoc.getTokens(0, sDoc.getLength());
        while (it.hasNext()) {
            Token nextToken = it.next();
            String nextText = nextToken.getText(sDoc).toString();
            if (text.equals(nextText)) {
                Markers.markToken(pane, nextToken, marker);
            }

        }
        sDoc.readUnlock();
    }

    @Override
    public void config(Configuration config) {
        Color markerColor = config.getColor(
                PROPERTY_COLOR, DEFAULT_COLOR);
        this.marker = new Markers.SimpleMarker(markerColor);
        String types = config.getString(
                PROPERTY_TOKENTYPES, DEFAULT_TOKENTYPES);

        for (String type : types.split("\\s*,\\s*")) {
            try {
                TokenType tt = TokenType.valueOf(type);
                tokenTypes.add(tt);
            } catch (IllegalArgumentException e) {
                LOG.warning("Error in setting up TokenMarker " +
                        " - Invalid TokenType: " + type);
            }

        }
    }

    @Override
    public void install(JEditorPane editor) {
        this.pane = editor;
        pane.addCaretListener(this);
        markTokenAt(editor.getCaretPosition());
        status = Status.INSTALLING;
    }

    @Override
    public void deinstall(JEditorPane editor) {
        status = Status.DEINSTALLING;
        removeMarkers();
        pane.removeCaretListener(this);
    }
    private static final Logger LOG = Logger.getLogger(TokenMarker.class.getName());

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("document")) {
                pane.removeCaretListener(this);
            if (status.equals(Status.INSTALLING)) {
                pane.addCaretListener(this);
                removeMarkers();
            }
        }
    }
}
