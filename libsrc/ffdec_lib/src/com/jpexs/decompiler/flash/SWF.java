/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.abc.avm2.parser.script.AbcIndexing;
import com.jpexs.decompiler.flash.abc.types.ConvertData;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitClass;
import com.jpexs.decompiler.flash.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionGraphSource;
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.action.ActionListReader;
import com.jpexs.decompiler.flash.action.ActionLocalData;
import com.jpexs.decompiler.flash.action.as2.UninitializedClassFieldsDetector;
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
import com.jpexs.decompiler.flash.configuration.CustomConfigurationKeys;
import com.jpexs.decompiler.flash.configuration.SwfSpecificCustomConfiguration;
import com.jpexs.decompiler.flash.dumpview.DumpInfo;
import com.jpexs.decompiler.flash.dumpview.DumpInfoSwfNode;
import com.jpexs.decompiler.flash.ecma.Null;
import com.jpexs.decompiler.flash.exporters.commonshape.ExportRectangle;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.exporters.script.AS2ScriptExporter;
import com.jpexs.decompiler.flash.exporters.script.AS3ScriptExporter;
import com.jpexs.decompiler.flash.exporters.settings.ScriptExportSettings;
import com.jpexs.decompiler.flash.exporters.shape.ShapeExportData;
import com.jpexs.decompiler.flash.harman.HarmanSwfEncrypt;
import com.jpexs.decompiler.flash.helpers.HighlightedText;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.helpers.NulWriter;
import com.jpexs.decompiler.flash.helpers.SWFDecompilerPlugin;
import com.jpexs.decompiler.flash.helpers.collections.MyEntry;
import com.jpexs.decompiler.flash.helpers.hilight.Highlighting;
import com.jpexs.decompiler.flash.helpers.hilight.HighlightingList;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.flash.tags.DebugIDTag;
import com.jpexs.decompiler.flash.tags.DefineBinaryDataTag;
import com.jpexs.decompiler.flash.tags.DefineScalingGridTag;
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
import com.jpexs.decompiler.flash.tags.base.BinaryDataInterface;
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
import com.jpexs.decompiler.flash.tags.gfx.DefineExternalImage2;
import com.jpexs.decompiler.flash.tags.gfx.ExporterInfo;
import com.jpexs.decompiler.flash.timeline.AS2Package;
import com.jpexs.decompiler.flash.timeline.Frame;
import com.jpexs.decompiler.flash.timeline.FrameScript;
import com.jpexs.decompiler.flash.timeline.TagScript;
import com.jpexs.decompiler.flash.timeline.Timeline;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.treeitems.Openable;
import com.jpexs.decompiler.flash.treeitems.OpenableList;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.decompiler.flash.types.SOUNDINFO;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.decompiler.flash.types.annotations.SWFField;
import com.jpexs.decompiler.flash.types.sound.SoundInfoSoundCacheEntry;
import com.jpexs.decompiler.flash.xfl.FLAVersion;
import com.jpexs.decompiler.flash.xfl.XFLConverter;
import com.jpexs.decompiler.flash.xfl.XFLExportSettings;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemContainer;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.ScopeStack;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.model.IfItem;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.Cache;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.ImmediateFuture;
import com.jpexs.helpers.NulStream;
import com.jpexs.helpers.ProgressListener;
import com.jpexs.helpers.Reference;
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Class representing SWF file.
 *
 * @author JPEXS
 */
public final class SWF implements SWFContainerItem, Timelined, Openable {

    /**
     * Default version of SWF file format.
     */
    public static final int DEFAULT_VERSION = 10;

    /**
     * Maximum SWF file format version Needs to be fixed when SWF versions
     * reaches this value.
     */
    public static final int MAX_VERSION = 64;

    /**
     * Tags inside of file.
     */
    @SWFField
    private List<Tag> tags = new ArrayList<>();

    /**
     * Readonly view of tags of the file.
     */
    @Internal
    public ReadOnlyTagList readOnlyTags;

    /**
     * Whether this SWF file has EndTag and the end of tag list.
     */
    public boolean hasEndTag = true;

    /**
     * ExportRectangle for the display.
     */
    public RECT displayRect;

    /**
     * Movie frame rate.
     */
    public float frameRate;

    /**
     * Number of frames in movie.
     */
    public int frameCount;

    /**
     * Version of SWF.
     */
    public int version;

    /**
     * Uncompressed size of the file.
     */
    @Internal
    public long fileSize;

    /**
     * Used compression mode.
     */
    public SWFCompression compression = SWFCompression.NONE;

    /**
     * Compressed size of the file (LZMA).
     */
    @Internal
    public long compressedSize;

    /**
     * LZMA Properties.
     */
    public byte[] lzmaProperties;

    /**
     * Uncompressed data.
     */
    @Internal
    public byte[] uncompressedData;

    /**
     * Original uncompressedData before saving.
     */
    @Internal
    public byte[] originalUncompressedData;

    /**
     * Whether this file is ScaleForm GFx.
     */
    public boolean gfx = false;

    /**
     * Whether the file uses HARMAN encryption.
     */
    public boolean encrypted = false;

    /**
     * OpenableList which this SWF is part of.
     */
    @Internal
    public OpenableList openableList;

    /**
     * File path from this SWF was loaded. Can be null.
     */
    @Internal
    private String file;

    /**
     * File title. Can be null.
     */
    @Internal
    private String fileTitle;

    /**
     * Map of characterId to CharacterTag for non-imported tags.
     */
    @Internal
    private volatile Map<Integer, CharacterTag> characters;

    /**
     * Map of characterId to CharacterTag including imported tags. The
     * CharacterTags.getCharacterId() does not necessarily be the characterId
     * in the map since there can be imported CharacterTags from other SWFs.
     */
    @Internal
    private volatile Map<Integer, CharacterTag> charactersWithImported;

    /**
     * Map of characterIdTags to characterId in this SWF file. It is not enough
     * to call getCharacterId(), because there can be imported tags.s
     */
    @Internal
    private volatile Map<CharacterIdTag, Integer> characterToId;

    /**
     * Map of imageId to DefineExternalImage2s.
     */
    @Internal
    private volatile Map<Integer, DefineExternalImage2> externalImages2;

    /**
     * List of all CharacterId tags for specified characterId.
     */
    @Internal
    private volatile Map<Integer, List<CharacterIdTag>> characterIdTags;

    /**
     * Map of characterId to Set of dependent characterIds.
     */
    @Internal
    private volatile Map<Integer, Set<Integer>> dependentCharacters;

    /**
     * Map of characterId to Set of dependent frame numbers.
     */
    @Internal
    private volatile Map<Integer, Set<Integer>> dependentFrames;

    /**
     * List of ABC container tags.
     */
    @Internal
    private volatile List<ABCContainerTag> abcList;

    /**
     * JPEGTables tag
     */
    @Internal
    private volatile JPEGTablesTag jtt;

    /**
     * Map of fontId to font name from which take new characters, line spacing,
     * etc.
     */
    @Internal
    public Map<Integer, String> sourceFontNamesMap = new HashMap<>();

    /**
     * Pixel to twip conversion.
     */
    public static final double unitDivisor = 20;

    /**
     * Logger.
     */
    private static final Logger logger = Logger.getLogger(SWF.class.getName());

    /**
     * Modified flag.
     */
    @Internal
    private boolean isModified;

    /**
     * Cached timeline.
     */
    @Internal
    private Timeline timeline;

    /**
     * Dump info.
     */
    @Internal
    public DumpInfoSwfNode dumpInfo;

    /**
     * Parent BinaryData which this SWF resides in.
     */
    @Internal
    public BinaryDataInterface binaryData;

    /**
     * Map of deobfuscated names.
     */
    @Internal
    private final HashMap<DottedChain, DottedChain> deobfuscated = new HashMap<>();

    /**
     * Deobfuscation.
     */
    @Internal
    private final IdentifiersDeobfuscation deobfuscation = new IdentifiersDeobfuscation();

    /**
     * Frame cache.
     */
    @Internal
    private final Cache<String, SerializableImage> frameCache = Cache.getInstance(false, false, "frame", true);

    /**
     * Rect cache.
     */
    @Internal
    private final Cache<CharacterTag, RECT> rectCache = Cache.getInstance(true, true, "rect", true);

    /**
     * Shape export data cache.
     */
    @Internal
    private final Cache<SHAPE, ShapeExportData> shapeExportDataCache = Cache.getInstance(true, true, "shapeExportData", true);

    /**
     * Sound cache.
     */
    @Internal
    private final Cache<SoundInfoSoundCacheEntry, byte[]> soundCache = Cache.getInstance(false, false, "sound", true);

    /**
     * AS2 cache.
     */
    @Internal
    public final AS2Cache as2Cache = new AS2Cache();

    /**
     * AS3 cache.
     */
    @Internal
    public final AS3Cache as3Cache = new AS3Cache();

    /**
     * Cache of ASMSources with export filenames as scriptname.
     */
    @Internal
    private Map<String, ASMSource> asmsCacheExportFilenames;

    /**
     * Cache of ASMSources with standard scriptnames.
     */
    @Internal
    private Map<String, ASMSource> asmsCache;

    /**
     * SWF was already freed flag.
     */
    @Internal
    private boolean destroyed = false;

    /**
     * Set of cyclic characterIds.
     */
    @Internal
    private Set<Integer> cyclicCharacters = null;

    /**
     * Header modified flag.
     */
    @Internal
    private boolean headerModified = false;

    /**
     * Charset for SWF files with version 5 and lower which do not use UTF-8.
     */
    @Internal
    private String charset = "UTF-8";

    /**
     * Map of characterId to imported class sets.
     */
    @Internal
    private final Map<Integer, LinkedHashSet<String>> importedTagToClassesMapping = new HashMap<>();

    /**
     * Map of characterId to imported name.
     */
    @Internal
    private final Map<Integer, String> importedTagToExportNameMapping = new HashMap<>();

    /**
     * Class to character id map.
     */
    @Internal
    private final Map<String, Integer> classToCharacterId = new HashMap<>();

    /**
     * Decompiler pool.
     */
    private static final DecompilerPool decompilerPool = new DecompilerPool();

    /**
     * Export name to characterId.
     */
    @Internal
    private final Map<String, Integer> exportNameToCharacter = new HashMap<>();

    /**
     * Imported name to characterId.
     */
    @Internal
    private final Map<String, Integer> importedNameToCharacter = new HashMap<>();

    /**
     * ABC indexing.
     */
    @Internal
    private AbcIndexing abcIndex;

    /**
     * Number of ABCIndex dependencies.
     */
    @Internal
    private int numAbcIndexDependencies = 0;

    /**
     * Uninitialized AS2 class traits. Class name to trait name to trait.
     */
    private volatile Map<String, Map<String, com.jpexs.decompiler.flash.action.as2.Trait>> uninitializedAs2ClassTraits = null;

    /**
     * ExporterInfo tag.
     */
    @Internal
    private ExporterInfo exporterInfo = null;

    /**
     * Name of debuggerPackage.
     */
    @Internal
    public String debuggerPackage = null;

    /**
     * Imported characterId to SWF map.
     */
    private final Map<Integer, SWF> importedCharacterSourceSwfs = new HashMap<>();

    /**
     * Imported class to imported URL map.
     */
    private final Map<String, String> importedClassSourceUrls = new HashMap<>();

    /**
     * Map of characterIds of this SWF file to characterIds of imported SWF
     * file.
     */
    private final Map<Integer, Integer> importedCharacterIds = new HashMap<>();

    /**
     * Map of imported classes to characterTags.
     */
    private final Map<String, CharacterTag> importedClassToCharacter = new HashMap<>();

    /**
     * Playerglobal.swf ABCIndex
     */
    private static AbcIndexing playerGlobalAbcIndex;

    /**
     * Airglobal.swf ABCIndex
     */
    private static AbcIndexing airGlobalAbcIndex;

    /**
     * Prefix of exportname of DefineSprites of AS2 classes.
     */
    public static final String AS2_PKG_PREFIX = "__Packages.";

    /**
     * Known SWF signatures.
     */
    public static List<String> swfSignatures = Arrays.asList(
            "FWS", // Uncompressed Flash
            "CWS", // ZLib compressed Flash
            "ZWS", // LZMA compressed Flash
            "GFX", // Uncompressed ScaleForm GFx
            "CFX", // Compressed ScaleForm GFx
            "ABC", // Non-standard LZMA compressed Flash
            "fWS", //Harman encrypted uncompressed Flash,
            "cWS", //Harman encrypted ZLib compressed Flash,
            "zWS" //Harman encrypted LZMA compressed Flash
    );

    /**
     * Color to paint when there is an error (missing image, ...).
     */
    public static final Color ERROR_COLOR = Color.red;

    /**
     * Use AIR library
     */
    public static final int LIBRARY_AIR = 0;
    /**
     * Use Flash library
     */
    public static final int LIBRARY_FLASH = 1;

    /**
     * Event listeners
     */
    private final HashSet<EventListener> listeners = new HashSet<>();

    /**
     * Lock for characters synchronization
     */
    private final Object charactersLock = new Object();

    /**
     * Sets main GFX exporterinfo tag
     *
     * @param exporterInfo ExporterInfo
     */
    public void setExporterInfo(ExporterInfo exporterInfo) {
        this.exporterInfo = exporterInfo;
    }

    /**
     * Gets main GFX exporterinfo tag
     *
     * @return ExporterInfo
     */
    public ExporterInfo getExporterInfo() {
        return exporterInfo;
    }

    /**
     * Checks whether the ExporterInfo (GFX) tag has flag for stripping shapes
     * from DefineFont.
     *
     * @return True if the flag is set, false otherwise.
     */
    public boolean hasStrippedShapesFromFonts() {
        if (exporterInfo == null) {
            return false;
        }
        if (exporterInfo.hasFlagShapesStrippedFromDefineFont()) {
            return true;
        }
        return false;
    }

    /**
     * Gets main ABCIndexing object for playerglobal.swc
     *
     * @return ABCIndexing
     */
    public static AbcIndexing getPlayerGlobalAbcIndex() {
        return playerGlobalAbcIndex;
    }

    /**
     * Gets main ABCIndexing object for airglobal.swc
     *
     * @return ABCIndexing
     */
    public static AbcIndexing getAirGlobalAbcIndex() {
        return airGlobalAbcIndex;
    }

    /**
     * Resets AbcIndex. Next call to getAbcIndex will calculate it again.
     */
    public void resetAbcIndex() {
        abcIndex = null;
    }

    /**
     * Gets ABCIndexing object
     *
     * @return ABCIndexing
     */
    public AbcIndexing getAbcIndex() {
        if (abcIndex != null) {
            return abcIndex;
        }
        boolean air = false;
        SwfSpecificCustomConfiguration conf = Configuration.getSwfSpecificCustomConfiguration(getShortPathTitle());
        if (conf != null) {
            if (conf.getCustomData(CustomConfigurationKeys.KEY_LIBRARY, "" + LIBRARY_FLASH).equals("" + LIBRARY_AIR)) {
                air = true;
            }
        }
        try {
            SWF.initPlayer();
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(SWF.class.getName()).log(Level.SEVERE, null, ex);
        }
        abcIndex = new AbcIndexing(air ? SWF.getAirGlobalAbcIndex() : SWF.getPlayerGlobalAbcIndex());
        for (Tag tag : tags) {
            if (tag instanceof ABCContainerTag) {
                abcIndex.addAbc(((ABCContainerTag) tag).getABC());
            }
        }
        abcIndex.rebuildPkgToObjectsNameMap();
        return abcIndex;
    }

    /**
     * Gets number of ABCIndex dependent SWFs
     *
     * @return Number of dependent SWFs
     */
    public int getNumAbcIndexDependencies() {
        return numAbcIndexDependencies;
    }

    /**
     * Set dependencies for ABCIndex for resolving names during editation.
     *
     * @param swfs List of SWFs to set as dependencies
     */
    public void setAbcIndexDependencies(List<SWF> swfs) {
        abcIndex = null;
        getAbcIndex();
        for (SWF swf : swfs) {
            for (Tag tag : swf.tags) {
                if (tag instanceof ABCContainerTag) {
                    abcIndex.addAbc(((ABCContainerTag) tag).getABC());
                }
            }
        }
        abcIndex.rebuildPkgToObjectsNameMap();
        numAbcIndexDependencies = swfs.size();
    }

