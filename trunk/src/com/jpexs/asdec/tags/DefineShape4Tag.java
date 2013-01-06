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

import com.jpexs.asdec.SWFInputStream;
import com.jpexs.asdec.tags.base.BoundedTag;
import com.jpexs.asdec.tags.base.CharacterTag;
import com.jpexs.asdec.types.RECT;
import com.jpexs.asdec.types.SHAPEWITHSTYLE;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DefineShape4Tag extends Tag implements CharacterTag,BoundedTag{

   public int shapeId;
   public RECT shapeBounds;
   public RECT edgeBounds;
   public boolean usesFillWindingRule;
   public boolean usesNonScalingStrokes;
   public boolean usesScalingStrokes;
   public SHAPEWITHSTYLE shapes;

   @Override
   public int getCharacterID() {
      return shapeId;
   }
   
   @Override
   public RECT getRect() {
      return shapeBounds;
   }
   
   public DefineShape4Tag(byte[] data, int version, long pos) throws IOException {
      super(83, data, pos);
      SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
      shapeId = sis.readUI16();
      shapeBounds = sis.readRECT();
      edgeBounds = sis.readRECT();
      sis.readUB(5);
      usesFillWindingRule = sis.readUB(1) == 1;
      usesNonScalingStrokes = sis.readUB(1) == 1;
      usesScalingStrokes = sis.readUB(1) == 1;
      shapes = sis.readSHAPEWITHSTYLE(4);
   }

   public List<ExportAssetsTag> exportAssetsTags = new ArrayList<ExportAssetsTag>();
   
   @Override
   public String toString() {
      String name = "";
      for (ExportAssetsTag eat : exportAssetsTags) {
         int pos = eat.tags.indexOf(shapeId);
         if (pos > -1) {
            name = ": " + eat.names.get(pos);
         }
      }
      return "DefineShape4 (" + shapeId + name + ")";
   }
}
