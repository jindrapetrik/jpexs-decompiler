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
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.helpers.FontHelper;
import com.jpexs.decompiler.flash.tags.DefineFontNameTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.TextTag;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import com.jpexs.helpers.Helper;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import layout.TableLayout;

/**
 *
 * @author JPEXS
 */
public class FontPanel extends javax.swing.JPanel {

    private final MainPanel mainPanel;
    private FontTag fontTag;

    /**
     * Creates new form FontPanel
     *
     * @param mainPanel
     */
    public FontPanel(MainPanel mainPanel) {
        this.mainPanel = mainPanel;
        initComponents();
    }

    public FontTag getFontTag() {
        return fontTag;
    }

    public void clear() {
        fontTag = null;
    }

    private ComboBoxModel<String> getFamilyModel() {
        return new DefaultComboBoxModel<>(new Vector<String>(new TreeSet<String>(FontTag.installedFonts.keySet())));
    }

    private ComboBoxModel<String> getNameModel(String family) {
        return new DefaultComboBoxModel<>(new Vector<String>(FontTag.installedFonts.get(family).keySet()));
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

    private void fontAddChars(FontTag ft, Set<Integer> selChars, Font font) {
        FontTag f = (FontTag) mainPanel.tagTree.getCurrentTreeItem();
        SWF swf = ft.getSwf();
        String oldchars = f.getCharacters(swf.tags);
        for (int ic : selChars) {
            char c = (char) ic;
            if (oldchars.indexOf((int) c) == -1) {
                font = font.deriveFont(f.getFontStyle(), 1024);
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
            f.addCharacter(c, font);
            oldchars += c;
        }

        int fontId = ft.getFontId();
        if (updateTextsCheckBox.isSelected()) {
            for (Tag tag : swf.tags) {
                if (tag instanceof TextTag) {
                    TextTag textTag = (TextTag) tag;
                    if (textTag.getFontIds().contains(fontId)) {
                        String text = textTag.getFormattedText();
                        mainPanel.saveText(textTag, text, null);
                    }
                }
            }
        }
        ft.setModified(true);
        SWF.clearImageCache();
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
        setAllowSave(false);
        String key = swf.getShortFileName() + "_" + ft.getFontId() + "_" + ft.getFontName();
        if (swf.sourceFontFamiliesMap.containsKey(ft.getFontId())) {
            fontFamilyNameSelection.setSelectedItem(swf.sourceFontFamiliesMap.get(ft.getFontId()));
        } else if (Configuration.getFontIdToFamilyMap().containsKey(key)) {
            fontFamilyNameSelection.setSelectedItem(Configuration.getFontIdToFamilyMap().get(key));
        } else if (Configuration.getFontIdToFamilyMap().containsKey(ft.getFontName())) {
            fontFamilyNameSelection.setSelectedItem(Configuration.getFontIdToFamilyMap().get(ft.getFontName()));
        } else {
            fontFamilyNameSelection.setSelectedItem(FontTag.findInstalledFontFamily(ft.getFontName()));
        }

        if (swf.sourceFontFacesMap.containsKey(ft.getFontId())) {
            fontFaceSelection.setSelectedItem(swf.sourceFontFacesMap.get(ft.getFontId()));
        } else if (Configuration.getFontIdToFaceMap().containsKey(key)) {
            fontFaceSelection.setSelectedItem(Configuration.getFontIdToFaceMap().get(key));
        } else if (Configuration.getFontIdToFaceMap().containsKey(ft.getFontName())) {
            fontFaceSelection.setSelectedItem(Configuration.getFontIdToFaceMap().get(ft.getFontName()));
        } else {
            java.util.Map<String, Font> faces = FontTag.installedFonts.get(fontFamilyNameSelection.getSelectedItem().toString());
            boolean found = false;
            for (String face : faces.keySet()) {
                Font f = faces.get(face);
                if (f.isBold() == ft.isBold() && f.isItalic() == ft.isItalic()) {
                    found = true;
                    fontFaceSelection.setSelectedItem(face);
                    break;
                }
            }
            if (!found) {
                fontFaceSelection.setSelectedItem("");
            }
        }
        setAllowSave(true);
        setEditable(false);
    }

    private static void addTableSpaces(TableLayout tl, double size) {
        int cols = tl.getNumColumn();
        int rows = tl.getNumRow();
        for (int x = 0; x <= cols; x++) {
            tl.insertColumn(x * 2, size);
        }
        for (int y = 0; y <= rows; y++) {
            tl.insertRow(y * 2, size);
        }
    }

    private void initComponents() {

        addCharsPanel = new javax.swing.JPanel();
        fontParamsPanel = new javax.swing.JPanel();
        fontNameLabel = new javax.swing.JLabel();
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
        javax.swing.JLabel fontCharsAddLabel = new javax.swing.JLabel();
        fontAddCharactersField = new javax.swing.JTextField();
        fontAddCharsButton = new javax.swing.JButton();
        updateTextsCheckBox = new javax.swing.JCheckBox();
        fontSourceLabel = new javax.swing.JLabel();
        fontFamilyNameSelection = new javax.swing.JComboBox<>();
        fontFaceSelection = new javax.swing.JComboBox<>();
        fontEmbedButton = new javax.swing.JButton();
        buttonEdit = new javax.swing.JButton();
        buttonSave = new javax.swing.JButton();
        buttonCancel = new javax.swing.JButton();
        buttonPreviewFont = new javax.swing.JButton();
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        TableLayout tlFontParamsPanel;
        fontParamsPanel.setLayout(tlFontParamsPanel = new TableLayout(new double[][]{
            {TableLayout.PREFERRED, TableLayout.FILL},
            {TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED,}
        }));

        JLabel fontNameLabLabel = new JLabel();
        fontNameLabLabel.setText(AppStrings.translate("font.name")); // NOI18N
        fontParamsPanel.add(fontNameLabLabel, "0,0,R");

        fontNameLabel.setText(AppStrings.translate("value.unknown")); // NOI18N
        fontNameLabel.setMaximumSize(new java.awt.Dimension(250, 14));
        fontNameLabel.setMinimumSize(new java.awt.Dimension(250, 14));
        fontNameLabel.setPreferredSize(new java.awt.Dimension(250, 14));
        fontParamsPanel.add(fontNameLabel, "1,0");

        JLabel fontNameNameLabLabel = new JLabel();
        fontNameNameLabLabel.setText(AppStrings.translate("fontName.name")); // NOI18N        
        fontParamsPanel.add(fontNameNameLabLabel, "0,1,R");

        fontDisplayNameScrollPane.setBorder(null);
        fontDisplayNameScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        fontDisplayNameScrollPane.setHorizontalScrollBar(null);

        fontDisplayNameTextArea.setEditable(false);
        fontDisplayNameTextArea.setColumns(20);
        fontDisplayNameTextArea.setFont(new JLabel().getFont());
        fontDisplayNameTextArea.setLineWrap(true);
        fontDisplayNameTextArea.setText(AppStrings.translate("value.unknown")); // NOI18N
        fontDisplayNameTextArea.setWrapStyleWord(true);
        fontDisplayNameTextArea.setMinimumSize(new java.awt.Dimension(250, 16));
        fontDisplayNameTextArea.setOpaque(false);
        fontDisplayNameScrollPane.setViewportView(fontDisplayNameTextArea);

        fontParamsPanel.add(fontDisplayNameScrollPane, "1,1");

        jLabel3.setText(AppStrings.translate("fontName.copyright")); // NOI18N
        fontParamsPanel.add(jLabel3, "0,2,R");

        fontCopyrightScrollPane.setBorder(null);
        fontCopyrightScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        fontCopyrightScrollPane.setHorizontalScrollBar(null);

        fontCopyrightTextArea.setEditable(false);
        fontCopyrightTextArea.setColumns(20);
        fontCopyrightTextArea.setFont(new JLabel().getFont());
        fontCopyrightTextArea.setLineWrap(true);
        fontCopyrightTextArea.setText(AppStrings.translate("value.unknown")); // NOI18N
        fontCopyrightTextArea.setWrapStyleWord(true);
        fontCopyrightTextArea.setMinimumSize(new java.awt.Dimension(250, 16));
        fontCopyrightTextArea.setOpaque(false);
        fontCopyrightScrollPane.setViewportView(fontCopyrightTextArea);

        fontParamsPanel.add(fontCopyrightScrollPane, "1,2");

        jLabel4.setText(AppStrings.translate("font.isbold")); // NOI18N       
        fontParamsPanel.add(jLabel4, "0,3,R");

        fontIsBoldCheckBox.setEnabled(false);

        fontParamsPanel.add(fontIsBoldCheckBox, "1,3");

        jLabel5.setText(AppStrings.translate("font.isitalic")); // NOI18N

        fontParamsPanel.add(jLabel5, "0,4,R");

        fontIsItalicCheckBox.setEnabled(false);
        fontParamsPanel.add(fontIsItalicCheckBox, "1,4");

        jLabel6.setText(AppStrings.translate("font.ascent")); // NOI18N
        fontParamsPanel.add(jLabel6, "0,5,R");

        fontAscentLabel.setText(AppStrings.translate("value.unknown")); // NOI18N
        fontParamsPanel.add(fontAscentLabel, "1,5");

        jLabel7.setText(AppStrings.translate("font.descent")); // NOI18N        
        fontParamsPanel.add(jLabel7, "0,6,R");

        fontDescentLabel.setText(AppStrings.translate("value.unknown")); // NOI18N
        fontParamsPanel.add(fontDescentLabel, "1,6");

        jLabel8.setText(AppStrings.translate("font.leading")); // NOI18N
        fontParamsPanel.add(jLabel8, "0,7,R");

        fontLeadingLabel.setText(AppStrings.translate("value.unknown")); // NOI18N        
        fontParamsPanel.add(fontLeadingLabel, "1,7");

        jLabel9.setText(AppStrings.translate("font.characters")); // NOI18N        
        fontParamsPanel.add(jLabel9, "0,8,R");

        fontCharactersScrollPane.setBorder(null);
        fontCharactersScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        fontCharactersScrollPane.setHorizontalScrollBar(null);

        fontCharactersTextArea.setEditable(false);
        fontCharactersTextArea.setColumns(20);
        fontCharactersTextArea.setFont(new JLabel().getFont());
        fontCharactersTextArea.setLineWrap(true);
        fontCharactersTextArea.setWrapStyleWord(true);
        fontCharactersTextArea.setMinimumSize(new java.awt.Dimension(250, 16));
        fontCharactersTextArea.setOpaque(false);
        fontCharactersScrollPane.setViewportView(fontCharactersTextArea);
        fontParamsPanel.add(fontCharactersScrollPane, "1,8");

        fontCharsAddLabel.setText(AppStrings.translate("font.characters.add")); // NOI18N

        fontAddCharsButton.setText(AppStrings.translate("button.ok")); // NOI18N
        fontAddCharsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fontAddCharsButtonActionPerformed(evt);
            }
        });

