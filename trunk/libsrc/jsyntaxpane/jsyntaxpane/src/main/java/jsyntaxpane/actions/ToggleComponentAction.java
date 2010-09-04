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
import javax.swing.JEditorPane;
import javax.swing.text.JTextComponent;
import jsyntaxpane.DefaultSyntaxKit;

/**
 * This Action will Toggle any SyntaxComponents on the EditorPane
 * You need the configuration Key prefix.ACTION_NAME.Component = componentclassname
 * Where:
 * ACTION_NAME is the name given to the action (prefix.Action.ACTION_NAME)
 * componentclassname is the fully qualified class name of the component
 * @author Ayman Al-Sairafi
 */
public class ToggleComponentAction extends DefaultSyntaxAction {

    private String componentName;

    public ToggleComponentAction() {
        super("toggle-component");
        putValue(SELECTED_KEY, Boolean.TRUE);
    }

    public void setComponent(String name) {
        componentName = name;
    }
    
    @Override
    public String toString() {
        return super.toString() + "(" + componentName + ")";
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JTextComponent target = getTextComponent(e);
        if (target instanceof JEditorPane) {
            JEditorPane jEditorPane = (JEditorPane) target;
            DefaultSyntaxKit kit = (DefaultSyntaxKit) jEditorPane.getEditorKit();
            boolean status = kit.toggleComponent(jEditorPane, componentName);
            putValue(SELECTED_KEY, status);
        }
    }
}
