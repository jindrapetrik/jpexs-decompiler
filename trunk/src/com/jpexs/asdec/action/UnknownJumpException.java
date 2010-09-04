package com.jpexs.asdec.action;

import com.jpexs.asdec.action.treemodel.TreeItem;

import java.util.List;
import java.util.Stack;

/**
 * Raised when actual address has been referenced with an unknown jump
 *
 * @author JPEXS
 */
public class UnknownJumpException extends RuntimeException {
    /**
     * Actual stack
     */
    public Stack stack;
    /**
     * Actual address
     */
    public long addr;
    /**
     * Output of the method before raising the exception
     */
    public List<TreeItem> output;

    /**
     * Constructor
     *
     * @param stack  Actual stack
     * @param addr   Actual address
     * @param output Output of the method before raising the exception
     */
    public UnknownJumpException(Stack stack, long addr, List<TreeItem> output) {
        this.stack = stack;
        this.addr = addr;
        this.output = output;
    }

    /**
     * Returns a string representation of the object
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "Unknown jump to " + addr;
    }


}