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

/**
 * This is usually mapped to Shift-TAB to unindent the selection.  The
 * current line, or the selected lines are un-indented by the tabstop of the
 * document.
 */
public class UnindentAction extends DefaultSyntaxAction {

    public UnindentAction() {
        super("UNINDENT");
    }

    @Override
    public void actionPerformed(JTextComponent target, SyntaxDocument sDoc,
            int dot, ActionEvent e) {
        String indent = ActionUtils.getTab(target);
        String[] lines = ActionUtils.getSelectedLines(target);
        int start = target.getSelectionStart();
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            if (line.startsWith(indent)) {
                sb.append(line.substring(indent.length()));
            } else if (line.startsWith("\t")) {
                sb.append(line.substring(1));
            } else {
                sb.append(line);
            }
            sb.append('\n');
        }
        target.replaceSelection(sb.toString());
        target.select(start, start + sb.length());
    }
}
