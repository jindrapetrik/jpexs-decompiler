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
package com.jpexs.asdec.abc.types;

import com.jpexs.asdec.abc.ABC;
import com.jpexs.asdec.abc.types.traits.Trait;
import com.jpexs.asdec.abc.types.traits.TraitClass;
import com.jpexs.asdec.abc.types.traits.Traits;
import com.jpexs.asdec.tags.DoABCTag;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ScriptInfo {

   public int init_index; //MethodInfo
   public Traits traits;

   @Override
   public String toString() {
      return "method_index=" + init_index + "\r\n" + traits.toString();
   }

   public String toString(ABC abc, List<String> fullyQualifiedNames) {
      return "method_index=" + init_index + "\r\n" + traits.toString(abc, fullyQualifiedNames);
   }

   public String getPath(ABC abc) {
      String packageName="";
      String scriptName="";
      int classCount=0;
      for (Trait t : traits.traits) {
         Multiname name = t.getName(abc);
         Namespace ns = name.getNamespace(abc.constants);
         if ((ns.kind == Namespace.KIND_PACKAGE) || (ns.kind == Namespace.KIND_PACKAGE_INTERNAL)) {
            packageName=ns.getName(abc.constants);
            scriptName= name.getName(abc.constants, new ArrayList<String>());            
            if(t instanceof TraitClass){
               classCount++;
            }
         }
      }
      if(classCount>1){
         scriptName = "[script]";
      }
      return packageName+"."+scriptName;
   }

   public String convert(List<DoABCTag> abcTags, ABC abc, boolean pcode, boolean highlighting) {
      return traits.convert("",abcTags, abc, false, pcode, true, -1, highlighting, new ArrayList<String>());
   }

   public void export(ABC abc, List<DoABCTag> abcList, String directory, boolean pcode) throws IOException {
      String path = getPath(abc);
      String packageName = path.substring(0, path.lastIndexOf("."));
      String className = path.substring(path.lastIndexOf(".") + 1);
      File outDir = new File(directory + File.separatorChar + packageName.replace('.', File.separatorChar));
      if (!outDir.exists()) {
         outDir.mkdirs();
      }
      String fileName = outDir.toString() + File.separator + className + ".as";
      FileOutputStream fos = new FileOutputStream(fileName);
      fos.write(convert(abcList, abc, pcode, false).getBytes());
      fos.close();

   }
}
