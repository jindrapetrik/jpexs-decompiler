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
package com.jpexs.decompiler.flash.gui;

import com.jpexs.decompiler.flash.AbortRetryIgnoreHandler;
import com.jpexs.decompiler.flash.ApplicationInfo;
import com.jpexs.decompiler.flash.Bundle;
import com.jpexs.decompiler.flash.DecompilerPool;
import com.jpexs.decompiler.flash.EventListener;
import com.jpexs.decompiler.flash.OpenableSourceInfo;
import com.jpexs.decompiler.flash.ReadOnlyTagList;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.RenameType;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.deobfuscation.AbcMultiNameCollisionFixer;
import com.jpexs.decompiler.flash.abc.avm2.deobfuscation.DeobfuscationLevel;
import com.jpexs.decompiler.flash.abc.avm2.deobfuscation.DeobfuscationScope;
import com.jpexs.decompiler.flash.abc.types.ClassInfo;
import com.jpexs.decompiler.flash.abc.types.InstanceInfo;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitClass;
import com.jpexs.decompiler.flash.abc.types.traits.TraitFunction;
import com.jpexs.decompiler.flash.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.configuration.ConfigurationItem;
import com.jpexs.decompiler.flash.configuration.ConfigurationItemChangeListener;
import com.jpexs.decompiler.flash.configuration.CustomConfigurationKeys;
import com.jpexs.decompiler.flash.configuration.SwfSpecificCustomConfiguration;
import com.jpexs.decompiler.flash.dumpview.DumpInfo;
import com.jpexs.decompiler.flash.dumpview.DumpInfoSwfNode;
import com.jpexs.decompiler.flash.easygui.EasyPanel;
import com.jpexs.decompiler.flash.exporters.BinaryDataExporter;
import com.jpexs.decompiler.flash.exporters.Font4Exporter;
import com.jpexs.decompiler.flash.exporters.FontExporter;
import com.jpexs.decompiler.flash.exporters.FrameExporter;
import com.jpexs.decompiler.flash.exporters.ImageExporter;
import com.jpexs.decompiler.flash.exporters.MorphShapeExporter;
import com.jpexs.decompiler.flash.exporters.MovieExporter;
import com.jpexs.decompiler.flash.exporters.ShapeExporter;
import com.jpexs.decompiler.flash.exporters.SoundExporter;
import com.jpexs.decompiler.flash.exporters.SymbolClassExporter;
import com.jpexs.decompiler.flash.exporters.TextExporter;
import com.jpexs.decompiler.flash.exporters.modes.BinaryDataExportMode;
import com.jpexs.decompiler.flash.exporters.modes.ButtonExportMode;
import com.jpexs.decompiler.flash.exporters.modes.Font4ExportMode;
import com.jpexs.decompiler.flash.exporters.modes.FontExportMode;
import com.jpexs.decompiler.flash.exporters.modes.FrameExportMode;
import com.jpexs.decompiler.flash.exporters.modes.ImageExportMode;
import com.jpexs.decompiler.flash.exporters.modes.MorphShapeExportMode;
import com.jpexs.decompiler.flash.exporters.modes.MovieExportMode;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.exporters.modes.ShapeExportMode;
import com.jpexs.decompiler.flash.exporters.modes.SoundExportMode;
import com.jpexs.decompiler.flash.exporters.modes.SpriteExportMode;
import com.jpexs.decompiler.flash.exporters.modes.SymbolClassExportMode;
import com.jpexs.decompiler.flash.exporters.modes.TextExportMode;
import com.jpexs.decompiler.flash.exporters.script.AS2ScriptExporter;
import com.jpexs.decompiler.flash.exporters.script.AS3ScriptExporter;
import com.jpexs.decompiler.flash.exporters.settings.BinaryDataExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.ButtonExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.Font4ExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.FontExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.FrameExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.ImageExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.MorphShapeExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.MovieExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.ScriptExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.ShapeExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.SoundExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.SpriteExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.SymbolClassExportSettings;
import com.jpexs.decompiler.flash.exporters.settings.TextExportSettings;
import com.jpexs.decompiler.flash.exporters.swf.SwfFlashDevelopExporter;
import com.jpexs.decompiler.flash.exporters.swf.SwfIntelliJIdeaExporter;
import com.jpexs.decompiler.flash.exporters.swf.SwfJavaExporter;
import com.jpexs.decompiler.flash.exporters.swf.SwfVsCodeExporter;
import com.jpexs.decompiler.flash.exporters.swf.SwfXmlExporter;
import com.jpexs.decompiler.flash.flexsdk.MxmlcAs3ScriptReplacer;
import com.jpexs.decompiler.flash.gui.abc.ABCExplorerDialog;
import com.jpexs.decompiler.flash.gui.abc.ABCPanel;
import com.jpexs.decompiler.flash.gui.abc.ClassesListTreeModel;
import com.jpexs.decompiler.flash.gui.abc.DecompiledEditorPane;
import com.jpexs.decompiler.flash.gui.abc.DeobfuscationDialog;
import com.jpexs.decompiler.flash.gui.action.ActionPanel;
import com.jpexs.decompiler.flash.gui.controls.JPersistentSplitPane;
import com.jpexs.decompiler.flash.gui.dumpview.DumpTree;
import com.jpexs.decompiler.flash.gui.dumpview.DumpTreeModel;
import com.jpexs.decompiler.flash.gui.dumpview.DumpViewPanel;
import com.jpexs.decompiler.flash.gui.editor.LineMarkedEditorPane;
import com.jpexs.decompiler.flash.gui.helpers.CollectionChangedAction;
import com.jpexs.decompiler.flash.gui.helpers.ObservableList;
import com.jpexs.decompiler.flash.gui.soleditor.Cookie;
import com.jpexs.decompiler.flash.gui.taglistview.TagListTree;
import com.jpexs.decompiler.flash.gui.taglistview.TagListTreeModel;
import com.jpexs.decompiler.flash.gui.tagtree.AbstractTagTree;
import com.jpexs.decompiler.flash.gui.tagtree.AbstractTagTreeModel;
import com.jpexs.decompiler.flash.gui.tagtree.FilteredTreeModel;
import com.jpexs.decompiler.flash.gui.tagtree.TagTree;
import com.jpexs.decompiler.flash.gui.tagtree.TagTreeContextMenu;
import com.jpexs.decompiler.flash.gui.tagtree.TagTreeModel;
import com.jpexs.decompiler.flash.gui.tagtree.TreeRoot;
import com.jpexs.decompiler.flash.helpers.FileTextWriter;
import com.jpexs.decompiler.flash.helpers.Freed;
import com.jpexs.decompiler.flash.importers.AS2ScriptImporter;
import com.jpexs.decompiler.flash.importers.AS3ScriptImporter;
import com.jpexs.decompiler.flash.importers.As3ScriptReplacerFactory;
import com.jpexs.decompiler.flash.importers.As3ScriptReplacerInterface;
import com.jpexs.decompiler.flash.importers.BinaryDataImporter;
import com.jpexs.decompiler.flash.importers.FFDecAs3ScriptReplacer;
import com.jpexs.decompiler.flash.importers.ImageImporter;
import com.jpexs.decompiler.flash.importers.MovieImporter;
import com.jpexs.decompiler.flash.importers.ScriptImporterProgressListener;
import com.jpexs.decompiler.flash.importers.ShapeImporter;
import com.jpexs.decompiler.flash.importers.SoundImporter;
import com.jpexs.decompiler.flash.importers.SpriteImporter;
import com.jpexs.decompiler.flash.importers.SwfXmlImporter;
import com.jpexs.decompiler.flash.importers.SymbolClassImporter;
import com.jpexs.decompiler.flash.importers.TextImporter;
import com.jpexs.decompiler.flash.importers.morphshape.MorphShapeGenerator;
import com.jpexs.decompiler.flash.importers.morphshape.StyleMismatchException;
import com.jpexs.decompiler.flash.importers.svg.SvgImporter;
import com.jpexs.decompiler.flash.search.ABCSearchResult;
import com.jpexs.decompiler.flash.search.ActionSearchResult;
import com.jpexs.decompiler.flash.search.ScriptSearchResult;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.flash.tags.DefineBinaryDataTag;
import com.jpexs.decompiler.flash.tags.DefineBitsJPEG3Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsJPEG4Tag;
import com.jpexs.decompiler.flash.tags.DefineFont4Tag;
import com.jpexs.decompiler.flash.tags.DefineMorphShape2Tag;
import com.jpexs.decompiler.flash.tags.DefineShape4Tag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.DefineVideoStreamTag;
import com.jpexs.decompiler.flash.tags.DoActionTag;
import com.jpexs.decompiler.flash.tags.DoInitActionTag;
import com.jpexs.decompiler.flash.tags.FileAttributesTag;
import com.jpexs.decompiler.flash.tags.MetadataTag;
import com.jpexs.decompiler.flash.tags.PlaceObject3Tag;
import com.jpexs.decompiler.flash.tags.ProductInfoTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.TagInfo;
import com.jpexs.decompiler.flash.tags.UnknownTag;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.flash.tags.base.BinaryDataInterface;
import com.jpexs.decompiler.flash.tags.base.BoundedTag;
import com.jpexs.decompiler.flash.tags.base.ButtonTag;
import com.jpexs.decompiler.flash.tags.base.CharacterIdTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.DrawableTag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.MissingCharacterHandler;
import com.jpexs.decompiler.flash.tags.base.MorphShapeTag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.tags.base.SoundImportException;
import com.jpexs.decompiler.flash.tags.base.SoundStreamHeadTypeTag;
import com.jpexs.decompiler.flash.tags.base.SoundTag;
import com.jpexs.decompiler.flash.tags.base.SymbolClassTypeTag;
import com.jpexs.decompiler.flash.tags.base.TextImportErrorHandler;
import com.jpexs.decompiler.flash.tags.base.TextTag;
import com.jpexs.decompiler.flash.tags.base.UnsupportedSamplingRateException;
import com.jpexs.decompiler.flash.tags.gfx.DefineExternalStreamSound;
import com.jpexs.decompiler.flash.tags.text.TextParseException;
import com.jpexs.decompiler.flash.timeline.AS3Package;
import com.jpexs.decompiler.flash.timeline.Frame;
import com.jpexs.decompiler.flash.timeline.Scene;
import com.jpexs.decompiler.flash.timeline.SceneFrame;
import com.jpexs.decompiler.flash.timeline.SoundStreamFrameRange;
import com.jpexs.decompiler.flash.timeline.TagScript;
import com.jpexs.decompiler.flash.timeline.Timeline;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.treeitems.FolderItem;
import com.jpexs.decompiler.flash.treeitems.HeaderItem;
import com.jpexs.decompiler.flash.treeitems.Openable;
import com.jpexs.decompiler.flash.treeitems.OpenableList;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import com.jpexs.decompiler.flash.types.BUTTONRECORD;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.sound.SoundFormat;
import com.jpexs.decompiler.flash.xfl.FLAVersion;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.LinkedIdentityHashSet;
import com.jpexs.helpers.Path;
import com.jpexs.helpers.ProgressListener;
import com.jpexs.helpers.Reference;
import com.jpexs.helpers.SerializableImage;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import jsyntaxpane.DefaultSyntaxKit;

/**
 * @author JPEXS
 */
public final class MainPanel extends JPanel implements TreeSelectionListener, SearchListener<TextTag>, Freed {

    private final MainFrame mainFrame;

    private final ObservableList<OpenableList> openables;

    private final JPanel welcomePanel;

    public final EasyPanel easyPanel;

    private final MainFrameStatusPanel statusPanel;

    private Thread taskThread;

    private final MainFrameMenu mainMenu;

    private final JProgressBar progressBar = new JProgressBar(0, 100);

    public TagTree tagTree;

    public FasterScrollPane tagTreeScrollPanel;

    public DumpTree dumpTree;

    public TagListTree tagListTree;

    private ClipboardPanel resourcesClipboardPanel;
    private ClipboardPanel tagListClipboardPanel;
    
    private final JPanel contentPanel;

    private final JPanel displayPanel;

    public FolderPreviewPanel folderPreviewPanel;

    private String currentFolderName = null;

    public JPanel folderPreviewCard;

    public FolderListPanel folderListPanel;

    private boolean isWelcomeScreen = true;

    private static final String CARDPREVIEWPANEL = "Preview card";

    private static final String CARDFOLDERPREVIEWPANEL = "Folder preview card";

    private static final String CARDFOLDERLISTPANEL = "Folder list card";

    private static final String CARDEMPTYPANEL = "Empty card";

    private static final String CARDDUMPVIEW = "Dump view";

    private static final String CARDACTIONSCRIPTPANEL = "ActionScript card";

    private static final String CARDACTIONSCRIPT3PANEL = "ActionScript3 card";

    private static final String CARDHEADER = "Header card";

    private static final int DETAILCARDAS3NAVIGATOR = 1;

    private static final int DETAILCARDTAGINFO = 0;

    private static final int DETAILCARDEMPTYPANEL = -1;

    private static final int DETAILCARDDEBUGSTACKFRAME = 2;

    private static final String SPLIT_PANE1 = "SPLITPANE1";

    private static final String WELCOME_PANEL = "WELCOMEPANEL";

    private static final String EASY_PANEL = "EASYPANEL";

    private static final String RESOURCES_VIEW = "RESOURCES";

    private static final String DUMP_VIEW = "DUMP";

    private static final String TAGLIST_VIEW = "TAGLIST";

    private static final String EASY_VIEW = "EASY";

    private final JPersistentSplitPane splitPane1;

    private final JPersistentSplitPane splitPane2;

    private final JTabbedPane detailPanel;

    private QuickTreeFilterPanel quickTreeFindPanel;

    private QuickTreeFindPanel quickTagListFindPanel;

    private ABCPanel abcPanel;

    private ActionPanel actionPanel;

    private final PreviewPanel previewPanel;

    private final HeaderInfoPanel headerPanel;

    private DumpViewPanel dumpViewPanel;

    private final JPanel treePanel;

    private final PreviewPanel dumpPreviewPanel;

    private final TagInfoPanel tagInfoPanel;

    private final DebugStackPanel debugStackPanel;

    private TreePanelMode treePanelMode;

    public TreeItem oldItem;

    private int currentView = VIEW_RESOURCES;

    public List<SearchResultsDialog> searchResultsDialogs = new ArrayList<>();

    private TagTreeContextMenu contextPopupMenu;

    private static final Logger logger = Logger.getLogger(MainPanel.class.getName());

    private Map<TreeItem, Set<Integer>> neededCharacters = new WeakHashMap<>();
    private Map<TreeItem, Set<Integer>> missingNeededCharacters = new WeakHashMap<>();

    private CalculateMissingNeededThread calculateMissingNeededThread;

    private List<WeakReference<TreeItem>> orderedClipboard = new ArrayList<>();
    private Map<TreeItem, Boolean> clipboard = new WeakHashMap<>();

    private boolean clipboardCut = false;

    private PinsPanel pinsPanel;

    private List<List<String>> unfilteredTreeExpandedNodes = new ArrayList<>();
    private List<List<String>> unfilteredTagListExpandedNodes = new ArrayList<>();

    public ScrollPosStorage scrollPosStorage;

    private Map<Openable, ABCExplorerDialog> abcExplorerDialogs = new WeakHashMap<>();

    private Map<SWF, BreakpointListDialog> breakpointsListDialogs = new WeakHashMap<>();

    public void savePins() {
        pinsPanel.save();
    }

    public void clearPins() {
        pinsPanel.setCurrent(null);
        pinsPanel.clear();
    }

    public void refreshPins() {
        pinsPanel.load();
    }

    public void destroyPins() {
        pinsPanel.destroy();
    }

    public void unpinItem(TreeItem item) {
        pinsPanel.removeItem(item);
    }

    public void unpinOthers(TreeItem item) {
        pinsPanel.removeOthers(item);
    }

    public void pinItem(TreeItem item) {
        pinsPanel.pin(item);
    }

    public int getPinCount() {
        return pinsPanel.getPinCount();
    }

    public boolean isPinned(TreeItem item) {
        return pinsPanel.isPinned(item);
    }

    public void replaceItemPin(TreeItem oldItem, TreeItem newItem) {
        pinsPanel.replaceItem(oldItem, newItem);
    }

    public void refreshPinnedScriptPacks() {
        pinsPanel.refreshScriptPacks();
    }

