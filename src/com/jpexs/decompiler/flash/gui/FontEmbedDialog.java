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
import com.jpexs.decompiler.flash.tags.font.CharacterRanges;
import com.jpexs.helpers.Helper;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author JPEXS
 */
public class FontEmbedDialog extends AppDialog {

    private static final int SAMPLE_MAX_LENGTH = 50;

    private final JComboBox<FontFamily> familyNamesSelection;

    private final JComboBox<FontFace> faceSelection;

    private final JCheckBox[] rangeCheckboxes;

    private final String rangeNames[];

    private final JLabel[] rangeSamples;

    private final JTextField individualCharsField;

    private int result = ERROR_OPTION;

    private JLabel individialSample;

    private Font customFont;

    private final JCheckBox allCheckbox;

    private final JCheckBox updateTextsCheckbox;

    public Font getSelectedFont() {
        if (ttfFileRadio.isSelected() && customFont != null) {
            return customFont;
        }
        return ((FontFace) faceSelection.getSelectedItem()).font;
    }

    public boolean hasUpdateTexts() {
        return updateTextsCheckbox.isSelected();
    }

    public Set<Integer> getSelectedChars() {
        Set<Integer> chars = new TreeSet<>();
        Font f = getSelectedFont();
        if (allCheckbox.isSelected()) {
            for (int i = 0; i < rangeCheckboxes.length; i++) {
                int codes[] = CharacterRanges.rangeCodes(i);
                for (int c : codes) {
                    if (f.canDisplay(c)) {
                        chars.add(c);
                    }
                }
            }
        } else {
            for (int i = 0; i < rangeCheckboxes.length; i++) {
                if (rangeCheckboxes[i].isSelected()) {
                    int codes[] = CharacterRanges.rangeCodes(i);
                    for (int c : codes) {
                        if (f.canDisplay(c)) {
                            chars.add(c);
                        }
                    }
                }
            }
            String indStr = individualCharsField.getText();
            for (int i = 0; i < indStr.length(); i++) {
                if (f.canDisplay(indStr.codePointAt(i))) {
                    chars.add(indStr.codePointAt(i));
                }
            }
        }
        return chars;
    }

    private JRadioButton ttfFileRadio;

    private JRadioButton installedRadio;

    private void updateFaceSelection() {
        faceSelection.setModel(FontPanel.getFaceModel((FontFamily) familyNamesSelection.getSelectedItem()));
    }

