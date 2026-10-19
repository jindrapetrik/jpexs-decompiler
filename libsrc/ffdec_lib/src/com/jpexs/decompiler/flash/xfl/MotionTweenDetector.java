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

import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.XMLStreamException;

/**
 * Fits independent cubic property curves for modern motion-object tweens.
 * Classic-compatible spans are handled by ClassicTweenDetector first.
 */
final class MotionTweenDetector {
    private static final double POSITION_TOLERANCE = 0.051;
    private static final double MATRIX_TOLERANCE = 2.0 / 65536;
    private static final String[] COLOR_PROPERTIES = {
        "AdvClr_R_Pct", "AdvClr_G_Pct", "AdvClr_B_Pct", "AdvClr_A_Pct",
        "AdvClr_R_Offset", "AdvClr_G_Offset", "AdvClr_B_Offset", "AdvClr_A_Offset"
    };

    private MotionTweenDetector() {
    }

    static String fit(List<ClassicTweenDetector.Sample> samples, int start, int end,
            double frameRate, double[] registrationPoint) throws XMLStreamException {
        // Four points always define a cubic; require additional evidence.
        if (end - start < 5 || !Double.isFinite(frameRate) || frameRate <= 0) {
            return null;
        }
        ClassicTweenDetector.Sample endpoint = samples.get(end);
        if (endpoint.duration > 1) {
            // A quantized native wave may end in a coalesced flat tail. Its
            // full length affects frame-rounded Bounce widths. Try the entire
            // hold before shortening the native map; verify every held frame.
            List<ClassicTweenDetector.Sample> expanded = new ArrayList<>(samples.subList(0, end + 1));
            ClassicTweenDetector.Sample hold = copySample(endpoint);
            hold.duration--;
            expanded.set(end, hold);
            ClassicTweenDetector.Sample last = copySample(endpoint);
            last.index += endpoint.duration - 1;
            last.duration = 1;
            expanded.add(last);
            String full = fit(expanded, start, end + 1, frameRate, registrationPoint);
            if (full != null && (full.contains("type=\"Spring\"") || full.contains("type=\"Bounce\"")
                    || full.contains("type=\"BounceIn\""))) {
                return full;
            }
        }
        if (!FilterTweenDetector.nativeStrengths(samples, start, end)) {
            return null;
        }
        boolean sampledTransform = false;
        for (int f = start; f <= end; f++) {
            sampledTransform |= samples.get(f).matrix[1] != 0 || samples.get(f).matrix[2] != 0;
        }
        if (sampledTransform) {
            for (int f = start; f <= end; f++) {
                if (sampledTransform(samples.get(f)) == null) {
                    // CS6's fractional-angle lookup does not reproduce the
                    // mathematical matrix. Preserve the original DOM frames
                    // instead of accepting a fit that only works with sin/cos.
                    return null;
                }
            }
        }
        AlphaCurve alpha = fitAlpha(samples, start, end);
        if (alpha != null && !sampledTransform) {
            return writeAlphaMotion(samples, start, end, frameRate, registrationPoint, alpha);
        }
        int firstFrame = samples.get(start).index;
        int frameCount = samples.get(end).index - firstFrame;
        double[] first = samples.get(start).values;
        int[] components = new int[first.length];
        for (int component = 0; component < components.length; component++) {
            components[component] = component;
        }
        double[][] curves = new double[components.length][4];
        NativeTimeMap[] maps = new NativeTimeMap[components.length];
        boolean[] sampledColors = new boolean[4];
        List<List<Segment>> pieces = new ArrayList<>();
        for (int component : components) {
            pieces.add(null);
        }
        double largestChange = 0;
        // A cheap cubic interpolation classifies each channel before the
        // full least-squares pass. This matters when
        // searching long timelines for a shorter recoverable tween.
        int left = start + (end - start) / 3;
        int right = start + 2 * (end - start) / 3;
        double ta = (samples.get(left).index - firstFrame) / (double) frameCount;
        double tb = (samples.get(right).index - firstFrame) / (double) frameCount;
        double aa = ta * (1 - ta);
        double ab = aa * (2 * ta - 1);
        double ba = tb * (1 - tb);
        double bb = ba * (2 * tb - 1);
        double det = aa * bb - ab * ba;
        boolean[] cubicCandidates = new boolean[components.length];
        for (int component : components) {
            cubicCandidates[component] = true;
            double delta = channelValue(samples.get(end), component) - channelValue(samples.get(start), component);
            double ya = channelValue(samples.get(left), component) - channelValue(samples.get(start), component) - delta * ta;
            double yb = channelValue(samples.get(right), component) - channelValue(samples.get(start), component) - delta * tb;
            double quadratic = (ya * bb - yb * ab) / det;
            double cubic = (yb * aa - ya * ba) / det;
            for (int offset = 1; offset <= Math.min(4, end - start - 1); offset++) {
                for (int f : new int[]{start + offset, end - offset}) {
                    double t = (samples.get(f).index - firstFrame) / (double) frameCount;
                    double expected = channelValue(samples.get(start), component) + delta * t
                            + t * (1 - t) * (quadratic + cubic * (2 * t - 1));
                    double tolerance = tolerance(component, samples.get(f));
                    if (Math.abs(channelValue(samples.get(f), component) - expected) > 4 * tolerance) {
                        cubicCandidates[component] = false;
                    }
                }
            }
        }
        for (int property = 0; property < curves.length; property++) {
            int component = components[property];
            double begin = channelValue(samples.get(start), component);
            double finish = channelValue(samples.get(end), component);
            boolean constant = true;
            for (int f = start; f <= end; f++) {
                double value = channelValue(samples.get(f), component);
                constant &= value == begin;
                largestChange = Math.max(largestChange, Math.abs(value - begin) / tolerance(component, samples.get(f)));
            }
            if (constant) {
                curves[property] = new double[]{begin, begin, begin, begin};
                continue;
            }
            if (component < 14) {
                Integer strength = fitQuadratic(samples, start, end, component);
                if (strength != null && strength != 0) {
                    maps[property] = new NativeTimeMap(NativeTimeMap.Type.QUADRATIC, strength);
                    curves[property] = new double[]{begin, begin + (finish - begin) / 3,
                        begin + 2 * (finish - begin) / 3, finish};
                    continue;
                }
            }
            if (component < 14) {
                NativeTimeMap polynomial = fitNativePolynomial(samples, start, end, component);
                if (polynomial != null) {
                    maps[property] = polynomial;
                    curves[property] = new double[]{begin, begin + (finish - begin) / 3,
                        begin + 2 * (finish - begin) / 3, finish};
                    continue;
                }
                NativeCurve nativeCurve = fitNativeWave(samples, start, end, component);
                if (nativeCurve != null) {
                    maps[property] = nativeCurve.map;
                    double delta = nativeCurve.delta;
                    curves[property] = new double[]{begin, begin + delta / 3, begin + 2 * delta / 3, begin + delta};
                    continue;
                }
            }
            if (!cubicCandidates[component]) {
                Integer strength = fitDualQuadratic(samples, start, end, component);
                if (strength == null) {
                    List<Segment> segments = fitPreset(samples, start, end, component);
                    if (segments == null) {
                        segments = fitSegments(samples, start, end, component);
                    }
                    if (segments == null) {
                        return null;
                    }
                    pieces.set(property, segments);
                } else {
                    maps[property] = new NativeTimeMap(NativeTimeMap.Type.DUAL_QUADRATIC, strength);
                }
                curves[property] = new double[]{begin, begin + (finish - begin) / 3,
                    begin + 2 * (finish - begin) / 3, finish};
                continue;
            }
            double sumAA = 0;
            double sumBB = 0;
            double sumAB = 0;
            double sumAY = 0;
            double sumBY = 0;
            for (int f = start; f < end; f++) {
                ClassicTweenDetector.Sample sample = samples.get(f);
                double value = channelValue(sample, component);
                largestChange = Math.max(largestChange, Math.abs(value - begin) / tolerance(component, sample));
                // Coalesced keyframes represent identical samples at every
                // held frame, not an interval with missing observations.
                for (int frame = sample.index; frame < sample.index + sample.duration; frame++) {
                    double t = (frame - firstFrame) / (double) frameCount;
                    double a = t * (1 - t);
                    double b = a * (2 * t - 1);
                    double residual = value - begin - (finish - begin) * t;
                    sumAA += a * a;
                    sumBB += b * b;
                    sumAB += a * b;
                    sumAY += a * residual;
                    sumBY += b * residual;
                }
            }
            double determinant = sumAA * sumBB - sumAB * sumAB;
            double quadratic = (sumAY * sumBB - sumBY * sumAB) / determinant;
            double cubic = (sumBY * sumAA - sumAY * sumAB) / determinant;
            curves[property] = new double[]{begin,
                begin + (finish - begin + quadratic - cubic) / 3,
                finish - (finish - begin - quadratic - cubic) / 3, finish};
            if (!matchesChannel(samples, start, end, component, curves[property], 0)) {
                Integer strength = fitDualQuadratic(samples, start, end, component);
                if (strength == null) {
                    List<Segment> segments = fitPreset(samples, start, end, component);
                    if (segments == null) {
                        segments = fitSegments(samples, start, end, component);
                    }
                    if (segments == null) {
                        return null;
                    }
                    pieces.set(property, segments);
                } else {
                    maps[property] = new NativeTimeMap(NativeTimeMap.Type.DUAL_QUADRATIC, strength);
                }
                curves[property] = new double[]{begin, begin + (finish - begin) / 3,
                    begin + 2 * (finish - begin) / 3, finish};
            }
        }
        if (largestChange < 4) {
            return null;
        }
        // Paired spatial cubics have different native timing from independent
        // scalar curves. Keys at every SWF frame preserve their sampled timing
        // inside one motion object, without relying on spatial interpolation.
        int xKeys = pieces.get(0) == null ? 2 : pieces.get(0).size() + 1;
        int yKeys = pieces.get(1) == null ? 2 : pieces.get(1).size() + 1;
        boolean sampledPosition = xKeys == yKeys
                && !constant(curves[0]) && !constant(curves[1])
                && (pieces.get(0) != null || pieces.get(1) != null
                    || !linear(curves[0]) || !linear(curves[1]));
        // Verify actual matrices, not just the fitted decomposition. This also
        // accounts for SWF fixed-point rounding in scales and angles.
        for (int f = start; f <= end; f++) {
            ClassicTweenDetector.Sample sample = samples.get(f);
            int lastFrame = f == end ? sample.index : sample.index + sample.duration - 1;
            for (int frame = sample.index; frame <= lastFrame; frame++) {
                double t = (frame - firstFrame) / (double) frameCount;
                // Validate each independent color curve at SWF precision. Irregular
                // color changes must not be hidden by otherwise smooth motion.
                for (int property = 6; property < curves.length; property++) {
                    int component = components[property];
                    double predicted = evaluateChannel(curves[property], maps[property], pieces.get(property), t);
                    if (Math.abs(sample.values[component] - predicted) > tolerance(component, sample)) {
                        return null;
                    }
                    // CS6 interpolates changing percentage channels in whole
                    // percentages and clips them to 0..100. A mathematical
                    // fixed8 fit alone therefore cannot establish fidelity.
                    boolean changing = pieces.get(property) != null || !constant(curves[property]);
                    if (component < 10 && changing && frame != firstFrame
                            && frame != samples.get(end).index) {
                        double percent = Math.max(0, Math.min(100, Math.floor(predicted * 100)));
                        double published = Math.round(percent * 256 / 100) / 256.0;
                        if (Math.abs(sample.values[component] - published) > 1.0 / 256) {
                            sampledColors[property - 6] = true;
                        }
                    }
                }

                if (!matchesPosition(sample.values[0], evaluateChannel(curves[0], maps[0], pieces.get(0), t), maps[0])
                        || !matchesPosition(sample.values[1], evaluateChannel(curves[1], maps[1], pieces.get(1), t), maps[1])) {
                    return null;
                }
                double sx = evaluateChannel(curves[2], maps[2], pieces.get(2), t);
                double sy = evaluateChannel(curves[3], maps[3], pieces.get(3), t);
                double rotation = evaluateChannel(curves[4], maps[4], pieces.get(4), t);
                double yRotation = rotation + evaluateChannel(curves[5], maps[5], pieces.get(5), t);
                double[] matrix = {sx * Math.cos(rotation), sx * Math.sin(rotation),
                    -sy * Math.sin(yRotation), sy * Math.cos(yRotation)};
                if (sx <= 0 || sy == 0) {
                    return null;
                }
                for (int i = 0; i < matrix.length; i++) {
                    if (Math.abs(sample.matrix[i] - matrix[i]) > MATRIX_TOLERANCE) {
                        return null;
                    }
                }
            }
        }
        // Motion XML stores relative displacement, absolute scale
        // percentages, and rotation in degrees. Keep its registration point.
        for (int i = 0; i < 4; i++) {
            curves[0][i] -= first[0];
            curves[1][i] -= first[1];
            curves[2][i] *= 100;
            curves[3][i] *= 100;
            curves[5][i] = Math.toDegrees(curves[5][i]);
            curves[4][i] = Math.toDegrees(curves[4][i]);
        }
        for (int property = 0; property < 6; property++) {
            if (pieces.get(property) == null) {
                continue;
            }
            for (Segment segment : pieces.get(property)) {
                for (int i = 0; i < segment.curve.length; i++) {
                    if (property < 2) {
                        segment.curve[i] -= first[property];
                    } else if (property < 4) {
                        segment.curve[i] *= 100;
                    } else {
                        segment.curve[i] = Math.toDegrees(segment.curve[i]);
                    }
                }
            }
        }
        int lastTime = (samples.get(end).index - samples.get(start).index) * 1000;
        XFLXmlWriter writer = new XFLXmlWriter();
        writer.writeStartElement("motionObjectXML");
        writer.writeStartElement("AnimationCore", new String[]{"TimeScale", Long.toString(Math.round(frameRate * 1000)),
            "Version", "1", "duration", Long.toString((long) lastTime + 1000)});
        writer.writeEmptyElement("TimeMap", new String[]{"strength", "0", "type", "Quadratic"});
        int[] mapIndices = new int[maps.length];
        List<NativeTimeMap> timeMaps = new ArrayList<>();
        // Sampled properties ignore TimeMap; emit only maps actually consumed.
        for (int property = 0; property < Math.min(maps.length, 14); property++) {
            if (maps[property] == null || property < 2 && sampledPosition
                    || property >= 2 && property < 6 && sampledTransform
                    || property >= 6 && property < 10 && sampledColors[property - 6]) {
                continue;
            }
            int index = timeMaps.indexOf(maps[property]);
            if (index < 0) {
                index = timeMaps.size();
                timeMaps.add(maps[property]);
                writer.writeEmptyElement("TimeMap", new String[]{"strength",
                    Integer.toString(maps[property].strength), "type", maps[property].type.xmlName});
            }
            mapIndices[property] = index + 1;
        }
        writer.writeStartElement("metadata");
        writer.writeStartElement("names");
        writer.writeEmptyElement("name", new String[]{"langID", "en_US", "value", ""});
        writer.writeEndElement();
        writer.writeEmptyElement("Settings", new String[]{"orientToPath", "0",
            "xformPtXOffsetPct", Double.toString(registrationPoint[0]),
            "xformPtYOffsetPct", Double.toString(registrationPoint[1]), "xformPtZOffsetPixels", "0"});
        writer.writeEndElement();
        writer.writeStartElement("PropertyContainer", new String[]{"id", "headContainer"});
        writer.writeStartElement("PropertyContainer", new String[]{"id", "Basic_Motion"});
        if (sampledPosition) {
            writeSampledProperty(writer, "Motion_X", samples, start, end, 0);
            writeSampledProperty(writer, "Motion_Y", samples, start, end, 1);
        } else {
            writeChannel(writer, "Motion_X", curves[0], lastTime, mapIndices[0], pieces.get(0), 1);
            writeChannel(writer, "Motion_Y", curves[1], lastTime, mapIndices[1], pieces.get(1), 1);
        }
        if (sampledTransform) {
            writeProperty(writer, "Rotation_Z", new double[4], lastTime);
        } else {
            writeChannel(writer, "Rotation_Z", curves[4], lastTime, mapIndices[4], pieces.get(4), 1);
        }
        writer.writeEndElement();
        writer.writeStartElement("PropertyContainer", new String[]{"id", "Transformation"});
        if (sampledTransform) {
            writeSampledProperty(writer, "Skew_X", samples, start, end, 5);
            writeSampledProperty(writer, "Scale_X", samples, start, end, 2);
            writeSampledProperty(writer, "Scale_Y", samples, start, end, 3);
        } else {
            writeChannel(writer, "Skew_X", curves[5], lastTime, mapIndices[5], pieces.get(5), 1);
            writeChannel(writer, "Scale_X", curves[2], lastTime, mapIndices[2], pieces.get(2), 1);
            writeChannel(writer, "Scale_Y", curves[3], lastTime, mapIndices[3], pieces.get(3), 1);
        }
        if (sampledTransform) {
            writeSampledProperty(writer, "Skew_Y", samples, start, end, 4);
        } else {
            writeProperty(writer, "Skew_Y", new double[4], lastTime);
        }
        writer.writeEndElement();
        writer.writeStartElement("PropertyContainer", new String[]{"id", "Colors"});
        // Emit the full transform, including constant channels: a partially
        // specified advanced effect would reset their values to defaults.
        writer.writeStartElement("PropertyContainer", new String[]{"id", "Advanced_ColorXform"});
        for (int property = 0; property < COLOR_PROPERTIES.length; property++) {
            double[] curve = curves[property + 6].clone();
            if (property < 4) {
                for (int i = 0; i < curve.length; i++) {
                    curve[i] *= 100;
                }
            }
            if (property < 4 && sampledColors[property]) {
                writeSampledProperty(writer, COLOR_PROPERTIES[property], samples, start, end, property + 6);
            } else {
                writeChannel(writer, COLOR_PROPERTIES[property], curve, lastTime, mapIndices[property + 6],
                        pieces.get(property + 6), property < 4 ? 100 : 1);
            }
        }
        writer.writeEndElement();
        writer.writeEndElement();
        FilterTweenDetector.write(writer, samples, start, end);
        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndElement();
        return writer.toString();
    }

