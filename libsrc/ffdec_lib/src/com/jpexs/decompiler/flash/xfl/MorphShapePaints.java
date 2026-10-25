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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.exporters.shape.PathExporter;
import com.jpexs.decompiler.flash.tags.base.MorphShapeTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.FILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.LINESTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.LINESTYLEARRAY;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
import com.jpexs.decompiler.flash.types.shaperecords.CurvedEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.EndShapeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;

/** Keeps independent SWF paints separate in the planar XFL shape model. */
final class MorphShapePaints {

    private MorphShapePaints() {
    }

    static boolean hasOverlappingFills(MorphShapeTag morph) {
        if (morph.getFillStyles().fillStyles.length < 2) {
            return false;
        }
        int shapeNum = morph.getShapeNum() == 1 ? 3 : 4;
        for (int ratio : new int[]{0, 32768, 65535}) {
            SHAPEWITHSTYLE shape = morph.getShapeAtRatio(ratio);
            List<Area> areas = new ArrayList<>();
            for (int i = 0; i < shape.fillStyles.fillStyles.length; i++) {
                Area area = new Area(PathExporter.exportFillStyle(ShapeTag.WIND_EVEN_ODD, shapeNum, morph.getSwf(), shape, i + 1));
                if (area.isEmpty()) {
                    continue;
                }
                for (Area previous : areas) {
                    if (!previous.getBounds2D().intersects(area.getBounds2D())) {
                        continue;
                    }
                    Area intersection = new Area(previous);
                    intersection.intersect(area);
                    if (!intersection.isEmpty()) {
                        return true;
                    }
                }
                areas.add(area);
            }
        }
        return false;
    }

    // SWF paints fills in style order, followed by strokes. Each returned
    // shape has its own plane so an overlapping fill cannot erase another.
    static List<SHAPEWITHSTYLE> paints(SWF swf, int shapeNum, SHAPEWITHSTYLE shape) {
        List<SHAPEWITHSTYLE> result = new ArrayList<>();
        for (int i = 0; i < shape.fillStyles.fillStyles.length; i++) {
            SHAPEWITHSTYLE paint = empty();
            paint.fillStyles.fillStyles = new FILLSTYLE[]{shape.fillStyles.fillStyles[i]};
            // Area resolves SWF's even-odd fill rule into closed contours, including holes.
            // Its outer contours are counterclockwise, so XFL fillStyle0 is the inside.
            paint.shapeRecords = records(new Area(PathExporter.exportFillStyle(ShapeTag.WIND_EVEN_ODD, shapeNum, swf, shape, i + 1)), true);
            result.add(paint);
        }
        int lineCount = shapeNum <= 3 ? shape.lineStyles.lineStyles.length : shape.lineStyles.lineStyles2.length;
        for (int i = 0; i < lineCount; i++) {
            SHAPEWITHSTYLE paint = empty();
            if (shapeNum <= 3) {
                paint.lineStyles.lineStyles = new LINESTYLE[]{shape.lineStyles.lineStyles[i]};
            } else {
                paint.lineStyles.lineStyles2 = new LINESTYLE2[]{shape.lineStyles.lineStyles2[i]};
            }
            paint.shapeRecords = records(PathExporter.exportLineStyle(ShapeTag.WIND_EVEN_ODD, shapeNum, swf, shape, i + 1), false);
            result.add(paint);
        }
        return result;
    }

    private static SHAPEWITHSTYLE empty() {
        SHAPEWITHSTYLE result = new SHAPEWITHSTYLE();
        result.fillStyles = new FILLSTYLEARRAY();
        result.fillStyles.fillStyles = new FILLSTYLE[0];
        result.lineStyles = new LINESTYLEARRAY();
        result.lineStyles.lineStyles = new LINESTYLE[0];
        result.lineStyles.lineStyles2 = new LINESTYLE2[0];
        return result;
    }

    private static List<SHAPERECORD> records(Shape path, boolean fill) {
        List<SHAPERECORD> result = new ArrayList<>();
        int x = 0;
        int y = 0;
        int startX = 0;
        int startY = 0;
        boolean hasPath = false;
        double[] p = new double[6];
        for (PathIterator it = path.getPathIterator(null); !it.isDone(); it.next()) {
            int type = it.currentSegment(p);
            if (type == PathIterator.SEG_MOVETO) {
                if (fill && hasPath) {
                    close(result, x, y, startX, startY);
                }
                StyleChangeRecord move = new StyleChangeRecord();
                move.stateMoveTo = true;
                move.moveDeltaX = x = (int) Math.round(p[0]);
                move.moveDeltaY = y = (int) Math.round(p[1]);
                startX = x;
                startY = y;
                hasPath = true;
                move.stateFillStyle0 = fill;
                move.fillStyle0 = fill ? 1 : 0;
                move.stateLineStyle = !fill;
                move.lineStyle = fill ? 0 : 1;
                result.add(move);
            } else if (type == PathIterator.SEG_LINETO) {
                StraightEdgeRecord edge = new StraightEdgeRecord();
                edge.generalLineFlag = true;
                edge.deltaX = (int) Math.round(p[0]) - x;
                edge.deltaY = (int) Math.round(p[1]) - y;
                result.add(edge);
                x += edge.deltaX;
                y += edge.deltaY;
            } else if (type == PathIterator.SEG_QUADTO) {
                CurvedEdgeRecord edge = new CurvedEdgeRecord();
                int cx = (int) Math.round(p[0]);
                int cy = (int) Math.round(p[1]);
                edge.controlDeltaX = cx - x;
                edge.controlDeltaY = cy - y;
                x = (int) Math.round(p[2]);
                y = (int) Math.round(p[3]);
                edge.anchorDeltaX = x - cx;
                edge.anchorDeltaY = y - cy;
                result.add(edge);
            } else if (type == PathIterator.SEG_CLOSE) {
                close(result, x, y, startX, startY);
                x = startX;
                y = startY;
            } else {
                throw new IllegalArgumentException("Unexpected SWF path segment " + type);
            }
        }
        if (fill && hasPath) {
            close(result, x, y, startX, startY);
        }
        result.add(new EndShapeRecord());
        return result;
    }

    private static void close(List<SHAPERECORD> records, int x, int y, int startX, int startY) {
        if (x != startX || y != startY) {
            StraightEdgeRecord edge = new StraightEdgeRecord();
            edge.generalLineFlag = true;
            edge.deltaX = startX - x;
            edge.deltaY = startY - y;
            records.add(edge);
        }
    }

}
