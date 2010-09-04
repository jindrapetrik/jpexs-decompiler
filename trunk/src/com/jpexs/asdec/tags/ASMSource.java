package com.jpexs.asdec.tags;

import com.jpexs.asdec.action.Action;

import java.util.List;

/**
 * Object containing ASM source
 *
 * @author JPEXS
 */
public interface ASMSource {
    /**
     * Converts actions to ASM source
     *
     * @param version SWF version
     * @return ASM source
     */
    public String getASMSource(int version);

    /**
     * Whether or not this object contains ASM source
     *
     * @return True when contains
     */
    public boolean containsSource();

    /**
     * Returns actions associated with this object
     *
     * @return List of actions
     */
    public List<Action> getActions();

    /**
     * Sets actions associated with this object
     *
     * @param actions Action list
     */
    public void setActions(List<Action> actions);
}
