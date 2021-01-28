package com.jpexs.decompiler.graph.precontinues;

/**
 *
 * @author JPEXS
 */
public class WhileNode extends Node {
    public Node body;

    @Override
    public String toString() {
        return "while" + super.toString();
    }


}
