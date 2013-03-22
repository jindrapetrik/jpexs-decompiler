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
package com.jpexs.decompiler.flash.abc.avm2.instructions;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ABCOutputStream;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.JumpIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.LookupSwitchIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.ReturnValueIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.ReturnVoidIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.ThrowIns;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.flash.graph.GraphSource;
import com.jpexs.decompiler.flash.graph.GraphSourceItem;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import com.jpexs.decompiler.flash.helpers.Helper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class AVM2Instruction implements Serializable, GraphSourceItem {

   public InstructionDefinition definition;
   public int operands[];
   public long offset;
   public byte bytes[];
   public String comment;
   public boolean ignored = false;
   public String labelname;
   public long mappedOffset = -1;
   public int changeJumpTo = -1;

   public AVM2Instruction(long offset, InstructionDefinition definition, int[] operands, byte bytes[]) {
      this.definition = definition;
      this.operands = operands;
      this.offset = offset;
      this.bytes = bytes;
   }

   public byte[] getBytes() {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      try {
         ABCOutputStream aos = new ABCOutputStream(bos);
         aos.write(definition.instructionCode);
         for (int i = 0; i < definition.operands.length; i++) {
            int opt = definition.operands[i] & 0xff00;
            switch (opt) {
               case AVM2Code.OPT_S24:
                  aos.writeS24(operands[i]);
                  break;
               case AVM2Code.OPT_U30:
                  aos.writeU30(operands[i]);
                  break;
               case AVM2Code.OPT_U8:
                  aos.writeU8(operands[i]);
                  break;
               case AVM2Code.OPT_BYTE:
                  aos.writeU8(0xff & operands[i]);
                  break;
               case AVM2Code.OPT_CASE_OFFSETS:

                  aos.writeU30(operands[i]); //case count
                  for (int j = i + 1; j < operands.length; j++) {
                     aos.writeS24(operands[j]);
                  }
                  break;
            }
         }
      } catch (IOException ex) {
         //ignored
      }
      return bos.toByteArray();
   }

   @Override
   public String toString() {
      String s = definition.instructionName;
      for (int i = 0; i < operands.length; i++) {
         s += " " + operands[i];
      }
      return s;
   }

   public List<Long> getOffsets() {
      List<Long> ret = new ArrayList<Long>();
      String s = "";
      for (int i = 0; i < definition.operands.length; i++) {
         switch (definition.operands[i]) {
            case AVM2Code.DAT_OFFSET:
               ret.add(offset + operands[i] + getBytes().length);
               break;
            case AVM2Code.DAT_CASE_BASEOFFSET:
               ret.add(offset + operands[i]);
               break;
            case AVM2Code.OPT_CASE_OFFSETS:
               for (int j = i + 1; j < operands.length; j++) {
                  ret.add(offset + operands[j]);
               }
               break;
         }

      }
      return ret;
   }

   public List getParamsAsList(ConstantPool constants) {
      List s = new ArrayList();
      for (int i = 0; i < definition.operands.length; i++) {
         switch (definition.operands[i]) {
            case AVM2Code.DAT_MULTINAME_INDEX:
               s.add(constants.constant_multiname[operands[i]]);
               break;
            case AVM2Code.DAT_STRING_INDEX:
               s.add(constants.constant_string[operands[i]]);
               break;
            case AVM2Code.DAT_INT_INDEX:
               s.add(new Long(constants.constant_int[operands[i]]));
               break;
            case AVM2Code.DAT_UINT_INDEX:
               s.add(new Long(constants.constant_uint[operands[i]]));
               break;
            case AVM2Code.DAT_DOUBLE_INDEX:
               s.add(new Double(constants.constant_double[operands[i]]));
               break;
            case AVM2Code.DAT_OFFSET:
               s.add(new Long(offset + operands[i] + getBytes().length));
               break;
            case AVM2Code.DAT_CASE_BASEOFFSET:
               s.add(new Long(offset + operands[i]));
               break;
            case AVM2Code.OPT_CASE_OFFSETS:
               s.add(new Long(operands[i]));
               for (int j = i + 1; j < operands.length; j++) {
                  s.add(new Long(offset + operands[j]));
               }
               break;
            default:
               s.add(new Long(operands[i]));
         }

      }
      return s;
   }

   public String getParams(ConstantPool constants, List<String> fullyQualifiedNames) {
      String s = "";
      for (int i = 0; i < definition.operands.length; i++) {
         switch (definition.operands[i]) {
            case AVM2Code.DAT_MULTINAME_INDEX:
               s += " m[" + operands[i] + "]\"" + Helper.escapeString(constants.constant_multiname[operands[i]].toString(constants, fullyQualifiedNames)) + "\"";
               break;
            case AVM2Code.DAT_STRING_INDEX:
               s += " \"" + Helper.escapeString(constants.constant_string[operands[i]]) + "\"";
               break;
            case AVM2Code.DAT_INT_INDEX:
               s += " " + constants.constant_int[operands[i]] + "";
               break;
            case AVM2Code.DAT_UINT_INDEX:
               s += " " + constants.constant_uint[operands[i]] + "";
               break;
            case AVM2Code.DAT_DOUBLE_INDEX:
               s += " " + constants.constant_double[operands[i]] + "";
               break;
            case AVM2Code.DAT_OFFSET:
               s += " ";
               if (operands[i] > 0) {
                  //s += "+";
               }//operands[i]
               s += "ofs" + Helper.formatAddress(offset + operands[i] + getBytes().length) + "";
               break;
            case AVM2Code.DAT_CASE_BASEOFFSET:
               s += " ";
               if (operands[i] > 0) {
                  //s += "+";
               }//operands[i]
               s += "ofs" + Helper.formatAddress(offset + operands[i]) + "";
               break;
            case AVM2Code.OPT_CASE_OFFSETS:
               s += " " + operands[i];
               for (int j = i + 1; j < operands.length; j++) {
                  s += " ";
                  if (operands[j] > 0) {
                     //s += "+";
                  }//operands[j]
                  s += "ofs" + Helper.formatAddress(offset + operands[j]) + "";
               }
               break;
            default:
               s += " " + operands[i];
         }

      }
      return s;
   }

   public String getComment() {
      if (ignored) {
         return " ;ignored";
      }
      if ((comment == null) || comment.equals("")) {
         return "";
      }
      return " ;" + comment;
   }

   @Override
   public boolean isIgnored() {
      return ignored;
   }

   public String toString(ConstantPool constants, List<String> fullyQualifiedNames) {
      String s = Helper.formatAddress(offset) + " " + Helper.padSpaceRight(Helper.byteArrToString(getBytes()), 30) + definition.instructionName;
      s += getParams(constants, fullyQualifiedNames) + getComment();
      return s;
   }

   public String toStringNoAddress(ConstantPool constants, List<String> fullyQualifiedNames) {
      String s = definition.instructionName;
      s += getParams(constants, fullyQualifiedNames) + getComment();
      return s;
   }
   public List replaceWith;

   @Override
   public void translate(List localData, Stack<GraphTargetItem> stack, List<GraphTargetItem> output) {
      definition.translate((Boolean) localData.get(0), (Integer) localData.get(1), (HashMap<Integer, GraphTargetItem>) localData.get(2), stack, (Stack<GraphTargetItem>) localData.get(3), (ConstantPool) localData.get(4), this, (MethodInfo[]) localData.get(5), output, (MethodBody) localData.get(6), (ABC) localData.get(7), (HashMap<Integer, String>) localData.get(8), (List<String>) localData.get(9));
   }

   @Override
   public boolean isJump() {
      return (definition instanceof JumpIns);
   }

   @Override
   public boolean isBranch() {
      return (definition instanceof IfTypeIns) || (definition instanceof LookupSwitchIns);
   }

   @Override
   public boolean isExit() {
      return (definition instanceof ReturnValueIns) || (definition instanceof ReturnVoidIns) || (definition instanceof ThrowIns);
   }

   @Override
   public long getOffset() {
      return mappedOffset > -1 ? mappedOffset : offset;
   }

   @Override
   public List<Integer> getBranches(GraphSource code) {
      List<Integer> ret = new ArrayList<Integer>();
      if (definition instanceof IfTypeIns) {
         ret.add(code.adr2pos(offset + getBytes().length + operands[0]));
         if (!(definition instanceof JumpIns)) {
            ret.add(code.adr2pos(offset + getBytes().length));
         }
      }
      if (definition instanceof LookupSwitchIns) {
         ret.add(code.adr2pos(offset + operands[0]));
         for (int k = 2; k < operands.length; k++) {
            ret.add(code.adr2pos(offset + operands[k]));
         }
      }
      return ret;
   }

   @Override
   public boolean ignoredLoops() {
      return false;
   }

   @Override
   public void setIgnored(boolean ignored) {
      this.ignored = ignored;
   }
}
