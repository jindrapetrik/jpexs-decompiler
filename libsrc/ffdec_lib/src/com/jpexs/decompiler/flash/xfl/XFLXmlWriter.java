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
package com.jpexs.decompiler.flash.xfl;

import com.jpexs.helpers.Helper;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * XFL XML writer.
 * @author JPEXS
 */
public class XFLXmlWriter implements XMLStreamWriter {

    private static final Logger logger = Logger.getLogger(XFLXmlWriter.class.getName());

    private final String newLineCharacters = "\n"; //Helper.newLine;

    private boolean newLine = true;

    private boolean newLineNeeded = false;

    private boolean startElementClosed = true;

    private final StringBuilder sb = new StringBuilder();

    private final Map<String, String> namespaces = new HashMap<>();

    private final Stack<String> tagsStack = new Stack<>();

    @Override
    public String toString() {
        return sb.toString();
    }

    private XFLXmlWriter append(char character) {
        sb.append(character);
        newLine = false;
        return this;
    }

    private XFLXmlWriter append(String text) {
        sb.append(text);
        newLine = false;
        return this;
    }

    private void makeNewLine() {
        if (!newLine) {
            sb.append(newLineCharacters);
            newLine = true;
            for (int i = 0; i < tagsStack.size(); i++) {
                sb.append("  ");
            }
        }
    }

    private void closeStartElement() {
        if (!startElementClosed) {
            append('>');
            startElementClosed = true;
        }
    }

    private void ensureStartElementOpen() throws XMLStreamException {
        if (startElementClosed) {
            throw new XMLStreamException("Attempted to write attribute out of the start element");
        }
    }

    private void closeStartElementNewLine() {
        closeStartElement();
        makeNewLine();
    }

    @Override
    public void writeStartElement(String localName) throws XMLStreamException {
        closeStartElementNewLine();
        append('<').append(localName);
        tagsStack.add(localName);
        startElementClosed = false;
        newLineNeeded = false;
    }

    public void writeStartElement(String localName, String[] attributes) throws XMLStreamException {
        writeStartElement(localName);
        if (attributes.length % 2 != 0) {
            throw new XMLStreamException("Attribute count should be even");
        }

        for (int i = 0; i < attributes.length / 2; i++) {
            writeAttribute(attributes[i * 2], attributes[i * 2 + 1]);
        }
    }

