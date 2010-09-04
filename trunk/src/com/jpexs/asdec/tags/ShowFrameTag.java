package com.jpexs.asdec.tags;

/**
 * Instructs Flash Player to display the contents of the display list
 *
 * @author JPEXS
 */
public class ShowFrameTag extends Tag {
    /**
     * Constructor
     */
    public ShowFrameTag() {
        super(1, new byte[0]);
    }

    /**
     * Gets data bytes
     *
     * @param version SWF version
     * @return Bytes of data
     */
    @Override
    public byte[] getData(int version) {
        return super.getData(version);
    }

    /**
     * Returns string representation of the object
     *
     * @return String representation of the object
     */
    @Override
    public String toString() {
        return "ShowFrame";
    }
}
