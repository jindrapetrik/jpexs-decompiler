/*
 *  Copyright (C) 2010-2014 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.action;

import com.jpexs.decompiler.flash.abc.RenameType;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
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
public class ActionDeobfuscation {

    private final Random rnd = new Random();
    private final int DEFAULT_FOO_SIZE = 10;
    public HashSet<String> allVariableNamesStr = new HashSet<>();
    private final HashMap<String, Integer> typeCounts = new HashMap<>();

    public static final String VALID_FIRST_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_";
    public static final String VALID_NEXT_CHARACTERS = VALID_FIRST_CHARACTERS + "0123456789";
    public static final String FOO_CHARACTERS = "bcdfghjklmnpqrstvwz";
    public static final String FOO_JOIN_CHARACTERS = "aeiouy";

    private String fooString(HashMap<String, String> deobfuscated, String orig, boolean firstUppercase, int rndSize) {
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
            if (Action.isReservedWord(ret)) {
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

    public void deobfuscateInstanceNames(HashMap<String, String> namesMap, RenameType renameType, List<Tag> tags, Map<String, String> selected) {
        for (Tag t : tags) {
            if (t instanceof DefineSpriteTag) {
                deobfuscateInstanceNames(namesMap, renameType, ((DefineSpriteTag) t).subTags, selected);
            }
            if (t instanceof PlaceObjectTypeTag) {
                PlaceObjectTypeTag po = (PlaceObjectTypeTag) t;
                String name = po.getInstanceName();
                if (name != null) {
                    String changedName = deobfuscateName(name, false, "instance", namesMap, renameType, selected);
                    if (changedName != null) {
                        po.setInstanceName(changedName);
                        ((Tag) po).setModified(true);
                    }
                }
                String className = po.getClassName();
                if (className != null) {
                    String changedClassName = deobfuscateNameWithPackage(className, namesMap, renameType, selected);
                    if (changedClassName != null) {
                        po.setClassName(changedClassName);
                        ((Tag) po).setModified(true);
                    }
                }
            }
        }
    }

    public String deobfuscatePackage(String pkg, HashMap<String, String> namesMap, RenameType renameType, Map<String, String> selected) {
        if (namesMap.containsKey(pkg)) {
            return namesMap.get(pkg);
        }
        String[] parts = null;
        if (pkg.contains(".")) {
            parts = pkg.split("\\.");
        } else {
            parts = new String[]{pkg};
        }
        String ret = "";
        boolean isChanged = false;
        for (int p = 0; p < parts.length; p++) {
            if (p > 0) {
                ret += ".";
            }
            String partChanged = deobfuscateName(parts[p], false, "package", namesMap, renameType, selected);
            if (partChanged != null) {
                ret += partChanged;
                isChanged = true;
            } else {
                ret += parts[p];
            }
        }
        if (isChanged) {
            namesMap.put(pkg, ret);
            return ret;
        }
        return null;
    }

    public String deobfuscateNameWithPackage(String n, HashMap<String, String> namesMap, RenameType renameType, Map<String, String> selected) {
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
            String changedPkg = deobfuscatePackage(pkg, namesMap, renameType, selected);
            if (changedPkg != null) {
                changed = true;
                pkg = changedPkg;
            }
        }
        String changedName = deobfuscateName(name, true, "class", namesMap, renameType, selected);
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

    public String deobfuscateName(String s, boolean firstUppercase, String usageType, HashMap<String, String> namesMap, RenameType renameType, Map<String, String> selected) {
        boolean isValid = true;
        if (usageType == null) {
            usageType = "name";
        }

        if (selected != null) {
            if (selected.containsKey(s)) {
                return selected.get(s);
            }
        }

        if (Action.isReservedWord(s)) {
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
                    ret = fooString(namesMap, s, firstUppercase, DEFAULT_FOO_SIZE);
                }
                namesMap.put(s, ret);
                return ret;
            }
        }
        return null;
    }

}
