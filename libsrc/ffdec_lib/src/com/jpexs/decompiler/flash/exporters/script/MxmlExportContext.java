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
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.InitPropertyIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.SetPropertyIns;
import com.jpexs.decompiler.flash.abc.types.ConvertData;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitClass;
import com.jpexs.decompiler.flash.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.exporters.settings.ScriptExportSettings;
import com.jpexs.decompiler.flash.tags.SoundStreamBlockTag;
import com.jpexs.decompiler.flash.tags.StartSound2Tag;
import com.jpexs.decompiler.flash.tags.StartSoundTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.VideoFrameTag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.helpers.Reference;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Shared dependency and output state for an MXML source export.
 *
 * @author JPEXS
 */
final class MxmlExportContext {

    private static final Set<String> ROOT_TYPES = new HashSet<>(Arrays.asList(
            "spark.components.Application", "mx.core.Application",
            "spark.components.WindowedApplication", "mx.core.WindowedApplication",
            "spark.modules.Module", "mx.modules.Module"));

    final Set<String> exportedClasses = new HashSet<>();
    final Set<String> activeClasses = new HashSet<>();
    final Set<String> inlineClasses = new HashSet<>();
    final Map<File, String> pendingDocuments = new LinkedHashMap<>();
    final Map<File, byte[]> pendingAssets = new LinkedHashMap<>();
    final Map<String, ScriptPack> packs = new LinkedHashMap<>();
    final Set<String> ignoredClasses = new HashSet<>();
    final Set<String> resourceBundles = new LinkedHashSet<>();
    private final Set<String> ignoredNamespaces = new LinkedHashSet<>();
    private SWF swf;
    String mainClass;

    String detectRootType(SWF source) {
        if (source == null || !source.isAS3()) {
            return null;
        }
        for (Tag tag : source.getTags()) {
            if (tag instanceof PlaceObjectTypeTag || tag instanceof SoundStreamBlockTag || tag instanceof VideoFrameTag
                    || tag instanceof StartSoundTag || tag instanceof StartSound2Tag) {
                return null;
            }
        }
        for (ScriptPack candidate : source.getAS3Packs()) {
            packs.putIfAbsent(candidate.getClassPath().toRawString(), candidate);
        }
        mainClass = source.getDocumentClass();
        ScriptPack document = packs.get(mainClass);
        if (document == null || !(document.getPublicTrait() instanceof TraitClass)) {
            return null;
        }
        int cls = ((TraitClass) document.getPublicTrait()).class_info;
        int parent = document.abc.instance_info.get(cls).super_index;
        if (parent == 0) {
            return null;
        }
        String base = document.abc.constants.getMultiname(parent)
                .getNameWithNamespace(new LinkedHashSet<>(), document.abc, document.abc.constants, true).toRawString();
        if ("mx.managers.SystemManager".equals(base) || "mx.managers.WindowedSystemManager".equals(base)
                || "mx.core.FlexModuleFactory".equals(base)) {
            mainClass = null;
            for (Trait trait : document.abc.instance_info.get(cls).instance_traits.traits) {
                if (!(trait instanceof TraitMethodGetterSetter)
                        || !"info".equals(document.abc.constants.getString(trait.getName(document.abc).name_index))) {
                    continue;
                }
                MethodBody body = document.abc.findBody(((TraitMethodGetterSetter) trait).method_info);
                if (body == null) {
                    continue;
                }
                List<AVM2Instruction> code = body.getCode().code;
                for (int i = 0; i + 1 < code.size(); i++) {
                    AVM2Instruction key = code.get(i);
                    int next = i + 1;
                    while (next < code.size() && (code.get(next).definition.instructionCode == AVM2Instructions.Debug
                            || code.get(next).definition.instructionCode == AVM2Instructions.DebugLine
                            || code.get(next).definition.instructionCode == AVM2Instructions.DebugFile
                            || code.get(next).definition.instructionCode == AVM2Instructions.Nop)) {
                        next++;
                    }
                    if (next >= code.size()) {
                        continue;
                    }
                    AVM2Instruction value = code.get(next);
                    if (key.definition.instructionCode == AVM2Instructions.PushString
                            && "mainClassName".equals(document.abc.constants.getString(key.operands[0]))
                            && value.definition.instructionCode == AVM2Instructions.PushString) {
                        mainClass = document.abc.constants.getString(value.operands[0]);
                    }
                }
            }
        }
        ScriptPack main = packs.get(mainClass);
        if (main == null || !(main.getPublicTrait() instanceof TraitClass)) {
            return null;
        }
        int mainIndex = ((TraitClass) main.getPublicTrait()).class_info;
        MethodBody constructor = main.abc.findBody(main.abc.instance_info.get(mainIndex).iinit_index);
        if (constructor == null || constructor.getCode().code.stream().noneMatch(instruction ->
                instruction.definition.instructionCode == AVM2Instructions.ConstructSuper && instruction.operands[0] == 0)) {
            return null;
        }
        return supportedRootType(main);
    }

