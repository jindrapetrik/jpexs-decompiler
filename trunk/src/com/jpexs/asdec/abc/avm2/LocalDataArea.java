/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


public class LocalDataArea {
    public Stack operandStack = new Stack();
    public Stack scopeStack = new Stack();
    public List localRegisters = new ArrayList<Object>();

}
