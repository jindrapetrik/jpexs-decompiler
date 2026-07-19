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
package com.jpexs.decompiler.flash.exporters.swf;

import com.jpexs.decompiler.flash.AbortRetryIgnoreHandler;
import com.jpexs.decompiler.flash.ApplicationInfo;
import com.jpexs.decompiler.flash.EventListener;
import com.jpexs.decompiler.flash.ReadOnlyTagList;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.exporters.ImageExporter;
import com.jpexs.decompiler.flash.exporters.SoundExporter;
import com.jpexs.decompiler.flash.exporters.modes.ImageExportMode;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.exporters.script.AS2ScriptExporter;
import com.jpexs.decompiler.flash.exporters.settings.ImageExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.ScriptExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.SoundExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.XmlSwfExportSettings;
import com.jpexs.decompiler.flash.helpers.InternalClass;
import com.jpexs.decompiler.flash.helpers.LazyObject;
import com.jpexs.decompiler.flash.tags.DefineButtonTag;
import com.jpexs.decompiler.flash.tags.DefineSoundTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.UnknownTag;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.flash.tags.base.ButtonAction;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.SoundTag;
import com.jpexs.decompiler.flash.types.annotations.Conditional;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.decompiler.flash.types.annotations.parser.AnnotationParseException;
import com.jpexs.decompiler.flash.types.annotations.parser.ConditionEvaluator;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.ReflectionTools;
import com.jpexs.helpers.XmlPrettyFormat;
import com.jpexs.helpers.utf8.Utf8OutputStreamWriter;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import macromedia.asc.util.Decimal128;

/**
 * Exports SWF to XML.
 *
 * @author JPEXS
 */
public class SwfXmlExporter {

    /**
     * XML export version major.
     */
    public static final int XML_EXPORT_VERSION_MAJOR = 2;

    public static final int XML_EXPORT_VERSION_MAJOR_WITH_EXTERNAL_FILES = 3;

    /**
     * XML export version minor.
     *
     * Version 2 - export only fields that meet conditions
     */
    public static final int XML_EXPORT_VERSION_MINOR = 2;

    private static final Logger logger = Logger.getLogger(SwfXmlExporter.class.getName());

    private final Map<Class, List<Field>> cachedFields = new HashMap<>();

    /**
     * Exports SWF to XML.
     *
     * @param swf SWF to export
     * @param outFile Target file to save to
     * @throws IOException On I/O error
     */
    public void exportXml(SWF swf, File outFile) throws IOException {
        exportXml(swf, outFile, new XmlSwfExportSettings(), null, new AbortRetryIgnoreHandler() {
            @Override
            public int handle(Throwable thrown) {
                return AbortRetryIgnoreHandler.ABORT;
            }

            @Override
            public AbortRetryIgnoreHandler getNewInstance() {
                return this;
            }
        });
    }

