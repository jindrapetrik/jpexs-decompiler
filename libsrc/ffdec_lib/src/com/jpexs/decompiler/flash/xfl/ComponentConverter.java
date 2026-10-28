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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.flash.xfl;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import com.jpexs.decompiler.flash.abc.avm2.model.CallPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.FullMultinameAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.GetLexAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.GetPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.IntegerValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NullAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NumberValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetLocalAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.StringAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ThisAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.UndefinedAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.clauses.DeclarationAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.clauses.TryAVM2Item;
import com.jpexs.decompiler.flash.abc.types.ConvertData;
import com.jpexs.decompiler.flash.abc.types.InstanceInfo;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitClass;
import com.jpexs.decompiler.flash.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.NulWriter;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.flash.tags.DefineScalingGridTag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.DoABC2Tag;
import com.jpexs.decompiler.flash.tags.FileAttributesTag;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.SetBackgroundColorTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.SymbolClassTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.timeline.DepthState;
import com.jpexs.decompiler.flash.timeline.Frame;
import com.jpexs.decompiler.flash.timeline.Timeline;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.types.CLIPACTIONRECORD;
import com.jpexs.decompiler.flash.types.CLIPACTIONS;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.ScopeStack;
import com.jpexs.decompiler.graph.model.FalseItem;
import com.jpexs.decompiler.graph.model.TrueItem;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLStreamException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Restores the authoring representation of standard AS1, AS2 and AS3 components.
 * Only complete, literal Flash-generated parameter initializers are consumed.
 * Unknown components and initializers remain ordinary symbols and source code.
 */
final class ComponentConverter {

    private static final String PREVIEW_CLASS = "fl.livepreview.LivePreviewParent";

    private static final String NS = "http://ns.adobe.com/xfl/2008/";

    private final SWF swf;

    private final As2ComponentConverter as2;

    private final Map<String, ComponentDefinition> definitions = new LinkedHashMap<>();

    private final Map<CharacterTag, ComponentDefinition> components = new LinkedHashMap<>();

    private final Set<String> blockedClasses = new HashSet<>();

    private final Map<MethodBody, ComponentDefinition> methodDefinitions = new HashMap<>();

    private final Map<String, Map<String, TreeMap<Integer, Initializer>>> initializers = new HashMap<>();

    private final Set<MethodBody> ignoredMethods = new HashSet<>();

    private static class Initializer {

        String instanceName;

        MethodBody body;

        ComponentDefinition definition;

        Map<String, String> values = new LinkedHashMap<>();
    }

    ComponentConverter(SWF swf) throws IOException, InterruptedException {
        this(swf, false);
    }

    ComponentConverter(SWF swf, boolean legacyOnly) throws IOException, InterruptedException {
        this.swf = swf;
        as2 = swf.isAS3() ? null : new As2ComponentConverter(swf, legacyOnly);
        if (as2 != null || legacyOnly) {
            return;
        }
        for (ComponentDefinition definition : ComponentDefinition.load(swf).values()) {
            CharacterTag character = swf.getCharacterByClass(definition.className);
            boolean dependenciesPresent = true;
            for (String dependency : definition.requiredClasses.split(",")) {
                if (!dependency.isEmpty() && swf.getCharacterByClass(dependency) == null) {
                    dependenciesPresent = false;
                }
            }
            if (character instanceof DefineSpriteTag && dependenciesPresent) {
                definitions.put(definition.className, definition);
                components.put(character, definition);
            }
        }
        if (components.isEmpty()) {
            return;
        }
        for (ScriptPack pack : swf.getAS3Packs()) {
            checkInterrupted();
            for (int traitIndex : pack.traitIndices) {
                Trait trait = pack.abc.script_info.get(pack.scriptIndex).traits.traits.get(traitIndex);
                if (trait instanceof TraitClass) {
                    analyze(pack, ((TraitClass) trait).class_info);
                }
            }
        }
        for (Map.Entry<MethodBody, ComponentDefinition> method : methodDefinitions.entrySet()) {
            if (!ignoredMethods.contains(method.getKey())) {
                blockedClasses.add(method.getValue().className);
            }
        }
        components.entrySet().removeIf(entry -> blockedClasses.contains(entry.getValue().className));
        ignoredMethods.removeIf(body -> blockedClasses.contains(methodDefinitions.get(body).className));
    }

    boolean isBundledAs2Character(int characterId) {
        return as2 != null && as2.isBundledCharacter(characterId);
    }

    boolean isLegacyAs2Character(int characterId) {
        return as2 != null && as2.isLegacyCharacter(characterId);
    }

