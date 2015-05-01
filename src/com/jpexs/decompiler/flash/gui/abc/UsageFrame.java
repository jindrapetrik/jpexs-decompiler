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
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.usages.InsideClassMultinameUsage;
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
        Multiname m = abc.constants.constant_multiname.get(multinameIndex);
        if (m.namespace_index > 0 && abc.constants.constant_namespace.get(m.namespace_index).kind != Namespace.KIND_PRIVATE) {
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
        setTitle((definitions ? translate("dialog.title.declaration") : translate("dialog.title")) + abc.constants.getMultiname(multinameIndex).getNameWithNamespace(abc.constants, false));
        View.centerScreen(this);
        View.setWindowIcon(this);
    }

    public static void gotoUsage(final ABCPanel abcPanel, final MultinameUsage usage) {
        if (usage instanceof InsideClassMultinameUsage) {
            final InsideClassMultinameUsage icu = (InsideClassMultinameUsage) usage;

            Runnable settrait = new Runnable() {

                @Override
                public void run() {
                    abcPanel.decompiledTextArea.removeScriptListener(this);
                    abcPanel.decompiledTextArea.setClassIndex(icu.classIndex);
                    if (usage instanceof TraitMultinameUsage) {
                        TraitMultinameUsage tmu = (TraitMultinameUsage) usage;
                        int traitIndex;
                        if (tmu.parentTraitIndex > -1) {
                            traitIndex = tmu.parentTraitIndex;
                        } else {
                            traitIndex = tmu.traitIndex;
                        }
                        if (!tmu.isStatic) {
                            traitIndex += abcPanel.abc.class_info.get(tmu.classIndex).static_traits.traits.size();
                        }
                        if (tmu instanceof MethodMultinameUsage) {
                            MethodMultinameUsage mmu = (MethodMultinameUsage) usage;
                            if (mmu.isInitializer == true) {
                                traitIndex = abcPanel.abc.class_info.get(mmu.classIndex).static_traits.traits.size() + abcPanel.abc.instance_info.get(mmu.classIndex).instance_traits.traits.size() + (mmu.isStatic ? 1 : 0);
                            }
                        }
                        abcPanel.decompiledTextArea.gotoTrait(traitIndex);
                    }
                }
            };

            if (abcPanel.decompiledTextArea.getClassIndex() == icu.classIndex && abcPanel.abc == icu.abc) {
                settrait.run();
            } else {
                abcPanel.decompiledTextArea.addScriptListener(settrait);
                abcPanel.hilightScript(abcPanel.getSwf(), abcPanel.abc.instance_info.get(icu.classIndex).getName(abcPanel.abc.constants).getNameWithNamespace(abcPanel.abc.constants, false));
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
