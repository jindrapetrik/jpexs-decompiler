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
package com.jpexs.decompiler.flash.ftr;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.shape.ShapeExporterBase;
import com.jpexs.decompiler.flash.tags.DefineBitsLossless2Tag;
import com.jpexs.decompiler.flash.tags.DefineShape3Tag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.GRADIENT;
import com.jpexs.decompiler.flash.types.GRADRECORD;
import com.jpexs.decompiler.flash.types.LINESTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
import com.jpexs.decompiler.flash.types.shaperecords.CurvedEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.EndShapeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * Converts FTR pens, brushes and GDI+ paths using FFDec's SWF shape model.
 */
final class FtrShapes {

    private static final int SOLID = 0;
    private static final int TEXTURE = 2;
    private static final int RADIAL = 3;
    private static final int LINEAR = 4;
    private static final int START = 0;
    private static final int LINE = 1;
    private static final int BEZIER = 3;
    private static final int CLOSE = 0x80;
    private static final int SELECT_PEN = 0;
    private static final int SELECT_BRUSH = 1;
    private static final int DRAW_PATH = 2;

    private FtrShapes() {
    }

    private static final class Vertex {

        final int x;
        final int y;
        final int type;

        Vertex(int x, int y, int type) {
            this.x = x;
            this.y = y;
            this.type = type;
        }

        int kind() {
            return type & 7;
        }
    }

    private static final class Texture {

        final byte[] png;
        final List<FILLSTYLE> fills = new ArrayList<>();

        Texture(byte[] png) {
            this.png = png;
        }
    }

    static final class Reader {

        private final SWF swf;
        private final BitSet usedIds = new BitSet();
        private final List<Texture> textures = new ArrayList<>();

        Reader(SWF swf) {
            this.swf = swf;
            usedIds.set(0);
        }

        void registerId(int id) throws IOException {
            if (usedIds.get(id)) {
                throw new IOException("Duplicate FTR character ID " + id);
            }
            usedIds.set(id);
        }

        void addImages() throws IOException {
            for (int i = 0; i < textures.size(); i++) {
                Texture texture = textures.get(i);
                int id = usedIds.nextClearBit(1);
                if (id > 65535) {
                    throw new IOException("No SWF character IDs left for FTR textures");
                }
                usedIds.set(id);
                DefineBitsLossless2Tag image = new DefineBitsLossless2Tag(swf);
                image.setCharacterId(id);
                image.setImage(texture.png);
                for (FILLSTYLE fill : texture.fills) {
                    fill.bitmapId = id;
                }
                image.setTimelined(swf);
                swf.addTag(i, image);
            }
        }

