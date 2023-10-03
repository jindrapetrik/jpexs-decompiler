/*
 *  Copyright (C) 2010-2023 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.importers;

import at.dhyan.open_imaging.GifDecoder;
import at.dhyan.open_imaging.GifDecoder.GifImage;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.DefineShape2Tag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.RemoveObjectTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.CharacterIdTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.timeline.Timelined;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.imageio.ImageIO;

/**
 *
 * @author JPEXS
 */
public class SpriteImporter {

    private void removeCharacters(Set<Integer> usedCharacters, SWF swf) {
        for (int ch:usedCharacters) {
            CharacterTag ct = swf.getCharacter(ch);
            if (ct == null) {
                continue;
            }
            if (!ct.getClassNames().isEmpty()) {
                continue;
            }
            if (ct.getExportName() != null) {
                continue;
            }
            Set<Integer> dependentCharacters = swf.getDependentCharacters(ch);            
            if (dependentCharacters.isEmpty()) {
                Set<Integer> needed = new LinkedHashSet<>();
                ct.getNeededCharacters(needed, swf);
                List<CharacterIdTag> attachedTags = swf.getCharacterIdTags(ch);
                for (CharacterIdTag cit : attachedTags) {
                    if (cit instanceof Tag) {
                        Tag citt = (Tag) cit;
                        citt.getTimelined().removeTag(citt);
                    }
                }
                ct.getTimelined().removeTag(ct);
                swf.computeDependentCharacters();
                removeCharacters(needed, swf);
            }
        }
    }
    
    public boolean importSprite(DefineSpriteTag spriteTag, InputStream is) throws IOException {
        final GifImage gif = GifDecoder.read(is);
        final int frameCount = gif.getFrameCount();
        Set<Integer> usedCharacters = new LinkedHashSet<>();
        for (int i = spriteTag.getTags().size() - 1; i >= 0; i--) {
            Tag t = spriteTag.getTags().get(i);
            if (t instanceof PlaceObjectTypeTag) {
                PlaceObjectTypeTag pt = (PlaceObjectTypeTag)t;
                int characterId = pt.getCharacterId();
                if (characterId != -1) {
                    usedCharacters.add(characterId);
                }
            }
            spriteTag.removeTag(i);
        }
        spriteTag.getSwf().computeDependentCharacters();
        
        removeCharacters(usedCharacters, spriteTag.getSwf());
        

        ShapeImporter shapeImporter = new ShapeImporter();
        SWF swf = spriteTag.getSwf();

        float swfFrameRate = swf.frameRate;
        int gifFrameTimeMs = 0;
        int gifFrame = 0;
        int swfFrame = 0;
        int lastGifFrame = -1;
        int gifFrameCount = gif.getFrameCount();
        while (gifFrame < gifFrameCount) {

            if (lastGifFrame != gifFrame) {
                final BufferedImage img = gif.getFrame(gifFrame);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(img, "png", baos);
                byte frameImageData[] = baos.toByteArray();
                DefineShape2Tag shapeTag = new DefineShape2Tag(spriteTag.getSwf());
                SWF.addTagBefore(shapeTag, spriteTag);
                shapeImporter.importImage(shapeTag, frameImageData, 0, false);

                PlaceObject2Tag placeObject = new PlaceObject2Tag(swf);
                placeObject.placeFlagHasCharacter = true;
                placeObject.characterId = shapeTag.shapeId;
                placeObject.depth = 1;

                if (swfFrame > 0) {
                    placeObject.placeFlagMove = true;
                }

                spriteTag.addTag(placeObject);
                placeObject.setTimelined(spriteTag);
            }

            ShowFrameTag showFrame = new ShowFrameTag(swf);
            spriteTag.addTag(showFrame);
            showFrame.setTimelined(spriteTag);
            lastGifFrame = gifFrame;
            swfFrame++;
            float swfFrameTimeMs = swfFrame * 100 / swfFrameRate;            
            while (gifFrame < gifFrameCount && gifFrameTimeMs + gif.getDelay(gifFrame) < swfFrameTimeMs) {
                gifFrameTimeMs += gif.getDelay(gifFrame);
                gifFrame++;
            }
        }
        spriteTag.frameCount = frameCount;
        spriteTag.hasEndTag = true;
        spriteTag.resetTimeline();
        swf.resetTimeline();

        return true;
    }
}
