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
package com.jpexs.decompiler.flash;

import SevenZip.Compression.LZMA.Encoder;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ClassPath;
import com.jpexs.decompiler.flash.abc.RenameType;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionDeobfuscation;
import com.jpexs.decompiler.flash.action.ActionGraphSource;
import com.jpexs.decompiler.flash.action.ActionLocalData;
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
import com.jpexs.decompiler.flash.ecma.Null;
import com.jpexs.decompiler.flash.exporters.ExportRectangle;
import com.jpexs.decompiler.flash.exporters.Matrix;
import com.jpexs.decompiler.flash.flv.AUDIODATA;
import com.jpexs.decompiler.flash.flv.FLVOutputStream;
import com.jpexs.decompiler.flash.flv.FLVTAG;
import com.jpexs.decompiler.flash.flv.VIDEODATA;
import com.jpexs.decompiler.flash.gui.SWFList;
import com.jpexs.decompiler.flash.helpers.collections.MyEntry;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.flash.tags.DefineBinaryDataTag;
import com.jpexs.decompiler.flash.tags.DefineButton2Tag;
import com.jpexs.decompiler.flash.tags.DefineButtonTag;
import com.jpexs.decompiler.flash.tags.DefineSoundTag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.DefineVideoStreamTag;
import com.jpexs.decompiler.flash.tags.DoInitActionTag;
import com.jpexs.decompiler.flash.tags.ExportAssetsTag;
import com.jpexs.decompiler.flash.tags.FileAttributesTag;
import com.jpexs.decompiler.flash.tags.JPEGTablesTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.SoundStreamBlockTag;
import com.jpexs.decompiler.flash.tags.SymbolClassTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.VideoFrameTag;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.flash.tags.base.BoundedTag;
import com.jpexs.decompiler.flash.tags.base.CharacterIdTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.Container;
import com.jpexs.decompiler.flash.tags.base.ContainerItem;
import com.jpexs.decompiler.flash.tags.base.DrawableTag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.tags.base.RemoveTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.tags.base.SoundStreamHeadTypeTag;
import com.jpexs.decompiler.flash.tags.base.TextTag;
import com.jpexs.decompiler.flash.timeline.DepthState;
import com.jpexs.decompiler.flash.timeline.Frame;
import com.jpexs.decompiler.flash.timeline.Timeline;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.treeitems.AS2PackageNodeItem;
import com.jpexs.decompiler.flash.treeitems.AS3PackageNodeItem;
import com.jpexs.decompiler.flash.treeitems.FrameNodeItem;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import com.jpexs.decompiler.flash.treenodes.AS2PackageNode;
import com.jpexs.decompiler.flash.treenodes.ContainerNode;
import com.jpexs.decompiler.flash.treenodes.FrameNode;
import com.jpexs.decompiler.flash.treenodes.TagNode;
import com.jpexs.decompiler.flash.treenodes.TreeNode;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.filters.BlendComposite;
import com.jpexs.decompiler.flash.types.filters.FILTER;
import com.jpexs.decompiler.flash.types.sound.AdpcmDecoder;
import com.jpexs.decompiler.flash.xfl.FLAVersion;
import com.jpexs.decompiler.flash.xfl.XFLConverter;
import com.jpexs.decompiler.graph.ExportMode;
import com.jpexs.decompiler.graph.Graph;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemContainer;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.helpers.Cache;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.Helper;
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
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.HashSet;
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
import javax.imageio.ImageIO;

/**
 * Class representing SWF file
 *
 * @author JPEXS
 */
public final class SWF implements TreeItem, Timelined {

    /**
     * Default version of SWF file format
     */
    public static final int DEFAULT_VERSION = 10;
    /**
     * Tags inside of file
     */
    public List<Tag> tags = new ArrayList<>();
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
     * Size of the file
     */
    public long fileSize;
    /**
     * Use compression
     */
    public boolean compressed = false;
    /**
     * Use LZMA compression
     */
    public boolean lzma = false;
    /**
     * Compressed size of the file (LZMA)
     */
    public long compressedSize;
    /**
     * LZMA Properties
     */
    public byte[] lzmaProperties;
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
    public HashMap<Integer, CharacterTag> characters;
    public List<ABCContainerTag> abcList;
    public JPEGTablesTag jtt;
    public Map<Integer, String> sourceFontsMap = new HashMap<>();
    public static final double unitDivisor = 20;

    private Timeline timeline;

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
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            SWFOutputStream sos = new SWFOutputStream(baos, version);
            sos.writeRECT(displayRect);
            sos.writeUI8(0);
            sos.writeUI8(frameRate);
            sos.writeUI16(frameCount);

