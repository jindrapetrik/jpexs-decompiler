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
package com.jpexs.decompiler.flash.gui.taglistview;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.gui.MainPanel;
import com.jpexs.decompiler.flash.gui.tagtree.AbstractTagTree;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.timeline.Frame;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.treeitems.Openable;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;

/**
 * @author JPEXS
 */
public class TagListTree extends AbstractTagTree {

    public TagListTree(TagListTreeModel model, MainPanel mainPanel) {
        super(model, mainPanel);
        setCellRenderer(new TagListTreeCellRenderer());
        setDragEnabled(true);        
        setDropMode(DropMode.ON_OR_INSERT);
        setTransferHandler(new TreeTransferHandler(mainPanel));
    }

    @Override
    public List<TreeItem> getSelection(Openable openable) {
        return getSelection(openable, getAllSelected());
    }

    @Override
    public TagListTreeModel getFullModel() {
        return (TagListTreeModel) super.getFullModel();
    }

    class TreeTransferHandler extends TransferHandler {

        DataFlavor nodesFlavor;
        DataFlavor[] flavors = new DataFlavor[1];
        JTree.DropLocation dropLocation = null;
        MainPanel mainPanel;

        public TreeTransferHandler(MainPanel mainPanel) {
            this.mainPanel = mainPanel;
            try {
                String mimeType = DataFlavor.javaJVMLocalObjectMimeType
                        + ";class=\""
                        + Tag[].class.getName()
                        + "\"";
                nodesFlavor = new DataFlavor(mimeType);
                flavors[0] = nodesFlavor;
            } catch (ClassNotFoundException e) {
                System.err.println("ClassNotFound: " + e.getMessage());
            }
        }

        @Override
        public boolean canImport(TransferHandler.TransferSupport support) {
            if (!support.isDrop()) {
                return false;
            }
            support.setShowDropLocation(true);
            if (!support.isDataFlavorSupported(nodesFlavor)) {
                return false;
            }
            // Do not allow a drop on the drag source selections.
            JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();

            if (((dl.getPath().getLastPathComponent() instanceof Tag)
                    && !(dl.getPath().getLastPathComponent() instanceof DefineSpriteTag))
                    && dl.getChildIndex() == -1) {
                return false;
            }

            //no insert before SWF header
            if ((dl.getPath().getLastPathComponent() instanceof SWF) && dl.getChildIndex() == 0) {
                return false;
            }

            /*if (dl.getPath().getLastPathComponent() instanceof TagListTreeRoot) {
            return false;
        }*/
            AbstractTagTree tree = (AbstractTagTree) support.getComponent();

            List<TreeItem> selected = tree.getSelected();
            TreePath destPath = dl.getPath();
            List<TreeItem> parents = new ArrayList<>();
            for (int i = 0; i < destPath.getPathCount(); i++) {
                parents.add((TreeItem) destPath.getPathComponent(i));
            }
            for (TreeItem item : selected) {
                if (parents.contains(item)) {
                    return false;
                }
            }

            int dropRow = tree.getRowForPath(dl.getPath());
            int[] selRows = tree.getSelectionRows();

            for (int i = 0; i < selRows.length; i++) {
                if (selRows[i] == dropRow) {
                    return false;
                }
            }
            return true;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            AbstractTagTree tree = (AbstractTagTree) c;
            dropLocation = null;

            if (!Configuration.allowDragAndDropInTagListTree.get()) {
                return null;
            }
            TreePath[] paths = tree.getSelectionPaths();
            if (paths == null) {
                return null;
            }
            List<Tag> tags = new ArrayList<>();
            for (TreePath path : paths) {
                if (path.getLastPathComponent() instanceof Tag) {
                    tags.add((Tag) path.getLastPathComponent());
                } else {
                    return null;
                }
            }
            Tag[] tagArr = tags.toArray(new Tag[tags.size()]);
            return new TagsTransferable(tagArr);
        }

