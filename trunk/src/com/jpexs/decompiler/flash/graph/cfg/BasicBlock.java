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
 * Adapted from Boomerang, basicblock.cpp 
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
public class BasicBlock {

    public int m_DFTfirst;		   // depth-first traversal first visit
    public int m_DFTlast;		   // depth-first traversal last visit
    public int m_DFTrevfirst;	   // reverse depth-first traversal first visit
    public int m_DFTrevlast;	   // reverse depth-first traversal last visit

    /* high level structuring */
    public SBBType m_structType;	// structured type of this node
    public SBBType m_loopCondType; // type of conditional to treat this loop header as (if any)
    public BasicBlock m_loopHead;		// head of the most nested enclosing loop
    public BasicBlock m_caseHead;		// head of the most nested enclosing case
    public BasicBlock m_condFollow;	// follow of a conditional header
    public BasicBlock m_loopFollow;	// follow of a loop header
    public BasicBlock m_latchNode;	// latch node of a loop header  
    //protected:
  /* general basic block information */
    public BBType m_nodeType;		// type of basic block
    //std::list<RTL*>* m_pRtls;	// Ptr to list of RTLs
    public int m_iLabelNum;	// Nonzero if start of BB needs label
    public String m_labelStr;		// string label of this bb.
    public boolean m_labelneeded;
    public boolean m_bIncomplete;	// True if not yet complete
    public boolean m_bJumpReqd;	// True if jump required for "fall through"

    /* in-edges and out-edges */
    public List<BasicBlock> m_InEdges = new ArrayList<>();	// Vector of in-edges
    public List<BasicBlock> m_OutEdges = new ArrayList<>();// Vector of out-edges
    public int m_iNumInEdges;	// We need these two because GCC doesn't
    public int m_iNumOutEdges;	// support resize() of vectors!

    /* for traversal */
    public boolean m_iTraversed;	// traversal marker

    /* Liveness */
    //LocationSet	liveIn;			// Set of locations live at BB start
    //protected:
  /* Control flow analysis stuff, lifted from Doug Simon's honours thesis.
     */
    public int ord;	 // node's position within the ordering structure
    public int revOrd;	 // position within ordering structure for the reverse graph
    public int inEdgesVisited; // counts the number of in edges visited during a DFS
    public int numForwardInEdges; // inedges to this node that aren't back edges
    public int loopStamps[] = new int[2];
    public int revLoopStamps[] = new int[2]; // used for structuring analysis
    public TravType traversed; // traversal flag for the numerous DFS's
    public boolean hllLabel; // emit a label for this node when generating HL code?
    public String labelStr; // the high level label for this node (if needed)
    public int indentLevel; // the indentation level of this node in the final code
    // analysis information
    public BasicBlock immPDom; // immediate post dominator
    public BasicBlock loopHead; // head of the most nested enclosing loop
    public BasicBlock condFollow; // follow of a conditional header
    public BasicBlock loopFollow; // follow of a loop header
    public BasicBlock latchNode; // latching node of a loop header
    // Structured type of the node
    public StructType sType; // the structuring class (Loop, Cond , etc)
    public UnStructType usType; // the restructured type of a conditional header
    public LoopType lType; // the loop type of a loop header
    public CondType cType; // the conditional type of a conditional header 
    // true if processing for overlapped registers on statements in this BB
    // has been completed.
    public int startAddress;
    public int endAddress;
    public static final int BTHEN = 0;
    public static final int BELSE = 1;

    public BasicBlock() {
        m_DFTfirst = 0;
        m_DFTlast = 0;
        m_structType = SBBType.NONE;
        m_loopCondType = SBBType.NONE;
        m_loopHead = null;
        m_caseHead = null;
        m_condFollow = null;
        m_loopFollow = null;
        m_latchNode = null;
        m_nodeType = BBType.INVALID;
        //m_pRtls=null;
        m_iLabelNum = 0;
        m_labelneeded = false;
        m_bIncomplete = true;
        m_bJumpReqd = false;
        m_iNumInEdges = 0;
        m_iNumOutEdges = 0;
        m_iTraversed = false;
        // From Doug's code
        ord = -1;
        revOrd = -1;
        inEdgesVisited = 0;
        numForwardInEdges = -1;
        traversed = TravType.UNTRAVERSED;
        hllLabel = false;
        indentLevel = 0;
        immPDom = null;
        loopHead = null;
        condFollow = null;
        loopFollow = null;
        latchNode = null;
        sType = StructType.Seq;
        usType = UnStructType.Structured;
        // Others
    }

