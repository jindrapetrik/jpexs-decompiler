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
package com.jpexs.decompiler.flash;

import SevenZip.Compression.LZMA.Decoder;
import SevenZip.Compression.LZMA.Encoder;
import com.jpexs.debugger.flash.SWD;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ClassPath;
import com.jpexs.decompiler.flash.abc.RenameType;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.deobfuscation.AbcMultiNameCollisionFixer;
import com.jpexs.decompiler.flash.abc.avm2.deobfuscation.DeobfuscationLevel;
import com.jpexs.decompiler.flash.abc.avm2.model.GetLexAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.InitPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NameValuePair;
import com.jpexs.decompiler.flash.abc.avm2.model.NewArrayAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NewObjectAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.StringAVM2Item;
import com.jpexs.decompiler.flash.abc.types.ConvertData;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.ScriptInfo;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitClass;
import com.jpexs.decompiler.flash.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionGraphSource;
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.action.ActionListReader;
import com.jpexs.decompiler.flash.action.ActionLocalData;
import com.jpexs.decompiler.flash.action.model.ConstantPool;
import com.jpexs.decompiler.flash.action.model.DirectValueActionItem;
import com.jpexs.decompiler.flash.action.model.FunctionActionItem;
import com.jpexs.decompiler.flash.action.model.GetMemberActionItem;
import com.jpexs.decompiler.flash.action.model.GetVariableActionItem;
import com.jpexs.decompiler.flash.action.model.clauses.ClassActionItem;
import com.jpexs.decompiler.flash.action.model.clauses.InterfaceActionItem;
import com.jpexs.decompiler.flash.action.swf4.ActionEquals;
import com.jpexs.decompiler.flash.action.swf4.ActionGetVariable;
import com.jpexs.decompiler.flash.action.swf4.ActionIf;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf4.ActionSetVariable;
import com.jpexs.decompiler.flash.action.swf4.ConstantIndex;
import com.jpexs.decompiler.flash.action.swf5.ActionCallFunction;
import com.jpexs.decompiler.flash.action.swf5.ActionCallMethod;
import com.jpexs.decompiler.flash.action.swf5.ActionConstantPool;
import com.jpexs.decompiler.flash.action.swf5.ActionDefineFunction;
import com.jpexs.decompiler.flash.action.swf5.ActionDefineLocal;
import com.jpexs.decompiler.flash.action.swf5.ActionDefineLocal2;
import com.jpexs.decompiler.flash.action.swf5.ActionEquals2;
import com.jpexs.decompiler.flash.action.swf5.ActionGetMember;
import com.jpexs.decompiler.flash.action.swf5.ActionNewMethod;
import com.jpexs.decompiler.flash.action.swf5.ActionNewObject;
import com.jpexs.decompiler.flash.action.swf5.ActionSetMember;
import com.jpexs.decompiler.flash.action.swf7.ActionDefineFunction2;
import com.jpexs.decompiler.flash.cache.AS2Cache;
import com.jpexs.decompiler.flash.cache.AS3Cache;
import com.jpexs.decompiler.flash.cache.ScriptDecompiledListener;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.dumpview.DumpInfo;
import com.jpexs.decompiler.flash.dumpview.DumpInfoSwfNode;
import com.jpexs.decompiler.flash.ecma.Null;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.exporters.script.AS2ScriptExporter;
import com.jpexs.decompiler.flash.exporters.script.AS3ScriptExporter;
import com.jpexs.decompiler.flash.exporters.settings.ScriptExportSettings;
import com.jpexs.decompiler.flash.exporters.shape.ShapeExportData;
import com.jpexs.decompiler.flash.helpers.HighlightedText;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.helpers.NulWriter;
import com.jpexs.decompiler.flash.helpers.SWFDecompilerPlugin;
import com.jpexs.decompiler.flash.helpers.collections.MyEntry;
import com.jpexs.decompiler.flash.helpers.hilight.Highlighting;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.flash.tags.DebugIDTag;
import com.jpexs.decompiler.flash.tags.DefineBinaryDataTag;
import com.jpexs.decompiler.flash.tags.DefineSoundTag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.DoABC2Tag;
import com.jpexs.decompiler.flash.tags.DoABCTag;
import com.jpexs.decompiler.flash.tags.DoInitActionTag;
import com.jpexs.decompiler.flash.tags.EnableDebugger2Tag;
import com.jpexs.decompiler.flash.tags.EnableDebuggerTag;
import com.jpexs.decompiler.flash.tags.EnableTelemetryTag;
import com.jpexs.decompiler.flash.tags.EndTag;
import com.jpexs.decompiler.flash.tags.ExportAssetsTag;
import com.jpexs.decompiler.flash.tags.FileAttributesTag;
import com.jpexs.decompiler.flash.tags.JPEGTablesTag;
import com.jpexs.decompiler.flash.tags.MetadataTag;
import com.jpexs.decompiler.flash.tags.ProtectTag;
import com.jpexs.decompiler.flash.tags.SetBackgroundColorTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.SymbolClassTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.TagStub;
import com.jpexs.decompiler.flash.tags.VideoFrameTag;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.flash.tags.base.ASMSourceContainer;
import com.jpexs.decompiler.flash.tags.base.BoundedTag;
import com.jpexs.decompiler.flash.tags.base.ButtonTag;
import com.jpexs.decompiler.flash.tags.base.CharacterIdTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.DrawableTag;
import com.jpexs.decompiler.flash.tags.base.Exportable;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.ImportTag;
import com.jpexs.decompiler.flash.tags.base.MorphShapeTag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.tags.base.RemoveTag;
import com.jpexs.decompiler.flash.tags.base.RenderContext;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.tags.base.SoundTag;
import com.jpexs.decompiler.flash.tags.base.TextTag;
import com.jpexs.decompiler.flash.tags.enums.ImageFormat;
import com.jpexs.decompiler.flash.tags.gfx.DefineCompactedFont;
import com.jpexs.decompiler.flash.timeline.AS2Package;
import com.jpexs.decompiler.flash.timeline.Frame;
import com.jpexs.decompiler.flash.timeline.FrameScript;
import com.jpexs.decompiler.flash.timeline.TagScript;
import com.jpexs.decompiler.flash.timeline.Timeline;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.treeitems.SWFList;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.decompiler.flash.types.annotations.SWFField;
import com.jpexs.decompiler.flash.xfl.FLAVersion;
import com.jpexs.decompiler.flash.xfl.XFLConverter;
import com.jpexs.decompiler.flash.xfl.XFLExportSettings;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.Graph;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemContainer;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.ScopeStack;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.model.IfItem;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.Cache;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.ImmediateFuture;
import com.jpexs.helpers.NulStream;
import com.jpexs.helpers.ProgressListener;
import com.jpexs.helpers.SerializableImage;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * Class representing SWF file
 *
 * @author JPEXS
 */
public final class SWF implements SWFContainerItem, Timelined {

    /**
     * Default version of SWF file format
     */
    public static final int DEFAULT_VERSION = 10;

    /**
     * Maximum SWF file format version Needs to be fixed when SWF versions
     * reaches this value
     */
    public static final int MAX_VERSION = 64;

    /**
     * Tags inside of file
     */
    @SWFField
    private List<Tag> tags = new ArrayList<>();

    @Internal
    public ReadOnlyTagList readOnlyTags;

    public boolean hasEndTag = true;

    /**
     * ExportRectangle for the display
     */
    public RECT displayRect;

    /**
     * Movie frame rate
     */
    public float frameRate;

    /**
     * Number of frames in movie
     */
    public int frameCount;

    /**
     * Version of SWF
     */
    public int version;

    /**
     * Uncompressed size of the file
     */
    @Internal
    public long fileSize;

    /**
     * Used compression mode
     */
    public SWFCompression compression = SWFCompression.NONE;

    /**
     * Compressed size of the file (LZMA)
     */
    @Internal
    public long compressedSize;

    /**
     * LZMA Properties
     */
    public byte[] lzmaProperties;

    @Internal
    public byte[] uncompressedData;

    @Internal
    public byte[] originalUncompressedData;

    /**
     * ScaleForm GFx
     */
    public boolean gfx = false;

    @Internal
    public SWFList swfList;

    @Internal
    private String file;

    @Internal
    private String fileTitle;

    @Internal
    private volatile Map<Integer, CharacterTag> characters;

    @Internal
    private volatile Map<Integer, List<CharacterIdTag>> characterIdTags;

    @Internal
    private volatile Map<Integer, Set<Integer>> dependentCharacters;

    @Internal
    private volatile List<ABCContainerTag> abcList;

    @Internal
    private volatile JPEGTablesTag jtt;

    @Internal
    public Map<Integer, String> sourceFontNamesMap = new HashMap<>();

    public static final double unitDivisor = 20;

    private static final Logger logger = Logger.getLogger(SWF.class.getName());

    @Internal
    private boolean isModified;

    @Internal
    private Timeline timeline;

    @Internal
    public DumpInfoSwfNode dumpInfo;

    @Internal
    public DefineBinaryDataTag binaryData;

    @Internal
    private final HashMap<DottedChain, DottedChain> deobfuscated = new HashMap<>();

    @Internal
    private final IdentifiersDeobfuscation deobfuscation = new IdentifiersDeobfuscation();

    @Internal
    private final Cache<String, SerializableImage> frameCache = Cache.getInstance(false, false, "frame");

    @Internal
    private final Cache<CharacterTag, RECT> rectCache = Cache.getInstance(true, true, "rect");

    @Internal
    private final Cache<SHAPE, ShapeExportData> shapeExportDataCache = Cache.getInstance(true, true, "shapeExportData");

    @Internal
    private final Cache<SoundTag, byte[]> soundCache = Cache.getInstance(false, false, "sound");

    @Internal
    public final AS2Cache as2Cache = new AS2Cache();

    @Internal
    public final AS3Cache as3Cache = new AS3Cache();

    @Internal
    private Map<String, ASMSource> asmsCacheExportFilenames;

    @Internal
    private Map<String, ASMSource> asmsCache;

    private static final DecompilerPool decompilerPool = new DecompilerPool();

    public static final String AS2_PKG_PREFIX = "__Packages.";

    public static List<String> swfSignatures = Arrays.asList(
            "FWS", // Uncompressed Flash
            "CWS", // ZLib compressed Flash
            "ZWS", // LZMA compressed Flash
            "GFX", // Uncompressed ScaleForm GFx
            "CFX", // Compressed ScaleForm GFx
            "ABC" // Non-standard LZMA compressed Flash
    );

    public void updateCharacters() {
        characters = null;
        characterIdTags = null;
    }

    public void clearTagSwfs() {
        resetTimelines(this);
        updateCharacters();

        for (Tag tag : getTags()) {
            if (tag instanceof DefineSpriteTag) {
                DefineSpriteTag spriteTag = (DefineSpriteTag) tag;
                for (Tag tag1 : spriteTag.getTags()) {
                    tag1.setSwf(null);
                    tag1.setTimelined(null);
                }

                for (int i = spriteTag.getTags().size() - 1; i >= 0; i--) {
                    spriteTag.removeTag(i);
                }
            }

            if (tag instanceof DefineBinaryDataTag) {
                DefineBinaryDataTag binaryTag = (DefineBinaryDataTag) tag;
                if (binaryTag.innerSwf != null) {
                    binaryTag.innerSwf.clearTagSwfs();
                }
            }

            tag.setSwf(null);
            tag.setTimelined(null);
        }

        tags.clear();
        if (abcList != null) {

            for (ABCContainerTag c : abcList) {
                c.getABC().free();
            }
            abcList.clear();
        }

        if (swfList != null) {
            swfList.swfs.clear();
        }

        clearScriptCache();
        frameCache.clear();
        soundCache.clear();

        timeline = null;
        clearDumpInfo(dumpInfo);
        dumpInfo = null;
        jtt = null;
        binaryData = null;
    }

    private void clearDumpInfo(DumpInfo di) {
        for (DumpInfo childInfo : di.getChildInfos()) {
            clearDumpInfo(childInfo);
        }

        di.getChildInfos().clear();
    }

    public Map<Integer, CharacterTag> getCharacters() {
        if (characters == null) {
            synchronized (this) {
                if (characters == null) {
                    Map<Integer, CharacterTag> chars = new HashMap<>();
                    Map<Integer, List<CharacterIdTag>> charIdtags = new HashMap<>();
                    parseCharacters(getTags(), chars, charIdtags);
                    characters = Collections.unmodifiableMap(chars);
                    characterIdTags = Collections.unmodifiableMap(charIdtags);
                }
            }
        }

        return characters;
    }

    public List<CharacterIdTag> getCharacterIdTags(int characterId) {
        if (characterIdTags == null) {
            getCharacters();
        }

        return characterIdTags.get(characterId);
    }

    public CharacterIdTag getCharacterIdTag(int characterId, int tagId) {
        List<CharacterIdTag> characterIdTags = getCharacterIdTags(characterId);
        if (characterIdTags != null) {
            for (CharacterIdTag t : characterIdTags) {
                if (((Tag) t).getId() == tagId) {
                    if (t.getCharacterId() == characterId) {
                        return t;
                    }
                }
            }
        }

        return null;
    }

    public Map<Integer, Set<Integer>> getDependentCharacters() {
        if (dependentCharacters == null) {
            synchronized (this) {
                if (dependentCharacters == null) {
                    Map<Integer, Set<Integer>> dep = new HashMap<>();
                    for (Tag tag : getTags()) {
                        if (tag instanceof CharacterTag) {
                            int characterId = ((CharacterTag) tag).getCharacterId();
                            Set<Integer> needed = new HashSet<>();
                            tag.getNeededCharacters(needed);
                            for (Integer needed1 : needed) {
                                Set<Integer> s = dep.get(needed1);
                                if (s == null) {
                                    s = new HashSet<>();
                                    dep.put(needed1, s);
                                }

                                s.add(characterId);
                            }
                        }
                    }

                    dependentCharacters = dep;
                }
            }
        }

        return dependentCharacters;
    }

    public Set<Integer> getDependentCharacters(int characterId) {
        Set<Integer> visited = new HashSet<>();

        Set<Integer> dependents2 = new LinkedHashSet<>();
        Set<Integer> deps = getDependentCharacters().get(characterId);
        if (deps != null) {
            dependents2.addAll(deps);
        }

        while (visited.size() != dependents2.size()) {
            for (int chId : dependents2) {
                if (!visited.contains(chId)) {
                    visited.add(chId);
                    if (getCharacters().containsKey(chId)) {
                        deps = getDependentCharacters().get(chId);
                        if (deps != null) {
                            dependents2.addAll(deps);
                        }

                        break;
                    }
                }
            }
        }

        Set<Integer> dependents = new LinkedHashSet<>();
        for (Integer chId : dependents2) {
            if (getCharacters().containsKey(chId)) {
                dependents.add(chId);
            }
        }

        return dependents;
    }

    public CharacterTag getCharacter(int characterId) {
        return getCharacters().get(characterId);
    }

    public String getExportName(int characterId) {
        CharacterTag characterTag = getCharacters().get(characterId);
        String exportName = characterTag != null ? characterTag.getExportName() : null;
        return exportName;
    }

    public FontTag getFontByClass(String fontClass) {
        if (fontClass == null) {
            return null;
        }
        for (Tag t : getTags()) {
            if (t instanceof FontTag) {
                if (fontClass.equals(((FontTag) t).getClassName())) {
                    return (FontTag) t;
                }
            }
        }
        return null;
    }

    public FontTag getFontByName(String fontName) {
        if (fontName == null) {
            return null;
        }
        for (Tag t : getTags()) {
            if (t instanceof FontTag) {
                if (fontName.equals(((FontTag) t).getFontName())) {
                    return (FontTag) t;
                }
            }
        }
        return null;
    }