    /**
     * Property keys, rather than DOM keyframes: the symbol remains in a single
     * motion object. Held SWF frames also get keys, so Adobe cannot interpolate
     * through an interval that was constant in the source.
     */
    private static void writeSampledProperty(XFLXmlWriter writer, String id,
            List<ClassicTweenDetector.Sample> samples, int start, int end, int component) throws XMLStreamException {
        writer.writeStartElement("Property", new String[]{"id", id, "enabled", "1", "readonly", "0",
            "visible", "1", "ignoreTimeMap", "1"});
        double begin = sampledValue(samples.get(start), samples.get(start), component);
        boolean changing = false;
        for (int f = start + 1; f <= end; f++) {
            changing |= sampledValue(samples.get(f), samples.get(start), component) != begin;
        }
        for (int f = start; f <= (changing ? end : start); f++) {
            ClassicTweenDetector.Sample sample = samples.get(f);
            int lastFrame = f == end || !changing ? sample.index : sample.index + sample.duration - 1;
            for (int frame = sample.index; frame <= lastFrame; frame++) {
                String anchor = point(0, sampledValue(sample, samples.get(start), component));
                writer.writeEmptyElement("Keyframe", new String[]{"timevalue",
                    Long.toString((long) (frame - samples.get(start).index) * 1000), "roving", "0",
                    "anchor", anchor, "previous", anchor, "next", anchor});
            }
        }
        writer.writeEndElement();
    }

