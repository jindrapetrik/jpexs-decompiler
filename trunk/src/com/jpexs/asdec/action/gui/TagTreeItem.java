package com.jpexs.asdec.action.gui;

import java.util.ArrayList;
import java.util.List;

public class TagTreeItem {
    public List<TagTreeItem> subItems;
    public Object tag;

    public TagTreeItem(Object tag) {
        this.tag = tag;
        this.subItems = new ArrayList<TagTreeItem>();
    }

    @Override
    public String toString() {
        return tag.toString();
    }
}
