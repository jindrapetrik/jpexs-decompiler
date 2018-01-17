/*
 *  Copyright (C) 2010-2018 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.gui.tagtree;

import com.jpexs.decompiler.flash.tags.CSMTextSettingsTag;
import com.jpexs.decompiler.flash.tags.DebugIDTag;
import com.jpexs.decompiler.flash.tags.DefineBinaryDataTag;
import com.jpexs.decompiler.flash.tags.DefineBitsJPEG2Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsJPEG3Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsJPEG4Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsLossless2Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsLosslessTag;
import com.jpexs.decompiler.flash.tags.DefineBitsTag;
import com.jpexs.decompiler.flash.tags.DefineButton2Tag;
import com.jpexs.decompiler.flash.tags.DefineButtonCxformTag;
import com.jpexs.decompiler.flash.tags.DefineButtonSoundTag;
import com.jpexs.decompiler.flash.tags.DefineButtonTag;
import com.jpexs.decompiler.flash.tags.DefineEditTextTag;
import com.jpexs.decompiler.flash.tags.DefineFont2Tag;
import com.jpexs.decompiler.flash.tags.DefineFont3Tag;
import com.jpexs.decompiler.flash.tags.DefineFont4Tag;
import com.jpexs.decompiler.flash.tags.DefineFontAlignZonesTag;
import com.jpexs.decompiler.flash.tags.DefineFontInfo2Tag;
import com.jpexs.decompiler.flash.tags.DefineFontInfoTag;
import com.jpexs.decompiler.flash.tags.DefineFontNameTag;
import com.jpexs.decompiler.flash.tags.DefineFontTag;
import com.jpexs.decompiler.flash.tags.DefineMorphShape2Tag;
import com.jpexs.decompiler.flash.tags.DefineMorphShapeTag;
import com.jpexs.decompiler.flash.tags.DefineScalingGridTag;
import com.jpexs.decompiler.flash.tags.DefineSceneAndFrameLabelDataTag;
import com.jpexs.decompiler.flash.tags.DefineShape2Tag;
import com.jpexs.decompiler.flash.tags.DefineShape3Tag;
import com.jpexs.decompiler.flash.tags.DefineShape4Tag;
import com.jpexs.decompiler.flash.tags.DefineShapeTag;
import com.jpexs.decompiler.flash.tags.DefineSoundTag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.DefineText2Tag;
import com.jpexs.decompiler.flash.tags.DefineTextTag;
import com.jpexs.decompiler.flash.tags.DefineVideoStreamTag;
import com.jpexs.decompiler.flash.tags.DoABC2Tag;
import com.jpexs.decompiler.flash.tags.DoABCTag;
import com.jpexs.decompiler.flash.tags.DoActionTag;
import com.jpexs.decompiler.flash.tags.DoInitActionTag;
import com.jpexs.decompiler.flash.tags.EnableDebugger2Tag;
import com.jpexs.decompiler.flash.tags.EnableDebuggerTag;
import com.jpexs.decompiler.flash.tags.EnableTelemetryTag;
import com.jpexs.decompiler.flash.tags.EndTag;
import com.jpexs.decompiler.flash.tags.ExportAssetsTag;
import com.jpexs.decompiler.flash.tags.FileAttributesTag;
import com.jpexs.decompiler.flash.tags.FrameLabelTag;
import com.jpexs.decompiler.flash.tags.ImportAssets2Tag;
import com.jpexs.decompiler.flash.tags.ImportAssetsTag;
import com.jpexs.decompiler.flash.tags.JPEGTablesTag;
import com.jpexs.decompiler.flash.tags.MetadataTag;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.PlaceObject3Tag;
import com.jpexs.decompiler.flash.tags.PlaceObject4Tag;
import com.jpexs.decompiler.flash.tags.PlaceObjectTag;
import com.jpexs.decompiler.flash.tags.ProductInfoTag;
import com.jpexs.decompiler.flash.tags.ProtectTag;
import com.jpexs.decompiler.flash.tags.RemoveObject2Tag;
import com.jpexs.decompiler.flash.tags.RemoveObjectTag;
import com.jpexs.decompiler.flash.tags.ScriptLimitsTag;
import com.jpexs.decompiler.flash.tags.SetBackgroundColorTag;
import com.jpexs.decompiler.flash.tags.SetTabIndexTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.SoundStreamBlockTag;
import com.jpexs.decompiler.flash.tags.SoundStreamHead2Tag;
import com.jpexs.decompiler.flash.tags.SoundStreamHeadTag;
import com.jpexs.decompiler.flash.tags.StartSound2Tag;
import com.jpexs.decompiler.flash.tags.StartSoundTag;
import com.jpexs.decompiler.flash.tags.SymbolClassTag;
import com.jpexs.decompiler.flash.tags.VideoFrameTag;
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
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author JPEXS
 */