    private static double sampledValue(ClassicTweenDetector.Sample sample,
            ClassicTweenDetector.Sample first, int component) {
        if (component < 2) {
            return sample.values[component] - first.values[component];
        }
        if (component < 6) {
            return sampledTransform(sample)[component - 2];
        }
        if (component < 10) {
            // Native publishes quantize even interior property keys to whole
            // percentages. Choose the closest representable fixed8 value.
            // The small positive margin keeps XML float conversion above
            // the selected integer-percent boundary. Do not clamp: native
            // keys preserve multipliers outside 0..100.
            double percent = sample.values[component] * 100;
            double lower = Math.floor(percent);
            double upper = Math.ceil(percent);
            double expected = Math.rint(sample.values[component] * 256);
            double lowerError = Math.abs(Math.round(lower * 256 / 100) - expected);
            double upperError = Math.abs(Math.round(upper * 256 / 100) - expected);
            return (lowerError <= upperError ? lower : upper) + 0.001;
        }
        return sample.values[component];
    }

    /**
     * CS6 publishes fractional angles using a quarter-degree sine table with
     * discontinuous interpolation between entries. Only emit table entries
     * that reproduce the observed matrix at SWF precision. Validate each axis
     * separately so a harmless scale snap cannot reject an otherwise exact
     * angle. Keep degrees in the returned array: converting through radians
     * again can move an exact lookup-table boundary just below that boundary.
     */
    private static double[] sampledTransform(ClassicTweenDetector.Sample sample) {
        double[] result = new double[4];
        for (int axis = 0; axis < 2; axis++) {
            double scale = sample.values[2 + axis];
            double degrees = Math.rint(Math.toDegrees(sample.values[4 + axis]) * 4) / 4;
            double angle = Math.toRadians(degrees);
            double first = axis == 0 ? Math.cos(angle) : -Math.sin(angle);
            double second = axis == 0 ? Math.sin(angle) : Math.cos(angle);
            double snappedScale = Math.rint(scale * 100) / 100;
            int offset = axis * 2;
            if (Math.abs(snappedScale - scale) < 0.00001
                    && Math.abs(snappedScale * first - sample.matrix[offset]) <= MATRIX_TOLERANCE
                    && Math.abs(snappedScale * second - sample.matrix[offset + 1]) <= MATRIX_TOLERANCE) {
                scale = snappedScale;
            }
            if (Math.abs(scale * first - sample.matrix[offset]) > MATRIX_TOLERANCE
                    || Math.abs(scale * second - sample.matrix[offset + 1]) > MATRIX_TOLERANCE) {
                return null;
            }
            result[axis] = scale * 100;
            result[2 + axis] = degrees;
        }
        return result;
    }

