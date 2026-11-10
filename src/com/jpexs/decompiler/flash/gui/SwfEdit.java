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
import com.jpexs.decompiler.flash.SWFCompression;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.easygui.DoableOperation;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.flash.tags.DefineBinaryDataTag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.TagStub;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.helpers.ByteArrayRange;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Records legacy GUI edits that mutate tags in place. Each entry retains only
 * the changed payloads and tag lists, with the live tag identities preserved
 * for earlier and later editor operations.
 */
final class SwfEdit implements DoableOperation {

    private static final Logger logger = Logger.getLogger(SwfEdit.class.getName());

    private final String description;
    private final Map<SWF, State> before;
    private final Map<SWF, State> after = new LinkedHashMap<>();

    SwfEdit(String description, Map<SWF, State> before) {
        this.description = description;
        this.before = before;
        before.entrySet().removeIf(entry -> {
            SWF swf = entry.getKey();
            State next = new State(swf);
            State original = entry.getValue();
            original.cookies.entrySet().removeIf(cookie -> {
                try {
                    next.trackCookie(cookie.getKey());
                    return false;
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, "Cannot record shared object edit", ex);
                    return true;
                }
            });
            original.keepChanges(next);
            if (next.isEmpty() && original.isEmpty()) {
                if (!original.wasModified && swf.isModified()) {
                    swf.clearModified();
                }
                return true;
            }
            // Some ABC operations edit the constant pool without marking the
            // container. Ensure the new contents will be written on Save.
            next.tags.forEach((tag, state) -> {
                if (tag instanceof ABCContainerTag && !tag.isModified()
                        && !TagState.equalBytes(state.bytes, TagState.originalPayload(tag))) {
                    tag.setModified(true);
                }
            });
            after.put(swf, next);
            return false;
        });
    }

    Set<SWF> getMovies() {
        return before.keySet();
    }

    @Override
    public boolean modifiesSwf(SWF swf) {
        return before.get(swf).hasSwfChanges() || after.get(swf).hasSwfChanges();
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void doOperation() {
        replay(after, before);
    }

    @Override
    public void undoOperation() {
        replay(before, after);
    }

    private void replay(Map<SWF, State> target, Map<SWF, State> previous) {
        Map<Path, byte[]> files = new LinkedHashMap<>();
        Map<Path, byte[]> rollbackFiles = new LinkedHashMap<>();
        for (State state : target.values()) {
            files.putAll(state.cookies);
        }
        try {
            for (State state : previous.values()) {
                for (Map.Entry<Path, byte[]> entry : state.cookies.entrySet()) {
                    Path file = entry.getKey();
                    if (!Arrays.equals(readCookie(file), entry.getValue())) {
                        throw new IOException("Shared object changed outside FFDec: " + file);
                    }
                    rollbackFiles.put(file, entry.getValue());
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }

        List<Path> written = new ArrayList<>();
        try {
            for (Map.Entry<Path, byte[]> entry : files.entrySet()) {
                writeCookie(entry.getKey(), entry.getValue());
                written.add(entry.getKey());
            }
            apply(target, previous);
        } catch (IOException | InterruptedException | RuntimeException ex) {
            for (Path file : written) {
                try {
                    writeCookie(file, rollbackFiles.get(file));
                } catch (IOException rollbackError) {
                    ex.addSuppressed(rollbackError);
                }
            }
            try {
                apply(previous, target);
            } catch (IOException | InterruptedException | RuntimeException rollbackError) {
                ex.addSuppressed(rollbackError);
            }
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException(ex);
        }
    }

    private void apply(Map<SWF, State> states, Map<SWF, State> previous) throws IOException, InterruptedException {
        Map<SWF, List<ABC>> previousAbcs = new LinkedHashMap<>();
        states.forEach((swf, state) -> {
            if (state.changesAbc() || previous.get(swf).changesAbc()) {
                swf.clearAbcListCache();
                List<ABC> abcs = new ArrayList<>();
                for (ABCContainerTag container : swf.getAbcList()) {
                    abcs.add(container.getABC());
                }
                previousAbcs.put(swf, abcs);
            }
        });
        states.forEach((swf, state) -> state.restoreStructure(swf));
        for (Map.Entry<SWF, State> entry : states.entrySet()) {
            entry.getValue().restorePayloads(entry.getKey());
        }
        states.forEach((swf, state) -> {
            if (!modifiesSwf(swf)) {
                return;
            }
            swf.clearAbcListCache();
            swf.clearAllCache();
            swf.clearSoundCache();
            swf.assignExportNamesToSymbols();
            swf.assignClassesToSymbols();
            swf.resetTimelines(swf);
        });
        previousAbcs.forEach(SWF::refreshAbcIndex);
        states.values().forEach(state -> state.tags.keySet().forEach(tag -> {
            if (tag instanceof ABCContainerTag) {
                ((ABCContainerTag) tag).getABC().fireChanged();
            }
        }));
    }

    private static byte[] readCookie(Path file) throws IOException {
        try {
            return Files.readAllBytes(file);
        } catch (NoSuchFileException ex) {
            return null;
        }
    }

    static void writeCookie(Path file, byte[] bytes) throws IOException {
        if (bytes == null) {
            Files.deleteIfExists(file);
            return;
        }
        Path temp = Files.createTempFile(file.getParent(), "ffdec-", ".sol");
        try {
            Files.write(temp, bytes);
            try {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    static final class State {

        private Header header;
        private final boolean wasModified;
        private final Map<Path, byte[]> cookies = new LinkedHashMap<>();
        private final Map<Timelined, List<Tag>> timelines = new LinkedHashMap<>();
        private final Map<Tag, TagState> tags = new LinkedHashMap<>();

        State(SWF swf) {
            header = new Header(swf);
            wasModified = swf.isModified();
            capture(swf);
        }

        void useOriginalTagState(Tag tag, TagState original) {
            tags.replace(tag, original);
        }

        void trackCookie(Path file) throws IOException {
            if (!cookies.containsKey(file)) {
                cookies.put(file, readCookie(file));
            }
        }

        private void capture(Timelined timeline) {
            List<Tag> list = new ArrayList<>();
            for (Tag tag : timeline.getTags()) {
                list.add(tag);
                tags.put(tag, new TagState(tag));
                if (tag instanceof DefineSpriteTag) {
                    capture((DefineSpriteTag) tag);
                }
            }
            timelines.put(timeline, list);
        }

        private void keepChanges(State next) {
            cookies.entrySet().removeIf(entry -> {
                if (Arrays.equals(entry.getValue(), next.cookies.get(entry.getKey()))) {
                    next.cookies.remove(entry.getKey());
                    return true;
                }
                return false;
            });
            if (header.matches(next.header)) {
                header = null;
                next.header = null;
            }
            timelines.entrySet().removeIf(entry -> {
                if (entry.getValue().equals(next.timelines.get(entry.getKey()))) {
                    next.timelines.remove(entry.getKey());
                    return true;
                }
                return false;
            });
            tags.entrySet().removeIf(entry -> {
                if (entry.getValue().matches(next.tags.get(entry.getKey()))) {
                    next.tags.remove(entry.getKey());
                    return true;
                }
                return false;
            });
        }

        private boolean changesAbc() {
            return tags.keySet().stream().anyMatch(ABCContainerTag.class::isInstance);
        }

        private boolean isEmpty() {
            return !hasSwfChanges() && cookies.isEmpty();
        }

        private boolean hasSwfChanges() {
            return header != null || !timelines.isEmpty() || !tags.isEmpty();
        }

        private void restoreStructure(SWF swf) {
            if (header != null) {
                header.restore(swf);
            }
            timelines.forEach((timeline, list) -> {
                if (timeline instanceof SWF) {
                    ((SWF) timeline).replaceTags(list);
                } else {
                    ((DefineSpriteTag) timeline).replaceTags(list);
                }
                for (Tag tag : list) {
                    tag.setSwf(swf);
                    tag.setTimelined(timeline);
                }
            });
        }

        private void restorePayloads(SWF swf) throws IOException, InterruptedException {
            for (Map.Entry<Tag, TagState> entry : tags.entrySet()) {
                entry.getValue().restore(entry.getKey(), swf);
            }
        }
    }

    private static final class Header {

        private final int version;
        private final SWFCompression compression;
        private final boolean gfx;
        private final boolean encrypted;
        private final boolean hasEndTag;
        private final int frameCount;
        private final float frameRate;
        private final RECT rect;

        Header(SWF swf) {
            version = swf.version;
            compression = swf.compression;
            gfx = swf.gfx;
            encrypted = swf.encrypted;
            hasEndTag = swf.hasEndTag;
            frameCount = swf.frameCount;
            frameRate = swf.frameRate;
            rect = new RECT(swf.displayRect);
        }

        boolean matches(Header other) {
            return version == other.version && compression == other.compression
                    && gfx == other.gfx && encrypted == other.encrypted && hasEndTag == other.hasEndTag
                    && frameCount == other.frameCount && frameRate == other.frameRate
                    && rect.Xmin == other.rect.Xmin && rect.Xmax == other.rect.Xmax
                    && rect.Ymin == other.rect.Ymin && rect.Ymax == other.rect.Ymax;
        }

        void restore(SWF swf) {
            swf.version = version;
            swf.compression = compression;
            swf.gfx = gfx;
            swf.encrypted = encrypted;
            swf.hasEndTag = hasEndTag;
            swf.frameCount = frameCount;
            swf.frameRate = frameRate;
            swf.displayRect = new RECT(rect);
            swf.setHeaderModified(true);
        }
    }

    static final class TagState {

        private final ByteArrayRange bytes;
        private final boolean longHeader;
        private final int spriteId;
        private final int spriteFrames;
        private final boolean spriteEnd;
        private final SWF embeddedMovie;

        TagState(Tag tag) {
            longHeader = tag.forceWriteAsLong;
            embeddedMovie = tag instanceof DefineBinaryDataTag ? ((DefineBinaryDataTag) tag).getInnerSwf() : null;
            if (tag instanceof DefineSpriteTag) {
                DefineSpriteTag sprite = (DefineSpriteTag) tag;
                spriteId = sprite.spriteId;
                spriteFrames = sprite.frameCount;
                spriteEnd = sprite.hasEndTag;
                bytes = sprite.remainingData;
            } else {
                spriteId = 0;
                spriteFrames = 0;
                spriteEnd = false;
                ByteArrayRange original = originalPayload(tag);
                if (original != null && !tag.isModified() && !(tag instanceof ABCContainerTag)) {
                    bytes = original;
                } else {
                    bytes = new ByteArrayRange(tag.getData());
                }
            }
        }

        private static ByteArrayRange originalPayload(Tag tag) {
            ByteArrayRange original = tag.getOriginalRange();
            if (original == null) {
                return null;
            }
            int length = tag.getOriginalDataLength();
            return original.getSubRange(original.getLength() - length, length);
        }

        boolean matches(TagState other) {
            return other != null && longHeader == other.longHeader && spriteId == other.spriteId
                    && spriteFrames == other.spriteFrames && spriteEnd == other.spriteEnd
                    && embeddedMovie == other.embeddedMovie && equalBytes(bytes, other.bytes);
        }

        private static boolean equalBytes(ByteArrayRange first, ByteArrayRange second) {
            if (first == second) {
                return true;
            }
            if (first == null || second == null || first.getLength() != second.getLength()) {
                return false;
            }
            if (first.getArray() == second.getArray() && first.getPos() == second.getPos()) {
                return true;
            }
            for (int i = 0; i < first.getLength(); i++) {
                if (first.get(i) != second.get(i)) {
                    return false;
                }
            }
            return true;
        }

        void restore(Tag tag, SWF swf) throws IOException, InterruptedException {
            boolean headerChanged = tag.forceWriteAsLong != longHeader;
            tag.forceWriteAsLong = longHeader;
            if (tag instanceof TagStub) {
                return;
            }
            if (tag instanceof DefineSpriteTag) {
                DefineSpriteTag sprite = (DefineSpriteTag) tag;
                sprite.spriteId = spriteId;
                sprite.frameCount = spriteFrames;
                sprite.hasEndTag = spriteEnd;
                sprite.remainingData = bytes;
                sprite.resetTimeline();
            } else {
                ABCContainerTag container = tag instanceof ABCContainerTag ? (ABCContainerTag) tag : null;
                ABC original = container == null ? null : container.getABC();
                try (SWFInputStream stream = new SWFInputStream(swf, bytes.getArray(), 0, bytes.getPos() + bytes.getLength())) {
                    stream.seek(bytes.getPos());
                    tag.readData(stream, bytes, 0, false, false, false);
                    tag.remainingData = stream.available() == 0 ? null : stream.readByteRangeEx(stream.available(), "remaining");
                }
                if (original != null) {
                    // Keep references held by the ABC explorer and by the
                    // open-file list for standalone ABCs.
                    original.restoreContents(container.getABC());
                    container.setABC(original);
                }
                if (tag instanceof DefineBinaryDataTag) {
                    ((DefineBinaryDataTag) tag).setInnerSwf(embeddedMovie != null && !embeddedMovie.isDestroyed() ? embeddedMovie : null);
                }
            }
            tag.setModified(headerChanged || tag instanceof DefineSpriteTag || !equalBytes(bytes, originalPayload(tag)));
            if (tag instanceof Timelined) {
                ((Timelined) tag).resetTimeline();
            }
            Timelined parent = tag.getTimelined();
            if (parent != null) {
                parent.resetTimeline();
            }
        }
    }
}
