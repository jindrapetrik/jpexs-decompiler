/*
 *  Copyright (C) 2010-2013 JPEXS
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
import com.jpexs.decompiler.flash.tags.DefineFontNameTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.TextTag;
import java.awt.Font;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 *
 * @author JPEXS
 */
public class FontPanel extends javax.swing.JPanel {

    private MainPanel mainPanel;
    private FontTag fontTag;
    
    /**
     * Creates new form FontPanel
     */
    public FontPanel(MainPanel mainPanel) {
        this.mainPanel = mainPanel;
        initComponents();
    }

    private ComboBoxModel<String> getModel() {
        return new DefaultComboBoxModel<>(FontTag.fontNamesArray);
    }

    private void setEditable(boolean editable) {
        if (editable) {
            buttonEdit.setVisible(false);
            buttonSave.setVisible(true);
            buttonCancel.setVisible(true);
            if (fontTag.isBoldEditable()) {
                fontIsBoldCheckBox.setEnabled(true);
            }
            if (fontTag.isItalicEditable()) {
                fontIsItalicCheckBox.setEnabled(true);
            }
        } else {
            buttonEdit.setVisible(true);
            buttonSave.setVisible(false);
            buttonCancel.setVisible(false);
            fontIsBoldCheckBox.setEnabled(false);
            fontIsItalicCheckBox.setEnabled(false);
        }
    }
    
    private String translate(String key) {
        return mainPanel.translate(key);
    }