        updateTextsCheckBox.setText(AppStrings.translate("font.updateTexts")); // NOI18N

        fontSourceLabel.setText(AppStrings.translate("font.source")); // NOI18N

        fontFamilyNameSelection.setModel(getFamilyModel());
        fontFamilyNameSelection.setSelectedItem(FontTag.defaultFontName);
        fontFaceSelection.setModel(getNameModel((String) fontFamilyNameSelection.getSelectedItem()));
        fontFamilyNameSelection.addItemListener(new java.awt.event.ItemListener() {
            @Override
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fontFamilySelectionItemStateChanged();
            }
        });

        fontFaceSelection.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent evt) {
                fontFaceSelectionItemStateChanged();
            }
        });

        fontEmbedButton.setText(AppStrings.translate("button.font.embed")); // NOI18N
        fontEmbedButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fontEmbedButtonActionPerformed(evt);
            }
        });

        buttonEdit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/jpexs/decompiler/flash/gui/graphics/edit16.png"))); // NOI18N
        buttonEdit.setText(AppStrings.translate("button.edit")); // NOI18N
        buttonEdit.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonEditActionPerformed(evt);
            }
        });

        buttonSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/jpexs/decompiler/flash/gui/graphics/save16.png"))); // NOI18N
        buttonSave.setText(AppStrings.translate("button.save")); // NOI18N
        buttonSave.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSaveActionPerformed(evt);
            }
        });

        buttonCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/jpexs/decompiler/flash/gui/graphics/cancel16.png"))); // NOI18N
        buttonCancel.setText(AppStrings.translate("button.cancel")); // NOI18N
        buttonCancel.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCancelActionPerformed(evt);
            }
        });

        buttonPreviewFont.setText(AppStrings.translate("button.preview")); // NOI18N
        buttonPreviewFont.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonPreviewFontActionPerformed(evt);
            }
        });

        TableLayout tlAddCharsPanel;
        addCharsPanel.setLayout(tlAddCharsPanel = new TableLayout(new double[][]{
            {TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED},
            {TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED}
        }));
        addCharsPanel.setBorder(BorderFactory.createRaisedBevelBorder());

        addCharsPanel.add(fontCharsAddLabel, "0,0,R");
        addCharsPanel.add(fontAddCharactersField, "1,0,2,0");
        addCharsPanel.add(fontAddCharsButton, "3,0");
        addCharsPanel.add(fontEmbedButton, "4,0");

        addCharsPanel.add(fontSourceLabel, "0,1,R");
        addCharsPanel.add(fontFamilyNameSelection, "1,1");
        addCharsPanel.add(fontFaceSelection, "2,1");
        addCharsPanel.add(buttonPreviewFont, "3,1");

        addCharsPanel.add(updateTextsCheckBox, "0,2,2,2");

        JPanel buttonsPanel = new JPanel(new FlowLayout());
        buttonsPanel.add(buttonEdit);
        buttonsPanel.add(buttonSave);
        buttonsPanel.add(buttonCancel);

        TableLayout tlAll;
        setLayout(tlAll = new TableLayout(new double[][]{
            {TableLayout.FILL},
            {TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED}
        }));

        add(fontParamsPanel, "0,0");
        add(buttonsPanel, "0,1");
        add(addCharsPanel, "0,2");

        addTableSpaces(tlAddCharsPanel, 10);
        addTableSpaces(tlFontParamsPanel, 10);
        addTableSpaces(tlAll, 10);

    }

    private void labsize(JLabel lab) {
        lab.setPreferredSize(new Dimension(lab.getFontMetrics(lab.getFont()).stringWidth(lab.getText()) + 30, lab.getPreferredSize().height));
        lab.setMinimumSize(lab.getPreferredSize());
    }

    private void fontAddCharsButtonActionPerformed(java.awt.event.ActionEvent evt) {
        String newchars = fontAddCharactersField.getText();

        TreeItem item = mainPanel.tagTree.getCurrentTreeItem();
        if (item instanceof FontTag) {
            Set<Integer> selChars = new TreeSet<>();
            for (int c = 0; c < newchars.length(); c++) {
                selChars.add(newchars.codePointAt(c));
            }
            fontAddChars((FontTag) item, selChars, FontTag.installedFonts.get(fontFamilyNameSelection.getSelectedItem().toString()).get(fontFaceSelection.getSelectedItem().toString()));
            fontAddCharactersField.setText("");
            mainPanel.reload(true);
        }
    }

    private void fontEmbedButtonActionPerformed(java.awt.event.ActionEvent evt) {
        TreeItem item = mainPanel.tagTree.getCurrentTreeItem();
        if (item instanceof FontTag) {
            FontTag ft = (FontTag) item;
            FontEmbedDialog fed = new FontEmbedDialog(fontFamilyNameSelection.getSelectedItem().toString(), fontFaceSelection.getSelectedItem().toString(), fontAddCharactersField.getText());
            if (fed.display()) {
                Set<Integer> selChars = fed.getSelectedChars();
                if (!selChars.isEmpty()) {
                    Font selFont = fed.getSelectedFont();
                    updateTextsCheckBox.setSelected(fed.hasUpdateTexts());
                    fontFamilyNameSelection.setSelectedItem(selFont.getName());
                    fontFaceSelection.setSelectedItem(FontHelper.getFontFace(selFont));
                    fontAddChars(ft, selChars, selFont);
                    fontAddCharactersField.setText("");
                    mainPanel.reload(true);
                }
            }
        }
    }

    private boolean allowSave = true;

    private synchronized void setAllowSave(boolean v) {
        allowSave = v;
    }

    private synchronized void savePair() {
        if (!allowSave) {
            return;
        }
        TreeItem item = mainPanel.tagTree.getCurrentTreeItem();
        if (item instanceof FontTag) {
            FontTag f = (FontTag) item;
            SWF swf = f.getSwf();
            String selectedFamily = (String) fontFamilyNameSelection.getSelectedItem();
            String selectedFace = (String) fontFaceSelection.getSelectedItem();
            swf.sourceFontFamiliesMap.put(f.getFontId(), selectedFamily);
            swf.sourceFontFacesMap.put(f.getFontId(), selectedFace);
            Configuration.addFontPair(swf.getShortFileName(), f.getFontId(), f.getFontName(), selectedFamily, selectedFace);
        }
    }

    private void fontFamilySelectionItemStateChanged() {

        savePair();
        fontFaceSelection.setModel(getNameModel((String) fontFamilyNameSelection.getSelectedItem()));
    }

    private void fontFaceSelectionItemStateChanged() {
        savePair();
    }

    private void buttonEditActionPerformed(java.awt.event.ActionEvent evt) {
        setEditable(true);
    }

    private void buttonSaveActionPerformed(java.awt.event.ActionEvent evt) {
        if (fontTag.isBoldEditable()) {
            fontTag.setBold(fontIsBoldCheckBox.isSelected());
        }
        if (fontTag.isItalicEditable()) {
            fontTag.setItalic(fontIsItalicCheckBox.isSelected());
        }
        setEditable(false);
    }

    private void buttonCancelActionPerformed(java.awt.event.ActionEvent evt) {
        showFontTag(fontTag);
        setEditable(false);
    }

    private void buttonPreviewFontActionPerformed(java.awt.event.ActionEvent evt) {
        String familyName = (String) fontFamilyNameSelection.getSelectedItem();
        String face = (String) fontFaceSelection.getSelectedItem();
        new FontPreviewDialog(null, true, FontTag.installedFonts.get(familyName).get(face)).setVisible(true);
    }

    private void formComponentResized(java.awt.event.ComponentEvent evt) {
        fontParamsPanel.updateUI();
    }

    private void importTTFButtonActionPerformed(java.awt.event.ActionEvent evt) {
        TreeItem item = mainPanel.tagTree.getCurrentTreeItem();
        if (item instanceof FontTag) {
            FontTag ft = (FontTag) item;

            JFileChooser fc = new JFileChooser();
            fc.setCurrentDirectory(new File(Configuration.lastOpenDir.get()));
            FileFilter ttfFilter = new FileFilter() {
                @Override
                public boolean accept(File f) {
                    return (f.getName().toLowerCase().endsWith(".ttf")) || (f.isDirectory());
                }

                @Override
                public String getDescription() {
                    return "TTF files";
                }
            };
            fc.setFileFilter(ttfFilter);

            fc.setAcceptAllFileFilterUsed(false);
            JFrame fr = new JFrame();
            View.setWindowIcon(fr);
            int returnVal = fc.showOpenDialog(fr);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                Configuration.lastOpenDir.set(Helper.fixDialogFile(fc.getSelectedFile()).getParentFile().getAbsolutePath());
                File selfile = Helper.fixDialogFile(fc.getSelectedFile());
                Set<Integer> selChars = new HashSet<>();
                try {
                    Font f = Font.createFont(Font.TRUETYPE_FONT, selfile);
                    int required[] = new int[]{0x0001, 0x0000, 0x000D, 0x0020};
                    loopi:
                    for (char i = 0; i < Character.MAX_VALUE; i++) {
                        for (int r : required) {
                            if (r == i) {
                                continue loopi;
                            }
                        }
                        if (f.canDisplay((int) i)) {
                            selChars.add((int) i);
                        }
                    }
                    fontAddChars(ft, selChars, f);
                    mainPanel.reload(true);
                } catch (FontFormatException ex) {
                    JOptionPane.showMessageDialog(mainPanel, "Invalid TTF font");
                } catch (IOException ex) {
                    Logger.getLogger(FontPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private javax.swing.JButton buttonCancel;
    private javax.swing.JButton buttonEdit;
    private javax.swing.JButton buttonPreviewFont;
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
    private javax.swing.JComboBox<String> fontFamilyNameSelection;
    private javax.swing.JComboBox<String> fontFaceSelection;
    private javax.swing.JLabel fontSourceLabel;
    private javax.swing.JPanel fontParamsPanel;
    private javax.swing.JPanel addCharsPanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JCheckBox updateTextsCheckBox;
}
