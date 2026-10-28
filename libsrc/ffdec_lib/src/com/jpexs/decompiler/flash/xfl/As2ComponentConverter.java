/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 */
package com.jpexs.decompiler.flash.xfl;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.model.CallFunctionActionItem;
import com.jpexs.decompiler.flash.action.model.CallMethodActionItem;
import com.jpexs.decompiler.flash.action.model.DirectValueActionItem;
import com.jpexs.decompiler.flash.action.model.GetMemberActionItem;
import com.jpexs.decompiler.flash.action.model.GetVariableActionItem;
import com.jpexs.decompiler.flash.action.model.InitArrayActionItem;
import com.jpexs.decompiler.flash.action.model.NewObjectActionItem;
import com.jpexs.decompiler.flash.action.model.SetMemberActionItem;
import com.jpexs.decompiler.flash.action.model.SetPropertyActionItem;
import com.jpexs.decompiler.flash.action.model.SetVariableActionItem;
import com.jpexs.decompiler.flash.action.parser.script.ActionScript2Parser;
import com.jpexs.decompiler.flash.action.special.ActionEnd;
import com.jpexs.decompiler.flash.action.swf5.ActionConstantPool;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.tags.DefineScalingGridTag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.DoActionTag;
import com.jpexs.decompiler.flash.tags.DoInitActionTag;
import com.jpexs.decompiler.flash.tags.ExportAssetsTag;
import com.jpexs.decompiler.flash.tags.FileAttributesTag;
import com.jpexs.decompiler.flash.tags.JPEGTablesTag;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.tags.base.RemoveTag;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.types.CLIPACTIONRECORD;
import com.jpexs.decompiler.flash.types.CLIPACTIONS;
import com.jpexs.decompiler.flash.types.CLIPEVENTFLAGS;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.model.FalseItem;
import com.jpexs.decompiler.graph.model.TrueItem;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.xml.stream.XMLStreamException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Restores Flash 5 Smart Clips, Flash MX v1 and AS2 components. Compiled clips keep their bytecode;
 * MX components retain their editable timelines and class initialization scripts.
 * The bundled declarations describe the Inspector and SWC dependency graph;
 * all executable code and visual assets come from the input SWF.
 */
final class As2ComponentConverter {

    private static final String NS = "http://ns.adobe.com/xfl/2008/";

    private static final String RUNTIME = "component_runtime.swf";

    private final SWF swf;

    private final Map<CharacterTag, ComponentDefinition> components = new LinkedHashMap<>();

    private final Map<String, String> classProperties = new LinkedHashMap<>();

    private final Map<String, String> assetNames = new LinkedHashMap<>();

    private final Map<String, String> legacyAssetNames = new LinkedHashMap<>();

    private final Set<Integer> legacyAssets = new LinkedHashSet<>();

    private final Map<String, Element> declarations = new LinkedHashMap<>();

    private final Map<CLIPACTIONRECORD, Map<String, String>> initializers = new IdentityHashMap<>();

    private final Set<Integer> bundled = new LinkedHashSet<>();

    private final Map<CharacterTag, String> libraryNames = new IdentityHashMap<>();

