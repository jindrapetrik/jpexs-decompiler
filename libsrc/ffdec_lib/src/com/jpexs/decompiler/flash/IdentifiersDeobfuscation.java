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
package com.jpexs.decompiler.flash;

import com.jpexs.decompiler.flash.abc.RenameType;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.helpers.Cache;
import com.jpexs.helpers.Helper;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

/**
 *
 * @author JPEXS
 */
public class IdentifiersDeobfuscation {

    private final Random rnd = new Random();

    private final int DEFAULT_FOO_SIZE = 10;

    public HashSet<String> allVariableNamesStr = new HashSet<>();

    private final HashMap<String, Integer> typeCounts = new HashMap<>();

    public static final String VALID_FIRST_CHARACTERS = "\\p{Lu}\\p{Ll}\\p{Lt}\\p{Lm}\\p{Lo}_$";

    public static final String VALID_NEXT_CHARACTERS = VALID_FIRST_CHARACTERS + "\\p{Nl}\\p{Mn}\\p{Mc}\\p{Nd}\\p{Pc}";

    public static final Pattern IDENTIFIER_PATTERN = Pattern.compile("^[" + VALID_FIRST_CHARACTERS + "][" + VALID_NEXT_CHARACTERS + "]*$");

    public static final String FOO_CHARACTERS = "bcdfghjklmnpqrstvwz";

    public static final String FOO_JOIN_CHARACTERS = "aeiouy";

    // http://help.adobe.com/en_US/AS2LCR/Flash_10.0/help.html?content=00000477.html
    public static final String[] reservedWordsAS2 = {
        // is "add" really a keyword? documentation says yes, but I can create "add" variable in CS6...
        // "add",
        "and", "break", "case", "catch", "class", "continue", "default", "delete", "do", "dynamic", "else",
        "eq", "extends", "false", "finally", "for", "function", "ge", "get", "gt", "if", "ifFrameLoaded", "implements",
        "import", "in", "instanceof", "interface", "intrinsic", "le",
        // is "it" really a keyword? documentation says yes, but I can create "it" variable in CS6...
        // "it",
        "ne", "new", "not", "null", "on", "onClipEvent",
        "or", "private", "public", "return", "set", "static", "super", "switch", "tellTarget", "this", "throw", "try",
        "typeof", "undefined", "var", "void", "while", "with"
    };

    // http://www.adobe.com/devnet/actionscript/learning/as3-fundamentals/syntax.html
    public static final String[] reservedWordsAS3 = {
        "as", "break", "case", "catch", "class", "const", "continue", "default", "delete", "do", "else",
        "extends", "false", "finally", "for", "function", "if", "implements", "import", "in", "instanceof",
        "interface", "internal", "is", "new", "null", "package", "private", "protected", "public",
        "return", "super", "switch", "this", "throw",
        // is "to" really a keyword? documentation says yes, but I can create "to" variable...
        // "to",
        "true", "try", "typeof", "use", "var",
        "void", "while", "with"
    };

    //syntactic keywords - can be used as identifiers, but that have special meaning in certain contexts
    public static final String[] syntacticKeywordsAS3 = {"each", "get", "set", "namespace", "include", "dynamic", "final", "native", "override", "static"};

    public static boolean isReservedWord(String s, boolean as3) {
        if (s == null) {
            return false;
        }
        String reservedWords[] = as3 ? reservedWordsAS3 : reservedWordsAS2;
        for (String rw : reservedWords) {
            if (rw.equals(s.trim())) {
                return true;
            }
        }
        return false;
    }

