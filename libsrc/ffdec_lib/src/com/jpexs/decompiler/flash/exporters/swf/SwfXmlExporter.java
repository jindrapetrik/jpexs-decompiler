/*
 *  Copyright (C) 2010-2021 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.exporters.swf;

import com.jpexs.decompiler.flash.ApplicationInfo;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.helpers.InternalClass;
import com.jpexs.decompiler.flash.helpers.LazyObject;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.UnknownTag;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.decompiler.flash.types.annotations.Multiline;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.ReflectionTools;
import com.jpexs.helpers.utf8.Utf8OutputStreamWriter;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 *
 * @author JPEXS
 */
public class SwfXmlExporter {

    private static final Logger logger = Logger.getLogger(SwfXmlExporter.class.getName());

    private final Map<Class, List<Field>> cachedFields = new HashMap<>();

    public List<File> exportXml(SWF swf, File outFile) throws IOException {
        try {
            File tmp = File.createTempFile("FFDEC", "XML");

            try (Writer writer = new Utf8OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(tmp)))) {
                XMLStreamWriter xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(writer);

                xmlWriter.writeStartDocument();
                xmlWriter.writeComment("WARNING: The structure of this XML is not final. In later versions of FFDec it can be changed.");
                xmlWriter.writeComment(ApplicationInfo.applicationVerName);

                exportXml(swf, xmlWriter);

                xmlWriter.writeEndDocument();
                xmlWriter.flush();
                xmlWriter.close();
            }

            TransformerFactory factory = TransformerFactory.newInstance();

            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            transformer.transform(new StreamSource(tmp), new StreamResult(outFile));

            tmp.delete();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        List<File> ret = new ArrayList<>();
        ret.add(outFile);
        return ret;
    }

    public void exportXml(SWF swf, XMLStreamWriter writer) throws IOException, XMLStreamException {
        generateXml(writer, "swf", swf, false, false);
    }

    public List<Field> getSwfFieldsCached(Class cls) {
        List<Field> result = cachedFields.get(cls);
        if (result == null) {
            result = ReflectionTools.getSwfFields(cls);

            result.removeIf((f) -> {
                return Modifier.isStatic(f.getModifiers()) || f.getAnnotation(Internal.class) != null;
            });

            result.sort((o1, o2) -> {
                
                boolean a1 = canBeAttribute(o1.getType());
                boolean a2 = canBeAttribute(o2.getType());
                
                if(a1 == a2 && a1 == true) {
                    return o1.getName().compareTo(o2.getName());
                }
                
                return a1 ? -1 : a2 ? 1 : 0;
            });

            cachedFields.put(cls, result);
        }

        return result;
    }

    private boolean isPrimitive(Class cls) {
        return cls != null && !cls.equals(Void.class) && (cls.isPrimitive()
                || cls == Short.class
                || cls == Integer.class
                || cls == Long.class
                || cls == Float.class
                || cls == Double.class
                || cls == Boolean.class
                || cls == Character.class
                || cls == String.class);
    }

    private boolean canBeAttribute(Class cls) {
        return cls != null && (isPrimitive(cls)
                || cls.equals(byte[].class)
                || ByteArrayRange.class.isAssignableFrom(cls)
                || cls.isEnum());
    }

    private boolean isList(Class cls) {
        return cls != null && (cls.isArray() || List.class.isAssignableFrom(cls));
    }

    private void generateXml(XMLStreamWriter writer, String name, Object obj, boolean isListItem, boolean needsCData) throws XMLStreamException {
        Class cls = obj != null ? obj.getClass() : null;

        if (obj != null && needsCData && cls == String.class) {
            writer.writeStartElement(name);
            writer.writeAttribute("type", "String");
            writer.writeCData((String) obj);
            writer.writeEndElement();
        } else if (obj != null && isPrimitive(cls)) {
            Object value = obj;
            if (value instanceof String) {
                value = Helper.removeInvalidXMLCharacters((String) value);
            }

            if (isListItem) {
                writer.writeStartElement(name);
                writer.writeCharacters(value.toString());
                writer.writeEndElement();
            } else {
                writer.writeAttribute(name, value.toString());
            }
        } else if (cls != null && obj != null && cls.isEnum()) {
            writer.writeAttribute(name, obj.toString());
        } else if (obj instanceof ByteArrayRange) {
            ByteArrayRange range = (ByteArrayRange) obj;
            byte[] data = range.getRangeData();
            writer.writeAttribute(name, Helper.byteArrayToHex(data));
        } else if (obj instanceof byte[]) {
            byte[] data = (byte[]) obj;
            writer.writeAttribute(name, Helper.byteArrayToHex(data));
        } else if (isList(cls)) {
            Object value = obj;
            if (List.class.isAssignableFrom(cls)) {
                value = ((List) value).toArray();
            }

            writer.writeStartElement(name);
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                generateXml(writer, "item", Array.get(value, i), true, false);
            }
            writer.writeEndElement();
        } else if (obj != null) {
            if (obj instanceof LazyObject) {
                ((LazyObject) obj).load();
            }

            Class clazz = obj.getClass();
            List<Field> fields = getSwfFieldsCached(clazz);

            if (obj instanceof InternalClass) {
                clazz = clazz.getSuperclass();
            }

            writer.writeStartElement(name);
            writer.writeAttribute("type", clazz.getSimpleName());

            if (obj instanceof UnknownTag) {
                writer.writeAttribute("tagId", String.valueOf(((Tag) obj).getId()));
            }

            for (Field f : fields) {
                Multiline multilineA = f.getAnnotation(Multiline.class);

                try {
                    f.setAccessible(true);
                    generateXml(writer, f.getName(), f.get(obj), false, multilineA != null);
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            }
            writer.writeEndElement();
        } else if (isListItem) {
            writer.writeStartElement(name);
            writer.writeAttribute("isNull", Boolean.TRUE.toString());
            writer.writeEndElement();
        }
    }
}
