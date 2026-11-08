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
import com.jpexs.decompiler.flash.tags.DefineBitsLossless2Tag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

public class LibraryImageShapeTest {

    @DataProvider(name = "versions")
    public Object[][] versions() {
        return new Object[][]{{1, 1}, {2, 2}, {3, 3}, {8, 4}};
    }

    @Test(dataProvider = "versions")
    public void bitmapShapeKeepsNativeSizeAndReusesLibraryImageAfterSaving(int version, int shapeNum) throws Exception {
        SWF swf = new SWF();
        swf.version = version;
        DefineBitsLossless2Tag image = new DefineBitsLossless2Tag(swf);
        image.bitmapWidth = 137;
        image.bitmapHeight = 59;
        swf.addTag(image);

        ShapeTag shape = EasySwfPanel.createImageShape(image);
        assertEquals(shape.getShapeNum(), shapeNum);
        // Preparing a drop must not add a definition before placement succeeds.
        assertEquals(swf.getTags().size(), 1);
        assertNotEquals(shape.getCharacterId(), image.getCharacterId());
        swf.addTag(shape);

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        swf.saveTo(bytes);
        SWF saved = new SWF(new ByteArrayInputStream(bytes.toByteArray()), false);
        ShapeTag restored = (ShapeTag) saved.getCharacter(shape.getCharacterId());
        assertEquals(restored.getRect().Xmin, 0);
        assertEquals(restored.getRect().Ymin, 0);
        assertEquals(restored.getRect().getWidth(), 137 * (int) SWF.unitDivisor);
        assertEquals(restored.getRect().getHeight(), 59 * (int) SWF.unitDivisor);
        FILLSTYLE fill = restored.getShapes().fillStyles.fillStyles[0];
        assertEquals(fill.bitmapId, image.getCharacterId());
        assertTrue(fill.fillStyleType == FILLSTYLE.CLIPPED_BITMAP
                || fill.fillStyleType == FILLSTYLE.NON_SMOOTHED_CLIPPED_BITMAP);
        assertEquals(fill.bitmapMatrix.scaleX, (float) SWF.unitDivisor);
        assertEquals(fill.bitmapMatrix.scaleY, (float) SWF.unitDivisor);
        assertEquals(saved.getCharacters(false).size(), 2);
    }
}
