/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.tags.base;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.commonshape.ExportRectangle;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.commonshape.SVGExporter;
import com.jpexs.decompiler.flash.exporters.shape.BitmapExporter;
import com.jpexs.decompiler.flash.exporters.shape.CanvasShapeExporter;
import com.jpexs.decompiler.flash.exporters.shape.PathExporter;
import com.jpexs.decompiler.flash.exporters.shape.SVGShapeExporter;
import com.jpexs.decompiler.flash.helpers.LazyObject;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.ILINESTYLE;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.SerializableImage;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base class for shape tags.
 *
 * @author JPEXS
 */
public abstract class ShapeTag extends DrawableTag implements LazyObject {

    /**
     * Shape ID
     */
    @SWFType(BasicType.UI16)
    public int shapeId;

    /**
     * Shape bounds
     */
    public RECT shapeBounds;

    /**
     * Shapes
     */
    public SHAPEWITHSTYLE shapes;

    /**
     * Shape data
     */
    protected ByteArrayRange shapeData;

    private final int markerSize = 10;

    /**
     * Winding rule - even-odd
     */
    public static final int WIND_EVEN_ODD = 0;

    /**
     * Winding rule - nonzero
     */
    public static final int WIND_NONZERO = 1;

    /**
     * Constructor.
     * @param swf SWF
     * @param id ID
     * @param name Name
     * @param data Data
     */
    public ShapeTag(SWF swf, int id, String name, ByteArrayRange data) {
        super(swf, id, name, data);
    }

    @Override
    public void load() {
        getShapes();
    }

    /**
     * Gets the winding rule.
     * @return Winding rule
     */
    public abstract int getWindingRule();

    /**
     * Gets shape number.
     * DefineShape = 1, DefineShape2 = 2, ...
     * @return Shape number
     */
    public abstract int getShapeNum();

