/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.flash.iggy;

import com.jpexs.decompiler.flash.iggy.streams.IggyFixup;
import com.jpexs.decompiler.flash.iggy.streams.IggyIndexBuilder;
import com.jpexs.decompiler.flash.iggy.streams.MemoryDataStream;
import com.jpexs.decompiler.flash.iggy.streams.ReadDataStreamInterface;
import com.jpexs.decompiler.flash.iggy.streams.SeekMode;
import com.jpexs.decompiler.flash.iggy.streams.StructureInterface;
import com.jpexs.decompiler.flash.iggy.streams.WriteDataStreamInterface;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The main movie/library block. Unknown objects remain in the original graph.
 */
public class IggySwf implements StructureInterface {

    public static final class DictionaryEntry {

        private final int id;
        private final long address;
        private final int type;

        private DictionaryEntry(int id, long address, int type) {
            this.id = id;
            this.address = address;
            this.type = type;
        }

        public int getId() {
            return id;
        }

        public long getAddress() {
            return address;
        }

        public int getType() {
            return type;
        }

        public boolean isNull() {
            return address == 0;
        }
    }

    private IggyFlashHeaderInterface hdr;
    private final boolean is64;
    private final int pointerSize;
    private String name;
    private String sourceName;
    private byte[] originalData;
    private final List<IggyFont> fonts = new ArrayList<>();
    private final List<IggyText> texts = new ArrayList<>();
    private final List<IggyFont> addFonts = new ArrayList<>();
    private final List<IggyText> addTexts = new ArrayList<>();
    private final List<DictionaryEntry> dictionary = new ArrayList<>();
    private final List<IggyNamedFrame> frameLabels = new ArrayList<>();
    private final List<IggyNamedFrame> scenes = new ArrayList<>();
    private final List<IggySymbolClass> symbolClasses = new ArrayList<>();
    private final List<IggyImport> imports = new ArrayList<>();
    private List<IggyImport> importChanges;
    private List<IggySymbolClass> symbolChanges;
    private List<IggyNamedFrame> sceneChanges;
    private List<IggyNamedFrame> frameLabelChanges;
    private Boolean as3Change;
    private final Map<Integer, float[]> scalingGridChanges = new LinkedHashMap<>();
    private IggyTimeline timeline;
    private final Map<Long, Integer> dictionaryIds = new LinkedHashMap<>();
    private final Map<Long, Integer> dictionaryReferenceIds = new LinkedHashMap<>();
    private String textureResourceName = "";
    private final Set<Integer> removedDefinitions = new HashSet<>();
    private final Map<Integer, IggyObjectData> objectChanges = new LinkedHashMap<>();
    private final Map<Integer, byte[]> textureChanges = new LinkedHashMap<>();
    private final Map<Integer, IggyTimeline.Replacement> timelineChanges = new LinkedHashMap<>();
    private final Map<Integer, IggyTimeline> spriteTimelines = new LinkedHashMap<>();
    private final List<Long> dictionaryFixupEntries = new ArrayList<>();
    private final List<IggyDeclStrings> abcBlocks = new ArrayList<>();
    private final List<IggyDeclStrings> originalAbcBlocks = new ArrayList<>();
    private final IdentityHashMap<StructureInterface, Long> addresses = new IdentityHashMap<>();
    private final IdentityHashMap<StructureInterface, byte[]> snapshots = new IdentityHashMap<>();
    private final List<IggyFixup.Field> originalFields = new ArrayList<>();
    private final Map<Long, String> symbolClassNames = new LinkedHashMap<>();
    private final Set<Integer> originalTextSymbols = new HashSet<>();

    public IggySwf(ReadDataStreamInterface stream) throws IOException {
        this(stream, Collections.emptyList());
    }

    public IggySwf(ReadDataStreamInterface stream, List<IggyFixup.Field> fixups) throws IOException {
        this(stream, fixups, true);
    }

    public IggySwf(ReadDataStreamInterface stream, List<IggyFixup.Field> fixups, boolean is64) throws IOException {
        this.is64 = is64;
        pointerSize = is64 ? 8 : 4;
        originalFields.addAll(fixups);
        readFromDataStream(stream);
    }

    public IggyFlashHeaderInterface getHdr() {
        return hdr;
    }

    public String getName() {
        return name;
    }

    public String getSourceName() {
        return sourceName;
    }

    void setDefaultName(String defaultName) {
        if ((sourceName == null || sourceName.isEmpty()) && hdr.getFlashFilenameAddress() == 0) {
            name = defaultName;
        }
    }

    public List<IggyFont> getFonts() {
        return fonts;
    }

    public List<IggyText> getTexts() {
        return texts;
    }

    public List<IggyFont> getAddFonts() {
        return addFonts;
    }

    public List<IggyText> getAddTexts() {
        return addTexts;
    }

    public List<DictionaryEntry> getDictionary() {
        return Collections.unmodifiableList(dictionary);
    }

    public List<IggyNamedFrame> getFrameLabels() {
        return Collections.unmodifiableList(frameLabelChanges == null ? frameLabels : frameLabelChanges);
    }

    public List<IggyNamedFrame> getScenes() {
        return Collections.unmodifiableList(sceneChanges == null ? scenes : sceneChanges);
    }

    public List<IggySymbolClass> getSymbolClasses() {
        List<IggySymbolClass> result = new ArrayList<>();
        for (IggySymbolClass symbol : symbolChanges == null ? symbolClasses : symbolChanges) {
            String name = symbolClassNames.containsKey(symbol.getCharacterId()) ? symbolClassNames.get(symbol.getCharacterId()) : symbol.getName();
            result.add(new IggySymbolClass(symbol.getObjectAddress(), name, symbol.getCharacterId(), symbol.getReserved()));
        }
        return Collections.unmodifiableList(result);
    }

    public List<IggyImport> getImports() {
        return Collections.unmodifiableList(importChanges == null ? imports : importChanges);
    }

    /**
     * Source declarations, retained as the baseline for repeated SWF updates.
     */
    public List<IggyImport> getOriginalImports() {
        return Collections.unmodifiableList(imports);
    }

    /**
     * Replace library dependencies while preserving an unchanged source table.
     */
    public void setImports(List<IggyImport> libraries) throws IOException {
        if (libraries.size() > 65535) {
            throw new IOException("Too many Iggy library imports");
        }
        Set<Integer> ids = new HashSet<>();
        Set<Integer> originalIds = importIds(imports);
        for (IggyImport library : libraries) {
            importName(library.getLibrary());
            if (library.getSymbols().size() > 65535) {
                throw new IOException("Too many Iggy imported symbols");
            }
            for (IggySymbolClass symbol : library.getSymbols()) {
                long id = symbol.getCharacterId();
                importName(symbol.getName());
                if (id <= 0 || id > 65535 || !ids.add((int) id)) {
                    throw new IOException("Invalid or duplicate Iggy import character ID " + id);
                }
                if (id < dictionary.size() && !dictionary.get((int) id).isNull()
                        && dictionary.get((int) id).type != 18 && !originalIds.contains((int) id)) {
                    throw new IOException("Iggy import ID is occupied by a local object: " + id);
                }
            }
        }
        boolean same = libraries.size() == imports.size();
        for (int i = 0; same && i < libraries.size(); i++) {
            IggyImport before = imports.get(i);
            IggyImport after = libraries.get(i);
            same = java.util.Objects.equals(before.getLibrary(), after.getLibrary())
                    && before.getSymbols().size() == after.getSymbols().size();
            for (int j = 0; same && j < before.getSymbols().size(); j++) {
                IggySymbolClass a = before.getSymbols().get(j);
                IggySymbolClass b = after.getSymbols().get(j);
                same = a.getCharacterId() == b.getCharacterId() && java.util.Objects.equals(a.getName(), b.getName());
            }
        }
        if (!same && !is64 && !(hdr instanceof IggyFlashHeader32)) {
            throw new IOException("Library import table is unknown for the Iggy 32 legacy layout");
        }
        importChanges = same ? null : new ArrayList<>(libraries);
    }

