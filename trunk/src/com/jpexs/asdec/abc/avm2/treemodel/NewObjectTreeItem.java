/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.treemodel;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;

import java.util.List;


public class NewObjectTreeItem extends TreeItem {
    public List<NameValuePair> pairs;

    public NewObjectTreeItem(AVM2Instruction instruction, List<NameValuePair> pairs) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.pairs = pairs;
    }

    @Override
    public String toString(ConstantPool constants) {
        String params = "";
        for (int n = 0; n < pairs.size(); n++) {
            if (n > 0) params += ",\r\n";
            params += pairs.get(n).toString(constants);
        }
        return hilight("{") + params + hilight("}");
    }


}
