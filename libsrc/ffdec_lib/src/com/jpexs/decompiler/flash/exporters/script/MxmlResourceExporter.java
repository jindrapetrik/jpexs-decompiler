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
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.abc.avm2.model.CallPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ConstructSuperAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.FullMultinameAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.GetLexAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.GetPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NameValuePair;
import com.jpexs.decompiler.flash.abc.avm2.model.NewArrayAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NewFunctionAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NewObjectAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ReturnValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.StringAVM2Item;
import com.jpexs.decompiler.flash.abc.types.ConvertData;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitClass;
import com.jpexs.decompiler.flash.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.graph.AbstractGraphTargetVisitor;
import com.jpexs.decompiler.graph.GraphTargetItem;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Recovers the resource bundles used by exported user code.
 *
 * @author JPEXS
 */
final class MxmlResourceExporter {

    private static final Set<String> SDK_BUNDLES = new HashSet<>(Arrays.asList(
            "aircontrols", "aircore", "binding", "collections", "components", "containers", "controls", "core", "effects",
            "formatters", "layout", "logging", "messaging", "rpc", "skins", "states", "styles", "utils", "validators",
            "charts", "datamanagement", "SharedResources", "sparkEffects", "osmf", "textLayout", "mobilecomponents"));

    static void collect(ScriptPack pack, Set<String> bundles) {
        Set<Integer> methods = new LinkedHashSet<>();
        for (int index : pack.traitIndices) {
            Trait trait = pack.abc.script_info.get(pack.scriptIndex).traits.traits.get(index);
            if (trait instanceof TraitClass) {
                int cls = ((TraitClass) trait).class_info;
                methods.add(pack.abc.instance_info.get(cls).iinit_index);
                methods.add(pack.abc.class_info.get(cls).cinit_index);
                addMethods(pack.abc.instance_info.get(cls).instance_traits.traits, methods);
                addMethods(pack.abc.class_info.get(cls).static_traits.traits, methods);
            } else if (trait instanceof TraitMethodGetterSetter) {
                methods.add(((TraitMethodGetterSetter) trait).method_info);
            }
        }
        Set<Integer> seen = new HashSet<>();
        while (!methods.isEmpty()) {
            int method = methods.iterator().next();
            methods.remove(method);
            if (!seen.add(method)) {
                continue;
            }
            MethodBody body = pack.abc.findBody(method);
            if (body == null || body.convertedItems == null) {
                continue;
            }
            for (GraphTargetItem item : body.convertedItems) {
                item.visitRecursively(new AbstractGraphTargetVisitor() {
                    @Override
                    public boolean visit(GraphTargetItem value) {
                        if (value instanceof NewFunctionAVM2Item) {
                            methods.add(((NewFunctionAVM2Item) value).methodIndex);
                        }
                        if (value instanceof CallPropertyAVM2Item) {
                            CallPropertyAVM2Item call = (CallPropertyAVM2Item) value;
                            String name = property(call.propertyName);
                            if (name.matches("get(?:String|StringArray|Boolean|Number|Int|Uint|Class|Object)")
                                    && call.arguments.size() >= 2 && call.arguments.get(0) instanceof StringAVM2Item
                                    && resourceManager(call.receiver, pack)) {
                                bundles.add(((StringAVM2Item) call.arguments.get(0)).getValue());
                            }
                        }
                        return true;
                    }
                });
            }
        }
    }

    private static void addMethods(List<Trait> traits, Set<Integer> methods) {
        for (Trait trait : traits) {
            if (trait instanceof TraitMethodGetterSetter) {
                methods.add(((TraitMethodGetterSetter) trait).method_info);
            }
        }
    }

    private static String property(GraphTargetItem value) {
        return value instanceof FullMultinameAVM2Item ? ((FullMultinameAVM2Item) value).resolvedMultinameName : "";
    }

    private static boolean resourceManager(GraphTargetItem value, ScriptPack pack) {
        if (value instanceof GetLexAVM2Item) {
            return "resourceManager".equals(pack.abc.constants.getString(((GetLexAVM2Item) value).propertyName.name_index));
        }
        if (value instanceof GetPropertyAVM2Item) {
            return "resourceManager".equals(property(((GetPropertyAVM2Item) value).propertyName));
        }
        if (value instanceof CallPropertyAVM2Item) {
            CallPropertyAVM2Item call = (CallPropertyAVM2Item) value;
            return "getInstance".equals(property(call.propertyName)) && call.receiver instanceof GetLexAVM2Item
                    && "mx.resources.ResourceManager".equals(((GetLexAVM2Item) call.receiver).getRawPropertyName());
        }
        return false;
    }

