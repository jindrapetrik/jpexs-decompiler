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

import com.jpexs.decompiler.flash.ReReadableInputStream;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Instructs Flash Player to perform a list of actions when the current frame is
 * complete.
 */
public class DoActionTag extends Tag implements ASMSource {

   /**
    * List of actions to perform
    */
   //public List<Action> actions = new ArrayList<Action>();
   public byte[] actionBytes;

   /**
    * Constructor
    *
    * @param data Data bytes
    * @param version SWF version
    * @throws IOException
    */
   public DoActionTag(byte[] data, int version, long pos) {
      super(12, "DoAction", data, pos);
      actionBytes = data;
   }

   /**
    * Gets data bytes
    *
    * @param version SWF version
    * @return Bytes of data
    */
   @Override
   public byte[] getData(int version) {
      return actionBytes;//Action.actionsToBytes(actions, true, version);
   }

   /**
    * Converts actions to ASM source
    *
    * @param version SWF version
    * @return ASM source
    */
   @Override
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

   /**
    * Returns string representation of the object
    *
    * @return String representation of the object
    */
   @Override
   public String toString() {
      return "DoAction";
   }

   public List<Action> getActions(int version) {
      try {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         int prevLength = 0;
         if (previousTag != null) {
            byte prevData[] = previousTag.getData(version);
            baos.write(prevData);
            prevLength = prevData.length;
            byte header[] = SWFOutputStream.getTagHeader(this, data, version);
            baos.write(header);
            prevLength += header.length;
         }
         baos.write(actionBytes);
         ReReadableInputStream rri = new ReReadableInputStream(new ByteArrayInputStream(baos.toByteArray()));
         rri.setPos(prevLength);
         return Action.removeNops(SWFInputStream.readActionList(rri, version, prevLength), version);
      } catch (Exception ex) {
         Logger.getLogger(DoActionTag.class.getName()).log(Level.SEVERE, null, ex);
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
