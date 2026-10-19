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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.flash;

import com.jpexs.decompiler.flash.tags.*;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DefineSpriteBoundsTest {

    private static SWF movie(int size) {
        SWF swf = new SWF();
        DefineShape4Tag shape = new DefineShape4Tag(swf);
        shape.shapeId = 1; shape.shapeBounds = new RECT(0, size, 0, size + 20);
        shape.edgeBounds = new RECT(shape.shapeBounds); swf.addTag(shape);
        SymbolClassTag classes = new SymbolClassTag(swf);
        classes.tags.add(1); classes.names.add("ui.Widget"); swf.addTag(classes);
        return swf;
    }

    private static DefineSpriteTag sprite(SWF swf) {
        DefineSpriteTag sprite = new DefineSpriteTag(swf);
        sprite.spriteId = 2; sprite.frameCount = 1; swf.addTag(sprite); return sprite;
    }

    private static PlaceObject3Tag place(SWF swf, String name, int id, boolean move, int x, int y) {
        PlaceObject3Tag place = new PlaceObject3Tag(swf);
        place.depth = 1; place.placeFlagMove = move;
        place.placeFlagHasClassName = name != null; place.className = name;
        place.placeFlagHasCharacter = id >= 0; place.characterId = id;
        place.placeFlagHasMatrix = true; place.matrix = new MATRIX();
        place.matrix.translateX = x; place.matrix.translateY = y; return place;
    }

    private static SWF read(SWF source, final SWF external) throws Exception {
        ByteArrayOutputStream data = new ByteArrayOutputStream(); source.saveTo(data);
        UrlResolver resolver = external == null ? null : new UrlResolver() {
            @Override public boolean doIgnoreUrl(String base, String url) { return false; }
            @Override public SWF resolveUrl(String base, String url) { return external; }
        };
        return new SWF(new ByteArrayInputStream(data.toByteArray()), "root.swf", "root.swf",
                null, false, false, false, resolver, "UTF-8");
    }

    private static void bounds(SWF swf, int xmin, int xmax, int ymin, int ymax) {
        RECT rect = ((DefineSpriteTag) swf.getCharacter(2)).getRect();
        Assert.assertEquals(rect.Xmin, xmin); Assert.assertEquals(rect.Xmax, xmax);
        Assert.assertEquals(rect.Ymin, ymin); Assert.assertEquals(rect.Ymax, ymax);
    }

    @Test
    public void localClassPlacementContributesTransformedBounds() throws Exception {
        SWF swf = movie(40); sprite(swf).addTag(place(swf, "ui.Widget", -1, false, 10, 20));
        bounds(read(swf, null), 10, 50, 20, 80);
    }

    @Test
    public void numericCharacterPlacementKeepsExistingBehavior() throws Exception {
        SWF swf = movie(40); sprite(swf).addTag(place(swf, null, 1, false, 10, 20));
        bounds(read(swf, null), 10, 50, 20, 80);
    }

    @Test
    public void importedClassUsesForeignTagEvenWhenLocalIdCollides() throws Exception {
        SWF external = read(movie(40), null), swf = movie(1000);
        ImportAssets2Tag imports = new ImportAssets2Tag(swf); imports.url = "library.swf"; swf.addTag(imports);
        sprite(swf).addTag(place(swf, "ui.Widget", -1, false, 10, 20));
        bounds(read(swf, external), 10, 50, 20, 80);
    }

    @Test
    public void moveWithoutCharacterRetainsImportedClassAtDepth() throws Exception {
        SWF external = read(movie(40), null), swf = movie(1000);
        ImportAssets2Tag imports = new ImportAssets2Tag(swf); imports.url = "library.swf"; swf.addTag(imports);
        DefineSpriteTag sprite = sprite(swf);
        sprite.addTag(place(swf, "ui.Widget", -1, false, 10, 20));
        sprite.addTag(place(swf, null, -1, true, 100, 200));
        bounds(read(swf, external), 10, 140, 20, 260);
    }

    @Test
    public void removeClearsClassPlacementBeforeSubsequentMove() throws Exception {
        SWF swf = movie(40); DefineSpriteTag sprite = sprite(swf);
        sprite.addTag(place(swf, "ui.Widget", -1, false, 10, 20));
        RemoveObject2Tag remove = new RemoveObject2Tag(swf); remove.depth = 1; sprite.addTag(remove);
        sprite.addTag(place(swf, null, -1, true, 100, 200));
        bounds(read(swf, null), 10, 50, 20, 80);
    }

    @Test
    public void missingClassIsIgnored() throws Exception {
        SWF swf = movie(40); sprite(swf).addTag(place(swf, "ui.Missing", -1, false, 10, 20));
        bounds(read(swf, null), 0, 0, 0, 0);
    }

    @Test
    public void recursiveClassPlacementTerminates() throws Exception {
        SWF swf = movie(40); DefineSpriteTag sprite = sprite(swf);
        SymbolClassTag classes = new SymbolClassTag(swf); classes.tags.add(2); classes.names.add("ui.Self"); swf.addTag(classes);
        sprite.addTag(place(swf, "ui.Self", -1, false, 0, 0));
        swf.updateCharacters(); swf.assignClassesToSymbols();
        bounds(swf, 0, 0, 0, 0);
    }
}