public class TagIdClassMap {

    private static final Map<Integer, Class<?>> tagIdClassMap = new HashMap<>();

    private static final Map<Class, Integer> classTagIdMap = new HashMap<>();

    static {
        addTag(CSMTextSettingsTag.ID, CSMTextSettingsTag.class);
        addTag(DebugIDTag.ID, DebugIDTag.class);
        addTag(DefineBinaryDataTag.ID, DefineBinaryDataTag.class);
        addTag(DefineBitsJPEG2Tag.ID, DefineBitsJPEG2Tag.class);
        addTag(DefineBitsJPEG3Tag.ID, DefineBitsJPEG3Tag.class);
        addTag(DefineBitsJPEG4Tag.ID, DefineBitsJPEG4Tag.class);
        addTag(DefineBitsLossless2Tag.ID, DefineBitsLossless2Tag.class);
        addTag(DefineBitsLosslessTag.ID, DefineBitsLosslessTag.class);
        addTag(DefineBitsTag.ID, DefineBitsTag.class);
        addTag(DefineButton2Tag.ID, DefineButton2Tag.class);
        addTag(DefineButtonCxformTag.ID, DefineButtonCxformTag.class);
        addTag(DefineButtonSoundTag.ID, DefineButtonSoundTag.class);
        addTag(DefineButtonTag.ID, DefineButtonTag.class);
        addTag(DefineEditTextTag.ID, DefineEditTextTag.class);
        addTag(DefineFont2Tag.ID, DefineFont2Tag.class);
        addTag(DefineFont3Tag.ID, DefineFont3Tag.class);
        addTag(DefineFont4Tag.ID, DefineFont4Tag.class);
        addTag(DefineFontAlignZonesTag.ID, DefineFontAlignZonesTag.class);
        addTag(DefineFontInfo2Tag.ID, DefineFontInfo2Tag.class);
        addTag(DefineFontInfoTag.ID, DefineFontInfoTag.class);
        addTag(DefineFontNameTag.ID, DefineFontNameTag.class);
        addTag(DefineFontTag.ID, DefineFontTag.class);
        addTag(DefineMorphShape2Tag.ID, DefineMorphShape2Tag.class);
        addTag(DefineMorphShapeTag.ID, DefineMorphShapeTag.class);
        addTag(DefineScalingGridTag.ID, DefineScalingGridTag.class);
        addTag(DefineSceneAndFrameLabelDataTag.ID, DefineSceneAndFrameLabelDataTag.class);
        addTag(DefineShape2Tag.ID, DefineShape2Tag.class);
        addTag(DefineShape3Tag.ID, DefineShape3Tag.class);
        addTag(DefineShape4Tag.ID, DefineShape4Tag.class);
        addTag(DefineShapeTag.ID, DefineShapeTag.class);
        addTag(DefineSoundTag.ID, DefineSoundTag.class);
        addTag(DefineSpriteTag.ID, DefineSpriteTag.class);
        addTag(DefineText2Tag.ID, DefineText2Tag.class);
        addTag(DefineTextTag.ID, DefineTextTag.class);
        addTag(DefineVideoStreamTag.ID, DefineVideoStreamTag.class);
        addTag(DoABC2Tag.ID, DoABC2Tag.class);
        addTag(DoABCTag.ID, DoABCTag.class);
        addTag(DoActionTag.ID, DoActionTag.class);
        addTag(DoInitActionTag.ID, DoInitActionTag.class);
        addTag(EnableDebugger2Tag.ID, EnableDebugger2Tag.class);
        addTag(EnableDebuggerTag.ID, EnableDebuggerTag.class);
        addTag(EnableTelemetryTag.ID, EnableTelemetryTag.class);
        addTag(EndTag.ID, EndTag.class);
        addTag(ExportAssetsTag.ID, ExportAssetsTag.class);
        addTag(FileAttributesTag.ID, FileAttributesTag.class);
        addTag(FrameLabelTag.ID, FrameLabelTag.class);
        addTag(ImportAssets2Tag.ID, ImportAssets2Tag.class);
        addTag(ImportAssetsTag.ID, ImportAssetsTag.class);
        addTag(JPEGTablesTag.ID, JPEGTablesTag.class);
        addTag(MetadataTag.ID, MetadataTag.class);
        addTag(PlaceObject2Tag.ID, PlaceObject2Tag.class);
        addTag(PlaceObject3Tag.ID, PlaceObject3Tag.class);
        addTag(PlaceObject4Tag.ID, PlaceObject4Tag.class);
        addTag(PlaceObjectTag.ID, PlaceObjectTag.class);
        addTag(ProductInfoTag.ID, ProductInfoTag.class);
        addTag(ProtectTag.ID, ProtectTag.class);
        addTag(RemoveObject2Tag.ID, RemoveObject2Tag.class);
        addTag(RemoveObjectTag.ID, RemoveObjectTag.class);
        addTag(ScriptLimitsTag.ID, ScriptLimitsTag.class);
        addTag(SetBackgroundColorTag.ID, SetBackgroundColorTag.class);
        addTag(SetTabIndexTag.ID, SetTabIndexTag.class);
        addTag(ShowFrameTag.ID, ShowFrameTag.class);
        addTag(SoundStreamBlockTag.ID, SoundStreamBlockTag.class);
        addTag(SoundStreamHead2Tag.ID, SoundStreamHead2Tag.class);
        addTag(SoundStreamHeadTag.ID, SoundStreamHeadTag.class);
        addTag(StartSound2Tag.ID, StartSound2Tag.class);
        addTag(StartSoundTag.ID, StartSoundTag.class);
        addTag(SymbolClassTag.ID, SymbolClassTag.class);
        addTag(VideoFrameTag.ID, VideoFrameTag.class);
        addTag(DefineCompactedFont.ID, DefineCompactedFont.class);
        addTag(DefineExternalGradient.ID, DefineExternalGradient.class);
        addTag(DefineExternalImage.ID, DefineExternalImage.class);
        addTag(DefineExternalImage2.ID, DefineExternalImage2.class);
        addTag(DefineExternalSound.ID, DefineExternalSound.class);
        addTag(DefineExternalStreamSound.ID, DefineExternalStreamSound.class);
        addTag(DefineGradientMap.ID, DefineGradientMap.class);
        addTag(DefineSubImage.ID, DefineSubImage.class);
        addTag(ExporterInfo.ID, ExporterInfo.class);
        addTag(FontTextureInfo.ID, FontTextureInfo.class);
    }

    private static void addTag(int tagId, Class<?> cl) {
        tagIdClassMap.put(tagId, cl);
        classTagIdMap.put(cl, tagId);
    }

    public static Class<?> getClassByTagId(int tagId) {
        return tagIdClassMap.get(tagId);
    }

    public static Integer getTagIdByClass(Class cl) {
        return classTagIdMap.get(cl);
    }
}
