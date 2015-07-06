/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash;

import SevenZip.Compression.LZMA.Decoder;
import SevenZip.Compression.LZMA.Encoder;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.CachedDecompilation;
import com.jpexs.decompiler.flash.abc.ClassPath;
import com.jpexs.decompiler.flash.abc.RenameType;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.ScriptInfo;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionGraphSource;
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.action.ActionLocalData;
import com.jpexs.decompiler.flash.action.CachedScript;
import com.jpexs.decompiler.flash.action.model.ConstantPool;
import com.jpexs.decompiler.flash.action.model.DirectValueActionItem;
import com.jpexs.decompiler.flash.action.model.FunctionActionItem;
import com.jpexs.decompiler.flash.action.model.GetMemberActionItem;
import com.jpexs.decompiler.flash.action.model.GetVariableActionItem;
import com.jpexs.decompiler.flash.action.model.clauses.ClassActionItem;
import com.jpexs.decompiler.flash.action.model.clauses.InterfaceActionItem;
import com.jpexs.decompiler.flash.action.swf4.ActionEquals;
import com.jpexs.decompiler.flash.action.swf4.ActionGetVariable;
import com.jpexs.decompiler.flash.action.swf4.ActionIf;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf4.ActionSetVariable;
import com.jpexs.decompiler.flash.action.swf4.ConstantIndex;
import com.jpexs.decompiler.flash.action.swf5.ActionCallFunction;
import com.jpexs.decompiler.flash.action.swf5.ActionCallMethod;
import com.jpexs.decompiler.flash.action.swf5.ActionConstantPool;
import com.jpexs.decompiler.flash.action.swf5.ActionDefineFunction;
import com.jpexs.decompiler.flash.action.swf5.ActionDefineLocal;
import com.jpexs.decompiler.flash.action.swf5.ActionDefineLocal2;
import com.jpexs.decompiler.flash.action.swf5.ActionEquals2;
import com.jpexs.decompiler.flash.action.swf5.ActionGetMember;
import com.jpexs.decompiler.flash.action.swf5.ActionNewMethod;
import com.jpexs.decompiler.flash.action.swf5.ActionNewObject;
import com.jpexs.decompiler.flash.action.swf5.ActionSetMember;
import com.jpexs.decompiler.flash.action.swf7.ActionDefineFunction2;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.dumpview.DumpInfo;
import com.jpexs.decompiler.flash.dumpview.DumpInfoSwfNode;
import com.jpexs.decompiler.flash.ecma.Null;
import com.jpexs.decompiler.flash.exporters.commonshape.ExportRectangle;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.commonshape.SVGExporter;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.exporters.script.AS2ScriptExporter;
import com.jpexs.decompiler.flash.exporters.script.AS3ScriptExporter;
import com.jpexs.decompiler.flash.exporters.settings.ScriptExportSettings;
import com.jpexs.decompiler.flash.helpers.HighlightedText;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.helpers.SWFDecompilerPlugin;
import com.jpexs.decompiler.flash.helpers.collections.MyEntry;
import com.jpexs.decompiler.flash.helpers.hilight.Highlighting;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.flash.tags.DefineBinaryDataTag;
import com.jpexs.decompiler.flash.tags.DefineButton2Tag;
import com.jpexs.decompiler.flash.tags.DefineButtonTag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.DoInitActionTag;
import com.jpexs.decompiler.flash.tags.EndTag;
import com.jpexs.decompiler.flash.tags.ExportAssetsTag;
import com.jpexs.decompiler.flash.tags.FileAttributesTag;
import com.jpexs.decompiler.flash.tags.JPEGTablesTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.SymbolClassTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.TagStub;
import com.jpexs.decompiler.flash.tags.VideoFrameTag;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.flash.tags.base.ASMSourceContainer;
import com.jpexs.decompiler.flash.tags.base.BoundedTag;
import com.jpexs.decompiler.flash.tags.base.ButtonTag;
import com.jpexs.decompiler.flash.tags.base.CharacterIdTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.DrawableTag;
import com.jpexs.decompiler.flash.tags.base.Exportable;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.MorphShapeTag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.tags.base.RemoveTag;
import com.jpexs.decompiler.flash.tags.base.RenderContext;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.tags.base.SoundTag;
import com.jpexs.decompiler.flash.tags.base.TextTag;
import com.jpexs.decompiler.flash.tags.enums.ImageFormat;
import com.jpexs.decompiler.flash.timeline.AS2Package;
import com.jpexs.decompiler.flash.timeline.Clip;
import com.jpexs.decompiler.flash.timeline.DepthState;
import com.jpexs.decompiler.flash.timeline.Frame;
import com.jpexs.decompiler.flash.timeline.FrameScript;
import com.jpexs.decompiler.flash.timeline.SvgClip;
import com.jpexs.decompiler.flash.timeline.TagScript;
import com.jpexs.decompiler.flash.timeline.Timeline;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.treeitems.SWFList;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.decompiler.flash.types.filters.BlendComposite;
import com.jpexs.decompiler.flash.types.filters.FILTER;
import com.jpexs.decompiler.flash.xfl.FLAVersion;
import com.jpexs.decompiler.flash.xfl.XFLConverter;
import com.jpexs.decompiler.graph.Graph;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemContainer;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.helpers.Cache;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.NulStream;
import com.jpexs.helpers.ProgressListener;
import com.jpexs.helpers.SerializableImage;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * Class representing SWF file
 *
 * @author JPEXS
 */
public final class SWF implements SWFContainerItem, Timelined {

    // big object for testing cleanup
    //BigObject bigObj = new BigObject();
    /**
     * Default version of SWF file format
     */
    public static final int DEFAULT_VERSION = 10;

    /**
     * Maximum SWF file format version Needs to be fixed when SWF versions
     * reaches this value
     */
    public static final int MAX_VERSION = 30;

    /**
     * Tags inside of file
     */
    public List<Tag> tags = new ArrayList<>();

    @Internal
    public boolean hasEndTag = true;

    /**
     * ExportRectangle for the display
     */
    public RECT displayRect;

    /**
     * Movie frame rate
     */
    public int frameRate;

    /**
     * Number of frames in movie
     */
    public int frameCount;

    /**
     * Version of SWF
     */
    public int version;

    /**
     * Uncompressed size of the file
     */
    @Internal
    public long fileSize;

    /**
     * Used compression mode
     */
    public SWFCompression compression = SWFCompression.NONE;

    /**
     * Compressed size of the file (LZMA)
     */
    @Internal
    public long compressedSize;

    /**
     * LZMA Properties
     */
    public byte[] lzmaProperties;

    @Internal
    public byte[] uncompressedData;

    @Internal
    public byte[] originalUncompressedData;

    /**
     * ScaleForm GFx
     */
    public boolean gfx = false;

    @Internal
    public SWFList swfList;

    @Internal
    private String file;

    @Internal
    private String fileTitle;

    @Internal
    private Map<Integer, CharacterTag> characters;

    @Internal
    private List<ABCContainerTag> abcList;

    @Internal
    private JPEGTablesTag jtt;

    @Internal
    public Map<Integer, String> sourceFontNamesMap = new HashMap<>();

    public static final double unitDivisor = 20;

    private static final Logger logger = Logger.getLogger(SWF.class.getName());

    @Internal
    private Timeline timeline;

    @Internal
    public DumpInfoSwfNode dumpInfo;

    @Internal
    public DefineBinaryDataTag binaryData;

    @Internal
    private final HashMap<String, String> deobfuscated = new HashMap<>();

    @Internal
    private final IdentifiersDeobfuscation deobfuscation = new IdentifiersDeobfuscation();

    @Internal
    private Cache<String, SerializableImage> frameCache = Cache.getInstance(false, false, "frame");

    @Internal
    private Cache<SoundTag, byte[]> soundCache = Cache.getInstance(false, false, "sound");

    @Internal
    private final Cache<ASMSource, CachedScript> as2Cache = Cache.getInstance(true, false, "as2");

    @Internal
    private final Cache<ScriptPack, CachedDecompilation> as3Cache = Cache.getInstance(true, false, "as3");

    public static List<String> swfSignatures = Arrays.asList(
            "FWS", // Uncompressed Flash
            "CWS", // ZLib compressed Flash
            "ZWS", // LZMA compressed Flash
            "GFX", // Uncompressed ScaleForm GFx
            "CFX", // Compressed ScaleForm GFx
            "ABC" // Non-standard LZMA compressed Flash
    );

    public void updateCharacters() {
        characters = null;
    }

    public void clearTagSwfs() {
        resetTimelines(this);
        updateCharacters();

        for (Tag tag : tags) {
            if (tag instanceof DefineSpriteTag) {
                DefineSpriteTag spriteTag = (DefineSpriteTag) tag;
                for (Tag tag1 : spriteTag.subTags) {
                    tag1.setSwf(null);
                }

                spriteTag.subTags.clear();
            }

            if (tag instanceof DefineBinaryDataTag) {
                DefineBinaryDataTag binaryTag = (DefineBinaryDataTag) tag;
                if (binaryTag.innerSwf != null) {
                    binaryTag.innerSwf.clearTagSwfs();
                }
            }

            tag.setSwf(null);
        }

        tags.clear();
        if (abcList != null) {
            abcList.clear();
        }

        if (swfList != null) {
            swfList.swfs.clear();
        }

        as2Cache.clear();
        as3Cache.clear();
        frameCache.clear();
        soundCache.clear();

        timeline = null;
        clearDumpInfo(dumpInfo);
        dumpInfo = null;
        jtt = null;
        binaryData = null;
    }

    private void clearDumpInfo(DumpInfo di) {
        for (DumpInfo childInfo : di.getChildInfos()) {
            clearDumpInfo(childInfo);
        }

        di.getChildInfos().clear();
    }

    public Map<Integer, CharacterTag> getCharacters() {
        if (characters == null) {
            synchronized (this) {
                if (characters == null) {
                    Map<Integer, CharacterTag> chars = new HashMap<>();
                    parseCharacters(tags, chars);
                    characters = Collections.unmodifiableMap(chars);
                }
            }
        }

        return characters;
    }

    public CharacterTag getCharacter(int characterId) {
        return getCharacters().get(characterId);
    }

    public String getExportName(int characterId) {
        CharacterTag characterTag = getCharacters().get(characterId);
        String exportName = characterTag != null ? characterTag.getExportName() : null;
        return exportName;
    }

    public FontTag getFont(int fontId) {
        CharacterTag characterTag = getCharacters().get(fontId);
        if (characterTag instanceof FontTag) {
            return (FontTag) characterTag;
        }

        if (characterTag != null) {
            logger.log(Level.SEVERE, "CharacterTag should be a FontTag. characterId: {0}", fontId);
        }

        return null;
    }

    public ImageTag getImage(int imageId) {
        CharacterTag characterTag = getCharacters().get(imageId);
        if (characterTag instanceof ImageTag) {
            return (ImageTag) characterTag;
        }

        if (characterTag != null) {
            logger.log(Level.SEVERE, "CharacterTag should be an ImageTag. characterId: {0}", imageId);
        }

        return null;
    }

    public TextTag getText(int textId) {
        CharacterTag characterTag = getCharacters().get(textId);
        if (characterTag instanceof TextTag) {
            return (TextTag) characterTag;
        }

        if (characterTag != null) {
            logger.log(Level.SEVERE, "CharacterTag should be a TextTag. characterId: {0}", textId);
        }

        return null;
    }

    public List<ABCContainerTag> getAbcList() {
        if (abcList == null) {
            synchronized (this) {
                if (abcList == null) {
                    ArrayList<ABCContainerTag> newAbcList = new ArrayList<>();
                    getAbcTags(tags, newAbcList);
                    abcList = newAbcList;
                }
            }
        }

        return abcList;
    }

    public boolean isAS3() {
        FileAttributesTag fileAttributes = getFileAttributes();
        return (fileAttributes != null && fileAttributes.actionScript3) || (fileAttributes == null && !getAbcList().isEmpty());
    }

    public FileAttributesTag getFileAttributes() {
        for (Tag t : tags) {
            if (t instanceof FileAttributesTag) {
                return (FileAttributesTag) t;
            }
        }

        return null;
    }

