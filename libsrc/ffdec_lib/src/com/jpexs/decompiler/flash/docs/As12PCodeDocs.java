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
import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generator for AS 1/2 actions documentation.
 *
 * @author JPEXS
 */
public class As12PCodeDocs extends AbstractDocs {

    static ResourceBundle prop;
    private static List<String> allInstructionNames = Arrays.asList("gotoframe",
            "geturl",
            "nextframe",
            "prevframe",
            "play",
            "stop",
            "togglequality",
            "stopsounds",
            "waitforframe",
            "settarget",
            "gotolabel",
            "push",
            "pop",
            "add",
            "subtract",
            "multiply",
            "divide",
            "equals",
            "less",
            "and",
            "or",
            "not",
            "stringequals",
            "stringlength",
            "stringadd",
            "stringextract",
            "stringless",
            "mbstringlength",
            "mbstringextract",
            "tointeger",
            "chartoascii",
            "asciitochar",
            "mbchartoascii",
            "mbasciitochar",
            "jump",
            "if",
            "call",
            "getvariable",
            "setvariable",
            "geturl2",
            "gotoframe2",
            "settarget2",
            "getproperty",
            "setproperty",
            "clonesprite",
            "removesprite",
            "startdrag",
            "enddrag",
            "waitforframe2",
            "trace",
            "gettime",
            "randomnumber",
            "callfunction",
            "callmethod",
            "constantpool",
            "definefunction",
            "definelocal",
            "definelocal2",
            "delete",
            "delete2",
            "enumerate",
            "equals2",
            "getmember",
            "initarray",
            "initobject",
            "newmethod",
            "newobject",
            "setmember",
            "targetpath",
            "with",
            "tonumber",
            "tostring",
            "typeof",
            "add2",
            "less2",
            "modulo",
            "bitand",
            "bitlshift",
            "bitor",
            "bitrshift",
            "biturshift",
            "bitxor",
            "decrement",
            "increment",
            "pushduplicate",
            "return",
            "stackswap",
            "storeregister",
            "instanceof",
            "enumerate2",
            "strictequals",
            "greater",
            "stringgreater",
            "definefunction2",
            "extends",
            "castop",
            "implementsop",
            "try",
            "throw"
    );

    static {
        prop = ResourceBundle.getBundle("com.jpexs.decompiler.flash.locales.docs.pcode.AS2");
    }

    static final String NEWLINE = "\r\n";

    /**
     * Constructor.
     */
    public As12PCodeDocs() {
        
    }
    
