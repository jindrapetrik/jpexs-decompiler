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
import com.jpexs.decompiler.flash.abc.avm2.model.CallPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ConstructPropAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.FullMultinameAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.GetLexAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.GetPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NullAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NumberValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.StringAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ThisAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.UndefinedAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.XMLAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.clauses.TryAVM2Item;
import com.jpexs.decompiler.flash.abc.types.ConvertData;
import com.jpexs.decompiler.flash.abc.types.InstanceInfo;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitClass;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.NulWriter;
import com.jpexs.decompiler.flash.tags.DefineBinaryDataTag;
import com.jpexs.decompiler.flash.tags.DefineFont4Tag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.graph.AbstractGraphTargetVisitor;
import com.jpexs.decompiler.graph.Block;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.ScopeStack;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** Restores literal Flash CS5/CS6 TLF initializers as native authoring text. */
final class TlfTextConverter {

    private static final String NS = "http://ns.adobe.com/xfl/2008/";

    private final SWF swf;

    private final Map<String, List<Text>> texts = new LinkedHashMap<>();

    private final Map<MethodBody, List<Text>> constructors = new HashMap<>();

    private final Map<MethodBody, ABC> constructorAbcs = new HashMap<>();

    private final Map<MethodBody, List<GraphTargetItem>> constructorItems = new HashMap<>();

    private TlfScriptCleanup scriptCleanup;

    private final Set<MethodBody> completeConstructors = new HashSet<>();

    private final Set<Text> restored = new HashSet<>();

    private final Set<Trait> ignoredTraits = new HashSet<>();

    private final Map<String, String> markupTokens = new LinkedHashMap<>();

    private static class Text {
        String name;
        String placement;
        String scene;
        String markup;
        double[] bounds;
        double originX;
        double originY;
        int firstFrame;
        int lastFrame;
    }

    /** The RSL preloader is packaging, rather than the document's timeline. */
    static SWF unwrap(SWF swf) throws IOException, InterruptedException {
        String document = swf.getDocumentClass();
        if (document == null || !document.endsWith("__Preloader__")
                || swf.getScriptPacksByClassNames(Collections.singletonList("fl.rsl.RSLPreloader")).isEmpty()) {
            return swf;
        }
        CharacterTag content = swf.getCharacterByClass(document.substring(0,
                document.length() - "__Preloader__".length()) + "__Content__");
        if (!(content instanceof DefineBinaryDataTag) || !((DefineBinaryDataTag) content).isSwfData()) {
            return swf;
        }
        SWF inner = new SWF(new ByteArrayInputStream(((DefineBinaryDataTag) content).binaryData.getRangeData()), false);
        return hasRuntime(inner) ? inner : swf;
    }

    private static boolean hasRuntime(SWF swf) {
        return !swf.getScriptPacksByClassNames(java.util.Arrays.asList(
                "fl.text.RuntimeManager", "fl.text.TCMRuntimeManager")).isEmpty();
    }

    TlfTextConverter(SWF swf) throws InterruptedException {
        this.swf = swf;
        if (!hasRuntime(swf)) {
            return;
        }
        for (ScriptPack pack : swf.getAS3Packs()) {
            ComponentConverter.checkInterrupted();
            for (int index : pack.traitIndices) {
                Trait trait = pack.abc.script_info.get(pack.scriptIndex).traits.traits.get(index);
                if (trait instanceof TraitClass) {
                    analyze(pack, ((TraitClass) trait).class_info);
                }
            }
        }
    }

    private static String name(GraphTargetItem item) {
        return item instanceof FullMultinameAVM2Item
                ? ((FullMultinameAVM2Item) item).resolvedMultinameName : null;
    }

    private static String string(GraphTargetItem item) {
        return item instanceof StringAVM2Item ? ((StringAVM2Item) item).getValue() : null;
    }

    private static Double number(GraphTargetItem item) {
        if (!(item instanceof NumberValueAVM2Item) || !(item.getResult() instanceof Number)) {
            return null;
        }
        double value = ((Number) item.getResult()).doubleValue();
        return Double.isFinite(value) ? value : null;
    }

