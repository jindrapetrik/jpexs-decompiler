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
package com.jpexs.decompiler.flash;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SWC file.
 *
 * @author JPEXS
 */
public class SWC extends ZippedBundle {   

    /**
     * Constructs SWC from file.
     *
     * @param filename File
     * @throws IOException On I/O error
     */
    public SWC(File filename) throws IOException {
        super(filename);
    }

    /**
     * Initializes SWC bundle.
     *
     * @param filename File
     * @throws IOException On I/O error
     */
    @Override
    protected void initBundle(File filename) throws IOException {
        super.initBundle(filename);
        keySet.clear();
        ZipFile zipFile = new ZipFile(filename);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();

        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.getName().equals("catalog.xml")) {
                try {
                    SAXParserFactory factory = SAXParserFactory.newInstance();
                    SAXParser saxParser = factory.newSAXParser();
                    DefaultHandler handler = new DefaultHandler() {

                        @Override
                        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                            if (qName.equalsIgnoreCase("library")) {
                                String path = attributes.getValue("path");
                                if (path != null) {
                                    keySet.add(path);
                                }
                            }
                        }

                    };
                    saxParser.parse(zipFile.getInputStream(entry), handler);
                } catch (Exception ex) {
                    //ignored
                }
                return;
            }
        }
        zipFile.close();
    }

    /**
     * Returns extension of SWC file.
     *
     * @return Extension
     */
    @Override
    public String getExtension() {
        return "swc";
    }
}