    /**
     * Exports SWF to XML.
     *
     * @param swf SWF to export
     * @param outFile Target file to save to
     * @param settings Export settings
     * @param evl Event listener
     * @param handler Abort/Retry/Ignore handler
     *
     * @throws IOException On I/O error
     */
    public void exportXml(SWF swf, File outFile, XmlSwfExportSettings settings, EventListener evl, AbortRetryIgnoreHandler handler) throws IOException {
        try {
            File tmp = File.createTempFile("FFDEC", "XML");

            String assetsDirName = outFile.getName();
            if (assetsDirName.contains(".")) {
                assetsDirName = assetsDirName.substring(0, assetsDirName.lastIndexOf("."));
            }
            assetsDirName = assetsDirName + "_assets";

            Map<ASMSource, String> asmExternalFiles = new HashMap<>();
            if (settings.as12ExportMode != null) {
                Map<String, ASMSource> externalNameToAsm = swf.getASMs(true);
                Set<String> existingNames = new HashSet<>();
                for (String key : externalNameToAsm.keySet()) {
                    ASMSource asm = externalNameToAsm.get(key);
                    String currentOutDir = key + "/";
                    currentOutDir = new File(currentOutDir).getParentFile().toString();
                    currentOutDir = currentOutDir.replace("\\", "/");
                    if (!"/".equals(currentOutDir)) {
                        currentOutDir += "/";
                    }
                    String name = Helper.makeFileName(asm.getExportFileName());
                    int i = 1;
                    String baseName = name;
                    while (existingNames.contains(currentOutDir + name)) {
                        i++;
                        name = baseName + "_" + i;
                    }
                    existingNames.add(currentOutDir + name);
                    asmExternalFiles.put(asm, assetsDirName + "/scripts" + currentOutDir + name + ".as");
                }
            }

            Map<Tag, String> tagExternalFiles = new IdentityHashMap<>();
            List<Tag> imagesList = new ArrayList<>();
            if (settings.imageExportMode != null) {
                ImageExportSettings imageExportSettings = new ImageExportSettings(settings.imageExportMode);
                Map<Integer, CharacterTag> chars = swf.getCharacters(false);
                for (int charId : chars.keySet()) {
                    CharacterTag ch = chars.get(charId);
                    if (ch instanceof ImageTag) {
                        ImageTag imageTag = (ImageTag) ch;
                        tagExternalFiles.put(imageTag, assetsDirName + "/images/" + Helper.makeFileName(imageTag.getCharacterExportFileName()) + "." + ImageExporter.getExportExtension(imageTag, imageExportSettings));
                        imagesList.add(imageTag);
                    }
                }
            }

            List<SoundTag> soundList = new ArrayList<>();
            if (settings.defineSoundExportMode != null) {
                SoundExportSettings soundExportSettings = new SoundExportSettings(settings.defineSoundExportMode);
                Map<Integer, CharacterTag> chars = swf.getCharacters(false);
                for (int charId : chars.keySet()) {
                    CharacterTag ch = chars.get(charId);
                    if (ch instanceof DefineSoundTag) {
                        DefineSoundTag soundTag = (DefineSoundTag) ch;
                        tagExternalFiles.put(soundTag, assetsDirName + "/sounds/" + Helper.makeFileName(soundTag.getCharacterExportFileName()) + "." + SoundExporter.getExportExtension(soundTag, soundExportSettings));
                        soundList.add(soundTag);
                    }
                }
            }

            try (Writer writer = new Utf8OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(tmp)))) {
                XMLStreamWriter xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(writer);

                xmlWriter.writeStartDocument();
                xmlWriter.writeComment("\r\nWARNING: The structure of this XML is not final.\r\nIn later versions of FFDec it can be changed.\r\nMake sure you use compatible reader/writer based on _xmlExportMajor/_xmlExportMinor keys.\r\n");

                exportXml(asmExternalFiles, tagExternalFiles, swf, xmlWriter);

                xmlWriter.writeEndDocument();
                xmlWriter.flush();
                xmlWriter.close();
            }

            //Test write to raise IOException
            try (FileOutputStream fos = new FileOutputStream(outFile)) {
                fos.write(1);
            }

            if (!new XmlPrettyFormat().prettyFormat(tmp, outFile, 2, true)) {
                logger.log(Level.SEVERE, "Cannot prettyformat XML");
            }
            tmp.delete();

