/*
 * Copyright (C) 2024 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.easygui;

import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.gui.AppStrings;
import com.jpexs.decompiler.flash.gui.View;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author JPEXS
 */
public class EasyGuiMain {
    public static MainFrame mainFrame;
    
    public static void main(String[] args) {
        
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("Error: Your system does not support Graphic User Interface");
            System.exit(1);
        }
        
        System.setProperty("sun.java2d.d3d", "false");
        System.setProperty("sun.java2d.noddraw", "true");

        System.setProperty("sun.java2d.uiScale", "1.0");

        System.setProperty("sun.java2d.opengl", "false");
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException
                | IllegalAccessException ignored) {
            //ignored
        }
        
        UIManager.put("Tree.expandedIcon", View.getIcon("expand16"));
        UIManager.put("Tree.collapsedIcon", View.getIcon("collapse16"));
        
        AppStrings.setResourceClass(com.jpexs.decompiler.flash.gui.MainFrame.class);
        
        
        mainFrame = new MainFrame();
        mainFrame.setVisible(true);
        
        try {
            mainFrame.open(new File(args[1]));
        } catch (IOException ex) {
            Logger.getLogger(EasyGuiMain.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(EasyGuiMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }        
}
