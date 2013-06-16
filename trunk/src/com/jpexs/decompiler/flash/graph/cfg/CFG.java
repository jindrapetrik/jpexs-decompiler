/*
 * Copyright (C) 2013 JPEXS
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
/**
 * Adapted from Boomerang, cfg.cpp
 * Copyright (C) 1997-2000, The University of Queensland
 * Copyright (C) 2000-2001, Sun Microsystems, Inc
 * Copyright (C) 2002, Trent Waddington 
 */
package com.jpexs.decompiler.flash.graph.cfg;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class CFG {
    /*
     * The list of pointers to BBs.
     */

    public List<BasicBlock> m_listBB;

    /*
     * Ordering of BBs for control flow structuring
     */
    public List<BasicBlock> Ordering = new ArrayList<>();
    public List<BasicBlock> revOrdering = new ArrayList<>();

    /*
     * The ADDRESS to BasicBlock map.
     */
    //MABasicBlock		m_maBasicBlock;

    /*
     * The entry and exit BBs.
     */
    public BasicBlock entryBB;
    public BasicBlock exitBB;

    /*
     * True if well formed.
     */
    public boolean m_bWellFormed, structured;
    /*
     * Last label (positive integer) used by any BB this Cfg
     */
    public int lastLabel;

    public void setTimeStamps() {
        // set DFS tag
        for (BasicBlock it : m_listBB) {
            it.traversed = TravType.DFS_TAG;
        }

        // set the parenthesis for the nodes as well as setting the post-order ordering between the nodes
        RefInt time = new RefInt();
        time.value = 1;
        Ordering.clear();
        entryBB.setLoopStamps(time, Ordering);

        // set the reverse parenthesis for the nodes
        time.value = 1;
        entryBB.setRevLoopStamps(time);

        BasicBlock retNode = findRetNode();
        assert (retNode != null);
        revOrdering.clear();
        retNode.setRevOrder(revOrdering);
    }

    public void getReachableBlocks(BasicBlock b, List<BasicBlock> ret) {
        if (ret.contains(b)) {
            return;
        }
        ret.add(b);
        for (BasicBlock next : b.m_OutEdges) {
            getReachableBlocks(next, ret);
        }
    }

    public BasicBlock getCommonBlock(List<BasicBlock> parts, List<BasicBlock> visited) {
        if (parts.isEmpty()) {
            return null;
        }

        List<List<BasicBlock>> reachable = new ArrayList<>();
        for (BasicBlock p : parts) {
            List<BasicBlock> r1 = new ArrayList<>();
            getReachableBlocks(p, r1);
            r1.add(p);
            reachable.add(r1);
        }
        List<BasicBlock> first = reachable.get(0);
        for (BasicBlock p : first) {
            /*if (ignored.contains(p)) {
             continue;
             }*/
            boolean common = true;
            for (List<BasicBlock> r : reachable) {
                if (!r.contains(p)) {
                    common = false;
                    break;
                }
            }
            if (common) {
                return p;
            }
        }
        return null;
    }

    BasicBlock findRetNode() {
        BasicBlock retNode = null;
        for (BasicBlock it : m_listBB) {
            if (it.getType() == BBType.RET) {
                retNode = it;
                break;
            }
        }
        return retNode;
    }

    public void findImmedPDom() {
        BasicBlock curNode, succNode;	// the current Node and its successor

        // traverse the nodes in order (i.e from the bottom up)
        int i;
        for (i = revOrdering.size() - 1; i >= 0; i--) {
            curNode = revOrdering.get(i);
            List<BasicBlock> oEdges = curNode.getOutEdges();
            for (int j = 0; j < oEdges.size(); j++) {
                succNode = oEdges.get(j);
                if (succNode.revOrd > curNode.revOrd) {
                    curNode.immPDom = commonPDom(curNode.immPDom, succNode);
                }
            }
        }

        // make a second pass but consider the original CFG ordering this time
        int u;
        for (u = 0; u < Ordering.size(); u++) {
            curNode = Ordering.get(u);
            List<BasicBlock> oEdges = curNode.getOutEdges();
            if (oEdges.size() > 1) {
                for (int j = 0; j < oEdges.size(); j++) {
                    succNode = oEdges.get(j);
                    curNode.immPDom = commonPDom(curNode.immPDom, succNode);
                }
            }
        }

        // one final pass to fix up nodes involved in a loop
        for (u = 0; u < Ordering.size(); u++) {
            curNode = Ordering.get(u);
            List<BasicBlock> oEdges = curNode.getOutEdges();
            if (oEdges.size() > 1) {
                for (int j = 0; j < oEdges.size(); j++) {
                    succNode = oEdges.get(j);
                    if (curNode.hasBackEdgeTo(succNode) && (curNode.getOutEdges().size() > 1)
                            && (succNode.immPDom != null)
                            && (succNode.immPDom.ord < curNode.immPDom.ord)) {
                        curNode.immPDom = commonPDom(succNode.immPDom, curNode.immPDom);
                    } else {
                        curNode.immPDom = commonPDom(curNode.immPDom, succNode);
                    }
                }
            }
        }
    }

    // Finds the common post dominator of the current immediate post dominator and its successor's immediate post dominator
    public BasicBlock commonPDom(BasicBlock curImmPDom, BasicBlock succImmPDom) {
        if (curImmPDom == null) {
            return succImmPDom;
        }
        if (succImmPDom == null) {
            return curImmPDom;
        }
        if (curImmPDom.revOrd == succImmPDom.revOrd) {
            return curImmPDom;  // ordering hasn't been done
        }
        BasicBlock oldCurImmPDom = curImmPDom;
        BasicBlock oldSuccImmPDom = succImmPDom;

        int giveup = 0;
        final int GIVEUP = 10000;
        while (giveup < GIVEUP && (curImmPDom != null) && (succImmPDom != null) && (curImmPDom != succImmPDom)) {
            if (curImmPDom.revOrd > succImmPDom.revOrd) {
                succImmPDom = succImmPDom.immPDom;
            } else {
                curImmPDom = curImmPDom.immPDom;
            }
            giveup++;
        }

        if (giveup >= GIVEUP) {
            /*if (VERBOSE)
             LOG << "failed to find commonPDom for " << oldCurImmPDom.getLowAddr() << " and " <<
             oldSuccImmPDom.getLowAddr() << "\n";*/
            return oldCurImmPDom;  // no change
        }

        return curImmPDom;
    }

    // Structures all conditional headers (i.e. nodes with more than one outedge)
    public void structConds() {
        // Process the nodes in order
        for (int i = 0; i < Ordering.size(); i++) {
            BasicBlock curNode = Ordering.get(i);

            // does the current node have more than one out edge?
            if (curNode.getOutEdges().size() > 1) {
                // if the current conditional header is a two way node and has a back edge, then it won't have a follow
                if (curNode.hasBackEdge() && curNode.getType() == BBType.TWOWAY) {
                    curNode.setStructType(StructType.Cond);
                    continue;
                }

                // set the follow of a node to be its immediate post dominator
                curNode.setCondFollow(curNode.immPDom);

                // set the structured type of this node
                curNode.setStructType(StructType.Cond);

                // if this is an nway header, then we have to tag each of the nodes within the body of the nway subgraph
                //if (curNode.getCondType() == CondType.Case)
                //curNode.setCaseHead(curNode,curNode.getCondFollow());
            }
        }
    }

// Pre: The graph for curProc has been built.
// Post: Each node is tagged with the header of the most nested loop of which it is a member (possibly none).
// The header of each loop stores information on the latching node as well as the type of loop it heads.
    public void structLoops() {
        for (int i = Ordering.size() - 1; i >= 0; i--) {
            BasicBlock curNode = Ordering.get(i);	// the current node under investigation
            BasicBlock latch = null;			// the latching node of the loop

            // If the current node has at least one back edge into it, it is a loop header. If there are numerous back edges
            // into the header, determine which one comes form the proper latching node.
            // The proper latching node is defined to have the following properties:
            //	 i) has a back edge to the current node
            //	ii) has the same case head as the current node
            // iii) has the same loop head as the current node
            //	iv) is not an nway node
            //	 v) is not the latch node of an enclosing loop
            //	vi) has a lower ordering than all other suitable candiates
            // If no nodes meet the above criteria, then the current node is not a loop header
            List<BasicBlock> iEdges = curNode.getInEdges();
            for (int j = 0; j < iEdges.size(); j++) {
                BasicBlock pred = iEdges.get(j);
                if (/*pred.getCaseHead() == curNode.getCaseHead() &&  // ii)*//*pred.getLoopHead() == curNode.getLoopHead() &&*/ // iii)
                        ((latch == null) || latch.ord > pred.ord) && // vi)
                        !((pred.getLoopHead() != null)
                        && pred.getLoopHead().getLatchNode() == pred) && // v)
                        pred.hasBackEdgeTo(curNode)) // i)
                {
                    latch = pred;
                }
            }

            // if a latching node was found for the current node then it is a loop header.
            if (latch != null) {
                // define the map that maps each node to whether or not it is within the current loop
                boolean loopNodes[] = new boolean[Ordering.size()];
                for (int j = 0; j < Ordering.size(); j++) {
                    loopNodes[j] = false;
                }

                curNode.setLatchNode(latch);

                // the latching node may already have been structured as a conditional header. If it is not also the loop
                // header (i.e. the loop is over more than one block) then reset it to be a sequential node otherwise it
                // will be correctly set as a loop header only later
                if (latch != curNode && latch.getStructType() == StructType.Cond) {
                    latch.setStructType(StructType.Seq);
                }

                // set the structured type of this node
                curNode.setStructType(StructType.Loop);

                // tag the members of this loop
                tagNodesInLoop(curNode, loopNodes);

                // calculate the type of this loop
                determineLoopType(curNode, loopNodes);

                // calculate the follow node of this loop
                findLoopFollow(curNode, loopNodes);

                // delete the space taken by the loopnodes map
                //delete[] loopNodes;
            }
        }
    }

// Pre: The loop induced by (head,latch) has already had all its member nodes tagged
// Post: The type of loop has been deduced
    public void determineLoopType(BasicBlock header, boolean loopNodes[]) {
        assert (header.getLatchNode() != null);

        // if the latch node is a two way node then this must be a post tested loop
        if (header.getLatchNode().getType() == BBType.TWOWAY) {
            header.setLoopType(LoopType.PostTested);

            // if the head of the loop is a two way node and the loop spans more than one block  then it must also be a
            // conditional header
            if (header.getType() == BBType.TWOWAY && header != header.getLatchNode()) {
                header.setStructType(StructType.LoopCond);
            }
        } // otherwise it is either a pretested or endless loop
        else if (header.getType() == BBType.TWOWAY) {
            // if the header is a two way node then it must have a conditional follow (since it can't have any backedges
            // leading from it). If this follow is within the loop then this must be an endless loop
            if ((header.getCondFollow() != null) && loopNodes[header.getCondFollow().ord]) {
                header.setLoopType(LoopType.Endless);

                // retain the fact that this is also a conditional header
                header.setStructType(StructType.LoopCond);
            } else {
                header.setLoopType(LoopType.PreTested);
            }
        } // both the header and latch node are one way nodes so this must be an endless loop
        else {
            header.setLoopType(LoopType.Endless);
        }
    }

// Pre: The loop headed by header has been induced and all it's member nodes have been tagged
// Post: The follow of the loop has been determined.
    void findLoopFollow(BasicBlock header, boolean loopNodes[]) {
        assert (header.getStructType() == StructType.Loop || header.getStructType() == StructType.LoopCond);
        LoopType lType = header.getLoopType();
        BasicBlock latch = header.getLatchNode();

        if (lType == LoopType.PreTested) {
            // if the 'while' loop's true child is within the loop, then its false child is the loop follow
            if (loopNodes[header.getOutEdges().get(0).ord]) {
                header.setLoopFollow(header.getOutEdges().get(1));
            } else {
                header.setLoopFollow(header.getOutEdges().get(0));
            }
        } else if (lType == LoopType.PostTested) {
            // the follow of a post tested ('repeat') loop is the node on the end of the non-back edge from the latch node
            if (latch.getOutEdges().get(0) == header) {
                header.setLoopFollow(latch.getOutEdges().get(1));
            } else {
                header.setLoopFollow(latch.getOutEdges().get(0));
            }
        } else {
            // endless loop
            BasicBlock follow = null;
            List<BasicBlock> follows = new ArrayList<>();

            // traverse the ordering array between the header and latch nodes.
            //BasicBlock latch = header.getLatchNode();
            for (int i = header.ord - 1; i > latch.ord; i--) {
                BasicBlock desc = Ordering.get(i);
                // the follow for an endless loop will have the following
                // properties:
                //	 i) it will have a parent that is a conditional header inside the loop whose follow is outside the
                //		loop
                //	ii) it will be outside the loop according to its loop stamp pair
                // iii) have the highest ordering of all suitable follows (i.e. highest in the graph)

                if (desc.getStructType() == StructType.Cond && (desc.getCondFollow() != null)
                        && desc.getLoopHead() == header) {
                    if (loopNodes[desc.getCondFollow().ord]) {
                        // if the conditional's follow is in the same loop AND is lower in the loop, jump to this follow
                        if (desc.ord > desc.getCondFollow().ord) {
                            i = desc.getCondFollow().ord;
                        } // otherwise there is a backward jump somewhere to a node earlier in this loop. We don't need to any
                        //  nodes below this one as they will all have a conditional within the loop.
                        else {
                            break;
                        }
                    } else {
                        // otherwise find the child (if any) of the conditional header that isn't inside the same loop
                        BasicBlock succ = desc.getOutEdges().get(0);
                        if (loopNodes[succ.ord]) {
                            if (!loopNodes[desc.getOutEdges().get(1).ord]) {
                                succ = desc.getOutEdges().get(1);
                            } else {
                                succ = null;
                            }
                        }
                        // if a potential follow was found, compare its ordering with the currently found follow
                        if (succ != null) {
                            follows.add(succ);
                        }
                        /*if ((succ != null) && ((follow == null) || succ.ord > follow.ord)) {
                         follow = succ;
                         }*/
                    }
                }
            }
            // if a follow was found, assign it to be the follow of the loop under
            // investigation
            follow = getCommonBlock(follows, new ArrayList<BasicBlock>());
            if (follow != null) {
                header.setLoopFollow(follow);
            }
        }
    }

    public void tagNodesInLoop(BasicBlock header, boolean loopNodes[]) {
        assert (header.getLatchNode() != null);

        // traverse the ordering structure from the header to the latch node tagging the nodes determined to be within the
        // loop. These are nodes that satisfy the following:
        //	i) header.loopStamps encloses curNode.loopStamps and curNode.loopStamps encloses latch.loopStamps
        //	OR
        //	ii) latch.revLoopStamps encloses curNode.revLoopStamps and curNode.revLoopStamps encloses header.revLoopStamps
        //	OR
        //	iii) curNode is the latch node

        BasicBlock latch = header.getLatchNode();
        for (int i = header.ord - 1; i >= latch.ord; i--) {
            if (Ordering.get(i).inLoop(header, latch)) {
                // update the membership map to reflect that this node is within the loop
                loopNodes[i] = true;

                Ordering.get(i).setLoopHead(header);
            }
        }
    }

    public void structure() {
        setTimeStamps();
        findImmedPDom();
        structConds();
        structLoops();
    }

    public CFG(List<BasicBlock> blocks, BasicBlock entry) {
        this.m_listBB = blocks;
        this.entryBB = entry;
    }
}
