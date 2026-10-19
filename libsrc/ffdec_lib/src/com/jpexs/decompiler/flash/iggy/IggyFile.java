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
import com.jpexs.decompiler.flash.iggy.streams.RandomAccessFileDataStream;
import com.jpexs.decompiler.flash.iggy.streams.ReadDataStreamInterface;
import com.jpexs.decompiler.flash.iggy.streams.SeekMode;
import com.jpexs.decompiler.flash.iggy.streams.StructureInterface;
import com.jpexs.decompiler.flash.iggy.streams.WriteDataStreamInterface;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Iggy file.
 *
 * @author JPEXS
 * <p>
 * Based of works of somebody called eternity.
 */
public class IggyFile implements StructureInterface {

    static final Logger LOGGER = Logger.getLogger(IggyFile.class.getName());

    /** Native root and pointer layouts supported by the converter. */
    public enum Variant {
        X86_LEGACY("32 legacy", "32_legacy"),
        X86("32", "32"),
        X64("64", "64");

        private final String displayName;
        private final String identifier;

        Variant(String displayName, String identifier) {
            this.displayName = displayName;
            this.identifier = identifier;
        }

        @Override
        public String toString() {
            return displayName;
        }

        public static Variant fromString(String value) throws IOException {
            for (Variant variant : values()) {
                if (value != null && variant.identifier.equalsIgnoreCase(value.trim())) {
                    return variant;
                }
            }
            throw new IOException("Unknown Iggy variant: " + value);
        }
    }

    private File originalFile;

    private IggyHeader header;

    private List<IggySubFileEntry> subFileEntries = new ArrayList<>();

    private List<byte[]> subFileEntriesData = new ArrayList<>();

    private IggySwf iggySwf;
    private IggySubFileEntry mainEntry;
    private boolean legacy32;

    private static final class LayoutMismatchException extends IOException {

        LayoutMismatchException() {
            super("Iggy main layout does not match the container variant");
        }
    }

    private final Map<IggySubFileEntry, IggySubFileEntry> fixupTargets = new HashMap<>();
    private final List<IggyTextureMetadata> textureMetadata = new ArrayList<>();

    public List<IggyTextureMetadata> getTextureMetadata() {
        return java.util.Collections.unmodifiableList(textureMetadata);
    }

    public static final int FIRST_TAG_POSITION = 3;

    public IggySwf getSwf() {
        return iggySwf;
    }

    /**
     * Get a version suitable for SWF export. Older files may have no version.
     *
     * @return Original SWF version, or the historical fallback of 10
     */
    public int getSwfVersion() {
        long version = header.getSwfVersion();
        return version >= 3 && version <= 255 ? (int) version : 10;
    }

    public IggyFile(String filePath) throws IOException {
        this(new File(filePath));
    }

    public IggyFile(File file) throws IOException {
        this.originalFile = file;
        try (ReadDataStreamInterface stream = new RandomAccessFileDataStream(file, true)) {
            readFromDataStream(stream);
        }
    }

    /** Read a container from memory or another seekable stream. */
    public IggyFile(ReadDataStreamInterface stream) throws IOException {
        readFromDataStream(stream);
    }