    private static boolean manager(GraphTargetItem receiver) {
        if (!(receiver instanceof CallPropertyAVM2Item)) {
            return false;
        }
        CallPropertyAVM2Item singleton = (CallPropertyAVM2Item) receiver;
        if (!"getSingleton".equals(name(singleton.propertyName)) || !singleton.arguments.isEmpty()
                || !(singleton.receiver instanceof GetLexAVM2Item)) {
            return false;
        }
        String type = ((GetLexAVM2Item) singleton.receiver).getRawPropertyName();
        return "fl.text.RuntimeManager".equals(type) || "fl.text.TCMRuntimeManager".equals(type);
    }

    private static List<CallPropertyAVM2Item> calls(GraphTargetItem root) {
        List<CallPropertyAVM2Item> result = new ArrayList<>();
        if (root instanceof CallPropertyAVM2Item) {
            result.add((CallPropertyAVM2Item) root);
        }
        root.visitRecursively(new AbstractGraphTargetVisitor() {
            @Override
            public boolean visit(GraphTargetItem item) {
                if (item instanceof CallPropertyAVM2Item) {
                    result.add((CallPropertyAVM2Item) item);
                }
                return true;
            }
        });
        return result;
    }

    private static Text read(CallPropertyAVM2Item call) {
        List<GraphTargetItem> args = call.arguments;
        if (!manager(call.receiver) || !"addInstance".equals(name(call.propertyName))
                || (args.size() != 9 && args.size() != 11) || !(args.get(0) instanceof ThisAVM2Item)
                || string(args.get(1)) == null || !(args.get(2) instanceof ConstructPropAVM2Item)
                || !(args.get(3) instanceof XMLAVM2Item) || !(args.get(4) instanceof NullAVM2Item)
                || !(args.get(5) instanceof UndefinedAVM2Item
                || args.get(5) instanceof GetLexAVM2Item && "undefined".equals(((GetLexAVM2Item) args.get(5)).getRawPropertyName()))) {
            return null;
        }
        ConstructPropAVM2Item rectangle = (ConstructPropAVM2Item) args.get(2);
        if (!"Rectangle".equals(name(rectangle.propertyName)) || rectangle.args.size() != 4) {
            return null;
        }
        Text text = new Text();
        text.bounds = new double[4];
        for (int i = 0; i < 4; i++) {
            Double value = number(rectangle.args.get(i));
            if (value == null) {
                return null;
            }
            text.bounds[i] = value;
        }
        Double first = number(args.get(6));
        Double last = number(args.get(7));
        if (first == null || last == null || first < 0 || last < first
                || first != Math.rint(first) || last != Math.rint(last) || last > Integer.MAX_VALUE
                || string(args.get(8)) == null) {
            return null;
        }
        StringBuilder markup = new StringBuilder();
        for (GraphTargetItem part : ((XMLAVM2Item) args.get(3)).parts) {
            if (string(part) == null) {
                return null; // Dynamic XML must remain code.
            }
            markup.append(string(part));
        }
        try {
            Element root = ComponentConverter.parse(markup.toString()).getDocumentElement();
            if (!"tlfTextObject".equals(root.getNodeName())
                    || root.getElementsByTagNameNS("http://ns.adobe.com/textLayout/2008", "TextFlow").getLength() != 1) {
                return null;
            }
            if ("false".equals(root.getAttribute("multiline"))) {
                // Single-line authoring text uses the padded baseline origin;
                // the runtime placeholder instead starts at the outer bounds.
                text.originX = root.hasAttribute("paddingLeft") ? Double.parseDouble(root.getAttribute("paddingLeft")) : 2;
                text.originY = root.hasAttribute("paddingTop") ? Double.parseDouble(root.getAttribute("paddingTop")) : 2;
                if (!Double.isFinite(text.originX) || !Double.isFinite(text.originY)) {
                    return null;
                }
                text.bounds[0] -= text.originX;
                text.bounds[1] -= text.originY;
            }
        } catch (Exception ex) {
            return null;
        }
        text.name = string(args.get(1));
        text.placement = text.name;
        text.scene = string(args.get(8));
        text.firstFrame = first.intValue();
        text.lastFrame = last.intValue();
        text.markup = markup.toString();
        return text;
    }

