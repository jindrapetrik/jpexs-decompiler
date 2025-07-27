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
package com.jpexs.decompiler.flash;

import com.jpexs.decompiler.flash.abc.RenameType;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.ActionScriptLexer;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.ParsedSymbol;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.SymbolType;
import com.jpexs.decompiler.flash.asdoc.ActionScriptDocParser;
import com.jpexs.decompiler.flash.asdoc.AsDocComment;
import com.jpexs.decompiler.flash.asdoc.AsDocTag;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.model.DocCommentItem;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.helpers.Cache;
import com.jpexs.helpers.Helper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;
import natorder.NaturalOrderComparator;

/**
 * Identifiers deobfuscation.
 *
 * @author JPEXS
 */
public class IdentifiersDeobfuscation {

    /**
     * Prefix to be put instead of obfuscated name. It will by suffixed with a
     * number.
     */
    public static final String SAFE_STRING_PREFIX = "_SafeStr_";
    
    /**
     * Safe prefix for packages
     */
    public static final String SAFE_PACKAGE_PREFIX = "_SafePkg_";
    
    /**
     * Safe prefix for classes
     */
    public static final String SAFE_CLASS_PREFIX = "_SafeCls_";
    
    

    /**
     * Random number generator.
     */
    private static final Random rnd = new Random();

    /**
     * Default size of random string.
     */
    private final int DEFAULT_FOO_SIZE = 10;

    /**
     * All variable names.
     */
    public HashSet<String> allVariableNamesStr = new HashSet<>();

    /**
     * Type counts.
     */
    private final HashMap<String, Integer> typeCounts = new HashMap<>();

    /**
     * AS2 name cache.
     */
    private static final Cache<String, String> as2NameCache = Cache.getInstance(false, true, "as2_ident", true);

    /**
     * AS3 name cache.
     */
    private static final Cache<String, String> as3NameCache = Cache.getInstance(false, true, "as3_ident", true);

    /**
     * Valid first characters.
     */
    public static final String VALID_FIRST_CHARACTERS = "\\p{Lu}\\p{Ll}\\p{Lt}\\p{Lm}\\p{Lo}_$";

    /**
     * Valid next characters.
     */
    public static final String VALID_NEXT_CHARACTERS = VALID_FIRST_CHARACTERS + "\\p{Nl}\\p{Mn}\\p{Mc}\\p{Nd}\\p{Pc}";

    /**
     * Valid name pattern.
     */
    private static final Pattern VALID_NAME_PATTERN = Pattern.compile("^[a-zA-Z_\\$][a-zA-Z0-9_\\$]*$");

    /**
     * Identifier pattern.
     */
    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("^[" + VALID_FIRST_CHARACTERS + "][" + VALID_NEXT_CHARACTERS + "]*$");

    /**
     * Valid name pattern with dot.
     */
    private static final Pattern VALID_NAME_PATTERN_DOT = Pattern.compile("^[a-zA-Z_\\$][a-zA-Z0-9_.\\$]*$");

    /**
     * Identifier pattern with dot.
     */
    private static final Pattern IDENTIFIER_PATTERN_DOT = Pattern.compile("^[" + VALID_FIRST_CHARACTERS + "][" + VALID_NEXT_CHARACTERS + ".]*$");

    /**
     * Random name generator characters.
     */
    public static final String FOO_CHARACTERS = "bcdfghjklmnpqrstvwz";

    /**
     * Random name generator join characters.
     */
    public static final String FOO_JOIN_CHARACTERS = "aeiouy";

