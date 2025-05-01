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
package com.jpexs.decompiler.flash.configuration;

import com.jpexs.decompiler.flash.ApplicationInfo;
import com.jpexs.decompiler.flash.exporters.modes.ExeExportMode;
import com.jpexs.decompiler.flash.helpers.CodeFormatting;
import com.jpexs.decompiler.flash.helpers.FontHelper;
import com.jpexs.decompiler.flash.importers.TextImportResizeTextBoundsMode;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.Path;
import java.awt.Font;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;

/**
 * Configuration of FFDec.
 *
 * @author JPEXS
 */
public final class Configuration {

    private static final String CONFIG_NAME = "config.bin";

    private static final File unspecifiedFile = new File("unspecified");

    private static File directory = unspecifiedFile;

    /**
     * Log level
     */
    public static final Level logLevel;

    /**
     * Show stats
     */
    public static boolean showStat;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("ui")
    public static ConfigurationItem<Boolean> openMultipleFiles = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("script")
    public static ConfigurationItem<Boolean> decompile = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("decompilation")
    public static ConfigurationItem<Boolean> parallelSpeedUp = null;

    @ConfigurationDefaultInt(0)
    @ConfigurationCategory("decompilation")
    public static ConfigurationItem<Integer> parallelSpeedUpThreadCount = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("script")
    public static ConfigurationItem<Boolean> autoDeobfuscate = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("")
    public static ConfigurationItem<Boolean> cacheOnDisk = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("")
    public static ConfigurationItem<Boolean> cacheImages = null;

    /*
    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("display")
    public static ConfigurationItem<Boolean> internalFlashViewer = null;
     */
    @ConfigurationDefaultBoolean(false)
    @ConfigurationInternal
    public static ConfigurationItem<Boolean> useAdobeFlashPlayerForPreviews = null;

    @ConfigurationDefaultInt(1000)
    @ConfigurationCategory("display")
    public static ConfigurationItem<Integer> setMovieDelay = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("display")
    public static ConfigurationItem<Boolean> dumpView = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("display")
    public static ConfigurationItem<Boolean> useHexColorFormat = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("display")
    public static ConfigurationItem<Boolean> showOldTextDuringTextEditing = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("ui")
    public static ConfigurationItem<Boolean> gotoMainClassOnStartup = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("script")
    public static ConfigurationItem<Boolean> autoRenameIdentifiers = null;

    @ConfigurationDefaultBoolean(false)
    public static ConfigurationItem<Boolean> offeredAssociation = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("script")
    public static ConfigurationItem<Boolean> decimalAddress = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("script")
    public static ConfigurationItem<Boolean> showAllAddresses = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("display")
    public static ConfigurationItem<Boolean> useFrameCache = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("ui")
    public static ConfigurationItem<Boolean> useRibbonInterface = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("export")
    public static ConfigurationItem<Boolean> overwriteExistingFiles = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("export")
    public static ConfigurationItem<Boolean> openFolderAfterFlaExport = null;

    @ConfigurationCategory("export")
    public static ConfigurationItem<String> overrideTextExportFileName = null;

    @ConfigurationDefaultBoolean(false)
    public static ConfigurationItem<Boolean> useDetailedLogging = null;

    @ConfigurationDefaultBoolean(true)
    public static ConfigurationItem<Boolean> warningOpeningReadOnly = null;

    /**
     * Debug mode = throwing an error when comparing original file and
     * recompiled
     */
    @ConfigurationDefaultBoolean(false)
    @ConfigurationInternal
    public static ConfigurationItem<Boolean> _debugMode = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationInternal
    public static ConfigurationItem<Boolean> _showDebugMenu = null;

    /**
     * Turn off resolving constants in ActionScript 2
     */
    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("script")
    public static ConfigurationItem<Boolean> resolveConstants = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("script")
    @ConfigurationInternal
    public static ConfigurationItem<Boolean> showFileOffsetInPcodeHex = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("script")
    @ConfigurationInternal
    public static ConfigurationItem<Boolean> showOriginalBytesInPcodeHex = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("format")
    public static ConfigurationItem<Boolean> padAs3PCodeInstructionName = null;

    /**
     * Limit of code subs (for obfuscated code)
     */
    @ConfigurationDefaultInt(500)
    @ConfigurationCategory("limit")
    public static ConfigurationItem<Integer> sublimiter = null;

    /**
     * Total export timeout in seconds
     */
    @ConfigurationDefaultInt(30 * 60)
    @ConfigurationCategory("limit")
    public static ConfigurationItem<Integer> exportTimeout = null;

    /**
     * Decompilation timeout in seconds for a single file
     */
    @ConfigurationDefaultInt(5 * 60)
    @ConfigurationCategory("limit")
    public static ConfigurationItem<Integer> decompilationTimeoutFile = null;

    /**
     * AS1/2 deobfuscator execution limit (max number of instructions processed)
     */
    @ConfigurationDefaultInt(10000)
    @ConfigurationCategory("limit")
    public static ConfigurationItem<Integer> as12DeobfuscatorExecutionLimit = null;

    /**
     * Using parameter names in decompiling may cause problems because official
     * programs like Flash CS 5.5 inserts wrong parameter names indices
     */
    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("script")
    public static ConfigurationItem<Boolean> paramNamesEnable = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("ui")
    public static ConfigurationItem<Boolean> displayFileName = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationInternal
    public static ConfigurationItem<Boolean> _debugCopy = null;

    @ConfigurationDefaultBoolean(false)
    public static ConfigurationItem<Boolean> dumpTags = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("export")
    public static ConfigurationItem<Boolean> setFFDecVersionInExportedFont = null;

    @ConfigurationDefaultInt(60)
    @ConfigurationCategory("limit")
    public static ConfigurationItem<Integer> decompilationTimeoutSingleMethod = null;

    @ConfigurationDefaultInt(1)
    public static ConfigurationItem<Integer> lastRenameType = null;

    @ConfigurationDefaultString(".")
    @ConfigurationDirectory
    public static ConfigurationItem<String> lastSaveDir = null;

    @ConfigurationDefaultString(".")
    @ConfigurationDirectory
    public static ConfigurationItem<String> lastOpenDir = null;

    @ConfigurationDefaultString(".")
    @ConfigurationDirectory
    public static ConfigurationItem<String> lastExportDir = null;

