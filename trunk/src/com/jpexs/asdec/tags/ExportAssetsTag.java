package com.jpexs.asdec.tags;

import com.jpexs.asdec.SWFInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * Makes portions of a SWF file available for import by other SWF files
 *
 * @author JPEXS
 */
public class ExportAssetsTag extends Tag {
    /**
     * HashMap with assets
     */
    public HashMap<Integer, String> assets;

    /**
     * Constructor
     *
     * @param data    Data bytes
     * @param version SWF version
     * @throws IOException
     */
    public ExportAssetsTag(byte[] data, int version) throws IOException {
        super(56, data);
        assets = new HashMap<Integer, String>();
        SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
        int count = sis.readUI16();
        for (int i = 0; i < count; i++) {
            int characterId = sis.readUI16();
            String name = sis.readString();
            assets.put(characterId, name);
        }
    }

    /**
     * Returns string representation of the object
     *
     * @return String representation of the object
     */
    @Override
    public String toString() {
        return "ExportAssets";
    }
}
