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
package com.jpexs.decompiler.flash.exporters;

import com.jpexs.decompiler.flash.FontNormalizer;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.modes.FontExportMode;
import com.jpexs.decompiler.flash.exporters.shape.XamlShapeExporter;
import com.jpexs.decompiler.flash.tags.DefineEditTextTag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.DefineTextTag;
import com.jpexs.decompiler.flash.tags.DefineVideoStreamTag;
import com.jpexs.decompiler.flash.tags.SetBackgroundColorTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ButtonTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.MorphShapeTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.tags.base.StaticTextTag;
import com.jpexs.decompiler.flash.tags.base.TextTag;
import com.jpexs.decompiler.flash.tags.enums.ImageFormat;
import com.jpexs.decompiler.flash.timeline.DepthState;
import com.jpexs.decompiler.flash.timeline.Frame;
import com.jpexs.decompiler.flash.timeline.Timeline;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
import com.jpexs.decompiler.flash.types.TEXTRECORD;
import com.jpexs.decompiler.flash.xfl.XFLXmlWriter;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author JPEXS
 */
public class XamlExporter {

    public void exportSwf(SWF swf, File projectParentFolder, String projectName, double zoom) throws IOException {

        createProject(projectParentFolder, projectName);

        String projectIdentifier = makeIdentifier(projectName);

        File mainWindowXaml = new File(projectParentFolder.getAbsolutePath() + "/" + projectName + "/" + projectName + "/MainWindow.xaml");
        File csProj = new File(projectParentFolder.getAbsolutePath() + "/" + projectName + "/" + projectName + "/" + projectName + ".csproj");

        StringBuilder imageData = new StringBuilder();
        StringBuilder fontData = new StringBuilder();
        Map<Integer, FontTag> normalizedFonts = new HashMap<>();
        Map<Integer, TextTag> normalizedTexts = new HashMap<>();
        FontNormalizer fontNormalizer = new FontNormalizer();
        fontNormalizer.normalizeFonts(swf, normalizedFonts, normalizedTexts);

        XFLXmlWriter writer = new XFLXmlWriter();
        SetBackgroundColorTag bkgColorTag = swf.getBackgroundColor();
        RGB bgColor = new RGB(255, 255, 255);
        if (bkgColorTag != null) {
            bgColor = bkgColorTag.backgroundColor;
        }
        try {
            writer.writeStartElement("Window", new String[]{
                "x:Class", projectIdentifier + ".MainWindow",
                "xmlns", "http://schemas.microsoft.com/winfx/2006/xaml/presentation",
                "xmlns:x", "http://schemas.microsoft.com/winfx/2006/xaml",
                "Title", "MainWindow",
                "SizeToContent", "WidthAndHeight",
                "Background", bgColor.toHexRGB()
            });
            writer.writeStartElement("Window.Resources");
            Map<Integer, CharacterTag> characters = swf.getCharacters(false);
            Set<CharacterTag> characterSet = new HashSet<>(characters.values());

            Map<Integer, String> characterNames = new HashMap<>();
            for (int charId : characters.keySet()) {
                CharacterTag character = characters.get(charId);
                String type = "Character";
                if (character instanceof ShapeTag) {
                    type = "Shape";
                }
                if (character instanceof DefineSpriteTag) {
                    type = "Sprite";
                }
                if (character instanceof ImageTag) {
                    type = "Image";
                }
                if (character instanceof TextTag) {
                    type = "Text";
                }
                String name = type + charId;
                characterNames.put(charId, name);
            }

            Map<Integer, String> imageFiles = new HashMap<>();
            Set<String> systemFonts = new HashSet<>();

            File outputDir = mainWindowXaml.getParentFile();

            File imagesDir = new File(outputDir.getAbsolutePath() + "/Images");
            File fontsDir = new File(outputDir.getAbsolutePath() + "/Fonts");

            for (int charId : characters.keySet()) {
                CharacterTag character = characters.get(charId);

                if (character instanceof MorphShapeTag) {
                    continue;
                }
                if (character instanceof DefineVideoStreamTag) {
                    continue;
                }
                if (character instanceof ImageTag) {
                    ImageTag image = (ImageTag) character;
                    byte[] data = Helper.readStream(image.getConvertedImageData());
                    if (!imagesDir.exists()) {
                        imagesDir.mkdirs();
                    }
                    ImageFormat format = ImageTag.getImageFormat(data);
                    imageFiles.put(charId, "Images\\image" + charId + format.getExtension());
                    Helper.writeFile(imagesDir.getAbsolutePath() + "/image" + charId + format.getExtension(), data);
                    imageData.append("    <Resource Include=\"Images\\image").append(charId).append(format.getExtension()).append("\" />\r\n");
                    continue;
                }

                if (character instanceof FontTag) {
                    FontTag font = (FontTag) character;
                    if (normalizedFonts.containsKey(charId)) {
                        font = normalizedFonts.get(charId);
                    }
                    if (!fontsDir.exists()) {
                        fontsDir.mkdirs();
                    }
                    FontExporter exporter = new FontExporter();
                    String fontFile = fontsDir.getAbsolutePath() + "/font" + charId + ".ttf";
                    exporter.exportFont(font, FontExportMode.TTF, new File(fontFile));
                    if (!new File(fontFile).exists()) {
                        systemFonts.add(font.getFontNameIntag());
                    } else {
                        fontData.append("    <Resource Include=\"Fonts\\font").append(charId).append(".ttf\" />\r\n");
                    }
                    continue;
                }

                String name = characterNames.get(charId);
                writer.writeStartElement("DataTemplate", new String[]{"x:Key", name});
                if (character instanceof ShapeTag) {
                    ShapeTag shape = (ShapeTag) character;
                    XamlShapeExporter exporter = new XamlShapeExporter(shape.getWindingRule(), shape.getShapeNum(), swf, shape.getShapes(), charId, null, null, 1, 1, new Matrix(), imageFiles, false, null);
                    exporter.export();
                    String shapeCanvas = exporter.getResultAsString();
                    writer.writeCharactersRaw(shapeCanvas);
                }
                if (character instanceof DefineSpriteTag) {
                    String storyBoardName = name + "StoryBoard";
                    DefineSpriteTag sprite = (DefineSpriteTag) character;
                    writer.writeStartElement("DataTemplate.Resources");
                    writeStoryBoard(writer, sprite, swf, storyBoardName);
                    writer.writeEndElement(); //DataTemplate.Resources
                    writeTimeline(sprite, writer, characterNames, swf, 1, storyBoardName, imageFiles, outputDir);
                }
                if (character instanceof ButtonTag) {
                    ButtonTag button = (ButtonTag) character;
                    Frame frame = button.getTimeline().getFrame(0);
                    writer.writeStartElement("Canvas");
                    writeFrame(frame, writer, characterNames, swf, 1, imageFiles, mainWindowXaml.getParentFile());
                    writer.writeEndElement(); //Canvas
                }
                if (character instanceof StaticTextTag) {
                    StaticTextTag text = (StaticTextTag) character;
                    if (normalizedTexts.containsKey(charId)) {
                        text = (StaticTextTag) normalizedTexts.get(charId);
                    }
                    int fontId = -1;
                    FontTag font = null;
                    String fontName = null;
                    int textHeight = -1;
                    RGB textColor = null;
                    RGBA textColorA = null;
                    double lastLineHeight = -1;
                    double lastLeftMargin = -1;
                    double lastRightMargin = -1;
                    boolean firstRun = true;

                    writer.writeStartElement("Canvas");

                    writer.writeStartElement("RichTextBox", new String[]{
                        "Width", "" + (twipToPixel(text.getBounds().getWidth()) + 20),
                        "Height", "" + (twipToPixel(text.getBounds().getHeight()) + 20),
                        "BorderBrush", "Transparent",
                        "Background", "Transparent",
                        "IsReadOnly", "True",
                        "Focusable", "False",
                        "IsHitTestVisible", "False", //"Canvas.Left", "-5",
                        "Canvas.Left", "" + twipToPixel(text.getBounds().Xmin),
                        "Canvas.Top", "" + twipToPixel(text.getBounds().Ymin)
                    });

                    //Some magic to get RichTextBox behave correctly
                    writer.writeAttribute("Padding", "-4,-2,0,0");

                    writer.writeStartElement("RichTextBox.RenderTransform");
                    writer.writeStartElement("MatrixTransform", new String[]{
                        "Matrix", new Matrix(text.getTextMatrix()).getXamlTransformationString(SWF.unitDivisor / zoom, 1 / zoom)
                    });
                    writer.writeEndElement(); //MatrixTransform
                    writer.writeEndElement(); //RichTextBox.RenderTransform

                    writer.writeStartElement("FlowDocument");

                    boolean first = true;
                    Map<String, Object> attrs = TextTag.getTextRecordsAttributes(text.textRecords, swf, normalizedFonts);
                    if ((int) attrs.get("lineSpacing") < 0) {
                        attrs.put("lineSpacing", 0);
                    }
                    @SuppressWarnings("unchecked")
                    List<Integer> leftMargins = (List<Integer>) attrs.get("allLeftMargins");
                    for (int r = 0; r < text.textRecords.size(); r++) {
                        TEXTRECORD rec = text.textRecords.get(r);
                        if (rec.styleFlagsHasColor) {
                            if (text instanceof DefineTextTag) {
                                textColor = rec.textColor;
                            } else {
                                textColorA = rec.textColorA;
                            }
                        }
                        if (rec.styleFlagsHasFont) {
                            fontId = rec.fontId;
                            fontName = null;
                            textHeight = rec.textHeight;
                            font = ((Tag) text).getSwf().getFont(fontId);
                            if (normalizedFonts.containsKey(fontId)) {
                                font = normalizedFonts.get(fontId);
                            }

                            if (font != null) {
                                fontName = font.getFontNameIntag();
                            }
                            if (fontName == null) {
                                fontName = FontTag.getDefaultFontName();
                            }
                            int fontStyle = 0;
                            if (font != null) {
                                fontStyle = font.getFontStyle();
                            }
                        }
                        boolean newline = false;
                        if (!firstRun && rec.styleFlagsHasYOffset) {
                            newline = true;
                        }
                        firstRun = false;
                        if (font != null) {

                            boolean newParagraph = false;
                            if ((int) attrs.get("indent") > 0) {
                                newParagraph = true;
                            }
                            if (newline) {
                                newParagraph = true;
                            }
                            if (lastLineHeight != twipToPixel(textHeight) + twipToPixel((int) attrs.get("lineSpacing"))) {
                                newParagraph = true;
                            }
                            if (lastLeftMargin != twipToPixel(leftMargins.get(r))) {
                                newParagraph = true;
                            }
                            if (lastRightMargin != twipToPixel((int) attrs.get("rightMargin"))) {
                                newParagraph = true;
                            }
                            if (first) {
                                newParagraph = true;
                            }

                            if (newParagraph) {
                                if (!first) {
                                    writer.writeEndElement(); //Paragraph
                                }
                                lastLineHeight = twipToPixel(textHeight) + twipToPixel((int) attrs.get("lineSpacing"));
                                lastLeftMargin = twipToPixel(leftMargins.get(r));
                                lastRightMargin = twipToPixel((int) attrs.get("rightMargin"));
                                writer.writeStartElement("Paragraph", new String[]{
                                    "TextIndent", "" + (int) attrs.get("indent"),
                                    "LineHeight", doubleToString(lastLineHeight),
                                    "Margin", doubleToString(lastLeftMargin) + ",0," + doubleToString(lastRightMargin) + ",0"
                                });
                            }
                            writer.writeStartElement("Run", new String[]{
                                "FontSize", doubleToString(twipToPixel(textHeight)),
                                "FontFamily", systemFonts.contains(fontName) ? fontName : "/Fonts/#" + fontName
                            });

                            if (textColor != null) {
                                writer.writeAttribute("Foreground", textColor.toHexRGB());
                            } else if (textColorA != null) {
                                writer.writeAttribute("Foreground", textColorA.toHexARGB());
                            }
                            if (font.isBold()) {
                                writer.writeAttribute("FontWeight", "Bold");
                            }
                            if (font.isItalic()) {
                                writer.writeAttribute("FontStyle", "Italic");
                            }
                            writer.writeCharacters(rec.getText(font));
                            writer.writeEndElement(); //Run      
                            first = false;
                        }
                    }
                    if (!first) {
                        writer.writeEndElement(); //Paragraph
                    }
                    writer.writeEndElement(); //FlowDocument                            
                    writer.writeEndElement(); //RichTextBox
                    writer.writeEndElement(); //Canvas
                }
                if (character instanceof DefineEditTextTag) {
                    DefineEditTextTag defineEditText = (DefineEditTextTag) character;
                    if (normalizedTexts.containsKey(charId)) {
                        defineEditText = (DefineEditTextTag) normalizedTexts.get(charId);
                    }

                    writer.writeStartElement("Canvas");
                    /*
                    if (defineEditText.border) {
                        writer.writeStartElement("Border", new String[] {
                            "BorderBrush", "Black",
                            "BorderThickness", "1",
                            "Background", "White",
                            "Padding", "0",
                            "Width", "" + twipToPixel(defineEditText.getBounds().getWidth()),
                            "Height", "" + twipToPixel(defineEditText.getBounds().getHeight()),
                            //"Canvas.Left", "" + twipToPixel(defineEditText.getBounds().Xmin),
                            //"Canvas.Top", "" + twipToPixel(defineEditText.getBounds().Ymin),
                        });
                        writer.writeStartElement("Border.RenderTransform");
                        writer.writeStartElement("MatrixTransform", new String[] {
                            "Matrix", new Matrix(defineEditText.getTextMatrix()).getXamlTransformationString(SWF.unitDivisor / zoom, 1 / zoom)
                        });
                        writer.writeEndElement(); //MatrixTransform
                        writer.writeEndElement(); //Border.RenderTransform
                        
                        writer.writeEndElement(); //Border
                    }*/
                    writer.writeStartElement("RichTextBox", new String[]{
                        "Width", "" + twipToPixel(defineEditText.getBounds().getWidth()),
                        "Height", "" + twipToPixel(defineEditText.getBounds().getHeight()),
                        "BorderBrush", defineEditText.border ? "Black" : "Transparent",
                        "BorderThickness", defineEditText.border ? "1" : "0",
                        "Background", defineEditText.border ? "White" : "Transparent",
                        //"Canvas.Left", "-3",
                        //"Canvas.Top", "4"
                        "Canvas.Left", "" + (twipToPixel(defineEditText.getBounds().Xmin)), // - 3),
                        "Canvas.Top", "" + (twipToPixel(defineEditText.getBounds().Ymin)), // + 4),
                    });
                    //Note: I tried to make it more pixel perfect, but unfortunately, RichTextBox does not work very well

                    int fontId = defineEditText.fontId;
                    FontTag font = null;
                    if (fontId == -1) {
                        if (defineEditText.hasFontClass) {
                            font = swf.getFontByClass(defineEditText.fontClass);
                            fontId = swf.getCharacterId(font);
                        }
                    }
                    if (normalizedFonts.containsKey(fontId)) {
                        font = normalizedFonts.get(fontId);
                    }

                    if (font != null) {
                        if (font.hasLayout()) {
                            //Some magic to get RichTextBox behave correctly
                            double originalSize = defineEditText.fontHeight * (font.getAscent() + font.getDescent()) / font.getDivider() / 1024.0 / SWF.unitDivisor;
                            double normalizedSize = defineEditText.fontHeight / SWF.unitDivisor;
                            double padding = originalSize - normalizedSize - 2;
                            writer.writeAttribute("Padding", "-4," + padding + ",0,0");
                        }
                    }

                    if (defineEditText.readOnly) {
                        writer.writeAttribute("IsReadOnly", "True");
                    }
                    if (defineEditText.noSelect) {
                        writer.writeAttribute("Focusable", "False");
                        writer.writeAttribute("IsHitTestVisible", "False");
                    }

                    writer.writeStartElement("RichTextBox.RenderTransform");
                    writer.writeStartElement("MatrixTransform", new String[]{
                        "Matrix", new Matrix(defineEditText.getTextMatrix()).getXamlTransformationString(SWF.unitDivisor / zoom, 1 / zoom)
                    });
                    writer.writeEndElement(); //MatrixTransform
                    writer.writeEndElement(); //RichTextBox.RenderTransform

                    writer.writeStartElement("FlowDocument");

                    String txt = "";
                    if (defineEditText.hasText) {
                        txt = defineEditText.initialText;
                    }
                    String paragraphs = convertHtmlText(characterSet, defineEditText, txt, swf, systemFonts, normalizedFonts);
                    writer.writeCharactersRaw(paragraphs);
                    writer.writeEndElement(); //FlowDocument
                    writer.writeEndElement(); //RichTextBox                    
                    writer.writeEndElement(); //Canvas
                }

                writer.writeEndElement(); //DataTemplate
            }
            writeStoryBoard(writer, swf, swf, "MainTimeline");
            writer.writeEndElement(); //Window.Resources

            writer.writeStartElement("Grid", new String[]{
                "Width", "" + twipToPixel(swf.getRect().getWidth() * zoom),
                "Height", "" + twipToPixel(swf.getRect().getHeight() * zoom)
            });
            writeTimeline(swf, writer, characterNames, swf, zoom, "MainTimeline", imageFiles, outputDir);
            writer.writeEndElement(); //Grid
            writer.writeEndElement(); //Window

        } catch (XMLStreamException ex) {
            Logger.getLogger(XamlExporter.class.getName()).log(Level.SEVERE, null, ex);
        }
        String mainWindowsStr = writer.toString();

        try {
            //String formatted = new XmlPrettyFormat().prettyFormat(mainWindowsStr, 5, false);
            String formatted = mainWindowsStr;
            try (FileOutputStream fos = new FileOutputStream(mainWindowXaml)) {
                fos.write(formatted.getBytes(Utf8Helper.charset));
            }
        } catch (Exception te) {
            System.out.println(mainWindowsStr);
        }

        if (imageData.length() > 0 || fontData.length() > 0) {
            String projContents = Helper.readTextFile(csProj.getAbsolutePath());
            String itemGroupEnd = "</ItemGroup>\r\n";
            int pos = projContents.lastIndexOf(itemGroupEnd) + itemGroupEnd.length();
            StringBuilder fullData = new StringBuilder();
            if (imageData.length() > 0) {
                fullData.append("  <ItemGroup>\r\n").append(imageData.toString()).append("  </ItemGroup>\r\n");
            }
            if (fontData.length() > 0) {
                fullData.append("  <ItemGroup>\r\n").append(fontData.toString()).append("  </ItemGroup>\r\n");
            }
            projContents = projContents.substring(0, pos) + fullData.toString() + projContents.substring(pos);
            try (FileOutputStream fos = new FileOutputStream(csProj)) {
                fos.write(projContents.getBytes(Utf8Helper.charset));
            }
        }
    }

