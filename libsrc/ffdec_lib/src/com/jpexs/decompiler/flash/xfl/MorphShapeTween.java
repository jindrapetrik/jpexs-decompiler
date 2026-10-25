/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.flash.xfl;

import com.jpexs.decompiler.flash.tags.base.MorphShapeTag;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
import com.jpexs.decompiler.flash.types.shaperecords.CurvedEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.stream.XMLStreamException;

/** Restores editable single-contour tweens using SWF's vertex correspondence. */
final class MorphShapeTween {
    private static final Pattern FRAME = Pattern.compile("<DOMFrame\\b[^>]*>.*?</DOMFrame>", Pattern.DOTALL);
    private static final Pattern INDEX = Pattern.compile("\\bindex=\"([0-9]+)\"");
    private static final Pattern DURATION = Pattern.compile("\\bduration=\"([0-9]+)\"");
    private static final Pattern FILLS = Pattern.compile("<fills>.*?</fills>", Pattern.DOTALL);
    private static final Pattern STROKES = Pattern.compile("<strokes>.*?</strokes>", Pattern.DOTALL);

    private MorphShapeTween() {
    }

    static final class Sample {
        final MorphShapeTag morph;
        final int ratio;
        final int tx;
        final int ty;
        final List<Curve> curves;
        final String styles;
        final boolean fill;
        final boolean stroke;

        Sample(MorphShapeTag morph, int ratio, MATRIX matrix, List<Curve> curves, String styles, boolean fill, boolean stroke) {
            this.morph = morph;
            this.ratio = ratio;
            tx = matrix == null ? 0 : matrix.translateX;
            ty = matrix == null ? 0 : matrix.translateY;
            this.curves = curves;
            this.styles = styles;
            this.fill = fill;
            this.stroke = stroke;
        }

        String elements() throws XMLStreamException {
            XFLXmlWriter writer = new XFLXmlWriter();
            writer.writeStartElement("elements");
            writer.writeStartElement("DOMShape");
            writer.writeCharactersRaw(styles);
            writer.writeStartElement("edges");
            writer.writeStartElement("Edge");
            // SWF fill side 0 is XFL side 1 for this clockwise contour.
            if (fill) {
                writer.writeAttribute("fillStyle1", "1");
            }
            if (stroke) {
                writer.writeAttribute("strokeStyle", "1");
            }
            StringBuilder edges = new StringBuilder("! ").append(curves.get(0).x + tx).append(' ').append(curves.get(0).y + ty);
            for (Curve curve : curves) {
                edges.append(curve.line ? "| " : "[ ");
                if (!curve.line) {
                    edges.append(curve.cx + tx).append(' ').append(curve.cy + ty).append(' ');
                }
                edges.append(curve.ex + tx).append(' ').append(curve.ey + ty);
            }
            writer.writeAttribute("edges", edges.toString());
            writer.writeEndElement();
            writer.writeEndElement();
            writer.writeEndElement();
            writer.writeEndElement();
            return writer.toString();
        }
    }

    private static final class Curve {
        int x;
        int y;
        int cx;
        int cy;
        int ex;
        int ey;
        boolean line;
        boolean fill;
        boolean stroke;
    }

    static Sample sample(MorphShapeTag morph, int ratio, MATRIX matrix, String elements) {
        // Keep independently painted fills, holes and changing object transforms
        // on the sampled-pose path. A single paint can also tween its styles.
        if (matrix != null && (matrix.getScaleXFloat() != 1 || matrix.getScaleYFloat() != 1
                || matrix.getRotateSkew0Float() != 0 || matrix.getRotateSkew1Float() != 0)
                || morph.morphFillStyles.fillStyles.length > 1) {
            return null;
        }
        List<Curve> start = contour(morph.getShapeAtRatio(0));
        List<Curve> end = contour(morph.getShapeAtRatio(65535));
        List<Curve> pose = contour(morph.getShapeAtRatio(ratio));
        if (start == null || end == null || pose == null || start.size() != end.size() || start.size() != pose.size()) {
            return null;
        }
        for (int i = 0; i < start.size(); i++) {
            if (start.get(i).line != end.get(i).line || start.get(i).line != pose.get(i).line) {
                return null;
            }
        }
        Matcher fills = FILLS.matcher(elements);
        Matcher strokes = STROKES.matcher(elements);
        boolean fill = fills.find();
        String styles = fill ? fills.group() : "";
        boolean stroke = strokes.find();
        if (!fill && !stroke) {
            return null;
        }
        if (stroke) {
            String line = strokes.group();
            styles += line;
        }
        // CS6 can reverse its contour matching when the first hint lies on a
        // short or nearly straight junction. Start at a distinct corner in both
        // source endpoints, and keep that same cyclic order for every ratio.
        Collections.rotate(pose, -hintStart(start, end));
        return new Sample(morph, ratio, matrix, pose, styles, fill, stroke);
    }

