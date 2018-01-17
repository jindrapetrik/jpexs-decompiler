/*
 *  Copyright (C) 2010-2018 JPEXS
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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.beans.PropertyChangeEvent;
import java.util.Date;
import javax.swing.JSplitPane;

/**
 *
 * @author JPEXS
 */
public class JPersistentSplitPane extends JSplitPane {

    private ConfigurationItem<Double> config;

    private boolean painted = false;

    private Date resize = getDateAfter(500);

    private ComponentListener childComponentListener;

    public JPersistentSplitPane(int newOrientation, ConfigurationItem<Double> config) {
        super(newOrientation);
        initialize(config);
    }

    public JPersistentSplitPane(int newOrientation,
            Component newLeftComponent,
            Component newRightComponent,
            ConfigurationItem<Double> config) {
        super(newOrientation, newLeftComponent, newRightComponent);
        initialize(config);
        newLeftComponent.addComponentListener(childComponentListener);
        newRightComponent.addComponentListener(childComponentListener);
    }

    private double getConfigValue(ConfigurationItem<Double> config) {
        double pos = config.get();
        if (pos < 0) {
            pos = 0;
        } else if (pos > 1) {
            pos = 1;
        }

        return pos;
    }

    private void initialize(ConfigurationItem<Double> config) {
        this.config = config;
        double pos = getConfigValue(config);
        //System.out.println("init " + config.getName() + ": " + pos);
        setDividerLocation(pos);
        setResizeWeight(pos);

        addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                resize = getDateAfter(500);
                double pos = getConfigValue(config);
                //System.out.println("resized " + resize.getTime() + " " + config.getName() + ": " + pos);
                setDividerLocation(pos);
                setResizeWeight(pos);
            }

            @Override
            public void componentShown(ComponentEvent e) {
                double pos = getConfigValue(config);
                //System.out.println("shown " + config.getName() + ": " + pos);
                setDividerLocation(pos);
                setResizeWeight(pos);
            }
        });

        childComponentListener = new ComponentAdapter() {

            @Override
            public void componentShown(ComponentEvent e) {
                double pos = getConfigValue(config);
                //System.out.println("childShown " + config.getName() + ": " + pos);
                setDividerLocation(pos);
                setResizeWeight(pos);
            }
        };

        addContainerListener(new ContainerListener() {

            @Override
            public void componentAdded(ContainerEvent e) {
                e.getChild().addComponentListener(childComponentListener);
            }

            @Override
            public void componentRemoved(ContainerEvent e) {
                e.getChild().removeComponentListener(childComponentListener);
            }
        });

        addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, (PropertyChangeEvent pce) -> {
            if (!painted) {
                return;
            }

            // hack
            long diff = new Date().getTime() - resize.getTime();
            if (diff < 0) {
                resize = getDateAfter(100);
                //System.out.println("set after resize " + diff + " " + config.getName());
                return;
            }

            if (getLeftComponent() != null && getRightComponent() != null && getLeftComponent().isVisible() && getRightComponent().isVisible()) {
                JPersistentSplitPane pane = (JPersistentSplitPane) pce.getSource();
                double size = (getOrientation() == JSplitPane.HORIZONTAL_SPLIT
                        ? pane.getWidth() : pane.getHeight()) - pane.getDividerSize();
                if (size != 0) {
                    double p = (Integer) pce.getNewValue() / size;
                    setResizeWeight(p);
                    //System.out.println("set " + diff + " " + config.getName() + ": " + p);
                    config.set(p);
                }
            }
        });
    }

    @Override
    public void setSize(Dimension d) {
        resize = getDateAfter(500);
        super.setSize(d);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        painted = true;
    }

    private static Date getDateAfter(int ms) {
        Date d = new Date();
        d.setTime(d.getTime() + ms);
        return d;
    }
}