    void reset(SWF source) {
        swf = source;
        exportedClasses.clear();
        activeClasses.clear();
        inlineClasses.clear();
        pendingDocuments.clear();
        pendingAssets.clear();
        packs.clear();
        ignoredClasses.clear();
        resourceBundles.clear();
        ignoredNamespaces.clear();
        ignoredNamespaces.addAll(Arrays.asList("flash", "flashx", "mx", "spark", "__AS3__", "avmplus", "org.apache.flex", "org.osmf"));
        List<String> classes = new ArrayList<>();
        List<String> namespaces = new ArrayList<>();
        mainClass = swf.getFlexMainClass(classes, namespaces);
        if (mainClass == null) {
            mainClass = swf.getDocumentClass();
        }
        ignoredClasses.addAll(classes);
        ignoredNamespaces.addAll(namespaces);
        for (ScriptPack pack : swf.getAS3Packs()) {
            packs.putIfAbsent(pack.getClassPath().toRawString(), pack);
        }
    }

    ConvertData convertData(ScriptPack pack, File directory) {
        ConvertData result = new ConvertData();
        File source = pack.getExportFile(directory.getAbsolutePath(), ".as");
        result.assetsDir = source.getParentFile().toPath().relativize(new File(directory.getAbsoluteFile(), "_assets").toPath())
                .toString().replace('\\', '/') + "/";
        result.exportEmbed = true;
        return result;
    }

