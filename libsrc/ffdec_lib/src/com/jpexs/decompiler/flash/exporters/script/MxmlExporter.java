/*
 *  Copyright (C) 2026 JPEXS, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 */
package com.jpexs.decompiler.flash.exporters.script;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.abc.avm2.model.ApplyTypeAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.CallPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.CoerceAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ConstructAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ConstructPropAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ConstructSuperAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ConvertAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.FindPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.FullMultinameAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.GetLexAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.GetPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.GetSlotAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.InitPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.InitVectorAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.LocalRegAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NameValuePair;
import com.jpexs.decompiler.flash.abc.avm2.model.NewArrayAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NewFunctionAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NewObjectAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NullAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ReturnValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ReturnVoidAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetLocalAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetSlotAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.StoreNewActivationAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.StringAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ThisAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.UndefinedAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.XMLAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.clauses.DeclarationAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.parser.AVM2ParseException;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.ActionScriptLexer;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.ParsedSymbol;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.SymbolType;
import com.jpexs.decompiler.flash.abc.types.AssignedValue;
import com.jpexs.decompiler.flash.abc.types.ConvertData;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitClass;
import com.jpexs.decompiler.flash.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.graph.AbstractGraphTargetVisitor;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.IfItem;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.decompiler.graph.model.NotItem;
import com.jpexs.decompiler.graph.model.TernarOpItem;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Recovery of classic Flex factories and compact MXML component descriptors.
 * Works on the AVM2 graph; the SWF is never executed. Original formatting and
 * compiler-generated names cannot in general be recovered.
 *
 * @author JPEXS
 */
public class MxmlExporter {

    /**
     * Checks the document/bootstrap and Flex ancestry without decompiling methods.
     * Full reconstruction still validates individual compiler constructions.
     * @param swf Source movie
     * @return Whether the movie has a supported Flex document root
     */
    public static boolean canExportSwf(SWF swf) {
        return getRootType(swf) != null;
    }

    /**
     * Finds the supported SDK application or module base of the main document.
     * @param swf Source movie
     * @return SDK root class, or null for unsupported movies
     */
    public static String getRootType(SWF swf) {
        return new MxmlExportContext().detectRootType(swf);
    }

    private static final Properties COMPONENT_TAGS = componentTags();