    Set<String> stage(MxmlExportContext context, SWF swf, File directory) throws IOException, InterruptedException {
        Set<String> dependencies = new LinkedHashSet<>();
        boolean userBundles = context.packs.keySet().stream().anyMatch(name -> name.contains("$") && name.endsWith("_properties")
                && !SDK_BUNDLES.contains(name.substring(name.indexOf('$') + 1, name.length() - "_properties".length())));
        if (context.resourceBundles.isEmpty() && !userBundles) {
            return dependencies;
        }
        Set<String> found = new HashSet<>();
        Set<String> locales = new LinkedHashSet<>();
        ScriptPack bootstrap = context.packs.get(swf.getDocumentClass());
        if (bootstrap != null && bootstrap.getPublicTrait() instanceof TraitClass) {
            int bootstrapClass = ((TraitClass) bootstrap.getPublicTrait()).class_info;
            for (Trait trait : bootstrap.abc.instance_info.get(bootstrapClass).instance_traits.traits) {
                if (!(trait instanceof TraitMethodGetterSetter) || !"info".equals(bootstrap.abc.constants.getString(trait.getName(bootstrap.abc).name_index))) {
                    continue;
                }
                MethodBody body = bootstrap.abc.findBody(((TraitMethodGetterSetter) trait).method_info);
                if (body == null || body.convertedItems == null) {
                    continue;
                }
                for (GraphTargetItem item : body.convertedItems) {
                    item.visitRecursively(new AbstractGraphTargetVisitor() {
                        @Override
                        public boolean visit(GraphTargetItem value) {
                            if (value instanceof NewObjectAVM2Item) {
                                for (NameValuePair pair : ((NewObjectAVM2Item) value).pairs) {
                                    if (pair.name instanceof StringAVM2Item && "compiledLocales".equals(((StringAVM2Item) pair.name).getValue())
                                            && pair.value instanceof NewArrayAVM2Item) {
                                        for (GraphTargetItem locale : ((NewArrayAVM2Item) pair.value).values) {
                                            if (locale instanceof StringAVM2Item) {
                                                locales.add(((StringAVM2Item) locale).getValue());
                                            }
                                        }
                                    }
                                }
                            }
                            return true;
                        }
                    });
                }
            }
        }
        for (ScriptPack pack : context.packs.values()) {
            if (!(pack.getPublicTrait() instanceof TraitClass)) {
                continue;
            }
            int index = ((TraitClass) pack.getPublicTrait()).class_info;
            int parent = pack.abc.instance_info.get(index).super_index;
            if (parent == 0 || !"mx.resources.ResourceBundle".equals(pack.abc.constants.getMultiname(parent)
                    .getNameWithNamespace(new LinkedHashSet<>(), pack.abc, pack.abc.constants, true).toRawString())) {
                continue;
            }
            ConvertData data = context.convertData(pack, directory);
            pack.toSource(swf.getAbcIndex(), new HighlightedTextWriter(Configuration.getCodeFormatting(), false),
                    pack.abc.script_info.get(pack.scriptIndex).traits.traits, data, ScriptExportMode.AS, false, false, false);
            String locale = null;
            String bundle = null;
            MethodBody constructor = pack.abc.findBody(pack.abc.instance_info.get(index).iinit_index);
            if (constructor == null || constructor.convertedItems == null) {
                throw new IOException("Unsupported resource constructor: " + pack.getClassPath());
            }
            for (GraphTargetItem item : constructor.convertedItems) {
                if (item instanceof ConstructSuperAVM2Item) {
                    List<GraphTargetItem> args = ((ConstructSuperAVM2Item) item).args;
                    if (args.size() == 2 && args.get(0) instanceof StringAVM2Item && args.get(1) instanceof StringAVM2Item) {
                        locale = ((StringAVM2Item) args.get(0)).getValue();
                        bundle = ((StringAVM2Item) args.get(1)).getValue();
                    }
                }
            }
            String packName = pack.getClassPath().toRawString();
            if (bundle != null && packName.contains("$") && packName.endsWith("_properties") && !SDK_BUNDLES.contains(bundle)) {
                context.resourceBundles.add(bundle);
            }
            if (!context.resourceBundles.contains(bundle)) {
                continue;
            }
            if (locale == null || !locale.matches("[A-Za-z0-9_]+") || !bundle.matches("[A-Za-z_$][A-Za-z0-9_$]*(?:\\.[A-Za-z_$][A-Za-z0-9_$]*)*")) {
                throw new IOException("Unsupported resource bundle name: " + bundle);
            }
            StringBuilder text = new StringBuilder();
            boolean content = false;
            for (Trait trait : pack.abc.instance_info.get(index).instance_traits.traits) {
                if (!(trait instanceof TraitMethodGetterSetter) || !"getContent".equals(pack.abc.constants.getString(trait.getName(pack.abc).name_index))) {
                    continue;
                }
                MethodBody factory = pack.abc.findBody(((TraitMethodGetterSetter) trait).method_info);
                if (factory == null || factory.convertedItems == null) {
                    throw new IOException("Unsupported resource content factory: " + bundle);
                }
                for (GraphTargetItem item : factory.convertedItems) {
                    if (item instanceof ReturnValueAVM2Item && MxmlExporter.unwrap(item.value) instanceof NewObjectAVM2Item) {
                        content = true;
                        for (NameValuePair pair : ((NewObjectAVM2Item) MxmlExporter.unwrap(item.value)).pairs) {
                            if (!(pair.name instanceof StringAVM2Item)) {
                                throw new IOException("Unsupported resource value in " + bundle);
                            }
                            text.append(escape(((StringAVM2Item) pair.name).getValue())).append('=')
                                    .append(resourceValue(pair.value, data, pack, new HashSet<>())).append('\n');
                        }
                    }
                }
            }
            if (!content) {
                throw new IOException("Unsupported resource content factory: " + bundle);
            }
            locales.add(locale);
            found.add(bundle);
            context.pendingDocuments.put(new File(directory, "locale/" + locale + "/" + bundle.replace('.', '/') + ".properties"), text.toString());
            dependencies.addAll(context.dependencies(pack));
        }
        if (!found.containsAll(context.resourceBundles)) {
            Set<String> missing = new LinkedHashSet<>(context.resourceBundles);
            missing.removeAll(found);
            throw new IOException("Resource bundles are not present in the SWF: " + missing);
        }
        ScriptPack main = context.packs.get(context.mainClass);
        File source = main.getExportFile(directory.getAbsolutePath(), ".mxml");
        String root = source.getParentFile().toPath().relativize(directory.getAbsoluteFile().toPath()).toString().replace('\\', '/');
        root = root.isEmpty() ? "." : root;
        StringBuilder config = new StringBuilder("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<flex-config>\n    <compiler>\n        <locale>\n");
        for (String locale : locales) {
            config.append("            <locale-element>").append(locale).append("</locale-element>\n");
        }
        config.append("        </locale>\n        <source-path append=\"true\">\n            <path-element>").append(root)
                .append("/locale/{locale}</path-element>\n            <path-element>").append(root)
                .append("</path-element>\n        </source-path>\n    </compiler>\n</flex-config>\n");
        context.pendingDocuments.put(new File(source.getParentFile(), main.getPathScriptName() + "-config.xml"), config.toString());
        return dependencies;
    }

