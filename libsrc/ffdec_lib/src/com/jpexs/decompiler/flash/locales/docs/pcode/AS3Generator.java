package com.jpexs.decompiler.flash.locales.docs.pcode;

import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2InstructionFlag;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
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
public class AS3Generator {

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

    public static void main(String[] args) throws IOException {
        Properties prop = new Properties();
        prop.load(AS3Generator.class.getClassLoader().getResourceAsStream("com/jpexs/decompiler/flash/locales/docs/pcode/AS3.properties"));

        Map<AVM2InstructionFlag, String> flagDescriptions = new HashMap<>();

        for (AVM2InstructionFlag flg : AVM2InstructionFlag.values()) {
            String flagIdent = makeIdent(flg.toString());
            String flagDescription = prop.getProperty("instructionFlag." + flagIdent);
            flagDescriptions.put(flg, flagDescription);
        }

        for (InstructionDefinition def : AVM2Code.allInstructionSet) {
            if (def == null) {
                continue;
            }
            System.out.println("=========================");
            String insName = def.instructionName;

            String insShortDescription = prop.getProperty("instruction." + insName + ".shortDescription");
            String insDescription = prop.getProperty("instruction." + insName + ".description");
            String stackBefore = def.hasFlag(AVM2InstructionFlag.UNKNOWN_STACK) ? "???" : prop.getProperty("instruction." + insName + ".stackBefore");
            String stackAfter = def.hasFlag(AVM2InstructionFlag.UNKNOWN_STACK) ? "???" : prop.getProperty("instruction." + insName + ".stackAfter");
            String operandsDoc = def.hasFlag(AVM2InstructionFlag.UNKNOWN_OPERANDS) ? "???" : prop.getProperty("instruction." + insName + ".operands");

            System.out.println(String.format("0x%02X", def.instructionCode) + " " + insName + ": " + insShortDescription);

            if (!insDescription.trim().isEmpty()) {
                System.out.println("Description: " + insDescription);
            }
            System.out.println("Stack before: " + stackBefore);
            System.out.println("Stack after: " + stackAfter);
            boolean flagsPrinted = false;

            System.out.print("Operands: ");

            if (def.hasFlag(AVM2InstructionFlag.UNKNOWN_OPERANDS)) {
                System.out.println("???");
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
                                System.out.print(", ");
                            } else {
                                first = false;
                            }
                            opDoc = operandsDocs[i + j];
                            operandTypeCombined = operandTypesCombined[j];
                            if (opDoc.equals("...")) {
                                System.out.print("...");
                            } else {
                                System.out.print(opDoc + ":" + operandTypeCombined);
                            }
                        }
                    } else {
                        if (!first) {
                            System.out.print(", ");
                        } else {
                            first = false;
                        }
                        if (opDoc.equals(operandTypeRaw)) {
                            System.out.print(operandTypeCombined);
                        } else {
                            System.out.print(opDoc + ":" + operandTypeCombined);
                        }
                    }
                }
                if (def.operands.length == 0) {
                    //System.out.print("");
                }
                System.out.println("");
            }

            AVM2InstructionFlag flags[] = def.flags.clone();
            Arrays.sort(flags, Enum::compareTo);

            for (AVM2InstructionFlag fl : flags) {
                if (fl != AVM2InstructionFlag.UNKNOWN_OPERANDS && fl != AVM2InstructionFlag.UNKNOWN_STACK) {
                    if (!flagsPrinted) {
                        flagsPrinted = true;
                        System.out.println("Flags:");
                    }
                    System.out.println(" - " + flagDescriptions.get(fl));
                }
            }
        }
    }
}