    private static String point(double time, double value) {
        // Flash's XML import should never need to parse scientific notation
        // for coordinates or angles that are effectively zero.
        return decimal(time) + "," + decimal(value);
    }

    private static String decimal(double value) {
        return java.math.BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }

    private static final class Segment {
        final double start;
        final double end;
        final double[] curve;

        Segment(double start, double end, double[] curve) {
            this.start = start;
            this.end = end;
            this.curve = curve;
        }
    }

    private static double evaluateChannel(double[] curve, NativeTimeMap map, List<Segment> segments, double t) {
        if (segments == null) {
            return evaluate(curve, map == null ? t : map.progress(t));
        }
        for (Segment segment : segments) {
            if (t <= segment.end + 1e-12) {
                return evaluate(segment.curve, (t - segment.start) / (segment.end - segment.start));
            }
        }
        return segments.get(segments.size() - 1).curve[3];
    }

    private static final class NativeCurve {
        final NativeTimeMap map;
        final double delta;

        NativeCurve(NativeTimeMap map, double delta) {
            this.map = map;
            this.delta = delta;
        }
    }

    private static ClassicTweenDetector.Sample copySample(ClassicTweenDetector.Sample source) {
        ClassicTweenDetector.Sample copy = new ClassicTweenDetector.Sample();
        copy.xml = source.xml;
        copy.index = source.index;
        copy.duration = source.duration;
        copy.identity = source.identity;
        copy.values = source.values;
        copy.matrix = source.matrix;
        copy.filters = source.filters;
        return copy;
    }

    private static boolean matchesPosition(double actual, double predicted, NativeTimeMap map) {
        if (map != null && (map.isWave() || map.isPolynomial())) {
            // Native measurements establish one twip after rounding, which
            // can exceed 0.051 px when compared to an unrounded formula.
            return Math.abs(Math.round(actual * 20) - Math.round(predicted * 20)) <= 1;
        }
        return Math.abs(actual - predicted) <= POSITION_TOLERANCE;
    }

    /** Recover native polynomial maps before generic cubic/piecewise fitting. */
    private static NativeTimeMap fitNativePolynomial(List<ClassicTweenDetector.Sample> samples,
            int start, int end, int component) {
        if (end - start < 10) {
            return null;
        }
        int firstFrame = samples.get(start).index;
        int intervals = samples.get(end).index - firstFrame;
        double begin = channelValue(samples.get(start), component);
        double delta = channelValue(samples.get(end), component) - begin;
        if (Math.abs(delta) < 4 * tolerance(component, samples.get(start))) {
            return null;
        }
        for (NativeTimeMap.Type type : new NativeTimeMap.Type[]{NativeTimeMap.Type.CUBIC,
            NativeTimeMap.Type.QUARTIC, NativeTimeMap.Type.QUINTIC, NativeTimeMap.Type.DUAL_CUBIC,
            NativeTimeMap.Type.DUAL_QUARTIC, NativeTimeMap.Type.DUAL_QUINTIC}) {
            // Native CS6 captures validate strength 50 only. Other strengths
            // retain existing curves until their native semantics are measured.
            NativeTimeMap map = new NativeTimeMap(type, 50);
            boolean valid = true;
            for (int f = start; f <= end && valid; f++) {
                ClassicTweenDetector.Sample sample = samples.get(f);
                int lastFrame = f == end ? sample.index : sample.index + sample.duration - 1;
                for (int frame = sample.index; frame <= lastFrame; frame++) {
                    double predicted = begin + delta * map.progress((frame - firstFrame) / (double) intervals);
                    double actual = channelValue(sample, component);
                    if (component < 2 ? !matchesPosition(actual, predicted, map)
                            : Math.abs(actual - predicted) > tolerance(component, sample)) {
                        valid = false;
                        break;
                    }
                }
            }
            if (valid) {
                return map;
            }
        }
        return null;
    }