    private static String convertHtmlText(Set<CharacterTag> characterTags, DefineEditTextTag det, String html, SWF swf, Set<String> systemFonts, Map<Integer, FontTag> normalizedFonts) {
        HtmlTextParser tparser = new HtmlTextParser(characterTags, det, swf, systemFonts, normalizedFonts);
        XMLReader parser;
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser sparser = factory.newSAXParser();
            parser = sparser.getXMLReader();
            parser.setContentHandler(tparser);
            parser.setErrorHandler(tparser);
            html = "<?xml version=\"1.0\"?>\n"
                    + "<!DOCTYPE some_name [ \n"
                    + "<!ENTITY nbsp \"&#160;\"> \n"
                    + "]><html>" + html + "</html>";
            try {
                parser.parse(new InputSource(new StringReader(html)));
            } catch (SAXParseException spe) {
                System.out.println(html);
                System.err.println(tparser.result);
            }
        } catch (SAXException | IOException | ParserConfigurationException e) {
            Logger.getLogger(XamlExporter.class.getName()).log(Level.SEVERE, "Error while converting HTML", e);
        }
        return tparser.result.toString();
    }

    private static class HtmlTextParser extends DefaultHandler {

        public XFLXmlWriter result = new XFLXmlWriter();

        private String fontFace = "";

        private String color = "";

        private int colorAlpha = 255;

        private boolean autoKern = false;

        private int size = -1;

        private int indent = -1;

        private int leftMargin = -1;

        private int rightMargin = -1;

        private int lineSpacing = -1;

        private double letterSpacing = 0;

        private String alignment = null;

        private final Set<CharacterTag> characterTags;

        private boolean bold = false;

        private boolean italic = false;

        private boolean underline = false;

        private boolean li = false;

        private String url = null;

        private String target = null;
        private boolean first = true;

        private Stack<Double> fontLetterSpacingStack = new Stack<>();
        private Stack<Integer> fontSizeStack = new Stack<>();
        private Stack<String> fontFaceStack = new Stack<>();
        private Stack<String> fontColorStack = new Stack<>();
        private Stack<Integer> fontColorAlphaStack = new Stack<>();
        private Stack<Boolean> fontKerningStack = new Stack<>();
        private final SWF swf;

        String lastAlignment = null;
        private int lastIndent = -1;
        private int lastLeftMargin = -1;
        private int lastRightMargin = -1;
        private int lastLineSize = -1;
        private final Set<String> systemFonts;
        private final Map<Integer, FontTag> normalizedFonts;

        @Override
        public void error(SAXParseException e) throws SAXException {
        }

        @Override
        public void fatalError(SAXParseException e) throws SAXException {
        }

        @Override
        public void warning(SAXParseException e) throws SAXException {
        }

        public HtmlTextParser(
                Set<CharacterTag> characterTags,
                DefineEditTextTag det,
                SWF swf,
                Set<String> systemFonts,
                Map<Integer, FontTag> normalizedFonts
        ) {
            if (det.hasFont || det.hasFontClass) {
                String fontName = null;
                FontTag ft = null;
                if (det.hasFont) {
                    ft = (FontTag) det.getSwf().getCharacter(det.fontId);
                }
                if (det.hasFontClass) {
                    ft = det.getSwf().getFontByClass(det.fontClass);
                }
                if (ft != null) {
                    /*DefineFontNameTag fnt = ft.getFontNameTag();
                    if (fnt != null) {
                        fontName = fnt.fontName;
                    }*/

                    if (fontName == null) {
                        fontName = ft.getFontNameIntag();
                    }
                    if (fontName == null) {
                        fontName = FontTag.getDefaultFontName();
                    }
                    italic = ft.isItalic();
                    bold = ft.isBold();
                    size = (int) twipToPixel(det.fontHeight);

                    fontFace = fontName;
                    fontFaceStack.push(fontFace);
                    fontSizeStack.push(size);
                }
            }
            if (det.hasLayout) {
                leftMargin = det.leftMargin;
                rightMargin = det.rightMargin;
                indent = det.indent;
                lineSpacing = (int) twipToPixel(det.leading);
                String[] alignNames = {"left", "right", "center", "justify"};
                if (det.align < alignNames.length) {
                    alignment = alignNames[det.align];
                } else {
                    alignment = "unknown";
                }
            }
            if (det.hasTextColor) {
                color = det.textColor.toHexRGB();
                colorAlpha = det.textColor.alpha;
            }

            this.characterTags = characterTags;
            this.swf = swf;
            this.systemFonts = systemFonts;
            this.normalizedFonts = normalizedFonts;
        }

        @Override
        public void startDocument() throws SAXException {
        }

        @Override
        public void startElement(String uri, String localName,
                String qName, Attributes attributes) throws SAXException {
            switch (qName) {
                case "a":
                    String href = attributes.getValue("href");
                    if (href != null) {
                        url = href;
                    }
                    String t = attributes.getValue("target");
                    if (t != null) {
                        target = t;
                    }
                    break;
                case "b":
                    bold = true;
                    break;
                case "i":
                    italic = true;
                    break;
                case "u":
                    underline = true;
                    break;
                case "li":
                    li = true;
                    break;
                case "p":
                    String a = attributes.getValue("align");
                    if (a != null) {
                        alignment = a;
                    }
                    if (!result.isEmpty()) {
                        putText("\r\n");
                    }
                    break;
                case "font":
                    String k = attributes.getValue("kerning");
                    if (k != null) {
                        autoKern = k.equals("1");
                    }
                    String ls = attributes.getValue("letterSpacing");
                    if (ls != null) {
                        try {
                            letterSpacing = Double.parseDouble(ls);
                        } catch (NumberFormatException ex) {
                            Logger.getLogger(XamlExporter.class.getName()).log(Level.WARNING, "Invalid letter spacing value: {0}", ls);
                        }
                    }
                    String s = attributes.getValue("size");
                    if (s != null) {

                        try {
                            if (s.startsWith("+")) {
                                size += Integer.parseInt(s.substring(1));
                            } else if (s.startsWith("-")) {
                                size -= Integer.parseInt(s.substring(1));
                            } else {
                                size = Integer.parseInt(s);
                            }
                        } catch (NumberFormatException ex) {
                            Logger.getLogger(XamlExporter.class.getName()).log(Level.WARNING, "Invalid font size: {0}", s);
                        }
                    }
                    String c = attributes.getValue("color");
                    if (c != null) {
                        if (c.matches("^#[0-9a-fA-F]{8}$")) {
                            color = "#" + c.substring(3);
                        } else if (c.matches("^#[0-9a-fA-F]{6}$")) {
                            color = c;
                            colorAlpha = 255;
                        } else {
                            //wrong format, do not change color
                        }
                    }
                    String f = attributes.getValue("face");
                    if (f != null) {
                        for (Tag tag : characterTags) {
                            if (tag instanceof FontTag) {
                                FontTag ft = (FontTag) tag;
                                String fontName = null;
                                if (f.equals(ft.getFontNameIntag())) {
                                    /*DefineFontNameTag fnt = ft.getFontNameTag();
                                    if (fnt != null) {
                                        fontName = fnt.fontName;
                                    }*/
                                    if (fontName == null) {
                                        fontName = ft.getFontNameIntag();
                                    }
                                    fontFace = fontName;
                                    break;
                                }
                            }
                        }
                    }
                    fontColorStack.push(color);
                    fontColorAlphaStack.push(colorAlpha);
                    fontFaceStack.push(fontFace);
                    fontSizeStack.push(size);
                    fontLetterSpacingStack.push(letterSpacing);
                    fontKerningStack.push(autoKern);
                    break;
            }
        }

        @Override
        public void endElement(String uri, String localName,
                String qName) throws SAXException {
            if (qName.equals("a")) {
                url = null;
                target = null;
            }
            if (qName.equals("b")) {
                bold = false;
            }
            if (qName.equals("i")) {
                italic = false;
            }
            if (qName.equals("u")) {
                underline = false;
            }
            if (qName.equals("li")) {
                li = false;
            }
            if (qName.equals("font")) {
                fontColorStack.pop();
                fontColorAlphaStack.pop();
                fontFaceStack.pop();
                fontKerningStack.pop();
                fontLetterSpacingStack.pop();
                fontSizeStack.pop();
                color = null;
                colorAlpha = 255;
                if (!fontColorStack.isEmpty()) {
                    color = fontColorStack.peek();
                    colorAlpha = fontColorAlphaStack.peek();
                }
                fontFace = null;
                if (!fontFaceStack.isEmpty()) {
                    fontFace = fontFaceStack.peek();
                }
                autoKern = false;
                if (!fontKerningStack.isEmpty()) {
                    autoKern = fontKerningStack.peek();
                }
                letterSpacing = 0;
                if (!fontLetterSpacingStack.isEmpty()) {
                    letterSpacing = fontLetterSpacingStack.peek();
                }
                size = 10; //??
                if (!fontSizeStack.isEmpty()) {
                    size = fontSizeStack.peek();
                }
            }
        }

        private void putText(String txt) {
            try {
                int newLineSize = 0;
                if (fontFace != null && size > -1) {
                    FontTag font = swf.getFontByNameInTag(fontFace, bold, italic);
                    if (font != null) {
                        int fontId = swf.getCharacterId(font);
                        if (normalizedFonts.containsKey(fontId)) {
                            font = normalizedFonts.get(fontId);
                        }
                        if (font.hasLayout()) {
                            newLineSize = (int) Math.round(size * (font.getAscent() + font.getDescent()) / font.getDivider() / 1024.0);
                            if (lineSpacing > -1) {
                                newLineSize += lineSpacing;
                            }
                        }
                    }
                }

                if ((alignment != null && !Objects.equals(lastAlignment, alignment))
                        || (indent > -1 && lastIndent != indent)
                        || (leftMargin > -1 && lastLeftMargin != leftMargin)
                        || (rightMargin > -1 && lastRightMargin != rightMargin)
                        || (lineSpacing > -1 && size > -1 && lastLineSize != newLineSize)
                        || first
                        || "\r\n".equals(txt)) {

                    lastAlignment = alignment;
                    lastIndent = indent;
                    lastLeftMargin = leftMargin;
                    lastRightMargin = rightMargin;
                    lastLineSize = newLineSize;
                    if (!first) {
                        result.writeEndElement(); //Paragraph
                    }
                    result.writeStartElement("Paragraph");
                    if (alignment != null) {
                        String alignmentValue = alignment.substring(0, 1).toUpperCase() + alignment.substring(1);
                        result.writeAttribute("TextAlignment", alignmentValue);
                    }
                    if (indent > -1) {
                        result.writeAttribute("TextIndent", twipToPixel(indent));
                    }
                    if (leftMargin > -1 || rightMargin > -1) {
                        result.writeAttribute("Margin", twipToPixel(leftMargin == -1 ? 0 : leftMargin) + ",0," + twipToPixel(rightMargin == -1 ? 0 : rightMargin) + ",0");
                    }
                    if (lineSpacing > -1 && size > -1) {
                        result.writeAttribute("LineHeight", newLineSize);
                    }
                }
                result.writeStartElement("Run");

                result.writeAttribute("Typography.Kerning", autoKern ? "True" : "False");
                /*if (letterSpacing != 0) {
                    result.writeAttribute("letterSpacing", letterSpacing);
                }*/

                if (size > -1) {
                    result.writeAttribute("FontSize", size);
                    //result.writeAttribute("bitmapSize", (int) (size * SWF.unitDivisor));
                }
                if (fontFace != null) {
                    result.writeAttribute("FontFamily", systemFonts.contains(fontFace) ? fontFace : "/Fonts/#" + fontFace);
                }
                if (color != null && !color.isEmpty()) {
                    if (colorAlpha != 255) {
                        result.writeAttribute("Foreground", "#" + String.format("%02x", colorAlpha) + color.substring(1));
                    } else {
                        result.writeAttribute("Foreground", color);
                    }
                }
                if (bold) {
                    result.writeAttribute("FontWeight", "Bold");
                }
                if (italic) {
                    result.writeAttribute("FontStyle", "Italic");
                }

                if (url != null) {
                    result.writeStartElement("Hyperlink");
                    result.writeAttribute("NavigateUri", url);
                    result.writeAttribute("Foreground", "Blue");
                    result.writeAttribute("TextDecorations", "Underline");
                }
                result.writeCharacters(txt);
                if (url != null) {
                    result.writeEndElement(); //Hyperlink
                }
                if (target != null) {
                    //result.writeAttribute("target", target);
                }
                result.writeEndElement(); //Run

                first = false;
            } catch (XMLStreamException ex) {
                Logger.getLogger(XamlExporter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @Override
        public void characters(char[] ch, int start, int length)
                throws SAXException {
            putText(new String(ch, start, length));
        }

        @Override
        public void endDocument() {
            if (this.result.isEmpty()) {
                putText("");
            }
            if (!first) {
                try {
                    result.writeEndElement(); //Paragraph
                } catch (XMLStreamException ex) {
                    Logger.getLogger(XamlExporter.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public static String formatTime(double totalSeconds) {
        long totalMillis = (long) (totalSeconds * 1000);

        long hours = totalMillis / 3600000;
        long minutes = (totalMillis % 3600000) / 60000;
        double secondsWithFraction = (totalMillis % 60000) / 1000.0;

        return String.format(Locale.ENGLISH, "%d:%d:%.3f", hours, minutes, secondsWithFraction);
    }

    private void writeStoryBoard(XFLXmlWriter writer, Timelined timelined, SWF swf, String storyBoardName) throws XMLStreamException {
        writer.writeStartElement("Storyboard", new String[]{
            "x:Key", storyBoardName,
            "RepeatBehavior", "Forever"
        });
        Timeline timeline = timelined.getTimeline();
        for (int i = 0; i < timeline.getFrameCount(); i++) {
            writer.writeStartElement("ObjectAnimationUsingKeyFrames", new String[]{
                "Storyboard.TargetName", "frame" + (i + 1),
                "Storyboard.TargetProperty", "Visibility"
            });
            if (i == 0) {
                writer.writeStartElement("DiscreteObjectKeyFrame", new String[]{
                    "KeyTime", "0:0:0.0",
                    "Value", "{x:Static Visibility.Visible}"
                });
                writer.writeEndElement();
                writer.writeStartElement("DiscreteObjectKeyFrame", new String[]{
                    "KeyTime", formatTime(1 / swf.frameRate),
                    "Value", "{x:Static Visibility.Collapsed}"
                });
                writer.writeEndElement();
            } else {
                writer.writeStartElement("DiscreteObjectKeyFrame", new String[]{
                    "KeyTime", "0:0:0.0",
                    "Value", "{x:Static Visibility.Collapsed}"
                });
                writer.writeEndElement();
                writer.writeStartElement("DiscreteObjectKeyFrame", new String[]{
                    "KeyTime", formatTime(i * 1 / swf.frameRate),
                    "Value", "{x:Static Visibility.Visible}"
                });
                writer.writeEndElement();
                writer.writeStartElement("DiscreteObjectKeyFrame", new String[]{
                    "KeyTime", formatTime((i + 1) * 1 / swf.frameRate),
                    "Value", "{x:Static Visibility.Collapsed}"
                });
                writer.writeEndElement();

            }
            writer.writeEndElement(); //ObjectAnimationUsingKeyFrames
        }

        writer.writeEndElement();; // StoryBoard
    }

    private void writeTimeline(Timelined timelined, XFLXmlWriter writer, Map<Integer, String> characterNames, SWF swf, double zoom, String storyBoardName, Map<Integer, String> imageFiles, File outputDir) throws XMLStreamException, IOException {
        writer.writeStartElement("Grid");

        writer.writeStartElement("Grid.Triggers");
        writer.writeStartElement("EventTrigger", new String[]{
            "RoutedEvent", "FrameworkElement.Loaded"
        });
        writer.writeStartElement("BeginStoryboard", new String[]{
            "Storyboard", "{StaticResource " + storyBoardName + "}"
        });
        writer.writeEndElement(); //BeginStoryboard
        writer.writeEndElement(); //EventTrigger
        writer.writeEndElement(); //Grid.Triggers

        Timeline timeline = timelined.getTimeline();

        for (int i = 0; i < timeline.getFrameCount(); i++) {
            writer.writeStartElement("Canvas", new String[]{
                "x:Name", "frame" + (i + 1)
            });
            if (i > 0) {
                writer.writeAttribute("Visibility", "Collapsed");
            }

            Frame frame = timeline.getFrame(i);
            writeFrame(frame, writer, characterNames, swf, zoom, imageFiles, outputDir);

            writer.writeEndElement(); //Canvas
        }

        writer.writeEndElement(); //Grid
    }

    private void writeFrame(Frame frame, XFLXmlWriter writer, Map<Integer, String> characterNames, SWF swf, double zoom, Map<Integer, String> imageFiles, File outputDir) throws XMLStreamException, IOException {
        Matrix zoomMatrix = Matrix.getScaleInstance(zoom);
        Stack<Integer> clipDepths = new Stack<>();
        for (Integer depth : frame.layers.keySet()) {
            DepthState depthState = frame.layers.get(depth);

            while (!clipDepths.isEmpty()) {
                int topDepth = clipDepths.peek();
                if (depth >= topDepth) {
                    clipDepths.pop();
                    writer.writeEndElement(); //Canvas
                } else {
                    break;
                }
            }

            if (depthState.characterId > -1) {
                String characterElement;
                CharacterTag character = depthState.getCharacter();

                if (depthState.clipDepth > -1) {
                    writer.writeStartElement("Canvas");
                    if (character instanceof ShapeTag) {
                        writer.writeStartElement("Canvas.Clip");                    
                        ShapeTag shape = (ShapeTag) character;
                        Matrix matrix = depthState.matrix == null ? new Matrix() : new Matrix(depthState.matrix);
                        matrix = matrix.preConcatenate(zoomMatrix);
                        XamlShapeExporter exporter = new XamlShapeExporter(shape.getWindingRule(), shape.getShapeNum(), swf, shape.getShapes(), shape.getCharacterId(), null, null, 1, 1, new Matrix(), imageFiles, true, matrix);
                        exporter.export();
                        String shapeGeometry = exporter.getResultAsString();
                        writer.writeCharactersRaw(shapeGeometry);
                        writer.writeEndElement(); //Canvas.Clip                    
                    } else {
                        //TODO: Create shape from sprites, etc.
                    }
                    clipDepths.push(depthState.clipDepth);
                    continue;
                }

                if (character instanceof MorphShapeTag) {
                    writer.writeStartElement("Canvas");
                    characterElement = "Canvas";

                    MorphShapeTag morphShape = (MorphShapeTag) character;
                    SHAPEWITHSTYLE shape = morphShape.getShapeAtRatio(depthState.ratio);
                    XamlShapeExporter exporter = new XamlShapeExporter(ShapeTag.WIND_EVEN_ODD, morphShape.getShapeNum() == 2 ? 4 : 1, swf, shape, morphShape.getCharacterId(), null, null, 1, 1, new Matrix(), imageFiles, false, null);
                    exporter.export();
                    String shapeCanvas = exporter.getResultAsString();
                    writer.writeCharactersRaw(shapeCanvas);
                } else if (character instanceof DefineVideoStreamTag) {

                    //No support for Video, this is just a stub, but it extracts all PNG frames to disk...
                    continue;
                    /*DefineVideoStreamTag video = (DefineVideoStreamTag) character;
                    
                    int ratio = depthState.ratio;
                    if (ratio == -1) {
                        ratio = 0;
                    }
                    File videoDir = new File(outputDir.getAbsolutePath() + "/Video");
                    if (!videoDir.exists()) {
                        videoDir.mkdirs();
                    }
                    File videoFrameFile = new File(videoDir.getAbsolutePath() + "/video" + video.getCharacterId()+"_" + ratio + ".png");
                    
                    if (!videoFrameFile.exists()) {                    
                        SerializableImage img = new SerializableImage(video.width, video.height, BufferedImage.TYPE_INT_ARGB);
                        Matrix matrix = new Matrix();
                        video.toImage(0, 0, ratio, new RenderContext(), img, img, false, matrix, null, null, null, null, 1, true, new ExportRectangle(swf.getRect()), new ExportRectangle(swf.getRect()), true, 0, 0, false, 1);
                        ImageIO.write(img.getBufferedImage(), "PNG", videoFrameFile);                        
                    }
                                        
                    writer.writeStartElement("Image", new String[] {
                        "Source", "/Video/video" + video.getCharacterId() + "_" + ratio + ".png"
                    });
                    characterElement = "Image";*/

                } else {
                    writer.writeStartElement("ContentControl", new String[]{
                        "ContentTemplate", "{StaticResource " + characterNames.get(depthState.characterId) + "}"
                    });
                    characterElement = "ContentControl";
                }
                MATRIX matrix = new MATRIX();
                if (depthState.matrix != null) {
                    matrix = depthState.matrix;
                }
                writer.writeStartElement(characterElement + ".RenderTransform");
                writer.writeStartElement("MatrixTransform", new String[]{
                    "Matrix", new Matrix(matrix).getXamlTransformationString(SWF.unitDivisor / zoom, 1 / zoom)
                });
                writer.writeEndElement(); //MatrixTransform
                writer.writeEndElement(); //characterElement + .RenderTransform

                writer.writeEndElement(); //ContentControl
            }
        }
        while (!clipDepths.isEmpty()) {
            clipDepths.pop();
            writer.writeEndElement(); //Canvas
        }
    }

    private static double twipToPixel(double tw) {
        return tw / SWF.unitDivisor;
    }

    private static String doubleToString(double d, int precision) {
        double m = Math.pow(10, precision);
        d = Math.round(d * m) / m;
        return doubleToString(d);
    }

    private static String doubleToString(double d) {
        String ds = "" + d;
        if (ds.endsWith(".0")) {
            ds = ds.substring(0, ds.length() - 2);
        }
        return ds;
    }

    private static String makeIdentifier(String name) {
        return name.replace(" ", "_");
    }

    private static void createProject(File destDir, String projectName) throws IOException {
        InputStream is = XamlExporter.class.getResourceAsStream("/com/jpexs/decompiler/flash/exporters/XamlExporterStub.zip");
        String templateName = "My Wpf App";
        String templateIdentifier = makeIdentifier(templateName);

        String projectIdentifier = makeIdentifier(projectName);

        destDir = new File(destDir, projectName);

        try (ZipInputStream zis = new ZipInputStream(is)) {
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {

                String newName = entry.getName();
                if (newName.contains(templateName)) {
                    newName = newName.replace(templateName, projectName);
                }

                File newFile = new File(destDir, newName);

                if (entry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    new File(newFile.getParent()).mkdirs();

                    String contents = new String(Helper.readStream(zis), "UTF-8");
                    contents = contents.replace(templateIdentifier, projectIdentifier);
                    contents = contents.replace(templateName, projectName);

                    String projectGuidBegin = "<ProjectGuid>";
                    String projectGuidEnd = "</ProjectGuid>";

                    if (contents.contains(projectGuidBegin)) {
                        String projectGuid = "{" + UUID.randomUUID().toString().toUpperCase() + "}";
                        int start = contents.indexOf(projectGuidBegin) + projectGuidBegin.length();
                        int end = contents.indexOf(projectGuidEnd, start);
                        contents = contents.substring(0, start)
                                + projectGuid
                                + contents.substring(end);
                    }

                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        fos.write(contents.getBytes("UTF-8"));
                    }
                }

                zis.closeEntry();
            }
        }
    }   
}
