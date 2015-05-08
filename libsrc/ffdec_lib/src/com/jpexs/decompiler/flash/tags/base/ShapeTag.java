/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.commonshape.SVGExporter;
import com.jpexs.decompiler.flash.exporters.shape.BitmapExporter;
import com.jpexs.decompiler.flash.exporters.shape.CanvasShapeExporter;
import com.jpexs.decompiler.flash.exporters.shape.PathExporter;
import com.jpexs.decompiler.flash.exporters.shape.SVGShapeExporter;
import com.jpexs.decompiler.flash.helpers.LazyObject;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.SerializableImage;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public abstract class ShapeTag extends CharacterTag implements DrawableTag, LazyObject {

    private final int markerSize = 10;

    public ShapeTag(SWF swf, int id, String name, ByteArrayRange data) {
        super(swf, id, name, data);
    }

    public abstract SHAPEWITHSTYLE getShapes();

    @Override
    public void load() {
        getShapes();
    }

    public abstract int getShapeNum();

    @Override
    public RECT getRect() {
        return getRect(null); // parameter not used
    }

    @Override
    public Shape getOutline(int frame, int time, int ratio, RenderContext renderContext, Matrix transformation) {
        return transformation.toTransform().createTransformedShape(getShapes().getOutline());
    }

    @Override
    public void toImage(int frame, int time, int ratio, RenderContext renderContext, SerializableImage image, Matrix transformation, ColorTransform colorTransform) {
        BitmapExporter.export(swf, getShapes(), null, image, transformation, colorTransform);
        if (Configuration.debugMode.get()) { // show control points
            List<GeneralPath> paths = PathExporter.export(getShapes());
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
    public void toSVG(SVGExporter exporter, int ratio, ColorTransform colorTransform, int level, double zoom) throws IOException {
        SVGShapeExporter shapeExporter = new SVGShapeExporter(swf, getShapes(), exporter, null, colorTransform, zoom);
        shapeExporter.export();
    }

    @Override
    public String toHtmlCanvas(double unitDivisor) {
        CanvasShapeExporter cse = new CanvasShapeExporter(null, unitDivisor, swf, getShapes(), new ColorTransform(), 0, 0);
        cse.export();
        return cse.getShapeData();
    }

    @Override
    public void getNeededCharacters(Set<Integer> needed) {
        getShapes().getNeededCharacters(needed);
    }

    @Override
    public boolean replaceCharacter(int oldCharacterId, int newCharacterId) {
        return getShapes().replaceCharacter(oldCharacterId, newCharacterId);
    }

    @Override
    public boolean removeCharacter(int characterId) {
        return getShapes().removeCharacter(characterId);
    }
}
