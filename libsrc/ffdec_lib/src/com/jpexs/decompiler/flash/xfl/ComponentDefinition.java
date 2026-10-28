/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
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

import com.jpexs.decompiler.flash.SWF;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Authoring schemas of the standard components shipped with Flash CS6.
 * These contain Inspector metadata only, never component implementations.
 */
final class ComponentDefinition {

    final String name;

    final boolean compiled;

    final String className;

    final String minimumPlayerVersion;

    final int width;

    final int height;

    final String requiredClasses;

    final Icon icon;

    final String iconId;

    final Map<String, Parameter> parameters;

    static final class Icon {

        final String resource;

        final Map<String, String> attributes = new LinkedHashMap<>();

        Icon(Element element) {
            resource = element.getAttribute("resource");
            for (String name : new String[]{"rowByteCount", "colorDepth", "width", "height", "frameRight", "frameBottom", "isTransparent"}) {
                if (element.hasAttribute(name)) {
                    attributes.put(name, element.getAttribute(name));
                }
            }
        }
    }

    static final class Parameter {

        final String id;

        final String type;

        final String defaultValue;

        final Map<String, String> attributes;

        Parameter(Element element) {
            id = element.getAttribute("id");
            type = element.getAttribute("type");
            defaultValue = element.getAttribute("defaultValue");
            Map<String, String> values = new LinkedHashMap<>();
            for (String name : new String[]{"name", "variable", "category", "verbose", "defaultValue", "enumeration", "type"}) {
                if (element.hasAttribute(name)) {
                    values.put(name, element.getAttribute(name));
                }
            }
            attributes = Collections.unmodifiableMap(values);
        }
    }

    private ComponentDefinition(Element element) {
        name = element.getAttribute("name");
        compiled = "true".equals(element.getAttribute("compiled"));
        className = element.getAttribute("className");
        minimumPlayerVersion = element.getAttribute("minimumPlayerVersion");
        width = Integer.parseInt(element.getAttribute("width"));
        height = Integer.parseInt(element.getAttribute("height"));
        requiredClasses = element.getAttribute("requiredClasses");
        iconId = element.getAttribute("iconId");
        Icon componentIcon = null;
        Map<String, Parameter> values = new LinkedHashMap<>();
        for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof Element && "parameter".equals(child.getNodeName())) {
                Parameter parameter = new Parameter((Element) child);
                values.put(parameter.id, parameter);
            } else if (child instanceof Element && "icon".equals(child.getNodeName())) {
                componentIcon = new Icon((Element) child);
            }
        }
        icon = componentIcon;
        parameters = Collections.unmodifiableMap(values);
    }

    static Map<String, ComponentDefinition> load(SWF swf) throws IOException {
        return load(swf, "component_definitions.xml");
    }

    static Map<String, ComponentDefinition> load(SWF swf, String resource) throws IOException {
        try (InputStream input = ComponentDefinition.class.getResourceAsStream(resource)) {
            if (input == null) {
                throw new IOException("Missing standard component schemas");
            }
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            Element root = factory.newDocumentBuilder().parse(input).getDocumentElement();
            Map<String, ComponentDefinition> definitions = new LinkedHashMap<>();
            for (Node child = root.getFirstChild(); child != null; child = child.getNextSibling()) {
                if (child instanceof Element && "component".equals(child.getNodeName())) {
                    String variantClass = ((Element) child).getAttribute("variantClass");
                    if (!variantClass.isEmpty() && swf.getScriptPacksByClassNames(Collections.singletonList(variantClass)).isEmpty()) {
                        continue;
                    }
                    ComponentDefinition definition = new ComponentDefinition((Element) child);
                    definitions.put(definition.className, definition);
                }
            }
            return Collections.unmodifiableMap(definitions);
        } catch (IOException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IOException("Cannot read standard component schemas", ex);
        }
    }
}
