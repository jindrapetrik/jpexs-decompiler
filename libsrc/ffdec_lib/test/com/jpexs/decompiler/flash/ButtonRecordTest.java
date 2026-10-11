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

import com.jpexs.decompiler.flash.tags.DefineButton2Tag;
import com.jpexs.decompiler.flash.tags.DefineButtonTag;
import com.jpexs.decompiler.flash.tags.DefineShape3Tag;
import com.jpexs.decompiler.flash.tags.PlaceObject3Tag;
import com.jpexs.decompiler.flash.tags.base.ButtonTag;
import com.jpexs.decompiler.flash.timeline.DepthState;
import com.jpexs.decompiler.flash.types.BUTTONRECORD;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.filters.BLURFILTER;
import com.jpexs.helpers.ByteArrayRange;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Tests optional filters and blend modes in both button record formats.
 *
 * @author JPEXS
 */
public class ButtonRecordTest {

    private static final byte[] ACTIONS = {0x06, 0x07, 0x00}; // Play, Stop, End.

    @DataProvider(name = "buttonFormats")
    public Object[][] buttonFormats() {
        return new Object[][]{
            {false, 0x00}, {false, 0x10}, {false, 0x20}, {false, 0x30},
            {true, 0x00}, {true, 0x10}, {true, 0x20}, {true, 0x30}
        };
    }

    @Test(dataProvider = "buttonFormats")
    public void testOptionalFields(boolean defineButton2, int flags) throws IOException {
        byte[] data = buttonData(defineButton2, flags);
        SWF swf = new SWF();
        swf.version = 8;
        DefineShape3Tag shape = new DefineShape3Tag(swf);
        shape.shapeId = 0x1234;
        shape.shapeBounds = new RECT(0, 200, 0, 100);
        swf.addTag(shape);
        ButtonTag tag;
        try (SWFInputStream sis = new SWFInputStream(swf, data)) {
            tag = defineButton2
                    ? new DefineButton2Tag(sis, new ByteArrayRange(data))
                    : new DefineButtonTag(sis, new ByteArrayRange(data));
            assertEquals(sis.available(), 0);
        }
        assertEquals(tag.getCharacterId(), 0x4567);
        assertEquals(tag.getRecords().size(), 2);
        BUTTONRECORD first = tag.getRecords().get(0);
        BUTTONRECORD second = tag.getRecords().get(1);
        assertEquals(first.characterId, 0x1234);
        assertEquals(first.placeDepth, 1);
        assertTrue(first.buttonStateUp);
        assertEquals(second.characterId, 0x2345);
        assertEquals(second.placeDepth, 2);
        assertTrue(second.buttonStateOver);
        assertFalse(second.buttonHasFilterList);
        assertFalse(second.buttonHasBlendMode);
        assertEquals(first.buttonHasFilterList, (flags & 0x10) != 0);
        assertEquals(first.buttonHasBlendMode, (flags & 0x20) != 0);
        assertEquals(first.blendMode, (flags & 0x20) != 0 ? 3 : 0);
        if (first.buttonHasFilterList) {
            assertEquals(first.filterList.size(), 1);
            assertTrue(first.filterList.get(0) instanceof BLURFILTER);
            BLURFILTER blur = (BLURFILTER) first.filterList.get(0);
            assertEquals(blur.blurX, 2.0);
            assertEquals(blur.blurY, 3.0);
            assertEquals(blur.passes, 1);
        } else {
            assertNull(first.filterList);
        }
        if (defineButton2) {
            assertNotNull(first.colorTransform);
            assertEquals(first.colorTransform.redAddTerm, 1);
            assertEquals(first.colorTransform.greenAddTerm, -1);
            assertEquals(first.colorTransform.blueAddTerm, 0);
            assertEquals(first.colorTransform.alphaAddTerm, 1);
            assertNotNull(second.colorTransform);
            DefineButton2Tag button = (DefineButton2Tag) tag;
            assertEquals(button.actions.size(), 1);
            assertTrue(button.actions.get(0).condIdleToOverUp);
            assertEquals(button.actions.get(0).actionBytes.getRangeData(), ACTIONS);
        } else {
            assertNull(first.colorTransform);
            assertNull(second.colorTransform);
            assertEquals(((DefineButtonTag) tag).actionBytes.getRangeData(), ACTIONS);
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (SWFOutputStream sos = new SWFOutputStream(output, swf.version, swf.getCharset())) {
            tag.getData(sos);
        }
        assertEquals(output.toByteArray(), data);

        PlaceObject3Tag placed = first.toPlaceObject();
        assertEquals(placed.placeFlagHasFilterList, first.buttonHasFilterList);
        assertEquals(placed.placeFlagHasBlendMode, first.buttonHasBlendMode);
        assertEquals(placed.blendMode, first.blendMode);
        if (first.buttonHasFilterList) {
            assertSame(placed.surfaceFilterList, first.filterList);
        }
        assertSame(placed.colorTransform, first.colorTransform);
        DepthState layer = tag.getTimeline().getFrame(0).layers.get(1);
        assertNotNull(layer);
        assertEquals(layer.blendMode, first.blendMode);
        assertEquals(layer.filters, first.filterList);
        RECT bounds = tag.getRect();
        int marginX = first.buttonHasFilterList ? 40 : 0;
        int marginY = first.buttonHasFilterList ? 60 : 0;
        assertEquals(bounds.Xmin, -marginX);
        assertEquals(bounds.Xmax, 200 + marginX);
        assertEquals(bounds.Ymin, -marginY);
        assertEquals(bounds.Ymax, 100 + marginY);
    }

    private byte[] buttonData(boolean defineButton2, int flags) throws IOException {
        // Literal wire bytes, independent of the BUTTONRECORD writer under test.
        ByteArrayOutputStream records = new ByteArrayOutputStream();
        records.write(flags | 0x01);
        records.write(new byte[]{0x34, 0x12, 0x01, 0x00, 0x00}); // ID, depth, identity MATRIX.
        if (defineButton2) {
            records.write(new byte[]{(byte) 0x89, (byte) 0xc4}); // Add RGBA (1, -1, 0, 1).
        }
        if ((flags & 0x10) != 0) {
            records.write(new byte[]{
                0x01, 0x01, // One filter, BLURFILTER ID.
                0x00, 0x00, 0x02, 0x00, // BlurX = 2 in FIXED.
                0x00, 0x00, 0x03, 0x00, // BlurY = 3 in FIXED.
                0x08 // One pass, reserved bits zero.
            });
        }
        if ((flags & 0x20) != 0) {
            records.write(0x03); // Multiply.
        }
        records.write(new byte[]{0x02, 0x45, 0x23, 0x02, 0x00, 0x00}); // Next record, Over.
        if (defineButton2) {
            records.write(0x04); // Identity CXFORMWITHALPHA, Nbits = 1.
        }
        records.write(0x00); // End of BUTTONRECORD list.

        ByteArrayOutputStream data = new ByteArrayOutputStream();
        data.write(new byte[]{0x67, 0x45}); // Button ID.
        if (defineButton2) {
            data.write(0x00); // Reserved and TrackAsMenu.
            data.write(2 + records.size()); // UI16 ActionOffset, all fixtures fit in UI8.
            data.write(0x00);
        }
        records.writeTo(data);
        if (defineButton2) {
            data.write(new byte[]{0x00, 0x00, 0x01, 0x00}); // Last action block, IdleToOverUp.
        }
        data.write(ACTIONS);
        return data.toByteArray();
    }
}
