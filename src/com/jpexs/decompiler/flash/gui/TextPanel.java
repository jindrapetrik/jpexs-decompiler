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
package com.jpexs.decompiler.flash.gui;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.gui.controls.JRepeatButton;
import com.jpexs.decompiler.flash.gui.editor.LineMarkedEditorPane;
import com.jpexs.decompiler.flash.helpers.HighlightedText;
import com.jpexs.decompiler.flash.helpers.hilight.HighlightSpecialType;
import com.jpexs.decompiler.flash.helpers.hilight.Highlighting;
import com.jpexs.decompiler.flash.tags.DefineEditTextTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.MissingCharacterHandler;
import com.jpexs.decompiler.flash.tags.base.TextTag;
import com.jpexs.decompiler.flash.tags.text.TextAlign;
import com.jpexs.decompiler.flash.tags.text.TextParseException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.text.BadLocationException;

/**
 * @author JPEXS
 */
public class TextPanel extends JPanel implements TagEditorPanel {

    private static final Logger logger = Logger.getLogger(TextPanel.class.getName());

    private final MainPanel mainPanel;

    private final SearchPanel<TextTag> textSearchPanel;

    private final LineMarkedEditorPane textValue;

    private final JPanel buttonsPanel;

    private final JButton textEditButton;

    private final JButton textSaveButton;

    private final JButton textCancelButton;

    private final JButton selectPreviousTagButton;

    private final JButton selectNextTagButton;

    private final JButton textAlignLeftButton;

    private final JButton textAlignCenterButton;

    private final JButton textAlignRightButton;

    private final JButton textAlignJustifyButton;

    private final JButton decreaseTranslateXButton;

    private final JButton increaseTranslateXButton;

    private final JButton changeCaseButton;

    private final JButton undoChangesButton;

    private TextTag textTag;

    public TextPanel(final MainPanel mainPanel) {
        super(new BorderLayout());

        this.mainPanel = mainPanel;
        textSearchPanel = new SearchPanel<>(new FlowLayout(), mainPanel);
        textSearchPanel.setAlignmentX(0);
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(textSearchPanel);
        textValue = new LineMarkedEditorPane();
        add(new FasterScrollPane(textValue), BorderLayout.CENTER);
        textValue.setFont(Configuration.getSourceFont());
        textValue.changeContentType("text/swftext");
        textValue.addTextChangedListener(this::textChanged);

        JPanel textButtonsPanel = new JPanel();
        textButtonsPanel.setLayout(new FlowLayout(SwingConstants.WEST));
        textButtonsPanel.setMinimumSize(new Dimension(10, textButtonsPanel.getMinimumSize().height));

        selectPreviousTagButton = createButton(null, "arrowup16", "selectPreviousTag", e -> mainPanel.previousTag());
        selectNextTagButton = createButton(null, "arrowdown16", "selectNextTag", e -> mainPanel.nextTag());
        textAlignLeftButton = createButton(null, "textalignleft16", "text.align.left", e -> textAlign(TextAlign.LEFT));
        textAlignCenterButton = createButton(null, "textaligncenter16", "text.align.center", e -> textAlign(TextAlign.CENTER));
        textAlignRightButton = createButton(null, "textalignright16", "text.align.right", e -> textAlign(TextAlign.RIGHT));
        textAlignJustifyButton = createButton(null, "textalignjustify16", "text.align.justify", e -> textAlign(TextAlign.JUSTIFY));
        decreaseTranslateXButton = createButton(null, "textunindent16", "text.align.translatex.decrease", e -> translateX(-(int) SWF.unitDivisor, ((JRepeatButton) e.getSource()).getRepeatCount()), true);
        increaseTranslateXButton = createButton(null, "textindent16", "text.align.translatex.increase", e -> translateX((int) SWF.unitDivisor, ((JRepeatButton) e.getSource()).getRepeatCount()), true);
        changeCaseButton = createButton(null, "textuppercase16", "text.toggleCase", e -> changeCase(0));
        undoChangesButton = createButton(null, "reload16", "text.undo", e -> undoChanges());

        textButtonsPanel.add(selectPreviousTagButton);
        textButtonsPanel.add(selectNextTagButton);
        textButtonsPanel.add(textAlignLeftButton);
        textButtonsPanel.add(textAlignCenterButton);
        textButtonsPanel.add(textAlignRightButton);
        textButtonsPanel.add(textAlignJustifyButton);
        textButtonsPanel.add(decreaseTranslateXButton);
        textButtonsPanel.add(increaseTranslateXButton);
        textButtonsPanel.add(changeCaseButton);
        textButtonsPanel.add(undoChangesButton);

        textButtonsPanel.setAlignmentX(0);
        topPanel.add(textButtonsPanel);
        add(topPanel, BorderLayout.NORTH);

        buttonsPanel = new JPanel(new FlowLayout());
        textEditButton = createButton("button.edit", "edit16", null, e -> editText());
        textSaveButton = createButton("button.save", "save16", null, e -> saveText(true));
        textCancelButton = createButton("button.cancel", "cancel16", null, e -> cancelText());

        // hide the buttonts to avoid panel resize problems on other views
        textEditButton.setVisible(false);
        textSaveButton.setVisible(false);
        textCancelButton.setVisible(false);

        buttonsPanel.add(textEditButton);
        buttonsPanel.add(textSaveButton);
        buttonsPanel.add(textCancelButton);
        add(buttonsPanel, BorderLayout.SOUTH);
    }

