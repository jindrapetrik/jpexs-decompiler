/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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
 * Instance info.
 *
 * @author JPEXS
 */
public class InstanceInfo {

    /**
     * Name index - multiname.
     */
    public int name_index;

    /**
     * Super index - multiname.
     */
    public int super_index;

    /**
     * Flags. 1 = sealed, 0 = dynamic, 2 = final, 4 = interface, 8 =
     * ProtectedNs, 16 = non nullable
     */
    public int flags;

    /**
     * Protected namespace.
     * If flag CLASS_PROTECTEDNS set.
     */
    public int protectedNS;

    /**
     * Interfaces.
     */
    public int[] interfaces;

    /**
     * Instance initializer (constructor) - method index.
     */
    public int iinit_index;

    /**
     * Instance traits.
     */
    public Traits instance_traits;

    /**
     * Not dynamic
     */
    public static final int CLASS_SEALED = 1; //

    /**
     * Final class
     */
    public static final int CLASS_FINAL = 2;

    /**
     * Interface
     */
    public static final int CLASS_INTERFACE = 4;

    /**
     * Has protected namespace
     */
    public static final int CLASS_PROTECTEDNS = 8;

    /**
     * Non nullable class
     */
    public static final int CLASS_NON_NULLABLE = 16;

    /**
     * True if class is deleted.
     */
    @Internal
    public boolean deleted;

    /**
     * Constructs a new InstanceInfo.
     */
    public InstanceInfo() {
        instance_traits = new Traits();
    }

    /**
     * Constructs a new InstanceInfo.
     *
     * @param traits Instance traits
     */
    public InstanceInfo(Traits traits) {
        instance_traits = traits;
    }

    /**
     * To string.
     *
     * @return String
     */
    @Override
    public String toString() {
        return "name_index=" + name_index + " super_index=" + super_index + " flags=" + flags + " protectedNS=" + protectedNS + " interfaces=" + Helper.intArrToString(interfaces) + " method_index=" + iinit_index + "\r\n" + instance_traits.toString();
    }

    /**
     * To string.
     *
     * @param abc ABC
     * @param fullyQualifiedNames Fully qualified names
     * @return String
     */
    public String toString(ABC abc, List<DottedChain> fullyQualifiedNames) {
        String supIndexStr = "[nothing]";
        if (super_index > 0) {
            supIndexStr = abc.constants.getMultiname(super_index).toString(abc.constants, fullyQualifiedNames);
        }
        return "name_index=" + abc.constants.getMultiname(name_index).toString(abc.constants, fullyQualifiedNames) + " super_index=" + supIndexStr + " flags=" + flags + " protectedNS=" + protectedNS + " interfaces=" + Helper.intArrToString(interfaces) + " method_index=" + iinit_index + "\r\n" + instance_traits.toString(abc, fullyQualifiedNames);
    }

