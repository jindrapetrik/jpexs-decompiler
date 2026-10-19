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
package com.jpexs.decompiler.flash.iggy.conversion;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.iggy.IggyObjectData;
import com.jpexs.decompiler.flash.iggy.IggySwf;
import com.jpexs.decompiler.flash.iggy.IggyTimeline;
import com.jpexs.decompiler.flash.tags.DefineMorphShape2Tag;
import com.jpexs.decompiler.flash.tags.DefineShape4Tag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.MorphShapeTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.FILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.LINESTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.LINESTYLEARRAY;
import com.jpexs.decompiler.flash.types.MORPHFILLSTYLE;
import com.jpexs.decompiler.flash.types.MORPHFILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.MORPHLINESTYLE;
import com.jpexs.decompiler.flash.types.MORPHLINESTYLE2;
import com.jpexs.decompiler.flash.types.MORPHLINESTYLEARRAY;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
import com.jpexs.decompiler.flash.types.shaperecords.CurvedEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.EndShapeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Dictionary vector shapes (type 1) and morph shapes (type 2).
 */
public final class IggyVectorConvertor {

    private IggyVectorConvertor() {
    }

    static RECT bounds(IggyTimeline t, long at) throws IOException {
        return new RECT(Math.round(IggyShapeStyles.f32(t, at) * 20), Math.round(IggyShapeStyles.f32(t, at + 8) * 20),
                Math.round(IggyShapeStyles.f32(t, at + 4) * 20), Math.round(IggyShapeStyles.f32(t, at + 12) * 20));
    }

    static void bounds(IggyTimelineBuffer b, long at, RECT r) throws IOException {
        b.f32(at, r.Xmin / 20.0);
        b.f32(at + 4, r.Ymin / 20.0);
        b.f32(at + 8, r.Xmax / 20.0);
        b.f32(at + 12, r.Ymax / 20.0);
    }

    private static final class Geometry {

        final List<SHAPERECORD> records = new ArrayList<>();
        final List<FILLSTYLE> fills = new ArrayList<>();
        final List<LINESTYLE2> lines = new ArrayList<>();
        final List<MORPHFILLSTYLE> morphFills = new ArrayList<>();
        final List<MORPHLINESTYLE2> morphLines = new ArrayList<>();
    }

