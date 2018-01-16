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
package com.jpexs.decompiler.flash.importers.amf.amf3.parser;

import com.jpexs.decompiler.flash.amf.amf3.Amf3InputStream;
import com.jpexs.decompiler.flash.amf.amf3.ListMap;
import com.jpexs.decompiler.flash.amf.amf3.NoSerializerExistsException;
import com.jpexs.decompiler.flash.amf.amf3.ObjectTypeSerializeHandler;
import com.jpexs.decompiler.flash.exporters.amf.amf3.Amf3Exporter;
import com.jpexs.decompiler.flash.importers.amf.amf3.Amf3Importer;
import com.jpexs.decompiler.flash.importers.amf.amf3.Amf3ParseException;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.MemoryInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class Amf3ImporterTest {

    @DataProvider(name = "files")
    public static Object[][] provideSamples() {
        return new Object[][]{
            {"all.bin"}
        };
    }

    private Map<String, ObjectTypeSerializeHandler> getSerializers() {
        Map<String, ObjectTypeSerializeHandler> serializers = new HashMap<>();
        serializers.put("CustomClass", new ObjectTypeSerializeHandler() {
            @Override
            public Map<String, Object> readObject(String className, InputStream is) throws IOException {
                Map<String, Object> members = new ListMap<>();
                members.put("val8", (long) is.read());
                members.put("val32", (long) ((is.read() << 24) + (is.read() << 16) + (is.read() << 8) + (is.read())));
                return members;
            }

            @Override
            public void writeObject(Map<String, Object> members, OutputStream os) throws IOException {
                Map<String, Object> memberMap = new HashMap<>();
                for (String key : members.keySet()) {
                    memberMap.put(key, members.get(key));
                }
                os.write((int) (long) (Long) memberMap.get("val8"));
                long val32 = (long) (Long) memberMap.get("val32");
                os.write((int) ((val32 >> 24) & 0xff));
                os.write((int) ((val32 >> 16) & 0xff));
                os.write((int) ((val32 >> 8) & 0xff));
                os.write((int) (val32 & 0xff));
            }
        });
        return serializers;
    }

    @Test(dataProvider = "files")
    public void testRecompile(String fileName) throws IOException, NoSerializerExistsException, Amf3ParseException {

        String originalFile = "testdata/amf3/generated/" + fileName;

        Amf3InputStream is = new Amf3InputStream(new MemoryInputStream(Helper.readFile(originalFile)));

        Object val = is.readValue("testValue", getSerializers());
        String exported1File = "testdata/amf3/generated/exported1." + fileName;
        String exported2File = "testdata/amf3/generated/exported2." + fileName;

        String exported1 = Amf3Exporter.amfToString(val);
        Helper.writeFile(exported1File, exported1.getBytes("UTF-8"));
        Amf3Importer imp = new Amf3Importer();
        Object valImported = imp.stringToAmf(exported1);
        String exported2 = Amf3Exporter.amfToString(valImported);

        Helper.writeFile(exported2File, exported2.getBytes("UTF-8"));

        Assert.assertEquals(exported2, exported1);
        new File(exported1File).delete();
        new File(exported2File).delete();

    }
}
