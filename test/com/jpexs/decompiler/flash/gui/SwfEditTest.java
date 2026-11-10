/*
 *  Copyright (C) 2024-2026 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.gui;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.easygui.UndoManager;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.DoABC2Tag;
import com.jpexs.decompiler.flash.tags.MetadataTag;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class SwfEditTest {

    @Test
    public void restoresAnInPlaceEditAndHeaderOnBothSidesOfSave() throws Exception {
        SWF swf = new SWF();
        MetadataTag tag = metadata(swf, "before");
        swf.clearModified();

        UndoManager history = new UndoManager();
        Map<SWF, SwfEdit.State> before = capture(history, swf);
        tag.xmlMetadata = "after";
        tag.setModified(true);
        swf.frameRate = 32;
        swf.setHeaderModified(true);
        record(history, before);
        swf.clearModified();

        history.undo(swf);
        assertSame(swf.getTags().get(0), tag);
        assertEquals(tag.xmlMetadata, "before");
        assertTrue(swf.isModified());
        assertEquals(((MetadataTag) reload(swf).getTags().get(0)).xmlMetadata, "before");

        history.redo(swf);
        assertEquals(swf.frameRate, 32.0f);
        assertFalse(swf.isModified());
        assertEquals(((MetadataTag) reload(swf).getTags().get(0)).xmlMetadata, "after");
    }

    @Test
    public void movesNestedTagsBetweenMoviesAsOneEdit() throws Exception {
        SWF source = new SWF();
        SWF target = new SWF();
        DefineSpriteTag sprite = new DefineSpriteTag(source);
        source.addTag(sprite);
        sprite.setTimelined(source);
        MetadataTag tag = new MetadataTag(source);
        tag.xmlMetadata = "nested";
        tag.setTimelined(sprite);
        sprite.addTag(tag);
        source.clearModified();
        target.clearModified();

        UndoManager history = new UndoManager();
        Map<SWF, SwfEdit.State> before = capture(history, source, target);
        sprite.removeTag(tag);
        target.addTag(tag);
        tag.setSwf(target);
        tag.setTimelined(target);
        record(history, before);

        Map<SWF, SwfEdit.State> later = capture(history, target);
        tag.xmlMetadata = "later";
        tag.setModified(true);
        record(history, later);
        assertFalse(history.canUndo(source));

        history.undo(target);
        history.undo(source);
        assertSame(sprite.getTags().get(0), tag);
        assertSame(tag.getTimelined(), sprite);
        assertSame(tag.getSwf(), source);
        assertEquals(tag.xmlMetadata, "nested");
        assertFalse(source.isModified());
        assertFalse(target.isModified());

        history.redo(target);
        assertSame(target.getTags().get(0), tag);
        assertSame(tag.getSwf(), target);
        assertTrue(sprite.getTags().isEmpty());

        history.undo(source);
        before = capture(history, source);
        metadata(source, "new branch");
        record(history, before);
        assertFalse(history.canRedo(target));
    }

    @Test
    public void keepsAbcIdentityAndRestoresItsConstantPool() {
        SWF swf = new SWF();
        DoABC2Tag tag = new DoABC2Tag(swf);
        tag.setTimelined(swf);
        swf.addTag(tag);
        swf.clearModified();
        ABC abc = tag.getABC();

        UndoManager history = new UndoManager();
        Map<SWF, SwfEdit.State> before = capture(history, swf);
        int originalCount = abc.constants.getStringCount();
        int added = abc.constants.getStringId("added", true);
        tag.setModified(true);
        record(history, before);

        history.undo(swf);
        assertSame(tag.getABC(), abc);
        assertEquals(abc.constants.getStringCount(), originalCount);

        history.redo(swf);
        assertSame(tag.getABC(), abc);
        assertEquals(abc.constants.getString(added), "added");
    }

    @Test
    public void restoresDeletedCookieWithoutMarkingMovieDirty() throws Exception {
        Path file = Files.createTempFile("ffdec-undo-test-", ".sol");
        try {
            byte[] contents = {1, 2, 3};
            Files.write(file, contents);
            SWF swf = new SWF();
            UndoManager history = new UndoManager();
            Map<SWF, SwfEdit.State> before = capture(history, swf);
            before.get(swf).trackCookie(file);
            Files.delete(file);
            record(history, before);
            assertFalse(swf.isModified());

            history.undo(swf);
            assertEquals(Files.readAllBytes(file), contents);
            assertFalse(swf.isModified());
            history.redo(swf);
            assertFalse(Files.exists(file));
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    public void plansMultipleStepsAcrossMoviesInReplayOrder() {
        SWF source = new SWF();
        SWF target = new SWF();
        float sourceOriginal = source.frameRate;
        float targetOriginal = target.frameRate;
        UndoManager history = new UndoManager();
        editFrameRate(history, "shared first", 30, source, target);
        editFrameRate(history, "shared second", 32, source, target);
        editFrameRate(history, "target only", 34, target);
        editFrameRate(history, "source only", 36, source);

        UndoManager.ReplayPlan undoSource = history.planReplay(source, false, 10);
        assertEquals(undoSource.size(), 1);
        assertTrue(history.replayStep(undoSource, 0));
        assertEquals(history.planReplay(source, false, 10).size(), 0);

        UndoManager.ReplayPlan undoTarget = history.planReplay(target, false, 10);
        assertEquals(descriptions(undoTarget), Arrays.asList("target only", "shared second", "shared first"));
        assertTrue(history.canReplay(undoTarget, 3));
        for (int index = 0; index < undoTarget.size(); index++) {
            assertTrue(history.replayStep(undoTarget, index));
        }
        assertEquals(source.frameRate, sourceOriginal);
        assertEquals(target.frameRate, targetOriginal);
        assertFalse(source.isModified());
        assertFalse(target.isModified());

        UndoManager.ReplayPlan redo = history.planReplay(source, true, 10);
        assertEquals(descriptions(redo), Arrays.asList("shared first", "shared second", "source only"));
        assertTrue(history.canReplay(redo, 2));
        assertTrue(history.replayStep(redo, 0));
        assertTrue(history.replayStep(redo, 1));
        assertEquals(source.frameRate, 32.0f);
        assertEquals(target.frameRate, 32.0f);
        assertEquals(history.getRedoName(source), "source only");
    }

    @Test
    public void rejectsAPlanFromAReplacedBranchEvenWithTheSameDescription() {
        SWF swf = new SWF();
        UndoManager history = new UndoManager();
        editFrameRate(history, "edit header", 30, swf);
        UndoManager.ReplayPlan plan = history.planReplay(swf, false, 10);
        history.undo(swf);
        editFrameRate(history, "edit header", 36, swf);

        assertFalse(history.canReplay(plan, 1));
        assertFalse(history.replayStep(plan, 0));
        assertEquals(swf.frameRate, 36.0f);
    }

    private static void editFrameRate(UndoManager history, String name, float frameRate, SWF... swfs) {
        Map<SWF, SwfEdit.State> before = capture(history, swfs);
        for (SWF swf : swfs) {
            swf.frameRate = frameRate;
            swf.setHeaderModified(true);
        }
        SwfEdit edit = new SwfEdit(name, before);
        history.recordOperation(edit, edit.getMovies());
    }

    private static List<String> descriptions(UndoManager.ReplayPlan plan) {
        List<String> result = new ArrayList<>();
        for (int index = 0; index < plan.size(); index++) {
            result.add(plan.getOperation(index).getDescription());
        }
        return result;
    }

    private static MetadataTag metadata(SWF swf, String xml) {
        MetadataTag tag = new MetadataTag(swf);
        tag.xmlMetadata = xml;
        tag.setTimelined(swf);
        swf.addTag(tag);
        return tag;
    }

    private static Map<SWF, SwfEdit.State> capture(UndoManager history, SWF... swfs) {
        Map<SWF, SwfEdit.State> result = new LinkedHashMap<>();
        for (SWF swf : swfs) {
            history.rememberSavedPosition(swf);
            result.put(swf, new SwfEdit.State(swf));
        }
        return result;
    }

    private static void record(UndoManager history, Map<SWF, SwfEdit.State> before) {
        SwfEdit edit = new SwfEdit("edit", before);
        assertFalse(edit.getMovies().isEmpty());
        history.recordOperation(edit, edit.getMovies());
    }

    private static SWF reload(SWF swf) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        swf.saveTo(output);
        return new SWF(new ByteArrayInputStream(output.toByteArray()), false);
    }
}
