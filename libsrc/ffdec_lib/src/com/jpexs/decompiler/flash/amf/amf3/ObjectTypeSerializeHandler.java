package com.jpexs.decompiler.flash.amf.amf3;

import com.jpexs.decompiler.flash.amf.amf3.types.ObjectType;
import java.io.InputStream;
import java.io.OutputStream;

public interface ObjectTypeSerializeHandler {

    public ObjectType readObject(String className, InputStream is);

    public void writeObject(ObjectType val, OutputStream os);
}