    public FontTag getFont(int fontId) {
        CharacterTag characterTag = getCharacters().get(fontId);
        if (characterTag instanceof FontTag) {
            return (FontTag) characterTag;
        }

        if (characterTag != null) {
            logger.log(Level.SEVERE, "CharacterTag should be a FontTag. characterId: {0}", fontId);
        }

        return null;
    }

    public ImageTag getImage(int imageId) {
        CharacterTag characterTag = getCharacters().get(imageId);
        if (characterTag instanceof ImageTag) {
            return (ImageTag) characterTag;
        }

        if (characterTag != null) {
            logger.log(Level.SEVERE, "CharacterTag should be an ImageTag. characterId: {0}", imageId);
        }

        return null;
    }

    public DefineSoundTag getSound(int soundId) {
        CharacterTag characterTag = getCharacters().get(soundId);
        if (characterTag instanceof DefineSoundTag) {
            return (DefineSoundTag) characterTag;
        }

        if (characterTag != null) {
            logger.log(Level.SEVERE, "CharacterTag should be a DefineSoundTag. characterId: {0}", soundId);
        }

        return null;
    }

    public TextTag getText(int textId) {
        CharacterTag characterTag = getCharacters().get(textId);
        if (characterTag instanceof TextTag) {
            return (TextTag) characterTag;
        }

        if (characterTag != null) {
            logger.log(Level.SEVERE, "CharacterTag should be a TextTag. characterId: {0}", textId);
        }

        return null;
    }

    public List<ABCContainerTag> getAbcList() {
        if (abcList == null) {
            synchronized (this) {
                if (abcList == null) {
                    ArrayList<ABCContainerTag> newAbcList = new ArrayList<>();
                    getAbcTags(getTags(), newAbcList);
                    abcList = newAbcList;
                }
            }
        }

        return abcList;
    }

    public boolean isAS3() {
        FileAttributesTag fileAttributes = getFileAttributes();
        return (fileAttributes != null && fileAttributes.actionScript3) || (fileAttributes == null && !getAbcList().isEmpty());
    }

    public MetadataTag getMetadata() {
        for (Tag t : getTags()) {
            if (t instanceof MetadataTag) {
                return (MetadataTag) t;
            }
        }

        return null;
    }

    public FileAttributesTag getFileAttributes() {
        for (Tag t : getTags()) {
            if (t instanceof FileAttributesTag) {
                return (FileAttributesTag) t;
            }
        }

        return null;
    }

    public SetBackgroundColorTag getBackgroundColor() {
        for (Tag t : getTags()) {
            if (t instanceof SetBackgroundColorTag) {
                return (SetBackgroundColorTag) t;
            }
        }

        return null;
    }

    public EnableTelemetryTag getEnableTelemetry() {
        for (Tag t : getTags()) {
            if (t instanceof EnableTelemetryTag) {
                return (EnableTelemetryTag) t;
            }
        }
        return null;
    }

    public int getNextCharacterId() {
        int max = 0;
        Set<Integer> ids = new HashSet<>(getCharacters().keySet());
        for (Tag t : tags) {
            if (t instanceof ImportTag) {
                ids.addAll(((ImportTag) t).getAssets().keySet());
            }
        }
        for (int characterId : ids) {
            if (characterId > max) {
                max = characterId;
            }
        }

        return max + 1;
    }

    public synchronized JPEGTablesTag getJtt() {
        if (jtt == null) {
            synchronized (this) {
                if (jtt == null) {
                    for (Tag t : getTags()) {
                        if (t instanceof JPEGTablesTag) {
                            jtt = (JPEGTablesTag) t;
                            break;
                        }
                    }
                }
            }
        }

        return jtt;
    }

    public String getDocumentClass() {
        for (Tag t : getTags()) {
            if (t instanceof SymbolClassTag) {
                SymbolClassTag sc = (SymbolClassTag) t;
                for (int i = 0; i < sc.tags.size(); i++) {
                    if (sc.tags.get(i) == 0) {
                        return sc.names.get(i);
                    }
                }
            }
        }

        return null;
    }

    public void fixCharactersOrder(boolean checkAll) {
        Set<Integer> addedCharacterIds = new HashSet<>();
        Set<CharacterTag> movedTags = new HashSet<>();
        for (int i = 0; i < tags.size(); i++) {
            Tag tag = tags.get(i);
            if (checkAll || tag.isModified()) {
                Set<Integer> needed = new HashSet<>();
                tag.getNeededCharacters(needed);
                if (tag instanceof CharacterTag) {
                    CharacterTag characterTag = (CharacterTag) tag;
                    needed.remove(characterTag.getCharacterId());
                }
                boolean moved = false;
                for (Integer id : needed) {
                    if (!addedCharacterIds.contains(id)) {
                        CharacterTag neededCharacter = getCharacter(id);
                        if (neededCharacter == null) {
                            continue;
                        }

                        if (movedTags.contains(neededCharacter)) {
                            logger.log(Level.SEVERE, "Fixing characters order failed, recursion detected.");
                            return;
                        }

                        // move the needed character to the current position
                        tags.remove(neededCharacter);
                        tags.add(i, neededCharacter);
                        movedTags.add(neededCharacter);
                        moved = true;
                    }
                }

                if (moved) {
                    i--;
                    continue;
                }
            }
            if (tag instanceof CharacterTag) {
                addedCharacterIds.add(((CharacterTag) tag).getCharacterId());
            }
        }
    }

    public void resetTimelines(Timelined timelined) {
        timelined.resetTimeline();
        if (timelined instanceof SWF) {
            for (Tag t : ((SWF) timelined).getTags()) {
                if (t instanceof Timelined) {
                    resetTimelines((Timelined) t);
                }
            }
        }
    }

    private void parseCharacters(Iterable<Tag> list, Map<Integer, CharacterTag> characters, Map<Integer, List<CharacterIdTag>> characterIdTags) {
        for (Tag t : list) {
            if (t instanceof CharacterIdTag) {
                int characterId = ((CharacterIdTag) t).getCharacterId();
                if (t instanceof CharacterTag) {
                    if (characters.containsKey(characterId)) {
                        logger.log(Level.SEVERE, "SWF already contains characterId={0}", characterId);
                    }

                    if (characterId != 0) {
                        characters.put(characterId, (CharacterTag) t);
                        characterIdTags.put(characterId, new ArrayList<>());
                    }
                } else if (characterIdTags.containsKey(characterId)) {
                    characterIdTags.get(characterId).add((CharacterIdTag) t);
                }
            }

            if (t instanceof DefineSpriteTag) {
                parseCharacters(((DefineSpriteTag) t).getTags(), characters, characterIdTags);
            }
        }
    }

    /**
     * Unresolve recursive sprites
     */
    private void checkInvalidSprites() {
        for (int i = 0; i < tags.size(); i++) {
            Tag t = tags.get(i);
            if (t instanceof DefineSpriteTag) {
                if (!isSpriteValid((DefineSpriteTag) t, new ArrayList<>())) {
                    tags.set(i, new TagStub(this, t.getId(), "InvalidSprite", t.getOriginalRange(), null));
                }
            }
        }
    }

    private boolean isSpriteValid(DefineSpriteTag sprite, List<Integer> path) {
        if (path.contains(sprite.spriteId)) {
            return false;
        }
        path.add(sprite.spriteId);
        for (Tag t : sprite.getTags()) {
            if (t instanceof DefineSpriteTag) {
                if (!isSpriteValid((DefineSpriteTag) t, path)) {
                    return false;
                }
            }
        }
        path.remove((Integer) sprite.spriteId);
        return true;
    }

    @Override
    public Timeline getTimeline() {
        if (timeline == null) {
            timeline = new Timeline(this);
        }
        return timeline;
    }

    @Override
    public void resetTimeline() {
        if (timeline != null) {
            timeline.reset(this);
        }
    }

    /**
     * Gets all tags with specified id
     *
     * @param tagId Identificator of tag type
     * @return List of tags
     */
    public List<Tag> getTagData(int tagId) {
        List<Tag> ret = new ArrayList<>();
        for (Tag tag : getTags()) {
            if (tag.getId() == tagId) {
                ret.add(tag);
            }
        }
        return ret;
    }

    /**
     * Saves this SWF into new file
     *
     * @param os OutputStream to save SWF in
     * @throws IOException
     */
    public void saveTo(OutputStream os) throws IOException {
        byte[] uncompressedData = saveToByteArray();
        compress(new ByteArrayInputStream(uncompressedData), os, compression, lzmaProperties);
    }

    public void saveTo(OutputStream os, boolean gfx) throws IOException {
        byte[] uncompressedData = saveToByteArray(gfx);
        compress(new ByteArrayInputStream(uncompressedData), os, compression, lzmaProperties);
    }

    public byte[] getHeaderBytes() {
        return getHeaderBytes(compression, gfx);
    }

    private static byte[] getHeaderBytes(SWFCompression compression, boolean gfx) {
        if (compression == SWFCompression.LZMA_ABC) {
            return new byte[]{'A', 'B', 'C'};
        }

        byte[] ret = new byte[3];

        if (compression == SWFCompression.LZMA) {
            ret[0] = 'Z';
        } else if (compression == SWFCompression.ZLIB) {
            ret[0] = 'C';
        } else if (gfx) {
            ret[0] = 'G';
        } else {
            ret[0] = 'F';
        }

        if (gfx) {
            ret[1] = 'F';
            ret[2] = 'X';
        } else {
            ret[1] = 'W';
            ret[2] = 'S';
        }

        return ret;
    }

    private byte[] saveToByteArray() throws IOException {
        return saveToByteArray(gfx);
    }

    private byte[] saveToByteArray(boolean gfx) throws IOException {
        fixCharactersOrder(false);

        byte[] data;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                SWFOutputStream sos = new SWFOutputStream(baos, version)) {
            sos.write(getHeaderBytes(SWFCompression.NONE, gfx));
            sos.writeUI8(version);
            sos.writeUI32(0); // placeholder for file length
            sos.writeRECT(displayRect);
            sos.writeFIXED8(frameRate);
            sos.writeUI16(frameCount);

            sos.writeTags(getLocalTags());
            if (hasEndTag) {
                sos.writeUI16(0);
            }

            data = baos.toByteArray();
        }

