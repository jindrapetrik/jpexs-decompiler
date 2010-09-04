package com.jpexs.asdec.tags;

/**
 * Represents Tag inside SWF file
 */
public class Tag {
    /**
     * Identifier of tag type
     */
    protected int id;
    /**
     * Data in the tag
     */
    protected byte data[];

    /**
     * If true, then Tag is written to the stream as longer than 0x3f even if it is not
     */
    public boolean forceWriteAsLong = false;

    /**
     * Returns identifier of tag type
     *
     * @return Identifier of tag type
     */
    public int getId() {
        return id;
    }

    /**
     * Constructor
     *
     * @param id   Tag type identifier
     * @param data Bytes of data
     */
    public Tag(int id, byte[] data) {
        this.id = id;
        this.data = data;
    }

    /**
     * Gets data bytes
     *
     * @param version SWF version
     * @return Bytes of data
     */
    public byte[] getData(int version) {
        return data;
    }

    /**
     * Returns string representation of the object
     *
     * @return String representation of the object
     */
    @Override
    public String toString() {
        return "Tag id:" + id;
    }

    public final long getOrigDataLength() {
        return data.length;
    }

}