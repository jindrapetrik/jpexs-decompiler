/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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

import com.jpexs.decompiler.flash.EndOfStreamException;
import com.jpexs.decompiler.flash.amf.amf0.types.ArrayType;
import com.jpexs.decompiler.flash.amf.amf0.types.BasicType;
import com.jpexs.decompiler.flash.amf.amf0.types.ComplexObject;
import com.jpexs.decompiler.flash.amf.amf0.types.DateType;
import com.jpexs.decompiler.flash.amf.amf0.types.EcmaArrayType;
import com.jpexs.decompiler.flash.amf.amf0.types.ObjectType;
import com.jpexs.decompiler.flash.amf.amf0.types.ReferenceType;
import com.jpexs.decompiler.flash.amf.amf0.types.TypedObjectType;
import com.jpexs.decompiler.flash.amf.amf0.types.XmlDocumentType;
import com.jpexs.decompiler.flash.amf.amf3.Amf3InputStream;
import com.jpexs.decompiler.flash.amf.amf3.NoSerializerExistsException;
import com.jpexs.decompiler.flash.dumpview.DumpInfo;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.exporters.amf.amf0.Amf0Exporter;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.MemoryInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * InputStream for AMF0 data.
 *
 * @author JPEXS
 */
public class Amf0InputStream extends InputStream {

    private final MemoryInputStream is;

    /**
     * Dump info
     */
    public DumpInfo dumpInfo;

    public Amf0InputStream(MemoryInputStream is) {
        this.is = is;
    }

    private int readInternal() throws IOException {
        int ret = read();
        if (ret == -1) {
            throw new EndOfStreamException();
        }
        return ret;
    }

    public byte[] readBytes(int count) throws IOException {
        DataInputStream dais = new DataInputStream(is);
        byte[] ret = new byte[count];
        try {
            dais.readFully(ret);
        } catch (EOFException e) {
            throw new EndOfStreamException();
        }
        return ret;
    }

    @Override
    public int read() throws IOException {
        return is.read();
    }

    @Override
    public int available() throws IOException {
        return is.available();
    }

    /**
     * New dump level.
     *
     * @param name Name
     * @param type Type
     * @return Dump info
     */
    public DumpInfo newDumpLevel(String name, String type) {
        if (dumpInfo != null) {
            long startByte = is.getPos();
            DumpInfo di = new DumpInfo(name, type, null, startByte, 0, 0, 0);
            di.parent = dumpInfo;
            dumpInfo.getChildInfos().add(di);
            dumpInfo = di;
        }

        return dumpInfo;
    }

    /**
     * Ends dump level
     */
    public void endDumpLevel() {
        endDumpLevel(null);
    }

    /**
     * Ends dump level
     *
     * @param value Value
     */
    public void endDumpLevel(Object value) {
        if (dumpInfo != null) {
            dumpInfo.lengthBytes = is.getPos() - dumpInfo.startByte;
            dumpInfo.previewValue = value;
            dumpInfo = dumpInfo.parent;
        }
    }

    /**
     * Ends dump level until
     *
     * @param di Dump info
     */
    public void endDumpLevelUntil(DumpInfo di) {
        if (di != null) {
            while (dumpInfo != null && dumpInfo != di) {
                endDumpLevel();
            }
        }
    }

    /**
     * Reads U8 (unsigned 8-bit integer) value.
     *
     * @param name Name
     * @return U8 value
     * @throws IOException On I/O error
     */
    public int readU8(String name) throws IOException {
        newDumpLevel(name, "U8");
        int ret = readInternal();
        endDumpLevel(ret);
        return ret;
    }

    /**
     * Reads U16 (unsigned 16-bit integer) value.
     *
     * @param name Name
     * @return U16 value
     * @throws IOException On I/O error
     */
    public int readU16(String name) throws IOException {
        newDumpLevel(name, "U16");
        int b1 = readInternal();
        int b2 = readInternal();
        int ret = (b1 << 8) + b2;
        endDumpLevel(ret);
        return ret;
    }

    /**
     * Reads S16 (signed 16-bit integer) value.
     *
     * @param name Name
     * @return S16 value
     * @throws IOException On I/O error
     */
    public int readS16(String name) throws IOException {
        newDumpLevel(name, "S16");
        int b1 = readInternal();
        int b2 = readInternal();
        int ret = (b1 << 8) + b2;
        ret = (int) signExtend(ret, 16);
        endDumpLevel(ret);
        return ret;
    }

    /**
     * Reads U32 (unsigned 32-bit integer) value.
     *
     * @param name Name
     * @return U32 value
     * @throws IOException On I/O error
     */
    public long readU32(String name) throws IOException {
        newDumpLevel(name, "U32");
        long ret = readU32Internal();
        endDumpLevel(ret);
        return ret;
    }

