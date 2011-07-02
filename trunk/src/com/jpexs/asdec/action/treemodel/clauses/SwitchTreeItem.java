/*
 *  Copyright (C) 2010-2011 JPEXS
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.jpexs.asdec.action.treemodel.clauses;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.ContinueTreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;

import java.util.ArrayList;
import java.util.List;


public class SwitchTreeItem extends LoopTreeItem implements Block {

    public TreeItem switchedObject;
    public List<TreeItem> caseValues;
    public List<List<TreeItem>> caseCommands;
    public List<TreeItem> defaultCommands;

    public SwitchTreeItem(Action instruction, long switchBreak, TreeItem switchedObject, List<TreeItem> caseValues, List<List<TreeItem>> caseCommands, List<TreeItem> defaultCommands) {
        super(instruction, switchBreak, -1);
        this.switchedObject = switchedObject;
        this.caseValues = caseValues;
        this.caseCommands = caseCommands;
        this.defaultCommands = defaultCommands;
    }

    @Override
    public String toString(ConstantPool constants) {
        String ret = "";
        ret += "loop" + loopBreak + ":\r\n";
        ret += hilight("switch(") + switchedObject.toString(constants) + hilight(")") + "\r\n{\r\n";
        for (int i = 0; i < caseValues.size(); i++) {
            ret += "case " + caseValues.get(i).toString(constants) + ":\r\n";
            ret += Action.INDENTOPEN + "\r\n";
            for (int j = 0; j < caseCommands.get(i).size(); j++) {
                ret += caseCommands.get(i).get(j).toString(constants) + "\r\n";
            }
            ret += Action.INDENTCLOSE + "\r\n";
        }
        if (defaultCommands != null) {
            if (defaultCommands.size() > 0) {
                ret += hilight("default") + ":\r\n";
                ret += Action.INDENTOPEN + "\r\n";
                for (int j = 0; j < defaultCommands.size(); j++) {
                    ret += defaultCommands.get(j).toString(constants) + "\r\n";
                }
                ret += Action.INDENTCLOSE + "\r\n";
            }
        }
        ret += hilight("}") + "\r\n";
        ret += ":loop" + loopBreak;
        return ret;
    }

    public List<ContinueTreeItem> getContinues() {
        List<ContinueTreeItem> ret = new ArrayList<ContinueTreeItem>();

        for (List<TreeItem> onecase : caseCommands) {
            for (TreeItem ti : onecase) {
                if (ti instanceof ContinueTreeItem) {
                    ret.add((ContinueTreeItem) ti);
                }
                if (ti instanceof Block) {
                    ret.addAll(((Block) ti).getContinues());
                }
            }
        }
        if (defaultCommands != null) {
            for (TreeItem ti : defaultCommands) {
                if (ti instanceof ContinueTreeItem) {
                    ret.add((ContinueTreeItem) ti);
                }
                if (ti instanceof Block) {
                    ret.addAll(((Block) ti).getContinues());
                }
            }
        }
        return ret;
    }
}
