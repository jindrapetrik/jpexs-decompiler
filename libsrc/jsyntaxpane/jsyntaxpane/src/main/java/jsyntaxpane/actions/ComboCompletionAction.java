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
import java.util.List;
import java.util.Map;
import javax.swing.text.JTextComponent;
import jsyntaxpane.SyntaxDocument;
import jsyntaxpane.Token;
import jsyntaxpane.actions.gui.ComboCompletionDialog;
import jsyntaxpane.util.JarServiceProvider;

/**
 * ComboBox like Completion Action:
 * This will display a list of items to choose from, it can be used similar to
 * IntelliSense.  The List is obtained from a plain text file, each line being
 * an item in the list.
 * 
 * @author Ayman Al-Sairafi
 */
public class ComboCompletionAction extends DefaultSyntaxAction {

    Map<String, String> completions;
    ComboCompletionDialog dlg;
    private List<String> items;

    public ComboCompletionAction() {
        super("COMBO_COMPLETION");
    }

    @Override
    public void actionPerformed(JTextComponent target, SyntaxDocument sdoc,
            int dot, ActionEvent e) {
        if (sdoc == null) {
            return;
        }
        Token token = sdoc.getTokenAt(dot);
        String abbrev = "";
        if (token != null) {
            abbrev = token.getString(sdoc);
            target.select(token.start, token.end());
        }
        if (dlg == null) {
            dlg = new ComboCompletionDialog(target);
        }
        dlg.displayFor(abbrev, items);
    }

    public void setItemsURL(String value) {
        items = JarServiceProvider.readLines(value);
    }

    /**
     * Gets the items to display in the combo
     * @return
     */
    public List<String> getItems() {
        return items;
    }

    /**
     * Sets the items to display in the combo.
     * @param items
     */
    public void setItems(List<String> items) {
        this.items = items;
    }
}
