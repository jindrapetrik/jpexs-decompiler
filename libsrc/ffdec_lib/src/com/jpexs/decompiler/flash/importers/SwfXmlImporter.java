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
package com.jpexs.decompiler.flash.importers;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ABCVersion;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.types.ABCException;
import com.jpexs.decompiler.flash.abc.types.ClassInfo;
import com.jpexs.decompiler.flash.abc.types.Decimal;
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
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.TagTypeInfo;
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
import com.jpexs.decompiler.flash.types.shaperecords.CurvedEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.EndShapeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.HashArrayList;
import com.jpexs.helpers.ReflectionTools;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author JPEXS
 */
@SuppressWarnings("unchecked")
public class SwfXmlImporter {

    private static final Logger logger = Logger.getLogger(SwfXmlImporter.class.getName());

    private Map<String, Class> swfTags;

    private Map<String, Class> swfObjects;

    private Map<String, Class> swfObjectsParam;

    public void importSwf(SWF swf, String xml) throws IOException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new InputSource(new StringReader(xml)));
            processElement(doc.getDocumentElement(), swf, swf, null);
            swf.clearAllCache();
        } catch (ParserConfigurationException | SAXException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    public Object importObject(String xml, Class requiredType, SWF swf) throws IOException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new InputSource(new StringReader(xml)));
            return processObject(doc.getDocumentElement(), requiredType, swf, null);
        } catch (ParserConfigurationException | SAXException | IllegalArgumentException | IllegalAccessException | NoSuchMethodException | InstantiationException | InvocationTargetException ex) {
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
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);

        //Remove final attribute temporary (For example Multiname.namespace_set_index
        int originalModifiers = field.getModifiers();
        if ((originalModifiers & Modifier.FINAL) > 0) {
            modifiersField.setInt(field, originalModifiers & ~Modifier.FINAL);
        }

        field.setAccessible(true);

        int newModifiers = field.getModifiers();

        field.set(obj, value);

        //Put final back in
        if (originalModifiers != newModifiers) {
            modifiersField.setInt(field, originalModifiers);
        }
    }

    private void processElement(Element element, Object obj, SWF swf, Tag tag) {
        Class cls = obj.getClass();
        for (int i = 0; i < element.getAttributes().getLength(); i++) {
            Attr attr = (Attr) element.getAttributes().item(i);
            String name = attr.getName();
            if (!name.equals("type")) {
                try {
                    Field field = getField(cls, name);
                    String attrValue = attr.getValue();
                    setFieldValue(field, obj, getAs(field.getType(), attrValue));
                } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            }
        }

        for (int i = 0; i < element.getChildNodes().getLength(); i++) {
            Node childNode = element.getChildNodes().item(i);
            if (childNode instanceof Element) {
                Element child = (Element) childNode;
                String name = child.getTagName();
                try {
                    Field field = getField(cls, name);
                    Class childCls = field.getType();
                    if (List.class.isAssignableFrom(childCls)) {
                        List list = HashArrayList.class.isAssignableFrom(childCls) ? new HashArrayList() : new ArrayList();
                        for (int j = 0; j < child.getChildNodes().getLength(); j++) {
                            Node childChildNode = child.getChildNodes().item(j);
                            if (childChildNode instanceof Element) {
                                Element childChild = (Element) child.getChildNodes().item(j);
                                Object childObj = processObject(childChild, ReflectionTools.getFieldSubType(obj, field), swf, tag);
                                list.add(childObj);
                            }
                        }

                        setFieldValue(field, obj, list);
                    } else if (childCls.isArray()) {
                        List list = new ArrayList();
                        for (int j = 0; j < child.getChildNodes().getLength(); j++) {
                            Node childChildNode = child.getChildNodes().item(j);
                            if (childChildNode instanceof Element) {
                                Element childChild = (Element) child.getChildNodes().item(j);
                                Object childObj = processObject(childChild, childCls.getComponentType(), swf, tag);
                                list.add(childObj);
                            }
                        }

                        Object array = Array.newInstance(childCls.getComponentType(), list.size());
                        for (int j = 0; j < list.size(); j++) {
                            Array.set(array, j, list.get(j));
                        }

                        setFieldValue(field, obj, array);
                    } else {
                        Object childObj = processObject(child, null, swf, tag);
                        setFieldValue(field, obj, childObj);
                    }
                } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | NoSuchMethodException | InstantiationException | InvocationTargetException ex) {
                    logger.log(Level.SEVERE, "Error while getting val from class " + cls + " field: " + name, ex);
                }
            }
        }
    }

    private Object processObject(Element element, Class requiredType, SWF swf, Tag tag) throws IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InstantiationException, InvocationTargetException {
        String type = element.getAttribute("type");
        if ("String".equals(type)) {
            return element.getTextContent();
        } else if (type != null && !type.isEmpty()) {
            Object childObj = createObject(type, swf, tag);
            if (childObj instanceof Tag) {
                tag = (Tag) childObj;
            }

            processElement(element, childObj, swf, tag);
            return childObj;
        } else {
            String isNullAttr = element.getAttribute("isNull");
            if (Boolean.parseBoolean(isNullAttr)) {
                return null;
            }

            return getAs(requiredType, element.getTextContent());
        }
    }

    private Object createObject(String type, SWF swf, Tag tag) throws NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (swfTags == null) {
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
        }

        Class cls = swfTags.get(type);
        if (cls != null) {
            return cls.getConstructor(SWF.class).newInstance(swf);
        }

        if (swfObjects == null) {
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
                AVM2ConstantPool.class, Decimal.class, Namespace.class, NamespaceSet.class, Multiname.class, MethodInfo.class, MetadataInfo.class,
                ValueKind.class, InstanceInfo.class, Traits.class, TraitClass.class, TraitFunction.class,
                TraitMethodGetterSetter.class, TraitSlotConst.class, ClassInfo.class, ScriptInfo.class, MethodBody.class,
                ABCException.class, ABCVersion.class, Amf3Value.class};
            for (Class cls2 : knownObjects) {
                if (!ReflectionTools.canInstantiateDefaultConstructor(cls2)) {
                    System.err.println("Can't instantiate: " + cls2.getName());
                }
                objects.put(cls2.getSimpleName(), cls2);
            }

            swfObjects = objects;
        }

        cls = swfObjects.get(type);
        if (cls != null) {
            return cls.getConstructor().newInstance();
        }

        if (swfObjectsParam == null) {
            Map<String, Class> objects = new HashMap<>();
            Class[] knownObjects = new Class[]{ABC.class};
            for (Class cls2 : knownObjects) {
                if (!ReflectionTools.canInstantiate(cls2)) {
                    System.err.println("Can't instantiate: " + cls2.getName());
                }
                objects.put(cls2.getSimpleName(), cls2);
            }

            swfObjectsParam = objects;
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

    private Object getAs(Class cls, String stringValue) throws IllegalArgumentException, IllegalAccessException {
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
        } else if (cls == Boolean.class || cls == boolean.class) {
            return Boolean.parseBoolean(stringValue);
        } else if (cls == Character.class || cls == char.class) {
            return stringValue.charAt(0);
        } else if (cls == String.class) {
            return stringValue;
        } else if (cls == ByteArrayRange.class) {
            ByteArrayRange range = new ByteArrayRange(stringValue);
            return range;
        } else if (cls == byte[].class) {
            ByteArrayRange range = new ByteArrayRange(stringValue);
            return range.getArray();
        } else if (cls.isEnum()) {
            return Enum.valueOf(cls, stringValue);
        } else {
            throw new RuntimeException("Unsupported object type.");
        }
    }
}
