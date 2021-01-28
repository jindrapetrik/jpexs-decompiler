package com.jpexs.decompiler.graph.precontinues;

/**
 *
 * @author JPEXS
 */
public class IfNode extends Node {
    public Node onTrue;
    public Node onFalse;

    @Override
    public String toString() {
        return "if" + super.toString();
    }

}
