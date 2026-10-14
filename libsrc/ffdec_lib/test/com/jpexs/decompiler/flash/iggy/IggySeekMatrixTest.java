/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 3.0 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library.
 */
package com.jpexs.decompiler.flash.iggy;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.iggy.conversion.IggyTimelineConvertor;
import com.jpexs.decompiler.flash.iggy.conversion.IggyToSwfConvertor;
import com.jpexs.decompiler.flash.iggy.conversion.SwfToIggyConvertor;
import com.jpexs.decompiler.flash.iggy.streams.MemoryDataStream;
import com.jpexs.decompiler.flash.tags.DefineShapeTag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.EndTag;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.PlaceObject3Tag;
import com.jpexs.decompiler.flash.tags.RemoveObject2Tag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class IggySeekMatrixTest {

    @DataProvider(name = "timelines")
    public Object[][] timelines() {
        List<Object[]> result = new ArrayList<>();
        for (IggyFile.Variant variant : IggyFile.Variant.values()) {
            for (boolean sprite : new boolean[]{false, true}) {
                result.add(new Object[]{variant, sprite});
            }
        }
        return result.toArray(new Object[0][]);
    }

    @DataProvider(name = "widths")
    public Object[][] widths() {
        return new Object[][]{{false}, {true}};
    }

    private static MATRIX matrix(int which) {
        MATRIX matrix = new MATRIX();
        matrix.hasScale = true;
        matrix.hasRotate = true;
        matrix.scaleX = which + 0.5f;
        matrix.scaleY = which + 0.25f;
        matrix.rotateSkew0 = which * -0.125f;
        matrix.rotateSkew1 = which * 0.25f;
        matrix.translateX = which * 1000;
        matrix.translateY = which * -400;
        return matrix;
    }

    private static PlaceObject2Tag place(SWF swf, int depth, int id, MATRIX matrix) {
        return new PlaceObject2Tag(swf, true, depth, id, matrix, null, -1, null, -1, null);
    }

    private static byte[] bytes(SWF swf) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        swf.saveTo(out);
        return out.toByteArray();
    }

    private static byte[] bytes(IggyFile file) throws Exception {
        file.updateFlashEntry();
        MemoryDataStream out = new MemoryDataStream();
        file.writeToDataStream(out);
        return out.getAllBytes();
    }

    private static SWF reparse(SWF swf) throws Exception {
        return new SWF(new ByteArrayInputStream(bytes(swf)), false, "UTF-8");
    }

    private static IggyFile reload(IggyFile file) throws Exception {
        return new IggyFile(new MemoryDataStream(bytes(file)));
    }

    private static Timelined timeline(SWF swf, boolean sprite) {
        return sprite ? (DefineSpriteTag) swf.getCharacter(20) : swf;
    }

    private static SWF movie(boolean sprite) throws Exception {
        SWF swf = new SWF();
        swf.version = 10;
        swf.frameRate = 24;
        swf.frameCount = sprite ? 1 : 8;
        swf.displayRect = new RECT(0, 10000, 0, 8000);
        for (int id = 1; id <= 3; id++) {
            DefineShapeTag shape = new DefineShapeTag(swf);
            shape.shapeId = id;
            swf.addTag(shape);
        }
        Timelined target = swf;
        if (sprite) {
            DefineSpriteTag nested = new DefineSpriteTag(swf);
            nested.spriteId = 20;
            nested.frameCount = 8;
            swf.addTag(nested);
            target = nested;
        }
        PlaceObject2Tag first = place(swf, 2, 1, matrix(1));
        first.placeFlagMove = false;
        target.addTag(first);
        first = place(swf, 5, 2, matrix(2));
        first.placeFlagMove = false;
        target.addTag(first);
        target.addTag(new ShowFrameTag(swf));
        target.addTag(place(swf, 2, 3, null));
        target.addTag(place(swf, 5, 1, null));
        target.addTag(new ShowFrameTag(swf));
        target.addTag(place(swf, 2, -1, matrix(3)));
        target.addTag(new ShowFrameTag(swf));
        target.addTag(place(swf, 2, 2, null));
        target.addTag(place(swf, 5, 2, null));
        target.addTag(new ShowFrameTag(swf));
        RemoveObject2Tag remove = new RemoveObject2Tag(swf);
        remove.depth = 2;
        target.addTag(remove);
        PlaceObject2Tag fresh = place(swf, 2, 3, null);
        fresh.placeFlagMove = false;
        target.addTag(fresh);
        target.addTag(new ShowFrameTag(swf));
        target.addTag(place(swf, 2, 1, null));
        target.addTag(place(swf, 5, 3, matrix(4)));
        target.addTag(new ShowFrameTag(swf));
        target.addTag(place(swf, 5, 2, null));
        target.addTag(new ShowFrameTag(swf));
        target.addTag(new ShowFrameTag(swf));
        target.addTag(new EndTag(swf));
        if (sprite) {
            PlaceObject2Tag rootPlace = place(swf, 1, 20, new MATRIX());
            rootPlace.placeFlagMove = false;
            swf.addTag(rootPlace);
            swf.addTag(new ShowFrameTag(swf));
            swf.addTag(new EndTag(swf));
        }
        return reparse(swf);
    }

    private static void assertMatrix(MATRIX actual, MATRIX expected) {
        Assert.assertNotNull(actual);
        Assert.assertEquals(actual.scaleX, expected.scaleX, 0.00001);
        Assert.assertEquals(actual.scaleY, expected.scaleY, 0.00001);
        Assert.assertEquals(actual.rotateSkew0, expected.rotateSkew0, 0.00001);
        Assert.assertEquals(actual.rotateSkew1, expected.rotateSkew1, 0.00001);
        Assert.assertEquals(actual.translateX, expected.translateX);
        Assert.assertEquals(actual.translateY, expected.translateY);
    }

    private static void verify(SWF result, boolean sprite, int extraTranslation) {
        int frame = 1;
        for (Tag tag : timeline(result, sprite).getTags()) {
            if (tag instanceof ShowFrameTag) {
                frame++;
            }
            if (!(tag instanceof PlaceObjectTypeTag)) {
                continue;
            }
            PlaceObjectTypeTag place = (PlaceObjectTypeTag) tag;
            int depth = place.getDepth();
            if (frame == 2) {
                assertMatrix(place.getMatrix(), matrix(depth == 2 ? 1 : 2));
            } else if (frame == 4) {
                MATRIX expected = matrix(depth == 2 ? 3 : 2);
                if (depth == 2) {
                    expected.translateX += extraTranslation;
                }
                assertMatrix(place.getMatrix(), expected);
            } else if ((frame == 5 || frame == 6) && depth == 2) {
                Assert.assertNull(place.getMatrix(), "Remove must clear inherited matrix state");
            } else if (frame == 7) {
                assertMatrix(place.getMatrix(), matrix(4));
            }
        }
        Assert.assertEquals(frame, 9);
    }

    @Test(dataProvider = "timelines")
    public void serializeInheritedMatricesPerDepth(IggyFile.Variant variant, boolean sprite) throws Exception {
        SWF source = movie(sprite);
        byte[] before = bytes(source);
        IggyFile file = reload(SwfToIggyConvertor.createIggy(source, variant, "seek-matrices.swf"));
        verify(reparse(IggyToSwfConvertor.getSwf(file)), sprite, 0);
        Assert.assertEquals(bytes(source), before, "Source placement flags/matrices must remain untouched");
    }

    @Test(dataProvider = "timelines")
    public void editedTimelineUpdatesInheritedMatrices(IggyFile.Variant variant, boolean sprite) throws Exception {
        IggyFile file = SwfToIggyConvertor.createIggy(movie(sprite), variant, "seek-matrices.swf");
        SWF replacement = movie(sprite);
        for (Tag tag : timeline(replacement, sprite).getTags()) {
            if (tag instanceof PlaceObjectTypeTag) {
                PlaceObjectTypeTag place = (PlaceObjectTypeTag) tag;
                if (place.getCharacterId() < 0 && place.getMatrix() != null) {
                    place.getMatrix().translateX += 220;
                    tag.setModified(true);
                }
            }
        }
        replacement = reparse(replacement);
        byte[] before = bytes(replacement);
        SwfToIggyConvertor.updateIggy(file.getSwf(), replacement);
        verify(reparse(IggyToSwfConvertor.getSwf(reload(file))), sprite, 220);
        Assert.assertEquals(bytes(replacement), before);
    }

    @Test(dataProvider = "timelines")
    public void unchangedRoundTripPreservesIggy(IggyFile.Variant variant, boolean sprite) throws Exception {
        IggyFile file = reload(SwfToIggyConvertor.createIggy(movie(sprite), variant, "seek-matrices.swf"));
        byte[] before = bytes(file);
        SwfToIggyConvertor.updateIggy(file.getSwf(), reparse(IggyToSwfConvertor.getSwf(file)));
        Assert.assertEquals(bytes(file), before);
    }

    @Test(dataProvider = "widths")
    public void classOnlyReplacementGetsMatrixWithoutCharacterId(boolean wide) throws Exception {
        SWF swf = new SWF();
        List<Tag> tags = new ArrayList<>();
        tags.add(place(swf, 2, 1, matrix(1)));
        tags.add(new ShowFrameTag(swf));
        PlaceObject3Tag replacement = new PlaceObject3Tag(swf);
        replacement.depth = 2;
        replacement.placeFlagMove = true;
        replacement.placeFlagHasClassName = true;
        replacement.className = "ImportedClass";
        tags.add(replacement);
        tags.add(new ShowFrameTag(swf));
        IggyTimeline.Replacement result = IggyTimelineConvertor.fromSwf(swf, wide, tags, 2);
        IggyTimeline timeline = new IggyTimeline(result.data, result.first, wide);
        IggyTimeline.Command second = timeline.getCommands().get(2);
        int flags = timeline.bytes(second.getAddress() + (wide ? 9 : 5), 1).get() & 255;
        Assert.assertEquals(flags & 33, 33, "Class-only placement must carry inherited matrix");
        Assert.assertFalse(result.characters.containsKey(second.getAddress() + (wide ? 48 : 40)), "Do not add a SWF character ID for a class-only placement");
        Assert.assertFalse(replacement.placeFlagHasMatrix);
    }
}