    public BasicBlock(BasicBlock bb) {
        m_DFTfirst = 0;
        m_DFTlast = 0;
        m_structType = bb.m_structType;
        m_loopCondType = bb.m_loopCondType;
        m_loopHead = bb.m_loopHead;
        m_caseHead = bb.m_caseHead;
        m_condFollow = bb.m_condFollow;
        m_loopFollow = bb.m_loopFollow;
        m_latchNode = bb.m_latchNode;
        m_nodeType = bb.m_nodeType;
        //m_pRtls=null;
        m_iLabelNum = bb.m_iLabelNum;
        m_labelneeded = false;
        m_bIncomplete = bb.m_bIncomplete;
        m_bJumpReqd = bb.m_bJumpReqd;
        m_InEdges = bb.m_InEdges;
        m_OutEdges = bb.m_OutEdges;
        m_iNumInEdges = bb.m_iNumInEdges;
        m_iNumOutEdges = bb.m_iNumOutEdges;
        m_iTraversed = false;
// From Doug's code
        ord = bb.ord;
        revOrd = bb.revOrd;
        inEdgesVisited = bb.inEdgesVisited;
        numForwardInEdges = bb.numForwardInEdges;
        traversed = (bb.traversed);
        hllLabel = (bb.hllLabel);
        indentLevel = (bb.indentLevel);
        immPDom = (bb.immPDom);
        loopHead = (bb.loopHead);
        condFollow = (bb.condFollow);
        loopFollow = (bb.loopFollow);
        latchNode = (bb.latchNode);
        sType = (bb.sType);
        usType = (bb.usType);

        //setRTLs(bb.m_pRtls);
    }

    public int getLabel() {
        return m_iLabelNum;
    }

    public boolean isTraversed() {
        return m_iTraversed;
    }

    public void setTraversed(boolean bTraversed) {
        m_iTraversed = bTraversed;
    }

    public BBType getType() {
        return m_nodeType;
    }

    public void updateType(BBType bbType, int iNumOutEdges) {
        m_nodeType = bbType;
        m_iNumOutEdges = iNumOutEdges;
    }

    public void setJumpReqd() {
        m_bJumpReqd = true;
    }

    public boolean isJumpReqd() {
        return m_bJumpReqd;
    }

    public boolean isBackEdge(int inEdge) {
        BasicBlock in = m_InEdges.get(inEdge);
        return this == in || (m_DFTfirst < in.m_DFTfirst && m_DFTlast > in.m_DFTlast);
    }

    public List<BasicBlock> getInEdges() {
        return m_InEdges;
    }

    public List<BasicBlock> getOutEdges() {
        return m_OutEdges;
    }

    public void setInEdge(int i, BasicBlock pNewInEdge) {
        m_InEdges.set(i, pNewInEdge);
    }

    public void setOutEdge(int i, BasicBlock pNewOutEdge) {
        if (m_OutEdges.isEmpty()) {
            assert (i == 0);
            m_OutEdges.add(pNewOutEdge);
        } else {
            assert (i < (int) m_OutEdges.size());
            m_OutEdges.set(i, pNewOutEdge);
        }
    }

    public BasicBlock getOutEdge(int i) {
        if (i < m_OutEdges.size()) {
            return m_OutEdges.get(i);
        } else {
            return null;
        }
    }

    public int getLowAddr() {
        return startAddress;
    }

    public int getHiAddr() {
        return endAddress;
    }

    BasicBlock getCorrectOutEdge(int a) {
        for (BasicBlock b : m_OutEdges) {
            if (b.getLowAddr() == a) {
                return b;
            }
        }
        return null;
    }

    public void addInEdge(BasicBlock pNewInEdge) {
        m_InEdges.add(pNewInEdge);
        m_iNumInEdges++;
    }

    public void addOutEdge(BasicBlock pNewOutEdge) {
        m_OutEdges.add(pNewOutEdge);
        m_iNumOutEdges++;
    }

    public void deleteInEdge(List<BasicBlock> blocks) {
        m_InEdges.removeAll(blocks);
        m_iNumInEdges--;
    }

    public void deleteInEdge(BasicBlock edge) {
        if (m_InEdges.contains(edge)) {
            m_InEdges.remove(edge);
            m_iNumInEdges--;
        }
    }

    public void deleteEdge(BasicBlock edge) {
        edge.deleteInEdge(this);
        if (m_OutEdges.contains(edge)) {
            m_OutEdges.remove(edge);
            m_iNumOutEdges--;
        }
    }

