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
package com.jpexs.decompiler.flash.importers;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ABCVersion;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.types.ABCException;
import com.jpexs.decompiler.flash.abc.types.ClassInfo;
import com.jpexs.decompiler.flash.abc.types.InstanceInfo;
import com.jpexs.decompiler.flash.abc.types.MetadataInfo;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.NamespaceSet;
import com.jpexs.decompiler.flash.abc.types.ScriptInfo;
import com.jpexs.decompiler.flash.abc.types.ValueKind;
import com.jpexs.decompiler.flash.abc.types.traits.TraitClass;
import com.jpexs.decompiler.flash.abc.types.traits.TraitFunction;
import com.jpexs.decompiler.flash.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
import com.jpexs.decompiler.flash.amf.amf3.Amf3Value;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.TagTypeInfo;
import com.jpexs.decompiler.flash.tags.UnknownTag;
import com.jpexs.decompiler.flash.types.ALPHABITMAPDATA;
import com.jpexs.decompiler.flash.types.ALPHACOLORMAPDATA;
import com.jpexs.decompiler.flash.types.ARGB;
import com.jpexs.decompiler.flash.types.BITMAPDATA;
import com.jpexs.decompiler.flash.types.BUTTONCONDACTION;
import com.jpexs.decompiler.flash.types.BUTTONRECORD;
import com.jpexs.decompiler.flash.types.CLIPACTIONRECORD;
import com.jpexs.decompiler.flash.types.CLIPACTIONS;
import com.jpexs.decompiler.flash.types.CLIPEVENTFLAGS;
import com.jpexs.decompiler.flash.types.COLORMAPDATA;
import com.jpexs.decompiler.flash.types.CXFORM;
import com.jpexs.decompiler.flash.types.CXFORMWITHALPHA;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.FILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.FOCALGRADIENT;
import com.jpexs.decompiler.flash.types.GLYPHENTRY;
import com.jpexs.decompiler.flash.types.GRADIENT;
import com.jpexs.decompiler.flash.types.GRADRECORD;
import com.jpexs.decompiler.flash.types.KERNINGRECORD;
import com.jpexs.decompiler.flash.types.LANGCODE;
import com.jpexs.decompiler.flash.types.LINESTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.LINESTYLEARRAY;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.MORPHFILLSTYLE;
import com.jpexs.decompiler.flash.types.MORPHFILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.MORPHFOCALGRADIENT;
import com.jpexs.decompiler.flash.types.MORPHGRADIENT;
import com.jpexs.decompiler.flash.types.MORPHGRADRECORD;
import com.jpexs.decompiler.flash.types.MORPHLINESTYLE;
import com.jpexs.decompiler.flash.types.MORPHLINESTYLE2;
import com.jpexs.decompiler.flash.types.MORPHLINESTYLEARRAY;
import com.jpexs.decompiler.flash.types.PIX15;
import com.jpexs.decompiler.flash.types.PIX24;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
import com.jpexs.decompiler.flash.types.SOUNDENVELOPE;
import com.jpexs.decompiler.flash.types.SOUNDINFO;
import com.jpexs.decompiler.flash.types.TEXTRECORD;
import com.jpexs.decompiler.flash.types.ZONEDATA;
import com.jpexs.decompiler.flash.types.ZONERECORD;
import com.jpexs.decompiler.flash.types.filters.BEVELFILTER;
import com.jpexs.decompiler.flash.types.filters.BLURFILTER;
import com.jpexs.decompiler.flash.types.filters.COLORMATRIXFILTER;
import com.jpexs.decompiler.flash.types.filters.CONVOLUTIONFILTER;
import com.jpexs.decompiler.flash.types.filters.DROPSHADOWFILTER;
import com.jpexs.decompiler.flash.types.filters.GLOWFILTER;
import com.jpexs.decompiler.flash.types.filters.GRADIENTBEVELFILTER;
import com.jpexs.decompiler.flash.types.filters.GRADIENTGLOWFILTER;
import com.jpexs.decompiler.flash.types.gfx.ContourType;
import com.jpexs.decompiler.flash.types.gfx.EdgeType;
import com.jpexs.decompiler.flash.types.gfx.FONTINFO;
import com.jpexs.decompiler.flash.types.gfx.FontType;
import com.jpexs.decompiler.flash.types.gfx.GLYPHIDX;
import com.jpexs.decompiler.flash.types.gfx.GlyphInfoType;
import com.jpexs.decompiler.flash.types.gfx.GlyphType;
import com.jpexs.decompiler.flash.types.gfx.KerningPairType;
import com.jpexs.decompiler.flash.types.gfx.TEXGLYPH;
import com.jpexs.decompiler.flash.types.shaperecords.CurvedEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.EndShapeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.HashArrayList;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.ReflectionTools;
import com.jpexs.helpers.utf8.Utf8InputStreamReader;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import macromedia.asc.util.Decimal128;

