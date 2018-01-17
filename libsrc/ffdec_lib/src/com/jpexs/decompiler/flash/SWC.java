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
package com.jpexs.decompiler.flash;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author JPEXS
 */
public class SWC extends ZippedSWFBundle {

    public SWC(InputStream is) throws IOException {
        super(is);
    }

    public SWC(File filename) throws IOException {
        super(filename);
    }

    @Override
    protected void initBundle(InputStream is, File filename) throws IOException {
        super.initBundle(is, filename);
        keySet.clear();
        this.is.reset();
        ZipInputStream zip = new ZipInputStream(this.is);
        ZipEntry entry;

        while ((entry = zip.getNextEntry()) != null) {
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
                    saxParser.parse(zip, handler);
                } catch (Exception ex) {

                }
                return;
            }
        }
    }

    @Override
    public String getExtension() {
        return "swc";
    }
}