    private static int hintStart(List<Curve> start, List<Curve> end) {
        int best = 0;
        double strength = -1;
        for (int i = 0; i < start.size(); i++) {
            double candidate = Math.min(cornerStrength(start, i), cornerStrength(end, i));
            if (candidate > strength) {
                best = i;
                strength = candidate;
            }
        }
        return best;
    }

    private static double cornerStrength(List<Curve> curves, int index) {
        Curve previous = curves.get((index + curves.size() - 1) % curves.size());
        Curve current = curves.get(index);
        double ax = current.x - (previous.line ? previous.x : previous.cx);
        double ay = current.y - (previous.line ? previous.y : previous.cy);
        double bx = (current.line ? current.ex : current.cx) - current.x;
        double by = (current.line ? current.ey : current.cy) - current.y;
        double chord = Math.hypot(ax + bx, ay + by);
        // Distance from the junction to its tangent endpoints' chord. This
        // penalizes both nearly collinear edges and tiny adjacent segments.
        return chord == 0 ? 0 : Math.abs(ax * by - ay * bx) / chord;
    }

    private static List<Curve> contour(SHAPEWITHSTYLE shape) {
        int lineCount = shape.lineStyles.lineStyles.length + shape.lineStyles.lineStyles2.length;
        if (lineCount > 1) {
            return null;
        }
        List<Curve> all = new ArrayList<>();
        int x = 0;
        int y = 0;
        boolean filled = false;
        boolean stroked = false;
        boolean activeFill = false;
        boolean activeStroke = false;
        for (SHAPERECORD record : shape.shapeRecords) {
            if (record instanceof StyleChangeRecord) {
                StyleChangeRecord style = (StyleChangeRecord) record;
                if (style.stateNewStyles || style.stateFillStyle1 && style.fillStyle1 != 0
                        || style.stateFillStyle0 && style.fillStyle0 != 0 && style.fillStyle0 != 1) {
                    return null;
                }
                if (style.stateFillStyle0) {
                    activeFill = style.fillStyle0 == 1;
                }
                if (style.stateLineStyle) {
                    if (style.lineStyle != 0 && style.lineStyle != 1) {
                        return null;
                    }
                    activeStroke = style.lineStyle == 1;
                }
                filled |= style.stateFillStyle0 && style.fillStyle0 == 1;
                stroked |= style.stateLineStyle && style.lineStyle == 1;
                if (style.stateMoveTo) {
                    x = style.moveDeltaX;
                    y = style.moveDeltaY;
                }
                continue;
            }
            if (!(record instanceof StraightEdgeRecord) && !(record instanceof CurvedEdgeRecord)) {
                continue;
            }
            Curve c = new Curve();
            c.fill = activeFill;
            c.stroke = activeStroke;
            c.x = x;
            c.y = y;
            c.line = record instanceof StraightEdgeRecord;
            if (c.line) {
                StraightEdgeRecord e = (StraightEdgeRecord) record;
                c.cx = x + e.deltaX / 2;
                c.cy = y + e.deltaY / 2;
            } else {
                CurvedEdgeRecord e = (CurvedEdgeRecord) record;
                c.cx = x + e.controlDeltaX;
                c.cy = y + e.controlDeltaY;
            }
            x = c.ex = record.changeX(x);
            y = c.ey = record.changeY(y);
            all.add(c);
        }
        if ((!filled && !stroked) || all.size() < 3) {
            return null;
        }
        int count = all.size();
        // Flash's compiled morphs often repeat the outline for the stroke and
        // fill. Collapse only matching copies, allowing accumulated twip rounding.
        if (count % 2 == 0) {
            int half = count / 2;
            boolean duplicate = true;
            for (int i = 0; i < half; i++) {
                Curve a = all.get(i);
                Curve b = all.get(i + half);
                duplicate &= a.line == b.line && near(a.x, b.x) && near(a.y, b.y)
                        && near(a.cx, b.cx) && near(a.cy, b.cy) && near(a.ex, b.ex) && near(a.ey, b.ey);
            }
            if (duplicate) {
                count = half;
            }
        }
        // Authoring shape hints are named a-z. Never silently drop vertices.
        if (count < 3 || count > 26) {
            return null;
        }
        boolean wholeFill = false;
        boolean wholeStroke = false;
        for (int offset = 0; offset < all.size(); offset += count) {
            Curve initial = all.get(offset);
            for (int v = 0; v < count; v++) {
                Curve c = all.get(offset + v);
                if (c.fill != initial.fill || c.stroke != initial.stroke) {
                    return null;
                }
            }
            wholeFill |= initial.fill;
            wholeStroke |= initial.stroke;
        }
        if ((!wholeFill && !wholeStroke) || lineCount == 1 && !wholeStroke) {
            return null;
        }
        List<Curve> result = new ArrayList<>(all.subList(0, count));
        for (int i = 0; i < count; i++) {
            for (int j = 0; j < i; j++) {
                if (result.get(i).x == result.get(j).x && result.get(i).y == result.get(j).y) {
                    return null; // Coincident vertices cannot carry unambiguous hints.
                }
            }
        }
        Curve first = result.get(0);
        Curve last = result.get(count - 1);
        if (!near(first.x, last.ex) || !near(first.y, last.ey)) {
            return null;
        }
        last.ex = first.x;
        last.ey = first.y;
        Path2D path = new Path2D.Double(Path2D.WIND_EVEN_ODD);
        path.moveTo(first.x, first.y);
        for (int i = 0; i < count; i++) {
            Curve c = result.get(i);
            if (i > 0 && (c.x != result.get(i - 1).ex || c.y != result.get(i - 1).ey)) {
                return null;
            }
            if (c.line) {
                path.lineTo(c.ex, c.ey);
            } else {
                path.quadTo(c.cx, c.cy, c.ex, c.ey);
            }
        }
        path.closePath();
        int contours = 0;
        double[] p = new double[6];
        for (PathIterator it = new Area(path).getPathIterator(null); !it.isDone(); it.next()) {
            if (it.currentSegment(p) == PathIterator.SEG_MOVETO) {
                contours++;
            }
        }
        double area = 0;
        double px = first.x;
        double py = first.y;
        for (PathIterator it = path.getPathIterator(null, 0.5); !it.isDone(); it.next()) {
            if (it.currentSegment(p) == PathIterator.SEG_LINETO) {
                area += px * p[1] - p[0] * py;
                px = p[0];
                py = p[1];
            }
        }
        return contours == 1 && area > 0 ? result : null;
    }