    private static Geometry read(SWF swf, IggySwf movie, IggyTimeline t, long collection,
            boolean morph, boolean end) throws IOException {
        boolean wide = t.is64();
        int count = IggyShapeStyles.u16(t, collection + (end ? 0 : wide ? 8 : 4));
        long groups = t.reference(collection + (end ? wide ? 8 : 4 : wide ? 24 : 12));
        t.bytes(groups, Math.multiplyExact(count, wide ? 64 : 40));
        Geometry result = new Geometry();
        int x = 0;
        int y = 0;
        for (int g = 0; g < count; g++) {
            long group = groups + (wide ? 64L : 40L) * g;
            int fillOffset = morph ? result.morphFills.size() : result.fills.size();
            int lineOffset = morph ? result.morphLines.size() : result.lines.size();
            long fills = t.reference(group + (wide ? 32 : 24));
            long lines = t.reference(group + (wide ? 40 : 28));
            int fillCount = !end && fills != 0 ? IggyShapeStyles.u32(t, fills) : 0;
            int lineCount = !end && lines != 0 ? IggyShapeStyles.u32(t, lines) : 0;
            if (fillCount > 32767 || lineCount > 32767 || fillOffset + fillCount > 32767 || lineOffset + lineCount > 32767) {
                throw new IOException("Too many SWF shape styles");
            }
            for (int i = 0; i < fillCount; i++) {
                long at = fills + (wide ? 8 : 4) + (wide ? 72L : 60L) * i;
                if (morph) {
                    result.morphFills.add(IggyShapeStyles.morphFill(swf, movie, t, at));
                } else {
                    result.fills.add(IggyShapeStyles.fill(swf, movie, t, at));
                }
            }
            for (int i = 0; i < lineCount; i++) {
                long at = lines + (wide ? 8 : 4) + (wide ? 88L : 76L) * i;
                if (morph) {
                    result.morphLines.add(IggyShapeStyles.morphLine(swf, movie, t, at));
                } else {
                    result.lines.add(IggyShapeStyles.line(swf, movie, t, at));
                }
            }
            long nodes = t.reference(group + 16);
            int size = IggyShapeStyles.u32(t, group + (wide ? 24 : 20));
            t.bytes(nodes, Math.multiplyExact(size, 24));
            int fill0 = -1;
            int fill1 = -1;
            int line = -1;
            for (int i = 0; i < size; i++) {
                long node = nodes + 24L * i;
                int type = IggyShapeStyles.u8(t, node + 16);
                int targetX = Math.round(IggyShapeStyles.f32(t, node) * 20);
                int targetY = Math.round(IggyShapeStyles.f32(t, node + 4) * 20);
                int f0 = end ? 0 : IggyShapeStyles.u16(t, node + (morph ? 20 : 18));
                int f1 = end ? 0 : IggyShapeStyles.u16(t, node + (morph ? 18 : 20));
                int ls = end ? 0 : IggyShapeStyles.u16(t, node + 22);
                if (f0 > fillCount || f1 > fillCount || ls > lineCount) {
                    throw new IOException("Invalid Iggy node style index");
                }
                if (type == 1 || !end && (f0 != fill0 || f1 != fill1 || ls != line)) {
                    StyleChangeRecord style = new StyleChangeRecord();
                    if (type == 1) {
                        style.stateMoveTo = true;
                        style.moveDeltaX = targetX;
                        style.moveDeltaY = targetY;
                        x = targetX;
                        y = targetY;
                    }
                    // Move nodes carry no styles. Resetting them here would split
                    // connected fill contours at every pen move in SWF renderers.
                    if (!end && (type != 1 || i == 0)) {
                        style.stateFillStyle0 = true;
                        style.stateFillStyle1 = true;
                        style.stateLineStyle = true;
                        style.fillStyle0 = f0 == 0 ? 0 : f0 + fillOffset;
                        style.fillStyle1 = f1 == 0 ? 0 : f1 + fillOffset;
                        style.lineStyle = ls == 0 ? 0 : ls + lineOffset;
                    }
                    style.calculateBits();
                    result.records.add(style);
                    if (type != 1 || i == 0) {
                        fill0 = f0;
                        fill1 = f1;
                        line = ls;
                    }
                }
                if (type == 2) {
                    StraightEdgeRecord edge = new StraightEdgeRecord();
                    edge.deltaX = targetX - x;
                    edge.deltaY = targetY - y;
                    edge.generalLineFlag = true;
                    edge.simplify();
                    edge.calculateBits();
                    result.records.add(edge);
                    x = targetX;
                    y = targetY;
                } else if (type == 3) {
                    int cx = Math.round(IggyShapeStyles.f32(t, node + 8) * 20);
                    int cy = Math.round(IggyShapeStyles.f32(t, node + 12) * 20);
                    CurvedEdgeRecord edge = new CurvedEdgeRecord();
                    edge.controlDeltaX = cx - x;
                    edge.controlDeltaY = cy - y;
                    edge.anchorDeltaX = targetX - cx;
                    edge.anchorDeltaY = targetY - cy;
                    edge.calculateBits();
                    result.records.add(edge);
                    x = targetX;
                    y = targetY;
                } else if (type != 1) {
                    throw new IOException("Unsupported Iggy shape node " + type);
                }
            }
        }
        result.records.add(new EndShapeRecord());
        return result;
    }

    private static SHAPE edges(Geometry geometry, int fills, int lines) {
        SHAPE result = new SHAPE();
        result.shapeRecords = geometry.records;
        result.numFillBits = bits(fills);
        result.numLineBits = bits(lines);
        return result;
    }

    private static int bits(int count) {
        return count == 0 ? 0 : 32 - Integer.numberOfLeadingZeros(count);
    }

