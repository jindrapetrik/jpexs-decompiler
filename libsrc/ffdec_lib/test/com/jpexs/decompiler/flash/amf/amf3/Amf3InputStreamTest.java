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

import com.jpexs.decompiler.flash.amf.amf3.types.ArrayType;
import com.jpexs.decompiler.flash.amf.amf3.types.DictionaryType;
import com.jpexs.decompiler.flash.amf.amf3.types.ObjectType;
import com.jpexs.decompiler.flash.amf.amf3.types.VectorObjectType;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.MemoryInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import static org.testng.Assert.*;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;

public class Amf3InputStreamTest {

    private Amf3InputStream is;

    @AfterTest
    public void deinitStream() {
        if (is != null) {
            try {
                is.close();
            } catch (IOException ex) {
                //ignore
            }
            is = null;
        }
    }

    private void initStream(String fileName) throws IOException {
        String file = "testdata/amf3/generated/" + fileName;
        is = new Amf3InputStream(new MemoryInputStream(Helper.readFile(file)));
    }

    @Test
    public void testReadObject() throws IOException, NoSerializerExistsException {
        initStream("all.bin");
        is.readValue("testValue");
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
                throw new UnsupportedOperationException("Not implemented");
            }
        });
        return serializers;
    }

    @Test(expectedExceptions = NoSerializerExistsException.class)
    public void testReadCustomSerializedNeedsSerializer() throws IOException, NoSerializerExistsException {
        initStream("custom.bin");
        is.readValue("testValue"); //needs deserializer for CustomClass
    }

    @Test
    public void testReadCustomSerialized() throws IOException, NoSerializerExistsException {
        initStream("custom.bin");
        is.readValue("testValue", getSerializers());
    }

    @Test
    public void testNoSerializerHandlingInObjectDynamicProp() throws IOException {
        initStream("noserializer_object_dynamic.bin");
        try {
            is.readValue("testValue");
        } catch (NoSerializerExistsException ex) {
            assertTrue(ex.getIncompleteData() instanceof ObjectType, "Expected datatype: ObjectType, Actual datatype: " + ex.getIncompleteData().getClass());
            //TODO: examinate the data more
        }
    }

    @Test
    public void testNoSerializerHandlingInObjectSealedProp() throws IOException {
        initStream("noserializer_object_sealed.bin");
        try {
            is.readValue("testValue");
        } catch (NoSerializerExistsException ex) {
            assertTrue(ex.getIncompleteData() instanceof ObjectType, "Expected datatype: ObjectType, Actual datatype: " + ex.getIncompleteData().getClass());
            //TODO: examinate the data more
        }
    }

    @Test
    public void testNoSerializerHandlingInArrayDense() throws IOException {
        initStream("noserializer_array_dense.bin");
        try {
            is.readValue("testValue");
        } catch (NoSerializerExistsException ex) {
            assertTrue(ex.getIncompleteData() instanceof ArrayType, "Expected datatype: ArrayType, Actual datatype: " + ex.getIncompleteData().getClass());
            //TODO: examinate the data more
        }
    }

    @Test
    public void testNoSerializerHandlingInArrayAssociative() throws IOException {
        initStream("noserializer_array_associative.bin");
        try {
            is.readValue("testValue");
        } catch (NoSerializerExistsException ex) {
            assertTrue(ex.getIncompleteData() instanceof ArrayType, "Expected datatype: ArrayType, Actual datatype: " + ex.getIncompleteData().getClass());
            //TODO: examinate the data more
        }
    }

    @Test
    public void testNoSerializerHandlingInVector() throws IOException {
        initStream("noserializer_vector.bin");
        try {
            is.readValue("testValue");
        } catch (NoSerializerExistsException ex) {
            assertTrue(ex.getIncompleteData() instanceof VectorObjectType, "Expected datatype: VectorObjectType, Actual datatype: " + ex.getIncompleteData().getClass());
            //TODO: examinate the data more
        }
    }

    @Test
    public void testNoSerializerHandlingInDictionaryValues() throws IOException {
        initStream("noserializer_dictionary_value.bin");
        try {
            is.readValue("testValue");
        } catch (NoSerializerExistsException ex) {
            assertTrue(ex.getIncompleteData() instanceof DictionaryType, "Expected datatype: DictionaryType, Actual datatype: " + ex.getIncompleteData().getClass());
            //TODO: examinate the data more
        }
    }

    @Test
    public void testNoSerializerHandlingInDictionaryKeys() throws IOException {
        initStream("noserializer_dictionary_key.bin");
        try {
            is.readValue("testValue");
        } catch (NoSerializerExistsException ex) {
            assertTrue(ex.getIncompleteData() instanceof DictionaryType, "Expected datatype: DictionaryType, Actual datatype: " + ex.getIncompleteData().getClass());
            //TODO: examinate the data more
        }
    }

    @Test(expectedExceptions = UnsupportedValueTypeException.class)
    public void testUnsupportedMarker() throws IOException, NoSerializerExistsException {
        final int UNSUPPORTED_MARKER = 100;
        is = new Amf3InputStream(new MemoryInputStream(new byte[]{UNSUPPORTED_MARKER}));
        is.readValue("testValue");
    }
}
