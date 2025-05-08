/*
 *  Copyright (C) 2010-2024 JPEXS
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
import com.jpexs.helpers.Helper;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GraphicsDevice;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import javax.swing.JFrame;

/**
 * @author JPEXS
 */
public final class MainFrameClassic extends AppFrame implements MainFrame {

    private final MainPanel panel;

    private final MainFrameMenu mainMenu;

    public MainFrameClassic() {
        super();      

        mainMenu = new MainFrameClassicMenu(this);
        mainMenu.createMenuBar();

        panel = new MainPanel(this, mainMenu);

        int w = Configuration.guiWindowWidth.get();
        int h = Configuration.guiWindowHeight.get();
        GraphicsDevice device = View.getMainDefaultScreenDevice();
        Rectangle bounds = device.getDefaultConfiguration().getBounds();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(device.getDefaultConfiguration());
        int maxWidth = bounds.width - (insets.left + insets.right);
        int maxHeight = bounds.height - (insets.top + insets.bottom);

        if (w > maxWidth) {
            w = maxWidth;
        }
        if (h > maxHeight) {
            h = maxHeight;
        }
        setSize(w, h);

        boolean maximizedHorizontal = Configuration.guiWindowMaximizedHorizontal.get();
        boolean maximizedVertical = Configuration.guiWindowMaximizedVertical.get();

        int state = 0;
        if (maximizedHorizontal) {
            state |= JFrame.MAXIMIZED_HORIZ;
        }
        if (maximizedVertical) {
            state |= JFrame.MAXIMIZED_VERT;
        }
        setExtendedState(state);

        View.setWindowIcon(this);
        addWindowStateListener(new WindowStateListener() {
            @Override
            public void windowStateChanged(WindowEvent e) {
                int state = e.getNewState();
                Configuration.guiWindowMaximizedHorizontal.set((state & JFrame.MAXIMIZED_HORIZ) == JFrame.MAXIMIZED_HORIZ);
                Configuration.guiWindowMaximizedVertical.set((state & JFrame.MAXIMIZED_VERT) == JFrame.MAXIMIZED_VERT);
            }
        });
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int state = getExtendedState();
                if ((state & JFrame.MAXIMIZED_HORIZ) == 0) {
                    Configuration.guiWindowWidth.set(getWidth());
                }
                if ((state & JFrame.MAXIMIZED_VERT) == 0) {
                    Configuration.guiWindowHeight.set(getHeight());
                }
            }
        });
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Main.exit();
            }
        });

        Container cnt = getContentPane();
        cnt.setLayout(new BorderLayout());
        cnt.add(panel);

        View.centerScreenMain(this);
    }

    @Override
    public MainPanel getPanel() {
        return panel;
    }

    @Override
    public Window getWindow() {
        return this;
    }

    @Override
    public void dispose() {
        removeAll();
        mainMenu.dispose();
        Helper.emptyObject(mainMenu);
        panel.dispose();
        Helper.emptyObject(this);
        super.dispose();
    }

    @Override
    public MainFrameMenu getMenu() {
        return mainMenu;
    }
}