    private static Set<Integer> importIds(List<IggyImport> libraries) {
        Set<Integer> result = new HashSet<>();
        for (IggyImport library : libraries) {
            for (IggySymbolClass symbol : library.getSymbols()) {
                result.add((int) symbol.getCharacterId());
            }
        }
        return result;
    }

    private static void importName(String name) throws IOException {
        if (name == null || name.isEmpty() || name.indexOf('\0') >= 0) {
            throw new IOException("Invalid Iggy library or import symbol name");
        }
    }

    public IggyTimeline getTimeline() {
        return timeline;
    }

    public Map<Integer, IggyTimeline> getSpriteTimelines() {
        return Collections.unmodifiableMap(spriteTimelines);
    }

    public boolean is64() {
        return is64;
    }

    public int getDictionaryId(long referenceField, long address) throws IOException {
        Integer imported = dictionaryReferenceIds.get(referenceField);
        if (imported != null) {
            return imported;
        }
        if (address == 0) {
            return -1;
        }
        Integer id = dictionaryIds.get(address);
        if (id == null) {
            throw new IOException("Timeline object is outside the Iggy dictionary");
        }
        return id;
    }

    public int getTimelineFrameCount(int characterId) throws IOException {
        if (characterId == 0) {
            MemoryDataStream s = new MemoryDataStream(originalData);
            s.seek(is64 ? 68 : hdr instanceof IggyFlashHeader32 ? 44 : 32, SeekMode.SET);
            return (int) s.readUI32();
        }
        if (!spriteTimelines.containsKey(characterId)) {
            throw new IOException("Unknown Iggy sprite ID");
        }
        MemoryDataStream s = new MemoryDataStream(originalData);
        s.seek(dictionary.get(characterId).address + (is64 ? 40 : 20), SeekMode.SET);
        return s.readUI16();
    }

    public void replaceTimeline(int characterId, IggyTimeline.Replacement replacement) throws IOException {
        if (characterId != 0 && !getSpriteIds().contains(characterId)) {
            throw new IOException("Missing Iggy sprite definition " + characterId);
        }
        if (replacement == null) {
            timelineChanges.remove(characterId);
        } else {
            timelineChanges.put(characterId, replacement);
        }
    }

    /** The original graph is retained; removal disconnects its dictionary slot. */
    public void setActiveDefinitions(Set<Integer> ids) {
        removedDefinitions.clear();
        for (DictionaryEntry entry : dictionary) {
            int type = entry.type;
            if (entry.id > 0 && (type == 1 || type == 2 || type == 3 || type == 4 || type == 5
                    || type == 6 || type == 7 || type == 20 || type == 21 || type == 22) && !ids.contains(entry.id)) {
                removedDefinitions.add(entry.id);
                if (type == 4) {
                    replaceTextureRecord(entry.id, null);
                }
            }
        }
        timelineChanges.keySet().removeIf(id -> id != 0 && !ids.contains(id));
    }

    public boolean isDefinitionRemoved(int id) {
        return removedDefinitions.contains(id);
    }

    public Set<Integer> getSpriteIds() {
        Set<Integer> result = new LinkedHashSet<>(spriteTimelines.keySet());
        for (Map.Entry<Integer, IggyObjectData> entry : objectChanges.entrySet()) {
            if (entry.getValue().type == 3) {
                result.add(entry.getKey());
            }
        }
        result.removeAll(removedDefinitions);
        return result;
    }

    public void replaceDefinition(StructureInterface object) throws IOException {
        boolean font = object instanceof IggyFont;
        int id = font ? ((IggyFont) object).getFontId() : ((IggyText) object).getTextIndex();
        Serialized serialized = serialize(object);
        List<IggyFixup.Field> definitionFields = new ArrayList<>(serialized.fields);
        if (definitionFields.stream().noneMatch(field -> field.offset == 1)) {
            definitionFields.add(new IggyFixup.Field(1, 12));
        }
        // New x86 fonts have no original common-header fixups to inherit.
        long classPointer = 2L * pointerSize;
        if (definitionFields.stream().noneMatch(field -> field.offset == classPointer)) {
            definitionFields.add(new IggyFixup.Field(classPointer, 2));
        }
        int size = font ? (is64 ? 352 : ((IggyFont32) object).getHeaderSize()) : (is64 ? 104 : 68);
        replaceObject(id, new IggyObjectData(serialized.data, definitionFields, Collections.emptyMap(), 0, size, font ? 22 : 6));
    }

    public String getTextureResourceName() {
        return textureResourceName;
    }

    public void setTextureResourceName(String name) {
        textureResourceName = name;
    }

    public void clearAssetChanges() {
        objectChanges.clear();
        textureChanges.clear();
    }

    public void replaceObject(int id, IggyObjectData replacement) throws IOException {
        if (id <= 0 || id > 65535) {
            throw new IOException("Invalid Iggy asset ID");
        }
        objectChanges.put(id, replacement);
    }

    /**
     * A null record removes an external texture superseded by embedded pixels.
     */
    public void replaceTextureRecord(int id, byte[] record) {
        textureChanges.put(id, record == null ? null : record.clone());
    }

    public Map<Integer, byte[]> getTextureChanges() {
        return Collections.unmodifiableMap(textureChanges);
    }

    public List<IggyDeclStrings> getAbcBlocks() {
        return abcBlocks;
    }

    public IggyDeclStrings getDeclStrings() {
        return abcBlocks.isEmpty() ? null : abcBlocks.get(0);
    }

    public byte[] getOriginalData() {
        return originalData.clone();
    }

    public void setSymbolClassName(long characterId, String name) throws IOException {
        if (name == null) {
            throw new IOException("Missing Iggy class name");
        }
        for (IggySymbolClass symbol : symbolClasses) {
            if (symbol.getCharacterId() == characterId) {
                symbolClassNames.put(characterId, name);
                for (IggyText text : texts) {
                    if (text.getTextIndex() == characterId && text.getSymbolClassName() != null) {
                        text.setSymbolClassName(name);
                    }
                }
                return;
            }
        }
        for (IggyText text : texts) {
            if (text.getTextIndex() == characterId) {
                text.setSymbolClassName(name);
                return;
            }
        }
        throw new IOException("Adding new Iggy SymbolClass bindings is not supported");
    }

    public List<IggySymbolClass> getOriginalSymbolClasses() {
        return Collections.unmodifiableList(symbolClasses);
    }

    public boolean hasOriginalTextSymbolClass(int id) {
        return originalTextSymbols.contains(id);
    }

    public boolean hasAS3() {
        return as3Change == null ? hdr.hasAS3() : as3Change;
    }

    public void setAS3(boolean enabled) throws IOException {
        if (enabled && !is64 && !(hdr instanceof IggyFlashHeader32)) {
            throw new IOException("AS3 root location is unknown for the Iggy 32 legacy layout");
        }
        as3Change = enabled == hdr.hasAS3() ? null : enabled;
    }

