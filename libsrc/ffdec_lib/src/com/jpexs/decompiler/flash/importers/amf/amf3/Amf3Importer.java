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
package com.jpexs.decompiler.flash.importers.amf.amf3;

import com.jpexs.decompiler.flash.amf.amf3.ListMap;
import com.jpexs.decompiler.flash.amf.amf3.Traits;
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
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Amf3Importer {

    private Amf3Lexer lexer;

    private ParsedSymbol lex() throws IOException, Amf3ParseException {
        ParsedSymbol ret = lexer.lex();
        return ret;
    }

    private void pushback(ParsedSymbol s) {
        lexer.pushback(s);
    }

    private void expected(ParsedSymbol symb, int line, Object... expected) throws IOException, Amf3ParseException {
        boolean found = false;
        for (Object t : expected) {
            if (symb.type == t) {
                found = true;
            }
            if (symb.group == t) {
                found = true;
            }
        }
        if (!found) {
            String expStr = "";
            boolean first = true;
            for (Object e : expected) {
                if (!first) {
                    expStr += " or ";
                }
                expStr += e;
                first = false;
            }
            throw new Amf3ParseException("" + expStr + " expected but " + symb.type + " found", line);
        }
    }

    private ParsedSymbol expectedType(Object... type) throws IOException, Amf3ParseException {
        ParsedSymbol symb = lex();
        expected(symb, lexer.yyline(), type);
        return symb;
    }

    private JsArray parseArray(Map<String, Object> objectTable) throws IOException, Amf3ParseException {
        expectedType(SymbolType.BRACKET_OPEN);
        List<Object> arrayVals = new ArrayList<>();
        ParsedSymbol s = lex();
        if (!s.isType(SymbolType.BRACKET_CLOSE)) {
            pushback(s);
            arrayVals.add(value(objectTable));
            s = lex();
            while (s.isType(SymbolType.COMMA)) {
                arrayVals.add(value(objectTable));
                s = lex();
            }
            pushback(s);
        }
        expectedType(SymbolType.BRACKET_CLOSE);
        return new JsArray(arrayVals);
    }

    private class JsArray {

        private List<Object> values = new ArrayList<>();

        public JsArray() {
        }

        public JsArray(List<Object> values) {
            this.values = values;
        }

        public void add(Object value) {
            values.add(value);
        }

        public List<Object> getValues() {
            return values;
        }

    }

    private class JsObject {

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            for (Object key : values.keySet()) {
                sb.append(key).append(":").append("?").append(",\r\n");
            }
            sb.append("}");
            return sb.toString();
        }

        private final Map<Object, Object> values = new ListMap<>();

        public Object remove(Object key) {
            return values.remove(key);
        }

        public Set<Object> keySet() {
            return values.keySet();
        }

        public Object get(Object key) {
            return values.get(key);
        }

        public void put(Object key, Object value) {
            values.put(key, value);
        }

        public String getString(Object key) throws Amf3ParseException {
            return (String) getRequired(key, "String");
        }

        public Boolean getBoolean(Object key) throws Amf3ParseException {
            return (Boolean) getRequired(key, "Boolean");
        }

        public JsObject getJsObject(Object key) throws Amf3ParseException {
            return (JsObject) getRequired(key, "JsObject");
        }

        public JsArray getJsArray(Object key) throws Amf3ParseException {
            return (JsArray) getRequired(key, "JsArray");
        }

        public List<Object> getJsArrayOfObject(Object key) throws Amf3ParseException {
            return getJsArray(key).getValues();
        }

        @SuppressWarnings("unchecked")
        public List<String> getJsArrayOfString(Object key) throws Amf3ParseException {
            return (List<String>) getJsArray(key, "String");
        }

        @SuppressWarnings("unchecked")
        public List<Long> getJsArrayOfInt(Object key) throws Amf3ParseException {
            return (List<Long>) getJsArray(key, "int");
        }

        @SuppressWarnings("unchecked")

        public List<Long> getJsArrayOfUint(Object key) throws Amf3ParseException {
            return (List<Long>) getJsArray(key, "uint");
        }

        @SuppressWarnings("unchecked")
        public List<Double> getJsArrayOfNumber(Object key) throws Amf3ParseException {
            return (List<Double>) getJsArray(key, "Number");
        }

        public List getJsArray(Object key, String valueType) throws Amf3ParseException {
            JsArray jsArr = (JsArray) getRequired(key, "JsArray");
            switch (valueType) {
                case "String":
                    List<String> stringList = new ArrayList<>();
                    for (Object v : jsArr.getValues()) {
                        String sv = null;
                        if (v instanceof String) {
                            sv = (String) v;
                        } else {
                            throw new Amf3ParseException("Not String: " + v, 0);
                        }
                        stringList.add(sv);
                    }
                    return stringList;
                case "int":
                case "uint":
                    List<Long> longList = new ArrayList<>();
                    for (Object v : jsArr.getValues()) {
                        Long lv = null;
                        if (v instanceof Long) {
                            lv = (Long) v;
                        } else {
                            throw new Amf3ParseException("Not an Integer value: " + v, 0);
                        }
                        if (valueType.equals("uint") && lv < 0) {
                            throw new Amf3ParseException("Not an Unsigned Integer value: " + v, 0);
                        }
                        longList.add(lv);
                    }
                    return longList;
                case "Number":
                    List<Double> doubleList = new ArrayList<>();
                    for (Object v : jsArr.getValues()) {
                        Double cv = null;
                        if (v instanceof Long) {
                            cv = (double) (long) (Long) v;
                        } else if (v instanceof Double) {
                            cv = (Double) v;
                        } else {
                            throw new Amf3ParseException("Not a Number: " + v, 0);
                        }
                        doubleList.add(cv);
                    }
                    return doubleList;
                default:
                    throw new Amf3ParseException("Unsupported array value type: " + valueType, 0);
            }
        }

        public Long getLong(Object key) throws Amf3ParseException {
            return (Long) getRequired(key, "Long");
        }

        public Object getRequired(Object key, String requiredType) throws Amf3ParseException {
            if (!containsKey(key)) {
                throw new Amf3ParseException("\"" + key + "\" is missing", 0);
            }
            Object val = get(key);
            boolean typeMatches = true;
            if (requiredType != null) {
                switch (requiredType) {
                    case "String":
                        typeMatches = val instanceof String;
                        break;
                    case "Long":
                        typeMatches = val instanceof Long;
                        break;
                    case "JsObject":
                        typeMatches = val instanceof JsObject;
                        break;
                    case "JsArray":
                        typeMatches = val instanceof JsArray;
                        break;
                    case "Boolean":
                        typeMatches = val instanceof Boolean;
                        break;
                }
            }
            if (!typeMatches) {
                throw new Amf3ParseException("\"" + key + "\" value must be of type " + requiredType, 0);
            }
            return val;
        }

        public boolean containsKey(Object key) {
            return values.containsKey(key);
        }

        public void resolve(Object key, Map<String, Object> objectTable, boolean allowTypedObject) throws Amf3ParseException {
            Object val = values.get(key);
            Object resolved = resolveObjects(val, objectTable, allowTypedObject);
            values.put(key, resolved);
        }

        public List<String> stringKeys() {
            List<String> ret = new ArrayList<>();
            for (Object key : values.keySet()) {
                if (key instanceof String) {
                    ret.add((String) key);
                }
            }
            return ret;
        }

        public Map<Object, Object> getAll() {
            return values;
        }

        public Map<String, Object> getStringMapped() {
            Map<String, Object> ret = new ListMap<>();
            for (Object key : values.keySet()) {
                if (key instanceof String) {
                    String keyStr = (String) key;
                    ret.put(keyStr, values.get(key));
                }
            }
            return ret;
        }
    }

    private JsObject parseObject(Map<String, Object> objectTable) throws IOException, Amf3ParseException {
        JsObject ret = new JsObject();

        expectedType(SymbolType.CURLY_OPEN);
        ParsedSymbol s = lex();
        if (!s.isType(SymbolType.CURLY_CLOSE)) {
            pushback(s);
            do {
                Object key = value(objectTable);
                expectedType(SymbolType.COLON);
                Object value = value(objectTable);
                ret.put(key, value);
                if ("id".equals(key)) {
                    if (!(value instanceof String)) {
                        throw new Amf3ParseException("id must be string value", lexer.yyline());
                    }
                    objectTable.put((String) value, BasicType.UNDEFINED);
                }
                s = lex();
            } while (s.isType(SymbolType.COMMA));
        }
        pushback(s);
        expectedType(SymbolType.CURLY_CLOSE);
        return ret;
    }

    private Object resolveObjects(Object object, Map<String, Object> objectTable, boolean allowTypedObject) throws Amf3ParseException {
        Object resultObject = object;
        if (object instanceof JsArray) {
            JsArray jsa = (JsArray) object;
            JsArray ret = new JsArray();
            for (int i = 0; i < jsa.values.size(); i++) {
                ret.values.add(resolveObjects(jsa.values.get(i), objectTable, true));
            }
            resultObject = ret;
        } else if (object instanceof JsObject) {
            if (allowTypedObject) {
                JsObject typedObject = (JsObject) object;
                if (typedObject.containsKey("type")) {
                    String typeStr = typedObject.getString("type");
                    String id = typedObject.containsKey("id") ? typedObject.getString("id") : null;
                    switch (typeStr) {
                        case "Date":
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS");
                            String dateStr = typedObject.getString("value");
                            try {
                                resultObject = new DateType((double) sdf.parse(dateStr).getTime());
                            } catch (ParseException ex) {
                                throw new Amf3ParseException("Invalid date format: " + dateStr, lexer.yyline());
                            }
                            break;
                        case "XML":
                            resultObject = new XmlType(typedObject.getString("value"));
                            break;
                        case "XMLDocument":
                            resultObject = new XmlDocType(typedObject.getString("value"));
                            break;
                        case "Object":
                            String className = typedObject.getString("className");
                            if (typedObject.containsKey("serialized")) {
                                //TODO
                            } else {
                                boolean dynamic = typedObject.getBoolean("dynamic");
                                typedObject.resolve("sealedMembers", objectTable, false);
                                JsObject jsoSealed = typedObject.getJsObject("sealedMembers");

                                Map<String, Object> sealedMembers = jsoSealed.getStringMapped();
                                typedObject.resolve("dynamicMembers", objectTable, false);
                                Map<String, Object> dynamicMembers = typedObject.getJsObject("dynamicMembers").getStringMapped();

                                List<String> sealedMemberNames = new ArrayList<>(jsoSealed.stringKeys());
                                resultObject = new ObjectType(new Traits(className, dynamic, sealedMemberNames), sealedMembers, dynamicMembers);
                            }
                            break;
                        case "Array":
                            typedObject.resolve("denseValues", objectTable, false);
                            List<Object> denseValues = typedObject.getJsArray("denseValues").getValues();
                            typedObject.resolve("associativeValues", objectTable, false);
                            JsObject resolvedArr = typedObject.getJsObject("associativeValues");
                            Map<String, Object> associativeValues = resolvedArr.getStringMapped();
                            resultObject = new ArrayType(denseValues, associativeValues);
                            break;

                        case "Vector":
                            boolean fixed = typedObject.getBoolean("fixed");
                            String subtype = typedObject.getString("subtype");
                            typedObject.resolve("values", objectTable, false);
                            switch (subtype) {
                                case "int":
                                    resultObject = new VectorIntType(fixed, typedObject.getJsArrayOfInt("values"));
                                    break;
                                case "uint":
                                    resultObject = new VectorUIntType(fixed, typedObject.getJsArrayOfUint("values"));
                                    break;
                                case "Number":
                                    resultObject = new VectorDoubleType(fixed, typedObject.getJsArrayOfNumber("values"));
                                    break;
                                default:
                                    resultObject = new VectorObjectType(fixed, subtype, typedObject.getJsArrayOfObject("values"));
                                    break;
                            }
                            break;
                        case "ByteArray":
                            try {
                                resultObject = new ByteArrayType(javax.xml.bind.DatatypeConverter.parseHexBinary(typedObject.getString("value")));
                            } catch (IllegalArgumentException iex) {
                                throw new Amf3ParseException("Invalid hex byte sequence", lexer.yyline());
                            }
                            break;
                        case "Dictionary":
                            boolean weakKeys = typedObject.getBoolean("weakKeys");
                            typedObject.resolve("entries", objectTable, false);
                            Map<Object, Object> entries = typedObject.getJsObject("entries").getAll();
                            resultObject = new DictionaryType(weakKeys, entries);
                            break;
                        default:
                            throw new Amf3ParseException("Unknown object type: " + typeStr, lexer.yyline());
                    }
                    if (id != null) {
                        objectTable.put(id, resultObject);
                    }
                }
            } else { //not allowTypeObject
                JsObject jsObject = (JsObject) object;
                for (Object key : jsObject.keySet()) {
                    Object val = jsObject.get(key);
                    Object resKey = resolveObjects(key, objectTable, true);
                    Object resVal = resolveObjects(val, objectTable, true);
                    jsObject.remove(key);
                    jsObject.put(resKey, resVal);
                }
                resultObject = jsObject;
            }
        }
        return resultObject;
    }

    private Object value(Map<String, Object> objectTable) throws IOException, Amf3ParseException {
        ParsedSymbol s = lex();
        switch (s.type) {
            case CURLY_OPEN:
                pushback(s);
                return parseObject(objectTable);
            case BRACKET_OPEN:
                pushback(s);
                return parseArray(objectTable);
            case STRING:
            case DOUBLE:
            case INTEGER:
                return s.value;
            case UNDEFINED:
                return BasicType.UNDEFINED;
            case NULL:
                return BasicType.NULL;
            case UNKNOWN:
                return BasicType.UNKNOWN;
            case TRUE:
                return Boolean.TRUE;
            case FALSE:
                return Boolean.FALSE;
            case REFERENCE:
                String referencedId = (String) s.value;
                return new ReferencedObjectType(referencedId);
            default:
                throw new Amf3ParseException("Unexpected symbol: " + s, lexer.yyline());
        }
    }

    /**
     * Deeply replace all ReferencedObjectType with the correct value
     *
     * @param object
     * @param objectsTable
     * @return
     */
    private Object replaceReferences(Object object, Map<String, Object> objectsTable) throws Amf3ParseException {
        if (object instanceof ReferencedObjectType) {
            String key = ((ReferencedObjectType) object).key;
            if (!objectsTable.containsKey(key)) {
                throw new Amf3ParseException("Reference to undefined object: #" + key, 0);
            }
            return objectsTable.get(key);
        } else if (object instanceof ObjectType) {
            ObjectType ot = (ObjectType) object;
            for (String key : ot.sealedMembersKeySet()) {
                ot.putSealedMember(key, replaceReferences(ot.getSealedMember(key), objectsTable));
            }
            for (String key : ot.dynamicMembersKeySet()) {
                ot.putDynamicMember(key, replaceReferences(ot.getDynamicMember(key), objectsTable));
            }
            for (String key : ot.serializedMembersKeySet()) {
                ot.putSerializedMember(key, replaceReferences(ot.getSerializedMember(key), objectsTable));
            }

        } else if (object instanceof ArrayType) {
            ArrayType at = (ArrayType) object;
            for (String key : at.associativeKeySet()) {
                at.putAssociative(key, replaceReferences(at.getAssociative(key), objectsTable));
            }
            for (int i = 0; i < at.getDenseValues().size(); i++) {
                at.setDense(i, replaceReferences(at.getDense(i), objectsTable));
            }
        } else if (object instanceof VectorObjectType) {
            VectorObjectType vot = (VectorObjectType) object;
            for (int i = 0; i < vot.getValues().size(); i++) {
                vot.getValues().set(i, replaceReferences(vot.getValues().get(i), objectsTable));
            }
        } else if (object instanceof DictionaryType) {
            DictionaryType dt = (DictionaryType) object;
            for (Object key : dt.keySet()) {
                Object val = dt.get(key);
                Object newKey = replaceReferences(key, objectsTable);
                Object newVal = replaceReferences(val, objectsTable);
                dt.remove(key);
                dt.put(newKey, newVal);
            }
        }

        return object;
    }

    private class ReferencedObjectType {

        private final String key;

        public ReferencedObjectType(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        @Override
        public String toString() {
            return "#" + key;
        }
    }

    public Object stringToAmf(String val) throws IOException, Amf3ParseException {
        lexer = new Amf3Lexer(new StringReader(val));
        Map<String, Object> objectsTable = new HashMap<>();
        List<ReferencedObjectType> references = new ArrayList<>();
        Object result = value(objectsTable);
        Object resultResolved = resolveObjects(result, objectsTable, true);
        Object resultNoRef = replaceReferences(resultResolved, objectsTable);
        return resultNoRef;
    }
}