    /** Recover the underlying endpoint, which differs from the final SWF pose for waves. */
    private static NativeCurve fitNativeWave(List<ClassicTweenDetector.Sample> samples,
            int start, int end, int component) {
        if (end - start < 10) {
            return null;
        }
        int firstFrame = samples.get(start).index;
        int intervals = samples.get(end).index - firstFrame;
        double begin = channelValue(samples.get(start), component);
        for (NativeTimeMap.Type type : new NativeTimeMap.Type[]{NativeTimeMap.Type.SPRING,
            NativeTimeMap.Type.BOUNCE, NativeTimeMap.Type.BOUNCE_IN}) {
            // Only strengths independently measured in native CS6 are enabled.
            for (int strength : new int[]{1, 2, 3, 4, 5, 6, 8}) {
                NativeTimeMap map = new NativeTimeMap(type, strength, intervals);
                double lower = Double.NEGATIVE_INFINITY;
                double upper = Double.POSITIVE_INFINITY;
                double sumPV = 0;
                double sumPP = 0;
                boolean valid = true;
                for (int f = start; f <= end && valid; f++) {
                    ClassicTweenDetector.Sample sample = samples.get(f);
                    int lastFrame = f == end ? sample.index : sample.index + sample.duration - 1;
                    double value = channelValue(sample, component) - begin;
                    double error = component < 2 ? 0.075 : tolerance(component, sample);
                    for (int frame = sample.index; frame <= lastFrame; frame++) {
                        double p = map.progress((frame - firstFrame) / (double) intervals);
                        if (Math.abs(p) < 1e-12) {
                            if (Math.abs(value) > error) {
                                valid = false;
                                break;
                            }
                        } else {
                            double a = (value - error) / p;
                            double b = (value + error) / p;
                            lower = Math.max(lower, Math.min(a, b));
                            upper = Math.min(upper, Math.max(a, b));
                            if (lower > upper) {
                                valid = false;
                                break;
                            }
                        }
                        sumPV += p * value;
                        sumPP += p * p;
                    }
                }
                if (valid && sumPP > 0) {
                    double delta = Math.max(lower, Math.min(upper, sumPV / sumPP));
                    // Prefer recoverable SWF-grid endpoints to tiny amplitude
                    // shifts caused by native truncation of sampled positions.
                    double unit = component < 2 ? 20 : component < 4 ? 65536 : 0;
                    if (unit != 0) {
                        double rounded = Math.rint((begin + delta) * unit) / unit - begin;
                        if (rounded >= lower && rounded <= upper) {
                            delta = rounded;
                        }
                    }
                    return new NativeCurve(map, delta);
                }
            }
        }
        return null;
    }

    /** Recognize a preset before accepting dense keys; arbitrary animation still fails. */
    private static List<Segment> fitPreset(List<ClassicTweenDetector.Sample> samples, int start, int end,
            int component) {
        if (end - start < 10) {
            return null;
        }
        double begin = channelValue(samples.get(start), component);
        double delta = channelValue(samples.get(end), component) - begin;
        if (Math.abs(delta) < 4 * tolerance(component, samples.get(start))) {
            return null;
        }
        int firstFrame = samples.get(start).index;
        int duration = samples.get(end).index - firstFrame;
        for (PresetEasing.Family family : PresetEasing.Family.values()) {
            for (int direction = 0; direction < 3; direction++) {
                // Intersect the permitted blend amounts, including every held
                // SWF frame. Strength is continuous here: output stores samples,
                // rather than guessing an Adobe TimeMap name or strength scale.
                double lower = 0.01;
                double upper = 1;
                boolean matches = true;
                for (int f = start; f <= end && matches; f++) {
                    ClassicTweenDetector.Sample sample = samples.get(f);
                    int last = f == end ? sample.index : sample.index + sample.duration - 1;
                    for (int frame = sample.index; frame <= last; frame++) {
                        double t = (frame - firstFrame) / (double) duration;
                        double h = delta * (PresetEasing.progress(family, direction, t) - t);
                        double residual = channelValue(sample, component) - begin - delta * t;
                        double error = tolerance(component, sample);
                        if (Math.abs(h) < 1e-12) {
                            matches = Math.abs(residual) <= error;
                        } else {
                            double a = (residual - error) / h;
                            double b = (residual + error) / h;
                            lower = Math.max(lower, Math.min(a, b));
                            upper = Math.min(upper, Math.max(a, b));
                            matches = lower <= upper;
                        }
                        if (!matches) {
                            break;
                        }
                    }
                }
                if (!matches) {
                    continue;
                }
                // Linear handles between exact observations use the established
                // motion XML schema and preserve overshoots and sharp bounces.
                List<Segment> segments = new ArrayList<>();
                for (int f = start; f < end; f++) {
                    ClassicTweenDetector.Sample sample = samples.get(f);
                    double value = channelValue(sample, component);
                    for (int frame = sample.index; frame < sample.index + sample.duration; frame++) {
                        double next = frame + 1 < sample.index + sample.duration
                                ? value : channelValue(samples.get(f + 1), component);
                        segments.add(new Segment((frame - firstFrame) / (double) duration,
                                (frame + 1 - firstFrame) / (double) duration,
                                new double[]{value, value + (next - value) / 3,
                                    value + 2 * (next - value) / 3, next}));
                    }
                }
                return segments;
            }
        }
        return null;
    }

    private static List<Segment> fitSegments(List<ClassicTweenDetector.Sample> samples, int start, int end,
            int component) {
        // Each segment needs six distinct observations, including both ends.
        // Bound the number of segments to avoid turning arbitrary keyframes
        // into an almost one-key-per-frame motion object.
        if (end - start < 10) {
            return null;
        }
        int firstFrame = samples.get(start).index;
        double duration = samples.get(end).index - firstFrame;
        List<Segment> result = new ArrayList<>();
        int current = start;
        while (current < end && result.size() < 8) {
            double[] best = null;
            int finish = end;
            for (; finish >= current + 5; finish--) {
                if (finish != end && end - finish < 5) {
                    continue;
                }
                best = fitCubicSegment(samples, current, finish, component);
                if (best != null) {
                    break;
                }
            }
            if (best == null) {
                return null;
            }
            result.add(new Segment((samples.get(current).index - firstFrame) / duration,
                    (samples.get(finish).index - firstFrame) / duration, best));
            current = finish;
        }
        return current == end && result.size() > 1 ? result : null;
    }

