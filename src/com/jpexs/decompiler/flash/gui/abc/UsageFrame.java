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
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.usages.InsideClassMultinameUsageInterface;
import com.jpexs.decompiler.flash.abc.usages.MethodMultinameUsage;
import com.jpexs.decompiler.flash.abc.usages.MultinameUsage;
import com.jpexs.decompiler.flash.abc.usages.TraitMultinameUsage;
import com.jpexs.decompiler.flash.gui.AppDialog;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author JPEXS
 */
public class UsageFrame extends AppDialog implements MouseListener {

    private final JButton gotoButton = new JButton(translate("button.goto"));

    private final JButton cancelButton = new JButton(translate("button.cancel"));

    private final JList usageList;

    private final UsageListModel usageListModel;

    private final ABCPanel abcPanel;

    public UsageFrame(ABC abc, int multinameIndex, ABCPanel abcPanel, boolean definitions) {
        super(abcPanel.getMainPanel().getMainFrame().getWindow());
        this.abcPanel = abcPanel;
        List<MultinameUsage> usages = definitions ? abc.findMultinameDefinition(multinameIndex) : abc.findMultinameUsage(multinameIndex);
        Multiname m = abc.constants.getMultiname(multinameIndex);
        if (m.namespace_index > 0 && abc.constants.getNamespace(m.namespace_index).kind != Namespace.KIND_PRIVATE) {
            for (ABCContainerTag at : abc.getAbcTags()) {
                ABC a = at.getABC();
                if (a == abc) {
                    continue;
                }
                int mid = a.constants.getMultinameId(m, false);
                if (mid > 0) {
                    usages.addAll(definitions ? a.findMultinameDefinition(mid) : a.findMultinameUsage(mid));
                }
            }
        }
        usageListModel = new UsageListModel();
        for (MultinameUsage u : usages) {
            usageListModel.addElement(u);
        }
        usageList = new JList<>(usageListModel);
        usageList.setBackground(Color.white);
        gotoButton.addActionListener(this::gotoButtonActionPerformed);
        cancelButton.addActionListener(this::cancelButtonActionPerformed);

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout());
        buttonsPanel.add(gotoButton);
        buttonsPanel.add(cancelButton);

        usageList.addMouseListener(this);
        Container cont = getContentPane();
        cont.setLayout(new BorderLayout());
        cont.add(new JScrollPane(usageList), BorderLayout.CENTER);
        cont.add(buttonsPanel, BorderLayout.SOUTH);
        setSize(400, 300);
        setTitle((definitions ? translate("dialog.title.declaration") : translate("dialog.title")) + abc.constants.getMultiname(multinameIndex).getNameWithNamespace(abc.constants, true).toPrintableString(true));
        View.centerScreen(this);
        View.setWindowIcon(this);
    }

    public static void gotoUsage(final ABCPanel abcPanel, final MultinameUsage usage) {
        View.checkAccess();

        if (usage instanceof InsideClassMultinameUsageInterface) {
            final InsideClassMultinameUsageInterface icu = (InsideClassMultinameUsageInterface) usage;

            DecompiledEditorPane decompiledTextArea = abcPanel.decompiledTextArea;
            ABC abc = abcPanel.abc;
            Runnable setTrait = new Runnable() {
                @Override
                public void run() {
                    decompiledTextArea.removeScriptListener(this);
                    decompiledTextArea.setClassIndex(icu.getClassIndex());
                    if (usage instanceof TraitMultinameUsage) {
                        TraitMultinameUsage tmu = (TraitMultinameUsage) usage;
                        int traitIndex;
                        if (tmu.getParentTraitIndex() > -1) {
                            traitIndex = tmu.getParentTraitIndex();
                        } else {
                            traitIndex = tmu.getTraitIndex();
                        }
                        if (tmu.getTraitsType() == TraitMultinameUsage.TRAITS_TYPE_INSTANCE) {
                            traitIndex += abc.class_info.get(tmu.getClassIndex()).static_traits.traits.size();
                        }
                        if (tmu instanceof MethodMultinameUsage) {
                            MethodMultinameUsage mmu = (MethodMultinameUsage) usage;
                            if (mmu.isInitializer() == true) {
                                traitIndex = abc.class_info.get(mmu.getClassIndex()).static_traits.traits.size() + abc.instance_info.get(mmu.getClassIndex()).instance_traits.traits.size() + (mmu.getTraitsType() == TraitMultinameUsage.TRAITS_TYPE_CLASS ? 1 : 0);
                            }
                        }
                        decompiledTextArea.gotoTrait(traitIndex);
                    }
                }
            };

            if (decompiledTextArea.getClassIndex() == icu.getClassIndex() && abc == icu.getAbc()) {
                setTrait.run();
            } else {
                decompiledTextArea.addScriptListener(setTrait);
                abcPanel.hilightScript(abcPanel.getSwf(), icu.getAbc().instance_info.get(icu.getClassIndex()).getName(icu.getAbc().constants).getNameWithNamespace(icu.getAbc().constants, true).toRawString());
            }
        }
    }

    private void gotoUsage() {
        if (usageList.getSelectedIndex() != -1) {
            MultinameUsage usage = usageListModel.getUsage(usageList.getSelectedIndex());
            gotoUsage(abcPanel, usage);
        }
    }

    private void gotoButtonActionPerformed(ActionEvent evt) {
        gotoUsage();
        setVisible(false);
    }

    private void cancelButtonActionPerformed(ActionEvent evt) {
        setVisible(false);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            gotoUsage();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}
