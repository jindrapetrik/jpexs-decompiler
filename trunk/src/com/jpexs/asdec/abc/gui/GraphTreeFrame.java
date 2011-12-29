/*
 *  Copyright (C) 2010-2011 JPEXS
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

package com.jpexs.asdec.abc.gui;

import com.jpexs.asdec.abc.avm2.flowgraph.Graph;
import com.jpexs.asdec.abc.avm2.flowgraph.GraphPart;
import java.awt.BorderLayout;
import java.awt.Container;
import javax.swing.JFrame;
import javax.swing.JTree;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author JPEXS
 */
public class GraphTreeFrame extends JFrame {
    public JTree graphTree;



    public GraphTreeFrame(final Graph graph){
        setSize(400,400);
        graphTree=new JTree(new TreeModel(){
            public Object getRoot() {
                return graph.head;
            }

            public Object getChild(Object parent, int index) {                
                return ((GraphPart)parent).nextParts.get(index);
            }

            public int getChildCount(Object parent) {
                return ((GraphPart)parent).nextParts.size();
            }

            public boolean isLeaf(Object node) {
                return getChildCount(node)==0;
            }

            public void valueForPathChanged(TreePath path, Object newValue) {

            }

            public int getIndexOfChild(Object parent, Object child) {
                return ((GraphPart)parent).nextParts.indexOf(child);
            }

            public void addTreeModelListener(TreeModelListener l) {

            }

            public void removeTreeModelListener(TreeModelListener l) {

            }
        });

        Container cnt=getContentPane();
        cnt.setLayout(new BorderLayout());
        cnt.add(graphTree,BorderLayout.CENTER);
    }
}
