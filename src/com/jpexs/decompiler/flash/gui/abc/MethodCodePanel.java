/*
 *  Copyright (C) 2010-2018 JPEXS
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
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.gui.AppStrings;
import com.jpexs.decompiler.flash.gui.DocsPanel;
import com.jpexs.decompiler.flash.gui.Main;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.gui.controls.JPersistentSplitPane;
import com.jpexs.decompiler.flash.gui.controls.NoneSelectedButtonGroup;
import com.jpexs.decompiler.flash.helpers.hilight.HighlightSpecialType;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;

/**
 *
 * @author JPEXS
 */
public class MethodCodePanel extends JPanel {

    private final ASMSourceEditorPane sourceTextArea;

    public JPanel buttonsPanel;

    private final JToggleButton hexButton;

    private final JToggleButton hexOnlyButton;

    private final DocsPanel docsPanel;

    public void refreshMarkers() {
        sourceTextArea.refreshMarkers();
    }

    public void clearDebuggerColors() {
        sourceTextArea.removeColorMarkerOnAllLines(DecompiledEditorPane.IP_MARKER);
    }

    public void gotoInstrLine(int line) {
        sourceTextArea.gotoInstrLine(line);
    }

    public void focusEditor() {
        sourceTextArea.requestFocusInWindow();
    }

    public int getScriptIndex() {
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

    public void setBodyIndex(String scriptPathName, int bodyIndex, ABC abc, Trait trait, int scriptIndex) {
        sourceTextArea.setBodyIndex(scriptPathName, bodyIndex, abc, sourceTextArea.getName(), trait, scriptIndex);
    }

    public void setBodyIndex(String scriptPathName, int bodyIndex, ABC abc, String name, Trait trait, int scriptIndex) {
        sourceTextArea.setBodyIndex(scriptPathName, bodyIndex, abc, name, trait, scriptIndex);
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

        docsPanel = new DocsPanel();
        sourceTextArea.addDocsListener(docsPanel);
        add(new JPersistentSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(sourceTextArea), new JScrollPane(docsPanel), Configuration.guiAvm2DocsSplitPaneDividerLocationPercent));
        sourceTextArea.changeContentType("text/flasm3");
        sourceTextArea.setFont(Configuration.getSourceFont());

        buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));

        JButton graphButton = new JButton(View.getIcon("graph16"));
        graphButton.addActionListener(this::graphButtonActionPerformed);
        graphButton.setToolTipText(AppStrings.translate("button.viewgraph"));
        graphButton.setMargin(new Insets(3, 3, 3, 3));

        hexButton = new JToggleButton(View.getIcon("hexas16"));
        hexButton.addActionListener(this::hexButtonActionPerformed);
        hexButton.setToolTipText(AppStrings.translate("button.viewhexpcode"));
        hexButton.setMargin(new Insets(3, 3, 3, 3));

        hexOnlyButton = new JToggleButton(View.getIcon("hex16"));
        hexOnlyButton.addActionListener(this::hexOnlyButtonActionPerformed);
        hexOnlyButton.setToolTipText(AppStrings.translate("button.viewhex"));
        hexOnlyButton.setMargin(new Insets(3, 3, 3, 3));

        NoneSelectedButtonGroup exportModeButtonGroup = new NoneSelectedButtonGroup();
        exportModeButtonGroup.add(hexButton);
        exportModeButtonGroup.add(hexOnlyButton);

        buttonsPanel.add(graphButton);
        buttonsPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonsPanel.add(hexButton);
        buttonsPanel.add(hexOnlyButton);
        buttonsPanel.add(new JPanel());

        add(buttonsPanel, BorderLayout.NORTH);

    }

    private void graphButtonActionPerformed(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }

        sourceTextArea.graph();
    }

    private void hexButtonActionPerformed(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }

        sourceTextArea.setHex(getExportMode(), false);
    }

    private void hexOnlyButtonActionPerformed(ActionEvent evt) {
        if (Main.isWorking()) {
            return;
        }

        sourceTextArea.setHex(getExportMode(), false);
    }

    public ASMSourceEditorPane getSourceTextArea() {
        return sourceTextArea;
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
        } else if (exportMode != ScriptExportMode.PCODE) {
            sourceTextArea.setHex(exportMode, false);
        }

        sourceTextArea.setEditable(val);
        sourceTextArea.getCaret().setVisible(true);
        buttonsPanel.setVisible(!val);
    }
}