        DefineShape3Tag read(SWFInputStream in) throws IOException {
            DefineShape3Tag tag = new DefineShape3Tag(swf);
            int id = in.readUI16("shapeId");
            registerId(id);
            tag.setCharacterId(id);
            SHAPEWITHSTYLE shape = tag.shapes;
            int penCount = FtrConverter.readCount(in, "penCount", 8);
            LINESTYLE[] pens = new LINESTYLE[penCount];
            for (int i = 0; i < penCount; i++) {
                LINESTYLE pen = new LINESTYLE();
                pen.color = in.readRGBA("penColor");
                pen.width = Math.round(FtrConverter.readFloat(in, "penWidth"));
                if (pen.width < 0 || pen.width > 65535) {
                    throw new IOException("FTR pen width cannot be represented in SWF");
                }
                pens[i] = pen;
            }
            int fillCount = FtrConverter.readCount(in, "brushCount", 8);
            FILLSTYLE[] fills = new FILLSTYLE[fillCount];
            for (int i = 0; i < fillCount; i++) {
                fills[i] = readFill(in);
            }
            if (penCount > 32767 || fillCount > 32767) {
                throw new IOException("Too many styles for an SWF shape");
            }
            shape.lineStyles.lineStyles = pens;
            shape.fillStyles.fillStyles = fills;

            int pathCount = FtrConverter.readCount(in, "pathCount", 4);
            List<List<Vertex>> paths = new ArrayList<>();
            for (int i = 0; i < pathCount; i++) {
                List<Vertex> path = new ArrayList<>();
                int vertices = FtrConverter.readCount(in, "pointCount", 9);
                for (int j = 0; j < vertices; j++) {
                    int x = Math.round(FtrConverter.readFloat(in, "x"));
                    int y = Math.round(FtrConverter.readFloat(in, "y"));
                    int type = in.readUI8("pointType");
                    path.add(new Vertex(x, y, type));
                }
                paths.add(path);
            }

            List<SHAPERECORD> records = new ArrayList<>();
            int fill = -1;
            int pen = -1;
            int count = FtrConverter.readCount(in, "operationCount", 8);
            for (int i = 0; i < count; i++) {
                int operation = FtrConverter.readInt(in, "operation");
                int index = FtrConverter.readInt(in, "index");
                switch (operation) {
                    case SELECT_PEN:
                        pen = checkIndex(index, penCount);
                        break;
                    case SELECT_BRUSH:
                    case 3: // Both opcodes select the current brush.
                        fill = checkIndex(index, fillCount);
                        break;
                    case DRAW_PATH:
                        int path = checkIndex(index, paths.size());
                        if (path >= 0) {
                            // SWF automatically closes fills. Strokes follow the original contour.
                            if (fill >= 0) {
                                appendDraw(records, paths.get(path), fill + 1, 0);
                            }
                            if (pen >= 0) {
                                appendDraw(records, paths.get(path), 0, pen + 1);
                            }
                        }
                        break;
                    default:
                        throw new IOException("Unsupported FTR drawing operation " + operation);
                }
            }
            records.add(new EndShapeRecord());
            shape.shapeRecords = records;
            tag.updateBounds();
            return tag;
        }

        private FILLSTYLE readFill(SWFInputStream in) throws IOException {
            FILLSTYLE fill = new FILLSTYLE();
            fill.inShape3 = true;
            int type = FtrConverter.readInt(in, "brushType");
            switch (type) {
                case SOLID:
                    fill.fillStyleType = FILLSTYLE.SOLID;
                    fill.color = in.readRGBA("color");
                    return fill;
                case TEXTURE:
                    byte[] png = in.readBytes(FtrConverter.readCount(in, "imageSize", 1), "image");
                    int wrap = FtrConverter.readInt(in, "wrapMode");
                    if (wrap != 0 && wrap != 4) {
                        throw new IOException("Unsupported FTR texture wrap mode " + wrap);
                    }
                    fill.fillStyleType = wrap == 0 ? FILLSTYLE.REPEATING_BITMAP : FILLSTYLE.CLIPPED_BITMAP;
                    fill.bitmapMatrix = FtrConverter.readMatrix(in);
                    Texture texture = null;
                    for (Texture candidate : textures) {
                        if (Arrays.equals(png, candidate.png)) {
                            texture = candidate;
                            break;
                        }
                    }
                    if (texture == null) {
                        texture = new Texture(png);
                        textures.add(texture);
                    }
                    texture.fills.add(fill);
                    return fill;
                case RADIAL:
                case LINEAR:
                    fill.fillStyleType = type == RADIAL ? FILLSTYLE.RADIAL_GRADIENT : FILLSTYLE.LINEAR_GRADIENT;
                    int stops = FtrConverter.readCount(in, "gradientStopCount", 8);
                    if (stops == 0 || stops > 10) {
                        throw new IOException("Invalid FTR gradient stop count " + stops);
                    }
                    GRADIENT gradient = new GRADIENT();
                    gradient.gradientRecords = new GRADRECORD[stops];
                    for (int i = 0; i < stops; i++) {
                        GRADRECORD stop = new GRADRECORD();
                        stop.color = in.readRGBA("gradientColor");
                        float position = FtrConverter.readFloat(in, "gradientPosition");
                        if (position < 0 || position > 1) {
                            throw new IOException("Invalid FTR gradient position");
                        }
                        stop.ratio = Math.round((type == RADIAL ? 1 - position : position) * 255);
                        gradient.gradientRecords[type == RADIAL ? stops - 1 - i : i] = stop;
                    }
                    fill.gradient = gradient;
                    fill.gradientMatrix = FtrConverter.readMatrix(in);
                    return fill;
                default:
                    throw new IOException("Unsupported FTR brush type " + type);
            }
        }
    }

