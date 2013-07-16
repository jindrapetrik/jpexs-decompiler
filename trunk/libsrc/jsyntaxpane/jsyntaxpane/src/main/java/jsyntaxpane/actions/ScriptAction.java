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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.regex.Pattern;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.JOptionPane;
import javax.swing.text.JTextComponent;
import jsyntaxpane.SyntaxDocument;
import jsyntaxpane.util.Configuration;
import jsyntaxpane.util.JarServiceProvider;

/**
 * This class executes a script every time it is called.
 * Anything can be done using any script.
 *
 * @author Ayman Al-Sairafi
 */
public class ScriptAction extends DefaultSyntaxAction {

	public ScriptAction() {
		super("scripted-action");
	}

	@Override
	public void actionPerformed(JTextComponent target, SyntaxDocument sDoc,
		int dot, ActionEvent e) {
		if (getValue(SCRIPT_FUNCTION) != null) {
			String f = getValue(SCRIPT_FUNCTION).toString();
			try {
				engine.put("TARGET", target);
				engine.put("SDOC", sDoc);
				engine.put("DOT", dot);
				engine.put("EVENT", e);
				engine.put("ACTION", this);
				engine.put("AU", ActionUtils.getInstance());
				invocable.invokeFunction(f);
			} catch (ScriptException ex) {
				showScriptError(target, ex);
			} catch (NoSuchMethodException ex) {
				showScriptError(target, ex);
			}
		} else {
			JOptionPane.showMessageDialog(target, java.util.ResourceBundle.getBundle("jsyntaxpane/Bundle").getString("ScriptAction.NoScriptConfigured"),
				java.util.ResourceBundle.getBundle("jsyntaxpane/Bundle").getString("ScriptAction.ErrorInScript"), JOptionPane.WARNING_MESSAGE);
		}
	}

	public void setFunction(String name) {
		putValue(SCRIPT_FUNCTION, name);
	}

	@Override
	public void config(Configuration config, String name) {
		super.config(config, name);
		// now read and store all of our scripts.
		for (Configuration.StringKeyMatcher m : config.getKeys(Pattern.compile("Script\\.((\\w|-)+)\\.URL"))) {
			getScriptFromURL(m.value);
		}
	}

	/**
	 * 
	 * @param url
	 */
	public void getScriptFromURL(String url) {
		InputStream is = JarServiceProvider.findResource(url, this.getClass().getClassLoader());
		if (is != null) {
			Reader reader = new InputStreamReader(is);
			try {
				engine.eval(reader);
			} catch (ScriptException ex) {
				showScriptError(null, ex);
			}
		} else {
			JOptionPane.showMessageDialog(null, java.util.ResourceBundle.getBundle("jsyntaxpane/Bundle").getString("ScriptAction.NoScriptFoundIn") + url,
				java.util.ResourceBundle.getBundle("jsyntaxpane/Bundle").getString("ScriptAction.ErrorInScript"), JOptionPane.WARNING_MESSAGE);
		}
	}

	private void showScriptError(JTextComponent target, Exception ex) {
		JOptionPane.showMessageDialog(target, ex.getMessage(),
			java.util.ResourceBundle.getBundle("jsyntaxpane/Bundle").getString("ScriptAction.ErrorInScript"), JOptionPane.WARNING_MESSAGE);
	}
	/**
	 * The key used to store the Script Name for the this action
	 */
	static final String SCRIPT_FUNCTION = "SCRIPT_FUNCTION";
	static final ScriptEngine engine;
	static final Invocable invocable;


	static {
		engine = new ScriptEngineManager().getEngineByExtension("js");
		invocable = (Invocable) engine;
	}
}
