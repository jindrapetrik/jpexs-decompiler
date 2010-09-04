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
import java.util.Arrays;
import javax.swing.text.JTextComponent;
import jsyntaxpane.SyntaxDocument;
import jsyntaxpane.Token;

/**
 * This actions Jumps to the pair of the token at the cursor.
 */
public class JumpToPairAction extends DefaultSyntaxAction {

    public JumpToPairAction() {
        super("JUMP_TO_PAIR");
    }

    @Override
    public void actionPerformed(JTextComponent target, SyntaxDocument sdoc,
            int dot, ActionEvent e) {
        Token current = sdoc.getTokenAt(dot);
        if (current == null) {
            return;
        }

        Token pair = sdoc.getPairFor(current);
        if (pair != null) {
            target.setCaretPosition(pair.start);
        }
    }
}
