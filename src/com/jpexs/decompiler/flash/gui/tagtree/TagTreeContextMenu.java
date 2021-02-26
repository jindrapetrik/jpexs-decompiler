/*
 *  Copyright (C) 2010-2021 JPEXS
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
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.abc.avm2.parser.AVM2ParseException;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.ActionScript3Parser;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.parser.script.ActionScript2Parser;
import com.jpexs.decompiler.flash.gui.AppDialog;
import com.jpexs.decompiler.flash.gui.Main;
import com.jpexs.decompiler.flash.gui.MainPanel;
import com.jpexs.decompiler.flash.gui.ReplaceCharacterDialog;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.gui.abc.ClassesListTreeModel;
import com.jpexs.decompiler.flash.gui.action.AddScriptDialog;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.flash.tags.DefineBinaryDataTag;
import com.jpexs.decompiler.flash.tags.DefineButton2Tag;
import com.jpexs.decompiler.flash.tags.DefineSoundTag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.DoABC2Tag;
import com.jpexs.decompiler.flash.tags.DoABCTag;
import com.jpexs.decompiler.flash.tags.DoActionTag;
import com.jpexs.decompiler.flash.tags.DoInitActionTag;
import com.jpexs.decompiler.flash.tags.ExportAssetsTag;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.PlaceObject3Tag;
import com.jpexs.decompiler.flash.tags.PlaceObject4Tag;
import com.jpexs.decompiler.flash.tags.PlaceObjectTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.flash.tags.base.ASMSourceContainer;
import com.jpexs.decompiler.flash.tags.base.CharacterIdTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.tags.base.TextTag;
import com.jpexs.decompiler.flash.timeline.AS2Package;
import com.jpexs.decompiler.flash.timeline.AS3Package;
import com.jpexs.decompiler.flash.timeline.Frame;
import com.jpexs.decompiler.flash.timeline.FrameScript;
import com.jpexs.decompiler.flash.timeline.TagScript;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.treeitems.AS3ClassTreeItem;
import com.jpexs.decompiler.flash.treeitems.FolderItem;
import com.jpexs.decompiler.flash.treeitems.SWFList;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import com.jpexs.decompiler.flash.types.BUTTONCONDACTION;
import com.jpexs.decompiler.flash.types.CLIPACTIONRECORD;
import com.jpexs.decompiler.flash.types.CLIPACTIONS;
import com.jpexs.decompiler.flash.types.CXFORMWITHALPHA;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.helpers.Helper;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.function.Predicate;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 *
 * @author JPEXS
 */
public class TagTreeContextMenu extends JPopupMenu {

    private static final Logger logger = Logger.getLogger(TagTreeContextMenu.class.getName());

    private final MainPanel mainPanel;

    private final TagTree tagTree;

    private JMenuItem expandRecursiveMenuItem;

    private JMenuItem removeMenuItem;

    private JMenuItem removeWithDependenciesMenuItem;

    private JMenuItem undoTagMenuItem;

    private JMenuItem exportSelectionMenuItem;

    private JMenuItem replaceMenuItem;

    private JMenuItem replaceNoFillMenuItem;

    private JMenuItem replaceWithTagMenuItem;

    private JMenuItem rawEditMenuItem;

    private JMenuItem jumpToCharacterMenuItem;

    private JMenuItem exportJavaSourceMenuItem;

    private JMenuItem exportSwfXmlMenuItem;

    private JMenuItem importSwfXmlMenuItem;

    private JMenuItem closeMenuItem;

    private JMenu addTagMenu;

    private JMenu moveTagMenu;

    private JMenu copyTagMenu;

    private JMenu copyTagWithDependenciesMenu;

    private JMenuItem openSWFInsideTagMenuItem;

    private JMenuItem addAs12ScriptMenuItem;

    private JMenuItem addAs3ClassMenuItem;

    private JMenuItem textSearchMenuItem;

