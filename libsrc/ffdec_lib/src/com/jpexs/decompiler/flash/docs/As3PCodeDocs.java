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
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2InstructionFlag;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generator for AVM2 instruction set documentation.
 *
 * @author JPEXS
 */
public class As3PCodeDocs extends AbstractDocs {

    static ResourceBundle prop;
    private static final Map<AVM2InstructionFlag, String> flagDescriptions = new HashMap<>();

    private static final Map<String, InstructionDefinition> nameToDef = new HashMap<>();

    static final String NEWLINE = "\r\n";

    static {
        prop = ResourceBundle.getBundle("com.jpexs.decompiler.flash.locales.docs.pcode.AS3");
        for (InstructionDefinition def : AVM2Code.allInstructionSet) {
            if (def == null) {
                continue;
            }
            nameToDef.put(def.instructionName, def);
        }

        for (AVM2InstructionFlag flg : AVM2InstructionFlag.values()) {
            String flagIdent = makeIdent(flg.toString());
            String flagDescription = getProperty("instructionFlag." + flagIdent);
            flagDescriptions.put(flg, flagDescription);
        }
    }

    /**
     * Constructor.
     */
    public As3PCodeDocs() {

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
     * Get documentation for instruction.
     * @param insName Name of the instruction
     * @param showDataSize Show data size
     * @param ui UI
     * @param withStyle With style
     * @param nightMode Night mode
     * @param argumentToHilight Argument to hilight
     * @return Documentation for instruction
     */
    public static String getDocsForIns(String insName, boolean showDataSize, boolean ui, boolean withStyle, boolean nightMode, int argumentToHilight) {
        if (!nameToDef.containsKey(insName)) {
            return null;
        }
        return getDocsForIns(nameToDef.get(insName), showDataSize, ui, withStyle, nightMode, argumentToHilight);
    }

    /**
     * Get documentation for instruction.
     * @param def Instruction definition
     * @param showDataSize Show data size
     * @param ui UI
     * @param standalone Standalone
     * @param nightMode Night mode
     * @param argumentToHilight Argument to hilight
     * @return Documentation for instruction
     */
    public static String getDocsForIns(InstructionDefinition def, boolean showDataSize, boolean ui, boolean standalone, boolean nightMode, int argumentToHilight) {
        final String cacheKey = def.instructionName + "|" + (showDataSize ? 1 : 0) + "|" + (ui ? 1 : 0) + "|" + (standalone ? 1 : 0);
        String v = docsCache.get(cacheKey);
        if (v != null) {
            return hilightArgument(v, argumentToHilight);
        }

        StringBuilder sb = new StringBuilder();
        if (standalone) {
            sb.append(htmlHeader("", getStyle(), nightMode));
        }
        String insName = def.instructionName;

        String insShortDescription = getProperty("instruction." + insName + ".shortDescription");
        String insDescription = getProperty("instruction." + insName + ".description");
        String stackBefore = def.hasFlag(AVM2InstructionFlag.UNKNOWN_STACK) ? "" : getProperty("instruction." + insName + ".stackBefore");
        String stackAfter = def.hasFlag(AVM2InstructionFlag.UNKNOWN_STACK) ? "" : getProperty("instruction." + insName + ".stackAfter");
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

        String stack = def.hasFlag(AVM2InstructionFlag.UNKNOWN_STACK) ? getProperty("ui.unknown") : stackBefore + "<span class=\"stack-to\">" + getProperty("ui.stack.to") + "</span>" + stackAfter;
        String operandsDoc = def.hasFlag(AVM2InstructionFlag.UNKNOWN_OPERANDS) ? getProperty("ui.unknown") : getProperty("instruction." + insName + ".operands");

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

        for (AVM2InstructionFlag fl : def.flags) {
            sb.append(" instruction-flag-").append(makeIdent(fl.toString()));
        }
        sb.append("\">");

        sb.append("<div class=\"instruction-signature\"><span class=\"instruction-code\">").append(String.format("0x%02X", def.instructionCode)).append("</span> <strong class=\"instruction-name\">").append(insName).append("</strong>");

        if (def.hasFlag(AVM2InstructionFlag.UNKNOWN_OPERANDS)) {
            sb.append(" ").append(getProperty("ui.unknown")).append(NEWLINE);
        } else if (ui && insName.equals("lookupswitch")) {
            sb.append(" ");
            sb.append("<span class=\"instruction-operands\">");
            sb.append(getProperty("instruction.lookupswitch.operands.ui"));
            sb.append("</span>");
        } else {
            String[] operandsDocs = operandsDoc.split(", ?");
            boolean first = true;
            if (def.operands.length > 0) {
                sb.append(" ");
            }
            sb.append("<span class=\"instruction-operands\">");
            for (int i = 0; i < def.operands.length; i++) {
                int op = def.operands[i];
                String opDoc = operandsDocs[i];
                String operandTypeRaw = AVM2Code.operandTypeToString(op, false);
                String operandTypeCombined = AVM2Code.operandTypeToString(op, true);
                if (operandTypeCombined.contains(", ")) {
                    String[] operandTypesCombined = operandTypeCombined.split(", ?");
                    String[] operandTypesRaw = operandTypeRaw.split(", ?");

                    for (int j = 0; j < operandTypesCombined.length; j++) {
                        if (!first) {
                            sb.append(", ");
                        } else {
                            first = false;
                        }
                        opDoc = operandsDocs[i + j];
                        operandTypeCombined = operandTypesCombined[j];
                        operandTypeRaw = operandTypesRaw[j];
                        operandTypeRaw = getProperty("operandType." + operandTypeRaw + (ui ? ".uiName" : ".name"));
                        if (opDoc.equals("...")) {
                            sb.append("...");
                        } else {
                            sb.append(opDoc);
                            sb.append(":").append(showDataSize ? operandTypeCombined : operandTypeRaw);
                        }
                    }
                } else {
                    if (!first) {
                        sb.append(", ");
                    } else {
                        first = false;
                    }
                    operandTypeRaw = getProperty("operandType." + operandTypeRaw + (ui ? ".uiName" : ".name"));

                    if (opDoc.equals(operandTypeRaw)) {
                        sb.append(showDataSize ? operandTypeCombined : operandTypeRaw);
                    } else {
                        sb.append(opDoc).append(":").append(showDataSize ? operandTypeCombined : operandTypeRaw);
                    }
                }
            }
            sb.append("</span>");
        }
        sb.append("</div>").append(NEWLINE);

        sb.append("<div class=\"short-description\">").append(insShortDescription).append("</div>").append(NEWLINE);

        if (!insDescription.trim().isEmpty()) {
            sb.append("<div class=\"description\">").append("<strong class=\"description-title\">").append(getProperty("ui.description")).append("</strong>").append(insDescription).append("</div>").append(NEWLINE);
        }
        sb.append("<div class=\"stack\"><strong class=\"stack-title\">").append(getProperty("ui.stack")).append("</strong><span class=\"stack-values " + (def.hasFlag(AVM2InstructionFlag.UNKNOWN_STACK) ? " unknown" : "") + "\">").append(stack).append("</span>").append("</div>").append(NEWLINE);
        boolean flagsPrinted = false;

        AVM2InstructionFlag[] flags = def.flags.clone();
        Arrays.sort(flags, Enum::compareTo);

        for (AVM2InstructionFlag fl : flags) {
            if (!flagsPrinted) {
                flagsPrinted = true;
                sb.append("<strong class=\"flags-title\">").append(getProperty("ui.flags")).append("</strong>").append("<br />").append(NEWLINE).append("<ul class=\"flags\">").append(NEWLINE);
            }
            sb.append("\t<li class=\"flag flag-").append(makeIdent(fl.toString())).append("\">").append(flagDescriptions.get(fl));
            if (fl == AVM2InstructionFlag.DEPRECATED) {
                String depDetail = getProperty("instruction." + insName + ".deprecated");
                if (depDetail != null) {
                    sb.append(": <span class=\"flag-deprecated-detail\">").append(depDetail).append("</span>");
                }
            }
            sb.append("</li>").append(NEWLINE);
        }
        if (flagsPrinted) {
            sb.append("</ul>").append(NEWLINE);
        }
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
     * Gets JavaScript.
     * @return JavaScript
     */
    public static String getJs() {
        String cached = docsCache.get("__js");
        if (cached != null) {
            return cached;
        }
        String js = "";
        InputStream is = As3PCodeDocs.class.getResourceAsStream("/com/jpexs/decompiler/flash/docs/docs.js");
        if (is == null) {
            Logger.getLogger(As3PCodeDocs.class.getName()).log(Level.SEVERE, "docs.js needed for documentation not found");
        } else {
            js = new String(Helper.readStream(is), Utf8Helper.charset);
        }

        docsCache.put("__js", js);
        return js;
    }

    /**
     * Gets all instruction documentation.
     * @param nightMode Night mode
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
        jsData += "var flags = {};" + NEWLINE;
        for (AVM2InstructionFlag f : AVM2InstructionFlag.values()) {
            jsData += "flags[\"" + makeIdent(f.toString()) + "\"] = \"" + Helper.escapeJavaString(flagDescriptions.get(f)) + "\";" + NEWLINE;
            jsData += "flags_set[\"" + makeIdent(f.toString()) + "\"] = false;" + NEWLINE;
        }

        AVM2InstructionFlag[] hideFlags = new AVM2InstructionFlag[]{AVM2InstructionFlag.NO_FLASH_PLAYER};
        for (AVM2InstructionFlag f : hideFlags) {
            jsData += "flags_set[\"" + makeIdent(f.toString()) + "\"] = true;" + NEWLINE;
        }

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
        Set<String> s = new TreeSet<>(nameToDef.keySet());
        for (String name : s) {
            InstructionDefinition def = nameToDef.get(name);
            if (def == null) {
                continue;
            }
            sb.append("\t\t\t<li class=\"instruction-item\">").append(NEWLINE);
            sb.append("\t\t\t\t").append(getDocsForIns(def, true, false, false, nightMode, -1).trim().replace(NEWLINE, NEWLINE + "\t\t\t\t")).append(NEWLINE);
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
     * @throws UnsupportedEncodingException On unsupported encoding
     */
    public static void main(String[] args) throws UnsupportedEncodingException {
        System.out.println(getAllInstructionDocs(false));
    }

    /**
     * Gets HTML header.
     * @param js JavaScript
     * @param style Style
     * @param nightMode Night mode
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
