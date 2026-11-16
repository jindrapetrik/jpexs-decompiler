/*
 *  Copyright (C) 2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.easygui;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.DefineShape2Tag;
import com.jpexs.decompiler.flash.tags.DefineShape3Tag;
import com.jpexs.decompiler.flash.tags.DefineShape4Tag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.FILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.LINESTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
import com.jpexs.decompiler.flash.types.ShapeLayers;
import com.jpexs.decompiler.flash.types.shaperecords.CurvedEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.EndShapeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import com.jpexs.helpers.Helper;
import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * An immutable before/after shape replacement, including tag format upgrades.
 *
 * @author JPEXS
 */
public class ShapeStyleChangeOperation implements DoableOperation {

    private final ShapeTag before;
    private final ShapeTag after;
    private final Timelined parent;
    private final boolean parentModified;
    private final boolean swfModified;
    private final boolean headerModified;
    private final int version;
    private final String description;
    private final BiConsumer<ShapeTag, Boolean> refresh;

    public ShapeStyleChangeOperation(ShapeTag before, ShapeSelection edited, String description,
            BiConsumer<ShapeTag, Boolean> refresh) throws IOException, InterruptedException {
        this(before, createShape(before, edited), description, refresh);
    }

    public ShapeStyleChangeOperation(ShapeTag before, ShapeTag edited, String description,
            BiConsumer<ShapeTag, Boolean> refresh) throws IOException, InterruptedException {
        this.before = before;
        this.after = (ShapeTag) edited.cloneTag();
        this.parent = before.getTimelined() == null ? before.getSwf() : before.getTimelined();
        this.parentModified = parent.isModified();
        this.swfModified = before.getSwf().isModified();
        this.headerModified = before.getSwf().isHeaderModified();
        this.version = before.getSwf().version;
        this.description = description;
        this.refresh = refresh;
    }

    @Override
    public void doOperation() {
        apply(after, true);
    }

    @Override
    public void undoOperation() {
        apply(before, false);
    }

    private void apply(ShapeTag target, boolean forward) {
        SWF swf = before.getSwf();
        ShapeTag current = (ShapeTag) swf.getCharacter(before.getCharacterId());
        target.setOriginalRange(current.getOriginalRange());
        target.setTimelined(parent);
        parent.replaceTag(current, target);
        target.setModified(true);
        int requiredVersion = after.getShapeNum() == 4 ? 8 : after.getShapeNum();
        swf.version = forward ? Math.max(version, requiredVersion) : version;
        swf.setHeaderModified(forward ? headerModified || swf.version != version : headerModified);
        parent.setModified(forward || parentModified);
        swf.setModified(forward || swfModified);
        swf.clearAllCache();
        if (refresh != null) {
            refresh.accept(target, forward);
        }
    }

    @Override
    public String getDescription() {
        return description;
    }

    public static ShapeTag createShape(ShapeTag original, ShapeSelection model) throws IOException, InterruptedException {
        return createShape(original, Collections.singletonList(original.getShapes()), 0, model);
    }

    static ShapeTag createShape(ShapeTag original, List<SHAPEWITHSTYLE> layers, int activeLayer, ShapeSelection model)
            throws IOException, InterruptedException {
        return createShape(original, layers, Collections.singletonMap(activeLayer, model));
    }

    static ShapeTag createShape(ShapeTag original, List<SHAPEWITHSTYLE> layers, Map<Integer, ShapeSelection> models)
            throws IOException, InterruptedException {
        Map<Integer, SHAPEWITHSTYLE> edits = new HashMap<>();
        int shapeNum = original.getShapeNum();
        for (Map.Entry<Integer, ShapeSelection> entry : models.entrySet()) {
            SHAPEWITHSTYLE data = createShapeData(entry.getValue());
            shapeNum = Math.max(shapeNum, data.getMinShapeNum(4));
            edits.put(entry.getKey(), data);
        }
        List<SHAPEWITHSTYLE> editedLayers = new ArrayList<>(layers.size());
        for (int i = 0; i < layers.size(); i++) {
            SHAPEWITHSTYLE edit = edits.get(i);
            editedLayers.add(edit == null ? convertLayer(layers.get(i), original.getShapeNum(), shapeNum)
                    : convertLayer(edit, 4, shapeNum));
        }
        return createTag(original, editedLayers, shapeNum);
    }

