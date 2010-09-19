/*
 *  Copyright (C) 2010 JPEXS
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.jpexs.asdec.abc.gui;

import com.jpexs.asdec.abc.avm2.flowgraph.Graph;
import com.jpexs.asdec.abc.avm2.flowgraph.GraphDecision;
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
public class GraphFrame extends JFrame {
    public JTree graphTree;


    private class DecisionItem{
        public GraphDecision decision;
        public boolean isTrue;

        public DecisionItem(GraphDecision decision, boolean isTrue) {
            this.decision = decision;
            this.isTrue = isTrue;
        }

        @Override
        public String toString() {
            if(isTrue){
                return "onTrue";
            }else{
                return "onFalse";
            }
        }


    }

    public GraphFrame(final Graph graph){
        setSize(400,400);
        graphTree=new JTree(new TreeModel(){
            private String root="root";
            public Object getRoot() {
                return root;
            }

            public Object getChild(Object parent, int index) {
                if(parent==root){
                    return graph.parts.get(index);
                }else{
                    if(parent instanceof GraphDecision){
                        if(index==0) return new DecisionItem((GraphDecision)parent,true);
                        return new DecisionItem((GraphDecision)parent,false);
                    }
                    if(parent instanceof DecisionItem){
                       DecisionItem di=(DecisionItem)parent;
                       if(di.isTrue) return di.decision.onTrue.get(index);
                       return di.decision.onFalse.get(index);
                    }
                }
                return null;
            }

            public int getChildCount(Object parent) {
                if(parent==root){
                    return graph.parts.size();
                }
                if(parent instanceof GraphDecision){
                    return 2;
                }
                if(parent instanceof DecisionItem){
                       DecisionItem di=(DecisionItem)parent;
                       if(di.isTrue) return di.decision.onTrue.size();
                       return di.decision.onFalse.size();
                    }
                return 0;
            }

            public boolean isLeaf(Object node) {
                return getChildCount(node)==0;
            }

            public void valueForPathChanged(TreePath path, Object newValue) {

            }

            public int getIndexOfChild(Object parent, Object child) {
                if(parent==root){
                    return graph.parts.indexOf(child);
                }else{
                    if(parent instanceof GraphDecision){
                        if(child instanceof DecisionItem){
                            DecisionItem di=(DecisionItem)child;
                            if(di.isTrue) return 0;
                            return 1;
                        }
                    }
                    if(parent instanceof DecisionItem){
                       DecisionItem di=(DecisionItem)parent;
                       if(di.isTrue) return di.decision.onTrue.indexOf(child);
                       return di.decision.onFalse.indexOf(child);
                    }
                }
                return -1;
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
