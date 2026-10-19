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
package com.jpexs.decompiler.flash;

import com.jpexs.decompiler.flash.tags.DefineEditTextTag;
import com.jpexs.decompiler.flash.tags.DefineFontTag;
import com.jpexs.decompiler.flash.tags.DefineShape3Tag;
import com.jpexs.decompiler.flash.tags.DefineShapeTag;
import com.jpexs.decompiler.flash.tags.DefineText2Tag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.tags.base.TextTag;
import com.jpexs.decompiler.flash.tags.converters.TextTypeConverter;
import com.jpexs.decompiler.flash.types.GLYPHENTRY;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
import com.jpexs.decompiler.flash.types.TEXTRECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

public class TextTypeConverterTest {

    @Test
    public void defineTextConvertsToOpaqueShape() throws Exception {
        SWF swf = new SWF();
        TextTypeConverter converter = new TextTypeConverter();
        TextTag text = converter.convertTagType(createText(swf, 255), swf, TextTypeConverter.TEXT_TYPE_DEFINE_TEXT);
        ShapeTag result = converter.convertToShape(text, swf);
        assertTrue(result instanceof DefineShapeTag);
        assertEquals(roundTrip(result).getBounds(1).Xmax, 280);
    }

    @Test
    public void mixedColorsPreserveSeparateFills() throws Exception {
        SWF swf = new SWF();
        DefineText2Tag text = createText(swf, 255);
        TEXTRECORD second = new TEXTRECORD();
        second.styleFlagsHasColor = true;
        second.textColorA = new RGBA(0, 0, 255, 120);
        second.glyphEntries = text.textRecords.get(0).glyphEntries;
        text.textRecords.add(second);
        ShapeTag result = new TextTypeConverter().convertToShape(text, swf);
        assertTrue(result instanceof DefineShape3Tag);
        SHAPEWITHSTYLE saved = roundTrip(result);
        assertEquals(saved.fillStyles.fillStyles.length, 2);
        assertEquals(((RGBA) saved.fillStyles.fillStyles[0].color).alpha, 255);
        assertEquals(((RGBA) saved.fillStyles.fillStyles[1].color).alpha, 120);
        assertEquals(saved.fillStyles.fillStyles[1].color.blue, 255);
    }

    private DefineText2Tag createText(SWF swf, int alpha) {
        DefineFontTag font = new DefineFontTag(swf);
        SHAPE glyph = TextTag.getBorderShape(new RGB(Color.BLACK), new RGB(Color.BLACK), new RECT(0, 100, 0, 100));
        for (com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD record : glyph.shapeRecords) {
            if (record instanceof StyleChangeRecord) {
                ((StyleChangeRecord) record).stateLineStyle = false;
            }
        }
        // Fonts store outlines without fill/line style arrays.
        SHAPE outline = new SHAPE();
        outline.shapeRecords = new ArrayList<>(glyph.shapeRecords);
        font.glyphShapeTable.add(outline);
        swf.addTag(font);
        swf.updateCharacters();
        DefineText2Tag text = new DefineText2Tag(swf);
        TEXTRECORD rec = new TEXTRECORD();
        rec.styleFlagsHasFont = true;
        rec.fontId = font.fontId;
        rec.textHeight = 1024;
        rec.styleFlagsHasColor = true;
        rec.textColorA = new RGBA(255, 0, 0, alpha);
        rec.styleFlagsHasXOffset = true;
        rec.styleFlagsHasYOffset = true;
        rec.xOffset = 30;
        rec.yOffset = 40;
        GLYPHENTRY first = new GLYPHENTRY();
        first.glyphIndex = 0;
        first.glyphAdvance = 150;
        GLYPHENTRY second = new GLYPHENTRY();
        second.glyphIndex = 0;
        second.glyphAdvance = 150;
        rec.glyphEntries = Arrays.asList(first, second);
        text.textRecords = new ArrayList<>(Arrays.asList(rec));
        return text;
    }