    static ShapeTag createShape(ShapeTag original, List<SHAPEWITHSTYLE> layers) throws IOException, InterruptedException {
        int sourceShapeNum = original.getShapeNum();
        int shapeNum = Math.max(sourceShapeNum, layers.size() > 1 ? 2 : 1);
        List<SHAPEWITHSTYLE> converted = new ArrayList<>(layers.size());
        for (SHAPEWITHSTYLE layer : layers) {
            converted.add(convertLayer(layer, sourceShapeNum, shapeNum));
        }
        return createTag(original, converted, shapeNum);
    }

    private static SHAPEWITHSTYLE createShapeData(ShapeSelection model) {
        SHAPEWITHSTYLE editedRecords = model.getEditedRecords();
        if (editedRecords != null) {
            return editedRecords;
        }
        SHAPEWITHSTYLE data = SHAPEWITHSTYLE.createEmpty(4);
        data.shapeRecords.clear();
        List<FILLSTYLE> fills = new ArrayList<>();
        List<LINESTYLE2> lines = new ArrayList<>();
        for (ShapeSelection.Selection part : model.getParts()) {
            if (part.fill == null && part.line == null) {
                continue;
            }
            int fill = 0;
            int line = 0;
            if (part.fill != null) {
                fills.add(part.fill.toShapeNum(4));
                fill = fills.size();
            }
            if (part.line != null) {
                LINESTYLE2 line2 = part.line instanceof LINESTYLE2 ? Helper.deepCopy((LINESTYLE2) part.line)
                        : ((LINESTYLE) part.line).toLineStyle2();
                lines.add(line2);
                line = lines.size();
            }
            // A group preserves the original paint order across overlapping parts.
            data.shapeRecords.add(styles(0, 0));
            data.shapeRecords.add(styles(fill, line));
            appendPath(data.shapeRecords, part.geometry);
        }
        data.shapeRecords.add(new EndShapeRecord());
        data.fillStyles.fillStyles = fills.toArray(new FILLSTYLE[0]);
        data.lineStyles.lineStyles2 = lines.toArray(new LINESTYLE2[0]);
        return data;
    }

    private static ShapeTag createTag(ShapeTag original, List<SHAPEWITHSTYLE> layers, int shapeNum)
            throws IOException, InterruptedException {
        SHAPEWITHSTYLE data = ShapeLayers.combine(layers);
        ShapeTag result;
        if (shapeNum == original.getShapeNum()) {
            result = (ShapeTag) original.cloneTag();
        } else if (shapeNum == 4) {
            result = new DefineShape4Tag(original.getSwf());
        } else if (shapeNum == 3) {
            result = new DefineShape3Tag(original.getSwf());
        } else {
            result = new DefineShape2Tag(original.getSwf());
        }
        result.shapeId = original.shapeId;
        result.shapes = data;
        result.updateBounds();
        if (result instanceof DefineShape4Tag) {
            DefineShape4Tag shape4 = (DefineShape4Tag) result;
            shape4.usesScalingStrokes = layers.stream().flatMap(layer -> Arrays.stream(layer.lineStyles.lineStyles2))
                    .anyMatch(line -> !line.noHScaleFlag || !line.noVScaleFlag);
            shape4.usesNonScalingStrokes = layers.stream().flatMap(layer -> Arrays.stream(layer.lineStyles.lineStyles2))
                    .anyMatch(line -> line.noHScaleFlag || line.noVScaleFlag);
        }
        result.setModified(true);
        return result;
    }

    static SHAPEWITHSTYLE convertLayer(SHAPEWITHSTYLE layer, int sourceShapeNum, int shapeNum) {
        if (sourceShapeNum == shapeNum) {
            return layer;
        }
        SHAPEWITHSTYLE converted = new SHAPEWITHSTYLE();
        converted.shapeRecords = layer.shapeRecords;
        converted.fillStyles = new FILLSTYLEARRAY();
        converted.fillStyles.fillStyles = Arrays.stream(layer.fillStyles.fillStyles)
                .map(fill -> fill.toShapeNum(shapeNum)).toArray(FILLSTYLE[]::new);
        converted.lineStyles = layer.lineStyles.toShapeNum(sourceShapeNum, shapeNum);
        return converted;
    }