            sos.writeTags(tags);
            sos.writeUI16(0);
            sos.close();
            if (compressed && lzma) {
                os.write('Z');
            } else if (compressed) {
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

            if (compressed) {
                if (lzma) {
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
                    os.write(lzmaProperties);
                } else {
                    os = new DeflaterOutputStream(os);
                }
            }
            os.write(data);
        } finally {
            if (os != null) {
                os.close();
            }
        }
    }

    public SWF(InputStream is, boolean parallelRead) throws IOException, InterruptedException {
        this(is, null, parallelRead);
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
        byte[] hdr = new byte[3];
        is.read(hdr);
        String shdr = new String(hdr, Utf8Helper.charset);
        if (!Arrays.asList(
                "FWS", //Uncompressed Flash
                "CWS", //ZLib compressed Flash
                "ZWS", //LZMA compressed Flash
                "GFX", //Uncompressed ScaleForm GFx
                "CFX" //Compressed ScaleForm GFx
        ).contains(shdr)) {
            throw new IOException("Invalid SWF file");
        }
        version = is.read();
        fileSize = (is.read() + (is.read() << 8) + (is.read() << 16) + (is.read() << 24)) & 0xffffffff;

        if (hdr[0] == 'C') {
            is = new InflaterInputStream(is);
        }

        if (hdr[0] == 'Z') {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            //outSize
            is.read();
            is.read();
            is.read();
            is.read();
            int propertiesSize = 5;
            lzmaProperties = new byte[propertiesSize];
            if (is.read(lzmaProperties, 0, propertiesSize) != propertiesSize) {
                throw new IOException("LZMA:input .lzma file is too short");
            }
            long dictionarySize = 0;
            for (int i = 0; i < 4; i++) {
                dictionarySize += ((int) (lzmaProperties[1 + i]) & 0xFF) << (i * 8);
                if (dictionarySize > Runtime.getRuntime().freeMemory()) {
                    throw new IOException("LZMA: Too large dictionary size");
                }
            }

            SevenZip.Compression.LZMA.Decoder decoder = new SevenZip.Compression.LZMA.Decoder();
            if (!decoder.SetDecoderProperties(lzmaProperties)) {
                throw new IOException("LZMA:Incorrect stream properties");
            }

            if (!decoder.Code(is, baos, fileSize - 8)) {
                throw new IOException("LZMA:Error in data stream");
            }
        } else {
            long toRead = fileSize - 8;
            if (toRead > 0) {
                byte[] bytes = new byte[4096];
                while (toRead > 4096) {
                    int read = is.read(bytes);
                    if (read == -1) {
                        throw new IOException("Invalid SWF file");
                    }
                    toRead -= read;
                }
                while (toRead > 0) {
                    int read = is.read(bytes, 0, (int) toRead);
                    if (read == -1) {
                        throw new IOException("Invalid SWF file");
                    }
                    toRead -= read;
                }
            }
        }
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
        byte[] hdr = new byte[3];
        is.read(hdr);
        String shdr = new String(hdr, Utf8Helper.charset);
        if (!Arrays.asList(
                "FWS", //Uncompressed Flash
                "CWS", //ZLib compressed Flash
                "ZWS", //LZMA compressed Flash
                "GFX", //Uncompressed ScaleForm GFx
                "CFX" //Compressed ScaleForm GFx
        ).contains(shdr)) {
            throw new IOException("Invalid SWF file");
        }
        version = is.read();
        SWFInputStream sis = new SWFInputStream(is, version, 4);
        fileSize = sis.readUI32();

        if (hdr[1] == 'F' && hdr[2] == 'X') {
            gfx = true;
        }
        if (hdr[0] == 'C') {
            sis = new SWFInputStream(new InflaterInputStream(is), version, 8);
            compressed = true;
        }

        if (hdr[0] == 'Z') {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            sis.readUI32(); //outSize
            int propertiesSize = 5;
            lzmaProperties = new byte[propertiesSize];
            if (sis.read(lzmaProperties, 0, propertiesSize) != propertiesSize) {
                throw new IOException("LZMA:input .lzma file is too short");
            }
            long dictionarySize = 0;
            for (int i = 0; i < 4; i++) {
                dictionarySize += ((int) (lzmaProperties[1 + i]) & 0xFF) << (i * 8);
                if (dictionarySize > Runtime.getRuntime().freeMemory()) {
                    throw new IOException("LZMA: Too large dictionary size");
                }
            }

            SevenZip.Compression.LZMA.Decoder decoder = new SevenZip.Compression.LZMA.Decoder();
            if (!decoder.SetDecoderProperties(lzmaProperties)) {
                throw new IOException("LZMA:Incorrect stream properties");
            }

            if (!decoder.Code(sis, baos, fileSize - 8)) {
                throw new IOException("LZMA:Error in data stream");
            }
            sis = new SWFInputStream(new ByteArrayInputStream(baos.toByteArray()), version, 8);
            compressed = true;
            lzma = true;
        }

        if (listener != null) {
            sis.addPercentListener(listener);
        }
        sis.setPercentMax(fileSize);
        displayRect = sis.readRECT();
        // FIXED8 (16 bit fixed point) frameRate
        sis.readUI8(); //tmpFirstByetOfFrameRate
        frameRate = sis.readUI8();
        frameCount = sis.readUI16();
        tags = sis.readTagList(this, 0, parallelRead, true, !checkOnly, gfx);
        if (!checkOnly) {
            Map<Long, Tag> tagMap = new HashMap<>();
            for (Tag tag : tags) {
                tagMap.put(tag.getPos(), tag);
            }

            for (Tag tag : tags) {
                if (tag instanceof ShowFrameTag) {
                    ShowFrameTag showFrameTag = (ShowFrameTag) tag;
                    List<Long> tagPositions = sis.tagPositionsInFrames.get(tag.getPos());
                    List<Tag> innerTags = new ArrayList<>();
                    for (long tagPos : tagPositions) {
                        innerTags.add(tagMap.get(tagPos));
                    }
                    showFrameTag.innerTags = innerTags;
                }
            }

            assignExportNamesToSymbols();
            assignClassesToSymbols();
            findFileAttributes();
        } else {
            boolean hasNonUnknownTag = false;
            for (Tag tag : tags) {
                if (tag.getData().length > 0 && Tag.getRequiredTags().contains(tag.getId())) {
                    hasNonUnknownTag = true;
                }
            }
            if (!hasNonUnknownTag) {
                throw new IOException("Invalid SWF file. No known tag found.");
            }
        }
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
                    if ((!exportNames.containsKey(eat.tags.get(i))) && (!exportNames.containsValue(eat.names.get(i)))) {
                        exportNames.put(eat.tags.get(i), eat.names.get(i));
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
            if (t instanceof CharacterIdTag) {
                CharacterIdTag ct = (CharacterIdTag) t;
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
            byte[] hdr = new byte[3];
            fis.read(hdr);
            String shdr = new String(hdr, Utf8Helper.charset);
            switch (shdr) {
                case "CWS": {
                    int version = fis.read();
                    SWFInputStream sis = new SWFInputStream(fis, version, 4);
                    long fileSize = sis.readUI32();
                    SWFOutputStream sos = new SWFOutputStream(fos, version);
                    sos.write(Utf8Helper.getBytes("FWS"));
                    sos.writeUI8(version);
                    sos.writeUI32(fileSize);
                    InflaterInputStream iis = new InflaterInputStream(fis);
                    int i;
                    try {
                        while ((i = iis.read()) != -1) {
                            fos.write(i);
                        }
                    } catch (EOFException ex) {
                    }
                    fis.close();
                    fos.close();
                    break;
                }
                case "ZWS": {
                    int version = fis.read();
                    SWFInputStream sis = new SWFInputStream(fis, version, 4);
                    long fileSize = sis.readUI32();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    sis.readUI32();
                    int propertiesSize = 5;
                    byte[] lzmaProperties = new byte[propertiesSize];
                    if (sis.read(lzmaProperties, 0, propertiesSize) != propertiesSize) {
                        throw new IOException("LZMA:input .lzma file is too short");
                    }
                    SevenZip.Compression.LZMA.Decoder decoder = new SevenZip.Compression.LZMA.Decoder();
                    if (!decoder.SetDecoderProperties(lzmaProperties)) {
                        throw new IOException("LZMA:Incorrect stream properties");
                    }
                    if (!decoder.Code(sis, baos, fileSize - 8)) {
                        throw new IOException("LZMA:Error in data stream");
                    }
                    try (SWFOutputStream sos = new SWFOutputStream(fos, version)) {
                        sos.write(Utf8Helper.getBytes("FWS"));
                        sos.write(version);
                        sos.writeUI32(fileSize);
                        sos.write(baos.toByteArray());
                    }
                    fis.close();
                    fos.close();
                    break;
                }
                default:
                    return false;
            }
        } catch (IOException ex) {
            return false;
        }
        return true;
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

    public boolean exportAS3Class(String className, String outdir, ExportMode exportMode, boolean parallel) throws Exception {
        List<ABCContainerTag> abcTags = new ArrayList<>();

        for (Tag t : tags) {
            if (t instanceof ABCContainerTag) {
                ABCContainerTag cnt = (ABCContainerTag) t;
                abcTags.add(cnt);
            }
        }
        for (int i = 0; i < abcTags.size(); i++) {
            ABC abc = abcTags.get(i).getABC();
            ScriptPack scr = abc.findScriptPackByPath(className);
            if (scr != null) {
                String cnt = "";
                if (abc.script_info.length > 1) {
                    cnt = "script " + (i + 1) + "/" + abc.script_info.length + " ";
                }
                String exStr = "Exporting " + "tag " + (i + 1) + "/" + abcTags.size() + " " + cnt + scr.getPath() + " ...";
                informListeners("exporting", exStr);
                scr.export(outdir, abcTags, exportMode, parallel);
                exStr = "Exported " + "tag " + (i + 1) + "/" + abcTags.size() + " " + cnt + scr.getPath() + " ...";
                informListeners("exported", exStr);
                return true;
            }
        }
        return false;
    }

    private List<MyEntry<ClassPath, ScriptPack>> uniqueAS3Packs(List<MyEntry<ClassPath, ScriptPack>> packs) {
        List<MyEntry<ClassPath, ScriptPack>> ret = new ArrayList<>();
        for (MyEntry<ClassPath, ScriptPack> item : packs) {
            for (MyEntry<ClassPath, ScriptPack> itemOld : ret) {
                if (item.key.equals(itemOld.key)) {
                    Logger.getLogger(SWF.class.getName()).log(Level.SEVERE, "Duplicate pack path found (" + itemOld.key + ")!");
                    break;
                }
            }
            ret.add(item);
        }
        return ret;
    }

    public List<MyEntry<ClassPath, ScriptPack>> getAS3Packs() {
        List<ABCContainerTag> abcTags = new ArrayList<>();
        for (Tag t : tags) {
            if (t instanceof ABCContainerTag) {
                abcTags.add((ABCContainerTag) t);
            }
        }
        List<MyEntry<ClassPath, ScriptPack>> packs = new ArrayList<>();
        for (int i = 0; i < abcTags.size(); i++) {
            ABCContainerTag t = abcTags.get(i);
            packs.addAll(t.getABC().getScriptPacks());
        }
        return uniqueAS3Packs(packs);
    }

    private class ExportPackTask implements Callable<File> {

        ScriptPack pack;
        String directory;
        List<ABCContainerTag> abcList;
        ExportMode exportMode;
        ClassPath path;
        AtomicInteger index;
        int count;
        boolean parallel;
        AbortRetryIgnoreHandler handler;
        long startTime;
        long stopTime;

        public ExportPackTask(AbortRetryIgnoreHandler handler, AtomicInteger index, int count, ClassPath path, ScriptPack pack, String directory, List<ABCContainerTag> abcList, ExportMode exportMode, boolean parallel) {
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
                long time = stopTime - startTime;
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

    public List<File> exportActionScript2(AbortRetryIgnoreHandler handler, String outdir, ExportMode exportMode, boolean parallel, EventListener evl) throws IOException {
        List<File> ret = new ArrayList<>();
        List<ContainerItem> list2 = new ArrayList<>();
        list2.addAll(tags);
        List<TreeNode> list = createASTagList(list2, null);

        TagNode.setExport(list, true);
        if (!outdir.endsWith(File.separator)) {
            outdir += File.separator;
        }
        outdir += "scripts" + File.separator;
        ret.addAll(TagNode.exportNodeAS(handler, list, outdir, exportMode, evl));
        return ret;
    }

    public List<File> exportActionScript3(final AbortRetryIgnoreHandler handler, final String outdir, final ExportMode exportMode, final boolean parallel) {
        final AtomicInteger cnt = new AtomicInteger(1);
        final List<ABCContainerTag> abcTags = new ArrayList<>();
        for (Tag t : tags) {
            if (t instanceof ABCContainerTag) {
                abcTags.add((ABCContainerTag) t);
            }
        }

        final List<File> ret = new ArrayList<>();
        final List<MyEntry<ClassPath, ScriptPack>> packs = getAS3Packs();

        if (!parallel || packs.size() < 2) {
            try {
                CancellableWorker.call(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        for (MyEntry<ClassPath, ScriptPack> item : packs) {
                            ExportPackTask task = new ExportPackTask(handler, cnt, packs.size(), item.key, item.value, outdir, abcTags, exportMode, parallel);
                            ret.add(task.call());
                        }
                        return null;
                    }
                }, Configuration.exportTimeout.get(), TimeUnit.SECONDS);
            } catch (TimeoutException ex) {
                Logger.getLogger(ABC.class.getName()).log(Level.SEVERE, Helper.formatTimeToText(Configuration.exportTimeout.get()) + " ActionScript export limit reached", ex);
            } catch (Exception ex) {
                Logger.getLogger(ABC.class.getName()).log(Level.SEVERE, "Error during ABC export", ex);
            }
        } else {
            ExecutorService executor = Executors.newFixedThreadPool(Configuration.parallelThreadCount.get());
            List<Future<File>> futureResults = new ArrayList<>();
            for (MyEntry<ClassPath, ScriptPack> item : packs) {
                Future<File> future = executor.submit(new ExportPackTask(handler, cnt, packs.size(), item.key, item.value, outdir, abcTags, exportMode, parallel));
                futureResults.add(future);
            }

            try {
                executor.shutdown();
                if (!executor.awaitTermination(Configuration.exportTimeout.get(), TimeUnit.SECONDS)) {
                    Logger.getLogger(ABC.class.getName()).log(Level.SEVERE, Helper.formatTimeToText(Configuration.exportTimeout.get()) + " ActionScript export limit reached");
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
                    Logger.getLogger(SWF.class.getName()).log(Level.SEVERE, "Error during ABC export", ex);
                }
            }
        }

        return ret;
    }

    public List<File> exportActionScript(AbortRetryIgnoreHandler handler, String outdir, ExportMode exportMode, boolean parallel) throws Exception {
        boolean asV3Found = false;
        List<File> ret = new ArrayList<>();
        final EventListener evl = new EventListener() {
            @Override
            public void handleEvent(String event, Object data) {
                if (event.equals("exporting") || event.equals("exported")) {
                    informListeners(event, data);
                }
            }
        };
        for (Tag t : tags) {
            if (t instanceof ABCContainerTag) {
                asV3Found = true;
            }
        }

        if (asV3Found) {
            ret.addAll(exportActionScript3(handler, outdir, exportMode, parallel));
        } else {
            ret.addAll(exportActionScript2(handler, outdir, exportMode, parallel, evl));
        }
        return ret;
    }

    public static List<TreeNode> createASTagList(List<? extends ContainerItem> list, Tag parent) {
        List<TreeNode> ret = new ArrayList<>();
        int frame = 1;
        List<TreeNode> frames = new ArrayList<>();

        List<ExportAssetsTag> exportAssetsTags = new ArrayList<>();
        for (ContainerItem t : list) {
            if (t instanceof ExportAssetsTag) {
                exportAssetsTags.add((ExportAssetsTag) t);
            }
            TreeNode addNode = null;
            if (t instanceof ShowFrameTag) {
                // do not add PlaceObjects (+etc) to script nodes
                FrameNode tti = new FrameNode(new FrameNodeItem(t.getSwf(), frame, parent, false), null);

                for (int r = ret.size() - 1; r >= 0; r--) {
                    if (!(ret.get(r).getItem() instanceof DefineSpriteTag)) {
                        if (!(ret.get(r).getItem() instanceof DefineButtonTag)) {
                            if (!(ret.get(r).getItem() instanceof DefineButton2Tag)) {
                                if (!(ret.get(r).getItem() instanceof DoInitActionTag)) {
                                    if (!(ret.get(r).getItem() instanceof AS2PackageNodeItem)) {
                                        tti.subNodes.add(ret.get(r));
                                        ret.remove(r);
                                    }
                                }
                            }
                        }
                    }
                }
                frame++;
                frames.add(tti);
            } else if (t instanceof ASMSource) {
                ContainerNode tti = new ContainerNode(t);
                //ret.add(tti);
                addNode = tti;
            } else if (t instanceof Container) {
                if (((Container) t).getItemCount() > 0) {

                    ContainerNode tti = new ContainerNode(t);
                    List<ContainerItem> subItems = ((Container) t).getSubItems();

                    Tag tag = t instanceof Tag ? (Tag) t : null;
                    tti.subNodes = createASTagList(subItems, tag);
                    addNode = tti;
                    //ret.add(tti);
                }
            }
            if (addNode != null) {
                if (addNode.getItem() instanceof CharacterIdTag) {
                    CharacterIdTag cit = (CharacterIdTag) addNode.getItem();
                    String path = cit.getExportName();
                    if (path == null) {
                        path = "";
                    }
                    String[] pathParts;
                    if (path.contains(".")) {
                        pathParts = path.split("\\.");
                    } else {
                        pathParts = new String[]{path};
                    }
                    List<TreeNode> items = ret;
                    int pos = 0;
                    TreeNode selNode = null;
                    do {
                        if (pos == pathParts.length - 1) {
                            break;
                        }
                        selNode = null;
                        for (TreeNode node : items) {
                            if (node.getItem() instanceof AS2PackageNodeItem) {
                                AS2PackageNodeItem pkg = (AS2PackageNodeItem) node.getItem();
                                if (pkg.packageName.equals(pathParts[pos])) {
                                    selNode = node;
                                    break;
                                }
                            }
                        }
                        int pkgCount = 0;
                        for (; pkgCount < items.size(); pkgCount++) {
                            if (items.get(pkgCount).getItem() instanceof AS3PackageNodeItem) {
                                AS2PackageNodeItem pkg = (AS2PackageNodeItem) items.get(pkgCount).getItem();
                                if (pkg.packageName.compareTo(pathParts[pos]) > 0) {
                                    break;
                                }
                            } else {
                                break;
                            }
                        }
                        if (selNode == null) {
                            items.add(pkgCount, selNode = new AS2PackageNode(new AS2PackageNodeItem(pathParts[pos], t.getSwf())));
                        }
                        pos++;
                        if (selNode != null) {
                            items = selNode.subNodes;
                        }

                    } while (selNode != null);

                    int clsCount = 0;
                    for (; clsCount < items.size(); clsCount++) {
                        if (items.get(clsCount).getItem() instanceof CharacterIdTag) {
                            CharacterIdTag ct = (CharacterIdTag) items.get(clsCount).getItem();
                            String expName = ct.getExportName();
                            if (expName == null) {
                                expName = "";
                            }
                            if (expName.contains(".")) {
                                expName = expName.substring(expName.lastIndexOf('.') + 1);
                            }
                            if ((ct.getClass().getName() + "_" + expName).compareTo(addNode.getItem().getClass().getName() + "_" + pathParts[pos]) > 0) {
                                break;
                            }
                        }
                    }
                    items.add(clsCount, addNode);
                } else {
                    ret.add(addNode);
                }
            }

        }
        ret.addAll(frames);
        for (int i = ret.size() - 1; i >= 0; i--) {
            if (ret.get(i).getItem() instanceof DefineSpriteTag) {
                ((DefineSpriteTag) ret.get(i).getItem()).exportAssetsTags = exportAssetsTags;
            }
            if (ret.get(i).getItem() instanceof DefineButtonTag) {
                ((DefineButtonTag) ret.get(i).getItem()).exportAssetsTags = exportAssetsTags;
            }
            if (ret.get(i).getItem() instanceof DefineButton2Tag) {
                ((DefineButton2Tag) ret.get(i).getItem()).exportAssetsTags = exportAssetsTags;
            }
            /*if (ret.get(i).tag instanceof DoInitActionTag) {
             //((DoInitActionTag) ret.get(i).tag).exportAssetsTags = exportAssetsTags;
             }*/
            if (ret.get(i).getItem() instanceof ASMSource) {
                ASMSource ass = (ASMSource) ret.get(i).getItem();
                if (ass.containsSource()) {
                    continue;
                }
            }
            if (ret.get(i).subNodes.isEmpty()) {
                ret.remove(i);
            }
        }
        return ret;
    }

    public static void getTagsFromTreeNodes(List<TreeNode> treeNodes, List<Tag> result) {
        for (TreeNode treeNode : treeNodes) {
            TreeItem treeItem = treeNode.getItem();
            if (treeItem instanceof Tag) {
                result.add((Tag) treeItem);
            }
            getTagsFromTreeNodes(treeNode.subNodes, result);
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
        if (data.length > 4) {
            if ((data[0] & 0xff) == 0xff) {
                if ((data[1] & 0xff) == 0xd9) {
                    if ((data[2] & 0xff) == 0xff) {
                        if ((data[3] & 0xff) == 0xd8) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static void populateSoundStreamBlocks(List<? extends ContainerItem> tags, Tag head, List<SoundStreamBlockTag> output) {
        boolean found = false;
        for (ContainerItem t : tags) {
            if (t == head) {
                found = true;
                continue;
            }
            if (!found) {
                continue;
            }
            if (t instanceof SoundStreamBlockTag) {
                output.add((SoundStreamBlockTag) t);
            }
            if (t instanceof SoundStreamHeadTypeTag) {
                break;
            }
            if (t instanceof Container) {
                populateSoundStreamBlocks(((Container) t).getSubItems(), head, output);
            }
        }
    }

    public void populateVideoFrames(int streamId, List<? extends ContainerItem> tags, HashMap<Integer, VideoFrameTag> output) {
        for (ContainerItem t : tags) {
            if (t instanceof VideoFrameTag) {
                output.put(((VideoFrameTag) t).frameNum, (VideoFrameTag) t);
            }
            if (t instanceof Container) {
                populateVideoFrames(streamId, ((Container) t).getSubItems(), output);
            }
        }
    }

    public void exportMovies(AbortRetryIgnoreHandler handler, String outdir) throws IOException {
        exportMovies(handler, outdir, tags);
    }

    public void exportSounds(AbortRetryIgnoreHandler handler, String outdir, boolean mp3, boolean wave) throws IOException {
        exportSounds(handler, outdir, tags, mp3, wave);
    }

    public byte[] exportSound(Tag t) throws IOException {
        boolean mp3 = true;
        boolean wave = true;
        try (ByteArrayOutputStream fos = new ByteArrayOutputStream()) {

            if (t instanceof DefineSoundTag) {
                DefineSoundTag st = (DefineSoundTag) t;
                if ((st.soundFormat == DefineSoundTag.FORMAT_ADPCM) && wave) {
                    createWavFromAdpcm(fos, st.soundRate, st.soundSize, st.soundType, st.soundData);
                } else if ((st.soundFormat == DefineSoundTag.FORMAT_MP3) && mp3) {
                    fos.write(st.soundData, 2, st.soundData.length - 2);
                } else {
                    FLVOutputStream flv = new FLVOutputStream(fos);
                    flv.writeHeader(true, false);
                    flv.writeTag(new FLVTAG(0, new AUDIODATA(st.soundFormat, st.soundRate, st.soundSize, st.soundType, st.soundData)));
                }
            }
            if (t instanceof SoundStreamHeadTypeTag) {
                SoundStreamHeadTypeTag shead = (SoundStreamHeadTypeTag) t;
                List<SoundStreamBlockTag> blocks = new ArrayList<>();
                populateSoundStreamBlocks(this.tags, t, blocks);
                if ((shead.getSoundFormat() == DefineSoundTag.FORMAT_ADPCM) && wave) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    for (int b = 0; b < blocks.size(); b++) {
                        byte[] data = blocks.get(b).getData();
                        baos.write(data);
                    }
                    createWavFromAdpcm(fos, shead.getSoundRate(), shead.getSoundSize(), shead.getSoundType(), baos.toByteArray());
                } else if ((shead.getSoundFormat() == DefineSoundTag.FORMAT_MP3) && mp3) {
                    for (int b = 0; b < blocks.size(); b++) {
                        byte[] data = blocks.get(b).getData();
                        fos.write(data, 4, data.length - 4);
                    }
                } else {
                    FLVOutputStream flv = new FLVOutputStream(fos);
                    flv.writeHeader(true, false);

                    int ms = (int) (1000.0f / ((float) frameRate));
                    for (int b = 0; b < blocks.size(); b++) {
                        byte[] data = blocks.get(b).getData();
                        if (shead.getSoundFormat() == 2) { //MP3
                            data = Arrays.copyOfRange(data, 4, data.length);
                        }
                        flv.writeTag(new FLVTAG(ms * b, new AUDIODATA(shead.getSoundFormat(), shead.getSoundRate(), shead.getSoundSize(), shead.getSoundType(), data)));
                    }
                }
            }
            return fos.toByteArray();
        }
    }

    private static void writeLE(OutputStream os, long val, int size) throws IOException {
        for (int i = 0; i < size; i++) {
            os.write((int) (val & 0xff));
            val >>= 8;
        }
    }

    private static void createWavFromAdpcm(OutputStream fos, int soundRate, boolean soundSize, boolean soundType, byte[] data) throws IOException {
        byte[] pcmData = AdpcmDecoder.decode(data, soundType);

        ByteArrayOutputStream subChunk1Data = new ByteArrayOutputStream();
        int audioFormat = 1; //PCM
        writeLE(subChunk1Data, audioFormat, 2);
        int numChannels = soundType ? 2 : 1;
        writeLE(subChunk1Data, numChannels, 2);
        int[] rateMap = {5512, 11025, 22050, 44100};
        int sampleRate = rateMap[soundRate];
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
        writeLE(chunks, pcmData.length, 4);
        chunks.write(pcmData);

        fos.write(Utf8Helper.getBytes("RIFF"));
        byte[] chunkBytes = chunks.toByteArray();
        writeLE(fos, 4 + chunkBytes.length, 4);
        fos.write(Utf8Helper.getBytes("WAVE"));
        fos.write(chunkBytes);
        //size1=>16bit*/
    }

    public List<File> exportSounds(AbortRetryIgnoreHandler handler, String outdir, List<Tag> tags, boolean mp3, boolean wave) throws IOException {
        List<File> ret = new ArrayList<>();
        if (tags.isEmpty()) {
            return ret;
        }
        File foutdir = new File(outdir);
        if (!foutdir.exists()) {
            if (!foutdir.mkdirs()) {
                if (!foutdir.exists()) {
                    throw new IOException("Cannot create directory " + outdir);
                }
            }
        }
        for (Tag t : tags) {
            File newfile = null;
            int id = 0;
            if (t instanceof DefineSoundTag) {
                id = ((DefineSoundTag) t).soundId;
            }

            if (t instanceof DefineSoundTag) {
                final DefineSoundTag st = (DefineSoundTag) t;

                if ((st.soundFormat == DefineSoundTag.FORMAT_ADPCM) && wave) {
                    final File file = new File(outdir + File.separator + st.getCharacterExportFileName() + ".wav");
                    newfile = file;
                    new RetryTask(new RunnableIOEx() {
                        @Override
                        public void run() throws IOException {
                            try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
                                createWavFromAdpcm(os, st.soundRate, st.soundSize, st.soundType, st.soundData);
                            }
                        }
                    }, handler).run();
                } else if ((st.soundFormat == DefineSoundTag.FORMAT_MP3) && mp3) {
                    final File file = new File(outdir + File.separator + st.getCharacterExportFileName() + ".mp3");
                    newfile = file;
                    new RetryTask(new RunnableIOEx() {
                        @Override
                        public void run() throws IOException {
                            try (FileOutputStream fos = new FileOutputStream(file)) {
                                fos.write(st.soundData, 2, st.soundData.length - 2);
                            }
                        }
                    }, handler).run();
                } else {
                    final File file = new File(outdir + File.separator + st.getCharacterExportFileName() + ".flv");
                    newfile = file;
                    new RetryTask(new RunnableIOEx() {
                        @Override
                        public void run() throws IOException {
                            FileOutputStream fos = new FileOutputStream(file);
                            try (FLVOutputStream flv = new FLVOutputStream(fos)) {
                                flv.writeHeader(true, false);
                                flv.writeTag(new FLVTAG(0, new AUDIODATA(st.soundFormat, st.soundRate, st.soundSize, st.soundType, st.soundData)));
                            }
                        }
                    }, handler).run();
                }
            }
            if (t instanceof SoundStreamHeadTypeTag) {
                final SoundStreamHeadTypeTag shead = (SoundStreamHeadTypeTag) t;
                final List<SoundStreamBlockTag> blocks = new ArrayList<>();
                List<ContainerItem> objs = new ArrayList<>();
                objs.addAll(this.tags);
                populateSoundStreamBlocks(objs, t, blocks);
                if ((shead.getSoundFormat() == DefineSoundTag.FORMAT_ADPCM) && wave) {
                    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    for (int b = 0; b < blocks.size(); b++) {
                        byte data[] = blocks.get(b).getData();
                        baos.write(data);
                    }
                    final File file = new File(outdir + File.separator + id + ".wav");
                    newfile = file;
                    new RetryTask(new RunnableIOEx() {
                        @Override
                        public void run() throws IOException {
                            try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(file))) {
                                createWavFromAdpcm(fos, shead.getSoundRate(), shead.getSoundSize(), shead.getSoundType(), baos.toByteArray());
                            }
                        }
                    }, handler).run();
                } else if ((shead.getSoundFormat() == DefineSoundTag.FORMAT_MP3) && mp3) {
                    final File file = new File(outdir + File.separator + id + ".mp3");
                    newfile = file;
                    new RetryTask(new RunnableIOEx() {
                        @Override
                        public void run() throws IOException {
                            try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
                                for (int b = 0; b < blocks.size(); b++) {
                                    byte data[] = blocks.get(b).getData();
                                    os.write(data, 2, data.length - 2);
                                }
                            }
                        }
                    }, handler).run();
                } else {
                    final File file = new File(outdir + File.separator + id + ".flv");
                    newfile = file;
                    new RetryTask(new RunnableIOEx() {
                        @Override
                        public void run() throws IOException {
                            try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
                                FLVOutputStream flv = new FLVOutputStream(os);
                                flv.writeHeader(true, false);

                                int ms = (int) (1000.0f / ((float) frameRate));
                                for (int b = 0; b < blocks.size(); b++) {
                                    byte data[] = blocks.get(b).getData();
                                    if (shead.getSoundFormat() == 2) { //MP3
                                        data = Arrays.copyOfRange(data, 4, data.length);
                                    }
                                    flv.writeTag(new FLVTAG(ms * b, new AUDIODATA(shead.getSoundFormat(), shead.getSoundRate(), shead.getSoundSize(), shead.getSoundType(), data)));
                                }
                            }
                        }
                    }, handler).run();
                }
            }
            if (newfile != null) {
                ret.add(newfile);
            }
        }
        return ret;
    }

    public byte[] exportMovie(DefineVideoStreamTag videoStream) throws IOException {
        HashMap<Integer, VideoFrameTag> frames = new HashMap<>();
        populateVideoFrames(videoStream.characterID, this.tags, frames);
        if (frames.isEmpty()) {
            return new byte[0];
        }

        //double ms = 1000.0f / ((float) frameRate);
        ByteArrayOutputStream fos = new ByteArrayOutputStream();
        //CopyOutputStream cos = new CopyOutputStream(fos, new FileInputStream("f:\\trunk\\testdata\\xfl\\xfl\\_obj\\streamvideo 7.flv"));
        OutputStream tos = fos;
        FLVOutputStream flv = new FLVOutputStream(tos);
        flv.writeHeader(false, true);
        //flv.writeTag(new FLVTAG(0, SCRIPTDATA.onMetaData(ms * frames.size() / 1000.0, videoStream.width, videoStream.height, 0, frameRate, videoStream.codecID, 0, 0, false, 0, fileSize)));
        int horizontalAdjustment = 0;
        int verticalAdjustment = 0;
        for (int i = 0; i < frames.size(); i++) {
            VideoFrameTag tag = frames.get(i);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            int frameType = 1;

            if ((videoStream.codecID == DefineVideoStreamTag.CODEC_VP6)
                    || (videoStream.codecID == DefineVideoStreamTag.CODEC_VP6_ALPHA)) {
                SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(tag.videoData), version);
                if (videoStream.codecID == DefineVideoStreamTag.CODEC_VP6_ALPHA) {
                    sis.readUI24(); //offsetToAlpha
                }
                int frameMode = (int) sis.readUB(1);

                if (frameMode == 0) {
                    frameType = 1; //intra
                } else {
                    frameType = 2; //inter
                }
                sis.readUB(6); //qp
                int marker = (int) sis.readUB(1);
                if (frameMode == 0) {
                    int version = (int) sis.readUB(5);
                    int version2 = (int) sis.readUB(2);
                    sis.readUB(1);//interlace
                    if (marker == 1 || version2 == 0) {
                        sis.readUI16();//offset
                    }
                    int dim_y = sis.readUI8();
                    int dim_x = sis.readUI8();
                    sis.readUI8(); //render_y
                    sis.readUI8(); //render_x
                    horizontalAdjustment = (int) (dim_x * Math.ceil(((double) videoStream.width) / (double) dim_x)) - videoStream.width;
                    verticalAdjustment = (int) (dim_y * Math.ceil(((double) videoStream.height) / (double) dim_y)) - videoStream.height;

                }

                SWFOutputStream sos = new SWFOutputStream(baos, version);
                sos.writeUB(4, horizontalAdjustment);
                sos.writeUB(4, verticalAdjustment);
            }
            if (videoStream.codecID == DefineVideoStreamTag.CODEC_SORENSON_H263) {
                SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(tag.videoData), version);
                sis.readUB(17);//pictureStartCode
                sis.readUB(5); //version
                sis.readUB(8); //temporalReference
                int pictureSize = (int) sis.readUB(3); //pictureSize
                if (pictureSize == 0) {
                    sis.readUB(8); //customWidth
                    sis.readUB(8); //customHeight
                }
                if (pictureSize == 1) {
                    sis.readUB(16); //customWidth
                    sis.readUB(16); //customHeight
                }
                int pictureType = (int) sis.readUB(2);
                switch (pictureType) {
                    case 0: //intra
                        frameType = 1; //keyframe
                        break;
                    case 1://inter
                        frameType = 2;
                        break;
                    case 2: //disposable
                        frameType = 3;
                        break;
                }
            }

            baos.write(tag.videoData);
            flv.writeTag(new FLVTAG((int) Math.floor(i * 1000.0f / ((float) frameRate)), new VIDEODATA(frameType, videoStream.codecID, baos.toByteArray())));
        }
        return fos.toByteArray();
    }

    public List<File> exportMovies(AbortRetryIgnoreHandler handler, String outdir, List<Tag> tags) throws IOException {
        List<File> ret = new ArrayList<>();
        if (tags.isEmpty()) {
            return ret;
        }
        File foutdir = new File(outdir);
        if (!foutdir.exists()) {
            if (!foutdir.mkdirs()) {
                if (!foutdir.exists()) {
                    throw new IOException("Cannot create directory " + outdir);
                }
            }
        }
        for (Tag t : tags) {
            if (t instanceof DefineVideoStreamTag) {
                final DefineVideoStreamTag videoStream = (DefineVideoStreamTag) t;
                final File file = new File(outdir + File.separator + ((DefineVideoStreamTag) t).getCharacterExportFileName() + ".flv");
                new RetryTask(new RunnableIOEx() {
                    @Override
                    public void run() throws IOException {
                        try (FileOutputStream fos = new FileOutputStream(file)) {
                            fos.write(exportMovie(videoStream));
                        }
                    }
                }, handler).run();

            }
        }
        return ret;
    }

    public List<File> exportTexts(AbortRetryIgnoreHandler handler, String outdir, List<Tag> tags, final boolean formatted) throws IOException {
        List<File> ret = new ArrayList<>();
        if (tags.isEmpty()) {
            return ret;
        }
        File foutdir = new File(outdir);
        if (!foutdir.exists()) {
            if (!foutdir.mkdirs()) {
                if (!foutdir.exists()) {
                    throw new IOException("Cannot create directory " + outdir);
                }
            }
        }
        for (final Tag t : tags) {
            if (t instanceof TextTag) {
                final File file = new File(outdir + File.separator + ((TextTag) t).getCharacterId() + ".txt");
                new RetryTask(new RunnableIOEx() {
                    @Override
                    public void run() throws IOException {
                        try (FileOutputStream fos = new FileOutputStream(file)) {
                            if (formatted) {
                                fos.write(Utf8Helper.getBytes(((TextTag) t).getFormattedText()));
                            } else {
                                fos.write(Utf8Helper.getBytes(((TextTag) t).getText()));
                            }
                        }
                    }
                }, handler).run();
                ret.add(file);
            }
        }
        return ret;
    }

    public void exportTexts(AbortRetryIgnoreHandler handler, String outdir, boolean formatted) throws IOException {
        exportTexts(handler, outdir, tags, formatted);
    }

    public static List<File> exportShapes(AbortRetryIgnoreHandler handler, String outdir, List<Tag> tags) throws IOException {
        List<File> ret = new ArrayList<>();
        if (tags.isEmpty()) {
            return ret;
        }
        File foutdir = new File(outdir);
        if (!foutdir.exists()) {
            if (!foutdir.mkdirs()) {
                if (!foutdir.exists()) {
                    throw new IOException("Cannot create directory " + outdir);
                }
            }
        }
        loopb:
        for (final Tag t : tags) {
            if (t instanceof ShapeTag) {
                int characterID = 0;
                if (t instanceof CharacterTag) {
                    characterID = ((CharacterTag) t).getCharacterId();
                }
                final File file = new File(outdir + File.separator + characterID + ".svg");
                new RetryTask(new RunnableIOEx() {
                    @Override
                    public void run() throws IOException {
                        try (FileOutputStream fos = new FileOutputStream(file)) {
                            fos.write(Utf8Helper.getBytes(((ShapeTag) t).toSVG()));
                        }
                    }
                }, handler).run();
                ret.add(file);
            }
        }
        return ret;
    }

    public static List<File> exportBinaryData(AbortRetryIgnoreHandler handler, String outdir, List<Tag> tags) throws IOException {
        List<File> ret = new ArrayList<>();
        if (tags.isEmpty()) {
            return ret;
        }
        File foutdir = new File(outdir);
        if (!foutdir.exists()) {
            if (!foutdir.mkdirs()) {
                if (!foutdir.exists()) {
                    throw new IOException("Cannot create directory " + outdir);
                }
            }
        }
        loopb:
        for (final Tag t : tags) {
            if (t instanceof DefineBinaryDataTag) {
                int characterID = ((DefineBinaryDataTag) t).getCharacterId();
                final File file = new File(outdir + File.separator + characterID + ".bin");
                new RetryTask(new RunnableIOEx() {
                    @Override
                    public void run() throws IOException {
                        try (FileOutputStream fos = new FileOutputStream(file)) {
                            fos.write(((DefineBinaryDataTag) t).binaryData);
                        }
                    }
                }, handler).run();
                ret.add(file);
            }
        }
        return ret;
    }

    public List<File> exportImages(AbortRetryIgnoreHandler handler, String outdir, List<Tag> tags) throws IOException {
        List<File> ret = new ArrayList<>();
        if (tags.isEmpty()) {
            return ret;
        }
        File foutdir = new File(outdir);
        if (!foutdir.exists()) {
            if (!foutdir.mkdirs()) {
                if (!foutdir.exists()) {
                    throw new IOException("Cannot create directory " + outdir);
                }
            }
        }
        for (final Tag t : tags) {
            if (t instanceof ImageTag) {
                final File file = new File(outdir + File.separator + ((ImageTag) t).getCharacterId() + "." + ((ImageTag) t).getImageFormat());
                final List<Tag> ttags = this.tags;
                new RetryTask(new RunnableIOEx() {
                    @Override
                    public void run() throws IOException {
                        ImageIO.write(((ImageTag) t).getImage().getBufferedImage(), ((ImageTag) t).getImageFormat().toUpperCase(Locale.ENGLISH), file);
                    }
                }, handler).run();
                ret.add(file);
            }
        }
        return ret;
    }

    public void exportImages(AbortRetryIgnoreHandler handler, String outdir) throws IOException {
        exportImages(handler, outdir, tags);
    }

    public void exportShapes(AbortRetryIgnoreHandler handler, String outdir) throws IOException {
        exportShapes(handler, outdir, tags);
    }

    public void exportBinaryData(AbortRetryIgnoreHandler handler, String outdir) throws IOException {
        exportBinaryData(handler, outdir, tags);
    }
    private final HashMap<String, String> deobfuscated = new HashMap<>();
    private List<MyEntry<DirectValueActionItem, ConstantPool>> allVariableNames = new ArrayList<>();
    private List<GraphSourceItem> allFunctions = new ArrayList<>();
    private HashMap<DirectValueActionItem, ConstantPool> allStrings = new HashMap<>();
    private final HashMap<DirectValueActionItem, String> usageTypes = new HashMap<>();
    private final ActionDeobfuscation deobfuscation = new ActionDeobfuscation();

    private static void getVariables(ConstantPool constantPool, BaseLocalData localData, Stack<GraphTargetItem> stack, List<GraphTargetItem> output, ActionGraphSource code, int ip, List<MyEntry<DirectValueActionItem, ConstantPool>> variables, List<GraphSourceItem> functions, HashMap<DirectValueActionItem, ConstantPool> strings, List<Integer> visited, HashMap<DirectValueActionItem, String> usageTypes, String path) throws InterruptedException {
        boolean debugMode = false;
        while ((ip > -1) && ip < code.size()) {
            if (visited.contains(ip)) {
                break;
            }
            GraphSourceItem ins = code.get(ip);

            if (debugMode) {
                System.err.println("Visit " + ip + ": ofs" + Helper.formatAddress(((Action) ins).getAddress()) + ":" + ((Action) ins).getASMSource(new ArrayList<GraphSourceItem>(), new ArrayList<Long>(), new ArrayList<String>(), code.version, ExportMode.PCODE) + " stack:" + Helper.stackToString(stack, LocalData.create(new ConstantPool())));
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
                    @SuppressWarnings("unchecked")
                    Stack<GraphTargetItem> brStack = (Stack<GraphTargetItem>) stack.clone();
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
        getVariables(null, localData, new Stack<GraphTargetItem>(), new ArrayList<GraphTargetItem>(), code, code.adr2pos(addr), variables, functions, strings, new ArrayList<Integer>(), usageType, path);
    }

    private List<MyEntry<DirectValueActionItem, ConstantPool>> getVariables(List<MyEntry<DirectValueActionItem, ConstantPool>> variables, List<GraphSourceItem> functions, HashMap<DirectValueActionItem, ConstantPool> strings, HashMap<DirectValueActionItem, String> usageType, ASMSource src, String path) throws InterruptedException {
        List<MyEntry<DirectValueActionItem, ConstantPool>> ret = new ArrayList<>();
        List<Action> actions = src.getActions();
        actionsMap.put(src, actions);
        getVariables(variables, functions, strings, usageType, new ActionGraphSource(actions, version, new HashMap<Integer, String>(), new HashMap<String, GraphTargetItem>(), new HashMap<String, GraphTargetItem>()), 0, path);
        return ret;
    }
    private HashMap<ASMSource, List<Action>> actionsMap = new HashMap<>();

    private void getVariables(List<ContainerItem> objs, String path) throws InterruptedException {
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
            }
        }
        for (Tag tag : tags) {
            if (tag instanceof ABCContainerTag) {
                ((ABCContainerTag) tag).getABC().deobfuscateIdentifiers(deobfuscated, renameType, false);
            }
        }
        for (Tag tag : tags) {
            if (tag instanceof SymbolClassTag) {
                SymbolClassTag sc = (SymbolClassTag) tag;
                for (int i = 0; i < sc.names.length; i++) {
                    String newname = deobfuscation.deobfuscateNameWithPackage(sc.names[i], deobfuscated, renameType, deobfuscated);
                    if (newname != null) {
                        sc.names[i] = newname;
                    }
                }
            }
        }
        deobfuscation.deobfuscateInstanceNames(deobfuscated, renameType, tags, new HashMap<String, String>());
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

        List<ContainerItem> objs = new ArrayList<>();
        int ret = 0;
        objs.addAll(tags);
        getVariables(objs, "");
        informListeners("rename", "");
        int fc = 0;
        for (MyEntry<DirectValueActionItem, ConstantPool> it : allVariableNames) {
            String name = it.key.toStringNoH(it.value);
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
                                    String changed = deobfuscation.deobfuscateName(fname, false, "method", deobfuscated, renameType, selected);
                                    if (changed != null) {
                                        deobfuscated.put(fname, changed);
                                    }
                                }
                            }
                        }

                        List<GraphTargetItem> vars = new ArrayList<>();
                        for (MyEntry<GraphTargetItem, GraphTargetItem> item : cti.vars) {
                            vars.add(item.key);
                        }
                        for (MyEntry<GraphTargetItem, GraphTargetItem> item : cti.staticVars) {
                            vars.add(item.key);
                        }
                        for (GraphTargetItem gti : vars) {
                            if (gti instanceof DirectValueActionItem) {
                                DirectValueActionItem dvf = (DirectValueActionItem) gti;
                                String vname = dvf.toStringNoH(null);
                                String changed = deobfuscation.deobfuscateName(vname, false, "attribute", deobfuscated, renameType, selected);
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
                            String changedNameStr2 = deobfuscation.deobfuscateName(changedNameStr, pos == 0, pos == 0 ? "class" : "package", deobfuscated, renameType, selected);
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
                            String changedNameStr2 = deobfuscation.deobfuscateName(changedNameStr, pos == 0, pos == 0 ? "class" : "package", deobfuscated, renameType, selected);
                            if (changedNameStr2 != null) {
                                changedNameStr = changedNameStr2;
                            }
                            ret++;
                            deobfuscated.put(nameStr, changedNameStr);
                            pos++;
                        }
                    }
                }
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
                String changed = deobfuscation.deobfuscateName(f.functionName, false, "function", deobfuscated, renameType, selected);
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
                String changed = deobfuscation.deobfuscateName(f.functionName, false, "function", deobfuscated, renameType, selected);
                if (changed != null) {
                    f.replacedFunctionName = changed;
                    ret++;
                }
            }
        }

        HashSet<String> stringsNoVarH = new HashSet<>();
        List<DirectValueActionItem> allVariableNamesDv = new ArrayList<>();
        for (MyEntry<DirectValueActionItem, ConstantPool> it : allVariableNames) {
            allVariableNamesDv.add(it.key);
        }
        for (DirectValueActionItem ti : allStrings.keySet()) {
            if (!allVariableNamesDv.contains(ti)) {
                stringsNoVarH.add(System.identityHashCode(allStrings.get(ti)) + "_" + ti.toStringNoH(allStrings.get(ti)));
            }
        }

        int vc = 0;
        for (MyEntry<DirectValueActionItem, ConstantPool> it : allVariableNames) {
            vc++;
            String name = it.key.toStringNoH(it.value);
            String changed = deobfuscation.deobfuscateName(name, false, usageTypes.get(it.key), deobfuscated, renameType, selected);
            if (changed != null) {
                boolean addNew = false;
                String h = System.identityHashCode(it.key) + "_" + name;
                if (stringsNoVarH.contains(h)) {
                    addNew = true;
                }
                ActionPush pu = (ActionPush) it.key.src;
                if (pu.replacement == null) {
                    pu.replacement = new ArrayList<>();
                    pu.replacement.addAll(pu.values);
                }
                if (pu.replacement.get(it.key.pos) instanceof ConstantIndex) {
                    ConstantIndex ci = (ConstantIndex) pu.replacement.get(it.key.pos);
                    ConstantPool pool = it.value;
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
                    pu.replacement.set(it.key.pos, changed);
                }
                ret++;
            }
        }
        for (ASMSource src : actionsMap.keySet()) {
            actionsMap.put(src, Action.removeNops(0, actionsMap.get(src), version, 0, ""/*FIXME path*/));
            src.setActions(actionsMap.get(src));
        }
        deobfuscation.deobfuscateInstanceNames(deobfuscated, renameType, tags, selected);
        return ret;
    }

    public void exportFla(AbortRetryIgnoreHandler handler, String outfile, String swfName, String generator, String generatorVerName, String generatorVersion, boolean parallel, FLAVersion version) throws IOException {
        XFLConverter.convertSWF(handler, this, swfName, outfile, true, generator, generatorVerName, generatorVersion, parallel, version);
    }

    public void exportXfl(AbortRetryIgnoreHandler handler, String outfile, String swfName, String generator, String generatorVerName, String generatorVersion, boolean parallel, FLAVersion version) throws IOException {
        XFLConverter.convertSWF(handler, this, swfName, outfile, false, generator, generatorVerName, generatorVersion, parallel, version);
    }

    public static AffineTransform matrixToTransform(MATRIX mat) {
        return new AffineTransform(mat.getScaleXFloat(), mat.getRotateSkew0Float(),
                mat.getRotateSkew1Float(), mat.getScaleYFloat(),
                mat.translateX, mat.translateY);
    }

    private static Cache<SerializableImage> frameCache = Cache.getInstance(false);

    public static SerializableImage getFromCache(String key) {
        if (frameCache.contains(key)) {
            return (SerializableImage) SWF.frameCache.get(key);
        }
        return null;
    }

    public static void putToCache(String key, SerializableImage img) {
        if (Configuration.useFrameCache.get()) {
            frameCache.put(key, img);
        }
    }

    public static void clearImageCache() {
        frameCache.clear();
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

    public static SerializableImage frameToImageGet(Timeline timeline, int frame, RECT displayRect, Stack<Integer> visited, Matrix transformation, ColorTransform colorTransform) {
        String key = "frame_" + frame + "_" + timeline.id + "_" + timeline.swf.hashCode();
        SerializableImage image = getFromCache(key);
        if (image != null) {
            return image;
        }

        if (timeline.frames.isEmpty()) {
            return new SerializableImage(1, 1, SerializableImage.TYPE_INT_ARGB);
        }

        RECT rect = displayRect;
        image = new SerializableImage((int) (rect.getWidth() / SWF.unitDivisor) + 1,
                (int) (rect.getHeight() / SWF.unitDivisor) + 1, SerializableImage.TYPE_INT_ARGB);
        image.fillTransparent();
        Matrix m = new Matrix();
        m.translate(-rect.Xmin, -rect.Ymin);
        frameToImage(timeline, frame, displayRect, visited, image, m, colorTransform);
        putToCache(key, image);
        return image;
    }

    public static void framesToImage(Timeline timeline, List<SerializableImage> ret, int startFrame, int stopFrame, RECT displayRect, int totalFrameCount, Stack<Integer> visited, Matrix transformation, ColorTransform colorTransform) {
        RECT rect = displayRect;
        for (int f = 0; f < timeline.frames.size(); f++) {
            SerializableImage image = new SerializableImage((int) (rect.getWidth() / SWF.unitDivisor) + 1,
                    (int) (rect.getHeight() / SWF.unitDivisor) + 1, SerializableImage.TYPE_INT_ARGB);
            image.fillTransparent();
            Matrix m = new Matrix();
            m.translate(-rect.Xmin, -rect.Ymin);
            frameToImage(timeline, f, displayRect, visited, image, m, colorTransform);
            ret.add(image);
        }
    }

    private static class Clip {

        public Shape shape;
        public int clipDepth;

        public Clip(Shape clip, int clipDepth) {
            this.shape = clip;
            this.clipDepth = clipDepth;
        }

    }

    public static void frameToImage(Timeline timeline, int frame, RECT displayRect, Stack<Integer> visited, SerializableImage image, Matrix transformation, ColorTransform colorTransform) {
        float unzoom = (float) SWF.unitDivisor;
        Frame frameObj = timeline.frames.get(frame);
        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setPaint(frameObj.backgroundColor.toColor());
        g.fill(new Rectangle(image.getWidth(), image.getHeight()));
        g.setTransform(transformation.toTransform());
        List<Clip> clips = new ArrayList<>();
        List<Shape> prevClips = new ArrayList<>();

        for (int i = 1; i <= timeline.getMaxDepth(); i++) {
            for (int c = 0; c < clips.size(); c++) {
                if (clips.get(c).clipDepth == i) {
                    g.setClip(prevClips.get(c));
                    prevClips.remove(c);
                    clips.remove(c);
                }
            }
            if (!frameObj.layers.containsKey(i)) {
                continue;
            }
            DepthState layer = frameObj.layers.get(i);
            if (!timeline.characters.containsKey(layer.characterId)) {
                continue;
            }
            if (!layer.isVisible) {
                continue;
            }
            CharacterTag character = timeline.characters.get(layer.characterId);
            Matrix mat = new Matrix(layer.matrix);
            mat = mat.preConcatenate(transformation);

            if (colorTransform == null) {
                colorTransform = new ColorTransform();
            }

            ColorTransform clrTrans = Helper.deepCopy(colorTransform);
            if (layer.colorTransForm != null && layer.blendMode <= 1) { //Normal blend mode
                clrTrans = colorTransform.merge(layer.colorTransForm);
            }

            boolean showPlaceholder = false;
            if (character instanceof DrawableTag) {
                DrawableTag drawable = (DrawableTag) character;
                SerializableImage img;
                Matrix drawMatrix = new Matrix();
                int dframe = 0 + layer.time % drawable.getNumFrames();
                if (drawable instanceof BoundedTag) {
                    BoundedTag bounded = (BoundedTag) drawable;
                    RECT boundRect = bounded.getRect(timeline.characters, new Stack<Integer>());
                    ExportRectangle rect = new ExportRectangle(boundRect);
                    rect = mat.transform(rect);
                    Matrix m = mat.clone();
                    if (layer.filters != null) {
                        // calculate size after applying the filters
                        double deltaXMax = 0;
                        double deltaYMax = 0;
                        for (FILTER filter : layer.filters) {
                            double x = filter.getDeltaX();
                            double y = filter.getDeltaY();
                            deltaXMax = Math.max(x, deltaXMax);
                            deltaYMax = Math.max(y, deltaYMax);
                        }
                        rect.xMin -= deltaXMax * SWF.unitDivisor;
                        rect.xMax += deltaXMax * SWF.unitDivisor;
                        rect.yMin -= deltaYMax * SWF.unitDivisor;
                        rect.yMax += deltaYMax * SWF.unitDivisor;
                    }

                    rect.xMin = Math.max(0, rect.xMin);
                    rect.yMin = Math.max(0, rect.yMin);

                    int newWidth = (int) (rect.getWidth() / SWF.unitDivisor);
                    int newHeight = (int) (rect.getHeight() / SWF.unitDivisor);
                    int deltaX = (int) (rect.xMin / SWF.unitDivisor);
                    int deltaY = (int) (rect.yMin / SWF.unitDivisor);
                    newWidth = Math.min(image.getWidth() - deltaX, newWidth) + 1;
                    newHeight = Math.min(image.getHeight() - deltaY, newHeight) + 1;

                    if (newWidth <= 0 || newHeight <= 0) {
                        continue;
                    }

                    img = new SerializableImage(newWidth, newHeight, SerializableImage.TYPE_INT_ARGB);
                    img.fillTransparent();

                    m.translate(-rect.xMin, -rect.yMin);
                    drawMatrix.translate(rect.xMin, rect.yMin);

                    drawable.toImage(dframe, layer.ratio, timeline.swf.tags, timeline.characters, visited, img, m, clrTrans);
                } else if (drawable instanceof FontTag) {
                    // only DefineFont tags
                    FontTag fontTag = (FontTag) drawable;
                    img = fontTag.toImage(dframe, layer.ratio, timeline.swf.tags, timeline.characters, visited, transformation, clrTrans);
                } else {
                    throw new Error("Unsupported drawable.");
                }
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
                    Clip clip = new Clip(Helper.imageToShape(mask), layer.clipDepth); //Maybe we can get corrent outline instead converting from image (?)
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
                RECT r = b.getRect(timeline.characters, visited);
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

    public void removeTagFromTimeline(Tag toRemove, List<Tag> timeline) {
        int characterId = 0;
        if (toRemove instanceof CharacterTag) {
            characterId = ((CharacterTag) toRemove).getCharacterId();
        }
        Map<Integer, Integer> stage = new HashMap<>();

        Set<Integer> dependingChars = new HashSet<>();
        if (characterId != 0) {
            dependingChars.add(characterId);
            for (int i = 0; i < timeline.size(); i++) {
                Tag t = timeline.get(i);
                if (t instanceof CharacterIdTag) {
                    CharacterIdTag c = (CharacterIdTag) t;
                    Set<Integer> needed = t.getNeededCharacters();
                    if (needed.contains(characterId)) {
                        dependingChars.add(c.getCharacterId());
                    }
                }

            }
        }

        for (int i = 0; i < timeline.size(); i++) {
            Tag t = timeline.get(i);
            if (t instanceof RemoveTag) {
                RemoveTag rt = (RemoveTag) t;
                int currentCharId = stage.get(rt.getDepth());
                stage.remove(rt.getDepth());
                if (dependingChars.contains(currentCharId)) {
                    timeline.remove(i);
                    i--;
                    continue;
                }
            }
            if (t instanceof PlaceObjectTypeTag) {
                PlaceObjectTypeTag po = (PlaceObjectTypeTag) t;
                int placeCharId = po.getCharacterId();
                int placeDepth = po.getDepth();
                if (placeCharId != 0) {
                    stage.put(placeDepth, placeCharId);
                }
                int currentCharId = stage.get(placeDepth);
                if (dependingChars.contains(currentCharId)) {
                    timeline.remove(i);
                    i--;
                    continue;
                }
            }
            if (t instanceof CharacterIdTag) {
                CharacterIdTag c = (CharacterIdTag) t;
                if (dependingChars.contains(c.getCharacterId())) {
                    timeline.remove(i);
                    i--;
                    continue;
                }
            }
            Set<Integer> needed = t.getNeededCharacters();
            for (int dep : dependingChars) {
                if (needed.contains(dep)) {
                    timeline.remove(i);
                    i--;
                    continue;
                }
            }
            if (t == toRemove) {
                timeline.remove(i);
                i--;
                continue;
            }
            if (t instanceof DefineSpriteTag) {
                DefineSpriteTag spr = (DefineSpriteTag) t;
                removeTagFromTimeline(toRemove, spr.subTags);
            }

        }
    }

    public void removeTag(Tag t) {
        removeTagFromTimeline(t, tags);
    }
}
