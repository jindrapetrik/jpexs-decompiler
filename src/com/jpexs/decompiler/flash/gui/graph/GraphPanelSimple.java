/*
 * Copyright (C) 2018 Jindra
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
package com.jpexs.decompiler.flash.gui.graph;

import com.jpexs.decompiler.graph.Graph;
import com.jpexs.decompiler.graph.GraphPart;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author Jindra
 */
public class GraphPanelSimple extends AbstractGraphPanel {

    private static final int SPACE_VERTICAL = 16;

    private static final int SPACE_HORIZONTAL = 10;

    private static final int SPACE_BACKLINKS = 5;

    private static final int BLOCK_WIDTH = 200;

    private static final int BLOCK_HEIGHT = 20;

    private final HashMap<GraphPart, Point> partPos = new HashMap<>();

    private final Point size;

    private int backLinksLeft = 0;

    private int backLinksRight = 0;

    private final GraphPart head;

    public GraphPanelSimple(Graph graph) throws InterruptedException {
        super(graph);
        size = getPartPositions(head = graph.heads.get(0), SPACE_VERTICAL + SPACE_VERTICAL + BLOCK_HEIGHT / 2, getPartWidth(graph.heads.get(0), new HashSet<>()) * (BLOCK_WIDTH + SPACE_HORIZONTAL) / 2 - SPACE_HORIZONTAL, partPos, true);
        backLinksLeft = 1;
        backLinksRight = 1;
        for (GraphPart part : partPos.keySet()) {
            Point p = partPos.get(part);
            for (GraphPart n : part.nextParts) {
                Point npos = partPos.get(n);
                if (npos.y < p.y) {
                    if (p.x > size.x / 2) {
                        backLinksRight++;
                    } else {
                        backLinksLeft++;
                    }
                }
            }
        }
        size.x += 2 * SPACE_BACKLINKS + backLinksLeft * SPACE_BACKLINKS + backLinksRight * SPACE_BACKLINKS;
        setPreferredSize(new Dimension(size.x, size.y));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        /*g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
             RenderingHints.VALUE_ANTIALIAS_ON);*/
        g2.setColor(Color.black);
        int startX = SPACE_BACKLINKS + backLinksLeft * SPACE_BACKLINKS;
        int blLeft = 0;
        int blRight = 0;
        for (GraphPart part : partPos.keySet()) {
            Point p = partPos.get(part);
            g2.setColor(Color.white);
            g2.fillRect(startX + p.x - BLOCK_WIDTH / 2, p.y - BLOCK_HEIGHT / 2, BLOCK_WIDTH, BLOCK_HEIGHT);
            g2.setColor(Color.black);
            g2.drawRect(startX + p.x - BLOCK_WIDTH / 2, p.y - BLOCK_HEIGHT / 2, BLOCK_WIDTH, BLOCK_HEIGHT);
            g2.drawString(part.toString(), startX + p.x - g2.getFontMetrics().stringWidth(part.toString()) / 2, p.y + g2.getFontMetrics().getHeight() / 2);
        }

        Point hp = partPos.get(head);
        drawArrow(g2, startX + hp.x, hp.y - BLOCK_HEIGHT / 2 - SPACE_VERTICAL, startX + hp.x, hp.y - BLOCK_HEIGHT / 2);

        for (GraphPart part : partPos.keySet()) {
            Point p = partPos.get(part);

            for (int n = 0; n < part.nextParts.size(); n++) {
                boolean isIf = part.nextParts.size() == 2;
                if (isIf) {
                    if (n == 0) {
                        g2.setColor(new Color(0, 0x80, 0));
                    } else {
                        g2.setColor(Color.red);
                    }
                } else {
                    g2.setColor(Color.black);
                }
                Point npos = partPos.get(part.nextParts.get(n));
                if (npos.y < p.y) {
                    int sidex = startX;
                    if (p.x > size.x / 2) {
                        blRight++;
                        sidex = size.x - backLinksRight * SPACE_BACKLINKS;
                        sidex += SPACE_BACKLINKS + SPACE_BACKLINKS * blRight;
                    } else {
                        blLeft++;
                        sidex -= SPACE_BACKLINKS + SPACE_BACKLINKS * blLeft;
                    }
                    g2.drawLine(startX + p.x, p.y + BLOCK_HEIGHT / 2, startX + p.x, p.y + BLOCK_HEIGHT / 2 + SPACE_VERTICAL / 2);
                    g2.drawLine(startX + p.x, p.y + BLOCK_HEIGHT / 2 + SPACE_VERTICAL / 2, sidex, p.y + BLOCK_HEIGHT / 2 + SPACE_VERTICAL / 2);
                    g2.drawLine(sidex, npos.y - BLOCK_HEIGHT / 2 - SPACE_VERTICAL / 2, sidex, p.y + BLOCK_HEIGHT / 2 + SPACE_VERTICAL / 2);
                    drawArrow(g2, sidex, npos.y - BLOCK_HEIGHT / 2 - SPACE_VERTICAL / 2, startX + npos.x, npos.y - BLOCK_HEIGHT / 2 - SPACE_VERTICAL / 2);
                    g2.drawLine(startX + npos.x, npos.y - BLOCK_HEIGHT / 2 - SPACE_VERTICAL / 2, startX + npos.x, npos.y - BLOCK_HEIGHT / 2);
                } else {
                    drawArrow(g2, startX + p.x, p.y + BLOCK_HEIGHT / 2, startX + npos.x, npos.y - BLOCK_HEIGHT / 2 - SPACE_VERTICAL / 2);
                    g2.drawLine(startX + npos.x, npos.y - BLOCK_HEIGHT / 2 - SPACE_VERTICAL / 2, startX + npos.x, npos.y - BLOCK_HEIGHT / 2);
                }
            }
        }
    }

