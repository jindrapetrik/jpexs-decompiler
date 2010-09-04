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
import java.text.CharacterIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Segment;
import jsyntaxpane.SyntaxDocument;

/**
 *
 * @author Ayman Al-Sairafi
 */
public class SmartHomeAction extends DefaultSyntaxAction {

    public SmartHomeAction() {
        super("smart-home");
    }

    @Override
    public void actionPerformed(JTextComponent target, SyntaxDocument sDoc,
            int dot, ActionEvent e) {
        try {
            target.setCaretPosition(getSmartHomeOffset(target, sDoc, dot));
        } catch (BadLocationException ex) {
            Logger.getLogger(SmartHomeAction.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    static int getSmartHomeOffset(JTextComponent target, SyntaxDocument sDoc,
            int dot) throws BadLocationException {
        Element el = sDoc.getParagraphElement(dot);
        Segment seg = new Segment();
        sDoc.getText(el.getStartOffset(),
                el.getEndOffset() - el.getStartOffset() - 1, seg);
        int homeOffset = 0;
        int dotLineOffset = dot - el.getStartOffset();
        boolean inText = false;
        // see the location of first non-space offset
        for (int i = 0; i < dotLineOffset; i++) {
            if (!Character.isWhitespace(seg.charAt(i))) {
                inText = true;
                break;
            }
        }
        // if we are at first char in line, or we are past the non space
        // chars in the line, then we move to non-space char
        // otherwise, we move to first char of line
        if (dotLineOffset == 0 || inText) {
            for (char ch = seg.first();
                    ch != CharacterIterator.DONE && Character.isWhitespace(ch);
                    ch = seg.next()) {
                homeOffset++;
            }
        }
        return el.getStartOffset() + homeOffset;
    }
}
