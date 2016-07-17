package com.jpexs.decompiler.flash.amf.amf3;

import com.jpexs.decompiler.flash.amf.amf3.types.ArrayType;
import com.jpexs.decompiler.flash.amf.amf3.types.XmlType;
import com.jpexs.decompiler.flash.amf.amf3.types.ObjectType;
import com.jpexs.decompiler.flash.amf.amf3.types.XmlDocType;
import com.jpexs.decompiler.flash.amf.amf3.types.VectorObjectType;
import com.jpexs.decompiler.flash.amf.amf3.types.VectorIntType;
import com.jpexs.decompiler.flash.amf.amf3.types.ByteArrayType;
import com.jpexs.decompiler.flash.amf.amf3.types.DateType;
import com.jpexs.decompiler.flash.amf.amf3.types.VectorDoubleType;
import com.jpexs.decompiler.flash.amf.amf3.types.DictionaryType;
import com.jpexs.decompiler.flash.amf.amf3.types.VectorUIntType;
import com.jpexs.decompiler.flash.amf.amf3.types.BasicType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Amf3InputStream_ extends InputStream {

    public final static Logger LOGGER = Logger.getLogger(Amf3InputStream_.class.getName());
    private final InputStream is;

    public Amf3InputStream_(InputStream is) {
        this.is = is;
    }

    public int readU8() throws IOException {
        return safeRead();
    }

    public int readU16() throws IOException {
        int b1 = safeRead();
        int b2 = safeRead();
        return (b1 << 8) + b2;
    }

    public long readU32() throws IOException {
        int b1 = safeRead();
        int b2 = safeRead();
        int b3 = safeRead();
        int b4 = safeRead();

        return (b1 << 24) + (b2 << 16) + (b3 << 8) + b4;
    }

    private long readLong() throws IOException {
        byte[] readBuffer = new byte[8];
        for (int i = 0; i < 8; i++) {
            readBuffer[i] = (byte) safeRead();
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

    public double readDouble() throws IOException {
        long lval = readLong();
        double ret = Double.longBitsToDouble(lval);
        return ret;
    }

    public long readU29() throws IOException {
        long val = 0;
        for (int i = 1; i <= 4; i++) {
            int b = safeRead();
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
            long mask = (1 << size) - 1; // 111111...up to size
            long positiveVal = (~(val - 1)) & mask;
            long negativeVal = -positiveVal;
            return negativeVal;
        }
        return val;
    }

    private String readUtf8Char(int byteLength) throws IOException {
        if (byteLength == 0) {
            return "";
        }
        byte buf[] = new byte[(int) byteLength]; //how about long strings(?), will the int length be enough?
        int cnt = is.read(buf);
        if (cnt < buf.length) {
            throw new PrematureEndOfTheStreamException();
        }
        String retString = new String(buf, "UTF-8");
        return retString;
    }

    public String readUtf8Vr(List<String> stringTable) throws IOException {
        long u = readU29();
        int stringNoRefFlag = (int) (u & 1);
        if (stringNoRefFlag == 1) {
            int byteLength = (int) (u >> 1); //TODO: long strings, int is not enough for them
            String retString = readUtf8Char(byteLength);
            stringTable.add(retString);
            LOGGER.log(Level.FINE, "Read string: \"{0}\"", retString);
            return retString;
        } else { //flag==0
            int stringRefTableIndex = (int) (u >> 1);

            String retString = stringTable.get(stringRefTableIndex);
            LOGGER.log(Level.FINE, "Read string: reference({0}):" + retString, stringRefTableIndex);
            return retString;

        }
    }

    private int safeRead() throws IOException {
        int ret = read();
        if (ret == -1) {
            throw new PrematureEndOfTheStreamException();
        }
        return ret;
    }

    @Override
    public int read() throws IOException {
        return is.read();
    }

    public Object readValue() throws IOException, NoSerializerExistsException {
        return readValue(new HashMap<>());
    }

    public Object readValue(Map<String, ObjectTypeSerializeHandler> serializers) throws IOException, NoSerializerExistsException {
        return readValue(serializers, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    private Object readValue(Map<String, ObjectTypeSerializeHandler> serializers,
            List<Object> objectTable,
            List<Traits> traitsTable,
            List<String> stringTable
    ) throws IOException, NoSerializerExistsException {

        int marker = readU8();
        switch (marker) {
            case Marker.UNDEFINED:
                LOGGER.log(Level.FINE, "Read value: undefined");
                return BasicType.UNDEFINED;
            case Marker.NULL:
                LOGGER.log(Level.FINE, "Read value: null");
                return BasicType.NULL;
            case Marker.FALSE:
                LOGGER.log(Level.FINE, "Read value: false");
                return Boolean.FALSE;
            case Marker.TRUE:
                LOGGER.log(Level.FINE, "Read value: true");
                return Boolean.TRUE;
            case Marker.INTEGER:
                LOGGER.log(Level.FINE, "Read value: integer");
                long ival = signExtend(readU29(), 29);
                LOGGER.log(Level.FINER, "Integer value: {0}", ival);
                return ival;
            case Marker.DOUBLE:
                LOGGER.log(Level.FINE, "Read value: double");
                double dval = readDouble();
                LOGGER.log(Level.FINER, "Double value: {0}", "" + dval);
                return dval;
            case Marker.STRING:
                LOGGER.log(Level.FINE, "Read value: string");
                String sval = readUtf8Vr(stringTable);
                LOGGER.log(Level.FINER, "String value: {0}", sval);
                return sval;
            case Marker.XML_DOC:
                LOGGER.log(Level.FINE, "Read value: xml_doc");
                long xmlDocU29 = readU29();
                int xmlDocNoRefFlag = (int) (xmlDocU29 & 1);
                if (xmlDocNoRefFlag == 1) {
                    long byteLength = xmlDocU29 >> 1;
                    String xval;
                    if (xmlDocU29 == 0) {
                        xval = "";
                    } else {
                        byte buf[] = new byte[(int) byteLength]; //TODO: long strings, int is not enough for them
                        int cnt = is.read(buf);
                        if (cnt < buf.length) {
                            throw new PrematureEndOfTheStreamException();
                        }
                        xval = new String(buf, "UTF-8");
                    }
                    LOGGER.log(Level.FINER, "XmlDoc value: {0}", xval);
                    XmlDocType retXmlDoc = new XmlDocType(xval);
                    objectTable.add(retXmlDoc);
                    return retXmlDoc;
                } else {
                    int refIndexXmlDoc = (int) (xmlDocU29 >> 1);
                    LOGGER.log(Level.FINER, "XmlDoc value: reference({0})", refIndexXmlDoc);
                    return objectTable.get(refIndexXmlDoc);    //What if it's not XmlRef?
                }
            case Marker.DATE:
                LOGGER.log(Level.FINE, "Read value: date");
                long dateU29 = readU29();
                int dateNoRefFlag = (int) (dateU29 & 1);
                if (dateNoRefFlag == 1) {
                    //remaining bits of dateU29 are not used
                    double dtval = readDouble();
                    DateType retDate = new DateType(dtval);
                    LOGGER.log(Level.FINER, "Date value: {0}", retDate);
                    objectTable.add(retDate);
                    return retDate;
                } else {
                    int refIndexDate = (int) (dateU29 >> 1);
                    LOGGER.log(Level.FINER, "Date value: reference({0})", refIndexDate);
                    return objectTable.get(refIndexDate);   //What if it's not Date?
                }
            case Marker.ARRAY:
                LOGGER.log(Level.FINE, "Read value: array");
                long arrayU29 = readU29();
                int arrayNoRefFlag = (int) (arrayU29 & 1);
                if (arrayNoRefFlag == 1) {
                    int denseCount = (int) (arrayU29 >> 1);
                    LOGGER.log(Level.FINEST, "Array value: denseCount={0}", new Object[]{denseCount});
                    List<Pair<String, Object>> assocPart = new ArrayList<>();
                    List<Object> densePart = new ArrayList<>();
                    ArrayType retArray = new ArrayType(densePart, assocPart);
                    objectTable.add(retArray); //add before processing  elements which may reference this

                    while (true) {
                        String key = readUtf8Vr(stringTable);
                        if (key.isEmpty()) {
                            break;
                        } else {
                            try {
                                Object val = readValue(serializers, objectTable, traitsTable, stringTable);
                                assocPart.add(new Pair<>(key, val));
                            } catch (NoSerializerExistsException nse) {
                                assocPart.add(new Pair<>(key, nse.getIncompleteData()));
                                throw new NoSerializerExistsException(nse.getClassName(), retArray, nse);
                            }
                        }
                    }
                    LOGGER.log(Level.FINEST, "Array value: assocSize={0}", new Object[]{assocPart.size()});

                    for (int i = 0; i < denseCount; i++) {
                        try {
                            densePart.add(readValue(serializers, objectTable, traitsTable, stringTable));
                        } catch (NoSerializerExistsException nse) {
                            densePart.add(nse.getIncompleteData());
                            for (int j = i + 1; j < denseCount; j++) {
                                densePart.add(BasicType.UNKNOWN);
                            }
                            throw new NoSerializerExistsException(nse.getClassName(), retArray, nse);
                        }
                    }
                    LOGGER.log(Level.FINER, "Array value: dense_size={0},assocSize={1}", new Object[]{densePart.size(), assocPart.size()});
                    return retArray;

                } else {
                    int refIndexArray = (int) (arrayU29 >> 1);
                    LOGGER.log(Level.FINER, "Array value: reference({0})", refIndexArray);
                    return objectTable.get(refIndexArray);   //What if it's not Array?
                }

            case Marker.OBJECT:
                LOGGER.log(Level.FINE, "Read value: object");
                long objectU29 = readU29();
                int objectNoRefFlag = (int) (objectU29 & 1);
                if (objectNoRefFlag == 1) {
                    Traits traits;
                    int objectTraitsNoRefFlag = (int) ((objectU29 >> 1) & 1);
                    if (objectTraitsNoRefFlag == 1) {
                        int objectTraitsExtFlag = (int) ((objectU29 >> 2) & 1);
                        ObjectType retObjectType;
                        if (objectTraitsExtFlag == 1) {
                            String className = readUtf8Vr(stringTable);
                            if (!serializers.containsKey(className)) {
                                throw new NoSerializerExistsException(className, new ObjectType(className, null, new ArrayList<>()), null);
                            }

                            MonitoredInputStream mis = new MonitoredInputStream(is);
                            List<Pair<String, Object>> serMembers = serializers.get(className).readObject(className, mis);
                            byte serData[] = mis.getReadData();
                            retObjectType = new ObjectType(className, serData, serMembers);

                            LOGGER.log(Level.FINER, "Object/Traits value: customSerialized");
                            objectTable.add(retObjectType);
                            return retObjectType;
                        } else {
                            int dynamicFlag = (int) ((objectU29 >> 3) & 1);
                            int numSealed = (int) (objectU29 >> 4);
                            LOGGER.log(Level.FINEST, "object dynamicFlag:{0}", dynamicFlag);
                            LOGGER.log(Level.FINEST, "object numSealed:{0}", numSealed);
                            String className = readUtf8Vr(stringTable);
                            LOGGER.log(Level.FINEST, "object className:{0}", className);
                            List<String> sealedMemberNames = new ArrayList<>();
                            for (int i = 0; i < numSealed; i++) {
                                sealedMemberNames.add(readUtf8Vr(stringTable));
                            }
                            traits = new Traits(className, dynamicFlag == 1, sealedMemberNames);
                        }

                    } else {
                        int refIndexTraits = (int) (objectU29 >> 2);
                        traits = traitsTable.get(refIndexTraits);
                        LOGGER.log(Level.FINER, "Traits value: reference({0}) - traitsize={1}", new Object[]{refIndexTraits, traits.getSealedMemberNames().size()});
                    }

                    if (objectTraitsNoRefFlag == 1) {
                        traitsTable.add(traits);
                    }
                    List<Pair<String, Object>> sealedMembers = new ArrayList<>();
                    List<Pair<String, Object>> dynamicMembers = new ArrayList<>();

                    Object retObjectType = new ObjectType(traits.isDynamic(), sealedMembers, dynamicMembers, traits.getClassName());
                    objectTable.add(retObjectType); //add it before any subvalue can reference it
                    List<Object> sealedMemberValues = new ArrayList<>();
                    NoSerializerExistsException error = null;

                    for (int i = 0; i < traits.getSealedMemberNames().size(); i++) {
                        try {
                            sealedMemberValues.add(readValue(serializers, objectTable, traitsTable, stringTable));
                        } catch (NoSerializerExistsException nse) {
                            sealedMemberValues.add(nse.getIncompleteData());
                            for (int j = i + 1; j < traits.getSealedMemberNames().size(); j++) {
                                sealedMemberValues.add(BasicType.UNKNOWN);
                            }
                            error = nse;
                            break;
                        }
                    }

                    for (int i = 0; i < traits.getSealedMemberNames().size(); i++) {
                        sealedMembers.add(new Pair<>(traits.getSealedMemberNames().get(i), sealedMemberValues.get(i)));
                    }
                    if (traits.isDynamic()) {
                        String dynamicMemberName;
                        while (!(dynamicMemberName = readUtf8Vr(stringTable)).isEmpty()) {
                            try {
                                Object dynamicMemberValue = readValue(serializers, objectTable, traitsTable, stringTable);
                                dynamicMembers.add(new Pair<>(dynamicMemberName, dynamicMemberValue));
                            } catch (NoSerializerExistsException nse) {
                                dynamicMembers.add(new Pair<>(dynamicMemberName, nse.getIncompleteData()));
                                throw new NoSerializerExistsException(nse.getClassName(), retObjectType, nse);
                            }
                        }
                    }

                    LOGGER.log(Level.FINER, "Object value: dynamic={0},className={1},sealedSize={2},dynamicSize={3}", new Object[]{traits.isDynamic(), traits.getClassName(), sealedMembers.size(), dynamicMembers.size()});
                    return retObjectType;
                } else {
                    int refIndexObject = (int) (objectU29 >> 1);
                    LOGGER.log(Level.FINER, "Object value: reference({0})", refIndexObject);
                    return objectTable.get(refIndexObject);
                }
            case Marker.XML:
                LOGGER.log(Level.FINE, "Read value: xml");
                long xmlU29 = readU29();
                int xmlNoRefFlag = (int) (xmlU29 & 1);
                if (xmlNoRefFlag == 1) {
                    int byteLength = (int) (xmlU29 >> 1); //TODO: long strings, int is not enough for them
                    String xString = readUtf8Char(byteLength);
                    XmlType retXmlType = new XmlType(xString);
                    LOGGER.log(Level.FINER, "Xml value: {0}", xString);
                    objectTable.add(retXmlType);
                    return retXmlType;
                } else {
                    int refIndexXml = (int) (xmlU29 >> 1);
                    LOGGER.log(Level.FINER, "XML value: reference({0})", refIndexXml);
                    return objectTable.get(refIndexXml);
                }
            case Marker.BYTE_ARRAY:
                LOGGER.log(Level.FINE, "Read value: bytearray");
                long byteArrayU29 = readU29();
                int byteArrayNoRefFlag = (int) (byteArrayU29 & 1);
                if (byteArrayNoRefFlag == 1) {
                    int byteArrayLength = (int) (byteArrayU29 >> 1);
                    byte byteArrayBuf[] = new byte[byteArrayLength];
                    if (is.read(byteArrayBuf) != byteArrayLength) {
                        throw new PrematureEndOfTheStreamException();
                    }

                    LOGGER.log(Level.FINER, "ByteArray value: bytes[{0}]", byteArrayLength);
                    ByteArrayType retByteArrayType = new ByteArrayType(byteArrayBuf);
                    objectTable.add(retByteArrayType);
                    return retByteArrayType;
                } else {
                    int refIndexByteArray = (int) (byteArrayU29 >> 1);
                    LOGGER.log(Level.FINER, "ByteArray value: reference({0})", refIndexByteArray);
                    return objectTable.get(refIndexByteArray);
                }
            case Marker.VECTOR_INT:
                LOGGER.log(Level.FINE, "Read value: vector_int");
                long vectorIntU29 = readU29();
                int vectorIntNoRefFlag = (int) (vectorIntU29 & 1);
                if (vectorIntNoRefFlag == 1) {
                    int vectorIntCountItems = (int) (vectorIntU29 >> 1);
                    int fixed = readU8();
                    List<Long> vals = new ArrayList<>();
                    for (int i = 0; i < vectorIntCountItems; i++) {
                        vals.add(readU32());
                    }
                    VectorIntType retVectorInt = new VectorIntType(fixed == 1, vals);
                    LOGGER.log(Level.FINER, "Vector<int> value: fixed={0}, size={1}]", new Object[]{fixed, vectorIntCountItems});
                    objectTable.add(retVectorInt);
                    return retVectorInt;
                } else {
                    int refIndexVectorInt = (int) (vectorIntU29 >> 1);
                    LOGGER.log(Level.FINER, "Vector<int> value: reference({0})", refIndexVectorInt);
                    return objectTable.get(refIndexVectorInt);
                }
            case Marker.VECTOR_UINT:
                LOGGER.log(Level.FINE, "Read value: vector_uint");
                long vectorUIntU29 = readU29();
                int vectorUIntNoRefFlag = (int) (vectorUIntU29 & 1);
                if (vectorUIntNoRefFlag == 1) {
                    int vectorUIntCountItems = (int) (vectorUIntU29 >> 1);
                    int fixed = readU8();
                    List<Long> vals = new ArrayList<>();
                    for (int i = 0; i < vectorUIntCountItems; i++) {
                        vals.add(signExtend(readU32(), 32));
                    }
                    VectorUIntType retVectorUInt = new VectorUIntType(fixed == 1, vals);
                    LOGGER.log(Level.FINER, "Vector<uint> value: fixed={0}, size={1}]", new Object[]{fixed, vectorUIntCountItems});
                    objectTable.add(retVectorUInt);
                    return retVectorUInt;
                } else {
                    int refIndexVectorUInt = (int) (vectorUIntU29 >> 1);
                    LOGGER.log(Level.FINER, "Vector<uint> value: reference({0})", refIndexVectorUInt);
                    return objectTable.get(refIndexVectorUInt);
                }
            case Marker.VECTOR_DOUBLE:
                LOGGER.log(Level.FINE, "Read value: vector_double");
                long vectorDoubleU29 = readU29();
                int vectorDoubleNoRefFlag = (int) (vectorDoubleU29 & 1);
                if (vectorDoubleNoRefFlag == 1) {
                    int vectorDoubleCountItems = (int) (vectorDoubleU29 >> 1);
                    int fixed = readU8();
                    List<Double> vals = new ArrayList<>();
                    for (int i = 0; i < vectorDoubleCountItems; i++) {
                        vals.add(readDouble());
                    }
                    VectorDoubleType retVectorDouble = new VectorDoubleType(fixed == 1, vals);
                    LOGGER.log(Level.FINER, "Vector<double> value: fixed={0}, size={1}]", new Object[]{fixed, vectorDoubleCountItems});
                    objectTable.add(retVectorDouble);
                    return retVectorDouble;
                } else {
                    int refIndexVectorDouble = (int) (vectorDoubleU29 >> 1);
                    LOGGER.log(Level.FINER, "Vector<double> value: reference({0})", refIndexVectorDouble);
                    return objectTable.get(refIndexVectorDouble);
                }
            case Marker.VECTOR_OBJECT:
                LOGGER.log(Level.FINE, "Read value: vector_object");
                long vectorObjectU29 = readU29();
                int vectorObjectNoRefFlag = (int) (vectorObjectU29 & 1);
                if (vectorObjectNoRefFlag == 1) {
                    int vectorObjectCountItems = (int) (vectorObjectU29 >> 1);
                    int fixed = readU8();
                    String objectTypeName = readUtf8Vr(stringTable); //uses "*" for any type
                    List<Object> vals = new ArrayList<>();
                    NoSerializerExistsException error = null;
                    for (int i = 0; i < vectorObjectCountItems; i++) {
                        try {
                            vals.add(readValue(serializers, objectTable, traitsTable, stringTable));
                        } catch (NoSerializerExistsException nse) {
                            vals.add(nse.getIncompleteData());
                            for (int j = i + 1; j < vectorObjectCountItems; j++) {
                                vals.add(BasicType.UNKNOWN);
                            }
                            error = nse;
                            break;
                        }
                    }
                    VectorObjectType retVectorObject = new VectorObjectType(fixed == 1, objectTypeName, vals);
                    LOGGER.log(Level.FINER, "Vector<Object> value: fixed={0}, size={1}, typeName:{2}]", new Object[]{fixed, vectorObjectCountItems, objectTypeName});
                    objectTable.add(retVectorObject);
                    if (error != null) {
                        throw new NoSerializerExistsException(error.getClassName(), retVectorObject, error);
                    }
                    return retVectorObject;
                } else {
                    int refIndexVectorObject = (int) (vectorObjectU29 >> 1);
                    LOGGER.log(Level.FINER, "Vector<Object> value: reference({0})", refIndexVectorObject);
                    return objectTable.get(refIndexVectorObject);
                }
            case Marker.DICTIONARY:
                long dictionaryObjectU29 = readU29();
                int dictionaryNoRefFlag = (int) (dictionaryObjectU29 & 1);
                if (dictionaryNoRefFlag == 1) {
                    int numEntries = (int) (dictionaryObjectU29 >> 1);
                    int weakKeys = readU8();
                    List<Pair<Object, Object>> data = new ArrayList<>();
                    NoSerializerExistsException error = null;
                    for (int i = 0; i < numEntries; i++) {
                        Object key;
                        Object val;
                        try {
                            key = readValue(serializers, objectTable, traitsTable, stringTable);
                            try {
                                val = readValue(serializers, objectTable, traitsTable, stringTable);
                            } catch (NoSerializerExistsException nse) {
                                error = nse;
                                val = BasicType.UNKNOWN;
                            }
                        } catch (NoSerializerExistsException nse) {
                            error = nse;
                            key = BasicType.UNKNOWN;
                            val = BasicType.UNKNOWN;
                        }

                        data.add(new Pair<>(key, val));
                        if (error != null) {
                            for (int j = i + 1; j < numEntries; j++) {
                                data.add(new Pair<>(BasicType.UNKNOWN, BasicType.UNKNOWN));
                            }
                            break;
                        }
                    }
                    DictionaryType retDictionary = new DictionaryType(weakKeys == 1, data);
                    objectTable.add(retDictionary);
                    if (error != null) {
                        throw new NoSerializerExistsException(error.getClassName(), retDictionary, error);
                    }
                    return retDictionary;
                } else {
                    int refIndexDictionary = (int) (dictionaryObjectU29 >> 1);
                    LOGGER.log(Level.FINER, "Dictionary value: reference({0})", refIndexDictionary);
                    return objectTable.get(refIndexDictionary);
                }
            default:
                throw new UnsupportedValueType(marker);
        }
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