    @ConfigurationDefaultString("en")
    @ConfigurationCategory("ui")
    public static ConfigurationItem<String> locale = null;

    @ConfigurationDefaultString("_loc%d_")
    @ConfigurationCategory("script")
    public static ConfigurationItem<String> registerNameFormat = null;

    @ConfigurationDefaultInt(15)
    public static ConfigurationItem<Integer> maxRecentFileCount = null;

    public static ConfigurationItem<String> recentFiles = null;

    public static ConfigurationItem<HashMap<String, String>> fontPairingMap = null;

    public static ConfigurationItem<HashMap<String, SwfSpecificConfiguration>> swfSpecificConfigs = null;

    public static ConfigurationItem<HashMap<String, SwfSpecificCustomConfiguration>> swfSpecificCustomConfigs = null;

    @ConfigurationDefaultCalendar(0)
    public static ConfigurationItem<Calendar> lastUpdatesCheckDate = null;

    @ConfigurationDefaultInt(1000)
    @ConfigurationName("gui.window.width")
    public static ConfigurationItem<Integer> guiWindowWidth = null;

    @ConfigurationDefaultInt(700)
    @ConfigurationName("gui.window.height")
    public static ConfigurationItem<Integer> guiWindowHeight = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationName("gui.window.maximized.horizontal")
    public static ConfigurationItem<Boolean> guiWindowMaximizedHorizontal = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationName("gui.window.maximized.vertical")
    public static ConfigurationItem<Boolean> guiWindowMaximizedVertical = null;

    @ConfigurationDefaultDouble(1)
    @ConfigurationCategory("display")
    @ConfigurationName("gui.fontSizeMultiplier")
    public static ConfigurationItem<Double> guiFontSizeMultiplier = null;

    // font used in AS1/2/3 source area, P-Code area, Define Text area and in Metadata area
    @ConfigurationDefaultString("Monospaced-Plain-12")
    @ConfigurationCategory("display")
    @ConfigurationName("gui.sourceFont")
    public static ConfigurationItem<String> sourceFontString = null;

    @ConfigurationDefaultDouble(0.5)
    @ConfigurationName("gui.avm2.splitPane.dividerLocationPercent")
    @ConfigurationInternal
    public static ConfigurationItem<Double> guiAvm2SplitPaneDividerLocationPercent = null;

    @ConfigurationDefaultDouble(0.5)
    @ConfigurationName("gui.actionSplitPane.dividerLocationPercent")
    @ConfigurationInternal
    public static ConfigurationItem<Double> guiActionSplitPaneDividerLocationPercent = null;

    @ConfigurationDefaultDouble(0.5)
    @ConfigurationName("gui.previewSplitPane.dividerLocationPercent")
    @ConfigurationInternal
    public static ConfigurationItem<Double> guiPreviewSplitPaneDividerLocationPercent = null;

    @ConfigurationDefaultDouble(0.3333333333)
    @ConfigurationName("gui.splitPane1.dividerLocationPercent")
    @ConfigurationInternal
    public static ConfigurationItem<Double> guiSplitPane1DividerLocationPercent = null;

    @ConfigurationDefaultDouble(0.6)
    @ConfigurationName("gui.splitPane2.dividerLocationPercent")
    @ConfigurationInternal
    public static ConfigurationItem<Double> guiSplitPane2DividerLocationPercent = null;

    @ConfigurationDefaultDouble(0.5)
    @ConfigurationName("gui.timeLineSplitPane.dividerLocationPercent")
    @ConfigurationInternal
    public static ConfigurationItem<Double> guiTimeLineSplitPaneDividerLocationPercent = null;

    @ConfigurationDefaultDouble(0.6)
    @ConfigurationName("gui.dump.splitPane.dividerLocationPercent")
    @ConfigurationInternal
    public static ConfigurationItem<Double> guiDumpSplitPaneDividerLocationPercent = null;

    @ConfigurationDefaultString("com.jpexs.decompiler.flash.gui.OceanicSkin")
    @ConfigurationName("gui.skin")
    @ConfigurationCategory("ui")
    public static ConfigurationItem<String> guiSkin = null;

    @ConfigurationDefaultInt(3)
    @ConfigurationCategory("export")
    public static ConfigurationItem<Integer> saveAsExeScaleMode = null;

    @ConfigurationCategory("export")
    public static ConfigurationItem<ExeExportMode> exeExportMode = null;

    @ConfigurationDefaultInt(1024 * 1024/*1MiB*/)
    @ConfigurationCategory("limit")
    public static ConfigurationItem<Integer> syntaxHighlightLimit = null;

    public static ConfigurationItem<Integer> guiFontPreviewSampleText = null;

    @ConfigurationName("gui.fontPreviewWindow.width")
    public static ConfigurationItem<Integer> guiFontPreviewWidth = null;

    @ConfigurationName("gui.fontPreviewWindow.height")
    public static ConfigurationItem<Integer> guiFontPreviewHeight = null;

    @ConfigurationName("gui.fontPreviewWindow.posX")
    public static ConfigurationItem<Integer> guiFontPreviewPosX = null;

    @ConfigurationName("gui.fontPreviewWindow.posY")
    public static ConfigurationItem<Integer> guiFontPreviewPosY = null;

    @ConfigurationDefaultInt(3)
    @ConfigurationName("formatting.indent.size")
    @ConfigurationCategory("format")
    public static ConfigurationItem<Integer> indentSize = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationName("formatting.indent.useTabs")
    @ConfigurationCategory("format")
    public static ConfigurationItem<Boolean> indentUseTabs = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("format")
    public static ConfigurationItem<Boolean> beginBlockOnNewLine = null;

    @ConfigurationDefaultInt(1000 * 60 * 60 * 24)
    @ConfigurationCategory("update")
    @ConfigurationName("check.updates.delay")
    public static ConfigurationItem<Integer> checkForUpdatesDelay = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("update")
    @ConfigurationName("check.updates.stable")
    public static ConfigurationItem<Boolean> checkForUpdatesStable = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("update")
    @ConfigurationName("check.updates.nightly")
    public static ConfigurationItem<Boolean> checkForUpdatesNightly = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("update")
    @ConfigurationName("check.updates.enabled")
    public static ConfigurationItem<Boolean> checkForUpdatesAuto = null;

    @ConfigurationCategory("update")
    public static ConfigurationItem<String> updateProxyAddress = null;

