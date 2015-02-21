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
package com.jpexs.decompiler.flash.gui;

import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.gui.abc.LineMarkedEditorPane;
import com.jpexs.decompiler.flash.tags.DefineEditTextTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.MissingCharacterHandler;
import com.jpexs.decompiler.flash.tags.base.TextTag;
import com.jpexs.decompiler.flash.tags.text.TextAlign;
import com.jpexs.decompiler.flash.tags.text.TextParseException;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import jsyntaxpane.DefaultSyntaxKit;

/**
 *
 * @author JPEXS
 */
public class TextPanel extends JPanel implements ActionListener {

    private static final String ACTION_EDIT_TEXT = "EDITTEXT";

    private static final String ACTION_CANCEL_TEXT = "CANCELTEXT";

    private static final String ACTION_SAVE_TEXT = "SAVETEXT";

    private static final String ACTION_TEXT_ALIGN_LEFT = "TEXTALIGNLEFT";

    private static final String ACTION_TEXT_ALIGN_CENTER = "TEXTALIGNCENTER";

    private static final String ACTION_TEXT_ALIGN_RIGHT = "TEXTALIGNRIGHT";

    private static final String ACTION_TEXT_ALIGN_JUSTIFY = "TEXTALIGNJUSTIFY";

    private static final String ACTION_UNDO_CHANGES = "UNDOCHANGES";

    private final MainPanel mainPanel;

    private final SearchPanel<TextTag> textSearchPanel;

    private final LineMarkedEditorPane textValue;

    private final JButton textSaveButton;

    private final JButton textEditButton;

    private final JButton textCancelButton;

    private final JButton textAlignLeftButton;

    private final JButton textAlignCenterButton;

    private final JButton textAlignRightButton;

    private final JButton textAlignJustifyButton;

    private final JButton undoChangesButton;