    private void handleKeyReleased(KeyEvent e) {
        if (checkEdited()) {
            return;
        }
        Object source = e.getSource();
        List<TreeItem> items = new ArrayList<>();
        if (source == folderPreviewPanel) {
            items.addAll(folderPreviewPanel.getSelectedItemsSorted());
        } else if (source == folderListPanel) {
            items.addAll(folderListPanel.getSelectedItemsSorted());
        } else {
            AbstractTagTree tree = (AbstractTagTree) e.getSource();
            TreePath[] paths = tree.getSelectionPathsSorted();
            if (paths != null) {
                for (TreePath treePath : paths) {
                    TreeItem item = (TreeItem) treePath.getLastPathComponent();
                    items.add(item);
                }
            }
        }
        if (items.isEmpty()) {
            return;
        }

        if ((e.getKeyCode() == KeyEvent.VK_UP
                || e.getKeyCode() == KeyEvent.VK_DOWN)
                && e.isAltDown() && !e.isControlDown() && !e.isShiftDown()) {
            TreeItem item = items.get(0);

            if (item instanceof Tag) {
                if (((Tag) item).isReadOnly()) {
                    return;
                }
            }

            if (e.getKeyCode() == KeyEvent.VK_UP) {
                contextPopupMenu.moveUpDown(item, true);
            }
            if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                contextPopupMenu.moveUpDown(item, false);
            }
        }
    }

    public void moveSwfListUpDown(TreeItem item, boolean up) {
        OpenableList openableList = null;
        if (item instanceof Openable) {
            Openable openable = (Openable) item;
            if (openable.getOpenableList() != null && !openable.getOpenableList().isBundle() && openable.getOpenableList().size() == 1) {
                openableList = openable.getOpenableList();
            } else {
                return;
            }
        } else if (item instanceof OpenableList) {
            openableList = (OpenableList) item;
        } else {
            return;
        }
        int index = openables.indexOf(openableList);

        List<List<String>> expandedTagTree = View.getExpandedNodes(tagTree);
        List<List<String>> expandedTagListTree = View.getExpandedNodes(tagListTree);

        if (up) {
            if (index <= 0) {
                return;
            }
            openables.move(index, index - 1);
        } else {
            if (index < 0 || index >= openables.size() - 1) {
                return;
            }
            openables.move(index, index + 2);
        }
        View.expandTreeNodes(tagTree, expandedTagTree);
        View.expandTreeNodes(tagListTree, expandedTagListTree);
        TreePath path = getCurrentTree().getFullModel().getTreePath(item);
        getCurrentTree().setSelectionPath(path);
        getCurrentTree().scrollPathToVisible(path);
        repaintTree();
    }

    public void hideQuickTreeFind() {
        quickTreeFindPanel.setVisible(false);
        quickTagListFindPanel.setVisible(false);
    }

    private void handleKeyPressed(KeyEvent e) {
        if (checkEdited()) {
            return;
        }
        Object source = e.getSource();
        List<TreeItem> items = new ArrayList<>();
        if (source == folderPreviewPanel) {
            items.addAll(folderPreviewPanel.getSelectedItemsSorted());
        } else if (source == folderListPanel) {
            items.addAll(folderListPanel.getSelectedItemsSorted());
        } else {
            AbstractTagTree tree = (AbstractTagTree) e.getSource();
            TreePath[] paths = tree.getSelectionPathsSorted();
            if (paths != null) {
                for (TreePath treePath : paths) {
                    TreeItem item = (TreeItem) treePath.getLastPathComponent();
                    items.add(item);
                }
            }
        }

        if (items.isEmpty()) {
            return;
        }
        if ((e.getKeyCode() == 'G') && (e.isControlDown())) {
            Openable openable = items.get(0).getOpenable();
            SWF swf = null;
            if (openable instanceof SWF) {
                swf = (SWF) openable;
            }
            if (swf != null) {
                String val = "";
                boolean valid;
                int characterId = -1;
                do {
                    val = ViewMessages.showInputDialog(MainPanel.this, translate("message.input.gotoCharacter"), translate("message.input.gotoCharacter.title"), val);
                    if (val == null) {
                        break;
                    }
                    try {
                        characterId = Integer.parseInt(val);
                    } catch (NumberFormatException nfe) {
                        characterId = -1;
                    }
                } while (characterId <= 0);

                if (characterId > 0) {
                    CharacterTag tag = swf.getCharacter(characterId);
                    if (tag == null) {
                        ViewMessages.showMessageDialog(MainPanel.this, translate("message.character.notfound").replace("%characterid%", "" + characterId), translate("error"), JOptionPane.ERROR_MESSAGE);
                    } else {
                        setTagTreeSelectedNode(getCurrentTree(), tag);
                    }
                }
            }
            return;
        }

        if ((e.getKeyCode() == 'F') && (e.isControlDown())) {
            AbstractTagTree tree = getCurrentTree();
            if (tree == tagTree) {
                quickTreeFindPanel.setVisible(true);
            }
            if (tree == tagListTree) {
                quickTagListFindPanel.setVisible(true);
            }
        }
        if ((e.getKeyCode() == KeyEvent.VK_DELETE) && !e.isControlDown() && !e.isAltDown()) {
            if (contextPopupMenu.canRemove(items)) {
                contextPopupMenu.update(items);
                contextPopupMenu.removeItemActionPerformed(null, e.isShiftDown());
            }
        }
        if ((e.getKeyCode() == 'C' || e.getKeyCode() == 'X') && (e.isControlDown())) {
            List<TreeItem> tagItems = new ArrayList<>();
            List<TreeItem> frameItems = new ArrayList<>();
            Timelined frameTimelined = null;
            for (TreeItem item : items) {
                if (item instanceof TagScript) {
                    tagItems.add(((TagScript) item).getTag());
                } else if (item instanceof Tag) {
                    tagItems.add((Tag) item);
                }
                if (item instanceof Frame) {
                    frameItems.add(item);
                    Frame frame = (Frame) item;
                    if (frameTimelined != null && frameTimelined != frame.timeline.timelined) {
                        tagItems.clear();
                        frameItems.clear();
                        break;
                    }
                    frameTimelined = frame.timeline.timelined;
                }
            }
            boolean allWritable = true;
            for (TreeItem item : tagItems) {
                if (((Tag) item).isReadOnly()) {
                    allWritable = false;
                    break;
                }
            }

            if (e.getKeyCode() == 'C') {
                if (!frameItems.isEmpty()) {
                    contextPopupMenu.copyTagOrFrameToClipboardActionPerformed(null, frameItems);
                } else {
                    if (e.isShiftDown()) {
                        contextPopupMenu.copyTagToClipboardWithDependenciesActionPerformed(null, tagItems);
                    } else {
                        contextPopupMenu.copyTagOrFrameToClipboardActionPerformed(null, tagItems);
                    }
                }
            }
            if (e.getKeyCode() == 'X' && allWritable) {
                if (!frameItems.isEmpty()) {
                    contextPopupMenu.update(frameItems);
                    contextPopupMenu.cutTagOrFrameToClipboardActionPerformed(null);
                } else {
                    contextPopupMenu.update(tagItems);
                    if (e.isShiftDown()) {
                        contextPopupMenu.cutTagToClipboardWithDependenciesActionPerformed(null);
                    } else {
                        contextPopupMenu.cutTagOrFrameToClipboardActionPerformed(null);
                    }
                }
            }
            repaintTree();
        }
        if (e.getKeyCode() == 'V' && e.isControlDown()) {
            if (items.size() > 1) {
                return;
            }
            TreeItem firstItem = items.get(0);
            if (getClipboardType() == ClipboardType.FRAME) {
                if (!(firstItem instanceof Frame)) {
                    return;
                }
                if (firstItem.getOpenable() != getClipboardContents().iterator().next().getOpenable()) {
                    return;
                }
            } else {
                if (!((firstItem instanceof Tag) || (firstItem instanceof Frame))) {
                    return;
                }
            }
            contextPopupMenu.update(items);
            if (e.isShiftDown()) {
                contextPopupMenu.pasteAfterActionPerformed(null);
            } else {
                contextPopupMenu.pasteBeforeActionPerformed(null);
            }
        }
    }

    public void gcClipboard() {
        for (int i = orderedClipboard.size() - 1; i >= 0; i--) {
            WeakReference<TreeItem> ref = orderedClipboard.get(i);
            TreeItem item = ref.get();
            if (item != null) {
                if (item.getOpenable() == null) {
                    orderedClipboard.remove(i);
                    clipboard.remove(item);
                }
            }
        }
        resourcesClipboardPanel.update();
        tagListClipboardPanel.update();
    }

    public void emptyClipboard() {
        copyToClipboard(new ArrayList<>());
    }

    public void copyToClipboard(Collection<TreeItem> items) {

        orderedClipboard.clear();
        clipboard.clear();
        for (TreeItem item : items) {
            orderedClipboard.add(new WeakReference<>(item));
            clipboard.put(item, true);
        }
        clipboardCut = false;
        resourcesClipboardPanel.update();
        tagListClipboardPanel.update();
        resourcesClipboardPanel.flash();
        tagListClipboardPanel.flash();
        folderPreviewPanel.repaint();

    }

    public void cutToClipboard(Collection<TreeItem> items) {
        copyToClipboard(items);
        clipboardCut = true;
    }

    public boolean clipboardContains(TreeItem item) {
        return clipboard.containsKey(item);
    }

    public boolean clipboardEmpty() {
        return clipboard.isEmpty();
    }

    public ClipboardType getClipboardType() {
        if (clipboard.isEmpty()) {
            return ClipboardType.NONE;
        }
        TreeItem item = clipboard.keySet().iterator().next();
        if (item instanceof Frame) {
            return ClipboardType.FRAME;
        }
        return ClipboardType.TAG;
    }

    public int getClipboardSize() {
        return clipboard.size();
    }

    public Set<TreeItem> getClipboardContents() {
        Set<TreeItem> ret = new LinkedHashSet<>();
        for (WeakReference<TreeItem> ref : orderedClipboard) {
            TreeItem item = ref.get();
            if (item != null) {
                ret.add(item);
            }
        }
        return ret;
    }

    public boolean isClipboardCut() {
        return clipboardCut;
    }

    public boolean checkEdited() {
        if (abcPanel != null && abcPanel.isEditing()) {
            abcPanel.tryAutoSave();
        }

        if (actionPanel != null && actionPanel.isEditing()) {
            actionPanel.tryAutoSave();
        }

        if (previewPanel.isEditing()) {
            previewPanel.tryAutoSave();
        }

        if (headerPanel.isEditing()) {
            headerPanel.tryAutoSave();
        }

        return (abcPanel != null && abcPanel.isEditing())
                || (actionPanel != null && actionPanel.isEditing())
                || previewPanel.isEditing() || headerPanel.isEditing();
    }

    private class MyTreeSelectionModel extends DefaultTreeSelectionModel {

        @Override
        public void addSelectionPath(TreePath path) {
            if (checkEdited()) {
                return;
            }

            super.addSelectionPath(path);
        }

        @Override
        public void addSelectionPaths(TreePath[] paths) {
            if (checkEdited()) {
                return;
            }

            super.addSelectionPaths(paths);
        }

        @Override
        public void setSelectionPath(TreePath path) {
            if (checkEdited()) {
                return;
            }

            super.setSelectionPath(path);
        }

        @Override
        public void setSelectionPaths(TreePath[] pPaths) {
            if (checkEdited()) {
                return;
            }

            super.setSelectionPaths(pPaths);
        }

        @Override
        public void clearSelection() {
            if (checkEdited()) {
                return;
            }

            super.clearSelection();
        }

        public void setSelection(TreePath[] selection) {
            if (checkEdited()) {
                return;
            }

            this.selection = selection;
        }

        @Override
        public void removeSelectionPath(TreePath path) {
            if (checkEdited()) {
                return;
            }

            super.removeSelectionPath(path);
        }

        @Override
        public void removeSelectionPaths(TreePath[] paths) {
            if (checkEdited()) {
                return;
            }

            super.removeSelectionPaths(paths);
        }
    }

    public TagTreeContextMenu getContextPopupMenu() {
        return contextPopupMenu;
    }

    public void setPercent(int percent) {
        View.checkAccess();

        progressBar.setValue(percent);
        progressBar.setVisible(true);
    }

    public void hidePercent() {
        View.checkAccess();

        if (progressBar.isVisible()) {
            progressBar.setVisible(false);
        }
    }

    public MainFrame getMainFrame() {
        return mainFrame;
    }

    static {
        try {
            File.createTempFile("temp", ".swf").delete(); //First call to this is slow, so make it first
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    public void updateMenu() {
        mainMenu.updateComponents();
    }

    private static void addTab(JTabbedPane tabbedPane, Component tab, String title, Icon icon) {
        tabbedPane.add(tab);

        JLabel lbl = new JLabel(title);
        lbl.setIcon(icon);
        lbl.setIconTextGap(5);
        lbl.setHorizontalTextPosition(SwingConstants.RIGHT);

        tabbedPane.setTabComponentAt(tabbedPane.getTabCount() - 1, lbl);
    }

    public void setStatus(String s) {
        statusPanel.setStatus(s);
    }

    public void setEditingStatus() {
        statusPanel.setStatus(translate(Configuration.autoSaveTagModifications.get() ? "status.editing.autosave" : "status.editing"));
    }

    public void clearEditingStatus() {
        statusPanel.setStatus("");
    }

    public void setWorkStatus(String s, CancellableWorker worker) {
        statusPanel.setWorkStatus(s, worker);
        mainMenu.updateComponents();
    }

    public void setWorkStatusHidden(String s, CancellableWorker worker) {
        statusPanel.setWorkStatusHidden(s, worker);
    }

    public void showOldStatus() {
        statusPanel.showOldStatus();
        mainMenu.updateComponents();
    }

    public CancellableWorker getCurrentWorker() {
        return statusPanel.getCurrentWorker();
    }

    private JPanel createWelcomePanel() {
        JPanel welcomePanel = new JPanel();
        welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));
        JLabel welcomeToLabel = new JLabel(translate("startup.welcometo"));
        welcomeToLabel.setFont(welcomeToLabel.getFont().deriveFont(40));
        welcomeToLabel.setAlignmentX(0.5f);
        JPanel appNamePanel = new JPanel(new FlowLayout());
        JLabel jpLabel = new JLabel("JPEXS ");
        jpLabel.setAlignmentX(0.5f);
        jpLabel.setForeground(new Color(0, 0, 160));
        jpLabel.setFont(new Font("Tahoma", Font.BOLD, 50));
        jpLabel.setHorizontalAlignment(SwingConstants.CENTER);
        appNamePanel.add(jpLabel);

        JLabel ffLabel = new JLabel("Free Flash ");
        ffLabel.setAlignmentX(0.5f);
        ffLabel.setFont(new Font("Tahoma", Font.BOLD, 50));
        ffLabel.setHorizontalAlignment(SwingConstants.CENTER);
        appNamePanel.add(ffLabel);

        JLabel decLabel = new JLabel("Decompiler");
        decLabel.setAlignmentX(0.5f);
        decLabel.setForeground(Color.red);
        decLabel.setFont(new Font("Tahoma", Font.BOLD, 50));
        decLabel.setHorizontalAlignment(SwingConstants.CENTER);
        appNamePanel.add(decLabel);
        appNamePanel.setAlignmentX(0.5f);
        welcomePanel.add(Box.createGlue());
        welcomePanel.add(welcomeToLabel);
        welcomePanel.add(appNamePanel);
        JLabel startLabel = new JLabel(translate("startup.selectopen"));
        startLabel.setAlignmentX(0.5f);
        startLabel.setFont(startLabel.getFont().deriveFont(30));
        welcomePanel.add(startLabel);
        welcomePanel.add(Box.createGlue());
        return welcomePanel;
    }

    private JPanel createFolderPreviewCard() {
        folderPreviewCard = new JPanel(new BorderLayout());
        folderPreviewPanel = new FolderPreviewPanel(this, new ArrayList<>());
        FasterScrollPane folderPreviewScrollPane = new FasterScrollPane(folderPreviewPanel);
        folderPreviewCard.add(folderPreviewScrollPane, BorderLayout.CENTER);

        folderPreviewPanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                handleKeyReleased(e);
            }

            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPressed(e);
            }
        });

        return folderPreviewCard;
    }

    private JPanel createFolderListCard() {
        JPanel folderListCard = new JPanel(new BorderLayout());
        folderListPanel = new FolderListPanel(this, new ArrayList<>());
        folderListCard.add(new FasterScrollPane(folderListPanel), BorderLayout.CENTER);

        folderListPanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                handleKeyReleased(e);
            }

            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPressed(e);
            }
        });

        return folderListCard;
    }

    private JPanel createDumpPreviewCard() {
        JPanel dumpViewCard = new JPanel(new BorderLayout());
        dumpViewPanel = new DumpViewPanel(dumpTree);
        dumpViewCard.add(new FasterScrollPane(dumpViewPanel), BorderLayout.CENTER);

        return dumpViewCard;
    }

    public String translate(String key) {
        if (mainFrame == null) {
            return "";
        }
        return mainFrame.translate(key);
    }

    public MainPanel(MainFrame mainFrame, MainFrameMenu mainMenu) {
        super();

        this.mainFrame = mainFrame;
        this.mainMenu = mainMenu;

        mainFrame.setTitle(ApplicationInfo.applicationVerName);

        setLayout(new BorderLayout());
        openables = new ObservableList<>();

        detailPanel = new JTabbedPane();
        //detailPanel.setLayout(new CardLayout());

        /*JPanel whitePanel = new JPanel();
        if (View.isOceanic()) {
            whitePanel.setBackground(Color.white);
        }
        detailPanel.add(whitePanel, DETAILCARDEMPTYPANEL);
         */
        tagInfoPanel = new TagInfoPanel(this);

        debugStackPanel = new DebugStackPanel();
        Main.getDebugHandler().addBreakListener(new DebuggerHandler.BreakListener() {
            @Override
            public void breakAt(String scriptName, int line, int classIndex, int traitIndex, int methodIndex) {
                View.execInEventDispatchLater(new Runnable() {
                    @Override
                    public void run() {
                        showDetail(DETAILCARDDEBUGSTACKFRAME);
                    }
                });
            }

            @Override
            public void doContinue() {
                View.execInEventDispatchLater(new Runnable() {
                    @Override
                    public void run() {
                        showDetail(DETAILCARDEMPTYPANEL);
                    }
                });
            }
        });
        Main.getDebugHandler().addConnectionListener(new DebuggerHandler.ConnectionListener() {
            @Override
            public void connected() {

            }

            @Override
            public void disconnected() {
                View.execInEventDispatchLater(new Runnable() {
                    @Override
                    public void run() {
                        reload(true);
                    }
                });
            }
        });

        UIManager.getDefaults().put("TreeUI", BasicTreeUI.class.getName());
        tagTree = new TagTree(null, this);
        tagTree.addTreeSelectionListener(this);
        tagTree.setSelectionModel(new MyTreeSelectionModel());

        tagListTree = new TagListTree(null, this);
        tagListTree.addTreeSelectionListener(this);
        tagListTree.setSelectionModel(new MyTreeSelectionModel());

        DragSource dragSource = DragSource.getDefaultDragSource();
        dragSource.createDefaultDragGestureRecognizer(tagTree, DnDConstants.ACTION_COPY_OR_MOVE, new DragGestureListener() {
            @Override
            public void dragGestureRecognized(DragGestureEvent dge) {
                dge.startDrag(DragSource.DefaultCopyDrop, new Transferable() {
                    @Override
                    public DataFlavor[] getTransferDataFlavors() {
                        return new DataFlavor[]{DataFlavor.javaFileListFlavor};
                    }

                    @Override
                    public boolean isDataFlavorSupported(DataFlavor flavor) {
                        return flavor.equals(DataFlavor.javaFileListFlavor);
                    }

                    @Override
                    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
                        if (flavor.equals(DataFlavor.javaFileListFlavor)) {
                            List<File> files;
                            String tempDir = System.getProperty("java.io.tmpdir");
                            if (!tempDir.endsWith(File.separator)) {
                                tempDir += File.separator;
                            }
                            Random rnd = new Random();
                            tempDir += "ffdec" + File.separator + "export" + File.separator + System.currentTimeMillis() + "_" + rnd.nextInt(1000);
                            File fTempDir = new File(tempDir);
                            Path.createDirectorySafe(fTempDir);

                            File ftemp = new File(tempDir);
                            ExportDialog exd = new ExportDialog(Main.getDefaultDialogsOwner(), null);
                            try {
                                files = exportSelection(null, new GuiAbortRetryIgnoreHandler(), tempDir, exd);
                            } catch (InterruptedException ex) {
                                logger.log(Level.SEVERE, null, ex);
                                return null;
                            }

                            files.clear();

                            File[] fs = ftemp.listFiles();
                            files.addAll(Arrays.asList(fs));

                            Main.stopWork();

                            for (File f : files) {
                                f.deleteOnExit();
                            }
                            new File(tempDir).deleteOnExit();
                            return files;

                        }
                        return null;
                    }
                }, new DragSourceListener() {
                    @Override
                    public void dragEnter(DragSourceDragEvent dsde) {
                        enableDrop(false);
                    }

                    @Override
                    public void dragOver(DragSourceDragEvent dsde) {
                    }

                    @Override
                    public void dropActionChanged(DragSourceDragEvent dsde) {
                    }

                    @Override
                    public void dragExit(DragSourceEvent dse) {
                    }

                    @Override
                    public void dragDropEnd(DragSourceDropEvent dsde) {
                        enableDrop(true);
                    }
                });
            }
        });

        List<AbstractTagTree> trees = new ArrayList<>();
        trees.add(tagTree);
        trees.add(tagListTree);

        contextPopupMenu = new TagTreeContextMenu(trees, this);

        dumpTree = new DumpTree(null, this);
        dumpTree.addTreeSelectionListener(this);
        dumpTree.createContextMenu();

        currentView = Configuration.lastView.get();

        statusPanel = new MainFrameStatusPanel(this);
        add(statusPanel, BorderLayout.SOUTH);

        displayPanel = new JPanel(new CardLayout());

        DefaultSyntaxKit.initKit();
        previewPanel = new PreviewPanel(this);

        dumpPreviewPanel = new PreviewPanel(this);
        dumpPreviewPanel.setReadOnly(true);

        displayPanel.add(previewPanel, CARDPREVIEWPANEL);
        displayPanel.add(createFolderPreviewCard(), CARDFOLDERPREVIEWPANEL);
        displayPanel.add(createFolderListCard(), CARDFOLDERLISTPANEL);
        displayPanel.add(createDumpPreviewCard(), CARDDUMPVIEW);

        headerPanel = new HeaderInfoPanel(this);
        displayPanel.add(headerPanel, CARDHEADER);

        displayPanel.add(new JPanel(), CARDEMPTYPANEL);
        showCard(CARDEMPTYPANEL);

        LazyCardLayout treePanelLayout = new LazyCardLayout();
        treePanelLayout.registerLayout(createResourcesViewCard(), RESOURCES_VIEW);
        treePanelLayout.registerLayout(createDumpViewCard(), DUMP_VIEW);
        treePanelLayout.registerLayout(createTagListViewCard(), TAGLIST_VIEW);
        treePanel = new JPanel(treePanelLayout);

        //treePanel.add(searchPanel, BorderLayout.SOUTH);
        //searchPanel.setVisible(false);
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(displayPanel, BorderLayout.CENTER);
        pinsPanel = new PinsPanel(this);
        pinsPanel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                TreeItem item = pinsPanel.getCurrent();
                if ((getCurrentTree() == tagListTree) && (item instanceof TagScript)) {
                    item = ((TagScript) item).getTag();
                }
                setTagTreeSelectedNode(getCurrentTree(), item);
            }
        });
        rightPanel.add(pinsPanel, BorderLayout.NORTH);

        //displayPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        splitPane2 = new JPersistentSplitPane(JSplitPane.VERTICAL_SPLIT, treePanel, detailPanel, Configuration.guiSplitPane2DividerLocationPercent);
        splitPane1 = new JPersistentSplitPane(JSplitPane.HORIZONTAL_SPLIT, splitPane2, rightPanel, Configuration.guiSplitPane1DividerLocationPercent);

        welcomePanel = createWelcomePanel();
        add(welcomePanel, BorderLayout.CENTER);

        easyPanel = new EasyPanel(this);

        contentPanel = new JPanel(new CardLayout());
        contentPanel.add(welcomePanel, WELCOME_PANEL);
        contentPanel.add(splitPane1, SPLIT_PANE1);
        contentPanel.add(easyPanel, EASY_PANEL);
        add(contentPanel);
        showContentPanelCard(WELCOME_PANEL);

        tagTree.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                handleKeyReleased(e);
            }

            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPressed(e);
            }
        });
        tagListTree.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                handleKeyReleased(e);
            }

            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPressed(e);
            }
        });
        detailPanel.setVisible(false);

        scrollPosStorage = new ScrollPosStorage(this);

        updateUi();

        this.openables.addCollectionChangedListener((e) -> {
            AbstractTagTreeModel ttm = tagTree.getFullModel();
            if (ttm != null) {
                if (getCurrentSwf() == null) {
                    tagTree.clearSelection();
                }
                ttm.updateSwfs(e);
                tagTree.expandRoot();
                if (Configuration.expandFirstLevelOfTreeOnLoad.get()) {
                    if (e.getAction() == CollectionChangedAction.RESET) {
                        tagTree.expandFirstLevelNodes();
                    } else if (e.getAction() == CollectionChangedAction.ADD) {
                        OpenableList list = e.getNewItem();
                        if (!list.isBundle() && list.items.size() == 1) {
                            tagTree.expandPath(tagTree.getFullModel().getTreePath(list.get(0)));
                        } else {
                            tagTree.expandPath(tagTree.getFullModel().getTreePath(list));
                        }
                    }
                }
            }
            ttm = tagListTree.getFullModel();
            if (ttm != null) {
                if (getCurrentSwf() == null) {
                    tagListTree.clearSelection();
                }
                ttm.updateSwfs(e);
                tagListTree.expandRoot();

                if (Configuration.expandFirstLevelOfTreeOnLoad.get()) {
                    if (e.getAction() == CollectionChangedAction.RESET) {
                        tagListTree.expandFirstLevelNodes();
                    } else if (e.getAction() == CollectionChangedAction.ADD) {
                        OpenableList list = e.getNewItem();
                        if (!list.isBundle() && list.items.size() == 1) {
                            tagListTree.expandPath(tagListTree.getFullModel().getTreePath(list.get(0)));
                        } else {
                            tagListTree.expandPath(tagListTree.getFullModel().getTreePath(list));
                        }
                    }
                }
            }

            DumpTreeModel dtm = dumpTree.getModel();
            if (dtm != null) {
                List<List<String>> expandedNodes = View.getExpandedNodes(dumpTree);
                dtm.updateSwfs();
                View.expandTreeNodes(dumpTree, expandedNodes);
                dumpTree.expandRoot();
                if (Configuration.expandFirstLevelOfTreeOnLoad.get()) {
                    if (e.getAction() == CollectionChangedAction.RESET) {
                        dumpTree.expandFirstLevelNodes();
                    } else if (e.getAction() == CollectionChangedAction.ADD) {
                        OpenableList list = e.getNewItem();
                        for (Openable dopenable : list) {
                            if (dopenable instanceof SWF) {
                                dumpTree.expandSwfNode((SWF) dopenable);
                            }
                        }
                    }
                }
            }

            if (openables.isEmpty()) {
                tagTree.setUI(new BasicTreeUI() {
                    {
                        setHashColor(Color.gray);
                    }
                });
                dumpTree.setUI(new BasicTreeUI() {
                    {
                        setHashColor(Color.gray);
                    }
                });
                tagListTree.setUI(new BasicTreeUI() {
                    {
                        setHashColor(Color.gray);
                    }
                });
            }
        });

        //Opening files with drag&drop to main window
        enableDrop(true);
        calculateMissingNeededThread = new CalculateMissingNeededThread();
        calculateMissingNeededThread.start();
        pinsPanel.load();

        Configuration.flattenASPackages.addListener(new ConfigurationItemChangeListener<Boolean>() {
            @Override
            public void configurationItemChanged(Boolean newValue) {
                resetAllTimelines();
                refreshTree();
            }
        });
    }

    public void resetAllTimelines() {
        List<OpenableList> openableLists = new ArrayList<>(openables);
        List<SWF> allSwfs = new ArrayList<>();
        for (OpenableList openableList : openableLists) {
            for (Openable openable : openableList) {
                if (openable instanceof SWF) {
                    allSwfs.add((SWF) openable);
                }
            }
            for (Openable openable : openableList) {
                if (openable instanceof SWF) {
                    Main.populateSwfs((SWF) openable, allSwfs);
                }
            }
        }
        for (SWF swf : allSwfs) {
            swf.resetTimeline();
        }
    }

    public void loadSwfAtPos(OpenableList newSwfs, int index) {
        View.checkAccess();

        OpenableList oldSwfList = openables.get(index);

        List<SWF> allSwfs = new ArrayList<>();
        for (Openable o : oldSwfList.items) {
            if (o instanceof SWF) {
                allSwfs.add((SWF) o);
                Main.populateSwfs((SWF) o, allSwfs);
            }
        }

        List<List<String>> expandedNodes = View.getExpandedNodes(tagTree);
        previewPanel.clear();
        openables.set(index, newSwfs);

        for (SWF s : allSwfs) {
            s.clearTagSwfs();
            Main.searchResultsStorage.destroySwf(s);
        }
        Openable openable = newSwfs.size() > 0 ? newSwfs.get(0) : null;

        easyPanel.setSwfs(new ArrayList<>(getAllSwfs()));
        if (openable instanceof SWF) {
            easyPanel.setSwf((SWF) openable);
        }
        if (openable != null) {
            updateUi(openable);
        }

        gcClipboard();
        reload(false);
        View.expandTreeNodes(tagTree, expandedNodes);
        doFilter();
        pinsPanel.load();
    }

    public void load(OpenableList newOpenables, boolean first) {
        View.checkAccess();

        List<List<String>> expandedNodes = View.getExpandedNodes(getCurrentTree());
        previewPanel.clear();

        openables.add(newOpenables);

        easyPanel.setSwfs(new ArrayList<>(getAllSwfs()));

        Openable openable = newOpenables.size() > 0 ? newOpenables.get(0) : null;
        if (openable != null) {
            updateUi(openable);
        }

        gcClipboard();

        reload(false);
        View.expandTreeNodes(getCurrentTree(), expandedNodes);
        doFilter();
        pinsPanel.load();
    }

    public ABCPanel getABCPanel() {
        if (abcPanel == null) {
            abcPanel = new ABCPanel(this);
            displayPanel.add(abcPanel, CARDACTIONSCRIPT3PANEL);
        }

        return abcPanel;
    }

    public ActionPanel getActionPanel() {
        if (actionPanel == null) {
            actionPanel = new ActionPanel(MainPanel.this);
            displayPanel.add(actionPanel, CARDACTIONSCRIPTPANEL);
        }

        return actionPanel;
    }

    public void updateUiWithCurrentOpenable() {
        switch (currentView) {
            case VIEW_RESOURCES:
                TreeItem resourcesTi = (TreeItem) tagTree.getLastSelectedPathComponent();
                if (resourcesTi != null) {
                    Openable resourcesOpenable = resourcesTi.getOpenable();
                    if (resourcesOpenable != null) {
                        updateUi(resourcesOpenable);
                    }
                }
                break;
            case VIEW_TAGLIST:
                TreeItem tagListTi = (TreeItem) tagListTree.getLastSelectedPathComponent();
                if (tagListTi != null) {
                    Openable tagListOpenable = tagListTi.getOpenable();
                    if (tagListOpenable != null) {
                        updateUi(tagListOpenable);
                    }
                }
                break;
            case VIEW_DUMP:
                DumpInfo di = (DumpInfo) dumpTree.getLastSelectedPathComponent();
                if (di != null) {
                    Openable dumpOpenable = di.getOpenable();
                    if (dumpOpenable != null) {
                        updateUi(dumpOpenable);
                    }
                }
                break;
            case VIEW_EASY:
                updateUi(easyPanel.getSwf());
                break;
        }
    }

    private void updateUi(final Openable openable) {
        View.checkAccess();

        if (isWelcomeScreen) {
            if (currentView == VIEW_EASY) {
                showContentPanelCard(EASY_PANEL);
            } else {
                showContentPanelCard(SPLIT_PANE1);
            }
            isWelcomeScreen = false;
        }
        SWF swf = null;
        if (openable instanceof SWF) {
            swf = (SWF) openable;
            List<ABCContainerTag> abcList = swf.getAbcList();

            boolean hasAbc = !abcList.isEmpty();

            if (hasAbc) {
                boolean abcFound = false;
                for (ABCContainerTag c : abcList) {
                    if (getABCPanel().abc == c.getABC()) {
                        abcFound = true;
                        break;
                    }
                }
                if (!abcFound) {
                    getABCPanel().setAbc(abcList.get(0).getABC());
                }
            }
        }
        mainMenu.updateComponents(openable);

        if (taskThread != null) {
            taskThread.interrupt();
        }

        if (Configuration._debugMode.get() && swf != null) {
            final SWF fSwf = swf;
            Thread t = new Thread() {
                @Override
                public void run() {
                    while (!Thread.currentThread().isInterrupted()) {
                        DecompilerPool d = fSwf.getDecompilerPool();
                        statusPanel.setStatus(fSwf.getFileTitle() + " " + d.getStat());

                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ex) {
                            break;
                        }
                    }
                }
            };

            t.start();
            taskThread = t;
        }
    }

    private void updateUi() {
        View.checkAccess();

        if (!isWelcomeScreen && openables.isEmpty()) {
            showContentPanelCard(WELCOME_PANEL);
            isWelcomeScreen = true;
            quickTagListFindPanel.setVisible(false);
            quickTreeFindPanel.setVisible(false);
            doFilter();
        }

        mainFrame.setTitle(ApplicationInfo.applicationVerName);
        mainMenu.updateComponents(null);

        showView(getCurrentView());
    }

    private boolean closeConfirmation(OpenableList swfList) {
        View.checkAccess();

        String message = swfList == null
                ? translate("message.confirm.closeAll")
                : translate("message.confirm.close").replace("{swfName}", swfList.toString());

        return ViewMessages.showConfirmDialog(this, message, translate("message.warning"), JOptionPane.OK_CANCEL_OPTION, Configuration.showCloseConfirmation, JOptionPane.OK_OPTION) == JOptionPane.OK_OPTION;
    }

    public boolean isModified() {
        for (OpenableList openableList : openables) {
            for (Openable openable : openableList) {
                if (openable.isModified()) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean closeAll(boolean showCloseConfirmation, boolean onExit) {
        View.checkAccess();

        if (showCloseConfirmation && isModified()) {
            boolean closeConfirmResult = closeConfirmation(openables.size() == 1 ? openables.get(0) : null);
            if (!closeConfirmResult) {
                return false;
            }
        }

        scrollPosStorage.saveScrollPos(getCurrentTree().getCurrentTreeItem());

        clearPins();

        List<OpenableList> swfsLists = new ArrayList<>(openables);

        for (SearchResultsDialog sr : searchResultsDialogs) {
            sr.setVisible(false);
        }
        searchResultsDialogs.clear();

        openables.clear();
        oldItem = null;
        clear();
        updateUi();

        List<SWF> swfsToClose = new ArrayList<>();
        for (OpenableList openableList : swfsLists) {
            for (Openable openable : openableList) {
                if (openable instanceof SWF) {
                    swfsToClose.add((SWF) openable);
                } else {
                    ABCExplorerDialog abcExportDialog = abcExplorerDialogs.get(openable);
                    if (abcExportDialog != null) {
                        abcExportDialog.setVisible(false);
                        abcExplorerDialogs.remove(openable);
                    }
                }
            }
            for (Openable openable : openableList) {
                if (openable instanceof SWF) {
                    Main.populateSwfs((SWF) openable, swfsToClose);
                }
            }
        }

        for (SWF swf : swfsToClose) {
            swf.clearTagSwfs();
            saveBreakpoints(swf);
            ABCExplorerDialog abcExportDialog = abcExplorerDialogs.get(swf);
            if (abcExportDialog != null) {
                abcExportDialog.setVisible(false);
                abcExplorerDialogs.remove(swf);
            }
            BreakpointListDialog breakpointsListDialog = breakpointsListDialogs.get(swf);
            if (breakpointsListDialog != null) {
                breakpointsListDialog.setVisible(false);
                breakpointsListDialogs.remove(swf);
            }
            if (!onExit) {
                SwfSpecificCustomConfiguration cc = Configuration.getSwfSpecificCustomConfiguration(swf.getShortPathTitle());
                if (cc != null) {
                    cc.setCustomData(CustomConfigurationKeys.KEY_LOADED_IMPORT_ASSETS, "");
                }
            }
        }

        refreshTree();

        gcClipboard();
        mainMenu.updateComponents(null);
        previewPanel.clear();

        return true;
    }

    public boolean close(OpenableList openableList) {
        View.checkAccess();

        boolean modified = false;
        for (Openable openable : openableList) {
            if (openable.isModified()) {
                modified = true;
            }
        }

        if (modified) {
            boolean closeConfirmResult = closeConfirmation(openableList);
            if (!closeConfirmResult) {
                return false;
            }
        }

        List<SWF> swfsToClose = new ArrayList<>();
        for (Openable openable : openableList) {
            if (openable instanceof SWF) {
                swfsToClose.add((SWF) openable);
            } else {
                ABCExplorerDialog abcExportDialog = abcExplorerDialogs.get(openable);
                if (abcExportDialog != null) {
                    abcExportDialog.setVisible(false);
                }
            }
        }
        for (Openable openable : openableList) {
            if (openable instanceof SWF) {
                Main.populateSwfs((SWF) openable, swfsToClose);
            }
        }

        for (int i = 0; i < searchResultsDialogs.size(); i++) {
            SearchResultsDialog sr = searchResultsDialogs.get(i);
            for (SWF swf : swfsToClose) {
                sr.removeSwf(swf);
            }
            if (sr.isEmpty()) {
                sr.setVisible(false);
                searchResultsDialogs.remove(i);
                i--;
            }
        }
        for (SWF swf : swfsToClose) {
            Main.searchResultsStorage.destroySwf(swf);
            pinsPanel.removeOpenable(swf);
            SwfSpecificCustomConfiguration cc = Configuration.getSwfSpecificCustomConfiguration(swf.getShortPathTitle());
            if (cc != null) {
                cc.setCustomData(CustomConfigurationKeys.KEY_LOADED_IMPORT_ASSETS, "");
            }

            saveBreakpoints(swf);

            ABCExplorerDialog abcExportDialog = abcExplorerDialogs.get(swf);
            if (abcExportDialog != null) {
                abcExportDialog.setVisible(false);
            }

            BreakpointListDialog breakpointsListDialog = breakpointsListDialogs.get(swf);
            if (breakpointsListDialog != null) {
                breakpointsListDialog.setVisible(false);
                breakpointsListDialogs.remove(swf);
            }
        }

        openables.remove(openableList);
        oldItem = null;
        clear();
        updateUi();

        for (SWF swf : swfsToClose) {
            swf.clearTagSwfs();
        }

        refreshTree();

        gcClipboard();

        mainMenu.updateComponents(null);
        previewPanel.clear();
        dumpPreviewPanel.clear();
        doFilter();
        return true;
    }

    private void enableDrop(boolean value) {
        if (value) {
            setDropTarget(new DropTarget() {
                @Override
                public synchronized void drop(DropTargetDropEvent dtde) {
                    try {
                        dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                        @SuppressWarnings("unchecked")
                        List<File> droppedFiles = (List<File>) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                        if (droppedFiles != null && !droppedFiles.isEmpty()) {
                            OpenableSourceInfo[] sourceInfos = new OpenableSourceInfo[droppedFiles.size()];
                            for (int i = 0; i < droppedFiles.size(); i++) {
                                sourceInfos[i] = new OpenableSourceInfo(null, droppedFiles.get(i).getAbsolutePath(), null);
                            }
                            Main.openFile(sourceInfos, null);
                        }
                    } catch (UnsupportedFlavorException | IOException ex) {
                        //ignored
                    }
                }
            });
        } else {
            setDropTarget(null);
        }
    }

    public void updateClassesList() {
        String selectionPath = getCurrentTree().getSelectionPathString();
        List<TreeItem> nodes = getASTreeNodes(tagTree);
        boolean updateNeeded = false;
        for (TreeItem n : nodes) {
            if (n instanceof ClassesListTreeModel) {
                ((ClassesListTreeModel) n).update();
                updateNeeded = true;
            }
        }

        refreshTree();

        if (updateNeeded) {
            tagTree.updateUI();
        }
        TreePath tp = getCurrentTree().getTreePathFromString(selectionPath);
        if (tp != null) {
            getCurrentTree().setSelectionPath(tp);
        }
        refreshPinnedScriptPacks();
    }

    private boolean isFilterEmpty(String filter) {
        return filter.trim().length() < 3;
    }

    private void doFilter(AbstractTagTree tree, QuickTreeFilterInterface findPanel, List<List<String>> unfilteredExpandedNodes) {
        TreeModel model = tree.getModel();
        String oldFilter = "";
        List<String> oldFoldersFilter = new ArrayList<>();
        if (model instanceof FilteredTreeModel) {
            oldFilter = ((FilteredTreeModel) model).getFilter();
            oldFoldersFilter = ((FilteredTreeModel) model).getFoldersFilter();
        }
        String newFilter = findPanel.getFilter();
        List<String> newFoldersFilter = findPanel.getFolders();

        if (isFilterEmpty(oldFilter) && oldFoldersFilter.isEmpty()) {
            unfilteredExpandedNodes.clear();
            unfilteredExpandedNodes.addAll(View.getExpandedNodes(tree));
        }

        if (oldFilter.trim().equals(newFilter.trim())
                && oldFoldersFilter.equals(newFoldersFilter)) {
            return;
        }

        TreePath[] selectionPaths = tree.getSelectionPaths();
        tree.setModel(new FilteredTreeModel(newFilter, newFoldersFilter, tree.getFullModel(), tree));
        if (!isFilterEmpty(newFilter)) {
            for (int i = 0; i < tree.getRowCount(); i++) {
                tree.expandRow(i);
            }
        } else if (!newFoldersFilter.isEmpty()) {
            View.expandTreeNodes(tree, unfilteredExpandedNodes);
        } else {
            tree.setModel(tree.getFullModel());
            View.expandTreeNodes(tree, unfilteredExpandedNodes);
        }
        tree.setSelectionPaths(selectionPaths);
    }

    public void doFilter() {
        View.checkAccess();
        quickTreeFindPanel.updateFolders();
        doFilter(tagTree, quickTreeFindPanel, unfilteredTreeExpandedNodes);
        doFilter(tagListTree, quickTagListFindPanel, unfilteredTagListExpandedNodes);
    }

    public void renameIdentifier(SWF swf, String identifier) throws InterruptedException {
        String oldName = identifier;
        String newName = ViewMessages.showInputDialog(this, translate("rename.enternew"), oldName);
        if (newName != null) {
            if (!oldName.equals(newName)) {
                swf.renameAS2Identifier(oldName, newName);
                ViewMessages.showMessageDialog(this, translate("rename.finished.identifier"));
                updateClassesList();
                reload(true);
            }
        }
    }

    public void renameMultiname(List<ABCContainerTag> abcList, int multiNameIndex) {
        SWF swf = getABCPanel().getSwf();
        String oldName = "";
        AVM2ConstantPool constants = getABCPanel().abc.constants;
        if (constants.getMultiname(multiNameIndex).name_index > 0) {
            oldName = constants.getString(constants.getMultiname(multiNameIndex).name_index);
        }

        String scriptName = abcPanel.decompiledTextArea.getScriptLeaf().getClassPath().toString();

        String newName = ViewMessages.showInputDialog(this, translate("rename.enternew"), oldName);
        if (newName != null) {
            if (!oldName.equals(newName)) {

                if (oldName.equals(abcPanel.decompiledTextArea.getScriptLeaf().getClassPath().className)) {
                    scriptName = abcPanel.decompiledTextArea.getScriptLeaf().getClassPath().packageStr.add(newName, "").toPrintableString(true);
                }

                final String fScriptName = scriptName;

                int mulCount = 0;
                for (ABCContainerTag cnt : abcList) {
                    ABC abc = cnt.getABC();
                    for (int m = 1; m < abc.constants.getMultinameCount(); m++) {
                        int ni = abc.constants.getMultiname(m).name_index;
                        String n = "";
                        if (ni > 0) {
                            n = abc.constants.getString(ni);
                        }
                        if (n.equals(oldName)) {
                            abc.renameMultiname(m, newName);
                            mulCount++;
                        }
                    }
                }

                int fmulCount = mulCount;
                View.execInEventDispatch(() -> {
                    ViewMessages.showMessageDialog(this, translate("rename.finished.multiname").replace("%count%", Integer.toString(fmulCount)));
                    swf.clearScriptCache();
                    updateClassesList();
                    Main.stopWork();
                    abcPanel.hilightScript(abcPanel.getSwf(), fScriptName);
                });
            }
        }
    }

    public List<TreeItem> getASTreeNodes(TagTree tree) {
        List<TreeItem> result = new ArrayList<>();
        TagTreeModel tm = (TagTreeModel) tree.getFullModel();
        if (tm == null) {
            return result;
        }
        TreeItem root = tm.getRoot();
        for (int i = 0; i < tm.getChildCount(root); i++) {
            // first level node can be SWF and SWFBundle
            TreeItem node = tm.getChild(root, i);
            if (node instanceof Bundle) {
                for (int j = 0; j < tm.getChildCount(node); j++) {
                    // child of SWFBundle should be SWF
                    SWF swfNode = (SWF) tm.getChild(node, j);
                    result.add(tm.getScriptsNode(swfNode));
                }
            } else if (node instanceof SWF) {
                SWF swfNode = (SWF) tm.getChild(root, i);
                result.add(tm.getScriptsNode(swfNode));
            }
        }
        return result;
    }

    public boolean confirmExperimental() {
        View.checkAccess();

        return ViewMessages.showConfirmDialog(this, translate("message.confirm.experimental"), translate("message.warning"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION;
    }

    private List<TreeItem> getSelection(Openable openable, List<TreeItem> selection) {
        if (currentView == MainPanel.VIEW_RESOURCES) {
            return selection == null ? tagTree.getSelection(openable) : tagTree.getSelectionAndAllSubs(openable, selection);
        } else if (currentView == MainPanel.VIEW_TAGLIST) {
            return selection == null ? tagListTree.getSelection(openable) : tagListTree.getSelectionAndAllSubs(openable, selection);
        }
        return new ArrayList<>();
    }

    public List<File> exportSelection(List<TreeItem> selection, AbortRetryIgnoreHandler handler, String selFile, ExportDialog export) throws IOException, InterruptedException {

        List<File> ret = new ArrayList<>();
        List<TreeItem> sel = getSelection(null, selection);

        Set<Openable> usedOpenables = new HashSet<>();
        Set<OpenableList> usedOpenableLists = new HashSet<>();

        Map<OpenableList, Map<String, Integer>> usedSwfIdsInBundles = new HashMap<>();

        for (TreeItem d : sel) {
            Openable selectedNodeOpenable = d.getOpenable();
            usedOpenables.add(selectedNodeOpenable);

            OpenableList list = selectedNodeOpenable.getOpenableList();
            if (list != null) {
                usedOpenableLists.add(list);
            }
        }

        Map<String, Integer> usedSwfsIds = new HashMap<>();
        for (Openable openable : usedOpenables) {

            SWF swf = null;
            if (openable instanceof SWF) {
                swf = (SWF) openable;
            } else {
                swf = ((ABC) openable).getSwf();
            }
            List<ScriptPack> as3scripts = new ArrayList<>();
            List<Tag> images = new ArrayList<>();
            List<Tag> shapes = new ArrayList<>();
            List<Tag> morphshapes = new ArrayList<>();
            List<Tag> sprites = new ArrayList<>();
            List<Tag> buttons = new ArrayList<>();
            List<Tag> movies = new ArrayList<>();
            List<SoundTag> sounds = new ArrayList<>();
            List<Tag> texts = new ArrayList<>();
            List<TreeItem> as12scripts = new ArrayList<>();
            List<BinaryDataInterface> binaryData = new ArrayList<>();
            Map<Integer, List<Integer>> frames = new HashMap<>();
            List<Tag> fonts = new ArrayList<>();
            List<Tag> fonts4 = new ArrayList<>();
            List<Tag> symbolNames = new ArrayList<>();

            for (TreeItem d : sel) {
                Openable selectedNodeSwf = d.getOpenable();

                if (selectedNodeSwf != openable) {
                    continue;
                }

                if (d instanceof TagScript) {
                    Tag tag = ((TagScript) d).getTag();
                    if (tag instanceof DoActionTag || tag instanceof DoInitActionTag) {
                        as12scripts.add(d);
                    }
                }

                if (d instanceof SoundStreamHeadTypeTag) {
                    continue;
                }

                if (d instanceof Tag
                        || d instanceof ASMSource
                        || d instanceof BinaryDataInterface
                        || d instanceof SoundStreamFrameRange) {
                    TreeNodeType nodeType = TagTree.getTreeNodeType(d);
                    if (nodeType == TreeNodeType.IMAGE) {
                        images.add((Tag) d);
                    }
                    if (nodeType == TreeNodeType.SHAPE) {
                        shapes.add((Tag) d);
                    }
                    if (nodeType == TreeNodeType.BUTTON) {
                        buttons.add((Tag) d);
                    }
                    if (nodeType == TreeNodeType.MORPH_SHAPE) {
                        morphshapes.add((Tag) d);
                    }
                    if (nodeType == TreeNodeType.SPRITE) {
                        sprites.add((Tag) d);
                    }
                    if ((nodeType == TreeNodeType.AS)
                            || (nodeType == TreeNodeType.AS_FRAME)
                            || (nodeType == TreeNodeType.AS_BUTTON)
                            || (nodeType == TreeNodeType.AS_CLIP)
                            || (nodeType == TreeNodeType.AS_INIT)
                            || (nodeType == TreeNodeType.AS_CLASS)) {
                        as12scripts.add(d);
                    }
                    if (nodeType == TreeNodeType.MOVIE) {
                        movies.add((Tag) d);
                    }
                    if (nodeType == TreeNodeType.SOUND) {
                        sounds.add((SoundTag) d);
                    }
                    if (nodeType == TreeNodeType.BINARY_DATA) {
                        binaryData.add((BinaryDataInterface) d);
                    }
                    if (nodeType == TreeNodeType.TEXT) {
                        texts.add((Tag) d);
                    }
                    if (d instanceof DefineFont4Tag) {
                        fonts4.add((Tag) d);
                    } else if (nodeType == TreeNodeType.FONT) {
                        fonts.add((Tag) d);
                    }
                    if (nodeType == TreeNodeType.OTHER_TAG) {
                        if (d instanceof SymbolClassTypeTag) {
                            symbolNames.add((Tag) d);
                        }
                    }
                }

                if (d instanceof Frame) {
                    Frame fn = (Frame) d;
                    Timelined parent = fn.timeline.timelined;
                    int frame = fn.frame;
                    int parentId = 0;
                    if (parent instanceof CharacterTag) {
                        parentId = ((CharacterTag) parent).getCharacterId();
                    }
                    if (!frames.containsKey(parentId)) {
                        frames.put(parentId, new ArrayList<>());
                    }

                    frames.get(parentId).add(frame);
                }

                if (d instanceof ScriptPack) {
                    as3scripts.add((ScriptPack) d);
                }

                if (d instanceof AS3Package) {
                    AS3Package p = (AS3Package) d;
                    if (p.isCompoundScript()) {
                        as3scripts.add(p.getCompoundInitializerPack());
                    }
                }
            }

            for (Tag sprite : sprites) {
                frames.put(((DefineSpriteTag) sprite).getCharacterId(), null);
            }

            String selFile2;
            if (usedOpenables.size() > 1) {
                if (usedOpenableLists.size() > 1 && openable.getOpenableList() != null && openable.getOpenableList().isBundle()) {
                    if (!usedSwfIdsInBundles.containsKey(openable.getOpenableList())) {
                        usedSwfIdsInBundles.put(openable.getOpenableList(), new HashMap<>());
                    }
                    selFile2 = selFile + File.separator + openable.getOpenableList().name + File.separator + Helper.getNextId(openable.getTitleOrShortFileName(), usedSwfIdsInBundles.get(openable.getOpenableList()));
                } else {
                    selFile2 = selFile + File.separator + Helper.getNextId(openable.getTitleOrShortFileName(), usedSwfsIds);
                }
            } else {
                selFile2 = selFile;
            }

            EventListener evl = swf.getExportEventListener();

            if (export.isOptionEnabled(ImageExportMode.class)) {
                ret.addAll(new ImageExporter().exportImages(handler, selFile2 + File.separator + ImageExportSettings.EXPORT_FOLDER_NAME, new ReadOnlyTagList(images),
                        new ImageExportSettings(export.getValue(ImageExportMode.class)), evl));
            }

            if (export.isOptionEnabled(ShapeExportMode.class)) {
                ret.addAll(new ShapeExporter().exportShapes(handler, selFile2 + File.separator + ShapeExportSettings.EXPORT_FOLDER_NAME, swf, new ReadOnlyTagList(shapes),
                        new ShapeExportSettings(export.getValue(ShapeExportMode.class), export.getZoom()), evl, export.getZoom()));
            }

            if (export.isOptionEnabled(MorphShapeExportMode.class)) {
                ret.addAll(new MorphShapeExporter().exportMorphShapes(handler, selFile2 + File.separator + MorphShapeExportSettings.EXPORT_FOLDER_NAME, new ReadOnlyTagList(morphshapes),
                        new MorphShapeExportSettings(export.getValue(MorphShapeExportMode.class), export.getZoom()), evl));
            }

            if (export.isOptionEnabled(TextExportMode.class)) {
                ret.addAll(new TextExporter().exportTexts(handler, selFile2 + File.separator + TextExportSettings.EXPORT_FOLDER_NAME, new ReadOnlyTagList(texts),
                        new TextExportSettings(export.getValue(TextExportMode.class), Configuration.textExportSingleFile.get(), export.getZoom()), evl));
            }

            if (export.isOptionEnabled(MovieExportMode.class)) {
                ret.addAll(new MovieExporter().exportMovies(handler, selFile2 + File.separator + MovieExportSettings.EXPORT_FOLDER_NAME, new ReadOnlyTagList(movies),
                        new MovieExportSettings(export.getValue(MovieExportMode.class)), evl));
            }

            if (export.isOptionEnabled(SoundExportMode.class)) {
                ret.addAll(new SoundExporter().exportSounds(handler, selFile2 + File.separator + SoundExportSettings.EXPORT_FOLDER_NAME, sounds,
                        new SoundExportSettings(export.getValue(SoundExportMode.class), export.isResampleWavEnabled()), evl));
            }

            if (export.isOptionEnabled(BinaryDataExportMode.class)) {
                ret.addAll(new BinaryDataExporter().exportBinaryData(handler, selFile2 + File.separator + BinaryDataExportSettings.EXPORT_FOLDER_NAME, binaryData,
                        new BinaryDataExportSettings(export.getValue(BinaryDataExportMode.class)), evl));
            }

            if (export.isOptionEnabled(FontExportMode.class)) {
                ret.addAll(new FontExporter().exportFonts(handler, selFile2 + File.separator + FontExportSettings.EXPORT_FOLDER_NAME, new ReadOnlyTagList(fonts),
                        new FontExportSettings(export.getValue(FontExportMode.class)), evl));
            }

            if (export.isOptionEnabled(Font4ExportMode.class)) {
                ret.addAll(new Font4Exporter().exportFonts(handler, selFile2 + File.separator + Font4ExportSettings.EXPORT_FOLDER_NAME, new ReadOnlyTagList(fonts4),
                        new Font4ExportSettings(export.getValue(Font4ExportMode.class)), evl));
            }

            if (export.isOptionEnabled(SymbolClassExportMode.class)) {
                ret.addAll(new SymbolClassExporter().exportNames(handler, selFile2 + File.separator + SymbolClassExportSettings.EXPORT_FOLDER_NAME, new ReadOnlyTagList(symbolNames),
                        new SymbolClassExportSettings(export.getValue(SymbolClassExportMode.class)), evl));
            }

            FrameExporter frameExporter = new FrameExporter();

            if (export.isOptionEnabled(FrameExportMode.class)) {
                FrameExportSettings fes = new FrameExportSettings(export.getValue(FrameExportMode.class), export.getZoom(), export.isTransparentFrameBackgroundEnabled());
                if (frames.containsKey(0)) {
                    String subFolder = FrameExportSettings.EXPORT_FOLDER_NAME;
                    ret.addAll(frameExporter.exportFrames(handler, selFile2 + File.separator + subFolder, swf, 0, frames.get(0), fes, evl));
                }
            }

            if (export.isOptionEnabled(SpriteExportMode.class)) {
                SpriteExportSettings ses = new SpriteExportSettings(export.getValue(SpriteExportMode.class), export.getZoom());
                for (Entry<Integer, List<Integer>> entry : frames.entrySet()) {
                    int containerId = entry.getKey();
                    if (containerId != 0) {
                        String subFolder = SpriteExportSettings.EXPORT_FOLDER_NAME;
                        ret.addAll(frameExporter.exportSpriteFrames(handler, selFile2 + File.separator + subFolder, swf, containerId, entry.getValue(), ses, evl));
                    }
                }
            }

            if (export.isOptionEnabled(ButtonExportMode.class)) {
                ButtonExportSettings bes = new ButtonExportSettings(export.getValue(ButtonExportMode.class), export.getZoom());
                for (Tag tag : buttons) {
                    ButtonTag button = (ButtonTag) tag;
                    String subFolder = ButtonExportSettings.EXPORT_FOLDER_NAME;
                    ret.addAll(frameExporter.exportButtonFrames(handler, selFile2 + File.separator + subFolder, swf, button.getCharacterId(), null, bes, evl));
                }
            }

            if (export.isOptionEnabled(ScriptExportMode.class)) {
                if (as3scripts.size() > 0 || as12scripts.size() > 0) {
                    boolean parallel = Configuration.parallelSpeedUp.get();
                    String scriptsFolder = Path.combine(selFile2, ScriptExportSettings.EXPORT_FOLDER_NAME);
                    Path.createDirectorySafe(new File(scriptsFolder));
                    boolean singleScriptFile = Configuration.scriptExportSingleFile.get();
                    if (parallel && singleScriptFile) {
                        logger.log(Level.WARNING, AppStrings.translate("export.script.singleFileParallelModeWarning"));
                        singleScriptFile = false;
                    }

                    ScriptExportSettings scriptExportSettings = new ScriptExportSettings(export.getValue(ScriptExportMode.class), singleScriptFile, false, export.isEmbedEnabled(), false, export.isResampleWavEnabled());
                    String singleFileName = Path.combine(scriptsFolder, openable.getShortFileName() + scriptExportSettings.getFileExtension());
                    try (FileTextWriter writer = scriptExportSettings.singleFile ? new FileTextWriter(Configuration.getCodeFormatting(), new FileOutputStream(singleFileName)) : null) {
                        scriptExportSettings.singleFileWriter = writer;
                        if (swf.isAS3()) {
                            ret.addAll(new AS3ScriptExporter().exportActionScript3(swf, handler, scriptsFolder, as3scripts, scriptExportSettings, parallel, evl));
                        } else {
                            Map<String, ASMSource> asmsToExport = swf.getASMs(true, as12scripts, false);
                            ret.addAll(new AS2ScriptExporter().exportAS2Scripts(handler, scriptsFolder, asmsToExport, scriptExportSettings, parallel, evl));
                        }
                    }
                }
            }
        }

        return ret;
    }

    public void exportAll(SWF swf, AbortRetryIgnoreHandler handler, String selFile, ExportDialog export) throws IOException, InterruptedException {
        boolean exportAll = false;
        if (exportAll) {
            exportAllDebug(swf, handler, selFile, export);
            return;
        }

        EventListener evl = swf.getExportEventListener();

        if (export.isOptionEnabled(ImageExportMode.class)) {
            new ImageExporter().exportImages(handler, Path.combine(selFile, ImageExportSettings.EXPORT_FOLDER_NAME), swf.getTags(),
                    new ImageExportSettings(export.getValue(ImageExportMode.class)), evl);
        }

        if (export.isOptionEnabled(ShapeExportMode.class)) {
            new ShapeExporter().exportShapes(handler, Path.combine(selFile, ShapeExportSettings.EXPORT_FOLDER_NAME), swf, swf.getTags(),
                    new ShapeExportSettings(export.getValue(ShapeExportMode.class), export.getZoom()), evl, export.getZoom());
        }

        if (export.isOptionEnabled(MorphShapeExportMode.class)) {
            new MorphShapeExporter().exportMorphShapes(handler, Path.combine(selFile, MorphShapeExportSettings.EXPORT_FOLDER_NAME), swf.getTags(),
                    new MorphShapeExportSettings(export.getValue(MorphShapeExportMode.class), export.getZoom()), evl);
        }

        if (export.isOptionEnabled(TextExportMode.class)) {
            new TextExporter().exportTexts(handler, Path.combine(selFile, TextExportSettings.EXPORT_FOLDER_NAME), swf.getTags(),
                    new TextExportSettings(export.getValue(TextExportMode.class), Configuration.textExportSingleFile.get(), export.getZoom()), evl);
        }

        if (export.isOptionEnabled(MovieExportMode.class)) {
            new MovieExporter().exportMovies(handler, Path.combine(selFile, MovieExportSettings.EXPORT_FOLDER_NAME), swf.getTags(),
                    new MovieExportSettings(export.getValue(MovieExportMode.class)), evl);
        }

        if (export.isOptionEnabled(SoundExportMode.class)) {
            new SoundExporter().exportSounds(handler, Path.combine(selFile, SoundExportSettings.EXPORT_FOLDER_NAME), swf.getTags(),
                    new SoundExportSettings(export.getValue(SoundExportMode.class), export.isResampleWavEnabled()), evl);
        }

        if (export.isOptionEnabled(BinaryDataExportMode.class)) {
            new BinaryDataExporter().exportBinaryData(handler, Path.combine(selFile, BinaryDataExportSettings.EXPORT_FOLDER_NAME), swf.getTags(),
                    new BinaryDataExportSettings(export.getValue(BinaryDataExportMode.class)), evl);
        }

        if (export.isOptionEnabled(FontExportMode.class)) {
            new FontExporter().exportFonts(handler, Path.combine(selFile, FontExportSettings.EXPORT_FOLDER_NAME), swf.getTags(),
                    new FontExportSettings(export.getValue(FontExportMode.class)), evl);
        }

        if (export.isOptionEnabled(SymbolClassExportMode.class)) {
            new SymbolClassExporter().exportNames(handler, Path.combine(selFile, SymbolClassExportSettings.EXPORT_FOLDER_NAME), swf.getTags(),
                    new SymbolClassExportSettings(export.getValue(SymbolClassExportMode.class)), evl);
        }

        FrameExporter frameExporter = new FrameExporter();

        if (export.isOptionEnabled(FrameExportMode.class)) {
            FrameExportSettings fes = new FrameExportSettings(export.getValue(FrameExportMode.class), export.getZoom(), export.isTransparentFrameBackgroundEnabled());
            frameExporter.exportFrames(handler, Path.combine(selFile, FrameExportSettings.EXPORT_FOLDER_NAME), swf, 0, null, fes, evl);
        }

        if (export.isOptionEnabled(SpriteExportMode.class)) {
            SpriteExportSettings ses = new SpriteExportSettings(export.getValue(SpriteExportMode.class), export.getZoom());
            for (CharacterTag c : swf.getCharacters(false).values()) {
                if (c instanceof DefineSpriteTag) {
                    frameExporter.exportSpriteFrames(handler, Path.combine(selFile, SpriteExportSettings.EXPORT_FOLDER_NAME), swf, c.getCharacterId(), null, ses, evl);
                }
            }
        }

        if (export.isOptionEnabled(ButtonExportMode.class)) {
            ButtonExportSettings bes = new ButtonExportSettings(export.getValue(ButtonExportMode.class), export.getZoom());
            for (CharacterTag c : swf.getCharacters(false).values()) {
                if (c instanceof ButtonTag) {
                    frameExporter.exportButtonFrames(handler, Path.combine(selFile, ButtonExportSettings.EXPORT_FOLDER_NAME), swf, c.getCharacterId(), null, bes, evl);
                }
            }
        }

        if (export.isOptionEnabled(ScriptExportMode.class)) {
            boolean parallel = Configuration.parallelSpeedUp.get();
            String scriptsFolder = Path.combine(selFile, ScriptExportSettings.EXPORT_FOLDER_NAME);
            Path.createDirectorySafe(new File(scriptsFolder));
            boolean singleScriptFile = Configuration.scriptExportSingleFile.get();
            if (parallel && singleScriptFile) {
                logger.log(Level.WARNING, AppStrings.translate("export.script.singleFileParallelModeWarning"));
                singleScriptFile = false;
            }

            ScriptExportSettings scriptExportSettings = new ScriptExportSettings(export.getValue(ScriptExportMode.class), singleScriptFile, false, export.isEmbedEnabled(), false, export.isResampleWavEnabled());
            String singleFileName = Path.combine(scriptsFolder, swf.getShortFileName() + scriptExportSettings.getFileExtension());
            try (FileTextWriter writer = scriptExportSettings.singleFile ? new FileTextWriter(Configuration.getCodeFormatting(), new FileOutputStream(singleFileName)) : null) {
                scriptExportSettings.singleFileWriter = writer;
                swf.exportActionScript(handler, scriptsFolder, scriptExportSettings, parallel, evl);
            }
        }
    }

    public void exportAllDebug(SWF swf, AbortRetryIgnoreHandler handler, String selFile, ExportDialog export) throws IOException, InterruptedException {
        EventListener evl = swf.getExportEventListener();

        if (export.isOptionEnabled(ImageExportMode.class)) {
            for (ImageExportMode exportMode : ImageExportMode.values()) {
                new ImageExporter().exportImages(handler, Path.combine(selFile, ImageExportSettings.EXPORT_FOLDER_NAME, exportMode.name()), swf.getTags(),
                        new ImageExportSettings(exportMode), evl);
            }
        }

        if (export.isOptionEnabled(ShapeExportMode.class)) {
            for (ShapeExportMode exportMode : ShapeExportMode.values()) {
                new ShapeExporter().exportShapes(handler, Path.combine(selFile, ShapeExportSettings.EXPORT_FOLDER_NAME, exportMode.name()), swf, swf.getTags(),
                        new ShapeExportSettings(exportMode, export.getZoom()), evl, export.getZoom());
            }
        }

        if (export.isOptionEnabled(MorphShapeExportMode.class)) {
            for (MorphShapeExportMode exportMode : MorphShapeExportMode.values()) {
                new MorphShapeExporter().exportMorphShapes(handler, Path.combine(selFile, MorphShapeExportSettings.EXPORT_FOLDER_NAME, exportMode.name()), swf.getTags(),
                        new MorphShapeExportSettings(exportMode, export.getZoom()), evl);
            }
        }

        if (export.isOptionEnabled(TextExportMode.class)) {
            for (TextExportMode exportMode : TextExportMode.values()) {
                new TextExporter().exportTexts(handler, Path.combine(selFile, TextExportSettings.EXPORT_FOLDER_NAME, exportMode.name()), swf.getTags(),
                        new TextExportSettings(exportMode, Configuration.textExportSingleFile.get(), export.getZoom()), evl);
            }
        }

        if (export.isOptionEnabled(MovieExportMode.class)) {
            for (MovieExportMode exportMode : MovieExportMode.values()) {
                new MovieExporter().exportMovies(handler, Path.combine(selFile, MovieExportSettings.EXPORT_FOLDER_NAME, exportMode.name()), swf.getTags(),
                        new MovieExportSettings(exportMode), evl);
            }
        }

        if (export.isOptionEnabled(SoundExportMode.class)) {
            for (SoundExportMode exportMode : SoundExportMode.values()) {
                new SoundExporter().exportSounds(handler, Path.combine(selFile, SoundExportSettings.EXPORT_FOLDER_NAME, exportMode.name()), swf.getTags(),
                        new SoundExportSettings(exportMode, export.isResampleWavEnabled()), evl);
            }
        }

        if (export.isOptionEnabled(BinaryDataExportMode.class)) {
            for (BinaryDataExportMode exportMode : BinaryDataExportMode.values()) {
                new BinaryDataExporter().exportBinaryData(handler, Path.combine(selFile, BinaryDataExportSettings.EXPORT_FOLDER_NAME, exportMode.name()), swf.getTags(),
                        new BinaryDataExportSettings(exportMode), evl);
            }
        }

        if (export.isOptionEnabled(FontExportMode.class)) {
            for (FontExportMode exportMode : FontExportMode.values()) {
                new FontExporter().exportFonts(handler, Path.combine(selFile, FontExportSettings.EXPORT_FOLDER_NAME, exportMode.name()), swf.getTags(),
                        new FontExportSettings(exportMode), evl);
            }
        }

        if (export.isOptionEnabled(SymbolClassExportMode.class)) {
            for (SymbolClassExportMode exportMode : SymbolClassExportMode.values()) {
                new SymbolClassExporter().exportNames(handler, Path.combine(selFile, SymbolClassExportSettings.EXPORT_FOLDER_NAME, exportMode.name()), swf.getTags(),
                        new SymbolClassExportSettings(exportMode), evl);
            }
        }

        FrameExporter frameExporter = new FrameExporter();

        if (export.isOptionEnabled(FrameExportMode.class)) {
            for (FrameExportMode exportMode : FrameExportMode.values()) {
                FrameExportSettings fes = new FrameExportSettings(exportMode, export.getZoom(), export.isTransparentFrameBackgroundEnabled());
                frameExporter.exportFrames(handler, Path.combine(selFile, FrameExportSettings.EXPORT_FOLDER_NAME, exportMode.name()), swf, 0, null, fes, evl);
            }
        }

        if (export.isOptionEnabled(SpriteExportMode.class)) {
            for (SpriteExportMode exportMode : SpriteExportMode.values()) {
                SpriteExportSettings ses = new SpriteExportSettings(exportMode, export.getZoom());
                for (CharacterTag c : swf.getCharacters(false).values()) {
                    if (c instanceof DefineSpriteTag) {
                        frameExporter.exportSpriteFrames(handler, Path.combine(selFile, SpriteExportSettings.EXPORT_FOLDER_NAME, exportMode.name()), swf, c.getCharacterId(), null, ses, evl);
                    }
                }
            }
        }

        if (export.isOptionEnabled(ButtonExportMode.class)) {
            for (ButtonExportMode exportMode : ButtonExportMode.values()) {
                ButtonExportSettings bes = new ButtonExportSettings(exportMode, export.getZoom());
                for (CharacterTag c : swf.getCharacters(false).values()) {
                    if (c instanceof ButtonTag) {
                        frameExporter.exportButtonFrames(handler, Path.combine(selFile, ButtonExportSettings.EXPORT_FOLDER_NAME, exportMode.name()), swf, c.getCharacterId(), null, bes, evl);
                    }
                }
            }
        }

        if (export.isOptionEnabled(ScriptExportMode.class)) {
            boolean parallel = Configuration.parallelSpeedUp.get();
            for (ScriptExportMode exportMode : ScriptExportMode.values()) {
                String scriptsFolder = Path.combine(selFile, ScriptExportSettings.EXPORT_FOLDER_NAME, exportMode.name());
                Path.createDirectorySafe(new File(scriptsFolder));
                boolean singleScriptFile = Configuration.scriptExportSingleFile.get();
                if (parallel && singleScriptFile) {
                    logger.log(Level.WARNING, AppStrings.translate("export.script.singleFileParallelModeWarning"));
                    singleScriptFile = false;
                }

                ScriptExportSettings scriptExportSettings = new ScriptExportSettings(exportMode, singleScriptFile, false, export.isEmbedEnabled(), false, export.isResampleWavEnabled());
                String singleFileName = Path.combine(scriptsFolder, swf.getShortFileName() + scriptExportSettings.getFileExtension());
                try (FileTextWriter writer = scriptExportSettings.singleFile ? new FileTextWriter(Configuration.getCodeFormatting(), new FileOutputStream(singleFileName)) : null) {
                    scriptExportSettings.singleFileWriter = writer;
                    swf.exportActionScript(handler, scriptsFolder, scriptExportSettings, parallel, evl);
                }
            }
        }
    }

    public List<OpenableList> getSwfs() {
        return openables;
    }

    public Map<String, SWF> getSwfsMap(SWF swf) {
        Map<String, SWF> result = new LinkedHashMap<>();
        populateSwfs(result, swf, swf.getShortFileName());
        return result;
    }

    private void populateSwfs(Map<String, SWF> result, SWF targetSwf, String name) {
        for (Tag t : targetSwf.getTags()) {
            if (t instanceof DefineBinaryDataTag) {
                BinaryDataInterface binaryData = (BinaryDataInterface) t;
                String bname = name;
                do {
                    bname = bname + "/" + binaryData.getPathIdentifier();
                    if (binaryData.getInnerSwf() != null) {
                        result.put(bname, binaryData.getInnerSwf());
                    }
                } while ((binaryData = binaryData.getSub()) != null);
            }
        }
    }

    public OpenableList getCurrentSwfList() {
        SWF swf = getCurrentSwf();
        if (swf == null) {
            return null;
        }

        return swf.openableList;
    }

    public Openable getCurrentOpenable() {
        if (openables == null || openables.isEmpty()) {
            return null;
        }

        if (currentView == VIEW_EASY) {
            return easyPanel.getSwf();
        }
        if (treePanelMode == TreePanelMode.TAG_TREE) {
            TreeItem treeNode = (TreeItem) tagTree.getLastSelectedPathComponent();
            if (treeNode == null || treeNode instanceof OpenableList) {
                return null;
            }

            return treeNode.getOpenable();
        } else if (treePanelMode == TreePanelMode.DUMP_TREE) {
            DumpInfo dumpInfo = (DumpInfo) dumpTree.getLastSelectedPathComponent();

            if (dumpInfo == null) {
                return null;
            }

            return DumpInfoSwfNode.getSwfNode(dumpInfo).getOpenable();
        } else if (treePanelMode == TreePanelMode.TAGLIST_TREE) {
            TreeItem treeNode = (TreeItem) tagListTree.getLastSelectedPathComponent();
            if (treeNode == null || treeNode instanceof OpenableList) {
                return null;
            }

            return treeNode.getOpenable();
        }

        return null;
    }

    public SWF getCurrentSwf() {
        Openable openable = getCurrentOpenable();
        if (openable instanceof SWF) {
            return (SWF) openable;
        }
        return null;
    }

    public AbstractTagTree getCurrentTree() {
        if (currentView == VIEW_RESOURCES) {
            return tagTree;
        }
        if (currentView == VIEW_TAGLIST) {
            return tagListTree;
        }
        return tagTree; //???
    }

    public void gotoFrame(int frame) {
        View.checkAccess();

        TreeItem treeItem = (TreeItem) getCurrentTree().getLastSelectedPathComponent();
        if (treeItem == null) {
            return;
        }
        if (treeItem instanceof Timelined) {
            Timelined t = (Timelined) treeItem;
            Frame f = tagTree.getFullModel().getFrame((SWF) treeItem.getOpenable(), t, frame);
            if (f != null) {
                setTagTreeSelectedNode(getCurrentTree(), f);
            }
        }
    }

    public void gotoScriptMethod(SWF swf, String scriptName, int methodIndex) {
        abcPanel.decompiledTextArea.addScriptListener(new Runnable() {
            @Override
            public void run() {
                if (abcPanel != null) {
                    abcPanel.decompiledTextArea.removeScriptListener(this);
                    abcPanel.decompiledTextArea.gotoMethod(methodIndex);
                }
            }
        });
        gotoScriptName(swf, scriptName);
    }

    public void gotoScriptTrait(SWF swf, String scriptName, int classIndex, int traitIndex) {
        abcPanel.decompiledTextArea.addScriptListener(new Runnable() {
            @Override
            public void run() {
                if (abcPanel != null) {
                    abcPanel.decompiledTextArea.removeScriptListener(this);
                    if (abcPanel.decompiledTextArea.getClassIndex() != classIndex) {
                        abcPanel.decompiledTextArea.setClassIndex(classIndex);
                    }
                    if (traitIndex != -10) {
                        abcPanel.decompiledTextArea.gotoTrait(traitIndex);
                    }
                }
            }
        });
        gotoScriptName(swf, scriptName);
    }

    public void gotoScriptLine(SWF swf, String scriptName, int line, int classIndex, int traitIndex, int methodIndex, boolean pcode) {
        View.checkAccess();

        if (abcPanel != null && swf.isAS3()) {
            abcPanel.decompiledTextArea.addScriptListener(new Runnable() {
                @Override
                public void run() {
                    abcPanel.decompiledTextArea.removeScriptListener(this);
                    View.execInEventDispatchLater(new Runnable() {
                        @Override
                        public void run() {
                            if (pcode) {
                                if (classIndex != -1) {
                                    if (abcPanel.decompiledTextArea.getClassIndex() != classIndex) {
                                        abcPanel.decompiledTextArea.setClassIndex(classIndex);
                                    }
                                }
                                abcPanel.decompiledTextArea.gotoMethod(methodIndex, new Runnable() {
                                    @Override
                                    public void run() {
                                        abcPanel.detailPanel.methodTraitPanel.methodCodePanel.gotoInstrLine(line);
                                    }
                                });

                            } else {
                                abcPanel.decompiledTextArea.gotoLine(line);
                            }
                            scrollPosStorage.saveScrollPos(oldItem);
                            refreshBreakPoints();
                        }
                    });
                }
            });

        } else if (actionPanel != null && !swf.isAS3()) {
            actionPanel.addScriptListener(new Runnable() {
                @Override
                public void run() {
                    actionPanel.removeScriptListener(this);
                    View.execInEventDispatchLater(new Runnable() {
                        @Override
                        public void run() {
                            if (pcode) {
                                actionPanel.editor.gotoLine(line);
                            } else {
                                actionPanel.decompiledEditor.gotoLine(line);
                            }
                            scrollPosStorage.saveScrollPos(oldItem);
                            refreshBreakPoints();
                        }
                    });
                }
            });
        }
        gotoScriptName(swf, scriptName);
    }

    public void refreshBreakPoints() {
        if (abcPanel != null) {
            abcPanel.decompiledTextArea.refreshMarkers();
            abcPanel.detailPanel.methodTraitPanel.methodCodePanel.refreshMarkers();
        }
        if (actionPanel != null) {
            actionPanel.decompiledEditor.refreshMarkers();
            actionPanel.editor.refreshMarkers();
        }
    }

    /*
     public void debuggerBreakAt(SWF swf, String cls, int line) {
     View.execInEventDispatchLater(new Runnable() {

     @Override
     public void run() {
     gotoClassLine(swf, cls, line);
     if (abcPanel != null) {
     abcPanel.decompiledTextArea.addColorMarker(line, DecompiledEditorPane.FG_IP_COLOR, DecompiledEditorPane.BG_IP_COLOR, DecompiledEditorPane.PRIORITY_IP);
     }
     }
     });

     }*/
    public void gotoScriptName(SWF mainSwf, String scriptNameWithSwfHash) {
        View.checkAccess();

        if (mainSwf == null) {
            return;
        }
        if (mainSwf.isAS3()) {
            String rawScriptName = scriptNameWithSwfHash;
            if (rawScriptName.startsWith("#PCODE ")) {
                rawScriptName = rawScriptName.substring(rawScriptName.indexOf(';') + 1);
            }

            //List<ABCContainerTag> abcList = swf.getAbcList();
            //abcPanel.setAbc(abcList.get(0).getABC());
            //if (!abcList.isEmpty()) {
            ABCPanel abcPanel = getABCPanel();

            abcPanel.hilightScript(mainSwf, rawScriptName);
            //}
        } else {
            String rawScriptName = scriptNameWithSwfHash;
            if (rawScriptName.startsWith("#PCODE ")) {
                rawScriptName = rawScriptName.substring("#PCODE ".length());
            }
            Map<String, ASMSource> asms = mainSwf.getASMs(true);
            if (asms.containsKey(rawScriptName)) {
                oldItem = null;
                getCurrentTree().setSelectionPath(null);
                setTagTreeSelectedNode(getCurrentTree(), asms.get(rawScriptName));
            }
            /*if (actionPanel != null && asms.containsKey(rawScriptName)) {
                actionPanel.setSource(asms.get(rawScriptName), true);                
            }*/
        }
    }

    public void gotoDocumentClass(SWF swf) {
        View.checkAccess();

        if (swf == null) {
            return;
        }

        String documentClass = swf.getDocumentClass();
        if (documentClass != null && currentView != VIEW_DUMP) {
            String documentClassPrintable = DottedChain.parseNoSuffix(documentClass).toPrintableString(true);
            List<ABCContainerTag> abcList = swf.getAbcList();
            if (!abcList.isEmpty()) {
                ABCPanel abcPanel = getABCPanel();
                for (ABCContainerTag c : abcList) {
                    if (c.getABC().findClassByName(documentClass) > -1) {
                        abcPanel.setAbc(c.getABC());
                        abcPanel.hilightScript(swf, documentClassPrintable);
                        break;
                    }
                }
            }
        }
    }

    public void disableDecompilationChanged() {
        View.checkAccess();

        clearAllScriptCache();

        if (abcPanel != null) {
            abcPanel.reload();
        }

        updateClassesList();
    }

    private void clearAllScriptCache() {
        for (OpenableList openableList : openables) {
            for (Openable openable : openableList) {
                if (openable instanceof SWF) {
                    ((SWF) openable).clearScriptCache();
                }
            }
        }
    }

    public Set<SWF> getAllSwfs() {
        List<SWF> allSwfs = new ArrayList<>();
        for (OpenableList slist : getSwfs()) {
            for (Openable o : slist.items) {
                if (o instanceof SWF) {
                    allSwfs.add((SWF) o);
                    Main.populateSwfs((SWF) o, allSwfs);
                }
            }
        }
        return new LinkedHashSet<>(allSwfs);
    }

    public Set<Openable> getAllOpenablesAndSwfs() {
        List<Openable> allOpenables = new ArrayList<>();
        for (OpenableList slist : getSwfs()) {
            for (Openable o : slist.items) {
                allOpenables.add(o);
                if (o instanceof SWF) {
                    List<SWF> subSwfs = new ArrayList<>();
                    Main.populateSwfs((SWF) o, subSwfs);
                    for (SWF swf : subSwfs) {
                        allOpenables.add(swf);
                    }
                }
            }
        }
        return new LinkedHashSet<>(allOpenables);
    }

    private List<TreeItem> getAllSelected() {
        if (currentView == VIEW_RESOURCES) {
            return tagTree.getAllSelected();
        }
        if (currentView == VIEW_TAGLIST) {
            return tagListTree.getAllSelected();
        }
        return new ArrayList<>();
    }

    private List<TreeItem> getSelected() {
        if (currentView == VIEW_RESOURCES) {
            return tagTree.getSelected();
        }
        if (currentView == VIEW_TAGLIST) {
            return tagListTree.getSelected();
        }
        return new ArrayList<>();
    }

    public void searchInActionScriptOrText(Boolean searchInText, Openable openable, boolean useSelection) {
        View.checkAccess();

        /*if (!(openable instanceof SWF)) { //FIXME for ABCs
            return;
        }*/
        SWF swf = (openable instanceof SWF) ? (SWF) openable : ((ABC) openable).getSwf();

        Map<Openable, List<ScriptPack>> scopeAs3 = new LinkedHashMap<>();
        Map<SWF, Map<String, ASMSource>> swfToAllASMSourceMap = new HashMap<>();
        Map<SWF, Map<String, ASMSource>> scopeAs12 = new LinkedHashMap<>();
        Set<TextTag> scopeTextTags = new LinkedIdentityHashSet<>();

        Set<Openable> openablesUsed = new LinkedHashSet<>();

        List<TreeItem> allItems = getAllSelected();
        for (TreeItem t : allItems) {
            if (t instanceof SWF) {
                for (Tag g : ((SWF) t).getTags()) {
                    if (g instanceof TextTag) {
                        scopeTextTags.add((TextTag) g);
                    }
                }
            }
            if (t instanceof FolderItem) {
                FolderItem f = (FolderItem) t;
                for (TreeItem t2 : f.subItems) {
                    if (t2 instanceof TextTag) {
                        scopeTextTags.add((TextTag) t2);
                    }
                }
            }
            if (t instanceof TextTag) {
                scopeTextTags.add((TextTag) t);
            }
            if (t instanceof ScriptPack) {
                ScriptPack sp = (ScriptPack) t;
                Openable s = sp.getOpenable(); //Fixme for ABCs
                if (!scopeAs3.containsKey(s)) {
                    scopeAs3.put(s, new ArrayList<>());
                }
                scopeAs3.get(s).add(sp);
                openablesUsed.add(s);
            }
            ASMSource as = null;
            if (t instanceof ASMSource) {
                as = (ASMSource) t;
            } else if (t instanceof TagScript) {
                TagScript ts = (TagScript) t;
                if (ts.getTag() instanceof ASMSource) {
                    as = (ASMSource) ts.getTag();
                }
            }
            if (as != null) {
                SWF s = as.getSourceTag().getSwf();
                String asId = null;
                Map<String, ASMSource> allSources;
                if (swfToAllASMSourceMap.containsKey(s)) {
                    allSources = swfToAllASMSourceMap.get(s);
                } else {
                    allSources = s.getASMs(false);
                    swfToAllASMSourceMap.put(s, allSources);
                }
                for (String path : allSources.keySet()) {
                    if (allSources.get(path) == as) {
                        asId = path;
                        break;
                    }
                }
                if (!scopeAs12.containsKey(s)) {
                    scopeAs12.put(s, new LinkedHashMap<>());
                }
                scopeAs12.get(s).put(asId, as);
                openablesUsed.add(s);
            }
        }

        List<TreeItem> items = getSelected();
        String selected;

        if (scopeAs12.isEmpty() && scopeAs3.isEmpty() && scopeTextTags.isEmpty()) {
            selected = null;
        } else if (items.size() == 1) {
            selected = items.get(0).toString();
        } else if (items.isEmpty()) {
            selected = null;
        } else {
            selected = AppDialog.translateForDialog("scope.selection.items", SearchDialog.class).replace("%count%", "" + items.size());
        }

        SearchDialog searchDialog = new SearchDialog(getMainFrame().getWindow(), false, selected, useSelection, openable instanceof ABC);
        if (searchInText != null) {
            if (searchInText) {
                searchDialog.searchInTextsRadioButton.setSelected(true);
            } else {
                searchDialog.searchInASRadioButton.setSelected(true);
            }
        }

        if (searchDialog.showDialog() == AppDialog.OK_OPTION) {
            final String txt = searchDialog.searchField.getText();
            if (!txt.isEmpty()) {

                if (searchDialog.getCurrentScope() == SearchDialog.SCOPE_CURRENT_FILE) {
                    scopeAs3.clear();
                    scopeAs12.clear();
                    if (swf.isAS3()) {
                        List<ScriptPack> packs = new ArrayList<>();
                        if (openable instanceof SWF) {
                            packs = ((SWF) openable).getAS3Packs();
                        } else {
                            ABC abc = (ABC) openable;
                            List<ABC> allAbcs = new ArrayList<>();
                            allAbcs.add(abc);
                            packs = abc.getScriptPacks(null, allAbcs);
                        }
                        scopeAs3.put(openable, packs);
                    } else {
                        scopeAs12.put((SWF) openable, swf.getASMs(false));
                    }
                    openablesUsed.clear();
                    openablesUsed.add(openable);
                }
                if (searchDialog.getCurrentScope() == SearchDialog.SCOPE_ALL_FILES) {
                    Set<Openable> allOpenables = getAllOpenablesAndSwfs();

                    for (Openable s : allOpenables) {
                        SWF ss = (s instanceof SWF) ? (SWF) s : ((ABC) s).getSwf();
                        if (ss.isAS3()) {
                            List<ScriptPack> packs = new ArrayList<>();
                            if (s instanceof SWF) {
                                packs = ((SWF) s).getAS3Packs();
                            } else {
                                ABC abc = (ABC) s;
                                List<ABC> allAbcs = new ArrayList<>();
                                allAbcs.add(abc);
                                packs = abc.getScriptPacks(null, allAbcs);
                            }
                            scopeAs3.put(s, packs);
                        } else {
                            scopeAs12.put(ss, ss.getASMs(false));
                        }
                    }
                    openablesUsed.clear();
                    openablesUsed.addAll(allOpenables);
                }

                if (!scopeAs3.isEmpty()) {
                    getABCPanel();
                }
                if (!scopeAs12.isEmpty()) {
                    getActionPanel();
                }

                boolean ignoreCase = searchDialog.ignoreCaseCheckBox.isSelected();
                boolean regexp = searchDialog.regexpCheckBox.isSelected();

                boolean scriptSearch = searchDialog.searchInASRadioButton.isSelected()
                        || searchDialog.searchInPCodeRadioButton.isSelected();
                if (scriptSearch) {
                    boolean pCodeSearch = searchDialog.searchInPCodeRadioButton.isSelected();
                    new CancellableWorker<Void>("scriptSearch") {
                        @Override
                        protected Void doInBackground() throws Exception {

                            List<ScriptSearchResult> fResult = new ArrayList<>();
                            for (Openable s : openablesUsed) {
                                if (scopeAs3.containsKey(s)) {
                                    List<ABCSearchResult> abcResult = getABCPanel().search(s, txt, ignoreCase, regexp, pCodeSearch, this, scopeAs3.get(s));
                                    fResult.addAll(abcResult);
                                    if (!abcResult.isEmpty()) {
                                        Main.searchResultsStorage.addABCResults(s, txt, ignoreCase, regexp, abcResult);
                                    }
                                }
                                if (scopeAs12.containsKey(s)) {
                                    List<ActionSearchResult> actionResult = getActionPanel().search((SWF) s, txt, ignoreCase, regexp, pCodeSearch, this, scopeAs12.get(s));
                                    fResult.addAll(actionResult);
                                    if (!actionResult.isEmpty()) {
                                        Main.searchResultsStorage.addActionResults((SWF) s, txt, ignoreCase, regexp, actionResult);
                                    }
                                }
                            }
                            Main.searchResultsStorage.finishGroup();

                            View.execInEventDispatch(() -> {
                                boolean found = false;
                                found = true;
                                List<SearchListener<ScriptSearchResult>> listeners = new ArrayList<>();
                                listeners.add(getABCPanel());
                                listeners.add(getActionPanel());
                                SearchResultsDialog<ScriptSearchResult> sr = new SearchResultsDialog<>(getMainFrame().getWindow(), txt, ignoreCase, regexp, listeners);
                                sr.setResults(fResult);
                                sr.setVisible(true);
                                searchResultsDialogs.add(sr);
                                if (!found) {
                                    ViewMessages.showMessageDialog(MainPanel.this, translate("message.search.notfound").replace("%searchtext%", txt), translate("message.search.notfound.title"), JOptionPane.INFORMATION_MESSAGE);
                                }

                                Main.stopWork();
                            });

                            return null;
                        }

                        @Override
                        protected void done() {
                            View.execInEventDispatch(() -> {
                                Main.stopWork();
                            });

                        }

                    }.execute();
                } else if (searchDialog.searchInTextsRadioButton.isSelected()) {
                    new CancellableWorker<Void>("searchInTexts") {
                        @Override
                        protected Void doInBackground() throws Exception {
                            List<TextTag> textResult;
                            SearchPanel<TextTag> textSearchPanel = previewPanel.getTextPanel().getSearchPanel();
                            textSearchPanel.setOptions(ignoreCase, regexp);
                            List<TextTag> scope = new ArrayList<>();
                            if (searchDialog.getCurrentScope() == SearchDialog.SCOPE_CURRENT_FILE) {
                                for (Tag t : swf.getTags()) {
                                    if (t instanceof TextTag) {
                                        scope.add((TextTag) t);
                                    }
                                }
                            } else if (searchDialog.getCurrentScope() == SearchDialog.SCOPE_ALL_FILES) {
                                for (SWF s : getAllSwfs()) {
                                    for (Tag t : s.getTags()) {
                                        if (t instanceof TextTag) {
                                            scope.add((TextTag) t);
                                        }
                                    }
                                }
                            } else if (searchDialog.getCurrentScope() == SearchDialog.SCOPE_SELECTION) {
                                scope.addAll(scopeTextTags);
                            }
                            textResult = searchText(txt, ignoreCase, regexp, scope);

                            List<TextTag> fTextResult = textResult;
                            View.execInEventDispatch(() -> {
                                textSearchPanel.setSearchText(txt);
                                boolean found = textSearchPanel.setResults(fTextResult);
                                if (!found) {
                                    ViewMessages.showMessageDialog(MainPanel.this, translate("message.search.notfound").replace("%searchtext%", txt), translate("message.search.notfound.title"), JOptionPane.INFORMATION_MESSAGE);
                                }

                                Main.stopWork();
                            });

                            return null;
                        }

                        @Override
                        protected void done() {
                            View.execInEventDispatch(() -> {
                                Main.stopWork();
                            });

                        }
                    }.execute();
                }
            }
        }
    }

    public void replaceText() {
        Set<TextTag> scopeTextTags = new LinkedIdentityHashSet<>();

        List<TreeItem> allItems = getAllSelected();
        for (TreeItem t : allItems) {
            if (t instanceof SWF) {
                for (Tag g : ((SWF) t).getTags()) {
                    if (g instanceof TextTag) {
                        scopeTextTags.add((TextTag) g);
                    }
                }
            }
            if (t instanceof FolderItem) {
                FolderItem f = (FolderItem) t;
                for (TreeItem t2 : f.subItems) {
                    if (t2 instanceof TextTag) {
                        scopeTextTags.add((TextTag) t2);
                    }
                }
            }
            if (t instanceof TextTag) {
                scopeTextTags.add((TextTag) t);
            }
        }
        List<TreeItem> items = getSelected();

        String selected;
        if (scopeTextTags.isEmpty()) {
            selected = null;
        } else if (items.size() == 1) {
            selected = items.get(0).toString();
        } else if (items.isEmpty()) {
            selected = null;
        } else {
            selected = AppDialog.translateForDialog("scope.selection.items", SearchDialog.class).replace("%count%", "" + items.size());
        }
        SearchDialog replaceDialog = new SearchDialog(getMainFrame().getWindow(), true, selected, false, false);
        if (replaceDialog.showDialog() == AppDialog.OK_OPTION) {
            final String txt = replaceDialog.searchField.getText();
            if (!txt.isEmpty()) {
                final SWF swf = getCurrentSwf();

                new CancellableWorker("replace") {
                    @Override
                    protected Void doInBackground() throws Exception {
                        int findCount = 0;
                        boolean ignoreCase = replaceDialog.ignoreCaseCheckBox.isSelected();
                        boolean regexp = replaceDialog.regexpCheckBox.isSelected();
                        String replacement = replaceDialog.replaceField.getText();
                        if (!regexp) {
                            replacement = Matcher.quoteReplacement(replacement);
                        }
                        Pattern pat;
                        if (regexp) {
                            pat = Pattern.compile(txt, ignoreCase ? (Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE) : 0);
                        } else {
                            pat = Pattern.compile(Pattern.quote(txt), ignoreCase ? (Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE) : 0);
                        }

                        List<TextTag> scope = new ArrayList<>();
                        if (replaceDialog.getCurrentScope() == SearchDialog.SCOPE_CURRENT_FILE) {
                            for (Tag t : swf.getTags()) {
                                if (t instanceof TextTag) {
                                    scope.add((TextTag) t);
                                }
                            }
                        } else if (replaceDialog.getCurrentScope() == SearchDialog.SCOPE_ALL_FILES) {
                            for (SWF s : getAllSwfs()) {
                                for (Tag t : s.getTags()) {
                                    if (t instanceof TextTag) {
                                        scope.add((TextTag) t);
                                    }
                                }
                            }
                        } else if (replaceDialog.getCurrentScope() == SearchDialog.SCOPE_SELECTION) {
                            scope.addAll(scopeTextTags);
                        }
                        for (TextTag textTag : scope) {
                            if (!replaceDialog.replaceInParametersCheckBox.isSelected()) {
                                List<String> texts = textTag.getTexts();
                                boolean found = false;
                                for (int i = 0; i < texts.size(); i++) {
                                    String text = texts.get(i);
                                    if (pat.matcher(text).find()) {
                                        texts.set(i, text.replaceAll(txt, replacement));
                                        found = true;
                                        findCount++;
                                    }
                                }
                                if (found) {
                                    String[] textArray = texts.toArray(new String[texts.size()]);
                                    textTag.setFormattedText(getMissingCharacterHandler(), textTag.getFormattedText(false).text, textArray);
                                }
                            } else {
                                String text = textTag.getFormattedText(false).text;
                                if (pat.matcher(text).find()) {
                                    textTag.setFormattedText(getMissingCharacterHandler(), text.replaceAll(txt, replacement), null);
                                    findCount++;
                                }
                            }
                        }

                        if (findCount > 0) {
                            swf.clearImageCache();
                            repaintTree();
                        }

                        return null;
                    }
                }.execute();
            }
        }
    }

    private List<TextTag> searchText(String txt, boolean ignoreCase, boolean regexp, List<TextTag> scope) {
        if (txt != null && !txt.isEmpty()) {
            List<TextTag> found = new ArrayList<>();
            Pattern pat;
            if (regexp) {
                pat = Pattern.compile(txt, ignoreCase ? (Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE) : 0);
            } else {
                pat = Pattern.compile(Pattern.quote(txt), ignoreCase ? (Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE) : 0);
            }
            for (TextTag textTag : scope) {
                if (pat.matcher(textTag.getFormattedText(true).text).find()) {
                    found.add(textTag);
                }
            }

            return found;
        }

        return null;
    }

    @Override
    public void updateSearchPos(String searchedText, boolean ignoreCase, boolean regExp, TextTag item) {
        View.checkAccess();

        setTagTreeSelectedNode(getCurrentTree(), item);
        previewPanel.getTextPanel().updateSearchPos();
    }

    private void setDumpTreeSelectedNode(DumpInfo dumpInfo) {
        DumpTreeModel dtm = (DumpTreeModel) dumpTree.getModel();
        TreePath tp = dtm.getDumpInfoPath(dumpInfo);
        if (tp != null) {
            dumpTree.setSelectionPath(tp);
            dumpTree.scrollPathToVisible(tp);
        } else {
            showCard(CARDEMPTYPANEL);
        }
    }

    public void setTagTreeSelectedNode(AbstractTagTree tree, TreeItem treeItem) {
        AbstractTagTreeModel ttm = tree.getFullModel();
        if (ttm == null) {
            return;
        }
        TreePath tp = ttm.getTreePath(treeItem);
        if (tp != null) {
            tree.setSelectionPath(tp);
            tree.scrollPathToVisible(tp);
        } else {
            showCard(CARDEMPTYPANEL);
        }
    }

    public void autoDeobfuscateChanged() {
        Helper.decompilationErrorAdd = AppStrings.translate(Configuration.autoDeobfuscate.get() ? "deobfuscation.comment.failed" : "deobfuscation.comment.tryenable");
        clearAllScriptCache();
        updateClassesList();
        reload(true);
    }

    public void renameColliding(final Openable openable) {
        View.checkAccess();

        SWF swf = null;
        if (openable instanceof SWF) {
            swf = (SWF) openable;
        }
        //FIXME for ABCs

        if (swf == null) {
            return;
        }
        final SWF fswf = swf;
        if (confirmExperimental()) {
            new CancellableWorker<Integer>("renameColliding") {
                @Override
                protected Integer doInBackground() throws Exception {
                    AbcMultiNameCollisionFixer fixer = new AbcMultiNameCollisionFixer();
                    return fixer.fixCollisions(fswf);
                }

                @Override
                protected void onStart() {
                    Main.startWork(translate("work.renaming.identifiers") + "...", this);
                }

                @Override
                protected void done() {
                    View.execInEventDispatch(() -> {
                        try {
                            int cnt = get();
                            Main.stopWork();
                            ViewMessages.showMessageDialog(MainPanel.this, translate("message.rename.renamed").replace("%count%", Integer.toString(cnt)));
                            fswf.assignClassesToSymbols();
                            fswf.clearScriptCache();
                            if (abcPanel != null) {
                                abcPanel.reload();
                            }
                            updateClassesList();
                            reload(true);
                        } catch (Exception ex) {
                            logger.log(Level.SEVERE, "Error during renaming identifiers", ex);
                            Main.stopWork();
                            ViewMessages.showMessageDialog(MainPanel.this, translate("error.occurred").replace("%error%", ex.getClass().getSimpleName()));
                        }
                    });
                }
            }.execute();
        }
    }

    public void renameOneIdentifier(final SWF swf) {
        View.checkAccess();

        if (swf == null) {
            return;
        }

        FileAttributesTag fileAttributes = swf.getFileAttributes();
        if (fileAttributes != null && fileAttributes.actionScript3) {
            final int multiName = getABCPanel().decompiledTextArea.getMultinameUnderCaret(new Reference<ABC>(null));
            final List<ABCContainerTag> abcList = swf.getAbcList();
            if (multiName > 0) {
                new CancellableWorker("renameOneIdentifierAs3") {
                    @Override
                    public Void doInBackground() throws Exception {
                        renameMultiname(abcList, multiName);
                        return null;
                    }

                    @Override
                    protected void onStart() {
                        Main.startWork(translate("work.renaming") + "...", this);
                    }

                    @Override
                    protected void done() {

                    }
                }.execute();

            } else {
                ViewMessages.showMessageDialog(MainPanel.this, translate("message.rename.notfound.multiname"), translate("message.rename.notfound.title"), JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            final String identifier = getActionPanel().getStringUnderCursor();
            if (identifier != null) {
                new CancellableWorker("renameOneIdentifierAs2") {
                    @Override
                    public Void doInBackground() throws Exception {
                        try {
                            renameIdentifier(swf, identifier);
                        } catch (InterruptedException ex) {
                            logger.log(Level.SEVERE, null, ex);
                        }
                        return null;
                    }

                    @Override
                    protected void onStart() {
                        Main.startWork(translate("work.renaming") + "...", this);
                    }

                    @Override
                    protected void done() {
                        Main.stopWork();
                    }
                }.execute();
            } else {
                ViewMessages.showMessageDialog(MainPanel.this, translate("message.rename.notfound.identifier"), translate("message.rename.notfound.title"), JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    public void exportFlashDevelop(final SWF swf) {
        if (swf == null) {
            return;
        }

        JFileChooser fc = View.getFileChooserWithIcon("exportflashdevelop");
        String selDir = Configuration.lastOpenDir.get();
        fc.setCurrentDirectory(new File(selDir));
        if (!selDir.endsWith(File.separator)) {
            selDir += File.separator;
        }
        String swfShortName = swf.getShortFileName();
        if ("".equals(swfShortName)) {
            swfShortName = "untitled.swf";
        }
        String fileName;
        if (swfShortName.contains(".")) {
            fileName = swfShortName.substring(0, swfShortName.lastIndexOf(".")) + ".as3proj";
        } else {
            fileName = swfShortName + ".as3proj";
        }

        FileFilter as3Filter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || (f.getName().toLowerCase(Locale.ENGLISH).endsWith(".as3proj"));
            }

            @Override
            public String getDescription() {
                return translate("filter.as3proj");
            }
        };
        FileFilter airAs3Filter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || (f.getName().toLowerCase(Locale.ENGLISH).endsWith(".as3proj"));
            }

            @Override
            public String getDescription() {
                return translate("filter.air.as3proj");
            }
        };
        fc.setFileFilter(as3Filter);
        fc.addChoosableFileFilter(airAs3Filter);
        fc.setAcceptAllFileFilterUsed(false);
        fc.setSelectedFile(new File(selDir + fileName));

        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Configuration.lastOpenDir.set(Helper.fixDialogFile(fc.getSelectedFile()).getParentFile().getAbsolutePath());
        File sf = Helper.fixDialogFile(fc.getSelectedFile());
        SwfFlashDevelopExporter exporter = new SwfFlashDevelopExporter();

        String path = sf.getAbsolutePath();
        if (path.endsWith(".as3proj")) {
            path = path.substring(0, path.length() - ".as3proj".length());
        }
        path += ".as3proj";

        final String fpath = path;

        long timeBefore = System.currentTimeMillis();
        new CancellableWorker("exportFlashDevelop") {
            @Override
            protected Void doInBackground() throws Exception {
                Helper.freeMem();

                CancellableWorker w = this;

                ProgressListener prog = new ProgressListener() {
                    @Override
                    public void progress(int p) {
                    }

                    @Override
                    public void status(String status) {
                        Main.startWork(translate("work.exporting.flashDevelop") + "..." + status, w);
                    }
                };
                EventListener evl = swf.getExportEventListener();
                try {
                    AbortRetryIgnoreHandler errorHandler = new GuiAbortRetryIgnoreHandler();
                    exporter.exportFlashDevelopProject(swf, new File(fpath), fc.getFileFilter() == airAs3Filter, errorHandler, evl);
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, "FlashDevelop export error", ex);
                    ViewMessages.showMessageDialog(MainPanel.this, translate("error.export") + ": " + ex.getClass().getName() + " " + ex.getLocalizedMessage(), translate("error"), JOptionPane.ERROR_MESSAGE);
                }
                Helper.freeMem();
                return null;
            }

            @Override
            protected void onStart() {
                Main.startWork(translate("work.exporting.flashDevelop") + "...", this);
            }

            @Override
            protected void done() {
                Main.stopWork();
                long timeAfter = System.currentTimeMillis();
                final long timeMs = timeAfter - timeBefore;

                View.execInEventDispatch(() -> {
                    setStatus(translate("export.finishedin").replace("%time%", Helper.formatTimeSec(timeMs)));
                });

                if (Configuration.openFolderAfterFlaExport.get()) {
                    try {
                        Desktop.getDesktop().open(new File(fpath).getAbsoluteFile().getParentFile());
                    } catch (IOException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    }
                }
            }
        }.execute();
    }

    public void exportIdea(final SWF swf) {
        if (swf == null) {
            return;
        }

        JFileChooser chooser = View.getFileChooserWithIcon("exportidea");
        chooser.setCurrentDirectory(new File(Configuration.lastExportDir.get()));
        chooser.setDialogTitle(translate("export.project.select.directory"));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        final String fpath = Helper.fixDialogFile(chooser.getSelectedFile()).getAbsolutePath();
        Configuration.lastExportDir.set(Helper.fixDialogFile(chooser.getSelectedFile()).getAbsolutePath());
        SwfIntelliJIdeaExporter exporter = new SwfIntelliJIdeaExporter();

        long timeBefore = System.currentTimeMillis();
        new CancellableWorker("exportIdea") {
            @Override
            protected Void doInBackground() throws Exception {
                Helper.freeMem();

                CancellableWorker w = this;

                ProgressListener prog = new ProgressListener() {
                    @Override
                    public void progress(int p) {
                    }

                    @Override
                    public void status(String status) {
                        Main.startWork(translate("work.exporting.idea") + "..." + status, w);
                    }
                };
                EventListener evl = swf.getExportEventListener();
                try {
                    AbortRetryIgnoreHandler errorHandler = new GuiAbortRetryIgnoreHandler();
                    exporter.exportIntelliJIdeaProject(swf, new File(fpath), errorHandler, evl);
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, "IDEA export error", ex);
                    ViewMessages.showMessageDialog(MainPanel.this, translate("error.export") + ": " + ex.getClass().getName() + " " + ex.getLocalizedMessage(), translate("error"), JOptionPane.ERROR_MESSAGE);
                }
                Helper.freeMem();
                return null;
            }

            @Override
            protected void onStart() {
                Main.startWork(translate("work.exporting.idea") + "...", this);
            }

            @Override
            protected void done() {
                Main.stopWork();
                long timeAfter = System.currentTimeMillis();
                final long timeMs = timeAfter - timeBefore;

                View.execInEventDispatch(() -> {
                    setStatus(translate("export.finishedin").replace("%time%", Helper.formatTimeSec(timeMs)));
                });

                if (Configuration.openFolderAfterFlaExport.get()) {
                    try {
                        Desktop.getDesktop().open(new File(fpath).getAbsoluteFile().getParentFile());
                    } catch (IOException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    }
                }
            }
        }.execute();
    }

    public void exportVsCode(final SWF swf) {
        if (swf == null) {
            return;
        }

        JFileChooser chooser = View.getFileChooserWithIcon("exportvscode");
        chooser.setCurrentDirectory(new File(Configuration.lastExportDir.get()));
        chooser.setDialogTitle(translate("export.project.select.directory"));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        final String fpath = Helper.fixDialogFile(chooser.getSelectedFile()).getAbsolutePath();
        Configuration.lastExportDir.set(Helper.fixDialogFile(chooser.getSelectedFile()).getAbsolutePath());
        SwfVsCodeExporter exporter = new SwfVsCodeExporter();

        long timeBefore = System.currentTimeMillis();
        new CancellableWorker("exportVsCode") {
            @Override
            protected Void doInBackground() throws Exception {
                Helper.freeMem();

                CancellableWorker w = this;

                ProgressListener prog = new ProgressListener() {
                    @Override
                    public void progress(int p) {
                    }

                    @Override
                    public void status(String status) {
                        Main.startWork(translate("work.exporting.vsCode") + "..." + status, w);
                    }
                };
                EventListener evl = swf.getExportEventListener();
                try {
                    AbortRetryIgnoreHandler errorHandler = new GuiAbortRetryIgnoreHandler();
                    exporter.exportVsCodeProject(swf, new File(fpath), false, errorHandler, evl);
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, "VsCode export error", ex);
                    ViewMessages.showMessageDialog(MainPanel.this, translate("error.export") + ": " + ex.getClass().getName() + " " + ex.getLocalizedMessage(), translate("error"), JOptionPane.ERROR_MESSAGE);
                }
                Helper.freeMem();
                return null;
            }

            @Override
            protected void onStart() {
                Main.startWork(translate("work.exporting.vsCode") + "...", this);
            }

            @Override
            protected void done() {
                Main.stopWork();
                long timeAfter = System.currentTimeMillis();
                final long timeMs = timeAfter - timeBefore;

                View.execInEventDispatch(() -> {
                    setStatus(translate("export.finishedin").replace("%time%", Helper.formatTimeSec(timeMs)));
                });

                if (Configuration.openFolderAfterFlaExport.get()) {
                    try {
                        Desktop.getDesktop().open(new File(fpath).getAbsoluteFile().getParentFile());
                    } catch (IOException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    }
                }
            }
        }.execute();
    }

    public void exportFla(final SWF swf) {
        if (swf == null) {
            return;
        }
        JFileChooser fc = View.getFileChooserWithIcon("exportfla");
        String selDir = Configuration.lastOpenDir.get();
        fc.setCurrentDirectory(new File(selDir));
        if (!selDir.endsWith(File.separator)) {
            selDir += File.separator;
        }
        String swfShortName = swf.getShortFileName();
        if ("".equals(swfShortName)) {
            swfShortName = "untitled.swf";
        }
        String fileName;
        if (swfShortName.contains(".")) {
            fileName = swfShortName.substring(0, swfShortName.lastIndexOf(".")) + ".fla";
        } else {
            fileName = swfShortName + ".fla";
        }
        final String fSwfShortName = swfShortName;

        fc.setSelectedFile(new File(selDir + fileName));
        List<FileFilter> flaFilters = new ArrayList<>();
        List<FileFilter> xflFilters = new ArrayList<>();
        List<FLAVersion> versions = new ArrayList<>();
        boolean isAS3 = swf.isAS3();
        Map<FileFilter, String> filterToVersion = new HashMap<>();
        Map<FileFilter, Boolean> filterToCompressed = new HashMap<>();

        Map<FileFilter, FLAVersion> filterToFlaVersion = new HashMap<>();

        FLAVersion lastVersion = FLAVersion.fromString(Configuration.lastFlaExportVersion.get("CS6"));
        boolean lastCompressed = Configuration.lastFlaExportCompressed.get(true);

        for (int i = FLAVersion.values().length - 1; i >= 0; i--) {
            final FLAVersion v = FLAVersion.values()[i];
            if (!isAS3 && v.minASVersion() > 2) {
                // This version does not support AS1/2
            } else {
                versions.add(v);
                FileFilter f = new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        return f.isDirectory() || (f.getName().toLowerCase(Locale.ENGLISH).endsWith(".fla"));
                    }

                    @Override
                    public String getDescription() {
                        return translate("filter.fla").replace("%version%", v.applicationName());
                    }
                };
                if (v == lastVersion && lastCompressed) {
                    fc.setFileFilter(f);
                } else {
                    fc.addChoosableFileFilter(f);
                }
                filterToFlaVersion.put(f, v);
                filterToVersion.put(f, "" + v);
                filterToCompressed.put(f, true);
                flaFilters.add(f);

                if (v.xflVersion() != null) {
                    f = new FileFilter() {
                        @Override
                        public boolean accept(File f) {
                            return f.isDirectory() || (f.getName().toLowerCase(Locale.ENGLISH).endsWith(".xfl"));
                        }

                        @Override
                        public String getDescription() {
                            return translate("filter.xfl").replace("%version%", v.applicationName());
                        }
                    };
                    filterToFlaVersion.put(f, v);
                    filterToVersion.put(f, "" + v);
                    filterToCompressed.put(f, false);

                    if (v == lastVersion && !lastCompressed) {
                        fc.setFileFilter(f);
                    } else {
                        fc.addChoosableFileFilter(f);
                    }
                    xflFilters.add(f);
                }
            }
        }

        fc.setAcceptAllFileFilterUsed(false);
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            Configuration.lastFlaExportVersion.set(filterToVersion.get(fc.getFileFilter()));
            Configuration.lastFlaExportCompressed.set(filterToCompressed.get(fc.getFileFilter()));
            Configuration.lastOpenDir.set(Helper.fixDialogFile(fc.getSelectedFile()).getParentFile().getAbsolutePath());
            File sf = Helper.fixDialogFile(fc.getSelectedFile());

            FileFilter selectedFilter = fc.getFileFilter();
            final boolean compressed = flaFilters.contains(selectedFilter);
            String path = sf.getAbsolutePath();
            if (path.endsWith(".fla") || path.endsWith(".xfl")) {
                path = path.substring(0, path.length() - 4);
            }
            path += compressed ? ".fla" : ".xfl";
            final FLAVersion selectedVersion = filterToFlaVersion.get(selectedFilter);
            final File selfile = new File(path);
            long timeBefore = System.currentTimeMillis();
            new CancellableWorker("exportFla") {
                @Override
                protected Void doInBackground() throws Exception {
                    Helper.freeMem();

                    CancellableWorker w = this;

                    ProgressListener prog = new ProgressListener() {
                        @Override
                        public void progress(int p) {
                        }

                        @Override
                        public void status(String status) {
                            Main.startWork(translate("work.exporting.fla") + "..." + status, w);
                        }
                    };

                    try {
                        AbortRetryIgnoreHandler errorHandler = new GuiAbortRetryIgnoreHandler();
                        if (compressed) {
                            swf.exportFla(errorHandler, selfile.getAbsolutePath(), fSwfShortName, ApplicationInfo.APPLICATION_NAME, ApplicationInfo.applicationVerName, ApplicationInfo.version, Configuration.parallelSpeedUp.get(), selectedVersion, prog);
                        } else {
                            swf.exportXfl(errorHandler, selfile.getAbsolutePath(), fSwfShortName, ApplicationInfo.APPLICATION_NAME, ApplicationInfo.applicationVerName, ApplicationInfo.version, Configuration.parallelSpeedUp.get(), selectedVersion, prog);
                        }
                    } catch (Exception ex) {
                        logger.log(Level.SEVERE, "FLA export error", ex);
                        ViewMessages.showMessageDialog(MainPanel.this, translate("error.export") + ": " + ex.getClass().getName() + " " + ex.getLocalizedMessage(), translate("error"), JOptionPane.ERROR_MESSAGE);
                    }
                    Helper.freeMem();
                    return null;
                }

                @Override
                protected void onStart() {
                    Main.startWork(translate("work.exporting.fla") + "...", this);
                }

                @Override
                protected void done() {
                    Main.stopWork();
                    long timeAfter = System.currentTimeMillis();
                    final long timeMs = timeAfter - timeBefore;

                    View.execInEventDispatch(() -> {
                        setStatus(translate("export.finishedin").replace("%time%", Helper.formatTimeSec(timeMs)));
                    });

                    if (Configuration.openFolderAfterFlaExport.get()) {
                        try {
                            Desktop.getDesktop().open(selfile.getAbsoluteFile().getParentFile());
                        } catch (IOException ex) {
                            logger.log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }.execute();
        }
    }

    public void importMovie(final SWF swf) {
        ViewMessages.showMessageDialog(MainPanel.this, translate("message.info.importMovies2"), translate("message.info"), JOptionPane.INFORMATION_MESSAGE, Configuration.showImportMovieInfo);
        JFileChooser chooser = View.getFileChooserWithIcon("importmovie");
        chooser.setCurrentDirectory(new File(Configuration.lastExportDir.get()));
        chooser.setDialogTitle(translate("import.select.directory"));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {

            previewPanel.clear();

            String selFile = Helper.fixDialogFile(chooser.getSelectedFile()).getAbsolutePath();
            File moviesDir = new File(Path.combine(selFile, MovieExportSettings.EXPORT_FOLDER_NAME));
            if (!moviesDir.exists()) {
                moviesDir = new File(selFile);
            }
            final File fMoviesDir = moviesDir;
            MovieImporter movieImporter = new MovieImporter();

            final long timeBefore = System.currentTimeMillis();
            new CancellableWorker<Void>("importMovie") {

                private int count = 0;

                @Override
                public Void doInBackground() throws Exception {
                    try {
                        count = movieImporter.bulkImport(fMoviesDir, swf, false);
                    } catch (Exception ex) {
                        logger.log(Level.SEVERE, "Error during import", ex);
                        ViewMessages.showMessageDialog(null, translate("error.import") + ": " + ex.getClass().getName() + " " + ex.getLocalizedMessage());
                    }
                    return null;
                }

                @Override
                protected void onStart() {
                    Main.startWork(translate("work.importing") + "...", this);
                }

                @Override
                protected void done() {
                    Main.stopWork();
                    long timeAfter = System.currentTimeMillis();
                    final long timeMs = timeAfter - timeBefore;

                    View.execInEventDispatch(() -> {
                        refreshTree(swf);
                        setStatus(translate("import.finishedin").replace("%time%", Helper.formatTimeSec(timeMs)));

                        ViewMessages.showMessageDialog(MainPanel.this, translate("import.movie.result").replace("%count%", Integer.toString(count)));
                        if (count != 0) {
                            reload(true);
                        }
                    });
                }
            }.execute();
        }
    }

    public void importSound(final SWF swf) {
        ViewMessages.showMessageDialog(MainPanel.this, translate("message.info.importSounds2"), translate("message.info"), JOptionPane.INFORMATION_MESSAGE, Configuration.showImportSoundInfo);
        JFileChooser chooser = View.getFileChooserWithIcon("importsound");
        chooser.setCurrentDirectory(new File(Configuration.lastExportDir.get()));
        chooser.setDialogTitle(translate("import.select.directory"));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {

            previewPanel.clear();

            String selFile = Helper.fixDialogFile(chooser.getSelectedFile()).getAbsolutePath();
            File soundsDir = new File(Path.combine(selFile, SoundExportSettings.EXPORT_FOLDER_NAME));
            if (!soundsDir.exists()) {
                soundsDir = new File(selFile);
            }
            final File fSoundsDir = soundsDir;
            SoundImporter soundImporter = new SoundImporter();

            final long timeBefore = System.currentTimeMillis();
            new CancellableWorker<Void>("importSound") {

                private int count = 0;

                @Override
                public Void doInBackground() throws Exception {
                    try {
                        count = soundImporter.bulkImport(fSoundsDir, swf, false);
                    } catch (Exception ex) {
                        logger.log(Level.SEVERE, "Error during import", ex);
                        ViewMessages.showMessageDialog(null, translate("error.import") + ": " + ex.getClass().getName() + " " + ex.getLocalizedMessage());
                    }
                    return null;
                }

                @Override
                protected void onStart() {
                    Main.startWork(translate("work.importing") + "...", this);
                }

                @Override
                protected void done() {
                    Main.stopWork();
                    long timeAfter = System.currentTimeMillis();
                    final long timeMs = timeAfter - timeBefore;

                    View.execInEventDispatch(() -> {
                        refreshTree(swf);
                        setStatus(translate("import.finishedin").replace("%time%", Helper.formatTimeSec(timeMs)));

                        ViewMessages.showMessageDialog(MainPanel.this, translate("import.sound.result").replace("%count%", Integer.toString(count)));
                        if (count != 0) {
                            reload(true);
                        }
                    });
                }
            }.execute();
        }
    }

    public void importSprite(final SWF swf) {
        ViewMessages.showMessageDialog(MainPanel.this, translate("message.info.importSprites"), translate("message.info"), JOptionPane.INFORMATION_MESSAGE, Configuration.showImportSpriteInfo);
        JFileChooser chooser = View.getFileChooserWithIcon("importsprite");
        chooser.setCurrentDirectory(new File(Configuration.lastExportDir.get()));
        chooser.setDialogTitle(translate("import.select.directory"));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            String selFile = Helper.fixDialogFile(chooser.getSelectedFile()).getAbsolutePath();
            File spritesDir = new File(Path.combine(selFile, SpriteExportSettings.EXPORT_FOLDER_NAME));
            if (!spritesDir.exists()) {
                spritesDir = new File(selFile);
            }
            final File fSpritesDir = spritesDir;
            SpriteImporter spriteImporter = new SpriteImporter();

            final long timeBefore = System.currentTimeMillis();
            new CancellableWorker<Void>("importSprite") {

                private int count = 0;

                @Override
                public Void doInBackground() throws Exception {
                    try {
                        count = spriteImporter.bulkImport(fSpritesDir, swf, false);
                        swf.clearImageCache();
                        swf.clearShapeCache();
                    } catch (Exception ex) {
                        logger.log(Level.SEVERE, "Error during import", ex);
                        ViewMessages.showMessageDialog(null, translate("error.import") + ": " + ex.getClass().getName() + " " + ex.getLocalizedMessage());
                    }
                    return null;
                }

                @Override
                protected void onStart() {
                    Main.startWork(translate("work.importing") + "...", this);
                }

                @Override
                protected void done() {
                    Main.stopWork();
                    long timeAfter = System.currentTimeMillis();
                    final long timeMs = timeAfter - timeBefore;

                    View.execInEventDispatch(() -> {
                        refreshTree(swf);
                        setStatus(translate("import.finishedin").replace("%time%", Helper.formatTimeSec(timeMs)));

                        ViewMessages.showMessageDialog(MainPanel.this, translate("import.sprite.result").replace("%count%", Integer.toString(count)));
                        if (count != 0) {
                            reload(true);
                        }
                    });
                }
            }.execute();
        }
    }

    public void importShape(final SWF swf, boolean noFill) {
        ViewMessages.showMessageDialog(MainPanel.this, translate("message.info.importShapes2"), translate("message.info"), JOptionPane.INFORMATION_MESSAGE, Configuration.showImportShapeInfo);
        JFileChooser chooser = View.getFileChooserWithIcon("importshape");
        chooser.setCurrentDirectory(new File(Configuration.lastExportDir.get()));
        chooser.setDialogTitle(translate("import.select.directory"));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            String selFile = Helper.fixDialogFile(chooser.getSelectedFile()).getAbsolutePath();
            File shapesDir = new File(Path.combine(selFile, ShapeExportSettings.EXPORT_FOLDER_NAME));
            if (!shapesDir.exists()) {
                shapesDir = new File(selFile);
            }
            final File fShapesDir = shapesDir;
            ShapeImporter shapeImporter = new ShapeImporter();
            SvgImporter svgImporter = new SvgImporter();

            final long timeBefore = System.currentTimeMillis();
            new CancellableWorker<Void>("importShape") {

                private int count = 0;

                @Override
                public Void doInBackground() throws Exception {
                    try {
                        count = shapeImporter.bulkImport(fShapesDir, swf, noFill, false);
                        swf.clearImageCache();
                        swf.clearShapeCache();
                    } catch (Exception ex) {
                        logger.log(Level.SEVERE, "Error during import", ex);
                        ViewMessages.showMessageDialog(null, translate("error.import") + ": " + ex.getClass().getName() + " " + ex.getLocalizedMessage());
                    }
                    return null;
                }

                @Override
                protected void onStart() {
                    Main.startWork(translate("work.importing") + "...", this);
                }

                @Override
                protected void done() {
                    Main.stopWork();
                    long timeAfter = System.currentTimeMillis();
                    final long timeMs = timeAfter - timeBefore;

                    View.execInEventDispatch(() -> {
                        refreshTree(swf);
                        setStatus(translate("import.finishedin").replace("%time%", Helper.formatTimeSec(timeMs)));

                        ViewMessages.showMessageDialog(MainPanel.this, translate("import.shape.result").replace("%count%", Integer.toString(count)));
                        if (count != 0) {
                            reload(true);
                        }
                    });
                }
            }.execute();
        }
    }

    public void importImage(final SWF swf) {
        ViewMessages.showMessageDialog(MainPanel.this, translate("message.info.importImages2"), translate("message.info"), JOptionPane.INFORMATION_MESSAGE, Configuration.showImportImageInfo);
        JFileChooser chooser = View.getFileChooserWithIcon("importimage");
        chooser.setCurrentDirectory(new File(Configuration.lastExportDir.get()));
        chooser.setDialogTitle(translate("import.select.directory"));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            String selFile = Helper.fixDialogFile(chooser.getSelectedFile()).getAbsolutePath();
            File imagesDir = new File(Path.combine(selFile, ImageExportSettings.EXPORT_FOLDER_NAME));
            if (!imagesDir.exists()) {
                imagesDir = new File(selFile);
            }
            final File fImagesDir = imagesDir;
            ImageImporter imageImporter = new ImageImporter();

            final long timeBefore = System.currentTimeMillis();
            new CancellableWorker<Void>("importImage") {

                private int count = 0;

                @Override
                public Void doInBackground() throws Exception {
                    try {
                        count = imageImporter.bulkImport(fImagesDir, swf, false);
                        swf.clearImageCache();
                    } catch (Exception ex) {
                        logger.log(Level.SEVERE, "Error during import", ex);
                        ViewMessages.showMessageDialog(null, translate("error.import") + ": " + ex.getClass().getName() + " " + ex.getLocalizedMessage());
                    }
                    return null;
                }

                @Override
                protected void onStart() {
                    Main.startWork(translate("work.importing") + "...", this);
                }

                @Override
                protected void done() {
                    Main.stopWork();
                    long timeAfter = System.currentTimeMillis();
                    final long timeMs = timeAfter - timeBefore;

                    View.execInEventDispatch(() -> {
                        refreshTree(swf);
                        setStatus(translate("import.finishedin").replace("%time%", Helper.formatTimeSec(timeMs)));

                        ViewMessages.showMessageDialog(MainPanel.this, translate("import.image.result").replace("%count%", Integer.toString(count)));
                        if (count != 0) {
                            reload(true);
                        }
                    });
                }
            }.execute();
        }
    }

    public void importText(final SWF swf) {
        ViewMessages.showMessageDialog(MainPanel.this, translate("message.info.importTexts2"), translate("message.info"), JOptionPane.INFORMATION_MESSAGE, Configuration.showImportTextInfo);
        JFileChooser chooser = View.getFileChooserWithIcon("importtext");
        chooser.setCurrentDirectory(new File(Configuration.lastExportDir.get()));
        chooser.setDialogTitle(translate("import.select.directory"));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            String selFile = Helper.fixDialogFile(chooser.getSelectedFile()).getAbsolutePath();
            boolean textsFolderExists = new File(Path.combine(selFile, TextExportSettings.EXPORT_FOLDER_NAME)).exists();
            File textsFile = new File(Path.combine(selFile, TextExportSettings.EXPORT_FOLDER_NAME, TextExporter.TEXT_EXPORT_FILENAME_FORMATTED));
            if (!textsFolderExists) {
                textsFile = new File(Path.combine(selFile, TextExporter.TEXT_EXPORT_FILENAME_FORMATTED));
            }
            TextImporter textImporter = new TextImporter(getMissingCharacterHandler(), new TextImportErrorHandler() {
                // "configuration items" for the current replace only
                private final ConfigurationItem<Boolean> showAgainImportError = new ConfigurationItem<>("showAgainImportError", true, true);

                private final ConfigurationItem<Boolean> showAgainInvalidText = new ConfigurationItem<>("showAgainInvalidText", true, true);

                private String getTextTagInfo(TextTag textTag) {
                    StringBuilder ret = new StringBuilder();
                    if (textTag != null) {
                        ret.append(" TextId: ").append(textTag.getCharacterId()).append(" (").append(String.join(", ", textTag.getTexts())).append(")");
                    }

                    return ret.toString();
                }

                @Override
                public boolean handle(TextTag textTag) {
                    String msg = translate("error.text.import");
                    logger.log(Level.SEVERE, "{0}{1}", new Object[]{msg, getTextTagInfo(textTag)});
                    return ViewMessages.showConfirmDialog(MainPanel.this, msg, translate("error"), JOptionPane.OK_CANCEL_OPTION, showAgainImportError, JOptionPane.OK_OPTION) != JOptionPane.OK_OPTION;
                }

                @Override
                public boolean handle(TextTag textTag, String message, long line) {
                    String msg = translate("error.text.invalid.continue").replace("%text%", message).replace("%line%", Long.toString(line));
                    logger.log(Level.SEVERE, "{0}{1}", new Object[]{msg, getTextTagInfo(textTag)});
                    return ViewMessages.showConfirmDialog(MainPanel.this, msg, translate("error"), JOptionPane.OK_CANCEL_OPTION, showAgainInvalidText, JOptionPane.OK_OPTION) != JOptionPane.OK_OPTION;
                }
            });

            // try to import formatted texts
            if (textsFile.exists()) {
                textImporter.importTextsSingleFileFormatted(textsFile, swf);
            } else {
                textsFile = new File(Path.combine(selFile, TextExportSettings.EXPORT_FOLDER_NAME, TextExporter.TEXT_EXPORT_FILENAME_PLAIN));
                if (!textsFolderExists) {
                    textsFile = new File(Path.combine(selFile, TextExporter.TEXT_EXPORT_FILENAME_PLAIN));
                }
                // try to import plain texts
                if (textsFile.exists()) {
                    textImporter.importTextsSingleFile(textsFile, swf);
                } else {
                    textImporter.importTextsMultipleFiles(selFile, swf);
                }
            }

            swf.clearImageCache();
            reload(true);
            updateMissingNeededCharacters();
        }
    }

    public As3ScriptReplacerInterface getAs3ScriptReplacer(boolean air) {
        As3ScriptReplacerInterface r = As3ScriptReplacerFactory.createByConfig(air);
        if (!r.isAvailable()) {
            if (r instanceof MxmlcAs3ScriptReplacer) {
                if (ViewMessages.showConfirmDialog(this, AppStrings.translate("message.flexpath.notset"), AppStrings.translate("error"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {
                    Main.advancedSettings("paths");
                }
            } else if (r instanceof FFDecAs3ScriptReplacer) {
                FFDecAs3ScriptReplacer fr = (FFDecAs3ScriptReplacer) r;
                if (fr.isAir()) {
                    if (ViewMessages.showConfirmDialog(this, AppStrings.translate("message.airpath.lib.notset"), AppStrings.translate("message.action.airglobal.title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {
                        Main.advancedSettings("paths");
                    }
                } else {
                    if (ViewMessages.showConfirmDialog(this, AppStrings.translate("message.playerpath.lib.notset"), AppStrings.translate("message.action.playerglobal.title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {
                        Main.advancedSettings("paths");
                    }
                }
            } else {
                //Not translated yet - just in case there are more Script replacers in the future. Unused now.
                ViewMessages.showConfirmDialog(this, "Current script replacer is not available", "Script replacer not available", JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE);
            }
            return null;
        }
        return r;
    }

    public void importScript(final Openable openable) {
        As3ScriptReplacerInterface as3ScriptReplacer = getAs3ScriptReplacer(Main.isSwfAir(openable));
        if (as3ScriptReplacer == null) {
            return;
        }
        ViewMessages.showMessageDialog(MainPanel.this, translate("message.info.importScripts2"), translate("message.info"), JOptionPane.INFORMATION_MESSAGE, Configuration.showImportScriptsInfo);

        JFileChooser chooser = View.getFileChooserWithIcon("importscript");
        chooser.setCurrentDirectory(new File(Configuration.lastExportDir.get()));
        chooser.setDialogTitle(translate("import.select.directory"));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            String selFile = Helper.fixDialogFile(chooser.getSelectedFile()).getAbsolutePath();
            String scriptsFolder = Path.combine(selFile, ScriptExportSettings.EXPORT_FOLDER_NAME);
            if (!new File(scriptsFolder).exists()) {
                scriptsFolder = selFile;
            }
            final String fScriptsFolder = scriptsFolder;
            final long timeBefore = System.currentTimeMillis();
            new CancellableWorker<Void>("importScript") {
                private int countAs2 = 0;
                private int countAs3 = 0;

                @Override
                public Void doInBackground() throws Exception {

                    SWF swf = (openable instanceof SWF) ? (SWF) openable : ((ABC) openable).getSwf();

                    new AS2ScriptImporter().importScripts(fScriptsFolder, swf.getASMs(true), new ScriptImporterProgressListener() {
                        @Override
                        public void scriptImported() {
                            countAs2++;
                        }

                        @Override
                        public void scriptImportError() {

                        }
                    });

                    List<ScriptPack> packs;
                    if (openable instanceof SWF) {
                        packs = swf.getAS3Packs();
                    } else {
                        List<ABC> allAbcs = new ArrayList<>();
                        ABC abc = (ABC) openable;
                        allAbcs.add(abc);
                        packs = abc.getScriptPacks(null, allAbcs);
                    }

                    new AS3ScriptImporter().importScripts(as3ScriptReplacer, fScriptsFolder, packs, new ScriptImporterProgressListener() {
                        @Override
                        public void scriptImported() {
                            countAs3++;
                        }

                        @Override
                        public void scriptImportError() {
                        }
                    },
                            Main.getDependencies(swf)
                    );

                    if (countAs3 > 0) {
                        updateClassesList();
                    }
                    return null;
                }

                @Override
                protected void onStart() {
                    Main.importWorker = this;
                    Main.startWork(translate("work.importing_as") + "...", this);
                }

                @Override
                protected void done() {
                    Main.stopWork();
                    long timeAfter = System.currentTimeMillis();
                    final long timeMs = timeAfter - timeBefore;

                    Main.importWorker = null;
                    View.execInEventDispatch(() -> {
                        setStatus(translate("importing_as.finishedin").replace("%time%", Helper.formatTimeSec(timeMs)));

                        ViewMessages.showMessageDialog(MainPanel.this, translate("import.script.result").replace("%count%", Integer.toString(countAs2 + countAs3)));
                        if (countAs2 != 0 || countAs3 != 0) {
                            reload(true);
                            updateMissingNeededCharacters();
                        }
                    });
                }
            }.execute();

        }
    }

    public void importSymbolClass(final SWF swf) {
        ViewMessages.showMessageDialog(MainPanel.this, translate("message.info.importSymbolClass").replace("%file%", SymbolClassExporter.SYMBOL_CLASS_EXPORT_FILENAME), translate("message.info"), JOptionPane.INFORMATION_MESSAGE, Configuration.showImportSymbolClassInfo);

        JFileChooser chooser = View.getFileChooserWithIcon("importsymbolclass");
        chooser.setCurrentDirectory(new File(Configuration.lastExportDir.get()));
        chooser.setDialogTitle(translate("import.select.directory"));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            String selFile = Helper.fixDialogFile(chooser.getSelectedFile()).getAbsolutePath();
            File importFile = new File(Path.combine(selFile, SymbolClassExporter.SYMBOL_CLASS_EXPORT_FILENAME));
            SymbolClassImporter importer = new SymbolClassImporter();

            if (importFile.exists()) {
                importer.importSymbolClasses(importFile, swf);
            }
        }
    }

    private String selectExportDir(String icon) {
        JFileChooser chooser = View.getFileChooserWithIcon(icon);
        chooser.setCurrentDirectory(new File(Configuration.lastExportDir.get()));
        chooser.setDialogTitle(translate("export.select.directory"));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            final String selFile = Helper.fixDialogFile(chooser.getSelectedFile()).getAbsolutePath();
            Configuration.lastExportDir.set(Helper.fixDialogFile(chooser.getSelectedFile()).getAbsolutePath());
            return selFile;
        }
        return null;
    }

    public void export(final boolean onlySel, List<TreeItem> selected) {
        View.checkAccess();

        final SWF swf = getCurrentSwf();
        List<TreeItem> sel = getCurrentTree().getAllSubsForItems(selected);
        if (!onlySel) {
            sel = null;
        } else if (sel.isEmpty()) {
            return;
        }
        final ExportDialog export = new ExportDialog(Main.getDefaultDialogsOwner(), sel);
        if (export.showExportDialog() == AppDialog.OK_OPTION) {
            final String selFile = selectExportDir("export");
            if (selFile != null) {
                final long timeBefore = System.currentTimeMillis();

                new CancellableWorker<Void>("export") {
                    @Override
                    public Void doInBackground() throws Exception {
                        try {
                            AbortRetryIgnoreHandler errorHandler = new GuiAbortRetryIgnoreHandler();
                            if (onlySel) {
                                exportSelection(selected, errorHandler, selFile, export);
                            } else {
                                exportAll(swf, errorHandler, selFile, export);
                            }
                        } catch (Exception ex) {
                            logger.log(Level.SEVERE, "Error during export", ex);
                            ViewMessages.showMessageDialog(null, translate("error.export") + ": " + ex.getClass().getName() + " " + ex.getLocalizedMessage());
                        }
                        return null;
                    }

                    @Override
                    protected void onStart() {
                        Main.startWork(translate("work.exporting") + "...", this);
                    }

                    @Override
                    protected void done() {
                        Main.stopWork();
                        long timeAfter = System.currentTimeMillis();
                        final long timeMs = timeAfter - timeBefore;

                        View.execInEventDispatch(() -> {
                            setStatus(translate("export.finishedin").replace("%time%", Helper.formatTimeSec(timeMs)));
                        });
                    }
                }.execute();

            }
        }
    }

    public void exportJavaSource(List<TreeItem> items) {
        Set<SWF> swfs = new LinkedHashSet<>();

        for (TreeItem item : items) {
            if (item instanceof OpenableList) {
                OpenableList list = (OpenableList) item;
                for (Openable openable : list) {
                    if (openable instanceof SWF) {
                        swfs.add((SWF) openable);
                    }
                }
            } else {
                Openable openable = item.getOpenable();
                if (openable instanceof SWF) {
                    swfs.add((SWF) openable);
                }
            }
        }

        for (SWF item : swfs) {
            SWF swf = (SWF) item;
            final String selFile = selectExportDir("exportjava");
            if (selFile != null) {
                Main.startWork(translate("work.exporting") + "...", null);

                try {
                    new SwfJavaExporter().exportJavaCode(swf, selFile);
                    Main.stopWork();
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public void exportSwfXml(List<TreeItem> items) {
        View.checkAccess();

        Set<SWF> swfs = new LinkedHashSet<>();

        for (TreeItem item : items) {
            if (item instanceof OpenableList) {
                OpenableList list = (OpenableList) item;
                for (Openable openable : list) {
                    if (openable instanceof SWF) {
                        swfs.add((SWF) openable);
                    }
                }
            } else {
                Openable openable = item.getOpenable();
                if (openable instanceof SWF) {
                    swfs.add((SWF) openable);
                }
            }
        }

        for (SWF swf : swfs) {
            final String selFile = selectExportDir("exportxml");
            if (selFile != null) {
                Main.startWork(translate("work.exporting") + "...", null);

                try {
                    File outFile = new File(selFile + File.separator + Helper.makeFileName("swf.xml"));
                    new SwfXmlExporter().exportXml(swf, outFile);
                    Main.stopWork();
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public void importSwfXml(List<TreeItem> items) {
        View.checkAccess();

        ViewMessages.showMessageDialog(MainPanel.this, translate("message.info.importXml"), translate("message.info"), JOptionPane.INFORMATION_MESSAGE, Configuration.showImportXmlInfo);

        Set<SWF> swfs = new LinkedHashSet<>();

        for (TreeItem item : items) {
            if (item instanceof OpenableList) {
                OpenableList list = (OpenableList) item;
                for (Openable openable : list) {
                    if (openable instanceof SWF) {
                        swfs.add((SWF) openable);
                    }
                }
            } else {
                Openable openable = item.getOpenable();
                if (openable instanceof SWF) {
                    swfs.add((SWF) openable);
                }
            }
        }
        if (swfs.size() > 1) {
            return;
        }

        for (SWF swf : swfs) {
            File selectedFile = showImportFileChooser("filter.xml|*.xml", false, "importxml");
            if (selectedFile != null) {
                File selfile = Helper.fixDialogFile(selectedFile);
                try {
                    try (FileInputStream fis = new FileInputStream(selfile)) {
                        new SwfXmlImporter().importSwf(swf, fis);
                    }
                    swf.clearAllCache();
                    swf.assignExportNamesToSymbols();
                    swf.assignClassesToSymbols();
                    refreshTree(swf);
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public void renameIdentifiers(final Openable openable) {
        View.checkAccess();

        SWF swf = null;

        if (openable instanceof SWF) {
            swf = (SWF) openable;
        }
        //FIXME for ABCs

        if (swf == null) {
            return;
        }

        final SWF fswf = swf;
        if (confirmExperimental()) {
            RenameDialog renameDialog = new RenameDialog(Main.getDefaultDialogsOwner());
            if (renameDialog.showRenameDialog() == AppDialog.OK_OPTION) {
                final RenameType renameType = renameDialog.getRenameType();
                new CancellableWorker<Integer>("renameIdentifiers") {
                    @Override
                    protected Integer doInBackground() throws Exception {
                        int cnt = fswf.deobfuscateIdentifiers(renameType);
                        return cnt;
                    }

                    @Override
                    protected void onStart() {
                        Main.startWork(translate("work.renaming.identifiers") + "...", this);
                    }

                    @Override
                    protected void done() {
                        View.execInEventDispatch(() -> {
                            try {
                                int cnt = get();
                                Main.stopWork();
                                ViewMessages.showMessageDialog(MainPanel.this, translate("message.rename.renamed").replace("%count%", Integer.toString(cnt)));
                                fswf.assignClassesToSymbols();
                                fswf.clearScriptCache();
                                if (abcPanel != null) {
                                    abcPanel.reload();
                                }
                                updateClassesList();
                                reload(true);
                            } catch (Exception ex) {
                                logger.log(Level.SEVERE, "Error during renaming identifiers", ex);
                                Main.stopWork();
                                ViewMessages.showMessageDialog(MainPanel.this, translate("error.occurred").replace("%error%", ex.getClass().getSimpleName()));
                            }
                        });
                    }
                }.execute();
            }
        }
    }

    private void deobfuscateMethod(Trait t, int scriptIndex, DeobfuscationLevel level, boolean isStatic, int methodIndex, ABC abc) throws InterruptedException {
        if (methodIndex == -1) {
            return;
        }
        MethodBody body = abc.findBody(methodIndex);
        if (body != null) {
            body.deobfuscate(level, t, scriptIndex, methodIndex, isStatic, "");
        }
    }

    private void deobfuscateTraits(int scriptIndex, DeobfuscationLevel level, List<Trait> traits, ABC abc, boolean isStatic) throws InterruptedException {
        for (Trait t : traits) {
            int methodIndex = -1;
            if (t instanceof TraitMethodGetterSetter) {
                methodIndex = ((TraitMethodGetterSetter) t).method_info;
            }
            if (t instanceof TraitFunction) {
                methodIndex = ((TraitFunction) t).method_info;
            }
            if (methodIndex != -1) {
                deobfuscateMethod(t, scriptIndex, level, isStatic, methodIndex, abc);
            }
            if (t instanceof TraitClass) {
                TraitClass tc = (TraitClass) t;
                ClassInfo ci = abc.class_info.get(tc.class_info);
                deobfuscateMethod(t, scriptIndex, level, true, ci.cinit_index, abc);
                deobfuscateTraits(scriptIndex, level, ci.static_traits.traits, abc, true);
                InstanceInfo ii = abc.instance_info.get(tc.class_info);
                deobfuscateMethod(t, scriptIndex, level, false, ii.iinit_index, abc);
                deobfuscateTraits(scriptIndex, level, ii.instance_traits.traits, abc, false);
            }
        }
    }

    public void deobfuscate() {
        View.checkAccess();

        DeobfuscationDialog deobfuscationDialog = new DeobfuscationDialog(Main.getDefaultDialogsOwner());
        if (deobfuscationDialog.showDialog() == AppDialog.OK_OPTION) {
            DeobfuscationLevel level = deobfuscationDialog.getDeobfuscationLevel();
            new CancellableWorker("deobfuscate") {
                @Override
                protected Void doInBackground() throws Exception {
                    try {
                        ABCPanel abcPanel = getABCPanel();
                        DeobfuscationScope scope = deobfuscationDialog.getDeobfuscationScope();
                        if (scope == DeobfuscationScope.SWF) {
                            SWF swf = abcPanel.getSwf();
                            swf.deobfuscate(level);
                        } else if (scope == DeobfuscationScope.CLASS) {
                            ScriptPack pack = abcPanel.getPack();
                            if (pack == null) {
                                return null;
                            }
                            List<Trait> traits = pack.abc.script_info.get(pack.scriptIndex).traits.traits;
                            deobfuscateMethod(null, pack.scriptIndex, level, true, pack.abc.script_info.get(pack.scriptIndex).init_index, pack.abc);
                            deobfuscateTraits(pack.scriptIndex, level, traits, pack.abc, true);
                        } else {
                            int mi = abcPanel.detailPanel.methodTraitPanel.methodCodePanel.getMethodIndex();
                            int bi = abcPanel.detailPanel.methodTraitPanel.methodCodePanel.getBodyIndex();
                            DecompiledEditorPane decompiledTextArea = abcPanel.decompiledTextArea;
                            Trait t = abcPanel.decompiledTextArea.getCurrentTrait();
                            ABC abc = abcPanel.abc;
                            if (bi != -1) {
                                int scriptIndex = decompiledTextArea.getScriptLeaf().scriptIndex;
                                int classIndex = decompiledTextArea.getClassIndex();
                                boolean isStatic = decompiledTextArea.getIsStatic();
                                abc.bodies.get(bi).deobfuscate(level, t, scriptIndex, classIndex, isStatic, ""/*FIXME*/);
                            }
                            abcPanel.detailPanel.methodTraitPanel.methodCodePanel.setMethod(decompiledTextArea.getScriptLeaf().getPathScriptName(), mi, bi, abc, t, abcPanel.detailPanel.methodTraitPanel.methodCodePanel.getScriptIndex());
                        }
                    } catch (Exception ex) {
                        logger.log(Level.SEVERE, "Deobfuscation error", ex);
                        ViewMessages.showMessageDialog(MainPanel.this, translate("error.deobfuscation"), translate("error"), JOptionPane.ERROR_MESSAGE);
                    }

                    return null;
                }

                @Override
                protected void onStart() {
                    Main.deobfuscatePCodeWorker = this;
                    Main.startWork(translate("work.deobfuscating") + "...", this);
                }

                @Override
                protected void done() {
                    Main.deobfuscatePCodeWorker = null;
                    View.execInEventDispatch(() -> {
                        Main.stopWork();
                        ViewMessages.showMessageDialog(MainPanel.this, translate("work.deobfuscating.complete"));

                        clearAllScriptCache();
                        getABCPanel().reload();
                        updateClassesList();
                    });
                }
            }.execute();
        }
    }

    public void removeNonScripts(SWF swf) {
        if (swf == null) {
            return;
        }

        List<Tag> tags = swf.getTags().toArrayList();
        List<Tag> toRemove = new ArrayList<>();
        for (Tag tag : tags) {
            System.out.println(tag.getClass());
            if (!(tag instanceof ABCContainerTag || tag instanceof ASMSource)) {
                toRemove.add(tag);
            }
        }

        swf.removeTags(toRemove, true, null);
        refreshTree(swf);
    }

    public void removeExceptSelected(SWF swf) {
        if (swf == null) {
            return;
        }

        List<TreeItem> sel = getAllSelected();
        Set<Integer> needed = new HashSet<>();
        for (TreeItem item : sel) {
            if (item instanceof CharacterTag) {
                CharacterTag characterTag = (CharacterTag) item;
                characterTag.getNeededCharactersDeep(needed);
                needed.add(characterTag.getCharacterId());
            }
        }

        List<Tag> tagsToRemove = new ArrayList<>();
        for (Tag tag : swf.getTags()) {
            if (tag instanceof CharacterTag) {
                CharacterTag characterTag = (CharacterTag) tag;
                if (!needed.contains(characterTag.getCharacterId())) {
                    tagsToRemove.add(tag);
                }
            }
        }

        swf.removeTags(tagsToRemove, true, null);
        refreshTree(swf);
    }

    private void clear() {
        dumpViewPanel.clear();
        previewPanel.clear();
        headerPanel.clear();
        folderPreviewPanel.clear();
        folderListPanel.clear();
        if (abcPanel != null) {
            abcPanel.clearSwf();
        }
        if (actionPanel != null) {
            actionPanel.clearSource();
        }
    }

    public void treeOperation(Runnable runnable) {
        TreeItem treeItem = getCurrentTree().getCurrentTreeItem();
        tagTree.clearSelection();
        tagListTree.clearSelection();
        runnable.run();
        clear();
        showCard(CARDEMPTYPANEL);

        tagTree.updateSwfs(new SWF[0]);
        tagListTree.updateSwfs(new SWF[0]);

        if (treeItem != null) {
            Openable openable = treeItem.getOpenable();
            if (openable != null) {
                if (openable instanceof SWF) {
                    SWF treeItemSwf = ((SWF) openable).getRootSwf();
                    if (this.openables.contains(treeItemSwf.openableList)) {
                        setTagTreeSelectedNode(getCurrentTree(), treeItem);
                    }
                } else {
                    if (this.openables.contains(openable.getOpenableList())) {
                        setTagTreeSelectedNode(getCurrentTree(), treeItem);
                    }
                }
            }
        }

        reload(true);
        refreshPins();
    }

    public void refreshTree() {
        refreshTree(new SWF[0]);
    }

    public void refreshTree(Openable openable) {
        refreshTree(new Openable[]{openable});
    }

    public void refreshTree(Openable[] openables) {
        String selectionPath = null;
        if (currentView == VIEW_RESOURCES || currentView == VIEW_TAGLIST) {
            selectionPath = getCurrentTree().getSelectionPathString();
        }

        clear();
        showCard(CARDEMPTYPANEL);

        tagTree.updateSwfs(openables);
        tagListTree.updateSwfs(openables);

        getCurrentTree().clearSelection();
        if (selectionPath != null) {
            getCurrentTree().setSelectionPathString(selectionPath);
        }
        reload(true);
        updateMissingNeededCharacters();
        refreshPins();
        updateUiWithCurrentOpenable();
    }

    public void refreshDecompiled() {
        clearAllScriptCache();
        if (abcPanel != null) {
            abcPanel.reload();
        }

        reload(true);
        updateClassesList();
    }

    private MissingCharacterHandler getMissingCharacterHandler() {
        return new MissingCharacterHandler() {
            // "configuration items" for the current replace only
            private final ConfigurationItem<Boolean> showAgainIgnoreMissingCharacters = new ConfigurationItem<>("showAgainIgnoreMissingCharacters", true, true);

            private boolean ignoreMissingCharacters = false;

            @Override
            public boolean getIgnoreMissingCharacters() {
                return ignoreMissingCharacters;
            }

            @Override
            public boolean handle(TextTag textTag, final FontTag font, final char character) {
                String fontName = font.getSwf().sourceFontNamesMap.get(font.getCharacterId());
                if (fontName == null) {
                    fontName = font.getFontName();
                }
                final Font f = FontTag.getInstalledFontsByName().get(fontName);
                if (f == null || !f.canDisplay(character)) {
                    String msg = translate("error.font.nocharacter").replace("%char%", "" + character);
                    logger.log(Level.SEVERE, "{0} FontId: {1} TextId: {2}", new Object[]{msg, font.getCharacterId(), textTag.getCharacterId()});
                    ignoreMissingCharacters = ViewMessages.showConfirmDialog(MainPanel.this, msg, translate("error"),
                            JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE,
                            showAgainIgnoreMissingCharacters,
                            ignoreMissingCharacters ? JOptionPane.OK_OPTION : JOptionPane.CANCEL_OPTION) == JOptionPane.OK_OPTION;
                    return false;
                }

                return font.addCharacter(character, f);
            }
        };
    }

    public boolean saveText(TextTag textTag, String formattedText, String[] texts, LineMarkedEditorPane editor) {
        try {
            if (textTag.setFormattedText(getMissingCharacterHandler(), formattedText, texts)) {
                return true;
            }
        } catch (TextParseException ex) {
            if (editor != null) {
                editor.gotoLine((int) ex.line);
                editor.markError();
            }

            ViewMessages.showMessageDialog(MainPanel.this, translate("error.text.invalid").replace("%text%", ex.text).replace("%line%", Long.toString(ex.line)), translate("error"), JOptionPane.ERROR_MESSAGE);
        }

        return false;
    }

    public boolean previousTag() {
        JTree tree = getCurrentTree();

        if (tree != null) {
            if (tree.getSelectionRows().length > 0) {
                int row = tree.getSelectionRows()[0];
                if (row > 0) {
                    tree.setSelectionRow(row - 1);
                    tree.scrollRowToVisible(row - 1);
                    previewPanel.focusTextPanel();
                }
            }
            return true;
        }

        return false;
    }

    public boolean nextTag() {
        JTree tree = getCurrentTree();

        if (tree != null) {
            if (tree.getSelectionRows().length > 0) {
                int row = tree.getSelectionRows()[0];
                if (row < tree.getRowCount() - 1) {
                    tree.setSelectionRow(row + 1);
                    tree.scrollRowToVisible(row + 1);
                    previewPanel.focusTextPanel();
                }
            }
            return true;
        }
        return false;
    }

    public void selectBkColorButtonActionPerformed(ActionEvent evt) {
        Color newColor = JColorChooser.showDialog(null, AppStrings.translate("dialog.selectbkcolor.title"), View.getSwfBackgroundColor());
        if (newColor != null) {
            View.setSwfBackgroundColor(newColor);
            reload(true);
        }
    }

    public void replaceButtonActionPerformed(List<TreeItem> items) {
        replace(items, false);
    }

    public boolean replaceMorphShape(MorphShapeTag morphShape, boolean create, boolean fill) {
        File fileStart = showImportFileChooser("filter.images|*.jpg;*.jpeg;*.gif;*.png;*.bmp;*.svg", true, AppStrings.translate("dialog.morphshape.startShape"), "importmorphshape");
        if (fileStart == null) {
            return false;
        }

        DefineShape4Tag shapeStart = new DefineShape4Tag(morphShape.getSwf());
        SWF.addTagBefore(shapeStart, morphShape);

        DefineShape4Tag shapeEnd = new DefineShape4Tag(morphShape.getSwf());
        SWF.addTagBefore(shapeEnd, morphShape);

        shapeStart.shapeBounds = Helper.deepCopy(morphShape.startBounds);
        shapeEnd.shapeBounds = Helper.deepCopy(morphShape.endBounds);

        if (morphShape instanceof DefineMorphShape2Tag) {
            DefineMorphShape2Tag ms2 = (DefineMorphShape2Tag) morphShape;
            shapeStart.edgeBounds = Helper.deepCopy(ms2.startEdgeBounds);
            shapeEnd.edgeBounds = Helper.deepCopy(ms2.endEdgeBounds);
        }

        File selfileStart = Helper.fixDialogFile(fileStart);
        byte[] dataStart = null;
        String svgTextStart = null;
        boolean svgWarningShown = false;
        if (".svg".equals(Path.getExtension(selfileStart))) {
            svgTextStart = Helper.readTextFile(selfileStart.getAbsolutePath());
            showSvgImportWarning();
            svgWarningShown = true;
        } else {
            dataStart = Helper.readFile(selfileStart.getAbsolutePath());
        }

        try {
            Tag newStartTag;
            if (svgTextStart != null) {
                newStartTag = new SvgImporter().importSvg(shapeStart, shapeEnd, svgTextStart, fill);
            } else {
                newStartTag = new ShapeImporter().importImage(shapeStart, dataStart, 0, fill);
            }
            newStartTag.getTimelined().removeTag(newStartTag);

            if (shapeEnd.shapes.shapeRecords.size() <= 1) {
                File fileEnd = showImportFileChooser("filter.images|*.jpg;*.jpeg;*.gif;*.png;*.bmp;*.svg", true, AppStrings.translate("dialog.morphshape.endShape"));

                if (fileEnd == null) {
                    fileEnd = fileStart;
                }

                File selfileEnd = Helper.fixDialogFile(fileEnd);
                byte[] dataEnd = null;
                String svgTextEnd = null;
                if (".svg".equals(Path.getExtension(selfileEnd))) {
                    svgTextEnd = Helper.readTextFile(selfileEnd.getAbsolutePath());
                    if (!svgWarningShown) {
                        showSvgImportWarning();
                    }
                } else {
                    dataEnd = Helper.readFile(selfileEnd.getAbsolutePath());
                }

                Tag newEndTag;
                if (svgTextEnd != null) {
                    newEndTag = new SvgImporter().importSvg(shapeEnd, svgTextEnd, fill);
                } else {
                    newEndTag = new ShapeImporter().importImage(shapeEnd, dataEnd, 0, fill);
                }
                newEndTag.getTimelined().removeTag(newEndTag);
            }

            DefineMorphShape2Tag newMorphShape = new DefineMorphShape2Tag(morphShape.getSwf());
            newMorphShape.setTimelined(morphShape.getTimelined());
            SWF.addTagBefore(newMorphShape, morphShape);

            MorphShapeGenerator gen = new MorphShapeGenerator();
            try {
                gen.generate(newMorphShape, shapeStart, shapeEnd);
            } catch (StyleMismatchException sme) {
                newMorphShape.getTimelined().removeTag(newMorphShape);
                SWF swf = morphShape.getSwf();
                swf.resetTimeline();
                refreshTree(swf);
                ViewMessages.showMessageDialog(this, AppStrings.translate("error.morphshape.incompatible"), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
                return false;
            }
            SWF swf = newMorphShape.getSwf();

            morphShape.getTimelined().removeTag(morphShape);
            newMorphShape.setCharacterId(morphShape.getCharacterId());
            swf.updateCharacters();
            swf.resetTimeline();
            refreshTree(swf);
            setTagTreeSelectedNode(getCurrentTree(), newMorphShape);
            swf.clearImageCache();
            swf.clearShapeCache();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Invalid image", ex);
            ViewMessages.showMessageDialog(MainPanel.this, translate("error.image.invalid"), translate("error"), JOptionPane.ERROR_MESSAGE);
        }
        reload(true);
        return true;
    }

    public boolean replace(List<TreeItem> items, boolean create) {
        return replace(items, create, true);
    }

    public boolean replace(List<TreeItem> items, boolean create, boolean fill) {
        if (items.isEmpty()) {
            return false;
        }

        TreeItem ti0 = items.get(0);
        File file = null;
        if (ti0 instanceof SoundTag) {
            file = showImportFileChooser("filter.sounds|*.mp3;*.wav|filter.sounds.mp3|*.mp3|filter.sounds.wav|*.wav", false, "importsound");
        }
        if (ti0 instanceof ImageTag) {
            file = showImportFileChooser("filter.images|*.jpg;*.jpeg;*.gif;*.png;*.bmp", true, "importimage");
        }
        if (ti0 instanceof ShapeTag) {
            file = showImportFileChooser("filter.images|*.jpg;*.jpeg;*.gif;*.png;*.bmp;*.svg", true, "importshape");
        }
        if (ti0 instanceof MorphShapeTag) {
            return replaceMorphShape((MorphShapeTag) ti0, create, true);
        }
        if (ti0 instanceof DefineVideoStreamTag) {
            file = showImportFileChooser("filter.movies|*.flv", false, "importmovie");
        }
        if (ti0 instanceof DefineBinaryDataTag) {
            file = showImportFileChooser("", false, "importbinarydata");
        }
        if (ti0 instanceof UnknownTag) {
            file = showImportFileChooser("", false, "importother");
        }
        if (file == null) {
            return false;
        }
        for (TreeItem ti : items) {
            doReplaceAction(ti, file, create, fill);
        }
        return true;
    }

    private void doReplaceAction(TreeItem item, File selectedFile, boolean create, boolean fill) {
        if (selectedFile == null) {
            return;
        }
        if (item instanceof SoundTag) {
            File selfile = Helper.fixDialogFile(selectedFile);
            SoundTag st = (SoundTag) item;
            int soundFormat = SoundFormat.FORMAT_UNCOMPRESSED_LITTLE_ENDIAN;
            if (selfile.getName().toLowerCase(Locale.ENGLISH).endsWith(".mp3")) {
                soundFormat = SoundFormat.FORMAT_MP3;
            }

            SoundImporter soundImporter = new SoundImporter();

            boolean ok = false;
            try {
                ok = soundImporter.importSound(st, new FileInputStream(selfile), soundFormat);
                ((Tag) st).getSwf().clearSoundCache();
            } catch (IOException ex) {
                //ignore
            } catch (UnsupportedSamplingRateException ex) {
                String samplingRateKhz = "" + (ex.getSoundRate() / 1000.0) + " kHz";
                List<String> supportedRatesKhz = new ArrayList<>();
                for (int rate : ex.getSupportedRates()) {
                    supportedRatesKhz.add("" + (rate / 1000.0) + " kHz");
                }
                ViewMessages.showMessageDialog(this, translate("error.sound.rate").replace("%saplingRate%", samplingRateKhz).replace("%supportedRates%", String.join(", ", supportedRatesKhz)), translate("error"), JOptionPane.ERROR_MESSAGE);
                return;
            } catch (SoundImportException ex) {
                //ignore
            }

            if (!ok) {
                ViewMessages.showMessageDialog(this, translate("error.sound.invalid"), translate("error"), JOptionPane.ERROR_MESSAGE);
            } else {
                refreshTree(((Tag) st).getSwf());
                reload(true);
            }
        }
        if (item instanceof ImageTag) {
            ImageTag it = (ImageTag) item;
            if (it.importSupported()) {
                File selfile = Helper.fixDialogFile(selectedFile);
                byte[] data = Helper.readFile(selfile.getAbsolutePath());
                try {
                    Tag newTag = new ImageImporter().importImage(it, data, create ? -1 : 0);
                    SWF swf = it.getSwf();
                    refreshTree(swf);
                    if (newTag != null) {
                        setTagTreeSelectedNode(getCurrentTree(), newTag);
                    }
                    swf.clearImageCache();
                    swf.clearShapeCache();
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, "Invalid image", ex);
                    ViewMessages.showMessageDialog(this, translate("error.image.invalid"), translate("error"), JOptionPane.ERROR_MESSAGE);
                }

                reload(true);
            }
        }
        if ((item instanceof ShapeTag) || (item instanceof MorphShapeTag)) {
            Tag st = (Tag) item;
            File selfile = Helper.fixDialogFile(selectedFile);
            byte[] data = null;
            String svgText = null;
            if (".svg".equals(Path.getExtension(selfile))) {
                svgText = Helper.readTextFile(selfile.getAbsolutePath());
                showSvgImportWarning();
            } else {
                data = Helper.readFile(selfile.getAbsolutePath());
            }
            try {
                Tag newTag = null;
                if (st instanceof ShapeTag) {
                    if (svgText != null) {
                        newTag = new SvgImporter().importSvg((ShapeTag) st, svgText, fill);
                    } else {
                        newTag = new ShapeImporter().importImage((ShapeTag) st, data, 0, fill);
                    }
                }
                if (st instanceof MorphShapeTag) {
                    if (svgText != null) {
                        newTag = new SvgImporter().importSvg((MorphShapeTag) st, svgText, fill);
                    } else {
                        newTag = new ShapeImporter().importImage((MorphShapeTag) st, data, 0, fill);
                    }
                }
                SWF swf = st.getSwf();
                refreshTree(swf);
                if (newTag != null) {
                    setTagTreeSelectedNode(getCurrentTree(), newTag);
                }

                swf.clearImageCache();
                swf.clearShapeCache();
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "Invalid image", ex);
                ViewMessages.showMessageDialog(MainPanel.this, translate("error.image.invalid"), translate("error"), JOptionPane.ERROR_MESSAGE);
            }
            reload(true);
        }
        if (item instanceof DefineBinaryDataTag) {
            DefineBinaryDataTag bt = (DefineBinaryDataTag) item;
            File selfile = Helper.fixDialogFile(selectedFile);
            byte[] data = Helper.readFile(selfile.getAbsolutePath());
            new BinaryDataImporter().importData(bt, data);
            refreshTree(bt.getSwf());
            reload(true);
        }

        if (item instanceof UnknownTag) {
            UnknownTag ut = (UnknownTag) item;
            File selfile = Helper.fixDialogFile(selectedFile);
            byte[] data = Helper.readFile(selfile.getAbsolutePath());
            ut.unknownData = new ByteArrayRange(data);
            ut.setModified(true);
            refreshTree(ut.getSwf());
            reload(true);
        }

        if (item instanceof DefineVideoStreamTag) {
            previewPanel.clear();
            DefineVideoStreamTag movie = (DefineVideoStreamTag) item;
            File selfile = Helper.fixDialogFile(selectedFile);
            try {
                new MovieImporter().importMovie(movie, Helper.readFile(selfile.getAbsolutePath()));
                refreshTree();
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "Invalid movie", ex);
                ViewMessages.showMessageDialog(MainPanel.this, translate("error.movie.invalid") + ": " + ex.getMessage(), translate("error"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public boolean replaceSpriteWithGif(TreeItem item) {
        if (item == null) {
            return false;
        }
        if (item instanceof DefineSpriteTag) {
            String filter = "filter.images|*.gif";
            File selectedFile = showImportFileChooser(filter, false, "importsprite");
            if (selectedFile != null) {
                File selfile = Helper.fixDialogFile(selectedFile);
                DefineSpriteTag sprite = (DefineSpriteTag) item;
                SWF swf = sprite.getSwf();
                try (FileInputStream fis = new FileInputStream(selfile)) {
                    SpriteImporter spriteImporter = new SpriteImporter();
                    spriteImporter.importSprite((DefineSpriteTag) item, fis);

                    swf.clearImageCache();
                    swf.clearShapeCache();
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, "Invalid image", ex);
                    ViewMessages.showMessageDialog(this, translate("error.image.invalid"), translate("error"), JOptionPane.ERROR_MESSAGE);
                }
                reload(true);
                refreshTree(swf);
                return true;
            }
        }
        return false;
    }

    public void replaceSpriteWithGifButtonActionPerformed(TreeItem item) {
        replaceSpriteWithGif(item);
    }

    public void replaceNoFillButtonActionPerformed(List<TreeItem> items) {
        replace(items, false, false);
    }

    public boolean replaceNoFill(TreeItem item) {
        List<TreeItem> items = new ArrayList<>();
        items.add(item);
        return replace(items, false, false);
    }

    private void showSvgImportWarning() {
        ViewMessages.showMessageDialog(this, AppStrings.translate("message.warning.svgImportExperimental"), AppStrings.translate("message.warning"), JOptionPane.WARNING_MESSAGE, Configuration.warningSvgImport);
    }

    public void replaceAlphaButtonActionPerformed(ActionEvent evt) {
        TreeItem item = getCurrentTree().getCurrentTreeItem();
        if (item == null) {
            return;
        }

        if (item instanceof DefineBitsJPEG3Tag || item instanceof DefineBitsJPEG4Tag) {
            ImageTag it = (ImageTag) item;
            if (it.importSupported()) {
                File selectedFile = showImportFileChooser("", false, "replacealpha");
                if (selectedFile != null) {
                    File selfile = Helper.fixDialogFile(selectedFile);
                    byte[] data = Helper.readFile(selfile.getAbsolutePath());
                    try {
                        new ImageImporter().importImageAlpha(it, data);
                        SWF swf = it.getSwf();
                        swf.clearImageCache();
                        swf.clearShapeCache();
                    } catch (IOException ex) {
                        logger.log(Level.SEVERE, "Invalid alpha channel data", ex);
                        ViewMessages.showMessageDialog(this, translate("error.image.alpha.invalid"), translate("error"), JOptionPane.ERROR_MESSAGE);
                    }

                    reload(true);
                }
            }
        }
    }

    public void exportJavaSourceActionPerformed(List<TreeItem> items) {
        if (Main.isWorking()) {
            return;
        }

        exportJavaSource(items);
    }

    public void exportSwfXmlActionPerformed(List<TreeItem> items) {
        if (Main.isWorking()) {
            return;
        }

        exportSwfXml(items);
    }

    public void importSwfXmlActionPerformed(List<TreeItem> items) {
        if (Main.isWorking()) {
            return;
        }

        importSwfXml(items);
    }

    public void exportSelectionActionPerformed(List<TreeItem> selected) {
        if (Main.isWorking()) {
            return;
        }

        export(true, selected);
    }

    public File showImportFileChooser(String filter, boolean imagePreview, String icon) {
        return showImportFileChooser(filter, imagePreview, null, icon);
    }

    public File showImportFileChooser(String filter, boolean imagePreview, String title, String icon) {
        String[] filterArray = filter.length() > 0 ? filter.split("\\|") : new String[0];

        JFileChooser fc = View.getFileChooserWithIcon(icon);
        fc.setCurrentDirectory(new File(Configuration.lastOpenDir.get()));
        if (imagePreview) {
            fc.setAccessory(new FileChooserImagePreview(fc));
            Dimension preferredSize = new Dimension(fc.getPreferredSize());
            preferredSize.width += FileChooserImagePreview.PREVIEW_SIZE;
            fc.setPreferredSize(preferredSize);
        }
        boolean first = true;
        for (int i = 0; i < filterArray.length; i += 2) {
            final String filterName = filterArray[i];
            final String[] extensions = filterArray[i + 1].split(";");
            for (int j = 0; j < extensions.length; j++) {
                if (extensions[j].startsWith("*.")) {
                    extensions[j] = extensions[j].substring(1);
                }
            }
            FileFilter ff = new FileFilter() {
                @Override
                public boolean accept(File f) {
                    if (f.isDirectory()) {
                        return true;
                    }
                    String fileName = f.getName().toLowerCase(Locale.ENGLISH);
                    for (String ext : extensions) {
                        if (fileName.endsWith(ext)) {
                            return true;
                        }
                    }
                    return false;
                }

                @Override
                public String getDescription() {
                    StringBuilder extStr = new StringBuilder();
                    boolean first = true;
                    for (String ext : extensions) {
                        if (first) {
                            first = false;
                        } else {
                            extStr.append(",");
                        }

                        extStr.append("*").append(ext);
                    }

                    return translate(filterName).replace("%extensions%", extStr);
                }
            };
            if (first) {
                fc.setFileFilter(ff);
            } else {
                fc.addChoosableFileFilter(ff);
            }
            first = false;
        }

        if (title != null) {
            fc.setDialogTitle(title);
        }

        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File result = fc.getSelectedFile();
            Configuration.lastOpenDir.set(Helper.fixDialogFile(result).getParentFile().getAbsolutePath());
            return result;
        }

        return null;
    }

    private void showDetail(int card) {
        if (debugStackPanel.isActive() && card != DETAILCARDDEBUGSTACKFRAME) {
            return;
        }

        detailPanel.removeAll();
        int pos = 0;

        if (card == DETAILCARDTAGINFO) {
            detailPanel.addTab(AppStrings.translate("taginfo.header"), tagInfoPanel);
            if (card == DETAILCARDTAGINFO) {
                detailPanel.setSelectedIndex(pos);
            }
            pos++;
        }
        if (card == DETAILCARDAS3NAVIGATOR || card == DETAILCARDDEBUGSTACKFRAME) {
            if (abcPanel != null && getCurrentSwf() != null && getCurrentSwf().isAS3()) {
                detailPanel.addTab(AppStrings.translate("traits"), abcPanel.navigatorPanel);
                if (card == DETAILCARDAS3NAVIGATOR) {
                    detailPanel.setSelectedIndex(pos);
                }
                pos++;
            }
        }
        if (debugStackPanel.isActive()) {
            detailPanel.addTab(AppStrings.translate("callStack.header"), debugStackPanel);
            if (card == DETAILCARDDEBUGSTACKFRAME) {
                detailPanel.setSelectedIndex(pos);
            }
            pos++;
        }
        if (currentView != VIEW_DUMP) {
            detailPanel.setVisible(true);
        }
    }

    private void showCard(String card) {
        CardLayout cl = (CardLayout) (displayPanel.getLayout());
        cl.show(displayPanel, card);
    }

    private void valueChanged(Object source, TreePath selectedPath) {
        TreeItem treeItem = (TreeItem) selectedPath.getLastPathComponent();

        if (treeItem == null) {
            return;
        }

        if (!(treeItem instanceof OpenableList)) {
            Openable openable = treeItem.getOpenable();
            if (openables.isEmpty()) {
                // show welcome panel after closing swfs
                updateUi();
            } else {
                if (openable == null && openables.get(0) != null) {
                    openable = openables.get(0).get(0);
                }

                if (openable != null) {
                    updateUi(openable);
                }
            }
        } else {
            updateUi();
        }

        clearEditingStatus();
        reload(false, false);

        if (source == dumpTree) {
            TreeItem t = null;
            if (treeItem instanceof DumpInfo) {
                DumpInfo di = (DumpInfo) treeItem;
                t = di.getTag();
                if ("BUTTONRECORD".equals(di.type)) {
                    DumpInfo recList = di.parent;
                    int index = recList.getChildInfos().indexOf(di);
                    ButtonTag buttonTag = (ButtonTag) di.parent.parent.getTag();
                    if (index < buttonTag.getRecords().size()) { //last is empty, not displayed in resource/taglist views
                        t = buttonTag.getRecords().get(index);
                    }
                }
            }
            if (t instanceof Tag) {
                t = dumpTree.getOriginalTag(t);
            }
            showPreview(t, dumpPreviewPanel, getFrameForTreeItem(t), getTimelinedForTreeItem(t));
        }
    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        Object source = e.getSource();
        valueChanged(source, e.getPath());
    }

    private int getFrameForTreeItem(TreeItem treeItem) {
        if (treeItem == null) {
            return -1;
        }
        if (currentView == VIEW_DUMP) {
            if (treeItem instanceof Tag) {
                return dumpTree.getFrameForItem(treeItem);
            }
            return -1;
        }

        if (currentView == VIEW_EASY) {
            return -1;
        }
        TreePath path = getCurrentTree().getFullModel().getTreePath(treeItem);
        if (path == null) {
            return -1;
        }
        for (int i = path.getPathCount() - 1; i >= 0; i--) {
            if (path.getPathComponent(i) instanceof Frame) {
                Frame frame = (Frame) path.getPathComponent(i);
                return frame.frame;
            }
        }
        return -1;
    }

    private Timelined getTimelinedForTreeItem(TreeItem treeItem) {
        if (treeItem == null) {
            return null;
        }

        if (currentView == VIEW_DUMP) {
            if (treeItem instanceof Tag) {
                return dumpTree.getTimelinedForItem(treeItem);
            }
            return null;
        }
        if (currentView == VIEW_EASY) {
            return null;
        }

        TreePath path = getCurrentTree().getFullModel().getTreePath(treeItem);
        if (path == null) {
            return null;
        }
        for (int i = path.getPathCount() - 1; i >= 0; i--) {
            if (path.getPathComponent(i) instanceof Timelined) {
                return (Timelined) path.getPathComponent(i);
            }
        }
        return null;
    }   

    public void clearDebuggerColors() {
        if (abcPanel != null) {
            abcPanel.decompiledTextArea.removeColorMarkerOnAllLines(DecompiledEditorPane.IP_MARKER);
            abcPanel.detailPanel.methodTraitPanel.methodCodePanel.clearDebuggerColors();
        }
        if (actionPanel != null) {
            actionPanel.decompiledEditor.removeColorMarkerOnAllLines(DecompiledEditorPane.IP_MARKER);
            actionPanel.editor.removeColorMarkerOnAllLines(DecompiledEditorPane.IP_MARKER);
        }
    }  

    public static final int VIEW_RESOURCES = 0;

    public static final int VIEW_DUMP = 1;

    public static final int VIEW_EASY = 2;

    public static final int VIEW_TAGLIST = 3;

    public int getCurrentView() {
        return currentView;
    }

    public void setTreeModel(int view) {
        switch (view) {
            case VIEW_DUMP:
                if (dumpTree.getModel() == null) {
                    DumpTreeModel dtm = new DumpTreeModel(openables);
                    dumpTree.setModel(dtm);
                    dumpTree.expandFirstLevelNodes();
                }
                break;
            case VIEW_RESOURCES:
            case VIEW_TAGLIST:
                if (tagTree.getModel() == null) {
                    TagTreeModel ttm = new TagTreeModel(openables, Configuration.tagTreeShowEmptyFolders.get());
                    tagTree.setModel(ttm);
                    //tagTree.expandFirstLevelNodes();
                }

                if (tagListTree.getModel() == null) {
                    TagListTreeModel ttm = new TagListTreeModel(openables);
                    tagListTree.setModel(ttm);
                    //tagListTree.expandFirstLevelNodes();
                }
                break;
        }
    }

    private JPanel createDumpViewCard() {
        JPanel r = new JPanel(new BorderLayout());
        r.add(new JPersistentSplitPane(JSplitPane.VERTICAL_SPLIT, new FasterScrollPane(dumpTree), dumpPreviewPanel, Configuration.guiDumpSplitPaneDividerLocationPercent), BorderLayout.CENTER);
        return r;
    }

    private JPanel createTagListViewCard() {
        tagListClipboardPanel = new ClipboardPanel(this);

        JPanel r = new JPanel(new BorderLayout());
        r.add(tagListClipboardPanel, BorderLayout.NORTH);
        r.add(new FasterScrollPane(tagListTree), BorderLayout.CENTER);
        quickTagListFindPanel = new QuickTreeFindPanel();
        quickTagListFindPanel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doFilter();
            }
        });
        r.add(quickTagListFindPanel, BorderLayout.SOUTH);
        return r;
    }

    private JPanel createResourcesViewCard() {
        resourcesClipboardPanel = new ClipboardPanel(this);
        JPanel r = new JPanel(new BorderLayout());
        r.add(resourcesClipboardPanel, BorderLayout.NORTH);
        r.add(tagTreeScrollPanel = new FasterScrollPane(tagTree), BorderLayout.CENTER);
        quickTreeFindPanel = new QuickTreeFilterPanel(tagTree);
        quickTreeFindPanel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doFilter();
            }
        });
        r.add(quickTreeFindPanel, BorderLayout.SOUTH);
        return r;
    }

    private void showContentPanelCard(String card) {
        CardLayout cl = (CardLayout) (contentPanel.getLayout());
        cl.show(contentPanel, card);
    }

    private void showTreePanelCard(String card) {
        CardLayout cl = (CardLayout) (treePanel.getLayout());
        cl.show(treePanel, card);
    }

    public boolean showView(int view) {
        View.checkAccess();

        setTreeModel(view);
        switch (view) {
            case VIEW_DUMP:
                easyPanel.setNoSwf();
                pinsPanel.setVisible(false);
                currentView = view;
                Configuration.lastView.set(currentView);
                if (!isWelcomeScreen) {
                    showContentPanelCard(SPLIT_PANE1);
                }
                showTreePanelCard(DUMP_VIEW);
                treePanelMode = TreePanelMode.DUMP_TREE;
                //showDetail(DETAILCARDEMPTYPANEL);
                showDetail(DETAILCARDEMPTYPANEL);
                reload(true);
                updateUiWithCurrentOpenable();
                detailPanel.setVisible(false);
                return true;
            case VIEW_RESOURCES:
                easyPanel.setNoSwf();
                pinsPanel.setVisible(true);
                currentView = view;
                Configuration.lastView.set(currentView);
                if (!isWelcomeScreen) {
                    showContentPanelCard(SPLIT_PANE1);
                }
                showTreePanelCard(RESOURCES_VIEW);

                treePanelMode = TreePanelMode.TAG_TREE;

                treePanel.addComponentListener(new ComponentAdapter() {
                    @Override
                    public void componentResized(ComponentEvent e) {
                        tagTree.scrollPathToVisible(tagTree.getSelectionPath());
                    }
                });

                detailPanel.setVisible(true);
                refreshPins();
                reload(true);
                updateUiWithCurrentOpenable();
                return true;
            case VIEW_EASY:
                SWF swf = getCurrentSwf();
                pinsPanel.setVisible(false);
                currentView = view;
                Configuration.lastView.set(currentView);
                Set<SWF> swfs = getAllSwfs();
                easyPanel.setSwfs(new ArrayList<>(swfs));
                easyPanel.setSwf(swf);
                if (!isWelcomeScreen) {
                    showContentPanelCard(EASY_PANEL);
                }
                return true;
            case VIEW_TAGLIST:
                easyPanel.setNoSwf();
                pinsPanel.setVisible(true);
                currentView = view;
                Configuration.lastView.set(currentView);
                if (!isWelcomeScreen) {
                    showContentPanelCard(SPLIT_PANE1);
                }
                showTreePanelCard(TAGLIST_VIEW);
                treePanelMode = TreePanelMode.TAGLIST_TREE;
                detailPanel.setVisible(true);
                refreshPins();
                reload(true);
                updateUiWithCurrentOpenable();
                return true;
        }

        return false;
    }

    private void dumpViewReload(boolean forceReload) {
        showDetail(DETAILCARDEMPTYPANEL);

        DumpInfo dumpInfo = (DumpInfo) dumpTree.getLastSelectedPathComponent();
        if (dumpInfo == null) {
            showCard(CARDEMPTYPANEL);
            return;
        }

        dumpViewPanel.revalidate();
        dumpViewPanel.setSelectedNode(dumpInfo);
        showCard(CARDDUMPVIEW);
    }

    public void loadFromBinaryTag(final BinaryDataInterface binaryDataTag) {
        loadFromBinaryTag(Arrays.asList(binaryDataTag));
    }

    public void loadFromBinaryTag(final List<BinaryDataInterface> binaryDataTags) {

        Main.loadingDialog.setVisible(true);
        new CancellableWorker<Void>("loadFromBinaryTag") {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    for (BinaryDataInterface binaryData : binaryDataTags) {
                        String path = binaryData.getSwf().getShortPathTitle() + "/" + binaryData.getPathIdentifier();
                        try {
                            SwfSpecificCustomConfiguration conf = Configuration.getSwfSpecificCustomConfiguration(path);
                            String charset = conf == null ? Charset.defaultCharset().name() : conf.getCustomData(CustomConfigurationKeys.KEY_CHARSET, Charset.defaultCharset().name());
                            byte[] data = binaryData.getDataBytes().getRangeData();
                            InputStream is = new ByteArrayInputStream(data);
                            SWF bswf = new SWF(is, null, "(SWF Data)", new ProgressListener() {
                                @Override
                                public void progress(int p) {
                                    Main.loadingDialog.setPercent(p);
                                }

                                @Override
                                public void status(String status) {
                                }
                            }, Configuration.parallelSpeedUp.get(), charset);
                            binaryData.setInnerSwf(bswf);
                            bswf.binaryData = binaryData;
                        } catch (IOException ex) {
                            //ignore
                        }
                    }
                } catch (InterruptedException ex) {
                    //ignore
                }

                return null;
            }

            @Override
            protected void onStart() {
                Main.startWork(AppStrings.translate("work.reading.swf") + "...", this);
            }

            @Override
            protected void done() {
                View.execInEventDispatch(() -> {
                    Main.loadingDialog.setVisible(false);
                    refreshTree();
                    Main.stopWork();
                });
            }
        }.execute();
    }

    private void closeTag() {
        View.checkAccess();

        previewPanel.closeTag();
    }

    public static void showPreview(TreeItem treeItem, PreviewPanel previewPanel, int frame, Timelined timelinedContainer) {
        previewPanel.clear();
        if (treeItem == null) {
            previewPanel.showEmpty();
            return;
        }

        boolean isVideoButNotDrawable = (treeItem instanceof DefineVideoStreamTag) && (!DefineVideoStreamTag.displayAvailable());

        if (treeItem instanceof SWF) {
            SWF swf = (SWF) treeItem;
            previewPanel.showImagePanel(swf, swf, -1, true, Configuration.autoPlaySwfs.get() && Configuration.autoPlayPreviews.get(), !Configuration.animateSubsprites.get(), false, !Configuration.playFrameSounds.get(), true, false, true, false, true);            
        } else if ((treeItem instanceof PlaceObjectTypeTag)) {
            previewPanel.showDisplayEditTagPanel((PlaceObjectTypeTag) treeItem, frame);
        } else if (treeItem instanceof ShapeTag) {
            previewPanel.showDisplayEditTagPanel((ShapeTag) treeItem, 0);
            previewPanel.setImageReplaceButtonVisible(false, false, !((Tag) treeItem).isReadOnly(), false, false, false, false);
        } else if (treeItem instanceof MorphShapeTag) {
            previewPanel.showDisplayEditTagPanel((MorphShapeTag) treeItem, 0);
            previewPanel.setImageReplaceButtonVisible(false, false, false, false, false, !((Tag) treeItem).isReadOnly(), false);
        } else if (treeItem instanceof MetadataTag) {
            MetadataTag metadataTag = (MetadataTag) treeItem;
            previewPanel.showMetaDataPanel(metadataTag);
        } else if (treeItem instanceof Cookie) {
            previewPanel.showCookiePanel((Cookie) treeItem);
        } else if (treeItem instanceof BinaryDataInterface) {
            BinaryDataInterface binary = (BinaryDataInterface) treeItem;
            previewPanel.showBinaryPanel(binary);
        } else if (treeItem instanceof ProductInfoTag) {
            ProductInfoTag productInfoTag = (ProductInfoTag) treeItem;
            previewPanel.showProductInfoPanel(productInfoTag);
        } else if (treeItem instanceof UnknownTag) {
            UnknownTag unknownTag = (UnknownTag) treeItem;
            previewPanel.showUnknownPanel(unknownTag);
        } else if (treeItem instanceof ImageTag) {
            ImageTag imageTag = (ImageTag) treeItem;
            previewPanel.setImageReplaceButtonVisible(!((Tag) imageTag).isReadOnly() && imageTag.importSupported(), imageTag instanceof DefineBitsJPEG3Tag || imageTag instanceof DefineBitsJPEG4Tag, false, false, false, false, false);
            SWF imageSWF = TimelinedMaker.makeTimelinedImage(imageTag);
            previewPanel.showImagePanel(imageSWF, imageSWF, 0, false, true, true, true, true, false, false, true, true, true);
        } else if (!isVideoButNotDrawable && (treeItem instanceof DrawableTag) && (!(treeItem instanceof TextTag)) && (!(treeItem instanceof FontTag))) {
            final Tag tag = (Tag) treeItem;
            DrawableTag d = (DrawableTag) tag;
            Timelined timelined;
            if (treeItem instanceof Timelined && !(treeItem instanceof ButtonTag)) {
                timelined = (Timelined) tag;
            } else {
                timelined = TimelinedMaker.makeTimelined(tag);
            }

            previewPanel.setParametersPanelVisible(false);
            if (treeItem instanceof ShapeTag) {
                previewPanel.setImageReplaceButtonVisible(false, false, !((Tag) treeItem).isReadOnly(), false, false, false, false);
            }
            if (treeItem instanceof DefineVideoStreamTag) {
                previewPanel.setImageReplaceButtonVisible(false, false, false, false, !((Tag) treeItem).isReadOnly(), false, false);
            }
            if (treeItem instanceof DefineSpriteTag) {
                previewPanel.setImageReplaceButtonVisible(false, false, false, false, false, false, !((Tag) treeItem).isReadOnly());
            }
            previewPanel.showImagePanel(timelined, tag.getSwf(), -1, true, Configuration.autoPlayPreviews.get(), !Configuration.animateSubsprites.get() && !(treeItem instanceof ButtonTag), treeItem instanceof ShapeTag, !Configuration.playFrameSounds.get(), (treeItem instanceof DefineSpriteTag) || (treeItem instanceof ButtonTag), (treeItem instanceof DefineSpriteTag) || (treeItem instanceof ButtonTag) || (treeItem instanceof ShapeTag), true, false, true);
        } else if (treeItem instanceof Frame) {
            Frame fn = (Frame) treeItem;
            SWF swf = (SWF) fn.getOpenable();
            previewPanel.showImagePanel(fn.timeline.timelined, swf, fn.frame, true, Configuration.autoPlayPreviews.get(), !Configuration.animateSubsprites.get(), false, !Configuration.playFrameSounds.get(), true, false, true, false, true);
        } else if (treeItem instanceof ShowFrameTag) {
            SWF swf;
            if (timelinedContainer instanceof DefineSpriteTag) {
                swf = ((DefineSpriteTag) timelinedContainer).getSwf();
            } else {
                swf = (SWF) timelinedContainer;
            }
            previewPanel.showImagePanel(timelinedContainer, swf, frame, true, Configuration.autoPlayPreviews.get(), !Configuration.animateSubsprites.get(), false, !Configuration.playFrameSounds.get(), true, false, true, false, true);
        } else if ((treeItem instanceof SoundTag)) { //&& isInternalFlashViewerSelected() && (Arrays.asList("mp3", "wav").contains(((SoundTag) tagObj).getExportFormat())))) {
            previewPanel.showImagePanel(new SerializableImage(View.loadImage("sound32")));
            previewPanel.setImageReplaceButtonVisible(false, false, false, !((SoundTag) treeItem).isReadOnly() && ((SoundTag) treeItem).importSupported(), false, false, false);
            if (!(treeItem instanceof SoundStreamHeadTypeTag)) {
                try {
                    SoundTagPlayer soundThread = new SoundTagPlayer(null, (SoundTag) treeItem, Configuration.loopMedia.get() ? Integer.MAX_VALUE : 1, true, Configuration.previewResampleSound.get());
                    if (!Configuration.autoPlaySounds.get()) {
                        soundThread.pause();
                    }
                    previewPanel.setMedia(soundThread);
                } catch (LineUnavailableException | IOException | UnsupportedAudioFileException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            }
        } else if ((treeItem instanceof FontTag)) {
            previewPanel.showFontPanel((FontTag) treeItem);
        } else if ((treeItem instanceof TextTag)) {
            previewPanel.showTextPanel((TextTag) treeItem);
        } else if ((!(treeItem instanceof DefineFont4Tag)) && ((treeItem instanceof Frame) || (treeItem instanceof CharacterTag) || (treeItem instanceof FontTag) || (treeItem instanceof SoundStreamHeadTypeTag))) {
            previewPanel.createAndShowTempSwf(treeItem);

            if (treeItem instanceof TextTag) {
                previewPanel.showTextPanel((TextTag) treeItem);
            } else if (treeItem instanceof FontTag) {
                previewPanel.showFontPanel((FontTag) treeItem);
            } else {
                previewPanel.setParametersPanelVisible(false);
            }
        } else if ((treeItem instanceof BUTTONRECORD) && (!((BUTTONRECORD) treeItem).getSwf().getCyclicCharacters().contains(((BUTTONRECORD) treeItem).characterId))) {
            BUTTONRECORD buttonRecord = (BUTTONRECORD) treeItem;
            previewPanel.setParametersPanelVisible(false);
            SWF origSwf = ((SWF) treeItem.getOpenable());
            Timelined tim = new Timelined() {

                ReadOnlyTagList cachedTags = null;

                @Override
                public SWF getSwf() {
                    return origSwf;
                }

                @Override
                public Timeline getTimeline() {
                    return new Timeline(origSwf, this, Integer.MAX_VALUE, buttonRecord.getTag().getRect());
                }

                @Override
                public void resetTimeline() {

                }

                @Override
                public void setModified(boolean value) {

                }

                @Override
                public boolean isModified() {
                    return false;
                }

                @Override
                public ReadOnlyTagList getTags() {
                    if (cachedTags == null) {
                        List<Tag> tags = new ArrayList<>();
                        PlaceObject3Tag placeTag = buttonRecord.toPlaceObject();
                        placeTag.setSwf(origSwf);
                        placeTag.setTimelined(this);
                        tags.add(placeTag);

                        ShowFrameTag showFrameTag = new ShowFrameTag(origSwf);
                        showFrameTag.setTimelined(this);
                        tags.add(showFrameTag);
                        cachedTags = new ReadOnlyTagList(tags);
                    }
                    return cachedTags;
                }

                @Override
                public void removeTag(int index) {
                }

                @Override
                public void removeTag(Tag tag) {
                }

                @Override
                public void addTag(Tag tag) {
                }

                @Override
                public void addTag(int index, Tag tag) {
                }

                @Override
                public void replaceTag(int index, Tag newTag) {
                }

                @Override
                public void replaceTag(Tag oldTag, Tag newTag) {
                }

                @Override
                public int indexOfTag(Tag tag) {
                    return getTags().indexOf(tag);
                }

                @Override
                public void setFrameCount(int frameCount) {
                }

                @Override
                public int getFrameCount() {
                    return 1;
                }

                @Override
                public RECT getRect() {
                    return buttonRecord.getTag().getRect();
                }

                @Override
                public RECT getRect(Set<BoundedTag> added) {
                    return getRect();
                }

                @Override
                public RECT getRectWithStrokes() {
                    return getRect();
                }
            };
            previewPanel.showImagePanel(tim, origSwf, 0, true, true, !Configuration.animateSubsprites.get(), false, !Configuration.playFrameSounds.get(), true, false, true, true, true);
        } else if (treeItem instanceof DefineFont4Tag) {
            previewPanel.showGenericTagPanel((Tag) treeItem);
        } else {
            previewPanel.showEmpty();
        }
    }

    private void tagListViewReload(boolean forceReload) {
        showDetail(DETAILCARDEMPTYPANEL);
        showCard(CARDEMPTYPANEL);
    }

    private void saveBreakpoints(Openable openable) {
        if (openable instanceof SWF) {
            SwfSpecificCustomConfiguration swfCustomConf = Configuration.getOrCreateSwfSpecificCustomConfiguration(openable.getShortPathTitle());
            SWF swf = (SWF) openable;
            Map<String, Set<Integer>> breakpoints = Main.getDebugHandler().getAllBreakPoints(swf, false);
            List<String> breakpointList = new ArrayList<>();
            for (String scriptName : breakpoints.keySet()) {
                for (int line : breakpoints.get(scriptName)) {
                    breakpointList.add(scriptName + ":" + line);
                }
            }
            swfCustomConf.setCustomData(CustomConfigurationKeys.KEY_BREAKPOINTS, breakpointList);
        }
    }

    public void reload(boolean forceReload) {
        reload(forceReload, true);
    }

    public void reload(boolean forceReload, boolean scrollToVisible) {
        View.checkAccess();

        String previousFolderName = currentFolderName;

        JScrollBar folderPreviewScrollBar = ((JScrollPane) folderPreviewPanel.getParent().getParent()).getVerticalScrollBar();
        int scrollValue = folderPreviewScrollBar.getValue();
        Map<Integer, TreeItem> folderItems = new HashMap<>(folderPreviewPanel.getSelectedItems());

        if (scrollToVisible) {
            tagTree.scrollPathToVisible(tagTree.getSelectionPath());
        }
        if (currentView == VIEW_DUMP) {
            dumpViewReload(forceReload);
            return;
        }
        /*else if (currentView == VIEW_TAGLIST) {
            tagListViewReload(forceReload);
            return;
        }*/

        AbstractTagTree tree = getCurrentTree();
        TreeItem treeItem = null;
        TreePath treePath = tree.getSelectionPath();
        if (treePath != null && tree.getFullModel().treePathExists(treePath)) {
            treeItem = (TreeItem) treePath.getLastPathComponent();
        }

        if (currentView == VIEW_EASY) {
            if (treeItem != null) {
                Openable op = treeItem.getOpenable();
                if (op instanceof SWF) {
                    easyPanel.setSwf((SWF) op);
                }
            }
            return;
        }

        // save last selected node to config
        if (treeItem != null && !(treeItem instanceof OpenableList)) {
            Openable openable = treeItem.getOpenable();
            if (openable != null && (openable instanceof SWF)) {
                openable = ((SWF) openable).getRootSwf();
            }

            if (openable != null) {
                SwfSpecificCustomConfiguration swfCustomConf = Configuration.getOrCreateSwfSpecificCustomConfiguration(openable.getShortPathTitle());
                swfCustomConf.setCustomData(CustomConfigurationKeys.KEY_LAST_SELECTED_PATH_RESOURCES, tagTree.getSelectionPathString());
                swfCustomConf.setCustomData(CustomConfigurationKeys.KEY_LAST_SELECTED_PATH_TAGLIST, tagListTree.getSelectionPathString());
                saveBreakpoints(openable);
            }
        }

        if (!forceReload && (treeItem == oldItem)) {
            return;
        }

        if (oldItem != treeItem) {
            scrollPosStorage.saveScrollPos(oldItem);
            closeTag();
        }

        oldItem = treeItem;

        // show the preview of the tag when the user clicks to the tagname inside the scripts node, too
        // this is a little bit inconsistent, because the frames (FrameScript) are not shown
        boolean preferScript = false;
        if (treeItem instanceof TagScript) {
            treeItem = ((TagScript) treeItem).getTag();
            preferScript = true;
        }

        folderPreviewPanel.clear();
        folderListPanel.clear();
        previewPanel.clear();

        previewPanel.setImageReplaceButtonVisible(false, false, false, false, false, false, false);

        Frame frameTreeItem = null;
        if (treeItem instanceof Frame) {
            frameTreeItem = (Frame) treeItem;
        }
        if (treeItem instanceof SceneFrame) {
            frameTreeItem = ((SceneFrame) treeItem).getFrame();
        }

        if ((treeItem instanceof AS3Package) && ((AS3Package) treeItem).isCompoundScript()) {
            final ScriptPack scriptLeaf = ((AS3Package) treeItem).getCompoundInitializerPack();
            if (Main.isInited() && (!Main.isWorking() || Main.isDebugging())) {
                ABCPanel abcPanel = getABCPanel();
                abcPanel.setScript(scriptLeaf);
                abcPanel.setCompound(true);
            }

            if (Configuration.displayAs3TraitsListAndConstantsPanel.get()) {
                showDetail(DETAILCARDAS3NAVIGATOR);
            } else {
                showDetail(DETAILCARDEMPTYPANEL);
            }
            showCard(CARDACTIONSCRIPT3PANEL);
        } else if (treeItem instanceof ScriptPack) {
            final ScriptPack scriptLeaf = (ScriptPack) treeItem;
            if (Main.isInited() && (!Main.isWorking() || Main.isDebugging())) {
                ABCPanel abcPanel = getABCPanel();
                abcPanel.setScript(scriptLeaf);
                abcPanel.setCompound(!scriptLeaf.isSimple);
            }

            if (Configuration.displayAs3TraitsListAndConstantsPanel.get()) {
                showDetail(DETAILCARDAS3NAVIGATOR);
            } else {
                showDetail(DETAILCARDEMPTYPANEL);
            }
            showCard(CARDACTIONSCRIPT3PANEL);
        } else if (treeItem instanceof Tag) {
            Tag tag = (Tag) treeItem;
            TagInfo tagInfo = new TagInfo((SWF) treeItem.getOpenable());
            tag.getTagInfo(tagInfo);

            Set<Integer> needed;
            if (neededCharacters.containsKey(treeItem)) {
                needed = neededCharacters.get(treeItem);
            } else {
                needed = new LinkedHashSet<>();
                tag.getNeededCharactersDeep(needed);
                neededCharacters.put(treeItem, needed);
            }

            if (needed.size() > 0) {
                tagInfo.addInfo("general", "neededCharacters", Helper.joinStrings(needed, ", "));
            }

            if (tag instanceof CharacterTag) {
                int characterId = ((CharacterTag) tag).getCharacterId();
                Set<Integer> dependent = tag.getSwf().getDependentCharacters(characterId);
                if (dependent != null) {
                    if (dependent.size() > 0) {
                        tagInfo.addInfo("general", "dependentCharacters", Helper.joinStrings(dependent, ", "));
                    }
                }

                Set<Integer> dependent2 = tag.getSwf().getDependentFrames(characterId);
                if (dependent2 != null && dependent2.size() > 0) {
                    tagInfo.addInfo("general", "dependentFrames", Helper.joinStrings(dependent2, ", "));
                }
            }

            if (!tagInfo.isEmpty()) {
                tagInfoPanel.setTagInfos(tagInfo);
                showDetail(DETAILCARDTAGINFO);
            } else {
                showDetail(DETAILCARDEMPTYPANEL);
            }
        } else if (frameTreeItem != null) {
            Frame frame = frameTreeItem;
            Set<Integer> needed = new LinkedHashSet<>();

            frame.getNeededCharacters(needed);

            if (!needed.isEmpty()) {
                TagInfo tagInfo = new TagInfo((SWF) treeItem.getOpenable());
                tagInfo.addInfo("general", "neededCharacters", Helper.joinStrings(needed, ", "));
                tagInfoPanel.setTagInfos(tagInfo);
                showDetail(DETAILCARDTAGINFO);
            } else {
                showDetail(DETAILCARDEMPTYPANEL);
            }
        } else {
            showDetail(DETAILCARDEMPTYPANEL);
        }

        /*if (treeItem instanceof SoundStreamBlockTag) {
            SoundStreamBlockTag block = (SoundStreamBlockTag)treeItem;
            byte[] data = block.streamSoundData.getRangeData();
            try{
            SWFInputStream sis = new SWFInputStream(block.getSwf(), data);
            int sampleCount = sis.readUI16("sampleCount");
            int seekSamples = sis.readSI16("seekSamples");
            System.out.println("sampleCount = "+sampleCount);
            System.out.println("seekSamples = "+seekSamples);
            System.out.println("============");
            }catch(Exception ex){

            }
        }*/
        if (treeItem instanceof HeaderItem) {
            headerPanel.load((SWF) ((HeaderItem) treeItem).getOpenable());
            showCard(CARDHEADER);
        } else if (treeItem instanceof FolderItem) {
            showFolderPreview((FolderItem) treeItem);
        } else if (treeItem instanceof SWF) {
            showPreview(treeItem, previewPanel, -1, null);
            showCard(CARDPREVIEWPANEL);
        } else if (treeItem instanceof Scene) {
            showFolderPreviewList(treePath);
        } else if (treeItem instanceof MetadataTag) {
            showPreview(treeItem, previewPanel, -1, null);
            showCard(CARDPREVIEWPANEL);
        } else if (treeItem instanceof Cookie) {
            showPreview(treeItem, previewPanel, -1, null);
            showCard(CARDPREVIEWPANEL);
        } else if (treeItem instanceof BinaryDataInterface) {
            showPreview(treeItem, previewPanel, -1, null);
            showCard(CARDPREVIEWPANEL);
        } else if (treeItem instanceof UnknownTag) {
            showPreview(treeItem, previewPanel, -1, null);
            showCard(CARDPREVIEWPANEL);
        } else if (treeItem instanceof ASMSource && (!(treeItem instanceof DrawableTag) || preferScript)) {
            getActionPanel().setSource((ASMSource) treeItem, !forceReload);
            showCard(CARDACTIONSCRIPTPANEL);
        } else if (treeItem instanceof ImageTag) {
            showPreview(treeItem, previewPanel, -1, null);
            showCard(CARDPREVIEWPANEL);
        } else if ((treeItem instanceof DrawableTag) && (!(treeItem instanceof TextTag)) && (!(treeItem instanceof FontTag))) {
            showPreview(treeItem, previewPanel, -1, null);
            showCard(CARDPREVIEWPANEL);
        } else if (treeItem instanceof FontTag) {
            showPreview(treeItem, previewPanel, -1, null);
            showCard(CARDPREVIEWPANEL);
        } else if (treeItem instanceof TextTag) {
            showPreview(treeItem, previewPanel, -1, null);
            showCard(CARDPREVIEWPANEL);
        } else if (frameTreeItem != null) {
            showPreview(frameTreeItem, previewPanel, -1, null);
            showCard(CARDPREVIEWPANEL);
        } else if (treeItem instanceof ShowFrameTag) {
            showPreview(treeItem, previewPanel, getFrameForTreeItem(treeItem), getTimelinedForTreeItem(treeItem));
            showCard(CARDPREVIEWPANEL);
        } else if ((treeItem instanceof SoundTag)) {
            showPreview(treeItem, previewPanel, -1, null);
            showCard(CARDPREVIEWPANEL);
        } else if (frameTreeItem != null) {
            showPreview(frameTreeItem, previewPanel, -1, null);
            showCard(CARDPREVIEWPANEL);
        } else if ((treeItem instanceof CharacterTag) || (treeItem instanceof FontTag) || (treeItem instanceof SoundStreamHeadTypeTag)) {
            showPreview(treeItem, previewPanel, -1, null);
            showCard(CARDPREVIEWPANEL);
        } else if (treeItem instanceof PlaceObjectTypeTag) {
            showPreview(treeItem, previewPanel, getFrameForTreeItem(treeItem), null);
            showCard(CARDPREVIEWPANEL);
        } else if (treeItem instanceof ProductInfoTag) {
            showPreview(treeItem, previewPanel, -1, null);
            showCard(CARDPREVIEWPANEL);
        } else if (treeItem instanceof Tag) {
            showGenericTag((Tag) treeItem);
        } else if (treeItem instanceof BUTTONRECORD) {
            showPreview(treeItem, previewPanel, -1, null);
            showCard(CARDPREVIEWPANEL);
        } else if (!((treeItem instanceof ScriptPack) || ((treeItem instanceof AS3Package) && ((AS3Package) treeItem).isCompoundScript()))) {
            if (treePath == null) {
                showCard(CARDEMPTYPANEL);
            } else {
                showFolderList(treePath);
            }
        }
        if (oldItem instanceof TreeRoot) {
            pinsPanel.setCurrent(null);
        } else {
            pinsPanel.setCurrent(oldItem);
        }

        if (currentFolderName != null && currentFolderName.equals(previousFolderName)) {
            folderPreviewPanel.setSelectedItems(folderItems);
            folderPreviewScrollBar.setValue(scrollValue);
        }

        View.execInEventDispatchLater(new Runnable() {
            @Override
            public void run() {
                scrollPosStorage.loadScrollPos(oldItem);
            }
        });

    }

    public void repaintTree() {
        tagTree.repaint();
        tagListTree.repaint();
        reload(true);
        updateUiWithCurrentOpenable();
    }

    public void showGenericTag(Tag tag) {
        previewPanel.clear();
        previewPanel.showGenericTagPanel(tag);
        showCard(CARDPREVIEWPANEL);
    }

    public void showTextTagWithNewValue(TextTag textTag, TextTag newTextTag) {

        previewPanel.showTextComparePanel(textTag, newTextTag);
    }

    private void addFolderPreviewItems(List<TreeItem> folderPreviewItems, String folderName, Timelined timelined) {
        switch (folderName) {
            case TagTreeModel.FOLDER_SHAPES:
                for (Tag tag : timelined.getTags()) {
                    if (tag instanceof ShapeTag) {
                        folderPreviewItems.add(tag);
                    }
                    if (tag instanceof DefineSpriteTag) {
                        addFolderPreviewItems(folderPreviewItems, folderName, (DefineSpriteTag) tag);
                    }
                }
                break;
            case TagTreeModel.FOLDER_MORPHSHAPES:
                for (Tag tag : timelined.getTags()) {
                    if (tag instanceof MorphShapeTag) {
                        folderPreviewItems.add(tag);
                    }
                    if (tag instanceof DefineSpriteTag) {
                        addFolderPreviewItems(folderPreviewItems, folderName, (DefineSpriteTag) tag);
                    }
                }
                break;
            case TagTreeModel.FOLDER_SPRITES:
                for (Tag tag : timelined.getTags()) {
                    if (tag instanceof DefineSpriteTag) {
                        folderPreviewItems.add(tag);
                        addFolderPreviewItems(folderPreviewItems, folderName, (DefineSpriteTag) tag);
                    }
                }
                break;
            case TagTreeModel.FOLDER_MOVIES:
                for (Tag tag : timelined.getTags()) {
                    if (tag instanceof DefineVideoStreamTag) {
                        folderPreviewItems.add(tag);
                    }
                    if (tag instanceof DefineSpriteTag) {
                        addFolderPreviewItems(folderPreviewItems, folderName, (DefineSpriteTag) tag);
                    }
                }
                break;
            case TagTreeModel.FOLDER_BUTTONS:
                for (Tag tag : timelined.getTags()) {
                    if (tag instanceof ButtonTag) {
                        folderPreviewItems.add(tag);
                    }
                    if (tag instanceof DefineSpriteTag) {
                        addFolderPreviewItems(folderPreviewItems, folderName, (DefineSpriteTag) tag);
                    }
                }
                break;
            case TagTreeModel.FOLDER_FONTS:
                for (Tag tag : timelined.getTags()) {
                    if (tag instanceof FontTag) {
                        folderPreviewItems.add(tag);
                    }
                    if (tag instanceof DefineSpriteTag) {
                        addFolderPreviewItems(folderPreviewItems, folderName, (DefineSpriteTag) tag);
                    }
                }
                break;
            case TagTreeModel.FOLDER_FRAMES:
                for (Frame frame : timelined.getTimeline().getFrames()) {
                    folderPreviewItems.add(frame);
                }
                break;
            case TagTreeModel.FOLDER_IMAGES:
                for (Tag tag : timelined.getTags()) {
                    if (tag instanceof ImageTag) {
                        folderPreviewItems.add(tag);
                    }
                    if (tag instanceof DefineSpriteTag) {
                        addFolderPreviewItems(folderPreviewItems, folderName, (DefineSpriteTag) tag);
                    }
                }
                break;
            case TagTreeModel.FOLDER_TEXTS:
                for (Tag tag : timelined.getTags()) {
                    if (tag instanceof TextTag) {
                        folderPreviewItems.add(tag);
                    }
                    if (tag instanceof DefineSpriteTag) {
                        addFolderPreviewItems(folderPreviewItems, folderName, (DefineSpriteTag) tag);
                    }
                }
                break;
            case TagTreeModel.FOLDER_SCENES:
                folderPreviewItems.addAll(timelined.getTimeline().getScenes());
                break;
        }
    }

    private void showFolderPreview(FolderItem item) {
        String folderName = item.getName();
        if (TagTreeModel.FOLDER_OTHERS.equals(folderName)
                || TagTreeModel.FOLDER_SCRIPTS.equals(folderName)
                || TagTreeModel.FOLDER_SOUNDS.equals(folderName)) {
            showFolderList(tagTree.getFullModel().getTreePath(item));
            return;
        }
        List<TreeItem> folderPreviewItems = new ArrayList<>();
        SWF swf = item.swf;
        addFolderPreviewItems(folderPreviewItems, folderName, swf);

        currentFolderName = folderName;
        folderPreviewPanel.setItems(folderPreviewItems);
        showCard(CARDFOLDERPREVIEWPANEL);
    }

    private void showFolderPreviewList(TreePath path) {
        List<TreeItem> items = new ArrayList<>(getCurrentTree().getFullModel().getAllChildren((TreeItem) path.getLastPathComponent()));
        folderPreviewPanel.setItems(items);
        showCard(CARDFOLDERPREVIEWPANEL);
    }

    private void showFolderList(TreePath path) {
        List<TreeItem> items = new ArrayList<>(getCurrentTree().getFullModel().getAllChildren((TreeItem) path.getLastPathComponent()));
        folderListPanel.setItems(path, items);
        showCard(CARDFOLDERLISTPANEL);
    }

    private boolean isFreeing;

    @Override
    public boolean isFreeing() {
        return isFreeing;
    }

    @Override
    public void free() {
        isFreeing = true;
    }

    public void setErrorState(ErrorState errorState) {
        statusPanel.setErrorState(errorState);
    }

    private void disposeInner(Container container) {
        for (Component c : container.getComponents()) {
            if (c instanceof Container) {
                Container c2 = (Container) c;
                disposeInner(c2);
            }
        }

        container.removeAll();
        container.setLayout(null);
        if (container instanceof TagEditorPanel) {
            Helper.emptyObject(container);
        }
    }

    public void dispose() {
        easyPanel.dispose();
        if (calculateMissingNeededThread != null) {
            calculateMissingNeededThread.interrupt();
        }
        setDropTarget(null);
        disposeInner(this);
        Helper.emptyObject(this);
    }

    public void updateMissingNeededCharacters() {
        if (calculateMissingNeededThread != null) {
            calculateMissingNeededThread.recalculate();
        }
    }

    private static void calculateMissingNeededCharacters(Map<TreeItem, Set<Integer>> neededMap, Map<TreeItem, Set<Integer>> missingNeededCharacters, Timelined tim) {
        List<Tag> tags = tim.getTags().toArrayList();
        Map<Integer, List<CharacterIdTag>> nestedTags = new HashMap<>();
        for (Tag t : tags) {
            if ((t instanceof CharacterIdTag) && !(t instanceof CharacterTag) && !(t instanceof SoundStreamHeadTypeTag) && !(t instanceof DefineExternalStreamSound)) {
                int characterId = ((CharacterIdTag) t).getCharacterId();
                if (!nestedTags.containsKey(characterId)) {
                    nestedTags.put(characterId, new ArrayList<>());
                }
                nestedTags.get(characterId).add((CharacterIdTag) t);
            }

            if (!(t instanceof CharacterTag) && !(t instanceof SoundStreamHeadTypeTag) && !(t instanceof DefineExternalStreamSound)) {
                int characterId = -1;
                if (t instanceof CharacterIdTag) {
                    characterId = ((CharacterIdTag) t).getCharacterId();
                }
                Set<Integer> needed = new LinkedHashSet<>();
                t.getNeededCharactersDeep(needed);
                neededMap.put(t, needed);
                if ((t instanceof CharacterIdTag) && !(t instanceof PlaceObjectTypeTag)) {
                    needed = new HashSet<>();
                    needed.add(characterId);
                }

                if ((t instanceof PlaceObjectTypeTag) && (characterId != -1) && nestedTags.containsKey(characterId)) {
                    for (CharacterIdTag n : nestedTags.get(characterId)) {
                        ((Tag) n).getNeededCharactersDeep(needed);
                    }
                }

                missingNeededCharacters.put(t, t.getMissingNeededCharacters(needed));
                if (characterId != -1 && tim.getTimeline().swf.getCharacter(characterId) == null) {
                    missingNeededCharacters.get(t).add(characterId);
                }

            }
            /*if (t instanceof DefineSpriteTag) {
                calculateMissingNeededCharacters(neededMap, missingNeededCharacters, (DefineSpriteTag) t);
            }*/
        }
    }

    private void calculateMissingNeededCharacters() {
        Map<TreeItem, Set<Integer>> missingNeededCharacters = new WeakHashMap<>();
        Map<TreeItem, Set<Integer>> neededCharacters = new WeakHashMap<>();

        List<OpenableList> swfsLists = new ArrayList<>(openables);
        for (OpenableList swfList : swfsLists) {
            for (Openable openable : swfList) {
                if (openable instanceof SWF) {
                    calculateMissingNeededCharacters(neededCharacters, missingNeededCharacters, (SWF) openable);
                    //TODO: how about SubSWFs???
                }
            }
        }
        this.neededCharacters = neededCharacters;
        this.missingNeededCharacters = missingNeededCharacters;
        tagTree.setMissingNeededCharacters(missingNeededCharacters);
        tagListTree.setMissingNeededCharacters(missingNeededCharacters);
    }

    class CalculateMissingNeededThread extends Thread {

        public CalculateMissingNeededThread() {
            super("calculateMissingNeededThread");
            setPriority(Thread.MIN_PRIORITY);
        }

        private boolean recalculate = false;

        public synchronized void recalculate() {
            this.recalculate = true;
            this.notify();
        }

        @Override
        public void run() {
            while (true) {
                synchronized (this) {
                    recalculate = false;
                }
                try {
                    calculateMissingNeededCharacters();
                } catch (ConcurrentModificationException cme) {
                    //ignore
                }
                synchronized (this) {

                    if (recalculate) {
                        continue;
                    }

                    try {
                        wait();
                    } catch (InterruptedException ex) {
                        return;
                    }
                }
            }
        }
    }

    public String itemToString(TreeItem item) {
        int index = getCurrentTree().getFullModel().getItemIndex(item);
        String itemToStr = item.toString();
        if (index > 1) {
            return itemToStr + " [" + index + "]";
        }
        return itemToStr;
    }

    public void startEdit() {
        TreeItem treeItem = getCurrentTree().getCurrentTreeItem();
        if (treeItem == null) {
            return;
        }
        if (treeItem instanceof HeaderItem) {
            headerPanel.startEdit();
        } else if (treeItem instanceof PlaceObjectTypeTag) {
            previewPanel.startEditPlaceTag();
        } else if (treeItem instanceof MetadataTag) {
            previewPanel.startEditMetaDataTag();
        } else if (treeItem instanceof DefineBinaryDataTag) {
            //TODO
        } else if (treeItem instanceof FontTag) {
            previewPanel.startEditFontTag();
        } else if (treeItem instanceof TextTag) {
            previewPanel.startEditTextTag();
        } else if (treeItem instanceof ASMSource) {
            //There are two kinds of edit - Script and P-code.
        } else if (treeItem instanceof Tag) {
            Tag tag = (Tag) treeItem;
            previewPanel.showGenericTagPanel(tag);
            previewPanel.startEditGenericTag();
        }

    }

    public MainFrameStatusPanel getStatusPanel() {
        return statusPanel;
    }

    public void closeAbcExplorer(Openable openable) {
        ABCExplorerDialog dialog = abcExplorerDialogs.get(openable);
        if (dialog != null) {
            dialog.setVisible(false);
            abcExplorerDialogs.remove(dialog);
        }
    }

    public ABCExplorerDialog showAbcExplorer(Openable openable, ABC abc) {
        ABCExplorerDialog dialog = abcExplorerDialogs.get(openable);
        if (dialog != null) {
            dialog.selectAbc(abc);
            if (!dialog.isVisible()) {
                dialog.setVisible(true);
            } else {
                dialog.toFront();
            }
        } else {
            dialog = new ABCExplorerDialog(mainFrame.getWindow(), this, openable, abc);
            abcExplorerDialogs.put(openable, dialog);
            dialog.setVisible(true);
        }
        return dialog;
    }

    public BreakpointListDialog showBreakpointlistDialog(SWF swf) {
        BreakpointListDialog dialog = breakpointsListDialogs.get(swf);
        if (dialog != null) {
            dialog.refresh();
            if (!dialog.isVisible()) {
                dialog.setVisible(true);
            } else {
                dialog.toFront();
            }
        } else {
            dialog = new BreakpointListDialog(mainFrame.getWindow(), swf);
            breakpointsListDialogs.put(swf, dialog);
            dialog.setVisible(true);
        }
        return dialog;
    }

    public boolean fontEmbed(TreeItem item, boolean create) {
        return previewPanel.getFontPanel().fontEmbed(item, create);
    }

    public EasyPanel getEasyPanel() {
        return easyPanel;
    }
}
