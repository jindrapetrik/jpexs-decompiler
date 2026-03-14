package com.jpexs.decompiler.graph;

import java.util.List;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public class IgnoreEdgesPrevNextWalker implements PrevNextWalker {

    private final Set<GraphPartEdge> backEdges;

    private final PrevNextWalker prevNextWalker;

    public IgnoreEdgesPrevNextWalker(Set<GraphPartEdge> ignoredEdges, PrevNextWalker prevNextWalker) {
        this.backEdges = ignoredEdges;
        this.prevNextWalker = prevNextWalker;        
    }
    
    @Override
    public List<? extends GraphPart> getPrev(GraphPart node) {
        List<? extends GraphPart> ret = prevNextWalker.getPrev(node);
        for (int i = ret.size() - 1; i >= 0; i--) {
            GraphPartEdge edge = new GraphPartEdge(ret.get(i), node);
            if (backEdges.contains(edge)) {
                ret.remove(i);
            }
        }
        return ret;
    }

    @Override
    public List<? extends GraphPart> getNext(GraphPart node) {
        List<? extends GraphPart> ret = prevNextWalker.getNext(node);
        for (int i = ret.size() - 1; i >= 0; i--) {
            GraphPartEdge edge = new GraphPartEdge(node, ret.get(i));
            if (backEdges.contains(edge)) {
                ret.remove(i);
            }
        }
        return ret;
    }
    
}