    private static int checkIndex(int index, int length) throws IOException {
        if (index < -1 || index >= length) {
            throw new IOException("Invalid FTR drawing index " + index);
        }
        return index;
    }

    private static void appendDraw(List<SHAPERECORD> records, List<Vertex> path, int fill, int pen) throws IOException {
        // A style reset creates a new drawing group and keeps the order of FTR draw calls.
        if (!records.isEmpty()) {
            records.add(styleChange(0, 0));
        }
        Vertex start = null;
        Vertex previous = null;
        for (int i = 0; i < path.size(); i++) {
            Vertex vertex = path.get(i);
            switch (vertex.kind()) {
                case START:
                    if (fill != 0 && previous != null) {
                        appendLine(records, previous, start);
                    }
                    StyleChangeRecord move = styleChange(fill, pen);
                    move.stateMoveTo = true;
                    move.moveDeltaX = vertex.x;
                    move.moveDeltaY = vertex.y;
                    if (move.isTooLarge()) {
                        throw new IOException("FTR path position exceeds SWF limits");
                    }
                    records.add(move);
                    start = vertex;
                    break;
                case LINE:
                    requirePathStart(previous);
                    appendLine(records, previous, vertex);
                    break;
                case BEZIER:
                    requirePathStart(previous);
                    if (i + 2 >= path.size() || path.get(i + 1).kind() != BEZIER || path.get(i + 2).kind() != BEZIER) {
                        throw new IOException("Incomplete FTR Bezier segment");
                    }
                    Vertex second = path.get(++i);
                    Vertex end = path.get(++i);
                    appendCurve(records, previous, vertex, second, end);
                    vertex = end;
                    break;
                default:
                    throw new IOException("Unsupported FTR path point type " + vertex.type);
            }
            if ((vertex.type & CLOSE) != 0) {
                appendLine(records, vertex, start);
                vertex = start;
            }
            previous = vertex;
        }
        if (fill != 0 && previous != null) {
            appendLine(records, previous, start);
        }
    }

    private static void requirePathStart(Vertex previous) throws IOException {
        if (previous == null) {
            throw new IOException("FTR path must start with a start point");
        }
    }

    private static StyleChangeRecord styleChange(int fill, int pen) {
        StyleChangeRecord result = new StyleChangeRecord();
        result.stateFillStyle0 = result.stateFillStyle1 = result.stateLineStyle = true;
        result.fillStyle1 = fill;
        result.lineStyle = pen;
        return result;
    }

    private static void appendLine(List<SHAPERECORD> records, Vertex from, Vertex to) {
        long dx = (long) to.x - from.x;
        long dy = (long) to.y - from.y;
        if (dx == 0 && dy == 0) {
            return;
        }
        int pieces = (int) ((Math.max(Math.abs(dx), Math.abs(dy)) + 65534) / 65535);
        long x = from.x;
        long y = from.y;
        for (int i = 1; i <= pieces; i++) {
            long nextX = from.x + dx * i / pieces;
            long nextY = from.y + dy * i / pieces;
            StraightEdgeRecord line = new StraightEdgeRecord();
            line.deltaX = (int) (nextX - x);
            line.deltaY = (int) (nextY - y);
            line.generalLineFlag = line.deltaX != 0 && line.deltaY != 0;
            line.vertLineFlag = line.deltaX == 0;
            records.add(line);
            x = nextX;
            y = nextY;
        }
    }

    private static void appendCurve(List<SHAPERECORD> records, Vertex from, Vertex first, Vertex second, Vertex to) throws IOException {
        Integer x = invertControl(from.x, first.x, second.x, to.x);
        Integer y = invertControl(from.y, first.y, second.y, to.y);
        if (x != null && y != null) {
            appendQuadratic(records, from.x, from.y, x, y, to.x, to.y);
        } else {
            approximateCubic(records, from.x, from.y, first.x, first.y, second.x, second.y, to.x, to.y, 0);
        }
    }

