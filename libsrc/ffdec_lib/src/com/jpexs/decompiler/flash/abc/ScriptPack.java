/*
 *  Copyright (C) 2010-2021 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.abc;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.avm2.ConvertException;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import com.jpexs.decompiler.flash.abc.avm2.instructions.debug.DebugFileIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.debug.DebugIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.debug.DebugLineIns;
import com.jpexs.decompiler.flash.abc.types.ConvertData;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.ScriptInfo;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitClass;
import com.jpexs.decompiler.flash.abc.types.traits.TraitFunction;
import com.jpexs.decompiler.flash.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.exporters.settings.ScriptExportSettings;
import com.jpexs.decompiler.flash.helpers.FileTextWriter;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.HighlightedText;
import com.jpexs.decompiler.flash.helpers.NulWriter;
import com.jpexs.decompiler.flash.helpers.hilight.Highlighting;
import com.jpexs.decompiler.flash.search.MethodId;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.treeitems.AS3ClassTreeItem;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.ScopeStack;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.Path;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class ScriptPack extends AS3ClassTreeItem {

    private static final Logger logger = Logger.getLogger(ScriptPack.class.getName());

    public final ABC abc;

    public List<ABC> allABCs;

    public final int scriptIndex;

    public final List<Integer> traitIndices;

    private final ClassPath path;

    public boolean isSimple = false;

    public boolean scriptInitializerIsEmpty = false;

    @Override
    public SWF getSwf() {
        return abc.getSwf();
    }

    public ClassPath getClassPath() {
        return path;
    }

    public ScriptPack(ClassPath path, ABC abc, List<ABC> allAbcs, int scriptIndex, List<Integer> traitIndices) {
        super(path.className, path.namespaceSuffix, path);
        this.abc = abc;
        this.scriptIndex = scriptIndex;
        this.traitIndices = traitIndices;
        this.path = path;
        this.allABCs = allAbcs;
    }

    public DottedChain getPathPackage() {
        DottedChain packageName = DottedChain.TOPLEVEL;
        for (int t : traitIndices) {
            Multiname name = abc.script_info.get(scriptIndex).traits.traits.get(t).getName(abc);
            Namespace ns = name.getNamespace(abc.constants);
            if ((ns.kind == Namespace.KIND_PACKAGE) || (ns.kind == Namespace.KIND_PACKAGE_INTERNAL)) {
                packageName = ns.getName(abc.constants); // assume not null
            }
        }
        return packageName;
    }

    public String getPathScriptName() {
        String scriptName = "";
        for (int t : traitIndices) {
            Multiname name = abc.script_info.get(scriptIndex).traits.traits.get(t).getName(abc);
            Namespace ns = name.getNamespace(abc.constants);
            if ((ns.kind == Namespace.KIND_PACKAGE) || (ns.kind == Namespace.KIND_PACKAGE_INTERNAL)) {
                scriptName = name.getName(abc.constants, null, false, true);
            }
        }
        return scriptName;
    }

    public File getExportFile(String directory, String extension) {

        String scriptName = getPathScriptName();
        DottedChain packageName = getPathPackage();
        File outDir = new File(directory + File.separatorChar + packageName.toFilePath());
        String fileName = outDir.toString() + File.separator + Helper.makeFileName(scriptName) + extension;
        return new File(fileName);
    }

    public File getExportFile(String directory, ScriptExportSettings exportSettings) {
        if (exportSettings.singleFile) {
            return null;
        }

        return getExportFile(directory, exportSettings.getFileExtension());
    }

    /*public String getPath() {
     String packageName = "";
     String scriptName = "";
     for (int t : traitIndices) {
     Multiname name = abc.script_info[scriptIndex].traits.traits.get(t).getName(abc);
     Namespace ns = name.getNamespace(abc.constants);
     if ((ns.kind == Namespace.KIND_PACKAGE) || (ns.kind == Namespace.KIND_PACKAGE_INTERNAL)) {
     packageName = ns.getName(abc.constants);
     scriptName = name.getName(abc.constants, new ArrayList<>());
     }
     }
     return packageName.equals("") ? scriptName : packageName + "." + scriptName;
     }*/
    public void convert(final NulWriter writer, final List<Trait> traits, final ConvertData convertData, final ScriptExportMode exportMode, final boolean parallel) throws InterruptedException {

        int sinit_index = abc.script_info.get(scriptIndex).init_index;
        int sinit_bodyIndex = abc.findBodyIndex(sinit_index);
        if (sinit_bodyIndex != -1) {
            List<Traits> ts = new ArrayList<>();
            //initialize all classes traits
            for (Trait t : traits) {
                if (t instanceof TraitClass) {
                    ts.add(abc.class_info.get(((TraitClass) t).class_info).static_traits);
                }
            }
            ts.add(abc.script_info.get(scriptIndex).traits);
            writer.mark();
            abc.bodies.get(sinit_bodyIndex).convert(convertData, path +/*packageName +*/ "/.scriptinitializer", exportMode, true, sinit_index, scriptIndex, -1, abc, null, new ScopeStack(), GraphTextWriter.TRAIT_SCRIPT_INITIALIZER, writer, new ArrayList<>(), ts, true, new HashSet<>());
            scriptInitializerIsEmpty = !writer.getMark();

        }
        for (int t : traitIndices) {
            Trait trait = traits.get(t);
            Multiname name = trait.getName(abc);
            Namespace ns = name.getNamespace(abc.constants);
            if ((ns.kind == Namespace.KIND_PACKAGE) || (ns.kind == Namespace.KIND_PACKAGE_INTERNAL)) {
                trait.convertPackaged(null, convertData, "", abc, false, exportMode, scriptIndex, -1, writer, new ArrayList<>(), parallel);
            } else {
                trait.convert(null, convertData, "", abc, false, exportMode, scriptIndex, -1, writer, new ArrayList<>(), parallel);
            }
        }
    }

    private void appendTo(GraphTextWriter writer, List<Trait> traits, ConvertData convertData, ScriptExportMode exportMode, boolean parallel) throws InterruptedException {
        boolean first = true;
        //script initializer
        int script_init = abc.script_info.get(scriptIndex).init_index;
        int bodyIndex = abc.findBodyIndex(script_init);
        if (bodyIndex != -1 && Configuration.enableScriptInitializerDisplay.get()) {
            //Note: There must be trait/method highlight even if the initializer is empty to TraitList in GUI to work correctly
            //TODO: handle this better in GUI(?)
            writer.startTrait(GraphTextWriter.TRAIT_SCRIPT_INITIALIZER);
            writer.startMethod(script_init);
            if (exportMode != ScriptExportMode.AS_METHOD_STUBS) {
                if (!scriptInitializerIsEmpty) {
                    writer.startBlock();
                    abc.bodies.get(bodyIndex).toString(path +/*packageName +*/ "/.scriptinitializer", exportMode, abc, null, writer, new ArrayList<>(), new HashSet<>());
                    writer.endBlock();
                } else {
                    writer.append(" ");
                }
            }
            writer.endMethod();
            writer.endTrait();
            if (!scriptInitializerIsEmpty) {
                writer.newLine();
            }
            first = false;
        } else {
            //"/*classInitializer*/";
        }

        for (int t : traitIndices) {
            if (!first) {
                writer.newLine();
            }

            Trait trait = traits.get(t);
            Multiname name = trait.getName(abc);
            Namespace ns = name.getNamespace(abc.constants);
            if ((ns.kind == Namespace.KIND_PACKAGE) || (ns.kind == Namespace.KIND_PACKAGE_INTERNAL)) {
                trait.toStringPackaged(null, convertData, "", abc, false, exportMode, scriptIndex, -1, writer, new ArrayList<>(), parallel);
            } else {
                trait.toString(null, convertData, "", abc, false, exportMode, scriptIndex, -1, writer, new ArrayList<>(), parallel);
            }

            first = false;
        }
    }

    public void toSource(GraphTextWriter writer, final List<Trait> traits, final ConvertData convertData, final ScriptExportMode exportMode, final boolean parallel) throws InterruptedException {
        writer.suspendMeasure();
        int timeout = Configuration.decompilationTimeoutFile.get();
        try {
            CancellableWorker.call(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    convert(new NulWriter(), traits, convertData, exportMode, parallel);
                    return null;
                }
            }, timeout, TimeUnit.SECONDS);
        } catch (TimeoutException ex) {
            writer.continueMeasure();
            logger.log(Level.SEVERE, "Decompilation timeout", ex);
            Helper.appendTimeoutCommentAs3(writer, timeout, 0);
            return;
        } catch (CancellationException ex) {
            throw new InterruptedException();
        } catch (ExecutionException ex) {
            writer.continueMeasure();
            Exception convertException = ex;
            Throwable cause = ex.getCause();
            if (cause instanceof Exception) {
                convertException = (Exception) cause;
            }

            if (convertException instanceof CancellationException) {
                throw new InterruptedException();
            }
            if (convertException instanceof InterruptedException) {
                throw (InterruptedException) convertException;
            }
            logger.log(Level.SEVERE, "Decompilation error", convertException);
            Helper.appendErrorComment(writer, convertException);
            return;
        }
        writer.continueMeasure();

        appendTo(writer, traits, convertData, exportMode, parallel);
    }

    public File export(File file, ScriptExportSettings exportSettings, boolean parallel) throws IOException, InterruptedException {
        if (!exportSettings.singleFile) {
            if (file.exists() && !Configuration.overwriteExistingFiles.get()) {
                return file;
            }
        }

        if (file != null) {
            Path.createDirectorySafe(file.getParentFile());
        }

        try (FileTextWriter writer = exportSettings.singleFile ? null : new FileTextWriter(Configuration.getCodeFormatting(), new FileOutputStream(file))) {
            FileTextWriter writer2 = exportSettings.singleFile ? exportSettings.singleFileWriter : writer;
            toSource(writer2, abc.script_info.get(scriptIndex).traits.traits, new ConvertData(), exportSettings.mode, parallel);
        } catch (FileNotFoundException ex) {
            logger.log(Level.SEVERE, "The file path is probably too long", ex);
        }

        return file;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + System.identityHashCode(abc);
        hash = 79 * hash + scriptIndex;
        hash = 79 * hash + Objects.hashCode(path);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ScriptPack other = (ScriptPack) obj;
        if (abc != other.abc) {
            return false;
        }
        if (scriptIndex != other.scriptIndex) {
            return false;
        }
        return Objects.equals(path, other.path);
    }

    @Override
    public boolean isModified() {
        if (scriptIndex >= abc.script_info.size()) {
            return false;
        }
        return abc.script_info.get(scriptIndex).isModified();
    }

    /**
     * Injects debugfile, debugline instructions into the code
     *
     * Based on idea of Jacob Thompson
     * http://securityevaluators.com/knowledge/flash/
     */
    public void injectDebugInfo(File directoryPath) {
        Map<Integer, Map<Integer, Integer>> bodyToPosToLine = new HashMap<>();
        Map<Integer, Map<Integer, Integer>> bodyLineToPos = new HashMap<>();
        Map<Integer, Map<Integer, String>> bodyToRegToName = new HashMap<>();
        Map<Integer, Map<Integer, Integer>> bodyToRegToLine = new HashMap<>();

        Set<Integer> lonelyBody = new HashSet<>();
        try {
            HighlightedText decompiled = SWF.getCached(this);
            int line = 1;
            String txt = decompiled.text;
            txt = txt.replace("\r", "");

            for (int i = 0; i < txt.length(); i++) {
                blk:
                {
                    Highlighting sh = Highlighting.searchPos(decompiled.getSpecialHighlights(), i);

                    Highlighting cls = Highlighting.searchPos(decompiled.getClassHighlights(), i);
                    /*if (cls == null) {
                     continue;
                     }*/
                    Highlighting trt = Highlighting.searchPos(decompiled.getTraitHighlights(), i);
                    /*if (trt == null) {
                     continue;
                     }*/
                    Highlighting method = Highlighting.searchPos(decompiled.getMethodHighlights(), i);
                    if (method == null) {
                        break blk;
                    }
                    Highlighting instr = Highlighting.searchPos(decompiled.getInstructionHighlights(), i); //h
                    /*if (instr == null) {
                     continue;
                     }*/
                    int classIndex = cls == null ? -1 : (int) cls.getProperties().index;
                    int methodIndex = (int) method.getProperties().index;
                    int bodyIndex = abc.findBodyIndex(methodIndex);
                    if (bodyIndex == -1) {
                        break blk;
                    }
                    int pos = -1;
                    int regIndex = -1;
                    String regName = null;
                    if (sh != null && sh.getProperties().declaration && sh.getProperties().regIndex > -1) {
                        regIndex = sh.getProperties().regIndex;
                        regName = sh.getProperties().localName;
                    }
                    if (instr != null) {
                        if (instr.getProperties().declaration && instr.getProperties().regIndex > -1) {
                            regIndex = instr.getProperties().regIndex;
                            regName = instr.getProperties().localName;
                        }

                        long instrOffset = instr.getProperties().firstLineOffset;
                        if (trt != null && cls != null) {
                            int traitIndex = (int) trt.getProperties().index;

                            Trait trait = abc.findTraitByTraitId(classIndex, traitIndex);
                            if (((trait instanceof TraitMethodGetterSetter) && (((TraitMethodGetterSetter) trait).method_info != methodIndex))
                                    || ((trait instanceof TraitFunction) && (((TraitFunction) trait).method_info != methodIndex))) {
                                continue; //inner anonymous function - ignore. TODO: make work
                            }
                        }

                        if (instrOffset == -1) {
                            lonelyBody.add(bodyIndex);
                            break blk;
                        }
                        try {
                            pos = abc.bodies.get(bodyIndex).getCode().adr2pos(instrOffset);
                        } catch (ConvertException cex) {
                            //ignore
                        }
                        if (pos == -1) {
                            lonelyBody.add(bodyIndex);
                            break blk;
                        }
                        if (!bodyToPosToLine.containsKey(bodyIndex)) {
                            bodyToPosToLine.put(bodyIndex, new HashMap<>());
                            bodyLineToPos.put(bodyIndex, new HashMap<>());
                        }
                        //int origPos = bodyLineToPos.get(bodyIndex).containsKey(line) ? bodyLineToPos.get(bodyIndex).get(line) : -1;

                        bodyToPosToLine.get(bodyIndex).put(pos, line);
                        bodyLineToPos.get(bodyIndex).put(line, pos);
                    } else {
                        lonelyBody.add(bodyIndex);
                    }
                    if (regIndex > -1 && regName != null) {
                        if (!bodyToRegToName.containsKey(bodyIndex)) {
                            bodyToRegToName.put(bodyIndex, new HashMap<>());
                            bodyToRegToLine.put(bodyIndex, new HashMap<>());
                        }
                        if (!bodyToRegToName.get(bodyIndex).containsKey(regIndex)) {
                            bodyToRegToName.get(bodyIndex).put(regIndex, regName);
                            bodyToRegToLine.get(bodyIndex).put(regIndex, line);
                        }
                    }
                }
                if (txt.charAt(i) == '\n') {
                    line++;
                }

            }
        } catch (InterruptedException ex) {
            logger.log(Level.SEVERE, "Cannot decompile", ex);
        }
        int scriptInitBody = abc.findBodyIndex(abc.script_info.get(scriptIndex).init_index);
        if (!bodyToRegToName.containsKey(scriptInitBody)) {
            lonelyBody.add(scriptInitBody);
        }

        //String filepath = path.toString().replace('.', '/') + ".as";
        String pkg = path.packageStr.toString();
        String cls = path.className;
        String filename = new File(directoryPath, path.packageStr.toFilePath()) + ";" + pkg.replace(".", File.separator) + ";" + cls + ".as";

        //Remove debug info from lonely bodies
        for (int bodyIndex : lonelyBody) {
            if (!bodyToPosToLine.keySet().contains(bodyIndex)) {
                MethodBody b = abc.bodies.get(bodyIndex);
                List<AVM2Instruction> code = b.getCode().code;
                for (int i = 0; i < code.size(); i++) {
                    AVM2Instruction ins = code.get(i);
                    if (ins.definition instanceof DebugLineIns) {
                        b.removeInstruction(i);
                        i--;
                    } else if (ins.definition instanceof DebugFileIns) {
                        b.removeInstruction(i);
                        i--;
                    } else if (ins.definition instanceof DebugIns) {
                        b.removeInstruction(i);
                        i--;
                    }
                }
                b.setModified();
            }
        }

        for (int bodyIndex : bodyToPosToLine.keySet()) {
            List<AVM2Instruction> delIns = new ArrayList<>();

            MethodBody b = abc.bodies.get(bodyIndex);
            List<AVM2Instruction> code = b.getCode().code;
            //add old debug instructions to TOREMOVE list
            for (AVM2Instruction ins : code) {
                if (ins.definition instanceof DebugLineIns) {
                    delIns.add(ins);
                }
                if (ins.definition instanceof DebugFileIns) {
                    delIns.add(ins);
                }
                if (ins.definition instanceof DebugIns) {
                    delIns.add(ins);
                }
            }
            int dpos = 0;
            b.insertInstruction(0, new AVM2Instruction(0, AVM2Instructions.DebugFile, new int[]{abc.constants.getStringId(filename, true)}), true);
            dpos++;
            Set<Integer> regs = bodyToRegToName.containsKey(bodyIndex) ? bodyToRegToName.get(bodyIndex).keySet() : new TreeSet<>();
            for (int r : regs) {
                String name = bodyToRegToName.get(bodyIndex).get(r);
                int line = bodyToRegToLine.get(bodyIndex).get(r);
                b.insertInstruction(dpos++, new AVM2Instruction(0, AVM2Instructions.Debug, new int[]{1, abc.constants.getStringId(name, true), r - 1, line}));
            }
            List<Integer> pos = new ArrayList<>(bodyToPosToLine.get(bodyIndex).keySet());
            Collections.sort(pos);
            Collections.reverse(pos);
            Set<Integer> addedLines = new HashSet<>();
            loopi:
            for (int i : pos) {
                int line = bodyToPosToLine.get(bodyIndex).get(i);
                if (addedLines.contains(line)) {
                    continue;
                }
                addedLines.add(line);
                logger.log(Level.FINE, "Script {0}: Insert debugline({1}) at pos {2} to body {3}", new Object[]{path, line, i, bodyIndex});
                b.insertInstruction(i + dpos, new AVM2Instruction(0, AVM2Instructions.DebugLine, new int[]{line}));
            }
            //remove old debug instructions
            for (int i = 0; i < code.size(); i++) {
                AVM2Instruction ins = code.get(i);
                for (AVM2Instruction d : delIns) {
                    if (ins == d) {
                        b.removeInstruction(i);
                        i--;
                        break;
                    }
                }
            }

            b.setModified();
        }

        ((Tag) abc.parentTag).setModified(true);
    }

    public void injectPCodeDebugInfo(int abcIndex) {

        Map<Integer, String> bodyToIdentifier = new HashMap<>();

        try {
            HighlightedText decompiled = SWF.getCached(this);
            String txt = decompiled.text;
            txt = txt.replace("\r", "");

            for (int i = 0; i < txt.length(); i++) {
                blk:
                {
                    Highlighting sh = Highlighting.searchPos(decompiled.getSpecialHighlights(), i);

                    Highlighting cls = Highlighting.searchPos(decompiled.getClassHighlights(), i);
                    Highlighting trt = Highlighting.searchPos(decompiled.getTraitHighlights(), i);
                    Highlighting method = Highlighting.searchPos(decompiled.getMethodHighlights(), i);
                    if (method == null) {
                        break blk;
                    }

                    int classIndex = cls == null ? -1 : (int) cls.getProperties().index;
                    int methodIndex = (int) method.getProperties().index;
                    int bodyIndex = abc.findBodyIndex(methodIndex);
                    if (bodyIndex == -1) {
                        break blk;
                    }

                    Trait trait;
                    int traitIndex = -10;
                    if (trt != null && cls != null) {
                        traitIndex = (int) trt.getProperties().index;

                        trait = abc.findTraitByTraitId(classIndex, traitIndex);
                        if (((trait instanceof TraitMethodGetterSetter) && (((TraitMethodGetterSetter) trait).method_info != methodIndex))
                                || ((trait instanceof TraitFunction) && (((TraitFunction) trait).method_info != methodIndex))) {
                            continue; //inner anonymous function - ignore. TODO: make work
                        }
                    }
                    bodyToIdentifier.put(bodyIndex, "abc:" + abcIndex + ",script:" + scriptIndex + ",class:" + classIndex + ",trait:" + traitIndex + ",method:" + methodIndex + ",body:" + bodyIndex);
                }
            }
        } catch (InterruptedException ex) {
            logger.log(Level.SEVERE, "Cannot decompile", ex);
        }

        int scriptInitBody = abc.findBodyIndex(abc.script_info.get(scriptIndex).init_index);

        if (!bodyToIdentifier.containsKey(scriptInitBody)) {
            bodyToIdentifier.put(scriptInitBody, "abc:" + abcIndex + ",script:" + scriptIndex + ",class:-1,trait:-3,method:" + abc.script_info.get(scriptIndex).init_index + ",body:" + scriptInitBody);
        }

        String pkg = path.packageStr.toString();
        String cls = path.className;

        for (int bodyIndex : bodyToIdentifier.keySet()) {
            String bodyName = bodyToIdentifier.get(bodyIndex);

            MethodBody b = abc.bodies.get(bodyIndex);
            List<AVM2Instruction> list = b.getCode().code;

            int siz = list.size();

            for (int i = 0; i < siz; i++) {
                b.insertInstruction(i * 2, new AVM2Instruction(0, AVM2Instructions.DebugLine, new int[]{i + 1}));
            }
            for (int i = 1 /*odd, even are new debuglines*/; i < list.size(); i += 2) {
                if (list.get(i).definition instanceof DebugLineIns) {
                    b.removeInstruction(i);
                    b.removeInstruction(i - 1); //remove its new debugline too
                    i -= 2; //for loop to work correctly
                } else if (list.get(i).definition instanceof DebugFileIns) {
                    b.removeInstruction(i);
                    b.removeInstruction(i - 1);
                    i -= 2;
                } else if (list.get(i).definition instanceof DebugIns) {
                    b.removeInstruction(i);
                    b.removeInstruction(i - 1);
                    i -= 2;
                }
            }
            String filename = "#PCODE " + bodyName + ";" + pkg.replace(".", File.separator) + ";" + cls + ".as";

            b.insertInstruction(0, new AVM2Instruction(0, AVM2Instructions.DebugFile, new int[]{abc.constants.getStringId(filename, true)}));
            b.setModified();
        }

        ((Tag) abc.parentTag).setModified(true);
    }

    public void getMethodInfos(List<MethodId> methodInfos) {
        int script_init = abc.script_info.get(scriptIndex).init_index;
        methodInfos.add(new MethodId(GraphTextWriter.TRAIT_SCRIPT_INITIALIZER, -1, script_init));

        List<Trait> traits = abc.script_info.get(scriptIndex).traits.traits;
        for (int t : traitIndices) {
            Trait trait = traits.get(t);
            trait.getMethodInfos(abc, GraphTextWriter.TRAIT_UNKNOWN, -1, methodInfos);
        }
    }

    public void delete(ABC abc, boolean d) {
        ScriptInfo si = abc.script_info.get(scriptIndex);
        if (isSimple) {
            si.delete(abc, d);
        } else {
            for (int t : traitIndices) {
                si.traits.traits.get(t).delete(abc, d);
            }
        }
    }
}