    /**
     * Init main AbcIndexes of playerGlobal and airGlobal
     *
     * @throws IOException On I/O error
     * @throws InterruptedException On interrupt
     */
    public static void initPlayer() throws IOException, InterruptedException {
        if (playerGlobalAbcIndex == null) {
            /*if (Configuration.getPlayerSWC() == null) {
                throw new IOException("Player SWC library not found, please place it to " + Configuration.getFlashLibPath());
            }*/

            if (Configuration.getPlayerSWC() != null) {
                SWC swc = new SWC(new FileInputStream(Configuration.getPlayerSWC()));
                //set allowRenameIdentifiers parameter to FALSE otherwise there will be an infinite loop
                SWF swf = new SWF(swc.getOpenable("library.swf"), null, null, null, true, false, true, null, Charset.defaultCharset().name(), false);
                playerGlobalAbcIndex = new AbcIndexing(swf);
            }
        }
        if (airGlobalAbcIndex == null) {
            if (Configuration.getAirSWC() != null) {
                SWC swc = new SWC(new FileInputStream(Configuration.getAirSWC()));
                //set allowRenameIdentifiers to FALSE
                SWF swf = new SWF(swc.getOpenable("library.swf"), null, null, null, true, false, true, null, Charset.defaultCharset().name(), false);
                airGlobalAbcIndex = new AbcIndexing(swf);
            }
        }
    }

    /**
     * Gets SWF charset. SWF version 5 or lower were non-unicode. SWF object has
     * assigned charset.
     *
     * @return Charset
     */
    public String getCharset() {
        return charset;
    }

    /**
     * Sets SWF charset. SWF version 5 or lower were non-unicode. SWF object has
     * assigned charset.
     *
     * @param charset Charset
     */
    public void setCharset(String charset) {
        this.charset = charset;
    }

    /**
     * Sets modification flag for header.
     *
     * @param headerModified Header modified flag
     */
    public void setHeaderModified(boolean headerModified) {
        this.headerModified = headerModified;
    }

    /**
     * Gets modification flag for header.s
     *
     * @return True if header was modified, false otherwise
     */
    public boolean isHeaderModified() {
        return headerModified;
    }

    /**
     * Refreshes character cache. Call this when you modify character ids, etc.
     */
    public void updateCharacters() {
        characters = null;
        charactersWithImported = null;
        characterToId = null;
        characterIdTags = null;
        externalImages2 = null;
    }

    /**
     * Frees all tags and SWFs inside, destroys this SWF.
     */
    public void clearTagSwfs() {
        destroyed = true;
        decompilerPool.destroySwf(this);
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
            abcList = null;
        }

        if (openableList != null) {
            openableList.items.clear();
        }

        clearScriptCache();
        frameCache.clear();
        soundCache.clear();

        clearImageCache();
        clearShapeCache();
        clearAbcListCache();

        characters = null;
        charactersWithImported = null;
        characterToId = null;
        characterIdTags = null;
        externalImages2 = null;

