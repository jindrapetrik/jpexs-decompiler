/*
 *  Copyright (C) 2010-2018 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.gui.editor;

import com.jpexs.helpers.Reference;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.gui.AppStrings;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.KeyEventPostProcessor;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.plaf.TextUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Highlighter.HighlightPainter;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.text.View;
import jsyntaxpane.SyntaxDocument;
import jsyntaxpane.SyntaxStyle;
import jsyntaxpane.Token;
import jsyntaxpane.actions.ActionUtils;

/**
 *
 * @author JPEXS
 */
public class LineMarkedEditorPane extends UndoFixedEditorPane implements LinkHandler {

    private static final int truncateLimit = 2 * 1024 * 1024;

    public static final Color BG_SELECTED_LINE = new Color(0xe9, 0xef, 0xf8);

    public static final Color BG_ERROR_LINE = new Color(255, 200, 200);

    private int lastLine = -1;

    private boolean error = false;

    private Token lastUnderlined = null;

    private static final HighlightPainter underLinePainter = new UnderLinePainter(new Color(0, 0, 255));

    private LinkHandler linkHandler = this;

    public static class LineMarker implements Comparable<LineMarker> {

        private final Color bgColor;

        private final Color color;

        private FgPainter fgPainter;

        //private int line;
        private final int priority;

        public FgPainter getForegroundPainter() {
            return fgPainter;
        }

        @Override
        public String toString() {
            return bgColor.toString() + " priority:" + priority;
        }

        public LineMarker(Color color, Color bgColor, int priority) {
            this.bgColor = bgColor;
            this.color = color;
            this.priority = priority;
            if (color != null) {
                this.fgPainter = new FgPainter(color, bgColor);
            }
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 17 * hash + Objects.hashCode(this.bgColor);
            hash = 17 * hash + Objects.hashCode(this.color);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final LineMarker other = (LineMarker) obj;
            if (!Objects.equals(this.bgColor, other.bgColor)) {
                return false;
            }
            return (Objects.equals(this.color, other.color));
        }

        public Color getBgColor() {
            return bgColor;
        }

        public Color getColor() {
            return color;
        }

        @Override
        public int compareTo(LineMarker o) {
            return priority - o.priority;
        }
    }
//(Map<Integer, TreeSet<LineMarker>>)

    private Map<Integer, SortedSet<LineMarker>> lineMarkers = Collections.synchronizedMap(new HashMap<Integer, SortedSet<LineMarker>>());

    public void setLineMarkers(Map<Integer, SortedSet<LineMarker>> colorMarkers) {
        this.lineMarkers = colorMarkers;
    }

    public void clearLineColors() {
        lineMarkers.clear();
        repaint();
    }

    public boolean hasColorMarker(int line, LineMarker lm) {
        line -= firstLineOffset();
        if (lineMarkers.containsKey(line)) {
            return lineMarkers.get(line).contains(lm);
        }
        return false;
    }

    public void removeColorMarker(int line, LineMarker lm) {
        line -= firstLineOffset();
        if (lineMarkers.containsKey(line)) {
            lineMarkers.get(line).remove(lm);
        }
        getParent().repaint();
    }

    public void removeColorMarkerOnAllLines(LineMarker lm) {
        for (int line : lineMarkers.keySet()) {
            line += firstLineOffset();
            removeColorMarker(line, lm);
        }
    }

    public int firstLineOffset() {
        return 0;
    }

    public void toggleColorMarker(int line, LineMarker lm) {
        if (!lineMarkers.containsKey(line - firstLineOffset())) {
            addColorMarker(line, lm);
        } else if (lineMarkers.get(line - firstLineOffset()).contains(lm)) {
            removeColorMarker(line, lm);
        } else {
            addColorMarker(line, lm);
        }
        getParent().repaint();
    }

    public void addColorMarker(int line, LineMarker lm) {
        line -= firstLineOffset();
        if (!lineMarkers.containsKey(line)) {
            lineMarkers.put(line, Collections.synchronizedSortedSet(new TreeSet<>()));
        }
        lineMarkers.get(line).add(lm);
        getParent().repaint();
    }

    public int getLine() {
        int caretPosition = getCaretPosition();
        Element root = getDocument().getDefaultRootElement();
        int currentLine = root.getElementIndex(caretPosition);
        return currentLine;
    }

