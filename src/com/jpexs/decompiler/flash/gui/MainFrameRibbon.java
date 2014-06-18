/*
 *  Copyright (C) 2010-2014 JPEXS
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
import com.jpexs.decompiler.flash.gui.player.FlashPlayerPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.util.List;
import javax.swing.JFrame;
import org.pushingpixels.flamingo.api.ribbon.JRibbon;
import org.pushingpixels.flamingo.internal.ui.ribbon.appmenu.JRibbonApplicationMenuButton;

/**
 *
 * @author JPEXS
 */
public final class MainFrameRibbon extends AppRibbonFrame implements MainFrame {

    public MainPanel panel;
    private MainFrameMenu mainMenu;

    public MainFrameRibbon() {
        super();

        FlashPlayerPanel flashPanel = null;
        try {
            flashPanel = new FlashPlayerPanel(this);
        } catch (FlashUnsupportedException fue) {
        }

        Container cnt = getContentPane();
        cnt.setLayout(new BorderLayout());
        JRibbon ribbon = getRibbon();
        cnt.add(ribbon, BorderLayout.NORTH);

        boolean externalFlashPlayerUnavailable = flashPanel == null;
        mainMenu = new MainFrameRibbonMenu(this, ribbon, externalFlashPlayerUnavailable);

        panel = new MainPanel(this, mainMenu, flashPanel);
        panel.setBackground(Color.yellow);
        cnt.add(panel, BorderLayout.CENTER);

        int w = Configuration.guiWindowWidth.get();
        int h = Configuration.guiWindowHeight.get();
        Dimension dim = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        if (w > dim.width) {
            w = dim.width;
        }
        if (h > dim.height) {
            h = dim.height;
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

        View.centerScreen(this);
        
        
    }

    private static void getApplicationMenuButtons(Component comp, List<JRibbonApplicationMenuButton> ret) {
        if (comp instanceof JRibbonApplicationMenuButton) {
            ret.add((JRibbonApplicationMenuButton) comp);
            return;
        }
        if (comp instanceof java.awt.Container) {
            java.awt.Container cont = (java.awt.Container) comp;
            for (int i = 0; i < cont.getComponentCount(); i++) {
                getApplicationMenuButtons(cont.getComponent(i), ret);
            }
        }
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);

        /*        final MainFrameRibbon t = this;

         //TODO: Handle this better. This is awful :-(
         new Timer().schedule(new TimerTask() {
         @Override
         public void run() {
         List<JRibbonApplicationMenuButton> mbuttons = new ArrayList<>();
         getApplicationMenuButtons(t, mbuttons);
         if (mbuttons.size() < 2) {
         //return, task will run again
         return;
         }

         for (final JRibbonApplicationMenuButton mbutton : mbuttons) {
         mbutton.setIcon(View.getResizableIcon("buttonicon_256"));
         mbutton.setDisplayState(new CommandButtonDisplayState(
         "My Ribbon Application Menu Button", mbutton.getSize().width) {
         @Override
         public CommandButtonLayoutManager createLayoutManager(
         AbstractCommandButton commandButton) {
         return new CommandButtonLayoutManager() {
         @Override
         public int getPreferredIconSize() {
         return mbutton.getSize().width;
         }

         @Override
         public CommandButtonLayoutManager.CommandButtonLayoutInfo getLayoutInfo(
         AbstractCommandButton commandButton, Graphics g) {
         CommandButtonLayoutManager.CommandButtonLayoutInfo result = new CommandButtonLayoutManager.CommandButtonLayoutInfo();
         result.actionClickArea = new Rectangle(0, 0, 0, 0);
         result.popupClickArea = new Rectangle(0, 0, commandButton
         .getWidth(), commandButton.getHeight());
         result.popupActionRect = new Rectangle(0, 0, 0, 0);
         ResizableIcon icon = commandButton.getIcon();
         icon.setDimension(new Dimension(commandButton.getWidth(), commandButton.getHeight()));
         result.iconRect = new Rectangle(
         0,
         0,
         commandButton.getWidth(), commandButton.getHeight());
         result.isTextInActionArea = false;
         return result;
         }

         @Override
         public Dimension getPreferredSize(
         AbstractCommandButton commandButton) {
         return new Dimension(40, 40);
         }

         @Override
         public void propertyChange(PropertyChangeEvent evt) {
         }

         @Override
         public Point getKeyTipAnchorCenterPoint(
         AbstractCommandButton commandButton) {
         // dead center
         return new Point(commandButton.getWidth() / 2,
         commandButton.getHeight() / 2);
         }
         };
         }
         });

         MyRibbonApplicationMenuButtonUI mui = (MyRibbonApplicationMenuButtonUI) mbutton.getUI();
         mui.setHoverIcon(View.getResizableIcon("buttonicon_hover_256"));
         mui.setNormalIcon(View.getResizableIcon("buttonicon_256"));
         mui.setClickIcon(View.getResizableIcon("buttonicon_down_256"));
         mbutton.repaint();
         }
         cancel(); //cancel task so it does not run again
         }
         }, 1, 50);*/
        panel.setVisible(b);
    }

    @Override
    public MainPanel getPanel() {
        return panel;
    }

    @Override
    public Window getWindow() {        
        return this;
    }
    
    
}
