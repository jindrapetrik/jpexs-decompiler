/*
 *  Copyright (C) 2010-2018 JPEXS
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
import com.jpexs.decompiler.flash.gui.helpers.TableLayoutHelper;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.TextTag;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import com.jpexs.helpers.Helper;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.filechooser.FileFilter;
import layout.TableLayout;

/**
 *
 * @author JPEXS
 */
public class FontPanel extends JPanel {

    private final MainPanel mainPanel;

    private FontTag fontTag;

    /**
     * Creates new form FontPanel
     *
     * @param mainPanel Main panel
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

    public static ComboBoxModel<FontFamily> getFamilyModel() {
        Set<FontFamily> famSet = new TreeSet<>();
        for (Font f : FontTag.getInstalledFontsByName().values()) {
            famSet.add(new FontFamily(f));
        }
        return new DefaultComboBoxModel<>(new Vector<>(famSet));
    }

    public static ComboBoxModel<FontFace> getFaceModel(FontFamily family) {

        Set<FontFace> faceSet = new TreeSet<>();
        for (Font f : FontTag.getInstalledFontsByFamily().get(family.familyEn).values()) {
            faceSet.add(new FontFace(f));
        }

        return new DefaultComboBoxModel<>(new Vector<>(faceSet));
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
        String oldchars = f.getCharacters();
        for (int ic : selChars) {
            char c = (char) ic;
            if (oldchars.indexOf((int) c) == -1) {
                font = font.deriveFont(f.getFontStyle(), 1024);
                if (!font.canDisplay(c)) {
                    String msg = translate("error.font.nocharacter").replace("%char%", "" + c);
                    Logger.getLogger(FontPanel.class.getName()).log(Level.SEVERE, msg);
                    View.showMessageDialog(null, msg, translate("error"), JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        }

        String[] yesno = new String[]{translate("button.yes"), translate("button.no"), translate("button.yes.all"), translate("button.no.all")};
        boolean yestoall = false;
        boolean notoall = false;
        boolean replaced = false;
        for (int ic : selChars) {
            char c = (char) ic;
            if (oldchars.indexOf((int) c) > -1) {
                int opt = -1;
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
                    opt = 0; // yes
                } else if (notoall) {
                    opt = 1; // no
                }

                if (opt == 1) {
                    continue;
                }

                replaced = true;
            }

            f.addCharacter(c, font);
            oldchars += c;
        }

        if (replaced) {
            if (View.showConfirmDialog(null, translate("message.font.replace.updateTexts"), translate("message.warning"), JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE) == JOptionPane.YES_OPTION) {
                int fontId = ft.getFontId();
                SWF swf = ft.getSwf();
                for (Tag tag : swf.getTags()) {
                    if (tag instanceof TextTag) {
                        TextTag textTag = (TextTag) tag;
                        if (textTag.getFontIds().contains(fontId)) {
                            String text = textTag.getFormattedText(true).text;
                            mainPanel.saveText(textTag, text, null, null);
                        }
                    }
                }
            }
        }
    }

    private void fontRemoveChars(FontTag ft, Set<Integer> selChars) {
        FontTag f = (FontTag) mainPanel.tagTree.getCurrentTreeItem();

        for (int ic : selChars) {
            char c = (char) ic;
            f.removeCharacter(c);
        }
    }

    public void showFontTag(FontTag ft) {
        SWF swf = ft.getSwf();
        fontTag = ft;
        fontNameIntagLabel.setText(ft.getFontNameIntag());
        fontNameTextArea.setText(ft.getFontName());
        fontCopyrightTextArea.setText(ft.getFontCopyright());

        fontIsBoldCheckBox.setSelected(ft.isBold());
        fontIsItalicCheckBox.setSelected(ft.isItalic());
        fontDescentLabel.setText(ft.getDescent() == -1 ? translate("value.unknown") : Integer.toString(ft.getDescent()));
        fontAscentLabel.setText(ft.getAscent() == -1 ? translate("value.unknown") : Integer.toString(ft.getAscent()));
        fontLeadingLabel.setText(ft.getLeading() == -1 ? translate("value.unknown") : Integer.toString(ft.getLeading()));
        String chars = ft.getCharacters();
        fontCharactersTextArea.setText(chars);
        fontCharactersScrollPane.getVerticalScrollBar().scrollRectToVisible(new Rectangle(0, 0, 1, 1));
        setAllowSave(false);

        Font selFont = ft.getSystemFont();
        fontFamilyNameSelection.setSelectedItem(new FontFamily(selFont));
        fontFaceSelection.setSelectedItem(new FontFace(selFont));

        setAllowSave(true);
        setEditable(false);
        boolean readOnly = ((Tag) ft).isReadOnly();
        if (readOnly) {
            addCharsPanel.setVisible(false);
            buttonEdit.setVisible(false);
        }
    }

    private void initComponents() {

        contentScrollPane = new JScrollPane();
        addCharsPanel = new JPanel();
        fontParamsPanel = new JPanel();
        fontNameIntagLabel = new JLabel();
        JScrollPane fontDisplayNameScrollPane = new JScrollPane();
        fontNameTextArea = new JTextArea();
        JLabel jLabel3 = new JLabel();
        JScrollPane fontCopyrightScrollPane = new JScrollPane();
        fontCopyrightTextArea = new JTextArea();
        JLabel jLabel4 = new JLabel();
        fontIsBoldCheckBox = new JCheckBox();
        JLabel jLabel5 = new JLabel();
        fontIsItalicCheckBox = new JCheckBox();
        JLabel jLabel6 = new JLabel();
        fontAscentLabel = new JLabel();
        JLabel jLabel7 = new JLabel();
        fontDescentLabel = new JLabel();
        JLabel jLabel8 = new JLabel();
        fontLeadingLabel = new JLabel();
        JLabel jLabel9 = new JLabel();
        fontCharactersScrollPane = new JScrollPane();
        fontCharactersTextArea = new JTextArea();
        JLabel fontCharsAddLabel = new JLabel();
        fontAddCharactersField = new JTextField();
        fontAddCharsButton = new JButton();
        fontRemoveCharsButton = new JButton();
        fontSourceLabel = new JLabel();
        fontFamilyNameSelection = new JComboBox<>();
        fontFaceSelection = new JComboBox<>();
        fontEmbedButton = new JButton();
        buttonEdit = new JButton();
        buttonSave = new JButton();
        buttonCancel = new JButton();
        buttonPreviewFont = new JButton();
        buttonSetAdvanceValues = new JButton();
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        contentPanel = new JPanel();
        contentScrollPane.setBorder(null);
        contentScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        TableLayout tlFontParamsPanel;
        fontParamsPanel.setLayout(tlFontParamsPanel = new TableLayout(new double[][]{
            {TableLayout.PREFERRED, TableLayout.FILL},
            {TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL,}
        }));

        JLabel fontNameIntagLabLabel = new JLabel();
        fontNameIntagLabLabel.setText(AppStrings.translate("font.name.intag"));
        fontParamsPanel.add(fontNameIntagLabLabel, "0,0,R");

        fontNameIntagLabel.setText(AppStrings.translate("value.unknown"));
        fontNameIntagLabel.setMinimumSize(new Dimension(100, fontNameIntagLabel.getMinimumSize().height));
        fontNameIntagLabel.setPreferredSize(new Dimension(250, fontNameIntagLabel.getPreferredSize().height));
        fontParamsPanel.add(fontNameIntagLabel, "1,0");

        JLabel fontNameNameLabLabel = new JLabel();
        fontNameNameLabLabel.setText(AppStrings.translate("font.name"));
        fontParamsPanel.add(fontNameNameLabLabel, "0,1,R");

        fontDisplayNameScrollPane.setBorder(null);
        fontDisplayNameScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        fontDisplayNameScrollPane.setHorizontalScrollBar(null);

        fontNameTextArea.setEditable(false);
        fontNameTextArea.setColumns(20);
        fontNameTextArea.setFont(new JLabel().getFont());
        fontNameTextArea.setLineWrap(true);
        fontNameTextArea.setText(AppStrings.translate("value.unknown"));
        fontNameTextArea.setWrapStyleWord(true);
        fontNameTextArea.setMinimumSize(new Dimension(100, fontNameTextArea.getMinimumSize().height));
        fontNameTextArea.setOpaque(false);
        fontDisplayNameScrollPane.setViewportView(fontNameTextArea);

        fontParamsPanel.add(fontDisplayNameScrollPane, "1,1");

        jLabel3.setText(AppStrings.translate("fontName.copyright"));
        fontParamsPanel.add(jLabel3, "0,2,R");

        fontCopyrightScrollPane.setBorder(null);
        fontCopyrightScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        fontCopyrightScrollPane.setHorizontalScrollBar(null);

        fontCopyrightTextArea.setEditable(false);
        fontCopyrightTextArea.setColumns(20);
        fontCopyrightTextArea.setFont(new JLabel().getFont());
        fontCopyrightTextArea.setLineWrap(true);
        fontCopyrightTextArea.setText(AppStrings.translate("value.unknown"));
        fontCopyrightTextArea.setWrapStyleWord(true);
        fontCopyrightTextArea.setMinimumSize(new Dimension(100, fontCopyrightTextArea.getMinimumSize().height));
        fontCopyrightTextArea.setOpaque(false);
        fontCopyrightScrollPane.setViewportView(fontCopyrightTextArea);

        fontParamsPanel.add(fontCopyrightScrollPane, "1,2");

        jLabel4.setText(AppStrings.translate("font.isbold"));
        fontParamsPanel.add(jLabel4, "0,3,R");

        fontIsBoldCheckBox.setEnabled(false);

        fontParamsPanel.add(fontIsBoldCheckBox, "1,3");

        jLabel5.setText(AppStrings.translate("font.isitalic"));

        fontParamsPanel.add(jLabel5, "0,4,R");

        fontIsItalicCheckBox.setEnabled(false);
        fontParamsPanel.add(fontIsItalicCheckBox, "1,4");

        jLabel6.setText(AppStrings.translate("font.ascent"));
        fontParamsPanel.add(jLabel6, "0,5,R");

        fontAscentLabel.setText(AppStrings.translate("value.unknown"));
        fontParamsPanel.add(fontAscentLabel, "1,5");

        jLabel7.setText(AppStrings.translate("font.descent"));
        fontParamsPanel.add(jLabel7, "0,6,R");

        fontDescentLabel.setText(AppStrings.translate("value.unknown"));
        fontParamsPanel.add(fontDescentLabel, "1,6");

        jLabel8.setText(AppStrings.translate("font.leading"));
        fontParamsPanel.add(jLabel8, "0,7,R");

        fontLeadingLabel.setText(AppStrings.translate("value.unknown"));
        fontParamsPanel.add(fontLeadingLabel, "1,7");

        jLabel9.setText(AppStrings.translate("font.characters"));
        fontParamsPanel.add(jLabel9, "0,8,R,T");

        fontCharactersScrollPane.setBorder(null);
        fontCharactersScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        fontCharactersScrollPane.setHorizontalScrollBar(null);
        fontCharactersScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        fontCharactersTextArea.setEditable(false);
        fontCharactersTextArea.setColumns(20);
        fontCharactersTextArea.setFont(new JLabel().getFont());
        fontCharactersTextArea.setLineWrap(true);
        fontCharactersTextArea.setWrapStyleWord(true);
        fontCharactersTextArea.setMinimumSize(new Dimension(100, fontCharactersTextArea.getMinimumSize().height));
        fontCharactersTextArea.setOpaque(false);
        fontCharactersScrollPane.setViewportView(fontCharactersTextArea);
        fontParamsPanel.add(fontCharactersScrollPane, "1,8");

        fontCharsAddLabel.setText(AppStrings.translate("font.characters.add"));

        fontAddCharsButton.setText(AppStrings.translate("button.ok"));
        fontAddCharsButton.addActionListener(this::fontAddCharsButtonActionPerformed);

        fontRemoveCharsButton.setText(AppStrings.translate("button.remove"));
        fontRemoveCharsButton.addActionListener(this::fontRemoveCharsButtonActionPerformed);

        fontSourceLabel.setText(AppStrings.translate("font.source"));

        fontFamilyNameSelection.setPreferredSize(new Dimension(100, fontFamilyNameSelection.getMinimumSize().height));
        fontFamilyNameSelection.setModel(getFamilyModel());
        fontFamilyNameSelection.setSelectedItem(FontTag.getDefaultFontName());
        fontFaceSelection.setModel(getFaceModel((FontFamily) fontFamilyNameSelection.getSelectedItem()));
        fontFamilyNameSelection.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent evt) {
                fontFamilySelectionItemStateChanged();
            }
        });

        fontFaceSelection.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent evt) {
                fontFaceSelectionItemStateChanged();
            }
        });

        fontEmbedButton.setText(AppStrings.translate("button.font.embed"));
        fontEmbedButton.addActionListener(this::fontEmbedButtonActionPerformed);

        buttonEdit.setIcon(View.getIcon("edit16"));
        buttonEdit.setText(AppStrings.translate("button.edit"));
        buttonEdit.addActionListener(this::buttonEditActionPerformed);

        buttonSave.setIcon(View.getIcon("save16"));
        buttonSave.setText(AppStrings.translate("button.save"));
        buttonSave.addActionListener(this::buttonSaveActionPerformed);

        buttonCancel.setIcon(View.getIcon("cancel16"));
        buttonCancel.setText(AppStrings.translate("button.cancel"));
        buttonCancel.addActionListener(this::buttonCancelActionPerformed);

        buttonPreviewFont.setText(AppStrings.translate("button.preview"));
        buttonPreviewFont.addActionListener(this::buttonPreviewFontActionPerformed);

        buttonSetAdvanceValues.setText(AppStrings.translate("button.setAdvanceValues"));
        buttonSetAdvanceValues.addActionListener(this::buttonSetAdvanceValuesActionPerformed);

        TableLayout tlAddCharsPanel;
        addCharsPanel.setLayout(tlAddCharsPanel = new TableLayout(new double[][]{
            {TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED},
            {TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED}
        }));
        addCharsPanel.setBorder(BorderFactory.createRaisedBevelBorder());

        addCharsPanel.add(fontCharsAddLabel, "0,0,R");
        addCharsPanel.add(fontAddCharactersField, "1,0,2,0");
        addCharsPanel.add(fontAddCharsButton, "3,0");
        //addCharsPanel.add(fontRemoveCharsButton, "3,0");
        addCharsPanel.add(fontEmbedButton, "4,0");

        addCharsPanel.add(fontSourceLabel, "0,1,R");
        addCharsPanel.add(fontFamilyNameSelection, "1,1");
        addCharsPanel.add(fontFaceSelection, "2,1");
        addCharsPanel.add(buttonPreviewFont, "3,1");
        addCharsPanel.add(buttonSetAdvanceValues, "4,1");

        JPanel buttonsPanel = new JPanel(new FlowLayout());
        buttonsPanel.add(buttonEdit);
        buttonsPanel.add(buttonSave);
        buttonsPanel.add(buttonCancel);

        TableLayout tlAll;
        contentPanel.setLayout(tlAll = new TableLayout(new double[][]{
            {TableLayout.FILL},
            {TableLayout.FILL, TableLayout.PREFERRED, TableLayout.PREFERRED}
        }));

        contentPanel.add(fontParamsPanel, "0,0");
        contentPanel.add(buttonsPanel, "0,1");
        contentPanel.add(addCharsPanel, "0,2");
        contentScrollPane.setViewportView(contentPanel);

        setLayout(new BorderLayout());
        add(contentScrollPane, BorderLayout.CENTER);

        TableLayoutHelper.addTableSpaces(tlAddCharsPanel, 10);
        TableLayoutHelper.addTableSpaces(tlFontParamsPanel, 10);
        TableLayoutHelper.addTableSpaces(tlAll, 10);

        addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                contentPanel.setPreferredSize(new Dimension(Math.max(getSize().width, addCharsPanel.getPreferredSize().width + 20), getSize().height));
                contentPanel.revalidate();
            }

        });
    }

    private void fontAddCharsButtonActionPerformed(ActionEvent evt) {
        String newchars = fontAddCharactersField.getText();

        TreeItem item = mainPanel.tagTree.getCurrentTreeItem();
        if (item instanceof FontTag) {
            FontTag ft = (FontTag) item;
            Set<Integer> selChars = new TreeSet<>();
            for (int c = 0; c < newchars.length(); c++) {
                selChars.add(newchars.codePointAt(c));
            }

            if (!selChars.isEmpty()) {
                fontAddChars(ft, selChars, ((FontFace) fontFaceSelection.getSelectedItem()).font);
                fontAddCharactersField.setText("");
                mainPanel.repaintTree();
            }
        }
    }

    private void fontRemoveCharsButtonActionPerformed(ActionEvent evt) {
        String newchars = fontAddCharactersField.getText();

        TreeItem item = mainPanel.tagTree.getCurrentTreeItem();
        if (item instanceof FontTag) {
            FontTag ft = (FontTag) item;
            Set<Integer> selChars = new TreeSet<>();
            for (int c = 0; c < newchars.length(); c++) {
                selChars.add(newchars.codePointAt(c));
            }

            if (!selChars.isEmpty()) {
                fontRemoveChars(ft, selChars);
                fontAddCharactersField.setText("");
                mainPanel.repaintTree();
            }
        }
    }

    private void fontEmbedButtonActionPerformed(ActionEvent evt) {
        TreeItem item = mainPanel.tagTree.getCurrentTreeItem();
        if (item instanceof FontTag) {
            FontTag ft = (FontTag) item;
            FontEmbedDialog fed = new FontEmbedDialog((FontFace) fontFaceSelection.getSelectedItem(), fontAddCharactersField.getText());
            if (fed.showDialog() == AppDialog.OK_OPTION) {
                Set<Integer> selChars = fed.getSelectedChars();
                if (!selChars.isEmpty()) {
                    Font selFont = fed.getSelectedFont();
                    fontFamilyNameSelection.setSelectedItem(new FontFamily(selFont));
                    fontFaceSelection.setSelectedItem(new FontFace(selFont));
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
            String selectedName = ((FontFace) fontFaceSelection.getSelectedItem()).font.getFontName(Locale.ENGLISH);
            swf.sourceFontNamesMap.put(f.getFontId(), selectedName);
            Configuration.addFontPair(swf.getShortFileName(), f.getFontId(), f.getFontNameIntag(), selectedName);
        }
    }

    private void fontFamilySelectionItemStateChanged() {

        savePair();
        fontFaceSelection.setModel(getFaceModel((FontFamily) fontFamilyNameSelection.getSelectedItem()));
    }

    private void fontFaceSelectionItemStateChanged() {
        savePair();
    }

    private void buttonEditActionPerformed(ActionEvent evt) {
        setEditable(true);
    }

    private void buttonSaveActionPerformed(ActionEvent evt) {
        if (fontTag.isBoldEditable()) {
            fontTag.setBold(fontIsBoldCheckBox.isSelected());
        }
        if (fontTag.isItalicEditable()) {
            fontTag.setItalic(fontIsItalicCheckBox.isSelected());
        }
        setEditable(false);
    }

    private void buttonCancelActionPerformed(ActionEvent evt) {
        showFontTag(fontTag);
        setEditable(false);
    }

    private void buttonPreviewFontActionPerformed(ActionEvent evt) {
        new FontPreviewDialog(null, true, ((FontFace) fontFaceSelection.getSelectedItem()).font).setVisible(true);
    }

    private void buttonSetAdvanceValuesActionPerformed(ActionEvent evt) {
        if (View.showConfirmDialog(null, AppStrings.translate("message.font.setadvancevalues"), AppStrings.translate("message.warning"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, Configuration.showSetAdvanceValuesMessage, JOptionPane.OK_OPTION) == JOptionPane.OK_OPTION) {
            fontTag.setAdvanceValues(((FontFace) fontFaceSelection.getSelectedItem()).font);
        }
    }

    private void formComponentResized(ComponentEvent evt) {
        fontParamsPanel.updateUI();
    }

    private void importTTFButtonActionPerformed(ActionEvent evt) {
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
                    int[] required = new int[]{0x0001, 0x0000, 0x000D, 0x0020};
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

    private JButton buttonCancel;

    private JButton buttonEdit;

    private JButton buttonPreviewFont;

    private JButton buttonSetAdvanceValues;

    private JButton buttonSave;

    private JTextField fontAddCharactersField;

    private JButton fontAddCharsButton;

    private JButton fontRemoveCharsButton;

    private JLabel fontAscentLabel;

    private JScrollPane fontCharactersScrollPane;

    private JTextArea fontCharactersTextArea;

    private JTextArea fontCopyrightTextArea;

    private JLabel fontDescentLabel;

    private JTextArea fontNameTextArea;

    private JButton fontEmbedButton;

    private JCheckBox fontIsBoldCheckBox;

    private JCheckBox fontIsItalicCheckBox;

    private JLabel fontLeadingLabel;

    private JLabel fontNameIntagLabel;

    private JComboBox<FontFamily> fontFamilyNameSelection;

    private JComboBox<FontFace> fontFaceSelection;

    private JLabel fontSourceLabel;

    private JPanel fontParamsPanel;

    private JPanel addCharsPanel;

    private JPanel contentPanel;

    private JScrollPane contentScrollPane;
}
