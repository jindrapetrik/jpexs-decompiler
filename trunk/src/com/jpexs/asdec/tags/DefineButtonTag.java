package com.jpexs.asdec.tags;

import com.jpexs.asdec.Main;
import com.jpexs.asdec.SWFInputStream;
import com.jpexs.asdec.SWFOutputStream;
import com.jpexs.asdec.abc.CopyOutputStream;
import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.types.BUTTONRECORD;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Defines a button character
 *
 * @author JPEXS
 */
public class DefineButtonTag extends Tag implements ASMSource {
    /**
     * ID for this character
     */
    public int buttonId;
    /**
     * Characters that make up the button
     */
    public List<BUTTONRECORD> characters;
    /**
     * Actions to perform
     */
    public List<Action> actions;

    /**
     * List of ExportAssetsTag used for converting to String
     */
    public List<ExportAssetsTag> exportAssetsTags = new ArrayList<ExportAssetsTag>();

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
    public DefineButtonTag(byte[] data, int version) throws IOException {
        super(7, data);
        SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
        buttonId = sis.readUI16();
        characters = sis.readBUTTONRECORDList(false);
        actions = sis.readActionList();
    }

    /**
     * Gets data bytes
     *
     * @param version SWF version
     * @return Bytes of data
     */
    @Override
    public byte[] getData(int version) {
        if (Main.DISABLE_DANGEROUS) return super.getData(version);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream os = baos;
        if (Main.DEBUG_COPY) {
            os = new CopyOutputStream(os, new ByteArrayInputStream(super.data));
        }
        SWFOutputStream sos = new SWFOutputStream(os, version);
        try {
            sos.writeUI16(buttonId);
            sos.writeBUTTONRECORDList(characters, false);
            sos.write(Action.actionsToBytes(actions, true, version));
            sos.close();
        } catch (IOException e) {

        }
        return baos.toByteArray();
    }

    /**
     * Returns string representation of the object
     *
     * @return String representation of the object
     */
    @Override
    public String toString() {
        String name = "";
        for (ExportAssetsTag eat : exportAssetsTags) {
            if (eat.assets.containsKey(buttonId)) {
                name = ": " + eat.assets.get((Integer) buttonId);
            }
        }
        return "DefineButtonTag (" + buttonId + name + ")";
    }

    /**
     * Converts actions to ASM source
     *
     * @param version SWF version
     * @return ASM source
     */
    public String getASMSource(int version) {
        String ret = "";
        for (Action a : actions) {
            ret += a.toString() + "\r\n";
        }
        return ret;
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