    private Point getPartSubPositions(Point ret, int totalWidth, GraphPart part, int y, int x, HashMap<GraphPart, Point> used) {
        if (used.containsKey(part)) {
            Point p = used.get(part);
            return new Point(x, y);
        }
        used.put(part, new Point(x, y));
        if (part.nextParts.size() > 0) {
            int cx = x - totalWidth / 2;
            for (int p = 0; p < part.nextParts.size(); p++) {
                HashSet<GraphPart> k = new HashSet<>();
                k.addAll(used.keySet());
                int partWidth = getPartWidth(part.nextParts.get(p), k);
                int cellWidth = partWidth * (BLOCK_WIDTH + SPACE_HORIZONTAL);
                Point pt = getPartPositions(part.nextParts.get(p), y + BLOCK_HEIGHT + SPACE_VERTICAL, cx + cellWidth / 2, used, false);
                if (pt.x > ret.x) {
                    ret.x = pt.x;
                }
                if (pt.y > ret.y) {
                    ret.y = pt.y;
                }
                cx += cellWidth;

            }
            cx = x - totalWidth / 2;
            for (int p = 0; p < part.nextParts.size(); p++) {
                HashSet<GraphPart> k = new HashSet<>();
                k.addAll(used.keySet());
                int cellWidth = getPartWidth(part.nextParts.get(p), k) * (BLOCK_WIDTH + SPACE_HORIZONTAL);

                HashSet<GraphPart> hs = new HashSet<>();
                hs.addAll(used.keySet());
                int totalWidthParts2 = getPartWidth(part.nextParts.get(p), hs);
                int totalWidth2 = totalWidthParts2 * (BLOCK_WIDTH + SPACE_HORIZONTAL);

                Point pt = getPartSubPositions(ret, totalWidth2, part.nextParts.get(p), y + BLOCK_HEIGHT + SPACE_VERTICAL, cx + cellWidth / 2, used);
                if (pt.x > ret.x) {
                    ret.x = pt.x;
                }
                if (pt.y > ret.y) {
                    ret.y = pt.y;
                }
                cx += cellWidth;
            }

        }
        return ret;
    }

    private Point getPartPositions(GraphPart part, int y, int x, HashMap<GraphPart, Point> used, boolean goSub) {
        HashMap<GraphPart, Point> l = new HashMap<>();
        l.putAll(used);
        HashSet<GraphPart> hs = new HashSet<>();
        hs.addAll(l.keySet());
        int totalWidthParts = getPartWidth(part, hs);
        int totalWidth = totalWidthParts * (BLOCK_WIDTH + SPACE_HORIZONTAL);

        if (used.containsKey(part)) {
            Point p = used.get(part);
            return new Point(x, y);
        }
        Point ret = new Point(x - BLOCK_WIDTH / 2 - SPACE_HORIZONTAL / 2 + BLOCK_WIDTH, y + BLOCK_HEIGHT);
        if (goSub) {
            Point p2 = getPartSubPositions(ret, totalWidth, part, y, x, used);
            if (p2.x > ret.x) {
                ret.x = p2.x;
            }
            if (p2.y > ret.y) {
                ret.y = p2.y;
            }
        }
        return ret;
    }

    private int getPartHeight(GraphPart part, List<GraphPart> used) {
        if (used.contains(part)) {
            return 1;
        }
        used.add(part);
        int maxH = 0;
        for (int p = 0; p < part.nextParts.size(); p++) {
            int h = getPartHeight(part.nextParts.get(p), used);
            if (h > maxH) {
                maxH = h;
            }
        }
        return 1 + maxH;
    }

    private int getPartWidth(GraphPart part, HashSet<GraphPart> used) {

        if (used.contains(part)) {
            return 1;
        }
        used.add(part);
        if (part.nextParts.isEmpty()) {
            return 1;
        }
        int w = 0;
        for (GraphPart subpart : part.nextParts) {
            w += getPartWidth(subpart, used);
        }
        return w;
    }

    private void drawArrow(Graphics g, int x1, int y1, int x2, int y2) {
        Polygon arrowHead = new Polygon();
        arrowHead.addPoint(0, 0);
        arrowHead.addPoint(-3, -8);
        arrowHead.addPoint(3, -8);
        Line2D.Double line = new Line2D.Double(x1, y1, x2, y2);
        AffineTransform tx = new AffineTransform();
        tx.setToIdentity();
        double angle = Math.atan2(line.y2 - line.y1, line.x2 - line.x1);
        tx.translate(line.x2, line.y2);
        tx.rotate((angle - Math.PI / 2d));

        Graphics2D g2d = (Graphics2D) g;
        AffineTransform oldTransform = g2d.getTransform();
        g2d.draw(line);
        tx.preConcatenate(oldTransform);
        g2d.setTransform(tx);
        g2d.fill(arrowHead);
        g2d.setTransform(oldTransform);
    }
}
