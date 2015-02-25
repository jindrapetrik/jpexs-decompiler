/*
 *  Copyright (C) 2010-2015 JPEXS
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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.gui.Main;
import com.jpexs.decompiler.flash.gui.MainFrameRibbonMenu;
import com.jpexs.decompiler.flash.gui.MainPanel;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.tags.DefineBinaryDataTag;
import com.jpexs.decompiler.flash.tags.DefineSoundTag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.DoActionTag;
import com.jpexs.decompiler.flash.tags.DoInitActionTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.CharacterIdTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.timeline.Frame;
import com.jpexs.decompiler.flash.timeline.TagScript;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.treeitems.FolderItem;
import com.jpexs.decompiler.flash.treeitems.SWFList;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import com.jpexs.helpers.Helper;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

/**
 *
 * @author JPEXS
 */
public class TagTreeContextMenu extends JPopupMenu implements ActionListener {

    private static final String ACTION_RAW_EDIT = "RAWEDIT";

    private static final String ACTION_JUMP_TO_CHARACTER = "JUMPTOCHARACTER";

    private static final String ACTION_REMOVE_ITEM = "REMOVEITEM";

    private static final String ACTION_REMOVE_ITEM_WITH_DEPENDENCIES = "REMOVEITEMWITHDEPENDENCIES";

    private static final String ACTION_UNDO_TAG = "UNDOTAG";

    private static final String ACTION_CLOSE_SWF = "CLOSESWF";

    private static final String ACTION_EXPAND_RECURSIVE = "EXPANDRECURSIVE";

    private static final String ACTION_OPEN_SWFINSIDE = "OPENSWFINSIDE";

    private final MainPanel mainPanel;

    private final TagTree tagTree;

    private JMenuItem expandRecursiveMenuItem;

    private JMenuItem removeMenuItem;

    private JMenuItem removeWithDependenciesMenuItem;

    private JMenuItem undoTagMenuItem;

    private JMenuItem exportSelectionMenuItem;

    private JMenuItem replaceMenuItem;

    private JMenuItem rawEditMenuItem;

    private JMenuItem jumpToCharacterMenuItem;

    private JMenuItem exportJavaSourceMenuItem;

    private JMenuItem exportSwfXmlMenuItem;

    private JMenuItem importSwfXmlMenuItem;

    private JMenuItem closeMenuItem;

    private JMenu addTagMenu;

    private JMenu moveTagMenu;

    private JMenu copyTagMenu;

    private JMenuItem openSWFInsideTagMenuItem;

