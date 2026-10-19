package com.jpexs.decompiler.flash.action;

import java.util.List;

/**
 *
 * @author JPEXS
 */
public interface ConstantPoolAwareAction {
    
    public void setConstantPool(List<String> constantPool);
    
    public List<String> getConstantPool();
}
