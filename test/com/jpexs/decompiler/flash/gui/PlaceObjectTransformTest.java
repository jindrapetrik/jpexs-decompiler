/*
 *  Copyright (C) 2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.gui;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.PlaceObject3Tag;
import com.jpexs.decompiler.flash.tags.PlaceObject4Tag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.helpers.ByteArrayRange;
import java.io.ByteArrayOutputStream;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class PlaceObjectTransformTest {

    @DataProvider(name = "placements")
    public Object[][] placements() {
        return new Object[][]{{2, false}, {3, false}, {4, false}, {2, true}, {3, true}, {4, true}};
    }

    @Test(dataProvider = "placements")
    public void moveWithoutMatrixUsesLatestInheritedTransform(int version, boolean nested) throws Exception {
        for (Matrix transform : new Matrix[]{Matrix.getTranslateInstance(240, -120),
            new Matrix(0, 0, 1, -1, 240, -120)}) {
            SWF swf = new SWF();
            Timelined timeline = nested ? new DefineSpriteTag(swf) : swf;
            PlaceObjectTypeTag initial = place(swf, version, timeline, false, 7, Matrix.getTranslateInstance(100, 200).toMATRIX());
            timeline.addTag(new ShowFrameTag(swf));
            MATRIX inherited = new Matrix(2, 0.5, 0.25, -0.5, 1200, -800).toMATRIX();
            PlaceObjectTypeTag previous = place(swf, version, timeline, true, 7, inherited);
            // A placement at another depth must not supply the inherited matrix.
            place(swf, version, timeline, false, 8, Matrix.getTranslateInstance(9000, 9000).toMATRIX());
            timeline.addTag(new ShowFrameTag(swf));
            PlaceObjectTypeTag unchanged = place(swf, version, timeline, true, 7, null);
            timeline.addTag(new ShowFrameTag(swf));
            PlaceObjectTypeTag edited = place(swf, version, timeline, true, 7, null);
            timeline.addTag(new ShowFrameTag(swf));

            assertNull(edited.getMatrix());
            assertSame(timeline.getTimeline().getDepthState(3, 7).matrix, inherited);
            PreviewPanel.applyPlaceObjectTransform(edited, 3, transform);

            MATRIX expected = transform.concatenate(new Matrix(inherited)).toMATRIX();
            assertEquals(edited.getMatrix(), expected);
            assertTrue(edited.flagMove());
            assertSame(previous.getMatrix(), inherited);
            assertNull(unchanged.getMatrix());
            assertEquals(initial.getMatrix().translateX, 100);
            timeline.resetTimeline();
            assertEquals(timeline.getTimeline().getDepthState(3, 7).matrix, expected);
            assertSame(timeline.getTimeline().getDepthState(2, 7).matrix, inherited);

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (SWFOutputStream output = new SWFOutputStream(bytes, swf.version, swf.getCharset())) {
                edited.getData(output);
            }
            PlaceObjectTypeTag restored = createPlace(swf, version);
            byte[] data = bytes.toByteArray();
            restored.readData(new SWFInputStream(swf, data), new ByteArrayRange(data), 0, false, false, false);
            assertEquals(restored.getMatrix(), expected);
            assertTrue(restored.flagMove());
        }
    }

    @Test(dataProvider = "placements")
    public void explicitMatrixAndImplicitIdentityStillWork(int version, boolean nested) {
        for (MATRIX original : new MATRIX[]{null, new Matrix(0.5, 2, -0.25, 0.75, 400, 600).toMATRIX()}) {
            SWF swf = new SWF();
            Timelined timeline = nested ? new DefineSpriteTag(swf) : swf;
            PlaceObjectTypeTag edited = place(swf, version, timeline, false, 7, original);
            timeline.addTag(new ShowFrameTag(swf));
            Matrix transform = Matrix.getTranslateInstance(120, -80);

            PreviewPanel.applyPlaceObjectTransform(edited, 0, transform);

            assertEquals(edited.getMatrix(), transform.concatenate(new Matrix(original)).toMATRIX());
            assertFalse(edited.flagMove());
        }
    }

    private PlaceObjectTypeTag place(SWF swf, int version, Timelined timeline, boolean move, int depth, MATRIX matrix) {
        PlaceObjectTypeTag tag = createPlace(swf, version);
        tag.setTimelined(timeline);
        tag.setDepth(depth);
        tag.setPlaceFlagMove(move);
        tag.setCharacterId(move ? -1 : 1);
        tag.setMatrix(matrix);
        tag.setPlaceFlagHasMatrix(matrix != null);
        timeline.addTag(tag);
        return tag;
    }

    private PlaceObjectTypeTag createPlace(SWF swf, int version) {
        switch (version) {
            case 2:
                return new PlaceObject2Tag(swf);
            case 3:
                return new PlaceObject3Tag(swf);
            default:
                return new PlaceObject4Tag(swf);
        }
    }
}
