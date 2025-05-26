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
package com.jpexs.decompiler.flash.docs;

import com.jpexs.decompiler.flash.ApplicationInfo;
import com.jpexs.helpers.Helper;
import java.util.Arrays;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * AS3 PCode other documentation.
 */
public class As3PCodeOtherDocs extends AbstractDocs {

    static ResourceBundle prop;
    static final String NEWLINE = "\r\n";

    static {
        prop = ResourceBundle.getBundle("com.jpexs.decompiler.flash.locales.docs.pcode.AS3other");
    }

    /**
     * Constructor.
     */
    public As3PCodeOtherDocs() {

    }

    /**
     * Gets documentation for path.
     * @param path Path
     * @param nightMode Night mode
     * @return Documentation
     */
    public static String getDocsForPath(String path, boolean nightMode) {

        return getDocsForPath(path, true, nightMode);
    }

    private static String getDocsForPath(String path, boolean standalone, boolean nightMode) {

        final String cacheKey = path + "|" + (standalone ? 1 : 0);
        String v = docsCache.get(cacheKey);
        if (v != null) {
            return v;
        }

        String pathNoTrait = path;
        if (path.startsWith("trait.method")) {
            pathNoTrait = path.substring("trait.".length());
        }

        StringBuilder sb = new StringBuilder();
        if (standalone) {
            sb.append(htmlHeader("", getStyle()));
        }

        if (standalone) {
            sb.append("<body class=\"");
            if (nightMode) {
                sb.append("standalonenight");
            } else {
                sb.append("standalone");
            }
            sb.append("\">");
        }
        sb.append("<div class=\"otherdoc\">");

        String[] pathParts = new String[]{path};
        if (path.contains(".")) {
            pathParts = path.split("\\.");
        }
        for (int i = 0; i < pathParts.length; i++) {
            String curPath = String.join(".", Arrays.copyOf(pathParts, i + 1));
            if (curPath.startsWith("trait.method")) {
                curPath = path.substring("trait.".length());
            }
            if (curPath.startsWith("method.body.trait.")) {
                curPath = path.substring("method.body.".length());
            }
            if (prop.containsKey(curPath)) {
                String docStr = prop.getString(curPath);
                sb.append("<div class=\"path-block\">");
                for (int j = 0; j < i; j++) {
                    sb.append("&nbsp;");
                    sb.append("&nbsp;");
                }
                sb.append("<span class=\"path\">");
                sb.append(pathParts[i]);
                sb.append("</span>");
                sb.append(" ");
                sb.append("<span class=\"path-docs\">");
                sb.append(docStr);
                sb.append("</span>");
                sb.append("</div>");
            }
        }

        sb.append("</div>").append(NEWLINE); //.instruction        
        if (standalone) {
            sb.append("</body>");
            sb.append(htmlFooter());
        }
        String r = sb.toString();
        docsCache.put(cacheKey, r);
        return r;
    }

    /**
     * Gets HTML header.
     * @param js JavaScript
     * @param style Style
     * @return HTML header
     */
    protected static String htmlHeader(String js, String style) {
        Date dateGenerated = new Date();
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>").append(NEWLINE)
                .append("<html>").append(NEWLINE)
                .append("\t<head>").append(NEWLINE);
        if (style != null && !style.isEmpty()) {
            sb.append("\t\t<style>").append(style).append("</style>").append(NEWLINE);
        }
        if (js != null && !js.isEmpty()) {
            sb.append("\t\t<script>").append(js).append("</script>").append(NEWLINE);
        }
        sb.append("\t\t<meta charset=\"UTF-8\">").append(NEWLINE)
                .append(meta("generator", ApplicationInfo.applicationVerName))
                .append(meta("description", getProperty("ui.list.pageDescription")))
                .append(metaProp("og:title", getProperty("ui.list.pageTitle")))
                .append(metaProp("og:type", "article"))
                .append(metaProp("og:description", getProperty("ui.list.pageDescription")))
                .append(meta("date", dateGenerated))
                .append("\t\t<title>").append(getProperty("ui.list.documentTitle")).append("</title>").append(NEWLINE)
                .append("\t</head>").append(NEWLINE);
        return sb.toString();
    }

    /**
     * Gets property.
     * @param name Name
     * @return Property
     */
    protected static String getProperty(String name) {
        if (prop.containsKey(name)) {
            return Helper.escapeHTML(prop.getString(name));
        }
        return null;
    }
}
