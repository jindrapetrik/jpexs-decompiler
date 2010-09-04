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
import javax.swing.text.JTextComponent;
import jsyntaxpane.SyntaxDocument;
import jsyntaxpane.Token;
import jsyntaxpane.TokenType;

/**
 * Completes an the Tag.
 * @author Ayman Al-Sairafi
 */
public class XmlTagCompleteAction extends DefaultSyntaxAction {

    public XmlTagCompleteAction() {
        super("XML_TAG_COMPLETE");
    }

    @Override
    public void actionPerformed(JTextComponent target, SyntaxDocument sDoc,
            int dot, ActionEvent e) {
        Token tok = sDoc.getTokenAt(dot);
        while (tok != null && tok.type != TokenType.TYPE) {
            tok = sDoc.getPrevToken(tok);
        }
        if (tok == null) {
            target.replaceSelection(">");
        } else {
            CharSequence tag = tok.getText(sDoc);
            int savepos = target.getSelectionStart();
            target.replaceSelection("></" + tag.subSequence(1, tag.length()) + ">");
            target.setCaretPosition(savepos + 1);
        }
    }
}
