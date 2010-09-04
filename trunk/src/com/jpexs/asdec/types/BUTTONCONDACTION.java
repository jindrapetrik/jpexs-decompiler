package com.jpexs.asdec.types;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.tags.ASMSource;

import java.util.List;

/**
 * Actions to execute at particular button events
 *
 * @author JPEXS
 */
public class BUTTONCONDACTION implements ASMSource {
    /**
     * Is this BUTTONCONDACTION last in the list?
     */
    public boolean isLast;
    /**
     * Idle to OverDown
     */
    public boolean condIdleToOverDown;
    /**
     * OutDown to Idle
     */
    public boolean condOutDownToIdle;
    /**
     * OutDown to OverDown
     */
    public boolean condOutDownToOverDown;
    /**
     * OverDown to OutDown
     */
    public boolean condOverDownToOutDown;
    /**
     * OverDown to OverUp
     */
    public boolean condOverDownToOverUp;
    /**
     * OverUp to OverDown
     */
    public boolean condOverUpToOverDown;
    /**
     * OverUp to Idle
     */
    public boolean condOverUpToIddle;
    /**
     * Idle to OverUp
     */
    public boolean condIdleToOverUp;
    /**
     * @since SWF 4
     *        key code
     */
    public int condKeyPress;
    /**
     * OverDown to Idle
     */
    public boolean condOverDownToIddle;
    /**
     * Actions to perform
     */
    public List<Action> actions;

    /**
     * Sets actions associated with this object
     *
     * @param actions Action list
     */
    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    /**
     * Returns a string representation of the object
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "BUTTONCONDACTION";
    }

    /**
     * Converts actions to ASM source
     *
     * @param version SWF version
     * @return ASM source
     */
    public String getASMSource(int version) {
        return Action.actionsToString(actions, null, version);
    }

    /**
     * Whether or not this object contains ASM source
     *
     * @return True when contains
     */
    public boolean containsSource() {
        return true;
    }

    /**
     * Returns actions associated with this object
     *
     * @return List of actions
     */
    public List<Action> getActions() {
        return actions;
    }
}