    public void setSymbolClasses(List<IggySymbolClass> symbols) throws IOException {
        Set<Long> ids = new HashSet<>();
        Set<String> names = new HashSet<>();
        for (IggySymbolClass s : symbols) {
            if (s.getCharacterId() < 0 || s.getCharacterId() > 65535 || !ids.add(s.getCharacterId())
                    || s.getName() == null || s.getName().isEmpty() || s.getName().indexOf('\0') >= 0 || !names.add(s.getName())) {
                throw new IOException("Invalid or duplicate Iggy SymbolClass binding");
            }
        }
        boolean same = symbols.size() == symbolClasses.size();
        for (int i = 0; same && i < symbols.size(); i++) {
            same = symbols.get(i).getCharacterId() == symbolClasses.get(i).getCharacterId()
                    && symbols.get(i).getName().equals(symbolClasses.get(i).getName());
        }
        if (!same && !is64 && !(hdr instanceof IggyFlashHeader32)) {
            throw new IOException("SymbolClass table is unknown for the Iggy 32 legacy layout");
        }
        symbolChanges = same ? null : new ArrayList<>(symbols);
        symbolClassNames.clear();
    }

    private static List<IggyNamedFrame> namedChanges(List<IggyNamedFrame> before, List<IggyNamedFrame> after) throws IOException {
        boolean same = before.size() == after.size();
        for (int i = 0; i < after.size(); i++) {
            IggyNamedFrame f = after.get(i);
            if (f.getName() == null || f.getName().indexOf('\0') >= 0 || f.getFrame() < 0 || f.getFrame() > 0x3fffffffL) {
                throw new IOException("Invalid Iggy scene or frame label");
            }
            if (same) {
                same = f.getFrame() == before.get(i).getFrame() && f.getName().equals(before.get(i).getName());
            }
        }
        return same ? null : new ArrayList<>(after);
    }

    public void setNamedFrames(List<IggyNamedFrame> newScenes, List<IggyNamedFrame> newLabels) throws IOException {
        sceneChanges = namedChanges(scenes, newScenes);
        frameLabelChanges = namedChanges(frameLabels, newLabels);
    }

    private float[] originalScalingGrid(int id) throws IOException {
        if (id <= 0 || id >= dictionary.size() || dictionary.get(id).type != 3) {
            if (objectChanges.containsKey(id) && objectChanges.get(id).type == 3) {
                return null;
            }
            throw new IOException("Iggy scaling grids require a sprite: " + id);
        }
        long field = dictionary.get(id).address + (is64 ? 32 : 16);
        range(field, pointerSize);
        long offset = readReference(originalData, (int) field);
        if (offset == 0 || offset == 1) {
            return null;
        }
        long target = field + offset;
        if (!is64) {
            target &= 0xffffffffL;
        }
        range(target, 16);
        java.nio.ByteBuffer data = java.nio.ByteBuffer.wrap(originalData).order(java.nio.ByteOrder.LITTLE_ENDIAN);
        float[] result = new float[4];
        for (int i = 0; i < 4; i++) {
            result[i] = data.getFloat((int) target + 4 * i);
        }
        return result;
    }

    public float[] getScalingGrid(int id) throws IOException {
        float[] result = scalingGridChanges.containsKey(id) ? scalingGridChanges.get(id) : originalScalingGrid(id);
        return result == null ? null : result.clone();
    }

    public void setScalingGrids(Map<Integer, float[]> grids) throws IOException {
        Map<Integer, float[]> changes = new LinkedHashMap<>();
        for (int id : grids.keySet()) {
            originalScalingGrid(id);
        }
        for (int id : getSpriteIds()) {
            float[] grid = grids.get(id);
            if (grid != null) {
                if (grid.length != 4 || !Float.isFinite(grid[0]) || !Float.isFinite(grid[1])
                        || !Float.isFinite(grid[2]) || !Float.isFinite(grid[3]) || grid[0] > grid[2] || grid[1] > grid[3]) {
                    throw new IOException("Invalid Iggy scaling grid");
                }
            }
            float[] original = originalScalingGrid(id);
            // SWF stores integer twips, so compare in that representable domain.
            boolean same = (grid == null) == (original == null);
            for (int i = 0; same && grid != null && i < 4; i++) {
                same = Math.round(grid[i] * 20) == Math.round(original[i] * 20);
            }
            if (!same) {
                changes.put(id, grid == null ? null : grid.clone());
            }
        }
        scalingGridChanges.clear();
        scalingGridChanges.putAll(changes);
    }

    static boolean has32Root(byte[] data, List<IggyFixup.Field> fields) throws IOException {
        for (IggyFixup.Field field : fields) {
            if (field.offset == 4) {
                return field.operation == 2;
            }
        }
        // Files without relocation metadata cannot be edited, but may still be
        // read. The 32 root has a dictionary count at +0x28 and FPS at +0x40.
        if (data.length < 128) {
            return false;
        }
        MemoryDataStream stream = new MemoryDataStream(data);
        long base = IggyLayout.readPointer(stream, false);
        stream.seek(0x28, SeekMode.SET);
        long count = stream.readUI32();
        stream.seek(0x40, SeekMode.SET);
        float fps = stream.readFloat();
        return base >= 128 && count <= (data.length - base) / 4 && fps >= 1 && fps <= 1000;
    }

    private void range(long position, long size) throws IOException {
        if (position < 0 || size < 0 || position > originalData.length - size) {
            throw new IOException("Iggy reference outside main block: " + position);
        }
    }

    private long pointer(MemoryDataStream stream, long position) throws IOException {
        range(position, pointerSize);
        stream.seek(position, SeekMode.SET);
        long target = IggyLayout.readPointer(stream, is64);
        if (target != 0) {
            range(target, 1);
        }
        return target;
    }

    private String wideString(MemoryDataStream stream, long address) throws IOException {
        if (address == 0) {
            return null;
        }
        range(address, 2);
        stream.seek(address, SeekMode.SET);
        return stream.readWChar();
    }

    private String byteString(long address) throws IOException {
        if (address == 0) {
            return null;
        }
        range(address, 1);
        int end = (int) address;
        while (end < originalData.length && originalData[end] != 0) {
            end++;
        }
        if (end == originalData.length) {
            throw new IOException("Unterminated Iggy source name");
        }
        return new String(originalData, (int) address, end - (int) address, StandardCharsets.UTF_8);
    }

    private void namedFrames(MemoryDataStream stream, long address, long count, List<IggyNamedFrame> result) throws IOException {
        if (count == 0) {
            return;
        }
        if (address == 0) {
            throw new IOException("Missing Iggy named-frame table");
        }
        int stride = is64 ? 16 : 8;
        range(address, count * stride);
        for (int i = 0; i < count; i++) {
            long position = address + (long) stride * i;
            long nameAddress = pointer(stream, position);
            stream.seek(position + pointerSize, SeekMode.SET);
            long frame = stream.readUI32();
            long reserved = is64 ? stream.readUI32() : 0;
            result.add(new IggyNamedFrame(wideString(stream, nameAddress), frame, reserved));
        }
    }

    private List<IggySymbolClass> symbols(MemoryDataStream stream, long address, long count) throws IOException {
        List<IggySymbolClass> result = new ArrayList<>();
        if (count == 0) {
            return result;
        }
        if (address == 0) {
            throw new IOException("Missing Iggy symbol table");
        }
        int stride = is64 ? 24 : 12;
        range(address, count * stride);
        for (int i = 0; i < count; i++) {
            long position = address + (long) stride * i;
            long object = pointer(stream, position);
            long nameAddress = pointer(stream, position + pointerSize);
            stream.seek(position + 2 * pointerSize, SeekMode.SET);
            long id = stream.readUI32();
            long reserved = is64 ? stream.readUI32() : 0;
            // ID zero binds the document class, even when the dictionary is empty.
            if (id != 0 && id >= dictionary.size()) {
                throw new IOException("Invalid Iggy symbol dictionary ID");
            }
            result.add(new IggySymbolClass(object, wideString(stream, nameAddress), id, reserved));
        }
        return result;
    }

