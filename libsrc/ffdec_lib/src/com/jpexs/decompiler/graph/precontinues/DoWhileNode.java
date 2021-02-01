package com.jpexs.decompiler.graph.precontinues;

/**
 *
 * @author JPEXS
 */
public class DoWhileNode extends Node {
    public Node body;

    @Override
    public String toString() {
        return "dowhile" + super.toString();
    }
}