    public static Tag toSwf(SWF swf, IggySwf movie, IggyTimeline t, long at, int id, int type) throws IOException {
        boolean wide = t.is64();
        long collection = at + (wide ? 48 : 32);
        Geometry start = read(swf, movie, t, collection, type == 2, false);
        if (type == 1) {
            DefineShape4Tag tag = new DefineShape4Tag(swf);
            tag.shapeId = id;
            tag.shapeBounds = bounds(t, at + (wide ? 32 : 16));
            tag.shapes = new SHAPEWITHSTYLE();
            tag.shapes.shapeRecords = start.records;
            tag.shapes.fillStyles = new FILLSTYLEARRAY();
            tag.shapes.fillStyles.fillStyles = start.fills.toArray(new FILLSTYLE[0]);
            tag.shapes.lineStyles = new LINESTYLEARRAY();
            tag.shapes.lineStyles.lineStyles2 = start.lines.toArray(new LINESTYLE2[0]);
            tag.shapes.numFillBits = bits(start.fills.size());
            tag.shapes.numLineBits = bits(start.lines.size());
            tag.edgeBounds = SHAPERECORD.getBounds(start.records, null, 4, true);
            tag.usesNonScalingStrokes = start.lines.stream().anyMatch(line -> line.noHScaleFlag || line.noVScaleFlag);
            tag.usesScalingStrokes = start.lines.stream().anyMatch(line -> !line.noHScaleFlag || !line.noVScaleFlag);
            tag.setModified(true);
            return tag;
        }
        DefineMorphShape2Tag tag = new DefineMorphShape2Tag(swf);
        tag.characterId = id;
        tag.startBounds = bounds(t, at + (wide ? 32 : 16));
        tag.endBounds = bounds(t, at + (wide ? 112 : 72));
        Geometry end = read(swf, movie, t, at + (wide ? 80 : 48), true, true);
        tag.morphFillStyles = new MORPHFILLSTYLEARRAY();
        tag.morphFillStyles.fillStyles = start.morphFills.toArray(new MORPHFILLSTYLE[0]);
        tag.morphLineStyles = new MORPHLINESTYLEARRAY();
        tag.morphLineStyles.lineStyles2 = start.morphLines.toArray(new MORPHLINESTYLE2[0]);
        tag.startEdges = edges(start, start.morphFills.size(), start.morphLines.size());
        tag.endEdges = edges(end, 0, 0);
        tag.startEdgeBounds = SHAPERECORD.getBounds(start.records, null, 4, true);
        tag.endEdgeBounds = SHAPERECORD.getBounds(end.records, null, 4, true);
        tag.usesNonScalingStrokes = start.morphLines.stream().anyMatch(line -> line.noHScaleFlag || line.noVScaleFlag);
        tag.usesScalingStrokes = start.morphLines.stream().anyMatch(line -> !line.noHScaleFlag || !line.noVScaleFlag);
        tag.setModified(true);
        return tag;
    }

    private static final class Node {

        int x;
        int y;
        int cx;
        int cy;
        int type;
        int f0;
        int f1;
        int line;

