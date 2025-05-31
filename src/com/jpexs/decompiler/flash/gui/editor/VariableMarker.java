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
package com.jpexs.decompiler.flash.gui.editor;

import com.jpexs.decompiler.flash.gui.AppStrings;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.simpleparser.SimpleParseException;
import com.jpexs.decompiler.flash.simpleparser.SimpleParser;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.KeyEventPostProcessor;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
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
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
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
 * This class highlights Variable and error tokens.
 */
public class VariableMarker implements SyntaxComponent, CaretListener, PropertyChangeListener, DocumentListener {

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
    private OccurencesMarker marker;
    private Markers.SimpleMarker errorMarker;
    private Status status;
    private Map<Integer, String> errors = new LinkedHashMap<>();
    public static final long ERROR_DELAY = 2000;
    private Timer errorsTimer;

    private Map<Integer, List<Integer>> definitionPosToReferences = new LinkedHashMap<>();
    private Map<Integer, Integer> referenceToDefinition = new LinkedHashMap<>();

    private MouseMotionAdapter mouseMotionAdapter;

    private HighlightsPanel highlightsPanel;

    private ScrollPaneUI originalScrollPaneUI;

    private ScrollBarUI originalScrollBarUI;

    private LinkAdapter linkAdapter;

    private KeyEventPostProcessor kevEventPostProcessor;

    private Set<Integer> occurencesPositions = new HashSet<>();

    private UnderlinePainter underLinePainter = new UnderlinePainter(new Color(0, 0, 255), null);
    private UnderlinePainter underLineMarkOccurencesPainter = new UnderlinePainter(new Color(0, 0, 255), DEFAULT_COLOR);
    private UnderlinePainter underLineExternalPainter = new UnderlinePainter(new Color(0, 255, 0), null);
    private UnderlinePainter underLineExternalMarkOccurencesPainter = new UnderlinePainter(new Color(0, 255, 0), DEFAULT_COLOR);

    private Token lastUnderlined;
    private LinkType lastUnderlinedLinkType = LinkType.NO_LINK;
    
    
    public static enum LinkType {
        NO_LINK,
        LINK_THIS_SCRIPT,
        LINK_OTHER_SCRIPT;
    }