    void stageAssets(File directory) throws IOException, InterruptedException {
        List<ScriptPack> assets = new ArrayList<>();
        for (String name : new TreeSet<>(exportedClasses)) {
            if (swf.getCharacterByClass(name) != null) {
                assets.add(packs.get(name));
            }
        }
        if (assets.isEmpty()) {
            return;
        }
        Path staging = Files.createTempDirectory("ffdec-mxml-assets-");
        try {
            new AS3ScriptExporter().exportEmbeddedAssets(null, staging.toString(), swf, assets,
                    new ScriptExportSettings(ScriptExportMode.AS, false, false, true, false), null);
            try (Stream<Path> paths = Files.walk(staging)) {
                for (Path path : paths.filter(Files::isRegularFile).collect(Collectors.toList())) {
                    pendingAssets.put(new File(directory, staging.relativize(path).toString()), Files.readAllBytes(path));
                }
            }
        } finally {
            try (Stream<Path> paths = Files.walk(staging)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).collect(Collectors.toList())) {
                    Files.delete(path);
                }
            }
        }
    }

    void addDocument(String name, boolean inline) {
        if (inline) {
            inlineClasses.add(name);
        }
        String simple = name.substring(name.lastIndexOf('.') + 1);
        String prefix = name.contains(".") ? name.substring(0, name.lastIndexOf('.') + 1) : "";
        ignoredClasses.add(prefix + "_" + simple + "WatcherSetupUtil");
        ignoredClasses.add("_" + name.replace('.', '_') + "WatcherSetupUtil");
    }

    boolean shouldExport(String name) {
        if (exportedClasses.contains(name) || activeClasses.contains(name) || inlineClasses.contains(name)
                || ignoredClasses.contains(name) || !packs.containsKey(name)) {
            return false;
        }
        for (String namespace : ignoredNamespaces) {
            if (name.startsWith(namespace + ".")) {
                return false;
            }
        }
        return true;
    }

    boolean isMxml(ScriptPack pack) {
        if (!(pack.getPublicTrait() instanceof TraitClass)) {
            return false;
        }
        int index = ((TraitClass) pack.getPublicTrait()).class_info;
        boolean moduleFactory = false;
        boolean moduleFactorySetter = false;
        boolean initialize = false;
        boolean compact = false;
        for (Trait trait : pack.abc.instance_info.get(index).instance_traits.traits) {
            String name = pack.abc.constants.getString(trait.getName(pack.abc).name_index);
            if ("__moduleFactoryInitialized".equals(name)) {
                moduleFactory = true;
            } else if ("moduleFactory".equals(name) && trait.kindType == Trait.TRAIT_SETTER) {
                moduleFactorySetter = true;
            } else if ("initialize".equals(name) && trait.kindType == Trait.TRAIT_METHOD) {
                initialize = true;
            } else if (("MXMLDescriptor".equals(name) || "MXMLProperties".equals(name)) && trait.kindType == Trait.TRAIT_GETTER) {
                compact = true;
            }
        }
        MethodBody constructor = pack.abc.findBody(pack.abc.instance_info.get(index).iinit_index);
        if (constructor != null && compact) {
            return true;
        }
        if (!moduleFactory || constructor == null) {
            return false;
        }
        for (AVM2Instruction instruction : constructor.getCode().code) {
            if (instruction.definition instanceof InitPropertyIns || instruction.definition instanceof SetPropertyIns) {
                Multiname name = pack.abc.constants.getMultiname(instruction.operands[0]);
                if (name != null && "_document".equals(pack.abc.constants.getString(name.name_index))) {
                    return true;
                }
            }
        }
        return moduleFactorySetter && initialize;
    }

    boolean isSupportedRoot(ScriptPack pack) {
        return supportedRootType(pack) != null;
    }

    private String supportedRootType(ScriptPack pack) {
        String name = pack.getClassPath().toRawString();
        Set<String> visited = new HashSet<>();
        while (visited.add(name)) {
            if (ROOT_TYPES.contains(name)) {
                return name;
            }
            ScriptPack ancestor = packs.get(name);
            if (ancestor == null || !(ancestor.getPublicTrait() instanceof TraitClass)) {
                return null;
            }
            int index = ((TraitClass) ancestor.getPublicTrait()).class_info;
            int parent = ancestor.abc.instance_info.get(index).super_index;
            if (parent == 0 || ancestor.abc.instance_info.get(index).isInterface()) {
                return null;
            }
            name = ancestor.abc.constants.getMultiname(parent)
                    .getNameWithNamespace(new LinkedHashSet<>(), ancestor.abc, ancestor.abc.constants, true).toRawString();
        }
        return null;
    }

    Set<String> dependencies(ScriptPack pack) throws InterruptedException {
        List<Dependency> found = new ArrayList<>();
        Set<String> deobfuscations = new LinkedHashSet<>();
        List<DottedChain> fullyQualifiedNames = new ArrayList<>();
        List<String> uses = new ArrayList<>();
        Reference<Integer> numberContext = new Reference<>(null);
        for (int index : pack.traitIndices) {
            Trait trait = pack.abc.script_info.get(pack.scriptIndex).traits.traits.get(index);
            trait.getDependencies(deobfuscations, swf.getAbcIndex(), pack.scriptIndex, -1, false, null,
                    pack.abc, found, DottedChain.TOPLEVEL, fullyQualifiedNames, uses, numberContext);
        }
        DependencyParser.parseDependenciesFromNewFunctionsInMethodInfo(deobfuscations, swf.getAbcIndex(), null,
                pack.scriptIndex, -1, true, null, pack.abc, pack.abc.script_info.get(pack.scriptIndex).init_index,
                found, DottedChain.TOPLEVEL, fullyQualifiedNames, uses, numberContext);
        Set<String> result = new LinkedHashSet<>();
        for (Dependency dependency : found) {
            if (!"*".equals(dependency.getId().getLast())) {
                result.add(dependency.getId().toRawString());
            }
        }
        return result;
    }
}