        Node(int type, int x, int y, int cx, int cy, int f0, int f1, int line) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.cx = cx;
            this.cy = cy;
            this.f0 = f0;
            this.f1 = f1;
            this.line = line;
        }
    }

    private static final class Group {

        final List<Node> nodes = new ArrayList<>();
        FILLSTYLEARRAY fills;
        LINESTYLEARRAY lines;

        Group(FILLSTYLEARRAY fills, LINESTYLEARRAY lines) {
            this.fills = fills;
            this.lines = lines;
        }
    }

    private static List<Group> groups(SHAPE shape, FILLSTYLEARRAY fills, LINESTYLEARRAY lines) throws IOException {
        List<Group> groups = new ArrayList<>();
        Group group = new Group(fills, lines);
        int x = 0;
        int y = 0;
        int f0 = 0;
        int f1 = 0;
        int line = 0;
        for (SHAPERECORD record : shape.shapeRecords) {
            if (record instanceof StyleChangeRecord) {
                StyleChangeRecord style = (StyleChangeRecord) record;
                if (style.stateMoveTo) {
                    x = style.moveDeltaX;
                    y = style.moveDeltaY;
                    group.nodes.add(new Node(1, x, y, 0, 0, f0, f1, line));
                }
                if (style.stateFillStyle0) {
                    f0 = style.fillStyle0;
                }
                if (style.stateFillStyle1) {
                    f1 = style.fillStyle1;
                }
                if (style.stateLineStyle) {
                    line = style.lineStyle;
                }
                if (style.stateNewStyles) {
                    if (!group.nodes.isEmpty()) {
                        groups.add(group);
                    }
                    group = new Group(style.fillStyles, style.lineStyles);
                }
            } else if (record instanceof StraightEdgeRecord || record instanceof CurvedEdgeRecord) {
                if (group.nodes.isEmpty()) {
                    group.nodes.add(new Node(1, x, y, 0, 0, f0, f1, line));
                }
                if (record instanceof StraightEdgeRecord) {
                    StraightEdgeRecord edge = (StraightEdgeRecord) record;
                    x += edge.deltaX;
                    y += edge.deltaY;
                    group.nodes.add(new Node(2, x, y, 0, 0, f0, f1, line));
                } else {
                    CurvedEdgeRecord edge = (CurvedEdgeRecord) record;
                    int cx = x + edge.controlDeltaX;
                    int cy = y + edge.controlDeltaY;
                    x = cx + edge.anchorDeltaX;
                    y = cy + edge.anchorDeltaY;
                    group.nodes.add(new Node(3, x, y, cx, cy, f0, f1, line));
                }
            } else if (!(record instanceof EndShapeRecord)) {
                throw new IOException("Unsupported SWF shape record");
            }
        }
        if (!group.nodes.isEmpty()) {
            groups.add(group);
        }
        return groups;
    }

    private static List<LINESTYLE2> ordinaryLines(LINESTYLEARRAY styles, int shapeNum) {
        List<LINESTYLE2> result = new ArrayList<>();
        if (styles != null) {
            if (shapeNum == 4 && styles.lineStyles2 != null) {
                java.util.Collections.addAll(result, styles.lineStyles2);
            } else if (styles.lineStyles != null) {
                for (LINESTYLE style : styles.lineStyles) {
                    result.add(style.toLineStyle2());
                }
            }
        }
        return result;
    }

    private static MORPHLINESTYLE2 upgrade(MORPHLINESTYLE style) {
        MORPHLINESTYLE2 result = new MORPHLINESTYLE2();
        result.startWidth = style.startWidth;
        result.endWidth = style.endWidth;
        result.startColor = style.startColor;
        result.endColor = style.endColor;
        return result;
    }

    private static void include(float[] bounds, Node n, Node previous) {
        bounds[0] = Math.min(bounds[0], Math.min(n.x, previous.x) / 20f);
        bounds[1] = Math.min(bounds[1], Math.min(n.y, previous.y) / 20f);
        bounds[2] = Math.max(bounds[2], Math.max(n.x, previous.x) / 20f);
        bounds[3] = Math.max(bounds[3], Math.max(n.y, previous.y) / 20f);
        if (n.type == 3) {
            bounds[0] = Math.min(bounds[0], n.cx / 20f);
            bounds[1] = Math.min(bounds[1], n.cy / 20f);
            bounds[2] = Math.max(bounds[2], n.cx / 20f);
            bounds[3] = Math.max(bounds[3], n.cy / 20f);
        }
    }

    private static void writeGroups(SWF swf, IggyTimelineBuffer b, long collection, List<Group> groups,
            int shapeNum, MorphShapeTag morph, boolean end) throws IOException {
        if (groups.size() > 32767) {
            throw new IOException("Too many Iggy shape groups");
        }
        int countAt = end ? 0 : b.wide ? 8 : 4;
        int pointerAt = end ? b.wide ? 8 : 4 : b.wide ? 24 : 12;
        b.u16(collection + countAt, groups.size());
        b.u8(collection + countAt + 2, 0);
        if (!end) {
            b.pointer(collection, -1);
            b.value(collection + (b.wide ? 16 : 8), 0, b.wide ? 8 : 4, 8);
        }
        long array = groups.isEmpty() ? -1 : b.allocate(groups.size() * (b.wide ? 64 : 40));
        b.pointer(collection + pointerAt, array);
        for (int g = 0; g < groups.size(); g++) {
            Group group = groups.get(g);
            long at = array + (b.wide ? 64L : 40L) * g;
            FILLSTYLE[] fills = group.fills == null ? new FILLSTYLE[0] : group.fills.fillStyles;
            List<LINESTYLE2> lines = ordinaryLines(group.lines, shapeNum);
            int fillCount = end ? 0 : morph == null ? fills.length : morph.morphFillStyles.fillStyles.length;
            int lineCount = end ? 0 : morph == null ? lines.size() : morph.getShapeNum() == 2
                    ? morph.morphLineStyles.lineStyles2.length : morph.morphLineStyles.lineStyles.length;
            if (fillCount > 32767 || lineCount > 32767) {
                throw new IOException("Too many Iggy shape styles");
            }
            // The native loader dereferences both tables even when their count is zero.
            long fillTable = b.allocate((b.wide ? 8 : 4) + fillCount * (b.wide ? 72 : 60));
            b.pointer(at + (b.wide ? 32 : 24), fillTable);
            b.u32(fillTable, fillCount);
            for (int i = 0; i < fillCount; i++) {
                long style = fillTable + (b.wide ? 8 : 4) + (b.wide ? 72L : 60L) * i;
                if (morph == null) {
                    IggyShapeStyles.fill(swf, b, style, fills[i]);
                } else {
                    IggyShapeStyles.fill(swf, b, style, morph.morphFillStyles.fillStyles[i]);
                }
            }
            long lineTable = b.allocate((b.wide ? 8 : 4) + lineCount * (b.wide ? 88 : 76));
            b.pointer(at + (b.wide ? 40 : 28), lineTable);
            b.u32(lineTable, lineCount);
            for (int i = 0; i < lineCount; i++) {
                long style = lineTable + (b.wide ? 8 : 4) + (b.wide ? 88L : 76L) * i;
                if (morph == null) {
                    IggyShapeStyles.line(swf, b, style, lines.get(i));
                } else {
                    IggyShapeStyles.line(swf, b, style, morph.getShapeNum() == 2 ? morph.morphLineStyles.lineStyles2[i] : upgrade(morph.morphLineStyles.lineStyles[i]));
                }
            }
            long nodes = b.allocate(group.nodes.size() * 24);
            b.pointer(at + 16, nodes);
            b.u32(at + (b.wide ? 24 : 20), group.nodes.size());
            float[] total = {Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY};
            float[][] fillBounds = new float[fillCount][4];
            for (float[] r : fillBounds) {
                r[0] = r[1] = 1e15f;
                r[2] = r[3] = -1e15f;
            }
            boolean noLines = true;
            Node previous = group.nodes.get(0);
            for (int i = 0; i < group.nodes.size(); i++) {
                Node n = group.nodes.get(i);
                long node = nodes + 24L * i;
                if (!end && n.type != 1 && (n.f0 < 0 || n.f0 > fillCount || n.f1 < 0 || n.f1 > fillCount || n.line < 0 || n.line > lineCount)) {
                    throw new IOException("Invalid SWF shape style index");
                }
                b.f32(node, n.x / 20.0);
                b.f32(node + 4, n.y / 20.0);
                b.f32(node + 8, n.cx / 20.0);
                b.f32(node + 12, n.cy / 20.0);
                // Move records do not draw and their latent style indices are discarded.
                b.u8(node + 16, n.type);
                b.u16(node + 18, n.type == 1 ? 0 : morph == null ? n.f0 : n.f1);
                b.u16(node + 20, n.type == 1 ? 0 : morph == null ? n.f1 : n.f0);
                b.u16(node + 22, n.type == 1 ? 0 : n.line);
                noLines &= n.type == 1 || n.line == 0;
                include(total, n, n);
                if (n.type == 3) {
                    b.u8(collection + countAt + 2, 1);
                }
                if (!end && n.type != 1 && n.f0 != n.f1) {
                    if (n.f0 != 0) {
                        include(fillBounds[n.f0 - 1], n, previous);
                    }
                    if (n.f1 != 0) {
                        include(fillBounds[n.f1 - 1], n, previous);
                    }
                }
                previous = n;
            }
            for (int i = 0; i < 4; i++) {
                b.f32(at + 4 * i, total[i]);
            }
            long fb = fillCount == 0 ? -1 : b.allocate(fillCount * 16);
            b.pointer(at + (b.wide ? 48 : 32), fb);
            for (int i = 0; i < fillCount; i++) {
                for (int j = 0; j < 4; j++) {
                    b.f32(fb + i * 16L + j * 4L, fillBounds[i][j]);
                }
            }
            b.u8(at + (b.wide ? 56 : 36), noLines ? 1 : 0);
        }
    }

    public static IggyObjectData fromSwf(SWF swf, boolean wide, Tag tag) throws IOException {
        boolean morph = tag instanceof MorphShapeTag;
        int type = morph ? 2 : 1;
        int size = morph ? wide ? 128 : 88 : wide ? 80 : 48;
        int id = morph ? ((MorphShapeTag) tag).characterId : ((ShapeTag) tag).shapeId;
        IggyTimelineBuffer b = new IggyTimelineBuffer(wide);
        long at = IggyAssetConvertor.header(b, id, type, size);
        if (!morph) {
            ShapeTag shape = (ShapeTag) tag;
            if (shape instanceof DefineShape4Tag && ((DefineShape4Tag) shape).usesFillWindingRule) {
                throw new IggyUnsupportedTagException(tag, "Iggy does not retain the SWF nonzero fill winding flag");
            }
            SHAPEWITHSTYLE geometry = shape.getShapes();
            bounds(b, at + (wide ? 32 : 16), shape.shapeBounds);
            writeGroups(swf, b, at + (wide ? 48 : 32), groups(geometry, geometry.fillStyles, geometry.lineStyles), shape.getShapeNum(), null, false);
        } else {
            MorphShapeTag shape = (MorphShapeTag) tag;
            List<Group> start = groups(shape.startEdges, null, null);
            List<Group> end = groups(shape.endEdges, null, null);
            normalizeMorph(start, end);
            bounds(b, at + (wide ? 32 : 16), shape.startBounds);
            bounds(b, at + (wide ? 112 : 72), shape.endBounds);
            RECT union = new RECT(Math.min(shape.startBounds.Xmin, shape.endBounds.Xmin), Math.max(shape.startBounds.Xmax, shape.endBounds.Xmax),
                    Math.min(shape.startBounds.Ymin, shape.endBounds.Ymin), Math.max(shape.startBounds.Ymax, shape.endBounds.Ymax));
            bounds(b, at + (wide ? 96 : 56), union);
            writeGroups(swf, b, at + (wide ? 48 : 32), start, 4, shape, false);
            writeGroups(swf, b, at + (wide ? 80 : 48), end, 4, shape, true);
        }
        return new IggyObjectData(b.data.getAllBytes(), new ArrayList<>(b.fields.values()), b.characters, at, size, type);
    }

    private static void normalizeMorph(List<Group> start, List<Group> end) throws IOException {
        List<Node> a = new ArrayList<>();
        List<Node> z = new ArrayList<>();
        for (Group g : start) {
            for (Node n : g.nodes) {
                if (n.type != 1) {
                    a.add(n);
                }
            }
        }
        for (Group g : end) {
            for (Node n : g.nodes) {
                if (n.type != 1) {
                    z.add(n);
                }
            }
        }
        if (a.size() != z.size()) {
            throw new IOException("Morph shape edge counts differ");
        }
        for (List<Group> side : java.util.Arrays.asList(start, end)) {
            int edge = 0;
            for (Group g : side) {
                Node previous = g.nodes.get(0);
                for (Node n : g.nodes) {
                    if (n.type != 1) {
                        if ((a.get(edge).type == 3 || z.get(edge).type == 3) && n.type == 2) {
                            // Promote a line to a quadratic without changing its geometry.
                            n.type = 3;
                            n.cx = (previous.x + n.x) / 2;
                            n.cy = (previous.y + n.y) / 2;
                        }
                        edge++;
                    }
                    previous = n;
                }
            }
        }
    }
}