    @ConfigurationDefaultString("")
    @ConfigurationName("export.formats")
    public static ConfigurationItem<String> lastSelectedExportFormats = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("export")
    public static ConfigurationItem<Boolean> textExportSingleFile = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("export")
    public static ConfigurationItem<Boolean> scriptExportSingleFile = null;

    @ConfigurationDefaultString("--- SEPARATOR ---")
    @ConfigurationCategory("export")
    public static ConfigurationItem<String> textExportSingleFileSeparator = null;

    @ConfigurationDefaultString("--- RECORDSEPARATOR ---")
    @ConfigurationCategory("export")
    public static ConfigurationItem<String> textExportSingleFileRecordSeparator = null;

    @ConfigurationCategory("import")
    public static ConfigurationItem<TextImportResizeTextBoundsMode> textImportResizeTextBoundsMode = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("import")
    public static ConfigurationItem<Boolean> resetLetterSpacingOnTextImport = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationName("warning.experimental.as12edit")
    @ConfigurationCategory("script")
    public static ConfigurationItem<Boolean> warningExperimentalAS12Edit = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationName("warning.experimental.as3edit")
    @ConfigurationCategory("script")
    public static ConfigurationItem<Boolean> warningExperimentalAS3Edit = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("script")
    public static ConfigurationItem<Boolean> showCodeSavedMessage = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("script")
    public static ConfigurationItem<Boolean> showTraitSavedMessage = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("export")
    public static ConfigurationItem<Boolean> packJavaScripts = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("export")
    public static ConfigurationItem<Boolean> textExportExportFontFace = null;

    @ConfigurationDefaultInt(128)
    public static ConfigurationItem<Integer> lzmaFastBytes = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("script")
    public static ConfigurationItem<Boolean> showMethodBodyId = null;

    @ConfigurationDefaultDouble(1.0)
    @ConfigurationName("export.zoom")
    public static ConfigurationItem<Double> lastSelectedExportZoom = null;

    public static ConfigurationItem<String> pluginPath = null;

    @ConfigurationDefaultInt(55556)
    @ConfigurationCategory("script")
    public static ConfigurationItem<Integer> debuggerPort = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("script")
    public static ConfigurationItem<Boolean> randomDebuggerPackage = null;

    @ConfigurationDefaultBoolean(true)
    public static ConfigurationItem<Boolean> displayDebuggerInfo = null;

    @ConfigurationDefaultString("debugConsole")
    public static ConfigurationItem<String> lastDebuggerReplaceFunction = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("script")
    public static ConfigurationItem<Boolean> getLocalNamesFromDebugInfo = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("ui")
    public static ConfigurationItem<Boolean> tagTreeShowEmptyFolders = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("ui")
    public static ConfigurationItem<Boolean> autoLoadEmbeddedSwfs = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("ui")
    public static ConfigurationItem<Boolean> showCloseConfirmation = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("ui")
    public static ConfigurationItem<Boolean> editorMode = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("ui")
    public static ConfigurationItem<Boolean> autoSaveTagModifications = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("ui")
    public static ConfigurationItem<Boolean> saveSessionOnExit = null;

    public static ConfigurationItem<String> lastSessionFiles = null;

    public static ConfigurationItem<String> lastSessionFileTitles = null;

    public static ConfigurationItem<String> lastSessionSelection = null;

    public static ConfigurationItem<String> lastSessionTagListSelection = null;

    @ConfigurationDefaultInt(0)
    public static ConfigurationItem<Integer> lastView = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("ui")
    public static ConfigurationItem<Boolean> loopMedia = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("ui")
    public static ConfigurationItem<Boolean> allowOnlyOneInstance = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("script")
    public static ConfigurationItem<Boolean> ignoreCLikePackages = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("script")
    public static ConfigurationItem<Boolean> smartNumberFormatting = null;

    @ConfigurationDefaultBoolean(false)
    //@ConfigurationCategory("script")    
    public static ConfigurationItem<Boolean> enableScriptInitializerDisplay = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("ui")
    public static ConfigurationItem<Boolean> autoOpenLoadedSWFs = null;

    @ConfigurationDefaultString("")
    @ConfigurationCategory("paths")
    @ConfigurationFile
    public static ConfigurationItem<String> playerLocation = null;

    @ConfigurationDefaultString("")
    @ConfigurationCategory("paths")
    @ConfigurationFile
    public static ConfigurationItem<String> playerDebugLocation = null;

    @ConfigurationDefaultString("")
    @ConfigurationCategory("paths")
    @ConfigurationFile(".*\\.swc$")
    public static ConfigurationItem<String> playerLibLocation = null;

    @ConfigurationDefaultString("")
    @ConfigurationCategory("paths")
    @ConfigurationDirectory
    public static ConfigurationItem<String> flexSdkLocation = null;

    @ConfigurationDefaultDouble(0.7)
    @ConfigurationName("gui.avm2.splitPane.vars.dividerLocationPercent")
    @ConfigurationInternal
    public static ConfigurationItem<Double> guiAvm2VarsSplitPaneDividerLocationPercent = null;

    @ConfigurationDefaultDouble(0.7)
    @ConfigurationName("gui.action.splitPane.vars.dividerLocationPercent")
    @ConfigurationInternal
    public static ConfigurationItem<Double> guiActionVarsSplitPaneDividerLocationPercent = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("script")
    public static ConfigurationItem<Boolean> debugHalt = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationName("warning.svgImport")
    @ConfigurationCategory("import")
    public static ConfigurationItem<Boolean> warningSvgImport = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationName("warning.hexViewNotUpToDate")
    @ConfigurationCategory("import")
    public static ConfigurationItem<Boolean> warningHexViewNotUpToDate = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationName("shapeImport.useNonSmoothedFill")
    @ConfigurationCategory("import")
    public static ConfigurationItem<Boolean> shapeImportUseNonSmoothedFill = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("display")
    @ConfigurationName("internalFlashViewer.execute.as12")
    public static ConfigurationItem<Boolean> internalFlashViewerExecuteAs12 = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("script")
    public static ConfigurationItem<Boolean> displayDupInstructions = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("script")
    public static ConfigurationItem<Boolean> useRegExprLiteral = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("script")
    public static ConfigurationItem<Boolean> handleSkinPartsAutomatically = null;

    @ConfigurationDefaultBoolean(false)
    //@ConfigurationCategory("script")
    @ConfigurationInternal
    public static ConfigurationItem<Boolean> _ignoreAdditionalFlexClasses = null;

