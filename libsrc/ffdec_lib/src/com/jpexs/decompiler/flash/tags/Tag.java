/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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

import com.jpexs.decompiler.flash.ReadOnlyTagList;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.abc.CopyOutputStream;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.tags.base.BoundedTag;
import com.jpexs.decompiler.flash.tags.base.CharacterIdTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.Exportable;
import com.jpexs.decompiler.flash.tags.base.ImportTag;
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
import com.jpexs.decompiler.flash.treeitems.Openable;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Tag inside SWF file
 *
 * @author JPEXS
 */
public abstract class Tag implements NeedsCharacters, Exportable, Serializable {

    private static final Logger logger = Logger.getLogger(Tag.class.getName());

    /**
     * Identifier of tag type
     */
    protected int id;

    /**
     * If true, then Tag is written to the stream as longer than 0x3f even if it
     * is not
     */
    public boolean forceWriteAsLong = false;

    /**
     * Tag name
     */
    protected String tagName;

    /**
     * SWF
     */
    @Internal
    protected transient SWF swf;

    /**
     * Timelined
     */
    @Internal
    protected transient Timelined timelined;

    @Internal
    private boolean modified;

    /**
     * Imported flag
     */
    @Internal
    protected boolean imported = false;

    /**
     * Imported deep flag
     */
    @Internal
    protected boolean importedDeep = false;

    /**
     * Sets imported flag
     * @param imported True if imported
     * @param deep True if imported deep
     */
    public void setImported(boolean imported, boolean deep) {
        this.imported = imported;
        this.importedDeep = deep;
    }

    /**
     * Returns true if tag is imported
     * @return True if imported
     */
    public boolean isImported() {
        return imported;
    }

    /**
     * Returns true if tag is imported deep
     * @return True if imported deep
     */
    public boolean isImportedDeep() {
        return importedDeep;
    }

    /**
     * Original tag data
     */
    @Internal
    private ByteArrayRange originalRange;

    private final HashSet<TagChangedListener> listeners = new HashSet<>();

    @Internal
    public ByteArrayRange remainingData;

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

    /**
     * Returns tag name.
     * @return Tag name
     */
    public String getTagName() {
        return tagName;
    }

    /**
     * Gets name properties.
     * @return Name properties
     */
    public Map<String, String> getNameProperties() {
        return new LinkedHashMap<>();
    }

    /**
     * Gets name
     * @return Name
     */
    public String getName() {
        StringBuilder sb = new StringBuilder();
        sb.append(tagName);

        Map<String, String> props = getNameProperties();
        if (!props.isEmpty()) {
            sb.append(" (");
            List<String> parts = new ArrayList<>();
            for (String key : props.keySet()) {
                parts.add(key + ": " + props.get(key));
            }
            sb.append(String.join(", ", parts));
            sb.append(")");
        }
        return sb.toString();
    }

