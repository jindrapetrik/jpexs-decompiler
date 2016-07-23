package com.jpexs.decompiler.flash.exporters.amf.amf3;

import com.jpexs.decompiler.flash.amf.amf3.Pair;
import com.jpexs.decompiler.flash.amf.amf3.WithSubValues;
import com.jpexs.decompiler.flash.amf.amf3.types.ArrayType;
import com.jpexs.decompiler.flash.amf.amf3.types.XmlType;
import com.jpexs.decompiler.flash.amf.amf3.types.ObjectType;
import com.jpexs.decompiler.flash.amf.amf3.types.AbstractVectorType;
import com.jpexs.decompiler.flash.amf.amf3.types.XmlDocType;
import com.jpexs.decompiler.flash.amf.amf3.types.ByteArrayType;
import com.jpexs.decompiler.flash.amf.amf3.types.DateType;
import com.jpexs.decompiler.flash.amf.amf3.types.DictionaryType;
import com.jpexs.decompiler.flash.amf.amf3.types.BasicType;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.helpers.Helper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Amf3Exporter {

    /**
     * Converts AMF value to something human-readable.
     *
     * @param amfValue
     * @return
     */
    public static String amfToString(Object amfValue) {
        Map<Object, Integer> refCount = new HashMap<>();
        List<Object> objectList = new ArrayList<>();
        Map<Object, String> objectAlias = new HashMap<>();
        populateObjects(amfValue, refCount, objectList, objectAlias);
        return amfToString(new ArrayList<>(), 0, amfValue, refCount, objectAlias);
    }

    /**
     * Populates all object instances and their references and generates aliases
     *
     * @param object Object to be populated
     * @param referenceCount Result: Map of reference number
     * @param objectList Result: List of all found object instances
     * @param objectAlias Result: Map of assigned object names
     */
    public static void populateObjects(Object object, Map<Object, Integer> referenceCount, List<Object> objectList, Map<Object, String> objectAlias) {
        if (((List<? extends Class>) Arrays.asList(String.class, Long.class, Double.class, BasicType.class, Boolean.class)).contains(object.getClass())) {
            return;
        }
        if (object instanceof BasicType) {
            return;
        }
        int prevRef = 0;
        if (referenceCount.containsKey(object)) {
            prevRef = referenceCount.get(object);
        }
        referenceCount.put(object, prevRef + 1);
        if (prevRef == 0) {
            if (object instanceof WithSubValues) {
                List<Object> subvalues = ((WithSubValues) object).getSubValues();
                for (Object o : subvalues) {
                    populateObjects(o, referenceCount, objectList, objectAlias);
                }
            }
            objectList.add(object);
            objectAlias.put(object, "obj" + objectList.size());
        }
    }

    private static String indent(int level) {
        String na = "";
        for (int i = 0; i < level; i++) {
            na += "  ";
        }
        return na;
    }

    /**
     * Processes one level of object and converts it to string
     *
     * @param processedObjects
     * @param level
     * @param object
     * @param referenceCount
     * @param objectAlias
     * @return
     */
    private static String amfToString(List<Object> processedObjects, int level, Object object, Map<Object, Integer> referenceCount, Map<Object, String> objectAlias) {
        if (object instanceof String) {
            return "\"" + Helper.escapeActionScriptString((String) object) + "\"";
        }
        if (((List<? extends Class>) Arrays.asList(Long.class, Double.class, Boolean.class)).contains(object.getClass())) {
            return EcmaScript.toString(object);
        }

        if (object instanceof BasicType) {
            return object.toString();
        }

        StringBuilder ret = new StringBuilder();

        Integer refCount = referenceCount.get(object);
        if (refCount > 1 && processedObjects.contains(object)) {
            ret.append("#").append(objectAlias.get(object));
            return ret.toString();
        }
        processedObjects.add(object);

        String addId = refCount > 1 ? indent(level + 1) + "\"id\": \"" + objectAlias.get(object) + "\",\r\n" : "";

        if (object instanceof AbstractVectorType) {
            AbstractVectorType avt = (AbstractVectorType) object;
            ret.append("{\r\n");
            ret.append(indent(level + 1)).append("\"type\": \"Vector\",\r\n");
            ret.append(addId);
            ret.append(indent(level + 1)).append("\"fixed\": ").append(avt.isFixed()).append(",\r\n");
            ret.append(indent(level + 1)).append("\"subtype\": ").append(amfToString(processedObjects, level, avt.getTypeName(), referenceCount, objectAlias)).append(",\r\n");
            ret.append(indent(level + 1)).append("\"values\": [");
            for (int i = 0; i < avt.getValues().size(); i++) {
                if (i > 0) {
                    ret.append(", ");
                }
                ret.append(amfToString(processedObjects, level + 1, avt.getValues().get(i), referenceCount, objectAlias));
            }
            ret.append("]\r\n");
            ret.append(indent(level)).append("}");
        } else if (object instanceof ObjectType) {
            ObjectType ot = (ObjectType) object;
            ret.append("{\r\n");
            ret.append(indent(level + 1)).append("\"type\": \"Object\",\r\n");
            ret.append(addId);
            ret.append(indent(level + 1)).append("\"className\": ").append(amfToString(processedObjects, level, ot.getClassName(), referenceCount, objectAlias)).append(",\r\n");
            if (ot.isSerialized()) {
                byte[] serData = ot.getSerializedData();
                if (serData == null) {
                    ret.append(indent(level + 1)).append("\"serialized\": unknown\r\n");
                } else {
                    ret.append(indent(level + 1)).append("\"serialized\": \"").append(javax.xml.bind.DatatypeConverter.printHexBinary(serData)).append("\",\r\n");
                    if (!ot.getSerializedMembers().isEmpty()) {
                        ret.append(indent(level + 1)).append("\"unserializedMembers\": {\r\n");
                        for (int i = 0; i < ot.getSerializedMembers().size(); i++) {
                            Pair<String, Object> member = ot.getSerializedMembers().get(i);
                            ret.append(indent(level + 2)).append(amfToString(processedObjects, level + 2, member.getFirst(), referenceCount, objectAlias)).append(":").append(amfToString(processedObjects, level + 1, member.getSecond(), referenceCount, objectAlias));
                            if (i < ot.getSerializedMembers().size() - 1) {
                                ret.append(",\r\n");
                            } else {
                                ret.append("\r\n");
                            }
                        }
                        ret.append(indent(level + 1)).append("}");
                        ret.append("\r\n");
                    }
                }
            } else {
                ret.append(indent(level + 1)).append("\"dynamic\": ").append(ot.isDynamic()).append(",\r\n");
                //if (!ot.getSealedMembers().isEmpty()) {
                ret.append(indent(level + 1)).append("\"sealedMembers\": {\r\n");
                for (int i = 0; i < ot.getSealedMembers().size(); i++) {
                    Pair<String, Object> member = ot.getSealedMembers().get(i);
                    ret.append(indent(level + 2)).append(amfToString(processedObjects, level + 2, member.getFirst(), referenceCount, objectAlias)).append(":").append(amfToString(processedObjects, level + 1, member.getSecond(), referenceCount, objectAlias));
                    if (i < ot.getSealedMembers().size() - 1) {
                        ret.append(",\r\n");
                    } else {
                        ret.append("\r\n");
                    }
                }
                ret.append(indent(level + 1)).append("}");
                //if (!ot.getDynamicMembers().isEmpty()) {
                ret.append(",");
                //}
                ret.append("\r\n");
                //}
                //if (!ot.getDynamicMembers().isEmpty()) {
                ret.append(indent(level + 1)).append("\"dynamicMembers\": {\r\n");
                for (int i = 0; i < ot.getDynamicMembers().size(); i++) {
                    Pair<String, Object> member = ot.getDynamicMembers().get(i);
                    ret.append(indent(level + 2)).append(amfToString(processedObjects, level + 2, member.getFirst(), referenceCount, objectAlias));
                    ret.append(":");
                    ret.append(amfToString(processedObjects, level + 2, member.getSecond(), referenceCount, objectAlias));
                    if (i < ot.getDynamicMembers().size() - 1) {
                        ret.append(",");
                    }
                    ret.append("\r\n");
                }
                ret.append(indent(level + 1)).append("}\r\n");
                //}
            }
            ret.append(indent(level)).append("}");
        } else if (object instanceof ArrayType) {
            ArrayType at = (ArrayType) object;
            ret.append("{\r\n");
            ret.append(indent(level + 1)).append("\"type\": \"Array\",\r\n");
            ret.append(addId);
            ret.append(indent(level + 1)).append("\"denseValues\": [");

            for (int i = 0; i < at.getDenseValues().size(); i++) {
                if (i > 0) {
                    ret.append(", ");
                }
                ret.append(amfToString(processedObjects, level + 2, at.getDenseValues().get(i), referenceCount, objectAlias));
            }
            ret.append("],\r\n");
            ret.append(indent(level + 1)).append("\"associativeValues\": {");
            if (!at.getAssociativeValues().isEmpty()) {
                ret.append("\r\n");
            }
            for (int i = 0; i < at.getAssociativeValues().size(); i++) {
                Pair<String, Object> p = at.getAssociativeValues().get(i);
                ret.append(indent(level + 2)).append(amfToString(processedObjects, level + 1, p.getFirst(), referenceCount, objectAlias)).append(" : ").append(amfToString(processedObjects, level + 1, p.getSecond(), referenceCount, objectAlias));
                if (i < at.getAssociativeValues().size() - 1) {
                    ret.append(",");
                }
                ret.append("\r\n");
            }
            if (!at.getAssociativeValues().isEmpty()) {
                ret.append(indent(level + 1));
            }
            ret.append("}\r\n");
            ret.append(indent(level)).append("}");
        } else if (object instanceof DictionaryType) {
            DictionaryType dt = (DictionaryType) object;
            ret.append("{\r\n");
            ret.append(indent(level + 1)).append("\"type\": \"Dictionary\",\r\n");
            ret.append(addId);
            ret.append(indent(level + 1)).append("\"weakKeys\": ").append(dt.hasWeakKeys()).append(",\r\n");
            ret.append(indent(level + 1)).append("\"entries\": {\r\n");
            for (int i = 0; i < dt.getPairs().size(); i++) {
                Pair<Object, Object> pair = dt.getPairs().get(i);
                ret.append(indent(level + 1)).append(amfToString(processedObjects, level + 1, pair.getFirst(), referenceCount, objectAlias)).append(" : ").append(amfToString(processedObjects, level + 1, pair.getSecond(), referenceCount, objectAlias));
                if (i < dt.getPairs().size() - 1) {
                    ret.append(",");
                }
                ret.append("\r\n");
            }
            ret.append(indent(level + 1)).append("}\r\n");
            ret.append(indent(level)).append("}");
        } else if (object instanceof ByteArrayType) {
            ByteArrayType ba = (ByteArrayType) object;
            byte data[] = ba.getData();
            return "{\r\n"
                    + indent(level + 1) + "\"type\": \"ByteArray\",\r\n"
                    + addId
                    + indent(level + 1) + "\"value\": \"" + javax.xml.bind.DatatypeConverter.printHexBinary(data) + "\"\r\n"
                    + indent(level) + "}";
        } else if (object instanceof DateType) {
            DateType dt = (DateType) object;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS");
            return "{\r\n"
                    + indent(level + 1) + "\"type\": \"Date\",\r\n"
                    + addId
                    + indent(level + 1) + "\"value\": " + amfToString(processedObjects, level, sdf.format(new Date((long) dt.getVal())), referenceCount, objectAlias) + "\r\n"
                    + indent(level) + "}";

        } else if (object instanceof XmlDocType) {
            return "{\r\n"
                    + indent(level + 1) + "\"type\": \"XMLDocument\",\r\n"
                    + addId
                    + indent(level + 1) + "\"value\": " + amfToString(processedObjects, level, ((XmlDocType) object).getData(), referenceCount, objectAlias) + "\r\n"
                    + indent(level) + "}";
        } else if (object instanceof XmlType) {
            return "{\r\n"
                    + indent(level + 1) + "\"type\": \"XML\",\r\n"
                    + addId
                    + indent(level + 1) + "\"value\": " + amfToString(processedObjects, level, ((XmlType) object).getData(), referenceCount, objectAlias) + "\r\n"
                    + indent(level) + "}";
        } else {
            throw new IllegalArgumentException("Unsupported type: " + object.getClass());
        }

        return ret.toString();
    }
}
