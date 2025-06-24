/*
 *  Copyright (C) 2024-2025 JPEXS
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
package com.jpexs.decompiler.flash.easygui;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.DefineVideoStreamTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ButtonTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.MorphShapeTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.tags.base.SoundTag;
import com.jpexs.decompiler.flash.tags.base.TextTag;
import com.jpexs.decompiler.flash.timeline.Timelined;
import de.javagl.treetable.JTreeTable;
import de.javagl.treetable.TreeTableModel;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelListener;
import javax.swing.plaf.basic.BasicTableUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

/**
 *
 * @author JPEXS
 */
public class LibraryTreeTable extends JTreeTable {

    private final EasySwfPanel easySwfPanel;
    private SWF swf;

    public LibraryTreeTable(EasySwfPanel easySwfPanel) {
        super(new LibraryTreeTableModel(null));
        getTree().setCellRenderer(new LibraryTreeCellRenderer());
        getTree().setRootVisible(false);
        getTree().setShowsRootHandles(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int selectedRow = getSelectedRow();
                JTree tree = getTree();

                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    TreePath path = tree.getPathForRow(selectedRow);
                    if (path != null && tree.isExpanded(path)) {
                        tree.collapsePath(path);

                        int parentRow = tree.getRowForPath(path);
                        changeSelection(parentRow, 0, false, false);
                    } else if (path != null) {
                        TreePath parentPath = path.getParentPath();
                        if (parentPath != null) {
                            int parentRow = tree.getRowForPath(parentPath);
                            changeSelection(parentRow, 0, false, false);
                        }
                    }
                    e.consume();
                }

                if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    TreePath path = tree.getPathForRow(selectedRow);
                    if (path != null && !tree.isExpanded(path)) {
                        tree.expandPath(path);
                        int parentRow = tree.getRowForPath(path);
                        changeSelection(parentRow, 0, false, false);
                    } else {
                        TreePath childPath = tree.getPathForRow(selectedRow + 1);
                        if (childPath != null) {
                            int childRow = tree.getRowForPath(childPath);
                            changeSelection(childRow, 0, false, false);
                        }
                    }
                    e.consume();
                }
            }
        });
        setTransferHandler(new CharacterTagTransferHandler());
        setDragEnabled(true);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setUI(new BasicTableUI());

        setRowHeight(18);
        getTree().setRowHeight(18);

        if (View.isOceanic()) {
            setBackground(Color.WHITE);
            getTree().setBackground(Color.WHITE);
        }
        this.easySwfPanel = easySwfPanel;

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
                    int selectedRow = getSelectedRow();
                    if (selectedRow == -1) {
                        return;
                    }
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) getValueAt(selectedRow, 0);
                    Object obj = node.getUserObject();
                    if (obj instanceof Timelined) {
                        easySwfPanel.setTimelined((Timelined) obj);
                    }
                }
            }
        });

    }

    public void setSwf(SWF swf) {
        if (swf == this.swf) {
            return;
        }
        this.swf = swf;
        setTreeTableModel(new LibraryTreeTableModel(swf));
    }

    private static class LibraryTreeCellRenderer extends DefaultTreeCellRenderer {

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            /*if (!leaf) {
                if (expanded) {
                    label.setIcon(View.getIcon("folderopen16"));
                } else {
                    label.setIcon(View.getIcon("folder16"));
                }
            }*/
            if (value instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                Object object = node.getUserObject();
                if (object instanceof LibraryFolder) {
                    String folderName = ((LibraryFolder) object).getName();
                    String prefix = "folder";
                    switch (folderName) {
                        case "images":
                            label.setIcon(View.getIcon(prefix + "images16"));
                            break;
                        case "graphics":
                            label.setIcon(View.getIcon(prefix + "shapes16"));
                            break;
                        case "shapeTweens":
                            label.setIcon(View.getIcon(prefix + "morphshapes16"));
                            break;
                        case "texts":
                            label.setIcon(View.getIcon(prefix + "texts16"));
                            break;
                        case "fonts":
                            label.setIcon(View.getIcon(prefix + "fonts16"));
                            break;
                        case "movieClips":
                            label.setIcon(View.getIcon(prefix + "sprites16"));
                            break;
                        case "buttons":
                            label.setIcon(View.getIcon(prefix + "buttons16"));
                            break;
                        case "sounds":
                            label.setIcon(View.getIcon(prefix + "sounds16"));
                            break;
                        case "videos":
                            label.setIcon(View.getIcon(prefix + "movies16"));
                            break;
                        default:
                            label.setIcon(View.getIcon("folder16"));
                            break;
                    }
                }
                if (object instanceof ImageTag) {
                    ImageTag it = (ImageTag) object;
                    label.setIcon(View.getIcon("image16"));
                }
                if (object instanceof ShapeTag) {
                    ShapeTag st = (ShapeTag) object;
                    label.setIcon(View.getIcon("shape16"));
                }
                if (object instanceof MorphShapeTag) {
                    MorphShapeTag mst = (MorphShapeTag) object;
                    label.setIcon(View.getIcon("morphshape16"));
                }
                if (object instanceof TextTag) {
                    TextTag t = (TextTag) object;
                    label.setIcon(View.getIcon("text16"));
                }
                if (object instanceof FontTag) {
                    FontTag f = (FontTag) object;
                    label.setIcon(View.getIcon("font16"));
                }
                if (object instanceof DefineSpriteTag) {
                    DefineSpriteTag st = (DefineSpriteTag) object;
                    label.setIcon(View.getIcon("sprite16"));
                }
                if (object instanceof ButtonTag) {
                    ButtonTag bt = (ButtonTag) object;
                    label.setIcon(View.getIcon("button16"));
                }
                if (object instanceof SoundTag) {
                    SoundTag st = (SoundTag) object;
                    label.setIcon(View.getIcon("sound16"));
                }
                if (object instanceof DefineVideoStreamTag) {
                    DefineVideoStreamTag vt = (DefineVideoStreamTag) object;
                    label.setIcon(View.getIcon("movie16"));
                }
                if (object instanceof Tag) {
                    EasyTagNameResolver tagNameResolver = new EasyTagNameResolver();
                    label.setText(tagNameResolver.getTagName((Tag) object));
                }
            }
            if (View.isOceanic()) {
                if (selected) {
                    label.setBackground(getBackgroundSelectionColor());
                } else {
                    label.setBackground(Color.white);
                }
                label.setOpaque(true);
            }
            return label;
        }
    }

    private static class LibraryFolder {
        private String name;

        public LibraryFolder(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return EasyStrings.translate("library.folder." + name);
        }                

        public String getName() {
            return name;
        }                
    }
    
    private static class LibraryTreeTableModel implements TreeTableModel {

        private DefaultMutableTreeNode root;

        public LibraryTreeTableModel(SWF swf) {
            DefaultMutableTreeNode root = new DefaultMutableTreeNode("SWF");

            DefaultMutableTreeNode imagesNode = new DefaultMutableTreeNode(new LibraryFolder("images"));
            DefaultMutableTreeNode graphicsNode = new DefaultMutableTreeNode(new LibraryFolder("graphics"));
            DefaultMutableTreeNode shapeTweensNode = new DefaultMutableTreeNode(new LibraryFolder("shapeTweens"));
            DefaultMutableTreeNode textsNode = new DefaultMutableTreeNode(new LibraryFolder("texts"));
            DefaultMutableTreeNode fontsNode = new DefaultMutableTreeNode(new LibraryFolder("fonts"));
            DefaultMutableTreeNode movieClipsNode = new DefaultMutableTreeNode(new LibraryFolder("movieClips"));
            DefaultMutableTreeNode buttonsNode = new DefaultMutableTreeNode(new LibraryFolder("buttons"));
            DefaultMutableTreeNode soundsNode = new DefaultMutableTreeNode(new LibraryFolder("sounds"));
            DefaultMutableTreeNode videosNode = new DefaultMutableTreeNode(new LibraryFolder("videos"));

            this.root = root;

            if (swf == null) {
                return;
            }
            for (Tag t : swf.getTags()) {
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(t);
                if (t instanceof ImageTag) {
                    imagesNode.add(node);
                }
                if (t instanceof ShapeTag) {
                    graphicsNode.add(node);
                }
                if (t instanceof MorphShapeTag) {
                    shapeTweensNode.add(node);
                }
                if (t instanceof TextTag) {
                    textsNode.add(node);
                }
                if (t instanceof FontTag) {
                    fontsNode.add(node);
                }
                if (t instanceof DefineSpriteTag) {
                    movieClipsNode.add(node);
                }
                if (t instanceof ButtonTag) {
                    buttonsNode.add(node);
                }
                if (t instanceof SoundTag) {
                    soundsNode.add(node);
                }
                if (t instanceof DefineVideoStreamTag) {
                    videosNode.add(node);
                }
            }

            if (!imagesNode.isLeaf()) {
                root.add(imagesNode);
            }
            if (!graphicsNode.isLeaf()) {
                root.add(graphicsNode);
            }
            if (!shapeTweensNode.isLeaf()) {
                root.add(shapeTweensNode);
            }
            if (!textsNode.isLeaf()) {
                root.add(textsNode);
            }
            if (!fontsNode.isLeaf()) {
                root.add(fontsNode);
            }
            if (!movieClipsNode.isLeaf()) {
                root.add(movieClipsNode);
            }
            if (!buttonsNode.isLeaf()) {
                root.add(buttonsNode);
            }
            if (!soundsNode.isLeaf()) {
                root.add(soundsNode);
            }
            if (!videosNode.isLeaf()) {
                root.add(videosNode);
            }
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return EasyStrings.translate("library.header.name");
                case 1:
                    return EasyStrings.translate("library.header.asLinkage");
                default:
                    return null;
            }
        }

        @Override
        public Class<?> getColumnClass(int column) {
            switch (column) {
                case 0:
                    return TreeTableModel.class;
                default:
                    return String.class;
            }

        }

        @Override
        public Object getValueAt(Object node, int column) {
            DefaultMutableTreeNode n = (DefaultMutableTreeNode) node;
            Object o = n.getUserObject();
            switch (column) {
                case 0:
                    return node;
                case 1:
                    if (o instanceof CharacterTag) {
                        CharacterTag ct = (CharacterTag) o;
                        if (!ct.getClassNames().isEmpty()) {
                            return String.join(", ", ct.getClassNames());
                        }
                        String en = ct.getExportName();
                        if (en != null) {
                            return en;
                        }
                    }
                    return "";
                default:
                    return null;
            }
        }

        @Override
        public boolean isCellEditable(Object node, int column) {
            if (column == 0) {
                return true;
            }
            return false;
        }

        @Override
        public void setValueAt(Object value, Object node, int column) {

        }

        @Override
        public Object getRoot() {
            return root;
        }

        @Override
        public Object getChild(Object parent, int index) {
            return ((DefaultMutableTreeNode) parent).getChildAt(index);
        }

        @Override
        public int getChildCount(Object parent) {
            return ((DefaultMutableTreeNode) parent).getChildCount();
        }

        @Override
        public boolean isLeaf(Object node) {
            return ((DefaultMutableTreeNode) node).isLeaf();
        }

        @Override
        public void valueForPathChanged(TreePath path, Object newValue) {

        }

        @Override
        public int getIndexOfChild(Object parent, Object child) {
            return ((DefaultMutableTreeNode) parent).getIndex((DefaultMutableTreeNode) child);
        }

        @Override
        public void addTreeModelListener(TreeModelListener l) {
        }

        @Override
        public void removeTreeModelListener(TreeModelListener l) {
        }

    }
}
