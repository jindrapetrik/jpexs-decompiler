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
package com.jpexs.decompiler.flash.abc.avm2;

import com.jpexs.decompiler.flash.IdentifiersDeobfuscation;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.RenameType;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.helpers.Helper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * AVM2 deobfuscation.
 *
 * @author JPEXS
 */
public class AVM2Deobfuscation {

    /**
     * Default size of random word.
     */
    private static final int DEFAULT_FOO_SIZE = 10;

    /**
     * Valid characters for first character of name.
     */
    public static final String VALID_FIRST_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_$";

    /**
     * Valid characters for next characters of name.
     */
    public static final String VALID_NEXT_CHARACTERS = VALID_FIRST_CHARACTERS + "0123456789";

    /**
     * Valid characters for namespace.
     */
    public static final String VALID_NS_CHARACTERS = ".:";

    /**
     * SWF file.
     */
    private final SWF swf;

    /**
     * AVM2 constant pool.
     */
    private final AVM2ConstantPool constants;

    /**
     * Usage types count.
     */
    private final Map<String, Integer> usageTypesCount = new HashMap<>();

    /**
     * Flash proxy namespace.
     */
    public static final DottedChain FLASH_PROXY = new DottedChain(new String[]{"flash", "utils", "flash_proxy"});

    /**
     * Built-in namespace.
     */
    public static final DottedChain BUILTIN = new DottedChain(new String[]{"-"});

    /**
     * Constructs AVM2 deobfuscation.
     *
     * @param swf SWF
     * @param constants AVM2 constant pool
     */
    public AVM2Deobfuscation(SWF swf, AVM2ConstantPool constants) {
        this.swf = swf;
        this.constants = constants;
    }

