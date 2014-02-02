/*
 *  Copyright (C) 2010-2014 JPEXS
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

import com.jpexs.decompiler.flash.SWF;
import java.util.List;

/**
 * Instructs Flash Player to display the contents of the display list
 *
 * @author JPEXS
 */
public class ShowFrameTag extends Tag {

    public static final int ID = 1;
    
    public List<Tag> innerTags;

    /**
     * Constructor
     *
     * @param swf
     * @param pos
     */
    public ShowFrameTag(SWF swf, long pos) {
        super(swf, ID, "ShowFrame", new byte[0], pos);
    }

    public ShowFrameTag(SWF swf) {
        super(swf, ID, "ShowFrame", new byte[0], 0);
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
}