            if (settings.as12ExportMode != null) {
                AS2ScriptExporter exporter = new AS2ScriptExporter();
                exporter.exportActionScript2(swf, handler, outFile.getParentFile().toPath().resolve(assetsDirName + "/scripts").toFile().getAbsolutePath(), new ScriptExportSettings(settings.as12ExportMode, false, false, false, false), true, evl);
            }
            if (settings.imageExportMode != null) {
                ImageExporter exporter = new ImageExporter();
                try {
                    exporter.exportImages(handler, outFile.getParentFile().toPath().resolve(assetsDirName + "/images").toFile().getAbsolutePath(), new ReadOnlyTagList(imagesList), new ImageExportSettings(settings.imageExportMode), evl);
                } catch (InterruptedException ex) {
                    return;
                }
            }
            if (settings.defineSoundExportMode != null) {
                SoundExporter exporter = new SoundExporter();
                try {
                    exporter.exportSounds(handler, outFile.getParentFile().toPath().resolve(assetsDirName + "/sounds").toFile().getAbsolutePath(), soundList, new SoundExportSettings(settings.defineSoundExportMode), evl);
                } catch (InterruptedException ex) {
                    return;
                }
            }
        } catch (XMLStreamException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Exports SWF to XML.
     *
     * @param asmExternalFiles ASM external files
     * @param tagExternalFiles Tag external files
     * @param swf SWF to export
     * @param writer XML writer
     * @throws IOException On I/O error
     * @throws XMLStreamException On XML error
     */
    private void exportXml(
            Map<ASMSource, String> asmExternalFiles,
            Map<Tag, String> tagExternalFiles,
            SWF swf,
            XMLStreamWriter writer
    ) throws IOException, XMLStreamException {
        generateXml(
                asmExternalFiles.isEmpty() && tagExternalFiles.isEmpty() ? XML_EXPORT_VERSION_MAJOR : XML_EXPORT_VERSION_MAJOR_WITH_EXTERNAL_FILES,
                asmExternalFiles,
                tagExternalFiles,
                swf,
                null,
                writer,
                "swf",
                swf,
                false
        );
    }

    public List<Field> getSwfFieldsCached(Class cls) {
        List<Field> result = cachedFields.get(cls);
        if (result == null) {
            result = ReflectionTools.getSwfFields(cls);

            result.removeIf((f) -> {
                return Modifier.isStatic(f.getModifiers()) || f.getAnnotation(Internal.class) != null;
            });

            result.sort((o1, o2) -> {

                boolean a1 = canBeAttribute(o1.getType());
                boolean a2 = canBeAttribute(o2.getType());

                if (a1 == a2 && a1 == true) {
                    return o1.getName().compareTo(o2.getName());
                }

                return a1 ? -1 : a2 ? 1 : 0;
            });

            cachedFields.put(cls, result);
        }

        return result;
    }

    private boolean isPrimitive(Class cls) {
        return cls != null && !cls.equals(Void.class) && (cls.isPrimitive()
                || cls == Short.class
                || cls == Integer.class
                || cls == Long.class
                || cls == Float.class
                || cls == Double.class
                || cls == Boolean.class
                || cls == Character.class
                || cls == String.class);
    }

    private boolean canBeAttribute(Class cls) {
        return cls != null && (isPrimitive(cls)
                || cls.equals(byte[].class)
                || ByteArrayRange.class.isAssignableFrom(cls)
                || cls.isEnum());
    }

    private boolean isList(Class cls) {
        return cls != null && (cls.isArray() || List.class.isAssignableFrom(cls));
    }

    private void generateXml(
            int major,
            Map<ASMSource, String> asmExternalFiles,
            Map<Tag, String> tagExternalFiles,
            SWF swf,
            Tag currentTag,
            XMLStreamWriter writer,
            String name,
            Object obj,
            boolean isListItem
    ) throws XMLStreamException {
        Class cls = obj != null ? obj.getClass() : null;

        /*if (obj != null && cls == String.class) {
            writer.writeStartElement(name);
            writer.writeAttribute("type", "String");
            writer.writeCData((String) obj);
            writer.writeEndElement();
        } else */
        if (obj != null && isPrimitive(cls)) {
            Object value = obj;
            String stringValue = Helper.escapeXmlExportString(value.toString());

            if (isListItem) {
                writer.writeStartElement(name);
                writer.writeCharacters(stringValue);
                writer.writeEndElement();
            } else {
                writer.writeAttribute(name, stringValue);
            }
        } else if (cls == Decimal128.class) {
            Decimal128 value = (Decimal128) obj;
            String stringValue = value.toActionScriptString();
            if (isListItem) {
                writer.writeStartElement(name);
                writer.writeCharacters(stringValue);
                writer.writeEndElement();
            } else {
                writer.writeAttribute(name, stringValue);
            }
        } else if (cls != null && obj != null && cls.isEnum()) {
            writer.writeAttribute(name, obj.toString());
        } else if (obj instanceof ByteArrayRange) {
            ByteArrayRange range = (ByteArrayRange) obj;
            byte[] data = range.getRangeData();
            writer.writeAttribute(name, Helper.byteArrayToHex(data));
        } else if (obj instanceof byte[]) {
            byte[] data = (byte[]) obj;
            writer.writeAttribute(name, Helper.byteArrayToHex(data));
        } else if (isList(cls)) {
            Object value = obj;
            if (List.class.isAssignableFrom(cls)) {
                value = ((List) value).toArray();
            }

            writer.writeStartElement(name);
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                generateXml(major, asmExternalFiles, tagExternalFiles, swf, currentTag, writer, "item", Array.get(value, i), true);
            }
            writer.writeEndElement();
        } else if (obj != null) {
            if (obj instanceof LazyObject) {
                ((LazyObject) obj).load();
            }

            Class clazz = obj.getClass();
            List<Field> fields = getSwfFieldsCached(clazz);

            if (obj instanceof InternalClass) {
                clazz = clazz.getSuperclass();
            }

            writer.writeStartElement(name);

            if (obj instanceof SWF) {
                writer.writeAttribute("_xmlExportMajor", "" + major);
                writer.writeAttribute("_xmlExportMinor", "" + XML_EXPORT_VERSION_MINOR);
                writer.writeAttribute("_generator", ApplicationInfo.applicationVerName);
                swf = (SWF) obj;
            }

            writer.writeAttribute("type", clazz.getSimpleName());

            if (obj instanceof UnknownTag) {
                writer.writeAttribute("tagId", String.valueOf(((Tag) obj).getId()));
            }
            if (obj instanceof SWF) {
                writer.writeAttribute("charset", ((SWF) obj).getCharset());
            }

            boolean isExternal = false;

            if (obj instanceof Tag) {
                currentTag = (Tag) obj;

                if (tagExternalFiles.containsKey((Tag) obj)) {
                    writer.writeAttribute("_externalFile", tagExternalFiles.get((Tag) obj));
                    isExternal = true;
                }
            }

            for (Field f : fields) {
                //Multiline multilineA = f.getAnnotation(Multiline.class);               

                if (isExternal && !"characterID".equals(f.getName()) && !"soundId".equals(f.getName())) {
                    continue;
                }

                Conditional cond = f.getAnnotation(Conditional.class);
                if (cond != null) {
                    ConditionEvaluator ev = new ConditionEvaluator(cond);
                    try {
                        Set<String> condFields = ev.getFields();
                        Map<String, Boolean> fieldMap = new HashMap<>();
                        for (String sf : condFields) {
                            try {
                                Object value = ReflectionTools.getValue(obj, clazz.getField(sf));
                                if (value instanceof Boolean) {
                                    fieldMap.put(sf, (Boolean) value);
                                }
                                if (value instanceof Integer) {
                                    int intValue = (Integer) value;
                                    boolean found = false;
                                    for (int i : cond.options()) {
                                        if (i == intValue) {
                                            found = true;
                                        }
                                    }
                                    fieldMap.put(sf, found);
                                }
                            } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException ex) {
                                fieldMap.put(sf, true);
                            }
                        }
                        if (!ev.eval(fieldMap, currentTag.getId())) {
                            continue;
                        }
                    } catch (AnnotationParseException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    }
                }

                try {
                    f.setAccessible(true);
                    Object value = f.get(obj);

                    if ("actionBytes".equals(f.getName())) {
                        if (obj instanceof ASMSource && asmExternalFiles.containsKey((ASMSource) obj)) {
                            value = new ByteArrayRange("00");
                            writer.writeAttribute("_externalActions", asmExternalFiles.get((ASMSource) obj));
                        } else if (obj instanceof DefineButtonTag) {
                            for (ASMSource s : asmExternalFiles.keySet()) {
                                if (s instanceof ButtonAction) {
                                    ButtonAction ba = (ButtonAction) s;
                                    if (ba.getSourceTag() == obj) {
                                        value = new ByteArrayRange("00");
                                        writer.writeAttribute("_externalActions", asmExternalFiles.get(s));
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    generateXml(major, asmExternalFiles, tagExternalFiles, swf, currentTag, writer, f.getName(), value, false);
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            }

            writer.writeEndElement();
        } else if (isListItem) {
            writer.writeStartElement(name);
            writer.writeAttribute("isNull", Boolean.TRUE.toString());
            writer.writeEndElement();
        }
    }
}
