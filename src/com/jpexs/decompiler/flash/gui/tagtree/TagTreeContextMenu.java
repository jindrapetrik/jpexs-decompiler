/*
 *  Copyright (C) 2010-2024 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.gui.tagtree;

import com.jpexs.decompiler.flash.IdentifiersDeobfuscation;
import com.jpexs.decompiler.flash.ReadOnlyTagList;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.TagRemoveListener;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.abc.avm2.parser.AVM2ParseException;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.AbcIndexing;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.ActionScript3Parser;
import com.jpexs.decompiler.flash.abc.usages.simple.ABCCleaner;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.parser.script.ActionScript2Parser;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.configuration.CustomConfigurationKeys;
import com.jpexs.decompiler.flash.configuration.SwfSpecificCustomConfiguration;
import com.jpexs.decompiler.flash.exporters.swf.SwfFlashDevelopExporter;
import com.jpexs.decompiler.flash.exporters.swf.SwfIntelliJIdeaExporter;
import com.jpexs.decompiler.flash.exporters.swf.SwfVsCodeExporter;
import com.jpexs.decompiler.flash.gui.AppDialog;
import com.jpexs.decompiler.flash.gui.AppStrings;
import com.jpexs.decompiler.flash.gui.AsLinkageDialog;
import com.jpexs.decompiler.flash.gui.ClipboardType;
import com.jpexs.decompiler.flash.gui.CollectDepthAsSpritesDialog;
import com.jpexs.decompiler.flash.gui.ConvertPlaceObjectTypeDialog;
import com.jpexs.decompiler.flash.gui.ConvertShapeTypeDialog;
import com.jpexs.decompiler.flash.gui.Main;
import com.jpexs.decompiler.flash.gui.MainPanel;
import com.jpexs.decompiler.flash.gui.PathResolvingDialog;
import com.jpexs.decompiler.flash.gui.ReplaceCharacterDialog;
import com.jpexs.decompiler.flash.gui.SelectFramePositionDialog;
import com.jpexs.decompiler.flash.gui.SelectTagPositionDialog;
import com.jpexs.decompiler.flash.gui.TreeNodeType;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.gui.ViewMessages;
import com.jpexs.decompiler.flash.gui.abc.ABCExplorerDialog;
import com.jpexs.decompiler.flash.gui.abc.AddClassDialog;
import com.jpexs.decompiler.flash.gui.abc.As3ClassLinkageDialog;
import com.jpexs.decompiler.flash.gui.abc.ClassesListTreeModel;
import com.jpexs.decompiler.flash.gui.action.AddScriptDialog;
import com.jpexs.decompiler.flash.gui.soleditor.Cookie;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.packers.Packer;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.flash.tags.DefineBinaryDataTag;
import com.jpexs.decompiler.flash.tags.DefineBitsLossless2Tag;
import com.jpexs.decompiler.flash.tags.DefineButton2Tag;
import com.jpexs.decompiler.flash.tags.DefineFont3Tag;
import com.jpexs.decompiler.flash.tags.DefineMorphShape2Tag;
import com.jpexs.decompiler.flash.tags.DefineShape4Tag;
import com.jpexs.decompiler.flash.tags.DefineSoundTag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.DefineVideoStreamTag;
import com.jpexs.decompiler.flash.tags.DoABC2Tag;
import com.jpexs.decompiler.flash.tags.DoActionTag;
import com.jpexs.decompiler.flash.tags.DoInitActionTag;
import com.jpexs.decompiler.flash.tags.ExportAssetsTag;
import com.jpexs.decompiler.flash.tags.FileAttributesTag;
import com.jpexs.decompiler.flash.tags.FrameLabelTag;
import com.jpexs.decompiler.flash.tags.MetadataTag;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.PlaceObject3Tag;
import com.jpexs.decompiler.flash.tags.PlaceObject4Tag;
import com.jpexs.decompiler.flash.tags.PlaceObjectTag;
import com.jpexs.decompiler.flash.tags.RemoveObject2Tag;
import com.jpexs.decompiler.flash.tags.SetBackgroundColorTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.SoundStreamBlockTag;
import com.jpexs.decompiler.flash.tags.StartSound2Tag;
import com.jpexs.decompiler.flash.tags.StartSoundTag;
import com.jpexs.decompiler.flash.tags.SymbolClassTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.TagTypeInfo;
import com.jpexs.decompiler.flash.tags.UnknownTag;
import com.jpexs.decompiler.flash.tags.VideoFrameTag;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.flash.tags.base.BinaryDataInterface;
import com.jpexs.decompiler.flash.tags.base.ButtonTag;
import com.jpexs.decompiler.flash.tags.base.CharacterIdTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.DepthTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.MorphShapeTag;
import com.jpexs.decompiler.flash.tags.base.PackedBinaryData;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.tags.base.RemoveTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.tags.base.SoundTag;
import com.jpexs.decompiler.flash.tags.base.TextTag;
import com.jpexs.decompiler.flash.tags.converters.PlaceObjectTypeConverter;
import com.jpexs.decompiler.flash.tags.converters.ShapeTypeConverter;
import com.jpexs.decompiler.flash.tags.gfx.ExporterInfo;
import com.jpexs.decompiler.flash.timeline.AS2Package;
import com.jpexs.decompiler.flash.timeline.AS3Package;
import com.jpexs.decompiler.flash.timeline.DepthState;
import com.jpexs.decompiler.flash.timeline.Frame;
import com.jpexs.decompiler.flash.timeline.FrameScript;
import com.jpexs.decompiler.flash.timeline.Scene;
import com.jpexs.decompiler.flash.timeline.SceneFrame;
import com.jpexs.decompiler.flash.timeline.TagScript;
import com.jpexs.decompiler.flash.timeline.Timeline;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.treeitems.AS3ClassTreeItem;
import com.jpexs.decompiler.flash.treeitems.FolderItem;
import com.jpexs.decompiler.flash.treeitems.HeaderItem;
import com.jpexs.decompiler.flash.treeitems.Openable;
import com.jpexs.decompiler.flash.treeitems.OpenableList;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import com.jpexs.decompiler.flash.types.BUTTONCONDACTION;
import com.jpexs.decompiler.flash.types.BUTTONRECORD;
import com.jpexs.decompiler.flash.types.CLIPACTIONRECORD;
import com.jpexs.decompiler.flash.types.CLIPACTIONS;
import com.jpexs.decompiler.flash.types.CXFORMWITHALPHA;
import com.jpexs.decompiler.flash.types.HasCharacterId;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.LinkedIdentityHashSet;
import com.jpexs.helpers.Reference;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.TreePath;

/**
 * @author JPEXS
 */
public class TagTreeContextMenu extends JPopupMenu {

    private static final Logger logger = Logger.getLogger(TagTreeContextMenu.class.getName());

    private final MainPanel mainPanel;

    private JMenuItem gotoDocumentClassMenuItem;

    private JMenuItem configurePathResolvingMenuItem;

    private JMenuItem setAsLinkageMenuItem;

    private JMenuItem setAs3ClassLinkageMenuItem;

    private JMenuItem expandRecursiveMenuItem;

    private JMenuItem collapseRecursiveMenuItem;

    private JMenuItem removeMenuItem;

    private JMenuItem removeWithDependenciesMenuItem;

    private JMenuItem undoTagMenuItem;

    private JMenuItem exportSelectionMenuItem;

    private JMenuItem exportABCMenuItem;

    private JMenuItem replaceMenuItem;

    private JMenuItem replaceNoFillMenuItem;

    private JMenuItem replaceWithTagMenuItem;

    private JMenuItem replaceRefsWithTagMenuItem;

    private JMenuItem rawEditMenuItem;

    private JMenuItem jumpToCharacterMenuItem;

    private JMenuItem exportJavaSourceMenuItem;

    private JMenuItem exportFlaMenuItem;

    private JMenuItem exportFlashDevelopMenuItem;

    private JMenuItem exportIdeaMenuItem;

    private JMenuItem exportVsCodeMenuItem;

    private JMenuItem exportSwfXmlMenuItem;

    private JMenuItem importSwfXmlMenuItem;

    private JMenuItem importScriptsMenuItem;

    private JMenuItem importTextsMenuItem;

    private JMenuItem importImagesMenuItem;

    private JMenuItem importShapesMenuItem;

    private JMenuItem importShapesNoFillMenuItem;

    private JMenuItem importMoviesMenuItem;

    private JMenuItem importSoundsMenuItem;

    private JMenuItem importSymbolClassMenuItem;

    private JMenuItem closeMenuItem;

    private JMenu addTagInsideMenu;

    private JMenu attachTagMenu;

    private JMenu addTagBeforeMenu;

    private JMenu addTagAfterMenu;

    private JMenuItem cloneMenuItem;

    private JMenu moveTagToMenu;

    private JMenu moveTagToWithDependenciesMenu;

    private JMenu moveFrameToMenu;

    private JMenuItem moveUpMenuItem;

    private JMenuItem moveDownMenuItem;

    private JMenu copyTagToMenu;

    private JMenu copyTagToWithDependenciesMenu;

    private JMenu copyTagToReplaceByClassMenu;

    private JMenu copyFrameToMenu;

    private JMenuItem copyFrameToClipboardMenuItem;

    private JMenuItem cutTagToClipboardMenuItem;

    private JMenuItem cutTagToClipboardWithDependenciesMenuItem;

    private JMenuItem cutFrameToClipboardMenuItem;

    private JMenuItem pasteBeforeMenuItem;

    private JMenuItem pasteAfterMenuItem;

    private JMenuItem pasteInsideMenuItem;

    private JMenu applyUnpackerMenu;

    private JMenuItem openSWFInsideTagMenuItem;

    private JMenuItem addAs12ScriptMenuItem;

    private JMenuItem addAs12FrameScriptMenuItem;

    private JMenuItem addAs12ButtonEventScriptMenuItem;

    private JMenuItem addAs12InstanceEventScriptMenuItem;

    private JMenuItem addAs12SpriteInitScriptMenuItem;

    private JMenuItem addAs3ClassMenuItem;

    private JMenuItem textSearchMenuItem;

    private JMenuItem moveTagMenuItem;

    private JMenuItem showInResourcesViewTagMenuItem;

    private JMenuItem showInTagListViewTagMenuItem;

    private JMenuItem showInHexDumpViewTagMenuItem;

    private JMenuItem showInFramesFolderMenuItem;

    private JMenuItem addFramesMenuItem;

    private JMenuItem addFramesBeforeMenuItem;

    private JMenuItem addFramesAfterMenuItem;

    private JMenu changeCharsetMenu;

    private JMenuItem pinMenuItem;

    private JMenuItem unpinMenuItem;

    private JMenuItem unpinAllMenuItem;

    private JMenuItem unpinOthersMenuItem;

    private JMenuItem abcExplorerMenuItem;

    private JMenuItem cleanAbcMenuItem;

    private JMenuItem replaceWithGifMenuItem;

    private JMenuItem collectDepthAsSpritesMenuItem;

    private JMenuItem convertShapeTypeMenuItem;

    private JMenuItem convertPlaceObjectTypeMenuItem;

    private List<TreeItem> items = new ArrayList<>();

    private static final int KIND_TAG_MOVETO = 0;
    private static final int KIND_TAG_MOVETODEPS = 1;
    private static final int KIND_TAG_COPYTO = 2;
    private static final int KIND_TAG_COPYTODEPS = 3;
    private static final int KIND_FRAME_MOVETO = 4;
    private static final int KIND_FRAME_COPYTO = 5;
    private static final int KIND_TAG_COPYTOCLASSOREXPORTNAME = 6;

    private TreeItem getCurrentItem() {
        if (items.isEmpty()) {
            return null;
        }
        return items.get(0);
    }

    private List<TreeItem> getSelectedItems() {
        return items;
    }

