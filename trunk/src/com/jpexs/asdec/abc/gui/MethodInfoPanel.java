/*
 *  Copyright (C) 2011 JPEXS
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

package com.jpexs.asdec.abc.gui;

import com.jpexs.asdec.Main;
import com.jpexs.asdec.abc.ABC;
import com.jpexs.asdec.abc.methodinfo_parser.MethodInfoParser;
import com.jpexs.asdec.abc.methodinfo_parser.ParseException;
import com.jpexs.asdec.abc.types.MethodInfo;
import com.jpexs.asdec.helpers.Helper;
import java.awt.Dimension;
import javax.swing.BoxLayout;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import jsyntaxpane.syntaxkits.Flasm3MethodInfoSyntaxKit;

/**
 *
 * @author JPEXS
 */
public class MethodInfoPanel extends JPanel {
   public LineMarkedEditorPane paramEditor;
   public JEditorPane returnTypeEditor;
   private MethodInfo methodInfo;
   private ABC abc;
   public MethodInfoPanel()
   {
      returnTypeEditor=new JEditorPane();
      paramEditor=new LineMarkedEditorPane();
      setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS));
      add(new JLabel("Parameters:"));
      add(new JScrollPane(paramEditor));
      add(new JLabel("Return value type:"));
      JScrollPane jsp=new JScrollPane(returnTypeEditor);
      add(jsp);
      paramEditor.setContentType("text/flasm3_methodinfo");
      returnTypeEditor.setContentType("text/flasm3_methodinfo");
      jsp.setMaximumSize(new Dimension(1024,25));
      Flasm3MethodInfoSyntaxKit sk=(Flasm3MethodInfoSyntaxKit)returnTypeEditor.getEditorKit();
      sk.deinstallComponent(returnTypeEditor, "jsyntaxpane.components.LineNumbersRuler");
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
      if(methodInfo.ret_type==0){
         returnTypeEditor.setText("*");
      }else{
         returnTypeEditor.setText("m["+methodInfo.ret_type+"]\""+Helper.escapeString(abc.constants.constant_multiname[methodInfo.ret_type].toString(abc.constants))+"\"");
      }
   }

   public boolean save()
   {
      try {
         MethodInfoParser.parseParams(paramEditor.getText(), methodInfo, abc);
      } catch (ParseException ex) {
        JOptionPane.showMessageDialog(paramEditor, ex.text, "MethodInfo Params Error", JOptionPane.ERROR_MESSAGE);
        return false;
      }
      try {
         MethodInfoParser.parseReturnType(returnTypeEditor.getText(), methodInfo);
      } catch (ParseException ex) {
        JOptionPane.showMessageDialog(returnTypeEditor, ex.text, "MethodInfo Return type Error", JOptionPane.ERROR_MESSAGE);
        return false;
      }
      return true;
   }
}
