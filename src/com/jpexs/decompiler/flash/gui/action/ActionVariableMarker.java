/*
 *  Copyright (C) 2010-2025 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.gui.action;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.parser.script.variables.ActionScript2VariableParser;
import com.jpexs.decompiler.flash.action.parser.script.variables.ActionVariableParseException;
import com.jpexs.decompiler.flash.gui.editor.DebuggableEditorPane;
import com.jpexs.decompiler.flash.gui.editor.LineMarkedEditorPane;
import com.jpexs.decompiler.flash.gui.editor.LinkHandler;
import com.jpexs.decompiler.flash.gui.editor.ScrollbarOverlay;
import com.jpexs.decompiler.flash.gui.editor.TrackRectSubstanceScrollbarUI;
import com.jpexs.decompiler.flash.gui.editor.WavyUnderLinePainter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.JLayer;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.ScrollBarUI;
import javax.swing.plaf.ScrollPaneUI;
import javax.swing.plaf.basic.BasicScrollPaneUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import jsyntaxpane.SyntaxDocument;
import jsyntaxpane.Token;
import jsyntaxpane.TokenType;
import jsyntaxpane.actions.ActionUtils;
import jsyntaxpane.components.Markers;
import jsyntaxpane.components.SyntaxComponent;
import jsyntaxpane.util.Configuration;
import org.pushingpixels.substance.internal.ui.SubstanceScrollBarUI;

/**
 * This class highlights Variable tokens of ActionScript 1/2
 */
public class ActionVariableMarker implements SyntaxComponent, CaretListener, PropertyChangeListener, DocumentListener, LinkHandler {

    public static final String DEFAULT_TOKENTYPES = "IDENTIFIER, KEYWORD, REGEX";
    public static final String PROPERTY_COLOR = "ActionVariableMarker.Color";
    public static final String PROPERTY_ERRORCOLOR = "ActionVariableMarker.ErrorColor";
    public static final String PROPERTY_TOKENTYPES = "ActionVariableMarker.TokenTypes";
    private static final Color DEFAULT_COLOR = new Color(0xffeedd);
    private static final Color DEFAULT_ERRORCOLOR = new Color(0xff0000);

    private static final Color SCROLLBAR_VARIABLE_COLOR = new Color(0xb59070);
    private static final Color SCROLLBAR_ERROR_COLOR = new Color(0xff0000);
    private JEditorPane pane;
    private final Set<TokenType> tokenTypes = new HashSet<>();
    private Markers.SimpleMarker marker;
    private Markers.SimpleMarker errorMarker;
    private Status status;
    private Map<Integer, String> errors = new LinkedHashMap<>();
    private boolean errorsShown = false;

    public static final long ERROR_DELAY = 2000;
    private Timer errorsTimer;

    private Map<Integer, List<Integer>> definitionPosToReferences = new LinkedHashMap<>();
    private Map<Integer, Integer> referenceToDefinition = new LinkedHashMap<>();

    private MouseMotionAdapter mouseMotionAdapter;

    private JLayer<JScrollBar> layer;

    private ScrollbarOverlay scrollbarOverlay;

    private ScrollPaneUI originalScrollPaneUI;

    private ScrollBarUI originalScrollBarUI;

    /**
     * Constructs a new Token highlighter
     */
    public ActionVariableMarker() {
    }

    @Override
    public void caretUpdate(CaretEvent e) {
        markTokenAt(e.getDot());
    }

    public void markTokenAt(int pos) {
        SyntaxDocument doc = ActionUtils.getSyntaxDocument(pane);
        if (doc != null) {
            Token everyToken = getNearestTokenAt(doc, pos);
            if (everyToken != null) {
                if (errors.containsKey(everyToken.start)) {
                    //System.err.println(errors.get(everyToken.start));
                }
            }
            Token token = getIdentifierTokenAt(doc, pos);
            removeMarkers();
            if (token != null && tokenTypes.contains(token.type)) {
                addMarkers(token);
            }
            layer.repaint();
        }
    }

    /**
     * removes all markers from the pane.
     */
    public void removeMarkers() {
        Markers.removeMarkers(pane, marker);
        scrollbarOverlay.removeMarkers(SCROLLBAR_VARIABLE_COLOR);
    }

    public void removeErrorMarkers() {
        Markers.removeMarkers(pane, errorMarker);
        errorsShown = false;
        scrollbarOverlay.removeMarkers(SCROLLBAR_ERROR_COLOR);
    }