    @ConfigurationDefaultBoolean(false)
    //@ConfigurationCategory("script")
    @ConfigurationInternal
    public static ConfigurationItem<Boolean> _enableFlexExport = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("script")
    public static ConfigurationItem<Boolean> simplifyExpressions = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationInternal
    public static ConfigurationItem<Boolean> hwAcceleratedGraphics = null;

    @ConfigurationDefaultDouble(0.85)
    @ConfigurationName("gui.avm2.splitPane.docs.dividerLocationPercent")
    @ConfigurationInternal
    public static ConfigurationItem<Double> guiAvm2DocsSplitPaneDividerLocationPercent = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("script")
    public static ConfigurationItem<Boolean> useFlexAs3Compiler = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("ui")
    public static ConfigurationItem<Boolean> showSetAdvanceValuesMessage = null;

    @ConfigurationDefaultString("")
    @ConfigurationCategory("paths")
    @ConfigurationFile
    public static ConfigurationItem<String> graphVizDotLocation = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationInternal
    public static ConfigurationItem<Boolean> showLineNumbersInPCodeGraphvizGraph = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("format")
    public static ConfigurationItem<Boolean> indentAs3PCode = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("format")
    public static ConfigurationItem<Boolean> labelOnSeparateLineAs3PCode = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("script")
    public static ConfigurationItem<Boolean> useOldStyleGetSetLocalsAs3PCode = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("script")
    public static ConfigurationItem<Boolean> useOldStyleLookupSwitchAs3PCode = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("ui")
    public static ConfigurationItem<Boolean> checkForModifications = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationName("warning.initializers")
    @ConfigurationCategory("script")
    public static ConfigurationItem<Boolean> warningInitializers = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("ui")
    public static ConfigurationItem<Boolean> parametersPanelInSearchResults = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("ui")
    public static ConfigurationItem<Boolean> displayAs3PCodeDocsPanel = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("ui")
    public static ConfigurationItem<Boolean> displayAs3TraitsListAndConstantsPanel = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("ui")
    public static ConfigurationItem<Boolean> useAsTypeIcons = null;

    @ConfigurationDefaultInt(20000)
    @ConfigurationCategory("limit")
    public static ConfigurationItem<Integer> limitAs3PCodeOffsetMatching = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationInternal
    public static ConfigurationItem<Boolean> showSlowRenderingWarning = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("script")
    public static ConfigurationItem<Boolean> autoCloseQuotes = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("script")
    public static ConfigurationItem<Boolean> autoCloseDoubleQuotes = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("script")
    public static ConfigurationItem<Boolean> autoCloseBrackets = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("script")
    public static ConfigurationItem<Boolean> autoCloseParenthesis = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationInternal
    public static ConfigurationItem<Boolean> showDialogOnError = null;

    @ConfigurationDefaultInt(20)
    @ConfigurationCategory("limit")
    public static ConfigurationItem<Integer> limitSameChars = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("ui")
    public static ConfigurationItem<Boolean> showImportScriptsInfo = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("ui")
    public static ConfigurationItem<Boolean> showImportTextInfo = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("ui")
    public static ConfigurationItem<Boolean> showImportSymbolClassInfo = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("ui")
    public static ConfigurationItem<Boolean> showImportXmlInfo = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("ui")
    public static ConfigurationItem<Boolean> showImportImageInfo = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("display")
    public static ConfigurationItem<Boolean> autoPlaySwfs = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("ui")
    public static ConfigurationItem<Boolean> expandFirstLevelOfTreeOnLoad = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("ui")
    public static ConfigurationItem<Boolean> allowPlacingDefinesIntoSprites = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("ui")
    public static ConfigurationItem<Boolean> allowDragAndDropInTagListTree = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("display")
    public static ConfigurationItem<Boolean> allowMiterClipLinestyle = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("display")
    public static ConfigurationItem<Boolean> animateSubsprites = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("display")
    public static ConfigurationItem<Boolean> autoPlayPreviews = null;

    @ConfigurationDefaultInt(5 * 60 * 1000)
    @ConfigurationCategory("limit")
    public static ConfigurationItem<Integer> maxCachedTime = null;

    @ConfigurationDefaultInt(500)
    @ConfigurationCategory("limit")
    public static ConfigurationItem<Integer> maxCachedNum = null;

    @ConfigurationDefaultString("")
    @ConfigurationCategory("paths")
    @ConfigurationFile(".*\\.swc$")
    public static ConfigurationItem<String> airLibLocation = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("ui")
    public static ConfigurationItem<Boolean> showImportShapeInfo = null;

    @ConfigurationDefaultString("")
    public static ConfigurationItem<String> pinnedItemsTagTreePaths = null;

    @ConfigurationDefaultString("")
    public static ConfigurationItem<String> pinnedItemsTagListPaths = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("script")
    public static ConfigurationItem<Boolean> flattenASPackages = null;

    @ConfigurationDefaultDouble(1.0)
    @ConfigurationCategory("display")
    @ConfigurationName("gui.scale")
    public static ConfigurationItem<Double> uiScale = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationName("warning.video.vlc")
    @ConfigurationCategory("display")
    public static ConfigurationItem<Boolean> warningVideoVlc = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("display")
    public static ConfigurationItem<Boolean> playFrameSounds = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("display")
    public static ConfigurationItem<Boolean> fixAntialiasConflation = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("display")
    public static ConfigurationItem<Boolean> autoPlaySounds = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("script")
    public static ConfigurationItem<Boolean> deobfuscateAs12RemoveInvalidNamesAssignments = null;

    @ConfigurationDefaultDouble(0.6)
    @ConfigurationName("gui.splitPanePlace.dividerLocationPercent")
    @ConfigurationInternal
    public static ConfigurationItem<Double> guiSplitPanePlaceDividerLocationPercent = null;

    @ConfigurationDefaultDouble(0.6)
    @ConfigurationName("gui.splitPaneTransform1.dividerLocationPercent")
    @ConfigurationInternal
    public static ConfigurationItem<Double> guiSplitPaneTransform1DividerLocationPercent = null;

    @ConfigurationDefaultDouble(0.6)
    @ConfigurationName("gui.splitPaneTransform2.dividerLocationPercent")
    @ConfigurationInternal
    public static ConfigurationItem<Double> guiSplitPaneTransform2DividerLocationPercent = null;