    public TagTreeContextMenu(final TagTree tagTree, MainPanel mainPanel) {
        this.mainPanel = mainPanel;
        this.tagTree = tagTree;
        expandRecursiveMenuItem = new JMenuItem(mainPanel.translate("contextmenu.expandAll"));
        expandRecursiveMenuItem.addActionListener(this);
        expandRecursiveMenuItem.setActionCommand(ACTION_EXPAND_RECURSIVE);
        add(expandRecursiveMenuItem);

        removeMenuItem = new JMenuItem(mainPanel.translate("contextmenu.remove"));
        removeMenuItem.addActionListener(this);
        removeMenuItem.setActionCommand(ACTION_REMOVE_ITEM);
        add(removeMenuItem);

        removeWithDependenciesMenuItem = new JMenuItem(mainPanel.translate("contextmenu.removeWithDependencies"));
        removeWithDependenciesMenuItem.addActionListener(this);
        removeWithDependenciesMenuItem.setActionCommand(ACTION_REMOVE_ITEM_WITH_DEPENDENCIES);
        add(removeWithDependenciesMenuItem);

        undoTagMenuItem = new JMenuItem(mainPanel.translate("contextmenu.undo"));
        undoTagMenuItem.addActionListener(this);
        undoTagMenuItem.setActionCommand(ACTION_UNDO_TAG);
        add(undoTagMenuItem);

        exportSelectionMenuItem = new JMenuItem(mainPanel.translate("menu.file.export.selection"));
        exportSelectionMenuItem.setActionCommand(MainFrameRibbonMenu.ACTION_EXPORT_SEL);
        exportSelectionMenuItem.addActionListener(mainPanel);
        add(exportSelectionMenuItem);

        replaceMenuItem = new JMenuItem(mainPanel.translate("button.replace"));
        replaceMenuItem.setActionCommand(MainPanel.ACTION_REPLACE);
        replaceMenuItem.addActionListener(mainPanel);
        add(replaceMenuItem);

        rawEditMenuItem = new JMenuItem(mainPanel.translate("contextmenu.rawEdit"));
        rawEditMenuItem.setActionCommand(ACTION_RAW_EDIT);
        rawEditMenuItem.addActionListener(this);
        rawEditMenuItem.setVisible(false);
        add(rawEditMenuItem);

        jumpToCharacterMenuItem = new JMenuItem(mainPanel.translate("contextmenu.jumpToCharacter"));
        jumpToCharacterMenuItem.setActionCommand(ACTION_JUMP_TO_CHARACTER);
        jumpToCharacterMenuItem.addActionListener(this);
        jumpToCharacterMenuItem.setVisible(false);
        add(jumpToCharacterMenuItem);

        exportJavaSourceMenuItem = new JMenuItem(mainPanel.translate("contextmenu.exportJavaSource"));
        exportJavaSourceMenuItem.setActionCommand(MainFrameRibbonMenu.ACTION_EXPORT_JAVA_SOURCE);
        exportJavaSourceMenuItem.addActionListener(mainPanel);
        add(exportJavaSourceMenuItem);

        exportSwfXmlMenuItem = new JMenuItem(mainPanel.translate("contextmenu.exportSwfXml"));
        exportSwfXmlMenuItem.setActionCommand(MainFrameRibbonMenu.ACTION_EXPORT_SWF_XML);
        exportSwfXmlMenuItem.addActionListener(mainPanel);
        add(exportSwfXmlMenuItem);

        importSwfXmlMenuItem = new JMenuItem(mainPanel.translate("contextmenu.importSwfXml"));
        importSwfXmlMenuItem.setActionCommand(MainFrameRibbonMenu.ACTION_IMPORT_SWF_XML);
        importSwfXmlMenuItem.addActionListener(mainPanel);
        add(importSwfXmlMenuItem);

        closeMenuItem = new JMenuItem(mainPanel.translate("contextmenu.closeSwf"));
        closeMenuItem.setActionCommand(ACTION_CLOSE_SWF);
        closeMenuItem.addActionListener(this);
        add(closeMenuItem);

        addTagMenu = new JMenu(mainPanel.translate("contextmenu.addTag"));
        add(addTagMenu);

        moveTagMenu = new JMenu(mainPanel.translate("contextmenu.moveTag"));
        add(moveTagMenu);

        copyTagMenu = new JMenu(mainPanel.translate("contextmenu.copyTag"));
        add(copyTagMenu);

        openSWFInsideTagMenuItem = new JMenuItem(mainPanel.translate("contextmenu.openswfinside"));
        add(openSWFInsideTagMenuItem);
        openSWFInsideTagMenuItem.setActionCommand(ACTION_OPEN_SWFINSIDE);
        openSWFInsideTagMenuItem.addActionListener(this);

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

    public void update(List<TreeItem> items) {

        if (items.isEmpty()) {
            return;
        }

        final List<SWFList> swfs = mainPanel.getSwfs();

        boolean allSelectedIsTagOrFrame = true;
        for (TreeItem item : items) {
            if (!(item instanceof Tag) && !(item instanceof Frame)) {
                if (item instanceof TagScript) {
                    Tag tag = ((TagScript) item).getTag();
                    if (tag instanceof DoActionTag || tag instanceof DoInitActionTag) {
                        continue;
                    }
                }

                allSelectedIsTagOrFrame = false;
                break;
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
                if (swf.swfList != null && swf.swfList.isBundle) {
                    allSelectedIsSwf = false;
                }
            }
        }

        expandRecursiveMenuItem.setVisible(false);
        removeMenuItem.setVisible(allSelectedIsTagOrFrame);
        removeWithDependenciesMenuItem.setVisible(allSelectedIsTagOrFrame);
        undoTagMenuItem.setVisible(allSelectedIsTag);
        exportSelectionMenuItem.setEnabled(tagTree.hasExportableNodes());
        replaceMenuItem.setVisible(false);
        rawEditMenuItem.setVisible(false);
        jumpToCharacterMenuItem.setVisible(false);
        exportJavaSourceMenuItem.setVisible(allSelectedIsSwf);
        exportSwfXmlMenuItem.setVisible(allSelectedIsSwf);
        importSwfXmlMenuItem.setVisible(allSelectedIsSwf);
        closeMenuItem.setVisible(allSelectedIsSwf);
        addTagMenu.setVisible(false);
        moveTagMenu.setVisible(false);
        copyTagMenu.setVisible(false);
        openSWFInsideTagMenuItem.setVisible(false);

        final TreeItem firstItem = items.get(0);
        boolean singleSelect = items.size() == 1;

        if (singleSelect) {
            // replace
            if (firstItem instanceof ImageTag && ((ImageTag) firstItem).importSupported()) {
                replaceMenuItem.setVisible(true);
            }

            if (firstItem instanceof ShapeTag) {
                replaceMenuItem.setVisible(true);
            }

            if (firstItem instanceof DefineBinaryDataTag) {
                replaceMenuItem.setVisible(true);
            }

            if (firstItem instanceof DefineSoundTag) {
                replaceMenuItem.setVisible(true);
            }

            List<Integer> allowedTagTypes = null;
            if (firstItem instanceof FolderItem) {
                allowedTagTypes = tagTree.getSwfFolderItemNestedTagIds(((FolderItem) firstItem).getName(), firstItem.getSwf().gfx);
            } else if (firstItem instanceof DefineSpriteTag) {
                allowedTagTypes = tagTree.getSpriteNestedTagIds();
            }

            addTagMenu.removeAll();
            if (allowedTagTypes != null) {
                for (Integer tagId : allowedTagTypes) {
                    final Class cl = TagIdClassMap.getClassByTagId(tagId);
                    JMenuItem tagItem = new JMenuItem(cl.getSimpleName());
                    tagItem.addActionListener(new ActionListener() {

                        @Override
                        @SuppressWarnings("unchecked")
                        public void actionPerformed(ActionEvent ae) {
                            try {
                                SWF swf = firstItem.getSwf();
                                Tag t = (Tag) cl.getDeclaredConstructor(SWF.class).newInstance(new Object[]{swf});
                                boolean isDefineSprite = firstItem instanceof DefineSpriteTag;
                                Timelined timelined = isDefineSprite ? (DefineSpriteTag) firstItem : swf;
                                t.setTimelined(timelined);
                                if (isDefineSprite) {
                                    ((DefineSpriteTag) firstItem).subTags.add(t);
                                } else {
                                    swf.tags.add(t);
                                }
                                timelined.getTimeline().reset();
                                swf.updateCharacters();
                                mainPanel.refreshTree();
                            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException ex) {
                                Logger.getLogger(TagTreeContextMenu.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    });
                    addTagMenu.add(tagItem);
                }
                addTagMenu.setVisible(true);
            }

            if (firstItem instanceof Tag && swfs.size() > 1) {
                final Tag tag = (Tag) firstItem;
                moveTagMenu.removeAll();
                copyTagMenu.removeAll();
                for (SWFList targetSwfList : swfs) {
                    for (final SWF targetSwf : targetSwfList) {
                        if (targetSwf != tag.getSwf()) {
                            JMenuItem swfItem = new JMenuItem(targetSwf.getShortFileName());
                            swfItem.addActionListener(new ActionListener() {

                                @Override
                                public void actionPerformed(ActionEvent ae) {
                                    SWF sourceSwf = tag.getSwf();
                                    sourceSwf.tags.remove(tag);
                                    tag.setSwf(targetSwf);
                                    targetSwf.tags.add(tag);
                                    tag.setModified(true);
                                    sourceSwf.assignExportNamesToSymbols();
                                    targetSwf.assignExportNamesToSymbols();
                                    sourceSwf.assignClassesToSymbols();
                                    targetSwf.assignClassesToSymbols();
                                    sourceSwf.clearImageCache();
                                    targetSwf.clearImageCache();
                                    mainPanel.refreshTree();
                                }
                            });
                            moveTagMenu.add(swfItem);

                            swfItem = new JMenuItem(targetSwf.getShortFileName());
                            swfItem.addActionListener(new ActionListener() {

                                @Override
                                public void actionPerformed(ActionEvent ae) {
                                    try {
                                        Tag copyTag = tag.cloneTag();
                                        copyTag.setSwf(targetSwf);
                                        targetSwf.tags.add(copyTag);
                                        copyTag.setModified(true);
                                        targetSwf.assignExportNamesToSymbols();
                                        targetSwf.assignClassesToSymbols();
                                        targetSwf.clearImageCache();
                                        mainPanel.refreshTree();
                                    } catch (IOException | InterruptedException ex) {
                                        Logger.getLogger(TagTreeContextMenu.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            });
                            copyTagMenu.add(swfItem);
                        }
                    }
                }
                moveTagMenu.setVisible(true);
                copyTagMenu.setVisible(true);
            }

            if (tagTree.getModel().getChildCount(firstItem) > 0) {
                expandRecursiveMenuItem.setVisible(true);
            }

            if (firstItem instanceof CharacterIdTag && !(firstItem instanceof CharacterTag)) {
                jumpToCharacterMenuItem.setVisible(true);
            }

            if (firstItem instanceof Tag) {
                rawEditMenuItem.setVisible(firstItem instanceof Tag);
            }
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
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case ACTION_OPEN_SWFINSIDE: {
                List<TreeItem> sel = tagTree.getSelected(tagTree);
                List<DefineBinaryDataTag> binaryDatas = new ArrayList<>();
                for (TreeItem item : sel) {
                    DefineBinaryDataTag binaryData = (DefineBinaryDataTag) item;
                    if (binaryData.isSwfData()) {
                        binaryDatas.add((DefineBinaryDataTag) item);
                    }
                }

                mainPanel.loadFromBinaryTag(binaryDatas);
                break;
            }
            case ACTION_RAW_EDIT: {
                TreeItem itemr = tagTree.getCurrentTreeItem();
                if (itemr == null) {
                    return;
                }

                mainPanel.showGenericTag((Tag) itemr);
                break;
            }
            case ACTION_JUMP_TO_CHARACTER: {
                TreeItem itemj = tagTree.getCurrentTreeItem();
                if (itemj == null || !(itemj instanceof CharacterIdTag)) {
                    return;
                }

                CharacterIdTag characterIdTag = (CharacterIdTag) itemj;
                mainPanel.setTagTreeSelectedNode(itemj.getSwf().getCharacter(characterIdTag.getCharacterId()));
                break;
            }
            case ACTION_EXPAND_RECURSIVE: {
                TreePath path = tagTree.getSelectionPath();
                if (path == null) {
                    return;
                }
                View.expandTreeNodes(tagTree, path, true);
                break;
            }
            case ACTION_REMOVE_ITEM:
            case ACTION_REMOVE_ITEM_WITH_DEPENDENCIES: {
                List<TreeItem> sel = tagTree.getSelected(tagTree);

                List<Tag> tagsToRemove = new ArrayList<>();
                for (TreeItem item : sel) {
                    if (item instanceof Tag) {
                        tagsToRemove.add((Tag) item);
                    } else if (item instanceof TagScript) {
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
                    }
                }

                boolean removeDependencies = e.getActionCommand().equals(ACTION_REMOVE_ITEM_WITH_DEPENDENCIES);
                if (tagsToRemove.size() == 1) {
                    Tag tag = tagsToRemove.get(0);
                    if (View.showConfirmDialog(this, mainPanel.translate("message.confirm.remove").replace("%item%", tag.toString()), mainPanel.translate("message.confirm"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                        tag.getSwf().removeTag(tag, removeDependencies);
                        mainPanel.refreshTree();
                    }
                } else if (tagsToRemove.size() > 1) {
                    if (View.showConfirmDialog(this, mainPanel.translate("message.confirm.removemultiple").replace("%count%", Integer.toString(tagsToRemove.size())), mainPanel.translate("message.confirm"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                        for (Tag tag : tagsToRemove) {
                            tag.getSwf().removeTag(tag, removeDependencies);
                        }
                        mainPanel.refreshTree();
                    }
                }
                break;
            }
            case ACTION_UNDO_TAG: {
                List<TreeItem> sel = tagTree.getSelected(tagTree);

                for (TreeItem item : sel) {
                    if (item instanceof Tag) {
                        try {
                            Tag tag = (Tag) item;
                            tag.undo();
                            tag.getSwf().clearAllCache();
                        } catch (InterruptedException | IOException ex) {
                            Logger.getLogger(TagTreeContextMenu.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }

                mainPanel.refreshTree();
                break;
            }
            case ACTION_CLOSE_SWF: {
                List<TreeItem> sel = tagTree.getSelected(tagTree);
                for (TreeItem item : sel) {
                    if (item instanceof SWF) {
                        SWF swf = (SWF) item;
                        if (swf.binaryData != null) {
                            // embedded swf
                            swf.binaryData.innerSwf = null;
                            mainPanel.refreshTree();
                        } else {
                            Main.closeFile(swf.swfList);
                        }
                    } else if (item instanceof SWFList) {
                        Main.closeFile((SWFList) item);
                    }
                }
                break;
            }
        }
    }
}
