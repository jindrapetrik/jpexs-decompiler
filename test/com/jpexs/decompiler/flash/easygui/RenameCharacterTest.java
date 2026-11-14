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
import com.jpexs.decompiler.flash.gui.taglistview.TagListTreeModel;
import com.jpexs.decompiler.flash.tags.DefineBitsLossless2Tag;
import com.jpexs.decompiler.flash.tags.DefineButton2Tag;
import com.jpexs.decompiler.flash.tags.DefineShapeTag;
import com.jpexs.decompiler.flash.tags.DefineSoundTag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.NameCharacterTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collections;
import javax.swing.JLabel;
import javax.swing.tree.DefaultMutableTreeNode;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class RenameCharacterTest {

    @DataProvider(name = "characters")
    public Object[][] characters() {
        return new Object[][]{
            {new DefineBitsLossless2Tag(new SWF()), NameCharacterTag.TYPE_BITMAP},
            {new DefineShapeTag(new SWF()), NameCharacterTag.TYPE_SYMBOL},
            {new DefineSpriteTag(new SWF()), NameCharacterTag.TYPE_SYMBOL},
            {new DefineButton2Tag(new SWF()), NameCharacterTag.TYPE_SYMBOL},
            {new DefineSoundTag(new SWF()), NameCharacterTag.TYPE_SOUND}
        };
    }

    @Test(dataProvider = "characters")
    public void namesSurviveSavingAndUndoRedo(CharacterTag character, int type) throws Exception {
        SWF swf = character.getSwf();
        swf.addTag(character);
        swf.clearModified();
        EasyTagNameResolver resolver = new EasyTagNameResolver();
        String generated = resolver.getTagName(character);
        assertFalse(character.getNameProperties().containsKey("nm"));
        String custom = "My \u017e shape name";
        UndoManager undo = new UndoManager();
        undo.doOperation(new RenameCharacterOperation(character, custom, null), swf);
        NameCharacterTag name = (NameCharacterTag) swf.getCharacterIdTag(character.getCharacterId(), NameCharacterTag.ID);
        assertNotNull(name);
        assertEquals(character.getNameProperties().get("nm"), "\"" + custom + "\"");
        assertEquals(name.type, type);
        assertTrue(swf.getTags().indexOf(name) > swf.getTags().indexOf(character));
        assertEquals(resolver.getTagName(character), custom + " (" + generated + ")");
        JLabel label = new JLabel();
        LibraryTreeTable.styleLibraryNode(label, new DefaultMutableTreeNode(character));
        assertEquals(label.getText(), resolver.getTagName(character));
        assertTrue(swf.isModified());

        SWF restored = reload(swf);
        CharacterTag restoredCharacter = restored.getCharacter(character.getCharacterId());
        assertEquals(restoredCharacter.getNameProperties().get("nm"), "\"" + custom + "\"");
        assertEquals(resolver.getTagName(restoredCharacter), custom + " (" + generated + ")");
        assertEquals(((NameCharacterTag) restored.getCharacterIdTag(character.getCharacterId(), NameCharacterTag.ID)).type, type);

        undo.undo(swf);
        assertNull(swf.getCharacterIdTag(character.getCharacterId(), NameCharacterTag.ID));
        assertFalse(character.getNameProperties().containsKey("nm"));
        assertEquals(resolver.getTagName(character), generated);
        assertFalse(swf.isModified());
        undo.redo(swf);
        assertEquals(resolver.getTagName(character), custom + " (" + generated + ")");

        undo.doOperation(new RenameCharacterOperation(character, "Second name", null), swf);
        assertSame(swf.getCharacterIdTag(character.getCharacterId(), NameCharacterTag.ID), name);
        assertEquals(resolver.getTagName(character), "Second name (" + generated + ")");
        assertEquals(character.getNameProperties().get("nm"), "\"Second name\"");
        undo.undo(swf);
        assertEquals(name.name, custom);
        undo.redo(swf);

        undo.doOperation(new RenameCharacterOperation(character, "", null), swf);
        assertEquals(resolver.getTagName(character), generated);
        assertNull(reload(swf).getCharacterIdTag(character.getCharacterId(), NameCharacterTag.ID));
        assertFalse(character.getNameProperties().containsKey("nm"));
        undo.undo(swf);
        assertSame(swf.getCharacterIdTag(character.getCharacterId(), NameCharacterTag.ID), name);
        assertEquals(name.name, "Second name");
    }

    @Test
    public void existingNameAndVersionAreRestored() throws Exception {
        SWF swf = new SWF();
        DefineShapeTag character = new DefineShapeTag(swf);
        swf.addTag(character);
        NameCharacterTag name = new NameCharacterTag(swf);
        name.characterId = character.getCharacterId();
        name.name = "Existing name";
        swf.addTag(name);
        swf.clearModified();
        RenameCharacterOperation change = new RenameCharacterOperation(character, "New name", null);
        change.doOperation();
        assertEquals(((NameCharacterTag) reload(swf).getCharacterIdTag(character.getCharacterId(), NameCharacterTag.ID)).name, "New name");
        change.undoOperation();
        assertEquals(name.name, "Existing name");
        assertFalse(name.isModified());
        assertFalse(swf.isModified());

        swf.removeTag(name);
        swf.version = 2;
        swf.setHeaderModified(false);
        swf.setModified(false);
        RenameCharacterOperation create = new RenameCharacterOperation(character, "Name", null);
        create.doOperation();
        assertEquals(swf.version, 3);
        assertTrue(swf.isHeaderModified());
        create.undoOperation();
        assertEquals(swf.version, 2);
        assertFalse(swf.isHeaderModified());
        assertFalse(swf.isModified());
    }

    @DataProvider(name = "owners")
    public Object[][] owners() {
        return new Object[][]{{false}, {true}};
    }

    @Test(dataProvider = "owners")
    public void tagListUpdatesWithoutReloading(boolean nested) {
        SWF swf = new SWF();
        Timelined owner = swf;
        if (nested) {
            DefineSpriteTag sprite = new DefineSpriteTag(swf);
            swf.addTag(sprite);
            owner = sprite;
        }
        DefineShapeTag character = new DefineShapeTag(swf);
        character.setTimelined(owner);
        owner.addTag(character);
        owner.addTag(new ShowFrameTag(swf));
        TagListTreeModel model = new TagListTreeModel(Collections.emptyList());
        int frameIndex = nested ? 0 : 1;
        TreeItem frame = model.getChild(owner, frameIndex);
        int originalCount = model.getChildCount(frame);
        RenameCharacterOperation rename = new RenameCharacterOperation(character, "Library name", null);

        rename.doOperation();
        frame = model.getChild(owner, frameIndex);
        assertEquals(model.getChildCount(frame), originalCount + 1);
        NameCharacterTag name = (NameCharacterTag) swf.getCharacterIdTag(character.getCharacterId(), NameCharacterTag.ID);
        assertSame(model.getChild(frame, 1), name);

        rename.undoOperation();
        assertEquals(model.getChildCount(model.getChild(owner, frameIndex)), originalCount);
        rename.doOperation();
        assertEquals(model.getChildCount(model.getChild(owner, frameIndex)), originalCount + 1);

        RenameCharacterOperation clear = new RenameCharacterOperation(character, "", null);
        clear.doOperation();
        assertEquals(model.getChildCount(model.getChild(owner, frameIndex)), originalCount);
        clear.undoOperation();
        assertSame(model.getChild(model.getChild(owner, frameIndex), 1), name);
    }

    private static SWF reload(SWF swf) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        swf.saveTo(bytes);
        return new SWF(new ByteArrayInputStream(bytes.toByteArray()), false);
    }
}
