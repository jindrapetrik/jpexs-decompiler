/*
 *  Copyright (C) 2010-2011 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
