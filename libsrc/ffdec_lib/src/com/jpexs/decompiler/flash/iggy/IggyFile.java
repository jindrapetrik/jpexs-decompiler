/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.flash.iggy;

import com.jpexs.decompiler.flash.iggy.streams.DataStreamInterface;
import com.jpexs.decompiler.flash.iggy.streams.IggyIndexBuilder;
import com.jpexs.decompiler.flash.iggy.streams.RandomAccessFileDataStream;
import com.jpexs.decompiler.flash.iggy.streams.ReadDataStreamInterface;
import com.jpexs.decompiler.flash.iggy.streams.SeekMode;
import com.jpexs.decompiler.flash.iggy.streams.StructureInterface;
import com.jpexs.decompiler.flash.iggy.streams.TemporaryDataStream;
import com.jpexs.decompiler.flash.iggy.streams.WriteDataStreamInterface;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 *
 * Based of works of somebody called eternity.
 *
 */
public class IggyFile implements StructureInterface {

    final static Logger LOGGER = Logger.getLogger(IggyFile.class.getName());

    private File originalFile;

    private IggyHeader header;

    private List<IggySubFileEntry> subFileEntries = new ArrayList<>();

    private List<byte[]> subFileEntriesData = new ArrayList<>();

    private IggySwf iggySwf;

    public static final int FIRST_TAG_POSITION = 3;

    public IggySwf getSwf() {
        return iggySwf;
    }

    public IggyFile(String filePath) throws IOException {
        this(new File(filePath));
    }

    public IggyFile(File file) throws IOException {
        this.originalFile = file;
        try (ReadDataStreamInterface stream = new RandomAccessFileDataStream(file)) {
            readFromDataStream(stream);
        }
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

        byte replacementData[];
        byte replacementIndexData[];
        IggyIndexBuilder ib = new IggyIndexBuilder();
        try (DataStreamInterface stream = new TemporaryDataStream()) {
            stream.setIndexing(ib);
            iggySwf.writeToDataStream(stream);
            replacementData = stream.getAllBytes();
            replacementIndexData = ib.getIndexBytes();
        } catch (IOException ex) {
            Logger.getLogger(IggyFile.class.getName()).log(Level.SEVERE, "Error during updating SWF", ex);
            return false;
        }

        long offsetsChange = 0;
        for (int i = 0; i < subFileEntries.size(); i++) {
            IggySubFileEntry entry = subFileEntries.get(i);
            entry.offset += offsetsChange;
            if (entry.type == IggySubFileEntry.TYPE_FLASH) {
                long oldSize = entry.size;
                long newSize = replacementData.length;
                entry.size = newSize;
                entry.size2 = newSize;
                offsetsChange = offsetsChange + (newSize - oldSize); //entries after this one will have modified offsets
                subFileEntriesData.set(i, replacementData);
            }
        }

        removeIndexEntries();
        IggySubFileEntry indexEntry = new IggySubFileEntry(IggySubFileEntry.TYPE_INDEX, replacementIndexData.length, replacementIndexData.length, 0 /*offset will be set automatically*/);
        subFileEntries.add(indexEntry);
        subFileEntriesData.add(replacementIndexData);
        return true;
    }

    private void parseEntries() throws IOException {
        for (int i = 0; i < subFileEntries.size(); i++) {
            IggySubFileEntry entry = subFileEntries.get(i);
            if (entry.type == IggySubFileEntry.TYPE_FLASH) {
                iggySwf = new IggySwf(new TemporaryDataStream(getEntryData(i)));
                break;
            }
            /*if (entry.type == IggySubFileEntry.TYPE_INDEX) {
                IggyIndexParser.parseIndex(true, new TemporaryDataStream(getEntryData(i)), new ArrayList<>(), new ArrayList<>());
            }*/
        }
    }

    @Override
    public void readFromDataStream(ReadDataStreamInterface stream) throws IOException {
        header = new IggyHeader(stream);
        if (!header.is64()) {
            throw new IOException("32 bit iggy files are not (yet) supported, sorry");
        }
        for (int i = 0; i < header.getNumSubfiles(); i++) {
            subFileEntries.add(new IggySubFileEntry(stream));
        }
        for (IggySubFileEntry entry : subFileEntries) {
            stream.seek(entry.offset, SeekMode.SET);
            byte[] entryData = stream.readBytes((int) entry.size);
            subFileEntriesData.add(entryData);
        }
        parseEntries();
    }

    public void saveChanges() throws IOException {
        updateFlashEntry();
        try (RandomAccessFileDataStream raf = new RandomAccessFileDataStream(originalFile)) {
            writeToDataStream(raf);
        }
    }

    @Override
    public void writeToDataStream(WriteDataStreamInterface stream) throws IOException {
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