    private static double[] fitCubicSegment(List<ClassicTweenDetector.Sample> samples, int start, int end,
            int component) {
        double begin = channelValue(samples.get(start), component);
        double finish = channelValue(samples.get(end), component);
        int firstFrame = samples.get(start).index;
        double duration = samples.get(end).index - firstFrame;
        int left = start + (end - start) / 3;
        int right = start + 2 * (end - start) / 3;
        double ta = (samples.get(left).index - firstFrame) / duration;
        double tb = (samples.get(right).index - firstFrame) / duration;
        double aa = ta * (1 - ta);
        double ab = aa * (2 * ta - 1);
        double ba = tb * (1 - tb);
        double bb = ba * (2 * tb - 1);
        double det = aa * bb - ab * ba;
        double ya = channelValue(samples.get(left), component) - begin - (finish - begin) * ta;
        double yb = channelValue(samples.get(right), component) - begin - (finish - begin) * tb;
        double quadratic = (ya * bb - yb * ab) / det;
        double cubic = (yb * aa - ya * ba) / det;
        for (int offset = 1; offset <= Math.min(4, end - start - 1); offset++) {
            for (int f : new int[]{start + offset, end - offset}) {
                double t = (samples.get(f).index - firstFrame) / duration;
                double value = begin + (finish - begin) * t
                        + t * (1 - t) * (quadratic + cubic * (2 * t - 1));
                if (Math.abs(value - channelValue(samples.get(f), component)) > 4 * tolerance(component, samples.get(f))) {
                    return null;
                }
            }
        }
        double sumAA = 0;
        double sumBB = 0;
        double sumAB = 0;
        double sumAY = 0;
        double sumBY = 0;
        for (int f = start; f < end; f++) {
            ClassicTweenDetector.Sample sample = samples.get(f);
            for (int frame = sample.index; frame < sample.index + sample.duration; frame++) {
                double t = (frame - firstFrame) / duration;
                double a = t * (1 - t);
                double b = a * (2 * t - 1);
                double residual = channelValue(sample, component) - begin - (finish - begin) * t;
                sumAA += a * a;
                sumBB += b * b;
                sumAB += a * b;
                sumAY += a * residual;
                sumBY += b * residual;
            }
        }
        double determinant = sumAA * sumBB - sumAB * sumAB;
        quadratic = (sumAY * sumBB - sumBY * sumAB) / determinant;
        cubic = (sumBY * sumAA - sumAY * sumAB) / determinant;
        double[] curve = {begin, begin + (finish - begin + quadratic - cubic) / 3,
            finish - (finish - begin - quadratic - cubic) / 3, finish};
        return matchesChannel(samples, start, end, component, curve, 0) ? curve : null;
    }

    private static void writeChannel(XFLXmlWriter writer, String id, double[] curve, int lastTime,
            int mapIndex, List<Segment> segments, double scale) throws XMLStreamException {
        if (segments == null) {
            writeProperty(writer, id, curve, lastTime, mapIndex);
            return;
        }
        writer.writeStartElement("Property", new String[]{"id", id, "enabled", "1", "readonly", "0",
            "visible", "1", "ignoreTimeMap", "1"});
        for (int key = 0; key <= segments.size(); key++) {
            Segment previous = key == 0 ? null : segments.get(key - 1);
            Segment next = key == segments.size() ? null : segments.get(key);
            double t = next == null ? previous.end : next.start;
            double anchor = (next == null ? previous.curve[3] : next.curve[0]) * scale;
            double prevTime = previous == null ? 0 : -(previous.end - previous.start) * lastTime / 3;
            double nextTime = next == null ? 0 : (next.end - next.start) * lastTime / 3;
            double prevValue = previous == null ? anchor : previous.curve[2] * scale;
            double nextValue = next == null ? anchor : next.curve[1] * scale;
            writer.writeEmptyElement("Keyframe", new String[]{"timevalue", Long.toString(Math.round(t * lastTime)),
                "roving", "0", "anchor", point(0, anchor), "previous", point(prevTime, prevValue),
                "next", point(nextTime, nextValue)});
        }
        writer.writeEndElement();
    }

    private static double channelValue(ClassicTweenDetector.Sample sample, int component) {
        // Canonical decomposition: rotation is the X axis angle, Skew_X is
        // the difference between the Y and X axis angles; Skew_Y stays zero.
        return component == 5 ? sample.values[5] - sample.values[4] : sample.values[component];
    }

    private static double progress(double t, int strength) {
        return t + strength / 100.0 * (t < 0.5 ? t : 1 - t) * (1 - 2 * t);
    }

