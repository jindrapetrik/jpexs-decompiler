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

import jsyntaxpane.actions.gui.ReflectCompletionDialog;
import java.awt.event.ActionEvent;
import javax.swing.text.JTextComponent;
import jsyntaxpane.SyntaxDocument;
import jsyntaxpane.Token;

/**
 * ComboBox like Completion Action:
 * This will display a list of items to choose from, its can be used similar to
 * IntelliSense
 * 
 * @author Ayman Al-Sairafi
 */
public class ReflectCompletionAction extends DefaultSyntaxAction {

    ReflectCompletionDialog dlg;

    public ReflectCompletionAction() {
        super("REFLECT_COMPLETION");
    }

    @Override
    public void actionPerformed(JTextComponent target, SyntaxDocument sDoc,
            int dot, ActionEvent e) {
        Token t = sDoc.getTokenAt(dot);
        if(t != null) {
            target.select(t.start, t.end());
        }
        if (dlg == null) {
            dlg = new ReflectCompletionDialog(target);
        }
        dlg.displayFor(target);
    }
}
