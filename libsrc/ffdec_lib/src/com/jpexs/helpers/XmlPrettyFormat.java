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
package com.jpexs.helpers;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * Pretty formats XML.
 *
 * @author JPEXS
 */
public class XmlPrettyFormat {

    private static final String PRETTY_PRINT_XSLT = "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\n"
            + "    <xsl:strip-space elements=\"*\"/>\n"
            + "    <xsl:output method=\"xml\" encoding=\"UTF-8\"/>\n"
            + "\n"
            + "    <xsl:template match=\"@*|node()\">\n"
            + "        <xsl:copy>\n"
            + "            <xsl:apply-templates select=\"@*|node()\"/>\n"
            + "        </xsl:copy>\n"
            + "    </xsl:template>\n"
            + "\n"
            + "</xsl:stylesheet>";

    public boolean prettyFormat(Source source, Result result, int indent, boolean withXmlDeclaration) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute("indent-number", indent);
            Transformer transformer = transformerFactory.newTransformer(new StreamSource(new StringReader(PRETTY_PRINT_XSLT)));
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            if (!withXmlDeclaration) {
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            }
            transformer.transform(source, result);
            return true;
        } catch (TransformerFactoryConfigurationError | IllegalArgumentException | TransformerException e) {
            Logger.getLogger(XmlPrettyFormat.class.getName()).log(Level.SEVERE, "Pretty print error", e);
            return false;
        }
    }

    public boolean prettyFormat(File source, File result, int indent, boolean withXmlDeclaration) {
        return prettyFormat(new StreamSource(source), new StreamResult(result), indent, withXmlDeclaration);
    }

    public String prettyFormat(String input, int indent, boolean withXmlDeclaration) {
        Source xmlInput = new StreamSource(new StringReader(input));
        StringWriter stringWriter = new StringWriter();
        StreamResult xmlOutput = new StreamResult(stringWriter);
        if (!prettyFormat(xmlInput, xmlOutput, indent, withXmlDeclaration)) {
            return input;
        }
        return xmlOutput.getWriter().toString();
    }
}
