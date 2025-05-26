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
package com.jpexs.decompiler.flash.gui;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.DefineBinaryDataTag;
import com.jpexs.decompiler.flash.tags.DefineFont4Tag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.ExportAssetsTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.ImportTag;
import com.jpexs.decompiler.flash.tags.base.SoundTag;
import com.jpexs.decompiler.flash.timeline.Timelined;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * @author JPEXS
 */
public class AsLinkageDialog extends AppDialog {

    private final JButton proceedButton = new JButton(translate("button.proceed"));
    private final JButton cancelButton = new JButton(translate("button.cancel"));

    private final JTextField identifierTextField = new JTextField(30);
    private final JTextField classNameTextField = new JTextField(30);
    private final JTextField parentClassNameTextField = new JTextField(30);
    private String selectedIdentifier = null;
    private String selectedClass = null;
    private String selectedParentClass = null;
    private ExportAssetsTag originalExportAssetsTag;
    private ExportAssetsTag selectedExportAssetsTag;
    private String originalClassName;
    private Tag selectedPosition;
    private Timelined selectedTimelined;
    private int result = ERROR_OPTION;

    private final SWF swf;

    private final int characterId;

    private int characterFrame = -1;

    private int exportAssetsCount = 0;

    private final JLabel errorLabel = new JLabel();

    private final JLabel parentClassNameLabel = new JLabel(translate("class.parentname"));

    private final JRadioButton existingExportAssetsTagRadioButton = new JRadioButton(translate("linkage.notfound.exportAssets.where.existing"));
    private final JRadioButton newExportAssetsTagRadioButton = new JRadioButton(translate("linkage.notfound.exportAssets.where.new"));

    private static final Map<Class<?>, String> tagTypeToParentClass = new HashMap<>();

    private Set<String> existingNames = new HashSet<>();

    static {
        tagTypeToParentClass.put(SoundTag.class, "flash.media.Sound");
        tagTypeToParentClass.put(ImageTag.class, "flash.display.Bitmap");
        tagTypeToParentClass.put(FontTag.class, "flash.text.Font");
        tagTypeToParentClass.put(DefineFont4Tag.class, "flash.text.Font");
        tagTypeToParentClass.put(DefineBinaryDataTag.class, "flash.utils.ByteArray");
        tagTypeToParentClass.put(DefineSpriteTag.class, "flash.display.Sprite");
    }

    public static String getParentClassFromCharacter(CharacterTag ch) {
        for (Class<?> cls : tagTypeToParentClass.keySet()) {
            if (cls.isAssignableFrom(ch.getClass())) {
                return tagTypeToParentClass.get(cls);
            }
        }
        return null;
    }

