/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.abc.avm2;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.RenameType;
import com.jpexs.decompiler.graph.DottedChain;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

/**
 *
 * @author JPEXS
 */
public class AVM2Deobfuscation {

    private static final Random rnd = new Random();

    private static final int DEFAULT_FOO_SIZE = 10;

    public static final String[] reservedWords = {
        "as", "break", "case", "catch", "class", "const", "continue", "default", "delete", "do", "each", "else",
        "extends", "false", "finally", "for", "function", "get", "if", "implements", "import", "in", "instanceof",
        "interface", "internal", "is", "native", "new", "null", "override", "package", "private", "protected", "public",
        "return", "set", "super", "switch", "this", "throw", "true", "try", "typeof", "use", "var", /*"void",*/ "while",
        "with", "dynamic", "default", "final", "in", "static"};

    public static final String VALID_FIRST_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_";

    public static final String VALID_NEXT_CHARACTERS = VALID_FIRST_CHARACTERS + "0123456789";

    public static final String VALID_NS_CHARACTERS = ".:$";

    public static final String FOO_CHARACTERS = "bcdfghjklmnpqrstvwz";

    public static final String FOO_JOIN_CHARACTERS = "aeiouy";

    private final SWF swf;

    private final AVM2ConstantPool constants;

    private final Map<String, Integer> usageTypesCount = new HashMap<>();

    public static final DottedChain FLASH_PROXY = new DottedChain("flash", "utils", "flash_proxy");

    public static final DottedChain BUILTIN = new DottedChain("-");

    public AVM2Deobfuscation(SWF swf, AVM2ConstantPool constants) {
        this.swf = swf;
        this.constants = constants;
    }

    private static boolean isReserved(String s) {
        for (String rw : reservedWords) {
            if (rw.equals(s.trim())) {
                return true;
            }
        }
        return false;
    }

    private boolean isValidNSPart(String s) {
        boolean isValid = true;
        if (isReserved(s)) {
            isValid = false;
        }

        if (isValid) {
            for (int i = 0; i < s.length(); i++) {
                if (s.charAt(i) > 127) {
                    isValid = false;
                    break;
                }
            }
        }
        if (isValid) {
            Pattern pat = Pattern.compile("^([" + Pattern.quote(VALID_FIRST_CHARACTERS) + "]" + "[" + Pattern.quote(VALID_FIRST_CHARACTERS + VALID_NEXT_CHARACTERS + VALID_NS_CHARACTERS) + "]*)*$");
            if (!pat.matcher(s).matches()) {
                isValid = false;
            }
        }
        return isValid;
    }

    public DottedChain builtInNs(String ns) {
        if (ns == null) {
            return null;
        }
        if (ns.equals("http://www.adobe.com/2006/actionscript/flash/proxy")) {
            return FLASH_PROXY;
        }
        if (ns.equals("http://adobe.com/AS3/2006/builtin")) {
            return BUILTIN;
        }
        return null;
    }