    @Override
    public void readFromDataStream(ReadDataStreamInterface input) throws IOException {
        if (originalData != null) {
            throw new IOException("Iggy main block is already loaded");
        }
        originalData = input.getAllBytes();
        range(0, is64 ? 184 : 64);
        MemoryDataStream stream = new MemoryDataStream(originalData);
        hdr = is64 ? new IggyFlashHeader64(stream)
                : has32Root(originalData, originalFields) ? new IggyFlashHeader32(stream) : new IggyFlashHeader32Legacy(stream);
        sourceName = byteString(hdr.getSourceNameAddress());
        name = wideString(stream, hdr.getFlashFilenameAddress());
        if (name == null || name.isEmpty()) {
            name = sourceName == null || sourceName.isEmpty() ? "movie.swf" : sourceName;
        }
        long base = hdr.getBaseAddress();
        long count = hdr.getDictionarySize();
        if (count > 0 && base == 0) {
            throw new IOException("Missing Iggy dictionary");
        }
        range(base, count * pointerSize);
        Set<Long> visited = new HashSet<>();
        for (int id = 0; id < count; id++) {
            long address = pointer(stream, base + (long) pointerSize * id);
            int type = address == 0 ? -1 : originalData[(int) address] & 255;
            dictionary.add(new DictionaryEntry(id, address, type));
            if (address != 0 && !dictionaryIds.containsKey(address)) {
                dictionaryIds.put(address, id);
            }
            if (type == 3) {
                range(address, is64 ? 72 : 36);
                spriteTimelines.put(id, new IggyTimeline(originalData, pointer(stream, address + (is64 ? 56 : 28)), is64));
            }
            if (address == 0 || !visited.add(address)) {
                continue;
            }
            stream.seek(address, SeekMode.SET);
            StructureInterface object = null;
            if (type == 22) {
                IggyFont font = is64 ? new IggyFont(stream) : new IggyFont32(stream, hdr instanceof IggyFlashHeader32);
                fonts.add(font);
                object = font;
            }
            if (type == 6) {
                IggyText text = is64 ? new IggyText(stream) : new IggyText32(stream);
                texts.add(text);
                if (text.getSymbolClassName() != null) {
                    originalTextSymbols.add(text.getTextIndex());
                }
                object = text;
            }
            if (object != null) {
                addresses.put(object, address);
                snapshots.put(object, serialize(object).data);
            }
        }
        namedFrames(stream, hdr.getFrameLabelsAddress(), hdr.getFrameLabelCount(), frameLabels);
        timeline = new IggyTimeline(originalData, hdr.getTimelineAddress(), is64);
        namedFrames(stream, hdr.getScenesAddress(), hdr.getSceneCount(), scenes);
        symbolClasses.addAll(symbols(stream, hdr.getSymbolClassesAddress(), hdr.getSymbolClassCount()));
        if (hdr.getImportCount() > 0) {
            long importsAddress = hdr.getImportsAddress();
            if (importsAddress == 0) {
                throw new IOException("Missing Iggy imports");
            }
            int stride = is64 ? 24 : 12;
            range(importsAddress, hdr.getImportCount() * stride);
            for (int i = 0; i < hdr.getImportCount(); i++) {
                long position = importsAddress + (long) stride * i;
                long libraryName = pointer(stream, position);
                long symbolAddress = pointer(stream, position + pointerSize);
                stream.seek(position + 2 * pointerSize, SeekMode.SET);
                long symbolCount = stream.readUI32();
                imports.add(new IggyImport(wideString(stream, libraryName), symbols(stream, symbolAddress, symbolCount)));
            }
        }
        if (hdr.getDictionaryFixupsAddress() != 0) {
            long table = hdr.getDictionaryFixupsAddress();
            range(table, pointerSize + 4);
            long entries = pointer(stream, table);
            stream.seek(table + pointerSize, SeekMode.SET);
            long fixupCount = stream.readUI32();
            if (fixupCount > 0 && entries == 0) {
                throw new IOException("Missing Iggy dictionary fixups");
            }
            range(entries, fixupCount * 8);
            for (int i = 0; i < fixupCount; i++) {
                stream.seek(entries + 8L * i, SeekMode.SET);
                long destination = stream.readUI32();
                long id = stream.readUI32();
                range(destination, pointerSize);
                dictionaryFixupEntries.add(entries + 8L * i);
                dictionaryReferenceIds.put(destination, (int) id);
                if (id >= dictionary.size()) {
                    throw new IOException("Invalid dictionary fixup ID");
                }
            }
        }
        visited.clear();
        long abcAddress = hdr.getAbcAddress();
        while (abcAddress != 0) {
            range(abcAddress, pointerSize + 4);
            if (!visited.add(abcAddress)) {
                throw new IOException("Cycle in Iggy ABC list");
            }
            stream.seek(abcAddress, SeekMode.SET);
            IggyDeclStrings abc = new IggyDeclStrings(stream, is64);
            abcBlocks.add(abc);
            originalAbcBlocks.add(abc);
            snapshots.put(abc, abc.getData());
            abcAddress = abc.getNextAddress();
        }
    }

    private static final class Serialized {

        byte[] data;
        List<IggyFixup.Field> fields;
    }

    private Serialized serialize(StructureInterface object) throws IOException {
        MemoryDataStream stream = new MemoryDataStream();
        IggyIndexBuilder builder = new IggyIndexBuilder();
        stream.setIndexing(builder);
        object.writeToDataStream(stream);
        Serialized result = new Serialized();
        result.data = stream.getAllBytes();
        result.fields = IggyFixup.parse(is64, builder.getIndexBytes(), result.data.length).getFields();
        // Keep context and opaque pointer fields from the original object body.
        if (!is64 && addresses.containsKey(object)) {
            long start = addresses.get(object);
            int size = object instanceof IggyFont32 ? ((IggyFont32) object).getHeaderSize() : 68;
            Map<Long, IggyFixup.Field> combined = new LinkedHashMap<>();
            IggyFont32 font = object instanceof IggyFont32 ? (IggyFont32) object : null;
            java.util.NavigableMap<Long, Long> parts = new java.util.TreeMap<>();
            if (font != null) {
                parts.putAll(font.relocatedParts);
            }
            for (IggyFixup.Field field : originalFields) {
                if (field.offset >= start && field.offset < start + size) {
                    combined.put(field.offset - start, new IggyFixup.Field(field.offset - start, field.operation));
                }
                if (font != null) {
                    Map.Entry<Long, Long> part = parts.floorEntry(field.offset);
                    if (part != null) {
                        long old = part.getKey();
                        if (field.offset >= old && field.offset < old + font.partSizes.get(old)) {
                            long offset = part.getValue() + field.offset - old;
                            combined.put(offset, new IggyFixup.Field(offset, field.operation));
                            // Descriptor opaque references keep their original target.
                            if (field.operation == 2 && font.partSizes.get(old) == 16 && field.offset - old != 12) {
                                long value = readReference(originalData, (int) field.offset);
                                if (value != 1) {
                                    font.externalReferences.put(offset, field.offset + value);
                                }
                            }
                        }
                    }
                }
            }
            for (IggyFixup.Field field : result.fields) {
                combined.put(field.offset, field);
            }
            result.fields = new ArrayList<>(combined.values());
        }
        return result;
    }

    private long readReference(byte[] bytes, int position) {
        long value = 0;
        for (int i = 0; i < pointerSize; i++) {
            value |= (long) (bytes[position + i] & 255) << (8 * i);
        }
        return is64 ? value : (int) value;
    }

    private static final class Move {

        long oldAddress;
        long newAddress;
        int length;

        Move(long oldAddress, long newAddress, int length) {
            this.oldAddress = oldAddress;
            this.newAddress = newAddress;
            this.length = length;
        }
    }

