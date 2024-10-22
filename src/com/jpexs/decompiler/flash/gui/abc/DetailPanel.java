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
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.gui.AppStrings;
import com.jpexs.decompiler.flash.gui.DebuggerHandler;
import com.jpexs.decompiler.flash.gui.HeaderLabel;
import com.jpexs.decompiler.flash.gui.Main;
import com.jpexs.decompiler.flash.gui.MainPanel;
import com.jpexs.decompiler.flash.gui.TagEditorPanel;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.gui.ViewMessages;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

/**
 * @author JPEXS
 */
public class DetailPanel extends JPanel implements TagEditorPanel {

    public MethodTraitDetailPanel methodTraitPanel;

    public JPanel unsupportedTraitPanel;

    public SlotConstTraitDetailPanel slotConstTraitPanel;

    public ClassTraitDetailPanel classTraitPanel;

    public static final String METHOD_GETTER_SETTER_TRAIT_CARD = "abc.detail.methodtrait";

    public static final String UNSUPPORTED_TRAIT_CARD = "abc.detail.unsupported";

    public static final String SLOT_CONST_TRAIT_CARD = "abc.detail.slotconsttrait";

    public static final String CLASS_TRAIT_CARD = "abc.detail.classtrait";

    private final JPanel innerPanel;

    public JButton saveButton = new JButton(AppStrings.translate("button.save"), View.getIcon("save16"));

    public JButton editButton = new JButton(AppStrings.translate("button.edit.script.disassembled"), View.getIcon("edit16"));

    public JButton cancelButton = new JButton(AppStrings.translate("button.cancel"), View.getIcon("cancel16"));

    private final HashMap<String, JComponent> cardMap = new HashMap<>();

    private String selectedCard;

    private final JLabel selectedLabel;

    private final JPanel buttonsPanel;

    private final ABCPanel abcPanel;

    private final JLabel traitNameLabel;

    private boolean debugRunning = false;

    private boolean buttonsShouldBeShown = false;

    private final DebuggerHandler.ConnectionListener conListener;

    private MainPanel mainPanel;

    public DetailPanel(ABCPanel abcPanel, MainPanel mainPanel) {
        this.abcPanel = abcPanel;
        this.mainPanel = mainPanel;
        innerPanel = new JPanel();
        CardLayout layout = new CardLayout();
        innerPanel.setLayout(layout);
        methodTraitPanel = new MethodTraitDetailPanel(abcPanel);
        cardMap.put(METHOD_GETTER_SETTER_TRAIT_CARD, methodTraitPanel);

        unsupportedTraitPanel = new JPanel(new BorderLayout());
        JLabel unsup = new JLabel("<html>" + AppStrings.translate("info.selecttrait") + "</html>", SwingConstants.CENTER);
        unsupportedTraitPanel.add(unsup, BorderLayout.CENTER);

        cardMap.put(UNSUPPORTED_TRAIT_CARD, unsupportedTraitPanel);

        slotConstTraitPanel = new SlotConstTraitDetailPanel(abcPanel.decompiledTextArea);
        cardMap.put(SLOT_CONST_TRAIT_CARD, slotConstTraitPanel);

        classTraitPanel = new ClassTraitDetailPanel(abcPanel.decompiledTextArea);
        cardMap.put(CLASS_TRAIT_CARD, classTraitPanel);

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
        saveButton.addActionListener(this::saveButtonActionPerformed);
        editButton.addActionListener(this::editButtonActionPerformed);
        cancelButton.addActionListener(this::cancelButtonActionPerformed);

        saveButton.setVisible(false);
        cancelButton.setVisible(false);

        buttonsPanel.setBorder(new BevelBorder(BevelBorder.RAISED));
        buttonsPanel.add(editButton);
        buttonsPanel.add(saveButton);
        buttonsPanel.add(cancelButton);
        add(buttonsPanel, BorderLayout.SOUTH);
        selectedCard = UNSUPPORTED_TRAIT_CARD;
        layout.show(innerPanel, UNSUPPORTED_TRAIT_CARD);
        buttonsPanel.setVisible(false);

        conListener = new DebuggerHandler.ConnectionListener() {
            @Override
            public void connected() {
                synchronized (DetailPanel.this) {
                    debugRunning = true;
                    if (buttonsPanel != null) {
                        buttonsPanel.setVisible(false);
                    }
                }
            }

            @Override
            public void disconnected() {
                synchronized (DetailPanel.this) {
                    debugRunning = false;

                    if (buttonsPanel != null) {
                        buttonsPanel.setVisible(buttonsShouldBeShown);
                    }
                }
            }
        };
        Main.getDebugHandler().addConnectionListener(conListener);

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
        methodTraitPanel.methodCodePanel.getSourceTextArea().addTextChangedListener(this::editorTextChanged);
        slotConstTraitPanel.slotConstEditor.addTextChangedListener(this::editorTextChanged);
        classTraitPanel.classEditor.addTextChangedListener(this::editorTextChanged);
        add(topPanel, BorderLayout.NORTH);
    }

