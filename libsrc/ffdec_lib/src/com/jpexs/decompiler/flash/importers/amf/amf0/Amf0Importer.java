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
package com.jpexs.decompiler.flash.importers.amf.amf0;

import com.jpexs.decompiler.flash.amf.amf0.types.ArrayType;
import com.jpexs.decompiler.flash.amf.amf0.types.BasicType;
import com.jpexs.decompiler.flash.amf.amf0.types.DateType;
import com.jpexs.decompiler.flash.amf.amf0.types.EcmaArrayType;
import com.jpexs.decompiler.flash.amf.amf0.types.ObjectType;
import com.jpexs.decompiler.flash.amf.amf0.types.TypedObjectType;
import com.jpexs.decompiler.flash.amf.amf0.types.XmlDocumentType;
import com.jpexs.decompiler.flash.importers.amf.AmfLexer;
import com.jpexs.decompiler.flash.importers.amf.AmfParseException;
import com.jpexs.decompiler.flash.importers.amf.ParsedSymbol;
import com.jpexs.decompiler.flash.importers.amf.SymbolType;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * AMF0 importer.
 */
public class Amf0Importer {

    private AmfLexer lexer;

    /**
     * Constructor.
     */
    public Amf0Importer() {
    }

    private ParsedSymbol lex() throws IOException, AmfParseException {
        ParsedSymbol ret = lexer.lex();
        return ret;
    }

    private void pushback(ParsedSymbol s) {
        lexer.pushback(s);
    }