    /**
     * Create an empty native container, including the root relocation program.
     *
     * @param variant Native root layout
     * @param swfName Movie/library name stored in the container
     * @return An editable empty movie without a backing file
     * @throws IOException If the name or layout is invalid
     */
    public static IggyFile create(Variant variant, String swfName) throws IOException {
        if (variant == null || swfName == null || swfName.isEmpty() || swfName.indexOf('\0') >= 0) {
            throw new IOException("Missing Iggy variant or valid movie name");
        }
        boolean wide = variant == Variant.X64;
        boolean standard32 = variant == Variant.X86;
        int size = wide ? 184
                : standard32 ? IggyFlashHeader32.STRUCT_SIZE : IggyFlashHeader32Legacy.STRUCT_SIZE;
        MemoryDataStream root = new MemoryDataStream();
        root.writeBytes(new byte[size]);
        for (int offset : wide ? new int[]{0, 8, 16, 24, 32, 40, 112, 120, 136, 144, 152, 160}
                : standard32 ? new int[]{0, 4, 8, 12, 16, 20, 80, 84, 96, 100, 104, 108}
                        : new int[]{0, 8, 12, 44, 56, 60}) {
            root.seek(offset, SeekMode.SET);
            IggyLayout.writePointer(root, wide, 0);
        }
        root.seek(size, SeekMode.SET);
        long source = root.position();
        root.writeBytes(swfName.getBytes(StandardCharsets.UTF_8));
        root.writeUI8(0);
        root.pad8bytes();
        long name = root.position();
        if (variant != Variant.X86_LEGACY) {
            root.writeWChar(swfName);
            root.pad8bytes();
            root.seek(wide ? 144 : 100, SeekMode.SET);
            IggyLayout.writePointer(root, wide, name);
        }
        root.seek(wide ? 8 : standard32 ? 4 : 60, SeekMode.SET);
        IggyLayout.writePointer(root, wide, source);
        root.seek(0, SeekMode.SET);
        IggyFlashHeaderInterface movieHeader = wide ? new IggyFlashHeader64(root)
                : standard32 ? new IggyFlashHeader32(root) : new IggyFlashHeader32Legacy(root);
        movieHeader.setMovieProperties(0, 0, 0, 0, 24, 0, 0xffffffffL);
        // Use the header writer's existing field declarations for all three layouts.
        MemoryDataStream main = new MemoryDataStream();
        main.writeBytes(root.getAllBytes());
        main.seek(0, SeekMode.SET);
        IggyIndexBuilder headerIndex = new IggyIndexBuilder();
        main.setIndexing(headerIndex);
        movieHeader.writeToDataStream(main);
        byte[] data = main.getAllBytes();
        List<IggyFixup.Field> fields = new ArrayList<>(IggyFixup.parse(wide, headerIndex.getIndexBytes(), data.length).getFields());
        if (variant != Variant.X86_LEGACY) {
            for (int i = 0; i <= swfName.length(); i++) {
                fields.add(new IggyFixup.Field(name + 2L * i, 4));
            }
        }
        IggyIndexBuilder index = new IggyIndexBuilder();
        IggyFixup.writeFields(index, fields, data.length);
        List<byte[]> programs = IggyFixup.compact(wide, index.getIndexBytes(), data.length);
        if (programs.size() >= 256) {
            throw new IOException("Too many Iggy root relocation blocks");
        }
        MemoryDataStream container = new MemoryDataStream();
        int entries = programs.size() + 1;
        long offset = IggyHeader.STRUCT_SIZE + (long) IggySubFileEntry.STRUCTURE_SIZE * entries;
        new IggyHeader(0x900, 1, wide ? 64 : 32, 1, 3, 0, new byte[12], entries).writeToDataStream(container);
        new IggySubFileEntry(1, data.length, 0, offset).writeToDataStream(container);
        offset += data.length;
        for (byte[] program : programs) {
            new IggySubFileEntry(0, program.length, 0, offset).writeToDataStream(container);
            offset += program.length;
        }
        container.writeBytes(data);
        for (byte[] program : programs) {
            container.writeBytes(program);
        }
        IggyFile result = new IggyFile(new MemoryDataStream(container.getAllBytes()));
        int dot = swfName.lastIndexOf('.');
        result.iggySwf.setTextureResourceName((dot < 0 ? swfName : swfName.substring(0, dot)) + ".raw.iggytex");
        return result;
    }

    public File getOriginalFile() {
        return originalFile;
    }

    public IggyHeader getHeader() {
        return header;
    }