    private static long relocated(long address, java.util.NavigableMap<Long, Move> moves) {
        Map.Entry<Long, Move> entry = moves.floorEntry(address);
        if (entry != null) {
            Move move = entry.getValue();
            if (address >= move.oldAddress && address < move.oldAddress + move.length) {
                return move.newAddress + address - move.oldAddress;
            }
        }
        return address;
    }

    private void setPointer(MemoryDataStream stream, long position, long target) throws IOException {
        long saved = stream.position();
        stream.seek(position, SeekMode.SET);
        IggyLayout.writePointer(stream, is64, target);
        stream.seek(saved, SeekMode.SET);
    }

    private void writeScalar(MemoryDataStream data, Map<Long, IggyFixup.Field> fields,
            long position, long value, boolean word) throws IOException {
        long saved = data.position();
        data.seek(position, SeekMode.SET);
        if (word) {
            data.writeUI16((int) value);
        } else {
            data.writeUI32(value);
        }
        data.seek(saved, SeekMode.SET);
        fields.put(position, new IggyFixup.Field(position, word ? 4 : 5));
    }

    private Map<Long, Integer> writeTimelines(MemoryDataStream data, Map<Long, IggyFixup.Field> fields,
            Map<Long, Long> references, Set<Long> replaced, Map<Integer, Long> dictionaryAddresses) throws IOException {
        Map<Long, Integer> imports = new LinkedHashMap<>();
        Set<Integer> importedIds = importIds(getImports());
        for (Map.Entry<Integer, IggyTimeline.Replacement> change : timelineChanges.entrySet()) {
            int id = change.getKey();
            IggyTimeline.Replacement replacement = change.getValue();
            data.pad8bytes();
            long base = data.position();
            data.writeBytes(replacement.data);
            for (IggyFixup.Field field : replacement.fields) {
                fields.put(base + field.offset, new IggyFixup.Field(base + field.offset, field.operation));
            }
            for (Map.Entry<Long, Integer> reference : replacement.characters.entrySet()) {
                int characterId = reference.getValue();
                if (characterId <= 0 || !dictionaryAddresses.containsKey(characterId)) {
                    throw new IOException("Invalid Iggy timeline character ID " + characterId);
                }
                long field = base + reference.getKey();
                long target = dictionaryAddresses.get(characterId);
                setPointer(data, field, importedIds.contains(characterId) ? 0 : target);
                if (target != 0 && !importedIds.contains(characterId)) {
                    references.put(field, target);
                } else {
                    imports.put(field, characterId);
                }
            }
            long first = replacement.first < 0 ? 0 : base + replacement.first;
            long last = replacement.last < 0 ? 0 : base + replacement.last;
            long head;
            long tail;
            if (id == 0) {
                head = is64 ? 16 : 8;
                tail = is64 ? 24 : 12;
                writeScalar(data, fields, is64 ? 72 : hdr instanceof IggyFlashHeader32 ? 48 : 36, replacement.maxDepth, false);
                writeScalar(data, fields, is64 ? 68 : hdr instanceof IggyFlashHeader32 ? 44 : 32, replacement.frameCount, false);
                // Frame-label lookup tables must match the rebuilt display list.
                long labels = 0;
                if (!replacement.labels.isEmpty()) {
                    data.pad8bytes();
                    labels = data.position();
                    data.writeBytes(new byte[replacement.labels.size() * (is64 ? 16 : 8)]);
                    for (int i = 0; i < replacement.labels.size(); i++) {
                        long record = labels + (is64 ? 16L : 8L) * i;
                        IggyNamedFrame label = replacement.labels.get(i);
                        long name = data.position();
                        data.writeWChar(label.getName());
                        for (int j = 0; j <= label.getName().length(); j++) {
                            fields.put(name + 2L * j, new IggyFixup.Field(name + 2L * j, 4));
                        }
                        setPointer(data, record, name);
                        fields.put(record, new IggyFixup.Field(record, 2));
                        writeScalar(data, fields, record + pointerSize, label.getFrame(), false);
                    }
                }
                long labelField = is64 ? 32 : hdr instanceof IggyFlashHeader32 ? 16 : 44;
                setPointer(data, labelField, labels);
                replaced.add(labelField);
                fields.put(labelField, new IggyFixup.Field(labelField, 2));
                writeScalar(data, fields, is64 ? 76 : hdr instanceof IggyFlashHeader32 ? 52 : 48, replacement.labels.size(), false);
            } else {
                if (replacement.frameCount < 0 || replacement.frameCount > 65535 || replacement.maxDepth > 65535) {
                    throw new IOException("Iggy sprite frame count or depth exceeds UI16");
                }
                long object = dictionaryAddresses.get(id);
                head = object + (is64 ? 56 : 28);
                tail = object + (is64 ? 64 : 32);
                writeScalar(data, fields, object + (is64 ? 40 : 20), replacement.frameCount, true);
                writeScalar(data, fields, object + (is64 ? 42 : 22), replacement.maxDepth, true);
                long cache = object + (is64 ? 48 : 24);
                setPointer(data, cache, 0);
                replaced.add(cache);
                fields.put(cache, new IggyFixup.Field(cache, 2));
            }
            setPointer(data, head, first);
            setPointer(data, tail, last);
            replaced.add(head);
            replaced.add(tail);
            fields.put(head, new IggyFixup.Field(head, 2));
            fields.put(tail, new IggyFixup.Field(tail, 2));
        }
        return imports;
    }