    public FontEmbedDialog(FontFace selectedFace, String selectedChars) {
        setSize(900, 600);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setTitle(translate("dialog.title"));

        Container cnt = getContentPane();
        cnt.setLayout(new BoxLayout(cnt, BoxLayout.Y_AXIS));

        JPanel selFontPanel = new JPanel(new FlowLayout());

        installedRadio = new JRadioButton(translate("installed"));
        ttfFileRadio = new JRadioButton(translate("ttffile.noselection"));

        ButtonGroup bg = new ButtonGroup();
        bg.add(installedRadio);
        bg.add(ttfFileRadio);

        installedRadio.setSelected(true);

        individialSample = new JLabel();
        familyNamesSelection = new JComboBox<>(FontPanel.getFamilyModel());
        familyNamesSelection.setSelectedItem(new FontFamily(selectedFace.font));
        faceSelection = new JComboBox<>();
        updateFaceSelection();
        faceSelection.setSelectedItem(selectedFace);
        JButton loadFromDiskButton = new JButton(View.getIcon("open16"));
        loadFromDiskButton.setToolTipText(translate("button.loadfont"));
        loadFromDiskButton.addActionListener(this::loadFromDiscButtonActionPerformed);
        selFontPanel.add(installedRadio);
        selFontPanel.add(familyNamesSelection);
        selFontPanel.add(faceSelection);
        selFontPanel.add(ttfFileRadio);
        selFontPanel.add(loadFromDiskButton);

        installedRadio.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    updateCheckboxes();
                }
            }
        });

        ttfFileRadio.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    if (ttfFileRadio.isSelected()) {
                        if (customFont == null) {
                            if (loadFromDisk()) {
                                updateCheckboxes();
                            } else {
                                installedRadio.setSelected(true);
                            }
                        } else {
                            updateCheckboxes();
                        }
                    }
                }
            }
        });

        cnt.add(selFontPanel);
        JPanel rangesPanel = new JPanel();
        rangesPanel.setLayout(new BoxLayout(rangesPanel, BoxLayout.Y_AXIS));
        final int rc = CharacterRanges.rangeCount();
        rangeCheckboxes = new JCheckBox[rc];
        rangeSamples = new JLabel[rc];
        rangeNames = new String[rc];
        allCheckbox = new JCheckBox(translate("allcharacters"));
        allCheckbox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    for (int i = 0; i < rc; i++) {
                        rangeCheckboxes[i].setEnabled(false);
                    }
                    individualCharsField.setEnabled(false);
                } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                    for (int i = 0; i < rc; i++) {
                        rangeCheckboxes[i].setEnabled(true);
                    }
                    individualCharsField.setEnabled(true);
                }
            }
        });
        JPanel rangeRowPanel = new JPanel();
        rangeRowPanel.setLayout(new BorderLayout());
        rangeRowPanel.add(allCheckbox, BorderLayout.WEST);
        rangeRowPanel.setAlignmentX(0);
        rangesPanel.add(rangeRowPanel);

        for (int i = 0; i < rc; i++) {
            rangeNames[i] = CharacterRanges.rangeName(i);
            rangeSamples[i] = new JLabel("");
            rangeCheckboxes[i] = new JCheckBox(rangeNames[i]);
            rangeRowPanel = new JPanel();
            rangeRowPanel.setLayout(new BoxLayout(rangeRowPanel, BoxLayout.X_AXIS));
            rangeRowPanel.add(rangeCheckboxes[i]);
            rangeRowPanel.add(Box.createHorizontalGlue());
            rangeRowPanel.add(rangeSamples[i]);
            rangeRowPanel.setAlignmentX(0);
            rangesPanel.add(rangeRowPanel);
        }
        cnt.add(new JScrollPane(rangesPanel));

        JPanel specialPanel = new JPanel();
        specialPanel.setLayout(new BoxLayout(specialPanel, BoxLayout.X_AXIS));
        specialPanel.add(new JLabel(translate("label.individual")));
        individualCharsField = new JTextField();
        individualCharsField.setPreferredSize(new Dimension(100, individualCharsField.getPreferredSize().height));
        individialSample = new JLabel();
        specialPanel.add(individualCharsField);

        updateTextsCheckbox = new JCheckBox(AppStrings.translate("font.updateTexts"));

        JPanel utPanel = new JPanel(new FlowLayout());
        utPanel.add(updateTextsCheckbox);
        cnt.add(specialPanel);
        cnt.add(individialSample);
        cnt.add(utPanel);

        JPanel buttonsPanel = new JPanel(new FlowLayout());
        JButton okButton = new JButton(AppStrings.translate("button.ok"));
        okButton.addActionListener(this::okButtonActionPerformed);
        JButton cancelButton = new JButton(AppStrings.translate("button.cancel"));
        cancelButton.addActionListener(this::cancelButtonActionPerformed);
        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);
        cnt.add(buttonsPanel);
        View.setWindowIcon(this);
        View.centerScreen(this);
        setModalityType(ModalityType.APPLICATION_MODAL);
        individualCharsField.setText(selectedChars);
        getRootPane().setDefaultButton(okButton);
        familyNamesSelection.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                updateFaceSelection();
                updateCheckboxes();
            }
        });
        faceSelection.addItemListener((ItemEvent e) -> {
            updateCheckboxes();
        });
        updateCheckboxes();
        individualCharsField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateIndividual();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateIndividual();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateIndividual();
            }
        });
    }

    private void updateIndividual() {
        String chars = individualCharsField.getText();
        Font f = getSelectedFont();
        StringBuilder visibleChars = new StringBuilder();
        for (int i = 0; i < chars.length(); i++) {
            if (f.canDisplay(chars.codePointAt(i))) {
                visibleChars.append(chars.charAt(i));
            }
        }
        individialSample.setText(visibleChars.toString());
    }

    private void updateCheckboxes() {
        Font f = getSelectedFont().deriveFont(12f);
        int rc = CharacterRanges.rangeCount();

        Set<Integer> allChars = new HashSet<>();
        for (int i = 0; i < rc; i++) {
            rangeNames[i] = CharacterRanges.rangeName(i);
            int codes[] = CharacterRanges.rangeCodes(i);
            int avail = 0;
            StringBuilder sample = new StringBuilder();
            for (int c = 0; c < codes.length; c++) {
                if (f.canDisplay(codes[c])) {
                    allChars.add(codes[c]);
                    if (avail < SAMPLE_MAX_LENGTH) {
                        sample.append((char) codes[c]);
                    }
                    avail++;
                }
            }
            rangeSamples[i].setText(sample.toString());
            rangeSamples[i].setFont(f);
            rangeCheckboxes[i].setText(translate("range.description").replace("%available%", Integer.toString(avail)).replace("%name%", rangeNames[i]).replace("%total%", Integer.toString(codes.length)));
        }
        allCheckbox.setText(translate("allcharacters").replace("%available%", Integer.toString(allChars.size())));
        individialSample.setFont(f);
        updateIndividual();
    }

    @Override
    public void setVisible(boolean b) {
        if (b) {
            result = ERROR_OPTION;
        }

        super.setVisible(b);
    }

    private void okButtonActionPerformed(ActionEvent evt) {
        result = OK_OPTION;
        setVisible(false);
    }

    private void cancelButtonActionPerformed(ActionEvent evt) {
        result = CANCEL_OPTION;
        setVisible(false);
    }

    private void loadFromDiscButtonActionPerformed(ActionEvent evt) {
        if (customFont != null) {
            if (loadFromDisk()) {
                updateCheckboxes();
            }
        }

        ttfFileRadio.setSelected(true);
    }

    private boolean loadFromDisk() {
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new File(Configuration.lastOpenDir.get()));
        FileFilter ttfFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                return (f.getName().toLowerCase().endsWith(".ttf")) || (f.isDirectory());
            }

            @Override
            public String getDescription() {
                return translate("filter.ttf");
            }
        };
        fc.setFileFilter(ttfFilter);
        fc.setAcceptAllFileFilterUsed(true);
        JFrame f = new JFrame();
        View.setWindowIcon(f);
        int returnVal = fc.showOpenDialog(f);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            Configuration.lastOpenDir.set(Helper.fixDialogFile(fc.getSelectedFile()).getParentFile().getAbsolutePath());
            File selfile = Helper.fixDialogFile(fc.getSelectedFile());
            try {
                customFont = Font.createFont(Font.TRUETYPE_FONT, selfile);
                ttfFileRadio.setText(translate("ttffile.selection").replace("%fontname%", customFont.getName()).replace("%filename%", selfile.getName()));
                return true;
            } catch (FontFormatException ex) {
                JOptionPane.showMessageDialog(this, translate("error.invalidfontfile"), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, translate("error.cannotreadfontfile"), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
            }
        }
        return false;
    }

    public int showDialog() {
        setVisible(true);
        return result;
    }
}
