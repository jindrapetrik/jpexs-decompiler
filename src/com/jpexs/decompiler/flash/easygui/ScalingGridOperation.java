/*
 *  Copyright (C) 2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.easygui;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.DefineScalingGridTag;
import com.jpexs.decompiler.flash.tags.base.BoundedTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.types.RECT;

/**
 * Adds, removes or moves a character's scaling grid as a single undo step.
 *
 * @author JPEXS
 */
public class ScalingGridOperation extends ChangeDoableOperation {

    private final SWF swf;
    private final DefineScalingGridTag grid;
    private final Timelined parent;
    private final int index;
    private final RECT before;
    private final RECT after;
    private final boolean gridModified;
    private final boolean parentModified;
    private final boolean swfModified;
    private final boolean headerModified;
    private final int version;
    private final Runnable refresh;

    public ScalingGridOperation(CharacterTag character, RECT splitter, Runnable refresh) {
        super("scalingGrid");
        swf = character.getSwf();
        DefineScalingGridTag existing = character.getScalingGridTag();
        before = existing == null ? null : new RECT(existing.splitter);
        after = splitter == null ? null : new RECT(splitter);
        grid = existing == null ? new DefineScalingGridTag(swf) : existing;
        grid.characterId = character.getCharacterId();
        Timelined owner = existing == null ? character.getTimelined() : existing.getTimelined();
        parent = owner == null ? swf : owner;
        index = existing == null ? parent.getTags().indexOf(character) + 1 : parent.getTags().indexOf(existing);
        gridModified = grid.isModified();
        parentModified = parent.isModified();
        swfModified = swf.isModified();
        headerModified = swf.isHeaderModified();
        version = swf.version;
        this.refresh = refresh;
    }

    public static RECT initialSplitter(BoundedTag character) {
        RECT bounds = character.getRect();
        int width = bounds.Xmax - bounds.Xmin;
        int height = bounds.Ymax - bounds.Ymin;
        return new RECT(bounds.Xmin + width / 3, bounds.Xmin + (int) (2L * width / 3),
                bounds.Ymin + height / 3, bounds.Ymin + (int) (2L * height / 3));
    }

    @Override
    public void doOperation() {
        apply(after, true);
    }

    @Override
    public void undoOperation() {
        apply(before, false);
    }

    private void apply(RECT splitter, boolean forward) {
        if (splitter == null) {
            parent.removeTag(grid);
        } else {
            grid.splitter = new RECT(splitter);
            grid.setTimelined(parent);
            if (parent.getTags().indexOf(grid) < 0) {
                parent.addTag(Math.max(0, Math.min(index, parent.getTags().size())), grid);
            }
        }
        grid.setModified(forward || gridModified);
        swf.version = forward && after != null ? Math.max(8, version) : version;
        swf.setHeaderModified(forward ? headerModified || swf.version != version : headerModified);
        parent.setModified(forward || parentModified);
        swf.setModified(forward || swfModified);
        swf.clearAllCache();
        if (refresh != null) {
            refresh.run();
        }
    }
}
