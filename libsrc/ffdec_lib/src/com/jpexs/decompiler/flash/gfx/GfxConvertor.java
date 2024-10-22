/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.gfx;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.DefineBitsLossless2Tag;
import com.jpexs.decompiler.flash.tags.DefineFont2Tag;
import com.jpexs.decompiler.flash.tags.DefineSoundTag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.ImportAssets2Tag;
import com.jpexs.decompiler.flash.tags.ImportAssetsTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.gfx.DefineCompactedFont;
import com.jpexs.decompiler.flash.tags.gfx.DefineExternalGradient;
import com.jpexs.decompiler.flash.tags.gfx.DefineExternalImage;
import com.jpexs.decompiler.flash.tags.gfx.DefineExternalImage2;
import com.jpexs.decompiler.flash.tags.gfx.DefineExternalSound;
import com.jpexs.decompiler.flash.tags.gfx.DefineExternalStreamSound;
import com.jpexs.decompiler.flash.tags.gfx.DefineGradientMap;
import com.jpexs.decompiler.flash.tags.gfx.DefineSubImage;
import com.jpexs.decompiler.flash.tags.gfx.ExporterInfo;
import com.jpexs.decompiler.flash.tags.gfx.FontTextureInfo;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.types.KERNINGRECORD;
import com.jpexs.decompiler.flash.types.LANGCODE;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.decompiler.flash.types.gfx.FontType;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.Helper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Converts GFX SWF to normal SWF.
 *
 * @author JPEXS
 */
public class GfxConvertor {

    /**
     * Converts DefineCompactedFont to DefineFont2Tag.
     * @param compactedFont DefineCompactedFont
     * @return DefineFont2Tag
     */
    public DefineFont2Tag convertDefineCompactedFont(DefineCompactedFont compactedFont) {
        DefineFont2Tag ret = new DefineFont2Tag(compactedFont.getSwf());
        ret.fontID = compactedFont.getCharacterId();
        ret.fontFlagsBold = compactedFont.isBold();
        ret.fontFlagsItalic = compactedFont.isItalic();
        ret.fontFlagsWideOffsets = true;
        ret.fontFlagsWideCodes = true;
        ret.fontFlagsHasLayout = true;
        ret.fontAscent = compactedFont.resize(compactedFont.getAscent());
        ret.fontDescent = compactedFont.resize(compactedFont.getDescent());
        ret.fontLeading = compactedFont.resize(compactedFont.getLeading());
        ret.fontAdvanceTable = new ArrayList<>();
        ret.fontBoundsTable = new ArrayList<>();
        ret.codeTable = new ArrayList<>();
        ret.glyphShapeTable = new ArrayList<>();
        List<SHAPE> shp = compactedFont.getGlyphShapeTable();
        for (int g = 0; g < shp.size(); g++) {
            ret.fontAdvanceTable.add((int) compactedFont.getGlyphAdvance(g)); //already resized
            ret.codeTable.add((int) compactedFont.glyphToChar(g));

            SHAPE shpX = compactedFont.resizeShape(shp.get(g));
            ret.glyphShapeTable.add(shpX);
            ret.fontBoundsTable.add(compactedFont.getGlyphBounds(g));
        }
        ret.fontName = compactedFont.getFontNameIntag();
        ret.languageCode = new LANGCODE(1);
        ret.fontKerningTable = new ArrayList<>();

        FontType ft = compactedFont.fonts.get(0);
        for (int i = 0; i < ft.kerning.size(); i++) {
            KERNINGRECORD kr = new KERNINGRECORD();
            kr.fontKerningAdjustment = compactedFont.resize(ft.kerning.get(i).advance);
            kr.fontKerningCode1 = ft.kerning.get(i).char1;
            kr.fontKerningCode2 = ft.kerning.get(i).char2;
            ret.fontKerningTable.add(kr);
        }
        return ret;
    }