    private Map<Integer, Long> writeObjects(MemoryDataStream data, Map<Long, IggyFixup.Field> fields,
            Map<Long, Long> references, Set<Long> replaced, List<Move> moves, Map<Long, Integer> imports) throws IOException {
        Map<Integer, Long> dictionaryAddresses = new LinkedHashMap<>();
        for (DictionaryEntry entry : dictionary) {
            dictionaryAddresses.put(entry.id, entry.address);
        }
        Map<Integer, Long> bases = new LinkedHashMap<>();
        Set<Integer> importedIds = importIds(getImports());
        boolean importDictionaryChanged = !removedDefinitions.isEmpty();
        for (int id : removedDefinitions) {
            dictionaryAddresses.put(id, 0L);
        }
        int dictionarySize = dictionary.size();
        int commonSize = is64 ? 32 : 16;
        if (importChanges != null) {
            Set<Integer> active = importIds(importChanges);
            for (int id : importIds(this.imports)) {
                if (!active.contains(id)) {
                    importDictionaryChanged |= dictionaryAddresses.get(id) != 0;
                    dictionaryAddresses.put(id, 0L);
                }
            }
            for (int id : active) {
                dictionarySize = Math.max(dictionarySize, id + 1);
                dictionaryAddresses.putIfAbsent(id, 0L);
            }
        }
        for (Map.Entry<Integer, IggyObjectData> change : objectChanges.entrySet()) {
            int id = change.getKey();
            IggyObjectData replacement = change.getValue();
            data.pad8bytes();
            long base = data.position();
            data.writeBytes(replacement.data);
            long target = base + replacement.address;
            bases.put(id, base);
            dictionaryAddresses.put(id, target);
            dictionarySize = Math.max(dictionarySize, id + 1);
            for (IggyFixup.Field field : replacement.fields) {
                fields.put(base + field.offset, new IggyFixup.Field(base + field.offset, field.operation));
            }
            if (id < dictionary.size() && dictionary.get(id).address != 0) {
                DictionaryEntry old = dictionary.get(id);
                int oldSize = old.type == 1 ? (is64 ? 80 : 48) : old.type == 2 ? (is64 ? 128 : 88)
                        : old.type == 4 ? (is64 ? 88 : 56) : old.type == 5 ? (is64 ? 80 : 60)
                                        : old.type == 7 ? (is64 ? 88 : 60) : old.type == 20 ? (is64 ? 48 : 24) : (is64 ? 64 : 40);
                moves.add(new Move(old.address, target, oldSize));
                // Preserve AS3/runtime bindings and their original relocation operations.
                long end = data.position();
                data.seek(target + 1, SeekMode.SET);
                data.writeBytes(Arrays.copyOfRange(originalData, (int) old.address + 1, (int) old.address + commonSize));
                data.seek(end, SeekMode.SET);
                for (IggyFixup.Field field : originalFields) {
                    if (field.offset < old.address || field.offset >= old.address + commonSize) {
                        continue;
                    }
                    long position = target + field.offset - old.address;
                    fields.put(position, new IggyFixup.Field(position, field.operation));
                    if (field.operation == 2) {
                        long offset = readReference(originalData, (int) field.offset);
                        setPointer(data, position, offset == 1 ? 0 : field.offset + offset);
                        if (offset != 1) {
                            references.put(position, field.offset + offset);
                        }
                    }
                }
            }
        }
        // All new object addresses are known before installing cross-object bitmap fills.
        for (Map.Entry<Integer, IggyObjectData> change : objectChanges.entrySet()) {
            long base = bases.get(change.getKey());
            for (Map.Entry<Long, Integer> reference : change.getValue().characters.entrySet()) {
                int id = reference.getValue();
                Long target = dictionaryAddresses.get(id);
                if (id <= 0 || id >= dictionarySize || (target == null || target == 0) && !importedIds.contains(id)) {
                    throw new IOException("Invalid Iggy asset reference ID " + id);
                }
                long field = base + reference.getKey();
                setPointer(data, field, target == null || importedIds.contains(id) ? 0 : target);
                if (target != null && target != 0 && !importedIds.contains(id)) {
                    references.put(field, target);
                } else {
                    imports.put(field, id);
                }
            }
        }
        if (importDictionaryChanged || dictionarySize != dictionary.size() || objectChanges.keySet().stream().anyMatch(id -> id < dictionary.size() && dictionary.get(id).address == 0)) {
            data.pad8bytes();
            long array = data.position();
            data.writeBytes(new byte[dictionarySize * pointerSize]);
            for (int i = 0; i < dictionarySize; i++) {
                long field = array + (long) i * pointerSize;
                Long target = dictionaryAddresses.get(i);
                setPointer(data, field, target == null ? 0 : target);
                fields.put(field, new IggyFixup.Field(field, 2));
                if (target != null && target != 0) {
                    references.put(field, target);
                }
                dictionaryAddresses.putIfAbsent(i, 0L);
            }
            if (hdr.getBaseAddress() != 0) {
                moves.add(new Move(hdr.getBaseAddress(), array, dictionary.size() * pointerSize));
            }
            setPointer(data, 0, array);
            replaced.add(0L);
            fields.put(0L, new IggyFixup.Field(0, 2));
            writeScalar(data, fields, is64 ? 64 : hdr instanceof IggyFlashHeader32 ? 40 : 4, dictionarySize, false);
        }
        return dictionaryAddresses;
    }

    private long writeImportString(MemoryDataStream data, Map<Long, IggyFixup.Field> fields, String text) throws IOException {
        // Include the UTF-16 terminator in scalar relocation metadata.
        long at = data.position();
        data.writeWChar(text);
        for (int i = 0; i <= text.length(); i++) {
            fields.put(at + 2L * i, new IggyFixup.Field(at + 2L * i, 4));
        }
        return at;
    }

    private void writeImports(MemoryDataStream data, Map<Long, IggyFixup.Field> fields,
            Map<Long, Long> references, Set<Long> replaced) throws IOException {
        if (importChanges == null) {
            return;
        }
        int stride = is64 ? 24 : 12;
        Map<Long, IggySymbolClass> originalSymbols = new LinkedHashMap<>();
        for (IggyImport library : imports) {
            for (IggySymbolClass symbol : library.getSymbols()) {
                originalSymbols.put(symbol.getCharacterId(), symbol);
            }
        }
        long table = 0;
        if (!importChanges.isEmpty()) {
            data.pad8bytes();
            table = data.position();
            data.writeBytes(new byte[importChanges.size() * stride]);
        }
        for (int i = 0; i < importChanges.size(); i++) {
            IggyImport library = importChanges.get(i);
            long record = table + (long) i * stride;
            long name = writeImportString(data, fields, library.getLibrary());
            setPointer(data, record, name);
            fields.put(record, new IggyFixup.Field(record, 2));
            long symbols = 0;
            if (!library.getSymbols().isEmpty()) {
                data.pad8bytes();
                symbols = data.position();
                data.writeBytes(new byte[library.getSymbols().size() * stride]);
            }
            setPointer(data, record + pointerSize, symbols);
            fields.put(record + pointerSize, new IggyFixup.Field(record + pointerSize, 2));
            writeScalar(data, fields, record + 2 * pointerSize, library.getSymbols().size(), false);
            // Preserve the uninterpreted x64 trailing word when this library already existed.
            if (is64) {
                for (int j = 0; j < imports.size(); j++) {
                    if (imports.get(j).getLibrary().equals(library.getLibrary())) {
                        int old = (int) hdr.getImportsAddress() + j * stride + 20;
                        long end = data.position();
                        data.seek(record + 20, SeekMode.SET);
                        data.writeBytes(Arrays.copyOfRange(originalData, old, old + 4));
                        data.seek(end, SeekMode.SET);
                        break;
                    }
                }
            }
            for (int j = 0; j < library.getSymbols().size(); j++) {
                IggySymbolClass symbol = library.getSymbols().get(j);
                IggySymbolClass original = originalSymbols.get(symbol.getCharacterId());
                long at = symbols + (long) j * stride;
                long object = original == null ? 0 : original.getObjectAddress();
                setPointer(data, at, object);
                fields.put(at, new IggyFixup.Field(at, 2));
                if (object != 0) {
                    references.put(at, object);
                }
                long symbolName = writeImportString(data, fields, symbol.getName());
                setPointer(data, at + pointerSize, symbolName);
                fields.put(at + pointerSize, new IggyFixup.Field(at + pointerSize, 2));
                writeScalar(data, fields, at + 2 * pointerSize, symbol.getCharacterId(), false);
                if (is64) {
                    writeScalar(data, fields, at + 20, original == null ? 0 : original.getReserved(), false);
                }
            }
        }
        long field = is64 ? 112 : 80;
        setPointer(data, field, table);
        replaced.add(field);
        fields.put(field, new IggyFixup.Field(field, 2));
        writeScalar(data, fields, is64 ? 128 : 88, importChanges.size(), false);
    }

    private void writeNamedTable(MemoryDataStream data, Map<Long, IggyFixup.Field> fields,
            Set<Long> replaced, List<IggyNamedFrame> values, boolean scene) throws IOException {
        if (values == null) {
            return;
        }
        int stride = is64 ? 16 : 8;
        long table = 0;
        if (!values.isEmpty()) {
            data.pad8bytes();
            table = data.position();
            data.writeBytes(new byte[values.size() * stride]);
        }
        List<IggyNamedFrame> original = scene ? scenes : frameLabels;
        for (int i = 0; i < values.size(); i++) {
            IggyNamedFrame value = values.get(i);
            long at = table + (long) stride * i;
            long name = writeImportString(data, fields, value.getName());
            setPointer(data, at, name);
            fields.put(at, new IggyFixup.Field(at, 2));
            writeScalar(data, fields, at + pointerSize, value.getFrame(), false);
            if (is64) {
                long reserved = i < original.size() ? original.get(i).getReserved() : 0;
                writeScalar(data, fields, at + 12, reserved, false);
            }
        }
        long field = is64 ? (scene ? 40 : 32) : hdr instanceof IggyFlashHeader32 ? (scene ? 20 : 16) : (scene ? 56 : 44);
        long count = is64 ? (scene ? 80 : 76) : hdr instanceof IggyFlashHeader32 ? (scene ? 56 : 52) : (scene ? 52 : 48);
        setPointer(data, field, table);
        replaced.add(field);
        fields.put(field, new IggyFixup.Field(field, 2));
        writeScalar(data, fields, count, values.size(), false);
    }

