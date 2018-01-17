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

import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;
import jsyntaxpane.actions.DocumentSearchData;

/**
 *
 * @author JPEXS
 * @param <E> Element to search
 */
public class SearchPanel<E> extends JPanel {

    private final SearchListener<E> listener;

    private final JLabel searchPos;

    private int foundPos = 0;

    private final JLabel searchForLabel;

    private String searchFor;

    private boolean searchIgnoreCase;

    private boolean searchRegexp;

    private List<E> found = new ArrayList<>();

    public SearchPanel(LayoutManager lm, SearchListener<E> listener) {
        super(lm);

        this.listener = listener;

        JButton prevSearchButton = new JButton(View.getIcon("prev16"));
        prevSearchButton.setMargin(new Insets(3, 3, 3, 3));
        prevSearchButton.addActionListener(this::prevButtonActionPerformed);
        JButton nextSearchButton = new JButton(View.getIcon("next16"));
        nextSearchButton.setMargin(new Insets(3, 3, 3, 3));
        nextSearchButton.addActionListener(this::nextButtonActionPerformed);
        JButton cancelSearchButton = new JButton(View.getIcon("cancel16"));
        cancelSearchButton.setMargin(new Insets(3, 3, 3, 3));
        cancelSearchButton.addActionListener(this::cancelButtonActionPerformed);
        searchPos = new JLabel("0/0");
        searchForLabel = new JLabel(AppStrings.translate("search.info").replace("%text%", ""));
        add(searchForLabel);
        add(prevSearchButton);
        add(new JLabel(AppStrings.translate("search.script") + " "));
        add(searchPos);
        add(nextSearchButton);
        add(cancelSearchButton);
        setVisible(false);
    }

    public void showQuickFindDialog(JTextComponent editor) {
        DocumentSearchData dsd = DocumentSearchData.getFromEditor(editor);
        dsd.setPattern(searchFor, searchRegexp, searchIgnoreCase);
        dsd.showQuickFindDialogEx(editor, searchIgnoreCase, searchRegexp);
    }

    public void setSearchText(String txt) {
        searchFor = txt;
        searchForLabel.setText(AppStrings.translate("search.info").replace("%text%", txt) + " ");
    }

    public boolean setResults(List<E> results) {
        View.checkAccess();

        found = results;
        if (found.isEmpty()) {
            setVisible(false);
            return false;
        } else {
            setPos(0);
            setVisible(true);
            return true;
        }
    }

    public void setOptions(boolean ignoreCase, boolean regExp) {
        searchIgnoreCase = ignoreCase;
        searchRegexp = regExp;
    }

    public void setPos(int pos) {
        View.checkAccess();

        foundPos = pos;
        doUpdate();
    }

    public void clear() {
        View.checkAccess();

        foundPos = 0;
        found.clear();
    }

    private void doUpdate() {
        View.checkAccess();

        searchPos.setText((foundPos + 1) + "/" + found.size());
        listener.updateSearchPos(found.get(foundPos));
    }

    private void cancelButtonActionPerformed(ActionEvent evt) {
        foundPos = 0;
        setVisible(false);
        found = new ArrayList<>();
        searchFor = null;
    }

    private void prevButtonActionPerformed(ActionEvent evt) {
        foundPos--;
        if (foundPos < 0) {
            foundPos += found.size();
        }
        doUpdate();
    }

    private void nextButtonActionPerformed(ActionEvent evt) {
        foundPos = (foundPos + 1) % found.size();
        doUpdate();
    }
}
