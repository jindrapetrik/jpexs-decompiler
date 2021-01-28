package com.jpexs.decompiler.graph.precontinues;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class JoinedNode extends Node {
    public List<Node> nodes = new ArrayList<>();

    @Override
    public String toString() {
        return "join" + super.toString();
    }

}
