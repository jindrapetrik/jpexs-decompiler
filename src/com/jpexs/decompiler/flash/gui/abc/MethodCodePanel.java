/*
 *  Copyright (C) 2010-2015 JPEXS
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

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.gui.AppStrings;
import com.jpexs.decompiler.flash.gui.Main;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.helpers.hilight.HighlightSpecialType;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;

/**
 *
 * @author JPEXS
 */
public class MethodCodePanel extends JPanel implements ActionListener {

    private static final String ACTION_GRAPH = "GRAPH";

    private static final String ACTION_HEX = "HEX";

    private static final String ACTION_HEX_ONLY = "HEXONLY";

    private final ASMSourceEditorPane sourceTextArea;

    public JPanel buttonsPanel;

    private final JToggleButton hexButton;

    private final JToggleButton hexOnlyButton;

    public void focusEditor() {
        sourceTextArea.requestFocusInWindow();
    }

    public int getScriptIndex(){
        return sourceTextArea.getScriptIndex();
    }
    
    public String getTraitName() {
        return sourceTextArea.getName();
    }

    public void setIgnoreCarret(boolean ignoreCarret) {
        sourceTextArea.setIgnoreCarret(ignoreCarret);
    }

    public void hilighOffset(long offset) {
        sourceTextArea.hilighOffset(offset);
    }

    public void hilighSpecial(HighlightSpecialType type, String specialValue) {
        sourceTextArea.hilighSpecial(type, specialValue);
    }

    public void setBodyIndex(int bodyIndex, ABC abc, Trait trait, int scriptIndex) {
        sourceTextArea.setBodyIndex(bodyIndex, abc, sourceTextArea.getName(), trait,scriptIndex);
    }

    public void setBodyIndex(int bodyIndex, ABC abc, String name, Trait trait,int scriptIndex) {
        sourceTextArea.setBodyIndex(bodyIndex, abc, name, trait,scriptIndex);
    }
    
    

    public int getBodyIndex() {
        return sourceTextArea.bodyIndex;
    }

    public void clear() {
        sourceTextArea.clear();
    }

    public boolean save() {
        return sourceTextArea.save();
    }

    public MethodCodePanel(DecompiledEditorPane decompiledEditor) {
        sourceTextArea = new ASMSourceEditorPane(decompiledEditor);

        setLayout(new BorderLayout());
        add(new JScrollPane(sourceTextArea), BorderLayout.CENTER);
        sourceTextArea.setContentType("text/flasm3");
        sourceTextArea.setFont(new Font("Monospaced", Font.PLAIN, sourceTextArea.getFont().getSize()));

        buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));

        JButton graphButton = new JButton(View.getIcon("graph16"));
        graphButton.setActionCommand(ACTION_GRAPH);
        graphButton.addActionListener(this);
        graphButton.setToolTipText(AppStrings.translate("button.viewgraph"));
        graphButton.setMargin(new Insets(3, 3, 3, 3));

        hexButton = new JToggleButton(View.getIcon("hexas16"));
        hexButton.setActionCommand(ACTION_HEX);
        hexButton.addActionListener(this);
        hexButton.setToolTipText(AppStrings.translate("button.viewhex"));
        hexButton.setMargin(new Insets(3, 3, 3, 3));

        hexOnlyButton = new JToggleButton(View.getIcon("hex16"));
        hexOnlyButton.setActionCommand(ACTION_HEX_ONLY);
        hexOnlyButton.addActionListener(this);
        hexOnlyButton.setToolTipText(AppStrings.translate("button.viewhex"));
        hexOnlyButton.setMargin(new Insets(3, 3, 3, 3));

        buttonsPanel.add(graphButton);
        buttonsPanel.add(hexButton);
        buttonsPanel.add(hexOnlyButton);
        buttonsPanel.add(new JPanel());
        // buttonsPanel.add(saveButton);
        // buttonsPan.add(execButton);

        add(buttonsPanel, BorderLayout.NORTH);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (Main.isWorking()) {
            return;
        }

        switch (e.getActionCommand()) {
            case ACTION_GRAPH:
                sourceTextArea.graph();
                break;
            case ACTION_HEX:
            case ACTION_HEX_ONLY:
                if (e.getActionCommand().equals(ACTION_HEX)) {
                    hexOnlyButton.setSelected(false);
                } else {
                    hexButton.setSelected(false);
                }
                sourceTextArea.setHex(getExportMode(), false);
                break;
        }
    }

    private ScriptExportMode getExportMode() {
        ScriptExportMode exportMode = hexOnlyButton.isSelected() ? ScriptExportMode.HEX
                : (hexButton.isSelected() ? ScriptExportMode.PCODE_HEX : ScriptExportMode.PCODE);
        return exportMode;
    }

    public void setEditMode(boolean val) {
        ScriptExportMode exportMode = getExportMode();
        if (val) {
            sourceTextArea.setHex(exportMode == ScriptExportMode.HEX ? ScriptExportMode.HEX : ScriptExportMode.PCODE, false);
        } else {
            if (exportMode != ScriptExportMode.PCODE) {
                sourceTextArea.setHex(exportMode, false);
            }
        }

        sourceTextArea.setEditable(val);
        sourceTextArea.getCaret().setVisible(true);
        buttonsPanel.setVisible(!val);
    }
}