    private JButton createButton(String textResource, String iconName, String toolTipResource, ActionListener actionListener) {
        return createButton(textResource, iconName, toolTipResource, actionListener, false);
    }

    private JButton createButton(String textResource, String iconName, String toolTipResource, ActionListener actionListener, boolean repeat) {
        String text = textResource == null ? "" : mainPanel.translate(textResource);
        JButton button = repeat ? new JRepeatButton(text, View.getIcon(iconName)) : new JButton(text, View.getIcon(iconName));
        button.setMargin(new Insets(3, 3, 3, 10));
        button.addActionListener(actionListener);
        if (toolTipResource != null) {
            button.setToolTipText(mainPanel.translate(toolTipResource));
        }

        return button;
    }

    public SearchPanel<TextTag> getSearchPanel() {
        return textSearchPanel;
    }

    public void setText(TextTag textTag) {
        this.textTag = textTag;
        String formattedText;
        try {
            formattedText = textTag.getFormattedText(false).text;
        } catch (IndexOutOfBoundsException ex) {
            formattedText = "Invalid text tag";
        }

        textValue.setText(formattedText);
        textValue.setCaretPosition(0);
        setModified(false);
        setEditText(false);
        boolean readOnly = ((Tag) textTag).isReadOnly();
        if (readOnly) {
            textValue.setEditable(false);
        }
        buttonsPanel.setVisible(!readOnly);
        textAlignLeftButton.setVisible(!readOnly);
        textAlignCenterButton.setVisible(!readOnly);
        textAlignRightButton.setVisible(!readOnly);
        textAlignJustifyButton.setVisible(!readOnly);
        decreaseTranslateXButton.setVisible(!readOnly);
        increaseTranslateXButton.setVisible(!readOnly);
        changeCaseButton.setVisible(!readOnly);
        undoChangesButton.setVisible(!readOnly);
        selectPreviousTagButton.setVisible(mainPanel.getCurrentView() == MainPanel.VIEW_RESOURCES);
        selectNextTagButton.setVisible(mainPanel.getCurrentView() == MainPanel.VIEW_RESOURCES);
    }

    private boolean isModified() {
        return textSaveButton.isVisible() && textSaveButton.isEnabled();
    }

    private void setModified(boolean value) {
        textSaveButton.setEnabled(value);
        textCancelButton.setEnabled(value);
    }

    public void focusTextValue() {
        textValue.requestFocusInWindow();
        if (textTag != null && !isModified()) {
            HighlightedText text = textTag.getFormattedText(false);
            for (Highlighting highlight : text.getSpecialHighlights()) {
                if (highlight.getProperties().subtype == HighlightSpecialType.TEXT) {
                    textValue.select(highlight.startPos, highlight.startPos + highlight.len);
                    break;
                }
            }
        }
    }

