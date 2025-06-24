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
package com.jpexs.decompiler.flash.action.as2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Index of ActionScript 2 built in classes.
 *
 * @author JPEXS
 */
public class ActionScript2Classes {

    /**
     * Map of class name to map of trait name to trait
     */
    private static final Map<String, Map<String, Trait>> classToTraits = new HashMap<>();
    /**
     * Map of class name to list of parent class names
     */
    private static final Map<String, List<String>> classInheritance = new HashMap<>();

    /**
     * Whether the classes are already initialized
     */
    private static boolean inited = false;

    /**
     * Constructor.
     */
    private ActionScript2Classes() {

    }

    /**
     * Initialize the classes
     */
    private static synchronized void initClasses() {
        if (inited) {
            return;
        }
        InputStream is = ActionScript2Classes.class.getResourceAsStream("/com/jpexs/decompiler/flash/action/as2/as2_classes.txt");
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line;
            String clsName = "";
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                String[] parts = line.split(" ");
                if (parts[0].equals("class")) {
                    clsName = parts[1];
                    classToTraits.put(clsName, new TreeMap<>());
                    continue;
                }
                boolean isStatic = false;
                int pos = 0;
                if (parts[pos].equals("static")) {
                    isStatic = true;
                    pos++;
                }
                if (parts[pos].equals("extends")) {
                    if (!classInheritance.containsKey(clsName)) {
                        classInheritance.put(clsName, new ArrayList<>());
                    }
                    classInheritance.get(clsName).add(parts[pos + 1]);
                    continue;
                }

                String traitType = parts[pos];
                String name = parts[pos + 1];
                String type = parts[pos + 2];
                Trait trait;
                switch (traitType) {
                    case "function":
                        trait = new Method(isStatic, name, type, clsName);
                        break;
                    case "var":
                        trait = new Variable(isStatic, name, type, clsName);
                        break;
                    default:
                        throw new RuntimeException("Unknown trait type: " + traitType);
                }
                classToTraits.get(clsName).put(name, trait);
            }
        } catch (UnsupportedEncodingException ex) {
            //ignored
        } catch (IOException ex) {
            Logger.getLogger(ActionScript2Classes.class.getName()).log(Level.SEVERE, null, ex);
        }
        inited = true;
    }

    /**
     * Check if trait exists in class.
     *
     * @param className Class name
     * @param name Trait name
     * @param withInheritance Whether to check also parent classes
     * @return True if trait exists
     */
    public static boolean traitExists(String className, String name, boolean withInheritance) {
        if (!classToTraits.containsKey(className)) {
            return false;
        }

        if (classToTraits.get(className).containsKey(name)) {
            return true;
        }

        if (withInheritance) {
            if (classInheritance.containsKey(className)) {
                for (String parentClassName : classInheritance.get(className)) {
                    if (classToTraits.containsKey(parentClassName)) {
                        if (classToTraits.get(parentClassName).containsKey(name)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Get class traits, null when class not exists (or is not built-in).
     *
     * @param className Class name
     * @param withInheritance Whether to include parent classes
     * @return Map of trait name to trait
     */
    public static Map<String, Trait> getClassTraits(String className, boolean withInheritance) {
        initClasses();

        Map<String, Trait> result = new LinkedHashMap<>();
        if (!classToTraits.containsKey(className)) {
            return null;
        }
        for (String name : classToTraits.get(className).keySet()) {
            result.put(name, classToTraits.get(className).get(name));
        }

        if (withInheritance) {
            if (classInheritance.containsKey(className)) {
                for (String parentClassName : classInheritance.get(className)) {
                    if (classToTraits.containsKey(parentClassName)) {
                        for (String name : classToTraits.get(parentClassName).keySet()) {
                            if (!result.containsKey(name)) {
                                result.put(name, classToTraits.get(parentClassName).get(name));
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Get map of class name to map of trait name to trait.
     *
     * @return Map of class name to map of trait name to trait
     */
    public static Map<String, Map<String, Trait>> getClassToTraits() {
        initClasses();
        return classToTraits;
    }

    /**
     * Get map of class name to list of parent class names.
     *
     * @return Map of class name to list of parent class names
     */
    public static Map<String, List<String>> getClassInheritance() {
        initClasses();
        return classInheritance;
    }

    /**
     * Sample test
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        Map<String, Trait> traits = getClassTraits("flash.filters.BevelFilter", true);
        if (traits != null) {
            for (String name : traits.keySet()) {
                Trait t = traits.get(name);
                System.out.println(t.toString() + " (" + t.getClassName() + ")");
            }
        }

        System.out.println("trait exists: " + traitExists("flash.filters.BevelFilter", "quality", false));
    }

}
