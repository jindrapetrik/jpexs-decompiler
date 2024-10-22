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

import java.awt.Component;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import jsyntaxpane.SyntaxDocument;
import jsyntaxpane.actions.gui.QuickFindDialog;
import jsyntaxpane.actions.gui.ReplaceDialog;

/**
 * Data that is shared by Find / Replace and Find Next actions for a Document
 * The data here will be added as a property of the Document using the key
 * PROPERTY_KEY.  Only through the getFtmEditor can you crate a new instance.
 *
 * The class is responsible for handling the doFind and doReplace all actions.
 *
 * The class is also responsible for displaying the Find / Replace dialog
 *
 * @author Ayman Al-Sairafi
 */
public class DocumentSearchData {

	private static final String PROPERTY_KEY = "SearchData";
	private Pattern pattern = null;
	private boolean wrap = true;
	private ReplaceDialog replaceDlg;
	private QuickFindDialog quickFindDlg;

	/**
	 * This prevent creating a new instance.  You must call the getFromEditor
	 * to crate a new instance attached to a Document
	 *
	 */
	private DocumentSearchData() {
	}

	public Pattern getPattern() {
		return pattern;
	}

	/**
	 * Set the pattern to the given compiled pattern.
	 * @see this#setPattern(String, boolean, boolean)
	 * @param pattern
	 */
	public void setPattern(Pattern pattern) {
		this.pattern = pattern;
	}

	/**
	 * Sets the pattern from a string and flags
	 * @param pat String of pattern
	 * @param regex true if the pattern should be a regexp
	 * @param ignoreCase true to ignore case
	 * @throws java.util.regex.PatternSyntaxException
	 */
	public void setPattern(String pat, boolean regex, boolean ignoreCase)
		throws PatternSyntaxException {
		if (pat != null && pat.length() > 0) {
			int flag = (regex) ? 0 : Pattern.LITERAL;
			flag |= (ignoreCase) ? Pattern.CASE_INSENSITIVE : 0;
			setPattern(Pattern.compile(pat, flag));
		} else {
			setPattern(null);
		}
	}

	public boolean isWrap() {
		return wrap;
	}

	public void setWrap(boolean wrap) {
		this.wrap = wrap;
	}

	/**
	 * Get the Search data from a Document.  If document does not have any
	 * search data, then a new instance is added, put and returned.
	 * @param target JTextCOmponent we are attaching to
	 * @return
	 */
	public static DocumentSearchData getFromEditor(JTextComponent target) {
		if (target == null) {
			return null;
		}
		Object o = target.getDocument().getProperty(PROPERTY_KEY);
		if (o instanceof DocumentSearchData) {
			DocumentSearchData documentSearchData = (DocumentSearchData) o;
			return documentSearchData;
		} else {
			DocumentSearchData newDSD = new DocumentSearchData();
			target.getDocument().putProperty(PROPERTY_KEY, newDSD);
			return newDSD;
		}
	}

	/**
	 * Perform a replace all operation on the given component.
	 * Note that this create a new duplicate String big as the entire
	 * document and then assign it to the target text component
	 * @param target
	 * @param replacement
	 */
	public void doReplaceAll(JTextComponent target, String replacement) {
		if (replacement == null) {
			replacement = "";
		}
		SyntaxDocument sDoc = ActionUtils.getSyntaxDocument(target);
		if (sDoc == null) {
			return;
		}
		if (getPattern() == null) {
			return;
		}
		Matcher matcher = sDoc.getMatcher(getPattern());
		String newText = matcher.replaceAll(replacement);
		try {
			sDoc.replace(0, sDoc.getLength(), newText, null);
		} catch (BadLocationException ex) {
			Logger.getLogger(DocumentSearchData.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Replace single occurrence of match with the replacement.
	 * @param target
	 * @param replacement
	 */
	public void doReplace(JTextComponent target, String replacement) {
		if (target.getSelectedText() != null) {
			target.replaceSelection(replacement == null ? "" : replacement);
			doFindNext(target);
		}
	}

	/**
	 * FInd the previous match
	 * @param target
	 * @return
	 */
	public boolean doFindPrev(JTextComponent target) {
		if (getPattern() == null) {
			return false;
		}
		SyntaxDocument sDoc = ActionUtils.getSyntaxDocument(target);
		if (sDoc == null) {
			return false;
		}
		int dot = target.getSelectionStart();
		Matcher matcher = sDoc.getMatcher(getPattern());
		if (matcher == null) {
			return false;
		}
		// we have no way of jumping to last match, so we need to
		// go throw all matches, and stop when we reach current pos
		int start = -1;
		int end = -1;
		while (matcher.find()) {
			if (matcher.end() >= dot) {
				break;
			}
			start = matcher.start();
			end = matcher.end();
		}
		if (end > 0) {
			target.select(start, end);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Perform a FindNext operation on the given text component.  Position
	 * the caret at the start of the next found pattern.
	 * @param target
	 * @return true if pattern is found, false otherwise
	 */
	public boolean doFindNext(JTextComponent target) {
		if (getPattern() == null) {
			return false;
		}
		SyntaxDocument sDoc = ActionUtils.getSyntaxDocument(target);
		if (sDoc == null) {
			return false;
		}
		int start = target.getSelectionEnd();
		if (target.getSelectionEnd() == target.getSelectionStart()) {
			// we must advance the position by one, otherwise we will find
			// the same text again
			start++;
		}
		if (start >= sDoc.getLength()) {
			start = sDoc.getLength();
		}
		Matcher matcher = sDoc.getMatcher(getPattern(), start);
		if (matcher != null && matcher.find()) {
			// since we used an offset in the matcher, the matcher location
			// MUST be offset by that location
			target.select(matcher.start() + start, matcher.end() + start);
			return true;
		} else {
			if (isWrap()) {
				matcher = sDoc.getMatcher(getPattern());
				if (matcher != null && matcher.find()) {
					target.select(matcher.start(), matcher.end());
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
	}

	/**
	 * Display an OptionPane dialog that the search string is not found
	 * @param target
	 */
	public void msgNotFound(Component target) {
		JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(target),
			MessageFormat.format(java.util.ResourceBundle.getBundle("jsyntaxpane/Bundle").getString("DocumentSearchData.SearchStringNotFound"), getPattern()),
			java.util.ResourceBundle.getBundle("jsyntaxpane/Bundle").getString("DocumentSearchData.Find"), JOptionPane.INFORMATION_MESSAGE);
	}

        public void showQuickFindDialogEx(JTextComponent target, boolean ignoreCase, boolean regularExpresion) {
		if (quickFindDlg == null) {
			quickFindDlg = new QuickFindDialog(target, this);
		}
                quickFindDlg.setIgnoreCase(ignoreCase);
                quickFindDlg.setRegularExpression(regularExpresion);
		quickFindDlg.showFor(target);
	}
        
	/**
	 * Show the Find and Replace dialog for the given frame
	 * @param target
	 */
	public void showReplaceDialog(JTextComponent target) {
		if (replaceDlg == null) {
			replaceDlg = new ReplaceDialog(target, this);
		}
		replaceDlg.setVisible(true);
	}

	public void showQuickFindDialog(JTextComponent target) {
		if (quickFindDlg == null) {
			quickFindDlg = new QuickFindDialog(target, this);
		}
		quickFindDlg.showFor(target);
	}
}
