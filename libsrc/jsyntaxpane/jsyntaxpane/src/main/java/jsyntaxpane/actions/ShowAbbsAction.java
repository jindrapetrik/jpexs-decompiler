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
import java.util.Map;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.text.JTextComponent;
import jsyntaxpane.DefaultSyntaxKit;
import jsyntaxpane.SyntaxDocument;
import jsyntaxpane.actions.gui.ShowAbbsDialog;

/**
 * Display all abbreviations for a JTextComponent., if it has any.
 * Abbreviations are obtained from the IndentAction, so if the target does not
 * have an instance of that actions, nothing is displayed.
 * @author Ayman Al-Sairafi
 */
public class ShowAbbsAction extends DefaultSyntaxAction {

	public ShowAbbsAction() {
		super("show-abbreviations");
	}

	@Override
	public void actionPerformed(JTextComponent target, SyntaxDocument sDoc,
		int dot, ActionEvent e) {
		// find the abbreviations actions:
		DefaultSyntaxKit kit = ActionUtils.getSyntaxKit(target);
		if (kit != null) {
			Map<String, String> abbs = kit.getAbbreviations();
			if (abbs == null || abbs.isEmpty()) {
				JOptionPane.showMessageDialog(target,
					java.util.ResourceBundle.getBundle("jsyntaxpane/Bundle").getString("ShowAbbsAction.NoAbbsForType"));
			} else {
				new ShowAbbsDialog((JEditorPane) target, abbs);
			}
		}
	}
}
