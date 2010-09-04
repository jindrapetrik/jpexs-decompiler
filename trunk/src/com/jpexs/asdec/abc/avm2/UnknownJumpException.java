/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2;

import com.jpexs.asdec.abc.avm2.treemodel.TreeItem;

import java.util.List;
import java.util.Stack;


public class UnknownJumpException extends RuntimeException {
    public Stack stack;
    public int ip;
    public List<TreeItem> output;

    public UnknownJumpException(Stack stack, int ip, List<TreeItem> output) {
        this.stack = stack;
        this.ip = ip;
        this.output = output;
    }

    @Override
    public String toString() {
        return "Unknown jump to " + ip;
    }


}
