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
import java.text.MessageFormat;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import jsyntaxpane.SyntaxDocument;

/**
 * Executes the script in the component's text using a ScriptEngine
 * The Configuration must contain the key [prefix.]ACTION_NAME.ScriptExtension
 * and its value is the ScriptExtension that getEngineByExtension returns
 * If no engine is found, then an option is given to the user to disable the action
 * 
 * @author Ayman Al-Sairafi
 */
public class ScriptRunnerAction extends DefaultSyntaxAction {

    public ScriptRunnerAction() {
        super("SCRIPT_EXECUTE");
    }

    @Override
    public void actionPerformed(JTextComponent target, SyntaxDocument sDoc,
            int dot, ActionEvent e) {
        try {
            ScriptEngine eng = getEngine(target);
            if (eng != null) {
                getEngine(target).eval(target.getText());
            }
        } catch (ScriptException ex) {
            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(target),
                    java.util.ResourceBundle.getBundle("jsyntaxpane/Bundle").getString("ScriptRunnerAction.ErrorExecutingScript") + ex.getMessage(),
                    java.util.ResourceBundle.getBundle("jsyntaxpane/Bundle").getString("ScriptRunnerAction.ScriptError"),
                    JOptionPane.ERROR_MESSAGE);
            ActionUtils.setCaretPosition(target,
                    ex.getLineNumber(),
                    ex.getColumnNumber());
        }
    }

    private ScriptEngine getEngine(JTextComponent target) {
        if (engine == null) {
            if (sem == null) {
                sem = new ScriptEngineManager();
            }
            engine = sem.getEngineByExtension(scriptExtension);
        }
        if (engine == null) {
            int result = JOptionPane.showOptionDialog(target,
                    MessageFormat.format(java.util.ResourceBundle.getBundle("jsyntaxpane/Bundle").getString("ScriptRunnerAction.ScriptEngineNotFound"), scriptExtension),
                    "jsyntaxpane",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.ERROR_MESSAGE,
                    null,
                    null,
                    null);
            if (result == JOptionPane.YES_OPTION) {
                setEnabled(false);
            }
        }
        return engine;
    }

    public void setScriptExtension(String value) {
        scriptExtension = value;
    }

    static ScriptEngineManager sem;
    private ScriptEngine engine;
    private String scriptExtension;
}
