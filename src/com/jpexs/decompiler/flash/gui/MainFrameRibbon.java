/*
 *  Copyright (C) 2010-2025 JPEXS
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
import com.jpexs.decompiler.flash.gui.jna.platform.win32.BaseTSD;
import com.jpexs.decompiler.flash.gui.jna.platform.win32.Dwmapi;
import com.jpexs.decompiler.flash.gui.jna.platform.win32.NtDll;
import com.jpexs.decompiler.flash.gui.jna.platform.win32.User32;
import com.jpexs.decompiler.flash.gui.jna.platform.win32.WinDef;
import com.jpexs.decompiler.flash.gui.jna.platform.win32.WinNT;
import com.jpexs.decompiler.flash.gui.jna.platform.win32.WinUser;
import com.jpexs.decompiler.flash.treeitems.OpenableList;
import com.jpexs.helpers.Helper;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.win32.StdCallLibrary;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.plaf.RootPaneUI;
import org.pushingpixels.flamingo.api.ribbon.JRibbon;
import org.pushingpixels.flamingo.internal.ui.ribbon.appmenu.JRibbonApplicationMenuButton;
import org.pushingpixels.substance.flamingo.ribbon.ui.SubstanceRibbonRootPaneUI;
import org.pushingpixels.substance.internal.utils.SubstanceSizeUtils;

/**
 * @author JPEXS
 */
public final class MainFrameRibbon extends AppRibbonFrame {

    private final MainPanel panel;

    private final MainFrameMenu mainMenu;

