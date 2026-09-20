/*
 *  Copyright (C) 2010-2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Interactive, piecewise cubic Bezier easing editor.
 */
public class CustomEasePanel extends JPanel {

    private static final int LEFT = 78;
    private static final int RIGHT = 16;
    private static final int TOP = 16;
    private static final int BOTTOM = 48;
    private static final int HIT_SIZE = 9;
    private static final double MIN_POINT_DISTANCE = 0.01;

    private final List<EasePoint> points = new ArrayList<>();
    private final List<Runnable> changeListeners = new ArrayList<>();
    private final int frameCount;
    private final String framesLabel;
    private final String tweenLabel;
    private final String frameLabel;
    private DragTarget dragTarget;
    private EasePoint selectedPoint;

    public CustomEasePanel(int frameCount, String framesLabel, String tweenLabel, String frameLabel) {
        this.frameCount = frameCount;
        this.framesLabel = framesLabel;
        this.tweenLabel = tweenLabel;
        this.frameLabel = frameLabel;
        setPreferredSize(new Dimension(520, 250));
        setBackground(Color.WHITE);

        EasePoint start = new EasePoint(0, 0);
        EasePoint end = new EasePoint(1, 1);
        start.outX = 1.0 / 3;
        start.outY = 1.0 / 3;
        end.inX = 2.0 / 3;
        end.inY = 2.0 / 3;
        points.add(start);
        points.add(end);
        selectedPoint = start;

        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                requestFocusInWindow();
                if (SwingUtilities.isRightMouseButton(event)) {
                    removePointAt(event.getPoint());
                    return;
                }
                dragTarget = findTarget(event.getPoint());
                if (dragTarget == null) {
                    dragTarget = addPointAt(event.getPoint());
                }
                if (dragTarget != null) {
                    selectedPoint = dragTarget.point;
                    repaint();
                }
            }

