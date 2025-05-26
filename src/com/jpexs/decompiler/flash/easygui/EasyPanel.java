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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.gui.MainPanel;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.timeline.Timelined;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

/**
 *
 * @author JPEXS
 */
public class EasyPanel extends JPanel {
    
    private TabSwitcher<SWF> tabSwitcher;
    private EasySwfPanel easySwfPanel;

    public EasyPanel(MainPanel mainPanel) {
        easySwfPanel = new EasySwfPanel(mainPanel);
        tabSwitcher = new TabSwitcher<>(easySwfPanel);
        setLayout(new BorderLayout());
        add(tabSwitcher, BorderLayout.CENTER);
        tabSwitcher.addTabSwitchedListener(new TabSwitchedListener<SWF>() {
            @Override
            public void tabSwitched(SWF value) {
                easySwfPanel.setTimelined(value);
            }
        });
    }

    public void setSwfs(List<SWF> swfs) {
        tabSwitcher.clear();
        for (SWF swf : swfs) {
            tabSwitcher.addTab(swf, swf.getShortPathTitle(), View.getIcon("flash16"));
        }
        easySwfPanel.clearUndos();
    }

    public void setSwf(SWF swf) {
        tabSwitcher.setValue(swf);
    }

    public int indexOf(SWF swf) {
        return tabSwitcher.indexOf(swf);
    }
    
    public void setSwfIndex(int index) {
        if (index < 0) {
            return;
        }
        if (index >= tabSwitcher.getValueCount()) {
            return;
        }
        tabSwitcher.setSelectedIndex(index);
    }
    
    public SWF getSwfAtIndex(int index) {
        if (index < 0 || index >= tabSwitcher.getValueCount()) {
            return null;
        }
        return tabSwitcher.getValueAtIndex(index);                
    }
    
    public void setNoSwf() {
        easySwfPanel.setTimelined(null);
        tabSwitcher.setValue(null);
    }

    public SWF getSwf() {
        return tabSwitcher.getSelectedValue();
    }
    
    public void setTimelined(Timelined tim) {
        setSwf(tim.getSwf());
        easySwfPanel.setTimelined(tim);
    }

    public void dispose() {
        setSwfs(new ArrayList<>());
        easySwfPanel.dispose();
    }
}