        timeline = null;
        if (dumpInfo != null) {
            clearDumpInfo(dumpInfo);
        }
        dumpInfo = null;
        jtt = null;
        binaryData = null;
    }

    /**
     * Clears specified dump info.
     *
     * @param di Dump info to clear
     */
    private void clearDumpInfo(DumpInfo di) {
        for (DumpInfo childInfo : di.getChildInfos()) {
            clearDumpInfo(childInfo);
        }

        di.getChildInfos().clear();
    }

    /**
     * Gets map of character id to character tag. When withImported argument
     * passed, the CharacterTags in the map can have different character id than
     * the one assigned through the map as they can come from different
     * (imported) SWF.
     *
     * @param withImported Include tags imported with importasset/2 tag?
     * @return Character id to character map
     */
    public Map<Integer, CharacterTag> getCharacters(boolean withImported) {
        Map<Integer, CharacterTag> newCharacters = characters;
        Map<Integer, CharacterTag> newCharactersWithImported = charactersWithImported;
        synchronized (charactersLock) {
            if (newCharacters == null || newCharactersWithImported == null) {
                if (destroyed) {
                    return new HashMap<>();
                }
                Map<Integer, CharacterTag> chars = new HashMap<>();
                Map<Integer, CharacterTag> charsWithImported = new HashMap<>();
                Map<Integer, List<CharacterIdTag>> charIdtags = new HashMap<>();
                Map<Integer, DefineExternalImage2> eimages = new HashMap<>();
                parseCharacters(getTags(), eimages, chars, charIdtags);
                charsWithImported.putAll(chars);
                for (int importedCharacterId : importedCharacterIds.keySet()) {
                    int exportedCharacterId = importedCharacterIds.get(importedCharacterId);
                    SWF importedSwf = importedCharacterSourceSwfs.get(importedCharacterId);
                    CharacterTag exportedCharacter = importedSwf.getCharacter(exportedCharacterId);
                    charsWithImported.put(importedCharacterId, exportedCharacter);
                    charIdtags.put(importedCharacterId, importedSwf.getCharacterIdTags(exportedCharacterId));
                    //FIXME? eimages

                    charsWithImported.get(importedCharacterId).setImported(true, true);
                    for (CharacterIdTag chi : charIdtags.get(importedCharacterId)) {
                        if (chi instanceof Tag) {
                            ((Tag) chi).setImported(true, true);
                        }
                    }
                }
                Map<CharacterIdTag, Integer> charToId = new IdentityHashMap<>();
                for (int id : charsWithImported.keySet()) {
                    charToId.put(charsWithImported.get(id), id);
                }
                for (int id : charIdtags.keySet()) {
                    for (CharacterIdTag ch : charIdtags.get(id)) {
                        charToId.put(ch, id);
                    }
                }

                newCharacters = Collections.unmodifiableMap(chars);
                newCharactersWithImported = Collections.unmodifiableMap(charsWithImported);
                characters = newCharacters;
                charactersWithImported = newCharactersWithImported;
                characterToId = Collections.unmodifiableMap(charToId);
                characterIdTags = Collections.unmodifiableMap(charIdtags);
                externalImages2 = Collections.unmodifiableMap(eimages);
            }

            return withImported ? newCharactersWithImported : newCharacters;
        }
    }

    /**
     * Gets map of GFX DefineExternalImage2 tags.
     *
     * @return Map of GFX imageId to DefineExternalImage2 tag
     */
    public Map<Integer, DefineExternalImage2> getExternalImages2() {
        if (externalImages2 == null) {
            getCharacters(true);
        }
        return externalImages2;
    }

    /**
     * Gets GFX DefineExternalImage2 by imageId.
     *
     * @param imageId Id of the image. It is not a character id!
     * @return DefineExternalImage2 or null when not found
     */
    public DefineExternalImage2 getExternalImage2(int imageId) {
        Map<Integer, DefineExternalImage2> images = getExternalImages2();
        return images.get(imageId);
    }

    /**
     * Gets all CharacterIdTags for specified character (with the same
     * characterId).
     *
     * @param characterId Character id
     * @return List of CharacterIdTags
     */
    public List<CharacterIdTag> getCharacterIdTags(int characterId) {
        if (characterIdTags == null) {
            getCharacters(true);
        }

        return characterIdTags.get(characterId);
    }

    /**
     * Gets CharacterIdTag with specific tag type.
     *
     * @param characterId Id of the character
     * @param tagId Id of type of the tag. For example DefineFontNameTag.ID
     * @return CharacterIdTag or null when not found
     */
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

    /**
     * Computes dependent characters of specified Timelined object.
     *
     * @param timelined Timelined object
     * @param dep Adds results to this map.
     */
    private void computeDependentCharacters(Timelined timelined, Map<Integer, Set<Integer>> dep) {
        for (Tag tag : timelined.getTags()) {
            if (tag instanceof CharacterTag) {
                int characterId = ((CharacterTag) tag).getCharacterId();
                if (characterId != -1) {
                    Set<Integer> needed = new HashSet<>();
                    tag.getNeededCharacters(needed, this);
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
            if (tag instanceof DefineSpriteTag) {
                computeDependentCharacters((DefineSpriteTag) tag, dep);
            }
        }
    }

    /**
     * Computes dependent characters.
     */
    public void computeDependentCharacters() {
        Map<Integer, Set<Integer>> dep = new HashMap<>();
        computeDependentCharacters(this, dep);

        dependentCharacters = dep;
    }

    /**
     * Gets all dependent character map.
     *
     * @return Map of characterId to set of dependent characterIds
     */
    public Map<Integer, Set<Integer>> getDependentCharacters() {
        if (dependentCharacters == null) {
            synchronized (this) {
                if (dependentCharacters == null) {
                    computeDependentCharacters();
                }
            }
        }

        return dependentCharacters;
    }

    /**
     * Gets dependent characters of a character.
     *
     * @param characterId Character id
     * @return Set of dependent characterIds
     */
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
                    if (getCharacters(true).containsKey(chId)) {
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
            if (getCharacters(true).containsKey(chId)) {
                dependents.add(chId);
            }
        }

        return dependents;
    }

    /**
     * Computes dependent frames.
     */
    public void computeDependentFrames() {
        Map<Integer, Set<Integer>> dep = new HashMap<>();
        Timeline tim = getTimeline();
        for (int i = 0; i < tim.getFrameCount(); i++) {
            Frame frame = tim.getFrame(i);
            Set<Integer> needed = new HashSet<>();
            frame.getNeededCharactersDeep(needed);
            for (Integer needed1 : needed) {
                Set<Integer> s = dep.get(needed1);
                if (s == null) {
                    s = new HashSet<>();
                    dep.put(needed1, s);
                }

                s.add(i);
            }
        }

        dependentFrames = dep;
    }

    /**
     * Gets dependent frames for specified character.
     *
     * @param characterId Character id
     * @return Set of dependent characterids
     */
    public Set<Integer> getDependentFrames(int characterId) {
        if (dependentFrames == null) {
            synchronized (this) {
                if (dependentFrames == null) {
                    computeDependentFrames();
                }
            }
        }

        return dependentFrames.get(characterId);
    }

    /**
     * Gets character tag by character id
     *
     * @param characterId Character id
     * @return CharacterTag or null when not found
     */
    public CharacterTag getCharacter(int characterId) {
        return getCharacters(true).get(characterId);
    }

    /**
     * Gets character tag by the assigned AS3 class name (SymbolClass tag)
     *
     * @param className Class name
     * @return CharacterTag or null when not found
     */
    public CharacterTag getCharacterByClass(String className) {
        if (importedClassToCharacter.containsKey(className)) {
            return importedClassToCharacter.get(className);
        }
        if (classToCharacterId.containsKey(className)) {
            int charId = classToCharacterId.get(className);
            return getCharacter(charId);
        }
        return null;
    }

    /**
     * Gets character tag by the assigned export name (ExportAssets tag)
     *
     * @param exportName Export name
     * @return CharacterTag or null when not found
     */
    public CharacterTag getCharacterByExportName(String exportName) {
        int charId;
        if (importedNameToCharacter.containsKey(exportName)) {
            charId = importedNameToCharacter.get(exportName);
        } else if (exportNameToCharacter.containsKey(exportName)) {
            charId = exportNameToCharacter.get(exportName);
        } else {
            return null;
        }
        return getCharacter(charId);
    }

    /**
     * Gets export name for specified character. Export names come from
     * ExportAssets tag.
     *
     * @param characterId Character id
     * @return Export name or null when no assigned exportname.
     */
    public String getExportName(int characterId) {
        CharacterTag characterTag = getCharacters(true).get(characterId);
        String exportName = characterTag != null ? characterTag.getExportName() : null;
        return exportName;
    }

    /**
     * Gets URL from where the specified class was imported (ImportAssets/2
     * tag).
     *
     * @param className Class name
     * @return URL or null when it's not imported
     */
    public String getClassSourceUrl(String className) {
        return importedClassSourceUrls.get(className);
    }

    /**
     * Gets FontTag by class name.
     *
     * @param fontClass Class name
     * @return FontTag or null when not found
     */
    public FontTag getFontByClass(String fontClass) {
        if (fontClass == null) {
            return null;
        }
        CharacterTag t = getCharacterByClass(fontClass);
        if (t instanceof FontTag) {
            return (FontTag) t;
        }

        return null;
    }

    /**
     * Gets FontTag by font name (that one from DefineFontNameTag).
     *
     * @param fontName Font name
     * @return FontTag or null when not found
     */
    public FontTag getFontByName(String fontName) {
        if (fontName == null) {
            return null;
        }
        for (Tag t : getCharacters(true).values()) {
            if (t instanceof FontTag) {
                if (fontName.equals(((FontTag) t).getFontName())) {
                    return (FontTag) t;
                }
            }
        }
        return null;
    }

    /**
     * Gets FontTag by font name in tag and font style. The font name is taken
     * directly from that tag (from FontInfoTag for DefineFont1).
     *
     * @param fontName Font name
     * @param bold Bold
     * @param italic Italic
     * @return FontTag or null when not found
     */
    public FontTag getFontByNameInTag(String fontName, boolean bold, boolean italic) {
        if (fontName == null) {
            return null;
        }
        for (Tag t : getCharacters(true).values()) {
            if (t instanceof FontTag) {
                FontTag ft = (FontTag) t;
                if (fontName.equals(ft.getFontNameIntag()) && ft.isBold() == bold && ft.isItalic() == italic) {
                    return (FontTag) t;
                }
            }
        }
        return null;
    }

    /**
     * Gets real character id of a character tag on this SWF. Normal
     * .getCharacterId method od the CharacterTag does not work for imported
     * characters.
     *
     * @param tag CharacterId tag
     * @return Character id or -1 if not found
     */
    public int getCharacterId(CharacterIdTag tag) {
        if (characterToId == null) {
            getCharacters(true);
        }
        Map<CharacterIdTag, Integer> characterToId2 = characterToId;
        if (characterToId2 == null) {
            return -1;
        }
        if (!characterToId2.containsKey(tag)) {
            return -1;
        }
        return characterToId2.get(tag);
    }

    /**
     * Gets FontTag by fontId (= characterId). Logs a SEVERE message when the
     * charter found, but is not a FontTag.
     *
     * @param fontId Font id
     * @return FontTag or null when not found or the character is not a FontTag
     */
    public FontTag getFont(int fontId) {
        CharacterTag characterTag = getCharacters(true).get(fontId);
        if (characterTag instanceof FontTag) {
            return (FontTag) characterTag;
        }

        if (characterTag != null) {
            logger.log(Level.SEVERE, "CharacterTag should be a FontTag. characterId: {0}", fontId);
        }

        return null;
    }

    /**
     * Gets ImageTag by imageId (= characterId). Logs a SEVERE message when the
     * charter found, but is not a ImageTag.
     *
     * @param imageId Image id
     * @return ImageTag or null when not found or the character is not an
     * ImageTag
     */
    public ImageTag getImage(int imageId) {
        CharacterTag characterTag = getCharacters(true).get(imageId);
        if (characterTag instanceof ImageTag) {
            return (ImageTag) characterTag;
        }

        if (characterTag != null) {
            logger.log(Level.SEVERE, "CharacterTag should be an ImageTag. characterId: {0}", imageId);
        }

        return null;
    }

    /**
     * Gets DefineSoundTag by soundId (= characterId). Logs a SEVERE message
     * when the charter found, but is not a DefineSoundTag.
     *
     * @param soundId Sound id
     * @return DefineSoundTag or null when not found or the character is not an
     * DefineSoundTag
     */
    public DefineSoundTag getSound(int soundId) {
        CharacterTag characterTag = getCharacters(true).get(soundId);
        if (characterTag instanceof DefineSoundTag) {
            return (DefineSoundTag) characterTag;
        }

        if (characterTag != null) {
            logger.log(Level.SEVERE, "CharacterTag should be a DefineSoundTag. characterId: {0}", soundId);
        }

        return null;
    }

    /**
     * Gets TextTag by textId (= characterId). Logs a SEVERE message when the
     * charter found, but is not a TextTag.
     *
     * @param textId Text id
     * @return TextTag or null when not found or the character is not an TextTag
     */
    public TextTag getText(int textId) {
        CharacterTag characterTag = getCharacters(true).get(textId);
        if (characterTag instanceof TextTag) {
            return (TextTag) characterTag;
        }

        if (characterTag != null) {
            logger.log(Level.SEVERE, "CharacterTag should be a TextTag. characterId: {0}", textId);
        }

        return null;
    }

    /**
     * Gets list of all ABC container tags in this SWF.
     *
     * @return List of ABCContainerTag
     */
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

    /**
     * Checks whether this SWF is AS3. The information is coming from
     * FileAttributesTag and its actionscript3 flag.
     *
     * @return True if AS3, false otherwise
     */
    public boolean isAS3() {
        FileAttributesTag fileAttributes = getFileAttributes();
        return (fileAttributes != null && fileAttributes.actionScript3) || (fileAttributes == null && !getAbcList().isEmpty());
    }

    /**
     * Gets (first) MetadataTag.
     *
     * @return MetadataTag or null when not found
     */
    public MetadataTag getMetadata() {
        for (Tag t : getTags()) {
            if (t instanceof MetadataTag) {
                return (MetadataTag) t;
            }
        }

        return null;
    }

    /**
     * Gets (first) FileAttributesTag.
     *
     * @return FileAttributesTag or null when not found
     */
    public FileAttributesTag getFileAttributes() {
        for (Tag t : getTags()) {
            if (t instanceof FileAttributesTag) {
                return (FileAttributesTag) t;
            }
        }

        return null;
    }

    /**
     * Gets (first) SetBackgroundColorTag.
     *
     * @return SetBackgroundColorTag or null when not found
     */
    public SetBackgroundColorTag getBackgroundColor() {
        for (Tag t : getTags()) {
            if (t instanceof SetBackgroundColorTag) {
                return (SetBackgroundColorTag) t;
            }
        }

        return null;
    }

    /**
     * Gets (first) EnableTelemetryTag.
     *
     * @return EnableTelemetryTag or null when not found
     */
    public EnableTelemetryTag getEnableTelemetry() {
        for (Tag t : getTags()) {
            if (t instanceof EnableTelemetryTag) {
                return (EnableTelemetryTag) t;
            }
        }
        return null;
    }

    /**
     * Gets next available free characterId to use.
     *
     * @return New characterId
     */
    public int getNextCharacterId() {
        int max = 0;
        Set<Integer> ids = new HashSet<>(getCharacters(false).keySet());
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

    /**
     * Gets (first) JPEGTablesTag.
     *
     * @return JPEGTablesTag or null when not found
     */
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

    /**
     * Gets AS3 document class name. The information is taken from the
     * SymbolClass tag for id = 0.s
     *
     * @return Document class or null when no document class assigned
     */
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

    /**
     * Resets all timelines of the specified timelined object.
     *
     * @param timelined Timelined object
     */
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

    /**
     * Walks all tags lin the list and searches for characterTags,
     * characterIdTags and DefineExternalImage2
     *
     * @param list List of tags
     * @param externalImages2 Map of imageId to DefineExternalImage2
     * @param characters Map of characterId to CharacterTag
     * @param characterIdTags Map of characterId to list of CharacterIdTags
     */
    private synchronized void parseCharacters(Iterable<Tag> list, Map<Integer, DefineExternalImage2> externalImages2, Map<Integer, CharacterTag> characters, Map<Integer, List<CharacterIdTag>> characterIdTags) {
        Iterator<Tag> iterator = list.iterator();
        while (iterator.hasNext()) {
            Tag t = iterator.next();

            if (t instanceof DefineExternalImage2) {
                DefineExternalImage2 ei2 = (DefineExternalImage2) t;
                externalImages2.put(ei2.imageID, ei2);
            }

            if (t instanceof CharacterIdTag) {
                int characterId = ((CharacterIdTag) t).getCharacterId();
                if (characterId != -1) {
                    if (t instanceof CharacterTag) {
                        if (characters.containsKey(characterId)) {
                            CharacterTag ct = (CharacterTag) t;
                            CharacterTag oldCt = characters.get(characterId);
                            logger.log(Level.SEVERE, "SWF already contains characterId={0} of type {1}, tried to add type {2}", new Object[]{characterId, oldCt.getTagName(), ct.getTagName()});
                        }

                        characters.put(characterId, (CharacterTag) t);
                        characterIdTags.put(characterId, new ArrayList<>());
                    } else if (characterIdTags.containsKey(characterId)) {
                        characterIdTags.get(characterId).add((CharacterIdTag) t);
                    }
                }
            }

            if (t instanceof DefineSpriteTag) {
                parseCharacters(((DefineSpriteTag) t).getTags(), externalImages2, characters, characterIdTags);
            }
        }
    }

    /**
     * Marks recursive sprites as unresolved.
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

    /**
     * Checks whether a sprite is not recursive and thus valid.
     *
     * @param sprite Sprite to check
     * @param path Already processed character ids
     * @return True if valid, false otherwise
     */
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

    /**
     * Gets timeline.
     *
     * @return Timeline
     */
    @Override
    public Timeline getTimeline() {
        if (timeline == null) {
            timeline = new Timeline(this);
        }
        return timeline;
    }

    /**
     * Resets timeline.
     */
    @Override
    public void resetTimeline() {
        if (timeline != null) {
            timeline.reset(this);
        }
    }

    /**
     * Gets all tags with specified type id.
     *
     * @param tagId Identifier of tag type
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
     * Saves this SWF into OutputStream.
     *
     * @param os OutputStream
     */
    @Override
    public void saveTo(OutputStream os) throws IOException {
        saveTo(os, gfx, false);
    }

    /**
     * Saves this SWF into OutputStream with gfx and includeImported option.
     *
     * @param os OutputStream
     * @param gfx GFX
     * @param includeImported Include imported characters
     * @throws IOException On I/O error
     */
    public void saveTo(OutputStream os, boolean gfx, boolean includeImported) throws IOException {
        checkCharset();
        byte[] newUncompressedData = saveToByteArray(gfx, includeImported);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        compress(new ByteArrayInputStream(newUncompressedData), baos, compression, lzmaProperties);
        byte[] newCompressedData = baos.toByteArray();
        if (encrypted) {
            encrypt(new ByteArrayInputStream(newCompressedData), os);
        } else {
            os.write(newCompressedData);
        }
    }

    /**
     * Save nested DefineBinaryData tags. Walks nested opened SWFs in
     * DefineBinaryData and saves the modified contents-
     */
    public void saveNestedDefineBinaryData() {
        Map<Integer, CharacterTag> chtags = getCharacters(false);
        for (CharacterTag t : chtags.values()) {
            if (t instanceof DefineBinaryDataTag) {
                DefineBinaryDataTag dbd = (DefineBinaryDataTag) t;
                if (dbd.innerSwf != null && dbd.innerSwf.isModified()) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    try {
                        SWF swf = (SWF) dbd.innerSwf;
                        swf.saveNestedDefineBinaryData();
                        swf.saveTo(baos);
                        byte[] data = baos.toByteArray();
                        swf.binaryData.setDataBytes(new ByteArrayRange(data));
                        swf.binaryData.setModified(true);
                        swf.binaryData.getTopLevelBinaryData().pack();
                        dbd.innerSwf.clearModified();
                    } catch (IOException ex) {
                        Logger.getLogger(SWF.class.getName()).log(Level.SEVERE, "Cannot save SWF", ex);
                    }
                    dbd.setModified(true);
                }
            }
        }
    }

    /**
     * Gets bytes of header.
     *
     * @return Header bytes
     */
    public byte[] getHeaderBytes() {
        return getHeaderBytes(compression, gfx, encrypted);
    }

    /**
     * Gets specific header bytes.
     *
     * @param compression Compression
     * @param gfx GFX
     * @return Header bytes
     */
    private static byte[] getHeaderBytes(SWFCompression compression, boolean gfx) {
        return getHeaderBytes(compression, gfx, false);
    }

    /**
     * Gets specific header bytes with encrypted option.
     *
     * @param compression Compression
     * @param gfx GFX
     * @param encrypted Enable Harman encryption?
     * @return Header bytes
     */
    private static byte[] getHeaderBytes(SWFCompression compression, boolean gfx, boolean encrypted) {
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

        if (!gfx && encrypted) {
            ret[0] += 32; //to lowercase
        }

        return ret;
    }

    /**
     * Checks version of SWF and if 6 or later, sets charset to UTF-8.
     */
    private void checkCharset() {
        if (version > 5) {
            charset = Utf8Helper.charsetName;
        }
    }

    /**
     * Save file to byte array.
     *
     * @param includeImported Include imported characters
     * @return Byte array
     * @throws IOException On I/O error
     */
    private byte[] saveToByteArray(boolean includeImported) throws IOException {
        return saveToByteArray(gfx, includeImported);
    }

    /**
     * Save file to byte array with GFX option.
     *
     * @param gfx GFX
     * @param includeImported Include imported characters
     * @return Byte array
     * @throws IOException On I/O error
     */
    private byte[] saveToByteArray(boolean gfx, boolean includeImported) throws IOException {
        byte[] data;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); SWFOutputStream sos = new SWFOutputStream(baos, version, charset)) {
            sos.write(getHeaderBytes(SWFCompression.NONE, gfx));
            sos.writeUI8(version);
            sos.writeUI32(0); // placeholder for file length
            sos.writeRECT(displayRect);
            sos.writeFIXED8(frameRate);
            sos.writeUI16(frameCount);

            sos.writeTags(getTags());
            if (hasEndTag) {
                sos.writeUI16(0);
            }

            data = baos.toByteArray();
        }

        // update file size
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); SWFOutputStream sos = new SWFOutputStream(baos, version, charset)) {
            sos.writeUI32(data.length);
            byte[] lengthData = baos.toByteArray();
            System.arraycopy(lengthData, 0, data, 4, lengthData.length);
        }

        return data;
    }

    /**
     * Checks whether SWF is modified. First checks its internal modified flag,
     * if its not, then walks all tags and checks their modified flag.
     *
     * @return True if modified, false otherwise
     */
    @Override
    public boolean isModified() {
        if (isModified) {
            return true;
        }

        if (headerModified) {
            return true;
        }

        for (Tag tag : getTags()) {
            if (tag.isModified() && !tag.isReadOnly()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sets modified flag.
     *
     * @param value Value to set
     */
    @Override
    public void setModified(boolean value) {
        isModified = value;
    }

    /**
     * Clears modified flag for the SWF and its tags.
     */
    @Override
    public void clearModified() {
        for (Tag tag : getTags()) {
            if (tag.isModified()) {
                tag.createOriginalData();
                tag.setModified(false);
            }
        }

        headerModified = false;
        isModified = false;

        try {
            uncompressedData = saveToByteArray(false);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Cannot save SWF", ex);
        }
    }

    /**
     * Constructs an empty SWF.
     */
    public SWF() {
        version = SWF.DEFAULT_VERSION;
        displayRect = new RECT(0, 1, 0, 1);
        dumpInfo = new DumpInfoSwfNode(this, "rootswf", "", null, 0, 0);
    }

    /**
     * Constructs a SWF with specified charset.
     *
     * @param charset Charset for SWFs with version 5 or lower (they do not use
     * unicode)
     */
    public SWF(String charset) {
        this();
        this.charset = charset;
    }

    /**
     * Constructs SWF from stream.
     *
     * @param is Stream to read SWF from
     * @param parallelRead Use parallel threads?
     * @throws IOException On I/O error
     * @throws InterruptedException On interrupt
     */
    public SWF(InputStream is, boolean parallelRead) throws IOException, InterruptedException {
        this(is, null, null, null, parallelRead, false, true);
    }

    /**
     * Constructs SWF from stream.
     *
     * @param is Stream to read SWF from
     * @param parallelRead Use parallel threads?
     * @param charset Charset for SWFs with version 5 or lower (they do not use
     * unicode)
     * @throws IOException On I/O error
     * @throws InterruptedException On interrupt
     */
    public SWF(InputStream is, boolean parallelRead, String charset) throws IOException, InterruptedException {
        this(is, null, null, null, parallelRead, false, true, charset);
    }

    /**
     * Constructs SWF from stream.
     *
     * @param is Stream to read SWF from
     * @param parallelRead Use parallel threads?
     * @param lazy Do not parse all data, load it as necessary.
     * @throws IOException On I/O error
     * @throws InterruptedException On interrupt
     */
    public SWF(InputStream is, boolean parallelRead, boolean lazy) throws IOException, InterruptedException {
        this(is, null, null, null, parallelRead, false, lazy);
    }

    /**
     * Constructs SWF from stream.
     *
     * @param is Stream to read SWF from
     * @param parallelRead Use parallel threads?
     * @param lazy Do not parse all data, load it as necessary.
     * @param charset Charset for SWFs with version 5 or lower (they do not use
     * unicode)
     * @throws IOException On I/O error
     * @throws InterruptedException On interrupt
     */
    public SWF(InputStream is, boolean parallelRead, boolean lazy, String charset) throws IOException, InterruptedException {
        this(is, null, null, null, parallelRead, false, lazy, charset);
    }

    /**
     * Constructs SWF from stream.
     *
     * @param is Stream to read SWF from
     * @param file Path to the file
     * @param fileTitle Title of the SWF
     * @param parallelRead Use parallel threads?
     * @throws IOException On I/O error
     * @throws InterruptedException On interrupt
     */
    public SWF(InputStream is, String file, String fileTitle, boolean parallelRead) throws IOException, InterruptedException {
        this(is, file, fileTitle, null, parallelRead, false, true);
    }

    /**
     * Constructs SWF from stream.
     *
     * @param is Stream to read SWF from
     * @param file Path to the file
     * @param fileTitle Title of the SWF
     * @param parallelRead Use parallel threads?
     * @param charset Charset for SWFs with version 5 or lower (they do not use
     * unicode)
     * @throws IOException On I/O error
     * @throws InterruptedException On interrupt
     */
    public SWF(InputStream is, String file, String fileTitle, boolean parallelRead, String charset) throws IOException, InterruptedException {
        this(is, file, fileTitle, null, parallelRead, false, true, charset);
    }

    /**
     * Constructs SWF from stream.
     *
     * @param is Stream to read SWF from
     * @param listener Progress listener
     * @param parallelRead Use parallel threads?
     */
    public SWF(InputStream is, ProgressListener listener, boolean parallelRead) throws IOException, InterruptedException {
        this(is, null, null, listener, parallelRead, false, true);
    }

    /**
     * Constructs SWF from stream.
     *
     * @param is Stream to read SWF from
     * @param listener Progress listener
     * @param parallelRead Use parallel threads?
     * @param charset Charset for SWFs with version 5 or lower (they do not use
     * unicode)
     * @throws IOException On I/O error
     * @throws InterruptedException On interrupt
     */
    public SWF(InputStream is, ProgressListener listener, boolean parallelRead, String charset) throws IOException, InterruptedException {
        this(is, null, null, listener, parallelRead, false, true, charset);
    }

    /**
     * Constructs SWF from stream.
     *
     * @param is Stream to read SWF from
     * @param file Path to the file
     * @param fileTitle Title of the SWF
     * @param listener Progress listener
     * @param parallelRead Use parallel threads?
     * @throws IOException On I/O error
     * @throws InterruptedException On interrupt
     */
    public SWF(InputStream is, String file, String fileTitle, ProgressListener listener, boolean parallelRead) throws IOException, InterruptedException {
        this(is, file, fileTitle, listener, parallelRead, false, true);
    }

    /**
     * Constructs SWF from stream.
     *
     * @param is Stream to read SWF from
     * @param file Path to the file
     * @param fileTitle Title of the SWF
     * @param listener Progress listener
     * @param parallelRead Use parallel threads?
     * @param charset Charset for SWFs with version 5 or lower (they do not use
     * unicode)
     * @throws IOException On I/O error
     * @throws InterruptedException On interrupt
     */
    public SWF(InputStream is, String file, String fileTitle, ProgressListener listener, boolean parallelRead, String charset) throws IOException, InterruptedException {
        this(is, file, fileTitle, listener, parallelRead, false, true, charset);
    }

    /**
     * Constructs SWF from stream - Faster constructor to check SWF only.
     *
     * @param is Stream to read SWF from
     * @throws IOException On I/O error
     */
    public SWF(InputStream is) throws IOException {
        decompress(is, new NulStream(), true);
    }

    /**
     * Constructs SWF from stream.
     *
     * @param is Stream to read SWF from
     * @param file Path to the file
     * @param fileTitle Title of the SWF
     * @param listener Progress listener
     * @param parallelRead Use parallel threads?
     * @param checkOnly Check only file validity
     * @param lazy Do not parse all data, load it as necessary.
     * @param charset Charset for SWFs with version 5 or lower (they do not use
     * unicode)
     * @throws IOException On I/O error
     * @throws InterruptedException On interrupt
     */
    public SWF(InputStream is, String file, String fileTitle, ProgressListener listener, boolean parallelRead, boolean checkOnly, boolean lazy, String charset) throws IOException, InterruptedException {
        this(is, file, fileTitle, listener, parallelRead, checkOnly, lazy, null, charset, true);
    }

    /**
     * Constructs SWF from stream.
     *
     * @param is Stream to read SWF from
     * @param file Path to the file
     * @param fileTitle Title of the SWF
     * @param listener Progress listener
     * @param parallelRead Use parallel threads?
     * @param checkOnly Check only file validity
     * @param lazy Do not parse all data, load it as necessary.
     * @throws IOException On I/O error
     * @throws InterruptedException On interrupt
     */
    public SWF(InputStream is, String file, String fileTitle, ProgressListener listener, boolean parallelRead, boolean checkOnly, boolean lazy) throws IOException, InterruptedException {
        this(is, file, fileTitle, listener, parallelRead, checkOnly, lazy, null, Charset.defaultCharset().name(), true);
    }

    /**
     * Constructs SWF from stream.
     *
     * @param is Stream to read SWF from
     * @param file Path to the file
     * @param fileTitle Title of the SWF
     * @param listener Progress listener
     * @param parallelRead Use parallel threads?
     * @param checkOnly Check only file validity
     * @param lazy Do not parse all data, load it as necessary.
     * @param resolver URL resolver for importAssets/2 tags
     * @throws IOException On I/O error
     * @throws InterruptedException On interrupt
     */
    public SWF(InputStream is, String file, String fileTitle, ProgressListener listener, boolean parallelRead, boolean checkOnly, boolean lazy, UrlResolver resolver) throws IOException, InterruptedException {
        this(is, file, fileTitle, listener, parallelRead, checkOnly, lazy, resolver, Charset.defaultCharset().name(), true);
    }

    /**
     * Constructs SWF from stream.
     *
     * @param is Stream to read SWF from
     * @param file Path to the file
     * @param fileTitle Title of the SWF
     * @param listener Progress listener
     * @param parallelRead Use parallel threads?
     * @param checkOnly Only check file, do not parse
     * @param lazy Do not parse all data, load it as necessary.
     * @param resolver URL resolver for importAssets/2 tags
     * @param charset Charset for SWFs with version 5 or lower (they do not use
     * unicode)
     * @throws IOException On I/O error
     * @throws InterruptedException On interrupt
     */
    public SWF(InputStream is, String file, String fileTitle, ProgressListener listener, boolean parallelRead, boolean checkOnly, boolean lazy, UrlResolver resolver, String charset) throws IOException, InterruptedException {
        this(is, file, fileTitle, listener, parallelRead, checkOnly, lazy, resolver, charset, true);
    }

    /**
     * Constructs SWF from stream.
     *
     * @param is Stream to read SWF from
     * @param file Path to the file
     * @param fileTitle Title of the SWF
     * @param listener Progress listener
     * @param parallelRead Use parallel threads?
     * @param checkOnly Check only file validity
     * @param lazy Do not parse all data, load it as necessary.
     * @param resolver URL resolver for importAssets/2 tags
     * @param charset Charset for SWFs with version 5 or lower (they do not use
     * unicode)
     * @param allowRenameIdentifiers Allow auto renaming identifiers when
     * enabled
     */
    public SWF(InputStream is, String file, String fileTitle, ProgressListener listener, boolean parallelRead, boolean checkOnly, boolean lazy, UrlResolver resolver, String charset, boolean allowRenameIdentifiers) throws IOException, InterruptedException {
        this.file = file;
        this.fileTitle = fileTitle;
        this.charset = charset;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SWFHeader header = decompress(is, baos, true);
        gfx = header.gfx;
        encrypted = header.encrypted;
        compression = header.compression;
        lzmaProperties = header.lzmaProperties;
        uncompressedData = baos.toByteArray();
        originalUncompressedData = uncompressedData;

        SWFInputStream sis = new SWFInputStream(this, uncompressedData);
        dumpInfo = new DumpInfoSwfNode(this, "rootswf", "", null, 0, 0);
        sis.dumpInfo = dumpInfo;
        sis.skipBytesEx(3, "signature"); // skip signature
        version = sis.readUI8("version");

        if (version > 5) {
            this.charset = Utf8Helper.charsetName;
        }

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
            if (resolver != null) {
                resolveImported(resolver);
            }
            assignExportNamesToSymbols();
            assignClassesToSymbols();
            if (Configuration.autoLoadEmbeddedSwfs.get()) {
                loadAllEmbeddedSwfs();
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

        if (allowRenameIdentifiers && Configuration.autoRenameIdentifiers.get()) {
            if (listener != null) {
                listener.status("renaming.identifiers");
            }
            deobfuscateIdentifiers(RenameType.TYPENUMBER);
            AbcMultiNameCollisionFixer collisionFixer = new AbcMultiNameCollisionFixer();
            collisionFixer.fixCollisions(this);
            assignClassesToSymbols();
            clearScriptCache();
        }

        getASMs(true); // Add scriptNames to ASMs                     
    }

    /**
     * Loads all SWFs embedded in DefineBinaryData tags
     */
    private void loadAllEmbeddedSwfs() {
        for (Tag t : getTags()) {
            if (t instanceof DefineBinaryDataTag) {
                DefineBinaryDataTag binaryData = (DefineBinaryDataTag) t;
                binaryData.loadEmbeddedSwf();
            }
        }
    }

    /**
     * Resolve importAssets/2 tags. Loads the external files using URL resolver.
     * Calculates all characters.
     *
     * @param resolver URL resolver
     */
    private synchronized void resolveImported(UrlResolver resolver) {
        Map<String, SWF> importedSwfs = new HashMap<>();
        for (int p = 0; p < tags.size(); p++) {
            Tag t = tags.get(p);
            if (t instanceof ImportTag) {
                ImportTag importTag = (ImportTag) t;

                String url = importTag.getUrl();

                SWF iSwf;
                if (importedSwfs.containsKey(url)) {
                    iSwf = importedSwfs.get(url);
                } else {
                    iSwf = resolver.resolveUrl(url);
                    importedSwfs.put(url, iSwf);
                }
                if (iSwf != null) {

                    Map<Integer, String> importedIdToNameMap = importTag.getAssets();

                    Map<String, Integer> exportedNameToIdsMap = new HashMap<>();

                    for (Tag t2 : iSwf.tags) {
                        if (t2 instanceof ExportAssetsTag) {
                            ExportAssetsTag sc = (ExportAssetsTag) t2;
                            for (int i = 0; i < sc.names.size(); i++) {
                                exportedNameToIdsMap.put(sc.names.get(i), sc.tags.get(i));
                            }
                        }
                        if (t2 instanceof SymbolClassTag) {
                            SymbolClassTag sc = (SymbolClassTag) t2;
                            for (int i = 0; i < sc.names.size(); i++) {
                                importedClassToCharacter.put(sc.names.get(i), iSwf.getCharacter(sc.tags.get(i)));
                                importedClassSourceUrls.put(sc.names.get(i), url);
                            }
                        }
                    }

                    for (int importedId : importedIdToNameMap.keySet()) {
                        String importedName = importedIdToNameMap.get(importedId);
                        if (exportedNameToIdsMap.containsKey(importedName)) {
                            int exportedId = exportedNameToIdsMap.get(importedName);
                            if (iSwf.getCharacter(exportedId) == null) {
                                logger.log(Level.WARNING, "Imported character from URL {0} not found: exported id = {1}, exported name = {2}, imported id = {3}", new Object[]{url, exportedId, importedName, importedId});
                                continue;
                            }
                            importedCharacterSourceSwfs.put(importedId, iSwf);
                            importedCharacterIds.put(importedId, exportedId);
                            importedNameToCharacter.put(importedName, importedId);
                        } else {
                            logger.log(Level.WARNING, "Imported character from URL {0} not found: imported name = {1}, imported id = {2}", new Object[]{url, importedName, importedId});
                        }
                    }
                }
            }
        }
        updateCharacters();
    }

    /**
     * Gets openable. (Self for SWF)
     *
     * @return SWF
     */
    @Override
    public SWF getOpenable() {
        return this;
    }

    /**
     * Gets SWF that is root of the DefineBinaryData tag chain.
     *
     * @return Root SWF
     */
    public SWF getRootSwf() {
        SWF result = this;
        while (result.binaryData != null) {
            result = result.binaryData.getSwf();
        }

        return result;
    }

    /**
     * Gets SWF file.
     *
     * @return File or null
     */
    @Override
    public String getFile() {
        return file;
    }

    /**
     * Gets title of the file.
     *
     * @return file title or file when file title is null
     */
    @Override
    public String getFileTitle() {
        if (fileTitle != null) {
            return fileTitle;
        }
        return file;
    }

    /**
     * Gets title of the file or short filename.
     *
     * @return file title or base file name when file title is null or "_" when
     * file is null too
     */
    @Override
    public String getTitleOrShortFileName() {
        if (fileTitle != null) {
            return fileTitle;
        }
        if (file == null) {
            return "_";
        }
        return new File(file).getName();
    }

    /**
     * Gets short file name based on file title and file.
     *
     * @return Short file name
     */
    @Override
    public String getShortFileName() {
        return new File(getTitleOrShortFileName()).getName();
    }

    /**
     * Gets title of this SWF including parent nodes like SwfList and
     * DefineBinaryData.
     *
     * @return Title of the SWF
     */
    @Override
    public String getShortPathTitle() {
        if (binaryData != null) {
            return binaryData.getSwf().getShortPathTitle() + "/" + binaryData.getPathIdentifier();
        }
        if (openableList != null) {
            if (openableList.isBundle()) {
                return openableList.name + "/" + getTitleOrShortFileName();
            }
        }
        return getTitleOrShortFileName();
    }

    /**
     * Gets full path title of this SWF including parent nodes like SwfList and
     * DefineBinaryData.
     *
     * @return Full path title of the SWF
     */
    @Override
    public String getFullPathTitle() {
        if (binaryData != null) {
            return binaryData.getSwf().getFullPathTitle() + "/" + binaryData.getPathIdentifier();
        }
        if (openableList != null) {
            if (openableList.isBundle()) {
                return openableList.sourceInfo.getFileTitleOrName() + "/" + getFileTitle();
            }
        }
        return getFileTitle();
    }

    /**
     * Sets file.
     *
     * @param file File
     */
    @Override
    public void setFile(String file) {
        this.file = file;
        fileTitle = null;
    }

    /**
     * Gets file modification date.
     *
     * @return Modification date
     */
    public Date getFileModificationDate() {
        try {
            if (openableList != null && openableList.sourceInfo != null) {
                String fileName = openableList.sourceInfo.getFile();
                if (fileName != null) {
                    long lastModified = new File(fileName).lastModified();
                    if (lastModified > 0) {
                        return new Date(lastModified);
                    }
                }
            }
        } catch (SecurityException sex) {
            //ignored
        }

        return new Date();
    }

    /**
     * Gets all ABC container tags.
     *
     * @param list List of tags
     * @param actionScripts List of ABC container tags
     */
    private static void getAbcTags(Iterable<Tag> list, List<ABCContainerTag> actionScripts) {
        for (Tag t : list) {
            /*if (t instanceof DefineSpriteTag) {
                getAbcTags(((DefineSpriteTag) t).getTags(), actionScripts);
            }*/
            if (t instanceof ABCContainerTag) {
                actionScripts.add((ABCContainerTag) t);
            }
        }
    }

    /**
     * Assigns export names to symbols. Export name = the name assigned through
     * ExportAssets tag.
     */
    public void assignExportNamesToSymbols() {
        exportNameToCharacter.clear();
        HashMap<Integer, String> exportNames = new HashMap<>(importedTagToExportNameMapping);
        for (Tag t : getTags()) {
            if (t instanceof ExportAssetsTag) {
                ExportAssetsTag eat = (ExportAssetsTag) t;
                for (int i = 0; i < eat.tags.size(); i++) {
                    Integer tagId = eat.tags.get(i);
                    String name = eat.names.get(i);
                    if ((!exportNames.containsKey(tagId)) && (!exportNames.containsValue(name))) {
                        exportNames.put(tagId, name);
                        exportNameToCharacter.put(name, tagId);
                    }
                }
            }
        }
        for (Tag t : getTags()) {
            if (t instanceof CharacterTag) {
                CharacterTag ct = (CharacterTag) t;
                if (ct.getCharacterId() == -1) {
                    continue;
                }
                if (exportNames.containsKey(ct.getCharacterId())) {
                    ct.setExportName(exportNames.get(ct.getCharacterId()));
                } else {
                    ct.setExportName("");
                }
            }
        }
    }

    /**
     * Assigns class names to symbols. Class name is assigned through
     * SymbolClass tag.
     */
    public void assignClassesToSymbols() {
        HashMap<Integer, LinkedHashSet<String>> classes = new HashMap<>();

        for (int ch : importedTagToClassesMapping.keySet()) {
            classes.put(ch, new LinkedHashSet<>(importedTagToClassesMapping.get(ch)));
        }

        Set<String> uniqueClasses = new HashSet<>();
        for (Tag t : getTags()) {
            if (t instanceof SymbolClassTag) {
                SymbolClassTag sct = (SymbolClassTag) t;
                for (int i = 0; i < sct.tags.size(); i++) {
                    if (!classes.containsKey(sct.tags.get(i))) {
                        classes.put(sct.tags.get(i), new LinkedHashSet<>());
                    }
                    if (uniqueClasses.contains(sct.names.get(i))) {
                        //when two characters have assigned same class, only first assignment is valid                        
                        continue;
                    }
                    uniqueClasses.add(sct.names.get(i));
                    classes.get(sct.tags.get(i)).add(sct.names.get(i));
                }
            }
        }

        for (Tag t : getTags()) {
            if (t instanceof CharacterTag) {
                CharacterTag ct = (CharacterTag) t;
                if (ct.getCharacterId() == -1) {
                    continue;
                }
                if (classes.containsKey(ct.getCharacterId())) {
                    ct.setClassNames(classes.get((Integer) ct.getCharacterId()));
                } else {
                    ct.setClassNames(new LinkedHashSet<>());
                }
            }
        }

        classToCharacterId.clear();
        for (int ch : classes.keySet()) {
            for (String cls : classes.get(ch)) {
                classToCharacterId.put(cls, ch);
            }
        }
    }

    /**
     * Compresses SWF file
     *
     * @param is InputStream
     * @param os OutputStream
     * @param compression Compression
     * @param lzmaProperties LZMA properties
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
            // first decompress, then compress to the given format
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

        SWFOutputStream sos = new SWFOutputStream(os, version, Utf8Helper.charsetName);
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

    /**
     * Compresses SWF file.
     *
     * @param fis InputStream
     * @param fos OutputStream
     * @param compression Compression
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

    /**
     * Encrypts Harman AIR encryption
     *
     * @param is InputStream
     * @param os OutputStream
     * @return True on success
     * @throws IOException On I/O error
     */
    public static boolean encrypt(InputStream is, OutputStream os) throws IOException {
        byte[] hdr = new byte[8];

        // SWFheader: signature, version and fileSize
        if (is.read(hdr) != 8) {
            throw new SwfOpenException(AppResources.translate("error.swf.headerTooShort"));
        }

        decodeHeader(hdr);

        byte[] encrypted;
        try {
            encrypted = HarmanSwfEncrypt.encrypt(is, hdr);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
                | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException ex) {
            return false;
        }
        os.write(encrypted);
        return true;
    }

    /**
     * Decrypts Harman AIR encryption
     *
     * @param is InputStream
     * @param os OutputStream
     * @return True on success
     * @throws IOException On I/O error
     */
    public static boolean decrypt(InputStream is, OutputStream os) throws IOException {
        byte[] hdr = new byte[8];

        // SWFheader: signature, version and fileSize
        if (is.read(hdr) != 8) {
            throw new SwfOpenException(AppResources.translate("error.swf.headerTooShort"));
        }

        decodeHeader(hdr);

        switch (hdr[0]) {
            case 'c':
            case 'z':
            case 'f':
                try {
                    byte[] decrypted = HarmanSwfEncrypt.decrypt(is, hdr); //Note: this call will uppercase hdr[0]
                    os.write(hdr);
                    os.write(decrypted);
                    return true;
                } catch (IOException | InvalidAlgorithmParameterException | InvalidKeyException
                        | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException
                        | NoSuchPaddingException ex) {
                    throw new SwfOpenException(AppResources.translate("error.swf.decryptionProblem"));
                }
        }
        return false;
    }

    /**
     * Decodes LZMA stream.
     *
     * @param is InputStream
     * @param os OutputStream
     * @param lzmaProperties LZMA properties
     * @param fileSize File size
     * @throws IOException On I/O error
     */
    private static void decodeLZMAStream(InputStream is, OutputStream os, byte[] lzmaProperties, long fileSize) throws IOException {
        Decoder decoder = new Decoder();
        if (!decoder.SetDecoderProperties(lzmaProperties)) {
            throw new IOException("LZMA:Incorrect stream properties");
        }
        if (!decoder.Code(is, os, fileSize - 8)) {
            throw new IOException("LZMA:Error in data stream");
        }
    }

    /**
     * Decodes SWF header.
     *
     * @param headerData First 8 bytes of the file
     * @return SWF header
     * @throws IOException On I/O error
     */
    public static SWFHeader decodeHeader(byte[] headerData) throws IOException {
        String signature = new String(headerData, 0, 3, Utf8Helper.charset);
        if (!swfSignatures.contains(signature)) {
            throw new SwfOpenException(AppResources.translate("error.swf.invalid"));
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

    /**
     * Decompresses SWF file.
     *
     * @param fis InputStream
     * @param fos OutputStream
     * @return True on success
     */
    public static boolean decompress(InputStream fis, OutputStream fos) {
        try {
            decompress(fis, fos, false);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    /**
     * Decompresses SWF file
     *
     * @param is InputStream
     * @param os OutputStream
     * @param allowUncompressed When true, it will fail when file is not
     * compressed.
     * @return SWF header
     * @throws IOException On I/O error
     */
    private static SWFHeader decompress(InputStream is, OutputStream os, boolean allowUncompressed) throws IOException {

        byte[] hdr = new byte[8];

        // SWFheader: signature, version and fileSize
        if (is.read(hdr) != 8) {
            throw new SwfOpenException(AppResources.translate("error.swf.headerTooShort"));
        }

        SWFHeader header = decodeHeader(hdr);
        long fileSize = header.fileSize;

        try (SWFOutputStream sos = new SWFOutputStream(os, header.version, Utf8Helper.charsetName)) {
            sos.write(getHeaderBytes(SWFCompression.NONE, header.gfx));
            sos.writeUI8(header.version);
            sos.writeUI32(fileSize);

            switch (hdr[0]) {
                case 'c':
                case 'z':
                case 'f':
                    header.encrypted = true;
                    byte[] decrypted;
                    try {
                        decrypted = HarmanSwfEncrypt.decrypt(is, hdr);
                    } catch (IOException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException
                            | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException ex) {
                        throw new SwfOpenException(AppResources.translate("error.swf.decryptionProblem"));
                    }
                    is = new ByteArrayInputStream(decrypted);
                    break;

            }

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
                        //In old versions of GFX format (I saw it in 1.02), the fileSize field 
                        // does not contain size of header (signature + version + filesize = 8 bytes)
                        if (header.gfx && is.available() + 8 > fileSize) {
                            final InputStream fis = is;

                            //pass to outputstream all we read
                            InputStream copyIs = new InputStream() {
                                @Override
                                public int read() throws IOException {
                                    int value = fis.read();
                                    os.write(value);
                                    return value;
                                }
                            };
                            //Use special constructor to pass InputStream
                            SWFInputStream sis = new SWFInputStream(copyIs);
                            sis.readRECT("displayRect");
                            sis.readFIXED8("frameRate");
                            sis.readUI16("frameCount");
                            int tagIDTagLength = sis.readUI16("tagIDTagLength");
                            long tagLength = (tagIDTagLength & 0x003F);
                            if (tagLength == 0x3f) {
                                sis.readSI32("tagLength");
                            }
                            int tagID = (tagIDTagLength) >> 6;
                            if (tagID == ExporterInfo.ID) {
                                int exporterVersion = sis.readUI16("exporterInfo");
                                if (exporterVersion < 0x200) { //assuming version 2 corrected this
                                    Helper.copyStream(is, os, fileSize - sis.getPos());
                                } else {
                                    Helper.copyStream(is, os, fileSize - 8 - sis.getPos());
                                }
                            } else {
                                Helper.copyStream(is, os, fileSize - 8 - sis.getPos());
                            }
                        } else {
                            Helper.copyStream(is, os, fileSize - 8);
                        }
                    } else {
                        throw new IOException("SWF is not compressed");
                    }
                }
            }

            return header;
        }
    }

    /**
     * Rename invalid identifiers.
     *
     * @param renameType Rename type
     * @param fis InputStream
     * @param fos OutputStream
     * @return True on success
     */
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

    /**
     * Gets ScriptPacks for specified class names.
     *
     * @param classNames List of class names
     * @return List of ScriptPacks
     * @throws Exception On error
     */
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

    /**
     * Makes scriptpacks unique. Unique = no two packs with same classpath
     * exist.
     *
     * @param packs List of ScriptPacks
     * @return List of unique ScriptPacks
     */
    private List<ScriptPack> uniqueAS3Packs(List<ScriptPack> packs) {
        List<ScriptPack> ret = new ArrayList<>();
        Set<ClassPath> classPaths = new HashSet<>();
        for (ScriptPack item : packs) {
            ClassPath key = item.getClassPath();
            if (classPaths.contains(key) && item.isSimple) {
                logger.log(Level.SEVERE, "Duplicate pack path found ({0})!", key);
            } else {
                classPaths.add(key);
                ret.add(item);
            }
        }
        return ret;
    }

    /**
     * Gets AS3 ScriptPacks. ScriptPack = script or a part of script (for
     * compound scripts).
     *
     * @return List of ScriptPacks
     * @see ScriptPack
     */
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

    /**
     * Gets SWF display rect.
     *
     * @return Display RECT
     */
    @Override
    public RECT getRect() {
        return displayRect;
    }

    /**
     * Gets display RECT with added tags.
     *
     * @param added Set of added tags
     * @return Display RECT
     */
    @Override
    public RECT getRect(Set<BoundedTag> added) {
        return displayRect;
    }

    /**
     * Gets listener for Export events
     *
     * @return Event listener
     */
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

    /**
     * Exports ActionScript.
     *
     * @param handler Handler for I/O fails
     * @param outdir Output directory
     * @param exportSettings Export settings
     * @param parallel Use parallel threads?
     * @param evl Event listener
     * @return List of exported files
     * @throws IOException On I/O error
     */
    public List<File> exportActionScript(AbortRetryIgnoreHandler handler, String outdir, ScriptExportSettings exportSettings, boolean parallel, EventListener evl) throws IOException {
        return exportActionScript(handler, outdir, null, exportSettings, parallel, evl, true, true);
    }

    /**
     * Exports ActionScript.
     *
     * @param handler Handler for I/O fails
     * @param outdir Output directory
     * @param as3scripts List of AS3 ScriptPacks to export
     * @param exportSettings Export settings
     * @param parallel Use parallel threads?
     * @param evl Event listener
     * @param as2 Export AS1/2
     * @param as3 Export AS3
     * @return List of exported files
     * @throws IOException On I/O error
     */
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

    /**
     * Get ASMSources (AS1/2).
     *
     * @param exportFileNames Use export filenames
     * @return Map from path to ASMSource
     */
    public Map<String, ASMSource> getASMs(boolean exportFileNames) {
        return getASMs(exportFileNames, new ArrayList<>(), true);
    }

    /**
     * Get ASMSources (AS1/2).
     *
     * @param exportFileNames Use export filenames
     * @param nodesToExport Which tree nodes to export
     * @param exportAll Export everything
     * @return Map from path to ASMSource
     */
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
            getASMs(exportFileNames, treeItem, nodesToExport, exportAll, asmsToExport,
                    File.separator + getASMPath(true, treeItem),
                    File.separator + getASMPath(false, treeItem)
            );
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

    /**
     * Get ASMSources (AS1/2).
     *
     * @param exportFileNames Use export filenames
     * @param treeItem Current item
     * @param nodesToExport Which tree nodes to export
     * @param exportAll Export everything
     * @param asmsToExport Result
     * @param pathExportFilenames Path for exported filenames
     * @param pathNoExportFilenames Path for not exported filenames
     */
    private void getASMs(boolean exportFileNames, TreeItem treeItem, List<TreeItem> nodesToExport, boolean exportAll, Map<String, ASMSource> asmsToExport, String pathExportFilenames, String pathNoExportFilenames) {
        TreeItem realItem = treeItem instanceof TagScript ? ((TagScript) treeItem).getTag() : treeItem;
        boolean exportNode = nodesToExport.contains(treeItem) || nodesToExport.contains(realItem);

        if (realItem instanceof ASMSource && (exportAll || exportNode)) {
            String pathNoExportFilenames2 = pathNoExportFilenames;
            String pathExportFilenames2 = pathExportFilenames;
            String path = exportFileNames ? pathExportFilenames : pathNoExportFilenames;

            int ppos = 1;
            while (asmsToExport.containsKey(path)) {
                ppos++;
                pathNoExportFilenames2 = pathNoExportFilenames + "_" + ppos;
                pathExportFilenames2 = pathExportFilenames + "[" + ppos + "]";
                path = exportFileNames ? pathExportFilenames2 : pathNoExportFilenames2;
            }
            ((ASMSource) realItem).setScriptName(pathNoExportFilenames2);
            ((ASMSource) realItem).setExportedScriptName(pathExportFilenames2);
            asmsToExport.put(path, (ASMSource) realItem);
        }

        if (treeItem instanceof TagScript) {
            TagScript tagScript = (TagScript) treeItem;
            for (TreeItem subItem : tagScript.getFrames()) {
                getASMs(exportFileNames, subItem, nodesToExport, exportAll, asmsToExport,
                        pathExportFilenames + File.separator + getASMPath(true, subItem),
                        pathNoExportFilenames + File.separator + getASMPath(false, subItem)
                );
            }
        } else if (treeItem instanceof FrameScript) {
            FrameScript frameScript = (FrameScript) treeItem;
            Frame parentFrame = frameScript.getFrame();
            for (TreeItem subItem : parentFrame.actionContainers) {
                getASMs(exportFileNames, getASMWrapToTagScript(subItem), nodesToExport, exportAll || exportNode, asmsToExport,
                        pathExportFilenames + File.separator + getASMPath(true, subItem),
                        pathNoExportFilenames + File.separator + getASMPath(false, subItem)
                );
            }
            for (TreeItem subItem : parentFrame.actions) {
                getASMs(exportFileNames, getASMWrapToTagScript(subItem), nodesToExport, exportAll || exportNode, asmsToExport,
                        pathExportFilenames + File.separator + getASMPath(true, subItem),
                        pathNoExportFilenames + File.separator + getASMPath(false, subItem)
                );
            }
        } else if (treeItem instanceof AS2Package) {
            AS2Package as2Package = (AS2Package) treeItem;
            for (TreeItem subItem : as2Package.subPackages.values()) {
                if ((subItem instanceof AS2Package) && ((AS2Package) subItem).isDefaultPackage()) {
                    getASMs(exportFileNames, subItem, nodesToExport, exportAll, asmsToExport, pathExportFilenames, pathNoExportFilenames);
                } else {
                    getASMs(exportFileNames, subItem, nodesToExport, exportAll, asmsToExport,
                            pathExportFilenames + File.separator + getASMPath(true, subItem),
                            pathNoExportFilenames + File.separator + getASMPath(false, subItem)
                    );
                }
            }
            for (TreeItem subItem : as2Package.scripts.values()) {
                getASMs(exportFileNames, subItem, nodesToExport, exportAll, asmsToExport,
                        pathExportFilenames + File.separator + getASMPath(true, subItem),
                        pathNoExportFilenames + File.separator + getASMPath(false, subItem)
                );
            }
        }
    }

    /**
     * Gets path of ASMSource.
     *
     * @param exportFileName Use export filenames
     * @param treeItem TreeItem
     * @return Path
     */
    private String getASMPath(boolean exportFileName, TreeItem treeItem) {

        if (treeItem instanceof AS2Package) {
            AS2Package pkg = (AS2Package) treeItem;
            if (pkg.isFlat()) {
                String[] parts = pkg.toString().split("\\.");
                for (int i = 0; i < parts.length; i++) {
                    parts[i] = Helper.makeFileName(parts[i]);
                }
                return String.join(File.separator, parts);
            }
        }

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

    /**
     * Wrap ASMSource to tagscript.
     *
     * @param treeItem TreeItem
     * @return TagScript if script, otherwise treeItem
     */
    private TreeItem getASMWrapToTagScript(TreeItem treeItem) {
        if (treeItem instanceof Tag) {
            Tag resultTag = (Tag) treeItem;
            List<TreeItem> subNodes = new ArrayList<>();
            if (treeItem instanceof ASMSourceContainer) {
                for (ASMSource item : ((ASMSourceContainer) treeItem).getSubItems()) {
                    subNodes.add(item);
                }
            }

            TagScript tagScript = new TagScript((SWF) treeItem.getOpenable(), resultTag, subNodes);
            return tagScript;
        }

        return treeItem;
    }

    /**
     * Gets ASMSources of the first level.
     *
     * @param tagScriptCache Tag to tagscript cache
     * @return List of ASMSource
     */
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

    ;

    /**
     * Adds event listener.
     *
     * @param listener Listener
     */
    public final void addEventListener(EventListener listener) {
        listeners.add(listener);
        for (Tag t : getTags()) {
            if (t instanceof ABCContainerTag) {
                (((ABCContainerTag) t).getABC()).addEventListener(listener);
            }
        }
    }

    /**
     * Removes event listener.
     *
     * @param listener Listener
     */
    public final void removeEventListener(EventListener listener) {
        listeners.remove(listener);
        for (Tag t : getTags()) {
            if (t instanceof ABCContainerTag) {
                (((ABCContainerTag) t).getABC()).removeEventListener(listener);
            }
        }
    }

    /**
     * Informs all listeners registered on this SWF.
     *
     * @param event Event
     * @param data Data
     */
    public void informListeners(String event, Object data) {
        for (EventListener listener : listeners) {
            listener.handleEvent(event, data);
        }
    }

    /**
     * Gets all VideoFrameTags for specified streamId.
     *
     * @param streamId Stream ID
     * @param tags Input tags
     * @param output Output - map of frame number to VideoFrameTag.
     */
    public static void populateVideoFrames(int streamId, Iterable<Tag> tags, Map<Integer, VideoFrameTag> output) {
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

    /**
     * Write Little endian to stream
     *
     * @param os OutputStream
     * @param val Value
     * @param size Size
     * @throws IOException On I/O error
     */
    private static void writeLE(OutputStream os, long val, int size) throws IOException {
        for (int i = 0; i < size; i++) {
            os.write((int) (val & 0xff));
            val >>= 8;
        }
    }

    /**
     * Creates Wav file from PCM data.
     *
     * @param fos OutputStream
     * @param soundRateHz Sound rate in Hz
     * @param soundSize True = 16 bit, false = 8b bit.
     * @param soundType True = stereo, false = mono
     * @param data PCM data
     * @throws IOException On I/O error
     */
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

    /**
     * Gets prefix of type of specified character.
     *
     * @param characterTag Character tag
     * @return Type prefix
     */
    public static String getTypePrefix(CharacterTag characterTag) {
        if (characterTag instanceof ShapeTag) {
            return "shape";
        }
        if (characterTag instanceof MorphShapeTag) {
            return "morphshape";
        }
        if (characterTag instanceof DefineSpriteTag) {
            return "sprite";
        }
        if (characterTag instanceof TextTag) {
            return "text";
        }
        if (characterTag instanceof ButtonTag) {
            return "button";
        }
        if (characterTag instanceof FontTag) {
            return "font";
        }
        if (characterTag instanceof ImageTag) {
            return "image";
        }
        return "character";
    }

    /**
     * Converts set of characters to HTML canvas.
     *
     * @param fswf SWF
     * @param library Set of characterIds
     * @param fos OutputStream
     * @throws IOException On I/O error
     */
    public static void libraryToHtmlCanvas(SWF fswf, Set<Integer> library, OutputStream fos) throws IOException {
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
                    byte[] imageData = Helper.readStream(image.getConvertedImageData());
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
                DefineScalingGridTag scalingGrid = ch.getScalingGridTag();
                if (scalingGrid != null && (ch instanceof BoundedTag)) {
                    BoundedTag bt = (BoundedTag) ch;
                    RECT bounds = bt.getRect();
                    fos.write(Utf8Helper.getBytes("boundRects[\"" + (getTypePrefix(ch) + c) + "\"] = {"
                            + "xMin:" + bounds.Xmin + ","
                            + "xMax:" + bounds.Xmax + ","
                            + "yMin:" + bounds.Ymin + ","
                            + "yMax:" + bounds.Ymax
                            + "};\r\n\r\n"));
                    RECT grid = scalingGrid.splitter;
                    fos.write(Utf8Helper.getBytes("scalingGrids[\"" + (getTypePrefix(ch) + c) + "\"] = {"
                            + "xMin:" + grid.Xmin + ","
                            + "xMax:" + grid.Xmax + ","
                            + "yMin:" + grid.Ymin + ","
                            + "yMax:" + grid.Ymax
                            + "};\r\n\r\n"));
                }
            }
        }
    }

    /**
     * Gets variables from AS1/2 code.
     *
     * @param constantPool Constant pool
     * @param localData Local data
     * @param stack Stack
     * @param output Output
     * @param code Code
     * @param ip Instruction pointer
     * @param variables Variables
     * @param functions Functions
     * @param strings Strings
     * @param visited Visited
     * @param usageTypes Usage types
     * @param path Path
     * @throws InterruptedException On interrupt
     */
    private static void getVariables(ConstantPool constantPool, BaseLocalData localData, TranslateStack stack, List<GraphTargetItem> output, ActionGraphSource code, int ip, List<MyEntry<DirectValueActionItem, ConstantPool>> variables, List<GraphSourceItem> functions, HashMap<DirectValueActionItem, ConstantPool> strings, List<Integer> visited, HashMap<DirectValueActionItem, String> usageTypes, String path) throws InterruptedException {
        ActionLocalData aLocalData = (ActionLocalData) localData;
        boolean debugMode = false;
        while ((ip > -1) && ip < code.size()) {
            if (visited.contains(ip)) {
                break;
            }
            GraphSourceItem ins = code.get(ip);

            if (debugMode) {
                System.err.println("Visit " + ip + ": ofs" + Helper.formatAddress(((Action) ins).getAddress()) + ":" + ((Action) ins).getASMSource(new ActionList(code.getCharset()), new HashSet<>(), ScriptExportMode.PCODE) + " stack:" + Helper.stackToString(stack, LocalData.create(new ConstantPool())));
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
                    getVariables(aLocalData.insideDoInitAction, variables, functions, strings, usageTypes, new ActionGraphSource(path, aLocalData.insideDoInitAction, code.getActions().subList(ip, nextip), code.version, new HashMap<>(), new HashMap<>(), new HashMap<>(), code.getCharset()), 0, path + (cntName == null ? "" : "/" + cntName));
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
            int staticOperation = 0;

            int requiredStackSize = ins.getStackPopCount(localData, stack);
            if (stack.size() < requiredStackSize) {
                // probably obfuscated code, never executed branch
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

    /**
     * Gets variables from AS1/2 code.
     *
     * @param insideDoInitAction Is inside DoInitAction
     * @param variables Variables
     * @param functions Functions
     * @param strings Strings
     * @param usageTypes Usage types
     * @param code Code
     * @param addr Address
     * @param path Path
     * @throws InterruptedException On interrupt
     */
    private static void getVariables(boolean insideDoInitAction, List<MyEntry<DirectValueActionItem, ConstantPool>> variables, List<GraphSourceItem> functions, HashMap<DirectValueActionItem, ConstantPool> strings, HashMap<DirectValueActionItem, String> usageTypes, ActionGraphSource code, int addr, String path) throws InterruptedException {
        ActionLocalData localData = new ActionLocalData(null, insideDoInitAction, new HashMap<>() /*??*/);
        getVariables(null, localData, new TranslateStack(path), new ArrayList<>(), code, code.adr2pos(addr), variables, functions, strings, new ArrayList<>(), usageTypes, path);
    }

    /**
     * Gets variables from AS1/2 code.
     *
     * @param insideDefineFunction1 Is inside DefineFunction1
     * @param variables Variables
     * @param actionsMap Actions map
     * @param functions Functions
     * @param strings Strings
     * @param usageTypes Usage types
     * @param src Source
     * @param path Path
     * @return List of variables
     * @throws InterruptedException On interrupt
     */
    private List<MyEntry<DirectValueActionItem, ConstantPool>> getVariables(boolean insideDefineFunction1, List<MyEntry<DirectValueActionItem, ConstantPool>> variables, HashMap<ASMSource, ActionList> actionsMap, List<GraphSourceItem> functions, HashMap<DirectValueActionItem, ConstantPool> strings, HashMap<DirectValueActionItem, String> usageTypes, ASMSource src, String path) throws InterruptedException {
        List<MyEntry<DirectValueActionItem, ConstantPool>> ret = new ArrayList<>();
        ActionList actions = src.getActions();
        actionsMap.put(src, actions);
        boolean insideDoInitAction = src instanceof DoInitActionTag;
        getVariables(insideDoInitAction, variables, functions, strings, usageTypes, new ActionGraphSource(path, insideDoInitAction, actions, version, new HashMap<>(), new HashMap<>(), new HashMap<>(), src.getSwf().getCharset()), 0, path);
        return ret;
    }

    /**
     * Gets variables from AS1/2 code.
     *
     * @param insideDefineFunction1 Is inside DefineFunction1
     * @param tags Tags
     * @param path Path
     * @param variables Variables
     * @param actionsMap Actions map
     * @param functions Functions
     * @param strings Strings
     * @param usageTypes Usage types
     * @throws InterruptedException On interrupt
     */
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

    /**
     * Adds variable for AS1/2 getVariables.
     *
     * @param insideDefineFunction1 Is inside DefineFunction1
     * @param asm ASMSource
     * @param path Path
     * @param processed Processed
     * @param variables Variables
     * @param actionsMap Actions map
     * @param functions Functions
     * @param strings Strings
     * @param usageTypes Usage types
     * @throws InterruptedException On interrupt
     */
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

    /**
     * Checks whether AS3 string constant exists. Walks all ABC containers and
     * ABCs inside.
     *
     * @param str String
     * @return True if exists
     */
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

    /**
     * Fixes problems with ABC bodies. FIXME: Is this really needed?
     */
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

    /**
     * Deobfuscates AS3 identifiers.
     *
     * @param renameType Rename type
     * @return Number of changes
     */
    public int deobfuscateAS3Identifiers(RenameType renameType) {
        AbcIndexing ai = getAbcIndex();
        Map<Tag, Map<Integer, String>> stringUsageTypesMap = new HashMap<>();
        Map<Tag, Set<Integer>> stringUsagesMap = new HashMap<>();
        informListeners("deobfuscate", "Getting usages...");
        for (Tag tag : getTags()) {
            if (tag instanceof ABCContainerTag) {
                Map<Integer, String> stringUsageTypes = new HashMap<>();
                Set<Integer> stringUsages = ((ABCContainerTag) tag).getABC().getStringUsages();
                ((ABCContainerTag) tag).getABC().getStringUsageTypes(stringUsageTypes);
                stringUsageTypesMap.put(tag, stringUsageTypes);
                stringUsagesMap.put(tag, stringUsages);
            }
        }

        for (Tag tag : getTags()) {
            if (tag instanceof ABCContainerTag) {
                ((ABCContainerTag) tag).getABC().deobfuscateIdentifiers(stringUsageTypesMap.get(tag), stringUsagesMap.get(tag), deobfuscated, renameType, true);
                ((ABCContainerTag) tag).getABC().constants.clearCachedMultinames();
                ((ABCContainerTag) tag).getABC().constants.clearCachedDottedChains();
                tag.setModified(true);
            }
        }
        for (Tag tag : getTags()) {
            if (tag instanceof ABCContainerTag) {
                ((ABCContainerTag) tag).getABC().deobfuscateIdentifiers(stringUsageTypesMap.get(tag), stringUsagesMap.get(tag), deobfuscated, renameType, false);
                ((ABCContainerTag) tag).getABC().constants.clearCachedMultinames();
                ((ABCContainerTag) tag).getABC().constants.clearCachedDottedChains();
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

        for (Tag tag : getTags()) {
            if (tag instanceof ABCContainerTag) {
                ai.refreshAbc(((ABCContainerTag) tag).getABC());
            }
        }
        return deobfuscated.size();
    }

    /**
     * Deobfuscates identifiers.
     *
     * @param renameType Rename type
     * @return Number of changes
     * @throws InterruptedException On interrupt
     */
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

    /**
     * Renames AS2 identifier.
     *
     * @param identifier Identifier
     * @param newname New name
     * @throws InterruptedException On interrupt
     */
    public void renameAS2Identifier(String identifier, String newname) throws InterruptedException {
        Map<DottedChain, DottedChain> selected = new HashMap<>();
        selected.put(DottedChain.parseWithSuffix(identifier), DottedChain.parseWithSuffix(newname));
        renameAS2Identifiers(null, selected);
    }

    /**
     * Deobfuscates AS2 identifiers.
     *
     * @param renameType Rename type
     * @return Number of changes
     * @throws InterruptedException On interrupt
     */
    private int deobfuscateAS2Identifiers(RenameType renameType) throws InterruptedException {
        return renameAS2Identifiers(renameType, null);
    }

    /**
     * Renames AS2 identifiers.
     *
     * @param renameType Rename type
     * @param selected Preselected identifiers map. Can be null when no
     * preselected.
     * @return Number of changes
     * @throws InterruptedException On interrupt
     */
    private int renameAS2Identifiers(RenameType renameType, Map<DottedChain, DottedChain> selected) throws InterruptedException {
        boolean wrongConstantIndices = false;
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
                int staticOperation = 0;
                List<GraphTargetItem> dec;
                try {
                    dec = Action.actionsToTree(new HashMap<>() /*??*/, true /*Yes, inside doInitAction*/, false, dia.getActions(), version, staticOperation, ""/*FIXME*/, getCharset());
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
                if (pu.replacement.get(it.getKey().getPos()) instanceof ConstantIndex) {
                    ConstantIndex ci = (ConstantIndex) pu.replacement.get(it.getKey().getPos());
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
                        if (ci.index >= pool.constants.size()) {
                            wrongConstantIndices = true;
                        } else {
                            pool.constants.set(ci.index, changed);
                        }
                    }
                } else {
                    pu.replacement.set(it.getKey().getPos(), changed);
                }
                ret++;
            }
        }

        for (ASMSource src : actionsMap.keySet()) {
            actionsMap.get(src).removeNops();
            try {
                src.setActions(actionsMap.get(src));
            } catch (ValueTooLargeException vtle) {
                Logger.getLogger(SWF.class.getName()).log(Level.WARNING, "renaming AS2 identifiers failed for an action source with error: {0}", vtle.getMessage());
            }
            src.setModified();
        }

        deobfuscation.deobfuscateInstanceNames(false, deobfuscated, renameType, getTags(), selected);
        if (wrongConstantIndices) {
            logger.warning("Cannot properly rename some invalid AS2 identifiers as there exist unresolved constant indices. It might be fixed by turning Deobfuscation on and try to rename identifiers again.");
        }
        return ret;
    }

    /**
     * Gets IdentifiersDeobfuscation.
     *
     * @return IdentifiersDeobfuscation
     */
    public IdentifiersDeobfuscation getDeobfuscation() {
        return deobfuscation;
    }

    /**
     * Exports file to FLA.
     *
     * @param handler Handler for I/O fails
     * @param outfile Output file
     * @param swfName SWF name
     * @param generator Generator name
     * @param generatorVerName Generator name with version
     * @param generatorVersion Generator version
     * @param parallel Use parallel threads?
     * @param version FLA version
     * @param progressListener Progress listener
     * @throws IOException On I/O error
     * @throws InterruptedException On interrupt
     */
    public void exportFla(AbortRetryIgnoreHandler handler, String outfile, String swfName, String generator, String generatorVerName, String generatorVersion, boolean parallel, FLAVersion version, ProgressListener progressListener) throws IOException, InterruptedException {
        XFLExportSettings settings = new XFLExportSettings();
        settings.compressed = true;
        exportXfl(handler, outfile, swfName, generator, generatorVerName, generatorVersion, parallel, version, settings, progressListener);
    }

    /**
     * Exports file to uncompressed FLA (XFL).
     *
     * @param handler Handler for I/O fails
     * @param outfile Output file
     * @param swfName SWF name
     * @param generator Generator name
     * @param generatorVerName Generator name with version
     * @param generatorVersion Generator version
     * @param parallel Use parallel threads?
     * @param version FLA version
     * @param progressListener Progress listener
     * @throws IOException On I/O error
     * @throws InterruptedException On interrupt
     */
    public void exportXfl(AbortRetryIgnoreHandler handler, String outfile, String swfName, String generator, String generatorVerName, String generatorVersion, boolean parallel, FLAVersion version, ProgressListener progressListener) throws IOException, InterruptedException {
        XFLExportSettings settings = new XFLExportSettings();
        settings.compressed = false;
        exportXfl(handler, outfile, swfName, generator, generatorVerName, generatorVersion, parallel, version, settings, progressListener);
    }

    /**
     * Exports file to uncompressed FLA (XFL).
     *
     * @param handler Handler for I/O fails
     * @param outfile Output file
     * @param swfName SWF name
     * @param generator Generator name
     * @param generatorVerName Generator name with version
     * @param generatorVersion Generator version
     * @param parallel Use parallel threads?
     * @param version FLA version
     * @param settings Export settings
     * @param progressListener Progress listener
     * @throws IOException On I/O error
     * @throws InterruptedException On interrupt
     */
    public void exportXfl(AbortRetryIgnoreHandler handler, String outfile, String swfName, String generator, String generatorVerName, String generatorVersion, boolean parallel, FLAVersion version, XFLExportSettings settings, ProgressListener progressListener) throws IOException, InterruptedException {
        new XFLConverter().convertSWF(handler, this, swfName, outfile, settings, generator, generatorVerName, generatorVersion, parallel, version, progressListener);
        clearAllCache();
    }

    /**
     * Converts MATRIX to AffineTransform.
     *
     * @param mat Matrix
     * @return AffineTransform
     */
    public static AffineTransform matrixToTransform(MATRIX mat) {
        return new AffineTransform(mat.getScaleXFloat(), mat.getRotateSkew0Float(),
                mat.getRotateSkew1Float(), mat.getScaleYFloat(),
                mat.translateX, mat.translateY);
    }

    /**
     * Puts image to frame cache
     *
     * @param key Key
     * @param img Image
     */
    public void putToCache(String key, SerializableImage img) {
        if (Configuration.useFrameCache.get()) {
            frameCache.put(key, img);
        }
    }

    /**
     * Puts sound to sound cache
     *
     * @param soundInfo Sound info
     * @param soundTag Sound tag
     * @param resample Resample to 44kHz?
     * @param data Byte data
     */
    public void putToCache(SOUNDINFO soundInfo, SoundTag soundTag, boolean resample, byte[] data) {
        SoundInfoSoundCacheEntry key = new SoundInfoSoundCacheEntry(soundInfo, soundTag, resample);
        soundCache.put(key, data);
    }

    /**
     * Clears image cache. FrameCache, RectCache, JPEGTables and
     * DefineCompactedFont.shapecache is cleared.
     */
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

    /**
     * Clears shape cache.
     */
    public void clearShapeCache() {
        shapeExportDataCache.clear();
    }

    /**
     * Clears sound cache.
     */
    public void clearSoundCache() {
        soundCache.clear();
    }

    /**
     * Clears script cache.
     */
    public void clearScriptCache() {
        uninitializedAs2ClassTraits = null;
        as2Cache.clear();
        as3Cache.clear();
        List<ABCContainerTag> abcList = getAbcList();
        for (ABCContainerTag c : abcList) {
            c.getABC().clearPacksCache();
        }

        asmsCache = null;
        asmsCacheExportFilenames = null;
        IdentifiersDeobfuscation.clearCache();
    }

    /**
     * Clears (readonly)tags cache.
     */
    public void clearReadOnlyListCache() {
        readOnlyTags = null;
        for (Tag tag : tags) {
            if (tag instanceof DefineSpriteTag) {
                ((DefineSpriteTag) tag).clearReadOnlyListCache();
            }
        }
    }

    /**
     * Clears all static caches.
     */
    public static void clearAllStaticCache() {
        Cache.clearAll();
        Helper.clearShapeCache();
        System.gc();
    }

    /**
     * Clears ABC list cache.
     */
    public void clearAbcListCache() {
        abcList = null;
    }

    /**
     * Clears all caches.
     */
    public void clearAllCache() {
        characters = null;
        charactersWithImported = null;
        characterToId = null;
        characterIdTags = null;
        externalImages2 = null;
        timeline = null;
        cyclicCharacters = null;
        dependentCharacters = null;
        dependentFrames = null;
        clearReadOnlyListCache();
        clearImageCache();
        clearShapeCache();
        clearScriptCache();
        clearAbcListCache();
    }

    /**
     * Removes ASMSource from cache.
     *
     * @param src ASMSource
     */
    public static void uncache(ASMSource src) {
        if (src != null) {
            SWF swf = src.getSwf();
            if (swf != null) {
                swf.as2Cache.remove(src);
            }
        }
    }

    /**
     * Removes ScriptPack from cache.
     *
     * @param pack ScriptPack
     */
    public static void uncache(ScriptPack pack) {
        if (pack != null) {
            Openable openable = pack.getOpenable();
            if (openable == null) {
                return;
            }
            SWF swf = (openable instanceof SWF) ? (SWF) openable : ((ABC) openable).getSwf();
            if (swf != null) {
                swf.as3Cache.remove(pack);
            }
        }
    }

    /**
     * Checks whether list of Actions of ASMSource is cached.
     *
     * @param src ASMSource
     * @return True if cached
     */
    public static boolean isActionListCached(ASMSource src) {
        if (src != null) {
            SWF swf = src.getSwf();
            if (swf != null) {
                return swf.as2Cache.isPCodeCached(src);
            }
        }

        return false;
    }

    /**
     * Checks whether ASMSource is cached.
     *
     * @param src ASMSource
     * @return True if cached
     */
    public static boolean isCached(ASMSource src) {
        if (src != null) {
            SWF swf = src.getSwf();
            if (swf != null) {
                return swf.as2Cache.isCached(src);
            }
        }

        return false;
    }

    /**
     * Checks whether ScriptPack is cached.
     *
     * @param pack ScriptPack
     * @return True if cached
     */
    public static boolean isCached(ScriptPack pack) {
        if (pack != null) {
            Openable openable = pack.getOpenable();
            SWF swf = (openable instanceof SWF) ? (SWF) openable : ((ABC) openable).getSwf();
            if (swf != null) {
                return swf.as3Cache.isCached(pack);
            }
        }

        return false;
    }

    /**
     * Gets HighlightedText of ASMSource from cache.
     *
     * @param src ASMSource
     * @return True if cached
     */
    public static HighlightedText getFromCache(ASMSource src) {
        if (src != null) {
            SWF swf = src.getSwf();
            if (swf != null) {
                return swf.as2Cache.get(src);
            }
        }

        return null;
    }

    /**
     * Gets image from frame cache.
     *
     * @param key Key
     * @return Image
     */
    public SerializableImage getFromCache(String key) {
        if (frameCache.contains(key)) {
            return frameCache.get(key);
        }
        return null;
    }

    /**
     * Gets sound from sound cache.
     *
     * @param soundInfo Sound info
     * @param soundTag Sound tag
     * @param resample Resample to 44kHz
     * @return Byte data
     */
    public byte[] getFromCache(SOUNDINFO soundInfo, SoundTag soundTag, boolean resample) {
        SoundInfoSoundCacheEntry key = new SoundInfoSoundCacheEntry(soundInfo, soundTag, resample);
        if (soundCache.contains(key)) {
            return soundCache.get(key);
        }
        return null;
    }

    /**
     * Gets HighlightedText for ScriptPack from cache.
     *
     * @param pack ScriptPack
     * @return Hi
     */
    public static HighlightedText getFromCache(ScriptPack pack) {
        if (pack != null) {
            Openable openable = pack.getOpenable();
            SWF swf = (openable instanceof SWF) ? (SWF) openable : ((ABC) openable).getSwf();
            if (swf != null) {
                return swf.as3Cache.get(pack);
            }
        }

        return null;
    }

    /**
     * Gets ActionList of ASMSource from cache.
     *
     * @param src ASMSource
     * @return ActionList
     */
    public static ActionList getActionListFromCache(ASMSource src) {
        if (src != null) {
            SWF swf = src.getSwf();
            if (swf != null) {
                return swf.as2Cache.getPCode(src);
            }
        }

        return null;
    }

    /**
     * Gets ActionList of ASMSource from cache, if not in cache, parses it.
     *
     * @param src ASMSource
     * @param listeners Disassembly listeners
     * @return ActionList
     * @throws InterruptedException On interrupt
     */
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
            } catch (CancellationException ex) {
                throw ex;
            } catch (Exception ex) {
                logger.log(Level.SEVERE, null, ex);
                return new ActionList(src.getSwf().getCharset());
            }
        }
    }

    /**
     * Gets HighlightedText of ASMSource and ActionList from cache, decompiles
     * when not cached.
     *
     * @param src ASMSource
     * @param actions ActionList
     * @return HighlightedText
     * @throws InterruptedException On interrupt
     */
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

    /**
     * Gets HighlightedText of ScriptPack from cache, decompiles when not
     * cached.
     *
     * @param pack ScriptPack
     * @return HighlightedText
     * @throws InterruptedException On interrupt
     */
    public static HighlightedText getCached(ScriptPack pack) throws InterruptedException {
        Openable openable = pack.getOpenable();
        SWF swf = (openable instanceof SWF) ? (SWF) openable : ((ABC) openable).getSwf();
        HighlightedText res;
        if (swf != null) {
            res = swf.as3Cache.get(pack);
            if (res != null) {
                return res;
            }
        }

        return decompilerPool.decompile(swf.getAbcIndex(), pack);
    }

    /**
     * Gets Future of HighlightedText of ASMSource, ActionList
     *
     * @param src ASMSource
     * @param actions ActionList
     * @param listener Decompiled listeners
     * @return Future of HighlightedText
     * @throws InterruptedException On interrupt
     */
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

    /**
     * Gets Future of HighlightedText of ScriptPack
     *
     * @param pack ScriptPack
     * @param listener Decompiled listeners
     * @return Future of HighlightedText
     * @throws InterruptedException On interrupt
     */
    public static Future<HighlightedText> getCachedFuture(ScriptPack pack, ScriptDecompiledListener<HighlightedText> listener) throws InterruptedException {
        Openable openable = pack.getOpenable();
        SWF swf = (openable instanceof SWF) ? (SWF) openable : ((ABC) openable).getSwf();
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

        return decompilerPool.submitTask(swf.getAbcIndex(), pack, listener);
    }

    /**
     * Gets decompiler pool.
     *
     * @return DecompilerPool
     */
    public DecompilerPool getDecompilerPool() {
        return decompilerPool;
    }

    /**
     * Gets cache of Rectangles.
     *
     * @return Cache of Rectangles
     */
    public Cache<CharacterTag, RECT> getRectCache() {
        return rectCache;
    }

    /**
     * Gets shape export data cache.
     *
     * @return Shape export data cache
     */
    public Cache<SHAPE, ShapeExportData> getShapeExportDataCache() {
        return shapeExportDataCache;
    }

    /**
     * Gets image of specified frame of timeline.
     *
     * @param timeline Timeline
     * @param frame Frame
     * @param time Time
     * @param cursorPosition Cursor position
     * @param mouseButton Mouse button
     * @param displayRect Display rectangle
     * @param transformation Transformation
     * @param colorTransform Color transform
     * @param backGroundColor Background color
     * @param zoom Zoom
     * @param canUseSmoothing Can use smoothing
     * @return Image
     */
    public static SerializableImage frameToImageGet(Timeline timeline, int frame, int time, Point cursorPosition, int mouseButton, RECT displayRect, Matrix transformation, ColorTransform colorTransform, Color backGroundColor, double zoom, boolean canUseSmoothing) {
        if (timeline.getFrameCount() == 0) {
            return new SerializableImage(1, 1, SerializableImage.TYPE_INT_ARGB_PRE);
        }

        RECT rect = displayRect;
        SerializableImage image = new SerializableImage(
                rect.getWidth() == 0 ? 1 /*FIXME: is this necessary?*/ : (int) (rect.getWidth() * zoom / SWF.unitDivisor),
                rect.getHeight() == 0 ? 1 : (int) (rect.getHeight() * zoom / SWF.unitDivisor), SerializableImage.TYPE_INT_ARGB_PRE);
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
        ExportRectangle viewRect = new ExportRectangle(rect);
        timeline.toImage(frame, time, renderContext, image, image, false, m, new Matrix(), m, colorTransform, zoom, true, viewRect, m, true, Timeline.DRAW_MODE_ALL, 0, canUseSmoothing, new ArrayList<>());

        return image;
    }

    /**
     * Removes character from timeline.
     *
     * @param characterId Character ID
     * @param timeline Timeline
     * @param listener Listener to call after removing each of character.
     * @return True if modified
     */
    public boolean removeCharacterFromTimeline(int characterId, Timeline timeline, TagRemoveListener listener) {
        Set<Integer> chars = new HashSet<>();
        chars.add(characterId);
        return removeTagWithDependenciesFromTimeline(null, timeline, chars, listener);
    }

    /**
     * Removes character with dependencies.
     *
     * @param toRemove Tag to remove
     * @param timeline Timeline
     * @param listener Listener to call after removing each of character.
     */
    private void removeTagWithDependenciesFromTimeline(Tag toRemove, Timeline timeline, TagRemoveListener listener) {
        Set<Integer> dependingChars = new HashSet<>();
        if (toRemove instanceof CharacterTag) {
            int characterId = ((CharacterTag) toRemove).getCharacterId();
            if (characterId != -1) {
                dependingChars = getDependentCharacters(characterId);
                dependingChars.add(characterId);
            }
        }
        removeTagWithDependenciesFromTimeline(toRemove, timeline, dependingChars, listener);
    }

    /**
     * Removes character with dependencies.
     *
     * @param toRemove Tag to remove
     * @param timeline Timeline
     * @param dependingChars Depending characters
     * @param listener Listener to call after removing each of character.
     * @return True if modified
     */
    private boolean removeTagWithDependenciesFromTimeline(Tag toRemove, Timeline timeline, Set<Integer> dependingChars, TagRemoveListener listener) {
        Map<Integer, Integer> stage = new HashMap<>();
        Timelined timelined = timeline.timelined;
        ReadOnlyTagList tags = timelined.getTags();
        boolean modified = false;
        for (int i = 0; i < tags.size(); i++) {
            Tag t = tags.get(i);
            if (t instanceof RemoveTag) {
                RemoveTag rt = (RemoveTag) t;
                int depth = rt.getDepth();
                if (stage.containsKey(depth)) {
                    int currentCharId = stage.get(depth);
                    stage.remove(depth);
                    if (dependingChars.contains(currentCharId)) {
                        if (listener != null) {
                            listener.tagRemoved(t);
                        }
                        timelined.removeTag(i);
                        modified = true;
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
                    if (listener != null) {
                        listener.tagRemoved(t);
                    }
                    timelined.removeTag(i);
                    modified = true;
                    i--;
                    continue;
                }
            }

            if (t instanceof CharacterIdTag) {
                CharacterIdTag c = (CharacterIdTag) t;
                if (dependingChars.contains(c.getCharacterId())) {
                    if (listener != null) {
                        listener.tagRemoved(t);
                    }

                    timelined.removeTag(i);
                    modified = true;
                    i--;
                    continue;
                }
            }

            if (t == toRemove) {
                if (listener != null) {
                    listener.tagRemoved(t);
                }

                timelined.removeTag(i);
                modified = true;
                i--;
                continue;
            }

            if (t instanceof Timelined) {
                modified |= removeTagWithDependenciesFromTimeline(toRemove, ((Timelined) t).getTimeline(), dependingChars, listener);
            }
        }
        return modified;
    }

    /**
     * Removes character with dependencies.
     *
     * @param toRemove Tag to remove
     * @param timeline Timeline
     * @param listener Listener to call after removing each of character.
     * @return True if modified
     */
    private boolean removeTagFromTimeline(Tag toRemove, Timeline timeline, TagRemoveListener listener) {
        boolean modified = false;
        int characterId = -1;
        if (toRemove instanceof CharacterTag) {
            characterId = ((CharacterTag) toRemove).getCharacterId();
            if (characterId != -1) {
                modified = removeCharacterFromTimeline(characterId, timeline, listener);
            }
        }
        Timelined timelined = timeline.timelined;
        ReadOnlyTagList tags = timelined.getTags();
        for (int i = 0; i < tags.size(); i++) {
            Tag t = tags.get(i);
            if (t == toRemove) {
                if (listener != null) {
                    listener.tagRemoved(t);
                }
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
                boolean sprModified = removeTagFromTimeline(toRemove, spr.getTimeline(), listener);
                if (sprModified) {
                    spr.setModified(true);
                }
                modified |= sprModified;
            }
        }
        return modified;
    }

    /**
     * Removes tags.
     *
     * @param tags Tags
     * @param removeDependencies Remove dependencies?
     * @param listener Listener to call after removing each of character.
     */
    public synchronized void removeTags(Collection<Tag> tags, boolean removeDependencies, TagRemoveListener listener) {
        Set<Timelined> timelineds = new HashSet<>();
        for (Tag tag : tags) {
            Timelined timelined = tag.getTimelined();
            timelineds.add(timelined);
            removeTagInternal(timelined, tag, removeDependencies, listener);
        }

        for (Timelined timelined : timelineds) {
            resetTimelines(timelined);
        }

        updateCharacters();
        clearImageCache();
        clearShapeCache();
    }

    /**
     * Removes tag from SWF timeline.
     *
     * @param index Index of tag
     */
    @Override
    public synchronized void removeTag(int index) {
        setModified(true);
        tags.remove(index);
        updateCharacters();
    }

    /**
     * Removes tag from SWF timeline.
     *
     * @param tag Tag
     */
    @Override
    public synchronized void removeTag(Tag tag) {
        setModified(true);
        tags.remove(tag);
        updateCharacters();
        readOnlyTags = null;        
    }

    /**
     * Removes tag from SWF timeline.s
     *
     * @param tag Tag
     * @param removeDependencies Remove dependencies?
     * @param listener Listener to call after removing each of character.
     */
    public synchronized void removeTag(Tag tag, boolean removeDependencies, TagRemoveListener listener) {
        Timelined timelined = tag.getTimelined();
        removeTagInternal(timelined, tag, removeDependencies, listener);
        resetTimelines(timelined);
        updateCharacters();
        clearImageCache();
        clearShapeCache();
        readOnlyTags = null;
    }

    /**
     * Removes tag from SWF timeline.
     *
     * @param timelined Timelined
     * @param tag Tag
     * @param removeDependencies Remove dependencies?
     * @param listener Listener to call after removing each of character.
     */
    private void removeTagInternal(Timelined timelined, Tag tag, boolean removeDependencies, TagRemoveListener listener) {
        if ((tag instanceof DoABC2Tag) || (tag instanceof DoABCTag)) {
            clearAbcListCache();
        }
        if (tag instanceof ShowFrameTag || ShowFrameTag.isNestedTagType(tag.getId())) {
            if (listener != null) {
                listener.tagRemoved(tag);
            }
            timelined.removeTag(tag);
            timelined.setModified(true);
            timelined.resetTimeline();
        } else if (removeDependencies) { // timeline should be always the swf here
            removeTagWithDependenciesFromTimeline(tag, timelined.getTimeline(), listener);
            timelined.setModified(true);
        } else {
            boolean modified = removeTagFromTimeline(tag, timelined.getTimeline(), listener);
            if (modified) {
                timelined.setModified(true);
            }
        }
    }

    /**
     * Gets (readonly) list of all tags in the SWF file.
     *
     * @return Tags
     */
    @Override
    public synchronized ReadOnlyTagList getTags() {
        if (readOnlyTags == null) {
            readOnlyTags = new ReadOnlyTagList(tags);
        }

        return readOnlyTags;
    }

    /**
     * Adds a tag to the SWF.
     *
     * @param tag Tag
     */
    @Override
    public synchronized void addTag(Tag tag) {
        setModified(true);
        tags.add(tag);
        updateCharacters();
        readOnlyTags = null;
    }

    /**
     * Adds a tag to the SWF at the specified index.
     *
     * @param index Index
     * @param tag Tag
     */
    @Override
    public synchronized void addTag(int index, Tag tag) {
        setModified(true);
        tags.add(index, tag);
        updateCharacters();
        readOnlyTags = null;
    }

    /**
     * Gets index of specified tag.
     *
     * @param tag Tag
     * @return Index or -1 when not found
     */
    @Override
    public synchronized int indexOfTag(Tag tag) {
        return tags.indexOf(tag);
    }

    /**
     * Adds tag just before targetTag in targetTags timeline.
     *
     * @param newTag New tag
     * @param targetTag Target tag
     */
    public static void addTagBefore(Tag newTag, Tag targetTag) {
        Timelined tim = targetTag.getTimelined();
        int index = tim.indexOfTag(targetTag);
        if (index < 0) {
            return;
        }
        tim.addTag(index, newTag);
        tim.resetTimeline();
        if (tim instanceof DefineSpriteTag) {
            DefineSpriteTag sprite = (DefineSpriteTag) tim;
            sprite.frameCount = tim.getTimeline().getFrameCount();
        } else if (tim instanceof SWF) {
            SWF swf = (SWF) tim;
            swf.frameCount = tim.getTimeline().getFrameCount();
        }
        newTag.setTimelined(tim);
    }

    /**
     * Packs character ids. When a character id does not belong to any
     * character, it is removed, and then later characters characterids are
     * shifted.
     */
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

    /**
     * Sorts character ids. Order of character ids will match the first
     * occurrence of the characters.
     */
    public void sortCharacterIds() {
        int maxId = Math.max(tags.size(), getNextCharacterId());
        int id = maxId;
        // first set the character ids to surely not used ids
        for (Tag tag : getTags()) {
            if (tag instanceof CharacterTag) {
                CharacterTag characterTag = (CharacterTag) tag;
                if (characterTag.getCharacterId() != -1) {
                    replaceCharacter(characterTag.getCharacterId(), id++);
                }
            }
        }
        // then set them to 1,2,3...
        id = 1;
        for (Tag tag : getTags()) {
            if (tag instanceof CharacterTag) {
                CharacterTag characterTag = (CharacterTag) tag;
                if (characterTag.getCharacterId() != -1) {
                    replaceCharacter(characterTag.getCharacterId(), id++);
                }
            }
        }
    }

    /**
     * Replaces character id with another.
     *
     * @param oldCharacterId Old character id
     * @param newCharacterId New character id
     * @return True if modified
     */
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

    /**
     * Switches ids of two characters.
     *
     * @param characterTag Character tag
     * @param newCharacterId New character id
     */
    public void replaceCharacterTags(CharacterTag characterTag, int newCharacterId) {
        int characterId = characterTag.getCharacterId();
        if (characterId == -1) {
            return;
        }
        CharacterTag newCharacter = getCharacter(newCharacterId);
        newCharacter.setCharacterId(characterId);
        characterTag.setCharacterId(newCharacterId);
        newCharacter.setModified(true);
        characterTag.setModified(true);

        assignExportNamesToSymbols();
        assignClassesToSymbols();
        clearImageCache();
        clearShapeCache();
        updateCharacters();
        computeDependentCharacters();
        computeDependentFrames();
    }

    /**
     * Converts SWF to string.
     *
     * @return String
     */
    @Override
    public String toString() {
        return getTitleOrShortFileName();
    }

    /**
     * Deobfuscates SWF file.
     *
     * @param level Deobfuscation level
     * @throws InterruptedException On interrupt
     */
    public void deobfuscate(DeobfuscationLevel level) throws InterruptedException {
        List<ABCContainerTag> atags = getAbcList();

        int apos = 0;
        for (ABCContainerTag tag : atags) {
            apos++;
            final int fpos = apos;
            Reference<Integer> numDeoScripts = new Reference<>(0);
            DeobfuscationListener deoListener = new DeobfuscationListener() {
                @Override
                public void itemDeobfuscated() {
                    numDeoScripts.setVal(numDeoScripts.getVal() + 1);
                    informListeners("deobfuscate_pcode", "abc " + fpos + "/" + atags.size() + " script " + numDeoScripts.getVal() + "/" + tag.getABC().script_info.size());
                }
            };
            if (level == DeobfuscationLevel.LEVEL_REMOVE_DEAD_CODE) {
                tag.getABC().removeDeadCode(deoListener);
            } else if (level == DeobfuscationLevel.LEVEL_REMOVE_TRAPS) {
                tag.getABC().removeTraps(deoListener);
            }

            ((Tag) tag).setModified(true);
        }
    }

    /**
     * Injects debugline and debugfile instructions to AS3 P-code (lines of
     * P-code).
     *
     * @throws InterruptedException On interrupt
     */
    public void injectAS3PcodeDebugInfo() throws InterruptedException {
        injectAS3PcodeDebugInfo("main");
    }

    /**
     * Injects debugline and debugfile instructions to AS3 P-code (lines of
     * P-code).
     *
     * @param swfHash SWF identifier
     * @throws InterruptedException On interrupt
     */
    public void injectAS3PcodeDebugInfo(String swfHash) throws InterruptedException {
        List<ScriptPack> packs = getAS3Packs();
        int i = 0;
        for (ScriptPack s : packs) {
            if (CancellableWorker.isInterrupted()) {
                throw new InterruptedException();
            }
            i++;
            informListeners("inject_debuginfo", "" + i + "/" + packs.size() + ": " + s.getPath());
            int abcIndex = s.allABCs.indexOf(s.abc);
            if (s.isSimple) {
                s.injectPCodeDebugInfo(abcIndex, swfHash);
            }
        }
    }

    /**
     * Injects debugline and debugfile instructions to AS3 code.
     *
     * @param decompileDir Directory to set file information paths
     * @throws InterruptedException On interrupt
     */
    public void injectAS3DebugInfo(File decompileDir) throws InterruptedException {
        injectAS3DebugInfo(decompileDir, "main");
    }

    /**
     * Injects debugline and debugfile instructions to AS3 code.
     *
     * @param decompileDir Directory to set file information paths
     * @param swfHash SWF identifier
     * @throws InterruptedException On interrupt
     */
    public void injectAS3DebugInfo(File decompileDir, String swfHash) throws InterruptedException {
        List<ScriptPack> packs = getAS3Packs();
        int i = 0;
        for (ScriptPack s : packs) {
            if (CancellableWorker.isInterrupted()) {
                throw new InterruptedException();
            }
            i++;
            informListeners("inject_debuginfo", "" + i + "/" + packs.size() + ": " + s.getPath());
            if (s.isSimple) {
                try {
                    s.injectDebugInfo(decompileDir, swfHash);
                } catch (Throwable t) {
                    Logger.getLogger(SWF.class.getName()).log(Level.SEVERE, "Error injecting debug info", t);
                }
            }
        }
    }

    /**
     * Enables debugging. Adds tags to enable debugging and optionally injects
     * debugline and debugfile instructions to AS3 code by decompiling it first
     *
     * @param injectAS3Code Modify AS3 code with debugfile / debugline ?
     * @param decompileDir Directory to virtual decompile (will affect
     * debugfile)
     * @throws InterruptedException On interrupt
     */
    public void enableDebugging(boolean injectAS3Code, File decompileDir) throws InterruptedException {
        enableDebugging(injectAS3Code, decompileDir, false);
    }

    /**
     * Enables debugging. Adds tags to enable debugging.
     *
     * @throws InterruptedException On interrupt
     */
    public void enableDebugging() throws InterruptedException {
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
     * @throws InterruptedException On interrupt
     */
    public void enableDebugging(boolean injectAS3Code, File decompileDir, boolean telemetry) throws InterruptedException {
        enableDebugging(injectAS3Code, decompileDir, telemetry, false);
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
    public void enableDebugging(boolean injectAS3Code, File decompileDir, boolean telemetry, boolean pcodeLevel) throws InterruptedException {
        enableDebugging(injectAS3Code, decompileDir, telemetry, pcodeLevel, "main");
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
     * @param swfHash SWF identifier
     * @throws InterruptedException On interrupt
     */
    public void enableDebugging(boolean injectAS3Code, File decompileDir, boolean telemetry, boolean pcodeLevel, String swfHash) throws InterruptedException {

        if (injectAS3Code) {
            if (pcodeLevel) {
                injectAS3PcodeDebugInfo(swfHash);
            } else {
                injectAS3DebugInfo(decompileDir, swfHash);
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
     * Finds DebugID tag.
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
     * Finds DebugID tag and generates new one if none exists.
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

    /**
     * Generates SWD file for P-code debugging.
     *
     * @param file SWD file
     * @param breakpoints Breakpoints - map of script name to list of lines
     * @return True on success
     * @throws IOException On I/O error
     * @throws InterruptedException On interrupt
     */
    public boolean generatePCodeSwdFile(File file, Map<String, Set<Integer>> breakpoints) throws IOException, InterruptedException {
        return generatePCodeSwdFile(file, breakpoints, "main");
    }

    /**
     * Generates SWD file for P-code debugging.
     *
     * @param file SWD file
     * @param breakpoints Breakpoints - map of script name to list of lines
     * @param swfHash SWF identifier
     * @return True on success
     * @throws IOException On I/O error
     * @throws InterruptedException On interrupt
     */
    public boolean generatePCodeSwdFile(File file, Map<String, Set<Integer>> breakpoints, String swfHash) throws IOException, InterruptedException {
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
            if (CancellableWorker.isInterrupted()) {
                throw new InterruptedException();
            }
            informListeners("generate_swd", name);
            moduleId++;
            String sname = swfHash + ":" + "#PCODE " + name;
            int bitmap = SWD.bitmapAction;
            items.add(new SWD.DebugScript(moduleId, bitmap, sname, ""));

            HighlightedTextWriter writer = new HighlightedTextWriter(Configuration.getCodeFormatting(), true);
            try {
                asms.get(name).getASMSource(ScriptExportMode.PCODE, writer, null);
            } catch (InterruptedException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
            writer.finishHilights();
            HighlightingList hls = writer.instructionHilights;

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

    /**
     * Generates SWF file for debugging.
     *
     * @param file SWD file
     * @param breakpoints Breakpoints - map of script name to list of lines
     * @return True on success
     * @throws IOException On I/O error
     */
    public boolean generateSwdFile(File file, Map<String, Set<Integer>> breakpoints) throws IOException {
        return generateSwdFile(file, breakpoints, "main");
    }

    /**
     * Generates SWF file for debugging.
     *
     * @param file SWD file
     * @param breakpoints Breakpoints - map of script name to list of lines
     * @param swfHash SWF identifier
     * @return True on success
     * @throws IOException On I/O error
     */
    public boolean generateSwdFile(File file, Map<String, Set<Integer>> breakpoints, String swfHash) throws IOException {
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
                if (CancellableWorker.isInterrupted()) {
                    throw new InterruptedException();
                }
                informListeners("generate_swd", name);
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
                String sname = swfHash + ":" + name;
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

    /**
     * Enables telemetry.
     *
     * @param password Password
     * @return True on success
     */
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

    /**
     * Gets Flex Main class.
     *
     * @param ignoredClasses List of ignored classes
     * @param ignoredNs List of ignored namespace names
     * @return Main class name
     */
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
                                    List<MethodBody> callStack = new ArrayList<>();
                                    callStack.add(documentPack.abc.findBody(mi));
                                    int swfVersion = -1;
                                    if (documentPack.getOpenable() instanceof SWF) {
                                        swfVersion = ((SWF) documentPack.getOpenable()).version;
                                    }
                                    documentPack.abc.findBody(mi).convert(swfVersion, callStack, getAbcIndex(), new ConvertData(), "??", ScriptExportMode.AS, true, mi, documentPack.scriptIndex, cindex, documentPack.abc, t, new ScopeStack(), 0, new NulWriter(), new ArrayList<>(), new Traits(), true, new HashSet<>());
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
                                                                            callStack = new ArrayList<>();
                                                                            callStack.add(p.abc.findBody(cinit));
                                                                            p.abc.findBody(cinit).convert(swfVersion, callStack, getAbcIndex(), new ConvertData(), "??", ScriptExportMode.AS, true, cinit, p.scriptIndex, cindex, p.abc, t, new ScopeStack(), 0, new NulWriter(), new ArrayList<>(), new Traits(), true, new HashSet<>());
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

    /**
     * Replaces a tag in the SWF.
     *
     * @param oldTag Old tag
     * @param newTag New tag
     */
    @Override
    public void replaceTag(Tag oldTag, Tag newTag) {
        setModified(true);
        int index = tags.indexOf(oldTag);
        if (index != -1) {
            tags.set(index, newTag);
            updateCharacters();
        }
    }

    /**
     * Replaces tag at given index.
     *
     * @param index Index
     * @param newTag New tag
     */
    @Override
    public void replaceTag(int index, Tag newTag) {
        removeTag(index);
        addTag(index, newTag);
    }

    /**
     * Gets rect including strokes.
     *
     * @return Rect including strokes
     */
    @Override
    public RECT getRectWithStrokes() {
        return getRect();
    }

    /**
     * Gets cyclic character ids.
     *
     * @return Set of character ids
     */
    public Set<Integer> getCyclicCharacters() {
        if (cyclicCharacters != null) {
            return cyclicCharacters;
        }
        Set<Integer> ct = new HashSet<>();
        Map<Integer, Set<Integer>> characterToNeeded = new HashMap<>();

        for (Tag t : getTags()) {
            if (t instanceof CharacterTag) {
                CharacterTag cht = (CharacterTag) t;
                if (cht.getCharacterId() != -1) {
                    Set<Integer> needed = new HashSet<>();
                    cht.getNeededCharacters(needed, this);
                    characterToNeeded.put(cht.getCharacterId(), needed);
                }
            }
        }

        for (int chid : characterToNeeded.keySet()) {
            for (int n : characterToNeeded.get(chid)) {
                if (searchNeeded(characterToNeeded, chid, n, new HashSet<>())) {
                    ct.add(chid);
                }
            }
        }

        cyclicCharacters = ct;
        return cyclicCharacters;
    }

    /**
     * Searches needed characters
     *
     * @param characterToNeeded Character to needed map
     * @param searched Searched character id
     * @param current Current character id
     * @param visited Visited characters
     * @return True if found
     */
    private boolean searchNeeded(Map<Integer, Set<Integer>> characterToNeeded, int searched, int current, Set<Integer> visited) {
        if (visited.contains(current)) {
            return false;
        }
        visited.add(current);
        if (current == searched) {
            return true;
        }

        if (!characterToNeeded.containsKey(current)) {
            return false;
        }

        for (int n : characterToNeeded.get(current)) {
            if (searchNeeded(characterToNeeded, searched, n, visited)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Sets file title.
     *
     * @param fileTitle File title
     */
    public void setFileTitle(String fileTitle) {
        this.fileTitle = fileTitle;
    }

    /**
     * Gets frame count. It is a number stored in header.
     *
     * @return Frame count
     */
    @Override
    public int getFrameCount() {
        return frameCount;
    }

    /**
     * Sets frame count. It is a number stored in header.
     *
     * @param frameCount Frame count
     */
    @Override
    public void setFrameCount(int frameCount) {
        this.frameCount = frameCount;
    }

    /**
     * Sets openableList which this SWF is part of.
     *
     * @param openableList OpenableList
     */
    @Override
    public void setOpenableList(OpenableList openableList) {
        this.openableList = openableList;
    }

    /**
     * Gets openableList which this SWF is part of.
     *
     * @return OpenableList
     */
    @Override
    public OpenableList getOpenableList() {
        return openableList;
    }

    /**
     * Calculates uninitialized class traits in AS2.
     *
     * @throws java.lang.InterruptedException On interruption
     */
    public void calculateAs2UninitializedClassTraits() throws InterruptedException {
        uninitializedAs2ClassTraits = new HashMap<>();
        UninitializedClassFieldsDetector detector = new UninitializedClassFieldsDetector();
        try {
            uninitializedAs2ClassTraits = detector.calculateAs2UninitializedClassTraits(this);
        } catch (Throwable t) {
            uninitializedAs2ClassTraits = null;
            throw t;
        }
    }

    /**
     * Gets uninitialized class traits in AS2.
     *
     * @return Map of class name to map of trait name to trait
     */
    public synchronized Map<String, Map<String, com.jpexs.decompiler.flash.action.as2.Trait>> getUninitializedAs2ClassTraits() throws InterruptedException {
        if (CancellableWorker.isInterrupted()) {
            throw new InterruptedException();
        }
        if (uninitializedAs2ClassTraits == null) {            
            calculateAs2UninitializedClassTraits();
        }
        return uninitializedAs2ClassTraits;
    }
    
    public boolean needsCalculatingAS2UninitializeClassTraits(ASMSource src) {
        if (!isAS3()) {
            if (src instanceof DoInitActionTag) {
                if (uninitializedAs2ClassTraits == null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gets SWF (self)
     *
     * @return SWF
     */
    @Override
    public SWF getSwf() {
        return this;
    }
}