    /**
     * Reserved words in AS2.
     * http://help.adobe.com/en_US/AS2LCR/Flash_10.0/help.html?content=00000477.html
     */
    public static final String[] reservedWordsAS2 = {
        // is "add" really a keyword? documentation says yes, but I can create "add" variable in CS6...
        // "add",
        "and", "break", "case", "catch", "class", "continue", "default", "delete", "do", "dynamic",
        "each", //can be in variable definition
        "else",
        "eq", "extends",
        "false", //can be in variable definition
        "finally", "for", "function", "ge",
        "get", //can be in variable definition
        "gt", "if", "ifFrameLoaded", "implements",
        "import", "in", "instanceof", "interface", "intrinsic", "le",
        // is "it" really a keyword? documentation says yes, but I can create "it" variable in CS6...
        // "it",
        "ne", "new", "not",
        "null", //can be in variable definition
        "on", "onClipEvent",
        "or", "private", "public", "return",
        "set", //can be in variable definition
        "static",
        //allow as variable:
        //"super",
        "switch", "tellTarget",
        //allow as variable:
        //"this",
        "throw",
        "true", //can be in variable definition
        "try",
        "typeof", "undefined", "var", "void", "while", "with"
    };

    /**
     * Reserved words in AS3.
     * http://www.adobe.com/devnet/actionscript/learning/as3-fundamentals/syntax.html
     */
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

    /**
     * TODO, why do we have 2 different list? Moved from AVM2Deobfuscation
     */
    public static final String[] reservedWordsAS3_2 = {
        "as", "break", "case", "catch", "class", "const", "continue", "default", "delete", "do", "each", "else",
        "extends", "false", "finally", "for", "function", "get", "if", "implements", "import", "in", "instanceof",
        "interface", "internal", "is", "native", "new", "null", "override", "package", "private", "protected", "public",
        "return", "set", "super", "switch", "this", "throw", "true", "try", "typeof", "use", "var", /*"void",*/ "while",
        "with", "dynamic", "default", "final", "in", "static"};

    /**
     * Syntactic keywords - can be used as identifiers, but that have special
     * meaning in certain contexts
     */
    public static final String[] syntacticKeywordsAS3 = {"each", "get", "set", "namespace", "include", "dynamic", "final", "native", "override", "static"};

    /**
     * Checks if string is reserved word.
     *
     * @param s String
     * @param as3 Is ActionScript3
     */
    public static boolean isReservedWord(String s, boolean as3) {
        if (s == null) {
            return false;
        }
        String[] reservedWords = as3 ? reservedWordsAS3 : reservedWordsAS2;
        s = s.trim();
        for (String rw : reservedWords) {
            if (rw.equals(s)) {
                return true;
            }
        }
        return false;
    }

