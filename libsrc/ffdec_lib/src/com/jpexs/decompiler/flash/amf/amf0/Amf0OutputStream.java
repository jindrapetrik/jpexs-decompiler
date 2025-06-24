/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.amf.amf0;

import com.jpexs.decompiler.flash.amf.amf0.types.ArrayType;
import com.jpexs.decompiler.flash.amf.amf0.types.BasicType;
import com.jpexs.decompiler.flash.amf.amf0.types.ComplexObject;
import com.jpexs.decompiler.flash.amf.amf0.types.DateType;
import com.jpexs.decompiler.flash.amf.amf0.types.EcmaArrayType;
import com.jpexs.decompiler.flash.amf.amf0.types.ObjectType;
import com.jpexs.decompiler.flash.amf.amf0.types.TypedObjectType;
import com.jpexs.decompiler.flash.amf.amf0.types.XmlDocumentType;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * AMF0 output stream.
 * @author JPEXS
 */
public class Amf0OutputStream extends OutputStream {

    private final OutputStream os;

    /**
     * Constructor.
     *
     * @param os Output stream
     */
    public Amf0OutputStream(OutputStream os) {
        this.os = os;
    }

    /**
     * Writes U8 (unsigned 8-bit integer).
     *
     * @param v Value
     * @throws IOException On I/O error
     */
    public void writeU8(int v) throws IOException {
        write(v);
    }

    /**
     * Writes U16 (unsigned 16-bit integer).
     *
     * @param v Value
     * @throws IOException On I/O error
     */
    public void writeU16(int v) throws IOException {
        int b1 = (v >> 8) & 0xff;
        int b2 = v & 0xff;
        write(b1);
        write(b2);
    }

    /**
     * Writes S16 (signed 16-bit integer).
     *
     * @param v Value
     * @throws IOException On I/O error
     */
    public void writeS16(int v) throws IOException {
        int b1 = (v >> 8) & 0xff;
        int b2 = v & 0xff;
        write(b1);
        write(b2);
    }

    /**
     * Writes U32 (unsigned 32-bit integer).
     *
     * @param v Value
     * @throws IOException On I/O error
     */
    public void writeU32(long v) throws IOException {
        int b1 = (int) ((v >> 24) & 0xff);
        int b2 = (int) ((v >> 16) & 0xff);
        int b3 = (int) ((v >> 8) & 0xff);
        int b4 = (int) (v & 0xff);

        write(b1);
        write(b2);
        write(b3);
        write(b4);
    }

    /**
     * Writes double.
     *
     * @param v Value
     * @throws IOException On I/O error
     */
    public void writeDouble(double v) throws IOException {
        writeLong(Double.doubleToLongBits(v));
    }

    /**
     * Writes long.
     *
     * @param value Value
     * @throws IOException On I/O error
     */
    private void writeLong(long value) throws IOException {
        byte[] writeBuffer = new byte[8];
        writeBuffer[0] = (byte) (value >>> 56);
        writeBuffer[1] = (byte) (value >>> 48);
        writeBuffer[2] = (byte) (value >>> 40);
        writeBuffer[3] = (byte) (value >>> 32);
        writeBuffer[4] = (byte) (value >>> 24);
        writeBuffer[5] = (byte) (value >>> 16);
        writeBuffer[6] = (byte) (value >>> 8);
        writeBuffer[7] = (byte) (value);
        write(writeBuffer);
    }

    /**
     * Writes bytes.
     *
     * @param data Data
     * @throws IOException On I/O error
     */
    public void writeBytes(byte[] data) throws IOException {
        os.write(data);
    }

    @Override
    public void write(int v) throws IOException {
        os.write(v);
    }

    @Override
    public void write(byte[] b) throws IOException {
        os.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        os.write(b, off, len);
    }

    public void writeUtf8(String value) throws IOException {
        byte[] data = value.getBytes("UTF-8");
        writeU16(data.length);
        writeBytes(data);
    }

    public void writeUtf8Long(String value) throws IOException {
        byte[] data = value.getBytes("UTF-8");
        writeU32(data.length);
        writeBytes(data);
    }

    public void writeObjectProperty(String name, Object value, List<Object> complexObjectsList) throws IOException {
        writeUtf8(name);
        writeValue(value, complexObjectsList);
    }

    public void writeUtf8Empty() throws IOException {
        writeU16(0);
    }

    public void writeValue(Object value, List<Object> complexObjectsList) throws IOException {

        if (value instanceof ComplexObject) {
            int index = complexObjectsList.indexOf(value);
            if (index != -1 && index <= 65535) {
                write(Marker.REFERENCE);
                writeU16(index);
                return;
            } else {
                complexObjectsList.add(value);
            }
        }

        if (value instanceof Double) {
            write(Marker.NUMBER);
            writeDouble((Double) value);
        } else if (value instanceof Boolean) {
            write(Marker.BOOLEAN);
            write(((Boolean) value) ? 1 : 0);
        } else if (value instanceof String) {
            String sval = (String) value;
            if (sval.length() > 65535) {
                write(Marker.LONG_STRING);
                writeUtf8Long(sval);
            } else {
                write(Marker.STRING);
                writeUtf8(sval);
            }
        } else if (value instanceof ObjectType) {
            write(Marker.OBJECT);
            ObjectType ot = (ObjectType) value;
            for (String key : ot.properties.keySet()) {
                writeObjectProperty(key, ot.properties.get(key), complexObjectsList);
            }
            writeUtf8Empty();
            write(Marker.OBJECT_END);
        } else if (value == BasicType.NULL) {
            write(Marker.NULL);
        } else if (value == BasicType.UNDEFINED) {
            write(Marker.UNDEFINED);
        } else if (value instanceof EcmaArrayType) {
            write(Marker.ECMA_ARRAY);
            EcmaArrayType ea = (EcmaArrayType) value;
            writeU32(ea.denseValues.size());
            for (String key : ea.denseValues.keySet()) {
                writeObjectProperty(key, ea.denseValues.get(key), complexObjectsList);
            }
            for (String key : ea.associativeValues.keySet()) {
                writeObjectProperty(key, ea.associativeValues.get(key), complexObjectsList);
            }
            writeUtf8Empty();
            write(Marker.OBJECT_END);
        } else if (value instanceof ArrayType) {
            write(Marker.STRICT_ARRAY);
            ArrayType at = (ArrayType) value;
            writeU32(at.values.size());
            for (Object v : at.values) {
                writeValue(v, complexObjectsList);
            }
        } else if (value instanceof DateType) {
            write(Marker.DATE);
            DateType dt = (DateType) value;
            writeDouble(dt.getVal());
            writeS16(dt.getTimezone());
        } else if (value instanceof XmlDocumentType) {
            write(Marker.XML_DOCUMENT);
            XmlDocumentType xmlDoc = (XmlDocumentType) value;
            writeUtf8Long(xmlDoc.getData());
        } else if (value instanceof TypedObjectType) {
            write(Marker.TYPED_OBJECT);
            TypedObjectType tot = (TypedObjectType) value;
            writeUtf8(tot.className);
            for (String key : tot.properties.keySet()) {
                writeObjectProperty(key, tot.properties.get(key), complexObjectsList);
            }
            writeUtf8Empty();
            write(Marker.OBJECT_END);
        } else {
            throw new IllegalArgumentException("Unsupported value type for serialization");
        }

        //TODO: Switching to AMF3 when necessary
    }
}