    private static boolean motionTarget(CallPropertyAVM2Item call) {
        if (!"addTargetInfo".equals(name(call.propertyName)) || call.arguments.size() != 10
                || !(call.arguments.get(0) instanceof ThisAVM2Item)
                || !(call.receiver instanceof GetPropertyAVM2Item)) {
            return false;
        }
        GetPropertyAVM2Item receiver = (GetPropertyAVM2Item) call.receiver;
        return receiver.object instanceof ThisAVM2Item && name(receiver.propertyName) != null
                && name(receiver.propertyName).startsWith("__animFactory_")
                && call.arguments.get(9) instanceof GetLexAVM2Item
                && "fl.text.RuntimeManager".equals(((GetLexAVM2Item) call.arguments.get(9)).getRawPropertyName());
    }

    private void analyze(ScriptPack pack, int classIndex) throws InterruptedException {
        ABC abc = pack.abc;
        InstanceInfo instance = abc.instance_info.get(classIndex);
        String owner = instance.getName(abc.constants).getNameWithNamespace(new LinkedHashSet<>(), abc, abc.constants, true).toRawString();
        if (!owner.equals(swf.getDocumentClass()) && swf.getCharacterByClass(owner) == null) {
            return;
        }
        MethodBody body = abc.findBody(instance.iinit_index);
        if (body == null) {
            return;
        }
        List<MethodBody> stack = new ArrayList<>();
        stack.add(body);
        body.convert(swf.version, stack, swf.getAbcIndex(), new ConvertData(), "TLF text",
                ScriptExportMode.AS, false, instance.iinit_index, pack.scriptIndex, classIndex, abc,
                null, new ScopeStack(), 0, new NulWriter(), new ArrayList<>(), new Traits(), true,
                new HashSet<>(), new ArrayList<>(), new LinkedHashSet<>());
        if (body.convertException != null || body.convertedItems == null) {
            return;
        }
        List<Text> found = new ArrayList<>();
        List<CallPropertyAVM2Item> allCalls = new ArrayList<>();
        List<CallPropertyAVM2Item> registrations = new ArrayList<>();
        for (GraphTargetItem item : body.convertedItems) {
            allCalls.addAll(calls(item));
            // Generated registrations are direct statements in the
            // constructor's XML-settings try/finally, never in user loops.
            if (item instanceof TryAVM2Item) {
                TryAVM2Item block = (TryAVM2Item) item;
                if (block.catchCommands.isEmpty() && block.finallyCommands != null) {
                    for (GraphTargetItem statement : block.tryCommands) {
                        if (statement instanceof CallPropertyAVM2Item) {
                            registrations.add((CallPropertyAVM2Item) statement);
                        }
                    }
                }
            }
        }
        for (CallPropertyAVM2Item call : registrations) {
            Text text = read(call);
            if (text != null && found.stream().noneMatch(previous -> previous.name.equals(text.name)
                    && previous.firstFrame == text.firstFrame && previous.lastFrame == text.lastFrame
                    && previous.markup.equals(text.markup))) {
                found.add(text);
            }
        }
        if (found.isEmpty()) {
            return;
        }
        // CS5 places a separate, empty MovieClip used as the motion target.
        for (CallPropertyAVM2Item call : allCalls) {
            if (!motionTarget(call)) {
                continue;
            }
            for (Text text : found) {
                if (text.name.equals(string(call.arguments.get(1))) && string(call.arguments.get(8)) != null) {
                    text.placement = string(call.arguments.get(8));
                }
            }
        }
        texts.put(owner, found);
        constructors.put(body, found);
        constructorAbcs.put(body, abc);
        constructorItems.put(body, new ArrayList<>(body.convertedItems));
        if (allCalls.stream().filter(call -> manager(call.receiver) && "addInstance".equals(name(call.propertyName)))
                .allMatch(call -> read(call) != null)) {
            completeConstructors.add(body);
        }
    }

