/*
 *  Copyright (C) 2022-2025 JPEXS
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

import com.jpexs.decompiler.flash.SWC;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.ZippedBundle;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitClass;
import com.jpexs.decompiler.flash.abc.types.traits.TraitFunction;
import com.jpexs.decompiler.flash.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.gui.MainPanel;
import com.jpexs.decompiler.flash.gui.TreeNodeType;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.gui.abc.ClassesListTreeModel;
import com.jpexs.decompiler.flash.gui.soleditor.Cookie;
import com.jpexs.decompiler.flash.iggy.conversion.IggySwfBundle;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.flash.tags.CSMTextSettingsTag;
import com.jpexs.decompiler.flash.tags.DebugIDTag;
import com.jpexs.decompiler.flash.tags.DefineBinaryDataTag;
import com.jpexs.decompiler.flash.tags.DefineButton2Tag;
import com.jpexs.decompiler.flash.tags.DefineButtonCxformTag;
import com.jpexs.decompiler.flash.tags.DefineButtonSoundTag;
import com.jpexs.decompiler.flash.tags.DefineButtonTag;
import com.jpexs.decompiler.flash.tags.DefineFont2Tag;
import com.jpexs.decompiler.flash.tags.DefineFont3Tag;
import com.jpexs.decompiler.flash.tags.DefineFont4Tag;
import com.jpexs.decompiler.flash.tags.DefineFontAlignZonesTag;
import com.jpexs.decompiler.flash.tags.DefineFontInfo2Tag;
import com.jpexs.decompiler.flash.tags.DefineFontInfoTag;
import com.jpexs.decompiler.flash.tags.DefineFontNameTag;
import com.jpexs.decompiler.flash.tags.DefineFontTag;
import com.jpexs.decompiler.flash.tags.DefineScalingGridTag;
import com.jpexs.decompiler.flash.tags.DefineSceneAndFrameLabelDataTag;
import com.jpexs.decompiler.flash.tags.DefineSoundTag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.DefineVideoStreamTag;
import com.jpexs.decompiler.flash.tags.DoActionTag;
import com.jpexs.decompiler.flash.tags.DoInitActionTag;
import com.jpexs.decompiler.flash.tags.EnableDebugger2Tag;
import com.jpexs.decompiler.flash.tags.EnableDebuggerTag;
import com.jpexs.decompiler.flash.tags.EnableTelemetryTag;
import com.jpexs.decompiler.flash.tags.EndTag;
import com.jpexs.decompiler.flash.tags.ExportAssetsTag;
import com.jpexs.decompiler.flash.tags.FileAttributesTag;
import com.jpexs.decompiler.flash.tags.FrameLabelTag;
import com.jpexs.decompiler.flash.tags.ImportAssets2Tag;
import com.jpexs.decompiler.flash.tags.ImportAssetsTag;
import com.jpexs.decompiler.flash.tags.JPEGTablesTag;
import com.jpexs.decompiler.flash.tags.MetadataTag;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.PlaceObject3Tag;
import com.jpexs.decompiler.flash.tags.PlaceObject4Tag;
import com.jpexs.decompiler.flash.tags.PlaceObjectTag;
import com.jpexs.decompiler.flash.tags.ProductInfoTag;
import com.jpexs.decompiler.flash.tags.ProtectTag;
import com.jpexs.decompiler.flash.tags.RemoveObject2Tag;
import com.jpexs.decompiler.flash.tags.RemoveObjectTag;
import com.jpexs.decompiler.flash.tags.ScriptLimitsTag;
import com.jpexs.decompiler.flash.tags.SetBackgroundColorTag;
import com.jpexs.decompiler.flash.tags.SetTabIndexTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.SoundStreamBlockTag;
import com.jpexs.decompiler.flash.tags.SoundStreamHead2Tag;
import com.jpexs.decompiler.flash.tags.SoundStreamHeadTag;
import com.jpexs.decompiler.flash.tags.StartSound2Tag;
import com.jpexs.decompiler.flash.tags.StartSoundTag;
import com.jpexs.decompiler.flash.tags.SymbolClassTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.TagStub;
import com.jpexs.decompiler.flash.tags.UnknownTag;
import com.jpexs.decompiler.flash.tags.VideoFrameTag;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.flash.tags.base.BinaryDataInterface;
import com.jpexs.decompiler.flash.tags.base.ButtonTag;
import com.jpexs.decompiler.flash.tags.base.FontInfoTag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.MorphShapeTag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.tags.base.RemoveTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.tags.base.SoundStreamHeadTypeTag;
import com.jpexs.decompiler.flash.tags.base.SymbolClassTypeTag;
import com.jpexs.decompiler.flash.tags.base.TextTag;
import com.jpexs.decompiler.flash.tags.gfx.DefineCompactedFont;
import com.jpexs.decompiler.flash.tags.gfx.DefineExternalSound;
import com.jpexs.decompiler.flash.tags.gfx.DefineExternalStreamSound;
import com.jpexs.decompiler.flash.timeline.AS2Package;
import com.jpexs.decompiler.flash.timeline.AS3Package;
import com.jpexs.decompiler.flash.timeline.Frame;
import com.jpexs.decompiler.flash.timeline.FrameScript;
import com.jpexs.decompiler.flash.timeline.Scene;
import com.jpexs.decompiler.flash.timeline.SceneFrame;
import com.jpexs.decompiler.flash.timeline.SoundStreamFrameRange;
import com.jpexs.decompiler.flash.timeline.TagScript;
import com.jpexs.decompiler.flash.treeitems.FolderItem;
import com.jpexs.decompiler.flash.treeitems.HeaderItem;
import com.jpexs.decompiler.flash.treeitems.Openable;
import com.jpexs.decompiler.flash.treeitems.OpenableList;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import com.jpexs.decompiler.flash.types.BUTTONCONDACTION;
import com.jpexs.decompiler.flash.types.BUTTONRECORD;
import com.jpexs.decompiler.flash.types.CLIPACTIONRECORD;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * @author JPEXS
 */