    @Override
    public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
        writeStartElementInternal(getPrefix(namespaceURI), localName, namespaceURI);
    }

    @Override
    public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        setPrefix(prefix, namespaceURI);
        writeStartElementInternal(prefix, localName, namespaceURI);
    }

    private void writeStartElementInternal(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        closeStartElementNewLine();
        append('<').append(prefix).append(':').append(localName);
        writeNamespace(prefix, namespaceURI);
        tagsStack.add(localName);
        startElementClosed = false;
        newLineNeeded = false;
    }

    private void writeEmptyElementInternal(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        writeStartElement(prefix, localName, namespaceURI);
        writeEndElement();
    }

    @Override
    public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
        writeEmptyElementInternal(getPrefix(namespaceURI), localName, namespaceURI);
    }

    @Override
    public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        setPrefix(prefix, namespaceURI);
        writeEmptyElementInternal(prefix, localName, namespaceURI);
    }

    @Override
    public void writeEmptyElement(String localName) throws XMLStreamException {
        writeStartElement(localName);
        writeEndElement();
    }

    public void writeEmptyElement(String localName, String[] attributes) throws XMLStreamException {
        writeStartElement(localName, attributes);
        writeEndElement();
    }

    @Override
    public void writeEndElement() throws XMLStreamException {
        String localName = tagsStack.pop();
        if (startElementClosed) {
            if (newLineNeeded) {
                makeNewLine();
            }

            append("</").append(localName).append('>');
        } else {
            append(" />");
            startElementClosed = true;
        }

        newLineNeeded = true;
    }

    public void writeElementValueRaw(String localName, String value) throws XMLStreamException {
        writeStartElement(localName);
        writeCharactersRaw(value);
        writeEndElement();
    }

    public void writeElementValue(String localName, String value) throws XMLStreamException {
        writeStartElement(localName);
        writeCharacters(value);
        writeEndElement();
    }

    public void writeElementValue(String localName, float value) throws XMLStreamException {
        writeElementValue(localName, Float.toString(value));
    }

    public void writeElementValue(String localName, double value) throws XMLStreamException {
        writeElementValue(localName, Double.toString(value));
    }

    public void writeElementValue(String localName, int value) throws XMLStreamException {
        writeElementValue(localName, Integer.toString(value));
    }

    public void writeElementValue(String localName, long value) throws XMLStreamException {
        writeElementValue(localName, Long.toString(value));
    }

    public void writeElementValue(String localName, String value, String[] attributes) throws XMLStreamException {
        writeStartElement(localName, attributes);
        writeCharacters(value);
        writeEndElement();
    }

    @Override
    public void writeEndDocument() throws XMLStreamException {
    }

    @Override
    public void close() throws XMLStreamException {
    }

    @Override
    public void flush() throws XMLStreamException {
    }

    @Override
    public void writeAttribute(String localName, String value) throws XMLStreamException {
        ensureStartElementOpen();
        append(' ').append(localName).append("=\"").append(escapeAttribute(value)).append('"');
    }

    public void writeAttribute(String localName, float value) throws XMLStreamException {
        writeAttribute(localName, Float.toString(value));
    }

    public void writeAttribute(String localName, double value) throws XMLStreamException {
        writeAttribute(localName, Double.toString(value));
    }

    public void writeAttribute(String localName, int value) throws XMLStreamException {
        writeAttribute(localName, Integer.toString(value));
    }

    public void writeAttribute(String localName, long value) throws XMLStreamException {
        writeAttribute(localName, Long.toString(value));
    }

    public void writeAttribute(String localName, boolean value) throws XMLStreamException {
        writeAttribute(localName, value ? "true" : "false");
    }

    @Override
    public void writeAttribute(String prefix, String namespaceURI, String localName, String value) throws XMLStreamException {
        setPrefix(prefix, namespaceURI);
        writeAttributeInternal(prefix, localName, value);
    }

    @Override
    public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
        writeAttributeInternal(getPrefix(namespaceURI), localName, value);
    }

    private void writeAttributeInternal(String prefix, String localName, String value) throws XMLStreamException {
        ensureStartElementOpen();
        append(' ').append(prefix).append(':').append(localName).append("=\"").append(escapeAttribute(value)).append('"');
    }

    @Override
    public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
        writeAttribute("xmlns", null, prefix, namespaceURI);
    }

    @Override
    public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
    }

    @Override
    public void writeComment(String data) throws XMLStreamException {
        closeStartElement();
        append("<!--").append(data).append("-->");
    }

    @Override
    public void writeProcessingInstruction(String target) throws XMLStreamException {
    }

    @Override
    public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
    }

    @Override
    public void writeCData(String data) throws XMLStreamException {
        closeStartElement();
        // todo: split when data contains "]]>"
        append("<![CDATA[").append(data).append("]]>");
    }

    @Override
    public void writeDTD(String dtd) throws XMLStreamException {
    }

    @Override
    public void writeEntityRef(String name) throws XMLStreamException {
    }

    @Override
    public void writeStartDocument() throws XMLStreamException {
        writeStartDocument("utf-8", "1.0");
    }

    @Override
    public void writeStartDocument(String version) throws XMLStreamException {
        writeStartDocument(version, "utf-8");
    }

    @Override
    public void writeStartDocument(String encoding, String version) throws XMLStreamException {
        append("<?xml version=\"" + escapeAttribute(version) + "\" encoding=\"" + escapeAttribute(encoding) + "\"?>");
    }

    public void writeCharactersRaw(String text) throws XMLStreamException {
        closeStartElement();
        append(text);
    }

    @Override
    public void writeCharacters(String text) throws XMLStreamException {
        closeStartElement();
        append(escapeText(text));
    }

    @Override
    public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
        writeCharacters(new String(text, start, len));
    }

    @Override
    public String getPrefix(String uri) throws XMLStreamException {
        String prefix = namespaces.get(uri);
        return prefix;
    }

    @Override
    public void setPrefix(String prefix, String uri) throws XMLStreamException {
        namespaces.put(prefix, prefix);
    }

    @Override
    public void setDefaultNamespace(String uri) throws XMLStreamException {
    }

    @Override
    public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
    }

    @Override
    public NamespaceContext getNamespaceContext() {
        return null;
    }

    @Override
    public Object getProperty(String name) throws IllegalArgumentException {
        return null;
    }

    public static String escapeText(String text) {
        //String[] from = new String[]{"&", "<"};
        //String[] to = new String[]{"&amp;", "&lt;"};
        // temporary escape everything to make easier to compare the export with the old export
        String[] from = new String[]{"&", "<", "\"", "'", "\r", "\n"};
        String[] to = new String[]{"&amp;", "&lt;", "&quot;", "&#x27;", "&#xD;", "&#xA;"};
        for (int i = 0; i < from.length; i++) {
            text = text.replace(from[i], to[i]);
        }

        if (Helper.containsInvalidXMLCharacter(text)) {
            logger.log(Level.WARNING, "The following text contains a character which is invalid in XML: {0}", text);
            return Helper.removeInvalidXMLCharacters(text);
        }

        return text;
    }

    public static String escapeAttribute(String text) {
        String[] from = new String[]{"&", "<", "\"", "'", "\r", "\n"};
        String[] to = new String[]{"&amp;", "&lt;", "&quot;", "&#x27;", "&#xD;", "&#xA;"};
        for (int i = 0; i < from.length; i++) {
            text = text.replace(from[i], to[i]);
        }
        if (Helper.containsInvalidXMLCharacter(text)) {
            logger.log(Level.WARNING, "The following text contains a character which is invalid in XML: {0}", text);
            return Helper.removeInvalidXMLCharacters(text);
        }

        return text;
    }

    public int length() {
        return sb.length();
    }

    // todo: remove
    public void setLength(int newLength) {
        sb.setLength(newLength);
    }

    public boolean isEmpty() {
        return sb.length() == 0;
    }
}