    private static Integer invertControl(int start, int first, int second, int end) {
        if (first + (int) (((float) end - start) * (1f / 3f)) != second) {
            return null;
        }
        int control = start + (int) Math.round(1.5 * (first - (double) start));
        for (int delta : new int[]{0, 1, -1, 2, -2}) {
            int candidate = control + delta;
            if (first == start + (int) ((candidate - (float) start) * (2f / 3f))) {
                return candidate;
            }
        }
        return null;
    }

    private static void approximateCubic(List<SHAPERECORD> records, double x0, double y0, double x1, double y1,
            double x2, double y2, double x3, double y3, int depth) throws IOException {
        double controlX = (3 * (x1 + x2) - x0 - x3) / 4;
        double controlY = (3 * (y1 + y2) - y0 - y3) / 4;
        double errorX = x0 - 3 * x1 + 3 * x2 - x3;
        double errorY = y0 - 3 * y1 + 3 * y2 - y3;
        if (Math.max(Math.abs(errorX), Math.abs(errorY)) <= 4 || depth == 10) {
            appendQuadratic(records, x0, y0, controlX, controlY, x3, y3);
            return;
        }
        double ax = (x0 + x1) / 2;
        double ay = (y0 + y1) / 2;
        double bx = (x1 + x2) / 2;
        double by = (y1 + y2) / 2;
        double cx = (x2 + x3) / 2;
        double cy = (y2 + y3) / 2;
        double dx = (ax + bx) / 2;
        double dy = (ay + by) / 2;
        double ex = (bx + cx) / 2;
        double ey = (by + cy) / 2;
        double mx = (dx + ex) / 2;
        double my = (dy + ey) / 2;
        approximateCubic(records, x0, y0, ax, ay, dx, dy, mx, my, depth + 1);
        approximateCubic(records, mx, my, ex, ey, cx, cy, x3, y3, depth + 1);
    }

    private static void appendQuadratic(List<SHAPERECORD> records, double x0, double y0, double x1, double y1,
            double x2, double y2) throws IOException {
        long cx = Math.round(x1) - Math.round(x0);
        long cy = Math.round(y1) - Math.round(y0);
        long ax = Math.round(x2) - Math.round(x1);
        long ay = Math.round(y2) - Math.round(y1);
        if (Math.max(Math.max(Math.abs(cx), Math.abs(cy)), Math.max(Math.abs(ax), Math.abs(ay))) > 65535) {
            throw new IOException("FTR curve exceeds SWF edge limits");
        }
        CurvedEdgeRecord curve = new CurvedEdgeRecord();
        curve.controlDeltaX = (int) cx;
        curve.controlDeltaY = (int) cy;
        curve.anchorDeltaX = (int) ax;
        curve.anchorDeltaY = (int) ay;
        records.add(curve);
    }

    static void write(SWFOutputStream out, SWF swf, ShapeTag tag) throws IOException {
        Writer writer = new Writer(swf, tag);
        writer.export();
        writer.write(out);
    }

    private static final class Brush {

        final int type;
        RGB color;
        GRADRECORD[] stops;
        Matrix matrix;
        int bitmapId;
        boolean repeat;

        Brush(int type) {
            this.type = type;
        }
    }

    private static final class Stroke {

        final RGB color;
        final float width;

        Stroke(RGB color, float width) {
            this.color = color;
            this.width = width;
        }
    }

    private static final class Draw {

        final List<Vertex> vertices;
        final Brush brush;
        final Stroke stroke;

        Draw(List<Vertex> vertices, Brush brush, Stroke stroke) {
            this.vertices = vertices;
            this.brush = brush;
            this.stroke = stroke;
        }
    }

    private static final class Writer extends ShapeExporterBase {

        private final SWF swf;
        private final ShapeTag tag;
        private final List<Draw> draws = new ArrayList<>();
        private List<Vertex> path;
        private Brush brush;
        private Stroke stroke;
        private String unsupported;

