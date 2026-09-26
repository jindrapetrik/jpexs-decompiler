/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 */
package com.jpexs.decompiler.flash.exporters.shape;

import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.FILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.LINESTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.LINESTYLEARRAY;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
import com.jpexs.decompiler.flash.types.shaperecords.EndShapeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import java.awt.Color;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.Test;

public class PathExporterTest {

    @Test
    public void exportFillStyleIncludesStylesAddedByStyleChangeRecord() {
        SHAPEWITHSTYLE shape = new SHAPEWITHSTYLE();
        shape.fillStyles = fillStyles(new FILLSTYLE());
        shape.fillStyles.fillStyles[0].color = new RGB(Color.RED);
        shape.lineStyles = emptyLineStyles();
        shape.shapeRecords = new ArrayList<>();

        shape.shapeRecords.add(styleChange(0, 0, 1, null));
        addRectangle(shape.shapeRecords, 100, 100);

        FILLSTYLE addedStyle = new FILLSTYLE();
        addedStyle.color = new RGB(Color.BLUE);
        shape.shapeRecords.add(styleChange(200, 0, 1, fillStyles(addedStyle)));
        addRectangle(shape.shapeRecords, 100, 100);
        shape.shapeRecords.add(new EndShapeRecord());

        GeneralPath initialFill = PathExporter.exportFillStyle(ShapeTag.WIND_EVEN_ODD, 1, null, shape, 1);
        GeneralPath addedFill = PathExporter.exportFillStyle(ShapeTag.WIND_EVEN_ODD, 1, null, shape, 2);

        Assert.assertTrue(initialFill.contains(50, 50));
        Assert.assertFalse(initialFill.contains(250, 50));
        Assert.assertTrue(addedFill.contains(250, 50));
        Assert.assertFalse(addedFill.contains(50, 50));
    }

    @Test
    public void exportLineStyleIncludesLineStylesAddedByStyleChangeRecord() {
        assertLineStyleSelection(1, lineStyles(new LINESTYLE()), lineStyles(new LINESTYLE()));
    }

    @Test
    public void exportLineStyleIncludesLineStyle2AddedByStyleChangeRecord() {
        assertLineStyleSelection(4, lineStyles2(new LINESTYLE2()), lineStyles2(new LINESTYLE2()));
    }

    private static void assertLineStyleSelection(int shapeNum, LINESTYLEARRAY initialStyles, LINESTYLEARRAY addedStyles) {
        SHAPEWITHSTYLE shape = new SHAPEWITHSTYLE();
        shape.fillStyles = fillStyles();
        shape.lineStyles = initialStyles;
        shape.shapeRecords = new ArrayList<>();

        shape.shapeRecords.add(lineStyleChange(0, 0, 1, null));
        addRectangle(shape.shapeRecords, 100, 100);
        shape.shapeRecords.add(lineStyleChange(200, 0, 1, addedStyles));
        addRectangle(shape.shapeRecords, 100, 100);
        shape.shapeRecords.add(new EndShapeRecord());

        GeneralPath initialLine = PathExporter.exportLineStyle(ShapeTag.WIND_EVEN_ODD, shapeNum, null, shape, 1);
        GeneralPath addedLine = PathExporter.exportLineStyle(ShapeTag.WIND_EVEN_ODD, shapeNum, null, shape, 2);

        Assert.assertEquals(initialLine.getBounds().x, 0);
        Assert.assertEquals(initialLine.getBounds().width, 100);
        Assert.assertEquals(addedLine.getBounds().x, 200);
        Assert.assertEquals(addedLine.getBounds().width, 100);
    }

    private static StyleChangeRecord styleChange(int x, int y, int fillStyle1, FILLSTYLEARRAY newStyles) {
        StyleChangeRecord record = new StyleChangeRecord();
        record.stateMoveTo = true;
        record.moveDeltaX = x;
        record.moveDeltaY = y;
        record.stateFillStyle1 = true;
        record.fillStyle1 = fillStyle1;
        if (newStyles != null) {
            record.stateNewStyles = true;
            record.fillStyles = newStyles;
            record.lineStyles = emptyLineStyles();
        }
        return record;
    }

    private static StyleChangeRecord lineStyleChange(int x, int y, int lineStyle, LINESTYLEARRAY newStyles) {
        StyleChangeRecord record = new StyleChangeRecord();
        record.stateMoveTo = true;
        record.moveDeltaX = x;
        record.moveDeltaY = y;
        record.stateLineStyle = true;
        record.lineStyle = lineStyle;
        if (newStyles != null) {
            record.stateNewStyles = true;
            record.fillStyles = fillStyles();
            record.lineStyles = newStyles;
        }
        return record;
    }

    private static void addRectangle(List<SHAPERECORD> records, int width, int height) {
        records.add(edge(width, 0));
        records.add(edge(0, height));
        records.add(edge(-width, 0));
        records.add(edge(0, -height));
    }

    private static StraightEdgeRecord edge(int deltaX, int deltaY) {
        StraightEdgeRecord edge = new StraightEdgeRecord();
        edge.generalLineFlag = true;
        edge.deltaX = deltaX;
        edge.deltaY = deltaY;
        edge.simplify();
        edge.calculateBits();
        return edge;
    }

    private static FILLSTYLEARRAY fillStyles(FILLSTYLE... styles) {
        FILLSTYLEARRAY result = new FILLSTYLEARRAY();
        result.fillStyles = styles;
        return result;
    }

    private static LINESTYLEARRAY emptyLineStyles() {
        LINESTYLEARRAY result = new LINESTYLEARRAY();
        result.lineStyles = new LINESTYLE[0];
        result.lineStyles2 = new LINESTYLE2[0];
        return result;
    }

    private static LINESTYLEARRAY lineStyles(LINESTYLE... styles) {
        LINESTYLEARRAY result = emptyLineStyles();
        result.lineStyles = styles;
        return result;
    }

    private static LINESTYLEARRAY lineStyles2(LINESTYLE2... styles) {
        LINESTYLEARRAY result = emptyLineStyles();
        result.lineStyles2 = styles;
        return result;
    }
}