    /**
     * Reads U32 (unsigned 32-bit integer) value.
     *
     * @return U32 value
     * @throws IOException On I/O error
     */
    private long readU32Internal() throws IOException {
        int b1 = readInternal();
        int b2 = readInternal();
        int b3 = readInternal();
        int b4 = readInternal();

        return ((b1 << 24) + (b2 << 16) + (b3 << 8) + b4) & 0xffffffff;
    }

    /**
     * Reads S32 (signed 32-bit integer) value.
     *
     * @param name Name
     * @return S32 value
     * @throws IOException On I/O error
     */
    public long readS32(String name) throws IOException {
        newDumpLevel(name, "S32");
        long ret = signExtend(readU32Internal(), 32);
        endDumpLevel(ret);
        return ret;
    }

    /**
     * Reads long value.
     *
     * @return Long value
     * @throws IOException On I/O error
     */
    private long readLong() throws IOException {
        byte[] readBuffer = new byte[8];
        for (int i = 0; i < 8; i++) {
            readBuffer[i] = (byte) readInternal();
        }
        return (((long) readBuffer[0] << 56)
                + ((long) (readBuffer[1] & 0xff) << 48)
                + ((long) (readBuffer[2] & 0xff) << 40)
                + ((long) (readBuffer[3] & 0xff) << 32)
                + ((long) (readBuffer[4] & 0xff) << 24)
                + ((readBuffer[5] & 0xff) << 16)
                + ((readBuffer[6] & 0xff) << 8)
                + ((readBuffer[7] & 0xff)));
    }

    /**
     * Reads double value.
     *
     * @param name Name
     * @return Double value
     * @throws IOException On I/O error
     */
    public double readDouble(String name) throws IOException {
        newDumpLevel(name, "DOUBLE");
        long lval = readLong();
        double ret = Double.longBitsToDouble(lval);
        endDumpLevel(EcmaScript.toString(ret));
        return ret;
    }

    private long signExtend(long val, int size) {
        if (((val >> (size - 1)) & 1) == 1) { //has sign bit
            long mask = size == 32 ? 0xFFFFFFFF : (1 << size) - 1; // 111111...up to size
            long positiveVal = (~(val - 1)) & mask;
            long negativeVal = -positiveVal;
            return negativeVal;
        }
        return val;
    }

    /**
     * Reads UTF-8 value
     * @param name Name
     * @return UTF-8 value
     * @throws IOException On I/O error
     */
    public String readUtf8(String name) throws IOException {
        newDumpLevel(name, "UTF-8");
        int len = readU16("length");
        byte[] data = len == 0 ? null : readBytes(len);
        String retString = data == null ? "" : new String(data, "UTF-8");
        endDumpLevel("\"" + Helper.escapeActionScriptString(retString) + "\"");
        return retString;
    }

    /**
     * Reads UTF-8-long value
     * @param name Name
     * @return UTF-8-long value
     * @throws IOException On I/O error
     */
    public String readUtf8Long(String name) throws IOException {
        newDumpLevel(name, "UTF-8-long");
        int len = (int) readU32("length"); //TODO: handle lengths that not fit int

        byte[] data = len == 0 ? null : readBytes(len);
        String retString = data == null ? "" : new String(data, "UTF-8");
        endDumpLevel();
        return retString;
    }

    public Object readValueWithReferences(String name) throws IOException, NoSerializerExistsException {
        Object value = readValue(name);
        List<Object> complexObjects = new ArrayList<>();
        populateComplexObjects(value, complexObjects);
        return resolveReferences(value, complexObjects);
    }
    