    private static String makeIdent(String name) {
        StringBuilder identName = new StringBuilder();
        boolean cap = false;
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c == '_') {
                cap = true;
                continue;
            }
            if (cap) {
                identName.append(c);
                cap = false;
            } else {
                identName.append(Character.toLowerCase(c));
            }
        }
        return identName.toString();
    }

    /**
     * Gets documentation for given instruction.
     *
     * @param insName Instruction name
     * @param ui If true, returns documentation for UI
     * @param standalone If true, returns standalone documentation
     * @param nightMode If true, uses night mode
     * @param argumentToHilight Argument to hilight
     * @return Documentation for given instruction
     */
    public static String getDocsForIns(String insName, boolean ui, boolean standalone, boolean nightMode, int argumentToHilight) {
        insName = insName.toLowerCase();
        if (!allInstructionNames.contains(insName)) {
            return null;
        }
        final String cacheKey = insName + "|" + (ui ? 1 : 0) + "|" + (standalone ? 1 : 0);
        String v = docsCache.get(cacheKey);
        if (v != null) {
            return hilightArgument(v, argumentToHilight);
        }

        StringBuilder sb = new StringBuilder();
        if (standalone) {
            sb.append(htmlHeader("", getStyle(), nightMode));
        }

        String insShortDescription = getProperty("action." + insName + ".shortDescription");
        String insDescription = getProperty("action." + insName + ".description");
        String stackBefore = getProperty("action." + insName + ".stackBefore");
        String stackAfter = getProperty("action." + insName + ".stackAfter");
        String instructionCode = getProperty("action." + insName + ".code");
        String swfVersion = getProperty("action." + insName + ".swfVersion");
        String insRealName = getProperty("action." + insName + ".name");

        if (stackBefore.trim().isEmpty()) {
            stackBefore = getProperty("ui.stack.before.empty");
        } else {
            stackBefore = getProperty("ui.stack.before") + stackBefore;
        }
        if (stackAfter.trim().isEmpty()) {
            stackAfter = getProperty("ui.stack.before.empty");
        } else {
            stackAfter = getProperty("ui.stack.before") + stackAfter;
        }
        stackBefore = "<span class=\"stack-before\">" + stackBefore + "</span>";
        stackAfter = "<span class=\"stack-after\">" + stackAfter + "</span>";

        String stack = stackBefore + "<span class=\"stack-to\">" + getProperty("ui.stack.to") + "</span>" + stackAfter;
        String operandsDoc = getProperty("action." + insName + ".operands");

        if (standalone) {
            sb.append("<body class=\"");
            if (nightMode) {
                sb.append("standalonenight");
            } else {
                sb.append("standalone");
            }
            sb.append("\">");
        }

        sb.append("<div class=\"instruction");
        sb.append("\">");

        sb.append("<div class=\"instruction-signature\"><span class=\"instruction-code\">").append(instructionCode).append("</span> <strong class=\"instruction-name\">").append(insRealName).append("</strong>");

        if (!operandsDoc.isEmpty()) {
            sb.append(" ");
        }
        sb.append("<span class=\"instruction-operands\">");
        sb.append(operandsDoc);
        sb.append("</span>");
        sb.append("</div>").append(NEWLINE);

        sb.append("<div class=\"short-description\">").append(insShortDescription).append("</div>").append(NEWLINE);

        if (!insDescription.trim().isEmpty()) {
            sb.append("<div class=\"description\">").append("<strong class=\"description-title\">").append(getProperty("ui.description")).append("</strong>").append(insDescription).append("</div>").append(NEWLINE);
        }
        sb.append("<div class=\"stack\"><strong class=\"stack-title\">").append(getProperty("ui.stack")).append("</strong><span class=\"stack-values\">").append(stack).append("</span>").append("</div>").append(NEWLINE);
        sb.append("<div class=\"swfVersion\"><strong class=\"swfVersion-title\">").append(getProperty("ui.swfVersion")).append("</strong><span class=\"swfVersion-value\">").append(swfVersion).append("</span>").append("</div>").append(NEWLINE);
        sb.append("</div>").append(NEWLINE); //.instruction
        if (standalone) {
            sb.append("</body>");
            sb.append(htmlFooter());
        }
        String r = sb.toString();
        docsCache.put(cacheKey, r);
        return hilightArgument(r, argumentToHilight);
    }

    /**
     * Gets JS code for documentation.
     * @return JS code for documentation
     */
    public static String getJs() {
        String cached = docsCache.get("__js");
        if (cached != null) {
            return cached;
        }
        String js = "";
        InputStream is = As12PCodeDocs.class.getResourceAsStream("/com/jpexs/decompiler/flash/docs/docs.js");
        if (is == null) {
            Logger.getLogger(As12PCodeDocs.class.getName()).log(Level.SEVERE, "docs.js needed for documentation not found");
        } else {
            js = new String(Helper.readStream(is), Utf8Helper.charset);
        }

        docsCache.put("__js", js);
        return js;
    }

    /**
     * Gets all instruction documentation.
     * @param nightMode If true, uses night mode
     * @return All instruction documentation
     */
    public static String getAllInstructionDocs(boolean nightMode) {

        String jsData = "";
        jsData += "var txt_filter_hide = \"" + getProperty("ui.filter.hide") + "\";" + NEWLINE;
        jsData += "var txt_filter_byname = \"" + getProperty("ui.filter.byname") + "\";" + NEWLINE;
        jsData += "var txt_filter_order = \"" + getProperty("ui.filter.order") + "\";" + NEWLINE;
        jsData += "var txt_filter_order_code = \"" + getProperty("ui.filter.order.code") + "\";" + NEWLINE;
        jsData += "var txt_filter_order_name = \"" + getProperty("ui.filter.order.name") + "\";" + NEWLINE;

        jsData += "var order_set = \"name\";";
        jsData += "var flags_set = {};" + NEWLINE;
        jsData += "var flags = null;" + NEWLINE;
        StringBuilder sb = new StringBuilder();
        sb.append(htmlHeader(jsData + getJs(), getStyle(), nightMode));
        sb.append("<body");
        if (nightMode) {
            sb.append(" class=\"night\"");
        }
        sb.append(">");
        sb.append("\t\t<h1>").append(getProperty("ui.list.heading")).append("</h1>").append(NEWLINE);
        sb.append("<span id=\"js-switcher\" class=\"js\"></span>");
        sb.append("\t\t<ul class=\"instruction-list\">").append(NEWLINE);
        Set<String> s = new TreeSet<>(allInstructionNames);
        for (String name : s) {
            sb.append("\t\t\t<li class=\"instruction-item\">").append(NEWLINE);
            sb.append("\t\t\t\t").append(getDocsForIns(name, false, false, nightMode, -1).trim().replace(NEWLINE, NEWLINE + "\t\t\t\t")).append(NEWLINE);
            sb.append("\t\t\t</li>").append(NEWLINE);
        }
        sb.append("\t\t</ul>").append(NEWLINE);
        sb.append("\t</body>").append(NEWLINE);
        sb.append(htmlFooter());
        return sb.toString();
    }

    /**
     * Main method.
     * @param args Arguments
     * @throws UnsupportedEncodingException If encoding is not supported
     */
    public static void main(String[] args) throws UnsupportedEncodingException {
        System.out.println(getAllInstructionDocs(false));
    }

    /**
     * Gets HTML header.
     * @param js JS code
     * @param style Style
     * @param nightMode If true, uses night mode
     * @return HTML header
     */
    protected static String htmlHeader(String js, String style, boolean nightMode) {
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