    @Override
    public String getExportFileName() {
        return tagName;
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
    public Openable getOpenable() {
        return swf;
    }

    /**
     * Gets SWF
     * @return SWF
     */
    public SWF getSwf() {
        return swf;
    }

    /**
     * Sets SWF
     * @param swf SWF
     */
    public void setSwf(SWF swf) {
        setSwf(swf, false);
    }

    /**
     * Sets SWF
     * @param swf SWF
     * @param deep True if deep
     */
    public void setSwf(SWF swf, boolean deep) {
        this.swf = swf;
        if (deep) {
            if (this instanceof DefineSpriteTag) {
                DefineSpriteTag sprite = (DefineSpriteTag) this;
                for (Tag subTag : sprite.getTags()) {
                    subTag.setSwf(swf);
                }
            }
        }
    }

    /**
     * Gets timelined.
     * @return Timelined
     */
    public Timelined getTimelined() {
        return timelined;
    }

    /**
     * Sets timelined.
     * @param timelined Timelined
     */
    public void setTimelined(Timelined timelined) {
        this.timelined = timelined;
    }

    /**
     * Reads tag data from the stream-
     * @param sis SWF input stream
     * @param data Data
     * @param level Level
     * @param parallel Parallel
     * @param skipUnusualTags Skip unusual tags
     * @param lazy Lazy
     * @throws IOException On I/O error
     * @throws InterruptedException On interrupt
     */
    public abstract void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException, InterruptedException;

    private static final Object lockObject = new Object();

    private static volatile Integer[] knownTagIds;

    private static volatile Map<Integer, TagTypeInfo> knownTagInfosById;

    private static volatile Map<String, TagTypeInfo> knownTagInfosByName;

    private static volatile List<Integer> requiredTagIds;

    /**
     * Gets known tag identifiers
     * @return Known tag identifiers
     */
    public static Integer[] getKnownTags() {
        if (knownTagIds == null) {
            synchronized (lockObject) {
                if (knownTagIds == null) {
                    Set<Integer> keySet = getKnownClasses().keySet();
                    Integer[] tagIds = keySet.toArray(new Integer[keySet.size()]);
                    knownTagIds = tagIds;
                }
            }
        }
        return knownTagIds;
    }

    /**
     * Gets known classes.
     * @return Known classes
     */
    public static Map<Integer, TagTypeInfo> getKnownClasses() {
        if (knownTagInfosById == null) {
            synchronized (lockObject) {
                if (knownTagInfosById == null) {
                    Map<Integer, TagTypeInfo> map = new HashMap<>();
                    Map<String, TagTypeInfo> map2 = new HashMap<>();
                    addTagInfo(map, map2, CSMTextSettingsTag.ID, CSMTextSettingsTag.class, CSMTextSettingsTag.NAME);
                    addTagInfo(map, map2, DebugIDTag.ID, DebugIDTag.class, DebugIDTag.NAME);
                    addTagInfo(map, map2, DefineBinaryDataTag.ID, DefineBinaryDataTag.class, DefineBinaryDataTag.NAME);
                    addTagInfo(map, map2, DefineBitsJPEG2Tag.ID, DefineBitsJPEG2Tag.class, DefineBitsJPEG2Tag.NAME);
                    addTagInfo(map, map2, DefineBitsJPEG3Tag.ID, DefineBitsJPEG3Tag.class, DefineBitsJPEG3Tag.NAME);
                    addTagInfo(map, map2, DefineBitsJPEG4Tag.ID, DefineBitsJPEG4Tag.class, DefineBitsJPEG4Tag.NAME);
                    addTagInfo(map, map2, DefineBitsLossless2Tag.ID, DefineBitsLossless2Tag.class, DefineBitsLossless2Tag.NAME);
                    addTagInfo(map, map2, DefineBitsLosslessTag.ID, DefineBitsLosslessTag.class, DefineBitsLosslessTag.NAME);
                    addTagInfo(map, map2, DefineBitsTag.ID, DefineBitsTag.class, DefineBitsTag.NAME);
                    addTagInfo(map, map2, DefineButton2Tag.ID, DefineButton2Tag.class, DefineButton2Tag.NAME);
                    addTagInfo(map, map2, DefineButtonCxformTag.ID, DefineButtonCxformTag.class, DefineButtonCxformTag.NAME);
                    addTagInfo(map, map2, DefineButtonSoundTag.ID, DefineButtonSoundTag.class, DefineButtonSoundTag.NAME);
                    addTagInfo(map, map2, DefineButtonTag.ID, DefineButtonTag.class, DefineButtonTag.NAME);
                    addTagInfo(map, map2, DefineEditTextTag.ID, DefineEditTextTag.class, DefineEditTextTag.NAME);
                    addTagInfo(map, map2, DefineFont2Tag.ID, DefineFont2Tag.class, DefineFont2Tag.NAME);
                    addTagInfo(map, map2, DefineFont3Tag.ID, DefineFont3Tag.class, DefineFont3Tag.NAME);
                    addTagInfo(map, map2, DefineFont4Tag.ID, DefineFont4Tag.class, DefineFont4Tag.NAME);
                    addTagInfo(map, map2, DefineFontAlignZonesTag.ID, DefineFontAlignZonesTag.class, DefineFontAlignZonesTag.NAME);
                    addTagInfo(map, map2, DefineFontInfo2Tag.ID, DefineFontInfo2Tag.class, DefineFontInfo2Tag.NAME);
                    addTagInfo(map, map2, DefineFontInfoTag.ID, DefineFontInfoTag.class, DefineFontInfoTag.NAME);
                    addTagInfo(map, map2, DefineFontNameTag.ID, DefineFontNameTag.class, DefineFontNameTag.NAME);
                    addTagInfo(map, map2, DefineFontTag.ID, DefineFontTag.class, DefineFontTag.NAME);
                    addTagInfo(map, map2, DefineMorphShape2Tag.ID, DefineMorphShape2Tag.class, DefineMorphShape2Tag.NAME);
                    addTagInfo(map, map2, DefineMorphShapeTag.ID, DefineMorphShapeTag.class, DefineMorphShapeTag.NAME);
                    addTagInfo(map, map2, DefineScalingGridTag.ID, DefineScalingGridTag.class, DefineScalingGridTag.NAME);
                    addTagInfo(map, map2, DefineSceneAndFrameLabelDataTag.ID, DefineSceneAndFrameLabelDataTag.class, DefineSceneAndFrameLabelDataTag.NAME);
                    addTagInfo(map, map2, DefineShape2Tag.ID, DefineShape2Tag.class, DefineShape2Tag.NAME);
                    addTagInfo(map, map2, DefineShape3Tag.ID, DefineShape3Tag.class, DefineShape3Tag.NAME);
                    addTagInfo(map, map2, DefineShape4Tag.ID, DefineShape4Tag.class, DefineShape4Tag.NAME);
                    addTagInfo(map, map2, DefineShapeTag.ID, DefineShapeTag.class, DefineShapeTag.NAME);
                    addTagInfo(map, map2, DefineSoundTag.ID, DefineSoundTag.class, DefineSoundTag.NAME);
                    addTagInfo(map, map2, DefineSpriteTag.ID, DefineSpriteTag.class, DefineSpriteTag.NAME);
                    addTagInfo(map, map2, DefineText2Tag.ID, DefineText2Tag.class, DefineText2Tag.NAME);
                    addTagInfo(map, map2, DefineTextTag.ID, DefineTextTag.class, DefineTextTag.NAME);
                    addTagInfo(map, map2, DefineVideoStreamTag.ID, DefineVideoStreamTag.class, DefineVideoStreamTag.NAME);
                    addTagInfo(map, map2, DoABC2Tag.ID, DoABC2Tag.class, DoABC2Tag.NAME);
                    addTagInfo(map, map2, DoABCTag.ID, DoABCTag.class, DoABCTag.NAME);
                    addTagInfo(map, map2, DoActionTag.ID, DoActionTag.class, DoActionTag.NAME);
                    addTagInfo(map, map2, DoInitActionTag.ID, DoInitActionTag.class, DoInitActionTag.NAME);
                    addTagInfo(map, map2, EnableDebugger2Tag.ID, EnableDebugger2Tag.class, EnableDebugger2Tag.NAME);
                    addTagInfo(map, map2, EnableDebuggerTag.ID, EnableDebuggerTag.class, EnableDebuggerTag.NAME);
                    addTagInfo(map, map2, EnableTelemetryTag.ID, EnableTelemetryTag.class, EnableTelemetryTag.NAME);
                    addTagInfo(map, map2, EndTag.ID, EndTag.class, EndTag.NAME);
                    addTagInfo(map, map2, ExportAssetsTag.ID, ExportAssetsTag.class, ExportAssetsTag.NAME);
                    addTagInfo(map, map2, FileAttributesTag.ID, FileAttributesTag.class, FileAttributesTag.NAME);
                    addTagInfo(map, map2, FrameLabelTag.ID, FrameLabelTag.class, FrameLabelTag.NAME);
                    addTagInfo(map, map2, ImportAssets2Tag.ID, ImportAssets2Tag.class, ImportAssets2Tag.NAME);
                    addTagInfo(map, map2, ImportAssetsTag.ID, ImportAssetsTag.class, ImportAssetsTag.NAME);
                    addTagInfo(map, map2, JPEGTablesTag.ID, JPEGTablesTag.class, JPEGTablesTag.NAME);
                    addTagInfo(map, map2, MetadataTag.ID, MetadataTag.class, MetadataTag.NAME);
                    addTagInfo(map, map2, PlaceObject2Tag.ID, PlaceObject2Tag.class, PlaceObject2Tag.NAME);
                    addTagInfo(map, map2, PlaceObject3Tag.ID, PlaceObject3Tag.class, PlaceObject3Tag.NAME);
                    addTagInfo(map, map2, PlaceObject4Tag.ID, PlaceObject4Tag.class, PlaceObject4Tag.NAME);
                    addTagInfo(map, map2, PlaceObjectTag.ID, PlaceObjectTag.class, PlaceObjectTag.NAME);
                    addTagInfo(map, map2, ProductInfoTag.ID, ProductInfoTag.class, ProductInfoTag.NAME);
                    addTagInfo(map, map2, ProtectTag.ID, ProtectTag.class, ProtectTag.NAME);
                    addTagInfo(map, map2, RemoveObject2Tag.ID, RemoveObject2Tag.class, RemoveObject2Tag.NAME);
                    addTagInfo(map, map2, RemoveObjectTag.ID, RemoveObjectTag.class, RemoveObjectTag.NAME);
                    addTagInfo(map, map2, ScriptLimitsTag.ID, ScriptLimitsTag.class, ScriptLimitsTag.NAME);
                    addTagInfo(map, map2, SetBackgroundColorTag.ID, SetBackgroundColorTag.class, SetBackgroundColorTag.NAME);
                    addTagInfo(map, map2, SetTabIndexTag.ID, SetTabIndexTag.class, SetTabIndexTag.NAME);
                    addTagInfo(map, map2, ShowFrameTag.ID, ShowFrameTag.class, ShowFrameTag.NAME);
                    addTagInfo(map, map2, SoundStreamBlockTag.ID, SoundStreamBlockTag.class, SoundStreamBlockTag.NAME);
                    addTagInfo(map, map2, SoundStreamHead2Tag.ID, SoundStreamHead2Tag.class, SoundStreamHead2Tag.NAME);
                    addTagInfo(map, map2, SoundStreamHeadTag.ID, SoundStreamHeadTag.class, SoundStreamHeadTag.NAME);
                    addTagInfo(map, map2, StartSound2Tag.ID, StartSound2Tag.class, StartSound2Tag.NAME);
                    addTagInfo(map, map2, StartSoundTag.ID, StartSoundTag.class, StartSoundTag.NAME);
                    addTagInfo(map, map2, SymbolClassTag.ID, SymbolClassTag.class, SymbolClassTag.NAME);
                    addTagInfo(map, map2, VideoFrameTag.ID, VideoFrameTag.class, VideoFrameTag.NAME);
                    addTagInfo(map, map2, DefineCompactedFont.ID, DefineCompactedFont.class, DefineCompactedFont.NAME);
                    addTagInfo(map, map2, DefineExternalGradient.ID, DefineExternalGradient.class, DefineExternalGradient.NAME);
                    addTagInfo(map, map2, DefineExternalImage.ID, DefineExternalImage.class, DefineExternalImage.NAME);
                    addTagInfo(map, map2, DefineExternalImage2.ID, DefineExternalImage2.class, DefineExternalImage2.NAME);
                    addTagInfo(map, map2, DefineExternalSound.ID, DefineExternalSound.class, DefineExternalSound.NAME);
                    addTagInfo(map, map2, DefineExternalStreamSound.ID, DefineExternalStreamSound.class, DefineExternalStreamSound.NAME);
                    addTagInfo(map, map2, DefineGradientMap.ID, DefineGradientMap.class, DefineGradientMap.NAME);
                    addTagInfo(map, map2, DefineSubImage.ID, DefineSubImage.class, DefineSubImage.NAME);
                    addTagInfo(map, map2, ExporterInfo.ID, ExporterInfo.class, ExporterInfo.NAME);
                    addTagInfo(map, map2, FontTextureInfo.ID, FontTextureInfo.class, FontTextureInfo.NAME);
                    knownTagInfosById = map;
                    knownTagInfosByName = map2;
                }
            }
        }
        return knownTagInfosById;
    }

    /**
     * Gets known classes by name.
     * @return Known classes by name
     */
    public static Map<String, TagTypeInfo> getKnownClassesByName() {
        // map is filled together with knownTagInfosById
        if (knownTagInfosByName == null) {
            getKnownClasses();
        }

        return knownTagInfosByName;
    }

    private static void addTagInfo(Map<Integer, TagTypeInfo> map, Map<String, TagTypeInfo> map2, int id, Class cls, String name) {
        map.put(id, new TagTypeInfo(id, cls, name));
        map2.put(name, new TagTypeInfo(id, cls, name));
    }

    /**
     * Gets required tag identifiers
     * @return Required tag identifiers
     */
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
                            DoABC2Tag.ID,
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
     * Gets SWF version.
     * @return SWF version
     */
    public int getVersion() {
        if (swf == null) {
            return SWF.DEFAULT_VERSION;
        }
        return swf.version;
    }

    /**
     * Gets header.
     * @param dataLength Data length
     * @return Header
     */
    protected byte[] getHeader(int dataLength) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {

            SWFOutputStream sos = new SWFOutputStream(baos, swf.version, swf.getCharset());
            int tagLength = dataLength;
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

    /**
     * Gets tag header.
     * @param tagIDTagLength Tag ID tag length
     * @param tagLength Tag length
     * @param writeLong Write long
     * @param version Version
     * @return Tag header
     */
    public static byte[] getTagHeader(int tagIDTagLength, long tagLength, boolean writeLong, int version) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {

            SWFOutputStream sos = new SWFOutputStream(baos, version, Utf8Helper.charsetName);
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
     * @throws IOException On I/O error
     */
    public void writeTag(SWFOutputStream sos) throws IOException {
        if (Configuration._debugCopy.get() || isModified() || isImported()) {
            byte[] newData = getData();
            byte[] newHeaderData = getHeader(newData.length);
            sos.write(newHeaderData);
            sos.write(newData);
        } else {
            sos.write(originalRange.getArray(), originalRange.getPos(), originalRange.getLength());
        }
    }
    
    /**
     * Writes Tag value to the stream, ignoring all scripts
     * @param sos Output stream
     * @throws IOException On I/O error
     */
    public void writeTagNoScripts(SWFOutputStream sos) throws IOException {
        byte[] newData = getDataNoScript();
        byte[] newHeaderData = getHeader(newData.length);
        sos.write(newHeaderData);
        sos.write(newData);
    }        

    /**
     * Clones the tag.
     * @return Cloned tag
     * @throws InterruptedException On interrupt
     * @throws IOException On I/O error
     */
    public Tag cloneTag() throws InterruptedException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] data = getData();
        byte[] headerData = getHeader(data.length);
        baos.write(headerData);
        baos.write(data);
        
        byte[] dataWithHeader = baos.toByteArray();
        SWFInputStream tagDataStream = new SWFInputStream(swf, data, 0, data.length);
        TagStub copy = new TagStub(swf, getId(), "Unresolved", new ByteArrayRange(dataWithHeader), tagDataStream);
        copy.forceWriteAsLong = forceWriteAsLong;
        return SWFInputStream.resolveTag(copy, 0, false, true, false, false);
    }

    /**
     * Gets original tag.
     * @return Original tag
     * @throws InterruptedException On interrupt
     * @throws IOException On I/O error
     */
    public Tag getOriginalTag() throws InterruptedException, IOException {
        byte[] data = getOriginalData();
        SWFInputStream tagDataStream = new SWFInputStream(swf, data, getDataPos(), data.length);
        TagStub copy = new TagStub(swf, getId(), "Unresolved", getOriginalRange(), tagDataStream);
        copy.forceWriteAsLong = forceWriteAsLong;
        return SWFInputStream.resolveTag(copy, 0, false, true, false, false);
    }

    /**
     * Checks if the tag changes can be undone.
     * @return True if can be undone
     */
    public boolean canUndo() {
        return originalRange != null && isModified();
    }

    /**
     * Undoes the tag changes.
     * @throws InterruptedException On interrupt
     * @throws IOException On I/O error
     */
    public void undo() throws InterruptedException, IOException {
        if (originalRange == null) { //If the tag is newly created in GUI it has no original data
            return;
        }
        SWFInputStream tagDataStream = new SWFInputStream(swf, originalRange.getArray(), 0, (int) originalRange.getPos() + originalRange.getLength());
        tagDataStream.seek(getDataPos());
        readData(tagDataStream, getOriginalRange(), 0, false, true, false);
        setModified(false);
    }

    @Override
    public String toString() {
        return getName();
    }

    /**
     * Gets data bytes
     *
     * @param sos SWF output stream
     * @throws IOException On I/O error
     */
    public abstract void getData(SWFOutputStream sos) throws IOException;
      
    /**
     * Gets data bytes
     *
     * @return Bytes of data
     */
    public byte[] getData() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream os = baos;
        if (Configuration._debugCopy.get()) {
            byte[] originalData = getOriginalData();
            if (originalData != null) {
                os = new CopyOutputStream(os, new ByteArrayInputStream(getOriginalData()));
            }
        }

        try (SWFOutputStream sos = new SWFOutputStream(os, getVersion(), getCharset())) {
            getData(sos);
            if (remainingData != null) {
                sos.write(remainingData);
            }
        } catch (IOException e) {
            throw new Error("This should never happen.", e);
        }

        return baos.toByteArray();
    }
    
