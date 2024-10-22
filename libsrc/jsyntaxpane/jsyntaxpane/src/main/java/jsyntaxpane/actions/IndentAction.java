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
import java.util.regex.Pattern;
import javax.swing.JEditorPane;
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;
import jsyntaxpane.DefaultSyntaxKit;
import jsyntaxpane.SyntaxDocument;
import jsyntaxpane.Token;

/**
 * IndentAction is used to replace Tabs with spaces.  If there is selected
 * text, then the lines spanning the selection will be shifted
 * right by one tab-width space character.
 *
 * Since this is also used as an abbreviation completion action,
 * Abbreviations are processed by this event.
 *
 * FIXME:  Move the abbreviation expansion to an ActionUtils proc
 * @author Ayman Al-Sairafi
 * 
 */
public class IndentAction extends DefaultSyntaxAction {

	public IndentAction() {
		super("insert-tab");
	}

	@Override
	public void actionPerformed(JTextComponent target, SyntaxDocument sDoc,
		int dot, ActionEvent e) {
		String selected = target.getSelectedText();
		EditorKit kit = ((JEditorPane) target).getEditorKit();
		Map<String, String> abbrvs = ((DefaultSyntaxKit) kit).getAbbreviations();
		if (selected == null) {
			// Check for abbreviations:
			Token abbrToken = sDoc.getWordAt(dot, wordsPattern);
			Integer tabStop = ActionUtils.getTabSize(target);
			int lineStart = sDoc.getParagraphElement(dot).getStartOffset();
			int column = dot - lineStart;
			int needed = tabStop - (column % tabStop);
			if (abbrvs == null || abbrToken == null) {
                                if (ActionUtils.usesTabs(target)) { //JPEXS
                                    target.replaceSelection("\t");
                                } else {
                                    target.replaceSelection(ActionUtils.SPACES.substring(0, needed));
                                }
			} else {
				String abbr = abbrToken.getString(sDoc);
				if (abbrvs.containsKey(abbr)) {
					target.select(abbrToken.start, abbrToken.end());
					abbr = abbrvs.get(abbr);
					String[] abbrLines = abbr.split("\n");
					if (abbrLines.length > 1) {
						ActionUtils.insertLinesTemplate(target, abbrLines);
					} else {
						ActionUtils.insertSimpleTemplate(target, abbr);
					}
				} else {
                                        if (ActionUtils.usesTabs(target)) { //JPEXS
                                            target.replaceSelection("\t");
                                        } else {
                                            target.replaceSelection(ActionUtils.SPACES.substring(0, needed));
                                        }
				}
			}
		} else {
			String[] lines = ActionUtils.getSelectedLines(target);
			int start = target.getSelectionStart();
			StringBuilder sb = new StringBuilder();
			for (String line : lines) {
				sb.append(ActionUtils.getTab(target));
				sb.append(line);
				sb.append('\n');
			}
			target.replaceSelection(sb.toString());
			target.select(start, start + sb.length());
		}
	}
	private Pattern wordsPattern = Pattern.compile("\\w+");

	public void setWordRegex(String regex) {
		wordsPattern = Pattern.compile(regex);
	}

	public Pattern getWordRegex() {
		return wordsPattern;
	}
}