/**
 * SWF XML importer.
 *
 * @author JPEXS
 */
@SuppressWarnings("unchecked")
public class SwfXmlImporter {

    /**
     * Maximum XML import version major.
     */
    public static final int MAX_XML_IMPORT_VERSION_MAJOR = 2;

    private static final Logger logger = Logger.getLogger(SwfXmlImporter.class.getName());

    private static final Map<String, Class> swfTags;

    private static final Map<String, Class> swfObjects;

    private static final Map<String, Class> swfObjectsParam;

    static {
        Map<String, Class> tags = new HashMap<>();
        Map<Integer, TagTypeInfo> knownTags = Tag.getKnownClasses();
        for (Integer key : knownTags.keySet()) {
            Class cls = knownTags.get(key).getCls();
            if (!ReflectionTools.canInstantiate(cls)) {
                System.err.println("Can't instantiate: " + cls.getName());
            }
            tags.put(cls.getSimpleName(), cls);
        }

        swfTags = tags;

        Map<String, Class> objects = new HashMap<>();
        Class[] knownObjects = new Class[]{ALPHABITMAPDATA.class, ALPHACOLORMAPDATA.class, ARGB.class, BITMAPDATA.class,
            BUTTONCONDACTION.class, BUTTONRECORD.class, CLIPACTIONRECORD.class, CLIPACTIONS.class, CLIPEVENTFLAGS.class,
            COLORMAPDATA.class, ColorTransform.class, CXFORM.class, CXFORMWITHALPHA.class,
            FILLSTYLE.class, FILLSTYLEARRAY.class, FOCALGRADIENT.class, GLYPHENTRY.class, GRADIENT.class, GRADRECORD.class,
            KERNINGRECORD.class, LANGCODE.class, LINESTYLE.class, LINESTYLE2.class, LINESTYLEARRAY.class, MATRIX.class,
            MORPHFILLSTYLE.class, MORPHFILLSTYLEARRAY.class, MORPHFOCALGRADIENT.class, MORPHGRADIENT.class,
            MORPHGRADRECORD.class, MORPHLINESTYLE.class, MORPHLINESTYLE2.class, MORPHLINESTYLEARRAY.class, PIX15.class,
            PIX24.class, RECT.class, RGB.class, RGBA.class, SHAPE.class, SHAPEWITHSTYLE.class, SOUNDENVELOPE.class,
            SOUNDINFO.class, TEXTRECORD.class, ZONEDATA.class, ZONERECORD.class,
            CurvedEdgeRecord.class, EndShapeRecord.class, StraightEdgeRecord.class, StyleChangeRecord.class,
            BEVELFILTER.class, BLURFILTER.class, COLORMATRIXFILTER.class, CONVOLUTIONFILTER.class,
            DROPSHADOWFILTER.class, GLOWFILTER.class, GRADIENTBEVELFILTER.class, GRADIENTGLOWFILTER.class,
            AVM2ConstantPool.class, Namespace.class, NamespaceSet.class, Multiname.class, MethodInfo.class, MetadataInfo.class,
            ValueKind.class, InstanceInfo.class, Traits.class, TraitClass.class, TraitFunction.class,
            TraitMethodGetterSetter.class, TraitSlotConst.class, ClassInfo.class, ScriptInfo.class, MethodBody.class,
            ABCException.class, ABCVersion.class, Amf3Value.class,
            //GFX:
            ContourType.class, EdgeType.class, FONTINFO.class, FontType.class, GLYPHIDX.class,
            GlyphInfoType.class, GlyphType.class, KerningPairType.class, TEXGLYPH.class
        };

        for (Class cls2 : knownObjects) {
            if (!ReflectionTools.canInstantiateDefaultConstructor(cls2)) {
                System.err.println("Can't instantiate: " + cls2.getName());
            }
            objects.put(cls2.getSimpleName(), cls2);
        }
        
        swfObjects = objects;

        Map<String, Class> objectsParam = new HashMap<>();
        Class[] knownObjectsParam = new Class[]{ABC.class};
        for (Class cls2 : knownObjectsParam) {
            if (!ReflectionTools.canInstantiate(cls2)) {
                System.err.println("Can't instantiate: " + cls2.getName());
            }
            objectsParam.put(cls2.getSimpleName(), cls2);
        }

        swfObjectsParam = objectsParam;
    }

