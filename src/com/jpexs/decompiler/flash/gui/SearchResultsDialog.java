/*
 *  Copyright (C) 2010-2021 JPEXS
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
import com.jpexs.decompiler.flash.search.SearchResult;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

/**
 *
 * @author JPEXS
 * @param <E> Element to search
 */
public class SearchResultsDialog<E extends SearchResult> extends AppDialog {

    private final JList<E> resultsList;

    private final DefaultListModel<E> model;
    private final boolean regExp;

    private final List<SearchListener<E>> listeners;

    private final JButton gotoButton = new JButton(translate("button.goto"));

    private final JButton closeButton = new JButton(translate("button.close"));

    private String text;
    private final boolean ignoreCase;

    public SearchResultsDialog(Window owner, String text, boolean ignoreCase, boolean regExp, List<SearchListener<E>> listeners) {
        super(owner);
        setTitle(translate("dialog.title").replace("%text%", text));
        this.text = text;
        Container cnt = getContentPane();
        model = new DefaultListModel<>();
        resultsList = new JList<>(model);
        this.regExp = regExp;
        this.listeners = listeners;

        gotoButton.addActionListener(this::gotoButtonActionPerformed);
        closeButton.addActionListener(this::closeButtonActionPerformed);

        JPanel paramsPanel = new JPanel();
        paramsPanel.setLayout(new FlowLayout());
        JLabel searchTextLabel = new JLabel(AppDialog.translateForDialog("label.searchtext", SearchDialog.class) + text);
        JLabel ignoreCaseLabel = new JLabel(AppDialog.translateForDialog("checkbox.ignorecase", SearchDialog.class) + ": " + (ignoreCase ? AppStrings.translate("yes") : AppStrings.translate("no")));
        JLabel regExpLabel = new JLabel(AppDialog.translateForDialog("checkbox.regexp", SearchDialog.class) + ": " + (regExp ? AppStrings.translate("yes") : AppStrings.translate("no")));
        paramsPanel.add(ignoreCaseLabel);
        paramsPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        paramsPanel.add(regExpLabel);

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout());
        buttonsPanel.add(gotoButton);
        buttonsPanel.add(closeButton);
        resultsList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    gotoElement();
                }
            }
        });

        resultsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    gotoElement();
                }
            }
        });

        cnt.setLayout(new BorderLayout());
        JScrollPane sp = new JScrollPane(resultsList);
        sp.setPreferredSize(new Dimension(300, 300));
        cnt.add(sp, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        JPanel searchTextPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        searchTextPanel.add(searchTextLabel);
        if (Configuration.parametersPanelInSearchResults.get()) {
            bottomPanel.add(searchTextPanel);
            bottomPanel.add(paramsPanel);
            bottomPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }
        bottomPanel.add(buttonsPanel);
        cnt.add(bottomPanel, BorderLayout.SOUTH);
        pack();
        View.centerScreen(this);
        View.setWindowIcon(this);
        this.ignoreCase = ignoreCase;
    }

    public void setResults(List<E> results) {
        model.clear();
        for (E e : results) {
            model.addElement(e);
        }
    }

    public void removeSwf(SWF swf) {
        List<E> newItems = new ArrayList<>();
        for (int i = 0; i < model.getSize(); i++) {
            if (model.getElementAt(i).getSWF() != swf) {
                newItems.add(model.getElementAt(i));
            }
        }
        setResults(newItems);
    }

    public boolean isEmpty() {
        return model.isEmpty();
    }

    private void gotoButtonActionPerformed(ActionEvent evt) {
        gotoElement();
    }

    private void closeButtonActionPerformed(ActionEvent evt) {
        setVisible(false);
    }

    private void gotoElement() {
        if (resultsList.getSelectedIndex() != -1) {
            for (SearchListener<E> listener : listeners) {
                listener.updateSearchPos(text, ignoreCase, regExp, resultsList.getSelectedValue());
            }
        }
    }
}
