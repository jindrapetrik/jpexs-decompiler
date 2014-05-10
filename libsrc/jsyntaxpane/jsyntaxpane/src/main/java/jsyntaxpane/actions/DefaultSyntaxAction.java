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
import java.net.URL;
import java.util.regex.Pattern;
import javax.swing.ImageIcon;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import jsyntaxpane.SyntaxDocument;
import jsyntaxpane.util.Configuration;
import jsyntaxpane.util.ReflectUtils;

/**
 * The DefaultSyntaxAction.  You can extend this class or implement the interface
 * SyntaxAction to create your own actions.
 * 
 * @author Ayman Al-Sairafi
 */
abstract public class DefaultSyntaxAction extends TextAction implements SyntaxAction {

	public DefaultSyntaxAction(String actionName) {
		super(actionName);
		putValue(NAME, actionName);
	}

	@Override
	public void config(Configuration config, String name) {
		// find setter methods for each property key:
		String actionName = name.substring(ACTION_PREFIX.length());
		for (Configuration.StringKeyMatcher m : config.getKeys(
			Pattern.compile(Pattern.quote(name) + "\\.((\\w|-)+)"))) {
			if (!ReflectUtils.callSetter(this, m.group1, m.value)) {
				putValue(m.group1, m.value);
			}
		}
		// if we did not put a name, use the action name
		if (getValue(NAME) == null) {
			putValue(NAME, actionName);
		}
		// if we did not put an icon, try and find one using our name
		if (getValue(SMALL_ICON) == null) {
			setSmallIcon(actionName + ".png");
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JTextComponent text = getTextComponent(e);
		SyntaxDocument sdoc = ActionUtils.getSyntaxDocument(text);
		if (text != null) {
			actionPerformed(text, sdoc, text.getCaretPosition(), e);
		}
	}

	/**
	 * Convenience method that will be called if the Action is performed on a
	 * JTextComponent.  SyntaxActions should generally override this method.
	 * @param target (non-null JTextComponent from Action.getSource
	 * @param sDoc (SyntaxDOcument of the text component, could be null)
	 * @param dot (position of caret at text document)
	 * @param e actual ActionEvent passed to actionPerformed
	 */
	public void actionPerformed(JTextComponent target, SyntaxDocument sDoc,
		int dot, ActionEvent e) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public String toString() {
		return "Action " + getValue(NAME) + "of type " + this.getClass().getSimpleName();
	}

	/**
	 * Configure the MenuText for the Action
	 * @param text
	 */
	public final void setMenuText(String text) {
		putValue(NAME, text);
		// also set the SHORT_DESCRIPTIOn if it was not set, so we have
		// at least some tooltip for toolbar buttons
		if (getValue(SHORT_DESCRIPTION) == null) {
			putValue(SHORT_DESCRIPTION, text);
		}
	}

	/**
	 * Configure the ToolTip for the Action
	 * @param text
	 */
	public final void setToolTip(String text) {
		putValue(SHORT_DESCRIPTION, text);
	}

	/**
	 * Sets the Large Icon for this action from given url
	 *
	 * @param url
	 */
	public final void setLargeIcon(String url) {
		URL loc = this.getClass().getResource(LARGE_ICONS_LOC_PREFIX + url);
		if (loc != null) {
			ImageIcon i = new ImageIcon(loc);
			putValue(LARGE_ICON_KEY, i);
		}
	}

	/**
	 * Configure the SmallIcon for the Action
	 * @param url
	 */
	public final void setSmallIcon(String url) {
		URL loc = this.getClass().getResource(SMALL_ICONS_LOC_PREFIX + url);
		if (loc != null) {
			ImageIcon i = new ImageIcon(loc);
			putValue(SMALL_ICON, i);
		}
	}
	public static final String ACTION_PREFIX = "Action.";
	public static final String SMALL_ICONS_LOC_PREFIX = "/META-INF/images/small-icons/";
	public static final String LARGE_ICONS_LOC_PREFIX = "/META-INF/images/large-icons/";
}
