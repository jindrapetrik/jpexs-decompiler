/*
 *  Copyright (C) 2010-2018 JPEXS
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
package com.jpexs.decompiler.flash.gui.dumpview;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ABCInputStream;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionListReader;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.dumpview.DumpInfo;
import com.jpexs.decompiler.flash.dumpview.DumpInfoSpecial;
import com.jpexs.decompiler.flash.dumpview.DumpInfoSpecialType;
import com.jpexs.decompiler.flash.dumpview.DumpInfoSwfNode;
import com.jpexs.decompiler.flash.gui.Main;
import com.jpexs.decompiler.flash.gui.MainPanel;
import com.jpexs.decompiler.flash.gui.TreeNodeType;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.gui.tagtree.TagTree;
import com.jpexs.decompiler.flash.tags.DefineBinaryDataTag;
import com.jpexs.decompiler.flash.tags.DefineBitsJPEG2Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsJPEG3Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsJPEG4Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsLossless2Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsLosslessTag;
import com.jpexs.decompiler.flash.tags.DefineBitsTag;
import com.jpexs.decompiler.flash.tags.DefineButton2Tag;
import com.jpexs.decompiler.flash.tags.DefineButtonTag;
import com.jpexs.decompiler.flash.tags.DefineEditTextTag;
import com.jpexs.decompiler.flash.tags.DefineFont2Tag;
import com.jpexs.decompiler.flash.tags.DefineFont3Tag;
import com.jpexs.decompiler.flash.tags.DefineFont4Tag;
import com.jpexs.decompiler.flash.tags.DefineFontTag;
import com.jpexs.decompiler.flash.tags.DefineMorphShape2Tag;
import com.jpexs.decompiler.flash.tags.DefineMorphShapeTag;
import com.jpexs.decompiler.flash.tags.DefineShape2Tag;
import com.jpexs.decompiler.flash.tags.DefineShape3Tag;
import com.jpexs.decompiler.flash.tags.DefineShape4Tag;
import com.jpexs.decompiler.flash.tags.DefineShapeTag;
import com.jpexs.decompiler.flash.tags.DefineSoundTag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.DefineText2Tag;
import com.jpexs.decompiler.flash.tags.DefineTextTag;
import com.jpexs.decompiler.flash.tags.DefineVideoStreamTag;
import com.jpexs.decompiler.flash.tags.DoABC2Tag;
import com.jpexs.decompiler.flash.tags.DoABCTag;
import com.jpexs.decompiler.flash.tags.DoActionTag;
import com.jpexs.decompiler.flash.tags.DoInitActionTag;
import com.jpexs.decompiler.flash.tags.FileAttributesTag;
import com.jpexs.decompiler.flash.tags.MetadataTag;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.PlaceObject3Tag;
import com.jpexs.decompiler.flash.tags.PlaceObject4Tag;
import com.jpexs.decompiler.flash.tags.PlaceObjectTag;
import com.jpexs.decompiler.flash.tags.RemoveObject2Tag;
import com.jpexs.decompiler.flash.tags.RemoveObjectTag;
import com.jpexs.decompiler.flash.tags.SetBackgroundColorTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.SoundStreamHead2Tag;
import com.jpexs.decompiler.flash.tags.SoundStreamHeadTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.gfx.DefineCompactedFont;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.MemoryInputStream;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicLabelUI;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author JPEXS
 */
public class DumpTree extends JTree {

    private static final Logger logger = Logger.getLogger(DumpTree.class.getName());

    private final MainPanel mainPanel;

    public class DumpTreeCellRenderer extends DefaultTreeCellRenderer {