    /**
     * Converts DefineSubImage to DefineBitsLossless2Tag.
     * @param defineSubImage DefineSubImage
     * @return DefineBitsLossless2Tag
     */
    public DefineBitsLossless2Tag convertDefineSubImage(DefineSubImage defineSubImage) {
        DefineBitsLossless2Tag ret = new DefineBitsLossless2Tag(defineSubImage.getSwf());
        ret.characterID = defineSubImage.characterID;
        ret.bitmapWidth = defineSubImage.getImageDimension().width;
        ret.bitmapHeight = defineSubImage.getImageDimension().height;
        ret.bitmapFormat = DefineBitsLossless2Tag.FORMAT_32BIT_ARGB;
        try {
            ret.setImage(Helper.readStream(defineSubImage.getImageData()));
        } catch (IOException ex) {
            Logger.getLogger(GfxConvertor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    /**
     * Converts DefineExternalImage to DefineBitsLossless2Tag.
     * @param defineExternalImage DefineExternalImage
     * @return DefineBitsLossless2Tag
     */
    public DefineBitsLossless2Tag convertDefineExternalImage(DefineExternalImage defineExternalImage) {
        DefineBitsLossless2Tag ret = new DefineBitsLossless2Tag(defineExternalImage.getSwf());
        ret.characterID = defineExternalImage.characterID;
        ret.bitmapWidth = defineExternalImage.getImageDimension().width;
        ret.bitmapHeight = defineExternalImage.getImageDimension().height;
        ret.bitmapFormat = DefineBitsLossless2Tag.FORMAT_32BIT_ARGB;
        try {
            ret.setImage(Helper.readStream(defineExternalImage.getImageData()));
        } catch (IOException ex) {
            Logger.getLogger(GfxConvertor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    /**
     * Converts DefineExternalSound to DefineSoundTag.
     * @param defineExternalSound DefineExternalSound
     * @return DefineSoundTag
     */
    public DefineSoundTag convertDefineExternalSound(DefineExternalSound defineExternalSound) {
        DefineSoundTag ret = new DefineSoundTag(defineExternalSound.getSwf());
        ret.soundId = defineExternalSound.characterId;
        ret.soundFormat = defineExternalSound.getSoundFormatId();
        ret.soundRate = defineExternalSound.getSoundRate();
        ret.soundSampleCount = defineExternalSound.getTotalSoundSampleCount();
        ret.soundSize = defineExternalSound.getSoundSize();
        ret.soundType = defineExternalSound.getSoundType();
        ret.soundData = new ByteArrayRange(defineExternalSound.getRawSoundData().get(0).getRangeData());
        return ret;
    }

    /**
     * Converts DefineExternalStreamSound to list of tags.
     * @param defineExternalStreamSound DefineExternalStreamSound
     * @return List of tags
     */
    public List<Tag> convertDefineExternalStreamSound(DefineExternalStreamSound defineExternalStreamSound) {
        List<Tag> ret = new ArrayList<>();

        //TODO: distribute stream to particular frames
        /*SoundStreamHead2Tag head = new SoundStreamHead2Tag(defineExternalStreamSound.getSwf());
        head.streamSoundCompression = defineExternalStreamSound.getSoundFormatId();
        head.streamSoundRate = defineExternalStreamSound.getSoundRate();
        head.streamSoundSampleCount = 0; //(int)defineExternalStreamSound.getTotalSoundSampleCount();
        head.streamSoundSize = defineExternalStreamSound.getSoundSize();
        head.streamSoundType = defineExternalStreamSound.getSoundType();
        
        SoundStreamBlockTag block = new SoundStreamBlockTag(defineExternalStreamSound.getSwf());
        block.streamSoundData = new ByteArrayRange(defineExternalStreamSound.getRawSoundData().get(0).getRangeData());
        
        ret.add(head);
        ret.add(block);*/
        return ret;
    }

    /**
     * Converts DefineSpriteTag.
     * @param defineSprite DefineSpriteTag
     * @return DefineSpriteTag
     */
    public DefineSpriteTag convertDefineSprite(DefineSpriteTag defineSprite) {
        DefineSpriteTag ret = new DefineSpriteTag(defineSprite.getSwf());
        ret.frameCount = defineSprite.frameCount;
        ret.hasEndTag = defineSprite.hasEndTag;
        ret.spriteId = defineSprite.spriteId;
        convertTags(defineSprite, ret);
        return ret;
    }

    /**
     * Converts tags.
     * @param source Source
     * @param target Target
     * @return List of tags
     */
    public List<Tag> convertTags(Timelined source, Timelined target) {
        List<Tag> ret = new ArrayList<>();
        for (Tag t : source.getTags()) {
            List<Tag> convertedTags = convertTag(t);
            for (Tag ct : convertedTags) {
                target.addTag(ct);
            }
        }
        return ret;
    }

    /**
     * Converts tag.
     * @param tag Tag
     * @return List of tags
     */
    public List<Tag> convertTag(Tag tag) {
        List<Tag> ret = new ArrayList<>();
        if (tag instanceof DefineCompactedFont) {
            ret.add(convertDefineCompactedFont((DefineCompactedFont) tag));
            return ret;
        }
        if (tag instanceof DefineExternalGradient) {
            return ret;
        }
        if (tag instanceof DefineExternalImage) {
            ret.add(convertDefineExternalImage((DefineExternalImage) tag));
            return ret;
        }

        if (tag instanceof DefineExternalImage2) {
            return ret;
        }

        if (tag instanceof DefineExternalSound) {
            ret.add(convertDefineExternalSound((DefineExternalSound) tag));
            return ret;
        }

        if (tag instanceof DefineExternalStreamSound) {
            return convertDefineExternalStreamSound((DefineExternalStreamSound) tag);
        }

        if (tag instanceof DefineGradientMap) {
            return ret;
        }

        if (tag instanceof DefineSubImage) {
            ret.add(convertDefineSubImage((DefineSubImage) tag));
            return ret;
        }

        if (tag instanceof ExporterInfo) {
            return ret;
        }

        if (tag instanceof FontTextureInfo) {
            return ret;
        }

        if (tag instanceof ImportAssetsTag) {
            return ret;
        }

        if (tag instanceof ImportAssets2Tag) {
            return ret;
        }

        if (tag instanceof DefineSpriteTag) {
            ret.add(convertDefineSprite((DefineSpriteTag) tag));
            return ret;
        }

        ret.add(tag);
        return ret;
    }

    /**
     * Converts GFX SWF to normal SWF.
     * @param gfxSwf GFX SWF
     * @return Normal SWF
     */
    public SWF convertSwf(SWF gfxSwf) {
        if (!gfxSwf.gfx) {
            return gfxSwf;
        }
        SWF ret = new SWF();
        ret.displayRect = gfxSwf.displayRect;
        ret.frameRate = gfxSwf.frameRate;
        ret.compression = gfxSwf.compression;
        ret.frameCount = gfxSwf.frameCount;
        ret.gfx = false;
        ret.hasEndTag = gfxSwf.hasEndTag;
        convertTags(gfxSwf, ret);

        return ret;
    }
}
