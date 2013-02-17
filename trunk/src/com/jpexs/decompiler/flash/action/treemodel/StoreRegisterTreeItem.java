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
package com.jpexs.decompiler.flash.action.treemodel;

import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.swf4.RegisterNumber;
import java.util.List;

public class StoreRegisterTreeItem extends TreeItem implements SetTypeTreeItem {

   public RegisterNumber register;
   public TreeItem value;

   public StoreRegisterTreeItem(Action instruction, RegisterNumber register, TreeItem value) {
      super(instruction, PRECEDENCE_PRIMARY);
      this.value = value;
      this.register = register;
   }

   @Override
   public String toString(ConstantPool constants) {
      return hilight(register.toString() + "=") + value.toString(constants) + ";";
   }

   @Override
   public TreeItem getObject() {
      return new DirectValueTreeItem(instruction, -1, register, null);
   }

   @Override
   public List<com.jpexs.decompiler.flash.action.IgnoredPair> getNeededActions() {
      List<com.jpexs.decompiler.flash.action.IgnoredPair> ret = super.getNeededActions();
      ret.addAll(value.getNeededActions());
      return ret;
   }
}
