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
import jsyntaxpane.actions.gui.HTMLPreviewFrame;

/**
 * Show an HTML Preview window.
 * This will automatically update on changes to the underlying document.
 * 
 */
public class HTMLPreviewAction extends DefaultSyntaxAction {
	public static final String HTML_PREVIEW_WINDOW = "html-preview-window";

    public HTMLPreviewAction() {
        super("HTML_PREVIEW");
    }

    @Override
    public void actionPerformed(JTextComponent target, SyntaxDocument sDoc,
            int dot, ActionEvent e) {
        // have the document property
        Object obj = sDoc.getProperty(HTML_PREVIEW_WINDOW);
        if(obj == null) {
            HTMLPreviewFrame dlg = new HTMLPreviewFrame(sDoc);
            sDoc.putProperty( HTML_PREVIEW_WINDOW,dlg);
            dlg.setVisible(true);
        } else {
            HTMLPreviewFrame dlg = (HTMLPreviewFrame) obj;
            dlg.setVisible(enabled);
        }
    }
}
