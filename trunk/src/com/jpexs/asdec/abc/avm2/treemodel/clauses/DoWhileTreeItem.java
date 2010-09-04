/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.treemodel.clauses;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.abc.avm2.treemodel.ContinueTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.TreeItem;

import java.util.ArrayList;
import java.util.List;


public class DoWhileTreeItem extends LoopTreeItem implements Block {

    public List<TreeItem> commands;
    public TreeItem expression;

    public DoWhileTreeItem(AVM2Instruction instruction, int loopBreak, int loopContinue, List<TreeItem> commands, TreeItem expression) {
        super(instruction, loopBreak, loopContinue);
        this.expression = expression;
        this.commands = commands;
    }

    @Override
    public String toString(ConstantPool constants) {
        String ret = "";
        ret += "loop" + loopBreak + ":\r\n";
        ret += hilight("do\r\n{") + "\r\n";
        for (TreeItem ti : commands) {
            ret += ti.toString(constants) + "\r\n";
        }
        ret += hilight("}\r\nwhile(") + expression.toString(constants) + hilight(");") + "\r\n";
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
