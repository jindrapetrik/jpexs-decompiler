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
package com.jpexs.asdec.tags;

import com.jpexs.asdec.Main;
import com.jpexs.asdec.SWFInputStream;
import com.jpexs.asdec.SWFOutputStream;
import com.jpexs.asdec.abc.CopyOutputStream;
import com.jpexs.asdec.tags.base.Container;
import com.jpexs.asdec.tags.base.TagName;
import com.jpexs.asdec.types.BUTTONCONDACTION;
import com.jpexs.asdec.types.BUTTONRECORD;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Extends the capabilities of DefineButton by allowing any state transition to
 * trigger actions
 *
 * @author JPEXS
 */
public class DefineButton2Tag extends Tag implements Container, TagName {

   /**
    * ID for this character
    */
   public int buttonId;
   /**
    * Track as menu button
    */
   public boolean trackAsMenu;
   /**
    * Characters that make up the button
    */
   public List<BUTTONRECORD> characters;
   /**
    * Actions to execute at particular button events
    */
   public List<BUTTONCONDACTION> actions = new ArrayList<BUTTONCONDACTION>();
   /**
    * List of ExportAssetsTag used for converting to String
    */
   public List<ExportAssetsTag> exportAssetsTags = new ArrayList<ExportAssetsTag>();

   /**
    * Constructor
    *
    * @param data Data bytes
    * @param version SWF version
    * @throws IOException
    */
   public DefineButton2Tag(byte data[], int version, long pos) throws IOException {
      super(34, data, pos);
      SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
      buttonId = sis.readUI16();
      sis.readUB(7); //reserved
      trackAsMenu = sis.readUB(1) == 1;
      int actionOffset = sis.readUI16();
      characters = sis.readBUTTONRECORDList(true);
      if (actionOffset > 0) {
         actions = sis.readBUTTONCONDACTIONList();
      }
   }

   /**
    * Gets data bytes
    *
    * @param version SWF version
    * @return Bytes of data
    */
   @Override
   public byte[] getData(int version) {
      if (Main.DISABLE_DANGEROUS) {
         return super.getData(version);
      }

      ByteArrayInputStream bais = new ByteArrayInputStream(super.data);

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      OutputStream os = baos;
      if (Main.DEBUG_COPY) {
         os = new CopyOutputStream(os, bais);
      }
      SWFOutputStream sos = new SWFOutputStream(os, version);
      try {
         sos.writeUI16(buttonId);
         sos.writeUB(7, 0); //reserved
         sos.writeUB(1, trackAsMenu ? 1 : 0);

         ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
         OutputStream os2 = baos2;
         byte origbrdata[] = null;
         if (Main.DEBUG_COPY) {
            SWFInputStream sis = new SWFInputStream(bais, version);
            int len = sis.readUI16();
            if (len != 0) {
               origbrdata = sis.readBytes(len - 2);
               os2 = new CopyOutputStream(os2, new ByteArrayInputStream(origbrdata));
            }
         }
         SWFOutputStream sos2 = new SWFOutputStream(os2, version);
         sos2.writeBUTTONRECORDList(characters, true);
         sos2.close();
         byte brdata[] = baos2.toByteArray();
         if (Main.DEBUG_COPY) {
            if (origbrdata != null) {
               if (origbrdata.length != brdata.length) {
                  /*throw nso*/
               }
            }
         }
         if (Main.DEBUG_COPY) {
            sos = new SWFOutputStream(baos, version);
         }
         if ((actions == null) || (actions.isEmpty())) {
            sos.writeUI16(0);
         } else {
            sos.writeUI16(2 + brdata.length);
         }
         sos.write(brdata);
         if (Main.DEBUG_COPY) {
            sos = new SWFOutputStream(new CopyOutputStream(baos, bais), version);
         }
         sos.writeBUTTONCONDACTIONList(actions);
         sos.close();
      } catch (IOException e) {
         Logger.getLogger(DefineButton2Tag.class.getName()).log(Level.SEVERE, null, e);
      }
      return baos.toByteArray();
   }

   /**
    * Returns all sub-items
    *
    * @return List of sub-items
    */
   public List<Object> getSubItems() {
      List<Object> ret = new ArrayList<Object>();
      ret.addAll(actions);
      return ret;
   }

   /**
    * Returns number of sub-items
    *
    * @return Number of sub-items
    */
   public int getItemCount() {
      return actions.size();
   }

   public String getName() {
      return "DefineButton2Tag" + buttonId;
   }

   @Override
   public String toString() {
      String name = "";
      for (ExportAssetsTag eat : exportAssetsTags) {
         int pos = eat.tags.indexOf(buttonId);
         if (pos > -1) {
            name = ": " + eat.names.get(pos);
         }
      }
      return "DefineButton2 (" + buttonId + name + ")";
   }
}