    private Token getIdentifierTokenAt(SyntaxDocument sDoc, int pos) {
        Token thisToken = sDoc.getTokenAt(pos);
        if (thisToken != null && (thisToken.type == TokenType.IDENTIFIER || thisToken.type == TokenType.REGEX || thisToken.type == TokenType.KEYWORD)) {
            return thisToken;
        }

        Token token = sDoc.getTokenAt(pos - 1);
        if (token != null
                && (token.type == TokenType.IDENTIFIER || token.type == TokenType.REGEX || token.type == TokenType.KEYWORD)
                && (token.start + token.length == pos)) {
            return token;
        }

        token = sDoc.getTokenAt(pos + 1);
        if (token != null
                && (token.type == TokenType.IDENTIFIER || token.type == TokenType.REGEX || token.type == TokenType.KEYWORD)
                && (token.start == pos)) {
            return token;
        }
        return null;
    }

    private Token getNearestTokenAt(SyntaxDocument sDoc, int pos) {
        Token thisToken = sDoc.getTokenAt(pos);
        if (thisToken != null) {
            return thisToken;
        }

        Token token = sDoc.getTokenAt(pos - 1);
        if (token != null
                && (token.start + token.length == pos)) {
            return token;
        }

        token = sDoc.getTokenAt(pos + 1);
        if (token != null
                && (token.start == pos)) {
            return token;
        }
        return null;
    }

    void addErrorMarkers() {
        SyntaxDocument doc = ActionUtils.getSyntaxDocument(pane);
        if (doc == null) {
            return;
        }
        doc.readLock();
        for (int position : errors.keySet()) {
            Token token = getNearestTokenAt(doc, position);
            if (token != null) {
                Markers.markToken(pane, token, errorMarker);
                markPositionOnScrollbar(position, SCROLLBAR_ERROR_COLOR);
            }
        }
        layer.repaint();
        doc.readUnlock();
        errorsShown = true;
    }

    /**
     * add highlights for the given pattern
     *
     * @param pattern
     */
    void addMarkers(Token tok) {
        SyntaxDocument sDoc = (SyntaxDocument) pane.getDocument();
        sDoc.readLock();
        int definitionPos = tok.start;
        if (referenceToDefinition.containsKey(tok.start)) {
            definitionPos = referenceToDefinition.get(tok.start);
        }
        Token definitionToken = getIdentifierTokenAt(sDoc, definitionPos < 0 ? -(definitionPos + 1) : definitionPos);
        if (definitionToken != null) {
            if (definitionPosToReferences.containsKey(definitionPos)) {
                Markers.markToken(pane, definitionToken, marker);
                markPositionOnScrollbar(definitionToken.start, SCROLLBAR_VARIABLE_COLOR);
                for (int i : definitionPosToReferences.get(definitionPos)) {
                    Token referenceToken = getIdentifierTokenAt(sDoc, i);
                    if (referenceToken != null) {
                        Markers.markToken(pane, referenceToken, marker);
                        markPositionOnScrollbar(referenceToken.start, SCROLLBAR_VARIABLE_COLOR);
                    }
                }
            }
        }
        sDoc.readUnlock();
    }

    private void markPositionOnScrollbar(int position, Color color) {
        try {
            scrollbarOverlay.addMarker(ActionUtils.getLineNumber(pane, position), color);
        } catch (BadLocationException ex) {
            //ignore
        }
    }

    @Override
    public void config(Configuration config) {
        Color markerColor = config.getColor(PROPERTY_COLOR, DEFAULT_COLOR);
        Color errorColor = config.getColor(PROPERTY_ERRORCOLOR, DEFAULT_ERRORCOLOR);
        this.marker = new Markers.SimpleMarker(markerColor);
        this.errorMarker = new WavyUnderLinePainter(errorColor); //Markers.SimpleMarker(errorColor);
        String types = config.getString(
                PROPERTY_TOKENTYPES, DEFAULT_TOKENTYPES);

        for (String type : types.split("\\s*,\\s*")) {
            try {
                TokenType tt = TokenType.valueOf(type);
                tokenTypes.add(tt);
            } catch (IllegalArgumentException e) {
                LOG.warning("Error in setting up TokenMarker "
                        + " - Invalid TokenType: " + type);
            }

        }
    }

