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
package com.jpexs.decompiler.flash.docs;

import com.jpexs.decompiler.flash.ApplicationInfo;
import static com.jpexs.decompiler.flash.docs.As3PCodeDocs.NEWLINE;
import com.jpexs.helpers.Cache;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

public class As3PCodeOtherDocs extends AbstractDocs {

    static ResourceBundle prop;
    static final String NEWLINE = "\r\n";

    static {
        prop = ResourceBundle.getBundle("com.jpexs.decompiler.flash.locales.docs.pcode.AS3other");
    }

    public static String getDocsForPath(String path) {

        return getDocsForPath(path, true);
    }

    private static String getDocsForPath(String path, boolean standalone) {

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

        sb.append("<");
        sb.append(standalone ? "body" : "div");
        sb.append(" class=\"otherdoc\"");
        sb.append(">");

        String pathParts[] = new String[]{path};
        if (path.contains(".")) {
            pathParts = path.split("\\.");
        }
        for (int i = 0; i < pathParts.length; i++) {
            String curPath = String.join(".", Arrays.copyOf(pathParts, i + 1));
            if (curPath.startsWith("trait.method")) {
                curPath = path.substring("trait.".length());
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

        sb.append("</");
        sb.append(standalone ? "body" : "div"); //.instruction
        sb.append(">").append(NEWLINE);
        if (standalone) {
            sb.append(htmlFooter());
        }
        String r = sb.toString();
        docsCache.put(cacheKey, r);
        return r;
    }

    protected static String htmlHeader(String js, String style) {
        Date dateGenerated = new Date();
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>").append(NEWLINE).
                append("<html>").append(NEWLINE).
                append("\t<head>").append(NEWLINE);
        if (style != null && !style.isEmpty()) {
            sb.append("\t\t<style>").append(style).append("</style>").append(NEWLINE);
        }
        if (js != null && !js.isEmpty()) {
            sb.append("\t\t<script>").append(js).append("</script>").append(NEWLINE);
        }
        sb.append("\t\t<meta charset=\"UTF-8\">").append(NEWLINE).
                append(meta("generator", ApplicationInfo.applicationVerName)).
                append(meta("description", getProperty("ui.list.pageDescription"))).
                append(metaProp("og:title", getProperty("ui.list.pageTitle"))).
                append(metaProp("og:type", "article")).
                append(metaProp("og:description", getProperty("ui.list.pageDescription"))).
                append(meta("date", dateGenerated)).
                append("\t\t<title>").append(getProperty("ui.list.documentTitle")).append("</title>").append(NEWLINE).
                append("\t</head>").append(NEWLINE);
        return sb.toString();
    }

    protected static String getProperty(String name) {
        if (prop.containsKey(name)) {
            return Helper.escapeHTML(prop.getString(name));
        }
        return null;
    }
}
