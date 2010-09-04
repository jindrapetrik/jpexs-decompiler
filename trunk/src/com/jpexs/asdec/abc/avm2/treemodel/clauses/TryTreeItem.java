/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.treemodel.clauses;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.treemodel.ContinueTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.TreeItem;
import com.jpexs.asdec.abc.types.ABCException;

import java.util.ArrayList;
import java.util.List;


public class TryTreeItem extends TreeItem implements Block {

    public List<TreeItem> tryCommands;
    public List<ABCException> catchExceptions;
    public List<List<TreeItem>> catchCommands;
    public List<TreeItem> finallyCommands;

    public TryTreeItem(List<TreeItem> tryCommands, List<ABCException> catchExceptions, List<List<TreeItem>> catchCommands, List<TreeItem> finallyCommands) {
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
            ret += "\r\ncatch(" + catchExceptions.get(e).getVarName(constants) + ":" + catchExceptions.get(e).getTypeName(constants) + ")\r\n{\r\n";
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