        public DumpTreeCellRenderer() {
            setUI(new BasicLabelUI());
            setOpaque(false);
            setBackgroundNonSelectionColor(Color.white);
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            Component ret = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            if (ret instanceof JLabel) {
                JLabel lab = (JLabel) ret;
                if (value instanceof DumpInfo) {
                    DumpInfo di = (DumpInfo) value;
                    TreeNodeType nodeType = null;
                    if ("".equals(di.type)) {
                        nodeType = TreeNodeType.FLASH;
                    } else if ("TAG".equals(di.type)) {
                        String name = di.name;
                        if (name.contains(" ")) {
                            name = name.substring(0, name.indexOf(' ')).trim();
                        }
                        switch (name) {
                            case DefineFontTag.NAME:
                            case DefineFont2Tag.NAME:
                            case DefineFont3Tag.NAME:
                            case DefineFont4Tag.NAME:
                            case DefineCompactedFont.NAME:
                                nodeType = TreeNodeType.FONT;
                                break;
                            case DefineTextTag.NAME:
                            case DefineText2Tag.NAME:
                            case DefineEditTextTag.NAME:
                                nodeType = TreeNodeType.TEXT;
                                break;
                            case DefineBitsTag.NAME:
                            case DefineBitsJPEG2Tag.NAME:
                            case DefineBitsJPEG3Tag.NAME:
                            case DefineBitsJPEG4Tag.NAME:
                            case DefineBitsLosslessTag.NAME:
                            case DefineBitsLossless2Tag.NAME:
                                nodeType = TreeNodeType.IMAGE;
                                break;
                            case DefineShapeTag.NAME:
                            case DefineShape2Tag.NAME:
                            case DefineShape3Tag.NAME:
                            case DefineShape4Tag.NAME:
                                nodeType = TreeNodeType.SHAPE;
                                break;
                            case DefineMorphShapeTag.NAME:
                            case DefineMorphShape2Tag.NAME:
                                nodeType = TreeNodeType.MORPH_SHAPE;
                                break;
                            case DefineSpriteTag.NAME:
                                nodeType = TreeNodeType.SPRITE;
                                break;
                            case DefineButtonTag.NAME:
                            case DefineButton2Tag.NAME:
                                nodeType = TreeNodeType.BUTTON;
                                break;
                            case DefineVideoStreamTag.NAME:
                                nodeType = TreeNodeType.MOVIE;
                                break;

                            case DefineSoundTag.NAME:
                            case SoundStreamHeadTag.NAME:
                            case SoundStreamHead2Tag.NAME:
                                nodeType = TreeNodeType.SOUND;
                                break;
                            case DefineBinaryDataTag.NAME:
                                nodeType = TreeNodeType.BINARY_DATA;
                                break;
                            case DoActionTag.NAME:
                            case DoInitActionTag.NAME:
                            case DoABCTag.NAME:
                            case DoABC2Tag.NAME:
                                nodeType = TreeNodeType.AS;
                                break;
                            case ShowFrameTag.NAME:
                                nodeType = TreeNodeType.FRAME; //show_frame?
                                break;
                            case SetBackgroundColorTag.NAME:
                                nodeType = TreeNodeType.SET_BACKGROUNDCOLOR;
                                break;
                            case FileAttributesTag.NAME:
                                nodeType = TreeNodeType.FILE_ATTRIBUTES;
                                break;
                            case MetadataTag.NAME:
                                nodeType = TreeNodeType.METADATA;
                                break;
                            case PlaceObjectTag.NAME:
                            case PlaceObject2Tag.NAME:
                            case PlaceObject3Tag.NAME:
                            case PlaceObject4Tag.NAME:
                                nodeType = TreeNodeType.PLACE_OBJECT;
                                break;
                            case RemoveObjectTag.NAME:
                            case RemoveObject2Tag.NAME:
                                nodeType = TreeNodeType.REMOVE_OBJECT;
                                break;
                            default:
                                nodeType = TreeNodeType.OTHER_TAG;
                        }
                    }
                    if (nodeType != null) {
                        lab.setIcon(TagTree.getIconForType(nodeType));
                    }
                }
            }
            return ret;

        }
    }

    public DumpTree(DumpTreeModel treeModel, MainPanel mainPanel) {
        super(treeModel);
        this.mainPanel = mainPanel;
        setCellRenderer(new DumpTreeCellRenderer());
        setRootVisible(false);
        setBackground(Color.white);
        setUI(new BasicTreeUI() {
            {
                setHashColor(Color.gray);
            }
        });
    }