    public int DFTOrder(RefInt first, RefInt last) {
        first.value++;
        m_DFTfirst = first.value;

        int numTraversed = 1;
        m_iTraversed = true;

        for (BasicBlock child : m_OutEdges) {
            if (child.m_iTraversed == false) {
                numTraversed = numTraversed + child.DFTOrder(first, last);
            }
        }

        last.value++;
        m_DFTlast = last.value;

        return numTraversed;
    }

    public int RevDFTOrder(RefInt first, RefInt last) {
        first.value++;
        m_DFTrevfirst = first.value;

        int numTraversed = 1;
        m_iTraversed = true;

        for (BasicBlock parent : m_InEdges) {
            if (parent.m_iTraversed == false) {
                numTraversed = numTraversed + parent.RevDFTOrder(first, last);
            }
        }

        last.value++;
        m_DFTrevlast = last.value;

        return numTraversed;
    }

    public boolean lessAddress(BasicBlock bb1, BasicBlock bb2) {
        return bb1.getLowAddr() < bb2.getLowAddr();
    }

    public boolean lessFirstDFT(BasicBlock bb1, BasicBlock bb2) {
        return bb1.m_DFTfirst < bb2.m_DFTfirst;
    }

    public boolean lessLastDFT(BasicBlock bb1, BasicBlock bb2) {
        return bb1.m_DFTlast < bb2.m_DFTlast;
    }
    //getCallDest()
    //getCallDestProc()

    BasicBlock getLoopBody() {
        assert (m_structType == SBBType.PRETESTLOOP || m_structType == SBBType.POSTTESTLOOP || m_structType == SBBType.ENDLESSLOOP);
        assert (m_iNumOutEdges == 2);
        if (m_OutEdges.get(0) != m_loopFollow) {
            return m_OutEdges.get(0);
        }
        return m_OutEdges.get(1);
    }

    public boolean isAncestorOf(BasicBlock other) {
        return ((loopStamps[0] < other.loopStamps[0]
                && loopStamps[1] > other.loopStamps[1])
                || (revLoopStamps[0] < other.revLoopStamps[0]
                && revLoopStamps[1] > other.revLoopStamps[1]));
    }

    public boolean hasBackEdgeTo(BasicBlock dest) {
//	assert(HasEdgeTo(dest) || dest == this);
        return dest == this || dest.isAncestorOf(this);
    }

    // had its code generated
    public boolean allParentsGenerated() {
        for (int i = 0; i < m_InEdges.size(); i++) {
            if (!m_InEdges.get(i).hasBackEdgeTo(this)
                    && m_InEdges.get(i).traversed != TravType.DFS_CODEGEN) {
                return false;
            }
        }
        return true;
    }
    //emitGotoAndLabel

    public void setLoopStamps(RefInt time, List<BasicBlock> order) {
        // timestamp the current node with the current time and set its traversed
        // flag
        traversed = TravType.DFS_LNUM;
        loopStamps[0] = time.value;

        // recurse on unvisited children and set inedges for all children
        for (int i = 0; i < m_OutEdges.size(); i++) {
            // set the in edge from this child to its parent (the current node)
            // (not done here, might be a problem)
            // outEdges[i].inEdges.Add(this);

            // recurse on this child if it hasn't already been visited
            if (m_OutEdges.get(i).traversed != TravType.DFS_LNUM) {
                time.value++;
                m_OutEdges.get(i).setLoopStamps(time, order);
            }
        }

        // set the the second loopStamp value
        time.value++;
        loopStamps[1] = time.value;

        // add this node to the ordering structure as well as recording its position within the ordering
        ord = order.size();
        order.add(this);
    }

    public void setRevLoopStamps(RefInt time) {
        // timestamp the current node with the current time and set its traversed flag
        traversed = TravType.DFS_RNUM;
        revLoopStamps[0] = time.value;

        // recurse on the unvisited children in reverse order
        for (int i = m_OutEdges.size() - 1; i >= 0; i--) {
            // recurse on this child if it hasn't already been visited
            if (m_OutEdges.get(i).traversed != TravType.DFS_RNUM) {
                time.value++;
                m_OutEdges.get(i).setRevLoopStamps(time);
            }
        }

        // set the the second loopStamp value
        time.value++;
        revLoopStamps[1] = time.value;
    }

    public void setRevOrder(List<BasicBlock> order) {
        // Set this node as having been traversed during the post domimator DFS ordering traversal
        traversed = TravType.DFS_PDOM;

        // recurse on unvisited children
        for (int i = 0; i < m_InEdges.size(); i++) {
            if (m_InEdges.get(i).traversed != TravType.DFS_PDOM) {
                m_InEdges.get(i).setRevOrder(order);
            }
        }

        // add this node to the ordering structure and record the post dom. order of this node as its index within this
        // ordering structure
        revOrd = order.size();
        order.add(this);
    }

