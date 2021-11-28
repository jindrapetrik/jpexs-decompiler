package com.jpexs.decompiler.graph;

import java.util.List;

/**
 *
 * @author JPEXS
 */
public class GraphPartChangeException extends Exception {
    private final int ip;
    private final List<GraphTargetItem> output;

    public GraphPartChangeException(List<GraphTargetItem> output, int ip) {
        this.output = output;
        this.ip = ip;
    }

    public int getIp() {
        return ip;
    }

    public List<GraphTargetItem> getOutput() {
        return output;
    }


}
