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
import com.jpexs.decompiler.flash.tags.NameCharacterTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.SoundTag;
import com.jpexs.decompiler.flash.timeline.Timelined;

/**
 * Changes a character's library name as a single undo step.
 *
 * @author JPEXS
 */
public class RenameCharacterOperation extends ChangeDoableOperation {

    private final SWF swf;
    private final NameCharacterTag nameTag;
    private final Timelined parent;
    private final int index;
    private final String before;
    private final String after;
    private final boolean tagModified;
    private final boolean parentModified;
    private final boolean swfModified;
    private final boolean headerModified;
    private final int version;
    private final Runnable refresh;

    public RenameCharacterOperation(CharacterTag character, String name, Runnable refresh) {
        super("libraryName");
        swf = character.getSwf();
        NameCharacterTag existing = (NameCharacterTag) swf.getCharacterIdTag(character.getCharacterId(), NameCharacterTag.ID);
        before = existing == null ? null : existing.name;
        after = name.isEmpty() ? null : name;
        nameTag = existing == null ? new NameCharacterTag(swf) : existing;
        if (existing == null) {
            nameTag.characterId = character.getCharacterId();
            nameTag.type = character instanceof ImageTag ? NameCharacterTag.TYPE_BITMAP : NameCharacterTag.TYPE_SYMBOL;
            if (character instanceof SoundTag) {
                nameTag.type = NameCharacterTag.TYPE_SOUND;
            }
        }
        Timelined owner = existing == null ? character.getTimelined() : existing.getTimelined();
        parent = owner == null ? swf : owner;
        index = existing == null ? parent.getTags().indexOf(character) + 1 : parent.getTags().indexOf(existing);
        tagModified = nameTag.isModified();
        parentModified = parent.isModified();
        swfModified = swf.isModified();
        headerModified = swf.isHeaderModified();
        version = swf.version;
        this.refresh = refresh;
    }

    @Override
    public void doOperation() {
        apply(after, true);
    }

    @Override
    public void undoOperation() {
        apply(before, false);
    }

    private void apply(String name, boolean forward) {
        if (name == null) {
            parent.removeTag(nameTag);
        } else {
            nameTag.name = name;
            nameTag.setTimelined(parent);
            if (parent.getTags().indexOf(nameTag) < 0) {
                parent.addTag(Math.max(0, Math.min(index, parent.getTags().size())), nameTag);
            }
        }
        nameTag.setModified(forward || tagModified);
        swf.version = forward && after != null ? Math.max(3, version) : version;
        swf.setHeaderModified(forward ? headerModified || swf.version != version : headerModified);
        parent.setModified(forward || parentModified);
        swf.setModified(forward || swfModified);
        swf.updateCharacters();
        parent.resetTimeline();
        if (refresh != null) {
            refresh.run();
        }
    }
}
