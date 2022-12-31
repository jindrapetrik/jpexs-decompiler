/*
 *  Copyright (C) 2010-2022 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.types.KERNINGRECORD;
import com.jpexs.decompiler.flash.types.LANGCODE;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.decompiler.flash.types.gfx.FontType;
import com.jpexs.helpers.Helper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Converts GFX SWF to normal SWF.
 * @author JPEXS
 */
public class GfxConvertor {
    
    public DefineFont2Tag convertDefineCompactedFont(DefineCompactedFont compactedFont) {
        DefineFont2Tag ret = new DefineFont2Tag(compactedFont.getSwf());
        ret.fontID = compactedFont.getFontId();
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
    
    public Tag convertTag(Tag tag) {
        if (tag instanceof DefineCompactedFont) {
            return convertDefineCompactedFont((DefineCompactedFont)tag);
        }
        if (tag instanceof DefineExternalGradient) {
            return null;
        }
        if (tag instanceof DefineExternalImage) {
            return convertDefineExternalImage((DefineExternalImage) tag);
        }
        
        if (tag instanceof DefineExternalImage2) {
            return null;
        }
        
        if (tag instanceof DefineExternalSound) {
            return null;
        }
        
        if (tag instanceof DefineExternalStreamSound) {
            return null;
        }
        
        if (tag instanceof DefineGradientMap) {
            return null;
        }
        
        if (tag instanceof DefineSubImage) {
            return convertDefineSubImage((DefineSubImage) tag);
        }
                        
        if (tag instanceof ExporterInfo) {
            return null;
        }
        
        if (tag instanceof FontTextureInfo) {
            return null;
        }        
        
        if (tag instanceof ImportAssetsTag) {
            return null;
        }
        
        if (tag instanceof ImportAssets2Tag) {
            return null;
        }
                
        return tag;
    }
    
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
        for (Tag t:gfxSwf.getTags()) {
            Tag ct = convertTag(t);
            if (ct != null) {
                ret.addTag(ct);
            }
        }
        
        return ret;       
    }
}