    public MainFrameRibbon() {
        super();

        Container cnt = getContentPane();
        cnt.setLayout(new BorderLayout());
        JRibbon ribbon = getRibbon();
        cnt.add(ribbon, BorderLayout.NORTH);

        mainMenu = new MainFrameRibbonMenu(this, ribbon);
        mainMenu.createMenuBar();

        panel = new MainPanel(this, mainMenu);
        panel.setBackground(Color.yellow);
        cnt.add(panel, BorderLayout.CENTER);

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

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (Configuration.saveSessionOnExit.get()) {
                    StringBuilder sb = new StringBuilder();
                    StringBuilder sbt = new StringBuilder();

                    boolean first = true;
                    for (OpenableList swf : panel.getSwfs()) {
                        if (!first) {
                            sb.append(File.pathSeparator);
                            sbt.append(File.pathSeparator);
                        }
                        first = false;
                        String file = swf.sourceInfo.getFile();
                        if (file != null) {
                            sb.append(file);
                            String t = swf.sourceInfo.getFileTitle();
                            sbt.append(t == null ? "" : t);
                        }
                    }

                    Configuration.lastSessionFiles.set(sb.toString());
                    Configuration.lastSessionFileTitles.set(sbt.toString());

                    SWF easySwf = panel.getEasyPanel().getSwf();
                    if (easySwf != null) {
                        Configuration.lastSessionEasySwf.set(easySwf.getFile() + "|" + easySwf.getFileTitle());
                    } else {
                        Configuration.lastSessionEasySwf.set("");
                    }

                    String pathResources = panel.tagTree.getSelectionPathString();
                    if (pathResources != null) {
                        Configuration.lastSessionSelection.set(pathResources);
                    }
                    String pathTagList = panel.tagListTree.getSelectionPathString();
                    if (pathTagList != null) {
                        Configuration.lastSessionTagListSelection.set(pathTagList);
                    }

                }

                boolean closeResult = panel.closeAll(true, true);
                if (closeResult) {
                    Main.exit();
                }
            }
        });

        View.centerScreenMain(this);

        enableAeroSnap();
    }

    private boolean isAeroSnapAvailable() {
        if (!Platform.isWindows()) {
            return false;
        }

        WinNT.OSVERSIONINFOEX version = new WinNT.OSVERSIONINFOEX();
        if (NtDll.INSTANCE.RtlGetVersion(version) != 0) {
            return false;
        }

        int major = version.dwMajorVersion.intValue();
        //int minor = version.dwMinorVersion.intValue();

        if (major < 6) {
            // Windows XP or older
            return false;
        }

        try {
            WinNT.BOOLbyReference enabled = new WinNT.BOOLbyReference();
            if (Dwmapi.INSTANCE.DwmIsCompositionEnabled(enabled).intValue() == 0) {
                if (enabled.getValue().intValue() != 1) {
                    return false;
                }
            }
        } catch (UnsatisfiedLinkError | NoClassDefFoundError e) {
            return false;
        }
        return true;
    }

    private void enableAeroSnap() {
        if (!isAeroSnapAvailable()) {
            return;
        }

        Point posOnScreen = new Point();
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                update();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                update();
            }

            private void update() {
                if (MainFrameRibbon.this.isVisible()) {
                    posOnScreen.x = MainFrameRibbon.this.getLocationOnScreen().x;
                    posOnScreen.y = MainFrameRibbon.this.getLocationOnScreen().y;
                }
            }
        });
        addWindowListener(new WindowAdapter() {
            private BaseTSD.LONG_PTR oldWndProc;
            private StdCallLibrary.StdCallCallback newProc;
            private Rectangle dragRect = new Rectangle();
            private Rectangle toggleRect = new Rectangle();
            private AffineTransform trans;

            @Override
            public void windowOpened(WindowEvent e) {
                RootPaneUI ui = getRootPane().getUI();
                if (ui instanceof SubstanceRibbonRootPaneUI) {
                    SubstanceRibbonRootPaneUI sui = (SubstanceRibbonRootPaneUI) ui;
                    JComponent titlePane = sui.getTitlePane();

                    Window window = MainFrameRibbon.this;
                    trans = View.getWindowDevice(window).getDefaultConfiguration().getDefaultTransform();
                    if (trans == null) {
                        trans = new AffineTransform();
                    }

                    ComponentAdapter ad = new ComponentAdapter() {
                        @Override
                        public void componentResized(ComponentEvent e) {
                            updateRect();
                        }

                        @Override
                        public void componentShown(ComponentEvent e) {
                            updateRect();
                        }

                        private void updateRect() {
                            int appButtonSize = (int) Math.round(trans.getScaleX() * Integer.getInteger("peacock.appButtonSize", 24));
                            int titleIconsWidth
                                    = 3 + SubstanceSizeUtils.getTitlePaneIconSize() //close                                    
                                    + 10 + SubstanceSizeUtils.getTitlePaneIconSize() //maximize / restore
                                    + 2 + SubstanceSizeUtils.getTitlePaneIconSize() //minimize
                                    + 2 + SubstanceSizeUtils.getTitlePaneIconSize() //always on top
                                    ;

                            dragRect = new Rectangle(
                                    5 + appButtonSize,
                                    4,
                                    titlePane.getWidth() - titleIconsWidth - appButtonSize,
                                    titlePane.getHeight()
                            );

                            toggleRect = new Rectangle(
                                    titlePane.getWidth() - (3 + SubstanceSizeUtils.getTitlePaneIconSize() // close
                                    + 10 + SubstanceSizeUtils.getTitlePaneIconSize() //maximize / restore
                                    ),
                                    4,
                                    SubstanceSizeUtils.getTitlePaneIconSize(),
                                    SubstanceSizeUtils.getTitlePaneIconSize()
                            );                            
                        }
                    };                    
                    titlePane.addComponentListener(ad);                    

                    ad.componentShown(null);

                    WinDef.HWND hwnd = new WinDef.HWND(Native.getComponentPointer(window));

                    oldWndProc = User32.INSTANCE.GetWindowLongPtr(hwnd, WinUser.GWL_WNDPROC);

                    newProc = new StdCallLibrary.StdCallCallback() {
                        public WinDef.LRESULT callback(WinDef.HWND hWnd, int uMsg, WinDef.WPARAM wParam, WinDef.LPARAM lParam) {
                            if (!User32.INSTANCE.IsWindow(hwnd)) {
                                return new WinDef.LRESULT(0);
                            }
                            if (uMsg == WinUser.WM_NCCALCSIZE) {
                                return new WinDef.LRESULT(0);
                            }
                            if (uMsg == WinUser.WM_NCMOUSEMOVE) {
                                if (wParam.intValue() == WinUser.HTMAXBUTTON) {
                                    User32.INSTANCE.PostMessage(hwnd, WinUser.WM_MOUSEMOVE, new WinDef.WPARAM(0), lParam);
                                    return new WinDef.LRESULT(0);
                                }
                            }
                            if (uMsg == WinUser.WM_NCMOUSEHOVER) {
                                if (wParam.intValue() == WinUser.HTMAXBUTTON) {                                    
                                    User32.INSTANCE.PostMessage(hwnd, WinUser.WM_MOUSEHOVER, new WinDef.WPARAM(0), lParam);
                                    return new WinDef.LRESULT(0);
                                }
                            }
                            if (uMsg == WinUser.WM_NCMOUSELEAVE) {
                                if (wParam.intValue() == WinUser.HTMAXBUTTON) {
                                    User32.INSTANCE.PostMessage(hwnd, WinUser.WM_MOUSELEAVE, new WinDef.WPARAM(0), lParam);
                                    return new WinDef.LRESULT(0);
                                }
                            }
                            if (uMsg == WinUser.WM_NCLBUTTONDOWN) {
                                if (wParam.intValue() == WinUser.HTMAXBUTTON) {
                                    User32.INSTANCE.PostMessage(hwnd, WinUser.WM_LBUTTONDOWN, new WinDef.WPARAM(WinUser.MK_LBUTTON), lParam);
                                    return new WinDef.LRESULT(0);
                                }
                            }
                            if (uMsg == WinUser.WM_NCLBUTTONUP) {
                                if (wParam.intValue() == WinUser.HTMAXBUTTON) {
                                    User32.INSTANCE.PostMessage(hwnd, WinUser.WM_LBUTTONUP, new WinDef.WPARAM(WinUser.MK_LBUTTON), lParam);
                                    return new WinDef.LRESULT(0);
                                }
                            }                            
                            if (uMsg == WinUser.WM_NCHITTEST) {
                                int y = (short) ((lParam.longValue() >> 16) & 0xFFFF);
                                int x = (short) (lParam.longValue() & 0xFFFF);

                                int BORDER_WIDTH = 4;
                                WinDef.RECT winRect = new WinDef.RECT();
                                User32.INSTANCE.GetWindowRect(hWnd, winRect);

                                boolean left = x >= winRect.left - BORDER_WIDTH && x < winRect.left + BORDER_WIDTH;
                                boolean right = x < winRect.right + BORDER_WIDTH && x >= winRect.right - BORDER_WIDTH;
                                boolean top = y >= winRect.top - BORDER_WIDTH && y < winRect.top + BORDER_WIDTH;
                                boolean bottom = y < winRect.bottom + BORDER_WIDTH && y >= winRect.bottom - BORDER_WIDTH;

                                if (left && top) {
                                    return new WinDef.LRESULT(WinUser.HTTOPLEFT);
                                }
                                if (right && top) {
                                    return new WinDef.LRESULT(WinUser.HTTOPRIGHT);
                                }
                                if (left && bottom) {
                                    return new WinDef.LRESULT(WinUser.HTBOTTOMLEFT);
                                }
                                if (right && bottom) {
                                    return new WinDef.LRESULT(WinUser.HTBOTTOMRIGHT);
                                }
                                if (top) {
                                    return new WinDef.LRESULT(WinUser.HTTOP);
                                }
                                if (bottom) {
                                    return new WinDef.LRESULT(WinUser.HTBOTTOM);
                                }
                                if (left) {
                                    return new WinDef.LRESULT(WinUser.HTLEFT);
                                }
                                if (right) {
                                    return new WinDef.LRESULT(WinUser.HTRIGHT);
                                }

                                Point p = new Point(x, y);

                                p.x = (int) Math.round(p.x / trans.getScaleX());
                                p.y = (int) Math.round(p.y / trans.getScaleY());

                                p.x -= posOnScreen.x;
                                p.y -= posOnScreen.y;
                                if (dragRect.contains(p)) {
                                    return new WinDef.LRESULT(WinUser.HTCAPTION);
                                }

                                if (toggleRect.contains(p)) {
                                    return new WinDef.LRESULT(WinUser.HTMAXBUTTON);
                                }

                                //return new WinDef.LRESULT(WinUser.HTCLIENT);
                            }
                            return User32.INSTANCE.CallWindowProc(oldWndProc.toPointer(), hWnd, uMsg, wParam, lParam);
                        }
                    };

                    User32.INSTANCE.SetWindowLongPtr(hwnd, WinUser.GWL_WNDPROC, newProc);

                    int style = User32.INSTANCE.GetWindowLong(hwnd, WinUser.GWL_STYLE);
                    style |= WinUser.WS_THICKFRAME;
                    User32.INSTANCE.SetWindowLong(hwnd, WinUser.GWL_STYLE, style);
                    User32.INSTANCE.SetWindowPos(hwnd, null, 0, 0, 0, 0,
                            WinUser.SWP_NOMOVE | WinUser.SWP_NOSIZE | WinUser.SWP_NOZORDER | WinUser.SWP_FRAMECHANGED);

                    Dwmapi.MARGINS margins = new Dwmapi.MARGINS();
                    margins.cxLeftWidth = 0;
                    margins.cxRightWidth = 0;
                    margins.cyTopHeight = 0;
                    margins.cyBottomHeight = 0;

                    Dwmapi.INSTANCE.DwmExtendFrameIntoClientArea(hwnd, margins);
                }
            }
        });
    }

    @Override
    public void setExtendedState(int state) {
        if ((state & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH) {
            GraphicsConfiguration gc = View.getWindowDevice(MainFrameRibbon.this.getWindow()).getDefaultConfiguration();

            Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
            Rectangle screenBounds = gc.getBounds();
            Rectangle maxBounds = new Rectangle(
                    screenBounds.x + screenInsets.left,
                    screenBounds.y + screenInsets.top,
                    screenBounds.width - (screenInsets.left + screenInsets.right),
                    screenBounds.height - (screenInsets.top + screenInsets.bottom)
            );
            setMaximizedBounds(maxBounds);
        }
        super.setExtendedState(state);
    }

    private static void getApplicationMenuButtons(Component comp, List<JRibbonApplicationMenuButton> ret) {
        if (comp instanceof JRibbonApplicationMenuButton) {
            ret.add((JRibbonApplicationMenuButton) comp);
            return;
        }
        if (comp instanceof Container) {
            Container cont = (Container) comp;
            for (int i = 0; i < cont.getComponentCount(); i++) {
                getApplicationMenuButtons(cont.getComponent(i), ret);
            }
        }
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
