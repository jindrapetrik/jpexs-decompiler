package com.jpexs.asdec.types;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.tags.ASMSource;

import java.util.List;

/**
 * Event handler
 *
 * @author JPEXS
 */
public class CLIPACTIONRECORD implements ASMSource {
    /**
     * Events to which this handler applies
     */
    public CLIPEVENTFLAGS eventFlags;
    /**
     * If EventFlags contain ClipEventKeyPress: Key code to trap
     */
    public int keyCode;
    /**
     * Actions to perform
     */
    public List<Action> actions;

    /**
     * Returns a string representation of the object
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "CLIPACTIONRECORD";
    }

    /**
     * Sets actions associated with this object
     *
     * @param actions Action list
     */
    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    /**
     * Returns header with events converted to string
     *
     * @return String representation of events
     */
    public String getHeader() {
        String ret = "";
        ret = eventFlags.toString();
        if (eventFlags.clipEventKeyPress) {
            ret = ret.replace("keyPress", "keyPress<" + keyCode + ">");
        }
        return ret;
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
