package com.jpexs.decompiler.flash.amf.amf3;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public interface ObjectTypeSerializeHandler {

    public Map<String, Object> readObject(String className, InputStream is) throws IOException;

    public void writeObject(Map<String, Object> members, OutputStream os) throws IOException;
}