    /**
     * Gets data bytes ignoring all scripts
     * @param sos SWF output stream
     * @throws IOException On I/O error
     */
    public void getDataNoScript(SWFOutputStream sos) throws IOException {
        getData(sos);
    }
    
    public byte[] getDataNoScript() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream os = baos;
        try (SWFOutputStream sos = new SWFOutputStream(os, getVersion(), getCharset())) {
            getDataNoScript(sos);
            if (remainingData != null) {
                sos.write(remainingData);
            }
        } catch (IOException e) {
            throw new Error("This should never happen.", e);
        }

        return baos.toByteArray();
    }

    /**
     * Gets original byte range.
     * @return Original byte range
     */
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

    /**
     * Gets original data length.
     * @return Original data length
     */
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

    /**
     * Gets position in original Range.
     * @return Position
     */
    public long getPos() {
        if (originalRange == null) {
            return -1;
        }

        return originalRange.getPos();
    }

    /**
     * Gets data position.
     * @return Data position
     */
    public long getDataPos() {
        if (originalRange == null) {
            return -1;
        }

        return originalRange.getPos() + (isLongOriginal() ? 6 : 2);
    }

    /**
     * Sets modified flag
     * @param value True if modified
     */
    public void setModified(boolean value) {
        boolean oldValue = modified;
        modified = value;
        if (value && oldValue != value) {
            informListeners();
        }
    }

    /**
     * Adds event listener.
     * @param listener Listener
     */
    public final void addEventListener(TagChangedListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes event listener.
     * @param listener Listener
     */
    public final void removeEventListener(TagChangedListener listener) {
        listeners.remove(listener);
    }

    /**
     * Informs listeners.
     */
    protected void informListeners() {
        for (TagChangedListener listener : listeners) {
            listener.handleEvent(this);
        }
    }

    /**
     * Creates original data.
     */
    public void createOriginalData() {
        byte[] data = getData();
        byte[] headerData = getHeader(data.length);
        byte[] tagData = new byte[data.length + headerData.length];
        System.arraycopy(headerData, 0, tagData, 0, headerData.length);
        System.arraycopy(data, 0, tagData, headerData.length, data.length);
        originalRange = new ByteArrayRange(tagData);
    }

    @Override
    public boolean isModified() {
        return modified;
    }

    /**
     * Checks if the tag is read only.
     * @return True if read only
     */
    public boolean isReadOnly() {
        return isImported();
    }

    @Override
    public void getNeededCharacters(Set<Integer> needed, SWF swf) {
    }

    /**
     * Gets missing needed characters.
     * @param needed Needed
     * @return Missing needed characters
     */
    public Set<Integer> getMissingNeededCharacters(Set<Integer> needed) {
        Set<Integer> needed2 = new LinkedHashSet<>(needed);
        if (needed2.isEmpty()) {
            return new LinkedHashSet<>();
        }
        Timelined tim = getTimelined();
        if (tim == null) {
            return needed2;
        }
        ReadOnlyTagList tags = tim.getTags();
        for (int i = tags.indexOf(this) - 1; i >= 0; i--) {
            if (tags.get(i) instanceof ImportTag) {
                ImportTag it = (ImportTag) tags.get(i);
                needed2.removeAll(it.getAssets().keySet());
                if (needed2.isEmpty()) {
                    return needed2;
                }
            }
            if (tags.get(i) instanceof CharacterTag) {
                int charId = ((CharacterTag) tags.get(i)).getCharacterId();
                needed2.remove(charId);
                if (needed2.isEmpty()) {
                    return needed2;
                }
            }
        }
        return needed2;
    }

    @Override
    public boolean replaceCharacter(int oldCharacterId, int newCharacterId) {
        return false;
    }

    @Override
    public boolean removeCharacter(int characterId) {
        return false;
    }

    /**
     * Gets needed characters deep.
     * @param needed Needed
     */
    public void getNeededCharactersDeep(Set<Integer> needed) {
        Set<Integer> needed2 = new LinkedHashSet<>();
        getNeededCharacters(needed2, swf);
        List<Integer> needed3 = new ArrayList<>(needed2);

        for (int i = 0; i < needed3.size(); i++) {
            int characterId = needed3.get(i);
            if (swf == null) {
                return;
            }
            if (swf.getCharacters(true).containsKey(characterId) && !swf.getCyclicCharacters().contains(characterId)) {
                Set<Integer> needed4 = new LinkedHashSet<>();
                CharacterTag character = swf.getCharacter(characterId);
                if (character.isImported()) {
                    continue;
                }
                character.getNeededCharacters(needed4, swf);
                List<Integer> newItems = new ArrayList<>();
                for (int n : needed4) {
                    int index = needed3.indexOf((Integer) n);
                    if (index > i) {
                        needed3.remove(index);
                    }
                    if (!needed3.contains(n) && !newItems.contains(n)) {
                        newItems.add(n);
                    }
                }
                if (!newItems.isEmpty()) {
                    needed3.addAll(i, newItems);
                    i--;
                }
            }
        }

        for (Integer characterId : needed3) {
            if (swf == null) {
                return;
            }
            if (swf.getCharacters(true).containsKey(characterId)) {
                needed.add(characterId);
            }
        }
    }

    private void getDependentCharactersOnTimelined(Timelined timelined, Set<Integer> dependent) {
        for (Tag tag : timelined.getTags()) {
            if (tag instanceof CharacterTag) {
                if (((CharacterTag) tag).getCharacterId() != -1) {
                    Set<Integer> needed = new HashSet<>();
                    tag.getNeededCharactersDeep(needed);
                    for (int dep : dependent) {
                        if (needed.contains(dep)) {
                            dependent.add(((CharacterTag) tag).getCharacterId());
                            break;
                        }
                    }
                }
            }
            if (tag instanceof DefineSpriteTag) {
                getDependentCharactersOnTimelined((DefineSpriteTag) tag, dependent);
            }
        }
    }

    /**
     * Gets dependent characters.
     * @param dependent Result
     */
    public void getDependentCharacters(Set<Integer> dependent) {
        getDependentCharactersOnTimelined(swf, dependent);
    }

    /**
     * Gets tag info.
     * @param tagInfo Tag info
     */
    public void getTagInfo(TagInfo tagInfo) {

        tagInfo.addInfo("general", "tagType", String.format("%s (%d)", tagName, id));
        if (this instanceof CharacterIdTag) {
            CharacterIdTag characterIdTag = (CharacterIdTag) this;
            tagInfo.addInfo("general", "characterId", characterIdTag.getCharacterId());
        }

        if (originalRange != null) {
            int pos = originalRange.getPos();
            int length = originalRange.getLength();
            tagInfo.addInfo("general", "offset", String.format("%d (0x%x)", pos, pos));
            tagInfo.addInfo("general", "length", String.format("%d (0x%x)", length, length));
        }

        if (this instanceof BoundedTag) {
            BoundedTag boundedIdTag = (BoundedTag) this;
            RECT bounds = boundedIdTag.getRect();
            tagInfo.addInfo("general", "bounds",
                    String.format("(%.2f, %.2f)[%.2f x %.2f]", bounds.Xmin / SWF.unitDivisor,
                            bounds.Ymin / SWF.unitDivisor,
                            bounds.getWidth() / SWF.unitDivisor,
                            bounds.getHeight() / SWF.unitDivisor));
        }

        /*Set<Integer> needed = new LinkedHashSet<>();
        getNeededCharactersDeep(needed);

        if (needed.size() > 0) {
            tagInfo.addInfo("general", "neededCharacters", Helper.joinStrings(needed, ", "));
        }

        if (this instanceof CharacterTag) {
            int characterId = ((CharacterTag) this).getCharacterId();
            Set<Integer> dependent = swf.getDependentCharacters(characterId);
            if (dependent != null) {
                if (dependent.size() > 0) {
                    tagInfo.addInfo("general", "dependentCharacters", Helper.joinStrings(dependent, ", "));
                }
            }
            
            Set<Integer> dependent2 = swf.getDependentFrames(characterId);
            if(dependent2 != null && dependent2.size() > 0) {
                tagInfo.addInfo("general", "dependentFrames", Helper.joinStrings(dependent2, ", "));
            }
        }*/
    }

    /**
     * Gets charset.
     * @return Charset
     */
    public String getCharset() {
        if (swf == null) {
            return Utf8Helper.charsetName;
        }
        return swf.getCharset();
    }

    /**
     * Gets unique id.
     * @return Unique id
     */
    public String getUniqueId() {
        return null;
    }
}
