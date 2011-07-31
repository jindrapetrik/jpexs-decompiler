/*
 *  Copyright (C) 2011 JPEXS
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.jpexs.asdec.abc.usages;

import com.jpexs.asdec.abc.ABC;

/**
 *
 * @author JPEXS
 */
public class MethodBodyMultinameUsage extends MethodMultinameUsage {

    public MethodBodyMultinameUsage(int multinameIndex,int classIndex,int traitIndex,boolean isStatic,boolean isInitializer)
    {
       super(multinameIndex,classIndex,traitIndex,isStatic,isInitializer);
    }

   @Override
   public String toString(ABC abc) {
      return super.toString(abc)+" body";
   }
}
