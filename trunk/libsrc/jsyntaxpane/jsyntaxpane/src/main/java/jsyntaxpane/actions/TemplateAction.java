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
 * This action replaces the selection with the configured template in
 * the config ACTION-NAME.Template
 *
 * There are two kinds of templates:
 * <li>Simple Templates are replaced as is</li>
 * <li>Whole Line Templates will ensure a whole line is selected.
 * Each line in the selection will be prefixed, and postfixed with whatever appears
 * on the line in the template</li>
 *
 */
public class TemplateAction extends DefaultSyntaxAction {

    private String template;
    private String[] tlines = null;
    private boolean wholeLines;
    private boolean mustHaveSelection;

    public TemplateAction() {
        super("template");
    }

    @Override
    public void actionPerformed(JTextComponent target, SyntaxDocument sdoc,
            int dot, ActionEvent e) {
        if (mustHaveSelection) {
            if (target.getSelectionEnd() == target.getSelectionStart()) {
                return;
            }
        }
        if (wholeLines) {
            if(tlines == null) {
                tlines = template.split("\n");
            }
            ActionUtils.insertLinesTemplate(target, tlines);
        } else {
            ActionUtils.insertSimpleTemplate(target, template);
        }
    }

    public void setWholeLines(String value) {
        wholeLines = Boolean.parseBoolean(value);
    }

    public void setTemplate(String t) {
        template = t;
    }

    public void setMustHaveSelection(String value) {
        mustHaveSelection = Boolean.parseBoolean(value);
    }
}