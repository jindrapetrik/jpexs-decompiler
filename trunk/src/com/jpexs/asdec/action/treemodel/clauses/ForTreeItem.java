/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.action.treemodel.clauses;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.ContinueTreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;

import java.util.ArrayList;
import java.util.List;


public class ForTreeItem extends LoopTreeItem implements Block {

    public List<TreeItem> firstCommands;
    public TreeItem expression;
    public List<TreeItem> finalCommands;
    public List<TreeItem> commands;

    public ForTreeItem(Action instruction, long loopBreak, long loopContinue, List<TreeItem> firstCommands, TreeItem expression, List<TreeItem> finalCommands, List<TreeItem> commands) {
        super(instruction, loopBreak, loopContinue);
        this.firstCommands = firstCommands;
        this.expression = expression;
        this.finalCommands = finalCommands;
        this.commands = commands;
    }

    private String stripSemicolon(String s) {
        if (s.endsWith(";")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    @Override
    public String toString(ConstantPool constants) {
        String ret = "";
        ret += "loop" + loopBreak + ":\r\n";
        ret += hilight("for(");
        for (int i = 0; i < firstCommands.size(); i++) {
            if (i > 0) {
                ret += ",";
            }
            ret += stripSemicolon(firstCommands.get(i).toString(constants));
        }
        ret += ";";
        ret += expression.toString(constants);
        ret += ";";
        for (int i = 0; i < finalCommands.size(); i++) {
            if (i > 0) {
                ret += ",";
            }
            ret += stripSemicolon(finalCommands.get(i).toString(constants));
        }
        ret += hilight(")") + "\r\n{\r\n";
        for (TreeItem ti : commands) {
            ret += ti.toString(constants) + "\r\n";
        }
        ret += hilight("}") + "\r\n";
        ret += ":loop" + loopBreak;
        return ret;
    }

    public List<ContinueTreeItem> getContinues() {
        List<ContinueTreeItem> ret = new ArrayList<ContinueTreeItem>();
        for (TreeItem ti : commands) {
            if (ti instanceof ContinueTreeItem) {
                ret.add((ContinueTreeItem) ti);
            }
            if (ti instanceof Block) {
                ret.addAll(((Block) ti).getContinues());
            }
        }
        return ret;
    }
}