    /**
     * TODO: Why do we need this method???
     *
     * @param s String
     * @return True if string is reserved word
     */
    public static boolean isReservedWord2(String s) {
        if (s == null) {
            return false;
        }
        String[] reservedWords = reservedWordsAS3_2;
        s = s.trim();
        for (String rw : reservedWords) {
            if (rw.equals(s)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Generates random string.
     *
     * @param firstUppercase First character uppercase
     * @param rndSize Random size
     * @return Random string
     */
    public static String fooString(boolean firstUppercase, int rndSize) {
        int len = 3 + rnd.nextInt(rndSize - 3);
        StringBuilder sb = new StringBuilder(len);
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

        return sb.toString();
    }

    /**
     * Deobfuscates instance names.
     *
     * @param as3 Is ActionScript3
     * @param namesMap Names map
     * @param renameType Rename type
     * @param tags Tags
     * @param selected Preselected names
     */
    public void deobfuscateInstanceNames(boolean as3, HashMap<DottedChain, DottedChain> namesMap, RenameType renameType, Iterable<Tag> tags, Map<DottedChain, DottedChain> selected) {
        for (Tag t : tags) {
            if (t instanceof DefineSpriteTag) {
                deobfuscateInstanceNames(as3, namesMap, renameType, ((DefineSpriteTag) t).getTags(), selected);
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

    /**
     * Deobfuscates package names.
     *
     * @param as3 Is ActionScript3
     * @param pkg Package
     * @param namesMap Names map
     * @param renameType Rename type
     * @param selected Preselected names
     * @return Deobfuscated package
     */
    public DottedChain deobfuscatePackage(boolean as3, DottedChain pkg, HashMap<DottedChain, DottedChain> namesMap, RenameType renameType, Map<DottedChain, DottedChain> selected) {
        if (namesMap.containsKey(pkg)) {
            return namesMap.get(pkg);
        }
        List<String> ret = new ArrayList<>(pkg.size());
        boolean isChanged = false;
        for (int p = 0; p < pkg.size(); p++) {
            String part = pkg.get(p);
            String partChanged = deobfuscateName(as3, part, false, "package", namesMap, renameType, selected);
            if (partChanged != null) {
                ret.add(partChanged);
                isChanged = true;
            } else {
                ret.add(part);
            }
        }
        if (isChanged) {
            DottedChain chain = new DottedChain(ret);
            namesMap.put(pkg, chain);
            return chain;
        }
        return null;
    }

    /**
     * Deobfuscates name with package.
     *
     * @param as3 Is ActionScript3
     * @param n Name
     * @param namesMap Names map
     * @param renameType Rename type
     * @param selected Preselected names
     * @return Deobfuscated name
     */
    public String deobfuscateNameWithPackage(boolean as3, String n, HashMap<DottedChain, DottedChain> namesMap, RenameType renameType, Map<DottedChain, DottedChain> selected) {
        DottedChain nChain = DottedChain.parseNoSuffix(n);
        DottedChain pkg = nChain.getWithoutLast();
        String name = nChain.getLast();

        boolean changed = false;
        if ((pkg != null) && (!pkg.isEmpty()) && (!pkg.isTopLevel())) {
            DottedChain changedPkg = deobfuscatePackage(as3, pkg, namesMap, renameType, selected);
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
            String newClassName;
            if ((pkg == null) || (pkg.isEmpty()) || (pkg.isTopLevel())) {
                newClassName = name;
            } else {
                newClassName = pkg + "." + name;
            }
            return newClassName;
        }
        return null;
    }

    /**
     * Checks if string is valid slash path.
     *
     * @param s String
     * @param exceptions Exceptions
     * @return True if string is valid slash path
     */
    private static boolean isValidSlashPath(String s, String... exceptions) {
        String[] slashParts = s.split("\\/");
        if (s.isEmpty()) {
            return false;
        }
        for (int p = 0; p < slashParts.length; p++) {
            String part = slashParts[p];
            if (p == 0 && part.isEmpty()) {
                continue;
            }
            if (part.isEmpty() && p < slashParts.length - 1) { //  two slashesh xx//yy
                return false;
            }
            if ("..".equals(part)) {
                continue; //okay
            }
            if (!isValidName(false, part, exceptions)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if string is valid name with slash.
     *
     * @param s String
     * @param exceptions Exceptions
     * @return True if string is valid name with slash
     */
    public static boolean isValidNameWithSlash(String s, String... exceptions) {
        if (s.contains(":")) {
            String[] pathVar = s.split(":");
            if (!isValidSlashPath(pathVar[0], exceptions)) {
                return false;
            }
            for (int i = 1; i < pathVar.length; i++) {
                if (!isValidName(false, pathVar[i], exceptions)) {
                    return false;
                }
            }
            return true;
        } else {
            return isValidName(false, s, exceptions);
        }
    }

    /**
     * Checks if string is valid name with dot.
     *
     * @param as3 Is ActionScript3
     * @param s String
     * @param exceptions Exceptions
     * @return True if string is valid name with dot
     */
    public static boolean isValidNameWithDot(boolean as3, String s, String... exceptions) {
        for (String e : exceptions) {
            if (e.equals(s)) {
                return true;
            }
        }

        if (isReservedWord(s, as3)) {
            return false;
        }
        
        if (Configuration.autoDeobfuscateIdentifiers.get()
                && (s.contains(SAFE_STRING_PREFIX)
                    || s.contains(SAFE_PACKAGE_PREFIX)
                    || s.contains(SAFE_CLASS_PREFIX))) {
            return false;
        }

        // simple fast test
        if (VALID_NAME_PATTERN_DOT.matcher(s).matches()) {
            return true;
        }
        // unicode test
        if (IDENTIFIER_PATTERN_DOT.matcher(s).matches()) {
            return true;
        }
        return false;
    }

    /**
     * Checks if string is valid name.
     *
     * @param as3 Is ActionScript3
     * @param s String
     * @param exceptions Exceptions
     * @return True if string is valid name
     */
    public static boolean isValidName(boolean as3, String s, String... exceptions) {
        
        if (Configuration.autoDeobfuscateIdentifiers.get() 
                && (s.startsWith(SAFE_STRING_PREFIX)
                    || s.startsWith(SAFE_PACKAGE_PREFIX)
                    || s.startsWith(SAFE_CLASS_PREFIX))) {
            return false;
        }
        
        for (String e : exceptions) {
            if (e.equals(s)) {
                return true;
            }
        }

        if (isReservedWord(s, as3)) {
            return false;
        }

        // simple fast test
        if (VALID_NAME_PATTERN.matcher(s).matches()) {
            return true;
        }
        // unicode test
        if (IDENTIFIER_PATTERN.matcher(s).matches()) {
            return true;
        }
        return false;
    }

    /**
     * Deobfuscates name.
     *
     * @param as3 Is ActionScript3
     * @param s String
     * @param firstUppercase First character uppercase
     * @param usageType Usage type
     * @param namesMap Names map
     * @param renameType Rename type
     * @param selected Preselected names
     * @return Deobfuscated name
     */
    public String deobfuscateName(boolean as3, String s, boolean firstUppercase, String usageType, HashMap<DottedChain, DottedChain> namesMap, RenameType renameType, Map<DottedChain, DottedChain> selected) {
        if (usageType == null) {
            usageType = "name";
        }

        DottedChain sChain = DottedChain.parseNoSuffix(s);
        if (selected != null) {
            if (selected.containsKey(sChain)) {
                return selected.get(sChain).toRawString();
            }
        }

        boolean isValid = isValidName(as3, s);
        if (!isValid) {
            if (namesMap.containsKey(sChain)) {
                return namesMap.get(sChain).toRawString();
            } else {
                String ret = null;
                boolean found;
                int rndSize = DEFAULT_FOO_SIZE;
                do {
                    found = false;
                    if (renameType == RenameType.TYPENUMBER) {
                        ret = Helper.getNextId(usageType, typeCounts, true);
                        if (allVariableNamesStr.contains(ret)) {
                            found = true;
                        }
                    } else if (renameType == RenameType.RANDOMWORD) {
                        ret = fooString(firstUppercase, rndSize);
                        if (allVariableNamesStr.contains(ret)
                                || isReservedWord(ret, as3)
                                || namesMap.containsValue(DottedChain.parseNoSuffix(ret))) {
                            found = true;
                            rndSize++;
                        }
                    }
                } while (found);

                namesMap.put(DottedChain.parseNoSuffix(s), DottedChain.parseNoSuffix(ret));
                return ret;
            }
        }
        return null;
    }

    /**
     * Appends obfuscated identifier.
     *
     * @param swf SWF
     * @param used Used deobfuscations
     * @param s String
     * @param writer Writer
     * @return Writer
     */
    public static GraphTextWriter appendObfuscatedIdentifier(SWF swf, Set<String> used, String s, GraphTextWriter writer) {
        Map<String, String> map = new LinkedHashMap<>();
        if (Configuration.autoDeobfuscateIdentifiers.get() && swf != null) {
            map = swf.getObfuscatedIdentifiersMap();
            used.add(s);
            if (map.containsKey(s)) {
                writer.append(map.get(s));
            } else {
                String ret = IdentifiersDeobfuscation.SAFE_STRING_PREFIX + map.size();
                map.put(s, ret);
                writer.append(ret);
            }
            return writer;
        }

        writer.append("\u00A7");
        escapeOIdentifier(s, writer);
        return writer.append("\u00A7");
    }

    /**
     * Ensures identifier is valid and if not, uses paragraph syntax.
     *
     * @param swf SWF
     * @param used Set of used obfuscated identifiers in this script - the
     * method will add to it
     * @param as3 Is ActionScript3
     * @param s Identifier
     * @param validExceptions Exceptions which are valid (e.g. some reserved
     * words)
     * @return Printable identifier
     */
    public static String printIdentifier(SWF swf, Set<String> used, boolean as3, String s, String... validExceptions) {
        if (s == null || s.isEmpty()) {
            return "";
        }

        Map<String, String> map = new LinkedHashMap<>();
        if (Configuration.autoDeobfuscateIdentifiers.get()) {

            if (swf != null) {
                map = swf.getObfuscatedIdentifiersMap();
            }

            if (map.containsKey(s)) {
                used.add(s);
                return map.get(s);
            }
            
            if (s.startsWith(SAFE_STRING_PREFIX)
                    || s.startsWith(SAFE_PACKAGE_PREFIX)
                    || s.startsWith(SAFE_CLASS_PREFIX)) {
                String foundKey = null;
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    if (entry.getValue().equals(s)) {
                        foundKey = entry.getKey();
                        break;
                    }
                }
                if (foundKey == null) {
                    map.put(s, s);
                    return s;
                } else {
                    if (foundKey.equals(s)) {
                        return s;
                    }
                    map.put(foundKey, SAFE_STRING_PREFIX + map.size());                    
                    map.put(s, s);
                    return s;
                }
            }
        }

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

        if (isValidName(as3, s)) {
            nameCache.put(s, s);
            return s;
        }

        if (Configuration.autoDeobfuscateIdentifiers.get()) {
            String ret = IdentifiersDeobfuscation.SAFE_STRING_PREFIX + map.size();
            map.put(s, ret);
            used.add(s);
            return ret;
        }

        String ret = "\u00A7" + escapeOIdentifier(s) + "\u00A7";
        nameCache.put(s, ret);
        return ret;
    }

    /**
     * Escapes obfuscated identifier.
     *
     * @param s String
     * @param writer Writer
     * @return Writer
     */
    public static GraphTextWriter escapeOIdentifier(String s, GraphTextWriter writer) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\n') {
                writer.append("\\n");
            } else if (c == '\r') {
                writer.append("\\r");
            } else if (c == '\b') {
                writer.append("\\b");
            } else if (c == '\t') {
                writer.append("\\t");
            } else if (c == '\f') {
                writer.append("\\f");
            } else if (c == '\\') {
                writer.append("\\\\");
            } else if (c == '\u00A7') {
                writer.append("\\\u00A7");
            } else if (c < 32) {
                writer.append("\\x").append(Helper.byteToHex((byte) c));
            } else {
                int num = 1;
                for (int j = i + 1; j < s.length(); j++) {
                    if (s.charAt(j) == c) {
                        num++;
                    } else {
                        break;
                    }
                }
                if (num > Configuration.limitSameChars.get()) {
                    writer.append("\\{").append(num).append("}");
                    i += num - 1;
                }
                writer.append(c);
            }
        }

        return writer;
    }

    /**
     * Escapes obfuscated identifier.
     *
     * @param s String
     * @return Escaped string
     */
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
            } else if (c == '\f') {
                ret.append("\\f");
            } else if (c == '\\') {
                ret.append("\\\\");
            } else if (c == '\u00A7') {
                ret.append("\\\u00A7");
            } else if (c < 32) {
                ret.append("\\x").append(Helper.byteToHex((byte) c));
            } else {
                int num = 1;
                for (int j = i + 1; j < s.length(); j++) {
                    if (s.charAt(j) == c) {
                        num++;
                    } else {
                        break;
                    }
                }
                if (num > Configuration.limitSameChars.get()) {
                    ret.append("\\{").append(num).append("}");
                    i += num - 1;
                }
                ret.append(c);
            }
        }

        return ret.toString();
    }

    /**
     * Unescapes deobfuscated identifier
     *
     * @param swf SWF
     * @param s String
     * @return Unescaped string
     */
    public static String unescapeOIdentifier(SWF swf, String s) {
        StringBuilder ret = new StringBuilder(s.length());

        Map<String, String> map = swf.getObfuscatedIdentifiersMap();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (s.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        if (s.length() < 2) {
            return s;
        }
        if (!(s.startsWith("\u00A7") && s.endsWith("\u00A7"))) {
            return s;
        }
        for (int i = 1; i < s.length() - 1; i++) {
            char c = s.charAt(i);
            if (c == '\\') {
                if (i + 1 < s.length() - 1) {
                    i++;
                    c = s.charAt(i);
                    if (c == 'n') {
                        ret.append("\n");
                    } else if (c == 'r') {
                        ret.append("\r");
                    } else if (c == 't') {
                        ret.append("\t");
                    } else if (c == 'b') {
                        ret.append("\b");
                    } else if (c == 'f') {
                        ret.append("\f");
                    } else if (c == '\\') {
                        ret.append("\\");
                    } else if (c == '\u00A7') {
                        ret.append("\u00A7");
                    } else if (c == 'x' && i + 2 < s.length() - 1) {
                        ret.append((char) Integer.parseInt(s.substring(i + 1, i + 3), 16));
                        i += 2;
                    } else if (c == '{') {
                        int endPos = s.indexOf("}", i);
                        if (endPos != -1) {
                            int numRepeat = Integer.parseInt(s.substring(i + 1, endPos));
                            i = endPos + 1;
                            c = s.charAt(i);
                            for (int j = 0; j < numRepeat; j++) {
                                ret.append(c);
                            }
                        }
                    }
                }
            } else {
                ret.append(c);
            }
        }

        return ret.toString();
    }

    /**
     * Clears cache.
     */
    public static void clearCache() {
        as2NameCache.clear();
        as3NameCache.clear();
    }

    @SuppressWarnings("unchecked")
    public static GraphTextWriter writeCurrentScriptReplacements(GraphTextWriter writer, Set<String> usedDeobfuscations, SWF swf) {
        if (usedDeobfuscations.isEmpty()) {
            return writer;
        }
        if (!Configuration.autoDeobfuscateIdentifiers.get()) {
            return writer;
        }
        
        writer.newLine();
        List<String> commentLines = new ArrayList<>();
        Map<String, String> fullMap = swf.getObfuscatedIdentifiersMap();
        for (String obfuscated : usedDeobfuscations) {
            String deobfuscated = fullMap.get(obfuscated);
            if (obfuscated.equals(deobfuscated)) {
                continue;
            }
            commentLines.add("@identifier " + deobfuscated + " = \"" + Helper.escapePCodeString(obfuscated) + "\"");                
        }
        if (commentLines.isEmpty()) {
            return writer;
        }
        commentLines.sort(new NaturalOrderComparator());
        commentLines.add(0, AppResources.translate("decompilationWarning.obfuscatedIdentifiers"));
        commentLines.add(1, AppResources.translate("decompilationWarning.replacementsFollow"));
        String[] commentLinesArr = commentLines.toArray(new String[commentLines.size()]);
        new DocCommentItem(commentLinesArr).appendTo(writer, LocalData.empty);
        
        return writer;
    }

    public static Map<String, String> getReplacementsFromDoc(String s) throws Exception {
        ActionScriptDocParser asd = new ActionScriptDocParser();
        List<AsDocComment> comments = asd.parse(s);
        Map<String, String> replacements = new LinkedHashMap<>();
        for (AsDocComment comment : comments) {
            for (AsDocTag tag : comment.tags) {
                if ("identifier".equals(tag.tagName)) {
                    String tagText = tag.tagText;
                    if (tagText != null && !tagText.isEmpty()) {
                        ActionScriptLexer lexer = new ActionScriptLexer(tagText);
                        ParsedSymbol symb = lexer.yylex();
                        if (symb.type != SymbolType.IDENTIFIER) {
                            throw new Exception("Invalid @identifier AsDoc tag value. Identifier expected.");
                        }
                        ParsedSymbol symb2 = lexer.yylex();
                        if (symb2.type != SymbolType.ASSIGN) {
                            throw new Exception("Invalid @identifier AsDoc tag value. Assign expected.");
                        }
                        ParsedSymbol symb3 = lexer.yylex();
                        if (symb3.type != SymbolType.STRING) {
                            throw new Exception("Invalid @identifier AsDoc tag value. String expected.");
                        }
                        replacements.put(symb.value.toString(), symb3.value.toString());
                    }
                }
            }
        }
        return replacements;
    }
}
