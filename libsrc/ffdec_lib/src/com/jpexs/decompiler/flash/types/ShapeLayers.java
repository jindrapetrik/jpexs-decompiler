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
package com.jpexs.decompiler.flash.types;

import com.jpexs.decompiler.flash.types.shaperecords.CurvedEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.EndShapeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

/** SWF shape layers delimited by style banks. */
public final class ShapeLayers {

    private ShapeLayers() {
    }

    /**
     * Returns layers in paint order, including empty style banks. The layers
     * have independent style indexes and pen positions, but share edge records
     * and style values with the source. Callers should treat them as read-only.
     */
    public static List<SHAPEWITHSTYLE> split(SHAPEWITHSTYLE source, int shapeNum) {
        boolean layered = source.shapeRecords.stream().anyMatch(record ->
                record instanceof StyleChangeRecord && ((StyleChangeRecord) record).stateNewStyles);
        if (!layered) {
            return Collections.singletonList(source);
        }
        List<SHAPEWITHSTYLE> layers = new ArrayList<>();
        List<FILLSTYLE> fills = new ArrayList<>(Arrays.asList(source.fillStyles.fillStyles));
        List<LINESTYLE> lines = new ArrayList<>();
        List<LINESTYLE2> lines2 = new ArrayList<>();
        if (shapeNum <= 3) {
            lines.addAll(Arrays.asList(source.lineStyles.lineStyles));
        } else {
            lines2.addAll(Arrays.asList(source.lineStyles.lineStyles2));
        }
        int fillOffset = 0;
        int lineOffset = 0;
        int fill0 = 0;
        int fill1 = 0;
        int line = 0;
        int x = 0;
        int y = 0;
        boolean hasEdges = false;
        SHAPEWITHSTYLE layer = layerAt(shapeNum, x, y, fill0, fill1, line);
        for (SHAPERECORD record : source.shapeRecords) {
            x = record.changeX(x);
            y = record.changeY(y);
            if (record instanceof StyleChangeRecord) {
                StyleChangeRecord change = (StyleChangeRecord) record.clone();
                if (change.stateNewStyles) {
                    finishLayer(layers, layer, hasEdges, shapeNum);
                    hasEdges = false;
                    fillOffset = fills.size();
                    lineOffset = shapeNum <= 3 ? lines.size() : lines2.size();
                    fills.addAll(Arrays.asList(change.fillStyles.fillStyles));
                    if (shapeNum <= 3) {
                        lines.addAll(Arrays.asList(change.lineStyles.lineStyles));
                    } else {
                        lines2.addAll(Arrays.asList(change.lineStyles.lineStyles2));
                    }
                }
                if (change.stateFillStyle0) {
                    fill0 = change.fillStyle0 = change.fillStyle0 == 0 ? 0 : change.fillStyle0 + fillOffset;
                }
                if (change.stateFillStyle1) {
                    fill1 = change.fillStyle1 = change.fillStyle1 == 0 ? 0 : change.fillStyle1 + fillOffset;
                }
                if (change.stateLineStyle) {
                    line = change.lineStyle = change.lineStyle == 0 ? 0 : change.lineStyle + lineOffset;
                }
                if (change.stateNewStyles) {
                    layer = layerAt(shapeNum, x, y, fill0, fill1, line);
                } else {
                    if (change.stateMoveTo && !hasEdges) {
                        // An explicit move supersedes the inherited pen position.
                        ((StyleChangeRecord) layer.shapeRecords.get(0)).stateMoveTo = false;
                    }
                    layer.shapeRecords.add(change);
                }
            } else if (!(record instanceof EndShapeRecord)) {
                layer.shapeRecords.add(record);
                hasEdges = true;
            }
        }
        finishLayer(layers, layer, hasEdges, shapeNum);
        for (SHAPEWITHSTYLE part : layers) {
            if (hasEdges(part)) {
                compactStyles(part, fills, lines, lines2, shapeNum);
            }
        }
        return layers;
    }

    private static void finishLayer(List<SHAPEWITHSTYLE> layers, SHAPEWITHSTYLE layer, boolean hasEdges, int shapeNum) {
        if (hasEdges) {
            StyleChangeRecord start = (StyleChangeRecord) layer.shapeRecords.get(0);
            if (!start.stateMoveTo && !start.stateFillStyle0 && !start.stateFillStyle1 && !start.stateLineStyle) {
                layer.shapeRecords.remove(0);
            }
            layer.shapeRecords.add(new EndShapeRecord());
            layers.add(layer);
        } else {
            layers.add(SHAPEWITHSTYLE.createEmpty(shapeNum));
        }
    }

    public static boolean hasEdges(SHAPEWITHSTYLE layer) {
        return layer.shapeRecords.stream().anyMatch(record ->
                record instanceof StraightEdgeRecord || record instanceof CurvedEdgeRecord);
    }

