/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.sol;

import com.jpexs.decompiler.flash.EndOfStreamException;
import com.jpexs.decompiler.flash.amf.amf0.Amf0OutputStream;
import com.jpexs.decompiler.flash.exporters.amf.amf0.Amf0Exporter;
import com.jpexs.decompiler.flash.exporters.amf.amf3.Amf3Exporter;
import com.jpexs.decompiler.flash.importers.amf.AmfParseException;
import com.jpexs.decompiler.flash.importers.amf.amf0.Amf0Importer;
import com.jpexs.decompiler.flash.importers.amf.amf3.Amf3Importer;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author JPEXS
 */
public class SolFile {

    private DataInputStream is;

    private long pos = 0;

    private List<Tag> tags = new ArrayList<>();

    public SolFile(InputStream is) throws IOException {
        this.is = new DataInputStream(is);
        readTags();
    }

    public SolFile(String fileName, int amfVersion, Map<String, Object> amfValues) {
        tags.add(new LsoTag(fileName, amfVersion, amfValues));
    }

    public void writeTo(OutputStream os) throws IOException {
        for (Tag t : tags) {
            writeTag(os, t);
        }
    }

    private void readTags() throws IOException {
        while (is.available() > 0) {
            Tag t = readTag();
            t.readData();
            tags.add(t);
        }
    }

    private int readInternal() throws IOException {
        int ret = is.read();
        if (ret == -1) {
            throw new EndOfStreamException();
        }
        pos++;
        return ret;
    }

    private int readUI16() throws IOException {
        int b1 = readInternal();
        int b2 = readInternal();
        int ret = (b1 << 8) + b2;
        return ret;
    }

    private long readUI32() throws IOException {
        int b1 = readInternal();
        int b2 = readInternal();
        int b3 = readInternal();
        int b4 = readInternal();

        return ((b1 << 24) + (b2 << 16) + (b3 << 8) + b4) & 0xffffffff;
    }

    private byte[] readBytes(int count) throws IOException {
        byte[] ret = new byte[count];
        try {
            is.readFully(ret);
        } catch (EOFException eof) {
            throw new EndOfStreamException();
        }
        pos += count;
        return ret;
    }

    private void skipBytes(int count) throws IOException {
        is.skip(count);
        pos += count;
    }

    private void writeTag(OutputStream os, Tag t) throws IOException {
        Amf0OutputStream aos = new Amf0OutputStream(os);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        t.writeData(baos);
        int contentLength = baos.size();
        if (contentLength > 0x3F || t.forceWriteAsLong) {
            aos.writeU16((t.getTagType() << 6) | 0x3F);
            aos.writeU32(baos.size());
        } else {
            aos.writeU16((t.getTagType() << 6) | contentLength);
        }
        aos.writeBytes(baos.toByteArray());
    }

    private Tag readTag() throws IOException {
        long headerPos = pos;
        int tagTypeAndLength = readUI16();
        int contentLength = tagTypeAndLength & 0x3F;
        boolean writeAsLong = false;
        if (contentLength == 0x3F) {
            contentLength = (int) readUI32();
            writeAsLong = true;
        }
        int tagType = tagTypeAndLength >> 6;
        byte[] data = readBytes(contentLength);

        switch (tagType) {
            case LsoTag.ID:
                return new LsoTag(data, writeAsLong);
            case FilePathTag.ID:
                return new FilePathTag(data, writeAsLong);
            default:
                return new UnknownTag(tagType, data, writeAsLong);
        }
    }

    public List<Tag> getTags() {
        return tags;
    }

    private LsoTag getLsoTag() throws IOException {
        for (Tag t : getTags()) {
            if (t instanceof LsoTag) {
                return (LsoTag) t;
            }
        }
        return null;
    }

    public int getAmfVersion() throws IOException {
        LsoTag lsoTag = getLsoTag();
        if (lsoTag == null) {
            return -1;
        }
        return lsoTag.amfVersion;
    }

    public String getFileName() throws IOException {
        LsoTag lsoTag = getLsoTag();
        if (lsoTag == null) {
            return null;
        }
        return lsoTag.fileName;
    }

    public Map<String, Object> getAmfValues() throws IOException {
        LsoTag lsoTag = getLsoTag();
        if (lsoTag == null) {
            return new HashMap<>();
        }
        return lsoTag.amfValues;
    }

    public static void main(String[] args) throws FileNotFoundException, IOException {
        SolFile sol0 = new SolFile(new FileInputStream("testdata/sharedobjects/data/amf0test.sol"));
        sol0.writeTo(new FileOutputStream("testdata/sharedobjects/data/out/amf0test.sol"));
        for (Tag t : sol0.getTags()) {
            if (t instanceof LsoTag) {
                LsoTag lt = (LsoTag) t;
                String amf0string = Amf0Exporter.amfMapToString(lt.amfValues, 0, "\r\n");
                Amf0Importer importer = new Amf0Importer();
                try {
                    Map<String, Object> imported = importer.stringToAmfMap(amf0string);
                    String amf0stringNew = Amf0Exporter.amfMapToString(imported, 0, "\r\n");
                    System.err.println("same0 = " + amf0stringNew.equals(amf0string));
                } catch (AmfParseException ex) {
                    ex.printStackTrace();
                }
            }
        }

        SolFile sol3 = new SolFile(new FileInputStream("testdata/sharedobjects/data/amf3test.sol"));
        sol3.writeTo(new FileOutputStream("testdata/sharedobjects/data/out/amf3test.sol"));
        for (Tag t : sol3.getTags()) {
            if (t instanceof LsoTag) {
                LsoTag lt = (LsoTag) t;
                String amf3string = Amf3Exporter.amfMapToString(lt.amfValues, "  ", "\r\n", 0);
                Amf3Importer importer = new Amf3Importer();
                try {
                    Map<String, Object> imported = importer.stringToAmfMap(amf3string);
                    String amf3stringNew = Amf3Exporter.amfMapToString(imported, "  ", "\r\n", 0);;
                    System.err.println("same3 = " + amf3stringNew.equals(amf3string));
                } catch (AmfParseException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