    Set<Trait> getIgnoredTraits() {
        return Collections.unmodifiableSet(ignoredTraits);
    }

    String convert(String document, Map<String, byte[]> files) throws IOException, InterruptedException {
        if (texts.isEmpty()) {
            return document;
        }
        try {
            Document dom = ComponentConverter.parse(document);
            convertDocument(dom, swf.getDocumentClass());
            for (Map.Entry<String, byte[]> file : files.entrySet()) {
                ComponentConverter.checkInterrupted();
                if (file.getKey().endsWith(".xml")) {
                    Document symbol = ComponentConverter.parse(new String(file.getValue(), Utf8Helper.charset));
                    if (convertDocument(symbol, symbol.getDocumentElement().getAttribute("linkageClassName"))) {
                        file.setValue(Utf8Helper.getBytes(xml(symbol)));
                    }
                }
            }
            // Hide only fields corresponding to text actually restored in XML.
            for (Map.Entry<MethodBody, List<Text>> entry : constructors.entrySet()) {
                ABC abc = constructorAbcs.get(entry.getKey());
                for (InstanceInfo instance : abc.instance_info) {
                    if (instance.iinit_index != entry.getKey().method_info) {
                        continue;
                    }
                    for (Trait trait : instance.instance_traits.traits) {
                        String name = trait.getName(abc).getName(new LinkedHashSet<>(), abc, abc.constants, new ArrayList<>(), false, false);
                        for (Text text : entry.getValue()) {
                            if (restored.contains(text) && trait instanceof TraitSlotConst
                                    && (text.name.equals(name) || text.placement.equals(name))) {
                                ignoredTraits.add(trait);
                            }
                        }
                    }
                }
            }
            Map<MethodBody, Set<String>> nativeNames = new HashMap<>();
            Set<MethodBody> complete = new HashSet<>();
            for (Map.Entry<MethodBody, List<Text>> entry : constructors.entrySet()) {
                Set<String> names = new HashSet<>();
                for (Text text : entry.getValue()) {
                    if (restored.contains(text)) {
                        names.add(text.name);
                    }
                }
                if (!names.isEmpty()) {
                    nativeNames.put(entry.getKey(), names);
                }
                if (completeConstructors.contains(entry.getKey()) && entry.getValue().stream().allMatch(restored::contains)) {
                    complete.add(entry.getKey());
                }
            }
            scriptCleanup = new TlfScriptCleanup(swf, constructorAbcs, constructorItems, nativeNames, complete, ignoredTraits);
            return xml(dom);
        } catch (InterruptedException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IOException("Cannot restore TLF text", ex);
        }
    }

    private String xml(Document document) throws Exception {
        String result = ComponentConverter.xml(document);
        for (Map.Entry<String, String> markup : markupTokens.entrySet()) {
            result = result.replace(markup.getKey(), markup.getValue());
        }
        return result;
    }