    public TextPanel(final MainPanel mainPanel) {
        super(new BorderLayout());

        DefaultSyntaxKit.initKit();
        this.mainPanel = mainPanel;
        textSearchPanel = new SearchPanel<>(new FlowLayout(), mainPanel);
        textSearchPanel.setAlignmentX(0);
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(textSearchPanel);
        textValue = new LineMarkedEditorPane();
        add(new JScrollPane(textValue), BorderLayout.CENTER);
        textValue.setEditable(false);
        textValue.setFont(new Font("Monospaced", Font.PLAIN, textValue.getFont().getSize()));
        textValue.setContentType("text/swftext");
        textValue.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                textChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                textChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                textChanged();
            }
        });

        JPanel textButtonsPanel = new JPanel();
        textButtonsPanel.setLayout(new FlowLayout(SwingConstants.WEST));

        textAlignLeftButton = createButton(null, "textalignleft16", ACTION_TEXT_ALIGN_LEFT, "text.align.left");
        textAlignCenterButton = createButton(null, "textaligncenter16", ACTION_TEXT_ALIGN_CENTER, "text.align.center");
        textAlignRightButton = createButton(null, "textalignright16", ACTION_TEXT_ALIGN_RIGHT, "text.align.right");
        textAlignJustifyButton = createButton(null, "textalignjustify16", ACTION_TEXT_ALIGN_JUSTIFY, "text.align.justify");
        undoChangesButton = createButton(null, "reload16", ACTION_UNDO_CHANGES, "text.undo");

        textButtonsPanel.add(textAlignLeftButton);
        textButtonsPanel.add(textAlignCenterButton);
        textButtonsPanel.add(textAlignRightButton);
        textButtonsPanel.add(textAlignJustifyButton);
        textButtonsPanel.add(undoChangesButton);

        textButtonsPanel.setAlignmentX(0);
        topPanel.add(textButtonsPanel);
        add(topPanel, BorderLayout.NORTH);

        JPanel buttonsPanel = new JPanel(new FlowLayout());
        textSaveButton = createButton("button.save", "save16", ACTION_SAVE_TEXT, null);
        textEditButton = createButton("button.edit", "edit16", ACTION_EDIT_TEXT, null);
        textCancelButton = createButton("button.cancel", "cancel16", ACTION_CANCEL_TEXT, null);

        textSaveButton.setVisible(false);
        textCancelButton.setVisible(false);

        buttonsPanel.add(textEditButton);
        buttonsPanel.add(textSaveButton);
        buttonsPanel.add(textCancelButton);
        add(buttonsPanel, BorderLayout.SOUTH);
    }

    private JButton createButton(String textResource, String iconName, String command, String toolTipResource) {
        String text = textResource == null ? "" : mainPanel.translate(textResource);
        JButton button = new JButton(text, View.getIcon(iconName));
        button.setMargin(new Insets(3, 3, 3, 10));
        button.setActionCommand(command);
        button.addActionListener(this);
        if (toolTipResource != null) {
            button.setToolTipText(mainPanel.translate(toolTipResource));
        }

        return button;
    }

    public SearchPanel<TextTag> getSearchPanel() {
        return textSearchPanel;
    }

    public void setText(String text) {
        textValue.setText(text);
        textValue.setCaretPosition(0);
    }

    public void setEditText(boolean edit) {
        textValue.setEditable(edit);
        textSaveButton.setVisible(edit);
        textEditButton.setVisible(!edit);
        textCancelButton.setVisible(edit);

        TreeItem item = mainPanel.tagTree.getCurrentTreeItem();
        boolean alignable = false;
        if (item instanceof TextTag && !(item instanceof DefineEditTextTag)) {
            alignable = !edit;
        }

        textAlignLeftButton.setVisible(alignable);
        textAlignCenterButton.setVisible(alignable);
        textAlignRightButton.setVisible(alignable);
        textAlignJustifyButton.setVisible(alignable);

        undoChangesButton.setVisible(item != null && item instanceof TextTag && ((Tag) item).isModified());
    }

    public void updateSearchPos() {
        textValue.setCaretPosition(0);
        View.execInEventDispatchLater(new Runnable() {

            @Override
            public void run() {
                textSearchPanel.showQuickFindDialog(textValue);
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case ACTION_EDIT_TEXT:
                setEditText(true);
                textChanged();
                break;
            case ACTION_CANCEL_TEXT:
                setEditText(false);
                mainPanel.reload(true);
                break;
            case ACTION_SAVE_TEXT: {
                TreeItem item = mainPanel.tagTree.getCurrentTreeItem();
                if (item instanceof TextTag) {
                    TextTag textTag = (TextTag) item;
                    if (mainPanel.saveText(textTag, textValue.getText(), null)) {
                        setEditText(false);
                        item.getSwf().clearImageCache();
                        mainPanel.refreshTree();
                    }
                }
                break;
            }
            case ACTION_TEXT_ALIGN_LEFT:
            case ACTION_TEXT_ALIGN_CENTER:
            case ACTION_TEXT_ALIGN_RIGHT:
            case ACTION_TEXT_ALIGN_JUSTIFY: {
                TreeItem item = mainPanel.tagTree.getCurrentTreeItem();
                if (item instanceof TextTag) {
                    TextTag textTag = (TextTag) item;
                    TextAlign ta = null;
                    switch (e.getActionCommand()) {
                        case ACTION_TEXT_ALIGN_LEFT:
                            ta = TextAlign.LEFT;
                            break;
                        case ACTION_TEXT_ALIGN_CENTER:
                            ta = TextAlign.CENTER;
                            break;
                        case ACTION_TEXT_ALIGN_RIGHT:
                            ta = TextAlign.RIGHT;
                            break;
                        case ACTION_TEXT_ALIGN_JUSTIFY:
                            ta = TextAlign.JUSTIFY;
                            break;
                    }

                    if (mainPanel.alignText(textTag, ta)) {
                        setEditText(false);
                        item.getSwf().clearImageCache();
                        mainPanel.refreshTree();
                    }
                }
                undoChangesButton.setVisible(item != null && item instanceof TextTag && ((Tag) item).isModified());
                break;
            }
            case ACTION_UNDO_CHANGES: {
                TreeItem item = mainPanel.tagTree.getCurrentTreeItem();
                if (item instanceof TextTag) {
                    try {
                        ((Tag) item).undo();
                    } catch (InterruptedException | IOException ex) {
                        Logger.getLogger(TextPanel.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    item.getSwf().clearImageCache();
                    mainPanel.refreshTree();
                }
                break;
            }
        }
    }

    private void textChanged() {
        if (!Configuration.showOldTextDuringTextEditing.get()) {
            return;
        }

        if (textValue.isEditable()) {
            TreeItem item = mainPanel.tagTree.getCurrentTreeItem();
            if (item instanceof TextTag) {
                TextTag textTag = (TextTag) item;
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
                }

                if (!ok) {
                    mainPanel.showTextTagWithNewValue(textTag, null);
                }
            }
        }
    }
}
