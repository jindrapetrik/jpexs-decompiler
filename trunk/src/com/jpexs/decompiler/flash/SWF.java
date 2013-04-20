/*
 *  Copyright (C) 2010-2013 JPEXS
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
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionGraphSource;
import com.jpexs.decompiler.flash.action.swf4.ActionEquals;
import com.jpexs.decompiler.flash.action.swf4.ActionGetVariable;
import com.jpexs.decompiler.flash.action.swf4.ActionIf;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf4.ActionSetVariable;
import com.jpexs.decompiler.flash.action.swf4.Null;
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
import com.jpexs.decompiler.flash.action.treemodel.ConstantPool;
import com.jpexs.decompiler.flash.action.treemodel.DirectValueTreeItem;
import com.jpexs.decompiler.flash.flv.AUDIODATA;
import com.jpexs.decompiler.flash.flv.FLVOutputStream;
import com.jpexs.decompiler.flash.flv.FLVTAG;
import com.jpexs.decompiler.flash.flv.VIDEODATA;
import com.jpexs.decompiler.flash.graph.GraphSourceItem;
import com.jpexs.decompiler.flash.graph.GraphSourceItemContainer;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import com.jpexs.decompiler.flash.gui.FrameNode;
import com.jpexs.decompiler.flash.gui.TagNode;
import com.jpexs.decompiler.flash.helpers.Helper;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.flash.tags.DefineBitsJPEG2Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsJPEG3Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsJPEG4Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsLossless2Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsLosslessTag;
import com.jpexs.decompiler.flash.tags.DefineBitsTag;
import com.jpexs.decompiler.flash.tags.DefineButton2Tag;
import com.jpexs.decompiler.flash.tags.DefineButtonTag;
import com.jpexs.decompiler.flash.tags.DefineSoundTag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.DefineVideoStreamTag;
import com.jpexs.decompiler.flash.tags.DoInitActionTag;
import com.jpexs.decompiler.flash.tags.ExportAssetsTag;
import com.jpexs.decompiler.flash.tags.JPEGTablesTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.SoundStreamBlockTag;
import com.jpexs.decompiler.flash.tags.SoundStreamHeadTypeTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.VideoFrameTag;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.Container;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.RECT;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import javax.imageio.ImageIO;

/**
 * Class representing SWF file
 *
 * @author JPEXS
 */
public class SWF {

    /**
     * Default version of SWF file format
     */
    public static final int DEFAULT_VERSION = 10;
    /**
     * Tags inside of file
     */
    public List<Tag> tags = new ArrayList<Tag>();
    /**
     * Rectangle for the display
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
    public byte lzmaProperties[];

    /**
     * Gets all tags with specified id
     *
     * @param tagId Identificator of tag type
     * @return List of tags
     */
    public List<Tag> getTagData(int tagId) {
        List<Tag> ret = new ArrayList<Tag>();
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
                os.write('F');
            }
            os.write('W');
            os.write('S');
            os.write(version);
            byte data[] = baos.toByteArray();
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
                    byte udata[] = new byte[4];
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

    public SWF(InputStream is) throws IOException {
        this(is, null);
    }

