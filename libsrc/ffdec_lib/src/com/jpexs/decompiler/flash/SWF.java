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
package com.jpexs.decompiler.flash;

import SevenZip.Compression.LZMA.Decoder;
import SevenZip.Compression.LZMA.Encoder;
import com.jpacker.JPacker;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.CachedDecompilation;
import com.jpexs.decompiler.flash.abc.ClassPath;
import com.jpexs.decompiler.flash.abc.RenameType;
import com.jpexs.decompiler.flash.abc.ScriptPack;
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
import com.jpexs.decompiler.flash.dumpview.DumpInfoSwfNode;
import com.jpexs.decompiler.flash.ecma.Null;
import com.jpexs.decompiler.flash.exporters.BinaryDataExporter;
import com.jpexs.decompiler.flash.exporters.FontExporter;
import com.jpexs.decompiler.flash.exporters.ImageExporter;
import com.jpexs.decompiler.flash.exporters.MorphShapeExporter;
import com.jpexs.decompiler.flash.exporters.MovieExporter;
import com.jpexs.decompiler.flash.exporters.ShapeExporter;
import com.jpexs.decompiler.flash.exporters.SoundExporter;
import com.jpexs.decompiler.flash.exporters.TextExporter;
import com.jpexs.decompiler.flash.exporters.commonshape.ExportRectangle;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.commonshape.SVGExporter;
import com.jpexs.decompiler.flash.exporters.modes.FramesExportMode;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.exporters.script.AS2ScriptExporter;
import com.jpexs.decompiler.flash.exporters.settings.BinaryDataExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.FontExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.FramesExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.ImageExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.MorphShapeExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.MovieExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.ShapeExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.SoundExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.TextExportSettings;
import com.jpexs.decompiler.flash.exporters.shape.CanvasShapeExporter;
import com.jpexs.decompiler.flash.helpers.BMPFile;
import com.jpexs.decompiler.flash.helpers.HighlightedText;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.helpers.ImageHelper;
import com.jpexs.decompiler.flash.helpers.SWFDecompilerPlugin;
import com.jpexs.decompiler.flash.helpers.collections.MyEntry;
import com.jpexs.decompiler.flash.helpers.hilight.Highlighting;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.flash.tags.DefineBinaryDataTag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.DoInitActionTag;
import com.jpexs.decompiler.flash.tags.EndTag;
import com.jpexs.decompiler.flash.tags.ExportAssetsTag;
import com.jpexs.decompiler.flash.tags.FileAttributesTag;
import com.jpexs.decompiler.flash.tags.JPEGTablesTag;
import com.jpexs.decompiler.flash.tags.SetBackgroundColorTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.SymbolClassTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.TagStub;
import com.jpexs.decompiler.flash.tags.VideoFrameTag;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.flash.tags.base.BoundedTag;
import com.jpexs.decompiler.flash.tags.base.ButtonTag;
import com.jpexs.decompiler.flash.tags.base.CharacterIdTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.Container;
import com.jpexs.decompiler.flash.tags.base.ContainerItem;
import com.jpexs.decompiler.flash.tags.base.DrawableTag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.MorphShapeTag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.tags.base.RemoveTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.tags.base.TextTag;
import com.jpexs.decompiler.flash.timeline.Clip;
import com.jpexs.decompiler.flash.timeline.DepthState;
import com.jpexs.decompiler.flash.timeline.Frame;
import com.jpexs.decompiler.flash.timeline.SvgClip;
import com.jpexs.decompiler.flash.timeline.Timeline;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.treeitems.SWFList;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.filters.BEVELFILTER;
import com.jpexs.decompiler.flash.types.filters.BlendComposite;
import com.jpexs.decompiler.flash.types.filters.COLORMATRIXFILTER;
import com.jpexs.decompiler.flash.types.filters.CONVOLUTIONFILTER;
import com.jpexs.decompiler.flash.types.filters.DROPSHADOWFILTER;
import com.jpexs.decompiler.flash.types.filters.FILTER;
import com.jpexs.decompiler.flash.types.filters.GLOWFILTER;
import com.jpexs.decompiler.flash.types.filters.GRADIENTBEVELFILTER;
import com.jpexs.decompiler.flash.types.filters.GRADIENTGLOWFILTER;
import com.jpexs.decompiler.flash.xfl.FLAVersion;
import com.jpexs.decompiler.flash.xfl.XFLConverter;
import com.jpexs.decompiler.graph.Graph;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemContainer;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.Cache;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.NulStream;
import com.jpexs.helpers.ProgressListener;
import com.jpexs.helpers.SerializableImage;
import com.jpexs.helpers.utf8.Utf8Helper;
import gnu.jpdf.PDFJob;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import net.kroo.elliot.GifSequenceWriter;
import org.monte.media.VideoFormatKeys;
import org.monte.media.avi.AVIWriter;

/**
 * Class representing SWF file
 *
 * @author JPEXS
 */
public final class SWF implements SWFContainerItem, Timelined {

    /**
     * Default version of SWF file format
     */
    public static final int DEFAULT_VERSION = 10;
    /**
     * Tags inside of file
     */
    public List<Tag> tags = new ArrayList<>();
    public boolean hasEndTag;
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
    public long fileSize;
    /**
     * Used compression mode
     */
    public SWFCompression compression = SWFCompression.NONE;
    /**
     * Compressed size of the file (LZMA)
     */
    public long compressedSize;
    /**
     * LZMA Properties
     */
    public byte[] lzmaProperties;
    public byte[] uncompressedData;
    public byte[] originalUncompressedData;
    public FileAttributesTag fileAttributes;
    /**
     * ScaleForm GFx
     */
    public boolean gfx = false;

    public SWFList swfList;
    public String file;
    public String fileTitle;
    public boolean readOnly;
    public boolean isAS3;
    public Map<Integer, CharacterTag> characters = new HashMap<>();
    public List<ABCContainerTag> abcList;
    private JPEGTablesTag jtt;
    public Map<Integer, String> sourceFontNamesMap = new HashMap<>();
    public static final double unitDivisor = 20;
    private static final Logger logger = Logger.getLogger(SWF.class.getName());

    private Timeline timeline;

    public DumpInfoSwfNode dumpInfo;
    public DefineBinaryDataTag binaryData;

    private static Cache<String, SerializableImage> frameCache = Cache.getInstance(false, "frame");
    private final Cache<ASMSource, CachedScript> as2Cache = Cache.getInstance(true, "as2");
    private final Cache<ScriptPack, CachedDecompilation> as3Cache = Cache.getInstance(true, "as3");

    public void updateCharacters() {
        characters.clear();
        parseCharacters(new ArrayList<ContainerItem>(tags));
    }

    public int getNextCharacterId() {
        int max = -1;
        for (int characterId : characters.keySet()) {
            if (characterId > max) {
                max = characterId;
            }
        }

        return max + 1;
    }

    public synchronized JPEGTablesTag getJtt() {
        if (jtt == null) {
            for (Tag t : tags) {
                if (t instanceof JPEGTablesTag) {
                    jtt = (JPEGTablesTag) t;
                    break;
                }
            }
        }

        return jtt;
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
                        CharacterTag neededCharacter = characters.get(id);
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
        timelined.getTimeline().reset();
        for (Tag t : timelined.getTimeline().tags) {
            if (t instanceof Timelined) {
                resetTimelines((Timelined) t);
            }
        }
    }

