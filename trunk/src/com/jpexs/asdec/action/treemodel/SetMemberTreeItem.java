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

public class SetMemberTreeItem extends TreeItem implements SetTypeTreeItem {

   public TreeItem object;
   public TreeItem objectName;
   public TreeItem value;

   public SetMemberTreeItem(Action instruction, TreeItem object, TreeItem objectName, TreeItem value) {
      super(instruction, PRECEDENCE_PRIMARY);
      this.object = object;
      this.objectName = objectName;
      this.value = value;
   }

   @Override
   public String toString(ConstantPool constants) {
      return object.toString(constants) + "." + stripQuotes(objectName) + "=" + value.toString(constants) + ";";
   }

   @Override
   public TreeItem getObject() {
      return new GetMemberTreeItem(instruction, object, objectName);
   }
   
   @Override
    public List<com.jpexs.asdec.action.IgnoredPair> getNeededActions() {
      List<com.jpexs.asdec.action.IgnoredPair> ret=super.getNeededActions();
      ret.addAll(object.getNeededActions());
      ret.addAll(objectName.getNeededActions());
      ret.addAll(value.getNeededActions());
      return ret;
   }
}