        Writer(SWF swf, ShapeTag tag) {
            super(tag.getWindingRule(), tag.getShapeNum(), swf, tag.getShapes(), null);
            this.swf = swf;
            this.tag = tag;
        }

        void write(SWFOutputStream out) throws IOException {
            if (unsupported != null) {
                throw new IOException("Shape " + tag.getCharacterId() + ": FTR does not support " + unsupported);
            }
            if (tag.getWindingRule() != ShapeTag.WIND_EVEN_ODD) {
                throw new IOException("FTR does not support nonzero winding in shape " + tag.getCharacterId());
            }
            out.writeUI16(tag.getCharacterId());
            out.writeUI32(draws.stream().filter(draw -> draw.stroke != null).count());
            for (Draw draw : draws) {
                if (draw.stroke != null) {
                    out.writeRGBA(new RGBA(draw.stroke.color));
                    out.writeFLOAT(draw.stroke.width);
                }
            }
            out.writeUI32(draws.stream().filter(draw -> draw.brush != null).count());
            for (Draw draw : draws) {
                if (draw.brush != null) {
                    writeBrush(out, draw.brush);
                }
            }
            out.writeUI32(draws.size());
            for (Draw draw : draws) {
                out.writeUI32(draw.vertices.size());
                for (Vertex vertex : draw.vertices) {
                    out.writeFLOAT(vertex.x);
                    out.writeFLOAT(vertex.y);
                    out.writeUI8(vertex.type);
                }
            }
            out.writeUI32(draws.size() * 3L);
            int fill = 0;
            int pen = 0;
            for (int i = 0; i < draws.size(); i++) {
                Draw draw = draws.get(i);
                writeOperation(out, SELECT_BRUSH, draw.brush == null ? -1 : fill++);
                writeOperation(out, SELECT_PEN, draw.stroke == null ? -1 : pen++);
                writeOperation(out, DRAW_PATH, i);
            }
        }

        private static void writeOperation(SWFOutputStream out, int operation, int index) throws IOException {
            out.writeUI32(operation);
            out.writeSI32(index);
        }

        private void writeBrush(SWFOutputStream out, Brush brush) throws IOException {
            out.writeUI32(brush.type);
            if (brush.type == SOLID) {
                out.writeRGBA(new RGBA(brush.color));
            } else if (brush.type == TEXTURE) {
                if (!(swf.getCharacter(brush.bitmapId) instanceof ImageTag)) {
                    throw new IOException("Missing bitmap " + brush.bitmapId + " in FTR shape");
                }
                ImageTag image = (ImageTag) swf.getCharacter(brush.bitmapId);
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                if (!ImageIO.write(image.getImageCached().getBufferedImage(), "png", bytes)) {
                    throw new IOException("PNG encoder unavailable");
                }
                out.writeUI32(bytes.size());
                bytes.writeTo(out);
                out.writeUI32(brush.repeat ? 0 : 4);
                FtrConverter.writeMatrix(out, brush.matrix);
            } else {
                out.writeUI32(brush.stops.length);
                for (int i = 0; i < brush.stops.length; i++) {
                    GRADRECORD stop = brush.stops[brush.type == RADIAL ? brush.stops.length - 1 - i : i];
                    out.writeRGBA(new RGBA(stop.color));
                    float position = stop.ratio / 255f;
                    out.writeFLOAT(brush.type == RADIAL ? 1 - position : position);
                }
                FtrConverter.writeMatrix(out, brush.matrix);
            }
        }

        private void start(Brush brush, Stroke stroke) {
            finish();
            this.brush = brush;
            this.stroke = stroke;
            path = new ArrayList<>();
        }

        private void finish() {
            if (path != null && path.size() > 1) {
                draws.add(new Draw(path, brush, stroke));
            }
            path = null;
        }

        @Override
        public void beginShape() {
        }

        @Override
        public void endShape() {
            finish();
        }

        @Override
        public void beginFills() {
        }

