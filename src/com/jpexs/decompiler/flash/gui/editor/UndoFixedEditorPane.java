/*
 *  Copyright (C) 2010-2024 JPEXS
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

import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.helpers.Stopwatch;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;
import jsyntaxpane.SyntaxDocument;

/**
 * @author JPEXS
 */
public class UndoFixedEditorPane extends JEditorPane {

    private static final Object setTextLock = new Object();

    private final List<TextChangedListener> textChangedListeners = new ArrayList<>();

    private String originalContentType;

    private DocumentListener documentListener;

    public UndoFixedEditorPane() {
        addDocumentListener();
    }

    private void fireTextChanged() {
        for (TextChangedListener listener : textChangedListeners) {
            listener.textChanged();
        }
    }

    private void addDocumentListener() {
        documentListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                fireTextChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                fireTextChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                fireTextChanged();
            }
        };

        getDocument().addDocumentListener(documentListener);
    }

    private void removeDocumentListener() {
        getDocument().removeDocumentListener(documentListener);
    }

    public void changeContentType(String type) {
        if (!type.equals(getContentType())) {
            removeDocumentListener();
            Font oldFont = getFont();
            setContentType(type);
            setFont(oldFont);
            addDocumentListener();
        }

        if (!Configuration.autoCloseQuotes.get()) {
            getActionMap().remove("quotes");
            KeyStroke ks = KeyStroke.getKeyStroke("typed '");
            getInputMap(JTextComponent.WHEN_FOCUSED).remove(ks);
        }

        if (!Configuration.autoCloseDoubleQuotes.get()) {
            getActionMap().remove("double-quotes");
            KeyStroke ks = KeyStroke.getKeyStroke("typed \"");
            getInputMap(JTextComponent.WHEN_FOCUSED).remove(ks);
        }

        if (!Configuration.autoCloseBrackets.get()) {
            getActionMap().remove("brackets");
            KeyStroke ks = KeyStroke.getKeyStroke("typed [");
            getInputMap(JTextComponent.WHEN_FOCUSED).remove(ks);
        }

        if (!Configuration.autoCloseParenthesis.get()) {
            getActionMap().remove("parenthesis");
            KeyStroke ks = KeyStroke.getKeyStroke("typed (");
            getInputMap(JTextComponent.WHEN_FOCUSED).remove(ks);
        }
    }

    @Override
    public void setText(final String t) {
        View.execInEventDispatch(() -> {
            removeDocumentListener();
            setText(t, getContentType());
            addDocumentListener();
        });
    }

    private void setText(String t, String contentType) {
        synchronized (setTextLock) {
            if (t == null) {
                t = "";
            }

            if (!t.equals(getText())) {
                boolean plain = t.length() > Configuration.syntaxHighlightLimit.get();
                if (plain) {
                    contentType = "text/plain";
                    originalContentType = getContentType();
                    changeContentType(contentType);
                } else if (originalContentType != null) {
                    changeContentType(originalContentType);
                    originalContentType = null;
                }

                Stopwatch sw = Stopwatch.startNew();
                try {
                    Reader r = new StringReader(t);
                    EditorKit kit = createEditorKitForContentType(contentType);
                    Document doc = kit.createDefaultDocument();
                    if (doc instanceof SyntaxDocument) {
                        ((SyntaxDocument) doc).setIgnoreUpdate(true);
                    }

                    kit.read(r, doc, 0);

                    if (doc instanceof SyntaxDocument) {
                        ((SyntaxDocument) doc).setIgnoreUpdate(false);
                    }

                    doc.putProperty(PlainDocument.tabSizeAttribute, Configuration.tabSize.get());
                    doc.putProperty("jpexs:useTabs", Configuration.indentUseTabs.get());

                    setDocument(doc);
                } catch (BadLocationException | IOException ex) {
                    Logger.getLogger(UndoFixedEditorPane.class.getName()).log(Level.SEVERE, null, ex);
                }

                sw.stop();
                if (!plain && sw.getElapsedMilliseconds() > 5000) {
                    Logger.getLogger(UndoFixedEditorPane.class.getName()).log(Level.WARNING, "Syntax highlighting took long time. You can try to decrease the syntax highlight limit in advanced settings.");
                }

                clearUndos();
            }
        }
    }

    @Override
    protected void processKeyEvent(KeyEvent ke) {
        if (!isEditable()) {
            // disable Ctrl-E: delete line
            // and Ctrl-H: Search and replace
            if ((ke.getKeyCode() == KeyEvent.VK_E && ke.isControlDown())
                    || (ke.getKeyCode() == KeyEvent.VK_H && ke.isControlDown())) {
                return;
            }
        }

        super.processKeyEvent(ke);
    }

    public void clearUndos() {
        Document doc = getDocument();
        if (doc instanceof SyntaxDocument) {
            SyntaxDocument sdoc = (SyntaxDocument) doc;
            sdoc.clearUndos();
        }
    }

    public void addTextChangedListener(TextChangedListener l) {
        textChangedListeners.add(l);
    }

    public void removeTextChangedListener(TextChangedListener l) {
        textChangedListeners.remove(l);
    }
}
