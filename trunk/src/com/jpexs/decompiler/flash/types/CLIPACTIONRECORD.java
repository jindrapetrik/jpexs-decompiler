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
package com.jpexs.decompiler.flash.types;

import com.jpexs.decompiler.flash.ReReadableInputStream;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Event handler
 *
 * @author JPEXS
 */
public class CLIPACTIONRECORD implements ASMSource {

   /**
    * Events to which this handler applies
    */
   public CLIPEVENTFLAGS eventFlags;
   /**
    * If EventFlags contain ClipEventKeyPress: Key code to trap
    */
   public int keyCode;
   /**
    * Actions to perform
    */
   //public List<Action> actions;
   public byte[] actionBytes;

   /**
    * Returns a string representation of the object
    *
    * @return a string representation of the object.
    */
   @Override
   public String toString() {
      return "CLIPACTIONRECORD";
   }

   /**
    * Returns header with events converted to string
    *
    * @return String representation of events
    */
   public String getHeader() {
      String ret;
      ret = eventFlags.toString();
      if (eventFlags.clipEventKeyPress) {
         ret = ret.replace("keyPress", "keyPress<" + keyCode + ">");
      }
      return ret;
   }

   /**
    * Converts actions to ASM source
    *
    * @param version SWF version
    * @return ASM source
    */
   public String getASMSource(int version, boolean hex) {
      return Action.actionsToString(getActions(version), null, version, hex);
   }

   /**
    * Whether or not this object contains ASM source
    *
    * @return True when contains
    */
   public boolean containsSource() {
      return true;
   }

   public List<Action> getActions(int version) {
      try {
         return Action.removeNops(SWFInputStream.readActionList(new ReReadableInputStream(new ByteArrayInputStream(actionBytes)), version, 0), version);
      } catch (IOException ex) {
         Logger.getLogger(BUTTONCONDACTION.class.getName()).log(Level.SEVERE, null, ex);
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
}