    /**
     * Constructs a new Token highlighter
     */
    public VariableMarker() {
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
            highlightsPanel.repaint();
        }
    }

    /**
     * removes all markers from the pane.
     */
    public void removeMarkers() {
        Markers.removeMarkers(pane, marker);
        Markers.removeMarkers(pane, underLinePainter);
        Markers.removeMarkers(pane, underLineMarkOccurencesPainter);
        Markers.removeMarkers(pane, underLineExternalPainter);
        Markers.removeMarkers(pane, underLineExternalMarkOccurencesPainter);
        occurencesPositions.clear();
    }

    public void removeErrorMarkers() {
        Markers.removeMarkers(pane, errorMarker);
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
        return getNearestTokenAt(sDoc, pos);
    }

    private Token getNearestTokenAt(SyntaxDocument sDoc, int pos) {
        Token thisToken = sDoc.getTokenAt(pos);
        if (thisToken != null) {

            if (thisToken.length == 1) {
                Token nextToken = sDoc.getTokenAt(pos + 1);
                if (nextToken != null && nextToken.start == pos && nextToken.length > 1) {
                    return nextToken;
                }
            }

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
        highlightsPanel.setErrors(errors);

        SyntaxDocument doc = ActionUtils.getSyntaxDocument(pane);
        if (doc == null) {
            return;
        }
        doc.readLock();
        for (int position : errors.keySet()) {
            Token token = getNearestTokenAt(doc, position);
            if (token != null) {
                Markers.markToken(pane, token, errorMarker);
            }
        }
        highlightsPanel.repaint();
        doc.readUnlock();
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
                
                Markers.SimpleMarker markerKind = marker;
                if (lastUnderlined == definitionToken) {
                    if (lastUnderlinedLinkType == LinkType.LINK_OTHER_SCRIPT) {
                        markerKind = underLineExternalMarkOccurencesPainter;
                    } else {
                        markerKind = underLineMarkOccurencesPainter;
                    }
                }
                if (tokenTypes.contains(definitionToken.type)) {
                    Markers.markToken(pane, definitionToken, markerKind);
                }
                occurencesPositions.add(definitionToken.start);
                for (int i : definitionPosToReferences.get(definitionPos)) {
                    Token referenceToken = getIdentifierTokenAt(sDoc, i);
                    if (referenceToken != null) {
                        markerKind = marker;
                        if (lastUnderlined == referenceToken) {
                            if (lastUnderlinedLinkType == LinkType.LINK_OTHER_SCRIPT) {
                                markerKind = underLineExternalMarkOccurencesPainter;
                            } else {
                                markerKind = underLineMarkOccurencesPainter;
                            }
                        }
                        Markers.markToken(pane, referenceToken, markerKind);
                        occurencesPositions.add(referenceToken.start);
                    }
                }
            }
        }
        sDoc.readUnlock();
    }

    @Override
    public void config(Configuration config) {
        Color markerColor = config.getColor(PROPERTY_COLOR, DEFAULT_COLOR);
        Color errorColor = config.getColor(PROPERTY_ERRORCOLOR, DEFAULT_ERRORCOLOR);
        this.marker = new OccurencesMarker(markerColor);
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
        /*if (editor instanceof LineMarkedEditorPane) {
            ((LineMarkedEditorPane) editor).setLinkHandler(this);
        }*/
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

        highlightsPanel = new HighlightsPanel((LineMarkedEditorPane) pane);
        //scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        JPanel panel = (JPanel) SwingUtilities.getAncestorOfClass(JPanel.class, scrollPane);
        panel.add(highlightsPanel, BorderLayout.EAST);

        linkAdapter = new LinkAdapter();

        pane.addMouseListener(linkAdapter);
        pane.addMouseMotionListener(linkAdapter);

        //No standard AddKeyListener as we want to catch Ctrl globally no matter of focus                
        kevEventPostProcessor = new KeyEventPostProcessor() {
            @Override
            public boolean postProcessKeyEvent(KeyEvent e) {
                if (e.getID() == KeyEvent.KEY_PRESSED) {
                    linkAdapter.keyPressed(e);
                }
                if (e.getID() == KeyEvent.KEY_RELEASED) {
                    linkAdapter.keyReleased(e);
                }
                if (e.getID() == KeyEvent.KEY_TYPED) {
                    linkAdapter.keyTyped(e);
                }
                return false;
            }
        };
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventPostProcessor(kevEventPostProcessor);

        View.addEditorAction(pane, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SyntaxDocument doc = ActionUtils.getSyntaxDocument(pane);
                int pos = pane.getCaretPosition();
                Token token = getIdentifierTokenAt(doc, pos);
                handleLink(token);
            }
        }, "find-declaration", AppStrings.translate("abc.action.find-declaration"), "control B");
        
        
        documentUpdated();
        markTokenAt(editor.getCaretPosition());
        status = Status.INSTALLING;
    }

    private class LinkAdapter extends MouseAdapter implements KeyListener {

        private boolean ctrlDown = false;

        private Point lastCursorPos;

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
                ctrlDown = true;
                update();
            }
        }

        @Override
        public void keyTyped(KeyEvent e) {

        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
                ctrlDown = false;
                update();
            }
        }

        private void update() {
            if (lastCursorPos == null) {
                return;
            }
            if (ctrlDown) {
                Token t = ((LineMarkedEditorPane) pane).tokenAtPos(lastCursorPos);

                if (t != lastUnderlined) {
                    if (t == null || lastUnderlined == null || !t.equals(lastUnderlined)) {
                        MyMarkers.removeMarkers(pane, underLinePainter);
                        MyMarkers.removeMarkers(pane, underLineMarkOccurencesPainter);
                        MyMarkers.removeMarkers(pane, underLineExternalPainter);
                        MyMarkers.removeMarkers(pane, underLineExternalMarkOccurencesPainter);
                        
                        lastUnderlinedLinkType = t == null ? LinkType.NO_LINK : getLinkType(t);
                        if (t != null && lastUnderlinedLinkType != LinkType.NO_LINK) {
                            lastUnderlined = t;
                            pane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                        } else {
                            lastUnderlined = null;
                            removeMarkers();
                            markTokenAt(pane.getCaretPosition());
                        }
                    } else {
                        lastUnderlined = null;
                        lastUnderlinedLinkType = LinkType.NO_LINK;
                    }
                }

                if (lastUnderlined != null) {
                    Highlighter.HighlightPainter painter = underLinePainter;
                    if (occurencesPositions.contains(lastUnderlined.start)) {
                        if (lastUnderlinedLinkType == LinkType.LINK_OTHER_SCRIPT) {
                            painter = underLineExternalMarkOccurencesPainter;
                        } else {
                            painter = underLineMarkOccurencesPainter;
                        }
                        removeMarkers();
                        markTokenAt(pane.getCaretPosition());
                    } else {
                        if (lastUnderlinedLinkType == LinkType.LINK_OTHER_SCRIPT) {
                            painter = underLineExternalPainter;
                        } 
                        MyMarkers.markToken(pane, lastUnderlined, painter);                        
                    }
                } else {
                    pane.setCursor(Cursor.getDefaultCursor());
                }
            } else {
                if (lastUnderlined != null) {
                    lastUnderlined = null;
                    MyMarkers.removeMarkers(pane, underLinePainter);
                    MyMarkers.removeMarkers(pane, underLineMarkOccurencesPainter);
                    MyMarkers.removeMarkers(pane, underLineExternalPainter);
                    MyMarkers.removeMarkers(pane, underLineExternalMarkOccurencesPainter);
                    
                    
                    removeMarkers();
                    markTokenAt(pane.getCaretPosition());
                }
                pane.setCursor(Cursor.getDefaultCursor());
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (ctrlDown) {
                Token t = ((LineMarkedEditorPane) pane).tokenAtPos(lastCursorPos);
                if (t != null && getLinkType(t) != LinkType.NO_LINK) {
                    e.consume();
                    handleLink(t);
                }
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {

            ctrlDown = (e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0;
            lastCursorPos = e.getPoint();
            update();

        }
    }

    private void editorMouseMoved(MouseEvent e) {
        if (pane instanceof LineMarkedEditorPane) {
            Token token = ((LineMarkedEditorPane) pane).tokenAtPos(e.getPoint());
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
        View.removeEditorAction(pane, "find-declaration");
        pane.removePropertyChangeListener(this);
        pane.getDocument().removeDocumentListener(this);
        pane.removeCaretListener(this);
        pane.removeMouseMotionListener(mouseMotionAdapter);
        pane.removeMouseMotionListener(linkAdapter);
        pane.removeMouseListener(linkAdapter);
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventPostProcessor(kevEventPostProcessor);
        mouseMotionAdapter = null;
        /*if (editor instanceof LineMarkedEditorPane) {
            ((LineMarkedEditorPane) editor).setLinkHandler(null);
        }*/
        JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, editor);
        JPanel panel = (JPanel) SwingUtilities.getAncestorOfClass(JPanel.class, scrollPane);
        panel.remove(highlightsPanel);
        //scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        panel.revalidate();
        panel.repaint();
        scrollPane.setUI(originalScrollPaneUI);
        scrollPane.getVerticalScrollBar().setUI(originalScrollBarUI);
    }

    private static final Logger LOG = Logger.getLogger(VariableMarker.class.getName());

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
        highlightsPanel.repaint();
        try {
            SyntaxDocument sDoc = (SyntaxDocument) pane.getDocument();

            String fullText = sDoc.getText(0, sDoc.getLength());

            if (!(pane instanceof LineMarkedEditorPane)) {
                return;
            }
            SimpleParser parser = ((LineMarkedEditorPane) pane).getParser();
            if (parser == null) {
                return;
            }

            Map<Integer, List<Integer>> newDefinitionPosToReferences = new LinkedHashMap<>();
            Map<Integer, Integer> newReferenceToDefinition = new LinkedHashMap<>();
            List<SimpleParseException> newErrors = new ArrayList<>();
            parser.parse(fullText, newDefinitionPosToReferences, newReferenceToDefinition, newErrors);
            definitionPosToReferences = newDefinitionPosToReferences;
            referenceToDefinition = newReferenceToDefinition;
            for (SimpleParseException ex : newErrors) {
                errors.put((int) ex.position, ex.getMessage());
            }
        } catch (BadLocationException | IOException | InterruptedException ex) {
            definitionPosToReferences.clear();
            referenceToDefinition.clear();
            //ex.printStackTrace();
        } catch (SimpleParseException ex) {
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

    private LinkType getLinkType(Token token) {
        if (definitionPosToReferences.containsKey(token.start)) {
            return LinkType.NO_LINK;
        }
        boolean linkThisScript = referenceToDefinition.containsKey(token.start) && referenceToDefinition.get(token.start) >= 0;
        if (linkThisScript) {
            return LinkType.LINK_THIS_SCRIPT;
        }
        if (pane.isEditable()) {
            return LinkType.NO_LINK;
        }
        if (((LineMarkedEditorPane) pane).getLinkHandler().isLink(token)) {
            return LinkType.LINK_OTHER_SCRIPT;
        }
        return LinkType.NO_LINK;
    }

    public void handleLink(Token token) {
        Integer definition = referenceToDefinition.get(token.start);
        if (definition != null && definition >= 0) {
            pane.setCaretPosition(definition);
        } else if (!pane.isEditable()) {
            lastUnderlined = null;
            removeErrorMarkers();
            removeMarkers();
            pane.repaint();
            ((LineMarkedEditorPane) pane).getLinkHandler().handleLink(token);
        }
    }

}
