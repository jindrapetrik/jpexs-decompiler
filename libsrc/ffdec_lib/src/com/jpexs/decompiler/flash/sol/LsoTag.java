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

import com.jpexs.decompiler.flash.amf.amf0.Amf0InputStream;
import com.jpexs.decompiler.flash.amf.amf0.Amf0OutputStream;
import com.jpexs.decompiler.flash.amf.amf3.Amf3InputStream;
import com.jpexs.decompiler.flash.amf.amf3.Amf3OutputStream;
import com.jpexs.decompiler.flash.amf.amf3.NoSerializerExistsException;
import com.jpexs.decompiler.flash.amf.amf3.Traits;
import com.jpexs.helpers.MemoryInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class LsoTag extends Tag {

    public static final int ID = 2;

    public String fileName;
    public int amfVersion;
    public Map<String, Object> amfValues = new LinkedHashMap<>();

    public LsoTag(byte[] data, boolean forceWriteAsLong) {
        super(ID, "DefineLso", data, forceWriteAsLong);
    }

    public LsoTag(String fileName, int amfVersion, Map<String, Object> amfValues) {
        super(ID, "DefineLso", new byte[0], true);
        this.fileName = fileName;
        this.amfVersion = amfVersion;
        this.amfValues = amfValues;
    }

    @Override
    public void readData() throws IOException {
        Amf0InputStream is = new Amf0InputStream(new MemoryInputStream(data));

        byte[] expectedSig = new byte[]{'T', 'C', 'S', 'O'};
        byte[] actualSig = is.readBytes(4);
        if (!Arrays.equals(actualSig, expectedSig)) {
            throw new IllegalArgumentException("Not a SOL file - invalid signature");
        }
        is.skip(6); //00 04 00 00 00 00
        int filenameLen = is.readU16("filenameLen");
        fileName = new String(is.readBytes(filenameLen), "UTF-8");

        amfVersion = (int) is.readU32("amfVersion");
        if (amfVersion != 0 && amfVersion != 3) {
            throw new IllegalArgumentException("Unsupported AMF version");
        }
        byte[] amfData = is.readBytes(is.available());
        if (amfVersion == 0) {
            Amf0InputStream ais = new Amf0InputStream(new MemoryInputStream(amfData));
            List<Object> complexObjects = new ArrayList<>();
            while (ais.available() > 0) {
                String varName = ais.readUtf8("varName");
                try {
                    Object varValue = ais.readValue("varValue");
                    amfValues.put(varName, varValue);
                } catch (NoSerializerExistsException ex) {
                    throw new IllegalArgumentException("Serializer for class " + ex.getClassName() + " not found");
                }
                ais.read(); //ending byte
            }
            ais.resolveMapReferences(amfValues);
        }

        if (amfVersion == 3) {
            Amf3InputStream ais = new Amf3InputStream(new MemoryInputStream(amfData));
            List<Object> objectsTable = new ArrayList<>();
            List<Traits> traitsTable = new ArrayList<>();
            List<String> stringTable = new ArrayList<>();

            while (ais.available() > 0) {
                String varName = ais.readUtf8Vr("varName", stringTable);
                try {
                    Object varValue = ais.readValue("varValue", new HashMap<>(), objectsTable, traitsTable, stringTable);
                    amfValues.put(varName, varValue);
                } catch (NoSerializerExistsException ex) {
                    throw new IllegalArgumentException("Serializer for class " + ex.getClassName() + " not found");
                }
                ais.read(); //ending byte
            }
        }

    }

    @Override
    public void writeData(OutputStream os) throws IOException {
        Amf0OutputStream aos = new Amf0OutputStream(os);
        aos.write(new byte[]{'T', 'C', 'S', 'O'});
        aos.write(new byte[]{0x00, 0x04, 0x00, 0x00, 0x00, 0x00});
        byte[] fileNameData = fileName.getBytes("UTF-8");
        aos.writeU16(fileNameData.length);
        aos.write(fileNameData);
        aos.writeU32(amfVersion);

        if (amfVersion == 0) {
            List<Object> complexObjects = new ArrayList<>();
            for (String key : amfValues.keySet()) {
                aos.writeUtf8(key);
                aos.writeValue(amfValues.get(key), complexObjects);
                aos.write(0);
            }
        }
        if (amfVersion == 3) {
            Amf3OutputStream a3os = new Amf3OutputStream(os);
            List<String> stringTable = new ArrayList<>();
            List<Traits> traitTable = new ArrayList<>();
            List<Object> objectTable = new ArrayList<>();
            for (String key : amfValues.keySet()) {
                try {
                    a3os.writeUtf8Vr(key, stringTable);
                    a3os.writeValue(amfValues.get(key), new HashMap<>(), stringTable, traitTable, objectTable);
                } catch (NoSerializerExistsException ex) {
                    throw new IllegalArgumentException("Serializer not found for class " + ex.getClassName());
                }
                aos.write(0);
            }
        }

    }
}
