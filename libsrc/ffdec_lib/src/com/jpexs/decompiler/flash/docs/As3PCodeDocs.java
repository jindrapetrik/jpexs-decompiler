package com.jpexs.decompiler.flash.docs;

import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2InstructionFlag;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.helpers.Cache;
import com.jpexs.helpers.Helper;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;

/**
 * Generator for AVM2 instruction set documentation.
 *
 * @author JPEXS
 */
public class As3PCodeDocs {

    private static ResourceBundle prop;
    private static Map<AVM2InstructionFlag, String> flagDescriptions = new HashMap<>();

    private static Cache<String, String> docsCache = Cache.getInstance(false, true, "as3DocsCache");
    private static Map<String, InstructionDefinition> nameToDef = new HashMap<>();

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

    public static String getDocsForIns(String insName, boolean showDataSize, boolean ui) {
        if (!nameToDef.containsKey(insName)) {
            return null;
        }
        return getDocsForIns(nameToDef.get(insName), showDataSize, ui);
    }

    private static String getProperty(String name) {
        if (prop.containsKey(name)) {
            return prop.getString(name);
        }
        return null;
    }

    public static String getDocsForIns(InstructionDefinition def, boolean showDataSize, boolean ui) {
        final String cacheKey = def.instructionName + "|" + (showDataSize ? 1 : 0) + "|" + (ui ? 1 : 0);
        String v = docsCache.get(cacheKey);
        if (v != null) {
            return v;
        }

        StringBuilder sb = new StringBuilder();
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
        String stack = def.hasFlag(AVM2InstructionFlag.UNKNOWN_STACK) ? getProperty("ui.unknown") : stackBefore + " => " + stackAfter;
        String operandsDoc = def.hasFlag(AVM2InstructionFlag.UNKNOWN_OPERANDS) ? getProperty("ui.unknown") : getProperty("instruction." + insName + ".operands");

        sb.append(String.format("0x%02X", def.instructionCode)).append(" <font color=\"blue\"><b>").append(insName).append("</b></font>").append(" ");

        if (def.hasFlag(AVM2InstructionFlag.UNKNOWN_OPERANDS)) {
            sb.append(getProperty("ui.unknown")).append(NEWLINE);
        } else {
            String[] operandsDocs = operandsDoc.split(", ?");
            boolean first = true;
            for (int i = 0; i < def.operands.length; i++) {
                int op = def.operands[i];
                String opDoc = operandsDocs[i];
                String operandTypeRaw = AVM2Code.operandTypeToString(op, false);
                String operandTypeCombined = AVM2Code.operandTypeToString(op, true);
                if (operandTypeCombined.contains(", ")) {
                    String operandTypesCombined[] = operandTypeCombined.split(", ?");
                    String operandTypesRaw[] = operandTypeRaw.split(", ?");

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
            sb.append(NEWLINE);
        }

        sb.append(insShortDescription).append(NEWLINE);

        if (!insDescription.trim().isEmpty()) {
            sb.append(insDescription).append(NEWLINE);
        }

        sb.append("<b>").append(getProperty("ui.stack")).append("</b>").append(stack).append(NEWLINE);
        boolean flagsPrinted = false;

        AVM2InstructionFlag flags[] = def.flags.clone();
        Arrays.sort(flags, Enum::compareTo);

        for (AVM2InstructionFlag fl : flags) {
            if (fl != AVM2InstructionFlag.UNKNOWN_OPERANDS && fl != AVM2InstructionFlag.UNKNOWN_STACK) {
                if (!flagsPrinted) {
                    flagsPrinted = true;
                    sb.append("<b>").append(getProperty("ui.flags")).append("</b>").append(NEWLINE);
                }
                sb.append(getProperty("ui.flags.beginning")).append(flagDescriptions.get(fl)).append(NEWLINE);
            }
        }
        String r = sb.toString();
        docsCache.put(cacheKey, r);
        return r;
    }

    public static String getAllInstructionDocs() {
        StringBuilder sb = new StringBuilder();
        String style = "li {list-style:none; padding:10px;border:1px solid black; border-bottom:none; border-collapse:collapse; } " + NEWLINE
                + "ul {padding-left:0; display:table; border-bottom:1px solid black;}" + NEWLINE;

        sb.append("<!DOCTYPE html>").append(NEWLINE).
                append("<html>").append(NEWLINE).
                append("<head>").append(NEWLINE).
                append("<style>").append(style).append("</style>").append(NEWLINE).
                append("<meta charset=\"UTF-8\">").append(NEWLINE).
                append("<title>").append(getProperty("ui.list.pageTitle")).append("</title>").append(NEWLINE).
                append("</head>").append(NEWLINE).
                append("<body>").append(NEWLINE);
        sb.append("<h1>").append(getProperty("ui.list.heading")).append("</h1>").append(NEWLINE);
        sb.append("<ul>").append(NEWLINE);
        Set<String> s = new TreeSet<>(nameToDef.keySet());
        for (String name : s) {
            InstructionDefinition def = nameToDef.get(name);
            if (def == null) {
                continue;
            }
            sb.append("<li>").append(NEWLINE);
            sb.append(getDocsForIns(def, true, false).replace(NEWLINE, "<br />" + NEWLINE));
            sb.append("</li>").append(NEWLINE);
        }
        sb.append("</ul>").append(NEWLINE);
        sb.append("</body>").append(NEWLINE);
        sb.append("</html>").append(NEWLINE);
        return sb.toString();
    }

    public static void main(String[] args) {
        System.out.println(getAllInstructionDocs());
    }
}
