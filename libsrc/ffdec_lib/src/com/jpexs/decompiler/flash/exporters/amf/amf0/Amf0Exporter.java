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
package com.jpexs.decompiler.flash.exporters.amf.amf0;

import com.jpexs.decompiler.flash.amf.amf0.types.ArrayType;
import com.jpexs.decompiler.flash.amf.amf0.types.BasicType;
import com.jpexs.decompiler.flash.amf.amf0.types.DateType;
import com.jpexs.decompiler.flash.amf.amf0.types.EcmaArrayType;
import com.jpexs.decompiler.flash.amf.amf0.types.ObjectType;
import com.jpexs.decompiler.flash.amf.amf0.types.TypedObjectType;
import com.jpexs.decompiler.flash.amf.amf0.types.XmlDocumentType;
import com.jpexs.helpers.Helper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.jpexs.decompiler.flash.amf.amf0.types.ComplexObject;
import com.jpexs.decompiler.flash.amf.amf0.types.ReferenceType;
import com.jpexs.decompiler.flash.ecma.EcmaScript;

/**
 * AMF0 exporter.
 *
 * @author JPEXS
 */
public class Amf0Exporter {

    public static String amfMapToString(
            Map<String, Object> map,
            int level, 
            String newLine
            ) {
        List<Object> processedObjects = new ArrayList<>();
        Map<Object, Integer> referenceCount = new LinkedHashMap<>();
        Map<Object, String> objectAlias = new LinkedHashMap<>();
        
        List<Object> objectList = new ArrayList<>();
        for (String key: map.keySet()) {
            Object val = map.get(key);
            populateObjects(val, referenceCount, objectList, objectAlias);
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("{").append(newLine);
        boolean first = true;
        for (String key: map.keySet()) {
            if (!first) {
                sb.append(",").append(newLine);
            }
            first = false;
            sb.append(indent(level + 1)).append("\"").append(Helper.escapeActionScriptString(key)).append("\": ");
            sb.append(amfToString(map.get(key), level + 1, newLine, processedObjects, referenceCount, objectAlias));
        }
        sb.append(newLine);
        sb.append(indent(level)).append("}");
        return sb.toString();
    }
        
    private static String referenceToString(String objectAlias, int level, String newLine) {
        StringBuilder sb = new StringBuilder();
        sb.append("{").append(newLine);
        sb.append(indent(level + 1)).append("\"type\": \"Reference\",").append(newLine);
        sb.append(indent(level + 1)).append("\"referencedId\": \"").append(objectAlias).append("\"").append(newLine);
        sb.append(indent(level)).append("}");
        return sb.toString();
    }
    
    public static String amfToString(
            Object value,
            int level, 
            String newLine,
            List<Object> processedObjects,
            Map<Object, Integer> referenceCount,
            Map<Object, String> objectAlias
    ) {                  
        String addId = "";
        if (referenceCount.containsKey(value)) {
            Integer refCount = referenceCount.get(value);
            if (refCount > 1 && processedObjects.contains(value)) {
                return referenceToString(objectAlias.get(value), level, newLine);
            }
            if (refCount > 1) {
                addId = indent(level + 1) + "\"id\": \"" + objectAlias.get(value) + "\"," + newLine;
            }
            processedObjects.add(value);
        }
        
        if (value instanceof ReferenceType) {
            ReferenceType rt = (ReferenceType) value;
            return referenceToString("obj" + rt.referenceIndex, level, newLine);
        }
        
        if (value == BasicType.UNDEFINED) {
            StringBuilder sb = new StringBuilder();
            sb.append("{").append(newLine);
            sb.append(indent(level + 1)).append("\"type\": \"Undefined\"").append(newLine);
            sb.append(indent(level)).append("}");
            return sb.toString();
        }
        
        if (value instanceof Double) {
            return EcmaScript.toString(value);
        }
        if (value instanceof Boolean) {
            return value.toString();
        }
        if (value instanceof String) {
            return "\"" + Helper.escapeActionScriptString((String) value) + "\"";
        }
        if (value instanceof ObjectType) {
            ObjectType ot = (ObjectType) value;
            StringBuilder sb = new StringBuilder();
            sb.append("{").append(newLine);
            sb.append(indent(level + 1)).append("\"type\": \"Object\",").append(newLine);
            sb.append(addId);
            membersToString("members", sb, ot.properties, level + 1, newLine, processedObjects, referenceCount, objectAlias);
            sb.append(newLine);
            sb.append(indent(level)).append("}");
            return sb.toString();
        }
        if (value instanceof EcmaArrayType) {
            EcmaArrayType eat = (EcmaArrayType) value;
            StringBuilder sb = new StringBuilder();
            sb.append("{").append(newLine);
            sb.append(indent(level + 1)).append("\"type\": \"EcmaArray\",").append(newLine);
            sb.append(addId);
            membersToString("denseValues", sb, eat.denseValues, level + 1, newLine, processedObjects, referenceCount, objectAlias);
            sb.append(",").append(newLine);
            membersToString("associativeValues", sb, eat.associativeValues, level + 1, newLine, processedObjects, referenceCount, objectAlias);
            sb.append(newLine);
            sb.append(indent(level)).append("}");
            return sb.toString();
        }

        if (value instanceof ArrayType) {
            ArrayType at = (ArrayType) value;
            StringBuilder sb = new StringBuilder();
            sb.append("{").append(newLine);
            sb.append(indent(level + 1)).append("\"type\": \"Array\",").append(newLine);
            sb.append(addId);            
            sb.append(indent(level + 1)).append("\"values\": [").append(newLine);
            boolean first = true;
            for (Object val : at.values) {
                if (!first) {
                    sb.append(", ");
                }
                first = false;
                sb.append(amfToString(val, level + 1, newLine, processedObjects, referenceCount, objectAlias));
            }
            sb.append(indent(level + 1)).append("]").append(newLine);

            sb.append(indent(level)).append("}");
            return sb.toString();
        }

        if (value instanceof TypedObjectType) {
            TypedObjectType tot = (TypedObjectType) value;
            StringBuilder sb = new StringBuilder();
            sb.append("{").append(newLine);
            sb.append(indent(level + 1)).append("\"type\": \"TypedObject\",").append(newLine);
            sb.append(addId);            
            sb.append(indent(level + 1)).append("\"className\": \"").append(Helper.escapeActionScriptString(tot.className)).append("\",").append(newLine);
            membersToString("members", sb, tot.properties, level + 1, newLine, processedObjects, referenceCount, objectAlias);
            sb.append(newLine);
            sb.append(indent(level)).append("}");
            return sb.toString();
        }

        if (value instanceof BasicType) {
            return value.toString();
        }

        if (value instanceof DateType) {
            DateType dt = (DateType) value;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            StringBuilder sb = new StringBuilder();
            sb.append("{").append(newLine);
            sb.append(indent(level + 1)).append("\"type\": \"Date\",").append(newLine);
            sb.append(addId);
            sb.append(indent(level + 1)).append("\"value\": \"").append(sdf.format(dt.toDate())).append("\",").append(newLine);
            sb.append(indent(level + 1)).append("\"timezone\": ").append(dt.getTimezone()).append(newLine);            
            sb.append(indent(level)).append("}");
            return sb.toString();
        }

        if (value instanceof XmlDocumentType) {
            XmlDocumentType xdt = (XmlDocumentType) value;
            StringBuilder sb = new StringBuilder();
            sb.append("{").append(newLine);
            sb.append(indent(level + 1)).append("\"type\": \"XMLDocument\",").append(newLine);
            sb.append(addId);
            sb.append(indent(level + 1)).append("\"value\": \"").append(Helper.escapeActionScriptString(xdt.getData())).append("\"").append(newLine);
            sb.append(indent(level)).append("}");
            return sb.toString();
        }
        
        return "unknown";
    }

    private static void membersToString(
            String membersLabel,
            StringBuilder sb,
            Map<String, Object> members,
            int level, 
            String newLine,
            List<Object> processedObjects,
            Map<Object, Integer> referenceCount,
            Map<Object, String> objectAlias) {
        sb.append(indent(level)).append("\"").append(membersLabel).append("\": {").append(newLine);
        boolean first = true;
        for (String key : members.keySet()) {
            if (!first) {
                sb.append(",").append(newLine);
            }
            first = false;
            sb.append(indent(level + 1)).append("\"").append(Helper.escapeActionScriptString(key)).append("\": ");
            sb.append(amfToString(members.get(key), level + 1, newLine, processedObjects, referenceCount, objectAlias));
        }
        sb.append(newLine);
        sb.append(indent(level)).append("}");

    }

    private static String indent(int level) {
        String na = "";
        for (int i = 0; i < level; i++) {
            na += "  ";
        }
        return na;
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
        if (((List<? extends Class>) Arrays.asList(String.class, Double.class, BasicType.class, Boolean.class)).contains(object.getClass())) {
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
            if (object instanceof ComplexObject) {
                objectAlias.put(object, "obj" + objectList.size());
                objectList.add(object);            
                List<Object> subvalues = ((ComplexObject) object).getSubValues();
                for (Object o : subvalues) {
                    populateObjects(o, referenceCount, objectList, objectAlias);
                }
            }            
        }
    }
}
