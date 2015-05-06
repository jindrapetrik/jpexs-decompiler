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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import javax.swing.JSplitPane;

/**
 *
 * @author JPEXS
 */
public class JPersistentSplitPane extends JSplitPane {

    private ConfigurationItem<Integer> config;

    private boolean resize = false;

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

    private double getConfigValue(ConfigurationItem<Integer> config) {
        double pos = config.get() / 100.0;
        if (pos < 0) {
            pos = 0;
        } else if (pos > 1) {
            pos = 1;
        }

        return pos;
    }

    private void initialize(ConfigurationItem<Integer> config) {
        this.config = config;
        double pos = getConfigValue(config);
        setDividerLocation(pos);
        setResizeWeight(pos);

        addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                resize = true;
                double pos = getConfigValue(config);
                setDividerLocation(pos);
            }

            @Override
            public void componentShown(ComponentEvent e) {
                double pos = getConfigValue(config);
                setDividerLocation(pos);
            }
        });

        addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, (PropertyChangeEvent pce) -> {
            if (resize) {
                return;
            }

            if (getLeftComponent().isVisible() && getRightComponent().isVisible()) {
                JPersistentSplitPane pane = (JPersistentSplitPane) pce.getSource();
                int width = pane.getWidth() - pane.getDividerSize();
                if (width != 0) {
                    int p = Math.round(100.0f * (Integer) pce.getNewValue() / width);
                    config.set(p);
                }
            }
        });
    }
}
