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

package com.jpexs.asdec.abc.gui;

import com.jpexs.asdec.Main;
import com.jpexs.asdec.abc.ABC;
import com.jpexs.asdec.abc.methodinfo_parser.MethodInfoParser;
import com.jpexs.asdec.abc.methodinfo_parser.ParseException;
import com.jpexs.asdec.abc.types.MethodInfo;
import com.jpexs.asdec.helpers.Helper;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author JPEXS
 */
public class MethodInfoPanel extends JPanel {
   public LineMarkedEditorPane paramEditor;
   private MethodInfo methodInfo;
   private ABC abc;
   public MethodInfoPanel()
   {
      paramEditor=new LineMarkedEditorPane();
      setLayout(new BorderLayout());
      add(new JLabel("Parameters:"),BorderLayout.NORTH);
      add(new JScrollPane(paramEditor), BorderLayout.CENTER);
      paramEditor.setContentType("text/flasm3_methodinfo");
   }

   public void load(int methodInfoIndex,ABC abc)
   {
      this.abc=abc;
      if(methodInfoIndex<=0)
      {
         paramEditor.setText("");
      }
      this.methodInfo=abc.method_info[methodInfoIndex];
      int p=0;
      String ret="";
      int optParPos=0;
      if(methodInfo.flagHas_optional())
      {
         optParPos=methodInfo.param_types.length-methodInfo.optional.length;
      }
      for(int ptype:methodInfo.param_types)
      {
         if(p>0){
            ret+=",\n";
         }
         if(methodInfo.flagHas_paramnames()&&Main.PARAM_NAMES_ENABLE)
         {
            ret=ret+abc.constants.constant_string[methodInfo.paramNames[p]];
         }else{
            ret=ret+"param"+(p+1);
         }
         ret+=":";
         if(ptype==0){
            ret+="*";
         }else{
            ret+="m["+ptype+"]\""+Helper.escapeString(abc.constants.constant_multiname[ptype].toString(abc.constants))+"\"";
         }
         if(methodInfo.flagHas_optional())
         {
            if(p>=optParPos)
            {
               ret+="="+methodInfo.optional[p-optParPos].toString(abc.constants);               
            }
         }
         p++;
      }
      if(methodInfo.flagNeed_rest()){
         ret+=",\n... rest";
      }
      paramEditor.setText(ret);
   }

   public boolean save()
   {
      try {
         MethodInfoParser.parse(paramEditor.getText(), methodInfo, abc);
      } catch (ParseException ex) {
        JOptionPane.showMessageDialog(paramEditor, ex.text, "MethodInfo Error", JOptionPane.ERROR_MESSAGE);
        return false;
      }
      return true;
   }
}