    private boolean convertDocument(Document document, String owner) throws Exception {
        List<Text> definitions = texts.get(owner);
        if (definitions == null) {
            return false;
        }
        boolean changed = false;
        NodeList instances = document.getElementsByTagNameNS(NS, "DOMSymbolInstance");
        List<Element> placements = new ArrayList<>();
        Set<Element> duplicateLayers = new HashSet<>();
        for (int i = 0; i < instances.getLength(); i++) {
            placements.add((Element) instances.item(i));
        }
        for (Element instance : placements) {
            if (instance.getParentNode() == null) {
                continue;
            }
            Element frame = ancestor(instance, "DOMFrame");
            Element timeline = ancestor(instance, "DOMTimeline");
            if (frame == null || timeline == null) {
                continue;
            }
            int first = Integer.parseInt(frame.getAttribute("index"));
            int duration = frame.hasAttribute("duration") ? Integer.parseInt(frame.getAttribute("duration")) : 1;
            for (Text text : definitions) {
                if (!text.placement.equals(instance.getAttribute("name"))
                        || first < text.firstFrame || first + duration - 1 > text.lastFrame
                        || owner.equals(swf.getDocumentClass()) && !text.scene.equals(timeline.getAttribute("name"))) {
                    continue;
                }
                Element element = document.createElementNS(NS, "DOMTLFText");
                if (!text.placement.equals(text.name)) {
                    removeRuntimeDuplicate(instance, duplicateLayers);
                }
                if (!text.name.matches("__id[0-9]+_")) {
                    element.setAttribute("name", text.name);
                }
                if (text.bounds[0] != 0) {
                    element.setAttribute("left", twips(text.bounds[0]));
                }
                if (text.bounds[1] != 0) {
                    element.setAttribute("top", twips(text.bounds[1]));
                }
                element.setAttribute("right", twips(text.bounds[0] + text.bounds[2]));
                element.setAttribute("bottom", twips(text.bounds[1] + text.bounds[3]));
                for (Node child = instance.getFirstChild(); child != null; child = child.getNextSibling()) {
                    if (child instanceof Element && ("matrix".equals(child.getLocalName())
                            || "filters".equals(child.getLocalName()))) {
                        element.appendChild(child.cloneNode(true));
                    }
                }
                if (text.placement.equals(text.name) && (text.originX != 0 || text.originY != 0)) {
                    Element matrix = (Element) element.getElementsByTagNameNS(NS, "Matrix").item(0);
                    if (matrix != null) {
                        matrix.setAttribute("tx", Double.toString(attribute(matrix, "tx", 0)
                                + attribute(matrix, "a", 1) * text.originX + attribute(matrix, "c", 0) * text.originY));
                        matrix.setAttribute("ty", Double.toString(attribute(matrix, "ty", 0)
                                + attribute(matrix, "b", 0) * text.originX + attribute(matrix, "d", 1) * text.originY));
                    }
                }
                Document markup = ComponentConverter.parse(text.markup);
                appendFonts(document, element, markup);
                Element container = document.createElementNS(NS, "markup");
                // Flash's TLF reader requires the original attribute ordering.
                // Do not pretty-print or DOM-serialize this mixed-content XML.
                String token = "ffdec_tlf_" + java.util.UUID.randomUUID().toString();
                markupTokens.put(token, text.markup);
                container.setTextContent(token);
                element.appendChild(container);
                instance.getParentNode().replaceChild(element, instance);
                restored.add(text);
                changed = true;
                break;
            }
        }
        for (Element layer : duplicateLayers) {
            removeEmptyRuntimeLayer(layer);
        }
        return changed;
    }

    /** CS5 emits a second anonymous copy two depths above each motion target. */
    private static void removeRuntimeDuplicate(Element instance, Set<Element> duplicateLayers) {
        Element layer = ancestor(instance, "DOMLayer");
        Element frame = ancestor(instance, "DOMFrame");
        if (layer == null || frame == null) {
            return;
        }
        Node previous = layer.getPreviousSibling();
        while (previous != null && !(previous instanceof Element)) {
            previous = previous.getPreviousSibling();
        }
        if (!(previous instanceof Element) || !"DOMLayer".equals(previous.getLocalName())) {
            return;
        }
        Element duplicateLayer = (Element) previous;
        NodeList candidates = duplicateLayer.getElementsByTagNameNS(NS, "DOMSymbolInstance");
        for (int i = 0; i < candidates.getLength(); i++) {
            Element candidate = (Element) candidates.item(i);
            Element candidateFrame = ancestor(candidate, "DOMFrame");
            if (candidate.hasAttribute("name") || candidateFrame == null
                    || !candidate.getAttribute("libraryItemName").equals(instance.getAttribute("libraryItemName"))
                    || attribute(candidateFrame, "index", 0) != attribute(frame, "index", 0)
                    || attribute(candidateFrame, "duration", 1) != attribute(frame, "duration", 1)) {
                continue;
            }
            Element matrix = (Element) instance.getElementsByTagNameNS(NS, "Matrix").item(0);
            Element other = (Element) candidate.getElementsByTagNameNS(NS, "Matrix").item(0);
            if (matrix == null || other == null) {
                continue;
            }
            boolean same = true;
            for (String name : new String[]{"a", "b", "c", "d", "tx", "ty"}) {
                double defaultValue = "a".equals(name) || "d".equals(name) ? 1 : 0;
                same &= attribute(matrix, name, defaultValue) == attribute(other, name, defaultValue);
            }
            if (same) {
                candidate.getParentNode().removeChild(candidate);
                duplicateLayers.add(duplicateLayer);
                return;
            }
        }
    }

