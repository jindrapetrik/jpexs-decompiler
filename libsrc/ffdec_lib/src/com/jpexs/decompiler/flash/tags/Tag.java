/*
 *  Copyright (C) 2010-2014 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFOutputStream;
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
import com.jpexs.decompiler.flash.tags.gfx.ExporterInfo;
import com.jpexs.decompiler.flash.tags.gfx.FontTextureInfo;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.helpers.ByteArrayRange;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents Tag inside SWF file
 */
public abstract class Tag implements NeedsCharacters, Exportable, ContainerItem, Serializable {

    /**
     * Identifier of tag type
     */
    protected int id;
    /**
     * If true, then Tag is written to the stream as longer than 0x3f even if it
     * is not
     */
    @Internal
    public boolean forceWriteAsLong = false;
    protected String tagName;
    @Internal
    protected transient SWF swf;
    @Internal
    protected transient Timelined timelined;
    @Internal
    private boolean modified;
    /**
     * Original tag data
     */
    @Internal
    private ByteArrayRange originalRange;

    private final HashSet<TagChangedListener> listeners = new HashSet<>();

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
     * @param swf The SWF
     * @param id Tag type identifier
     * @param name Tag name
     * @param data Original tag data
     */
    public Tag(SWF swf, int id, String name, ByteArrayRange data) {
        this.id = id;
        this.tagName = name;
        this.originalRange = data;
        this.swf = swf;
        if (swf == null) {
            throw new Error("swf parameter cannot be null.");
        }
        if (data == null) { // it is tag build by constructor        
            modified = true;
        }
    }

    private static final Object lockObject = new Object();
    private volatile static List<Integer> knownTagIds;
    private volatile static List<Integer> requiredTagIds;

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
                            VideoFrameTag.ID,
                            DefineCompactedFont.ID,
                            DefineExternalGradient.ID,
                            DefineExternalImage.ID,
                            DefineExternalImage2.ID,
                            DefineExternalSound.ID,
                            DefineExternalStreamSound.ID,
                            DefineGradientMap.ID,
                            DefineSubImage.ID,
                            ExporterInfo.ID,
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

    public int getVersion() {
        if (swf == null) {
            return SWF.DEFAULT_VERSION;
        }
        return swf.version;
    }

    protected byte[] getHeader(byte[] data) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {

            SWFOutputStream sos = new SWFOutputStream(baos, swf.version);
            int tagLength = data.length;
            int tagID = getId();
            int tagIDLength = (tagID << 6);
            if ((tagLength <= 62) && (!forceWriteAsLong)) {
                tagIDLength += tagLength;
                sos.writeUI16(tagIDLength);
            } else {
                tagIDLength += 0x3f;
                sos.writeUI16(tagIDLength);
                sos.writeSI32(tagLength);
            }
        } catch (IOException iex) {
            throw new Error("This should never happen.", iex);
        }
        return baos.toByteArray();
    }

    public static byte[] getTagHeader(int tagIDTagLength, long tagLength, boolean writeLong, int version) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {

            SWFOutputStream sos = new SWFOutputStream(baos, version);
            sos.writeUI16(tagIDTagLength);
            if (writeLong) {
                sos.writeSI32(tagLength);
            }
        } catch (IOException iex) {
            throw new Error("This should never happen.", iex);
        }
        return baos.toByteArray();
    }

    /**
     * Writes Tag value to the stream
     *
     * @param sos SWF output stream
     * @throws IOException
     */
    public void writeTag(SWFOutputStream sos) throws IOException {
        if (isModified()) {
            byte[] newData = getData();
            byte[] newHeaderData = getHeader(newData);
            sos.write(newHeaderData);
            sos.write(newData);
        } else {
            sos.write(originalRange.getArray(), originalRange.getPos(), originalRange.getLength());
        }
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

    /**
     * Gets data bytes
     *
     * @return Bytes of data
     */
    public abstract byte[] getData();

    public final ByteArrayRange getOriginalRange() {
        return originalRange;
    }

    /**
     * Returns the original inner data of the tag, without the 2-6 bytes length
     * header Call this method only from debug codes
     *
     * @return The data
     */
    public final byte[] getOriginalData() {
        if (originalRange == null) {
            return null;
        }

        int dataLength = getOriginalDataLength();
        int pos = (int) (originalRange.getPos() + originalRange.getLength() - dataLength);

        byte[] data = new byte[dataLength];
        System.arraycopy(originalRange.getArray(), pos, data, 0, dataLength);
        return data;
    }

    public final int getOriginalDataLength() {
        if (originalRange == null) {
            return 0;
        }

        return originalRange.getLength() - (isLongOriginal() ? 6 : 2);
    }

    private boolean isLongOriginal() {
        int shortLength = originalRange.getArray()[(int) originalRange.getPos()] & 0x003F;
        return shortLength == 0x3f;
    }

    public boolean hasSubTags() {
        return false;
    }

    public List<Tag> getSubTags() {
        return null;
    }

    public long getPos() {
        return originalRange.getPos();
    }

    public long getDataPos() {
        return originalRange.getPos() + (isLongOriginal() ? 6 : 2);
    }

    public void setModified(boolean value) {
        modified = value;
        if (value) {
            informListeners();
        }
    }

    public final void addEventListener(TagChangedListener listener) {
        listeners.add(listener);
    }

    public final void removeEventListener(TagChangedListener listener) {
        listeners.remove(listener);
    }

    protected void informListeners() {
        for (TagChangedListener listener : listeners) {
            listener.handleEvent(this);
        }
    }

    public void createOriginalData() {
        byte[] data = getData();
        byte[] headerData = getHeader(data);
        byte[] tagData = new byte[data.length + headerData.length];
        System.arraycopy(headerData, 0, tagData, 0, headerData.length);
        System.arraycopy(data, 0, tagData, headerData.length, data.length);
        originalRange = new ByteArrayRange(tagData, 0, tagData.length);
    }

    public boolean isModified() {
        return modified;
    }

    @Override
    public void getNeededCharacters(Set<Integer> needed) {
    }

    @Override
    public boolean removeCharacter(int characterId) {
        return false;
    }

    public void getNeededCharactersDeep(Set<Integer> needed) {
        Set<Integer> visited = new HashSet<>();
        Set<Integer> needed2 = new HashSet<>();
        getNeededCharacters(needed2);

        while (visited.size() != needed2.size()) {
            for (Integer characterId : needed2) {
                if (!visited.contains(characterId)) {
                    visited.add(characterId);
                    if (swf.characters.containsKey(characterId)) {
                        swf.characters.get(characterId).getNeededCharacters(needed2);
                        break;
                    }
                }
            }
        }

        for (Integer characterId : needed2) {
            if (swf.characters.containsKey(characterId)) {
                needed.add(characterId);
            }
        }
    }
}
