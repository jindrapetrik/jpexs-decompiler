/*
 *  Copyright (C) 2024 JPEXS
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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.gui.AppDialog;
import com.jpexs.decompiler.flash.gui.SelectTagOfTypeDialog;
import com.jpexs.decompiler.flash.gui.SelectTagPositionDialog;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.flash.tags.DefineBinaryDataTag;
import com.jpexs.decompiler.flash.tags.DefineFont4Tag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.SymbolClassTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.SoundTag;
import com.jpexs.decompiler.flash.timeline.Timelined;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
public class As3ClassLinkageDialog extends AppDialog {

    private final JButton proceedButton = new JButton(translate("button.proceed"));
    private final JButton cancelButton = new JButton(translate("button.cancel"));

    private final JTextField classNameTextField = new JTextField(30);
    private final JTextField parentClassNameTextField = new JTextField(30);
    private String selectedClass = null;
    private String selectedParentClass = null;
    private ABCContainerTag selectedAbcContainer;
    private SymbolClassTag selectedSymbolClassTag;
    private SymbolClassTag originalSymbolClassTag;
    private Tag selectedPosition;
    private Timelined selectedTimelined;
    private int result = ERROR_OPTION;

    private final SWF swf;

    private final int characterId;

    private int characterFrame = -1;

    private int abcCount = 0;

    private int symbolClassCount = 0;

    private ABCContainerTag foundInAbcContainer = null;

    private final List<ABCContainerTag> abcContainers = new ArrayList<>();

    private int abcFrame = -1;

    private boolean createClass = false;

    private final JPanel classFoundOrNotOrErrorPanel;

    private final JLabel errorLabel = new JLabel();

    private final JRadioButton existingAbcTagRadioButton = new JRadioButton(translate("class.notfound.create.abc.where.existing"));
    private final JRadioButton newAbcTagRadioButton = new JRadioButton(translate("class.notfound.create.abc.where.new"));

    private final JRadioButton createClassRadioButton = new JRadioButton(translate("class.notfound.create"));
    private final JRadioButton onlySetClassNameRadioButton = new JRadioButton(translate("class.notfound.onlySetClassName"));

    private final JRadioButton existingSymbolClassTagRadioButton = new JRadioButton(translate("class.notfound.onlySetClassName.symbolClass.where.existing"));
    private final JRadioButton newSymbolClassTagRadioButton = new JRadioButton(translate("class.notfound.onlySetClassName.symbolClass.where.new"));

    private static final String CLASS_NOT_FOUND_CARD = "Class not found panel";
    private static final String CLASS_FOUND_CARD = "Class found panel";
    private static final String ERROR_CARD = "Error panel";

    private static final String CREATE_CLASS_CARD = "Create class panel";
    private static final String DO_NOT_CREATE_CLASS_CARD = "Do not create class panel";

    private static final Map<Class<?>, String> tagTypeToParentClass = new HashMap<>();

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

    public As3ClassLinkageDialog(Window owner, SWF swf, int characterId) {
        super(owner);

        this.swf = swf;
        this.characterId = characterId;

        CharacterTag ch = swf.getCharacter(characterId);
        if (ch == null) {
            throw new RuntimeException("Character " + characterId + " not found");
        }

        parentClassNameTextField.setText(getParentClassFromCharacter(ch));

        int frame = 1;
        for (Tag t : swf.getTags()) {
            if (t == ch) {
                characterFrame = frame;
            }
            if (t instanceof ShowFrameTag) {
                frame++;
            }
        }
        frame = 1;
        for (Tag t : swf.getTags()) {
            if (frame >= characterFrame) {
                if (t instanceof ABCContainerTag) {
                    abcCount++;
                    abcContainers.add((ABCContainerTag) t);
                }
                if (t instanceof SymbolClassTag) {
                    SymbolClassTag sc = (SymbolClassTag) t;
                    if (sc.tags.contains(characterId)) {
                        originalSymbolClassTag = sc;
                    }
                    symbolClassCount++;
                }
            }
            if (t instanceof ShowFrameTag) {
                frame++;
            }
        }

        LinkedHashSet<String> classNames = ch.getClassNames();

        if (classNames.size() == 1) {
            classNameTextField.setText(classNames.iterator().next());
        }

        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setTitle(translate("dialog.title"));

        Container cnt = getContentPane();
        cnt.setLayout(new BoxLayout(cnt, BoxLayout.Y_AXIS));

        JPanel classFoundPanel = new JPanel();
        classFoundPanel.setLayout(new BoxLayout(classFoundPanel, BoxLayout.Y_AXIS));

        JLabel classFoundLabel = new JLabel(translate("class.found"));
        classFoundPanel.add(classFoundLabel);
        JLabel symbolClassAppropriateLabel = new JLabel(translate("symbolClassAppropriate"));
        classFoundPanel.add(symbolClassAppropriateLabel);

        JPanel classNotFoundPanel = new JPanel();
        classNotFoundPanel.setLayout(new BoxLayout(classNotFoundPanel, BoxLayout.Y_AXIS));
        JLabel notFoundLabel = new JLabel(translate("class.notfound"));
        classNotFoundPanel.add(notFoundLabel);
        JLabel createAskLabel = new JLabel(translate("class.notfound.createAsk"));
        classNotFoundPanel.add(createAskLabel);

        ButtonGroup doCreateClassButtonGroup = new ButtonGroup();
        doCreateClassButtonGroup.add(createClassRadioButton);
        doCreateClassButtonGroup.add(onlySetClassNameRadioButton);

        JPanel createNewClassAsk = new JPanel(new FlowLayout());
        createNewClassAsk.add(createClassRadioButton);
        createNewClassAsk.add(onlySetClassNameRadioButton);
        createClassRadioButton.setSelected(true);
        classNotFoundPanel.add(createNewClassAsk);

        JPanel createNewClassPanel = new JPanel();
        createNewClassPanel.setLayout(new BoxLayout(createNewClassPanel, BoxLayout.Y_AXIS));

        JLabel parentClassNameLabel = new JLabel(translate("class.notfound.create.parentType"));
        createNewClassPanel.add(parentClassNameLabel);
        createNewClassPanel.add(parentClassNameTextField);

        JLabel abcWhereLabel = new JLabel(translate("class.notfound.create.abc.where"));
        createNewClassPanel.add(abcWhereLabel);
        ButtonGroup abcTargetButtonGroup = new ButtonGroup();
        abcTargetButtonGroup.add(existingAbcTagRadioButton);
        abcTargetButtonGroup.add(newAbcTagRadioButton);
        existingAbcTagRadioButton.setSelected(true);

        JPanel abcTargetPanel = new JPanel(new FlowLayout());
        abcTargetPanel.add(existingAbcTagRadioButton);
        abcTargetPanel.add(newAbcTagRadioButton);

        createNewClassPanel.add(abcTargetPanel);

        JLabel symbolClassAppropriate2Label = new JLabel(translate("symbolClassAppropriate"));
        classFoundPanel.add(symbolClassAppropriate2Label);
        createNewClassPanel.add(symbolClassAppropriate2Label);

        JPanel doNotCreateNewClassPanel = new JPanel();
        doNotCreateNewClassPanel.setLayout(new BoxLayout(doNotCreateNewClassPanel, BoxLayout.Y_AXIS));

        ButtonGroup whereToStoreMappingButtonGroup = new ButtonGroup();
        whereToStoreMappingButtonGroup.add(existingSymbolClassTagRadioButton);
        whereToStoreMappingButtonGroup.add(newSymbolClassTagRadioButton);

        if (originalSymbolClassTag == null) {
            doNotCreateNewClassPanel.add(new JLabel(translate("class.notfound.onlySetClassName.symbolClass.where")));

            JPanel whereToStoreMappingPanel = new JPanel(new FlowLayout());
            whereToStoreMappingPanel.add(existingSymbolClassTagRadioButton);
            whereToStoreMappingPanel.add(newSymbolClassTagRadioButton);
            doNotCreateNewClassPanel.add(whereToStoreMappingPanel);
        }
        existingSymbolClassTagRadioButton.setSelected(true);

        setCentralAlignment(doNotCreateNewClassPanel);
        setCentralAlignment(createNewClassPanel);

        JPanel createNewClassAskCards = new JPanel(new CardLayout());
        createNewClassAskCards.add(createNewClassPanel, CREATE_CLASS_CARD);
        createNewClassAskCards.add(doNotCreateNewClassPanel, DO_NOT_CREATE_CLASS_CARD);

        classNotFoundPanel.add(createNewClassAskCards);

        ChangeListener createClassSwitched = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                CardLayout cl = (CardLayout) createNewClassAskCards.getLayout();
                if (createClassRadioButton.isSelected()) {
                    cl.show(createNewClassAskCards, CREATE_CLASS_CARD);
                } else {
                    cl.show(createNewClassAskCards, DO_NOT_CREATE_CLASS_CARD);
                }
                checkEnabled();
            }
        };

        createClassRadioButton.addChangeListener(createClassSwitched);
        onlySetClassNameRadioButton.addChangeListener(createClassSwitched);

        JPanel buttonsPanel = new JPanel(new FlowLayout());
        proceedButton.addActionListener(this::okButtonActionPerformed);
        cancelButton.addActionListener(this::cancelButtonActionPerformed);
        buttonsPanel.add(proceedButton);
        buttonsPanel.add(cancelButton);

        JLabel classNameLabel = new JLabel(translate("classname"));
        cnt.add(classNameLabel);
        cnt.add(classNameTextField);

        JPanel errorPanel = new JPanel(new FlowLayout());
        errorPanel.add(errorLabel);

        setCentralAlignment(classFoundPanel);
        setCentralAlignment(classNotFoundPanel);
        setCentralAlignment(errorPanel);

        classFoundOrNotOrErrorPanel = new JPanel(new CardLayout());
        classFoundOrNotOrErrorPanel.add(classFoundPanel, CLASS_FOUND_CARD);
        classFoundOrNotOrErrorPanel.add(classNotFoundPanel, CLASS_NOT_FOUND_CARD);
        classFoundOrNotOrErrorPanel.add(errorPanel, ERROR_CARD);

        cnt.add(classFoundOrNotOrErrorPanel);

        cnt.add(buttonsPanel);

        setCentralAlignment((JComponent) cnt);

        if (abcCount == 0) {
            newAbcTagRadioButton.setSelected(true);
            //abcTargetPanel.setVisible(false);    
            newAbcTagRadioButton.setEnabled(false);
            existingAbcTagRadioButton.setEnabled(false);
        }

        if (symbolClassCount == 0) {
            newSymbolClassTagRadioButton.setSelected(true);
            newSymbolClassTagRadioButton.setEnabled(false);
            existingSymbolClassTagRadioButton.setEnabled(false);
        }

        existingSymbolClassTagRadioButton.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                checkEnabled();
            }
        });
        newSymbolClassTagRadioButton.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                checkEnabled();
            }
        });

        existingAbcTagRadioButton.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                checkEnabled();
            }
        });
        newAbcTagRadioButton.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                checkEnabled();
            }
        });

        classNameTextField.getDocument().addDocumentListener(new DocumentListener() {
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
        });

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

        String newClassName = classNameTextField.getText();

        /*if (newClassName.isEmpty()) {
            ok = false;
            errorLabel.setText("");
        }*/
        if (newClassName.endsWith(".")) {
            ok = false;
            errorLabel.setText("");
        }

        if (ok) {
            CharacterTag ch = swf.getCharacter(characterId);
            LinkedHashSet<String> oldClassNames = ch.getClassNames();
            if (oldClassNames.size() > 1) {
                ok = false;
                errorLabel.setText(translate("error.multipleClasses"));
            } else if (oldClassNames.size() == 1 && newClassName.equals(oldClassNames.iterator().next())) {
                ok = false;
                errorLabel.setText(translate("error.needToModify"));
            } else if (!newClassName.isEmpty() && swf.getCharacterByClass(newClassName) != null) {
                ok = false;
                errorLabel.setText(translate("error.alreadyAssignedClass"));
            }
        }

        CardLayout cl = (CardLayout) classFoundOrNotOrErrorPanel.getLayout();

        if (ok) {
            if (newClassName.isEmpty()) {
                proceedButton.setText(translate("button.ok"));
                cl.show(classFoundOrNotOrErrorPanel, ERROR_CARD);
                errorLabel.setText("");
            } else {
                List<String> classNames = new ArrayList<>();
                classNames.add(newClassName);
                foundInAbcContainer = null;
                try {
                    List<ScriptPack> scriptPacks = swf.getScriptPacksByClassNames(classNames);
                    if (!scriptPacks.isEmpty()) {
                        ABC foundInAbc = scriptPacks.get(0).abc; //Assume there is only single pack with the class

                        boolean foundContainer = false;
                        for (ABCContainerTag cnt : abcContainers) {
                            if (cnt.getABC() == foundInAbc) {
                                foundInAbcContainer = cnt;
                                break;
                            }
                        }
                    }
                } catch (Exception ex) {
                    //ignore
                }

                if (foundInAbcContainer != null) {
                    cl.show(classFoundOrNotOrErrorPanel, CLASS_FOUND_CARD);
                } else {
                    cl.show(classFoundOrNotOrErrorPanel, CLASS_NOT_FOUND_CARD);

                    if (createClassRadioButton.isSelected()) {
                        if (existingAbcTagRadioButton.isSelected() && abcCount == 1) {
                            proceedButton.setText(translate("button.ok"));
                        } else {
                            proceedButton.setText(translate("button.proceed"));
                        }
                    } else {
                        if (originalSymbolClassTag != null || (existingSymbolClassTagRadioButton.isSelected() && symbolClassCount == 1)) {
                            proceedButton.setText(translate("button.ok"));
                        } else {
                            proceedButton.setText(translate("button.proceed"));
                        }
                    }
                }
            }
        } else {
            cl.show(classFoundOrNotOrErrorPanel, ERROR_CARD);
            proceedButton.setText(translate("button.ok"));
        }

        proceedButton.setEnabled(ok);
    }

    private void okButtonActionPerformed(ActionEvent evt) {
        if (!proceedButton.isEnabled()) {
            return;
        }
        setVisible(false);
        boolean emptyClassName = classNameTextField.getText().isEmpty();
        if (emptyClassName) {
            selectedSymbolClassTag = originalSymbolClassTag;
        } else {
            if (foundInAbcContainer == null) {
                if (createClassRadioButton.isSelected()) {
                    if (existingAbcTagRadioButton.isSelected()) {
                        SelectTagOfTypeDialog selectDoABCDialog = new SelectTagOfTypeDialog(owner, swf, ABCContainerTag.class, "DoABC", characterFrame);
                        selectedAbcContainer = (ABCContainerTag) selectDoABCDialog.showDialog();
                        if (selectedAbcContainer == null) {
                            cancelButtonActionPerformed(evt);
                            return;
                        }
                    }
                    if (newAbcTagRadioButton.isSelected()) {
                        SelectTagPositionDialog selectTagPositionDialog = new SelectTagPositionDialog(owner, swf, false, "DoABC", characterFrame);
                        if (selectTagPositionDialog.showDialog() != OK_OPTION) {
                            cancelButtonActionPerformed(evt);
                            return;
                        }
                        selectedPosition = selectTagPositionDialog.getSelectedTag();
                        selectedTimelined = selectTagPositionDialog.getSelectedTimelined();
                    }
                } else {
                    if (originalSymbolClassTag != null) {
                        selectedSymbolClassTag = originalSymbolClassTag;
                    } else {
                        if (existingSymbolClassTagRadioButton.isSelected()) {
                            SelectTagOfTypeDialog selectSymbolClassDialog = new SelectTagOfTypeDialog(owner, swf, SymbolClassTag.class, "SymbolClass", characterFrame);
                            selectedSymbolClassTag = (SymbolClassTag) selectSymbolClassDialog.showDialog();
                            if (selectedSymbolClassTag == null) {
                                cancelButtonActionPerformed(evt);
                                return;
                            }
                        }
                        if (newSymbolClassTagRadioButton.isSelected()) {
                            SelectTagPositionDialog selectTagPositionDialog = new SelectTagPositionDialog(owner, swf, false, "SymbolClass", characterFrame);
                            if (selectTagPositionDialog.showDialog() != OK_OPTION) {
                                cancelButtonActionPerformed(evt);
                                return;
                            }
                            selectedPosition = selectTagPositionDialog.getSelectedTag();
                            selectedTimelined = selectTagPositionDialog.getSelectedTimelined();
                        }
                    }
                }
            } else {
                selectedAbcContainer = foundInAbcContainer;
            }
        }

        if (selectedAbcContainer != null) {
            int frame = 1;
            for (Tag t : swf.getTags()) {
                if (t == selectedAbcContainer) {
                    abcFrame = frame;
                    break;
                }
                if (t instanceof ShowFrameTag) {
                    frame++;
                }
            }
        }

        createClass = !emptyClassName && foundInAbcContainer == null && createClassRadioButton.isSelected();

        result = OK_OPTION;
        selectedClass = classNameTextField.getText();
        selectedParentClass = parentClassNameTextField.getText();
        setVisible(false);
    }

    private void cancelButtonActionPerformed(ActionEvent evt) {
        selectedClass = null;
        selectedParentClass = null;
        selectedAbcContainer = null;
        selectedPosition = null;
        selectedTimelined = null;
        selectedSymbolClassTag = null;
        foundInAbcContainer = null;
        createClass = false;
        abcFrame = -1;
        characterFrame = -1;
        result = CANCEL_OPTION;
        setVisible(false);
    }

    public int showDialog() {
        selectedClass = null;
        selectedParentClass = null;
        selectedAbcContainer = null;
        selectedPosition = null;
        selectedTimelined = null;
        selectedSymbolClassTag = null;
        foundInAbcContainer = null;
        createClass = false;
        abcFrame = -1;
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

    public SymbolClassTag getSelectedSymbolClassTag() {
        return selectedSymbolClassTag;
    }

    public Timelined getSelectedTimelined() {
        return selectedTimelined;
    }

    public ABCContainerTag getSelectedAbcContainer() {
        return selectedAbcContainer;
    }

    public boolean isClassFound() {
        return foundInAbcContainer != null;
    }

    public int getCharacterFrame() {
        return characterFrame;
    }

    public int getAbcFrame() {
        return abcFrame;
    }

    public boolean doCreateClass() {
        return createClass;
    }
}
