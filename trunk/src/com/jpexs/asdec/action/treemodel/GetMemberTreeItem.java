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
package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;
import java.util.HashMap; import java.util.List;

public class GetMemberTreeItem extends TreeItem {

   public TreeItem object;
   public TreeItem memberName;

   public GetMemberTreeItem(Action instruction, TreeItem object, TreeItem memberName) {
      super(instruction, PRECEDENCE_PRIMARY);
      this.object = object;
      this.memberName = memberName;
   }

   @Override
   public String toString(ConstantPool constants) {
      if (!((memberName instanceof DirectValueTreeItem) && (((DirectValueTreeItem) memberName).value instanceof String))) {
         //if(!(functionName instanceof GetVariableTreeItem))
         return object.toString(constants) + "[" + stripQuotes(memberName) + "]";
      }
      return object.toString(constants) + "." + stripQuotes(memberName);
   }
   
   @Override
    public List<com.jpexs.asdec.action.IgnoredPair> getNeededActions() {
      List<com.jpexs.asdec.action.IgnoredPair> ret=super.getNeededActions();
      ret.addAll(object.getNeededActions());
      ret.addAll(memberName.getNeededActions());
      return ret;
   }
}
