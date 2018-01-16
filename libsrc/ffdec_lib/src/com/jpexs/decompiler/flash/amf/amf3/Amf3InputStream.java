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

import com.jpexs.decompiler.flash.EndOfStreamException;
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
import com.jpexs.decompiler.flash.dumpview.DumpInfo;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.MemoryInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Amf3InputStream extends InputStream {

    public final static Logger LOGGER = Logger.getLogger(Amf3InputStream.class.getName());
    private final MemoryInputStream is;
    public DumpInfo dumpInfo;
    private static final String NO_REFERENCE_BIT_TEXT = "not reference";
    private static final String OBJECT_INDEX_TEXT = "object index";
    private static final String STRING_INDEX_TEXT = "string index";
    private static final String TRAIT_INDEX_TEXT = "trait index";

    public Amf3InputStream(MemoryInputStream is) {
        this.is = is;
    }

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

    public void endDumpLevel() {
        endDumpLevel(null);
    }

    public void endDumpLevel(Object value) {
        if (dumpInfo != null) {
            dumpInfo.lengthBytes = is.getPos() - dumpInfo.startByte;
            dumpInfo.previewValue = value;
            dumpInfo = dumpInfo.parent;
        }
    }

    public void endDumpLevelUntil(DumpInfo di) {
        if (di != null) {
            while (dumpInfo != null && dumpInfo != di) {
                endDumpLevel();
            }
        }
    }

    public int readU8(String name) throws IOException {
        newDumpLevel(name, "U8");
        int ret = readInternal();
        endDumpLevel(ret);
        return ret;
    }

    public int readU16(String name) throws IOException {
        newDumpLevel(name, "U16");
        int b1 = readInternal();
        int b2 = readInternal();
        int ret = (b1 << 8) + b2;
        endDumpLevel(ret);
        return ret;
    }

    public long readU32(String name) throws IOException {
        newDumpLevel(name, "U32");
        long ret = readU32Internal();
        endDumpLevel(ret);
        return ret;
    }

    private long readU32Internal() throws IOException {
        int b1 = readInternal();
        int b2 = readInternal();
        int b3 = readInternal();
        int b4 = readInternal();

        return ((b1 << 24) + (b2 << 16) + (b3 << 8) + b4) & 0xffffffff;
    }

    public long readS32(String name) throws IOException {
        newDumpLevel(name, "S32");
        long ret = signExtend(readU32Internal(), 32);
        endDumpLevel(ret);
        return ret;
    }

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

    public double readDouble(String name) throws IOException {
        newDumpLevel(name, "DOUBLE");
        long lval = readLong();
        double ret = Double.longBitsToDouble(lval);
        endDumpLevel(EcmaScript.toString(ret));
        return ret;
    }

    public long readU29(String name) throws IOException {
        newDumpLevel(name, "U29");
        long val = readU29Internal();
        endDumpLevel(val);
        return val;
    }

    private void renameU29O_ref() {
        renameU29("U29O-ref", NO_REFERENCE_BIT_TEXT, OBJECT_INDEX_TEXT);
    }

    private void renameU29S_ref() {
        renameU29("U29S-ref", NO_REFERENCE_BIT_TEXT, STRING_INDEX_TEXT);
    }

    private void renameU29Traits_ref() {
        renameU29("U29O-traits-ref", NO_REFERENCE_BIT_TEXT, "trait reference", TRAIT_INDEX_TEXT);
    }

    private void renameLastDump(String wholeName) {
        if (dumpInfo != null) {
            if (!dumpInfo.getChildInfos().isEmpty()) {
                DumpInfo u29DumpInfo = dumpInfo.getChildInfos().get(dumpInfo.getChildInfos().size() - 1);
                u29DumpInfo.name = wholeName;
            }
        }
    }

    private void setDumpInfoType(String type) {
        if (dumpInfo != null) {
            dumpInfo.type = type;
        }
    }

    private void renameU29(String wholeName, String name1, String... names) {
        List<String> bitNames = new ArrayList<>();
        bitNames.add(name1);
        bitNames.addAll(Arrays.asList(names));

        String restName = bitNames.remove(bitNames.size() - 1);

        if (bitNames.size() > 6) {
            throw new RuntimeException("Renaming more than 6 bits in U29 is not supported");
        }

        if (dumpInfo != null) {
            if (!dumpInfo.getChildInfos().isEmpty()) {
                DumpInfo u29DumpInfo = dumpInfo.getChildInfos().get(dumpInfo.getChildInfos().size() - 1);
                u29DumpInfo.name = wholeName;
                long lastBytePos = u29DumpInfo.startByte + u29DumpInfo.lengthBytes - 1;   //last byte of U29 is least significant (U29 is big endian)

                int remainingBitLength = ((int) u29DumpInfo.lengthBytes) * 8 - bitNames.size();
                long u29val = ((long) (Long) u29DumpInfo.previewValue);

                DumpInfo restDumpInfo = new DumpInfo(restName, "UB(" + remainingBitLength + ")", u29val >> bitNames.size(), lastBytePos, 0, u29DumpInfo.lengthBytes, remainingBitLength);
                restDumpInfo.parent = u29DumpInfo;
                u29DumpInfo.getChildInfos().add(restDumpInfo);

                for (int i = bitNames.size() - 1; i >= 0; i--) {
                    int bitVal = (int) ((u29val >> i) & 1);
                    DumpInfo bitDumpInfo = new DumpInfo(bitNames.get(i), "bit", bitVal, lastBytePos, 7 - i, 1, 1);
                    bitDumpInfo.parent = u29DumpInfo;
                    u29DumpInfo.getChildInfos().add(bitDumpInfo);
                }

            }
        }
    }

    public long readS29(String name) throws IOException {
        newDumpLevel(name, "S29");
        long val = signExtend(readU29Internal(), 29);
        endDumpLevel(val);
        return val;
    }

    public long readU29Internal() throws IOException {
        long val = 0;
        for (int i = 1; i <= 4; i++) {
            int b = readInternal();
            if (i == 4) {
                val = ((val << 8) + b);
            } else {
                val = (val << 7) + (b & 0x7F);
                if ((b & 0x80) != 0x80) {
                    break;
                }
            }
        }
        return val;
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

    private String readUtf8Char(String name, long byteLength) throws IOException {
        if (byteLength == 0) {
            return "";
        }
        newDumpLevel(name, "UTF8-char");

        byte[] buf = new byte[(int) byteLength]; //how about long strings(?), will the int length be enough?
        int cnt = is.read(buf);
        if (cnt < buf.length) {
            throw new EndOfStreamException();
        }
        String retString = new String(buf, "UTF-8");
        endDumpLevel("\"" + Helper.escapeActionScriptString(retString) + "\"");
        return retString;
    }

    public String readUtf8Vr(String name, List<String> stringTable) throws IOException {
        newDumpLevel(name, "UTF-8-vr");
        long u = readU29("U29S");
        int stringNoRefFlag = (int) (u & 1);
        String retString;
        if (stringNoRefFlag == 1) {
            renameU29("U29S-value", NO_REFERENCE_BIT_TEXT, "byte length");
            long byteLength = u >> 1; //TODO: long strings, int is not enough for them
            retString = readUtf8Char("characters", byteLength);
            if (byteLength > 0) {
                stringTable.add(retString);
            }
            LOGGER.log(Level.FINE, "Read string: \"{0}\"", retString);
        } else { //flag==0
            renameU29S_ref();
            int stringRefTableIndex = (int) (u >> 1);

            retString = stringTable.get(stringRefTableIndex);
            LOGGER.log(Level.FINE, "Read string: reference({0}):" + retString, stringRefTableIndex);

        }
        endDumpLevel("\"" + Helper.escapeActionScriptString(retString) + "\"");
        return retString;

    }

    private int readInternal() throws IOException {
        int ret = read();
        if (ret == -1) {
            throw new EndOfStreamException();
        }
        return ret;
    }

    @Override
    public int read() throws IOException {
        return is.read();
    }

    public Object readValue(String name) throws IOException, NoSerializerExistsException {
        return readValue(name, new HashMap<>());
    }

    public Object readValue(String name, Map<String, ObjectTypeSerializeHandler> serializers) throws IOException, NoSerializerExistsException {
        return readValue(name, serializers, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    private Object readValue(String name, Map<String, ObjectTypeSerializeHandler> serializers,
            List<Object> objectTable,
            List<Traits> traitsTable,
            List<String> stringTable
    ) throws IOException, NoSerializerExistsException {
        newDumpLevel(name, "value-type");
        Object result;

        try {
            int marker = readU8("marker");
            markerswitch:
            switch (marker) {
                case Marker.UNDEFINED:
                    renameLastDump("undefined-marker");
                    setDumpInfoType("undefined-type");
                    LOGGER.log(Level.FINE, "Read value: undefined");
                    result = BasicType.UNDEFINED;
                    break;
                case Marker.NULL:
                    renameLastDump("null-marker");
                    setDumpInfoType("null-type");
                    LOGGER.log(Level.FINE, "Read value: null");
                    result = BasicType.NULL;
                    break;
                case Marker.FALSE:
                    renameLastDump("false-marker");
                    setDumpInfoType("false-type");
                    LOGGER.log(Level.FINE, "Read value: false");
                    result = Boolean.FALSE;
                    break;
                case Marker.TRUE:
                    renameLastDump("true-marker");
                    setDumpInfoType("true-type");
                    LOGGER.log(Level.FINE, "Read value: true");
                    result = Boolean.TRUE;
                    break;
                case Marker.INTEGER:
                    renameLastDump("integer-marker");
                    setDumpInfoType("integer-type");
                    LOGGER.log(Level.FINE, "Read value: integer");
                    long ival = readS29("intValue");
                    LOGGER.log(Level.FINER, "Integer value: {0}", ival);
                    result = ival;
                    break;
                case Marker.DOUBLE:
                    renameLastDump("double-marker");
                    setDumpInfoType("double-type");
                    LOGGER.log(Level.FINE, "Read value: double");
                    double dval = readDouble("doubleValue");
                    LOGGER.log(Level.FINER, "Double value: {0}", "" + dval);
                    result = dval;
                    break;
                case Marker.STRING:
                    renameLastDump("string-marker");
                    setDumpInfoType("string-type");
                    LOGGER.log(Level.FINE, "Read value: string");
                    String sval = readUtf8Vr("stringValue", stringTable);
                    LOGGER.log(Level.FINER, "String value: {0}", sval);
                    result = sval;
                    break;
                case Marker.XML_DOC:
                    renameLastDump("xml-doc-marker");
                    setDumpInfoType("xml-doc-type");
                    LOGGER.log(Level.FINE, "Read value: xml_doc");
                    long xmlDocU29 = readU29("U29");
                    int xmlDocNoRefFlag = (int) (xmlDocU29 & 1);
                    if (xmlDocNoRefFlag == 1) {
                        renameU29("U29X-value", NO_REFERENCE_BIT_TEXT, "byte length");
                        long byteLength = xmlDocU29 >> 1;
                        String xval = readUtf8Char("characters", byteLength);
                        LOGGER.log(Level.FINER, "XmlDoc value: {0}", xval);
                        XmlDocType retXmlDoc = new XmlDocType(xval);
                        objectTable.add(retXmlDoc);
                        result = retXmlDoc;
                    } else {
                        renameU29O_ref();
                        int refIndexXmlDoc = (int) (xmlDocU29 >> 1);
                        LOGGER.log(Level.FINER, "XmlDoc value: reference({0})", refIndexXmlDoc);
                        result = objectTable.get(refIndexXmlDoc);    //What if it's not XmlRef?
                    }
                    break;
                case Marker.DATE:
                    renameLastDump("date-marker");
                    setDumpInfoType("date-type");
                    LOGGER.log(Level.FINE, "Read value: date");
                    long dateU29 = readU29("U29");
                    int dateNoRefFlag = (int) (dateU29 & 1);
                    if (dateNoRefFlag == 1) {
                        renameU29("U29D-value", NO_REFERENCE_BIT_TEXT, "unused");
                        //remaining bits of dateU29 are not used
                        double dtval = readDouble("date-time");
                        DateType retDate = new DateType(dtval);
                        LOGGER.log(Level.FINER, "Date value: {0}", retDate);
                        objectTable.add(retDate);
                        result = retDate;
                    } else {
                        renameU29O_ref();
                        int refIndexDate = (int) (dateU29 >> 1);
                        LOGGER.log(Level.FINER, "Date value: reference({0})", refIndexDate);
                        result = objectTable.get(refIndexDate);   //What if it's not Date?
                    }
                    break;
                case Marker.ARRAY:
                    renameLastDump("array-marker");
                    setDumpInfoType("array-type");
                    LOGGER.log(Level.FINE, "Read value: array");
                    long arrayU29 = readU29("U29");
                    int arrayNoRefFlag = (int) (arrayU29 & 1);
                    if (arrayNoRefFlag == 1) {
                        renameU29("U29A-value", NO_REFERENCE_BIT_TEXT, "dense count");
                        int denseCount = (int) (arrayU29 >> 1);
                        LOGGER.log(Level.FINEST, "Array value: denseCount={0}", new Object[]{denseCount});
                        Map<String, Object> assocPart = new ListMap<>();
                        List<Object> densePart = new ArrayList<>();
                        ArrayType retArray = new ArrayType();
                        objectTable.add(retArray); //add before processing  elements which may reference this
                        newDumpLevel("associativeValues", "assoc-value");
                        while (true) {
                            String key = readUtf8Vr("key", stringTable);
                            if (key.isEmpty()) {
                                renameLastDump("UTF-8-empty");
                                break;
                            } else {
                                try {
                                    Object val = readValue("value", serializers, objectTable, traitsTable, stringTable);
                                    assocPart.put(key, val);
                                } catch (NoSerializerExistsException nse) {
                                    assocPart.put(key, nse.getIncompleteData());
                                    retArray.setAssociativeValues(assocPart);
                                    throw new NoSerializerExistsException(nse.getClassName(), retArray, nse);
                                }
                            }
                        }
                        retArray.setAssociativeValues(assocPart);
                        endDumpLevel();
                        LOGGER.log(Level.FINEST, "Array value: assocSize={0}", new Object[]{assocPart.size()});

                        newDumpLevel("denseValues", "value-type[]");
                        for (int i = 0; i < denseCount; i++) {
                            try {
                                densePart.add(readValue("denseValue", serializers, objectTable, traitsTable, stringTable));
                            } catch (NoSerializerExistsException nse) {
                                densePart.add(nse.getIncompleteData());
                                for (int j = i + 1; j < denseCount; j++) {
                                    densePart.add(BasicType.UNKNOWN);
                                }
                                retArray.setDenseValues(densePart);
                                throw new NoSerializerExistsException(nse.getClassName(), retArray, nse);
                            }
                        }
                        retArray.setDenseValues(densePart);

                        endDumpLevel();
                        LOGGER.log(Level.FINER, "Array value: dense_size={0},assocSize={1}", new Object[]{densePart.size(), assocPart.size()});
                        result = retArray;

                    } else {
                        renameU29O_ref();
                        int refIndexArray = (int) (arrayU29 >> 1);
                        LOGGER.log(Level.FINER, "Array value: reference({0})", refIndexArray);
                        result = objectTable.get(refIndexArray);   //What if it's not Array?
                    }
                    break;

                case Marker.OBJECT:
                    renameLastDump("object-marker");
                    setDumpInfoType("object-type");
                    LOGGER.log(Level.FINE, "Read value: object");
                    long objectU29 = readU29("U29");
                    int objectNoRefFlag = (int) (objectU29 & 1);
                    if (objectNoRefFlag == 1) {
                        Traits traits;
                        int objectTraitsNoRefFlag = (int) ((objectU29 >> 1) & 1);
                        if (objectTraitsNoRefFlag == 1) {
                            int objectTraitsExtFlag = (int) ((objectU29 >> 2) & 1);
                            ObjectType retObjectType;
                            if (objectTraitsExtFlag == 1) {
                                renameU29("U29O-traits-ext", NO_REFERENCE_BIT_TEXT, "not trait reference", "externalized traits", "unused");
                                String className = readUtf8Vr("className", stringTable);
                                if (!serializers.containsKey(className)) {
                                    throw new NoSerializerExistsException(className, new ObjectType(new Traits(className, false, new ArrayList<>()), (byte[]) null, new HashMap<>()), null);
                                }
                                newDumpLevel("serializedData", "U8[]");
                                MonitoredInputStream mis = new MonitoredInputStream(is);
                                Map<String, Object> serMembers = serializers.get(className).readObject(className, mis);
                                byte[] serData = mis.getReadData();
                                endDumpLevel();
                                Traits unserTraits = new Traits(className, false, new ArrayList<>());
                                retObjectType = new ObjectType(unserTraits, serData, serMembers);

                                LOGGER.log(Level.FINER, "Object/Traits value: customSerialized");
                                objectTable.add(retObjectType);
                                result = retObjectType;
                                break markerswitch;
                            } else {
                                renameU29("U29O-traits", NO_REFERENCE_BIT_TEXT, "not trait reference", "externalized traits", "dynamic", "sealed count");
                                int dynamicFlag = (int) ((objectU29 >> 3) & 1);
                                int numSealed = (int) (objectU29 >> 4);
                                LOGGER.log(Level.FINEST, "object dynamicFlag:{0}", dynamicFlag);
                                LOGGER.log(Level.FINEST, "object numSealed:{0}", numSealed);
                                String className = readUtf8Vr("className", stringTable);
                                LOGGER.log(Level.FINEST, "object className:{0}", className);
                                List<String> sealedMemberNames = new ArrayList<>();
                                if (numSealed > 0) {
                                    newDumpLevel("sealedMemberNames", "UTF-8-vr[]");

                                    for (int i = 0; i < numSealed; i++) {
                                        sealedMemberNames.add(readUtf8Vr("sealedMemberName", stringTable));
                                    }
                                    endDumpLevel();
                                }
                                traits = new Traits(className, dynamicFlag == 1, sealedMemberNames);
                                traitsTable.add(traits);
                            }
                        } else {
                            renameU29Traits_ref();
                            int refIndexTraits = (int) (objectU29 >> 2);
                            traits = traitsTable.get(refIndexTraits);
                            LOGGER.log(Level.FINER, "Traits value: reference({0}) - traitsize={1}", new Object[]{refIndexTraits, traits.getSealedMemberNames().size()});
                        }
                        Map<String, Object> sealedMembers = new ListMap<>();
                        Map<String, Object> dynamicMembers = new ListMap<>();

                        ObjectType retObjectType = new ObjectType(traits);
                        objectTable.add(retObjectType); //add it before any subvalue can reference it
                        List<Object> sealedMemberValues = new ArrayList<>();
                        NoSerializerExistsException error = null;
                        if (!traits.getSealedMemberNames().isEmpty()) {
                            newDumpLevel("sealedMemberValues", "value-type[]");
                            for (int i = 0; i < traits.getSealedMemberNames().size(); i++) {
                                try {
                                    sealedMemberValues.add(readValue("sealedMemberValue", serializers, objectTable, traitsTable, stringTable));
                                } catch (NoSerializerExistsException nse) {
                                    sealedMemberValues.add(nse.getIncompleteData());
                                    for (int j = i + 1; j < traits.getSealedMemberNames().size(); j++) {
                                        sealedMemberValues.add(BasicType.UNKNOWN);
                                    }
                                    error = nse;
                                    break;
                                }
                            }
                            endDumpLevel();
                        }

                        List<String> memberNames = new ArrayList<>();
                        memberNames.addAll(traits.getSealedMemberNames());  //Assuming it is ListSet so maintains order
                        for (int i = 0; i < memberNames.size(); i++) {
                            sealedMembers.put(memberNames.get(i), sealedMemberValues.get(i));
                        }
                        retObjectType.setSealedMembers(sealedMembers);
                        if (traits.isDynamic()) {
                            newDumpLevel("dynamicMembers", "dynamic-member[]");
                            String dynamicMemberName;
                            while (!(dynamicMemberName = readUtf8Vr("name", stringTable)).isEmpty()) {
                                try {
                                    Object dynamicMemberValue = readValue("value", serializers, objectTable, traitsTable, stringTable);
                                    dynamicMembers.put(dynamicMemberName, dynamicMemberValue);
                                } catch (NoSerializerExistsException nse) {
                                    dynamicMembers.put(dynamicMemberName, nse.getIncompleteData());
                                    retObjectType.setDynamicMembers(dynamicMembers);
                                    throw new NoSerializerExistsException(nse.getClassName(), retObjectType, nse);
                                } finally {
                                    //group dumpInfo to one sub "dynamic-member"
                                    if (dumpInfo != null) {
                                        DumpInfo valueDumpInfo = dumpInfo.getChildInfos().remove(dumpInfo.getChildInfos().size() - 1);
                                        DumpInfo nameDumpInfo = dumpInfo.getChildInfos().remove(dumpInfo.getChildInfos().size() - 1);
                                        DumpInfo memberDumpInfo = new DumpInfo("member", "dynamic-member", "", nameDumpInfo.startByte, nameDumpInfo.lengthBytes + valueDumpInfo.lengthBytes);
                                        memberDumpInfo.getChildInfos().add(nameDumpInfo);
                                        memberDumpInfo.getChildInfos().add(valueDumpInfo);
                                        memberDumpInfo.parent = dumpInfo;
                                        nameDumpInfo.parent = memberDumpInfo;
                                        valueDumpInfo.parent = memberDumpInfo;
                                        memberDumpInfo.previewValue = "" + nameDumpInfo.previewValue + (valueDumpInfo.previewValue != null ? " : " + valueDumpInfo.previewValue : "");
                                        dumpInfo.getChildInfos().add(memberDumpInfo);
                                    }
                                }
                            }
                            retObjectType.setDynamicMembers(dynamicMembers);
                            renameLastDump("UTF-8-empty");
                            endDumpLevel();
                        }

                        LOGGER.log(Level.FINER, "Object value: dynamic={0},className={1},sealedSize={2},dynamicSize={3}", new Object[]{traits.isDynamic(), traits.getClassName(), sealedMembers.size(), dynamicMembers.size()});
                        result = retObjectType;
                    } else {
                        renameU29O_ref();
                        int refIndexObject = (int) (objectU29 >> 1);
                        LOGGER.log(Level.FINER, "Object value: reference({0})", refIndexObject);
                        result = objectTable.get(refIndexObject);
                    }
                    break;
                case Marker.XML:
                    renameLastDump("xml-marker");
                    setDumpInfoType("xml-type");
                    LOGGER.log(Level.FINE, "Read value: xml");
                    long xmlU29 = readU29("U29");
                    int xmlNoRefFlag = (int) (xmlU29 & 1);
                    if (xmlNoRefFlag == 1) {
                        renameU29("U29X-value", NO_REFERENCE_BIT_TEXT, "byte length");
                        long byteLength = (xmlU29 >> 1);
                        String xString = readUtf8Char("characters", byteLength);
                        XmlType retXmlType = new XmlType(xString);
                        LOGGER.log(Level.FINER, "Xml value: {0}", xString);
                        objectTable.add(retXmlType);
                        result = retXmlType;
                    } else {
                        renameU29O_ref();
                        int refIndexXml = (int) (xmlU29 >> 1);
                        LOGGER.log(Level.FINER, "XML value: reference({0})", refIndexXml);
                        result = objectTable.get(refIndexXml);
                    }
                    break;
                case Marker.BYTE_ARRAY:
                    renameLastDump("byte-array-marker");
                    setDumpInfoType("bytearray-type");
                    LOGGER.log(Level.FINE, "Read value: bytearray");
                    long byteArrayU29 = readU29("U29");
                    int byteArrayNoRefFlag = (int) (byteArrayU29 & 1);
                    if (byteArrayNoRefFlag == 1) {
                        renameU29("U29B-value", NO_REFERENCE_BIT_TEXT, "byte array length");
                        int byteArrayLength = (int) (byteArrayU29 >> 1);
                        newDumpLevel("bytes", "U8[]");
                        byte[] byteArrayBuf = new byte[byteArrayLength];
                        if (is.read(byteArrayBuf) != byteArrayLength) {
                            throw new EndOfStreamException();
                        }
                        endDumpLevel();

                        LOGGER.log(Level.FINER, "ByteArray value: bytes[{0}]", byteArrayLength);
                        ByteArrayType retByteArrayType = new ByteArrayType(byteArrayBuf);
                        objectTable.add(retByteArrayType);
                        result = retByteArrayType;
                    } else {
                        renameU29O_ref();
                        int refIndexByteArray = (int) (byteArrayU29 >> 1);
                        LOGGER.log(Level.FINER, "ByteArray value: reference({0})", refIndexByteArray);
                        result = objectTable.get(refIndexByteArray);
                    }
                    break;
                case Marker.VECTOR_INT:
                    renameLastDump("vector-int-marker");
                    setDumpInfoType("vector-int-type");
                    LOGGER.log(Level.FINE, "Read value: vector_int");
                    long vectorIntU29 = readU29("U29");
                    int vectorIntNoRefFlag = (int) (vectorIntU29 & 1);
                    if (vectorIntNoRefFlag == 1) {
                        renameU29("U29V-value", NO_REFERENCE_BIT_TEXT, "item count");
                        int vectorIntCountItems = (int) (vectorIntU29 >> 1);
                        int fixed = readU8("fixed");
                        List<Long> vals = new ArrayList<>();
                        newDumpLevel("items", "S32[]");
                        for (int i = 0; i < vectorIntCountItems; i++) {
                            vals.add(readS32("intValue"));
                        }
                        endDumpLevel();
                        VectorIntType retVectorInt = new VectorIntType(fixed == 1, vals);
                        LOGGER.log(Level.FINER, "Vector<int> value: fixed={0}, size={1}]", new Object[]{fixed, vectorIntCountItems});
                        objectTable.add(retVectorInt);
                        result = retVectorInt;
                    } else {
                        renameU29O_ref();
                        int refIndexVectorInt = (int) (vectorIntU29 >> 1);
                        LOGGER.log(Level.FINER, "Vector<int> value: reference({0})", refIndexVectorInt);
                        result = objectTable.get(refIndexVectorInt);
                    }
                    break;
                case Marker.VECTOR_UINT:
                    renameLastDump("vector-uint-marker");
                    setDumpInfoType("vector-uint-type");

                    LOGGER.log(Level.FINE, "Read value: vector_uint");
                    long vectorUIntU29 = readU29("U29");
                    int vectorUIntNoRefFlag = (int) (vectorUIntU29 & 1);
                    if (vectorUIntNoRefFlag == 1) {
                        renameU29("U29V-value", NO_REFERENCE_BIT_TEXT, "item count");
                        int vectorUIntCountItems = (int) (vectorUIntU29 >> 1);
                        int fixed = readU8("fixed");
                        List<Long> vals = new ArrayList<>();
                        newDumpLevel("items", "U32[]");
                        for (int i = 0; i < vectorUIntCountItems; i++) {
                            vals.add(readU32("uintValue"));
                        }
                        endDumpLevel();
                        VectorUIntType retVectorUInt = new VectorUIntType(fixed == 1, vals);
                        LOGGER.log(Level.FINER, "Vector<uint> value: fixed={0}, size={1}]", new Object[]{fixed, vectorUIntCountItems});
                        objectTable.add(retVectorUInt);
                        result = retVectorUInt;
                    } else {
                        renameU29O_ref();
                        int refIndexVectorUInt = (int) (vectorUIntU29 >> 1);
                        LOGGER.log(Level.FINER, "Vector<uint> value: reference({0})", refIndexVectorUInt);
                        result = objectTable.get(refIndexVectorUInt);
                    }
                    break;
                case Marker.VECTOR_DOUBLE:
                    renameLastDump("vector-double-marker");
                    setDumpInfoType("vector-double-type");

                    LOGGER.log(Level.FINE, "Read value: vector_double");
                    long vectorDoubleU29 = readU29("U29");
                    int vectorDoubleNoRefFlag = (int) (vectorDoubleU29 & 1);
                    if (vectorDoubleNoRefFlag == 1) {
                        renameU29("U29V-value", NO_REFERENCE_BIT_TEXT, "item count");
                        int vectorDoubleCountItems = (int) (vectorDoubleU29 >> 1);
                        int fixed = readU8("fixed");
                        List<Double> vals = new ArrayList<>();
                        newDumpLevel("items", "DOUBLE[]");
                        for (int i = 0; i < vectorDoubleCountItems; i++) {
                            vals.add(readDouble("doubleValue"));
                        }
                        endDumpLevel();
                        VectorDoubleType retVectorDouble = new VectorDoubleType(fixed == 1, vals);
                        LOGGER.log(Level.FINER, "Vector<double> value: fixed={0}, size={1}]", new Object[]{fixed, vectorDoubleCountItems});
                        objectTable.add(retVectorDouble);
                        result = retVectorDouble;
                    } else {
                        renameU29O_ref();
                        int refIndexVectorDouble = (int) (vectorDoubleU29 >> 1);
                        LOGGER.log(Level.FINER, "Vector<double> value: reference({0})", refIndexVectorDouble);
                        result = objectTable.get(refIndexVectorDouble);
                    }
                    break;
                case Marker.VECTOR_OBJECT:
                    renameLastDump("vector-object-marker");
                    setDumpInfoType("vector-object-type");

                    LOGGER.log(Level.FINE, "Read value: vector_object");
                    long vectorObjectU29 = readU29("U29");
                    int vectorObjectNoRefFlag = (int) (vectorObjectU29 & 1);
                    if (vectorObjectNoRefFlag == 1) {
                        renameU29("U29V-value", NO_REFERENCE_BIT_TEXT, "item count");
                        int vectorObjectCountItems = (int) (vectorObjectU29 >> 1);
                        int fixed = readU8("fixed");
                        String objectTypeName = readUtf8Vr("object-type-name", stringTable); //uses "*" for any type
                        List<Object> vals = new ArrayList<>();
                        NoSerializerExistsException error = null;
                        newDumpLevel("items", "value_type[]");
                        for (int i = 0; i < vectorObjectCountItems; i++) {
                            try {
                                vals.add(readValue("value", serializers, objectTable, traitsTable, stringTable));
                            } catch (NoSerializerExistsException nse) {
                                vals.add(nse.getIncompleteData());
                                for (int j = i + 1; j < vectorObjectCountItems; j++) {
                                    vals.add(BasicType.UNKNOWN);
                                }
                                error = nse;
                                break;
                            }
                        }
                        endDumpLevel();
                        VectorObjectType retVectorObject = new VectorObjectType(fixed == 1, objectTypeName, vals);
                        LOGGER.log(Level.FINER, "Vector<Object> value: fixed={0}, size={1}, typeName:{2}]", new Object[]{fixed, vectorObjectCountItems, objectTypeName});
                        objectTable.add(retVectorObject);
                        if (error != null) {
                            throw new NoSerializerExistsException(error.getClassName(), retVectorObject, error);
                        }
                        result = retVectorObject;
                    } else {
                        renameU29O_ref();

                        int refIndexVectorObject = (int) (vectorObjectU29 >> 1);
                        LOGGER.log(Level.FINER, "Vector<Object> value: reference({0})", refIndexVectorObject);
                        result = objectTable.get(refIndexVectorObject);
                    }
                    break;
                case Marker.DICTIONARY:
                    renameLastDump("dictionary-marker");
                    setDumpInfoType("dictionary-type");

                    long dictionaryObjectU29 = readU29("U29");
                    int dictionaryNoRefFlag = (int) (dictionaryObjectU29 & 1);
                    if (dictionaryNoRefFlag == 1) {
                        renameU29("U29Dict-value", NO_REFERENCE_BIT_TEXT, "entries count");
                        int numEntries = (int) (dictionaryObjectU29 >> 1);
                        int weakKeys = readU8("weak keys");
                        Map<Object, Object> data = new ListMap<>(true);
                        DictionaryType retDictionary = new DictionaryType(weakKeys == 1);
                        objectTable.add(retDictionary);
                        NoSerializerExistsException error = null;
                        newDumpLevel("entries", "");
                        for (int i = 0; i < numEntries; i++) {
                            Object key;
                            Object val;
                            try {
                                key = readValue("entry-key", serializers, objectTable, traitsTable, stringTable);
                                try {
                                    val = readValue("entry-value", serializers, objectTable, traitsTable, stringTable);
                                } catch (NoSerializerExistsException nse) {
                                    error = nse;
                                    val = BasicType.UNKNOWN;
                                }
                            } catch (NoSerializerExistsException nse) {
                                error = nse;
                                key = BasicType.UNKNOWN;
                                val = BasicType.UNKNOWN;
                            }

                            retDictionary.put(key, val);
                            if (error != null) {
                                for (int j = i + 1; j < numEntries; j++) {
                                    retDictionary.put(BasicType.UNKNOWN, BasicType.UNKNOWN);
                                }
                                break;
                            }
                        }
                        endDumpLevel();
                        if (error != null) {
                            throw new NoSerializerExistsException(error.getClassName(), retDictionary, error);
                        }
                        result = retDictionary;
                    } else {
                        renameU29O_ref();
                        int refIndexDictionary = (int) (dictionaryObjectU29 >> 1);
                        LOGGER.log(Level.FINER, "Dictionary value: reference({0})", refIndexDictionary);
                        result = objectTable.get(refIndexDictionary);
                    }
                    break;
                default:
                    throw new UnsupportedValueTypeException(marker);
            }
        } finally {
            endDumpLevel();
        }
        return result;
    }

    private static String valToPreviewString(Object v) {
        if (v instanceof ObjectType) {
            return "{...}";
        }
        if (v instanceof ArrayType) {
            return "[...]";
        }
        if (v instanceof DictionaryType) {
            return "";
        }
        if (v instanceof BasicType) {
            return "";
        }
        if (v instanceof DateType) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS");
            return sdf.format(((DateType) v).toDate());
        }
        if (v instanceof String) {
            return "\"" + Helper.escapeActionScriptString((String) v) + "\"";
        }
        return EcmaScript.toString(v);
    }

    private class MonitoredInputStream extends InputStream {

        private final InputStream is;
        private ByteArrayOutputStream baos;

        public MonitoredInputStream(InputStream is) {
            this.is = is;
            this.baos = new ByteArrayOutputStream();
        }

        @Override
        public int read() throws IOException {
            int ret = is.read();
            if (ret > -1) {
                baos.write(ret);
            }
            return ret;
        }

        public byte[] getReadData() {
            return baos.toByteArray();
        }
    }
}