    //setCaseHead
    public void setStructType(StructType s) {
        // if this is a conditional header, determine exactly which type of conditional header it is (i.e. switch, if-then,
        // if-then-else etc.)
        if (s == StructType.Cond) {
            if (getType() == BBType.NWAY) {
                cType = CondType.Case;
            } else if (m_OutEdges.get(BELSE) == condFollow) {
                cType = CondType.IfThen;
            } else if (m_OutEdges.get(BTHEN) == condFollow) {
                cType = CondType.IfElse;
            } else {
                cType = CondType.IfThenElse;
            }
        }

        sType = s;
    }

    public void setUnstructType(UnStructType us) {
        assert ((sType == StructType.Cond || sType == StructType.LoopCond) && cType != CondType.Case);
        usType = us;
    }

    public UnStructType getUnstructType() {
        assert ((sType == StructType.Cond || sType == StructType.LoopCond) && cType != CondType.Case);
        return usType;
    }

    public void setLoopType(LoopType l) {
        assert (sType == StructType.Loop || sType == StructType.LoopCond);
        lType = l;

        // set the structured class (back to) just Loop if the loop type is PreTested OR it's PostTested and is a single
        // block loop
        if (lType == LoopType.PreTested || (lType == LoopType.PostTested && this == latchNode)) {
            sType = StructType.Loop;
        }
    }

    public LoopType getLoopType() {
        assert (sType == StructType.Loop || sType == StructType.LoopCond);
        return lType;
    }

    public void setCondType(CondType c) {
        assert (sType == StructType.Cond || sType == StructType.LoopCond);
        cType = c;
    }

    public CondType getCondType() {
        assert (sType == StructType.Cond || sType == StructType.LoopCond);
        return cType;
    }

    public boolean inLoop(BasicBlock header, BasicBlock latch) {
        assert (header.latchNode == latch);
        assert (header == latch
                || ((header.loopStamps[0] > latch.loopStamps[0] && latch.loopStamps[1] > header.loopStamps[1])
                || (header.loopStamps[0] < latch.loopStamps[0] && latch.loopStamps[1] < header.loopStamps[1])));
        // this node is in the loop if it is the latch node OR
        // this node is within the header and the latch is within this when using the forward loop stamps OR
        // this node is within the header and the latch is within this when using the reverse loop stamps
        return this == latch
                || (header.loopStamps[0] < loopStamps[0] && loopStamps[1] < header.loopStamps[1]
                && loopStamps[0] < latch.loopStamps[0] && latch.loopStamps[1] < loopStamps[1])
                || (header.revLoopStamps[0] < revLoopStamps[0] && revLoopStamps[1] < header.revLoopStamps[1]
                && revLoopStamps[0] < latch.revLoopStamps[0] && latch.revLoopStamps[1] < revLoopStamps[1]);
    }

////////////////////////////////////////////////////
// Basically the "whichPred" function as per Briggs, Cooper, et al (and presumably "Cryton, Ferante, Rosen, Wegman, and
// Zadek").  Return -1 if not found
    public int whichPred(BasicBlock pred) {
        int n = m_InEdges.size();
        for (int i = 0; i < n; i++) {
            if (m_InEdges.get(i) == pred) {
                return i;
            }
        }
        assert false;
        return -1;
    }

    public boolean hasBackEdge() {
        for (int i = 0; i < m_OutEdges.size(); i++) {
            if (hasBackEdgeTo(m_OutEdges.get(i))) {
                return true;
            }
        }
        return false;
    }

    public void setCondFollow(BasicBlock other) {
        condFollow = other;
    }

    public BasicBlock getCondFollow() {
        return condFollow;
    }

    public void setLoopHead(BasicBlock head) {
        loopHead = head;
    }

    public BasicBlock getLoopHead() {
        return loopHead;
    }

    public void setLatchNode(BasicBlock latch) {
        latchNode = latch;
    }

    public boolean isLatchNode() {
        return loopHead != null && loopHead.latchNode == this;
    }

    public BasicBlock getLatchNode() {
        return latchNode;
    }

    public StructType getStructType() {
        return sType;
    }

    public void setLoopFollow(BasicBlock other) {
        loopFollow = other;
    }

    public BasicBlock getLoopFollow() {
        return loopFollow;
    }

    public void setType(BBType type) {
        this.m_nodeType = type;
    }

    @Override
    public String toString() {
        return (startAddress + 1) + "-" + (endAddress + 1);
    }
}
