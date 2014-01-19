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

import com.jpexs.decompiler.flash.AppStrings;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import jsyntaxpane.actions.DocumentSearchData;

/**
 *
 * @author JPEXS
 */
public class SearchPanel<E> extends JPanel implements ActionListener {
    static final String ACTION_SEARCH_PREV = "SEARCHPREV";
    static final String ACTION_SEARCH_NEXT = "SEARCHNEXT";
    static final String ACTION_SEARCH_CANCEL = "SEARCHCANCEL";

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
        prevSearchButton.addActionListener(this);
        prevSearchButton.setActionCommand(ACTION_SEARCH_PREV);
        JButton nextSearchButton = new JButton(View.getIcon("next16"));
        nextSearchButton.setMargin(new Insets(3, 3, 3, 3));
        nextSearchButton.addActionListener(this);
        nextSearchButton.setActionCommand(ACTION_SEARCH_NEXT);
        JButton cancelSearchButton = new JButton(View.getIcon("cancel16"));
        cancelSearchButton.setMargin(new Insets(3, 3, 3, 3));
        cancelSearchButton.addActionListener(this);
        cancelSearchButton.setActionCommand(ACTION_SEARCH_CANCEL);
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
        foundPos = pos;
        doUpdate();
    }
    
    public void clear() {
        foundPos = 0;
        found.clear();
    }
    
    private void doUpdate() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                searchPos.setText((foundPos + 1) + "/" + found.size());
                listener.updateSearchPos(found.get(foundPos));
            }
        });
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case ACTION_SEARCH_CANCEL:
                foundPos = 0;
                setVisible(false);
                found = new ArrayList<>();
                searchFor = null;
                break;
            case ACTION_SEARCH_PREV:
                foundPos--;
                if (foundPos < 0) {
                    foundPos += found.size();
                }
                doUpdate();
                break;
            case ACTION_SEARCH_NEXT:
                foundPos = (foundPos + 1) % found.size();
                doUpdate();
                break;
        }
    }
}
