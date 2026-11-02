/*
 *  Copyright (C) 2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.easygui;

import java.awt.Shape;
import java.awt.event.InputEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * A pending Bezier path. Clicks add corners, drags pull symmetric handles and
 * Shift breaks the handle symmetry. Geometry stays in shape-local twips;
 * snapping, hit testing and guide sizes use the current canvas transform.
 *
 * @author JPEXS
 */
public final class BezierPen {

    private static final double HIT_DISTANCE = 6;
    private final List<Node> nodes = new ArrayList<>();
    private Node active;
    private Point2D pressPoint;
    private Point2D hover;
    private boolean dragging;
    private boolean independentHandle;
    private boolean closing;
    private boolean closed;
    private boolean finished;

    private static class Node {

        final Point2D position;
        Point2D incoming;
        Point2D outgoing;

        Node(Point2D position) {
            this.position = (Point2D) position.clone();
            incoming = (Point2D) position.clone();
            outgoing = (Point2D) position.clone();
        }
    }

    public void press(Point2D point, int modifiers, int clickCount, AffineTransform toPanel) {
        if (finished) {
            return;
        }
        if (clickCount > 1 && !nodes.isEmpty()) {
            finish(false);
            return;
        }
        closing = nodes.size() > 1 && distance(nodes.get(0).position, point, toPanel) <= HIT_DISTANCE;
        active = closing ? nodes.get(0) : new Node(snapNode(point, modifiers, toPanel));
        if (!closing) {
            nodes.add(active);
        }
        hover = null;
        pressPoint = (Point2D) point.clone();
        dragging = false;
        independentHandle = false;
    }

    public void drag(Point2D point, int modifiers, AffineTransform toPanel) {
        if (active == null || finished) {
            return;
        }
        if (!dragging && distance(pressPoint, point, toPanel) < 3) {
            return;
        }
        dragging = true;
        if ((modifiers & InputEvent.SHIFT_DOWN_MASK) != 0) {
            independentHandle = true;
        }
        active.outgoing = snap(active.position, point, modifiers, toPanel);
        if (!independentHandle) {
            active.incoming = new Point2D.Double(2 * active.position.getX() - active.outgoing.getX(),
                    2 * active.position.getY() - active.outgoing.getY());
        }
    }

    public void release(Point2D point, int modifiers, AffineTransform toPanel) {
        if (active == null || finished) {
            return;
        }
        drag(point, modifiers, toPanel);
        active = null;
        if (closing) {
            finish(true);
        }
    }

    public void move(Point2D point, int modifiers, AffineTransform toPanel) {
        if (active == null && !finished && !nodes.isEmpty()) {
            hover = nodes.size() > 1 && distance(nodes.get(0).position, point, toPanel) <= HIT_DISTANCE
                    ? (Point2D) nodes.get(0).position.clone() : snapNode(point, modifiers, toPanel);
        }
    }

    private Point2D snapNode(Point2D point, int modifiers, AffineTransform toPanel) {
        return nodes.isEmpty() ? (Point2D) point.clone() : snap(nodes.get(nodes.size() - 1).position, point, modifiers, toPanel);
    }

    private static Point2D snap(Point2D origin, Point2D point, int modifiers, AffineTransform toPanel) {
        if ((modifiers & InputEvent.CTRL_DOWN_MASK) == 0) {
            return (Point2D) point.clone();
        }
        Point2D start = toPanel.transform(origin, null);
        Point2D end = toPanel.transform(point, null);
        double angle = Math.round(Math.atan2(end.getY() - start.getY(), end.getX() - start.getX()) / (Math.PI / 12)) * Math.PI / 12;
        double radius = start.distance(end);
        try {
            return toPanel.inverseTransform(new Point2D.Double(start.getX() + radius * Math.cos(angle),
                    start.getY() + radius * Math.sin(angle)), null);
        } catch (NoninvertibleTransformException ex) {
            return (Point2D) point.clone();
        }
    }

    private static double distance(Point2D first, Point2D second, AffineTransform toPanel) {
        return toPanel.transform(first, null).distance(toPanel.transform(second, null));
    }

    public void removeLastNode() {
        if (!nodes.isEmpty()) {
            nodes.remove(nodes.size() - 1);
        }
        active = null;
        hover = null;
        closing = false;
        finished = false;
        closed = false;
    }

    public void finish(boolean close) {
        active = null;
        hover = null;
        closed = close && nodes.size() > 1;
        finished = true;
    }

    public boolean isFinished() {
        return finished;
    }

    public boolean isClosed() {
        return closed;
    }

    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    public Path2D.Double getPath() {
        return path(false);
    }

    public Path2D.Double getPreviewPath() {
        return path(true);
    }

    private Path2D.Double path(boolean preview) {
        Path2D.Double result = new Path2D.Double();
        if (nodes.isEmpty()) {
            return result;
        }
        Node first = nodes.get(0);
        result.moveTo(first.position.getX(), first.position.getY());
        for (int i = 1; i < nodes.size(); i++) {
            append(result, nodes.get(i - 1), nodes.get(i));
        }
        Node last = nodes.get(nodes.size() - 1);
        if (closed || closing) {
            append(result, last, first);
            result.closePath();
        } else if (preview && hover != null) {
            append(result, last, hover.equals(first.position) ? first : new Node(hover));
        }
        return result;
    }

    private static void append(Path2D.Double path, Node from, Node to) {
        if (from.outgoing.equals(from.position) && to.incoming.equals(to.position)) {
            path.lineTo(to.position.getX(), to.position.getY());
        } else {
            path.curveTo(from.outgoing.getX(), from.outgoing.getY(), to.incoming.getX(), to.incoming.getY(),
                    to.position.getX(), to.position.getY());
        }
    }

    /**
     * Returns a local-coordinate guide whose nodes and handles keep their size
     * on screen even inside a rotated or nonuniformly scaled sprite.
     */
    public Shape getGuide(AffineTransform toPanel) {
        Path2D.Double guide = new Path2D.Double(toPanel.createTransformedShape(getPreviewPath()));
        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            Point2D anchor = toPanel.transform(node.position, null);
            double size = i == 0 ? 8 : 6;
            guide.append(new Rectangle2D.Double(anchor.getX() - size / 2, anchor.getY() - size / 2, size, size), false);
            appendHandle(guide, anchor, toPanel.transform(node.incoming, null));
            appendHandle(guide, anchor, toPanel.transform(node.outgoing, null));
        }
        if (hover != null && hover.equals(nodes.get(0).position)) {
            Point2D first = toPanel.transform(hover, null);
            guide.append(new Ellipse2D.Double(first.getX() - 6, first.getY() - 6, 12, 12), false);
        }
        try {
            return toPanel.createInverse().createTransformedShape(guide);
        } catch (NoninvertibleTransformException ex) {
            return new Path2D.Double();
        }
    }

    private static void appendHandle(Path2D.Double guide, Point2D anchor, Point2D handle) {
        if (anchor.distance(handle) < 1) {
            return;
        }
        guide.moveTo(anchor.getX(), anchor.getY());
        guide.lineTo(handle.getX(), handle.getY());
        guide.append(new Ellipse2D.Double(handle.getX() - 2.5, handle.getY() - 2.5, 5, 5), false);
    }
}
