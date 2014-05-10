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
import jsyntaxpane.Token;
import jsyntaxpane.TokenType;

/**
 * This class should be mapped to VK_ENTER.  It performs proper indentation
 * for Java Type languages and automatically inserts "*" in multi-line comments
 * Initial Code contributed by ser... AT mail.ru
 *
 * @author Ayman Al-Sairafi
 */
public class JIndentAction extends DefaultSyntaxAction {

	public JIndentAction() {
		super("JINDENT");
	}

	/**
	 * {@inheritDoc}
	 * @param e
	 */
	@Override
	public void actionPerformed(JTextComponent target, SyntaxDocument sDoc,
		int dot, ActionEvent e) {
		int pos = target.getCaretPosition();
		int start = sDoc.getParagraphElement(pos).getStartOffset();
		String line = ActionUtils.getLine(target);
		String lineToPos = line.substring(0, pos - start);
		String prefix = ActionUtils.getIndent(line);
		Token t = sDoc.getTokenAt(pos);
		if (TokenType.isComment(t)) {
			String trimmed = line.trim();
			if (trimmed.startsWith("/*") && trimmed.endsWith("*/")) {
				// it's a single line comment, do not do anything special
			} else if (trimmed.endsWith("*/")) {
				try {
					// the prefix should be the line where the comment started
					String commentStartLine = sDoc.getLineAt(t.start);
					prefix = ActionUtils.getIndent(commentStartLine);
				} catch (BadLocationException ex) {
					Logger.getLogger(JIndentAction.class.getName()).log(Level.SEVERE, null, ex);
				}
			} else if (trimmed.startsWith("*")) {
				prefix += "* ";
			} else if (trimmed.startsWith("/**")) {
				prefix += " * ";
			} else if (trimmed.startsWith("/*")) {
				prefix += " ";
			}
		} else if (lineToPos.trim().endsWith("{")) {
			prefix += ActionUtils.getTab(target);
		} else {
			String noComment = sDoc.getUncommentedText(start, pos); // skip EOL comments

			if (noComment.trim().endsWith("{")) {
				prefix += ActionUtils.getTab(target);
			}
		}
		target.replaceSelection("\n" + prefix);
	}
}
