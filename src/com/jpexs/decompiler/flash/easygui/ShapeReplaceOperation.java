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
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.timeline.Timelined;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * Records an existing shape import, including any bitmap tags it creates.
 */
final class ShapeReplaceOperation implements DoableOperation {

    private final ShapeTag before;
    private ShapeTag after;
    private final Timelined parent;
    private final boolean parentModified;
    private final boolean swfModified;
    private final boolean headerModified;
    private final int version;
    private int importedVersion;
    private boolean importedHeaderModified;
    private final String description;
    private final BiConsumer<ShapeTag, Boolean> refresh;
    private final Set<Tag> originalTags = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Map<Tag, Integer> addedTags = new LinkedHashMap<>();

    ShapeReplaceOperation(ShapeTag original, String description, BiConsumer<ShapeTag, Boolean> refresh) throws IOException, InterruptedException {
        before = (ShapeTag) original.cloneTag();
        before.setOriginalRange(original.getOriginalRange());
        before.setModified(original.isModified());
        parent = original.getTimelined() == null ? original.getSwf() : original.getTimelined();
        parentModified = parent.isModified();
        SWF swf = original.getSwf();
        swfModified = swf.isModified();
        headerModified = swf.isHeaderModified();
        version = swf.version;
        for (Tag tag : swf.getTags()) {
            originalTags.add(tag);
        }
        this.description = description;
        this.refresh = refresh;
    }

    boolean finishImport() throws IOException, InterruptedException {
        SWF swf = before.getSwf();
        ShapeTag current = (ShapeTag) swf.getCharacter(before.shapeId);
        after = (ShapeTag) current.cloneTag();
        after.setModified(true);
        int index = 0;
        for (Tag tag : swf.getTags()) {
            if (!originalTags.contains(tag) && tag != current) {
                addedTags.put(tag, index);
            }
            index++;
        }
        importedVersion = Math.max(swf.version, after.getShapeNum() == 4 ? 8 : after.getShapeNum());
        importedHeaderModified = swf.isHeaderModified() || importedVersion != version;
        boolean changed = !addedTags.isEmpty() || before.getId() != after.getId() || !Arrays.equals(before.getData(), after.getData());
        // Rewind the in-place import before UndoManager records it. Merely clearing
        // the SWF flag is insufficient: imported bitmap tags are modified too.
        if (changed) {
            apply(before, false, false);
        } else {
            current.setModified(before.isModified());
            parent.setModified(parentModified);
            swf.setModified(swfModified);
            swf.setHeaderModified(headerModified);
            swf.version = version;
        }
        return changed;
    }

    @Override
    public void doOperation() {
        apply(after, true, true);
    }

    @Override
    public void undoOperation() {
        apply(before, false, true);
    }

    private void apply(ShapeTag target, boolean forward, boolean notify) {
        SWF swf = before.getSwf();
        for (Map.Entry<Tag, Integer> added : addedTags.entrySet()) {
            if (forward) {
                if (swf.indexOfTag(added.getKey()) < 0) {
                    swf.addTag(Math.min(added.getValue(), swf.getTags().size()), added.getKey());
                }
            } else {
                swf.removeTag(added.getKey());
            }
        }
        ShapeTag current = (ShapeTag) swf.getCharacter(before.shapeId);
        if (notify) {
            target.setOriginalRange(current.getOriginalRange());
        }
        target.setTimelined(parent);
        parent.replaceTag(current, target);
        swf.version = forward ? importedVersion : version;
        swf.setHeaderModified(forward ? importedHeaderModified : headerModified);
        parent.setModified(forward || parentModified);
        swf.setModified(forward || swfModified);
        swf.clearAllCache();
        if (notify) {
            target.setModified(true);
            refresh.accept(target, forward);
        }
    }

    @Override
    public String getDescription() {
        return description;
    }
}
