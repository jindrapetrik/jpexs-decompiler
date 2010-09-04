/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.treemodel.clauses;

import com.jpexs.asdec.abc.avm2.treemodel.ContinueTreeItem;

import java.util.List;


public interface Block {
    public List<ContinueTreeItem> getContinues();
}