    private void editorTextChanged() {
        setModified(true);
    }

    private boolean isModified() {
        return saveButton.isVisible() && saveButton.isEnabled();
    }

    private void setModified(boolean value) {
        saveButton.setEnabled(value);
        cancelButton.setEnabled(value);
    }

    public void setEditMode(boolean val) {
        slotConstTraitPanel.setEditMode(val);
        methodTraitPanel.setEditMode(val);
        classTraitPanel.setEditMode(val);
        saveButton.setVisible(val);
        saveButton.setEnabled(false);
        editButton.setVisible(!val);
        cancelButton.setVisible(val);
        selectedLabel.setIcon(val ? View.getIcon("editing16") : null);
    }

    public void showCard(final String name, final Trait trait, int traitIndex, ABC abc) {
        View.execInEventDispatch(() -> {
            CardLayout layout = (CardLayout) innerPanel.getLayout();
            layout.show(innerPanel, name);
            boolean b = cardMap.get(name) instanceof TraitDetail;
            boolean drun;
            synchronized (this) {
                buttonsShouldBeShown = b;
                drun = debugRunning;
            }
            buttonsPanel.setVisible(b && !drun);

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
            String detailTitleStr = AppStrings.translate("panel.disassembled") + AppStrings.translate("abc.detail.split") + "%pcode_type%";
            String traitStr = AppStrings.translate("abc.detail.trait");
            String pcodeTypeStr = null;

            if (trait != null) {
                String traitTypeLang;
                switch (trait.kindType) {
                    case Trait.TRAIT_CLASS:
                        traitTypeLang = "abc.detail.trait.class";
                        break;
                    case Trait.TRAIT_CONST:
                        traitTypeLang = "abc.detail.trait.const";
                        break;
                    case Trait.TRAIT_FUNCTION:
                        traitTypeLang = "abc.detail.trait.function";
                        break;
                    case Trait.TRAIT_GETTER:
                        traitTypeLang = "abc.detail.trait.getter";
                        break;
                    case Trait.TRAIT_METHOD:
                        traitTypeLang = "abc.detail.trait.method";
                        break;
                    case Trait.TRAIT_SETTER:
                        traitTypeLang = "abc.detail.trait.setter";
                        break;
                    case Trait.TRAIT_SLOT:
                        traitTypeLang = "abc.detail.trait.slot";
                        break;
                    default:
                        traitTypeLang = "abc.detail.unsupported";
                }
                traitStr = traitStr.replace("%trait_type%", AppStrings.translate(traitTypeLang));
                pcodeTypeStr = traitStr;
            }
            String specialMethodTypeLang = null;
            switch (traitIndex) {
                case GraphTextWriter.TRAIT_SCRIPT_INITIALIZER:
                    specialMethodTypeLang = "abc.detail.specialmethod.scriptinitializer";
                    break;
                case GraphTextWriter.TRAIT_CLASS_INITIALIZER:
                    specialMethodTypeLang = "abc.detail.specialmethod.classinitializer";
                    break;
                case GraphTextWriter.TRAIT_INSTANCE_INITIALIZER:
                    specialMethodTypeLang = "abc.detail.specialmethod.instanceinitializer";
                    break;
            }
            if (specialMethodTypeLang != null) {
                String specialMethodStr = AppStrings.translate("abc.detail.specialmethod").replace("%specialmethod_type%", AppStrings.translate(specialMethodTypeLang));
                pcodeTypeStr = specialMethodStr;
            }
            if (pcodeTypeStr == null) {
                if (METHOD_GETTER_SETTER_TRAIT_CARD.equals(name) && trait == null) {
                    pcodeTypeStr = AppStrings.translate("abc.detail.innerfunction");
                } else {
                    pcodeTypeStr = AppStrings.translate("abc.detail.unsupported");
                }
            }
            detailTitleStr = detailTitleStr.replace("%pcode_type%", pcodeTypeStr);

            selectedLabel.setText(detailTitleStr);
            if (trait == null) {
                traitNameLabel.setText("-");
            } else if (abcPanel != null) {
                Multiname traitName = trait.getName(abc);
                String traitNameStr = traitName == null ? "" : traitName.getName(abc.constants, null, false, true);
                traitNameLabel.setText(traitNameStr);
            }
        });

    }

