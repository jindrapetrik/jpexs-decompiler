/*
 *  Copyright (C) 2026 JPEXS, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 */
package com.jpexs.decompiler.flash;

import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import com.jpexs.decompiler.flash.abc.types.ConvertData;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitClass;
import com.jpexs.decompiler.flash.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.exporters.script.MxmlExporter;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilderFactory;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class MxmlAdvancedExporterTest {

    private static final String FX = "http://ns.adobe.com/mxml/2009";
    private static final String SPARK = "library://ns.adobe.com/flex/spark";

    private SWF input(String name) throws Exception {
        try (InputStream stream = Files.newInputStream(Paths.get("testdata/mxml/" + name + ".swf"))) {
            return new SWF(stream, false);
        }
    }

    private Document xml(Path path) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory.newDocumentBuilder().parse(path.toFile());
    }

    private Element id(Document document, String name) {
        NodeList elements = document.getElementsByTagName("*");
        for (int i = 0; i < elements.getLength(); i++) {
            Element element = (Element) elements.item(i);
            if (name.equals(element.getAttribute("id"))) {
                return element;
            }
        }
        throw new AssertionError("Missing ID " + name);
    }

    private String text(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    private ScriptPack pack(SWF swf, String name) {
        return swf.getAS3Packs().stream().filter(value -> name.equals(value.getClassPath().toRawString())).findFirst().get();
    }

    private void remove(Path directory) throws IOException {
        try (Stream<Path> files = Files.walk(directory)) {
            for (Path file : files.sorted(Comparator.reverseOrder()).collect(Collectors.toList())) {
                Files.delete(file);
            }
        }
    }

    @Test
    public void recoversXmlVectorsModelsAndLocalizedResources() throws Exception {
        Path output = Files.createTempDirectory("mxml-data-resources-");
        try {
            Path main = new MxmlExporter().exportMxml(input("DataAndResources"), output.toFile()).toPath();
            Document document = xml(main);
            Element catalog = id(document, "catalog");
            Assert.assertEquals(catalog.getLocalName(), "XML");
            Assert.assertEquals(catalog.getElementsByTagNameNS("urn:showcase", "item").getLength(), 2);
            Assert.assertTrue(catalog.getTextContent().contains("First & best"));
            Assert.assertEquals(id(document, "entries").getElementsByTagName("entry").getLength(), 2);
            Element numbers = id(document, "numbers");
            Assert.assertEquals(numbers.getAttribute("type"), "int");
            Assert.assertEquals(numbers.getAttribute("fixed"), "true");
            Assert.assertEquals(numbers.getElementsByTagNameNS(FX, "int").getLength(), 3);
            Assert.assertEquals(id(document, "products").getAttribute("type"), "model.Product");
            Assert.assertEquals(id(document, "products").getElementsByTagNameNS("model.*", "Product").getLength(), 2);
            Assert.assertEquals(id(document, "configuration").getAttribute("title"), "Data declarations");
            Assert.assertEquals(id(document, "launchDate").getAttribute("fullYear"), "2026");
            Assert.assertEquals(id(document, "localizedTitle").getAttribute("text"), "@Resource(bundle='showcase', key='title')");
            Assert.assertEquals(id(document, "localizedImage").getAttribute("source"), "@Resource(bundle='showcase', key='icon')");
            Document config = xml(output.resolve("DataAndResources-config.xml"));
            NodeList locales = config.getElementsByTagName("locale-element");
            Assert.assertEquals(locales.getLength(), 2);
            Assert.assertEquals(locales.item(0).getTextContent(), "en_US");
            Assert.assertEquals(locales.item(1).getTextContent(), "en_GB");
            Assert.assertEquals(config.getElementsByTagName("path-element").item(0).getTextContent(), "./locale/{locale}");
            Assert.assertEquals(config.getElementsByTagName("path-element").item(1).getTextContent(), ".");
            for (String locale : new String[]{"en_US", "en_GB"}) {
                Properties bundle = new Properties();
                try (InputStream stream = Files.newInputStream(output.resolve("locale/" + locale + "/showcase.properties"))) {
                    bundle.load(stream);
                }
                Assert.assertTrue(bundle.getProperty("escaped").startsWith("Owner: Alex\nSecond line"));
                Assert.assertTrue(bundle.getProperty("unicode").contains("★"));
                Assert.assertEquals(bundle.getProperty("productType"), "ClassReference(\"model.Product\")");
                String icon = bundle.getProperty("icon");
                Assert.assertTrue(icon.startsWith("ClassReference(\""));
                String type = icon.substring("ClassReference(\"".length(), icon.length() - 2);
                Assert.assertTrue(Files.isRegularFile(output.resolve(type.replace('.', '/') + ".as")));
            }
            try (Stream<Path> files = Files.list(output.resolve("_assets"))) {
                Assert.assertTrue(files.anyMatch(path -> path.toString().endsWith(".png")));
            }
        } finally {
            remove(output);
        }
    }

    @Test
    public void recoversScalarClassAndFunctionDeclarationsWithoutNumericOrDirectiveLoss() throws Exception {
        Path output = Files.createTempDirectory("mxml-scalar-types-");
        try {
            Document document = xml(new MxmlExporter().exportMxml(input("DataTypes"), output.toFile()).toPath());
            Assert.assertEquals(id(document, "count").getLocalName(), "int");
            Assert.assertEquals(id(document, "bitMask").getLocalName(), "uint");
            Assert.assertEquals(id(document, "bitMask").getTextContent(), "4294967295");
            Assert.assertEquals(id(document, "ratio").getTextContent(), "1.5");
            Assert.assertEquals(id(document, "enabledFlag").getTextContent(), "true");
            Assert.assertTrue(id(document, "literal").getTextContent().contains("\\{braces}"));
            Assert.assertTrue(id(document, "literal").getTextContent().contains("C:\\demo"));
            Assert.assertTrue(id(document, "directive").getTextContent().startsWith("\\@Embed"));
            Assert.assertEquals(id(document, "productClass").getTextContent(), "model.Product");
            Assert.assertEquals(id(document, "callback").getTextContent(), "this.describe");
            Assert.assertTrue(Files.isRegularFile(output.resolve("model/Product.as")));
        } finally {
            remove(output);
        }
    }

    @Test
    public void recoversCompactChildrenPropertiesStylesEffectsEventsVectorsAndBindings() throws Exception {
        Path output = Files.createTempDirectory("mxml-compact-descriptors-");
        try {
            Path main = new MxmlExporter().exportMxml(input("OptimizedShowcase"), output.toFile()).toPath();
            Document document = xml(main);
            Assert.assertEquals(document.getDocumentElement().getAttribute("width"), "640");
            Assert.assertEquals(document.getDocumentElement().getAttribute("backgroundColor"), "15398143");
            Assert.assertEquals(id(document, "container").getElementsByTagNameNS(SPARK, "VerticalLayout").getLength(), 1);
            Assert.assertEquals(id(document, "optimizedLabel").getAttribute("color"), "2245734");
            Assert.assertEquals(id(document, "optimizedLabel").getElementsByTagNameNS(SPARK, "Fade").getLength(), 1);
            Assert.assertFalse(id(document, "optimizedButton").getAttribute("click").isEmpty());
            Assert.assertEquals(id(document, "optimizedList").getElementsByTagNameNS(FX, "Object").getLength(), 2);
            Assert.assertEquals(id(document, "points").getAttribute("type"), "flash.geom.Point");
            Assert.assertEquals(id(document, "points").getElementsByTagNameNS("flash.geom.*", "Point").getLength(), 1);
            Element binding = (Element) document.getElementsByTagNameNS(FX, "Binding").item(0);
            Assert.assertEquals(binding.getAttribute("source"), "message");
            Assert.assertEquals(binding.getAttribute("destination"), "optimizedLabel.text");
            Element added = id(document, "stateLabel");
            Assert.assertEquals(added.getAttribute("includeIn"), "expanded");
            Assert.assertEquals(added.getParentNode(), id(document, "container"));
            Assert.assertEquals(((Element) added.getPreviousSibling().getPreviousSibling()).getAttribute("id"), "optimizedButton");
            Assert.assertFalse(text(main).contains("generateMXML"));
            Assert.assertFalse(text(main).contains("setupBindings"));
        } finally {
            remove(output);
        }
    }

    @Test
    public void recognizesAndRecoversCompactGettersInReferencedComponents() throws Exception {
        Path output = Files.createTempDirectory("mxml-compact-getters-");
        try {
            new MxmlExporter().exportMxml(input("CompactHost"), output.toFile());
            Path component = output.resolve("components/CompactPanel.mxml");
            Document document = xml(component);
            Assert.assertEquals(document.getDocumentElement().getLocalName(), "Group");
            Assert.assertEquals(document.getDocumentElement().getAttribute("width"), "280");
            Assert.assertEquals(document.getDocumentElement().getAttribute("height"), "80");
            Assert.assertEquals(id(document, "compactLabel").getAttribute("text"), "A nested component with compact getters");
            Assert.assertFalse(text(component).contains("MXMLDescriptor"));
            Assert.assertFalse(text(component).contains("compactDescriptor"));
            Assert.assertFalse(Files.exists(output.resolve("components/CompactPanel.as")));
        } finally {
            remove(output);
        }
    }

    @Test
    public void recoversStateEventsXmlBindingsServicesCssAndUserNamespaces() throws Exception {
        Path output = Files.createTempDirectory("mxml-advanced-semantics-");
        try {
            Path main = new MxmlExporter().exportMxml(input("AdvancedMxml"), output.toFile()).toPath();
            Document document = xml(main);
            Assert.assertEquals(document.getDocumentElement().getAttribute("implements"), "interfaces.IShowcase");
            Assert.assertEquals(document.getDocumentElement().getAttribute("backgroundColor.alternate"), "15660287");
            Assert.assertEquals(id(document, "alternateState").getAttribute("name"), "alternate");
            Assert.assertTrue(Files.isRegularFile(output.resolve("interfaces/IShowcase.as")));
            Assert.assertEquals(id(document, "stateButton").getAttribute("click"), "normalClick()");
            Assert.assertEquals(id(document, "stateButton").getAttribute("click.alternate"), "alternateClick()");
            Assert.assertEquals(id(document, "policyLabel").getAttribute("itemCreationPolicy"), "immediate");
            Assert.assertEquals(id(document, "policyLabel").getAttribute("itemDestructionPolicy"), "auto");
            String code = text(main);
            Assert.assertFalse(code.contains("§§namespace"));
            Assert.assertTrue(code.contains("showcase function qualifiedCaption"));
            Assert.assertTrue(code.contains("showcase::qualifiedCaption"));
            Assert.assertTrue(code.contains("param1:spark.components.Label, param2:mx.controls.Label"));
            Assert.assertFalse(code.contains("_typeRef1"));
            Assert.assertTrue(code.contains("@namespace c0 \"components.*\""));
            Assert.assertTrue(code.contains("*.outer s|Label"));
            Assert.assertTrue(code.contains("c0|StatusBadge"));
            Assert.assertEquals(id(document, "service").getAttribute("request"), "{_AdvancedMxml_Object1}");
            Assert.assertTrue(code.contains("public var _AdvancedMxml_Object1:Object = {"));
            NodeList bindings = document.getElementsByTagNameNS(FX, "Binding");
            Assert.assertEquals(bindings.getLength(), 5);
            Assert.assertEquals(((Element) bindings.item(0)).getAttribute("destination"), "dynamicCatalog.person[0].@code");
            Assert.assertFalse(code.contains("@ns1::"));
            Assert.assertTrue(code.contains("private namespace recoveredXmlNamespace1 = \"urn:showcase:data\""));
            Assert.assertEquals(((Element) bindings.item(2)).getAttribute("destination"),
                    "namespacedCatalog.recoveredXmlNamespace1::person[0].@recoveredXmlNamespace1::code");
            try (Stream<Path> paths = Files.walk(output)) {
                boolean font = false;
                for (Path path : paths.filter(file -> file.toString().endsWith(".as")).collect(Collectors.toList())) {
                    font |= text(path).contains("fontName=\"RecoveredShowcaseFont\"");
                }
                Assert.assertTrue(font);
            }
        } finally {
            remove(output);
        }
    }

    @DataProvider(name = "invalidCompactCounts")
    public Object[][] invalidCompactCounts() {
        return new Object[][]{{-1}, {127}};
    }

    @Test
    public void recoversRepeatersDeferredViewsAndRpcOperationAliases() throws Exception {
        Path output = Files.createTempDirectory("mxml-legacy-features-");
        try {
            Path main = new MxmlExporter().exportMxml(input("LegacyShowcase"), output.toFile()).toPath();
            Document document = xml(main);
            String mx = "library://ns.adobe.com/flex/mx";
            Assert.assertEquals(document.getDocumentElement().getAttribute("layout"), "vertical");
            Assert.assertEquals(id(document, "rows").getAttribute("dataProvider"), "{records}");
            Assert.assertEquals(id(document, "rowButton").getAttribute("label"),
                    "{\"rows.mx_internal::getItemAt(param2[0]) \" + rows.currentIndex + \": \" + rows.currentItem.name}");
            Assert.assertEquals(id(document, "nestedRows").getAttribute("dataProvider"), "{rows.currentItem.children}");
            Assert.assertEquals(id(document, "nestedLabel").getAttribute("text"),
                    "{rows.currentItem.name + \": \" + nestedRows.currentIndex + \": \" + nestedRows.currentItem.name}");
            Assert.assertEquals(id(document, "rowButton").getParentNode(), id(document, "rows"));
            Assert.assertEquals(id(document, "pages").getElementsByTagNameNS(mx, "Canvas").getLength(), 2);
            Assert.assertEquals(id(document, "pages").getElementsByTagNameNS(mx, "DateChooser").getLength(), 1);
            Assert.assertEquals(id(document, "remote").getElementsByTagNameNS(mx, "method").getLength(), 1);
            Assert.assertEquals(id(document, "service").getElementsByTagNameNS(mx, "operation").getLength(), 1);
            Assert.assertEquals(id(document, "standaloneOperation").getLocalName(), "RemoteObjectOperation");
            Assert.assertTrue(text(main).contains("getRepeaterItem().name"));
            Assert.assertFalse(text(main).contains("RepeatableBinding"));
        } finally {
            remove(output);
        }
    }

    @Test(dataProvider = "invalidCompactCounts")
    public void rejectsInvalidCompactCountsBeforeWritingFiles(int count) throws Exception {
        Path output = Files.createTempDirectory("mxml-compact-invalid-");
        try {
            SWF swf = input("OptimizedShowcase");
            ScriptPack main = pack(swf, "OptimizedShowcase");
            int cls = ((TraitClass) main.getPublicTrait()).class_info;
            MethodBody constructor = main.abc.findBody(main.abc.instance_info.get(cls).iinit_index);
            boolean changed = false;
            for (AVM2Instruction instruction : constructor.getCode().code) {
                if (instruction.definition.instructionCode == AVM2Instructions.PushByte && instruction.operands[0] == 3) {
                    instruction.operands[0] = count;
                    changed = true;
                    break;
                }
            }
            Assert.assertTrue(changed);
            try {
                new MxmlExporter().exportMxml(swf, output.toFile());
                Assert.fail("Malformed compact counts must be rejected");
            } catch (IOException ex) {
                Assert.assertTrue(ex.getMessage().contains("compact descriptor count"), ex.getMessage());
            }
            try (Stream<Path> files = Files.list(output)) {
                Assert.assertEquals(files.count(), 0L);
            }
        } finally {
            remove(output);
        }
    }

    @Test
    public void recoversLegacyStateInheritanceAndComplexOverrides() throws Exception {
        Path output = Files.createTempDirectory("mxml-legacy-states-");
        try {
            Path main = new MxmlExporter().exportMxml(input("LegacyStates"), output.toFile()).toPath();
            Document document = xml(main);
            Assert.assertEquals(document.getDocumentElement().getAttribute("currentState"), "");
            Assert.assertEquals(id(document, "heading").getAttribute("text.expanded"), "{caption}");
            Assert.assertEquals(id(document, "heading").getAttribute("text.detailed"), "Detailed inherited legacy state");
            Assert.assertEquals(id(document, "heading").getAttribute("color.detailed"), "2245734");
            Assert.assertEquals(id(document, "heading").getElementsByTagNameNS("flash.filters.*", "DropShadowFilter").getLength(), 2);
            Assert.assertEquals(id(document, "stateLabel").getAttribute("includeIn"), "expanded,detailed");
            Assert.assertEquals(id(document, "stateLabel").getAttribute("itemCreationPolicy"), "immediate");
            Assert.assertEquals(id(document, "stateLabel").getParentNode(), id(document, "content"));
            Assert.assertEquals(id(document, "baseLabel").getAttribute("excludeFrom"), "expanded,detailed");
            Assert.assertFalse(text(main).contains("basedOn="));
            Assert.assertFalse(text(main).contains("DeferredInstanceFromFunction"));
        } finally {
            remove(output);
        }
    }

    @Test
    public void rejectsCyclicLegacyStateInheritanceBeforeWritingFiles() throws Exception {
        Path output = Files.createTempDirectory("mxml-legacy-state-cycle-");
        try {
            SWF swf = input("LegacyStates");
            ScriptPack main = pack(swf, "LegacyStates");
            int cls = ((TraitClass) main.getPublicTrait()).class_info;
            boolean changed = false;
            for (Trait trait : main.abc.instance_info.get(cls).instance_traits.traits) {
                if (trait instanceof TraitMethodGetterSetter && "_LegacyStates_State2_c".equals(main.abc.constants.getString(trait.getName(main.abc).name_index))) {
                    MethodBody body = main.abc.findBody(((TraitMethodGetterSetter) trait).method_info);
                    for (AVM2Instruction instruction : body.getCode().code) {
                        if (instruction.definition.instructionCode == AVM2Instructions.PushString
                                && "expanded".equals(main.abc.constants.getString(instruction.operands[0]))) {
                            instruction.operands[0] = main.abc.constants.getStringId("detailed", false);
                            changed = true;
                        }
                    }
                }
            }
            Assert.assertTrue(changed);
            try {
                new MxmlExporter().exportMxml(swf, output.toFile());
                Assert.fail("Cyclic legacy states must be rejected");
            } catch (IOException ex) {
                Assert.assertTrue(ex.getMessage().contains("Cyclic legacy state inheritance"), ex.getMessage());
            }
            try (Stream<Path> files = Files.list(output)) {
                Assert.assertEquals(files.count(), 0L);
            }
        } finally {
            remove(output);
        }
    }

    @Test
    public void escapesPropertiesWithoutChangingTheirValues() throws Exception {
        Path output = Files.createTempDirectory("mxml-properties-escaping-");
        try {
            SWF swf = input("DataAndResources");
            ScriptPack bundle = pack(swf, "en_US$showcase_properties");
            String value = " \tOwner: Alex\r\n\\path=#value! \u0001 ★";
            int index = bundle.abc.constants.getStringId("Owner: Alex\nSecond line", false);
            Assert.assertTrue(index > 0);
            bundle.abc.constants.setString(index, value);
            new MxmlExporter().exportMxml(swf, output.toFile());
            Properties properties = new Properties();
            try (InputStream stream = Files.newInputStream(output.resolve("locale/en_US/showcase.properties"))) {
                properties.load(stream);
            }
            Assert.assertEquals(properties.getProperty("escaped"), value);
        } finally {
            remove(output);
        }
    }

    @Test
    public void failsBeforeWritingIfResourceContentCannotBeRecovered() throws Exception {
        Path output = Files.createTempDirectory("mxml-properties-invalid-");
        try {
            SWF swf = input("DataAndResources");
            ScriptPack bundle = pack(swf, "en_US$showcase_properties");
            bundle.abc.constants.setString(bundle.abc.constants.getStringId("getContent", false), "unrecognizedContent");
            try {
                new MxmlExporter().exportMxml(swf, output.toFile());
                Assert.fail("Unsupported resource factories must not produce partial output");
            } catch (IOException ex) {
                Assert.assertTrue(ex.getMessage().contains("resource content factory"), ex.getMessage());
            }
            try (Stream<Path> files = Files.list(output)) {
                Assert.assertEquals(files.count(), 0L);
            }
        } finally {
            remove(output);
        }
    }

    @Test
    public void recognizesDirectReturnBootstrapInformation() throws Exception {
        Path output = Files.createTempDirectory("mxml-direct-bootstrap-");
        try {
            SWF swf = input("DataTypes");
            ScriptPack bootstrap = pack(swf, swf.getDocumentClass());
            int cls = ((TraitClass) bootstrap.getPublicTrait()).class_info;
            MethodBody info = null;
            for (Trait trait : bootstrap.abc.instance_info.get(cls).instance_traits.traits) {
                if (trait instanceof TraitMethodGetterSetter && "info".equals(bootstrap.abc.constants.getString(trait.getName(bootstrap.abc).name_index))) {
                    info = bootstrap.abc.findBody(((TraitMethodGetterSetter) trait).method_info);
                }
            }
            Assert.assertNotNull(info);
            AVM2Code direct = new AVM2Code();
            direct.code.add(new AVM2Instruction(0, AVM2Instructions.GetLocal0, new int[0]));
            direct.code.add(new AVM2Instruction(0, AVM2Instructions.PushScope, new int[0]));
            boolean copying = false;
            boolean completed = false;
            for (AVM2Instruction instruction : info.getCode().code) {
                if (instruction.definition.instructionCode == AVM2Instructions.PushString
                        && "compiledLocales".equals(bootstrap.abc.constants.getString(instruction.operands[0]))) {
                    copying = true;
                }
                if (copying && instruction.definition.instructionCode == AVM2Instructions.InitProperty) {
                    completed = true;
                    break;
                }
                if (copying) {
                    direct.code.add(instruction.clone());
                }
            }
            Assert.assertTrue(completed);
            direct.code.add(new AVM2Instruction(0, AVM2Instructions.ReturnValue, new int[0]));
            long address = 0;
            for (AVM2Instruction instruction : direct.code) {
                instruction.setAddress(address);
                address += instruction.getBytesLength();
            }
            info.setCode(direct);
            Assert.assertEquals(swf.getFlexMainClass(new ArrayList<>(), new ArrayList<>()), "DataTypes");
            Assert.assertEquals(new MxmlExporter().exportMxml(swf, output.toFile()).getName(), "DataTypes.mxml");
        } finally {
            remove(output);
        }
    }

    @Test
    public void normalAs3DecompilerResolvesClassMemberNamespaces() throws Exception {
        SWF swf = input("AdvancedMxml");
        ScriptPack main = pack(swf, "AdvancedMxml");
        HighlightedTextWriter writer = new HighlightedTextWriter(Configuration.getCodeFormatting(), false);
        main.toSource(swf.getAbcIndex(), writer, main.abc.script_info.get(main.scriptIndex).traits.traits,
                new ConvertData(), ScriptExportMode.AS, false, false, false);
        String source = writer.toString();
        Assert.assertFalse(source.contains("§§namespace"));
        Assert.assertTrue(source.contains("showcase function qualifiedCaption"));
        Assert.assertTrue(source.contains("showcase::qualifiedCaption"));
    }
}