    private static Properties componentTags() {
        Properties result = new Properties();
        try (InputStream stream = MxmlExporter.class.getResourceAsStream("mxml-components.properties")) {
            if (stream != null) {
                result.load(stream);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot read MXML component aliases", ex);
        }
        return result;
    }

    private SWF swf;
    private ScriptPack pack;
    private ABC abc;
    private TraitClass classTrait;
    private int classIndex;
    private final Map<String, TraitMethodGetterSetter> methods = new LinkedHashMap<>();
    private final Set<String> factories = new HashSet<>();
    private final Set<String> activeFactories = new HashSet<>();
    private final Set<String> ids = new HashSet<>();
    private final Map<String, String> eventMethods = new LinkedHashMap<>();
    private final Map<String, Node> nodesById = new LinkedHashMap<>();
    private final Map<String, Node> componentNodes = new LinkedHashMap<>();
    private final List<Node> declarations = new ArrayList<>();
    private final List<Node> bindings = new ArrayList<>();
    private final List<GraphTargetItem> optimizedBindingData = new ArrayList<>();
    private final Map<String, String> xmlBindingNamespaces = new LinkedHashMap<>();
    private final List<Node> legacyStates = new ArrayList<>();
    private final Map<String, String> cssNamespaces = new LinkedHashMap<>();
    private final Map<String, String> scriptDataInitializers = new LinkedHashMap<>();
    private final Map<Node, Map.Entry<Node, String>> stateBindingTargets = new LinkedHashMap<>();
    private final Set<String> dependencies = new LinkedHashSet<>();
    private final List<DottedChain> fullyQualifiedNames = new ArrayList<>();
    private final Map<String, Integer> classReferences = new LinkedHashMap<>();
    private final Set<String> exportedClasses;
    private final Set<String> activeClasses;
    private final Map<File, String> pendingDocuments;
    private final MxmlExportContext context;
    private final String requestedClass;
    private final boolean inlineComponent;
    private String documentXml;
    private String css;
    private String metadata;
    private String hostComponent;
    private Node documentRoot;
    private ConvertData convertData;

    /**
     * Constructs an MXML exporter.
     */
    public MxmlExporter() {
        this(null, false, new MxmlExportContext());
    }

    private MxmlExporter(String requestedClass, boolean inlineComponent, MxmlExportContext context) {
        this.requestedClass = requestedClass;
        this.inlineComponent = inlineComponent;
        this.context = context;
        this.exportedClasses = context.exportedClasses;
        this.activeClasses = context.activeClasses;
        this.pendingDocuments = context.pendingDocuments;
    }

    private static final class Node {

        String type;
        String explicitTag;
        String text;
        String generatorClass;
        String generatorExpression;
        String inlineXml;
        String literalExpression;
        boolean scriptData;
        Map<String, String> attributes = new LinkedHashMap<>();
        Map<String, List<Node>> properties = new LinkedHashMap<>();
        List<Node> children = new ArrayList<>();

        Node(String type) {
            this.type = type;
        }
    }

    /**
     * Exports the Flex application into a package-relative MXML file.
     * Unsupported component construction fails before writing output.
     *
     * @param source Source SWF
     * @param directory Destination source directory
     * @return Exported file
     * @throws IOException Unsupported input or I/O failure
     * @throws InterruptedException Decompilation interrupted
     */
    public File exportMxml(SWF source, File directory) throws IOException, InterruptedException {
        swf = source;
        methods.clear();
        factories.clear();
        activeFactories.clear();
        ids.clear();
        eventMethods.clear();
        nodesById.clear();
        componentNodes.clear();
        declarations.clear();
        bindings.clear();
        optimizedBindingData.clear();
        xmlBindingNamespaces.clear();
        legacyStates.clear();
        cssNamespaces.clear();
        scriptDataInitializers.clear();
        stateBindingTargets.clear();
        dependencies.clear();
        fullyQualifiedNames.clear();
        classReferences.clear();
        css = "";
        metadata = "";
        hostComponent = null;
        documentXml = null;
        if (inlineComponent) {
            // fx:Component regenerates its typed outerDocument property.
            ids.add("outerDocument");
        }
        if (requestedClass == null) {
            context.reset(source);
        }
        String main = requestedClass != null ? requestedClass : context.mainClass;
        pack = context.packs.get(main);
        if (pack == null || !(pack.getPublicTrait() instanceof TraitClass)) {
            throw unsupported("No Flex application class found");
        }
        if (!activeClasses.add(main)) {
            throw unsupported("Recursive component document " + main);
        }
        context.addDocument(main, inlineComponent);
        try {
            return exportDocument(main, directory);
        } finally {
            activeClasses.remove(main);
        }
    }

    private File exportDocument(String main, File directory) throws IOException, InterruptedException {
        abc = pack.abc;
        classTrait = (TraitClass) pack.getPublicTrait();
        classIndex = classTrait.class_info;
        String base = abc.constants.getMultiname(abc.instance_info.get(classIndex).super_index)
                .getNameWithNamespace(new LinkedHashSet<>(), abc, abc.constants, true).toRawString();
        if (requestedClass == null && !context.isSupportedRoot(pack)) {
            throw unsupported("Application base class " + base);
        }
        Set<String> seenTypes = new HashSet<>();
        for (String type : context.dependencies(pack)) {
            String simple = type.substring(type.lastIndexOf('.') + 1);
            if (!seenTypes.add(simple)) {
                fullyQualifiedNames.add(DottedChain.parseWithSuffix(simple));
            }
        }
        for (Trait trait : abc.instance_info.get(classIndex).instance_traits.traits) {
            if (trait instanceof TraitMethodGetterSetter && trait.kindType == Trait.TRAIT_METHOD) {
                methods.put(traitName(trait), (TraitMethodGetterSetter) trait);
            }
        }
        // Use the normal decompiler for imports, user code and converted graphs.
        HighlightedTextWriter writer = new HighlightedTextWriter(Configuration.getCodeFormatting(), false);
        convertData = context.convertData(pack, directory);
        pack.toSource(swf.getAbcIndex(), writer, abc.script_info.get(pack.scriptIndex).traits.traits,
                convertData, ScriptExportMode.AS, false, false, false);
        String actionScript = writer.toString();
        MxmlResourceExporter.collect(pack, context.resourceBundles);
        readMetadata();
        Node root = new Node(base);
        documentRoot = root;
        List<String> interfaces = new ArrayList<>();
        for (int index : abc.instance_info.get(classIndex).interfaces) {
            String type = abc.constants.getMultiname(index).getNameWithNamespace(new LinkedHashSet<>(), abc, abc.constants, true).toRawString();
            if (!"mx.binding.IBindingClient".equals(type) && !"mx.core.IStateClient2".equals(type)) {
                interfaces.add(type);
            }
        }
        if (!interfaces.isEmpty()) {
            root.attributes.put("implements", String.join(",", interfaces));
        }
        readConstructor(root);
        readOptimizedGetter(root, "MXMLProperties", true);
        readOptimizedGetter(root, "MXMLDescriptor", false);
        readRootStyles(root);
        css = readStyles();
        hoistLiteralData(root);
        for (Node declaration : declarations) {
            hoistLiteralData(declaration);
        }
        readBindings(root);
        flattenLegacyStates(root);
        boolean superConstructor = false;
        for (GraphTargetItem item : body(abc.instance_info.get(classIndex).iinit_index)) {
            if (item instanceof ConstructSuperAVM2Item && ((ConstructSuperAVM2Item) item).args.isEmpty()) {
                superConstructor = true;
                break;
            }
        }
        if (!superConstructor) {
            throw unsupported("No classic MXML constructor found");
        }
        String script = script(actionScript, root);
        inlineComponents(root, script, directory);
        for (int i = 0; i < declarations.size(); i++) {
            declarations.set(i, inlineComponents(declarations.get(i), script, directory));
        }
        Map<String, String> namespaces = new LinkedHashMap<>();
        namespaces.put("http://ns.adobe.com/mxml/2009", "fx");
        namespaces.put("library://ns.adobe.com/flex/spark", "s");
        namespaces.put("library://ns.adobe.com/flex/mx", "mx");
        collectNamespaces(root, namespaces);
        for (Node declaration : declarations) {
            collectNamespaces(declaration, namespaces);
        }
        StringBuilder xml = new StringBuilder();
        writeNode(xml, root, namespaces, 0, script);
        documentXml = xml.toString();
        File file = pack.getExportFile(directory.getAbsolutePath(), ".mxml");
        if (!inlineComponent) {
            pendingDocuments.put(file, "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + documentXml);
            exportedClasses.add(main);
        }
        dependencies.addAll(context.dependencies(pack));
        exportDependencies(dependencies, directory);
        // Reconstruct every referenced component before writing any output.
        if (requestedClass == null) {
            int bundleCount = -1;
            while (bundleCount != context.resourceBundles.size()) {
                bundleCount = context.resourceBundles.size();
                exportDependencies(new MxmlResourceExporter().stage(context, swf, directory), directory);
            }
            context.stageAssets(directory);
            for (Map.Entry<File, byte[]> asset : context.pendingAssets.entrySet()) {
                if (asset.getKey().exists() && !Configuration.overwriteExistingFiles.get()) {
                    continue;
                }
                Files.createDirectories(asset.getKey().toPath().getParent());
                Files.write(asset.getKey().toPath(), asset.getValue());
            }
            for (Map.Entry<File, String> document : pendingDocuments.entrySet()) {
                if (document.getKey().exists() && !Configuration.overwriteExistingFiles.get()) {
                    continue;
                }
                Files.createDirectories(document.getKey().toPath().getParent());
                Files.write(document.getKey().toPath(), document.getValue().getBytes(StandardCharsets.UTF_8));
            }
        }
        return file;
    }

    private void exportDependencies(Set<String> names, File directory) throws IOException, InterruptedException {
        for (String name : names) {
            if (!context.shouldExport(name)) {
                continue;
            }
            ScriptPack dependency = context.packs.get(name);
            if (context.isMxml(dependency)) {
                new MxmlExporter(name, false, context).exportMxml(swf, directory);
            } else {
                // Mark first so mutually referring AS3 classes terminate normally.
                exportedClasses.add(name);
                HighlightedTextWriter writer = new HighlightedTextWriter(Configuration.getCodeFormatting(), false);
                dependency.toSource(swf.getAbcIndex(), writer, dependency.abc.script_info.get(dependency.scriptIndex).traits.traits,
                        context.convertData(dependency, directory), ScriptExportMode.AS, false, false, false);
                MxmlResourceExporter.collect(dependency, context.resourceBundles);
                pendingDocuments.put(dependency.getExportFile(directory.getAbsolutePath(), ".as"), writer.toString());
                exportDependencies(context.dependencies(dependency), directory);
            }
        }
    }

    private Node inlineComponents(Node node, String script, File directory) throws IOException, InterruptedException {
        String generator = node.generatorClass;
        if ("mx.core.ClassFactory".equals(node.type) && generator != null
                && generator.matches(Pattern.quote(pack.getClassPath().toRawString()) + "InnerClass\\d+")
                && classReferences.get(generator) == 1
                && node.attributes.size() == (node.attributes.containsKey("id") ? 2 : 1)
                && node.generatorExpression.equals(node.attributes.get("generator")) && node.properties.size() == 1
                && node.properties.containsKey("properties") && node.children.isEmpty()) {
            List<Node> properties = node.properties.get("properties");
            String simple = generator.substring(generator.lastIndexOf('.') + 1);
            if (properties.size() == 1 && "Object".equals(properties.get(0).type)
                    && properties.get(0).attributes.size() == 1 && "{this}".equals(properties.get(0).attributes.get("outerDocument"))
                    && properties.get(0).properties.isEmpty() && properties.get(0).children.isEmpty()
                    && !Pattern.compile("\\b" + Pattern.quote(simple) + "\\b").matcher(script).find()) {
                MxmlExporter component = new MxmlExporter(generator, true, context);
                component.exportMxml(swf, directory);
                dependencies.remove(generator);
                Node inline = new Node("Component");
                if (node.attributes.containsKey("id")) {
                    inline.attributes.put("id", node.attributes.get("id"));
                }
                inline.inlineXml = component.documentXml;
                return inline;
            }
        }
        for (int i = 0; i < node.children.size(); i++) {
            node.children.set(i, inlineComponents(node.children.get(i), script, directory));
        }
        for (Map.Entry<String, List<Node>> property : node.properties.entrySet()) {
            List<Node> values = new ArrayList<>();
            for (Node value : property.getValue()) {
                values.add(inlineComponents(value, script, directory));
            }
            property.setValue(values);
        }
        return node;
    }

    private IOException unsupported(String detail) {
        return new IOException("Unsupported MXML reconstruction: " + detail);
    }

    private GraphTargetItem assignment(GraphTargetItem item) {
        return item instanceof DeclarationAVM2Item ? ((DeclarationAVM2Item) item).assignment : item;
    }

    private boolean propertyAssignment(GraphTargetItem item) {
        return item instanceof SetPropertyAVM2Item || item instanceof InitPropertyAVM2Item;
    }

    private boolean bindingField(String name) {
        return "_bindings".equals(name) || "_watchers".equals(name)
                || "_bindingsByDestination".equals(name) || "_bindingsBeginWithWord".equals(name);
    }

    private void readConstructor(Node root) throws IOException, InterruptedException {
        boolean bindingSetup = methods.containsKey("_" + pack.getPathScriptName() + "_bindingsSetup");
        List<GraphTargetItem> properties = new ArrayList<>();
        Map<Integer, GraphTargetItem> locals = new LinkedHashMap<>();
        Map<String, GraphTargetItem> namedLocals = new LinkedHashMap<>();
        Set<GraphTargetItem> immediateFactories = new HashSet<>();
        Map<Integer, String> stateIds = new LinkedHashMap<>();
        GraphTargetItem states = null;
        for (GraphTargetItem original : body(abc.instance_info.get(classIndex).iinit_index)) {
            GraphTargetItem item = assignment(original);
            if (item instanceof StoreNewActivationAVM2Item) {
                continue;
            }
            if (original instanceof DeclarationAVM2Item && (item instanceof GetLexAVM2Item || item instanceof LocalRegAVM2Item)) {
                continue;
            }
            if (item instanceof SetSlotAVM2Item) {
                String local = abc.constants.getString(((SetSlotAVM2Item) item).slotName.name_index);
                namedLocals.put(local, unwrap(item.value));
                if ("mx.core.DeferredInstanceFromFunction".equals(constructedType(unwrap(item.value)))
                        || "mx.core.DeferredInstanceFromClass".equals(constructedType(unwrap(item.value)))
                        || bindingSetup && ("bindings".equals(local) || "watchers".equals(local) || "target".equals(local)
                        || "watcherSetupUtilClass".equals(local) || "i".equals(local))) {
                    continue;
                }
            }
            if (item instanceof SetLocalAVM2Item) {
                locals.put(((SetLocalAVM2Item) item).regIndex, unwrap(item.value));
                // Flex creates locals for binding setup and deferred state instances.
                if (bindingSetup || item.value == null || unwrap(item.value) instanceof NullAVM2Item
                        || "mx.core.DeferredInstanceFromFunction".equals(constructedType(unwrap(item.value)))
                        || "mx.core.DeferredInstanceFromClass".equals(constructedType(unwrap(item.value)))) {
                    continue;
                }
            }
            if (propertyAssignment(item)) {
                String name = name(propertyName(item));
                if (bindingField(name)) {
                    continue;
                }
                GraphTargetItem value = unwrap(item.value);
                if (register(propertyObject(item), 0) && value instanceof GetPropertyAVM2Item) {
                    GraphTargetItem array = ((GetPropertyAVM2Item) value).object;
                    if (array instanceof GetLexAVM2Item && "states".equals(qualified((GetLexAVM2Item) array))
                            || array instanceof GetPropertyAVM2Item && register(((GetPropertyAVM2Item) array).object, 0)
                            && "states".equals(name(((GetPropertyAVM2Item) array).propertyName))) {
                        stateIds.put(arrayIndex(value), name);
                        continue;
                    }
                }
                boolean fieldInitializer = false;
                if (register(propertyObject(item), 0) && !name.startsWith("_" + pack.getPathScriptName() + "_")) {
                    for (Trait trait : abc.instance_info.get(classIndex).instance_traits.traits) {
                        if (trait instanceof TraitSlotConst && name.equals(traitName(trait))) {
                            convertData.assignedValues.put((TraitSlotConst) trait, new AssignedValue(item, item.value,
                                    GraphTextWriter.TRAIT_INSTANCE_INITIALIZER, abc.instance_info.get(classIndex).iinit_index));
                            fieldInitializer = true;
                            break;
                        }
                    }
                }
                if (fieldInitializer) {
                    continue;
                }
                if ("states".equals(name)) {
                    states = item.value;
                    continue;
                }
            }
            if (item instanceof CallPropertyAVM2Item) {
                CallPropertyAVM2Item call = (CallPropertyAVM2Item) item;
                String method = name(call.propertyName);
                if ("getInstance".equals(method) && call.arguments.isEmpty()) {
                    GraphTargetItem deferred = deferredFactory(call.receiver, locals, namedLocals);
                    if ("mx.core.DeferredInstanceFromFunction".equals(constructedType(deferred))
                            || "mx.core.DeferredInstanceFromClass".equals(constructedType(deferred))) {
                        immediateFactories.add(deferred);
                        continue;
                    }
                }
                if (register(call.receiver, 0) && call.arguments.isEmpty() && methods.containsKey(method)
                        && method.startsWith("_" + pack.getPathScriptName() + "_") && method.endsWith("_i")) {
                    Node declaration = component(item);
                    if (!declarations.contains(declaration)) {
                        declarations.add(declaration);
                    }
                    continue;
                }
                if (bindingSetup && "setup".equals(method)) {
                    continue;
                }
                if (bindingSetup && "executeBindings".equals(method) && call.receiver instanceof GetLexAVM2Item
                        && "mx.binding.BindingManager".equals(qualified((GetLexAVM2Item) call.receiver))) {
                    continue;
                }
            }
            if (bindingSetup && (item instanceof IfItem || item.getClass().getSimpleName().equals("WhileItem"))) {
                // The generated watcher initialization and initial Binding.execute loop.
                continue;
            }
            properties.add(item);
        }
        readRootDescriptor(root);
        readProperties(root, properties, 0);
        if (states != null) {
            readStates(root, states, locals, namedLocals, immediateFactories);
            List<Node> definitions = root.properties.get("states");
            for (Map.Entry<Integer, String> entry : stateIds.entrySet()) {
                if (entry.getKey() < 0 || entry.getKey() >= definitions.size()) {
                    throw unsupported("State ID array index " + entry.getKey());
                }
                Node state = definitions.get(entry.getKey());
                state.attributes.put("id", entry.getValue());
                ids.add(entry.getValue());
                nodesById.put(entry.getValue(), state);
            }
        }
    }

    private void readRootDescriptor(Node root) throws IOException, InterruptedException {
        for (Trait trait : abc.instance_info.get(classIndex).instance_traits.traits) {
            if (trait instanceof TraitSlotConst && "_documentDescriptor_".equals(traitName(trait))
                    && convertData.assignedValues.containsKey(trait)) {
                Node descriptor = descriptor(convertData.assignedValues.get(trait).value);
                if (!root.type.equals(descriptor.type)) {
                    throw unsupported("Document descriptor type " + descriptor.type);
                }
                root.attributes.putAll(descriptor.attributes);
                root.properties.putAll(descriptor.properties);
                root.children.addAll(descriptor.children);
                factories.add("_documentDescriptor_");
            }
        }
    }

    private Node descriptor(GraphTargetItem value) throws IOException, InterruptedException {
        value = unwrap(value);
        if (!"mx.core.UIComponentDescriptor".equals(constructedType(value)) || constructionArguments(value).size() != 1) {
            throw unsupported("Expected a UIComponentDescriptor");
        }
        Map<String, GraphTargetItem> data = object(constructionArguments(value).get(0));
        GraphTargetItem type = unwrap(data.get("type"));
        if (!(type instanceof GetLexAVM2Item)) {
            throw unsupported("Descriptor component type");
        }
        Node node = new Node(qualified((GetLexAVM2Item) type));
        if (data.containsKey("id")) {
            String id = string(data.get("id"));
            node.attributes.put("id", id);
            ids.add(id);
            nodesById.put(id, node);
        }
        for (Map.Entry<String, GraphTargetItem> entry : data.entrySet()) {
            String key = entry.getKey();
            if ("type".equals(key) || "id".equals(key)) {
                continue;
            }
            if ("events".equals(key)) {
                for (Map.Entry<String, GraphTargetItem> event : object(entry.getValue()).entrySet()) {
                    node.attributes.put(event.getKey(), event(string(event.getValue())));
                }
            } else if ("effects".equals(key) && unwrap(entry.getValue()) instanceof NewArrayAVM2Item) {
                for (GraphTargetItem effect : ((NewArrayAVM2Item) unwrap(entry.getValue())).values) {
                    String name = string(effect);
                    node.attributes.putIfAbsent(name, "{null}");
                }
            } else if ("propertiesFactory".equals(key) && unwrap(entry.getValue()) instanceof NewFunctionAVM2Item) {
                List<GraphTargetItem> items = body(((NewFunctionAVM2Item) unwrap(entry.getValue())).methodIndex);
                items = new ArrayList<>(items);
                items.removeIf(item -> item instanceof StoreNewActivationAVM2Item);
                if (items.size() != 1 || !(items.get(0) instanceof ReturnValueAVM2Item)) {
                    throw unsupported("Descriptor properties factory");
                }
                for (Map.Entry<String, GraphTargetItem> property : object(unwrap(items.get(0).value)).entrySet()) {
                    if ("childDescriptors".equals(property.getKey())) {
                        node.children.addAll(descriptorChildren(property.getValue()));
                    } else {
                        setValue(node, property.getKey(), property.getValue());
                    }
                }
            } else if ("stylesFactory".equals(key) && unwrap(entry.getValue()) instanceof NewFunctionAVM2Item) {
                for (GraphTargetItem style : body(((NewFunctionAVM2Item) unwrap(entry.getValue())).methodIndex)) {
                    if (propertyAssignment(style) && register(propertyObject(style), 0)) {
                        setValue(node, name(propertyName(style)), style.value);
                    } else if (!(style instanceof ReturnVoidAVM2Item)) {
                        throw unsupported("Descriptor styles factory");
                    }
                }
            } else {
                throw unsupported("Descriptor field " + key);
            }
        }
        return node;
    }

    private List<Node> descriptorChildren(GraphTargetItem value) throws IOException, InterruptedException {
        value = unwrap(value);
        if (!(value instanceof NewArrayAVM2Item)) {
            throw unsupported("Nonliteral child descriptors");
        }
        List<Node> result = new ArrayList<>();
        for (GraphTargetItem child : ((NewArrayAVM2Item) value).values) {
            result.add(descriptor(child));
        }
        return result;
    }

    private final class DescriptorCursor {

        final List<GraphTargetItem> values;
        int index;

        DescriptorCursor(GraphTargetItem value) throws IOException {
            value = unwrap(value);
            if (!(value instanceof NewArrayAVM2Item)) {
                throw unsupported("Expected a compact descriptor array");
            }
            values = ((NewArrayAVM2Item) value).values;
        }

        GraphTargetItem next() throws IOException {
            if (index >= values.size()) {
                throw unsupported("Truncated compact descriptor");
            }
            return unwrap(values.get(index++));
        }

        int count() throws IOException {
            GraphTargetItem value = next();
            if (!value.isCompileTime() || !(value.getResult() instanceof Number)) {
                throw unsupported("Nonconstant compact descriptor count");
            }
            double count = ((Number) value.getResult()).doubleValue();
            if (count < 0 || count != Math.floor(count) || count > values.size() - index) {
                throw unsupported("Invalid compact descriptor count " + count);
            }
            return (int) count;
        }
    }

    private List<Node> optimizedChildren(GraphTargetItem value) throws IOException, InterruptedException {
        DescriptorCursor cursor = new DescriptorCursor(value);
        List<Node> result = new ArrayList<>();
        while (cursor.index < cursor.values.size()) {
            result.add(optimizedNode(cursor));
        }
        return result;
    }

    private GraphTargetItem compactValue(GraphTargetItem value) throws IOException, InterruptedException {
        value = unwrap(value);
        if (value instanceof NewArrayAVM2Item) {
            return value;
        }
        String field = value instanceof GetLexAVM2Item ? abc.constants.getString(((GetLexAVM2Item) value).propertyName.name_index)
                : value instanceof GetPropertyAVM2Item ? name(((GetPropertyAVM2Item) value).propertyName) : null;
        if (field != null) {
            for (Trait trait : abc.instance_info.get(classIndex).instance_traits.traits) {
                if (!field.equals(traitName(trait))) {
                    continue;
                }
                if (trait.kindType == Trait.TRAIT_GETTER && ("MXMLDescriptor".equals(field) || "MXMLProperties".equals(field))) {
                    return null; // Decoded once from the corresponding getter.
                }
                if (trait instanceof TraitSlotConst && convertData.assignedValues.containsKey(trait)) {
                    GraphTargetItem array = unwrap(convertData.assignedValues.get(trait).value);
                    if (array instanceof NewArrayAVM2Item) {
                        factories.add(field);
                        return array;
                    }
                }
            }
        }
        throw unsupported("Nonliteral compact descriptor data");
    }

    private Node optimizedNode(DescriptorCursor cursor) throws IOException, InterruptedException {
        String type = qualifiedReference(cursor.next());
        if (type == null) {
            throw unsupported("Compact descriptor component type");
        }
        Node node = new Node(type);
        optimizedAttributes(node, cursor);
        GraphTargetItem children = cursor.next();
        if (!(children instanceof NullAVM2Item)) {
            node.children.addAll(optimizedChildren(children));
        }
        return node;
    }

    private void optimizedAttributes(Node node, DescriptorCursor cursor) throws IOException, InterruptedException {
        for (int section = 0; section < 3; section++) {
            int count = cursor.count();
            for (int i = 0; i < count; i++) {
                String property = string(cursor.next());
                GraphTargetItem simple = cursor.next();
                GraphTargetItem value = cursor.next();
                if ("document".equals(property) && register(value, 0)) {
                    continue;
                }
                if ("_id".equals(property)) {
                    property = "id";
                }
                if ("id".equals(property)) {
                    String id = string(value);
                    ids.add(id);
                    nodesById.put(id, node);
                }
                if (simple instanceof NullAVM2Item) {
                    List<Node> items = optimizedChildren(value);
                    if ("mxmlContent".equals(property)) {
                        node.children.addAll(items);
                    } else {
                        Node array = new Node("Array");
                        array.children.addAll(items);
                        storeOptimizedProperty(node, property, array);
                    }
                } else if (simple instanceof UndefinedAVM2Item || simple instanceof GetLexAVM2Item && "undefined".equals(qualified((GetLexAVM2Item) simple))) {
                    DescriptorCursor vector = new DescriptorCursor(value);
                    vector.next(); // Compiler type hint; the conversion closure supplies the type.
                    GraphTargetItem conversion = vector.next();
                    if (!(conversion instanceof NewFunctionAVM2Item)) {
                        throw unsupported("Compact vector conversion function");
                    }
                    int type = abc.method_info.get(((NewFunctionAVM2Item) conversion).methodIndex).ret_type;
                    String name = type == 0 ? "" : abc.constants.getMultiname(type)
                            .getNameWithNamespace(new LinkedHashSet<>(), abc, abc.constants, true).toRawString();
                    if (!name.contains("Vector.<") || !name.endsWith(">")) {
                        throw unsupported("Compact vector element type");
                    }
                    Node result = new Node("Vector");
                    com.jpexs.decompiler.flash.abc.types.Multiname vectorType = abc.constants.getMultiname(type);
                    result.attributes.put("type", vectorType.params != null && vectorType.params.length == 1
                            ? abc.constants.getMultiname(vectorType.params[0]).getNameWithNamespace(new LinkedHashSet<>(), abc, abc.constants, true).toRawString()
                            : name.substring(name.indexOf(".<") + 2, name.length() - 1));
                    while (vector.index < vector.values.size()) {
                        result.children.add(optimizedNode(vector));
                    }
                    storeOptimizedProperty(node, property, result);
                } else if (simple.isCompileTime() && Boolean.FALSE.equals(simple.getResult())) {
                    DescriptorCursor object = new DescriptorCursor(value);
                    Node child = optimizedNode(object);
                    if (object.index != object.values.size()) {
                        throw unsupported("Extra compact object data");
                    }
                    storeOptimizedProperty(node, property, child);
                } else if (simple.isCompileTime() && Boolean.TRUE.equals(simple.getResult())) {
                    setValue(node, property, value);
                } else {
                    throw unsupported("Compact descriptor value flag");
                }
            }
        }
        int events = cursor.count();
        for (int i = 0; i < events; i++) {
            String name = string(cursor.next());
            GraphTargetItem handler = cursor.next();
            node.attributes.put(name, event(reference(handler)));
        }
    }

    private void storeOptimizedProperty(Node node, String property, Node value) {
        if (node == documentRoot) {
            for (Trait trait : abc.instance_info.get(classIndex).instance_traits.traits) {
                if (property.equals(traitName(trait)) && (trait instanceof TraitSlotConst || trait.kindType == Trait.TRAIT_GETTER)) {
                    value.attributes.put("id", property);
                    ids.add(property);
                    nodesById.put(property, value);
                    declarations.add(value);
                    return;
                }
            }
        }
        node.properties.put(property, Collections.singletonList(value));
    }

    private void readOptimizedGetter(Node root, String name, boolean properties) throws IOException, InterruptedException {
        for (Trait trait : abc.instance_info.get(classIndex).instance_traits.traits) {
            if (!(trait instanceof TraitMethodGetterSetter) || trait.kindType != Trait.TRAIT_GETTER || !name.equals(traitName(trait))) {
                continue;
            }
            List<GraphTargetItem> arrays = new ArrayList<>();
            for (GraphTargetItem item : body(((TraitMethodGetterSetter) trait).method_info)) {
                item.visitRecursively(new AbstractGraphTargetVisitor() {
                    @Override
                    public boolean visit(GraphTargetItem value) {
                        if (value instanceof ReturnValueAVM2Item && unwrap(value.value) instanceof GetPropertyAVM2Item) {
                            GraphTargetItem key = ((GetPropertyAVM2Item) unwrap(value.value)).propertyName;
                            if (key instanceof FullMultinameAVM2Item) {
                                factories.add(((FullMultinameAVM2Item) key).resolvedMultinameName);
                            }
                        }
                        if ((propertyAssignment(value) || value instanceof ReturnValueAVM2Item || value instanceof SetLocalAVM2Item)
                                && unwrap(value.value) instanceof NewArrayAVM2Item) {
                            arrays.add(unwrap(value.value));
                            if (propertyAssignment(value)) {
                                GraphTargetItem key = propertyName(value);
                                if (key instanceof FullMultinameAVM2Item) {
                                    factories.add(((FullMultinameAVM2Item) key).resolvedMultinameName);
                                }
                            }
                            return false;
                        }
                        return true;
                    }
                });
            }
            if (arrays.size() != 1) {
                throw unsupported("Compact descriptor getter " + name);
            }
            factories.add(name);
            if (properties) {
                DescriptorCursor cursor = new DescriptorCursor(arrays.get(0));
                optimizedAttributes(root, cursor);
                if (cursor.index != cursor.values.size()) {
                    throw unsupported("Extra compact root properties");
                }
            } else {
                root.children.addAll(optimizedChildren(arrays.get(0)));
            }
        }
    }

    private String string(GraphTargetItem value) throws IOException {
        value = unwrap(value);
        if (value instanceof StringAVM2Item) {
            return ((StringAVM2Item) value).getValue();
        }
        throw unsupported("Expected a string constant");
    }

    private Map<String, GraphTargetItem> object(GraphTargetItem value) throws IOException {
        value = unwrap(value);
        if (!(value instanceof NewObjectAVM2Item)) {
            throw unsupported("Expected an object literal");
        }
        Map<String, GraphTargetItem> result = new LinkedHashMap<>();
        for (NameValuePair pair : ((NewObjectAVM2Item) value).pairs) {
            result.put(string(pair.name), pair.value);
        }
        return result;
    }

    private GraphTargetItem deferredFactory(GraphTargetItem value, Map<Integer, GraphTargetItem> locals,
            Map<String, GraphTargetItem> namedLocals) {
        value = unwrap(value);
        if (value instanceof LocalRegAVM2Item) {
            return locals.get(((LocalRegAVM2Item) value).regIndex);
        }
        if (value instanceof GetLexAVM2Item) {
            return namedLocals.get(abc.constants.getString(((GetLexAVM2Item) value).propertyName.name_index));
        }
        if (value instanceof GetSlotAVM2Item) {
            return namedLocals.get(abc.constants.getString(((GetSlotAVM2Item) value).slotName.name_index));
        }
        return value;
    }

    private void readStates(Node root, GraphTargetItem value, Map<Integer, GraphTargetItem> locals,
            Map<String, GraphTargetItem> namedLocals, Set<GraphTargetItem> immediateFactories) throws IOException, InterruptedException {
        if (!(unwrap(value) instanceof NewArrayAVM2Item)) {
            throw unsupported("Nonliteral states array");
        }
        List<Node> states = new ArrayList<>();
        Map<String, Node> added = new LinkedHashMap<>();
        Map<GraphTargetItem, List<Node>> compactAdded = new java.util.IdentityHashMap<>();
        for (GraphTargetItem item : ((NewArrayAVM2Item) unwrap(value)).values) {
            item = unwrap(item);
            String stateId = null;
            if (propertyAssignment(item) && register(propertyObject(item), 0)) {
                stateId = name(propertyName(item));
                item = unwrap(item.value);
            }
            if (item instanceof CallPropertyAVM2Item && "State".equals(name(((CallPropertyAVM2Item) item).propertyName))
                    && ((CallPropertyAVM2Item) item).arguments.size() == 1) {
                item = unwrap(((CallPropertyAVM2Item) item).arguments.get(0));
            }
            if (item instanceof CallPropertyAVM2Item) {
                Node state = component(item);
                if (!"mx.states.State".equals(state.type)) {
                    throw unsupported("Legacy State factory type");
                }
                legacyStates.add(state);
                states.add(state);
                continue;
            }
            if (!"mx.states.State".equals(constructedType(item)) || constructionArguments(item).size() != 1) {
                throw unsupported("Nonliteral State construction");
            }
            Map<String, GraphTargetItem> definition = object(constructionArguments(item).get(0));
            String stateName = string(definition.get("name"));
            Node state = new Node("mx.states.State");
            state.attributes.put("name", stateName);
            if (stateId != null) {
                state.attributes.put("id", stateId);
                ids.add(stateId);
                nodesById.put(stateId, state);
            }
            for (Map.Entry<String, GraphTargetItem> entry : definition.entrySet()) {
                if (!"name".equals(entry.getKey()) && !"overrides".equals(entry.getKey())) {
                    if ("stateGroups".equals(entry.getKey()) && entry.getValue() instanceof NewArrayAVM2Item) {
                        List<String> groups = new ArrayList<>();
                        for (GraphTargetItem group : ((NewArrayAVM2Item) entry.getValue()).values) {
                            groups.add(string(group));
                        }
                        state.attributes.put("stateGroups", String.join(",", groups));
                    } else {
                        setValue(state, entry.getKey(), entry.getValue());
                    }
                }
            }
            states.add(state);
            GraphTargetItem overrides = unwrap(definition.get("overrides"));
            if (!(overrides instanceof NewArrayAVM2Item)) {
                throw unsupported("Nonliteral state overrides");
            }
            for (GraphTargetItem override : ((NewArrayAVM2Item) overrides).values) {
                String overrideId = null;
                if (propertyAssignment(override) && register(propertyObject(override), 0)) {
                    overrideId = name(propertyName(override));
                    override = unwrap(override.value);
                }
                if (override instanceof CallPropertyAVM2Item) {
                    CallPropertyAVM2Item cast = (CallPropertyAVM2Item) override;
                    if (("SetProperty".equals(name(cast.propertyName)) || "SetStyle".equals(name(cast.propertyName)))
                            && cast.arguments.size() == 1) {
                        override = unwrap(cast.arguments.get(0));
                    }
                }
                if (!(override instanceof CallPropertyAVM2Item)) {
                    throw unsupported("State override construction");
                }
                CallPropertyAVM2Item call = (CallPropertyAVM2Item) override;
                if (!"initializeFromObject".equals(name(call.propertyName)) || call.arguments.size() != 1) {
                    throw unsupported("State override initialization");
                }
                Map<String, GraphTargetItem> data = object(call.arguments.get(0));
                String type = constructedType(call.receiver);
                if ("mx.states.SetEventHandler".equals(type)) {
                    String target = data.containsKey("target") && !(unwrap(data.get("target")) instanceof NullAVM2Item)
                            ? string(data.get("target")) : "";
                    Node node = target.isEmpty() ? root : nodesById.get(target);
                    if (node == null) {
                        throw unsupported("Unknown state event target " + target);
                    }
                    String property = string(data.get("name"));
                    GraphTargetItem handler = unwrap(data.get("handlerFunction"));
                    node.attributes.put(property + "." + stateName, handler instanceof NullAVM2Item ? "" : event(reference(handler)));
                    if (!node.attributes.containsKey(property) && data.containsKey("originalHandlerFunction")) {
                        GraphTargetItem original = unwrap(data.get("originalHandlerFunction"));
                        if (!(original instanceof NullAVM2Item)) {
                            node.attributes.put(property, event(reference(original)));
                        }
                    }
                } else if ("mx.states.SetProperty".equals(type) || "mx.states.SetStyle".equals(type)) {
                    String target = data.containsKey("target") && !(unwrap(data.get("target")) instanceof NullAVM2Item)
                            ? string(data.get("target")) : "";
                    Node node = target.isEmpty() ? root : nodesById.get(target);
                    if (node == null) {
                        throw unsupported("Unknown state target " + target);
                    }
                    String property = string(data.get("name")) + "." + stateName;
                    GraphTargetItem stateValue = unwrap(data.get("value"));
                    if (overrideId != null && (stateValue instanceof UndefinedAVM2Item
                            || stateValue instanceof GetLexAVM2Item && "undefined".equals(qualified((GetLexAVM2Item) stateValue)))) {
                        Node proxy = new Node("Object");
                        ids.add(overrideId);
                        nodesById.put(overrideId, proxy);
                        stateBindingTargets.put(proxy, new java.util.AbstractMap.SimpleEntry<>(node, property));
                    } else {
                        setValue(node, property, data.get("value"));
                    }
                } else if ("mx.states.AddItems".equals(type)) {
                    List<Node> nodes;
                    boolean fresh;
                    GraphTargetItem factory = null;
                    boolean immediate = false;
                    if (data.containsKey("items")) {
                        GraphTargetItem items = unwrap(data.get("items"));
                        nodes = compactAdded.get(items);
                        fresh = nodes == null;
                        if (fresh) {
                            Node created = valueNode(items);
                            nodes = "Array".equals(created.type) ? created.children : Collections.singletonList(created);
                            compactAdded.put(items, nodes);
                        }
                        immediate = true;
                    } else if (data.containsKey("itemsDescriptor")) {
                        GraphTargetItem descriptor = compactValue(data.get("itemsDescriptor"));
                        nodes = compactAdded.get(descriptor);
                        fresh = nodes == null;
                        if (fresh) {
                            nodes = optimizedChildren(descriptor);
                            compactAdded.put(descriptor, nodes);
                        }
                    } else {
                        factory = deferredFactory(data.get("itemsFactory"), locals, namedLocals);
                        if ("mx.core.DeferredInstanceFromClass".equals(constructedType(factory)) && constructionArguments(factory).size() == 1) {
                            nodes = compactAdded.get(factory);
                            fresh = nodes == null;
                            if (fresh) {
                                String component = qualifiedReference(constructionArguments(factory).get(0));
                                if (component == null) {
                                    throw unsupported("Deferred state component class");
                                }
                                nodes = Collections.singletonList(new Node(component));
                                compactAdded.put(factory, nodes);
                            }
                        } else {
                            if (!"mx.core.DeferredInstanceFromFunction".equals(constructedType(factory))) {
                                throw unsupported("Deferred state component factory");
                            }
                            List<GraphTargetItem> args = constructionArguments(factory);
                            if (args.size() > 2 || args.isEmpty()) {
                                throw unsupported("Deferred state factory arguments");
                            }
                            String method = reference(args.get(0));
                            Node node = added.get(method);
                            fresh = node == null;
                            if (fresh) {
                                node = componentFactory(method);
                                added.put(method, node);
                            }
                            nodes = Collections.singletonList(node);
                            if (args.size() == 2 && !(unwrap(args.get(1)) instanceof NullAVM2Item)) {
                                readStateDestructor(args.get(1), nodes);
                            }
                        }
                    }
                    if (fresh) {
                        placeStateItems(root, data, nodes);
                    }
                    for (Node node : nodes) {
                        String creation = data.containsKey("creationPolicy") ? string(data.get("creationPolicy")) : "auto";
                        if (immediate || immediateFactories.contains(factory) || "all".equals(creation)) {
                            node.attributes.put("itemCreationPolicy", "immediate");
                        } else if (!"auto".equals(creation)) {
                            throw unsupported("State creation policy " + creation);
                        }
                        if (data.containsKey("destructionPolicy")) {
                            String destruction = string(data.get("destructionPolicy"));
                            if (!"auto".equals(destruction) && !"never".equals(destruction)) {
                                throw unsupported("State destruction policy " + destruction);
                            }
                            node.attributes.put("itemDestructionPolicy", destruction);
                        }
                        String previous = node.attributes.get("includeIn");
                        node.attributes.put("includeIn", previous == null ? stateName : previous + "," + stateName);
                    }
                } else {
                    throw unsupported("State override " + type);
                }
            }
        }
        root.properties.put("states", states);
    }

    private void placeStateItems(Node root, Map<String, GraphTargetItem> data, List<Node> nodes) throws IOException {
        String destination = data.containsKey("destination") && !(unwrap(data.get("destination")) instanceof NullAVM2Item)
                ? string(data.get("destination")) : "";
        Node parent = destination.isEmpty() ? root : nodesById.get(destination);
        if (parent == null || data.containsKey("propertyName") && !"mxmlContent".equals(string(data.get("propertyName")))) {
            throw unsupported("State AddItems destination " + destination);
        }
        String position = data.containsKey("position") ? string(data.get("position")) : "last";
        int index;
        if ("first".equals(position)) {
            index = 0;
        } else if ("last".equals(position)) {
            index = parent.children.size();
        } else if ("before".equals(position) || "after".equals(position)) {
            GraphTargetItem relative = unwrap(data.get("relativeTo"));
            List<GraphTargetItem> targets = relative instanceof NewArrayAVM2Item
                    ? ((NewArrayAVM2Item) relative).values : Collections.singletonList(relative);
            index = -1;
            for (GraphTargetItem target : targets) {
                int current = parent.children.indexOf(nodesById.get(string(target)));
                if (current >= 0) {
                    index = current;
                    break;
                }
            }
            if (index < 0) {
                throw unsupported("State AddItems relative target");
            }
            if ("after".equals(position)) {
                index++;
            }
        } else {
            throw unsupported("State AddItems position " + position);
        }
        parent.children.addAll(index, nodes);
    }

    private void readStateDestructor(GraphTargetItem function, List<Node> nodes) throws IOException {
        String name = reference(function);
        TraitMethodGetterSetter method = methods.get(name);
        if (method == null) {
            throw unsupported("Missing state destructor " + name);
        }
        Set<String> resetIds = new HashSet<>();
        List<Node> pending = new ArrayList<>(nodes);
        for (int i = 0; i < pending.size(); i++) {
            Node node = pending.get(i);
            if (node.attributes.containsKey("id")) {
                resetIds.add(node.attributes.get("id"));
            }
            pending.addAll(node.children);
            for (List<Node> property : node.properties.values()) {
                pending.addAll(property);
            }
        }
        for (GraphTargetItem item : body(method.method_info)) {
            if (propertyAssignment(item) && register(propertyObject(item), 0) && unwrap(item.value) instanceof NullAVM2Item
                    && resetIds.contains(name(propertyName(item))) || item instanceof ReturnVoidAVM2Item) {
                continue;
            }
            throw unsupported("Custom state destructor " + name);
        }
        factories.add(name);
    }

    private Node boundStateTarget(Node root, String expression) throws IOException {
        if (expression == null || "{this}".equals(expression) || "{null}".equals(expression)) {
            return root;
        }
        if (expression.matches("[A-Za-z_$][A-Za-z0-9_$]*") && nodesById.containsKey(expression)) {
            return nodesById.get(expression);
        }
        if (expression == null || !expression.matches("\\{(?:this\\.)?[A-Za-z_$][A-Za-z0-9_$]*}")) {
            throw unsupported("Dynamic legacy state target " + expression);
        }
        Node target = nodesById.get(expression.substring(1, expression.length() - 1).replaceFirst("^this\\.", ""));
        if (target == null) {
            throw unsupported("Unknown legacy state target " + expression);
        }
        return target;
    }

    private List<Node> legacyOverrides(Node state, Set<Node> seen) throws IOException {
        if (!seen.add(state)) {
            throw unsupported("Cyclic legacy state inheritance");
        }
        List<Node> result = new ArrayList<>();
        String base = state.attributes.get("basedOn");
        if (base != null && !base.isEmpty()) {
            Node parent = null;
            for (Node candidate : legacyStates) {
                if (base.equals(candidate.attributes.get("name"))) {
                    parent = candidate;
                    break;
                }
            }
            if (parent == null) {
                throw unsupported("Unknown legacy base state " + base);
            }
            result.addAll(legacyOverrides(parent, seen));
        }
        List<Node> values = state.properties.get("overrides");
        if (values != null) {
            if (values.size() != 1 || !"Array".equals(values.get(0).type)) {
                throw unsupported("Legacy state override list");
            }
            result.addAll(values.get(0).children);
        }
        return result;
    }

    private Node parentOf(Node root, Node child) {
        for (Node node : root.children) {
            if (node == child) {
                return root;
            }
            Node parent = parentOf(node, child);
            if (parent != null) {
                return parent;
            }
        }
        return null;
    }

    private void flattenLegacyStates(Node root) throws IOException {
        Map<Node, List<Node>> overrides = new LinkedHashMap<>();
        for (Node state : legacyStates) {
            overrides.put(state, legacyOverrides(state, new HashSet<>()));
        }
        Set<Node> placed = new HashSet<>();
        for (Map.Entry<Node, List<Node>> entry : overrides.entrySet()) {
            String name = entry.getKey().attributes.get("name");
            for (Node override : entry.getValue()) {
                if ("mx.states.AddChild".equals(override.type)) {
                    List<Node> children = override.properties.get("target");
                    if (children == null || children.size() != 1) {
                        throw unsupported("Legacy state AddChild target");
                    }
                    Node target = children.get(0);
                    String creationPolicy = override.attributes.getOrDefault("creationPolicy", "auto");
                    if ("all".equals(creationPolicy)) {
                        target.attributes.put("itemCreationPolicy", "immediate");
                    } else if (!"auto".equals(creationPolicy)) {
                        throw unsupported("Legacy state creation policy " + creationPolicy);
                    }
                    if (placed.add(target)) {
                        Node relative = boundStateTarget(root, override.attributes.get("relativeTo"));
                        String position = override.attributes.getOrDefault("position", "lastChild");
                        if ("firstChild".equals(position) || "lastChild".equals(position)) {
                            relative.children.add("firstChild".equals(position) ? 0 : relative.children.size(), target);
                        } else if ("before".equals(position) || "after".equals(position)) {
                            Node parent = parentOf(root, relative);
                            if (parent == null) {
                                throw unsupported("Legacy state AddChild relative target");
                            }
                            parent.children.add(parent.children.indexOf(relative) + ("after".equals(position) ? 1 : 0), target);
                        } else {
                            throw unsupported("Legacy state AddChild position " + position);
                        }
                    }
                    String previous = target.attributes.get("includeIn");
                    target.attributes.put("includeIn", previous == null ? name : previous + "," + name);
                } else if ("mx.states.RemoveChild".equals(override.type)) {
                    Node target = boundStateTarget(root, override.attributes.get("target"));
                    String previous = target.attributes.get("excludeFrom");
                    target.attributes.put("excludeFrom", previous == null ? name : previous + "," + name);
                } else if ("mx.states.SetProperty".equals(override.type) || "mx.states.SetStyle".equals(override.type)) {
                    Node target = boundStateTarget(root, override.attributes.get("target"));
                    String property = override.attributes.get("name") + "." + name;
                    if (override.attributes.containsKey("value")) {
                        target.properties.remove(property);
                        target.attributes.put(property, override.attributes.get("value"));
                    } else if (override.properties.containsKey("value")) {
                        target.attributes.remove(property);
                        target.properties.put(property, override.properties.get("value"));
                    } else {
                        target.attributes.put(property, "{null}");
                    }
                } else {
                    throw unsupported("Legacy state override " + override.type);
                }
            }
            entry.getKey().properties.remove("overrides");
            entry.getKey().attributes.remove("basedOn");
        }
    }

    private String traitName(Trait trait) {
        return abc.constants.getString(trait.getName(abc).name_index);
    }

    private String name(GraphTargetItem item) throws IOException {
        if (item instanceof FullMultinameAVM2Item) {
            return abc.constants.getString(abc.constants.getMultiname(((FullMultinameAVM2Item) item).multinameIndex).name_index);
        }
        throw unsupported("Runtime property name");
    }

    static GraphTargetItem unwrap(GraphTargetItem item) {
        while (item instanceof CoerceAVM2Item || item instanceof ConvertAVM2Item) {
            item = item.value;
        }
        return item;
    }

    private boolean register(GraphTargetItem item, int index) {
        item = unwrap(item);
        return index == 0 && item instanceof ThisAVM2Item
                || item instanceof LocalRegAVM2Item && ((LocalRegAVM2Item) item).regIndex == index;
    }

    private List<GraphTargetItem> body(int method) throws IOException {
        MethodBody body = abc.findBody(method);
        if (body == null || body.convertException != null || body.convertedItems == null) {
            throw unsupported("Failed to decompile method " + method);
        }
        return body.convertedItems;
    }

    private List<GraphTargetItem> factory(String name) throws IOException {
        TraitMethodGetterSetter method = methods.get(name);
        if (method == null || !activeFactories.add(name)) {
            throw unsupported("Missing or recursive factory " + name);
        }
        factories.add(name);
        return body(method.method_info);
    }

    private String reference(GraphTargetItem value) throws IOException {
        value = unwrap(value);
        if (value instanceof GetLexAVM2Item) {
            String name = abc.constants.getString(((GetLexAVM2Item) value).propertyName.name_index);
            if (methods.containsKey(name)) {
                return name;
            }
        }
        if (value instanceof GetPropertyAVM2Item && register(((GetPropertyAVM2Item) value).object, 0)) {
            return name(((GetPropertyAVM2Item) value).propertyName);
        }
        throw unsupported("Expected a factory or event method reference");
    }

    private List<Node> children(GraphTargetItem value) throws IOException, InterruptedException {
        value = unwrap(value);
        if ("mx.core.DeferredInstanceFromFunction".equals(constructedType(value))) {
            List<GraphTargetItem> args = constructionArguments(value);
            if (args.size() == 1) {
                String factory = reference(args.get(0));
                List<Node> result = returnedChildren(factory(factory));
                activeFactories.remove(factory);
                return result;
            }
        }
        if (value instanceof NewArrayAVM2Item) {
            List<Node> result = new ArrayList<>();
            for (GraphTargetItem child : ((NewArrayAVM2Item) value).values) {
                result.add(component(child));
            }
            return result;
        }
        throw unsupported("Expected a component array or DeferredInstanceFromFunction");
    }

    private List<Node> returnedChildren(List<GraphTargetItem> items) throws IOException, InterruptedException {
        if (items.size() == 1 && items.get(0) instanceof ReturnValueAVM2Item) {
            return children(items.get(0).value);
        }
        throw unsupported("Nontrivial component array factory");
    }

    private String qualified(GetLexAVM2Item value) {
        return value.propertyName.getNameWithNamespace(new LinkedHashSet<>(), abc, abc.constants, true).toRawString();
    }

    private String qualifiedReference(GraphTargetItem value) {
        if (value instanceof GetLexAVM2Item) {
            return qualified((GetLexAVM2Item) value);
        }
        if (value instanceof TypeItem) {
            return ((TypeItem) value).fullTypeName.toRawString();
        }
        if (value instanceof GetPropertyAVM2Item && ((GetPropertyAVM2Item) value).propertyName instanceof FullMultinameAVM2Item) {
            return abc.constants.getMultiname(((FullMultinameAVM2Item) ((GetPropertyAVM2Item) value).propertyName).multinameIndex)
                    .getNameWithNamespace(new LinkedHashSet<>(), abc, abc.constants, true).toRawString();
        }
        return null;
    }

    private String constructedType(GraphTargetItem value) {
        if (value instanceof ConstructAVM2Item && ((ConstructAVM2Item) value).object instanceof GetLexAVM2Item) {
            return qualified((GetLexAVM2Item) ((ConstructAVM2Item) value).object);
        }
        if (value instanceof ConstructPropAVM2Item) {
            GraphTargetItem property = ((ConstructPropAVM2Item) value).propertyName;
            if (property instanceof FullMultinameAVM2Item) {
                return abc.constants.getMultiname(((FullMultinameAVM2Item) property).multinameIndex)
                        .getNameWithNamespace(new LinkedHashSet<>(), abc, abc.constants, true).toRawString();
            }
        }
        return null;
    }

    private List<GraphTargetItem> constructionArguments(GraphTargetItem value) {
        return value instanceof ConstructAVM2Item ? ((ConstructAVM2Item) value).args : ((ConstructPropAVM2Item) value).args;
    }

    private Node component(GraphTargetItem value) throws IOException, InterruptedException {
        value = unwrap(value);
        if (!(value instanceof CallPropertyAVM2Item)) {
            throw unsupported("Expected a component factory call");
        }
        CallPropertyAVM2Item call = (CallPropertyAVM2Item) value;
        if (!register(call.receiver, 0) || !call.arguments.isEmpty()) {
            throw unsupported("Component factory with arguments");
        }
        String method = name(call.propertyName);
        return componentFactory(method);
    }

    private Node componentFactory(String method) throws IOException, InterruptedException {
        if (componentNodes.containsKey(method)) {
            return componentNodes.get(method);
        }
        List<GraphTargetItem> items = factory(method);
        int resultTypeIndex = abc.method_info.get(methods.get(method).method_info).ret_type;
        String resultType = resultTypeIndex == 0 ? "*" : abc.constants.getMultiname(resultTypeIndex)
                .getNameWithNamespace(new LinkedHashSet<>(), abc, abc.constants, true).toRawString();
        Node node = null;
        int reg = -1;
        for (GraphTargetItem item : items) {
            if (item instanceof DeclarationAVM2Item) {
                item = ((DeclarationAVM2Item) item).assignment;
            }
            if (item instanceof SetLocalAVM2Item) {
                GraphTargetItem initial = unwrap(item.value);
                Node data = dataConstruction(initial);
                if (data == null && initial != null && (initial instanceof NewObjectAVM2Item || initial instanceof NewArrayAVM2Item || initial.isCompileTime())) {
                    data = valueNode(initial);
                }
                if (data == null && ("Class".equals(resultType) || "Function".equals(resultType))) {
                    data = new Node(resultType);
                    data.text = "Class".equals(resultType) && qualifiedReference(initial) != null
                            ? qualifiedReference(initial) : render(initial, methods.get(method).method_info);
                }
                if (node == null && data != null) {
                    node = data;
                    if ("Number".equals(node.type) && ("int".equals(resultType) || "uint".equals(resultType))) {
                        node.type = resultType;
                    }
                    reg = ((SetLocalAVM2Item) item).regIndex;
                    continue;
                }
                String type = constructedType(initial);
                if (type != null) {
                    if (constructionArguments(initial).isEmpty() && node == null) {
                        node = new Node(type);
                        reg = ((SetLocalAVM2Item) item).regIndex;
                    }
                }
            }
        }
        if (node == null) {
            if (items.size() == 1 && items.get(0) instanceof ReturnValueAVM2Item) {
                Node result = valueNode(items.get(0).value);
                componentNodes.put(method, result);
                activeFactories.remove(method);
                return result;
            }
            throw unsupported("No component construction in " + method);
        }
        // Nonvisual declarations have an initialized() call or a public field
        // assignment rather than a UIComponent.id property.
        for (GraphTargetItem item : items) {
            if (propertyAssignment(item)) {
                String property = name(propertyName(item));
                if (register(propertyObject(item), 0) && register(item.value, reg)) {
                    node.attributes.put("id", property);
                } else if (register(propertyObject(item), reg) && "id".equals(property)) {
                    node.attributes.put("id", string(item.value));
                }
            }
        }
        if (node.attributes.containsKey("id")) {
            String id = node.attributes.get("id");
            ids.add(id);
            nodesById.put(id, node);
        }
        componentNodes.put(method, node);
        readProperties(node, items, reg);
        if (node.children.contains(null)) {
            throw unsupported("Uninitialized vector element in " + method);
        }
        activeFactories.remove(method);
        return node;
    }

    private Node valueNode(GraphTargetItem value) throws IOException, InterruptedException {
        value = unwrap(value);
        if (value == null) {
            throw unsupported("Missing data value");
        }
        Node data = dataConstruction(value);
        if (data != null) {
            return data;
        }
        if (value instanceof CallPropertyAVM2Item) {
            return component(value);
        }
        if (value instanceof NewArrayAVM2Item) {
            Node array = new Node("Array");
            for (GraphTargetItem entry : ((NewArrayAVM2Item) value).values) {
                array.children.add(valueNode(entry));
            }
            return array;
        }
        if (value instanceof NewObjectAVM2Item) {
            Node object = new Node("Object");
            if (plainLiteral(value)) {
                object.literalExpression = render(value, abc.instance_info.get(classIndex).iinit_index);
            }
            for (Map.Entry<String, GraphTargetItem> entry : object(value).entrySet()) {
                setValue(object, entry.getKey(), entry.getValue());
            }
            return object;
        }
        if (value instanceof NullAVM2Item) {
            return new Node("null");
        }
        if (value.isCompileTime()) {
            Object result = value.getResult();
            Node node = new Node(result instanceof String ? "String" : result instanceof Boolean ? "Boolean" : "Number");
            node.text = literal(value);
            return node;
        }
        throw unsupported("Data value " + value.getClass().getSimpleName());
    }

    private boolean plainLiteral(GraphTargetItem value) {
        value = unwrap(value);
        if (value instanceof NewObjectAVM2Item) {
            for (NameValuePair pair : ((NewObjectAVM2Item) value).pairs) {
                if (!plainLiteral(pair.value)) {
                    return false;
                }
            }
            return true;
        }
        if (value instanceof NewArrayAVM2Item) {
            for (GraphTargetItem entry : ((NewArrayAVM2Item) value).values) {
                if (!plainLiteral(entry)) {
                    return false;
                }
            }
            return true;
        }
        return value instanceof NullAVM2Item || value.isCompileTime();
    }

    private void hoistLiteralData(Node node) {
        java.util.Iterator<Map.Entry<String, List<Node>>> properties = node.properties.entrySet().iterator();
        while (properties.hasNext()) {
            Map.Entry<String, List<Node>> property = properties.next();
            List<Node> values = property.getValue();
            if (values.size() == 1) {
                Node value = values.get(0);
                String id = value.attributes.get("id");
                if (value.literalExpression != null && id != null && id.startsWith("_" + pack.getPathScriptName() + "_Object")) {
                    value.scriptData = true;
                    scriptDataInitializers.put(id, value.literalExpression);
                    node.attributes.put(property.getKey(), "{" + id + "}");
                    properties.remove();
                    continue;
                }
            }
            for (Node child : values) {
                hoistLiteralData(child);
            }
        }
        for (Node child : node.children) {
            hoistLiteralData(child);
        }
    }

    private Node dataConstruction(GraphTargetItem value) throws IOException, InterruptedException {
        if (value instanceof ConstructAVM2Item && ((ConstructAVM2Item) value).object instanceof InitVectorAVM2Item) {
            return dataConstruction(((ConstructAVM2Item) value).object);
        }
        if (value instanceof XMLAVM2Item) {
            Node result = new Node("XML");
            result.attributes.put("format", "e4x");
            StringBuilder content = new StringBuilder();
            for (GraphTargetItem part : ((XMLAVM2Item) value).parts) {
                if (!(part instanceof StringAVM2Item)) {
                    throw unsupported("Dynamic XML construction");
                }
                content.append(((StringAVM2Item) part).getValue());
            }
            result.inlineXml = literalXml(content.toString());
            return result;
        }
        if ("XML".equals(constructedType(value)) || "XMLList".equals(constructedType(value))) {
            List<GraphTargetItem> args = constructionArguments(value);
            if (args.size() == 1 && unwrap(args.get(0)) instanceof StringAVM2Item) {
                Node result = new Node(constructedType(value));
                if ("XML".equals(result.type)) {
                    result.attributes.put("format", "e4x");
                }
                result.inlineXml = literalXml(string(unwrap(args.get(0))));
                return result;
            }
        }
        GraphTargetItem subtype = null;
        List<GraphTargetItem> values = null;
        boolean fixed = false;
        int length = 0;
        List<GraphTargetItem> vectorArguments = null;
        if (value instanceof InitVectorAVM2Item) {
            subtype = ((InitVectorAVM2Item) value).subtype;
            values = ((InitVectorAVM2Item) value).arguments;
        } else if (value instanceof ConstructAVM2Item && ((ConstructAVM2Item) value).object instanceof ApplyTypeAVM2Item) {
            ApplyTypeAVM2Item type = (ApplyTypeAVM2Item) ((ConstructAVM2Item) value).object;
            if (type.params.size() == 1 && "__AS3__.vec.Vector".equals(qualifiedReference(type.object))) {
                subtype = type.params.get(0);
                vectorArguments = ((ConstructAVM2Item) value).args;
            }
        }
        String constructed = constructedType(value);
        if (subtype == null && constructed != null && constructed.matches("(?:__AS3__\\.vec\\.)?Vector\\.<.+>")) {
            subtype = new TypeItem(DottedChain.parseNoSuffix(constructed.substring(constructed.indexOf(".<") + 2, constructed.length() - 1)));
            vectorArguments = constructionArguments(value);
        }
        if (vectorArguments != null) {
            if (!vectorArguments.isEmpty() && vectorArguments.get(0).isCompileTime() && vectorArguments.get(0).getResult() instanceof Number) {
                length = ((Number) vectorArguments.get(0).getResult()).intValue();
            }
            if (vectorArguments.size() > 1 && vectorArguments.get(1).isCompileTime()) {
                fixed = Boolean.TRUE.equals(vectorArguments.get(1).getResult());
            }
        }
        if (subtype == null) {
            return null;
        }
        Node result = new Node("Vector");
        String type = subtype instanceof GetLexAVM2Item ? qualified((GetLexAVM2Item) subtype)
                : subtype instanceof TypeItem ? ((TypeItem) subtype).fullTypeName.toRawString() : render(subtype, abc.instance_info.get(classIndex).iinit_index);
        result.attributes.put("type", type);
        result.attributes.put("fixed", Boolean.toString(fixed));
        if (values != null) {
            for (GraphTargetItem item : values) {
                result.children.add(valueNode(item));
            }
        } else {
            if (length < 0 || length > 100000) {
                throw unsupported("Vector length " + length);
            }
            result.children.addAll(Collections.nCopies(length, null));
        }
        return result;
    }

    private String literalXml(String value) {
        return value.replaceAll("&(?!(?:amp|lt|gt|apos|quot|#[0-9]+|#x[0-9a-fA-F]+);)", "&amp;").replace("{", "\\{");
    }

    private void setValue(Node node, String property, GraphTargetItem value) throws IOException, InterruptedException {
        value = unwrap(value);
        if ("mx.states.AddChild".equals(node.type) && "targetFactory".equals(property)
                && "mx.core.DeferredInstanceFromFunction".equals(constructedType(value)) && constructionArguments(value).size() == 1) {
            node.properties.put("target", Collections.singletonList(componentFactory(reference(constructionArguments(value).get(0)))));
            return;
        }
        if ("operations".equals(property) && ("mx.rpc.remoting.mxml.RemoteObject".equals(node.type)
                || "mx.rpc.soap.mxml.WebService".equals(node.type))) {
            String type = "mx.rpc.remoting.mxml.RemoteObject".equals(node.type)
                    ? "mx.rpc.remoting.mxml.Operation" : "mx.rpc.soap.mxml.Operation";
            for (Map.Entry<String, GraphTargetItem> entry : object(value).entrySet()) {
                Node operation = valueNode(entry.getValue());
                if (!type.equals(operation.type) || !entry.getKey().equals(operation.attributes.get("name"))) {
                    throw unsupported("RPC operation " + entry.getKey());
                }
                operation.explicitTag = "mx.rpc.remoting.mxml.RemoteObject".equals(node.type) ? "method" : "operation";
                node.children.add(operation);
            }
            return;
        }
        if (resourceValue(value) != null) {
            node.attributes.put(property, resourceValue(value));
        } else if (dataConstruction(value) != null) {
            node.properties.put(property, Collections.singletonList(dataConstruction(value)));
        } else if (value instanceof CallPropertyAVM2Item || value instanceof NewArrayAVM2Item || value instanceof NewObjectAVM2Item
                || value instanceof NullAVM2Item) {
            node.properties.put(property, Collections.singletonList(valueNode(value)));
        } else if (value instanceof GetLexAVM2Item || value instanceof GetPropertyAVM2Item || register(value, 0)) {
            if (value instanceof GetLexAVM2Item) {
                String type = qualified((GetLexAVM2Item) value);
                for (ScriptPack candidate : swf.getAS3Packs()) {
                    if (candidate.getClassPath().toRawString().equals(type)) {
                        dependencies.add(type);
                        classReferences.put(type, classReferences.getOrDefault(type, 0) + 1);
                        if ("mx.core.ClassFactory".equals(node.type) && "generator".equals(property)) {
                            node.generatorClass = type;
                        }
                        break;
                    }
                }
            }
            node.attributes.put(property, "{" + render(value, abc.instance_info.get(classIndex).iinit_index) + "}");
            if (node.generatorClass != null && "generator".equals(property)) {
                node.generatorExpression = node.attributes.get(property);
            }
        } else {
            String text = literal(value);
            if ("percentWidth".equals(property) || "percentHeight".equals(property)) {
                property = "percentWidth".equals(property) ? "width" : "height";
                text += "%";
            }
            node.attributes.put(property, text);
        }
    }

    private String resourceValue(GraphTargetItem value) throws IOException {
        if (!(value instanceof CallPropertyAVM2Item)) {
            return null;
        }
        CallPropertyAVM2Item call = (CallPropertyAVM2Item) value;
        if (!name(call.propertyName).matches("get(?:String|StringArray|Boolean|Number|Int|Uint|Class|Object)")
                || !(unwrap(call.receiver) instanceof CallPropertyAVM2Item) || call.arguments.size() != 2) {
            return null;
        }
        CallPropertyAVM2Item manager = (CallPropertyAVM2Item) unwrap(call.receiver);
        if (!(manager.receiver instanceof GetLexAVM2Item) || !"mx.resources.ResourceManager".equals(qualified((GetLexAVM2Item) manager.receiver))
                || !"getInstance".equals(name(manager.propertyName)) || !manager.arguments.isEmpty()) {
            return null;
        }
        String bundle = string(call.arguments.get(0));
        String key = string(call.arguments.get(1));
        return "@Resource(bundle='" + bundle.replace("'", "\\'") + "', key='" + key.replace("'", "\\'") + "')";
    }

    private String literal(GraphTargetItem value) throws IOException {
        value = unwrap(value);
        if (!value.isCompileTime()) {
            throw unsupported("Nonconstant component property " + value.getClass().getSimpleName());
        }
        Object result = value.getResult();
        if (!(result instanceof String || result instanceof Number || result instanceof Boolean)) {
            throw unsupported("Non-scalar component property");
        }
        String text = result.toString();
        if (result instanceof Number && Double.isFinite(((Number) result).doubleValue())) {
            text = java.math.BigDecimal.valueOf(((Number) result).doubleValue()).stripTrailingZeros().toPlainString();
            if (Double.doubleToRawLongBits(((Number) result).doubleValue()) == Long.MIN_VALUE) {
                text = "-0";
            }
        }
        if (result instanceof Number && text.endsWith(".0")) {
            text = text.substring(0, text.length() - 2);
        }
        // MXML treats braces as binding syntax even inside an XML attribute.
        if (result instanceof String) {
            text = text.replace("{", "\\{");
            if (text.matches("(?s)@(?:Embed|Resource|Clear|ContextRoot)\\(.*")) {
                text = "\\" + text;
            }
        }
        return text;
    }

    private void readProperties(Node node, List<GraphTargetItem> items, int reg) throws IOException, InterruptedException {
        for (GraphTargetItem item : items) {
            if (item instanceof DeclarationAVM2Item) {
                item = ((DeclarationAVM2Item) item).assignment;
            }
            if (item instanceof SetPropertyAVM2Item || item instanceof InitPropertyAVM2Item) {
                GraphTargetItem object = propertyObject(item);
                GraphTargetItem propertyName = propertyName(item);
                if ("Vector".equals(node.type) && register(object, reg) && propertyName instanceof FullMultinameAVM2Item
                        && ((FullMultinameAVM2Item) propertyName).name != null) {
                    GraphTargetItem index = ((FullMultinameAVM2Item) propertyName).name;
                    if (!index.isCompileTime() || !(index.getResult() instanceof Number)) {
                        throw unsupported("Nonconstant vector index");
                    }
                    int position = ((Number) index.getResult()).intValue();
                    if (position < 0 || position >= node.children.size()) {
                        throw unsupported("Vector index " + position);
                    }
                    Node child = valueNode(item.value);
                    if ("Number".equals(child.type) && ("int".equals(node.attributes.get("type")) || "uint".equals(node.attributes.get("type")))) {
                        child.type = node.attributes.get("type");
                    }
                    node.children.set(position, child);
                    continue;
                }
                String property = name(propertyName);
                if (reg == 0 && "_document".equals(property) && register(item.value, 0)) {
                    continue;
                }
                if (!register(object, reg) && !(reg == 0 && object instanceof FindPropertyAVM2Item)) {
                    if (register(object, 0) && register(item.value, reg)
                            && property.equals(node.attributes.get("id"))) {
                        continue; // Compiler-generated assignment to the public ID accessor.
                    }
                    throw unsupported("Assignment outside the component being constructed: " + property + " on " + object.getClass().getSimpleName());
                }
                if ("document".equals(property) || "_document".equals(property)) {
                    continue;
                }
                if ("childDescriptors".equals(property)) {
                    node.children.addAll(descriptorChildren(item.value));
                } else if ("mxmlContent".equals(property) || "mxmlContentFactory".equals(property)) {
                    node.children.addAll(children(item.value));
                } else {
                    setValue(node, property, item.value);
                    if ("id".equals(property)) {
                        String id = string(item.value);
                        ids.add(id);
                        nodesById.put(id, node);
                    }
                }
            } else if (item instanceof CallPropertyAVM2Item) {
                CallPropertyAVM2Item call = (CallPropertyAVM2Item) item;
                String method = name(call.propertyName);
                boolean compactReceiver = register(call.receiver, reg) || reg == 0 && call.receiver instanceof FindPropertyAVM2Item;
                if (compactReceiver && "setupBindings".equals(method) && call.arguments.size() == 1) {
                    optimizedBindingData.add(compactValue(call.arguments.get(0)));
                    continue;
                }
                if (compactReceiver && ("generateMXMLAttributes".equals(method) || "setMXMLProperties".equals(method))
                        && call.arguments.size() == 1) {
                    GraphTargetItem array = compactValue(call.arguments.get(0));
                    if (array == null) {
                        continue;
                    }
                    DescriptorCursor cursor = new DescriptorCursor(array);
                    optimizedAttributes(node, cursor);
                    if (cursor.index != cursor.values.size()) {
                        throw unsupported("Extra compact root attributes");
                    }
                    continue;
                }
                if (compactReceiver && "setMXMLDescriptor".equals(method) && call.arguments.size() == 1) {
                    GraphTargetItem array = compactValue(call.arguments.get(0));
                    if (array != null) {
                        node.children.addAll(optimizedChildren(array));
                    }
                    continue;
                }
                if (compactReceiver && "generateMXMLInstances".equals(method) && call.arguments.size() >= 2) {
                    node.children.addAll(optimizedChildren(call.arguments.get(1)));
                    continue;
                }
                if (!register(call.receiver, reg)) {
                    if (call.receiver instanceof GetLexAVM2Item
                            && "mx.binding.BindingManager".equals(qualified((GetLexAVM2Item) call.receiver))
                            && "executeBindings".equals(name(call.propertyName)) && node.attributes.containsKey("id")) {
                        continue; // This initializes the ID; it does not define a data binding.
                    }
                    throw unsupported("External call in component factory");
                }
                if ("setStyle".equals(method) && call.arguments.size() == 2 && call.arguments.get(0) instanceof StringAVM2Item) {
                    setValue(node, string(call.arguments.get(0)), call.arguments.get(1));
                } else if ("addEventListener".equals(method) && call.arguments.size() == 2 && call.arguments.get(0) instanceof StringAVM2Item) {
                    node.attributes.put(((StringAVM2Item) call.arguments.get(0)).getValue(), event(reference(call.arguments.get(1))));
                } else if ("registerEffects".equals(method) && call.arguments.size() == 1 && unwrap(call.arguments.get(0)) instanceof NewArrayAVM2Item) {
                    for (GraphTargetItem effect : ((NewArrayAVM2Item) unwrap(call.arguments.get(0))).values) {
                        node.attributes.putIfAbsent(string(effect), "{null}");
                    }
                } else if ("initialized".equals(method) && call.arguments.size() == 2 && register(call.arguments.get(0), 0)
                        && (unwrap(call.arguments.get(1)) instanceof NullAVM2Item
                        || string(call.arguments.get(1)).equals(node.attributes.get("id")))) {
                    // IMXMLObject initialization is generated again from the ID.
                } else {
                    throw unsupported("Component method " + method);
                }
            } else if (item instanceof SetLocalAVM2Item && ((SetLocalAVM2Item) item).regIndex == reg
                    && (constructedType(unwrap(item.value)) != null || dataConstruction(unwrap(item.value)) != null
                    || unwrap(item.value) instanceof NewObjectAVM2Item || unwrap(item.value) instanceof NewArrayAVM2Item || unwrap(item.value).isCompileTime()
                    || "Class".equals(node.type) || "Function".equals(node.type))) {
                // The factory's component declaration was processed above.
            } else if (item instanceof ReturnValueAVM2Item && register(item.value, reg)) {
                // The factory returns the constructed component.
            } else if (item instanceof ConstructSuperAVM2Item && reg == 0 && ((ConstructSuperAVM2Item) item).args.isEmpty()) {
                // Default application constructor.
            } else if (item instanceof ReturnVoidAVM2Item) {
                // End of constructor.
            } else if (documentGuard(item, reg)) {
                // Flex assigns document only when it was not set by the component.
            } else {
                throw unsupported("Control flow or side effect in component factory: " + item.getClass().getSimpleName()
                        + " " + render(item, abc.instance_info.get(classIndex).iinit_index));
            }
        }
    }

    private GraphTargetItem propertyObject(GraphTargetItem item) {
        return item instanceof SetPropertyAVM2Item ? ((SetPropertyAVM2Item) item).object : ((InitPropertyAVM2Item) item).object;
    }

    private GraphTargetItem propertyName(GraphTargetItem item) {
        return item instanceof SetPropertyAVM2Item ? ((SetPropertyAVM2Item) item).propertyName : ((InitPropertyAVM2Item) item).propertyName;
    }

    private boolean documentGuard(GraphTargetItem item, int reg) throws IOException {
        if (!(item instanceof IfItem)) {
            return false;
        }
        IfItem guard = (IfItem) item;
        if (!(guard.expression instanceof NotItem) || !guard.onFalse.isEmpty() || guard.onTrue.size() != 1) {
            return false;
        }
        GraphTargetItem condition = guard.expression.value;
        GraphTargetItem assignment = guard.onTrue.get(0);
        return condition instanceof GetPropertyAVM2Item
                && register(((GetPropertyAVM2Item) condition).object, reg)
                && "document".equals(name(((GetPropertyAVM2Item) condition).propertyName))
                && (assignment instanceof SetPropertyAVM2Item || assignment instanceof InitPropertyAVM2Item)
                && register(propertyObject(assignment), reg) && "document".equals(name(propertyName(assignment)))
                && register(assignment.value, 0);
    }

    private String render(GraphTargetItem item, int method) throws InterruptedException {
        MethodBody body = abc.findBody(method);
        HighlightedTextWriter writer = new HighlightedTextWriter(Configuration.getCodeFormatting(), false);
        LocalData local = LocalData.create(new ArrayList<>(), swf.getAbcIndex(), abc,
                body.getLocalRegNames(abc), fullyQualifiedNames, new HashSet<>(), ScriptExportMode.AS,
                swf.version, new LinkedHashSet<>(), classIndex);
        item.toString(writer, local);
        return writer.toString();
    }

    private static final class Binding {

        String source;
        String destination;
        int counterpart = -1;
        boolean primary;
    }

    private int arrayIndex(GraphTargetItem value) throws IOException {
        value = unwrap(value);
        if (value instanceof GetPropertyAVM2Item) {
            value = ((GetPropertyAVM2Item) value).propertyName;
        }
        if (value instanceof FullMultinameAVM2Item) {
            GraphTargetItem index = ((FullMultinameAVM2Item) value).name;
            if (index != null && index.isCompileTime() && index.getResult() instanceof Number) {
                return ((Number) index.getResult()).intValue();
            }
        }
        throw unsupported("Binding array index");
    }

    private String bindingExpression(NewFunctionAVM2Item function) throws IOException, InterruptedException {
        List<GraphTargetItem> items = body(function.methodIndex);
        if (items.size() == 1 && items.get(0) instanceof ReturnValueAVM2Item) {
            return render(items.get(0).value, function.methodIndex);
        }
        // Flex wraps a String expression in undefined/null conversion code.
        // Recover the original expression rather than the temporary register.
        if (items.size() == 2 && items.get(1) instanceof ReturnValueAVM2Item) {
            GraphTargetItem initial = assignment(items.get(0));
            GraphTargetItem returned = unwrap(items.get(1).value);
            if (initial instanceof SetLocalAVM2Item && returned instanceof TernarOpItem) {
                return render(initial.value, function.methodIndex);
            }
        }
        throw unsupported("Non-expression binding source");
    }

    private void readBindings(Node root) throws IOException, InterruptedException {
        for (GraphTargetItem value : optimizedBindingData) {
            DescriptorCursor cursor = new DescriptorCursor(value);
            int count = cursor.count();
            for (int i = 0; i < count; i++) {
                GraphTargetItem source = cursor.next();
                GraphTargetItem setter = cursor.next();
                GraphTargetItem destination = cursor.next();
                if (!(setter instanceof NullAVM2Item)) {
                    if (!(setter instanceof NewFunctionAVM2Item)) {
                        throw unsupported("Compact binding destination function");
                    }
                    int assignments = 0;
                    for (GraphTargetItem statement : body(((NewFunctionAVM2Item) setter).methodIndex)) {
                        GraphTargetItem item = assignment(statement);
                        if (propertyAssignment(item) && register(unwrap(item.value), 1)) {
                            String target = (render(propertyObject(item), ((NewFunctionAVM2Item) setter).methodIndex)
                                    + "." + name(propertyName(item))).replaceFirst("^this\\.", "");
                            if (!target.equals(compactBindingPath(destination))) {
                                throw unsupported("Compact binding destination differs from its assignment");
                            }
                            assignments++;
                        } else if (!(item instanceof ReturnVoidAVM2Item)) {
                            throw unsupported("Compact binding with custom destination function");
                        }
                    }
                    if (assignments != 1) {
                        throw unsupported("Compact binding destination assignment");
                    }
                }
                Node binding = new Node("Binding");
                binding.attributes.put("source", source instanceof NewFunctionAVM2Item
                        ? bindingExpression((NewFunctionAVM2Item) source) : compactBindingPath(source));
                binding.attributes.put("destination", bindingDestination(compactBindingPath(destination), setter));
                bindings.add(binding);
            }
        }
        String methodName = "_" + pack.getPathScriptName() + "_bindingsSetup";
        TraitMethodGetterSetter method = methods.get(methodName);
        if (method == null) {
            return;
        }
        factories.add(methodName);
        factories.add("_" + pack.getPathScriptName() + "_bindingExprs");
        Map<Integer, Binding> recovered = new LinkedHashMap<>();
        for (GraphTargetItem item : body(method.method_info)) {
            if (item instanceof DeclarationAVM2Item) {
                GraphTargetItem declared = assignment(item);
                if (declared instanceof GetLexAVM2Item || declared instanceof LocalRegAVM2Item) {
                    continue;
                }
            }
            item = assignment(item);
            if (!propertyAssignment(item)) {
                if (item instanceof SetLocalAVM2Item || item instanceof SetSlotAVM2Item
                        || item instanceof StoreNewActivationAVM2Item || item instanceof ReturnValueAVM2Item) {
                    continue;
                }
                throw unsupported("Binding setup statement " + item.getClass().getSimpleName());
            }
            GraphTargetItem value = unwrap(item.value);
            boolean repeated = "mx.binding.RepeatableBinding".equals(constructedType(value));
            if ("mx.binding.Binding".equals(constructedType(value)) || repeated) {
                List<GraphTargetItem> args = constructionArguments(value);
                if (args.size() < 4 || args.size() > 5 || !register(args.get(0), 0)) {
                    throw unsupported("Binding constructor arguments");
                }
                Binding binding = new Binding();
                if (unwrap(args.get(1)) instanceof NewFunctionAVM2Item) {
                    NewFunctionAVM2Item source = (NewFunctionAVM2Item) unwrap(args.get(1));
                    binding.source = repeated ? repeatableExpression(source, root, string(args.get(3))) : bindingExpression(source);
                } else if (unwrap(args.get(1)) instanceof NullAVM2Item && args.size() == 5) {
                    binding.source = string(args.get(4));
                } else {
                    throw unsupported("Binding source");
                }
                binding.destination = bindingDestination(string(args.get(3)), unwrap(args.get(2)));
                recovered.put(arrayIndex(propertyName(item)), binding);
            } else if ("twoWayCounterpart".equals(name(propertyName(item)))) {
                Binding binding = recovered.get(arrayIndex(propertyObject(item)));
                if (binding == null) {
                    throw unsupported("Missing two-way binding");
                }
                binding.counterpart = arrayIndex(value);
            } else if ("isTwoWayPrimary".equals(name(propertyName(item))) && value.isCompileTime()
                    && value.getResult() instanceof Boolean && recovered.containsKey(arrayIndex(propertyObject(item)))) {
                recovered.get(arrayIndex(propertyObject(item))).primary = Boolean.TRUE.equals(value.getResult());
            } else {
                throw unsupported("Binding setup assignment");
            }
        }
        Set<Integer> emitted = new HashSet<>();
        for (Map.Entry<Integer, Binding> entry : recovered.entrySet()) {
            if (emitted.contains(entry.getKey())) {
                continue;
            }
            Binding binding = entry.getValue();
            if (binding.counterpart >= 0) {
                Binding other = recovered.get(binding.counterpart);
                if (other == null || other.counterpart != entry.getKey()) {
                    throw unsupported("Unpaired two-way binding");
                }
                emitted.add(entry.getKey());
                emitted.add(binding.counterpart);
                if (other.primary) {
                    binding = other;
                }
            }
            int dot = binding.destination.indexOf('.');
            Node target = dot > 0 ? nodesById.get(binding.destination.substring(0, dot)) : null;
            String property = dot > 0 ? binding.destination.substring(dot + 1) : binding.destination;
            if ((binding.counterpart < 0 || binding.primary) && target != null && property.matches("[A-Za-z_][A-Za-z0-9_]*")
                    && !target.scriptData && !"XML".equals(target.type) && !"XMLList".equals(target.type)) {
                target.attributes.put(property, (binding.primary ? "@{" : "{") + binding.source + "}");
            } else {
                Node node = new Node("Binding");
                node.attributes.put("source", binding.source);
                node.attributes.put("destination", binding.destination);
                if (binding.counterpart >= 0) {
                    node.attributes.put("twoWay", "true");
                }
                bindings.add(node);
            }
        }
        for (Map.Entry<Node, Map.Entry<Node, String>> entry : stateBindingTargets.entrySet()) {
            String expression = entry.getKey().attributes.get("value");
            if (expression == null) {
                throw unsupported("Missing binding for state override " + entry.getValue().getValue());
            }
            entry.getValue().getKey().attributes.put(entry.getValue().getValue(), expression);
        }
    }

    private List<String> repeaterScope(Node node, Node target, List<String> ancestors) {
        List<String> scope = new ArrayList<>(ancestors);
        if ("mx.core.Repeater".equals(node.type)) {
            scope.add(node.attributes.get("id"));
        }
        if (node == target) {
            return scope;
        }
        List<Node> children = new ArrayList<>(node.children);
        for (List<Node> property : node.properties.values()) {
            children.addAll(property);
        }
        for (Node child : children) {
            List<String> result = repeaterScope(child, target, scope);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    private String repeatableExpression(NewFunctionAVM2Item function, Node root, String destination) throws IOException, InterruptedException {
        String expression = bindingExpression(function);
        String indices = render(new LocalRegAVM2Item(null, null, 2, null, TypeItem.UNBOUNDED), function.methodIndex);
        String instances = render(new LocalRegAVM2Item(null, null, 1, null, TypeItem.UNBOUNDED), function.methodIndex);
        Set<Integer> identifiers = new HashSet<>();
        try {
            ActionScriptLexer lexer = new ActionScriptLexer(expression);
            ParsedSymbol symbol;
            while ((symbol = lexer.lex()).type != SymbolType.EOF) {
                if (symbol.type == SymbolType.IDENTIFIER) {
                    identifiers.add(symbol.position);
                }
            }
        } catch (AVM2ParseException ex) {
            throw unsupported("Repeatable binding expression");
        }
        String instance = "\\[" + Pattern.quote(instances) + "\\[[0-9]+\\]\\]";
        Pattern references = Pattern.compile("([A-Za-z_$][A-Za-z0-9_$]*)(?:(?:" + instance + ")*\\.mx_internal::getItemAt\\("
                + Pattern.quote(indices) + "\\[[0-9]+\\]\\)|(?:" + instance + ")+)|" + Pattern.quote(indices) + "\\[([0-9]+)\\]");
        Matcher matcher = references.matcher(expression);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            if (matcher.group(2) != null && identifiers.contains(matcher.start())) {
                String id = destination.replaceFirst("^this\\.", "").split("\\.")[0];
                List<String> scope = repeaterScope(root, nodesById.get(id), Collections.emptyList());
                int level = Integer.parseInt(matcher.group(2));
                if (scope == null || level >= scope.size() || scope.get(level) == null) {
                    throw unsupported("Repeatable binding index scope");
                }
                matcher.appendReplacement(result, Matcher.quoteReplacement(scope.get(level) + ".currentIndex"));
                continue;
            }
            Node target = nodesById.get(matcher.group(1));
            if (target != null && identifiers.contains(matcher.start())) {
                String replacement = matcher.group(1);
                if (matcher.group().contains("getItemAt")) {
                    if (!"mx.core.Repeater".equals(target.type)) {
                        throw unsupported("Repeatable binding target " + replacement);
                    }
                    replacement += ".currentItem";
                }
                matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
            } else {
                matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group()));
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private String compactBindingPath(GraphTargetItem value) throws IOException {
        if (value instanceof NewArrayAVM2Item) {
            List<String> parts = new ArrayList<>();
            for (GraphTargetItem part : ((NewArrayAVM2Item) value).values) {
                parts.add(string(part));
            }
            return String.join(".", parts);
        }
        return string(value);
    }

    private String bindingDestination(String destination, GraphTargetItem setter) throws IOException, InterruptedException {
        if (!(setter instanceof NewFunctionAVM2Item) || !destination.matches(".*ns[0-9]+::.*")) {
            return destination;
        }
        int method = ((NewFunctionAVM2Item) setter).methodIndex;
        Map<String, String> namespaces = new LinkedHashMap<>();
        String target = null;
        for (GraphTargetItem statement : body(method)) {
            GraphTargetItem item = assignment(statement);
            if (item instanceof SetLocalAVM2Item && "Namespace".equals(constructedType(unwrap(item.value)))) {
                List<GraphTargetItem> args = constructionArguments(unwrap(item.value));
                if (args.size() != 1) {
                    throw unsupported("Binding namespace constructor");
                }
                String uri = string(args.get(0));
                String replacement = "";
                if (!uri.isEmpty()) {
                    String name = null;
                    for (Map.Entry<String, String> namespace : xmlBindingNamespaces.entrySet()) {
                        if (uri.equals(namespace.getValue())) {
                            name = namespace.getKey();
                            break;
                        }
                    }
                    if (name == null) {
                        int suffix = xmlBindingNamespaces.size();
                        do {
                            name = "recoveredXmlNamespace" + (++suffix);
                        } while (ids.contains(name) || xmlBindingNamespaces.containsKey(name)
                                || abc.instance_info.get(classIndex).instance_traits.traits.stream().map(this::traitName).anyMatch(name::equals));
                        xmlBindingNamespaces.put(name, uri);
                    }
                    replacement = name + "::";
                }
                namespaces.put(render(((SetLocalAVM2Item) item).getObject(), method) + "::", replacement);
            } else if (propertyAssignment(item) && register(unwrap(item.value), 1)) {
                if (target != null) {
                    throw unsupported("Multiple XML binding destination assignments");
                }
                String assignment = render(item, method);
                int equals = assignment.lastIndexOf(" = ");
                if (equals >= 0) {
                    target = assignment.substring(0, equals).replaceFirst("^this\\.", "");
                }
            }
        }
        if (target == null || namespaces.isEmpty()) {
            throw unsupported("Unresolved XML binding namespace");
        }
        for (Map.Entry<String, String> namespace : namespaces.entrySet()) {
            target = target.replace(namespace.getKey(), namespace.getValue());
        }
        return target.replaceAll("([A-Za-z_$][A-Za-z0-9_$]*)::@", "@$1::");
    }

    private void readRootStyles(Node root) throws IOException, InterruptedException {
        for (Trait trait : abc.instance_info.get(classIndex).instance_traits.traits) {
            if (trait instanceof TraitMethodGetterSetter && "moduleFactory".equals(traitName(trait))) {
                readDefaultFactory(root, body(((TraitMethodGetterSetter) trait).method_info));
            }
        }
    }

    private void readDefaultFactory(Node root, List<GraphTargetItem> items) throws IOException, InterruptedException {
        for (GraphTargetItem item : items) {
            if (item instanceof IfItem) {
                readDefaultFactory(root, ((IfItem) item).onTrue);
                readDefaultFactory(root, ((IfItem) item).onFalse);
            } else if (propertyAssignment(item) && "defaultFactory".equals(name(propertyName(item)))
                    && item.value instanceof NewFunctionAVM2Item) {
                for (GraphTargetItem style : body(((NewFunctionAVM2Item) item.value).methodIndex)) {
                    if (propertyAssignment(style) && register(propertyObject(style), 0)) {
                        setValue(root, name(propertyName(style)), style.value);
                    } else if (!(style instanceof ReturnVoidAVM2Item)) {
                        throw unsupported("Root style default factory");
                    }
                }
            }
        }
    }

    private String cssValue(GraphTargetItem value, String property) throws IOException {
        value = unwrap(value);
        if (value instanceof GetLexAVM2Item) {
            // CSS Embed values refer to a generated Class field, not directly
            // to the asset class stored in that field by the constructor.
            Set<String> visited = new HashSet<>();
            while (value instanceof GetLexAVM2Item && visited.add(qualified((GetLexAVM2Item) value))) {
                GraphTargetItem initial = null;
                for (Trait trait : abc.instance_info.get(classIndex).instance_traits.traits) {
                    if (trait instanceof TraitSlotConst && traitName(trait).equals(qualified((GetLexAVM2Item) value))
                            && convertData.assignedValues.containsKey(trait)) {
                        initial = unwrap(convertData.assignedValues.get(trait).value);
                        break;
                    }
                }
                if (!(initial instanceof GetLexAVM2Item)) {
                    break;
                }
                value = initial;
            }
            String type = qualified((GetLexAVM2Item) value);
            dependencies.add(type);
            return "ClassReference(\"" + DottedChain.parseNoSuffix(type).toPrintableString(new LinkedHashSet<>(), swf, true) + "\")";
        }
        if (value instanceof NewArrayAVM2Item) {
            List<String> values = new ArrayList<>();
            for (GraphTargetItem entry : ((NewArrayAVM2Item) value).values) {
                values.add(cssValue(entry, property));
            }
            return String.join(", ", values);
        }
        if (value instanceof StringAVM2Item) {
            return "\"" + string(value).replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\a ") + "\"";
        }
        if (value.isCompileTime() && value.getResult() instanceof Number && property.toLowerCase(java.util.Locale.ROOT).contains("color")) {
            return String.format(java.util.Locale.ROOT, "#%06x", ((Number) value.getResult()).intValue() & 0xffffff);
        }
        return literal(value);
    }

    private String cssSelector(String selector) {
        Matcher matcher = Pattern.compile("(?:spark|mx)\\.[A-Za-z0-9_.]+\\.([A-Z][A-Za-z0-9_]*)").matcher(selector);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(result, (matcher.group().startsWith("spark.") ? "s|" : "mx|") + matcher.group(1));
        }
        matcher.appendTail(result);
        matcher = Pattern.compile("(?<![A-Za-z0-9_.])(?:[A-Za-z_][A-Za-z0-9_]*\\.)+[A-Za-z_][A-Za-z0-9_]*").matcher(result);
        StringBuffer qualified = new StringBuffer();
        while (matcher.find()) {
            String type = matcher.group();
            int dot = type.lastIndexOf('.');
            String uri = type.substring(0, dot) + ".*";
            String prefix = cssNamespaces.computeIfAbsent(uri, key -> "c" + cssNamespaces.size());
            matcher.appendReplacement(qualified, Matcher.quoteReplacement(prefix + "|" + type.substring(dot + 1)));
        }
        matcher.appendTail(qualified);
        return qualified.toString();
    }

    private String readStyles() throws IOException {
        String styles = "_" + pack.getPathScriptName() + "_StylesInit";
        TraitMethodGetterSetter method = methods.get(styles);
        if (method == null) {
            return "";
        }
        StringBuilder css = new StringBuilder();
        String selector = null;
        for (GraphTargetItem original : body(method.method_info)) {
            GraphTargetItem item = assignment(original);
            if ((item instanceof SetLocalAVM2Item || item instanceof SetSlotAVM2Item) && unwrap(item.value) instanceof CallPropertyAVM2Item) {
                CallPropertyAVM2Item call = (CallPropertyAVM2Item) unwrap(item.value);
                if ("getStyleDeclaration".equals(name(call.propertyName)) && call.arguments.size() == 1) {
                    selector = string(call.arguments.get(0));
                }
            } else if (item instanceof IfItem) {
                IfItem conditional = (IfItem) item;
                for (GraphTargetItem statement : conditional.onTrue) {
                    if (propertyAssignment(statement) && "factory".equals(name(propertyName(statement)))) {
                        if (selector == null || !(statement.value instanceof NewFunctionAVM2Item)) {
                            throw unsupported("Application CSS styles");
                        }
                        css.append(cssSelector(selector)).append(" {\n");
                        for (GraphTargetItem style : body(((NewFunctionAVM2Item) statement.value).methodIndex)) {
                            if (propertyAssignment(style) && register(propertyObject(style), 0)) {
                                String property = name(propertyName(style));
                                css.append("    ").append(property).append(": ").append(cssValue(style.value, property)).append(";\n");
                            } else if (!(style instanceof ReturnVoidAVM2Item)) {
                                throw unsupported("Application CSS factory");
                            }
                        }
                        css.append("}\n");
                    }
                }
            } else if (item instanceof CallPropertyAVM2Item) {
                CallPropertyAVM2Item call = (CallPropertyAVM2Item) item;
                if (!"initProtoChainRoots".equals(name(call.propertyName))
                        && !"push".equals(name(call.propertyName))) {
                    throw unsupported("Application CSS styles");
                }
            }
        }
        if (css.length() == 0) {
            return "";
        }
        StringBuilder namespaces = new StringBuilder("@namespace s \"library://ns.adobe.com/flex/spark\";\n"
                + "@namespace mx \"library://ns.adobe.com/flex/mx\";\n");
        for (Map.Entry<String, String> entry : cssNamespaces.entrySet()) {
            namespaces.append("@namespace ").append(entry.getValue()).append(" \"").append(entry.getKey()).append("\";\n");
        }
        return namespaces.toString() + css;
    }

    private String event(String method) throws IOException, InterruptedException {
        TraitMethodGetterSetter trait = methods.get(method);
        if (trait == null) {
            throw unsupported("Missing event handler " + method);
        }
        List<GraphTargetItem> items = body(trait.method_info);
        if (items.size() == 1 && items.get(0) instanceof CallPropertyAVM2Item) {
            CallPropertyAVM2Item call = (CallPropertyAVM2Item) items.get(0);
            if (register(call.receiver, 0) && call.arguments.isEmpty()) {
                factories.add(method);
                return name(call.propertyName) + "()";
            }
        }
        String recovered = eventMethods.get(method);
        if (recovered == null) {
            recovered = "recoveredEventHandler" + (eventMethods.size() + 1);
            while (methods.containsKey(recovered)) {
                recovered += "_";
            }
            eventMethods.put(method, recovered);
        }
        return recovered + "(event)";
    }

    private String script(String actionScript, Node root) throws IOException, InterruptedException {
        StringBuilder members = new StringBuilder();
        for (Map.Entry<String, String> data : scriptDataInitializers.entrySet()) {
            members.append("public var ").append(data.getKey()).append(":Object = ").append(data.getValue()).append(";\n");
        }
        for (Map.Entry<String, String> namespace : xmlBindingNamespaces.entrySet()) {
            members.append("private namespace ").append(namespace.getKey()).append(" = \"")
                    .append(com.jpexs.helpers.Helper.escapeActionScriptString(namespace.getValue())).append("\";\n");
        }
        Map<String, String> bindableMembers = recoverBindableMembers();
        Set<String> bindableStorage = new HashSet<>();
        for (String property : bindableMembers.keySet()) {
            for (Trait trait : abc.instance_info.get(classIndex).instance_traits.traits) {
                if (trait instanceof TraitSlotConst && traitName(trait).matches("_-?\\d+" + Pattern.quote(property))) {
                    bindableStorage.add(traitName(trait));
                }
            }
        }
        for (Trait trait : abc.instance_info.get(classIndex).instance_traits.traits) {
            String name = traitName(trait);
            if (hostComponent != null && "hostComponent".equals(name)) {
                continue;
            }
            if (bindableStorage.contains(name)) {
                continue;
            }
            if (bindableMembers.containsKey(name)) {
                if (trait.kindType == Trait.TRAIT_GETTER) {
                    if (!bindableMembers.get(name).contains("[SkinPart(")) {
                        members.append(skinPartMetadata(name));
                    }
                    members.append(bindableMembers.get(name)).append('\n');
                }
                continue;
            }
            if (factories.contains(name) || ids.contains(name) || isIdStorage(name)
                    || bindingField(name)
                    || "__moduleFactoryInitialized".equals(name) || "moduleFactory".equals(name)
                    || "initialize".equals(name) || "skinParts".equals(name)
                    || name.startsWith("_" + pack.getPathScriptName() + "_StylesInit")) {
                continue;
            }
            if (name.matches("_typeRef[0-9]+") && trait instanceof TraitSlotConst
                    && trait.getName(abc).getSimpleNamespaceKind(abc.constants) == Namespace.KIND_PRIVATE) {
                continue;
            }
            if (name.startsWith("_" + pack.getPathScriptName() + "_") && !eventMethods.containsKey(name)) {
                throw unsupported("Unrecognized generated method or property " + name);
            }
            HighlightedTextWriter writer = new HighlightedTextWriter(Configuration.getCodeFormatting(), false);
            trait.toString(new LinkedHashSet<>(), swf.version, swf.getAbcIndex(), pack.getPathPackage(), classTrait,
                    convertData, "", abc, false, ScriptExportMode.AS, pack.scriptIndex, classIndex,
                    writer, fullyQualifiedNames, false, false);
            String text = writer.toString();
            if (eventMethods.containsKey(name)) {
                text = text.replaceFirst("(function\\s+)" + Pattern.quote(name) + "(?=\\s*\\()", "$1" + eventMethods.get(name));
            }
            if (!text.contains("[SkinPart(")) {
                members.append(skinPartMetadata(name));
            }
            members.append(text).append('\n');
        }
        for (Trait trait : abc.class_info.get(classIndex).static_traits.traits) {
            String name = traitName(trait);
            if ("_skinParts".equals(name) || "_watcherSetupUtil".equals(name) || "watcherSetupUtil".equals(name)) {
                continue;
            }
            HighlightedTextWriter writer = new HighlightedTextWriter(Configuration.getCodeFormatting(), false);
            trait.toString(new LinkedHashSet<>(), swf.version, swf.getAbcIndex(), pack.getPathPackage(), classTrait,
                    convertData, "", abc, true, ScriptExportMode.AS, pack.scriptIndex, classIndex,
                    writer, fullyQualifiedNames, false, false);
            members.append(writer.toString()).append('\n');
        }
        StringBuilder referencedCode = new StringBuilder(members);
        List<String> namespaceLines = new ArrayList<>();
        Matcher namespaces = Pattern.compile("(?m)^\\s*use namespace ([^;\\r\\n]+);").matcher(actionScript);
        while (namespaces.find()) {
            if (!"mx_internal".equals(namespaces.group(1))) {
                String directive = namespaces.group().trim();
                namespaceLines.add(directive);
                referencedCode.append('\n').append(directive);
            }
        }
        bindingExpressions(root, referencedCode);
        for (Node declaration : declarations) {
            bindingExpressions(declaration, referencedCode);
        }
        for (Node binding : bindings) {
            referencedCode.append('\n').append(binding.attributes.get("source"));
        }
        Set<String> importLines = new LinkedHashSet<>();
        Matcher imports = Pattern.compile("(?m)^\\s*import ([^;\\r\\n]+);").matcher(actionScript);
        while (imports.find()) {
            String type = imports.group(1);
            String simple = type.substring(type.lastIndexOf('.') + 1);
            if (fullyQualifiedNames.contains(DottedChain.parseWithSuffix(simple))) {
                continue;
            }
            if ("*".equals(simple)) {
                String packageName = type.substring(0, type.length() - 2);
                for (int i = 1; i < abc.constants.getMultinameCount(); i++) {
                    com.jpexs.decompiler.flash.abc.types.Multiname multiname = abc.constants.getMultiname(i);
                    if (multiname == null || multiname.name_index == 0 || multiname.getSimpleNamespaceKind(abc.constants) != Namespace.KIND_PACKAGE) {
                        continue;
                    }
                    String identifier = abc.constants.getString(multiname.name_index);
                    if (multiname.getSimpleNamespaceName(abc.constants).toRawString().equals(packageName)
                            && Pattern.compile("\\b" + Pattern.quote(identifier) + "\\b").matcher(referencedCode).find()) {
                        importLines.add("import " + packageName + "." + identifier + ";");
                    }
                }
            } else if (Pattern.compile("\\b" + Pattern.quote(simple) + "\\b").matcher(referencedCode).find()) {
                importLines.add(imports.group().trim());
            }
        }
        StringBuilder script = new StringBuilder();
        for (String line : importLines) {
            script.append(line).append('\n');
        }
        for (String line : namespaceLines) {
            script.append(line).append('\n');
        }
        script.append('\n').append(members);
        return script.toString().trim();
    }

    private void readMetadata() {
        HighlightedTextWriter writer = new HighlightedTextWriter(Configuration.getCodeFormatting(), false);
        classTrait.getMetaData(new LinkedHashSet<>(), classTrait, convertData, abc, writer);
        metadata = writer.toString().trim();
        for (Map.Entry<String, Map<String, String>> entry : classTrait.getMetaDataTable(new LinkedHashSet<>(), classTrait, convertData, abc)) {
            if ("HostComponent".equals(entry.getKey())) {
                if (!entry.getValue().isEmpty()) {
                    hostComponent = entry.getValue().values().iterator().next();
                }
            }
        }
        // HostComponent is normally stripped by the compiler, but its generated
        // typed backing field is retained and identifies the original host.
        if (hostComponent == null && swf.getAbcIndex().isInstanceOf(abc, classIndex,
                DottedChain.parseNoSuffix("spark.components.supportClasses.Skin"))) {
            for (Trait trait : abc.instance_info.get(classIndex).instance_traits.traits) {
                if (trait instanceof TraitSlotConst && traitName(trait).matches("(?:_-?\\d+)?hostComponent")) {
                    int type = ((TraitSlotConst) trait).type_index;
                    if (type != 0) {
                        hostComponent = abc.constants.getMultiname(type).getNameWithNamespace(new LinkedHashSet<>(), abc, abc.constants, true).toRawString();
                        metadata += (metadata.isEmpty() ? "" : "\n") + "[HostComponent(\"" + hostComponent + "\")]";
                        break;
                    }
                }
            }
        }
        if (hostComponent != null) {
            dependencies.add(hostComponent);
        }
    }

    private String skinPartMetadata(String name) throws IOException {
        for (Trait trait : abc.class_info.get(classIndex).static_traits.traits) {
            if (trait instanceof TraitSlotConst && "_skinParts".equals(traitName(trait)) && convertData.assignedValues.containsKey(trait)) {
                GraphTargetItem value = unwrap(convertData.assignedValues.get(trait).value);
                if (value instanceof NewObjectAVM2Item) {
                    GraphTargetItem required = object(value).get(name);
                    if (required != null && required.isCompileTime() && required.getResult() instanceof Boolean) {
                        return "[SkinPart(required=\"" + required.getResult() + "\")]\n";
                    }
                }
            }
        }
        return "";
    }

    private void bindingExpressions(Node node, StringBuilder code) {
        for (String attribute : node.attributes.values()) {
            if (attribute.startsWith("{") || attribute.startsWith("@{")) {
                code.append('\n').append(attribute);
            }
        }
        for (Node child : node.children) {
            bindingExpressions(child, code);
        }
        for (List<Node> values : node.properties.values()) {
            for (Node child : values) {
                bindingExpressions(child, code);
            }
        }
    }

    private Map<String, String> recoverBindableMembers() throws IOException, InterruptedException {
        Map<String, String> result = new LinkedHashMap<>();
        for (Trait trait : abc.instance_info.get(classIndex).instance_traits.traits) {
            String property = traitName(trait);
            if (!(trait instanceof TraitMethodGetterSetter) || trait.kindType != Trait.TRAIT_GETTER || ids.contains(property)) {
                continue;
            }
            boolean bindable = false;
            for (Map.Entry<String, Map<String, String>> metadata : trait.getMetaDataTable(new LinkedHashSet<>(), classTrait, new ConvertData(), abc)) {
                if ("Bindable".equals(metadata.getKey()) && "propertyChange".equals(metadata.getValue().get("event"))) {
                    bindable = true;
                }
            }
            if (!bindable) {
                continue;
            }
            List<GraphTargetItem> getter = body(((TraitMethodGetterSetter) trait).method_info);
            if (getter.size() != 1 || !(getter.get(0) instanceof ReturnValueAVM2Item)
                    || !(unwrap(getter.get(0).value) instanceof GetPropertyAVM2Item)) {
                continue;
            }
            GetPropertyAVM2Item access = (GetPropertyAVM2Item) unwrap(getter.get(0).value);
            String storage = name(access.propertyName);
            if (!register(access.object, 0) || !storage.matches("_-?\\d+" + Pattern.quote(property))) {
                continue;
            }
            boolean generatedSetter = false;
            TraitSlotConst field = null;
            for (Trait candidate : abc.instance_info.get(classIndex).instance_traits.traits) {
                if (candidate instanceof TraitSlotConst && storage.equals(traitName(candidate))) {
                    field = (TraitSlotConst) candidate;
                }
                if (candidate instanceof TraitMethodGetterSetter && candidate.kindType == Trait.TRAIT_SETTER && property.equals(traitName(candidate))) {
                    StringBuilder setter = new StringBuilder();
                    for (GraphTargetItem item : body(((TraitMethodGetterSetter) candidate).method_info)) {
                        setter.append(render(item, ((TraitMethodGetterSetter) candidate).method_info));
                    }
                    generatedSetter = setter.toString().contains("PropertyChangeEvent.createUpdateEvent(this,\"" + property + "\",")
                            && setter.toString().contains("this." + storage + " = ");
                }
            }
            if (field == null || !generatedSetter) {
                continue;
            }
            HighlightedTextWriter writer = new HighlightedTextWriter(Configuration.getCodeFormatting(), false);
            field.toString(new LinkedHashSet<>(), swf.version, swf.getAbcIndex(), pack.getPathPackage(), classTrait,
                    convertData, "", abc, false, ScriptExportMode.AS, pack.scriptIndex, classIndex,
                    writer, fullyQualifiedNames, false, false);
            int kind = trait.getName(abc).getSimpleNamespaceKind(abc.constants);
            String visibility = kind == Namespace.KIND_PRIVATE ? "private" : kind == Namespace.KIND_PROTECTED ? "protected"
                    : kind == Namespace.KIND_PACKAGE_INTERNAL ? "internal" : "public";
            String declaration = writer.toString().replaceFirst("\\b" + Pattern.quote(storage) + "\\b", Matcher.quoteReplacement(property));
            declaration = declaration.replaceFirst("\\b(?:private|public|protected|internal)\\b", visibility);
            result.put(property, "[Bindable]\n" + declaration);
        }
        return result;
    }

    private boolean isIdStorage(String name) {
        for (String id : ids) {
            if (name.matches("_-?\\d+" + Pattern.quote(id))) {
                return true;
            }
        }
        return false;
    }

    private String uri(String type) throws IOException {
        if (!type.contains(".")) {
            return "http://ns.adobe.com/mxml/2009";
        }
        String alias = COMPONENT_TAGS.getProperty(type, "");
        if (alias.startsWith("s:")) {
            return "library://ns.adobe.com/flex/spark";
        }
        if (alias.startsWith("mx:")) {
            return "library://ns.adobe.com/flex/mx";
        }
        int dot = type.lastIndexOf('.');
        return dot < 0 ? "*" : type.substring(0, dot) + ".*";
    }

    private void collectNamespaces(Node node, Map<String, String> namespaces) throws IOException {
        String uri = uri(node.type);
        if (!namespaces.containsKey(uri)) {
            namespaces.put(uri, "local" + namespaces.size());
        }
        for (Node child : node.children) {
            collectNamespaces(child, namespaces);
        }
        for (List<Node> values : node.properties.values()) {
            for (Node child : values) {
                collectNamespaces(child, namespaces);
            }
        }
    }

    private String tag(Node node, Map<String, String> namespaces) throws IOException {
        if (node.explicitTag != null) {
            return namespaces.get(uri(node.type)) + ":" + node.explicitTag;
        }
        if (COMPONENT_TAGS.containsKey(node.type)) {
            return COMPONENT_TAGS.getProperty(node.type);
        }
        String simple = node.type.substring(node.type.lastIndexOf('.') + 1);
        if (!simple.matches("[A-Za-z_][A-Za-z0-9_]*")) {
            throw unsupported("Invalid XML component name " + simple);
        }
        return namespaces.get(uri(node.type)) + ":" + simple;
    }

    private String xml(String text) throws IOException {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c < 32 && c != '\n' && c != '\r' && c != '\t') {
                throw unsupported("Control character in XML attribute");
            }
        }
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("\r", "&#13;").replace("\n", "&#10;").replace("\t", "&#9;");
    }

    private void writeNode(StringBuilder xml, Node node, Map<String, String> namespaces, int depth, String script) throws IOException {
        String indent = String.join("", Collections.nCopies(depth, "    "));
        String tag = tag(node, namespaces);
        xml.append(indent).append('<').append(tag);
        if (depth == 0) {
            for (Map.Entry<String, String> ns : namespaces.entrySet()) {
                xml.append("\n    xmlns:").append(ns.getValue()).append("=\"").append(xml(ns.getKey())).append('"');
            }
        }
        for (Map.Entry<String, String> attr : node.attributes.entrySet()) {
            if (!attr.getKey().matches("[A-Za-z_][A-Za-z0-9_]*(?:\\.[A-Za-z_][A-Za-z0-9_]*)?")) {
                throw unsupported("Invalid XML property " + attr.getKey());
            }
            xml.append("\n").append(indent).append("    ").append(attr.getKey()).append("=\"").append(xml(attr.getValue())).append('"');
        }
        if (depth > 0 && node.children.isEmpty() && node.properties.isEmpty() && node.text == null && node.inlineXml == null) {
            xml.append("/>\n");
            return;
        }
        if (node.text != null) {
            xml.append('>').append(xml(node.text)).append("</").append(tag).append(">\n");
            return;
        }
        xml.append(">\n");
        if (depth == 0 && !metadata.isEmpty()) {
            xml.append("    <fx:Metadata><![CDATA[\n");
            xml.append(metadata.replace("]]>", "]]]]><![CDATA[>")).append('\n');
            xml.append("    ]]></fx:Metadata>\n");
        }
        if (node.inlineXml != null) {
            for (String line : node.inlineXml.split("\\r?\\n")) {
                xml.append(indent).append("    ").append(line).append('\n');
            }
        }
        if (depth == 0 && !script.isEmpty()) {
            xml.append("\n    <fx:Script>\n        <![CDATA[\n");
            for (String line : script.split("\\r?\\n")) {
                xml.append("            ").append(line.replace("]]>", "]]]]><![CDATA[>")).append('\n');
            }
            xml.append("        ]]>\n    </fx:Script>\n\n");
        }
        if (depth == 0 && !css.isEmpty()) {
            xml.append("    <fx:Style>\n        <![CDATA[\n");
            for (String line : css.split("\\r?\\n")) {
                xml.append("            ").append(line.replace("]]>", "]]]]><![CDATA[>")).append('\n');
            }
            xml.append("        ]]>\n    </fx:Style>\n\n");
        }
        if (depth == 0 && !declarations.isEmpty()) {
            xml.append("    <fx:Declarations>\n");
            for (Node declaration : declarations) {
                writeNode(xml, declaration, namespaces, 2, "");
            }
            xml.append("    </fx:Declarations>\n\n");
        }
        if (depth == 0) {
            for (Node binding : bindings) {
                writeNode(xml, binding, namespaces, 1, "");
            }
        }
        for (Map.Entry<String, List<Node>> property : node.properties.entrySet()) {
            if (!property.getKey().matches("[A-Za-z_][A-Za-z0-9_]*(?:\\.[A-Za-z_][A-Za-z0-9_]*)?")) {
                throw unsupported("Invalid XML property " + property.getKey());
            }
            String propertyTag = namespaces.get(uri(node.type)) + ":" + property.getKey();
            xml.append(indent).append("    <").append(propertyTag).append(">\n");
            for (Node child : property.getValue()) {
                writeNode(xml, child, namespaces, depth + 2, "");
            }
            xml.append(indent).append("    </").append(propertyTag).append(">\n");
        }
        for (Node child : node.children) {
            writeNode(xml, child, namespaces, depth + 1, "");
        }
        xml.append(indent).append("</").append(tag).append(">\n");
    }
}
