/*
 *  Copyright (C) 2010-2011 JPEXS
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
package com.jpexs.asdec;

import SevenZip.Compression.LZMA.Encoder;
import com.jpexs.asdec.action.TagNode;
import com.jpexs.asdec.helpers.Highlighting;
import com.jpexs.asdec.tags.ASMSource;
import com.jpexs.asdec.tags.DoABCTag;
import com.jpexs.asdec.tags.Tag;
import com.jpexs.asdec.tags.TagName;
import com.jpexs.asdec.types.RECT;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * Class representing SWF file
 *
 * @author JPEXS
 */
public class SWF {

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

   /**
    * Construct SWF from stream
    *
    * @param is Stream to read SWF from
    * @throws IOException
    */
   public SWF(InputStream is) throws IOException {
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
         int i = 0;
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
         int i = 0;
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

   public boolean exportActionScript(String outdir, boolean isPcode) throws Exception {
      boolean asV3Found = false;
      final EventListener evl = new EventListener() {
         public void handleEvent(String event, Object data) {
            if (event.equals("export")) {
               informListeners(event, data);
            }
         }
      };
      for (Tag t : tags) {
         if (t instanceof DoABCTag) {
            ((DoABCTag) t).abc.addEventListener(evl);
            ((DoABCTag) t).abc.export(outdir, isPcode);
            asV3Found = true;
         }
      }
      if (!asV3Found) {
         List<Object> list2 = new ArrayList<Object>();
         list2.addAll(tags);
         return exportNode(TagNode.createTagList(list2), outdir, isPcode);
      }
      return asV3Found;
   }

   private boolean exportNode(List<TagNode> nodeList, String outdir, boolean isPcode) {
      File dir = new File(outdir);
      if (!dir.exists()) {
         dir.mkdirs();
      }
      List<String> existingNames = new ArrayList<String>();
      for (TagNode node : nodeList) {
         String name = "";
         if (node.tag instanceof TagName) {
            name = ((TagName) node.tag).getName();
         } else {
            name = node.tag.toString();
         }
         int i = 1;
         String baseName = name;
         while (existingNames.contains(name)) {
            i++;
            name = baseName + "_" + i;
         }
         existingNames.add(name);
         if (node.subItems.isEmpty()) {
            if (node.tag instanceof ASMSource) {
               try {
                  String f = outdir + File.separatorChar + name + ".as";
                  informListeners("export", "Exporting " + f + " ...");
                  String ret = "";
                  if (isPcode) {
                     ret = ((ASMSource) node.tag).getASMSource(10); //TODO:Ensure correct version here
                  } else {
                     List<com.jpexs.asdec.action.Action> as = ((ASMSource) node.tag).getActions(10);//TODO:Ensure correct version here
                     com.jpexs.asdec.action.Action.setActionsAddresses(as, 0, 10);//TODO:Ensure correct version here
                     ret = (Highlighting.stripHilights(com.jpexs.asdec.action.Action.actionsToSource(as, 10))); //TODO:Ensure correct version here
                  }


                  FileOutputStream fos = new FileOutputStream(f);
                  fos.write(ret.getBytes());
                  fos.close();
               } catch (Exception ex) {
               }
            }
         } else {
            exportNode(node.subItems, outdir + File.separatorChar + name, isPcode);
         }

      }
      return true;
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
}
