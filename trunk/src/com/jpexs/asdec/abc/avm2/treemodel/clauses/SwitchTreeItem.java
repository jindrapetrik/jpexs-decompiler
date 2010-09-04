/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.treemodel.clauses;

import com.jpexs.asdec.abc.avm2.AVM2Code;
import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.abc.avm2.treemodel.ContinueTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.TreeItem;

import java.util.ArrayList;
import java.util.List;


public class SwitchTreeItem extends LoopTreeItem implements Block {

    public TreeItem switchedObject;
    public List<TreeItem> caseValues;
    public List<List<TreeItem>> caseCommands;
    public List<TreeItem> defaultCommands;

    public SwitchTreeItem(AVM2Instruction instruction, int switchBreak, TreeItem switchedObject, List<TreeItem> caseValues, List<List<TreeItem>> caseCommands, List<TreeItem> defaultCommands) {
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
            ret += AVM2Code.IDENTOPEN + "\r\n";
            for (int j = 0; j < caseCommands.get(i).size(); j++) {
                ret += caseCommands.get(i).get(j).toString(constants) + "\r\n";
            }
            ret += AVM2Code.IDENTCLOSE + "\r\n";
        }
        if (defaultCommands.size() > 0) {
            ret += hilight("default") + ":\r\n";
            ret += AVM2Code.IDENTOPEN + "\r\n";
            for (int j = 0; j < defaultCommands.size(); j++) {
                ret += defaultCommands.get(j).toString(constants) + "\r\n";
            }
            ret += AVM2Code.IDENTCLOSE + "\r\n";
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

        for (TreeItem ti : defaultCommands) {
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
