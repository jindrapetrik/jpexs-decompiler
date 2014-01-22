/*
 *  Copyright (C) 2013 JPEXS
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
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import jsyntaxpane.SyntaxDocument;

/**
 *
 * @author JPEXS
 */
public class UndoFixedEditorPane extends JEditorPane {

    @Override
    public void setText(String t) {
        if (getText() != t) {
            super.setText(t);
            clearUndos();
        }
    }

    public void setText(String t, String contentType) {
        if (getText() != t) {
            // OK to check reference equals, because the string object should be the same
            super.setText(null);
            if (t.length() > Configuration.syntaxHighlightLimit.get()) {
                setContentType("text/plain");
            } else {
                if (!getContentType().equals(contentType)) {
                    setContentType(contentType);
                }
            }
            super.setText(t);
            clearUndos();
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
