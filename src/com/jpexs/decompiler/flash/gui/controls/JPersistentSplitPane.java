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
package com.jpexs.decompiler.flash.gui.controls;

import com.jpexs.decompiler.flash.configuration.ConfigurationItem;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import javax.swing.JSplitPane;

/**
 *
 * @author JPEXS
 */
public class JPersistentSplitPane extends JSplitPane {

    public JPersistentSplitPane(int newOrientation, ConfigurationItem<Integer> config) {
        super(newOrientation);
        initialize(config);
    }

    public JPersistentSplitPane(int newOrientation,
            Component newLeftComponent,
            Component newRightComponent,
            ConfigurationItem<Integer> config) {
        super(newOrientation, newLeftComponent, newRightComponent);
        initialize(config);
    }

    private void initialize(ConfigurationItem<Integer> config) {
        setResizeWeight(0.5);

        addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, (PropertyChangeEvent pce) -> {
            if (getLeftComponent().isVisible() && getRightComponent().isVisible()) {
                int width = ((JSplitPane) pce.getSource()).getWidth();
                if (width != 0) {
                    int p = Math.round((100.0f * (Integer) pce.getNewValue() / width));
                    config.set(p);
                }
            }
        });
    }
}
