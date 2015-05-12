package com.jpexs.decompiler.flash.gui;

import com.jpexs.decompiler.flash.gui.AdvancedSettingsDialog;
import com.jpexs.decompiler.flash.gui.AppStrings;
import java.util.HashMap;
import java.util.ResourceBundle;
import javax.swing.JComboBox;
import org.testng.annotations.Test;

/*
 *  Copyright (C) 2010-2015 JPEXS
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
/**
 *
 * @author JPEXS
 */
public class AdvancedSettingsTest {

    @Test
    public void testAdvancedSettginsDialog() {
        ResourceBundle resourceBundle = ResourceBundle.getBundle(AppStrings.getResourcePath(AdvancedSettingsDialog.class));
        AdvancedSettingsDialog.getCategories(new HashMap<>(), new HashMap<>(), new JComboBox<>(), resourceBundle);
    }
}
