/*
 *  Copyright (C) 2014 JPEXS, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.flash.importers;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.DefineBitsJPEG2Tag;
import com.jpexs.decompiler.flash.tags.DefineShape2Tag;
import com.jpexs.decompiler.flash.tags.DefineShape3Tag;
import com.jpexs.decompiler.flash.tags.DefineShape4Tag;
import com.jpexs.decompiler.flash.tags.DefineShapeTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.BoundedTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import javax.imageio.ImageIO;

/**
 *
 * @author JPEXS
 */
public class ShapeImporter {
    
    public Tag importImage(ShapeTag st, byte[] newData) throws IOException {
        SWF swf = st.getSwf();
        
        if(newData[0] == 'B' && newData[1] == 'M'){
            BufferedImage b = ImageIO.read(new ByteArrayInputStream(newData));
            ByteArrayOutputStream baos=new ByteArrayOutputStream();
            ImageIO.write(b, "PNG", baos);
            newData = baos.toByteArray();
        }
        
        DefineBitsJPEG2Tag jpeg2Tag = new DefineBitsJPEG2Tag(swf, null, swf.getNextCharacterId(), newData);
        jpeg2Tag.setModified(true);
        swf.tags.add(jpeg2Tag);
        swf.updateCharacters();
        st.setModified(true);
        SHAPEWITHSTYLE shapes = jpeg2Tag.getShape(st.getRect(new HashSet<BoundedTag>()), true);
        
        if (st instanceof DefineShapeTag) {
            DefineShapeTag dst = (DefineShapeTag) st;
            dst.shapes  = shapes;
        } else if (st instanceof DefineShape2Tag) {
            DefineShape2Tag dst = (DefineShape2Tag) st;
            dst.shapes = shapes;
        } else if (st instanceof DefineShape3Tag) {
            DefineShape3Tag dst = (DefineShape3Tag) st;
            dst.shapes = shapes;
        } else if (st instanceof DefineShape4Tag) {
            DefineShape4Tag dst = (DefineShape4Tag) st;
            dst.shapes = shapes;
        }
        
        return (Tag) st;
    }
}
