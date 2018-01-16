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
package com.jpexs.decompiler.flash.gui;

import com.jpexs.decompiler.flash.abc.avm2.graph.AVM2Graph;
import com.jpexs.decompiler.graph.GraphPart;
import java.awt.BorderLayout;
import java.awt.Container;
import javax.swing.JTree;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author JPEXS
 */
public class GraphTreeFrame extends AppFrame {

    public JTree graphTree;

    public GraphTreeFrame(final AVM2Graph graph) {
        setSize(400, 400);
        graphTree = new JTree(new TreeModel() {
            @Override
            public Object getRoot() {
                return graph.heads.get(0);
            }

            @Override
            public Object getChild(Object parent, int index) {
                return ((GraphPart) parent).nextParts.get(index);
            }

            @Override
            public int getChildCount(Object parent) {
                return ((GraphPart) parent).nextParts.size();
            }

            @Override
            public boolean isLeaf(Object node) {
                return getChildCount(node) == 0;
            }

            @Override
            public void valueForPathChanged(TreePath path, Object newValue) {
            }

            @Override
            public int getIndexOfChild(Object parent, Object child) {
                return ((GraphPart) parent).nextParts.indexOf(child);
            }

            @Override
            public void addTreeModelListener(TreeModelListener l) {
            }

            @Override
            public void removeTreeModelListener(TreeModelListener l) {
            }
        });

        Container cnt = getContentPane();
        cnt.setLayout(new BorderLayout());
        cnt.add(graphTree, BorderLayout.CENTER);
    }
}
