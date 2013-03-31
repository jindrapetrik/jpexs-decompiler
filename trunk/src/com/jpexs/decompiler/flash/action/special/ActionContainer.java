package com.jpexs.decompiler.flash.action.special;

import com.jpexs.decompiler.flash.action.Action;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public interface ActionContainer {

    public byte[] getHeaderBytes();

    public List<Action> getActions();

    public int getDataLength();
}