    public int getNextCharacterId() {
        int max = -1;
        for (int characterId : getCharacters().keySet()) {
            if (characterId > max) {
                max = characterId;
            }
        }

        return max + 1;
    }

    public synchronized JPEGTablesTag getJtt() {
        if (jtt == null) {
            synchronized (this) {
                if (jtt == null) {
                    for (Tag t : tags) {
                        if (t instanceof JPEGTablesTag) {
                            jtt = (JPEGTablesTag) t;
                            break;
                        }
                    }
                }
            }
        }

        return jtt;
    }

    public String getDocumentClass() {
        for (Tag t : tags) {
            if (t instanceof SymbolClassTag) {
                SymbolClassTag sc = (SymbolClassTag) t;
                for (int i = 0; i < sc.tags.size(); i++) {
                    if (sc.tags.get(i) == 0) {
                        return sc.names.get(i);
                    }
                }
            }
        }

        return null;
    }

    public void fixCharactersOrder(boolean checkAll) {
        Set<Integer> addedCharacterIds = new HashSet<>();
        Set<CharacterTag> movedTags = new HashSet<>();
        for (int i = 0; i < tags.size(); i++) {
            Tag tag = tags.get(i);
            if (checkAll || tag.isModified()) {
                Set<Integer> needed = new HashSet<>();
                tag.getNeededCharacters(needed);
                if (tag instanceof CharacterTag) {
                    CharacterTag characterTag = (CharacterTag) tag;
                    needed.remove(characterTag.getCharacterId());
                }
                boolean moved = false;
                for (Integer id : needed) {
                    if (!addedCharacterIds.contains(id)) {
                        CharacterTag neededCharacter = getCharacter(id);
                        if (neededCharacter == null) {
                            continue;
                        }

                        if (movedTags.contains(neededCharacter)) {
                            logger.log(Level.SEVERE, "Fixing characters order failed, recursion detected.");
                            return;
                        }

                        // move the needed character to the current position
                        tags.remove(neededCharacter);
                        tags.add(i, neededCharacter);
                        movedTags.add(neededCharacter);
                        moved = true;
                    }
                }

                if (moved) {
                    i--;
                    continue;
                }
            }
            if (tag instanceof CharacterTag) {
                addedCharacterIds.add(((CharacterTag) tag).getCharacterId());
            }
        }
    }

    public void resetTimelines(Timelined timelined) {
        timelined.resetTimeline();
        if (timelined instanceof SWF) {
            for (Tag t : ((SWF) timelined).tags) {
                if (t instanceof Timelined) {
                    resetTimelines((Timelined) t);
                }
            }
        }
    }

    private void parseCharacters(List<Tag> list, Map<Integer, CharacterTag> characters) {
        for (Tag t : list) {
            if (t instanceof CharacterTag) {
                int characterId = ((CharacterTag) t).getCharacterId();
                if (characters.containsKey(characterId)) {
                    logger.log(Level.SEVERE, "SWF already contains characterId={0}", characterId);
                }

                if (characterId != 0) {
                    characters.put(characterId, (CharacterTag) t);
                }
            }
            if (t instanceof DefineSpriteTag) {
                parseCharacters(((DefineSpriteTag) t).getSubTags(), characters);
            }
        }
    }

    /**
     * Unresolve recursive sprites
     */
    private void checkInvalidSprites() {
        for (int i = 0; i < tags.size(); i++) {
            Tag t = tags.get(i);
            if (t instanceof DefineSpriteTag) {
                if (!isSpriteValid((DefineSpriteTag) t, new ArrayList<>())) {
                    tags.set(i, new TagStub(this, t.getId(), "InvalidSprite", t.getOriginalRange(), null));
                }
            }
        }
    }

    private boolean isSpriteValid(DefineSpriteTag sprite, List<Integer> path) {
        if (path.contains(sprite.spriteId)) {
            return false;
        }
        path.add(sprite.spriteId);
        for (Tag t : sprite.subTags) {
            if (t instanceof DefineSpriteTag) {
                if (!isSpriteValid((DefineSpriteTag) t, path)) {
                    return false;
                }
            }
        }
        path.remove((Integer) sprite.spriteId);
        return true;
    }

    @Override
    public Timeline getTimeline() {
        if (timeline == null) {
            timeline = new Timeline(this);
        }
        return timeline;
    }

    @Override
    public void resetTimeline() {
        if (timeline != null) {
            timeline.reset(this);
        }
    }

    /**
     * Gets all tags with specified id
     *
     * @param tagId Identificator of tag type
     * @return List of tags
     */
    public List<Tag> getTagData(int tagId) {
        List<Tag> ret = new ArrayList<>();
        for (Tag tag : tags) {
            if (tag.getId() == tagId) {
                ret.add(tag);
            }
        }
        return ret;
    }

    /**
     * Saves this SWF into new file
     *
     * @param os OutputStream to save SWF in
     * @throws IOException
     */
    public void saveTo(OutputStream os) throws IOException {
        byte[] uncompressedData = saveToByteArray();
        compress(new ByteArrayInputStream(uncompressedData), os, compression, lzmaProperties);
    }

    public byte[] getHeaderBytes() {
        return getHeaderBytes(compression, gfx);
    }

    private static byte[] getHeaderBytes(SWFCompression compression, boolean gfx) {
        if (compression == SWFCompression.LZMA_ABC) {
            return new byte[]{'A', 'B', 'C'};
        }

        byte[] ret = new byte[3];

        if (compression == SWFCompression.LZMA) {
            ret[0] = 'Z';
        } else if (compression == SWFCompression.ZLIB) {
            ret[0] = 'C';
        } else {
            if (gfx) {
                ret[0] = 'G';
            } else {
                ret[0] = 'F';
            }
        }

        if (gfx) {
            ret[1] = 'F';
            ret[2] = 'X';
        } else {
            ret[1] = 'W';
            ret[2] = 'S';
        }

        return ret;
    }

    private byte[] saveToByteArray() throws IOException {
        fixCharactersOrder(false);

        byte[] data;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                SWFOutputStream sos = new SWFOutputStream(baos, version)) {
            sos.write(getHeaderBytes(SWFCompression.NONE, gfx));
            sos.writeUI8(version);
            sos.writeUI32(0); // placeholder for file length
            sos.writeRECT(displayRect);
            sos.writeUI8(0);
            sos.writeUI8(frameRate);
            sos.writeUI16(frameCount);

            sos.writeTags(tags);
            if (hasEndTag) {
                sos.writeUI16(0);
            }

            data = baos.toByteArray();
        }

