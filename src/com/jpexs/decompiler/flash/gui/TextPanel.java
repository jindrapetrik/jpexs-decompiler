/*
 *  Copyright (C) 2010-2014 JPEXS
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
import com.jpexs.decompiler.flash.gui.abc.LineMarkedEditorPane;
import com.jpexs.decompiler.flash.tags.base.TextTag;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author JPEXS
 */
public class TextPanel extends JPanel implements ActionListener {

    private static final String ACTION_EDIT_TEXT = "EDITTEXT";
    private static final String ACTION_CANCEL_TEXT = "CANCELTEXT";
    private static final String ACTION_SAVE_TEXT = "SAVETEXT";

    private final MainPanel mainPanel;
    private final SearchPanel<TextTag> textSearchPanel;
    private final LineMarkedEditorPane textValue;
    private final JButton textSaveButton;
    private final JButton textEditButton;
    private final JButton textCancelButton;

    public TextPanel(MainPanel mainPanel) {
        super(new BorderLayout());

        this.mainPanel = mainPanel;
        textSearchPanel = new SearchPanel<>(new FlowLayout(), mainPanel);
        add(textSearchPanel, BorderLayout.NORTH);
        textValue = new LineMarkedEditorPane();
        add(new JScrollPane(textValue), BorderLayout.CENTER);
        textValue.setEditable(false);

        JPanel textButtonsPanel = new JPanel();
        textButtonsPanel.setLayout(new FlowLayout());

        textSaveButton = new JButton(mainPanel.translate("button.save"), View.getIcon("save16"));
        textSaveButton.setMargin(new Insets(3, 3, 3, 10));
        textSaveButton.setActionCommand(ACTION_SAVE_TEXT);
        textSaveButton.addActionListener(this);

        textEditButton = new JButton(mainPanel.translate("button.edit"), View.getIcon("edit16"));
        textEditButton.setMargin(new Insets(3, 3, 3, 10));
        textEditButton.setActionCommand(ACTION_EDIT_TEXT);
        textEditButton.addActionListener(this);

        textCancelButton = new JButton(mainPanel.translate("button.cancel"), View.getIcon("cancel16"));
        textCancelButton.setMargin(new Insets(3, 3, 3, 10));
        textCancelButton.setActionCommand(ACTION_CANCEL_TEXT);
        textCancelButton.addActionListener(this);

        textButtonsPanel.add(textEditButton);
        textButtonsPanel.add(textSaveButton);
        textButtonsPanel.add(textCancelButton);

        textSaveButton.setVisible(false);
        textCancelButton.setVisible(false);

        add(textButtonsPanel, BorderLayout.SOUTH);
    }

    public SearchPanel<TextTag> getSearchPanel() {
        return textSearchPanel;
    }

    public void setText(String text) {
        textValue.setContentType("text/swf_text");
        // textValue.setFont(new Font("Monospaced", Font.PLAIN, 13));
        textValue.setText(text);
        textValue.setCaretPosition(0);
    }

    public void setEditText(boolean edit) {
        textValue.setEditable(edit);
        textSaveButton.setVisible(edit);
        textEditButton.setVisible(!edit);
        textCancelButton.setVisible(edit);
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
                break;
            case ACTION_CANCEL_TEXT:
                setEditText(false);
                mainPanel.reload(true);
                break;
            case ACTION_SAVE_TEXT:
                TreeItem item = mainPanel.tagTree.getCurrentTreeItem();
                if (item instanceof TextTag) {
                    TextTag textTag = (TextTag) item;
                    if (mainPanel.saveText(textTag, textValue.getText(), null)) {
                        setEditText(false);
                        mainPanel.reload(true);
                    }
                    SWF.clearImageCache();
                }
                break;
        }
    }
}