    /**
     * Reads AMF0 value
     * @param name Name
     * @return AMF0 value
     * @throws IOException On I/O error
     * @throws NoSerializerExistsException When reading is switched to AMF3 and no serializer found for an object
     */
    public Object readValue(String name) throws IOException, NoSerializerExistsException {
        newDumpLevel(name, "value-type");
        Object result = null;
        int marker = readInternal();
        //System.err.println("marker " + Integer.toHexString(marker));
        switch (marker) {
            case Marker.NUMBER:                    
                result = readDouble("DOUBLE");
                break;
            case Marker.BOOLEAN:
                result = readU8("U8") > 0;
                break;
            case Marker.STRING:
                result = readUtf8("UTF-8");
                break;
            case Marker.OBJECT_END:
                result = BasicType.OBJECT_END;
                break;
            case Marker.OBJECT:
                ObjectType object = new ObjectType();
                String propName;
                Object val;

                while (true) {
                    propName = readUtf8("propertyName");
                    val = readValue("propertyValue");
                    if (propName.equals("")) {
                        break;
                    }
                    object.properties.put(propName, val);
                }                    
                result = object;
                break;
            case Marker.MOVIECLIP:
                throw new IllegalArgumentException("MovieClip not supported in AMF0");
            case Marker.NULL:
                result = BasicType.NULL;
                break;
            case Marker.UNDEFINED:
                result = BasicType.UNDEFINED;
                break;
            case Marker.REFERENCE:
                result = new ReferenceType(readU16("referenceIndex"));
                break;
            case Marker.ECMA_ARRAY:
                int associativeCount = (int) readU32("associative-count");
                EcmaArrayType ea = new EcmaArrayType();
                for (int a = 0; a < associativeCount; a++) {
                    String eaKey = readUtf8("key");                        
                    Object eaVal = readValue("value");
                    ea.denseValues.put(eaKey, eaVal);
                }
                while (true) {
                    String eaKey = readUtf8("key");
                    Object eaVal = readValue("value");
                    if ("".equals(eaKey)) {
                        break;
                    }
                    ea.associativeValues.put(eaKey, eaVal);
                }

                result = ea;
                break;
            case Marker.STRICT_ARRAY:
                int arrayCount = (int) readU32("array-count");
                ArrayType at = new ArrayType();
                for (int a = 0; a < arrayCount; a++) {
                    at.values.add(readValue("value"));
                }
                result = at;
                break;
            case Marker.DATE:
                double dval = readDouble("epoch-millis");
                int timezone = readS16("time-zone");
                result = new DateType(dval, timezone);
                break;
            case Marker.LONG_STRING:
                result = readUtf8Long("long-string");
                break;
            case Marker.UNSUPPORTED:
                throw new IllegalArgumentException("Unsupported type");
            case Marker.RECORDSET:
                throw new IllegalArgumentException("RecordSet not supported in AMF0");
            case Marker.XML_DOCUMENT:
                return new XmlDocumentType(readUtf8Long("xml"));
            case Marker.TYPED_OBJECT:
                String className = readUtf8("class-name");
                TypedObjectType typedObject = new TypedObjectType();
                typedObject.className = className;

                while (true) {
                    propName = readUtf8("propertyName");                            
                    val = readValue("propertyValue");
                    if (propName.equals("")) {
                        break;
                    }
                    typedObject.properties.put(propName, val);
                }
                result = typedObject;
                break;
            case Marker.AVMPLUS_OBJECT:
                Amf3InputStream amf3 = new Amf3InputStream(is);
                result = amf3.readValue("avm-plus-object");                    
                break;
            default:
                throw new IllegalArgumentException("Unsupported type");
        }
        
        /*if (result != null) {
            System.err.println("Read: " + Amf0Exporter.amfToString(result, 0, "\r\n", new ArrayList<>(), new HashMap<>(), new HashMap<>()));
        }*/
        endDumpLevel();
        return result;
    }
    
    public void resolveMapReferences(Map<String, Object> map) {        
        List<Object> complexObjects = new ArrayList<>();
        populateComplexObjects((List<Object>)new ArrayList<>(map.values()), complexObjects);
        
        for (String key : map.keySet()) {
            map.put(key, resolveReferences(map.get(key), complexObjects));            
        }        
    }
    
    public Object resolveReferences(Object value, List<Object> complexObjects) {
        if (value instanceof ReferenceType) {
            ReferenceType rt = (ReferenceType) value;
            return complexObjects.get(rt.referenceIndex);
        }
        if (value instanceof ObjectType) {
            ObjectType ot = (ObjectType) value;
            for (String key : ot.properties.keySet()) {
                ot.properties.put(key, resolveReferences(ot.properties.get(key), complexObjects));                
            }
        }
        if (value instanceof TypedObjectType) {
            TypedObjectType tot = (TypedObjectType) value;
            for (String key : tot.properties.keySet()) {
                tot.properties.put(key, resolveReferences(tot.properties.get(key), complexObjects));                
            }
        }
        if (value instanceof EcmaArrayType) {
            EcmaArrayType eat = (EcmaArrayType) value;
            for (String key : eat.denseValues.keySet()) {
                eat.denseValues.put(key, resolveReferences(eat.denseValues.get(key), complexObjects));                
            }
            for (String key : eat.associativeValues.keySet()) {
                eat.associativeValues.put(key, resolveReferences(eat.associativeValues.get(key), complexObjects));
            }
        }
        if (value instanceof ArrayType) {
            ArrayType at = (ArrayType) value;
            for (int i = 0; i < at.values.size(); i++) {
                at.values.set(i, resolveReferences(at.values.get(i), complexObjects));
            }
        }
        return value;
    }
    
    public void populateComplexObjects(List<Object> values, List<Object> result) {
        for (Object value : values) {
            populateComplexObjects(value, result);
        }
    }
    
    public void populateComplexObjects(Object value, List<Object> result) {
        if (result.contains(value)) {
            return;
        }
        if (value instanceof ComplexObject) {
            result.add(value);
            for (Object subvalue : ((ComplexObject) value).getSubValues()) {
                populateComplexObjects(subvalue, result);
            }
        }
    }

    @Override
    public long skip(long n) throws IOException {
        return is.skip(n);
    }        
}
