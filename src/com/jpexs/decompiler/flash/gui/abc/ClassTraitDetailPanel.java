/*
 *  Copyright (C) 2010-2025 JPEXS
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
import com.jpexs.decompiler.flash.abc.avm2.parser.AVM2ParseException;
import com.jpexs.decompiler.flash.abc.avm2.parser.pcode.ASM3Parser;
import com.jpexs.decompiler.flash.abc.types.traits.TraitClass;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.gui.AppStrings;
import com.jpexs.decompiler.flash.gui.FasterScrollPane;
import com.jpexs.decompiler.flash.gui.ViewMessages;
import com.jpexs.decompiler.flash.gui.editor.LineMarkedEditorPane;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.helpers.hilight.Highlighting;
import com.jpexs.decompiler.flash.helpers.hilight.HighlightingList;
import com.jpexs.decompiler.flash.tags.Tag;
import java.awt.BorderLayout;
import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

/**
 * @author JPEXS
 */
public class ClassTraitDetailPanel extends JPanel implements TraitDetail {

    public LineMarkedEditorPane classEditor;

    private ABC abc;

    private TraitClass trait;

    private HighlightingList specialHilights;

    private boolean ignoreCaret = false;

    public ClassTraitDetailPanel(final DecompiledEditorPane editor) {
        classEditor = new LineMarkedEditorPane();
        setLayout(new BorderLayout());
        add(new FasterScrollPane(classEditor), BorderLayout.CENTER);
        classEditor.setFont(Configuration.getSourceFont());
        classEditor.changeContentType("text/flasm3");
        classEditor.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent e) {
                if (ignoreCaret) {
                    return;
                }
                Highlighting spec = Highlighting.searchPos(specialHilights, classEditor.getCaretPosition());
                if (spec != null) {
                    editor.hilightSpecial(spec.getProperties().subtype, (int) spec.getProperties().index);
                    classEditor.getCaret().setVisible(true);
                }
            }
        });
    }

    public void hilightSpecial(Highlighting special) {
        Highlighting sel = null;
        for (Highlighting h : specialHilights) {
            if (h.getProperties().subtype.equals(special.getProperties().subtype)) {
                if (h.getProperties().index == special.getProperties().index) {
                    sel = h;
                    break;
                }
            }
        }
        if (sel != null) {
            ignoreCaret = true;
            classEditor.setCaretPosition(sel.startPos);
            classEditor.getCaret().setVisible(true);
            ignoreCaret = false;
        }
    }

    public void load(TraitClass trait, ABC abc, boolean isStatic) {
        this.abc = abc;
        this.trait = trait;
        HighlightedTextWriter writer = new HighlightedTextWriter(Configuration.getCodeFormatting(), true);
        trait.convertTraitHeader(abc, writer);
        if (Configuration.indentAs3PCode.get()) {
            writer.unindent();
        }
        writer.appendNoHilight("end ; trait");
        writer.finishHilights();
        String s = writer.toString();
        specialHilights = writer.specialHilights;
        classEditor.setText(s);
    }

    @Override
    public boolean save() {
        try {
            if (!ASM3Parser.parseClass(abc, new StringReader(classEditor.getText()), abc.constants, trait)) {
                return false;
            }
        } catch (AVM2ParseException ex) {
            ViewMessages.showMessageDialog(classEditor, ex.text, AppStrings.translate("error.class"), JOptionPane.ERROR_MESSAGE);
            return false;
        } catch (IOException ex) {
            Logger.getLogger(ClassTraitDetailPanel.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        abc.refreshMultinameNamespaceSuffixes();
        ((Tag) abc.parentTag).setModified(true);
        abc.fireChanged();
        return true;
    }

    @Override
    public void setEditMode(boolean val) {
        if (val && active) {
            ViewMessages.showMessageDialog(this, AppStrings.translate("warning.initializers.class"), AppStrings.translate("message.warning"), JOptionPane.WARNING_MESSAGE, Configuration.warningInitializersClass);
        }
        classEditor.setEditable(val);
        if (val) {
            classEditor.requestFocusInWindow();
        }
    }

    private boolean active = false;

    @Override
    public void setActive(boolean val) {
        this.active = val;
    }
}