    @ConfigurationDefaultString("")
    @ConfigurationName("gui.transform.lastExpandedCards")
    @ConfigurationInternal
    public static ConfigurationItem<String> guiTransformLastExpandedCards = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("ui")
    public static ConfigurationItem<Boolean> doubleClickNodeToEdit = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("script")
    public static ConfigurationItem<Boolean> warningDeobfuscation = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("script")
    public static ConfigurationItem<Boolean> warningRenameIdentifiers = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("ui")
    public static ConfigurationItem<Boolean> showImportMovieInfo = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("ui")
    public static ConfigurationItem<Boolean> showImportSoundInfo = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("export")
    public static ConfigurationItem<Boolean> svgRetainBounds = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("display")
    public static ConfigurationItem<Boolean> disableBitmapSmoothing = null;

    @ConfigurationDefaultString("")
    public static ConfigurationItem<String> pinnedItemsScrollPos = null;

    @ConfigurationDefaultInt(30)
    @ConfigurationCategory("ui")
    public static ConfigurationItem<Integer> maxRememberedScrollposItems = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("ui")
    public static ConfigurationItem<Boolean> rememberScriptsScrollPos = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("ui")
    public static ConfigurationItem<Boolean> rememberFoldersScrollPos = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationName("warning.initializers.class")
    @ConfigurationCategory("script")
    public static ConfigurationItem<Boolean> warningInitializersClass = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationName("warning.cannotencrypt")
    public static ConfigurationItem<Boolean> warningCannotEncrypt = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("export")
    public static ConfigurationItem<Boolean> lastExportEnableEmbed = null;

    @ConfigurationDefaultString("CS6")
    public static ConfigurationItem<String> lastFlaExportVersion = null;

    @ConfigurationDefaultBoolean(true)
    public static ConfigurationItem<Boolean> lastFlaExportCompressed = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("ui")
    public static ConfigurationItem<Boolean> showImportSpriteInfo = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("ui")
    public static ConfigurationItem<Boolean> displayAs12PCodeDocsPanel = null;

    @ConfigurationDefaultDouble(0.85)
    @ConfigurationName("gui.action.splitPane.docs.dividerLocationPercent")
    @ConfigurationInternal
    public static ConfigurationItem<Double> guiActionDocsSplitPaneDividerLocationPercent = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("ui")
    public static ConfigurationItem<Boolean> rememberLastScreen = null;

    @ConfigurationDefaultInt(-1)
    @ConfigurationInternal
    public static ConfigurationItem<Integer> lastMainWindowScreenIndex = null;

    @ConfigurationDefaultInt(-1)
    @ConfigurationInternal
    public static ConfigurationItem<Integer> lastMainWindowScreenX = null;

    @ConfigurationDefaultInt(-1)
    @ConfigurationInternal
    public static ConfigurationItem<Integer> lastMainWindowScreenY = null;

    @ConfigurationDefaultInt(-1)
    @ConfigurationInternal
    public static ConfigurationItem<Integer> lastMainWindowScreenWidth = null;

    @ConfigurationDefaultInt(-1)
    @ConfigurationInternal
    public static ConfigurationItem<Integer> lastMainWindowScreenHeight = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("ui")
    public static ConfigurationItem<Boolean> displayAs12PCodePanel = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("ui")
    public static ConfigurationItem<Boolean> displayAs3PCodePanel = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("export")
    public static ConfigurationItem<Boolean> flaExportUseMappedFontLayout = null;

    @ConfigurationDefaultInt(3)
    @ConfigurationName("formatting.tab.size")
    @ConfigurationCategory("format")
    public static ConfigurationItem<Integer> tabSize = null;

    @ConfigurationDefaultInt(1000)
    @ConfigurationCategory("limit")
    public static ConfigurationItem<Integer> boxBlurPixelsLimit = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("export")
    public static ConfigurationItem<Boolean> as3ExportNamesUseClassNamesOnly = null;

    @ConfigurationDefaultString("")
    @ConfigurationDirectory
    public static ConfigurationItem<String> jnaTempDirectory = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("export")
    public static ConfigurationItem<Boolean> flaExportFixShapes = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("export")
    public static ConfigurationItem<Boolean> lastExportResampleWav = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("display")
    public static ConfigurationItem<Boolean> previewResampleSound = null;

    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("export")
    public static ConfigurationItem<Boolean> lastExportTransparentBackground = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("script")
    public static ConfigurationItem<Boolean> warningAbcClean = null;

    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("script")
    public static ConfigurationItem<Boolean> warningAddFunction = null;
    
    @ConfigurationDefaultBoolean(false)
    @ConfigurationCategory("export")
    public static ConfigurationItem<Boolean> linkAllClasses = null;
    
    @ConfigurationDefaultString("")
    @ConfigurationInternal
    public static ConfigurationItem<String> recentColors = null;
    
    
    @ConfigurationDefaultDouble(0.7)
    @ConfigurationName("gui.splitPaneEasyVertical.dividerLocationPercent")
    @ConfigurationInternal
    public static ConfigurationItem<Double> guiSplitPaneEasyVerticalDividerLocationPercent = null;
    
    @ConfigurationDefaultDouble(0.7)
    @ConfigurationName("gui.splitPaneEasyHorizontal.dividerLocationPercent")
    @ConfigurationInternal
    public static ConfigurationItem<Double> guiSplitPaneEasyHorizontalDividerLocationPercent = null;
    
    public static ConfigurationItem<String> lastSessionEasySwf = null;
    
    @ConfigurationDefaultInt(1000)
    @ConfigurationCategory("limit")
    public static ConfigurationItem<Integer> maxScriptLineLength = null;
    
    @ConfigurationDefaultString(".")
    @ConfigurationDirectory
    public static ConfigurationItem<String> lastSolEditorDirectory = null;
    
    @ConfigurationDefaultBoolean(true)
    @ConfigurationCategory("display")
    public static ConfigurationItem<Boolean> halfTransparentParentLayersEasy = null;
    
    private enum OSId {
        WINDOWS, OSX, UNIX
    }

    private static OSId getOSId() {
        String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
        if ((OS.indexOf("mac") >= 0) || (OS.indexOf("darwin") >= 0)) {
            return OSId.OSX;
        } else if (OS.indexOf("win") >= 0) {
            return OSId.WINDOWS;
        } else {
            return OSId.UNIX;
        }
    }

