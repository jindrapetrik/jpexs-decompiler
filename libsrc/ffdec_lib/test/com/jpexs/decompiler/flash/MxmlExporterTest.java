/*
 *  Copyright (C) 2026 JPEXS, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 */
package com.jpexs.decompiler.flash;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import com.jpexs.decompiler.flash.abc.types.ConvertData;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.traits.TraitClass;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.exporters.script.MxmlExporter;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.tags.ExportAssetsTag;
import com.jpexs.decompiler.flash.tags.DefineFont4Tag;
import com.jpexs.decompiler.flash.tags.DefineScalingGridTag;
import com.jpexs.decompiler.flash.tags.SymbolClassTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import java.awt.image.BufferedImage;
import com.jpexs.helpers.ByteArrayRange;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.imageio.ImageIO;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class MxmlExporterTest {

    private static final String SPARK = "library://ns.adobe.com/flex/spark";

    private SWF input() throws Exception {
        return input("Main");
    }

    private SWF input(String name) throws Exception {
        // Deliberately read only the SWF. The original MXML is not needed.
        try (InputStream stream = new BufferedInputStream(Files.newInputStream(Paths.get("testdata/mxml/" + name + ".swf")))) {
            return new SWF(stream, false);
        }
    }

    private ABC mainAbc(SWF swf) {
        for (ScriptPack pack : swf.getAS3Packs()) {
            if ("Main".equals(pack.getClassPath().toRawString())) {
                return pack.abc;
            }
        }
        throw new AssertionError("Missing Main class");
    }

    private Document xml(Path file) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory.newDocumentBuilder().parse(file.toFile());
    }

    private Element element(Document xml, String name) {
        Assert.assertEquals(xml.getElementsByTagNameNS(SPARK, name).getLength(), 1);
        return (Element) xml.getElementsByTagNameNS(SPARK, name).item(0);
    }

    private Element id(Document document, String id) {
        NodeList elements = document.getElementsByTagName("*");
        for (int i = 0; i < elements.getLength(); i++) {
            Element element = (Element) elements.item(i);
            if (id.equals(element.getAttribute("id"))) {
                return element;
            }
        }
        throw new AssertionError("Missing component ID " + id);
    }

    @DataProvider(name = "documentRoots")
    public Object[][] documentRoots() {
        String mx = "library://ns.adobe.com/flex/mx";
        return new Object[][]{
            {"DesktopMain", SPARK, "WindowedApplication", "desktopLabel", "{message}", new String[0]},
            {"MxDesktopMain", mx, "WindowedApplication", "mxDesktopLabel", "{message}", new String[0]},
            {"FeatureModule", SPARK, "Module", "moduleLabel", "{caption}", new String[0]},
            {"MxFeatureModule", mx, "Module", "mxModuleLabel", "{caption}", new String[0]},
            {"MxApplicationMain", mx, "Application", "mxApplicationLabel", "{message}", new String[0]},
            {"InheritedApplication", "app.*", "ApplicationBase", "inheritedLabel", "{formatCaption()}",
                new String[]{"app/ApplicationBase.as", "app/ApplicationAncestor.as"}},
            {"MxmlInheritedApplication", "app.*", "VisualApplicationBase", "mxmlInheritedLabel", "{baseCaption}",
                new String[]{"app/VisualApplicationBase.mxml"}},
            {"InheritedModule", "app.*", "FeatureModuleBase", "inheritedModuleLabel", "{caption}",
                new String[]{"app/FeatureModuleBase.as"}}
        };
    }

    @Test(dataProvider = "documentRoots")
    public void recoversApplicationAndModuleRoots(String name, String namespace, String rootType, String label,
            String expression, String[] dependencies) throws Exception {
        Path output = Files.createTempDirectory("mxml-document-root-");
        try {
            SWF swf = input(name);
            ArrayList<String> ignored = new ArrayList<>();
            Assert.assertEquals(swf.getFlexMainClass(ignored, new ArrayList<>()), name);
            Assert.assertTrue(ignored.contains(swf.getDocumentClass()));
            Path file = new MxmlExporter().exportMxml(swf, output.toFile()).toPath();
            Assert.assertEquals(file.getFileName().toString(), name + ".mxml");
            Document document = xml(file);
            Assert.assertEquals(document.getDocumentElement().getNamespaceURI(), namespace);
            Assert.assertEquals(document.getDocumentElement().getLocalName(), rootType);
            Assert.assertEquals(id(document, label).getAttribute("text"), expression);
            Set<String> expected = new HashSet<>(Arrays.asList(dependencies));
            expected.add(name + ".mxml");
            try (Stream<Path> paths = Files.walk(output)) {
                Assert.assertEquals(paths.filter(Files::isRegularFile)
                        .map(path -> output.relativize(path).toString().replace('\\', '/')).collect(Collectors.toSet()), expected);
            }
            String text = new String(Files.readAllBytes(file), java.nio.charset.StandardCharsets.UTF_8);
            Assert.assertFalse(text.contains("UIComponentDescriptor"));
            Assert.assertFalse(text.contains("_documentDescriptor_"));
            if ("MxFeatureModule".equals(name)) {
                Assert.assertEquals(id(document, "moduleBox").getAttribute("width"), "100%");
                Assert.assertEquals(id(document, "moduleBox").getAttribute("verticalGap"), "12");
                Assert.assertSame(id(document, label).getParentNode(), id(document, "moduleBox"));
                Assert.assertEquals(id(document, "mxModuleButton").getAttribute("click"), "updateCaption()");
                Assert.assertEquals(id(document, "mxModuleButton").getAttribute("color"), "2245734");
                Assert.assertEquals(document.getDocumentElement().getAttribute("backgroundColor"), "15398143");
            }
            if ("MxmlInheritedApplication".equals(name)) {
                Document parent = xml(output.resolve("app/VisualApplicationBase.mxml"));
                Assert.assertEquals(parent.getDocumentElement().getLocalName(), "Application");
                Assert.assertEquals(parent.getDocumentElement().getAttribute("backgroundColor"), "15398143");
                Assert.assertTrue(parent.getElementsByTagNameNS("http://ns.adobe.com/mxml/2009", "Script")
                        .item(0).getTextContent().contains("public var baseCaption:String = \"Visual application base\""));
            }
            if ("InheritedApplication".equals(name)) {
                String ancestor = new String(Files.readAllBytes(output.resolve("app/ApplicationAncestor.as")), java.nio.charset.StandardCharsets.UTF_8);
                Assert.assertTrue(ancestor.contains("extends Application"));
                Assert.assertTrue(ancestor.contains("Custom AS3 ancestor"));
                // The Flex compiler generates this table again when compiling
                // an AS3 class derived from a skinnable application.
                Assert.assertFalse(ancestor.contains("_skinParts"));
            }
        } finally {
            remove(output);
        }
    }

    private void useDirectDocument(SWF swf, String name) {
        for (SymbolClassTag symbols : swf.getTags().toArrayList().stream().filter(tag -> tag instanceof SymbolClassTag)
                .map(tag -> (SymbolClassTag) tag).collect(Collectors.toList())) {
            int index = symbols.tags.indexOf(0);
            if (index >= 0) {
                symbols.names.set(index, name);
                return;
            }
        }
        throw new AssertionError("Missing document class linkage");
    }

    @Test
    public void recoversModuleWithoutBootstrapDocumentClass() throws Exception {
        Path output = Files.createTempDirectory("mxml-direct-module-");
        try {
            SWF swf = input("FeatureModule");
            useDirectDocument(swf, "FeatureModule");
            Path file = new MxmlExporter().exportMxml(swf, output.toFile()).toPath();
            Document document = xml(file);
            Assert.assertEquals(document.getDocumentElement().getLocalName(), "Module");
            Assert.assertEquals(id(document, "moduleLabel").getAttribute("text"), "{caption}");
        } finally {
            remove(output);
        }
    }

    @DataProvider(name = "invalidRootParents")
    public Object[][] invalidRootParents() {
        return new Object[][]{{"cycle"}, {"missing"}, {"nonFlex"}};
    }

    @Test(dataProvider = "invalidRootParents")
    public void rejectsInvalidRootAncestryBeforeWritingFiles(String kind) throws Exception {
        Path output = Files.createTempDirectory("mxml-invalid-root-");
        try {
            SWF swf = input("FeatureModule");
            useDirectDocument(swf, "FeatureModule");
            ScriptPack main = swf.getAS3Packs().stream().filter(pack -> "FeatureModule".equals(pack.getClassPath().toRawString())).findFirst().get();
            int index = ((TraitClass) main.getPublicTrait()).class_info;
            main.abc.instance_info.get(index).super_index = "cycle".equals(kind) ? main.abc.instance_info.get(index).name_index
                    : main.abc.constants.getQnameId("nonFlex".equals(kind) ? "Sprite" : "MissingBase",
                            Namespace.KIND_PACKAGE, "nonFlex".equals(kind) ? "flash.display" : "app", true);
            try {
                new MxmlExporter().exportMxml(swf, output.toFile());
                Assert.fail("Invalid root ancestry must be rejected");
            } catch (IOException ex) {
                Assert.assertTrue(ex.getMessage().contains("Application base class"), ex.getMessage());
            }
            try (Stream<Path> paths = Files.list(output)) {
                Assert.assertEquals(paths.count(), 0L);
            }
        } finally {
            remove(output);
        }
    }

    @Test
    public void rejectsUnknownDescriptorFieldsBeforeWritingFiles() throws Exception {
        Path output = Files.createTempDirectory("mxml-unknown-descriptor-");
        try {
            SWF swf = input("MxFeatureModule");
            ScriptPack main = swf.getAS3Packs().stream().filter(pack -> "MxFeatureModule".equals(pack.getClassPath().toRawString())).findFirst().get();
            int field = main.abc.constants.getStringId("propertiesFactory", false);
            Assert.assertTrue(field > 0);
            main.abc.constants.setString(field, "unsupportedDescriptorFactory");
            try {
                new MxmlExporter().exportMxml(swf, output.toFile());
                Assert.fail("Unsupported descriptors must not produce partial MXML");
            } catch (IOException ex) {
                Assert.assertTrue(ex.getMessage().contains("Descriptor field unsupportedDescriptorFactory"), ex.getMessage());
            }
            try (Stream<Path> paths = Files.list(output)) {
                Assert.assertEquals(paths.count(), 0L);
            }
        } finally {
            remove(output);
        }
    }

    @Test(dataProvider = "assetNames")
    public void staticInitializerHonorsSkinPartVisibility(boolean automatic) throws Exception {
        boolean old = Configuration.handleSkinPartsAutomatically.get();
        try {
            Configuration.handleSkinPartsAutomatically.set(automatic);
            SWF swf = input("InheritedApplication");
            ScriptPack ancestor = swf.getAS3Packs().stream()
                    .filter(pack -> "app.ApplicationAncestor".equals(pack.getClassPath().toRawString())).findFirst().get();
            HighlightedTextWriter writer = new HighlightedTextWriter(Configuration.getCodeFormatting(), false);
            ancestor.toSource(swf.getAbcIndex(), writer, ancestor.abc.script_info.get(ancestor.scriptIndex).traits.traits,
                    new ConvertData(), ScriptExportMode.AS, false, false, false);
            Assert.assertEquals(writer.toString().contains("private static var _skinParts"), !automatic);
        } finally {
            Configuration.handleSkinPartsAutomatically.set(old);
        }
    }

    @DataProvider(name = "assetNames")
    public Object[][] assetNames() {
        return new Object[][]{{false}, {true}};
    }

    @Test(dataProvider = "assetNames")
    public void exportsEmbeddedMediaAndCustomSkins(boolean classNamesOnly) throws Exception {
        boolean oldNames = Configuration.as3ExportNamesUseClassNamesOnly.get();
        Path output = Files.createTempDirectory("mxml-embedded-assets-");
        try {
            Configuration.as3ExportNamesUseClassNamesOnly.set(classNamesOnly);
            SWF source = input("AssetsAndSkins");
            Path main = new MxmlExporter().exportMxml(source, output.toFile()).toPath();
            Document document = xml(main);
            Assert.assertTrue(id(document, "gridImage").getAttribute("source").startsWith("{"));
            String css = document.getElementsByTagNameNS("http://ns.adobe.com/mxml/2009", "Style").item(0).getTextContent();
            Assert.assertTrue(css.contains("skinClass: ClassReference(\"skins.ShowcaseButtonSkin\")"));
            // A CSS Embed is a Class-valued field; ClassReference must name its
            // generated asset class, not the private field holding the class.
            Matcher references = Pattern.compile("ClassReference\\(\"([^\"]+)\"\\)").matcher(css);
            while (references.find()) {
                String name = references.group(1);
                Assert.assertTrue(Files.exists(output.resolve(name.replace('.', '/') + ".as"))
                        || Files.exists(output.resolve(name.replace('.', '/') + ".mxml")), name);
            }
            assertSkin(output, "skins/ShowcaseButtonSkin.mxml", "spark.components.Button");
            Document skin = assertSkin(output, "skins/ShowcaseControlSkin.mxml", "components.ShowcaseControl");
            Assert.assertEquals(id(skin, "labelDisplay").getAttribute("text"), "{hostComponent.caption}");
            String control = new String(Files.readAllBytes(output.resolve("components/ShowcaseControl.mxml")), java.nio.charset.StandardCharsets.UTF_8);
            Assert.assertTrue(control.contains("[SkinPart(required=\"true\")]"));
            Assert.assertTrue(control.contains("[SkinPart(required=\"false\")]"));
            Assert.assertTrue(control.contains("[SkinState(\"normal\")]"));
            Assert.assertTrue(control.contains("[Event(name=\"activated\",type=\"flash.events.Event\")]"));
            Set<Path> referencedAssets = new HashSet<>();
            try (Stream<Path> paths = Files.walk(output)) {
                for (Path path : paths.filter(p -> p.toString().endsWith(".as")).collect(Collectors.toList())) {
                    String code = new String(Files.readAllBytes(path), java.nio.charset.StandardCharsets.UTF_8);
                    Matcher embeds = Pattern.compile("\\[Embed\\(source=\"([^\"]+)\"").matcher(code);
                    while (embeds.find()) {
                        Path asset = path.getParent().resolve(embeds.group(1)).normalize();
                        Assert.assertTrue(asset.startsWith(output.resolve("_assets")), embeds.group(1));
                        Assert.assertTrue(Files.size(asset) > 0, asset.toString());
                        referencedAssets.add(asset);
                    }
                }
            }
            try (Stream<Path> paths = Files.list(output.resolve("_assets"))) {
                Assert.assertEquals(paths.collect(Collectors.toSet()), referencedAssets);
            }
            Set<String> extensions = new HashSet<>();
            for (Path asset : referencedAssets) {
                String name = asset.getFileName().toString();
                extensions.add(name.substring(name.lastIndexOf('.') + 1));
                if (name.endsWith(".bin")) {
                    Assert.assertEquals(Files.readAllBytes(asset), Files.readAllBytes(Paths.get("testdata/as3_embed/assets/data.bin")));
                } else if (name.endsWith(".png")) {
                    BufferedImage actual = ImageIO.read(asset.toFile());
                    BufferedImage expected = ((ImageTag) source.getCharacterByClass("AssetsAndSkins_picture")).getImageCached().getBufferedImage();
                    Assert.assertEquals(actual.getWidth(), expected.getWidth());
                    Assert.assertEquals(actual.getHeight(), expected.getHeight());
                    for (int y = 0; y < actual.getHeight(); y++) {
                        for (int x = 0; x < actual.getWidth(); x++) {
                            int actualPixel = actual.getRGB(x, y);
                            int expectedPixel = expected.getRGB(x, y);
                            Assert.assertEquals(actualPixel >>> 24, expectedPixel >>> 24);
                            for (int shift = 0; shift < 24; shift += 8) {
                                // Encoding premultiplied SWF colors as PNG can
                                // round an RGB channel by one unit.
                                Assert.assertTrue(Math.abs(((actualPixel >>> shift) & 255) - ((expectedPixel >>> shift) & 255)) <= 1);
                            }
                        }
                    }
                }
            }
            Assert.assertEquals(extensions, new HashSet<>(Arrays.asList("png", "bin", "mp3", "ttf", "swf")));
            try (InputStream stream = Files.newInputStream(output.resolve("_assets/assets.swf"))) {
                SWF assets = new SWF(stream, false);
                ExportAssetsTag symbols = (ExportAssetsTag) assets.getTags().toArrayList().stream()
                        .filter(tag -> tag instanceof ExportAssetsTag).findFirst().get();
                DefineScalingGridTag grid = (DefineScalingGridTag) assets.getTags().toArrayList().stream()
                        .filter(tag -> tag instanceof DefineScalingGridTag).findFirst().get();
                Assert.assertEquals(grid.splitter.Xmin, 200);
                Assert.assertEquals(grid.splitter.Xmax, 1400);
                Assert.assertEquals(grid.splitter.Ymin, 200);
                Assert.assertEquals(grid.splitter.Ymax, 800);
                Assert.assertTrue(symbols.tags.size() >= 2);
                for (int symbol : symbols.tags) {
                    Assert.assertNotNull(assets.getCharacter(symbol));
                    Set<Integer> needed = new HashSet<>();
                    ((CharacterTag) assets.getCharacter(symbol)).getNeededCharactersDeep(needed, new HashSet<>());
                    for (int dependency : needed) {
                        Assert.assertNotNull(assets.getCharacter(dependency));
                    }
                }
            }
        } finally {
            Configuration.as3ExportNamesUseClassNamesOnly.set(oldNames);
            remove(output);
        }
    }

    private Document assertSkin(Path output, String path, String host) throws Exception {
        Document skin = xml(output.resolve(path));
        Assert.assertEquals(skin.getDocumentElement().getLocalName(), "Skin");
        NodeList metadata = skin.getElementsByTagNameNS("http://ns.adobe.com/mxml/2009", "Metadata");
        Assert.assertEquals(metadata.getLength(), 1);
        String text = metadata.item(0).getTextContent();
        Assert.assertTrue(text.contains("[HostComponent(\"" + host + "\")]"));
        Assert.assertEquals(text.indexOf("[HostComponent"), text.lastIndexOf("[HostComponent"));
        Assert.assertEquals(skin.getElementsByTagNameNS("http://ns.adobe.com/mxml/2009", "Script").getLength(), 0);
        return skin;
    }

    @Test
    public void infersHostAndSkinPartsWithoutRetainedMetadataOrAutomaticSkinPartSetting() throws Exception {
        Path output = Files.createTempDirectory("mxml-stripped-skin-metadata-");
        boolean oldParts = Configuration.handleSkinPartsAutomatically.get();
        try {
            Configuration.handleSkinPartsAutomatically.set(false);
            SWF swf = input("AssetsAndSkins");
            for (ScriptPack pack : swf.getAS3Packs()) {
                if (pack.getClassPath().toRawString().startsWith("skins.")) {
                    pack.getPublicTrait().metadata = new int[0];
                } else if ("components.ShowcaseControl".equals(pack.getClassPath().toRawString())) {
                    int index = ((TraitClass) pack.getPublicTrait()).class_info;
                    pack.abc.instance_info.get(index).instance_traits.traits.forEach(trait -> trait.metadata = Arrays.stream(trait.metadata)
                            .filter(metadata -> !"SkinPart".equals(pack.abc.constants.getString(pack.abc.metadata_info.get(metadata).name_index))).toArray());
                }
            }
            new MxmlExporter().exportMxml(swf, output.toFile());
            assertSkin(output, "skins/ShowcaseControlSkin.mxml", "components.ShowcaseControl");
            assertSkin(output, "skins/ShowcaseButtonSkin.mxml", "spark.components.Button");
            String control = new String(Files.readAllBytes(output.resolve("components/ShowcaseControl.mxml")), java.nio.charset.StandardCharsets.UTF_8);
            Assert.assertTrue(control.contains("[SkinPart(required=\"true\")]"));
            Assert.assertTrue(control.contains("[SkinPart(required=\"false\")]"));
        } finally {
            Configuration.handleSkinPartsAutomatically.set(oldParts);
            remove(output);
        }
    }

    @Test
    public void preservesExistingEmbeddedAssetsWhenOverwriteIsDisabled() throws Exception {
        Path output = Files.createTempDirectory("mxml-asset-overwrite-");
        boolean oldOverwrite = Configuration.overwriteExistingFiles.get();
        try {
            new MxmlExporter().exportMxml(input("AssetsAndSkins"), output.toFile());
            Path asset;
            try (Stream<Path> paths = Files.list(output.resolve("_assets"))) {
                asset = paths.filter(path -> path.toString().endsWith(".bin")).findFirst().get();
            }
            byte[] marker = new byte[]{1, 7, 9};
            Files.write(asset, marker);
            Files.write(output.resolve("AssetsAndSkins.mxml"), marker);
            Configuration.overwriteExistingFiles.set(false);
            new MxmlExporter().exportMxml(input("AssetsAndSkins"), output.toFile());
            Assert.assertEquals(Files.readAllBytes(asset), marker);
            Assert.assertEquals(Files.readAllBytes(output.resolve("AssetsAndSkins.mxml")), marker);
        } finally {
            Configuration.overwriteExistingFiles.set(oldOverwrite);
            remove(output);
        }
    }

    @Test
    public void doesNotWriteEmbeddedMediaWhenSkinReconstructionFails() throws Exception {
        Path output = Files.createTempDirectory("mxml-assets-skin-failure-");
        try {
            SWF swf = input("AssetsAndSkins");
            for (ScriptPack pack : swf.getAS3Packs()) {
                if ("skins.ShowcaseButtonSkin".equals(pack.getClassPath().toRawString())) {
                    int index = ((TraitClass) pack.getPublicTrait()).class_info;
                    AVM2Code code = new AVM2Code();
                    code.code.add(new AVM2Instruction(0, AVM2Instructions.ReturnVoid, new int[0]));
                    pack.abc.findBody(pack.abc.instance_info.get(index).iinit_index).setCode(code);
                }
            }
            try {
                new MxmlExporter().exportMxml(swf, output.toFile());
                Assert.fail("Unsupported skin construction must fail before writing media or sources");
            } catch (IOException ex) {
                Assert.assertTrue(ex.getMessage().contains("No classic MXML constructor"), ex.getMessage());
            }
            try (Stream<Path> paths = Files.list(output)) {
                Assert.assertEquals(paths.count(), 0L);
            }
        } finally {
            remove(output);
        }
    }

    @Test
    public void exportsCffFontDataAndEmbedAttributes() throws Exception {
        Path output = Files.createTempDirectory("mxml-cff-font-");
        try {
            SWF swf = input("AssetsAndSkins");
            CharacterTag original = swf.getCharacterByClass("components.AssetHolder_classicFont");
            Assert.assertNotNull(original);
            DefineFont4Tag font = new DefineFont4Tag(swf);
            font.fontID = original.getCharacterId();
            font.fontName = "RecoveredCffFont";
            font.fontFlagsHasFontData = true;
            byte[] cff = Files.readAllBytes(Paths.get("testdata/as3_embed/assets/font.cff"));
            font.fontData = new ByteArrayRange(cff);
            swf.replaceTag(original, font);
            new MxmlExporter().exportMxml(swf, output.toFile());
            String code = new String(Files.readAllBytes(output.resolve("components/AssetHolder_classicFont.as")), java.nio.charset.StandardCharsets.UTF_8);
            Assert.assertTrue(code.contains("embedAsCFF=\"true\""));
            Assert.assertTrue(code.contains("fontName=\"RecoveredCffFont\""));
            Matcher source = Pattern.compile("\\[Embed\\(source=\"([^\"]+)\"").matcher(code);
            Assert.assertTrue(source.find());
            Path asset = output.resolve("components").resolve(source.group(1)).normalize();
            Assert.assertTrue(asset.toString().endsWith(".cff"));
            Assert.assertEquals(Files.readAllBytes(asset), cff);
        } finally {
            remove(output);
        }
    }

    @Test
    public void exportsTransitiveAs3DependenciesAndRecognizesMxmlComponents() throws Exception {
        Path output = Files.createTempDirectory("mxml-as3-dependencies-");
        try {
            SWF swf = input("ClassDependencies");
            Set<String> linked = swf.getAS3Packs().stream().map(pack -> pack.getClassPath().toRawString()).collect(Collectors.toSet());
            Assert.assertTrue(linked.contains("model.Unused"));
            Assert.assertTrue(linked.contains("model.Unrelated"));
            Assert.assertTrue(linked.stream().anyMatch(name -> name.startsWith("mx.")));
            Path main = new MxmlExporter().exportMxml(swf, output.toFile()).toPath();
            Set<String> expected = new HashSet<>(Arrays.asList("ClassDependencies.mxml", "components/ProductCard.mxml",
                    "components/TinyLabel.mxml", "components/StatusBadge.as", "model/Entity.as", "model/IPriced.as",
                    "model/Product.as", "model/RelatedProduct.as", "model/SignatureOnly.as", "model/SignatureDependency.as",
                    "model/Tag.as", "utils/PriceFormatter.as", "utils/CurrencySymbol.as", "utils/decorateLabel.as",
                    "utils/LabelPrefix.as", "utils/CardFormatter.as", "utils/RendererFormatter.as"));
            try (Stream<Path> files = Files.walk(output)) {
                Set<String> actual = files.filter(Files::isRegularFile)
                        .map(path -> output.relativize(path).toString().replace('\\', '/')).collect(Collectors.toSet());
                Assert.assertEquals(actual, expected);
            }
            Document document = xml(main);
            String script = document.getElementsByTagNameNS("http://ns.adobe.com/mxml/2009", "Script").item(0).getTextContent();
            Assert.assertTrue(script.contains("private var selected:Product = new Product();"));
            Assert.assertTrue(script.contains("private var tags:Vector.<Tag> = new <Tag>[new Tag(\"demo\")];"));
            Assert.assertTrue(script.contains("import utils.decorateLabel;"));
            Assert.assertEquals(document.getElementsByTagNameNS("http://ns.adobe.com/mxml/2009", "Component").getLength(), 1);
            Assert.assertEquals(xml(output.resolve("components/TinyLabel.mxml")).getDocumentElement().getLocalName(), "Label");
            String product = new String(Files.readAllBytes(output.resolve("model/Product.as")), java.nio.charset.StandardCharsets.UTF_8);
            Assert.assertTrue(product.contains("package model"));
            Assert.assertTrue(product.contains("class Product extends Entity implements IPriced"));
            Assert.assertTrue(product.contains("new RelatedProduct(this)"));
            String related = new String(Files.readAllBytes(output.resolve("model/RelatedProduct.as")), java.nio.charset.StandardCharsets.UTF_8);
            Assert.assertTrue(related.contains("var owner:Product"));
            String function = new String(Files.readAllBytes(output.resolve("utils/decorateLabel.as")), java.nio.charset.StandardCharsets.UTF_8);
            Assert.assertTrue(function.contains("public function decorateLabel("));
            Assert.assertTrue(function.contains("LabelPrefix.VALUE"));
            String status = new String(Files.readAllBytes(output.resolve("components/StatusBadge.as")), java.nio.charset.StandardCharsets.UTF_8);
            Assert.assertTrue(status.contains("class StatusBadge extends Label"));
        } finally {
            remove(output);
        }
    }

    @Test
    public void recoversBindingsCssStatesDeclarationsAndRenderer() throws Exception {
        Path output = Files.createTempDirectory("mxml-showcase-");
        try {
            Path file = new MxmlExporter().exportMxml(input("FeatureShowcase"), output.toFile()).toPath();
            Document document = xml(file);
            Assert.assertEquals(document.getDocumentElement().getAttribute("currentState"), "compact");
            Assert.assertEquals(document.getDocumentElement().getAttribute("backgroundColor"), "15331060");
            Assert.assertEquals(id(document, "_FeatureShowcase_Label1").getAttribute("text"), "{greeting + \", \" + userName + \"!\"}");
            Assert.assertEquals(id(document, "_FeatureShowcase_TextInput2").getAttribute("enabled"), "{canEdit.selected}");
            // Inline two-way bindings must preserve isTwoWayPrimary semantics.
            // Replacing this with fx:Binding changes initial values at runtime.
            Assert.assertEquals(id(document, "nameMirror").getAttribute("text"), "@{userName}");
            NodeList explicitBindings = document.getElementsByTagNameNS("http://ns.adobe.com/mxml/2009", "Binding");
            Assert.assertEquals(explicitBindings.getLength(), 1);
            Element binding = (Element) explicitBindings.item(0);
            Assert.assertEquals(binding.getAttribute("source"), "nameInput.text");
            Assert.assertEquals(binding.getAttribute("destination"), "userName");
            Assert.assertEquals(binding.getAttribute("twoWay"), "true");
            Assert.assertEquals(id(document, "quantityValidator").getAttribute("source"), "{quantity}");
            Assert.assertEquals(id(document, "detailsLabel").getAttribute("includeIn"), "details");
            Assert.assertEquals(id(document, "detailsLabel").getAttribute("showEffect"), "{fadeIn}");
            Assert.assertEquals(id(document, "stateLabel").getAttribute("text.details"), "{userName + \" - detailed mode\"}");
            Assert.assertEquals(id(document, "stateLabel").getAttribute("color.details"), "11162880");
            NodeList states = document.getElementsByTagNameNS("library://ns.adobe.com/flex/mx", "State");
            Assert.assertEquals(states.getLength(), 2);
            Assert.assertEquals(((Element) states.item(1)).getAttribute("stateGroups"), "expanded");
            Assert.assertEquals(document.getElementsByTagNameNS("library://ns.adobe.com/flex/mx", "Transition").getLength(), 1);
            Assert.assertEquals(document.getElementsByTagNameNS("library://ns.adobe.com/flex/mx", "DataGridColumn").getLength(), 3);
            Assert.assertEquals(document.getElementsByTagNameNS(SPARK, "Rect").getLength(), 1);
            Assert.assertEquals(id(document, "categories").getElementsByTagNameNS("http://ns.adobe.com/mxml/2009", "String")
                    .item(0).getTextContent(), "Drinks");
            String css = document.getElementsByTagNameNS("http://ns.adobe.com/mxml/2009", "Style").item(0).getTextContent();
            Assert.assertTrue(css.contains(".title {"));
            Assert.assertTrue(css.contains("color: #18446a;"));
            Assert.assertTrue(css.contains("s|Button:hover {"));
            Assert.assertTrue(css.contains("alternatingItemColors: #ffffff, #eef4fa;"));
            String script = document.getElementsByTagNameNS("http://ns.adobe.com/mxml/2009", "Script").item(0).getTextContent();
            Assert.assertTrue(script.contains("[Bindable]\n"));
            Assert.assertTrue(script.contains("private var userName:String = \"Alex\";"));
            Assert.assertTrue(script.contains("private static const DEMO_VERSION:String = \"1.0\";"));
            Assert.assertTrue(script.contains("public static function getDemoVersion()"));
            Assert.assertFalse(script.contains("_watcherSetupUtil"));
            Assert.assertFalse(script.contains("PropertyChangeEvent"));
            Assert.assertFalse(script.contains("_FeatureShowcase_SetProperty1"));
            NodeList components = document.getElementsByTagNameNS("http://ns.adobe.com/mxml/2009", "Component");
            Assert.assertEquals(components.getLength(), 1);
            Element renderer = (Element) document.getElementsByTagNameNS(SPARK, "ItemRenderer").item(0);
            Assert.assertSame(renderer.getParentNode(), components.item(0));
            Assert.assertEquals(id(document, "_FeatureShowcaseInnerClass0_Label1").getAttribute("text"), "{data.name}");
            Assert.assertEquals(document.getElementsByTagNameNS("mx.core.*", "ClassFactory").getLength(), 0);
            Assert.assertFalse(Files.exists(output.resolve("FeatureShowcaseInnerClass0.mxml")));
            Assert.assertFalse(document.getDocumentElement().hasAttribute("xmlns:local3"));
        } finally {
            remove(output);
        }
    }

    @Test
    public void inlinesNestedAndNamedComponentsWithTheirOwnScriptsAndScopes() throws Exception {
        Path output = Files.createTempDirectory("mxml-inline-scopes-");
        try {
            Path file = new MxmlExporter().exportMxml(input("InlineComponents"), output.toFile()).toPath();
            Document document = xml(file);
            Assert.assertEquals(document.getElementsByTagNameNS("http://ns.adobe.com/mxml/2009", "Component").getLength(), 3);
            Assert.assertEquals(document.getElementsByTagNameNS(SPARK, "ItemRenderer").getLength(), 3);
            Assert.assertEquals(id(document, "sharedRenderer").getLocalName(), "Component");
            Assert.assertEquals(id(document, "caption").getAttribute("text"), "{outerDocument.titleFor(data)}");
            Assert.assertEquals(id(document, "nestedList").getAttribute("dataProvider"), "{new ArrayCollection(data.children)}");
            Assert.assertEquals(id(document, "_InlineComponentsInnerClass1InnerClass0_Label1").getAttribute("text"),
                    "{outerDocument.data.name + \": \" + data}");
            Assert.assertEquals(document.getElementsByTagNameNS("mx.core.*", "ClassFactory").getLength(), 0);
            NodeList scripts = document.getElementsByTagNameNS("http://ns.adobe.com/mxml/2009", "Script");
            Assert.assertEquals(scripts.getLength(), 2);
            String rendererScript = scripts.item(1).getTextContent();
            Assert.assertTrue(rendererScript.contains("private function handleClick("));
            Assert.assertTrue(rendererScript.contains("import flash.events.MouseEvent;"));
            Assert.assertTrue(rendererScript.contains("import mx.collections.ArrayCollection;"));
            Assert.assertTrue(rendererScript.contains("this.outerDocument.record(data.name)"));
            Assert.assertFalse(rendererScript.contains("var outerDocument:"));
            try (Stream<Path> files = Files.list(output)) {
                Assert.assertEquals(files.count(), 1L);
            }
        } finally {
            remove(output);
        }
    }

    @Test
    public void preservesFactoriesWithExplicitInstanceProperties() throws Exception {
        Path output = Files.createTempDirectory("mxml-explicit-factory-");
        try {
            SWF swf = input("FeatureShowcase");
            for (ScriptPack pack : swf.getAS3Packs()) {
                if ("FeatureShowcase".equals(pack.getClassPath().toRawString())) {
                    int name = pack.abc.constants.getStringId("outerDocument", false);
                    Assert.assertTrue(name > 0);
                    pack.abc.constants.setString(name, "explicitOwner");
                    break;
                }
            }
            Path file = new MxmlExporter().exportMxml(swf, output.toFile()).toPath();
            Document document = xml(file);
            Assert.assertEquals(document.getElementsByTagNameNS("http://ns.adobe.com/mxml/2009", "Component").getLength(), 0);
            NodeList factories = document.getElementsByTagNameNS("mx.core.*", "ClassFactory");
            Assert.assertEquals(factories.getLength(), 1);
            Element properties = (Element) ((Element) factories.item(0)).getElementsByTagNameNS("http://ns.adobe.com/mxml/2009", "Object").item(0);
            Assert.assertEquals(properties.getAttribute("explicitOwner"), "{this}");
            Assert.assertTrue(Files.exists(output.resolve("FeatureShowcaseInnerClass0.mxml")));
        } finally {
            remove(output);
        }
    }

    private void remove(Path directory) throws Exception {
        try (Stream<Path> paths = Files.walk(directory)) {
            for (Path path : (Iterable<Path>) paths.sorted(Comparator.reverseOrder())::iterator) {
                Files.delete(path);
            }
        }
    }

    @Test
    public void recoversSparkTreeAndUserCodeFromSwf() throws Exception {
        Path output = Files.createTempDirectory("mxml-export-");
        try {
            Path file = new MxmlExporter().exportMxml(input(), output.toFile()).toPath();
            Assert.assertEquals(file.getFileName().toString(), "Main.mxml");
            Document xml = xml(file);
            Element root = element(xml, "Application");
            Assert.assertEquals(root.getAttribute("width"), "500");
            Assert.assertEquals(root.getAttribute("height"), "350");
            Element group = element(xml, "VGroup");
            Assert.assertSame(group.getParentNode(), root);
            Assert.assertEquals(group.getAttribute("horizontalCenter"), "0");
            Assert.assertEquals(group.getAttribute("verticalCenter"), "0");
            Assert.assertEquals(group.getAttribute("gap"), "15");
            Element label = element(xml, "Label");
            Assert.assertSame(label.getParentNode(), group);
            Assert.assertEquals(label.getAttribute("text"), "My first Flex application");
            Assert.assertEquals(label.getAttribute("fontSize"), "22");
            Element input = element(xml, "TextInput");
            Assert.assertEquals(input.getAttribute("id"), "nameInput");
            Assert.assertEquals(input.getAttribute("text"), "Alex");
            Element button = element(xml, "Button");
            Assert.assertEquals(button.getAttribute("label"), "Say hello");
            Assert.assertEquals(button.getAttribute("click"), "greet()");
            String script = xml.getElementsByTagNameNS("http://ns.adobe.com/mxml/2009", "Script").item(0).getTextContent();
            Assert.assertTrue(script.contains("import mx.controls.Alert;"));
            Assert.assertTrue(script.contains("private function greet()"));
            Assert.assertTrue(script.contains("Alert.show(\"Hello \" + this.nameInput.text,\"My first application\")"));
            Assert.assertFalse(script.contains("BindingManager"));
            Assert.assertFalse(script.contains("skinParts"));
        } finally {
            remove(output);
        }
    }

    @Test
    public void escapesXmlAndLiteralBindingBraces() throws Exception {
        Path output = Files.createTempDirectory("mxml-escaping-");
        try {
            SWF swf = input();
            ABC abc = mainAbc(swf);
            String text = "A & B < C \"quote\"\n\t{literal} ]]>";
            abc.constants.setString(abc.constants.getStringId("My first Flex application", false), text);
            abc.constants.setString(abc.constants.getStringId("My first application", false), "CDATA ]]> terminator");
            Path file = new MxmlExporter().exportMxml(swf, output.toFile()).toPath();
            Document document = xml(file);
            Assert.assertEquals(element(document, "Label").getAttribute("text"), text.replace("{", "\\{"));
            Assert.assertTrue(document.getElementsByTagNameNS("http://ns.adobe.com/mxml/2009", "Script")
                    .item(0).getTextContent().contains("CDATA ]]> terminator"));
        } finally {
            remove(output);
        }
    }

    @Test
    public void rejectsUnsupportedStylesBeforeWritingFile() throws Exception {
        Path output = Files.createTempDirectory("mxml-unsupported-");
        try {
            SWF swf = input();
            ABC abc = mainAbc(swf);
            abc.constants.setString(abc.constants.getStringId("initProtoChainRoots", false), "unsupportedCssCall");
            try {
                new MxmlExporter().exportMxml(swf, output.toFile());
                Assert.fail("Unsupported styles must not be silently dropped");
            } catch (IOException ex) {
                Assert.assertTrue(ex.getMessage().contains("Application CSS styles"), ex.getMessage());
            }
            Assert.assertFalse(Files.exists(output.resolve("Main.mxml")));
        } finally {
            remove(output);
        }
    }

    @Test
    public void doesNotWritePartialApplicationWhenRendererCannotBeRecovered() throws Exception {
        Path output = Files.createTempDirectory("mxml-renderer-failure-");
        try {
            SWF swf = input("FeatureShowcase");
            for (ScriptPack pack : swf.getAS3Packs()) {
                if ("FeatureShowcaseInnerClass0".equals(pack.getClassPath().toRawString())) {
                    int index = ((TraitClass) pack.getPublicTrait()).class_info;
                    AVM2Code code = new AVM2Code();
                    code.code.add(new AVM2Instruction(0, AVM2Instructions.ReturnVoid, new int[0]));
                    pack.abc.findBody(pack.abc.instance_info.get(index).iinit_index).setCode(code);
                }
            }
            try {
                new MxmlExporter().exportMxml(swf, output.toFile());
                Assert.fail("An unsupported renderer must fail the whole export");
            } catch (IOException ex) {
                Assert.assertTrue(ex.getMessage().contains("No classic MXML constructor"), ex.getMessage());
            }
            try (Stream<Path> files = Files.list(output)) {
                Assert.assertEquals(files.count(), 0L);
            }
        } finally {
            remove(output);
        }
    }

    @Test
    public void rejectsNonFlexSwf() throws Exception {
        Path output = Files.createTempDirectory("mxml-nonflex-");
        try {
            try {
                new MxmlExporter().exportMxml(new SWF(), output.toFile());
                Assert.fail("A non-Flex SWF cannot be exported as MXML");
            } catch (IOException ex) {
                Assert.assertTrue(ex.getMessage().contains("No Flex application class found"), ex.getMessage());
            }
        } finally {
            remove(output);
        }
    }
}
