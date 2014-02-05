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
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.tags.base.BoundedTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Stack;

/**
 *
 *
 * @author JPEXS
 */
public class DefineVideoStreamTag extends CharacterTag implements BoundedTag {

    @SWFType(BasicType.UI16)
    public int characterID;
    
    @SWFType(BasicType.UI16)
    public int numFrames;
    
    @SWFType(BasicType.UI16)
    public int width;
    
    @SWFType(BasicType.UI16)
    public int height;
    
    @SWFType(value=BasicType.UB,count=3)
    public int videoFlagsDeblocking;
    
    public boolean videoFlagsSmoothing;
    
    @SWFType(BasicType.UI8)
    public int codecID;
    
    public static final int CODEC_SORENSON_H263 = 2;
    public static final int CODEC_SCREEN_VIDEO = 3;
    public static final int CODEC_VP6 = 4;
    public static final int CODEC_VP6_ALPHA = 5;
    public static final int ID = 60;

    @Override
    public int getCharacterId() {
        return characterID;
    }

    /**
     * Gets data bytes
     *
     * @param version SWF version
     * @return Bytes of data
     */
    @Override
    public byte[] getData(int version) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream os = baos;
        SWFOutputStream sos = new SWFOutputStream(os, version);
        try {
            sos.writeUI16(characterID);
            sos.writeUI16(numFrames);
            sos.writeUI16(width);
            sos.writeUI16(height);
            sos.writeUB(4, 0);
            sos.writeUB(3, videoFlagsDeblocking);
            sos.writeUB(1, videoFlagsSmoothing ? 1 : 0);
            sos.writeUI8(codecID);
        } catch (IOException e) {
        }
        return baos.toByteArray();
    }

    /**
     * Constructor
     *
     * @param data Data bytes
     * @param version SWF version
     * @throws IOException
     */
    public DefineVideoStreamTag(SWF swf, byte[] data, int version, long pos) throws IOException {
        super(swf, ID, "DefineVideoStream", data, pos);
        SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
        characterID = sis.readUI16();
        numFrames = sis.readUI16();
        width = sis.readUI16();
        height = sis.readUI16();
        sis.readUB(4);
        videoFlagsDeblocking = (int) sis.readUB(3);
        videoFlagsSmoothing = sis.readUB(1) == 1;
        codecID = sis.readUI8();
    }

    @Override
    public RECT getRect(HashMap<Integer, CharacterTag> characters, Stack<Integer> visited) {
        return new RECT(0, (int) (SWF.unitDivisor * width), 0, (int) (SWF.unitDivisor * height));
    }
}
