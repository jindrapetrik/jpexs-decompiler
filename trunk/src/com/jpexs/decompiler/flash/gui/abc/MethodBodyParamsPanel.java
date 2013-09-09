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

import com.jpexs.decompiler.flash.abc.types.MethodBody;
import static com.jpexs.decompiler.flash.gui.AppStrings.translate;
import com.jpexs.decompiler.flash.gui.MyFormattedTextField;
import com.jpexs.decompiler.flash.gui.View;
import java.awt.Color;
import java.awt.Dimension;
import java.text.NumberFormat;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author JPEXS
 */
public class MethodBodyParamsPanel extends JPanel implements ChangeListener {

    public JLabel maxStackLabel;
    public JFormattedTextField maxStackField;
    public JLabel localCountLabel;
    public JFormattedTextField localCountField;
    public JLabel initScopeDepthLabel;
    public JFormattedTextField initScopeDepthField;
    public JLabel maxScopeDepthLabel;
    public JFormattedTextField maxScopeDepthField;
    public MethodBody body;
    public JCheckBox autoFillCheckBox = new JCheckBox(translate("abc.detail.body.params.autofill"));
    public JLabel experimentalLabel = new JLabel(translate("abc.detail.body.params.autofill.experimental"));
    private ABCPanel abcPanel;

    public MethodBodyParamsPanel(ABCPanel abcPanel) {
        setLayout(null);
        this.abcPanel = abcPanel;

        JComponent[][] cmps = new JComponent[][]{
            {maxStackLabel = new JLabel(translate("abc.detail.body.params.maxstack"), SwingConstants.RIGHT), maxStackField = new MyFormattedTextField(NumberFormat.getNumberInstance())},
            {localCountLabel = new JLabel(translate("abc.detail.body.params.localregcount"), SwingConstants.RIGHT), localCountField = new MyFormattedTextField(NumberFormat.getNumberInstance())},
            {initScopeDepthLabel = new JLabel(translate("abc.detail.body.params.minscope"), SwingConstants.RIGHT), initScopeDepthField = new MyFormattedTextField(NumberFormat.getNumberInstance())},
            {maxScopeDepthLabel = new JLabel(translate("abc.detail.body.params.maxscope"), SwingConstants.RIGHT), maxScopeDepthField = new MyFormattedTextField(NumberFormat.getNumberInstance())}
        };


        int maxw = 0;
        for (int i = 0; i < cmps.length; i++) {
            Dimension d = cmps[i][0].getPreferredSize();
            if (d.width > maxw) {
                maxw = d.width;
            }
        }

        int top = 0;
        for (int i = 0; i < cmps.length; i++) {

            cmps[i][0].setBounds(10, top, maxw, cmps[i][1].getPreferredSize().height);
            cmps[i][1].setBounds(10 + maxw + 10, top, 75, cmps[i][1].getPreferredSize().height);
            add(cmps[i][0]);
            add(cmps[i][1]);
            top += cmps[i][1].getPreferredSize().height;
        }

        add(autoFillCheckBox);
        autoFillCheckBox.addChangeListener(this);

        experimentalLabel.setForeground(Color.red);

        autoFillCheckBox.setLocation(0, top);
        autoFillCheckBox.setSize(autoFillCheckBox.getPreferredSize());
        experimentalLabel.setLocation(20 + autoFillCheckBox.getWidth(), top);
        experimentalLabel.setSize(experimentalLabel.getPreferredSize());
        add(experimentalLabel);

        setPreferredSize(new Dimension(300, 150));
    }

    public void loadFromBody(MethodBody body) {
        this.body = body;
        if (body == null) {
            maxStackField.setText("0");
            localCountField.setText("0");
            initScopeDepthField.setText("0");
            maxScopeDepthField.setText("0");
            return;
        }
        maxStackField.setText("" + body.max_stack);
        localCountField.setText("" + body.max_regs);
        initScopeDepthField.setText("" + body.init_scope_depth);
        maxScopeDepthField.setText("" + body.max_scope_depth);
    }

    public boolean save() {
        if (body != null) {
            body.init_scope_depth = Integer.parseInt(initScopeDepthField.getText());
            if (!autoFillCheckBox.isSelected()) {
                body.max_stack = Integer.parseInt(maxStackField.getText());
                body.max_regs = Integer.parseInt(localCountField.getText());
                body.max_scope_depth = Integer.parseInt(maxScopeDepthField.getText());
            } else {
                if (!body.autoFillStats(abcPanel.abc)) {
                    View.showMessageDialog(null, translate("message.autofill.failed"), translate("message.warning"), JOptionPane.WARNING_MESSAGE);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == autoFillCheckBox) {
            if (autoFillCheckBox.isSelected()) {
                localCountField.setEnabled(false);
                maxScopeDepthField.setEnabled(false);
                maxStackField.setEnabled(false);
            } else {
                localCountField.setEnabled(true);
                maxScopeDepthField.setEnabled(true);
                maxStackField.setEnabled(true);
            }
        }
    }

    public void setEditMode(boolean val) {
        maxStackField.setEditable(val);
        localCountField.setEditable(val);
        initScopeDepthField.setEditable(val);
        maxScopeDepthField.setEditable(val);
        autoFillCheckBox.setEnabled(val);
    }
}
