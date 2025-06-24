/*
 *  Copyright (C) 2024-2025 JPEXS
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
package com.jpexs.decompiler.flash.easygui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Component for switching tabs which has only single tab component.
 * @author JPEXS
 * @param <E> Element
 */
public class TabSwitcher<E> extends JPanel {
    private final JTabbedPane tabbedPane;
    private final List<E> values;
    private final List<String> titles;
    private Component tabComponent;
    private final JPanel centralPanel;
    private final List<TabSwitchedListener<E>> listeners = new ArrayList<>();
    
    public TabSwitcher(Component tabComponent) {
        titles = new ArrayList<>();
        values = new ArrayList<>();
        tabbedPane = new JTabbedPane();
        tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int index = tabbedPane.getSelectedIndex();
                if (index == -1) {
                    fireTabSwitched(null);
                } else {
                    fireTabSwitched(values.get(index));
                }
            }            
        });
        this.tabComponent = tabComponent;
        centralPanel = new JPanel();
        centralPanel.setLayout(null);
        centralPanel.add(tabComponent);
        centralPanel.add(tabbedPane);
        setLayout(new BorderLayout());
        add(centralPanel, BorderLayout.CENTER);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateSizes();
            }            
        });
    }
        
    public void addTabSwitchedListener(TabSwitchedListener<E> listener) {
        listeners.add(listener);
    }
    
    public void removeTabSwitchedListener(TabSwitchedListener<E> listener) {
        listeners.remove(listener);
    }
    
    private void fireTabSwitched(E value) {
        for (TabSwitchedListener<E> listener : listeners) {
            listener.tabSwitched(value);
        }
    }   
    
    private void updateSizes() {
        tabbedPane.setBounds(0, 0, centralPanel.getWidth(), centralPanel.getHeight());
        int maxH = 0;
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            Rectangle r = tabbedPane.getUI().getTabBounds(tabbedPane, i);
            int h = r.y + r.height;
            if (h > maxH) {
                maxH = h;
            }
        }
        maxH += 2;
        tabbedPane.setBounds(0, 0, centralPanel.getWidth(), maxH + 10);
        tabComponent.setBounds(0, maxH, centralPanel.getWidth(), centralPanel.getHeight() - maxH); 
    }

    public Component getTabComponent() {
        return tabComponent;
    }        
    
    public void setTabComponent(Component component) {
        if (this.tabComponent != null) {
            centralPanel.remove(this.tabComponent);            
        }
        this.tabComponent = component;        
        
        centralPanel.add(component);
        revalidate();
    }
    
    public void addTab(E value, String title, Icon icon) {
        titles.add(title);
        values.add(value);
        tabbedPane.insertTab(title, icon, new JPanel(), null, values.size() - 1);
        updateSizes();
    }       
    
    public String getTabTitleAtIndex(int index) {
        return titles.get(index);
    }
    
    public E getValueAtIndex(int index) {
        return values.get(index);
    }
    
    public int getValueCount() {
        return values.size();
    }
    
    public void clear() {
        values.clear();
        titles.clear();
        tabbedPane.removeAll();
        fireTabSwitched(null);
    }
    
    public void setSelectedIndex(int index) {
        tabbedPane.setSelectedIndex(index);
    }
    
    public int getSelectedIndex() {
        return tabbedPane.getSelectedIndex();
    }
    
    public void setValue(E value) {
        if (value == null) {
            setSelectedIndex(-1);
            return;
        }
        int index = values.indexOf(value);
        setSelectedIndex(index);
    }
    
    
    public int indexOf(E value) {
        return values.indexOf(value);
    }
    
    public E getSelectedValue() {
        int index = tabbedPane.getSelectedIndex();
        if (index == -1) {
            return null;
        }
        return values.get(index);
    }
}
