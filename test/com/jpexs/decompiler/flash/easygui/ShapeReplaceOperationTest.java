/*
 *  Copyright (C) 2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.easygui;

import static com.jpexs.decompiler.flash.easygui.ShapeSelectionTest.rectangle;
import static com.jpexs.decompiler.flash.easygui.ShapeSelectionTest.selection;
import static com.jpexs.decompiler.flash.easygui.ShapeSelectionTest.shape;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.importers.ShapeImporter;
import com.jpexs.decompiler.flash.importers.svg.SvgImporter;
import com.jpexs.decompiler.flash.tags.DefineShape4Tag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import org.testng.annotations.Test;

public class ShapeReplaceOperationTest {

    private static DefineShape4Tag original() {
        DefineShape4Tag tag = shape();
        rectangle(tag, 0, 0, 2000, 1000, 1, 0);
        selection(tag);
        tag.updateBounds();
        tag.getSwf().addTag(tag);
        tag.setTimelined(tag.getSwf());
        tag.setModified(false);
        tag.getSwf().setModified(false);
        return tag;
    }

    @Test
    public void resourceUndoRestoresOriginalBytesAfterImport() throws Exception {
        DefineShape4Tag tag = original();
        tag.createOriginalData();
        byte[] originalData = tag.getData();
        byte[] originalRange = tag.getOriginalRange().getRangeData();
        ShapeReplaceOperation operation = new ShapeReplaceOperation(tag, "Replace", (shape, forward) -> { });
        new SvgImporter().importSvg(tag,
                "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"20\" height=\"20\"><rect width=\"20\" height=\"20\" fill=\"#00ff00\"/></svg>", false);
        assertTrue(operation.finishImport());
        UndoManager history = new UndoManager();
        history.doOperation(operation, tag.getSwf());
        ShapeTag imported = (ShapeTag) tag.getSwf().getCharacter(tag.shapeId);
        assertTrue(imported.canUndo());
        assertEquals(imported.getOriginalRange().getRangeData(), originalRange);
        imported.undo();
        assertEquals(imported.getData(), originalData);
        assertFalse(imported.canUndo());
    }

    @Test
    public void rasterReplacementUsesExistingBoundsOptionsAndUndoesImportedBitmapTags() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(27, 13, BufferedImage.TYPE_INT_ARGB), "png", output);
        for (boolean updateBounds : new boolean[]{false, true}) {
            DefineShape4Tag tag = original();
            SWF swf = tag.getSwf();
            byte[] before = tag.getData();
            ShapeReplaceOperation replace = new ShapeReplaceOperation(tag, "Replace", (shape, forward) -> { });
            new ShapeImporter().importImage(tag, output.toByteArray(), 0, !updateBounds);
            int bitmap = tag.shapes.fillStyles.fillStyles[0].bitmapId;
            assertTrue(replace.finishImport());
            assertFalse(swf.isModified(), "The import must be registered from the previous clean undo position");
            UndoManager undo = new UndoManager();
            undo.doOperation(replace, swf);
            ShapeTag imported = (ShapeTag) swf.getCharacter(tag.shapeId);
            assertTrue(imported.isModified());
            assertEquals(imported.getRect().getWidth(), updateBounds ? 540 : 2000);
            assertEquals(imported.getRect().getHeight(), updateBounds ? 260 : 1000);
            assertNotNull(swf.getCharacter(bitmap));
            byte[] after = imported.getData();
            undo.undo(swf);
            assertNotNull(swf.getCyclicCharacters(), "Dependency checks must tolerate the tag cache invalidated by import undo");
            assertEquals(((ShapeTag) swf.getCharacter(tag.shapeId)).getData(), before);
            assertNull(swf.getCharacter(bitmap), "Undo must also remove the newly imported bitmap");
            assertFalse(swf.isModified());
            assertFalse(((ShapeTag) swf.getCharacter(tag.shapeId)).isModified());
            undo.redo(swf);
            assertEquals(((ShapeTag) swf.getCharacter(tag.shapeId)).getData(), after);
            assertNotNull(swf.getCharacter(bitmap));
            assertTrue(swf.isModified());
            assertTrue(((ShapeTag) swf.getCharacter(tag.shapeId)).isModified());
            assertTrue(swf.indexOfTag(swf.getCharacter(bitmap)) < swf.indexOfTag(swf.getCharacter(tag.shapeId)));
            swf.clearModified();
            undo.undo(swf);
            assertTrue(((ShapeTag) swf.getCharacter(tag.shapeId)).isModified());
            undo.redo(swf);
            assertFalse(((ShapeTag) swf.getCharacter(tag.shapeId)).isModified());
        }
    }

    @Test
    public void svgReplacementUsesTheExistingImporterAndCanBeUndoneWithoutBitmapTags() throws Exception {
        DefineShape4Tag tag = original();
        SWF swf = tag.getSwf();
        byte[] before = tag.getData();
        int tagCount = swf.getTags().size();
        ShapeReplaceOperation replace = new ShapeReplaceOperation(tag, "Replace", (shape, forward) -> { });
        new SvgImporter().importSvg(tag, "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"27\" height=\"13\"><rect width=\"27\" height=\"13\" fill=\"#00ff00\"/></svg>", false);
        assertTrue(replace.finishImport());
        UndoManager undo = new UndoManager();
        undo.doOperation(replace, swf);
        assertEquals(swf.getTags().size(), tagCount);
        ShapeTag imported = (ShapeTag) swf.getCharacter(tag.shapeId);
        assertEquals(imported.getRect().getWidth(), 540);
        assertEquals(imported.getRect().getHeight(), 260);
        byte[] after = imported.getData();
        undo.undo(swf);
        assertEquals(((ShapeTag) swf.getCharacter(tag.shapeId)).getData(), before);
        undo.redo(swf);
        assertEquals(((ShapeTag) swf.getCharacter(tag.shapeId)).getData(), after);
    }
}