        @Override
        public void endFills() {
        }

        @Override
        public void beginLines() {
        }

        @Override
        public void endLines(boolean close) {
            finish();
        }

        @Override
        public void beginFill(RGB color) {
            if (color == null) {
                unsupported = "a fill without a color";
            }
            Brush fill = new Brush(SOLID);
            fill.color = color;
            start(fill, null);
        }

        @Override
        public void beginGradientFill(int type, GRADRECORD[] stops, Matrix matrix, int spread,
                int interpolation, float focalPoint) {
            if ((type != FILLSTYLE.LINEAR_GRADIENT && type != FILLSTYLE.RADIAL_GRADIENT)
                    || spread != 0 || interpolation != 0 || focalPoint != 0 || stops.length < 2 || stops.length > 10) {
                unsupported = "this gradient";
            }
            Brush fill = new Brush(type == FILLSTYLE.LINEAR_GRADIENT ? LINEAR : RADIAL);
            List<GRADRECORD> normalized = new ArrayList<>(Arrays.asList(stops));
            if (stops.length > 0 && stops[0].ratio != 0) {
                normalized.add(0, endpoint(stops[0], 0));
            }
            if (stops.length > 0 && stops[stops.length - 1].ratio != 255) {
                normalized.add(endpoint(stops[stops.length - 1], 255));
            }
            if (normalized.size() > 10) {
                unsupported = "more than ten gradient stops including endpoints";
            }
            fill.stops = normalized.toArray(new GRADRECORD[0]);
            fill.matrix = matrix;
            start(fill, null);
        }

        private static GRADRECORD endpoint(GRADRECORD original, int ratio) {
            GRADRECORD stop = new GRADRECORD();
            stop.ratio = ratio;
            stop.color = original.color;
            return stop;
        }

        @Override
        public void beginBitmapFill(int bitmapId, Matrix matrix, boolean repeat, boolean smooth, ColorTransform colorTransform) {
            Brush fill = new Brush(TEXTURE);
            fill.bitmapId = bitmapId;
            fill.matrix = matrix;
            fill.repeat = repeat;
            start(fill, null);
        }

        @Override
        public void endFill() {
            finish();
        }

        @Override
        public void lineStyle(double width, RGB color, boolean pixelHinting, String scaleMode,
                int startCaps, int endCaps, int joints, float miterLimit, boolean noClose) {
            if (color == null || pixelHinting || !"NORMAL".equals(scaleMode)
                    || startCaps != LINESTYLE2.ROUND_CAP || endCaps != LINESTYLE2.ROUND_CAP
                    || joints != LINESTYLE2.ROUND_JOIN) {
                unsupported = "this stroke style";
            }
            start(null, new Stroke(color, (float) width));
        }

        @Override
        public void lineGradientStyle(int type, GRADRECORD[] stops, Matrix matrix, int spread, int interpolation, float focal) {
            unsupported = "gradient strokes";
        }

        @Override
        public void lineBitmapStyle(int bitmapId, Matrix matrix, boolean repeat, boolean smooth, ColorTransform colorTransform) {
            unsupported = "bitmap strokes";
        }

        @Override
        public void moveTo(double x, double y) {
            path.add(new Vertex((int) x, (int) y, START));
        }

        @Override
        public void lineTo(double x, double y) {
            path.add(new Vertex((int) x, (int) y, LINE));
        }

        @Override
        public void curveTo(double controlX, double controlY, double x, double y) {
            Vertex start = path.get(path.size() - 1);
            // Match the truncation used by the DirectUI SWF loader.
            int firstX = start.x + (int) (((float) controlX - start.x) * (2f / 3f));
            int firstY = start.y + (int) (((float) controlY - start.y) * (2f / 3f));
            path.add(new Vertex(firstX, firstY, BEZIER));
            path.add(new Vertex(firstX + (int) (((float) x - start.x) * (1f / 3f)),
                    firstY + (int) (((float) y - start.y) * (1f / 3f)), BEZIER));
            path.add(new Vertex((int) x, (int) y, BEZIER));
        }
    }
}

