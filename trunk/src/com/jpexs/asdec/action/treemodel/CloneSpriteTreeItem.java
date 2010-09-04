package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;

public class CloneSpriteTreeItem extends TreeItem {
    public TreeItem source;
    public TreeItem target;
    public TreeItem depth;

    public CloneSpriteTreeItem(Action instruction, TreeItem source, TreeItem target, TreeItem depth) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.source = source;
        this.target = target;
        this.depth = depth;
    }

    @Override
    public String toString(ConstantPool constants) {
        return "duplicateMovieClip(" + target.toString(constants) + "," + source.toString(constants) + "," + depth.toString(constants) + ");";
    }
}
