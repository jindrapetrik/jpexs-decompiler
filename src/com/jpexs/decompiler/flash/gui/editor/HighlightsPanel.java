/*
 * Copyright (C) 2025 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.gui.editor;

import com.jpexs.decompiler.flash.gui.AppStrings;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.plaf.ScrollBarUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import jsyntaxpane.actions.ActionUtils;
import jsyntaxpane.components.Markers;

/**
 *
 * @author JPEXS
 */
public class HighlightsPanel extends JPanel {

    private final LineMarkedEditorPane editorPane;
    private Cursor HAND_CURSOR = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    private Cursor DEFAULT_CURSOR = Cursor.getDefaultCursor();
    private Map<Integer, String> errors = new LinkedHashMap<>();
    private int scrollBarButtonSize = 0;
    private JScrollPane scrollPane;

    public HighlightsPanel(LineMarkedEditorPane editorPane) {
        this.editorPane = editorPane;

        scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, editorPane);

        JScrollBar bar = new JScrollBar(JScrollBar.VERTICAL, 0, 1, 0, 100);
        scrollBarButtonSize = bar.getPreferredSize().width;
        setPreferredSize(new Dimension(16, Integer.MAX_VALUE));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e)) {
                    return;
                }
                int totalLineCount = ActionUtils.getLineCount(editorPane);
                Rectangle r = getScrollbarTrackRect();
                int line = (e.getY() - r.y) * totalLineCount / r.height;
                int linesCountPer1Px = totalLineCount / r.height;
                if (!scrollPane.getVerticalScrollBar().isVisible()) {
                    linesCountPer1Px = 0;
                }

                for (Highlighter.Highlight highlight : editorPane.getHighlighter().getHighlights()) {
                    Highlighter.HighlightPainter painter = highlight.getPainter();
                    if (painter instanceof Markers.SimpleMarker) {
                        try {
                            int lineNum = ActionUtils.getLineNumber(editorPane, highlight.getStartOffset());
                            if (lineNum >= line - linesCountPer1Px && lineNum <= line + linesCountPer1Px) {
                                editorPane.setCaretPosition(highlight.getStartOffset());
                                return;
                            }
                        } catch (BadLocationException ex) {
                            //ignore
                        }
                    }
                }
            }
        });
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int totalLineCount = ActionUtils.getLineCount(editorPane);
                Rectangle r = getScrollbarTrackRect();
                int line = (e.getY() - r.y) * totalLineCount / r.height;

                int linesCountPer1Px = totalLineCount / r.height;
                if (!scrollPane.getVerticalScrollBar().isVisible()) {
                    linesCountPer1Px = 0;
                }

                int lineStart = line * r.height / totalLineCount;
                int lineEnd = (line + 1) * r.height / totalLineCount;
                if (e.getY() - r.y < lineStart - 1 || e.getY() - r.y > lineStart + 2) {
                    if (getCursor() != DEFAULT_CURSOR) {
                        setCursor(DEFAULT_CURSOR);
                    }

                    setToolTipText(null);
                    return;
                }

                int currentLine = editorPane.getLine();

                for (Highlighter.Highlight highlight : editorPane.getHighlighter().getHighlights()) {
                    Highlighter.HighlightPainter painter = highlight.getPainter();
                    if (painter instanceof Markers.SimpleMarker) {
                        try {
                            int lineNum = ActionUtils.getLineNumber(editorPane, highlight.getStartOffset());
                            if (lineNum >= line - linesCountPer1Px && lineNum <= line + linesCountPer1Px) {
                                if (line != currentLine) {
                                    if (getCursor() != HAND_CURSOR) {
                                        setCursor(HAND_CURSOR);
                                    }
                                }
                                if (errors.containsKey(highlight.getStartOffset())) {
                                    setToolTipText(AppStrings.translate("highlighter.error").replace("%error%", errors.get(highlight.getStartOffset())));
                                    return;
                                }
                                if (line != currentLine && painter instanceof OccurencesMarker) {
                                    setToolTipText(AppStrings.translate("highlighter.occurences"));
                                    return;
                                }

                                if (line == currentLine) {
                                    setToolTipText(AppStrings.translate("highlighter.currentLine"));
                                }
                                return;
                            }
                        } catch (BadLocationException ex) {
                            //ignore
                        }
                    }
                }

                if (getCursor() != DEFAULT_CURSOR) {
                    setCursor(DEFAULT_CURSOR);
                }

                setToolTipText(null);
            }
        });
    }

    public void setErrors(Map<Integer, String> errors) {
        this.errors = errors;
    }

    private Rectangle getScrollbarTrackRect() {
        return new Rectangle(0, scrollBarButtonSize, 16, getHeight() + - 2 * scrollBarButtonSize);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Rectangle r = getScrollbarTrackRect();

        int totalLineCount = ActionUtils.getLineCount(editorPane);
        int h = 3;
        Highlighter.Highlight[] highlights = editorPane.getHighlighter().getHighlights();
        Set<Integer> ignoredLines = new HashSet<>();
        int currentLine = editorPane.getLine();

        for (Highlighter.Highlight highlight : highlights) {
            Highlighter.HighlightPainter painter = highlight.getPainter();
            if (painter instanceof Markers.SimpleMarker) {
                Markers.SimpleMarker simpleMarker = (Markers.SimpleMarker) painter;
                g.setColor(simpleMarker.getColor());

                try {
                    int line = ActionUtils.getLineNumber(editorPane, highlight.getStartOffset());

                    //prefer error lines
                    if (painter instanceof WavyUnderLinePainter) {
                        ignoredLines.add(line);
                    } else if (ignoredLines.contains(line)) {
                        continue;
                    }
                    float ratio = (float) line / totalLineCount;
                    int y = r.y + (int) (ratio * r.height);
                    g.fillRect(0, y, getWidth(), h);
                } catch (BadLocationException ex) {
                    //ignore
                }
            }
        }

        if (currentLine >= 0) {
            float ratio = (float) currentLine / totalLineCount;
            int y = r.y + (int) (ratio * r.height) + 1;
            g.setColor(Color.gray);
            g.fillRect(0, y, getWidth(), 1);
            g.fillOval(16 / 2 - 2, y - 1, 3, 3);
        }
    }
}
