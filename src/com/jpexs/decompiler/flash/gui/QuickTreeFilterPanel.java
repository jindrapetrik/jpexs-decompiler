/*
 *  Copyright (C) 2022-2024 JPEXS
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

import com.jpexs.decompiler.flash.gui.tagtree.TagTree;
import com.jpexs.decompiler.flash.gui.tagtree.TagTreeModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * @author JPEXS
 */
public class QuickTreeFilterPanel extends JPanel implements QuickTreeFilterInterface {

    private List<ActionListener> listeners = new ArrayList<>();

    private JTextField filterField = new MyTextField("");
    private final TagTree tagTree;
    
    private JPanel foldersPanel;
    
    private List<String> foldersList = new ArrayList<>();
    private List<JLabel> folderLabelsList = new ArrayList<>();
    private Map<JLabel, String> labelToFolder = new HashMap<>();
    
    private Set<String> selectedFolders = new LinkedHashSet<>();

    public QuickTreeFilterPanel(TagTree tagTree) {
        this.tagTree = tagTree;
        filterField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                warn();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                warn();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                warn();
            }

            public void warn() {
                fireAction();
            }
        });

        JPanel quickFindPanel = new JPanel(new BorderLayout(4, 0));
        
        quickFindPanel.add(filterField, BorderLayout.CENTER);
        quickFindPanel.add(new JLabel(View.getIcon("search16")), BorderLayout.WEST);
        JLabel closeSearchButton = new JLabel(View.getIcon("cancel16"));
        closeSearchButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                filterField.setText("");
                setVisible(false);
                fireAction();
            }
        });
        closeSearchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        quickFindPanel.add(closeSearchButton, BorderLayout.EAST);
        
        setLayout(new BorderLayout());
        add(quickFindPanel, BorderLayout.NORTH);
        
        foldersPanel = new JPanel(new WrapLayout(WrapLayout.LEFT));
        
        add(foldersPanel, BorderLayout.CENTER);
        setVisible(false);        
    }
    
    public void updateFolders() {
        foldersPanel.removeAll();
        folderLabelsList.clear();
        foldersPanel.setSize(new Dimension(getParent().getWidth(), 47));
        
        labelToFolder.clear();
        foldersList = ((TagTreeModel) tagTree.getFullModel()).getAvailableFolders();
        for (String f : foldersList) {
            String icon = "folder" + f.toLowerCase(Locale.ENGLISH) + "16";
            if (f.equals("header")) {
                icon = "header16";
            }
            JLabel lab = new JLabel(AppStrings.translate("node." + f), View.getIcon(icon), JLabel.LEFT);
            lab.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            labelToFolder.put(lab, f);
            lab.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (selectedFolders.contains(f)) {
                        selectedFolders.remove(f);
                    } else {
                        selectedFolders.add(f);
                    }                    
                    labelHilight(lab, selectedFolders.contains(f));
                    fireAction();
                }                
            });
            folderLabelsList.add(lab);
            foldersPanel.add(lab);
            labelHilight(lab, selectedFolders.contains(f));            
        }            
    }
    
    private Color fixColor(Color color) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }
    
    private void labelHilight(JLabel label, boolean hilight) {
        label.setOpaque(true);        
        if (hilight) {
            label.setBackground(fixColor(UIManager.getColor("Tree.selectionBackground")));
            label.setForeground(fixColor(UIManager.getColor("Tree.selectionForeground")));            
        } else {
            label.setBackground(fixColor(UIManager.getColor("Tree.textBackground")));
            label.setForeground(fixColor(UIManager.getColor("Tree.textForeground")));
        }         
        label.repaint();
    }

    private void fireAction() {
        for (ActionListener listener : listeners) {
            listener.actionPerformed(new ActionEvent(this, 0, ""));
        }
    }

    public void addActionListener(ActionListener listener) {
        listeners.add(listener);
    }

    public void removeActionListener(ActionListener listener) {
        listeners.remove(listener);
    }

    public String getFilter() {
        return filterField.getText().trim();
    }

    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        if (aFlag) {
            View.execInEventDispatchLater(new Runnable() {
                @Override
                public void run() {
                    updateFolders();
                }                
            });
            
            filterField.requestFocusInWindow();
        } else {
            filterField.setText("");
            selectedFolders.clear();            
        }
    }

    @Override
    public List<String> getFolders() {
        return new ArrayList<>(selectedFolders);
    }       
}
