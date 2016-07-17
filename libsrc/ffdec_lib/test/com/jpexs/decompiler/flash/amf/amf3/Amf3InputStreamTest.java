package com.jpexs.decompiler.flash.amf.amf3;

import com.jpexs.decompiler.flash.amf.amf3.types.ArrayType;
import com.jpexs.decompiler.flash.amf.amf3.types.DictionaryType;
import com.jpexs.decompiler.flash.amf.amf3.types.ObjectType;
import com.jpexs.decompiler.flash.amf.amf3.types.VectorObjectType;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.testng.Assert.*;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;

public class Amf3InputStreamTest {

    private FileInputStream fis;
    private AMF3InputStream is;

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
        if (fis != null) {
            try {
                fis.close();
            } catch (IOException ex) {
                //ignore
            }
            fis = null;
        }
    }

    private void initStream(String fileName) throws FileNotFoundException {
        fis = new FileInputStream("testdata/amf3/generated/" + fileName);
        is = new AMF3InputStream(fis);
    }

    @Test
    public void testReadObject() throws IOException, NoSerializerExistsException {
        initStream("all.bin");
        is.readValue();
    }

    private Map<String, ObjectTypeSerializeHandler> getSerializers() {
        Map<String, ObjectTypeSerializeHandler> serializers = new HashMap<>();
        serializers.put("CustomClass", new ObjectTypeSerializeHandler() {
            @Override
            public List<Pair<String, Object>> readObject(String className, InputStream is) throws IOException {
                List<Pair<String, Object>> members = new ArrayList<>();
                members.add(new Pair<>("val8", (long) is.read()));
                members.add(new Pair<>("val32", (long) ((is.read() << 24) + (is.read() << 16) + (is.read() << 8) + (is.read()))));
                return members;
            }

            @Override
            public void writeObject(List<Pair<String, Object>> members, OutputStream os) throws IOException {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        return serializers;
    }

    @Test(expectedExceptions = NoSerializerExistsException.class)
    public void testReadCustomSerializedNeedsSerializer() throws IOException, NoSerializerExistsException {
        initStream("custom.bin");
        is.readValue(); //needs deserializer for CustomClass
    }

    @Test
    public void testReadCustomSerialized() throws IOException, NoSerializerExistsException {
        initStream("custom.bin");
        is.readValue(getSerializers());
    }

    @Test
    public void testNoSerializerHandlingInObjectDynamicProp() throws IOException {
        initStream("noserializer_object_dynamic.bin");
        try {
            is.readValue();
        } catch (NoSerializerExistsException ex) {
            assertTrue(ex.getIncompleteData() instanceof ObjectType, "Expected datatype: ObjectType, Actual datatype: " + ex.getIncompleteData().getClass());
            //TODO: examinate the data more
        }
    }

    @Test
    public void testNoSerializerHandlingInObjectSealedProp() throws IOException {
        initStream("noserializer_object_sealed.bin");
        try {
            is.readValue();
        } catch (NoSerializerExistsException ex) {
            assertTrue(ex.getIncompleteData() instanceof ObjectType, "Expected datatype: ObjectType, Actual datatype: " + ex.getIncompleteData().getClass());
            //TODO: examinate the data more
        }
    }

    @Test
    public void testNoSerializerHandlingInArrayDense() throws IOException {
        initStream("noserializer_array_dense.bin");
        try {
            is.readValue();
        } catch (NoSerializerExistsException ex) {
            assertTrue(ex.getIncompleteData() instanceof ArrayType, "Expected datatype: ArrayType, Actual datatype: " + ex.getIncompleteData().getClass());
            //TODO: examinate the data more
        }
    }

    @Test
    public void testNoSerializerHandlingInArrayAssociative() throws IOException {
        initStream("noserializer_array_associative.bin");
        try {
            is.readValue();
        } catch (NoSerializerExistsException ex) {
            assertTrue(ex.getIncompleteData() instanceof ArrayType, "Expected datatype: ArrayType, Actual datatype: " + ex.getIncompleteData().getClass());
            //TODO: examinate the data more
        }
    }

    @Test
    public void testNoSerializerHandlingInVector() throws IOException {
        initStream("noserializer_vector.bin");
        try {
            is.readValue();
        } catch (NoSerializerExistsException ex) {
            assertTrue(ex.getIncompleteData() instanceof VectorObjectType, "Expected datatype: VectorObjectType, Actual datatype: " + ex.getIncompleteData().getClass());
            //TODO: examinate the data more
        }
    }

    @Test
    public void testNoSerializerHandlingInDictionaryValues() throws IOException {
        initStream("noserializer_dictionary_value.bin");
        try {
            is.readValue();
        } catch (NoSerializerExistsException ex) {
            assertTrue(ex.getIncompleteData() instanceof DictionaryType, "Expected datatype: DictionaryType, Actual datatype: " + ex.getIncompleteData().getClass());
            //TODO: examinate the data more
        }
    }

    @Test
    public void testNoSerializerHandlingInDictionaryKeys() throws IOException {
        initStream("noserializer_dictionary_key.bin");
        try {
            is.readValue();
        } catch (NoSerializerExistsException ex) {
            assertTrue(ex.getIncompleteData() instanceof DictionaryType, "Expected datatype: DictionaryType, Actual datatype: " + ex.getIncompleteData().getClass());
            //TODO: examinate the data more
        }
    }

    @Test(expectedExceptions = UnsupportedValueType.class)
    public void testUnsupportedMarker() throws IOException, NoSerializerExistsException {
        final int UNSUPPORTED_MARKER = 100;
        is = new AMF3InputStream(new ByteArrayInputStream(new byte[]{UNSUPPORTED_MARKER}));
        is.readValue();
    }
}