    private String fooString(HashMap<DottedChain, DottedChain> deobfuscated, String orig, boolean firstUppercase, int rndSize, String usageType, RenameType renameType) {
        boolean exists;
        String ret;
        int pos = 0;

        if (usageType == null) {
            usageType = "name";
        }
        if (usageTypesCount.containsKey(usageType)) {
            pos = usageTypesCount.get(usageType);
        }

        loopfoo:
        do {
            exists = false;
            ret = "";
            if (renameType == RenameType.TYPENUMBER) {
                pos++;
                ret = usageType + "_" + pos;
            } else if (renameType == RenameType.RANDOMWORD) {
                int len = 3 + rnd.nextInt(rndSize - 3);

                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < len; i++) {
                    char c;
                    if ((i % 2) == 0) {
                        c = FOO_CHARACTERS.charAt(rnd.nextInt(FOO_CHARACTERS.length()));
                    } else {
                        c = FOO_JOIN_CHARACTERS.charAt(rnd.nextInt(FOO_JOIN_CHARACTERS.length()));
                    }
                    if (i == 0 && firstUppercase) {
                        c = Character.toUpperCase(c);
                    }
                    sb.append(c);
                }

                ret = sb.toString();
            }
            if (swf.as3StringConstantExists(ret)) {
                exists = true;
                rndSize += 1;
                continue loopfoo;

            }
            if (isReserved(ret)) {
                exists = true;
                rndSize += 1;
                continue;
            }
            if (deobfuscated.containsValue(DottedChain.parse(ret))) {
                exists = true;
                rndSize += 1;
                continue;
            }
        } while (exists);
        usageTypesCount.put(usageType, pos);
        deobfuscated.put(DottedChain.parse(orig), DottedChain.parse(ret));
        return ret;
    }

    public int deobfuscatePackageName(Map<Integer, String> stringUsageTypes, Set<Integer> stringUsages, HashMap<DottedChain, DottedChain> namesMap, int strIndex, RenameType renameType) {
        if (strIndex <= 0) {
            return strIndex;
        }
        String s = constants.getString(strIndex);
        if (builtInNs(s) != null) {
            return strIndex;
        }
        boolean isValid = isValidNSPart(s);
        if (!isValid) {
            DottedChain sChain = DottedChain.parse(s);
            DottedChain newName;
            if (namesMap.containsKey(sChain)) {
                newName = namesMap.get(sChain);
                constants.setString(strIndex, newName.toRawString());
            } else {
                List<String> ret = new ArrayList<>();
                for (int p = 0; p < sChain.size(); p++) {
                    String part = sChain.get(p);
                    if (!isValidNSPart(part)) {
                        ret.add(fooString(namesMap, part, false, DEFAULT_FOO_SIZE, "package", renameType));
                    } else {
                        ret.add(part);
                    }
                }
                newName = new DottedChain(ret);
                namesMap.put(sChain, newName);
            }
            if (stringUsages.contains(strIndex)) {
                strIndex = constants.addString(newName.toRawString());
            } else {
                constants.setString(strIndex, newName.toRawString());
            }

        }
        return strIndex;
    }

    public int deobfuscateName(Map<Integer, String> stringUsageTypes, Set<Integer> stringUsages, Set<Integer> namespaceUsages, HashMap<DottedChain, DottedChain> namesMap, int strIndex, boolean firstUppercase, RenameType renameType) {
        if (strIndex <= 0) {
            return strIndex;
        }
        String s = constants.getString(strIndex);
        boolean isValid = true;
        if (isReserved(s)) {
            isValid = false;
        }

        if (isValid) {
            for (int i = 0; i < s.length(); i++) {
                if (s.charAt(i) > 127) {
                    isValid = false;
                    break;
                }
            }
        }

        if (isValid) {
            Pattern pat = Pattern.compile("^[" + Pattern.quote(VALID_FIRST_CHARACTERS) + "]" + "[" + Pattern.quote(VALID_FIRST_CHARACTERS + VALID_NEXT_CHARACTERS) + "]*$");
            if (!pat.matcher(s).matches()) {
                isValid = false;
            }
        }

        if (!isValid) {
            DottedChain newname;
            DottedChain sChain = DottedChain.parse(s);
            if (namesMap.containsKey(sChain)) {
                newname = namesMap.get(sChain);
            } else {
                String str = fooString(namesMap, constants.getString(strIndex), firstUppercase, DEFAULT_FOO_SIZE, stringUsageTypes.get(strIndex), renameType);
                newname = DottedChain.parse(str);
            }
            if (stringUsages.contains(strIndex) || namespaceUsages.contains(strIndex)) { // this name is already referenced as String
                strIndex = constants.addString(s); // add new index
            }
            constants.setString(strIndex, newname.toRawString());
            if (!namesMap.containsKey(sChain)) {
                namesMap.put(sChain, DottedChain.parse(constants.getString(strIndex)));
            }
        }
        return strIndex;
    }
}