    @Override
    public void install(JEditorPane editor) {
        this.pane = editor;
        editor.addCaretListener(this);
        editor.addPropertyChangeListener(this);
        editor.getDocument().addDocumentListener(this);
        if (editor instanceof LineMarkedEditorPane) {
            ((LineMarkedEditorPane) editor).setLinkHandler(this);
        }
        mouseMotionAdapter = new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                editorMouseMoved(e);
            }
        };
        editor.addMouseMotionListener(mouseMotionAdapter);

        JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, editor);

        originalScrollPaneUI = scrollPane.getUI();
        scrollPane.setUI(new BasicScrollPaneUI());

        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        originalScrollBarUI = verticalScrollBar.getUI();
        if (originalScrollBarUI instanceof SubstanceScrollBarUI) {
            verticalScrollBar.setUI(new TrackRectSubstanceScrollbarUI(verticalScrollBar));
        }

        scrollbarOverlay = new ScrollbarOverlay((LineMarkedEditorPane) pane);

        layer = new JLayer<>(verticalScrollBar, scrollbarOverlay);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        JPanel panel = (JPanel) SwingUtilities.getAncestorOfClass(JPanel.class, scrollPane);
        panel.add(layer, BorderLayout.EAST);

        documentUpdated();
        markTokenAt(editor.getCaretPosition());
        status = Status.INSTALLING;
    }

    private void editorMouseMoved(MouseEvent e) {
        if (pane instanceof LineMarkedEditorPane) {
            Token token = ((LineMarkedEditorPane) pane).getTokenUnderCursor();
            if (token != null) {
                String err = errors.get(token.start);
                pane.setToolTipText(err);
            } else {
                pane.setToolTipText(null);
            }
        }
    }

    @Override
    public void deinstall(JEditorPane editor) {
        status = Status.DEINSTALLING;
        removeMarkers();
        removeErrorMarkers();
        Timer tim = errorsTimer;
        if (tim != null) {
            tim.cancel();
            errorsTimer = null;
        }
        pane.removePropertyChangeListener(this);
        pane.getDocument().removeDocumentListener(this);
        pane.removeCaretListener(this);
        pane.removeMouseMotionListener(mouseMotionAdapter);
        mouseMotionAdapter = null;
        if (editor instanceof LineMarkedEditorPane) {
            ((LineMarkedEditorPane) editor).setLinkHandler(null);
        }
        JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, editor);
        JPanel panel = (JPanel) SwingUtilities.getAncestorOfClass(JPanel.class, scrollPane);
        panel.remove(layer);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        panel.revalidate();
        panel.repaint();
        scrollPane.setUI(originalScrollPaneUI);
        scrollPane.getVerticalScrollBar().setUI(originalScrollBarUI);
    }

    private static final Logger LOG = Logger.getLogger(ActionVariableMarker.class.getName());

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("document")) {
            pane.removeCaretListener(this);
            pane.getDocument().removeDocumentListener(this);
            if (status.equals(Status.INSTALLING)) {
                pane.addCaretListener(this);
                pane.getDocument().addDocumentListener(this);
                removeMarkers();
                documentUpdated();
            }
        }
    }

    private void documentUpdated() {
        errors.clear();
        removeErrorMarkers();
        layer.repaint();
        try {
            SyntaxDocument sDoc = (SyntaxDocument) pane.getDocument();

            String fullText = sDoc.getText(0, sDoc.getLength());

            SWF swf = null;
            if (pane instanceof DebuggableEditorPane) {
                DebuggableEditorPane dpane = (DebuggableEditorPane) pane;
                swf = dpane.getSwf();
            }
            if (swf == null) {
                return;
            }

            Map<Integer, List<Integer>> newDefinitionPosToReferences = new LinkedHashMap<>();
            Map<Integer, Integer> newReferenceToDefinition = new LinkedHashMap<>();
            ActionScript2VariableParser varParser = new ActionScript2VariableParser(swf);
            List<ActionVariableParseException> newErrors = new ArrayList<>();
            varParser.parse(fullText, newDefinitionPosToReferences, newReferenceToDefinition, newErrors);
            definitionPosToReferences = newDefinitionPosToReferences;
            referenceToDefinition = newReferenceToDefinition;
            for (ActionVariableParseException ex : newErrors) {
                errors.put((int) ex.position, ex.getMessage());
            }
        } catch (BadLocationException | IOException | InterruptedException ex) {
            definitionPosToReferences.clear();
            referenceToDefinition.clear();
            //ex.printStackTrace();
        } catch (ActionParseException ex) {
            definitionPosToReferences.clear();
            referenceToDefinition.clear();
            errors.put((int) ex.position, ex.getMessage());
        }
        Timer tim = errorsTimer;
        if (tim != null) {
            tim.cancel();
            errorsTimer = null;
        }
        tim = new Timer();
        tim.schedule(new TimerTask() {
            @Override
            public void run() {
                removeErrorMarkers();
                addErrorMarkers();
            }
        }, ERROR_DELAY);
        errorsTimer = tim;
        markTokenAt(pane.getCaretPosition());
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        documentUpdated();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        documentUpdated();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        documentUpdated();
    }

    @Override
    public boolean isLink(Token token) {
        return referenceToDefinition.containsKey(token.start) && referenceToDefinition.get(token.start) >= 0;
    }

    @Override
    public void handleLink(Token token) {
        Integer definition = referenceToDefinition.get(token.start);
        if (definition != null) {
            pane.setCaretPosition(definition);
        }
    }

    @Override
    public Highlighter.HighlightPainter linkPainter() {
        return ((LineMarkedEditorPane) pane).linkPainter();
    }
}
