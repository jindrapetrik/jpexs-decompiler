/*
 * Copyright (C) 2024 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.easygui;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.DefineVideoStreamTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.MorphShapeTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.tags.base.SoundTag;
import de.javagl.treetable.JTreeTable;
import de.javagl.treetable.TreeTableModel;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

/**
 *
 * @author JPEXS
 */
public class LibraryTreeTable extends JTreeTable {
    
    private SWF swf;
    private LibraryTreeTableModel model;
    
    public LibraryTreeTable() {
        super(new LibraryTreeTableModel(null));        
        getTree().setCellRenderer(new LibraryTreeCellRenderer());
        getTree().setRootVisible(false);
        getTree().setShowsRootHandles(true);
    }
    
    public void setSwf(SWF swf) {
        this.swf = swf;
        setTreeTableModel(new LibraryTreeTableModel(swf));
    }
        
    
    private static class LibraryTreeCellRenderer extends DefaultTreeCellRenderer {

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            JLabel label =  (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            if (!leaf) {
                if (expanded) {
                    label.setIcon(View.getIcon("folderopen16"));
                } else {
                    label.setIcon(View.getIcon("folder16"));
                }
            }
            if (value instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                Object object = node.getUserObject();
                if (object instanceof ImageTag) {
                    ImageTag it = (ImageTag) object;
                    label.setIcon(View.getIcon("image16"));                    
                    label.setText("image " + it.getCharacterId() + it.getImageFormat().getExtension());
                }
                if (object instanceof ShapeTag) {
                    ShapeTag st = (ShapeTag) object;
                    label.setIcon(View.getIcon("shape16"));
                    label.setText("graphic " + st.getCharacterId());
                }
                if (object instanceof MorphShapeTag) {
                    MorphShapeTag mst = (MorphShapeTag) object;
                    label.setIcon(View.getIcon("morphshape16"));
                    label.setText("shapeTween " + mst.getCharacterId());
                }
                if (object instanceof DefineSpriteTag) {
                    DefineSpriteTag st = (DefineSpriteTag) object;
                    label.setIcon(View.getIcon("sprite16"));
                    label.setText("movieClip " + st.getCharacterId());
                }
                if (object instanceof SoundTag) {
                    SoundTag st = (SoundTag) object;
                    label.setIcon(View.getIcon("sound16"));
                    label.setText("sound" + (st.getCharacterId() == -1 ? "" : " " + st.getCharacterId()));
                }
                if (object instanceof DefineVideoStreamTag) {
                    DefineVideoStreamTag vt = (DefineVideoStreamTag) object;
                    label.setIcon(View.getIcon("movie16"));
                    label.setText("video " + vt.getCharacterId());
                }
            }
            return label;
        }                
    }
    
    private static class LibraryTreeTableModel implements TreeTableModel {

        private DefaultMutableTreeNode root;

        public LibraryTreeTableModel(SWF swf) {
            DefaultMutableTreeNode root = new DefaultMutableTreeNode("SWF");
        
            DefaultMutableTreeNode imagesNode = new DefaultMutableTreeNode("images");
            DefaultMutableTreeNode graphicsNode = new DefaultMutableTreeNode("graphics");
            DefaultMutableTreeNode shapeTweensNode = new DefaultMutableTreeNode("shapeTweens");
            DefaultMutableTreeNode movieClipsNode = new DefaultMutableTreeNode("movieClips");
            DefaultMutableTreeNode soundsNode = new DefaultMutableTreeNode("sounds");
            DefaultMutableTreeNode videosNode = new DefaultMutableTreeNode("videos");


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
                if (t instanceof DefineSpriteTag) {
                    movieClipsNode.add(node);
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
            if (!movieClipsNode.isLeaf()) {
                root.add(movieClipsNode);
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
                    return "Name";
                case 1:
                    return "AS Linkage";
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
            switch (column) {
                case 0:
                    return node.toString();
                case 1:
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
            return ((DefaultMutableTreeNode) parent).getIndex((DefaultMutableTreeNode)child);
        }

        @Override
        public void addTreeModelListener(TreeModelListener l) {
        }

        @Override
        public void removeTreeModelListener(TreeModelListener l) {
        }
    
    }
}
