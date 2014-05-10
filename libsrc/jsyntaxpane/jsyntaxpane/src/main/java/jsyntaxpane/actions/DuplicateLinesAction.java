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
package jsyntaxpane.actions;

import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import jsyntaxpane.SyntaxDocument;

/**
 * This Action duplicates the current line, or all the highlighted lines.
 * @author Ayman Al-Sairafi
 */
public class DuplicateLinesAction extends DefaultSyntaxAction {

    public DuplicateLinesAction() {
        super("DUPLICATE_LINES");
    }

    @Override
    public void actionPerformed(JTextComponent target, SyntaxDocument sdoc,
            int dot, ActionEvent e) {
        try {
            int st = sdoc.getLineStartOffset(target.getSelectionStart());
            int en = sdoc.getLineEndOffset(target.getSelectionEnd());
            String dupLines = sdoc.getText(st, en - st);
            sdoc.insertString(st, dupLines, null);
        } catch (BadLocationException ex) {
            Logger.getLogger(DuplicateLinesAction.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