    private boolean isList(Class cls) {
        return cls != null && (cls.isArray() || List.class.isAssignableFrom(cls));
    }

    /**
     * Imports SWF from input stream.
     * @param swf SWF object
     * @param in Input stream
     * @throws IOException On I/O error
     */
    public void importSwf(SWF swf, InputStream in) throws IOException {
        XMLInputFactory xmlFactory = XMLInputFactory.newInstance();

        try {
            try (Reader reader = new Utf8InputStreamReader(new BufferedInputStream(in))) {
                XMLStreamReader xmlReader = xmlFactory.createXMLStreamReader(reader);

                xmlReader.nextTag();
                xmlReader.require(XMLStreamConstants.START_ELEMENT, null, "swf");

                processElement(xmlReader, swf, swf, null, MAX_XML_IMPORT_VERSION_MAJOR);
            }

            swf.clearAllCache();
            setSwfAndTimelined(swf);
        } catch (XMLStreamException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    private void setSwfAndTimelined(SWF swf) {
        for (Tag t : swf.getTags()) {
            t.setSwf(swf);
            t.setTimelined(swf);
            if (t instanceof DefineSpriteTag) {
                DefineSpriteTag s = (DefineSpriteTag) t;
                for (Tag st : s.getTags()) {
                    st.setSwf(swf);
                    st.setTimelined(s);
                }
            }
        }
    }

    /**
     * Imports object from XML string.
     * @param xml XML string
     * @param requiredType Required type
     * @param swf SWF object
     * @return Imported object
     * @throws IOException On I/O error
     */
    public Object importObject(String xml, Class requiredType, SWF swf) throws IOException {
        XMLInputFactory xmlFactory = XMLInputFactory.newInstance();
        try {
            XMLStreamReader reader = xmlFactory.createXMLStreamReader(new StringReader(xml));
            return processObject(reader, requiredType, swf, null, 1);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchMethodException | InstantiationException
                | InvocationTargetException | XMLStreamException ex) {
            Logger.getLogger(SwfXmlImporter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private Field getField(Class cls, String name) throws NoSuchFieldException {
        Field field;
        try {
            field = cls.getField(name);
        } catch (NoSuchFieldException ex) {
            field = cls.getDeclaredField(name);
            field.setAccessible(true);
        }

        return field;
    }

    private static void setFieldValue(Field field, Object obj, Object value) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException {
        /* Unsupported in java 9+ Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);

        //Remove final attribute temporary (For example Multiname.namespace_set_index
        int originalModifiers = field.getModifiers();
        if ((originalModifiers & Modifier.FINAL) > 0) {
            modifiersField.setInt(field, originalModifiers & ~Modifier.FINAL);
        }        

        field.setAccessible(true);

        int newModifiers = field.getModifiers();
         */
        field.set(obj, value);

        /*      //Put final back in
        if (originalModifiers != newModifiers) {
            modifiersField.setInt(field, originalModifiers);
        }*/
    }

    private void processElement(XMLStreamReader reader, Object obj, SWF swf, Tag tag, int xmlExportMajor) throws XMLStreamException {
        // Check if element started and start if needed
        if (!reader.isStartElement()) {
            reader.nextTag();
            reader.require(XMLStreamConstants.START_ELEMENT, null, null);
        }

        Class cls = obj.getClass();

        Map<String, String> attributes = new HashMap<>();
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            String value = reader.getAttributeValue(i);
            attributes.put(name, value);
        }

        if ("SWF".equals(attributes.get("type"))) {
            int xmlExportMinor = 0;
            if (attributes.containsKey("_xmlExportMajor")) {
                xmlExportMajor = Integer.parseInt(attributes.get("_xmlExportMajor"));
            }

            if (attributes.containsKey("_xmlExportMinor")) {
                xmlExportMinor = Integer.parseInt(attributes.get("_xmlExportMinor"));
            }

            if (xmlExportMajor > MAX_XML_IMPORT_VERSION_MAJOR) {
                throw new RuntimeException("The XML file was exported with newer XML format (major " + xmlExportMajor + ", minor " + xmlExportMinor + "). Please download newer version of FFDec to correctly parse it.");
            }
        }

        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            String name = entry.getKey();
            String val = entry.getValue();

            if (name.equals("tagId") && "UnknownTag".equals(attributes.get("type"))) {
                continue;
            }
            if (name.equals("charset") && "SWF".equals(attributes.get("type"))) {
                ((SWF) obj).setCharset(val);
                continue;
            }

            //skip meta parameters starting with "_". expandable in the future...
            if ("SWF".equals(attributes.get("type")) && name.startsWith("_")) {
                continue;
            }

            //backwards compatibility
            if (name.equals("reserved1") && "FileAttributesTag".equals(attributes.get("type"))) {
                name = "reservedA";
            }
            if (name.equals("reserved2") && "FileAttributesTag".equals(attributes.get("type"))) {
                name = "swfRelativeUrls";
            }
            if (name.equals("reserved3") && "FileAttributesTag".equals(attributes.get("type"))) {
                name = "reservedB";
            }

            if (!name.equals("type")) {
                try {
                    Field field = getField(cls, name);
                    setFieldValue(field, obj, getAs(field.getType(), val, xmlExportMajor));
                } catch (NoSuchFieldException | SecurityException | IllegalArgumentException
                        | IllegalAccessException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            }
        }

        // Check for child elements
        reader.nextTag();
        while (reader.isStartElement()) {
            // Child element open
            String name = reader.getLocalName();
            try {
                Field field = getField(cls, name);
                Class childCls = field.getType();

                if (isList(childCls)) {
                    List list = HashArrayList.class.isAssignableFrom(childCls) ? new HashArrayList() : new ArrayList();
                    Class reqType = childCls.isArray() ? childCls.getComponentType() : ReflectionTools.getFieldSubType(obj, field);

                    // Check for list item elements
                    reader.nextTag();
                    while (reader.isStartElement()) {
                        Object childObj = processObject(reader, reqType, swf, tag, xmlExportMajor);
                        list.add(childObj);

                        reader.nextTag();
                    }

                    Object value = list;

                    if (childCls.isArray()) {
                        value = Array.newInstance(childCls.getComponentType(), list.size());
                        for (int j = 0; j < list.size(); j++) {
                            Array.set(value, j, list.get(j));
                        }
                    }

                    setFieldValue(field, obj, value);
                } else {
                    Object childObj = processObject(reader, null, swf, tag, xmlExportMajor);
                    setFieldValue(field, obj, childObj);
                }
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException
                    | NoSuchMethodException | InstantiationException | InvocationTargetException ex) {
                logger.log(Level.SEVERE, "Error while getting val from class " + cls + " field: " + name, ex);
            }

            reader.nextTag();
        }

        // Check if element ended and end if needed
        if (reader.getEventType() != XMLStreamConstants.END_ELEMENT) {
            reader.nextTag();
            reader.require(XMLStreamConstants.END_ELEMENT, null, null);
        }
    }

    private Object processObject(XMLStreamReader reader, Class requiredType, SWF swf, Tag tag, int xmlExportMajor) throws IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InstantiationException, InvocationTargetException, XMLStreamException {
        // Check if element started and start if needed
        if (!reader.isStartElement()) {
            reader.nextTag();
            reader.require(XMLStreamConstants.START_ELEMENT, null, null);
        }

        Map<String, String> attributes = new HashMap<>();
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            String value = reader.getAttributeValue(i);
            attributes.put(name, value);
        }

        String type = attributes.get("type");
        String tagTypeIdStr = attributes.get("tagId");
        int tagTypeId = -1;
        try {
            tagTypeId = Integer.parseInt(tagTypeIdStr);
        } catch (NumberFormatException nfe) {
            //ignore
        }

        Object ret;

        if ("String".equals(type)) {
            ret = reader.getElementText();
        } else if (type != null && !type.isEmpty()) {
            Object childObj = createObject(type, tagTypeId, swf, tag);
            if (childObj instanceof Tag) {
                tag = (Tag) childObj;
            }

            processElement(reader, childObj, swf, tag, xmlExportMajor);
            ret = childObj;
        } else {
            String isNullAttr = attributes.get("isNull");
            if (Boolean.parseBoolean(isNullAttr)) {
                ret = null;
            } else {
                ret = getAs(requiredType, reader.getElementText(), xmlExportMajor);
            }
        }

        // Check if element ended and end if needed
        if (reader.getEventType() != XMLStreamConstants.END_ELEMENT) {
            reader.nextTag();
            reader.require(XMLStreamConstants.END_ELEMENT, null, null);
        }

        return ret;
    }

    private Object createObject(String type, int tagTypeId, SWF swf, Tag tag) throws NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if ("UnknownTag".equals(type)) {
            return new UnknownTag(swf, tagTypeId);
        }

        Class cls = swfTags.get(type);
        if (cls != null) {
            return cls.getConstructor(SWF.class).newInstance(swf);
        }

        cls = swfObjects.get(type);
        if (cls != null) {
            return cls.getConstructor().newInstance();
        }

        cls = swfObjectsParam.get(type);
        if (cls != null) {
            for (Constructor<?> constructor : cls.getConstructors()) {
                if (constructor.getParameterCount() == 1) {
                    Class<?> parameterType = constructor.getParameterTypes()[0];
                    if (parameterType.isAssignableFrom(tag.getClass())) {
                        return constructor.newInstance(tag);
                    }
                }
            }
        }

        System.err.println("Type not found: " + type);
        return null;
    }

    private Object getAs(Class cls, String stringValue, int xmlExportMajor) throws IllegalArgumentException, IllegalAccessException {
        if (cls == Byte.class || cls == byte.class) {
            return Byte.parseByte(stringValue);
        } else if (cls == Short.class || cls == short.class) {
            return Short.parseShort(stringValue);
        } else if (cls == Integer.class || cls == int.class) {
            return Integer.parseInt(stringValue);
        } else if (cls == Long.class || cls == long.class) {
            return Long.parseLong(stringValue);
        } else if (cls == Float.class || cls == float.class) {
            return Float.parseFloat(stringValue);
        } else if (cls == Double.class || cls == double.class) {
            return Double.parseDouble(stringValue);
        } else if (cls == Decimal128.class) {
            String sdec = stringValue;
            if (sdec.endsWith("m")) {
                sdec = sdec.substring(0, sdec.length() - 1);                
            }
            return new Decimal128(sdec);
        } else if (cls == Boolean.class || cls == boolean.class) {
            return Boolean.parseBoolean(stringValue);
        } else if (cls == Character.class || cls == char.class) {
            return stringValue.charAt(0);
        } else if (cls == String.class) {
            return xmlExportMajor >= 2 ? Helper.unescapeXmlExportString(stringValue) : stringValue;
        } else if (cls == ByteArrayRange.class) {
            ByteArrayRange range = new ByteArrayRange(stringValue);
            return range;
        } else if (cls == byte[].class) {
            ByteArrayRange range = new ByteArrayRange(stringValue);
            return range.getArray();
        } else if (cls.isEnum()) {
            return Enum.valueOf(cls, stringValue);
        } else {
            throw new RuntimeException("Unsupported object type: " + cls.getSimpleName() + ".");
        }
    }
}