    public void createContextMenu() {
        final JPopupMenu contextPopupMenu = new JPopupMenu();

        final JMenuItem expandRecursiveMenuItem = new JMenuItem(mainPanel.translate("contextmenu.expandAll"));
        expandRecursiveMenuItem.addActionListener(this::expandRecursiveButtonActionPerformed);
        contextPopupMenu.add(expandRecursiveMenuItem);

        final JMenuItem saveToFileMenuItem = new JMenuItem(mainPanel.translate("contextmenu.saveToFile"));
        saveToFileMenuItem.addActionListener(this::saveToFileButtonActionPerformed);
        contextPopupMenu.add(saveToFileMenuItem);

        final JMenuItem saveUncompressedToFileMenuItem = new JMenuItem(mainPanel.translate("contextmenu.saveUncompressedToFile"));
        saveUncompressedToFileMenuItem.addActionListener(this::saveUncompressedToFileButtonActionPerformed);
        contextPopupMenu.add(saveUncompressedToFileMenuItem);

        final JMenuItem closeSelectionMenuItem = new JMenuItem(mainPanel.translate("contextmenu.closeSwf"));
        closeSelectionMenuItem.addActionListener(this::closeSwfButtonActionPerformed);
        contextPopupMenu.add(closeSelectionMenuItem);

        final JMenuItem parseActionsMenuItem = new JMenuItem(mainPanel.translate("contextmenu.parseActions"));
        parseActionsMenuItem.addActionListener(this::parseActionsButtonActionPerformed);
        contextPopupMenu.add(parseActionsMenuItem);

        final JMenuItem parseAbcMenuItem = new JMenuItem(mainPanel.translate("contextmenu.parseABC"));
        parseAbcMenuItem.addActionListener(this::parseAbcButtonActionPerformed);
        contextPopupMenu.add(parseAbcMenuItem);

        final JMenuItem parseInstructionsMenuItem = new JMenuItem(mainPanel.translate("contextmenu.parseInstructions"));
        parseInstructionsMenuItem.addActionListener(this::parseInstructionsButtonActionPerformed);
        contextPopupMenu.add(parseInstructionsMenuItem);

        final JMenuItem gotoTagMenuItem = new JMenuItem(mainPanel.translate("contextmenu.showInResources"));
        gotoTagMenuItem.addActionListener(this::gotoTagButtonActionPerformed);
        contextPopupMenu.add(gotoTagMenuItem);

        final JMenuItem gotoActionListMenuItem = new JMenuItem(mainPanel.translate("contextmenu.showInResources"));
        gotoActionListMenuItem.addActionListener(this::gotoActionListButtonActionPerformed);
        contextPopupMenu.add(gotoActionListMenuItem);

        final JMenuItem gotoMethodMenuItem = new JMenuItem(mainPanel.translate("contextmenu.showInResources"));
        gotoMethodMenuItem.addActionListener(this::gotoMethodButtonActionPerformed);
        contextPopupMenu.add(gotoMethodMenuItem);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {

                    int row = getClosestRowForLocation(e.getX(), e.getY());
                    int[] selectionRows = getSelectionRows();
                    if (!Helper.contains(selectionRows, row)) {
                        setSelectionRow(row);
                    }

                    TreePath[] paths = getSelectionPaths();
                    if (paths == null || paths.length == 0) {
                        return;
                    }

                    closeSelectionMenuItem.setVisible(false);
                    expandRecursiveMenuItem.setVisible(false);
                    saveToFileMenuItem.setVisible(false);
                    saveUncompressedToFileMenuItem.setVisible(false);
                    parseActionsMenuItem.setVisible(false);
                    parseAbcMenuItem.setVisible(false);
                    parseInstructionsMenuItem.setVisible(false);
                    gotoTagMenuItem.setVisible(false);
                    gotoActionListMenuItem.setVisible(false);
                    gotoMethodMenuItem.setVisible(false);

                    if (paths.length == 1) {
                        DumpInfo treeNode = (DumpInfo) paths[0].getLastPathComponent();
                        DumpInfoSpecialType specialType = getSpecialType(treeNode);

                        if (treeNode instanceof DumpInfoSwfNode) {
                            closeSelectionMenuItem.setVisible(true);
                        }

                        if (treeNode.getEndByte() - treeNode.startByte > 3) {
                            saveToFileMenuItem.setVisible(true);
                            if (specialType == DumpInfoSpecialType.ZLIB_DATA) {
                                saveUncompressedToFileMenuItem.setVisible(true);
                            }
                        }

                        boolean noChild = treeNode.getChildCount() == 0;

                        if (noChild) {
                            switch (specialType) {
                                case ACTION_BYTES:
                                    parseActionsMenuItem.setVisible(true);
                                    break;
                                case ABC_BYTES:
                                    parseAbcMenuItem.setVisible(true);
                                    break;
                                case ABC_CODE:
                                    parseInstructionsMenuItem.setVisible(true);
                                    break;
                            }
                        }

                        switch (specialType) {
                            case TAG:
                                gotoTagMenuItem.setVisible(true);
                                break;
                            case ACTION_BYTES:
                                gotoActionListMenuItem.setVisible(true);
                                break;
                            case ABC_CODE:
                            case ABC_METHOD_BODY:
                                gotoMethodMenuItem.setVisible(true);
                                break;
                        }

                        TreeModel model = getModel();
                        expandRecursiveMenuItem.setVisible(model.getChildCount(treeNode) > 0);
                    }

                    contextPopupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    private DumpInfoSpecialType getSpecialType(DumpInfo dumpInfo) {
        DumpInfoSpecialType specialType = dumpInfo instanceof DumpInfoSpecial
                ? ((DumpInfoSpecial) dumpInfo).specialType
                : DumpInfoSpecialType.NONE;
        return specialType;
    }

    private void expandRecursiveButtonActionPerformed(ActionEvent evt) {
        TreePath path = getSelectionPath();
        if (path == null) {
            return;
        }
        View.expandTreeNodes(this, path, true);
    }

    private void saveToFileButtonActionPerformed(ActionEvent evt) {
        saveToFileButtonActionPerformed(false);
    }

    private void saveUncompressedToFileButtonActionPerformed(ActionEvent evt) {
        saveToFileButtonActionPerformed(true);
    }

    private void saveToFileButtonActionPerformed(boolean decompress) {
        TreePath[] paths = getSelectionPaths();
        DumpInfo dumpInfo = (DumpInfo) paths[0].getLastPathComponent();
        JFileChooser fc = new JFileChooser();
        String selDir = Configuration.lastOpenDir.get();
        fc.setCurrentDirectory(new File(selDir));
        JFrame f = new JFrame();
        View.setWindowIcon(f);
        if (fc.showSaveDialog(f) == JFileChooser.APPROVE_OPTION) {
            File sf = Helper.fixDialogFile(fc.getSelectedFile());
            try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(sf))) {
                byte[] data = DumpInfoSwfNode.getSwfNode(dumpInfo).getSwf().originalUncompressedData;
                if (decompress) {
                    fos.write(SWFInputStream.uncompressByteArray(data, (int) dumpInfo.startByte, (int) (dumpInfo.getEndByte() - dumpInfo.startByte + 1)));
                } else {
                    fos.write(data, (int) dumpInfo.startByte, (int) (dumpInfo.getEndByte() - dumpInfo.startByte + 1));
                }
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
    }

    private void parseActionsButtonActionPerformed(ActionEvent evt) {
        TreePath[] paths = getSelectionPaths();
        DumpInfo dumpInfo = (DumpInfo) paths[0].getLastPathComponent();
        SWF swf = DumpInfoSwfNode.getSwfNode(dumpInfo).getSwf();
        byte[] data = swf.originalUncompressedData;
        int prevLength = (int) dumpInfo.startByte;
        try {
            SWFInputStream rri = new SWFInputStream(swf, data);
            if (prevLength != 0) {
                rri.seek(prevLength);
            }
            List<Action> actions = ActionListReader.getOriginalActions(rri, prevLength, (int) dumpInfo.getEndByte());
            for (Action action : actions) {
                DumpInfo di = new DumpInfo(action.toString(), "Action", null, action.getAddress(), action.getTotalActionLength());
                di.parent = dumpInfo;
                rri.dumpInfo = di;
                rri.seek(action.getAddress());
                rri.readAction();
                dumpInfo.getChildInfos().add(di);
            }
            repaint();
        } catch (IOException | InterruptedException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    private void parseAbcButtonActionPerformed(ActionEvent evt) {
        TreePath[] paths = getSelectionPaths();
        DumpInfo dumpInfo = (DumpInfo) paths[0].getLastPathComponent();
        SWF swf = DumpInfoSwfNode.getSwfNode(dumpInfo).getSwf();
        byte[] data = swf.originalUncompressedData;
        int prevLength = (int) dumpInfo.startByte;
        try {
            ABCInputStream ais = new ABCInputStream(new MemoryInputStream(data, 0, prevLength + (int) dumpInfo.lengthBytes));
            ais.seek(prevLength);
            ais.dumpInfo = dumpInfo;
            new ABC(ais, swf, null);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        repaint();
    }

    private void parseInstructionsButtonActionPerformed(ActionEvent evt) {
        TreePath[] paths = getSelectionPaths();
        DumpInfo dumpInfo = (DumpInfo) paths[0].getLastPathComponent();
        SWF swf = DumpInfoSwfNode.getSwfNode(dumpInfo).getSwf();
        byte[] data = swf.originalUncompressedData;
        int prevLength = (int) dumpInfo.startByte;
        try {
            ABCInputStream ais = new ABCInputStream(new MemoryInputStream(data, 0, prevLength + (int) dumpInfo.lengthBytes));
            ais.seek(prevLength);
            ais.dumpInfo = dumpInfo;
            new AVM2Code(ais, null /*FIXME! Pass correct body!*/);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        repaint();
    }

    private void gotoTagButtonActionPerformed(ActionEvent evt) {
        TreePath[] paths = getSelectionPaths();
        DumpInfoSpecial dumpInfo = (DumpInfoSpecial) paths[0].getLastPathComponent();

        SWF swf = DumpInfoSwfNode.getSwfNode(dumpInfo).getSwf();
        long address = (long) (Long) dumpInfo.specialValue;
        Tag foundTag = null;
        for (Tag tag : swf.getTags()) {
            if (tag.getOriginalRange().getPos() == address) {
                foundTag = tag;
                break;
            }
        }

        if (foundTag != null) {
            mainPanel.getMainFrame().getMenu().showResourcesView();
            mainPanel.setTagTreeSelectedNode(foundTag);
        }
    }

    private void gotoActionListButtonActionPerformed(ActionEvent evt) {
        TreePath[] paths = getSelectionPaths();
        DumpInfoSpecial dumpInfo = (DumpInfoSpecial) paths[0].getLastPathComponent();

        SWF swf = DumpInfoSwfNode.getSwfNode(dumpInfo).getSwf();
        long address = (long) (Long) dumpInfo.specialValue;
        mainPanel.getMainFrame().getMenu().showResourcesView();
        //mainPanel.setTagTreeSelectedNode(asm);
    }

    private void gotoMethodButtonActionPerformed(ActionEvent evt) {
        TreePath[] paths = getSelectionPaths();
        DumpInfoSpecial dumpInfo = (DumpInfoSpecial) paths[0].getLastPathComponent();
        if (dumpInfo.specialType == DumpInfoSpecialType.ABC_CODE) {
            dumpInfo = (DumpInfoSpecial) dumpInfo.parent; // method_body
        }

        SWF swf = DumpInfoSwfNode.getSwfNode(dumpInfo).getSwf();
        int method_info = (int) dumpInfo.specialValue;
        // todo
    }

    private void closeSwfButtonActionPerformed(ActionEvent evt) {
        Main.closeFile(mainPanel.getCurrentSwfList());
    }

    @Override
    public DumpTreeModel getModel() {
        return (DumpTreeModel) super.getModel();
    }

    public void expandRoot() {
        DumpTreeModel dtm = getModel();
        DumpInfo root = dtm.getRoot();
        expandPath(new TreePath(new Object[]{root}));
    }

    public void expandFirstLevelNodes() {
        DumpTreeModel dtm = getModel();
        DumpInfo root = dtm.getRoot();
        int childCount = dtm.getChildCount(root);
        expandPath(new TreePath(new Object[]{root}));
        for (int i = 0; i < childCount; i++) {
            expandPath(new TreePath(new Object[]{root, dtm.getChild(root, i)}));
        }
    }
}
