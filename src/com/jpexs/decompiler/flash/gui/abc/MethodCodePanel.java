/*
 *  Copyright (C) 2010-2024 JPEXS
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
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.gui.AppStrings;
import com.jpexs.decompiler.flash.gui.DocsPanel;
import com.jpexs.decompiler.flash.gui.FasterScrollPane;
import com.jpexs.decompiler.flash.gui.Main;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.gui.ViewMessages;
import com.jpexs.decompiler.flash.gui.controls.JPersistentSplitPane;
import com.jpexs.decompiler.flash.gui.controls.NoneSelectedButtonGroup;
import com.jpexs.decompiler.flash.helpers.hilight.HighlightSpecialType;
import com.jpexs.decompiler.flash.tags.Tag;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;

/**
 * @author JPEXS
 */
public class MethodCodePanel extends JPanel {

    private final ASMSourceEditorPane sourceTextArea;

    private final FasterScrollPane sourceScrollPane;

    public JPanel detailButtonsPanel;

    private final JToggleButton hexButton;

    private final JToggleButton hexOnlyButton;

    private final JButton addFunctionButton;

    private final DocsPanel docsPanel;

    public ABC getABC() {
        return sourceTextArea.abc;
    }

    public FasterScrollPane getSourceScrollPane() {
        return sourceScrollPane;
    }

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

    public void setIgnoreCaret(boolean ignoreCaret) {
        sourceTextArea.setIgnoreCaret(ignoreCaret);
    }

    public void hilightOffset(long offset) {
        sourceTextArea.hilightOffset(offset);
    }

    public void hilighSpecial(HighlightSpecialType type, String specialValue) {
        sourceTextArea.hilighSpecial(type, specialValue);
    }

    public void setMethod(String scriptPathName, int methodIndex, int bodyIndex, ABC abc, Trait trait, int scriptIndex) {
        sourceTextArea.setMethod(scriptPathName, methodIndex, bodyIndex, abc, sourceTextArea.getName(), trait, scriptIndex);
    }

    public void setMethod(String scriptPathName, int methodIndex, int bodyIndex, ABC abc, String name, Trait trait, int scriptIndex) {
        sourceTextArea.setMethod(scriptPathName, methodIndex, bodyIndex, abc, name, trait, scriptIndex);
    }

    public int getBodyIndex() {
        return sourceTextArea.bodyIndex;
    }

    public int getMethodIndex() {
        return sourceTextArea.methodIndex;
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
        if (Configuration.displayAs3PCodeDocsPanel.get()) {
            add(new JPersistentSplitPane(JSplitPane.VERTICAL_SPLIT, sourceScrollPane = new FasterScrollPane(sourceTextArea), new FasterScrollPane(docsPanel), Configuration.guiAvm2DocsSplitPaneDividerLocationPercent));
        } else {
            add(sourceScrollPane = new FasterScrollPane(sourceTextArea));
        }
        sourceTextArea.changeContentType("text/flasm3");
        sourceTextArea.setFont(Configuration.getSourceFont());

        detailButtonsPanel = new JPanel();
        detailButtonsPanel.setLayout(new BoxLayout(detailButtonsPanel, BoxLayout.X_AXIS));

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

        addFunctionButton = new JButton(View.getIcon("addfunction16"));
        addFunctionButton.addActionListener(this::addFunctionButtonActionPerformed);
        addFunctionButton.setToolTipText(AppStrings.translate("button.addfunction"));
        addFunctionButton.setMargin(new Insets(3, 3, 3, 3));

        NoneSelectedButtonGroup exportModeButtonGroup = new NoneSelectedButtonGroup();
        exportModeButtonGroup.add(hexButton);
        exportModeButtonGroup.add(hexOnlyButton);

        detailButtonsPanel.add(graphButton);
        detailButtonsPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        detailButtonsPanel.add(hexButton);
        detailButtonsPanel.add(hexOnlyButton);

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
        JPanel editVisibleButtonsPanel = new JPanel();
        editVisibleButtonsPanel.setLayout(new BoxLayout(editVisibleButtonsPanel, BoxLayout.X_AXIS));
        editVisibleButtonsPanel.add(addFunctionButton);

        buttonsPanel.add(detailButtonsPanel);
        buttonsPanel.add(Box.createHorizontalStrut(5));
        buttonsPanel.add(editVisibleButtonsPanel);
        buttonsPanel.add(Box.createHorizontalGlue());
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

    private void addFunctionButtonActionPerformed(ActionEvent evt) {
        if (ViewMessages.showConfirmDialog(this, AppStrings.translate("message.confirm.addfunction"), AppStrings.translate("message.warning"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, Configuration.warningAddFunction, JOptionPane.OK_OPTION) != JOptionPane.OK_OPTION) {
            return;
        }
        MethodInfo methodInfoObj = new MethodInfo(new int[0], 0, 0, 0, null, null);
        ABC abc = getABC();
        int methodInfo = abc.addMethodInfo(methodInfoObj);
        MethodBody body = new MethodBody();
        List<AVM2Instruction> code = body.getCode().code;
        code.add(new AVM2Instruction(0, AVM2Instructions.ReturnVoid, new int[0]));
        body.method_info = methodInfo;
        abc.addMethodBody(body);
        ((Tag) abc.parentTag).setModified(true);
        if (ViewMessages.showConfirmDialog(this, AppStrings.translate("addfunction.result").replace("%method_info_index%", "" + methodInfo), AppStrings.translate("addfunction.result.title"), JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
            return;
        }
        StringSelection stringSelection = new StringSelection("" + methodInfo);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
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
        detailButtonsPanel.setVisible(!val);
    }
}