    /**
     * Gets shapes.
     * @return Shapes
     */
    public synchronized SHAPEWITHSTYLE getShapes() {
        if (shapes == null && shapeData != null) {
            try {
                SWFInputStream sis = new SWFInputStream(swf, shapeData.getArray(), 0, shapeData.getPos() + shapeData.getLength());
                sis.seek(shapeData.getPos());
                shapes = sis.readSHAPEWITHSTYLE(getShapeNum(), false, "shapes");
                shapeData = null; // not needed anymore, give it to GC
            } catch (IOException ex) {
                Logger.getLogger(ShapeTag.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return shapes;
    }

    @Override
    public void getNeededCharacters(Set<Integer> needed, SWF swf) {
        SHAPEWITHSTYLE shapes = getShapes();
        if (shapes != null) {
            getShapes().getNeededCharacters(needed, swf);
        }
    }

    @Override
    public boolean replaceCharacter(int oldCharacterId, int newCharacterId) {
        boolean modified = getShapes().replaceCharacter(oldCharacterId, newCharacterId);
        if (modified) {
            setModified(true);
        }
        return modified;
    }

    @Override
    public boolean removeCharacter(int characterId) {
        boolean modified = getShapes().removeCharacter(characterId);
        if (modified) {
            setModified(true);
        }
        return modified;
    }

    @Override
    public RECT getRect(Set<BoundedTag> added) {
        return shapeBounds;
    }

    @Override
    public RECT getRect() {
        return getRect(null); // parameter not used
    }

    @Override
    public RECT getRectWithStrokes() {
        int maxWidth = 0;
        List<ILINESTYLE> ilineStyles = new ArrayList<>();
        if (getShapeNum() == 4) {
            for (ILINESTYLE ls : getShapes().lineStyles.lineStyles2) {
                if (ls.getWidth() > maxWidth) {
                    maxWidth = ls.getWidth();
                }
            }
        } else {
            for (ILINESTYLE ls : getShapes().lineStyles.lineStyles) {
                if (ls.getWidth() > maxWidth) {
                    maxWidth = ls.getWidth();
                }
            }
        }

        for (SHAPERECORD sr : getShapes().shapeRecords) {
            if (sr instanceof StyleChangeRecord) {
                StyleChangeRecord scr = (StyleChangeRecord) sr;
                if (scr.stateNewStyles) {
                    if (getShapeNum() == 4) {
                        for (ILINESTYLE ls : scr.lineStyles.lineStyles2) {
                            if (ls.getWidth() > maxWidth) {
                                maxWidth = ls.getWidth();
                            }
                        }
                    } else {
                        for (ILINESTYLE ls : scr.lineStyles.lineStyles) {
                            if (ls.getWidth() > maxWidth) {
                                maxWidth = ls.getWidth();
                            }
                        }
                    }
                }
            }
        }

        RECT r = new RECT(getRect());
        r.Xmin -= maxWidth;
        r.Ymin -= maxWidth;
        r.Xmax += maxWidth;
        r.Ymax += maxWidth;

        return r;
    }

    @Override
    public int getUsedParameters() {
        return 0;
    }

    @Override
    public Shape getOutline(boolean fast, int frame, int time, int ratio, RenderContext renderContext, Matrix transformation, boolean stroked, ExportRectangle viewRect, double unzoom) {
        return transformation.toTransform().createTransformedShape(getShapes().getOutline(fast, getShapeNum(), swf, stroked));
    }

    @Override
    public void toImage(int frame, int time, int ratio, RenderContext renderContext, SerializableImage image, SerializableImage fullImage, boolean isClip, Matrix transformation, Matrix strokeTransformation, Matrix absoluteTransformation, Matrix fullTransformation, ColorTransform colorTransform, double unzoom, boolean sameImage, ExportRectangle viewRect, ExportRectangle viewRectRaw, boolean scaleStrokes, int drawMode, int blendMode, boolean canUseSmoothing) {
        BitmapExporter.export(getWindingRule(), getShapeNum(), getSwf(), getShapes(), null, image, unzoom, transformation, strokeTransformation, colorTransform, scaleStrokes, canUseSmoothing);
        if (Configuration._debugMode.get()) { // show control points
            List<GeneralPath> paths = PathExporter.export(getWindingRule(), getShapeNum(), swf, getShapes());
            double[] coords = new double[6];
            AffineTransform at = transformation.toTransform();
            at.preConcatenate(AffineTransform.getScaleInstance(1 / SWF.unitDivisor, 1 / SWF.unitDivisor));

            // get the graphics from the inner image object, because it creates a new Graphics object
            Graphics2D graphics = (Graphics2D) image.getBufferedImage().getGraphics();
            graphics.setPaint(Color.black);
            for (GeneralPath path : paths) {
                PathIterator iterator = path.getPathIterator(at);
                while (!iterator.isDone()) {
                    int type = iterator.currentSegment(coords);
                    double x = coords[0];
                    double y = coords[1];
                    switch (type) {
                        case PathIterator.SEG_MOVETO:
                            graphics.drawRect((int) (x - markerSize / 2), (int) (y - markerSize / 2), markerSize, markerSize);
                            break;
                        case PathIterator.SEG_LINETO:
                            graphics.drawRect((int) (x - markerSize / 2), (int) (y - markerSize / 2), markerSize, markerSize);
                            break;
                        case PathIterator.SEG_QUADTO:
                            graphics.drawRect((int) (x - markerSize / 2), (int) (y - markerSize / 2), markerSize, markerSize);
                            x = coords[2];
                            y = coords[3];
                            graphics.drawRect((int) (x - markerSize / 2), (int) (y - markerSize / 2), markerSize, markerSize);
                            break;
                        case PathIterator.SEG_CUBICTO:
                            System.out.print("CUBICTO NOT SUPPORTED. ");
                            break;
                        case PathIterator.SEG_CLOSE:
                            System.out.print("CLOSE NOT SUPPORTED. ");
                            break;
                    }
                    iterator.next();
                }
            }
        }
    }

    @Override
    public void toSVG(SVGExporter exporter, int ratio, ColorTransform colorTransform, int level) throws IOException {
        SVGShapeExporter shapeExporter = new SVGShapeExporter(getWindingRule(), getShapeNum(), swf, getShapes(), getCharacterId(), exporter, null, colorTransform, 1);
        shapeExporter.export();
    }

    @Override
    public void toHtmlCanvas(StringBuilder result, double unitDivisor) {
        CanvasShapeExporter cse = new CanvasShapeExporter(getWindingRule(), getShapeNum(), null, unitDivisor, swf, getShapes(), null, 0, 0);
        cse.export();
        result.append(cse.getShapeData());
    }

    @Override
    public int getNumFrames() {
        return 1;
    }

    @Override
    public boolean isSingleFrame() {
        return true;
    }

    @Override
    public int getCharacterId() {
        return shapeId;
    }

    @Override
    public void setCharacterId(int characterId) {
        this.shapeId = characterId;
    }

    /**
     * Updates bounds.
     */
    public void updateBounds() {
        shapes.clearCachedOutline();
        shapeBounds = SHAPERECORD.getBounds(shapes.shapeRecords, shapes.lineStyles, getShapeNum(), false);
    }
}
