/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;


public class EachTreeItem extends TreeItem {
    public TreeItem object;
    public TreeItem collection;

    public EachTreeItem(Action instruction, TreeItem object, TreeItem collection) {
        super(instruction, NOPRECEDENCE);
        this.object = object;
        this.collection = collection;
    }

    @Override
    public String toString(ConstantPool constants) {
        return hilight("each (") + object.toString(constants) + hilight(" in ") + collection.toString(constants) + ")";
    }


}