    private void parseCharacters(List<? extends ContainerItem> list) {
        for (ContainerItem t : list) {
            if (t instanceof CharacterTag) {
                characters.put(((CharacterTag) t).getCharacterId(), (CharacterTag) t);
            }
            if (t instanceof Container) {
                parseCharacters(((Container) t).getSubItems());
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
                if (!isSpriteValid((DefineSpriteTag) t, new ArrayList<Integer>())) {
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
        saveTo(os, compression);
    }

    public String getHeaderBytes() {
        String ret = "";
        if (compression == SWFCompression.LZMA) {
            ret += 'Z';
        } else if (compression == SWFCompression.ZLIB) {
            ret += 'C';
        } else {
            if (gfx) {
                ret += 'G';
            } else {
                ret += 'F';
            }
        }
        if (gfx) {
            ret += 'F';
            ret += 'X';
        } else {
            ret += 'W';
            ret += 'S';
        }
        return ret;
    }

    /**
     * Saves this SWF into new file
     *
     * @param os OutputStream to save SWF in
     * @param compression
     * @throws IOException
     */
    public void saveTo(OutputStream os, SWFCompression compression) throws IOException {
        try {
            fixCharactersOrder(false);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            SWFOutputStream sos = new SWFOutputStream(baos, version);
            sos.writeRECT(displayRect);
            sos.writeUI8(0);
            sos.writeUI8(frameRate);
            sos.writeUI16(frameCount);

            sos.writeTags(tags);
            if (hasEndTag) {
                sos.writeUI16(0);
            }
            sos.close();
            if (compression == SWFCompression.LZMA) {
                os.write('Z');
            } else if (compression == SWFCompression.ZLIB) {
                os.write('C');
            } else {
                if (gfx) {
                    os.write('G');
                } else {
                    os.write('F');
                }
            }
            if (gfx) {
                os.write('F');
                os.write('X');
            } else {
                os.write('W');
                os.write('S');
            }
            os.write(version);
            byte[] data = baos.toByteArray();
            sos = new SWFOutputStream(os, version);
            sos.writeUI32(data.length + 8);

            if (compression == SWFCompression.LZMA) {
                Encoder enc = new Encoder();
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
                baos = new ByteArrayOutputStream();
                enc.SetEndMarkerMode(true);
                enc.Code(new ByteArrayInputStream(data), baos, -1, -1, null);
                data = baos.toByteArray();
                byte[] udata = new byte[4];
                udata[0] = (byte) (data.length & 0xFF);
                udata[1] = (byte) ((data.length >> 8) & 0xFF);
                udata[2] = (byte) ((data.length >> 16) & 0xFF);
                udata[3] = (byte) ((data.length >> 24) & 0xFF);
                os.write(udata);
                enc.WriteCoderProperties(os);
            } else if (compression == SWFCompression.ZLIB) {
                os = new DeflaterOutputStream(os);
            }
            os.write(data);
        } finally {
            if (os != null) {
                os.close();
            }
        }
    }

    public void clearModified() {
        for (Tag tag : tags) {
            if (tag.isModified()) {
                tag.createOriginalData();
                tag.setModified(false);
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            saveTo(baos, SWFCompression.NONE);
            byte[] swfData = baos.toByteArray();
            uncompressedData = swfData;
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Cannot save SWF", ex);
        }
    }

    public SWF(InputStream is, boolean parallelRead) throws IOException, InterruptedException {
        this(is, null, parallelRead, false);
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
        this(is, listener, parallelRead, false);
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
     * @param listener
     * @param parallelRead Use parallel threads?
     * @param checkOnly Check only file validity
     * @throws IOException
     * @throws java.lang.InterruptedException
     */
    public SWF(InputStream is, ProgressListener listener, boolean parallelRead, boolean checkOnly) throws IOException, InterruptedException {
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
        sis.readUI8("tmpFirstByetOfFrameRate"); //tmpFirstByetOfFrameRate
        frameRate = sis.readUI8("frameRate");
        frameCount = sis.readUI16("frameCount");
        List<Tag> tags = sis.readTagList(this, 0, parallelRead, true, !checkOnly);
        if (tags.get(tags.size() - 1).getId() == EndTag.ID) {
            hasEndTag = true;
            tags.remove(tags.size() - 1);
        }
        this.tags = tags;
        if (!checkOnly) {
            checkInvalidSprites();
            updateCharacters();
            assignExportNamesToSymbols();
            assignClassesToSymbols();
            findFileAttributes();
            findABCTags();

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

        /* preload shape tags
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

    private void findABCTags() {

        ArrayList<ABCContainerTag> newAbcList = new ArrayList<>();
        getABCTags(tags, newAbcList);
        isAS3 = (fileAttributes != null && fileAttributes.actionScript3) || (fileAttributes == null && !newAbcList.isEmpty());
        abcList = newAbcList;
    }

    private static void getABCTags(List<? extends ContainerItem> list, List<ABCContainerTag> actionScripts) {
        for (ContainerItem t : list) {
            if (t instanceof Container) {
                getABCTags(((Container) t).getSubItems(), actionScripts);
            }
            if (t instanceof ABCContainerTag) {
                actionScripts.add((ABCContainerTag) t);
            }
        }
    }

    private void findFileAttributes() {
        for (Tag t : tags) {
            if (t instanceof FileAttributesTag) {
                fileAttributes = (FileAttributesTag) t;
                break;
            }
        }
    }

    private void assignExportNamesToSymbols() {
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
            if (t instanceof CharacterIdTag) {
                CharacterIdTag ct = (CharacterIdTag) t;
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
                for (int i = 0; i < sct.tags.length; i++) {
                    if ((!classes.containsKey(sct.tags[i])) && (!classes.containsValue(sct.names[i]))) {
                        classes.put(sct.tags[i], sct.names[i]);
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
     * @return True on success
     */
    public static boolean fws2cws(InputStream fis, OutputStream fos) {
        try {
            byte[] swfHead = new byte[8];
            fis.read(swfHead);

            if (swfHead[0] != 'F') {
                fis.close();
                return false;
            }
            swfHead[0] = 'C';
            fos.write(swfHead);
            fos = new DeflaterOutputStream(fos);
            int i;
            while ((i = fis.read()) != -1) {
                fos.write(i);
            }

            fis.close();
            fos.close();
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

    private static SWFHeader decompress(InputStream is, OutputStream os, boolean allowUncompressed) throws IOException {
        byte[] hdr = new byte[8];

        // SWFheader: signature, version and fileSize
        if (is.read(hdr) != 8) {
            throw new IOException("SWF header is too short");
        }

        String signature = new String(hdr, 0, 3, Utf8Helper.charset);
        if (!Arrays.asList(
                "FWS", //Uncompressed Flash
                "CWS", //ZLib compressed Flash
                "ZWS", //LZMA compressed Flash
                "GFX", //Uncompressed ScaleForm GFx
                "CFX" //Compressed ScaleForm GFx
        ).contains(signature)) {
            throw new IOException("Invalid SWF file");
        }

        int version = hdr[3];
        SWFInputStream sis = new SWFInputStream(null, Arrays.copyOfRange(hdr, 4, 8), 4, 4);
        long fileSize = sis.readUI32("fileSize");
        SWFHeader header = new SWFHeader();
        header.version = version;
        header.fileSize = fileSize;

        if (hdr[1] == 'F' && hdr[2] == 'X') {
            header.gfx = true;
        }

        try (SWFOutputStream sos = new SWFOutputStream(os, version)) {
            sos.write(Utf8Helper.getBytes("FWS"));
            sos.writeUI8(version);
            sos.writeUI32(fileSize);

            switch (hdr[0]) {
                case 'C': { // CWS, CFX
                    Helper.copyStream(new InflaterInputStream(is), os, fileSize - 8);
                    header.compression = SWFCompression.ZLIB;
                    break;
                }
                case 'Z': { // ZWS                   
                    byte lzmaprop[] = new byte[9];
                    is.read(lzmaprop);
                    sis = new SWFInputStream(null, lzmaprop);
                    sis.readUI32("LZMAsize"); // compressed LZMA data size = compressed SWF - 17 byte,
                    // where 17 = 8 byte header + this 4 byte + 5 bytes decoder properties
                    int propertiesSize = 5;
                    byte[] lzmaProperties = sis.readBytes(propertiesSize, "lzmaproperties");
                    if (lzmaProperties.length != propertiesSize) {
                        throw new IOException("LZMA:input .lzma file is too short");
                    }
                    Decoder decoder = new Decoder();
                    if (!decoder.SetDecoderProperties(lzmaProperties)) {
                        throw new IOException("LZMA:Incorrect stream properties");
                    }
                    if (!decoder.Code(is, os, fileSize - 8)) {
                        throw new IOException("LZMA:Error in data stream");
                    }

                    header.compression = SWFCompression.LZMA;
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

    public boolean exportAS3Class(String className, String outdir, ScriptExportMode exportMode, boolean parallel) throws Exception {
        boolean exported = false;

        for (int i = 0; i < abcList.size(); i++) {
            ABC abc = abcList.get(i).getABC();
            List<ScriptPack> scrs = abc.findScriptPacksByPath(className);
            for (int j = 0; j < scrs.size(); j++) {
                ScriptPack scr = scrs.get(j);
                String cnt = "";
                if (scrs.size() > 1) {
                    cnt = "script " + (j + 1) + "/" + scrs.size() + " ";
                }
                String exStr = "Exporting " + "tag " + (i + 1) + "/" + abcList.size() + " " + cnt + scr.getPath() + " ...";
                informListeners("exporting", exStr);
                scr.export(outdir, abcList, exportMode, parallel);
                exStr = "Exported " + "tag " + (i + 1) + "/" + abcList.size() + " " + cnt + scr.getPath() + " ...";
                informListeners("exported", exStr);
                exported = true;
            }
        }
        return exported;
    }

    private List<MyEntry<ClassPath, ScriptPack>> uniqueAS3Packs(List<MyEntry<ClassPath, ScriptPack>> packs) {
        List<MyEntry<ClassPath, ScriptPack>> ret = new ArrayList<>();
        for (MyEntry<ClassPath, ScriptPack> item : packs) {
            for (MyEntry<ClassPath, ScriptPack> itemOld : ret) {
                if (item.getKey().equals(itemOld.getKey())) {
                    logger.log(Level.SEVERE, "Duplicate pack path found (" + itemOld.getKey() + ")!");
                    break;
                }
            }
            ret.add(item);
        }
        return ret;
    }

    public List<MyEntry<ClassPath, ScriptPack>> getAS3Packs() {
        List<MyEntry<ClassPath, ScriptPack>> packs = new ArrayList<>();
        for (ABCContainerTag abcTag : abcList) {
            packs.addAll(abcTag.getABC().getScriptPacks());
        }
        return uniqueAS3Packs(packs);
    }

    @Override
    public RECT getRect(Set<BoundedTag> added) {
        return displayRect;
    }

    private class ExportPackTask implements Callable<File> {

        ScriptPack pack;
        String directory;
        List<ABCContainerTag> abcList;
        ScriptExportMode exportMode;
        ClassPath path;
        AtomicInteger index;
        int count;
        boolean parallel;
        AbortRetryIgnoreHandler handler;
        long startTime;
        long stopTime;

        public ExportPackTask(AbortRetryIgnoreHandler handler, AtomicInteger index, int count, ClassPath path, ScriptPack pack, String directory, List<ABCContainerTag> abcList, ScriptExportMode exportMode, boolean parallel) {
            this.pack = pack;
            this.directory = directory;
            this.abcList = abcList;
            this.exportMode = exportMode;
            this.path = path;
            this.index = index;
            this.count = count;
            this.parallel = parallel;
            this.handler = handler;
        }

        @Override
        public File call() throws Exception {
            RunnableIOExResult<File> rio = new RunnableIOExResult<File>() {
                @Override
                public void run() throws IOException {
                    startTime = System.currentTimeMillis();
                    this.result = pack.export(directory, abcList, exportMode, parallel);
                    stopTime = System.currentTimeMillis();
                }
            };
            int currentIndex = index.getAndIncrement();
            synchronized (ABC.class) {
                informListeners("exporting", "Exporting script " + currentIndex + "/" + count + " " + path);
            }
            new RetryTask(rio, handler).run();
            synchronized (ABC.class) {
                long time = stopTime - startTime;
                informListeners("exported", "Exported script " + currentIndex + "/" + count + " " + path + ", " + Helper.formatTimeSec(time));
            }
            return rio.result;
        }
    }

    private List<File> exportActionScript2(AbortRetryIgnoreHandler handler, String outdir, ScriptExportMode exportMode, boolean parallel, EventListener evl) throws IOException {
        List<File> ret = new ArrayList<>();
        Map<String, ASMSource> asms = getASMs();

        if (!outdir.endsWith(File.separator)) {
            outdir += File.separator;
        }
        outdir += "scripts" + File.separator;
        ret.addAll(new AS2ScriptExporter().exportAS2ScriptsTimeout(handler, outdir, asms.values(), exportMode, evl));
        return ret;
    }

    private List<File> exportActionScript3(final AbortRetryIgnoreHandler handler, final String outdir, final ScriptExportMode exportMode, final boolean parallel) {
        final AtomicInteger cnt = new AtomicInteger(1);

        final List<File> ret = new ArrayList<>();
        final List<MyEntry<ClassPath, ScriptPack>> packs = getAS3Packs();

        if (!parallel || packs.size() < 2) {
            try {
                CancellableWorker.call(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        for (MyEntry<ClassPath, ScriptPack> item : packs) {
                            ExportPackTask task = new ExportPackTask(handler, cnt, packs.size(), item.getKey(), item.getValue(), outdir, abcList, exportMode, parallel);
                            ret.add(task.call());
                        }
                        return null;
                    }
                }, Configuration.exportTimeout.get(), TimeUnit.SECONDS);
            } catch (TimeoutException ex) {
                logger.log(Level.SEVERE, Helper.formatTimeToText(Configuration.exportTimeout.get()) + " ActionScript export limit reached", ex);
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Error during ABC export", ex);
            }
        } else {
            ExecutorService executor = Executors.newFixedThreadPool(Configuration.parallelThreadCount.get());
            List<Future<File>> futureResults = new ArrayList<>();
            for (MyEntry<ClassPath, ScriptPack> item : packs) {
                Future<File> future = executor.submit(new ExportPackTask(handler, cnt, packs.size(), item.getKey(), item.getValue(), outdir, abcList, exportMode, parallel));
                futureResults.add(future);
            }

            try {
                executor.shutdown();
                if (!executor.awaitTermination(Configuration.exportTimeout.get(), TimeUnit.SECONDS)) {
                    logger.log(Level.SEVERE, Helper.formatTimeToText(Configuration.exportTimeout.get()) + " ActionScript export limit reached");
                }
            } catch (InterruptedException ex) {
            } finally {
                executor.shutdownNow();
            }

            for (int f = 0; f < futureResults.size(); f++) {
                try {
                    if (futureResults.get(f).isDone()) {
                        ret.add(futureResults.get(f).get());
                    }
                } catch (InterruptedException ex) {
                } catch (ExecutionException ex) {
                    logger.log(Level.SEVERE, "Error during ABC export", ex);
                }
            }
        }

        return ret;
    }

    public List<File> exportActionScript(AbortRetryIgnoreHandler handler, String outdir, ScriptExportMode exportMode, boolean parallel) throws Exception {
        List<File> ret = new ArrayList<>();
        final EventListener evl = new EventListener() {
            @Override
            public void handleEvent(String event, Object data) {
                if (event.equals("exporting") || event.equals("exported")) {
                    informListeners(event, data);
                }
            }
        };

        if (isAS3) {
            ret.addAll(exportActionScript3(handler, outdir, exportMode, parallel));
        } else {
            ret.addAll(exportActionScript2(handler, outdir, exportMode, parallel, evl));
        }
        return ret;
    }

    public Map<String, ASMSource> getASMs() {
        Map<String, ASMSource> asms = new HashMap<>();
        getASMs("", tags, asms);
        return asms;
    }

    private static void getASMs(String path, List<? extends ContainerItem> items, Map<String, ASMSource> asms) {
        for (ContainerItem item : items) {
            String subPath = path + "/" + item.toString();
            if (item instanceof ASMSource) {
                String npath = subPath;
                int ppos = 1;
                while (asms.containsKey(npath)) {
                    ppos++;
                    npath = subPath + "[" + ppos + "]";
                }
                asms.put(npath, (ASMSource) item);
            }
            if (item instanceof Container) {
                getASMs(subPath, ((Container) item).getSubItems(), asms);
            }
        }
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

    public static boolean hasErrorHeader(byte[] data) {
        return hasErrorHeader(new ByteArrayRange(data));
    }

    public static boolean hasErrorHeader(ByteArrayRange data) {
        if (data.getLength() > 4) {
            if ((data.get(0) & 0xff) == 0xff && (data.get(1) & 0xff) == 0xd9
                    && (data.get(2) & 0xff) == 0xff && (data.get(3) & 0xff) == 0xd8) {
                return true;
            }
        }
        return false;
    }

    public static void populateVideoFrames(int streamId, List<? extends ContainerItem> tags, HashMap<Integer, VideoFrameTag> output) {
        for (ContainerItem t : tags) {
            if (t instanceof VideoFrameTag) {
                output.put(((VideoFrameTag) t).frameNum, (VideoFrameTag) t);
            }
            if (t instanceof Container) {
                populateVideoFrames(streamId, ((Container) t).getSubItems(), output);
            }
        }
    }

    public void exportMovies(AbortRetryIgnoreHandler handler, String outdir, MovieExportSettings settings) throws IOException {
        new MovieExporter().exportMovies(handler, outdir, tags, settings);
    }

    public void exportSounds(AbortRetryIgnoreHandler handler, String outdir, SoundExportSettings settings) throws IOException {
        new SoundExporter().exportSounds(handler, outdir, tags, settings);
    }

    public void exportFonts(AbortRetryIgnoreHandler handler, String outdir, FontExportSettings settings) throws IOException {
        new FontExporter().exportFonts(handler, outdir, tags, settings);
    }

    private static void writeLE(OutputStream os, long val, int size) throws IOException {
        for (int i = 0; i < size; i++) {
            os.write((int) (val & 0xff));
            val >>= 8;
        }
    }

    public static void createWavFromPcmData(OutputStream fos, int soundRateHz, boolean soundSize, boolean soundType, byte[] data) throws IOException {
        ByteArrayOutputStream subChunk1Data = new ByteArrayOutputStream();
        int audioFormat = 1; //PCM
        writeLE(subChunk1Data, audioFormat, 2);
        int numChannels = soundType ? 2 : 1;
        writeLE(subChunk1Data, numChannels, 2);
        int[] rateMap = {5512, 11025, 22050, 44100};
        int sampleRate = soundRateHz;//rateMap[soundRate];
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

    private static void makeAVI(Iterator<BufferedImage> images, int frameRate, File file) throws IOException {
        if (!images.hasNext()) {
            return;
        }
        AVIWriter out = new AVIWriter(file);
        BufferedImage img0 = images.next();
        out.addVideoTrack(VideoFormatKeys.ENCODING_AVI_PNG, 1, frameRate, img0.getWidth(), img0.getHeight(), 0, 0);
        try {
            out.write(0, img0, 1);
            while (images.hasNext()) {
                out.write(0, images.next(), 1);
            }
        } finally {
            out.close();
        }

    }

    private static void makeGIF(Iterator<BufferedImage> images, int frameRate, File file) throws IOException {
        if (!images.hasNext()) {
            return;
        }
        try (ImageOutputStream output = new FileImageOutputStream(file)) {
            BufferedImage img0 = images.next();
            GifSequenceWriter writer = new GifSequenceWriter(output, img0.getType(), 1000 / frameRate, true);
            writer.writeToSequence(img0);

            while (images.hasNext()) {
                writer.writeToSequence(images.next());
            }

            writer.close();
        }
    }

    private static String getTypePrefix(CharacterTag c) {
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
            CharacterTag ch = fswf.characters.get(c);
            if (ch instanceof FontTag) {
                fos.write(Utf8Helper.getBytes("function " + getTypePrefix(ch) + c + "(ctx,ch,textColor){\r\n"));
                fos.write(Utf8Helper.getBytes(((FontTag) ch).toHtmlCanvas(1)));
                fos.write(Utf8Helper.getBytes("}\r\n\r\n"));
            } else {

                if (ch instanceof ImageTag) {
                    ImageTag image = (ImageTag) ch;
                    String format = image.getImageFormat();
                    InputStream imageStream = image.getImageData();
                    byte[] imageData;
                    if (imageStream != null) {
                        imageData = Helper.readStream(image.getImageData());
                    } else {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ImageHelper.write(image.getImage().getBufferedImage(), format.toUpperCase(Locale.ENGLISH), baos);
                        imageData = baos.toByteArray();
                    }
                    String base64ImgData = Helper.byteArrayToBase64String(imageData);
                    fos.write(Utf8Helper.getBytes("var imageObj" + c + " = document.createElement(\"img\");\r\nimageObj" + c + ".src=\"data:image/" + format + ";base64," + base64ImgData + "\";\r\n"));
                }
                fos.write(Utf8Helper.getBytes("function " + getTypePrefix(ch) + c + "(ctx,ctrans,frame,ratio,time){\r\n"));
                if (ch instanceof DrawableTag) {
                    fos.write(Utf8Helper.getBytes(((DrawableTag) ch).toHtmlCanvas(1)));
                }
                fos.write(Utf8Helper.getBytes("}\r\n\r\n"));
            }
        }
    }

    public List<File> exportFrames(AbortRetryIgnoreHandler handler, String outdir, int containerId, List<Integer> frames, final FramesExportSettings settings) throws IOException {
        final List<File> ret = new ArrayList<>();
        if (tags.isEmpty()) {
            return ret;
        }
        Timeline tim = null;
        String path = "";
        if (containerId == 0) {
            tim = getTimeline();
        } else {
            tim = ((Timelined) characters.get(containerId)).getTimeline();
            path = File.separator + Helper.makeFileName(characters.get(containerId).getExportFileName());
        }
        if (frames == null) {
            int frameCnt = tim.getFrames().size();
            frames = new ArrayList<>();
            for (int i = 0; i < frameCnt; i++) {
                frames.add(i);
            }
        }

        final File foutdir = new File(outdir + path);
        if (!foutdir.exists()) {
            if (!foutdir.mkdirs()) {
                if (!foutdir.exists()) {
                    throw new IOException("Cannot create directory " + outdir);
                }
            }
        }

        final List<Integer> fframes = frames;

        Color backgroundColor = null;
        if (settings.mode == FramesExportMode.AVI) {
            for (Tag t : tags) {
                if (t instanceof SetBackgroundColorTag) {
                    SetBackgroundColorTag sb = (SetBackgroundColorTag) t;
                    backgroundColor = sb.backgroundColor.toColor();
                }
            }
        }

        if (settings.mode == FramesExportMode.SVG) {
            for (int i = 0; i < frames.size(); i++) {
                final int fi = i;
                final Timeline ftim = tim;
                final Color fbackgroundColor = backgroundColor;
                new RetryTask(new RunnableIOEx() {
                    @Override
                    public void run() throws IOException {
                        int frame = fframes.get(fi);
                        File f = new File(foutdir + File.separator + frame + ".svg");
                        try (FileOutputStream fos = new FileOutputStream(f)) {
                            ExportRectangle rect = new ExportRectangle(ftim.displayRect);
                            rect.xMax *= settings.zoom;
                            rect.yMax *= settings.zoom;
                            rect.xMin *= settings.zoom;
                            rect.yMin *= settings.zoom;
                            SVGExporter exporter = new SVGExporter(rect);
                            if (fbackgroundColor != null) {
                                exporter.setBackGroundColor(fbackgroundColor);
                            }
                            frameToSvg(ftim, frame, 0, null, 0, exporter, new ColorTransform(), 0, settings.zoom);
                            fos.write(Utf8Helper.getBytes(exporter.getSVG()));
                        }
                        ret.add(f);
                    }
                }, handler).run();
            }
            return ret;
        }

        if (settings.mode == FramesExportMode.CANVAS) {
            final Timeline ftim = tim;
            final Color fbackgroundColor = backgroundColor;
            final SWF fswf = this;
            new RetryTask(new RunnableIOEx() {
                @Override
                public void run() throws IOException {
                    File fcanvas = new File(foutdir + File.separator + "canvas.js");
                    Helper.saveStream(SWF.class.getClassLoader().getResourceAsStream("com/jpexs/helpers/resource/canvas.js"), fcanvas);
                    ret.add(fcanvas);

                    File f = new File(foutdir + File.separator + "frames.js");
                    File fmin = new File(foutdir + File.separator + "frames.min.js");
                    int width = (int) (ftim.displayRect.getWidth() * settings.zoom / SWF.unitDivisor);
                    int height = (int) (ftim.displayRect.getHeight() * settings.zoom / SWF.unitDivisor);
                    try (FileOutputStream fos = new FileOutputStream(f)) {
                        fos.write(Utf8Helper.getBytes("\r\n"));
                        Set<Integer> library = new HashSet<>();
                        ftim.getNeededCharacters(fframes, library);

                        writeLibrary(fswf, library, fos);

                        String currentName = ftim.id == 0 ? "main" : getTypePrefix(fswf.characters.get(ftim.id)) + ftim.id;

                        fos.write(Utf8Helper.getBytes("function " + currentName + "(ctx,ctrans,frame,ratio,time){\r\n"));
                        fos.write(Utf8Helper.getBytes("\tctx.save();\r\n"));
                        fos.write(Utf8Helper.getBytes("\tctx.transform(1,0,0,1," + (-ftim.displayRect.Xmin * settings.zoom / unitDivisor) + "," + (-ftim.displayRect.Ymin * settings.zoom / unitDivisor) + ");\r\n"));
                        fos.write(Utf8Helper.getBytes(framesToHtmlCanvas(unitDivisor / settings.zoom, ftim, fframes, 0, null, 0, ftim.displayRect, new ColorTransform(), fbackgroundColor)));
                        fos.write(Utf8Helper.getBytes("\tctx.restore();\r\n"));
                        fos.write(Utf8Helper.getBytes("}\r\n\r\n"));

                        fos.write(Utf8Helper.getBytes("var frame = -1;\r\n"));
                        fos.write(Utf8Helper.getBytes("var time = 0;\r\n"));
                        fos.write(Utf8Helper.getBytes("var frames = [];\r\n"));
                        for (int i : fframes) {
                            fos.write(Utf8Helper.getBytes("frames.push(" + i + ");\r\n"));
                        }
                        fos.write(Utf8Helper.getBytes("\r\n"));
                        RGB backgroundColor = new RGB(255, 255, 255);
                        for (Tag t : fswf.tags) {
                            if (t instanceof SetBackgroundColorTag) {
                                SetBackgroundColorTag sb = (SetBackgroundColorTag) t;
                                backgroundColor = sb.backgroundColor;
                            }
                        }

                        fos.write(Utf8Helper.getBytes("var backgroundColor = \"" + backgroundColor.toHexRGB() + "\";\r\n"));
                        fos.write(Utf8Helper.getBytes("var originalWidth = " + width + ";\r\n"));
                        fos.write(Utf8Helper.getBytes("var originalHeight= " + height + ";\r\n"));
                        fos.write(Utf8Helper.getBytes("function nextFrame(ctx,ctrans){\r\n"));
                        fos.write(Utf8Helper.getBytes("\tvar oldframe = frame;\r\n"));
                        fos.write(Utf8Helper.getBytes("\tframe = (frame+1)%frames.length;\r\n"));
                        fos.write(Utf8Helper.getBytes("\tif(frame==oldframe){time++;}else{time=0;};\r\n"));
                        fos.write(Utf8Helper.getBytes("\tdrawFrame();\r\n"));
                        fos.write(Utf8Helper.getBytes("}\r\n\r\n"));

                        fos.write(Utf8Helper.getBytes("function drawFrame(){\r\n"));
                        fos.write(Utf8Helper.getBytes("\tctx.fillStyle = backgroundColor;\r\n"));
                        fos.write(Utf8Helper.getBytes("\tctx.fillRect(0,0,canvas.width,canvas.height);\r\n"));
                        fos.write(Utf8Helper.getBytes("\tctx.save();\r\n"));
                        fos.write(Utf8Helper.getBytes("\tctx.transform(canvas.width/originalWidth,0,0,canvas.height/originalHeight,0,0);\r\n"));
                        fos.write(Utf8Helper.getBytes("\t" + currentName + "(ctx,ctrans,frames[frame],0,time);\r\n"));
                        fos.write(Utf8Helper.getBytes("\tctx.restore();\r\n"));
                        fos.write(Utf8Helper.getBytes("}\r\n\r\n"));
                        if (ftim.swf.frameRate > 0) {
                            fos.write(Utf8Helper.getBytes("window.setInterval(function(){nextFrame(ctx,ctrans);}," + (int) (1000.0 / ftim.swf.frameRate) + ");\r\n"));
                        }
                        fos.write(Utf8Helper.getBytes("nextFrame(ctx,ctrans);\r\n"));
                    }

                    boolean packed = false;
                    if (Configuration.packJavaScripts.get()) {
                        try {
                            JPacker.main(new String[]{"-q", "-b", "62", "-o", fmin.getAbsolutePath(), f.getAbsolutePath()});
                            f.delete();
                            packed = true;
                        } catch (Exception | Error e) { //Something wrong in the packer
                            logger.log(Level.WARNING, "JPacker: Cannot minimize script");
                            f.renameTo(fmin);
                        }
                    } else {
                        f.renameTo(fmin);
                    }

                    File fh = new File(foutdir + File.separator + "frames.html");
                    try (FileOutputStream fos = new FileOutputStream(fh); FileInputStream fis = new FileInputStream(fmin)) {
                        fos.write(Utf8Helper.getBytes(CanvasShapeExporter.getHtmlPrefix(width, height)));
                        fos.write(Utf8Helper.getBytes(CanvasShapeExporter.getJsPrefix()));
                        byte buf[] = new byte[1000];
                        int cnt;
                        while ((cnt = fis.read(buf)) > 0) {
                            fos.write(buf, 0, cnt);
                        }
                        if (packed) {
                            fos.write(Utf8Helper.getBytes(";"));
                        }
                        fos.write(Utf8Helper.getBytes(CanvasShapeExporter.getJsSuffix()));
                        fos.write(Utf8Helper.getBytes(CanvasShapeExporter.getHtmlSuffix()));
                    }
                    fmin.delete();

                    ret.add(f);
                }
            }, handler).run();

            return ret;
        }

        final Timeline ftim = tim;
        final Color fbackgroundColor = backgroundColor;
        final Iterator<BufferedImage> frameImages = new Iterator<BufferedImage>() {

            private int pos = 0;

            @Override
            public boolean hasNext() {
                return fframes.size() > pos;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

            @Override
            public BufferedImage next() {
                if (!hasNext()) {
                    return null;
                }
                return frameToImageGet(ftim, fframes.get(pos++), 0, null, 0, ftim.displayRect, new Matrix(), new ColorTransform(), fbackgroundColor, false, settings.zoom).getBufferedImage();
            }
        };

        switch (settings.mode) {
            case GIF:
                new RetryTask(new RunnableIOEx() {
                    @Override
                    public void run() throws IOException {
                        File f = new File(foutdir + File.separator + "frames.gif");
                        makeGIF(frameImages, frameRate, f);
                        ret.add(f);
                    }
                }, handler).run();
                break;
            case BMP:
                for (int i = 0; frameImages.hasNext(); i++) {
                    final int fi = i;
                    new RetryTask(new RunnableIOEx() {
                        @Override
                        public void run() throws IOException {
                            File f = new File(foutdir + File.separator + (fframes.get(fi) + 1) + ".bmp");
                            BMPFile.saveBitmap(frameImages.next(), f);
                            ret.add(f);
                        }
                    }, handler).run();
                }
                break;
            case PNG:
                for (int i = 0; frameImages.hasNext(); i++) {
                    final int fi = i;
                    new RetryTask(new RunnableIOEx() {
                        @Override
                        public void run() throws IOException {
                            File f = new File(foutdir + File.separator + (fframes.get(fi) + 1) + ".png");
                            ImageHelper.write(frameImages.next(), "PNG", new FileOutputStream(f));
                            ret.add(f);
                        }
                    }, handler).run();
                }
                break;
            case PDF:
                new RetryTask(new RunnableIOEx() {
                    @Override
                    public void run() throws IOException {
                        File f = new File(foutdir + File.separator + "frames.pdf");
                        PDFJob job = new PDFJob(new FileOutputStream(f));
                        PageFormat pf = new PageFormat();
                        pf.setOrientation(PageFormat.PORTRAIT);
                        Paper p = new Paper();
                        BufferedImage img0 = frameImages.next();
                        p.setSize(img0.getWidth() + 10, img0.getHeight() + 10);
                        pf.setPaper(p);

                        for (int i = 0; frameImages.hasNext(); i++) {
                            BufferedImage img = frameImages.next();
                            Graphics g = job.getGraphics(pf);
                            g.drawImage(img, 5, 5, img.getWidth(), img.getHeight(), null);
                            g.dispose();
                        }

                        job.end();
                        ret.add(f);
                    }
                }, handler).run();
                break;
            case AVI:
                new RetryTask(new RunnableIOEx() {
                    @Override
                    public void run() throws IOException {
                        File f = new File(foutdir + File.separator + "frames.avi");
                        makeAVI(frameImages, frameRate, f);
                        ret.add(f);
                    }
                }, handler).run();
                break;
        }

        return ret;
    }

    public void exportTexts(AbortRetryIgnoreHandler handler, String outdir, TextExportSettings settings) throws IOException {
        new TextExporter().exportTexts(handler, outdir, tags, settings);
    }

    public void exportImages(AbortRetryIgnoreHandler handler, String outdir, ImageExportSettings settings) throws IOException {
        new ImageExporter().exportImages(handler, outdir, tags, settings);
    }

    public void exportShapes(AbortRetryIgnoreHandler handler, String outdir, ShapeExportSettings settings) throws IOException {
        new ShapeExporter().exportShapes(handler, outdir, tags, settings);
    }

    public void exportMorphShapes(AbortRetryIgnoreHandler handler, String outdir, MorphShapeExportSettings settings) throws IOException {
        new MorphShapeExporter().exportMorphShapes(handler, outdir, tags, settings);
    }

    public void exportBinaryData(AbortRetryIgnoreHandler handler, String outdir, BinaryDataExportSettings settings) throws IOException {
        new BinaryDataExporter().exportBinaryData(handler, outdir, tags, settings);
    }

    private final HashMap<String, String> deobfuscated = new HashMap<>();
    private List<MyEntry<DirectValueActionItem, ConstantPool>> allVariableNames = new ArrayList<>();
    private List<GraphSourceItem> allFunctions = new ArrayList<>();
    private HashMap<DirectValueActionItem, ConstantPool> allStrings = new HashMap<>();
    private final HashMap<DirectValueActionItem, String> usageTypes = new HashMap<>();
    private final IdentifiersDeobfuscation deobfuscation = new IdentifiersDeobfuscation();

    private static void getVariables(ConstantPool constantPool, BaseLocalData localData, TranslateStack stack, List<GraphTargetItem> output, ActionGraphSource code, int ip, List<MyEntry<DirectValueActionItem, ConstantPool>> variables, List<GraphSourceItem> functions, HashMap<DirectValueActionItem, ConstantPool> strings, List<Integer> visited, HashMap<DirectValueActionItem, String> usageTypes, String path) throws InterruptedException {
        boolean debugMode = false;
        while ((ip > -1) && ip < code.size()) {
            if (visited.contains(ip)) {
                break;
            }
            GraphSourceItem ins = code.get(ip);

            if (debugMode) {
                System.err.println("Visit " + ip + ": ofs" + Helper.formatAddress(((Action) ins).getAddress()) + ":" + ((Action) ins).getASMSource(new ActionList(), new HashSet<Long>(), ScriptExportMode.PCODE) + " stack:" + Helper.stackToString(stack, LocalData.create(new ConstantPool())));
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
                usageType = "function"; //can there be method?
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
                    getVariables(variables, functions, strings, usageTypes, new ActionGraphSource(code.getActions().subList(ip, nextip), code.version, new HashMap<Integer, String>(), new HashMap<String, GraphTargetItem>(), new HashMap<String, GraphTargetItem>()), 0, path + (cntName == null ? "" : "/" + cntName));
                    ip = nextip;
                }
                List<List<GraphTargetItem>> r = new ArrayList<>();
                r.add(new ArrayList<GraphTargetItem>());
                r.add(new ArrayList<GraphTargetItem>());
                r.add(new ArrayList<GraphTargetItem>());
                try {
                    ((GraphSourceItemContainer) ins).translateContainer(r, stack, output, new HashMap<Integer, String>(), new HashMap<String, GraphTargetItem>(), new HashMap<String, GraphTargetItem>());
                } catch (EmptyStackException ex) {
                }
                //ip++;
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

            //for..in return
            if (((ins instanceof ActionEquals) || (ins instanceof ActionEquals2)) && (stack.size() == 1) && (stack.peek() instanceof DirectValueActionItem)) {
                stack.push(new DirectValueActionItem(null, 0, new Null(), new ArrayList<String>()));
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

    private static void getVariables(List<MyEntry<DirectValueActionItem, ConstantPool>> variables, List<GraphSourceItem> functions, HashMap<DirectValueActionItem, ConstantPool> strings, HashMap<DirectValueActionItem, String> usageType, ActionGraphSource code, int addr, String path) throws InterruptedException {
        ActionLocalData localData = new ActionLocalData();
        getVariables(null, localData, new TranslateStack(), new ArrayList<GraphTargetItem>(), code, code.adr2pos(addr), variables, functions, strings, new ArrayList<Integer>(), usageType, path);
    }

    private List<MyEntry<DirectValueActionItem, ConstantPool>> getVariables(List<MyEntry<DirectValueActionItem, ConstantPool>> variables, List<GraphSourceItem> functions, HashMap<DirectValueActionItem, ConstantPool> strings, HashMap<DirectValueActionItem, String> usageType, ASMSource src, String path) throws InterruptedException {
        List<MyEntry<DirectValueActionItem, ConstantPool>> ret = new ArrayList<>();
        ActionList actions = src.getActions();
        actionsMap.put(src, actions);
        getVariables(variables, functions, strings, usageType, new ActionGraphSource(actions, version, new HashMap<Integer, String>(), new HashMap<String, GraphTargetItem>(), new HashMap<String, GraphTargetItem>()), 0, path);
        return ret;
    }
    private HashMap<ASMSource, ActionList> actionsMap = new HashMap<>();

    private void getVariables(List<? extends ContainerItem> objs, String path) throws InterruptedException {
        List<String> processed = new ArrayList<>();
        for (ContainerItem o : objs) {
            if (o instanceof ASMSource) {
                String infPath = path + "/" + o.toString();
                int pos = 1;
                String infPath2 = infPath;
                while (processed.contains(infPath2)) {
                    pos++;
                    infPath2 = infPath + "[" + pos + "]";
                }
                processed.add(infPath2);
                informListeners("getVariables", infPath2);
                getVariables(allVariableNames, allFunctions, allStrings, usageTypes, (ASMSource) o, path);
            }
            if (o instanceof Container) {
                getVariables(((Container) o).getSubItems(), path + "/" + o.toString());
            }
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
                for (int i = 0; i < sc.names.length; i++) {
                    String newname = deobfuscation.deobfuscateNameWithPackage(true, sc.names[i], deobfuscated, renameType, deobfuscated);
                    if (newname != null) {
                        sc.names[i] = newname;
                    }
                }
                sc.setModified(true);
            }
        }
        deobfuscation.deobfuscateInstanceNames(true, deobfuscated, renameType, tags, new HashMap<String, String>());
        return deobfuscated.size();
    }

    public int deobfuscateIdentifiers(RenameType renameType) throws InterruptedException {
        findFileAttributes();
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
        actionsMap = new HashMap<>();
        allFunctions = new ArrayList<>();
        allVariableNames = new ArrayList<>();
        allStrings = new HashMap<>();

        int ret = 0;
        getVariables(tags, "");
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
                String exportName = dia.getExportName();
                final String pkgPrefix = "__Packages.";
                String[] classNameParts = null;
                if ((exportName != null) && exportName.startsWith(pkgPrefix)) {
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
                if (f.functionName.isEmpty()) { //anonymous function, leave as is
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
                if (f.functionName.isEmpty()) { //anonymous function, leave as is
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

    public static SerializableImage getFromCache(String key) {
        if (frameCache.contains(key)) {
            return SWF.frameCache.get(key);
        }
        return null;
    }

    public static void putToCache(String key, SerializableImage img) {
        if (Configuration.useFrameCache.get()) {
            frameCache.put(key, img);
        }
    }

    public void clearImageCache() {
        frameCache.clear();
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
        clearImageCache();
        clearScriptCache();
        Cache.clearAll();
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
        pack.toSource(writer, swf.abcList, script.traits.traits, ScriptExportMode.AS, parallel);
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

    private static String jsArrColor(RGB rgb) {
        return "[" + rgb.red + "," + rgb.green + "," + rgb.blue + "," + ((rgb instanceof RGBA) ? ((RGBA) rgb).getAlphaFloat() : 1) + "]";
    }

    public static String framesToHtmlCanvas(double unitDivisor, Timeline timeline, List<Integer> frames, int time, DepthState stateUnderCursor, int mouseButton, RECT displayRect, ColorTransform colorTransform, Color backGroundColor) {
        StringBuilder sb = new StringBuilder();
        if (frames == null) {
            frames = new ArrayList<>();
            for (int i = 0; i < timeline.getFrameCount(); i++) {
                frames.add(i);
            }
        }
        sb.append("\tvar clips = [];\r\n");
        sb.append("\tvar frame_cnt = ").append(timeline.getFrameCount()).append(";\r\n");
        sb.append("\tframe = frame % frame_cnt;\r\n");
        sb.append("\tswitch(frame){\r\n");
        int maxDepth = timeline.getMaxDepth();
        Stack<Integer> clipDepths = new Stack<>();
        for (int frame : frames) {
            sb.append("\t\tcase ").append(frame).append(":\r\n");
            Frame frameObj = timeline.getFrames().get(frame);
            for (int i = 1; i <= maxDepth + 1; i++) {
                while (!clipDepths.isEmpty() && clipDepths.peek() <= i) {
                    clipDepths.pop();
                    sb.append("\t\t\tvar o = clips.pop();\r\n");
                    sb.append("\t\t\tctx.globalCompositeOperation = \"destination-in\";\r\n");
                    sb.append("\t\t\tctx.setTransform(1,0,0,1,0,0);\r\n");
                    sb.append("\t\t\tctx.drawImage(o.clipCanvas,0,0);\r\n");
                    sb.append("\t\t\tvar ms=o.ctx._matrix;\r\n");
                    sb.append("\t\t\to.ctx.setTransform(1,0,0,1,0,0);\r\n");
                    sb.append("\t\t\to.ctx.globalCompositeOperation = \"source-over\";\r\n");
                    sb.append("\t\t\to.ctx.drawImage(canvas,0,0);\r\n");
                    sb.append("\t\t\to.ctx.applyTransforms(ms);\r\n");
                    sb.append("\t\t\tctx = o.ctx;\r\n");
                    sb.append("\t\t\tcanvas = o.canvas;\r\n");
                }
                if (!frameObj.layers.containsKey(i)) {
                    continue;
                }
                DepthState layer = frameObj.layers.get(i);
                if (!timeline.swf.characters.containsKey(layer.characterId)) {
                    continue;
                }
                if (!layer.isVisible) {
                    continue;
                }
                CharacterTag character = timeline.swf.characters.get(layer.characterId);

                if (colorTransform == null) {
                    colorTransform = new ColorTransform();
                }
                Matrix placeMatrix = new Matrix(layer.matrix);
                placeMatrix.scaleX /= unitDivisor;
                placeMatrix.scaleY /= unitDivisor;
                placeMatrix.rotateSkew0 /= unitDivisor;
                placeMatrix.rotateSkew1 /= unitDivisor;
                placeMatrix.translateX /= unitDivisor;
                placeMatrix.translateY /= unitDivisor;

                int f = 0;
                String fstr = "0";
                if (character instanceof DefineSpriteTag) {
                    DefineSpriteTag sp = (DefineSpriteTag) character;
                    Timeline tim = sp.getTimeline();
                    if (tim.getFrameCount() > 0) {
                        f = layer.time % tim.getFrameCount();
                        fstr = "(" + f + "+time)%" + tim.getFrameCount();
                    }
                }

                if (layer.clipDepth != -1) {
                    clipDepths.push(layer.clipDepth);
                    sb.append("\t\t\tclips.push({ctx:ctx,canvas:canvas});\r\n");
                    sb.append("\t\t\tvar ccanvas = createCanvas(canvas.width,canvas.height);\r\n");
                    sb.append("\t\t\tvar cctx = ccanvas.getContext(\"2d\");\r\n");
                    sb.append("\t\t\tenhanceContext(cctx);\r\n");
                    sb.append("\t\t\tcctx.applyTransforms(ctx._matrix);\r\n");
                    sb.append("\t\t\tcanvas = ccanvas;\r\n");
                    sb.append("\t\t\tctx = cctx;\r\n");
                }

                if (layer.filters != null && layer.filters.size() > 0) {
                    sb.append("\t\t\tvar oldctx = ctx;\r\n");
                    sb.append("\t\t\tvar fcanvas = createCanvas(canvas.width,canvas.height);");
                    sb.append("\t\t\tvar fctx = fcanvas.getContext(\"2d\");\r\n");
                    sb.append("\t\t\tenhanceContext(fctx);\r\n");
                    sb.append("\t\t\tfctx.applyTransforms(ctx._matrix);\r\n");
                    sb.append("\t\t\tctx = fctx;\r\n");
                }

                ColorTransform ctrans = layer.colorTransForm;
                String ctrans_str = "ctrans";
                if (ctrans == null) {
                    ctrans = new ColorTransform();
                } else {
                    ctrans_str = "ctrans.merge(new cxform("
                            + ctrans.getRedAdd() + "," + ctrans.getGreenAdd() + "," + ctrans.getBlueAdd() + "," + ctrans.getAlphaAdd() + ","
                            + ctrans.getRedMulti() + "," + ctrans.getGreenMulti() + "," + ctrans.getBlueMulti() + "," + ctrans.getAlphaMulti()
                            + "))";
                }
                sb.append("\t\t\tplace(\"").append(getTypePrefix(character)).append(layer.characterId).append("\",canvas,ctx,[").append(placeMatrix.scaleX).append(",")
                        .append(placeMatrix.rotateSkew0).append(",")
                        .append(placeMatrix.rotateSkew1).append(",")
                        .append(placeMatrix.scaleY).append(",")
                        .append(placeMatrix.translateX).append(",")
                        .append(placeMatrix.translateY).append("],").append(ctrans_str).append(",").append("").append(layer.blendMode < 1 ? 1 : layer.blendMode).append(",").append(fstr).append(",").append(layer.ratio < 0 ? 0 : layer.ratio).append(",time").append(");\r\n");

                if (layer.filters != null && layer.filters.size() > 0) {
                    for (FILTER filter : layer.filters) {
                        if (filter instanceof COLORMATRIXFILTER) {
                            COLORMATRIXFILTER cmf = (COLORMATRIXFILTER) filter;
                            String mat = "[";
                            for (int k = 0; k < cmf.matrix.length; k++) {
                                if (k > 0) {
                                    mat += ",";
                                }
                                mat += cmf.matrix[k];
                            }
                            mat += "]";
                            sb.append("\t\t\tfcanvas = Filters.colorMatrix(fcanvas,fcanvas.getContext(\"2d\"),").append(mat).append(");\r\n");
                        }

                        if (filter instanceof CONVOLUTIONFILTER) {
                            CONVOLUTIONFILTER cf = (CONVOLUTIONFILTER) filter;
                            int height = cf.matrix.length;
                            int width = cf.matrix[0].length;
                            float[] matrix2 = new float[width * height];
                            for (int y = 0; y < height; y++) {
                                for (int x = 0; x < width; x++) {
                                    matrix2[y * width + x] = cf.matrix[x][y] * cf.divisor + cf.bias;
                                }
                            }
                            String mat = "[";
                            for (int k = 0; k < matrix2.length; k++) {
                                if (k > 0) {
                                    mat += ",";
                                }
                                mat += matrix2[k];
                            }
                            mat += "]";
                            sb.append("\t\t\tfcanvas = Filters.convolution(fcanvas,fcanvas.getContext(\"2d\"),").append(mat).append(",false);\r\n");
                        }

                        if (filter instanceof GLOWFILTER) {
                            GLOWFILTER gf = (GLOWFILTER) filter;
                            sb.append("\t\t\tfcanvas = Filters.glow(fcanvas,fcanvas.getContext(\"2d\"),").append(gf.blurX).append(",").append(gf.blurY).append(",").append(gf.strength).append(",").append(jsArrColor(gf.glowColor)).append(",").append(gf.innerGlow ? "true" : "false").append(",").append(gf.knockout ? "true" : "false").append(",").append(gf.passes).append(");\r\n");
                        }

                        if (filter instanceof DROPSHADOWFILTER) {
                            DROPSHADOWFILTER ds = (DROPSHADOWFILTER) filter;
                            sb.append("\t\t\tfcanvas = Filters.dropShadow(fcanvas,fcanvas.getContext(\"2d\"),").append(ds.blurX).append(",").append(ds.blurY).append(",").append((int) (ds.angle * 180 / Math.PI)).append(",").append(ds.distance).append(",").append(jsArrColor(ds.dropShadowColor)).append(",").append(ds.innerShadow ? "true" : "false").append(",").append(ds.passes).append(",").append(ds.strength).append(",").append(ds.knockout ? "true" : "false").append(");\r\n");
                        }
                        if (filter instanceof BEVELFILTER) {
                            BEVELFILTER bv = (BEVELFILTER) filter;
                            String type = "Filters.INNER";
                            if (bv.onTop && !bv.innerShadow) {
                                type = "Filters.FULL";
                            } else if (!bv.innerShadow) {
                                type = "Filters.OUTER";
                            }
                            sb.append("\t\t\tfcanvas = Filters.bevel(fcanvas,fcanvas.getContext(\"2d\"),").append(bv.blurX).append(",").append(bv.blurY).append(",").append(bv.strength).append(",").append(type).append(",").append(jsArrColor(bv.highlightColor)).append(",").append(jsArrColor(bv.shadowColor)).append(",").append((int) (bv.angle * 180 / Math.PI)).append(",").append(bv.distance).append(",").append(bv.knockout ? "true" : "false").append(",").append(bv.passes).append(");\r\n");
                        }

                        if (filter instanceof GRADIENTBEVELFILTER) {
                            GRADIENTBEVELFILTER gbf = (GRADIENTBEVELFILTER) filter;
                            String colArr = "[";
                            String ratArr = "[";
                            for (int k = 0; k < gbf.gradientColors.length; k++) {
                                if (k > 0) {
                                    colArr += ",";
                                    ratArr += ",";
                                }
                                colArr += jsArrColor(gbf.gradientColors[k]);
                                ratArr += gbf.gradientRatio[k] / 255f;
                            }
                            colArr += "]";
                            ratArr += "]";
                            String type = "Filters.INNER";
                            if (gbf.onTop && !gbf.innerShadow) {
                                type = "Filters.FULL";
                            } else if (!gbf.innerShadow) {
                                type = "Filters.OUTER";
                            }

                            sb.append("\t\t\tfcanvas = Filters.gradientBevel(fcanvas,fcanvas.getContext(\"2d\"),").append(colArr).append(",").append(ratArr).append(",").append(gbf.blurX).append(",").append(gbf.blurY).append(",").append(gbf.strength).append(",").append(type).append(",").append((int) (gbf.angle * 180 / Math.PI)).append(",").append(gbf.distance).append(",").append(gbf.knockout ? "true" : "false").append(",").append(gbf.passes).append(");\r\n");
                        }

                        if (filter instanceof GRADIENTGLOWFILTER) {
                            GRADIENTGLOWFILTER ggf = (GRADIENTGLOWFILTER) filter;
                            String colArr = "[";
                            String ratArr = "[";
                            for (int k = 0; k < ggf.gradientColors.length; k++) {
                                if (k > 0) {
                                    colArr += ",";
                                    ratArr += ",";
                                }
                                colArr += jsArrColor(ggf.gradientColors[k]);
                                ratArr += ggf.gradientRatio[k] / 255f;
                            }
                            colArr += "]";
                            ratArr += "]";
                            String type = "Filters.INNER";
                            if (ggf.onTop && !ggf.innerShadow) {
                                type = "Filters.FULL";
                            } else if (!ggf.innerShadow) {
                                type = "Filters.OUTER";
                            }

                            sb.append("\t\t\tfcanvas = Filters.gradientGlow(fcanvas,fcanvas.getContext(\"2d\"),").append(ggf.blurX).append(",").append(ggf.blurY).append(",").append((int) (ggf.angle * 180 / Math.PI)).append(",").append(ggf.distance).append(",").append(colArr).append(",").append(ratArr).append(",").append(type).append(",").append(ggf.passes).append(",").append(ggf.strength).append(",").append(ggf.knockout ? "true" : "false").append(");\r\n");
                        }
                    }
                    sb.append("\t\t\tctx = oldctx;\r\n");
                    sb.append("\t\t\tvar ms=ctx._matrix;\r\n");
                    sb.append("\t\t\tctx.setTransform(1,0,0,1,0,0);\r\n");
                    sb.append("\t\t\tctx.drawImage(fcanvas,0,0);\r\n");
                    sb.append("\t\t\tctx.applyTransforms(ms);\r\n");
                }

                if (layer.clipDepth != -1) {
                    sb.append("\t\t\tclips[clips.length-1].clipCanvas = canvas;\r\n");
                    sb.append("\t\t\tcanvas = createCanvas(canvas.width,canvas.height);\r\n");
                    sb.append("\t\t\tvar nctx = canvas.getContext(\"2d\");\r\n");
                    sb.append("\t\t\tenhanceContext(nctx);\r\n");
                    sb.append("\t\t\tnctx.applyTransforms(ctx._matrix);\r\n");
                    sb.append("\t\t\tctx = nctx;\r\n");
                }
            }
            sb.append("\t\t\tbreak;\r\n");
        }
        sb.append("\t}\r\n");
        return sb.toString();
    }

    public static void frameToSvg(Timeline timeline, int frame, int time, DepthState stateUnderCursor, int mouseButton, SVGExporter exporter, ColorTransform colorTransform, int level, double zoom) throws IOException {
        if (timeline.getFrames().size() <= frame) {
            return;
        }
        Frame frameObj = timeline.getFrames().get(frame);
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
            if (!timeline.swf.characters.containsKey(layer.characterId)) {
                continue;
            }
            if (!layer.isVisible) {
                continue;
            }
            CharacterTag character = timeline.swf.characters.get(layer.characterId);

            if (colorTransform == null) {
                colorTransform = new ColorTransform();
            }

            ColorTransform clrTrans = colorTransform.clone();
            if (layer.colorTransForm != null && layer.blendMode <= 1) { //Normal blend mode
                clrTrans = colorTransform.merge(layer.colorTransForm);
            }

            if (character instanceof DrawableTag) {
                DrawableTag drawable = (DrawableTag) character;

                String assetName;
                Tag drawableTag = (Tag) drawable;
                RECT boundRect = drawable.getRect(new HashSet<BoundedTag>());
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
                    exporter.addUse(mat, boundRect, assetName);
                    exporter.setClip(clip.shape);
                    exporter.endGroup();
                } else {
                    Matrix mat = Matrix.getTranslateInstance(rect.xMin, rect.yMin).preConcatenate(new Matrix(layer.matrix));
                    exporter.addUse(mat, boundRect, assetName);
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
        String key = "frame_" + frame + "_" + timeline.id + "_" + timeline.swf.hashCode() + "_" + zoom;
        SerializableImage image;
        if (useCache) {
            image = getFromCache(key);
            if (image != null) {
                return image;
            }
        }

        if (timeline.getFrames().isEmpty()) {
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
        m.translate(-rect.Xmin, -rect.Ymin);
        m.scale(zoom);
        frameToImage(timeline, frame, time, stateUnderCursor, mouseButton, image, m, colorTransform);
        putToCache(key, image);
        return image;
    }

    public static void framesToImage(Timeline timeline, List<SerializableImage> ret, int startFrame, int stopFrame, DepthState stateUnderCursor, int mouseButton, RECT displayRect, int totalFrameCount, Stack<Integer> visited, Matrix transformation, ColorTransform colorTransform, double zoom) {
        RECT rect = displayRect;
        for (int f = 0; f < timeline.getFrames().size(); f++) {
            SerializableImage image = new SerializableImage((int) (rect.getWidth() / SWF.unitDivisor) + 1,
                    (int) (rect.getHeight() / SWF.unitDivisor) + 1, SerializableImage.TYPE_INT_ARGB);
            image.fillTransparent();
            Matrix m = new Matrix();
            m.translate(-rect.Xmin, -rect.Ymin);
            frameToImage(timeline, f, 0, stateUnderCursor, mouseButton, image, m, colorTransform);
            ret.add(image);
        }
    }

    public static void frameToImage(Timeline timeline, int frame, int time, DepthState stateUnderCursor, int mouseButton, SerializableImage image, Matrix transformation, ColorTransform colorTransform) {
        double unzoom = SWF.unitDivisor;
        if (timeline.getFrames().size() <= frame) {
            return;
        }
        Frame frameObj = timeline.getFrames().get(frame);
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
            if (!timeline.swf.characters.containsKey(layer.characterId)) {
                continue;
            }
            if (!layer.isVisible) {
                continue;
            }
            CharacterTag character = timeline.swf.characters.get(layer.characterId);
            Matrix mat = new Matrix(layer.matrix);
            mat = mat.preConcatenate(transformation);

            if (colorTransform == null) {
                colorTransform = new ColorTransform();
            }

            ColorTransform clrTrans = colorTransform.clone();
            if (layer.colorTransForm != null && layer.blendMode <= 1) { //Normal blend mode
                clrTrans = colorTransform.merge(layer.colorTransForm);
            }

            boolean showPlaceholder = false;
            if (character instanceof DrawableTag) {
                DrawableTag drawable = (DrawableTag) character;
                SerializableImage img;
                Matrix drawMatrix = new Matrix();
                int drawableFrameCount = drawable.getNumFrames();
                if (drawableFrameCount == 0) {
                    drawableFrameCount = 1;
                }
                int dframe = (time + layer.time) % drawableFrameCount;
                if (character instanceof ButtonTag) {
                    ButtonTag bt = (ButtonTag) character;
                    dframe = ButtonTag.FRAME_UP;
                    if (stateUnderCursor == layer) {
                        if (mouseButton > 0) {
                            dframe = ButtonTag.FRAME_DOWN;
                        } else {
                            dframe = ButtonTag.FRAME_OVER;
                        }
                    }
                }

                RECT boundRect = drawable.getRect(new HashSet<BoundedTag>());
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

                img = new SerializableImage(newWidth, newHeight, SerializableImage.TYPE_INT_ARGB);
                img.fillTransparent();

                m.translate(-rect.xMin, -rect.yMin);
                drawMatrix.translate(rect.xMin, rect.yMin);

                drawable.toImage(dframe, layer.time + time, layer.ratio, stateUnderCursor, mouseButton, img, m, clrTrans);
                //if(stateUnderCursor == layer){
              /* if(true){
                 Graphics2D gg = (Graphics2D)img.getGraphics();
                 gg.setStroke(new BasicStroke(3));
                 gg.setPaint(Color.red);
                 gg.setTransform(AffineTransform.getTranslateInstance(0, 0));
                 gg.draw(SHAPERECORD.twipToPixelShape(drawable.getOutline(frame, layer.ratio, stateUnderCursor, mouseButton, m)));
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
                    case 2://Layer
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
                    default: //Not implemented
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
                    Clip clip = new Clip(Helper.imageToShape(mask), layer.clipDepth); //Maybe we can get current outline instead converting from image (?)
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
                RECT r = b.getRect(new HashSet<BoundedTag>());
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
        int characterId = 0;
        if (toRemove instanceof CharacterTag) {
            characterId = ((CharacterTag) toRemove).getCharacterId();
        }
        Map<Integer, Integer> stage = new HashMap<>();

        Set<Integer> dependingChars = new HashSet<>();
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

    public void removeTag(Tag t, boolean removeDependencies) {
        Timelined timelined = t.getTimelined();
        if (t instanceof ShowFrameTag || ShowFrameTag.isNestedTagType(t.getId())) {
            List<Tag> tags;
            if (timelined instanceof DefineSpriteTag) {
                tags = ((DefineSpriteTag) timelined).getSubTags();
            } else {
                tags = this.tags;
            }
            tags.remove(t);
            timelined.getTimeline().reset();
        } else {
            // timeline should be always the swf here
            if (removeDependencies) {
                removeTagWithDependenciesFromTimeline(t, timelined.getTimeline());
            } else {
                removeTagFromTimeline(t, timeline);
            }
        }
        resetTimelines(timelined);
        updateCharacters();
        clearImageCache();
    }

    @Override
    public String toString() {
        return getShortFileName();
    }
}