    private static void removeEmptyRuntimeLayer(Element layer) {
        if (layer.hasAttribute("layerType")) {
            return;
        }
        NodeList frames = layer.getElementsByTagNameNS(NS, "DOMFrame");
        for (int i = 0; i < frames.getLength(); i++) {
            Element frame = (Element) frames.item(i);
            for (int j = 0; j < frame.getAttributes().getLength(); j++) {
                String name = frame.getAttributes().item(j).getNodeName();
                if (!"index".equals(name) && !"duration".equals(name) && !"keyMode".equals(name)) {
                    return;
                }
            }
            NodeList children = frame.getElementsByTagName("*");
            for (int j = 0; j < children.getLength(); j++) {
                if (!"elements".equals(children.item(j).getLocalName())) {
                    return;
                }
            }
        }
        Element layers = (Element) layer.getParentNode();
        List<Element> siblings = new ArrayList<>();
        for (Node node = layers.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node instanceof Element && "DOMLayer".equals(node.getLocalName())) {
                siblings.add((Element) node);
            }
        }
        int index = siblings.indexOf(layer);
        for (Element sibling : siblings) {
            if (sibling.hasAttribute("parentLayerIndex") && attribute(sibling, "parentLayerIndex", -1) == index) {
                return;
            }
        }
        for (Element sibling : siblings) {
            int parent = (int) attribute(sibling, "parentLayerIndex", -1);
            if (parent > index) {
                sibling.setAttribute("parentLayerIndex", Integer.toString(parent - 1));
            }
        }
        layers.removeChild(layer);
    }

    private static String twips(double value) {
        return Long.toString(Math.round(value * SWF.unitDivisor));
    }

    private static double attribute(Element element, String name, double defaultValue) {
        return element.hasAttribute(name) ? Double.parseDouble(element.getAttribute(name)) : defaultValue;
    }

    private static Element ancestor(Node node, String name) {
        for (Node parent = node.getParentNode(); parent != null; parent = parent.getParentNode()) {
            if (parent instanceof Element && name.equals(parent.getLocalName())) {
                return (Element) parent;
            }
        }
        return null;
    }

    private void appendFonts(Document document, Element text, Document markup) {
        Set<String> families = new LinkedHashSet<>();
        NodeList nodes = markup.getElementsByTagName("*");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element node = (Element) nodes.item(i);
            if (node.hasAttribute("fontFamily")) {
                families.add(node.getAttribute("fontFamily"));
            }
        }
        Element fonts = document.createElementNS(NS, "tlfFonts");
        for (String family : families) {
            Element font = document.createElementNS(NS, "TLFFont");
            font.setAttribute("platformName", family);
            String psName = family;
            for (Tag tag : swf.getTags()) {
                if (tag instanceof DefineFont4Tag && family.equals(((DefineFont4Tag) tag).fontName)) {
                    psName = postScriptName((DefineFont4Tag) tag);
                    break;
                }
            }
            font.setAttribute("psName", psName);
            fonts.appendChild(font);
        }
        text.appendChild(fonts);
    }

    /** First entry of the CFF Name INDEX, independent of installed system fonts. */
    private static String postScriptName(DefineFont4Tag font) {
        byte[] data = font.fontData.getRangeData();
        if (data.length < 4) {
            return font.fontName;
        }
        int cff = 0;
        // DefineFont4 also permits an OpenType container around the CFF table.
        if (data[0] == 'O' && data[1] == 'T' && data[2] == 'T' && data[3] == 'O') {
            if (data.length < 12) {
                return font.fontName;
            }
            int tables = ((data[4] & 255) << 8) | (data[5] & 255);
            boolean found = false;
            for (int i = 0; i < tables && 12L + 16L * (i + 1) <= data.length; i++) {
                int table = 12 + i * 16;
                if (data[table] == 'C' && data[table + 1] == 'F' && data[table + 2] == 'F' && data[table + 3] == ' ') {
                    long offset = 0;
                    for (int j = 0; j < 4; j++) {
                        offset = (offset << 8) | (data[table + 8 + j] & 255);
                    }
                    if (offset + 4 > data.length) {
                        return font.fontName;
                    }
                    cff = (int) offset;
                    found = true;
                    break;
                }
            }
            if (!found) {
                return font.fontName;
            }
        }
        int p = cff + (data[cff + 2] & 255);
        if (p + 3 > data.length) {
            return font.fontName;
        }
        int count = ((data[p] & 255) << 8) | (data[p + 1] & 255);
        int size = data[p + 2] & 255;
        if (count == 0 || size < 1 || size > 4 || p + 3L + (count + 1L) * size > data.length) {
            return font.fontName;
        }
        long start = 0;
        long end = 0;
        for (int i = 0; i < size; i++) {
            start = (start << 8) | (data[p + 3 + i] & 255);
            end = (end << 8) | (data[p + 3 + size + i] & 255);
        }
        long base = p + 3L + (count + 1L) * size - 1;
        return start >= 1 && end > start && base + end <= data.length
                ? new String(data, (int) (base + start), (int) (end - start), StandardCharsets.US_ASCII) : font.fontName;
    }

    /** Removes only the restored registrations; unrelated constructor code stays. */
    void filterStatements(MethodBody body, List<GraphTargetItem> items) {
        if (scriptCleanup != null) {
            scriptCleanup.filterStatements(body, items);
        }
        List<Text> definitions = constructors.get(body);
        if (definitions == null) {
            return;
        }
        Set<String> names = new HashSet<>();
        for (Text text : definitions) {
            if (restored.contains(text)) {
                names.add(text.name);
            }
        }
        filter(items, names, definitions, completeConstructors.contains(body) && definitions.stream().allMatch(restored::contains));
    }

    void cleanupScripts(java.util.List<java.io.File> files, java.io.File directory) throws IOException, InterruptedException {
        if (scriptCleanup != null) {
            scriptCleanup.cleanupSources(files, directory);
        }
    }

    private void filter(List<GraphTargetItem> items, Set<String> names, List<Text> definitions, boolean complete) {
        if (items == null) {
            return;
        }
        items.removeIf(item -> {
            if (!(item instanceof CallPropertyAVM2Item)) {
                return false;
            }
            CallPropertyAVM2Item call = (CallPropertyAVM2Item) item;
            String method = name(call.propertyName);
            Text candidate = read(call);
            boolean registration = candidate != null && definitions.stream().anyMatch(text -> restored.contains(text)
                    && text.name.equals(candidate.name) && text.scene.equals(candidate.scene)
                    && text.firstFrame == candidate.firstFrame && text.lastFrame == candidate.lastFrame
                    && text.markup.equals(candidate.markup) && java.util.Arrays.equals(text.bounds, candidate.bounds));
            return registration || manager(call.receiver) && complete && "addInstanceComplete".equals(method)
                    || motionTarget(call)
                    && names.contains(string(call.arguments.get(1)));
        });
        for (GraphTargetItem item : items) {
            if (item instanceof Block) {
                for (List<GraphTargetItem> sub : ((Block) item).getSubs()) {
                    filter(sub, names, definitions, complete);
                }
            }
        }
    }
}