        // update file size
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                SWFOutputStream sos = new SWFOutputStream(baos, version)) {
            sos.writeUI32(data.length);
            byte[] lengthData = baos.toByteArray();
            System.arraycopy(lengthData, 0, data, 4, lengthData.length);
        }

        return data;
    }

    /**
     * Compress SWF file
     *
     * @param is InputStream
     * @param os OutputStream to save SWF in
     * @param compression
     * @param lzmaProperties
     * @throws IOException
     */
    private static void compress(InputStream is, OutputStream os, SWFCompression compression, byte[] lzmaProperties) throws IOException {
        byte[] hdr = new byte[8];

        is.mark(8);

        // SWFheader: signature, version and fileSize
        if (is.read(hdr) != 8) {
            throw new SwfOpenException("SWF header is too short");
        }

        boolean uncompressed = hdr[0] == 'F' || hdr[0] == 'G'; // FWS or GFX
        if (!uncompressed) {
            // fisrt decompress, then compress to the given format
            is.reset();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            decompress(is, baos, false);
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            compress(bais, os, compression, lzmaProperties);
            return;
        }

        boolean gfx = hdr[1] == 'F' && hdr[2] == 'X';
        int version = hdr[3];
        long fileSize;
        try (SWFInputStream sis = new SWFInputStream(null, Arrays.copyOfRange(hdr, 4, 8), 4, 4)) {
            fileSize = sis.readUI32("fileSize");
        }

        SWFOutputStream sos = new SWFOutputStream(os, version);
        sos.write(getHeaderBytes(compression, gfx));
        sos.writeUI8(version);
        sos.writeUI32(fileSize);

        if (compression == SWFCompression.LZMA || compression == SWFCompression.LZMA_ABC) {
            long uncompressedLength = fileSize - 8;
            Encoder enc = new Encoder();
            if (lzmaProperties == null) {
                // todo: the bytes are from a sample swf
                lzmaProperties = new byte[]{93, 0, 0, 32, 0};
            }

            int val = lzmaProperties[0] & 0xFF;
            int lc = val % 9;
            int remainder = val / 9;
            int lp = remainder % 5;
            int pb = remainder / 5;
            int dictionarySize = 0;
            for (int i = 0; i < 4; i++) {
                dictionarySize += ((int) (lzmaProperties[1 + i]) & 0xFF) << (i * 8);
            }
            if (Configuration.lzmaFastBytes.get() > 0) {
                enc.SetNumFastBytes(Configuration.lzmaFastBytes.get());
            }
            enc.SetDictionarySize(dictionarySize);
            enc.SetLcLpPb(lc, lp, pb);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            enc.SetEndMarkerMode(true);
            enc.Code(is, baos, -1, -1, null);
            byte[] data = baos.toByteArray();
            if (compression == SWFCompression.LZMA) {
                byte[] udata = new byte[4];
                udata[0] = (byte) (data.length & 0xFF);
                udata[1] = (byte) ((data.length >> 8) & 0xFF);
                udata[2] = (byte) ((data.length >> 16) & 0xFF);
                udata[3] = (byte) ((data.length >> 24) & 0xFF);
                os.write(udata);
            }
            enc.WriteCoderProperties(os);
            if (compression == SWFCompression.LZMA_ABC) {
                byte[] udata = new byte[8];
                udata[0] = (byte) (uncompressedLength & 0xFF);
                udata[1] = (byte) ((uncompressedLength >> 8) & 0xFF);
                udata[2] = (byte) ((uncompressedLength >> 16) & 0xFF);
                udata[3] = (byte) ((uncompressedLength >> 24) & 0xFF);
                udata[4] = (byte) ((uncompressedLength >> 32) & 0xFF);
                udata[5] = (byte) ((uncompressedLength >> 40) & 0xFF);
                udata[6] = (byte) ((uncompressedLength >> 48) & 0xFF);
                udata[7] = (byte) ((uncompressedLength >> 56) & 0xFF);
                os.write(udata);
            }
            os.write(data);
        } else if (compression == SWFCompression.ZLIB) {
            DeflaterOutputStream dos = new DeflaterOutputStream(os);
            try {
                Helper.copyStream(is, dos);
            } finally {
                dos.finish();
            }
        } else {
            Helper.copyStream(is, os);
        }
    }

    @Override
    public boolean isModified() {
        if (isModified) {
            return true;
        }

        for (Tag tag : getTags()) {
            if (tag.isModified()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void setModified(boolean value) {
        isModified = value;
    }

    public void clearModified() {
        for (Tag tag : getTags()) {
            if (tag.isModified()) {
                tag.createOriginalData();
                tag.setModified(false);
            }
        }

        isModified = false;

        try {
            uncompressedData = saveToByteArray();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Cannot save SWF", ex);
        }
    }

    /**
     * Constructs an empty SWF
     */
    public SWF() {
        version = SWF.DEFAULT_VERSION;
        displayRect = new RECT(0, 1, 0, 1);
    }

    /**
     * Construct SWF from stream
     *
     * @param is Stream to read SWF from
     * @param parallelRead Use parallel threads?
     * @throws IOException
     * @throws java.lang.InterruptedException
     */
    public SWF(InputStream is, boolean parallelRead) throws IOException, InterruptedException {
        this(is, null, null, null, parallelRead, false, true);
    }

    /**
     * Construct SWF from stream
     *
     * @param is Stream to read SWF from
     * @param parallelRead Use parallel threads?
     * @param lazy
     * @throws IOException
     * @throws java.lang.InterruptedException
     */
    public SWF(InputStream is, boolean parallelRead, boolean lazy) throws IOException, InterruptedException {
        this(is, null, null, null, parallelRead, false, lazy);
    }

    /**
     * Construct SWF from stream
     *
     * @param is Stream to read SWF from
     * @param file Path to the file
     * @param fileTitle Title of the SWF
     * @param parallelRead Use parallel threads?
     * @throws IOException
     * @throws java.lang.InterruptedException
     */
    public SWF(InputStream is, String file, String fileTitle, boolean parallelRead) throws IOException, InterruptedException {
        this(is, file, fileTitle, null, parallelRead, false, true);
    }

    /**
     * Construct SWF from stream
     *
     * @param is Stream to read SWF from
     * @param listener
     * @param parallelRead Use parallel threads?
     * @throws IOException
     * @throws java.lang.InterruptedException
     */
    public SWF(InputStream is, ProgressListener listener, boolean parallelRead) throws IOException, InterruptedException {
        this(is, null, null, listener, parallelRead, false, true);
    }

    /**
     * Construct SWF from stream
     *
     * @param is Stream to read SWF from
     * @param file Path to the file
     * @param fileTitle Title of the SWF
     * @param listener
     * @param parallelRead Use parallel threads?
     * @throws IOException
     * @throws java.lang.InterruptedException
     */
    public SWF(InputStream is, String file, String fileTitle, ProgressListener listener, boolean parallelRead) throws IOException, InterruptedException {
        this(is, file, fileTitle, listener, parallelRead, false, true);
    }

    /**
     * Faster constructor to check SWF only
     *
     * @param is
     * @throws java.io.IOException
     */
    public SWF(InputStream is) throws IOException {
        decompress(is, new NulStream(), true);
    }

    public SWF(InputStream is, String file, String fileTitle, ProgressListener listener, boolean parallelRead, boolean checkOnly, boolean lazy) throws IOException, InterruptedException {
        this(is, file, fileTitle, listener, parallelRead, checkOnly, lazy, null);
    }

    /**
     * Construct SWF from stream
     *
     * @param is Stream to read SWF from
     * @param file Path to the file
     * @param fileTitle Title of the SWF
     * @param listener
     * @param parallelRead Use parallel threads?
     * @param checkOnly Check only file validity
     * @param lazy
     * @param resolver Resolver for imported tags
     * @throws IOException
     * @throws java.lang.InterruptedException
     */
    public SWF(InputStream is, String file, String fileTitle, ProgressListener listener, boolean parallelRead, boolean checkOnly, boolean lazy, UrlResolver resolver) throws IOException, InterruptedException {
        this.file = file;
        this.fileTitle = fileTitle;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SWFHeader header = decompress(is, baos, true);
        gfx = header.gfx;
        compression = header.compression;
        lzmaProperties = header.lzmaProperties;
        uncompressedData = baos.toByteArray();
        originalUncompressedData = uncompressedData;

        SWFInputStream sis = new SWFInputStream(this, uncompressedData);
        dumpInfo = new DumpInfoSwfNode(this, "rootswf", "", null, 0, 0);
        sis.dumpInfo = dumpInfo;
        sis.skipBytesEx(3, "signature"); // skip siganture
        version = sis.readUI8("version");
        fileSize = sis.readUI32("fileSize");
        dumpInfo.lengthBytes = fileSize;
        if (listener != null) {
            sis.addPercentListener(listener);
        }
        sis.setPercentMax(fileSize);
        displayRect = sis.readRECT("displayRect");
        frameRate = sis.readFIXED8("frameRate");
        frameCount = sis.readUI16("frameCount");
        List<Tag> tags = sis.readTagList(this, 0, parallelRead, true, !checkOnly, lazy);
        if (tags.size() > 0 && tags.get(tags.size() - 1).getId() == EndTag.ID) {
            tags.remove(tags.size() - 1);
        } else {
            hasEndTag = false;
        }
        this.tags = tags;
        readOnlyTags = null;
        if (!checkOnly) {
            checkInvalidSprites();
            updateCharacters();
            assignExportNamesToSymbols();
            assignClassesToSymbols();
            if (resolver != null) {
                resolveImported(resolver);
            }
            SWFDecompilerPlugin.fireSwfParsed(this);
        } else {
            boolean hasNonUnknownTag = false;
            for (Tag tag : tags) {
                if (tag.getOriginalDataLength() > 0 && Tag.getRequiredTags().contains(tag.getId())) {
                    hasNonUnknownTag = true;
                }
            }
            if (!hasNonUnknownTag) {
                throw new IOException("Invalid SWF file. No known tag found.");
            }
        }

        if (Configuration.autoRenameIdentifiers.get()) {
            deobfuscateIdentifiers(RenameType.TYPENUMBER);
            AbcMultiNameCollisionFixer collisionFixer = new AbcMultiNameCollisionFixer();
            collisionFixer.fixCollisions(this);
            assignClassesToSymbols();
            clearScriptCache();
        }

        getASMs(true); // Add scriptNames to ASMs
    }

    private void resolveImported(UrlResolver resolver) {
        for (int p = 0; p < tags.size(); p++) {
            Tag t = tags.get(p);
            if (t instanceof ImportTag) {
                ImportTag importTag = (ImportTag) t;

                SWF iSwf = resolver.resolveUrl(importTag.getUrl());
                if (iSwf != null) {
                    Map<Integer, String> exportedMap1 = new HashMap<>();
                    Map<Integer, String> classesMap1 = new HashMap<>();

                    for (Tag t2 : iSwf.tags) {
                        if (t2 instanceof ExportAssetsTag) {
                            ExportAssetsTag sc = (ExportAssetsTag) t2;
                            Map<Integer, String> m2 = sc.getTagToNameMap();
                            for (int key : m2.keySet()) {
                                if (!exportedMap1.containsKey(key)) {
                                    exportedMap1.put(key, m2.get(key));
                                }
                            }
                        }
                        if (t2 instanceof SymbolClassTag) {
                            SymbolClassTag sc = (SymbolClassTag) t2;
                            Map<Integer, String> m2 = sc.getTagToNameMap();
                            for (int key : m2.keySet()) {
                                if (!classesMap1.containsKey(key)) {
                                    classesMap1.put(key, m2.get(key));
                                }
                            }
                        }
                    }
                    Map<String, Integer> exportedMap2 = new HashMap<>();
                    for (int k : exportedMap1.keySet()) {
                        exportedMap2.put(exportedMap1.get(k), k);
                    }

                    Map<String, Integer> classesMap2 = new HashMap<>();
                    for (int k : classesMap1.keySet()) {
                        classesMap2.put(classesMap1.get(k), k);
                    }

                    Map<Integer, String> importedMap1 = importTag.getAssets();
                    Map<String, Integer> importedMap2 = new HashMap<>();
                    for (int k : importedMap1.keySet()) {
                        importedMap2.put(importedMap1.get(k), k);
                    }

                    int pos = 0;
                    for (String key : importedMap2.keySet()) {
                        if (!exportedMap2.containsKey(key)) {
                            continue; //?
                        }
                        int exportedId = exportedMap2.get(key);
                        int importedId = importedMap2.get(key);
                        for (Tag cht : iSwf.tags) {
                            if ((cht instanceof CharacterIdTag) && (((CharacterIdTag) cht).getCharacterId() == exportedId) && !(cht instanceof PlaceObjectTypeTag) && !(cht instanceof RemoveTag)) {
                                CharacterIdTag ch = (CharacterIdTag) cht;
                                cht.setSwf(this);
                                ch.setCharacterId(importedId);
                                cht.setImported(true);
                                tags.add(p + 1 + pos, cht);
                                pos++;
                            }
                        }
                    }

                    int newId = getNextCharacterId();
                    pos = 0;
                    for (String key : classesMap2.keySet()) {
                        int exportedId = classesMap2.get(key);
                        int importedId = newId++;
                        for (Tag cht : iSwf.tags) {
                            if ((cht instanceof CharacterIdTag) && (((CharacterIdTag) cht).getCharacterId() == exportedId) && !(cht instanceof PlaceObjectTypeTag) && !(cht instanceof RemoveTag)) {
                                CharacterIdTag ch = (CharacterIdTag) cht;
                                cht.setSwf(this);
                                ch.setCharacterId(importedId);
                                cht.setImported(true);
                                tags.add(p + 1 + pos, cht);
                                pos++;
                            }
                        }
                    }
                    updateCharacters();
                }
            }
        }
    }

    @Override
    public SWF getSwf() {
        return this;
    }

    public SWF getRootSwf() {
        SWF result = this;
        while (result.binaryData != null) {
            result = result.binaryData.getSwf();
        }

        return result;
    }

    public String getFile() {
        return file;
    }

    /**
     * Get title of the file
     *
     * @return file title
     */
    public String getFileTitle() {
        if (fileTitle != null) {
            return fileTitle;
        }
        return file;
    }

    public String getShortFileName() {
        String title = getFileTitle();
        if (title == null) {
            return "";
        }
        return new File(title).getName();
    }

    public void setFile(String file) {
        this.file = file;
        fileTitle = null;
    }

    public Date getFileModificationDate() {
        try {
            if (swfList != null && swfList.sourceInfo != null) {
                String fileName = swfList.sourceInfo.getFile();
                if (fileName != null) {
                    long lastModified = new File(fileName).lastModified();
                    if (lastModified > 0) {
                        return new Date(lastModified);
                    }
                }
            }
        } catch (SecurityException sex) {
        }

        return new Date();
    }

    private static void getAbcTags(Iterable<Tag> list, List<ABCContainerTag> actionScripts) {
        for (Tag t : list) {
            if (t instanceof DefineSpriteTag) {
                getAbcTags(((DefineSpriteTag) t).getTags(), actionScripts);
            }
            if (t instanceof ABCContainerTag) {
                actionScripts.add((ABCContainerTag) t);
            }
        }
    }

    public void assignExportNamesToSymbols() {
        HashMap<Integer, String> exportNames = new HashMap<>();
        for (Tag t : getTags()) {
            if (t instanceof ExportAssetsTag) {
                ExportAssetsTag eat = (ExportAssetsTag) t;
                for (int i = 0; i < eat.tags.size(); i++) {
                    Integer tagId = eat.tags.get(i);
                    String name = eat.names.get(i);
                    if ((!exportNames.containsKey(tagId)) && (!exportNames.containsValue(name))) {
                        exportNames.put(tagId, name);
                    }
                }
            }
        }
        for (Tag t : getTags()) {
            if (t instanceof CharacterTag) {
                CharacterTag ct = (CharacterTag) t;
                if (exportNames.containsKey(ct.getCharacterId())) {
                    ct.setExportName(exportNames.get(ct.getCharacterId()));
                }
            }
        }
    }

    public void assignClassesToSymbols() {
        HashMap<Integer, String> classes = new HashMap<>();
        for (Tag t : getTags()) {
            if (t instanceof SymbolClassTag) {
                SymbolClassTag sct = (SymbolClassTag) t;
                for (int i = 0; i < sct.tags.size(); i++) {
                    if ((!classes.containsKey(sct.tags.get(i))) && (!classes.containsValue(sct.names.get(i)))) {
                        classes.put(sct.tags.get(i), sct.names.get(i));
                    }
                }
            }
        }
        for (Tag t : getTags()) {
            if (t instanceof CharacterTag) {
                CharacterTag ct = (CharacterTag) t;
                if (classes.containsKey(ct.getCharacterId())) {
                    ct.setClassName(classes.get(ct.getCharacterId()));
                }
            }
        }
    }

    /**
     * Compress SWF file
     *
     * @param fis Input stream
     * @param fos Output stream
     * @param compression
     * @return True on success
     */
    public static boolean compress(InputStream fis, OutputStream fos, SWFCompression compression) {
        try {
            compress(fis, fos, compression, null);
        } catch (IOException ex) {
            return false;
        }
        return true;
    }

    public static boolean decompress(InputStream fis, OutputStream fos) {
        try {
            decompress(fis, fos, false);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    private static void decodeLZMAStream(InputStream is, OutputStream os, byte[] lzmaProperties, long fileSize) throws IOException {
        Decoder decoder = new Decoder();
        if (!decoder.SetDecoderProperties(lzmaProperties)) {
            throw new IOException("LZMA:Incorrect stream properties");
        }
        if (!decoder.Code(is, os, fileSize - 8)) {
            throw new IOException("LZMA:Error in data stream");
        }
    }

    public static SWFHeader decodeHeader(byte[] headerData) throws IOException {
        String signature = new String(headerData, 0, 3, Utf8Helper.charset);
        if (!swfSignatures.contains(signature)) {
            throw new SwfOpenException("Invalid SWF file, wrong signature.");
        }

        int version = headerData[3];
        long fileSize;
        try (SWFInputStream sis = new SWFInputStream(null, Arrays.copyOfRange(headerData, 4, 8), 4, 4)) {
            fileSize = sis.readUI32("fileSize");
        }

        SWFHeader header = new SWFHeader();
        header.version = version;
        header.fileSize = fileSize;
        header.gfx = headerData[1] == 'F' && headerData[2] == 'X';
        return header;
    }

    private static SWFHeader decompress(InputStream is, OutputStream os, boolean allowUncompressed) throws IOException {

        byte[] hdr = new byte[8];

        // SWFheader: signature, version and fileSize
        if (is.read(hdr) != 8) {
            throw new SwfOpenException("SWF header is too short");
        }

        SWFHeader header = decodeHeader(hdr);
        long fileSize = header.fileSize;

        try (SWFOutputStream sos = new SWFOutputStream(os, header.version)) {
            sos.write(getHeaderBytes(SWFCompression.NONE, header.gfx));
            sos.writeUI8(header.version);
            sos.writeUI32(fileSize);

            switch (hdr[0]) {
                case 'C': { // CWS, CFX
                    Helper.copyStream(new InflaterInputStream(is), os, fileSize - 8);
                    header.compression = SWFCompression.ZLIB;
                    break;
                }
                case 'Z': { // ZWS
                    byte[] lzmaprop = new byte[9];
                    is.read(lzmaprop);
                    try (SWFInputStream sis = new SWFInputStream(null, lzmaprop)) {
                        sis.readUI32("LZMAsize"); // compressed LZMA data size = compressed SWF - 17 byte,
                        // where 17 = 8 byte header + this 4 byte + 5 bytes decoder properties

                        int propertiesSize = 5;
                        byte[] lzmaProperties = sis.readBytes(propertiesSize, "lzmaproperties");
                        if (lzmaProperties.length != propertiesSize) {
                            throw new IOException("LZMA:input .lzma file is too short");
                        }

                        decodeLZMAStream(is, os, lzmaProperties, fileSize);

                        header.compression = SWFCompression.LZMA;
                        header.lzmaProperties = lzmaProperties;
                    }
                    break;
                }
                case 'A': { // ABC
                    byte[] lzmaProperties = new byte[5];
                    is.read(lzmaProperties);
                    byte[] uncompressedLength = new byte[8];
                    is.read(uncompressedLength);

                    decodeLZMAStream(is, os, lzmaProperties, fileSize);

                    header.compression = SWFCompression.LZMA_ABC;
                    header.lzmaProperties = lzmaProperties;
                    break;
                }
                default: { // FWS, GFX
                    if (allowUncompressed) {
                        Helper.copyStream(is, os, fileSize - 8);
                    } else {
                        throw new IOException("SWF is not compressed");
                    }
                }
            }

            return header;
        }
    }

    public static boolean renameInvalidIdentifiers(RenameType renameType, InputStream fis, OutputStream fos) {
        try {
            SWF swf = new SWF(fis, Configuration.parallelSpeedUp.get());
            int cnt = swf.deobfuscateIdentifiers(renameType);
            swf.assignClassesToSymbols();
            System.out.println(cnt + " identifiers renamed.");
            swf.saveTo(fos);
        } catch (InterruptedException | IOException ex) {
            return false;
        }
        return true;
    }

    public List<ScriptPack> getScriptPacksByClassNames(List<String> classNames) throws Exception {
        Set<ScriptPack> resultSet = new HashSet<>();

        List<ABCContainerTag> abcList = getAbcList();
        List<ABC> allAbcList = new ArrayList<>();
        for (int i = 0; i < abcList.size(); i++) {
            allAbcList.add(abcList.get(i).getABC());
        }

        for (String className : classNames) {
            for (int i = 0; i < abcList.size(); i++) {
                ABC abc = abcList.get(i).getABC();
                List<ScriptPack> scrs = abc.findScriptPacksByPath(className, allAbcList);
                for (int j = 0; j < scrs.size(); j++) {
                    ScriptPack scr = scrs.get(j);
                    resultSet.add(scr);
                }
            }
        }

        return new ArrayList<>(resultSet);
    }

    private List<ScriptPack> uniqueAS3Packs(List<ScriptPack> packs) {
        List<ScriptPack> ret = new ArrayList<>();
        Set<ClassPath> classPaths = new HashSet<>();
        for (ScriptPack item : packs) {
            ClassPath key = item.getClassPath();
            if (classPaths.contains(key)) {
                logger.log(Level.SEVERE, "Duplicate pack path found ({0})!", key);
            } else {
                classPaths.add(key);
                ret.add(item);
            }
        }
        return ret;
    }

    public List<ScriptPack> getAS3Packs() {
        List<ScriptPack> packs = new ArrayList<>();

        List<ABCContainerTag> abcList = getAbcList();
        List<ABC> allAbcList = new ArrayList<>();
        for (int i = 0; i < abcList.size(); i++) {
            allAbcList.add(abcList.get(i).getABC());
        }

        for (ABCContainerTag abcTag : abcList) {
            packs.addAll(abcTag.getABC().getScriptPacks(null, allAbcList));
        }
        return uniqueAS3Packs(packs);
    }

    @Override
    public RECT getRect() {
        return displayRect;
    }

    @Override
    public RECT getRect(Set<BoundedTag> added) {
        return displayRect;
    }

    public EventListener getExportEventListener() {
        EventListener evl = new EventListener() {
            @Override
            public void handleExportingEvent(String type, int index, int count, Object data) {
                for (EventListener listener : listeners) {
                    listener.handleExportingEvent(type, index, count, data);
                }
            }

            @Override
            public void handleExportedEvent(String type, int index, int count, Object data) {
                for (EventListener listener : listeners) {
                    listener.handleExportedEvent(type, index, count, data);
                }
            }

            @Override
            public void handleEvent(String event, Object data) {
                informListeners(event, data);
            }
        };

        return evl;
    }

    public List<File> exportActionScript(AbortRetryIgnoreHandler handler, String outdir, ScriptExportSettings exportSettings, boolean parallel, EventListener evl) throws IOException {
        return exportActionScript(handler, outdir, null, exportSettings, parallel, evl, true, true);
    }

    public List<File> exportActionScript(AbortRetryIgnoreHandler handler, String outdir, List<ScriptPack> as3scripts, ScriptExportSettings exportSettings, boolean parallel, EventListener evl, boolean as2, boolean as3) throws IOException {
        List<File> ret = new ArrayList<>();

        if (isAS3()) {
            if (as3) {
                ret.addAll(new AS3ScriptExporter().exportActionScript3(this, handler, outdir, as3scripts, exportSettings, parallel, evl));
            }
        } else if (as2) {
            ret.addAll(new AS2ScriptExporter().exportAS2Scripts(handler, outdir, getASMs(true), exportSettings, parallel, evl));
        }
        return ret;
    }

    public Map<String, ASMSource> getASMs(boolean exportFileNames) {
        return getASMs(exportFileNames, new ArrayList<>(), true);
    }

    public Map<String, ASMSource> getASMs(boolean exportFileNames, List<TreeItem> nodesToExport, boolean exportAll) {
        if (exportAll) {
            if (exportFileNames && asmsCacheExportFilenames != null) {
                return asmsCacheExportFilenames;
            }
            if (!exportFileNames && asmsCache != null) {
                return asmsCache;
            }
        }
        Map<String, ASMSource> asmsToExport = new LinkedHashMap<>();
        for (TreeItem treeItem : getFirstLevelASMNodes(null)) {
            getASMs(exportFileNames, treeItem, nodesToExport, exportAll, asmsToExport, File.separator + getASMPath(exportFileNames, treeItem));
        }
        if (exportAll) {
            if (exportFileNames) {
                asmsCacheExportFilenames = asmsToExport;
            } else {
                asmsCache = asmsToExport;
            }
        }
        return asmsToExport;
    }

    private void getASMs(boolean exportFileNames, TreeItem treeItem, List<TreeItem> nodesToExport, boolean exportAll, Map<String, ASMSource> asmsToExport, String path) {
        TreeItem realItem = treeItem instanceof TagScript ? ((TagScript) treeItem).getTag() : treeItem;
        boolean exportNode = nodesToExport.contains(treeItem) || nodesToExport.contains(realItem);

        if (realItem instanceof ASMSource && (exportAll || exportNode)) {
            String npath = path;
            String exPath = path;
            int ppos = 1;
            while (asmsToExport.containsKey(npath)) {
                ppos++;
                npath = path + (exportFileNames ? "[" + ppos + "]" : "_" + ppos);
                exPath = path + "[" + ppos + "]";
            }
            ((ASMSource) realItem).setScriptName(exPath);
            asmsToExport.put(npath, (ASMSource) realItem);
        }

        if (treeItem instanceof TagScript) {
            TagScript tagScript = (TagScript) treeItem;
            for (TreeItem subItem : tagScript.getFrames()) {
                getASMs(exportFileNames, subItem, nodesToExport, exportAll, asmsToExport, path + File.separator + getASMPath(exportFileNames, subItem));
            }
        } else if (treeItem instanceof FrameScript) {
            FrameScript frameScript = (FrameScript) treeItem;
            Frame parentFrame = frameScript.getFrame();
            for (TreeItem subItem : parentFrame.actionContainers) {
                getASMs(exportFileNames, getASMWrapToTagScript(subItem), nodesToExport, exportAll || exportNode, asmsToExport, path + File.separator + getASMPath(exportFileNames, subItem));
            }
            for (TreeItem subItem : parentFrame.actions) {
                getASMs(exportFileNames, getASMWrapToTagScript(subItem), nodesToExport, exportAll || exportNode, asmsToExport, path + File.separator + getASMPath(exportFileNames, subItem));
            }
        } else if (treeItem instanceof AS2Package) {
            AS2Package as2Package = (AS2Package) treeItem;
            for (TreeItem subItem : as2Package.subPackages.values()) {
                getASMs(exportFileNames, subItem, nodesToExport, exportAll, asmsToExport, path + File.separator + getASMPath(exportFileNames, subItem));
            }
            for (TreeItem subItem : as2Package.scripts.values()) {
                getASMs(exportFileNames, subItem, nodesToExport, exportAll, asmsToExport, path + File.separator + getASMPath(exportFileNames, subItem));
            }
        }
    }

    private String getASMPath(boolean exportFileName, TreeItem treeItem) {
        if (!exportFileName) {
            return treeItem.toString();
        }

        String result;
        if (treeItem instanceof Exportable) {
            result = ((Exportable) treeItem).getExportFileName();
        } else {
            result = treeItem.toString();
        }

        return Helper.makeFileName(result);
    }

    private TreeItem getASMWrapToTagScript(TreeItem treeItem) {
        if (treeItem instanceof Tag) {
            Tag resultTag = (Tag) treeItem;
            List<TreeItem> subNodes = new ArrayList<>();
            if (treeItem instanceof ASMSourceContainer) {
                for (ASMSource item : ((ASMSourceContainer) treeItem).getSubItems()) {
                    subNodes.add(item);
                }
            }

            TagScript tagScript = new TagScript(treeItem.getSwf(), resultTag, subNodes);
            return tagScript;
        }

        return treeItem;
    }

    public List<TreeItem> getFirstLevelASMNodes(Map<Tag, TagScript> tagScriptCache) {
        Timeline timeline = getTimeline();
        List<TreeItem> subNodes = new ArrayList<>();
        List<TreeItem> subFrames = new ArrayList<>();
        subNodes.addAll(timeline.getAS2RootPackage().subPackages.values());
        subNodes.addAll(timeline.getAS2RootPackage().scripts.values());

        for (ASMSource ass : timeline.asmSources) {
            if (ass instanceof DoInitActionTag) {
                String exportName = getExportName(((DoInitActionTag) ass).spriteId);
                if (exportName == null) {
                    subNodes.add(ass);
                }
            }
        }

        for (Tag tag : timeline.otherTags) {
            boolean hasInnerFrames = false;
            List<TreeItem> tagSubNodes = new ArrayList<>();
            if (tag instanceof Timelined) {
                Timeline timeline2 = ((Timelined) tag).getTimeline();
                for (Frame frame : timeline2.getFrames()) {
                    if (!frame.actions.isEmpty() || !frame.actionContainers.isEmpty()) {
                        FrameScript frameScript = new FrameScript(this, frame);
                        tagSubNodes.add(frameScript);
                        hasInnerFrames = true;
                    }
                }
            }

            if (tag instanceof ASMSourceContainer) {
                for (ASMSource asm : ((ASMSourceContainer) tag).getSubItems()) {
                    tagSubNodes.add(asm);
                }
            }

            if (!tagSubNodes.isEmpty()) {
                TagScript ts = new TagScript(this, tag, tagSubNodes);
                if (tagScriptCache != null) {
                    tagScriptCache.put(tag, ts);
                }
                if (hasInnerFrames) {
                    subFrames.add(ts);
                } else {
                    subNodes.add(ts);
                }
            }
        }

        subNodes.addAll(subFrames);
        for (Frame frame : timeline.getFrames()) {
            if (!frame.actions.isEmpty() || !frame.actionContainers.isEmpty()) {
                FrameScript frameScript = new FrameScript(this, frame);
                subNodes.add(frameScript);
            }
        }

        return subNodes;
    }

    private final HashSet<EventListener> listeners = new HashSet<>();

    public final void addEventListener(EventListener listener) {
        listeners.add(listener);
        for (Tag t : getTags()) {
            if (t instanceof ABCContainerTag) {
                (((ABCContainerTag) t).getABC()).addEventListener(listener);
            }
        }
    }

    public final void removeEventListener(EventListener listener) {
        listeners.remove(listener);
        for (Tag t : getTags()) {
            if (t instanceof ABCContainerTag) {
                (((ABCContainerTag) t).getABC()).removeEventListener(listener);
            }
        }
    }

    protected void informListeners(String event, Object data) {
        for (EventListener listener : listeners) {
            listener.handleEvent(event, data);
        }
    }

    public static void populateVideoFrames(int streamId, Iterable<Tag> tags, HashMap<Integer, VideoFrameTag> output) {
        for (Tag t : tags) {
            if (t instanceof VideoFrameTag) {
                VideoFrameTag videoFrameTag = (VideoFrameTag) t;
                if (videoFrameTag.streamID == streamId) {
                    output.put(videoFrameTag.frameNum, (VideoFrameTag) t);
                }
            }
            if (t instanceof DefineSpriteTag) {
                populateVideoFrames(streamId, ((DefineSpriteTag) t).getTags(), output);
            }
        }
    }

    private static void writeLE(OutputStream os, long val, int size) throws IOException {
        for (int i = 0; i < size; i++) {
            os.write((int) (val & 0xff));
            val >>= 8;
        }
    }

    public static void createWavFromPcmData(OutputStream fos, int soundRateHz, boolean soundSize, boolean soundType, byte[] data) throws IOException {
        ByteArrayOutputStream subChunk1Data = new ByteArrayOutputStream();
        int audioFormat = 1; // PCM
        writeLE(subChunk1Data, audioFormat, 2);
        int numChannels = soundType ? 2 : 1;
        writeLE(subChunk1Data, numChannels, 2);
        int[] rateMap = {5512, 11025, 22050, 44100};
        int sampleRate = soundRateHz; // rateMap[soundRate];
        writeLE(subChunk1Data, sampleRate, 4);
        int bitsPerSample = soundSize ? 16 : 8;
        int byteRate = sampleRate * numChannels * bitsPerSample / 8;
        writeLE(subChunk1Data, byteRate, 4);
        int blockAlign = numChannels * bitsPerSample / 8;
        writeLE(subChunk1Data, blockAlign, 2);
        writeLE(subChunk1Data, bitsPerSample, 2);

        ByteArrayOutputStream chunks = new ByteArrayOutputStream();
        chunks.write(Utf8Helper.getBytes("fmt "));
        byte[] subChunk1DataBytes = subChunk1Data.toByteArray();
        writeLE(chunks, subChunk1DataBytes.length, 4);
        chunks.write(subChunk1DataBytes);

        chunks.write(Utf8Helper.getBytes("data"));
        writeLE(chunks, data.length, 4);
        chunks.write(data);

        fos.write(Utf8Helper.getBytes("RIFF"));
        byte[] chunkBytes = chunks.toByteArray();
        writeLE(fos, 4 + chunkBytes.length, 4);
        fos.write(Utf8Helper.getBytes("WAVE"));
        fos.write(chunkBytes);
    }

    public static String getTypePrefix(CharacterTag c) {
        if (c instanceof ShapeTag) {
            return "shape";
        }
        if (c instanceof MorphShapeTag) {
            return "morphshape";
        }
        if (c instanceof DefineSpriteTag) {
            return "sprite";
        }
        if (c instanceof TextTag) {
            return "text";
        }
        if (c instanceof ButtonTag) {
            return "button";
        }
        if (c instanceof FontTag) {
            return "font";
        }
        if (c instanceof ImageTag) {
            return "image";
        }
        return "character";
    }

    public static void writeLibrary(SWF fswf, Set<Integer> library, OutputStream fos) throws IOException {
        for (int c : library) {
            CharacterTag ch = fswf.getCharacter(c);
            if (ch instanceof FontTag) {
                StringBuilder sb = new StringBuilder();
                sb.append("function ").append(getTypePrefix(ch)).append(c).append("(ctx,ch,textColor){\r\n");
                ((FontTag) ch).toHtmlCanvas(sb, 1);
                sb.append("}\r\n\r\n");
                fos.write(Utf8Helper.getBytes(sb.toString()));
            } else {
                if (ch instanceof ImageTag) {
                    ImageTag image = (ImageTag) ch;
                    ImageFormat format = image.getImageFormat();
                    byte[] imageData = Helper.readStream(image.getImageData());
                    String base64ImgData = Helper.byteArrayToBase64String(imageData);
                    fos.write(Utf8Helper.getBytes("var imageObj" + c + " = document.createElement(\"img\");\r\nimageObj" + c + ".src=\"data:image/" + format + ";base64," + base64ImgData + "\";\r\n"));
                }
                fos.write(Utf8Helper.getBytes("function " + getTypePrefix(ch) + c + "(ctx,ctrans,frame,ratio,time){\r\n"));
                if (ch instanceof DrawableTag) {
                    StringBuilder sb = new StringBuilder();
                    ((DrawableTag) ch).toHtmlCanvas(sb, 1);
                    fos.write(Utf8Helper.getBytes(sb.toString()));
                }
                fos.write(Utf8Helper.getBytes("}\r\n\r\n"));
            }
        }
    }

    private static void getVariables(ConstantPool constantPool, BaseLocalData localData, TranslateStack stack, List<GraphTargetItem> output, ActionGraphSource code, int ip, List<MyEntry<DirectValueActionItem, ConstantPool>> variables, List<GraphSourceItem> functions, HashMap<DirectValueActionItem, ConstantPool> strings, List<Integer> visited, HashMap<DirectValueActionItem, String> usageTypes, String path) throws InterruptedException {
        ActionLocalData aLocalData = (ActionLocalData) localData;
        boolean debugMode = false;
        while ((ip > -1) && ip < code.size()) {
            if (visited.contains(ip)) {
                break;
            }
            GraphSourceItem ins = code.get(ip);

            if (debugMode) {
                System.err.println("Visit " + ip + ": ofs" + Helper.formatAddress(((Action) ins).getAddress()) + ":" + ((Action) ins).getASMSource(new ActionList(), new HashSet<>(), ScriptExportMode.PCODE) + " stack:" + Helper.stackToString(stack, LocalData.create(new ConstantPool())));
            }
            if (ins.isExit()) {
                break;
            }
            if (ins.isIgnored()) {
                ip++;
                continue;
            }

            String usageType = "name";
            GraphTargetItem name = null;
            if ((ins instanceof ActionGetVariable)
                    || (ins instanceof ActionGetMember)
                    || (ins instanceof ActionDefineLocal2)
                    || (ins instanceof ActionNewMethod)
                    || (ins instanceof ActionNewObject)
                    || (ins instanceof ActionCallMethod)
                    || (ins instanceof ActionCallFunction)) {
                if (stack.isEmpty()) {
                    break;
                }
                name = stack.peek();
            }

            if ((ins instanceof ActionGetVariable) || (ins instanceof ActionDefineLocal2)) {
                usageType = "variable";
            }
            if (ins instanceof ActionGetMember) {
                usageType = "member";
            }
            if ((ins instanceof ActionNewMethod) || (ins instanceof ActionNewObject)) {
                usageType = "class";
            }
            if (ins instanceof ActionCallMethod) {
                usageType = "function"; // can there be method?
            }
            if (ins instanceof ActionCallFunction) {
                usageType = "function";
            }

            if ((ins instanceof ActionDefineFunction) || (ins instanceof ActionDefineFunction2)) {
                functions.add(ins);
            }

            if (ins instanceof GraphSourceItemContainer) {
                GraphSourceItemContainer cnt = (GraphSourceItemContainer) ins;
                List<Long> cntSizes = cnt.getContainerSizes();
                long addr = code.pos2adr(ip + 1);
                ip = code.adr2pos(addr);
                String cntName = cnt.getName();
                for (Long size : cntSizes) {
                    if (size == 0) {
                        continue;
                    }
                    ip = code.adr2pos(addr);
                    addr += size;
                    int nextip = code.adr2pos(addr);
                    getVariables(aLocalData.insideDoInitAction, variables, functions, strings, usageTypes, new ActionGraphSource(path, aLocalData.insideDoInitAction, code.getActions().subList(ip, nextip), code.version, new HashMap<>(), new HashMap<>(), new HashMap<>()), 0, path + (cntName == null ? "" : "/" + cntName));
                    ip = nextip;
                }
                List<List<GraphTargetItem>> r = new ArrayList<>();
                r.add(new ArrayList<>());
                r.add(new ArrayList<>());
                r.add(new ArrayList<>());
                ((GraphSourceItemContainer) ins).translateContainer(r, ins, stack, output, new HashMap<>(), new HashMap<>(), new HashMap<>());
                continue;
            }

            if ((ins instanceof ActionSetVariable) || (ins instanceof ActionSetMember) || (ins instanceof ActionDefineLocal)) {
                if (stack.size() < 2) {
                    break;
                }
                name = stack.get(stack.size() - 2);
            }

            if ((ins instanceof ActionSetVariable) || (ins instanceof ActionDefineLocal)) {
                usageType = "variable";
            }

            if (ins instanceof ActionSetMember) {
                usageType = "member";
            }

            if (name instanceof DirectValueActionItem) {
                variables.add(new MyEntry<>((DirectValueActionItem) name, constantPool));
                usageTypes.put((DirectValueActionItem) name, usageType);
            }

            // for..in return
            if (((ins instanceof ActionEquals) || (ins instanceof ActionEquals2)) && (stack.size() == 1) && (stack.peek() instanceof DirectValueActionItem)) {
                stack.push(new DirectValueActionItem(null, null, 0, Null.INSTANCE, new ArrayList<>()));
            }

            if (ins instanceof ActionConstantPool) {
                constantPool = new ConstantPool(((ActionConstantPool) ins).constantPool);
            }
            int staticOperation = Graph.SOP_USE_STATIC; //(Boolean) Configuration.getConfig("autoDeobfuscate", true) ? Graph.SOP_SKIP_STATIC : Graph.SOP_USE_STATIC;

            int requiredStackSize = ins.getStackPopCount(localData, stack);
            if (stack.size() < requiredStackSize) {
                // probably obfucated code, never executed branch
                break;
            }

            ins.translate(localData, stack, output, staticOperation, path);
            if (ins.isExit()) {
                break;
            }

            if (ins instanceof ActionPush) {
                if (!stack.isEmpty()) {
                    GraphTargetItem top = stack.peek();
                    if (top instanceof DirectValueActionItem) {
                        DirectValueActionItem dvt = (DirectValueActionItem) top;
                        if ((dvt.value instanceof String) || (dvt.value instanceof ConstantIndex)) {
                            if (constantPool == null) {
                                constantPool = new ConstantPool(dvt.constants);
                            }
                            strings.put(dvt, constantPool);
                        }
                    }
                }
            }

            if (ins.isBranch() || ins.isJump()) {
                if (ins instanceof ActionIf) {
                    if (stack.isEmpty()) {
                        break;
                    }
                    stack.pop();
                }
                visited.add(ip);
                List<Integer> branches = ins.getBranches(code);
                for (int b : branches) {
                    TranslateStack brStack = (TranslateStack) stack.clone();
                    if (b >= 0) {
                        getVariables(constantPool, localData, brStack, output, code, b, variables, functions, strings, visited, usageTypes, path);
                    } else if (debugMode) {
                        System.out.println("Negative branch:" + b);
                    }
                }
                // }
                break;
            }
            ip++;
        }
    }

    private static void getVariables(boolean insideDoInitAction, List<MyEntry<DirectValueActionItem, ConstantPool>> variables, List<GraphSourceItem> functions, HashMap<DirectValueActionItem, ConstantPool> strings, HashMap<DirectValueActionItem, String> usageTypes, ActionGraphSource code, int addr, String path) throws InterruptedException {
        ActionLocalData localData = new ActionLocalData(insideDoInitAction);
        getVariables(null, localData, new TranslateStack(path), new ArrayList<>(), code, code.adr2pos(addr), variables, functions, strings, new ArrayList<>(), usageTypes, path);
    }

    private List<MyEntry<DirectValueActionItem, ConstantPool>> getVariables(boolean insideDefineFunction1, List<MyEntry<DirectValueActionItem, ConstantPool>> variables, HashMap<ASMSource, ActionList> actionsMap, List<GraphSourceItem> functions, HashMap<DirectValueActionItem, ConstantPool> strings, HashMap<DirectValueActionItem, String> usageTypes, ASMSource src, String path) throws InterruptedException {
        List<MyEntry<DirectValueActionItem, ConstantPool>> ret = new ArrayList<>();
        ActionList actions = src.getActions();
        actionsMap.put(src, actions);
        boolean insideDoInitAction = src instanceof DoInitActionTag;
        getVariables(insideDoInitAction, variables, functions, strings, usageTypes, new ActionGraphSource(path, insideDoInitAction, actions, version, new HashMap<>(), new HashMap<>(), new HashMap<>()), 0, path);
        return ret;
    }

    private void getVariables(boolean insideDefineFunction1, Iterable<Tag> tags, String path, List<MyEntry<DirectValueActionItem, ConstantPool>> variables, HashMap<ASMSource, ActionList> actionsMap, List<GraphSourceItem> functions, HashMap<DirectValueActionItem, ConstantPool> strings, HashMap<DirectValueActionItem, String> usageTypes) throws InterruptedException {
        List<String> processed = new ArrayList<>();
        for (Tag t : tags) {
            String subPath = path + "/" + t.toString();
            if (t instanceof ASMSource) {
                addVariable(insideDefineFunction1, (ASMSource) t, subPath, processed, variables, actionsMap, functions, strings, usageTypes);
            }
            if (t instanceof ASMSourceContainer) {
                List<String> processed2 = new ArrayList<>();
                for (ASMSource asm : ((ASMSourceContainer) t).getSubItems()) {
                    addVariable(insideDefineFunction1, asm, subPath + "/" + asm.toString(), processed2, variables, actionsMap, functions, strings, usageTypes);
                }
            }
            if (t instanceof DefineSpriteTag) {
                getVariables(insideDefineFunction1, ((DefineSpriteTag) t).getTags(), path + "/" + t.toString(), variables, actionsMap, functions, strings, usageTypes);
            }
        }
    }

    private void addVariable(boolean insideDefineFunction1, ASMSource asm, String path, List<String> processed, List<MyEntry<DirectValueActionItem, ConstantPool>> variables, HashMap<ASMSource, ActionList> actionsMap, List<GraphSourceItem> functions, HashMap<DirectValueActionItem, ConstantPool> strings, HashMap<DirectValueActionItem, String> usageTypes) throws InterruptedException {
        int pos = 1;
        String infPath2 = path;
        while (processed.contains(infPath2)) {
            pos++;
            infPath2 = path + "[" + pos + "]";
        }
        processed.add(infPath2);
        informListeners("getVariables", infPath2);
        getVariables(insideDefineFunction1, variables, actionsMap, functions, strings, usageTypes, asm, path);
    }

    public boolean as3StringConstantExists(String str) {
        for (ABCContainerTag abcTag : getAbcList()) {
            ABC abc = abcTag.getABC();
            for (int i = 1; i < abc.constants.getStringCount(); i++) {
                if (abc.constants.getString(i).equals(str)) {
                    return true;
                }
            }
        }

        return false;
    }

    public void fixAS3Code() {
        for (ABCContainerTag abcTag : getAbcList()) {
            ABC abc = abcTag.getABC();
            for (MethodBody body : abc.bodies) {
                AVM2Code code = body.getCode();
                body.setCodeBytes(code.getBytes());
            }

            ((Tag) abcTag).setModified(true);
        }
    }

    public int deobfuscateAS3Identifiers(RenameType renameType) {
        for (Tag tag : getTags()) {
            if (tag instanceof ABCContainerTag) {
                ((ABCContainerTag) tag).getABC().deobfuscateIdentifiers(deobfuscated, renameType, true);
                tag.setModified(true);
            }
        }
        for (Tag tag : getTags()) {
            if (tag instanceof ABCContainerTag) {
                ((ABCContainerTag) tag).getABC().deobfuscateIdentifiers(deobfuscated, renameType, false);
                tag.setModified(true);
            }
        }
        for (Tag tag : getTags()) {
            if (tag instanceof SymbolClassTag) {
                SymbolClassTag sc = (SymbolClassTag) tag;
                for (int i = 0; i < sc.names.size(); i++) {
                    String newname = deobfuscation.deobfuscateNameWithPackage(true, sc.names.get(i), deobfuscated, renameType, deobfuscated);
                    if (newname != null) {
                        sc.names.set(i, newname);
                    }
                }
                sc.setModified(true);
            }
        }
        deobfuscation.deobfuscateInstanceNames(true, deobfuscated, renameType, getTags(), new HashMap<>());
        return deobfuscated.size();
    }

    public int deobfuscateIdentifiers(RenameType renameType) throws InterruptedException {
        FileAttributesTag fileAttributes = getFileAttributes();
        if (fileAttributes == null) {
            int cnt = 0;
            cnt += deobfuscateAS2Identifiers(renameType);
            cnt += deobfuscateAS3Identifiers(renameType);
            return cnt;
        } else if (fileAttributes.actionScript3) {
            return deobfuscateAS3Identifiers(renameType);
        } else {
            return deobfuscateAS2Identifiers(renameType);
        }
    }

    public void renameAS2Identifier(String identifier, String newname) throws InterruptedException {
        Map<DottedChain, DottedChain> selected = new HashMap<>();
        selected.put(DottedChain.parseWithSuffix(identifier), DottedChain.parseWithSuffix(newname));
        renameAS2Identifiers(null, selected);
    }

    private int deobfuscateAS2Identifiers(RenameType renameType) throws InterruptedException {
        return renameAS2Identifiers(renameType, null);
    }

    private int renameAS2Identifiers(RenameType renameType, Map<DottedChain, DottedChain> selected) throws InterruptedException {
        HashMap<ASMSource, ActionList> actionsMap = new HashMap<>();
        List<GraphSourceItem> allFunctions = new ArrayList<>();
        List<MyEntry<DirectValueActionItem, ConstantPool>> allVariableNames = new ArrayList<>();
        HashMap<DirectValueActionItem, ConstantPool> allStrings = new HashMap<>();
        HashMap<DirectValueActionItem, String> usageTypes = new HashMap<>();

        int ret = 0;
        getVariables(false, getTags(), "", allVariableNames, actionsMap, allFunctions, allStrings, usageTypes);
        informListeners("rename", "");
        int fc = 0;
        for (MyEntry<DirectValueActionItem, ConstantPool> it : allVariableNames) {
            String name = it.getKey().toStringNoH(it.getValue());
            deobfuscation.allVariableNamesStr.add(name);
        }

        informListeners("rename", "classes");
        int classCount = 0;
        for (Tag t : getTags()) {
            if (t instanceof DoInitActionTag) {
                classCount++;
            }
        }
        int cnt = 0;
        for (Tag t : getTags()) {
            if (t instanceof DoInitActionTag) {
                cnt++;
                informListeners("rename", "class " + cnt + "/" + classCount);
                DoInitActionTag dia = (DoInitActionTag) t;
                String exportName = getExportName(dia.spriteId);
                exportName = exportName != null ? exportName : "_unk_";
                String[] classNameParts = null;
                if (exportName.startsWith(AS2_PKG_PREFIX)) {
                    String className = exportName.substring(AS2_PKG_PREFIX.length());
                    if (className.contains(".")) {
                        classNameParts = className.split("\\.");
                    } else {
                        classNameParts = new String[]{className};
                    }
                }
                int staticOperation = Graph.SOP_USE_STATIC; //(Boolean) Configuration.getConfig("autoDeobfuscate", true) ? Graph.SOP_SKIP_STATIC : Graph.SOP_USE_STATIC;
                List<GraphTargetItem> dec;
                try {
                    dec = Action.actionsToTree(true /*Yes, inside doInitAction*/, dia.getActions(), version, staticOperation, ""/*FIXME*/);
                } catch (EmptyStackException ex) {
                    continue;
                }
                GraphTargetItem name = null;
                for (GraphTargetItem it : dec) {
                    if (it instanceof ClassActionItem) {
                        ClassActionItem cti = (ClassActionItem) it;
                        List<GraphTargetItem> methods = new ArrayList<>();
                        List<GraphTargetItem> vars = new ArrayList<>();

                        for (MyEntry<GraphTargetItem, GraphTargetItem> trait : cti.traits) {
                            if (trait.getValue() instanceof FunctionActionItem) {
                                methods.add(trait.getValue());
                            } else {
                                vars.add(trait.getValue());
                            }
                        }
                        for (GraphTargetItem gti : methods) {
                            if (gti instanceof FunctionActionItem) {
                                FunctionActionItem fun = (FunctionActionItem) gti;
                                if (fun.calculatedFunctionName instanceof DirectValueActionItem) {
                                    DirectValueActionItem dvf = (DirectValueActionItem) fun.calculatedFunctionName;
                                    String fname = dvf.toStringNoH(null);
                                    String changed = deobfuscation.deobfuscateName(false, fname, false, "method", deobfuscated, renameType, selected);
                                    if (changed != null) {
                                        deobfuscated.put(DottedChain.parseWithSuffix(fname), DottedChain.parseWithSuffix(changed));
                                    }
                                }
                            }
                        }

                        for (GraphTargetItem gti : vars) {
                            if (gti instanceof DirectValueActionItem) {
                                DirectValueActionItem dvf = (DirectValueActionItem) gti;
                                String vname = dvf.toStringNoH(null);
                                String changed = deobfuscation.deobfuscateName(false, vname, false, "attribute", deobfuscated, renameType, selected);
                                if (changed != null) {
                                    deobfuscated.put(DottedChain.parseWithSuffix(vname), DottedChain.parseWithSuffix(changed));
                                }
                            }
                        }

                        name = cti.className;
                        break;
                    }
                    if (it instanceof InterfaceActionItem) {
                        InterfaceActionItem ift = (InterfaceActionItem) it;
                        name = ift.name;
                    }
                }

                if (name != null) {
                    int pos = 0;
                    while (name instanceof GetMemberActionItem) {
                        GetMemberActionItem mem = (GetMemberActionItem) name;
                        GraphTargetItem memberName = mem.memberName;
                        if (memberName instanceof DirectValueActionItem) {
                            DirectValueActionItem dvt = (DirectValueActionItem) memberName;
                            String nameStr = dvt.toStringNoH(null);
                            if (classNameParts != null) {
                                if (classNameParts.length - 1 - pos < 0) {
                                    break;
                                }
                            }
                            String changedNameStr = nameStr;
                            if (classNameParts != null) {
                                changedNameStr = classNameParts[classNameParts.length - 1 - pos];
                            }
                            String changedNameStr2 = deobfuscation.deobfuscateName(false, changedNameStr, pos == 0, pos == 0 ? "class" : "package", deobfuscated, renameType, selected);
                            if (changedNameStr2 != null) {
                                changedNameStr = changedNameStr2;
                            }
                            ret++;
                            deobfuscated.put(DottedChain.parseWithSuffix(nameStr), DottedChain.parseWithSuffix(changedNameStr));
                            pos++;
                        }
                        name = mem.object;
                    }
                    if (name instanceof GetVariableActionItem) {
                        GetVariableActionItem var = (GetVariableActionItem) name;
                        if (var.name instanceof DirectValueActionItem) {
                            DirectValueActionItem dvt = (DirectValueActionItem) var.name;
                            String nameStr = dvt.toStringNoH(null);
                            if (classNameParts != null) {
                                if (classNameParts.length - 1 - pos < 0) {
                                    break;
                                }
                            }
                            String changedNameStr = nameStr;
                            if (classNameParts != null) {
                                changedNameStr = classNameParts[classNameParts.length - 1 - pos];
                            }
                            String changedNameStr2 = deobfuscation.deobfuscateName(false, changedNameStr, pos == 0, pos == 0 ? "class" : "package", deobfuscated, renameType, selected);
                            if (changedNameStr2 != null) {
                                changedNameStr = changedNameStr2;
                            }
                            ret++;
                            deobfuscated.put(DottedChain.parseWithSuffix(nameStr), DottedChain.parseWithSuffix(changedNameStr));
                            pos++;
                        }
                    }
                }
                t.setModified(true);
            }
        }

        for (GraphSourceItem fun : allFunctions) {
            fc++;
            informListeners("rename", "function " + fc + "/" + allFunctions.size());
            if (fun instanceof ActionDefineFunction) {
                ActionDefineFunction f = (ActionDefineFunction) fun;
                if (f.functionName.isEmpty()) { // anonymous function, leave as is
                    continue;
                }
                String changed = deobfuscation.deobfuscateName(false, f.functionName, false, "function", deobfuscated, renameType, selected);
                if (changed != null) {
                    f.replacedFunctionName = changed;
                    ret++;
                }
            }
            if (fun instanceof ActionDefineFunction2) {
                ActionDefineFunction2 f = (ActionDefineFunction2) fun;
                if (f.functionName.isEmpty()) { // anonymous function, leave as is
                    continue;
                }
                String changed = deobfuscation.deobfuscateName(false, f.functionName, false, "function", deobfuscated, renameType, selected);
                if (changed != null) {
                    f.replacedFunctionName = changed;
                    ret++;
                }
            }
        }

        HashSet<String> stringsNoVarH = new HashSet<>();
        List<DirectValueActionItem> allVariableNamesDv = new ArrayList<>();
        for (MyEntry<DirectValueActionItem, ConstantPool> it : allVariableNames) {
            allVariableNamesDv.add(it.getKey());
        }
        for (DirectValueActionItem ti : allStrings.keySet()) {
            if (!allVariableNamesDv.contains(ti)) {
                stringsNoVarH.add(System.identityHashCode(allStrings.get(ti)) + "_" + ti.toStringNoH(allStrings.get(ti)));
            }
        }

        int vc = 0;
        for (MyEntry<DirectValueActionItem, ConstantPool> it : allVariableNames) {
            vc++;
            String name = it.getKey().toStringNoH(it.getValue());
            String changed = deobfuscation.deobfuscateName(false, name, false, usageTypes.get(it.getKey()), deobfuscated, renameType, selected);
            if (changed != null) {
                boolean addNew = false;
                String h = System.identityHashCode(it.getKey()) + "_" + name;
                if (stringsNoVarH.contains(h)) {
                    addNew = true;
                }
                ActionPush pu = (ActionPush) it.getKey().getSrc();
                if (pu.replacement == null) {
                    pu.replacement = new ArrayList<>();
                    pu.replacement.addAll(pu.values);
                }
                if (pu.replacement.get(it.getKey().pos) instanceof ConstantIndex) {
                    ConstantIndex ci = (ConstantIndex) pu.replacement.get(it.getKey().pos);
                    ConstantPool pool = it.getValue();
                    if (pool == null) {
                        continue;
                    }
                    if (pool.constants == null) {
                        continue;
                    }
                    if (addNew) {
                        pool.constants.add(changed);
                        ci.index = pool.constants.size() - 1;
                    } else {
                        pool.constants.set(ci.index, changed);
                    }
                } else {
                    pu.replacement.set(it.getKey().pos, changed);
                }
                ret++;
            }
        }

        for (ASMSource src : actionsMap.keySet()) {
            actionsMap.get(src).removeNops();
            src.setActions(actionsMap.get(src));
            src.setModified();
        }

        deobfuscation.deobfuscateInstanceNames(false, deobfuscated, renameType, getTags(), selected);
        return ret;
    }

    public IdentifiersDeobfuscation getDeobfuscation() {
        return deobfuscation;
    }

    public void exportFla(AbortRetryIgnoreHandler handler, String outfile, String swfName, String generator, String generatorVerName, String generatorVersion, boolean parallel, FLAVersion version) throws IOException, InterruptedException {
        XFLExportSettings settings = new XFLExportSettings();
        settings.compressed = true;
        exportXfl(handler, outfile, swfName, generator, generatorVerName, generatorVersion, parallel, version, settings);
    }

    public void exportXfl(AbortRetryIgnoreHandler handler, String outfile, String swfName, String generator, String generatorVerName, String generatorVersion, boolean parallel, FLAVersion version) throws IOException, InterruptedException {
        XFLExportSettings settings = new XFLExportSettings();
        settings.compressed = false;
        exportXfl(handler, outfile, swfName, generator, generatorVerName, generatorVersion, parallel, version, settings);
    }

    public void exportXfl(AbortRetryIgnoreHandler handler, String outfile, String swfName, String generator, String generatorVerName, String generatorVersion, boolean parallel, FLAVersion version, XFLExportSettings settings) throws IOException, InterruptedException {
        new XFLConverter().convertSWF(handler, this, swfName, outfile, settings, generator, generatorVerName, generatorVersion, parallel, version);
        clearAllCache();
    }

    public static AffineTransform matrixToTransform(MATRIX mat) {
        return new AffineTransform(mat.getScaleXFloat(), mat.getRotateSkew0Float(),
                mat.getRotateSkew1Float(), mat.getScaleYFloat(),
                mat.translateX, mat.translateY);
    }

    public SerializableImage getFromCache(String key) {
        if (frameCache.contains(key)) {
            return frameCache.get(key);
        }
        return null;
    }

    public byte[] getFromCache(SoundTag soundTag) {
        if (soundCache.contains(soundTag)) {
            return soundCache.get(soundTag);
        }
        return null;
    }

    public void putToCache(String key, SerializableImage img) {
        if (Configuration.useFrameCache.get()) {
            frameCache.put(key, img);
        }
    }

    public void putToCache(SoundTag soundTag, byte[] data) {
        soundCache.put(soundTag, data);
    }

    public void clearImageCache() {
        jtt = null;
        frameCache.clear();
        rectCache.clear();
        for (Tag tag : getTags()) {
            if (tag instanceof ImageTag) {
                ((ImageTag) tag).clearCache();
            } else if (tag instanceof DefineCompactedFont) {
                ((DefineCompactedFont) tag).rebuildShapeCache();
            }
        }
    }

    public void clearSoundCache() {
        soundCache.clear();
    }

    public void clearScriptCache() {
        as2Cache.clear();
        as3Cache.clear();
        if (abcList != null) {
            for (ABCContainerTag c : abcList) {
                c.getABC().clearPacksCache();
            }
        }
        asmsCache = null;
        asmsCacheExportFilenames = null;
        IdentifiersDeobfuscation.clearCache();
    }

    public void clearReadOnlyListCache() {
        readOnlyTags = null;
        for (Tag tag : tags) {
            if (tag instanceof DefineSpriteTag) {
                ((DefineSpriteTag) tag).clearReadOnlyListCache();
            }
        }
    }

    public static void clearAllStaticCache() {
        Cache.clearAll();
        Helper.clearShapeCache();
        System.gc();
    }

    public void clearAbcListCache() {
        abcList = null;
    }

    public void clearAllCache() {
        characters = null;
        characterIdTags = null;
        clearAbcListCache();
        timeline = null;
        clearReadOnlyListCache();
        clearImageCache();
        clearScriptCache();
        clearAllStaticCache();
    }

    public static void uncache(ASMSource src) {
        if (src != null) {
            SWF swf = src.getSwf();
            if (swf != null) {
                swf.as2Cache.remove(src);
            }
        }
    }

    public static void uncache(ScriptPack pack) {
        if (pack != null) {
            SWF swf = pack.getSwf();
            if (swf != null) {
                swf.as3Cache.remove(pack);
            }
        }
    }

    public static boolean isCached(ASMSource src) {
        if (src != null) {
            SWF swf = src.getSwf();
            if (swf != null) {
                return swf.as2Cache.isCached(src);
            }
        }

        return false;
    }

    public static boolean isActionListCached(ASMSource src) {
        if (src != null) {
            SWF swf = src.getSwf();
            if (swf != null) {
                return swf.as2Cache.isPCodeCached(src);
            }
        }

        return false;
    }

    public static boolean isCached(ScriptPack pack) {
        if (pack != null) {
            SWF swf = pack.getSwf();
            if (swf != null) {
                return swf.as3Cache.isCached(pack);
            }
        }

        return false;
    }

    public static HighlightedText getFromCache(ASMSource src) {
        if (src != null) {
            SWF swf = src.getSwf();
            if (swf != null) {
                return swf.as2Cache.get(src);
            }
        }

        return null;
    }

    public static ActionList getActionListFromCache(ASMSource src) {
        if (src != null) {
            SWF swf = src.getSwf();
            if (swf != null) {
                return swf.as2Cache.getPCode(src);
            }
        }

        return null;
    }

    public static HighlightedText getFromCache(ScriptPack pack) {
        if (pack != null) {
            SWF swf = pack.getSwf();
            if (swf != null) {
                return swf.as3Cache.get(pack);
            }
        }

        return null;
    }

    public static ActionList getCachedActionList(ASMSource src, final List<DisassemblyListener> listeners) throws InterruptedException {
        synchronized (src) {
            SWF swf = src.getSwf();
            int deobfuscationMode = Configuration.autoDeobfuscate.get() ? 1 : 0;
            if (swf != null && swf.as2Cache.isPCodeCached(src)) {
                ActionList result = swf.as2Cache.getPCode(src);
                if (result.deobfuscationMode == deobfuscationMode) {
                    return result;
                }
            }

            try {
                ByteArrayRange actionBytes = src.getActionBytes();
                int prevLength = actionBytes.getPos();
                SWFInputStream rri = new SWFInputStream(swf, actionBytes.getArray());
                if (prevLength != 0) {
                    rri.seek(prevLength);
                }

                int version = swf == null ? SWF.DEFAULT_VERSION : swf.version;
                ActionList list = ActionListReader.readActionListTimeout(listeners, rri, version, prevLength, prevLength + actionBytes.getLength(), src.toString()/*FIXME?*/, deobfuscationMode);
                list.fileData = actionBytes.getArray();
                list.deobfuscationMode = deobfuscationMode;
                if (swf != null) {
                    swf.as2Cache.put(src, list);
                }

                return list;
            } catch (InterruptedException ex) {
                throw ex;
            } catch (Exception ex) {
                logger.log(Level.SEVERE, null, ex);
                return new ActionList();
            }
        }
    }

    public static HighlightedText getCached(ASMSource src, ActionList actions) throws InterruptedException {
        SWF swf = src.getSwf();
        HighlightedText res;
        if (swf != null) {
            res = swf.as2Cache.get(src);
            if (res != null) {
                return res;
            }
        }

        return decompilerPool.decompile(src, actions);
    }

    public static HighlightedText getCached(ScriptPack pack) throws InterruptedException {
        SWF swf = pack.getSwf();
        HighlightedText res;
        if (swf != null) {
            res = swf.as3Cache.get(pack);
            if (res != null) {
                return res;
            }
        }

        return decompilerPool.decompile(pack);
    }

    public static Future<HighlightedText> getCachedFuture(ASMSource src, ActionList actions, ScriptDecompiledListener<HighlightedText> listener) throws InterruptedException {
        SWF swf = src.getSwf();
        HighlightedText res;
        if (swf != null) {
            res = swf.as2Cache.get(src);
            if (res != null) {
                if (listener != null) {
                    listener.onComplete(res);
                }

                return new ImmediateFuture<>(res);
            }
        }

        return decompilerPool.submitTask(src, actions, listener);
    }

    public static Future<HighlightedText> getCachedFuture(ScriptPack pack, ScriptDecompiledListener<HighlightedText> listener) throws InterruptedException {
        SWF swf = pack.getSwf();
        HighlightedText res;
        if (swf != null) {
            res = swf.as3Cache.get(pack);
            if (res != null) {
                if (listener != null) {
                    listener.onComplete(res);
                }

                return new ImmediateFuture<>(res);
            }
        }

        return decompilerPool.submitTask(pack, listener);
    }

    public DecompilerPool getDecompilerPool() {
        return decompilerPool;
    }

    public Cache<CharacterTag, RECT> getRectCache() {
        return rectCache;
    }

    public Cache<SHAPE, ShapeExportData> getShapeExportDataCache() {
        return shapeExportDataCache;
    }

    public static RECT fixRect(RECT rect) {
        RECT ret = new RECT();
        ret.Xmin = rect.Xmin;
        ret.Xmax = rect.Xmax;
        ret.Ymin = rect.Ymin;
        ret.Ymax = rect.Ymax;

        if (ret.Xmax <= 0) {
            ret.Xmax = ret.getWidth();
            ret.Xmin = 0;
        }
        if (ret.Ymax <= 0) {
            ret.Ymax = ret.getHeight();
            ret.Ymin = 0;
        }
        if (ret.Xmin < 0) {
            ret.Xmax += (-ret.Xmin);
            ret.Xmin = 0;
        }
        if (ret.Ymin < 0) {
            ret.Ymax += (-ret.Ymin);
            ret.Ymin = 0;
        }

        if (ret.getWidth() < 1 || ret.getHeight() < 1) {
            ret.Xmin = 0;
            ret.Ymin = 0;
            ret.Xmax = 20;
            ret.Ymax = 20;
        }
        return ret;
    }

    public static SerializableImage frameToImageGet(Timeline timeline, int frame, int time, Point cursorPosition, int mouseButton, RECT displayRect, Matrix transformation, ColorTransform colorTransform, Color backGroundColor, double zoom) {
        if (timeline.getFrameCount() == 0) {
            return new SerializableImage(1, 1, SerializableImage.TYPE_INT_ARGB_PRE);
        }

        RECT rect = displayRect;
        SerializableImage image = new SerializableImage((int) (rect.getWidth() * zoom / SWF.unitDivisor) + 1,
                (int) (rect.getHeight() * zoom / SWF.unitDivisor) + 1, SerializableImage.TYPE_INT_ARGB_PRE);
        if (backGroundColor == null) {
            image.fillTransparent();
        } else {
            Graphics2D g = (Graphics2D) image.getBufferedImage().getGraphics();
            g.setComposite(AlphaComposite.Src);
            g.setColor(backGroundColor);
            g.fill(new Rectangle(image.getWidth(), image.getHeight()));
        }

        Matrix m = transformation.clone();
        m.translate(-rect.Xmin * zoom, -rect.Ymin * zoom);
        m.scale(zoom);
        RenderContext renderContext = new RenderContext();
        renderContext.cursorPosition = cursorPosition;
        renderContext.mouseButton = mouseButton;
        timeline.toImage(frame, time, renderContext, image, false, m, transformation, m, colorTransform);

        return image;
    }

    private void removeTagWithDependenciesFromTimeline(Tag toRemove, Timeline timeline) {
        Map<Integer, Integer> stage = new HashMap<>();
        Set<Integer> dependingChars = new HashSet<>();
        Timelined timelined = timeline.timelined;
        ReadOnlyTagList tags = timelined.getTags();
        if (toRemove instanceof CharacterTag) {
            int characterId = ((CharacterTag) toRemove).getCharacterId();
            dependingChars = getDependentCharacters(characterId);
            dependingChars.add(characterId);
        }

        for (int i = 0; i < tags.size(); i++) {
            Tag t = tags.get(i);
            if (t instanceof RemoveTag) {
                RemoveTag rt = (RemoveTag) t;
                int depth = rt.getDepth();
                if (stage.containsKey(depth)) {
                    int currentCharId = stage.get(depth);
                    stage.remove(depth);
                    if (dependingChars.contains(currentCharId)) {
                        timelined.removeTag(i);
                        i--;
                        continue;
                    }
                }
            }

            if (t instanceof PlaceObjectTypeTag) {
                PlaceObjectTypeTag po = (PlaceObjectTypeTag) t;
                int placeCharId = po.getCharacterId();
                int depth = po.getDepth();
                if (placeCharId >= 0) {
                    stage.put(depth, placeCharId);
                } else if (stage.containsKey(depth)) {
                    placeCharId = stage.get(depth);
                }

                if (placeCharId >= 0 && dependingChars.contains(placeCharId)) {
                    timelined.removeTag(i);
                    i--;
                    continue;
                }
            }

            if (t instanceof CharacterIdTag) {
                CharacterIdTag c = (CharacterIdTag) t;
                if (dependingChars.contains(c.getCharacterId())) {
                    timelined.removeTag(i);
                    i--;
                    continue;
                }
            }

            if (t == toRemove) {
                timelined.removeTag(i);
                i--;
                continue;
            }

            if (t instanceof Timelined) {
                removeTagWithDependenciesFromTimeline(toRemove, ((Timelined) t).getTimeline());
            }
        }
    }

    private boolean removeTagFromTimeline(Tag toRemove, Timeline timeline) {
        boolean modified = false;
        int characterId = -1;
        if (toRemove instanceof CharacterTag) {
            characterId = ((CharacterTag) toRemove).getCharacterId();
            modified = timeline.removeCharacter(characterId);
        }
        Timelined timelined = timeline.timelined;
        ReadOnlyTagList tags = timelined.getTags();
        for (int i = 0; i < tags.size(); i++) {
            Tag t = tags.get(i);
            if (t == toRemove) {
                timelined.removeTag(t);
                i--;
                continue;
            }

            if (toRemove instanceof CharacterTag) {
                if (t.removeCharacter(characterId)) {
                    modified = true;
                    i = -1;
                    continue;
                }
            }

            if (t instanceof DefineSpriteTag) {
                DefineSpriteTag spr = (DefineSpriteTag) t;
                boolean sprModified = removeTagFromTimeline(toRemove, spr.getTimeline());
                if (sprModified) {
                    spr.setModified(true);
                }
                modified |= sprModified;
            }
        }
        return modified;
    }

    public void removeTags(Collection<Tag> tags, boolean removeDependencies) {
        Set<Timelined> timelineds = new HashSet<>();
        for (Tag tag : tags) {
            Timelined timelined = tag.getTimelined();
            timelineds.add(timelined);
            removeTagInternal(timelined, tag, removeDependencies);
        }

        for (Timelined timelined : timelineds) {
            resetTimelines(timelined);
        }

        updateCharacters();
        clearImageCache();
    }

    @Override
    public void removeTag(int index) {
        setModified(true);
        tags.remove(index);
        updateCharacters();
    }

    @Override
    public void removeTag(Tag tag) {
        setModified(true);
        tags.remove(tag);
        updateCharacters();
    }

    public void removeTag(Tag tag, boolean removeDependencies) {
        Timelined timelined = tag.getTimelined();
        removeTagInternal(timelined, tag, removeDependencies);
        resetTimelines(timelined);
        updateCharacters();
        clearImageCache();
    }

    private void removeTagInternal(Timelined timelined, Tag tag, boolean removeDependencies) {
        if ((tag instanceof DoABC2Tag) || (tag instanceof DoABCTag)) {
            clearAbcListCache();
        }
        if (tag instanceof ShowFrameTag || ShowFrameTag.isNestedTagType(tag.getId())) {
            timelined.removeTag(tag);
            timelined.setModified(true);
            timelined.resetTimeline();
        } else // timeline should be always the swf here
        if (removeDependencies) {
            removeTagWithDependenciesFromTimeline(tag, timelined.getTimeline());
            timelined.setModified(true);
        } else {
            boolean modified = removeTagFromTimeline(tag, timelined.getTimeline());
            if (modified) {
                timelined.setModified(true);
            }
        }
    }

    @Override
    public ReadOnlyTagList getTags() {
        if (readOnlyTags == null) {
            readOnlyTags = new ReadOnlyTagList(tags);
        }

        return readOnlyTags;
    }

    public ReadOnlyTagList getLocalTags() {
        List<Tag> localTags = new ArrayList<>();
        for (Tag t : tags) {
            if (!t.isImported()) {
                localTags.add(t);
            }
        }
        return new ReadOnlyTagList(localTags);
    }

    /**
     * Adds a tag to the SWF
     *
     * @param tag
     */
    @Override
    public void addTag(Tag tag) {
        setModified(true);
        tags.add(tag);
        updateCharacters();
    }

    /**
     * Adds a tag to the SWF
     *
     * @param index
     * @param tag
     */
    @Override
    public void addTag(int index, Tag tag) {
        setModified(true);
        tags.add(index, tag);
        updateCharacters();
    }

    /**
     * Replaces a tag in the SWF
     *
     * @param oldTag
     * @param newTag
     */
    public void replaceTag(Tag oldTag, Tag newTag) {
        setModified(true);
        int index = tags.indexOf(oldTag);
        if (index != -1) {
            tags.set(index, newTag);
            updateCharacters();
        }
    }

    /**
     * Adds a tag to the SWF If targetTreeItem is: - Frame: adds the tag to the
     * Frame. Frame can be a frame of the main timeline or a DefineSprite frame
     * - DefineSprite: adds the tag to the end of the DefineSprite's tag list -
     * Any other tag in the SWF: adds the new tag exactly before the specified
     * tag - Other: adds the tag to the end of the SWF's tag list
     *
     * @param tag
     * @param targetTreeItem
     */
    public void addTag(Tag tag, TreeItem targetTreeItem) {
        SWF swf = tag.getSwf();
        Frame frame = targetTreeItem instanceof Frame ? (Frame) targetTreeItem : null;
        Timelined timelined;
        if (frame != null) {
            timelined = frame.timeline.timelined;
        } else {
            timelined = swf.getTimelined(targetTreeItem);
        }

        tag.setTimelined(timelined);

        ReadOnlyTagList tags = timelined.getTags();

        int index;
        if (frame != null) {
            if (frame.showFrameTag != null) {
                index = tags.indexOf(frame.showFrameTag);
            } else {
                index = -1;
            }
        } else if (timelined instanceof DefineSpriteTag) {
            index = -1;
        } else if (targetTreeItem instanceof Tag) {
            if (tag instanceof CharacterIdTag && !(tag instanceof CharacterTag) && targetTreeItem instanceof CharacterTag) {
                ((CharacterIdTag) tag).setCharacterId(((CharacterTag) targetTreeItem).getCharacterId());
            }

            index = tags.indexOf((Tag) targetTreeItem); // todo: honfika: why not index + 1?
        } else {
            index = -1;
            if (tag instanceof CharacterTag) {
                // add before the last ShowFrame tag
                for (int i = tags.size() - 1; i >= 0; i--) {
                    if (tags.get(i) instanceof ShowFrameTag) {
                        index = i;
                        break;
                    }
                }
            }
        }

        if (index > -1) {
            timelined.addTag(index, tag);
        } else {
            timelined.addTag(tag);
        }

        timelined.resetTimeline();

        if (timelined instanceof DefineSpriteTag) {
            DefineSpriteTag sprite = (DefineSpriteTag) timelined;
            sprite.frameCount = timelined.getTimeline().getFrameCount();
        }
        if (timelined == this) {
            frameCount = getTimeline().getFrameCount();
        }
    }

    public Timelined getTimelined(TreeItem treeItem) {
        if (treeItem instanceof Frame) {
            return ((Frame) treeItem).timeline.timelined;
        }

        if (treeItem instanceof DefineSpriteTag) {
            return (DefineSpriteTag) treeItem;
        }

        return treeItem.getSwf();
    }

    public void packCharacterIds() {
        int maxId = getNextCharacterId();
        int id = 1;
        for (int i = 1; i < maxId; i++) {
            CharacterTag charactertag = getCharacter(i);
            if (charactertag != null) {
                if (i != id) {
                    replaceCharacter(i, id);
                }
                id++;
            } else {
                // make sure that the id is not referenced in the tags
                replaceCharacter(i, 0);
            }
        }
    }

    public void sortCharacterIds() {
        int maxId = Math.max(tags.size(), getNextCharacterId());
        int id = maxId;
        // first set the chatacter ids to surely not used ids
        for (Tag tag : getTags()) {
            if (tag instanceof CharacterTag) {
                CharacterTag characterTag = (CharacterTag) tag;
                replaceCharacter(characterTag.getCharacterId(), id++);
            }
        }
        // then set them to 1,2,3...
        id = 1;
        for (Tag tag : getTags()) {
            if (tag instanceof CharacterTag) {
                CharacterTag characterTag = (CharacterTag) tag;
                replaceCharacter(characterTag.getCharacterId(), id++);
            }
        }
    }

    public boolean replaceCharacter(int oldCharacterId, int newCharacterId) {
        boolean modified = false;
        for (Tag tag : getTags()) {
            boolean modified2 = false;
            if (tag instanceof CharacterIdTag) {
                CharacterIdTag characterIdTag = (CharacterIdTag) tag;
                if (characterIdTag.getCharacterId() == oldCharacterId) {
                    characterIdTag.setCharacterId(newCharacterId);
                    modified2 = true;
                }
            }
            modified2 |= tag.replaceCharacter(oldCharacterId, newCharacterId);
            if (modified2) {
                tag.setModified(true);
            }
            modified |= modified2;
        }
        return modified;
    }

    public void replaceCharacterTags(CharacterTag characterTag, int newCharacterId) {
        int characterId = characterTag.getCharacterId();
        CharacterTag newCharacter = getCharacter(newCharacterId);
        newCharacter.setCharacterId(characterId);
        characterTag.setCharacterId(newCharacterId);
        newCharacter.setModified(true);
        characterTag.setModified(true);

        assignExportNamesToSymbols();
        assignClassesToSymbols();
        clearImageCache();
        updateCharacters();
    }

    @Override
    public String toString() {
        return getShortFileName();
    }

    public void deobfuscate(DeobfuscationLevel level) throws InterruptedException {
        List<ABCContainerTag> atags = getAbcList();

        for (ABCContainerTag tag : atags) {
            if (level == DeobfuscationLevel.LEVEL_REMOVE_DEAD_CODE) {
                tag.getABC().removeDeadCode();
            } else if (level == DeobfuscationLevel.LEVEL_REMOVE_TRAPS) {
                tag.getABC().removeTraps();
            } else if (level == DeobfuscationLevel.LEVEL_RESTORE_CONTROL_FLOW) {
                tag.getABC().removeTraps();
            }

            ((Tag) tag).setModified(true);
        }
    }

    /**
     * Enables debugging. Adds tags to enable debugging and optinally injects
     * debugline and debugfile instructions to AS3 code by decompiling it first
     *
     * @param injectAS3Code Modify AS3 code with debugfile / debugline ?
     * @param decompileDir Directory to virtual decompile (will affect
     * debugfile)
     */
    public void enableDebugging(boolean injectAS3Code, File decompileDir) {
        enableDebugging(injectAS3Code, decompileDir, false);
    }

    /**
     * Enables debugging. Adds tags to enable debugging.
     */
    public void enableDebugging() {
        enableDebugging(false, null, false);
    }

    /**
     * Enables debugging. Adds tags to enable debugging and injects debugline
     * and debugfile instructions to AS3 code. Optionally enables Telemetry
     *
     * @param injectAS3Code Modify AS3 code with debugfile / debugline ?
     * @param decompileDir Directory to virtual decompile (will affect
     * debugfile)
     * @param telemetry Enable telemetry info?
     */
    public void enableDebugging(boolean injectAS3Code, File decompileDir, boolean telemetry) {
        enableDebugging(injectAS3Code, decompileDir, telemetry, false);
    }

    /**
     * Injects debugline and debugfile instructions to AS3 P-code (lines of
     * P-code)
     */
    public void injectAS3PcodeDebugInfo() {
        List<ScriptPack> packs = getAS3Packs();
        for (ScriptPack s : packs) {
            int abcIndex = s.allABCs.indexOf(s.abc);
            if (s.isSimple) {
                s.injectPCodeDebugInfo(abcIndex);
            }
        }
    }

    /**
     * Injects debugline and debugfile instructions to AS3 code
     *
     * @param decompileDir Directory to set file information paths
     */
    public void injectAS3DebugInfo(File decompileDir) {
        List<ScriptPack> packs = getAS3Packs();
        for (ScriptPack s : packs) {
            if (s.isSimple) {
                s.injectDebugInfo(decompileDir);
            }
        }
    }

    /**
     * Enables debugging. Adds tags to enable debugging and injects debugline
     * and debugfile instructions to AS3 code. Optionally enables Telemetry
     *
     * @param injectAS3Code Modify AS3 code with debugfile / debugline ?
     * @param decompileDir Directory to virtual decompile (will affect
     * debugfile)
     * @param telemetry Enable telemetry info?
     * @param pcodeLevel inject Pcode lines instead of decompiled lines
     */
    public void enableDebugging(boolean injectAS3Code, File decompileDir, boolean telemetry, boolean pcodeLevel) {

        if (injectAS3Code) {
            if (pcodeLevel) {
                injectAS3PcodeDebugInfo();
            } else {
                injectAS3DebugInfo(decompileDir);
            }
        }

        int pos = 0;

        boolean hasEnabled = false;

        for (int i = 0; i < tags.size(); i++) {
            Tag t = tags.get(i);
            if (t instanceof MetadataTag) {
                pos = i + 1;
            }
            if (t instanceof FileAttributesTag) {
                pos = i + 1;
            }
            if (version >= 6 && (t instanceof EnableDebugger2Tag)) {
                hasEnabled = true;
                break;
            }
            if (version == 5 && (t instanceof EnableDebuggerTag)) {
                hasEnabled = true;
                break;
            }
            if (version < 5 && (t instanceof ProtectTag)) {
                hasEnabled = true;
                break;
            }
        }

        if (!hasEnabled) {
            if (version >= 6) {
                tags.add(pos, new EnableDebugger2Tag(this));
            } else if (version == 5) {
                tags.add(pos, new EnableDebuggerTag(this));
            } else {
                tags.add(pos, new ProtectTag(this));
            }
        }

        getOrAddDebugId();
    }

    /**
     * Finds DebugID tag
     *
     * @return the tag or null if not found
     */
    public DebugIDTag getDebugId() {
        for (Tag t : getTags()) {
            if (t instanceof DebugIDTag) {
                return (DebugIDTag) t;
            }
        }
        return null;
    }

    /**
     * Finds DebugID tag and generates new one if none exists
     *
     * @return the tag or null if there is not debugging enabled in the swf file
     */
    public DebugIDTag getOrAddDebugId() {
        DebugIDTag r = getDebugId();
        if (r == null) {
            for (int i = 0; i < tags.size(); i++) {
                Tag t = tags.get(i);
                if ((t instanceof EnableDebuggerTag) || (t instanceof EnableDebugger2Tag)) {
                    r = new DebugIDTag(this);
                    tags.add(i + 1, r);
                    new Random().nextBytes(r.debugId);
                    break;
                }
            }
        }
        return r;
    }

    public boolean generatePCodeSwdFile(File file, Map<String, Set<Integer>> breakpoints) throws IOException {
        DebugIDTag dit = getDebugId();
        if (dit == null) {
            return false;
        }
        List<SWD.DebugItem> items = new ArrayList<>();
        Map<String, ASMSource> asms = getASMs(true);

        try {
            items.add(new SWD.DebugId(dit.debugId));

        } catch (Throwable t) {
            logger.log(Level.SEVERE, "message", t);
            return false;
        }

        int moduleId = 0;
        List<String> names = new ArrayList<>(asms.keySet());
        Collections.sort(names);
        for (String name : names) {
            moduleId++;
            String sname = "#PCODE " + name;
            int bitmap = SWD.bitmapAction;
            items.add(new SWD.DebugScript(moduleId, bitmap, sname, ""));

            HighlightedTextWriter writer = new HighlightedTextWriter(Configuration.getCodeFormatting(), true);
            try {
                asms.get(name).getASMSource(ScriptExportMode.PCODE, writer, null);
            } catch (InterruptedException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
            List<Highlighting> hls = writer.instructionHilights;

            Map<Integer, Integer> offsetToLine = new TreeMap<>();
            String txt = writer.toString();
            txt = txt.replace("\r", "");
            int line = 1;
            for (int i = 0; i < txt.length(); i++) {
                Highlighting h = Highlighting.searchPos(hls, i);
                if (h != null) {
                    int of = (int) h.getProperties().fileOffset;
                    if (of > -1 && !offsetToLine.containsKey(of) && !offsetToLine.containsValue(line)) {
                        offsetToLine.put(of, line);
                    }
                }
                if (txt.charAt(i) == '\n') {
                    line++;
                }
            }

            for (int ofs : offsetToLine.keySet()) {
                items.add(new SWD.DebugOffset(moduleId, offsetToLine.get(ofs), ofs));
            }

            if (breakpoints.containsKey(sname)) {
                Set<Integer> bplines = breakpoints.get(sname);
                for (int bpline : bplines) {
                    if (offsetToLine.containsValue(bpline)) {
                        try {
                            SWD.DebugBreakpoint dbp = new SWD.DebugBreakpoint(moduleId, bpline);
                            items.add(dbp);
                        } catch (IllegalArgumentException iex) {
                            logger.log(Level.WARNING, "Cannot generate breakpoint to SWD: {0}", iex.getMessage());
                        }
                    }
                }
            }
        }

        SWD swd = new SWD(7, items);
        try (FileOutputStream fis = new FileOutputStream(file)) {
            swd.saveTo(fis);
        }
        return true;
    }

    public boolean generateSwdFile(File file, Map<String, Set<Integer>> breakpoints) throws IOException {
        DebugIDTag dit = getDebugId();
        if (dit == null) {
            return false;
        }
        List<SWD.DebugItem> items = new ArrayList<>();
        Map<String, ASMSource> asms = getASMs(true);

        try {
            items.add(new SWD.DebugId(dit.debugId));

            int moduleId = 0;
            List<String> names = new ArrayList<>(asms.keySet());
            Collections.sort(names);
            for (String name : names) {
                List<SWD.DebugRegisters> regitems = new ArrayList<>();
                moduleId++;
                HighlightedText cs;
                try {
                    cs = SWF.getCached(asms.get(name), null);
                } catch (InterruptedException ex) {
                    return false;
                }
                String txt = cs.text.replace("\r", "");
                int line = 1;
                Map<Integer, Integer> lineToOffset = new HashMap<>();
                Map<Integer, String> regNames = new HashMap<>();

                for (int pos = 0; pos < txt.length(); pos++) {
                    Highlighting h = Highlighting.searchPos(cs.getInstructionHighlights(), pos);
                    if (h != null) {

                        int firstLineOffset = (int) h.getProperties().firstLineOffset;
                        if (firstLineOffset > -1 && h.getProperties().declaration && h.getProperties().regIndex > -1 && (!regNames.containsKey(h.getProperties().regIndex) || !regNames.get(h.getProperties().regIndex).equals(h.getProperties().localName))) {
                            regNames.put(h.getProperties().regIndex, h.getProperties().localName);

                            List<Integer> curRegIndexes = new ArrayList<>(regNames.keySet());
                            List<String> curRegNames = new ArrayList<>();
                            for (int i = 0; i < curRegIndexes.size(); i++) {
                                curRegNames.add(regNames.get(curRegIndexes.get(i)));
                            }
                            regitems.add(new SWD.DebugRegisters((int) h.getProperties().firstLineOffset, curRegIndexes, curRegNames));
                        }

                        if (firstLineOffset != -1 && !lineToOffset.containsKey(line)) {
                            lineToOffset.put(line, firstLineOffset);
                        }
                    }
                    if (txt.charAt(pos) == '\n') {
                        line++;
                    }
                }

                Map<Integer, Integer> offSetToLine = new TreeMap<>();
                for (Map.Entry<Integer, Integer> en : lineToOffset.entrySet()) {
                    offSetToLine.put(en.getValue(), en.getKey());
                }

                //final String NONAME = "[No instance name assigned]";
                String sname = name;
                int bitmap = SWD.bitmapAction;
                /* Matcher m;
                 int bitmap = SWD.bitmapAction;
                 m = Pattern.compile("^\\\\frame_([0-9]+)\\\\DoAction$").matcher(sname);
                 if (m.matches()) {
                 //TODO: scenes?, layers?
                 sname = "Actions for Scene 1: Frame " + m.group(1) + " of Layer Name Layer 1";
                 } else if ((m = Pattern.compile("^\\\\__Packages\\\\(.*)$").matcher(sname)).matches()) {
                 sname = m.group(1).replace("\\", ".") + ": .\\" + m.group(1) + ".as";
                 } else {
                 continue; //FIXME!
                 }
                 m = Pattern.compile("^\\\\DefineSprite_([0-9])+\\\\frame_([0-9]+)\\\\DoAction$").matcher(sname);
                 if (m.matches()) {
                 //TODO: layers?
                 //sname = "Actions for Symbol " + m.group(1) + ": Frame " + m.group(2) + " of Layer Name Layer 1";
                 continue; //FIXME!
                 }

                 //TODO: handle onxxx together ?
                 m = Pattern.compile("^\\\\DefineButton2?_([0-9]+)\\\\on\\(.*$").matcher(sname);
                 if (m.matches()) {
                 //bitmap = SWD.bitmapOnAction;
                 //sname = "Actions for " + NONAME + " (Symbol " + m.group(1) + ")";
                 continue; //FIXME!
                 }

                 //TODO: handle onClipEvent together ?
                 m = Pattern.compile("^\\\\frame_([0-9]+)\\\\PlaceObject[2-3]?_([0-9]+)_[^\\\\]*\\\\onClipEvent\\(.*$").matcher(sname);
                 if (m.matches()) {
                 //bitmap = SWD.bitmapOnClipAction;
                 //sname = "Actions for " + NONAME + " (Symbol " + m.group(2) + ")";
                 continue; //FIXME!
                 }//*/

                items.add(new SWD.DebugScript(moduleId, bitmap, sname, txt));
                for (int ofs : offSetToLine.keySet()) {
                    items.add(new SWD.DebugOffset(moduleId, offSetToLine.get(ofs), ofs));
                }
                if (breakpoints.containsKey(name)) {
                    Set<Integer> bplines = breakpoints.get(name);
                    for (int bpline : bplines) {
                        if (lineToOffset.containsKey(bpline)) {
                            try {
                                SWD.DebugBreakpoint dbp = new SWD.DebugBreakpoint(moduleId, bpline);
                                items.add(dbp);
                            } catch (IllegalArgumentException iex) {
                                logger.log(Level.WARNING, "Cannot generate breakpoint to SWD: {0}", iex.getMessage());
                            }
                        }
                    }
                }
                items.addAll(regitems);
                //moduleId++;
            }
            //items.addAll(swdOffsets);
            //items.addAll(swfBps);

        } catch (Throwable t) {
            logger.log(Level.SEVERE, "message", t);
            return false;
        }
        SWD swd = new SWD(7, items);
        try (FileOutputStream fis = new FileOutputStream(file)) {
            swd.saveTo(fis);
        }
        return true;
    }

    public boolean enableTelemetry(String password) {

        EnableTelemetryTag et = getEnableTelemetry();

        if (et == null) {
            FileAttributesTag fat = getFileAttributes();
            if (fat == null) {
                return false;
            }
            int insertTo = tags.indexOf(fat) + 1;
            MetadataTag mt = getMetadata();
            if (mt != null) {
                insertTo = tags.indexOf(mt) + 1;
            }

            et = new EnableTelemetryTag(this);
            tags.add(insertTo, et);
        }
        et.setPassword(password);
        //TODO: SWFs with tag 92 (signed) are unsupported
        return true;
    }

    public String getFlexMainClass(List<String> ignoredClasses, List<String> ignoredNs) {
        String documentClass = getDocumentClass();

        ScriptPack documentPack = null;
        for (ScriptPack item : getAS3Packs()) {
            if (item.getClassPath().toString().equals(documentClass)) {
                documentPack = item;
                break;
            }
        }

        if (documentPack != null) {
            if (!documentPack.traitIndices.isEmpty()) {
                Trait firstTrait = documentPack.abc.script_info.get(documentPack.scriptIndex).traits.traits.get(documentPack.traitIndices.get(0));
                if (firstTrait instanceof TraitClass) {
                    int cindex = ((TraitClass) firstTrait).class_info;
                    Multiname superName = documentPack.abc.constants.getMultiname(documentPack.abc.instance_info.get(cindex).super_index);
                    String parentClass = superName.getNameWithNamespace(documentPack.abc.constants, true).toRawString();
                    if ("mx.managers.SystemManager".equals(parentClass)) {
                        for (Trait t : documentPack.abc.instance_info.get(cindex).instance_traits.traits) {
                            if ((t instanceof TraitMethodGetterSetter) && "info".equals(t.getName(documentPack.abc).getName(documentPack.abc.constants, new ArrayList<>(), true, true))) {

                                int mi = ((TraitMethodGetterSetter) t).method_info;
                                try {
                                    documentPack.abc.findBody(mi).convert(new ConvertData(), "??", ScriptExportMode.AS, true, mi, documentPack.scriptIndex, cindex, documentPack.abc, t, new ScopeStack(), 0, new NulWriter(), new ArrayList<>(), new ArrayList<>(), true, new HashSet<>());
                                    List<GraphTargetItem> infos = documentPack.abc.findBody(mi).convertedItems;
                                    if (!infos.isEmpty()) {
                                        if (infos.get(0) instanceof IfItem) {
                                            IfItem ift = ((IfItem) infos.get(0));
                                            if (!ift.onTrue.isEmpty()) {
                                                if (ift.onTrue.get(0) instanceof InitPropertyAVM2Item) {
                                                    if (ift.onTrue.get(0).value instanceof NewObjectAVM2Item) {
                                                        NewObjectAVM2Item no = (NewObjectAVM2Item) ift.onTrue.get(0).value;
                                                        List<String> compiledLocales = new ArrayList<>();
                                                        List<String> compiledResourceBundleNames = new ArrayList<>();
                                                        List<String> mixins = new ArrayList<>();
                                                        String mainClassName = null;
                                                        //currentDomain,preloader
                                                        /*double width = 0;
                                                         double height = 0;
                                                         */
                                                        for (NameValuePair nvp : no.pairs) {
                                                            if (nvp.name instanceof StringAVM2Item) {
                                                                String n = ((StringAVM2Item) nvp.name).getValue();
                                                                switch (n) {
                                                                    case "compiledLocales":
                                                                        if (nvp.value instanceof NewArrayAVM2Item) {
                                                                            NewArrayAVM2Item na = (NewArrayAVM2Item) nvp.value;
                                                                            for (GraphTargetItem tv : na.values) {
                                                                                compiledLocales.add("" + tv.getResult());
                                                                            }
                                                                        }
                                                                        break;
                                                                    case "compiledResourceBundleNames":
                                                                        if (nvp.value instanceof NewArrayAVM2Item) {
                                                                            NewArrayAVM2Item na = (NewArrayAVM2Item) nvp.value;
                                                                            for (GraphTargetItem tv : na.values) {
                                                                                compiledResourceBundleNames.add("" + tv.getResult());
                                                                            }
                                                                        }
                                                                        break;
                                                                    case "mixins":
                                                                        if (nvp.value instanceof NewArrayAVM2Item) {
                                                                            NewArrayAVM2Item na = (NewArrayAVM2Item) nvp.value;
                                                                            for (GraphTargetItem tv : na.values) {
                                                                                mixins.add("" + tv.getResult());
                                                                            }
                                                                        }
                                                                        break;
                                                                    /*case "width":
                                                                     width = Double.parseDouble("" + nvp.value.getResult());
                                                                     break;
                                                                     case "height":
                                                                     height = Double.parseDouble("" + nvp.value.getResult());
                                                                     break;*/
                                                                    case "mainClassName":
                                                                        mainClassName = "" + nvp.value.getResult();
                                                                        break;
                                                                }
                                                            }
                                                        }

                                                        ignoredClasses.add(documentClass);
                                                        for (String loc : compiledLocales) {
                                                            ignoredClasses.add(loc + "$" + "controls" + "_properties");
                                                            for (String res : compiledResourceBundleNames) {
                                                                ignoredClasses.add(loc + "$" + res + "_properties");
                                                            }
                                                        }
                                                        ignoredClasses.addAll(mixins);

                                                        //find internal classes used in mixins
                                                        for (ScriptPack p : getAS3Packs()) {
                                                            for (String m : mixins) {
                                                                if (m.equals(p.getClassPath().toRawString())) {
                                                                    for (int ti : p.traitIndices) {
                                                                        Trait tr = p.abc.script_info.get(p.scriptIndex).traits.traits.get(ti);
                                                                        if (tr instanceof TraitClass) {
                                                                            int ci = ((TraitClass) tr).class_info;
                                                                            int cinit = p.abc.class_info.get(ci).cinit_index;
                                                                            p.abc.findBody(cinit).convert(new ConvertData(), "??", ScriptExportMode.AS, true, cinit, p.scriptIndex, cindex, p.abc, t, new ScopeStack(), 0, new NulWriter(), new ArrayList<>(), new ArrayList<>(), true, new HashSet<>());
                                                                            List<GraphTargetItem> cinitBody = p.abc.findBody(cinit).convertedItems;
                                                                            for (GraphTargetItem cit : cinitBody) {
                                                                                if (cit instanceof SetPropertyAVM2Item) {
                                                                                    if (cit.value instanceof GetLexAVM2Item) {
                                                                                        GetLexAVM2Item gl = (GetLexAVM2Item) cit.value;
                                                                                        ignoredClasses.add(gl.propertyName.getNameWithNamespace(p.abc.constants, true).toRawString());
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        ignoredNs.add("mx");
                                                        ignoredNs.add("spark");
                                                        ignoredNs.add("flashx");
                                                        return mainClassName;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } catch (InterruptedException e) {
                                    //ignore
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void replaceTag(int index, Tag newTag) {
        removeTag(index);
        addTag(index, newTag);
    }
}
