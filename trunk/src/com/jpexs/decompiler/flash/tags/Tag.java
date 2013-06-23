/*
 *  Copyright (C) 2010-2013 JPEXS
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
package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.NeedsCharacters;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents Tag inside SWF file
 */
public class Tag implements NeedsCharacters {

    /**
     * Identifier of tag type
     */
    protected int id;
    /**
     * Data in the tag
     */
    protected byte data[];
    /**
     * If true, then Tag is written to the stream as longer than 0x3f even if it
     * is not
     */
    public boolean forceWriteAsLong = false;
    private final long pos;
    protected String name;
    public Tag previousTag;

    public String getName() {
        return name;
    }

    public String getExportFileName() {
        return getName();
    }

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
     * @param id Tag type identifier
     * @param name Tag name
     * @param data Bytes of data
     */
    public Tag(int id, String name, byte[] data, long pos) {
        this.id = id;
        this.name = name;
        this.data = data;
        this.pos = pos;
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
     * Gets original read data
     *
     * @return Bytes of data
     */
    public byte[] getOriginalData() {
        return data;
    }

    /**
     * Returns string representation of the object
     *
     * @return String representation of the object
     */
    @Override
    public String toString() {
        return getName();
    }

    public final long getOrigDataLength() {
        return data.length;
    }

    public boolean hasSubTags() {
        return false;
    }

    public List<Tag> getSubTags() {
        return null;
    }

    public long getPos() {
        return pos;
    }

    @Override
    public Set<Integer> getNeededCharacters() {
        return new HashSet<>();
    }

    public Set<Integer> getDeepNeededCharacters(HashMap<Integer, CharacterTag> characters) {
        Set<Integer> ret = new HashSet<>();
        Set<Integer> needed = getNeededCharacters();
        for (int ch : needed) {
            if (!characters.containsKey(ch)) { //TODO: use Import tag (?)
                continue;
            }
            ret.add(ch);
            ret.addAll(characters.get(ch).getDeepNeededCharacters(characters));
        }
        return ret;
    }
}