    private static boolean matchesChannel(List<ClassicTweenDetector.Sample> samples, int start, int end,
            int component, double[] curve, int strength) {
        int firstFrame = samples.get(start).index;
        int duration = samples.get(end).index - firstFrame;
        for (int f = start; f <= end; f++) {
            ClassicTweenDetector.Sample sample = samples.get(f);
            int lastFrame = f == end ? sample.index : sample.index + sample.duration - 1;
            for (int frame = sample.index; frame <= lastFrame; frame++) {
                double t = (frame - firstFrame) / (double) duration;
                if (Math.abs(channelValue(sample, component) - evaluate(curve, progress(t, strength)))
                        > tolerance(component, sample)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static Integer fitQuadratic(List<ClassicTweenDetector.Sample> samples, int start, int end,
            int component) {
        double begin = channelValue(samples.get(start), component);
        double delta = channelValue(samples.get(end), component) - begin;
        if (Math.abs(delta) < 4 * tolerance(component, samples.get(start))) {
            return null;
        }
        int firstFrame = samples.get(start).index;
        int duration = samples.get(end).index - firstFrame;
        double lower = -100;
        double upper = 100;
        double sumHH = 0;
        double sumHY = 0;
        for (int f = start; f <= end; f++) {
            ClassicTweenDetector.Sample sample = samples.get(f);
            int last = f == end ? sample.index : sample.index + sample.duration - 1;
            for (int frame = sample.index; frame <= last; frame++) {
                double t = (frame - firstFrame) / (double) duration;
                double h = delta * t * (1 - t) / 100;
                double residual = channelValue(sample, component) - begin - delta * t;
                double error = tolerance(component, sample);
                if (Math.abs(h) < 1e-12) {
                    if (Math.abs(residual) > error) {
                        return null;
                    }
                    continue;
                }
                double a = (residual - error) / h;
                double b = (residual + error) / h;
                lower = Math.max(lower, Math.min(a, b));
                upper = Math.min(upper, Math.max(a, b));
                if (lower > upper) {
                    return null;
                }
                sumHH += h * h;
                sumHY += h * residual;
            }
        }
        int minimum = (int) Math.ceil(lower);
        int maximum = (int) Math.floor(upper);
        if (minimum > maximum || sumHH == 0) {
            return null;
        }
        return Math.max(minimum, Math.min(maximum, (int) Math.round(sumHY / sumHH)));
    }

    private static Integer fitDualQuadratic(List<ClassicTweenDetector.Sample> samples, int start, int end,
            int component) {
        double begin = channelValue(samples.get(start), component);
        double finish = channelValue(samples.get(end), component);
        if (Math.abs(finish - begin) < tolerance(component, samples.get(start))) {
            return null;
        }
        // Intersect the strength intervals permitted by all quantized samples.
        // This avoids trying 201 strengths for every rejected candidate span.
        double minimum = -100;
        double maximum = 100;
        double sumHH = 0;
        double sumHY = 0;
        int firstFrame = samples.get(start).index;
        int duration = samples.get(end).index - firstFrame;
        for (int f = start; f <= end; f++) {
            ClassicTweenDetector.Sample sample = samples.get(f);
            int lastFrame = f == end ? sample.index : sample.index + sample.duration - 1;
            for (int frame = sample.index; frame <= lastFrame; frame++) {
                double t = (frame - firstFrame) / (double) duration;
                double h = (finish - begin) * (t < 0.5 ? t : 1 - t) * (1 - 2 * t) / 100;
                double residual = channelValue(sample, component) - begin - (finish - begin) * t;
                double tolerance = tolerance(component, sample);
                if (Math.abs(h) < 1e-12) {
                    if (Math.abs(residual) > tolerance) {
                        return null;
                    }
                    continue;
                }
                double a = (residual - tolerance) / h;
                double b = (residual + tolerance) / h;
                minimum = Math.max(minimum, Math.min(a, b));
                maximum = Math.min(maximum, Math.max(a, b));
                if (minimum > maximum) {
                    return null;
                }
                sumHH += h * h;
                sumHY += h * residual;
            }
        }
        int lower = (int) Math.ceil(minimum);
        int upper = (int) Math.floor(maximum);
        if (lower > upper || sumHH == 0) {
            return null;
        }
        int strength = Math.max(lower, Math.min(upper, (int) Math.round(sumHY / sumHH)));
        double[] curve = {begin, begin + (finish - begin) / 3, begin + 2 * (finish - begin) / 3, finish};
        return matchesChannel(samples, start, end, component, curve, strength) ? strength : null;
    }

    private static double tolerance(int component, ClassicTweenDetector.Sample sample) {
        if (component < 2) {
            return POSITION_TOLERANCE;
        }
        if (component < 4) {
            return MATRIX_TOLERANCE;
        }
        if (component < 6) {
            return component == 4 ? MATRIX_TOLERANCE / sample.values[2]
                    : MATRIX_TOLERANCE * (1 / sample.values[2] + 1 / Math.abs(sample.values[3]));
        }
        return component >= 14 ? FilterTweenDetector.tolerance(sample, component)
                : component < 10 ? 1.0 / 256 : 1;
    }

    private static final class AlphaCurve {
        int strength;
        double begin;
        double finish;
        double error;
    }

    /**
     * Flash quantizes Alpha_Amount to whole percentages before writing the SWF
     * multiplier. Recover the intervals of original percentages, including
     * held frames, rather than fitting already rounded multipliers exactly.
     */
    private static AlphaCurve fitAlpha(List<ClassicTweenDetector.Sample> samples, int start, int end) {
        ClassicTweenDetector.Sample first = samples.get(start);
        int duration = samples.get(end).index - first.index;
        if (duration < 9 || end - start < 7) {
            return null;
        }
        double change = samples.get(end).values[9] - first.values[9];
        if (Math.abs(change) < 0.1) {
            return null;
        }
        List<Integer> offsets = new ArrayList<>();
        List<Integer> percentages = new ArrayList<>();

        double previous = first.values[9];
        for (int i = start; i <= end; i++) {
            ClassicTweenDetector.Sample sample = samples.get(i);
            for (int component = 0; component < first.values.length; component++) {
                if (component != 9 && Math.abs(sample.values[component] - first.values[component]) > 1e-9) {
                    return null;
                }
            }
            double value = sample.values[9];
            if ((value - previous) * change < 0 || value < 0 || value > 1) {
                return null;
            }
            previous = value;
            int percent = (int) Math.round(value * 100);
            if (Math.round(percent * 256 / 100.0) != Math.round(value * 256)) {
                return null;
            }
            int firstFrame = sample.index - first.index;
            int lastFrame = i == end ? firstFrame : firstFrame + sample.duration - 1;
            // For monotone easing, constraints at both ends of a held interval
            // also constrain every frame inside it. Avoid quadratic work in the
            // duration of long, quantized holds.
            offsets.add(firstFrame);
            percentages.add(percent);
            if (lastFrame != firstFrame) {
                offsets.add(lastFrame);
                percentages.add(percent);
            }
        }
        int count = offsets.size();
        double[] lower = new double[count];
        double[] upper = new double[count];
        for (int f = 0; f < count; f++) {
            lower[f] = percentages.get(f);
            // Keep a margin from the next integer percentage. Double-only
            // boundary checks are insufficient when Adobe compiles the curve.
            upper[f] = lower[f] == 100 ? 100 : lower[f] + 1 - 0.001;
        }
        AlphaCurve best = null;
        for (int strength = -100; strength <= 100; strength++) {
            // Near-linear fades are ambiguous and remain classic tweens.
            if (Math.abs(strength) < 10) {
                continue;
            }
            double[] progress = new double[count];
            double minimumSlope = -Double.MAX_VALUE;
            double maximumSlope = Double.MAX_VALUE;
            for (int f = 0; f < count; f++) {
                double t = offsets.get(f) / (double) duration;
                progress[f] = t + strength / 100.0 * (t < 0.5 ? t : 1 - t) * (1 - 2 * t);
            }
            // Each pair of percentage intervals constrains the slope. The
            // intersection gives a feasible endpoint pair without loose error
            // tolerances which could silently alter the SWF alpha samples.
            boolean feasible = true;
            for (int i = 0; i < count && feasible; i++) {
                for (int j = 0; j < count; j++) {
                    double delta = progress[j] - progress[i];
                    double bound = upper[j] - lower[i];
                    if (delta > 1e-12) {
                        maximumSlope = Math.min(maximumSlope, bound / delta);
                    } else if (delta < -1e-12) {
                        minimumSlope = Math.max(minimumSlope, bound / delta);
                    } else if (bound < 0) {
                        feasible = false;
                        break;
                    }
                    if (minimumSlope > maximumSlope + 1e-9) {
                        feasible = false;
                        break;
                    }
                }
            }
            if (!feasible) {
                continue;
            }
            double sumX = 0;
            double sumY = 0;
            double sumXX = 0;
            double sumXY = 0;
            for (int f = 0; f < count; f++) {
                double x = progress[f];
                double y = (lower[f] + upper[f]) / 2;
                sumX += x;
                sumY += y;
                sumXX += x * x;
                sumXY += x * y;
            }

            double slope = (sumXY - sumX * sumY / count) / (sumXX - sumX * sumX / count);
            slope = Math.max(minimumSlope, Math.min(maximumSlope, slope));
            double minimumIntercept = -Double.MAX_VALUE;
            double maximumIntercept = Double.MAX_VALUE;
            for (int f = 0; f < count; f++) {
                minimumIntercept = Math.max(minimumIntercept, lower[f] - slope * progress[f]);
                maximumIntercept = Math.min(maximumIntercept, upper[f] - slope * progress[f]);
            }
            double intercept = Math.max(minimumIntercept,
                    Math.min(maximumIntercept, (sumY - slope * sumX) / count));
            // Prefer exact whole-percent endpoints when they satisfy every
            // sample interval. Fractional endpoints can be normalized during
            // Adobe compilation even when their double/float evaluation fits.
            // In the logo fade-out, 99 -> 0 is feasible and avoids the spurious
            // residual opacity selected by constrained least squares.
            double roundedBegin = Math.rint(intercept);
            double roundedFinish = Math.rint(intercept + slope);
            boolean wholeEndpointsFit = true;
            for (int f = 0; f < count; f++) {
                double value = roundedBegin + (roundedFinish - roundedBegin) * progress[f];
                if (value < lower[f] - 1e-8 || value > upper[f] + 1e-8) {
                    wholeEndpointsFit = false;
                    break;
                }
            }
            if (wholeEndpointsFit) {
                intercept = roundedBegin;
                slope = roundedFinish - roundedBegin;
            }
            double error = 0;
            for (int f = 0; f < count; f++) {
                double value = intercept + slope * progress[f];
                // Verify the actual two-stage Flash alpha quantization.
                long multiplier = Math.round(Math.floor(value + 1e-8) * 256 / 100.0);
                long expected = Math.round(lower[f] * 256 / 100.0);
                if (multiplier != expected) {
                    feasible = false;
                    break;
                }
                double residual = value - (lower[f] + upper[f]) / 2;
                error += residual * residual;
            }
            if (feasible && (best == null || error < best.error)) {
                best = new AlphaCurve();
                best.strength = strength;
                best.begin = intercept;
                best.finish = intercept + slope;
                best.error = error;
            }
        }
        return best;
    }

    private static String writeAlphaMotion(List<ClassicTweenDetector.Sample> samples, int start, int end,
            double frameRate, double[] registrationPoint, AlphaCurve alpha) throws XMLStreamException {
        double[] initial = samples.get(start).values;
        int lastTime = (samples.get(end).index - samples.get(start).index) * 1000;
        XFLXmlWriter writer = new XFLXmlWriter();
        writer.writeStartElement("motionObjectXML");
        writer.writeStartElement("AnimationCore", new String[]{"TimeScale", Long.toString(Math.round(frameRate * 1000)),
            "Version", "1", "duration", Long.toString((long) lastTime + 1000)});
        writer.writeEmptyElement("TimeMap", new String[]{"strength", "0", "type", "Quadratic"});
        writer.writeEmptyElement("TimeMap", new String[]{"strength", Integer.toString(alpha.strength), "type", "DualQuadratic"});
        writer.writeStartElement("metadata");
        writer.writeStartElement("names");
        writer.writeEmptyElement("name", new String[]{"langID", "en_US", "value", ""});
        writer.writeEndElement();
        writer.writeEmptyElement("Settings", new String[]{"orientToPath", "0",
            "xformPtXOffsetPct", Double.toString(registrationPoint[0]),
            "xformPtYOffsetPct", Double.toString(registrationPoint[1]), "xformPtZOffsetPixels", "0"});
        writer.writeEndElement();
        writer.writeStartElement("PropertyContainer", new String[]{"id", "headContainer"});
        writer.writeStartElement("PropertyContainer", new String[]{"id", "Basic_Motion"});
        writeProperty(writer, "Motion_X", new double[4], lastTime);
        writeProperty(writer, "Motion_Y", new double[4], lastTime);
        double rotation = Math.toDegrees(initial[4]);
        writeProperty(writer, "Rotation_Z", new double[]{rotation, rotation, rotation, rotation}, lastTime);
        writer.writeEndElement();
        writer.writeStartElement("PropertyContainer", new String[]{"id", "Transformation"});
        double skew = Math.toDegrees(initial[5] - initial[4]);
        writeProperty(writer, "Skew_X", new double[]{skew, skew, skew, skew}, lastTime);
        writeProperty(writer, "Skew_Y", new double[4], lastTime);
        double sx = initial[2] * 100;
        double sy = initial[3] * 100;
        writeProperty(writer, "Scale_X", new double[]{sx, sx, sx, sx}, lastTime);
        writeProperty(writer, "Scale_Y", new double[]{sy, sy, sy, sy}, lastTime);
        writer.writeEndElement();
        writer.writeStartElement("PropertyContainer", new String[]{"id", "Colors"});
        writer.writeStartElement("PropertyContainer", new String[]{"id", "Alpha_ColorXform"});
        writer.writeStartElement("Property", new String[]{"id", "Alpha_Amount", "enabled", "1", "readonly", "0",
            "visible", "1", "ignoreTimeMap", "0", "TimeMapIndex", "1"});
        writer.writeEmptyElement("Keyframe", new String[]{"timevalue", "0", "roving", "0",
            "anchor", "0," + alpha.begin, "previous", "0," + alpha.begin, "next", "0," + alpha.begin});
        writer.writeEmptyElement("Keyframe", new String[]{"timevalue", Integer.toString(lastTime), "roving", "0",
            "anchor", "0," + alpha.finish, "previous", "0," + alpha.finish, "next", "0," + alpha.finish});
        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndElement();
        FilterTweenDetector.write(writer, samples, start, end);
        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndElement();
        return writer.toString();
    }

    private static void writeProperty(XFLXmlWriter writer, String id, double[] curve, int lastTime) throws XMLStreamException {
        writeProperty(writer, id, curve, lastTime, 0);
    }

    private static void writeProperty(XFLXmlWriter writer, String id, double[] curve, int lastTime,
            int timeMapIndex) throws XMLStreamException {
        writer.writeStartElement("Property", new String[]{"id", id, "enabled", "1", "readonly", "0",
            "visible", "1", "ignoreTimeMap", timeMapIndex == 0 ? "1" : "0"});
        if (timeMapIndex != 0) {
            writer.writeAttribute("TimeMapIndex", Integer.toString(timeMapIndex));
        }
        double handleTime = constant(curve) ? 0 : lastTime / 3.0;
        writer.writeEmptyElement("Keyframe", new String[]{"timevalue", "0", "roving", "0",
            "anchor", point(0, curve[0]), "previous", point(0, curve[0]),
            "next", point(handleTime, curve[1])});
        if (curve[0] != curve[1] || curve[0] != curve[2] || curve[0] != curve[3]) {
            writer.writeEmptyElement("Keyframe", new String[]{"timevalue", Integer.toString(lastTime), "roving", "0",
                "anchor", point(0, curve[3]), "previous", point(-handleTime, curve[2]),
                "next", point(0, curve[3])});
        }
        writer.writeEndElement();
    }

    private static boolean linear(double[] curve) {
        double delta = curve[3] - curve[0];
        return Math.abs(curve[1] - curve[0] - delta / 3) <= POSITION_TOLERANCE
                && Math.abs(curve[2] - curve[0] - 2 * delta / 3) <= POSITION_TOLERANCE;
    }

    private static boolean constant(double[] curve) {
        return curve[0] == curve[1] && curve[0] == curve[2] && curve[0] == curve[3];
    }

    private static double evaluate(double[] curve, double t) {
        double u = 1 - t;
        return u * u * u * curve[0] + 3 * u * u * t * curve[1]
                + 3 * u * t * t * curve[2] + t * t * t * curve[3];
    }
}