    public IggySubFileEntry getSubFileEntry(int entryIndex) {
        if (entryIndex < 0 || entryIndex >= subFileEntries.size()) {
            throw new ArrayIndexOutOfBoundsException("No entry with index " + entryIndex + " exists");
        }
        return subFileEntries.get(entryIndex);
    }

    public int getNumEntries() {
        return subFileEntries.size();
    }

    public byte[] getEntryData(int entryIndex) {
        if (entryIndex < 0 || entryIndex >= subFileEntries.size()) {
            throw new ArrayIndexOutOfBoundsException("No entry with index " + entryIndex + " exists");
        }
        return subFileEntriesData.get(entryIndex);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[IggyFile:").append("\r\n");
        sb.append(header).append("\r\n");
        sb.append("Entries:").append("\r\n");
        for (IggySubFileEntry entry : subFileEntries) {
            sb.append(entry).append("\r\n");
        }
        sb.append("]");
        return sb.toString();
    }

    public static void extractIggyFile(File iggyFile, File extractDir) throws IOException {
        final String FILENAME_FORMAT = "index%d_type%d.bin";
        IggyFile ir = new IggyFile(iggyFile);
        for (int i = 0; i < ir.getNumEntries(); i++) {
            IggySubFileEntry entry = ir.getSubFileEntry(i);
            try (FileOutputStream fos = new FileOutputStream(new File(extractDir, String.format(FILENAME_FORMAT, i, entry.type)))) {
                fos.write(ir.getEntryData(i));
            }
        }
    }

    public String getSwfName() {
        return iggySwf.getName();
    }

    public long getSwfXMin() {
        return iggySwf.getHdr().getXMin();
    }

    public long getSwfYMin() {
        return iggySwf.getHdr().getYMin();
    }

    public long getSwfXMax() {
        return iggySwf.getHdr().getXMax();
    }

    public long getSwfYMax() {
        return iggySwf.getHdr().getYMax();
    }

    public float getSwfFrameRate() {
        return iggySwf.getHdr().getFrameRate();
    }

    /**
     * Removes entries of type INDEX.There can be more than one INDEX,
     * continuous. This removes all ot them.
     */
    public void removeIndexEntries() {
        long offsetsChange = 0;
        final int ENTRY_SIZE = 16;
        for (int i = 0; i < subFileEntries.size(); i++) {
            IggySubFileEntry entry = subFileEntries.get(i);
            entry.offset += offsetsChange;
            if (entry.type == IggySubFileEntry.TYPE_INDEX) {
                offsetsChange = offsetsChange - entry.size - ENTRY_SIZE;
                subFileEntriesData.remove(i);
                subFileEntries.remove(i);
                i--;
            }
        }
    }

    public boolean updateFlashEntry() throws IOException {
        MemoryDataStream stream = new MemoryDataStream();
        IggyIndexBuilder builder = new IggyIndexBuilder();
        stream.setIndexing(builder);
        iggySwf.writeToDataStream(stream);
        byte[] replacement = stream.getAllBytes();
        updateTextureEntries();
        int mainIndex = subFileEntries.indexOf(mainEntry);
        if (Arrays.equals(replacement, subFileEntriesData.get(mainIndex))) {
            return true;
        }
        byte[] indexBytes = builder.getIndexBytes();
        subFileEntriesData.set(mainIndex, replacement);
        mainEntry.size = replacement.length;
        replaceFixupPrograms(mainEntry, IggyFixup.compact(header.is64(), indexBytes, replacement.length));
        return true;
    }

    private void replaceFixupPrograms(IggySubFileEntry target, List<byte[]> programs) throws IOException {
        int previousCount = 0;
        IggySubFileEntry prototype = null;
        for (IggySubFileEntry entry : subFileEntries) {
            if (fixupTargets.get(entry) == target) {
                previousCount++;
                if (prototype == null) {
                    prototype = entry;
                }
            }
        }
        if (subFileEntries.size() - previousCount + programs.size() > 256) {
            throw new IOException("Too many Iggy blocks after partitioning relocation programs");
        }
        //The +8 word is preserved: its interpretation is not established.
        int insertion = prototype == null ? subFileEntries.size() : subFileEntries.indexOf(prototype);
        if (prototype == null) {
            prototype = new IggySubFileEntry(0, 0, 0, 0);
            for (IggySubFileEntry entry : subFileEntries) {
                if (entry == target) {
                    break;
                }
                if (selects(entry)) {
                    prototype.targetIndex++;
                }
            }
        }
        for (int i = subFileEntries.size() - 1; i >= 0; i--) {
            IggySubFileEntry entry = subFileEntries.get(i);
            if (fixupTargets.get(entry) == target) {
                subFileEntries.remove(i);
                subFileEntriesData.remove(i);
                fixupTargets.remove(entry);
            }
        }
        for (byte[] program : programs) {
            long descriptor = prototype.copyFlag << 8 | prototype.targetIndex << 16 | (long) prototype.metadata << 24;
            IggySubFileEntry entry = new IggySubFileEntry(descriptor, program.length, prototype.size2, 0);
            subFileEntries.add(insertion, entry);
            subFileEntriesData.add(insertion++, program);
            fixupTargets.put(entry, target);
        }
    }

    private void updateTextureEntries() throws IOException {
        if (iggySwf.getTextureChanges().isEmpty()) {
            return;
        }
        boolean found = false;
        for (int i = 0; i < subFileEntries.size(); i++) {
            IggySubFileEntry entry = subFileEntries.get(i);
            if (entry.type != 8 || legacy32) {
                continue;
            }
            found = true;
            byte[] old = subFileEntriesData.get(i);
            byte[] replacement = IggyTextureMetadata.update(old, header.is64(), iggySwf.getTextureChanges());
            if (Arrays.equals(old, replacement)) {
                continue;
            }
            // These platform records contain offsets into the companion file, not relocated pointers.
            subFileEntriesData.set(i, replacement);
            entry.size = replacement.length;
            updateTextureFixups(entry, old, replacement);
        }
        if (!found && iggySwf.getTextureChanges().values().stream().anyMatch(record -> record != null)) {
            if (legacy32) {
                throw new IOException("This Iggy 32 legacy layout does not support platform texture records");
            }
            byte[] replacement = IggyTextureMetadata.update(new byte[0], header.is64(), iggySwf.getTextureChanges());
            subFileEntries.add(new IggySubFileEntry(8, replacement.length, 0, 0));
            subFileEntriesData.add(replacement);
        }
    }

    private void updateTextureFixups(IggySubFileEntry target, byte[] old, byte[] replacement) throws IOException {
        int headerSize = header.is64() ? 16 : 12;
        int oldCount = old.length == 0 ? 0 : (old[0] & 255) | (old[1] & 255) << 8;
        int count = (replacement[0] & 255) | (replacement[1] & 255) << 8;
        List<IggyFixup.Field> original = new ArrayList<>();
        boolean found = false;
        for (int i = 0; i < subFileEntries.size(); i++) {
            IggySubFileEntry entry = subFileEntries.get(i);
            if (fixupTargets.get(entry) != target) {
                continue;
            }
            found = true;
            original.addAll(IggyFixup.parse(header.is64(), subFileEntriesData.get(i), old.length).getFields());
        }
        if (found) {
            List<IggyFixup.Field> fields = new ArrayList<>();
            Map<Long, Integer> pattern = new java.util.LinkedHashMap<>();
            for (IggyFixup.Field field : original) {
                if (field.offset < headerSize) {
                    fields.add(field);
                } else if (field.offset < headerSize + 12L * oldCount) {
                    long offset = (field.offset - headerSize) % 12;
                    Integer operation = pattern.put(offset, field.operation);
                    if (operation != null && operation != field.operation) {
                        throw new IOException("Inconsistent Iggy texture fixup layout");
                    }
                } else {
                    fields.add(new IggyFixup.Field(field.offset + 12L * (count - oldCount), field.operation));
                }
            }
            for (int record = 0; record < count; record++) {
                for (Map.Entry<Long, Integer> field : pattern.entrySet()) {
                    fields.add(new IggyFixup.Field(headerSize + 12L * record + field.getKey(), field.getValue()));
                }
            }
            IggyIndexBuilder builder = new IggyIndexBuilder();
            IggyFixup.writeFields(builder, fields, replacement.length);
            byte[] program = builder.getIndexBytes();
            replaceFixupPrograms(target, IggyFixup.compact(header.is64(), program, replacement.length));
        }
    }

    private boolean selects(IggySubFileEntry entry) {
        return entry.type == 1 || entry.type == 8 || legacy32 && entry.type != 0;
    }

    private void parseEntries() throws IOException {
        List<IggySubFileEntry> selected = new ArrayList<>();
        Map<IggySubFileEntry, List<IggyFixup.Field>> fields = new HashMap<>();
        boolean littleEndian = header.getPlatform1() == 1;
        boolean nativeBitfields = header.getPlatform3() == 1;
        Map<IggySubFileEntry, java.util.Set<Long>> normalized = new HashMap<>();
        for (int i = 0; i < subFileEntries.size(); i++) {
            IggySubFileEntry entry = subFileEntries.get(i);
            if (selects(entry)) {
                selected.add(entry);
                fields.put(entry, new ArrayList<>());
                normalized.put(entry, new java.util.HashSet<>());
                if (entry.type == 1 && mainEntry == null) {
                    mainEntry = entry;
                }
            } else if (entry.type == 0) {
                if (entry.targetIndex >= selected.size()) {
                    throw new IOException("Invalid Iggy fixup target");
                }
                IggySubFileEntry target = selected.get(entry.targetIndex);
                byte[] payload = subFileEntriesData.get(subFileEntries.indexOf(target));
                IggyFixup program = IggyFixup.parse(header.is64(), subFileEntriesData.get(i), payload.length);
                if (!littleEndian || !nativeBitfields) {
                    for (IggyFixup.Field field : program.getFields()) {
                        if (!normalized.get(target).add(field.offset)) {
                            throw new IOException("Overlapping Iggy platform fixups");
                        }
                    }
                    program.normalize(payload, littleEndian, nativeBitfields);
                }
                fields.get(target).addAll(program.getFields());
                fixupTargets.put(entry, target);
            }
        }
        if (mainEntry == null) {
            throw new IOException("Missing Iggy main block");
        }
        byte[] mainData = subFileEntriesData.get(subFileEntries.indexOf(mainEntry));
        if (!header.is64() && IggySwf.has32Root(mainData, fields.get(mainEntry)) == legacy32) {
            throw new LayoutMismatchException();
        }
        if (!legacy32) {
            header.requirePlatformFixups();
        }
        if (!littleEndian || !nativeBitfields) {
            for (IggySubFileEntry entry : selected) {
                if (header.is64() && fields.get(entry).isEmpty()) {
                    throw new IOException("Missing Iggy platform fixups");
                }
            }
            header.setNativePayload();
        }
        for (IggySubFileEntry entry : selected) {
            byte[] bytes = subFileEntriesData.get(subFileEntries.indexOf(entry));
            if (entry.type == 8 && !legacy32) {
                textureMetadata.add(new IggyTextureMetadata(bytes, header.is64()));
            }
        }
        iggySwf = new IggySwf(new MemoryDataStream(subFileEntriesData.get(subFileEntries.indexOf(mainEntry))), fields.get(mainEntry), header.is64());
        if (originalFile != null) {
            String filename = originalFile.getName();
            int extension = filename.lastIndexOf('.');
            iggySwf.setDefaultName((extension < 0 ? filename : filename.substring(0, extension)) + ".swf");
            iggySwf.setTextureResourceName((extension < 0 ? filename : filename.substring(0, extension)) + ".raw.iggytex");
        }
    }

    @Override
    public void readFromDataStream(ReadDataStreamInterface stream) throws IOException {
        long start = stream.position();
        try {
            readContainer(stream, false);
        } catch (IOException modernFailure) {
            if (header == null || header.is64()) {
                throw modernFailure;
            }
            // The shared 0x900 header has no SDK revision. Retry the 32 legacy
            // table convention, requiring its 64-byte root to confirm the choice.
            stream.seek(start, SeekMode.SET);
            try {
                readContainer(stream, true);
            } catch (IOException legacyFailure) {
                if (modernFailure instanceof LayoutMismatchException) {
                    legacyFailure.addSuppressed(modernFailure);
                    throw legacyFailure;
                }
                modernFailure.addSuppressed(legacyFailure);
                throw modernFailure;
            }
        }
    }

    private void readContainer(ReadDataStreamInterface stream, boolean legacy) throws IOException {
        subFileEntries.clear();
        subFileEntriesData.clear();
        fixupTargets.clear();
        textureMetadata.clear();
        mainEntry = null;
        iggySwf = null;
        header = null;
        header = new IggyHeader(stream, legacy);
        legacy32 = legacy && !header.is64();
        long tableSize = header.getNumSubfiles() * IggySubFileEntry.STRUCTURE_SIZE;
        if (header.getNumSubfiles() > Integer.MAX_VALUE || tableSize > stream.available()) {
            throw new IOException("Invalid Iggy descriptor table size");
        }
        for (int i = 0; i < header.getNumSubfiles(); i++) {
            IggySubFileEntry entry = new IggySubFileEntry(stream);
            if (legacy32 && header.getPlatform1() == 2) {
                entry.size = Integer.reverseBytes((int) entry.size) & 0xffffffffL;
                entry.offset = Integer.reverseBytes((int) entry.offset) & 0xffffffffL;
            }
            subFileEntries.add(entry);
        }
        for (IggySubFileEntry entry : subFileEntries) {
            if (entry.offset < 0 || entry.size < 0 || entry.size > Integer.MAX_VALUE
                    || entry.offset > stream.totalSize() - entry.size
                    || entry.offset < IggyHeader.STRUCT_SIZE + tableSize) {
                throw new IOException("Invalid Iggy block range");
            }
            stream.seek(entry.offset, SeekMode.SET);
            byte[] entryData = stream.readBytes((int) entry.size);
            subFileEntriesData.add(entryData);
        }
        parseEntries();
    }

    public void saveChanges() throws IOException {
        if (originalFile == null) {
            throw new IOException("Iggy has no backing file; use saveTo(File)");
        }
        saveTo(originalFile);
    }

    /** Save this movie to a file, including all pending converter changes. */
    public void saveTo(File file) throws IOException {
        if (!updateFlashEntry()) {
            throw new IOException("Could not update Iggy main block");
        }
        MemoryDataStream output = new MemoryDataStream();
        writeToDataStream(output);
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(output.getAllBytes());
        }
    }

    @Override
    public void writeToDataStream(WriteDataStreamInterface stream) throws IOException {
        header.setNumSubfiles(subFileEntries.size());
        header.writeToDataStream(stream);

        long startOffset = IggyHeader.STRUCT_SIZE + IggySubFileEntry.STRUCTURE_SIZE * subFileEntries.size();
        long currentOffset = startOffset;

        for (int i = 0; i < subFileEntries.size(); i++) {
            IggySubFileEntry entry = subFileEntries.get(i);
            entry.offset = currentOffset;
            currentOffset += entry.size;
            entry.writeToDataStream(stream);
        }

        for (int i = 0; i < subFileEntries.size(); i++) {
            byte[] entryData = subFileEntriesData.get(i);
            stream.writeBytes(entryData);
        }

    }
}