    public TagTreeContextMenu(final TagTree tagTree, MainPanel mainPanel) {
        this.mainPanel = mainPanel;
        this.tagTree = tagTree;

        expandRecursiveMenuItem = new JMenuItem(mainPanel.translate("contextmenu.expandAll"));
        expandRecursiveMenuItem.addActionListener(this::expandRecursiveActionPerformed);
        add(expandRecursiveMenuItem);

        removeMenuItem = new JMenuItem(mainPanel.translate("contextmenu.remove"));
        removeMenuItem.addActionListener((ActionEvent e) -> {
            removeItemActionPerformed(e, false);
        });
        add(removeMenuItem);

        removeWithDependenciesMenuItem = new JMenuItem(mainPanel.translate("contextmenu.removeWithDependencies"));
        removeWithDependenciesMenuItem.addActionListener((ActionEvent e) -> {
            removeItemActionPerformed(e, true);
        });
        add(removeWithDependenciesMenuItem);

        undoTagMenuItem = new JMenuItem(mainPanel.translate("contextmenu.undo"));
        undoTagMenuItem.addActionListener(this::undoTagActionPerformed);
        add(undoTagMenuItem);

        exportSelectionMenuItem = new JMenuItem(mainPanel.translate("menu.file.export.selection"));
        exportSelectionMenuItem.addActionListener(mainPanel::exportSelectionActionPerformed);
        add(exportSelectionMenuItem);

        replaceMenuItem = new JMenuItem(mainPanel.translate("button.replace"));
        replaceMenuItem.addActionListener(mainPanel::replaceButtonActionPerformed);
        add(replaceMenuItem);

        replaceNoFillMenuItem = new JMenuItem(mainPanel.translate("button.replaceNoFill"));
        replaceNoFillMenuItem.addActionListener(mainPanel::replaceNoFillButtonActionPerformed);
        add(replaceNoFillMenuItem);

        replaceWithTagMenuItem = new JMenuItem(mainPanel.translate("button.replaceWithTag"));
        replaceWithTagMenuItem.addActionListener(this::replaceWithTagActionPerformed);
        add(replaceWithTagMenuItem);

        rawEditMenuItem = new JMenuItem(mainPanel.translate("contextmenu.rawEdit"));
        rawEditMenuItem.addActionListener(this::rawEditActionPerformed);
        add(rawEditMenuItem);

        jumpToCharacterMenuItem = new JMenuItem(mainPanel.translate("contextmenu.jumpToCharacter"));
        jumpToCharacterMenuItem.addActionListener(this::jumpToCharacterActionPerformed);
        add(jumpToCharacterMenuItem);

        exportJavaSourceMenuItem = new JMenuItem(mainPanel.translate("contextmenu.exportJavaSource"));
        exportJavaSourceMenuItem.addActionListener(mainPanel::exportJavaSourceActionPerformed);
        add(exportJavaSourceMenuItem);

        exportSwfXmlMenuItem = new JMenuItem(mainPanel.translate("contextmenu.exportSwfXml"));
        exportSwfXmlMenuItem.addActionListener(mainPanel::exportSwfXmlActionPerformed);
        add(exportSwfXmlMenuItem);

        importSwfXmlMenuItem = new JMenuItem(mainPanel.translate("contextmenu.importSwfXml"));
        importSwfXmlMenuItem.addActionListener(mainPanel::importSwfXmlActionPerformed);
        add(importSwfXmlMenuItem);

        addTagMenu = new JMenu(mainPanel.translate("contextmenu.addTag"));
        add(addTagMenu);

        moveTagMenu = new JMenu(mainPanel.translate("contextmenu.moveTag"));
        add(moveTagMenu);

        copyTagMenu = new JMenu(mainPanel.translate("contextmenu.copyTag"));
        add(copyTagMenu);

        copyTagWithDependenciesMenu = new JMenu(mainPanel.translate("contextmenu.copyTagWithDependencies"));
        add(copyTagWithDependenciesMenu);

        openSWFInsideTagMenuItem = new JMenuItem(mainPanel.translate("contextmenu.openswfinside"));
        add(openSWFInsideTagMenuItem);
        openSWFInsideTagMenuItem.addActionListener(this::openSwfInsideActionPerformed);

        addAs12ScriptMenuItem = new JMenuItem(mainPanel.translate("contextmenu.addScript"));
        addAs12ScriptMenuItem.addActionListener(this::addAs12ScriptActionPerformed);
        add(addAs12ScriptMenuItem);

        addAs3ClassMenuItem = new JMenuItem(mainPanel.translate("contextmenu.addClass"));
        addAs3ClassMenuItem.addActionListener(this::addAs3ClassActionPerformed);
        add(addAs3ClassMenuItem);

        textSearchMenuItem = new JMenuItem(mainPanel.translate("menu.tools.search"));
        textSearchMenuItem.addActionListener(this::textSearchActionPerformed);
        add(textSearchMenuItem);

        closeMenuItem = new JMenuItem(mainPanel.translate("contextmenu.closeSwf"));
        closeMenuItem.addActionListener(this::closeSwfActionPerformed);
        add(closeMenuItem);

        tagTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {

                    int row = tagTree.getClosestRowForLocation(e.getX(), e.getY());
                    int[] selectionRows = tagTree.getSelectionRows();
                    if (!Helper.contains(selectionRows, row)) {
                        tagTree.setSelectionRow(row);
                    }

                    TreePath[] paths = tagTree.getSelectionPaths();
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
        });
    }

    public void update(final List<TreeItem> items) {

        if (items.isEmpty()) {
            return;
        }

        final List<SWFList> swfs = mainPanel.getSwfs();

        boolean canRemove = true;
        boolean allDoNotHaveDependencies = true;
        for (TreeItem item : items) {
            if (!(item instanceof Tag) && !(item instanceof Frame)) {
                if (item instanceof TagScript) {
                    Tag tag = ((TagScript) item).getTag();
                    if (tag instanceof DoActionTag || tag instanceof DoInitActionTag) {
                        allDoNotHaveDependencies = false;
                    }
                    continue;
                }

                if (item instanceof AS2Package) {
                    allDoNotHaveDependencies = false;
                    continue;
                }

                if (item instanceof CLIPACTIONRECORD) {
                    continue;
                }
                if (item instanceof BUTTONCONDACTION) {
                    continue;
                }

                if (item instanceof ScriptPack) {
                    continue;
                }
                if (item instanceof AS3Package) {
                    continue;
                }

                if (item instanceof FrameScript) {
                    continue;
                }


                canRemove = false;
                break;
            } else {
                allDoNotHaveDependencies = false;
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
        for (TreeItem item : items) {
            if (!(item instanceof Tag)) {
                if (item instanceof TagScript) {
                    Tag tag = ((TagScript) item).getTag();
                    if (tag instanceof DoActionTag || tag instanceof DoInitActionTag) {
                        continue;
                    }
                }

                allSelectedIsTag = false;
                break;
            }
        }

        boolean allSelectedIsBinaryData = true;
        for (TreeItem item : items) {
            if (!(item instanceof DefineBinaryDataTag)) {
                allSelectedIsBinaryData = false;
                break;
            }
        }

        boolean allSelectedIsSwf = true;
        for (TreeItem item : items) {
            if (!(item instanceof SWF) && !(item instanceof SWFList)) {
                allSelectedIsSwf = false;
                break;
            } else if (item instanceof SWF) {
                SWF swf = (SWF) item;
                // Do not allow to close SWF in bundle
                if (swf.swfList != null && swf.swfList.isBundle()) {
                    allSelectedIsSwf = false;
                }
            }
        }

        boolean allSelectedIsInTheSameSwf = true;
        SWF singleSwf = null;
        for (TreeItem item : items) {
            if (item instanceof SWFList) {
                allSelectedIsInTheSameSwf = false;
                break;
            }
            if (singleSwf == null) {
                singleSwf = item.getSwf();
            } else if (singleSwf != item.getSwf()) {
                allSelectedIsInTheSameSwf = false;
                break;
            }
        }

        expandRecursiveMenuItem.setVisible(false);
        removeMenuItem.setVisible(canRemove);
        removeWithDependenciesMenuItem.setVisible(canRemove && !allDoNotHaveDependencies);
        undoTagMenuItem.setVisible(allSelectedIsTag);
        exportSelectionMenuItem.setEnabled(tagTree.hasExportableNodes());
        replaceMenuItem.setVisible(false);
        replaceNoFillMenuItem.setVisible(false);
        replaceWithTagMenuItem.setVisible(false);
        rawEditMenuItem.setVisible(false);
        jumpToCharacterMenuItem.setVisible(false);
        exportJavaSourceMenuItem.setVisible(allSelectedIsSwf);
        exportSwfXmlMenuItem.setVisible(allSelectedIsSwf);
        importSwfXmlMenuItem.setVisible(allSelectedIsSwf);
        closeMenuItem.setVisible(allSelectedIsSwf);
        addTagMenu.setVisible(false);
        moveTagMenu.setVisible(false);
        copyTagMenu.setVisible(false);
        copyTagWithDependenciesMenu.setVisible(false);
        openSWFInsideTagMenuItem.setVisible(false);
        addAs12ScriptMenuItem.setVisible(false);
        addAs3ClassMenuItem.setVisible(false);
        textSearchMenuItem.setVisible(hasScripts || hasTexts);

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

        if (canReplace.test(it -> it instanceof DefineBinaryDataTag)) {
            replaceMenuItem.setVisible(true);
        }

        if (canReplace.test(it -> it instanceof DefineSoundTag)) {
            replaceMenuItem.setVisible(true);
        }

        if (singleSelect) {
            final TreeItem firstItem = items.get(0);

            if (firstItem instanceof FolderItem) {
                if (((FolderItem) firstItem).getName().equals(TagTreeModel.FOLDER_SCRIPTS)) {
                    addAs12ScriptMenuItem.setVisible(true);
                }
            }
            if (firstItem instanceof ClassesListTreeModel) {
                addAs3ClassMenuItem.setVisible(true);
            }

            if (firstItem instanceof CharacterTag) {
                replaceWithTagMenuItem.setVisible(true);
            }

            addTagMenu.removeAll();
            if (firstItem instanceof FolderItem) {
                List<Integer> allowedTagTypes;
                FolderItem folderItem = (FolderItem) firstItem;
                SWF swf = firstItem.getSwf();
                if (folderItem.getName().equals(TagTreeModel.FOLDER_OTHERS)) {
                    TagTreeModel ttm = tagTree.getModel();
                    for (FolderItem emptyFolder : ttm.getEmptyFolders(swf)) {
                        JMenu subMenu = new JMenu(emptyFolder.toString());
                        allowedTagTypes = tagTree.getSwfFolderItemNestedTagIds(emptyFolder.getName(), swf.gfx);
                        addAddTagMenuItems(allowedTagTypes, subMenu, firstItem);
                        if (subMenu.getItemCount() > 0) {
                            addTagMenu.add(subMenu);
                        }
                    }
                }

                allowedTagTypes = tagTree.getSwfFolderItemNestedTagIds(folderItem.getName(), swf.gfx);
                addAddTagMenuItems(allowedTagTypes, addTagMenu, firstItem);
            } else if (firstItem instanceof Tag) {
                List<Integer> allowedTagTypes = tagTree.getNestedTagIds((Tag) firstItem);
                addAddTagMenuItems(allowedTagTypes, addTagMenu, firstItem);
            } else if (firstItem instanceof Frame) {
                List<Integer> allowedTagTypes = tagTree.getFrameNestedTagIds();
                addAddTagMenuItems(allowedTagTypes, addTagMenu, firstItem);
            }

            addTagMenu.setVisible(addTagMenu.getItemCount() > 0);

            if (tagTree.getModel().getChildCount(firstItem) > 0) {
                expandRecursiveMenuItem.setVisible(true);
            }

            if (firstItem instanceof CharacterIdTag && !(firstItem instanceof CharacterTag)) {
                jumpToCharacterMenuItem.setVisible(true);
            }

            if (firstItem instanceof Tag) {
                rawEditMenuItem.setVisible(true);
            }
        }

        if (allSelectedIsInTheSameSwf && allSelectedIsTag && swfs.size() > 1) {
            moveTagMenu.removeAll();
            copyTagMenu.removeAll();
            copyTagWithDependenciesMenu.removeAll();
            for (SWFList targetSwfList : swfs) {
                for (final SWF targetSwf : targetSwfList) {
                    if (targetSwf != singleSwf) {
                        JMenuItem swfItem = new JMenuItem(targetSwf.getShortFileName());
                        swfItem.addActionListener((ActionEvent ae) -> {
                            moveTagActionPerformed(ae, items, targetSwf);
                        });
                        moveTagMenu.add(swfItem);

                        swfItem = new JMenuItem(targetSwf.getShortFileName());
                        swfItem.addActionListener((ActionEvent ae) -> {
                            copyTagActionPerformed(ae, items, targetSwf);
                        });
                        copyTagMenu.add(swfItem);

                        swfItem = new JMenuItem(targetSwf.getShortFileName());
                        swfItem.addActionListener((ActionEvent ae) -> {
                            copyTagWithDependenciesActionPerformed(ae, items, targetSwf);
                        });
                        copyTagWithDependenciesMenu.add(swfItem);
                    }
                }
            }
            moveTagMenu.setVisible(true);
            copyTagMenu.setVisible(true);
            copyTagWithDependenciesMenu.setVisible(true);
        }

        if (allSelectedIsBinaryData) {
            boolean anyInnerSwf = false;
            for (TreeItem item : items) {
                DefineBinaryDataTag binary = (DefineBinaryDataTag) item;

                // inner swf is not loaded yet
                if (binary.innerSwf == null && binary.isSwfData()) {
                    anyInnerSwf = true;
                }
            }

            openSWFInsideTagMenuItem.setVisible(anyInnerSwf);
        }

        for (TreeItem item : items) {
            if (item instanceof Tag) {
                if (((Tag) item).isReadOnly()) {
                    removeMenuItem.setVisible(false);
                    removeWithDependenciesMenuItem.setVisible(false);
                    undoTagMenuItem.setVisible(false);
                    replaceMenuItem.setVisible(false);
                    replaceNoFillMenuItem.setVisible(false);
                    replaceWithTagMenuItem.setVisible(false);
                    rawEditMenuItem.setVisible(false);
                    jumpToCharacterMenuItem.setVisible(false);
                    importSwfXmlMenuItem.setVisible(false);
                    addTagMenu.setVisible(false);
                    moveTagMenu.setVisible(false);
                    openSWFInsideTagMenuItem.setVisible(false);
                }
            }
        }
    }

    private void addAddTagMenuItems(List<Integer> allowedTagTypes, JMenu addTagMenu, TreeItem item) {
        if (allowedTagTypes == null) {
            return;
        }

        for (Integer tagId : allowedTagTypes) {
            final Class<?> cl = TagIdClassMap.getClassByTagId(tagId);
            JMenuItem tagItem = new JMenuItem(cl.getSimpleName());
            tagItem.addActionListener((ActionEvent ae) -> {
                addTagActionPerformed(ae, item, cl);
            });
            addTagMenu.add(tagItem);
        }
    }

    private void addTagActionPerformed(ActionEvent evt, TreeItem firstItem, Class<?> cl) {
        try {
            SWF swf = firstItem.getSwf();
            Tag t = (Tag) cl.getDeclaredConstructor(SWF.class).newInstance(new Object[]{swf});
            swf.addTag(t, firstItem);
            swf.updateCharacters();
            mainPanel.refreshTree(swf);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    private int chechUniqueCharacterId(Tag tag) {
        if (tag instanceof CharacterTag) {
            CharacterTag characterTag = (CharacterTag) tag;
            int characterId = characterTag.getCharacterId();
            SWF swf = tag.getSwf();
            if (swf.getCharacter(characterId) != null) {
                int newCharacterId = swf.getNextCharacterId();
                characterTag.setCharacterId(newCharacterId);
                logger.log(Level.WARNING, "Target SWF already contained chatacter tag with id = {0} => id changed to {1}", new Object[]{characterId, newCharacterId});
                return newCharacterId;
            }

            return characterId;
        }

        return -1;
    }

    private void moveTagActionPerformed(ActionEvent evt, List<TreeItem> items, SWF targetSwf) {
        SWF sourceSwf = items.get(0).getSwf();
        for (TreeItem item : items) {
            Tag tag = (Tag) item;
            sourceSwf.removeTag(tag);
            tag.setSwf(targetSwf, true);
            targetSwf.addTag(tag);
            chechUniqueCharacterId(tag);
            targetSwf.updateCharacters();
            tag.setModified(true);
        }

        sourceSwf.assignExportNamesToSymbols();
        targetSwf.assignExportNamesToSymbols();
        sourceSwf.assignClassesToSymbols();
        targetSwf.assignClassesToSymbols();
        sourceSwf.clearImageCache();
        targetSwf.clearImageCache();
        sourceSwf.updateCharacters();
        targetSwf.updateCharacters();
        sourceSwf.resetTimelines(sourceSwf);
        targetSwf.resetTimelines(targetSwf);
        mainPanel.refreshTree(new SWF[]{sourceSwf, targetSwf});
    }

    private void copyTagActionPerformed(ActionEvent evt, List<TreeItem> items, SWF targetSwf) {
        try {
            for (TreeItem item : items) {
                Tag tag = (Tag) item;
                Tag copyTag = tag.cloneTag();
                copyTag.setSwf(targetSwf, true);
                targetSwf.addTag(copyTag);
                chechUniqueCharacterId(copyTag);
                targetSwf.updateCharacters();
                copyTag.setModified(true);
            }

            targetSwf.assignExportNamesToSymbols();
            targetSwf.assignClassesToSymbols();
            targetSwf.clearImageCache();
            targetSwf.updateCharacters();
            mainPanel.refreshTree(targetSwf);
        } catch (IOException | InterruptedException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    private void copyTagWithDependenciesActionPerformed(ActionEvent evt, List<TreeItem> items, SWF targetSwf) {
        try {
            SWF sourceSwf = items.get(0).getSwf();
            for (TreeItem item : items) {
                Set<Tag> copiedTags = new HashSet<>();
                Set<Tag> newTags = new HashSet<>();
                Set<Integer> needed = new LinkedHashSet<>();
                Map<Integer, Integer> changedCharacterIds = new HashMap<>();

                Tag tag = (Tag) item;
                tag.getNeededCharactersDeep(needed);
                Tag copyTag;

                List<Integer> neededList = new ArrayList<>();
                for (Integer characterId : needed) {
                    neededList.add(characterId);
                }

                // first add dependencies in reverse order
                for (int i = neededList.size() - 1; i >= 0; i--) {
                    int characterId = neededList.get(i);
                    CharacterTag neededTag = sourceSwf.getCharacter(characterId);
                    if (!copiedTags.contains(neededTag)) {
                        copyTag = neededTag.cloneTag();
                        copyTag.setSwf(targetSwf, true);
                        targetSwf.addTag(copyTag);
                        int oldCharacterId = neededTag.getCharacterId();
                        int newCharacterId = chechUniqueCharacterId(copyTag);
                        changedCharacterIds.put(oldCharacterId, newCharacterId);

                        targetSwf.updateCharacters();
                        targetSwf.getCharacters(); // force rebuild character id cache
                        copyTag.setModified(true);
                        copiedTags.add(neededTag);
                        newTags.add(copyTag);
                    }
                }

                copyTag = tag.cloneTag();
                copyTag.setSwf(targetSwf, true);
                targetSwf.addTag(copyTag);
                if (tag instanceof CharacterTag) {
                    CharacterTag characterTag = (CharacterTag) copyTag;
                    int oldCharacterId = characterTag.getCharacterId();
                    int newCharacterId = chechUniqueCharacterId(copyTag);
                    changedCharacterIds.put(oldCharacterId, newCharacterId);
                }

                targetSwf.updateCharacters();
                targetSwf.getCharacters(); // force rebuild character id cache
                copyTag.setModified(true);
                copiedTags.add(tag);
                newTags.add(copyTag);

                for (int oldCharacterId : changedCharacterIds.keySet()) {
                    int newCharacterId = changedCharacterIds.get(oldCharacterId);
                    for (Tag newTag : newTags) {
                        // todo: avoid double replaces
                        newTag.replaceCharacter(oldCharacterId, newCharacterId);
                    }
                }
            }

            targetSwf.assignExportNamesToSymbols();
            targetSwf.assignClassesToSymbols();
            targetSwf.clearImageCache();
            targetSwf.updateCharacters();
            mainPanel.refreshTree(targetSwf);
        } catch (IOException | InterruptedException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    private void openSwfInsideActionPerformed(ActionEvent evt) {
        List<TreeItem> sel = tagTree.getSelected();
        List<DefineBinaryDataTag> binaryDatas = new ArrayList<>();
        for (TreeItem item : sel) {
            DefineBinaryDataTag binaryData = (DefineBinaryDataTag) item;
            if (binaryData.isSwfData()) {
                binaryDatas.add((DefineBinaryDataTag) item);
            }
        }

        mainPanel.loadFromBinaryTag(binaryDatas);
    }

    private void replaceWithTagActionPerformed(ActionEvent evt) {
        TreeItem itemr = tagTree.getCurrentTreeItem();
        if (itemr == null) {
            return;
        }

        SWF swf = itemr.getSwf();
        CharacterTag characterTag = (CharacterTag) itemr;
        int characterId = characterTag.getCharacterId();
        ReplaceCharacterDialog replaceCharacterDialog = new ReplaceCharacterDialog();
        if (replaceCharacterDialog.showDialog(swf, characterId) == AppDialog.OK_OPTION) {
            int newCharacterId = replaceCharacterDialog.getCharacterId();
            swf.replaceCharacterTags(characterTag, newCharacterId);
            mainPanel.refreshTree(swf);
        }
    }

    private void rawEditActionPerformed(ActionEvent evt) {
        TreeItem itemr = tagTree.getCurrentTreeItem();
        if (itemr == null) {
            return;
        }

        mainPanel.showGenericTag((Tag) itemr);
    }

    private void jumpToCharacterActionPerformed(ActionEvent evt) {
        TreeItem itemj = tagTree.getCurrentTreeItem();
        if (itemj == null || !(itemj instanceof CharacterIdTag)) {
            return;
        }

        CharacterIdTag characterIdTag = (CharacterIdTag) itemj;
        mainPanel.setTagTreeSelectedNode(itemj.getSwf().getCharacter(characterIdTag.getCharacterId()));
    }

    private void expandRecursiveActionPerformed(ActionEvent evt) {
        TreePath path = tagTree.getSelectionPath();
        if (path == null) {
            return;
        }
        View.expandTreeNodes(tagTree, path, true);
    }

    private void textSearchActionPerformed(ActionEvent evt) {
        Main.getMainFrame().getPanel().searchInActionScriptOrText(null, Main.getMainFrame().getPanel().getCurrentSwf(), true);
    }

    private void addAs3ClassActionPerformed(ActionEvent evt) {
        List<TreeItem> sel = tagTree.getSelected();
        if (!sel.isEmpty()) {
            if (sel.get(0) instanceof ClassesListTreeModel) {
                ClassesListTreeModel cl = (ClassesListTreeModel) sel.get(0);
                SWF swf = cl.getSwf();
                String className = "";
                String parts[];
                loopinput:
                while (true) {
                    className = View.showInputDialog(AppDialog.translateForDialog("classname", AddScriptDialog.class), className);
                    if (className == null || className.isEmpty()) {
                        return;
                    }


                    parts = className.contains(".") ? className.split("\\.") : new String[]{className};
                    DottedChain classNameDc = new DottedChain(parts, "");
                    for (ABCContainerTag ct : swf.getAbcList()) {
                        if (ct.getABC().findClassByName(classNameDc) > -1) {
                            View.showMessageDialog(mainPanel, AppDialog.translateForDialog("message.classexists", AddScriptDialog.class), mainPanel.translate("error"), JOptionPane.ERROR_MESSAGE);
                            continue loopinput;
                        }
                    }
                    break;
                }

                DoABC2Tag doAbc = new DoABC2Tag(swf);
                doAbc.setTimelined(swf);
                doAbc.name = className;

                List<ABC> abcs = new ArrayList<>();
                for (ABCContainerTag ct : swf.getAbcList()) {
                    abcs.add(ct.getABC());
                }

                ReadOnlyTagList tags = swf.getTags();
                int insertPos = -1;
                for (int i = 0; i < tags.size(); i++) {
                    if (tags.get(i) instanceof ShowFrameTag) {
                        insertPos = i;
                        break;
                    }
                }
                if (insertPos == -1) {
                    insertPos = tags.size();
                }
                swf.addTag(insertPos, doAbc);

                String pkg = className.contains(".") ? className.substring(0, className.lastIndexOf(".")) : "";
                String classSimpleName = className.contains(".") ? className.substring(className.lastIndexOf(".") + 1) : className;
                String fileName = className.replace(".", "/");
                String pkgParts[] = new String[0];
                if (!pkg.isEmpty()) {
                    if (pkg.contains(".")) {
                        pkgParts = pkg.split("\\.");
                    } else {
                        pkgParts = new String[]{pkg};
                    }
                }
                try {
                    ActionScript3Parser parser = new ActionScript3Parser(doAbc.getABC(), abcs);

                    DottedChain dc = new DottedChain(pkgParts, "");
                    String script = "package " + dc.toPrintableString(true) + " {"
                            + "public class " + IdentifiersDeobfuscation.printIdentifier(true, classSimpleName) + " {"
                            + " }"
                            + "}";
                    parser.addScript(script, fileName, 0, 0);
                } catch (IOException | InterruptedException | AVM2ParseException | CompilationException ex) {
                    Logger.getLogger(TagTreeContextMenu.class.getName()).log(Level.SEVERE, "Error during script compilation", ex);
                }

                doAbc.setModified(true);
                swf.clearAllCache();
                swf.setModified(true);
                mainPanel.refreshTree(swf);

                Object item = mainPanel.tagTree.getModel().getScriptsNode(swf);

                TreePath selection = mainPanel.tagTree.getSelectionPath();
                TreePath swfPath = selection.getParentPath();
                TreePath scriptsPath = swfPath.pathByAddingChild(item);
                TreePath classPath = scriptsPath;

                for (int i = 0; i < parts.length; i++) {
                    for (TreeItem ti : tagTree.getModel().getAllChildren(item)) {
                        if (ti instanceof AS3ClassTreeItem) {
                            AS3ClassTreeItem cti = (AS3ClassTreeItem) ti;
                            if (parts[i].equals(cti.getNameWithNamespaceSuffix())) {
                                classPath = classPath.pathByAddingChild(ti);
                                item = ti;
                                break;
                            }
                        }
                    }
                }
                mainPanel.tagTree.setSelectionPath(classPath);
            }
        }
    }

    private void addAs12ScriptActionPerformed(ActionEvent evt) {
        List<TreeItem> sel = tagTree.getSelected();
        if (!sel.isEmpty()) {
            if (sel.get(0) instanceof FolderItem) {

                FolderItem folder = (FolderItem) sel.get(0);
                SWF swf = folder.getSwf();

                AddScriptDialog addScriptDialog = new AddScriptDialog(swf);
                if (addScriptDialog.showDialog() == JOptionPane.OK_OPTION) {
                    if ((addScriptDialog.getScriptType() == AddScriptDialog.TYPE_FRAME)
                            || (addScriptDialog.getScriptType() == AddScriptDialog.TYPE_SPRITE_FRAME)) {

                        Timelined tim = swf;
                        if (addScriptDialog.getScriptType() == AddScriptDialog.TYPE_SPRITE_FRAME) {
                            tim = addScriptDialog.getSprite();
                        }

                        int targetFrame = addScriptDialog.getFrame();

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

                        TreePath selection = mainPanel.tagTree.getSelectionPath();
                        TreePath swfPath = selection.getParentPath();
                        tim.resetTimeline();
                        mainPanel.refreshTree(swf);

                        FolderItem scriptsNode = (FolderItem) mainPanel.tagTree.getModel().getScriptsNode(swf);
                        TreePath scriptsPath = swfPath.pathByAddingChild(scriptsNode);

                        if (addScriptDialog.getScriptType() == AddScriptDialog.TYPE_FRAME) {
                            for (TreeItem subItem : scriptsNode.subItems) {
                                if (subItem instanceof FrameScript) {
                                    if (((FrameScript) subItem).getFrame().frame + 1 == targetFrame) {
                                        TreePath framePath = scriptsPath.pathByAddingChild(subItem);
                                        TreeItem doActionTag = mainPanel.tagTree.getModel().getChild(subItem, 0);
                                        TreePath doActionPath = framePath.pathByAddingChild(doActionTag);
                                        mainPanel.tagTree.setSelectionPath(doActionPath);
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
                                                    TreeItem doActionTag = mainPanel.tagTree.getModel().getChild(fs, 0);
                                                    TreePath doActionPath = framePath.pathByAddingChild(doActionTag);
                                                    mainPanel.tagTree.setSelectionPath(doActionPath);
                                                    break;
                                                }
                                            }
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    } else if (addScriptDialog.getScriptType() == AddScriptDialog.TYPE_BUTTON_EVENT) {
                        DefineButton2Tag button = addScriptDialog.getButton();
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

                        FolderItem scriptsNode = (FolderItem) mainPanel.tagTree.getModel().getScriptsNode(swf);
                        TreePath selection = mainPanel.tagTree.getSelectionPath();
                        TreePath swfPath = selection.getParentPath();
                        TreePath scriptsPath = swfPath.pathByAddingChild(scriptsNode);
                        for (TreeItem subItem : scriptsNode.subItems) {
                            if (subItem instanceof TagScript) {
                                if (((TagScript) subItem).getTag() == button) {
                                    TreePath buttonPath = scriptsPath.pathByAddingChild(subItem);
                                    TreePath buttonCondPath = buttonPath.pathByAddingChild(bca);
                                    mainPanel.tagTree.setSelectionPath(buttonCondPath);
                                    break;
                                }
                            }
                        }
                    } else if (addScriptDialog.getScriptType() == AddScriptDialog.TYPE_INSTANCE_EVENT) {
                        DefineSpriteTag sprite = addScriptDialog.getSprite();
                        int frame = addScriptDialog.getFrame();
                        PlaceObjectTypeTag placeType = addScriptDialog.getPlaceObject();
                        CLIPACTIONS clipActions = null;

                        Timelined tim = swf;
                        if (sprite != null) {
                            tim = sprite;
                        }

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
                            }
                        }
                        if (placeType instanceof PlaceObject3Tag) {
                            PlaceObject3Tag place3 = (PlaceObject3Tag) placeType;
                            if (!place3.placeFlagHasClipActions) {
                                clipActions = place3.clipActions = new CLIPACTIONS();
                                place3.placeFlagHasClipActions = true;
                            }
                        }
                        if (placeType instanceof PlaceObject4Tag) {
                            PlaceObject4Tag place4 = (PlaceObject4Tag) placeType;
                            if (!place4.placeFlagHasClipActions) {
                                clipActions = place4.clipActions = new CLIPACTIONS();
                                place4.placeFlagHasClipActions = true;
                            }
                        }
                        CLIPACTIONRECORD clipActionRecord = new CLIPACTIONRECORD(swf, (Tag) placeType);
                        clipActionRecord.setParentClipActions(clipActions);
                        clipActionRecord.eventFlags.clipEventPress = true;

                        clipActions.clipActionRecords.add(clipActionRecord);
                        clipActions.calculateAllEventFlags();

                        ((Tag) placeType).setModified(true);

                        TreePath selection = mainPanel.tagTree.getSelectionPath();
                        TreePath swfPath = selection.getParentPath();
                        tim.resetTimeline();
                        mainPanel.refreshTree(swf);

                        FolderItem scriptsNode = (FolderItem) mainPanel.tagTree.getModel().getScriptsNode(swf);
                        TreePath scriptsPath = swfPath.pathByAddingChild(scriptsNode);

                        for (TreeItem subItem : scriptsNode.subItems) {
                            if (sprite != null) {
                                if (subItem instanceof TagScript) {
                                    if (((TagScript) subItem).getTag() == sprite) {
                                        TreePath spritePaths = scriptsPath.pathByAddingChild(subItem);
                                        List<TreeItem> frames = ((TagScript) subItem).getFrames();
                                        loopframes:
                                        for (TreeItem f : frames) {
                                            if (f instanceof FrameScript) {
                                                FrameScript fs = (FrameScript) f;
                                                if (fs.getFrame().frame + 1 == frame) {
                                                    TreePath framePath = spritePaths.pathByAddingChild(f);
                                                    List<? extends TreeItem> subs = mainPanel.tagTree.getModel().getAllChildren(fs);
                                                    for (TreeItem t : subs) {
                                                        if (t instanceof TagScript) {
                                                            if (((TagScript) t).getTag() == placeType) {
                                                                TreePath placePath = framePath.pathByAddingChild(t);
                                                                TreePath clipActionRecordPath = placePath.pathByAddingChild(clipActionRecord);
                                                                mainPanel.tagTree.setSelectionPath(clipActionRecordPath);
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
                                        List<? extends TreeItem> subs = mainPanel.tagTree.getModel().getAllChildren(fs);
                                        for (TreeItem t : subs) {
                                            if (t instanceof TagScript) {
                                                if (((TagScript) t).getTag() == placeType) {
                                                    TreePath placePath = framePath.pathByAddingChild(t);
                                                    TreePath clipActionRecordPath = placePath.pathByAddingChild(clipActionRecord);
                                                    mainPanel.tagTree.setSelectionPath(clipActionRecordPath);
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
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
                            DottedChain dc = new DottedChain(parts, "");

                            try {
                                List<Action> actions = parser.actionsFromString("class " + dc.toPrintableString(false) + "{}");
                                doInit.setActions(actions);
                            } catch (ActionParseException | IOException | CompilationException ex) {
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
                            FolderItem scriptsNode = (FolderItem) mainPanel.tagTree.getModel().getScriptsNode(swf);
                            TreePath scriptsPath = swfPath.pathByAddingChild(scriptsNode);
                            String classParts[] = className.contains(".") ? className.split("\\.") : new String[]{className};

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

                        TreePath selection = mainPanel.tagTree.getSelectionPath();
                        TreePath swfPath = selection.getParentPath();
                        FolderItem scriptsNode = (FolderItem) mainPanel.tagTree.getModel().getScriptsNode(swf);
                        TreePath scriptsPath = swfPath.pathByAddingChild(scriptsNode);
                        TreePath doinitPath = scriptsPath.pathByAddingChild(doinit);
                        mainPanel.tagTree.setSelectionPath(doinitPath);
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

    private void populateScriptSubs(TreePath path, TreeItem item, List<TreePath> out) {
        List<? extends TreeItem> subs = tagTree.getModel().getAllChildren(item);
        for (TreeItem t : subs) {
            TreePath tPath = path.pathByAddingChild(t);
            if ((t instanceof TagScript) && (((TagScript) t).getTag() instanceof ASMSource)) {
                out.add(tPath);
            }
            else if (t instanceof ASMSource) {
                out.add(tPath);
            } else {
                populateScriptSubs(tPath, t, out);
            }
        }
    }

    private void getAllAS2PackageScriptPacks(AS2Package pkg, List<ASMSource> out) {
        out.addAll(pkg.scripts.values());
        for (AS2Package sub : pkg.subPackages.values()) {
            getAllAS2PackageScriptPacks(sub, out);
        }
    }

    private void removeItemActionPerformed(ActionEvent evt, boolean removeDependencies) {

        TreePath[] tpsArr = tagTree.getSelectionModel().getSelectionPaths();
        if (tpsArr == null) {
            return;
        }
        List<TreePath> tps = new ArrayList<>(Arrays.asList(tpsArr));

        List<Tag> tagsToRemove = new ArrayList<>();
        List<Object> itemsToRemove = new ArrayList<>();
        List<Object> itemsToRemoveParents = new ArrayList<>();
        List<Object> itemsToRemoveSprites = new ArrayList<>();

        TreeItem itemLast = null;
        int itemCountFix = 0;
        for (int i = 0; i < tps.size(); i++) {
            TreeItem item = (TreeItem) tps.get(i).getLastPathComponent();
            if ((item instanceof FrameScript) || (item instanceof TagScript)) {
                List<TreePath> subs = new ArrayList<>();
                populateScriptSubs(tps.get(i), item, subs);
                int cnt = 0;
                for (TreePath tp : subs) {
                    if (!tps.contains(tp)) {
                        if (cnt > 0) {
                            itemCountFix--;
                        }
                        cnt++;
                        tps.add(tp);
                        itemLast = item;
                    }
                }
            }
        }
        for (TreePath path : tps) {
            TreeItem item = (TreeItem) path.getLastPathComponent();
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
                if (frame.showFrameTag != null) {
                    tagsToRemove.add(frame.showFrameTag);
                } else {
                    // this should be the last frame, so remove the inner tags
                    tagsToRemove.addAll(frame.innerTags);
                }
            } else if (item instanceof BUTTONCONDACTION) {
                itemsToRemove.add(item);
                itemsToRemoveParents.add(((TagScript) path.getParentPath().getLastPathComponent()).getTag());
                itemsToRemoveSprites.add(new Object());
            } else if (item instanceof CLIPACTIONRECORD) {
                itemsToRemove.add(item);
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
            }
        }

        if (tagsToRemove.size() > 0 || itemsToRemove.size() > 0) {
            String confirmationMessage;
            if (tagsToRemove.size() + itemsToRemove.size() == 1) {
                Object toRemove;
                if (tagsToRemove.size() == 1) {
                    toRemove = tagsToRemove.get(0);
                } else {
                    toRemove = itemsToRemove.get(0);
                }
                if (itemLast != null) {
                    toRemove = itemLast;
                }
                confirmationMessage = mainPanel.translate("message.confirm.remove" + (removeDependencies ? "" : ".nodep")).replace("%item%", toRemove.toString());
            } else {
                confirmationMessage = mainPanel.translate("message.confirm.removemultiple" + (removeDependencies ? "" : ".nodep")).replace("%count%", Integer.toString(tagsToRemove.size() + itemsToRemove.size() + itemCountFix));
            }

            if (View.showConfirmDialog(this, confirmationMessage, mainPanel.translate("message.confirm"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                Map<SWF, List<Tag>> tagsToRemoveBySwf = new HashMap<>();
                Set<SWF> swfsToClearCache = new HashSet<>();

                for (int i = 0; i < itemsToRemove.size(); i++) {
                    Object item = itemsToRemove.get(i);
                    if (item instanceof AS3Package) {
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
                        swfsToClearCache.add(sp.getSwf());
                        for (ABCContainerTag ct : sp.getSwf().getAbcList()) {
                            if (ct.getABC() == sp.abc) {
                                ((Tag) ct).setModified(true);
                                break;
                            }
                        }
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
                    if (!tagsToRemoveBySwf.containsKey(swf)) {
                        tagsToRemoveBySwf.put(swf, new ArrayList<>());
                    }

                    tagsToRemoveBySwf.get(swf).add(tag);
                }

                for (SWF swf : tagsToRemoveBySwf.keySet()) {
                    swf.removeTags(tagsToRemoveBySwf.get(swf), removeDependencies);
                }

                for (SWF swf : swfsToClearCache) {
                    swf.clearAllCache();
                }

                mainPanel.refreshTree();
            }
        }
    }

    private void undoTagActionPerformed(ActionEvent evt) {
        List<TreeItem> sel = tagTree.getSelected();

        for (TreeItem item : sel) {
            if (item instanceof Tag) {
                try {
                    Tag tag = (Tag) item;
                    tag.undo();
                    tag.getSwf().clearAllCache();
                    tagTree.getModel().updateNode(item);
                } catch (InterruptedException | IOException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            }
        }

        mainPanel.repaintTree();
    }

    private void closeSwfActionPerformed(ActionEvent evt) {
        List<TreeItem> sel = tagTree.getSelected();
        for (TreeItem item : sel) {
            if (item instanceof SWF) {
                SWF swf = (SWF) item;
                if (swf.binaryData != null) {
                    // embedded swf
                    swf.binaryData.innerSwf = null;
                    swf.clearTagSwfs();
                    mainPanel.refreshTree();
                } else {
                    Main.closeFile(swf.swfList);
                }
            } else if (item instanceof SWFList) {
                Main.closeFile((SWFList) item);
            }
        }
    }
}