    private void editButtonActionPerformed(ActionEvent evt) {
        setEditMode(true);
        methodTraitPanel.methodCodePanel.focusEditor();
        mainPanel.setEditingStatus();
    }

    private void cancelButtonActionPerformed(ActionEvent evt) {
        setEditMode(false);
        abcPanel.decompiledTextArea.resetEditing();
        mainPanel.clearEditingStatus();
    }

    private void save(boolean refreshTree) {
        if (cardMap.get(selectedCard) instanceof TraitDetail) {
            if (((TraitDetail) cardMap.get(selectedCard)).save()) {

                DecompiledEditorPane decompiledTextArea = abcPanel.decompiledTextArea;
                if (!refreshTree) {
                    decompiledTextArea.reloadClass();
                    setEditMode(false);
                    return;
                }
                int lastTrait = decompiledTextArea.lastTraitIndex;
                int lastClassIndex = decompiledTextArea.getClassIndex();

                Runnable reloadComplete = new Runnable() {
                    @Override
                    public void run() {
                        decompiledTextArea.removeScriptListener(this);
                        decompiledTextArea.setClassIndex(lastClassIndex); //reload resets caret to first class
                        if (lastTrait == GraphTextWriter.TRAIT_UNKNOWN) {
                            decompiledTextArea.gotoLastMethod();
                        } else {
                            decompiledTextArea.gotoTrait(lastTrait);
                        }
                        setEditMode(false);
                        mainPanel.clearEditingStatus();
                        ViewMessages.showMessageDialog(DetailPanel.this, AppStrings.translate("message.trait.saved"), AppStrings.translate("dialog.message.title"), JOptionPane.INFORMATION_MESSAGE, Configuration.showTraitSavedMessage);
                    }
                };

                decompiledTextArea.addScriptListener(reloadComplete);
                decompiledTextArea.reloadClass();
            }
        }
    }

    private void saveButtonActionPerformed(ActionEvent evt) {
        save(true);
    }

    @Override
    public boolean tryAutoSave() {
        if (saveButton.isVisible() && saveButton.isEnabled() && Configuration.autoSaveTagModifications.get()) {
            save(false);
            return !(saveButton.isVisible() && saveButton.isEnabled());
        }
        return false;
    }

    @Override
    public boolean isEditing() {
        return saveButton.isVisible() && saveButton.isEnabled();
    }
}
