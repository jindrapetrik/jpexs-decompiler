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
package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.Main;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.abc.CopyOutputStream;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.flash.tags.base.BoundedTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.types.BUTTONRECORD;
import com.jpexs.decompiler.flash.types.RECT;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Defines a button character
 *
 * @author JPEXS
 */
public class DefineButtonTag extends CharacterTag implements ASMSource, BoundedTag {

   /**
    * ID for this character
    */
   public int buttonId;
   /**
    * Characters that make up the button
    */
   public List<BUTTONRECORD> characters;
   /**
    * Actions to perform
    */
   //public List<Action> actions;
   public byte[] actionBytes;

   @Override
   public int getCharacterID() {
      return buttonId;
   }

   /**
    * Constructor
    *
    * @param data Data bytes
    * @param version SWF version
    * @throws IOException
    */
   public DefineButtonTag(byte[] data, int version, long pos) throws IOException {
      super(7, "DefineButton", data, pos);
      SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
      buttonId = sis.readUI16();
      characters = sis.readBUTTONRECORDList(false);
      //actions = sis.readActionList();
      actionBytes = sis.readBytes(sis.available());
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
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      OutputStream os = baos;
      if (Main.DEBUG_COPY) {
         os = new CopyOutputStream(os, new ByteArrayInputStream(super.data));
      }
      SWFOutputStream sos = new SWFOutputStream(os, version);
      try {
         sos.writeUI16(buttonId);
         sos.writeBUTTONRECORDList(characters, false);
         sos.write(actionBytes);
         //sos.write(Action.actionsToBytes(actions, true, version));
         sos.close();
      } catch (IOException e) {
      }
      return baos.toByteArray();
   }

   /**
    * Converts actions to ASM source
    *
    * @param version SWF version
    * @return ASM source
    */
   public String getASMSource(int version) {
      List<Action> actions = new ArrayList<Action>();
      try {
         actions = (new SWFInputStream(new ByteArrayInputStream(actionBytes), version)).readActionList();
      } catch (IOException ex) {
         Logger.getLogger(DefineButtonTag.class.getName()).log(Level.SEVERE, null, ex);
      }
      return Action.actionsToString(actions, null, version);
   }

   /**
    * Whether or not this object contains ASM source
    *
    * @return True when contains
    */
   public boolean containsSource() {
      return true;
   }

   /**
    * Returns actions associated with this object
    *
    * @param version Version
    * @return List of actions
    */
   public List<Action> getActions(int version) {
      try {
         return (new SWFInputStream(new ByteArrayInputStream(actionBytes), version)).readActionList();
      } catch (IOException ex) {
         return new ArrayList<Action>();
      }
   }

   public void setActions(List<Action> actions, int version) {
      actionBytes = Action.actionsToBytes(actions, true, version);
   }

   public byte[] getActionBytes() {
      return actionBytes;
   }

   public void setActionBytes(byte[] actionBytes) {
      this.actionBytes = actionBytes;
   }

   @Override
   public Set<Integer> getNeededCharacters() {
      HashSet<Integer> needed = new HashSet<Integer>();
      for (BUTTONRECORD r : characters) {
         needed.add(r.characterId);
      }
      return needed;
   }

   @Override
   public RECT getRect(HashMap<Integer, CharacterTag> allCharacters) {
      RECT rect = new RECT(Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE);
      for (BUTTONRECORD r : characters) {
         CharacterTag ch = allCharacters.get(r.characterId);
         if (ch instanceof BoundedTag) {
            RECT r2 = ((BoundedTag) ch).getRect(allCharacters);
            rect.Xmin = Math.min(r2.Xmin, rect.Xmin);
            rect.Ymin = Math.min(r2.Ymin, rect.Ymin);
            rect.Xmax = Math.max(r2.Xmax, rect.Xmax);
            rect.Ymax = Math.max(r2.Ymax, rect.Ymax);
         }
      }
      return rect;
   }
}