    public TagTreeContextMenu(final List<AbstractTagTree> trees, MainPanel mainPanel) {
        this.mainPanel = mainPanel;

        expandRecursiveMenuItem = new JMenuItem(mainPanel.translate("contextmenu.expandAll"));
        expandRecursiveMenuItem.addActionListener(this::expandRecursiveActionPerformed);
        expandRecursiveMenuItem.setIcon(View.getIcon("expand16"));
        add(expandRecursiveMenuItem);

        collapseRecursiveMenuItem = new JMenuItem(mainPanel.translate("contextmenu.collapseAll"));
        collapseRecursiveMenuItem.addActionListener(this::collapseRecursiveActionPerformed);
        collapseRecursiveMenuItem.setIcon(View.getIcon("collapse16"));
        add(collapseRecursiveMenuItem);

        addSeparator();

        changeCharsetMenu = new JMenu();
        JMenu currentCharsetMenu = changeCharsetMenu;
        int charsetCnt = 0;
        for (String charsetStr : Utf8Helper.getAllowedCharsets()) {
            if (charsetCnt == 30) {
                JMenu moreMenu = new JMenu(mainPanel.translate("contextmenu.more"));
                currentCharsetMenu.add(moreMenu);
                currentCharsetMenu = moreMenu;
                charsetCnt = 0;
            }
            JMenuItem charsetMenuItem = new JMenuItem(charsetStr);
            charsetMenuItem.addActionListener(this::changeCharsetActionPerformed);
            currentCharsetMenu.add(charsetMenuItem);
            charsetCnt++;
        }
        add(changeCharsetMenu);

        configurePathResolvingMenuItem = new JMenuItem(mainPanel.translate("contextmenu.configurePathResolving"));
        configurePathResolvingMenuItem.addActionListener(this::configurePathResolvingActionPerformed);
        configurePathResolvingMenuItem.setIcon(View.getIcon("settings16"));
        add(configurePathResolvingMenuItem);

        addSeparator();
        jumpToCharacterMenuItem = new JMenuItem(mainPanel.translate("contextmenu.jumpToCharacter"));
        jumpToCharacterMenuItem.addActionListener(this::jumpToCharacterActionPerformed);
        jumpToCharacterMenuItem.setIcon(View.getIcon("jumpto16"));
        add(jumpToCharacterMenuItem);

        showInFramesFolderMenuItem = new JMenuItem(mainPanel.translate("contextmenu.showInFramesFolder"));
        showInFramesFolderMenuItem.addActionListener(this::showInFramesFolderActionPerformed);
        showInFramesFolderMenuItem.setIcon(View.getIcon("frame16"));
        add(showInFramesFolderMenuItem);

        showInResourcesViewTagMenuItem = new JMenuItem(mainPanel.translate("contextmenu.showInResources"));
        showInResourcesViewTagMenuItem.addActionListener(this::showInResourcesViewActionPerformed);
        showInResourcesViewTagMenuItem.setIcon(View.getIcon("folder16"));
        add(showInResourcesViewTagMenuItem);

        showInTagListViewTagMenuItem = new JMenuItem(mainPanel.translate("contextmenu.showInTagList"));
        showInTagListViewTagMenuItem.addActionListener(this::showInTagListViewActionPerformed);
        showInTagListViewTagMenuItem.setIcon(View.getIcon("taglist16"));
        add(showInTagListViewTagMenuItem);

        showInHexDumpViewTagMenuItem = new JMenuItem(mainPanel.translate("contextmenu.showInHexDump"));
        showInHexDumpViewTagMenuItem.addActionListener(this::showInHexDumpViewActionPerformed);
        showInHexDumpViewTagMenuItem.setIcon(View.getIcon("viewhex16"));
        add(showInHexDumpViewTagMenuItem);

        textSearchMenuItem = new JMenuItem(mainPanel.translate("menu.tools.search"));
        textSearchMenuItem.addActionListener(this::textSearchActionPerformed);
        textSearchMenuItem.setIcon(View.getIcon("search16"));
        add(textSearchMenuItem);

        addSeparator();

        exportSelectionMenuItem = new JMenuItem(mainPanel.translate("menu.file.export.selection"));
        exportSelectionMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainPanel.exportSelectionActionPerformed(getSelectedItems());
            }
        });
        exportSelectionMenuItem.setIcon(View.getIcon("exportsel16"));
        add(exportSelectionMenuItem);

        exportABCMenuItem = new JMenuItem(mainPanel.translate("contextmenu.exportAbc"));
        exportABCMenuItem.addActionListener(this::exportABCActionPerformed);
        exportABCMenuItem.setIcon(View.getIcon("exportabc16"));
        add(exportABCMenuItem);

        exportFlaMenuItem = new JMenuItem(mainPanel.translate("contextmenu.exportFla"));
        exportFlaMenuItem.addActionListener(this::exportFlaActionPerformed);
        exportFlaMenuItem.setIcon(View.getIcon("exportfla16"));
        add(exportFlaMenuItem);

        exportFlashDevelopMenuItem = new JMenuItem(mainPanel.translate("contextmenu.exportFlashDevelop"));
        exportFlashDevelopMenuItem.addActionListener(this::exportFlashDevelopActionPerformed);
        exportFlashDevelopMenuItem.setIcon(View.getIcon("exportflashdevelop16"));
        add(exportFlashDevelopMenuItem);

        exportIdeaMenuItem = new JMenuItem(mainPanel.translate("contextmenu.exportIdea"));
        exportIdeaMenuItem.addActionListener(this::exportIdeaActionPerformed);
        exportIdeaMenuItem.setIcon(View.getIcon("exportidea16"));
        add(exportIdeaMenuItem);

        exportVsCodeMenuItem = new JMenuItem(mainPanel.translate("contextmenu.exportVsCode"));
        exportVsCodeMenuItem.addActionListener(this::exportVsCodeActionPerformed);
        exportVsCodeMenuItem.setIcon(View.getIcon("exportvscode16"));
        add(exportVsCodeMenuItem);

        exportJavaSourceMenuItem = new JMenuItem(mainPanel.translate("contextmenu.exportJavaSource"));
        exportJavaSourceMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainPanel.exportJavaSourceActionPerformed(getSelectedItems());
            }
        });
        exportJavaSourceMenuItem.setIcon(View.getIcon("exportjava16"));
        add(exportJavaSourceMenuItem);

        exportSwfXmlMenuItem = new JMenuItem(mainPanel.translate("contextmenu.exportSwfXml"));
        exportSwfXmlMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainPanel.exportSwfXmlActionPerformed(getSelectedItems());
            }
        });
        exportSwfXmlMenuItem.setIcon(View.getIcon("exportxml16"));
        add(exportSwfXmlMenuItem);

        addSeparator();

        rawEditMenuItem = new JMenuItem(mainPanel.translate("contextmenu.rawEdit"));
        rawEditMenuItem.addActionListener(this::rawEditActionPerformed);
        rawEditMenuItem.setIcon(View.getIcon("rawedit16"));
        add(rawEditMenuItem);

        undoTagMenuItem = new JMenuItem(mainPanel.translate("contextmenu.undo"));
        undoTagMenuItem.addActionListener(this::undoTagActionPerformed);
        undoTagMenuItem.setIcon(View.getIcon("undo16"));
        add(undoTagMenuItem);

        replaceMenuItem = new JMenuItem(mainPanel.translate("button.replace"));
        replaceMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainPanel.replaceButtonActionPerformed(getSelectedItems());
            }
        });
        replaceMenuItem.setIcon(View.getIcon("replaceitem16"));
        add(replaceMenuItem);

        replaceNoFillMenuItem = new JMenuItem(mainPanel.translate("button.replaceNoFill"));
        replaceNoFillMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainPanel.replaceNoFillButtonActionPerformed(getSelectedItems());
            }
        });
        replaceNoFillMenuItem.setIcon(View.getIcon("replaceitem16"));
        add(replaceNoFillMenuItem);

        replaceWithTagMenuItem = new JMenuItem(mainPanel.translate("button.replaceWithTag"));
        replaceWithTagMenuItem.addActionListener(this::replaceWithTagActionPerformed);
        replaceWithTagMenuItem.setIcon(View.getIcon("replacewithtag16"));
        add(replaceWithTagMenuItem);

        replaceWithGifMenuItem = new JMenuItem(mainPanel.translate("button.replaceWithGif"));
        replaceWithGifMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainPanel.replaceSpriteWithGifButtonActionPerformed(getCurrentItem());
            }
        });
        replaceWithGifMenuItem.setIcon(View.getIcon("replacesprite16"));
        add(replaceWithGifMenuItem);

        replaceRefsWithTagMenuItem = new JMenuItem(mainPanel.translate("button.replaceRefs"));
        replaceRefsWithTagMenuItem.addActionListener(this::replaceRefsWithTagActionPerformed);
        replaceRefsWithTagMenuItem.setIcon(View.getIcon("replacewithtag16"));
        add(replaceRefsWithTagMenuItem);

        convertShapeTypeMenuItem = new JMenuItem(mainPanel.translate("contextmenu.convertShapeType"));
        convertShapeTypeMenuItem.addActionListener(this::convertShapeTypeActionPerformed);
        convertShapeTypeMenuItem.setIcon(View.getIcon("shape16"));
        add(convertShapeTypeMenuItem);

        convertPlaceObjectTypeMenuItem = new JMenuItem(mainPanel.translate("contextmenu.convertPlaceObjectType"));
        convertPlaceObjectTypeMenuItem.addActionListener(this::convertPlaceObjectTypeActionPerformed);
        convertPlaceObjectTypeMenuItem.setIcon(View.getIcon("placeobject16"));
        add(convertPlaceObjectTypeMenuItem);

        addSeparator();

        gotoDocumentClassMenuItem = new JMenuItem(mainPanel.translate("menu.tools.gotoDocumentClass"));
        gotoDocumentClassMenuItem.addActionListener(this::gotoDocumentClassActionPerformed);
        gotoDocumentClassMenuItem.setIcon(View.getIcon("gotomainclass16"));
        add(gotoDocumentClassMenuItem);

        setAsLinkageMenuItem = new JMenuItem(mainPanel.translate("contextmenu.setAsLinkage"));
        setAsLinkageMenuItem.addActionListener(this::setAsLinkageActionPerformed);
        setAsLinkageMenuItem.setIcon(View.getIcon("asclass16"));
        add(setAsLinkageMenuItem);

        setAs3ClassLinkageMenuItem = new JMenuItem(mainPanel.translate("contextmenu.setAs3ClassLinkage"));
        setAs3ClassLinkageMenuItem.addActionListener(this::setAs3ClassLinkageActionPerformed);
        setAs3ClassLinkageMenuItem.setIcon(View.getIcon("asclass16"));
        add(setAs3ClassLinkageMenuItem);

        abcExplorerMenuItem = new JMenuItem(mainPanel.translate("contextmenu.abcexplorer"));
        abcExplorerMenuItem.addActionListener(this::abcExplorerActionPerformed);
        abcExplorerMenuItem.setIcon(View.getIcon("abcexplorer16"));
        add(abcExplorerMenuItem);

        cleanAbcMenuItem = new JMenuItem(mainPanel.translate("contextmenu.cleanAbc"));
        cleanAbcMenuItem.addActionListener(this::cleanAbcActionPerformed);
        cleanAbcMenuItem.setIcon(View.getIcon("clean16"));
        add(cleanAbcMenuItem);

        addAs12ScriptMenuItem = new JMenuItem(mainPanel.translate("contextmenu.addScript"));
        addAs12ScriptMenuItem.addActionListener(this::addAs12ScriptActionPerformed);
        addAs12ScriptMenuItem.setIcon(View.getIcon("scriptadd16"));
        add(addAs12ScriptMenuItem);

        addAs12FrameScriptMenuItem = new JMenuItem(mainPanel.translate("contextmenu.addScript.doaction"));
        addAs12FrameScriptMenuItem.addActionListener(this::addAs12FrameScriptActionPerformed);
        addAs12FrameScriptMenuItem.setIcon(View.getIcon("scriptadd16"));
        add(addAs12FrameScriptMenuItem);

        addAs12ButtonEventScriptMenuItem = new JMenuItem(mainPanel.translate("contextmenu.addScript.buttoncondaction"));
        addAs12ButtonEventScriptMenuItem.addActionListener(this::addAs12ButtonEventScriptActionPerformed);
        addAs12ButtonEventScriptMenuItem.setIcon(View.getIcon("scriptadd16"));
        add(addAs12ButtonEventScriptMenuItem);

        addAs12InstanceEventScriptMenuItem = new JMenuItem(mainPanel.translate("contextmenu.addScript.clipactionrecord"));
        addAs12InstanceEventScriptMenuItem.addActionListener(this::addAs12InstanceEventScriptActionPerformed);
        addAs12InstanceEventScriptMenuItem.setIcon(View.getIcon("scriptadd16"));
        add(addAs12InstanceEventScriptMenuItem);

        addAs12SpriteInitScriptMenuItem = new JMenuItem(mainPanel.translate("contextmenu.addScript.doinitaction"));
        addAs12SpriteInitScriptMenuItem.addActionListener(this::addAs12SpriteInitScriptActionPerformed);
        addAs12SpriteInitScriptMenuItem.setIcon(View.getIcon("scriptadd16"));
        add(addAs12SpriteInitScriptMenuItem);

        addAs3ClassMenuItem = new JMenuItem(mainPanel.translate("contextmenu.addClass"));
        addAs3ClassMenuItem.addActionListener(this::addAs3ClassActionPerformed);
        addAs3ClassMenuItem.setIcon(View.getIcon("scriptadd16"));
        add(addAs3ClassMenuItem);

        addSeparator();

        importSwfXmlMenuItem = new JMenuItem(mainPanel.translate("contextmenu.importSwfXml"));
        importSwfXmlMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainPanel.importSwfXmlActionPerformed(getSelectedItems());
            }
        });
        importSwfXmlMenuItem.setIcon(View.getIcon("importxml16"));
        add(importSwfXmlMenuItem);

        importScriptsMenuItem = new JMenuItem(mainPanel.translate("menu.file.import.script"));
        importScriptsMenuItem.addActionListener(this::importScriptsActionPerformed);
        importScriptsMenuItem.setIcon(View.getIcon("importscript16"));
        add(importScriptsMenuItem);

        importTextsMenuItem = new JMenuItem(mainPanel.translate("menu.file.import.text"));
        importTextsMenuItem.addActionListener(this::importTextsActionPerformed);
        importTextsMenuItem.setIcon(View.getIcon("importtext16"));
        add(importTextsMenuItem);

        importImagesMenuItem = new JMenuItem(mainPanel.translate("menu.file.import.image"));
        importImagesMenuItem.addActionListener(this::importImagesActionPerformed);
        importImagesMenuItem.setIcon(View.getIcon("importimage16"));
        add(importImagesMenuItem);

        importShapesMenuItem = new JMenuItem(mainPanel.translate("menu.file.import.shape"));
        importShapesMenuItem.addActionListener(this::importShapesActionPerformed);
        importShapesMenuItem.setIcon(View.getIcon("importshape16"));
        add(importShapesMenuItem);

        importShapesNoFillMenuItem = new JMenuItem(mainPanel.translate("menu.file.import.shapeNoFill"));
        importShapesNoFillMenuItem.addActionListener(this::importShapesNoFillActionPerformed);
        importShapesNoFillMenuItem.setIcon(View.getIcon("importshape16"));
        add(importShapesNoFillMenuItem);

        importMoviesMenuItem = new JMenuItem(mainPanel.translate("menu.file.import.movie"));
        importMoviesMenuItem.addActionListener(this::importMoviesActionPerformed);
        importMoviesMenuItem.setIcon(View.getIcon("importmovie16"));
        add(importMoviesMenuItem);

        importSoundsMenuItem = new JMenuItem(mainPanel.translate("menu.file.import.sound"));
        importSoundsMenuItem.addActionListener(this::importSoundsActionPerformed);
        importSoundsMenuItem.setIcon(View.getIcon("importsound16"));
        add(importSoundsMenuItem);

        importSymbolClassMenuItem = new JMenuItem(mainPanel.translate("menu.file.import.symbolClass"));
        importSymbolClassMenuItem.addActionListener(this::importSymbolClassActionPerformed);
        importSymbolClassMenuItem.setIcon(View.getIcon("importsymbolclass16"));
        add(importSymbolClassMenuItem);

        addSeparator();

        removeMenuItem = new JMenuItem(mainPanel.translate("contextmenu.remove") + " (DEL)");
        removeMenuItem.addActionListener((ActionEvent e) -> {
            removeItemActionPerformed(e, false);
        });
        removeMenuItem.setIcon(View.getIcon("remove16"));
        add(removeMenuItem);

        removeWithDependenciesMenuItem = new JMenuItem(mainPanel.translate("contextmenu.removeWithDependencies") + " (SHIFT+DEL)");
        removeWithDependenciesMenuItem.addActionListener((ActionEvent e) -> {
            removeItemActionPerformed(e, true);
        });
        removeWithDependenciesMenuItem.setIcon(View.getIcon("remove16"));
        add(removeWithDependenciesMenuItem);

        addSeparator();

        addTagInsideMenu = new JMenu(mainPanel.translate("contextmenu.addTagInside"));
        addTagInsideMenu.setIcon(View.getIcon("addtag16"));
        add(addTagInsideMenu);

        attachTagMenu = new JMenu(mainPanel.translate("contextmenu.attachTag"));
        attachTagMenu.setIcon(View.getIcon("addtag16"));
        add(attachTagMenu);

        addTagBeforeMenu = new JMenu(mainPanel.translate("contextmenu.addTagBefore"));
        addTagBeforeMenu.setIcon(View.getIcon("addtag16"));
        add(addTagBeforeMenu);

        addTagAfterMenu = new JMenu(mainPanel.translate("contextmenu.addTagAfter"));
        addTagAfterMenu.setIcon(View.getIcon("addtag16"));
        add(addTagAfterMenu);

        cloneMenuItem = new JMenuItem(mainPanel.translate("contextmenu.clone"));
        cloneMenuItem.addActionListener(this::cloneActionPerformed);
        cloneMenuItem.setIcon(View.getIcon("copy16"));
        add(cloneMenuItem);

        moveTagMenuItem = new JMenuItem(mainPanel.translate("contextmenu.moveTagAround"));
        moveTagMenuItem.addActionListener(this::moveTagActionPerformed);
        moveTagMenuItem.setIcon(View.getIcon("move16"));
        add(moveTagMenuItem);

        moveTagToMenu = new JMenu(mainPanel.translate("contextmenu.moveTag"));
        moveTagToMenu.setIcon(View.getIcon("move16"));
        add(moveTagToMenu);

        moveTagToWithDependenciesMenu = new JMenu(mainPanel.translate("contextmenu.moveTagWithDependencies"));
        moveTagToWithDependenciesMenu.setIcon(View.getIcon("move16"));
        add(moveTagToWithDependenciesMenu);

        moveFrameToMenu = new JMenu(mainPanel.translate("contextmenu.moveFrame"));
        moveFrameToMenu.setIcon(View.getIcon("move16"));
        add(moveFrameToMenu);

        moveUpMenuItem = new JMenuItem(mainPanel.translate("contextmenu.moveUp") + " (ALT + UP)");
        moveUpMenuItem.setIcon(View.getIcon("arrowup16"));
        moveUpMenuItem.addActionListener(this::moveUpActionPerformed);
        add(moveUpMenuItem);

        moveDownMenuItem = new JMenuItem(mainPanel.translate("contextmenu.moveDown") + " (ALT + DOWN)");
        moveDownMenuItem.setIcon(View.getIcon("arrowdown16"));
        moveDownMenuItem.addActionListener(this::moveDownActionPerformed);
        add(moveDownMenuItem);

        copyTagToMenu = new JMenu(mainPanel.translate("contextmenu.copyTag"));
        copyTagToMenu.setIcon(View.getIcon("copy16"));
        add(copyTagToMenu);

        copyTagToWithDependenciesMenu = new JMenu(mainPanel.translate("contextmenu.copyTagWithDependencies"));
        copyTagToWithDependenciesMenu.setIcon(View.getIcon("copy16"));
        add(copyTagToWithDependenciesMenu);

        copyTagToReplaceByClassMenu = new JMenu(mainPanel.translate("contextmenu.copyTagToReplaceByClass"));
        copyTagToReplaceByClassMenu.setIcon(View.getIcon("copy16"));
        add(copyTagToReplaceByClassMenu);

        copyFrameToMenu = new JMenu(mainPanel.translate("contextmenu.copyFrame"));
        copyFrameToMenu.setIcon(View.getIcon("copy16"));
        add(copyFrameToMenu);

        copyFrameToClipboardMenuItem = new JMenuItem(mainPanel.translate("contextmenu.copyFrame.clipboard") + " (CTRL+C)");
        copyFrameToClipboardMenuItem.setIcon(View.getIcon("copy16"));
        copyFrameToClipboardMenuItem.addActionListener(this::copyTagOrFrameToClipboardActionPerformed);
        add(copyFrameToClipboardMenuItem);

        cutTagToClipboardMenuItem = new JMenuItem(mainPanel.translate("contextmenu.cutTag") + " (CTRL+X)");
        cutTagToClipboardMenuItem.setIcon(View.getIcon("cut16"));
        cutTagToClipboardMenuItem.addActionListener(this::cutTagOrFrameToClipboardActionPerformed);
        add(cutTagToClipboardMenuItem);

        cutTagToClipboardWithDependenciesMenuItem = new JMenuItem(mainPanel.translate("contextmenu.cutTagWithDependencies") + " (CTRL+SHIFT+X)");
        cutTagToClipboardWithDependenciesMenuItem.setIcon(View.getIcon("cut16"));
        cutTagToClipboardWithDependenciesMenuItem.addActionListener(this::cutTagToClipboardWithDependenciesActionPerformed);
        add(cutTagToClipboardWithDependenciesMenuItem);

        cutFrameToClipboardMenuItem = new JMenuItem(mainPanel.translate("contextmenu.cutFrame") + " (CTRL+X)");
        cutFrameToClipboardMenuItem.setIcon(View.getIcon("cut16"));
        cutFrameToClipboardMenuItem.addActionListener(this::cutTagOrFrameToClipboardActionPerformed);
        add(cutFrameToClipboardMenuItem);

        pasteBeforeMenuItem = new JMenuItem(mainPanel.translate("contextmenu.pasteBefore") + " (CTRL+V)");
        pasteBeforeMenuItem.setIcon(View.getIcon("paste16"));
        pasteBeforeMenuItem.addActionListener(this::pasteBeforeActionPerformed);
        add(pasteBeforeMenuItem);

        pasteAfterMenuItem = new JMenuItem(mainPanel.translate("contextmenu.pasteAfter") + " (CTRL+SHIFT+V)");
        pasteAfterMenuItem.setIcon(View.getIcon("paste16"));
        pasteAfterMenuItem.addActionListener(this::pasteAfterActionPerformed);
        add(pasteAfterMenuItem);

        pasteInsideMenuItem = new JMenuItem(mainPanel.translate("contextmenu.pasteInside"));
        pasteInsideMenuItem.setIcon(View.getIcon("paste16"));
        pasteInsideMenuItem.addActionListener(this::pasteInsideActionPerformed);
        add(pasteInsideMenuItem);

        collectDepthAsSpritesMenuItem = new JMenuItem(mainPanel.translate("contextmenu.collectDepthAsSprites"));
        collectDepthAsSpritesMenuItem.setIcon(View.getIcon("sprite16"));
        collectDepthAsSpritesMenuItem.addActionListener(this::collectDepthAsSprites);
        add(collectDepthAsSpritesMenuItem);

        addSeparator();

        applyUnpackerMenu = new JMenu(mainPanel.translate("contextmenu.applyUnpacker"));
        applyUnpackerMenu.setIcon(View.getIcon("openinside16"));

        for (Packer packer : DefineBinaryDataTag.getAvailablePackers()) {
            JMenuItem packerMenuItem = new JMenuItem(packer.getName());
            packerMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    applyUnpackerActionPerformed(packer);
                }
            });
            applyUnpackerMenu.add(packerMenuItem);
        }

        add(applyUnpackerMenu);

        openSWFInsideTagMenuItem = new JMenuItem(mainPanel.translate("contextmenu.openswfinside"));
        openSWFInsideTagMenuItem.setIcon(View.getIcon("openinside16"));
        openSWFInsideTagMenuItem.addActionListener(this::openSwfInsideActionPerformed);
        add(openSWFInsideTagMenuItem);

        addSeparator();

        addFramesMenuItem = new JMenuItem(mainPanel.translate("contextmenu.addFrames"));
        addFramesMenuItem.addActionListener(this::addFramesActionPerformed);
        addFramesMenuItem.setIcon(View.getIcon("frameadd16"));
        add(addFramesMenuItem);

        addFramesBeforeMenuItem = new JMenuItem(mainPanel.translate("contextmenu.addFramesBefore"));
        addFramesBeforeMenuItem.addActionListener(this::addFramesBeforeActionPerformed);
        addFramesBeforeMenuItem.setIcon(View.getIcon("frameadd16"));
        add(addFramesBeforeMenuItem);

        addFramesAfterMenuItem = new JMenuItem(mainPanel.translate("contextmenu.addFramesAfter"));
        addFramesAfterMenuItem.addActionListener(this::addFramesAfterActionPerformed);
        addFramesAfterMenuItem.setIcon(View.getIcon("frameadd16"));
        add(addFramesAfterMenuItem);
        addSeparator();

        pinMenuItem = new JMenuItem(AppStrings.translate("contextmenu.pin"));
        pinMenuItem.setIcon(View.getIcon("pinned16"));
        pinMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainPanel.pinItem(getCurrentItem());
            }
        });
        add(pinMenuItem);

        unpinMenuItem = new JMenuItem(AppStrings.translate("contextmenu.unpin"));
        unpinMenuItem.setIcon(View.getIcon("pin16"));
        unpinMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainPanel.unpinItem(getCurrentItem());
            }
        });
        add(unpinMenuItem);

        unpinAllMenuItem = new JMenuItem(AppStrings.translate("contextmenu.unpin.all"));
        unpinAllMenuItem.setIcon(View.getIcon("pin16"));
        unpinAllMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainPanel.destroyPins();
            }
        });
        add(unpinAllMenuItem);

        unpinOthersMenuItem = new JMenuItem(AppStrings.translate("contextmenu.unpin.others"));
        unpinOthersMenuItem.setIcon(View.getIcon("pin16"));
        unpinOthersMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainPanel.unpinOthers(getCurrentItem());
            }
        });
        add(unpinOthersMenuItem);

        addSeparator();

        closeMenuItem = new JMenuItem(mainPanel.translate("contextmenu.closeSwf"));
        closeMenuItem.addActionListener(this::closeSwfActionPerformed);
        closeMenuItem.setIcon(View.getIcon("close16"));
        add(closeMenuItem);

        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    if (mainPanel.checkEdited()) {
                        return;
                    }
                    int row = getTree().getClosestRowForLocation(e.getX(), e.getY());
                    int[] selectionRows = getTree().getSelectionRows();
                    if (!Helper.contains(selectionRows, row)) {
                        getTree().setSelectionRow(row);
                    }

                    TreePath[] paths = getTree().getSelectionPathsSorted();
                    if (paths == null || paths.length == 0) {
                        return;
                    }

                    List<TreeItem> li = new ArrayList<>();
                    for (TreePath treePath : paths) {
                        TreeItem item = (TreeItem) treePath.getLastPathComponent();
                        li.add(item);
                    }

                    update(li);
                    show(e.getComponent(), e.getX(), e.getY());
                }
            }
        };
        for (AbstractTagTree tree : trees) {
            tree.addMouseListener(adapter);
        }
    }

    private AbstractTagTree getTree() {
        return mainPanel.getCurrentTree();
    }

    public boolean canRemove(final List<TreeItem> items) {
        boolean wasNotFrame = false;
        boolean wasFrame = false;
        Timelined frameTimelined = null;
        for (TreeItem item : items) {
            if (item instanceof Cookie) {
                if (wasFrame) {
                    return false;
                }
                wasNotFrame = true;
            } else if (item instanceof Tag) {
                if (wasFrame) {
                    return false;
                }
                if (((Tag) item).isReadOnly()) {
                    return false;
                }
                wasNotFrame = true;
            } else if (item instanceof Frame) {
                Frame frame = (Frame) item;
                if (frame.timeline.timelined instanceof DefineSpriteTag) {
                    if (((Tag) frame.timeline.timelined).isReadOnly()) {
                        return false;
                    }
                }
                if (wasNotFrame) {
                    return false;
                }
                if (frameTimelined != null && frame.timeline.timelined != frameTimelined) {
                    return false;
                }
                frameTimelined = frame.timeline.timelined;
                wasFrame = true;
            } else {
                if (wasFrame) {
                    return false;
                }
                wasNotFrame = true;
                if (item instanceof TagScript) {
                    if (((TagScript) item).getTag().isReadOnly()) {
                        return false;
                    }
                    continue;
                }

                if (item instanceof AS2Package) {
                    continue;
                }

                if (item instanceof CLIPACTIONRECORD) {
                    continue;
                }
                if (item instanceof BUTTONCONDACTION) {
                    continue;
                }

                if (item instanceof ScriptPack) {
                    ScriptPack pack = (ScriptPack) item;
                    if (pack.isSimple) {
                        continue;
                    }
                }
                if (item instanceof AS3Package) {
                    AS3Package pkg = (AS3Package) item;
                    if (pkg.isCompoundScript() || !pkg.isPartOfCompoundScript()) {
                        continue;
                    }
                }

                if (item instanceof FrameScript) {
                    continue;
                }

                if (item instanceof BUTTONRECORD) {
                    continue;
                }

                return false;
            }
        }
        return true;
    }

    public void update(final List<TreeItem> items) {

        if (items.isEmpty()) {
            return;
        }

        this.items = items;

        AbstractTagTree tree = getTree();

        final List<OpenableList> swfs = mainPanel.getSwfs();

        boolean canRemove = canRemove(items);
        boolean allDoNotHaveDependencies = true;
        for (TreeItem item : items) {
            if (!(item instanceof Tag) && !(item instanceof Frame)) {
                if (item instanceof TagScript) {
                    Tag tag = ((TagScript) item).getTag();
                    if (tag instanceof DoActionTag || tag instanceof DoInitActionTag) {
                        allDoNotHaveDependencies = false;
                        break;
                    }
                    continue;
                }

                if (item instanceof AS2Package) {
                    allDoNotHaveDependencies = false;
                    break;
                }
            } else {
                allDoNotHaveDependencies = false;
                break;
            }
        }

        boolean hasScripts = false;
        boolean hasTexts = false;
        for (TreeItem item : items) {
            if (item instanceof SWF) {
                hasScripts = true;
                hasTexts = true;
                break;
            }
            if (item instanceof ASMSource) {
                hasScripts = true;
            }
            if (item instanceof TagScript) {
                hasScripts = true;
            }
            if (item instanceof FrameScript) {
                hasScripts = true;
            }
            if (item instanceof AS2Package) {
                hasScripts = true;
            }
            if (item instanceof ScriptPack) {
                hasScripts = true;
            }
            if (item instanceof AS3Package) {
                hasScripts = true;
            }
            if (item instanceof TextTag) {
                hasTexts = true;
            }
            if (item instanceof FolderItem) {
                FolderItem f = (FolderItem) item;
                if (f.getName().equals(TagTreeModel.FOLDER_SCRIPTS)) {
                    hasScripts = true;
                }
                if (f.getName().equals(TagTreeModel.FOLDER_TEXTS)) {
                    hasTexts = true;
                }
            }
        }

        boolean allSelectedIsTag = true;
        boolean allSelectedIsWritable = true;
        boolean allSelectedIsNotImported = true;
        boolean allSelectedIsFrameInSameTimeline = true;
        boolean allSelectedIsTagWithClassName = true;
        boolean allSelectedIsTagWithExportName = true;
        Timelined tim = null;
        for (TreeItem item : items) {
            if (!(item instanceof Frame)) {
                allSelectedIsFrameInSameTimeline = false;
                break;
            }
            Frame fr = (Frame) item;
            if (tim != null && tim != fr.timeline.timelined) {
                allSelectedIsFrameInSameTimeline = false;
                break;
            }
            tim = fr.timeline.timelined;
        }

        boolean allSelectedIsShape = true;
        boolean allSelectedIsPlaceObject = true;

        if (items.isEmpty()) {
            allSelectedIsShape = false;
            allSelectedIsPlaceObject = false;
        }

        for (TreeItem item : items) {

            if (!(item instanceof ShapeTag)) {
                allSelectedIsShape = false;
            }
            if (!(item instanceof PlaceObjectTypeTag)) {
                allSelectedIsPlaceObject = false;
            }

            if (item instanceof Tag) {
                Tag tag = (Tag) item;
                if (tag.isReadOnly()) {
                    allSelectedIsWritable = false;
                }
                if (tag.isImported()) {
                    allSelectedIsNotImported = false;
                }
                if (tag instanceof CharacterTag) {
                    CharacterTag chtag = (CharacterTag) tag;
                    if (chtag.getClassNames().isEmpty()) {
                        allSelectedIsTagWithClassName = false;
                    }
                    if (chtag.getExportName() == null) {
                        allSelectedIsTagWithExportName = false;
                    }

                }
            } else {
                if (item instanceof TagScript) {
                    Tag tag = ((TagScript) item).getTag();
                    if (tag.isReadOnly()) {
                        allSelectedIsWritable = false;
                    }
                    if (tag.isImported()) {
                        allSelectedIsNotImported = false;
                    }
                    if (tag instanceof DoActionTag || tag instanceof DoInitActionTag) {
                        continue;
                    }
                }

                allSelectedIsTag = false;
                allSelectedIsTagWithClassName = false;
                allSelectedIsTagWithExportName = false;
                break;
            }
        }

        boolean allSelectedIsTagOrFrame = true;
        for (TreeItem item : items) {
            if (!(item instanceof Tag)) {
                if (item instanceof TagScript) {
                    Tag tag = ((TagScript) item).getTag();
                    if (tag instanceof DoActionTag || tag instanceof DoInitActionTag) {
                        continue;
                    }
                } else if (item instanceof Frame) {
                    continue;
                }

                allSelectedIsTagOrFrame = false;
                break;
            }
        }

        boolean allSelectedIsBinaryData = true;
        for (TreeItem item : items) {
            if (!(item instanceof BinaryDataInterface)) {
                allSelectedIsBinaryData = false;
                break;
            }
        }

        boolean allSelectedIsSwf = true;
        for (TreeItem item : items) {
            if (!(item instanceof SWF) && !(item instanceof OpenableList)) {
                allSelectedIsSwf = false;
                break;
            } else if (item instanceof SWF) {
                SWF swf = (SWF) item;
                // Do not allow to close SWF in bundle
                if (swf.openableList != null && swf.openableList.isBundle()) {
                    allSelectedIsSwf = false;
                }
            }
        }

        boolean allSelectedIsInTheSameSwf = true;
        SWF singleSwf = null;
        for (TreeItem item : items) {
            if (item instanceof OpenableList) {
                allSelectedIsInTheSameSwf = false;
                break;
            }
            if (singleSwf == null) {
                Openable singleOpenable = item.getOpenable();
                if (singleOpenable instanceof SWF) {
                    singleSwf = (SWF) singleOpenable;
                } else {
                    allSelectedIsInTheSameSwf = false;
                    break;
                }
            } else if (singleSwf != item.getOpenable()) {
                allSelectedIsInTheSameSwf = false;
                break;
            }
        }

        boolean allSelectedSameParent = !items.isEmpty();
        if (allSelectedSameParent) {
            AbstractTagTreeModel model = tree.getFullModel();
            TreePath thisPath = model.getTreePath(items.get(0));
            TreePath parent = thisPath == null ? null : thisPath.getParentPath();

            if (parent == null) {
                allSelectedSameParent = false;
            } else {
                Map<TreeItem, TreeItem> paths = model.getTreePathParentMulti(items);
                Set<TreeItem> parentSet = new LinkedIdentityHashSet<>();
                parentSet.addAll(paths.values());
                if (parentSet.size() != 1) {
                    allSelectedSameParent = false;
                }
            }
        }

        boolean allSelectedIsFrame = true;
        for (TreeItem item : items) {
            if (!(item instanceof Frame)) {
                allSelectedIsFrame = false;
                break;
            }
        }

        boolean hasExportableNodes = tree.hasExportableNodes();

        gotoDocumentClassMenuItem.setVisible(false);
        setAsLinkageMenuItem.setVisible(false);
        setAs3ClassLinkageMenuItem.setVisible(false);
        expandRecursiveMenuItem.setVisible(false);
        collapseRecursiveMenuItem.setVisible(false);
        pinMenuItem.setVisible(false);
        unpinMenuItem.setVisible(false);
        unpinAllMenuItem.setVisible(false);
        unpinOthersMenuItem.setVisible(false);

        configurePathResolvingMenuItem.setVisible(false);
        removeMenuItem.setVisible(canRemove);
        removeWithDependenciesMenuItem.setVisible(canRemove && !allDoNotHaveDependencies);
        cloneMenuItem.setVisible(allSelectedIsTagOrFrame && allSelectedSameParent);
        undoTagMenuItem.setVisible(allSelectedIsTag);
        exportSelectionMenuItem.setEnabled(hasExportableNodes); //?
        exportABCMenuItem.setVisible(false);
        replaceMenuItem.setVisible(false);
        replaceNoFillMenuItem.setVisible(false);
        replaceWithGifMenuItem.setVisible(false);
        replaceWithTagMenuItem.setVisible(false);
        replaceRefsWithTagMenuItem.setVisible(false);
        convertShapeTypeMenuItem.setVisible(false);
        convertPlaceObjectTypeMenuItem.setVisible(false);
        abcExplorerMenuItem.setVisible(false);
        cleanAbcMenuItem.setVisible(false);
        rawEditMenuItem.setVisible(false);
        jumpToCharacterMenuItem.setVisible(false);
        exportFlaMenuItem.setVisible(false);
        exportFlashDevelopMenuItem.setVisible(false);
        exportIdeaMenuItem.setVisible(false);
        exportVsCodeMenuItem.setVisible(false);
        exportJavaSourceMenuItem.setVisible(allSelectedIsSwf);
        exportSwfXmlMenuItem.setVisible(allSelectedIsSwf);

        importImagesMenuItem.setVisible(false);
        importShapesMenuItem.setVisible(false);
        importShapesNoFillMenuItem.setVisible(false);
        importMoviesMenuItem.setVisible(false);
        importSoundsMenuItem.setVisible(false);
        importScriptsMenuItem.setVisible(false);
        importSymbolClassMenuItem.setVisible(false);
        importTextsMenuItem.setVisible(false);
        importSwfXmlMenuItem.setVisible(false);

        closeMenuItem.setVisible(allSelectedIsSwf);
        addTagInsideMenu.setVisible(false);
        attachTagMenu.setVisible(false);
        addTagBeforeMenu.setVisible(false);
        addTagAfterMenu.setVisible(false);
        moveTagToMenu.setVisible(false);
        moveTagToWithDependenciesMenu.setVisible(false);
        moveFrameToMenu.setVisible(false);
        moveUpMenuItem.setVisible(false);
        moveDownMenuItem.setVisible(false);
        copyTagToMenu.setVisible(false);
        copyTagToWithDependenciesMenu.setVisible(false);
        copyTagToReplaceByClassMenu.setVisible(false);
        copyFrameToMenu.setVisible(false);
        copyFrameToClipboardMenuItem.setVisible(false);
        cutTagToClipboardMenuItem.setVisible(false);
        cutTagToClipboardWithDependenciesMenuItem.setVisible(false);
        cutFrameToClipboardMenuItem.setVisible(false);
        pasteAfterMenuItem.setVisible(false);
        pasteBeforeMenuItem.setVisible(false);
        pasteInsideMenuItem.setVisible(false);
        collectDepthAsSpritesMenuItem.setVisible(allSelectedIsFrame && allSelectedSameParent);
        applyUnpackerMenu.setVisible(false);
        openSWFInsideTagMenuItem.setVisible(false);
        addAs12ScriptMenuItem.setVisible(false);
        addAs12FrameScriptMenuItem.setVisible(false);
        addAs12ButtonEventScriptMenuItem.setVisible(false);
        addAs12InstanceEventScriptMenuItem.setVisible(false);
        addAs12SpriteInitScriptMenuItem.setVisible(false);
        addAs3ClassMenuItem.setVisible(false);
        textSearchMenuItem.setVisible(hasScripts || hasTexts);
        moveTagMenuItem.setVisible(items.size() == 1 && (items.get(0) instanceof Tag));
        showInResourcesViewTagMenuItem.setVisible(false);
        showInTagListViewTagMenuItem.setVisible(false);
        showInHexDumpViewTagMenuItem.setVisible(false);
        showInFramesFolderMenuItem.setVisible(false);
        addFramesMenuItem.setVisible(false);
        addFramesBeforeMenuItem.setVisible(false);
        addFramesAfterMenuItem.setVisible(false);

        changeCharsetMenu.setVisible(false);

        if (allSelectedIsTag) {
            boolean canUndo = false;
            for (TreeItem item : items) {
                if (item instanceof Tag) {
                    Tag tag = (Tag) item;
                    if (tag.canUndo()) {
                        canUndo = true;
                        break;
                    }
                }
            }

            undoTagMenuItem.setEnabled(canUndo);
        }

        boolean singleSelect = items.size() == 1;
        Predicate<Predicate<TreeItem>> canReplace = p -> {
            for (TreeItem ti : items) {
                if (!p.test(ti)) {
                    return false;
                }
            }
            return true;
        };

        // replace
        if (canReplace.test(it -> it instanceof ImageTag && ((ImageTag) it).importSupported())) {
            replaceMenuItem.setVisible(true);
        }

        if (canReplace.test(it -> it instanceof ShapeTag)) {
            replaceMenuItem.setVisible(true);
            replaceNoFillMenuItem.setVisible(true);
        }

        if (canReplace.test(it -> it instanceof MorphShapeTag)) {
            replaceMenuItem.setVisible(true);
            replaceNoFillMenuItem.setVisible(true);
        }

        if (canReplace.test(it -> it instanceof BinaryDataInterface)) {
            replaceMenuItem.setVisible(true);
        }

        if (canReplace.test(it -> it instanceof SoundTag && ((SoundTag) it).importSupported())) {
            replaceMenuItem.setVisible(true);
        }

        if (canReplace.test(it -> it instanceof UnknownTag)) {
            replaceMenuItem.setVisible(true);
        }

        if (canReplace.test(it -> it instanceof DefineVideoStreamTag)) {
            replaceMenuItem.setVisible(true);
        }

        if (singleSelect) {
            final TreeItem firstItem = items.get(0);
            boolean isFolder = firstItem instanceof FolderItem;

            if (firstItem instanceof FolderItem) {
                if (((FolderItem) firstItem).getName().equals(TagTreeModel.FOLDER_SCRIPTS)) {
                    addAs12ScriptMenuItem.setVisible(true);
                }
            }
            if ((firstItem instanceof Frame) || (firstItem instanceof FrameScript)) {
                addAs12FrameScriptMenuItem.setVisible(true);
            }
            if ((firstItem instanceof PlaceObjectTypeTag) || ((firstItem instanceof TagScript) && ((TagScript) firstItem).getTag() instanceof PlaceObjectTypeTag)) {
                addAs12InstanceEventScriptMenuItem.setVisible(true);
            }
            if ((firstItem instanceof DefineButton2Tag) || ((firstItem instanceof TagScript) && ((TagScript) firstItem).getTag() instanceof DefineButton2Tag)) {
                addAs12ButtonEventScriptMenuItem.setVisible(true);
            }
            if ((firstItem instanceof DefineSpriteTag) || ((firstItem instanceof TagScript) && ((TagScript) firstItem).getTag() instanceof DefineSpriteTag)) {
                addAs12SpriteInitScriptMenuItem.setVisible(true);
            }
            if (firstItem instanceof ClassesListTreeModel) {
                addAs3ClassMenuItem.setVisible(true);
                if (firstItem.getOpenable() instanceof SWF) {
                    gotoDocumentClassMenuItem.setVisible(true);
                }
            }
            if (firstItem instanceof AS3Package) {
                AS3Package pkg = (AS3Package) firstItem;
                if (!pkg.isPartOfCompoundScript()) {
                    addAs3ClassMenuItem.setVisible(true);
                }
                if (firstItem.getOpenable() instanceof SWF) {
                    gotoDocumentClassMenuItem.setVisible(true);
                }
            }
            if (firstItem instanceof ABC) {
                addAs3ClassMenuItem.setVisible(true);
                if (firstItem.getOpenable() instanceof SWF) {
                    gotoDocumentClassMenuItem.setVisible(true);
                }
            }
            if (firstItem instanceof ABCContainerTag) {
                addAs3ClassMenuItem.setVisible(true);
                exportABCMenuItem.setVisible(true);
                gotoDocumentClassMenuItem.setVisible(true);
            }

            if (firstItem instanceof ScriptPack) {
                if (firstItem.getOpenable() instanceof SWF) {
                    gotoDocumentClassMenuItem.setVisible(true);
                }
            }

            if (firstItem instanceof SWF) {
                if (((SWF) firstItem).isAS3()) {
                    gotoDocumentClassMenuItem.setVisible(true);
                }
            }

            if (mainPanel.isPinned(firstItem)) {
                int pinCount = mainPanel.getPinCount();
                unpinMenuItem.setVisible(true);
                if (pinCount > 1) {
                    unpinAllMenuItem.setVisible(true);
                    unpinOthersMenuItem.setVisible(true);
                }
            } else {
                pinMenuItem.setVisible(true);
            }

            if (firstItem instanceof CharacterTag) {
                CharacterTag ct = (CharacterTag) firstItem;
                if (ct.getCharacterId() != -1) {
                    replaceWithTagMenuItem.setVisible(true);
                    replaceRefsWithTagMenuItem.setVisible(true);
                }
            }

            if (firstItem instanceof DefineSpriteTag) {
                replaceWithGifMenuItem.setVisible(true);
            }

            TreePath thisPath = tree.getFullModel().getTreePath(firstItem);
            TreeItem parent = thisPath == null ? null : (TreeItem) thisPath.getParentPath().getLastPathComponent();
            boolean parentIsFolder = parent instanceof FolderItem;
            boolean parentIsTopLevelFrame = false;
            if (parent instanceof Frame) {
                if (((Frame) parent).timeline.timelined instanceof SWF) {
                    parentIsTopLevelFrame = true;
                }
            }

            boolean addAllTags = false;
            if (mainPanel.getCurrentView() == MainPanel.VIEW_TAGLIST && parentIsTopLevelFrame) {
                addAllTags = true;
            }

            boolean addInsideAddAllTags = false;
            if (mainPanel.getCurrentView() == MainPanel.VIEW_TAGLIST && (firstItem instanceof Frame)) {
                if (((Frame) firstItem).timeline.timelined instanceof SWF) {
                    addInsideAddAllTags = true;
                }
            }

            boolean isCookie = firstItem instanceof Cookie;

            addTagInsideMenu.removeAll();
            addAddTagInsideMenuItems(firstItem);
            addTagInsideMenu.setVisible(addTagInsideMenu.getItemCount() > 0);

            attachTagMenu.removeAll();
            addAttachTagMenuItems(firstItem);
            attachTagMenu.setVisible(attachTagMenu.getItemCount() > 0);

            addTagBeforeMenu.removeAll();

            addAddTagBeforeAfterMenuItems(true, addTagBeforeMenu, firstItem, this::addTagBeforeActionPerformed);
            addTagBeforeMenu.setVisible(!isCookie && addTagBeforeMenu.getItemCount() > 0);

            addTagAfterMenu.removeAll();
            addAddTagBeforeAfterMenuItems(false, addTagAfterMenu, firstItem, this::addTagAfterActionPerformed);

            //addAddTagMenuItems(getAllowedTagTypes(parent), addTagAfterMenu, firstItem, this::addTagAfterActionPerformed);
            /*JMenu othersMenu = new JMenu(AppStrings.translate("node.others"));
                othersMenu.setIcon(View.getIcon("folder16"));
                addAddTagMenuItems(null, othersMenu, firstItem, this::addTagAfterActionPerformed);
                addTagAfterMenu.add(othersMenu);*/
            addTagAfterMenu.setVisible(!isCookie && addTagAfterMenu.getItemCount() > 0);

            if ((firstItem instanceof CharacterTag)) {
                CharacterTag cht = (CharacterTag) firstItem;
                if (cht.getSwf().isAS3() && As3ClassLinkageDialog.getParentClassFromCharacter(cht) != null) {
                    setAs3ClassLinkageMenuItem.setVisible(true);
                }
                if (!cht.getSwf().isAS3()) {
                    String ename = cht.getExportName();
                    if (ename == null || !ename.startsWith("__Packages.")) {
                        setAsLinkageMenuItem.setVisible(true);
                    }
                }
            }

            if (tree.getModel().getChildCount(firstItem) > 0) {
                expandRecursiveMenuItem.setVisible(true);
                collapseRecursiveMenuItem.setVisible(true);
            }

            if (firstItem instanceof SWF) {
                SWF swf = (SWF) firstItem;
                exportFlaMenuItem.setVisible(true);
                if (swf.isAS3()) {
                    if (SwfFlashDevelopExporter.canExportSwf(swf)) {
                        exportFlashDevelopMenuItem.setVisible(true);
                    }
                    if (SwfIntelliJIdeaExporter.canExportSwf(swf)) {
                        exportIdeaMenuItem.setVisible(true);
                    }
                    if (SwfVsCodeExporter.canExportSwf(swf)) {
                        exportVsCodeMenuItem.setVisible(true);
                    }
                }
            }

            if (firstItem instanceof HasCharacterId && !(firstItem instanceof CharacterTag)) {
                jumpToCharacterMenuItem.setVisible(true);
            }

            if (firstItem instanceof ScriptPack) {
                abcExplorerMenuItem.setVisible(true);
            }

            if (firstItem instanceof AS3Package) {
                AS3Package pkg = (AS3Package) firstItem;
                if (pkg.isCompoundScript()) {
                    abcExplorerMenuItem.setVisible(true);
                }
            }

            if (firstItem instanceof ABCContainerTag) {
                abcExplorerMenuItem.setVisible(true);
                cleanAbcMenuItem.setVisible(true);
            }

            if (firstItem instanceof ABC) {
                abcExplorerMenuItem.setVisible(true);
                cleanAbcMenuItem.setVisible(true);
            }

            if (firstItem instanceof ClassesListTreeModel) {
                abcExplorerMenuItem.setVisible(true);
            }

            if (firstItem instanceof SWF) {
                SWF swf = (SWF) firstItem;
                if (swf.isAS3()) {
                    abcExplorerMenuItem.setVisible(true);
                    cleanAbcMenuItem.setVisible(true);
                }
                if (swf.gfx) {
                    configurePathResolvingMenuItem.setVisible(true);
                }
            }

            if (firstItem instanceof Tag) {
                rawEditMenuItem.setVisible(true);
            }

            if ((firstItem instanceof DefineSpriteTag) || (firstItem instanceof SWF)) {
                addFramesMenuItem.setVisible(true);
            }

            if (firstItem instanceof Frame) {
                addFramesBeforeMenuItem.setVisible(true);
                addFramesAfterMenuItem.setVisible(true);
            }

            if (mainPanel.getCurrentView() == MainPanel.VIEW_TAGLIST
                    && !(firstItem instanceof ShowFrameTag)
                    && !(firstItem instanceof AS3Package)) {
                showInResourcesViewTagMenuItem.setVisible(true);
            }

            if (mainPanel.getCurrentView() == MainPanel.VIEW_RESOURCES
                    && !isFolder
                    && !(firstItem instanceof AS3Package)
                    && !(firstItem instanceof PackedBinaryData)) {
                showInTagListViewTagMenuItem.setVisible(true);
            }

            if ((firstItem instanceof Tag)
                    || (firstItem instanceof CLIPACTIONRECORD)
                    || (firstItem instanceof BUTTONRECORD)
                    || (firstItem instanceof BUTTONCONDACTION)
                    || (firstItem instanceof TagScript)) {
                showInHexDumpViewTagMenuItem.setVisible(true);
            }

            if ((firstItem instanceof Scene) || (firstItem instanceof SceneFrame)) {
                showInFramesFolderMenuItem.setVisible(true);
            }

            if (firstItem instanceof SWF) {
                importImagesMenuItem.setVisible(true);
                importShapesMenuItem.setVisible(true);
                importShapesNoFillMenuItem.setVisible(true);
                importMoviesMenuItem.setVisible(true);
                importSoundsMenuItem.setVisible(true);
                importScriptsMenuItem.setVisible(true);
                importSymbolClassMenuItem.setVisible(true);
                importTextsMenuItem.setVisible(true);
                importSwfXmlMenuItem.setVisible(true);
            }

            if (firstItem instanceof ABC) {
                importScriptsMenuItem.setVisible(true);
            }

            if (mainPanel.getClipboardType() == ClipboardType.TAG) {
                if ((firstItem instanceof SWF) || (firstItem instanceof DefineSpriteTag) || (firstItem instanceof Frame)) {
                    pasteInsideMenuItem.setVisible(true);
                }
                if ((firstItem instanceof Tag) || (firstItem instanceof Frame)) {
                    pasteAfterMenuItem.setVisible(true);
                    pasteBeforeMenuItem.setVisible(true);
                }
            }
            if (mainPanel.getClipboardType() == ClipboardType.FRAME) {
                Openable clipboardOpenable = mainPanel.getClipboardContents().iterator().next().getOpenable();

                if (clipboardOpenable == firstItem.getOpenable()) {
                    if ((firstItem instanceof SWF) || (firstItem instanceof DefineSpriteTag)) {
                        pasteInsideMenuItem.setVisible(true);
                    }
                    if (firstItem instanceof Frame) {
                        pasteAfterMenuItem.setVisible(true);
                        pasteBeforeMenuItem.setVisible(true);
                    }
                }
            }

            if ((firstItem instanceof Tag) && (getTree() == mainPanel.tagListTree)) {
                moveUpMenuItem.setVisible(true);
                moveDownMenuItem.setVisible(true);
            }
            if (firstItem instanceof Openable) {
                Openable firstOpenable = (Openable) firstItem;
                if (firstOpenable.getOpenableList() != null && !firstOpenable.getOpenableList().isBundle() && firstOpenable.getOpenableList().size() == 1) {
                    moveUpMenuItem.setVisible(true);
                    moveDownMenuItem.setVisible(true);
                }
            }
            if (firstItem instanceof OpenableList) {
                moveUpMenuItem.setVisible(true);
                moveDownMenuItem.setVisible(true);
            }

            if (firstItem instanceof SWF) {
                SWF firstSwf = (SWF) firstItem;
                if (firstSwf.version <= 5) {
                    changeCharsetMenu.setText(mainPanel.translate("contextmenu.changeCharset").replace("%charset%", firstSwf.getCharset()));
                    changeCharsetMenu.setVisible(true);
                }
            }
        }

        if (allSelectedIsShape) {
            convertShapeTypeMenuItem.setVisible(true);
        }

        if (allSelectedIsPlaceObject) {
            convertPlaceObjectTypeMenuItem.setVisible(true);
        }

        moveTagToMenu.removeAll();
        moveTagToWithDependenciesMenu.removeAll();
        moveFrameToMenu.removeAll();
        copyTagToMenu.removeAll();
        copyTagToWithDependenciesMenu.removeAll();
        copyTagToReplaceByClassMenu.removeAll();
        copyFrameToMenu.removeAll();

        List<TreeItem> tagItems = new ArrayList<>();
        if (allSelectedIsTag) {
            for (TreeItem item : items) {
                if (item instanceof TagScript) {
                    tagItems.add(((TagScript) item).getTag());
                } else {
                    tagItems.add((Tag) item);
                }
            }
            JMenuItem copyTagToClipboardMenuItem = new JMenuItem(AppStrings.translate("contextmenu.clipboard") + " (CTRL+C)", View.getIcon("clipboard16"));
            copyTagToClipboardMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    copyTagOrFrameToClipboardActionPerformed(e, tagItems);
                }
            });

            copyTagToMenu.add(copyTagToClipboardMenuItem);

            JMenuItem copyToClipboardWithDependenciesMenuItem = new JMenuItem(AppStrings.translate("contextmenu.clipboard") + " (CTRL+SHIFT+C)", View.getIcon("clipboard16"));
            copyToClipboardWithDependenciesMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    copyTagToClipboardWithDependenciesActionPerformed(e, tagItems);
                }
            });

            copyTagToWithDependenciesMenu.add(copyToClipboardWithDependenciesMenuItem);

            cutTagToClipboardMenuItem.setVisible(true);
            cutTagToClipboardWithDependenciesMenuItem.setVisible(true);

            copyTagToMenu.setVisible(true);
            copyTagToWithDependenciesMenu.setVisible(true);
        }
        if (allSelectedIsFrameInSameTimeline) {
            /*JMenuItem copyFrameToSubClipboardMenuItem = new JMenuItem(AppStrings.translate("contextmenu.clipboard.frame") + " (CTRL+C)", View.getIcon("clipboard16"));
            copyFrameToSubClipboardMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    copyTagOrFrameToClipboardActionPerformed(e, items);
                }
            });
            copyFrameToMenu.add(copyFrameToSubClipboardMenuItem);
            copyFrameToMenu.setVisible(true);*/

            copyFrameToClipboardMenuItem.setVisible(true);
            cutFrameToClipboardMenuItem.setVisible(true);

            /*if (swfs.size() > 1) {
                for (OpenableList targetSwfList : swfs) {
                    if ((targetSwfList.size() == 1) && (targetSwfList.get(0) == singleSwf)) {
                        continue;
                    }
                    addCopyMoveToMenusSwfList(KIND_FRAME_MOVETO, singleSwf, targetSwfList, moveFrameToMenu, items);
                    addCopyMoveToMenusSwfList(KIND_FRAME_COPYTO, singleSwf, targetSwfList, copyFrameToMenu, items);
                }
                moveFrameToMenu.setVisible(true);
                copyFrameToMenu.setVisible(true);
            }
             */
        }

        if (allSelectedIsInTheSameSwf && allSelectedIsTag && swfs.size() > 1) {
            for (OpenableList targetSwfList : swfs) {
                if ((targetSwfList.size() == 1) && (targetSwfList.get(0) == singleSwf)) {
                    continue;
                }
                addCopyMoveToMenusSwfList(KIND_TAG_MOVETO, singleSwf, targetSwfList, moveTagToMenu, tagItems, true, true);
                addCopyMoveToMenusSwfList(KIND_TAG_MOVETODEPS, singleSwf, targetSwfList, moveTagToWithDependenciesMenu, tagItems, true, true);
                addCopyMoveToMenusSwfList(KIND_TAG_COPYTO, singleSwf, targetSwfList, copyTagToMenu, tagItems, true, true);
                addCopyMoveToMenusSwfList(KIND_TAG_COPYTODEPS, singleSwf, targetSwfList, copyTagToWithDependenciesMenu, tagItems, true, true);
                if (allSelectedIsTagWithClassName || allSelectedIsTagWithExportName) {
                    addCopyMoveToMenusSwfList(KIND_TAG_COPYTOCLASSOREXPORTNAME, singleSwf, targetSwfList, copyTagToReplaceByClassMenu, tagItems, allSelectedIsTagWithExportName, allSelectedIsTagWithClassName);
                }
            }
            moveTagToMenu.setVisible(true);
            moveTagToWithDependenciesMenu.setVisible(true);
            copyTagToMenu.setVisible(true);
            copyTagToWithDependenciesMenu.setVisible(true);
            if (allSelectedIsTagWithClassName || allSelectedIsTagWithExportName) {
                copyTagToReplaceByClassMenu.setVisible(true);
            }
            if (allSelectedIsTagWithClassName) {
                copyTagToReplaceByClassMenu.setText(AppStrings.translate("contextmenu.copyTagToReplaceByClass"));
            }
            if (allSelectedIsTagWithExportName) {
                copyTagToReplaceByClassMenu.setText(AppStrings.translate("contextmenu.copyTagToReplaceByExportName"));
            }
        }

        if (allSelectedIsBinaryData) {
            applyUnpackerMenu.setVisible(true);

            boolean anyInnerSwf = false;
            for (TreeItem item : items) {
                BinaryDataInterface binary = (BinaryDataInterface) item;

                // inner swf is not loaded yet
                if (binary.getInnerSwf() == null && binary.isSwfData()) {
                    anyInnerSwf = true;
                }
            }

            openSWFInsideTagMenuItem.setVisible(anyInnerSwf);
        }

        for (TreeItem item : items) {
            if (item instanceof Frame) {
                if (((Frame) item).timeline.timelined instanceof DefineSpriteTag) {
                    if (((Tag) ((Frame) item).timeline.timelined).isReadOnly()) {
                        removeMenuItem.setVisible(false);
                        addTagInsideMenu.setVisible(false);
                        addFramesAfterMenuItem.setVisible(false);
                        addFramesBeforeMenuItem.setVisible(false);
                    }
                }
            }
            if (item instanceof Tag) {
                if (((Tag) item).isReadOnly()) {
                    attachTagMenu.setVisible(false);
                    moveUpMenuItem.setVisible(false);
                    moveDownMenuItem.setVisible(false);
                    showInHexDumpViewTagMenuItem.setVisible(false);
                    addFramesBeforeMenuItem.setVisible(false);
                    addFramesAfterMenuItem.setVisible(false);
                    addFramesMenuItem.setVisible(false);
                    moveTagMenuItem.setVisible(false);
                    removeMenuItem.setVisible(false);
                    removeWithDependenciesMenuItem.setVisible(false);
                    undoTagMenuItem.setVisible(false);
                    replaceMenuItem.setVisible(false);
                    replaceNoFillMenuItem.setVisible(false);
                    replaceWithTagMenuItem.setVisible(false);
                    //rawEditMenuItem.setVisible(false);
                    jumpToCharacterMenuItem.setVisible(false);
                    importSwfXmlMenuItem.setVisible(false);
                    addTagInsideMenu.setVisible(false);
                    addTagBeforeMenu.setVisible(false);
                    addTagAfterMenu.setVisible(false);
                    moveTagToMenu.setVisible(false);
                    moveTagToWithDependenciesMenu.setVisible(false);
                    cutTagToClipboardMenuItem.setVisible(false);
                    cutTagToClipboardWithDependenciesMenuItem.setVisible(false);
                    //openSWFInsideTagMenuItem.setVisible(false);
                }
            }
        }
        updateSeparators();
    }

    private void updateSeparators() {
        final int ITEM_COUNT_LIMIT = 6;
        int totalVisible = 0;
        for (Component comp : getComponents()) {
            if (!(comp instanceof JSeparator) && comp.isVisible()) {
                totalVisible++;
            }
        }
        int numVisible = 0;
        for (Component comp : getComponents()) {
            if (comp instanceof JSeparator) {
                comp.setVisible(totalVisible > ITEM_COUNT_LIMIT && numVisible > 0);
                numVisible = 0;
            } else if (comp.isVisible()) {
                numVisible++;
            }
        }
    }

    private interface AddTagActionListener {

        void call(ActionEvent evt, TreeItem item, Class<?> cl, TreeNodeType createNodeType);
    }

    private void addAddTagMenuFolder(JMenu addTagMenu, String folder, boolean gfx, TreeItem item, AddTagActionListener listener) {
        String folderTranslated = AppStrings.translate("node." + folder);
        JMenu folderMenu = new JMenu(folderTranslated);
        folderMenu.setIcon(View.getIcon("folder" + folder.toLowerCase(Locale.ENGLISH) + "16"));

        Map<Integer, TagTypeInfo> classes = Tag.getKnownClasses();

        List<Integer> allowedTagTypes = new ArrayList<>(TagTree.getSwfFolderItemNestedTagIds(folder, gfx));
        Set<Integer> mappedTagTypes = new LinkedHashSet<>();
        for (int i : allowedTagTypes) {
            mappedTagTypes.addAll(AbstractTagTree.getMappedTagIdsForClass(classes.get(i).getCls()));
        }
        if (allowedTagTypes.isEmpty() && mappedTagTypes.isEmpty()) {
            return;
        }
        addAddTagMenuItems(allowedTagTypes, folderMenu, item, listener, folder);
        if (!allowedTagTypes.isEmpty() && !mappedTagTypes.isEmpty()) {
            folderMenu.addSeparator();
        }
        addAddTagMenuItems(new ArrayList<Integer>(mappedTagTypes), folderMenu, item, listener, null);

        addTagMenu.add(folderMenu);
    }

    private void addAttachTagMenuItems(TreeItem item) {
        AddTagActionListener listener = this::attachTagActionPerformed;
        List<Integer> mapped = AbstractTagTree.getMappedTagIdsForClass(item.getClass());
        addAddTagMenuItems(mapped, attachTagMenu, item, listener, null);
    }

    private void addAddTagInsideMenuItems(TreeItem item) {
        AddTagActionListener listener = this::addTagInsideActionPerformed;
        Map<Integer, TagTypeInfo> classes = Tag.getKnownClasses();
        SWF currentSwf = mainPanel.getCurrentSwf();
        if (currentSwf == null) {
            return;
        }
        boolean gfx = currentSwf.gfx;

        if (item instanceof SWF) {
            addAddTagMenuItems(null, addTagInsideMenu, item, listener, null);
            return;
        }

        if (item instanceof DefineSpriteTag) {
            addAddTagMenuItems(AbstractTagTree.getFrameNestedTagIds(), addTagInsideMenu, item, listener, TagTreeModel.FOLDER_FRAMES);
            addTagInsideMenu.addSeparator();
            addTagInsideMenu.add(createOthersMenu(item, listener));
            return;
        }

        if (item instanceof Frame) {
            Frame frame = (Frame) item;
            boolean insideSprite = frame.timeline.timelined instanceof DefineSpriteTag;
            if (mainPanel.getCurrentView() == MainPanel.VIEW_TAGLIST) {
                addAddTagMenuItems(null, addTagInsideMenu, item, listener, TagTreeModel.FOLDER_FRAMES);
                return;
            } else {
                addAddTagMenuItems(AbstractTagTree.getFrameNestedTagIds(), addTagInsideMenu, item, listener, TagTreeModel.FOLDER_FRAMES);
                addTagInsideMenu.addSeparator();
                addTagInsideMenu.add(createOthersMenu(item, listener));
            }
            return;
        }

        if (item instanceof FolderItem) {
            List<Integer> allowedTagTypes = new ArrayList<>(TagTree.getSwfFolderItemNestedTagIds(((FolderItem) item).getName(), gfx));
            addAddTagMenuItems(allowedTagTypes, addTagInsideMenu, item, listener, ((FolderItem) item).getName());
            return;
        }

        /*if (mainPanel.getCurrentView() == MainPanel.VIEW_RESOURCES) {
            List<Integer> mapped = AbstractTagTree.getMappedTagIdsForClass(item.getClass());
            addAddTagMenuItems(mapped, addTagInsideMenu, item, listener);
        }*/
    }

    private void addAddTagBeforeAfterMenuItems(boolean before, JMenu addTagMenu, TreeItem item, AddTagActionListener listener) {
        TreePath thisPath = getTree().getFullModel().getTreePath(item);
        TreeItem parent = thisPath == null ? null : (TreeItem) thisPath.getParentPath().getLastPathComponent();
        if (parent == null) {
            return;
        }
        Map<Integer, TagTypeInfo> classes = Tag.getKnownClasses();

        SWF currentSwf = mainPanel.getCurrentSwf();

        if (currentSwf == null) {
            return;
        }

        boolean gfx = currentSwf.gfx;
        boolean insideFrame = false;
        boolean insideSprite = false;

        if (item instanceof SWF) {
            return;
        }

        if (item instanceof Scene) {
            return;
        }

        if (item instanceof SceneFrame) {
            return;
        }

        if (item instanceof Frame) {
            insideFrame = true;
            Frame frame = (Frame) item;
            insideSprite = (frame.timeline.timelined instanceof DefineSpriteTag);
        }
        if (parent instanceof Frame) {
            insideFrame = true;
            Frame frame = (Frame) parent;
            insideSprite = (frame.timeline.timelined instanceof DefineSpriteTag);
        }

        if (insideFrame) {

            if (mainPanel.getCurrentView() == MainPanel.VIEW_TAGLIST && !insideSprite) {
                addAddTagMenuItems(null, addTagMenu, item, listener, TagTreeModel.FOLDER_FRAMES);
                return;
            }

            addAddTagMenuItems(AbstractTagTree.getFrameNestedTagIds(), addTagMenu, item, listener, TagTreeModel.FOLDER_FRAMES);
            addTagMenu.addSeparator();
            addTagMenu.add(createOthersMenu(item, listener));

            return;
        }

        if (parent instanceof FolderItem) {
            List<Integer> allowedTagTypes = new ArrayList<>(TagTree.getSwfFolderItemNestedTagIds(((FolderItem) parent).getName(), gfx));

            addAddTagMenuItems(allowedTagTypes, addTagMenu, item, listener, ((FolderItem) parent).getName());
            addTagMenu.addSeparator();
            addTagMenu.add(createOthersMenu(item, listener));
            return;
        }

        if ((item instanceof HeaderItem) && !before) {
            addAddTagMenuItems(null, addTagMenu, item, listener, null);
        }
    }

    private JMenu createOthersMenu(TreeItem item, AddTagActionListener listener) {
        JMenu othersMenu = new JMenu(AppStrings.translate("node.others"));
        othersMenu.setIcon(View.getIcon("folder16"));
        addAddTagMenuItems(null, othersMenu, item, listener, TagTreeModel.FOLDER_OTHERS);
        return othersMenu;
    }

    private void addAddTagMenuItems(List<Integer> allowedTagTypes, JMenu addTagMenu, TreeItem item, AddTagActionListener listener, String parentFolder) {
        if (allowedTagTypes == null) {
            boolean gfx = mainPanel.getCurrentSwf().gfx;

            String[] folders = new String[]{
                TagTreeModel.FOLDER_SHAPES,
                TagTreeModel.FOLDER_MORPHSHAPES,
                TagTreeModel.FOLDER_SPRITES,
                TagTreeModel.FOLDER_TEXTS,
                TagTreeModel.FOLDER_IMAGES,
                TagTreeModel.FOLDER_MOVIES,
                TagTreeModel.FOLDER_SOUNDS,
                TagTreeModel.FOLDER_BUTTONS,
                TagTreeModel.FOLDER_FONTS,
                TagTreeModel.FOLDER_BINARY_DATA,
                TagTreeModel.FOLDER_FRAMES,
                TagTreeModel.FOLDER_OTHERS
            };
            for (String folder : folders) {
                addAddTagMenuFolder(addTagMenu, folder, gfx, item, listener);
            }

            return;
        }

        for (Integer tagId : allowedTagTypes) {
            final Class<?> cl = TagIdClassMap.getClassByTagId(tagId);
            String className = cl.getSimpleName();
            if (className.endsWith("Tag")) {
                className = className.substring(0, className.length() - 3);
            }
            JMenuItem tagItem = new JMenuItem(className);
            TreeNodeType type = AbstractTagTree.getTagNodeTypeFromTagClass(cl);
            tagItem.setIcon(TagTree.getIconForType(type));
            tagItem.addActionListener((ActionEvent ae) -> {
                listener.call(ae, item, cl, null);
            });
            addTagMenu.add(tagItem);
        }

        if (parentFolder == null) {
            return;
        }
        switch (parentFolder) {
            case TagTreeModel.FOLDER_SHAPES:
                addTagMenu.addSeparator();
                JMenuItem createShapeItem = new JMenuItem(AppStrings.translate("tag.shape.create"));
                createShapeItem.setIcon(View.getIcon("importshape16"));
                createShapeItem.addActionListener((ActionEvent ae) -> {
                    listener.call(ae, item, DefineShape4Tag.class, TreeNodeType.SHAPE);
                });
                addTagMenu.add(createShapeItem);
                break;
            case TagTreeModel.FOLDER_IMAGES:
                addTagMenu.addSeparator();
                JMenuItem createImageItem = new JMenuItem(AppStrings.translate("tag.image.create"));
                createImageItem.setIcon(View.getIcon("importimage16"));
                createImageItem.addActionListener((ActionEvent ae) -> {
                    listener.call(ae, item, DefineBitsLossless2Tag.class, TreeNodeType.IMAGE);
                });
                addTagMenu.add(createImageItem);
                break;
            case TagTreeModel.FOLDER_SPRITES:
                addTagMenu.addSeparator();
                JMenuItem createSpriteItem = new JMenuItem(AppStrings.translate("tag.sprite.create"));
                createSpriteItem.setIcon(View.getIcon("importsprite16"));
                createSpriteItem.addActionListener((ActionEvent ae) -> {
                    listener.call(ae, item, DefineSpriteTag.class, TreeNodeType.SPRITE);
                });
                addTagMenu.add(createSpriteItem);
                break;
            case TagTreeModel.FOLDER_MOVIES:
                addTagMenu.addSeparator();
                JMenuItem createMovieItem = new JMenuItem(AppStrings.translate("tag.movie.create"));
                createMovieItem.setIcon(View.getIcon("importmovie16"));
                createMovieItem.addActionListener((ActionEvent ae) -> {
                    listener.call(ae, item, DefineVideoStreamTag.class, TreeNodeType.MOVIE);
                });
                addTagMenu.add(createMovieItem);
                break;
            case TagTreeModel.FOLDER_SOUNDS:
                addTagMenu.addSeparator();
                JMenuItem createSoundItem = new JMenuItem(AppStrings.translate("tag.sound.create"));
                createSoundItem.setIcon(View.getIcon("importsound16"));
                createSoundItem.addActionListener((ActionEvent ae) -> {
                    listener.call(ae, item, DefineSoundTag.class, TreeNodeType.SOUND);
                });
                addTagMenu.add(createSoundItem);
                break;
            case TagTreeModel.FOLDER_FONTS:
                addTagMenu.addSeparator();
                JMenuItem createFontItem = new JMenuItem(AppStrings.translate("tag.font.create"));
                createFontItem.setIcon(View.getIcon("importfont16"));
                createFontItem.addActionListener((ActionEvent ae) -> {
                    listener.call(ae, item, DefineFont3Tag.class, TreeNodeType.FONT);
                });
                addTagMenu.add(createFontItem);
                break;
            case TagTreeModel.FOLDER_BINARY_DATA:
                addTagMenu.addSeparator();
                JMenuItem createBinaryDataItem = new JMenuItem(AppStrings.translate("tag.binaryData.create"));
                createBinaryDataItem.setIcon(View.getIcon("importbinarydata16"));
                createBinaryDataItem.addActionListener((ActionEvent ae) -> {
                    listener.call(ae, item, DefineBinaryDataTag.class, TreeNodeType.BINARY_DATA);
                });
                addTagMenu.add(createBinaryDataItem);
                break;
            case TagTreeModel.FOLDER_MORPHSHAPES:
                addTagMenu.addSeparator();
                JMenuItem createMorphShapeItem = new JMenuItem(AppStrings.translate("tag.morphshape.create"));
                createMorphShapeItem.setIcon(View.getIcon("importmorphshape16"));
                createMorphShapeItem.addActionListener((ActionEvent ae) -> {
                    listener.call(ae, item, DefineMorphShape2Tag.class, TreeNodeType.MORPH_SHAPE);
                });
                addTagMenu.add(createMorphShapeItem);
                break;
        }
    }

    private void addTagInsideActionPerformed(ActionEvent evt, TreeItem item, Class<?> cl, TreeNodeType createNodeType) {
        int id = -1;
        try {
            id = cl.getDeclaredField("ID").getInt(null);

        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(TagTreeContextMenu.class.getName()).log(Level.SEVERE, null, ex);
        }

        SWF swf = (SWF) item.getOpenable();
        Timelined selectedTimelined = null;
        Tag selectedTag = null;
        boolean selectNext = false;
        if (item instanceof DefineSpriteTag) {
            selectedTimelined = (DefineSpriteTag) item;
        } else if (item instanceof Frame) {
            Frame frame = (Frame) item;
            selectedTimelined = frame.timeline.timelined;
            if (!frame.allInnerTags.isEmpty()) {
                selectedTag = frame.allInnerTags.get(frame.allInnerTags.size() - 1);
            }
        } else if (item instanceof FolderItem) {
            selectedTimelined = (SWF) item.getOpenable();
        } else if (item instanceof SWF) {
            selectedTimelined = (SWF) item;
        }

        SelectTagPositionDialog selectPositionDialog = new SelectTagPositionDialog(mainPanel.getMainFrame().getWindow(), swf, selectedTag, selectedTimelined, true, selectNext, null, 1);
        if (selectPositionDialog.showDialog() == AppDialog.OK_OPTION) {
            selectedTimelined = selectPositionDialog.getSelectedTimelined();
            selectedTag = selectPositionDialog.getSelectedTag();
            try {
                Tag t = (Tag) cl.getDeclaredConstructor(SWF.class).newInstance(new Object[]{swf});
                t.setTimelined(selectedTimelined);
                if (selectedTag == null) {
                    selectedTimelined.addTag(t);
                } else {
                    selectedTimelined.addTag(selectedTimelined.indexOfTag(selectedTag), t);
                }
                selectedTimelined.resetTimeline();
                swf.updateCharacters();
                mainPanel.refreshTree(swf);
                mainPanel.setTagTreeSelectedNode(mainPanel.getCurrentTree(), t);
                handleCreateFromFile(t, createNodeType);
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException
                    | IllegalArgumentException | InvocationTargetException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
    }

    private void addTagBeforeActionPerformed(ActionEvent evt, TreeItem item, Class<?> cl, TreeNodeType createNodeType) {
        try {
            SWF swf = (SWF) item.getOpenable();
            Tag t = (Tag) cl.getDeclaredConstructor(SWF.class).newInstance(new Object[]{swf});

            Timelined timelined = null;
            int index = -1;
            if (item instanceof Tag) {
                Tag itemTag = (Tag) item;
                timelined = itemTag.getTimelined();
                index = timelined.indexOfTag(itemTag);
            } else if (item instanceof Frame) {
                Frame frame = (Frame) item;
                timelined = frame.timeline.timelined;

                index = calcFramePositionToAdd(frame, timelined, true, new Reference<>(false), false);
            }

            if (timelined != null) {
                if (index == -1) {
                    timelined.addTag(t);
                } else {
                    timelined.addTag(index, t);
                }

                t.setTimelined(timelined);
                timelined.resetTimeline();

                timelined.setFrameCount(timelined.getTimeline().getFrameCount());
            }

            swf.updateCharacters();
            mainPanel.refreshTree(swf);
            mainPanel.setTagTreeSelectedNode(mainPanel.getCurrentTree(), t);
            handleCreateFromFile(t, createNodeType);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException
                | IllegalArgumentException | InvocationTargetException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    private void addTagAfterActionPerformed(ActionEvent evt, TreeItem item, Class<?> cl, TreeNodeType createNodeType) {
        try {
            SWF swf = (SWF) item.getOpenable();
            Tag t = (Tag) cl.getDeclaredConstructor(SWF.class).newInstance(new Object[]{swf});

            Timelined timelined = null;
            int index = -1;
            if (item instanceof Tag) {
                Tag itemTag = (Tag) item;
                timelined = itemTag.getTimelined();
                index = timelined.indexOfTag(itemTag) + 1;
            } else if (item instanceof Frame) {
                Frame frame = (Frame) item;
                timelined = frame.timeline.timelined;

                index = calcFramePositionToAdd(frame, timelined, false, new Reference<>(false), false);
            } else if (item instanceof HeaderItem) {
                timelined = swf;
                index = 0;
            }

            if (timelined != null) {
                if (index == -1) {
                    timelined.addTag(t);
                } else {
                    timelined.addTag(index, t);
                }

                t.setTimelined(timelined);
                timelined.resetTimeline();

                timelined.setFrameCount(timelined.getTimeline().getFrameCount());
            }

            swf.updateCharacters();
            mainPanel.refreshTree(swf);
            mainPanel.setTagTreeSelectedNode(mainPanel.getCurrentTree(), t);
            handleCreateFromFile(t, createNodeType);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException
                | IllegalArgumentException | InvocationTargetException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    private int checkUniqueCharacterId(Tag tag) {
        if (tag instanceof CharacterTag) {
            CharacterTag characterTag = (CharacterTag) tag;
            int characterId = characterTag.getCharacterId();
            SWF swf = tag.getSwf();
            if (swf.getCharacter(characterId) != null) {
                int newCharacterId = swf.getNextCharacterId();
                characterTag.setCharacterId(newCharacterId);
                logger.log(Level.WARNING, "Target SWF already contained character tag with id = {0} => id changed to {1}", new Object[]{characterId, newCharacterId});
                return newCharacterId;
            }

            return characterId;
        }

        return -1;
    }

    private void moveTagToActionPerformed(ActionEvent evt, List<TreeItem> items, SWF targetSwf) {
        SelectTagPositionDialog selectPositionDialog = new SelectTagPositionDialog(mainPanel.getMainFrame().getWindow(), targetSwf, true);
        if (selectPositionDialog.showDialog() != AppDialog.OK_OPTION) {
            return;
        }
        Tag position = selectPositionDialog.getSelectedTag();
        Timelined timelined = selectPositionDialog.getSelectedTimelined();
        copyOrMoveTags(new LinkedHashSet<TreeItem>(items), true, timelined, position);
    }

    private void moveFrameToActionPerformed(ActionEvent evt, List<TreeItem> items, SWF targetSwf) {
        SelectFramePositionDialog selectPositionDialog = new SelectFramePositionDialog(mainPanel.getMainFrame().getWindow(), targetSwf);
        if (selectPositionDialog.showDialog() != AppDialog.OK_OPTION) {
            return;
        }
        int position = selectPositionDialog.getSelectedFrame();
        Timelined timelined = selectPositionDialog.getSelectedTimelined();
        copyOrMoveFrames(new LinkedHashSet<TreeItem>(items), true, timelined, position);
    }

    private void copyTagToActionPerformed(ActionEvent evt, List<TreeItem> items, SWF targetSwf) {
        SelectTagPositionDialog selectPositionDialog = new SelectTagPositionDialog(mainPanel.getMainFrame().getWindow(), targetSwf, true);
        if (selectPositionDialog.showDialog() != AppDialog.OK_OPTION) {
            return;
        }
        Tag position = selectPositionDialog.getSelectedTag();
        Timelined timelined = selectPositionDialog.getSelectedTimelined();
        copyOrMoveTags(new LinkedHashSet<TreeItem>(items), false, timelined, position);
    }

    private class AlterCharacterTag extends CharacterTag {

        private int characterId;

        public AlterCharacterTag(SWF swf, int characterId) {
            super(swf, 10000, "Alter", new ByteArrayRange(new byte[]{}));
            this.characterId = characterId;
        }

        @Override
        public void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException, InterruptedException {
        }

        @Override
        public void getData(SWFOutputStream sos) throws IOException {
        }

        @Override
        public int getCharacterId() {
            return characterId;
        }

        @Override
        public void setCharacterId(int characterId) {
            this.characterId = characterId;
        }
    }

    private void copyTagToReplaceByClassOrExportNameActionPerformed(ActionEvent evt, List<TreeItem> items, SWF targetSwf) {
        Set<SWF> sourceSwfs = new LinkedHashSet<>();
        try {
            List<Tag> newTags = new ArrayList<>();
            Map<Integer, Integer> changedCharacterIds = new HashMap<>();

            Map<Tag, Tag> classMappedAlterTags = new HashMap<>();
            Map<Tag, CharacterTag> classMappedTags = new HashMap<>();

            Set<TreeItem> ignoredItems = new LinkedIdentityHashSet<>();

            for (TreeItem item : items) {
                Tag tag = (Tag) item;
                if (tag.getSwf() == null) {
                    continue;
                }
                Timelined realTargetTimelined;
                CharacterTag targetSameNameCharacter = null;

                CharacterTag chtag = (CharacterTag) tag;

                for (String className : chtag.getClassNames()) {
                    targetSameNameCharacter = targetSwf.getCharacterByClass(className);
                    if (targetSameNameCharacter != null) {
                        break;
                    }
                }

                if (targetSameNameCharacter == null && chtag.getExportName() != null) {
                    targetSameNameCharacter = targetSwf.getCharacterByExportName(chtag.getExportName());
                }

                if (targetSameNameCharacter == null) {
                    ignoredItems.add(item);
                    continue;
                }

                realTargetTimelined = targetSameNameCharacter.getTimelined();

                AlterCharacterTag alterTag = new AlterCharacterTag(targetSwf, targetSameNameCharacter.getCharacterId());

                Set<Integer> needed = new LinkedHashSet<>();
                targetSameNameCharacter.getNeededCharacters(needed, targetSwf);
                int ind = realTargetTimelined.indexOfTag(targetSameNameCharacter);
                realTargetTimelined.removeTag(ind);
                realTargetTimelined.addTag(ind, alterTag);
                alterTag.setTimelined(realTargetTimelined);
                targetSwf.computeDependentCharacters();
                removeAloneCharacters(needed, targetSwf);
                classMappedAlterTags.put(tag, alterTag);
                changedCharacterIds.put(((CharacterTag) tag).getCharacterId(), targetSameNameCharacter.getCharacterId());
                classMappedTags.put(tag, targetSameNameCharacter);
            }
            items.removeAll(ignoredItems);

            Set<TreeItem> allDeps = new LinkedIdentityHashSet<>();
            allDeps.addAll(items);
            Map<TreeItem, Tag> depToItem = new HashMap<>();
            for (TreeItem item : items) {
                Tag tag = (Tag) item;
                Set<TreeItem> deps = getDependenciesSet(Arrays.asList(item));
                for (TreeItem dep : deps) {
                    if (!allDeps.contains(dep)) {
                        allDeps.add(dep);
                        depToItem.put(dep, tag);
                    }
                }
            }
            allDeps.removeAll(items);
            allDeps.addAll(items);

            Set<TreeItem> itemsSet = new LinkedIdentityHashSet<>();
            itemsSet.addAll(items);

            for (TreeItem item : allDeps) {
                Tag tag = (Tag) item;
                if (tag.getSwf() == null) {
                    continue;
                }
                SWF sourceSwf = tag.getSwf();
                sourceSwfs.add(sourceSwf);

                Timelined realTargetTimelined;
                Tag realPosition;

                if (itemsSet.contains(tag)) {
                    realTargetTimelined = classMappedAlterTags.get(tag).getTimelined();
                    realPosition = classMappedAlterTags.get(tag);
                } else {
                    realTargetTimelined = classMappedAlterTags.get(depToItem.get(tag)).getTimelined();
                    realPosition = classMappedAlterTags.get(depToItem.get(tag));
                }

                ReadOnlyTagList tags = realTargetTimelined.getTags();
                int positionInt = realPosition == null ? tags.size() : tags.indexOf(realPosition);

                Tag copyTag = tag.cloneTag();
                copyTag.setSwf(targetSwf, true);
                copyTag.setTimelined(realTargetTimelined);
                if (!itemsSet.contains(tag)
                        && (tag instanceof CharacterTag)) {
                    CharacterTag characterTag = (CharacterTag) copyTag;
                    int oldCharacterId = characterTag.getCharacterId();
                    int newCharacterId = checkUniqueCharacterId(copyTag);

                    changedCharacterIds.put(oldCharacterId, newCharacterId);
                }
                realTargetTimelined.addTag(positionInt, copyTag);
                if (itemsSet.contains(tag)) {
                    realTargetTimelined.removeTag(classMappedAlterTags.get(tag));
                    ((CharacterTag) copyTag).setClassNames(classMappedTags.get(tag).getClassNames());
                    ((CharacterTag) copyTag).setExportName(classMappedTags.get(tag).getExportName());
                }

                targetSwf.updateCharacters();
                targetSwf.getCharacters(true); // force rebuild character id cache
                copyTag.setModified(true);
                newTags.add(copyTag);
                if (itemsSet.contains(tag)) {
                    mainPanel.replaceItemPin(classMappedTags.get(tag), copyTag);
                }
            }
            for (int oldCharacterId : changedCharacterIds.keySet()) {
                int newCharacterId = changedCharacterIds.get(oldCharacterId);
                for (Tag newTag : newTags) {
                    // todo: avoid double replaces
                    newTag.replaceCharacter(oldCharacterId, newCharacterId);
                    if ((newTag instanceof CharacterIdTag) && !(newTag instanceof CharacterTag)) {
                        CharacterIdTag characterIdTag = (CharacterIdTag) newTag;
                        if (characterIdTag.getCharacterId() == oldCharacterId) {
                            characterIdTag.setCharacterId(newCharacterId);
                        }
                    }
                }
            }

            for (SWF sourceSwf : sourceSwfs) {
                if (sourceSwf != targetSwf) {
                    sourceSwf.assignExportNamesToSymbols();
                    sourceSwf.assignClassesToSymbols();
                    sourceSwf.clearImageCache();
                    sourceSwf.clearShapeCache();
                    sourceSwf.updateCharacters();
                    sourceSwf.computeDependentCharacters();
                    sourceSwf.computeDependentFrames();
                    sourceSwf.resetTimelines(sourceSwf);
                }
            }
            targetSwf.assignExportNamesToSymbols();
            targetSwf.assignClassesToSymbols();
            targetSwf.clearImageCache();
            targetSwf.clearShapeCache();
            targetSwf.updateCharacters();
            targetSwf.computeDependentCharacters();
            targetSwf.computeDependentFrames();
            targetSwf.resetTimelines(targetSwf);

            mainPanel.refreshTree(targetSwf);
        } catch (InterruptedException | IOException ex) {
            Logger.getLogger(TagTreeContextMenu.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void copyFrameToActionPerformed(ActionEvent evt, List<TreeItem> items, SWF targetSwf) {
        SelectFramePositionDialog selectPositionDialog = new SelectFramePositionDialog(mainPanel.getMainFrame().getWindow(), targetSwf);
        if (selectPositionDialog.showDialog() != AppDialog.OK_OPTION) {
            return;
        }
        int position = selectPositionDialog.getSelectedFrame();
        Timelined timelined = selectPositionDialog.getSelectedTimelined();
        copyOrMoveFrames(new LinkedHashSet<TreeItem>(items), false, timelined, position);
    }

    private void copyTagWithDependenciesToActionPerformed(ActionEvent evt, List<TreeItem> items, SWF targetSwf) {
        SelectTagPositionDialog selectPositionDialog = new SelectTagPositionDialog(mainPanel.getMainFrame().getWindow(), targetSwf, true);
        if (selectPositionDialog.showDialog() != AppDialog.OK_OPTION) {
            return;
        }
        Tag position = selectPositionDialog.getSelectedTag();
        Timelined timelined = selectPositionDialog.getSelectedTimelined();

        copyOrMoveTags(getDependenciesSet(items), false, timelined, position);
    }

    private void moveTagWithDependenciesToActionPerformed(ActionEvent evt, List<TreeItem> items, SWF targetSwf) {
        SelectTagPositionDialog selectPositionDialog = new SelectTagPositionDialog(mainPanel.getMainFrame().getWindow(), targetSwf, true);
        if (selectPositionDialog.showDialog() != AppDialog.OK_OPTION) {
            return;
        }
        Tag position = selectPositionDialog.getSelectedTag();
        Timelined timelined = selectPositionDialog.getSelectedTimelined();

        copyOrMoveTags(getDependenciesSet(items), true, timelined, position);
    }

    private void applyUnpackerActionPerformed(Packer packer) {
        List<TreeItem> sel = getSelectedItems();
        String key = null;
        if (packer.usesKey()) {
            key = ViewMessages.showInputDialog(mainPanel, AppStrings.translate("packer.key").replace("%packer%", packer.getName()), AppStrings.translate("packer.key.title").replace("%packer%", packer.getName()), "");
            if (key == null || key.isEmpty()) {
                return;
            }
        }
        for (TreeItem item : sel) {
            BinaryDataInterface binaryData = (BinaryDataInterface) item;
            if (!binaryData.unpack(packer, key)) {
                ViewMessages.showMessageDialog(mainPanel, AppStrings.translate("error.wrong.packer").replace("%item%", item.toString()).replace("%packer%", packer.getName()), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
            }
        }
        mainPanel.refreshTree();
    }

    private void openSwfInsideActionPerformed(ActionEvent evt) {
        List<TreeItem> sel = getSelectedItems();
        List<BinaryDataInterface> binaryDatas = new ArrayList<>();
        for (TreeItem item : sel) {
            BinaryDataInterface binaryData = (BinaryDataInterface) item;
            if (binaryData.isSwfData()) {
                binaryDatas.add((BinaryDataInterface) item);
            }
        }

        mainPanel.loadFromBinaryTag(binaryDatas);
    }

    private void replaceWithTagActionPerformed(ActionEvent evt) {
        TreeItem itemr = getCurrentItem();
        if (itemr == null) {
            return;
        }

        SWF swf = (SWF) itemr.getOpenable();
        CharacterTag characterTag = (CharacterTag) itemr;
        int characterId = characterTag.getCharacterId();
        ReplaceCharacterDialog replaceCharacterDialog = new ReplaceCharacterDialog(Main.getDefaultDialogsOwner());
        if (replaceCharacterDialog.showDialog(swf, characterId) == AppDialog.OK_OPTION) {
            int newCharacterId = replaceCharacterDialog.getCharacterId();
            swf.replaceCharacterTags(characterTag, newCharacterId);
            mainPanel.refreshTree(swf);
        }
    }

    private void convertShapeTypeActionPerformed(ActionEvent evt) {
        List<TreeItem> itemr = getSelectedItems();
        if (itemr.isEmpty()) {
            return;
        }
        int currentShapeNum = 0;
        int min = 0;
        int minForced = 0;

        ShapeTypeConverter converter = new ShapeTypeConverter();

        if (itemr.size() == 1) {
            ShapeTag sh = (ShapeTag) itemr.get(0);
            currentShapeNum = sh.getShapeNum();
            min = converter.getMinShapeNum(sh);
            minForced = converter.getForcedMinShapeNum(sh);
        }

        ConvertShapeTypeDialog dialog = new ConvertShapeTypeDialog(Main.getDefaultDialogsOwner(), currentShapeNum, minForced, min);

        int shapeNum = dialog.showDialog();

        if (shapeNum == 0) {
            return;
        }

        for (TreeItem item : itemr) {
            ShapeTag sh = (ShapeTag) item;
            int newShapeNum = shapeNum;
            int forcedMin = converter.getForcedMinShapeNum(sh);
            if (newShapeNum < forcedMin) {
                newShapeNum = forcedMin;
            }
            if (sh.getShapeNum() == newShapeNum) {
                continue;
            }
            converter.convertCharacter(sh.getSwf(), sh.getCharacterId(), newShapeNum);
        }

        mainPanel.refreshTree();
        if (itemr.size() == 1) {
            ShapeTag sh = (ShapeTag) itemr.get(0);
            mainPanel.setTagTreeSelectedNode(mainPanel.getCurrentTree(), sh.getSwf().getCharacter(sh.getCharacterId()));
        }
    }

    private void convertPlaceObjectTypeActionPerformed(ActionEvent evt) {
        List<TreeItem> itemr = getSelectedItems();
        if (itemr.isEmpty()) {
            return;
        }
        int currentPlaceObjectNum = 0;
        int min = 0;

        PlaceObjectTypeConverter converter = new PlaceObjectTypeConverter();

        if (itemr.size() == 1) {
            PlaceObjectTypeTag sh = (PlaceObjectTypeTag) itemr.get(0);
            currentPlaceObjectNum = sh.getPlaceObjectNum();
            min = converter.getMinPlaceNum(sh);
        }

        ConvertPlaceObjectTypeDialog dialog = new ConvertPlaceObjectTypeDialog(Main.getDefaultDialogsOwner(), currentPlaceObjectNum, min);

        int placeNum = dialog.showDialog();

        if (placeNum == 0) {
            return;
        }

        PlaceObjectTypeTag lastConverted = null;
        for (TreeItem item : itemr) {
            PlaceObjectTypeTag pl = (PlaceObjectTypeTag) item;
            if (pl.getPlaceObjectNum() == placeNum) {
                continue;
            }
            lastConverted = converter.convertTagType(pl, placeNum);
        }

        mainPanel.refreshTree();
        if (itemr.size() == 1) {
            mainPanel.setTagTreeSelectedNode(mainPanel.getCurrentTree(), lastConverted);
        }
    }

    private void replaceRefsWithTagActionPerformed(ActionEvent evt) {
        TreeItem itemr = getCurrentItem();
        if (itemr == null) {
            return;
        }

        SWF swf = (SWF) itemr.getOpenable();
        CharacterTag characterTag = (CharacterTag) itemr;
        int characterId = characterTag.getCharacterId();
        ReplaceCharacterDialog replaceCharacterDialog = new ReplaceCharacterDialog(Main.getDefaultDialogsOwner());
        if (replaceCharacterDialog.showDialog(swf, characterId) == AppDialog.OK_OPTION) {
            int newCharacterId = replaceCharacterDialog.getCharacterId();

            for (Tag tag : swf.getTags()) {
                replaceRef(tag, characterId, newCharacterId);
            }

            swf.assignExportNamesToSymbols();
            swf.assignClassesToSymbols();
            swf.clearImageCache();
            swf.clearShapeCache();
            swf.updateCharacters();
            swf.computeDependentCharacters();
            swf.computeDependentFrames();
            swf.resetTimeline();
            mainPanel.refreshTree(swf);
        }
    }

    private void replaceRef(Tag tag, int characterId, int newCharacterId) {
        if (tag instanceof DefineSpriteTag) {
            DefineSpriteTag sprite = (DefineSpriteTag) tag;
            for (Tag subTag : sprite.getTags()) {
                replaceRef(subTag, characterId, newCharacterId);
            }
            sprite.resetTimeline();
            sprite.clearReadOnlyListCache();
            return;
        }

        if ((tag instanceof CharacterIdTag) && !(tag instanceof CharacterTag)) {
            CharacterIdTag charIdTag = (CharacterIdTag) tag;
            if (charIdTag.getCharacterId() == characterId) {
                charIdTag.setCharacterId(newCharacterId);
                tag.setModified(true);
            }
        }
        tag.replaceCharacter(characterId, newCharacterId);
    }

    private void abcExplorerActionPerformed(ActionEvent evt) {
        TreeItem item = getCurrentItem();
        if (item == null) {
            return;
        }
        if (item instanceof ClassesListTreeModel) {
            ClassesListTreeModel clsTreeModel = (ClassesListTreeModel) item;
            item = clsTreeModel.getOpenable();
        }

        if (item instanceof ABCContainerTag) {
            ABCContainerTag cnt = (ABCContainerTag) item;
            mainPanel.showAbcExplorer(cnt.getSwf(), cnt.getABC());
            return;
        }

        if (item instanceof SWF) {
            mainPanel.showAbcExplorer((SWF) item, null);
            return;
        }

        if (item instanceof ABC) {
            ABC abc = (ABC) item;
            mainPanel.showAbcExplorer(abc, abc);
            return;
        }

        if (item instanceof ScriptPack) {
            ScriptPack pack = (ScriptPack) item;
            ABCExplorerDialog dialog = mainPanel.showAbcExplorer(pack.getOpenable(), pack.abc);
            if (!pack.isSimple && !pack.traitIndices.isEmpty()) {
                dialog.selectTrait(pack.scriptIndex, -1, pack.traitIndices.get(0), GraphTextWriter.TRAIT_SCRIPT_INITIALIZER);
            } else {
                dialog.selectScriptInfo(pack.scriptIndex);
            }
        }

        if (item instanceof AS3Package) {
            AS3Package pkg = (AS3Package) item;
            if (pkg.isCompoundScript()) {
                ScriptPack pack = pkg.getCompoundInitializerPack();
                ABCExplorerDialog dialog = mainPanel.showAbcExplorer(pack.getOpenable(), pack.abc);
                dialog.selectScriptInfo(pack.scriptIndex);
            }
        }
    }

    private void cleanAbcActionPerformed(ActionEvent evt) {
        TreeItem item = getCurrentItem();
        if (item == null) {
            return;
        }
        if (ViewMessages.showConfirmDialog(this, AppStrings.translate("warning.cleanAbc"), AppStrings.translate("message.warning"), JOptionPane.OK_CANCEL_OPTION, Configuration.warningAbcClean, JOptionPane.OK_OPTION) != JOptionPane.OK_OPTION) {
            return;
        }
        ABCCleaner cleaner = new ABCCleaner();
        if (item instanceof ABCContainerTag) {
            ABCContainerTag cnt = (ABCContainerTag) item;
            cleaner.clean(cnt.getABC());
        }
        if (item instanceof ABC) {
            cleaner.clean((ABC) item);
        }
        if (item instanceof SWF) {
            SWF swf = (SWF) item;
            for (ABCContainerTag cnt : swf.getAbcList()) {
                cleaner.clean(cnt.getABC());
            }
        }
        Main.getMainFrame().getPanel().refreshTree();
    }

    private void rawEditActionPerformed(ActionEvent evt) {
        TreeItem itemr = getCurrentItem();
        if (itemr == null) {
            return;
        }

        TreePath sel = mainPanel.getCurrentTree().getSelectionPath();
        if (sel == null || sel.getLastPathComponent() != itemr) {
            mainPanel.setTagTreeSelectedNode(mainPanel.getCurrentTree(), itemr);
        }
        mainPanel.showGenericTag((Tag) itemr);
    }

    private void gotoDocumentClassActionPerformed(ActionEvent evt) {
        TreeItem item = getCurrentItem();
        item.getOpenable();
        mainPanel.gotoDocumentClass((SWF) item.getOpenable());
    }

    private void jumpToCharacterActionPerformed(ActionEvent evt) {
        TreeItem itemj = getCurrentItem();
        if (itemj == null || !(itemj instanceof HasCharacterId)) {
            return;
        }

        HasCharacterId hasCharacterId = (HasCharacterId) itemj;
        mainPanel.setTagTreeSelectedNode(mainPanel.getCurrentTree(), ((SWF) itemj.getOpenable()).getCharacter(hasCharacterId.getCharacterId()));
    }

    /*
    How setting AS linkage should work:
    User selects linkage identifier (the one really stored in ExportAssets of the character)
    If the file is AS1/2:
            If exists DoInitAction tag for that character, that has Object.registerClass("oldLinkageIdentifier", cls)
                    readonly class name is displayed
                    readonly identifier is displayed
                    exit
            else
                    (optional) User selects desired AS2 classname and parent class name to be created
    If the linkage did not exist before (no ExportAssets had such id) and linkage identifier is not empty,
            user can select whether to use existing ExportAssets tag (if some exists) or to create new

    On pressing OK:
    If the linkage did not exist before (no ExportAssets had such id) and the new identifier is not empty,
            user selects existing ExportAssets tag via dialog or selects position for new
                    if the dialog is cancelled, exit
    If the file is AS1/2:
            If the user chose to create new class (cls is its fully qualified name)
                    Frame of Exportassets is determined
                    New DoInitAction for the character is created in the EA frame which has Object.registerClass("linkageIdentifier", cls) in code
                    New empty DefineSprite is created in frame 1, and its name in ExportAssets tag is set to "__Packages." + cls
                    New DoInitAction for the DefineSprite in frame 1 is created and it's filled with new cls Class code
    The Exportassets tag is modified with the new linkage identifier
     */
    private void setAsLinkageActionPerformed(ActionEvent evt) {
        CharacterTag ch = (CharacterTag) getCurrentItem();
        SWF swf = ch.getSwf();
        AsLinkageDialog d = new AsLinkageDialog(Main.getDefaultDialogsOwner(), swf, ch.getCharacterId());
        if (d.showDialog() != AppDialog.OK_OPTION) {
            return;
        }
        String identifier = d.getSelectedIdentifier();
        String className = d.getSelectedClass();
        String classParent = d.getSelectedParentClass();
        ExportAssetsTag ea = d.getSelectedExportAssetsTag();

        if (ea == null) {
            ea = new ExportAssetsTag(swf);
            ea.setTimelined(swf);
            if (d.getSelectedPosition() == null) {
                swf.addTag(ea);
            } else {
                swf.addTag(swf.indexOfTag(d.getSelectedPosition()), ea);
            }
        }

        if (!className.isEmpty()) {
            int frame = 1;
            int eaFrame = -1;
            int regInsertPos = -1;
            ReadOnlyTagList tags = swf.getTags();
            for (int i = 0; i < tags.size(); i++) {
                Tag t = tags.get(i);
                if (t == ea) {
                    eaFrame = frame;
                }
                if (t instanceof ShowFrameTag) {
                    if (frame == eaFrame) {
                        regInsertPos = i;
                        break;
                    }
                    frame++;
                }
            }

            DoInitActionTag regDoInit = new DoInitActionTag(swf);
            regDoInit.spriteId = ch.getCharacterId();
            regDoInit.setTimelined(swf);

            ActionScript2Parser regParser = new ActionScript2Parser(swf, regDoInit);

            String[] parts = className.contains(".") ? className.split("\\.") : new String[]{className};
            DottedChain classDottedChain = new DottedChain(parts);

            try {
                List<Action> regActions = regParser.actionsFromString("Object.registerClass(\"" + Helper.escapePCodeString(identifier) + "\"," + classDottedChain.toPrintableString(false) + ");", swf.getCharset());
                regDoInit.setActions(regActions);
            } catch (ActionParseException | IOException | CompilationException | InterruptedException ex) {
                //ignore
            }

            swf.addTag(regInsertPos, regDoInit);

            int insertPos = 0;

            tags = swf.getTags();

            for (int i = 0; i < tags.size(); i++) {
                Tag t = tags.get(i);
                if (t instanceof ShowFrameTag) {
                    insertPos = i;
                    break;
                }
            }

            int classCharacterId = swf.getNextCharacterId();
            DefineSpriteTag classSprite = new DefineSpriteTag(swf);
            classSprite.spriteId = classCharacterId;
            classSprite.hasEndTag = true;
            classSprite.setTimelined(swf);

            String exportName = "__Packages." + className;

            ExportAssetsTag classExportAssets = new ExportAssetsTag(swf);
            classExportAssets.names = new ArrayList<>();
            classExportAssets.names.add(exportName);
            classExportAssets.tags = new ArrayList<>();
            classExportAssets.tags.add(classCharacterId);
            classExportAssets.setTimelined(swf);

            DoInitActionTag classDoInit = new DoInitActionTag(swf);
            classDoInit.spriteId = classCharacterId;
            classDoInit.setTimelined(swf);

            ActionScript2Parser parser = new ActionScript2Parser(swf, classDoInit);

            String[] partsParent = classParent.contains(".") ? classParent.split("\\.") : new String[]{classParent};
            DottedChain dcParent = new DottedChain(partsParent);

            try {
                List<Action> actions = parser.actionsFromString("class " + classDottedChain.toPrintableString(false) + (classParent.isEmpty() ? "" : " extends " + dcParent.toPrintableString(false)) + "{}", swf.getCharset());
                classDoInit.setActions(actions);
            } catch (ActionParseException | IOException | CompilationException | InterruptedException ex) {
                //ignore
            }

            classSprite.setExportName(exportName);

            swf.addTag(insertPos, classSprite);
            swf.addTag(insertPos + 1, classExportAssets);
            swf.addTag(insertPos + 2, classDoInit);

        }

        boolean found = false;
        for (int i = ea.names.size() - 1; i >= 0; i--) {
            if (ea.tags.get(i) == ch.getCharacterId()) {
                if (identifier.isEmpty()) {
                    ea.tags.remove(i);
                    ea.names.remove(i);
                } else {
                    ea.names.set(i, identifier);
                    found = true;
                }
            }
        }
        if (!identifier.isEmpty() && !found) {
            ea.tags.add(ch.getCharacterId());
            ea.names.add(identifier);
        }
        ea.setModified(true);
        if (ea.names.isEmpty()) {
            swf.removeTag(ea);
        }

        swf.clearAllCache();
        swf.assignExportNamesToSymbols();
        swf.setModified(true);
        mainPanel.refreshTree(swf);
    }

    /*
    How set class to character mapping work in AS3:
    a) a character is selected
    b) if the character already has assigned more than 1 class then exit with a message that user must do this manually
    c) new class name is entered, must be different than previous
    d) if new class name is already assigned to different character, exit
    e) if the classname is not empty find the ABC, where class with that name is defined
            if found
                    determine its frame
                    find nearest frame >= ABC frame, such that the character is defined in it (or is defined earlier in the file)
                    if the SymbolClass exists in that frame, use that one
                            otherwise create new SymbolClass in that frame
            else (not found)
                    suggest creating new class
                            select parent class (extends) - the default is set according to type of the character(sound = flash.media.Sound, etc.)
                            choose whether to use existing ABC or add new ABC
                            AFTER PRESSING OK:
                            select existing ABC or add new ABC (select its position). 
                            find nearest frame >= ABC frame, such that the character is defined in it (or is defined earlier in the file)
                            if the SymbolClass exists in that frame, use that one
                                    otherwise create new SymbolClass in that frame

                    or suggest not creating class, set only its name
                            if the character did not have assigned name before (= no SymbolClass used)
                                choose whether to use existing Symbolclass or add new
                                AFTER PRESSING OK:
                                select existing SymbolClass or add new SymbolClass (on selected position). 
                                (Selected frame for target SymbolClass must have defined the character in it or earlier in the file)
    AFTER PRESSING OK:
    I. if there is previously assigned classname, find associated SymbolClass and remove the mapping from it
    II. if the previous Symbolclass is empty and is not target SymbolClass, remove it
    III. if the new classname is not empty, add new character mapping to new SymbolClass determined
     */
    private void setAs3ClassLinkageActionPerformed(ActionEvent evt) {
        CharacterTag ch = (CharacterTag) getCurrentItem();
        SWF swf = ch.getSwf();
        As3ClassLinkageDialog d = new As3ClassLinkageDialog(Main.getDefaultDialogsOwner(), swf, ch.getCharacterId());
        if (d.showDialog() != AppDialog.OK_OPTION) {
            return;
        }

        int abcFrame = d.getAbcFrame();
        int characterFrame = d.getCharacterFrame();

        SymbolClassTag selectedSymbolClass = d.getSelectedSymbolClassTag();
        ABCContainerTag selectedAbcContainer = d.getSelectedAbcContainer();
        String className = d.getSelectedClass();

        if (className.isEmpty() && !ch.getClassNames().isEmpty()) {
            SymbolClassTag sct = d.getSelectedSymbolClassTag();
            for (int i = sct.tags.size() - 1; i >= 0; i--) {
                if (sct.tags.get(i) == ch.getCharacterId()) {
                    sct.names.remove(i);
                    sct.tags.remove(i);
                }
            }
            if (sct.names.isEmpty()) {
                swf.removeTag(sct);
            }
        } else {
            String parentClassName = d.getSelectedParentClass();

            if (!d.isClassFound() && !d.doCreateClass() && selectedSymbolClass == null) { //we selected position of new SymbolClass
                selectedSymbolClass = new SymbolClassTag(swf);
                selectedSymbolClass.setTimelined(swf);
                Tag pos = d.getSelectedPosition();
                if (pos == null) {
                    swf.addTag(selectedSymbolClass);
                } else {
                    swf.addTag(swf.indexOfTag(pos), selectedSymbolClass);
                }
            }

            if (!d.isClassFound() && d.doCreateClass()) {
                if (d.getSelectedAbcContainer() == null) {
                    selectedAbcContainer = new DoABC2Tag(swf);
                    ((Tag) selectedAbcContainer).setTimelined(swf);
                    Tag pos = d.getSelectedPosition();
                    if (pos == null) {
                        swf.addTag((Tag) selectedAbcContainer);
                    } else {
                        swf.addTag(swf.indexOfTag(pos), (Tag) selectedAbcContainer);
                    }
                }

                String pkg = className.contains(".") ? className.substring(0, className.lastIndexOf(".")) : "";
                String classSimpleName = className.contains(".") ? className.substring(className.lastIndexOf(".") + 1) : className;
                String fileName = className.replace(".", "/");
                String[] pkgParts = new String[0];
                if (!pkg.isEmpty()) {
                    if (pkg.contains(".")) {
                        pkgParts = pkg.split("\\.");
                    } else {
                        pkgParts = new String[]{pkg};
                    }
                }
                try {
                    AbcIndexing abcIndex = swf.getAbcIndex();
                    abcIndex.selectAbc(selectedAbcContainer.getABC());
                    ActionScript3Parser parser = new ActionScript3Parser(abcIndex);

                    DottedChain dc = new DottedChain(pkgParts);
                    String script = "package " + dc.toPrintableString(true) + " {"
                            + (parentClassName.isEmpty() ? "" : "import " + parentClassName + ";")
                            + "public class " + IdentifiersDeobfuscation.printIdentifier(true, classSimpleName) + (parentClassName.isEmpty() ? "" : " extends " + parentClassName) + " {"
                            + " }"
                            + "}";
                    parser.addScript(script, fileName, 0, 0, swf.getDocumentClass(), selectedAbcContainer.getABC());
                } catch (IOException | InterruptedException | AVM2ParseException | CompilationException ex) {
                    Logger.getLogger(TagTreeContextMenu.class.getName()).log(Level.SEVERE, "Error during script compilation", ex);
                }

                ((Tag) selectedAbcContainer).setModified(true);
            }
        }

        if (selectedSymbolClass == null) {
            int symbolClassFrame = Math.max(abcFrame, characterFrame);
            int frame = 1;
            int pos = 0;
            int symbolClassPos = -1;
            for (Tag t : swf.getTags()) {
                if ((t instanceof SymbolClassTag) && (symbolClassFrame == frame)) {
                    selectedSymbolClass = (SymbolClassTag) t;
                    break;
                }
                if (t instanceof ShowFrameTag) {
                    if (symbolClassFrame == frame) {
                        symbolClassPos = pos;
                    }
                    frame++;
                }
                pos++;
            }
            if (selectedSymbolClass == null) {
                if (symbolClassPos == -1) {
                    symbolClassPos = pos;
                }
                selectedSymbolClass = new SymbolClassTag(swf);
                selectedSymbolClass.setTimelined(swf);
                swf.addTag(symbolClassPos, selectedSymbolClass);
            }
        }

        //remove previously assigned name
        ReadOnlyTagList tags = swf.getTags();
        for (int j = 0; j < tags.size(); j++) {
            Tag t = tags.get(j);
            if (t instanceof SymbolClassTag) {
                SymbolClassTag sct = (SymbolClassTag) t;
                for (int i = sct.tags.size() - 1; i >= 0; i--) {
                    if (sct.tags.get(i) == ch.getCharacterId()) {
                        sct.names.remove(i);
                        sct.tags.remove(i);
                        sct.setModified(true);
                    }
                }
                if (sct.names.isEmpty() && sct != selectedSymbolClass) {
                    swf.removeTag(t);
                }
            }
        }

        selectedSymbolClass.tags.add(ch.getCharacterId());
        selectedSymbolClass.names.add(className);
        selectedSymbolClass.setModified(true);

        swf.clearAllCache();
        swf.assignClassesToSymbols();
        swf.setModified(true);
        mainPanel.refreshTree(swf);
    }

    private void expandRecursiveActionPerformed(ActionEvent evt) {
        AbstractTagTree tree = getTree();
        TreePath path = tree.getFullModel().getTreePath(getCurrentItem());
        if (path == null) {
            return;
        }
        View.expandTreeNodes(tree, path, true);
    }

    private void collapseRecursiveActionPerformed(ActionEvent evt) {
        AbstractTagTree tree = getTree();
        TreePath path = tree.getFullModel().getTreePath(getCurrentItem());
        if (path == null) {
            return;
        }
        View.expandTreeNodes(tree, path, false);
    }

    private void textSearchActionPerformed(ActionEvent evt) {
        Main.getMainFrame().getPanel().searchInActionScriptOrText(null, getCurrentItem().getOpenable(), true);
    }

    private void addAs3ClassActionPerformed(ActionEvent evt) {
        AbstractTagTree tree = getTree();
        //using tagTree only here is safe since tagListTree does not have AS3 classes
        List<TreeItem> sel = getSelectedItems();
        if (!sel.isEmpty()) {
            SWF swf = null;
            Openable openable = null;
            String preselected = "";
            if (sel.get(0) instanceof ClassesListTreeModel) {
                ClassesListTreeModel cl = (ClassesListTreeModel) sel.get(0);
                openable = cl.getOpenable();
            }
            if (sel.get(0) instanceof ABC) {
                openable = (ABC) sel.get(0);
            }
            if (sel.get(0) instanceof ABCContainerTag) {
                openable = ((Tag) sel.get(0)).getOpenable();
            }

            if (sel.get(0) instanceof AS3Package) {
                AS3Package pkg = (AS3Package) sel.get(0);
                openable = pkg.getOpenable();
                TreePath tp = tree.getFullModel().getTreePath(sel.get(0));
                Object[] path = tp.getPath();
                for (int p = path.length - 1; p >= 0; p--) {
                    if (path[p] instanceof ClassesListTreeModel) {
                        break;
                    }
                    if (path[p] instanceof ABC) {
                        break;
                    }
                    if (path[p] instanceof ABCContainerTag) {
                        break;
                    }
                    if (((AS3Package) path[p]).isDefaultPackage()) {
                        break;
                    }
                    preselected = ((AS3Package) path[p]).packageName + "." + preselected;
                }
            }

            if (openable instanceof SWF) {
                swf = (SWF) openable;
            } else {
                swf = ((ABC) openable).getSwf();
            }

            TreePath scriptsPath = tree.getSelectionPaths()[0];
            while (!(scriptsPath.getLastPathComponent() instanceof ClassesListTreeModel)
                    && !(scriptsPath.getLastPathComponent() instanceof ABC)
                    && !(scriptsPath.getLastPathComponent() instanceof ABCContainerTag)) {
                scriptsPath = scriptsPath.getParentPath();
            }

            ABCContainerTag preselectedContainer = null;

            TreeItem scriptsNode = (TreeItem) scriptsPath.getLastPathComponent();

            if (scriptsNode instanceof ABC) {
                preselectedContainer = ((ABC) scriptsNode).parentTag;
            } else if (scriptsNode instanceof ABCContainerTag) {
                preselectedContainer = (ABCContainerTag) scriptsNode;
            }

            AddClassDialog acd = new AddClassDialog(Main.getDefaultDialogsOwner(), openable, preselectedContainer);
            if (acd.showDialog(preselected) != AppDialog.OK_OPTION) {
                return;
            }
            String className = acd.getSelectedClass();
            String[] parts = className.contains(".") ? className.split("\\.") : new String[]{className};

            ABCContainerTag doAbc = acd.getSelectedAbcContainer();

            if (doAbc == null) {
                DoABC2Tag doAbc2 = new DoABC2Tag(swf);

                Timelined timelined = acd.getSelectedTimelined();
                Tag position = acd.getSelectedPosition();
                if (position == null) {
                    timelined.addTag(doAbc2);
                } else {
                    timelined.addTag(timelined.indexOfTag(position), doAbc2);
                }
                doAbc2.setTimelined(acd.getSelectedTimelined());
                doAbc2.name = className;
                doAbc = doAbc2;
            }

            List<ABC> abcs = new ArrayList<>();
            for (ABCContainerTag ct : swf.getAbcList()) {
                abcs.add(ct.getABC());
            }

            String pkg = className.contains(".") ? className.substring(0, className.lastIndexOf(".")) : "";
            String classSimpleName = className.contains(".") ? className.substring(className.lastIndexOf(".") + 1) : className;
            String fileName = className.replace(".", "/");
            String[] pkgParts = new String[0];
            if (!pkg.isEmpty()) {
                if (pkg.contains(".")) {
                    pkgParts = pkg.split("\\.");
                } else {
                    pkgParts = new String[]{pkg};
                }
            }
            try {
                AbcIndexing abcIndex = swf.getAbcIndex();
                abcIndex.selectAbc(doAbc.getABC());
                ActionScript3Parser parser = new ActionScript3Parser(abcIndex);

                DottedChain dc = new DottedChain(pkgParts);
                String script = "package " + dc.toPrintableString(true) + " {"
                        + "public class " + IdentifiersDeobfuscation.printIdentifier(true, classSimpleName) + " {"
                        + " }"
                        + "}";
                parser.addScript(script, fileName, 0, 0, swf.getDocumentClass(), doAbc.getABC());
            } catch (IOException | InterruptedException | AVM2ParseException | CompilationException ex) {
                Logger.getLogger(TagTreeContextMenu.class.getName()).log(Level.SEVERE, "Error during script compilation", ex);
            }

            ((Tag) doAbc).setModified(true);
            swf.clearAllCache();
            swf.setModified(true);
            mainPanel.refreshTree(swf);

            Object item;

            if ((mainPanel.getCurrentView() == MainPanel.VIEW_RESOURCES) && (openable instanceof SWF)) {
                item = mainPanel.tagTree.getFullModel().getScriptsNode((SWF) openable);
            } else if (openable instanceof ABC) {
                item = openable;
            } else { //SWF on taglist, should be DoABCContainer
                item = scriptsPath.getLastPathComponent();
            }

            loopparts:
            for (int i = 0; i < parts.length; i++) {
                for (TreeItem ti : tree.getFullModel().getAllChildren(item)) {
                    if ((ti instanceof AS3Package) && ((AS3Package) ti).isFlat()) {
                        AS3Package pti = (AS3Package) ti;
                        if ((pkg.isEmpty() && pti.isDefaultPackage()) || (!pti.isDefaultPackage() && pkg.equals(pti.packageName))) {
                            item = pti;
                            i = parts.length - 1 - 1;
                            break;
                        }
                        continue;
                    }
                    if (ti instanceof AS3ClassTreeItem) {
                        AS3ClassTreeItem cti = (AS3ClassTreeItem) ti;

                        if (parts[i].equals(cti.getNameWithNamespaceSuffix())) {
                            item = ti;
                            break;
                        }
                    }
                }
            }
            mainPanel.setTagTreeSelectedNode(mainPanel.getCurrentTree(), (TreeItem) item);
        }
    }

    /**
     * @param targetFrame 1 based frame
     */
    private void addFrameScript(SWF swf, Timelined tim, int targetFrame) {
        DoActionTag doAction = new DoActionTag(swf);
        doAction.setTimelined(tim);

        ReadOnlyTagList tagList = tim.getTags();
        int frame = 1;
        boolean frameFound = false;
        for (int i = 0; i < tagList.size(); i++) {
            Tag t = tagList.get(i);
            if (t instanceof ShowFrameTag) {
                if (frame == targetFrame) {
                    tim.addTag(i, doAction);
                    frameFound = true;
                    break;
                }
                frame++;
            }
        }
        if (!frameFound) {
            //inserting new frames
            for (; frame < targetFrame; frame++) {
                tim.addTag(new ShowFrameTag(swf));
            }
            tim.addTag(doAction);
            tim.addTag(new ShowFrameTag(swf));
            if (tim instanceof DefineSpriteTag) {
                ((DefineSpriteTag) tim).frameCount = targetFrame;
            } else {
                swf.frameCount = targetFrame;
            }
        }

        //TreePath selection = mainPanel.tagTree.getFullModel().getTreePath(sel.get(0));
        TreePath swfPath = mainPanel.tagTree.getFullModel().getTreePath(swf); //selection.getParentPath();
        tim.resetTimeline();
        mainPanel.refreshTree(swf);

        FolderItem scriptsNode = (FolderItem) mainPanel.tagTree.getFullModel().getScriptsNode(swf);
        TreePath scriptsPath = swfPath.pathByAddingChild(scriptsNode);

        if (tim instanceof SWF) {
            for (TreeItem subItem : scriptsNode.subItems) {
                if (subItem instanceof FrameScript) {
                    if (((FrameScript) subItem).getFrame().frame + 1 == targetFrame) {
                        TreePath framePath = scriptsPath.pathByAddingChild(subItem);
                        List<? extends TreeItem> doActionTags = mainPanel.tagTree.getFullModel().getAllChildren(subItem);
                        TreePath doActionPath = framePath.pathByAddingChild(doActionTags.get(doActionTags.size() - 1));
                        mainPanel.tagTree.setSelectionPath(doActionPath);
                        mainPanel.tagTree.scrollPathToVisible(doActionPath);
                        break;
                    }
                }
            }
        } else { //sprite
            for (TreeItem subItem : scriptsNode.subItems) {
                if (subItem instanceof TagScript) {
                    if (((TagScript) subItem).getTag() == tim) {
                        TreePath spritePath = scriptsPath.pathByAddingChild(subItem);
                        TagScript ts = (TagScript) subItem;
                        for (TreeItem f : ts.getFrames()) {
                            if (f instanceof FrameScript) {
                                FrameScript fs = (FrameScript) f;
                                if (fs.getFrame().frame + 1 == targetFrame) {
                                    TreePath framePath = spritePath.pathByAddingChild(fs);
                                    List<? extends TreeItem> doActionTags = mainPanel.tagTree.getFullModel().getAllChildren(fs);
                                    TreePath doActionPath = framePath.pathByAddingChild(doActionTags.get(doActionTags.size() - 1));
                                    mainPanel.tagTree.setSelectionPath(doActionPath);
                                    mainPanel.tagTree.scrollPathToVisible(doActionPath);
                                    break;
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    private void addButtonEventScript(SWF swf, DefineButton2Tag button) {
        BUTTONCONDACTION bca = new BUTTONCONDACTION(swf, button);
        bca.condOverUpToOverDown = true; //press
        if (!button.actions.isEmpty()) {
            button.actions.get(button.actions.size() - 1).isLast = false;
        }
        bca.isLast = true;
        button.actions.add(bca);
        button.setModified(true);

        button.resetTimeline();
        mainPanel.refreshTree(swf);

        TreePath swfPath = mainPanel.tagTree.getFullModel().getTreePath(swf);
        FolderItem scriptsNode = (FolderItem) mainPanel.tagTree.getFullModel().getScriptsNode(swf);
        TreePath scriptsPath = swfPath.pathByAddingChild(scriptsNode);
        for (TreeItem subItem : scriptsNode.subItems) {
            if (subItem instanceof TagScript) {
                if (((TagScript) subItem).getTag() == button) {
                    TreePath buttonPath = scriptsPath.pathByAddingChild(subItem);
                    TreePath buttonCondPath = buttonPath.pathByAddingChild(bca);
                    mainPanel.tagTree.setSelectionPath(buttonCondPath);
                    mainPanel.tagTree.scrollPathToVisible(buttonCondPath);
                    break;
                }
            }
        }
    }

    /**
     * @param frame 1 based frame
     */
    private void addInstanceEventScript(SWF swf, Timelined tim, PlaceObjectTypeTag placeType, int frame) {
        CLIPACTIONS clipActions = null;
        if (placeType instanceof PlaceObjectTag) {
            ReadOnlyTagList tags = tim.getTags();
            PlaceObjectTag place = (PlaceObjectTag) placeType;
            clipActions = new CLIPACTIONS();
            for (int i = 0; i < tags.size(); i++) {
                if (tags.get(i) == placeType) {
                    PlaceObject2Tag place2 = new PlaceObject2Tag(swf, false, place.depth, place.characterId, place.matrix,
                            new CXFORMWITHALPHA(place.colorTransform), -1, null, -1, clipActions);
                    place2.setTimelined(tim);
                    tim.replaceTag(i, place2);
                    placeType = place2;
                    break;
                }
            }
        }
        if (placeType instanceof PlaceObject2Tag) {
            PlaceObject2Tag place2 = (PlaceObject2Tag) placeType;
            if (!place2.placeFlagHasClipActions) {
                clipActions = place2.clipActions = new CLIPACTIONS();
                place2.placeFlagHasClipActions = true;
            } else {
                clipActions = place2.clipActions;
            }
        }
        if (placeType instanceof PlaceObject3Tag) {
            PlaceObject3Tag place3 = (PlaceObject3Tag) placeType;
            if (!place3.placeFlagHasClipActions) {
                clipActions = place3.clipActions = new CLIPACTIONS();
                place3.placeFlagHasClipActions = true;
            } else {
                clipActions = place3.clipActions;
            }
        }
        if (placeType instanceof PlaceObject4Tag) {
            PlaceObject4Tag place4 = (PlaceObject4Tag) placeType;
            if (!place4.placeFlagHasClipActions) {
                clipActions = place4.clipActions = new CLIPACTIONS();
                place4.placeFlagHasClipActions = true;
            } else {
                clipActions = place4.clipActions;
            }
        }
        CLIPACTIONRECORD clipActionRecord = new CLIPACTIONRECORD(swf, (Tag) placeType);
        clipActionRecord.setParentClipActions(clipActions);
        clipActionRecord.eventFlags.clipEventPress = true;

        clipActions.clipActionRecords.add(clipActionRecord);
        clipActions.calculateAllEventFlags();

        ((Tag) placeType).setModified(true);

        TreePath swfPath = mainPanel.tagTree.getFullModel().getTreePath(swf);
        tim.resetTimeline();
        mainPanel.refreshTree(swf);

        FolderItem scriptsNode = (FolderItem) mainPanel.tagTree.getFullModel().getScriptsNode(swf);
        TreePath scriptsPath = swfPath.pathByAddingChild(scriptsNode);

        for (TreeItem subItem : scriptsNode.subItems) {
            if (tim instanceof DefineSpriteTag) {
                if (subItem instanceof TagScript) {
                    if (((TagScript) subItem).getTag() == tim) {
                        TreePath spritePaths = scriptsPath.pathByAddingChild(subItem);
                        List<TreeItem> frames = ((TagScript) subItem).getFrames();
                        loopframes:
                        for (TreeItem f : frames) {
                            if (f instanceof FrameScript) {
                                FrameScript fs = (FrameScript) f;
                                if (fs.getFrame().frame + 1 == frame) {
                                    TreePath framePath = spritePaths.pathByAddingChild(f);
                                    List<? extends TreeItem> subs = mainPanel.tagTree.getFullModel().getAllChildren(fs);
                                    for (TreeItem t : subs) {
                                        if (t instanceof TagScript) {
                                            if (((TagScript) t).getTag() == placeType) {
                                                TreePath placePath = framePath.pathByAddingChild(t);
                                                TreePath clipActionRecordPath = placePath.pathByAddingChild(clipActionRecord);
                                                mainPanel.tagTree.setSelectionPath(clipActionRecordPath);
                                                mainPanel.tagTree.scrollPathToVisible(clipActionRecordPath);
                                                break loopframes;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                if (subItem instanceof FrameScript) {
                    FrameScript fs = (FrameScript) subItem;
                    if (fs.getFrame().frame + 1 == frame) {
                        TreePath framePath = scriptsPath.pathByAddingChild(fs);
                        List<? extends TreeItem> subs = mainPanel.tagTree.getFullModel().getAllChildren(fs);
                        for (TreeItem t : subs) {
                            if (t instanceof TagScript) {
                                if (((TagScript) t).getTag() == placeType) {
                                    TreePath placePath = framePath.pathByAddingChild(t);
                                    TreePath clipActionRecordPath = placePath.pathByAddingChild(clipActionRecord);
                                    mainPanel.tagTree.setSelectionPath(clipActionRecordPath);
                                    mainPanel.tagTree.scrollPathToVisible(clipActionRecordPath);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void addSpriteInitScript(SWF swf, DefineSpriteTag sprite) {
        DoInitActionTag doinit = new DoInitActionTag(swf);
        doinit.setTimelined(swf);
        doinit.spriteId = sprite.spriteId;
        ReadOnlyTagList tags = swf.getTags();
        int addPos = -1;
        for (int i = 0; i < tags.size(); i++) {
            Tag t = tags.get(i);
            if (t instanceof PlaceObjectTypeTag) {
                int placeCharacterId = ((PlaceObjectTypeTag) t).getCharacterId();
                if (usesCharacter(swf, placeCharacterId, sprite.spriteId)) {
                    addPos = i;
                    break;
                }
            }
        }
        if (addPos == -1) {
            addPos = tags.size();
        }
        swf.addTag(addPos, doinit);

        swf.clearAllCache();
        swf.setModified(true);
        mainPanel.refreshTree(swf);

        TreePath swfPath = mainPanel.tagTree.getFullModel().getTreePath(swf);
        FolderItem scriptsNode = (FolderItem) mainPanel.tagTree.getFullModel().getScriptsNode(swf);
        TreePath scriptsPath = swfPath.pathByAddingChild(scriptsNode);

        TreePath doinitPath = scriptsPath.pathByAddingChild(doinit);
        mainPanel.tagTree.setSelectionPath(doinitPath);
        mainPanel.tagTree.scrollPathToVisible(doinitPath);
    }

    private void addAs12FrameScriptActionPerformed(ActionEvent evt) {
        TreeItem item = getCurrentItem();
        int frame = -1;
        if (item instanceof Frame) {
            frame = ((Frame) item).frame;
        }
        if (item instanceof FrameScript) {
            frame = ((FrameScript) item).getFrame().frame;
        }
        TreeItem parent = getTree().getFullModel().getParent(item);

        if (parent instanceof Timelined) {
            //empty
        } else if (parent instanceof FolderItem) {
            parent = getTree().getFullModel().getParent(parent);
        } else if (parent instanceof TagScript) {
            parent = ((TagScript) parent).getTag();
        } else {
            return;
        }

        Timelined tim;
        SWF swf;
        if (parent instanceof DefineSpriteTag) {
            swf = ((DefineSpriteTag) parent).getSwf();
            tim = (DefineSpriteTag) parent;
        } else if (parent instanceof SWF) {
            swf = (SWF) parent;
            tim = swf;
        } else {
            return;
        }

        addFrameScript(swf, tim, frame + 1);
    }

    private void addAs12ButtonEventScriptActionPerformed(ActionEvent evt) {
        TreeItem item = getCurrentItem();
        if (item instanceof TagScript) {
            item = ((TagScript) item).getTag();
        }
        if (!(item instanceof DefineButton2Tag)) {
            return;
        }
        DefineButton2Tag button = (DefineButton2Tag) item;
        SWF swf = button.getSwf();
        addButtonEventScript(swf, button);
    }

    private void addAs12InstanceEventScriptActionPerformed(ActionEvent evt) {
        TreeItem item = getCurrentItem();
        if (item instanceof TagScript) {
            item = ((TagScript) item).getTag();
        }
        if (!(item instanceof PlaceObjectTypeTag)) {
            return;
        }
        PlaceObjectTypeTag placeType = (PlaceObjectTypeTag) item;
        SWF swf = placeType.getSwf();
        Timelined tim = placeType.getTimelined();
        TreeItem parent = getTree().getFullModel().getParent(item);
        int frame;
        if (parent instanceof Frame) {
            frame = ((Frame) parent).frame;
        } else if (parent instanceof FrameScript) {
            frame = ((FrameScript) parent).getFrame().frame;
        } else {
            return;
        }

        addInstanceEventScript(swf, tim, placeType, frame + 1);
    }

    private void addAs12SpriteInitScriptActionPerformed(ActionEvent evt) {
        TreeItem item = getCurrentItem();
        if (item instanceof TagScript) {
            item = ((TagScript) item).getTag();
        }
        if (!(item instanceof DefineSpriteTag)) {
            return;
        }
        DefineSpriteTag sprite = (DefineSpriteTag) item;
        addSpriteInitScript(sprite.getSwf(), sprite);
    }

    private void addAs12ScriptActionPerformed(ActionEvent evt) {
        List<TreeItem> sel = getSelectedItems();
        if (!sel.isEmpty()) {
            if (sel.get(0) instanceof FolderItem) {

                FolderItem folder = (FolderItem) sel.get(0);
                SWF swf = (SWF) folder.getOpenable();

                AddScriptDialog addScriptDialog = new AddScriptDialog(Main.getDefaultDialogsOwner(), swf);
                if (addScriptDialog.showDialog() == JOptionPane.OK_OPTION) {
                    if ((addScriptDialog.getScriptType() == AddScriptDialog.TYPE_FRAME)
                            || (addScriptDialog.getScriptType() == AddScriptDialog.TYPE_SPRITE_FRAME)) {
                        Timelined tim = swf;
                        if (addScriptDialog.getScriptType() == AddScriptDialog.TYPE_SPRITE_FRAME) {
                            tim = addScriptDialog.getSprite();
                        }
                        int targetFrame = addScriptDialog.getFrame();
                        addFrameScript(swf, tim, targetFrame);
                    } else if (addScriptDialog.getScriptType() == AddScriptDialog.TYPE_BUTTON_EVENT) {
                        DefineButton2Tag button = addScriptDialog.getButton();
                        addButtonEventScript(swf, button);
                    } else if (addScriptDialog.getScriptType() == AddScriptDialog.TYPE_INSTANCE_EVENT) {
                        DefineSpriteTag sprite = addScriptDialog.getSprite();
                        int frame = addScriptDialog.getFrame();
                        PlaceObjectTypeTag placeType = addScriptDialog.getPlaceObject();

                        Timelined tim = swf;
                        if (sprite != null) {
                            tim = sprite;
                        }
                        addInstanceEventScript(swf, tim, placeType, frame);
                    } else if (addScriptDialog.getScriptType() == AddScriptDialog.TYPE_CLASS) {
                        String className = addScriptDialog.getClassName();
                        ReadOnlyTagList tags = swf.getTags();
                        List<Integer> exportedIds = new ArrayList<>();
                        for (int i = 0; i < tags.size(); i++) {
                            if (tags.get(i) instanceof ExportAssetsTag) {
                                ExportAssetsTag ea = (ExportAssetsTag) tags.get(i);
                                exportedIds.addAll(ea.tags);
                            }
                        }

                        int insertPos = -1;
                        for (int i = 0; i < tags.size(); i++) {
                            if (tags.get(i) instanceof DoInitActionTag) {
                                DoInitActionTag doinit = (DoInitActionTag) tags.get(i);
                                if (!exportedIds.contains(doinit.spriteId)) {
                                    //this is #initpragma, make sure class is inserted before it
                                    insertPos = i;
                                    break;
                                }
                            }
                        }
                        if (insertPos == -1) {
                            for (int i = 0; i < tags.size(); i++) {
                                if (tags.get(i) instanceof ShowFrameTag) {
                                    insertPos = i;
                                    break;
                                }
                            }
                        }

                        if (insertPos > -1) {
                            int characterId = swf.getNextCharacterId();
                            DefineSpriteTag sprite = new DefineSpriteTag(swf);
                            sprite.spriteId = characterId;
                            sprite.hasEndTag = true;
                            sprite.setTimelined(swf);

                            String exportName = "__Packages." + className;

                            ExportAssetsTag exportAssets = new ExportAssetsTag(swf);
                            exportAssets.names = new ArrayList<>();
                            exportAssets.names.add(exportName);
                            exportAssets.tags = new ArrayList<>();
                            exportAssets.tags.add(characterId);
                            exportAssets.setTimelined(swf);

                            DoInitActionTag doInit = new DoInitActionTag(swf);
                            doInit.spriteId = characterId;
                            doInit.setTimelined(swf);

                            ActionScript2Parser parser = new ActionScript2Parser(swf, doInit);

                            String[] parts = className.contains(".") ? className.split("\\.") : new String[]{className};
                            DottedChain dc = new DottedChain(parts);

                            try {
                                List<Action> actions = parser.actionsFromString("class " + dc.toPrintableString(false) + "{}", swf.getCharset());
                                doInit.setActions(actions);
                            } catch (ActionParseException | IOException | CompilationException
                                    | InterruptedException ex) {
                                //ignore
                            }

                            sprite.setExportName(exportName);

                            swf.addTag(insertPos, sprite);
                            swf.addTag(insertPos + 1, exportAssets);
                            swf.addTag(insertPos + 2, doInit);

                            swf.clearAllCache();
                            swf.setModified(true);
                            mainPanel.refreshTree(swf);

                            TreePath selection = mainPanel.tagTree.getSelectionPath();
                            TreePath swfPath = selection.getParentPath();
                            FolderItem scriptsNode = (FolderItem) mainPanel.tagTree.getFullModel().getScriptsNode(swf);
                            TreePath scriptsPath = swfPath.pathByAddingChild(scriptsNode);
                            String[] classParts = className.contains(".") ? className.split("\\.") : new String[]{className};

                            for (TreeItem subItem : scriptsNode.subItems) {
                                if (subItem instanceof AS2Package) {
                                    AS2Package pkg = (AS2Package) subItem;
                                    if (pkg.getName().equals("__Packages")) {
                                        TreePath classPath = scriptsPath.pathByAddingChild(pkg);
                                        for (int i = 0; i < classParts.length - 1; i++) {
                                            List<TreeItem> subs = pkg.getAllChildren();
                                            for (TreeItem s : subs) {
                                                if (s instanceof AS2Package) {
                                                    if (((AS2Package) s).getName().equals(classParts[i])) {
                                                        pkg = (AS2Package) s;
                                                        classPath = classPath.pathByAddingChild(pkg);
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                        classPath = classPath.pathByAddingChild(doInit);
                                        mainPanel.tagTree.setSelectionPath(classPath);
                                        break;
                                    }
                                }
                            }

                        }
                    } else if (addScriptDialog.getScriptType() == AddScriptDialog.TYPE_SPRITE_INIT) {
                        DefineSpriteTag sprite = addScriptDialog.getSprite();
                        addSpriteInitScript(swf, sprite);
                    }
                }
            }
        }
    }

    private boolean usesCharacter(SWF swf, int characterId, int searchedCharacterId) {
        if (characterId == searchedCharacterId) {
            return true;
        }
        CharacterTag character = swf.getCharacter(characterId);
        if (character instanceof DefineSpriteTag) {
            DefineSpriteTag sprite = (DefineSpriteTag) character;
            for (Tag t : sprite.getTags()) {
                if (t instanceof PlaceObjectTypeTag) {
                    int placeCharacterId = ((PlaceObjectTypeTag) t).getCharacterId();
                    if (usesCharacter(swf, placeCharacterId, searchedCharacterId)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void getAllAS3PackageScriptPacks(AS3Package pkg, List<ScriptPack> out) {
        out.addAll(pkg.getScriptPacks());
        for (AS3Package sub : pkg.getSubPackages()) {
            getAllAS3PackageScriptPacks(sub, out);
        }
    }

    private void populateScriptSubs(TreeItem item, List<TreeItem> out) {
        List<? extends TreeItem> subs = getTree().getFullModel().getAllChildren(item);
        for (TreeItem t : subs) {
            //TreePath tPath = path.pathByAddingChild(t);
            if ((t instanceof TagScript) && (((TagScript) t).getTag() instanceof ASMSource)) {
                out.add(t);
            } else if (t instanceof ASMSource) {
                out.add(t);
            } else {
                populateScriptSubs(t, out);
            }
        }
    }

    private void getAllAS2PackageScriptPacks(AS2Package pkg, List<ASMSource> out) {
        out.addAll(pkg.scripts.values());
        for (AS2Package sub : pkg.subPackages.values()) {
            getAllAS2PackageScriptPacks(sub, out);
        }
    }

    public void removeItemActionPerformed(ActionEvent evt, boolean removeDependencies) {

        List<TreeItem> sel = getSelectedItems();

        List<Tag> tagsToRemove = new ArrayList<>();
        List<Frame> framesToRemove = new ArrayList<>();
        List<Object> itemsToRemove = new ArrayList<>();
        List<Object> itemsToRemoveParents = new ArrayList<>();
        List<Object> itemsToRemoveSprites = new ArrayList<>();

        TreeItem itemLast = null;
        int itemCountFix = 0;
        for (int i = 0; i < sel.size(); i++) {
            TreeItem item = sel.get(i); //TreeItem) tps.get(i).getLastPathComponent();
            if ((item instanceof FrameScript) || (item instanceof TagScript)) {
                List<TreeItem> subs = new ArrayList<>();
                populateScriptSubs(item, subs);
                int cnt = 0;
                for (TreeItem tp : subs) {
                    if (!sel.contains(tp)) {
                        if (cnt > 0) {
                            itemCountFix--;
                        }
                        cnt++;
                        sel.add(tp);
                        itemLast = item;
                    }
                }
            }
        }
        for (TreeItem item : sel) {
            if (item instanceof AS3Package) {
                itemsToRemove.add(item);
                itemsToRemoveParents.add(new Object());
                itemsToRemoveSprites.add(new Object());
            } else if (item instanceof AS2Package) {
                itemsToRemove.add(item);
                itemsToRemoveParents.add(new Object());
                itemsToRemoveSprites.add(new Object());
            } else if (item instanceof Tag) {
                tagsToRemove.add((Tag) item);
            } else if ((item instanceof TagScript) && (((TagScript) item).getTag() instanceof ASMSource)) {
                tagsToRemove.add(((TagScript) item).getTag());
            } else if (item instanceof Frame) {
                Frame frameNode = (Frame) item;
                Frame frame = frameNode.timeline.getFrame(frameNode.frame);
                /*if (frame.showFrameTag != null) {
                    tagsToRemove.add(frame.showFrameTag);
                } else {
                    // this should be the last frame, so remove the inner tags
                    tagsToRemove.addAll(frame.innerTags);
                }*/
                framesToRemove.add(frame);
            } else if (item instanceof BUTTONCONDACTION) {
                itemsToRemove.add(item);
                //This is slow. Let's hope users won't delete that much BUTTONCONDACTIONs
                TreePath path = getTree().getFullModel().getTreePath(item);
                itemsToRemoveParents.add(((TagScript) path.getParentPath().getLastPathComponent()).getTag());
                itemsToRemoveSprites.add(new Object());
            } else if (item instanceof CLIPACTIONRECORD) {
                itemsToRemove.add(item);
                //This is slow. Let's hope users won't delete that much CLIPACTIONRECORDs
                TreePath path = getTree().getFullModel().getTreePath(item);
                Object sprite = path.getParentPath().getParentPath().getParentPath().getLastPathComponent();
                if (sprite instanceof TagScript) {
                    sprite = ((TagScript) sprite).getTag();
                }
                itemsToRemoveParents.add(((TagScript) path.getParentPath().getLastPathComponent()).getTag());
                itemsToRemoveSprites.add(sprite);
            } else if (item instanceof ScriptPack) {
                if (!itemsToRemove.contains(item)) { //If parent package is selected, do not add it twice
                    itemsToRemove.add(item);
                    itemsToRemoveParents.add(new Object());
                    itemsToRemoveSprites.add(new Object());
                }
            } else if (item instanceof BUTTONRECORD) {
                itemsToRemove.add(item);
                itemsToRemoveParents.add(((BUTTONRECORD) item).getTag());
                itemsToRemoveSprites.add(new Object());
            } else if (item instanceof Cookie) {
                itemsToRemove.add(item);
                itemsToRemoveParents.add(new Object());
                itemsToRemoveSprites.add(new Object());
            }
        }

        if (tagsToRemove.size() > 0 || itemsToRemove.size() > 0 || framesToRemove.size() > 0) {
            String confirmationMessage;
            if (tagsToRemove.size() + itemsToRemove.size() + framesToRemove.size() == 1) {
                Object toRemove;
                if (tagsToRemove.size() == 1) {
                    toRemove = tagsToRemove.get(0);
                } else if (framesToRemove.size() == 1) {
                    toRemove = framesToRemove.get(0);
                } else {
                    toRemove = itemsToRemove.get(0);
                }
                if (itemLast != null) {
                    toRemove = itemLast;
                }
                confirmationMessage = mainPanel.translate("message.confirm.remove" + (removeDependencies ? "" : ".nodep")).replace("%item%", toRemove.toString());
            } else {
                confirmationMessage = mainPanel.translate("message.confirm.removemultiple" + (removeDependencies ? "" : ".nodep")).replace("%count%", Integer.toString(tagsToRemove.size() + itemsToRemove.size() + itemCountFix + framesToRemove.size()));
            }

            if (ViewMessages.showConfirmDialog(mainPanel, confirmationMessage, mainPanel.translate("message.confirm"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {

                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        Map<SWF, List<Tag>> tagsToRemoveBySwf = new HashMap<>();
                        Set<SWF> swfsToClearCache = new HashSet<>();
                        Set<SWF> swfToAssignClassesToSymbols = new HashSet<>();
                        Set<SWF> swfToAssignExportNamesToSymbols = new HashSet<>();

                        for (int i = 0; i < itemsToRemove.size(); i++) {
                            Object item = itemsToRemove.get(i);
                            if (item instanceof AS3Package) {
                                AS3Package pkg = (AS3Package) item;
                                if (pkg.isCompoundScript()) {
                                    continue;
                                }
                                List<ScriptPack> subScriptPacks = new ArrayList<>();
                                getAllAS3PackageScriptPacks((AS3Package) item, subScriptPacks);
                                for (ScriptPack pack : subScriptPacks) {
                                    if (!itemsToRemove.contains(pack)) {
                                        itemsToRemove.add(pack);
                                        itemsToRemoveParents.add(new Object());
                                        itemsToRemoveSprites.add(new Object());
                                    }
                                }
                            }
                            if (item instanceof AS2Package) {
                                List<ASMSource> subAsmSources = new ArrayList<>();
                                getAllAS2PackageScriptPacks((AS2Package) item, subAsmSources);
                                for (ASMSource asmSource : subAsmSources) {
                                    if (!itemsToRemove.contains(asmSource)) {
                                        tagsToRemove.add((Tag) asmSource);
                                    }
                                }
                            }
                        }

                        List<ABC> abcsToPack = new ArrayList<>();

                        for (int i = 0; i < itemsToRemove.size(); i++) {
                            Object item = itemsToRemove.get(i);
                            Object parent = itemsToRemoveParents.get(i);

                            if (item instanceof Cookie) {
                                Cookie cookie = (Cookie) item;
                                ((Cookie) item).getSolFile().delete();
                            }

                            if (item instanceof BUTTONRECORD) {
                                ButtonTag button = (ButtonTag) parent;
                                button.getRecords().remove((BUTTONRECORD) item);
                                List<BUTTONRECORD> unmodifiedRecords = new ArrayList<>();
                                for (BUTTONRECORD rec : button.getRecords()) {
                                    if (!rec.isModified()) {
                                        unmodifiedRecords.add(rec);
                                    }
                                }
                                button.setModified(true);
                                for (BUTTONRECORD rec : unmodifiedRecords) {
                                    rec.setModified(false);
                                }
                            }

                            if (item instanceof BUTTONCONDACTION) {
                                DefineButton2Tag button = (DefineButton2Tag) parent;
                                BUTTONCONDACTION buttonCondAction = (BUTTONCONDACTION) item;
                                button.actions.remove(buttonCondAction);
                                if (buttonCondAction.isLast) {
                                    if (!button.actions.isEmpty()) {
                                        button.actions.get(button.actions.size() - 1).isLast = true;
                                    }
                                }
                                button.setModified(true);
                            }
                            if (item instanceof CLIPACTIONRECORD) {
                                PlaceObjectTypeTag place = (PlaceObjectTypeTag) parent;
                                Timelined tim = (itemsToRemoveSprites.get(i) instanceof DefineSpriteTag) ? (DefineSpriteTag) itemsToRemoveSprites.get(i) : place.getSwf();

                                CLIPACTIONRECORD clipActionRecord = (CLIPACTIONRECORD) item;
                                CLIPACTIONS clipActions = place.getClipActions();
                                clipActions.clipActionRecords.remove(clipActionRecord);
                                if (clipActions.clipActionRecords.isEmpty()) {
                                    place.setPlaceFlagHasClipActions(false);
                                    place.setClipActions(null);
                                }
                                clipActions.calculateAllEventFlags();
                                place.setModified(true);
                                tim.resetTimeline();
                            }
                            if (item instanceof ScriptPack) {
                                ScriptPack sp = (ScriptPack) item;
                                sp.delete(sp.abc, true);
                                abcsToPack.add(sp.abc);
                                Openable openable = sp.getOpenable();
                                SWF swf = (openable instanceof SWF) ? (SWF) openable : ((ABC) openable).getSwf();
                                swfsToClearCache.add(swf);
                                for (ABCContainerTag ct : swf.getAbcList()) {
                                    if (ct.getABC() == sp.abc) {
                                        ((Tag) ct).setModified(true);
                                        break;
                                    }
                                }
                            }

                            if (item instanceof AS3Package) {
                                AS3Package pkg = (AS3Package) item;
                                if (pkg.isCompoundScript()) {
                                    ScriptPack sp = pkg.getCompoundInitializerPack();
                                    sp.abc.script_info.get(sp.scriptIndex).delete(sp.abc, true);
                                    abcsToPack.add(sp.abc);
                                    Openable openable = sp.getOpenable();
                                    SWF swf = (openable instanceof SWF) ? (SWF) openable : ((ABC) openable).getSwf();
                                    swfsToClearCache.add(swf);
                                    for (ABCContainerTag ct : swf.getAbcList()) {
                                        if (ct.getABC() == sp.abc) {
                                            ((Tag) ct).setModified(true);
                                            break;
                                        }
                                    }
                                }
                            }

                            if (item instanceof TreeItem) {
                                mainPanel.unpinItem((TreeItem) item);
                            }
                        }

                        for (ABC abc : abcsToPack) {
                            abc.pack();

                            ABCContainerTag container = null;
                            for (ABCContainerTag ct : abc.getSwf().getAbcList()) {
                                if (ct.getABC() == abc) {
                                    container = ct;
                                    break;
                                }
                            }

                            if (abc.script_info.isEmpty()) { //all scripts in abc were removed
                                abc.getSwf().removeTag((Tag) container);
                                abc.getSwf().setModified(true);
                            } else {
                                ((Tag) container).setModified(true);
                            }
                        }

                        for (Tag tag : tagsToRemove) {

                            SWF swf = tag.getSwf();

                            if (tag instanceof SymbolClassTag) {
                                swfToAssignClassesToSymbols.add(swf);
                            }

                            if (tag instanceof ExportAssetsTag) {
                                swfToAssignExportNamesToSymbols.add(swf);
                            }

                            if (!tagsToRemoveBySwf.containsKey(swf)) {
                                tagsToRemoveBySwf.put(swf, new ArrayList<>());
                            }

                            tagsToRemoveBySwf.get(swf).add(tag);
                            mainPanel.unpinItem(tag);
                        }

                        for (SWF swf : tagsToRemoveBySwf.keySet()) {
                            swf.removeTags(tagsToRemoveBySwf.get(swf), removeDependencies, new TagRemoveListener() {
                                @Override
                                public void tagRemoved(Tag tag) {
                                    mainPanel.unpinItem(tag);
                                }
                            });
                            swf.computeDependentCharacters();
                            swf.computeDependentFrames();
                        }

                        Set<Timelined> timelinesToUpdate = new LinkedHashSet<>();
                        for (Tag tag : tagsToRemove) {
                            if (tag instanceof ShowFrameTag) {
                                Timelined t = tag.getTimelined();
                                timelinesToUpdate.add(t);
                            }
                        }
                        for (Timelined t : timelinesToUpdate) {
                            t.setFrameCount(t.getTimeline().getFrameCount());
                        }

                        for (SWF swf : swfsToClearCache) {
                            swf.clearAllCache();
                        }

                        for (SWF swf : swfToAssignClassesToSymbols) {
                            swf.assignClassesToSymbols();
                        }

                        for (SWF swf : swfToAssignExportNamesToSymbols) {
                            swf.assignExportNamesToSymbols();
                        }

                        if (!framesToRemove.isEmpty()) {
                            Timelined tim = framesToRemove.get(0).timeline.timelined;
                            SWF swf = tim.getSwf();
                            removeFrames(new LinkedHashSet<>(framesToRemove));

                            swf.resetTimelines(tim);
                            swf.clearImageCache();
                            tim.setFrameCount(tim.getTimeline().getFrameCount());

                            mainPanel.refreshTree(swf);
                        }
                    }
                };

                if (!(mainPanel.folderPreviewCard.isVisible() && mainPanel.folderPreviewPanel.isSomethingSelected())) {
                    mainPanel.treeOperation(r);
                } else {
                    //current folder must stay selected
                    r.run();
                    mainPanel.refreshTree();
                }
                mainPanel.savePins();
                mainPanel.updateClassesList();
            }
        }
    }

    private void undoTagActionPerformed(ActionEvent evt) {
        AbstractTagTree tree = getTree();
        List<TreeItem> sel = getSelectedItems();
        Set<SWF> computeSWFs = new LinkedIdentityHashSet<>();

        for (TreeItem item : sel) {
            if (item instanceof Tag) {
                try {
                    Tag tag = (Tag) item;
                    tag.undo();
                    SWF swf = tag.getSwf();
                    swf.clearAllCache();
                    if (tag instanceof Timelined) {
                        ((Timelined) tag).resetTimeline();
                    }
                    tag.getTimelined().resetTimeline();
                    //For example DefineButton and its DefineButtonCxForm
                    if ((tag instanceof CharacterIdTag) && (!(tag instanceof CharacterTag))) {
                        CharacterTag parentCharacter = swf.getCharacter(((CharacterIdTag) tag).getCharacterId());
                        if (parentCharacter instanceof Timelined) {
                            ((Timelined) parentCharacter).resetTimeline();
                        }
                    }
                    computeSWFs.add(swf);
                    tree.getFullModel().updateNode(item);
                } catch (InterruptedException | IOException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            }
        }

        for (SWF swf : computeSWFs) {
            swf.computeDependentCharacters();
            swf.computeDependentFrames();
        }

        mainPanel.repaintTree();
    }

    private void closeSwfActionPerformed(ActionEvent evt) {
        List<TreeItem> sel = getSelectedItems();
        for (TreeItem item : sel) {
            if (item instanceof SWF) {
                SWF swf = (SWF) item;
                if (swf.binaryData != null) {
                    // embedded swf
                    swf.binaryData.setInnerSwf(null);
                    swf.clearTagSwfs();
                } else {
                    Main.closeFile(swf.openableList);
                }
            } else if (item instanceof Openable) {
                Main.closeFile(((Openable) item).getOpenableList());
            } else if (item instanceof OpenableList) {
                Main.closeFile((OpenableList) item);
            }
        }
        mainPanel.refreshTree();
    }

    private void cloneActionPerformed(ActionEvent e) {
        List<TreeItem> items = getSelectedItems();
        /* Currently useless since all selected items must have the same parent
         * but a better way to detect for parent/child selection
         * could remove that limitation */
        Set<SWF> swfs = new HashSet<>();

        try {
            for (TreeItem item : items) {
                SWF swf = (SWF) item.getOpenable();
                swfs.add(swf);

                if (item instanceof Tag) {
                    Tag tag = (Tag) item;

                    Tag copyTag = tag.cloneTag();
                    copyTag.setSwf(swf, true);
                    Timelined timelined = tag.getTimelined();
                    int idx = timelined.indexOfTag(tag);
                    copyTag.setTimelined(timelined);

                    checkUniqueCharacterId(copyTag);
                    timelined.addTag(idx + 1, copyTag);
                    copyTag.setModified(true);

                    timelined.resetTimeline();
                } else if (item instanceof Frame) {
                    Frame f = (Frame) item;
                    Timelined timelined = f.timeline.timelined;

                    int i;
                    boolean isLast = f.showFrameTag == null;
                    if (isLast) {
                        f.showFrameTag = new ShowFrameTag(swf);
                        Tag last = f.innerTags.get(f.innerTags.size() - 1);
                        int idx = timelined.indexOfTag(last) + 1;
                        timelined.addTag(idx, f.showFrameTag);
                        f.showFrameTag.setTimelined(timelined);
                        i = idx;
                    } else {
                        i = timelined.indexOfTag(f.showFrameTag);
                    }

                    for (Tag tag : f.innerTags) {
                        Tag copyTag = tag.cloneTag();
                        copyTag.setSwf(swf, true);

                        copyTag.setTimelined(timelined);

                        checkUniqueCharacterId(copyTag);

                        timelined.addTag(++i, copyTag);

                        copyTag.setModified(true);
                    }

                    if (!isLast) {
                        ShowFrameTag next = new ShowFrameTag(swf);
                        timelined.addTag(++i, next);
                        next.setTimelined(timelined);
                    }

                    timelined.resetTimeline();
                }
            }

            for (SWF swf : swfs) {
                swf.assignExportNamesToSymbols();
                swf.assignClassesToSymbols();
                swf.clearImageCache();
                swf.clearShapeCache();
                swf.updateCharacters();
                mainPanel.refreshTree(swf);
            }

        } catch (IOException | InterruptedException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    private void showInFramesFolderActionPerformed(ActionEvent evt) {
        TreeItem item = getCurrentItem();
        if (item instanceof SceneFrame) {
            item = ((SceneFrame) item).getFrame();
        }
        if (item instanceof Scene) {
            item = ((Scene) item).getSceneFrame(0).getFrame();
        }
        mainPanel.setTagTreeSelectedNode(mainPanel.tagTree, item);
        mainPanel.updateMenu();
    }

    private void showInResourcesViewActionPerformed(ActionEvent evt) {
        TreeItem item = getCurrentItem();
        mainPanel.showView(MainPanel.VIEW_RESOURCES);
        mainPanel.setTagTreeSelectedNode(mainPanel.tagTree, item);
        mainPanel.updateMenu();
    }

    private void showInTagListViewActionPerformed(ActionEvent evt) {
        TreeItem item = getCurrentItem();
        mainPanel.showView(MainPanel.VIEW_TAGLIST);

        if (item instanceof TagScript) {
            item = ((TagScript) item).getTag();
        }
        if (item instanceof SceneFrame) {
            item = ((SceneFrame) item).getFrame();
        }
        if (item instanceof Scene) {
            item = ((Scene) item).getSceneFrame(0).getFrame();
        }
        mainPanel.setTagTreeSelectedNode(mainPanel.tagListTree, item);
        mainPanel.updateMenu();
    }

    private void showInHexDumpViewActionPerformed(ActionEvent evt) {
        if (mainPanel.isModified()) {
            ViewMessages.showMessageDialog(Main.getDefaultMessagesComponent(), AppStrings.translate("message.warning.hexViewNotUpToDate"), AppStrings.translate("message.warning"), JOptionPane.WARNING_MESSAGE, Configuration.warningHexViewNotUpToDate);
        }
        TreeItem item = getCurrentItem();
        if (item instanceof TagScript) {
            item = ((TagScript) item).getTag();
        }
        mainPanel.showView(MainPanel.VIEW_DUMP);
        mainPanel.dumpTree.setSelectedItem(item);
        mainPanel.updateMenu();
    }

    private void moveTagActionPerformed(ActionEvent evt) {
        Tag t = (Tag) getCurrentItem();
        TreePath path = getTree().getFullModel().getTreePath(t);
        Timelined timelined = null;
        for (int i = path.getPathCount() - 1 - 1 /*not last path component*/; i >= 0; i--) {
            if ((path.getPathComponent(i) instanceof DefineSpriteTag) || (path.getPathComponent(i) instanceof SWF)) {
                timelined = (Timelined) path.getPathComponent(i);
                break;
            }
        }
        if (timelined == null) { //should not happen
            return;
        }

        SelectTagPositionDialog dialog = new SelectTagPositionDialog(Main.getDefaultDialogsOwner(), t.getSwf(), t, timelined, true, false, null, 1);
        if (dialog.showDialog() == AppDialog.OK_OPTION) {
            Tag selectedTag = dialog.getSelectedTag();
            Timelined selectedTimelined = dialog.getSelectedTimelined();

            if (selectedTag == t && selectedTimelined == timelined) {
                return;
            }
            timelined.removeTag(t);
            if (selectedTag == null) {
                selectedTimelined.addTag(t);
            } else {
                selectedTimelined.addTag(selectedTimelined.indexOfTag(selectedTag), t);
            }
            t.setTimelined(selectedTimelined);
            timelined.resetTimeline();
            if (timelined != selectedTimelined) {
                selectedTimelined.resetTimeline();
            }
            mainPanel.refreshTree(t.getSwf());
        }
    }

    private void addFramesBeforeActionPerformed(ActionEvent evt) {
        addFrames(true);
    }

    private int calcFramePositionToAdd(Frame frame, Timelined timelined, boolean before, Reference<Boolean> frameAdd, boolean addingFramesNotTags) {
        ReadOnlyTagList tagsList = timelined.getTags();
        int positionToAdd = -1;
        if (frame == null) {
            positionToAdd = tagsList.size();
        } else {
            if (before && frame.frame == 0) {
                positionToAdd = 0;
            } else {

                //adding frames before frame 0 => at 0
                //adding frames before frame 2 => after second ShowFrameTag
                //adding frames after frame 2 => after third ShowFrameTag
                //adding frames after frame 0 => after first ShowFrameTag
                int f = 0;
                int i = 0;
                for (; i < tagsList.size(); i++) {
                    Tag t = tagsList.get(i);
                    if (t instanceof ShowFrameTag) {
                        f++;

                        if (before && f == frame.frame) {
                            positionToAdd = i;
                            if (addingFramesNotTags) {
                                positionToAdd++;
                            }
                            break;
                        }
                        if (!before && f == frame.frame + 1) {
                            positionToAdd = i + 1;
                            break;
                        }
                    }
                }
                if (f == 0 && !before) { //last showFrameTag not found
                    if (!tagsList.isEmpty()) { //DefineSprite with some tags but no ShowFrameTag
                        frameAdd.setVal(true);
                    }
                    positionToAdd = tagsList.size();
                }
            }
        }
        return positionToAdd;
    }

    private void addFrames(boolean before) {
        TreeItem item = getCurrentItem();
        if (item == null) {
            return;
        }
        Frame frame = null;
        Timelined timelined = null;
        if (item instanceof Frame) {
            frame = (Frame) item;
            timelined = frame.timeline.timelined;
        } else if (item instanceof FolderItem) { //frames folder
            timelined = (SWF) item.getOpenable();
        } else {
            timelined = (Timelined) item;
        }

        while (true) {
            String frameCountString = ViewMessages.showInputDialog(mainPanel.getMainFrame().getWindow(), AppStrings.translate("message.input.addFrames.howmany"), AppStrings.translate("message.input.addFrames.title"), "1");
            if (frameCountString == null) {
                return;
            }
            int frameCount;
            try {
                frameCount = Integer.parseInt(frameCountString);
                if (frameCount < 0) {
                    continue;
                }
                if (frameCount == 0) {
                    return;
                }
                Reference<Boolean> frameAdd = new Reference<>(false);
                int positionToAdd = calcFramePositionToAdd(frame, timelined, before, frameAdd, true);
                if (frameAdd.getVal()) {
                    frameCount++;
                }

                SWF swf = timelined.getTimeline().swf;
                for (int i = 0; i < frameCount; i++) {
                    ShowFrameTag showFrameTag = new ShowFrameTag(swf);
                    showFrameTag.setTimelined(timelined);
                    timelined.addTag(positionToAdd, showFrameTag);
                }
                timelined.resetTimeline();

                if (timelined instanceof SWF) {
                    ((SWF) timelined).frameCount = timelined.getTimeline().getFrameCount();
                } else if (timelined instanceof DefineSpriteTag) {
                    ((DefineSpriteTag) timelined).frameCount = timelined.getTimeline().getFrameCount();
                }
                mainPanel.refreshTree(swf);

            } catch (NumberFormatException nfe) {
                continue;
            }
            break;
        }
    }

    private void addFramesAfterActionPerformed(ActionEvent evt) {
        addFrames(false);
    }

    private void addFramesActionPerformed(ActionEvent evt) {
        addFrames(false); //this works because timeline is selected, no frame
    }

    private void addCopyMoveToMenusSwfList(int kind, SWF singleSwf, OpenableList targetSwfList, JMenuItem menu, List<TreeItem> items, boolean as12, boolean as3) {
        if (targetSwfList.isBundle()) {
            JMenu bundleMenu = new JMenu(targetSwfList.name);
            bundleMenu.setIcon(AbstractTagTree.getIconForType(AbstractTagTree.getTreeNodeType(targetSwfList)));
            menu.add(bundleMenu);
            menu = bundleMenu;
        }
        for (final Openable targetOpenable : targetSwfList) {
            if (targetOpenable instanceof SWF) {
                if (targetOpenable != singleSwf) {
                    SWF swf = (SWF) targetOpenable;
                    if (!as12 && !swf.isAS3()) {
                        continue;
                    }
                    if (!as3 && swf.isAS3()) {
                        continue;
                    }
                    addCopyMoveToMenus(kind, menu, items, targetOpenable.getShortFileName(), (SWF) targetOpenable);
                }
            }
        }
    }

    private void addCopyMoveToMenus(int kind, JMenuItem menu, List<TreeItem> items, String name, SWF targetSwf) {
        JMenuItem swfItem = new JMenuItem(name);
        swfItem.addActionListener((ActionEvent ae) -> {
            switch (kind) {
                case KIND_TAG_MOVETO:
                    moveTagToActionPerformed(ae, items, targetSwf);
                    break;
                case KIND_TAG_MOVETODEPS:
                    moveTagWithDependenciesToActionPerformed(ae, items, targetSwf);
                    break;
                case KIND_TAG_COPYTO:
                    copyTagToActionPerformed(ae, items, targetSwf);
                    break;
                case KIND_TAG_COPYTODEPS:
                    copyTagWithDependenciesToActionPerformed(ae, items, targetSwf);
                    break;
                case KIND_FRAME_MOVETO:
                    moveFrameToActionPerformed(ae, items, targetSwf);
                    break;
                case KIND_FRAME_COPYTO:
                    copyFrameToActionPerformed(ae, items, targetSwf);
                    break;
                case KIND_TAG_COPYTOCLASSOREXPORTNAME:
                    copyTagToReplaceByClassOrExportNameActionPerformed(ae, items, targetSwf);
                    break;
            }
        });
        swfItem.setIcon(View.getIcon("flash16"));
        menu.add(swfItem);

        Map<String, SWF> binaryMap = mainPanel.getSwfsMap(targetSwf);
        for (String key : binaryMap.keySet()) {
            addCopyMoveToMenus(kind, menu, items, key, binaryMap.get(key));
        }
    }

    private void addCopyMoveToFramesMenus(int kind, JMenuItem menu, List<TreeItem> items, String name, SWF targetSwf) {
        JMenuItem swfItem = new JMenuItem(name);
        swfItem.addActionListener((ActionEvent ae) -> {
            switch (kind) {
                case KIND_TAG_MOVETO:
                    moveFrameToActionPerformed(ae, items, targetSwf);
                    break;
                case KIND_TAG_COPYTO:
                    copyFrameToActionPerformed(ae, items, targetSwf);
                    break;
            }
        });
        swfItem.setIcon(View.getIcon("flash16"));
        menu.add(swfItem);

        Map<String, SWF> binaryMap = mainPanel.getSwfsMap(targetSwf);
        for (String key : binaryMap.keySet()) {
            addCopyMoveToFramesMenus(kind, menu, items, key, binaryMap.get(key));
        }
    }

    private void attachTagActionPerformed(ActionEvent evt, TreeItem item, Class<?> cl, TreeNodeType createNodeType) {
        int id = -1;
        try {
            id = cl.getDeclaredField("ID").getInt(null);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(TagTreeContextMenu.class.getName()).log(Level.SEVERE, null, ex);
        }
        Timelined selectedTimelined = ((Tag) item).getTimelined();
        Tag tagToAttachTo = ((Tag) item);
        SWF swf = ((Tag) item).getSwf();
        try {
            Tag t = (Tag) cl.getDeclaredConstructor(SWF.class).newInstance(new Object[]{swf});

            //add mapping
            if (AbstractTagTree.getMappedTagIdsForClass(item.getClass()).contains(id)) {
                if ((t instanceof CharacterIdTag) && (!(t instanceof CharacterTag)) && (item instanceof CharacterTag)) {
                    CharacterIdTag chit = (CharacterIdTag) t;
                    chit.setCharacterId(((CharacterTag) item).getCharacterId());
                }
            }

            t.setTimelined(selectedTimelined);
            selectedTimelined.addTag(selectedTimelined.indexOfTag(tagToAttachTo) + 1, t);
            selectedTimelined.resetTimeline();
            swf.updateCharacters();
            mainPanel.refreshTree(swf);
            mainPanel.setTagTreeSelectedNode(mainPanel.getCurrentTree(), t);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException
                | IllegalArgumentException | InvocationTargetException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    public void copyTagOrFrameToClipboardActionPerformed(ActionEvent evt) {
        List<TreeItem> items = getSelectedItems();
        copyTagOrFrameToClipboardActionPerformed(evt, items);
    }

    public void copyTagOrFrameToClipboardActionPerformed(ActionEvent evt, List<TreeItem> items) {
        mainPanel.copyToClipboard(items);
    }

    private Set<TreeItem> getDependenciesSet(List<TreeItem> items) {
        Openable sourceOpenable = items.get(0).getOpenable();
        if (sourceOpenable instanceof ABC) {
            return new LinkedHashSet<>();
        }
        SWF sourceSwf = (SWF) items.get(0).getOpenable();
        Set<TreeItem> newItems = new LinkedHashSet<>();
        for (TreeItem item : items) {
            Set<Integer> needed = new LinkedHashSet<>();
            Tag tag = (Tag) item;
            tag.getNeededCharactersDeep(needed);
            if (tag instanceof CharacterTag) {
                needed.add(((CharacterTag) tag).getCharacterId());
            }
            for (Integer characterId : needed) {
                CharacterTag neededTag = sourceSwf.getCharacter(characterId);
                newItems.add(neededTag);
                List<Integer> mappedClasses = AbstractTagTree.getMappedTagIdsForClass(neededTag.getClass());
                ReadOnlyTagList tags = neededTag.getTimelined().getTags();
                for (int i = tags.indexOf(neededTag) + 1; i < tags.size(); i++) {
                    if (tags.get(i) instanceof CharacterIdTag) {
                        CharacterIdTag characterIdTag = (CharacterIdTag) tags.get(i);
                        if (mappedClasses.contains(((Tag) characterIdTag).getId()) && characterIdTag.getCharacterId() == characterId) {
                            newItems.add((Tag) characterIdTag);
                        }
                    }
                }
            }
            if (!(tag instanceof CharacterTag)) {
                newItems.add(tag);
            }
        }
        return newItems;
    }

    public void copyTagToClipboardWithDependenciesActionPerformed(ActionEvent evt, List<TreeItem> items) {
        mainPanel.copyToClipboard(getDependenciesSet(items));
    }

    public void cutTagOrFrameToClipboardActionPerformed(ActionEvent evt) {
        List<TreeItem> items = getSelectedItems();
        mainPanel.cutToClipboard(items);
        mainPanel.repaintTree();
    }

    public void cutTagToClipboardWithDependenciesActionPerformed(ActionEvent evt) {
        List<TreeItem> items = getSelectedItems();
        mainPanel.cutToClipboard(getDependenciesSet(items));
        mainPanel.repaintTree();
    }

    public void pasteBeforeActionPerformed(ActionEvent evt) {
        TreeItem item = getCurrentItem();

        if (mainPanel.getClipboardType() == ClipboardType.FRAME) {
            Frame frame = (Frame) item;
            Timelined timelined = frame.timeline.timelined;
            int position = frame.frame + 1; //Frame.frame is zero based
            copyOrMoveFrames(mainPanel.getClipboardContents(), mainPanel.isClipboardCut(), timelined, position);
        } else {
            Timelined timelined;
            Tag position;
            int positionInt;
            if (item instanceof Frame) {
                Frame frame = (Frame) item;
                timelined = frame.timeline.timelined;
                positionInt = calcFramePositionToAdd(frame, timelined, true, new Reference<>(false), false);
            } else {
                timelined = ((Tag) item).getTimelined();
                positionInt = timelined.indexOfTag((Tag) item);
            }
            ReadOnlyTagList tags = timelined.getTags();
            position = positionInt < tags.size() ? tags.get(positionInt) : null;
            copyOrMoveTags(mainPanel.getClipboardContents(), mainPanel.isClipboardCut(), timelined, position);
        }
        if (mainPanel.isClipboardCut()) {
            mainPanel.emptyClipboard();
        }
    }

    public void pasteAfterActionPerformed(ActionEvent evt) {
        TreeItem item = getCurrentItem();
        if (mainPanel.getClipboardType() == ClipboardType.FRAME) {
            Frame frame = (Frame) item;
            Timelined timelined = frame.timeline.timelined;
            int position = frame.frame + 2; //Frame.frame is zero based
            copyOrMoveFrames(mainPanel.getClipboardContents(), mainPanel.isClipboardCut(), timelined, position);
        } else {
            Timelined timelined;
            Tag position;
            int positionInt;
            if (item instanceof Frame) {
                Frame frame = (Frame) item;
                timelined = frame.timeline.timelined;
                positionInt = calcFramePositionToAdd(frame, timelined, false, new Reference<>(false), false);
            } else {
                timelined = ((Tag) item).getTimelined();
                positionInt = timelined.indexOfTag((Tag) item) + 1;
            }
            ReadOnlyTagList tags = timelined.getTags();
            position = positionInt < tags.size() ? tags.get(positionInt) : null;
            copyOrMoveTags(mainPanel.getClipboardContents(), mainPanel.isClipboardCut(), timelined, position);
        }
        if (mainPanel.isClipboardCut()) {
            mainPanel.emptyClipboard();
        }
    }

    private void pasteInsideActionPerformed(ActionEvent evt) {
        TreeItem item = getCurrentItem();

        if (mainPanel.getClipboardType() == ClipboardType.FRAME) {
            Timelined timelined = (Timelined) item;
            int position = timelined.getFrameCount() + 1;
            copyOrMoveFrames(mainPanel.getClipboardContents(), mainPanel.isClipboardCut(), timelined, position);
        } else {
            Timelined timelined;
            Tag position;
            if (item instanceof Frame) {
                Frame frame = (Frame) item;
                position = frame.allInnerTags.get(frame.allInnerTags.size() - 1);
                timelined = frame.timeline.timelined;
            } else {
                timelined = (Timelined) item;
                position = null;
            }
            copyOrMoveTags(mainPanel.getClipboardContents(), mainPanel.isClipboardCut(), timelined, position);
        }

        if (mainPanel.isClipboardCut()) {
            mainPanel.emptyClipboard();
        }
    }

    private void collectDepthAsSprites(ActionEvent evt) {
        List<Frame> frames = new ArrayList<>();
        Set<Integer> originalDepths = new TreeSet<>();

        for (TreeItem item : getSelectedItems()) {
            Frame f = (Frame) item;
            frames.add(f);
            for (Tag t : f.innerTags) {
                if (t instanceof DepthTag) {
                    originalDepths.add(((DepthTag) t).getDepth());
                }
            }
        }

        CollectDepthAsSpritesDialog dialog = new CollectDepthAsSpritesDialog(Main.getDefaultDialogsOwner());

        if (dialog.showDialog(originalDepths) != CollectDepthAsSpritesDialog.OK_OPTION) {
            return;
        }

        Frame first = frames.get(0);
        Frame afterLast = null;

        Timelined timelined = first.timeline.timelined;
        SWF swf = timelined instanceof SWF ? (SWF) timelined : ((DefineSpriteTag) timelined).getSwf();

        List<Integer> depths = dialog.getDepths();
        boolean replace = dialog.getReplace();
        boolean offset = dialog.getOffset();
        boolean firstMatrix = dialog.getEnsureFirstMatrix();

        // Keep track of next frame if replacing to fix matrices
        if (replace) {
            Frame last = frames.get(frames.size() - 1);
            afterLast = last.timeline.getFrame(last.frame + 1);
        }

        // Sprites creation
        Map<Integer, DefineSpriteTag> sprites = new HashMap<>();
        int idOffset = 0;
        for (int d : depths) {
            DefineSpriteTag sprite = new DefineSpriteTag(swf);
            sprite.spriteId += idOffset;
            ++idOffset;
            sprite.frameCount = frames.size();

            for (int i = 0; i < frames.size(); i++) {
                Tag showFrame = new ShowFrameTag(swf);
                showFrame.setTimelined(sprite);
                sprite.addTag(showFrame);
            }

            if (replace) {
                timelined.addTag(timelined.indexOfTag(first.innerTags.get(0)), sprite);
            } else {
                timelined.addTag(sprite);
            }
            sprite.setTimelined(timelined);
            sprite.getTimeline();
            sprites.put(d, sprite);
        }

        try {
            // Tags collection
            for (int i = frames.size() - 1; i > -1; i--) {
                Frame f = frames.get(i);
                Map<Integer, MATRIX> prevMatrixAtDepth = new HashMap<>();
                int pf = f.frame - 1;

                for (int j = 0; j < f.innerTags.size(); j++) {
                    Tag t = f.innerTags.get(j);

                    // Only handle Place/Remove Object tags
                    if (!(t instanceof DepthTag)) {
                        continue;
                    }

                    int depth = ((DepthTag) t).getDepth();

                    if (!sprites.containsKey(depth)) {
                        continue;
                    }

                    DefineSpriteTag sprite = sprites.get(depth);
                    Tag clone = t.cloneTag();
                    clone.setModified(true);

                    // Get previous matrix
                    if (firstMatrix && i == 0 && clone instanceof PlaceObjectTypeTag) {
                        PlaceObjectTypeTag place = (PlaceObjectTypeTag) clone;
                        if (place.getMatrix() == null) {
                            pf = getPreviousMatrix(pf, prevMatrixAtDepth, place, f.timeline);
                        }
                    }

                    clone.setTimelined(sprite);
                    sprite.addTag(i, clone);

                    if (replace) {
                        f.innerTags.remove(t);
                        timelined.removeTag(t);
                        //swf.removeTag(t);
                        j--;
                    }
                }
            }

            for (Entry<Integer, DefineSpriteTag> entry : sprites.entrySet()) {
                DefineSpriteTag sprite = entry.getValue();

                if (offset || replace) {
                    int minX = Integer.MAX_VALUE;
                    int minY = Integer.MAX_VALUE;

                    for (Tag t : sprite.getTags()) {
                        if (!(t instanceof PlaceObjectTypeTag)) {
                            continue;
                        }

                        PlaceObjectTypeTag placeTag = (PlaceObjectTypeTag) t;
                        MATRIX m = placeTag.getMatrix();

                        if (m != null) {
                            if (m.translateX < minX) {
                                minX = m.translateX;
                            }

                            if (m.translateY < minY) {
                                minY = m.translateY;
                            }
                        }
                    }

                    if (offset) {
                        offsetSprite(sprite, -minX, -minY);
                    }

                    if (replace) {
                        replaceSprite(sprite, timelined, swf, entry.getKey(), first, afterLast, offset, minX, minY);
                    }
                }

                sprite.setSwf(swf, true);
                sprite.resetTimeline();
            }

            swf.updateCharacters();
            if (replace) {
                timelined.resetTimeline();
                swf.computeDependentCharacters();
                swf.computeDependentFrames();
            }
            swf.resetTimelines(swf);
            mainPanel.refreshTree(swf);
        } catch (InterruptedException | IOException ex) {
            Logger.getLogger(TagTreeContextMenu.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private int getPreviousMatrix(int frameIndex, Map<Integer, MATRIX> prevMatrixAtDepth, PlaceObjectTypeTag target, Timeline timeline) {
        int depth = target.getDepth();

        for (; frameIndex > -1; frameIndex--) {
            if (prevMatrixAtDepth.containsKey(depth)) {
                break;
            }

            Frame prev = timeline.getFrame(frameIndex);
            for (Tag pt : prev.innerTags) {
                if (!(pt instanceof PlaceObjectTypeTag)) {
                    continue;
                }

                PlaceObjectTypeTag pplace = (PlaceObjectTypeTag) pt;
                MATRIX m = pplace.getMatrix();

                if (m != null) {
                    prevMatrixAtDepth.putIfAbsent(pplace.getDepth(), m);
                }
            }
        }

        MATRIX prevMatrix = prevMatrixAtDepth.get(depth);
        if (prevMatrix != null) {
            target.setPlaceFlagHasMatrix(true);
            target.setPlaceFlagMove(true);
            target.setMatrix(new MATRIX(prevMatrix));
        }

        return frameIndex;
    }

    private void offsetSprite(DefineSpriteTag sprite, int dx, int dy) {
        for (Tag t : sprite.getTags()) {
            if (!(t instanceof PlaceObjectTypeTag)) {
                continue;
            }

            PlaceObjectTypeTag placeTag = (PlaceObjectTypeTag) t;
            MATRIX m = placeTag.getMatrix();

            if (m != null) {
                m.translateX += dx;
                m.translateY += dy;
            }
        }
    }

    private void replaceSprite(DefineSpriteTag sprite, Timelined timelined, SWF swf, int depth, Frame first, Frame afterLast, boolean offset, int dx, int dy) {
        // Add new tag for the new sprite
        PlaceObject2Tag place = new PlaceObject2Tag(swf);
        place.placeFlagHasCharacter = true;
        place.characterId = sprite.getCharacterId();
        place.depth = depth;
        if (offset) {
            place.placeFlagHasMatrix = true;
            place.placeFlagMove = true;
            place.matrix = new MATRIX();
            place.matrix.translateX = dx;
            place.matrix.translateY = dy;
        }

        place.setTimelined(timelined);
        first.innerTags.add(place);
        timelined.addTag(timelined.indexOfTag(first.innerTags.get(0)), place);

        // Correct next frame matrix
        if (afterLast == null) {
            return;
        }

        MATRIX lastMatrix = null;
        ReadOnlyTagList sTags = sprite.getTags();

        // Get last matrix of the sprite
        for (int i = sTags.size() - 1; i > -1; i--) {
            Tag t = sTags.get(i);

            if (!(t instanceof PlaceObjectTypeTag)) {
                continue;
            }

            PlaceObjectTypeTag tplace = (PlaceObjectTypeTag) t;
            lastMatrix = tplace.getMatrix();

            if (lastMatrix != null) {
                break;
            }
        }

        // Find next matrix-less place tag
        List<Tag> tags = afterLast.innerTags;
        for (int i = tags.size() - 1; i > -1; i--) {
            Tag t = tags.get(i);

            if (!(t instanceof PlaceObjectTypeTag)) {
                continue;
            }

            PlaceObjectTypeTag tplace = (PlaceObjectTypeTag) t;
            if (tplace.getDepth() == place.depth && tplace.getMatrix() == null) {
                tplace.setMatrix(new MATRIX(lastMatrix));
                tplace.setPlaceFlagHasMatrix(true);
                tplace.setPlaceFlagMove(true);
            }
        }
    }

    /**
     * @param items
     * @param move
     * @param targetTimelined
     * @param position 1-based position
     */
    public void copyOrMoveFrames(Set<TreeItem> items, boolean move, Timelined targetTimelined, int position) {
        if (items.isEmpty()) {
            return;
        }

        Timelined sourceTimelined = ((Frame) items.iterator().next()).timeline.timelined;

        final boolean USE_REMOVE_TAG = false;

        int positionMinus = 0;

        int posPrevFrame = position - 1;

        int posTargetFrame = position;
        if (move && sourceTimelined == targetTimelined) {
            Set<Integer> removedFrames = new LinkedHashSet<>();
            for (TreeItem item : items) {
                Frame frame = (Frame) item;
                int removedFrameNum = frame.frame + 1;
                removedFrames.add(removedFrameNum);
                if (removedFrameNum < position) {
                    positionMinus++;
                }
            }
            while (removedFrames.contains(posPrevFrame)) {
                posPrevFrame--;
            }
            while (removedFrames.contains(posTargetFrame)) {
                posTargetFrame++;
            }
        }

        Frame targetFrame = targetTimelined.getTimeline().getFrame(posTargetFrame - 1);

        Frame prevTargetFrame;
        if (posPrevFrame == 0) {
            prevTargetFrame = null;
        } else {
            prevTargetFrame = targetTimelined.getTimeline().getFrame(posPrevFrame - 1);
        }

        int positionAfterDelete = position - positionMinus;

        if (move) {
            removeFrames(items);
        }
        boolean endsWithShowframe = false;
        ShowFrameTag prevShowFrame = null;
        int f = 1;
        for (int i = 0; i < targetTimelined.getTags().size(); i++) {
            Tag t = targetTimelined.getTags().get(i);
            endsWithShowframe = false;
            if (t instanceof ShowFrameTag) {
                f++;
                endsWithShowframe = true;
                if (f == positionAfterDelete) {
                    prevShowFrame = (ShowFrameTag) t;
                }
            }
        }

        SWF swf = targetTimelined.getSwf();

        List<Tag> newFrameTags = new ArrayList<>();

        int prevShowFrameIndex;
        if (targetFrame == null) {
            if (!endsWithShowframe) {
                ShowFrameTag showFrame = new ShowFrameTag(swf);
                newFrameTags.add(showFrame);
            }
            prevShowFrameIndex = targetTimelined.getTags().size() - 1;
        } else {
            prevShowFrameIndex = targetTimelined.getTags().indexOf(prevShowFrame);
        }

        Frame frame = null;
        for (TreeItem item : items) {
            frame = (Frame) item;
            Set<Integer> allDepths = new TreeSet<>(frame.layers.keySet());
            if (prevTargetFrame != null) {
                allDepths.addAll(prevTargetFrame.layers.keySet());
            }
            for (int depth : allDepths) {
                DepthState sourceState = frame.layers.get(depth);
                DepthState prevTargetState = prevTargetFrame == null ? null : prevTargetFrame.layers.get(depth);

                if (prevTargetState != null && sourceState != null && prevTargetState.equals(sourceState)) {
                    //empty
                } else if (sourceState != null) {
                    PlaceObjectTypeTag place = sourceState.toPlaceObjectTag(depth);
                    if (prevTargetState != null && !prevTargetState.equals(sourceState)) {
                        if (USE_REMOVE_TAG) {
                            RemoveObject2Tag rem = new RemoveObject2Tag(swf);
                            rem.depth = depth;
                            newFrameTags.add(rem);
                        } else {
                            place.setPlaceFlagMove(true);
                        }
                    }
                    newFrameTags.add(place);
                } else if (prevTargetState != null && sourceState == null) {
                    RemoveObject2Tag rem = new RemoveObject2Tag(swf);
                    rem.depth = depth;
                    newFrameTags.add(rem);
                }
            }

            for (DoActionTag doa : frame.actions) {
                if (move) {
                    newFrameTags.add(doa);
                } else {
                    try {
                        newFrameTags.add(doa.cloneTag());
                    } catch (InterruptedException | IOException ex) {
                        Logger.getLogger(TagTreeContextMenu.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

            ShowFrameTag showFrame = new ShowFrameTag(swf);
            newFrameTags.add(showFrame);
            prevTargetFrame = frame;
        }

        if (targetFrame != null) {
            Set<Integer> allDepths = new TreeSet<>(frame.layers.keySet());
            allDepths.addAll(targetFrame.layers.keySet());
            for (int depth : allDepths) {
                DepthState sourceState = frame.layers.get(depth);
                DepthState targetState = targetFrame.layers.get(depth);
                if (sourceState == null && targetState == null) {
                    continue;
                }
                if (sourceState != null && targetState != null
                        && sourceState.equals(targetState)
                        && targetState.placeFrame.frame == targetFrame.frame) {
                    targetTimelined.removeTag(targetState.placeObjectTag);
                } else if (targetState == null) {
                    RemoveObject2Tag rem = new RemoveObject2Tag(swf);
                    rem.depth = depth;
                    newFrameTags.add(rem);
                } else if (targetState.placeFrame.frame < targetFrame.frame
                        && sourceState != null
                        && !sourceState.equals(targetState)) {
                    PlaceObjectTypeTag fullPlace = targetState.toPlaceObjectTag(depth);
                    if (USE_REMOVE_TAG) {
                        RemoveObject2Tag rem = new RemoveObject2Tag(swf);
                        rem.depth = depth;
                        newFrameTags.add(rem);
                    } else {
                        fullPlace.setPlaceFlagMove(true);
                    }
                    newFrameTags.add(fullPlace);
                } else if (targetState.placeFrame.frame == targetFrame.frame) {
                    if (targetState.placeObjectTag.flagMove()) {
                        PlaceObjectTypeTag fullPlace = targetState.toPlaceObjectTag(depth);
                        fullPlace.setTimelined(targetTimelined);
                        fullPlace.setPlaceFlagMove(true);

                        targetTimelined.replaceTag(targetState.placeObjectTag, fullPlace);
                    } else {
                        targetState.placeObjectTag.setPlaceFlagMove(true);
                    }
                }
            }
        }

        for (int i = prevShowFrameIndex + 1; i < targetTimelined.getTags().size(); i++) {
            Tag t = targetTimelined.getTags().get(i);
            if (t instanceof RemoveTag) {
                targetTimelined.removeTag(i);
                i--;
            }
            if (t instanceof ShowFrameTag) {
                break;
            }
        }

        int pos = prevShowFrameIndex + 1;
        for (Tag t : newFrameTags) {
            t.setTimelined(targetTimelined);
            targetTimelined.addTag(pos, t);
            pos++;
        }

        fixDefineBeforeUsage(swf);
        fixHeaderTags(swf);
        swf.resetTimelines(targetTimelined);
        swf.clearImageCache();
        targetTimelined.setFrameCount(targetTimelined.getTimeline().getFrameCount());
        swf.resetTimelines(sourceTimelined);
        sourceTimelined.setFrameCount(sourceTimelined.getTimeline().getFrameCount());

        mainPanel.refreshTree(swf);
    }

    private int moveCharacter(SWF swf, int usageIndex, int characterId) {
        int i = 0;
        for (int j = 0; j < swf.getTags().size(); j++) {
            Tag t2 = swf.getTags().get(j);
            if ((t2 instanceof CharacterIdTag)
                    && !(t2 instanceof PlaceObjectTypeTag)
                    && !(t2 instanceof RemoveTag)
                    && !(t2 instanceof VideoFrameTag)) {
                CharacterIdTag chit = (CharacterIdTag) t2;
                if (chit.getCharacterId() == characterId) {
                    swf.removeTag(j);
                    swf.addTag(usageIndex + i, (Tag) chit);
                    i++;
                }
            }
        }
        return i;
    }

    /**
     * Move tags usually placed in the first frame to the first frame
     *
     * @param swf
     */
    private void fixHeaderTags(SWF swf) {
        List<Tag> headerTags = new ArrayList<>();
        for (int i = 0; i < swf.getTags().size(); i++) {
            Tag t = swf.getTags().get(i);
            if ((t instanceof FileAttributesTag)
                    || (t instanceof SetBackgroundColorTag)
                    || (t instanceof ExporterInfo)
                    || (t instanceof MetadataTag)) {
                swf.removeTag(i);
                headerTags.add(t);
                i--;
            }
        }
        for (int i = 0; i < headerTags.size(); i++) {
            swf.addTag(i, headerTags.get(i));
        }
    }

    private boolean fixDefineBeforeUsage(SWF swf) {
        ReadOnlyTagList tags = swf.getTags();
        Set<Integer> walkedSprites = new HashSet<>();
        boolean changed = false;
        for (int i = 0; i < tags.size(); i++) {
            Tag t = tags.get(i);
            if (t instanceof PlaceObjectTypeTag) {
                PlaceObjectTypeTag place = (PlaceObjectTypeTag) t;
                if (place.getCharacterId() != -1) {
                    int chId = place.getCharacterId();
                    CharacterTag ch = swf.getCharacter(chId);
                    if (ch != null) {
                        int defineIndex = tags.indexOf(ch);
                        int usageIndex = i;
                        if (usageIndex < defineIndex) {
                            i += moveCharacter(swf, i, chId);
                            changed = true;
                        }
                    }
                    if (ch instanceof DefineSpriteTag) {
                        DefineSpriteTag sprite = (DefineSpriteTag) ch;
                        if (walkedSprites.contains(sprite.getCharacterId())) {
                            continue;
                        }
                        walkedSprites.add(sprite.getCharacterId());
                        for (int j = 0; j < sprite.getTags().size(); j++) {
                            Tag st = sprite.getTags().get(j);
                            if (st instanceof PlaceObjectTypeTag) {
                                place = (PlaceObjectTypeTag) st;
                                if (place.getCharacterId() != -1) {
                                    chId = place.getCharacterId();
                                    ch = swf.getCharacter(chId);
                                    if (ch != null) {
                                        int defineIndex = tags.indexOf(ch);
                                        int usageIndex = i;
                                        if (usageIndex < defineIndex) {
                                            i += moveCharacter(swf, swf.getTags().indexOf(sprite), chId);
                                            changed = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return changed;
    }

    private void removeFrames(Set<TreeItem> items) {
        if (items.isEmpty()) {
            return;
        }
        final boolean USE_REMOVE_TAG = false;

        Timelined sourceTimelined = ((Frame) items.iterator().next()).timeline.timelined;
        Timeline sourceTimeline = sourceTimelined.getTimeline();
        SWF swf = sourceTimelined.getSwf();

        List<ShowFrameTag> frameTags = new ArrayList<>();
        Set<Integer> framesToDelete = new TreeSet<>();
        boolean deleteLastFrame = false;
        for (TreeItem item : items) {
            Frame frameItem = (Frame) item;
            framesToDelete.add(frameItem.frame + 1);
            ShowFrameTag sft = frameItem.showFrameTag;
            if (sft != null) { //can be null for last frame
                frameTags.add(sft);
            } else {
                deleteLastFrame = true;
            }
        }

        int f = 1;
        int lastNotDeletedFrameNum = 0;
        int numtagsInFrame = 0;
        for (int i = 0; i < sourceTimelined.getTags().size(); i++) {
            Tag t = sourceTimelined.getTags().get(i);
            numtagsInFrame++;
            if (t instanceof ShowFrameTag) {
                numtagsInFrame = 0;
                if (frameTags.contains((ShowFrameTag) t)) {
                    sourceTimelined.removeTag(t);
                    for (i = i - 1; i >= 0; i--) {
                        Tag t2 = sourceTimelined.getTags().get(i);
                        if (t2 instanceof ShowFrameTag) {
                            break;
                        }
                        if (isFrameTagToDelete(t2)) {
                            sourceTimelined.removeTag(i);
                        }
                    }
                } else {
                    lastNotDeletedFrameNum = f;
                }
                if (framesToDelete.contains(f)
                        && !framesToDelete.contains(f + 1)
                        && i + 1 + 1 < sourceTimelined.getTags().size()) {
                    Frame lastNotDeletedFrame = lastNotDeletedFrameNum == 0 ? null : sourceTimeline.getFrame(lastNotDeletedFrameNum - 1);
                    Frame nextFrame = sourceTimeline.getFrame(f + 1 - 1);
                    if (nextFrame != null) {
                        Set<Integer> allDepths = new TreeSet<>(nextFrame.layers.keySet());
                        if (lastNotDeletedFrame != null) {
                            allDepths.addAll(lastNotDeletedFrame.layers.keySet());
                        }
                        for (int j = i + 1 + 1; j < sourceTimelined.getTags().size(); j++) {
                            Tag t2 = sourceTimelined.getTags().get(j);
                            if (t2 instanceof ShowFrameTag) {
                                break;
                            }
                            if (t2 instanceof RemoveTag) {
                                sourceTimelined.removeTag(j);
                                j--;
                            }
                            i = j;
                        }
                        for (int depth : allDepths) {
                            DepthState lastDepthstate = lastNotDeletedFrame == null ? null : lastNotDeletedFrame.layers.get(depth);
                            DepthState nextDepthstate = nextFrame.layers.get(depth);
                            if (nextDepthstate != null
                                    && lastDepthstate == null
                                    && nextDepthstate.placeObjectTag.flagMove()) {
                                nextDepthstate.placeObjectTag.setPlaceFlagMove(false);
                                nextDepthstate.placeObjectTag.setModified(true);
                            } else if (nextDepthstate != null
                                    && nextDepthstate.placeFrame.frame < nextDepthstate.frame.frame
                                    && nextDepthstate.placeFrame.frame > lastNotDeletedFrameNum - 1) {
                                //the place was deleted                                                      
                                PlaceObjectTypeTag placeFull = nextDepthstate.toPlaceObjectTag(depth);
                                if (lastDepthstate != null) {
                                    if (USE_REMOVE_TAG) {
                                        RemoveObject2Tag rem = new RemoveObject2Tag(swf);
                                        rem.depth = depth;
                                        i++;
                                        sourceTimelined.addTag(i, rem);
                                        rem.setTimelined(sourceTimelined);
                                    } else {
                                        placeFull.setPlaceFlagMove(true);
                                    }
                                }
                                i++;
                                sourceTimelined.addTag(i, placeFull);
                                placeFull.setTimelined(sourceTimelined);
                            } else if (lastDepthstate != null
                                    && nextDepthstate != null
                                    && !lastDepthstate.equals(nextDepthstate)) {
                                if (nextDepthstate.placeFrame.frame < nextDepthstate.frame.frame) {
                                    PlaceObjectTypeTag placeFull = nextDepthstate.toPlaceObjectTag(depth);
                                    if (USE_REMOVE_TAG) {
                                        RemoveObject2Tag rem = new RemoveObject2Tag(swf);
                                        rem.depth = depth;
                                        i++;
                                        sourceTimelined.addTag(i, rem);
                                    } else {
                                        placeFull.setPlaceFlagMove(true);
                                    }
                                    i++;
                                    sourceTimelined.addTag(i, placeFull);
                                    placeFull.setTimelined(sourceTimelined);
                                }
                            } else if (lastDepthstate != null
                                    && nextDepthstate == null) {
                                RemoveObject2Tag rem = new RemoveObject2Tag(swf);
                                rem.depth = depth;
                                i++;
                                sourceTimelined.addTag(i, rem);
                                rem.setTimelined(sourceTimelined);
                            }
                        }
                    }
                }
                f++;
            }
        }
        if (numtagsInFrame > 0 && deleteLastFrame) {
            for (int i = sourceTimelined.getTags().size(); i >= 0; i--) {
                Tag t2 = sourceTimelined.getTags().get(i);
                if (t2 instanceof ShowFrameTag) {
                    break;
                }
                if (isFrameTagToDelete(t2)) {
                    sourceTimelined.removeTag(i);
                }
            }
        }
        //WARNING: this method does not reset timeline and does not set frame count. Caller must do it.
    }

    /**
     * Which of tags in a frame are deleted when deleting a frame
     *
     * @param tag
     * @return
     */
    private boolean isFrameTagToDelete(Tag tag) {
        return (tag instanceof PlaceObjectTypeTag)
                || (tag instanceof RemoveTag)
                || (tag instanceof FrameLabelTag)
                || (tag instanceof StartSoundTag)
                || (tag instanceof StartSound2Tag)
                || (tag instanceof SoundStreamBlockTag)
                || (tag instanceof DoActionTag);
    }

    public void copyOrMoveTags(Set<TreeItem> items, boolean move, Timelined targetTimelined, Tag position) {
        Set<SWF> sourceSwfs = new LinkedHashSet<>();
        SWF targetSwf = (targetTimelined instanceof SWF) ? (SWF) targetTimelined : ((DefineSpriteTag) targetTimelined).getSwf();
        try {
            List<Tag> newTags = new ArrayList<>();
            Map<Integer, Integer> changedCharacterIds = new HashMap<>();
            for (TreeItem item : items) {
                Tag tag = (Tag) item;
                if (tag.getSwf() == null) {
                    continue;
                }
                SWF sourceSwf = tag.getSwf();
                sourceSwfs.add(sourceSwf);

                Timelined realTargetTimelined = targetTimelined;
                Tag realPosition = position;

                if (!Configuration.allowPlacingDefinesIntoSprites.get()) {
                    //do not place Define tags in DefineSprites
                    if ((tag instanceof CharacterTag) && (targetTimelined instanceof DefineSpriteTag)) {
                        Tag spriteTag = ((DefineSpriteTag) targetTimelined);
                        //get to the topmost level DefineSprite
                        while (spriteTag.getTimelined() instanceof DefineSpriteTag) {
                            spriteTag = (DefineSpriteTag) spriteTag.getTimelined();
                        }
                        realTargetTimelined = spriteTag.getTimelined(); //should be SWF
                        realPosition = spriteTag;
                    }
                }

                //do not move to self
                if (items.size() == 1 && move) {
                    if (realTargetTimelined == tag.getTimelined()) {
                        if (tag == position) {
                            return;
                        }
                        int index = tag.getTimelined().indexOfTag(tag);
                        ReadOnlyTagList tags = tag.getTimelined().getTags();
                        Tag nextPosition;
                        if (index + 1 < tags.size()) {
                            nextPosition = tags.get(index + 1);
                            if (nextPosition == realPosition) {
                                return;
                            }
                        }
                    }
                }

                if (move) {
                    tag.getTimelined().removeTag(tag);
                }

                if (sourceSwf != targetSwf || !move) {
                    ReadOnlyTagList tags = realTargetTimelined.getTags();
                    int positionInt = realPosition == null ? tags.size() : tags.indexOf(realPosition);

                    Tag copyTag = tag.cloneTag();
                    copyTag.setSwf(targetSwf, true);
                    copyTag.setTimelined(realTargetTimelined);
                    if (tag instanceof CharacterTag) {
                        CharacterTag characterTag = (CharacterTag) copyTag;
                        int oldCharacterId = characterTag.getCharacterId();
                        int newCharacterId = checkUniqueCharacterId(copyTag);

                        changedCharacterIds.put(oldCharacterId, newCharacterId);
                    }
                    realTargetTimelined.addTag(positionInt, copyTag);

                    targetSwf.updateCharacters();
                    targetSwf.getCharacters(true); // force rebuild character id cache
                    copyTag.setModified(true);
                    newTags.add(copyTag);
                    if (move) {
                        mainPanel.replaceItemPin(tag, copyTag);
                    }
                } else if (sourceSwf == targetSwf && move) { //mainPanel.isClipboardCut()
                    ReadOnlyTagList tags = realTargetTimelined.getTags();
                    int positionInt = realPosition == null ? tags.size() : tags.indexOf(realPosition);
                    realTargetTimelined.addTag(positionInt, tag);
                    tag.setTimelined(realTargetTimelined);
                }
            }
            for (int oldCharacterId : changedCharacterIds.keySet()) {
                int newCharacterId = changedCharacterIds.get(oldCharacterId);
                for (Tag newTag : newTags) {
                    // todo: avoid double replaces
                    newTag.replaceCharacter(oldCharacterId, newCharacterId);
                    if ((newTag instanceof CharacterIdTag) && !(newTag instanceof CharacterTag)) {
                        CharacterIdTag characterIdTag = (CharacterIdTag) newTag;
                        if (characterIdTag.getCharacterId() == oldCharacterId) {
                            characterIdTag.setCharacterId(newCharacterId);
                        }
                    }
                }
            }

            for (SWF sourceSwf : sourceSwfs) {
                if (sourceSwf != targetSwf) {
                    sourceSwf.assignExportNamesToSymbols();
                    sourceSwf.assignClassesToSymbols();
                    sourceSwf.clearImageCache();
                    sourceSwf.clearShapeCache();
                    sourceSwf.updateCharacters();
                    sourceSwf.computeDependentCharacters();
                    sourceSwf.computeDependentFrames();
                    sourceSwf.resetTimelines(sourceSwf);
                }
            }
            targetSwf.assignExportNamesToSymbols();
            targetSwf.assignClassesToSymbols();
            targetSwf.clearImageCache();
            targetSwf.clearShapeCache();
            targetSwf.updateCharacters();
            targetSwf.computeDependentCharacters();
            targetSwf.computeDependentFrames();
            targetSwf.resetTimelines(targetSwf);

            mainPanel.refreshTree(targetSwf);
        } catch (InterruptedException | IOException ex) {
            Logger.getLogger(TagTreeContextMenu.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void removeAloneCharacters(Set<Integer> aloneCharacterIds, SWF swf) {
        for (int ch : aloneCharacterIds) {
            CharacterTag ct = swf.getCharacter(ch);
            if (ct == null) {
                continue;
            }
            if (!ct.getClassNames().isEmpty()) {
                continue;
            }
            if (ct.getExportName() != null) {
                continue;
            }
            Set<Integer> dependentCharacters = swf.getDependentCharacters(ch);
            if (dependentCharacters.isEmpty()) {
                Set<Integer> needed = new LinkedHashSet<>();
                ct.getNeededCharacters(needed, swf);
                List<CharacterIdTag> attachedTags = swf.getCharacterIdTags(ch);
                for (CharacterIdTag cit : attachedTags) {
                    if (cit instanceof Tag) {
                        Tag citt = (Tag) cit;
                        citt.getTimelined().removeTag(citt);
                    }
                }
                ct.getTimelined().removeTag(ct);
                swf.computeDependentCharacters();
                removeAloneCharacters(needed, swf);
            }
        }
    }

    public void exportFlaActionPerformed(ActionEvent evt) {
        SWF swf = (SWF) getCurrentItem().getOpenable();
        mainPanel.exportFla(swf);
    }

    public void exportFlashDevelopActionPerformed(ActionEvent evt) {
        SWF swf = (SWF) getCurrentItem().getOpenable();
        mainPanel.exportFlashDevelop(swf);
    }

    public void exportIdeaActionPerformed(ActionEvent evt) {
        SWF swf = (SWF) getCurrentItem().getOpenable();
        mainPanel.exportIdea(swf);
    }

    public void exportVsCodeActionPerformed(ActionEvent evt) {
        SWF swf = (SWF) getCurrentItem().getOpenable();
        mainPanel.exportVsCode(swf);
    }

    public void importScriptsActionPerformed(ActionEvent evt) {
        Openable openable = getCurrentItem().getOpenable();
        SWF swf = (openable instanceof SWF) ? (SWF) openable : ((ABC) openable).getSwf();
        mainPanel.importScript(swf);
    }

    public void importTextsActionPerformed(ActionEvent evt) {
        SWF swf = (SWF) getCurrentItem().getOpenable();
        mainPanel.importText(swf);
    }

    public void importImagesActionPerformed(ActionEvent evt) {
        SWF swf = (SWF) getCurrentItem().getOpenable();
        mainPanel.importImage(swf);
    }

    public void importShapesActionPerformed(ActionEvent evt) {
        SWF swf = (SWF) getCurrentItem().getOpenable();
        mainPanel.importShape(swf, false);
    }

    public void importShapesNoFillActionPerformed(ActionEvent evt) {
        SWF swf = (SWF) getCurrentItem().getOpenable();
        mainPanel.importShape(swf, true);
    }

    public void importMoviesActionPerformed(ActionEvent evt) {
        SWF swf = (SWF) getCurrentItem().getOpenable();
        mainPanel.importMovie(swf);
    }

    public void importSoundsActionPerformed(ActionEvent evt) {
        SWF swf = (SWF) getCurrentItem().getOpenable();
        mainPanel.importSound(swf);
    }

    public void importSymbolClassActionPerformed(ActionEvent evt) {
        SWF swf = (SWF) getCurrentItem().getOpenable();
        mainPanel.importSymbolClass(swf);
    }

    public void moveUpActionPerformed(ActionEvent evt) {
        moveUpDown(getCurrentItem(), true);
    }

    public void moveDownActionPerformed(ActionEvent evt) {
        moveUpDown(getCurrentItem(), false);
    }

    public void moveUpDown(TreeItem item, boolean up) {
        if ((item instanceof Openable) || (item instanceof OpenableList)) {
            mainPanel.moveSwfListUpDown(item, up);
            return;
        }
        if (!(item instanceof Tag)) {
            return;
        }
        if (getTree() != mainPanel.tagListTree) {
            return;
        }
        Set<TreeItem> itemsToMove = new HashSet<>();
        itemsToMove.add(item);
        Tag tag = (Tag) item;
        int index = tag.getTimelined().indexOfTag(tag);
        Tag position = null;
        Timelined timelined = null;
        ReadOnlyTagList tags = tag.getTimelined().getTags();
        if (up) {

            if (index == 0) {
                if (tag.getTimelined() instanceof SWF) {
                    return;
                }
                //move one level UP
                position = (DefineSpriteTag) tag.getTimelined();
            } else {
                index = tag.getTimelined().indexOfTag(tag);
                index--;

                position = tag.getTimelined().getTags().get(index);
            }
            timelined = position.getTimelined();
        } else {
            if (index == tags.size() - 1) {
                if (tag.getTimelined() instanceof SWF) {
                    return;
                }
                timelined = ((DefineSpriteTag) tag.getTimelined()).getTimelined();
                index = timelined.getTags().indexOf((DefineSpriteTag) tag.getTimelined());
                index++;
                if (index >= timelined.getTags().size()) {
                    position = null;
                } else {
                    position = timelined.getTags().get(index);
                }
            } else {
                timelined = tag.getTimelined();
                index = timelined.indexOfTag(tag);
                index += 2;
                if (index >= timelined.getTags().size()) {
                    position = null;
                } else {
                    position = timelined.getTags().get(index);
                }
            }
        }
        copyOrMoveTags(itemsToMove, true, timelined, position);

        TreePath path = getTree().getFullModel().getTreePath(item);
        getTree().setSelectionPath(path);
        getTree().scrollPathToVisible(path);
        mainPanel.repaintTree();
    }

    public void configurePathResolvingActionPerformed(ActionEvent evt) {
        SWF item = (SWF) getCurrentItem();
        PathResolvingDialog dialog = new PathResolvingDialog(item, Main.getDefaultDialogsOwner());
        dialog.showDialog();
    }

    public void changeCharsetActionPerformed(ActionEvent evt) {
        SWF item = (SWF) getCurrentItem();
        String newCharset = ((JMenuItem) evt.getSource()).getText();
        if (Objects.equals(item.getCharset(), newCharset)) {
            return;
        }

        SwfSpecificCustomConfiguration conf = Configuration.getOrCreateSwfSpecificCustomConfiguration(item.getShortPathTitle());
        conf.setCustomData(CustomConfigurationKeys.KEY_CHARSET, newCharset);
        while (item.binaryData != null) {
            item = item.binaryData.getSwf();
        }
        Main.reloadFile(item.openableList);
    }

    private void exportABCActionPerformed(ActionEvent evt) {
        ABCContainerTag container = (ABCContainerTag) getCurrentItem();
        FileFilter abcFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                return (f.getName().toLowerCase(Locale.ENGLISH).endsWith(".abc")) || (f.isDirectory());
            }

            @Override
            public String getDescription() {
                return AppStrings.translate("filter.abc");
            }
        };
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new File(Configuration.lastExportDir.get()));
        fc.setFileFilter(abcFilter);
        fc.setAcceptAllFileFilterUsed(false);
        if (fc.showSaveDialog(Main.getDefaultMessagesComponent()) == JFileChooser.APPROVE_OPTION) {
            File file = Helper.fixDialogFile(fc.getSelectedFile());
            FileFilter selFilter = fc.getFileFilter();
            try {
                String fileName = file.getAbsolutePath();
                if (selFilter == abcFilter) {
                    if (!fileName.toLowerCase(Locale.ENGLISH).endsWith(".abc")) {
                        fileName += ".abc";
                    }
                }

                try (FileOutputStream fos = new FileOutputStream(fileName)) {
                    container.getABC().saveTo(fos);
                }

                Configuration.lastExportDir.set(file.getParentFile().getAbsolutePath());

            } catch (Exception | OutOfMemoryError | StackOverflowError ex) {
                Main.handleSaveError(ex);
            }
        }
    }

    private void handleCreateFromFile(Tag tag, TreeNodeType createNodeType) {
        if (createNodeType == null) {
            return;
        }
        boolean remove;
        switch (createNodeType) {
            case SPRITE:
                remove = !mainPanel.replaceSpriteWithGif(tag);
                break;
            case SHAPE:
                remove = !mainPanel.replaceNoFill(tag);
                break;
            case MORPH_SHAPE:
                remove = !mainPanel.replaceMorphShape((MorphShapeTag) tag, true, false);
                break;
            case FONT:
                remove = !mainPanel.fontEmbed(tag, true);
                break;
            default:
                List<TreeItem> sel = new ArrayList<>();
                sel.add(tag);
                remove = !mainPanel.replace(sel, true);
                break;
        }
        if (remove) {
            tag.getTimelined().removeTag(tag);
            mainPanel.refreshTree();
        }
    }
}
