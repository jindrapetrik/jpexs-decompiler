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
import java.awt.Color;
import java.awt.Dimension;
import java.text.NumberFormat;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import static com.jpexs.decompiler.flash.gui.AppStrings.translate;

/**
 *
 * @author JPEXS
 */
public class MethodBodyParamsPanel extends JPanel implements ChangeListener {

    public JLabel maxStackLabel = new JLabel(translate("abc.detail.body.params.maxstack"), SwingConstants.RIGHT);
    public JFormattedTextField maxStackField = new JFormattedTextField(NumberFormat.getNumberInstance());
    public JLabel localCountLabel = new JLabel(translate("abc.detail.body.params.localregcount"), SwingConstants.RIGHT);
    public JFormattedTextField localCountField = new JFormattedTextField(NumberFormat.getNumberInstance());
    public JLabel initScopeDepthLabel = new JLabel(translate("abc.detail.body.params.minscope"), SwingConstants.RIGHT);
    public JFormattedTextField initScopeDepthField = new JFormattedTextField(NumberFormat.getNumberInstance());
    public JLabel maxScopeDepthLabel = new JLabel(translate("abc.detail.body.params.maxscope"), SwingConstants.RIGHT);
    public JFormattedTextField maxScopeDepthField = new JFormattedTextField(NumberFormat.getNumberInstance());
    public MethodBody body;
    public JCheckBox autoFillCheckBox = new JCheckBox(translate("abc.detail.body.params.autofill"));
    public JLabel experimentalLabel = new JLabel(translate("abc.detail.body.params.autofill.experimental"));
    private ABCPanel abcPanel;

    public MethodBodyParamsPanel(ABCPanel abcPanel) {
        setLayout(null);
        this.abcPanel = abcPanel;

        maxStackLabel.setBounds(10, 10, 150, 25);
        maxStackField.setBounds(10 + 150 + 10, 10, 75, 25);
        add(maxStackLabel);
        add(maxStackField);

        localCountLabel.setBounds(10, 10 + 30, 150, 25);
        localCountField.setBounds(10 + 150 + 10, 10 + 30, 75, 25);
        add(localCountLabel);
        add(localCountField);

        initScopeDepthLabel.setBounds(10, 10 + 30 + 30, 150, 25);
        initScopeDepthField.setBounds(10 + 150 + 10, 10 + 30 + 30, 75, 25);
        add(initScopeDepthLabel);
        add(initScopeDepthField);

        maxScopeDepthLabel.setBounds(10, 10 + 30 + 30 + 30, 150, 25);
        maxScopeDepthField.setBounds(10 + 150 + 10, 10 + 30 + 30 + 30, 75, 25);
        add(maxScopeDepthLabel);
        add(maxScopeDepthField);

        autoFillCheckBox.setBounds(30, 10 + 30 + 30 + 30 + 30, 230, 25);
        add(autoFillCheckBox);
        autoFillCheckBox.addChangeListener(this);

        experimentalLabel.setForeground(Color.red);
        experimentalLabel.setBounds(250, 10 + 30 + 30 + 30 + 30, 100, 25);
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
                    JOptionPane.showMessageDialog(null, translate("message.autofill.failed"), translate("message.warning"), JOptionPane.WARNING_MESSAGE);
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
