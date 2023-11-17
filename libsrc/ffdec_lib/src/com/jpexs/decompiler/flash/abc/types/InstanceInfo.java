/*
 *  Copyright (C) 2010-2023 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.abc.types;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.hilight.HighlightSpecialType;
import com.jpexs.decompiler.flash.tags.DefineBinaryDataTag;
import com.jpexs.decompiler.flash.tags.DefineFont4Tag;
import com.jpexs.decompiler.flash.tags.DefineFontAlignZonesTag;
import com.jpexs.decompiler.flash.tags.DefineSoundTag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.base.CharacterIdTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.decompiler.flash.types.sound.SoundFormat;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.helpers.Helper;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class InstanceInfo {

    public int name_index;

    public int super_index;

    public int flags; // 1 = sealed, 0 = dynamic, 2 = final, 4 = interface, 8 = ProtectedNs, 16 = non nullable

    public int protectedNS; //if flags & 8

    public int[] interfaces;

    public int iinit_index; // MethodInfo - constructor

    public Traits instance_traits;

    public static final int CLASS_SEALED = 1; //not dynamic

    public static final int CLASS_FINAL = 2;

    public static final int CLASS_INTERFACE = 4;

    public static final int CLASS_PROTECTEDNS = 8;

    public static final int CLASS_NON_NULLABLE = 16; //This is somehow used in Flex, propably through annotations or something with Vector datatype (?)

    @Internal
    public boolean deleted;

    public InstanceInfo() {
        instance_traits = new Traits();
    }

    public InstanceInfo(Traits traits) {
        instance_traits = traits;
    }

    @Override
    public String toString() {
        return "name_index=" + name_index + " super_index=" + super_index + " flags=" + flags + " protectedNS=" + protectedNS + " interfaces=" + Helper.intArrToString(interfaces) + " method_index=" + iinit_index + "\r\n" + instance_traits.toString();
    }

    public String toString(ABC abc, List<DottedChain> fullyQualifiedNames) {
        String supIndexStr = "[nothing]";
        if (super_index > 0) {
            supIndexStr = abc.constants.getMultiname(super_index).toString(abc.constants, fullyQualifiedNames);
        }
        return "name_index=" + abc.constants.getMultiname(name_index).toString(abc.constants, fullyQualifiedNames) + " super_index=" + supIndexStr + " flags=" + flags + " protectedNS=" + protectedNS + " interfaces=" + Helper.intArrToString(interfaces) + " method_index=" + iinit_index + "\r\n" + instance_traits.toString(abc, fullyQualifiedNames);
    }

    public GraphTextWriter getClassHeaderStr(GraphTextWriter writer, ABC abc, List<DottedChain> fullyQualifiedNames, boolean allowPrivate, boolean allowEmbed) {

        final String ASSETS_DIR = "/_assets/";
        if (allowEmbed) {
            if (abc.getSwf() != null) {
                String className = getName(abc.constants).getNameWithNamespace(abc.constants, false).toRawString();
                CharacterTag ct = abc.getSwf().getCharacterByClass(className);
                if (ct != null) {
                    String fileName = ct.getCharacterExportFileName();
                    if (Configuration.as3ExportNamesUseClassNamesOnly.get()) {
                        fileName = getName(abc.constants).getNameWithNamespace(abc.constants, false).toRawString();
                    }
                    fileName = Helper.makeFileName(fileName);
            
                    if (ct instanceof DefineBinaryDataTag) {
                        writer.appendNoHilight("[Embed(source=\"" + ASSETS_DIR + fileName + ".bin\", mimeType=\"application/octet-stream\")]").newLine();
                    }
                    if (ct instanceof ImageTag) {
                        ImageTag it = (ImageTag) ct;
                        writer.appendNoHilight("[Embed(source=\"" + ASSETS_DIR + fileName + ((ImageTag) ct).getImageFormat().getExtension() + "\")]").newLine();
                    }
                    if (ct instanceof DefineSpriteTag) {
                        writer.appendNoHilight("[Embed(source=\"" + ASSETS_DIR + "assets.swf\", symbol=\"" + Helper.escapeActionScriptString(className) + "\")]").newLine();
                    }
                    if (ct instanceof DefineSoundTag) {
                        //should be mp3, otherwise it won't work. Should we convert this?
                        DefineSoundTag st = (DefineSoundTag) ct;
                        writer.appendNoHilight("[Embed(source=\"" + ASSETS_DIR + fileName + "." + (st.getSoundFormat().formatId == SoundFormat.FORMAT_MP3 ? "mp3" : "wav") + "\")]").newLine();
                    }
                    if (ct instanceof FontTag) {
                        FontTag ft = (FontTag) ct;

                        boolean hasFontAlignZones = false;
                        List<CharacterIdTag> sameIdTags = ft.getSwf().getCharacterIdTags(ft.getFontId());
                        for (CharacterIdTag sit : sameIdTags) {
                            if (sit instanceof DefineFontAlignZonesTag) {
                                hasFontAlignZones = true;
                                break;
                            }
                        }

                        writer.appendNoHilight("[Embed(source=\"" + ASSETS_DIR + fileName + ".ttf\",").newLine();
                        writer.appendNoHilight("fontName=\"" + Helper.escapeActionScriptString(ft.getFontNameIntag()) + "\",").newLine();
                        writer.appendNoHilight("fontFamily=\"" + Helper.escapeActionScriptString(ft.getFontName()) + "\",").newLine();
                        writer.appendNoHilight("mimeType=\"application/x-font\",").newLine();
                        writer.appendNoHilight("fontWeight=\"" + (ft.isBold() ? "bold" : "normal") + "\",").newLine();
                        writer.appendNoHilight("fontStyle=\"" + (ft.isItalic() ? "italic" : "normal") + "\",").newLine();
                        String fontChars = ft.getCharacters();
                        if (!fontChars.isEmpty()) {
                            Character firstC = null;
                            Character lastC = null;
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < fontChars.length(); i++) {
                                char c = fontChars.charAt(i);
                                if (firstC == null) {
                                    firstC = c;
                                    lastC = c;
                                    continue;
                                }

                                if (lastC + 1 != c) {
                                    if (sb.length() > 0) {
                                        sb.append(",");
                                    }
                                    if (firstC == lastC) {
                                        sb.append(String.format("U+%04X", (int) firstC));
                                    } else {
                                        sb.append(String.format("U+%04X-%04X", (int) firstC, (int) lastC));
                                    }
                                    firstC = c;
                                }
                                lastC = c;
                            }
                            if (sb.length() > 0) {
                                sb.append(",");
                            }
                            if (firstC == lastC) {
                                sb.append(String.format("U+%04X", (int) firstC));
                            } else {
                                sb.append(String.format("U+%04X-%04X", (int) firstC, (int) lastC));
                            }
                            writer.appendNoHilight("unicodeRange=\"").appendNoHilight(sb.toString()).appendNoHilight("\",").newLine();
                        }
                        writer.appendNoHilight("advancedAntiAliasing=\"" + (hasFontAlignZones ? "true" : "false") + "\",").newLine();
                        writer.appendNoHilight("embedAsCFF=\"false\"").newLine();
                        writer.appendNoHilight(")]").newLine();
                    }

                    if (ct instanceof DefineFont4Tag) {
                        DefineFont4Tag ft4 = (DefineFont4Tag) ct;
                        writer.appendNoHilight("[Embed(source=\"" + ASSETS_DIR + fileName + ".cff\",").newLine();
                        writer.appendNoHilight("fontName=\"" + Helper.escapeActionScriptString(ft4.fontName) + "\",").newLine();
                        writer.appendNoHilight("mimeType=\"application/x-font\",").newLine();
                        writer.appendNoHilight("fontWeight=\"" + (ft4.fontFlagsBold ? "bold" : "normal") + "\",").newLine();
                        writer.appendNoHilight("fontStyle=\"" + (ft4.fontFlagsItalic ? "italic" : "normal") + "\",").newLine();
                        writer.appendNoHilight("embedAsCFF=\"true\"").newLine();
                        writer.appendNoHilight(")]").newLine();
                    }
                }
            }
        }

        String modifiers;
        Namespace ns = abc.constants.getMultiname(name_index).getNamespace(abc.constants);
        modifiers = ns.getPrefix();
        if (!allowPrivate && modifiers.equals("private")) {
            modifiers = "";
        }
        if (!modifiers.isEmpty()) {
            modifiers += " ";
        }

        if (isFinal()) {
            modifiers += "final ";
        }
        if (!isInterface() && isDynamic()) {
            modifiers += "dynamic ";
        }
        String objType = "class ";
        if (isInterface()) {
            objType = "interface ";
        }

        writer.appendNoHilight(modifiers + objType);
        String classTypeName = abc.constants.getMultiname(name_index).getNameWithNamespace(abc.constants, true).toRawString();

        writer.hilightSpecial(abc.constants.getMultiname(name_index).getName(abc.constants, null/* No full names here*/, false, true), HighlightSpecialType.CLASS_NAME, classTypeName);

        if (super_index > 0) {
            String typeName = abc.constants.getMultiname(super_index).getNameWithNamespace(abc.constants, true).toRawString();
            String parentName = abc.constants.getMultiname(super_index).getName(abc.constants, fullyQualifiedNames, false, true);
            if (!parentName.equals("Object")) {
                writer.appendNoHilight(" extends ");
                writer.hilightSpecial(parentName, HighlightSpecialType.TYPE_NAME, typeName);
            }
        }
        if (interfaces.length > 0) {
            if (isInterface()) {
                writer.appendNoHilight(" extends ");
            } else {
                writer.appendNoHilight(" implements ");
            }
            for (int i = 0; i < interfaces.length; i++) {
                if (i > 0) {
                    writer.append(", ");
                }
                String typeName = abc.constants.getMultiname(interfaces[i]).getNameWithNamespace(abc.constants, true).toRawString();
                writer.hilightSpecial(abc.constants.getMultiname(interfaces[i]).getName(abc.constants, fullyQualifiedNames, false, true), HighlightSpecialType.TYPE_NAME, typeName);
            }
        }

        return writer;
    }

    public Multiname getName(AVM2ConstantPool constants) {
        return constants.getMultiname(name_index);
    }

    public boolean isInterface() {
        return ((flags & CLASS_INTERFACE) == CLASS_INTERFACE);
    }

    public boolean isDynamic() {
        return (flags & CLASS_SEALED) == 0;
    }

    public boolean isFinal() {
        return (flags & CLASS_FINAL) == CLASS_FINAL;
    }

    public boolean isNullable() {
        return (flags & CLASS_NON_NULLABLE) != CLASS_NON_NULLABLE;
    }
}