    As2ComponentConverter(SWF swf, boolean legacyOnly, boolean smartClipsOnly) throws IOException, InterruptedException {
        this.swf = swf;
        try (InputStream input = getClass().getResourceAsStream("as2_swc_definitions.xml")) {
            if (input == null) {
                throw new IOException("Missing AS2 component declarations");
            }
            Document document = ComponentConverter.parse(Utf8Helper.decode(Helper.readStream(input)));
            for (Element element : ComponentConverter.elements(document.getDocumentElement(), "SwcItem")) {
                declarations.put(element.getAttribute("name"), element);
            }
        } catch (IOException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IOException("Cannot read AS2 component declarations", ex);
        }
        try (InputStream input = getClass().getResourceAsStream("as2_component_definitions.xml")) {
            Document metadata = ComponentConverter.parse(Utf8Helper.decode(Helper.readStream(input)));
            NodeList definitions = metadata.getElementsByTagName("component");
            for (int i = 0; i < definitions.getLength(); i++) {
                Element definition = (Element) definitions.item(i);
                NodeList properties = definition.getElementsByTagName("classProperties");
                if (properties.getLength() == 1) {
                    classProperties.put(definition.getAttribute("name"), properties.item(0).getTextContent());
                }
            }
            NodeList assets = metadata.getElementsByTagName("asset");
            for (int i = 0; i < assets.getLength(); i++) {
                Element asset = (Element) assets.item(i);
                assetNames.put(asset.getAttribute("exportName"), asset.getAttribute("path"));
            }
        } catch (Exception ex) {
            throw new IOException("Cannot read AS2 component asset names", ex);
        }
        try (InputStream input = getClass().getResourceAsStream("as1_component_definitions.xml")) {
            Document metadata = ComponentConverter.parse(Utf8Helper.decode(Helper.readStream(input)));
            NodeList assets = metadata.getElementsByTagName("asset");
            for (int i = 0; i < assets.getLength(); i++) {
                Element asset = (Element) assets.item(i);
                legacyAssetNames.put(asset.getAttribute("exportName"), asset.getAttribute("path"));
            }
        } catch (Exception ex) {
            throw new IOException("Cannot read Flash MX component asset names", ex);
        }
        List<ComponentDefinition> definitions = new ArrayList<>(ComponentDefinition.load(swf, "as2_component_definitions.xml").values());
        definitions.addAll(ComponentDefinition.load(swf, "as1_component_definitions.xml").values());
        definitions.addAll(ComponentDefinition.load(swf, "f5_smartclip_definitions.xml").values());
        SmartClipRuntime smartClips = new SmartClipRuntime(swf);
        for (ComponentDefinition definition : definitions) {
            if (smartClipsOnly && !definition.smartClip
                    || legacyOnly && !definition.legacy && !definition.smartClip) {
                continue;
            }
            if (definition.smartClip) {
                for (CharacterTag character : swf.getCharacters(false).values()) {
                    if (character instanceof DefineSpriteTag && definition.runtimeSignature.equals(
                            smartClips.signature((DefineSpriteTag) character))) {
                        components.put(character, definition);
                    }
                }
                continue;
            }
            CharacterTag character = swf.getCharacterByExportName(definition.exportName);
            // A matching instance name alone is not evidence of an AS2 component.
            if (!(character instanceof DefineSpriteTag)) {
                continue;
            }
            if (definition.compiled && (swf.getCharacterByExportName("__Packages." + definition.className) == null
                    || !hasRegistration(character, definition))) {
                continue;
            }
            if (definition.legacy && !hasRegistration(character, definition)) {
                continue;
            }
            if (!definition.compiled && ((DefineSpriteTag) character).getTags().size() < 2) {
                continue;
            }
            boolean dependenciesPresent = true;
            for (String dependency : definition.requiredClasses.split(",")) {
                if (!dependency.isEmpty() && swf.getCharacterByExportName(dependency) == null) {
                    dependenciesPresent = false;
                }
            }
            if (dependenciesPresent) {
                components.put(character, definition);
            }
        }
        if (components.isEmpty()) {
            return;
        }
        if (components.values().stream().anyMatch(definition -> definition.legacy)) {
            for (String name : legacyAssetNames.keySet()) {
                CharacterTag character = swf.getCharacterByExportName(name);
                if (character != null) {
                    legacyAssets.add(character.getCharacterId());
                    character.getNeededCharactersDeep(legacyAssets, new HashSet<>());
                }
            }
        }
        Set<String> dependencies = new LinkedHashSet<>();
        for (ComponentDefinition definition : components.values()) {
            if (definition.compiled) {
                collectDependencies(definition.name, dependencies);
            }
        }
        for (String name : dependencies) {
            CharacterTag character = swf.getCharacterByExportName(name);
            if (character != null) {
                bundled.add(character.getCharacterId());
                character.getNeededCharactersDeep(bundled, new HashSet<>());
            }
        }
        Set<Integer> externalInstances = new LinkedHashSet<>();
        collectExternalInstances(swf, externalInstances);
        Set<String> nestedComponents = new LinkedHashSet<>();
        for (ComponentDefinition definition : components.values()) {
            if (definition.compiled) {
                Set<String> nested = new LinkedHashSet<>();
                collectDependencies(definition.name, nested);
                nested.remove(definition.name);
                nestedComponents.addAll(nested);
            }
        }
        components.entrySet().removeIf(entry -> nestedComponents.contains(entry.getValue().name)
                && !externalInstances.contains(entry.getKey().getCharacterId()));
        // Parameters are emitted before user clip handlers. Only consume a whole
        // construct handler when every statement is a known literal parameter.
        if (!analyze(swf)) {
            components.clear();
            initializers.clear();
            bundled.clear();
            // Keep MX registrations and init order even when an instance's user
            // code requires ordinary-symbol export instead of an Inspector.
            return;
        }

    }