    private static SHAPEWITHSTYLE layerAt(int shapeNum, int x, int y, int fill0, int fill1, int line) {
        SHAPEWITHSTYLE layer = SHAPEWITHSTYLE.createEmpty(shapeNum);
        layer.shapeRecords.clear();
        StyleChangeRecord start = new StyleChangeRecord();
        start.stateMoveTo = x != 0 || y != 0;
        start.moveDeltaX = x;
        start.moveDeltaY = y;
        start.stateFillStyle0 = fill0 != 0;
        start.fillStyle0 = fill0;
        start.stateFillStyle1 = fill1 != 0;
        start.fillStyle1 = fill1;
        start.stateLineStyle = line != 0;
        start.lineStyle = line;
        layer.shapeRecords.add(start);
        return layer;
    }

    private static void compactStyles(SHAPEWITHSTYLE layer, List<FILLSTYLE> fills,
            List<LINESTYLE> lines, List<LINESTYLE2> lines2, int shapeNum) {
        BitSet usedFills = new BitSet();
        BitSet usedLines = new BitSet();
        int fill0 = 0;
        int fill1 = 0;
        int line = 0;
        for (SHAPERECORD record : layer.shapeRecords) {
            if (record instanceof StyleChangeRecord) {
                StyleChangeRecord change = (StyleChangeRecord) record;
                if (change.stateFillStyle0) {
                    fill0 = change.fillStyle0;
                }
                if (change.stateFillStyle1) {
                    fill1 = change.fillStyle1;
                }
                if (change.stateLineStyle) {
                    line = change.lineStyle;
                }
            } else if (record instanceof StraightEdgeRecord || record instanceof CurvedEdgeRecord) {
                usedFills.set(fill0);
                usedFills.set(fill1);
                usedLines.set(line);
            }
        }
        int[] fillMap = new int[fills.size() + 1];
        List<FILLSTYLE> localFills = new ArrayList<>();
        for (int i = usedFills.nextSetBit(1); i >= 0; i = usedFills.nextSetBit(i + 1)) {
            localFills.add(fills.get(i - 1));
            fillMap[i] = localFills.size();
        }
        int[] lineMap = new int[(shapeNum <= 3 ? lines.size() : lines2.size()) + 1];
        List<LINESTYLE> localLines = new ArrayList<>();
        List<LINESTYLE2> localLines2 = new ArrayList<>();
        for (int i = usedLines.nextSetBit(1); i >= 0; i = usedLines.nextSetBit(i + 1)) {
            if (shapeNum <= 3) {
                localLines.add(lines.get(i - 1));
                lineMap[i] = localLines.size();
            } else {
                localLines2.add(lines2.get(i - 1));
                lineMap[i] = localLines2.size();
            }
        }
        layer.fillStyles.fillStyles = localFills.toArray(new FILLSTYLE[0]);
        layer.lineStyles.lineStyles = localLines.toArray(new LINESTYLE[0]);
        layer.lineStyles.lineStyles2 = localLines2.toArray(new LINESTYLE2[0]);
        for (SHAPERECORD record : layer.shapeRecords) {
            if (record instanceof StyleChangeRecord) {
                StyleChangeRecord change = (StyleChangeRecord) record;
                if (change.stateFillStyle0) {
                    change.fillStyle0 = fillMap[change.fillStyle0];
                }
                if (change.stateFillStyle1) {
                    change.fillStyle1 = fillMap[change.fillStyle1];
                }
                if (change.stateLineStyle) {
                    change.lineStyle = lineMap[change.lineStyle];
                }
            }
        }
    }

    /**
     * Joins independent layers in paint order. A new style bank resets the
     * active paints and the pen so it cannot connect to the preceding layer.
     */
    public static SHAPEWITHSTYLE combine(List<SHAPEWITHSTYLE> layers) {
        SHAPEWITHSTYLE combined = new SHAPEWITHSTYLE();
        combined.fillStyles = layers.get(0).fillStyles;
        combined.lineStyles = layers.get(0).lineStyles;
        combined.shapeRecords = new ArrayList<>();
        for (int i = 0; i < layers.size(); i++) {
            SHAPEWITHSTYLE layer = layers.get(i);
            if (i > 0) {
                StyleChangeRecord start = new StyleChangeRecord();
                start.stateNewStyles = true;
                start.fillStyles = layer.fillStyles;
                start.lineStyles = layer.lineStyles;
                start.stateMoveTo = startsWithImplicitMove(layer);
                start.stateFillStyle0 = true;
                start.stateFillStyle1 = true;
                start.stateLineStyle = true;
                combined.shapeRecords.add(start);
            }
            for (SHAPERECORD record : layer.shapeRecords) {
                if (!(record instanceof EndShapeRecord)) {
                    combined.shapeRecords.add(record.clone());
                }
            }
        }
        combined.shapeRecords.add(new EndShapeRecord());
        return combined;
    }

    private static boolean startsWithImplicitMove(SHAPEWITHSTYLE layer) {
        for (SHAPERECORD record : layer.shapeRecords) {
            if (record.isMove()) {
                return false;
            }
            if (record instanceof StraightEdgeRecord || record instanceof CurvedEdgeRecord) {
                return true;
            }
        }
        return false;
    }
}