    public AsLinkageDialog(Window owner, SWF swf, int characterId) {
        super(owner);

        this.swf = swf;
        this.characterId = characterId;

        CharacterTag ch = swf.getCharacter(characterId);
        if (ch == null) {
            throw new RuntimeException("Character " + characterId + " not found");
        }

        //parentClassNameTextField.setText(getParentClassFromCharacter(ch));
        int frame = 1;
        for (Tag t : swf.getTags()) {
            if (t == ch) {
                characterFrame = frame;
            }
            if (t instanceof ShowFrameTag) {
                frame++;
            }
            if (t instanceof ExportAssetsTag) {
                ExportAssetsTag ea = (ExportAssetsTag) t;
                existingNames.addAll(ea.names);
            }
            if (t instanceof ImportTag) {
                ImportTag it = (ImportTag) t;
                existingNames.addAll(it.getAssets().values());
            }
        }
        frame = 1;
        for (Tag t : swf.getTags()) {
            if (frame >= characterFrame) {
                if (t instanceof ExportAssetsTag) {
                    ExportAssetsTag ea = (ExportAssetsTag) t;
                    if (ea.tags.contains(characterId)) {
                        originalExportAssetsTag = ea;
                    }
                    exportAssetsCount++;
                }
            }
            if (t instanceof ShowFrameTag) {
                frame++;
            }
        }

        String originalIdentifier = ch.getExportName();

        identifierTextField.setText(originalIdentifier);

        originalClassName = ch.getAs2ClassName();

        if (originalClassName != null) {
            classNameTextField.setText(originalClassName);
            classNameTextField.setEnabled(false);
            identifierTextField.setEnabled(false);
        }

        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setTitle(translate("dialog.title"));

        Container cnt = getContentPane();
        cnt.setLayout(new BoxLayout(cnt, BoxLayout.Y_AXIS));

        cnt.add(new JLabel(translate("identifier")));
        cnt.add(identifierTextField);

        cnt.add(new JLabel(translate("classname")));
        cnt.add(classNameTextField);
        if (originalClassName == null) {
            cnt.add(parentClassNameLabel);
            cnt.add(parentClassNameTextField);
        }
        cnt.add(errorLabel);

        if (originalClassName == null && originalExportAssetsTag == null) {
            ButtonGroup whereToStoreMappingButtonGroup = new ButtonGroup();
            whereToStoreMappingButtonGroup.add(existingExportAssetsTagRadioButton);
            whereToStoreMappingButtonGroup.add(newExportAssetsTagRadioButton);
            cnt.add(new JLabel(translate("linkage.notfound.exportAssets.where")));

            JPanel whereToStoreMappingPanel = new JPanel(new FlowLayout());
            whereToStoreMappingPanel.add(existingExportAssetsTagRadioButton);
            whereToStoreMappingPanel.add(newExportAssetsTagRadioButton);
            cnt.add(whereToStoreMappingPanel);
            existingExportAssetsTagRadioButton.setSelected(true);
        }

        JPanel buttonsPanel = new JPanel(new FlowLayout());
        proceedButton.addActionListener(this::okButtonActionPerformed);
        cancelButton.addActionListener(this::cancelButtonActionPerformed);
        buttonsPanel.add(proceedButton);
        buttonsPanel.add(cancelButton);

        cnt.add(buttonsPanel);

        setCentralAlignment((JComponent) cnt);

        if (exportAssetsCount == 0) {
            newExportAssetsTagRadioButton.setSelected(true);
            newExportAssetsTagRadioButton.setEnabled(false);
            existingExportAssetsTagRadioButton.setEnabled(false);
        }

        existingExportAssetsTagRadioButton.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                checkEnabled();
            }
        });
        newExportAssetsTagRadioButton.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                checkEnabled();
            }
        });

        DocumentListener updateDocumentListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                checkEnabled();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                checkEnabled();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                checkEnabled();
            }
        };

        identifierTextField.getDocument().addDocumentListener(updateDocumentListener);
        classNameTextField.getDocument().addDocumentListener(updateDocumentListener);
        parentClassNameTextField.getDocument().addDocumentListener(updateDocumentListener);

        classNameTextField.addActionListener(this::okButtonActionPerformed);
        checkEnabled();
        pack();
        setModal(true);
        setResizable(false);
        View.setWindowIcon(this);
        View.centerScreen(this);
    }

    private void setCentralAlignment(JComponent container) {
        Component[] components = container.getComponents();
        for (Component component : components) {
            if (component instanceof JComponent) {
                ((JComponent) component).setAlignmentX(Component.CENTER_ALIGNMENT);
            }
        }
    }

    private void checkEnabled() {

        boolean ok = true;

        CharacterTag ch = swf.getCharacter(characterId);

        String oldIdentifier = ch.getExportName();
        String newIdentifier = identifierTextField.getText();
        String newClassName = classNameTextField.getText();

        if (originalClassName != null) {
            ok = false;
            proceedButton.setText(translate("button.ok"));
            errorLabel.setText("");
        }

        if (!classNameTextField.isEnabled() && newIdentifier.isEmpty()) {
            ok = false;
            proceedButton.setText(translate("button.ok"));
            errorLabel.setText(translate("error.cannotRemoveIdentifierClassExists"));
        }

        if (newClassName.endsWith(".")) {
            ok = false;
            proceedButton.setText(translate("button.ok"));
            errorLabel.setText("");
        }

        if (newIdentifier.isEmpty() && !newClassName.isEmpty()) {
            ok = false;
            proceedButton.setText(translate("button.ok"));
            errorLabel.setText("");
        }

        if (!newIdentifier.isEmpty() && oldIdentifier != null && !oldIdentifier.equals(newIdentifier) && existingNames.contains(newIdentifier)) {
            ok = false;
            proceedButton.setText(translate("button.ok"));
            errorLabel.setText("");
        }

        if (ok) {
            String oldClassName = ch.getAs2ClassName();
            if (oldClassName == null && newClassName != null && ch.getSwf().getCharacterByExportName("__Packages." + newClassName) != null) {
                ok = false;
                errorLabel.setText(translate("error.alreadyExistsClass"));
            }
        }

        if (ok) {
            if (originalExportAssetsTag != null) {
                proceedButton.setText(translate("button.ok"));
            } else if (existingExportAssetsTagRadioButton.isSelected() && exportAssetsCount == 1) {
                proceedButton.setText(translate("button.ok"));
            } else {
                proceedButton.setText(translate("button.proceed"));
            }
        } else {
            proceedButton.setText(translate("button.ok"));
        }

        proceedButton.setEnabled(ok);
    }

    private void okButtonActionPerformed(ActionEvent evt) {
        if (!proceedButton.isEnabled()) {
            return;
        }
        setVisible(false);
        if (originalExportAssetsTag != null) {
            selectedExportAssetsTag = originalExportAssetsTag;
        } else {
            if (existingExportAssetsTagRadioButton.isSelected()) {
                SelectTagOfTypeDialog selectExportAssetsDialog = new SelectTagOfTypeDialog(owner, swf, ExportAssetsTag.class, "ExportAssets", characterFrame);
                selectedExportAssetsTag = (ExportAssetsTag) selectExportAssetsDialog.showDialog();
                if (selectedExportAssetsTag == null) {
                    cancelButtonActionPerformed(evt);
                    return;
                }
            }
            if (newExportAssetsTagRadioButton.isSelected()) {
                SelectTagPositionDialog selectTagPositionDialog = new SelectTagPositionDialog(owner, swf, false, "ExportAssets", characterFrame);
                if (selectTagPositionDialog.showDialog() != OK_OPTION) {
                    cancelButtonActionPerformed(evt);
                    return;
                }
                selectedPosition = selectTagPositionDialog.getSelectedTag();
                selectedTimelined = selectTagPositionDialog.getSelectedTimelined();
            }
        }
        result = OK_OPTION;
        selectedIdentifier = identifierTextField.getText();
        selectedClass = classNameTextField.getText();
        selectedParentClass = parentClassNameTextField.getText();
        setVisible(false);
    }

    private void cancelButtonActionPerformed(ActionEvent evt) {
        selectedIdentifier = null;
        selectedClass = null;
        selectedParentClass = null;
        selectedPosition = null;
        selectedTimelined = null;
        selectedExportAssetsTag = null;
        characterFrame = -1;
        result = CANCEL_OPTION;
        setVisible(false);
    }

    public int showDialog() {
        selectedIdentifier = null;
        selectedClass = null;
        selectedParentClass = null;
        selectedPosition = null;
        selectedTimelined = null;
        selectedExportAssetsTag = null;
        setVisible(true);
        return result;
    }

    public Tag getSelectedPosition() {
        return selectedPosition;
    }

    public String getSelectedClass() {
        return selectedClass;
    }

    public String getSelectedParentClass() {
        return selectedParentClass;
    }

    public ExportAssetsTag getSelectedExportAssetsTag() {
        return selectedExportAssetsTag;
    }

    public Timelined getSelectedTimelined() {
        return selectedTimelined;
    }

    public int getCharacterFrame() {
        return characterFrame;
    }

    public String getSelectedIdentifier() {
        return selectedIdentifier;
    }

}
