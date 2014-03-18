/*
 *  Copyright (C) 2010-2014 JPEXS
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
package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.ContainerItem;
import com.jpexs.decompiler.flash.tags.base.Exportable;
import com.jpexs.decompiler.flash.tags.base.NeedsCharacters;
import com.jpexs.decompiler.flash.tags.gfx.DefineCompactedFont;
import com.jpexs.decompiler.flash.tags.gfx.DefineExternalGradient;
import com.jpexs.decompiler.flash.tags.gfx.DefineExternalImage;
import com.jpexs.decompiler.flash.tags.gfx.DefineExternalImage2;
import com.jpexs.decompiler.flash.tags.gfx.DefineExternalSound;
import com.jpexs.decompiler.flash.tags.gfx.DefineExternalStreamSound;
import com.jpexs.decompiler.flash.tags.gfx.DefineGradientMap;
import com.jpexs.decompiler.flash.tags.gfx.DefineSubImage;
import com.jpexs.decompiler.flash.tags.gfx.ExporterInfoTag;
import com.jpexs.decompiler.flash.tags.gfx.FontTextureInfo;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents Tag inside SWF file
 */
public class Tag implements NeedsCharacters, Exportable, ContainerItem, Serializable {

    /**
     * Identifier of tag type
     */
    protected int id;
    /**
     * Data in the tag
     */
    protected byte[] data;
    /**
     * If true, then Tag is written to the stream as longer than 0x3f even if it
     * is not
     */
    @Internal
    public boolean forceWriteAsLong = false;
    @Internal
    private final long pos;
    protected String tagName;
    @Internal
    public Tag previousTag;
    @Internal
    protected transient SWF swf;
    @Internal
    protected transient Timelined timelined;
    @Internal
    private boolean modified;

    public String getTagName() {
        return tagName;
    }

    public String getName() {
        return tagName;
    }

    @Override
    public String getExportFileName() {
        return getName();
    }

    /**
     * Returns identifier of tag type
     *
     * @return Identifier of tag type
     */
    public int getId() {
        return id;
    }

    @Override
    public SWF getSwf() {
        return swf;
    }

    public void setSwf(SWF swf) {
        this.swf = swf;
    }

    public Timelined getTimelined() {
        return timelined;
    }
    
    public void setTimelined(Timelined timelined) {
        this.timelined = timelined;
    }
    
    /**
     * Constructor
     *
     * @param swf
     * @param id Tag type identifier
     * @param name Tag name
     * @param data Bytes of data
     * @param pos
     */
    public Tag(SWF swf, int id, String name, byte[] data, long pos) {
        this.id = id;
        this.tagName = name;
        this.data = data;
        this.pos = pos;
        this.swf = swf;
    }

    private static final Object lockObject = new Object();
    private static List<Integer> knownTagIds;
    private static List<Integer> requiredTagIds;

    public static List<Integer> getKnownTags() {
        if (knownTagIds == null) {
            synchronized (lockObject) {
                if (knownTagIds == null) {
                    List<Integer> tagIds = Arrays.asList(
                            CSMTextSettingsTag.ID,
                            DebugIDTag.ID,
                            DefineBinaryDataTag.ID,
                            DefineBitsJPEG2Tag.ID,
                            DefineBitsJPEG3Tag.ID,
                            DefineBitsJPEG4Tag.ID,
                            DefineBitsLossless2Tag.ID,
                            DefineBitsLosslessTag.ID,
                            DefineBitsTag.ID,
                            DefineButton2Tag.ID,
                            DefineButtonCxformTag.ID,
                            DefineButtonSoundTag.ID,
                            DefineButtonTag.ID,
                            DefineEditTextTag.ID,
                            DefineFont2Tag.ID,
                            DefineFont3Tag.ID,
                            DefineFont4Tag.ID,
                            DefineFontAlignZonesTag.ID,
                            DefineFontInfo2Tag.ID,
                            DefineFontInfoTag.ID,
                            DefineFontNameTag.ID,
                            DefineFontTag.ID,
                            DefineMorphShape2Tag.ID,
                            DefineMorphShapeTag.ID,
                            DefineScalingGridTag.ID,
                            DefineSceneAndFrameLabelDataTag.ID,
                            DefineShape2Tag.ID,
                            DefineShape3Tag.ID,
                            DefineShape4Tag.ID,
                            DefineShapeTag.ID,
                            DefineSoundTag.ID,
                            DefineSpriteTag.ID,
                            DefineText2Tag.ID,
                            DefineTextTag.ID,
                            DefineVideoStreamTag.ID,
                            DoABCDefineTag.ID,
                            DoABCTag.ID,
                            DoActionTag.ID,
                            DoInitActionTag.ID,
                            EnableDebugger2Tag.ID,
                            EnableDebuggerTag.ID,
                            EnableTelemetryTag.ID,
                            EndTag.ID,
                            ExportAssetsTag.ID,
                            FileAttributesTag.ID,
                            FrameLabelTag.ID,
                            ImportAssets2Tag.ID,
                            ImportAssetsTag.ID,
                            JPEGTablesTag.ID,
                            MetadataTag.ID,
                            PlaceObject2Tag.ID,
                            PlaceObject3Tag.ID,
                            PlaceObject4Tag.ID,
                            PlaceObjectTag.ID,
                            ProductInfoTag.ID,
                            ProtectTag.ID,
                            RemoveObject2Tag.ID,
                            RemoveObjectTag.ID,
                            ScriptLimitsTag.ID,
                            SetBackgroundColorTag.ID,
                            SetTabIndexTag.ID,
                            ShowFrameTag.ID,
                            SoundStreamBlockTag.ID,
                            SoundStreamHead2Tag.ID,
                            SoundStreamHeadTag.ID,
                            StartSound2Tag.ID,
                            StartSoundTag.ID,
                            SymbolClassTag.ID,
                            TagStub.ID,
                            VideoFrameTag.ID,
                            DefineCompactedFont.ID,
                            DefineExternalGradient.ID,
                            DefineExternalImage.ID,
                            DefineExternalImage2.ID,
                            DefineExternalSound.ID,
                            DefineExternalStreamSound.ID,
                            DefineGradientMap.ID,
                            DefineSubImage.ID,
                            ExporterInfoTag.ID,
                            FontTextureInfo.ID);
                    knownTagIds = tagIds;
                }
            }
        }
        return knownTagIds;
    }

