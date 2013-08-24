/*
 *  Copyright (C) 2011-2013 JPEXS
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
package com.jpexs.decompiler.flash.gui.abc;

import com.jpexs.decompiler.flash.Configuration;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.methodinfo_parser.MethodInfoParser;
import com.jpexs.decompiler.flash.abc.methodinfo_parser.ParseException;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import static com.jpexs.decompiler.flash.gui.AppStrings.translate;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.helpers.Helper;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import javax.swing.*;
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
    private JLabel methodIndexLabel;

    public MethodInfoPanel() {
        returnTypeEditor = new UndoFixedEditorPane();
        paramEditor = new LineMarkedEditorPane();
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        JPanel miPanel = new JPanel();
        miPanel.setLayout(new BoxLayout(miPanel, BoxLayout.LINE_AXIS));
        miPanel.add(new JLabel(translate("abc.detail.methodinfo.methodindex")));
        methodIndexLabel = new JLabel("   ");
        miPanel.add(methodIndexLabel);
        add(miPanel);
        add(new JLabel(translate("abc.detail.methodinfo.parameters")));
        add(new JScrollPane(paramEditor));
        add(new JLabel(translate("abc.detail.methodinfo.returnvalue")));
        JScrollPane jsp = new JScrollPane(returnTypeEditor);
        add(jsp);
        paramEditor.setContentType("text/flasm3_methodinfo");
        returnTypeEditor.setContentType("text/flasm3_methodinfo");
        paramEditor.setFont(new Font("Monospaced", Font.PLAIN, paramEditor.getFont().getSize()));
        returnTypeEditor.setFont(new Font("Monospaced", Font.PLAIN, returnTypeEditor.getFont().getSize()));

        jsp.setMaximumSize(new Dimension(1024, 25));
        Flasm3MethodInfoSyntaxKit sk = (Flasm3MethodInfoSyntaxKit) returnTypeEditor.getEditorKit();
        sk.deinstallComponent(returnTypeEditor, "jsyntaxpane.components.LineNumbersRuler");
    }

    public void load(int methodInfoIndex, ABC abc) {
        this.abc = abc;
        if (methodInfoIndex <= 0) {
            paramEditor.setText("");
        }

        methodIndexLabel.setText("" + methodInfoIndex);
        this.methodInfo = abc.method_info[methodInfoIndex];
        int p = 0;
        String ret = "";
        int optParPos = 0;
        if (methodInfo.flagHas_optional()) {
            optParPos = methodInfo.param_types.length - methodInfo.optional.length;
        }
        for (int ptype : methodInfo.param_types) {
            if (p > 0) {
                ret += ",\n";
            }
            if (methodInfo.flagHas_paramnames() && Configuration.PARAM_NAMES_ENABLE) {
                ret = ret + abc.constants.constant_string[methodInfo.paramNames[p]];
            } else {
                ret = ret + "param" + (p + 1);
            }
            ret += ":";
            if (ptype == 0) {
                ret += "*";
            } else {
                ret += "m[" + ptype + "]\"" + Helper.escapeString(abc.constants.constant_multiname[ptype].toString(abc.constants, new ArrayList<String>())) + "\"";
            }
            if (methodInfo.flagHas_optional()) {
                if (p >= optParPos) {
                    ret += "=" + methodInfo.optional[p - optParPos].toString(abc.constants);
                }
            }
            p++;
        }
        if (methodInfo.flagNeed_rest()) {
            if (p > 0) {
                ret += ",\n";
            }
            ret += "... rest";
        }
        paramEditor.setText(ret);
        if (methodInfo.ret_type == 0) {
            returnTypeEditor.setText("*");
        } else {
            returnTypeEditor.setText("m[" + methodInfo.ret_type + "]\"" + Helper.escapeString(abc.constants.constant_multiname[methodInfo.ret_type].toString(abc.constants, new ArrayList<String>())) + "\"");
        }
    }

    public boolean save() {
        try {
            MethodInfoParser.parseParams(paramEditor.getText(), methodInfo, abc);
        } catch (ParseException ex) {
            View.showMessageDialog(paramEditor, ex.text, translate("error.methodinfo.params"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        try {
            MethodInfoParser.parseReturnType(returnTypeEditor.getText(), methodInfo);
        } catch (ParseException ex) {
            View.showMessageDialog(returnTypeEditor, ex.text, translate("error.methodinfo.returnvalue"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    public void setEditMode(boolean val) {
        returnTypeEditor.setEditable(val);
        paramEditor.setEditable(val);
    }
}