    private SHAPEWITHSTYLE roundTrip(ShapeTag tag) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        SWFOutputStream out = new SWFOutputStream(bytes, 10, tag.getSwf().getCharset());
        out.writeSHAPEWITHSTYLE(tag.shapes, tag.getShapeNum());
        out.close();
        return new SWFInputStream(tag.getSwf(), bytes.toByteArray())
                .readSHAPEWITHSTYLE(tag.getShapeNum(), false, "shape");
    }

    @Test
    public void opaqueTextPreservesPlacementAndSource() throws Exception {
        SWF swf = new SWF();
        DefineText2Tag text = createText(swf, 255);
        text.textMatrix.translateX = 200;
        text.textMatrix.translateY = -100;
        text.textMatrix.hasScale = true;
        text.textMatrix.scaleX = 2;
        text.textMatrix.scaleY = 2;
        ShapeTag result = new TextTypeConverter().convertToShape(text, swf);
        assertTrue(result instanceof DefineShapeTag);
        assertEquals(result.shapeBounds.Xmin, 260);
        assertEquals(result.shapeBounds.Ymin, -20);
        assertEquals(result.shapeBounds.Xmax, 760);
        assertEquals(result.shapeBounds.Ymax, 180);
        assertEquals(result.shapes.fillStyles.fillStyles.length, 1);
        assertFalse(result.shapes.fillStyles.fillStyles[0].color instanceof RGBA);
        assertEquals(roundTrip(result).getBounds(1).Xmax, 760);
        assertEquals(swf.getFont(text.textRecords.get(0).fontId).getGlyphShapeTable().get(0).getBounds(1).Xmin, 0);
        assertEquals(text.textRecords.get(0).xOffset, 30);
    }

    @Test
    public void transparentTextKeepsAlphaAndInheritedStyle() throws Exception {
        SWF swf = new SWF();
        DefineText2Tag text = createText(swf, 80);
        TEXTRECORD inherited = new TEXTRECORD();
        inherited.glyphEntries = text.textRecords.get(0).glyphEntries;
        text.textRecords.add(inherited);
        ShapeTag result = new TextTypeConverter().convertToShape(text, swf);
        assertTrue(result instanceof DefineShape3Tag);
        assertEquals(((RGBA) result.shapes.fillStyles.fillStyles[0].color).alpha, 80);
        assertEquals(result.shapes.fillStyles.fillStyles.length, 1);
        assertEquals(result.shapeBounds.Xmax, 580);
        assertEquals(((RGBA) roundTrip(result).fillStyles.fillStyles[0].color).alpha, 80);
    }

    @Test
    public void conversionReplacesCharacterWithSameId() {
        SWF swf = new SWF();
        DefineText2Tag text = createText(swf, 255);
        swf.addTag(text);
        swf.updateCharacters();
        text.setTimelined(swf);
        int id = text.getCharacterId();
        new TextTypeConverter().convertCharacter(swf, id, TextTypeConverter.TEXT_TYPE_SHAPE);
        assertTrue(swf.getCharacter(id) instanceof DefineShapeTag);
        assertSame(swf.getCharacter(id).getTimelined(), swf);
        assertSame(swf.getTags().get(1), swf.getCharacter(id));
    }

    @Test
    public void dynamicTextUsesSystemFontAndPreservesAlpha() throws Exception {
        SWF swf = new SWF();
        DefineEditTextTag text = new DefineEditTextTag(swf);
        text.hasText = true;
        text.initialText = "Ab";
        text.hasTextColor = true;
        text.textColor = new RGBA(10, 20, 30, 100);
        text.bounds = new RECT(100, 2000, 200, 1000);
        ShapeTag result = new TextTypeConverter().convertToShape(text, swf);
        assertTrue(result instanceof DefineShape3Tag);
        assertTrue(result.shapes.shapeRecords.size() > 10);
        assertEquals(((RGBA) roundTrip(result).fillStyles.fillStyles[0].color).alpha, 100);
    }

    @Test
    public void emptyDynamicTextWithBorderPreservesBorder() throws Exception {
        SWF swf = new SWF();
        DefineEditTextTag text = new DefineEditTextTag(swf);
        text.border = true;
        text.bounds = new RECT(100, 400, 200, 600);
        ShapeTag result = new TextTypeConverter().convertToShape(text, swf);
        assertTrue(result instanceof DefineShapeTag);
        assertEquals(result.shapeBounds.Xmin, 90);
        assertEquals(result.shapeBounds.Ymin, 190);
        assertEquals(result.shapeBounds.Xmax, 410);
        assertEquals(result.shapeBounds.Ymax, 610);
        assertEquals(roundTrip(result).lineStyles.lineStyles.length, 1);
    }
}