    private boolean hasRegistration(CharacterTag character, ComponentDefinition definition) throws InterruptedException {
        for (Tag tag : swf.getTags()) {
            if (!(tag instanceof DoInitActionTag) || ((DoInitActionTag) tag).spriteId != character.getCharacterId()) {
                continue;
            }
            List<GraphTargetItem> tree = Action.actionsToTree(new LinkedHashSet<>(), false, new HashMap<>(),
                    true, false, ((DoInitActionTag) tag).getActions(), swf.version, 0, "", swf.getCharset());
            for (GraphTargetItem item : tree) {
                if (!(item instanceof CallMethodActionItem)) {
                    continue;
                }
                CallMethodActionItem call = (CallMethodActionItem) item;
                if (!(call.scriptObject instanceof GetVariableActionItem)
                        || !"Object".equals(literal(((GetVariableActionItem) call.scriptObject).name))
                        || !"registerClass".equals(literal(call.methodName)) || call.arguments.size() != 2
                        || !definition.exportName.equals(literal(call.arguments.get(0)))) {
                    continue;
                }
                GraphTargetItem target = call.arguments.get(1);
                if (definition.legacy && target instanceof GetVariableActionItem
                        && definition.className.equals(literal(((GetVariableActionItem) target).name))) {
                    return true;
                }
                if (target instanceof CallFunctionActionItem) {
                    CallFunctionActionItem function = (CallFunctionActionItem) target;
                    if ("eval".equals(literal(function.functionName)) && function.arguments.size() == 1
                            && definition.className.equals(literal(function.arguments.get(0)))) {
                        return true;
                    }
                }
                List<String> members = new ArrayList<>();
                while (target instanceof GetMemberActionItem) {
                    GetMemberActionItem member = (GetMemberActionItem) target;
                    members.add(0, literal(member.memberName));
                    target = member.object;
                }
                if (target instanceof GetVariableActionItem) {
                    members.add(0, literal(((GetVariableActionItem) target).name));
                    if (definition.className.equals(String.join(".", members))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void collectExternalInstances(Timelined owner, Set<Integer> result) {
        for (Tag tag : owner.getTags()) {
            if (tag instanceof DefineSpriteTag && !bundled.contains(((DefineSpriteTag) tag).spriteId)) {
                collectExternalInstances((DefineSpriteTag) tag, result);
            }
            if (tag instanceof PlaceObjectTypeTag) {
                result.add(((PlaceObjectTypeTag) tag).getCharacterId());
            }
        }
    }

    private void collectDependencies(String name, Set<String> result) {
        if (!result.add(name)) {
            return;
        }
        Element declaration = declarations.get(name);
        if (declaration == null) {
            return;
        }
        for (String kind : new String[]{"IncludesItem", "ExportAfterItem"}) {
            for (Element dependency : ComponentConverter.elements(declaration, kind)) {
                collectDependencies(dependency.getAttribute("name"), result);
            }
        }
    }

    private boolean analyze(Timelined owner) throws InterruptedException {
        Map<Integer, Integer> placedCharacters = new HashMap<>();
        for (Tag tag : owner.getTags()) {
            ComponentConverter.checkInterrupted();
            if (tag instanceof DefineSpriteTag && !bundled.contains(((DefineSpriteTag) tag).spriteId)
                    && !analyze((DefineSpriteTag) tag)) {
                return false;
            }
            if (tag instanceof RemoveTag) {
                placedCharacters.remove(((RemoveTag) tag).getDepth());
            }
            if (!(tag instanceof PlaceObjectTypeTag)) {
                continue;
            }
            PlaceObjectTypeTag place = (PlaceObjectTypeTag) tag;
            if (place.getCharacterId() >= 0) {
                placedCharacters.put(place.getDepth(), place.getCharacterId());
            }
            int characterId = placedCharacters.getOrDefault(place.getDepth(), -1);
            ComponentDefinition definition = components.get(swf.getCharacter(characterId));
            CLIPACTIONS actions = place.getClipActions();
            if (actions == null || definition == null && !legacyAssets.contains(characterId)) {
                continue;
            }
            for (CLIPACTIONRECORD record : actions.clipActionRecords) {
                boolean smartClipLoad = definition != null && definition.smartClip && record.eventFlags.clipEventLoad;
                if (!record.eventFlags.clipEventConstruct && !record.eventFlags.clipEventInitialize && !smartClipLoad) {
                    continue;
                }
                // Mixed event handlers must be retained intact.
                CLIPEVENTFLAGS flags = record.eventFlags;
                if (flags.clipEventConstruct && flags.clipEventInitialize
                        || smartClipLoad && (flags.clipEventConstruct || flags.clipEventInitialize)
                        || flags.clipEventKeyUp || flags.clipEventKeyDown || flags.clipEventMouseUp
                        || flags.clipEventMouseDown || flags.clipEventMouseMove || flags.clipEventUnload
                        || flags.clipEventEnterFrame || flags.clipEventLoad && !smartClipLoad || flags.clipEventData
                        || flags.clipEventDragOver || flags.clipEventDragOut || flags.clipEventRollOver
                        || flags.clipEventRollOut || flags.clipEventPress || flags.clipEventRelease
                        || flags.clipEventReleaseOutside || flags.clipEventKeyPress) {
                    if (definition == null) {
                        continue;
                    }
                    return false;
                }
                if (definition == null) {
                    // MX compiles these empty handlers from private SmartClip
                    // parameters. They have no source representation in MX;
                    // emitting on(initialize) causes compiler errors on save.
                    if (flags.clipEventInitialize && record.getActions().stream()
                            .allMatch(action -> action instanceof ActionEnd || action instanceof ActionConstantPool)) {
                        initializers.put(record, Collections.emptyMap());
                    }
                    continue;
                }
                List<GraphTargetItem> tree = Action.actionsToTree(new LinkedHashSet<>(), false,
                        new HashMap<>(), false, false, record.getActions(), swf.version, 0, "", swf.getCharset());
                Map<String, String> values = new LinkedHashMap<>();
                Map<String, List<String>> arrays = new LinkedHashMap<>();
                for (GraphTargetItem item : tree) {
                    // Flash 5 emits _name through SetProperty rather than SetVariable.
                    if (definition.smartClip && item instanceof SetPropertyActionItem) {
                        SetPropertyActionItem property = (SetPropertyActionItem) item;
                        String value = literal(property.value);
                        if (!"".equals(literal(property.target)) || property.propertyIndex != 13
                                || value == null || values.put("_name", value) != null) {
                            return false;
                        }
                        continue;
                    }
                    if (item instanceof SetMemberActionItem) {
                        SetMemberActionItem member = (SetMemberActionItem) item;
                        String name = member.object instanceof GetVariableActionItem
                                ? literal(((GetVariableActionItem) member.object).name) : null;
                        List<String> array = arrays.get(name);
                        String value = literal(member.value);
                        if (array == null || !Integer.toString(array.size()).equals(literal(member.objectName))
                                || value == null || value.contains(",") || value.contains("\n") || value.contains("\r")) {
                            return false;
                        }
                        array.add(value);
                        values.put(name, String.join(",", array));
                        continue;
                    }
                    if (!(item instanceof SetVariableActionItem)) {
                        return false;
                    }
                    SetVariableActionItem assignment = (SetVariableActionItem) item;
                    String name = literal(assignment.name);
                    ComponentDefinition.Parameter parameter = definition.parameters.get(name);
                    String value = parameter == null ? null : parameterValue(assignment.value, parameter);
                    if (assignment.value instanceof InitArrayActionItem && "Array".equals(parameter == null ? "" : parameter.type)) {
                        List<String> array = new ArrayList<>();
                        for (GraphTargetItem child : ((InitArrayActionItem) assignment.value).values) {
                            array.add(literal(child));
                        }
                        arrays.put(name, array);
                    } else if (emptyArray(assignment.value) && "Array".equals(parameter == null ? "" : parameter.type)) {
                        arrays.put(name, new ArrayList<>());
                    }
                    if (value == null || values.put(name, value) != null) {
                        return false;
                    }
                }
                initializers.put(record, values);
            }
        }
        return true;
    }

    private static String literal(GraphTargetItem item) {
        if (!(item instanceof DirectValueActionItem
                || item instanceof TrueItem
                || item instanceof FalseItem)) {
            return null;
        }
        Object value = item.getResult();
        if (value instanceof String || value instanceof Boolean) {
            return value.toString();
        }
        if (value instanceof Number) {
            return EcmaScript.toString(value);
        }
        return null;
    }

    private static String parameterValue(GraphTargetItem item, ComponentDefinition.Parameter parameter) {
        if ("Array".equals(parameter.type) && emptyArray(item)) {
            return "";
        }
        if ("Array".equals(parameter.type) && item instanceof InitArrayActionItem) {
            List<String> values = new ArrayList<>();
            for (GraphTargetItem child : ((InitArrayActionItem) item).values) {
                String value = literal(child);
                // Keep complex/ambiguous arrays as ActionScript rather than
                // flattening values which the Inspector cannot round-trip.
                if (value == null || value.contains(",") || value.contains("\n") || value.contains("\r")) {
                    return null;
                }
                values.add(value);
            }
            return String.join(",", values);
        }
        return literal(item);
    }

    private static boolean emptyArray(GraphTargetItem item) {
        return item instanceof NewObjectActionItem
                && "Array".equalsIgnoreCase(literal(((NewObjectActionItem) item).objectName))
                && ((NewObjectActionItem) item).arguments.isEmpty();
    }

    boolean isComponent(CharacterTag character) {
        return components.containsKey(character);
    }

    boolean isBundledCharacter(int characterId) {
        return bundled.contains(characterId);
    }

    boolean isLegacyCharacter(int characterId) {
        return legacyAssets.contains(characterId);
    }

    boolean isConsumedClipAction(CLIPACTIONRECORD record) {
        return initializers.containsKey(record);
    }

    String parameters(CharacterTag character, CLIPACTIONS actions) throws XMLStreamException {
        ComponentDefinition definition = components.get(character);
        if (definition == null) {
            return "";
        }
        Map<String, String> values = new LinkedHashMap<>();
        if (actions != null) {
            for (CLIPACTIONRECORD record : actions.clipActionRecords) {
                values.putAll(initializers.getOrDefault(record, Collections.emptyMap()));
            }
        }
        XFLXmlWriter writer = new XFLXmlWriter();
        writer.writeStartElement("parametersAsXML");
        writer.writeCData(ComponentConverter.parameterXml(definition, values));
        writer.writeEndElement();
        return writer.toString();
    }

    Set<String> initializeLibraryNames(Set<CharacterTag> characters, Map<CharacterTag, String> names) throws InterruptedException {
        Set<String> used = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        // Flash reads these in order; children preceding their parents create
        // stray empty folders at the Library root when the document is saved.
        Set<String> folders = new TreeSet<>();
        for (Map.Entry<CharacterTag, ComponentDefinition> entry : components.entrySet()) {
            String stem = entry.getValue().name;
            String name = stem;
            if (used.contains(name) && entry.getValue().legacy) {
                stem += " v1";
                name = stem;
            }
            for (int suffix = 2; !used.add(name); suffix++) {
                name = stem + " " + suffix;
            }
            names.put(entry.getKey(), name);
        }
        // Compiled-clip dependencies are opaque, just as when inserted from the
        // native Components panel. MX components and video controls have editable skins.
        for (CharacterTag character : new ArrayList<>(characters)) {
            if (bundled.contains(character.getCharacterId()) && !components.containsKey(character)) {
                characters.remove(character);
            }
        }
        Set<Integer> editableAssets = new LinkedHashSet<>(legacyAssets);
        for (Map.Entry<CharacterTag, ComponentDefinition> entry : components.entrySet()) {
            if (!entry.getValue().compiled) {
                entry.getKey().getNeededCharactersDeep(editableAssets, new HashSet<>());
            }
        }
        for (CharacterTag character : characters) {
            ComponentConverter.checkInterrupted();
            if (!components.containsKey(character) && editableAssets.contains(character.getCharacterId())) {
                String export = swf.getExportName(character.getCharacterId());
                String stem = legacyAssets.contains(character.getCharacterId()) && legacyAssetNames.containsKey(export)
                        ? legacyAssetNames.get(export)
                        : assetNames.getOrDefault(export, "Component Assets/" + (export == null ? "Symbol " + character.getCharacterId() : export));
                String name = stem;
                for (int suffix = 2; !used.add(name); suffix++) {
                    name = stem + " " + suffix;
                }
                names.put(character, name);
                for (int slash = name.lastIndexOf('/'); slash > 0; slash = name.lastIndexOf('/', slash - 1)) {
                    folders.add(name.substring(0, slash));
                }
            }
        }
        libraryNames.putAll(names);
        return folders;
    }

    private Element swcItem(Document document, String name) {
        Element source = declarations.get(name);
        if (source == null) {
            return null;
        }
        Element item = (Element) document.importNode(source, true);
        item.setAttribute("version", "1164217986");
        for (Element oldMap : ComponentConverter.elements(item, "assetMap")) {
            oldMap.getParentNode().removeChild(oldMap);
        }
        Set<String> dependencies = new LinkedHashSet<>();
        collectDependencies(name, dependencies);
        dependencies.remove(name);
        Element assetMap = document.createElementNS(NS, "assetMap");
        for (String dependency : dependencies) {
            Element declaration = declarations.get(dependency);
            if (declaration == null || swf.getCharacterByExportName(dependency) == null) {
                continue;
            }
            Element entry = document.createElementNS(NS, "AssetMapItem");
            entry.setAttribute("key", dependency);
            Element child = (Element) document.importNode(declaration, true);
            child.setAttribute("version", "1164217986");
            for (Element oldMap : ComponentConverter.elements(child, "assetMap")) {
                oldMap.getParentNode().removeChild(oldMap);
            }
            entry.appendChild(child);
            assetMap.appendChild(entry);
        }
        item.insertBefore(assetMap, item.getFirstChild());
        return item;
    }

    private byte[] runtime(Set<Integer> characters) throws IOException, InterruptedException {
        ByteArrayOutputStream original = new ByteArrayOutputStream();
        swf.saveTo(original);
        SWF runtime = new SWF(new ByteArrayInputStream(original.toByteArray()), false);
        for (Tag tag : new ArrayList<>(runtime.getTags().toArrayList())) {
            ComponentConverter.checkInterrupted();
            boolean keep = tag instanceof CharacterTag && characters.contains(((CharacterTag) tag).getCharacterId())
                    || tag instanceof DoInitActionTag && characters.contains(((DoInitActionTag) tag).spriteId)
                    || tag instanceof DefineScalingGridTag && characters.contains(((DefineScalingGridTag) tag).characterId)
                    // DefineBits depends on shared JPEG tables outside the character graph.
                    || tag instanceof JPEGTablesTag
                    || tag instanceof FileAttributesTag;
            if (tag instanceof ExportAssetsTag) {
                ExportAssetsTag exports = (ExportAssetsTag) tag;
                for (int i = exports.tags.size() - 1; i >= 0; i--) {
                    if (!characters.contains(exports.tags.get(i))) {
                        exports.tags.remove(i);
                        exports.names.remove(i);
                    }
                }
                exports.setModified(true);
                keep = !exports.tags.isEmpty();
            }
            if (!keep) {
                runtime.removeTag(tag);
            }
        }
        runtime.addTag(new ShowFrameTag(runtime));
        runtime.frameCount = 1;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        runtime.saveTo(output);
        return output.toByteArray();
    }

    private byte[] preview(byte[] runtimeBytes, CharacterTag character, ComponentDefinition definition) throws Exception {
        SWF preview = new SWF(new ByteArrayInputStream(runtimeBytes), false);
        // MX v1 uses the case-insensitive AVM1 semantics of SWF 6.
        if (!definition.legacy) {
            preview.version = Math.max(8, preview.version);
        }
        for (Tag tag : new ArrayList<>(preview.getTags().toArrayList())) {
            if (tag instanceof ShowFrameTag) {
                preview.removeTag(tag);
            }
        }
        DoActionTag dimensions = new DoActionTag(preview);
        dimensions.setActions(new ActionScript2Parser(preview, dimensions)
                .actionsFromString("previewWidth = " + definition.width + "; previewHeight = " + definition.height
                        + "; previewLegacy = " + definition.legacy + "; previewKind = '" + definition.name
                        + "'; previewLinkageID = '" + definition.exportName + "';", preview.getCharset()));
        preview.addTag(dimensions);
        try (InputStream input = getClass().getResourceAsStream("component_preview_as2.bin")) {
            if (input == null) {
                throw new IOException("Missing AS2 component preview");
            }
            SWF stub = new SWF(input, false);
            for (Tag tag : stub.getTags()) {
                if (tag instanceof DoActionTag) {
                    preview.addTag(tag);
                }
            }
        }
        preview.addTag(new PlaceObject2Tag(preview, false, 1, character.getCharacterId(),
                new MATRIX(), null, -1, "previewComponent", -1, null));
        preview.addTag(new ShowFrameTag(preview));
        preview.displayRect = new RECT(0, definition.width * 20, 0, definition.height * 20);
        preview.frameCount = 1;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        preview.saveTo(output);
        return output.toByteArray();
    }

    String convert(String documentXml, Map<String, byte[]> files, Map<String, byte[]> datfiles) throws IOException, InterruptedException {
        if (components.isEmpty() && legacyAssets.isEmpty()) {
            return documentXml;
        }
        try {
            Document document = ComponentConverter.parse(documentXml);
            Element root = document.getDocumentElement();
            Element media = ComponentConverter.elements(root, "media").stream().findFirst().orElse(null);
            if (media == null) {
                media = document.createElementNS(NS, "media");
                Node symbols = ComponentConverter.elements(root, "symbols").stream().findFirst().orElse(null);
                root.insertBefore(media, symbols == null ? root.getFirstChild() : symbols);
            }
            byte[] runtimeBytes = bundled.isEmpty() ? null : runtime(bundled);
            byte[] legacyRuntime = legacyAssets.isEmpty() ? null : runtime(legacyAssets);
            Map<String, ComponentDefinition> symbols = new LinkedHashMap<>();
            for (Map.Entry<CharacterTag, ComponentDefinition> entry : components.entrySet()) {
                ComponentConverter.checkInterrupted();
                ComponentDefinition definition = entry.getValue();
                String name = libraryNames.get(entry.getKey());
                String file = name + ".xml";
                if (!files.containsKey(file)) {
                    continue;
                }
                symbols.put(name, definition);
                if (definition.compiled) {
                    Element clip = document.createElementNS(NS, "DOMCompiledClipItem");
                    clip.setAttribute("name", name);
                    clip.setAttribute("itemID", "67829673-" + String.format("%08x", entry.getKey().getCharacterId()));
                    clip.setAttribute("sourceLastImported", "1164217986");
                    clip.setAttribute("tooltip", definition.name);
                    clip.setAttribute("editFrameIndex", "1");
                    clip.setAttribute("requiredMinimumASVersion", "0");
                    clip.setAttribute("requiredMinimumPlayerVersion", "0");
                    clip.setAttribute("linkageExportForAS", "true");
                    clip.setAttribute("linkageIdentifier", definition.name);
                    clip.setAttribute("linkageClassName", definition.name);
                    clip.setAttribute("actionscriptClass", definition.className);
                    String previewFile = "component_as2_" + definition.name.toLowerCase(Locale.ROOT) + ".swf";
                    datfiles.put(previewFile, preview(runtimeBytes, entry.getKey(), definition));
                    clip.setAttribute("swfScmHRef", previewFile);
                    clip.setAttribute("livePreview11ScmHRef", previewFile);
                    clip.setAttribute("livePreview11ScmSourceFilename", previewFile);
                    clip.setAttribute("swfScmSourceFilename", previewFile);
                    clip.setAttribute("swcPath", definition.name + ".xml");
                    clip.setAttribute("rootSymbolLinkageID", definition.name);
                    clip.setAttribute("movieRectRight", Integer.toString(definition.width * 20));
                    clip.setAttribute("movieRectBottom", Integer.toString(definition.height * 20));
                    clip.setAttribute("playerVersion", definition.minimumPlayerVersion);
                    clip.setAttribute("actionscriptVersion", "2");
                    clip.setAttribute("parametersAreLocked", "true");
                    clip.setAttribute("persistLivePreview11", "true");
                    ComponentConverter.appendParameters(clip, definition, Collections.emptyMap());
                    if (classProperties.containsKey(definition.name)) {
                        Element properties = document.createElementNS(NS, "classProperties");
                        properties.appendChild(document.createCDATASection(classProperties.get(definition.name)));
                        clip.appendChild(properties);
                    }
                    ComponentConverter.appendIcon(clip, definition, datfiles);
                    Element declaration = swcItem(document, definition.name);
                    if (declaration != null) {
                        declaration.setAttribute("implementation", previewFile);
                        NodeList nodes = declaration.getElementsByTagName("*");
                        for (int i = 0; i < nodes.getLength(); i++) {
                            Element node = (Element) nodes.item(i);
                            if (node.hasAttribute("implementation")) {
                                node.setAttribute("implementation", previewFile);
                            }
                        }
                        clip.appendChild(declaration);
                    }
                    media.appendChild(clip);
                    files.remove(file);
                    for (Element include : ComponentConverter.elements(root, "Include")) {
                        if (file.equals(include.getAttribute("href"))) {
                            include.getParentNode().removeChild(include);
                        }
                    }
                } else {
                    Document item = ComponentConverter.parse(Utf8Helper.decode(files.get(file)));
                    Element component = item.getDocumentElement();
                    item.renameNode(component, NS, "DOMComponentItem");
                    component.setAttribute("requiredMinimumASVersion", "2");
                    component.setAttribute("requiredMinimumPlayerVersion", definition.minimumPlayerVersion);
                    component.setAttribute("parametersAreLocked", "true");
                    if (definition.smartClip) {
                        component.setAttribute("requiredMinimumASVersion", "0");
                        component.setAttribute("tooltip", definition.name);
                        component.setAttribute("descriptionType", "text");
                        component.setAttribute("symbolDescription", definition.description);
                    }
                    if (definition.legacy) {
                        component.setAttribute("requiredMinimumASVersion", "1");
                        component.setAttribute("tooltip", definition.name);
                        String previewFile = "component_v1_" + definition.name.toLowerCase(Locale.ROOT) + ".swf";
                        datfiles.put(previewFile, preview(legacyRuntime, entry.getKey(), definition));
                        component.setAttribute("livePreviewScmHRef", previewFile);
                        component.setAttribute("livePreviewScmSourceFilename", previewFile);
                        component.setAttribute("persistLivePreview11", "true");
                    }
                    ComponentConverter.appendParameters(component, definition, Collections.emptyMap());
                    ComponentConverter.appendIcon(component, definition, datfiles);
                    files.put(file, Utf8Helper.getBytes(ComponentConverter.xml(item)));
                }
            }
            for (Element include : ComponentConverter.elements(root, "Include")) {
                String href = include.getAttribute("href");
                if (href.endsWith(".xml") && symbols.containsKey(href.substring(0, href.length() - 4))) {
                    include.removeAttribute("itemIcon");
                }
            }
            convertInstances(document, symbols);
            Map<String, Integer> initPriorities = new HashMap<>();
            int priority = 1;
            for (Tag tag : swf.getTags()) {
                if (tag instanceof DoInitActionTag && legacyAssets.contains(((DoInitActionTag) tag).spriteId)) {
                    initPriorities.put(libraryNames.get(swf.getCharacter(((DoInitActionTag) tag).spriteId)), priority++);
                }
            }
            for (String path : new ArrayList<>(files.keySet())) {
                if (path.endsWith(".xml")) {
                    Document item = ComponentConverter.parse(Utf8Helper.decode(files.get(path)));
                    boolean changed = convertInstances(item, symbols);
                    Integer initPriority = initPriorities.get(item.getDocumentElement().getAttribute("name"));
                    if (initPriority != null) {
                        for (Element script : ComponentConverter.elements(item.getDocumentElement(), "script")) {
                            String source = script.getTextContent();
                            String ordered = source.replaceFirst("#initclip[ \\t]*\\r?\\n", "#initclip " + initPriority + "\r\n");
                            if (!source.equals(ordered)) {
                                script.setTextContent("");
                                script.appendChild(item.createCDATASection(ordered));
                                changed = true;
                            }
                        }
                    }
                    if (changed) {
                        files.put(path, Utf8Helper.getBytes(ComponentConverter.xml(item)));
                    }
                }
            }
            if (!bundled.isEmpty()) {
                datfiles.put(RUNTIME, runtimeBytes);
            }
            return ComponentConverter.xml(document);
        } catch (IOException | InterruptedException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IOException("Cannot restore standard AS2 components", ex);
        }
    }

    private boolean convertInstances(Document document, Map<String, ComponentDefinition> symbols) {
        boolean changed = false;
        int uniqueId = 2;
        for (Element instance : ComponentConverter.elements(document.getDocumentElement(), "DOMSymbolInstance")) {
            ComponentDefinition definition = symbols.get(instance.getAttribute("libraryItemName"));
            if (definition != null) {
                document.renameNode(instance, NS, definition.compiled ? "DOMCompiledClipInstance" : "DOMComponentInstance");
                instance.setAttribute("uniqueID", Integer.toString(uniqueId++));
                Element binding = document.createElementNS(NS, "dataBindingXML");
                binding.appendChild(document.createCDATASection("<component metaDataFetched='true' schemaUrl='' schemaOperation=''/>"));
                Node parameters = ComponentConverter.elements(instance, "parametersAsXML").stream().findFirst().orElse(null);
                instance.insertBefore(binding, parameters);
                if (parameters != null) {
                    // The generic XFL pretty-printer normalizes CDATA to text.
                    // Flash requires CDATA when reading AS2 instance parameters.
                    String text = parameters.getTextContent();
                    while (parameters.hasChildNodes()) {
                        parameters.removeChild(parameters.getFirstChild());
                    }
                    parameters.appendChild(document.createCDATASection(text));
                }
                changed = true;
            }
        }
        return changed;
    }
}