    private static boolean near(int a, int b) {
        return Math.abs((long) a - b) <= 2;
    }

    static String detect(String frames, Map<Integer, Sample> samples, Set<Integer> instanceStarts) throws XMLStreamException {
        if (samples.isEmpty()) {
            return frames;
        }
        List<String> xml = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        Matcher matcher = FRAME.matcher(frames);
        while (matcher.find()) {
            xml.add(matcher.group());
            Matcher index = INDEX.matcher(matcher.group());
            index.find();
            indices.add(Integer.parseInt(index.group(1)));
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < xml.size();) {
            Sample first = samples.get(indices.get(i));
            int end = i;
            if (first != null) {
                int direction = 0;
                while (end + 1 < xml.size()) {
                    Sample next = samples.get(indices.get(end + 1));
                    Sample previous = samples.get(indices.get(end));
                    int change = next == null ? 0 : Integer.compare(next.ratio, previous.ratio);
                    if (next == null || next.morph != first.morph || next.tx != first.tx || next.ty != first.ty
                            || next.curves.size() != first.curves.size()  || instanceStarts.contains(indices.get(end + 1))
                            || indices.get(end + 1) != indices.get(end) + duration(xml.get(end))
                            || change != 0 && direction != 0 && change != direction) {
                        break;
                    }
                    if (change != 0) {
                        direction = change;
                    }
                    end++;
                }
            }
            Integer acceleration = null;
            while (end >= i + 2 && (acceleration = fit(samples, indices.get(i), indices.get(end))) == null) {
                end--;
            }
            // Require actual intermediate samples; adjacent keyframes alone do
            // not establish a tween. Retain the last visible pose before removal.
            if (acceleration == null) {
                result.append(xml.get(i++)).append('\n');
                continue;
            }
            Sample last = samples.get(indices.get(end));
            XFLXmlWriter tween = new XFLXmlWriter();
            tween.writeStartElement("DOMFrame", new String[]{"index", indices.get(i).toString(),
                "duration", Integer.toString(indices.get(end) - indices.get(i)), "keyMode", Integer.toString(XFLConverter.KEY_MODE_SHAPE_TWEEN),
                "tweenType", "shape", "acceleration", acceleration.toString()});
            // Frame-level hints survive CS6 rebuilding its MorphShape cache.
            // Hints inside MorphShape describe only the disposable cached mapping.
            tween.writeStartElement("morphHintsList");
            for (int v = 0; v < first.curves.size(); v++) {
                Curve a = first.curves.get(v);
                Curve b = last.curves.get(v);
                tween.writeEmptyElement("MorphHint", new String[]{"startPoint", point(a.x + first.tx, a.y + first.ty),
                    "endPoint", point(b.x + last.tx, b.y + last.ty), "key", Integer.toString(v)});
            }
            tween.writeEndElement();
            tween.writeCharactersRaw(first.elements());
            tween.writeEndElement();
            result.append(tween.toString()).append('\n');
            // Keep the endpoint available as the start of another tween.
            String endpoint = xml.get(end);
            int elementsStart = endpoint.indexOf("<elements");
            int elementsEnd = endpoint.indexOf("</elements>", elementsStart);
            xml.set(end, endpoint.substring(0, elementsStart) + last.elements() + endpoint.substring(elementsEnd + "</elements>".length()));
            i = end;
        }
        return result.toString();
    }

