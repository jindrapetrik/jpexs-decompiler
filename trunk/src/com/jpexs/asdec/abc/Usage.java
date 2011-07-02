/*
 *  Copyright (C) 2010-2011 JPEXS
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

package com.jpexs.asdec.abc;

import com.jpexs.asdec.abc.types.traits.Trait;
import com.jpexs.asdec.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.asdec.abc.types.traits.TraitSlotConst;

/**
 *
 * @author JPEXS
 */
public class Usage {
    public int classIndex;
    public int traitIndex;
    public int subTraitIndex;
    public boolean isStatic;
    public static final int TYPE_CLASS_NAME=0;
    public static final int TYPE_TRAIT_NAME=1;
    public static final int TYPE_SUBTRAIT_NAME=4;
    public static final int TYPE_INITIALIZER_SUBTRAIT_NAME=5;
    public static final int TYPE_INITIALIZER=2;
    public static final int TYPE_TRAIT_BODY=3;
    public int type;
    public ABC abc;
    public int bodyIndex;


    public Usage(ABC abc,int bodyIndex,int classIndex, int traitIndex, int subTraitIndex, boolean isStatic, int type) {
        this.abc = abc;
        this.classIndex = classIndex;
        this.traitIndex = traitIndex;
        this.isStatic = isStatic;
        this.type = type;
        this.subTraitIndex = subTraitIndex;
        this.bodyIndex = bodyIndex;
    }

    @Override
    public String toString() {
        String ret="";
        String className=abc.instance_info[classIndex].getName(abc.constants).getNameWithNamespace(abc.constants);
        Trait tr=null;
        String staticStr="";
        String traitName="";
        String subTraitName="";
        switch(type){
            case TYPE_CLASS_NAME:
                return "Class "+className;
            case TYPE_TRAIT_NAME:
                 
                 if(isStatic){
                     tr=abc.class_info[classIndex].static_traits.traits[traitIndex];
                 }else{
                     tr=abc.instance_info[classIndex].instance_traits.traits[traitIndex];
                 }
                 traitName=tr.getMultiName(abc.constants).getName(abc.constants);
                 staticStr=isStatic?"static ":"";
                 String typeStr="";
                 if(tr instanceof TraitMethodGetterSetter) typeStr="method ";
                 if(tr instanceof TraitSlotConst) {
                     if(((TraitSlotConst)tr).isConst()) typeStr="const ";
                     if(((TraitSlotConst)tr).isVar()) typeStr="var ";
                 }
                 return staticStr+typeStr+className+"."+traitName;
            case TYPE_INITIALIZER_SUBTRAIT_NAME:
            case TYPE_INITIALIZER:
                if(type==TYPE_SUBTRAIT_NAME){
                     subTraitName="."+abc.bodies[bodyIndex].traits.traits[subTraitIndex].getMultiName(abc.constants).getName(abc.constants);
                 }
                if(isStatic){
                   return "static initializer "+className+subTraitName;
                }else{
                    return "instance initializer "+className+subTraitName;
                }
            case TYPE_SUBTRAIT_NAME:
            case TYPE_TRAIT_BODY:
                 if(isStatic){
                     tr=abc.class_info[classIndex].static_traits.traits[traitIndex];
                 }else{
                     tr=abc.instance_info[classIndex].instance_traits.traits[traitIndex];
                 }
                 traitName=tr.getMultiName(abc.constants).getName(abc.constants);

                 if(type==TYPE_SUBTRAIT_NAME){
                     subTraitName="."+abc.bodies[bodyIndex].traits.traits[subTraitIndex].getMultiName(abc.constants).getName(abc.constants);
                 }
                 staticStr=isStatic?"static ":"";
                 return staticStr+"method body "+className+"."+traitName+subTraitName;
        }
        return ret;
    }


    
}
