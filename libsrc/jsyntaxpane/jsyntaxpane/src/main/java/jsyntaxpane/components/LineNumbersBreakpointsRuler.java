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

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import jsyntaxpane.SyntaxView;
import jsyntaxpane.actions.ActionUtils;

/**
 *
 * @author JPEXS
 */
public class LineNumbersBreakpointsRuler extends LineNumbersRuler {

    @Override
    public void install(final JEditorPane editor) {
        super.install(editor);
        if (editor instanceof LineMarkerPainter) {
            ((LineMarkerPainter) editor).installLineMarker(this);
        }
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

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (editor instanceof LineMarkerPainter) {
            FontMetrics fontMetrics = editor.getFontMetrics(editor.getFont());
            int lh = fontMetrics.getHeight();
            Rectangle bounds = g.getClipBounds();
            int minY = bounds.y;
            int maxY = minY + bounds.height;
            int maxLines = ActionUtils.getLineCount(editor);
            Insets insets = getInsets();

            int currentLine = -1;
            try {
                // get current line, and add one as we start from 1 for the display
                currentLine = ActionUtils.getLineNumber(editor, editor.getCaretPosition()) + 1;
            } catch (BadLocationException ex) {
                // this wont happen, even if it does, we can ignore it and we will not have
                // a current line to worry about...
            }

            for (int line = 1; line <= maxLines; line++) {
                int y = line * lh;
                if (y < minY) {
                    continue;
                }
                if (y - lh > maxY) {
                    break;
                }
                ((LineMarkerPainter) editor).paintLineMarker(g, line, insets.left, y - lh + fontMetrics.getDescent() - 1, y, lh, currentLine == line, maxLines);
            }
        }
    }

}
