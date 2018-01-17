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
import com.jpexs.decompiler.flash.amf.amf3.types.BasicType;
import com.jpexs.decompiler.flash.amf.amf3.types.ByteArrayType;
import com.jpexs.decompiler.flash.amf.amf3.types.DateType;
import com.jpexs.decompiler.flash.amf.amf3.types.DictionaryType;
import com.jpexs.decompiler.flash.amf.amf3.types.ObjectType;
import com.jpexs.decompiler.flash.amf.amf3.types.VectorDoubleType;
import com.jpexs.decompiler.flash.amf.amf3.types.VectorIntType;
import com.jpexs.decompiler.flash.amf.amf3.types.VectorObjectType;
import com.jpexs.decompiler.flash.amf.amf3.types.VectorUIntType;
import com.jpexs.decompiler.flash.amf.amf3.types.XmlDocType;
import com.jpexs.decompiler.flash.amf.amf3.types.XmlType;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class Amf3OutputStream extends OutputStream {

    public final static Logger LOGGER = Logger.getLogger(Amf3OutputStream.class.getName());

    private final OutputStream os;
    private static final int NO_REFERENCE_FLAG = 1;

    private static final int NO_TRAIT_REFERENCE_FLAG = 2;
    private static final int TRAIT_EXT_FLAG = 4;
    private static final int DYNAMIC_FLAG = 8;

    public Amf3OutputStream(OutputStream os) {
        this.os = os;
    }

    public void writeU8(int v) throws IOException {
        write(v);
    }

    public void writeU16(int v) throws IOException {
        int b1 = (v >> 8) & 0xff;
        int b2 = v & 0xff;
        write(b1);
        write(b2);
    }

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

    public void writeDouble(double v) throws IOException {
        writeLong(Double.doubleToLongBits(v));
    }

    public void writeBytes(byte[] data) throws IOException {
        os.write(data);
    }

    public void writeU29(long v) throws IOException {
        v = v & 0x3FFFFFFF; //make unsigned

        final int USE_NEXT_BYTE_FLAG = 0x80;
        final int SEVEN_BITS_MASK = 0x7f;
        final int EIGHT_BITS_MASK = 0xff;

        if (v <= 0x7F) {
            write((int) v);
        } else if (v <= 0x3FFF) {
            int b1 = (int) ((v >> 7) & SEVEN_BITS_MASK) | USE_NEXT_BYTE_FLAG;
            int b2 = (int) (v & SEVEN_BITS_MASK);
            write(b1);
            write(b2);
        } else if (v <= 0x1FFFFF) {
            int b1 = (int) ((v >> 14) & SEVEN_BITS_MASK) | USE_NEXT_BYTE_FLAG;
            int b2 = (int) ((v >> 7) & SEVEN_BITS_MASK) | USE_NEXT_BYTE_FLAG;
            int b3 = (int) (v & SEVEN_BITS_MASK);
            write(b1);
            write(b2);
            write(b3);
        } else if (v <= 0x3FFFFFFF) {
            int b1 = (int) ((v >> 21) & SEVEN_BITS_MASK) | USE_NEXT_BYTE_FLAG;
            int b2 = (int) ((v >> 14) & SEVEN_BITS_MASK) | USE_NEXT_BYTE_FLAG;
            int b3 = (int) ((v >> 7) & SEVEN_BITS_MASK) | USE_NEXT_BYTE_FLAG;
            int b4 = (int) (v & EIGHT_BITS_MASK);
            write(b1);
            write(b2);
            write(b3);
            write(b4);
        } else {
            throw new IllegalArgumentException("Value too long");
        }
    }

    private void writeUtf8Vr(String val, List<String> stringTable) throws IOException {
        int stringIndex = stringTable.indexOf(val);
        if (stringIndex == -1) {
            if (!val.isEmpty()) {
                stringTable.add(val);
            }
            byte[] data = val.getBytes("UTF-8");
            writeU29((data.length << 1) | NO_REFERENCE_FLAG);
            writeBytes(data);
        } else {
            writeU29((stringIndex << 1));
        }
    }

    private void writeByteArray(ByteArrayType val, List<Object> objectTable) throws IOException {
        int objectIndex = objectTable.indexOf(val);
        if (objectIndex == -1) {
            objectTable.add(val);
            byte[] data = val.getData();
            writeU29((data.length << 1) | NO_REFERENCE_FLAG);
            writeBytes(data);
        } else {
            writeU29((objectIndex << 1));
        }
    }

    private void writeXmlDoc(XmlDocType val, List<Object> objectTable) throws IOException {
        int objectIndex = objectTable.indexOf(val);
        if (objectIndex == -1) {
            objectTable.add(val);
            byte[] data = val.getData().getBytes("UTF-8");
            writeU29((data.length << 1) | NO_REFERENCE_FLAG);
            writeBytes(data);
        } else {
            writeU29((objectIndex << 1));
        }
    }

    private void writeXml(XmlType val, List<Object> objectTable) throws IOException {
        int objectIndex = objectTable.indexOf(val);
        if (objectIndex == -1) {
            objectTable.add(val);
            byte[] data = val.getData().getBytes("UTF-8");
            writeU29((data.length << 1) | NO_REFERENCE_FLAG);
            writeBytes(data);
        } else {
            writeU29((objectIndex << 1));
        }
    }

    @Override
    public void write(int v) throws IOException {
        os.write(v);
    }

    private void writeArray(ArrayType val, Map<String, ObjectTypeSerializeHandler> serializers, List<String> stringTable, List<Traits> traitsTable, List<Object> objectTable) throws IOException, NoSerializerExistsException {
        int objectIndex = objectTable.indexOf(val);
        if (objectIndex == -1) {
            objectTable.add(val);

            writeU29((val.getDenseValues().size() << 1) | NO_REFERENCE_FLAG);
            for (String key : val.associativeKeySet()) {
                writeUtf8Vr(key, stringTable);
                writeValue(val.getAssociative(key), serializers, stringTable, traitsTable, objectTable);
            }
            writeUtf8Vr("", stringTable);
            for (Object v : val.getDenseValues()) {
                writeValue(v, serializers, stringTable, traitsTable, objectTable);
            }
        } else {
            writeU29((objectIndex << 1));
        }
    }

    private void writeObject(ObjectType val, Map<String, ObjectTypeSerializeHandler> serializers, List<String> stringTable, List<Traits> traitsTable, List<Object> objectTable) throws IOException, NoSerializerExistsException {
        int objectIndex = objectTable.indexOf(val);
        if (objectIndex == -1) {
            objectTable.add(val);
            Traits traits = val.getTraits();
            int traitsIndex = traitsTable.indexOf(traits);
            if (traitsIndex == -1) {
                if (val.isSerialized()) {
                    writeU29(NO_REFERENCE_FLAG | NO_TRAIT_REFERENCE_FLAG | TRAIT_EXT_FLAG);
                    writeUtf8Vr(val.getClassName(), stringTable);
                    if (serializers.containsKey(val.getClassName())) {
                        serializers.get(val.getClassName()).writeObject(val.getSerializedMembers(), os);
                    } else if (val.getSerializedData() != null) {
                        writeBytes(val.getSerializedData());
                    } else {
                        throw new NoSerializerExistsException(val.getClassName(), null, null);
                    }
                } else {
                    traitsTable.add(traits);
                    writeU29((val.sealedMembersSize() << 4) | NO_REFERENCE_FLAG | NO_TRAIT_REFERENCE_FLAG | (traits.isDynamic() ? DYNAMIC_FLAG : 0));
                    writeUtf8Vr(val.getClassName(), stringTable);
                    for (String key : val.sealedMembersKeySet()) {
                        writeUtf8Vr(key, stringTable);
                    }
                }
            } else {
                writeU29((traitsIndex << 2) | NO_REFERENCE_FLAG);
            }
            for (String key : val.sealedMembersKeySet()) {
                writeValue(val.getSealedMember(key), serializers, stringTable, traitsTable, objectTable);
            }
            if (traits.isDynamic()) {
                for (String key : val.dynamicMembersKeySet()) {
                    writeUtf8Vr(key, stringTable);
                    writeValue(val.getDynamicMember(key), serializers, stringTable, traitsTable, objectTable);
                }
                writeUtf8Vr("", stringTable);
            }
        } else {
            writeU29((objectIndex << 1));
        }
    }

    public void writeValue(Object object) throws IOException, NoSerializerExistsException {
        writeValue(object, new HashMap<>());
    }

    public void writeValue(Object object, Map<String, ObjectTypeSerializeHandler> serializers) throws IOException, NoSerializerExistsException {
        writeValue(object, serializers, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    private void writeValue(Object object, Map<String, ObjectTypeSerializeHandler> serializers, List<String> stringTable, List<Traits> traitsTable, List<Object> objectTable) throws IOException, NoSerializerExistsException {
        if (object == BasicType.UNDEFINED) {
            writeU8(Marker.UNDEFINED);
        } else if (object == BasicType.NULL) {
            writeU8(Marker.NULL);
        } else if (object == Boolean.FALSE) {
            writeU8(Marker.FALSE);
        } else if (object == Boolean.TRUE) {
            writeU8(Marker.TRUE);
        } else if (object == BasicType.UNKNOWN) {
            //Nothing
        } else if (object instanceof Long) {
            writeU8(Marker.INTEGER);
            writeU29((Long) object);
        } else if (object instanceof Double) {
            writeU8(Marker.DOUBLE);
            writeDouble((Double) object);
        } else if (object instanceof String) {
            writeU8(Marker.STRING);
            writeUtf8Vr((String) object, stringTable);
        } else if (object instanceof XmlDocType) {
            writeU8(Marker.XML_DOC);
            writeXmlDoc((XmlDocType) object, objectTable);
        } else if (object instanceof DateType) {
            writeU8(Marker.DATE);
            int dateIndex = objectTable.indexOf(object);
            DateType val = (DateType) object;
            if (dateIndex == -1) {
                objectTable.add(val);
                writeU29(NO_REFERENCE_FLAG);
                writeDouble(val.getVal());
            } else {
                writeU29(dateIndex << 1);
            }
        } else if (object instanceof ArrayType) {
            writeU8(Marker.ARRAY);
            writeArray((ArrayType) object, serializers, stringTable, traitsTable, objectTable);
        } else if (object instanceof ObjectType) {
            writeU8(Marker.OBJECT);
            writeObject((ObjectType) object, serializers, stringTable, traitsTable, objectTable);
        } else if (object instanceof XmlType) {
            writeU8(Marker.XML);
            writeXml((XmlType) object, objectTable);
        } else if (object instanceof ByteArrayType) {
            writeU8(Marker.BYTE_ARRAY);
            writeByteArray((ByteArrayType) object, objectTable);
        } else if (object instanceof VectorIntType) {
            writeU8(Marker.VECTOR_INT);

            int vectorIndex = objectTable.indexOf(object);
            VectorIntType val = (VectorIntType) object;
            if (vectorIndex == -1) {
                objectTable.add(val);
                writeU29((val.getValues().size() << 1) | NO_REFERENCE_FLAG);
                writeU8(val.isFixed() ? 1 : 0);
                for (long v : val.getValues()) {
                    writeU32(v);
                }
            } else {
                writeU29(vectorIndex << 1);
            }
        } else if (object instanceof VectorUIntType) {
            writeU8(Marker.VECTOR_UINT);
            int vectorIndex = objectTable.indexOf(object);
            VectorUIntType val = (VectorUIntType) object;
            if (vectorIndex == -1) {
                objectTable.add(val);
                writeU29((val.getValues().size() << 1) | NO_REFERENCE_FLAG);
                writeU8(val.isFixed() ? 1 : 0);
                for (long v : val.getValues()) {
                    writeU32(v);
                }
            } else {
                writeU29(vectorIndex << 1);
            }
        } else if (object instanceof VectorDoubleType) {
            writeU8(Marker.VECTOR_DOUBLE);
            int vectorIndex = objectTable.indexOf(object);
            VectorDoubleType val = (VectorDoubleType) object;
            if (vectorIndex == -1) {
                objectTable.add(val);
                writeU29((val.getValues().size() << 1) | NO_REFERENCE_FLAG);
                writeU8(val.isFixed() ? 1 : 0);
                for (double v : val.getValues()) {
                    writeDouble(v);
                }
            } else {
                writeU29(vectorIndex << 1);
            }
        } else if (object instanceof VectorObjectType) {
            writeU8(Marker.VECTOR_OBJECT);
            int vectorIndex = objectTable.indexOf(object);
            VectorObjectType val = (VectorObjectType) object;
            if (vectorIndex == -1) {
                objectTable.add(val);
                writeU29((val.getValues().size() << 1) | NO_REFERENCE_FLAG);
                writeU8(val.isFixed() ? 1 : 0);
                writeUtf8Vr(val.getTypeName(), stringTable);
                for (Object v : val.getValues()) {
                    writeValue(v, serializers, stringTable, traitsTable, objectTable);
                }
            } else {
                writeU29(vectorIndex << 1);
            }
        } else if (object instanceof DictionaryType) {
            writeU8(Marker.DICTIONARY);
            int dictionaryIndex = objectTable.indexOf(object);
            DictionaryType val = (DictionaryType) object;
            if (dictionaryIndex == -1) {
                objectTable.add(val);
                writeU29((val.size() << 1) | NO_REFERENCE_FLAG);
                writeU8(val.hasWeakKeys() ? 1 : 0);
                for (Object key : val.keySet()) {
                    writeValue(key, serializers, stringTable, traitsTable, objectTable);
                    writeValue(val.get(key), serializers, stringTable, traitsTable, objectTable);
                }
            } else {
                writeU29(dictionaryIndex << 1);
            }
        } else {
            throw new UnsupportedValueTypeException(object.getClass());
        }
    }
}
