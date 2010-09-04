package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;

public class RemoveSpriteTreeItem extends TreeItem {
    private TreeItem target;

    public RemoveSpriteTreeItem(Action instruction, TreeItem target) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.target = target;
    }

    @Override
    public String toString(ConstantPool constants) {
        return "removeMovieClip(" + target.toString(constants) + ");";
    }
}