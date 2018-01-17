/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.flash.exporters.script;

import com.jpexs.decompiler.flash.AbortRetryIgnoreHandler;
import com.jpexs.decompiler.flash.EventListener;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.abc.avm2.model.CallPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.CoerceAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ConstructPropAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.FullMultinameAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.GetPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.InitPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.LocalRegAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NewArrayAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetLocalAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ThisAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.clauses.DeclarationAVM2Item;
import com.jpexs.decompiler.flash.abc.types.ConvertData;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitClass;
import com.jpexs.decompiler.flash.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.exporters.settings.ScriptExportSettings;
import com.jpexs.decompiler.flash.helpers.NulWriter;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.ScopeStack;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.Path;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 *
 * @author JPEXS
 */
public class AS3ScriptExporter {

    private static final Logger logger = Logger.getLogger(AS3ScriptExporter.class.getName());

    private static String prettyFormatXML(String input) {
        int indent = 5;
        try {
            Source xmlInput = new StreamSource(new StringReader(input));
            StringWriter stringWriter = new StringWriter();
            StreamResult xmlOutput = new StreamResult(stringWriter);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute("indent-number", indent);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.transform(xmlInput, xmlOutput);
            return xmlOutput.getWriter().toString();
        } catch (TransformerFactoryConfigurationError | IllegalArgumentException | TransformerException e) {
            logger.log(Level.SEVERE, "Pretty print error", e);
            return input;
        }
    }