            @Override
            public void mouseDragged(MouseEvent event) {
                if (dragTarget != null) {
                    moveTarget(dragTarget, toCurveX(event.getX()), toCurveY(event.getY()));
                }
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                dragTarget = null;
            }
        };
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
    }

    public void addChangeListener(Runnable listener) {
        changeListeners.add(listener);
    }

    public String getCurveData() {
        StringBuilder result = new StringBuilder();
        for (EasePoint point : points) {
            if (result.length() > 0) {
                result.append(';');
            }
            result.append(point.x).append(',').append(point.y).append(',')
                    .append(point.inX).append(',').append(point.inY).append(',')
                    .append(point.outX).append(',').append(point.outY);
        }
        return result.toString();
    }

    public void setCurveData(String curveData) {
        if (curveData == null || curveData.isEmpty()) {
            return;
        }
        List<EasePoint> loadedPoints = new ArrayList<>();
        try {
            String[] serializedPoints = curveData.split(";");
            for (String serializedPoint : serializedPoints) {
                String[] values = serializedPoint.split(",");
                if (values.length != 6) {
                    return;
                }
                EasePoint point = new EasePoint(Double.parseDouble(values[0]), Double.parseDouble(values[1]));
                point.inX = Double.parseDouble(values[2]);
                point.inY = Double.parseDouble(values[3]);
                point.outX = Double.parseDouble(values[4]);
                point.outY = Double.parseDouble(values[5]);
                loadedPoints.add(point);
            }
        } catch (NumberFormatException ex) {
            return;
        }
        if (!isValidCurve(loadedPoints)) {
            return;
        }
        points.clear();
        points.addAll(loadedPoints);
        selectedPoint = points.get(0);
        repaint();
    }

    private boolean isValidCurve(List<EasePoint> curvePoints) {
        if (curvePoints.size() < 2) {
            return false;
        }
        for (int i = 0; i < curvePoints.size(); i++) {
            EasePoint point = curvePoints.get(i);
            if (!isFinite(point.x) || !isFinite(point.y) || !isFinite(point.inX)
                    || !isFinite(point.inY) || !isFinite(point.outX) || !isFinite(point.outY)) {
                return false;
            }
            if (point.x < 0 || point.x > 1 || point.y < 0 || point.y > 1
                    || point.inX < 0 || point.inX > 1 || point.inY < 0 || point.inY > 1
                    || point.outX < 0 || point.outX > 1 || point.outY < 0 || point.outY > 1) {
                return false;
            }
            if (i > 0 && point.x <= curvePoints.get(i - 1).x) {
                return false;
            }
        }
        EasePoint first = curvePoints.get(0);
        EasePoint last = curvePoints.get(curvePoints.size() - 1);
        if (Double.compare(first.x, 0) != 0 || Double.compare(first.y, 0) != 0
                || Double.compare(last.x, 1) != 0 || Double.compare(last.y, 1) != 0) {
            return false;
        }
        for (int i = 0; i < curvePoints.size() - 1; i++) {
            EasePoint start = curvePoints.get(i);
            EasePoint end = curvePoints.get(i + 1);
            if (start.outX < start.x || start.outX > end.x
                    || end.inX < start.x || end.inX > end.x) {
                return false;
            }
        }
        return true;
    }

    private boolean isFinite(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }

    public double getValue(double progress) {
        double x = clamp(progress, 0, 1);
        int segment = findSegment(x);
        EasePoint start = points.get(segment);
        EasePoint end = points.get(segment + 1);
        double parameter = findParameter(x, start, end);
        return cubic(parameter, start.y, start.outY, end.inY, end.y);
    }

    private int findSegment(double x) {
        for (int i = 0; i < points.size() - 1; i++) {
            if (x <= points.get(i + 1).x) {
                return i;
            }
        }
        return points.size() - 2;
    }

    private double findParameter(double x, EasePoint start, EasePoint end) {
        double low = 0;
        double high = 1;
        double parameter = 0;
        for (int i = 0; i < 24; i++) {
            parameter = (low + high) / 2;
            double current = cubic(parameter, start.x, start.outX, end.inX, end.x);
            if (current < x) {
                low = parameter;
            } else {
                high = parameter;
            }
        }
        return parameter;
    }

    private double cubic(double t, double p0, double p1, double p2, double p3) {
        double inverse = 1 - t;
        return inverse * inverse * inverse * p0
                + 3 * inverse * inverse * t * p1
                + 3 * inverse * t * t * p2 + t * t * t * p3;
    }

    private DragTarget findTarget(Point point) {
        for (int i = points.size() - 1; i >= 0; i--) {
            EasePoint easePoint = points.get(i);
            if (distance(point, easePoint.x, easePoint.y) <= HIT_SIZE) {
                return new DragTarget(easePoint, TargetType.ANCHOR, i);
            }
            if (i > 0 && distance(point, easePoint.inX, easePoint.inY) <= HIT_SIZE) {
                return new DragTarget(easePoint, TargetType.IN_HANDLE, i);
            }
            if (i < points.size() - 1 && distance(point, easePoint.outX, easePoint.outY) <= HIT_SIZE) {
                return new DragTarget(easePoint, TargetType.OUT_HANDLE, i);
            }
        }
        return null;
    }

    private DragTarget addPointAt(Point mousePoint) {
        double x = toCurveX(mousePoint.x);
        if (x <= MIN_POINT_DISTANCE || x >= 1 - MIN_POINT_DISTANCE) {
            return null;
        }
        double y = getValue(x);
        if (distance(mousePoint, x, y) > HIT_SIZE * 1.5) {
            return null;
        }

        int segment = findSegment(x);
        EasePoint start = points.get(segment);
        EasePoint end = points.get(segment + 1);
        double parameter = findParameter(x, start, end);
        double q0x = lerp(start.x, start.outX, parameter);
        double q0y = lerp(start.y, start.outY, parameter);
        double q1x = lerp(start.outX, end.inX, parameter);
        double q1y = lerp(start.outY, end.inY, parameter);
        double q2x = lerp(end.inX, end.x, parameter);
        double q2y = lerp(end.inY, end.y, parameter);
        double r0x = lerp(q0x, q1x, parameter);
        double r0y = lerp(q0y, q1y, parameter);
        double r1x = lerp(q1x, q2x, parameter);
        double r1y = lerp(q1y, q2y, parameter);

        EasePoint added = new EasePoint(lerp(r0x, r1x, parameter), lerp(r0y, r1y, parameter));
        added.inX = r0x;
        added.inY = r0y;
        added.outX = r1x;
        added.outY = r1y;
        start.outX = q0x;
        start.outY = q0y;
        end.inX = q2x;
        end.inY = q2y;
        points.add(segment + 1, added);
        fireCurveChanged();
        return new DragTarget(added, TargetType.ANCHOR, segment + 1);
    }

    private void removePointAt(Point mousePoint) {
        DragTarget target = findTarget(mousePoint);
        if (target == null || target.type != TargetType.ANCHOR
                || target.index == 0 || target.index == points.size() - 1) {
            return;
        }
        EasePoint previous = points.get(target.index - 1);
        EasePoint next = points.get(target.index + 1);
        previous.outX = lerp(previous.x, next.x, 1.0 / 3);
        previous.outY = lerp(previous.y, next.y, 1.0 / 3);
        next.inX = lerp(previous.x, next.x, 2.0 / 3);
        next.inY = lerp(previous.y, next.y, 2.0 / 3);
        points.remove(target.index);
        selectedPoint = previous;
        fireCurveChanged();
    }

    private void moveTarget(DragTarget target, double x, double y) {
        EasePoint point = target.point;
        switch (target.type) {
            case ANCHOR:
                if (target.index == 0 || target.index == points.size() - 1) {
                    return;
                }
                EasePoint previous = points.get(target.index - 1);
                EasePoint next = points.get(target.index + 1);
                double newX = clamp(x, previous.x + MIN_POINT_DISTANCE, next.x - MIN_POINT_DISTANCE);
                double newY = clamp(y, 0, 1);
                double dx = newX - point.x;
                double dy = newY - point.y;
                point.x = newX;
                point.y = newY;
                point.inX = clamp(point.inX + dx, previous.x, point.x);
                point.inY = clamp(point.inY + dy, 0, 1);
                point.outX = clamp(point.outX + dx, point.x, next.x);
                point.outY = clamp(point.outY + dy, 0, 1);
                previous.outX = Math.min(previous.outX, point.x);
                next.inX = Math.max(next.inX, point.x);
                break;
            case IN_HANDLE:
                point.inX = clamp(x, points.get(target.index - 1).x, point.x);
                point.inY = clamp(y, 0, 1);
                break;
            case OUT_HANDLE:
                point.outX = clamp(x, point.x, points.get(target.index + 1).x);
                point.outY = clamp(y, 0, 1);
                break;
            default:
                return;
        }
        fireCurveChanged();
    }

    private void fireCurveChanged() {
        repaint();
        for (Runnable listener : changeListeners) {
            listener.run();
        }
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int graphWidth = Math.max(1, getWidth() - LEFT - RIGHT);
        int graphHeight = Math.max(1, getHeight() - TOP - BOTTOM);

        g.setColor(new Color(0xe4e4e4));
        for (int i = 0; i <= 10; i++) {
            int y = TOP + (int) Math.round(graphHeight * i / 10.0);
            g.drawLine(LEFT, y, LEFT + graphWidth, y);
            String label = (100 - i * 10) + "%";
            g.setColor(Color.GRAY);
            g.drawString(label, 30, y + 5);
            g.setColor(new Color(0xe4e4e4));
        }
        int gridFrames = Math.min(frameCount, 20);
        for (int i = 0; i < gridFrames; i++) {
            double ratio = gridFrames == 1 ? 0 : (double) i / (gridFrames - 1);
            int x = LEFT + (int) Math.round(graphWidth * ratio);
            g.drawLine(x, TOP, x, TOP + graphHeight);
            int frameNumber = 1 + (int) Math.round(ratio * Math.max(0, frameCount - 1));
            String frameText = Integer.toString(frameNumber);
            g.setColor(Color.GRAY);
            g.drawString(frameText, x - g.getFontMetrics().stringWidth(frameText) / 2,
                    TOP + graphHeight - 4);
            g.setColor(new Color(0xe4e4e4));
        }

        g.setColor(Color.DARK_GRAY);
        FontMetrics metrics = g.getFontMetrics();
        g.drawString(framesLabel, LEFT + (graphWidth - metrics.stringWidth(framesLabel)) / 2,
                getHeight() - 8);
        Graphics2D vertical = (Graphics2D) g.create();
        vertical.rotate(-Math.PI / 2);
        vertical.drawString(tweenLabel, -(TOP + (graphHeight + metrics.stringWidth(tweenLabel)) / 2), 15);
        vertical.dispose();

        g.setStroke(new BasicStroke(1f));
        g.setColor(Color.GRAY);
        for (int i = 0; i < points.size(); i++) {
            EasePoint point = points.get(i);
            if (i > 0) {
                drawLine(g, point.x, point.y, point.inX, point.inY);
                drawHandle(g, point.inX, point.inY);
            }
            if (i < points.size() - 1) {
                drawLine(g, point.x, point.y, point.outX, point.outY);
                drawHandle(g, point.outX, point.outY);
            }
        }

        Path2D curve = new Path2D.Double();
        EasePoint first = points.get(0);
        curve.moveTo(toScreenX(first.x), toScreenY(first.y));
        for (int i = 0; i < points.size() - 1; i++) {
            EasePoint start = points.get(i);
            EasePoint end = points.get(i + 1);
            curve.curveTo(toScreenX(start.outX), toScreenY(start.outY),
                    toScreenX(end.inX), toScreenY(end.inY), toScreenX(end.x), toScreenY(end.y));
        }
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(1.5f));
        g.draw(curve);
        for (EasePoint point : points) {
            drawAnchor(g, point);
        }

        if (selectedPoint != null) {
            double frame = 1 + selectedPoint.x * Math.max(0, frameCount - 1);
            String status = String.format("%s %.2f, %.0f%%", frameLabel, frame, selectedPoint.y * 100);
            g.setColor(Color.DARK_GRAY);
            g.drawString(status, LEFT + graphWidth - metrics.stringWidth(status), getHeight() - 8);
        }
        g.dispose();
    }

    private void drawLine(Graphics2D g, double x1, double y1, double x2, double y2) {
        g.drawLine(toScreenX(x1), toScreenY(y1), toScreenX(x2), toScreenY(y2));
    }

    private void drawHandle(Graphics2D g, double x, double y) {
        int screenX = toScreenX(x);
        int screenY = toScreenY(y);
        g.setColor(Color.WHITE);
        g.fillRect(screenX - 4, screenY - 4, 8, 8);
        g.setColor(Color.GRAY);
        g.drawRect(screenX - 4, screenY - 4, 8, 8);
    }

    private void drawAnchor(Graphics2D g, EasePoint point) {
        int screenX = toScreenX(point.x);
        int screenY = toScreenY(point.y);
        g.setColor(point == selectedPoint ? new Color(0x202020) : Color.BLACK);
        g.fillRect(screenX - 5, screenY - 5, 10, 10);
    }

    private double distance(Point point, double x, double y) {
        return point.distance(toScreenX(x), toScreenY(y));
    }

    private int toScreenX(double x) {
        return LEFT + (int) Math.round(x * Math.max(1, getWidth() - LEFT - RIGHT));
    }

    private int toScreenY(double y) {
        return TOP + (int) Math.round((1 - y) * Math.max(1, getHeight() - TOP - BOTTOM));
    }

    private double toCurveX(int x) {
        return clamp((double) (x - LEFT) / Math.max(1, getWidth() - LEFT - RIGHT), 0, 1);
    }

    private double toCurveY(int y) {
        return clamp(1 - (double) (y - TOP) / Math.max(1, getHeight() - TOP - BOTTOM), 0, 1);
    }

    private double lerp(double start, double end, double progress) {
        return start + (end - start) * progress;
    }

    private double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private enum TargetType {
        ANCHOR, IN_HANDLE, OUT_HANDLE
    }

    private static class DragTarget {

        private final EasePoint point;
        private final TargetType type;
        private final int index;

        DragTarget(EasePoint point, TargetType type, int index) {
            this.point = point;
            this.type = type;
            this.index = index;
        }
    }

    private static class EasePoint {

        private double x;
        private double y;
        private double inX;
        private double inY;
        private double outX;
        private double outY;

        EasePoint(double x, double y) {
            this.x = x;
            this.y = y;
            inX = x;
            inY = y;
            outX = x;
            outY = y;
        }
    }
}