    private static int duration(String frame) {
        Matcher matcher = DURATION.matcher(frame.substring(0, frame.indexOf('>')));
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : 1;
    }

    private static String point(int x, int y) {
        return coordinate(x) + ", " + coordinate(y);
    }

    private static String coordinate(int value) {
        // Morph points use signed 24.8 hexadecimal coordinates, unlike Edge's
        // decimal twips. This represents the same integer without losing bits.
        return String.format(java.util.Locale.ROOT, "#%X.%02X", (value >> 8) & 0xffffff, value & 255);
    }

    private static Integer fit(Map<Integer, Sample> samples, int firstIndex, int lastIndex) {
        Sample first = samples.get(firstIndex);
        Sample last = samples.get(lastIndex);
        double delta = last.ratio - first.ratio;
        if (delta == 0) {
            return null;
        }
        double numerator = 0;
        double denominator = 0;
        for (int frame = firstIndex + 1; frame < lastIndex; frame++) {
            Sample sample = samples.get(frame);
            if (sample == null || sample.morph != first.morph || sample.tx != first.tx || sample.ty != first.ty
                    || sample.curves.size() != first.curves.size()) {
                return null;
            }
            double t = (frame - firstIndex) / (double) (lastIndex - firstIndex);
            double q = t * (1 - t);
            numerator += q * ((sample.ratio - first.ratio) / delta - t);
            denominator += q * q;
        }
        int ease = (int) Math.round(100 * numerator / denominator);
        if (ease < -100 || ease > 100) {
            return null;
        }
        for (int frame = firstIndex + 1; frame < lastIndex; frame++) {
            double t = (frame - firstIndex) / (double) (lastIndex - firstIndex);
            double progress = t + ease / 100.0 * t * (1 - t);
            Sample sample = samples.get(frame);
            if (!first.styles.equals(last.styles)
                    && Math.abs(first.ratio + delta * progress - sample.ratio) > 2) {
                return null;
            }
            // Predict the compiled tween's rounding per edge delta, rather
            // than interpolating absolute points (which underestimates drift).
            progress = Math.round(progress * 65535) / 65535.0;
            int x = blend(first.curves.get(0).x, last.curves.get(0).x, progress);
            int y = blend(first.curves.get(0).y, last.curves.get(0).y, progress);
            for (int v = 0; v < first.curves.size(); v++) {
                Curve a = first.curves.get(v);
                Curve b = last.curves.get(v);
                Curve c = sample.curves.get(v);
                if (!matches(x, c.x) || !matches(y, c.y)) {
                    return null;
                }
                if (!a.line) {
                    x += blend(a.cx - a.x, b.cx - b.x, progress);
                    y += blend(a.cy - a.y, b.cy - b.y, progress);
                    if (!matches(x, c.cx) || !matches(y, c.cy)) {
                        return null;
                    }
                    x += blend(a.ex - a.cx, b.ex - b.cx, progress);
                    y += blend(a.ey - a.cy, b.ey - b.cy, progress);
                } else {
                    x += blend(a.ex - a.x, b.ex - b.x, progress);
                    y += blend(a.ey - a.y, b.ey - b.y, progress);
                }
                if (!matches(x, c.ex) || !matches(y, c.ey)) {
                    return null;
                }
            }
        }
        return -ease;
    }

    private static int blend(int start, int end, double progress) {
        return start + (int) Math.round((end - start) * progress);
    }

    private static boolean matches(int predicted, int actual) {
        // Both SWF edge deltas and CS6's integer acceleration are quantized.
        // Bound the resulting coordinate error to a quarter of an authoring pixel.
        return Math.abs((long) predicted - actual) <= 5;
    }
}