        @Override
        public int getSourceActions(JComponent c) {
            return COPY_OR_MOVE;
        }

        @Override
        protected void exportDone(JComponent source, Transferable data, int action) {
            AbstractTagTree tree = (AbstractTagTree) source;
            if (dropLocation == null) {
                return;
            }
            int childIndex = dropLocation.getChildIndex();
            TreeItem dest = (TreeItem) dropLocation.getPath().getLastPathComponent();
            Set<TreeItem> sourceItems = new LinkedHashSet<>(tree.getSelected());
            Timelined timelined;
            Tag position;
            if (childIndex == -1) {
                if (dest instanceof DefineSpriteTag) {
                    timelined = (Timelined) dest;
                    position = null;
                } else if (dest instanceof Tag) {
                    timelined = ((Tag) dest).getTimelined();
                    position = (Tag) dest;
                } else if (dest instanceof Frame) {
                    Frame frame = (Frame) dest;
                    position = frame.allInnerTags.get(frame.allInnerTags.size() - 1);
                    timelined = frame.timeline.timelined;
                } else {
                    timelined = (Timelined) dest;
                    position = null;
                }
            } else {
                if (dest instanceof Frame) {
                    Frame frame = (Frame) dest;
                    timelined = frame.timeline.timelined;
                    position = childIndex == frame.allInnerTags.size() ? null : frame.allInnerTags.get(childIndex);
                } else if (dest instanceof SWF) {
                    SWF swf = (SWF) dest;
                    timelined = swf;
                    int frameIndex = childIndex - 1/*header*/ - 1;
                    if (frameIndex == -1) {
                        frameIndex = 0;
                    }
                    Frame frame = swf.getTimeline().getFrame(frameIndex);
                    position = frame.allInnerTags.get(0);
                } else if (dest instanceof DefineSpriteTag) {
                    DefineSpriteTag sprite = (DefineSpriteTag) dest;
                    timelined = sprite;
                    int frameIndex = childIndex - 1;
                    if (frameIndex == -1) {
                        frameIndex = 0;
                    }
                    Frame frame = sprite.getTimeline().getFrame(frameIndex);
                    position = frame.allInnerTags.get(0);
                } else if (dest instanceof Tag) {
                    timelined = ((Tag) dest).getTimelined();
                    position = (Tag) dest;
                } else {
                    int childCount = tree.getFullModel().getChildCount(dest);
                    TreeItem child;
                    if (childIndex >= childCount) {
                        child = tree.getFullModel().getChild(dest, childCount - 1);
                    } else {
                        child = tree.getFullModel().getChild(dest, childIndex);
                    }
                    if (child instanceof SWF) {
                        SWF swf = (SWF) child;
                        timelined = swf;
                        position = null;
                    } else {
                        return;
                    }
                }
            }
            mainPanel.getContextPopupMenu().copyOrMoveTags(sourceItems, (action & MOVE) == MOVE, timelined, position);
        }

        @Override
        public boolean importData(TransferHandler.TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }
            Transferable t = support.getTransferable();
            Tag[] tags = null;
            try {
                tags = (Tag[]) t.getTransferData(nodesFlavor);
            } catch (UnsupportedFlavorException | IOException ex) {
                Logger.getLogger(TreeTransferHandler.class.getName()).log(Level.SEVERE, null, ex);
            }

            dropLocation = (JTree.DropLocation) support.getDropLocation();
            return true;
        }

        public class TagsTransferable implements Transferable {

            Tag[] nodes;

            public TagsTransferable(Tag[] nodes) {
                this.nodes = nodes;
            }

            @Override
            public Object getTransferData(DataFlavor flavor)
                    throws UnsupportedFlavorException {
                if (!isDataFlavorSupported(flavor)) {
                    throw new UnsupportedFlavorException(flavor);
                }
                return nodes;
            }

            @Override
            public DataFlavor[] getTransferDataFlavors() {
                return flavors;
            }

            @Override
            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return nodesFlavor.equals(flavor);
            }
        }
    }
}