    private static StyleChangeRecord styles(int fill, int line) {
        StyleChangeRecord record = new StyleChangeRecord();
        record.stateFillStyle0 = true;
        record.stateFillStyle1 = true;
        record.fillStyle1 = fill;
        record.stateLineStyle = true;
        record.lineStyle = line;
        return record;
    }

    private static void straight(List<SHAPERECORD> records, int fromX, int fromY, int x, int y) {
        if (fromX == x && fromY == y) {
            return;
        }
        // A SWF edge can store at most 17 signed bits per delta.
        if (Math.abs((long) x - fromX) > 65535 || Math.abs((long) y - fromY) > 65535) {
            int middleX = (int) (((long) fromX + x) / 2);
            int middleY = (int) (((long) fromY + y) / 2);
            straight(records, fromX, fromY, middleX, middleY);
            straight(records, middleX, middleY, x, y);
            return;
        }
        StraightEdgeRecord edge = new StraightEdgeRecord();
        edge.generalLineFlag = true;
        edge.deltaX = x - fromX;
        edge.deltaY = y - fromY;
        edge.simplify();
        edge.calculateBits();
        records.add(edge);
    }

    private static void curve(List<SHAPERECORD> records, int fromX, int fromY, double cx, double cy, double x, double y) {
        CurvedEdgeRecord edge = new CurvedEdgeRecord();
        edge.controlDeltaX = (int) Math.round(cx) - fromX;
        edge.controlDeltaY = (int) Math.round(cy) - fromY;
        edge.anchorDeltaX = (int) Math.round(x) - (int) Math.round(cx);
        edge.anchorDeltaY = (int) Math.round(y) - (int) Math.round(cy);
        if (edge.isTooLarge()) {
            double x1 = (fromX + cx) / 2;
            double y1 = (fromY + cy) / 2;
            double x2 = (cx + x) / 2;
            double y2 = (cy + y) / 2;
            double mx = (x1 + x2) / 2;
            double my = (y1 + y2) / 2;
            curve(records, fromX, fromY, x1, y1, mx, my);
            curve(records, (int) Math.round(mx), (int) Math.round(my), x2, y2, x, y);
            return;
        }
        edge.calculateBits();
        records.add(edge);
    }

    private static void appendPath(List<SHAPERECORD> records, Shape path) {
        double[] coords = new double[6];
        int x = 0;
        int y = 0;
        int startX = 0;
        int startY = 0;
        for (PathIterator iterator = path.getPathIterator(null); !iterator.isDone(); iterator.next()) {
            switch (iterator.currentSegment(coords)) {
                case PathIterator.SEG_MOVETO:
                    x = startX = (int) Math.round(coords[0]);
                    y = startY = (int) Math.round(coords[1]);
                    StyleChangeRecord move = new StyleChangeRecord();
                    move.stateMoveTo = true;
                    move.moveDeltaX = x;
                    move.moveDeltaY = y;
                    move.calculateBits();
                    records.add(move);
                    break;
                case PathIterator.SEG_LINETO:
                    straight(records, x, y, (int) Math.round(coords[0]), (int) Math.round(coords[1]));
                    x = (int) Math.round(coords[0]);
                    y = (int) Math.round(coords[1]);
                    break;
                case PathIterator.SEG_QUADTO:
                    curve(records, x, y, coords[0], coords[1], coords[2], coords[3]);
                    x = (int) Math.round(coords[2]);
                    y = (int) Math.round(coords[3]);
                    break;
                case PathIterator.SEG_CUBICTO:
                    double[][] quadratics = CubicToQuadratic.approximate(new double[]{x, y,
                        coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]});
                    for (double[] quadratic : quadratics) {
                        curve(records, x, y, quadratic[0], quadratic[1], quadratic[2], quadratic[3]);
                        x = (int) Math.round(quadratic[2]);
                        y = (int) Math.round(quadratic[3]);
                    }
                    break;
                case PathIterator.SEG_CLOSE:
                    straight(records, x, y, startX, startY);
                    x = startX;
                    y = startY;
                    break;
            }
        }
    }
}