    public void markError() {
        error = true;
    }

    public void gotoLine(int line) {
        int pos = ActionUtils.getDocumentPosition(this, line, 0);
        if (pos != -1) {
            setCaretPosition(pos);
        }
    }

    public void gotoLineCol(int line, int column) {
        int pos = ActionUtils.getDocumentPosition(this, line, column);
        if (pos != -1) {
            setCaretPosition(pos);
        }
    }

    public Point getLineLocation(int line) {
        int pos = ActionUtils.getDocumentPosition(this, line + 1, 0);
        if (pos < 0) {
            return null;
        }
        try {
            Rectangle r = modelToView(pos);
            return new Point(r.x, r.y);
        } catch (BadLocationException ex) {
            return null;
        }
    }

    private void getLineBounds(int line, Reference<Integer> lineStart, Reference<Integer> lineEnd) {
        Document d = getDocument();
        String text = "";
        try {
            text = d.getText(0, d.getLength());
        } catch (BadLocationException ex) {
            //ignore
        }
        int lineCnt = 0;
        int lineStartVal = 0;
        int lineEndVal = -1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                lineCnt++;
                if (lineCnt == line) {
                    lineStartVal = i + 1;
                }
                if (lineCnt == line + 1) {
                    lineEndVal = i;
                }
            }
        }
        if (lineCnt == 0) {
            lineEndVal = text.length() - 1;
            if (line > 0) {
                lineStartVal = text.length() - 1;
            }
        }
        lineEnd.setVal(lineEndVal);
        lineStart.setVal(lineStartVal);
    }

    public void selectLine(int line) {
        Reference<Integer> lineStart = new Reference<>(0);
        Reference<Integer> lineEnd = new Reference<>(0);
        getLineBounds(line, lineStart, lineEnd);

        select(lineStart.getVal(), lineEnd.getVal());
        requestFocus();
    }

    public String getCurrentLineText() {
        return getLineText(getLine());
    }

    public String getLineText(int line) {
        Reference<Integer> lineStart = new Reference<>(0);
        Reference<Integer> lineEnd = new Reference<>(0);
        getLineBounds(line, lineStart, lineEnd);
        try {
            return getDocument().getText(lineStart.getVal(), lineEnd.getVal() - lineStart.getVal());
        } catch (BadLocationException ex) {
            return null;
        }
    }

    public LineMarkedEditorPane() {
        setOpaque(false);
        addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent e) {
                int caretPosition = getCaretPosition();
                Element root = getDocument().getDefaultRootElement();
                int currentLine = root.getElementIndex(caretPosition);
                if (currentLine != lastLine) {
                    lastLine = currentLine;
                    error = false;
                    repaint();
                }

            }
        });
        final LinkAdapter la = new LinkAdapter();
        addMouseMotionListener(la);
        addMouseListener(la);

        //No standard AddKeyListener as we want to catch Ctrl globally no matter of focus
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventPostProcessor(new KeyEventPostProcessor() {
                    @Override
                    public boolean postProcessKeyEvent(KeyEvent e) {
                        if (e.getID() == KeyEvent.KEY_PRESSED) {
                            la.keyPressed(e);
                        }
                        if (e.getID() == KeyEvent.KEY_RELEASED) {
                            la.keyReleased(e);
                        }
                        if (e.getID() == KeyEvent.KEY_TYPED) {
                            la.keyTyped(e);
                        }
                        return false;
                    }
                });

    }

    public Token tokenAtPos(Point lastPos) {
        Document d = getDocument();
        if (d instanceof SyntaxDocument) {
            SyntaxDocument sd = (SyntaxDocument) d;

            //correction of token last character
            int pos = viewToModel(lastPos);
            Rectangle r;
            try {
                r = modelToView(pos);
                if (r != null) {
                    if (lastPos.x < r.x) {
                        pos--;
                    }
                }
            } catch (BadLocationException ex) {
                //ignore
            }
            Token t = sd.getTokenAt(pos);

            //Correction of token of length 1 character
            if (pos > 0 && pos < d.getLength() - 1 && t != null && t.length == 1) {
                Token tprev = sd.getTokenAt(pos - 1);
                if (tprev == t) {
                    t = sd.getTokenAt(pos + 1);
                }
            }

            return t;
        }
        return null;
    }

    private class LinkAdapter extends MouseAdapter implements KeyListener {

        private Point lastPos = new Point(0, 0);

        private boolean ctrlDown = false;

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
                ctrlDown = true;
                update();
            }
        }

        @Override
        public void keyTyped(KeyEvent e) {

        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
                ctrlDown = false;
                update();
            }
        }

        private void update() {
            if (ctrlDown) {
                Token t = tokenAtPos(lastPos);

                if (t != lastUnderlined) {
                    if (t == null || lastUnderlined == null || !t.equals(lastUnderlined)) {
                        MyMarkers.removeMarkers(LineMarkedEditorPane.this, underLinePainter);

                        if (t != null && linkHandler.isLink(t)) {
                            lastUnderlined = t;
                            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                        } else {
                            lastUnderlined = null;
                        }
                    } else {
                        lastUnderlined = null;
                    }
                }

                if (lastUnderlined != null) {
                    MyMarkers.markToken(LineMarkedEditorPane.this, lastUnderlined, underLinePainter);
                } else {
                    setCursor(Cursor.getDefaultCursor());
                }
                repaint();
            } else {
                lastUnderlined = null;
                MyMarkers.removeMarkers(LineMarkedEditorPane.this, underLinePainter);
                setCursor(Cursor.getDefaultCursor());
                repaint();
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (ctrlDown) {
                Token t = tokenAtPos(lastPos);
                if (t != null && linkHandler.isLink(t)) {
                    linkHandler.handleLink(t);
                }
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {

            ctrlDown = (e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0;
            lastPos = e.getPoint();
            update();

        }
    }

    public void setLinkHandler(LinkHandler linkHandler) {
        if (linkHandler == null) {
            linkHandler = this;
        }
        this.linkHandler = linkHandler;
    }

    public LinkHandler getLinkHandler() {
        return linkHandler;
    }

    @Override
    public HighlightPainter linkPainter() {
        return underLinePainter;
    }

    @Override
    public boolean isLink(Token token) {
        return false;
    }

    @Override
    public void handleLink(Token token) {

    }

    @Override
    public void setText(String t) {
        this.lineMarkers = new HashMap<>();
        lastLine = -1;
        error = false;
        if (Configuration._debugMode.get() && t != null && t.length() > truncateLimit) {
            t = t.substring(0, truncateLimit) + "\r\n" + AppStrings.translate("editorTruncateWarning").replace("%chars%", Integer.toString(truncateLimit));
        }

        super.setText(t);
        setCaretPosition(0); //scroll to top
    }

    public static class FgPainter extends DefaultHighlighter.DefaultHighlightPainter {

        private final SyntaxStyle fgStyle;

        public FgPainter(Color color, Color bgColor) {
            super(bgColor);
            this.fgStyle = new SyntaxStyle(color, false, false);
        }

        @Override
        public void paint(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c) {
            try {
                // --- determine locations ---
                TextUI mapper = c.getUI();

                Segment seg = new Segment();
                ((SyntaxDocument) c.getDocument()).getText(offs0, offs1 - offs0, seg);

                Rectangle r = mapper.modelToView(c, offs0, Position.Bias.Forward);
                FontMetrics fm = g.getFontMetrics();
                //int fh = fm.getHeight();
                fgStyle.drawText(seg, r.x, r.y + fm.getAscent(), g, null, offs0);
                /*for (int i = offs0; i < offs1; i++) {

                 Rectangle r = mapper.modelToView(c, i, Position.Bias.Forward);
                 Rectangle r1 = mapper.modelToView(c, i + 1, Position.Bias.Forward);
                 if (r1.y == r.y) {
                 ((SyntaxDocument) c.getDocument()).getText(i, 1, seg);
                 fgStyle.drawText(seg, r.x, r.y, g, null, i);
                 //g.drawLine(r.x, r.y + r.height - 3, r1.x, r.y + r.height - 3);
                 }
                 }*/

            } catch (BadLocationException e) {
                // can't render
            }
        }

        @Override
        public Shape paintLayer(Graphics g, int offs0, int offs1,
                Shape bounds, JTextComponent c, View view) {

            g.setColor(c.getSelectionColor());

            Rectangle r;

            if (offs0 == view.getStartOffset()
                    && offs1 == view.getEndOffset()) {
                // Contained in view, can just use bounds.
                if (bounds instanceof Rectangle) {
                    r = (Rectangle) bounds;
                } else {
                    r = bounds.getBounds();
                }
            } else {
                // Should only render part of View.
                try {
                    // --- determine locations ---
                    Shape shape = view.modelToView(offs0, Position.Bias.Forward,
                            offs1, Position.Bias.Backward,
                            bounds);
                    r = (shape instanceof Rectangle)
                            ? (Rectangle) shape : shape.getBounds();
                } catch (BadLocationException e) {
                    // can't render
                    r = null;
                }
            }

            if (r != null) {
                r.width = Math.max(r.width, 1);

                paint(g, offs0, offs1, r, c);
            }

            return r;
        }
    }

    public static class UnderLinePainter extends DefaultHighlighter.DefaultHighlightPainter {

        public UnderLinePainter(Color color) {
            super(color);
        }

        @Override
        public void paint(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c) {
            try {
                // --- determine locations ---
                TextUI mapper = c.getUI();

                Color col = getColor();
                if (col == null) {
                    col = Color.black;
                }
                g.setColor(col);
                for (int i = offs0; i < offs1; i++) {

                    Rectangle r = mapper.modelToView(c, i, Position.Bias.Forward);
                    Rectangle r1 = mapper.modelToView(c, i + 1, Position.Bias.Forward);
                    if (r1.y == r.y) {
                        g.drawLine(r.x, r.y + r.height - 3, r1.x, r.y + r.height - 3);
                    }
                }

            } catch (BadLocationException e) {
                // can't render
            }
        }

        @Override
        public Shape paintLayer(Graphics g, int offs0, int offs1,
                Shape bounds, JTextComponent c, View view) {

            g.setColor(c.getSelectionColor());

            Rectangle r;

            if (offs0 == view.getStartOffset()
                    && offs1 == view.getEndOffset()) {
                // Contained in view, can just use bounds.
                if (bounds instanceof Rectangle) {
                    r = (Rectangle) bounds;
                } else {
                    r = bounds.getBounds();
                }
            } else {
                // Should only render part of View.
                try {
                    // --- determine locations ---
                    Shape shape = view.modelToView(offs0, Position.Bias.Forward,
                            offs1, Position.Bias.Backward,
                            bounds);
                    r = (shape instanceof Rectangle)
                            ? (Rectangle) shape : shape.getBounds();
                } catch (BadLocationException e) {
                    // can't render
                    r = null;
                }
            }

            if (r != null) {
                r.width = Math.max(r.width, 1);

                paint(g, offs0, offs1, r, c);

            }

            return r;
        }
    }

    @Override
    public void paint(Graphics g) {
        g.setColor(Color.white);
        g.fillRect(0, 0, getWidth(), getHeight());
        FontMetrics fontMetrics = g.getFontMetrics();
        int lh = fontMetrics.getHeight();
        int d = fontMetrics.getDescent();

        if (lastLine > 0) {
            if (error) {
                g.setColor(BG_ERROR_LINE);
            } else {
                g.setColor(BG_SELECTED_LINE);
            }
            g.fillRect(0, d + lh * lastLine - 1, getWidth(), lh);
        }
        for (int line : lineMarkers.keySet()) {
            SortedSet<LineMarker> cs = lineMarkers.get(line);
            if (cs.isEmpty()) {
                continue;
            }
            LineMarker lastMarker = cs.first();
            if (lastMarker.getBgColor() == null) {
                continue;
            }
            g.setColor(lastMarker.getBgColor());
            line += firstLineOffset();
            g.fillRect(0, d + lh * (line - 1), getWidth(), lh);
        }
        super.paint(g);
        for (int line : lineMarkers.keySet()) {

            SortedSet<LineMarker> cs = lineMarkers.get(line);
            if (cs.isEmpty()) {
                continue;
            }
            line += firstLineOffset();

            Reference<Integer> lineStart = new Reference<>(0);
            Reference<Integer> lineEnd = new Reference<>(0);

            getLineBounds(line, lineStart, lineEnd);
            FgPainter fgp = cs.first().getForegroundPainter();
            if (fgp != null) {
                fgp.paint(g, lineStart.getVal(), lineEnd.getVal(), null, this);
            }
        }
    }
}