    private void writeMovieTags(MemoryDataStream data, Map<Long, IggyFixup.Field> fields,
            Map<Long, Long> references, Set<Long> replaced, Map<Integer, Long> dictionaryAddresses, List<Move> moves) throws IOException {
        if (as3Change != null) {
            long end = data.position();
            long field = is64 ? 168 : 112;
            data.seek(field, SeekMode.SET);
            data.writeUI8(as3Change ? 1 : 0);
            data.seek(end, SeekMode.SET);
            fields.put(field, new IggyFixup.Field(field, is64 ? 6 : 5));
        }
        for (Map.Entry<Integer, float[]> change : scalingGridChanges.entrySet()) {
            long field = dictionaryAddresses.get(change.getKey()) + (is64 ? 32 : 16);
            float[] grid = change.getValue();
            long target = 0;
            if (grid != null) {
                data.pad8bytes();
                target = data.position();
                for (float value : grid) {
                    long at = data.position();
                    data.writeFloat(value);
                    fields.put(at, new IggyFixup.Field(at, 5));
                }
            }
            setPointer(data, field, target);
            replaced.add(field);
            fields.put(field, new IggyFixup.Field(field, 2));
        }
        writeNamedTable(data, fields, replaced, sceneChanges, true);
        writeNamedTable(data, fields, replaced, frameLabelChanges, false);
        if (symbolChanges != null) {
            java.util.NavigableMap<Long, Move> moveIndex = new java.util.TreeMap<>();
            for (Move move : moves) {
                moveIndex.put(move.oldAddress, move);
            }
            int stride = is64 ? 24 : 12;
            long table = 0;
            if (!symbolChanges.isEmpty()) {
                data.pad8bytes();
                table = data.position();
                data.writeBytes(new byte[symbolChanges.size() * stride]);
            }
            Map<Long, IggySymbolClass> old = new LinkedHashMap<>();
            for (IggySymbolClass s : symbolClasses) {
                old.put(s.getCharacterId(), s);
            }
            Set<Integer> imported = importIds(getImports());
            Map<Integer, Long> classNames = new LinkedHashMap<>();
            for (IggySymbolClass s : symbolClasses) {
                classNames.put((int) s.getCharacterId(), 0L);
            }
            for (int i = 0; i < symbolChanges.size(); i++) {
                IggySymbolClass s = symbolChanges.get(i);
                int id = (int) s.getCharacterId();
                Long object = dictionaryAddresses.get(id);
                if (id != 0 && object == null) {
                    throw new IOException("Invalid Iggy SymbolClass character ID " + id);
                }
                long at = table + (long) stride * i;
                IggySymbolClass before = old.get((long) id);
                // The DLL can resolve bindings by ID. Retain original null object references.
                long target = id == 0 || imported.contains(id) || (before != null && before.getObjectAddress() == 0) ? 0 : object;
                setPointer(data, at, target);
                fields.put(at, new IggyFixup.Field(at, 2));
                if (target != 0) {
                    references.put(at, target);
                }
                long name = writeImportString(data, fields, s.getName());
                classNames.put(id, name);
                setPointer(data, at + pointerSize, name);
                fields.put(at + pointerSize, new IggyFixup.Field(at + pointerSize, 2));
                writeScalar(data, fields, at + 2 * pointerSize, id, false);
                if (is64) {
                    writeScalar(data, fields, at + 20, before == null ? 0 : before.getReserved(), false);
                }
            }
            long field = is64 ? 160 : 108;
            setPointer(data, field, table);
            replaced.add(field);
            fields.put(field, new IggyFixup.Field(field, 2));
            writeScalar(data, fields, is64 ? 176 : 120, symbolChanges.size(), false);
            // The shared object header also contains the class name. Keep it in
            // sync, including moved fonts/texts, and discard runtime class caches.
            for (Map.Entry<Integer, Long> binding : classNames.entrySet()) {
                int id = binding.getKey();
                Long object = dictionaryAddresses.get(id);
                if (id == 0 || imported.contains(id) || object == null || object == 0) {
                    continue;
                }
                long nameField = relocated(object, moveIndex) + (is64 ? 16 : 8);
                setPointer(data, nameField, binding.getValue());
                replaced.add(nameField);
                references.remove(nameField);
                fields.put(nameField, new IggyFixup.Field(nameField, 2));
                long runtimeField = nameField + pointerSize;
                long end = data.position();
                data.seek(runtimeField, SeekMode.SET);
                if (is64) {
                    data.writeUI64(0);
                } else {
                    data.writeUI32(0);
                }
                data.seek(end, SeekMode.SET);
                replaced.add(runtimeField);
                references.remove(runtimeField);
                fields.remove(runtimeField);
            }
        }
    }

