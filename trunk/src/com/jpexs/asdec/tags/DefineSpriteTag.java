package com.jpexs.asdec.tags;

import com.jpexs.asdec.Main;
import com.jpexs.asdec.SWFInputStream;
import com.jpexs.asdec.SWFOutputStream;
import com.jpexs.asdec.abc.CopyOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Defines a sprite character
 */
public class DefineSpriteTag extends Tag implements Container {
    /**
     * Character ID of sprite
     */
    public int spriteId;
    /**
     * Number of frames in sprite
     */
    public int frameCount;
    /**
     * A series of tags
     */
    public List<Tag> subTags;

    /**
     * List of ExportAssetsTag used for converting to String
     */
    public List<ExportAssetsTag> exportAssetsTags = new ArrayList<ExportAssetsTag>();

    /**
     * Constructor
     *
     * @param data    Data bytes
     * @param version SWF version
     * @throws IOException
     */
    public DefineSpriteTag(byte[] data, int version) throws IOException {
        super(39, data);
        SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
        spriteId = sis.readUI16();
        frameCount = sis.readUI16();
        subTags = sis.readTagList();
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
            sos.writeUI16(spriteId);
            sos.writeUI16(frameCount);
            sos.writeTags(subTags);
            sos.writeUI16(0);
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
            if (eat.assets.containsKey(spriteId)) {
                name = ": " + eat.assets.get((Integer) spriteId);
            }
        }
        return "DefineSpriteTag (" + spriteId + name + ")";
    }

    /**
     * Returns all sub-items
     *
     * @return List of sub-items
     */
    public List<Object> getSubItems() {
        List<Object> ret = new ArrayList<Object>();
        ret.addAll(subTags);
        return ret;
    }

    /**
     * Returns number of sub-items
     *
     * @return Number of sub-items
     */
    public int getItemCount() {
        return subTags.size();
    }
}