    private String handleMxmlMethod(Map<String, String> namespaces, ScriptPack pack, int cindex, TraitMethodGetterSetter t) {
        StringBuilder out = new StringBuilder();
        int method = t.method_info;
        try {
            pack.abc.findBody(method).convert(new ConvertData(), "??", ScriptExportMode.AS, false, method, pack.scriptIndex, cindex, pack.abc, t, new ScopeStack(), 0/*?*/, new NulWriter(), new ArrayList<>(), new ArrayList<>(), true);

            List<GraphTargetItem> ci = pack.abc.findBody(method).convertedItems;
            if (!ci.isEmpty()) {
                if (ci.get(0) instanceof DeclarationAVM2Item) {
                    GraphTargetItem asg = ((DeclarationAVM2Item) ci.get(0)).assignment;
                    if (asg instanceof SetLocalAVM2Item) {
                        if (asg.value instanceof CoerceAVM2Item) {
                            if (asg.value.value instanceof ConstructPropAVM2Item) {
                                ConstructPropAVM2Item cp = (ConstructPropAVM2Item) asg.value.value;
                                if (cp.propertyName instanceof FullMultinameAVM2Item) {
                                    int name = ((FullMultinameAVM2Item) cp.propertyName).multinameIndex;
                                    String tagName = getTagName(pack, name, name, namespaces);
                                    StringBuilder props = new StringBuilder();
                                    StringBuilder tagContent = new StringBuilder();
                                    for (int i = 1; i < ci.size(); i++) {
                                        if (ci.get(i) instanceof SetPropertyAVM2Item) {
                                            SetPropertyAVM2Item sp = (SetPropertyAVM2Item) ci.get(i);
                                            if (sp.object instanceof LocalRegAVM2Item) {
                                                if (((SetLocalAVM2Item) asg).regIndex == ((LocalRegAVM2Item) sp.object).regIndex) {
                                                    GraphTargetItem val = sp.value;
                                                    if (sp.propertyName instanceof FullMultinameAVM2Item) {
                                                        String propName = pack.abc.constants.getMultiname(((FullMultinameAVM2Item) sp.propertyName).multinameIndex).getName(pack.abc.constants, new ArrayList<>(), true, true);
                                                        if (val instanceof CallPropertyAVM2Item) {
                                                            CallPropertyAVM2Item cap = (CallPropertyAVM2Item) val;
                                                            if (cp.propertyName instanceof FullMultinameAVM2Item) {
                                                                int name2 = ((FullMultinameAVM2Item) cap.propertyName).multinameIndex;
                                                                for (Trait ct : pack.abc.instance_info.get(cindex).instance_traits.traits) {
                                                                    if (ct.name_index == name2 && (ct instanceof TraitMethodGetterSetter)) {
                                                                        tagContent.append(handleMxmlMethod(namespaces, pack, cindex, (TraitMethodGetterSetter) ct));
                                                                    }
                                                                }
                                                            }

                                                        } else if (val instanceof ConstructPropAVM2Item) {
                                                            //???
                                                        } else {
                                                            props.append(" ").append(propName).append("=\"").append(EcmaScript.toString(val.getResult())).append("\"");
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    out.append("<").append(tagName).append(props);
                                    if (tagContent.length() > 0) {
                                        out.append(">");
                                        out.append(tagContent);
                                        out.append("</").append(tagName).append(">");
                                    } else {
                                        out.append(" />");
                                    }
                                }
                            }
                        }
                    }
                }
            }
            //DeclarationAVM2->assignment=SetLocalAVM2->value=coerceAVM2Item-ConstructProp-FullMultiname
            //setprop - object localreg, propertyName, value value
            //setprop

        } catch (InterruptedException ex) {
            //?
            logger.log(Level.SEVERE, null, ex);
        }
        return out.toString();
    }

    private String handleMxmlArrMethod(Map<String, String> namespaces, ScriptPack pack, int cindex, TraitMethodGetterSetter t) {
        StringBuilder out = new StringBuilder();
        int method = t.method_info;
        try {
            pack.abc.findBody(method).convert(new ConvertData(), "??", ScriptExportMode.AS, false, method, pack.scriptIndex, cindex, pack.abc, t, new ScopeStack(), 0/*?*/, new NulWriter(), new ArrayList<>(), new ArrayList<>(), true);

            List<GraphTargetItem> ci = pack.abc.findBody(method).convertedItems;
            if (!ci.isEmpty() && (ci.get(0) instanceof DeclarationAVM2Item)) {
                GraphTargetItem asg = ((DeclarationAVM2Item) ci.get(0)).assignment;
                if (asg instanceof SetLocalAVM2Item) {
                    if (((SetLocalAVM2Item) asg).value.getNotCoerced() instanceof NewArrayAVM2Item) {
                        NewArrayAVM2Item nav = (NewArrayAVM2Item) ((SetLocalAVM2Item) asg).value.getNotCoerced();
                        for (GraphTargetItem v : nav.values) {
                            if (v instanceof CallPropertyAVM2Item) {
                                CallPropertyAVM2Item cp = (CallPropertyAVM2Item) v;
                                if (cp.receiver instanceof ThisAVM2Item) {
                                    if (cp.propertyName instanceof FullMultinameAVM2Item) {
                                        int name = ((FullMultinameAVM2Item) cp.propertyName).multinameIndex;
                                        for (Trait ct : pack.abc.instance_info.get(cindex).instance_traits.traits) {
                                            if (ct.name_index == name && (ct instanceof TraitMethodGetterSetter)) {
                                                out.append(handleMxmlMethod(namespaces, pack, cindex, (TraitMethodGetterSetter) ct));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            //declaration->setlocal->(coreceavm)->NewArray[ callpropertyav ->receiver this, propName  ]
        } catch (InterruptedException ex) {
            //?
            logger.log(Level.SEVERE, null, ex);
        }
        return out.toString();
    }

    private String getTagName(ScriptPack pack, int classMIndex, int nameMindex, Map<String, String> namespaces) {
        Multiname m = pack.abc.constants.getMultiname(classMIndex);
        Multiname mn = pack.abc.constants.getMultiname(nameMindex);
        String parentName = mn.getName(pack.abc.constants, new ArrayList<>(), true, true);
        String pkg = m.getNamespace(pack.abc.constants).getName(pack.abc.constants).toRawString();
        pkg += ".*";
        String ns = null;
        if (pkg.startsWith("spark.")) {
            ns = "s";
            pkg = "library://ns.adobe.com/flex/spark";
            namespaces.put(ns, pkg);
        } else if (pkg.startsWith("mx.")) {
            ns = "mx";
            pkg = "library://ns.adobe.com/flex/mx";
            //TODO: all common SWC libraries
            namespaces.put(ns, pkg);
        } else if (namespaces.containsValue(pkg)) {
            for (String k : namespaces.keySet()) {
                if (namespaces.get(k).equals(pkg)) {
                    ns = k;
                    break;
                }
            }
        } else {
            String baseNs = "pkg";
            ns = baseNs;
            int i = 1;
            for (i++; namespaces.containsKey(ns); i++) {
                ns = baseNs + i;
            }
            namespaces.put(ns, pkg);
        }
        return ns + ":" + parentName;
    }

    private String generateMxml(ScriptPack pack) {
        StringBuilder out = new StringBuilder();
        StringBuilder tagProp = new StringBuilder();
        StringBuilder tagContent = new StringBuilder();

        String hdr = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + System.lineSeparator();

        Map<String, String> namespaces = new HashMap<>();
        namespaces.put("fx", "http://ns.adobe.com/mxml/2009");

        for (int ti : pack.traitIndices) {
            Trait t = pack.abc.script_info.get(pack.scriptIndex).traits.traits.get(ti);
            if (t instanceof TraitClass) {

                int cindex = ((TraitClass) t).class_info;
                int mindex = pack.abc.instance_info.get(cindex).super_index;

                String tagName = getTagName(pack, mindex, mindex, namespaces);

                int iinit = pack.abc.instance_info.get(cindex).iinit_index;

                try {
                    pack.abc.findBody(iinit).convert(new ConvertData(), "??", ScriptExportMode.AS, false, iinit, pack.scriptIndex, cindex, pack.abc, t, new ScopeStack(), 0/*?*/, new NulWriter(), new ArrayList<>(), new ArrayList<>(), true);
                    List<GraphTargetItem> iinitBody = pack.abc.findBody(iinit).convertedItems;
                    for (GraphTargetItem it : iinitBody) {
                        if (it instanceof InitPropertyAVM2Item) {
                            InitPropertyAVM2Item ip = (InitPropertyAVM2Item) it;
                            if (ip.object instanceof ThisAVM2Item) {
                                String propName = pack.abc.constants.getMultiname(ip.propertyName.multinameIndex).getName(pack.abc.constants, new ArrayList<>(), true, true);
                                GraphTargetItem val = ((InitPropertyAVM2Item) it).value;
                                if (val instanceof CallPropertyAVM2Item) {
                                    CallPropertyAVM2Item cp = (CallPropertyAVM2Item) val;
                                    if (cp.propertyName instanceof FullMultinameAVM2Item) {
                                        String subtagName = getTagName(pack, mindex, ip.propertyName.multinameIndex, namespaces);
                                        tagContent.append("<").append(subtagName).append(">");
                                        int name = ((FullMultinameAVM2Item) cp.propertyName).multinameIndex;
                                        for (Trait ct : pack.abc.instance_info.get(cindex).instance_traits.traits) {
                                            if (ct.name_index == name && (ct instanceof TraitMethodGetterSetter)) {
                                                tagContent.append(handleMxmlMethod(namespaces, pack, cindex, (TraitMethodGetterSetter) ct));
                                            }
                                        }
                                        tagContent.append("</").append(subtagName).append(">");
                                    }

                                } else if (val instanceof ConstructPropAVM2Item) {
                                    ConstructPropAVM2Item cp = (ConstructPropAVM2Item) val;
                                    if (cp.propertyName instanceof FullMultinameAVM2Item) {
                                        Multiname m = pack.abc.constants.getMultiname(((FullMultinameAVM2Item) cp.propertyName).multinameIndex);
                                        if ("mx.core.DeferredInstanceFromFunction".equals("" + m.getNameWithNamespace(pack.abc.constants, true))) {
                                            if (!cp.args.isEmpty()) {
                                                if (cp.args.get(0) instanceof GetPropertyAVM2Item) {
                                                    GetPropertyAVM2Item gp = (GetPropertyAVM2Item) cp.args.get(0);
                                                    if (gp.object instanceof ThisAVM2Item) {
                                                        int name = ((FullMultinameAVM2Item) gp.propertyName).multinameIndex;
                                                        for (Trait ct : pack.abc.instance_info.get(cindex).instance_traits.traits) {
                                                            if (ct.name_index == name && (ct instanceof TraitMethodGetterSetter)) {
                                                                tagContent.append(handleMxmlArrMethod(namespaces, pack, cindex, (TraitMethodGetterSetter) ct));
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    tagProp.append(" ").append(propName).append("=\"").append(EcmaScript.toString(val.getResult())).append("\"");
                                }
                            }
                            //System.err.println("" + ((InitPropertyAVM2Item) it).value);
                        }
                    }
                    out.append("<").append(tagName);

                    for (String ns : namespaces.keySet()) {
                        out.append(" xmlns:").append(ns).append("=\"").append(namespaces.get(ns)).append("\"");
                    }
                    if (tagContent.length() == 0) {
                        out.append(" />");
                    } else {
                        out.append(tagProp).append(">");
                        out.append(tagContent);

                        out.append("</").append(tagName).append(">");
                    }

                } catch (InterruptedException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }

            }
        }

        return hdr + prettyFormatXML(out.toString());
    }

    public List<File> exportActionScript3(SWF swf, AbortRetryIgnoreHandler handler, String outdir, List<ScriptPack> as3scripts, ScriptExportSettings exportSettings, boolean parallel, EventListener evl) {
        final List<File> ret = new ArrayList<>();
        final List<ScriptPack> packs = as3scripts != null ? as3scripts : swf.getAS3Packs();

        List<String> ignoredClasses = new ArrayList<>();
        List<String> ignoredNss = new ArrayList<>();

        String flexClass = null;
        if (Configuration._enableFlexExport.get()) {
            flexClass = swf.getFlexMainClass(ignoredClasses, ignoredNss);
        }

        int cnt = 1;
        List<ExportPackTask> tasks = new ArrayList<>();
        Set<String> files = new HashSet<>();
        for (ScriptPack item : packs) {
            if (!item.isSimple && Configuration.ignoreCLikePackages.get()) {
                continue;
            }
            if (ignoredClasses.contains(item.getClassPath().toRawString())) {
                continue;
            }
            if (flexClass != null && item.getClassPath().toRawString().equals(flexClass)) {
                File file = item.getExportFile(outdir, ".mxml");
                String filePath = file.getPath();
                String mxml = generateMxml(item);
                if (mxml != null) {
                    Helper.writeFile(filePath, Utf8Helper.getBytes(mxml));
                    files.add(filePath.toLowerCase());
                    continue;
                }
            }

            File file = null;
            if (!exportSettings.singleFile) {
                file = item.getExportFile(outdir, exportSettings);
                String filePath = file.getPath();
                if (files.contains(filePath.toLowerCase())) {
                    String parentPath = file.getParent();
                    String fileName = file.getName();
                    String extension = Path.getExtension(fileName);
                    String fileNameWithoutExtension = Path.getFileNameWithoutExtension(file);
                    int i = 2;
                    do {
                        filePath = Path.combine(parentPath, fileNameWithoutExtension + "_" + i++ + extension);
                    } while (files.contains(filePath.toLowerCase()));

                    file = new File(filePath);
                }

                files.add(filePath.toLowerCase());
            }

            tasks.add(new ExportPackTask(handler, cnt++, packs.size(), item.getClassPath(), item, file, exportSettings, parallel, evl));
        }

        if (!parallel || tasks.size() < 2) {
            try {
                CancellableWorker.call(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        for (ExportPackTask task : tasks) {
                            if (Thread.currentThread().isInterrupted()) {
                                throw new InterruptedException();
                            }

                            ret.add(task.call());
                        }
                        return null;
                    }
                }, Configuration.exportTimeout.get(), TimeUnit.SECONDS);
            } catch (TimeoutException ex) {
                logger.log(Level.SEVERE, Helper.formatTimeToText(Configuration.exportTimeout.get()) + " ActionScript export limit reached", ex);
            } catch (ExecutionException | InterruptedException ex) {
                logger.log(Level.SEVERE, "Error during ABC export", ex);
            }
        } else {
            ExecutorService executor = Executors.newFixedThreadPool(Configuration.getParallelThreadCount());
            List<Future<File>> futureResults = new ArrayList<>();
            for (ExportPackTask task : tasks) {
                Future<File> future = executor.submit(task);
                futureResults.add(future);
            }

            try {
                executor.shutdown();
                if (!executor.awaitTermination(Configuration.exportTimeout.get(), TimeUnit.SECONDS)) {
                    logger.log(Level.SEVERE, Helper.formatTimeToText(Configuration.exportTimeout.get()) + " ActionScript export limit reached");
                }
            } catch (InterruptedException ex) {
            } finally {
                executor.shutdownNow();
            }

            for (int f = 0; f < futureResults.size(); f++) {
                try {
                    if (futureResults.get(f).isDone()) {
                        ret.add(futureResults.get(f).get());
                    }
                } catch (InterruptedException ex) {
                } catch (ExecutionException ex) {
                    logger.log(Level.SEVERE, "Error during ABC export", ex);
                }
            }
        }

        return ret;
    }
}