        // update file size
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                SWFOutputStream sos = new SWFOutputStream(baos, version)) {
            sos.writeUI32(data.length);
            byte[] lengthData = baos.toByteArray();
            System.arraycopy(lengthData, 0, data, 4, lengthData.length);
        }

        return data;
    }

    /**
     * Compress SWF file
     *
     * @param is InputStream
     * @param os OutputStream to save SWF in
     * @param compression
     * @param lzmaProperties
     * @throws IOException
     */
    private static void compress(InputStream is, OutputStream os, SWFCompression compression, byte[] lzmaProperties) throws IOException {
        byte[] hdr = new byte[8];

        is.mark(8);

        // SWFheader: signature, version and fileSize
        if (is.read(hdr) != 8) {
            throw new SwfOpenException("SWF header is too short");
        }

        boolean uncompressed = hdr[0] == 'F' || hdr[0] == 'G'; // FWS or GFX
        if (!uncompressed) {
            // fisrt decompress, then compress to the given format
            is.reset();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            decompress(is, baos, false);
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            compress(bais, os, compression, lzmaProperties);
            return;
        }

        boolean gfx = hdr[1] == 'F' && hdr[2] == 'X';
        int version = hdr[3];
        long fileSize;
        try (SWFInputStream sis = new SWFInputStream(null, Arrays.copyOfRange(hdr, 4, 8), 4, 4)) {
            fileSize = sis.readUI32("fileSize");
        }

        SWFOutputStream sos = new SWFOutputStream(os, version);
        sos.write(getHeaderBytes(compression, gfx));
        sos.writeUI8(version);
        sos.writeUI32(fileSize);

        if (compression == SWFCompression.LZMA || compression == SWFCompression.LZMA_ABC) {
            long uncompressedLength = fileSize - 8;
            Encoder enc = new Encoder();
            if (lzmaProperties == null) {
                // todo: the bytes are from a sample swf
                lzmaProperties = new byte[]{93, 0, 0, 32, 0};
            }

            int val = lzmaProperties[0] & 0xFF;
            int lc = val % 9;
            int remainder = val / 9;
            int lp = remainder % 5;
            int pb = remainder / 5;
            int dictionarySize = 0;
            for (int i = 0; i < 4; i++) {
                dictionarySize += ((int) (lzmaProperties[1 + i]) & 0xFF) << (i * 8);
            }
            if (Configuration.lzmaFastBytes.get() > 0) {
                enc.SetNumFastBytes(Configuration.lzmaFastBytes.get());
            }
            enc.SetDictionarySize(dictionarySize);
            enc.SetLcLpPb(lc, lp, pb);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            enc.SetEndMarkerMode(true);
            enc.Code(is, baos, -1, -1, null);
            byte[] data = baos.toByteArray();
            if (compression == SWFCompression.LZMA) {
                byte[] udata = new byte[4];
                udata[0] = (byte) (data.length & 0xFF);
                udata[1] = (byte) ((data.length >> 8) & 0xFF);
                udata[2] = (byte) ((data.length >> 16) & 0xFF);
                udata[3] = (byte) ((data.length >> 24) & 0xFF);
                os.write(udata);
            }
            enc.WriteCoderProperties(os);
            if (compression == SWFCompression.LZMA_ABC) {
                byte[] udata = new byte[8];
                udata[0] = (byte) (uncompressedLength & 0xFF);
                udata[1] = (byte) ((uncompressedLength >> 8) & 0xFF);
                udata[2] = (byte) ((uncompressedLength >> 16) & 0xFF);
                udata[3] = (byte) ((uncompressedLength >> 24) & 0xFF);
                udata[4] = (byte) ((uncompressedLength >> 32) & 0xFF);
                udata[5] = (byte) ((uncompressedLength >> 40) & 0xFF);
                udata[6] = (byte) ((uncompressedLength >> 48) & 0xFF);
                udata[7] = (byte) ((uncompressedLength >> 56) & 0xFF);
                os.write(udata);
            }
            os.write(data);
        } else if (compression == SWFCompression.ZLIB) {
            DeflaterOutputStream dos = new DeflaterOutputStream(os);
            try {
                Helper.copyStream(is, dos);
            } finally {
                dos.finish();
            }
        } else {
            Helper.copyStream(is, os);
        }
    }

    @Override
    public boolean isModified() {
        for (Tag tag : tags) {
            if (tag.isModified()) {
                return true;
            }
        }
        return false;
    }

    public void clearModified() {
        for (Tag tag : tags) {
            if (tag.isModified()) {
                tag.createOriginalData();
                tag.setModified(false);
            }
        }

        try {
            uncompressedData = saveToByteArray();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Cannot save SWF", ex);
        }
    }

    /**
     * Constructs an empty SWF
     */
    public SWF() {

    }

    /**
     * Construct SWF from stream
     *
     * @param is Stream to read SWF from
     * @param parallelRead Use parallel threads?
     * @throws IOException
     * @throws java.lang.InterruptedException
     */
    public SWF(InputStream is, boolean parallelRead) throws IOException, InterruptedException {
        this(is, null, null, null, parallelRead, false, true);
    }

    /**
     * Construct SWF from stream
     *
     * @param is Stream to read SWF from
     * @param parallelRead Use parallel threads?
     * @param lazy
     * @throws IOException
     * @throws java.lang.InterruptedException
     */
    public SWF(InputStream is, boolean parallelRead, boolean lazy) throws IOException, InterruptedException {
        this(is, null, null, null, parallelRead, false, lazy);
    }

    /**
     * Construct SWF from stream
     *
     * @param is Stream to read SWF from
     * @param file Path to the file
     * @param fileTitle Title of the SWF
     * @param parallelRead Use parallel threads?
     * @throws IOException
     * @throws java.lang.InterruptedException
     */
    public SWF(InputStream is, String file, String fileTitle, boolean parallelRead) throws IOException, InterruptedException {
        this(is, file, fileTitle, null, parallelRead, false, true);
    }

    /**
     * Construct SWF from stream
     *
     * @param is Stream to read SWF from
     * @param listener
     * @param parallelRead Use parallel threads?
     * @throws IOException
     * @throws java.lang.InterruptedException
     */
    public SWF(InputStream is, ProgressListener listener, boolean parallelRead) throws IOException, InterruptedException {
        this(is, null, null, listener, parallelRead, false, true);
    }

    /**
     * Construct SWF from stream
     *
     * @param is Stream to read SWF from
     * @param file Path to the file
     * @param fileTitle Title of the SWF
     * @param listener
     * @param parallelRead Use parallel threads?
     * @throws IOException
     * @throws java.lang.InterruptedException
     */
    public SWF(InputStream is, String file, String fileTitle, ProgressListener listener, boolean parallelRead) throws IOException, InterruptedException {
        this(is, file, fileTitle, listener, parallelRead, false, true);
    }

    /**
     * Faster constructor to check SWF only
     *
     * @param is
     * @throws java.io.IOException
     */
    public SWF(InputStream is) throws IOException {
        decompress(is, new NulStream(), true);
    }

    /**
     * Construct SWF from stream
     *
     * @param is Stream to read SWF from
     * @param file Path to the file
     * @param fileTitle Title of the SWF
     * @param listener
     * @param parallelRead Use parallel threads?
     * @param checkOnly Check only file validity
     * @param lazy
     * @throws IOException
     * @throws java.lang.InterruptedException
     */
    public SWF(InputStream is, String file, String fileTitle, ProgressListener listener, boolean parallelRead, boolean checkOnly, boolean lazy) throws IOException, InterruptedException {
        this.file = file;
        this.fileTitle = fileTitle;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SWFHeader header = decompress(is, baos, true);
        gfx = header.gfx;
        compression = header.compression;
        lzmaProperties = header.lzmaProperties;
        uncompressedData = baos.toByteArray();
        originalUncompressedData = uncompressedData;

        SWFInputStream sis = new SWFInputStream(this, uncompressedData);
        dumpInfo = new DumpInfoSwfNode(this, "rootswf", "", null, 0, 0);
        sis.dumpInfo = dumpInfo;
        sis.readBytesEx(3, "signature"); // skip siganture
        version = sis.readUI8("version");
        fileSize = sis.readUI32("fileSize");
        dumpInfo.lengthBytes = fileSize;
        if (listener != null) {
            sis.addPercentListener(listener);
        }
        sis.setPercentMax(fileSize);
        displayRect = sis.readRECT("displayRect");
        // FIXED8 (16 bit fixed point) frameRate
        sis.readUI8("tmpFirstByetOfFrameRate"); // tmpFirstByetOfFrameRate
        frameRate = sis.readUI8("frameRate");
        frameCount = sis.readUI16("frameCount");
        List<Tag> tags = sis.readTagList(this, 0, parallelRead, true, !checkOnly, lazy);
        if (tags.size() > 0 && tags.get(tags.size() - 1).getId() == EndTag.ID) {
            tags.remove(tags.size() - 1);
        } else {
            hasEndTag = false;
        }
        this.tags = tags;
        if (!checkOnly) {
            checkInvalidSprites();
            updateCharacters();
            assignExportNamesToSymbols();
            assignClassesToSymbols();
            SWFDecompilerPlugin.fireSwfParsed(this);
        } else {
            boolean hasNonUnknownTag = false;
            for (Tag tag : tags) {
                if (tag.getOriginalDataLength() > 0 && Tag.getRequiredTags().contains(tag.getId())) {
                    hasNonUnknownTag = true;
                }
            }
            if (!hasNonUnknownTag) {
                throw new IOException("Invalid SWF file. No known tag found.");
            }
        }

        /*preload shape tags
         for (Tag tag : tags) {
         if (tag instanceof ShapeTag) {
         ((ShapeTag) tag).getShapes();
         }
         }*/
    }

    @Override
    public SWF getSwf() {
        return this;
    }

    public SWF getRootSwf() {
        SWF result = this;
        while (result.binaryData != null) {
            result = result.binaryData.getSwf();
        }

        return result;
    }

    public String getFile() {
        return file;
    }

    /**
     * Get title of the file
     *
     * @return file title
     */
    public String getFileTitle() {
        if (fileTitle != null) {
            return fileTitle;
        }
        return file;
    }

    public String getShortFileName() {
        String title = getFileTitle();
        if (title == null) {
            return "";
        }
        return new File(title).getName();
    }

    public void setFile(String file) {
        this.file = file;
        fileTitle = null;
    }

    public Date getFileModificationDate() {
        try {
            if (swfList != null && swfList.sourceInfo != null) {
                String fileName = swfList.sourceInfo.getFile();
                if (fileName != null) {
                    long lastModified = new File(fileName).lastModified();
                    if (lastModified > 0) {
                        return new Date(lastModified);
                    }
                }
            }
        } catch (SecurityException sex) {
        }

        return new Date();
    }

    private static void getAbcTags(List<Tag> list, List<ABCContainerTag> actionScripts) {
        for (Tag t : list) {
            if (t instanceof DefineSpriteTag) {
                getAbcTags(((DefineSpriteTag) t).getSubTags(), actionScripts);
            }
            if (t instanceof ABCContainerTag) {
                actionScripts.add((ABCContainerTag) t);
            }
        }
    }

    public void assignExportNamesToSymbols() {
        HashMap<Integer, String> exportNames = new HashMap<>();
        for (Tag t : tags) {
            if (t instanceof ExportAssetsTag) {
                ExportAssetsTag eat = (ExportAssetsTag) t;
                for (int i = 0; i < eat.tags.size(); i++) {
                    Integer tagId = eat.tags.get(i);
                    String name = eat.names.get(i);
                    if ((!exportNames.containsKey(tagId)) && (!exportNames.containsValue(name))) {
                        exportNames.put(tagId, name);
                    }
                }
            }
        }
        for (Tag t : tags) {
            if (t instanceof CharacterTag) {
                CharacterTag ct = (CharacterTag) t;
                if (exportNames.containsKey(ct.getCharacterId())) {
                    ct.setExportName(exportNames.get(ct.getCharacterId()));
                }
            }
        }
    }

    public void assignClassesToSymbols() {
        HashMap<Integer, String> classes = new HashMap<>();
        for (Tag t : tags) {
            if (t instanceof SymbolClassTag) {
                SymbolClassTag sct = (SymbolClassTag) t;
                for (int i = 0; i < sct.tags.size(); i++) {
                    if ((!classes.containsKey(sct.tags.get(i))) && (!classes.containsValue(sct.names.get(i)))) {
                        classes.put(sct.tags.get(i), sct.names.get(i));
                    }
                }
            }
        }
        for (Tag t : tags) {
            if (t instanceof CharacterTag) {
                CharacterTag ct = (CharacterTag) t;
                if (classes.containsKey(ct.getCharacterId())) {
                    ct.setClassName(classes.get(ct.getCharacterId()));
                }
            }
        }
    }

    /**
     * Compress SWF file
     *
     * @param fis Input stream
     * @param fos Output stream
     * @param compression
     * @return True on success
     */
    public static boolean compress(InputStream fis, OutputStream fos, SWFCompression compression) {
        try {
            compress(fis, fos, compression, null);
        } catch (IOException ex) {
            return false;
        }
        return true;
    }

    public static boolean decompress(InputStream fis, OutputStream fos) {
        try {
            decompress(fis, fos, false);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    private static void decodeLZMAStream(InputStream is, OutputStream os, byte[] lzmaProperties, long fileSize) throws IOException {
        Decoder decoder = new Decoder();
        if (!decoder.SetDecoderProperties(lzmaProperties)) {
            throw new IOException("LZMA:Incorrect stream properties");
        }
        if (!decoder.Code(is, os, fileSize - 8)) {
            throw new IOException("LZMA:Error in data stream");
        }
    }

    private static SWFHeader decompress(InputStream is, OutputStream os, boolean allowUncompressed) throws IOException {
        byte[] hdr = new byte[8];

        // SWFheader: signature, version and fileSize
        if (is.read(hdr) != 8) {
            throw new SwfOpenException("SWF header is too short");
        }

        String signature = new String(hdr, 0, 3, Utf8Helper.charset);
        if (!swfSignatures.contains(signature)) {
            throw new SwfOpenException("Invalid SWF file, wrong signature.");
        }

        int version = hdr[3];
        long fileSize;
        try (SWFInputStream sis = new SWFInputStream(null, Arrays.copyOfRange(hdr, 4, 8), 4, 4)) {
            fileSize = sis.readUI32("fileSize");
        }

        SWFHeader header = new SWFHeader();
        header.version = version;
        header.fileSize = fileSize;
        header.gfx = hdr[1] == 'F' && hdr[2] == 'X';

        try (SWFOutputStream sos = new SWFOutputStream(os, version)) {
            sos.write(getHeaderBytes(SWFCompression.NONE, header.gfx));
            sos.writeUI8(version);
            sos.writeUI32(fileSize);

            switch (hdr[0]) {
                case 'C': { // CWS, CFX
                    Helper.copyStream(new InflaterInputStream(is), os, fileSize - 8);
                    header.compression = SWFCompression.ZLIB;
                    break;
                }
                case 'Z': { // ZWS
                    byte[] lzmaprop = new byte[9];
                    is.read(lzmaprop);
                    try (SWFInputStream sis = new SWFInputStream(null, lzmaprop)) {
                        sis.readUI32("LZMAsize"); // compressed LZMA data size = compressed SWF - 17 byte,
                        // where 17 = 8 byte header + this 4 byte + 5 bytes decoder properties

                        int propertiesSize = 5;
                        byte[] lzmaProperties = sis.readBytes(propertiesSize, "lzmaproperties");
                        if (lzmaProperties.length != propertiesSize) {
                            throw new IOException("LZMA:input .lzma file is too short");
                        }

                        decodeLZMAStream(is, os, lzmaProperties, fileSize);

                        header.compression = SWFCompression.LZMA;
                        header.lzmaProperties = lzmaProperties;
                    }
                    break;
                }
                case 'A': { // ABC
                    byte[] lzmaProperties = new byte[5];
                    is.read(lzmaProperties);
                    byte[] uncompressedLength = new byte[8];
                    is.read(uncompressedLength);

                    decodeLZMAStream(is, os, lzmaProperties, fileSize);

                    header.compression = SWFCompression.LZMA_ABC;
                    header.lzmaProperties = lzmaProperties;
                    break;
                }
                default: { // FWS, GFX
                    if (allowUncompressed) {
                        Helper.copyStream(is, os, fileSize - 8);
                    } else {
                        throw new IOException("SWF is not compressed");
                    }
                }
            }

            return header;
        }
    }

    public static boolean renameInvalidIdentifiers(RenameType renameType, InputStream fis, OutputStream fos) {
        try {
            SWF swf = new SWF(fis, Configuration.parallelSpeedUp.get());
            int cnt = swf.deobfuscateIdentifiers(renameType);
            swf.assignClassesToSymbols();
            System.out.println(cnt + " identifiers renamed.");
            swf.saveTo(fos);
        } catch (InterruptedException ex) {
            return false;
        } catch (IOException ex) {
            return false;
        }
        return true;
    }

    public boolean exportAS3Class(String className, String outdir, ScriptExportSettings exportSettings, boolean parallel, EventListener evl) throws Exception {
        boolean exported = false;

        List<ABCContainerTag> abcList = getAbcList();
        List<ABC> allAbcList = new ArrayList<>();
        for (int i = 0; i < abcList.size(); i++) {
            allAbcList.add(abcList.get(i).getABC());
        }

        for (int i = 0; i < abcList.size(); i++) {
            ABC abc = abcList.get(i).getABC();
            List<ScriptPack> scrs = abc.findScriptPacksByPath(className, allAbcList);
            for (int j = 0; j < scrs.size(); j++) {
                ScriptPack scr = scrs.get(j);
                if (!scr.isSimple && Configuration.ignoreCLikePackages.get()) {
                    continue;
                }
                String cnt = "";
                if (scrs.size() > 1) {
                    cnt = "script " + (j + 1) + "/" + scrs.size() + " ";
                }
                String eventData = cnt + scr.getPath() + " ...";
                evl.handleExportingEvent("tag", i + 1, abcList.size(), eventData);
                scr.export(outdir, exportSettings, parallel);
                evl.handleExportedEvent("tag", i + 1, abcList.size(), eventData);
                exported = true;
            }
        }
        return exported;
    }

    private List<ScriptPack> uniqueAS3Packs(List<ScriptPack> packs) {
        List<ScriptPack> ret = new ArrayList<>();
        Set<ClassPath> classPaths = new HashSet<>();
        for (ScriptPack item : packs) {
            ClassPath key = item.getClassPath();
            if (classPaths.contains(key)) {
                logger.log(Level.SEVERE, "Duplicate pack path found (" + key + ")!");
            } else {
                classPaths.add(key);
                ret.add(item);
            }
        }
        return ret;
    }

    public List<ScriptPack> getAS3Packs() {
        List<ScriptPack> packs = new ArrayList<>();

        List<ABCContainerTag> abcList = getAbcList();
        List<ABC> allAbcList = new ArrayList<>();
        for (int i = 0; i < abcList.size(); i++) {
            allAbcList.add(abcList.get(i).getABC());
        }

        for (ABCContainerTag abcTag : abcList) {
            packs.addAll(abcTag.getABC().getScriptPacks(null, allAbcList));
        }
        return uniqueAS3Packs(packs);
    }

    @Override
    public RECT getRect() {
        return displayRect;
    }

    @Override
    public RECT getRect(Set<BoundedTag> added) {
        return displayRect;
    }

    public EventListener getExportEventListener() {
        EventListener evl = new EventListener() {
            @Override
            public void handleExportingEvent(String type, int index, int count, Object data) {
                for (EventListener listener : listeners) {
                    listener.handleExportingEvent(type, index, count, data);
                }
            }

            @Override
            public void handleExportedEvent(String type, int index, int count, Object data) {
                for (EventListener listener : listeners) {
                    listener.handleExportedEvent(type, index, count, data);
                }
            }

            @Override
            public void handleEvent(String event, Object data) {
                informListeners(event, data);
            }
        };

        return evl;
    }

    public List<File> exportActionScript(AbortRetryIgnoreHandler handler, String outdir, ScriptExportSettings exportSettings, boolean parallel, EventListener evl) throws IOException {
        List<File> ret = new ArrayList<>();

        if (isAS3()) {
            ret.addAll(new AS3ScriptExporter().exportActionScript3(this, handler, outdir, exportSettings, parallel, evl));
        } else {
            ret.addAll(new AS2ScriptExporter().exportAS2ScriptsTimeout(handler, outdir, getASMs(true), exportSettings, evl));
        }
        return ret;
    }

    public Map<String, ASMSource> getASMs(boolean exportFileNames) {
        return getASMs(exportFileNames, new ArrayList<>(), true);
    }

    public Map<String, ASMSource> getASMs(boolean exportFileNames, List<TreeItem> nodesToExport, boolean exportAll) {
        Map<String, ASMSource> asmsToExport = new HashMap<>();
        for (TreeItem treeItem : getFirstLevelASMNodes(null)) {
            getASMs(exportFileNames, treeItem, nodesToExport, exportAll, asmsToExport, File.separator + getASMPath(exportFileNames, treeItem));
        }

        return asmsToExport;
    }

    private void getASMs(boolean exportFileNames, TreeItem treeItem, List<TreeItem> nodesToExport, boolean exportAll, Map<String, ASMSource> asmsToExport, String path) {
        boolean exportNode = nodesToExport.contains(treeItem);
        TreeItem realItem = treeItem instanceof TagScript ? ((TagScript) treeItem).getTag() : treeItem;
        if (realItem instanceof ASMSource && (exportAll || exportNode)) {
            String npath = path;
            int ppos = 1;
            while (asmsToExport.containsKey(npath)) {
                ppos++;
                npath = path + (exportFileNames ? "[" + ppos + "]" : "_" + ppos);
            }
            asmsToExport.put(npath, (ASMSource) realItem);
        }

        if (treeItem instanceof TagScript) {
            TagScript tagScript = (TagScript) treeItem;
            for (TreeItem subItem : tagScript.getFrames()) {
                getASMs(exportFileNames, subItem, nodesToExport, exportAll, asmsToExport, path + File.separator + getASMPath(exportFileNames, subItem));
            }
        } else if (treeItem instanceof FrameScript) {
            FrameScript frameScript = (FrameScript) treeItem;
            Frame parentFrame = frameScript.getFrame();
            for (TreeItem subItem : parentFrame.actionContainers) {
                getASMs(exportFileNames, getASMWrapToTagScript(subItem), nodesToExport, exportAll || exportNode, asmsToExport, path + File.separator + getASMPath(exportFileNames, subItem));
            }
            for (TreeItem subItem : parentFrame.actions) {
                getASMs(exportFileNames, getASMWrapToTagScript(subItem), nodesToExport, exportAll || exportNode, asmsToExport, path + File.separator + getASMPath(exportFileNames, subItem));
            }
        } else if (treeItem instanceof AS2Package) {
            AS2Package as2Package = (AS2Package) treeItem;
            for (TreeItem subItem : as2Package.subPackages.values()) {
                getASMs(exportFileNames, subItem, nodesToExport, exportAll, asmsToExport, path + File.separator + getASMPath(exportFileNames, subItem));
            }
            for (TreeItem subItem : as2Package.scripts.values()) {
                getASMs(exportFileNames, subItem, nodesToExport, exportAll, asmsToExport, path + File.separator + getASMPath(exportFileNames, subItem));
            }
        }
    }

    private String getASMPath(boolean exportFileName, TreeItem treeItem) {
        if (!exportFileName) {
            return treeItem.toString();
        }

        String result;
        if (treeItem instanceof Exportable) {
            result = ((Exportable) treeItem).getExportFileName();
        } else {
            result = treeItem.toString();
        }

        return Helper.makeFileName(result);
    }

    private TreeItem getASMWrapToTagScript(TreeItem treeItem) {
        if (treeItem instanceof Tag) {
            Tag resultTag = (Tag) treeItem;
            List<TreeItem> subNodes = new ArrayList<>();
            if (treeItem instanceof ASMSourceContainer) {
                for (ASMSource item : ((ASMSourceContainer) treeItem).getSubItems()) {
                    subNodes.add(item);
                }
            }

            TagScript tagScript = new TagScript(treeItem.getSwf(), resultTag, subNodes);
            return tagScript;
        }

        return treeItem;
    }

    public List<TreeItem> getFirstLevelASMNodes(Map<Tag, TagScript> tagScriptCache) {
        Timeline timeline = getTimeline();
        List<TreeItem> subNodes = new ArrayList<>();
        List<TreeItem> subFrames = new ArrayList<>();
        subNodes.addAll(timeline.getAS2RootPackage().subPackages.values());
        subNodes.addAll(timeline.getAS2RootPackage().scripts.values());

        for (Tag tag : timeline.otherTags) {
            boolean hasInnerFrames = false;
            List<TreeItem> tagSubNodes = new ArrayList<>();
            if (tag instanceof Timelined) {
                Timeline timeline2 = ((Timelined) tag).getTimeline();
                for (Frame frame : timeline2.getFrames()) {
                    if (!frame.actions.isEmpty() || !frame.actionContainers.isEmpty()) {
                        FrameScript frameScript = new FrameScript(this, frame);
                        tagSubNodes.add(frameScript);
                        hasInnerFrames = true;
                    }
                }
            }

            if (tag instanceof ASMSourceContainer) {
                for (ASMSource asm : ((ASMSourceContainer) tag).getSubItems()) {
                    tagSubNodes.add(asm);
                }
            }

            if (!tagSubNodes.isEmpty()) {
                TagScript ts = new TagScript(this, tag, tagSubNodes);
                if (tagScriptCache != null) {
                    tagScriptCache.put(tag, ts);
                }
                if (hasInnerFrames) {
                    subFrames.add(ts);
                } else {
                    subNodes.add(ts);
                }
            }
        }

        subNodes.addAll(subFrames);
        for (Frame frame : timeline.getFrames()) {
            if (!frame.actions.isEmpty() || !frame.actionContainers.isEmpty()) {
                FrameScript frameScript = new FrameScript(this, frame);
                subNodes.add(frameScript);
            }
        }

        return subNodes;
    }

    private final HashSet<EventListener> listeners = new HashSet<>();

    public final void addEventListener(EventListener listener) {
        listeners.add(listener);
        for (Tag t : tags) {
            if (t instanceof ABCContainerTag) {
                (((ABCContainerTag) t).getABC()).addEventListener(listener);
            }
        }
    }

    public final void removeEventListener(EventListener listener) {
        listeners.remove(listener);
        for (Tag t : tags) {
            if (t instanceof ABCContainerTag) {
                (((ABCContainerTag) t).getABC()).removeEventListener(listener);
            }
        }
    }

    protected void informListeners(String event, Object data) {
        for (EventListener listener : listeners) {
            listener.handleEvent(event, data);
        }
    }

    public static void populateVideoFrames(int streamId, List<Tag> tags, HashMap<Integer, VideoFrameTag> output) {
        for (Tag t : tags) {
            if (t instanceof VideoFrameTag) {
                output.put(((VideoFrameTag) t).frameNum, (VideoFrameTag) t);
            }
            if (t instanceof DefineSpriteTag) {
                populateVideoFrames(streamId, ((DefineSpriteTag) t).getSubTags(), output);
            }
        }
    }

    private static void writeLE(OutputStream os, long val, int size) throws IOException {
        for (int i = 0; i < size; i++) {
            os.write((int) (val & 0xff));
            val >>= 8;
        }
    }

    public static void createWavFromPcmData(OutputStream fos, int soundRateHz, boolean soundSize, boolean soundType, byte[] data) throws IOException {
        ByteArrayOutputStream subChunk1Data = new ByteArrayOutputStream();
        int audioFormat = 1; // PCM
        writeLE(subChunk1Data, audioFormat, 2);
        int numChannels = soundType ? 2 : 1;
        writeLE(subChunk1Data, numChannels, 2);
        int[] rateMap = {5512, 11025, 22050, 44100};
        int sampleRate = soundRateHz; // rateMap[soundRate];
        writeLE(subChunk1Data, sampleRate, 4);
        int bitsPerSample = soundSize ? 16 : 8;
        int byteRate = sampleRate * numChannels * bitsPerSample / 8;
        writeLE(subChunk1Data, byteRate, 4);
        int blockAlign = numChannels * bitsPerSample / 8;
        writeLE(subChunk1Data, blockAlign, 2);
        writeLE(subChunk1Data, bitsPerSample, 2);

        ByteArrayOutputStream chunks = new ByteArrayOutputStream();
        chunks.write(Utf8Helper.getBytes("fmt "));
        byte[] subChunk1DataBytes = subChunk1Data.toByteArray();
        writeLE(chunks, subChunk1DataBytes.length, 4);
        chunks.write(subChunk1DataBytes);

        chunks.write(Utf8Helper.getBytes("data"));
        writeLE(chunks, data.length, 4);
        chunks.write(data);

        fos.write(Utf8Helper.getBytes("RIFF"));
        byte[] chunkBytes = chunks.toByteArray();
        writeLE(fos, 4 + chunkBytes.length, 4);
        fos.write(Utf8Helper.getBytes("WAVE"));
        fos.write(chunkBytes);
    }

    public static String getTypePrefix(CharacterTag c) {
        if (c instanceof ShapeTag) {
            return "shape";
        }
        if (c instanceof MorphShapeTag) {
            return "morphshape";
        }
        if (c instanceof DefineSpriteTag) {
            return "sprite";
        }
        if (c instanceof TextTag) {
            return "text";
        }
        if (c instanceof ButtonTag) {
            return "button";
        }
        if (c instanceof FontTag) {
            return "font";
        }
        if (c instanceof ImageTag) {
            return "image";
        }
        return "character";
    }

    public static void writeLibrary(SWF fswf, Set<Integer> library, OutputStream fos) throws IOException {
        for (int c : library) {
            CharacterTag ch = fswf.getCharacter(c);
            if (ch instanceof FontTag) {
                StringBuilder sb = new StringBuilder();
                sb.append("function ").append(getTypePrefix(ch)).append(c).append("(ctx,ch,textColor){\r\n");
                ((FontTag) ch).toHtmlCanvas(sb, 1);
                sb.append("}\r\n\r\n");
                fos.write(Utf8Helper.getBytes(sb.toString()));
            } else {
                if (ch instanceof ImageTag) {
                    ImageTag image = (ImageTag) ch;
                    ImageFormat format = image.getImageFormat();
                    byte[] imageData = Helper.readStream(image.getImageData());
                    String base64ImgData = Helper.byteArrayToBase64String(imageData);
                    fos.write(Utf8Helper.getBytes("var imageObj" + c + " = document.createElement(\"img\");\r\nimageObj" + c + ".src=\"data:image/" + format + ";base64," + base64ImgData + "\";\r\n"));
                }
                fos.write(Utf8Helper.getBytes("function " + getTypePrefix(ch) + c + "(ctx,ctrans,frame,ratio,time){\r\n"));
                if (ch instanceof DrawableTag) {
                    StringBuilder sb = new StringBuilder();
                    ((DrawableTag) ch).toHtmlCanvas(sb, 1);
                    fos.write(Utf8Helper.getBytes(sb.toString()));
                }
                fos.write(Utf8Helper.getBytes("}\r\n\r\n"));
            }
        }
    }

    private static void getVariables(ConstantPool constantPool, BaseLocalData localData, TranslateStack stack, List<GraphTargetItem> output, ActionGraphSource code, int ip, List<MyEntry<DirectValueActionItem, ConstantPool>> variables, List<GraphSourceItem> functions, HashMap<DirectValueActionItem, ConstantPool> strings, List<Integer> visited, HashMap<DirectValueActionItem, String> usageTypes, String path) throws InterruptedException {
        boolean debugMode = false;
        while ((ip > -1) && ip < code.size()) {
            if (visited.contains(ip)) {
                break;
            }
            GraphSourceItem ins = code.get(ip);

            if (debugMode) {
                System.err.println("Visit " + ip + ": ofs" + Helper.formatAddress(((Action) ins).getAddress()) + ":" + ((Action) ins).getASMSource(new ActionList(), new HashSet<>(), ScriptExportMode.PCODE) + " stack:" + Helper.stackToString(stack, LocalData.create(new ConstantPool())));
            }
            if (ins.isExit()) {
                break;
            }
            if (ins.isIgnored()) {
                ip++;
                continue;
            }

            String usageType = "name";
            GraphTargetItem name = null;
            if ((ins instanceof ActionGetVariable)
                    || (ins instanceof ActionGetMember)
                    || (ins instanceof ActionDefineLocal2)
                    || (ins instanceof ActionNewMethod)
                    || (ins instanceof ActionNewObject)
                    || (ins instanceof ActionCallMethod)
                    || (ins instanceof ActionCallFunction)) {
                if (stack.isEmpty()) {
                    break;
                }
                name = stack.peek();
            }

            if ((ins instanceof ActionGetVariable) || (ins instanceof ActionDefineLocal2)) {
                usageType = "variable";
            }
            if (ins instanceof ActionGetMember) {
                usageType = "member";
            }
            if ((ins instanceof ActionNewMethod) || (ins instanceof ActionNewObject)) {
                usageType = "class";
            }
            if (ins instanceof ActionCallMethod) {
                usageType = "function"; // can there be method?
            }
            if (ins instanceof ActionCallFunction) {
                usageType = "function";
            }

            if ((ins instanceof ActionDefineFunction) || (ins instanceof ActionDefineFunction2)) {
                functions.add(ins);
            }

            if (ins instanceof GraphSourceItemContainer) {
                GraphSourceItemContainer cnt = (GraphSourceItemContainer) ins;
                List<Long> cntSizes = cnt.getContainerSizes();
                long addr = code.pos2adr(ip + 1);
                ip = code.adr2pos(addr);
                String cntName = cnt.getName();
                for (Long size : cntSizes) {
                    if (size == 0) {
                        continue;
                    }
                    ip = code.adr2pos(addr);
                    addr += size;
                    int nextip = code.adr2pos(addr);
                    getVariables(variables, functions, strings, usageTypes, new ActionGraphSource(code.getActions().subList(ip, nextip), code.version, new HashMap<>(), new HashMap<>(), new HashMap<>()), 0, path + (cntName == null ? "" : "/" + cntName));
                    ip = nextip;
                }
                List<List<GraphTargetItem>> r = new ArrayList<>();
                r.add(new ArrayList<>());
                r.add(new ArrayList<>());
                r.add(new ArrayList<>());
                try {
                    ((GraphSourceItemContainer) ins).translateContainer(r, stack, output, new HashMap<>(), new HashMap<>(), new HashMap<>());
                } catch (EmptyStackException ex) {
                }

                continue;
            }

            if ((ins instanceof ActionSetVariable) || (ins instanceof ActionSetMember) || (ins instanceof ActionDefineLocal)) {
                if (stack.size() < 2) {
                    break;
                }
                name = stack.get(stack.size() - 2);
            }

            if ((ins instanceof ActionSetVariable) || (ins instanceof ActionDefineLocal)) {
                usageType = "variable";
            }

            if (ins instanceof ActionSetMember) {
                usageType = "member";
            }

            if (name instanceof DirectValueActionItem) {
                variables.add(new MyEntry<>((DirectValueActionItem) name, constantPool));
                usageTypes.put((DirectValueActionItem) name, usageType);
            }

            // for..in return
            if (((ins instanceof ActionEquals) || (ins instanceof ActionEquals2)) && (stack.size() == 1) && (stack.peek() instanceof DirectValueActionItem)) {
                stack.push(new DirectValueActionItem(null, 0, new Null(), new ArrayList<>()));
            }

            if (ins instanceof ActionConstantPool) {
                constantPool = new ConstantPool(((ActionConstantPool) ins).constantPool);
            }
            int staticOperation = Graph.SOP_USE_STATIC; //(Boolean) Configuration.getConfig("autoDeobfuscate", true) ? Graph.SOP_SKIP_STATIC : Graph.SOP_USE_STATIC;

            try {
                ins.translate(localData, stack, output, staticOperation, path);
            } catch (EmptyStackException ex) {
                // probably obfucated code, never executed branch
                break;
            }
            if (ins.isExit()) {
                break;
            }

            if (ins instanceof ActionPush) {
                if (!stack.isEmpty()) {
                    GraphTargetItem top = stack.peek();
                    if (top instanceof DirectValueActionItem) {
                        DirectValueActionItem dvt = (DirectValueActionItem) top;
                        if ((dvt.value instanceof String) || (dvt.value instanceof ConstantIndex)) {
                            if (constantPool == null) {
                                constantPool = new ConstantPool(dvt.constants);
                            }
                            strings.put(dvt, constantPool);
                        }
                    }
                }
            }

            if (ins.isBranch() || ins.isJump()) {
                if (ins instanceof ActionIf) {
                    if (stack.isEmpty()) {
                        break;
                    }
                    stack.pop();
                }
                visited.add(ip);
                List<Integer> branches = ins.getBranches(code);
                for (int b : branches) {
                    TranslateStack brStack = (TranslateStack) stack.clone();
                    if (b >= 0) {
                        getVariables(constantPool, localData, brStack, output, code, b, variables, functions, strings, visited, usageTypes, path);
                    } else {
                        if (debugMode) {
                            System.out.println("Negative branch:" + b);
                        }
                    }
                }
                // }
                break;
            }
            ip++;
        };
    }

    private static void getVariables(List<MyEntry<DirectValueActionItem, ConstantPool>> variables, List<GraphSourceItem> functions, HashMap<DirectValueActionItem, ConstantPool> strings, HashMap<DirectValueActionItem, String> usageTypes, ActionGraphSource code, int addr, String path) throws InterruptedException {
        ActionLocalData localData = new ActionLocalData();
        getVariables(null, localData, new TranslateStack(path), new ArrayList<>(), code, code.adr2pos(addr), variables, functions, strings, new ArrayList<>(), usageTypes, path);
    }

    private List<MyEntry<DirectValueActionItem, ConstantPool>> getVariables(List<MyEntry<DirectValueActionItem, ConstantPool>> variables, HashMap<ASMSource, ActionList> actionsMap, List<GraphSourceItem> functions, HashMap<DirectValueActionItem, ConstantPool> strings, HashMap<DirectValueActionItem, String> usageTypes, ASMSource src, String path) throws InterruptedException {
        List<MyEntry<DirectValueActionItem, ConstantPool>> ret = new ArrayList<>();
        ActionList actions = src.getActions();
        actionsMap.put(src, actions);
        getVariables(variables, functions, strings, usageTypes, new ActionGraphSource(actions, version, new HashMap<>(), new HashMap<>(), new HashMap<>()), 0, path);
        return ret;
    }

    private void getVariables(List<Tag> tags, String path, List<MyEntry<DirectValueActionItem, ConstantPool>> variables, HashMap<ASMSource, ActionList> actionsMap, List<GraphSourceItem> functions, HashMap<DirectValueActionItem, ConstantPool> strings, HashMap<DirectValueActionItem, String> usageTypes) throws InterruptedException {
        List<String> processed = new ArrayList<>();
        for (Tag t : tags) {
            String subPath = path + "/" + t.toString();
            if (t instanceof ASMSource) {
                addVariable((ASMSource) t, subPath, processed, variables, actionsMap, functions, strings, usageTypes);
            }
            if (t instanceof ASMSourceContainer) {
                List<String> processed2 = new ArrayList<>();
                for (ASMSource asm : ((ASMSourceContainer) t).getSubItems()) {
                    addVariable(asm, subPath + "/" + asm.toString(), processed2, variables, actionsMap, functions, strings, usageTypes);
                }
            }
            if (t instanceof DefineSpriteTag) {
                getVariables(((DefineSpriteTag) t).getSubTags(), path + "/" + t.toString(), variables, actionsMap, functions, strings, usageTypes);
            }
        }
    }

    private void addVariable(ASMSource asm, String path, List<String> processed, List<MyEntry<DirectValueActionItem, ConstantPool>> variables, HashMap<ASMSource, ActionList> actionsMap, List<GraphSourceItem> functions, HashMap<DirectValueActionItem, ConstantPool> strings, HashMap<DirectValueActionItem, String> usageTypes) throws InterruptedException {
        int pos = 1;
        String infPath2 = path;
        while (processed.contains(infPath2)) {
            pos++;
            infPath2 = path + "[" + pos + "]";
        }
        processed.add(infPath2);
        informListeners("getVariables", infPath2);
        getVariables(variables, actionsMap, functions, strings, usageTypes, asm, path);
    }

    public void fixAS3Code() {
        for (ABCContainerTag abcTag : getAbcList()) {
            ABC abc = abcTag.getABC();
            for (MethodBody body : abc.bodies) {
                AVM2Code code = body.getCode();
                body.setCodeBytes(code.getBytes());
            }

            ((Tag) abcTag).setModified(true);
        }
    }

    public int deobfuscateAS3Identifiers(RenameType renameType) {
        for (Tag tag : tags) {
            if (tag instanceof ABCContainerTag) {
                ((ABCContainerTag) tag).getABC().deobfuscateIdentifiers(deobfuscated, renameType, true);
                tag.setModified(true);
            }
        }
        for (Tag tag : tags) {
            if (tag instanceof ABCContainerTag) {
                ((ABCContainerTag) tag).getABC().deobfuscateIdentifiers(deobfuscated, renameType, false);
                tag.setModified(true);
            }
        }
        for (Tag tag : tags) {
            if (tag instanceof SymbolClassTag) {
                SymbolClassTag sc = (SymbolClassTag) tag;
                for (int i = 0; i < sc.names.size(); i++) {
                    String newname = deobfuscation.deobfuscateNameWithPackage(true, sc.names.get(i), deobfuscated, renameType, deobfuscated);
                    if (newname != null) {
                        sc.names.set(i, newname);
                    }
                }
                sc.setModified(true);
            }
        }
        deobfuscation.deobfuscateInstanceNames(true, deobfuscated, renameType, tags, new HashMap<>());
        return deobfuscated.size();
    }

    public int deobfuscateIdentifiers(RenameType renameType) throws InterruptedException {
        FileAttributesTag fileAttributes = getFileAttributes();
        if (fileAttributes == null) {
            int cnt = 0;
            cnt += deobfuscateAS2Identifiers(renameType);
            cnt += deobfuscateAS3Identifiers(renameType);
            return cnt;
        } else {
            if (fileAttributes.actionScript3) {
                return deobfuscateAS3Identifiers(renameType);
            } else {
                return deobfuscateAS2Identifiers(renameType);
            }
        }
    }

    public void renameAS2Identifier(String identifier, String newname) throws InterruptedException {
        Map<String, String> selected = new HashMap<>();
        selected.put(identifier, newname);
        renameAS2Identifiers(null, selected);
    }

    private int deobfuscateAS2Identifiers(RenameType renameType) throws InterruptedException {
        return renameAS2Identifiers(renameType, null);
    }

    private int renameAS2Identifiers(RenameType renameType, Map<String, String> selected) throws InterruptedException {
        HashMap<ASMSource, ActionList> actionsMap = new HashMap<>();
        List<GraphSourceItem> allFunctions = new ArrayList<>();
        List<MyEntry<DirectValueActionItem, ConstantPool>> allVariableNames = new ArrayList<>();
        HashMap<DirectValueActionItem, ConstantPool> allStrings = new HashMap<>();
        HashMap<DirectValueActionItem, String> usageTypes = new HashMap<>();

        int ret = 0;
        getVariables(tags, "", allVariableNames, actionsMap, allFunctions, allStrings, usageTypes);
        informListeners("rename", "");
        int fc = 0;
        for (MyEntry<DirectValueActionItem, ConstantPool> it : allVariableNames) {
            String name = it.getKey().toStringNoH(it.getValue());
            deobfuscation.allVariableNamesStr.add(name);
        }

        informListeners("rename", "classes");
        int classCount = 0;
        for (Tag t : tags) {
            if (t instanceof DoInitActionTag) {
                classCount++;
            }
        }
        int cnt = 0;
        for (Tag t : tags) {
            if (t instanceof DoInitActionTag) {
                cnt++;
                informListeners("rename", "class " + cnt + "/" + classCount);
                DoInitActionTag dia = (DoInitActionTag) t;
                String exportName = getExportName(dia.spriteId);
                exportName = exportName != null ? exportName : "_unk_";
                final String pkgPrefix = "__Packages.";
                String[] classNameParts = null;
                if (exportName.startsWith(pkgPrefix)) {
                    String className = exportName.substring(pkgPrefix.length());
                    if (className.contains(".")) {
                        classNameParts = className.split("\\.");
                    } else {
                        classNameParts = new String[]{className};
                    }
                }
                int staticOperation = Graph.SOP_USE_STATIC; //(Boolean) Configuration.getConfig("autoDeobfuscate", true) ? Graph.SOP_SKIP_STATIC : Graph.SOP_USE_STATIC;
                List<GraphTargetItem> dec;
                try {
                    dec = Action.actionsToTree(dia.getActions(), version, staticOperation, ""/*FIXME*/);
                } catch (EmptyStackException ex) {
                    continue;
                }
                GraphTargetItem name = null;
                for (GraphTargetItem it : dec) {
                    if (it instanceof ClassActionItem) {
                        ClassActionItem cti = (ClassActionItem) it;
                        List<GraphTargetItem> methods = new ArrayList<>();
                        methods.addAll(cti.functions);
                        methods.addAll(cti.staticFunctions);

                        for (GraphTargetItem gti : methods) {
                            if (gti instanceof FunctionActionItem) {
                                FunctionActionItem fun = (FunctionActionItem) gti;
                                if (fun.calculatedFunctionName instanceof DirectValueActionItem) {
                                    DirectValueActionItem dvf = (DirectValueActionItem) fun.calculatedFunctionName;
                                    String fname = dvf.toStringNoH(null);
                                    String changed = deobfuscation.deobfuscateName(false, fname, false, "method", deobfuscated, renameType, selected);
                                    if (changed != null) {
                                        deobfuscated.put(fname, changed);
                                    }
                                }
                            }
                        }

                        List<GraphTargetItem> vars = new ArrayList<>();
                        for (MyEntry<GraphTargetItem, GraphTargetItem> item : cti.vars) {
                            vars.add(item.getKey());
                        }
                        for (MyEntry<GraphTargetItem, GraphTargetItem> item : cti.staticVars) {
                            vars.add(item.getKey());
                        }
                        for (GraphTargetItem gti : vars) {
                            if (gti instanceof DirectValueActionItem) {
                                DirectValueActionItem dvf = (DirectValueActionItem) gti;
                                String vname = dvf.toStringNoH(null);
                                String changed = deobfuscation.deobfuscateName(false, vname, false, "attribute", deobfuscated, renameType, selected);
                                if (changed != null) {
                                    deobfuscated.put(vname, changed);
                                }
                            }
                        }

                        name = cti.className;
                        break;
                    }
                    if (it instanceof InterfaceActionItem) {
                        InterfaceActionItem ift = (InterfaceActionItem) it;
                        name = ift.name;
                    }
                }

                if (name != null) {
                    int pos = 0;
                    while (name instanceof GetMemberActionItem) {
                        GetMemberActionItem mem = (GetMemberActionItem) name;
                        GraphTargetItem memberName = mem.memberName;
                        if (memberName instanceof DirectValueActionItem) {
                            DirectValueActionItem dvt = (DirectValueActionItem) memberName;
                            String nameStr = dvt.toStringNoH(null);
                            if (classNameParts != null) {
                                if (classNameParts.length - 1 - pos < 0) {
                                    break;
                                }
                            }
                            String changedNameStr = nameStr;
                            if (classNameParts != null) {
                                changedNameStr = classNameParts[classNameParts.length - 1 - pos];
                            }
                            String changedNameStr2 = deobfuscation.deobfuscateName(false, changedNameStr, pos == 0, pos == 0 ? "class" : "package", deobfuscated, renameType, selected);
                            if (changedNameStr2 != null) {
                                changedNameStr = changedNameStr2;
                            }
                            ret++;
                            deobfuscated.put(nameStr, changedNameStr);
                            pos++;
                        }
                        name = mem.object;
                    }
                    if (name instanceof GetVariableActionItem) {
                        GetVariableActionItem var = (GetVariableActionItem) name;
                        if (var.name instanceof DirectValueActionItem) {
                            DirectValueActionItem dvt = (DirectValueActionItem) var.name;
                            String nameStr = dvt.toStringNoH(null);
                            if (classNameParts != null) {
                                if (classNameParts.length - 1 - pos < 0) {
                                    break;
                                }
                            }
                            String changedNameStr = nameStr;
                            if (classNameParts != null) {
                                changedNameStr = classNameParts[classNameParts.length - 1 - pos];
                            }
                            String changedNameStr2 = deobfuscation.deobfuscateName(false, changedNameStr, pos == 0, pos == 0 ? "class" : "package", deobfuscated, renameType, selected);
                            if (changedNameStr2 != null) {
                                changedNameStr = changedNameStr2;
                            }
                            ret++;
                            deobfuscated.put(nameStr, changedNameStr);
                            pos++;
                        }
                    }
                }
                t.setModified(true);
            }
        }

        for (GraphSourceItem fun : allFunctions) {
            fc++;
            informListeners("rename", "function " + fc + "/" + allFunctions.size());
            if (fun instanceof ActionDefineFunction) {
                ActionDefineFunction f = (ActionDefineFunction) fun;
                if (f.functionName.isEmpty()) { // anonymous function, leave as is
                    continue;
                }
                String changed = deobfuscation.deobfuscateName(false, f.functionName, false, "function", deobfuscated, renameType, selected);
                if (changed != null) {
                    f.replacedFunctionName = changed;
                    ret++;
                }
            }
            if (fun instanceof ActionDefineFunction2) {
                ActionDefineFunction2 f = (ActionDefineFunction2) fun;
                if (f.functionName.isEmpty()) { // anonymous function, leave as is
                    continue;
                }
                String changed = deobfuscation.deobfuscateName(false, f.functionName, false, "function", deobfuscated, renameType, selected);
                if (changed != null) {
                    f.replacedFunctionName = changed;
                    ret++;
                }
            }
        }

        HashSet<String> stringsNoVarH = new HashSet<>();
        List<DirectValueActionItem> allVariableNamesDv = new ArrayList<>();
        for (MyEntry<DirectValueActionItem, ConstantPool> it : allVariableNames) {
            allVariableNamesDv.add(it.getKey());
        }
        for (DirectValueActionItem ti : allStrings.keySet()) {
            if (!allVariableNamesDv.contains(ti)) {
                stringsNoVarH.add(System.identityHashCode(allStrings.get(ti)) + "_" + ti.toStringNoH(allStrings.get(ti)));
            }
        }

        int vc = 0;
        for (MyEntry<DirectValueActionItem, ConstantPool> it : allVariableNames) {
            vc++;
            String name = it.getKey().toStringNoH(it.getValue());
            String changed = deobfuscation.deobfuscateName(false, name, false, usageTypes.get(it.getKey()), deobfuscated, renameType, selected);
            if (changed != null) {
                boolean addNew = false;
                String h = System.identityHashCode(it.getKey()) + "_" + name;
                if (stringsNoVarH.contains(h)) {
                    addNew = true;
                }
                ActionPush pu = (ActionPush) it.getKey().src;
                if (pu.replacement == null) {
                    pu.replacement = new ArrayList<>();
                    pu.replacement.addAll(pu.values);
                }
                if (pu.replacement.get(it.getKey().pos) instanceof ConstantIndex) {
                    ConstantIndex ci = (ConstantIndex) pu.replacement.get(it.getKey().pos);
                    ConstantPool pool = it.getValue();
                    if (pool == null) {
                        continue;
                    }
                    if (pool.constants == null) {
                        continue;
                    }
                    if (addNew) {
                        pool.constants.add(changed);
                        ci.index = pool.constants.size() - 1;
                    } else {
                        pool.constants.set(ci.index, changed);
                    }
                } else {
                    pu.replacement.set(it.getKey().pos, changed);
                }
                ret++;
            }
        }

        for (ASMSource src : actionsMap.keySet()) {
            actionsMap.get(src).removeNops();
            src.setActions(actionsMap.get(src));
            src.setModified();
        }

        deobfuscation.deobfuscateInstanceNames(false, deobfuscated, renameType, tags, selected);
        return ret;
    }

    public void exportFla(AbortRetryIgnoreHandler handler, String outfile, String swfName, String generator, String generatorVerName, String generatorVersion, boolean parallel, FLAVersion version) throws IOException {
        XFLConverter.convertSWF(handler, this, swfName, outfile, true, generator, generatorVerName, generatorVersion, parallel, version);
        clearAllCache();
    }

    public void exportXfl(AbortRetryIgnoreHandler handler, String outfile, String swfName, String generator, String generatorVerName, String generatorVersion, boolean parallel, FLAVersion version) throws IOException {
        XFLConverter.convertSWF(handler, this, swfName, outfile, false, generator, generatorVerName, generatorVersion, parallel, version);
        clearAllCache();
    }

    public static AffineTransform matrixToTransform(MATRIX mat) {
        return new AffineTransform(mat.getScaleXFloat(), mat.getRotateSkew0Float(),
                mat.getRotateSkew1Float(), mat.getScaleYFloat(),
                mat.translateX, mat.translateY);
    }

    public SerializableImage getFromCache(String key) {
        if (frameCache.contains(key)) {
            return frameCache.get(key);
        }
        return null;
    }

    public byte[] getFromCache(SoundTag soundTag) {
        if (soundCache.contains(soundTag)) {
            return soundCache.get(soundTag);
        }
        return null;
    }

    public void putToCache(String key, SerializableImage img) {
        if (Configuration.useFrameCache.get()) {
            frameCache.put(key, img);
        }
    }

    public void putToCache(SoundTag soundTag, byte[] data) {
        soundCache.put(soundTag, data);
    }

    public void clearImageCache() {
        frameCache.clear();
        DefineSpriteTag.clearCache();
        DefineButtonTag.clearCache();
        DefineButton2Tag.clearCache();
        for (Tag tag : tags) {
            if (tag instanceof ImageTag) {
                ((ImageTag) tag).clearCache();
            }
        }
    }

    public void clearScriptCache() {
        as2Cache.clear();
        as3Cache.clear();
    }

    public void clearAllCache() {
        characters = null;
        abcList = null;
        timeline = null;
        clearImageCache();
        clearScriptCache();
        Cache.clearAll();
        Helper.clearShapeCache();
        System.gc();
    }

    public static void uncache(ASMSource src) {
        if (src != null) {
            src.getSwf().as2Cache.remove(src);
        }
    }

    public static void uncache(ScriptPack pack) {
        if (pack != null) {
            pack.getSwf().as3Cache.remove(pack);
        }
    }

    public static boolean isCached(ASMSource src) {
        return src.getSwf().as2Cache.contains(src);
    }

    public static boolean isCached(ScriptPack pack) {
        return pack.getSwf().as3Cache.contains(pack);
    }

    public static CachedScript getCached(ASMSource src, ActionList actions) throws InterruptedException {
        SWF swf = src.getSwf();
        if (swf.as2Cache.contains(src)) {
            return swf.as2Cache.get(src);
        }

        if (actions == null) {
            actions = src.getActions();
        }

        HighlightedTextWriter writer = new HighlightedTextWriter(Configuration.getCodeFormatting(), true);
        Action.actionsToSource(src, actions, src.toString()/*FIXME?*/, writer);
        List<Highlighting> hilights = writer.instructionHilights;
        String srcNoHex = writer.toString();
        CachedScript res = new CachedScript(srcNoHex, hilights);
        swf.as2Cache.put(src, res);
        return res;
    }

    public static CachedDecompilation getCached(ScriptPack pack) throws InterruptedException {
        SWF swf = pack.getSwf();
        if (swf.as3Cache.contains(pack)) {
            return swf.as3Cache.get(pack);
        }

        int scriptIndex = pack.scriptIndex;
        ScriptInfo script = null;
        if (scriptIndex > -1) {
            script = pack.abc.script_info.get(scriptIndex);
        }
        boolean parallel = Configuration.parallelSpeedUp.get();
        HighlightedTextWriter writer = new HighlightedTextWriter(Configuration.getCodeFormatting(), true);
        pack.toSource(writer, script.traits.traits, ScriptExportMode.AS, parallel);
        HighlightedText hilightedCode = new HighlightedText(writer);
        CachedDecompilation res = new CachedDecompilation(hilightedCode);
        swf.as3Cache.put(pack, res);

        return res;
    }

    public static RECT fixRect(RECT rect) {
        RECT ret = new RECT();
        ret.Xmin = rect.Xmin;
        ret.Xmax = rect.Xmax;
        ret.Ymin = rect.Ymin;
        ret.Ymax = rect.Ymax;

        if (ret.Xmax <= 0) {
            ret.Xmax = ret.getWidth();
            ret.Xmin = 0;
        }
        if (ret.Ymax <= 0) {
            ret.Ymax = ret.getHeight();
            ret.Ymin = 0;
        }
        if (ret.Xmin < 0) {
            ret.Xmax += (-ret.Xmin);
            ret.Xmin = 0;
        }
        if (ret.Ymin < 0) {
            ret.Ymax += (-ret.Ymin);
            ret.Ymin = 0;
        }

        if (ret.getWidth() < 1 || ret.getHeight() < 1) {
            ret.Xmin = 0;
            ret.Ymin = 0;
            ret.Xmax = 20;
            ret.Ymax = 20;
        }
        return ret;
    }

    public static void frameToSvg(Timeline timeline, int frame, int time, DepthState stateUnderCursor, int mouseButton, SVGExporter exporter, ColorTransform colorTransform, int level, double zoom) throws IOException {
        if (timeline.getFrameCount() <= frame) {
            return;
        }
        Frame frameObj = timeline.getFrame(frame);
        List<SvgClip> clips = new ArrayList<>();
        List<String> prevClips = new ArrayList<>();

        int maxDepth = timeline.getMaxDepth();
        for (int i = 1; i <= maxDepth; i++) {
            for (int c = 0; c < clips.size(); c++) {
                if (clips.get(c).depth == i) {
                    exporter.setClip(prevClips.get(c));
                    prevClips.remove(c);
                    clips.remove(c);
                }
            }
            if (!frameObj.layers.containsKey(i)) {
                continue;
            }
            DepthState layer = frameObj.layers.get(i);
            if (!timeline.swf.getCharacters().containsKey(layer.characterId)) {
                continue;
            }
            if (!layer.isVisible) {
                continue;
            }

            CharacterTag character = timeline.swf.getCharacter(layer.characterId);
            if (colorTransform == null) {
                colorTransform = new ColorTransform();
            }

            ColorTransform clrTrans = colorTransform.clone();
            if (layer.colorTransForm != null && layer.blendMode <= 1) { // Normal blend mode
                clrTrans = colorTransform.merge(layer.colorTransForm);
            }

            if (character instanceof DrawableTag) {
                DrawableTag drawable = (DrawableTag) character;

                String assetName;
                Tag drawableTag = (Tag) drawable;
                RECT boundRect = drawable.getRect();
                if (exporter.exportedTags.containsKey(drawableTag)) {
                    assetName = exporter.exportedTags.get(drawableTag);
                } else {
                    assetName = getTagIdPrefix(drawableTag, exporter);
                    exporter.exportedTags.put(drawableTag, assetName);
                    exporter.createDefGroup(new ExportRectangle(boundRect), assetName);
                    drawable.toSVG(exporter, layer.ratio, clrTrans, level + 1, zoom);
                    exporter.endGroup();
                }
                ExportRectangle rect = new ExportRectangle(boundRect);

                // TODO: if (layer.filters != null)
                // TODO: if (layer.blendMode > 1)
                if (layer.clipDepth > -1) {
                    String clipName = exporter.getUniqueId("clipPath");
                    exporter.createClipPath(new Matrix(), clipName);
                    SvgClip clip = new SvgClip(clipName, layer.clipDepth);
                    clips.add(clip);
                    prevClips.add(exporter.getClip());
                    Matrix mat = Matrix.getTranslateInstance(rect.xMin, rect.yMin).preConcatenate(new Matrix(layer.matrix));
                    exporter.addUse(mat, boundRect, assetName, layer.instanceName);
                    exporter.setClip(clip.shape);
                    exporter.endGroup();
                } else {
                    Matrix mat = Matrix.getTranslateInstance(rect.xMin, rect.yMin).preConcatenate(new Matrix(layer.matrix));
                    exporter.addUse(mat, boundRect, assetName, layer.instanceName);
                }
            }
        }
    }

    private static String getTagIdPrefix(Tag tag, SVGExporter exporter) {
        if (tag instanceof ShapeTag) {
            return exporter.getUniqueId("shape");
        }
        if (tag instanceof MorphShapeTag) {
            return exporter.getUniqueId("morphshape");
        }
        if (tag instanceof DefineSpriteTag) {
            return exporter.getUniqueId("sprite");
        }
        if (tag instanceof TextTag) {
            return exporter.getUniqueId("text");
        }
        if (tag instanceof ButtonTag) {
            return exporter.getUniqueId("button");
        }
        return exporter.getUniqueId("tag");
    }

    public static SerializableImage frameToImageGet(Timeline timeline, int frame, int time, DepthState stateUnderCursor, int mouseButton, RECT displayRect, Matrix transformation, ColorTransform colorTransform, Color backGroundColor, boolean useCache, double zoom) {
        SWF swf = timeline.swf;
        String key = "frame_" + frame + "_" + timeline.id + "_" + swf.hashCode() + "_" + zoom;
        SerializableImage image;
        if (useCache) {
            image = swf.getFromCache(key);
            if (image != null) {
                return image;
            }
        }

        if (timeline.getFrameCount() == 0) {
            return new SerializableImage(1, 1, SerializableImage.TYPE_INT_ARGB);
        }

        RECT rect = displayRect;
        image = new SerializableImage((int) (rect.getWidth() * zoom / SWF.unitDivisor) + 1,
                (int) (rect.getHeight() * zoom / SWF.unitDivisor) + 1, SerializableImage.TYPE_INT_ARGB);
        if (backGroundColor == null) {
            image.fillTransparent();
        } else {
            Graphics2D g = (Graphics2D) image.getBufferedImage().getGraphics();
            g.setComposite(AlphaComposite.Src);
            g.setColor(backGroundColor);
            g.fill(new Rectangle(image.getWidth(), image.getHeight()));
        }

        Matrix m = transformation.clone();
        m.translate(-rect.Xmin * zoom, -rect.Ymin * zoom);
        m.scale(zoom);
        RenderContext renderContext = new RenderContext();
        renderContext.stateUnderCursor = stateUnderCursor;
        renderContext.mouseButton = mouseButton;
        frameToImage(timeline, frame, time, renderContext, image, m, colorTransform);
        if (useCache) {
            swf.putToCache(key, image);
        }

        return image;
    }

    public static void framesToImage(Timeline timeline, List<SerializableImage> ret, int startFrame, int stopFrame, RenderContext renderContext, RECT displayRect, int totalFrameCount, Stack<Integer> visited, Matrix transformation, ColorTransform colorTransform, double zoom) {
        RECT rect = displayRect;
        for (int f = 0; f < timeline.getFrameCount(); f++) {
            SerializableImage image = new SerializableImage((int) (rect.getWidth() / SWF.unitDivisor) + 1,
                    (int) (rect.getHeight() / SWF.unitDivisor) + 1, SerializableImage.TYPE_INT_ARGB);
            image.fillTransparent();
            Matrix m = new Matrix();
            m.translate(-rect.Xmin, -rect.Ymin);
            frameToImage(timeline, f, 0, renderContext, image, m, colorTransform);
            ret.add(image);
        }
    }

    public static void frameToImage(Timeline timeline, int frame, int time, RenderContext renderContext, SerializableImage image, Matrix transformation, ColorTransform colorTransform) {
        double unzoom = SWF.unitDivisor;
        if (timeline.getFrameCount() <= frame) {
            return;
        }
        Frame frameObj = timeline.getFrame(frame);
        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setPaint(frameObj.backgroundColor.toColor());
        g.fill(new Rectangle(image.getWidth(), image.getHeight()));
        g.setTransform(transformation.toTransform());
        List<Clip> clips = new ArrayList<>();
        List<Shape> prevClips = new ArrayList<>();

        int maxDepth = timeline.getMaxDepth();
        for (int i = 1; i <= maxDepth; i++) {
            for (int c = 0; c < clips.size(); c++) {
                if (clips.get(c).depth == i) {
                    g.setClip(prevClips.get(c));
                    prevClips.remove(c);
                    clips.remove(c);
                }
            }
            if (!frameObj.layers.containsKey(i)) {
                continue;
            }
            DepthState layer = frameObj.layers.get(i);
            if (!timeline.swf.getCharacters().containsKey(layer.characterId)) {
                continue;
            }
            if (!layer.isVisible) {
                continue;
            }

            CharacterTag character = timeline.swf.getCharacter(layer.characterId);
            Matrix mat = new Matrix(layer.matrix);
            mat = mat.preConcatenate(transformation);

            if (colorTransform == null) {
                colorTransform = new ColorTransform();
            }

            ColorTransform clrTrans = colorTransform.clone();
            if (layer.colorTransForm != null && layer.blendMode <= 1) { // Normal blend mode
                clrTrans = colorTransform.merge(layer.colorTransForm);
            }

            boolean showPlaceholder = false;
            if (character instanceof DrawableTag) {
                DrawableTag drawable = (DrawableTag) character;
                Matrix drawMatrix = new Matrix();
                int drawableFrameCount = drawable.getNumFrames();
                if (drawableFrameCount == 0) {
                    drawableFrameCount = 1;
                }

                int dframe;
                if (timeline.fontFrameNum != -1) {
                    dframe = timeline.fontFrameNum;
                } else {
                    dframe = (time + layer.time) % drawableFrameCount;
                }

                if (character instanceof ButtonTag) {
                    dframe = ButtonTag.FRAME_UP;
                    if (renderContext.stateUnderCursor == layer) {
                        if (renderContext.mouseButton > 0) {
                            dframe = ButtonTag.FRAME_DOWN;
                        } else {
                            dframe = ButtonTag.FRAME_OVER;
                        }
                    }
                }

                RECT boundRect = drawable.getRect();
                ExportRectangle rect = new ExportRectangle(boundRect);
                rect = mat.transform(rect);
                Matrix m = mat.clone();
                if (layer.filters != null && layer.filters.size() > 0) {
                    // calculate size after applying the filters
                    double deltaXMax = 0;
                    double deltaYMax = 0;
                    for (FILTER filter : layer.filters) {
                        double x = filter.getDeltaX();
                        double y = filter.getDeltaY();
                        deltaXMax = Math.max(x, deltaXMax);
                        deltaYMax = Math.max(y, deltaYMax);
                    }
                    rect.xMin -= deltaXMax * unzoom;
                    rect.xMax += deltaXMax * unzoom;
                    rect.yMin -= deltaYMax * unzoom;
                    rect.yMax += deltaYMax * unzoom;
                }

                rect.xMin -= 1 * unzoom;
                rect.yMin -= 1 * unzoom;
                rect.xMin = Math.max(0, rect.xMin);
                rect.yMin = Math.max(0, rect.yMin);

                int newWidth = (int) (rect.getWidth() / unzoom);
                int newHeight = (int) (rect.getHeight() / unzoom);
                int deltaX = (int) (rect.xMin / unzoom);
                int deltaY = (int) (rect.yMin / unzoom);
                newWidth = Math.min(image.getWidth() - deltaX, newWidth) + 1;
                newHeight = Math.min(image.getHeight() - deltaY, newHeight) + 1;

                if (newWidth <= 0 || newHeight <= 0) {
                    continue;
                }

                m.translate(-rect.xMin, -rect.yMin);
                drawMatrix.translate(rect.xMin, rect.yMin);

                SerializableImage img = null;
                String cacheKey = null;
                if (drawable instanceof ShapeTag) {
                    cacheKey = ((ShapeTag) drawable).getCharacterId() + m.toString() + clrTrans.toString();
                    img = renderContext.shapeCache.get(cacheKey);
                }

                if (img == null) {
                    img = new SerializableImage(newWidth, newHeight, SerializableImage.TYPE_INT_ARGB);
                    img.fillTransparent();

                    drawable.toImage(dframe, layer.time + time, layer.ratio, renderContext, img, m, clrTrans);

                    if (cacheKey != null) {
                        renderContext.shapeCache.put(cacheKey, img);
                    }
                }

                /*//if (renderContext.stateUnderCursor == layer) {
                 if (true) {
                 BufferedImage bi = img.getBufferedImage();
                 ColorModel cm = bi.getColorModel();
                 boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
                 WritableRaster raster = bi.copyData(null);
                 img = new SerializableImage(new BufferedImage(cm, raster, isAlphaPremultiplied, null));
                 Graphics2D gg = (Graphics2D) img.getGraphics();
                 gg.setStroke(new BasicStroke(3));
                 gg.setPaint(Color.red);
                 gg.setTransform(AffineTransform.getTranslateInstance(0, 0));
                 gg.draw(SHAPERECORD.twipToPixelShape(drawable.getOutline(dframe, layer.time + time, layer.ratio, renderContext, m)));
                 }*/
                if (layer.filters != null) {
                    for (FILTER filter : layer.filters) {
                        img = filter.apply(img);
                    }
                }
                if (layer.blendMode > 1) {
                    if (layer.colorTransForm != null) {
                        img = layer.colorTransForm.apply(img);
                    }
                }

                drawMatrix.translateX /= unzoom;
                drawMatrix.translateY /= unzoom;
                AffineTransform trans = drawMatrix.toTransform();

                switch (layer.blendMode) {
                    case 0:
                    case 1:
                        g.setComposite(AlphaComposite.SrcOver);
                        break;
                    case 2: // Layer
                        g.setComposite(AlphaComposite.SrcOver);
                        break;
                    case 3:
                        g.setComposite(BlendComposite.Multiply);
                        break;
                    case 4:
                        g.setComposite(BlendComposite.Screen);
                        break;
                    case 5:
                        g.setComposite(BlendComposite.Lighten);
                        break;
                    case 6:
                        g.setComposite(BlendComposite.Darken);
                        break;
                    case 7:
                        g.setComposite(BlendComposite.Difference);
                        break;
                    case 8:
                        g.setComposite(BlendComposite.Add);
                        break;
                    case 9:
                        g.setComposite(BlendComposite.Subtract);
                        break;
                    case 10:
                        g.setComposite(BlendComposite.Invert);
                        break;
                    case 11:
                        g.setComposite(BlendComposite.Alpha);
                        break;
                    case 12:
                        g.setComposite(BlendComposite.Erase);
                        break;
                    case 13:
                        g.setComposite(BlendComposite.Overlay);
                        break;
                    case 14:
                        g.setComposite(BlendComposite.HardLight);
                        break;
                    default: // Not implemented
                        g.setComposite(AlphaComposite.SrcOver);
                        break;
                }

                if (layer.clipDepth > -1) {
                    BufferedImage mask = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
                    Graphics2D gm = (Graphics2D) mask.getGraphics();
                    gm.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    gm.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    gm.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    gm.setComposite(AlphaComposite.Src);
                    gm.setColor(new Color(0, 0, 0, 0f));
                    gm.fillRect(0, 0, image.getWidth(), image.getHeight());
                    gm.setTransform(trans);
                    gm.drawImage(img.getBufferedImage(), 0, 0, null);
                    Clip clip = new Clip(Helper.imageToShape(mask), layer.clipDepth); // Maybe we can get current outline instead converting from image (?)
                    clips.add(clip);
                    prevClips.add(g.getClip());
                    g.setTransform(AffineTransform.getTranslateInstance(0, 0));
                    g.setClip(clip.shape);
                } else {
                    g.setTransform(trans);
                    g.drawImage(img.getBufferedImage(), 0, 0, null);
                }
            } else if (character instanceof BoundedTag) {
                showPlaceholder = true;
            }

            if (showPlaceholder) {
                mat.translateX /= unzoom;
                mat.translateY /= unzoom;
                AffineTransform trans = mat.toTransform();
                g.setTransform(trans);
                BoundedTag b = (BoundedTag) character;
                g.setPaint(new Color(255, 255, 255, 128));
                g.setComposite(BlendComposite.Invert);
                RECT r = b.getRect();
                int div = (int) unzoom;
                g.drawString(character.toString(), r.Xmin / div + 3, r.Ymin / div + 15);
                g.draw(new Rectangle(r.Xmin / div, r.Ymin / div, r.getWidth() / div, r.getHeight() / div));
                g.drawLine(r.Xmin / div, r.Ymin / div, r.Xmax / div, r.Ymax / div);
                g.drawLine(r.Xmax / div, r.Ymin / div, r.Xmin / div, r.Ymax / div);
                g.setComposite(AlphaComposite.Dst);
            }
        }

        g.setTransform(AffineTransform.getScaleInstance(1, 1));
    }

    private void removeTagWithDependenciesFromTimeline(Tag toRemove, Timeline timeline) {
        Map<Integer, Integer> stage = new HashMap<>();
        Set<Integer> dependingChars = new HashSet<>();
        if (toRemove instanceof CharacterTag) {
            int characterId = ((CharacterTag) toRemove).getCharacterId();

            if (characterId != 0) {
                dependingChars.add(characterId);
                for (int i = 0; i < timeline.tags.size(); i++) {
                    Tag t = timeline.tags.get(i);
                    if (t instanceof CharacterIdTag) {
                        CharacterIdTag c = (CharacterIdTag) t;
                        Set<Integer> needed = new HashSet<>();
                        t.getNeededCharacters(needed);
                        if (needed.contains(characterId)) {
                            dependingChars.add(c.getCharacterId());
                        }
                    }
                }
            }
        }

        for (int i = 0; i < timeline.tags.size(); i++) {
            Tag t = timeline.tags.get(i);
            if (t instanceof RemoveTag) {
                RemoveTag rt = (RemoveTag) t;
                int depth = rt.getDepth();
                if (stage.containsKey(depth)) {
                    int currentCharId = stage.get(depth);
                    stage.remove(depth);
                    if (dependingChars.contains(currentCharId)) {
                        timeline.tags.remove(i);
                        i--;
                        continue;
                    }
                }
            }
            if (t instanceof PlaceObjectTypeTag) {
                PlaceObjectTypeTag po = (PlaceObjectTypeTag) t;
                int placeCharId = po.getCharacterId();
                int depth = po.getDepth();
                if (placeCharId != 0) {
                    stage.put(depth, placeCharId);
                    if (dependingChars.contains(placeCharId)) {
                        timeline.tags.remove(i);
                        i--;
                        continue;
                    }
                }
            }
            if (t instanceof CharacterIdTag) {
                CharacterIdTag c = (CharacterIdTag) t;
                if (dependingChars.contains(c.getCharacterId())) {
                    timeline.tags.remove(i);
                    i--;
                    continue;
                }
            }
            Set<Integer> needed = new HashSet<>();
            t.getNeededCharacters(needed);
            for (int dep : dependingChars) {
                if (needed.contains(dep)) {
                    timeline.tags.remove(i);
                    i--;
                    continue;
                }
            }
            if (t == toRemove) {
                timeline.tags.remove(i);
                i--;
                continue;
            }
            if (t instanceof Timelined) {
                removeTagWithDependenciesFromTimeline(toRemove, ((Timelined) t).getTimeline());
            }
        }
    }

    private boolean removeTagFromTimeline(Tag toRemove, Timeline timeline) {
        boolean modified = false;
        int characterId = -1;
        if (toRemove instanceof CharacterTag) {
            characterId = ((CharacterTag) toRemove).getCharacterId();
            modified = timeline.removeCharacter(characterId);
        }
        for (int i = 0; i < timeline.tags.size(); i++) {
            Tag t = timeline.tags.get(i);
            if (t == toRemove) {
                timeline.tags.remove(t);
                i--;
                continue;
            }

            if (toRemove instanceof CharacterTag) {
                if (t.removeCharacter(characterId)) {
                    modified = true;
                    i = -1;
                    continue;
                }
            }

            if (t instanceof DefineSpriteTag) {
                DefineSpriteTag spr = (DefineSpriteTag) t;
                boolean sprModified = removeTagFromTimeline(toRemove, spr.getTimeline());
                if (sprModified) {
                    spr.setModified(true);
                }
                modified |= sprModified;
            }
        }
        return modified;
    }

    public void removeTags(Collection<Tag> tags, boolean removeDependencies) {
        Set<Timelined> timelineds = new HashSet<>();
        for (Tag tag : tags) {
            Timelined timelined = tag.getTimelined();
            timelineds.add(timelined);
            removeTagInternal(timelined, tag, removeDependencies);
        }

        for (Timelined timelined : timelineds) {
            resetTimelines(timelined);
        }

        updateCharacters();
        clearImageCache();
    }

    public void removeTag(Tag tag, boolean removeDependencies) {
        Timelined timelined = tag.getTimelined();
        removeTagInternal(timelined, tag, removeDependencies);
        resetTimelines(timelined);
        updateCharacters();
        clearImageCache();
    }

    private void removeTagInternal(Timelined timelined, Tag tag, boolean removeDependencies) {
        if (tag instanceof ShowFrameTag || ShowFrameTag.isNestedTagType(tag.getId())) {
            List<Tag> tags;
            if (timelined instanceof DefineSpriteTag) {
                DefineSpriteTag sprite = (DefineSpriteTag) timelined;
                tags = sprite.getSubTags();
            } else {
                tags = this.tags;
            }
            tags.remove(tag);
            if (timelined instanceof DefineSpriteTag) {
                DefineSpriteTag sprite = (DefineSpriteTag) timelined;
                sprite.setModified(true);
            }
            timelined.resetTimeline();
        } else {
            // timeline should be always the swf here
            if (removeDependencies) {
                removeTagWithDependenciesFromTimeline(tag, timelined.getTimeline());
                if (timelined instanceof DefineSpriteTag) {
                    DefineSpriteTag sprite = (DefineSpriteTag) timelined;
                    sprite.setModified(true);
                }
            } else {
                removeTagFromTimeline(tag, timeline);
            }
        }
    }

    @Override
    public String toString() {
        return getShortFileName();
    }
}