    private void changeCase(int caseMode) {
        // todo: use case mode: first letter, capitalize each word, toggle, etc
        int selStart = textValue.getSelectionStart();
        int selEnd = textValue.getSelectionEnd();
        if (selEnd > selStart) {
            StringBuilder selected = new StringBuilder(textValue.getSelectedText());

            HighlightedText text = textTag.getFormattedText(false);
            boolean allUpper = true;
            for (Highlighting highlight : text.getSpecialHighlights()) {
                if (highlight.getProperties().subtype == HighlightSpecialType.TEXT) {
                    int hStart = highlight.startPos;
                    int hEnd = highlight.startPos + highlight.len;
                    int start = Math.max(selStart, hStart);
                    int end = Math.min(selEnd, hEnd);

                    if (start < end) {
                        try {
                            String str = textValue.getDocument().getText(start, end - start);
                            if (!str.equals(str.toUpperCase())) {
                                allUpper = false;
                                break;
                            }
                        } catch (BadLocationException ex) {
                            logger.log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }

            for (Highlighting highlight : text.getSpecialHighlights()) {
                if (highlight.getProperties().subtype == HighlightSpecialType.TEXT) {
                    int hStart = highlight.startPos;
                    int hEnd = highlight.startPos + highlight.len;
                    int start = Math.max(selStart, hStart);
                    int end = Math.min(selEnd, hEnd);

                    if (start < end) {
                        try {
                            String str = textValue.getDocument().getText(start, end - start);
                            if (allUpper) {
                                str = str.toLowerCase();
                            } else {
                                str = str.toUpperCase();
                            }

                            selected.replace(start - selStart, end - selStart, str);
                        } catch (BadLocationException ex) {
                            logger.log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }

            textValue.replaceSelection(selected.toString());
            saveText(true);

            updateButtonsVisibility();
            textTag.getSwf().clearImageCache();
            mainPanel.repaintTree();

            textValue.requestFocusInWindow();
            textValue.select(selStart, selEnd);
        }
    }

    public void closeTag() {
        textTag = null;
    }

    private void setEditText(boolean edit) {
        textValue.setEditable(Configuration.editorMode.get() || edit);
        updateButtonsVisibility();
    }

    private void updateButtonsVisibility() {
        boolean edit = textValue.isEditable();
        boolean editorMode = Configuration.editorMode.get();
        textEditButton.setVisible(!edit);
        textSaveButton.setVisible(edit);
        boolean modified = isModified();
        textCancelButton.setVisible(edit);
        textCancelButton.setEnabled(modified || !editorMode);
        changeCaseButton.setEnabled(!modified);

        boolean alignable = false;
        if (textTag != null && !(textTag instanceof DefineEditTextTag)) {
            alignable = !edit || (editorMode && !modified);
        }

        textAlignLeftButton.setVisible(alignable);
        textAlignCenterButton.setVisible(alignable);
        textAlignRightButton.setVisible(alignable);
        textAlignJustifyButton.setVisible(alignable);
        increaseTranslateXButton.setVisible(alignable);
        decreaseTranslateXButton.setVisible(alignable);

        undoChangesButton.setVisible(textTag != null && textTag.isModified());
    }

    public void updateSearchPos() {
        textValue.setCaretPosition(0);
        textSearchPanel.showQuickFindDialog(textValue);
    }

    private void editText() {
        setEditText(true);
        showTextComparingPreview();
        mainPanel.setEditingStatus();
    }

    private void cancelText() {
        setEditText(false);
        mainPanel.reload(true);
        mainPanel.clearEditingStatus();
    }

    private void saveText(boolean refresh) {
        if (mainPanel.saveText(textTag, textValue.getText(), null, textValue)) {
            setEditText(false);
            setModified(false);
            textTag.getSwf().clearImageCache();
            if (refresh) {
                mainPanel.repaintTree();
            }
            mainPanel.clearEditingStatus();
        }
    }

    private void textAlign(TextAlign textAlign) {
        if (textTag.alignText(textAlign)) {
            updateButtonsVisibility();
            textTag.getSwf().clearImageCache();
            mainPanel.repaintTree();
        }
    }

    private void translateX(int delta, int repeatCount) {
        if (textTag.translateText(delta * (repeatCount + 1))) {
            updateButtonsVisibility();
            textTag.getSwf().clearImageCache();
            mainPanel.repaintTree();
        }
    }

    private void undoChanges() {
        try {
            textTag.undo();
        } catch (InterruptedException | IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        textTag.getSwf().clearImageCache();
        mainPanel.repaintTree();
    }

    private void textChanged() {
        setModified(true);

        showTextComparingPreview();
    }

    private void showTextComparingPreview() {
        if (!Configuration.showOldTextDuringTextEditing.get()) {
            return;
        }

        if (textValue.isEditable()) {
            boolean ok = false;
            try {
                TextTag copyTextTag = (TextTag) textTag.cloneTag();
                if (copyTextTag.setFormattedText(new MissingCharacterHandler() {
                    @Override
                    public boolean handle(TextTag textTag, FontTag font, char character) {
                        return false;
                    }
                }, textValue.getText(), null)) {
                    ok = true;
                    mainPanel.showTextTagWithNewValue(textTag, copyTextTag);
                }
            } catch (TextParseException | InterruptedException | IOException ex) {
                //ignored
            }

            if (!ok) {
                mainPanel.showTextTagWithNewValue(textTag, null);
            }
        }
    }

    @Override
    public boolean tryAutoSave() {
        if (isModified() && Configuration.autoSaveTagModifications.get()) {
            try {
                saveText(false);
                updateButtonsVisibility();
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Cannot auto-save text tag.", ex);
            }
        }

        return !isModified();
    }

    @Override
    public boolean isEditing() {
        return textSaveButton.isVisible() && textSaveButton.isEnabled();
    }

    public void startEdit() {
        if (!textEditButton.isVisible()) {
            return;
        }
        editText();
    }
}