public abstract class AbstractTagTree extends JTree {

    public TagTreeContextMenu contextPopupMenu;

    protected final MainPanel mainPanel;

    private AbstractTagTreeModel fullModel;

    private static final Map<TreeNodeType, Icon> ICONS;

    protected Map<TreeItem, Set<Integer>> missingNeededCharacters = new WeakHashMap<>();

    static {
        ICONS = new HashMap<>();
        for (TreeNodeType treeNodeType : TreeNodeType.values()) {
            String tagTypeStr = treeNodeType.toString().toLowerCase(Locale.ENGLISH).replace("_", "");
            ICONS.put(treeNodeType, View.getIcon(tagTypeStr + "16"));
        }
    }

    @Override
    public void setModel(TreeModel newModel) {
        super.setModel(newModel);
        if (newModel instanceof AbstractTagTreeModel) {
            this.fullModel = (AbstractTagTreeModel) newModel;
        }
    }

    public MainPanel getMainPanel() {
        return mainPanel;
    }

    public Map<TreeItem, Set<Integer>> getMissingNeededCharacters() {
        return missingNeededCharacters;
    }

    public void setMissingNeededCharacters(Map<TreeItem, Set<Integer>> missingNeededCharacters) {
        this.missingNeededCharacters = missingNeededCharacters;
        repaint();
    }

    public static Icon getIconForType(TreeNodeType t) {
        return ICONS.get(t);
    }

    public static Icon getIconFor(TreeItem val) {
        return getIconFor(val, false);
    }

    public static Icon getIconFor(TreeItem val, boolean folderExpanded) {

        if (val instanceof SoundStreamHeadTypeTag) {
            return View.getIcon("foldersounds16");
        }

        if (val instanceof ClassesListTreeModel) {
            return View.getIcon("folderscripts16");
        }
        
        
        
        TreeNodeType type = getTreeNodeType(val);

        if (val instanceof FrameScript) {
            FrameScript fs = (FrameScript) val;
            if (fs.getSingleDoActionTag() != null) {
                type = TreeNodeType.AS_FRAME;
            }
        }
        
        if (type == TreeNodeType.FOLDER && folderExpanded) {
            type = TreeNodeType.FOLDER_OPEN;
        }

        if ((type == TreeNodeType.FOLDER || type == TreeNodeType.FOLDER_OPEN) && val instanceof FolderItem) {
            FolderItem si = (FolderItem) val;
            if (!TagTreeRoot.FOLDER_ROOT.equals(si.getName())) {
                String itemName = "folder" + si.getName();
                return View.getIcon(itemName.toLowerCase(Locale.ENGLISH) + "16");
            }
        } else {
            return getIconForType(type);
        }
        return null;
    }

