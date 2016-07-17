package com.jpexs.decompiler.flash.amf.amf3;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface ObjectTypeSerializeHandler {

    public List<Pair<String, Object>> readObject(String className, InputStream is) throws IOException;

    public void writeObject(List<Pair<String, Object>> members, OutputStream os) throws IOException;
}
