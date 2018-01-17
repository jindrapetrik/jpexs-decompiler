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

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.lang.ref.WeakReference;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import jsyntaxpane.actions.DocumentSearchData;
import jsyntaxpane.components.Markers;

/**
 *
 * @author JPEXS
 */
public class QuickFindPanel extends JPanel {

    public JTextField findTextField;

    public JButton prevButton, nextButton;

    public JCheckBox ignoreCaseCheckbox, regExpCheckbox, wrapCheckbox;

    public JLabel statusLabel;

    private final Markers.SimpleMarker marker = new Markers.SimpleMarker(Color.pink);

    private WeakReference<JTextComponent> target;

    private WeakReference<DocumentSearchData> dsd;

    private int oldCaretPosition;

    public QuickFindPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

        JPanel pan1 = new JPanel(new WrapLayout());
        JPanel pan2 = new JPanel(new WrapLayout());
        pan1.setAlignmentX(0);
        pan2.setAlignmentX(0);
        pan1.setAlignmentY(0);
        pan2.setAlignmentY(0);

        JLabel jLabel1 = new javax.swing.JLabel();
        findTextField = new javax.swing.JTextField();
        prevButton = new javax.swing.JButton();
        nextButton = new javax.swing.JButton();
        ignoreCaseCheckbox = new javax.swing.JCheckBox();
        regExpCheckbox = new javax.swing.JCheckBox();
        wrapCheckbox = new javax.swing.JCheckBox();
        statusLabel = new javax.swing.JLabel();

        setName("QuickFindDialog");

        jLabel1.setLabelFor(findTextField);
        ResourceBundle bundle = ResourceBundle.getBundle("jsyntaxpane/Bundle");
        jLabel1.setText(bundle.getString("QuickFindDialog.jLabel1.text"));
        pan1.add(jLabel1);

        findTextField.setColumns(30);
        findTextField.setBorder(javax.swing.BorderFactory.createLineBorder(Color.black));
        findTextField.setMaximumSize(new java.awt.Dimension(200, 24));
        findTextField.setMinimumSize(new java.awt.Dimension(60, 24));
        pan1.add(findTextField);

        prevButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/META-INF/images/small-icons/go-up.png")));
        prevButton.setFocusable(false);
        prevButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        prevButton.setOpaque(false);
        prevButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        prevButton.addActionListener(this::previousButtonActionPerformed);
        pan1.add(prevButton);

        nextButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/META-INF/images/small-icons/go-down.png")));
        nextButton.setFocusable(false);
        nextButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        nextButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        nextButton.setOpaque(false);
        nextButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        nextButton.addActionListener(this::nextButtonActionPerformed);
        pan1.add(nextButton);

        ignoreCaseCheckbox.setMnemonic('C');
        ignoreCaseCheckbox.setText(bundle.getString("QuickFindDialog.jChkIgnoreCase.text"));
        ignoreCaseCheckbox.setFocusable(false);
        ignoreCaseCheckbox.setOpaque(false);
        ignoreCaseCheckbox.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        pan2.add(ignoreCaseCheckbox);
        //ignoreCaseCheckbox.addActionListener(this);

        regExpCheckbox.setMnemonic('R');
        regExpCheckbox.setText(bundle.getString("QuickFindDialog.jChkRegExp.text"));
        regExpCheckbox.setFocusable(false);
        regExpCheckbox.setOpaque(false);
        regExpCheckbox.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        pan2.add(regExpCheckbox);
        //regExpCheckbox.addActionListener(this);

        wrapCheckbox.setMnemonic('W');
        wrapCheckbox.setText(bundle.getString("QuickFindDialog.jChkWrap.text"));
        wrapCheckbox.setFocusable(false);
        wrapCheckbox.setOpaque(false);
        wrapCheckbox.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        pan2.add(wrapCheckbox);
        //wrapCheckbox.addActionListener(this);

        statusLabel.setFont(statusLabel.getFont().deriveFont(statusLabel.getFont().getStyle() | java.awt.Font.BOLD, statusLabel.getFont().getSize() - 2));
        statusLabel.setForeground(Color.red);
        pan2.add(statusLabel);

        add(pan1);
        add(pan2);
        setPreferredSize(getMinimumSize());
        setVisible(false);
    }

    private void previousButtonActionPerformed(ActionEvent evt) {
        if (dsd.get().doFindPrev(target.get())) {
            statusLabel.setText(null);
        } else {
            statusLabel.setText(java.util.ResourceBundle.getBundle("jsyntaxpane/Bundle").getString("QuickFindDialog.NotFound"));
        }
    }

    private void nextButtonActionPerformed(ActionEvent evt) {
        if (dsd.get().doFindNext(target.get())) {
            statusLabel.setText(null);
        } else {
            statusLabel.setText(java.util.ResourceBundle.getBundle("jsyntaxpane/Bundle").getString("QuickFindDialog.NotFound"));
        }
    }

    public void showQuickFind(final JTextComponent t, DocumentSearchData ds) {
        dsd = new WeakReference<>(ds);
        oldCaretPosition = t.getCaretPosition();
        Container view = t.getParent();
        Dimension wd = getSize();
        wd.width = t.getVisibleRect().width;
        Point loc = new Point(0, view.getHeight());
        setSize(wd);
        SwingUtilities.convertPointToScreen(loc, view);
        setLocation(loc);
        findTextField.setFont(t.getFont());
        final DocumentListener dl;
        findTextField.getDocument().addDocumentListener(dl = new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateFind();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateFind();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateFind();
            }

            private void updateFind() {
                JTextComponent t = target.get();
                DocumentSearchData d = dsd.get();
                String toFind = findTextField.getText();
                if (toFind == null || toFind.isEmpty()) {
                    statusLabel.setText(null);
                    return;
                }
                try {
                    d.setWrap(wrapCheckbox.isSelected());
                    d.setPattern(toFind,
                            regExpCheckbox.isSelected(),
                            ignoreCaseCheckbox.isSelected());
                    // The dsd doFindNext will always find from current pos,
                    // so we need to relocate to our saved pos before we call doFindNext
                    statusLabel.setText(null);
                    t.setCaretPosition(oldCaretPosition);
                    if (!d.doFindNext(t)) {
                        statusLabel.setText(java.util.ResourceBundle.getBundle("jsyntaxpane/Bundle").getString("QuickFindDialog.NotFound"));
                    } else {
                        statusLabel.setText(null);
                    }
                } catch (PatternSyntaxException e) {
                    statusLabel.setText(e.getDescription());
                }
            }
        });
        this.target = new WeakReference<>(t);
        Pattern p = dsd.get().getPattern();
        if (p != null) {
            findTextField.setText(p.pattern());
        }
        wrapCheckbox.setSelected(dsd.get().isWrap());

        setVisible(true);
        getParent().revalidate();
        getParent().repaint();
        findTextField.requestFocusInWindow();
    }
    /*
     @Override
     public void focusGained(FocusEvent e) {

     }

     @Override
     public void focusLost(FocusEvent e) {
     removeFocusListener(this);
     setVisible(false);
     getParent().revalidate();
     getParent().repaint();
     }*/
}
