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
package com.jpexs.decompiler.flash.exporters.amf.amf3;

import com.jpexs.decompiler.flash.amf.amf3.WithSubValues;
import com.jpexs.decompiler.flash.amf.amf3.types.AbstractVectorType;
import com.jpexs.decompiler.flash.amf.amf3.types.ArrayType;
import com.jpexs.decompiler.flash.amf.amf3.types.BasicType;
import com.jpexs.decompiler.flash.amf.amf3.types.ByteArrayType;
import com.jpexs.decompiler.flash.amf.amf3.types.DateType;
import com.jpexs.decompiler.flash.amf.amf3.types.DictionaryType;
import com.jpexs.decompiler.flash.amf.amf3.types.ObjectType;
import com.jpexs.decompiler.flash.amf.amf3.types.XmlDocType;
import com.jpexs.decompiler.flash.amf.amf3.types.XmlType;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.helpers.Helper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AMF3 exporter.
 */
public class Amf3Exporter {

    /**
     * Constructor.
     */
    private Amf3Exporter() {

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
     * Converts AMF value to something human-readable with indentStr " " and
     * CRLF newlines
     *
     * @param amfValue AMF value
     * @return Human-readable string
     */
    public static String amfToString(Object amfValue) {
        return amfToString(amfValue, "  ", "\r\n");
    }

    /**
     * Converts AMF value to something human-readable.
     *
     * @param amfValue AMF value
     * @param indentStr Indentation string
     * @param newLine Newline string
     * @return Human-readable string
     */
    public static String amfToString(Object amfValue, String indentStr, String newLine) {
        Map<Object, Integer> refCount = new HashMap<>();
        List<Object> objectList = new ArrayList<>();
        Map<Object, String> objectAlias = new HashMap<>();
        populateObjects(amfValue, refCount, objectList, objectAlias);
        return amfToString(indentStr, newLine, new ArrayList<>(), 0, amfValue, refCount, objectAlias);
    }
    
    /**
     * Processes one level of object and converts it to string
     *
     * @param processedObjects List of already processed objects
     * @param level Current level of recursion
     * @param object Object to be processed
     * @param referenceCount Reference count
     * @param objectAlias Object alias
     * @return String representation of object
     */
    private static String amfToString(String indentStr, String newLine, List<Object> processedObjects, int level, Object object, Map<Object, Integer> referenceCount, Map<Object, String> objectAlias) {
        if (object instanceof String) {
            return "\"" + Helper.escapeActionScriptString((String) object) + "\"";
        }
        if (((List<? extends Class>) Arrays.asList(Long.class, Double.class, Boolean.class)).contains(object.getClass())) {
            return EcmaScript.toString(object);
        }

        StringBuilder ret = new StringBuilder();

        if (object == BasicType.UNDEFINED) {
            ret.append("{").append(newLine);
            ret.append(indent(level + 1)).append("\"type\": \"Undefined\"").append(newLine);
            ret.append(indent(level)).append("}");
            return ret.toString();
        }

        if (object instanceof BasicType) {
            return object.toString();
        }

        Integer refCount = referenceCount.get(object);
        if (refCount > 1 && processedObjects.contains(object)) {
            ret.append("{").append(newLine);
            ret.append(indent(level + 1)).append("\"type\": \"Reference\",").append(newLine);
            ret.append(indent(level + 1)).append("\"referencedId\": \"").append(objectAlias.get(object)).append("\"").append(newLine);
            ret.append(indent(level)).append("}");
            return ret.toString();
        }
        processedObjects.add(object);

        String addId = refCount > 1 ? indent(level + 1) + "\"id\": \"" + objectAlias.get(object) + "\"," + newLine : "";

        if (object instanceof AbstractVectorType) {
            AbstractVectorType avt = (AbstractVectorType) object;
            ret.append("{").append(newLine);
            ret.append(indent(level + 1)).append("\"type\": \"Vector\",").append(newLine);
            ret.append(addId);
            ret.append(indent(level + 1)).append("\"fixed\": ").append(avt.isFixed()).append(",").append(newLine);
            ret.append(indent(level + 1)).append("\"subtype\": ").append(amfToString(indentStr, newLine, processedObjects, level, avt.getTypeName(), referenceCount, objectAlias)).append(",").append(newLine);
            ret.append(indent(level + 1)).append("\"values\": [");
            for (int i = 0; i < avt.getValues().size(); i++) {
                if (i > 0) {
                    ret.append(", ");
                }
                ret.append(amfToString(indentStr, newLine, processedObjects, level + 1, avt.getValues().get(i), referenceCount, objectAlias));
            }
            ret.append("]").append(newLine);
            ret.append(indent(level)).append("}");
        } else if (object instanceof ObjectType) {
            ObjectType ot = (ObjectType) object;
            ret.append("{").append(newLine);
            ret.append(indent(level + 1)).append("\"type\": \"Object\",").append(newLine);
            ret.append(addId);
            ret.append(indent(level + 1)).append("\"className\": ").append(amfToString(indentStr, newLine, processedObjects, level, ot.getClassName(), referenceCount, objectAlias)).append(",").append(newLine);
            if (ot.isSerialized()) {
                byte[] serData = ot.getSerializedData();
                if (serData == null) {
                    ret.append(indent(level + 1)).append("\"serialized\": unknown").append(newLine);
                } else {
                    ret.append(indent(level + 1)).append("\"serialized\": \"").append(Helper.byteArrayToHex(serData)).append("\",").append(newLine);
                    if (!ot.getSerializedMembers().isEmpty()) {
                        ret.append(indent(level + 1)).append("\"unserializedMembers\": {").append(newLine);
                        if (true) {
                            int i = 0;
                            for (String key : ot.sealedMembersKeySet()) {
                                Object val = ot.getSealedMember(key);
                                ret.append(indent(level + 2)).append(amfToString(indentStr, newLine, processedObjects, level + 2, key, referenceCount, objectAlias)).append(":").append(amfToString(indentStr, newLine, processedObjects, level + 1, val, referenceCount, objectAlias));
                                if (i < ot.serializedMembersSize() - 1) {
                                    ret.append(",").append(newLine);
                                } else {
                                    ret.append(newLine);
                                }
                                i++;
                            }
                        }
                        ret.append(indent(level + 1)).append("}");
                        ret.append(newLine);
                    }
                }
            } else {
                ret.append(indent(level + 1)).append("\"dynamic\": ").append(ot.isDynamic()).append(",").append(newLine);
                //if (!ot.getSealedMembers().isEmpty()) {
                ret.append(indent(level + 1)).append("\"sealedMembers\": {").append(newLine);
                if (true) {
                    int i = 0;
                    for (String key : ot.sealedMembersKeySet()) {
                        Object val = ot.getSealedMember(key);
                        ret.append(indent(level + 2)).append(amfToString(indentStr, newLine, processedObjects, level + 2, key, referenceCount, objectAlias)).append(":").append(amfToString(indentStr, newLine, processedObjects, level + 1, val, referenceCount, objectAlias));
                        if (i < ot.sealedMembersSize() - 1) {
                            ret.append(",").append(newLine);
                        } else {
                            ret.append(newLine);
                        }
                        i++;
                    }
                }
                ret.append(indent(level + 1)).append("}");
                //if (!ot.getDynamicMembers().isEmpty()) {
                ret.append(",");
                //}
                ret.append(newLine);
                //}
                //if (!ot.getDynamicMembers().isEmpty()) {
                ret.append(indent(level + 1)).append("\"dynamicMembers\": {").append(newLine);
                if (true) {
                    int i = 0;
                    for (String key : ot.dynamicMembersKeySet()) {
                        Object val = ot.getDynamicMember(key);
                        ret.append(indent(level + 2)).append(amfToString(indentStr, newLine, processedObjects, level + 2, key, referenceCount, objectAlias));
                        ret.append(": ");
                        ret.append(amfToString(indentStr, newLine, processedObjects, level + 2, val, referenceCount, objectAlias));
                        if (i < ot.dynamicMembersSize() - 1) {
                            ret.append(",");
                        }
                        ret.append(newLine);
                        i++;
                    }
                }
                ret.append(indent(level + 1)).append("}").append(newLine);
                //}
            }
            ret.append(indent(level)).append("}");
        } else if (object instanceof ArrayType) {
            ArrayType at = (ArrayType) object;
            ret.append("{").append(newLine);
            ret.append(indent(level + 1)).append("\"type\": \"Array\",").append(newLine);
            ret.append(addId);
            ret.append(indent(level + 1)).append("\"denseValues\": [");

            for (int i = 0; i < at.getDenseValues().size(); i++) {
                if (i > 0) {
                    ret.append(", ");
                }
                ret.append(amfToString(indentStr, newLine, processedObjects, level + 2, at.getDenseValues().get(i), referenceCount, objectAlias));
            }
            ret.append("],").append(newLine);
            ret.append(indent(level + 1)).append("\"associativeValues\": {");
            if (!at.getAssociativeValues().isEmpty()) {
                ret.append(newLine);
            }
            int i = 0;
            for (String key : at.associativeKeySet()) {
                Object val = at.getAssociative(key);
                ret.append(indent(level + 2)).append(amfToString(indentStr, newLine, processedObjects, level + 1, key, referenceCount, objectAlias)).append(" : ").append(amfToString(indentStr, newLine, processedObjects, level + 1, val, referenceCount, objectAlias));
                if (i < at.getAssociativeValues().size() - 1) {
                    ret.append(",");
                }
                ret.append(newLine);
                i++;
            }
            if (!at.getAssociativeValues().isEmpty()) {
                ret.append(indent(level + 1));
            }
            ret.append("}").append(newLine);
            ret.append(indent(level)).append("}");
        } else if (object instanceof DictionaryType) {
            DictionaryType dt = (DictionaryType) object;
            ret.append("{").append(newLine);
            ret.append(indent(level + 1)).append("\"type\": \"Dictionary\",").append(newLine);
            ret.append(addId);
            ret.append(indent(level + 1)).append("\"weakKeys\": ").append(dt.hasWeakKeys()).append(",").append(newLine);
            ret.append(indent(level + 1)).append("\"entries\": [").append(newLine);
            int i = 0;
            for (Object key : dt.keySet()) {
                Object val = dt.get(key);
                ret.append(indent(level + 2)).append("{").append(newLine);
                ret.append(indent(level + 3)).append("\"key\": ").append(amfToString(indentStr, newLine, processedObjects, level + 3, key, referenceCount, objectAlias)).append(",").append(newLine);
                ret.append(indent(level + 3)).append("\"value\": ").append(amfToString(indentStr, newLine, processedObjects, level + 3, val, referenceCount, objectAlias)).append(newLine);
                ret.append(indent(level + 2)).append("}");
                if (i < dt.size() - 1) {
                    ret.append(",");
                }
                ret.append(newLine);
                i++;
            }
            ret.append(indent(level + 1)).append("]").append(newLine);
            ret.append(indent(level)).append("}");
        } else if (object instanceof ByteArrayType) {
            ByteArrayType ba = (ByteArrayType) object;
            byte[] data = ba.getData();
            return "{" + newLine
                    + indent(level + 1) + "\"type\": \"ByteArray\"," + newLine
                    + addId
                    + indent(level + 1) + "\"value\": \"" + Helper.byteArrayToHex(data) + "\"" + newLine
                    + indent(level) + "}";
        } else if (object instanceof DateType) {
            DateType dt = (DateType) object;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            return "{" + newLine
                    + indent(level + 1) + "\"type\": \"Date\"," + newLine
                    + addId
                    + indent(level + 1) + "\"value\": " + amfToString(indentStr, newLine, processedObjects, level, sdf.format(new Date((long) dt.getVal())), referenceCount, objectAlias) + newLine
                    + indent(level) + "}";

        } else if (object instanceof XmlDocType) {
            return "{" + newLine
                    + indent(level + 1) + "\"type\": \"XMLDocument\"," + newLine
                    + addId
                    + indent(level + 1) + "\"value\": " + amfToString(indentStr, newLine, processedObjects, level, ((XmlDocType) object).getData(), referenceCount, objectAlias) + newLine
                    + indent(level) + "}";
        } else if (object instanceof XmlType) {
            return "{" + newLine
                    + indent(level + 1) + "\"type\": \"XML\"," + newLine
                    + addId
                    + indent(level + 1) + "\"value\": " + amfToString(indentStr, newLine, processedObjects, level, ((XmlType) object).getData(), referenceCount, objectAlias) + newLine
                    + indent(level) + "}";
        } else {
            throw new IllegalArgumentException("Unsupported type: " + object.getClass());
        }

        return ret.toString();
    }

    public static String amfMapToString(Map<String, Object> map, String indentStr, String newLine, int level) {
        Map<Object, Integer> refCount = new HashMap<>();
        List<Object> objectList = new ArrayList<>();
        Map<Object, String> objectAlias = new HashMap<>();
        for (Object val : map.values()) {
            populateObjects(val, refCount, objectList, objectAlias);
        }
        List<Object> processedObjects = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("{").append(newLine);
        boolean first = true;
        for (String key : map.keySet()) {
            if (!first) {
                sb.append(", ").append(newLine);
            }
            first = false;
            sb.append(indent(level + 1)).append("\"").append(Helper.escapeActionScriptString(key)).append("\": ");
            sb.append(amfToString(indentStr, newLine, processedObjects, level + 1, map.get(key), refCount, objectAlias));
        }
        sb.append(newLine);
        sb.append(indent(level)).append("}");
        return sb.toString();
    }   
}
