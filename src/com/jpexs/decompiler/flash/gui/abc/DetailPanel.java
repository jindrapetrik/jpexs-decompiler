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

import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.gui.AppStrings;
import com.jpexs.decompiler.flash.gui.HeaderLabel;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.helpers.CancellableWorker;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

/**
 *
 * @author JPEXS
 */
public class DetailPanel extends JPanel implements ActionListener {

    private static final String ACTION_SAVE_DETAIL = "SAVEDETAIL";

    private static final String ACTION_EDIT_DETAIL = "EDITDETAIL";

    private static final String ACTION_CANCEL_DETAIL = "CANCELDETAIL";

    public MethodTraitDetailPanel methodTraitPanel;

    public JPanel unsupportedTraitPanel;

    public SlotConstTraitDetailPanel slotConstTraitPanel;

    public static final String METHOD_TRAIT_CARD = AppStrings.translate("abc.detail.methodtrait");

    public static final String UNSUPPORTED_TRAIT_CARD = AppStrings.translate("abc.detail.unsupported");

    public static final String SLOT_CONST_TRAIT_CARD = AppStrings.translate("abc.detail.slotconsttrait");

    private final JPanel innerPanel;

    public JButton saveButton = new JButton(AppStrings.translate("button.save"), View.getIcon("save16"));

    public JButton editButton = new JButton(AppStrings.translate("button.edit"), View.getIcon("edit16"));

    public JButton cancelButton = new JButton(AppStrings.translate("button.cancel"), View.getIcon("cancel16"));

    private final HashMap<String, JComponent> cardMap = new HashMap<>();

    private String selectedCard;

    private final JLabel selectedLabel;

    private boolean editMode = false;

    private final JPanel buttonsPanel;

    private final ABCPanel abcPanel;

    private final JLabel traitNameLabel;

    public DetailPanel(ABCPanel abcPanel) {
        this.abcPanel = abcPanel;
        innerPanel = new JPanel();
        CardLayout layout = new CardLayout();
        innerPanel.setLayout(layout);
        methodTraitPanel = new MethodTraitDetailPanel(abcPanel);
        cardMap.put(METHOD_TRAIT_CARD, methodTraitPanel);

        unsupportedTraitPanel = new JPanel(new BorderLayout());
        JLabel unsup = new JLabel("<html>" + AppStrings.translate("info.selecttrait") + "</html>", SwingConstants.CENTER);
        unsupportedTraitPanel.add(unsup, BorderLayout.CENTER);

        cardMap.put(UNSUPPORTED_TRAIT_CARD, unsupportedTraitPanel);

        slotConstTraitPanel = new SlotConstTraitDetailPanel(abcPanel.decompiledTextArea);
        cardMap.put(SLOT_CONST_TRAIT_CARD, slotConstTraitPanel);

        for (String key : cardMap.keySet()) {
            innerPanel.add(cardMap.get(key), key);
        }

        setLayout(new BorderLayout());
        add(innerPanel, BorderLayout.CENTER);

        editButton.setMargin(new Insets(3, 3, 3, 10));
        saveButton.setMargin(new Insets(3, 3, 3, 10));
        cancelButton.setMargin(new Insets(3, 3, 3, 10));

        buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout());
        saveButton.setActionCommand(ACTION_SAVE_DETAIL);
        saveButton.addActionListener(this);
        editButton.setActionCommand(ACTION_EDIT_DETAIL);
        editButton.addActionListener(this);
        cancelButton.setActionCommand(ACTION_CANCEL_DETAIL);
        cancelButton.addActionListener(this);
        buttonsPanel.setBorder(new BevelBorder(BevelBorder.RAISED));
        buttonsPanel.add(editButton);
        buttonsPanel.add(saveButton);
        buttonsPanel.add(cancelButton);
        add(buttonsPanel, BorderLayout.SOUTH);
        selectedCard = UNSUPPORTED_TRAIT_CARD;
        layout.show(innerPanel, UNSUPPORTED_TRAIT_CARD);
        buttonsPanel.setVisible(false);
        selectedLabel = new HeaderLabel("");
        selectedLabel.setText(selectedCard);
        //selectedLabel.setBorder(new BevelBorder(BevelBorder.RAISED));
        selectedLabel.setHorizontalAlignment(SwingConstants.CENTER);
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(selectedLabel, BorderLayout.NORTH);
        traitNameLabel = new JLabel("");
        JPanel traitInfoPanel = new JPanel();
        traitInfoPanel.setLayout(new BoxLayout(traitInfoPanel, BoxLayout.LINE_AXIS));
        //traitInfoPanel.add(new JLabel("  " + translate("abc.detail.traitname")));
        traitInfoPanel.add(traitNameLabel);
        topPanel.add(traitInfoPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);
    }

    public void setEditMode(boolean val) {
        slotConstTraitPanel.setEditMode(val);
        methodTraitPanel.setEditMode(val);
        saveButton.setVisible(val);
        editButton.setVisible(!val);
        cancelButton.setVisible(val);
        editMode = val;
        if (val) {
            selectedLabel.setIcon(View.getIcon("editing16"));
        } else {
            selectedLabel.setIcon(null);
        }
    }

    public void showCard(final String name, final Trait trait) {
        View.execInEventDispatch(new Runnable() {

            @Override
            public void run() {
                CardLayout layout = (CardLayout) innerPanel.getLayout();
                layout.show(innerPanel, name);
                boolean b = cardMap.get(name) instanceof TraitDetail;
                buttonsPanel.setVisible(b);

                TraitDetail newDetail = null;
                if (b) {
                    newDetail = (TraitDetail) cardMap.get(name);
                }
                for (JComponent v : cardMap.values()) {
                    if (v instanceof TraitDetail) {
                        if (v != newDetail) {
                            TraitDetail oldDetail = (TraitDetail) v;
                            oldDetail.setActive(false);
                        }
                    }
                }
                if (newDetail != null) {
                    newDetail.setActive(true);
                }

                selectedCard = name;
                selectedLabel.setText(selectedCard);
                if (trait == null) {
                    traitNameLabel.setText("-");
                } else {
                    if (abcPanel != null) {
                        traitNameLabel.setText(trait.getName(abcPanel.abc).getName(abcPanel.abc.constants, new ArrayList<String>(), false));
                    }
                }
            }
        });

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case ACTION_EDIT_DETAIL:
                setEditMode(true);
                methodTraitPanel.methodCodePanel.focusEditor();
                break;
            case ACTION_CANCEL_DETAIL:
                setEditMode(false);
                abcPanel.decompiledTextArea.resetEditing();
                break;
            case ACTION_SAVE_DETAIL:
                if (cardMap.get(selectedCard) instanceof TraitDetail) {
                    if (((TraitDetail) cardMap.get(selectedCard)).save()) {
                        CancellableWorker worker = new CancellableWorker() {

                            @Override
                            public Void doInBackground() throws Exception {
                                int lasttrait = abcPanel.decompiledTextArea.lastTraitIndex;
                                abcPanel.decompiledTextArea.reloadClass();
                                abcPanel.decompiledTextArea.gotoTrait(lasttrait);
                                return null;
                            }

                            @Override
                            protected void done() {
                                setEditMode(false);
                                View.showMessageDialog(null, AppStrings.translate("message.trait.saved"));
                            }
                        };
                        worker.execute();
                    }
                }
                break;
        }
    }
}
