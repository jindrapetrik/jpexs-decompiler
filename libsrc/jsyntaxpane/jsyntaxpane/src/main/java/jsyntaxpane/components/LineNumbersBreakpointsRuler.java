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
package jsyntaxpane.components;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import jsyntaxpane.actions.ActionUtils;

/**
 *
 * @author JPEXS
 */
public class LineNumbersBreakpointsRuler extends LineNumbersRuler {

    @Override
    public void install(final JEditorPane editor) {
        super.install(editor);
        removeMouseListener(mouseListener);
        mouseListener = new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                Point p = e.getPoint();
                p.x = 0;
                int loc = editor.viewToModel(p);

                int currentLine = -1;
                try {
                    currentLine = ActionUtils.getLineNumber(editor, loc) + 1;
                } catch (BadLocationException ex) {
                    //ignore
                }

                if (currentLine > -1 && (editor instanceof BreakPointListener)) {
                    ((BreakPointListener) editor).toggled(currentLine);
                }
            }

        };
        addMouseListener(mouseListener);
    }

}