    private String fooString(boolean as3, HashMap<String, String> deobfuscated, String orig, boolean firstUppercase, int rndSize) {
        boolean exists;
        String ret;
        loopfoo:
        do {
            exists = false;
            int len = 3 + rnd.nextInt(rndSize - 3);
            ret = "";
            for (int i = 0; i < len; i++) {
                String c = "";
                if ((i % 2) == 0) {
                    c = "" + FOO_CHARACTERS.charAt(rnd.nextInt(FOO_CHARACTERS.length()));
                } else {
                    c = "" + FOO_JOIN_CHARACTERS.charAt(rnd.nextInt(FOO_JOIN_CHARACTERS.length()));
                }
                if (i == 0 && firstUppercase) {
                    c = c.toUpperCase(Locale.ENGLISH);
                }
                ret += c;
            }
            if (allVariableNamesStr.contains(ret)) {
                exists = true;
                rndSize += 1;
                continue loopfoo;
            }
            if (isReservedWord(ret, as3)) {
                exists = true;
                rndSize += 1;
                continue;
            }
            if (deobfuscated.containsValue(ret)) {
                exists = true;
                rndSize += 1;
                continue;
            }
        } while (exists);
        return ret;
    }

    public void deobfuscateInstanceNames(boolean as3, HashMap<String, String> namesMap, RenameType renameType, List<Tag> tags, Map<String, String> selected) {
        for (Tag t : tags) {
            if (t instanceof DefineSpriteTag) {
                deobfuscateInstanceNames(as3, namesMap, renameType, ((DefineSpriteTag) t).subTags, selected);
            }
            if (t instanceof PlaceObjectTypeTag) {
                PlaceObjectTypeTag po = (PlaceObjectTypeTag) t;
                String name = po.getInstanceName();
                if (name != null) {
                    String changedName = deobfuscateName(as3, name, false, "instance", namesMap, renameType, selected);
                    if (changedName != null) {
                        po.setInstanceName(changedName);
                        ((Tag) po).setModified(true);
                    }
                }
                String className = po.getClassName();
                if (className != null) {
                    String changedClassName = deobfuscateNameWithPackage(as3, className, namesMap, renameType, selected);
                    if (changedClassName != null) {
                        po.setClassName(changedClassName);
                        ((Tag) po).setModified(true);
                    }
                }
            }
        }
    }

    public String deobfuscatePackage(boolean as3, String pkg, HashMap<String, String> namesMap, RenameType renameType, Map<String, String> selected) {
        if (namesMap.containsKey(pkg)) {
            return namesMap.get(pkg);
        }
        String[] parts = null;
        if (pkg.contains(".")) {
            parts = pkg.split("\\.");
        } else {
            parts = new String[]{pkg};
        }
        StringBuilder ret = new StringBuilder();
        boolean isChanged = false;
        for (int p = 0; p < parts.length; p++) {
            if (p > 0) {
                ret.append(".");
            }
            String partChanged = deobfuscateName(as3, parts[p], false, "package", namesMap, renameType, selected);
            if (partChanged != null) {
                ret.append(partChanged);
                isChanged = true;
            } else {
                ret.append(parts[p]);
            }
        }
        if (isChanged) {
            String retStr = ret.toString();
            namesMap.put(pkg, retStr);
            return retStr;
        }
        return null;
    }

    public String deobfuscateNameWithPackage(boolean as3, String n, HashMap<String, String> namesMap, RenameType renameType, Map<String, String> selected) {
        String pkg = null;
        String name = "";
        if (n.contains(".")) {
            pkg = n.substring(0, n.lastIndexOf('.'));
            name = n.substring(n.lastIndexOf('.') + 1);
        } else {
            name = n;
        }
        boolean changed = false;
        if ((pkg != null) && (!pkg.isEmpty())) {
            String changedPkg = deobfuscatePackage(as3, pkg, namesMap, renameType, selected);
            if (changedPkg != null) {
                changed = true;
                pkg = changedPkg;
            }
        }
        String changedName = deobfuscateName(as3, name, true, "class", namesMap, renameType, selected);
        if (changedName != null) {
            changed = true;
            name = changedName;
        }
        if (changed) {
            String newClassName = "";
            if (pkg == null) {
                newClassName = name;
            } else {
                newClassName = pkg + "." + name;
            }
            return newClassName;
        }
        return null;
    }

    public static boolean isValidName(boolean as3, String s, String... exceptions) {
        for (String e : exceptions) {
            if (e.equals(s)) {
                return true;
            }
        }

        if (isReservedWord(s, as3)) {
            return false;
        }

        // simple fast test
        if (s.matches("^[a-zA-Z_\\$][a-zA-Z0-9_\\$]*$")) {
            return true;
        }
        // unicode test
        if (IDENTIFIER_PATTERN.matcher(s).matches()) {
            return true;
        }
        return false;
    }

