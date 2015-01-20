/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.helpers.LazyObject;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.ReflectionTools;
import com.jpexs.helpers.utf8.Utf8OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author JPEXS
 */
public class SwfXmlExporter {

    public List<File> exportXml(SWF swf, File outFile) throws IOException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document xmlDoc = docBuilder.newDocument();
            exportXml(swf, xmlDoc, xmlDoc);
            try (Writer writer = new BufferedWriter(new Utf8OutputStreamWriter(new FileOutputStream(outFile)))) {
                writer.append(getXml(xmlDoc));
            }
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(SwfXmlExporter.class.getName()).log(Level.SEVERE, null, ex);
        }

        List<File> ret = new ArrayList<>();
        ret.add(outFile);
        return ret;
    }

    private String getXml(Document xml) {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        StringWriter writer = new StringWriter();
        try {
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            DOMSource source = new DOMSource(xml);
            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);
        } catch (TransformerException ex) {
            Logger.getLogger(SwfXmlExporter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return writer.toString();
    }

    public void exportXml(SWF swf, Document doc, Node node) throws IOException {
        generateXml(doc, node, "swf", swf, false, 0);
    }

    private static void generateXml(Document doc, Node node, String name, Object obj, boolean isListItem, int level) {
        Class cls = obj != null ? obj.getClass() : null;

        if (cls == Byte.class || cls == byte.class
                || cls == Short.class || cls == short.class
                || cls == Integer.class || cls == int.class
                || cls == Long.class || cls == long.class
                || cls == Float.class || cls == float.class
                || cls == Double.class || cls == double.class
                || cls == Boolean.class || cls == boolean.class
                || cls == Character.class || cls == char.class
                || cls == String.class) {
            Object value = obj;
            if (value instanceof String) {
                value = Helper.removeInvalidXMLCharacters((String) value);
            }

            if (isListItem) {
                Element childNode = doc.createElement(name);
                childNode.setTextContent(value.toString());
                node.appendChild(childNode);
            } else {
                ((Element) node).setAttribute(name, value.toString());
            }
        } else if (cls != null && cls.isEnum()) {
            ((Element) node).setAttribute(name, obj.toString());
        } else if (obj instanceof ByteArrayRange) {
            ByteArrayRange range = (ByteArrayRange) obj;
            byte[] data = range.getRangeData();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < data.length; i++) {
                sb.append(String.format("%02x", data[i]));
            }

            ((Element) node).setAttribute(name, sb.toString());
        } else if (cls != null && List.class.isAssignableFrom(cls)) {
            List list = (List) obj;
            Element listNode = doc.createElement(name);
            node.appendChild(listNode);
            for (int i = 0; i < list.size(); i++) {
                generateXml(doc, listNode, "item", list.get(i), true, level + 1);
            }
        } else if (cls != null && cls.isArray()) {
            Class arrayType = cls.getComponentType();
            Element arrayNode = doc.createElement(name);
            node.appendChild(arrayNode);
            int length = Array.getLength(obj);
            for (int i = 0; i < length; i++) {
                generateXml(doc, arrayNode, "item", Array.get(obj, i), true, level + 1);
            }
        } else if (obj != null) {
            if (obj instanceof LazyObject) {
                ((LazyObject) obj).load();
            }

            String className = obj.getClass().getSimpleName();
            List<Field> fields = ReflectionTools.getSwfFields(obj.getClass());
            Element objNode = doc.createElement(name);
            objNode.setAttribute("type", className);
            node.appendChild(objNode);

            if (level == 0) {
                objNode.appendChild(doc.createComment("WARNING: The structure of this XML is not final. In later versions of FFDec it can be changed."));
            }

            for (Field f : fields) {
                if (Modifier.isStatic(f.getModifiers())) {
                    continue;
                }

                Internal inter = f.getAnnotation(Internal.class);
                if (inter != null) {
                    continue;
                }

                try {
                    f.setAccessible(true);
                    generateXml(doc, objNode, f.getName(), f.get(obj), false, level + 1);
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    Logger.getLogger(SwfXmlExporter.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else {
            if (isListItem) {
                Element childNode = doc.createElement(name);
                childNode.setAttribute("isNull", Boolean.TRUE.toString());
                node.appendChild(childNode);
            }
        }
    }
}
