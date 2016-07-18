package com.jpexs.decompiler.flash.amf.amf3;

import com.jpexs.helpers.Helper;
import com.jpexs.helpers.MemoryInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
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
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class Amf3OutputStreamTest {

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
                Map<String, Object> memberMap = new HashMap<>();
                for (Pair<String, Object> m : members) {
                    memberMap.put(m.getFirst(), m.getSecond());
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

    private static byte[] readFile(File file) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte buf[] = new byte[1024];
        int cnt;
        try (FileInputStream fis = new FileInputStream(file)) {
            while ((cnt = fis.read(buf)) > 0) {
                baos.write(buf, 0, cnt);
            }
        }
        return baos.toByteArray();
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