    private void fontAddChars(FontTag ft, Set<Integer> selChars, String selFont) {
        FontTag f = (FontTag) mainPanel.oldTag;
        SWF swf = ft.getSwf();
        String oldchars = f.getCharacters(swf.tags);
        for (int ic : selChars) {
            char c = (char) ic;
            if (oldchars.indexOf((int) c) == -1) {
                Font font = new Font(selFont, f.getFontStyle(), 1024);
                if (!font.canDisplay(c)) {
                    View.showMessageDialog(null, translate("error.font.nocharacter").replace("%char%", "" + c), translate("error"), JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        }

        String[] yesno = new String[]{translate("button.yes"), translate("button.no"), translate("button.yes.all"), translate("button.no.all")};
        boolean yestoall = false;
        boolean notoall = false;
        for (int ic : selChars) {
            char c = (char) ic;
            if (oldchars.indexOf((int) c) > -1) {
                int opt; //yes
                if (!(yestoall || notoall)) {
                    opt = View.showOptionDialog(null, translate("message.font.add.exists").replace("%char%", "" + c), translate("message.warning"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, yesno, translate("button.yes"));
                    if (opt == 2) {
                        yestoall = true;
                    }
                    if (opt == 3) {
                        notoall = true;
                    }
                }
                if (yestoall) {
                    opt = 0; //yes                
                } else if (notoall) {
                    opt = 1; //no
                } else {
                    opt = 1;
                }

                if (opt == 1) {
                    continue;
                }
            }
            f.addCharacter(swf.tags, c, fontSelection.getSelectedItem().toString());
            oldchars += c;
        }
        
        int fontId = ft.getFontId();
        if (updateTextsCheckBox.isSelected()) {
            for (Tag tag : swf.tags) {
                if (tag instanceof TextTag) {
                    TextTag textTag = (TextTag) tag;
                    if (textTag.getFontIds(swf.tags).contains(fontId)) {
                        String text = textTag.getFormattedText(textTag.getSwf().tags);
                        mainPanel.saveText(textTag, text);
                    }
                }
            }
        }
    }

    public void showFontTag(FontTag ft) {
        SWF swf = ft.getSwf();
        fontTag = ft;
        DefineFontNameTag fontNameTag = null;
        for (Tag tag : swf.tags) {
            if (tag instanceof DefineFontNameTag) {
                DefineFontNameTag dfnt = (DefineFontNameTag) tag;
                if (dfnt.fontId == ft.getFontId()) {
                    fontNameTag = dfnt;
                }
            }
        }

        fontNameLabel.setText(ft.getFontName());
        if (fontNameTag != null) {
            fontDisplayNameTextArea.setText(fontNameTag.fontName);
            fontCopyrightTextArea.setText(fontNameTag.fontCopyright);
        }
        
        fontIsBoldCheckBox.setSelected(ft.isBold());
        fontIsItalicCheckBox.setSelected(ft.isItalic());
        fontDescentLabel.setText(ft.getDescent() == -1 ? translate("value.unknown") : "" + ft.getDescent());
        fontAscentLabel.setText(ft.getAscent() == -1 ? translate("value.unknown") : "" + ft.getAscent());
        fontLeadingLabel.setText(ft.getLeading() == -1 ? translate("value.unknown") : "" + ft.getLeading());
        String chars = ft.getCharacters(swf.tags);
        fontCharactersTextArea.setText(chars);
        String key = swf.getShortFileName() + "_" + ft.getFontId() + "_" + ft.getFontName();
        if (swf.sourceFontsMap.containsKey(ft.getFontId())) {
            fontSelection.setSelectedItem(swf.sourceFontsMap.get(ft.getFontId()));
        } else if (Configuration.getFontPairs().containsKey(key)) {
            fontSelection.setSelectedItem(Configuration.getFontPairs().get(key));
        } else if (Configuration.getFontPairs().containsKey(ft.getFontName())) {
            fontSelection.setSelectedItem(Configuration.getFontPairs().get(ft.getFontName()));
        } else {
            fontSelection.setSelectedItem(FontTag.findInstalledFontName(ft.getFontName()));
        }
        setEditable(false);
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        fontNameLabel = new javax.swing.JLabel();
        javax.swing.JLabel jLabel2 = new javax.swing.JLabel();
        javax.swing.JScrollPane fontDisplayNameScrollPane = new javax.swing.JScrollPane();
        fontDisplayNameTextArea = new javax.swing.JTextArea();
        javax.swing.JLabel jLabel3 = new javax.swing.JLabel();
        javax.swing.JScrollPane fontCopyrightScrollPane = new javax.swing.JScrollPane();
        fontCopyrightTextArea = new javax.swing.JTextArea();
        javax.swing.JLabel jLabel4 = new javax.swing.JLabel();
        fontIsBoldCheckBox = new javax.swing.JCheckBox();
        javax.swing.JLabel jLabel5 = new javax.swing.JLabel();
        fontIsItalicCheckBox = new javax.swing.JCheckBox();
        javax.swing.JLabel jLabel6 = new javax.swing.JLabel();
        fontAscentLabel = new javax.swing.JLabel();
        javax.swing.JLabel jLabel7 = new javax.swing.JLabel();
        fontDescentLabel = new javax.swing.JLabel();
        javax.swing.JLabel jLabel8 = new javax.swing.JLabel();
        fontLeadingLabel = new javax.swing.JLabel();
        javax.swing.JLabel jLabel9 = new javax.swing.JLabel();
        fontCharactersScrollPane = new javax.swing.JScrollPane();
        fontCharactersTextArea = new javax.swing.JTextArea();
        javax.swing.JLabel jLabel10 = new javax.swing.JLabel();
        fontAddCharactersField = new javax.swing.JTextField();
        fontAddCharsButton = new javax.swing.JButton();
        updateTextsCheckBox = new javax.swing.JCheckBox();
        jLabel11 = new javax.swing.JLabel();
        fontSelection = new javax.swing.JComboBox();
        fontEmbedButton = new javax.swing.JButton();
        buttonEdit = new javax.swing.JButton();
        buttonSave = new javax.swing.JButton();
        buttonCancel = new javax.swing.JButton();

        jPanel1.setLayout(new java.awt.GridBagLayout());

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/jpexs/decompiler/flash/gui/locales/MainFrame"); // NOI18N
        jLabel1.setText(bundle.getString("font.name")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(jLabel1, gridBagConstraints);

        fontNameLabel.setText(bundle.getString("value.unknown")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(fontNameLabel, gridBagConstraints);

        jLabel2.setText(bundle.getString("fontName.name")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(jLabel2, gridBagConstraints);

        fontDisplayNameScrollPane.setBorder(null);

        fontDisplayNameTextArea.setEditable(false);
        fontDisplayNameTextArea.setColumns(20);
        fontDisplayNameTextArea.setFont(new JLabel().getFont());
        fontDisplayNameTextArea.setLineWrap(true);
        fontDisplayNameTextArea.setText(bundle.getString("value.unknown")); // NOI18N
        fontDisplayNameTextArea.setWrapStyleWord(true);
        fontDisplayNameTextArea.setOpaque(false);
        fontDisplayNameScrollPane.setViewportView(fontDisplayNameTextArea);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        jPanel1.add(fontDisplayNameScrollPane, gridBagConstraints);

        jLabel3.setText(bundle.getString("fontName.copyright")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(jLabel3, gridBagConstraints);

        fontCopyrightScrollPane.setBorder(null);

        fontCopyrightTextArea.setEditable(false);
        fontCopyrightTextArea.setColumns(20);
        fontCopyrightTextArea.setFont(new JLabel().getFont());
        fontCopyrightTextArea.setLineWrap(true);
        fontCopyrightTextArea.setText(bundle.getString("value.unknown")); // NOI18N
        fontCopyrightTextArea.setWrapStyleWord(true);
        fontCopyrightTextArea.setOpaque(false);
        fontCopyrightScrollPane.setViewportView(fontCopyrightTextArea);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        jPanel1.add(fontCopyrightScrollPane, gridBagConstraints);

        jLabel4.setText(bundle.getString("font.isbold")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(jLabel4, gridBagConstraints);

        fontIsBoldCheckBox.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        jPanel1.add(fontIsBoldCheckBox, gridBagConstraints);

        jLabel5.setText(bundle.getString("font.isitalic")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(jLabel5, gridBagConstraints);

        fontIsItalicCheckBox.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        jPanel1.add(fontIsItalicCheckBox, gridBagConstraints);

        jLabel6.setText(bundle.getString("font.ascent")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(jLabel6, gridBagConstraints);

        fontAscentLabel.setText(bundle.getString("value.unknown")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        jPanel1.add(fontAscentLabel, gridBagConstraints);

        jLabel7.setText(bundle.getString("font.descent")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(jLabel7, gridBagConstraints);

        fontDescentLabel.setText(bundle.getString("value.unknown")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        jPanel1.add(fontDescentLabel, gridBagConstraints);

        jLabel8.setText(bundle.getString("font.leading")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(jLabel8, gridBagConstraints);

        fontLeadingLabel.setText(bundle.getString("value.unknown")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        jPanel1.add(fontLeadingLabel, gridBagConstraints);

        jLabel9.setText(bundle.getString("font.characters")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(jLabel9, gridBagConstraints);

        fontCharactersScrollPane.setBorder(null);

        fontCharactersTextArea.setEditable(false);
        fontCharactersTextArea.setColumns(20);
        fontCharactersTextArea.setFont(new JLabel().getFont());
        fontCharactersTextArea.setLineWrap(true);
        fontCharactersTextArea.setWrapStyleWord(true);
        fontCharactersTextArea.setOpaque(false);
        fontCharactersScrollPane.setViewportView(fontCharactersTextArea);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        jPanel1.add(fontCharactersScrollPane, gridBagConstraints);

        jLabel10.setText(bundle.getString("font.characters.add")); // NOI18N

        fontAddCharsButton.setText(bundle.getString("button.ok")); // NOI18N
        fontAddCharsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fontAddCharsButtonActionPerformed(evt);
            }
        });

        updateTextsCheckBox.setText(bundle.getString("font.updateTexts")); // NOI18N

        jLabel11.setText(bundle.getString("font.source")); // NOI18N

        fontSelection.setModel(getModel());
        fontSelection.setSelectedItem(FontTag.defaultFontName);
        fontSelection.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fontSelectionItemStateChanged(evt);
            }
        });

        fontEmbedButton.setText(bundle.getString("button.font.embed")); // NOI18N
        fontEmbedButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fontEmbedButtonActionPerformed(evt);
            }
        });

        buttonEdit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/jpexs/decompiler/flash/gui/graphics/edit16.png"))); // NOI18N
        buttonEdit.setText(bundle.getString("button.edit")); // NOI18N
        buttonEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonEditActionPerformed(evt);
            }
        });

        buttonSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/jpexs/decompiler/flash/gui/graphics/save16.png"))); // NOI18N
        buttonSave.setText(bundle.getString("button.save")); // NOI18N
        buttonSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSaveActionPerformed(evt);
            }
        });

        buttonCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/jpexs/decompiler/flash/gui/graphics/cancel16.png"))); // NOI18N
        buttonCancel.setText(bundle.getString("button.cancel")); // NOI18N
        buttonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCancelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel11)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(fontSelection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel10)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(fontAddCharactersField, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(fontAddCharsButton)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(updateTextsCheckBox))
                                    .addComponent(fontEmbedButton))
                                .addGap(0, 68, Short.MAX_VALUE)))
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(buttonEdit)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(buttonSave)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(buttonCancel)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(fontAddCharactersField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fontAddCharsButton)
                    .addComponent(updateTextsCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(fontSelection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fontEmbedButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 30, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonEdit)
                    .addComponent(buttonSave)
                    .addComponent(buttonCancel))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void fontAddCharsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fontAddCharsButtonActionPerformed
        String newchars = fontAddCharactersField.getText();
        if (mainPanel.oldTag instanceof FontTag) {
            Set<Integer> selChars = new TreeSet<>();
            for (int c = 0; c < newchars.length(); c++) {
                selChars.add(newchars.codePointAt(c));
            }
            fontAddChars((FontTag) mainPanel.oldTag, selChars, fontSelection.getSelectedItem().toString());
            fontAddCharactersField.setText("");
            mainPanel.reload(true);
        }
    }//GEN-LAST:event_fontAddCharsButtonActionPerformed

    private void fontEmbedButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fontEmbedButtonActionPerformed
        if (mainPanel.oldTag instanceof FontTag) {
            FontEmbedDialog fed = new FontEmbedDialog(fontSelection.getSelectedItem().toString(), fontAddCharactersField.getText(), ((FontTag) mainPanel.oldTag).getFontStyle());
            if (fed.display()) {
                Set<Integer> selChars = fed.getSelectedChars();
                if (!selChars.isEmpty()) {
                    String selFont = fed.getSelectedFont();
                    fontSelection.setSelectedItem(selFont);
                    fontAddChars((FontTag) mainPanel.oldTag, selChars, selFont);
                    fontAddCharactersField.setText("");
                    mainPanel.reload(true);
                }
            }
        }
    }//GEN-LAST:event_fontEmbedButtonActionPerformed

    private void fontSelectionItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fontSelectionItemStateChanged
        if (mainPanel.oldTag instanceof FontTag) {
            FontTag f = (FontTag) mainPanel.oldTag;
            SWF swf = f.getSwf();
            String selectedSystemFont = (String) fontSelection.getSelectedItem();
            swf.sourceFontsMap.put(f.getFontId(), selectedSystemFont);
            Configuration.addFontPair(swf.getShortFileName(), f.getFontId(), f.getFontName(), selectedSystemFont);
        }
    }//GEN-LAST:event_fontSelectionItemStateChanged

    private void buttonEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonEditActionPerformed
        setEditable(true);
    }//GEN-LAST:event_buttonEditActionPerformed

    private void buttonSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSaveActionPerformed
        if (fontTag.isBoldEditable()) {
            fontTag.setBold(fontIsBoldCheckBox.isSelected());
        }
        if (fontTag.isItalicEditable()) {
            fontTag.setItalic(fontIsItalicCheckBox.isSelected());
        }
        setEditable(false);
    }//GEN-LAST:event_buttonSaveActionPerformed

    private void buttonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCancelActionPerformed
        showFontTag(fontTag);
        setEditable(false);
    }//GEN-LAST:event_buttonCancelActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonCancel;
    private javax.swing.JButton buttonEdit;
    private javax.swing.JButton buttonSave;
    private javax.swing.JTextField fontAddCharactersField;
    private javax.swing.JButton fontAddCharsButton;
    private javax.swing.JLabel fontAscentLabel;
    private javax.swing.JScrollPane fontCharactersScrollPane;
    private javax.swing.JTextArea fontCharactersTextArea;
    private javax.swing.JTextArea fontCopyrightTextArea;
    private javax.swing.JLabel fontDescentLabel;
    private javax.swing.JTextArea fontDisplayNameTextArea;
    private javax.swing.JButton fontEmbedButton;
    private javax.swing.JCheckBox fontIsBoldCheckBox;
    private javax.swing.JCheckBox fontIsItalicCheckBox;
    private javax.swing.JLabel fontLeadingLabel;
    private javax.swing.JLabel fontNameLabel;
    private javax.swing.JComboBox fontSelection;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JCheckBox updateTextsCheckBox;
    // End of variables declaration//GEN-END:variables
}
