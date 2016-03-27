package com.jpexs.decompiler.flash.locales.docs.pcode;

import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2InstructionFlag;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.helpers.Cache;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Generator for AVM2 instruction set documentation. TODO: use this somehow in
 * GUI
 *
 * @author JPEXS
 */
public class As3Docs {

    private static Properties prop;
    private static Map<AVM2InstructionFlag, String> flagDescriptions = new HashMap<>();

    private static Cache<String, String> docsCache = Cache.getInstance(false, true, "as3DocsCache");
    private static Map<String, InstructionDefinition> nameToDef = new HashMap<>();

    static {
        prop = new Properties();
        try {
            prop.load(As3Docs.class.getClassLoader().getResourceAsStream("com/jpexs/decompiler/flash/locales/docs/pcode/AS3.properties"));
        } catch (IOException e) {
            //ignore
        }

        for (InstructionDefinition def : AVM2Code.allInstructionSet) {
            if (def == null) {
                continue;
            }
            nameToDef.put(def.instructionName, def);
        }

        for (AVM2InstructionFlag flg : AVM2InstructionFlag.values()) {
            String flagIdent = makeIdent(flg.toString());
            String flagDescription = prop.getProperty("instructionFlag." + flagIdent);
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

    public static String getDocsForIns(String insName) {
        if (!nameToDef.containsKey(insName)) {
            return null;
        }
        return getDocsForIns(nameToDef.get(insName));
    }

    public static String getDocsForIns(InstructionDefinition def) {
        String v = docsCache.get(def.instructionName);
        if (v != null) {
            return v;
        }

        StringBuilder sb = new StringBuilder();
        String insName = def.instructionName;
        final String NEWLINE = "\r\n";

        String insShortDescription = prop.getProperty("instruction." + insName + ".shortDescription");
        String insDescription = prop.getProperty("instruction." + insName + ".description");
        String stackBefore = def.hasFlag(AVM2InstructionFlag.UNKNOWN_STACK) ? "???" : prop.getProperty("instruction." + insName + ".stackBefore");
        String stackAfter = def.hasFlag(AVM2InstructionFlag.UNKNOWN_STACK) ? "???" : prop.getProperty("instruction." + insName + ".stackAfter");
        String operandsDoc = def.hasFlag(AVM2InstructionFlag.UNKNOWN_OPERANDS) ? "???" : prop.getProperty("instruction." + insName + ".operands");

        sb.append(String.format("0x%02X", def.instructionCode)).append(" <font color=\"blue\"><b>").append(insName).append("</b></font>: ").append(insShortDescription).append(NEWLINE);

        if (!insDescription.trim().isEmpty()) {
            sb.append("<b>Description:</b> ").append(insDescription).append(NEWLINE);
        }
        sb.append("<b>Stack before:</b> ").append(stackBefore).append(NEWLINE);
        sb.append("<b>Stack after:</b> ").append(stackAfter).append(NEWLINE);
        boolean flagsPrinted = false;

        sb.append("<b>Operands:</b> ");

        if (def.hasFlag(AVM2InstructionFlag.UNKNOWN_OPERANDS)) {
            sb.append("???").append(NEWLINE);
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
                    for (int j = 0; j < operandTypesCombined.length; j++) {
                        if (!first) {
                            sb.append(", ");
                        } else {
                            first = false;
                        }
                        opDoc = operandsDocs[i + j];
                        operandTypeCombined = operandTypesCombined[j];
                        if (opDoc.equals("...")) {
                            sb.append("...");
                        } else {
                            sb.append(opDoc).append(":").append(operandTypeCombined);
                        }
                    }
                } else {
                    if (!first) {
                        sb.append(", ");
                    } else {
                        first = false;
                    }
                    if (opDoc.equals(operandTypeRaw)) {
                        sb.append(operandTypeCombined);
                    } else {
                        sb.append(opDoc).append(":").append(operandTypeCombined);
                    }
                }
            }
            sb.append(NEWLINE);
        }

        AVM2InstructionFlag flags[] = def.flags.clone();
        Arrays.sort(flags, Enum::compareTo);

        for (AVM2InstructionFlag fl : flags) {
            if (fl != AVM2InstructionFlag.UNKNOWN_OPERANDS && fl != AVM2InstructionFlag.UNKNOWN_STACK) {
                if (!flagsPrinted) {
                    flagsPrinted = true;
                    sb.append("Flags:").append(NEWLINE);
                }
                sb.append(" - ").append(flagDescriptions.get(fl)).append(NEWLINE);
            }
        }
        String r = sb.toString();
        docsCache.put(def.instructionName, r);
        return r;
    }

    public static void main(String[] args) throws IOException {

        for (InstructionDefinition def : AVM2Code.allInstructionSet) {
            if (def == null) {
                continue;
            }
            System.out.println("=========================");
            System.out.print(getDocsForIns(def));
        }
    }
}
