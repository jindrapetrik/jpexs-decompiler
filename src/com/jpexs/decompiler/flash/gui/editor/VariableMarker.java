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
import com.jpexs.decompiler.flash.gui.Main;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.gui.ViewMessages;
import com.jpexs.decompiler.flash.simpleparser.LinkType;
import com.jpexs.decompiler.flash.simpleparser.Path;
import com.jpexs.decompiler.flash.simpleparser.SimpleParseException;
import com.jpexs.decompiler.flash.simpleparser.SimpleParser;
import com.jpexs.decompiler.flash.simpleparser.Variable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.KeyEventPostProcessor;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
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
import natorder.NaturalOrderComparator;
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

    private JEditorPane pane;
    private final Set<TokenType> tokenTypes = new HashSet<>();
    private OccurencesMarker marker;
    private Markers.SimpleMarker errorMarker;
    private Status status;
    private Map<Integer, String> errors = new LinkedHashMap<>();
    public static final long ERROR_DELAY = 2000;
    private Timer errorsTimer;
    private Timer parseTimer;

    private Map<Integer, List<Integer>> definitionPosToReferences = new LinkedHashMap<>();
    private Map<Integer, Integer> referenceToDefinition = new LinkedHashMap<>();
    private List<Path> externalTypes = new ArrayList<>();
    private Map<Integer, Integer> referenceToExternalTypeIndex = new LinkedHashMap<>();
    private Map<Integer, List<Integer>> externalTypeIndexToReference = new LinkedHashMap<>();
    private Map<Path, Path> simpleExternalClassNameToFullClassName = new LinkedHashMap<>();
    private Map<Integer, Path> referenceToExternalTraitKey = new LinkedHashMap<>();
    private Map<Path, List<Integer>> externalTraitKeyToReference = new LinkedHashMap<>();
    private Map<Integer, Path> separatorPosToType = new LinkedHashMap<>();
    private Map<Path, List<Variable>> localTypeTraits = new LinkedHashMap<>();
    private Map<Integer, Path> definitionToType = new LinkedHashMap<>();
    private Map<Integer, Path> definitionToCallType = new LinkedHashMap<>();
    private Map<Integer, Boolean> separatorIsStatic = new LinkedHashMap<>();
    private List<Variable> variableSuggestions = new ArrayList<>();

    private MouseMotionAdapter mouseMotionAdapter;

    private KeyAdapter keyAdapter;

    private HighlightsPanel highlightsPanel;

    private ScrollPaneUI originalScrollPaneUI;

    private ScrollBarUI originalScrollBarUI;

    private LinkAdapter linkAdapter;

    private KeyEventPostProcessor kevEventPostProcessor;

    private Set<Integer> occurencesPositions = new HashSet<>();

    private Color basicUnderlineColor = new Color(0, 0, 255);
    private Color otherScriptUnderlineColor = new Color(0, 255, 0);
    private Color otherFileUnderlineColor = new Color(255, 0, 255);

    private UnderlinePainter underLinePainter = new UnderlinePainter(basicUnderlineColor, null);
    private UnderlinePainter underLineMarkOccurencesPainter = new UnderlinePainter(basicUnderlineColor, DEFAULT_COLOR);

    private UnderlinePainter underLineOtherScriptPainter = new UnderlinePainter(otherScriptUnderlineColor, null);
    private UnderlinePainter underLineOtherScriptMarkOccurencesPainter = new UnderlinePainter(otherScriptUnderlineColor, DEFAULT_COLOR);

    private UnderlinePainter underLineOtherFilePainter = new UnderlinePainter(otherFileUnderlineColor, null);
    private UnderlinePainter underLineOtherFileMarkOccurencesPainter = new UnderlinePainter(otherFileUnderlineColor, DEFAULT_COLOR);

    private Token lastUnderlined;
    private LinkType lastUnderlinedLinkType = LinkType.NO_LINK;

    private boolean goingOut = false;

    private JList<VariableListItem> codeCompletionList;
    private DefaultListModel<VariableListItem> codeCompletionListModel;
    private JPopupMenu codeCompletionPopup;

    /**
     * Constructs a new Token highlighter
     */
    public VariableMarker() {
        codeCompletionListModel = new DefaultListModel<>();

        codeCompletionList = new JList<>();
        codeCompletionList.setModel(codeCompletionListModel);

        codeCompletionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        codeCompletionList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    VariableListItem selected = codeCompletionList.getSelectedValue();
                    if (selected != null) {
                        completeCode(selected.variable.name.getLast().toString());
                    }
                    codeCompletionPopup.setVisible(false);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(codeCompletionList);
        codeCompletionPopup = new JPopupMenu();
        codeCompletionPopup.setLayout(new BorderLayout());
        codeCompletionPopup.add(scroll, BorderLayout.CENTER);
        codeCompletionPopup.setPopupSize(200, 100);

        codeCompletionList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {

                    VariableListItem selected = codeCompletionList.getSelectedValue();
                    if (selected != null) {
                        completeCode(selected.variable.name.getLast().toString());
                    }
                    codeCompletionPopup.setVisible(false);
                    if (selected == null) {
                        pane.dispatchEvent(e);
                    }
                    return;
                }
                if (e.getKeyCode() == KeyEvent.VK_DOWN
                        || e.getKeyCode() == KeyEvent.VK_UP
                        || e.getKeyCode() == KeyEvent.VK_PAGE_DOWN
                        || e.getKeyCode() == KeyEvent.VK_PAGE_UP) {
                    return;
                }
                pane.dispatchEvent(e);
                e.consume();
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN
                        || e.getKeyCode() == KeyEvent.VK_UP
                        || e.getKeyCode() == KeyEvent.VK_PAGE_DOWN
                        || e.getKeyCode() == KeyEvent.VK_PAGE_UP) {
                    return;
                }
                pane.dispatchEvent(e);
                e.consume();
            }

            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN
                        || e.getKeyCode() == KeyEvent.VK_UP
                        || e.getKeyCode() == KeyEvent.VK_PAGE_DOWN
                        || e.getKeyCode() == KeyEvent.VK_PAGE_UP) {
                    return;
                }
                pane.dispatchEvent(e);
                e.consume();
            }
        });
        //list.setFocusable(false);
    }

    @Override
    public void caretUpdate(CaretEvent e) {
        markTokenAt(e.getDot());

        if (codeCompletionPopup.isVisible()) {
            showCodeCompletion();
        }
    }

    public void markTokenAt(int pos) {
        SyntaxDocument doc = ActionUtils.getSyntaxDocument(pane);
        if (doc != null) {
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
        Markers.removeMarkers(pane, underLineOtherScriptPainter);
        Markers.removeMarkers(pane, underLineOtherScriptMarkOccurencesPainter);
        Markers.removeMarkers(pane, underLineOtherFilePainter);
        Markers.removeMarkers(pane, underLineOtherFileMarkOccurencesPainter);
        occurencesPositions.clear();
    }

    public void removeErrorMarkers() {
        Markers.removeMarkers(pane, errorMarker);
    }

    public static Token getIdentifierTokenAt(SyntaxDocument sDoc, int pos) {
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

    private static Token getNearestTokenAt(SyntaxDocument sDoc, int pos) {
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

        if (separatorPosToType.containsKey(tok.start)) {
            sDoc.readUnlock();
            return;
        }

        int definitionPos = tok.start;
        if (referenceToDefinition.containsKey(tok.start)) {
            definitionPos = referenceToDefinition.get(tok.start);
        } else {
            if (referenceToExternalTypeIndex.containsKey(tok.start)) {
                int typeIndex = referenceToExternalTypeIndex.get(tok.start);
                for (int i : externalTypeIndexToReference.get(typeIndex)) {
                    if (separatorPosToType.containsKey(i)) {
                        continue;
                    }
                    Token referenceToken = getIdentifierTokenAt(sDoc, i);
                    if (referenceToken != null) {
                        Markers.SimpleMarker markerKind = marker;
                        if (lastUnderlined == referenceToken) {
                            if (lastUnderlinedLinkType == LinkType.LINK_OTHER_SCRIPT) {
                                markerKind = underLineOtherScriptMarkOccurencesPainter;
                            } else if (lastUnderlinedLinkType == LinkType.LINK_OTHER_FILE) {
                                markerKind = underLineOtherFileMarkOccurencesPainter;
                            } else {
                                markerKind = underLineMarkOccurencesPainter;
                            }
                        }
                        Markers.markToken(pane, referenceToken, markerKind);
                        occurencesPositions.add(referenceToken.start);
                    }
                }
                sDoc.readUnlock();
                return;
            } else {
                if (referenceToExternalTraitKey.containsKey(tok.start)) {
                    Path traitKey = referenceToExternalTraitKey.get(tok.start);
                    for (int i : externalTraitKeyToReference.get(traitKey)) {
                        if (separatorPosToType.containsKey(i)) {
                            continue;
                        }
                        Token referenceToken = getIdentifierTokenAt(sDoc, i);
                        if (referenceToken != null) {
                            Markers.SimpleMarker markerKind = marker;
                            if (lastUnderlined == referenceToken) {
                                if (lastUnderlinedLinkType == LinkType.LINK_OTHER_SCRIPT) {
                                    markerKind = underLineOtherScriptMarkOccurencesPainter;
                                } else if (lastUnderlinedLinkType == LinkType.LINK_OTHER_FILE) {
                                    markerKind = underLineOtherFileMarkOccurencesPainter;
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
        }
        Token definitionToken = getIdentifierTokenAt(sDoc, definitionPos < 0 ? -(definitionPos + 1) : definitionPos);
        if (definitionToken != null) {
            if (definitionPosToReferences.containsKey(definitionPos)) {

                Markers.SimpleMarker markerKind = marker;
                if (lastUnderlined == definitionToken) {
                    if (lastUnderlinedLinkType == LinkType.LINK_OTHER_SCRIPT) {
                        markerKind = underLineOtherScriptMarkOccurencesPainter;
                    } else if (lastUnderlinedLinkType == LinkType.LINK_OTHER_FILE) {
                        markerKind = underLineOtherFileMarkOccurencesPainter;
                    } else {
                        markerKind = underLineMarkOccurencesPainter;
                    }
                }
                if (tokenTypes.contains(definitionToken.type)) {
                    Markers.markToken(pane, definitionToken, markerKind);
                }
                occurencesPositions.add(definitionToken.start);
                for (int i : definitionPosToReferences.get(definitionPos)) {
                    if (separatorPosToType.containsKey(i)) {
                        continue;
                    }
                    Token referenceToken = getIdentifierTokenAt(sDoc, i);
                    if (referenceToken != null) {
                        markerKind = marker;
                        if (lastUnderlined == referenceToken) {
                            if (lastUnderlinedLinkType == LinkType.LINK_OTHER_SCRIPT) {
                                markerKind = underLineOtherScriptMarkOccurencesPainter;
                            } else if (lastUnderlinedLinkType == LinkType.LINK_OTHER_FILE) {
                                markerKind = underLineOtherFileMarkOccurencesPainter;
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
        
        
        Color editorBackground = UIManager.getColor("EditorPane.background");
        int light = (editorBackground.getRed() + editorBackground.getGreen() + editorBackground.getBlue()) / 3;
        if (light < 128) {
            markerColor = new Color(0x443322);
        }
        
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

    private void completeCode(String suggestion) {
        try {
            SyntaxDocument sDoc = (SyntaxDocument) pane.getDocument();
            sDoc.readLock();

            int pos = pane.getCaretPosition();
            Token tokenAt = getNearestTokenAt(sDoc, pos);

            String identText = "";
            if (tokenAt != null && tokenAt.type == TokenType.IDENTIFIER) {
                identText = sDoc.getText(tokenAt.start, pos - tokenAt.start);
                tokenAt = sDoc.getPrevToken(tokenAt);
            } else if (tokenAt != null && tokenAt.start >= pos) {
                tokenAt = sDoc.getPrevToken(tokenAt);
                if (tokenAt != null && tokenAt.type == TokenType.IDENTIFIER) {
                    identText = sDoc.getText(tokenAt.start, pos - tokenAt.start);
                    tokenAt = sDoc.getPrevToken(tokenAt);
                }
            } else if (tokenAt == null) {
                while (tokenAt == null && pos >= 0) {
                    pos--;
                    tokenAt = getNearestTokenAt(sDoc, pos);
                }
                if (tokenAt != null && tokenAt.type == TokenType.IDENTIFIER) {
                    identText = sDoc.getText(tokenAt.start, pos - tokenAt.start);
                    tokenAt = sDoc.getPrevToken(tokenAt);
                }
            }

            boolean isDot = tokenAt != null && tokenAt.type == TokenType.OPERATOR ? ".".equals(sDoc.getText(tokenAt.start, tokenAt.length)) : false;
            sDoc.readUnlock();
            if (isDot) {
                int afterDot = tokenAt.start + 1;
                pane.getDocument().remove(afterDot, pane.getCaretPosition() - afterDot);
                pane.getDocument().insertString(afterDot, suggestion, null);
            } else {
                pane.getDocument().remove(pane.getCaretPosition() - identText.length(), identText.length());
                pane.getDocument().insertString(pane.getCaretPosition(), suggestion, null);
            }
        } catch (BadLocationException ex) {
            //ignore
        }
    }

    @SuppressWarnings("unchecked")
    private void showCodeCompletion() {
        SyntaxDocument sDoc = (SyntaxDocument) pane.getDocument();
        sDoc.readLock();
        try {
            int pos = pane.getCaretPosition();
            Token tokenAt = getNearestTokenAt(sDoc, pos);

            String identText = "";
            if (tokenAt != null && tokenAt.type == TokenType.IDENTIFIER) {
                identText = sDoc.getText(tokenAt.start, pos - tokenAt.start);
                tokenAt = sDoc.getPrevToken(tokenAt);
            } else if (tokenAt != null && tokenAt.start >= pos) {
                tokenAt = sDoc.getPrevToken(tokenAt);
                if (tokenAt != null && tokenAt.type == TokenType.IDENTIFIER) {
                    identText = sDoc.getText(tokenAt.start, pos - tokenAt.start);
                    tokenAt = sDoc.getPrevToken(tokenAt);
                }
            } else if (tokenAt == null) {
                while (tokenAt == null && pos >= 0) {
                    pos--;
                    tokenAt = getNearestTokenAt(sDoc, pos);
                }
                if (tokenAt != null && tokenAt.type == TokenType.IDENTIFIER) {
                    identText = sDoc.getText(tokenAt.start, pos - tokenAt.start);
                    tokenAt = sDoc.getPrevToken(tokenAt);
                }
            }

            boolean isDot = tokenAt != null && tokenAt.type == TokenType.OPERATOR ? ".".equals(sDoc.getText(tokenAt.start, tokenAt.length)) : false;
            List<Variable> suggestions = new ArrayList<>();
            if (isDot) {

                Token prevToken = sDoc.getPrevToken(tokenAt);
                boolean isCall = prevToken.pairValue == -1; //-1 = -PARENT

                pos = tokenAt.start;

                if (!separatorPosToType.containsKey(pos)) {
                    return;
                }

                if (separatorPosToType.containsKey(pos)) {
                    Path type = separatorPosToType.get(pos);

                    boolean isStatic = false;
                    if (separatorIsStatic.containsKey(pos) && separatorIsStatic.get(pos)) {
                        isStatic = true;
                    }

                    if (localTypeTraits.containsKey(type)) {
                        for (Variable traitName : localTypeTraits.get(type)) {
                            if (isStatic && !traitName.isStatic) {
                                continue;
                            }
                            //remove constructor
                            if (traitName.name.toString().equals(type.getLast().toString())) {
                                continue;
                            }
                            suggestions.add(traitName);
                        }
                    } else {
                        //type = externalTypes.get(referenceToExternalTypeIndex.get(pos));
                        if (simpleExternalClassNameToFullClassName.containsKey(type)) {
                            type = simpleExternalClassNameToFullClassName.get(type);
                        }
                        List<Variable> traitNames = ((LineMarkedEditorPane) pane).getLinkHandler().getClassTraits(type, true, true, true);
                        for (Variable traitName : traitNames) {
                            if (isStatic && !traitName.isStatic) {
                                continue;
                            }
                            //remove constructor
                            if (traitName.name.toString().equals(type.getLast().toString())) {
                                continue;
                            }
                            suggestions.add(traitName);
                        }
                    }
                }
            } else {
                suggestions.addAll(variableSuggestions);
            }
            if (suggestions.isEmpty()) {
                codeCompletionPopup.setVisible(false);
                return;
            }
            NaturalOrderComparator noc = new NaturalOrderComparator();
            Collections.sort(suggestions, new Comparator<Variable>() {
                @Override
                public int compare(Variable o1, Variable o2) {
                    return noc.compare(o1.name.getLast().toString(), o2.name.getLast().toString());
                }
            });
            if (!identText.isEmpty()) {
                for (int i = suggestions.size() - 1; i >= 0; i--) {
                    String sug = suggestions.get(i).name.getLast().toString();
                    if (!sug.startsWith(identText)) {
                        suggestions.remove(i);
                    }
                }
            }

            codeCompletionList.setSelectedIndex(-1);

            codeCompletionListModel.clear();

            for (Variable s : suggestions) {
                codeCompletionListModel.addElement(new VariableListItem(s));
            }

            if (!codeCompletionPopup.isVisible() && !suggestions.isEmpty()) {
                Rectangle2D caretCoords = View.textComponentModelToView(pane, pane.getCaretPosition());
                codeCompletionPopup.show(pane, (int) caretCoords.getX(), (int) (caretCoords.getY() + caretCoords.getHeight()));
            }
            codeCompletionPopup.setPopupSize(codeCompletionList.getPreferredSize().width + 32, 200);
            codeCompletionList.setSize(codeCompletionList.getPreferredSize().width, 200);
            pane.requestFocusInWindow();
            pane.getCaret().setVisible(true);
        } catch (BadLocationException ex) {
            //ignore
        } finally {
            sDoc.readUnlock();
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

        keyAdapter = new KeyAdapter() {
            Timer tim = null;

            @Override
            public void keyTyped(KeyEvent e) {
                if (tim != null) {
                    tim.cancel();
                    tim = null;
                }
                if (e.getKeyChar() == '.' && com.jpexs.decompiler.flash.configuration.Configuration.showCodeCompletionOnDot.get()) {
                    if (!editor.isEditable()) {
                        return;
                    }
                    tim = new Timer();
                    tim.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            View.execInEventDispatch(new Runnable() {
                                @Override
                                public void run() {
                                    reParse();
                                    showCodeCompletion();
                                }
                            });
                        }
                    }, 100);
                }
            }
        };

        pane.addMouseListener(linkAdapter);
        pane.addMouseMotionListener(linkAdapter);
        pane.addKeyListener(keyAdapter);

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

        View.addEditorAction(pane, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!pane.isEditable()) {
                    return;
                }
                documentUpdated();
                showCodeCompletion();
            }

        }, "code-completion", AppStrings.translate("action.code-completion"), "control SPACE");

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
            if (goingOut) {
                return;
            }
            if (lastCursorPos == null) {
                return;
            }
            if (ctrlDown) {
                Token t = ((LineMarkedEditorPane) pane).tokenAtPos(lastCursorPos);

                if (t != lastUnderlined) {
                    if (t == null || lastUnderlined == null || !t.equals(lastUnderlined)) {
                        MyMarkers.removeMarkers(pane, underLinePainter);
                        MyMarkers.removeMarkers(pane, underLineMarkOccurencesPainter);
                        MyMarkers.removeMarkers(pane, underLineOtherScriptPainter);
                        MyMarkers.removeMarkers(pane, underLineOtherScriptMarkOccurencesPainter);
                        MyMarkers.removeMarkers(pane, underLineOtherFilePainter);
                        MyMarkers.removeMarkers(pane, underLineOtherFileMarkOccurencesPainter);

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
                            painter = underLineOtherScriptMarkOccurencesPainter;
                        } else if (lastUnderlinedLinkType == LinkType.LINK_OTHER_FILE) {
                            painter = underLineOtherFileMarkOccurencesPainter;
                        } else {
                            painter = underLineMarkOccurencesPainter;
                        }
                        removeMarkers();
                        markTokenAt(pane.getCaretPosition());
                    } else {
                        if (lastUnderlinedLinkType == LinkType.LINK_OTHER_SCRIPT) {
                            painter = underLineOtherScriptPainter;
                        } else if (lastUnderlinedLinkType == LinkType.LINK_OTHER_FILE) {
                            painter = underLineOtherFilePainter;
                        }
                        MyMarkers.markToken(pane, lastUnderlined, painter);
                    }
                } else {
                    pane.setCursor(Cursor.getDefaultCursor());
                }
                highlightsPanel.repaint();
            } else {
                if (lastUnderlined != null) {
                    lastUnderlined = null;
                    MyMarkers.removeMarkers(pane, underLinePainter);
                    MyMarkers.removeMarkers(pane, underLineMarkOccurencesPainter);
                    MyMarkers.removeMarkers(pane, underLineOtherScriptPainter);
                    MyMarkers.removeMarkers(pane, underLineOtherScriptMarkOccurencesPainter);
                    MyMarkers.removeMarkers(pane, underLineOtherFilePainter);
                    MyMarkers.removeMarkers(pane, underLineOtherFileMarkOccurencesPainter);

                    removeMarkers();
                    markTokenAt(pane.getCaretPosition());
                }
                pane.setCursor(Cursor.getDefaultCursor());
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (lastCursorPos == null) {
                return;
            }
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
            if (token == null) {
                pane.setToolTipText(null);
                return;
            }
            String err = errors.get(token.start);
            if (err != null) {
                pane.setToolTipText(err);
                return;
            }

            /*
            String traitKey = referenceToExternalTraitKey.get(token.start);
            if (traitKey != null) {
                pane.setToolTipText(traitKey);
                return;
            }
            
            if (referenceToExternalTypeIndex.containsKey(token.start)) {
                String externalType = externalTypes.get(referenceToExternalTypeIndex.get(token.start));
                if (externalType != null) {
                    pane.setToolTipText(externalType);
                    return;
                }
            }
             */
            pane.setToolTipText(null);
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
        View.removeEditorAction(pane, "code-completion");
        pane.removePropertyChangeListener(this);
        pane.getDocument().removeDocumentListener(this);
        pane.removeCaretListener(this);
        pane.removeMouseMotionListener(mouseMotionAdapter);
        pane.removeMouseMotionListener(linkAdapter);
        pane.removeMouseListener(linkAdapter);
        pane.removeKeyListener(keyAdapter);
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
            goingOut = false;
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

    private synchronized void reParse() {
        errors.clear();
        removeErrorMarkers();
        highlightsPanel.repaint();
        boolean doClear = false;
        try {
            SyntaxDocument sDoc = (SyntaxDocument) pane.getDocument();

            sDoc.readLock();;
            int pos = pane.getCaretPosition();
            String fullText = sDoc.getText(0, sDoc.getLength());
            sDoc.readUnlock();
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
            List<Path> newExternalTypes = new ArrayList<>();
            Map<Integer, Integer> newReferenceToExternalTypeIndex = new LinkedHashMap<>();
            Map<Integer, List<Integer>> newExternalTypeIndexToReference = new LinkedHashMap<>();
            Map<Integer, Path> newReferenceToExternalTraitKey = new LinkedHashMap<>();
            Map<Path, List<Integer>> newExternalTraitKeyToReference = new LinkedHashMap<>();
            Map<Integer, Path> newSeparatorPosToType = new LinkedHashMap<>();
            Map<Path, List<Variable>> newLocalTypeTraits = new LinkedHashMap<>();
            Map<Integer, Path> newDefinitionToType = new LinkedHashMap<>();
            Map<Integer, Path> newDefinitionToCallType = new LinkedHashMap<>();
            Map<Integer, Boolean> newSeparatorIsStatic = new LinkedHashMap<>();
            List<Variable> newVariableSuggestions = new ArrayList<>();
            parser.parse(
                    fullText,
                    newDefinitionPosToReferences,
                    newReferenceToDefinition,
                    newErrors,
                    newExternalTypes,
                    newReferenceToExternalTypeIndex,
                    newExternalTypeIndexToReference,
                    ((LineMarkedEditorPane) pane).getLinkHandler(),
                    newReferenceToExternalTraitKey, newExternalTraitKeyToReference,
                    newSeparatorPosToType,
                    newSeparatorIsStatic,
                    newLocalTypeTraits,
                    newDefinitionToType,
                    newDefinitionToCallType,
                    pos,
                    newVariableSuggestions
            );

            Map<Path, Path> newSimpleExternalClassNameToFullClassName = new LinkedHashMap<>();
            for (int i = 0; i < newExternalTypes.size(); i++) {
                Path type = newExternalTypes.get(i);
                newSimpleExternalClassNameToFullClassName.put(type.getLast(), type);
            }
            
            definitionPosToReferences.clear();            
            referenceToDefinition.clear();
            externalTypes.clear();
            referenceToExternalTypeIndex.clear();
            externalTypeIndexToReference.clear();
            simpleExternalClassNameToFullClassName.clear();
            referenceToExternalTraitKey.clear();
            externalTraitKeyToReference.clear();
            separatorPosToType.clear();
            localTypeTraits.clear();
            definitionToType.clear();
            definitionToCallType.clear();
            separatorIsStatic.clear();
            variableSuggestions.clear();
            
            definitionPosToReferences = newDefinitionPosToReferences;            
            referenceToDefinition = newReferenceToDefinition;
            externalTypes = newExternalTypes;
            referenceToExternalTypeIndex = newReferenceToExternalTypeIndex;
            externalTypeIndexToReference = newExternalTypeIndexToReference;
            simpleExternalClassNameToFullClassName = newSimpleExternalClassNameToFullClassName;
            referenceToExternalTraitKey = newReferenceToExternalTraitKey;
            externalTraitKeyToReference = newExternalTraitKeyToReference;
            separatorPosToType = newSeparatorPosToType;
            localTypeTraits = newLocalTypeTraits;
            definitionToType = newDefinitionToType;
            definitionToCallType = newDefinitionToCallType;
            separatorIsStatic = newSeparatorIsStatic;
            variableSuggestions = newVariableSuggestions;
            for (SimpleParseException ex : newErrors) {
                errors.put((int) ex.position, ex.getMessage());
            }
        } catch (BadLocationException | IOException | InterruptedException ex) {
            definitionPosToReferences.clear();
            referenceToDefinition.clear();
            doClear = true;
        } catch (SimpleParseException ex) {
            doClear = true;
            errors.put((int) ex.position, ex.getMessage());
        }
        
        if (doClear) {
            definitionPosToReferences.clear();
            referenceToDefinition.clear();
            definitionPosToReferences.clear();            
            referenceToDefinition.clear();
            externalTypes.clear();
            referenceToExternalTypeIndex.clear();
            externalTypeIndexToReference.clear();
            simpleExternalClassNameToFullClassName.clear();
            referenceToExternalTraitKey.clear();
            externalTraitKeyToReference.clear();
            separatorPosToType.clear();
            localTypeTraits.clear();
            definitionToType.clear();
            definitionToCallType.clear();
            separatorIsStatic.clear();
            variableSuggestions.clear();
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

    private void documentUpdated() {
        Timer pTimer = parseTimer;
        if (pTimer != null) {
            pTimer.cancel();
        }
        pTimer = new Timer();

        pTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                reParse();
            }
        }, 100);
        parseTimer = pTimer;
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
        if (separatorPosToType.containsKey(token.start)) {
            return LinkType.NO_LINK;
        }
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
        if (referenceToExternalTypeIndex.containsKey(token.start)) {
            Path externalType = externalTypes.get(referenceToExternalTypeIndex.get(token.start));
            return ((LineMarkedEditorPane) pane).getLinkHandler().getClassLinkType(externalType);
        }
        if (referenceToExternalTraitKey.containsKey(token.start)) {
            Path traitKey = referenceToExternalTraitKey.get(token.start);
            //String traitName = traitKey.substring(traitKey.lastIndexOf("/") + 1);
            Path className = traitKey.getParent();
            if (simpleExternalClassNameToFullClassName.containsKey(className)) {
                className = simpleExternalClassNameToFullClassName.get(className);
            }
            return ((LineMarkedEditorPane) pane).getLinkHandler().getClassLinkType(className);
        }

        return LinkType.NO_LINK;
    }

    private class UnderlinedLabel extends JLabel {

        private final Color underlineColor;

        public UnderlinedLabel(String text, Color underlineColor) {
            super(text);
            this.underlineColor = underlineColor;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            FontMetrics fm = g.getFontMetrics();
            g.setColor(underlineColor);
            g.drawLine(0, fm.getHeight() - 3, fm.stringWidth(getText()), fm.getHeight() - 3);
        }
    }

    private class CommentLabel extends JLabel {

        public CommentLabel(String text) {
            super(text);
            setForeground(new Color(0x339933));
        }
    }

    public void handleLink(Token token) {
        Integer definition = referenceToDefinition.get(token.start);
        if (definition != null && definition >= 0) {
            pane.setCaretPosition(definition);
        } else if (!pane.isEditable()) {

            if (referenceToExternalTypeIndex.containsKey(token.start)
                    && com.jpexs.decompiler.flash.configuration.Configuration.warningLinkTypes.get()) {

                Path externalType = externalTypes.get(referenceToExternalTypeIndex.get(token.start));

                LinkType lt = ((LineMarkedEditorPane) pane).getLinkHandler().getClassLinkType(externalType);

                if (lt == LinkType.LINK_OTHER_FILE) {
                    JPanel msgPanel = new JPanel();
                    msgPanel.setLayout(new BoxLayout(msgPanel, BoxLayout.Y_AXIS));
                    JLabel bewareLabel = new JLabel(AppStrings.translate("message.link.clicked"));
                    bewareLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                    msgPanel.add(bewareLabel);

                    JLabel beware2Label = new JLabel(AppStrings.translate("message.link.bewareTypes"));
                    beware2Label.setAlignmentX(Component.CENTER_ALIGNMENT);
                    msgPanel.add(beware2Label);

                    JPanel linksPanel = new JPanel(new GridBagLayout());
                    linksPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                    linksPanel.setBackground(Color.white);
                    GridBagConstraints gbc = new GridBagConstraints();
                    gbc.anchor = GridBagConstraints.FIRST_LINE_START;
                    gbc.insets = new Insets(2, 2, 2, 2);
                    gbc.gridx = 0;
                    gbc.gridy = 0;

                    linksPanel.add(new UnderlinedLabel(AppStrings.translate("message.link.type.currentScript.sample"), basicUnderlineColor), gbc);
                    gbc.gridx++;
                    linksPanel.add(new CommentLabel("//" + AppStrings.translate("message.link.type.currentScript")), gbc);
                    gbc.gridx = 0;
                    gbc.gridy++;
                    linksPanel.add(new UnderlinedLabel(AppStrings.translate("message.link.type.otherScript.sample"), otherScriptUnderlineColor), gbc);
                    gbc.gridx++;
                    linksPanel.add(new CommentLabel("//" + AppStrings.translate("message.link.type.otherScript")), gbc);
                    gbc.gridx = 0;
                    gbc.gridy++;
                    linksPanel.add(new UnderlinedLabel(AppStrings.translate("message.link.type.otherFile.sample"), otherFileUnderlineColor), gbc);
                    gbc.gridx++;
                    linksPanel.add(new CommentLabel("//" + AppStrings.translate("message.link.type.otherFile")), gbc);

                    gbc.gridx = 2;
                    gbc.gridy = 0;
                    gbc.gridheight = 3;
                    gbc.weightx = 1;
                    gbc.weighty = 1;
                    JPanel finalPanel = new JPanel();
                    finalPanel.setOpaque(false);
                    linksPanel.add(finalPanel);

                    linksPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

                    msgPanel.add(linksPanel);
                    JLabel reallyLabel = new JLabel(AppStrings.translate("message.link.reallyGo"));
                    reallyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                    msgPanel.add(reallyLabel);
                    JCheckBox doNotShowAgainCheckbox = new JCheckBox(AppStrings.translate("message.confirm.donotshowagain"));
                    doNotShowAgainCheckbox.setAlignmentX(Component.CENTER_ALIGNMENT);
                    msgPanel.add(doNotShowAgainCheckbox);

                    int result = ViewMessages.showOptionDialog(Main.getDefaultMessagesComponent(),
                            msgPanel,
                            AppStrings.translate("message.warning"),
                            JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.WARNING_MESSAGE,
                            null, null, null);
                    if (doNotShowAgainCheckbox.isSelected()) {
                        com.jpexs.decompiler.flash.configuration.Configuration.warningLinkTypes.set(false);
                    }
                    if (result != JOptionPane.OK_OPTION) {
                        return;
                    }
                }

            }
            lastUnderlined = null;
            removeErrorMarkers();
            removeMarkers();
            pane.repaint();

            if (referenceToExternalTypeIndex.containsKey(token.start)) {
                goingOut = true;
                pane.setCursor(Cursor.getDefaultCursor());
                Path externalType = externalTypes.get(referenceToExternalTypeIndex.get(token.start));
                ((LineMarkedEditorPane) pane).getLinkHandler().handleClassLink(externalType);
                return;
            }
            if (referenceToExternalTraitKey.containsKey(token.start)) {
                Path traitKey = referenceToExternalTraitKey.get(token.start);
                String traitName = traitKey.getLast().toString();
                Path className = traitKey.getParent();

                if (simpleExternalClassNameToFullClassName.containsKey(className)) {
                    className = simpleExternalClassNameToFullClassName.get(className);
                }
                ((LineMarkedEditorPane) pane).getLinkHandler().handleTraitLink(className, traitName);
            }
        }
    }

    private class VariableListItem {

        private Variable variable;

        public VariableListItem(Variable variable) {
            this.variable = variable;
        }

        public Variable getVariable() {
            return variable;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(variable.name.getLast().toString());
            if (variable.callType != null) {
                sb.append("() : ");
                sb.append(variable.callType.getLast().toString());
            } else if (variable.type != null) {
                sb.append(" : ");
                sb.append(variable.type.getLast().toString());
            }
            return sb.toString();
        }
    }
}