    String as2Parameters(CharacterTag character, CLIPACTIONS actions) throws XMLStreamException {
        return as2 == null ? "" : as2.parameters(character, actions);
    }

    boolean isConsumedClipAction(CLIPACTIONRECORD record) {
        return as2 != null && as2.isConsumedClipAction(record);
    }

    static void checkInterrupted() throws InterruptedException {
        if (CancellableWorker.isInterrupted()) {
            throw new InterruptedException();
        }
    }

    Set<MethodBody> getIgnoredMethods() {
        return Collections.unmodifiableSet(ignoredMethods);
    }

    boolean isComponent(CharacterTag character) {
        return as2 == null ? components.containsKey(character) : as2.isComponent(character);
    }

    Set<String> initializeLibraryNames(Set<CharacterTag> characters, Map<CharacterTag, String> names) throws IOException, InterruptedException {
        if (as2 != null) {
            return as2.initializeLibraryNames(characters, names);
        }
        return components.isEmpty() ? Collections.emptySet()
                : new ComponentLibrary().initializeNames(swf, characters, components, definitions.keySet(), names);
    }

    static void appendIcon(Element root, ComponentDefinition definition, Map<String, byte[]> datfiles) throws IOException {
        if (!definition.iconId.isEmpty()) {
            root.setAttribute("customIconID", definition.iconId);
        }
        if (definition.icon == null) {
            return;
        }
        String stem = "component_" + definition.name.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9]", "_") + "_icon";
        String filename = stem + ".dat";
        for (int suffix = 2; datfiles.containsKey(filename); suffix++) {
            filename = stem + "_" + suffix + ".dat";
        }
        try (InputStream input = ComponentConverter.class.getResourceAsStream(definition.icon.resource)) {
            if (input == null) {
                throw new IOException("Missing component Library icon: " + definition.name);
            }
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int count;
            while ((count = input.read(buffer)) != -1) {
                output.write(buffer, 0, count);
            }
            datfiles.put(filename, output.toByteArray());
        }
        Document document = root.getOwnerDocument();
        Element wrapper = document.createElementNS(NS, "customIcon");
        Element icon = document.createElementNS(NS, "CustomIcon");
        for (Map.Entry<String, String> attribute : definition.icon.attributes.entrySet()) {
            icon.setAttribute(attribute.getKey(), attribute.getValue());
        }
        icon.setAttribute("href", filename);
        wrapper.appendChild(icon);
        root.appendChild(wrapper);
    }

    static String name(GraphTargetItem item) {
        if (item instanceof FullMultinameAVM2Item) {
            FullMultinameAVM2Item multiname = (FullMultinameAVM2Item) item;
            if (multiname.name instanceof StringAVM2Item) {
                return (String) multiname.name.getResult();
            }
            return multiname.resolvedMultinameName;
        }
        return null;
    }

    static String qualifiedName(ABC abc, int index) {
        return abc.constants.getMultiname(index).getNameWithNamespace(new LinkedHashSet<>(), abc, abc.constants, true).toRawString();
    }

    static String instanceName(GraphTargetItem item) {
        if (!(item instanceof GetPropertyAVM2Item)) {
            return null;
        }
        GetPropertyAVM2Item property = (GetPropertyAVM2Item) item;
        return property.object instanceof ThisAVM2Item ? name(property.propertyName) : null;
    }

    private List<GraphTargetItem> convertMethod(ScriptPack pack, int classIndex, int methodIndex) throws InterruptedException {
        MethodBody body = pack.abc.findBody(methodIndex);
        if (body == null) {
            return null;
        }
        List<MethodBody> callStack = new ArrayList<>();
        callStack.add(body);
        body.convert(swf.version, callStack, swf.getAbcIndex(), new ConvertData(), "component parameters",
                ScriptExportMode.AS, false, methodIndex, pack.scriptIndex, classIndex, pack.abc, null,
                new ScopeStack(), 0, new NulWriter(), new ArrayList<>(), new Traits(), true,
                new HashSet<>(), new ArrayList<>(), new LinkedHashSet<>());
        return body.convertException == null ? body.convertedItems : null;
    }

    private static boolean inspectorGuard(GraphTargetItem item, String instance, boolean value) {
        if (!(item instanceof TryAVM2Item)) {
            return false;
        }
        TryAVM2Item tryItem = (TryAVM2Item) item;
        if (tryItem.tryCommands.size() != 1 || (tryItem.finallyCommands != null && !tryItem.finallyCommands.isEmpty())
                || tryItem.catchCommands.size() != 1 || !tryItem.catchCommands.get(0).isEmpty()
                || !(tryItem.tryCommands.get(0) instanceof SetPropertyAVM2Item)) {
            return false;
        }
        SetPropertyAVM2Item set = (SetPropertyAVM2Item) tryItem.tryCommands.get(0);
        return instance.equals(instanceName(set.object)) && "componentInspectorSetting".equals(name(set.propertyName))
                && (value ? set.value instanceof TrueItem : set.value instanceof FalseItem);
    }

    private Initializer readInitializer(ScriptPack pack, int classIndex, TraitMethodGetterSetter trait,
            Map<String, ComponentDefinition> fields) throws InterruptedException {
        if (!nameOfTrait(pack.abc, trait).startsWith("__setProp_")
                || pack.abc.method_info.get(trait.method_info).param_types.length != 0) {
            return null;
        }
        List<GraphTargetItem> items = convertMethod(pack, classIndex, trait.method_info);
        if (items == null) {
            return null;
        }
        Initializer result = new Initializer();
        result.body = pack.abc.findBody(trait.method_info);
        for (GraphTargetItem item : items) {
            if (item instanceof SetPropertyAVM2Item) {
                ComponentDefinition definition = fields.get(instanceName(((SetPropertyAVM2Item) item).object));
                if (definition != null) {
                    result.definition = definition;
                    break;
                }
            }
        }
        if (result.definition == null) {
            return null;
        }
        int runtimeParameters = 0;
        for (ComponentDefinition.Parameter parameter : result.definition.parameters.values()) {
            if (!"Video Preview".equals(parameter.type) && !"Video Cue Points".equals(parameter.type)) {
                runtimeParameters++;
            }
        }
        int start = 0;
        while (start < items.size() && items.get(start) instanceof DeclarationAVM2Item) {
            GraphTargetItem assignment = ((DeclarationAVM2Item) items.get(start)).assignment;
            if (!(assignment instanceof SetLocalAVM2Item) || ((SetLocalAVM2Item) assignment).regIndex <= 0) {
                return null;
            }
            GraphTargetItem value = assignment.value.getNotCoerced();
            if (!(value instanceof NullAVM2Item) && !(value instanceof UndefinedAVM2Item)
                    && !(value instanceof IntegerValueAVM2Item && ((IntegerValueAVM2Item) value).intValue() == 0)) {
                return null;
            }
            start++;
        }
        if (start + 2 > items.size()) {
            return null;
        }
        for (int i = start + 1; i < items.size() - 1; i++) {
            ComponentCollectionConverter.Result collection = null;
            if (!(items.get(i) instanceof SetPropertyAVM2Item)) {
                collection = ComponentCollectionConverter.read(pack.abc, items, i);
                if (collection == null) {
                    return null;
                }
                i += 4;
            }
            SetPropertyAVM2Item set = (SetPropertyAVM2Item) items.get(i);
            String instance = instanceName(set.object);
            String parameter = name(set.propertyName);
            ComponentDefinition.Parameter schema = result.definition.parameters.get(parameter);
            if (fields.get(instance) != result.definition || schema == null
                    || (result.instanceName != null && !result.instanceName.equals(instance))
                    || result.values.containsKey(parameter)) {
                return null;
            }
            String value = collection == null ? scalarValue(schema, set.value)
                    : collection.serialize(schema);
            if (value == null) {
                return null;
            }
            result.instanceName = instance;
            result.values.put(parameter, value);
        }
        if (result.values.size() != runtimeParameters || result.instanceName == null
                || !inspectorGuard(items.get(start), result.instanceName, true)
                || !inspectorGuard(items.get(items.size() - 1), result.instanceName, false)) {
            return null;
        }
        return result;
    }

    private static String scalarValue(ComponentDefinition.Parameter parameter, GraphTargetItem item) {
        switch (parameter.type) {
            case "Boolean":
                return item instanceof TrueItem || item instanceof FalseItem ? String.valueOf(item.getResult()) : null;
            case "String":
            case "Video Content Path":
            case "Video Skin":
            case "List":
                if ("List".equals(parameter.type) && (item instanceof TrueItem || item instanceof FalseItem)) {
                    String booleanValue = String.valueOf(item.getResult());
                    return java.util.Arrays.asList(parameter.attributes.getOrDefault("enumeration", "").split(",")).contains(booleanValue) ? booleanValue : null;
                }
                if (!(item instanceof StringAVM2Item)) {
                    return null;
                }
                String value = (String) item.getResult();
                String enumeration = parameter.attributes.get("enumeration");
                return enumeration == null || java.util.Arrays.asList(enumeration.split(",")).contains(value) ? value : null;
            case "Number":
                return item instanceof NumberValueAVM2Item && item.getResult() instanceof Number ? String.valueOf(item.getResult()) : null;
            case "Color":
                if (item instanceof NumberValueAVM2Item && item.getResult() instanceof Number) {
                    long color = ((Number) item.getResult()).longValue();
                    return color >= 0 && color <= 0xffffff ? String.format(java.util.Locale.ROOT, "#%06X", color) : null;
                }
                return null;
            default:
                return null;
        }
    }

    private static String nameOfTrait(ABC abc, Trait trait) {
        return trait.getName(abc).getName(new LinkedHashSet<>(), abc, abc.constants, new ArrayList<>(), false, false);
    }

    private void analyze(ScriptPack pack, int classIndex) throws InterruptedException {
        ABC abc = pack.abc;
        InstanceInfo instance = abc.instance_info.get(classIndex);
        Map<String, ComponentDefinition> componentFields = new HashMap<>();
        for (Trait trait : instance.instance_traits.traits) {
            if (trait instanceof TraitSlotConst) {
                int type = ((TraitSlotConst) trait).type_index;
                ComponentDefinition definition = type == 0 ? null : definitions.get(qualifiedName(abc, type));
                if (definition != null) {
                    componentFields.put(nameOfTrait(abc, trait), definition);
                }
            }
        }
        if (componentFields.isEmpty()) {
            return;
        }
        String owner = qualifiedName(abc, instance.name_index);
        CharacterTag character = swf.getCharacterByClass(owner);
        Timelined timelineOwner = owner.equals(swf.getDocumentClass()) ? swf
                : character instanceof Timelined ? (Timelined) character : null;
        if (timelineOwner == null) {
            return;
        }
        Map<String, Initializer> methods = new HashMap<>();
        for (Trait trait : instance.instance_traits.traits) {
            if (trait instanceof TraitMethodGetterSetter) {
                Initializer initializer = readInitializer(pack, classIndex, (TraitMethodGetterSetter) trait, componentFields);
                if (initializer != null) {
                    methods.put(nameOfTrait(abc, trait), initializer);
                    methodDefinitions.put(initializer.body, initializer.definition);
                } else {
                    String method = nameOfTrait(abc, trait);
                    for (Map.Entry<String, ComponentDefinition> field : componentFields.entrySet()) {
                        if (method.startsWith("__setProp_" + field.getKey() + "_")) {
                            blockedClasses.add(field.getValue().className);
                        }
                    }
                }
            }
        }
        if (methods.isEmpty()) {
            return;
        }
        List<GraphTargetItem> constructor = convertMethod(pack, classIndex, instance.iinit_index);
        if (constructor == null) {
            return;
        }
        Map<Integer, List<GraphTargetItem>> callers = new LinkedHashMap<>();
        callers.put(0, new ArrayList<>(constructor));
        Set<Integer> supportedCallers = new HashSet<>();
        supportedCallers.add(instance.iinit_index);
        Map<String, Integer> frameMethods = new HashMap<>();
        for (GraphTargetItem item : constructor) {
            if (!(item instanceof CallPropertyAVM2Item)) {
                continue;
            }
            CallPropertyAVM2Item call = (CallPropertyAVM2Item) item;
            if (!"addFrameScript".equals(name(call.propertyName)) || call.arguments.size() % 2 != 0) {
                continue;
            }
            for (int i = 0; i < call.arguments.size(); i += 2) {
                if (!(call.arguments.get(i) instanceof IntegerValueAVM2Item)) {
                    continue;
                }
                GraphTargetItem target = call.arguments.get(i + 1);
                String method = target instanceof GetLexAVM2Item
                        ? ((GetLexAVM2Item) target).propertyName.getName(new LinkedHashSet<>(), abc, abc.constants, new ArrayList<>(), false, false)
                        : instanceName(target);
                int frame = ((IntegerValueAVM2Item) call.arguments.get(i)).intValue();
                if (method != null && frame >= 0) {
                    if (frameMethods.containsKey(method) && frameMethods.get(method) != frame) {
                        return;
                    }
                    frameMethods.put(method, frame);
                }
            }
        }
        // Constructor and frame 1 may both initialize parameters. Preserve order.
        for (Trait trait : instance.instance_traits.traits) {
            String method = nameOfTrait(abc, trait);
            if (trait instanceof TraitMethodGetterSetter && frameMethods.containsKey(method)) {
                List<GraphTargetItem> items = convertMethod(pack, classIndex, ((TraitMethodGetterSetter) trait).method_info);
                if (items != null) {
                    callers.computeIfAbsent(frameMethods.get(method), k -> new ArrayList<>()).addAll(items);
                    supportedCallers.add(((TraitMethodGetterSetter) trait).method_info);
                }
            }
        }
        // Do not consume a method referenced by arbitrary user code or used as
        // a function value. It cannot then be replaced by authoring parameters.
        Set<String> unsafeMethods = new HashSet<>();
        Map<String, Integer> referenceCounts = new HashMap<>();
        for (MethodBody body : abc.bodies) {
            for (AVM2Instruction instruction : body.getCode().code) {
                for (int i = 0; i < instruction.definition.operands.length; i++) {
                    if (instruction.definition.operands[i] != AVM2Code.DAT_MULTINAME_INDEX) {
                        continue;
                    }
                    String method = abc.constants.getMultiname(instruction.operands[i]).getName(
                            new LinkedHashSet<>(), abc, abc.constants, new ArrayList<>(), false, false);
                    if (methods.containsKey(method)) {
                        referenceCounts.merge(method, 1, Integer::sum);
                    }
                    if (methods.containsKey(method) && (!supportedCallers.contains(body.method_info)
                            || !(instruction.definition.instructionCode == AVM2Instructions.CallProperty
                            || instruction.definition.instructionCode == AVM2Instructions.CallPropVoid))) {
                        unsafeMethods.add(method);
                    }
                }
            }
        }
        Map<String, Integer> directCalls = new HashMap<>();
        for (List<GraphTargetItem> items : callers.values()) {
            for (GraphTargetItem item : items) {
                if (item instanceof CallPropertyAVM2Item) {
                    CallPropertyAVM2Item call = (CallPropertyAVM2Item) item;
                    if (call.receiver instanceof ThisAVM2Item && call.arguments.isEmpty()) {
                        directCalls.merge(name(call.propertyName), 1, Integer::sum);
                    }
                }
            }
        }
        for (String method : methods.keySet()) {
            if (!referenceCounts.getOrDefault(method, 0).equals(directCalls.getOrDefault(method, 0))) {
                unsafeMethods.add(method);
            }
        }
        for (String method : unsafeMethods) {
            blockedClasses.add(methods.get(method).definition.className);
            methods.remove(method);
        }
        Timeline timeline = new Timeline(swf, timelineOwner, character == null ? 0 : character.getCharacterId(),
                swf.displayRect, swf.getFilterDimensions());
        Map<String, TreeMap<Integer, Initializer>> byInstance = new HashMap<>();
        Set<MethodBody> restoredMethods = new HashSet<>();
        for (Map.Entry<Integer, List<GraphTargetItem>> entry : callers.entrySet()) {
            for (GraphTargetItem item : entry.getValue()) {
                if (!(item instanceof CallPropertyAVM2Item)) {
                    continue;
                }
                CallPropertyAVM2Item call = (CallPropertyAVM2Item) item;
                Initializer initializer = methods.get(name(call.propertyName));
                if (initializer == null || !(call.receiver instanceof ThisAVM2Item) || !call.arguments.isEmpty()) {
                    continue;
                }
                boolean found = false;
                for (Frame frame : timeline.getFrames().subList(Math.min(entry.getKey(), timeline.getFrames().size()), timeline.getFrames().size())) {
                    for (DepthState state : frame.layers.values()) {
                        if (state.characterId == swf.getCharacterByClass(initializer.definition.className).getCharacterId()
                                && initializer.instanceName.equals(state.instanceName)) {
                            found = true;
                        }
                    }
                }
                if (found) {
                    TreeMap<Integer, Initializer> changes = byInstance.computeIfAbsent(initializer.instanceName, k -> new TreeMap<>());
                    if (changes.containsKey(entry.getKey())) {
                        return;
                    }
                    changes.put(entry.getKey(), initializer);
                    restoredMethods.add(initializer.body);
                }
            }
        }
        if (!byInstance.isEmpty()) {
            initializers.put(owner, byInstance);
            ignoredMethods.addAll(restoredMethods);
        }
    }

    static Document parse(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        return factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
    }

    static String xml(Document document) throws Exception {
        XFLXmlWriter writer = new XFLXmlWriter();
        writer.setMakeNewLines(false);
        writeNode(document.getDocumentElement(), writer);
        return writer.toString();
    }

    private static void writeNode(Node node, XFLXmlWriter writer) throws XMLStreamException {
        if (node instanceof Element) {
            writer.writeStartElement(node.getNodeName());
            NamedNodeMap attributes = node.getAttributes();
            Set<String> written = new HashSet<>();
            // Flash CS6 requires identifying attributes before dependent ones,
            // notably libraryItemName before component-instance properties.
            String[] attributeOrder = "DOMCompiledClipItem".equals(node.getNodeName())
                    ? new String[]{"name", "itemID", "linkageExportForAS", "linkageIdentifier", "linkageClassName",
                        "sourceLastImported", "tooltip", "customIconID", "actionscriptClass", "swfScmHRef",
                        "swfScmSourceFilename", "persistLivePreview11", "editFrameIndex", "requiredMinimumPlayerVersion",
                        "requiredMinimumASVersion", "parametersAreLocked", "swcPath", "rootSymbolLinkageID",
                        "movieRectRight", "movieRectBottom", "playerVersion", "actionscriptVersion"}
                    : "SwcItem".equals(node.getNodeName())
                    ? new String[]{"name", "implementation", "version", "isTopLevel", "intrinsicClass", "intrinsicScript"}
                    : new String[]{"xmlns:xsi", "xmlns", "libraryItemName", "name", "itemID", "xflVersion", "linkageClassName", "linkageExportForAS"};
            for (String name : attributeOrder) {
                Node attribute = attributes.getNamedItem(name);
                if (attribute != null) {
                    writer.writeAttribute(name, attribute.getNodeValue());
                    written.add(name);
                }
            }
            for (int i = 0; i < attributes.getLength(); i++) {
                Node attribute = attributes.item(i);
                if (!written.contains(attribute.getNodeName())) {
                    writer.writeAttribute(attribute.getNodeName(), attribute.getNodeValue());
                }
            }
            for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
                writeNode(child, writer);
            }
            writer.writeEndElement();
        } else if (node.getNodeType() == Node.CDATA_SECTION_NODE) {
            writer.writeCData(node.getNodeValue());
        } else if (node.getNodeType() == Node.TEXT_NODE) {
            if (node.getNodeValue().trim().isEmpty()) {
                writer.writeCharactersRaw(node.getNodeValue());
            } else {
                writer.writeCharacters(node.getNodeValue());
            }
        }
    }

    static String parameterXml(ComponentDefinition definition, Map<String, String> values) throws XMLStreamException {
        XFLXmlWriter writer = new XFLXmlWriter();
        for (ComponentDefinition.Parameter parameter : definition.parameters.values()) {
            writer.writeStartElement("property", new String[]{"id", parameter.id});
            writer.writeStartElement("Inspectable");
            for (Map.Entry<String, String> attribute : parameter.attributes.entrySet()) {
                writer.writeAttribute(attribute.getKey(), "defaultValue".equals(attribute.getKey())
                        ? values.getOrDefault(parameter.id, parameter.defaultValue) : attribute.getValue());
            }
            writer.writeEndElement();
            writer.writeEndElement();
        }
        return "   " + writer.toString() + "\n";
    }

    static void appendParameters(Element element, ComponentDefinition definition, Map<String, String> values) throws XMLStreamException {
        Document document = element.getOwnerDocument();
        Element parameters = document.createElementNS(NS, "parametersAsXML");
        String text = parameterXml(definition, values);
        // A parameter can contain a CDATA terminator (for example a label).
        String[] parts = text.split("]]>", -1);
        for (int i = 0; i < parts.length; i++) {
            parameters.appendChild(document.createCDATASection((i > 0 ? ">" : "") + parts[i] + (i + 1 < parts.length ? "]]" : "")));
        }
        element.appendChild(parameters);
    }

    static List<Element> elements(Element root, String name) {
        List<Element> result = new ArrayList<>();
        NodeList nodes = root.getElementsByTagNameNS(NS, name);
        for (int i = 0; i < nodes.getLength(); i++) {
            result.add((Element) nodes.item(i));
        }
        return result;
    }

    private void convertInstances(Document document, String owner, String symbolName, ComponentDefinition definition) throws XMLStreamException {
        Map<String, TreeMap<Integer, Initializer>> byInstance = initializers.getOrDefault(owner, Collections.emptyMap());
        Map<Node, Integer> sceneOffsets = new HashMap<>();
        int sceneOffset = 0;
        for (Element timeline : elements(document.getDocumentElement(), "DOMTimeline")) {
            sceneOffsets.put(timeline, sceneOffset);
            int length = 0;
            for (Element frame : elements(timeline, "DOMFrame")) {
                length = Math.max(length, Integer.parseInt(frame.getAttribute("index"))
                        + (frame.hasAttribute("duration") ? Integer.parseInt(frame.getAttribute("duration")) : 1));
            }
            sceneOffset += length;
        }
        for (Element frame : elements(document.getDocumentElement(), "DOMFrame")) {
            int offset = frameOffset(frame, sceneOffsets);
            int index = offset + Integer.parseInt(frame.getAttribute("index"));
            int duration = frame.hasAttribute("duration") ? Integer.parseInt(frame.getAttribute("duration")) : 1;
            // Parameter-only changes may have been merged into an otherwise held frame.
            Set<Integer> splitPoints = new java.util.TreeSet<>();
            for (Element instance : elements(frame, "DOMSymbolInstance")) {
                if (symbolName.equals(instance.getAttribute("libraryItemName"))) {
                    TreeMap<Integer, Initializer> changes = byInstance.get(instance.getAttribute("name"));
                    if (changes != null) {
                        splitPoints.addAll(changes.subMap(index, false, index + duration, false).keySet());
                    }
                }
            }
            Element current = frame;
            int currentIndex = index;
            for (int split : splitPoints) {
                Element next = (Element) frame.cloneNode(true);
                next.setAttribute("index", Integer.toString(split - offset));
                next.setAttribute("duration", Integer.toString(index + duration - split));
                current.setAttribute("duration", Integer.toString(split - currentIndex));
                current.getParentNode().insertBefore(next, current.getNextSibling());
                current = next;
                currentIndex = split;
            }
        }
        for (Element frame : elements(document.getDocumentElement(), "DOMFrame")) {
            int index = frameOffset(frame, sceneOffsets) + Integer.parseInt(frame.getAttribute("index"));
            for (Element instance : elements(frame, "DOMSymbolInstance")) {
                if (!symbolName.equals(instance.getAttribute("libraryItemName"))) {
                    continue;
                }
                TreeMap<Integer, Initializer> changes = byInstance.get(instance.getAttribute("name"));
                Map.Entry<Integer, Initializer> change = changes == null ? null : changes.floorEntry(index);
                document.renameNode(instance, NS, "DOMComponentInstance");
                appendParameters(instance, definition, change == null ? Collections.emptyMap() : change.getValue().values);
            }
        }
        int uniqueId = 1;
        for (Element instance : elements(document.getDocumentElement(), "DOMComponentInstance")) {
            instance.setAttribute("uniqueID", Integer.toString(uniqueId++));
        }
    }

    private static int frameOffset(Element frame, Map<Node, Integer> offsets) {
        for (Node parent = frame.getParentNode(); parent != null; parent = parent.getParentNode()) {
            if (offsets.containsKey(parent)) {
                return offsets.get(parent);
            }
        }
        return 0;
    }

    String convert(String documentXml, Map<String, byte[]> files, Map<String, byte[]> datfiles) throws IOException, InterruptedException {
        if (as2 != null) {
            return as2.convert(documentXml, files, datfiles);
        }
        if (components.isEmpty()) {
            return documentXml;
        }
        checkInterrupted();
        try {
            Map<String, Document> documents = new LinkedHashMap<>();
            for (Map.Entry<String, byte[]> file : files.entrySet()) {
                if (file.getKey().endsWith(".xml")) {
                    documents.put(file.getKey(), parse(Utf8Helper.decode(file.getValue())));
                }
            }
            Map<String, ComponentDefinition> symbols = new LinkedHashMap<>();
            Set<String> changedFiles = new HashSet<>();
            for (Map.Entry<String, Document> file : documents.entrySet()) {
                Document component = file.getValue();
                Element root = component.getDocumentElement();
                ComponentDefinition definition = definitions.get(root.getAttribute("linkageClassName"));
                if (definition == null || !components.containsKey(swf.getCharacterByClass(definition.className))) {
                    continue;
                }
                String stem = "component_" + definition.name.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9]", "_") + "_preview";
                String previewFile = stem + ".swf";
                for (int i = 1; datfiles.containsKey(previewFile); i++) {
                    previewFile = stem + "_" + i + ".swf";
                }
                datfiles.put(previewFile, createPreview(swf.getCharacterByClass(definition.className), definition));
                symbols.put(root.getAttribute("name"), definition);
                component.renameNode(root, NS, "DOMComponentItem");
                root.setAttribute("actionscriptClass", definition.className);
                root.setAttribute("tooltip", definition.name);
                root.setAttribute("parametersAreLocked", "true");
                root.setAttribute("requiredMinimumASVersion", "3");
                root.setAttribute("requiredMinimumPlayerVersion", definition.minimumPlayerVersion);
                root.setAttribute("livePreviewScmHRef", previewFile);
                root.setAttribute("livePreviewScmSourceFilename", definition.name + ".swf");
                root.setAttribute("persistLivePreview11", "true");
                root.setAttribute("livePreview11ScmHRef", previewFile);
                root.setAttribute("livePreview11ScmSourceFilename", definition.name + ".swf");
                appendParameters(root, definition, Collections.emptyMap());
                appendIcon(root, definition, datfiles);
                changedFiles.add(file.getKey());
            }
            Document document = parse(documentXml);
            for (Element include : elements(document.getDocumentElement(), "Include")) {
                Document item = documents.get(include.getAttribute("href"));
                if (item != null && "DOMComponentItem".equals(item.getDocumentElement().getLocalName())) {
                    include.removeAttribute("itemIcon");
                }
            }
            for (Map.Entry<String, ComponentDefinition> symbol : symbols.entrySet()) {
                convertInstances(document, swf.getDocumentClass(), symbol.getKey(), symbol.getValue());
                for (Map.Entry<String, Document> file : documents.entrySet()) {
                    checkInterrupted();
                    Document libraryItem = file.getValue();
                    if (elements(libraryItem.getDocumentElement(), "DOMSymbolInstance").stream()
                            .anyMatch(instance -> symbol.getKey().equals(instance.getAttribute("libraryItemName")))) {
                        convertInstances(libraryItem, libraryItem.getDocumentElement().getAttribute("linkageClassName"), symbol.getKey(), symbol.getValue());
                        changedFiles.add(file.getKey());
                    }
                }
            }
            for (String file : changedFiles) {
                files.put(file, Utf8Helper.getBytes(xml(documents.get(file))));
            }
            return xml(document);
        } catch (InterruptedException | IOException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IOException("Cannot restore standard AS3 components", ex);
        }
    }

    private byte[] createPreview(CharacterTag character, ComponentDefinition definition) throws IOException, InterruptedException {
        ByteArrayOutputStream original = new ByteArrayOutputStream();
        swf.saveTo(original);
        SWF preview = new SWF(new ByteArrayInputStream(original.toByteArray()), false);
        SymbolClassTag symbols = new SymbolClassTag(preview);
        for (Tag tag : new ArrayList<>(preview.getTags().toArrayList())) {
            checkInterrupted();
            if (tag instanceof DoABC2Tag) {
                ((DoABC2Tag) tag).flags |= 1; // Only initialize scripts needed by the preview.
                tag.setModified(true);
            }
            if (tag instanceof SymbolClassTag) {
                SymbolClassTag originalSymbols = (SymbolClassTag) tag;
                for (int i = 0; i < originalSymbols.tags.size(); i++) {
                    if (originalSymbols.tags.get(i) != 0) {
                        symbols.tags.add(originalSymbols.tags.get(i));
                        symbols.names.add(originalSymbols.names.get(i));
                    }
                }
                preview.removeTag(tag);
            } else if (!(tag instanceof CharacterTag || tag instanceof ABCContainerTag
                    || tag instanceof DefineScalingGridTag || tag instanceof FileAttributesTag || tag instanceof SetBackgroundColorTag)) {
                preview.removeTag(tag);
            }
        }
        DefineSpriteTag wrapper = new DefineSpriteTag(preview);
        wrapper.frameCount = 1;
        wrapper.addTag(new PlaceObject2Tag(preview, false, 1, character.getCharacterId(), new MATRIX(), null, -1, null, -1, null));
        wrapper.addTag(new ShowFrameTag(preview));
        preview.addTag(wrapper);
        try (InputStream input = ComponentConverter.class.getResourceAsStream("component_preview.bin")) {
            if (input == null) {
                throw new IOException("Missing component live preview resource");
            }
            SWF stub = new SWF(input, false);
            for (Tag tag : stub.getTags()) {
                if (tag instanceof ABCContainerTag) {
                    preview.addTag(tag);
                }
            }
        }
        symbols.tags.add(wrapper.getCharacterId());
        symbols.names.add(PREVIEW_CLASS);
        preview.addTag(symbols);
        preview.frameCount = 1;
        preview.version = Math.max(9, Integer.parseInt(definition.minimumPlayerVersion));
        preview.displayRect = new RECT(0, definition.width * 20, 0, definition.height * 20);
        preview.addTag(new PlaceObject2Tag(preview, false, 1, wrapper.spriteId, new MATRIX(), null, -1, null, -1, null));
        preview.addTag(new ShowFrameTag(preview));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        preview.saveTo(output);
        return output.toByteArray();
    }
}