    public static List<Integer> getRequiredTags() {
        if (requiredTagIds == null) {
            synchronized (lockObject) {
                if (requiredTagIds == null) {
                    List<Integer> tagIds = Arrays.asList(
                            DefineBinaryDataTag.ID,
                            DefineBitsJPEG2Tag.ID,
                            DefineBitsJPEG3Tag.ID,
                            DefineBitsJPEG4Tag.ID,
                            DefineBitsLossless2Tag.ID,
                            DefineBitsLosslessTag.ID,
                            DefineBitsTag.ID,
                            DefineButton2Tag.ID,
                            DefineButtonCxformTag.ID,
                            DefineButtonSoundTag.ID,
                            DefineButtonTag.ID,
                            DefineEditTextTag.ID,
                            DefineFont2Tag.ID,
                            DefineFont3Tag.ID,
                            DefineFont4Tag.ID,
                            DefineFontAlignZonesTag.ID,
                            DefineFontInfo2Tag.ID,
                            DefineFontInfoTag.ID,
                            DefineFontNameTag.ID,
                            DefineFontTag.ID,
                            DefineMorphShape2Tag.ID,
                            DefineMorphShapeTag.ID,
                            DefineScalingGridTag.ID,
                            DefineSceneAndFrameLabelDataTag.ID,
                            DefineShape2Tag.ID,
                            DefineShape3Tag.ID,
                            DefineShape4Tag.ID,
                            DefineShapeTag.ID,
                            DefineSoundTag.ID,
                            DefineSpriteTag.ID,
                            DefineText2Tag.ID,
                            DefineTextTag.ID,
                            DefineVideoStreamTag.ID,
                            DoABCDefineTag.ID,
                            DoABCTag.ID,
                            DoActionTag.ID,
                            DoInitActionTag.ID,
                            ShowFrameTag.ID);
                    requiredTagIds = tagIds;
                }
            }
        }
        return requiredTagIds;
    }

    /**
     * Gets data bytes
     *
     * @return Bytes of data
     */
    public byte[] getData() {
        return data;
    }

    public int getVersion() {
        if (swf == null) {
            return SWF.DEFAULT_VERSION;
        }
        return swf.version;
    }

    /**
     * Gets original read data
     *
     * @return Bytes of data
     */
    public byte[] getOriginalData() {
        return data;
    }

    public byte[] getDataOrOriginalData() {
        if (modified) {
            return getData();
        }
        return data;
    }

    /**
     * Returns string representation of the object
     *
     * @return String representation of the object
     */
    @Override
    public String toString() {
        return getName();
    }

    public final long getOrigDataLength() {
        return data.length;
    }

    public boolean hasSubTags() {
        return false;
    }

    public List<Tag> getSubTags() {
        return null;
    }

    public long getPos() {
        return pos;
    }

    public void setModified(boolean value) {
        modified = value;
    }

    public boolean isModified() {
        return modified;
    }

    @Override
    public Set<Integer> getNeededCharacters() {
        return new HashSet<>();
    }

    public Set<Integer> getDeepNeededCharacters(Map<Integer, CharacterTag> characters) {
        Set<Integer> ret = new HashSet<>();
        Set<Integer> needed = getNeededCharacters();
        for (int ch : needed) {
            if (!characters.containsKey(ch)) { //TODO: use Import tag (?)
                continue;
            }
            ret.add(ch);
            ret.addAll(characters.get(ch).getDeepNeededCharacters(characters));
        }
        return ret;
    }
}
