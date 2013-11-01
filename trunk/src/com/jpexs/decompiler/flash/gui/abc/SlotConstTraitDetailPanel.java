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

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.parser.ASM3Parser;
import com.jpexs.decompiler.flash.abc.avm2.parser.ParseException;
import com.jpexs.decompiler.flash.abc.types.ValueKind;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import static com.jpexs.decompiler.flash.gui.AppStrings.translate;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.helpers.HilightedTextWriter;
import com.jpexs.decompiler.flash.helpers.hilight.Highlighting;
import java.awt.BorderLayout;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

/**
 *
 * @author JPEXS
 */
public class SlotConstTraitDetailPanel extends JPanel implements TraitDetail {

    public JEditorPane slotConstEditor;
    private ABC abc;
    private TraitSlotConst trait;
    private boolean showWarning = false;
    private List<Highlighting> specialHilights;
    private boolean ignoreCaret = false;

    public SlotConstTraitDetailPanel(final DecompiledEditorPane editor) {
        slotConstEditor = new LineMarkedEditorPane();
        setLayout(new BorderLayout());
        add(new JScrollPane(slotConstEditor), BorderLayout.CENTER);
        slotConstEditor.setContentType("text/flasm3");
        slotConstEditor.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent e) {
                if (ignoreCaret) {
                    return;
                }
                Highlighting spec = Highlighting.search(specialHilights, slotConstEditor.getCaretPosition());
                if (spec != null) {
                    editor.hilightSpecial(spec.getPropertyString("subtype"), (int) (long) spec.getPropertyLong("index"));
                    slotConstEditor.getCaret().setVisible(true);
                }
            }
        });
    }

    public void hilightSpecial(Highlighting special) {
        Highlighting sel = null;
        for (Highlighting h : specialHilights) {
            if (h.getPropertyString("subtype").equals(special.getPropertyString("subtype"))) {
                if (h.getPropertyString("index").equals(special.getPropertyString("index"))) {
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
        HilightedTextWriter writer = new HilightedTextWriter(true);
        writer.appendNoHilight("trait ");
        writer.hilightSpecial(abc.constants.multinameToString(trait.name_index), "traitname");
        writer.appendNoHilight(" ");
        writer.hilightSpecial(trait.isConst() ? "const" : "slot", "traittype");
        writer.appendNoHilight(" slotid ");
        writer.hilightSpecial("" + trait.slot_id, "slotid");
        writer.appendNoHilight(" type ");
        writer.hilightSpecial(abc.constants.multinameToString(trait.type_index), "traittypename");
        writer.appendNoHilight(" value ");
        writer.hilightSpecial((new ValueKind(trait.value_index, trait.value_kind).toASMString(abc.constants)), "traitvalue");
        String s = writer.toString();
        specialHilights = writer.specialHilights;
        showWarning = trait.isConst() || isStatic;
        slotConstEditor.setText(s);
    }

    @Override
    public boolean save() {
        try {//(slotConstEditor.getText(), trait, abc)
            if (!ASM3Parser.parseSlotConst(new ByteArrayInputStream(slotConstEditor.getText().getBytes("UTF-8")), abc.constants, trait)) {
                return false;
            }
        } catch (ParseException ex) {
            View.showMessageDialog(slotConstEditor, ex.text, translate("error.slotconst.typevalue"), JOptionPane.ERROR_MESSAGE);
            return false;
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(SlotConstTraitDetailPanel.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (IOException ex) {
            Logger.getLogger(SlotConstTraitDetailPanel.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    @Override
    public void setEditMode(boolean val) {
        if (val && active) {
            JOptionPane.showMessageDialog(null, translate("warning.initializers"), translate("message.warning"), JOptionPane.WARNING_MESSAGE);
        }
        slotConstEditor.setEditable(val);
    }
    private boolean active = false;

    @Override
    public void setActive(boolean val) {
        this.active = val;
    }
}