    /**
     * Get FFDec home directory
     *
     * @return FFDec home directory
     */
    public static String getFFDecHome() {
        if (directory == unspecifiedFile) {
            directory = null;
            String userHome = null;
            try {
                userHome = System.getProperty("user.home");
            } catch (SecurityException ignore) {
                //ignored
            }
            if (userHome != null) {
                String applicationId = ApplicationInfo.SHORT_APPLICATION_NAME;
                OSId osId = getOSId();
                if (osId == OSId.WINDOWS) {
                    File appDataDir = null;
                    try {
                        String appDataEV = System.getenv("APPDATA");
                        if ((appDataEV != null) && (appDataEV.length() > 0)) {
                            appDataDir = new File(appDataEV);
                        }
                    } catch (SecurityException ignore) {
                        //ignored
                    }
                    String vendorId = ApplicationInfo.VENDOR;
                    if ((appDataDir != null) && appDataDir.isDirectory()) {
                        // ${APPDATA}\{vendorId}\${applicationId}
                        String path = vendorId + "\\" + applicationId + "\\";
                        directory = new File(appDataDir, path);
                    } else {
                        // ${userHome}\Application Data\${vendorId}\${applicationId}
                        String path = "Application Data\\" + vendorId + "\\" + applicationId + "\\";
                        directory = new File(userHome, path);
                    }
                } else if (osId == OSId.OSX) {
                    // ${userHome}/Library/Application Support/${applicationId}
                    String path = "Library/Application Support/" + applicationId + "/";
                    directory = new File(userHome, path);
                } else {
                    File xdgConfigHome = null;
                    File oldConfigDir = new File(userHome, "." + applicationId + "/");
                    try {
                        String xdgConfigHomeEV = System.getenv("XDG_CONFIG_HOME");
                        if ((xdgConfigHomeEV != null) && (xdgConfigHomeEV.length() > 0)) {
                            xdgConfigHome = new File(xdgConfigHomeEV);
                        }
                    } catch (SecurityException ignore) {
                        //ignored
                    }
                    if ((xdgConfigHome != null) && xdgConfigHome.isDirectory()) {
                        // ${xdgConfigHome}/${applicationId}
                        String path = applicationId + "/";
                        directory = new File(xdgConfigHome, path);
                    } else if (oldConfigDir.isDirectory()) {
                        // ${userHome}/.${applicationId}
                        directory = oldConfigDir;
                    } else {
                        // ${userHome}/.config/${applicationId}
                        String path = ".config/" + applicationId + "/";
                        directory = new File(userHome, path);
                    }
                }
            } else {
                //no home, then use application directory
                directory = new File(".");
            }
        }
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                if (!directory.exists()) {
                    directory = new File("."); //fallback to current directory
                }
            }
        }
        String ret = directory.getAbsolutePath();
        if (!ret.endsWith(File.separator)) {
            ret += File.separator;
        }
        return ret;
    }

    public static Font getSourceFont() {
        return FontHelper.stringToFont(sourceFontString.get());
    }

    /**
     * Get recent files
     * @return List of recent files
     */
    public static List<String> getRecentFiles() {
        String files = recentFiles.get();
        if (files == null || files.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(files.split("::"));
    }

    /**
     * Add recent file
     * @param path Path to file
     */
    public static void addRecentFile(String path) {
        List<String> recentFilesArray = new ArrayList<>(getRecentFiles());
        int idx = recentFilesArray.indexOf(path);
        if (idx != -1) {
            recentFilesArray.remove(idx);
        }
        recentFilesArray.add(path);
        while (recentFilesArray.size() > maxRecentFileCount.get()) {
            recentFilesArray.remove(0);
        }
        recentFiles.set(Helper.joinStrings(recentFilesArray, "::"));
    }

    /**
     * Remove recent file
     * @param path Path to file
     */
    public static void removeRecentFile(String path) {
        List<String> recentFilesArray = new ArrayList<>(getRecentFiles());
        int idx = recentFilesArray.indexOf(path);
        if (idx != -1) {
            recentFilesArray.remove(idx);
        }
        recentFiles.set(Helper.joinStrings(recentFilesArray, "::"));
    }

    /**
     * Get font to name map
     * @return Font to name map
     */
    public static Map<String, String> getFontToNameMap() {
        HashMap<String, String> map = fontPairingMap.get();
        if (map == null) {
            map = new HashMap<>();
            fontPairingMap.set(map);
        }

        return map;
    }

    /**
     * Add font pair
     * @param fileName File name
     * @param fontId Font ID
     * @param fontName Font name
     * @param installedName Installed name
     */
    public static void addFontPair(String fileName, int fontId, String fontName, String installedName) {
        Map<String, String> fontPairs = getFontToNameMap();
        fontPairs.put(fontName, installedName);

        SwfSpecificConfiguration swfConf = getOrCreateSwfSpecificConfiguration(fileName);
        swfConf.fontPairingMap.put(fontId + "_" + fontName, installedName);
    }

    /**
     * Get per-swf configuration.
     * @param fileName SWF File name
     * @return SWF specific configuration, null if not found
     */
    public static SwfSpecificConfiguration getSwfSpecificConfiguration(String fileName) {
        HashMap<String, SwfSpecificConfiguration> map = swfSpecificConfigs.get();
        if (map == null) {
            map = new HashMap<>();
            swfSpecificConfigs.set(map);
        }

        return map.get(fileName);
    }

    /**
     * Get or create per-swf configuration.
     * @param fileName SWF File name
     * @return SWF specific configuration
     */
    public static SwfSpecificConfiguration getOrCreateSwfSpecificConfiguration(String fileName) {
        SwfSpecificConfiguration swfConf = getSwfSpecificConfiguration(fileName);
        if (swfConf == null) {
            swfConf = new SwfSpecificConfiguration();
            swfSpecificConfigs.get().put(fileName, swfConf);
        }

        return swfConf;
    }

    /**
     * Get per-swf custom configuration.
     * @param fileName SWF File name
     * @return SWF specific custom configuration, null if not found
     */
    public static SwfSpecificCustomConfiguration getSwfSpecificCustomConfiguration(String fileName) {
        HashMap<String, SwfSpecificCustomConfiguration> map = swfSpecificCustomConfigs.get();
        if (map == null) {
            map = new HashMap<>();
            swfSpecificCustomConfigs.set(map);
        }

        return map.get(fileName);
    }

    /**
     * Get or create per-swf custom configuration.
     * @param fileName SWF File name
     * @return SWF specific custom configuration
     */
    public static SwfSpecificCustomConfiguration getOrCreateSwfSpecificCustomConfiguration(String fileName) {
        SwfSpecificCustomConfiguration swfConf = getSwfSpecificCustomConfiguration(fileName);
        if (swfConf == null) {
            swfConf = new SwfSpecificCustomConfiguration();
            swfSpecificCustomConfigs.get().put(fileName, swfConf);
        }

        return swfConf;
    }

    private static String getConfigFile() throws IOException {
        return getFFDecHome() + CONFIG_NAME;
    }

    private static HashMap<String, Object> loadFromFile(String file) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {

            @SuppressWarnings("unchecked")
            HashMap<String, Object> cfg = (HashMap<String, Object>) ois.readObject();
            return cfg;
        } catch (ClassNotFoundException | IOException ex) {
            // ignore
        }

        return new HashMap<>();
    }

    private static void saveToFile(String file) {
        HashMap<String, Object> config = new HashMap<>();
        for (Entry<String, Field> entry : getConfigurationFields().entrySet()) {
            try {
                String name = entry.getKey();
                Field field = entry.getValue();
                ConfigurationItem item = (ConfigurationItem) field.get(null);
                if (item.hasValue) {
                    config.put(name, item.get());
                }
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
            oos.writeObject(config);
        } catch (IOException ex) {
            //TODO: move this to GUI
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Cannot save configuration.", "Error", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(Configuration.class.getName()).severe("Configuration directory is read only.");
        }
    }

    /**
     * Save configuration to file
     */
    public static void saveConfig() {
        try {
            saveToFile(getConfigFile());
        } catch (IOException ex) {
            // ignore
        }
    }

    static {
        setConfigurationFields();
        if (useDetailedLogging.get()) {
            logLevel = Level.FINEST;
        } else if (_debugMode.get()) {
            logLevel = Level.INFO;
        } else {
            logLevel = Level.WARNING;
        }
        //limit parallel threads?
        //int processorCount = Runtime.getRuntime().availableProcessors();
    }

    /**
     * Set configuration fields.
     */
    @SuppressWarnings("unchecked")
    public static void setConfigurationFields() {
        try {
            HashMap<String, Object> config = loadFromFile(getConfigFile());
            for (Entry<String, Field> entry : getConfigurationFields().entrySet()) {
                String name = entry.getKey();
                Field field = entry.getValue();
                /* Unsupported in java 9+
                // remove final modifier from field
                Field modifiersField = field.getClass().getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
                 */

                Object defaultValue = getDefaultValue(field);
                Object value = null;
                if (config.containsKey(name)) {
                    value = config.get(name);

                    Class<?> type = ConfigurationItem.getConfigurationFieldType(field);
                    if (value != null && !type.isAssignableFrom(value.getClass())) {
                        System.out.println("Configuration item has a wrong type: " + name + " expected: " + type.getSimpleName() + " actual: " + value.getClass().getSimpleName());
                        value = null;
                    }
                }

                if (value != null) {
                    field.set(null, new ConfigurationItem(name, defaultValue, value));
                } else {
                    field.set(null, new ConfigurationItem(name, defaultValue));
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException | SecurityException ex) {
            // Reflection exceptions. This should never happen
            throw new Error(ex.getMessage());
        } catch (IOException ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (playerLibLocation.get("").isEmpty()) {
            File swcFile = getPlayerSwcOld();
            if (swcFile != null) {
                playerLibLocation.set(swcFile.getAbsolutePath());
            }
        }
        if (airLibLocation.get("").isEmpty()) {
            File swcFile = getAirSwcOld();
            if (swcFile != null) {
                airLibLocation.set(swcFile.getAbsolutePath());
            }
        }
    }

    /**
     * Get default value for field
     * @param field Field
     * @return Default value
     */
    public static Object getDefaultValue(Field field) {
        Object defaultValue = null;
        ConfigurationDefaultBoolean aBool = field.getAnnotation(ConfigurationDefaultBoolean.class);
        if (aBool != null) {
            defaultValue = aBool.value();
        }
        ConfigurationDefaultInt aInt = field.getAnnotation(ConfigurationDefaultInt.class);
        if (aInt != null) {
            defaultValue = aInt.value();
        }
        ConfigurationDefaultString aString = field.getAnnotation(ConfigurationDefaultString.class);
        if (aString != null) {
            defaultValue = aString.value();
        }
        ConfigurationDefaultDouble aDouble = field.getAnnotation(ConfigurationDefaultDouble.class);
        if (aDouble != null) {
            defaultValue = aDouble.value();
        }
        ConfigurationDefaultCalendar aCalendar = field.getAnnotation(ConfigurationDefaultCalendar.class);
        if (aCalendar != null) {
            GregorianCalendar mingc = new GregorianCalendar();
            mingc.setTime(new Date(aCalendar.value()));
            defaultValue = mingc;
        }
        return defaultValue;
    }

    /**
     * Get configuration fields
     * @return Configuration fields
     */
    public static Map<String, Field> getConfigurationFields() {
        return getConfigurationFields(false);
    }

    /**
     * Get configuration fields
     * @param lowerCaseNames Lower case names
     * @return Configuration fields
     */
    public static Map<String, Field> getConfigurationFields(boolean lowerCaseNames) {
        Field[] fields = Configuration.class.getDeclaredFields();
        Map<String, Field> result = new HashMap<>();
        for (Field field : fields) {
            if (ConfigurationItem.class.isAssignableFrom(field.getType())) {
                String name = ConfigurationItem.getName(field);
                if (lowerCaseNames) {
                    name = name.toLowerCase();
                }

                result.put(name, field);
            }
        }
        return result;
    }

    /**
     * Get code formatting
     * @return Code formatting
     */
    public static CodeFormatting getCodeFormatting() {
        CodeFormatting ret = new CodeFormatting();
        String indentString = "";
        for (int i = 0; i < indentSize.get(); i++) {
            indentString += indentUseTabs.get() ? "\t" : " ";
        }
        ret.indentString = indentString;
        ret.beginBlockOnNewLine = beginBlockOnNewLine.get();
        return ret;
    }

    /**
     * Get number of parallel threads
     * @return Number of parallel threads
     */
    public static int getParallelThreadCount() {
        int processorCount = Runtime.getRuntime().availableProcessors();
        
        int threadCount = parallelSpeedUpThreadCount.get();
        
        if (threadCount <= 0) {
            threadCount = processorCount - 1;
        }
        
        if (threadCount < 2) {
            threadCount = 2;
        }
        
        return threadCount;
    }

    /**
     * Get folder in FFDec home
     * @param folder Folder
     * @return Folder
     */
    public static File getPath(String folder) {
        String home = getFFDecHome();
        File dir = new File(home + folder);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    /**
     * Get flashlib path
     * @return Flashlib path
     */
    public static File getFlashLibPath() {
        return getPath("flashlib");
    }

    /**
     * Get projector path
     * @return Projector path
     */
    public static File getProjectorPath() {
        return getPath("projector");
    }

    private static String getDownloadsHtml() throws IOException {
        String html = Helper.downloadUrlString("https://www.adobe.com/support/flashplayer/downloads.html");
        return html;
    }

    private static String getUrlFromDownloadsHtml(String urlPatternString) {
        try {
            String html = getDownloadsHtml();
            Pattern urlPattern = Pattern.compile(urlPatternString, Pattern.DOTALL);
            Matcher matcher = urlPattern.matcher(html);
            if (matcher.matches()) {
                String url = matcher.group(1);
                int a = url.length();
                return url;
            }

            return null;
        } catch (IOException ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private static String getLatestPlayerGlobalUrl() {
        return getUrlFromDownloadsHtml(".*<a href=\"([^\"]*playerglobal[^\"]*\\.swc)\".*");
    }

    private static String getLatestProjectorUrlWin() {
        return getUrlFromDownloadsHtml(".*<a href=\"([^\"]*flashplayer[^\"]*_sa\\.exe)\".*");
    }

    private static String getLatestProjectorUrlMac() {
        return getUrlFromDownloadsHtml(".*<a href=\"([^\"]*flashplayer[^\"]*_sa\\.dmg)\".*");
    }

    private static String getLatestProjectorUrlLinux() {
        // This is a compressed file
        return getUrlFromDownloadsHtml(".*<a href=\"([^\"]*flashplayer[^\"]*_sa\\.i386\\.tar\\.gz)\".*");
    }

    private static File getFFDecFlashLib(String name) {
        try {
            File ffdecLibJarFile = new File(Configuration.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            File ffdecDirectory = null;
            if (ffdecLibJarFile.getAbsolutePath().endsWith(".jar")) {
                ffdecDirectory = ffdecLibJarFile.getParentFile().getParentFile();
            } else if (ffdecLibJarFile.getAbsolutePath().replace("\\", "/").endsWith("libsrc/ffdec_lib/build/classes")) {
                ffdecDirectory = ffdecLibJarFile.getParentFile().getParentFile().getParentFile().getParentFile();
                ffdecDirectory = new File(Path.combine(ffdecDirectory.getAbsolutePath(), "resources"));
            } else {
                return null;
            }
            File flashLibDirectory = new File(Path.combine(ffdecDirectory.getAbsolutePath(), "flashlib"));
            File flashLib = new File(Path.combine(flashLibDirectory.getAbsolutePath(), name));

            if (!flashLib.exists()) {
                return null;
            }
            return flashLib;
        } catch (URISyntaxException ex) {
            return null;
        }
    }

    /**
     * Get player SWC
     * @return Player SWC
     */
    public static File getPlayerSWC() {
        String libLocation = playerLibLocation.get("");
        File ret = null;
        if (!libLocation.isEmpty()) {
            ret = new File(libLocation);
        }
        if (ret == null || !ret.exists()) {
            ret = getPlayerSwcOld();
            if (ret != null) {
                playerLibLocation.set(ret.getAbsolutePath());
            }
            if (ret == null) {
                ret = getFFDecFlashLib("playerglobal32_0.swc");
            }
        }
        return ret;
    }

    /**
     * Get AIR SWC
     * @return AIR SWC
     */
    public static File getAirSWC() {
        String libLocation = airLibLocation.get("");
        File ret = null;
        if (!libLocation.isEmpty()) {
            ret = new File(libLocation);
        }
        if (ret == null || !ret.exists()) {
            ret = getAirSwcOld();
            if (ret != null) {
                airLibLocation.set(ret.getAbsolutePath());
            }
            if (ret == null) {
                ret = getFFDecFlashLib("airglobal.swc");
            }
        }
        return ret;
    }

    private static File getAirSwcOld() {
        File libsDir = getFlashLibPath();
        if (libsDir != null && libsDir.exists()) {
            File airFile = new File(Path.combine(libsDir.getAbsolutePath(), "airglobal.swc"));
            if (airFile.exists()) {
                return airFile;
            }
        }
        return null;
    }

    private static File getPlayerSwcOld() {
        File libsDir = getFlashLibPath();
        if (libsDir != null && libsDir.exists()) {
            File[] libs = libsDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().startsWith("playerglobal");
                }
            });
            List<String> libNames = new ArrayList<>();
            for (File f : libs) {
                libNames.add(f.getName());
            }
            Collections.sort(libNames);
            if (!libNames.isEmpty()) {
                return new File(libsDir.getAbsolutePath() + File.separator + libNames.get(libNames.size() - 1));
            } else {
                return null;
            }
        }

        return null;
    }

    /**
     * Get projector file
     * @param exportMode Export mode
     * @return Projector file
     */
    public static File getProjectorFile(ExeExportMode exportMode) {
        File projectoDir = getProjectorPath();
        if (projectoDir != null && projectoDir.exists()) {
            File[] projectors = projectoDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    switch (exportMode) {
                        case PROJECTOR_WIN:
                            return name.toLowerCase().endsWith(".exe");
                        case PROJECTOR_MAC:
                            return name.toLowerCase().endsWith(".dmg");
                        case PROJECTOR_LINUX:
                            return name.toLowerCase().endsWith(".gz");
                    }

                    return false;
                }
            });
            List<String> projectorNames = new ArrayList<>();
            for (File f : projectors) {
                projectorNames.add(f.getName());
            }
            Collections.sort(projectorNames);
            if (!projectorNames.isEmpty()) {
                return new File(projectoDir.getAbsolutePath() + File.separator + projectorNames.get(projectorNames.size() - 1));
            } else {
                return null;
            }
        }

        return null;
    }
}