    public AbstractTagTree(AbstractTagTreeModel treeModel, MainPanel mainPanel) {
        super(treeModel);
        this.mainPanel = mainPanel;
        setRootVisible(false);
        if (View.isOceanic()) {
            setBackground(Color.white);
        }
        setRowHeight(Math.max(getFont().getSize() + 5, 16));
        setLargeModel(true);
        setUI(new BasicTreeUI() {
            {
                if (View.isOceanic()) {
                    setHashColor(Color.gray);
                }
            }

            @Override
            protected MouseListener createMouseListener() {
                MouseListener handler = super.createMouseListener();
                return new MouseListener() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        handler.mouseClicked(e);
                    }

                    @Override
                    public void mousePressed(MouseEvent e) {
                        handler.mousePressed(e);
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        handler.mouseReleased(e);
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        handler.mouseEntered(e);
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        handler.mouseReleased(e); //crucial to properly free nodes
                        handler.mouseExited(e);
                    }
                    
                };
            }
            
        });
        ToolTipManager.sharedInstance().registerComponent(this);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!Configuration.doubleClickNodeToEdit.get()) {
                    return;
                }
                if (!SwingUtilities.isLeftMouseButton(e)) {
                    return;
                }
                if (e.getClickCount() != 2) {
                    return;
                }
                TreeItem item = getCurrentTreeItem();
                if (!getModel().isLeaf(item)) { //double click also expands the node so editing should work only for leaf nodes
                    return;
                }
                mainPanel.startEdit();
            }
        });
    }

    public static TreeNodeType getTreeNodeType(TreeItem t) {

        if (t instanceof TagScript) {
            t = ((TagScript) t).getTag();
        }

        if (t instanceof HeaderItem) {
            return TreeNodeType.HEADER;
        }

        if ((t instanceof DefineFontTag)
                || (t instanceof DefineFont2Tag)
                || (t instanceof DefineFont3Tag)
                || (t instanceof DefineFont4Tag)
                || (t instanceof DefineCompactedFont)) {
            return TreeNodeType.FONT;
        }

        // DefineText, DefineText2, DefineEditTextTag
        if (t instanceof TextTag) {
            return TreeNodeType.TEXT;
        }
        
        if (t instanceof CSMTextSettingsTag) {
            return TreeNodeType.CSM_TEXT_SETTINGS;
        }

        // DefineBits, DefineBitsJPEG2, DefineBitsJPEG3, DefineBitsJPEG4, DefineBitsLossless, DefineBitsLossless2
        if (t instanceof ImageTag) {
            return TreeNodeType.IMAGE;
        }

        // DefineShape, DefineShape2, DefineShape3, DefineShape4
        if (t instanceof ShapeTag) {
            return TreeNodeType.SHAPE;
        }

        // DefineMorphShape, DefineMorphShape2
        if (t instanceof MorphShapeTag) {
            return TreeNodeType.MORPH_SHAPE;
        }

        if (t instanceof DefineSpriteTag) {
            return TreeNodeType.SPRITE;
        }

        // DefineButton, DefineButton2
        if (t instanceof ButtonTag) {
            return TreeNodeType.BUTTON;
        }
        
        if (t instanceof DefineButtonCxformTag) {
            return TreeNodeType.BUTTON_CXFORM;
        }
        
        if (t instanceof DefineButtonSoundTag) {
            return TreeNodeType.BUTTON_SOUND;
        }

        if (t instanceof BUTTONRECORD) {
            return TreeNodeType.BUTTON_RECORD;
        }

        if (t instanceof DefineVideoStreamTag) {
            return TreeNodeType.MOVIE;
        }
        
        if (t instanceof VideoFrameTag) {
            return TreeNodeType.VIDEO_FRAME;
        }

        if ((t instanceof DefineSoundTag)
                || (t instanceof SoundStreamHeadTag)
                || (t instanceof SoundStreamHead2Tag)
                || (t instanceof DefineExternalSound)
                || (t instanceof DefineExternalStreamSound)
            ) {
            return TreeNodeType.SOUND;
        }
        
        if (t instanceof SoundStreamFrameRange) {
            return TreeNodeType.SOUND_STREAM_RANGE;
        }
        
        if (t instanceof SoundStreamBlockTag) {
            return TreeNodeType.SOUND_STREAM_BLOCK;
        }

        if (t instanceof BinaryDataInterface) {
            return TreeNodeType.BINARY_DATA;
        }

        if (Configuration.useAsTypeIcons.get()) {
            if (t instanceof DoInitActionTag) {
                DoInitActionTag doInit = (DoInitActionTag) t;
                if (doInit.getSwf().getExportName(doInit.spriteId) != null) {
                    return TreeNodeType.AS_CLASS;
                }
                return TreeNodeType.AS_INIT;
            }

            if (t instanceof CLIPACTIONRECORD) {
                return TreeNodeType.AS_CLIP;
            }

            if (t instanceof BUTTONCONDACTION) {
                return TreeNodeType.AS_BUTTON;
            }

            if (t instanceof DoActionTag) {
                return TreeNodeType.AS_FRAME;
            }
        }

        if (t instanceof ASMSource) {
            return TreeNodeType.AS;
        }

        if (t instanceof ScriptPack) {
            if (Configuration.useAsTypeIcons.get()) {
                ScriptPack pack = (ScriptPack) t;
                Trait trait = pack.getPublicTrait();
                if (trait == null) {
                    return TreeNodeType.AS;
                }
                if (trait instanceof TraitFunction) {
                    return TreeNodeType.AS_FUNCTION;
                }
                if (trait instanceof TraitMethodGetterSetter) {
                    return TreeNodeType.AS_FUNCTION;
                }
                if (trait instanceof TraitSlotConst) {
                    TraitSlotConst traitSlotConst = (TraitSlotConst) trait;
                    if (traitSlotConst.isConst()) {
                        return TreeNodeType.AS_CONST;
                    } else {
                        return TreeNodeType.AS_VAR;
                    }
                }
                if (trait instanceof TraitClass) {
                    TraitClass traitClass = (TraitClass) trait;
                    if (pack.abc.instance_info.get(traitClass.class_info).isInterface()) {
                        return TreeNodeType.AS_INTERFACE;
                    }
                    return TreeNodeType.AS_CLASS;
                }
            }
            return TreeNodeType.AS;
        }

        if (t instanceof AS2Package) {
            return TreeNodeType.PACKAGE;
        }

        if (t instanceof AS3Package) {
            AS3Package pkg = (AS3Package) t;
            if (pkg.isCompoundScript()) {
                return TreeNodeType.AS;
            }
            return TreeNodeType.PACKAGE;
        }

        if ((t instanceof Frame)
                || (t instanceof FrameScript)
                || (t instanceof SceneFrame)) {
            return TreeNodeType.FRAME;
        }

        if (t instanceof Scene) {
            return TreeNodeType.SCENE;
        }

        if (t instanceof ShowFrameTag) {
            return TreeNodeType.SHOW_FRAME;
        }
        
        if (t instanceof FrameLabelTag) {
            return TreeNodeType.FRAME_LABEL;
        }
        
        if ((t instanceof StartSoundTag)
                || (t instanceof StartSound2Tag)) {
            return TreeNodeType.START_SOUND;
        }

        if (t instanceof SWF) {
            return TreeNodeType.FLASH;
        }

        if (t instanceof OpenableList) {
            OpenableList slist = (OpenableList) t;
            if (slist.isBundle()) {
                if (slist.bundle.getClass() == ZippedBundle.class) {
                    return TreeNodeType.BUNDLE_ZIP;
                } else if (slist.bundle.getClass() == SWC.class) {
                    return TreeNodeType.BUNDLE_SWC;
                } else if (slist.bundle.getClass() == IggySwfBundle.class) {
                    return TreeNodeType.BUNDLE_IGGY;
                } else {
                    return TreeNodeType.BUNDLE_BINARY;
                }
            }
        }

        if (t instanceof SetBackgroundColorTag) {
            return TreeNodeType.SET_BACKGROUNDCOLOR;
        }
        if (t instanceof FileAttributesTag) {
            return TreeNodeType.FILE_ATTRIBUTES;
        }
        if (t instanceof MetadataTag) {
            return TreeNodeType.METADATA;
        }
        if (t instanceof PlaceObjectTypeTag) {
            return TreeNodeType.PLACE_OBJECT;
        }
        if (t instanceof RemoveTag) {
            return TreeNodeType.REMOVE_OBJECT;
        }

        if (t instanceof DefineScalingGridTag) {
            return TreeNodeType.SCALING_GRID;
        }

        if (t instanceof ABCContainerTag) {
            return TreeNodeType.AS_ABC;
        }
        
        if (t instanceof DefineFontAlignZonesTag) {
            return TreeNodeType.FONT_ALIGN_ZONES;
        }
        
        if (t instanceof FontInfoTag) {
            return TreeNodeType.FONT_INFO;
        }
        
        if (t instanceof DefineFontNameTag) {
            return TreeNodeType.FONT_NAME;
        }
        
        if (t instanceof EndTag) {
            return TreeNodeType.END;
        }
        
        if (t instanceof ProtectTag
                || t instanceof EnableDebuggerTag
                || t instanceof EnableDebugger2Tag) {
            return TreeNodeType.ENABLE_DEBUGGER;
        }
        
        if (t instanceof EnableTelemetryTag) {
            return TreeNodeType.ENABLE_TELEMETRY;
        }
        
        if (t instanceof ExportAssetsTag) {
            return TreeNodeType.EXPORT_ASSETS;
        }
        
        if (t instanceof ImportAssetsTag
                || t instanceof ImportAssets2Tag) {
            return TreeNodeType.IMPORT_ASSETS;
        }
        
        if (t instanceof JPEGTablesTag) {
            return TreeNodeType.JPEG_TABLES;
        }

        if (t instanceof ProductInfoTag) {
            return TreeNodeType.PRODUCT_INFO;
        }
        
        if (t instanceof ScriptLimitsTag) {
            return TreeNodeType.SCRIPT_LIMITS;
        }
        
        if (t instanceof SetTabIndexTag) {
            return TreeNodeType.SET_TABINDEX;
        }
        
        if (t instanceof SymbolClassTag) {
            return TreeNodeType.SYMBOL_CLASS;
        }
        
        if (t instanceof DefineSceneAndFrameLabelDataTag) {
            return TreeNodeType.SCENE_AND_FRAME_LABEL_DATA;
        }
        
        if (t instanceof DebugIDTag) {
            return TreeNodeType.DEBUG_ID;
        }
        
        if (t instanceof TagStub) {
            return TreeNodeType.ERRORED;
        }

        if (t instanceof UnknownTag) {
            return TreeNodeType.UNKNOWN;
        }
        
        if (t instanceof Tag) {
            return TreeNodeType.OTHER_TAG;
        }

        if (t instanceof FolderItem) {
            return TreeNodeType.FOLDER;
        }

        if (t instanceof ABC) {
            return TreeNodeType.ABC;
        }
        
        if (t instanceof Cookie) {
            return TreeNodeType.COOKIE;
        }

        return TreeNodeType.FOLDER;
    }

    public AbstractTagTreeModel getFullModel() {
        return fullModel;
    }

    public void expandRoot() {
        TreeModel ttm = getModel();
        Object root = ttm.getRoot();
        expandPath(new TreePath(new Object[]{root}));
    }

    public void expandFirstLevelNodes() {
        TreeModel ttm = getModel();
        Object root = ttm.getRoot();
        int childCount = ttm.getChildCount(root);
        for (int i = 0; i < childCount; i++) {
            expandPath(new TreePath(new Object[]{root, ttm.getChild(root, i)}));
        }
    }

    public void setExpandPathString(String pathStr) {
        if (pathStr != null && pathStr.length() > 0) {
            String[] path = pathStr.split("\\|");

            TreePath tp = View.getTreePathByPathStrings(this, Arrays.asList(path));
            if (tp != null) {
                // the current view is the Resources view, otherwise tp is null
                /*if (mainPanel.getCurrentView() == MainPanel.VIEW_RESOURCES) {
                    mainPanel.tagTree.expandPath(tp.getParentPath());
                }
                if (mainPanel.getCurrentView() == MainPanel.VIEW_TAGLIST) {
                    mainPanel.tagListTree.expandPath(tp.getParentPath());
                 */
                expandPath(tp.getParentPath());
            }
        }
    }

    public TreePath getTreePathFromString(String pathStr) {
        if (pathStr == null || pathStr.length() == 0) {
            return null;
        }
        String[] path = pathStr.split("\\|");
        return View.getTreePathByPathStrings(this, Arrays.asList(path));
    }

    public TreeItem getTreeItemFromPathString(String pathStr) {
        TreePath path = getTreePathFromString(pathStr);
        if (path == null) {
            return null;
        }
        return (TreeItem) path.getLastPathComponent();
    }

    public void setSelectionPathString(String pathStr) {
        TreeItem item = getTreeItemFromPathString(pathStr);
        if (item != null) {
            // the current view is the Resources view, otherwise tp is null
            mainPanel.setTagTreeSelectedNode(this, item);
        }
    }

    public void getAllSubs(TreeItem o, List<TreeItem> ret) {
        AbstractTagTreeModel tm = getFullModel();
        for (TreeItem c : tm.getAllChildren(o)) {
            ret.add(c);
            getAllSubs(c, ret);
        }
    }

    public List<TreeItem> getAllSelected() {
        TreePath[] tps = getSelectionPathsSorted();
        List<TreeItem> ret = new ArrayList<>();
        if (tps == null) {
            return ret;
        }

        for (TreePath tp : tps) {
            TreeItem treeNode = (TreeItem) tp.getLastPathComponent();
            ret.add(treeNode);
            getAllSubs(treeNode, ret);
        }
        return ret;
    }

    public List<TreeItem> getAllSubsForItems(List<TreeItem> items) {
        List<TreeItem> ret = new ArrayList<>();
        for (TreeItem item : items) {
            ret.add(item);
            getAllSubs(item, ret);
        }
        return ret;
    }

    public TreePath[] getSelectionPathsSorted() {
        TreePath[] paths = getSelectionPaths();
        if (paths == null) {
            return null;
        }
        Arrays.sort(paths, new Comparator<TreePath>() {
            @Override
            public int compare(TreePath o1, TreePath o2) {
                return getRowForPath(o1) - getRowForPath(o2);
            }
        });
        return paths;
    }

    public List<TreeItem> getSelected() {
        if (mainPanel.folderPreviewCard.isVisible() && mainPanel.folderPreviewPanel.isSomethingSelected()) {
            return mainPanel.folderPreviewPanel.getSelectedItemsSorted();
        }
        TreePath[] tps = getSelectionPathsSorted();
        List<TreeItem> ret = new ArrayList<>();
        if (tps == null) {
            return ret;
        }

        for (TreePath tp : tps) {
            TreeItem treeNode = (TreeItem) tp.getLastPathComponent();
            ret.add(treeNode);
        }
        return ret;
    }

    public boolean hasExportableNodes() {
        return !getSelection(mainPanel.getCurrentSwf()).isEmpty();
    }

    public List<TreeItem> getSelectionAndAllSubs(Openable openable, List<TreeItem> selection) {
        List<TreeItem> sel = new ArrayList<>();

        for (TreeItem treeItem : selection) {
            sel.add(treeItem);
            getAllSubs(treeItem, sel);
        }
        return getSelection(openable, sel);
    }

    public abstract List<TreeItem> getSelection(Openable openable);

    public static List<TreeItem> getSelection(Openable openable, List<TreeItem> sel) {
        List<TreeItem> ret = new ArrayList<>();
        for (TreeItem d : sel) {
            if (d instanceof OpenableList) {
                continue;
            }
            if (openable != null && d.getOpenable() != openable) {
                continue;
            }

            if (d instanceof TagScript) {
                Tag tag = ((TagScript) d).getTag();
                if (tag instanceof DoActionTag || tag instanceof DoInitActionTag) {
                    d = tag;
                }
            }
            if (d instanceof FrameScript) {
                DoActionTag tag = ((FrameScript) d).getSingleDoActionTag();
                if (tag != null) {
                    d = tag;
                }
            }

            if (d instanceof Tag
                    || d instanceof ASMSource
                    || d instanceof BinaryDataInterface
                    || d instanceof SoundStreamFrameRange) {
                TreeNodeType nodeType = TagTree.getTreeNodeType(d);
                if (nodeType == TreeNodeType.IMAGE) {
                    ret.add(d);
                }
                if (nodeType == TreeNodeType.SHAPE) {
                    ret.add(d);
                }
                if (nodeType == TreeNodeType.MORPH_SHAPE) {
                    ret.add(d);
                }
                if (nodeType == TreeNodeType.SPRITE) {
                    ret.add(d);
                }
                if (nodeType == TreeNodeType.BUTTON) {
                    ret.add(d);
                }
                if (nodeType == TreeNodeType.AS
                        || nodeType == TreeNodeType.AS_BUTTON
                        || nodeType == TreeNodeType.AS_CLASS
                        || nodeType == TreeNodeType.AS_CLIP
                        || nodeType == TreeNodeType.AS_CONST
                        || nodeType == TreeNodeType.AS_FRAME
                        || nodeType == TreeNodeType.AS_FUNCTION
                        || nodeType == TreeNodeType.AS_INIT
                        || nodeType == TreeNodeType.AS_INTERFACE
                        || nodeType == TreeNodeType.AS_VAR) {
                    ret.add(d);
                }
                if (nodeType == TreeNodeType.MOVIE) {
                    ret.add(d);
                }
                if (nodeType == TreeNodeType.SOUND) {
                    ret.add(d);
                }
                if (nodeType == TreeNodeType.SOUND_STREAM_RANGE) {
                    ret.add(d);
                }
                if (nodeType == TreeNodeType.BINARY_DATA) {
                    ret.add(d);
                }
                if (nodeType == TreeNodeType.TEXT) {
                    ret.add(d);
                }
                if (nodeType == TreeNodeType.FONT) {
                    ret.add(d);
                }
                if (nodeType == TreeNodeType.SYMBOL_CLASS) {
                    ret.add(d);
                }
            }

            if (d instanceof Frame) {
                ret.add(d);
            }
            if (d instanceof ScriptPack) {
                ret.add(d);
            }
            if (d instanceof AS3Package) {
                AS3Package p = (AS3Package) d;
                if (p.isCompoundScript()) {
                    ret.add(d);
                }
            }
        }
        return ret;
    }

    public void updateSwfs(Openable[] openables) {
        AbstractTagTreeModel ttm = getFullModel();
        if (ttm != null) {
            List<List<String>> expandedNodes = View.getExpandedNodes(this);
            ttm.updateOpenable(null); // todo: honfika: update only the changed swfs, but there was an exception when i tried it
            View.expandTreeNodes(this, expandedNodes);
        }
    }

    public static List<Integer> getFrameNestedTagIds() {
        return Arrays.asList(PlaceObjectTag.ID, PlaceObject2Tag.ID, PlaceObject3Tag.ID, PlaceObject4Tag.ID,
                RemoveObjectTag.ID, RemoveObject2Tag.ID, ShowFrameTag.ID, FrameLabelTag.ID,
                StartSoundTag.ID, StartSound2Tag.ID, VideoFrameTag.ID,
                SoundStreamBlockTag.ID, SoundStreamHeadTag.ID, SoundStreamHead2Tag.ID,
                SetTabIndexTag.ID
        );
    }

    public TreeItem getCurrentTreeItem() {
        TreeItem item = (TreeItem) getLastSelectedPathComponent();
        return item;
    }

    public String getItemPathString(TreeItem item) {
        TreePath path = getFullModel().getTreePath(item);
        if (path == null) {
            return null;
        }
        return pathToString(path);
    }

    public final void calculateCollisions() {
        getFullModel().calculateCollisions();
    }

    public String pathToString(TreePath path) {
        StringBuilder sb = new StringBuilder();
        AbstractTagTreeModel model = getFullModel();
        if (path != null) {
            boolean first = true;
            for (Object p : path.getPath()) {
                if (!first) {
                    sb.append("|");
                }

                first = false;
                sb.append(p.toString());
                int index = model.getItemIndex((TreeItem) p);
                if (index > 1) {
                    sb.append(" [").append(index).append("]");
                }
            }
        }
        return sb.toString();
    }

    public String getSelectionPathString() {
        return pathToString(getSelectionPath());
    }

    public static TreeNodeType getTagNodeTypeFromTagClass(Class<?> cl) {
        if ((cl == DefineFontTag.class)
                || (cl == DefineFont2Tag.class)
                || (cl == DefineFont3Tag.class)
                || (cl == DefineFont4Tag.class)
                || (cl == DefineCompactedFont.class)) {
            return TreeNodeType.FONT;
        }
        
        if (FontInfoTag.class.isAssignableFrom(cl)) {
            return TreeNodeType.FONT_INFO;
        }
        
        if (cl == DefineFontNameTag.class) {
            return TreeNodeType.FONT_NAME;
        }
        
        if (cl == DefineFontAlignZonesTag.class) {
            return TreeNodeType.FONT_ALIGN_ZONES;
        }
        
        if (ABCContainerTag.class.isAssignableFrom(cl)) {
            return TreeNodeType.AS_ABC;
        }

        // DefineText, DefineText2, DefineEditTextTag
        if (TextTag.class.isAssignableFrom(cl)) {
            return TreeNodeType.TEXT;
        }
        
        if (cl == CSMTextSettingsTag.class) {
            return TreeNodeType.CSM_TEXT_SETTINGS;
        }

        // DefineBits, DefineBitsJPEG2, DefineBitsJPEG3, DefineBitsJPEG4, DefineBitsLossless, DefineBitsLossless2
        if (ImageTag.class.isAssignableFrom(cl)) {
            return TreeNodeType.IMAGE;
        }

        // DefineShape, DefineShape2, DefineShape3, DefineShape4
        if (ShapeTag.class.isAssignableFrom(cl)) {
            return TreeNodeType.SHAPE;
        }

        // DefineMorphShape, DefineMorphShape2
        if (MorphShapeTag.class.isAssignableFrom(cl)) {
            return TreeNodeType.MORPH_SHAPE;
        }

        if (cl == DefineSpriteTag.class) {
            return TreeNodeType.SPRITE;
        }

        // DefineButton, DefineButton2
        if (ButtonTag.class.isAssignableFrom(cl)) {
            return TreeNodeType.BUTTON;
        }
        
        if (cl == DefineButtonCxformTag.class) {
            return TreeNodeType.BUTTON_CXFORM;
        }
        
        if (cl == DefineButtonSoundTag.class) {
            return TreeNodeType.BUTTON_SOUND;
        }

        if (cl == DefineVideoStreamTag.class) {
            return TreeNodeType.MOVIE;
        }
        
        if (cl == VideoFrameTag.class) {
            return TreeNodeType.VIDEO_FRAME;
        }

        if ((cl == DefineSoundTag.class) || (cl == SoundStreamHeadTag.class) || (cl == SoundStreamHead2Tag.class)) {
            return TreeNodeType.SOUND;
        }
        
        if (cl == SoundStreamBlockTag.class) {
            return TreeNodeType.SOUND_STREAM_BLOCK;
        }

        if (cl == DefineBinaryDataTag.class) {
            return TreeNodeType.BINARY_DATA;
        }

        if (Configuration.useAsTypeIcons.get()) {
            if (cl == DoInitActionTag.class) {
                return TreeNodeType.AS_INIT;
            }

            if (cl == DoActionTag.class) {
                return TreeNodeType.AS_FRAME;
            }
        }

        if (cl == ShowFrameTag.class) {
            return TreeNodeType.SHOW_FRAME;
        }
        
        if (cl == FrameLabelTag.class) {
            return TreeNodeType.FRAME_LABEL;
        }
        
        if (cl == StartSoundTag.class
                || cl == StartSound2Tag.class) {
            return TreeNodeType.START_SOUND;
        }

        if (cl == SetBackgroundColorTag.class) {
            return TreeNodeType.SET_BACKGROUNDCOLOR;
        }
        if (cl == FileAttributesTag.class) {
            return TreeNodeType.FILE_ATTRIBUTES;
        }
        if (cl == MetadataTag.class) {
            return TreeNodeType.METADATA;
        }
        if (PlaceObjectTypeTag.class.isAssignableFrom(cl)) {
            return TreeNodeType.PLACE_OBJECT;
        }
        if (RemoveTag.class.isAssignableFrom(cl)) {
            return TreeNodeType.REMOVE_OBJECT;
        }

        if (cl == EndTag.class) {
            return TreeNodeType.END;
        }

        if (cl == DefineScalingGridTag.class) {
            return TreeNodeType.SCALING_GRID;
        }

        if (cl == ProtectTag.class
                || cl == EnableDebuggerTag.class
                || cl == EnableDebugger2Tag.class) {
            return TreeNodeType.ENABLE_DEBUGGER;
        }
        
        if (cl == EnableTelemetryTag.class) {
            return TreeNodeType.ENABLE_TELEMETRY;
        }
        
        if (cl == ExportAssetsTag.class) {
            return TreeNodeType.EXPORT_ASSETS;
        }       
        
        if (cl == ImportAssetsTag.class
                || cl == ImportAssets2Tag.class) {
            return TreeNodeType.IMPORT_ASSETS;
        }
        
        if (cl == JPEGTablesTag.class) {
            return TreeNodeType.JPEG_TABLES;
        }
        
        if (cl == ProductInfoTag.class) {
            return TreeNodeType.PRODUCT_INFO;
        }
        
        if (cl == ScriptLimitsTag.class) {
            return TreeNodeType.SCRIPT_LIMITS;
        }
        
        if (cl == SetTabIndexTag.class) {
            return TreeNodeType.SET_TABINDEX;
        }
        
        if (cl == SymbolClassTag.class) {
            return TreeNodeType.SYMBOL_CLASS;
        }
        
        if (cl == DefineSceneAndFrameLabelDataTag.class) {
            return TreeNodeType.SCENE_AND_FRAME_LABEL_DATA;
        }
        
        if (cl == DebugIDTag.class) {
            return TreeNodeType.DEBUG_ID;
        }
        
        if (Tag.class.isAssignableFrom(cl)) {
            return TreeNodeType.OTHER_TAG;
        }

        return TreeNodeType.FOLDER;
    }

    public static List<Integer> getMappedTagIdsForClass(Class<?> cls) {
        if (cls == DefineSpriteTag.class) {
            return Arrays.asList(DefineScalingGridTag.ID, DoInitActionTag.ID);
        }
        if (FontTag.class.isAssignableFrom(cls)) {
            return Arrays.asList(DefineFontNameTag.ID, DefineFontAlignZonesTag.ID, DefineFontInfoTag.ID, DefineFontInfo2Tag.ID);
        }
        if (TextTag.class.isAssignableFrom(cls)) {
            return Arrays.asList(CSMTextSettingsTag.ID);
        }
        if (cls == DefineButtonTag.class) {
            return Arrays.asList(DefineButtonCxformTag.ID, DefineButtonSoundTag.ID, DefineScalingGridTag.ID);
        }
        if (cls == DefineButton2Tag.class) {
            return Arrays.asList(DefineButtonSoundTag.ID, DefineScalingGridTag.ID);
        }
        return new ArrayList<>();
    }

}
