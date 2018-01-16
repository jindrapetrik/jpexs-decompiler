/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.exporters.commonshape.ExportRectangle;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.commonshape.Point;
import com.jpexs.decompiler.flash.tags.base.CharacterIdTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.DrawableTag;
import com.jpexs.decompiler.flash.tags.base.RenderContext;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.helpers.ByteArrayRange;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.io.IOException;

/**
 *
 * @author JPEXS
 */
@SWFVersion(from = 8)
public class DefineScalingGridTag extends Tag implements CharacterIdTag {

    public static final int ID = 78;

    public static final String NAME = "DefineScalingGrid";

    @SWFType(BasicType.UI16)
    public int characterId;

    public RECT splitter;

    /**
     * Constructor
     *
     * @param swf
     */
    public DefineScalingGridTag(SWF swf) {
        super(swf, ID, NAME, null);
        splitter = new RECT();
    }

    public DefineScalingGridTag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        characterId = sis.readUI16("characterId");
        splitter = sis.readRECT("splitter");
    }

    /**
     * Gets data bytes
     *
     * @param sos SWF output stream
     * @throws java.io.IOException
     */
    @Override
    public void getData(SWFOutputStream sos) throws IOException {
        sos.writeUI16(characterId);
        sos.writeRECT(splitter);
    }

    @Override
    public int getCharacterId() {
        return characterId;
    }

    @Override
    public void setCharacterId(int characterId) {
        this.characterId = characterId;
    }

    private static double roundPixels(double v) {
        return v; //Math.rint(v / SWF.unitDivisor) * SWF.unitDivisor;
    }

    private static double roundPixels20(double v) {
        return Math.rint(v / SWF.unitDivisor) * SWF.unitDivisor;
    }

    private static Matrix rectToRectMatrix(ExportRectangle fromRect, ExportRectangle toRect) {
        Matrix toOrigin = Matrix.getTranslateInstance(roundPixels(-fromRect.xMin), roundPixels(-fromRect.yMin));
        Matrix scale = new Matrix();
        scale.scaleX = roundPixels(toRect.getWidth()) / roundPixels(fromRect.getWidth());
        scale.scaleY = roundPixels(toRect.getHeight()) / roundPixels(fromRect.getHeight());
        Matrix toDest = Matrix.getTranslateInstance(roundPixels(toRect.xMin), roundPixels(toRect.yMin));
        return toOrigin.preConcatenate(scale).preConcatenate(toDest);
    }

    public RECT getRect() {
        Shape s = getOutline(0, 0, 0, new RenderContext(), new Matrix(), new Matrix(), true);
        if (s == null) {
            return null;
        }
        Rectangle r = s.getBounds();
        return new RECT(r.x, r.x + r.width, r.y, r.y + r.height);
    }

    public Shape getOutline(int frame, int time, int ratio, RenderContext renderContext, Matrix transformation, Matrix prevTransform, boolean stroked) {
        CharacterTag ct = swf.getCharacter(characterId);
        if (ct == null) {
            return null;
        }
        if (!(ct instanceof DrawableTag)) {
            return null;
        }
        double[] coords = new double[6];

        DrawableTag dt = (DrawableTag) ct;
        Shape path = dt.getOutline(frame, time, ratio, renderContext, transformation, stroked);
        PathIterator iterator = path.getPathIterator(new AffineTransform());
        GeneralPath gp = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        ExportRectangle boundsRect = new ExportRectangle(dt.getRect());
        ExportRectangle scalingGrid = new ExportRectangle(splitter);

        ExportRectangle[] sourceRect = new ExportRectangle[9];
        ExportRectangle[] targetRect = new ExportRectangle[9];
        Matrix[] transforms = new Matrix[9];

        getSlices(transformation.transform(boundsRect), boundsRect, scalingGrid, sourceRect, targetRect, transforms);

        while (!iterator.isDone()) {
            int type = iterator.currentSegment(coords);
            for (int i = 0; i < 6; i += 2) {
                double x = coords[i];
                double y = coords[i + 1];
                for (int s = 0; s < 9; s++) {
                    Point p = new Point(x, y);
                    if (sourceRect[s].contains(p)) {
                        p = transforms[s].transform(p);
                        coords[i] = p.x;
                        coords[i + 1] = p.y;
                        break;
                    }
                }
            }
            switch (type) {
                case PathIterator.SEG_MOVETO:
                    gp.moveTo(coords[0], coords[1]);
                    break;
                case PathIterator.SEG_LINETO:
                    gp.lineTo(coords[0], coords[1]);
                    break;
                case PathIterator.SEG_QUADTO:
                    gp.quadTo(coords[0], coords[1], coords[2], coords[3]);
                    break;
                case PathIterator.SEG_CUBICTO:
                    gp.curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
                    break;
                case PathIterator.SEG_CLOSE:
                    gp.closePath();
                    break;
            }
            iterator.next();
        }
        return gp;
    }

    public static void getSlices(ExportRectangle targetBounds, ExportRectangle boundsRect, ExportRectangle scalingGrid, ExportRectangle[] sourceRect, ExportRectangle[] targetRect, Matrix[] transforms) {

        double[] src_x = new double[]{boundsRect.xMin, scalingGrid.xMin, scalingGrid.xMax, boundsRect.xMax};
        double[] dst_x = new double[]{targetBounds.xMin, targetBounds.xMin + scalingGrid.xMin, targetBounds.xMax - (boundsRect.xMax - scalingGrid.xMax), targetBounds.xMax};

        double[] src_y = new double[]{boundsRect.yMin, scalingGrid.yMin, scalingGrid.yMax, boundsRect.yMax};
        double[] dst_y = new double[]{targetBounds.yMin, targetBounds.yMin + scalingGrid.yMin, targetBounds.yMax - (boundsRect.yMax - scalingGrid.yMax), targetBounds.yMax};

        int pos = 0;
        for (int sy = 0; sy < 3; sy++) {
            for (int sx = 0; sx < 3; sx++) {
                sourceRect[pos] = new ExportRectangle(src_x[sx], src_y[sy], src_x[sx + 1], src_y[sy + 1]);
                targetRect[pos] = new ExportRectangle(dst_x[sx], dst_y[sy], dst_x[sx + 1], dst_y[sy + 1]);
                pos++;
            }
        }

        for (int i = 0; i < targetRect.length; i++) {

            /*          sourceRect[i].xMax = roundPixels20(sourceRect[i].xMax);
             sourceRect[i].yMax = roundPixels20(sourceRect[i].yMax);
             sourceRect[i].xMin = roundPixels20(sourceRect[i].xMin);
             sourceRect[i].yMin = roundPixels20(sourceRect[i].yMin);
             */
            //System.out.println("source[" + i + "]=" + sourceRect[i]);
            //System.out.println("target[" + i + "]=" + targetRect[i]);

            /*targetRect[i].xMax = roundPixels20(targetRect[i].xMax);
             targetRect[i].yMax = roundPixels20(targetRect[i].yMax);
             targetRect[i].xMin = roundPixels20(targetRect[i].xMin);
             targetRect[i].yMin = roundPixels20(targetRect[i].yMin);
             */
            transforms[i] = rectToRectMatrix(sourceRect[i], targetRect[i]);

            targetRect[i].xMax = Math.rint(targetRect[i].xMax / SWF.unitDivisor);
            targetRect[i].yMax = Math.rint(targetRect[i].yMax / SWF.unitDivisor);
            targetRect[i].xMin = Math.rint(targetRect[i].xMin / SWF.unitDivisor);
            targetRect[i].yMin = Math.rint(targetRect[i].yMin / SWF.unitDivisor);

            //targetRect[i].xMax += maxStroke;
            //Round to pixel boundary
        }
    }
}
