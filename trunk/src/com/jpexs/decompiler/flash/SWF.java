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

import com.jpexs.decompiler.flash.flv.FLVOutputStream;
import SevenZip.Compression.LZMA.Encoder;
import com.jpexs.decompiler.flash.flv.AUDIODATA;
import com.jpexs.decompiler.flash.flv.FLVTAG;
import com.jpexs.decompiler.flash.flv.VIDEODATA;
import com.jpexs.decompiler.flash.gui.TagNode;
import com.jpexs.decompiler.flash.tags.DefineBitsJPEG2Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsJPEG3Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsJPEG4Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsLossless2Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsLosslessTag;
import com.jpexs.decompiler.flash.tags.DefineBitsTag;
import com.jpexs.decompiler.flash.tags.DefineSoundTag;
import com.jpexs.decompiler.flash.tags.DefineVideoStreamTag;
import com.jpexs.decompiler.flash.tags.DoABCTag;
import com.jpexs.decompiler.flash.tags.JPEGTablesTag;
import com.jpexs.decompiler.flash.tags.SoundStreamBlockTag;
import com.jpexs.decompiler.flash.tags.SoundStreamHead2Tag;
import com.jpexs.decompiler.flash.tags.SoundStreamHeadTag;
import com.jpexs.decompiler.flash.tags.SoundStreamHeadTypeTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.VideoFrameTag;
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

   /**
    * Decompress SWF file
    *
    * @param fis Input stream
    * @param fos Output stream
    */
   public static boolean cws2fws(InputStream fis, OutputStream fos) {
      try {
         byte swfHead[] = new byte[8];
         fis.read(swfHead);
         InflaterInputStream iis = new InflaterInputStream(fis);
         if (swfHead[0] != 'C') {
            fis.close();
            return false;
         }
         swfHead[0] = 'F';
         fos.write(swfHead);
         int i;
         while ((i = iis.read()) != -1) {
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

   /**
    * Decompress LZMA compressed SWF file
    *
    * @param fis Input stream
    * @param fos Output stream
    */
   public static boolean zws2fws(InputStream fis, OutputStream fos) {
      try {
         byte hdr[] = new byte[3];
         fis.read(hdr);
         String shdr = new String(hdr);
         if (!shdr.equals("ZWS")) {
            return false;
         }
         int version = fis.read();
         SWFInputStream sis = new SWFInputStream(fis, version, 4);
         long fileSize = sis.readUI32();

         if (hdr[0] == 'Z') {
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

   public boolean exportActionScript(String outdir, boolean isPcode) throws Exception {
      boolean asV3Found = false;
      final EventListener evl = new EventListener() {
         public void handleEvent(String event, Object data) {
            if (event.equals("export")) {
               informListeners(event, data);
            }
         }
      };
      List<DoABCTag> abcTags = new ArrayList<DoABCTag>();
      for (Tag t : tags) {
         if (t instanceof DoABCTag) {
            abcTags.add((DoABCTag) t);
            asV3Found = true;
         }
      }
      for (int i = 0; i < abcTags.size(); i++) {
         DoABCTag t = abcTags.get(i);
         t.abc.addEventListener(evl);
         t.abc.export(outdir, isPcode, abcTags, "tag " + (i + 1) + "/" + abcTags.size() + " ");
      }
      for (DoABCTag t : abcTags) {
      }

      if (!asV3Found) {
         List<Object> list2 = new ArrayList<Object>();
         list2.addAll(tags);
         List<TagNode> list = TagNode.createTagList(list2);
         TagNode.setExport(list, true);
         return TagNode.exportNodeAS(list, outdir, isPcode);
      }
      return asV3Found;
   }
   protected HashSet<EventListener> listeners = new HashSet<EventListener>();

   public void addEventListener(EventListener listener) {
      listeners.add(listener);
   }

   public void removeEventListener(EventListener listener) {
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

   public void exportSounds(String outdir) throws IOException {
      exportSounds(outdir, tags);
   }

   public void exportSounds(String outdir, List<Tag> tags) throws IOException {
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
            fos = new FileOutputStream(outdir + File.separator + id + ".flv");
            FLVOutputStream flv = new FLVOutputStream(fos);
            flv.writeHeader(true, false);
            if (t instanceof DefineSoundTag) {
               DefineSoundTag st = (DefineSoundTag) t;
               flv.writeTag(new FLVTAG(0, new AUDIODATA(st.soundFormat, st.soundRate, st.soundSize, st.soundType, st.soundData)));
            }
            if (t instanceof SoundStreamHeadTypeTag) {
               SoundStreamHeadTypeTag shead = (SoundStreamHeadTypeTag) t;
               List<SoundStreamBlockTag> blocks = new ArrayList<SoundStreamBlockTag>();
               List<Object> objs = new ArrayList<Object>(this.tags);
               populateSoundStreamBlocks(objs, t, blocks);
               int ms = (int) (1000.0f / ((float) frameRate));
               for (int b = 0; b < blocks.size(); b++) {
                  byte data[] = blocks.get(b).getData(SWF.DEFAULT_VERSION);
                  if (shead.getSoundFormat() == 2) { //MP3
                     data = Arrays.copyOfRange(data, 4, data.length);
                  }
                  flv.writeTag(new FLVTAG(ms * b, new AUDIODATA(shead.getSoundFormat(), shead.getSoundRate(), shead.getSoundSize(), shead.getSoundType(), data)));
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
}
