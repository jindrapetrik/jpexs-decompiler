/*
 *  Copyright (C) 2010-2014 JPEXS
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
package com.jpexs.decompiler.flash.gui.abc;

import com.jpexs.decompiler.flash.configuration.Configuration;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import static javax.swing.JEditorPane.createEditorKitForContentType;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import jsyntaxpane.SyntaxDocument;

/**
 *
 * @author JPEXS
 */
public class UndoFixedEditorPane extends JEditorPane {

    private static final Object setTextLock = new Object();

    @Override
    public void setText(String t) {
        setText(t, getContentType());
    }

    public void setText(String t, String contentType) {
        synchronized (setTextLock) {
            if (!t.equals(getText())) {
                boolean plain = t.length() > Configuration.syntaxHighlightLimit.get();
                if (plain) {
                    contentType = "text/plain";
                }

                try {
                    if (!getContentType().equals(contentType)) {
                        setContentType(contentType);
                    }
                    Document doc = getDocument();
                    setDocument(new SyntaxDocument(null));
                    doc.remove(0, doc.getLength());
                    Reader r = new StringReader(t);
                    EditorKit kit = createEditorKitForContentType(contentType);
                    kit.read(r, doc, 0);
                    setDocument(doc);
                } catch (BadLocationException | IOException ex) {
                    Logger.getLogger(UndoFixedEditorPane.class.getName()).log(Level.SEVERE, null, ex);
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
}