    /**
     * Gets class header string.
     *
     * @param assetsDir Assets directory
     * @param writer Writer
     * @param abc ABC
     * @param fullyQualifiedNames Fully qualified names
     * @param allowPrivate Allow private
     * @param allowEmbed Allow embed
     * @return Writer
     */
    public GraphTextWriter getClassHeaderStr(String assetsDir, GraphTextWriter writer, ABC abc, List<DottedChain> fullyQualifiedNames, boolean allowPrivate, boolean allowEmbed) {

        final String ASSETS_DIR = assetsDir; // "/_assets/";
        if (allowEmbed) {
            if (abc.getSwf() != null) {
                String className = getName(abc.constants).getNameWithNamespace(abc.constants, false).toRawString();
                CharacterTag ct = abc.getSwf().getCharacterByClass(className);
                if (ct != null) {
                    String fileName = ct.getCharacterExportFileName();
                    if (Configuration.as3ExportNamesUseClassNamesOnly.get()) {
                        fileName = getName(abc.constants).getNameWithNamespace(abc.constants, false).toRawString();
                    }                    

                    String ext = "";
                    if (ct instanceof DefineBinaryDataTag) {
                        DefineBinaryDataTag db = (DefineBinaryDataTag) ct;
                        ext = db.innerSwf != null ? ".swf" : ".bin";
                    }
                    if (ct instanceof ImageTag) {
                        ImageTag it = (ImageTag) ct;
                        ext = it.getImageFormat().getExtension();
                    }
                    if (ct instanceof DefineSoundTag) {
                        DefineSoundTag st = (DefineSoundTag) ct;
                        if (st.getSoundFormat().formatId == SoundFormat.FORMAT_MP3) {
                            ext = ".mp3";
                        }
                    }
                    if (ct instanceof FontTag) {
                        ext = ".ttf";
                    }
                    if (ct instanceof DefineFont4Tag) {
                        ext = ".cff";
                    }
                    
                    fileName = Helper.makeFileName(fileName + ext);
                    
                    if (ct instanceof DefineBinaryDataTag) {
                        writer.appendNoHilight("[Embed(source=\"" + ASSETS_DIR + fileName + "\", mimeType=\"application/octet-stream\")]").newLine();
                    }
                    if (ct instanceof ImageTag) {
                        writer.appendNoHilight("[Embed(source=\"" + ASSETS_DIR + fileName + "\")]").newLine();
                    }
                    if (ct instanceof DefineSpriteTag) {
                        writer.appendNoHilight("[Embed(source=\"" + ASSETS_DIR + "assets.swf\", symbol=\"" + "symbol" + ct.getCharacterId() + "\")]").newLine();
                    }
                    if (ct instanceof DefineSoundTag) {
                        DefineSoundTag st = (DefineSoundTag) ct;
                        if (st.getSoundFormat().formatId == SoundFormat.FORMAT_MP3) {
                            writer.appendNoHilight("[Embed(source=\"" + ASSETS_DIR + fileName + "\")]").newLine();
                        } else {
                            writer.appendNoHilight("[Embed(source=\"" + ASSETS_DIR + "assets.swf\", symbol=\"" + "symbol" + ct.getCharacterId() + "\")]").newLine();
                        }                               
                    }
                    if (ct instanceof FontTag) {
                        FontTag ft = (FontTag) ct;

                        boolean hasFontAlignZones = false;
                        List<CharacterIdTag> sameIdTags = ft.getSwf().getCharacterIdTags(abc.getSwf().getCharacterId(ft));
                        for (CharacterIdTag sit : sameIdTags) {
                            if (sit instanceof DefineFontAlignZonesTag) {
                                hasFontAlignZones = true;
                                break;
                            }
                        }

                        writer.appendNoHilight("[Embed(source=\"" + ASSETS_DIR + fileName + "\",").newLine();
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
                        writer.appendNoHilight("[Embed(source=\"" + ASSETS_DIR + fileName + "\",").newLine();
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
        
        if (!isNullable()) {
            writer.appendNoHilight("!");
        }

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

    /**
     * Gets name.
     *
     * @param constants Constants
     * @return Multiname
     */
    public Multiname getName(AVM2ConstantPool constants) {
        return constants.getMultiname(name_index);
    }

    /**
     * Checks if class is interface.
     *
     * @return True if class is interface
     */
    public boolean isInterface() {
        return ((flags & CLASS_INTERFACE) == CLASS_INTERFACE);
    }

    /**
     * Checks if class is dynamic.
     *
     * @return True if class is dynamic
     */
    public boolean isDynamic() {
        return (flags & CLASS_SEALED) == 0;
    }

    /**
     * Checks if class is final.
     *
     * @return True if class is final
     */
    public boolean isFinal() {
        return (flags & CLASS_FINAL) == CLASS_FINAL;
    }

    /**
     * Checks if class is nullable.
     *
     * @return True if class is nullable
     */
    public boolean isNullable() {
        return (flags & CLASS_NON_NULLABLE) != CLASS_NON_NULLABLE;
    }
}