    public String deobfuscateName(boolean as3, String s, boolean firstUppercase, String usageType, HashMap<String, String> namesMap, RenameType renameType, Map<String, String> selected) {
        boolean isValid = true;
        if (usageType == null) {
            usageType = "name";
        }

        if (selected != null) {
            if (selected.containsKey(s)) {
                return selected.get(s);
            }
        }

        isValid = isValidName(as3, s);
        if (!isValid) {
            if (namesMap.containsKey(s)) {
                return namesMap.get(s);
            } else {
                Integer cnt = typeCounts.get(usageType);
                if (cnt == null) {
                    cnt = 0;
                }

                String ret = null;
                if (renameType == RenameType.TYPENUMBER) {

                    boolean found;
                    do {
                        found = false;
                        cnt++;
                        ret = usageType + "_" + cnt;
                        found = allVariableNamesStr.contains(ret);
                    } while (found);
                    typeCounts.put(usageType, cnt);
                } else if (renameType == RenameType.RANDOMWORD) {
                    ret = fooString(as3, namesMap, s, firstUppercase, DEFAULT_FOO_SIZE);
                }
                namesMap.put(s, ret);
                return ret;
            }
        }
        return null;
    }

    public static String makeObfuscatedIdentifier(String s) {
        return "\u00A7" + escapeOIdentifier(s) + "\u00A7";
    }

    private static final Cache<String, String> as3NameCache = Cache.getInstance(false, "as3_ident");

    private static final Cache<String, String> as2NameCache = Cache.getInstance(false, "as2_ident");

    /**
     * Ensures identifier is valid and if not, uses paragraph syntax
     *
     * @param as3 Is ActionScript3
     * @param s Identifier
     * @param validExceptions Exceptions which are valid (e.g. some reserved
     * words)
     * @return
     */
    public static String printIdentifier(boolean as3, String s, String... validExceptions) {
        if (s.startsWith("\u00A7") && s.endsWith("\u00A7")) { // Assuming already printed - TODO:detect better
            return s;
        }

        for (String e : validExceptions) {
            if (e.equals(s)) {
                return s;
            }
        }
        Cache<String, String> nameCache = as3 ? as3NameCache : as2NameCache;

        if (nameCache.contains(s)) {
            return nameCache.get(s);
        }

        if (isValidName(as3, s, validExceptions)) {
            nameCache.put(s, s);
            return s;
        }
        String ret = makeObfuscatedIdentifier(s);
        nameCache.put(s, ret);
        return ret;
    }

    public static String printNamespace(boolean as3, String pkg, String... validNameExceptions) {
        Cache<String, String> nameCache = as3 ? as3NameCache : as2NameCache;
        if (nameCache.contains(pkg)) {
            return nameCache.get(pkg);
        }
        if (pkg.isEmpty()) {
            nameCache.put(pkg, pkg);
            return pkg;
        }
        String[] parts = null;
        if (pkg.contains(".")) {
            parts = pkg.split("\\.");
        } else {
            parts = new String[]{pkg};
        }
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                ret.append(".");
            }
            ret.append(printIdentifier(as3, parts[i], validNameExceptions));
        }
        String retStr = ret.toString();
        nameCache.put(pkg, retStr);
        return retStr;
    }

    public static String escapeOIdentifier(String s) {
        StringBuilder ret = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\n') {
                ret.append("\\n");
            } else if (c == '\r') {
                ret.append("\\r");
            } else if (c == '\t') {
                ret.append("\\t");
            } else if (c == '\b') {
                ret.append("\\b");
            } else if (c == '\t') {
                ret.append("\\t");
            } else if (c == '\f') {
                ret.append("\\f");
            } else if (c == '\\') {
                ret.append("\\\\");
            } else if (c == '\u00A7') {
                ret.append("\\\u00A7");
            } else if (c < 32) {
                ret.append("\\x").append(Helper.padZeros(Integer.toHexString((int) c), 2));
            } else {
                ret.append(c);
            }
        }

        return ret.toString();
    }
}
