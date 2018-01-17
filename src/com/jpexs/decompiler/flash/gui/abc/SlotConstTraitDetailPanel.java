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
import com.jpexs.decompiler.flash.abc.avm2.parser.AVM2ParseException;
import com.jpexs.decompiler.flash.abc.avm2.parser.pcode.ASM3Parser;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.gui.AppStrings;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.gui.editor.LineMarkedEditorPane;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.helpers.hilight.Highlighting;
import com.jpexs.decompiler.flash.tags.Tag;
import java.awt.BorderLayout;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

/**
 *
 * @author JPEXS
 */
public class SlotConstTraitDetailPanel extends JPanel implements TraitDetail {

    public LineMarkedEditorPane slotConstEditor;

    private ABC abc;

    private TraitSlotConst trait;

    private boolean showWarning = false;

    private List<Highlighting> specialHilights;

    private boolean ignoreCaret = false;

    public SlotConstTraitDetailPanel(final DecompiledEditorPane editor) {
        slotConstEditor = new LineMarkedEditorPane();
        setLayout(new BorderLayout());
        add(new JScrollPane(slotConstEditor), BorderLayout.CENTER);
        slotConstEditor.setFont(Configuration.getSourceFont());
        slotConstEditor.changeContentType("text/flasm3");
        slotConstEditor.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent e) {
                if (ignoreCaret) {
                    return;
                }
                Highlighting spec = Highlighting.searchPos(specialHilights, slotConstEditor.getCaretPosition());
                if (spec != null) {
                    editor.hilightSpecial(spec.getProperties().subtype, (int) spec.getProperties().index);
                    slotConstEditor.getCaret().setVisible(true);
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
            slotConstEditor.setCaretPosition(sel.startPos);
            slotConstEditor.getCaret().setVisible(true);
            ignoreCaret = false;
        }
    }

    public void load(TraitSlotConst trait, ABC abc, boolean isStatic) {
        this.abc = abc;
        this.trait = trait;
        HighlightedTextWriter writer = new HighlightedTextWriter(Configuration.getCodeFormatting(), true);
        trait.convertTraitHeader(abc, writer);
        writer.appendNoHilight("end ; trait");
        String s = writer.toString();
        specialHilights = writer.specialHilights;
        showWarning = trait.isConst() || isStatic;
        slotConstEditor.setText(s);
    }

    @Override
    public boolean save() {
        try {//(slotConstEditor.getText(), trait, abc)
            if (!ASM3Parser.parseSlotConst(abc, new StringReader(slotConstEditor.getText()), abc.constants, trait)) {
                return false;
            }
        } catch (AVM2ParseException ex) {
            View.showMessageDialog(slotConstEditor, ex.text, AppStrings.translate("error.slotconst.typevalue"), JOptionPane.ERROR_MESSAGE);
            return false;
        } catch (IOException ex) {
            Logger.getLogger(SlotConstTraitDetailPanel.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        abc.refreshMultinameNamespaceSuffixes();
        ((Tag) abc.parentTag).setModified(true);
        return true;
    }

    @Override
    public void setEditMode(boolean val) {
        if (val && active) {
            JOptionPane.showMessageDialog(null, AppStrings.translate("warning.initializers"), AppStrings.translate("message.warning"), JOptionPane.WARNING_MESSAGE);
        }
        slotConstEditor.setEditable(val);
    }

    private boolean active = false;

    @Override
    public void setActive(boolean val) {
        this.active = val;
    }
}