    @Override
    public void writeToDataStream(WriteDataStreamInterface output) throws IOException {
        List<StructureInterface> objects = new ArrayList<>();
        objects.addAll(fonts);
        objects.addAll(texts);
        if (objects.size() != addresses.size() || !addFonts.isEmpty() || !addTexts.isEmpty()) {
            throw new IOException("Direct changes to font/text lists are unsupported; use the SWF converter");
        }
        MemoryDataStream header = new MemoryDataStream();
        header.setIndexing(new IggyIndexBuilder());
        hdr.writeToDataStream(header);
        byte[] headerBytes = header.getAllBytes();
        MemoryDataStream data = new MemoryDataStream();
        data.writeBytes(originalData);
        data.seek(0, SeekMode.SET);
        data.writeBytes(headerBytes);
        data.seek(originalData.length, SeekMode.SET);
        Map<Long, IggyFixup.Field> fields = new LinkedHashMap<>();
        for (IggyFixup.Field field : originalFields) {
            fields.put(field.offset, field);
        }
        List<Move> moves = new ArrayList<>();
        Map<Long, Long> inheritedReferences = new LinkedHashMap<>();
        Set<Long> replacedPointers = new HashSet<>();
        boolean changed = !Arrays.equals(headerBytes, Arrays.copyOf(originalData, hdr.getHeaderSize()));
        for (int i = 0; symbolChanges == null && i < symbolClasses.size(); i++) {
            IggySymbolClass symbol = symbolClasses.get(i);
            String newName = symbolClassNames.get(symbol.getCharacterId());
            if (newName == null || newName.equals(symbol.getName())) {
                continue;
            }
            changed = true;
            data.pad8bytes();
            long address = data.position();
            data.writeWChar(newName);
            for (int j = 0; j <= newName.length(); j++) {
                fields.put(address + 2L * j, new IggyFixup.Field(address + 2L * j, 4));
            }
            long namePointer = hdr.getSymbolClassesAddress() + (is64 ? 24L : 12L) * i + pointerSize;
            setPointer(data, namePointer, address);
            replacedPointers.add(namePointer);
        }
        for (StructureInterface object : objects) {
            int id = object instanceof IggyFont ? ((IggyFont) object).getFontId() : ((IggyText) object).getTextIndex();
            if (removedDefinitions.contains(id)) {
                continue;
            }
            if (!addresses.containsKey(object)) {
                throw new IOException("Replacing Iggy object identity is not supported");
            }
            Serialized replacement = serialize(object);
            if (Arrays.equals(replacement.data, snapshots.get(object))) {
                continue;
            }
            changed = true;
            data.pad8bytes();
            long newAddress = data.position();
            long oldAddress = addresses.get(object);
            int bodySize = object instanceof IggyFont ? (is64 ? 352 : ((IggyFont32) object).getHeaderSize()) : (is64 ? 104 : 68);
            moves.add(new Move(oldAddress, newAddress, bodySize));
            data.writeBytes(replacement.data);
            if (object instanceof IggyFont32) {
                IggyFont32 font = (IggyFont32) object;
                for (Map.Entry<Long, Long> part : font.relocatedParts.entrySet()) {
                    moves.add(new Move(part.getKey(), newAddress + part.getValue(), font.partSizes.get(part.getKey())));
                }
                for (Map.Entry<Long, Long> reference : font.externalReferences.entrySet()) {
                    inheritedReferences.put(newAddress + reference.getKey(), reference.getValue());
                }
            }
            for (IggyFixup.Field field : replacement.fields) {
                long position = newAddress + field.offset;
                fields.put(position, new IggyFixup.Field(position, field.operation));
                boolean generatedPointer = is64 ? (object instanceof IggyFont
                        ? field.offset == 48 || field.offset == 56 || field.offset == 64 || field.offset == 96 || field.offset == 128
                        : field.offset == 96) : (object instanceof IggyFont
                                ? field.offset == 28 || field.offset == 32 || field.offset == 36 || field.offset == 60 || field.offset == 76
                                : field.offset == 64);
                if (object instanceof IggyText) {
                    generatedPointer = ((IggyText) object).isGeneratedPointer(field.offset);
                }
                if (field.operation == 2 && !generatedPointer && field.offset < bodySize && oldAddress + field.offset + pointerSize <= originalData.length) {
                    int oldPosition = (int) (oldAddress + field.offset);
                    long oldValue = readReference(originalData, oldPosition);
                    if (oldValue != 1 && readReference(replacement.data, (int) field.offset) == oldValue) {
                        inheritedReferences.put(position, oldPosition + oldValue);
                    }
                }
            }
        }
        Map<Long, Integer> timelineImports = new LinkedHashMap<>();
        Map<Integer, Long> dictionaryAddresses = writeObjects(data, fields, inheritedReferences, replacedPointers, moves, timelineImports);
        writeImports(data, fields, inheritedReferences, replacedPointers);
        timelineImports.putAll(writeTimelines(data, fields, inheritedReferences, replacedPointers, dictionaryAddresses));
        writeMovieTags(data, fields, inheritedReferences, replacedPointers, dictionaryAddresses, moves);
        changed |= !timelineChanges.isEmpty() || !objectChanges.isEmpty() || !removedDefinitions.isEmpty() || importChanges != null
                || !scalingGridChanges.isEmpty() || sceneChanges != null || frameLabelChanges != null || symbolChanges != null || as3Change != null;
        boolean abcChanged = abcBlocks.size() != originalAbcBlocks.size();
        for (int i = 0; !abcChanged && i < abcBlocks.size(); i++) {
            abcChanged = abcBlocks.get(i) != originalAbcBlocks.get(i)
                    || !Arrays.equals(abcBlocks.get(i).getData(), snapshots.get(abcBlocks.get(i)));
        }
        if (abcChanged) {
            if (hdr.getAbcPointerOffset() < 0) {
                throw new IOException("ABC root location is unknown for this 32-bit Iggy layout");
            }
            changed = true;
            long first = 0;
            long previous = -1;
            for (IggyDeclStrings abc : abcBlocks) {
                abc.validate();
                data.pad8bytes();
                long position = data.position();
                if (first == 0) {
                    first = position;
                }
                if (previous >= 0) {
                    setPointer(data, previous, position);
                }
                if (abc.getAddress() != 0) {
                    moves.add(new Move(abc.getAddress(), position, pointerSize + 4));
                }
                byte[] bytes = abc.getData();
                IggyLayout.writePointer(data, is64, 0);
                data.writeUI32(bytes.length);
                data.writeBytes(bytes);
                fields.put(position, new IggyFixup.Field(position, 2));
                fields.put(position + pointerSize, new IggyFixup.Field(position + pointerSize, 5));
                previous = position;
            }
            long abcPointer = hdr.getAbcPointerOffset();
            setPointer(data, abcPointer, first);
            replacedPointers.add(abcPointer);
            fields.put(abcPointer, new IggyFixup.Field(abcPointer, 2));
        }
        if (changed && originalFields.isEmpty()) {
            throw new IOException("Cannot edit Iggy data without its relocation program");
        }
        if (changed) {
            java.util.NavigableMap<Long, Move> moveIndex = new java.util.TreeMap<>();
            for (Move move : moves) {
                moveIndex.put(move.oldAddress, move);
            }
            if (!timelineImports.isEmpty() || importChanges != null && !dictionaryReferenceIds.isEmpty()) {
                if (!is64 && !(hdr instanceof IggyFlashHeader32)) {
                    throw new IOException("This Iggy layout cannot add unresolved import references");
                }
                Map<Long, Integer> entries = new LinkedHashMap<>();
                for (Map.Entry<Long, Integer> reference : dictionaryReferenceIds.entrySet()) {
                    if (importIds(getImports()).contains(reference.getValue())) {
                        entries.put(relocated(reference.getKey(), moveIndex), reference.getValue());
                    }
                }
                entries.putAll(timelineImports);
                data.pad8bytes();
                long table = data.position();
                IggyLayout.writePointer(data, is64, entries.isEmpty() ? 0 : table + pointerSize + 4);
                data.writeUI32(entries.size());
                fields.put(table, new IggyFixup.Field(table, 2));
                fields.put(table + pointerSize, new IggyFixup.Field(table + pointerSize, 5));
                for (Map.Entry<Long, Integer> entry : entries.entrySet()) {
                    long at = data.position();
                    data.writeUI32(entry.getKey());
                    data.writeUI32(entry.getValue());
                    fields.put(at, new IggyFixup.Field(at, 5));
                    fields.put(at + 4, new IggyFixup.Field(at + 4, 5));
                }
                long field = is64 ? 136 : 96;
                setPointer(data, field, table);
                replacedPointers.add(field);
                fields.put(field, new IggyFixup.Field(field, 2));
            }
            for (long entry : dictionaryFixupEntries) {
                MemoryDataStream source = new MemoryDataStream(originalData);
                source.seek(entry, SeekMode.SET);
                long destination = source.readUI32();
                long newDestination = relocated(destination, moveIndex);
                if (newDestination != destination) {
                    long saved = data.position();
                    data.seek(entry, SeekMode.SET);
                    data.writeUI32(newDestination);
                    data.seek(saved, SeekMode.SET);
                }
            }
            for (IggyFixup.Field field : originalFields) {
                if (field.operation != 2 || replacedPointers.contains(field.offset)) {
                    continue;
                }
                long value = readReference(originalData, (int) field.offset);
                if (value == 1) {
                    continue;
                }
                long oldTarget = field.offset + value;
                long target = relocated(oldTarget, moveIndex);
                if (target != oldTarget) {
                    setPointer(data, field.offset, target);
                }
            }
            for (Map.Entry<Long, Long> reference : inheritedReferences.entrySet()) {
                setPointer(data, reference.getKey(), relocated(reference.getValue(), moveIndex));
            }
        }
        byte[] result = data.getAllBytes();
        output.writeBytes(result);
        if (output.getIndexing() != null) {
            IggyFixup.writeFields(output.getIndexing(), new ArrayList<>(fields.values()), result.length);
        }
    }
}