    /**
     * Checks if string is valid namespace part.
     *
     * @param s String
     * @return True if string is valid namespace part
     */
    public boolean isValidNSPart(String s) {
        boolean isValid = true;
        if (IdentifiersDeobfuscation.isReservedWord2(s)) {
            isValid = false;
        }

        if (Configuration.autoDeobfuscateIdentifiers.get() 
                && (s.contains(IdentifiersDeobfuscation.SAFE_STRING_PREFIX)
                || s.contains(IdentifiersDeobfuscation.SAFE_PACKAGE_PREFIX)
                || s.contains(IdentifiersDeobfuscation.SAFE_CLASS_PREFIX))) {
            return false;
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

    /**
     * Gets built-in namespace.
     *
     * @param ns Namespace
     * @return Built-in namespace
     */
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

    /**
     * Generates random string.
     *
     * @param deobfuscated Deobfuscated names
     * @param orig Original name
     * @param firstUppercase First uppercase
     * @param usageType Usage type
     * @param renameType Rename type
     * @param autoAdd Auto add
     * @return Random string
     */
    private String fooString(HashMap<DottedChain, DottedChain> deobfuscated, String orig, boolean firstUppercase, String usageType, RenameType renameType, boolean autoAdd) {
        if (usageType == null) {
            usageType = "name";
        }

        String ret = null;
        boolean found;
        int rndSize = DEFAULT_FOO_SIZE;

        do {
            found = false;
            if (renameType == RenameType.TYPENUMBER) {
                ret = Helper.getNextId(usageType, usageTypesCount, true);
            } else if (renameType == RenameType.RANDOMWORD) {
                ret = IdentifiersDeobfuscation.fooString(firstUppercase, rndSize);
            }
            if (swf.as3StringConstantExists(ret)
                    || IdentifiersDeobfuscation.isReservedWord2(ret)
                    || deobfuscated.containsValue(DottedChain.parseNoSuffix(ret))) {
                found = true;
                rndSize++;
            }
        } while (found);
        if (autoAdd) {
            deobfuscated.put(DottedChain.parseNoSuffix(orig), DottedChain.parseNoSuffix(ret));
        }
        return ret;
    }

    /**
     * Checks whether string at given index is valid package name
     *
     * @param strIndex String index
     * @return True if valid
     */
    public boolean isValidPackageName(int strIndex) {
        if (strIndex <= 0) {
            return true;
        }
        String s = constants.getString(strIndex);
        if (builtInNs(s) != null) {
            return true;
        }
        return isValidNSPart(s);
    }

    /**
     * Deobfuscates package name.
     *
     * @param stringUsageTypes String usage types
     * @param stringUsages String usages
     * @param namesMap Names map
     * @param strIndex String index
     * @param renameType Rename type
     * @return Deobfuscated package name
     */
    public int deobfuscatePackageName(Map<Integer, String> stringUsageTypes, Set<Integer> stringUsages, HashMap<DottedChain, DottedChain> namesMap, int strIndex, RenameType renameType) {
        if (!isValidPackageName(strIndex)) {
            String s = constants.getString(strIndex);
            DottedChain sChain = DottedChain.parseWithSuffix(s);
            DottedChain newName;
            if (namesMap.containsKey(sChain)) {
                newName = namesMap.get(sChain);
                return constants.getStringId(newName.toRawString(), true); //constants.setString(strIndex, newName.toRawString());
            }

            List<String> ret = new ArrayList<>();
            for (int p = 0; p < sChain.size(); p++) {
                String part = sChain.get(p);
                if (!isValidNSPart(part)) {
                    ret.add(fooString(namesMap, part, false, "package", renameType, true));
                } else {
                    ret.add(part);
                }
            }
            newName = new DottedChain(ret);
            namesMap.put(sChain, newName);

            if (stringUsages.contains(strIndex)) {
                strIndex = constants.addString(newName.toRawString());
            } else {
                constants.setString(strIndex, newName.toRawString());
            }
        }
        return strIndex;
    }

    /**
     * Checks whether string at given index is valid name
     *
     * @param strIndex String index
     * @return True when valid
     */
    public boolean isValidName(int strIndex) {
        if (strIndex <= 0) {
            return true;
        }
        String s = constants.getString(strIndex);

        if (Configuration.autoDeobfuscateIdentifiers.get() 
                && (s.startsWith(IdentifiersDeobfuscation.SAFE_STRING_PREFIX)
                || s.startsWith(IdentifiersDeobfuscation.SAFE_PACKAGE_PREFIX)
                || s.startsWith(IdentifiersDeobfuscation.SAFE_CLASS_PREFIX))) {
            return false;
        }

        boolean isValid = true;
        if (IdentifiersDeobfuscation.isReservedWord2(s)) {
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
        return isValid;
    }

    /**
     * Deobfuscates name.
     *
     * @param stringUsageTypes String usage types
     * @param stringUsages String usages
     * @param namespaceUsages Namespace usages
     * @param namesMap Names map
     * @param strIndex String index
     * @param firstUppercase First uppercase
     * @param renameType Rename type
     * @return Deobfuscated name string index
     */
    public int deobfuscateName(Map<Integer, String> stringUsageTypes, Set<Integer> stringUsages, Set<Integer> namespaceUsages, HashMap<DottedChain, DottedChain> namesMap, int strIndex, boolean firstUppercase, RenameType renameType) {
        if (!isValidName(strIndex)) {
            String s = constants.getString(strIndex);
            DottedChain newname;
            DottedChain sChain = DottedChain.parseNoSuffix(s);
            if (namesMap.containsKey(sChain)) {
                newname = namesMap.get(sChain);
                return constants.getStringId(newname, true);
            }

            String str = fooString(namesMap, constants.getString(strIndex), firstUppercase, stringUsageTypes.get(strIndex), renameType, false);
            newname = DottedChain.parseNoSuffix(str);

            if (stringUsages.contains(strIndex) || namespaceUsages.contains(strIndex)) { // this name is already referenced as String
                String usageType = stringUsageTypes.get(strIndex);
                strIndex = constants.addString(s); // add new index
                stringUsageTypes.put(strIndex, usageType);
            }
            constants.setString(strIndex, newname.toRawString());
            if (!namesMap.containsKey(sChain)) {
                namesMap.put(sChain, DottedChain.parseNoSuffix(constants.getString(strIndex)));
            }
        }
        return strIndex;
    }
}