    /**
     * Construct SWF from stream
     *
     * @param is Stream to read SWF from
     * @throws IOException
     */
    public SWF(InputStream is, PercentListener listener) throws IOException {
        byte hdr[] = new byte[3];
        is.read(hdr);
        String shdr = new String(hdr);
        if ((!shdr.equals("FWS")) && (!shdr.equals("CWS")) && (!shdr.equals("ZWS"))) {
            throw new IOException("Invalid SWF file");
        }
        version = is.read();
        SWFInputStream sis = new SWFInputStream(is, version, 4);
        fileSize = sis.readUI32();

        if (hdr[0] == 'C') {
            sis = new SWFInputStream(new InflaterInputStream(is), version, 8);
            compressed = true;
        }

        if (hdr[0] == 'Z') {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            long outSize = sis.readUI32();
            int propertiesSize = 5;
            lzmaProperties = new byte[propertiesSize];
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
        int tmpFirstByetOfFrameRate = sis.readUI8();
        frameRate = sis.readUI8();
        frameCount = sis.readUI16();
        tags = sis.readTagList(0);
    }

    /**
     * Compress SWF file
     *
     * @param fis Input stream
     * @param fos Output stream
     */
    public static boolean fws2cws(InputStream fis, OutputStream fos) {
        try {
            byte swfHead[] = new byte[8];
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
        } catch (FileNotFoundException ex) {
            return false;
        } catch (IOException ex) {
            return false;
        }
        return true;
    }

    public static boolean decompress(InputStream fis, OutputStream fos) {
        try {
            byte hdr[] = new byte[3];
            fis.read(hdr);
            String shdr = new String(hdr);
            if (shdr.equals("CWS")) {
                int version = fis.read();
                SWFInputStream sis = new SWFInputStream(fis, version, 4);
                long fileSize = sis.readUI32();
                SWFOutputStream sos = new SWFOutputStream(fos, version);
                sos.write("FWS".getBytes());
                sos.writeUI8(version);
                sos.writeUI32(fileSize);
                InflaterInputStream iis = new InflaterInputStream(fis);
                int i;
                while ((i = iis.read()) != -1) {
                    fos.write(i);
                }

                fis.close();
                fos.close();
            } else if (shdr.equals("ZWS")) {
                int version = fis.read();
                SWFInputStream sis = new SWFInputStream(fis, version, 4);
                long fileSize = sis.readUI32();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                long outSize = sis.readUI32();
                int propertiesSize = 5;
                byte lzmaProperties[] = new byte[propertiesSize];
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
                SWFOutputStream sos = new SWFOutputStream(fos, version);
                sos.write("FWS".getBytes());
                sos.write(version);
                sos.writeUI32(fileSize);
                sos.write(baos.toByteArray());
                sos.close();
                fis.close();
                fos.close();
            } else {
                return false;
            }
        } catch (FileNotFoundException ex) {
            return false;
        } catch (IOException ex) {
            return false;
        }
        return true;
    }

    public boolean exportAS3Class(String className, String outdir, boolean isPcode) throws Exception {
        List<ABCContainerTag> abcTags = new ArrayList<ABCContainerTag>();

        for (Tag t : tags) {
            if (t instanceof ABCContainerTag) {
                ABCContainerTag cnt = (ABCContainerTag) t;
                abcTags.add(cnt);
            }
        }
        for (int i = 0; i < abcTags.size(); i++) {
            ABC abc = abcTags.get(i).getABC();
            int scriptIndex = abc.findScriptByPath(className);
            if (scriptIndex > -1) {
                String cnt = "";
                if (abc.script_info.length > 1) {
                    cnt = "script " + (i + 1) + "/" + abc.script_info.length + " ";
                }
                String path = abc.script_info[scriptIndex].getPath(abc);
                String packageName = path.substring(0, path.lastIndexOf("."));
                if (packageName.equals("")) {
                    path = path.substring(1);
                }
                String exStr = "Exporting " + "tag " + (i + 1) + "/" + abcTags.size() + " " + cnt + path + " ...";
                informListeners("export", exStr);
                abc.script_info[scriptIndex].export(abc, abcTags, outdir, isPcode);
                return true;
            }
        }
        return false;
    }

    public boolean exportActionScript(String outdir, boolean isPcode) throws Exception {
        boolean asV3Found = false;
        final EventListener evl = new EventListener() {
            @Override
            public void handleEvent(String event, Object data) {
                if (event.equals("export")) {
                    informListeners(event, data);
                }
            }
        };
        List<ABCContainerTag> abcTags = new ArrayList<ABCContainerTag>();
        for (Tag t : tags) {
            if (t instanceof ABCContainerTag) {
                abcTags.add((ABCContainerTag) t);
                asV3Found = true;
            }
        }
        for (int i = 0; i < abcTags.size(); i++) {
            ABCContainerTag t = abcTags.get(i);
            t.getABC().addEventListener(evl);
            t.getABC().export(outdir, isPcode, abcTags, "tag " + (i + 1) + "/" + abcTags.size() + " ");
        }

        if (!asV3Found) {
            List<Object> list2 = new ArrayList<Object>();
            list2.addAll(tags);
            List<TagNode> list = createASTagList(list2, null);

            TagNode.setExport(list, true);
            if (!outdir.endsWith(File.separator)) {
                outdir += File.separator;
            }
            outdir += "scripts" + File.separator;
            return TagNode.exportNodeAS(list, outdir, isPcode, evl);
        }
        return asV3Found;
    }

    public static List<TagNode> createASTagList(List<Object> list, Object parent) {
        List<TagNode> ret = new ArrayList<TagNode>();
        int frame = 1;
        List<TagNode> frames = new ArrayList<TagNode>();

        List<ExportAssetsTag> exportAssetsTags = new ArrayList<ExportAssetsTag>();
        for (Object t : list) {
            if (t instanceof ExportAssetsTag) {
                exportAssetsTags.add((ExportAssetsTag) t);
            }
            if (t instanceof ShowFrameTag) {
                TagNode tti = new TagNode(new FrameNode(frame, parent, false));

                for (int r = ret.size() - 1; r >= 0; r--) {
                    if (!(ret.get(r).tag instanceof DefineSpriteTag)) {
                        if (!(ret.get(r).tag instanceof DefineButtonTag)) {
                            if (!(ret.get(r).tag instanceof DefineButton2Tag)) {
                                if (!(ret.get(r).tag instanceof DoInitActionTag)) {
                                    tti.subItems.add(ret.get(r));
                                    ret.remove(r);
                                }
                            }
                        }
                    }
                }
                frame++;
                frames.add(tti);
            } else if (t instanceof ASMSource) {
                TagNode tti = new TagNode(t);
                ret.add(tti);
            } else if (t instanceof Container) {
                if (((Container) t).getItemCount() > 0) {

                    TagNode tti = new TagNode(t);
                    List<Object> subItems = ((Container) t).getSubItems();

                    tti.subItems = createASTagList(subItems, t);
                    ret.add(tti);
                }
            }

        }
        ret.addAll(frames);
        for (int i = ret.size() - 1; i >= 0; i--) {
            if (ret.get(i).tag instanceof DefineSpriteTag) {
                ((DefineSpriteTag) ret.get(i).tag).exportAssetsTags = exportAssetsTags;
            }
            if (ret.get(i).tag instanceof DefineButtonTag) {
                ((DefineButtonTag) ret.get(i).tag).exportAssetsTags = exportAssetsTags;
            }
            if (ret.get(i).tag instanceof DefineButton2Tag) {
                ((DefineButton2Tag) ret.get(i).tag).exportAssetsTags = exportAssetsTags;
            }
            if (ret.get(i).tag instanceof DoInitActionTag) {
                ((DoInitActionTag) ret.get(i).tag).exportAssetsTags = exportAssetsTags;
            }
            if (ret.get(i).tag instanceof ASMSource) {
                ASMSource ass = (ASMSource) ret.get(i).tag;
                if (ass.containsSource()) {
                    continue;
                }
            }
            if (ret.get(i).subItems.isEmpty()) {
                ret.remove(i);
            }
        }
        return ret;
    }
    private HashSet<EventListener> listeners = new HashSet<EventListener>();

    public final void addEventListener(EventListener listener) {
        listeners.add(listener);
    }

    public final void removeEventListener(EventListener listener) {
        listeners.remove(listener);
    }

    protected void informListeners(String event, Object data) {
        for (EventListener listener : listeners) {
            listener.handleEvent(event, data);
        }
    }

    private static String getImageFormat(byte data[]) {
        if (hasErrorHeader(data)) {
            return "jpg";
        }
        if (data.length > 2 && ((data[0] & 0xff) == 0xff) && ((data[1] & 0xff) == 0xd8)) {
            return "jpg";
        }
        if (data.length > 6 && ((data[0] & 0xff) == 0x47) && ((data[1] & 0xff) == 0x49) && ((data[2] & 0xff) == 0x46) && ((data[3] & 0xff) == 0x38) && ((data[4] & 0xff) == 0x39) && ((data[5] & 0xff) == 0x61)) {
            return "gif";
        }

        if (data.length > 8 && ((data[0] & 0xff) == 0x89) && ((data[1] & 0xff) == 0x50) && ((data[2] & 0xff) == 0x4e) && ((data[3] & 0xff) == 0x47) && ((data[4] & 0xff) == 0x0d) && ((data[5] & 0xff) == 0x0a) && ((data[6] & 0xff) == 0x1a) && ((data[7] & 0xff) == 0x0a)) {
            return "png";
        }

        return "unk";
    }

    public static boolean hasErrorHeader(byte data[]) {
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

    public static void populateSoundStreamBlocks(List<Object> tags, Tag head, List<SoundStreamBlockTag> output) {
        boolean found = false;
        for (Object t : tags) {
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

    public void populateVideoFrames(int streamId, List<Object> tags, HashMap<Integer, VideoFrameTag> output) {
        for (Object t : tags) {
            if (t instanceof VideoFrameTag) {
                output.put(((VideoFrameTag) t).frameNum, (VideoFrameTag) t);
            }
            if (t instanceof Container) {
                populateVideoFrames(streamId, ((Container) t).getSubItems(), output);
            }
        }
    }

    public void exportMovies(String outdir) throws IOException {
        exportMovies(outdir, tags);
    }

    public void exportSounds(String outdir, boolean mp3) throws IOException {
        exportSounds(outdir, tags, mp3);
    }

    public void exportSounds(String outdir, List<Tag> tags, boolean mp3) throws IOException {
        if (tags.isEmpty()) {
            return;
        }
        if (!(new File(outdir)).exists()) {
            (new File(outdir)).mkdirs();
        }
        List<Object> os = new ArrayList<Object>(this.tags);
        for (Tag t : tags) {
            FileOutputStream fos = null;
            try {
                int id = 0;
                if (t instanceof DefineSoundTag) {
                    id = ((DefineSoundTag) t).soundId;
                }
                if (mp3) {
                }

                if (t instanceof DefineSoundTag) {
                    DefineSoundTag st = (DefineSoundTag) t;
                    if ((st.soundFormat == 2) && mp3) {
                        fos = new FileOutputStream(outdir + File.separator + id + ".mp3");
                        fos.write(st.soundData);
                    } else {
                        fos = new FileOutputStream(outdir + File.separator + id + ".flv");
                        FLVOutputStream flv = new FLVOutputStream(fos);
                        flv.writeHeader(true, false);
                        flv.writeTag(new FLVTAG(0, new AUDIODATA(st.soundFormat, st.soundRate, st.soundSize, st.soundType, st.soundData)));
                    }
                }
                if (t instanceof SoundStreamHeadTypeTag) {
                    SoundStreamHeadTypeTag shead = (SoundStreamHeadTypeTag) t;
                    List<SoundStreamBlockTag> blocks = new ArrayList<SoundStreamBlockTag>();
                    List<Object> objs = new ArrayList<Object>(this.tags);
                    populateSoundStreamBlocks(objs, t, blocks);
                    if ((shead.getSoundFormat() == 2) && mp3) {
                        fos = new FileOutputStream(outdir + File.separator + id + ".mp3");
                        for (int b = 0; b < blocks.size(); b++) {
                            byte data[] = blocks.get(b).getData(SWF.DEFAULT_VERSION);
                            fos.write(data, 4, data.length - 4);
                        }
                    } else {
                        fos = new FileOutputStream(outdir + File.separator + id + ".flv");
                        FLVOutputStream flv = new FLVOutputStream(fos);
                        flv.writeHeader(true, false);

                        int ms = (int) (1000.0f / ((float) frameRate));
                        for (int b = 0; b < blocks.size(); b++) {
                            byte data[] = blocks.get(b).getData(SWF.DEFAULT_VERSION);
                            if (shead.getSoundFormat() == 2) { //MP3
                                data = Arrays.copyOfRange(data, 4, data.length);
                            }
                            flv.writeTag(new FLVTAG(ms * b, new AUDIODATA(shead.getSoundFormat(), shead.getSoundRate(), shead.getSoundSize(), shead.getSoundType(), data)));
                        }
                    }
                }
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (Exception ex) {
                        //ignore
                    }
                }
            }

        }
    }

    public void exportMovies(String outdir, List<Tag> tags) throws IOException {
        if (tags.isEmpty()) {
            return;
        }
        if (!(new File(outdir)).exists()) {
            (new File(outdir)).mkdirs();
        }
        List<Object> os = new ArrayList<Object>(this.tags);
        for (Tag t : tags) {
            if (t instanceof DefineVideoStreamTag) {
                DefineVideoStreamTag videoStream = (DefineVideoStreamTag) t;
                HashMap<Integer, VideoFrameTag> frames = new HashMap<Integer, VideoFrameTag>();
                populateVideoFrames(videoStream.characterID, os, frames);

                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(outdir + File.separator + ((DefineVideoStreamTag) t).characterID + ".flv");
                    FLVOutputStream flv = new FLVOutputStream(fos);
                    flv.writeHeader(false, true);
                    int ms = (int) (1000.0f / ((float) frameRate));
                    for (int i = 0; i < frames.size(); i++) {
                        VideoFrameTag tag = frames.get(i);
                        int frameType = 0;
                        if (videoStream.codecID == 2) { //H263
                            SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(tag.videoData), SWF.DEFAULT_VERSION);
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
                        flv.writeTag(new FLVTAG(i * ms, new VIDEODATA(frameType, videoStream.codecID, tag.videoData)));
                    }


                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (Exception ex) {
                            //ignore
                        }
                    }
                }
            }
        }
    }

    public static void exportShapes(String outdir, List<Tag> tags) throws IOException {
        if (tags.isEmpty()) {
            return;
        }
        if (!(new File(outdir)).exists()) {
            (new File(outdir)).mkdirs();
        }
        for (Tag t : tags) {
            if (t instanceof ShapeTag) {
                int characterID = 0;
                if (t instanceof CharacterTag) {
                    characterID = ((CharacterTag) t).getCharacterID();
                }
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(outdir + File.separator + characterID + ".svg");
                    fos.write(((ShapeTag) t).toSVG().getBytes());
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (Exception ex) {
                            //ignore
                        }
                    }
                }
            }
        }
    }

    public static void exportImages(String outdir, List<Tag> tags, JPEGTablesTag jtt) throws IOException {
        if (tags.isEmpty()) {
            return;
        }
        if (!(new File(outdir)).exists()) {
            (new File(outdir)).mkdirs();
        }
        for (Tag t : tags) {
            if ((t instanceof DefineBitsJPEG2Tag) || (t instanceof DefineBitsJPEG3Tag) || (t instanceof DefineBitsJPEG4Tag)) {
                byte imageData[] = null;
                int characterID = 0;
                if (t instanceof DefineBitsJPEG2Tag) {
                    imageData = ((DefineBitsJPEG2Tag) t).imageData;
                    characterID = ((DefineBitsJPEG2Tag) t).characterID;
                }
                if (t instanceof DefineBitsJPEG3Tag) {
                    imageData = ((DefineBitsJPEG3Tag) t).imageData;
                    characterID = ((DefineBitsJPEG3Tag) t).characterID;
                }
                if (t instanceof DefineBitsJPEG4Tag) {
                    imageData = ((DefineBitsJPEG4Tag) t).imageData;
                    characterID = ((DefineBitsJPEG4Tag) t).characterID;
                }

                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(outdir + File.separator + characterID + "." + getImageFormat(imageData));
                    if (hasErrorHeader(imageData)) {
                        fos.write(imageData, 4, imageData.length - 4);
                    } else {
                        fos.write(imageData);
                    }
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (Exception ex) {
                            //ignore
                        }
                    }
                }
            }
            if (t instanceof DefineBitsLosslessTag) {
                DefineBitsLosslessTag dbl = (DefineBitsLosslessTag) t;
                ImageIO.write(dbl.getImage(), "PNG", new File(outdir + File.separator + dbl.characterID + ".png"));
            }
            if (t instanceof DefineBitsLossless2Tag) {
                DefineBitsLossless2Tag dbl = (DefineBitsLossless2Tag) t;

                ImageIO.write(dbl.getImage(), "PNG", new File(outdir + File.separator + dbl.characterID + ".png"));
            }
            if ((jtt != null) && (t instanceof DefineBitsTag)) {
                DefineBitsTag dbt = (DefineBitsTag) t;
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(outdir + File.separator + dbt.characterID + ".jpg");
                    fos.write(dbt.getFullImageData(jtt));
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (Exception ex) {
                            //ignore
                        }
                    }
                }
            }
        }
    }

    public void exportImages(String outdir) throws IOException {
        JPEGTablesTag jtt = null;
        for (Tag t : tags) {
            if (t instanceof JPEGTablesTag) {
                jtt = (JPEGTablesTag) t;
            }
        }
        exportImages(outdir, tags, jtt);
    }

    public void exportShapes(String outdir) throws IOException {
        exportShapes(outdir, tags);
    }
    public static final String[] reservedWords = {
        "as", "break", "case", "catch", "class", "const", "continue", "default", "delete", "do", "each", "else",
        "extends", "false", "finally", "for", "function", "get", "if", "implements", "import", "in", "instanceof",
        "interface", "internal", "is", "native", "new", "null", "override", "package", "private", "protected", "public",
        "return", "set", "super", "switch", "this", "throw", "true", "try", "typeof", "use", "var", /*"void",*/ "while",
        "with", "dynamic", "default", "final", "in"};

    private boolean isReserved(String s) {
        for (String rw : reservedWords) {
            if (rw.equals(s.trim())) {
                return true;
            }
        }
        return false;
    }
    private HashMap<String, String> deobfuscated = new HashMap<String, String>();
    private Random rnd = new Random();
    private final int DEFAULT_FOO_SIZE = 10;
    public static final String validFirstCharacters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_";
    public static final String validNextCharacters = validFirstCharacters + "0123456789";
    public static final String fooCharacters = "bcdfghjklmnpqrstvwz";
    public static final String fooJoinCharacters = "aeiouy";
    private HashMap<DirectValueTreeItem, ConstantPool> allVariableNames = new HashMap<DirectValueTreeItem, ConstantPool>();
    private HashSet<String> allVariableNamesStr = new HashSet<String>();
    private List<GraphSourceItem> allFunctions = new ArrayList<GraphSourceItem>();

    private String fooString(String orig, boolean firstUppercase, int rndSize) {
        boolean exists;
        String ret;
        loopfoo:
        do {
            exists = false;
            int len = 3 + rnd.nextInt(rndSize - 3);
            ret = "";
            for (int i = 0; i < len; i++) {
                String c = "";
                if ((i % 2) == 0) {
                    c = "" + fooCharacters.charAt(rnd.nextInt(fooCharacters.length()));
                } else {
                    c = "" + fooJoinCharacters.charAt(rnd.nextInt(fooJoinCharacters.length()));
                }
                if (i == 0 && firstUppercase) {
                    c = c.toUpperCase();
                }
                ret += c;
            }
            if (allVariableNamesStr.contains(ret)) {
                exists = true;
                rndSize = rndSize + 1;
                continue loopfoo;
            }
            if (isReserved(ret)) {
                exists = true;
                rndSize = rndSize + 1;
                continue;
            }
            if (deobfuscated.containsValue(ret)) {
                exists = true;
                rndSize = rndSize + 1;
                continue;
            }
        } while (exists);
        deobfuscated.put(orig, ret);
        return ret;
    }

    public String deobfuscateName(HashMap<String, String> namesMap, String s, boolean firstUppercase) {
        boolean isValid = true;
        if (isReserved(s)) {
            isValid = false;
        }

        if (isValid) {
            for (int i = 0; i < s.length(); i++) {
                if (s.charAt(i) > 127) {
                    isValid = false;
                    break;
                }
            }
        }

        if (isValid) {
            Pattern pat = Pattern.compile("^[" + Pattern.quote(validFirstCharacters) + "]" + "[" + Pattern.quote(validFirstCharacters + validNextCharacters) + "]*$");
            if (!pat.matcher(s).matches()) {
                isValid = false;
            }
        }

        if (!isValid) {
            if (namesMap.containsKey(s)) {
                return namesMap.get(s);
            } else {
                String ret = fooString(s, firstUppercase, DEFAULT_FOO_SIZE);
                return ret;
            }
        }
        return null;
    }

    private static void getVariables(ConstantPool constantPool, List localData, Stack<GraphTargetItem> stack, List<GraphTargetItem> output, ActionGraphSource code, int ip, int lastIp, HashMap<DirectValueTreeItem, ConstantPool> variables, List<GraphSourceItem> functions, List<Integer> visited) {
        boolean debugMode = false;
        while ((ip > -1) && ip < code.size()) {
            if (visited.contains(ip)) {
                break;
            }

            lastIp = ip;
            GraphSourceItem ins = code.get(ip);

            if (debugMode) {
                System.err.println("Visit " + ip + ": ofs" + Helper.formatAddress(((Action) ins).getAddress()) + ":" + ((Action) ins).getASMSource(new ArrayList<GraphSourceItem>(), new ArrayList<Long>(), new ArrayList<String>(), code.version, false) + " stack:" + Helper.stackToString(stack, Helper.toList(new ConstantPool())));
            }
            if (ins.isIgnored()) {
                ip++;
                continue;
            }

            GraphTargetItem name = null;
            if ((ins instanceof ActionGetVariable)
                    || (ins instanceof ActionGetMember)
                    || (ins instanceof ActionDefineLocal2)
                    || (ins instanceof ActionNewMethod)
                    || (ins instanceof ActionNewObject)
                    || (ins instanceof ActionCallMethod)
                    || (ins instanceof ActionCallFunction)) {
                name = stack.peek();
            }


            if ((ins instanceof ActionDefineFunction) || (ins instanceof ActionDefineFunction2)) {
                functions.add(ins);
            }

            if (ins instanceof GraphSourceItemContainer) {
                GraphSourceItemContainer cnt = (GraphSourceItemContainer) ins;
                List<Long> cntSizes = cnt.getContainerSizes();
                long addr = code.pos2adr(ip + 1);
                for (Long size : cntSizes) {
                    if (size == 0) {
                        continue;
                    }
                    ip = code.adr2pos(addr);
                    addr += size;
                    int nextip = code.adr2pos(addr);
                    getVariables(variables, functions, new ActionGraphSource(code.getActions().subList(ip, nextip), code.version, new HashMap<Integer, String>(), new HashMap<String, GraphTargetItem>(), new HashMap<String, GraphTargetItem>()), 0);
                    ip = nextip;
                }
                ((GraphSourceItemContainer) ins).translateContainer(new ArrayList<List<GraphTargetItem>>(), stack, output, new HashMap<Integer, String>(), new HashMap<String, GraphTargetItem>(), new HashMap<String, GraphTargetItem>());
                continue;
            }

            if ((ins instanceof ActionSetVariable) || (ins instanceof ActionSetMember) || (ins instanceof ActionDefineLocal)) {
                name = stack.get(stack.size() - 2);
            }
            if (name instanceof DirectValueTreeItem) {
                variables.put((DirectValueTreeItem) name, constantPool);
            }

            //for..in return
            if (((ins instanceof ActionEquals) || (ins instanceof ActionEquals2)) && (stack.size() == 1) && (stack.peek() instanceof DirectValueTreeItem)) {
                stack.push(new DirectValueTreeItem(null, 0, new Null(), new ArrayList<String>()));
            }

            if (ins instanceof ActionConstantPool) {
                constantPool = new ConstantPool(((ActionConstantPool) ins).constantPool);
            }

            try {
                ins.translate(localData, stack, output);
            } catch (Exception ex) {
                Logger.getLogger(SWF.class.getName()).log(Level.SEVERE, "Error during getting variables", ex);
            }
            if (ins.isExit()) {
                break;
            }

            if (ins.isBranch() || ins.isJump()) {
                if (ins instanceof ActionIf) {
                    stack.pop();
                }
                visited.add(ip);
                List<Integer> branches = ins.getBranches(code);
                for (int b : branches) {
                    Stack<GraphTargetItem> brStack = (Stack<GraphTargetItem>) stack.clone();
                    if (b >= 0) {
                        getVariables(constantPool, localData, brStack, output, code, b, ip, variables, functions, visited);
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

    private static void getVariables(HashMap<DirectValueTreeItem, ConstantPool> variables, List<GraphSourceItem> functions, ActionGraphSource code, int addr) {
        List localData = Helper.toList(new HashMap<Integer, String>(), new HashMap<String, GraphTargetItem>(), new HashMap<String, GraphTargetItem>());
        try {
            getVariables(null, localData, new Stack<GraphTargetItem>(), new ArrayList<GraphTargetItem>(), code, code.adr2pos(addr), 0, variables, functions, new ArrayList<Integer>());
        } catch (Exception ex) {
            Logger.getLogger(SWF.class.getName()).log(Level.SEVERE, "Getting variables error", ex);
        }
    }

    private HashMap<DirectValueTreeItem, ConstantPool> getVariables(HashMap<DirectValueTreeItem, ConstantPool> variables, List<GraphSourceItem> functions, ASMSource src) {
        HashMap<DirectValueTreeItem, ConstantPool> ret = new HashMap<DirectValueTreeItem, ConstantPool>();
        List<Action> actions = src.getActions(version);
        actionsMap.put(src, actions);
        List<GraphSourceItem> ss = new ArrayList<GraphSourceItem>();
        ss.addAll(actions);
        getVariables(variables, functions, new ActionGraphSource(ss, version, new HashMap<Integer, String>(), new HashMap<String, GraphTargetItem>(), new HashMap<String, GraphTargetItem>()), 0);
        return ret;
    }
    private HashMap<ASMSource, List<Action>> actionsMap = new HashMap<ASMSource, List<Action>>();

    private void getVariables(List<Object> objs, String path) {
        for (Object o : objs) {
            if (o instanceof ASMSource) {
                informListeners("getVariables", path + "/" + o.toString());
                getVariables(allVariableNames, allFunctions, (ASMSource) o);
            }
            if (o instanceof Container) {
                getVariables(((Container) o).getSubItems(), path + "/" + o.toString());
            }
        }
    }

    public int deobfuscateAS2Identifiers() {
        actionsMap = new HashMap<ASMSource, List<Action>>();
        allFunctions = new ArrayList<GraphSourceItem>();
        allVariableNames = new HashMap<DirectValueTreeItem, ConstantPool>();
        List<Object> objs = new ArrayList<Object>();
        int ret = 0;
        objs.addAll(tags);
        getVariables(objs, "");
        for (GraphSourceItem fun : allFunctions) {
            if (fun instanceof ActionDefineFunction) {
                ActionDefineFunction f = (ActionDefineFunction) fun;
                String changed = deobfuscateName(deobfuscated, f.functionName, false);
                if (changed != null) {
                    f.replacedFunctionName = changed;
                }
            }
            if (fun instanceof ActionDefineFunction2) {
                ActionDefineFunction2 f = (ActionDefineFunction2) fun;
                String changed = deobfuscateName(deobfuscated, f.functionName, false);
                if (changed != null) {
                    f.replacedFunctionName = changed;
                }
            }
        }
        for (DirectValueTreeItem ti : allVariableNames.keySet()) {
            String name = ti.toStringNoH(allVariableNames.get(ti));
            allVariableNamesStr.add(name);
        }
        for (DirectValueTreeItem ti : allVariableNames.keySet()) {
            String name = ti.toStringNoH(allVariableNames.get(ti));
            String changed = deobfuscateName(deobfuscated, name, false);
            if (changed != null) {
                ActionPush pu = (ActionPush) ti.src;
                if (pu.replacement == null) {
                    pu.replacement = new ArrayList<Object>();
                    pu.replacement.addAll(pu.values);
                }
                pu.replacement.set(ti.pos, changed);
                ret++;
            }
        }
        for (ASMSource src : actionsMap.keySet()) {
            actionsMap.put(src, Action.removeNops(0, actionsMap.get(src), version, 0));
            src.setActions(actionsMap.get(src), version);
        }
        return ret;
    }
}
