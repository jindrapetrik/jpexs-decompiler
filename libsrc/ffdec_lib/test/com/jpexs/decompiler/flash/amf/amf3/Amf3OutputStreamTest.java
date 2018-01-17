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
package com.jpexs.decompiler.flash.amf.amf3;

import com.jpexs.helpers.Helper;
import com.jpexs.helpers.MemoryInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class Amf3OutputStreamTest {

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

    @DataProvider(name = "files")
    public static Object[][] provideSamples() {
        return new Object[][]{
            {"all.bin"},
            {"custom.bin"},
            {"noserializer_array_associative.bin"},
            {"noserializer_array_dense.bin"},
            {"noserializer_dictionary_key.bin"},
            {"noserializer_dictionary_value.bin"},
            {"noserializer_object_dynamic.bin"},
            {"noserializer_object_sealed.bin"},
            {"noserializer_vector.bin"}
        };
    }

    @Test(dataProvider = "files")
    public void testRecompile(String fileName) throws FileNotFoundException, IOException, NoSerializerExistsException {

        String originalFile = "testdata/amf3/generated/" + fileName;

        byte[] originalData = Helper.readFile(originalFile);
        byte[] savedData;

        Amf3InputStream is = new Amf3InputStream(new MemoryInputStream(Helper.readFile(originalFile)));

        Object val = is.readValue("testValue", getSerializers());
        String savedFile = "testdata/amf3/generated/recompiled." + fileName;
        try (FileOutputStream fos = new FileOutputStream(savedFile)) {
            Amf3OutputStream os = new Amf3OutputStream(fos);
            os.writeValue(val, getSerializers());
        }
        savedData = Helper.readFile(savedFile);

        Assert.assertEquals(savedData, originalData);
        new File(savedFile).delete();

    }
}
