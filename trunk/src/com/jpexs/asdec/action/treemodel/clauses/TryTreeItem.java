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

package com.jpexs.asdec.action.treemodel.clauses;

import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.ContinueTreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;

import java.util.ArrayList;
import java.util.List;


public class TryTreeItem extends TreeItem implements Block {

    public List<TreeItem> tryCommands;
    public List<TreeItem> catchExceptions;
    public List<List<TreeItem>> catchCommands;
    public List<TreeItem> finallyCommands;

    public TryTreeItem(List<TreeItem> tryCommands, List<TreeItem> catchExceptions, List<List<TreeItem>> catchCommands, List<TreeItem> finallyCommands) {
        super(null, NOPRECEDENCE);
        this.tryCommands = tryCommands;
        this.catchExceptions = catchExceptions;
        this.catchCommands = catchCommands;
        this.finallyCommands = finallyCommands;
    }

    @Override
    public String toString(ConstantPool constants) {
        String ret = "";
        ret += "try\r\n{\r\n";
        for (TreeItem ti : tryCommands) {
            ret += ti.toString(constants) + "\r\n";
        }
        ret += "}";
        for (int e = 0; e < catchExceptions.size(); e++) {
            ret += "\r\ncatch(" + catchExceptions.get(e).toString(constants) + ")\r\n{\r\n";
            List<TreeItem> commands = catchCommands.get(e);
            for (TreeItem ti : commands) {
                ret += ti.toString(constants) + "\r\n";
            }
            ret += "}";
        }
        if (finallyCommands.size() > 0) {
            ret += "\r\nfinally\r\n{\r\n";
            for (TreeItem ti : finallyCommands) {
                ret += ti.toString(constants) + "\r\n";
            }
            ret += "}";
        }
        return ret;
    }

    public List<ContinueTreeItem> getContinues() {
        List<ContinueTreeItem> ret = new ArrayList<ContinueTreeItem>();
        for (TreeItem ti : tryCommands) {
            if (ti instanceof ContinueTreeItem) {
                ret.add((ContinueTreeItem) ti);
            }
            if (ti instanceof Block) {
                ret.addAll(((Block) ti).getContinues());
            }
        }
        if (finallyCommands != null) {
            for (TreeItem ti : finallyCommands) {
                if (ti instanceof ContinueTreeItem) {
                    ret.add((ContinueTreeItem) ti);
                }
                if (ti instanceof Block) {
                    ret.addAll(((Block) ti).getContinues());
                }
            }
        }
        for (List<TreeItem> commands : catchCommands) {
            for (TreeItem ti : commands) {
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