    private void expected(ParsedSymbol symb, int line, Object... expected) throws IOException, AmfParseException {
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
            throw new AmfParseException("" + expStr + " expected but " + symb.type + " found", line);
        }
    }

    private ParsedSymbol expectedType(Object... type) throws IOException, AmfParseException {
        ParsedSymbol symb = lex();
        expected(symb, lexer.yyline(), type);
        return symb;
    }

    private JsArray parseArray(Map<String, Object> objectTable) throws IOException, AmfParseException {
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

        private final Map<Object, Object> values = new LinkedHashMap<>();

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

        public String getString(Object key) throws AmfParseException {
            return (String) getRequired(key, "String");
        }

        public Boolean getBoolean(Object key) throws AmfParseException {
            return (Boolean) getRequired(key, "Boolean");
        }

        public JsObject getJsObject(Object key) throws AmfParseException {
            return (JsObject) getRequired(key, "JsObject");
        }

        public List<Object> getJsArrayOfObject(Object key) throws AmfParseException {
            return getJsArray(key).getValues();
        }

        @SuppressWarnings("unchecked")
        public List<String> getJsArrayOfString(Object key) throws AmfParseException {
            return (List<String>) getJsArray(key, "String");
        }

        @SuppressWarnings("unchecked")
        public List<Long> getJsArrayOfInt(Object key) throws AmfParseException {
            return (List<Long>) getJsArray(key, "int");
        }

        @SuppressWarnings("unchecked")
        public List<Long> getJsArrayOfUint(Object key) throws AmfParseException {
            return (List<Long>) getJsArray(key, "uint");
        }

        @SuppressWarnings("unchecked")
        public List<Double> getJsArrayOfNumber(Object key) throws AmfParseException {
            return (List<Double>) getJsArray(key, "Number");
        }

        public JsArray getJsArray(Object key) throws AmfParseException {
            return (JsArray) getRequired(key, "JsArray");
        }

        public List getJsArray(Object key, String valueType) throws AmfParseException {
            JsArray jsArr = (JsArray) getRequired(key, "JsArray");
            switch (valueType) {
                case "String":
                    List<String> stringList = new ArrayList<>();
                    for (Object v : jsArr.getValues()) {
                        String sv = null;
                        if (v instanceof String) {
                            sv = (String) v;
                        } else {
                            throw new AmfParseException("Not String: " + v, 0);
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
                            throw new AmfParseException("Not an Integer value: " + v, 0);
                        }
                        if (valueType.equals("uint") && lv < 0) {
                            throw new AmfParseException("Not an Unsigned Integer value: " + v, 0);
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
                            throw new AmfParseException("Not a Number: " + v, 0);
                        }
                        doubleList.add(cv);
                    }
                    return doubleList;
                default:
                    throw new AmfParseException("Unsupported array value type: " + valueType, 0);
            }
        }

        public Long getLong(Object key) throws AmfParseException {
            return (Long) getRequired(key, "Long");
        }
        
        public Double getDouble(Object key) throws AmfParseException {
            return (Double) getRequired(key, "Double");
        }

        public Object getRequired(Object key, String requiredType) throws AmfParseException {
            if (!containsKey(key)) {
                throw new AmfParseException("\"" + key + "\" is missing", 0);
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
                throw new AmfParseException("\"" + key + "\" value must be of type " + requiredType, 0);
            }
            return val;
        }

        public boolean containsKey(Object key) {
            return values.containsKey(key);
        }

        public void resolve(Object key, Map<String, Object> objectTable, boolean allowTypedObject) throws AmfParseException {
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
            Map<String, Object> ret = new LinkedHashMap<>();
            for (Object key : values.keySet()) {
                if (key instanceof String) {
                    String keyStr = (String) key;
                    ret.put(keyStr, values.get(key));
                }
            }
            return ret;
        }
    }

    private JsObject parseObject(Map<String, Object> objectTable) throws IOException, AmfParseException {
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
                s = lex();
            } while (s.isType(SymbolType.COMMA));
        }
        pushback(s);
        expectedType(SymbolType.CURLY_CLOSE);
        return ret;
    }

    private Object resolveObjects(Object object, Map<String, Object> objectTable, boolean allowTypedObject) throws AmfParseException {
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
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                            String dateStr = typedObject.getString("value");
                            int timeZone = (int) (long) (double) typedObject.getDouble("timezone");
                            try {
                                resultObject = new DateType((double) sdf.parse(dateStr).getTime(), timeZone);
                            } catch (ParseException ex) {
                                throw new AmfParseException("Invalid date format: " + dateStr, lexer.yyline());
                            }
                            break;
                        case "XMLDocument":
                            resultObject = new XmlDocumentType(typedObject.getString("value"));
                            break;
                        case "Object":
                            ObjectType ot = new ObjectType();
                            typedObject.resolve("members", objectTable, false);                            
                            ot.properties = typedObject.getJsObject("members").getStringMapped();
                            resultObject = ot;
                            break;
                        case "TypedObject":
                            TypedObjectType tot = new TypedObjectType();
                            tot.className = typedObject.getString("className");
                            typedObject.resolve("members", objectTable, false);
                            tot.properties = typedObject.getJsObject("members").getStringMapped();
                            resultObject = tot;
                            break;
                        case "EcmaArray":
                            EcmaArrayType eat = new EcmaArrayType();
                            typedObject.resolve("members", objectTable, false);
                            eat.denseValues = typedObject.getJsObject("denseValues").getStringMapped();
                            eat.associativeValues = typedObject.getJsObject("associativeValues").getStringMapped();
                            resultObject = eat;
                            break;
                        case "Array":
                            ArrayType at = new ArrayType();
                            typedObject.resolve("values", objectTable, false);                            
                            at.values = typedObject.getJsArray("values").getValues();
                            resultObject = at;
                            break;
                        case "Reference":
                            ReferencedObjectType ref = new ReferencedObjectType(typedObject.getString("referencedId"));
                            resultObject = ref;
                            break;
                        case "Undefined":
                            resultObject = BasicType.UNDEFINED;
                            break;
                        default:
                            throw new AmfParseException("Unknown object type: " + typeStr, lexer.yyline());
                    }
                    if (id != null) {
                        objectTable.put(id, resultObject);
                    }
                }
            } else { //not allowTypeObject
                JsObject jsObject = (JsObject) object;
                for (Object key : jsObject.keySet()) {
                    Object val = jsObject.get(key);
                    //Object resKey = resolveObjects(key, objectTable, true);
                    Object resVal = resolveObjects(val, objectTable, true);
                    //jsObject.remove(key);
                    jsObject.put(key, resVal);
                }
                resultObject = jsObject;
            }
        }
        return resultObject;
    }

    private Map<String, Object> map(Map<String, Object> objectTable) throws IOException, AmfParseException {
        Map<String, Object> result = new LinkedHashMap<>();
        expectedType(SymbolType.CURLY_OPEN);
        ParsedSymbol s;
        do {
            s = lex();
            if (!s.isType(SymbolType.STRING)) {
                break;
            }
            String key = (String) s.value;
            expectedType(SymbolType.COLON);
            result.put(key, value(objectTable));
            s = lex();
        } while (s.type == SymbolType.COMMA);

        expected(s, lexer.yyline(), SymbolType.CURLY_CLOSE);
        return result;
    }

    private Object value(Map<String, Object> objectTable) throws IOException, AmfParseException {
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
                return s.value;
            case INTEGER:
                return (double) (long) (Long) s.value;
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
                throw new AmfParseException("Unexpected symbol: " + s, lexer.yyline());
        }
    }

    /**
     * Deeply replace all ReferencedObjectType with the correct value
     *
     * @param object Object
     * @param objectsTable Objects table
     * @return Replaced object
     */
    private Object replaceReferences(Object object, Map<String, Object> objectsTable) throws AmfParseException {
        if (object instanceof ReferencedObjectType) {
            String key = ((ReferencedObjectType) object).key;
            if (!objectsTable.containsKey(key)) {
                throw new AmfParseException("Reference to undefined object: #" + key, 0);
            }
            return objectsTable.get(key);
        } else if (object instanceof ObjectType) {
            ObjectType ot = (ObjectType) object;
            for (String key : ot.properties.keySet()) {
                ot.properties.put(key, replaceReferences(ot.properties.get(key), objectsTable));
            }
        } else if (object instanceof TypedObjectType) {
            TypedObjectType tot = (TypedObjectType) object;
            for (String key : tot.properties.keySet()) {
                tot.properties.put(key, replaceReferences(tot.properties.get(key), objectsTable));
            }
        } else if (object instanceof EcmaArrayType) {
            EcmaArrayType eat = (EcmaArrayType) object;
            for (String key : eat.denseValues.keySet()) {
                eat.denseValues.put(key, replaceReferences(eat.denseValues.get(key), objectsTable));
            }
            for (String key : eat.associativeValues.keySet()) {
                eat.associativeValues.put(key, replaceReferences(eat.associativeValues.get(key), objectsTable));
            }
        } else if (object instanceof ArrayType) {
            ArrayType at = (ArrayType) object;
            for (int i = 0; i < at.values.size(); i++) {
                at.values.set(i, replaceReferences(at.values.get(i), objectsTable));
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

    /**
     * Convert AMF3 string to object
     *
     * @param val AMF3 string
     * @return Object
     * @throws IOException On I/O error
     * @throws AmfParseException On parse error
     */
    public Object stringToAmf(String val) throws IOException, AmfParseException {
        lexer = new AmfLexer(new StringReader(val));
        Map<String, Object> objectsTable = new LinkedHashMap<>();
        List<ReferencedObjectType> references = new ArrayList<>();
        Object result = value(objectsTable);
        Object resultResolved = resolveObjects(result, objectsTable, true);
        Object resultNoRef = replaceReferences(resultResolved, objectsTable);
        return resultNoRef;
    }

    /**
     * Convert AMF3 map string to object
     *
     * @param val AMF3 string
     * @return Object
     * @throws IOException On I/O error
     * @throws AmfParseException On parse error
     */
    public Map<String, Object> stringToAmfMap(String val) throws IOException, AmfParseException {
        lexer = new AmfLexer(new StringReader(val));
        Map<String, Object> objectsTable = new LinkedHashMap<>();
        List<ReferencedObjectType> references = new ArrayList<>();
        Map<String, Object> result = map(objectsTable);
        for (String key : result.keySet()) {
            Object resultResolved = resolveObjects(result.get(key), objectsTable, true);
            result.put(key, resultResolved);
        }

        for (String key : result.keySet()) {
            Object resultNoRef = replaceReferences(result.get(key), objectsTable);
            result.put(key, resultNoRef);
        }

        return result;
    }
}
