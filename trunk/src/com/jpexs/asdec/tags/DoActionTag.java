package com.jpexs.asdec.tags;

import com.jpexs.asdec.SWFInputStream;
import com.jpexs.asdec.action.Action;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Instructs Flash Player to perform a list of actions when the current frame is complete.
 */
public class DoActionTag extends Tag implements ASMSource {

    /**
     * List of actions to perform
     */
    public List<Action> actions = new ArrayList<Action>();


    /**
     * Sets actions associated with this object
     *
     * @param actions Action list
     */
    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    /**
     * Constructor
     *
     * @param data    Data bytes
     * @param version SWF version
     * @throws IOException
     */
    public DoActionTag(byte[] data, int version) {
        super(12, data);
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            SWFInputStream sis = new SWFInputStream(bais, version);
            actions = sis.readActionList();
        } catch (IOException e) {

        }
    }

    /**
     * Gets data bytes
     *
     * @param version SWF version
     * @return Bytes of data
     */
    @Override
    public byte[] getData(int version) {
        return Action.actionsToBytes(actions, true, version);
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
     * Returns string representation of the object
     *
     * @return String representation of the object
     */
    @Override
    public String toString() {
        return "DoActionTag";
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