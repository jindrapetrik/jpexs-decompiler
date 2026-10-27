/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.flash.xfl;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.timeline.Timelined;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/** Restores standard CS6 Library paths before symbol references are written. */
final class ComponentLibrary {

    private static final String ASSETS = "Component Assets";

    private final Map<String, String> linkedNames = new HashMap<>();

    private final Map<String, String> generatedNames = new HashMap<>();

    ComponentLibrary() throws IOException {
        try (InputStream input = ComponentLibrary.class.getResourceAsStream("component_assets.xml")) {
            if (input == null) {
                throw new IOException("Missing standard component asset names");
            }
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            Element root = factory.newDocumentBuilder().parse(input).getDocumentElement();
            for (Node node = root.getFirstChild(); node != null; node = node.getNextSibling()) {
                if (node instanceof Element && "asset".equals(node.getNodeName())) {
                    Element asset = (Element) node;
                    if (asset.hasAttribute("className")) {
                        linkedNames.put(asset.getAttribute("className"), asset.getAttribute("name"));
                    } else if (asset.hasAttribute("generatedClass")) {
                        generatedNames.put(asset.getAttribute("generatedClass"), asset.getAttribute("name"));
                    }
                }
            }
        } catch (IOException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IOException("Cannot read standard component asset names", ex);
        }
    }

    private String assetName(CharacterTag character) {
        for (String className : character.getClassNames()) {
            String name = linkedNames.get(className);
            if (name != null) {
                return name;
            }
            int dot = className.lastIndexOf('.');
            if (dot >= 0 && className.substring(0, dot).endsWith("_fla")) {
                String generated = className.substring(dot + 1).replaceFirst("_[0-9]+$", "");
                name = generatedNames.get(generated);
                if (name != null) {
                    return name;
                }
            }
        }
        return null;
    }

    private static String uniqueName(String desired, Set<String> used) {
        String name = desired;
        for (int suffix = 2; !used.add(name); suffix++) {
            name = desired + " " + suffix;
        }
        return name;
    }

    private static String folder(String path) {
        int slash = path.lastIndexOf('/');
        return slash < 0 ? null : path.substring(0, slash);
    }

    Set<String> initializeNames(SWF swf, Set<CharacterTag> characters,
            Map<CharacterTag, ComponentDefinition> components, Set<String> componentClasses,
            Map<CharacterTag, String> names) throws InterruptedException {
        Map<CharacterTag, String> roots = new LinkedHashMap<>();
        Set<String> used = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        used.addAll(names.values());
        for (Map.Entry<CharacterTag, ComponentDefinition> entry : components.entrySet()) {
            if (characters.contains(entry.getKey())) {
                roots.put(entry.getKey(), uniqueName(entry.getValue().name, used));
            }
        }
        List<CharacterTag> ordered = new ArrayList<>(characters);
        ordered.sort(Comparator.comparingInt(CharacterTag::getCharacterId));
        for (CharacterTag character : ordered) {
            if (roots.containsKey(character) || character.isImported()) {
                continue;
            }
            String path = assetName(character);
            if (path != null) {
                roots.put(character, uniqueName(path, used));
            }
        }
        Map<CharacterTag, Set<String>> owners = new LinkedHashMap<>();
        for (Map.Entry<CharacterTag, String> root : roots.entrySet()) {
            ComponentConverter.checkInterrupted();
            String owner = folder(root.getValue());
            if (owner == null) {
                owner = ASSETS + "/" + root.getValue() + "Skins";
            }
            Set<Integer> visited = new HashSet<>();
            ArrayDeque<CharacterTag> queue = new ArrayDeque<>();
            queue.add(root.getKey());
            while (!queue.isEmpty()) {
                ComponentConverter.checkInterrupted();
                CharacterTag character = queue.remove();
                if (!visited.add(character.getCharacterId())) {
                    continue;
                }
                Set<Integer> needed = new LinkedHashSet<>();
                Set<String> neededClasses = new LinkedHashSet<>();
                character.getNeededCharacters(needed, neededClasses, swf);
                for (String neededClass : neededClasses) {
                    CharacterTag dependency = swf.getCharacterByClass(neededClass);
                    if (dependency != null) {
                        needed.add(dependency.getCharacterId());
                    }
                }
                for (int id : needed) {
                    CharacterTag child = swf.getCharacter(id);
                    if (child == null || child.isImported() || !characters.contains(child) || roots.containsKey(child)) {
                        continue;
                    }
                    if (child.getClassNames().stream().anyMatch(componentClasses::contains)) {
                        continue;
                    }
                    // Font faces and nonvisual media have separate naming rules.
                    if (child instanceof FontTag || !(child instanceof Timelined || child instanceof ImageTag
                            || child instanceof com.jpexs.decompiler.flash.tags.base.ShapeTag
                            || child instanceof com.jpexs.decompiler.flash.tags.base.MorphShapeTag)) {
                        continue;
                    }
                    owners.computeIfAbsent(child, k -> new LinkedHashSet<>()).add(owner);
                    queue.add(child);
                }
            }
        }
        names.putAll(roots);
        for (Map.Entry<CharacterTag, Set<String>> entry : owners.entrySet()) {
            CharacterTag character = entry.getKey();
            String owner = entry.getValue().size() == 1 ? entry.getValue().iterator().next() : ASSETS + "/Shared";
            String basename = (character instanceof ImageTag ? "Bitmap " : "Symbol ") + character.getCharacterId();
            names.put(character, uniqueName(owner + "/" + basename, used));
        }
        Set<String> folders = new TreeSet<>();
        for (String name : names.values()) {
            for (String parent = folder(name); parent != null; parent = folder(parent)) {
                folders.add(parent);
            }
        }
        return folders;
    }
}