    private String resourceValue(GraphTargetItem value, ConvertData data, ScriptPack pack, Set<String> seen) throws IOException {
        value = MxmlExporter.unwrap(value);
        if (value instanceof StringAVM2Item) {
            return escape(((StringAVM2Item) value).getValue());
        }
        String name = null;
        if (value instanceof GetLexAVM2Item) {
            name = ((GetLexAVM2Item) value).getRawPropertyName();
        } else if (value instanceof GetPropertyAVM2Item) {
            name = property(((GetPropertyAVM2Item) value).propertyName);
        }
        if (name != null && seen.add(name)) {
            for (Map.Entry<com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst, com.jpexs.decompiler.flash.abc.types.AssignedValue> entry : data.assignedValues.entrySet()) {
                if (name.equals(pack.abc.constants.getString(entry.getKey().getName(pack.abc).name_index))) {
                    return resourceValue(entry.getValue().value, data, pack, seen);
                }
            }
            return "ClassReference(\"" + com.jpexs.decompiler.graph.DottedChain.parseNoSuffix(name)
                    .toPrintableString(new LinkedHashSet<>(), pack.abc.getSwf(), true) + "\")";
        }
        throw new IOException("Unsupported resource value " + value.getClass().getSimpleName());
    }

    private static String escape(String value) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\n':
                    result.append("\\n");
                    break;
                case '\r':
                    result.append("\\r");
                    break;
                case '\t':
                    result.append("\\t");
                    break;
                case '\\':
                case ' ':
                case '=':
                case ':':
                case '#':
                case '!':
                    result.append('\\').append(c);
                    break;
                default:
                    if (c < 32 || c > 126) {
                        result.append(String.format(java.util.Locale.ROOT, "\\u%04x", (int) c));
                    } else {
                        result.append(c);
                    }
            }
        }
        return result.toString();
    }
}
